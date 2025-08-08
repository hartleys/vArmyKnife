package internalTests

import htsjdk.variant._;
import htsjdk.variant.variantcontext._;
import htsjdk.variant.vcf._;
import java.io.File;
//import scala.collection.JavaConversions._
import java.io._;
import internalUtils.commandLineUI._;
import internalUtils.Reporter._;

//import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import internalUtils.optionHolder._;
import internalUtils.Reporter._;
import internalUtils.stdUtils._;
import internalUtils.genomicAnnoUtils._;
import internalUtils.GtfTool._;
import internalUtils.commandLineUI._;
import internalUtils.fileUtils._;
import internalUtils.TXUtil._;
import internalUtils.TXUtil

import htsjdk.variant._;
import htsjdk.variant.variantcontext._;
import htsjdk.variant.vcf._;

import internalUtils.genomicUtils._;
import internalUtils.commonSeqUtils._;


object VcfSimpleMerge {
  
  class vcfSimpleMerger extends CommandLineRunUtil {
     override def priority = 100;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "simpleMergeVcfs", 
          quickSynopsis = "", 
          synopsis = "", 
          description = "Test utility.",   
          argList = 
                    new BinaryOptionArgument[List[String]](
                                         name = "infoList", 
                                         arg = List("--infoList"), 
                                         valueName = "tag1,tag2,...",  
                                         argDesc =  "..."
                                        ) ::
                    new FinalArgument[String](
                                         name = "vcfMaster",
                                         valueName = "vcffile.vcf",
                                         argDesc = "vcffile"
                                        ) ::
                    new FinalArgument[String](
                                         name = "vcfAdd",
                                         valueName = "vcffile.vcf",
                                         argDesc = "vcffile"
                                        ) ::
                    new FinalArgument[String](
                                         name = "output",
                                         valueName = "vcffile.vcf",
                                         argDesc = "The output file."
                                        ) ::
                    internalUtils.commandLineUI.CLUI_UNIVERSAL_ARGS );
          
     def run(args : Array[String]) {
       val out = parser.parseArguments(args.toList.tail);
       if(out){ 
         runMerge( 
              parser.get[String]("vcfMaster"),
              parser.get[String]("vcfAdd"),
              parser.get[String]("output"),
              parser.get[Option[List[String]]]("infoList")
             )
       }
     }
   }
  
  def runMerge(vcf1 : String, vcf2 : String, outfile : String, infoList : Option[List[String]]){
    val readerM = getLinesSmartUnzip(vcf1);
    val readerA = getLinesSmartUnzip(vcf2);
    
    
    
  }
  
}




























