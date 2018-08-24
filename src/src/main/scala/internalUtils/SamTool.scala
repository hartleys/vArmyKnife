package internalUtils

import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.Writer;
import java.io.File;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import scala.collection.JavaConverters._

//import scala.collection.JavaConversions._
import internalUtils.optionHolder._;
import internalUtils.Reporter._;
import internalUtils.stdUtils._;

import htsjdk.samtools._;
import internalUtils.fileUtils._;

object SamTool {
  
  final val SAM_PEEK_LINECT = 10000;
  final val SAM_TESTRUN_LINECT = 100000;
  
  abstract class SamToFastqConverter {
    def writeReads(r1 : SAMRecord, r2 : SAMRecord, readNameSuffix : String = "");
    def close();
    
    def readToFastq(r : SAMRecord, readNameSuffix : String = "") : Seq[String] = {
      val rev = r.getReadNegativeStrandFlag()
      Seq[String](
        r.getReadName()+readNameSuffix,
        if(rev){
          r.getReadBases().reverse.map(internalUtils.commonSeqUtils.reverseBaseByteArray(_).toChar).mkString("");
        } else {
          r.getReadBases().map(_.toChar).mkString("");
        },
        "+",
        if(rev){
          r.getOriginalBaseQualities().reverse.map(_.toChar).mkString("");
        } else {
          r.getOriginalBaseQualities().map(_.toChar).mkString("");
        }
      );
    }
  }
  case class NullSamToFastqConverter() extends SamToFastqConverter {
    def writeReads(r1 : SAMRecord, r2 : SAMRecord, readNameSuffix : String = ""){
      //do nothing
    }
    def close(){
      //do nothing
    }
  }
  case class SingleEndSamToFastq(filePrefix : String, ext : String = ".fq.gz") extends SamToFastqConverter{
    val writer = openWriterSmart(filePrefix+ext,false);
    def close(){
      writer.close();
    }
    def writeReads(r1 : SAMRecord, r2 : SAMRecord, readNameSuffix : String = ""){
      writer.write(  readToFastq(r1, readNameSuffix = readNameSuffix).mkString("\n") +"\n");
    }
  }
  case class PairedEndSamToFastq(filePrefix : String, ext : String = ".fq.gz") extends SamToFastqConverter{
    val writer1 = openWriterSmart(filePrefix+".1"+ext,false);
    val writer2 = openWriterSmart(filePrefix+".2"+ext,false);
    
    def close(){
      writer1.close();
      writer2.close();
    }
    def writeReads(r1 : SAMRecord, r2 : SAMRecord, readNameSuffix : String = ""){
      writer1.write(  readToFastq(r1, readNameSuffix = readNameSuffix).mkString("\n") +"\n");
      writer2.write(  readToFastq(r2, readNameSuffix = readNameSuffix).mkString("\n") +"\n");
    }
  }
  
  
  
  def getPairedEndReader(inbam : String, 
                         isSingleEnd : Boolean,
                         sortPairsByFirstPosition : Boolean = false,
                         unsorted : Boolean = true,
                         maxPhredScore : Int = 41,
                         stopAfterNReads : Option[Int],
                         testRunLineCt : Int,
                         testRun : Boolean,
                         maxReadLength : Option[Int],
                         strict : Boolean = true) : (Iterator[(SAMRecord,SAMRecord)],SAMFileHeader,SamFileAttributes) = {
    val readerFactory = SamReaderFactory.makeDefault();
    
    val reader = if(inbam == "-"){
      readerFactory.open(SamInputResource.of(System.in));
    } else {
      readerFactory.open(new File(inbam));
    }
    
    val peekCt = SAM_PEEK_LINECT;
    val testRunLineCt = stopAfterNReads.getOrElse(SAM_TESTRUN_LINECT);

    val (samFileAttributes, recordIter, samHeader) = initSamRecordIterator(reader, peekCount = peekCt, maxQual = maxPhredScore);
    
    val maxObservedReadLength = samFileAttributes.readLength;
    val readLength = if(maxReadLength.isEmpty) maxObservedReadLength else maxReadLength.get;
    val isSortedByNameLexicographically = samFileAttributes.isSortedByNameLexicographically;
    val isSortedByPosition = samFileAttributes.isSortedByPosition;
    val isDefinitelyPairedEnd = samFileAttributes.isDefinitelyPairedEnd;
    val minReadLength = samFileAttributes.minReadLength;
    
    //Debugging info:
    reportln("   Stats on the first "+peekCt+ " reads:","debug");
    reportln("        Num Reads Primary Map:    " + samFileAttributes.numPeekReadsMapped,"debug");
    reportln("        Num Reads Paired-ended:   " + samFileAttributes.numPeekReadsPaired,"debug");
    reportln("        Num Reads mapped pair:    " + samFileAttributes.numPeekReadsPairMapped,"debug");
    reportln("        Num Pair names found:     " + samFileAttributes.numPeekPairs,"debug");
    reportln("        Num Pairs matched:        " + samFileAttributes.numPeekPairsMatched,"debug");
    reportln("        Read Seq length:          " + samFileAttributes.simpleMinReadLength + " to " + samFileAttributes.simpleMaxReadLength,"debug");
    reportln("        Unclipped Read length:    " + samFileAttributes.minReadLength + " to " + samFileAttributes.readLength,"debug");
    reportln("        Final maxReadLength:      " + readLength,"debug");
    reportln("        maxPhredScore:            " + samFileAttributes.maxObservedQual,"debug");
    reportln("        minPhredScore:            " + samFileAttributes.minObservedQual,"debug");
    

    if(samFileAttributes.maxObservedQual > maxPhredScore){
            reportln("WARNING WARNING WARNING: \n"+
               "   SAM format check:\n"+
               "      Phred Qual > "+maxPhredScore+"!\n"+
               "      You will need to set either --adjustPhredScores or --maxPhredScores\n"+
               "      in order to compute Phred quality metrics! QoRTs WILL throw an error\n"+
               "      if quality metrics are attempted!","warn");
    }
    
    if(readLength != minReadLength){ reportln("NOTE: Read length is not consistent.\n"+
                                             "   In the first "+peekCt+" reads, read length varies from "+minReadLength+" to " +maxObservedReadLength+" (param maxReadLength="+readLength+")\n"+
                                             "Note that using data that is hard-clipped prior to alignment is NOT recommended, because this makes it difficult (or impossible) "+
                                             "to determine the sequencer read-cycle of each nucleotide base. This may obfuscate cycle-specific artifacts, trends, or errors, the detection of which is one of the primary purposes of QoRTs!"+
                                             "In addition, hard clipping (whether before or after alignment) removes quality score data, and thus quality score metrics may be misleadingly optimistic. "+
                                             "A MUCH preferable method of removing undesired sequence is to replace such sequence with N's, which preserves the quality score and the sequencer cycle information.","note")}
    if((readLength != minReadLength) & (maxReadLength.isEmpty)){
      reportln("   WARNING WARNING WARNING: Read length is not consistent, AND \"--maxReadLength\" option is not set!\n"+
               "      QoRTs has ATTEMPTED to determine the maximum read length ("+readLength+").\n"+
               "      It is STRONGLY recommended that you use the --maxReadLength option \n"+
               "      to set the maximum possible read length, or else errors may occur if/when \n"+
               "      reads longer than "+readLength+ " appear.","warn")
    }
    
    val variableReadLen = readLength != minReadLength || (! maxReadLength.isEmpty);
     
    if(samFileAttributes.allReadsMarkedPaired & isSingleEnd) reportln("   WARNING WARNING WARNING! Running in single-end mode, but reads appear to be paired-end! Errors may follow.\n"+
                                                                      "           Strongly recommend removing the '--isSingleEnd' option!","warn");
    if(samFileAttributes.allReadsMarkedSingle & (! isSingleEnd)) reportln("   WARNING WARNING WARNING! Running in paired-end mode, but reads appear to be single-end! Errors may follow.\n"+
                                                                          "           Strongly recommend using the '--isSingleEnd' option","warn");
    if(samFileAttributes.mixedSingleAndPaired) reportln("   WARNING WARNING WARNING! Data appears to be a mixture of single-end and paired-end reads!\n"+
                                                        "           QoRTs was not designed to function under these conditions. Errors may follow!","warn");


    if(! isSingleEnd){ 
      reportln("   Note: Data appears to be paired-ended.","debug");
      
      var sortWarning = false;
      
      if( ! (samFileAttributes.perfectPairing || isSortedByPosition) ){
        reportln("   WARNING: Reads do not appear to be sorted by coordinate or by name. Sorting input data is STRONGLY recommended, but not technically required.","warn");
        sortWarning = true;
      }
      if(samFileAttributes.numPeekPairsMatched == 0){
        reportln("   Warning: Have not found any matched read-pairs in the first "+peekCt+" reads. Is data paired-end? Is data sorted?","warn"); 
        if(samFileAttributes.malformedPairNameCt > 0){
          reportln("   WARNING: No read-pairs found, but there are reads that match exactly\n"+
                   "            except for the last character, which is \"1\" in one read \n"+
                   "            and \"2\" in the other. This may indicate a malformed SAM \n"+
                   "            file in which the read-pairs are named with their readID \n"+
                   "            rather than read-pair ID. In standard SAM files, paired \n"+
                   "            reads MUST have the EXACT SAME first column.",
                   "warn");
        }
        sortWarning = true;
      }
      if( (! isDefinitelyPairedEnd)){ 
        reportln("   Warning: Have not found any matched read pairs in the first "+peekCt+" reads. Is data paired-end? Use option --singleEnd for single-end data.","warn");
        sortWarning = true;
      }
      if( isSortedByPosition & (! unsorted )){ 
        reportln("   Warning: SAM/BAM file appears to be sorted by read position, but you are running in --nameSorted mode.\n"+
                 "            If this is so, you should probably omit the '--nameSorted' option, as errors may follow.","warn"); 
        sortWarning = true;
      }
      
      val isOkNote = if(! sortWarning){"(This is OK)."} else {""}
      if(samFileAttributes.perfectPairing){
        reportln("   Sorting Note: Reads appear to be grouped by read-pair, probably sorted by name"+isOkNote,"note");
      } else {
        reportln("   Sorting Note: Reads are not sorted by name "+isOkNote,"note");
      }
      if(isSortedByPosition){
        reportln("   Sorting Note: Reads are sorted by position "+isOkNote,"note");
      } else {
        reportln("   Sorting Note: Reads are not sorted by position "+isOkNote,"note");
      }
      
      //Samtools sorts in an odd way! Delete name sort check:
      //if( ((! isSortedByNameLexicographically) & (! unsorted ))) reportln("Note: SAM/BAM file does not appear to be sorted lexicographically by name (based on the first "+peekCt+" reads). It is (hopefully) sorted by read name using the samtools method.","debug");
    }
    if(internalUtils.Reporter.hasWarningOccurred()){
      reportln("Done checking first " + SAM_PEEK_LINECT + " reads. WARNINGS FOUND!","note");
    } else{
      reportln("Done checking first " + SAM_PEEK_LINECT + " reads. No major problems detected!","note");
    }
    
    val pairedIter : Iterator[(SAMRecord,SAMRecord)] = 
      if(isSingleEnd){
        if(testRun) samRecordPairIterator_withMulti_singleEnd(recordIter, true, testRunLineCt,isSingleEnd = isSingleEnd) else samRecordPairIterator_withMulti_singleEnd(recordIter,isSingleEnd = isSingleEnd);
      } else {
        if(unsorted){
          if(sortPairsByFirstPosition){
             if(testRun) samRecordPairIterator_resorted(recordIter, true, testRunLineCt,isSingleEnd = isSingleEnd,strict=strict) else samRecordPairIterator_resorted(recordIter,isSingleEnd = isSingleEnd,strict=strict)
          } else {
             //if(testRun) samRecordPairIterator_unsorted(recordIter, true, testRunLineCt) else samRecordPairIterator_unsorted(recordIter)
             if(testRun) samRecordPairIterator_resorted(recordIter, true, testRunLineCt,isSingleEnd = isSingleEnd,strict=strict) else samRecordPairIterator_resorted(recordIter,isSingleEnd = isSingleEnd,strict=strict)
          }
        // Faster noMultiMapped running is DEPRECIATED!
        //} else if(noMultiMapped){
        //  if(testRun) samRecordPairIterator(recordIter, true, 200000) else samRecordPairIterator(recordIter)
        } else {
          if(testRun) samRecordPairIterator_withMulti(recordIter, true, testRunLineCt,isSingleEnd = isSingleEnd) else samRecordPairIterator_withMulti(recordIter,isSingleEnd = isSingleEnd)
        }
      }
    
    reportln("SAMRecord Reader Generated. Read length: "+readLength+".","note");
    
    return ((pairedIter,samHeader,samFileAttributes))
  }
  
  case class SamFileAttributes(readLength : Int, 
                               isSortedByNameLexicographically : Boolean,
                               isSortedByPosition : Boolean, 
                               isDefinitelyPairedEnd : Boolean, 
                               allReadsMarkedPaired : Boolean,
                               allReadsMarkedSingle : Boolean,
                               mixedSingleAndPaired : Boolean,
                               minReadLength : Int,
                               numPeekReads : Int,
                               numPeekReadsMapped : Int,
                               numPeekReadsPaired : Int,
                               numPeekReadsPairMapped : Int,
                               numPeekPairs : Int,
                               numPeekPairsMatched : Int,
                               malformedPairNameCt : Int,
                               malformedPairNames : Boolean,
                               perfectPairing : Boolean,
                               simpleMaxReadLength : Int,
                               simpleMinReadLength : Int,
                               maxObservedQual : Int,
                               minObservedQual : Int
                               );
  
  def initSamRecordIterator(reader : SamReader, peekCount : Int = 1000, bufferSize : Int = 10000, maxQual : Int = 41) : (SamFileAttributes, Iterator[SAMRecord],SAMFileHeader) = {
    
    val header = reader.getFileHeader();
    
    // Check alignment software
    val alignmentProgramList : List[SAMProgramRecord] = header.getProgramRecords().asScala.toList;
    
    //Check for tophat 2, print note:
    alignmentProgramList.find( (pr : SAMProgramRecord) => pr.getId() == "TopHat" ) match {
      case Some(pr) => {
        reportln("   Note: Detected TopHat Alignment Program.","note");
        val tophatVer = pr.getProgramVersion();
        reportln("         Version: \""+tophatVer+"\"","note");
        if(tophatVer.substring(0,1) == "2"){
          reportln("   IMPORTANT NOTE: Detected TopHat Alignment Program, version > 2. \n"+
                   "       TopHat v2+ uses a different MAPQ convention than most aligners. \n"+
                   "       Make sure you set the --minMAPQ parameter to 50 if you want to ignore \n"+
                   "       multi-mapped reads.","note");
        }
      }
      case None => {
        //Do nothing.
      }
    }
    
    
    val iter : Iterator[SAMRecord] = reader.iterator().asScala;
    var peekRecords = Seq[SAMRecord]();
     
    for(i <- 0 until peekCount){
      if(iter.hasNext){
        val next = iter.next;
        try {
          peekRecords = peekRecords :+ next;
        } catch {
          case e : Exception => throw e;
        }
      } else { 
        //do nothing!
      }
    }
    //val maxQual = qcUtils.qcQualityScoreCounter.MAX_QUALITY_SCORE.toByte;
    val minQual = 0.toByte;
    
    val minObservedQual = peekRecords.map(r => {
      r.getBaseQualities.min
    }).min;
    val maxObservedQual = peekRecords.map(r => {
      r.getBaseQualities.max
    }).max;
    
    val isPhred33 : Boolean = peekRecords.forall(r => {
       r.getBaseQualities().forall(q => {
         q <= maxQual && q >= minQual;
       })
    })
    if(! isPhred33) {
      reportln("NOTE: \n"+
               "   SAM format check:\n"+
               "      Base Qualities are not "+minQual+" <= Q <= " + maxQual +"!\n"+
               "      The SAM file specification requires that Phred Quality scores \n"+
               "      be formatted in Phred+33 format, and the scores are (usually) supposed \n"+
               "      to fall in this range. Make sure you have properly set the \n "+
               "      --adjustPhredScore and/or --maxPhredScore parameters. \n"+
               "      If these parameters are not properly set, then errors may follow if further\n"+
               "      anaylses/functions use quality scores for anything.","note");
    }
    
    
    //val isSortedByName : Boolean = (Iterator.from(1,2).takeWhile(_ < peekRecords.size).map(peekRecords(_))).zip( (Iterator.from(0,2).takeWhile(_ < peekRecords.size).map(peekRecords(_))) ).forall( (r12) => r12._1.getReadName == r12._2.getReadName );
    val isSortedByNameLexicographically : Boolean = seqIsSortedBy(peekRecords, (r1 : SAMRecord, r2 : SAMRecord) => {
        val n1 = r1.getReadName();
        val n2 = r2.getReadName();
        n1.compareTo(n2) <= 0;
    })
    
    val isDefinitelyPairedEnd : Boolean = peekRecords.exists( r => ( peekRecords.count(_.getReadName() == r.getReadName()) > 1 ) );
    //val readLength = peekRecords.maxBy( _.getReadLength ).getReadLength;
    
    val allReadLengths = peekRecords.map( (r : SAMRecord) => internalUtils.commonSeqUtils.getUnclippedReadLength(r));
    val allSimpleReadLengths = peekRecords.map( (r : SAMRecord) => r.getReadLength());
    
    
    //val allReadLengths = peekRecords.map( (r : SAMRecord) => {  
    //  val baseLength = r.getReadLength();
    //  if(! r.getReadUnmappedFlag()){
    //    val cigar = r.getCigar().getCigarElements();
    //    val leftClip  = if(cigar.head.getOperator() == CigarOperator.HARD_CLIP) cigar.head.getLength() else 0;
    //    val rightClip = if(cigar.last.getOperator() == CigarOperator.HARD_CLIP) cigar.last.getLength() else 0;
    //    baseLength + leftClip + rightClip;
    //  } else {
    //    baseLength;
    //  }
    //});
    val readLength = allReadLengths.max;
    val minReadLength = allReadLengths.min;
    val simpleMaxReadLength = allSimpleReadLengths.max;
    val simpleMinReadLength = allSimpleReadLengths.min;
    
    val isSortedByPosition : Boolean = seqIsSortedBy(peekRecords, (r1 : SAMRecord, r2 : SAMRecord) => {
        val start1 = r1.getAlignmentStart();
        val start2 = r2.getAlignmentStart();
        val chrom1 = r1.getReferenceName();
        val chrom2 = r2.getReferenceName();
        if(chrom1 == chrom2){
          start1 <= start2;
        } else {
          true;
        }
    })
    
    val peekPrimary : Seq[SAMRecord] = peekRecords.filter(r => ! (r.getReadUnmappedFlag() || r.getNotPrimaryAlignmentFlag())).toVector;
    val peekPairMapped : Seq[SAMRecord] = peekPrimary.filter(r => r.getReadPairedFlag() && (! internalUtils.commonSeqUtils.getMateUnmappedFlag(r))).toVector;
    
    val numPeekReads : Int = peekRecords.length;
    val numPeekReadsPrimary : Int = peekPrimary.length;
    val numPeekReadsPaired : Int = peekRecords.count(r => r.getReadPairedFlag());
    val numPeekReadsPairMapped : Int = peekPairMapped.length;
    val peekReadNameSet : Set[String] = peekPairMapped.map(_.getReadName()).toSet;
    val numPeekPairs : Int = peekReadNameSet.size;
    val numPeekPairsMatched : Int = peekReadNameSet.count( id => {
      peekPairMapped.count(r => r.getReadName() == id) == 2;
    });
    
    val evenMappedPairs = zipIteratorWithCount(peekPairMapped.iterator).filter(x => x._2 % 2 == 0).map(x => x._1);
    val oddMappedPairs = zipIteratorWithCount(peekPairMapped.iterator).filter(x => x._2 % 2 != 0).map(x => x._1);
    val perfectPairing : Boolean = evenMappedPairs.zip(oddMappedPairs).forall( r12 => {
        val (r1,r2) = r12;
        r1.getReadName() == r2.getReadName();
    });
    
    
    //val minReadLength : Int = peekRecords.minBy( _.getReadLength ).getReadLength;
    
    val outIter = if(bufferSize == 0){ 
      peekRecords.iterator ++ iter
    } else {
      peekRecords.iterator ++ bufferIterator(iter, bufferSize);
    }
    
    val allReadsMarkedPaired : Boolean = peekRecords.forall( _.getReadPairedFlag);
    val allReadsMarkedSingle : Boolean = peekRecords.forall(! _.getReadPairedFlag);
    val mixedSingleAndPaired : Boolean = ! (allReadsMarkedPaired | allReadsMarkedSingle);
    
    //Check for malformed pair ID's:
    val malformedPairNameCt : Int = if( numPeekPairsMatched > 0 || 
                                        peekReadNameSet.exists(_.length < 2) ||
                                        (! allReadsMarkedPaired)
                                      ) {
      0;
    } else {
      peekReadNameSet.count( (n1) => {
                                       (n1.substring(n1.length-1,n1.length) == "1") &&
                                       (peekReadNameSet.contains( n1.substring(0,n1.length-1) + "2" ))
                                       }
                            );
    }
    
    val malformedPairNames : Boolean = malformedPairNameCt > 0;
    
    return ((SamFileAttributes(readLength, 
                               isSortedByNameLexicographically, 
                               isSortedByPosition, 
                               isDefinitelyPairedEnd, 
                               allReadsMarkedPaired, 
                               allReadsMarkedSingle, 
                               mixedSingleAndPaired, 
                               minReadLength,
                               numPeekReads,
                               numPeekReadsPrimary,
                               numPeekReadsPaired,
                               numPeekReadsPairMapped,
                               numPeekPairs,
                               numPeekPairsMatched,
                               malformedPairNameCt,
                               malformedPairNames,
                               perfectPairing,
                               simpleMaxReadLength,
                               simpleMinReadLength,
                               maxObservedQual,
                               minObservedQual), 
            outIter, header));
  }
  
  
  object presetProgressReporters {
    val DEFAULT_ITERATOR_PROGRESS_REPORTER_READPAIRS : IteratorProgressReporter = IteratorProgressReporter_ThreeLevel("Read-Pairs", 100000, 1000000, 1000000);
    
    def wrapIterator_readPairs[B](iter : Iterator[B], verbose : Boolean = true, isSingleEnd : Boolean = false, linesec : Int = 60, cutoff : Int = -1) : Iterator[B] = {
      val DEFAULT_ITERATOR_PROGRESS_REPORTER_READPAIRS = 
                      AdvancedIteratorProgressReporter_ThreeLevelAuto[B](if(isSingleEnd){"reads"}else{"read-pairs"}, lineSec = 300,
                                                                           reportFunction = ((a : B, i : Int) => ""));
      val iter2 = if(verbose){
        wrapIteratorWithAdvancedProgressReporter(iter,DEFAULT_ITERATOR_PROGRESS_REPORTER_READPAIRS)
      } else {
        iter;
      }
      val iter3 = if(cutoff == -1){
        iter2;
      } else {
        iter2.take(cutoff);
      }
      return iter3;
    }
  }
  
  def samRecordPairIterator_resorted(iter : Iterator[SAMRecord], verbose : Boolean = true, testCutoff : Int = -1, ignoreSecondary : Boolean = true, isSingleEnd : Boolean, strict : Boolean = true) : Iterator[(SAMRecord,SAMRecord)] = {

    if(ignoreSecondary){
       presetProgressReporters.wrapIterator_readPairs(getSRPairIterResorted(iter.filter((read : SAMRecord) => {
           (! read.getNotPrimaryAlignmentFlag()) && (! commonSeqUtils.getMateUnmappedFlag(read)) && (! read.getReadUnmappedFlag())
         }),strict = strict), verbose=verbose, cutoff=testCutoff,isSingleEnd=isSingleEnd);
    } else {
      error("FATAL ERROR: Using non-primary read mappings is not currently implemented!");
      return null;
    }
  }
  
  def samRecordPairIterator_unsorted(iter : Iterator[SAMRecord], verbose : Boolean = true, testCutoff : Int = -1, ignoreSecondary : Boolean = true, isSingleEnd : Boolean, strict : Boolean =true) : Iterator[(SAMRecord,SAMRecord)] = {

    if(ignoreSecondary){
       presetProgressReporters.wrapIterator_readPairs(getSRPairIterUnsorted(iter.filter((read : SAMRecord) => {
           (! read.getNotPrimaryAlignmentFlag()) && (! commonSeqUtils.getMateUnmappedFlag(read)) && (! read.getReadUnmappedFlag())
         }),strict=strict), verbose=verbose, cutoff=testCutoff,isSingleEnd=isSingleEnd);
    } else {
      error("FATAL ERROR: Using non-primary read mappings is not currently implemented!");
      return null;
    }
  }
  def samRecordPairIterator_withMulti(iter : Iterator[SAMRecord], verbose : Boolean = true, testCutoff : Int = -1, ignoreSecondary : Boolean = true, isSingleEnd : Boolean) : Iterator[(SAMRecord,SAMRecord)] = {
    if(ignoreSecondary){
      presetProgressReporters.wrapIterator_readPairs(getSRPairIter(iter.filter((read : SAMRecord) => {
        (! read.getNotPrimaryAlignmentFlag()) && (! commonSeqUtils.getMateUnmappedFlag(read)) && (! read.getReadUnmappedFlag())
      })), verbose=verbose, cutoff=testCutoff,isSingleEnd=isSingleEnd);
    } else {
      error("FATAL ERROR: Using non-primary read mappings is not currently implemented!");
      return null;
    }
  }
  def samRecordPairIterator_withMulti_singleEnd(iter : Iterator[SAMRecord], verbose : Boolean = true, testCutoff : Int = -1, ignoreSecondary : Boolean = true, isSingleEnd : Boolean) : Iterator[(SAMRecord,SAMRecord)] = {
    if(ignoreSecondary){
      presetProgressReporters.wrapIterator_readPairs(getSRPairIter_singleEnd(iter.filter((read : SAMRecord) =>{
        (! read.getNotPrimaryAlignmentFlag()) & (! read.getReadUnmappedFlag())
      })), verbose=verbose, cutoff=testCutoff,isSingleEnd=isSingleEnd);
    } else {
      error("FATAL ERROR: Using non-primary read mappings is not currently implemented!");
      return null;
    }
  }
  
  

  private def getSRPairIter_singleEnd(iter : Iterator[SAMRecord]) : Iterator[(SAMRecord,SAMRecord)] = {
    return iter.map( (next : SAMRecord) => (next,next) );
  }
  
  private def getSRPairIter(iter : Iterator[SAMRecord]) : Iterator[(SAMRecord,SAMRecord)] = {
    return new  Iterator[(SAMRecord,SAMRecord)] {
      def hasNext : Boolean = iter.hasNext;
      def next : (SAMRecord,SAMRecord) = {
        val rA = iter.next;
        val rB = iter.next;
        if(rA.getReadName != rB.getReadName){
          error("FATAL ERROR: SAMRecord is improperly paired! Is the file sorted by name?\n"+
                "    Offending reads: "+rA.getReadName + " != " + rB.getReadName +"\n"+
                "If the file is not sorted by name then you should included the '--coordSorted' parameter.\n"+
                "(Note: in coordSorted mode it is highly recommended but not actually required that the file be sorted by position)\n"+
                "This problem could also have a number of other causes: if there are orphaned reads that aren't marked as such in the sam flags, for example."
                );
        }
        if(rA.getFirstOfPairFlag) return( (rA,rB) );
        else return( (rB,rA) );
      }
    }
  }
  
  abstract class PairIteratorWithLimit extends Iterator[(SAMRecord,SAMRecord)] {
    def hasNext : Boolean;
    def next : (SAMRecord,SAMRecord);
    def bufferLimit : Int;
  }
  
  private def getSRPairIterUnsorted(iter : Iterator[SAMRecord], strict : Boolean = true) : Iterator[(SAMRecord,SAMRecord)] = {
    val initialPairContainerWarningSize = 100000;
    val warningSizeMultiplier = 2;
    
    return new Iterator[(SAMRecord,SAMRecord)] {
      val pairContainer = scala.collection.mutable.AnyRefMap[String,SAMRecord]();
      //NOTE: The above container type should never contain more than 500 million reads. If this is a problem, then something is terribly wrong.
      var pairContainerWarningSize = initialPairContainerWarningSize;
      
      //def bufferLimits : 
      
      def hasNext : Boolean = iter.hasNext;
      def next : (SAMRecord,SAMRecord) = {
        var curr = iter.next;
        
        while((! pairContainer.contains(curr.getReadName())) && iter.hasNext) {
          pairContainer(curr.getReadName()) = curr;
          if(pairContainerWarningSize < pairContainer.size){
              reportln("NOTE: Unmatched Read Buffer Size > "+pairContainerWarningSize+" [Mem usage:"+MemoryUtil.memInfo+"]","note");
              if(pairContainerWarningSize == initialPairContainerWarningSize){
                reportln("    (This is generally not a problem, but if this increases further then OutOfMemoryExceptions\n"+
                         "    may occur.\n"+
                         "    If memory errors do occur, either increase memory allocation or sort the bam-file by name\n"+
                         "    and rerun with the '--nameSorted' option.\n"+
                         "    This might also indicate that your dataset contains an unusually large number of\n"+
                         "    chimeric read-pairs. Or it could occur simply due to the presence of genomic\n"+
                         "    loci with extremly high coverage. It may also indicate a SAM/BAM file that \n"+
                         "    does not adhere to the standard SAM specification.)", "note");
              }
              pairContainerWarningSize = pairContainerWarningSize * warningSizeMultiplier;
          }
          curr = iter.next;
        }
        
        if(! pairContainer.contains(curr.getReadName()) ){
          internalUtils.Reporter.error("ERROR ERROR ERROR: Reached end of bam file, there are "+(pairContainer.size+1)+" orphaned reads, which are marked as having a mapped pair, but no corresponding pair is found in the bam file. \n(Example Orphaned Read Name: "+curr.getReadName()+")");
        }

        
        val rB = pairContainer.remove(curr.getReadName()).get;
        if(curr.getFirstOfPairFlag()) return((curr, rB));
        else return((rB, curr));
      }
    }
  }
  
  

  private def getSRPairIterResorted(iter : Iterator[SAMRecord], strict : Boolean = true) : Iterator[(SAMRecord,SAMRecord)] = {
    reportln("Starting getSRPairIterResorted...","debug");
    
    val initialPairContainerWarningSize = 100000;
    val warningSizeMultiplier = 2;
    
    var lnct = 0;
    
    if(! iter.hasNext){
      return(Iterator[(SAMRecord,SAMRecord)]());
    } else {
      return new Iterator[(SAMRecord,SAMRecord)] {
        val pairContainer = scala.collection.mutable.HashMap[String,SAMRecord]();
              reportln("    INIT STATUS:  " +pairContainer.size + ", "+pairContainer.keySet.size + ", "+pairContainer.keySet.toList.size+"\n"+
                       "       CONTENTS: '"+pairContainer.keySet.toList.take(10).mkString(",")+"'","debug");
        //NOTE: The above container type should never contain more than 500 million reads. If this is a problem, then something is terribly wrong.
        var pairContainerWarningSize = initialPairContainerWarningSize;
        var bufferWarningSize = initialPairContainerWarningSize;
        
        var buffer = scala.collection.mutable.HashMap[String,(SAMRecord,SAMRecord)]();
        var readOrder = Vector[String]();
        
        var pairContainerOpHistory = Vector[String]();
        def updateOpHistory(op : String){
          pairContainerOpHistory = pairContainerOpHistory :+ op
          if(pairContainerOpHistory.length > 100){
            pairContainerOpHistory = pairContainerOpHistory.tail;
          }
        }
        
        def bufferHasNext : Boolean = iter.hasNext;
        def addNextPairToBuffer {
          var curr = iter.next;
          while((! pairContainer.contains(curr.getReadName())) && iter.hasNext) {
            readOrder = readOrder :+ curr.getReadName();
            pairContainer(curr.getReadName()) = curr;
            if(pairContainerWarningSize < pairContainer.size){
                reportln("NOTE: Unmatched Read Buffer Size > "+pairContainerWarningSize+" [Mem usage:"+MemoryUtil.memInfo+"]","note");
                if(pairContainerWarningSize == initialPairContainerWarningSize){
                  reportln("    (This is generally not a problem, but if this increases further then OutOfMemoryExceptions\n"+
                           "    may occur.\n"+
                           "    If memory errors do occur, either increase memory allocation or sort the bam-file by name\n"+
                           "    and rerun with the '--nameSorted' option.\n"+
                           "    This might also indicate that your dataset contains an unusually large number of\n"+
                           "    chimeric read-pairs. Or it could occur simply due to the presence of genomic\n"+
                           "    loci with extremly high coverage. It may also indicate a SAM/BAM file that \n"+
                           "    does not adhere to the standard SAM specification.)", "note");
                }
                pairContainerWarningSize = pairContainerWarningSize * warningSizeMultiplier;
            }
            lnct += 1;
            //if(lnct % 10000 == 0){
            //  reportln("    STATUS: " +pairContainer.size + ", "+pairContainer.keySet.size + ", "+pairContainer.keySet.toList.size+"\n"+
            //           "  CONTENTS: '"+pairContainer.keySet.toList.take(10).mkString(",")+"'","debug");
            //}
            //updateOpHistory("Add["+curr.getReadName()+"]");
            if(pairContainer.size != pairContainer.keySet.toList.size){
              warning("Impossible Paircontainer status! pairContainer.size != pairContainer.keySet.toList.size"+
                      "     Just added read: "+curr.getReadName() + " to pairContainer.\n","Impossible_Paircontainer_State",10)
            }
            curr = iter.next;            
          }
          
          if((! pairContainer.contains(curr.getReadName())) ){
            if(strict){
              internalUtils.Reporter.error("ERROR ERROR ERROR  (636): Reached end of bam file, there are "+(pairContainer.size+1)+" orphaned reads, "+
                                           "which are marked as having a mapped pair, but no "+
                                           "corresponding pair is found in the bam file. \n"+
                                           "(Example Orphaned Read Name: "+curr.getReadName()+")\n"+
                                           "(Read line: "+curr.getSAMString()+")"
                                           );
            } else {
              internalUtils.Reporter.warning("ERROR ERROR ERROR  (636): Reached end of bam file, there are "+(pairContainer.size+1)+" orphaned reads, "+
                                           "which are marked as having a mapped pair, but no "+
                                           "corresponding pair is found in the bam file. \n"+
                                           "(Example Orphaned Read Name: "+curr.getReadName()+")\n"+
                                           "(Read line: "+curr.getSAMString()+")","UNPAIRED_READ",-1
                                           );
              }
          }
          val rB = pairContainer.remove(curr.getReadName()).get;
          //updateOpHistory("Del["+curr.getReadName()+"]");

          if(pairContainer.size != pairContainer.keySet.toList.size){
              warning("Impossible pairContainer status! pairContainer.size != pairContainer.keySet.toList.size"+
                      "     Just removed read: "+curr.getReadName() + " from pairContainer.","Impossible_Paircontainer_State",10)
          }
          
          if(curr.getFirstOfPairFlag()) buffer.put(curr.getReadName(),(curr,rB))
          else buffer.put(curr.getReadName(),(rB,curr))
        }
        var readHistory = Vector[String]();
        def hasNext : Boolean = iter.hasNext || buffer.size > 0;
        def next : (SAMRecord,SAMRecord) = {
          if(readOrder.isEmpty){
            addNextPairToBuffer;
          }
          readHistory = readHistory :+ readOrder.head;
          if(readHistory.length > 100){
            readHistory = readHistory.tail;
          }
          val nextName = readOrder.head;
          readOrder = readOrder.tail;
          if(buffer.contains(nextName)) return buffer.remove(nextName).get;
          
          var searchCt = 0;
          while(iter.hasNext && (! buffer.contains(nextName))){
            addNextPairToBuffer;
            if(bufferWarningSize < buffer.size){
                reportln("NOTE: Unmatched Read-PAIR-Buffer Size > "+bufferWarningSize+" [Mem usage:"+MemoryUtil.memInfo+"]\n"+
                         "  Currently searching for read: " + nextName + " for "+searchCt + " iterations."+
                         "  Current pairContainer status: "+pairContainer.size+", "+pairContainer.keySet.size+", "+pairContainer.keySet.toList.size,"note");
                if(bufferWarningSize == initialPairContainerWarningSize){
                  reportln("    (This is generally not a problem, but if this increases further then OutOfMemoryExceptions\n"+
                           "    may occur.\n"+
                           "    If memory errors do occur, increase memory allocation.\n"+
                           "    This might also indicate that your dataset contains an unusually large number of\n"+
                           "    chimeric read-pairs. Or it could occur simply due to the presence of genomic\n"+
                           "    loci with extremly high coverage, large and complex splicing/indels, or other oddities.\n"+
                           "    It may also indicate a SAM/BAM file that \n"+
                           "    does not adhere to the standard SAM specification.)", "note");
                }
                bufferWarningSize = bufferWarningSize * warningSizeMultiplier;
            }
            searchCt += 1;
          }
          if(!buffer.contains(nextName)){
            //internalUtils.Reporter.error("ERROR ERROR ERROR (679): Reached end of bam file, there are "+(pairContainer.size+1)+" orphaned reads, which are marked as having a mapped pair, but no corresponding pair is found in the bam file. \n(Example Orphaned Read lines: "+nextName+")");
              internalUtils.Reporter.error("ERROR ERROR ERROR  (679): Reached end of bam file, there are "+(pairContainer.size+1)+" orphaned reads, "+
                                           "which are marked as having a mapped pair, but no "+
                                           "corresponding pair is found in the bam file. \n"+
                                           "(Example Orphaned Read Name: "+nextName+")\n"+
                                           "Recent read-pairs:\n"+
                                           "    "+readHistory.mkString("\n    ")+"\n"+
                                           "Contents of unpaired read buffer (up to 100): (keySet.size="+pairContainer.keySet.size +", keySet.toList.size="+pairContainer.keySet.toList.size+")\n"+
                                           "    '"+pairContainer.keySet.mkString(",")+"'"+"\n"+
                                           "pairContainer.keySet.toString():\n"+
                                           "    '"+pairContainer.keySet.toString()+"'\n"+
                                           "pairContainer.size = "+pairContainer.size+"\n"+
                                           "pairContainer.toString() = '"+pairContainer.toString()+"'"
                                           );
          }
          
          return buffer.remove(nextName).get
        }
      }
    }
  }
  
  def samRecordPairIterator(iter : Iterator[SAMRecord], verbose : Boolean = true, testCutoff : Int = -1, isSingleEnd : Boolean = true) : Iterator[(SAMRecord,SAMRecord)] = {
    presetProgressReporters.wrapIterator_readPairs(getSRPairIter(iter), verbose=verbose, cutoff=testCutoff,isSingleEnd=isSingleEnd);
  }
  
  
  
  
  
  
}