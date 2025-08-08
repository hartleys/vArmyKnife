package internalTests

import htsjdk.variant._;
import htsjdk.variant.variantcontext._;
import htsjdk.variant.vcf._;
import java.io.File;
import scala.collection.JavaConversions._
import java.io._;
import internalUtils.commandLineUI._;
import internalUtils.Reporter._;
import internalUtils.stdUtils._;
import internalUtils.fileUtils._;

import java.io.FileOutputStream._;

object TrimNByQual {
  class SplitVcfByChrom extends CommandLineRunUtil {
     override def priority = 100;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "SplitVcfByChrom", 
          quickSynopsis = "", 
          synopsis = "", 
          description = "Converts VCF file to Plink files.",   
          argList = 
                    
                    new UnaryArgument(   name = "singleEnded", 
                                         arg = List("--singleEnded","-e"), // name of value
                                         argDesc = "Flag to indicate that reads are single end." // description
                                       ) ::
                    new BinaryOptionArgument[String](
                                         name = "illuminaClip", 
                                         arg = List("--ILLUMINACLIP"), 
                                         valueName = "<fastaWithAdapters>:A:B:C[:D:E]",
                                         argDesc =  "FA file with the illumina primers in it"+
                                                    "Parameters: A = seed mismatches, B = palindrome clip threshold, C = simple clip threshold."+
                                                    "Optionally: D = minAdapterLength, and E = keepBothReads."+
                                                    ""+
                                                    "(CURRENTLY UNIMPLEMENTED!)"
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "slidingWindow", 
                                         arg = List("--SLIDINGWINDOW"), 
                                         valueName = "windowsize:requirequality",
                                         argDesc =  "sliding window trimming"+
                                                    ""+
                                                    ""+
                                                    ""+
                                                    "(CURRENTLY UNIMPLEMENTED!)"
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "maxInfo", 
                                         arg = List("--MAXINFO"), 
                                         valueName = "2,30,10",
                                         argDesc =  "Parameters to use for the adaptor trimming. Requires option --primerFa"+
                                                    ""+
                                                    ""+
                                                    ""+
                                                    "(CURRENTLY UNIMPLEMENTED!)"
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
         trimNbyQual(
             parser.get[String]("infile"),
             parser.get[String]("outfileprefix"),
             parser.get[Boolean]("allowUnnanotatedContigs")
             );
       }
     }
   }
  
  def trimNbyQual(infile : String, outfileprefix : String, allowUnnanotatedContigs : Boolean){
    
  }
}










