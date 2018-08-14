package internalTests

import htsjdk.variant._;
import htsjdk.variant.variantcontext._;
import htsjdk.variant.vcf._;
import java.io.File;
//import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import java.io._;
import internalUtils.commandLineUI._;
import internalUtils.Reporter._;
import internalUtils.stdUtils._;
import internalUtils.fileUtils._;

import java.io.FileOutputStream._;

object SplitVcfByChrom {

  class SplitVcfByChrom extends CommandLineRunUtil {
     override def priority = 100;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "SplitVcfByChrom", 
          quickSynopsis = "", 
          synopsis = "", 
          description = "Converts VCF file to Plink files.",   
          argList = 
                    
                    new UnaryArgument( name = "version",
                                         arg = List("--version"), // name of value
                                         argDesc = "Flag. If raised, print version." // description
                                       ) ::
                    new UnaryArgument( name = "allowUnnanotatedContigs",
                                         arg = List("--allowUnnanotatedContigs"), // name of value
                                        argDesc = "Flag. If raised, allow unannotated contigs." // description
                                       ) ::
                    new FinalArgument[String](
                                         name = "infile",
                                         valueName = "infile.vcf",
                                         argDesc = "infile" // description
                                        ) ::
                    new FinalArgument[String](
                                         name = "outfileprefix",
                                         valueName = "outfileprefix",
                                         argDesc = "The output file prefix."// description
                                        ) ::
                    internalUtils.commandLineUI.CLUI_UNIVERSAL_ARGS );
      
     def run(args : Array[String]) { 
       val out = parser.parseArguments(args.toList.tail);
      
       if(out){
         splitVcf(
             parser.get[String]("infile"),
             parser.get[String]("outfileprefix"),
             parser.get[Boolean]("allowUnnanotatedContigs")
             );
       }
     }
   }
  
  def splitVcf(infile : String, outfileprefix : String, allowUnnanotatedContigs : Boolean){
    val vcfReader = new VCFFileReader(new File(infile),false);
    val vcfHeader = vcfReader.getFileHeader();
    
    val contigList = vcfHeader.getContigLines().asScala.map(x => x.getID()).toVector;
    
    val writerMap  = scala.collection.mutable.AnyRefMap.fromZip[String,BufferedWriter](contigList.toArray,contigList.map(c => (new BufferedWriter(new FileWriter(outfileprefix + c + ".vcf")))).toArray);
    
    writerMap.foreach{case (c,w) => {
      w.write(vcfHeader.toString());
    }}
    
    for( line <- vcfReader.asScala){
      val writer = writerMap.get(line.getContig()) match {
        case Some(writer) => {
          writer
        }
        case None => {
          if(! allowUnnanotatedContigs){
            error("ERROR: UNRECOGNIZED CONTIG: "+line.getContig());
          }
          val writer = new BufferedWriter(new FileWriter(outfileprefix + line.getContig()));
          writerMap.put(line.getContig(),writer);
          writer
        }
      }
      writer.write(line.toString()+"\n");
    }
    
    writerMap.foreach{case (c,w) => {
      w.close();
    }}
    
  }
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
}














