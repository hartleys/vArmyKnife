package internalTests

import java.util.zip.GZIPInputStream

import java.util.zip.GZIPOutputStream
import java.io.OutputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.io.File
import scala.collection.JavaConversions._
import scala.collection.mutable.HashMap;

import htsjdk._;

import internalUtils.Reporter._;
import internalUtils.stdUtils._;
import internalUtils.fileUtils._;
import internalUtils.commandLineUI._;
import internalUtils.commonSeqUtils._;
import internalUtils.optionHolder._;

import internalUtils.commonSeqUtils._;
import internalUtils.genomicUtils._;
import internalUtils.genomicAnnoUtils._;
import internalUtils.GtfTool._;
import scala.collection.JavaConversions._

import htsjdk.samtools._;

object SimSeqError {


  class GenerateSimulatedError extends CommandLineRunUtil {
     override def priority = 100;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "SimSeqError", 
          quickSynopsis = "", 
          synopsis = "", 
          description = " " + ALPHA_WARNING,   
          argList = 
                    /*new UnaryArgument( name = "countClinVar",
                                         arg = List("--countClinVar"), // name of value
                                         argDesc = "If this flag is used..."+
                                                   "" // description
                                       ) ::       
                    new BinaryOptionArgument[String](
                                         name = "txToGeneFile", 
                                         arg = List("--txToGeneFile"), 
                                         valueName = "txToGene.txt",  
                                         argDesc =  ""
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "gtfFile", 
                                         arg = List("--gtfFile"), 
                                         valueName = "anno.gtf.gz",  
                                         argDesc =  ""
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "chromList", 
                                         arg = List("--chromList"), 
                                         valueName = "chromList.txt",  
                                         argDesc =  ""
                                        ) ::   
                    new FinalArgument[String](
                                         name = "invcf",
                                         valueName = "variants.vcf",
                                         argDesc = "infput VCF file" // description
                                        ) ::
                    new FinalArgument[String](
                                         name = "domainFile",
                                         valueName = "domainFile.txt",
                                         argDesc = "infput domain info file." // description
                                        ) ::*/
                    new UnaryArgument(   name = "singleEnded", 
                                         arg = List("--singleEnded","-e"), // name of value
                                         argDesc = "Flag to indicate that reads are single end." // description
                                       ) ::
                    new BinaryOptionArgument[Double](
                                         name = "simpleSwapRate", 
                                         arg = List("--simpleSwapRate"), 
                                         valueName = "0.00001",  
                                         argDesc =  ""
                                        ) ::
                    new BinaryOptionArgument[List[Double]](
                                         name = "truncationRate", 
                                         arg = List("--truncationRates"), 
                                         valueName = "0.01,0.025",  
                                         argDesc =  ""
                                        ) ::
                    new BinaryOptionArgument[List[Int]](
                                         name = "truncationLength", 
                                         arg = List("--truncationLength"), 
                                         valueName = "5,10",
                                         argDesc =  ""
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "fastqOutput", 
                                         arg = List("--fastqOutput"), 
                                         valueName = "fileprefix",
                                         argDesc =  ""
                                        ) ::
                    new FinalArgument[String](
                                         name = "inbam",
                                         valueName = "input.bam",
                                         argDesc = "input bam file." // description
                                        ) ::
                    new FinalArgument[String](
                                         name = "outbam",
                                         valueName = "outbam",
                                         argDesc = "The output bam file."// description
                                        ) ::
                    internalUtils.commandLineUI.CLUI_UNIVERSAL_ARGS );
          
    def run(args : Array[String]) {
     val out = parser.parseArguments(args.toList.tail);
     if(out){
       SimSeqError.simError(
                   inbam  = parser.get[String]("inbam"),
                   outbam = parser.get[String]("outbam"),
                   fastqOutput = parser.get[Option[String]]("fastqOutput"),
                   simpleSwapRate = parser.get[Option[Double]]("simpleSwapRate"),
                   isSingleEnd = parser.get[Boolean]("singleEnded"),
                   truncationRate = parser.get[Option[List[Double]]]("truncationRate"),
                   truncationLength = parser.get[Option[List[Int]]]("truncationLength")
                   )
     }
    }
  }
  var rand = new scala.util.Random();
  def simError(  inbam : String, outbam : String, fastqOutput : Option[String],
                 //sim params:
                 simpleSwapRate : Option[Double],
                 truncationRate : Option[List[Double]],
                 truncationLength : Option[List[Int]],
                 
                 //file metadata:
                 isSingleEnd : Boolean = true,
                 testRunLineCt : Int = 10000,
                 stopAfterNReads : Option[Int] = None,
                 testRun : Boolean = false,
                 maxReadLength : Option[Int] = None,
                 sortPairsByFirstPosition : Boolean = true,
                 unsorted : Boolean = true,
                 maxPhredScore : Int = 41
               ){
    //val readerFactory = SamReaderFactory.makeDefault().enable(SamReaderFactory.Option.INCLUDE_SOURCE_IN_RECORDS).validationStringency(ValidationStringency.SILENT);
    val readerFactory = SamReaderFactory.makeDefault();
    
    val (samIter,samHeader,samAttributes) = internalUtils.SamTool.getPairedEndReader(inbam,isSingleEnd = isSingleEnd, 
                                                      sortPairsByFirstPosition=sortPairsByFirstPosition,
                                                      unsorted=unsorted,
                                                      maxPhredScore=maxPhredScore,
                                                      stopAfterNReads=stopAfterNReads,
                                                      testRunLineCt=testRunLineCt,
                                                      testRun=testRun,
                                                      maxReadLength=maxReadLength);
    
    
    val fqWriter : internalUtils.SamTool.SamToFastqConverter = fastqOutput match {
      case Some(fqo) => {
        if(isSingleEnd){
          internalUtils.SamTool.SingleEndSamToFastq(fqo, ext=".fq.gz");
        } else {
          internalUtils.SamTool.PairedEndSamToFastq(fqo, ext=".fq.gz");
        }
      }
      case None => {
        internalUtils.SamTool.NullSamToFastqConverter()
      }
    }
    
    val newHeader = samHeader;
    
    val rawpgid = "QoRTs.SimSeqError"
    val pggen = new SAMFileHeader.PgIdGenerator(samHeader);
    val pgid = pggen.getNonCollidingId(rawpgid);
    val pgRecord = new SAMProgramRecord(pgid);
    pgRecord.setCommandLine("java -jar QoRTs.jar "+internalUtils.optionHolder.TOPLEVEL_COMMAND_LINE_ARGS.mkString(" "));
    pgRecord.setProgramName(rawpgid);
    pgRecord.setProgramVersion(runner.runner.UTIL_COMPLETE_VERSION)
    newHeader.addProgramRecord(pgRecord);
    
    val writer = (new SAMFileWriterFactory()).makeBAMWriter(newHeader,false,new File(outbam));
    
    val mutators : Seq[ReadPairMutator] = Seq[ReadPairMutator]() ++ 
        (if(simpleSwapRate.isDefined) {Seq(new RandomSequenceErrorMutator(simpleSwapRate.get))} else {Seq[ReadPairMutator]()}) ++
        (if(truncationRate.isDefined) {
                                         val trate = truncationRate.get;
                                         val tlen = truncationLength.get;
                                         if(trate.length != 2) error("INPUT ERROR: truncationRate must have length 2 (comma delimited, no spaces)");
                                         if(tlen.length != 2) error("INPUT ERROR: truncationLength must have length 2 (comma delimited, no spaces)");
                                         val (tr1,tr2) = (trate(0),trate(1));
                                         val (tl1,tl2) = (tlen(0),tlen(1));
                                         Seq[ReadPairMutator](new TruncateTailMutator(truncateR1 = tl1, truncateRateR1 =tr1, truncateR2 = tl2, truncateRateR2 = tr2));
                                      } else { Seq[ReadPairMutator]() })
        
    
    samIter.zipWithIndex.foreach{case ((r1,r2),i) => {
      val (r1m,r2m) = mutators.foldLeft( (r1,r2) ){ case ((r1,r2),mutator) => {
        mutator.mutate(r1,r2,i);
      }}
      
      writer.addAlignment(r1m);
      writer.addAlignment(r2m);
      
      fqWriter.writeReads(r1m,r2m,"");
      
    }}
    fqWriter.close();
    writer.close();
  }
  
  abstract class ReadPairMutator {
    def mutate(r1 : SAMRecord, r2 : SAMRecord, i : Int) : (SAMRecord,SAMRecord);
  }
  
  val changedBaseByteArray = Array.ofDim[Byte](255,3);
  changedBaseByteArray('A'.toInt) = Array[Byte]('T'.toByte,'G'.toByte,'C'.toByte);
  changedBaseByteArray('T'.toInt) = Array[Byte]('A'.toByte,'G'.toByte,'C'.toByte);
  changedBaseByteArray('C'.toInt) = Array[Byte]('T'.toByte,'G'.toByte,'A'.toByte);
  changedBaseByteArray('G'.toInt) = Array[Byte]('T'.toByte,'A'.toByte,'C'.toByte);
  changedBaseByteArray('N'.toInt) = Array[Byte]('N'.toByte,'N'.toByte,'N'.toByte);
  
  class RandomSequenceErrorMutator(simpleSwapRate : Double) extends ReadPairMutator {
    def mutate(r1 : SAMRecord, r2 : SAMRecord, i : Int) : (SAMRecord,SAMRecord) = {
      (mutateRead(r1),mutateRead(r2));
    }
    def mutateRead(r : SAMRecord) : SAMRecord = {
      r.setReadBases(r.getReadBases().toVector.map{x => {
        if(rand.nextDouble() < simpleSwapRate){
          changedBaseByteArray(x.toInt)(rand.nextInt(3));
        } else {
          x;
        }
      }}.toArray)
      r;
    }
  }
  
  class TruncateTailMutator(truncateR1 : Int, truncateRateR1 : Double, truncateR2 : Int, truncateRateR2 : Double) extends ReadPairMutator {
    def mutate(r1 : SAMRecord, r2 : SAMRecord, i : Int) : (SAMRecord,SAMRecord) = {
      (mutateRead(r1,truncateR1,truncateRateR1),mutateRead(r2,truncateR2,truncateRateR2));
    }
    def mutateRead(r : SAMRecord, t : Int, tr : Double) : SAMRecord = {
      if(tr > 0 && rand.nextDouble() > tr){
          val idx = if(r.getReadNegativeStrandFlag()){
            Range(r.getReadBases().length-1,-1,-1);
          } else {
            Range(0,r.getReadBases().length);
          }
          
          r.setReadBases(r.getReadBases().toVector.zip(idx).map{ case (x,i) => {
            if(i >= t) 'N'.toByte;
            else x;
          }}.toArray)
          r;
      } else {
        r;
      }
    }
  }
  
}


























