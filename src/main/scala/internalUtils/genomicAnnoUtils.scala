package internalUtils

import scala.collection.immutable.TreeMap;
//import scala.collection.immutable.HashSet;
//import scala.collection.immutable.HashMap;
import internalUtils.stdUtils._;
import internalUtils.Reporter._;

import internalUtils.commonSeqUtils._;
import internalUtils.genomicAnnoUtils._;
import internalUtils.GtfTool._;
//import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import fileUtils._;

object genomicAnnoUtils {

  abstract class EfficientWiggleParser {
    def getValueAtPos(c : String, p : Int): String 
    
  }
  
  class SimpleEfficientWiggleParser(infile : String) extends EfficientWiggleParser {
    var currChrom = "";
    var currPos = 0;
    var buffer = "";
    var chromList : Vector[String] = Vector[String]();
    var iter = getLinesSmartUnzip(infile).buffered;
    var headerLine = "";
    var windowSize = 1;
    
    def skipToNextChrom(){
      while( iter.hasNext && (! iter.head.startsWith("fixedStep"))){
        iter.next;
      }
      //if(! iter.hasNext){
        //chromosome not found!
      //}
    }
    def findChromosome(c : String){
        val cix = chromList.indexOf(c)
        if(cix == -1 || chromList.indexOf(currChrom) < cix){
           notice("scanning for chromosome: "+c,"CHROM_SCAN_FWD",-1);
           var keepGoing = true;
           while( iter.hasNext && keepGoing){
            if(iter.head.startsWith("fixedStep")){
              headerLine = iter.next;
              val cc = headerLine.trim().split("\\s+").find(_.startsWith("chrom")).getOrElse({error("fixedStep line found with no chrom tag: \""+iter.head+"\"");"?NO CHROM FOUND?"}).split("=")(1);
              if( ! chromList.contains(cc)){
                notice("    Found chrom: \""+cc+"\" ["+getDateAndTimeString+"]","CHROM_OBS",-1);
                //notice("          chromlist before:["+chromList.mkString(",")+"]","CHROM_OBS_note1",-1);
                chromList = chromList :+ cc;
                //notice("          chromlist after: ["+chromList.mkString(",")+"]","CHROM_OBS_note2",-1);
              }
              if(cc == c){
                keepGoing = false;
              } else {
                notice("        Skipping chrom: \""+cc+"\" (searching for: \""+c+"\") ["+getDateAndTimeString+"]","CHROM_OBS",-1);
              }
            } else {
              iter.next;
            }
          }
        } else {
          notice("Skipping back to top and scanning for chromosome: "+c+" ["+getDateAndTimeString+"]","CHROM_SCAN_BACK",-1);
          iter = getLinesSmartUnzip(infile).buffered;
          while( iter.hasNext && ((! iter.head.startsWith("fixedStep")) || 
               ( iter.head.trim().split("\\s+").find(_.startsWith("chrom")).getOrElse({error("fixedStep line found with no chrom tag: \""+iter.head+"\"");"?NO CHROM FOUND?"}).split("=")(1) != c ))){
            iter.next;
          }
          headerLine = iter.next;
        }
        if(! iter.hasNext){
          error("Failed to find chromosome: \""+c+"\"");
        }
        windowSize = headerLine.trim().split("\\s+").find(_.startsWith("step")).getOrElse("step=1").split("=")(1).toInt
        currChrom = c;
        currPos = 0;
        buffer = iter.next;
    }
    
    def getValueAtPos(c : String, p : Int): String = {
        if(c != currChrom){
          findChromosome(c)
        }
        if(p < currPos){
          error("ERROR: back-access of wig file! Is vcf sorted?");
        }
        while(iter.hasNext && currPos+windowSize < p && (! iter.head.startsWith("fixedStep"))){
          currPos = currPos + windowSize;
          buffer = iter.next
        }
        if( currPos+windowSize < p ){
          warning("Attempt to access beyond chromosome end!","WIGGLE_ACCESS_BEYOND_CHROMEND",-1)
          "0";
        } else {
          buffer;
        }
    }
  }
  
  
  
  /*
   * Useful classes:
   */
  abstract class EfficientGenomeSeqContainer { 
    def currIter : Iterator[String];
    //var remainderIter : Iterator[String];    
    def switchToChrom(chrom : String);
    
    var currChrom = "";
    var bufferStart = 0;
    var buffer : Vector[String] = Vector[String]();
    val blockSize = 1000;
    
    var maxBuffer : Int = buffer.length;
    var chromList : List[String] = List[String]();
    
    def bufferEnd = bufferStart + buffer.length * blockSize;
    
    
    def clearBuffer(){
        residualBuffer = "";
        bufferStart = 0;
        buffer = Vector[String]();
    }
    
    def getSeqForInterval(chrom : String, start : Int, end : Int, truncate : Boolean = false) : String = {
      if(chrom != currChrom) {
        error("ERROR: EfficientGenomeSeqContainer: requested sequence for chromosome other than current chromosome!");
      }
      
      if(start < bufferStart){
        reportln("ERROR: EfficientGenomeSeqContainer: Illegal request for sequence prior to current buffer limits!\n"+
              "   request: "+chrom+":"+start+"-"+end,"note");
        reportBufferStatus;
        error("ERROR: EfficientGenomeSeqContainer: Illegal request for sequence prior to current buffer limits!");
      }
      if(bufferEnd < end){
        extendBufferTo(end);
      }

      val startPos = start - bufferStart;
      val endPos = end - bufferStart;
      val firstBlockIdx = startPos / blockSize
      val lastBlockIdx = (endPos-1) / blockSize
      
      val startOffset = startPos % blockSize;
      val endOffset = ((endPos-1) % blockSize) + 1;
      
      if(firstBlockIdx == lastBlockIdx){
        return buffer(firstBlockIdx).substring(startOffset,endOffset);
      } else if(firstBlockIdx + 1 == lastBlockIdx){
        return buffer(firstBlockIdx).substring(startOffset,blockSize) + buffer(lastBlockIdx).substring(0,endOffset);
      } else {
        return buffer(firstBlockIdx).substring(startOffset,blockSize) + buffer.slice(firstBlockIdx+1,lastBlockIdx).mkString("") + buffer(lastBlockIdx).substring(0,endOffset);
      }
    }
    
    var residualBuffer = "";
    def getNextBlock() : String = {
      var nextBlock = residualBuffer;
      while(currIter.hasNext && nextBlock.length < blockSize){
        nextBlock = nextBlock + currIter.next;
      }
      if(nextBlock.length > blockSize){
        residualBuffer = nextBlock.substring(blockSize);
        return nextBlock.substring(0,blockSize);
      } else {
        residualBuffer = "";
        return nextBlock + repString("N", blockSize - nextBlock.length);;
      }
    }
    
    def shiftBufferTo(chrom : String, start : Int, allowRecycle : Boolean = true){
      if(chrom == currChrom){
        if(bufferEnd < start){
          //quickly skip through a region without compiling a sequence as you go:
          bufferStart = bufferEnd;
          buffer = Vector[String]();
          while(currIter.hasNext && bufferEnd < start){
            bufferStart = bufferEnd;
            buffer = Vector[String](getNextBlock());
          }
          //maxBuffer = math.max(maxBuffer,buffer.length);
        } else if(bufferStart < start){
          val blockIdx = ((start - bufferStart) / blockSize)
          bufferStart = bufferStart + blockIdx * blockSize;
          buffer = buffer.drop(blockIdx);
        } //else do nothing.
      } else {
        switchToChrom(chrom);
        shiftBufferTo(chrom,start);
      }
    }
    
    def extendBufferTo(end : Int) {
      while(currIter.hasNext && bufferEnd < end){
        buffer = buffer :+ getNextBlock();
      }
      if(bufferEnd < end){
        error("ERROR: EfficientGenomeSeqContainer: Attempted to extend buffer beyond chromosome span! Attempted extendBufferTo("+end+") on chrom "+ currChrom + " which appears to end at length: "+bufferEnd);
      }
      maxBuffer = math.max(maxBuffer,buffer.length);
    }
    
    def extendBufferToOrTruncate(end : Int) : Int = {
      while(currIter.hasNext && bufferEnd < end){
        buffer = buffer :+ getNextBlock();
      }
      bufferEnd;
    }
    
    def reportBufferStatus {
      reportln("   [GenomeSeqContainer Status: "+"buf:("+currChrom+":"+bufferStart+"-"+bufferEnd+") "+"n="+buffer.length+", "+"MaxSoFar="+maxBuffer+"]","debug");
    }
    
    def fullStatusDump() : String = {
      return "EfficientGenomeSeqContainer.fullStatusDump(): \n"+
             "currChrom = " +currChrom + "\n"+
             "bufferStart = " +bufferStart + "\n"+
             "bufferEnd = "+bufferEnd+"\n"+
             "buffer.size = "+buffer.size+"\n"+
             "residualBuffer = " +residualBuffer + "\n"+
             "buffer:\n"+
             buffer.mkString("\n");
    }
  }
  
  def buildEfficientGenomeSeqContainer(infiles : Seq[String]) : EfficientGenomeSeqContainer = {
    if(infiles.length == 1){
      new EfficientGenomeSeqContainer_MFA(infiles.head);
    } else {
      new EfficientGenomeSeqContainer_FAList(infiles);
    }
  }
  

  class EfficientGenomeSeqContainer_MFA(infile : String) extends EfficientGenomeSeqContainer {
    //Implementation note: It is important that you never read from remainderIter without first emptying currentIter!
    // Otherwise scala attempts to store the currentIter values.
    // This even remains true if there are no external references to the attached currentIter.
    def parseChromLine(line : String) : String = {
      if(line.head != '>'){
        error("genome fasta file \""+infile+"\" appears mal-formatted! Chromsome line does not start with '>'!:\nOffending line: \""+line+"\"")
      }
      line.tail.split("\\s+",2)(0)
    }
    
    def switchToChrom(chrom : String){
        report("Switching to Chromosome: " + chrom + " ["+getDateAndTimeString+"] ... ","debug");
        clearBuffer();
        switchToChrom(chrom,true);
        report("    found chrom "+chrom+" ["+getDateAndTimeString+"]\n","debug");
    }
    def switchToChrom(chrom : String,circleBack : Boolean){
          while(chrom != currChrom && reader.hasNext){
            reportln("   Skipping chrom \""+currChrom+"\" in genome fasta...","debug");
            skipWhile(reader)(line => line.charAt(0) != '>')
            if(reader.hasNext){
              currChrom = parseChromLine(reader.next);
              if(chrom != currChrom) reportln("   Skipping chrom \""+currChrom+"\" in genome fasta...","debug");
            }
          }
          if(circleBack && currChrom != chrom){
             notice("Returning to start of genome FASTA file. NOTE: for optimal performance, sort the FASTA file so that the chromosomes appear in the same order as in the BAM files.","LOOPED_GENOME_FASTA",-1);
             reader = internalUtils.fileUtils.getLinesSmartUnzip(infile).buffered
             currChrom = parseChromLine(reader.next);
             switchToChrom(chrom,false);
          }
    }
    var reader = internalUtils.fileUtils.getLinesSmartUnzip(infile).buffered;
    if(! reader.hasNext){
      error("genome fasta file \""+infile+"\" appears to be empty!")
    }
    if(reader.head.head != '>'){
      error("genome fasta file \""+infile+"\" appears mal-formatted! First line does not start with '>'!:\nOffending line: \""+reader+"\"")
    }
    currChrom = reader.next().substring(1);
    chromList = List[String](currChrom);
    val currentIter = new BufferedIterator[String]{
      def hasNext = reader.hasNext && reader.head.charAt(0) != '>';
      def next = reader.next.toUpperCase();
      def head = reader.head;
    }
    //var (currentIter,remainderIter) = initialReader.span(line => line.charAt(0) != '>');
    //currentIter = currentIter.map(_.toUpperCase());
    def currIter = currentIter;
  }
  
  class EfficientGenomeSeqContainer_MFA_OLD(infile : String) extends EfficientGenomeSeqContainer {
    def switchToChrom(chrom : String){
        if(chrom != currChrom){
          var iterPair = remainderIter.span(line => line != (">"+chrom));
          if(iterPair._2.hasNext){
            iterPair = iterPair._2.drop(1).span(line => line.charAt(0) != '>');
          } else {
            iterPair = internalUtils.fileUtils.getLinesSmartUnzip(infile).span(line =>  line != (">"+chrom));
            if(iterPair._2.hasNext){
              reportln("Returning to start of genome FASTA file. NOTE: for optimal performance, sort the FASTA file so that the chromosomes appear in the same order as in the BAM files.","note");
              iterPair = iterPair._2.drop(1).span(line => line.charAt(0) != '>');
            } else {
              error("FATAL ERROR: Cannot find chromosome \""+chrom+"\" in genome FASTA file!")
            }
          }
          reportln("Switching to Chromosome: " + chrom,"debug");
          currentIter = iterPair._1.map(_.toUpperCase());
          remainderIter = iterPair._2;
          clearBuffer();
          currChrom = chrom;
        } else {
          reportln("Switching to Chromosome: " + chrom + " (already switched)","debug");
        }
    }
    
    var initialReader = internalUtils.fileUtils.getLinesSmartUnzip(infile);
    currChrom = initialReader.next().substring(1);
    chromList = List[String](currChrom);
    
    var (currentIter,remainderIter) = initialReader.span(line => line.charAt(0) != '>');
    currentIter = currentIter.map(_.toUpperCase());
    def currIter = currentIter;
  }
  
  class EfficientGenomeSeqContainer_MFA2(infile : String) extends EfficientGenomeSeqContainer {
    var reader = internalUtils.fileUtils.getLinesSmartUnzip(infile).buffered;
    currChrom = reader.next().substring(1);
    chromList = List[String](currChrom);
    
    var currentIter = new Iterator[String]{
      def hasNext : Boolean = {
        reader.hasNext && (! reader.head.startsWith(">"));
      }
      def next : String = {
        reader.next.toUpperCase();
      }
    }
    
    def currIter = currentIter;
    
    def switchToChrom(chrom : String){
        switchToChrom(chrom,true)
    }
    
    def switchToChrom(chrom : String, circleBack : Boolean){
        if(chrom != currChrom){
          reportln("Switching to Chromosome: " + chrom,"debug");
          skipWhile(reader)(s => ! s.startsWith(">"+chrom))
          if(! reader.hasNext){
            if(! circleBack){
              error("FATAL ERROR: Cannot find chromosome \""+chrom+"\" in genome FASTA file!");
            }
            reportln("Returning to start of genome FASTA file. NOTE: for optimal performance, sort the FASTA file so that the chromosomes appear in the same order as in the BAM files.","note");
            reader = internalUtils.fileUtils.getLinesSmartUnzip(infile).buffered;
          }
          currChrom = reader.next.substring(1);
          currentIter = new Iterator[String]{
            def hasNext : Boolean = {
              reader.hasNext && (! reader.head.startsWith(">"));
            }
            def next : String = {
              reader.next.toUpperCase();
            }
          }
          clearBuffer();
          switchToChrom(chrom,false);
        } else {
          reportln("Chromosome set: " + chrom + "","debug");
        }
    }
    
    
  }
  
  class EfficientGenomeSeqContainer_FAList(infiles : Seq[String]) extends EfficientGenomeSeqContainer {
    val fastaMap : Map[String,String] = infiles.map(infile => {
      (internalUtils.fileUtils.getLinesSmartUnzip(infile).next().tail,infile)
    }).toMap;
    var currentIter : Iterator[String] = Iterator()
    
    def currIter = currentIter;
    def switchToChrom(chrom : String){
        if(fastaMap.contains(chrom)){
          currentIter = internalUtils.fileUtils.getLinesSmartUnzip(fastaMap(chrom)).drop(1).map(_.toUpperCase());
          clearBuffer();
          currChrom = chrom;
        } else {
          error("FATAL ERROR: Cannot find chromosome \""+chrom+"\" in genome FASTA list!")
        }
    }
  }
  
  
  /*
  class EfficientGenomeSeqContainer(infiles : Seq[String]) {
    var initialReader = internalUtils.fileUtils.getLinesSmartUnzipFromSeq(infiles);
    
    var currChrom = initialReader.next().substring(1);
    
    var bufferStart = 0;
    var buffer : Vector[String] = Vector[String]();
    val blockSize = 1000;
    
    var maxBuffer : Int = buffer.length;
    var chromList : List[String] = List[String](currChrom);
    
    var (currIter,remainderIter) = initialReader.span(line => line.charAt(0) != '>');
    currIter = currIter.map(_.toUpperCase());
    
    def bufferEnd = bufferStart + buffer.length * blockSize;
    //def bufferEnd = bufferStart + buffer.map(_.length).sum;
    
    //def getBlockIdxForPosition(pos : Int) : Int = {
    //  return (pos - bufferStart) / blockSize;
    //}
    
    def getSeqForInterval(chrom : String, start : Int, end : Int, truncate : Boolean = false) : String = {
      if(debugmode) reportln("getSeqForInterval("+chrom+","+start+","+end+")\n"+fullStatusDump(),"debug")
      if(chrom != currChrom) {
        error("ERROR: EfficientGenomeSeqContainer: requested sequence for chromosome other than current chromosome!");
      }
      
      if(start < bufferStart){
        reportln("ERROR: EfficientGenomeSeqContainer: Illegal request for sequence prior to current buffer limits!\n"+
              "   request: "+chrom+":"+start+"-"+end,"note");
        reportBufferStatus;
        error("ERROR: EfficientGenomeSeqContainer: Illegal request for sequence prior to current buffer limits!");
      }
      if(bufferEnd < end){
        extendBufferTo(end);
      }

      val startPos = start - bufferStart;
      val endPos = end - bufferStart;
      val firstBlockIdx = startPos / blockSize
      val lastBlockIdx = (endPos-1) / blockSize
      
      val startOffset = startPos % blockSize;
      val endOffset = ((endPos-1) % blockSize) + 1;
      
      if(firstBlockIdx == lastBlockIdx){
        return buffer(firstBlockIdx).substring(startOffset,endOffset);
      } else if(firstBlockIdx + 1 == lastBlockIdx){
        return buffer(firstBlockIdx).substring(startOffset,blockSize) + buffer(lastBlockIdx).substring(0,endOffset);
      } else {
        return buffer(firstBlockIdx).substring(startOffset,blockSize) + buffer.slice(firstBlockIdx+1,lastBlockIdx).mkString("") + buffer(lastBlockIdx).substring(0,endOffset);
      }
      //buffer.substring(start - bufferStart,end - bufferStart);
    }
    
    var residualBuffer = "";
    def getNextBlock() : String = {
      /*if(residualBuffer.length > blockSize){
        val nextBlock = residualBuffer.substring(0,blockSize);
        residualBuffer = residualBuffer.substring(blockSize);
        return(nextBlock);
      }*/
      var nextBlock = residualBuffer;
      while(currIter.hasNext && nextBlock.length < blockSize){
        nextBlock = nextBlock + currIter.next;
      }
      if(nextBlock.length > blockSize){
        residualBuffer = nextBlock.substring(blockSize);
        return nextBlock.substring(0,blockSize);
      } else {
        residualBuffer = "";
        return nextBlock + repString("N", blockSize - nextBlock.length);;
      }
    }
    
    def shiftBufferTo(chrom : String, start : Int, allowRecycle : Boolean = true){
      if(debugmode) reportln("shiftBufferTo("+chrom+","+start+")\n"+fullStatusDump(),"debug")
      if(chrom == currChrom){
        if(bufferEnd < start){
          //quickly skip through a region without compiling a sequence as you go:
          bufferStart = bufferEnd;
          buffer = Vector[String]();
          while(currIter.hasNext && bufferEnd < start){
            bufferStart = bufferEnd;
            buffer = Vector[String](getNextBlock());
          }
          //maxBuffer = math.max(maxBuffer,buffer.length);
        } else if(bufferStart < start){
          val blockIdx = ((start - bufferStart) / blockSize)
          bufferStart = bufferStart + blockIdx * blockSize;
          buffer = buffer.drop(blockIdx);
        } //else do nothing.
      } else {
        if(remainderIter.hasNext()){
          currChrom = remainderIter.next.substring(1);
          val iterPair = remainderIter.span(line => line.charAt(0) != '>');
          currIter = iterPair._1.map(_.toUpperCase());
          remainderIter = iterPair._2;
        } else {
          if(allowRecycle){
            reportln("Returning to start of genome FASTA file. NOTE: for optimal performance, sort the FASTA file so that the chromosomes appear in the same order as in the BAM files.","note");
            initialReader = internalUtils.fileUtils.getLinesSmartUnzipFromSeq(infiles);
            currChrom = initialReader.next().substring(1);
            val iterPair = remainderIter.span(line => line.charAt(0) != '>');
            currIter = iterPair._1.map(_.toUpperCase());
            remainderIter = iterPair._2;
            
            shiftBufferTo(chrom, start, allowRecycle = false);
          } else {
            error("FATAL ERROR: Cannot find chromosome \""+chrom+"\" in genome FASTA file!")
          }
        }

        bufferStart = 0;
        buffer = Vector[String]();

        
        if(currChrom != chrom){
          reportln("SKIPPING FASTA SEQUENCE FOR CHROMOSOME "+currChrom+" (probable cause: 0 reads found on chromosome, or bam/fasta mis-sorted)!","note");
          //if(chromList.contains(chrom)){
          //  error("FATAL ERROR: BAM and BED files do not have the same chromosome ordering or BAM file is not properly sorted. Encountered BAM read from chromosome \""+chrom+"\", but have already passed this chromosome in the BED file.");
          //}
        } else {
          reportln("Shifting to chromosome: "+currChrom+"!","note");
        }
        chromList = chromList ++ List[String](currChrom);
        
        shiftBufferTo(chrom,start);
      }
    }
    /*var bufferLengthAlertLimit = 10500;
    val bufferLengthAlertUnit = 1000;
    def checkBufferStat {
      maxBuffer = math.max(maxBuffer,buffer.length);
      if(maxBuffer > bufferLengthAlertLimit){
        reportBufferStatus;
        bufferLengthAlertLimit += bufferLengthAlertUnit;
      }
    }*/
    
    def extendBufferTo(end : Int) {
      if(debugmode) reportln("extendBufferTo("+end+")\n"+fullStatusDump(),"debug")
      while(currIter.hasNext && bufferEnd < end){
        buffer = buffer :+ getNextBlock();
      }
      if(bufferEnd < end){
        error("ERROR: EfficientGenomeSeqContainer: Attempted to extend buffer beyond chromosome span! Attempted extendBufferTo("+end+") on chrom "+ currChrom + " which appears to end at length: "+bufferEnd);
      }
      maxBuffer = math.max(maxBuffer,buffer.length);
    }
    
    def extendBufferToOrTruncate(end : Int) : Int = {
      if(debugmode) reportln("extendBufferToOrTruncate("+end+")\n"+fullStatusDump(),"debug")
      while(currIter.hasNext && bufferEnd < end){
        buffer = buffer :+ getNextBlock();
      }
      bufferEnd;
    }
    
    def reportBufferStatus {
      reportln("   [GenomeSeqContainer Status: "+"buf:("+currChrom+":"+bufferStart+"-"+bufferEnd+") "+"n="+buffer.length+", "+"MaxSoFar="+maxBuffer+"]","debug");
    }
    
    var debugmode = false
    def makeVerbose {
      debugmode= true;
    }
    def makeQuiet {
      debugmode= false;
    }
    
    def fullStatusDump() : String = {
      return "EfficientGenomeSeqContainer.fullStatusDump(): \n"+
             "currChrom = " +currChrom + "\n"+
             "bufferStart = " +bufferStart + "\n"+
             "bufferEnd = "+bufferEnd+"\n"+
             "buffer.size = "+buffer.size+"\n"+
             "residualBuffer = " +residualBuffer + "\n"+
             "buffer:\n"+
             buffer.mkString("\n")+"\n"
    }
  }*/
   
  object GenomicArrayOfSets {
    def apply[B](isStranded : Boolean) : GenomicArrayOfSets[B] = {
      if(isStranded){
        //new GenomicArrayOfSets_Stranded[B]();
        new GenomicArrayOfSets_Generic[B](isStranded);
      } else {
        //new GenomicArrayOfSets_Unstranded[B]();
        new GenomicArrayOfSets_Generic[B](isStranded);
      }
    }  
     
    def printGenomicArrayToFile[B](outfile : String, ga : GenomicArrayOfSets[B]){
      val writer : internalUtils.fileUtils.WriterUtil = internalUtils.fileUtils.openWriter(outfile);
      reportln("Starting print of gtf. Memory usage: " + MemoryUtil.memInfo,"note");
      for((iv, stepSet) <- ga.finalizeStepVectors.getSteps){
        //  For reference: case class OutputGtfLine(in_chromName : String, in_featureSource : String, in_featureType : String, in_start : Int, in_end : Int, in_score : String, in_strand : Char, in_attributeMap : Map[String,String], in_gtfFmt_attributeBreak : String, in_stranded : Boolean) extends GtfLine
        val attrMap : Map[String,String] = Map("gene_id" -> stepSet.mkString("+"), "gene_ct" -> stepSet.size.toString);
        val gtfLine : GtfLine = new FlatOutputGtfLine(iv.chromName, FlatOutputGtfLine.DEF_FEATURESOURCE, "exonic_part", iv.start + 1, iv.end, FlatOutputGtfLine.DEF_SCORE, iv.strand, attrMap, FlatOutputGtfLine.DEF_ATTRBREAK, ga.isStranded );
        
        writeGtfLine(gtfLine, writer);
      }
      reportln("Finished print of gtf. Memory usage: " + MemoryUtil.memInfo,"note");
      internalUtils.fileUtils.close(writer);
    }
  }
  
  
  abstract class GenomicArrayOfSets[B] {
    def hasChrom(chromName : String) : Boolean;
    def isStranded : Boolean;
    def isFinalized : Boolean;
    
    def addSpan(span : GenomicInterval, element : B);
    
    def finalizeStepVectors : GenomicArrayOfSets[B];
    def findWhollyContainedSteps(iv : GenomicInterval) : Iterator[(GenomicInterval, Set[B])];
    def findIntersectingSteps(iv : GenomicInterval) : Iterator[(GenomicInterval, Set[B])];
    def findSetAtPosition(chromName : String, pos : Int, strand : Char) : Set[B]
    
    def getChroms : Iterator[(String,Char)];
    def getSteps(chromName : String, strand : Char) : Iterator[(GenomicInterval, Set[B])];
    def getSteps : Iterator[(GenomicInterval, Set[B])];
    
    def addChrom(chromName : String){
      error("ERROR: addChrom not implemented for this class!");
      //do nothing by default
    }
    
    def getValueSet : Set[B];
    
  }
   
  object GenomicSpliceJunctionSet {
    def apply[B](isStranded : Boolean) : GenomicSpliceJunctionSet[B] = {
      return new GenomicSpliceJunctionSet[B](isStranded,  Map[GenomicInterval,B]());
    }
  }
  
  case class GenomicSpliceJunctionSet[B](isStranded : Boolean, sjMap : Map[GenomicInterval,B]) {
    def getSpliceJunction(iv : GenomicInterval) : Option[B] = {
      sjMap.get(iv);
    }
    def addSpliceJunction(iv : GenomicInterval, element : B) : GenomicSpliceJunctionSet[B] = {
      if(iv.strand == '.' && isStranded) error("!");
      return new GenomicSpliceJunctionSet[B](isStranded, sjMap + ((iv.usingStrandedness(isStranded), element)));
    }
  }
  
  /*
   * Implementation:
   */ 
  
  class GenomicArrayOfSets_Generic[B](in_isStranded : Boolean) extends GenomicArrayOfSets[B] {
    private val chromSet : scala.collection.mutable.Set[String] = scala.collection.mutable.Set[String]();
    private val chromMap : scala.collection.mutable.Map[(String,Char),InternalStepVector[B]] = scala.collection.mutable.AnyRefMap[(String,Char),InternalStepVector[B]]();
    private var isFinalizedFlag : Boolean = false;
    private val gsvMap : scala.collection.mutable.AnyRefMap[(String,Char),GenomicStepVector[B]] = scala.collection.mutable.AnyRefMap[(String,Char),GenomicStepVector[B]]();
    private var valueSet : Set[B] = Set[B]();
    
    def getValueSet : Set[B] = valueSet;


    val isStranded : Boolean = in_isStranded;
    
    def hasChrom(chromName : String) : Boolean = if(isStranded){
      chromMap.contains((chromName,'+'));
    } else {
      chromMap.contains((chromName,'.'));
    }
    override def addChrom(chromName : String){
      if(isStranded){
        chromMap((chromName,'+')) = InternalStepVector[B]();
        chromMap((chromName,'-')) = InternalStepVector[B]();
      } else {
        chromMap((chromName,'.')) = InternalStepVector[B]();
      }
    }
    
    def isFinalized : Boolean = isFinalizedFlag;
     
    def finalizeStepVectors : GenomicArrayOfSets[B] = {
      if(isFinalizedFlag) return this;
      //gsvMap = Map[(String,Char),GenomicStepVector[B]]();
      chromMap.foreach((x) => {
        val chromName = x._1._1;
        val strand = x._1._2;
        val isv = x._2;
        gsvMap((chromName, strand)) = (new GenomicStepVector[B](chromName , strand , isv));
      });
      isFinalizedFlag = true;
      gsvMap.repack;
      return this;
      //gsv = new GenomicStepVector[B](chromName : String, strand : Char, isv : InternalStepVector[B])
    }
    
    def addSpan(iv_in : GenomicInterval, element : B){
      val iv = iv_in.usingStrandedness(isStranded);
      chromMap.get((iv.chromName, iv.strand)) match {
        case Some(isv : InternalStepVector[B]) => {
          chromMap((iv.chromName, iv.strand)) = isv.addInterval(iv.start,iv.end,element);
        }
        case None => {
          chromMap((iv.chromName,swapStrand(iv.strand))) = InternalStepVector[B]();
          chromMap((iv.chromName,iv.strand)) = InternalStepVector[B]().addInterval(iv.start,iv.end,element) ;
        }
      }
      valueSet += element;
    }
    
    /*
     * WARNING: the following methods can ONLY be called AFTER the array of sets is finalized!
     */
    def findSetAtPosition(chromName : String, pos : Int, strand : Char) : Set[B] = {
       val strandStranded = if(isStranded) strand else '.';
       gsvMap.get((chromName, strandStranded)) match {
            case Some(gsv : GenomicStepVector[B]) => {
              val out = gsv.findWhollyContainedSteps(pos,pos+1);
              if(out.hasNext){
                return Set[B]();
              } else {
                out.next._2
              }
            }
            case None => return Set[B]();
          }
    }
    
    def findWhollyContainedSteps(iv : GenomicInterval) : Iterator[(GenomicInterval, Set[B])] = {
      gsvMap.get((iv.chromName, iv.strandStranded(isStranded))) match {
            case Some(gsv : GenomicStepVector[B]) => {
              gsv.findWhollyContainedSteps(iv.start,iv.end);
            }
            case None => return (Seq[(GenomicInterval,Set[B])]()).iterator;
          }
    }
    def findIntersectingSteps(iv : GenomicInterval) : Iterator[(GenomicInterval, Set[B])] = {
      gsvMap.get((iv.chromName, iv.strandStranded(isStranded))) match {
            case Some(gsv : GenomicStepVector[B]) => {
              gsv.findIntersectingSteps(iv.start,iv.end);
            }
            case None => return (Seq[(GenomicInterval,Set[B])]()).iterator;
          }
    }
    def getChroms : Iterator[(String,Char)] = {
      return gsvMap.keys.toSeq.sorted.iterator;
    }
    def getSteps(chromName : String, strand : Char) : Iterator[(GenomicInterval, Set[B])] = {
      val strandedStrand = if(isStranded) strand else '.';
      gsvMap.get((chromName, strandedStrand)) match {
            case Some(gsv : GenomicStepVector[B]) => {
              gsv.getSteps
            }
            case None => return (Seq[(GenomicInterval,Set[B])]()).iterator;
          }
    }
    def getSteps : Iterator[(GenomicInterval, Set[B])] = {
      //reportln("Getting steps from: " + gsvMap.size + " chromosomes.","note");
      
      return gsvMap.keys.toSeq.sorted.foldLeft( (Seq[(GenomicInterval,Set[B])]()).iterator )( (soFar,currKey) =>{
        val gsv = gsvMap(currKey);
        soFar ++ gsv.getSteps;
      });
    }
  }
  
  
  class GenomicArrayOfSets_Stranded[B] extends GenomicArrayOfSets[B] {
    private var chromMap : Map[(String,Char),InternalStepVector[B]] = Map[(String,Char),InternalStepVector[B]]();
    private var isFinalizedFlag : Boolean = false;
    private var gsvMap : Map[(String,Char),GenomicStepVector[B]] = null;
    private var valueSet : Set[B] = Set[B]();
    def getValueSet : Set[B] = valueSet;

    def hasChrom(chromName : String) : Boolean = {
      chromMap.contains((chromName,'+'));
    }
    def isStranded : Boolean = true;
    def isFinalized : Boolean = isFinalizedFlag;
    
    override def addChrom(chromName : String){
        //chromMap((chromName,'+')) = InternalStepVector[B]();
        //chromMap((chromName,'-')) = InternalStepVector[B]();
          chromMap = chromMap + (( (chromName,'+'),  InternalStepVector[B]()))
          chromMap = chromMap + (( (chromName,'-'),  InternalStepVector[B]()))
    }
    
    def finalizeStepVectors : GenomicArrayOfSets[B] = {
      if(isFinalizedFlag) return this;
      gsvMap = Map[(String,Char),GenomicStepVector[B]]();
      chromMap.map((x) => {
        val chromName = x._1._1;
        val strand = x._1._2;
        val isv = x._2;
        gsvMap += ((((chromName, strand)) , new GenomicStepVector[B](chromName , strand , isv)));
      });
      isFinalizedFlag = true;
      return this;
      //gsv = new GenomicStepVector[B](chromName : String, strand : Char, isv : InternalStepVector[B])
    }
    
    def addSpan(iv : GenomicInterval, element : B){
      chromMap.get((iv.chromName, iv.strand)) match {
        case Some(isv : InternalStepVector[B]) => {
          chromMap = chromMap + (( (iv.chromName, iv.strand), isv.addInterval(iv.start,iv.end,element)  ));
        }
        case None => {
          chromMap = chromMap + (( (iv.chromName,iv.strand            ),  InternalStepVector[B]().addInterval(iv.start,iv.end,element)  ));
          chromMap = chromMap + (( (iv.chromName,swapStrand(iv.strand)),  InternalStepVector[B]()                                 ));
        }
      }
      valueSet = valueSet + element;
    }
    
    /*
     * WARNING: the following methods can ONLY be called AFTER the array of sets is finalized!
     */
    
    def findWhollyContainedSteps(iv : GenomicInterval) : Iterator[(GenomicInterval, Set[B])] = {
      gsvMap.get((iv.chromName, iv.strand)) match {
            case Some(gsv : GenomicStepVector[B]) => {
              gsv.findWhollyContainedSteps(iv.start,iv.end);
            }
            case None => return (Seq[(GenomicInterval,Set[B])]()).iterator;
          }
    }
    def findIntersectingSteps(iv : GenomicInterval) : Iterator[(GenomicInterval, Set[B])] = {
      gsvMap.get((iv.chromName, iv.strand)) match {
            case Some(gsv : GenomicStepVector[B]) => {
              gsv.findIntersectingSteps(iv.start,iv.end);
            }
            case None => return (Seq[(GenomicInterval,Set[B])]()).iterator;
          }
    }
    def findSetAtPosition(chromName : String, pos : Int, strand : Char) : Set[B] = {
       val strandStranded = if(isStranded) strand else '.';
       gsvMap.get((chromName, strandStranded)) match {
            case Some(gsv : GenomicStepVector[B]) => {
              val out = gsv.findWhollyContainedSteps(pos,pos+1);
              if(out.hasNext){
                return Set[B]();
              } else {
                out.next._2
              }
            }
            case None => return Set[B]();
          }
    }
    
    
    def getChroms : Iterator[(String,Char)] = {
      return gsvMap.keys.toSeq.sorted.iterator;
    }
    def getSteps(chromName : String, strand : Char) : Iterator[(GenomicInterval, Set[B])] = {
      gsvMap.get((chromName, strand)) match {
            case Some(gsv : GenomicStepVector[B]) => {
              gsv.getSteps
            }
            case None => return (Seq[(GenomicInterval,Set[B])]()).iterator;
          }
    }
    def getSteps : Iterator[(GenomicInterval, Set[B])] = {
      //reportln("Getting steps from: " + gsvMap.size + " chromosomes.","note");
      
      return gsvMap.keys.toSeq.sorted.foldLeft( (Seq[(GenomicInterval,Set[B])]()).iterator )( (soFar,currKey) =>{
        val gsv = gsvMap(currKey);
        soFar ++ gsv.getSteps;
      });
    }
  }

  class GenomicArrayOfSets_Unstranded[B] extends GenomicArrayOfSets[B] {
    private val internalArray = new GenomicArrayOfSets_Stranded[B];
    
    private var chromMap : Map[(String,Char),InternalStepVector[B]] = Map[(String,Char),InternalStepVector[B]]();
    private var isFinalizedFlag : Boolean = false;
    private var gsvMap : Map[(String,Char),GenomicStepVector[B]] = null;
    private var valueSet : Set[B] = Set[B]();
    
    def getValueSet : Set[B] = internalArray.getValueSet;

    def hasChrom(chromName : String) : Boolean = {
      chromMap.contains((chromName,'.'));
    }
    def isStranded : Boolean = true;
    def isFinalized : Boolean = isFinalizedFlag;
    
    override def addChrom(chromName : String){
        //chromMap((chromName,'+')) = InternalStepVector[B]();
        //chromMap((chromName,'-')) = InternalStepVector[B]();
          chromMap = chromMap + (( (chromName,'.'),  InternalStepVector[B]()   ))
    }
    
    def finalizeStepVectors : GenomicArrayOfSets[B] = {
      if(isFinalizedFlag) return this;
      gsvMap = Map[(String,Char),GenomicStepVector[B]]();
      chromMap.map((x) => {
        val chromName = x._1._1;
        val strand = x._1._2;
        val isv = x._2;
        gsvMap += ((((chromName, strand)) , new GenomicStepVector[B](chromName , strand , isv)));
      });
      isFinalizedFlag = true;
      return this;
      //gsv = new GenomicStepVector[B](chromName : String, strand : Char, isv : InternalStepVector[B])
    }
    
    def addSpan(iv : GenomicInterval, element : B){
      chromMap.get((iv.chromName, '.')) match {
        case Some(isv : InternalStepVector[B]) => {
          chromMap = chromMap + (( (iv.chromName, '.'), isv.addInterval(iv.start,iv.end,element)  ));
        }
        case None => {
          chromMap = chromMap + (( (iv.chromName,'.'            ),  InternalStepVector[B]().addInterval(iv.start,iv.end,element)  ));
        }
      }
      valueSet = valueSet + element;
    }
    
    /*
     * WARNING: the following methods can ONLY be called AFTER the array of sets is finalized!
     */
    
    def findWhollyContainedSteps(iv : GenomicInterval) : Iterator[(GenomicInterval, Set[B])] = {
      gsvMap.get((iv.chromName, '.')) match {
            case Some(gsv : GenomicStepVector[B]) => {
              gsv.findWhollyContainedSteps(iv.start,iv.end);
            }
            case None => return (Seq[(GenomicInterval,Set[B])]()).iterator;
          }
    } 
    def findIntersectingSteps(iv : GenomicInterval) : Iterator[(GenomicInterval, Set[B])] = {
      gsvMap.get((iv.chromName, '.')) match {
            case Some(gsv : GenomicStepVector[B]) => {
              gsv.findIntersectingSteps(iv.start,iv.end);
            }
            case None => return (Seq[(GenomicInterval,Set[B])]()).iterator;
          }
    }
    def findSetAtPosition(chromName : String, pos : Int, strand : Char) : Set[B] = {
       val strandStranded = if(isStranded) strand else '.';
       gsvMap.get((chromName, strandStranded)) match {
            case Some(gsv : GenomicStepVector[B]) => {
              gsv.findIntersectingSteps(pos,pos+1).foldLeft(Set[B]()){ case (soFar,(iv,currSet)) => {
                soFar ++ currSet
              }}
            }
            case None => return Set[B]();
          }
    }
    
    def getChroms : Iterator[(String,Char)] = {
      return gsvMap.keys.toSeq.sorted.iterator;
    }
    def getSteps(chromName : String, strand : Char) : Iterator[(GenomicInterval, Set[B])] = {
      gsvMap.get((chromName, strand)) match {
            case Some(gsv : GenomicStepVector[B]) => {
              return gsv.getSteps
            }
            case None => return (Seq[(GenomicInterval,Set[B])]()).iterator;
          }
    }
    def getSteps : Iterator[(GenomicInterval, Set[B])] = {
      //reportln("Getting steps from: " + gsvMap.size + " chromosomes.","note");
      
      return gsvMap.keys.toVector.sorted.foldLeft( (Seq[(GenomicInterval,Set[B])]()).iterator )( (soFar,currKey) =>{
        val gsv = gsvMap(currKey);
        soFar ++ gsv.getSteps;
      });
    }
  }
  
  /*
   * Internally-used classes and methods:
   */
  
  class GenomicStepVector[B](chromName : String, strand : Char, isv : InternalStepVector[B]){
    private val ivMap : Map[Int,GenomicInterval] = {
      var out = Map[Int,GenomicInterval]();
      var prev = 0;
      val iter = isv.steps.iterator;
      
      while(iter.hasNext) {
        val next = iter.next._1;
        out += ((next, GenomicInterval(chromName, strand,prev,next)));
        prev = next;
      }
      out;
    }
    
    private def wrapIterator(iter : Iterator[(Int, Set[B])]) : Iterator[(GenomicInterval, Set[B])] = {
      return new Iterator[(GenomicInterval,Set[B])]{
        def hasNext = iter.hasNext;
        def next : (GenomicInterval, Set[B]) = {
          val (nextEnd, nextElement) = iter.next;
          return (ivMap(nextEnd), nextElement);
        }
      }
    }
    
    def findWhollyContainedSteps(start : Int, end : Int) : Iterator[(GenomicInterval, Set[B])] = {
      wrapIterator(isv.findWhollyContainedSteps(start, end));
    }
    def findIntersectingSteps(start : Int, end : Int) : Iterator[(GenomicInterval, Set[B])] = {
      wrapIterator(isv.findIntersectingSteps(start, end));
    }
    def getSteps : Iterator[(GenomicInterval, Set[B])] = {
      wrapIterator(isv.getSteps);
    }

  }
  
  /*
   * ******************************************************************
   * Internal Step Vector:
   */
  
  object InternalStepVector {
    
    def apply[B]() : InternalStepVector[B] = {
      val tm = (new TreeMap[Int,Set[B]]()) + ((Int.MaxValue, Set[B]()));
      return new InternalStepVector[B](tm);
    }
  } 
   
  /* InternalStepVector: a tool for searching intervals across a genome.
   * 
   * (note: InternalStepVector is immutable)
   */
  
  
  
  case class InternalStepVector[B](steps : TreeMap[Int,Set[B]]) {

    /*  
     * Construction and modification methods: 
     */
    def addInterval(start : Int, end : Int, obj : B) : InternalStepVector[B] = {
      val curr : InternalStepVector[B] = this.insertBreakAt(start).insertBreakAt(end);
      
      val newSteps1 : Iterator[(Int,Set[B])] = curr.findIntersectingSteps(start,end);
      
      return newSteps1.foldLeft[InternalStepVector[B]]( curr )( (acc, step) =>{
        acc.addElementToSpanByEndPosition(step._1, obj);
      })
    }
    
    private def addElementToSpanByEndPosition(end : Int, element : B) : InternalStepVector[B] = {
      //Note: This method is unchecked!
      //Add this check if safe behavior is desired:
      if(! steps.contains(end)) error("ERROR! InternalStepVector.addElementToSpanByEndPosition("+end+")! for InternalStepVector. Impossible state?");
      
      return new InternalStepVector(steps + ((end, steps(end) + element )))
    }
    
    def insertBreakAt(pos : Int) : InternalStepVector[B] = {
      if(steps.contains(pos)){
        return this;
      } else {
        val (oldEnd, breakSet) : (Int, Set[B]) = steps.iteratorFrom(pos).next;
        return new InternalStepVector( steps + ((pos, breakSet)));
      }
    }
    
    /*
     * Search methods:
     */
    
    def getSteps : Iterator[(Int, Set[B])] = {
      steps.iterator;
    }
    final val InternalStepVector_NULL_PAIR = (Int.MaxValue, Set[B]());
     
    def findWhollyContainedSteps(start : Int, end : Int) : Iterator[(Int, Set[B])] = {
      val iter = steps.iteratorFrom(start);
      new Iterator[(Int,Set[B])]{
        var next_holder : (Int, Set[B]) = {
          val b = iter.next;
          if(iter.hasNext){
            iter.next;
          } else {
            InternalStepVector_NULL_PAIR
          }
          //If not assigned already, then the iterator must now be empty, and the curr value is irrelevant:
        }
        def hasNext : Boolean = {
          next_holder._1 <= end && iter.hasNext
        }
        def next : (Int,Set[B]) = {
          val buff = next_holder;
          next_holder = iter.next;
          return next_holder;
        }
      }
    }
    
    def findIntersectingSteps(start : Int, end : Int) : Iterator[(Int,Set[B])] = {
      val iter = steps.iteratorFrom(start+1);
      
      new Iterator[(Int,Set[B])]{
        var prev : (Int, Set[B]) = (-1, Set[B]());
        def hasNext : Boolean = {
          return prev._1 < end && iter.hasNext
        }
        def next : (Int,Set[B]) = {
          prev = iter.next;
          return prev;
        }
      }
    }
  }
  
  private def swapStrand(s : Char) : Char = {
    if(s == '+') '-';
    else if(s == '-') '+';
    else '.';
  }
  
  class SimpleGenomicSpanArray {
    val ssaMap = scala.collection.mutable.AnyRefMap[String,SimpleSpanArray]();
    
    def addInterval(chrom : String, start : Int, end : Int){
      if(! ssaMap.contains(chrom)){
        ssaMap(chrom) = new SimpleSpanArray(TreeMap[Int,Boolean]());
      }
      ssaMap(chrom) = ssaMap(chrom).addInterval(start,end);
    }
    
    def checkPosition(chrom : String, p : Int) : Boolean = {
      ssaMap.contains(chrom) &&  ssaMap(chrom).checkPosition(p);
    }
    def intersects(chrom : String, start : Int,end:Int) : Boolean = {
      ssaMap.contains(chrom) &&  ssaMap(chrom).intersects(start,end);
    }
  }
  
  case class SimpleSpanArray(steps : TreeMap[Int,Boolean]) {
    def addInterval(start : Int, end : Int) : SimpleSpanArray = {
      if(steps.contains(start)){
        return SimpleSpanArray( steps + ((end,true)) )
      } else {
        return SimpleSpanArray( steps + ((start,false)) + ((end,true)))
      }
    }
    
    def checkPosition(p : Int) : Boolean = {
      val itr = steps.iteratorFrom(p)
      if(itr.hasNext){
        itr.next._2;
      } else {
        false;
      }
    }
    
    def intersects(start : Int, end : Int) : Boolean = {
      steps.iteratorFrom(start).takeWhile{case (p,v) => { p <= end }}.exists{case (p,v) => { v }};
    }
    
  }
  /*
  case class SimpleStepVector[B](steps : TreeMap[Int,B]) {

    /*  
     * Construction and modification methods: 
     */
    def addInterval(start : Int, end : Int, obj : B) : SimpleStepVector[B] = {
      val curr : InternalStepVector[B] = this.insertBreakAt(start).insertBreakAt(end);
      
      val newSteps1 : Iterator[(Int,Set[B])] = curr.findIntersectingSteps(start,end);
      
      return newSteps1.foldLeft[InternalStepVector[B]]( curr )( (acc, step) =>{
        acc.addElementToSpanByEndPosition(step._1, obj);
      })
    }
    
    private def addElementToSpanByEndPosition(end : Int, element : B) : SimpleStepVector[B] = {
      //Note: This method is unchecked!
      //Add this check if safe behavior is desired:
      if(! steps.contains(end)) error("ERROR! InternalStepVector.addElementToSpanByEndPosition("+end+")! for InternalStepVector. Impossible state?");
      
      return new InternalStepVector(steps + ((end, steps(end) + element )))
    }
    
    def insertBreakAt(pos : Int) : SimpleStepVector[B] = {
      if(steps.contains(pos)){
        return this;
      } else {
        val (oldEnd, breakSet) : (Int, Set[B]) = steps.iteratorFrom(pos).next;
        return new InternalStepVector( steps + ((pos, breakSet)));
      }
    }
    
    /*
     * Search methods:
     */
    
    def getSteps : Iterator[(Int, Set[B])] = {
      steps.iterator;
    }
    final val InternalStepVector_NULL_PAIR = (Int.MaxValue, Set[B]());
     
    def findWhollyContainedSteps(start : Int, end : Int) : Iterator[(Int, Set[B])] = {
      val iter = steps.iteratorFrom(start);
      new Iterator[(Int,Set[B])]{
        var next_holder : (Int, Set[B]) = {
          val b = iter.next;
          if(iter.hasNext){
            iter.next;
          } else {
            InternalStepVector_NULL_PAIR
          }
          //If not assigned already, then the iterator must now be empty, and the curr value is irrelevant:
        }
        def hasNext : Boolean = {
          next_holder._1 <= end && iter.hasNext
        }
        def next : (Int,Set[B]) = {
          val buff = next_holder;
          next_holder = iter.next;
          return next_holder;
        }
      }
    }
    
    def findIntersectingSteps(start : Int, end : Int) : Iterator[(Int,B)] = {
      val iter = steps.iteratorFrom(start+1);
      
      new Iterator[(Int,Set[B])]{
        var prev : (Int, Set[B]) = (-1, Set[B]());
        def hasNext : Boolean = {
          return prev._1 < end && iter.hasNext
        }
        def next : (Int,Set[B]) = {
          prev = iter.next;
          return prev;
        }
      }
    }
  }*/
  
  
}