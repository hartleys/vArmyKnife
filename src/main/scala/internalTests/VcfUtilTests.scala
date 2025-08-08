package internalTests

import htsjdk.variant._;
import htsjdk.variant.variantcontext._;
import htsjdk.variant.vcf._;
import java.io.File;
//import scala.collection.JavaConversions._
import java.io._;
import internalUtils.commandLineUI._;
import internalUtils.Reporter._;
import scala.collection.JavaConverters._

import internalUtils.VcfTool._;

object AnnoVarToVcf {
  
  class runVcfUtilTests extends CommandLineRunUtil {
     override def priority = 100;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "VcfUtilTests", 
          quickSynopsis = "", 
          synopsis = "", 
          description = "Test utility.",   
          argList = 

                    new BinaryArgument[String](name = "subCommand",
                                            arg = List("--cmd"),  
                                            valueName = "cmd", 
                                            argDesc = "The sub-command. This utility serves many separate functions."+
                                                      ""+
                                                      ""+
                                                      "", 
                                            defaultValue = Some("stdtests")
                                       ) :: 
                    new UnaryArgument( name = "version",
                                         arg = List("--version"), // name of value
                                         argDesc = "Flag. If raised, print version." // description
                                       ) ::
                    new FinalArgument[String](
                                         name = "infile",
                                         valueName = "infile.vcf",
                                         argDesc = "infile" // description
                                        ) ::
                    new FinalArgument[String](
                                         name = "outfile",
                                         valueName = "outfile",
                                         argDesc = "The output file."// description
                                        ) ::
                    internalUtils.commandLineUI.CLUI_UNIVERSAL_ARGS );
     
     val subCommandList = List[String]("stdtests","extractClinVarLOF");
     
     def run(args : Array[String]) {
       val out = parser.parseArguments(args.toList.tail);
      
     }
   }
  /*
   def annoVarDbFileToRawVcf(indata : Iterator[String]) : (Iterator[SVcfVariantLine],SVcfHeader) = {
     //#Chr    Start   End     Ref     Alt
     val titleLine = indata.next.split("\t");
     SVcfHeader( infoLines = titleLine.drop(5).toSeq.map{t => {
                    new SVcfCompoundHeaderLine("INFO",t,Number=".",Type="String",desc=t+" field from ANNOVAR db file")
                 }},
                         formatLines = Seq(), 
                         otherHeaderLines = Seq() ,
                         walkLines = Seq(),
                         titleLine = SVcfTitleLine(sampleList = Seq()))
     (indata.map{_.split("\t")}.map{ cells => {
       
     }},
   }*/
  
  
  
  
}