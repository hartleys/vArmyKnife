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
import internalTests.SVcfWalkerUtils._;

import internalTests.SVcfTagFunctions._;

//import com.timgroup.iterata.ParIterator.Implicits._;

object SVcfWalkerMain {
   
  /*
                        ADDME?: synon: Seq[String]
                         hidden : Boolean = false,
                         deprecated : Boolean = false,
                         alpha : Boolean = false,
                         category : String = "General",
                         withinCatPriority : Int = 1000,
                         ord : Ordering[String] = ParamStrSetDefaultOrdering
   */
      val DEFAULT_VCF_CODES = VCFAnnoCodes();
  
  class WalkVcf extends CommandLineRunUtil {
     override def priority = 1;
     
     val vcfFilterManualTitle = "VARIANT-LEVEL BOOLEAN EXPRESSIONS"
     val gtFilterManualTitle = "GENOTYPE-LEVEL BOOLEAN EXPRESSIONS"
     
     val vcfFilterManualDesc = ""+
                              ""+
                              ""+
                              ""+
                              ""
     val gtFilterManualDesc = ""+
                              ""+
                              ""+
                              ""+
                              "";


                              
    /* val altCommandDocText : Seq[String] = Seq("Secondary Commands:",
                            "In addition to the standard command which parses a VCF or variant table, vArmyKnife includes a few ancillary tools "+
                            "which perform other tasks. ",
                            "These tools can be invoked with the command:",
                            "    varmyknife --CMD commandName [options]") ++ 
                               runner.runner.sortedCommandList.filter{ case (arg,cmdMk) => { ! cmdMk().isAlpha }}.flatMap{ case (arg,cmdMaker) => {
                                  val parser = cmdMaker().parser;
                                  Seq[String](arg, parser.getDescription)
                                  //sb.append("### ["+arg+"]("+arg+".html)\n\n");
                                  //sb.append("> "+(parser.getDescription).replaceAll("_","\\\\_") + "\n\n");
                                }}*/
     val altCommandDocText : Seq[String] = Seq("SECONDARY COMMANDS",
                            "In addition to the standard command which parses a VCF or variant table, vArmyKnife includes a few ancillary tools "+
                            "which perform other tasks. ",
                            "These tools can be invoked with the command:",
                            "    varmyknife --CMD commandName [options]",
                            "For more information, use the command:" ,
                            "    varmyknife --help CMD",
                            "For a listing of all secondary commands, use the command: ",
                            "    varmyknife --help secondaryCommands") 
                            
     val altCommandDocMd : Seq[String] = Seq("# SECONDARY COMMANDS\n",
                            "In addition to the standard command which parses a VCF or variant table, vArmyKnife includes a few ancillary tools "+
                            "which perform other tasks. ",
                            "These tools can be invoked with the command:\n",
                            "    varmyknife --CMD commandName [options]\n",
                            "For more information see the [secondary command page](docs/secondaryCommands.html), or use the command:\n" ,
                            "    varmyknife --help CMD\n",
                            "For a listing of all secondary commands, use the command: \n",
                            "    varmyknife --help secondaryCommands\n") 
     val manualExtras = 
                        internalTests.SVcfMapFunctions.MAPFUNCTIONS_getBlockStringManual +"\n"+
                        internalTests.SVcfTagFunctions.TAGFUNCTIONS_getBlockStringManual+"\n"+
                        internalTests.SVcfTagFunctions.FMTFUNCTIONS_getBlockStringManual+"\n"+
                        internalTests.SVcfTagFunctions.TALLYFUNCTIONS_getBlockStringManual+"\n"+
                        sVcfFilterLogicParser.getManualString(Some(vcfFilterManualTitle),Some(vcfFilterManualDesc)) +"\n"+
                        sGenotypeFilterLogicParser.getManualString(Some(gtFilterManualTitle),Some(gtFilterManualDesc)) +"\n"+
                        altCommandDocText.map{ acm => {
                          wrapLinesWithIndent(acm, internalUtils.commandLineUI.MD_CONSOLE_LINE_WIDTH, "        ", false) 
                        }}.mkString("\n");
                        
     val markdownManualExtras =  
                        internalTests.SVcfMapFunctions.MAPFUNCTIONS_getMarkdownStringManual +"\n\n"+
                        internalTests.SVcfTagFunctions.TAGFUNCTIONS_getMarkdownStringManual+"\n\n"+
                        internalTests.SVcfTagFunctions.FMTFUNCTIONS_getMarkdownStringManual +"\n\n"+
                        internalTests.SVcfTagFunctions.TALLYFUNCTIONS_getMarkdownStringManual+"\n\n"+
                        sVcfFilterLogicParser.getMarkdownManualString(Some(vcfFilterManualTitle),Some(vcfFilterManualDesc)) +"\n\n"+
                        sGenotypeFilterLogicParser.getMarkdownManualString(Some(gtFilterManualTitle),Some(gtFilterManualDesc))  +"\n\n"+
                        altCommandDocMd.map{ acm => {
                          wrapLinesWithIndent(acm, internalUtils.commandLineUI.MD_CONSOLE_LINE_WIDTH, "", false) 
                        }}.mkString("\n")+"\n\n";

     /*
      * 
      * Categories:
      * 
      * "Input Parameters",0
      * "Universal Parameters",1
      * "Preprocessing",2
      * 
      * "Annotation", 10
      * "Sample Stats",12
      * "Transcript Annotation",15
      * "SnpEff Annotation Processing",20
      * 
      * "Filtering, Genotype-Level",35
      * "Filtering, Variant-Level"
      * "Merge Multicaller VCFs",50
      * 
      * "Postprocessing",100
      * 
      * "ZZ ALPHA PARAMS, not for general use", 9000
      * "DEPRECATED",9990
      * "INCOMPLETE",9995
      * 
      * 
      * 
      * 
      */
                        
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "walkVcf", 
          aliases = Seq(),
          quickSynopsis = "", 
          synopsis = "", 
          description = "This utility performs a series of transformations on an input VCF file and adds an array of informative tags.",
          argList = 
                    new UnaryArgument( name = "tableInput",
                                         arg = List("--tableInput"), // name of value
                                         argDesc = "todo write desc"+
                                                   "" // description
                                       ).meta(true,"ZZ ALPHA PARAMS, not for general use", 9000) ::
                    new UnaryArgument( name = "tableInputNoID",
                                         arg = List("--tableInputNoID"), // name of value
                                         argDesc = "todo write desc"+
                                                   "" // description
                                       ).meta(true,"ZZ ALPHA PARAMS, not for general use", 9000) ::
                    new UnaryArgument( name = "tableOutput",
                                         arg = List("--tableOutput"), // name of value
                                         argDesc = "todo write desc"+
                                                   "" // description
                                       ).meta(true,"ZZ ALPHA PARAMS, not for general use", 9000) ::
     
                    new BinaryMonoToListArgument[String](
                                         name = "burdenCountsFile",
                                         arg = List("--burdenCountsFile"), 
                                         valueName = "table.file.txt",
                                         argDesc = "If multiple count files are desired for different burden counters, you must give each file an ID using the format: fileID:/path/to/file.txt" // description
                                        ).meta(true,"Sample Stats-DEPRECATED") ::
                                        
                    new BinaryOptionArgument[String](
                                         name = "genomeFA", 
                                         arg = List("--genomeFA"), 
                                         valueName = "genome.fa.gz",  
                                         argDesc =  "The genome fasta file. Can be gzipped or in plaintext."
                                        ).meta(false,"Other Inputs",5) ::
                    
                    new BinaryOptionArgument[String](
                                         name = "groupFile", 
                                         arg = List("--groupFile"), 
                                         valueName = "groups.txt",  
                                         argDesc =  "File containing a group decoder. This is a simple tab-delimited file. "+
                                                    "The first column on each line is the sample ID. All subsequent tab-delimited entries on that line "+
                                                    "will be processed as a sample group ID to which the sample belongs. WARNING: make sure ALL columns in the "+
                                                    "file are categorical. If you include numeric columns then you will get a huge number of groups and may have runtime issues."
                                        ).meta(false,"Other Inputs") :: 
                    new BinaryOptionArgument[String](
                                         name = "superGroupList", 
                                         arg = List("--superGroupList"), 
                                         valueName = "sup1,grpA,grpB,...;sup2,grpC,grpD,...",  
                                         argDesc =  "A list of top-level supergroups. Requires the --groupFile parameter to be set."
                                        ).meta(true,"Other Inputs") :: 
                                        
                    new BinaryOptionArgument[List[String]](
                                         name = "chromList", 
                                         arg = List("--chromList"), 
                                         valueName = "chr1,chr2,...",  
                                         argDesc =  "List of chromosomes. If supplied, then all analysis will be restricted to these chromosomes. All other chromosomes will be ignored. "+
                                                    "For a VCF that contains only one chromosome this option will improve runtime, since the utility will not have to load and process "+
                                                    "annotation data for the other chromosomes."
                                        ).meta(false,"Input Parameters") ::
                    
                    new UnaryArgument( name = "infileList",
                                         arg = List("--infileList"), // name of value
                                         argDesc = "If this option is set, then the infile parameter is a text file containing a list of input VCF files (one per line), rather than a simple path to a single VCF file. "+
                                                   "Multiple VCF files will be concatenated and used as input. Note that only the first file's headers will be used, "+
                                                   "and if any of the subsequent files have tags or fields that are not present in the first VCF file then errors may occur. "+
                                                   "Also note that if the VCF file includes sample genotypes then the samples MUST be in the same order."+
                                                   ""+
                                                   "" // description
                                       ).meta(false,"Input Parameters",0) ::
                                       
                    new BinaryOptionArgument[String](
                                         name = "infileListInfix", 
                                         arg = List("--infileListInfix"), 
                                         valueName = "infileList.txt",  
                                         argDesc =  "If this command is included, then all input files are treated very differently. "+
                                                    "The input VCF file path (or multiple VCFs, if you are running a VCF merge of some sort) must contain a BAR "+
                                                    "character. The file path string will be split at the bar character and the string infixes from the supplied "+
                                                    "infileList.txt infix file will be inserted into the break point. This can be very useful for merging multiple "+
                                                    "chromosomes or genomic-region-split VCFs."
                                        ).meta(false,"Input Parameters") ::
                    new BinaryOptionArgument[String](
                                         name = "listInfix", 
                                         arg = List("--listInfix"), 
                                         valueName = "infix1,infix2,...",  
                                         argDesc =  "If this command is included, then all input files are treated very differently. "+
                                                    "The input VCF file path (or multiple VCFs, if you are running a VCF merge of some sort) must contain a BAR "+
                                                    "character. The file path string will be split at the bar character and the string infixes from the supplied "+
                                                    "list will be inserted into the break point. This can be very useful for merging multiple "+
                                                    "chromosomes or genomic-region-split VCFs."
                                        ).meta(false,"Input Parameters") ::
                                        
                                        
                    new UnaryArgument( name = "splitOutputByChrom",
                                         arg = List("--splitOutputByChrom"), // name of value
                                         argDesc = "If this option is set, the output will be split up into parts by chromosome. "+
                                                   "NOTE: The outfile parameter must be either a file prefix (rather than a full filename), "+
                                                   "or must be a file prefix and file suffix separated by a bar character. In other worse, rather than being " +
                                                   "'outputFile.vcf.gz', it should be either just 'outputFile' or 'outputFile.|.vcf.gz'. "+
                                                   ""// description
                                       ).meta(false,"Output Parameters", 1) ::
                    new BinaryOptionArgument[String]( name = "splitOutputByBed",
                                         arg = List("--splitOutputByBed"), // name of value\
                                         valueName = "intervalBedFile.bed",
                                         argDesc = "If this option is set, the output will be split up into multiple VCF files based on the supplied BED file. "+
                                                   "An output VCF will be created for each line in the BED file. If the BED file has the 4th (optional) column, "+
                                                   "and if this 'name' column contains a unique name with no special characters then this name column will be used as the "+
                                                   "infix for all the output VCF filenames. If the BED file name column is missing, non-unique, or contains illegal characters then "+
                                                   "the files will simply be numbered. "+
                                                   "NOTE: If this option is used, then the 'outfile' parameter must be either a file prefix (rather than a full filename), "+
                                                   "or must be a file prefix and file suffix separated by a bar character. In other worse, rather than being " +
                                                   "'outputFile.vcf.gz', it should be either just 'outputFile' or 'outputFile.|.vcf.gz'. "+
                                                   ""// description
                                       ).meta(false,"Output Parameters") ::
                                       
                    new BinaryOptionArgument[Int](
                                         name = "numLinesRead", 
                                         arg = List("--numLinesRead","--testRunLines","--test"), 
                                         valueName = "N",  
                                         argDesc =  "If this parameter is set, then the utility will stop after reading in N variant lines. Intended for testing purposes."
                                        ).meta(false,"Input Parameters") ::
                    new UnaryArgument( name = "testRun",
                                         arg = List("--testRun","--test","-t"), // name of value
                                         argDesc = "Only read the first 1000 lines. Equivalent to --numLinesRead 1000. Intended for testing purposes."// description
                                       ).meta(true,"Input Parameters") ::
                                        
                    new BinaryMonoToListArgument[String](
                                         name = "FCN", 
                                         arg = List("--FCN","--variantMapFunction","--fcn","-F","-f"),
                                         valueName = "fcnName|ID|param1=p1|param2=p2|...",
                                         argDesc =  "This parameter tells vArmyKnife what to do to your VCF. "+
                                                    "You can specify multiple functions in a single vArmyKnife run, "+
                                                    "and the functions will be executed sequentially in order."+
                                                    ""+
                                                    ""
                                        ).meta(false,"Edit VCF",-1) :: 
                                        
                    new BinaryArgument[String](name = "gtTag",
                                           arg = List("--gtTag"),  
                                           valueName = "GT", 
                                           argDesc = "The genotype tag to use in all functions. Note that many functions have the option to override this.",
                                           defaultValue = Some("GT")
                                           ).meta(true,"Other Inputs") ::
                                        
                    new FinalArgument[String](
                                         name = "infile",
                                         valueName = "infile.vcf.gz",
                                         argDesc = "input VCF file. Can be gzipped or in plaintext. Can use dash - to read from STDIN. Note that multifile processing will obviously not be available in this mode." // description
                                        ).meta(false,"Mandatory Inputs") ::

                    new FinalArgument[String](
                                         name = "outfile",
                                         valueName = "outfile.vcf.gz",
                                         argDesc = "The output file. Can be gzipped or in plaintext. Can use dash - to write to STDOUT Note that multifile processing will obviously not be available in this mode."// description
                                        ).meta(false,"Mandatory Inputs") ::
                    internalUtils.commandLineUI.CLUI_UNIVERSAL_ARGS,

             manualExtras =  manualExtras,
             markdownManualExtras =  markdownManualExtras
       );
     
     def run(args : Array[String]) {
       val out = parser.parseArguments(args.toList.tail);
       if(out){
          runSAddTXAnno( parser.get[String]("infile"),
                       parser.get[String]("outfile"),
                       genomeFA = parser.get[Option[String]]("genomeFA"),
                       chromList = parser.get[Option[List[String]]]("chromList"),
                       groupFile = parser.get[Option[String]]("groupFile"),
                       superGroupList = parser.get[Option[String]]("superGroupList"),
                       infileList = parser.get[Boolean]("infileList"),
                       infileListInfix = parser.get[Option[String]]("infileListInfix"),
                       listInfix = parser.get[Option[String]]("listInfix"),
                       numLinesRead = parser.get[Option[Int]]("numLinesRead"),
                splitOutputByChrom = parser.get[Boolean]("splitOutputByChrom"),
                splitOutputByBed = parser.get[Option[String]]("splitOutputByBed"),
                //tagVariantsFunction = parser.get[List[String]]("tagVariantsFunction"),
                tableInputWithID = parser.get[Boolean]("tableInput"),
                tableInputNoID = parser.get[Boolean]("tableInputNoID"),
                tableOutput = parser.get[Boolean]("tableOutput"),
                //tagVariantsGtCountExpression = parser.get[List[String]]("tagVariantsGtCountExpression"),
                variantMapFunction = parser.get[List[String]]("FCN") ,
                burdenCountsFile = parser.get[List[String]]("burdenCountsFile"),
                calcStatGtTag = parser.get[String]("gtTag")
             )
       }
     }
  }

  
  def runSAddTXAnno(
                vcffile : String, outfile : String, 
                genomeFA : Option[String],
                chromList : Option[List[String]],
                groupFile : Option[String],
                superGroupList : Option[String],
                infileList : Boolean = false,
                infileListInfix : Option[String] = None,
                listInfix : Option[String] = None,
                vcfCodes : VCFAnnoCodes = VCFAnnoCodes(),
                numLinesRead : Option[Int] = None,
                splitOutputByChrom : Boolean = false,
                splitOutputByBed : Option[String] = None,
                //tagVariantsFunction : List[String] = List[String](),
                tableInputWithID : Boolean = false,
                tableInputNoID : Boolean = false,
                tableOutput : Boolean = false,
                //tagVariantsGtCountExpression : List[String]  = List[String](),
                variantMapFunction : List[String] = List[String](),
                burdenCountsFile : List[String] = List[String](),
                calcStatGtTag : String = "GT"
                )  {



    /*
     **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     * **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     * **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     * **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     * **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     * **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     * **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     * **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     * **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     * **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     * **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     * **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     * **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     * **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     * **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     */
    val tableInput = tableInputWithID || tableInputNoID

            val (sampleToGroupMap,groupToSampleMap,groups) : (scala.collection.mutable.AnyRefMap[String,Set[String]],
                           scala.collection.mutable.AnyRefMap[String,Set[String]],
                           Vector[String]) = getGroups(groupFile, None, superGroupList);

            
    //val summaryWriter = if(summaryFile.isEmpty) None else Some(openWriterSmart(summaryFile.get));
    val burdenWriterMap = burdenCountsFile.map{ bcf => 
      val cc = bcf.split("[:]");
      if(cc.length == 2){
        (cc(0),openWriterSmart(cc(1)));
      } else if(cc.length > 2){
        error("Error: burdenCountsFile must be at most 2 elements seperated by a colon: fileID and filePath. Or just filepath");
        ("",openWriterSmart(""))
      } else {
        ("",openWriterSmart(bcf))
      }
      
    }.toMap;
    
    burdenWriterMap.foreach{ case (id,bw) => {
      //out.write(tagID + "\t" + g+"\t"+mtr.count( pp => pp > 0)+"\t"+altCt+"\t"+varCt+"\n");
      bw.write("tagID\tgeneID\tburdenCt\taltCt\tvarCt\tmultiBurdenCt\n");
    }}
    reportln("Initializing VCF walker ... "+getDateAndTimeString,"debug");

    val deepDebugMode = false;
    val debugMode = true;
    
    val ccFcnIdx = variantMapFunction.toSeq.indexWhere{ vmfString => {
      val fullcells = vmfString.split("(?<!\\\\)[|]",-1).map{ xx => xx.replaceAll("\\\\[|]","|") }.map{ s => s.trim() }
      val rawMapType = fullcells.head;
      val mapType = internalTests.SVcfMapFunctions.MAP_ID_MAP(rawMapType);
      mapType == "concordanceCaller" || mapType == "concordanceCallerSV"
    }}

    
    val initWalkers : Seq[SVcfWalker] =         
        ({
            var pprm = Seq[(String,String)]( ("infile",vcffile) );
            chromList.foreach{ cl => {
              pprm = pprm ++ Seq(("chromList",cl.mkString("|")));
            }}
            infileListInfix.foreach{ ili => {
              pprm = pprm ++ Seq(("infileListInfix",ili));
            }} 
            Seq[SVcfWalker]( PassThroughSVcfWalker("inputVCF",debugMode,false,pprm) )
        }) ++ (
            if(deepDebugMode){
              Seq[SVcfWalker]( DebugSVcfWalker() )
            } else {
              Seq[SVcfWalker]()
            }
        ) ++ ({
            
            if( ccFcnIdx >= 1){
              SVcfMapFunctions.getSVcfMapFunction(variantMapFunction=variantMapFunction.take(ccFcnIdx), 
                                               chromList=chromList, 
                                               burdenWriterMap=burdenWriterMap, 
                                               groupFile=groupFile, 
                                               superGroupList=superGroupList, 
                                               genomeFA=genomeFA, 
                                               calcStatGtTag=calcStatGtTag);
            } else {
              Seq[SVcfWalker]()
            }
        })
        
        

        
    val postWalkers : Seq[SVcfWalker] = 
        ({
            SVcfMapFunctions.getSVcfMapFunction(variantMapFunction=variantMapFunction.drop(ccFcnIdx+1), 
                                               chromList=chromList, 
                                               burdenWriterMap=burdenWriterMap, 
                                               groupFile=groupFile, 
                                               superGroupList=superGroupList, 
                                               genomeFA=genomeFA, 
                                               calcStatGtTag=calcStatGtTag);
        }) ++ ( 
            if(debugMode){
              Seq[SVcfWalker]( PassThroughSVcfWalker("outputVCF") )
            } else {
              Seq[SVcfWalker]()
            }
        )
        
        
      addProgressReportFunction(f = (i) => {
        getWarningAndNoticeTallies("   ").mkString("\n      ")
      })

      val validStringRegex = java.util.regex.Pattern.compile("[^a-zA-Z0-9_ .+-]");
      
      val splitFuncOpt : Option[(String,Int) => Option[String]] = splitOutputByBed.map{ f => {
              val arr : internalUtils.genomicAnnoUtils.GenomicArrayOfSets[String] = internalUtils.genomicAnnoUtils.GenomicArrayOfSets[String](false);
              reportln("   Beginning bed file read: "+f+" ("+getDateAndTimeString+")","debug");
              val lines = getLinesSmartUnzip(f);
              var cells = lines.map{line => line.split("\t")}.toVector.map{cells => {
                (cells(0),string2int(cells(1)),string2int(cells(2)),cells.lift(3))
              }}
              var names = cells.map{ case (chrom,start,end,nameOpt) => {
                nameOpt.getOrElse(".")
              }}
              val namesValid = names.forall{ name => {
                val nameValid = ! ( validStringRegex.matcher(name).find());
                if(! nameValid){
                  notice("","IntervalNameNotValid: \""+name+"\"",1)
                }
                nameValid;
              }}
              val namesUnique = names.distinct.length == names.length
              if(! (namesValid && namesUnique) ){
                val spanNumCharLen = String.valueOf( names.length ).length();
                names = names.indices.map{ i => {
                  "part"+zeroPad(i,spanNumCharLen)
                }}.toVector
              }
              val cellsFinal = cells.zip(names).map{ case ((chrom,start,end,nameOpt),name) => {
                (chrom,start,end,name)
              }}
              cellsFinal.foreach{ case (chrom,start,end,name) => {
                  if(start != end){ 
                    arr.addSpan(internalUtils.commonSeqUtils.GenomicInterval(chrom, '.', start,end),name);
                  }
              }}
              arr.finalizeStepVectors;
              val outFunc : ((String,Int) => Option[String]) = ((c : String, p : Int) => {
                val currIvNames = arr.findIntersectingSteps(internalUtils.commonSeqUtils.GenomicInterval(c, '.', p,p+1)).foldLeft(Set[String]()){ case (soFar,(iv,currSet)) => {
                  soFar ++ currSet
                }}.toList.sorted
                if(currIvNames.size > 1){
                  warning("Warning: Variant spans multiple intervals in the split interval BED file.","VariantSpansMultipleSplitterIVs",100)
                }// else if(currIvNames.size == 0){
                //  warning("Warning: variant found on span that is not covered by any interval in the span interval split bed! This variant will be DROPPED!","VariantSpandsNoneSplitterIVs",100);
                //}
                currIvNames.headOption
              })
          Some(outFunc)
      }}.getOrElse({
        if(splitOutputByChrom){
          Some((
              ((c : String, p : Int) => {
                Some(c);
              })
          ))
        } else {
          None
        }
      })


    val infixesList = infileListInfix.map{ ili => {
      getLinesSmartUnzip(ili).toVector
    }}.getOrElse(Vector()) ++ listInfix.map{ li => {
      li.split(",").toVector;
    }}.getOrElse(Vector());
    
      
    if(ccFcnIdx >= 0){
      val preWalker : SVcfWalker = chainSVcfWalkers(initWalkers);
      
      if( ! vcffile.contains(';') ) warning("Error: runEnsembleMerger is set, the infile parameter must contain semicolons (\";\")","ConcordanceCaller_warning_no_semicolons_in_infile",-1);
      val vcfList = vcffile.split(";").map{ fxx => { fxx.trim() }}
      val (iterSeq,headerSeq) = vcfList.toSeq.zipWithIndex.map{ case (vf,idx) => {
          val (infiles,infixes) : (String,Vector[String]) = if(infixesList.length > 0){
            val (infilePrefix,infileSuffix) = (vf.split("\\|").head,vf.split("\\|")(1));
            (infixesList.map{ infix => infilePrefix+infix+infileSuffix }.mkString(","), infixesList)
          } else {
            (vf,Vector())
          }
          val ifl = infixes.length > 0 || infileList;
          
          val (vcIterRaw, vcfHeaderRaw) = getSVcfIterators(infiles,chromList=chromList,numLinesRead=numLinesRead,inputFileList = ifl, withProgress = idx == 0, infixes = infixes);
          val (vcIter,vcfHeader) = preWalker.walkVCF(vcIterRaw,vcfHeaderRaw);
          val vcIterBuf = vcIter.buffered;
          (vcIterBuf,vcfHeader);
      }}.unzip;
      
      val vmfString = variantMapFunction(ccFcnIdx)
      val params = {
        val fullcells = vmfString.split("(?<!\\\\)[|]",-1).map{ xx => xx.replaceAll("\\\\[|]","|") }.map{ s => s.trim() }
        if(fullcells.length < 2){
          error("variantMapFunction must be composed of at least 2 |-delimited elements: the mapFunctionType and the walker ID. In most cases it will also require additional parameters. Found: [\""+fullcells.mkString("\"|\"")+"\"]");
        }
        val rawMapType = fullcells.head;
        val mapType = internalTests.SVcfMapFunctions.MAP_ID_MAP(rawMapType);
        val mapID = fullcells.lift(1).getOrElse("");
        val tagPrefix = if(mapID == "") "" else mapID + "_";
        val sc = fullcells.drop(2);
        val params = ParsedParamStrSet(sc, internalTests.SVcfMapFunctions.MAP_FUNCTIONS(mapType))
        params.set("mapID",mapID);
        params.set("mapType",mapType);
        params.set("tagPrefix",tagPrefix);
        params;
      }
      

      
      
      if(params("mapType") == "concordanceCaller"){
        val inputNames : List[String]  = params.get("callerNames").map{ x => x.split(",").toList }.getOrElse(headerSeq.indices.toList.map{"C"+_.toString});
        val decision : String  = params.get("gtDecisionMethod").getOrElse("priority");
        val inputPriority : List[String]  = params.get("priority").map{ x => x.split(",").toList }.getOrElse(inputNames);
        val CC_ignoreSampleIds = params.isSet("ignoreSampleIds")
        val CC_ignoreSampleOrder = params.isSet("ignoreSampleOrder")
          
        val (ensIter,ensHeader) = {
          ensembleMergeVariants(iterSeq,headerSeq,inputVcfTypes = inputNames,
                                                        //genomeFA = genomeFA,
                                                        //windowSize = 200, 
                                                        singleCallerPriority = inputPriority,
                                                        CC_ignoreSampleIds = CC_ignoreSampleIds, CC_ignoreSampleOrder=CC_ignoreSampleOrder)
        }
        val finalWalker : SVcfWalker = chainSVcfWalkers(Seq[SVcfWalker](
            new EnsembleMergeMetaDataWalker(inputVcfTypes = inputPriority,
                                            decision = decision)
        ) ++ postWalkers);
      
        finalWalker.walkToFileSplit(outfile,ensIter,ensHeader, splitFuncOpt = splitFuncOpt);
      } else {
                         //crossChromWin : Int = 500,
                            //withinChromWin : Int = 500,
        val inputNames : List[String]  = params.get("callerNames").map{ x => x.split(",").toList }.getOrElse(headerSeq.indices.toList.map{"C"+_.toString});
        val crossChromWin = string2int(params("crossChromWindow"))
        val withinChromWin = string2int(params("withinChromWindow"))
        val CC_ignoreSampleIds = params.isSet("ignoreSampleIds")

        val (ensIter,ensHeader) = ensembleMergeSV(iterSeq,crossChromWin=crossChromWin,withinChromWin=withinChromWin,headers=headerSeq,inputVcfTypes=inputNames, CC_ignoreSampleIds = CC_ignoreSampleIds)
        
        val finalWalker : SVcfWalker = chainSVcfWalkers(postWalkers)
        finalWalker.walkToFileSplit(outfile,ensIter,ensHeader, splitFuncOpt = splitFuncOpt);

      }

      
    } else {
        
      val allWalkers : Seq[SVcfWalker] = initWalkers ++ postWalkers;
      val finalWalker : SVcfWalker = chainSVcfWalkers(allWalkers)
      
      reportln("All VCF Walkers initialized! "+getDateAndTimeString,"debug");
      infileListInfix match {
        case Some(ili) => {
          if( ! vcffile.contains('|') ) error("Error: infileListInfix is set, the infile parameter must contain a bar symbol (\"|\")");
          
          val (infilePrefix,infileSuffix) = (vcffile.split("\\|").head,vcffile.split("\\|")(1));
          val infiles = getLinesSmartUnzip(ili).map{ infix => infilePrefix+infix+infileSuffix }.mkString(",")
          finalWalker.walkVCFFiles(infiles,outfile, chromList, numLinesRead = numLinesRead, inputFileList = false, dropGenotypes = false, splitFuncOpt = splitFuncOpt);
        }
        case None => {
          if( (! tableInput) && (! tableOutput)){
            reportln("No table input and no table output. Reading/writing in VCF format. ("+getDateAndTimeString+")","debug")
            finalWalker.walkVCFFiles(vcffile,outfile, chromList, numLinesRead = numLinesRead, inputFileList = infileList, dropGenotypes = false, splitFuncOpt = splitFuncOpt);
          } else {
            val (vcIterRaw, vcfHeader) = if(!tableInput){
              reportln("No --tableInput param set. Reading in VCF format.","debug")
              getSVcfIterators(infileString=vcffile,chromList=chromList,numLinesRead=numLinesRead,inputFileList = infileList);
            } else {
              reportln("PARAM --tableInput set. Reading in TABLE format.","debug")
              getSVcfIteratorsFromTable(    infileString=vcffile,chromList=chromList,numLinesRead=numLinesRead,inputFileList = infileList, hasIDcol = ! tableInputNoID);                
            }
            val (newIter,newHeader) = finalWalker.walkVCF(vcIterRaw,vcfHeader);
            if(tableOutput){
              reportln("PARAM --tableOutput set. Writing in TABLE format.","debug")
              if(splitFuncOpt.isDefined){
                warning("Cannot write to file and split!","CANNOT_SPLIT_TABLES",-1); //note, move this check up and add more compatibility checks!
              }
              finalWalker.writeToTableFile(outfile = outfile, vcIter = newIter,vcfHeader = newHeader)
            } else {
              reportln("PARAM --tableOutput NOT set. Writing in VCF format.","debug")
              finalWalker.writeToFileSplit(outfile=outfile, vcIter = newIter, vcfHeader = newHeader, dropGenotypes = false, splitFuncOpt = splitFuncOpt);
            }
          }
          
          /*
           
          val (vcIterRaw, vcfHeader) = getSVcfIterators(infiles,chromList=chromList,numLinesRead=numLinesRead,inputFileList = inputFileList, infixes = infixes);
          val (newIter,newHeader) = walkVCF(vcIter,vcfHeader);
          writeToFileSplit(outfile=outfile,vcIter=newIter,vcfHeader=newHeader,dropGenotypes=dropGenotypes, splitFuncOpt=splitFuncOpt);
          
          walkToFileSplit(outfile=outfile, vcIter = vcIterRaw, vcfHeader = vcfHeader, dropGenotypes = dropGenotypes, splitFuncOpt = splitFuncOpt);
       
           */
        }
      }
    }
    
    burdenWriterMap.foreach{ case (id,bw) => {
      reportln("Closing burden count writer: "+id,"note");
      bw.flush();
      bw.close();
    }}
    //if(! summaryWriter.isEmpty) summaryWriter.get.close();
  }
  
    /*
     **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     * **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     * **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     * **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     * **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     * **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     * **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     * **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     * **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     * **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     * **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     * **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     * **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     * **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     * **************************************************************************************************************************************************** **************************************************************************************************************************************************** 
     */
  
  
}









 


















