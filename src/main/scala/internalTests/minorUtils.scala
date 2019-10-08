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

import internalUtils.VcfTool._;

import internalUtils.VcfTool;

import htsjdk.variant._;
import htsjdk.variant.variantcontext._;
import htsjdk.variant.vcf._;

import internalUtils.genomicUtils._;
import internalUtils.commonSeqUtils._;



object minorUtils {
  
  
  

  class CmdMemMergeVCF extends CommandLineRunUtil {
     override def priority = 100;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "MergeManyVcf", 
          quickSynopsis = "", 
          synopsis = "", 
          description = " " + ALPHA_WARNING,   
          argList = 
                    new BinaryArgument[List[String]](name = "sumInfoFields",
                                           arg = List("--sumInfoFields"),  
                                           valueName = "tag1,tag2,tag3,...", 
                                           argDesc = "", 
                                           defaultValue = Some(List())) ::
                    new BinaryArgument[List[String]](name = "splitInfoFields",
                                           arg = List("--splitInfoFields"),  
                                           valueName = "tag1,tag2,tag3,...", 
                                           argDesc = "", 
                                           defaultValue = Some(List())) ::
                    new BinaryArgument[List[String]](name = "firstInfoFields",
                                           arg = List("--firstInfoFields"),  
                                           valueName = "tag1,tag2,tag3,...", 
                                           argDesc = "", 
                                           defaultValue = Some(List())) ::
                    new BinaryArgument[List[String]](name = "gtInfoFields",
                                           arg = List("--gtInfoFields"),  
                                           valueName = "tag1,tag2,tag3,...", 
                                           argDesc = "", 
                                           defaultValue = Some(List())) ::
                    new BinaryOptionArgument[String](
                                         name = "chromList", 
                                         arg = List("--chromList"), 
                                         valueName = "chromList.txt",  
                                         argDesc =  ""
                                        ) ::   
                    new BinaryOptionArgument[String](
                                         name = "genomeFA", 
                                         arg = List("--genomeFA"), 
                                         valueName = "genome.fa",  
                                         argDesc =  ""
                                        ) ::   
                    new BinaryArgument[Int](name = "leftAlignAndTrimWindow",
                                           arg = List("--leftAlignAndTrimWindow"),  
                                           valueName = "200", 
                                           argDesc = "", 
                                           defaultValue = Some(200)) ::
                    new BinaryOptionArgument[List[String]](
                                         name = "infixList", 
                                         arg = List("--infixList"), 
                                         valueName = "infix1,infix2,...",  
                                         argDesc =  ""
                                        ) ::  
                    new BinaryOptionArgument[String](
                                         name = "infixFile", 
                                         arg = List("--infixFile"), 
                                         valueName = "infixlist.txt",  
                                         argDesc =  ""
                                        ) ::  
                    new FinalArgument[String](
                                         name = "infilePrefix",
                                         valueName = "infilePrefix",
                                         argDesc = "Input file prefix" // description
                                        ) ::
                    new FinalArgument[String](
                                         name = "infileSuffix",
                                         valueName = "infileSuffix",
                                         argDesc = "Input file suffix" // description
                                        ) :: 
                    new FinalArgument[String](
                                         name = "outvcf",
                                         valueName = "outvcf",
                                         argDesc = "The output vcf file."// description
                                        ) ::
                    internalUtils.commandLineUI.CLUI_UNIVERSAL_ARGS );
          
    def run(args : Array[String]) {
     val out = parser.parseArguments(args.toList.tail);
     if(out){
       
      memMergeVcf(
           infilePrefix=parser.get[String]("infilePrefix"),
           infileSuffix=parser.get[String]("infileSuffix"),
           outfile     =parser.get[String]("outvcf"),
           infixFile=parser.get[Option[String]]("infixFile"),
           infixList=parser.get[Option[List[String]]]("infixList"),
           leftAlignAndTrimWindow=parser.get[Int]("leftAlignAndTrimWindow"),
           genomeFA=parser.get[Option[String]]("genomeFA"),
           sumInfoFields=parser.get[List[String]]("sumInfoFields"),
           splitInfoFields=parser.get[List[String]]("splitInfoFields"),
           firstInfoFields=parser.get[List[String]]("firstInfoFields"),
           gtInfoFields=parser.get[List[String]]("gtInfoFields")
           )
           
           
       /*
       
        internalUtils.VcfTool.SVcfLine.memMergeVcfFiles()
        memMergeVcfFiles(infiles : List[String], sampids : List[String],  
                         sumInfoFields : Seq[String] = Seq[String](),     //not implemented
                         splitInfoFields : Seq[String] = Seq[String](),   //not implemented
                         firstInfoFields : Seq[String] = Seq[String](),   //not implemented
                         gtInfoFields : Seq[String] = Seq[String](),      //not implemented
                         genomeFA : String, latWindow : Int = 200, idxTagString : String = "IDX.",
                         leftAlignAndTrim : Boolean = true, splitMultiAllelics : Boolean = true) : (SVcfHeader, Iterator[SVcfVariantLine])
        */
     }   
    }
    
  def memMergeVcf(infilePrefix : String, infileSuffix : String, outfile : String,
                infixFile : Option[String], infixList : Option[List[String]],
                leftAlignAndTrimWindow : Int,
                genomeFA : Option[String],
                sumInfoFields : List[String],
                splitInfoFields : List[String],
                firstInfoFields  : List[String],
                 gtInfoFields  : List[String],
                 dropGenotypes : Boolean = false){
    
    val infixes : List[String] = (infixFile.toList.flatMap{ff => {
      getLines(ff).toList
    }}) ++ infixList.toList.flatten
    
    if(infixes.length == 0){
      error("Require at least 1 infix via --infixList or --infixFile!");
    }
    val infiles : List[String] = infixes.map{fx => infilePrefix + fx + infileSuffix}
    
    val (vcfHeader,vcIter) : (SVcfHeader, Iterator[SVcfVariantLine]) = 
               internalUtils.VcfTool.SVcfLine.memMergeVcfFiles(
                         infiles = infiles, 
                         sampids = infixes,  
                         sumInfoFields = sumInfoFields,     //not implemented
                         splitInfoFields = splitInfoFields,   //not implemented
                         firstInfoFields =firstInfoFields,   //not implemented
                         gtInfoFields =gtInfoFields,      //not implemented
                         genomeFA = genomeFA.get, 
                         latWindow =leftAlignAndTrimWindow, 
                         idxTagString = "IDX."
               )
    
    
    val writer = openWriterSmart(outfile,true);
    vcfHeader.getVcfLines.foreach{line => {
      writer.write(line+"\n");
    }}
    if(dropGenotypes){
      vcIter.foreach{ line => {
        writer.write(line.getVcfStringNoGenotypes+"\n");
      }}
    } else {
      vcIter.foreach{ line => {
        writer.write(line.getVcfString+"\n");
      }}
    }
    writer.close();
   //
   //writeToFile(outfile : String, vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader, dropGenotypes : Boolean =false)
  }
    
    
  }

  
}




















































