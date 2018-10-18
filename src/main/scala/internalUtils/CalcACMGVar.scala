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
import internalUtils.fileUtils._;

import htsjdk.variant._;
import htsjdk.variant.variantcontext._;
import htsjdk.variant.vcf._;

import VcfTool._;

import internalUtils.commandLineUI._;

import internalUtils.TXUtil.KnownVarHolder;
import internalUtils.TXUtil.pVariantInfo;


object CalcACMGVar {
  
  class CmdAssessACMG extends CommandLineRunUtil {
     override def priority = 11;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "CmdAssessACMG", 
          quickSynopsis = "",  
          synopsis = "", 
          description = "BETA: This function consolidates information from a wide variety of different input files and attempts to calculate a "+
                        "subset of the ACMG guidelines criteria. It then attempts to assign pathogenicity scores. " + BETA_WARNING,   
          argList = 
                    new BinaryOptionArgument[List[String]](
                                         name = "chromList", 
                                         arg = List("--chromList"), 
                                         valueName = "chr1,chr2,...",  
                                         argDesc =  "List of chromosomes. If supplied, then all analysis will be restricted to these chromosomes. All other chromosomes wil be ignored."
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "clinVarVcf", 
                                         arg = List("--clinVarVcf"), 
                                         valueName = "clinVarVcf.vcf",  
                                         argDesc =  "Processed clinvar variant vcf file. This file must have been processed by the addTxInfoToVCF command."
                                        ) ::  
                                        
                    new BinaryOptionArgument[String](
                                         name = "txToGeneFile", 
                                         arg = List("--txToGeneFile"), 
                                         valueName = "txToGene.txt",  
                                         argDesc =  "File containing the mapping of transcript names to gene symbols. This file must have 2 columns: the txID and the geneID. No header line."
                                        ) :: 
                    new BinaryOptionArgument[String](
                                         name = "canonicalTxFile", 
                                         arg = List("--canonicalTxFile"), 
                                         valueName = "knownCanonical.txt",
                                         //argDesc =  "A file containing a list of transcript ID's and whether or not they are a RefSeq transcript. Must have at least 2 labelled columns, name and isRefSeq. The header line may begin with a #, or not. It can be compressed or in plaintext."
                                         argDesc =  "A file containing a list of canonical transcript IDs. It must have a header line with a column labelled \"transcript\". The header line may begin with a #, or not. It can be compressed or in plaintext."
                                        ) :: 
                    new BinaryOptionArgument[String](
                                         name = "rmskFile", 
                                         arg = List("--rmskFile"), 
                                         valueName = "rmsk.txt.gz",  
                                         argDesc =  "rmsk.txt.gz file, from UCSC. The only columns that matter are the 6th through 8th columns, which specify the chromosome, starts, and ends of each repetitive region (counting from 0, upper bound exclusive)"
                                        ) :: 
                    new BinaryOptionArgument[String](
                                         name = "toleranceFile", 
                                         arg = List("--toleranceFile"), 
                                         valueName = "toleranceFile.txt",  
                                         argDesc =  "This file must contain three columns (labelled in a header line): geneID (gene symbol), LOFtolerant, and MIStolerant. Genes that are not included in this list will be assumed to be non-tolerant."
                                        ) :: 
                    new BinaryOptionArgument[String](
                                         name = "domainFile", 
                                         arg = List("--domainFile"), 
                                         valueName = "domainFile.txt",  
                                         argDesc =  "This file must contain at least four columns (labelled in a header line): chrom, start, end, and domainID."
                                        ) :: 
                    new BinaryOptionArgument[String](
                                         name = "pseudoGeneGTF", 
                                         arg = List("--pseudoGeneGTF"), 
                                         valueName = "pseudoGeneGTF.gtf.gz",  
                                         argDesc =  "Simple GTF file containing a list of pseudogenes."
                                        ) :: 
                    new BinaryOptionArgument[String](
                                         name = "inSilicoKeys", 
                                         arg = List("--inSilicoKeys"), 
                                         valueName = "dbNSFP_MetaSVM_pred:D:T",
                                         argDesc =  "This must be a comma-delimited list (no spaces). Each element in the list must consist of 3 parts, seperated by colons (\":\"). "+
                                                    "The first part is the INFO key referring to the stored results of an in silico prediction algorithm. The 2nd is an operator, which can be either \"eq\", \"ge\", or \"le\". "+
                                                    "For \"eq\", the third and fourth columns can be \"|\"-delimited lists of values that will be interpreted as \"damaging\" or \"benign\" respectively. "+
                                                    "For the other two functions, the third and fourth columns are values referring to the thresholds for counting the variant as \"damaging\" or \"benign\" respectively. "+
                                                    "Higher is assumed to mean more damaging for \"ge\", and lower is more damaging for \"le\". "+
                                                    "This utility will calculate a \"summary\" statistic for each specified algorithm which lists the variant as either damaging, ambiguous, benign, or unknown. Variants listed as both damaging as benign will be listed as ambiguous, "+
                                                    "and variants that are listed as both damaging and unknown will be listed as damaging, and similar for benign."
                                        ) :: 
                    new BinaryArgument[List[String]](
                                         name = "hgmdPathogenicClasses", 
                                         arg = List("--hgmdPathogenicClasses"), 
                                         valueName = "DM,DM?",
                                         argDesc =  "",
                                         defaultValue = Some(List("DM"))
                                        ) :: 
                    new BinaryArgument[String](
                                         name = "inSilicoMergeMethod", 
                                         arg = List("--inSilicoMergeMethod"), 
                                         valueName = "smart",
                                         argDesc =  "Currently the only legal value is the default, \"smart\", or \"intersection\"",
                                         defaultValue = Some("smart")
                                        ) :: 
                    new BinaryOptionArgument[String](
                                         name = "conservedElementFile", 
                                         arg = List("--conservedElementFile"), 
                                         valueName = "conservedElementFile.txt",  
                                         argDesc =  "This file contains the spans for the conserved element regions found by GERP. This file must contain 3 columns (no header line): chrom, start end."
                                        ) :: 
                    new BinaryOptionArgument[String](
                                         name = "lowMapBed", 
                                         arg = List("--lowMapBed"), 
                                         valueName = "lowMapBed.bed.gz",  
                                         argDesc =  "This simple bed file should contain all spans with mappability less than 1."
                                        ) :: 
                    new BinaryArgument[List[String]](name = "ctrlAlleFreqKeys",
                                           arg = List("--ctrlAlleFreqKeys"),  
                                           valueName = "key1,key2,...", 
                                           argDesc = "List of VCF INFO tags tcontaining the allele frequencies for the control datasets.",
                                           defaultValue = Some(List("1KG_AF","ESP_EA_AF","ExAC_ALL"))
                                           ) ::
                    new BinaryArgument[Double](name = "BA1_AF",
                                           arg = List("--BA1_AF"),  
                                           valueName = "val", 
                                           argDesc = "The allele frequency cutoff to assign BA1 (benign) status.",
                                           defaultValue = Some(0.05)
                                           ) ::
                    new BinaryArgument[Double](name = "PM2_AF",
                                           arg = List("--PM2_AF"),  
                                           valueName = "val", 
                                           argDesc = "The allele frequency cutoff to assign PM2 (moderate pathogenic) status.",
                                           defaultValue = Some(0.0001)
                                           ) ::
                    new BinaryOptionArgument[String](
                                         name = "groupFile", 
                                         arg = List("--groupFile"), 
                                         valueName = "groups.txt",  
                                         argDesc =  "File containing a group decoder. This is a simple 2-column file (no header line). The first column is the sample ID, the 2nd column is the group ID."
                                        ) ::  
                    new BinaryOptionArgument[String](
                                         name = "hgmdVarVcf", 
                                         arg = List("--hgmdVarVcf"), 
                                         valueName = "HGMD.vcf.gz",  
                                         argDesc =  "File containing HGMD variants All variants will be assumed to be likely pathogenic."
                                        ) :: 
                    new BinaryOptionArgument[String](
                                         name = "superGroupList", 
                                         arg = List("--superGroupList"), 
                                         valueName = "sup1,grpA,grpB,...;sup2,grpC,grpD,...",  
                                         argDesc =  "A list of top-level supergroups. Requires the --groupFile parameter to be set."
                                        ) :: 
                    new BinaryOptionArgument[String](
                                         name = "summaryOutputFile", 
                                         arg = List("--summaryOutputFile"), 
                                         valueName = "summaryOutputFile.txt",  
                                         argDesc =  "Optional summary output file."
                                        ) :: 
                    new UnaryArgument( name = "infileList",
                                         arg = List("--infileList"), // name of value
                                         argDesc = "Use this option if you want to provide input file(s) containing a list of input files rather than a single input file"+
                                                   "" // description
                                       ) ::
                    new BinaryOptionArgument[String](
                                         name = "infileListInfix", 
                                         arg = List("--infileListInfix"), 
                                         valueName = "infileList.txt",  
                                         argDesc =  ""+
                                                    ""
                                        ) ::
                                       
                    new UnaryArgument( name = "newParser",
                                         arg = List("--newParser"), // name of value
                                         argDesc = "The default parser. Adding this parameter has no effect but is available for backwards compatibility with previous versions."+
                                                   "" // description
                                       ) ::
                    new UnaryArgument( name = "oldParser",
                                         arg = List("--oldParser"), // name of value
                                         argDesc = "Deprecated, may not support all features. Included for testing purposes."+
                                                   "" // description
                                       ) ::
                    //new BinaryOptionArgument[List[String]](
                    //                     name = "dropKeys", 
                    //                     arg = List("--dropKeys"), 
                    //                     valueName = "key1,key2,...",  
                    //                     argDesc =  "A list of INFO keys to omit from the output VCF."
                    //                    ) :: 
                    new FinalArgument[String](
                                         name = "invcf",
                                         valueName = "variants.vcf",
                                         argDesc = "input VCF file. This file must have been processed by " // description
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
       
       val swalker = CalcACMGVar.AssessACMGWalker(
                   chromList = parser.get[Option[List[String]]]("chromList"),
                   clinVarVcf = parser.get[Option[String]]("clinVarVcf").get,
                   txToGeneFile = parser.get[Option[String]]("txToGeneFile"),
                   ctrlAlleFreqKeys = parser.get[List[String]]("ctrlAlleFreqKeys"),
                   toleranceFile = parser.get[Option[String]]("toleranceFile"),
                   domainFile = parser.get[Option[String]]("domainFile"),
                   conservedElementFile = parser.get[Option[String]]("conservedElementFile"),
                   BA1_AF = parser.get[Double]("BA1_AF"),
                   PM2_AF = parser.get[Double]("PM2_AF"),
                   rmskFile = parser.get[Option[String]]("rmskFile"),
                   refSeqFile = parser.get[Option[String]]("canonicalTxFile"),
                   inSilicoParams = parser.get[Option[String]]("inSilicoKeys"),
                   inSilicoMergeMethod = parser.get[String]("inSilicoMergeMethod"),
                   hgmdVarVcf = parser.get[Option[String]]("hgmdVarVcf"),
                   lowMapBed = parser.get[Option[String]]("lowMapBed"),
                   pseudoGeneGTF = parser.get[Option[String]]("pseudoGeneGTF"),
                   hgmdPathogenicClasses = parser.get[Seq[String]]("hgmdPathogenicClasses"),
                   //infileListInfix = parser.get[Option[String]]("infileListInfix"),
                   
                   locusRepetitiveTag = None,
                   locusDomainTag = None,
                   locusConservedTag = None,
                   locusMappableTag = None
                   //dropKeys = parser.get[Option[List[String]]]("dropKeys"),
                   )
       if(parser.get[Boolean]("oldParser")){
         swalker.getOldVcfWalker.chain(CalcACMGVar.SummaryACMGWalker(
                             groupFile = parser.get[Option[String]]("groupFile"),
                             groupList = None,
                             superGroupList = parser.get[Option[String]]("superGroupList"),
                             outfile = parser.get[Option[String]]("summaryOutputFile").getOrElse("undefinedfile.txt")
                           ), flag = parser.get[Option[String]]("summaryOutputFile").isDefined
                   ).walkVCFFiles(
                     infile    = parser.get[String]("invcf"),
                     outfile   = parser.get[String]("outvcf"),
                     chromList = parser.get[Option[List[String]]]("chromList"),
                     infileList = parser.get[Boolean]("infileList")
                   )
       } else {
         val vcffile = parser.get[String]("invcf")
         val infileList = parser.get[Boolean]("infileList")
         val chromList = parser.get[Option[List[String]]]("chromList")
         val outfile = parser.get[String]("outvcf")
         val infileListInfix = parser.get[Option[String]]("infileListInfix")
         
          infileListInfix match {
            case Some(ili) => {
              if( ! vcffile.contains('|') ) error("Error: infileListInfix is set, the infile parameter must contain a bar symbol (\"|\")");
              
              val (infilePrefix,infileSuffix) = (vcffile.split("\\|").head,vcffile.split("\\|")(1));
              val infiles = getLinesSmartUnzip(ili).map{ infix => infilePrefix+infix+infileSuffix }.mkString(",")
              //finalWalker.walkVCFFiles(infiles,outfile, chromList, numLinesRead = numLinesRead, inputFileList = false, dropGenotypes = false);
              
              swalker.walkVCFFiles(
                         infiles    = infiles,
                         outfile   = outfile,
                         chromList = chromList,
                         numLinesRead = None,
                         inputFileList = false,
                         dropGenotypes = false
                       )
            }
            case None => {
              swalker.walkVCFFiles(
                         infiles    = vcffile,
                         outfile   = outfile,
                         chromList = chromList,
                         numLinesRead = None,
                         inputFileList = infileList,
                         dropGenotypes = false
                       )
            }
          }
         

       }
           
     }
  }
  }
  
  
  
  
  class AddMoreGeneAnno(txToGeneFile : Option[String], refSeqFile : Option[String]) extends SVcfWalker {
    def walkerName : String = "AddMoreGeneAnno"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("txToGeneFile", txToGeneFile.getOrElse(".")),
        ("refSeqFile", refSeqFile.getOrElse("."))
    );
    val vcfCodes : VCFAnnoCodes = VCFAnnoCodes()
    
    reportln("Reading txToGene map... ["+stdUtils.getDateAndTimeString+"]","debug");
    val txToGene : (String => String) = txToGeneFile match {
      case Some(f) => {
        val txToGeneMap = getLinesSmartUnzip(f).map(line => {
          val cells = line.split("\t");
          (cells(0),cells(1));
        }).toMap
        ((s : String) => txToGeneMap.getOrElse(s,s));
      }
      case None => {
        ((s : String) => s);
      }
    }
    reportln("Reading geneToTX map... ["+stdUtils.getDateAndTimeString+"]","debug");

    val geneToTx : (String => Set[String]) = txToGeneFile match {
      case Some(f) => {
        val geneToTxMap = scala.collection.mutable.AnyRefMap[String,Set[String]]().withDefault(x => Set[String]());
        getLinesSmartUnzip(f).foreach(line => {
          val cells = line.split("\t");
          geneToTxMap(cells(1)) = geneToTxMap(cells(1)) + cells(0);
        })
        ((s : String) => geneToTxMap.getOrElse(s,Set[String]()));
      }
      case None => {
        ((s : String) => Set[String](s));
      }
    }
    
    reportln("Reading refSeq map... ["+stdUtils.getDateAndTimeString+"]","debug");
    
    val isRefSeq : (String => Boolean) = refSeqFile match {
      case Some(f) => {
        val lines = getLinesSmartUnzip(f);
        val table = getTableFromLines(lines,colNames = Seq("transcript"), errorName = "File "+f);
        var refSeqSet : Set[String] = table.map(tableCells => {
          val tx = tableCells(0);
          tx
        }).toSet
        reportln("   found: "+refSeqSet.size+" RefSeq transcripts.","debug");
        ((s : String) => {
          refSeqSet.contains(s);
        })
      }
      case None => {
        ((s : String) => {
          false;
        })
      }
    }
    
    val typeSubsets : Seq[(String,String,TXUtil.pVariantInfo => Boolean,Seq[SVcfCompoundHeaderLine])] = Seq[(String,String,TXUtil.pVariantInfo => Boolean)](
        ("mis", "missense AA swap", ((info : TXUtil.pVariantInfo) => {
                     info.subType == "swapAA"
        })),
        ("LOF", "Loss of function (start loss, stop loss, frameshift, stop-gain, exon deletion, whole gene deletion, splice junction change or splice junction loss)", ((info : TXUtil.pVariantInfo) => {
                     info.severityType == "LLOF" || info.severityType == "PLOF"
        })),
        ("NONSYNON", "Nonsynonymous (any variant that causes a change in the amino acid sequence of the resulting protein. Includes missense SNVs, in frame indels, and all LOF variants)", ((info : TXUtil.pVariantInfo) => {
            info.severityType == "LLOF" || info.severityType == "PLOF" || info.severityType == "NONSYNON"
        })),
        ("CODING", "Coding Region (Any variant that occurs on a coding region andor causes a change in the amino acid sequence)", ((info : TXUtil.pVariantInfo) => {
            info.severityType == "LLOF" || info.severityType == "PLOF" || info.severityType == "NONSYNON" || info.severityType == "SYNON"
        })),
        ("spliceModify", "Splice change (Indel that spans a splice junction or any base change to the 2bp region adjacent to a splice junction.)", ((info : TXUtil.pVariantInfo) => {
                     info.subType.startsWith("splice")
        })),
        ("startLoss", "Start loss (any change that results in the loss of the AUG codon at the start of the transcript)", ((info : TXUtil.pVariantInfo) => {
                     info.subType == "START-LOSS" || info.subType == "startIndel"
        })),
        ("weirdVarType", "Misc/Other (Unusual variant that cannot be easily categorized. Usually large-span indels.)", ((info : TXUtil.pVariantInfo) => {
                     info.subType == "???" || info.subType == "FullIntronIndel" || info.subType == "FullExonIndel"
        })),
        ("stopLoss", "Stop loss (any change that results in the loss of the transcripts normal stop codon)", ((info : TXUtil.pVariantInfo) => {
                     info.subType == "STOP-LOSS"
        })),
        ("fs", "FrameShift (any indel that causes a frameshift. Note that some frameshift variants may also be classified as stopGain variants if the first frameshifted codon is a stop codon)", ((info : TXUtil.pVariantInfo) => {
                     info.subType == "fs" || info.subType == "fsSTOP"
        })),
        ("stopGain", "Stop Gain (any variant that creates a premature stop codon)", ((info : TXUtil.pVariantInfo) => {
                     info.pType == "STOP-GAIN"
        }))
    ).map{ case (typeSubsetName,typeSubsetDesc,infoFunc) => {
      val infoLines : Seq[SVcfCompoundHeaderLine] = Seq[SVcfCompoundHeaderLine](
          new SVcfCompoundHeaderLine("INFO","SWH_ANNO_geneList_"+typeSubsetName,Number=".",Type="String",desc="Comma delimited list of gene names for which this variant is of type: "+typeSubsetDesc).addWalker(this),
          new SVcfCompoundHeaderLine("INFO","SWH_ANNO_geneList_"+typeSubsetName+"_CANON",Number=".",Type="String",desc="(For canonical TX only) the list of gene names for which this variant is of type: "+typeSubsetDesc).addWalker(this),
          new SVcfCompoundHeaderLine("INFO","SWH_ANNO_txList_"+typeSubsetName,Number=".",Type="String",desc="The list of transcript IDs for which this variant is of type: "+typeSubsetDesc).addWalker(this)
      );
      (typeSubsetName,typeSubsetDesc,infoFunc,infoLines)
    }}
    
    //
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader;
      outHeader.addWalk(this);
      outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",vcfCodes.assess_WARNINGS,Number=".",Type="String",desc="Warnings thrown during vArmyKnife transcript annotation.").addWalker(this));
      //outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",vcfCodes.assess_ACMG_numGenes,Number=".",Type="String",desc="Number of genes that this variant is on or near.").addWalker(this));
      //outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",vcfCodes.assess_ACMG_numGenes_CANON,Number=".",Type="String",desc="Number of genes for which this variant is on or near the canonical transcript.").addWalker(this));
      outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",vcfCodes.assess_geneList,Number=".",Type="String",desc="Number of genes that this variant is on or near.").addWalker(this));
      outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",vcfCodes.assess_geneTxList,Number=".",Type="String",desc="List of all transcripts for the genes that this variant is on or near. Note that this includes transcripts that are not near this variant, for reference.").addWalker(this));
      outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",vcfCodes.assess_refSeqKnown,Number=".",Type="String",desc="Indicates whether the canonical transcript is known for each gene near this variant.").addWalker(this));
      //outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",vcfCodes.assess_LofGenes,Number=".",Type="String",desc="List of genes for which this variant appears LOF, based on vArmyKnife method.").addWalker(this));
      //outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",vcfCodes.assess_LofTX,Number=".",Type="String",desc="List of tx for which this variant appears to be a missense SNV, based on vArmyKnife method.").addWalker(this));
      //outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",vcfCodes.assess_MisGenes,Number=".",Type="String",desc="List of genes for which this variant appears to be a missense SNV based on vArmyKnife method.").addWalker(this));
      //outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",vcfCodes.assess_MisTX,Number=".",Type="String",desc="List of tx for which this variant appears to change the protein, based on vArmyKnife method.").addWalker(this));
      outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",vcfCodes.assess_geneMisTxRatio ,Number=".",Type="String",desc="For each gene, the ratio of transcripts that this variant appears to change, non LOF, based on vArmyKnife method.").addWalker(this));
      outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",vcfCodes.assess_geneLofTxRatio,Number=".",Type="String",desc="For each gene, the ratio of transcripts that this variant appears to be LOF, based on vArmyKnife method.").addWalker(this));
      //outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",vcfCodes.assess_LofGenes+"_CANON",Number=".",Type="String",desc="List of genes where this variant is LOF for the canonical TX.").addWalker(this));
      //outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",vcfCodes.assess_MisGenes+"_CANON",Number=".",Type="String",desc="List of genes where this variant causes a coding change in the canonical TX.").addWalker(this));
      //outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",vcfCodes.assess_NonsynonGenes,Number=".",Type="String",desc="No description yet.").addWalker(this));
      //outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",vcfCodes.assess_NonsynonTX,Number=".",Type="String",desc="No description yet.").addWalker(this));
      //outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",vcfCodes.assess_NonsynonGenes+"_CANON",Number="No description yet.",Type="String",desc=".").addWalker(this));
      outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",vcfCodes.assess_WARNFLAG,Number=".",Type="String",desc="No description yet.").addWalker(this));
      //outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",vcfCodes.assess_CodingGenes,Number=".",Type="String",desc="No description yet.").addWalker(this));
      //outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",vcfCodes.assess_CodingTX,Number=".",Type="String",desc="No description yet.").addWalker(this));
      //outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",vcfCodes.assess_CodingGenes+"_CANON",Number=".",Type="String",desc="No description yet.").addWalker(this));
      
      
      typeSubsets.foreach{ case (tssName,tssDesc,tssInfoFunc,tssInfoLines) => {
        tssInfoLines.foreach{ tssil => {
          outHeader.addInfoLine(tssil);
        }}
      }}
      
      val overwriteInfos : Set[String] = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( outHeader.addedInfos );
      if( overwriteInfos.nonEmpty ){
        notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      }
      
      (vcMap(vcIter){ vc => {
        val vb = vc.getOutputLine;
        vb.dropInfo(overwriteInfos);
        val vb2 = addMoreGeneAnno(vb,txToGene = txToGene,geneToTx = geneToTx, isRefSeq = isRefSeq,vcfCodes=vcfCodes);
        vb2;
      }},outHeader);
    }
      
      
  def addMoreGeneAnno(v : SVcfVariantLine, 
                    txToGene : (String => String) = ((s : String) => s),
                    geneToTx : (String => Set[String]),
                              isRefSeq : (String => Boolean),
                              vcfCodes : VCFAnnoCodes = VCFAnnoCodes()) : SVcfVariantLine = {
    var vb = v.getOutputLine();


    val txList = v.getInfoList(vcfCodes.txList_TAG)
    
    if(txList.filter(tx => tx != ".").length > 0){
      var problemList = v.getInfo(vcfCodes.assess_WARNINGS).split(",").filter(_ == ".").toSet;
          /*
               vb.addInfo(vcfCodes.assess_WARNFLAG, if(problemList.isEmpty) "0" else "1" );
               vb.addInfo(vcfCodes.assess_WARNINGS, problemList.toVector.sorted.padTo(1,".").mkString(",") );
    
           */
      val chrom = v.chrom
      val pos = v.pos
      
      val refAlle = v.ref
      val altAlleles = v.alt.zipWithIndex.filter{ case (alt,altIdx) => alt != VcfTool.UNKNOWN_ALT_TAG_STRING };
      
      notice("tx found for variant","TX_FOUND",0);
      val geneList = txList.map(x => txToGene(x));
        val vTypesList = v.getInfoArray(vcfCodes.vType_TAG);
        val vLVL       = v.getInfoList(vcfCodes.vMutLVL_TAG);
        val vMutPList = v.getInfoArray(vcfCodes.vMutP_TAG);
        val vMutCList = v.getInfoArray(vcfCodes.vMutC_TAG);
        val vMutInfoList = v.getInfoArray(vcfCodes.vMutINFO_TAG).map{k => k.map(x => {internalUtils.TXUtil.getPvarInfoFromString(x)})};
        
        val variantIV = commonSeqUtils.GenomicInterval(chrom,'.', start = pos - 1, end = pos + math.max(v.ref.length,1));
        if(altAlleles.length > 1){
          error("FATAL ERROR: Cannot deal with multiallelic variants! Split variants to single-allelic!");
        }
        val (alt,altIdx) = altAlleles.head;
        
        //val out : Vector[ACMGCritSet] = altAlleles.map{ case (alt,altIdx) => {
          val vTypes = vTypesList(altIdx).map(_.split("_"));
          val vMutP = vMutPList(altIdx);
          val vMutC = vMutCList(altIdx);
          val vInfo = vMutInfoList(altIdx);
          val combo = txList.zip(vInfo).zip(vMutC).zipWithIndex.map{case (((tx,info),c),i) => (txToGene(tx),tx,info,c,i)}
          val canonCombo = combo.filter{ case (g,tx,info,c,i) => isRefSeq(tx) }
          
          if(combo.exists{ case (g,tx,info,c,i) => { info.subType == "???" }}){
            problemList = problemList + "VT|OddballVariantType" + "VT|???";
          }
          if(combo.exists{ case (g,tx,info,c,i) => { info.subType == "FullExonIndel" }}){
            problemList = problemList + "VT|OddballVariantType" + "VT|FullExonIndel";
          }
          if(combo.exists{ case (g,tx,info,c,i) => { info.subType == "FullIntronIndel" }}){
            problemList = problemList + "VT|OddballVariantType" + "VT|FullIntronIndel";
          }
          if(combo.exists{ case (g,tx,info,c,i) => { info.subType == "total-loss" }}){
            problemList = problemList + "VT|OddballVariantType" + "VT|totalloss";
          }
          val geneSet = geneList.toSet.toVector.sorted
          val geneSetCanon = canonCombo.map{ case (g,tx,info,c,i) => g}.toSet.toVector.sorted;
          
          //vb.addInfo(vcfCodes.assess_ACMG_numGenes , geneSet.size.toString);
          //vb.addInfo(vcfCodes.assess_ACMG_numGenes_CANON , geneSetCanon.size.toString);
          
          vb.addInfo(vcfCodes.assess_geneList , geneSet.padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_geneTxList , geneSet.map((g)  => { geneToTx(g).toVector.sorted.mkString("|") }).padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_refSeqKnown, geneSet.map( g => g + ":" + geneToTx(g).count(tx => isRefSeq(tx))).padTo(1,".").mkString(",") );
          
          if(geneSet.size > 1){
            problemList = problemList + "MG|MultipleGene";
          }
          
          val numVariantTypes = Seq[Boolean](
            (combo.exists{case (g,tx,info,c,i) => {
              info.severityType == "LLOF" || info.severityType == "PLOF"  || info.subType == "START-LOSS" || info.subType.startsWith("splice")
            }}),
            (combo.exists{case (g,tx,info,c,i) => {
              info.subType == "insAA" || info.subType == "delAA" || info.subType == "indelAA"
            }}),
            (combo.exists{case (g,tx,info,c,i) => {
              info.subType == "swapAA"
            }}),
            (combo.exists{case (g,tx,info,c,i) => {
              info.subType == "cds-synon"
            }})
          ).count(b => b);
          
          if(numVariantTypes > 1){
            problemList = problemList + "MV|MultipleVariantTypes";
          }
          
          //***************************Other major subsets:
          typeSubsets.foreach{ case (tssName,tssDesc,tssInfoFunc,tssInfoLines) => {
            val tssTX = combo.filter{case (g,tx,info,c,i) => {
              tssInfoFunc(info)
            }}
            val tssGenes      = tssTX.map{ case (g,tx,info,c,i) => g }.toList.distinct.sorted
            val tssGenesCanon = tssTX.withFilter{case (g,tx,info,c,i) => isRefSeq(tx)}.map{ case (g,tx,info,c,i) => g }.toList.distinct.sorted
            val tssTxList = tssTX.map{ case (g,tx,info,c,i) => tx }.toList.distinct.sorted
            
          //new SVcfCompoundHeaderLine("INFO","SWH_ANNO_geneList_"+typeSubsetName,Number=".",Type="String",desc="No description yet. "+typeSubsetDesc).addWalker(this),
          //new SVcfCompoundHeaderLine("INFO","SWH_ANNO_geneList_"+typeSubsetName+"_CANON",Number=".",Type="String",desc="No description yet. "+typeSubsetDesc).addWalker(this),
          //new SVcfCompoundHeaderLine("INFO","SWH_ANNO_txList_"+typeSubsetName,Number=".",Type="String",desc="No description yet. "+typeSubsetDesc).addWalker(this)
            
            vb.addInfo("SWH_ANNO_geneList_"+tssName,           tssGenes.padTo(1,".").mkString(","));
            vb.addInfo("SWH_ANNO_geneList_"+tssName+"_CANON",  tssGenesCanon.padTo(1,".").mkString(","));
            vb.addInfo("SWH_ANNO_txList_"  +tssName,           tssTxList.padTo(1,".").mkString(","));
          }}
          
          //***************************LOF:
          val LofTX = combo.filter{case (g,tx,info,c,i) => {
            info.severityType == "LLOF" || info.severityType == "PLOF"
          }}
          
          val LofGenes = LofTX.map{case (g,tx,info,c,i) => {g}}.toSet.toList.sorted;
    
          //vb.addInfo(vcfCodes.assess_LofGenes , LofGenes.padTo(1,".").mkString(","));
          val LofTxList = LofTX.map{case (g,tx,info,c,i) => tx};
          //vb.addInfo(vcfCodes.assess_LofTX , LofTxList.padTo(1,".").mkString(","))
          
          val geneLofTx = geneSet.map(g => {
            val txSet = geneToTx(g);
            val txLofCt = LofTxList.count{tx => { txSet.contains(tx) }};
            (g,txLofCt, txSet.size)
          });
          vb.addInfo(vcfCodes.assess_geneLofTxRatio , geneLofTx.map{case (g,a,b) => g+":"+a+"/"+b}.padTo(1,".").mkString(","))
          
          /*val refSeqLof = geneSet.filter{g => {
            val txSet = geneToTx(g);
            val txLof = LofTxList.filter(tx => {txSet.contains(tx)});
            txLof.exists(tx => {
              isRefSeq(tx);
            })
          }}
          vb.addInfo(vcfCodes.assess_LofGenes+"_CANON" , refSeqLof.padTo(1,".").mkString(","));*/
          
          //***************************Mis:
          val MisTX = combo.filter{case (g,tx,info,c,i) => {
            info.subType == "swapAA";
          }}
          val MisGenes = MisTX.map{case (g,tx,info,c,i) => {
            g
          }}.toSet.toList.sorted;
          
          val MisTxList = MisTX.map{case (g,tx,info,c,i) => tx};
          //vb.addInfo(vcfCodes.assess_MisGenes , MisGenes.padTo(1,".").mkString(","));
          //vb.addInfo(vcfCodes.assess_MisTX , MisTxList.padTo(1,".").mkString(","));
          
          val geneMisTx = geneSet.map(g => {
            val txSet = geneToTx(g);
            val txMisCt = MisTxList.count{tx => { txSet.contains(tx) }};
            (g,txMisCt, txSet.size)
          });
          vb.addInfo(vcfCodes.assess_geneMisTxRatio , geneMisTx.map{case (g,a,b) => g+":"+a+"/"+b}.padTo(1,".").mkString(","))
          
          /*val refSeqMis = geneSet.filter{g => {
            val txSet = geneToTx(g);
            val txMis = MisTxList.filter(tx => {txSet.contains(tx)});
            txMis.exists(tx => {
              isRefSeq(tx);
            })
          }}
          vb.addInfo(vcfCodes.assess_MisGenes+"_CANON", refSeqMis.padTo(1,".").mkString(","));*/
          
          //Any Coding:
          /*
          val codingTX = combo.filter{case (g,tx,info,c,i) => {
            info.severityType == "PLOF" || info.severityType == "LLOF" || info.severityType == "NONSYNON" || info.severityType == "SYNON"
          }}
          val codingGenes = codingTX.map{case (g,tx,info,c,i) => {
            g
          }}.toSet.toList.sorted;
          val codingTxList = codingTX.map{case (g,tx,info,c,i) => tx};
          vb.addInfo(vcfCodes.assess_CodingGenes , codingGenes.padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_CodingTX , codingTxList.padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_CodingGenes+"_CANON" , codingTX.withFilter{case (g,tx,info,c,i) => isRefSeq(tx)}.map(_._1).toSet.toVector.sorted.padTo(1,".").mkString(","));
          */
          //   ***************************Any NS:
          /*
          val nsTX = combo.filter{case (g,tx,info,c,i) => {
            info.severityType == "LLOF" || info.severityType == "PLOF" || info.severityType == "NONSYNON"
          }}
          val nsGenes = nsTX.map{case (g,tx,info,c,i) => {
            g
          }}.toSet.toList.sorted;
          val nsTxList = nsTX.map{case (g,tx,info,c,i) => tx};
          vb.addInfo(vcfCodes.assess_NonsynonGenes , nsGenes.padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_NonsynonTX ,    nsTxList.padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_NonsynonGenes+"_CANON" , nsTX.withFilter{case (g,tx,info,c,i) => isRefSeq(tx)}.map(_._1).toSet.toVector.sorted.padTo(1,".").mkString(","));          
          */
          
          vb.addInfo(vcfCodes.assess_WARNFLAG, if(problemList.isEmpty) "0" else "1" );
          vb.addInfo(vcfCodes.assess_WARNINGS, problemList.toVector.sorted.padTo(1,".").mkString(",") );
          
    }
    vb;
  }
  }
  
  
def getDbMappers(infile : String, chromList : Option[List[String]], vcfCodes : VCFAnnoCodes = VCFAnnoCodes(),
                 dbName : String,
                 idTag : String,
                 
                 keepInfo : Seq[String] = Seq[String](),
                 pathoExpression : Option[String] = None,
                 benExpression: Option[String] = None,
                 txSet : Option[Set[String]] = None,
                 //dbMeta : Seq[String] = Seq[String](),
                 soFar : Option[(scala.collection.mutable.Map[String,TXUtil.KnownVarHolder],scala.collection.mutable.Map[String,Set[internalUtils.TXUtil.pVariantInfo]])] = None
                ) : (scala.collection.mutable.Map[String,TXUtil.KnownVarHolder],scala.collection.mutable.Map[String,Set[internalUtils.TXUtil.pVariantInfo]]) = {
    //val hgmdPathogenicClassSet = hgmdPathogenicClasses.toSet
    val isPatho : (SVcfVariantLine => Boolean) = pathoExpression match {
      case Some(expr) => { 
            val parser : SFilterLogicParser[SVcfVariantLine] = internalUtils.VcfTool.sVcfFilterLogicParser;
            val filter : SFilterLogic[SVcfVariantLine] = parser.parseString(expr);
            (v : SVcfVariantLine) => filter.keep(v);
      }
      case None => {
        (v : SVcfVariantLine) => true;
      }
    }
    val isBenign : (SVcfVariantLine => Boolean) = benExpression match {
      case Some(expr) => {
            val parser : SFilterLogicParser[SVcfVariantLine] = internalUtils.VcfTool.sVcfFilterLogicParser;
            val filter : SFilterLogic[SVcfVariantLine] = parser.parseString(expr);
            (v : SVcfVariantLine) => filter.keep(v);
      }
      case None => {
        (v : SVcfVariantLine) => false;
      }
    }
    val keepTx : (String => Boolean) = txSet match {
      case Some(txs) => {
        (tx => txs.contains(tx))
      }
      case None => {
        (tx => true)
      }
    }
  
    val (variantInfo,out) = soFar.getOrElse((
        new scala.collection.mutable.AnyRefMap[String,internalUtils.TXUtil.KnownVarHolder](),
        new scala.collection.mutable.AnyRefMap[String,Set[internalUtils.TXUtil.pVariantInfo]](((k : String) => Set[internalUtils.TXUtil.pVariantInfo]()))      
    ));
    //gvariation
    //val gset = new scala.collection.mutable.AnyRefMap[String,TXUtil.KnownVariantInfoHolder]();
    //val variantInfo = new scala.collection.mutable.AnyRefMap[String,internalUtils.TXUtil.KnownVariantInfoHolder]();
    
    val (vcIter,vcfHeader) = VcfTool.getSVcfIterator(infile,chromList=chromList,numLinesRead=None);
    reportln("Starting database VCF read...","progress");
    for(v <- vcIter){
      val varid = v.chrom + ":" +v.getInfo(vcfCodes.vMutG_TAG);
      val dbVarId = v.getInfo(idTag);
      val patho = isPatho(v);
      val ben = isBenign(v);
      val kvh : KnownVarHolder = variantInfo.getOrElse(varid, 
           KnownVarHolder(varid,
                          Seq[String](),
                          Seq[String](),
                          new scala.collection.mutable.AnyRefMap[String,scala.collection.mutable.Map[String,String]](),
                          isPatho = Seq[Boolean](),
                          isBen = Seq[Boolean]()
               )
           );
      val metaMap = kvh.metaMap;
      val metadata = new scala.collection.mutable.AnyRefMap[String,String]();
      metaMap.update(dbName, metadata); 
      kvh.isPatho = kvh.isPatho :+ (patho);
      kvh.isBen = kvh.isBen :+ (ben);
      kvh.sourceList = kvh.sourceList :+ (dbName);
      kvh.idlist = kvh.idlist :+ (dbVarId);
      
      keepInfo.foreach{itag => {
        v.info.get(itag).getOrElse(None).foreach{ii => {
          metadata.update(itag,ii);
        }}
      }}
      val txList = v.getInfoList(vcfCodes.txList_TAG);
      
      variantInfo(varid) = kvh;
      
      if(txList.length > 0 && txList.exists{tx => keepTx(tx)}){
        notice("  Transcript info found for variant: \""+v.getSimpleVcfString()+"\":\n"+
               "      "+txList.mkString(",")+":"+v.info.get(vcfCodes.vMutINFO_TAG).getOrElse(None).filter( _ != "." ).map{ _.split("\\|").mkString(",") }.getOrElse("."),"dbMapLookup_TXFOUND",30);
        v.info.get(vcfCodes.vMutINFO_TAG).getOrElse(None).filter( _ != "." ).map{ _.split("\\|") }.foreach{ mutInfoString => {
          if(mutInfoString.length != txList.length){
             warning("SERIOUS WARNING: ERROR_TXLEN_AND_MUTPLEN_NOT_MATCHED","ERROR_TXLEN_AND_MUTPLEN_NOT_MATCHED",100);
          }
          notice("      mutInfoString EXISTS\n","dbMapLookup_TXFOUND_PART",30)
          txList.zip(mutInfoString).foreach{ case (tx,infoString) => {
            if(keepTx(tx)){
              out(tx) = ( out(tx) + (internalUtils.TXUtil.getPvarInfoFromString(infoString, ID = varid, dbInfo = Some(kvh))) )
            }
          }}
        }}
      } else if(txList.length > 0){
        notice("  No KEEP Transcript info found for variant: \""+v.getSimpleVcfString()+"\""+", INFOTAG("+vcfCodes.txList_TAG+")","dbMapLookup_NOKEEPTXFOUND",3);
      } else {
        notice("  No ANY Transcript info found for variant: \""+v.getSimpleVcfString()+"\""+", INFOTAG("+vcfCodes.txList_TAG+")","dbMapLookup_NOTXFOUND",3);
      }
    }
    
    def kvhStringDesc( kvh : KnownVarHolder) : String = {
      "["+kvh.idlist.mkString(",")+"],["+kvh.sourceList.mkString(",")+"],["+kvh.isPatho.map{b => if(b) "1" else "0"}.mkString(",")+"],["+kvh.isBen.map{b => if(b) "1" else "0"}.mkString(",")+"]"
    }
    
    variantInfo.take(10).zipWithIndex.foreach{ case ((varid,kvh),ii) => {
      notice("   VARINFO("+ii+")="+kvhStringDesc(kvh),
             "VariantInfoExample",30);
    }}
    
    out.take(10).zipWithIndex.foreach{ case ((txid,pVarSet),ii) => {
      notice("   TXID("+ii+")=["+txid+"]","varInfoTxId",30);
      pVarSet.take(2).zipWithIndex.foreach{ case (pvar,jj) => {
        notice("      pVarInfo["+ii+","+jj+"]="+pvar.pvar+"     "+kvhStringDesc(pvar.dbInfo.get),
               "pVarInfoExample",30)
      }}
    }}
    
    reportln("Finished database VCF read...","progress");

    
    return (variantInfo,out);
  }
  

  class AddDatabaseMatching(txToGeneFile : Option[String], refSeqFile : Option[String],
                            chromList : Option[List[String]], 
                            geneSet : Option[Set[String]],
                            dbdata : Seq[(String,String,String,Option[String],Option[String],Seq[String])],
                               //dbName,dbFile,idTag,pathoExpression,benExpression,metaDataTags
                            tagInfix : String = ""
                            
                            ) extends SVcfWalker {
    def walkerName : String = "AddDatabaseMatching"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("txToGeneFile", txToGeneFile.getOrElse(".")),
        ("refSeqFile", refSeqFile.getOrElse("."))
    );
    val vcfCodes : VCFAnnoCodes = VCFAnnoCodes()
    
    reportln("Reading geneToTX map... ["+stdUtils.getDateAndTimeString+"]","debug");

    val geneToTx : (String => Set[String]) = txToGeneFile match {
      case Some(f) => {
        val geneToTxMap = scala.collection.mutable.AnyRefMap[String,Set[String]]().withDefault(x => Set[String]());
        getLinesSmartUnzip(f).foreach(line => {
          val cells = line.split("\t");
          geneToTxMap(cells(1)) = geneToTxMap(cells(1)) + cells(0);
        })
        ((s : String) => geneToTxMap.getOrElse(s,Set[String]()));
      }
      case None => {
        ((s : String) => Set[String](s));
      }
    }
    val txSet = geneSet match {
      case Some(gs) => {
        Some(gs.flatMap{ g => geneToTx(g)})
      }
      case None => {
        None
      }
    }
    
    notice("geneSet.size="+geneSet.map{_.size}.getOrElse(-1)+". Examples:","GENESET_LENGTH",-1);
    geneSet.getOrElse(Set()).take(10).foreach{ tx => {
      notice("      "+tx,"GENEEX",-1);
    }}
    notice("txSet.size="+txSet.map{_.size}.getOrElse(-1)+". Examples:","TXSET_LENGTH",-1);
    txSet.getOrElse(Set()).take(10).foreach{ tx => {
      notice("      "+tx,"TXEX",-1);
    }}
    
    reportln("Reading dbVarMap,dbTxVarMap... ["+stdUtils.getDateAndTimeString+"]","debug");
    val dbList : Seq[String] = dbdata.map{ case (dbName,dbFile,idTag,pathoExpression,benExpression,metaDataTags) => dbName }.toSeq;
    val dbMeta : Seq[Seq[String]] = dbdata.map{ case (dbName,dbFile,idTag,pathoExpression,benExpression,metaDataTags) => metaDataTags }.toSeq;
    val (dbVarMap,dbTxVarMap) = dbdata.foldLeft[Option[(scala.collection.mutable.Map[String,TXUtil.KnownVarHolder],scala.collection.mutable.Map[String,Set[internalUtils.TXUtil.pVariantInfo]])]](None){ 
      case (soFar,(dbName,dbFile,idTag,pathoExpression,benExpression,metaDataTags)) => {
            Some(getDbMappers(infile =dbFile, chromList =chromList, vcfCodes = vcfCodes,
                 dbName = dbName,
                 keepInfo = metaDataTags,
                 idTag = idTag,
                 pathoExpression = pathoExpression,
                 benExpression = benExpression,
                 txSet = txSet,
                 soFar = soFar
                ))
    }}.get
    
    reportln("###    dbVarMap.size = "+dbVarMap.size,"note")
    reportln("###    dbTxVarMap.size = "+dbTxVarMap.size,"note")
    

    reportln("Reading txToGene map... ["+stdUtils.getDateAndTimeString+"]","debug");
    val txToGene : (String => String) = txToGeneFile match {
      case Some(f) => {
        val txToGeneMap = getLinesSmartUnzip(f).map(line => {
          val cells = line.split("\t");
          (cells(0),cells(1));
        }).toMap
        ((s : String) => txToGeneMap.getOrElse(s,s));
      }
      case None => {
        ((s : String) => s);
      }
    }

    reportln("Reading refSeq map... ["+stdUtils.getDateAndTimeString+"]","debug");
    
    val isCanon : (String => Boolean) = refSeqFile match {
      case Some(f) => {
        val lines = getLinesSmartUnzip(f);
        val table = getTableFromLines(lines,colNames = Seq("transcript"), errorName = "File "+f);
        var refSeqSet : Set[String] = table.map(tableCells => {
          val tx = tableCells(0);
          tx
        }).toSet
        reportln("   found: "+refSeqSet.size+" RefSeq transcripts.","debug");
        ((s : String) => {
          refSeqSet.contains(s);
        })
      }
      case None => {
        ((s : String) => {
          false;
        })
      }
    }
    
    def isLOF(info : pVariantInfo) : Boolean = {
      info.severityType == "LLOF" || info.severityType == "PLOF" || info.pType == "START-LOSS" || info.subType.startsWith("splice")
    }
    
    val dbIterationSet = Seq(
           ("","","variants from any DB",((kvh : KnownVarHolder) => true),Seq[String]()),
           ("_PTH","","variants with any pathogenic reports from any DB",((info : KnownVarHolder) => info.isPatho.exists{x => x}),Seq[String]()),
           ("_BEN","","variants with any pathogenic reports from any DB",((info : KnownVarHolder) => info.isBen.exists{x => x}),Seq[String]())
        ) ++ dbList.zip(dbMeta).flatMap{ case (dbName,meta) => {
          Seq(
           ("_"+dbName,dbName,"variants from any "+dbName,((info : KnownVarHolder) => info.sourceList.contains(dbName)),meta),
           ("_"+dbName+"_PTH",dbName,"variants with any pathogenic reports from "+dbName,((info : KnownVarHolder) => info.isPatho.zip(info.sourceList).exists{ case (p,src) => src == dbName && p}),meta),
           ("_"+dbName+"_BEN",dbName,"variants with any pathogenic reports from "+dbName,((info : KnownVarHolder) => info.isBen.zip(info.sourceList).exists{ case (p,src) => src == dbName && p}),meta)
          );
        }}
    val txSetIterationSet = Seq(
           ("_ALLTX"," (considering all valid coding transcripts)",((tx : String) => true)),
           ("_CANON"," (considering canonical transcripts only)",((tx : String) => isCanon(tx)))
        );
    val tpre = vcfCodes.assess_varInfoPrefix+tagInfix;
    
    val nonSynonSevSet = Set[String]("LLOF","PLOF","NONSYNON");
    val codingSevSet = Set[String]("LLOF","PLOF","NONSYNON","SYNON");
    
                  //tpre+dbInfix+txSetInfix+"_ID_aaMatch"
                  //tpre+dbInfix+txSetInfix+"_CT_aaMatch"
                  //tpre+dbInfix+txSetInfix+"_ID_aaMatch_diffBP"
                  //tpre+dbInfix+txSetInfix+"_CT_aaMatch_diffBP"
                  //tpre+dbInfix+txSetInfix+"_ID_sameCodon"
                  //tpre+dbInfix+txSetInfix+"_CT_sameCodon"
                  //tpre+dbInfix+txSetInfix+"_ID_sameCodon_diffAA"
                  //tpre+dbInfix+txSetInfix+"_CT_sameCodon_diffAA"
                  //tpre+dbInfix+txSetInfix+"_ID_downstreamLOF"
                  //tpre+dbInfix+txSetInfix+"_CT_downstreamLOF"
                  //tpre+dbInfix+txSetInfix+"_ID_downstreamLOF_sameType"
                  //tpre+dbInfix+txSetInfix+"_CT_downstreamLOF_sameType"
    
    val matchGroupingList = Seq("aaMatch","aaMatch_diffBP","sameCodon","sameCodon_diffAA","downstreamLOF","downstreamLOF_sameType")
    
    val infoList = dbIterationSet.flatMap{ case (dbInfix,dbName,dbDesc,dbFunc,meta) => {
      Seq(
        (new SVcfCompoundHeaderLine("INFO",ID=tpre+dbInfix+"_bpMatch_ID",Number=".",Type="String",desc="exact base-pair match "+dbDesc)).addWalker(this),
        (new SVcfCompoundHeaderLine("INFO",ID=tpre+dbInfix+"_bpMatch_CT",Number="1",Type="Integer",desc="Num exact base-pair match "+dbDesc)).addWalker(this)
      ) ++ txSetIterationSet.flatMap{ case (txSetInfix,txSetDesc,txSetFunc) => {
        Seq(
          (new SVcfCompoundHeaderLine("INFO",ID=tpre+dbInfix+txSetInfix+"_aaMatch_CT",Number="1",Type="Integer",desc="Num amino-acid-matching "+dbDesc)).addWalker(this),
          (new SVcfCompoundHeaderLine("INFO",ID=tpre+dbInfix+txSetInfix+"_aaMatch_ID",Number=".",Type="String",desc="Amino acid match "+dbDesc)).addWalker(this),
          (new SVcfCompoundHeaderLine("INFO",ID=tpre+dbInfix+txSetInfix+"_aaMatch_diffBP_CT",Number=".",Type="String",desc="Different bp but same amino acid match "+dbDesc)).addWalker(this),
          (new SVcfCompoundHeaderLine("INFO",ID=tpre+dbInfix+txSetInfix+"_aaMatch_diffBP_ID",Number="1",Type="Integer",desc="Num matching "+dbDesc)).addWalker(this),
          (new SVcfCompoundHeaderLine("INFO",ID=tpre+dbInfix+txSetInfix+"_sameCodon_CT",Number=".",Type="String",desc="Same codon pos match "+dbDesc)).addWalker(this),
          (new SVcfCompoundHeaderLine("INFO",ID=tpre+dbInfix+txSetInfix+"_sameCodon_ID",Number="1",Type="Integer",desc="Num same codon pos match "+dbDesc)).addWalker(this),
          (new SVcfCompoundHeaderLine("INFO",ID=tpre+dbInfix+txSetInfix+"_sameCodon_diffAA_CT",Number=".",Type="String",desc="Same codon pos but different aa "+dbDesc)).addWalker(this),
          (new SVcfCompoundHeaderLine("INFO",ID=tpre+dbInfix+txSetInfix+"_sameCodon_diffAA_ID",Number="1",Type="Integer",desc="Num same codon pos but different aa"+dbDesc)).addWalker(this),
          (new SVcfCompoundHeaderLine("INFO",ID=tpre+dbInfix+txSetInfix+"_downstreamLOF_CT",Number=".",Type="String",desc="Downstream LOF "+dbDesc)).addWalker(this),
          (new SVcfCompoundHeaderLine("INFO",ID=tpre+dbInfix+txSetInfix+"_downstreamLOF_ID",Number="1",Type="Integer",desc="Num downstream LOF "+dbDesc)).addWalker(this),
          (new SVcfCompoundHeaderLine("INFO",ID=tpre+dbInfix+txSetInfix+"_downstreamLOF_sameType_CT",Number=".",Type="String",desc="Downstream same-type LOF "+dbDesc)).addWalker(this),
          (new SVcfCompoundHeaderLine("INFO",ID=tpre+dbInfix+txSetInfix+"_downstreamLOF_sameType_ID",Number="1",Type="Integer",desc="Num downstream same-type LOF"+dbDesc)).addWalker(this)
          
        ) ++ matchGroupingList.flatMap{ mg => {
          meta.map{ mm => {
            (new SVcfCompoundHeaderLine("INFO",ID=tpre+dbInfix+txSetInfix+"_aaMatch_"+mm,Number="1",Type="Integer",desc="Num amino-acid-matching "+dbDesc)).addWalker(this)
          }}
        }}
      }}
    }}
    
    
    
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader;
      outHeader.addWalk(this);
      var exampleDebugCt = 0;
      val exampleDebugLimit = 20;
      
      infoList.foreach{ infoLine => {
        outHeader.addInfoLine(infoLine);
      }}


              def getId(kvr : KnownVarHolder, dbName : String) : Option[String] = {
                 if(dbName == ""){
                   Some( kvr.idlist.zip(kvr.sourceList).map{case (id,src) => { src+":"+id }}.mkString("|") )
                 } else {
                   val dbIdx = kvr.sourceList.indexOf(dbName);
                   if(dbIdx != -1){
                     Some(kvr.idlist(dbIdx));
                   } else {
                     None
                   }
                 }
              }
              def getInfo(kvr : KnownVarHolder, dbName : String) : Option[(String,Boolean,Boolean)] = {
                 if(dbName == ""){
                   val ii = kvr.idlist.zip(kvr.isPatho).zip(kvr.isBen).map{ case ((id,p),b) => {
                     (id,p,b)
                   }}
                   val pFinal = ii.exists{ case (id,p,b) => p } && (! ii.exists{ case (id,p,b) => b})
                   val bFinal = ii.exists{ case (id,p,b) => b } && (! ii.exists{ case (id,p,b) => p})
                   Some((kvr.idlist.zip(kvr.sourceList).map{case (id,src) => { src+":"+id }}.mkString("|"),pFinal,bFinal))
                 } else {
                   val dbIdx = kvr.sourceList.indexOf(dbName);
                   if(dbIdx != -1){
                     Some((kvr.idlist(dbIdx),kvr.isPatho(dbIdx),kvr.isBen(dbIdx)));
                   } else {
                     None
                   }
                 }
              }
              def addMetaData(kvhSeq : Seq[KnownVarHolder], dbName : String, dbInfix : String, meta : Seq[String], vb : SVcfOutputVariantLine, matchType : String){
                      if(dbName != "" && meta.length > 0){
                        meta.foreach{ md => {
                          val iiSeq = kvhSeq.map{ kvh => {
                            kvh.metaMap.get(dbName).flatMap{ dd => {
                              dd.get(md)
                            }}.getOrElse(".")
                          }}
                          vb.addInfo(tpre+dbInfix+"_"+matchType+"_"+md, iiSeq.mkString(","));
                        }}
                      }
              }
              
              
    def kvhStringDesc( kvh : KnownVarHolder) : String = {
      "["+kvh.idlist.mkString(",")+"],["+kvh.sourceList.mkString(",")+"],["+kvh.isPatho.map{b => if(b) "1" else "0"}.mkString(",")+"],["+kvh.isBen.map{b => if(b) "1" else "0"}.mkString(",")+"]"
    }
      
      (vcMap(vcIter){ v => {
        val vb = v.getOutputLine;
        
          var problemList = Set[String]();
              
          val chrom = v.chrom
          val pos = v.pos
          
          val refAlle = v.ref
          val altAlleles = v.alt.zipWithIndex.filter{ case (alt,altIdx) => alt != VcfTool.UNKNOWN_ALT_TAG_STRING };
          
          val txList = v.getInfoList(vcfCodes.txList_TAG)
          
          if(txList.filter(tx => tx != ".").length > 0){
            notice("tx found for variant","TX_FOUND",5);
            exampleDebugCt = exampleDebugCt + 1;
            if(exampleDebugCt < exampleDebugLimit){
              reportln("example Tx: \""+txList.filter(tx => tx != ".").mkString("\",\"")+"\"","note")
            }
            
            val geneList = txList.map(x => txToGene(x));
              val varid = v.chrom + ":" + v.getInfo(vcfCodes.vMutG_TAG)
              //val vTypes = v.info.get(vcfCodes.vType_TAG).getOrElse(None).filter(_ != ".").map{ _.split("\\|") }
              //val vLVL       = v.info.get(vcfCodes.vMutLVL_TAG).getOrElse(None).filter(_ != ".").map{ _.split("\\|") }
              val vMutP = v.info.get(vcfCodes.vMutP_TAG).getOrElse(None).filter(_ != ".").map{ _.split("\\|") }
              val vMutC = v.info.get(vcfCodes.vMutC_TAG).getOrElse(None).filter(_ != ".").map{ _.split("\\|") }
              val mutInfo = v.info.get(vcfCodes.vMutINFO_TAG).getOrElse(None).filter(_ != ".").map{_.split("\\|")}.map{ mutInfoString => {
                 val mutInf : Array[pVariantInfo] = mutInfoString.map{ mis => {
                   internalUtils.TXUtil.getPvarInfoFromString(mis, ID = varid);
                 }}
                 mutInf
              }}.getOrElse( Array[pVariantInfo]() )
              
              val variantIV = commonSeqUtils.GenomicInterval(chrom,'.', start = pos - 1, end = pos + math.max(v.ref.length,1));
              if(altAlleles.length > 1){
                error("FATAL ERROR: Cannot deal with multiallelic variants! Split variants to single-allelic!");
              }
              val (alt,altIdx) = altAlleles.head;
              val combo = txList.zip(mutInfo).zipWithIndex.map{ case ((tx,info),i) => (txToGene(tx),tx,info,i) }.filter{ case (g,tx,info,i) => {
                nonSynonSevSet.contains(info.severityType);
              }}
              combo.headOption.foreach{ case (g,tx,info,i) => {
                notice("Found nonNull Combo: "+g+","+tx+","+info.toString(),"COMBO_EXAMPLE",5);
              }}
              
              val canonCombo = combo.filter{ case (g,tx,info,i) => isCanon(tx) }
              val geneSet = geneList.distinct.toVector.sorted
              val geneSetCanon = canonCombo.map{ case (g,tx,info,i) => g}.distinct.toVector.sorted;
          
              val exactMatch = dbVarMap.get(varid)
              
              val dbDownstream = combo.map{ case (g,tx,info,i) => {
                (g,tx,info,dbTxVarMap(tx).filter{ p => {
                  p.start >= info.start && nonSynonSevSet.contains(p.severityType)
                }})
              }}
                  dbDownstream.filter{ case (g,tx,info,varset) => varset.nonEmpty}.headOption.foreach{ case (g,tx,info,varset) => {
                    notice("Found downstream variants: ["+g+","+tx+","+info.ID+"]:"+varset.take(3).map{ vv => vv.ID+"/"+vv.cType+"/"+vv.severityType+"/"+vv.pType+"/"+vv.subType+"/"+vv.pvar}.mkString(","),"DS_EXAMPLE",5);
                  }}
              
              val dbCodon = dbDownstream.flatMap{ case (g,tx,info,varset) => {
                val xx = varset.filter{ p => {
                  p.start == info.start
                }}
                if(xx.nonEmpty){
                  Some((g,tx,info,xx));
                } else {
                  None
                }
              }}
              val dbCodonStrict = dbCodon.flatMap{ case (g,tx,info,varset) => {
                val xx = varset.filter{ p => {
                  p.start == info.start && p.end == info.end && p.severityType == "NONSYNON" && p.cType == info.cType
                }}
                if(xx.nonEmpty){
                  Some((g,tx,info,xx));
                } else {
                  None
                }
              }}
                  dbCodon.filter{ case (g,tx,info,varset) => varset.nonEmpty}.headOption.foreach{ case (g,tx,info,varset) => {
                    notice("Found downstream variants: ["+g+","+tx+","+info.ID+"]:"+varset.take(3).map{ vv => vv.ID+"/"+vv.cType+"/"+vv.severityType+"/"+vv.pType+"/"+vv.subType+"/"+vv.pvar}.mkString(","),"CODON_EXAMPLE",5);
                  }}
              
              
              val dbAmino = dbCodon.flatMap{ case (g,tx,info,varset) => {
                val xx = varset.filter{ p => {
                  (
                      info.subType.startsWith("fs") && p.subType.startsWith("fs")
                  ) || (
                      info.pType == "STOP-LOSS" && p.pType == "STOP-LOSS"
                  ) || (
                      info.pType == "START-LOSS" && p.pType == "START-LOSS"
                  ) || (
                      info.pType == "STOP-GAIN" && p.pType == "STOP-GAIN"
                  ) || (
                      info.severityType == "NONSYNON" && info.pvar == p.pvar
                  )
                }}
                if(xx.nonEmpty){
                  Some((g,tx,info,xx))
                } else {
                  None
                }
              }}
                  dbAmino.filter{ case (g,tx,info,varset) => varset.nonEmpty}.headOption.foreach{ case (g,tx,info,varset) => {
                    notice("Found AMINO variants: ["+g+","+tx+","+info.ID+"]:"+varset.take(3).map{ vv => vv.ID+"/"+vv.cType+"/"+vv.severityType+"/"+vv.pType+"/"+vv.subType+"/"+vv.pvar+"/"+vv.dbInfo.map{kvh => kvhStringDesc(kvh)}.getOrElse(".")}.mkString(","),"AMINO_EXAMPLE",5);
                  }}
              
              //tpre+txSetInfix+dbInfix+"_CT_aaMatch"
              

                  
              dbIterationSet.foreach{ case (dbInfix, dbName,dbSetDesc, dbSetFunc, meta) => {
                val exm = exactMatch.flatMap{ em => {
                  if(dbSetFunc(em)){
                    vb.addInfo(tpre+dbInfix+"_bpMatch_CT","1");
                    //if(dbName != ""){
                      val id = getId(em, dbName);
                      vb.addInfo(tpre+dbInfix+"_bpMatch_ID",id.getOrElse("."));
                    //}
                    //addMetaData(kvh : KnownVariantHolder, dbName : String, meta : Seq[String], vb : SVcfVariantOutputLine)
                      addMetaData(Seq(em),dbName,dbInfix,meta,vb,"bpMatch");
                    Some(em);
                  } else {
                    vb.addInfo(tpre+dbInfix+"_bpMatch_CT","0");
                    None
                  }
                }}.toSet
                if(exm.size > 0) notice("Found exact variant: "+exm.map{_.id}.mkString(","),"EXACT_VAR_TEST",5)
                txSetIterationSet.foreach{ case (txSetInfix,txSetDesc,txSetFunc) => {
                  val aam = dbAmino.withFilter{ case (g,tx,info,varset) => {
                    txSetFunc(tx)
                  }}.flatMap{ case (g,tx,info,varset) => {
                    varset.map{mInfo => mInfo.dbInfo.get}.filter{kvh => dbSetFunc(kvh)}
                  }}.toSeq
                  val aaIds = aam.flatMap{ kvh => {
                    getId(kvh, dbName)
                  }}
                  val aamDiff = (aam.toSet -- exm.toSet).toSeq
                  val aamDiffIds = aamDiff.flatMap{getId(_,dbName)}
                  
                  //if(dbName != "") 
                  vb.addInfo(tpre+dbInfix+txSetInfix+"_aaMatch_ID",aaIds.toVector.sorted.padTo(1,".").mkString(","));
                  vb.addInfo(tpre+dbInfix+txSetInfix+"_aaMatch_CT",""+aam.size);
                  addMetaData(aam,dbName,dbInfix,meta,vb,"aaMatch");
                  //addMetaData(em,dbName,meta,vb,"bpMatch");
                  //if(dbName != "") 
                  vb.addInfo(tpre+dbInfix+txSetInfix+"_aaMatch_diffBP_ID",""+aamDiffIds.toVector.sorted.padTo(1,".").mkString(","));
                  vb.addInfo(tpre+dbInfix+txSetInfix+"_aaMatch_diffBP_CT",""+aamDiff.size);
                  addMetaData(aamDiff,dbName,dbInfix,meta,vb,"aaMatch_diffBP");
                  
                  val cdm = dbCodonStrict.withFilter{ case (g,tx,info,varset) => {
                    txSetFunc(tx)
                  }}.flatMap{ case (g,tx,info,varset) => {
                    varset.map{mInfo => mInfo.dbInfo.get}.filter{kvh => dbSetFunc(kvh)}
                  }}.toSeq
                  val cdIds = cdm.flatMap{ kvh => {
                    getId(kvh, dbName)
                  }}
                  val cdmDiff = (cdm.toSet -- aam.toSet -- exm).toSeq
                  val cdmDiffIds = cdmDiff.flatMap{getId(_,dbName)}
                  
                  //if(dbName != "") 
                  vb.addInfo(tpre+dbInfix+txSetInfix+"_sameCodon_ID",cdIds.toVector.sorted.padTo(1,".").mkString(","));
                  vb.addInfo(tpre+dbInfix+txSetInfix+"_sameCodon_CT",""+cdm.size);
                  addMetaData(cdm,dbName,dbInfix,meta,vb,"sameCodon");
                  //if(dbName != "") 
                  vb.addInfo(tpre+dbInfix+txSetInfix+"_sameCodon_diffAA_ID",""+cdmDiffIds.toVector.sorted.padTo(1,".").mkString(","));
                  vb.addInfo(tpre+dbInfix+txSetInfix+"_sameCodon_diffAA_CT",""+cdmDiff.size);
                  addMetaData(cdmDiff,dbName,dbInfix,meta,vb,"sameCodon_diffAA");
                  
                  val downlof = dbDownstream.flatMap{ case (g,tx,info,varset) => {
                    if(isLOF(info) && txSetFunc(tx)){
                      varset.withFilter{mInfo => {
                        isLOF(mInfo) && dbSetFunc(mInfo.dbInfo.get)
                      }}.map{mInfo => mInfo.dbInfo.get}.toSeq
                    } else {
                      Seq[KnownVarHolder]()
                    }
                  }}
                  //if(dbName != "") 
                  vb.addInfo(tpre+dbInfix+txSetInfix+"_downstreamLOF_ID",downlof.flatMap{getId(_, dbName)}.toVector.sorted.padTo(1,".").take(10).mkString(","));
                  vb.addInfo(tpre+dbInfix+txSetInfix+"_downstreamLOF_CT",""+downlof.size);
                  addMetaData(downlof,dbName,dbInfix,meta,vb,"downStreamLOF");
                  val downlofSameType = dbDownstream.flatMap{ case (g,tx,info,varset) => {
                    if(isLOF(info) && txSetFunc(tx)){
                      varset.withFilter{mInfo => {
                        isLOF(mInfo) && dbSetFunc(mInfo.dbInfo.get) && mInfo.pType == info.pType
                      }}.map{mInfo => mInfo.dbInfo.get}.toSeq
                    } else {
                      Seq[KnownVarHolder]()
                    }
                  }}
                  //if(dbName != "") 
                  vb.addInfo(tpre+dbInfix+txSetInfix+"_downstreamLOF_sameType_ID",downlofSameType.flatMap{getId(_, dbName)}.toVector.sorted.padTo(1,".").take(10).mkString(","));
                  vb.addInfo(tpre+dbInfix+txSetInfix+"_downstreamLOF_sameType_CT",""+downlofSameType.size);
                  addMetaData(downlofSameType,dbName,dbInfix,meta,vb,"downStreamLOF_sameType");
                }}
                
              }}

              
              /*
              val dbDownstreamLof = dbDownstream.map{ case (g,tx,mInfo,varSet) => (g,tx,mInfo,varSet.filter{ info => {
                isLOF(mInfo) && isLOF(info) && info.isPatho
              }})}
              val dbCodonLof = dbCodon.map{ case (g,tx,mInfo,varSet) => (g,tx,mInfo,varSet.filter{ info => {
                isLOF(mInfo) && isLOF(info) && info.isPatho
              }})}
              
              val geneHasDownstreamLOF = dbDownstreamLof.withFilter{ case (g,tx,mInfo,varSet) => varSet.nonEmpty }.map{ case (g,tx,mInfo,varSet) => g }.toSet;
              val txHasDownstreamLOF = dbDownstreamLof.withFilter{ case (g,tx,mInfo,varSet) => varSet.nonEmpty }.map{ case (g,tx,mInfo,varSet) => tx }.toSet;
              val geneHasDownstreamLOF_CANON = dbDownstreamLof.withFilter{ case (g,tx,mInfo,varSet) => isCanon(tx) && varSet.nonEmpty }.map{ case (g,tx,mInfo,varSet) => g }.toSet;
              val txHasDownstreamLOF_CANON = dbDownstreamLof.withFilter{ case (g,tx,mInfo,varSet) => isCanon(tx) && varSet.nonEmpty }.map{ case (g,tx,mInfo,varSet) => tx }.toSet;
              
              def seq2string(ss : Iterable[String]) : String = {
                ss.toSeq.sorted.padTo(1,".").mkString(",");
              }
              
              val startLoss = combo.withFilter{ case (g,tx,info,c,i) => {
                info.pType == "START-LOSS" 
              }}.map{ case (g,tx,info,c,i) => {
                (g,tx,info,dbTxVarMap(tx).filter{ p => {
                  p.pType == "START-LOSS" && p.isPatho;
                }})
              }}
              
              val stopLoss = combo.withFilter{ case (g,tx,info,c,i) => {
                info.pType == "STOP-LOSS" 
              }}.map{ case (g,tx,info,c,i) => {
                (g,tx,info,dbTxVarMap(tx).filter{ p => {
                  p.pType == "STOP-LOSS" && p.isPatho;
                }})
              }}
              
              val geneHasStartLoss = startLoss.flatMap{ case (g,tx,info,varset) => if(varset.isEmpty) None else Some(g) }.toSet
              val txHasStartLoss = startLoss.flatMap{ case (g,tx,info,varset) =>  if(varset.isEmpty) None else Some(g)  }.toSet
              val geneHasStartLoss_CANON = startLoss.withFilter{ case (g,tx,info,varset) => isCanon(tx) && varset.nonEmpty}.map{ case (g,tx,info,varset) => g }.toSet
              val txHasStartLoss_CANON = startLoss.withFilter{ case (g,tx,info,varset) => isCanon(tx) && varset.nonEmpty}.map{ case (g,tx,info,varset) => tx }.toSet
              
              val geneHasStopLoss = stopLoss.map{ case (g,tx,info,varset) => g }.toSet
              val txHasStopLoss = stopLoss.map{ case (g,tx,info,varset) => tx }.toSet
              val geneHasStopLoss_CANON = stopLoss.withFilter{ case (g,tx,info,varset) => isCanon(tx)}.map{ case (g,tx,info,varset) => g }.toSet
              val txHasStopLoss_CANON = stopLoss.withFilter{ case (g,tx,info,varset) => isCanon(tx)}.map{ case (g,tx,info,varset) => tx }.toSet
              */
              /*
              vb.addInfo(vcfCodes.assess_forBT_codonLOF,  getVarIds(dbCodonLof));
              vb.addInfo(vcfCodes.assess_forBT_codonLOF_CANON, getVarIds(dbCodonLof.filter{case (g,tx,info,varset) => isCanon(tx)}) );
              vb.addInfo(vcfCodes.assess_forBT_downstreamLOF,     getVarIds(dbDownstreamLof));
              vb.addInfo(vcfCodes.assess_forBT_downstreamLOF_CANON, getVarIds(dbDownstreamLof.filter{case (g,tx,info,varset) => isCanon(tx)}));

              vb.addInfo(vcfCodes.assess_forBT_geneHasDownstreamLOF,        seq2string(geneHasDownstreamLOF));
              vb.addInfo(vcfCodes.assess_forBT_geneHasDownstreamLOF_CANON,  seq2string(geneHasDownstreamLOF_CANON));
              
              vb.addInfo(vcfCodes.assess_forBT_geneHasStartLoss,            seq2string(geneHasStartLoss));
              vb.addInfo(vcfCodes.assess_forBT_geneHasStartLoss_CANON,      seq2string(geneHasStartLoss_CANON));
              vb.addInfo(vcfCodes.assess_forBT_geneHasStopLoss,             seq2string(geneHasStopLoss));
              vb.addInfo(vcfCodes.assess_forBT_geneHasStopLoss_CANON,       seq2string(geneHasStopLoss_CANON));
              */
              
              /*
              dbIterationSet.foreach{ case (dbInfix, dbName,dbSetDesc, dbSetFunc) => {
                exactMatch.find{m => dbSetFunc(m)}.foreach{ em => {
                  
                }}
                

              }}*/
              
              
              /*
    val dbIterationSet = Seq(
           ("","variants from any DB",((info : pVariantInfo) => true)),
           ("_PTH","variants with any pathogenic reports from any DB",((info : pVariantInfo) => info.dbInfo.get.isPatho.exists{x => x})),
           ("_BEN","variants with any pathogenic reports from any DB",((info : pVariantInfo) => info.dbInfo.get.isBenign.exists{x => x}))
        );
    val txSetIterationSet = Seq(
           ("","",((tx : String) => true)),
           ("_CANON"," (considering canonical transcripts only)",((tx : String) => isCanon(tx)))
        );
    

              vb.addInfo(vcfCodes.assess_pathoExactMatchRS, getOrFunc(varInfo, ".")(vi => vi.pathoIdString()));
              vb.addInfo(vcfCodes.assess_pathoAminoMatchRS, ps1RS.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
              vb.addInfo(vcfCodes.assess_pathoNearMatchRS,  pm5RS.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
              vb.addInfo(vcfCodes.assess_pathoAminoMatchRS_CANON, ps1RS_canon.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
              vb.addInfo(vcfCodes.assess_pathoNearMatchRS_CANON,  pm5RS_canon.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
              


              vb.addInfo(vcfCodes.assess_forBT_codonLOF,      codonPathoLOFRS.toList.sorted.map{_.toString}.padTo(1,".").mkString(","));
              vb.addInfo(vcfCodes.assess_forBT_codonLOF_CANON,codonPathoLOFRS_CANON.toList.sorted.map{_.toString}.padTo(1,".").mkString(","));
              vb.addInfo(vcfCodes.assess_forBT_downstreamLOF,      downstreamPathoLOFRS.toList.sorted.map{_.toString}.padTo(1,".").mkString(","));
              vb.addInfo(vcfCodes.assess_forBT_downstreamLOF_CANON,downstreamPathoLOFRS_CANON.toList.sorted.map{_.toString}.padTo(1,".").mkString(","));
              
              val aminoMatchInfo = scala.collection.mutable.AnyRefMap[TXUtil.KnownVariantInfoHolder,Set[String]]();
              val nearMatchInfo = scala.collection.mutable.AnyRefMap[TXUtil.KnownVariantInfoHolder,Set[String]]();
              
              var ps1RS_canon = Set[String]();
              var pm5RS_canon = Set[String]();
              var ps1_canon = ACMGCrit("PS",1,false,Seq[String]());
              var pm5_canon = ACMGCrit("PM",5,false,Seq[String]());
              var aminoMatchInfo_canon =  scala.collection.mutable.AnyRefMap[TXUtil.KnownVariantInfoHolder,Set[String]]();
              var nearMatchInfo_canon =  scala.collection.mutable.AnyRefMap[TXUtil.KnownVariantInfoHolder,Set[String]]();
              
              combo.withFilter{case (g,tx,info,c,i) => {
                info.severityType == "NONSYNON"
              }}.foreach{case (g,tx,info,c,i) => {
                val txvar = clinVarVariants(tx) //.filter(cvinfo => cvinfo.isPatho)
                val aminoMatchSet = txvar.filter{cvinfo => {cvinfo.start == info.start && cvinfo.subType == info.subType && cvinfo.pvar == info.pvar}};
                val newAminoMatchInfo = aminoMatchSet.map(cvinfo => cvinfo.dbVarInfo.get).toSet
                newAminoMatchInfo.foreach{ ami => {
                  aminoMatchInfo(ami) = aminoMatchInfo.getOrElse(ami,Set[String]()) + tx;
                }}
                //aminoMatchInfo = aminoMatchInfo ++ newAminoMatchInfo
                if(isRefSeq(tx)) {
                  //aminoMatchInfo_canon = aminoMatchInfo_canon ++ newAminoMatchInfo;
                  newAminoMatchInfo.foreach{ ami => {
                    aminoMatchInfo_canon(ami) = aminoMatchInfo_canon.getOrElse(ami,Set[String]()) + tx;
                  }}
                }
                val aminoPathoMatch = aminoMatchSet.filter(cvinfo => cvinfo.isPatho);
                val aminoBenignMatch = aminoMatchSet.filter(cvinfo => cvinfo.isBenign);
                
                if(aminoPathoMatch.size > 0){
                  ps1 = ps1.merge(ACMGCrit("PS",1,true,Seq[String](tx + ":" + info.pvar)));
                  notice("Adding aminoMatch: rsnums:[\""+aminoPathoMatch.map(_.ID).toSet.mkString("\",\"")+"\"] \n"+
                          "                          [\""+aminoPathoMatch.map{(cvinfo) => { cvinfo.txid+":"+cvinfo.pvar+":"+cvinfo.ID }}.mkString("\",\"")+"\"]",
                          "addAminoMatchRS",5);
                  ps1RS = ps1RS ++ aminoPathoMatch.map(_.ID).toSet
                  
                  if(isRefSeq(tx)){
                    ps1_canon = ps1_canon.merge(ACMGCrit("PS",1,true,Seq[String](tx + ":" + info.pvar)));
                    ps1RS_canon = ps1RS_canon ++ aminoPathoMatch.map(_.ID).toSet
                  }
                }
                
                if(info.subType == "swapAA"){
                  val partialMatch = txvar.filter{(cvinfo) => {cvinfo.subType == "swapAA" && cvinfo.start == info.start && cvinfo.altAA != info.altAA}};
                  val partialPathoMatch = partialMatch.filter(cvinfo => cvinfo.isPatho);
                  val newNearMatchInfo = partialMatch.map(cvinfo => cvinfo.dbVarInfo.get).toSet;
                  //pm5RS = pm5RS ++ partialPathoMatch.map(_.ID).toSet;
                  pm5RS = pm5RS ++ partialPathoMatch.map( m => m.dbVarInfo.get.pathoIdString() ).toSet;
                  //nearMatchInfo = nearMatchInfo ++ newNearMatchInfo;
                  newNearMatchInfo.foreach{ ami => {
                    nearMatchInfo(ami) = nearMatchInfo.getOrElse(ami,Set[String]()) + tx;
                  }}
                  if(isRefSeq(tx)){
                    //pm5RS_canon = pm5RS_canon ++ partialPathoMatch.map(_.ID).toSet;
                    pm5RS_canon = pm5RS_canon ++ partialPathoMatch.map( m => m.dbVarInfo.get.pathoIdString() ).toSet;
                    //nearMatchInfo_canon = nearMatchInfo_canon ++ newNearMatchInfo;
                    newNearMatchInfo.foreach{ ami => {
                      nearMatchInfo_canon(ami) = nearMatchInfo_canon.getOrElse(ami,Set[String]()) + tx;
                    }}
                  }
                  if(partialPathoMatch.size > 0 && aminoPathoMatch.size == 0){
                    pm5 = pm5.merge(ACMGCrit("PM",5,true,Seq[String](tx+":"+info.pvar+"Vs"+partialPathoMatch.head.altAA)));
                    if(isRefSeq(tx)) pm5_canon = pm5_canon.merge(ACMGCrit("PM",5,true,Seq[String](tx+":"+info.pvar+"Vs"+partialPathoMatch.head.altAA)));
                  }
                } else if(info.pType == "STOP-LOSS"){
                  //do stuff?
                } else if(info.subType == "indelAA" || info.subType == "insAA" || info.subType == "delAA"){ //what about "multSwapAA"?
                  //do stuff?
                }
              }}
              
              if(ps1.flag){
                vb.addInfo(vcfCodes.assess_PS1,"1"); //CONVERT TO GENEWISE
                vb.addInfo(vcfCodes.assess_PM5,"0"); //CONVERT TO GENEWISE
              } else if(pm5.flag){
                vb.addInfo(vcfCodes.assess_PS1,"0"); //CONVERT TO GENEWISE
                vb.addInfo(vcfCodes.assess_PM5,"1"); //CONVERT TO GENEWISE
              } else {
                vb.addInfo(vcfCodes.assess_PS1,"0"); //CONVERT TO GENEWISE
                vb.addInfo(vcfCodes.assess_PM5,"0"); //CONVERT TO GENEWISE
              }
              if(ps1_canon.flag){
                vb.addInfo(vcfCodes.assess_PS1_CANON,"1"); //CONVERT TO GENEWISE
                vb.addInfo(vcfCodes.assess_PM5_CANON,"0"); //CONVERT TO GENEWISE
              } else if(pm5_canon.flag){
                vb.addInfo(vcfCodes.assess_PS1_CANON,"0"); //CONVERT TO GENEWISE
                vb.addInfo(vcfCodes.assess_PM5_CANON,"1"); //CONVERT TO GENEWISE
              } else {
                vb.addInfo(vcfCodes.assess_PS1_CANON,"0"); //CONVERT TO GENEWISE
                vb.addInfo(vcfCodes.assess_PM5_CANON,"0"); //CONVERT TO GENEWISE
              }
              
              val ps1set = aminoMatchInfo.filter{ case (ami,txSet) => { ami.isPatho }}.flatMap{ case (ami,txSet) => txSet.map{txToGene(_)} }.toSet
              val pm5set = nearMatchInfo.filter{ case (ami,txSet) => { ami.isPatho }}.flatMap{ case (ami,txSet) => txSet.map{txToGene(_)} }.toSet -- ps1set;
              val ps1set_canon = aminoMatchInfo_canon.filter{ case (ami,txSet) => { ami.isPatho }}.flatMap{ case (ami,txSet) => txSet.map{txToGene(_)} }.toSet
              val pm5set_canon = nearMatchInfo_canon.filter{ case (ami,txSet) => { ami.isPatho }}.flatMap{ case (ami,txSet) => txSet.map{txToGene(_)} }.toSet -- ps1set_canon;
              
              if(includeGenewise) vb.addInfo(vcfCodes.assess_PS1+"_GENEWISE",ps1set.toVector.distinct.sorted.mkString(","));
              if(includeGenewise) vb.addInfo(vcfCodes.assess_PM5+"_GENEWISE",pm5set.toVector.distinct.sorted.mkString(","));
              if(includeGenewise) vb.addInfo(vcfCodes.assess_PS1_CANON+"_GENEWISE",ps1set_canon.toVector.distinct.sorted.mkString(","));
              if(includeGenewise) vb.addInfo(vcfCodes.assess_PM5_CANON+"_GENEWISE",pm5set_canon.toVector.distinct.sorted.mkString(","));
              
              crits.addCrit(ps1.addGeneWise(ps1set));
              crits.addCrit(pm5.addGeneWise(pm5set));
              canonCrits.addCrit(ps1_canon.addGeneWise(ps1set_canon));
              canonCrits.addCrit(pm5_canon.addGeneWise(pm5set_canon));
              
              //vb.addInfo(vcfCodes.assess_pathoExactMatchRS, if(exactRS == "") "." else exactRS);
              //varInfo.get.idString();
              vb.addInfo(vcfCodes.assess_pathoExactMatchRS, getOrFunc(varInfo, ".")(vi => vi.pathoIdString()));
              vb.addInfo(vcfCodes.assess_pathoAminoMatchRS, ps1RS.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
              vb.addInfo(vcfCodes.assess_pathoNearMatchRS,  pm5RS.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
              vb.addInfo(vcfCodes.assess_pathoAminoMatchRS_CANON, ps1RS_canon.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
              vb.addInfo(vcfCodes.assess_pathoNearMatchRS_CANON,  pm5RS_canon.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
              
              vb.addInfo(vcfCodes.assess_pathoExactMatchRS+"_ClinVar", (varInfo match {
                case Some(vinf) => {
                  if(vinf.isPathogenic(true,false)){
                    vinf.clinVarID.getOrElse(".")
                  } else {
                    "."
                  }
                }
                case None => ".";
              }));
              vb.addInfo(vcfCodes.assess_pathoAminoMatchRS+"_ClinVar", aminoMatchInfo.filter{ case (b,txSet) => b.isPathogenic(true,false) && b.clinVarID.isDefined}.map{ case (b,txSet) => {
                b.clinVarID.getOrElse(".")
              }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
              vb.addInfo(vcfCodes.assess_pathoNearMatchRS+"_ClinVar",  nearMatchInfo.filter{ case (b,txSet) => b.isPathogenic(true,false) && b.clinVarID.isDefined}.map{ case (b,txSet) => {
                b.clinVarID.getOrElse(".")
              }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
              vb.addInfo(vcfCodes.assess_pathoAminoMatchRS_CANON+"_ClinVar", aminoMatchInfo_canon.filter{ case (b,txSet) => b.isPathogenic(true,false) && b.clinVarID.isDefined}.map{ case (b,txSet) => {
                b.clinVarID.getOrElse(".")
              }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
              vb.addInfo(vcfCodes.assess_pathoNearMatchRS_CANON+"_ClinVar",  nearMatchInfo_canon.filter{ case (b,txSet) => b.isPathogenic(true,false) && b.clinVarID.isDefined}.map{ case (b,txSet) => {
                b.clinVarID.getOrElse(".")
              }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
              
              
              //vb.addInfo(vcfCodes.assess_pathoExactMatchRS, getOrFunc(varInfo, ".")(vi => vi.pathoIdString()));
              vb.addInfo(vcfCodes.assess_benignExactMatchRS+"_ClinVar", getOrFunc(varInfo, ".")(vi => if(vi.isBenign(true,false)) vi.clinVarID.get else "." ));
              vb.addInfo(vcfCodes.assess_benignAminoMatchRS+"_ClinVar", aminoMatchInfo.filter{ case (b,txSet) => b.isBenign(true,false) && b.clinVarID.isDefined}.map{ case (b,txSet) => {
                b.clinVarID.get
              }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
              vb.addInfo(vcfCodes.assess_benignAminoMatchRS_CANON+"_ClinVar", aminoMatchInfo_canon.filter{ case (b,txSet) => b.isBenign(true,false) && b.clinVarID.isDefined}.map{ case (b,txSet) => {
                b.clinVarID.get
              }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
              vb.addInfo(vcfCodes.assess_benignNearMatchRS+"_ClinVar",  nearMatchInfo.filter{ case (b,txSet) => b.isBenign(true,false) && b.clinVarID.isDefined}.map{ case (b,txSet) => {
                b.clinVarID.get
              }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
              vb.addInfo(vcfCodes.assess_benignNearMatchRS_CANON+"_ClinVar",  nearMatchInfo_canon.filter{ case (b,txSet) => b.isBenign(true,false) && b.clinVarID.isDefined}.map{ case (b,txSet) => {
                b.clinVarID.get
              }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
              
              
              vb.addInfo(vcfCodes.assess_pathoExactMatchRS+"_HGMD", (varInfo match {
                case Some(vinf) => {
                  if(vinf.isPathogenic(false,true)){
                    vinf.hgmdID.getOrElse(".")
                  } else {
                    "."
                  }
                }
                case None => ".";
              }));
              vb.addInfo(vcfCodes.assess_pathoAminoMatchRS+"_HGMD", aminoMatchInfo.filter{ case (b,txSet) => b.isPathogenic(false,true) && b.hgmdID.isDefined}.map{ case (b,txSet) => {
                b.hgmdID.getOrElse(".")
              }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
              vb.addInfo(vcfCodes.assess_pathoNearMatchRS+"_HGMD",  nearMatchInfo.filter{ case (b,txSet) => b.isPathogenic(false,true) && b.hgmdID.isDefined}.map{ case (b,txSet) => {
                b.hgmdID.getOrElse(".")
              }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
              vb.addInfo(vcfCodes.assess_pathoAminoMatchRS_CANON+"_HGMD", aminoMatchInfo_canon.filter{ case (b,txSet) => b.isPathogenic(false,true) && b.hgmdID.isDefined}.map{ case (b,txSet) => {
                b.hgmdID.getOrElse(".")
              }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
              vb.addInfo(vcfCodes.assess_pathoNearMatchRS_CANON+"_HGMD",  nearMatchInfo_canon.filter{ case (b,txSet) => b.isPathogenic(false,true) && b.hgmdID.isDefined}.map{ case (b,txSet) => {
                b.hgmdID.getOrElse(".")
              }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
              
              //        assess_exactMatchInfo : String = TOP_LEVEL_VCF_TAG+"ACMG_cvInfo_ExactMatch",
              //  assess_aminoMatchInfo : String = TOP_LEVEL_VCF_TAG+"ACMG_cvInfo_AminoMatch",
              //  assess_nearMatchInfo : String = TOP_LEVEL_VCF_TAG+"ACMG_cvInfo_NearMatch",
              
                val varInfoString = varInfo match {
                  case Some(vinf) => {
                    vinf.fullInfoString()
                  }
                  case None => {
                    ".";
                  }
                }
                
              vb.addInfo(vcfCodes.assess_exactMatchInfo,  varInfoString);
              vb.addInfo(vcfCodes.assess_aminoMatchInfo, aminoMatchInfo.toVector.sortBy{ case (b,txSet) => b.id }.map{case (b,txSet) => b.fullInfoString() + "|" + txSet.toVector.sorted.mkString("/")+"|"+txSet.map{tx => txToGene(tx)}.toVector.sorted.mkString("/")}.padTo(1,".").mkString(","));
              vb.addInfo(vcfCodes.assess_nearMatchInfo,  nearMatchInfo.toVector.sortBy{ case (b,txSet) => b.id }.map{case (b,txSet) => b.fullInfoString() + "|" + txSet.toVector.sorted.mkString("/")+"|"+txSet.map{tx => txToGene(tx)}.toVector.sorted.mkString("/")}.padTo(1,".").mkString(","));
              vb.addInfo(vcfCodes.assess_aminoMatchInfo_CANON, aminoMatchInfo_canon.toVector.sortBy{ case (b,txSet) => b.id }.map{case (b,txSet) => b.fullInfoString() + "|" + txSet.toVector.sorted.mkString("/")+"|"+txSet.map{tx => txToGene(tx)}.toVector.sorted.mkString("/")}.padTo(1,".").mkString(","));
              vb.addInfo(vcfCodes.assess_nearMatchInfo_CANON,  nearMatchInfo_canon.toVector.sortBy{ case (b,txSet) => b.id }.map{case (b,txSet) => b.fullInfoString() + "|" + txSet.toVector.sorted.mkString("/")+"|"+txSet.map{tx => txToGene(tx)}.toVector.sorted.mkString("/")}.padTo(1,".").mkString(","));
              
              vb.addInfo(vcfCodes.assess_exactMatchInfo+"_ClinVar", (varInfo match {
                case Some(vinf) => {
                  vinf.infoString(clinVar=true,hgmd=false)
                }
                case None => ".";
              }));
              
              vb.addInfo(vcfCodes.assess_aminoMatchInfo+"_ClinVar", aminoMatchInfo.filter{ case (b,txSet) => b.clinVarID.isDefined}.map{ case (b,txSet) => {
                b.infoString(clinVar = true,hgmd=false);
              }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
              vb.addInfo(vcfCodes.assess_nearMatchInfo+"_ClinVar",  nearMatchInfo.filter{ case (b,txSet) => b.clinVarID.isDefined}.map{ case (b,txSet) => {
                b.infoString(clinVar = true,hgmd=false);
              }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
              vb.addInfo(vcfCodes.assess_aminoMatchInfo_CANON+"_ClinVar", aminoMatchInfo_canon.filter{ case (b,txSet) => b.clinVarID.isDefined}.map{ case (b,txSet) => {
                b.infoString(clinVar = true,hgmd=false);
              }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
              vb.addInfo(vcfCodes.assess_nearMatchInfo_CANON+"_ClinVar",  nearMatchInfo_canon.filter{ case (b,txSet) => b.clinVarID.isDefined}.map{ case (b,txSet) => {
                b.infoString(clinVar = true,hgmd=false);
              }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
              
              vb.addInfo(vcfCodes.assess_exactMatchInfo+"_HGMD", (varInfo match {
                case Some(vinf) => {
                  vinf.infoString(clinVar=false,hgmd=true)
                }
                case None => ".";
              }));
              vb.addInfo(vcfCodes.assess_aminoMatchInfo+"_HGMD", aminoMatchInfo.filter{ case (b,txSet) => b.hgmdID.isDefined}.map{ case (b,txSet) => {
                b.infoString(clinVar = false,hgmd=true);
              }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
              vb.addInfo(vcfCodes.assess_nearMatchInfo+"_HGMD",  nearMatchInfo.filter{ case (b,txSet) => b.hgmdID.isDefined}.map{ case (b,txSet) => {
                b.infoString(clinVar = false,hgmd=true);
              }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
              vb.addInfo(vcfCodes.assess_aminoMatchInfo_CANON+"_HGMD", aminoMatchInfo_canon.filter{ case (b,txSet) => b.hgmdID.isDefined}.map{ case (b,txSet) => {
                b.infoString(clinVar = false,hgmd=true);
              }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
              vb.addInfo(vcfCodes.assess_nearMatchInfo_CANON+"_HGMD",  nearMatchInfo_canon.filter{ case (b,txSet) => b.hgmdID.isDefined}.map{ case (b,txSet) => {
                b.infoString(clinVar = false,hgmd=true);
              }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
              
              */
                            
          }
        
        vb;
      }},outHeader);
    }
      /*
    def addAminoMatchAnnotation(v : SVcfVariantLine, 
                      txToGene : (String => String) = ((s : String) => s),
                      geneToTx : (String => Set[String]),
                                dbVariants : scala.collection.Map[String,Set[internalUtils.TXUtil.pVariantInfo]],
                                isRefSeq : (String => Boolean),
                                dbVariantSet : scala.collection.Map[String,TXUtil.KnownVariantInfoHolder],
                                dbVariants_LOFSet : scala.collection.Map[String,Set[TXUtil.pVariantInfo]],
                                dbVariants_startLoss : scala.collection.Map[String,Set[TXUtil.pVariantInfo]],
                                dbVariants_stopLoss : scala.collection.Map[String,Set[TXUtil.pVariantInfo]],
                                vcfCodes : VCFAnnoCodes = VCFAnnoCodes()) : SVcfVariantLine = {
      var vb = v.getOutputLine();
  
      var problemList = Set[String]();
          
      val chrom = v.chrom
      val pos = v.pos
      
      val refAlle = v.ref
      val altAlleles = v.alt.zipWithIndex.filter{ case (alt,altIdx) => alt != VcfTool.UNKNOWN_ALT_TAG_STRING };
      
      val txList = v.getInfoList(vcfCodes.txList_TAG)
      
      if(txList.filter(tx => tx != ".").length > 0){
        notice("tx found for variant","TX_FOUND",0);
        val geneList = txList.map(x => txToGene(x));
          val vTypesList = v.getInfoArray(vcfCodes.vType_TAG);
          val vLVL       = v.getInfoList(vcfCodes.vMutLVL_TAG);
          val vMutPList = v.getInfoArray(vcfCodes.vMutP_TAG);
          val vMutCList = v.getInfoArray(vcfCodes.vMutC_TAG);
          val vMutInfoList = v.getInfoArray(vcfCodes.vMutINFO_TAG).map{k => k.map(x => {internalUtils.TXUtil.getPvarInfoFromString(x)})};
          
          val variantIV = commonSeqUtils.GenomicInterval(chrom,'.', start = pos - 1, end = pos + math.max(v.ref.length,1));
          if(altAlleles.length > 1){
            error("FATAL ERROR: Cannot deal with multiallelic variants! Split variants to single-allelic!");
          }
          val (alt,altIdx) = altAlleles.head;
          
          //val out : Vector[ACMGCritSet] = altAlleles.map{ case (alt,altIdx) => {
            val vTypes = vTypesList(altIdx).map(_.split("_"));
            val vMutP = vMutPList(altIdx);
            val vMutC = vMutCList(altIdx);
            val vInfo = vMutInfoList(altIdx);
            val combo = txList.zip(vInfo).zip(vMutC).zipWithIndex.map{case (((tx,info),c),i) => (txToGene(tx),tx,info,c,i)}
            val canonCombo = combo.filter{ case (g,tx,info,c,i) => isRefSeq(tx) }
            
            dbVarMap.get(
      }
      vb;
    }
   */ 
  }
  
  
  /*
   * 
         Vector[(String,Set[String],Set[String])](
          ("dbNSFP_MetaSVM_pred",Set[String]("D"),Set[String]("T"))//, //Includes predictors:   PolyPhen-2, SIFT, LRT, MutationTaster, Mutation Assessor, FATHMM, GERP++, PhyloP, SiPhy
                        //("dbNSFP_PROVEAN_pred",Set[String]("D"))
                    )
   * 
   */
  
  
  
  
  
  case class SummaryACMGWalker(
                   groupFile : Option[String],
                   groupList : Option[String],
                   superGroupList : Option[String],
                   outfile : String
                 ) extends internalUtils.VcfTool.VCFWalker {
      var vcfCodes : VCFAnnoCodes = VCFAnnoCodes();
      val sampleToGroupMap = new scala.collection.mutable.AnyRefMap[String,Set[String]](x => Set[String]());
      val groupToSampleMap = new scala.collection.mutable.AnyRefMap[String,Set[String]](x => Set[String]());
      var groupSet : Set[String] = Set[String]();
      
      groupFile match {
        case Some(gf) => {
          val r = getLinesSmartUnzip(gf).drop(1);
          r.foreach(line => {
            val cells = line.split("\t");
            if(cells.length != 2) error("ERROR: group file must have exactly 2 columns. sample.ID and group.ID!");
            groupToSampleMap(cells(1)) = groupToSampleMap(cells(1)) + cells(0);
            sampleToGroupMap(cells(0)) = sampleToGroupMap(cells(0)) + cells(1);
            groupSet = groupSet + cells(1);
          })
        }
        case None => {
          //do nothing
        }
      }
      
      groupList match {
        case Some(g) => {
          val r = g.split(";");
          r.foreach(grp => {
            val cells = grp.split(",");
            val grpID = cells.head;
            cells.tail.foreach(samp => {
              sampleToGroupMap(samp) = sampleToGroupMap(samp) + grpID;
              groupToSampleMap(grpID) = groupToSampleMap(grpID) + samp;
            })
            groupSet = groupSet + grpID;
          })
        }
        case None => {
          //do nothing
        }
      }
      superGroupList match {
        case Some(g) => {
          val r = g.split(";");
          r.foreach(grp => {
            val cells = grp.split(",");
            val grpID = cells.head;
            cells.tail.foreach(subGrp => {
              groupToSampleMap(grpID) = groupToSampleMap(grpID) ++ groupToSampleMap(subGrp);
            })
            groupSet = groupSet + grpID;
          })
        }
        case None => {
          //do nothing
        }
      }
      val groups = groupSet.toVector.sorted;
      reportln("Final Groups:","debug");
      for((g,i) <- groups.zipWithIndex){
          reportln("Group "+g+" ("+groupToSampleMap(g).size+")","debug");
      }
      
      val geneAndTypeCountMap = new scala.collection.mutable.AnyRefMap[String,Map[String,Int]](x => Map[String,Int]().withDefault(s => 0));
      val geneAndTypeCountMap_CANON = new scala.collection.mutable.AnyRefMap[String,Map[String,Int]](x => Map[String,Int]().withDefault(s => 0));
      
      
     /* val variantSampleMap = new scala.collection.mutable.AnyRefMap[Int,Set[String]](x => Set[String]());
      val variantGeneMap = new scala.collection.mutable.AnyRefMap[Int,Set[String]]();
      val variantTypeMap = new scala.collection.mutable.AnyRefMap[Int,String]();
      val variantTypeMap_CANON = new scala.collection.mutable.AnyRefMap[Int,String]();*/
      
      //(geneSet, (level,level_canon), sampleSet)
      var rawVariantInfo = Vector[(Set[String],(String,String),Set[String])]();
      var allGeneSet = Set[String]();
      
      def walkVCF(vcIter : Iterator[VariantContext], vcfHeader : VCFHeader, verbose : Boolean = true) : (Iterator[VariantContext],VCFHeader) = {
        val sampNames = vcfHeader.getSampleNamesInOrder().asScala.toVector;
        
        val newHeaderLines = List[VCFHeaderLine](); 
        val newHeader = internalUtils.VcfTool.addHeaderLines(vcfHeader,newHeaderLines);
        
        return (addIteratorCloseAction(vcIter.zipWithIndex.map{ case (vc,i) => {
           val geneList = getAttributeSmart(vc,vcfCodes.assess_geneList).toSet.toVector.toSet;
           val lvl = vc.getAttributeAsString(vcfCodes.assess_RATING,"UNK");
           val lvl_CANON = vc.getAttributeAsString(vcfCodes.assess_RATING_CANON,"UNK");
           
           allGeneSet = allGeneSet ++ geneList;
           
           val refAlle = vc.getReference();
           val altAlleles = Range(0,vc.getNAlleles()-1).map((a) => vc.getAlternateAllele(a)).zipWithIndex.filter{case (alt,altIdx) => { alt.getBaseString() != "*" }}
           if( altAlleles.length != 1 ){
             error("More than one alt allele found!");
           }
           if( altAlleles.head._2 != 0){
             warning("Alt alleIdx != 0","ALTIDX_NE_0",20);
           }
           val alt = altAlleles.head._1;
           
           //variantGeneMap(i) = geneList.toSet;
           //variantTypeMap(i) = 
           val altGenoSampleSet = vc.getGenotypes().asScala.iterator.filter(g => {
             g.getAlleles().asScala.exists(a => a.equals(alt))
           }).map(g => {
             g.getSampleName();
           }).toSet;
           
           if(! vc.isFiltered()){
             rawVariantInfo = rawVariantInfo :+ (geneList,(lvl,lvl_CANON), altGenoSampleSet);
           }
           
           vc;
        }},closeAction=() => {
          
          reportln("Filtering VariantInfo..." + getDateAndTimeString,"debug");
          val variantInfo = rawVariantInfo.filter{ case (geneSet,(lvl,lvl_CANON),ss) => {
            lvl == "PATHO" || lvl == "LPATH" || lvl_CANON == "PATHO" || lvl_CANON == "LPATH";
          }}
          reportln("Done filtering VariantInfo. " + getDateAndTimeString,"debug");
          val out = openWriterSmart(outfile);
          val allGenes = allGeneSet.toVector.sorted;
          
          reportln("   geneSampCount_patho..." + getDateAndTimeString,"debug");
          val geneSampCount_patho = allGenes.map{ gene => { 
            val geneVars = variantInfo.filter{ case (geneSet,(lvl,lvl_CANON),ss) => {
              geneSet.contains(gene) && lvl == "PATHO"
            }}
            groups.map{ grp => {
            val grpSS = groupToSampleMap(grp);
            geneVars.flatMap{ case (geneSet,(lvl,lvl_CANON),ss) => {
              ss
            }}.toSet.filter(s => { grpSS.contains(s) }).size;
          }}}}
          reportln("   geneSampCount_patho_canon..." + getDateAndTimeString,"debug");
          val geneSampCount_patho_canon = allGenes.map{ gene => { 
            val geneVars = variantInfo.filter{ case (geneSet,(lvl,lvl_CANON),ss) => {
              geneSet.contains(gene) && lvl_CANON == "PATHO"
            }}
            groups.map{ grp => {
            val grpSS = groupToSampleMap(grp);
            geneVars.flatMap{ case (geneSet,(lvl,lvl_CANON),ss) => {
              ss
            }}.toSet.filter(s => { grpSS.contains(s) }).size;
          }}}}
          reportln("   geneSampCount_lpath..." + getDateAndTimeString,"debug");
          val geneSampCount_lpath = allGenes.map{ gene => { 
            val geneVars = variantInfo.filter{ case (geneSet,(lvl,lvl_CANON),ss) => {
              geneSet.contains(gene) && (lvl == "PATHO" || lvl == "LPATH")
            }}
            groups.map{ grp => {
            val grpSS = groupToSampleMap(grp);
            geneVars.flatMap{ case (geneSet,(lvl,lvl_CANON),ss) => {
              ss
            }}.toSet.filter(s => { grpSS.contains(s) }).size;
          }}}}
          reportln("   geneSampCount_lpath_canon..." + getDateAndTimeString,"debug");
          val geneSampCount_lpath_canon = allGenes.map{ gene => { 
            val geneVars = variantInfo.filter{ case (geneSet,(lvl,lvl_CANON),ss) => {
              geneSet.contains(gene) && (lvl_CANON == "PATHO" || lvl_CANON == "LPATH")
            }}
            groups.map{ grp => {
            val grpSS = groupToSampleMap(grp);
            geneVars.flatMap{ case (geneSet,(lvl,lvl_CANON),ss) => {
              ss
            }}.toSet.filter(s => { grpSS.contains(s) }).size;
          }}}}
          reportln("   sampCounts..." + getDateAndTimeString,"debug");
          
          val sampCount_patho = groups.map{ grp => {
            val grpSS = groupToSampleMap(grp);
            variantInfo.filter{ case (geneSet,(lvl,lvl_CANON),ss) => {
              lvl == "PATHO"
            }}.flatMap{ case (geneSet,(lvl,lvl_CANON),ss) => {
              ss
            }}.toSet.filter(s => { grpSS.contains(s) }).size;
          }}
          val sampCount_lpath = groups.map{ grp => {
            val grpSS = groupToSampleMap(grp);
            variantInfo.filter{ case (geneSet,(lvl,lvl_CANON),ss) => {
              lvl == "PATHO" || lvl == "LPATH";
            }}.flatMap{ case (geneSet,(lvl,lvl_CANON),ss) => {
              ss
            }}.toSet.filter(s => { grpSS.contains(s) }).size;
          }}
          val sampCount_patho_canon = groups.map{ grp => {
            val grpSS = groupToSampleMap(grp);
            variantInfo.filter{ case (geneSet,(lvl,lvl_CANON),ss) => {
              lvl_CANON == "PATHO"
            }}.flatMap{ case (geneSet,(lvl,lvl_CANON),ss) => {
              ss
            }}.toSet.filter(s => { grpSS.contains(s) }).size;
          }}
          val sampCount_lpath_canon = groups.map{ grp => {
            val grpSS = groupToSampleMap(grp);
            variantInfo.filter{ case (geneSet,(lvl,lvl_CANON),ss) => {
              lvl_CANON == "PATHO" || lvl_CANON == "LPATH";
            }}.flatMap{ case (geneSet,(lvl,lvl_CANON),ss) => {
              ss
            }}.toSet.filter(s => { grpSS.contains(s) }).size;
          }}
          reportln("   done with all samp counts..." + getDateAndTimeString,"debug");
          
          out.write("geneID");
          groups.map{ grp => {
            out.write("\t"+Vector("numSamp PATHO","numSamp PATHO CANON","numSamp LPATH","numSamp LPATH CANON").map(grp + _).mkString("\t"));
          }}
          out.write("\n");

          allGenes.zipWithIndex.foreach{ case (gene,i) => {
            out.write(gene);
            groups.zipWithIndex.foreach{ case (grp,grpIdx) => {
             out.write("\t"+
                 geneSampCount_patho(i)(grpIdx)+"\t"+
                 geneSampCount_patho_canon(i)(grpIdx)+"\t"+
                 geneSampCount_lpath(i)(grpIdx)+"\t"+
                 geneSampCount_lpath_canon(i)(grpIdx));
            }}
            out.write("\n");
          }}
          
          out.write("ANY_GENE");
          
          groups.zipWithIndex.foreach{ case (grp,grpIdx) => {
             out.write("\t"+
                 sampCount_patho(grpIdx)+"\t"+
                 sampCount_patho_canon(grpIdx)+"\t"+
                 sampCount_lpath(grpIdx)+"\t"+
                 sampCount_lpath_canon(grpIdx));
          }}
          out.write("\n");
          
          out.close();
          reportln("   done with SummaryACMGWalker. " + getDateAndTimeString,"debug");
        }),newHeader);
      }
    
  }
  

  class AddDatabaseMatchingSimple(txToGeneFile : Option[String], refSeqFile : Option[String],
                            chromList : Option[List[String]], 
                            geneSet : Option[Set[String]],
                               dbName : String,
                               dbFile : String,
                               idTag : String,
                               pathoExpression : Option[String],
                               benExpression : Option[String],
                               metaDataTags : Seq[String],
 //                           dbdata : Seq[(String,String,String,Option[String],Option[String],Seq[String])],
                               //dbName,dbFile,idTag,pathoExpression,benExpression,metaDataTags
                            tagInfix : String = ""
                            
                            ) extends SVcfWalker {
    def walkerName : String = "AddDatabaseMatchingSimple"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("txToGeneFile", txToGeneFile.getOrElse(".")),
        ("refSeqFile", refSeqFile.getOrElse("."))
    );
    val vcfCodes : VCFAnnoCodes = VCFAnnoCodes()
    
    reportln("Reading geneToTX map... ["+stdUtils.getDateAndTimeString+"]","debug");

    
    
    val geneToTx : (String => Set[String]) = txToGeneFile match {
      case Some(f) => {
        val geneToTxMap = scala.collection.mutable.AnyRefMap[String,Set[String]]().withDefault(x => Set[String]());
        getLinesSmartUnzip(f).foreach(line => {
          val cells = line.split("\t");
          geneToTxMap(cells(1)) = geneToTxMap(cells(1)) + cells(0);
        })
        ((s : String) => geneToTxMap.getOrElse(s,Set[String]()));
      }
      case None => {
        ((s : String) => Set[String](s));
      }
    }
    val txSet = geneSet match {
      case Some(gs) => {
        Some(gs.flatMap{ g => geneToTx(g)})
      }
      case None => {
        None
      }
    }
    
    notice("txSet.size="+txSet.getOrElse(Set()).size+". Examples:","TXSET_LENGTH",-1);
    txSet.getOrElse(Set()).take(10).foreach{ tx => {
      notice("      "+tx,"TXEX",-1);
    }}

        
    val (dbVarMap,dbTxVarMap) = getDbMappers(infile =dbFile, chromList =chromList, vcfCodes = vcfCodes,
                 dbName = dbName,
                 keepInfo = metaDataTags,
                 idTag = idTag,
                 pathoExpression = pathoExpression,
                 benExpression = benExpression,
                 txSet = txSet,
                 soFar = None
                )
                
    reportln("Reading txToGene map... ["+stdUtils.getDateAndTimeString+"]","debug");
    val txToGene : (String => String) = txToGeneFile match {
      case Some(f) => {
        val txToGeneMap = getLinesSmartUnzip(f).map(line => {
          val cells = line.split("\t");
          (cells(0),cells(1));
        }).toMap
        ((s : String) => txToGeneMap.getOrElse(s,s));
      }
      case None => {
        ((s : String) => s);
      }
    } 


    reportln("Reading refSeq map... ["+stdUtils.getDateAndTimeString+"]","debug");
    
    val isCanon : (String => Boolean) = refSeqFile match {
      case Some(f) => {
        val lines = getLinesSmartUnzip(f);
        val table = getTableFromLines(lines,colNames = Seq("transcript"), errorName = "File "+f);
        var refSeqSet : Set[String] = table.map(tableCells => {
          val tx = tableCells(0);
          tx
        }).toSet
        reportln("   found: "+refSeqSet.size+" RefSeq transcripts.","debug");
        ((s : String) => {
          refSeqSet.contains(s);
        })
      }
      case None => {
        ((s : String) => {
          false;
        })
      }
    }
    
    def isLOF(info : pVariantInfo) : Boolean = {
      info.severityType == "LLOF" || info.severityType == "PLOF" || info.pType == "START-LOSS" || info.subType.startsWith("splice")
    }
    
    val dbIterationSet = Seq(
           ("_"+dbName,dbName,"variants from any "+dbName,((info : KnownVarHolder) => info.sourceList.contains(dbName))),
           ("_"+dbName+"_PTH",dbName,"variants with any pathogenic reports from "+dbName,((info : KnownVarHolder) => info.isPatho.zip(info.sourceList).exists{ case (p,src) => src == dbName && p})),
           ("_"+dbName+"_BEN",dbName,"variants with any pathogenic reports from "+dbName,((info : KnownVarHolder) => info.isBen.zip(info.sourceList).exists{ case (p,src) => src == dbName && p}))
          );
    val txSetIterationSet = Seq(
           ("_ALLTX"," (considering all valid coding transcripts)",((tx : String) => true)),
           ("_CANON"," (considering canonical transcripts only)",((tx : String) => isCanon(tx)))
        );
    val tpre = vcfCodes.assess_varInfoPrefix+tagInfix;
    
    val nonSynonSevSet = Set[String]("LLOF","PLOF","NONSYNON");
    val codingSevSet = Set[String]("LLOF","PLOF","NONSYNON","SYNON");
    
    val infoList = dbIterationSet.flatMap{ case (dbInfix,dbName,dbDesc,dbFunc) => {
      Seq(
        (new SVcfCompoundHeaderLine("INFO",ID=tpre+dbInfix+"_ID_bpMatch",Number=".",Type="String",desc="exact base-pair match "+dbDesc)).addWalker(this),
        (new SVcfCompoundHeaderLine("INFO",ID=tpre+dbInfix+"_CT_bpMatch",Number="1",Type="Integer",desc="Num exact base-pair match "+dbDesc)).addWalker(this)
      ) ++ txSetIterationSet.flatMap{ case (txSetInfix,txSetDesc,txSetFunc) => {
        Seq(
          (new SVcfCompoundHeaderLine("INFO",ID=tpre+txSetInfix+dbInfix++"_CT_aaMatch",Number="1",Type="Integer",desc="Num amino-acid-matching "+dbDesc)).addWalker(this),
          (new SVcfCompoundHeaderLine("INFO",ID=tpre+txSetInfix+dbInfix+"_ID_aaMatch",Number=".",Type="String",desc="Amino acid match "+dbDesc)).addWalker(this),
          (new SVcfCompoundHeaderLine("INFO",ID=tpre+txSetInfix+dbInfix+"_ID_aaMatch_diffBP",Number=".",Type="String",desc="Different bp but same amino acid match "+dbDesc)).addWalker(this),
          (new SVcfCompoundHeaderLine("INFO",ID=tpre+txSetInfix+dbInfix+"_CT_aaMatch_diffBP",Number="1",Type="Integer",desc="Num matching "+dbDesc)).addWalker(this),
          (new SVcfCompoundHeaderLine("INFO",ID=tpre+txSetInfix+dbInfix+"_ID_sameCodon",Number=".",Type="String",desc="Same codon pos match "+dbDesc)).addWalker(this),
          (new SVcfCompoundHeaderLine("INFO",ID=tpre+txSetInfix+dbInfix+"_CT_sameCodon",Number="1",Type="Integer",desc="Num same codon pos match "+dbDesc)).addWalker(this),
          (new SVcfCompoundHeaderLine("INFO",ID=tpre+txSetInfix+dbInfix+"_ID_sameCodon_diffAA",Number=".",Type="String",desc="Same codon pos but different aa "+dbDesc)).addWalker(this),
          (new SVcfCompoundHeaderLine("INFO",ID=tpre+txSetInfix+dbInfix+"_CT_sameCodon_diffAA",Number="1",Type="Integer",desc="Num same codon pos but different aa"+dbDesc)).addWalker(this),
          (new SVcfCompoundHeaderLine("INFO",ID=tpre+txSetInfix+dbInfix+"_ID_downstreamLOF",Number=".",Type="String",desc="Downstream LOF "+dbDesc)).addWalker(this),
          (new SVcfCompoundHeaderLine("INFO",ID=tpre+txSetInfix+dbInfix+"_CT_downstreamLOF",Number="1",Type="Integer",desc="Num downstream LOF "+dbDesc)).addWalker(this),
          (new SVcfCompoundHeaderLine("INFO",ID=tpre+txSetInfix+dbInfix+"_ID_downstreamLOF_sameType",Number=".",Type="String",desc="Downstream same-type LOF "+dbDesc)).addWalker(this),
          (new SVcfCompoundHeaderLine("INFO",ID=tpre+txSetInfix+dbInfix+"_CT_downstreamLOF_sameType",Number="1",Type="Integer",desc="Num downstream same-type LOF"+dbDesc)).addWalker(this)
          
        )
      }}
    }}
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader;
      outHeader.addWalk(this);
      
      infoList.foreach{ infoLine => {
        outHeader.addInfoLine(infoLine);
      }}
      
      (vcMap(vcIter){ v => {
        val vb = v.getOutputLine;
        
          var problemList = Set[String]();
              
          val chrom = v.chrom
          val pos = v.pos
          
          val refAlle = v.ref
          val altAlleles = v.alt.zipWithIndex.filter{ case (alt,altIdx) => alt != VcfTool.UNKNOWN_ALT_TAG_STRING };
          
          val txList = v.getInfoList(vcfCodes.txList_TAG)
          
          if(txList.filter(tx => tx != ".").length > 0){
            notice("tx found for variant","TX_FOUND",0);
            val geneList = txList.map(x => txToGene(x));
              val varid = v.chrom + ":" + v.getInfo(vcfCodes.vMutG_TAG)
              //val vTypes = v.info.get(vcfCodes.vType_TAG).getOrElse(None).filter(_ != ".").map{ _.split("\\|") }
              //val vLVL       = v.info.get(vcfCodes.vMutLVL_TAG).getOrElse(None).filter(_ != ".").map{ _.split("\\|") }
              val vMutP = v.info.get(vcfCodes.vMutP_TAG).getOrElse(None).filter(_ != ".").map{ _.split("\\|") }
              val vMutC = v.info.get(vcfCodes.vMutC_TAG).getOrElse(None).filter(_ != ".").map{ _.split("\\|") }
              val mutInfo = v.info.get(vcfCodes.vMutINFO_TAG).getOrElse(None).filter(_ != ".").map{_.split("\\|")}.map{ mutInfoString => {
                 val mutInf : Array[pVariantInfo] = mutInfoString.map{ mis => {
                   internalUtils.TXUtil.getPvarInfoFromString(mis, ID = varid);
                 }}
                 mutInf
              }}.getOrElse( Array[pVariantInfo]() )
              
              val variantIV = commonSeqUtils.GenomicInterval(chrom,'.', start = pos - 1, end = pos + math.max(v.ref.length,1));
              if(altAlleles.length > 1){
                error("FATAL ERROR: Cannot deal with multiallelic variants! Split variants to single-allelic!");
              }
              val (alt,altIdx) = altAlleles.head;
              val combo = txList.zip(mutInfo).zipWithIndex.map{ case ((tx,info),i) => (txToGene(tx),tx,info,i) }.filter{ case (g,tx,info,i) => {
                nonSynonSevSet.contains(info.severityType);
              }}
              val canonCombo = combo.filter{ case (g,tx,info,i) => isCanon(tx) }
              val geneSet = geneList.distinct.toVector.sorted
              val geneSetCanon = canonCombo.map{ case (g,tx,info,i) => g}.distinct.toVector.sorted;
          
              val exactMatch = dbVarMap.get(varid)
              
              val dbDownstream = combo.map{ case (g,tx,info,i) => {
                (g,tx,info,dbTxVarMap(tx).filter{ p => {
                  p.start >= info.start && nonSynonSevSet.contains(p.severityType)
                }})
              }}
              val dbCodon = dbDownstream.flatMap{ case (g,tx,info,varset) => {
                val xx = varset.filter{ p => {
                  p.start == info.start
                }}
                if(xx.nonEmpty){
                  Some((g,tx,info,xx));
                } else {
                  None
                }
              }}
              val dbCodonStrict = dbCodon.flatMap{ case (g,tx,info,varset) => {
                val xx = varset.filter{ p => {
                  p.start == info.start && p.end == info.end && p.severityType == "NONSYNON" && p.cType == info.cType
                }}
                if(xx.nonEmpty){
                  Some((g,tx,info,xx));
                } else {
                  None
                }
              }}
              
              val dbAmino = dbCodon.flatMap{ case (g,tx,info,varset) => {
                val xx = varset.filter{ p => {
                  (
                      info.subType.startsWith("fs") && p.subType.startsWith("fs")
                  ) || (
                      info.pType == "STOP-LOSS" && p.pType == "STOP-LOSS"
                  ) || (
                      info.pType == "START-LOSS" && p.pType == "START-LOSS"
                  ) || (
                      info.pType == "STOP-GAIN" && p.pType == "STOP-GAIN"
                  ) || (
                      info.severityType == "NONSYNON" && info.pvar == p.pvar
                  )
                }}
                if(xx.nonEmpty){
                  Some((g,tx,info,xx))
                } else {
                  None
                }
              }}
              
              def getInfo(kvr : KnownVarHolder, dbName : String) : Option[(String,Boolean,Boolean)] = {
                 val dbIdx = kvr.sourceList.indexOf(dbName);
                 if(dbIdx != -1){
                   Some((kvr.idlist(dbIdx),kvr.isPatho(dbIdx),kvr.isBen(dbIdx)));
                 } else {
                   None
                 }
              }
              def getId(kvr : KnownVarHolder, dbName : String) : Option[String] = {
                 val dbIdx = kvr.sourceList.indexOf(dbName);
                 if(dbIdx != -1){
                   Some(kvr.idlist(dbIdx));
                 } else {
                   None
                 }
              }
              
              
              dbIterationSet.foreach{ case (dbInfix, dbName,dbSetDesc, dbSetFunc) => {
                val exm = exactMatch.flatMap{ em => {
                  if(dbSetFunc(em)){
                    vb.addInfo(tpre+dbInfix+"_CT_bpMatch","1");
                    if(dbName != ""){
                      val id = getId(em, dbName);
                      vb.addInfo(tpre+dbInfix+"_ID_bpMatch",id.getOrElse("."));
                    }
                    Some(em);
                  } else {
                    vb.addInfo(tpre+dbInfix+"_CT_bpMatch","0");
                    None
                  }
                }}.toSet
                txSetIterationSet.foreach{ case (txSetInfix,txSetDesc,txSetFunc) => {
                  val aam = dbAmino.withFilter{ case (g,tx,info,varset) => {
                    txSetFunc(tx)
                  }}.flatMap{ case (g,tx,info,varset) => {
                    varset.map{mInfo => mInfo.dbInfo.get}.filter{kvh => dbSetFunc(kvh)}
                  }}.toSet
                  val aaIds = aam.flatMap{ kvh => {
                    getId(kvh, dbName)
                  }}
                  val aamDiff = (aam -- exm)
                  val aamDiffIds = aamDiff.flatMap{getId(_,dbName)}
                  
                  

                  if(dbName != "") vb.addInfo(tpre+dbInfix+txSetInfix+"_ID_aaMatch",aaIds.toVector.sorted.padTo(1,".").mkString(","));
                  vb.addInfo(tpre+dbInfix+txSetInfix+"_CT_aaMatch",""+aam.size);
                  if(dbName != "") vb.addInfo(tpre+dbInfix+txSetInfix+"_ID_aaMatch_diffBP",""+aamDiffIds.toVector.sorted.padTo(1,".").mkString(","));
                  vb.addInfo(tpre+dbInfix+txSetInfix+"_CT_aaMatch_diffBP",""+aamDiff.size);

                  val cdm = dbCodonStrict.withFilter{ case (g,tx,info,varset) => {
                    txSetFunc(tx)
                  }}.flatMap{ case (g,tx,info,varset) => {
                    varset.map{mInfo => mInfo.dbInfo.get}.filter{kvh => dbSetFunc(kvh)}
                  }}.toSet
                  val cdIds = cdm.flatMap{ kvh => {
                    getId(kvh, dbName)
                  }}
                  val cdmDiff = (cdm -- aam -- exm)
                  val cdmDiffIds = cdmDiff.flatMap{getId(_,dbName)}
                  
                  if(dbName != "") vb.addInfo(tpre+dbInfix+txSetInfix+"_ID_sameCodon",cdIds.toVector.sorted.padTo(1,".").mkString(","));
                  vb.addInfo(tpre+dbInfix+txSetInfix+"_CT_sameCodon",""+cdm.size);
                  if(dbName != "") vb.addInfo(tpre+dbInfix+txSetInfix+"_ID_sameCodon_diffAA",""+cdmDiffIds.toVector.sorted.padTo(1,".").mkString(","));
                  vb.addInfo(tpre+dbInfix+txSetInfix+"_CT_sameCodon_diffAA",""+cdmDiff.size);
                  
                  val downlof = dbDownstream.flatMap{ case (g,tx,info,varset) => {
                    if(isLOF(info) && txSetFunc(tx)){
                      varset.withFilter{mInfo => {
                        isLOF(mInfo) && dbSetFunc(mInfo.dbInfo.get)
                      }}.map{mInfo => mInfo.dbInfo.get}.toSet
                    } else {
                      Set[KnownVarHolder]()
                    }
                  }}
                  if(dbName != "") vb.addInfo(tpre+dbInfix+txSetInfix+"_ID_downstreamLOF",downlof.flatMap{getId(_, dbName)}.toVector.sorted.padTo(1,".").take(10).mkString(","));
                  vb.addInfo(tpre+dbInfix+txSetInfix+"_CT_downstreamLOF",""+downlof.size);
                  val downlofSameType = dbDownstream.flatMap{ case (g,tx,info,varset) => {
                    if(isLOF(info) && txSetFunc(tx)){
                      varset.withFilter{mInfo => {
                        isLOF(mInfo) && dbSetFunc(mInfo.dbInfo.get) && mInfo.pType == info.pType
                      }}.map{mInfo => mInfo.dbInfo.get}.toSet
                    } else {
                      Set[KnownVarHolder]()
                    }
                  }}
                  if(dbName != "") vb.addInfo(tpre+dbInfix+txSetInfix+"_ID_downstreamLOF_sameType",downlofSameType.flatMap{getId(_, dbName)}.toVector.sorted.padTo(1,".").take(10).mkString(","));
                  vb.addInfo(tpre+dbInfix+txSetInfix+"_CT_downstreamLOF_sameType",""+downlofSameType.size);
                }}
                
              }}
                            
          }
        
        vb;
      }},outHeader);
    }
  }
  
  case class SSummaryACMGWalker(
                   groupFile : Option[String],
                   groupList : Option[String],
                   superGroupList : Option[String],
                   outfile : String
                 ) extends internalUtils.VcfTool.DualFormatVcfWalker {
      var vcfCodes : VCFAnnoCodes = VCFAnnoCodes();
      val sampleToGroupMap = new scala.collection.mutable.AnyRefMap[String,Set[String]](x => Set[String]());
      val groupToSampleMap = new scala.collection.mutable.AnyRefMap[String,Set[String]](x => Set[String]());
      var groupSet : Set[String] = Set[String]();
      
      groupFile match {
        case Some(gf) => {
          val r = getLinesSmartUnzip(gf).drop(1);
          r.foreach(line => {
            val cells = line.split("\t");
            if(cells.length != 2) error("ERROR: group file must have exactly 2 columns. sample.ID and group.ID!");
            groupToSampleMap(cells(1)) = groupToSampleMap(cells(1)) + cells(0);
            sampleToGroupMap(cells(0)) = sampleToGroupMap(cells(0)) + cells(1);
            groupSet = groupSet + cells(1);
          })
        }
        case None => {
          //do nothing
        }
      }
      
      groupList match {
        case Some(g) => {
          val r = g.split(";");
          r.foreach(grp => {
            val cells = grp.split(",");
            val grpID = cells.head;
            cells.tail.foreach(samp => {
              sampleToGroupMap(samp) = sampleToGroupMap(samp) + grpID;
              groupToSampleMap(grpID) = groupToSampleMap(grpID) + samp;
            })
            groupSet = groupSet + grpID;
          })
        }
        case None => {
          //do nothing
        }
      }
      superGroupList match {
        case Some(g) => {
          val r = g.split(";");
          r.foreach(grp => {
            val cells = grp.split(",");
            val grpID = cells.head;
            cells.tail.foreach(subGrp => {
              groupToSampleMap(grpID) = groupToSampleMap(grpID) ++ groupToSampleMap(subGrp);
            })
            groupSet = groupSet + grpID;
          })
        }
        case None => {
          //do nothing
        }
      }
      val groups = groupSet.toVector.sorted;
      reportln("Final Groups:","debug");
      for((g,i) <- groups.zipWithIndex){
          reportln("Group "+g+" ("+groupToSampleMap(g).size+")","debug");
      }
      
      val geneAndTypeCountMap = new scala.collection.mutable.AnyRefMap[String,Map[String,Int]](x => Map[String,Int]().withDefault(s => 0));
      val geneAndTypeCountMap_CANON = new scala.collection.mutable.AnyRefMap[String,Map[String,Int]](x => Map[String,Int]().withDefault(s => 0));
      
      
     /* val variantSampleMap = new scala.collection.mutable.AnyRefMap[Int,Set[String]](x => Set[String]());
      val variantGeneMap = new scala.collection.mutable.AnyRefMap[Int,Set[String]]();
      val variantTypeMap = new scala.collection.mutable.AnyRefMap[Int,String]();
      val variantTypeMap_CANON = new scala.collection.mutable.AnyRefMap[Int,String]();*/
      
      //(geneSet, (level,level_canon), sampleSet)
      var rawVariantInfo = Vector[(Set[String],(String,String),Set[String])]();
      var allGeneSet = Set[String]();
      
     def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
        val sampNames = vcfHeader.titleLine.sampleList.toVector;
        
        return (addIteratorCloseAction(vcIter.zipWithIndex.map{ case (vc,i) => {
           //val geneList = getAttributeSmart(vc,vcfCodes.assess_geneList).toSet.toVector.toSet;
           val geneList : Set[String] = vc.info.getOrElse(vcfCodes.assess_geneList,None) match {
             case Some(glist) => {
               glist.split(",").filter(_ != ".").toSet
             }
             case None => {
               Set[String](); 
             }
           }
           val lvl : String = vc.info.getOrElse(vcfCodes.assess_RATING,None).getOrElse("UNK")
             //vc.getAttributeAsString(vcfCodes.assess_RATING,"UNK");
           val lvl_CANON : String = vc.info.getOrElse(vcfCodes.assess_RATING_CANON,None).getOrElse("UNK")
             //vc.getAttributeAsString(vcfCodes.assess_RATING_CANON,"UNK");
           
           allGeneSet = allGeneSet ++ geneList;
           
           val refAlle = vc.ref
           val altAlleles = vc.alt.zipWithIndex.filter{case (alt,altIdx) => { alt != "*" }}
           if( altAlleles.length != 1 ){
             error("More than one alt allele found!");
           }
           if( altAlleles.head._2 != 0){
             warning("Alt alleIdx != 0","ALTIDX_NE_0",20);
           }
           val alt = altAlleles.head._1;
           
           //variantGeneMap(i) = geneList.toSet;
           //variantTypeMap(i) = 
           val altGenoSampleSet = vc.genotypes.genotypeValues.head.zipWithIndex.filter{ case (g,i) => {
             g.split("[/\\|]").exists(a => a == alt)
           }}.map{ case (g,i) => {
             sampNames(i);
           }}.toSet;
           
           if(vc.filter == "." | vc.filter == "PASS"){
             rawVariantInfo = rawVariantInfo :+ (geneList,(lvl,lvl_CANON), altGenoSampleSet);
           }
           
           vc;
        }},closeAction=() => {
          
          reportln("Filtering VariantInfo..." + getDateAndTimeString,"debug");
          val variantInfo = rawVariantInfo.filter{ case (geneSet,(lvl,lvl_CANON),ss) => {
            lvl == "PATHO" || lvl == "LPATH" || lvl_CANON == "PATHO" || lvl_CANON == "LPATH";
          }}
          reportln("Done filtering VariantInfo. " + getDateAndTimeString,"debug");
          val out = openWriterSmart(outfile);
          val allGenes = allGeneSet.toVector.sorted;
          
          reportln("   geneSampCount_patho..." + getDateAndTimeString,"debug");
          val geneSampCount_patho = allGenes.map{ gene => { 
            val geneVars = variantInfo.filter{ case (geneSet,(lvl,lvl_CANON),ss) => {
              geneSet.contains(gene) && lvl == "PATHO"
            }}
            groups.map{ grp => {
            val grpSS = groupToSampleMap(grp);
            geneVars.flatMap{ case (geneSet,(lvl,lvl_CANON),ss) => {
              ss
            }}.toSet.filter(s => { grpSS.contains(s) }).size;
          }}}}
          reportln("   geneSampCount_patho_canon..." + getDateAndTimeString,"debug");
          val geneSampCount_patho_canon = allGenes.map{ gene => { 
            val geneVars = variantInfo.filter{ case (geneSet,(lvl,lvl_CANON),ss) => {
              geneSet.contains(gene) && lvl_CANON == "PATHO"
            }}
            groups.map{ grp => {
            val grpSS = groupToSampleMap(grp);
            geneVars.flatMap{ case (geneSet,(lvl,lvl_CANON),ss) => {
              ss
            }}.toSet.filter(s => { grpSS.contains(s) }).size;
          }}}}
          reportln("   geneSampCount_lpath..." + getDateAndTimeString,"debug");
          val geneSampCount_lpath = allGenes.map{ gene => { 
            val geneVars = variantInfo.filter{ case (geneSet,(lvl,lvl_CANON),ss) => {
              geneSet.contains(gene) && (lvl == "PATHO" || lvl == "LPATH")
            }}
            groups.map{ grp => {
            val grpSS = groupToSampleMap(grp);
            geneVars.flatMap{ case (geneSet,(lvl,lvl_CANON),ss) => {
              ss
            }}.toSet.filter(s => { grpSS.contains(s) }).size;
          }}}}
          reportln("   geneSampCount_lpath_canon..." + getDateAndTimeString,"debug");
          val geneSampCount_lpath_canon = allGenes.map{ gene => { 
            val geneVars = variantInfo.filter{ case (geneSet,(lvl,lvl_CANON),ss) => {
              geneSet.contains(gene) && (lvl_CANON == "PATHO" || lvl_CANON == "LPATH")
            }}
            groups.map{ grp => {
            val grpSS = groupToSampleMap(grp);
            geneVars.flatMap{ case (geneSet,(lvl,lvl_CANON),ss) => {
              ss
            }}.toSet.filter(s => { grpSS.contains(s) }).size;
          }}}}
          reportln("   sampCounts..." + getDateAndTimeString,"debug");
          
          val sampCount_patho = groups.map{ grp => {
            val grpSS = groupToSampleMap(grp);
            variantInfo.filter{ case (geneSet,(lvl,lvl_CANON),ss) => {
              lvl == "PATHO"
            }}.flatMap{ case (geneSet,(lvl,lvl_CANON),ss) => {
              ss
            }}.toSet.filter(s => { grpSS.contains(s) }).size;
          }}
          val sampCount_lpath = groups.map{ grp => {
            val grpSS = groupToSampleMap(grp);
            variantInfo.filter{ case (geneSet,(lvl,lvl_CANON),ss) => {
              lvl == "PATHO" || lvl == "LPATH";
            }}.flatMap{ case (geneSet,(lvl,lvl_CANON),ss) => {
              ss
            }}.toSet.filter(s => { grpSS.contains(s) }).size;
          }}
          val sampCount_patho_canon = groups.map{ grp => {
            val grpSS = groupToSampleMap(grp);
            variantInfo.filter{ case (geneSet,(lvl,lvl_CANON),ss) => {
              lvl_CANON == "PATHO"
            }}.flatMap{ case (geneSet,(lvl,lvl_CANON),ss) => {
              ss
            }}.toSet.filter(s => { grpSS.contains(s) }).size;
          }}
          val sampCount_lpath_canon = groups.map{ grp => {
            val grpSS = groupToSampleMap(grp);
            variantInfo.filter{ case (geneSet,(lvl,lvl_CANON),ss) => {
              lvl_CANON == "PATHO" || lvl_CANON == "LPATH";
            }}.flatMap{ case (geneSet,(lvl,lvl_CANON),ss) => {
              ss
            }}.toSet.filter(s => { grpSS.contains(s) }).size;
          }}
          reportln("   done with all samp counts..." + getDateAndTimeString,"debug");
          
          out.write("geneID");
          groups.map{ grp => {
            out.write("\t"+Vector("numSamp PATHO","numSamp PATHO CANON","numSamp LPATH","numSamp LPATH CANON").map(grp + _).mkString("\t"));
          }}
          out.write("\n");

          allGenes.zipWithIndex.foreach{ case (gene,i) => {
            out.write(gene);
            groups.zipWithIndex.foreach{ case (grp,grpIdx) => {
             out.write("\t"+
                 geneSampCount_patho(i)(grpIdx)+"\t"+
                 geneSampCount_patho_canon(i)(grpIdx)+"\t"+
                 geneSampCount_lpath(i)(grpIdx)+"\t"+
                 geneSampCount_lpath_canon(i)(grpIdx));
            }}
            out.write("\n");
          }}
          
          out.write("ANY_GENE");
          
          groups.zipWithIndex.foreach{ case (grp,grpIdx) => {
             out.write("\t"+
                 sampCount_patho(grpIdx)+"\t"+
                 sampCount_patho_canon(grpIdx)+"\t"+
                 sampCount_lpath(grpIdx)+"\t"+
                 sampCount_lpath_canon(grpIdx));
          }}
          out.write("\n");
          
          out.close();
          reportln("   done with SummaryACMGWalker. " + getDateAndTimeString,"debug");
        }),vcfHeader);
        
      }
      def walkerName = "summaryACMG"
      def walkerParams : Seq[(String,String)] = Seq();
      
     def oldWalkVCF(vcIter : Iterator[VariantContext], vcfHeader : VCFHeader, verbose : Boolean = true) : (Iterator[VariantContext],VCFHeader) = {
        val sampNames = vcfHeader.getSampleNamesInOrder().asScala.toVector;
        
        val newHeaderLines = List[VCFHeaderLine](); 
        val newHeader = internalUtils.VcfTool.addHeaderLines(vcfHeader,newHeaderLines);
        
        return (addIteratorCloseAction(vcIter.zipWithIndex.map{ case (vc,i) => {
           val geneList = getAttributeSmart(vc,vcfCodes.assess_geneList).toSet.toVector.toSet;
           val lvl = vc.getAttributeAsString(vcfCodes.assess_RATING,"UNK");
           val lvl_CANON = vc.getAttributeAsString(vcfCodes.assess_RATING_CANON,"UNK");
           
           allGeneSet = allGeneSet ++ geneList;
           
           val refAlle = vc.getReference();
           val altAlleles = Range(0,vc.getNAlleles()-1).map((a) => vc.getAlternateAllele(a)).zipWithIndex.filter{case (alt,altIdx) => { alt.getBaseString() != "*" }}
           if( altAlleles.length != 1 ){
             error("More than one alt allele found!");
           }
           if( altAlleles.head._2 != 0){
             warning("Alt alleIdx != 0","ALTIDX_NE_0",20);
           }
           val alt = altAlleles.head._1;
           
           //variantGeneMap(i) = geneList.toSet;
           //variantTypeMap(i) = 
           val altGenoSampleSet = vc.getGenotypes().asScala.iterator.filter(g => {
             g.getAlleles().asScala.exists(a => a.equals(alt))
           }).map(g => {
             g.getSampleName();
           }).toSet;
           
           if(! vc.isFiltered()){
             rawVariantInfo = rawVariantInfo :+ (geneList,(lvl,lvl_CANON), altGenoSampleSet);
           }
           
           vc;
        }},closeAction=() => {
          
          reportln("Filtering VariantInfo..." + getDateAndTimeString,"debug");
          val variantInfo = rawVariantInfo.filter{ case (geneSet,(lvl,lvl_CANON),ss) => {
            lvl == "PATHO" || lvl == "LPATH" || lvl_CANON == "PATHO" || lvl_CANON == "LPATH";
          }}
          reportln("Done filtering VariantInfo. " + getDateAndTimeString,"debug");
          val out = openWriterSmart(outfile);
          val allGenes = allGeneSet.toVector.sorted;
          
          reportln("   geneSampCount_patho..." + getDateAndTimeString,"debug");
          val geneSampCount_patho = allGenes.map{ gene => { 
            val geneVars = variantInfo.filter{ case (geneSet,(lvl,lvl_CANON),ss) => {
              geneSet.contains(gene) && lvl == "PATHO"
            }}
            groups.map{ grp => {
            val grpSS = groupToSampleMap(grp);
            geneVars.flatMap{ case (geneSet,(lvl,lvl_CANON),ss) => {
              ss
            }}.toSet.filter(s => { grpSS.contains(s) }).size;
          }}}}
          reportln("   geneSampCount_patho_canon..." + getDateAndTimeString,"debug");
          val geneSampCount_patho_canon = allGenes.map{ gene => { 
            val geneVars = variantInfo.filter{ case (geneSet,(lvl,lvl_CANON),ss) => {
              geneSet.contains(gene) && lvl_CANON == "PATHO"
            }}
            groups.map{ grp => {
            val grpSS = groupToSampleMap(grp);
            geneVars.flatMap{ case (geneSet,(lvl,lvl_CANON),ss) => {
              ss
            }}.toSet.filter(s => { grpSS.contains(s) }).size;
          }}}}
          reportln("   geneSampCount_lpath..." + getDateAndTimeString,"debug");
          val geneSampCount_lpath = allGenes.map{ gene => { 
            val geneVars = variantInfo.filter{ case (geneSet,(lvl,lvl_CANON),ss) => {
              geneSet.contains(gene) && (lvl == "PATHO" || lvl == "LPATH")
            }}
            groups.map{ grp => {
            val grpSS = groupToSampleMap(grp);
            geneVars.flatMap{ case (geneSet,(lvl,lvl_CANON),ss) => {
              ss
            }}.toSet.filter(s => { grpSS.contains(s) }).size;
          }}}}
          reportln("   geneSampCount_lpath_canon..." + getDateAndTimeString,"debug");
          val geneSampCount_lpath_canon = allGenes.map{ gene => { 
            val geneVars = variantInfo.filter{ case (geneSet,(lvl,lvl_CANON),ss) => {
              geneSet.contains(gene) && (lvl_CANON == "PATHO" || lvl_CANON == "LPATH")
            }}
            groups.map{ grp => {
            val grpSS = groupToSampleMap(grp);
            geneVars.flatMap{ case (geneSet,(lvl,lvl_CANON),ss) => {
              ss
            }}.toSet.filter(s => { grpSS.contains(s) }).size;
          }}}}
          reportln("   sampCounts..." + getDateAndTimeString,"debug");
          
          val sampCount_patho = groups.map{ grp => {
            val grpSS = groupToSampleMap(grp);
            variantInfo.filter{ case (geneSet,(lvl,lvl_CANON),ss) => {
              lvl == "PATHO"
            }}.flatMap{ case (geneSet,(lvl,lvl_CANON),ss) => {
              ss
            }}.toSet.filter(s => { grpSS.contains(s) }).size;
          }}
          val sampCount_lpath = groups.map{ grp => {
            val grpSS = groupToSampleMap(grp);
            variantInfo.filter{ case (geneSet,(lvl,lvl_CANON),ss) => {
              lvl == "PATHO" || lvl == "LPATH";
            }}.flatMap{ case (geneSet,(lvl,lvl_CANON),ss) => {
              ss
            }}.toSet.filter(s => { grpSS.contains(s) }).size;
          }}
          val sampCount_patho_canon = groups.map{ grp => {
            val grpSS = groupToSampleMap(grp);
            variantInfo.filter{ case (geneSet,(lvl,lvl_CANON),ss) => {
              lvl_CANON == "PATHO"
            }}.flatMap{ case (geneSet,(lvl,lvl_CANON),ss) => {
              ss
            }}.toSet.filter(s => { grpSS.contains(s) }).size;
          }}
          val sampCount_lpath_canon = groups.map{ grp => {
            val grpSS = groupToSampleMap(grp);
            variantInfo.filter{ case (geneSet,(lvl,lvl_CANON),ss) => {
              lvl_CANON == "PATHO" || lvl_CANON == "LPATH";
            }}.flatMap{ case (geneSet,(lvl,lvl_CANON),ss) => {
              ss
            }}.toSet.filter(s => { grpSS.contains(s) }).size;
          }}
          reportln("   done with all samp counts..." + getDateAndTimeString,"debug");
          
          out.write("geneID");
          groups.map{ grp => {
            out.write("\t"+Vector("numSamp PATHO","numSamp PATHO CANON","numSamp LPATH","numSamp LPATH CANON").map(grp + _).mkString("\t"));
          }}
          out.write("\n");

          allGenes.zipWithIndex.foreach{ case (gene,i) => {
            out.write(gene);
            groups.zipWithIndex.foreach{ case (grp,grpIdx) => {
             out.write("\t"+
                 geneSampCount_patho(i)(grpIdx)+"\t"+
                 geneSampCount_patho_canon(i)(grpIdx)+"\t"+
                 geneSampCount_lpath(i)(grpIdx)+"\t"+
                 geneSampCount_lpath_canon(i)(grpIdx));
            }}
            out.write("\n");
          }}
          
          out.write("ANY_GENE");
          
          groups.zipWithIndex.foreach{ case (grp,grpIdx) => {
             out.write("\t"+
                 sampCount_patho(grpIdx)+"\t"+
                 sampCount_patho_canon(grpIdx)+"\t"+
                 sampCount_lpath(grpIdx)+"\t"+
                 sampCount_lpath_canon(grpIdx));
          }}
          out.write("\n");
          
          out.close();
          reportln("   done with SummaryACMGWalker. " + getDateAndTimeString,"debug");
        }),newHeader);
      }
    
  }
  
  
  def getAttributeSmart(vc : VariantContext, code : String) : Seq[String] = {
    val attr = vc.getAttributeAsList(code).asScala.toList;
    if(attr.length == 1 && attr.head == "."){
      return Seq[String]();
    } else if(attr.length == 0){
      return Seq[String]();
    } else {
      return attr.map(a => a.toString()).toSeq;
    }
  }
  type StringToBool = (String => Boolean)
//                 txToGeneFile : Option[String],
//
  //clinVarVcf : String,
  //hgmdVarVcf : Option[String] = None,
  // hgmdPathogenicClasses : Seq[String] = Seq("DM"),
  
  case class AssessACMGWalker(
                 chromList : Option[List[String]],
                 clinVarVcf : String,
                 txToGeneFile : Option[String],
                 ctrlAlleFreqKeys : List[String],
                 toleranceFile : Option[String],
                 domainFile : Option[String],
                 conservedElementFile : Option[String],
                 BA1_AF : Double,
                 PM2_AF : Double,
                 rmskFile : Option[String],
                 refSeqFile : Option[String],
                 inSilicoParams : Option[String] = None,
                 inSilicoMergeMethod : String = "smart",
                 inSilicoMinCt : Option[Int] = None,
                 hgmdVarVcf : Option[String] = None,
                 lowMapBed : Option[String] = None,
                 pseudoGeneGTF : Option[String] = None,
                 hgmdPathogenicClasses : Seq[String] = Seq("DM"),
                 includeGenewise : Boolean = true,
                 
                 locusRepetitiveTag : Option[String] = None,
                 locusDomainTag : Option[String]  = None,
                 locusConservedTag : Option[String]  = None,
                 locusMappableTag : Option[String]  = None,
                 locusPseudoTag : Option[String] = None
                 //domainSummaryFile : Option[String] = None
                ) extends internalUtils.VcfTool.DualFormatVcfWalker {
    
    reportln("Creating AssessACMGWalker() ["+stdUtils.getDateAndTimeString+"]","note")
    
    def walkerParams : Seq[(String,String)] = Seq[(String,String)](
          ("chromList",fmtOptionList(chromList)),
          ("clinVarVcf","\""+clinVarVcf+"\""),
          ("txToGeneFile",fmtOption(txToGeneFile)),
          ("ctrlAlleFreqKeys",fmtList(ctrlAlleFreqKeys)),
          ("toleranceFile",fmtOption(toleranceFile)),
          ("domainFile",fmtOption(domainFile)),
          ("conservedElementFile",fmtOption(conservedElementFile)),
          ("BA1_AF",BA1_AF.toString),
          ("PM2_AF",PM2_AF.toString),
          ("rmskFile",fmtOption(rmskFile)),
          ("refSeqFile",fmtOption(refSeqFile)),
          ("inSilicoParams",fmtOption(inSilicoParams)),
          ("inSilicoMergeMethod",inSilicoMergeMethod),
          ("inSilicoMinCt",fmtOption(inSilicoMinCt)),
          ("hgmdVarVcf",fmtOption(hgmdVarVcf)),
          ("lowMapBed",fmtOption(lowMapBed)),
          ("pseudoGeneGTF",fmtOption(pseudoGeneGTF)),
          ("hgmdPathogenicClasses",fmtList(hgmdPathogenicClasses)),
          ("includeGenewise",includeGenewise.toString)
        );

    
    /*val inSilicoKeysOpt : Option[Seq[(String,Set[String],Set[String])]] = inSilicoParams match {
      case Some(isk) => {
        Some(isk.split(",").toSeq.map(s => {
          val cells = s.split(":");
          if(cells.length != 3){
            error("Malformed input parameter: each comma-delimited element in inSilicoKeys must have 3 colon-delimited parts!");
          }
          (cells(0),cells(1).split("\\|").toSet,cells(2).split("\\|").toSet);
        }))
      }
      case None => None;
    }*/
    val inSilicoFuns : Seq[(String, StringToBool,StringToBool)] = inSilicoParams match {
      case Some(isk) => {
        isk.split(",").toSeq.map(iskString => {
          val cells = iskString.split(":");
         // if(cells.length <= 2){
         //   error("Malformed input parameter: each comma-delimited element in inSilicoKeys must have 3 or more colon-delimited parts!");
         // }
          if(cells.length != 4){
              error("Malformed input parameter: each comma-delimited element in inSilicoKeys must have 4 colon-delimited parts!");
          }
          if(cells(1) == "eq"){
            val damKeys = cells(2).split("\\|").toSet;
            val benKeys = cells(3).split("\\|").toSet;
            (cells(0), ((s : String) => {damKeys.contains(s)}), ((s : String) => {benKeys.contains(s)}))
          } else if(cells(1) == "ge"){
            val damThresh = string2double(cells(2))
            val benThresh = string2double(cells(3))
            (cells(0), ((s : String) => {s != "." && string2double(s) >= damThresh}), ((s : String) => {s != "." && string2double(s) <= benThresh}))
          } else if(cells(1) == "le"){
            val damThresh = string2double(cells(2))
            val benThresh = string2double(cells(3))
            (cells(0), ((s : String) => {s != "." && string2double(s) <= damThresh}), ((s : String) => {s != "." && string2double(s) >= benThresh}))
          } else {
            error("UNKNOWN INSILICO FUNCTION PARAM: \""+cells(1)+"\"! Options are: in, eq, ge, le.");
            null;
          }
        })
      }
      case None => Seq();
    }
    
    reportln("   Applying In-Silico tags: \""+inSilicoFuns.map{ case (s,f1,f2) => s }.mkString("\",\"")+"\"","debug");
    
    val SinSilicoSummary : (SVcfVariantLine) => (String,Seq[String]) = if(inSilicoFuns.length > 0){
      if(inSilicoMergeMethod == "smart"){
        (vc : SVcfVariantLine) => {
          val statusSeq : Seq[(Boolean,Boolean,String)] = inSilicoFuns.map{ case (tag,damFun,benFun) => {  
            val attr = vc.getInfoList(tag).filter(_ != ".");
            val (d,b) = (attr.exists(p => damFun(p)), attr.exists(p => benFun(p)));
            val s = if(d && !b) "Damaging";
            else if(b && !d) "Benign";
            else if(b && d) "Ambiguous";
            else "Unknown";
            (d,b,s);
          }};
          val anyDam = statusSeq.exists{ case (damBool,benBool,s) => damBool};
          val anyBen = statusSeq.exists{ case (damBool,benBool,s) => benBool};
          val status = if(anyDam && (! anyBen)){
            "Damaging"
          } else if(anyBen && (! anyDam)){
            "Benign"
          } else if(anyBen && anyDam){
            "Ambiguous"
          } else {
            "Unknown"
          }
          (status,statusSeq.map{_._3});
        }
      } else {
        (vc : SVcfVariantLine) => {
          val statusSeq : Seq[(Boolean,Boolean,String)] = inSilicoFuns.map{ case (tag,damFun,benFun) => {
            val attr = vc.getInfoList(tag).filter(_ != ".");
            val (d,b) = (attr.exists(p => damFun(p)), attr.exists(p => benFun(p)));
            val s = if(d && !b) "Damaging";
            else if(b && !d) "Benign";
            else if(b && d) "Ambiguous";
            else "Unknown";
            (d,b,s);
          }};
          val allDam = ! statusSeq.exists{ case (damBool,benBool,s) => ! damBool};
          val allBen = ! statusSeq.exists{ case (damBool,benBool,s) => ! benBool};
          val status = if(allDam && (! allBen)){
            "Damaging"
          } else if(allBen && (! allDam)){
            "Benign"
          } else if(allDam && allBen){
            "Ambiguous"
          } else {
            "Unknown"
          }
          (status,statusSeq.map{_._3});
        }
      }
    } else {
      (vc : SVcfVariantLine) => (".",Seq());
    }
    
    val inSilicoSummary : (VariantContext) => (String,Seq[String]) = if(inSilicoFuns.length > 0){
      if(inSilicoMergeMethod == "smart"){
        (vc : VariantContext) => {
          val statusSeq : Seq[(Boolean,Boolean,String)] = inSilicoFuns.map{ case (tag,damFun,benFun) => {  
            val attr = vc.getAttributeAsList(tag).asScala.toList.map{_.toString()}.filter(_ != ".");
            val (d,b) = (attr.exists(p => damFun(p)), attr.exists(p => benFun(p)));
            val s = if(d && !b) "Damaging";
            else if(b && !d) "Benign";
            else if(b && d) "Ambiguous";
            else "Unknown";
            (d,b,s);
          }};
          val anyDam = statusSeq.exists{ case (damBool,benBool,s) => damBool};
          val anyBen = statusSeq.exists{ case (damBool,benBool,s) => benBool};
          val status = if(anyDam && (! anyBen)){
            "Damaging"
          } else if(anyBen && (! anyDam)){
            "Benign"
          } else if(anyBen && anyDam){
            "Ambiguous"
          } else {
            "Unknown"
          }
          (status,statusSeq.map{_._3});
        }
      } else {
        (vc : VariantContext) => {
          val statusSeq : Seq[(Boolean,Boolean,String)] = inSilicoFuns.map{ case (tag,damFun,benFun) => {
            val attr = vc.getAttributeAsList(tag).asScala.toList.map{_.toString()}.filter(_ != ".");
            val (d,b) = (attr.exists(p => damFun(p)), attr.exists(p => benFun(p)));
            val s = if(d && !b) "Damaging";
            else if(b && !d) "Benign";
            else if(b && d) "Ambiguous";
            else "Unknown";
            (d,b,s);
          }};
          val allDam = ! statusSeq.exists{ case (damBool,benBool,s) => ! damBool};
          val allBen = ! statusSeq.exists{ case (damBool,benBool,s) => ! benBool};
          val status = if(allDam && (! allBen)){
            "Damaging"
          } else if(allBen && (! allDam)){
            "Benign"
          } else if(allDam && allBen){
            "Ambiguous"
          } else {
            "Unknown"
          }
          (status,statusSeq.map{_._3});
        }
      }
    } else {
      (vc : VariantContext) => (".",Seq());
    }
    
    reportln("Reading txToGene map... ["+stdUtils.getDateAndTimeString+"]","debug");
    val txToGene : (String => String) = txToGeneFile match {
      case Some(f) => {
        val txToGeneMap = getLinesSmartUnzip(f).map(line => {
          val cells = line.split("\t");
          (cells(0),cells(1));
        }).toMap
        ((s : String) => txToGeneMap.getOrElse(s,s));
      }
      case None => {
        ((s : String) => s);
      }
    }
    reportln("Reading geneToTX map... ["+stdUtils.getDateAndTimeString+"]","debug");

    val geneToTx : (String => Set[String]) = txToGeneFile match {
      case Some(f) => {
        val geneToTxMap = scala.collection.mutable.AnyRefMap[String,Set[String]]().withDefault(x => Set[String]());
        getLinesSmartUnzip(f).foreach(line => {
          val cells = line.split("\t");
          geneToTxMap(cells(1)) = geneToTxMap(cells(1)) + cells(0);
        })
        ((s : String) => geneToTxMap.getOrElse(s,Set[String]()));
      }
      case None => {
        ((s : String) => Set[String](s));
      }
    }
    
    reportln("Reading refSeq map... ["+stdUtils.getDateAndTimeString+"]","debug");
    
    val isRefSeq : (String => Boolean) = refSeqFile match {
      case Some(f) => {
        val lines = getLinesSmartUnzip(f);
        val table = getTableFromLines(lines,colNames = Seq("transcript"), errorName = "File "+f);
        var refSeqSet : Set[String] = table.map(tableCells => {
          val tx = tableCells(0);
          tx
        }).toSet
        reportln("   found: "+refSeqSet.size+" RefSeq transcripts.","debug");
        ((s : String) => {
          refSeqSet.contains(s);
        })
      }
      case None => {
        ((s : String) => {
          false;
        })
      }
    }
    
//getClinVarVariants(infile : String, chromList : Option[List[String]], vcfCodes : VCFAnnoCodes = VCFAnnoCodes()) : scala.collection.Map[String,Set[(String,internalUtils.TXUtil.pVariantInfo)]]
    /****************************************
     * GENE MAPS:
     */ 
    reportln("Reading gene maps... ["+stdUtils.getDateAndTimeString+"]","debug");
    val geneIsLofSensitive : (String => Boolean) = toleranceFile match {
      case Some(f) => {
        val lines = getLinesSmartUnzip(f);
        val headerCells = lines.next.split("\\s+");
        val geneCol = headerCells.indexWhere(headerCell => headerCell == "geneID");
        val lofCol = headerCells.indexWhere(headerCell => headerCell == "LOFtolerant");
        var lofTolerantGeneList = Set[String]();
        while(lines.hasNext){
          val cells = lines.next().split("\\s+");
          if(cells(lofCol) == "1"){
            lofTolerantGeneList = lofTolerantGeneList + cells(geneCol);
          }
        }
        ((s : String) => {
          ! lofTolerantGeneList.contains(s);
        })
      }
      case None => {
        ((s : String) => true)        
      }
    }
    
    val geneIsMisSensitive : (String => Boolean) = toleranceFile match {
      case Some(f) => {
        val lines = getLinesSmartUnzip(f);
        val headerCells = lines.next.split("\\s+");
        val geneCol = headerCells.indexWhere(headerCell => headerCell == "geneID");
        val tolCol = headerCells.indexWhere(headerCell => headerCell == "MIStolerant");
        var tolerantGeneList = Set[String]();
        while(lines.hasNext){
          val cells = lines.next().split("\\s+");
          if(cells(tolCol) == "1"){
            tolerantGeneList = tolerantGeneList + cells(geneCol);
          }
        }
        ((s : String) => {
          ! tolerantGeneList.contains(s);
        })
      }
      case None => {
        ((s : String) => true)        
      }
    }
    
    val geneIsMisInsensitive : (String => Boolean) = ((s : String) => { ! geneIsMisSensitive(s); })
    reportln("done with gene maps...["+stdUtils.getDateAndTimeString+"]","debug");
    //val geneIsMisSensitive : (String => Boolean) = ((s : String) => true)
    //val geneIsMisInsensitive : (String => Boolean) = ((s : String) => false)
    
    /****************************************
     * LOCUS MAPS:
     */
    val conservedArray = conservedElementFile match {
      case Some(f) => {
        reportln("reading conserved locus file ["+stdUtils.getDateAndTimeString+"]","debug");
        val arr : genomicAnnoUtils.GenomicArrayOfSets[String] = genomicAnnoUtils.GenomicArrayOfSets[String](false);
        val lines = getLinesSmartUnzip(f);
        
        lines.foreach(line => {
          val cells = line.split("\t");
          val (chrom,start,end) = (cells(0),string2int(cells(1)),string2int(cells(2)))
          arr.addSpan(commonSeqUtils.GenomicInterval(chrom, '.', start,end), "CE");
        })
        arr.finalizeStepVectors;
        reportln("done with conserved locus file ["+stdUtils.getDateAndTimeString+"]","debug");
        Some(arr);
      }
      case None => {
        None;
      }
    }
    val locusIsConserved  : (commonSeqUtils.GenomicInterval => Boolean) = conservedArray match {
      case Some(arr) => {
        (iv : commonSeqUtils.GenomicInterval) => {
          ! arr.findIntersectingSteps(iv).foldLeft(Set[String]()){case (soFar,(iv,currSet)) => {
            soFar ++ currSet;
          }}.isEmpty
        }
      }
      case None => {
        (iv : commonSeqUtils.GenomicInterval) => {
          true;
        }
      }
    }
    
    val pseudogeneArray = pseudoGeneGTF match {
      case Some(f) => {
        reportln("reading pseudogene locus file ["+stdUtils.getDateAndTimeString+"]","debug");
        val arr : genomicAnnoUtils.GenomicArrayOfSets[String] = genomicAnnoUtils.GenomicArrayOfSets[String](false);
        val lines = getLinesSmartUnzip(f);
        
        lines.dropWhile(line => line.startsWith("#")).foreach(line => {
          val cells = line.split("\t");
          val (chrom,start,end) = (cells(0),string2int(cells(3)) - 1,string2int(cells(4)))
          arr.addSpan(commonSeqUtils.GenomicInterval(chrom, '.', start,end), "CE");
        })
        arr.finalizeStepVectors;
        reportln("done with conserved locus file ["+stdUtils.getDateAndTimeString+"]","debug");
        Some(arr);
      }
      case None => {
        None;
      }
    }
    val locusIsPseudogene : Option[(commonSeqUtils.GenomicInterval => Boolean)] = pseudogeneArray match {
      case Some(arr) => {
        Some(
               (iv : commonSeqUtils.GenomicInterval) => {
                 arr.findIntersectingSteps(iv).exists{ case (iv,currSet) => ! currSet.isEmpty}
                 //! arr.findIntersectingSteps(iv).foldLeft(Set[String]()){case (soFar,(iv,currSet)) => {
                 //  soFar ++ currSet;
                 //}}.isEmpty
               }
            );
      }
      case None => {
        None
      }
    }
    
    val lowMapArray = lowMapBed match {
      case Some(f) => {
        reportln("reading mappability locus file ["+stdUtils.getDateAndTimeString+"]","debug");
        val arr : genomicAnnoUtils.GenomicArrayOfSets[String] = genomicAnnoUtils.GenomicArrayOfSets[String](false);
        val lines = getLinesSmartUnzip(f);
        
        lines.foreach(line => {
          val cells = line.split("\t");
          val (chrom,start,end) = (cells(0),string2int(cells(1)),string2int(cells(2)))
          arr.addSpan(commonSeqUtils.GenomicInterval(chrom, '.', start,end), "CE");
        })
        arr.finalizeStepVectors;
        reportln("done with conserved locus file ["+stdUtils.getDateAndTimeString+"]","debug");
        Some(arr);
      }
      case None => {
        None;
      }
    }
    val locusIsMappable : Option[(commonSeqUtils.GenomicInterval => Boolean)] = lowMapArray match {
      case Some(arr) => {
        Some((iv : commonSeqUtils.GenomicInterval) => {
          arr.findIntersectingSteps(iv).foldLeft(Set[String]()){case (soFar,(iv,currSet)) => {
            soFar ++ currSet;
          }}.isEmpty
        })
      }
      case None => {
        None
      }
    }
    
    val locusArray = domainFile match {
      case Some(f) => {
        reportln("reading domain locus file ["+stdUtils.getDateAndTimeString+"]","debug");
        val lines = getLinesSmartUnzip(f);
        val headerCells = lines.next.split("\t");
        val chromCol = 0;
        val startCol = headerCells.indexWhere(headerCell => headerCell == "start");
        val endCol = headerCells.indexWhere(headerCell => headerCell == "end");
        val domainIdCol = if(headerCells.contains("domainUID")) headerCells.indexWhere(headerCell => headerCell == "domainUID");
                          else                                  headerCells.indexWhere(headerCell => headerCell == "domainID");
        
        val geneArray : genomicAnnoUtils.GenomicArrayOfSets[String] = genomicAnnoUtils.GenomicArrayOfSets[String](false);
        
        while(lines.hasNext){
          val cells = lines.next.split("\t");
          val (chrom,start,end,domainID) = (cells(chromCol),string2int(cells(startCol)),string2int(cells(endCol)),cells(domainIdCol))
          geneArray.addSpan(commonSeqUtils.GenomicInterval(chrom, '.', start,end), domainID);
        }
        geneArray.finalizeStepVectors;
        reportln("done with domain locus file. ["+stdUtils.getDateAndTimeString+"]","debug");
        Some(geneArray);
      }
      case None => {
        None;
      }
    }
    
    val locusDomains : ((commonSeqUtils.GenomicInterval) => Set[String]) = locusArray match {
      case Some(arr) => {
        (iv : commonSeqUtils.GenomicInterval) => {
          arr.findIntersectingSteps(iv).foldLeft(Set[String]()){case (soFar,(iv,currSet)) => {
            soFar ++ currSet;
          }}
        }
      }
      case None => {
        (iv : commonSeqUtils.GenomicInterval) => {
          Set[String]("UNK")
        }
      }
    }
    
    
    
    val locusIsHotspot    : (commonSeqUtils.GenomicInterval => Boolean) = (iv : commonSeqUtils.GenomicInterval) => {
      ! locusDomains(iv).isEmpty;
    }  //((s : String,i : Int) => false)

    
    val repLocusArray = rmskFile match {
      case Some(f) => {
        reportln("reading rmsk file ["+stdUtils.getDateAndTimeString+"]","debug");
        val geneArray : genomicAnnoUtils.GenomicArrayOfSets[String] = genomicAnnoUtils.GenomicArrayOfSets[String](false);
        getLinesSmartUnzip(f).foreach(line => {
          val cells = line.split("\t");
          val chrom = cells(5);
          val start = string2int(cells(6));
          val end = string2int(cells(7));
          geneArray.addSpan(commonSeqUtils.GenomicInterval(chrom,'.',start,end),cells(11));
        })
        geneArray.finalizeStepVectors;
        reportln("done with rmsk file ["+stdUtils.getDateAndTimeString+"]","debug");
        Some(geneArray);
      }
      case None => {
        None;
      }
    }
    
    val locusIsRepetitive : (commonSeqUtils.GenomicInterval => Boolean) = repLocusArray match {
      case Some(arr) => {
        ((iv : commonSeqUtils.GenomicInterval) => {
          ! arr.findIntersectingSteps(iv).foldLeft(Set[String]()){case (soFar,(iv,currSet)) => { soFar ++ currSet }}.isEmpty;
        })
      }
      case None => {
        ((iv : commonSeqUtils.GenomicInterval) => false)
      }
    }
    
    /****************************************
     * WALK FCN:
     */
    
    var vcfCodes : VCFAnnoCodes = VCFAnnoCodes();
    
    /*def calcDomainSummary(domainSummaryFile : String, 
                          locusArray : GenomicArrayOfSets[String],
                          locusDomains : ((commonSeqUtils.GenomicInterval) => Set[String]),
                          clinVarVariantSet : scala.collection.Map[String,(String,Int,String)],
                          clinVarVariants : scala.collection.Map[String,Set[internalUtils.TXUtil.pVariantInfo]]){
      
    }*/
    
      val (clinVarVariantSet,clinVarVariants) : (scala.collection.mutable.Map[String,TXUtil.KnownVariantInfoHolder],scala.collection.mutable.Map[String,Set[internalUtils.TXUtil.pVariantInfo]]) = 
        getFullClinVarVariants(infile=clinVarVcf,chromList =chromList, vcfCodes = vcfCodes, hgmdVarVcf = hgmdVarVcf,hgmdPathogenicClasses=hgmdPathogenicClasses);
       //getFullClinVarVariants(infile=clinVarVcf,chromList =chromList, vcfCodes = veeOneSeven_vcfAnnoCodes, hgmdVarVcf = hgmdVarVcf,hgmdPathogenicClasses=hgmdPathogenicClasses);
      
      val clinVarVariants_StopLoss_Patho = clinVarVariants.map{ case (tx,varSet) => {
        ((tx,varSet.flatMap{case pvar => {
          if( pvar.isPatho ){
            None;
          } else if(pvar.pType == "STOP-LOSS") {
            Some(pvar);
          } else {
            None;
          }
        }}))
      }}.withDefaultValue(Set());
      val clinVarVariants_StopLoss_Benign = clinVarVariants.map{ case (tx,varSet) => {
        ((tx,varSet.flatMap{case pvar => {
          if( ! pvar.isBenign ){
            None;
          } else if(pvar.pType == "STOP-LOSS") {
            Some(pvar);
          } else {
            None;
          }
        }}))
      }}.withDefaultValue(Set());
      
      val clinVarVariants_LOFSet = clinVarVariants.map{ case (tx,varSet) => {
        (tx,varSet.filter{ v => {
          v.severityType == "LLOF"  // && (v.CLNSIG == 4 || v.CLNSIG == 5)
        }})
      }}.withDefaultValue(Set());
      val clinVarVariants_startLoss = clinVarVariants.map{ case (tx,varSet) => {
        (tx,varSet.filter{ v => {
          v.pType == "START-LOSS"  // && (v.CLNSIG == 4 || v.CLNSIG == 5)
        }})
      }}.withDefaultValue(Set());
      val clinVarVariants_stopLoss = clinVarVariants.map{ case (tx,varSet) => {
        (tx,varSet.filter{ v => {
          v.pType == "STOP-LOSS"  // && (v.CLNSIG == 4 || v.CLNSIG == 5)
        }})
      }}.withDefaultValue(Set());
      
      
    def walkerName : String = "CalcACMGVar"

      
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {

      val newHeader = vcfHeader.copyHeader;
      newHeaderLines.foreach{ infoLine => {
          newHeader.addInfoLine(convertInfoLineFormat(infoLine));
      }}
      newHeader.addWalk(this);
      reportln("Walking input VCF...","note")
      return ( 
      vcMap(vcIter)(v => {
        
        
        val iv = commonSeqUtils.GenomicInterval(v.chrom,'.', start = v.pos - 1, end = v.pos + math.max(v.ref.length,1));
        
        val isRepetitive = getOrFunc(locusRepetitiveTag,locusIsRepetitive(iv))(t => {
          (v.info(t) match {
            case Some(v) => v == "1";
            case None => {
              warning("Missing isRepetetive tag: \""+t+"\". Assuming 0","MISSING_TAG_"+t,10)
              false
            }
          })
        })
        val isConserved  = getOrFunc(locusConservedTag,locusIsConserved(iv))(t => {
          (v.info(t) match {
            case Some(v) => v == "1";
            case None => {
              warning("Missing locusConservedTag: \""+t+"\". Assuming 1","MISSING_TAG_"+t,10)
              true
            }
          })
        })
        val isHotspot  = getOrFunc(locusDomainTag,locusIsHotspot(iv))(t => {
          (v.info(t) match {
            case Some(v) => v == "1";
            case None => {
              warning("Missing locusDomainTag: \""+t+"\". Assuming 0","MISSING_TAG_"+t,10)
              false
            }
          })
        })
        val isMappable  = getOrFunc(locusMappableTag,locusIsMappable.getOrElse( (iv : commonSeqUtils.GenomicInterval) => true )(iv))(t => {
          (v.info(t) match {
            case Some(v) => v == "1";
            case None => {
              warning("Missing locusMappableTag: \""+t+"\". Assuming 1","MISSING_TAG_"+t,10)
              true
            }
          })
        })
        val isPseudo  = getOrFunc(locusPseudoTag,locusIsPseudogene.getOrElse( (iv : commonSeqUtils.GenomicInterval) => true )(iv))(t => {
          (v.info(t) match {
            case Some(v) => v == "1";
            case None => {
              warning("Missing locusPseudoTag: \""+t+"\". Assuming 0","MISSING_TAG_"+t,10)
              false
            }
          })
        })
        
        SassessVariant(v=v,
            clinVarVariants=clinVarVariants,
            txToGene = txToGene, geneToTx = geneToTx,
            geneIsLofSensitive = geneIsLofSensitive,
            geneIsMisSensitive = geneIsMisSensitive,
            geneIsMisInsensitive = geneIsMisInsensitive,
            isRefSeq = isRefSeq,
            isRepetitive = isRepetitive,
            isConserved = isConserved,
            isHotspot = isHotspot,
            isMappable = isMappable,
            isPseudogene = isPseudo,
            ctrlAlleFreqKeys = ctrlAlleFreqKeys,
            //inSilicoKeysOpt = inSilicoKeysOpt,
            //inSilicoMin = inSilicoMin,
            inSilicoFuns = inSilicoFuns,inSilicoSummary=SinSilicoSummary,
            BA1_AF = BA1_AF,
            PM2_AF = PM2_AF,
            clinVarVariantSet = clinVarVariantSet,
            clinVarVariants_LOFSet = clinVarVariants_LOFSet,
            clinVarVariants_startLoss = clinVarVariants_startLoss,
            clinVarVariants_stopLoss =clinVarVariants_stopLoss,
            vcfCodes = vcfCodes)
       }), newHeader );
      
          /*
                 locusRepetitiveTags : Option[List[String]] = None,
                 locusDomainTags : Option[List[String]]  = None,
                 locusConservedTags : Option[List[String]]  = None,
                 locusMappableTags : Option[List[String]]  = None,
                 locusPseudoTags : Option[List[String]] = None
           * 
           */
    }
    
    val newHeaderLinesWithCanonVer : List[VCFInfoHeaderLine] = List[VCFInfoHeaderLine](
            new VCFInfoHeaderLine(vcfCodes.assess_ACMG_numGenes, 1, VCFHeaderLineType.Integer,   "Number of genes this variant is on or near."),
            new VCFInfoHeaderLine(vcfCodes.assess_LofGenes, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,    "A list of genes for which this variant causes loss-of-function (fs, early-stop, start-loss)"),
            new VCFInfoHeaderLine(vcfCodes.assess_MisGenes, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,    "A list of genes for which this variant causes a missense."),
            new VCFInfoHeaderLine(vcfCodes.assess_CodingGenes, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,    "A list of genes for which this variant is located over a coding region."),
            new VCFInfoHeaderLine(vcfCodes.assess_NonsynonGenes, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,    "A list of genes for which this variant causes a change in the predicted amino acid change.")
        )
   
      val newHeaderLines :  List[VCFInfoHeaderLine] = 
        newHeaderLinesWithCanonVer ++ 
        newHeaderLinesWithCanonVer.map{ hl => {
          (if(hl.getCountType ==  VCFHeaderLineCount.INTEGER){
             new VCFInfoHeaderLine(hl.getID()+"_CANON", hl.getCount(), hl.getType(),   "(Considering canonical TX only) " + hl.getDescription())
          } else {
             new VCFInfoHeaderLine(hl.getID()+"_CANON", hl.getCountType(), hl.getType(),   "(Considering canonical TX only) " + hl.getDescription())
          })
        }} ++ List[VCFInfoHeaderLine](
            new VCFInfoHeaderLine(vcfCodes.assess_IsRepetitive, 1, VCFHeaderLineType.Integer,    "Indicates whether the variant intersects with a repetitive region, as defined by repeatmasker (derived from rmsk.txt, downloaded from ucsc, APR 2017)"),
            new VCFInfoHeaderLine(vcfCodes.assess_IsConserved, 1, VCFHeaderLineType.Integer,    "Indicates whether the variant intersects with a disproportionately-conserved region (as defined by GERPplusplus)."),
            new VCFInfoHeaderLine(vcfCodes.assess_IsHotspot, 1, VCFHeaderLineType.Integer,    "Indicates whether the variant intersects with a known domain."),
            new VCFInfoHeaderLine(vcfCodes.assess_IsMappable, 1, VCFHeaderLineType.Integer,    "Indicates whether the variant intersects with a low-mappability region (mappability less than 1 in the UCSC wgEncodeCrgMapabilityAlign100mer.bw database file)"),

            //new VCFInfoHeaderLine(vcfCodes.assess_domain, 1, VCFHeaderLineType.Integer,    ""),
            //new VCFInfoHeaderLine(vcfCodes.assess_criteria, 1, VCFHeaderLineType.Integer,    ""),
            //new VCFInfoHeaderLine(vcfCodes.assess_GeneSenseLOF, 1, VCFHeaderLineType.Integer,    ""),
            //new VCFInfoHeaderLine(vcfCodes.assess_GeneSenseMis, 1, VCFHeaderLineType.Integer,    ""),
            //new VCFInfoHeaderLine(vcfCodes.assess_ctrlAFMIN, 1, VCFHeaderLineType.Float,    ""),
            new VCFInfoHeaderLine(vcfCodes.assess_ctrlAFMAX, 1, VCFHeaderLineType.Float,    "The highest alt allele frequency found across the control datasets ("+ctrlAlleFreqKeys.mkString(",")+")"),

            new VCFInfoHeaderLine(vcfCodes.assess_geneList, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,    "A list of genes that this variant is on or near."),
            
            new VCFInfoHeaderLine(vcfCodes.assess_geneTxList, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,    "For each gene that this variant is on or near, a list of all transcripts belonging to that gene (this is all TX, including TX that this variant is not on or near)."),
            new VCFInfoHeaderLine(vcfCodes.assess_geneLofTxRatio, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,    "For each gene that this variant overlaps with, the number of transcripts that lose function due to this variant slash the total number of transcripts belonging to that gene."),
            new VCFInfoHeaderLine(vcfCodes.assess_geneMisTxRatio, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,    "For each gene that this variant overlaps with, the number of transcripts that contain coding missense changes due to this variant slash the total number of transcripts belonging to that gene."),
            
            //new VCFInfoHeaderLine(vcfCodes.assess_refSeqLOF, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.Integer, "List of genes in which the Canonical isoform is loss-of-function due to this variant."),
            //new VCFInfoHeaderLine(vcfCodes.assess_refSeqMis, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.Integer, "List of genes in which the Canonical isoform contains a missense due to this variant"),
            new VCFInfoHeaderLine(vcfCodes.assess_refSeqKnown, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String, "For each gene that this variant overlaps with, how many canonical transcripts were found (should always be 1 for all genes)."),
            
            new VCFInfoHeaderLine(vcfCodes.assess_LofTX, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,    "A list of transcripts for which this variant causes loss-of-function (fs, early-stop, start-loss)"),
            new VCFInfoHeaderLine(vcfCodes.assess_MisTX, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,    "A list of transcripts for which this variant causes a missense."),
            new VCFInfoHeaderLine(vcfCodes.assess_CodingTX, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,    "A list of transcripts for which this variant is located in a coding region."),
            new VCFInfoHeaderLine(vcfCodes.assess_NonsynonTX, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,    "A list of transcripts for which this variant causes a change in the predicted amino acid sequence."),
            
            
            new VCFInfoHeaderLine(vcfCodes.assess_PVS1, 1, VCFHeaderLineType.Integer,    "Loss-of-function variant in gene that is LOF-sensitive. Loss-of-function is defined as one of:"+
                                                                                         "stop-gain, frameshift, total-gene-indel, or splice junction indel. "+
                                                                                         "A gene is defined as LOF-sensitive if at least 10pct of pathogenic "+
                                                                                         "clinvar variants are LOF-type variants."),
            new VCFInfoHeaderLine(vcfCodes.assess_PS1, 1, VCFHeaderLineType.Integer,      "Variant has the same amino acid change as a pathogenic variant from ClinVar."),
            new VCFInfoHeaderLine(vcfCodes.assess_PM1, 1, VCFHeaderLineType.Integer,      "Located in a known domain. (currently just any domain)"),
            new VCFInfoHeaderLine(vcfCodes.assess_PM2, 1, VCFHeaderLineType.Integer,      "Alt allele frequency less than or equal to "+PM2_AF+" in all control datasets ("+ctrlAlleFreqKeys.mkString(",")+")"),
            new VCFInfoHeaderLine(vcfCodes.assess_PM4, 1, VCFHeaderLineType.Integer,      "Protein length change in nonrepeat region OR stop-loss variant."),
            new VCFInfoHeaderLine(vcfCodes.assess_PM5, 1, VCFHeaderLineType.Integer,      "Novel missense variant change at amino acid where a different amino acid change is known pathogenic in ClinVar."),
            new VCFInfoHeaderLine(vcfCodes.assess_PP2, 1, VCFHeaderLineType.Integer,      "Missense variant in gene that is missense-sensitive (missense variants are at least 10pct of known pathogenic variants in clinvar)."),
            new VCFInfoHeaderLine(vcfCodes.assess_BP1, 1, VCFHeaderLineType.Integer,      "Missense variant in gene that is NOT missense-sensitive (less than 10pct of pathogenic variants are missense)"),
            new VCFInfoHeaderLine(vcfCodes.assess_BP3, 1, VCFHeaderLineType.Integer,      "In-frame indels in a repetitive region that does NOT overlap with any known domain."),
            new VCFInfoHeaderLine(vcfCodes.assess_BP7, 1, VCFHeaderLineType.Integer,      "Synonymous variant that does NOT intersect with a conserved element region."),
            new VCFInfoHeaderLine(vcfCodes.assess_BA1,    1, VCFHeaderLineType.Integer,   "Allele frequency greater than 5 percent in one or more control dataset ("+ctrlAlleFreqKeys.mkString(",")+")"),
            new VCFInfoHeaderLine(vcfCodes.assess_RATING, 1, VCFHeaderLineType.String,    "ACMG Pathogenicity rating: PATHO - pathogenic. LPATH - likely pathogenic, VUS - variant, unknown significance, LB - likely benign, B - benign."),
            new VCFInfoHeaderLine(vcfCodes.assess_WARNFLAG, 1, VCFHeaderLineType.Integer, "Whether or not there is anything odd about this variant that may require manual inspection."),
            new VCFInfoHeaderLine(vcfCodes.assess_WARNINGS, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,    "List of warnings concerning this variant."),
            
            new VCFInfoHeaderLine(vcfCodes.assess_PVS1_CANON, 1, VCFHeaderLineType.Integer,    "(Considering the canonical TX only) Loss-of-function variant in gene that is LOF-sensitive. Loss-of-function is defined as one of:"+
                                                                                         "stop-gain, frameshift, total-gene-indel, or splice junction indel. "+
                                                                                         "A gene is defined as LOF-sensitive if at least 10pct of pathogenic "+
                                                                                         "clinvar variants are LOF-type variants."),
            new VCFInfoHeaderLine(vcfCodes.assess_PS1_CANON, 1, VCFHeaderLineType.Integer,"(Considering the canonical TX only) Variant has the same amino acid change as a pathogenic variant from ClinVar."),
            new VCFInfoHeaderLine(vcfCodes.assess_PP2_CANON, 1, VCFHeaderLineType.Integer,"(Considering the canonical TX only) Missense variant in gene that is missense-sensitive (missense variants are at least 10pct of known pathogenic variants in clinvar)."),
            new VCFInfoHeaderLine(vcfCodes.assess_BP1_CANON, 1, VCFHeaderLineType.Integer,"(Considering the canonical TX only) Missense variant in gene that is NOT missense-sensitive (less than 10pct of pathogenic variants are missense)"),
            new VCFInfoHeaderLine(vcfCodes.assess_BP3_CANON, 1, VCFHeaderLineType.Integer,"(Considering the canonical TX only) In-frame indels in a repetitive region that does NOT overlap with any known domain."),
            new VCFInfoHeaderLine(vcfCodes.assess_PM4_CANON, 1, VCFHeaderLineType.Integer,      "(Considering the canonical TX only) Protein length change in nonrepeat region OR stop-loss variant."),
            new VCFInfoHeaderLine(vcfCodes.assess_PM5_CANON, 1, VCFHeaderLineType.Integer,      "(Considering the canonical TX only) Novel missense variant change at amino acid where a different amino acid change is known pathogenic in ClinVar."),
            new VCFInfoHeaderLine(vcfCodes.assess_BP7_CANON, 1, VCFHeaderLineType.Integer,      "(Considering the canonical TX only) Synonymous variant that does NOT intersect with a conserved element region."),
            new VCFInfoHeaderLine(vcfCodes.assess_RATING_CANON, 1, VCFHeaderLineType.String,    "(Considering the canonical TX only) ACMG Pathogenicity rating: PATHO - pathogenic. LPATH - likely pathogenic, VUS - variant, unknown significance, LB - likely benign, B - benign."),

            //new VCFInfoHeaderLine(vcfCodes.geneIDs, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,    "List of gene symbols that this variant overlaps with."),
            
            new VCFInfoHeaderLine(vcfCodes.assess_forBT_codonLOF, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,   "For LOF variants: List of RS numbers for previously-reported pathogenic or likely-pathogenic LOF variants that occur in the same codon as this LOF variant."),
            new VCFInfoHeaderLine(vcfCodes.assess_forBT_downstreamLOF, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,   "For LOF variants: List of RS numbers for previously-reported pathogenic or likely-pathogenic LOF variants that occur DOWNSTREAM of this LOF variant."),

            new VCFInfoHeaderLine(vcfCodes.assess_forBT_codonLOF_CANON, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,   "(Considering the canonical TX only) For LOF variants: List of RS numbers for previously-reported pathogenic or likely-pathogenic LOF variants that occur in the same codon as this LOF variant."),
            new VCFInfoHeaderLine(vcfCodes.assess_forBT_downstreamLOF_CANON, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,   "(Considering the canonical TX only) For LOF variants: List of RS numbers for previously-reported pathogenic or likely-pathogenic LOF variants that occur DOWNSTREAM of this LOF variant."),
  
            new VCFInfoHeaderLine(vcfCodes.assess_forBT_geneHasDownstreamLOF, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,   "For each gene that this variant intersects with, this will include the geneID if and only if there is a known-pathogenic or likely-pathogenic nonsense (stop gain) or frameshift variant in ClinVar/HGMD"),
            new VCFInfoHeaderLine(vcfCodes.assess_forBT_geneHasStartLoss, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,   "For each gene that this variant intersects with, this will include the geneID if and only if there is a known-pathogenic or likely-pathogenic start-loss variant in ClinVar/HGMD."),
            new VCFInfoHeaderLine(vcfCodes.assess_forBT_geneHasStopLoss, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,   "For each gene that this variant intersects with, this will include the geneID if and only if there is a known-pathogenic or likely-pathogenic stop-loss variant in ClinVar/HGMD."),
            new VCFInfoHeaderLine(vcfCodes.assess_forBT_geneHasDownstreamLOF_CANON, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,   "(Considering the canonical TX only) For each gene that this variant intersects with, this will include the geneID if and only if there is a known-pathogenic or likely-pathogenic nonsense (stop gain) or frameshift variant in ClinVar/HGMD"),
            new VCFInfoHeaderLine(vcfCodes.assess_forBT_geneHasStartLoss_CANON, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,   "(Considering the canonical TX only) For each gene that this variant intersects with, this will include the geneID if and only if there is a known-pathogenic or likely-pathogenic start-loss variant in ClinVar/HGMD."),
            new VCFInfoHeaderLine(vcfCodes.assess_forBT_geneHasStopLoss_CANON, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,   "(Considering the canonical TX only) For each gene that this variant intersects with, this will include the geneID if and only if there is a known-pathogenic or likely-pathogenic stop-loss variant in ClinVar/HGMD.")
            //assess_ACMG_numGenes
          ) ++ ({
            if(includeGenewise){
              Seq(
            new VCFInfoHeaderLine(vcfCodes.assess_PVS1+"_GENEWISE", 1, VCFHeaderLineType.Integer,    "Loss-of-function variant in gene that is LOF-sensitive. Loss-of-function is defined as one of:"+
                                                                                         "stop-gain, frameshift, total-gene-indel, or splice junction indel. "+
                                                                                         "A gene is defined as LOF-sensitive if at least 10pct of pathogenic "+
                                                                                         "clinvar variants are LOF-type variants."),
            new VCFInfoHeaderLine(vcfCodes.assess_PVS1_CANON+"_GENEWISE", 1, VCFHeaderLineType.Integer,    "(Considering the canonical TX only) Loss-of-function variant in gene that is LOF-sensitive. Loss-of-function is defined as one of:"+
                                                                                         "stop-gain, frameshift, total-gene-indel, or splice junction indel. "+
                                                                                         "A gene is defined as LOF-sensitive if at least 10pct of pathogenic "+
                                                                                         "clinvar variants are LOF-type variants."),
            new VCFInfoHeaderLine(vcfCodes.assess_PS1+"_GENEWISE", VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,      "Variant has the same amino acid change as a pathogenic variant from ClinVar."),
            new VCFInfoHeaderLine(vcfCodes.assess_PS1_CANON+"_GENEWISE", VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,"(Considering the canonical TX only) Variant has the same amino acid change as a pathogenic variant from ClinVar."),
            new VCFInfoHeaderLine(vcfCodes.assess_PM1+"_GENEWISE", VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,      "Located in a known domain. (currently just any domain)"),
            new VCFInfoHeaderLine(vcfCodes.assess_PM4+"_GENEWISE", VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,      "Protein length change in nonrepeat region OR stop-loss variant."),
            new VCFInfoHeaderLine(vcfCodes.assess_PM4_CANON+"_GENEWISE", VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,      "(Considering the canonical TX only) Protein length change in nonrepeat region OR stop-loss variant."),
            new VCFInfoHeaderLine(vcfCodes.assess_PM5+"_GENEWISE", VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,      "Novel missense variant change at amino acid where a different amino acid change is known pathogenic in ClinVar."),
            new VCFInfoHeaderLine(vcfCodes.assess_PM5_CANON+"_GENEWISE", VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,      "(Considering the canonical TX only) Novel missense variant change at amino acid where a different amino acid change is known pathogenic in ClinVar."),
            new VCFInfoHeaderLine(vcfCodes.assess_PP2+"_GENEWISE", VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,      "Missense variant in gene that is missense-sensitive (missense variants are at least 10pct of known pathogenic variants in clinvar)."),
            new VCFInfoHeaderLine(vcfCodes.assess_PP2_CANON+"_GENEWISE", VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,"(Considering the canonical TX only) Missense variant in gene that is missense-sensitive (missense variants are at least 10pct of known pathogenic variants in clinvar)."),
            new VCFInfoHeaderLine(vcfCodes.assess_BP1+"_GENEWISE", VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,      "Missense variant in gene that is NOT missense-sensitive (less than 10pct of pathogenic variants are missense)"),
            new VCFInfoHeaderLine(vcfCodes.assess_BP1_CANON+"_GENEWISE", VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,"(Considering the canonical TX only) Missense variant in gene that is NOT missense-sensitive (less than 10pct of pathogenic variants are missense)"),
            new VCFInfoHeaderLine(vcfCodes.assess_BP7+"_GENEWISE", VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,      "Synonymous variant that does NOT intersect with a conserved element region."),
            new VCFInfoHeaderLine(vcfCodes.assess_BP7_CANON+"_GENEWISE", VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,      "(Considering the canonical TX only) Synonymous variant that does NOT intersect with a conserved element region."),
            
            new VCFInfoHeaderLine(vcfCodes.assess_RATING_CANON+"_GENEWISE", VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,    "For each gene, (Considering the canonical TX only) ACMG Pathogenicity rating: PATHO - pathogenic. LPATH - likely pathogenic, VUS - variant, unknown significance, LB - likely benign, B - benign."),
            new VCFInfoHeaderLine(vcfCodes.assess_RATING+"_GENEWISE", VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,    "For each gene, ACMG Pathogenicity rating: PATHO - pathogenic. LPATH - likely pathogenic, VUS - variant, unknown significance, LB - likely benign, B - benign.")
              )
            } else {
              Seq();
            }
          }) ++ ({
            
              //       (if(this.isPatho){ "1" } else { if(this.isBenign) "-1" else "1" }) +"|" +this.clinVarID.getOrElse("")+"|"+this.clinVarPatho.getOrElse(-1)+"|"+this.clinVarFullPatho.getOrElse("-1")+"|"+
              //this.hgmdID.getOrElse("")+"|"+this.hgmdClass.getOrElse("-1")

            Seq("","_ClinVar","_HGMD").flatMap{ dbid => {
              val dbname = if(dbid == "") {"ClinVar andor HGMD"} else {""+dbid.tail+""}
              val colDescSimple = "Info is separated into bar-delimited sub fields which are as follows. FinalPathogenicityRating, clinVarID, clinVarClass, clinVarReports, hgmd ID, hgmd class"
              val colDescComplex = colDescSimple+", slash delimited transcript list, and slash delimited gene list." 
              val (matchDescPrefix,matchDescSuffix) = ("Annotation on "," matches found in "+dbname+".")
              Seq[VCFInfoHeaderLine](
                new VCFInfoHeaderLine(vcfCodes.assess_exactMatchInfo +dbid, 1, VCFHeaderLineType.String,   matchDescPrefix+"exact base-pair"+matchDescSuffix+colDescSimple),
                new VCFInfoHeaderLine(vcfCodes.assess_aminoMatchInfo+dbid, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,   matchDescPrefix+"amino-acid-level"+matchDescSuffix+colDescComplex),
                new VCFInfoHeaderLine(vcfCodes.assess_nearMatchInfo+dbid,  VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,   matchDescPrefix+"same-position-different-amino-acid-change"+matchDescSuffix+colDescComplex),

                new VCFInfoHeaderLine(vcfCodes.assess_pathoExactMatchRS+dbid, 1, VCFHeaderLineType.String,   "RS number referring to pathogenic variant in the "+dbname+" database that matches this variant exactly (or blank if there is no matching variant)."),
                new VCFInfoHeaderLine(vcfCodes.assess_pathoAminoMatchRS+dbid, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,   "List of RS numbers referring to pathogenic variant(s) in the "+dbname+" database that match the amino acid change of this variant  (or blank if there is no matching variant)."),
                new VCFInfoHeaderLine(vcfCodes.assess_pathoNearMatchRS+dbid,  VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,   "List of RS numbers referring to pathogenic variant(s) in the "+dbname+" database that alter the same amino acid position, but change to a different amino acid  (or blank if there is no matching variant)."),

                new VCFInfoHeaderLine(vcfCodes.assess_pathoAminoMatchRS_CANON+dbid, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,   "(Considering the canonical TX only) List of RS numbers referring to pathogenic variant(s) in the "+dbname+" database that match the amino acid change of this variant  (or blank if there is no matching variant)."),
                new VCFInfoHeaderLine(vcfCodes.assess_pathoNearMatchRS_CANON+dbid,  VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,   "(Considering the canonical TX only) List of RS numbers referring to pathogenic variant(s) in the "+dbname+" databasethat alter the same amino acid position, but change to a different amino acid  (or blank if there is no matching variant)."),
                new VCFInfoHeaderLine(vcfCodes.assess_aminoMatchInfo_CANON+dbid, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,   "(Considering the canonical TX only) "+ matchDescPrefix+"amino-acid-level"+matchDescSuffix+colDescComplex),
                new VCFInfoHeaderLine(vcfCodes.assess_nearMatchInfo_CANON+dbid,  VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,   "(Considering the canonical TX only) "+ matchDescPrefix+"same-position-different-amino-acid-change"+matchDescSuffix+colDescComplex)
              ) ++ (if(dbid == "_ClinVar"){
                Seq[VCFInfoHeaderLine](
                  new VCFInfoHeaderLine(vcfCodes.assess_benignExactMatchRS+dbid, 1, VCFHeaderLineType.String,   "RS number referring to benign variant in the "+dbname+" database that matches this variant exactly (or blank if there is no matching variant)."),
                  new VCFInfoHeaderLine(vcfCodes.assess_benignAminoMatchRS+dbid, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,   "List of RS numbers referring to benign variant(s) in the "+dbname+" database that match the amino acid change of this variant  (or blank if there is no matching variant)."),
                  new VCFInfoHeaderLine(vcfCodes.assess_benignNearMatchRS+dbid,  VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,   "List of RS numbers referring to benign variant(s) in the "+dbname+" database that alter the same amino acid position, but change to a different amino acid  (or blank if there is no matching variant)."),
                  new VCFInfoHeaderLine(vcfCodes.assess_benignAminoMatchRS_CANON+dbid, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,   "(Considering the canonical TX only) List of RS numbers referring to benign variant(s) in the "+dbname+" database that match the amino acid change of this variant  (or blank if there is no matching variant)."),
                  new VCFInfoHeaderLine(vcfCodes.assess_benignNearMatchRS_CANON+dbid,  VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String,   "(Considering the canonical TX only) List of RS numbers referring to benign variant(s) in the "+dbname+" databasethat alter the same amino acid position, but change to a different amino acid  (or blank if there is no matching variant).")
                );
              } else {
                Seq[VCFInfoHeaderLine]();
              })
            }}
          }) ++ (
              
            if(inSilicoFuns.length > 0){
              List[VCFInfoHeaderLine](
                new VCFInfoHeaderLine(vcfCodes.assess_inSilicoSummary,  1, VCFHeaderLineType.String,   "For each of the following in silico algorithms: ("+inSilicoFuns.map(_._1).mkString(",")+") the final determination of whether it was considered Damaging, Benign, Ambiguous or Unknown."),
                new VCFInfoHeaderLine(vcfCodes.assess_PP3, 1, VCFHeaderLineType.Integer,      "Predicted to be damaging by at least 1 of the following in silico prediction algorithms: "+inSilicoFuns.map(_._1).mkString(",")),
                new VCFInfoHeaderLine(vcfCodes.assess_BP4, 1, VCFHeaderLineType.Integer,      "Predicted to be benign by at least 1 of the following in silico prediction algorithms: "+inSilicoFuns.map(_._1).mkString(","))
              ) ++ inSilicoFuns.map(_._1).map{sf => {
                new VCFInfoHeaderLine(vcfCodes.assess_inSilicoSummary + "_"+ sf,  1, VCFHeaderLineType.String,   "For "+sf+", the summary determination of whether it was considered Damaging, Benign, Ambiguous or Unknown.")
              }}.toList;
            } else {
              List[VCFInfoHeaderLine]()
            }
          ) ++ ( if(pseudoGeneGTF.isDefined){ Some(new VCFInfoHeaderLine(vcfCodes.assess_pseudogene, 1, VCFHeaderLineType.String,   "Variant overlaps with a known pseudogene.")) } else { None })
           
    
    def oldWalkVCF(vcIter : Iterator[VariantContext], vcfHeader : VCFHeader, verbose : Boolean = true) : (Iterator[VariantContext],VCFHeader) = {
      //val (clinVarVariantSet,clinVarVariants) : (scala.collection.Map[String,String],scala.collection.Map[String,Set[(String,internalUtils.TXUtil.pVariantInfo,String)]]) = getClinVarVariants(infile=clinVarVcf,chromList =chromList, vcfCodes = vcfCodes);

      
      //if(domainSummaryFile.isDefined){
        
      //}
      
      
 
      val newHeader = internalUtils.VcfTool.addHeaderLines(vcfHeader,newHeaderLines);
      reportln("Walking input VCF...","note")
      return ( 
      vcIter.map(v => {
        assessVariant(v=v,
            clinVarVariants=clinVarVariants,
            txToGene = txToGene, geneToTx = geneToTx,
            geneIsLofSensitive = geneIsLofSensitive,
            geneIsMisSensitive = geneIsMisSensitive,
            geneIsMisInsensitive = geneIsMisInsensitive,
            isRefSeq = isRefSeq,
            locusIsRepetitive = locusIsRepetitive,
            locusIsConserved = locusIsConserved,
            locusIsHotspot = locusIsHotspot,
            locusIsMappable = locusIsMappable,
            ctrlAlleFreqKeys = ctrlAlleFreqKeys,
            //inSilicoKeysOpt = inSilicoKeysOpt,
            //inSilicoMin = inSilicoMin,
            inSilicoFuns = inSilicoFuns,inSilicoSummary=inSilicoSummary,
            BA1_AF = BA1_AF,
            PM2_AF = PM2_AF,
            clinVarVariantSet = clinVarVariantSet,
            clinVarVariants_LOFSet = clinVarVariants_LOFSet,
            clinVarVariants_startLoss = clinVarVariants_startLoss,
            clinVarVariants_stopLoss =clinVarVariants_stopLoss,
            locusIsPseudogene = locusIsPseudogene,
            vcfCodes = vcfCodes)
       }), newHeader );
    }
    reportln("AssessACMGWalker() Created... ["+stdUtils.getDateAndTimeString+"]","note")
  }
  
  def assessVariant(v : VariantContext, 
 //                   clinVarVariants : scala.collection.Map[String,Set[(String,internalUtils.TXUtil.pVariantInfo,String)]],
                    clinVarVariants : scala.collection.Map[String,Set[internalUtils.TXUtil.pVariantInfo]],
                    txToGene : (String => String) = ((s : String) => s),
                    geneToTx : (String => Set[String]),
                    geneIsLofSensitive : (String => Boolean) = ((s : String) => true),
                    geneIsMisSensitive : (String => Boolean) = ((s : String) => true),
                    geneIsMisInsensitive : (String => Boolean) = ((s : String) => false),
                    isRefSeq : (String => Boolean),
                    locusIsRepetitive : (commonSeqUtils.GenomicInterval => Boolean) ,
                    locusIsConserved  : (commonSeqUtils.GenomicInterval => Boolean) ,
                    locusIsHotspot    : (commonSeqUtils.GenomicInterval => Boolean) ,
                    locusIsMappable   : Option[(commonSeqUtils.GenomicInterval => Boolean)] ,
                    locusIsPseudogene : Option[(commonSeqUtils.GenomicInterval => Boolean)] ,
                    ctrlAlleFreqKeys : Seq[String] = Seq("1KG_AF","ESP_EA_AF","ExAC_ALL","SWH_AF_GRP_CTRL"),
                    
                    inSilicoFuns : Seq[(String,StringToBool,StringToBool)],
                    inSilicoSummary : (VariantContext => (String,Seq[String])),
                    //inSilicoKeysOpt : Option[Seq[(String,Set[String],Set[String])]],
                    //inSilicoMin : Int, //-1 == ALL
                    //inSilicoToleratedCt : Int = 1, 
                    
                    BA1_AF : Double = 0.05,
                    PM2_AF : Double = 0.0001,
                    clinVarVariantSet : scala.collection.Map[String,TXUtil.KnownVariantInfoHolder],
                    clinVarVariants_LOFSet : scala.collection.Map[String,Set[TXUtil.pVariantInfo]],
                    clinVarVariants_startLoss : scala.collection.Map[String,Set[TXUtil.pVariantInfo]],
                    clinVarVariants_stopLoss : scala.collection.Map[String,Set[TXUtil.pVariantInfo]],
                    
                    includeGenewise : Boolean = true,
                    vcfCodes : VCFAnnoCodes = VCFAnnoCodes()
                    
                    //clinVarVariants?
                    ) : VariantContext = {
    
    var vb = new htsjdk.variant.variantcontext.VariantContextBuilder(v);
    val crits = new ACMGCritSet();
    val canonCrits = new ACMGCritSet();

    var problemList = Set[String]();
    
    //val inSilicoMin = if(inSilicoMinCt == -1) inSilicoKeys.size else inSilicoMinCt;
    
    val chrom = v.getContig();
    val pos = v.getStart();
    
    val refAlle = v.getReference();
    val altAlleles = Range(0,v.getNAlleles()-1).map((a) => v.getAlternateAllele(a)).zipWithIndex.filter{case (alt,altIdx) => { alt.getBaseString() != "*" }}

    val txList = v.getAttributeAsList(vcfCodes.txList_TAG).asScala.toVector.map(_.toString).filter(_ != ".");
    
    if(txList.length > 0){
    val geneList = txList.map(x => txToGene(x));
    //vb = vb.attribute(vcfCodes.geneIDs , geneList.mkString(","));
    
    val vTypesList = v.getAttributeAsList(vcfCodes.vType_TAG).asScala.toVector.map(_.toString.split("\\|").toVector);
    val vLVL       = v.getAttributeAsList(vcfCodes.vMutLVL_TAG).asScala.toVector.map(_.toString);
    val vMutPList = v.getAttributeAsList(vcfCodes.vMutP_TAG).asScala.toVector.map(_.toString.split("\\|").toVector);
    val vMutCList = v.getAttributeAsList(vcfCodes.vMutC_TAG).asScala.toVector.map(_.toString.split("\\|").toVector);
    val vMutInfoList = v.getAttributeAsList(vcfCodes.vMutINFO_TAG).asScala.toVector.map(_.toString.split("\\|").toVector.map(x => {internalUtils.TXUtil.getPvarInfoFromString(x)}));
    
    val variantIV = commonSeqUtils.GenomicInterval(v.getContig(),'.', start = v.getStart() - 1, end = math.max(v.getEnd(),v.getStart+1));
    
    val isRepetitive = locusIsRepetitive(variantIV);
    val isConserved  = locusIsConserved(variantIV);
    val isHotspot    = locusIsHotspot(variantIV);
    
    locusIsMappable match {
      case Some(locusMapFunc) => {
        val isMappable = locusMapFunc(variantIV);
        vb = vb.attribute(vcfCodes.assess_IsMappable , if(isMappable) "1" else "0");
      }
      case None => {
        //do nothing
      }
    }
    
    locusIsPseudogene match {
      case Some(locusMapFunc) => {
        val isLocus = locusMapFunc(variantIV);
        vb = vb.attribute(vcfCodes.assess_pseudogene , if(isLocus) "1" else "0");
      }
      case None => {
        //do nothing
      }
    }
    
    if(altAlleles.length > 1){
      error("FATAL ERROR: Cannot deal with multiallelic variants! Split variants to single-allelic!");
    }
    
    vb = vb.attribute(vcfCodes.assess_IsRepetitive , if(isRepetitive) "1" else "0");
    vb = vb.attribute(vcfCodes.assess_IsConserved ,  if(isConserved)  "1" else "0");
    vb = vb.attribute(vcfCodes.assess_IsHotspot ,    if(isHotspot)    "1" else "0");
    //vb = vb.attribute(vcfCodes.assess_domain ,       if(isHotspot)    "1" else "0");
    
    val (alt,altIdx) = altAlleles.head;
    
    //val out : Vector[ACMGCritSet] = altAlleles.map{ case (alt,altIdx) => {
      val vTypes = vTypesList(altIdx).map(_.split("_"));
      val vMutP = vMutPList(altIdx);
      val vMutC = vMutCList(altIdx);
      val vInfo = vMutInfoList(altIdx);
      val combo = txList.zip(vInfo).zip(vMutC).zipWithIndex.map{case (((tx,info),c),i) => (txToGene(tx),tx,info,c,i)}
      val canonCombo = combo.filter{ case (g,tx,info,c,i) => isRefSeq(tx) }
      
      if(combo.exists{ case (g,tx,info,c,i) => { info.subType == "???" }}){
        problemList = problemList + "VT|OddballVariantType" + "VT|???";
      }
      if(combo.exists{ case (g,tx,info,c,i) => { info.subType == "FullExonIndel" }}){
        problemList = problemList + "VT|OddballVariantType" + "VT|FullExonIndel";
      }
      if(combo.exists{ case (g,tx,info,c,i) => { info.subType == "FullIntronIndel" }}){
        problemList = problemList + "VT|OddballVariantType" + "VT|FullIntronIndel";
      }
      if(combo.exists{ case (g,tx,info,c,i) => { info.subType == "total-loss" }}){
        problemList = problemList + "VT|OddballVariantType" + "VT|totalloss";
      }
      
      //***************************HotSpot:
      vb =  vb.attribute(vcfCodes.assess_PM1 , if(isHotspot) "1" else "0"); //CONVERT TO GENEWISE
      crits.addCrit(new ACMGCrit("PM",1,isHotspot,Seq[String]()))
      canonCrits.addCrit(new ACMGCrit("PM",1,isHotspot,Seq[String]()))
      
      //*****************ALLELE FREQS:
      val splitIdx = v.getAttributeAsInt(vcfCodes.splitIdx_TAG,0);
      val numSplit = v.getAttributeAsInt(vcfCodes.numSplit_TAG,1);
      val ctrlAlleFreqs = ctrlAlleFreqKeys.map(key => {
        val afList = v.getAttributeAsList(key).asScala.map(x => x.toString()).map(x => {
          if(x == ".") 0.toDouble;
          else string2double(x);
        })
        if(afList.length == 0){
          0.toDouble;
        } else if(afList.length == 1){
          afList(0);
        } else if(afList.length == numSplit){
          afList(splitIdx);
        } else {
          problemList = problemList + "AF|popAFalleleMismatch";
          warning("Warning: allele freq annotation doesn't match numSplit: afList.length = "+afList.length+", numSplit = "+numSplit+"\n"+
                  "   "+"ref["+refAlle.getBaseString()+"],ALTs=["+altAlleles.map(_._1.getBaseString()).mkString(",")+"]"+key+"=["+v.getAttributeAsList(key).asScala.map(_.toString()).mkString(",")+"]"+
                  (if(internalUtils.optionHolder.OPTION_DEBUGMODE) "\n   "+v.toStringWithoutGenotypes() else ""),
                  "POPAF_FORMAT_WARNING",25);
          0.toDouble;
        }
      })

      //val (minAF, maxAF) = ctrlAlleFreqs.foldLeft( (1.toDouble,0.toDouble) ){ case ((minSF,maxSF),af) => {
      //  
       // val afOpt = if(afString == "."){
      //    None
       // } else Some(string2double(afString));
      //  (minOption(Some(minSF),afOpt).get, maxOption(Some(maxSF),afOpt).get)
      //}}
      val maxAF = ctrlAlleFreqs.foldLeft(0.toDouble){case (maxSF,af) => {
        math.max(maxSF,af);
      }}
      //vb =  vb.attribute(vcfCodes.assess_ctrlAFMIN , minAF.toString);
      vb =  vb.attribute(vcfCodes.assess_ctrlAFMAX , maxAF.toString);
      vb =  vb.attribute(vcfCodes.assess_BA1 , if(maxAF >= BA1_AF) "1" else "0"); //leave as boolean
      vb =  vb.attribute(vcfCodes.assess_PM2 , if(maxAF <= PM2_AF) "1" else "0"); //leave as boolean
      crits.addCrit(new ACMGCrit("BA",1,maxAF >= BA1_AF,Seq[String]()));
      crits.addCrit(new ACMGCrit("PM",2,maxAF <= PM2_AF,Seq[String]()));
      canonCrits.addCrit(new ACMGCrit("BA",1,maxAF >= BA1_AF,Seq[String]()));
      canonCrits.addCrit(new ACMGCrit("PM",2,maxAF <= PM2_AF,Seq[String]()));
      
      //***************************genes:
      
      //val geneList = combo.map{case (g,tx,info,c,i) => g};
      val geneSet = geneList.toSet.toVector.sorted
      val geneSetCanon = canonCombo.map{ case (g,tx,info,c,i) => g}.toSet.toVector.sorted;
      
      vb =  vb.attribute(vcfCodes.assess_ACMG_numGenes , geneSet.size);
      vb =  vb.attribute(vcfCodes.assess_ACMG_numGenes_CANON , geneSetCanon.size);
      
      vb =  vb.attribute(vcfCodes.assess_geneList , geneSet.padTo(1,".").mkString(","));
      vb =  vb.attribute(vcfCodes.assess_geneTxList , geneSet.map((g)  => { geneToTx(g).toVector.sorted.mkString("|") }).padTo(1,".").mkString(","));
      vb =  vb.attribute(vcfCodes.assess_refSeqKnown, geneSet.map( g => g + ":" + geneToTx(g).count(tx => isRefSeq(tx))).padTo(1,".").mkString(",") );
      
      if(geneSet.size > 1){
        problemList = problemList + "MG|MultipleGene";
      }
      
      val numVariantTypes = Seq[Boolean](
        (combo.exists{case (g,tx,info,c,i) => {
          info.severityType == "LLOF" || info.subType == "START-LOSS" || info.subType == "splice"
        }}),
        (combo.exists{case (g,tx,info,c,i) => {
          info.subType == "insAA" || info.subType == "delAA" || info.subType == "indelAA"
        }}),
        (combo.exists{case (g,tx,info,c,i) => {
          info.subType == "swapAA"
        }}),
        (combo.exists{case (g,tx,info,c,i) => {
          info.subType == "cds-synon"
        }})
      ).count(b => b);
      
      if(numVariantTypes > 1){
        problemList = problemList + "MV|MultipleVariantTypes";
      }
      
      //***************************LOF:
      val LofTX = combo.filter{case (g,tx,info,c,i) => {
        info.severityType == "LLOF" || info.subType == "START-LOSS" || info.subType == "splice"
      }}
      
      val LofGenes = LofTX.map{case (g,tx,info,c,i) => {g}}.toSet.toList.sorted;

      vb =  vb.attribute(vcfCodes.assess_LofGenes , LofGenes.padTo(1,".").mkString(","));
      val LofTxList = LofTX.map{case (g,tx,info,c,i) => tx};
      vb =  vb.attribute(vcfCodes.assess_LofTX , LofTxList.padTo(1,".").mkString(","))
      
      val geneLofTx = geneSet.map(g => {
        val txSet = geneToTx(g);
        val txLofCt = LofTxList.count{tx => { txSet.contains(tx) }};
        (g,txLofCt, txSet.size)
      });
      vb =  vb.attribute(vcfCodes.assess_geneLofTxRatio , geneLofTx.map{case (g,a,b) => g+":"+a+"/"+b}.padTo(1,".").mkString(","))
      
      val refSeqLof = geneSet.filter{g => {
        val txSet = geneToTx(g);
        val txLof = LofTxList.filter(tx => {txSet.contains(tx)});
        txLof.exists(tx => {
          isRefSeq(tx);
        })
      }}
      vb =  vb.attribute(vcfCodes.assess_LofGenes+"_CANON" , refSeqLof.padTo(1,".").mkString(","));
      
      val pvs1flag = LofGenes.filter(g => {
        geneIsLofSensitive(g);
      }).toSet.toVector.sorted;
      val pvs1flag_canon = refSeqLof.filter{g => {
        geneIsLofSensitive(g);
      }}.toSet.toVector.sorted;
      crits.addCrit(new ACMGCrit("PVS",1,pvs1flag.length > 0,Seq[String](),geneWiseFlag = Some(pvs1flag.toSet)));
      canonCrits.addCrit(new ACMGCrit("PVS",1,pvs1flag_canon.length > 0,Seq[String](),geneWiseFlag = Some(pvs1flag_canon.toSet)));
      
      vb =  vb.attribute(vcfCodes.assess_PVS1 , if(pvs1flag.length > 0) "1" else "0"); //CONVERT TO GENEWISE
      vb =  vb.attribute(vcfCodes.assess_PVS1_CANON , if(pvs1flag_canon.length > 0) "1" else "0");  //CONVERT TO GENEWISE
      
      if(includeGenewise) vb =  vb.attribute(vcfCodes.assess_PVS1 + "_GENEWISE" , pvs1flag.padTo(1,".").mkString(",")); //CONVERT TO GENEWISE
      if(includeGenewise) vb =  vb.attribute(vcfCodes.assess_PVS1_CANON + "_GENEWISE" , pvs1flag_canon.padTo(1,".").mkString(","));  //CONVERT TO GENEWISE
      
      //***************************Mis:
      val MisTX = combo.filter{case (g,tx,info,c,i) => {
        info.subType == "swapAA";
      }}
      val MisGenes = MisTX.map{case (g,tx,info,c,i) => {
        g
      }}.toSet.toList.sorted;
      
      val MisTxList = MisTX.map{case (g,tx,info,c,i) => tx};
      vb =  vb.attribute(vcfCodes.assess_MisGenes , MisGenes.padTo(1,".").mkString(","));
      vb =  vb.attribute(vcfCodes.assess_MisTX , MisTxList.padTo(1,".").mkString(","));
      
      val geneMisTx = geneSet.map(g => {
        val txSet = geneToTx(g);
        val txMisCt = MisTxList.count{tx => { txSet.contains(tx) }};
        (g,txMisCt, txSet.size)
      });
      vb =  vb.attribute(vcfCodes.assess_geneMisTxRatio , geneMisTx.map{case (g,a,b) => g+":"+a+"/"+b}.padTo(1,".").mkString(","))
      
      val refSeqMis = geneSet.filter{g => {
        val txSet = geneToTx(g);
        val txMis = MisTxList.filter(tx => {txSet.contains(tx)});
        txMis.exists(tx => {
          isRefSeq(tx);
        })
      }}
      vb =  vb.attribute(vcfCodes.assess_MisGenes+"_CANON" , refSeqMis.padTo(1,".").mkString(","));
      
      /*Any Coding:
      val codingTX = combo.filter{case (g,tx,info,c,i) => {
        info.subType == "swapAA";
      }}
      val codingGenes = codingTX.map{case (g,tx,info,c,i) => {
        g
      }}.toSet.toList.sorted;
      val codingTxList = codingTX.map{case (g,tx,info,c,i) => tx};
      vb =  vb.attribute(vcfCodes.assess_CodingGenes , codingGenes.padTo(1,".").mkString(","));
      vb =  vb.attribute(vcfCodes.assess_CodingTX , codingTxList.padTo(1,".").mkString(","));
      //   ***************************Any NS:
      val nsTX = combo.filter{case (g,tx,info,c,i) => {
        info.subType == "swapAA";
      }}
      val nsGenes = nsTX.map{case (g,tx,info,c,i) => {
        g
      }}.toSet.toList.sorted;
      val nsTxList = nsTX.map{case (g,tx,info,c,i) => tx};
      vb =  vb.attribute(vcfCodes.assess_NonsynonGenes , nsGenes.padTo(1,".").mkString(","));
      vb =  vb.attribute(vcfCodes.assess_NonsynonTX , nsTxList.padTo(1,".").mkString(","));
      */
      
      
      //***************************
      val pp2flag = MisGenes.filter(g => geneIsMisSensitive(g));
      val bp1flag = MisGenes.filter(g => geneIsMisInsensitive(g));
      val simplebp1flag = (pvs1flag.length == 0) && (MisGenes.length > 0) && MisGenes.forall(g => {
        geneIsMisInsensitive(g);
      })
      val simplebp1flag_canon = (pvs1flag_canon.length == 0) && (refSeqMis.length > 0) && refSeqMis.forall(g => {
        geneIsMisInsensitive(g);
      })
      
      val bp1list = MisGenes.filter(g => {
        geneIsMisInsensitive(g)  && ! pvs1flag.contains(g)
      })
      val bp1list_canon = refSeqMis.filter(g => {
        geneIsMisInsensitive(g) && ! pvs1flag_canon.contains(g)
      })
      
      val pp2list = MisGenes.filter(g => {
        geneIsMisSensitive(g);
      });
      val pp2list_canon = refSeqMis.filter(g => {
        geneIsMisSensitive(g);
      });
      /*
      val pp2flag = MisGenes.exists(g => {
        geneIsMisSensitive(g);
      });
      val bp1flag = (MisGenes.length > 0) && MisGenes.forall(g => {
        geneIsMisInsensitive(g);
      })
      
      val pp2flag_canon = refSeqMis.exists(g => {
        geneIsMisSensitive(g);
      });
      val bp1flag_canon = (pvs1flag_canon == ".") && (refSeqMis.length > 0) && refSeqMis.forall(g => {
        geneIsMisInsensitive(g);
      })*/
      
      vb =  vb.attribute(vcfCodes.assess_PP2 , if(pp2list.length > 0) "1" else "0");  //CONVERT TO GENEWISE
      vb =  vb.attribute(vcfCodes.assess_BP1 , if(simplebp1flag) "1" else "0"); //CONVERT TO GENEWISE
      vb =  vb.attribute(vcfCodes.assess_PP2_CANON , if(pp2list_canon.length > 0) "1" else "0");  //CONVERT TO GENEWISE
      vb =  vb.attribute(vcfCodes.assess_BP1_CANON , if(simplebp1flag_canon) "1" else "0");  //CONVERT TO GENEWISE
       
      if(includeGenewise) vb =  vb.attribute(vcfCodes.assess_PP2 + "_GENEWISE" , pp2list.toVector.sorted.padTo(1,".").mkString(","));  //CONVERT TO GENEWISE
      if(includeGenewise) vb =  vb.attribute(vcfCodes.assess_BP1 + "_GENEWISE", bp1list.toVector.sorted.padTo(1,".").mkString(",")); //CONVERT TO GENEWISE
      if(includeGenewise) vb =  vb.attribute(vcfCodes.assess_PP2_CANON + "_GENEWISE", pp2list_canon.toVector.sorted.padTo(1,".").mkString(","));  //CONVERT TO GENEWISE
      if(includeGenewise) vb =  vb.attribute(vcfCodes.assess_BP1_CANON + "_GENEWISE", bp1list_canon.toVector.sorted.padTo(1,".").mkString(","));  //CONVERT TO GENEWISE
      
      crits.addCrit(new ACMGCrit("PP",2,pp2list.length > 0,Seq[String](), geneWiseFlag = Some(pp2list.toSet)));
      crits.addCrit(new ACMGCrit("BP",1,bp1list.length > 0,Seq[String](), geneWiseFlag = Some(bp1list.toSet)));
      canonCrits.addCrit(new ACMGCrit("PP",2,pp2list_canon.length > 0,Seq[String](), geneWiseFlag = Some(pp2list_canon.toSet)));
      canonCrits.addCrit(new ACMGCrit("BP",1,bp1list_canon.length > 0,Seq[String](), geneWiseFlag = Some(bp1list_canon.toSet)));
      
      //******************************* Length change variants:
      val pm4flag = {
        ((! isRepetitive) && ({
           combo.exists{case (g,tx,info,c,i) => {
             info.subType == "insAA" || info.subType == "delAA" || info.subType == "indelAA"
           }}
        })) || combo.exists{case (g,tx,info,c,i) => {
             info.pType == "STOP-LOSS"
           }}
      }
      val pm4flag_canon = {
        ((! isRepetitive) && ({
           canonCombo.exists{case (g,tx,info,c,i) => {
             info.subType == "insAA" || info.subType == "delAA" || info.subType == "indelAA"
           }}
        })) || canonCombo.exists{case (g,tx,info,c,i) => {
             info.pType == "STOP-LOSS"
           }}
      }
      val pm4list = combo.filter{case (g,tx,info,c,i) => {
        ((! isRepetitive) && (info.subType == "insAA" || info.subType == "delAA" || info.subType == "indelAA")) || (info.pType == "STOP-LOSS")
      }}.map{case (g,tx,info,c,i) => g}.toSet.toVector.sorted
      val pm4list_canon = canonCombo.filter{case (g,tx,info,c,i) => {
        ((! isRepetitive) && (info.subType == "insAA" || info.subType == "delAA" || info.subType == "indelAA")) || (info.pType == "STOP-LOSS")
      }}.map{case (g,tx,info,c,i) => g}.toSet.toVector.sorted
      
      vb =  vb.attribute(vcfCodes.assess_PM4,if(pm4flag) "1" else "0"); //CONVERT TO GENEWISE
      vb =  vb.attribute(vcfCodes.assess_PM4_CANON,if(pm4flag_canon) "1" else "0");  //CONVERT TO GENEWISE
      if(includeGenewise) vb =  vb.attribute(vcfCodes.assess_PM4+"_GENEWISE",pm4list.padTo(1,".").mkString(",")); //CONVERT TO GENEWISE
      if(includeGenewise) vb =  vb.attribute(vcfCodes.assess_PM4_CANON+"_GENEWISE",pm4list_canon.padTo(1,".").mkString(","));  //CONVERT TO GENEWISE
      
      val bp3flag = {
        (isRepetitive) && (! isHotspot) && ({
           combo.exists{case (g,tx,info,c,i) => {
             info.subType == "insAA" || info.subType == "delAA" || info.subType == "indelAA"
           }}
        })
      }
      val bp3flag_canon = {
        (isRepetitive) && (! isHotspot) && ({
           canonCombo.exists{case (g,tx,info,c,i) => {
             info.subType == "insAA" || info.subType == "delAA" || info.subType == "indelAA"
           }}
        })
      }
      vb =  vb.attribute(vcfCodes.assess_BP3,if(bp3flag) "1" else "0"); //leave as boolean
      vb =  vb.attribute(vcfCodes.assess_BP3_CANON,if(bp3flag_canon) "1" else "0"); //leave as boolean
      
      crits.addCrit(new ACMGCrit("BP",3,bp3flag,Seq[String]()));
      crits.addCrit(new ACMGCrit("PM",4,pm4flag,Seq[String](),geneWiseFlag = Some(pm4list.toSet)));
      canonCrits.addCrit(new ACMGCrit("BP",3,bp3flag_canon,Seq[String]()));
      canonCrits.addCrit(new ACMGCrit("PM",4,pm4flag_canon,Seq[String](),geneWiseFlag = Some(pm4list_canon.toSet)));
      
      //******************************* ClinVar matching:
      
      var ps1 = ACMGCrit("PS",1,false,Seq[String]());
      var pm5 = ACMGCrit("PM",5,false,Seq[String]());
      var ps1RS = Set[String]();
      var pm5RS = Set[String](); 
      val variantString = v.getContig() + ":"+v.getAttributeAsString(vcfCodes.vMutG_TAG,"?!?");
      val varInfo = clinVarVariantSet.get(variantString);
      //val (exactAllRS,exactAllSig,exactAllRawSig) = clinVarVariantSet.getOrElse(variantString,("",1,""))
      val exactRS = if( varInfo.isDefined && varInfo.get.isPatho ){
        varInfo.get.idString();
      } else {
        ""
      }
      
      var downstreamPathoLOFRS = Set[String]();
      var codonPathoLOFRS = Set[String]();
      var downstreamPathoLOFRS_CANON = Set[String]();
      var codonPathoLOFRS_CANON = Set[String]();
      combo.filter{ case (g,tx,info,c,i) => {
        info.severityType == "LLOF" || info.pType == "START-LOSS" || info.subType == "splice" || info.subType == "possSplice"
      }}.foreach{ case (g,tx,info,c,i) => {
        val txvar = clinVarVariants_LOFSet(tx);
        val txvar_downstream_patho = txvar.filter{ p => {
          p.start >= info.start && (p.isPatho)
        }}
        val txvar_downstream_patho_rs = txvar_downstream_patho.map{ p => p.ID }
        val txvar_codon_patho = txvar_downstream_patho.filter{ p => {
          p.start == info.start;
        }}
        val txvar_codon_patho_rs = txvar_codon_patho.map{p => p.ID}
        downstreamPathoLOFRS = downstreamPathoLOFRS ++ txvar_downstream_patho_rs;
        codonPathoLOFRS = codonPathoLOFRS ++ txvar_codon_patho_rs;
        if(isRefSeq(tx)){
          downstreamPathoLOFRS_CANON = downstreamPathoLOFRS_CANON ++ txvar_downstream_patho_rs;
          codonPathoLOFRS_CANON = codonPathoLOFRS_CANON ++ txvar_codon_patho_rs;
        }
      }}
      

      //vb =  vb.attribute(vcfCodes.assess_forBT_exactLOF,      exactPathoRS.toList.sorted.map{_.toString}.padTo(1,".").mkString(","));
      
      vb =  vb.attribute(vcfCodes.assess_forBT_codonLOF,      codonPathoLOFRS.toList.sorted.map{_.toString}.padTo(1,".").mkString(","));
      vb =  vb.attribute(vcfCodes.assess_forBT_codonLOF_CANON,codonPathoLOFRS_CANON.toList.sorted.map{_.toString}.padTo(1,".").mkString(","));
      vb =  vb.attribute(vcfCodes.assess_forBT_downstreamLOF,      downstreamPathoLOFRS.toList.sorted.map{_.toString}.padTo(1,".").mkString(","));
      vb =  vb.attribute(vcfCodes.assess_forBT_downstreamLOF_CANON,downstreamPathoLOFRS_CANON.toList.sorted.map{_.toString}.padTo(1,".").mkString(","));
      
      var geneHasDownstreamLOF = Set[String]();
      var txHasDownstreamLOF = Set[String]();
      var geneHasDownstreamLOF_CANON = Set[String]();
      var txHasDownstreamLOF_CANON = Set[String]();
      
      var geneHasStartLoss = Set[String]();
      var txHasStartLoss = Set[String]();
      var geneHasStartLoss_CANON = Set[String]();
      var txHasStartLoss_CANON = Set[String]();
      
      var geneHasStopLoss = Set[String]();
      var txHasStopLoss = Set[String]();
      var geneHasStopLoss_CANON = Set[String]();
      var txHasStopLoss_CANON = Set[String]();
      
      combo.foreach{ case (g,tx,info,c,i) => {
        val hasDownstreamPatho = clinVarVariants_LOFSet.getOrElse(tx,Set()).exists{ p => {
          p.start >= info.start && (p.isPatho)
        }}
        if(hasDownstreamPatho){
          geneHasDownstreamLOF = geneHasDownstreamLOF + g;
          txHasDownstreamLOF = txHasDownstreamLOF + tx;
          if(isRefSeq(tx)){
            geneHasDownstreamLOF_CANON = geneHasDownstreamLOF_CANON + g;
            txHasDownstreamLOF_CANON = txHasDownstreamLOF_CANON + tx;
          }
        }
        
        val hasStartLoss = clinVarVariants_startLoss.getOrElse(tx,Set()).exists{ p => p.isPatho}
        if(hasStartLoss){
          geneHasStartLoss = geneHasStartLoss + g;
          txHasStartLoss = txHasStartLoss + tx;
          if(isRefSeq(tx)){
            geneHasStartLoss_CANON = geneHasStartLoss_CANON + g;
            txHasStartLoss_CANON = txHasStartLoss_CANON + tx;
          }
        }
        
        val hasStopLoss = clinVarVariants_stopLoss(tx).exists{ p => p.isPatho}
        if(hasStopLoss){
          geneHasStopLoss = geneHasStopLoss + g;
          txHasStopLoss = txHasStopLoss + tx;
          if(isRefSeq(tx)){
            geneHasStopLoss_CANON = geneHasStopLoss_CANON + g;
            txHasStopLoss_CANON = txHasStopLoss_CANON + tx;
          }
        }
      }}
      
      vb =  vb.attribute(vcfCodes.assess_forBT_geneHasDownstreamLOF,        geneHasDownstreamLOF.toList.sorted.map{_.toString}.padTo(1,".").mkString(","));
      vb =  vb.attribute(vcfCodes.assess_forBT_geneHasDownstreamLOF_CANON,  geneHasDownstreamLOF_CANON.toList.sorted.map{_.toString}.padTo(1,".").mkString(","));
      vb =  vb.attribute(vcfCodes.assess_forBT_geneHasStartLoss,            geneHasStartLoss.toList.sorted.map{_.toString}.padTo(1,".").mkString(","));
      vb =  vb.attribute(vcfCodes.assess_forBT_geneHasStartLoss_CANON,      geneHasStartLoss_CANON.toList.sorted.map{_.toString}.padTo(1,".").mkString(","));
      vb =  vb.attribute(vcfCodes.assess_forBT_geneHasStopLoss,             geneHasStopLoss.toList.sorted.map{_.toString}.padTo(1,".").mkString(","));
      vb =  vb.attribute(vcfCodes.assess_forBT_geneHasStopLoss_CANON,       geneHasStopLoss_CANON.toList.sorted.map{_.toString}.padTo(1,".").mkString(","));
      
       // assess_forBT_geneHasDownstreamLOF : String = TOP_LEVEL_VCF_TAG+"ASSESS_geneList_hasPathoDownstreamLOF",
      //  assess_forBT_geneHasStartLoss : String = TOP_LEVEL_VCF_TAG+"ASSESS_geneList_hasPathoStartLoss",
      //  assess_forBT_geneHasStopLoss : String = TOP_LEVEL_VCF_TAG+"ASSESS_geneList_hasPathoStopLoss",
      //  assess_forBT_geneHasDownstreamLOF_CANON : String = TOP_LEVEL_VCF_TAG+"ASSESS_geneList_hasPathoDownstreamLOF_CANON",
      //  assess_forBT_geneHasStartLoss_CANON : String = TOP_LEVEL_VCF_TAG+"ASSESS_geneList_hasPathoStartLoss_CANON",
      //  assess_forBT_geneHasStopLoss_CANON : String = TOP_LEVEL_VCF_TAG+"ASSESS_geneList_hasPathoStopLoss_CANON",
      
      val aminoMatchInfo = scala.collection.mutable.AnyRefMap[TXUtil.KnownVariantInfoHolder,Set[String]]();
      val nearMatchInfo = scala.collection.mutable.AnyRefMap[TXUtil.KnownVariantInfoHolder,Set[String]]();
      
      var ps1RS_canon = Set[String]();
      var pm5RS_canon = Set[String]();
      var ps1_canon = ACMGCrit("PS",1,false,Seq[String]());
      var pm5_canon = ACMGCrit("PM",5,false,Seq[String]());
      var aminoMatchInfo_canon =  scala.collection.mutable.AnyRefMap[TXUtil.KnownVariantInfoHolder,Set[String]]();
      var nearMatchInfo_canon =  scala.collection.mutable.AnyRefMap[TXUtil.KnownVariantInfoHolder,Set[String]]();
      
      combo.filter{case (g,tx,info,c,i) => {
        info.severityType == "NONSYNON"
      }}.foreach{case (g,tx,info,c,i) => {
        val txvar = clinVarVariants(tx) //.filter(cvinfo => cvinfo.isPatho)
        val aminoMatchSet = txvar.filter{cvinfo => {cvinfo.start == info.start && cvinfo.subType == info.subType && cvinfo.pvar == info.pvar}};
        val newAminoMatchInfo = aminoMatchSet.map(cvinfo => cvinfo.dbVarInfo.get).toSet
        newAminoMatchInfo.foreach{ ami => {
          aminoMatchInfo(ami) = aminoMatchInfo.getOrElse(ami,Set[String]()) + tx;
        }}
        //aminoMatchInfo = aminoMatchInfo ++ newAminoMatchInfo
        if(isRefSeq(tx)) {
          //aminoMatchInfo_canon = aminoMatchInfo_canon ++ newAminoMatchInfo;
          newAminoMatchInfo.foreach{ ami => {
            aminoMatchInfo_canon(ami) = aminoMatchInfo_canon.getOrElse(ami,Set[String]()) + tx;
          }}
        }
        val aminoPathoMatch = aminoMatchSet.filter(cvinfo => cvinfo.isPatho);
        val aminoBenignMatch = aminoMatchSet.filter(cvinfo => cvinfo.isBenign);
        
        if(aminoPathoMatch.size > 0){
          ps1 = ps1.merge(ACMGCrit("PS",1,true,Seq[String](tx + ":" + info.pvar)));
          notice("Adding aminoMatch: rsnums:[\""+aminoPathoMatch.map(_.ID).toSet.mkString("\",\"")+"\"] \n"+
                  "                          [\""+aminoPathoMatch.map{(cvinfo) => { cvinfo.txid+":"+cvinfo.pvar+":"+cvinfo.ID }}.mkString("\",\"")+"\"]",
                  "addAminoMatchRS",5);
          ps1RS = ps1RS ++ aminoPathoMatch.map(_.ID).toSet
          
          if(isRefSeq(tx)){
            ps1_canon = ps1_canon.merge(ACMGCrit("PS",1,true,Seq[String](tx + ":" + info.pvar)));
            ps1RS_canon = ps1RS_canon ++ aminoPathoMatch.map(_.ID).toSet
          }
        }
        
        if(info.subType == "swapAA"){
          val partialMatch = txvar.filter{(cvinfo) => {cvinfo.subType == "swapAA" && cvinfo.start == info.start && cvinfo.altAA != info.altAA}};
          val partialPathoMatch = partialMatch.filter(cvinfo => cvinfo.isPatho);
          val newNearMatchInfo = partialMatch.map(cvinfo => cvinfo.dbVarInfo.get).toSet;
          pm5RS = pm5RS ++ partialPathoMatch.map(_.ID).toSet;
          //nearMatchInfo = nearMatchInfo ++ newNearMatchInfo;
          newNearMatchInfo.foreach{ ami => {
            nearMatchInfo(ami) = nearMatchInfo.getOrElse(ami,Set[String]()) + tx;
          }}
          if(isRefSeq(tx)){
            pm5RS_canon = pm5RS_canon ++ partialPathoMatch.map(_.ID).toSet;
            //nearMatchInfo_canon = nearMatchInfo_canon ++ newNearMatchInfo;
            newNearMatchInfo.foreach{ ami => {
              nearMatchInfo_canon(ami) = nearMatchInfo_canon.getOrElse(ami,Set[String]()) + tx;
            }}
          }
          if(partialPathoMatch.size > 0 && aminoPathoMatch.size == 0){
            pm5 = pm5.merge(ACMGCrit("PM",5,true,Seq[String](tx+":"+info.pvar+"Vs"+partialPathoMatch.head.altAA)));
            if(isRefSeq(tx)) pm5_canon = pm5_canon.merge(ACMGCrit("PM",5,true,Seq[String](tx+":"+info.pvar+"Vs"+partialPathoMatch.head.altAA)));
          }
        } else if(info.pType == "STOP-LOSS"){
          //do stuff?
        } else if(info.subType == "indelAA" || info.subType == "insAA" || info.subType == "delAA"){ //what about "multSwapAA"?
          //do stuff?
        }
      }}
      
      if(ps1.flag){
        vb =  vb.attribute(vcfCodes.assess_PS1,"1"); //CONVERT TO GENEWISE
        vb =  vb.attribute(vcfCodes.assess_PM5,"0"); //CONVERT TO GENEWISE
      } else if(pm5.flag){
        vb =  vb.attribute(vcfCodes.assess_PS1,"0"); //CONVERT TO GENEWISE
        vb =  vb.attribute(vcfCodes.assess_PM5,"1"); //CONVERT TO GENEWISE
      } else {
        vb =  vb.attribute(vcfCodes.assess_PS1,"0"); //CONVERT TO GENEWISE
        vb =  vb.attribute(vcfCodes.assess_PM5,"0"); //CONVERT TO GENEWISE
      }
      if(ps1_canon.flag){
        vb =  vb.attribute(vcfCodes.assess_PS1_CANON,"1"); //CONVERT TO GENEWISE
        vb =  vb.attribute(vcfCodes.assess_PM5_CANON,"0"); //CONVERT TO GENEWISE
      } else if(pm5_canon.flag){
        vb =  vb.attribute(vcfCodes.assess_PS1_CANON,"0"); //CONVERT TO GENEWISE
        vb =  vb.attribute(vcfCodes.assess_PM5_CANON,"1"); //CONVERT TO GENEWISE
      } else {
        vb =  vb.attribute(vcfCodes.assess_PS1_CANON,"0"); //CONVERT TO GENEWISE
        vb =  vb.attribute(vcfCodes.assess_PM5_CANON,"0"); //CONVERT TO GENEWISE
      }
      
      val ps1set = aminoMatchInfo.filter{ case (ami,txSet) => { ami.isPatho }}.flatMap{ case (ami,txSet) => txSet.map{txToGene(_)} }.toSet
      val pm5set = nearMatchInfo.filter{ case (ami,txSet) => { ami.isPatho }}.flatMap{ case (ami,txSet) => txSet.map{txToGene(_)} }.toSet -- ps1set;
      val ps1set_canon = aminoMatchInfo_canon.filter{ case (ami,txSet) => { ami.isPatho }}.flatMap{ case (ami,txSet) => txSet.map{txToGene(_)} }.toSet
      val pm5set_canon = nearMatchInfo_canon.filter{ case (ami,txSet) => { ami.isPatho }}.flatMap{ case (ami,txSet) => txSet.map{txToGene(_)} }.toSet -- ps1set_canon;
      
      if(includeGenewise) vb =  vb.attribute(vcfCodes.assess_PS1+"_GENEWISE",ps1set.toVector.distinct.sorted.mkString(","));
      if(includeGenewise) vb =  vb.attribute(vcfCodes.assess_PM5+"_GENEWISE",pm5set.toVector.distinct.sorted.mkString(","));
      if(includeGenewise) vb =  vb.attribute(vcfCodes.assess_PS1_CANON+"_GENEWISE",ps1set_canon.toVector.distinct.sorted.mkString(","));
      if(includeGenewise) vb =  vb.attribute(vcfCodes.assess_PM5_CANON+"_GENEWISE",pm5set_canon.toVector.distinct.sorted.mkString(","));
      
      crits.addCrit(ps1.addGeneWise(ps1set));
      crits.addCrit(pm5.addGeneWise(pm5set));
      canonCrits.addCrit(ps1_canon.addGeneWise(ps1set_canon));
      canonCrits.addCrit(pm5_canon.addGeneWise(pm5set_canon));
      
      vb = vb.attribute(vcfCodes.assess_pathoExactMatchRS, if(exactRS == "") "." else exactRS);
      vb = vb.attribute(vcfCodes.assess_pathoAminoMatchRS, ps1RS.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
      vb = vb.attribute(vcfCodes.assess_pathoNearMatchRS,  pm5RS.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
      vb = vb.attribute(vcfCodes.assess_pathoAminoMatchRS_CANON, ps1RS_canon.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
      vb = vb.attribute(vcfCodes.assess_pathoNearMatchRS_CANON,  pm5RS_canon.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
      
      vb = vb.attribute(vcfCodes.assess_pathoExactMatchRS+"_ClinVar", (varInfo match {
        case Some(vinf) => {
          if(vinf.isPathogenic(true,false)){
            vinf.clinVarID.getOrElse(".")
          } else {
            "."
          }
        }
        case None => ".";
      }));
      vb = vb.attribute(vcfCodes.assess_pathoAminoMatchRS+"_ClinVar", aminoMatchInfo.filter{ case (b,txSet) => b.isPathogenic(true,false) && b.clinVarID.isDefined}.map{ case (b,txSet) => {
        b.clinVarID.getOrElse(".")
      }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
      vb = vb.attribute(vcfCodes.assess_pathoNearMatchRS+"_ClinVar",  nearMatchInfo.filter{ case (b,txSet) => b.isPathogenic(true,false) && b.clinVarID.isDefined}.map{ case (b,txSet) => {
        b.clinVarID.getOrElse(".")
      }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
      vb = vb.attribute(vcfCodes.assess_pathoAminoMatchRS_CANON+"_ClinVar", aminoMatchInfo_canon.filter{ case (b,txSet) => b.isPathogenic(true,false) && b.clinVarID.isDefined}.map{ case (b,txSet) => {
        b.clinVarID.getOrElse(".")
      }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
      vb = vb.attribute(vcfCodes.assess_pathoNearMatchRS_CANON+"_ClinVar",  nearMatchInfo_canon.filter{ case (b,txSet) => b.isPathogenic(true,false) && b.clinVarID.isDefined}.map{ case (b,txSet) => {
        b.clinVarID.getOrElse(".")
      }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
      
      vb = vb.attribute(vcfCodes.assess_pathoExactMatchRS+"_HGMD", (varInfo match {
        case Some(vinf) => {
          if(vinf.isPathogenic(false,true)){
            vinf.hgmdID.getOrElse(".")
          } else {
            "."
          }
        }
        case None => ".";
      }));
      vb = vb.attribute(vcfCodes.assess_pathoAminoMatchRS+"_HGMD", aminoMatchInfo.filter{ case (b,txSet) => b.isPathogenic(false,true) && b.hgmdID.isDefined}.map{ case (b,txSet) => {
        b.hgmdID.getOrElse(".")
      }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
      vb = vb.attribute(vcfCodes.assess_pathoNearMatchRS+"_HGMD",  nearMatchInfo.filter{ case (b,txSet) => b.isPathogenic(false,true) && b.hgmdID.isDefined}.map{ case (b,txSet) => {
        b.hgmdID.getOrElse(".")
      }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
      vb = vb.attribute(vcfCodes.assess_pathoAminoMatchRS_CANON+"_HGMD", aminoMatchInfo_canon.filter{ case (b,txSet) => b.isPathogenic(false,true) && b.hgmdID.isDefined}.map{ case (b,txSet) => {
        b.hgmdID.getOrElse(".")
      }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
      vb = vb.attribute(vcfCodes.assess_pathoNearMatchRS_CANON+"_HGMD",  nearMatchInfo_canon.filter{ case (b,txSet) => b.isPathogenic(false,true) && b.hgmdID.isDefined}.map{ case (b,txSet) => {
        b.hgmdID.getOrElse(".")
      }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
      
      //        assess_exactMatchInfo : String = TOP_LEVEL_VCF_TAG+"ACMG_cvInfo_ExactMatch",
      //  assess_aminoMatchInfo : String = TOP_LEVEL_VCF_TAG+"ACMG_cvInfo_AminoMatch",
      //  assess_nearMatchInfo : String = TOP_LEVEL_VCF_TAG+"ACMG_cvInfo_NearMatch",
      
        val varInfoString = varInfo match {
          case Some(vinf) => {
            vinf.fullInfoString()
          }
          case None => {
            ".";
          }
        }
        
      vb = vb.attribute(vcfCodes.assess_exactMatchInfo,  varInfoString);
      vb = vb.attribute(vcfCodes.assess_aminoMatchInfo, aminoMatchInfo.toVector.sortBy{ case (b,txSet) => b.id }.map{case (b,txSet) => b.fullInfoString() + "|" + txSet.toVector.sorted.mkString("/")+"|"+txSet.map{tx => txToGene(tx)}.toVector.sorted.mkString("/")}.padTo(1,".").mkString(","));
      vb = vb.attribute(vcfCodes.assess_nearMatchInfo,  nearMatchInfo.toVector.sortBy{ case (b,txSet) => b.id }.map{case (b,txSet) => b.fullInfoString() + "|" + txSet.toVector.sorted.mkString("/")+"|"+txSet.map{tx => txToGene(tx)}.toVector.sorted.mkString("/")}.padTo(1,".").mkString(","));
      vb = vb.attribute(vcfCodes.assess_aminoMatchInfo_CANON, aminoMatchInfo_canon.toVector.sortBy{ case (b,txSet) => b.id }.map{case (b,txSet) => b.fullInfoString() + "|" + txSet.toVector.sorted.mkString("/")+"|"+txSet.map{tx => txToGene(tx)}.toVector.sorted.mkString("/")}.padTo(1,".").mkString(","));
      vb = vb.attribute(vcfCodes.assess_nearMatchInfo_CANON,  nearMatchInfo_canon.toVector.sortBy{ case (b,txSet) => b.id }.map{case (b,txSet) => b.fullInfoString() + "|" + txSet.toVector.sorted.mkString("/")+"|"+txSet.map{tx => txToGene(tx)}.toVector.sorted.mkString("/")}.padTo(1,".").mkString(","));
      
      vb = vb.attribute(vcfCodes.assess_exactMatchInfo+"_ClinVar", (varInfo match {
        case Some(vinf) => {
          vinf.infoString(clinVar=true,hgmd=false)
        }
        case None => ".";
      }));
      
      vb = vb.attribute(vcfCodes.assess_aminoMatchInfo+"_ClinVar", aminoMatchInfo.filter{ case (b,txSet) => b.clinVarID.isDefined}.map{ case (b,txSet) => {
        b.infoString(clinVar = true,hgmd=false);
      }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
      vb = vb.attribute(vcfCodes.assess_nearMatchInfo+"_ClinVar",  nearMatchInfo.filter{ case (b,txSet) => b.clinVarID.isDefined}.map{ case (b,txSet) => {
        b.infoString(clinVar = true,hgmd=false);
      }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
      vb = vb.attribute(vcfCodes.assess_aminoMatchInfo_CANON+"_ClinVar", aminoMatchInfo_canon.filter{ case (b,txSet) => b.clinVarID.isDefined}.map{ case (b,txSet) => {
        b.infoString(clinVar = true,hgmd=false);
      }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
      vb = vb.attribute(vcfCodes.assess_nearMatchInfo_CANON+"_ClinVar",  nearMatchInfo_canon.filter{ case (b,txSet) => b.clinVarID.isDefined}.map{ case (b,txSet) => {
        b.infoString(clinVar = true,hgmd=false);
      }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
      
      vb = vb.attribute(vcfCodes.assess_exactMatchInfo+"_HGMD", (varInfo match {
        case Some(vinf) => {
          vinf.infoString(clinVar=false,hgmd=true)
        }
        case None => ".";
      }));
      vb = vb.attribute(vcfCodes.assess_aminoMatchInfo+"_HGMD", aminoMatchInfo.filter{ case (b,txSet) => b.hgmdID.isDefined}.map{ case (b,txSet) => {
        b.infoString(clinVar = false,hgmd=true);
      }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
      vb = vb.attribute(vcfCodes.assess_nearMatchInfo+"_HGMD",  nearMatchInfo.filter{ case (b,txSet) => b.hgmdID.isDefined}.map{ case (b,txSet) => {
        b.infoString(clinVar = false,hgmd=true);
      }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
      vb = vb.attribute(vcfCodes.assess_aminoMatchInfo_CANON+"_HGMD", aminoMatchInfo_canon.filter{ case (b,txSet) => b.hgmdID.isDefined}.map{ case (b,txSet) => {
        b.infoString(clinVar = false,hgmd=true);
      }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
      vb = vb.attribute(vcfCodes.assess_nearMatchInfo_CANON+"_HGMD",  nearMatchInfo_canon.filter{ case (b,txSet) => b.hgmdID.isDefined}.map{ case (b,txSet) => {
        b.infoString(clinVar = false,hgmd=true);
      }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
      
      //******************************* Insilico:
      //inSilicoMin
      //val numSplit = v.getAttributeAsInt(vcfCodes.numSplit_TAG,1);
      val rawAlles = v.getAttributeAsList(vcfCodes.splitAlle_TAG).asScala.map(_.toString());
      
      if(inSilicoFuns.length > 0){
        val (inSilicoResult,algSummaries) = inSilicoSummary(v);
        
        val isDamaging = inSilicoResult == "Damaging"
        val isBenign   = inSilicoResult == "Benign"
        
        crits.addCrit(new ACMGCrit("PP",3,isDamaging,Seq[String]()));
        crits.addCrit(new ACMGCrit("BP",4,isBenign,Seq[String]()));
        canonCrits.addCrit(new ACMGCrit("PP",3,isDamaging,Seq[String]()));
        canonCrits.addCrit(new ACMGCrit("BP",4,isBenign,Seq[String]()));
        vb = vb.attribute(vcfCodes.assess_PP3, if(isDamaging) "1" else "0" ); //Leave as boolean
        vb = vb.attribute(vcfCodes.assess_BP4, if(isBenign) "1" else "0" ); //Leave as boolean
        
        if(inSilicoResult != "."){
          vb = vb.attribute(vcfCodes.assess_inSilicoSummary, inSilicoResult );
        }
        
        //inSilicoFuns.map(_._1).foreach{sf => {
                //new VCFInfoHeaderLine(vcfCodes.assess_inSilicoSummary + "_"+ sf
          inSilicoFuns.zip(algSummaries).foreach{ case ((tag,damFun,benFun),status) => {  
            vb = vb.attribute(vcfCodes.assess_inSilicoSummary + "_"+ tag,status);
          }}
          
        //}}
      }
      
      //******************************* BP7: Synonymous w/ no splice impact, not highly conserved:
      val bp7flag = (! isConserved) && combo.forall{case (g,tx,info,c,i) => {
        info.severityType == "SYNON" || info.severityType == "PSYNON" || info.severityType == "UNK"
      }}
      val bp7list = if(isConserved){
        (combo.filter{ case (g,tx,info,c,i) => info.severityType == "SYNON" }.map{ case (g,tx,info,c,i) => g }.toSet -- 
        combo.filter{case (g,tx,info,c,i) => {
          ! (info.severityType == "SYNON" || info.severityType == "PSYNON" || info.severityType == "UNK")
        }}.map{ case (g,tx,info,c,i) => { g }}.toSet).toVector.sorted;
      } else {
        Vector[String]();
      }
      val bp7list_CANON = if(isConserved){
        (canonCombo.filter{ case (g,tx,info,c,i) => info.severityType == "SYNON" }.map{ case (g,tx,info,c,i) => g }.toSet -- 
        canonCombo.filter{case (g,tx,info,c,i) => {
          ! (info.severityType == "SYNON" || info.severityType == "PSYNON" || info.severityType == "UNK")
        }}.map{ case (g,tx,info,c,i) => { g }}.toSet).toVector.sorted;
      } else {
        Vector[String]();
      }
      vb = vb.attribute(vcfCodes.assess_BP7, if(bp7flag) "1" else "0" ); //CONVERT TO GENEWISE
      vb = vb.attribute(vcfCodes.assess_BP7 + "_GENEWISE", bp7list.padTo(1,".").mkString(",") );
      vb = vb.attribute(vcfCodes.assess_BP7_CANON + "_GENEWISE", bp7list_CANON.padTo(1,".").mkString(",") );
      crits.addCrit(new ACMGCrit("BP",7,bp7flag,Seq[String](),geneWiseFlag = Some(bp7list.toSet)));
      
      val bp7flag_CANON = (! isConserved) && canonCombo.forall{ case (g,tx,info,c,i) => {
        info.severityType == "SYNON" || info.severityType == "PSYNON" || info.severityType == "UNK"
      }}
      vb = vb.attribute(vcfCodes.assess_BP7_CANON, if(bp7flag_CANON) "1" else "0" ); //CONVERT TO GENEWISE
      canonCrits.addCrit(new ACMGCrit("BP",7,bp7flag_CANON,Seq[String](),geneWiseFlag = Some(bp7list_CANON.toSet)));
      
      //*******************************
      
      for((critLvl, critNum, critTAG) <- VcfTool.UNAUTOMATED_ACMG_PARAMS){
        if(v.hasAttribute(critTAG)){
          crits.addCrit(new ACMGCrit(critLvl,critNum,v.getAttributeAsString(critTAG,"") == "1",Seq[String]()));
          canonCrits.addCrit(new ACMGCrit(critLvl,critNum,v.getAttributeAsString(critTAG,"") == "1",Seq[String]()));
        }
      }
      
      //CONVERT TO GENEWISE:
      val rating = CalcACMGVar.getACMGPathogenicityRating(crits.getCritSet);
      val rating_CANON = CalcACMGVar.getACMGPathogenicityRating(canonCrits.getCritSet);
      
      vb = vb.attribute(vcfCodes.assess_RATING, rating );
      vb = vb.attribute(vcfCodes.assess_RATING_CANON, rating_CANON );
      if(includeGenewise) {
        vb = vb.attribute(vcfCodes.assess_RATING + "_GENEWISE", CalcACMGVar.getGenewiseACMGPathogenicityRating(geneSet,crits.getCritSet).map{ case (g,pr) => g + ":"+pr }.mkString(","));
        vb = vb.attribute(vcfCodes.assess_RATING_CANON + "_GENEWISE", CalcACMGVar.getGenewiseACMGPathogenicityRating(geneSetCanon,canonCrits.getCritSet).map{ case (g,pr) => g + ":"+pr }.mkString(","));
      }
       //       assess_ACMG_numGenes : String = TOP_LEVEL_VCF_TAG+"ACMG_NUM_GENES",
       // assess_ACMG_numGenes_CANON : String = TOP_LEVEL_VCF_TAG+"ACMG_NUM_GENES_CANON",
        
      /*
            new VCFInfoHeaderLine(vcfCodes.assess_PVS1, 1, VCFHeaderLineType.Integer,    ""),
            new VCFInfoHeaderLine(vcfCodes.assess_PS1, 1, VCFHeaderLineType.Integer,      "Variant has the same amino acid change as a pathogenic variant from ClinVar."),
            new VCFInfoHeaderLine(vcfCodes.assess_PM1, 1, VCFHeaderLineType.Integer,      "Located in a known domain. (currently just any domain)"),
            new VCFInfoHeaderLine(vcfCodes.assess_PM2, 1, VCFHeaderLineType.Integer,      "Alt allele frequency less than or equal to "+PM2_AF+" in all control datasets ("+ctrlAlleFreqKeys.mkString(",")+")"),
            new VCFInfoHeaderLine(vcfCodes.assess_PM4, 1, VCFHeaderLineType.Integer,      "Protein length change in nonrepeat region OR stop-loss variant."),
            new VCFInfoHeaderLine(vcfCodes.assess_PM5, 1, VCFHeaderLineType.Integer,      "Novel missense variant change at amino acid where a different amino acid change is known pathogenic in ClinVar."),
            new VCFInfoHeaderLine(vcfCodes.assess_PP2, 1, VCFHeaderLineType.Integer,      "Missense variant in gene that is missense-sensitive (missense variants are at least 10pct of known pathogenic variants in clinvar)."),
            new VCFInfoHeaderLine(vcfCodes.assess_BP1, 1, VCFHeaderLineType.Integer,      "Missense variant in gene that is NOT missense-sensitive (less than 10pct of pathogenic variants are missense)"),
            new VCFInfoHeaderLine(vcfCodes.assess_BP3, 1, VCFHeaderLineType.Integer,      "In-frame indels in a repetitive region that does NOT overlap with any known domain."),
            new VCFInfoHeaderLine(vcfCodes.assess_BP7, 1, VCFHeaderLineType.Integer,      "Synonymous variant that does NOT intersect with a conserved element region."),
            new VCFInfoHeaderLine(vcfCodes.assess_BA1,    1, VCFHeaderLineType.Integer,   "Allele frequency greater than 5 percent in one or more control dataset ("+ctrlAlleFreqKeys.mkString(",")+")"),
                        
            new VCFInfoHeaderLine(vcfCodes.assess_PVS1_CANON, 1, VCFHeaderLineType.Integer,    "(Considering the canonical TX only) Loss-of-function variant in gene that is LOF-sensitive. Loss-of-function is defined as one of:"+
                                                                                         "stop-gain, frameshift, total-gene-indel, or splice junction indel. "+
                                                                                         "A gene is defined as LOF-sensitive if at least 10pct of pathogenic "+
                                                                                         "clinvar variants are LOF-type variants."),
            new VCFInfoHeaderLine(vcfCodes.assess_PS1_CANON, 1, VCFHeaderLineType.Integer,"(Considering the canonical TX only) Variant has the same amino acid change as a pathogenic variant from ClinVar."),
            new VCFInfoHeaderLine(vcfCodes.assess_PP2_CANON, 1, VCFHeaderLineType.Integer,"(Considering the canonical TX only) Missense variant in gene that is missense-sensitive (missense variants are at least 10pct of known pathogenic variants in clinvar)."),
            new VCFInfoHeaderLine(vcfCodes.assess_BP1_CANON, 1, VCFHeaderLineType.Integer,"(Considering the canonical TX only) Missense variant in gene that is NOT missense-sensitive (less than 10pct of pathogenic variants are missense)"),
            new VCFInfoHeaderLine(vcfCodes.assess_BP3_CANON, 1, VCFHeaderLineType.Integer,"(Considering the canonical TX only) In-frame indels in a repetitive region that does NOT overlap with any known domain."),
            new VCFInfoHeaderLine(vcfCodes.assess_PM4_CANON, 1, VCFHeaderLineType.Integer,      "(Considering the canonical TX only) Protein length change in nonrepeat region OR stop-loss variant."),
            new VCFInfoHeaderLine(vcfCodes.assess_PM5_CANON, 1, VCFHeaderLineType.Integer,      "(Considering the canonical TX only) Novel missense variant change at amino acid where a different amino acid change is known pathogenic in ClinVar."),
            new VCFInfoHeaderLine(vcfCodes.assess_BP7_CANON, 1, VCFHeaderLineType.Integer,      "(Considering the canonical TX only) Synonymous variant that does NOT intersect with a conserved element region."),
            new VCFInfoHeaderLine(vcfCodes.assess_RATING_CANON, 1, VCFHeaderLineType.String,    "(Considering the canonical TX only) ACMG Pathogenicity rating: PATHO - pathogenic. LPATH - likely pathogenic, VUS - variant, unknown significance, LB - likely benign, B - benign."),
       */
    //vb =  vb.attribute(vcfCodes.assess_PP3,if(inSilicoFlag,"1","0"));
      //val inSilico
      
      
      
      //BP3:
      
      //crits;
    //}}.toVector;
    
    //return (out,vb.make());
    }
    
    vb = vb.attribute(vcfCodes.assess_WARNFLAG, if(problemList.isEmpty) "0" else "1" );
    vb = vb.attribute(vcfCodes.assess_WARNINGS, problemList.toVector.sorted.padTo(1,".").mkString(",") );
    
    return vb.make();
  }
  
  def summarizeInSilico(v : SVcfVariantLine, ctrlAlleFreqKeys : Seq[String] = Seq("1KG_AF","ESP_EA_AF","ExAC_ALL","SWH_AF_GRP_CTRL"),
                                            inSilicoFuns : Seq[(String,StringToBool,StringToBool)],
                                            inSilicoSummary : (SVcfVariantLine => (String,Seq[String])),
                                            vcfCodes : VCFAnnoCodes = VCFAnnoCodes()) : SVcfVariantLine = {
    var vb = v.getLazyOutputLine();

          if(inSilicoFuns.length > 0){
            val (inSilicoResult,algSummaries) = inSilicoSummary(v);
            
            val isDamaging = inSilicoResult == "Damaging"
            val isBenign   = inSilicoResult == "Benign"
            
            //crits.addCrit(new ACMGCrit("PP",3,isDamaging,Seq[String]()));
            //crits.addCrit(new ACMGCrit("BP",4,isBenign,Seq[String]()));
            //canonCrits.addCrit(new ACMGCrit("PP",3,isDamaging,Seq[String]()));
            //canonCrits.addCrit(new ACMGCrit("BP",4,isBenign,Seq[String]()));
            vb.addInfo(vcfCodes.assess_PP3, if(isDamaging) "1" else "0" ); //Leave as boolean
            vb.addInfo(vcfCodes.assess_BP4, if(isBenign) "1" else "0" ); //Leave as boolean
            
            if(inSilicoResult != "."){
              vb.addInfo(vcfCodes.assess_inSilicoSummary, inSilicoResult );
            }
            
            //inSilicoFuns.map(_._1).foreach{sf => {
                    //new VCFInfoHeaderLine(vcfCodes.assess_inSilicoSummary + "_"+ sf
              inSilicoFuns.zip(algSummaries).foreach{ case ((tag,damFun,benFun),status) => {  
                vb.addInfo(vcfCodes.assess_inSilicoSummary + "_"+ tag,status);
              }}
              
            //}}
          }
          vb;
  }
  
  
  def addAminoMatchAnnotation(v : SVcfVariantLine, 
                    txToGene : (String => String) = ((s : String) => s),
                    geneToTx : (String => Set[String]),
                              dbVariants : scala.collection.Map[String,Set[internalUtils.TXUtil.pVariantInfo]],
                              isRefSeq : (String => Boolean),
                              dbVariantSet : scala.collection.Map[String,TXUtil.KnownVariantInfoHolder],
                              dbVariants_LOFSet : scala.collection.Map[String,Set[TXUtil.pVariantInfo]],
                              db : scala.collection.Map[String,Set[TXUtil.pVariantInfo]],
                              dbVariants_stopLoss : scala.collection.Map[String,Set[TXUtil.pVariantInfo]],
                              vcfCodes : VCFAnnoCodes = VCFAnnoCodes()) : SVcfVariantLine = {
    var vb = v.getOutputLine();

    var problemList = Set[String]();
        
    val chrom = v.chrom
    val pos = v.pos
    
    val refAlle = v.ref
    val altAlleles = v.alt.zipWithIndex.filter{ case (alt,altIdx) => alt != VcfTool.UNKNOWN_ALT_TAG_STRING };
    
    val txList = v.getInfoList(vcfCodes.txList_TAG)
    
    if(txList.filter(tx => tx != ".").length > 0){
      notice("tx found for variant","TX_FOUND",0);
      val geneList = txList.map(x => txToGene(x));
        val vTypesList = v.getInfoArray(vcfCodes.vType_TAG);
        val vLVL       = v.getInfoList(vcfCodes.vMutLVL_TAG);
        val vMutPList = v.getInfoArray(vcfCodes.vMutP_TAG);
        val vMutCList = v.getInfoArray(vcfCodes.vMutC_TAG);
        val vMutInfoList = v.getInfoArray(vcfCodes.vMutINFO_TAG).map{k => k.map(x => {internalUtils.TXUtil.getPvarInfoFromString(x)})};
        
        val variantIV = commonSeqUtils.GenomicInterval(chrom,'.', start = pos - 1, end = pos + math.max(v.ref.length,1));
        if(altAlleles.length > 1){
          error("FATAL ERROR: Cannot deal with multiallelic variants! Split variants to single-allelic!");
        }
        val (alt,altIdx) = altAlleles.head;
        
        //val out : Vector[ACMGCritSet] = altAlleles.map{ case (alt,altIdx) => {
          val vTypes = vTypesList(altIdx).map(_.split("_"));
          val vMutP = vMutPList(altIdx);
          val vMutC = vMutCList(altIdx);
          val vInfo = vMutInfoList(altIdx);
          val combo = txList.zip(vInfo).zip(vMutC).zipWithIndex.map{case (((tx,info),c),i) => (txToGene(tx),tx,info,c,i)}
          val canonCombo = combo.filter{ case (g,tx,info,c,i) => isRefSeq(tx) }
          
          
    }
    vb;
  }
  
  
  
  def SassessVariant(v : SVcfVariantLine, 
 //                   clinVarVariants : scala.collection.Map[String,Set[(String,internalUtils.TXUtil.pVariantInfo,String)]],
                    clinVarVariants : scala.collection.Map[String,Set[internalUtils.TXUtil.pVariantInfo]],
                    txToGene : (String => String) = ((s : String) => s),
                    geneToTx : (String => Set[String]),
                    geneIsLofSensitive : (String => Boolean) = ((s : String) => true),
                    geneIsMisSensitive : (String => Boolean) = ((s : String) => true),
                    geneIsMisInsensitive : (String => Boolean) = ((s : String) => false),
                    isRefSeq : (String => Boolean),
                    isRepetitive : Boolean ,
                    isConserved  : Boolean ,
                    isHotspot    : Boolean ,
                    isMappable   : Boolean ,
                    isPseudogene : Boolean ,
                    ctrlAlleFreqKeys : Seq[String] = Seq("1KG_AF","ESP_EA_AF","ExAC_ALL","SWH_AF_GRP_CTRL"),
                    
                    inSilicoFuns : Seq[(String,StringToBool,StringToBool)],
                    inSilicoSummary : (SVcfVariantLine => (String,Seq[String])),
                    //inSilicoKeysOpt : Option[Seq[(String,Set[String],Set[String])]],
                    //inSilicoMin : Int, //-1 == ALL
                    //inSilicoToleratedCt : Int = 1,
                    
                    BA1_AF : Double = 0.05,
                    PM2_AF : Double = 0.0001,
                    clinVarVariantSet : scala.collection.Map[String,TXUtil.KnownVariantInfoHolder],
                    clinVarVariants_LOFSet : scala.collection.Map[String,Set[TXUtil.pVariantInfo]],
                    clinVarVariants_startLoss : scala.collection.Map[String,Set[TXUtil.pVariantInfo]],
                    clinVarVariants_stopLoss : scala.collection.Map[String,Set[TXUtil.pVariantInfo]],
                    
                    includeGenewise : Boolean = true,
                    vcfCodes : VCFAnnoCodes = VCFAnnoCodes()
                    
                    //clinVarVariants?
                    ) : SVcfVariantLine = {
    
    var vb = v.getOutputLine();
    val crits = new ACMGCritSet();
    val canonCrits = new ACMGCritSet();

    var problemList = Set[String]();
    
    //val inSilicoMin = if(inSilicoMinCt == -1) inSilicoKeys.size else inSilicoMinCt;
    
    val chrom = v.chrom
    val pos = v.pos
    
    val refAlle = v.ref
    val altAlleles = v.alt.zipWithIndex.filter{ case (alt,altIdx) => alt != VcfTool.UNKNOWN_ALT_TAG_STRING };

    val txList = v.getInfoList(vcfCodes.txList_TAG)
    
    if(txList.filter(tx => tx != ".").length > 0){
        notice("tx found for variant","TX_FOUND",0);
        val geneList = txList.map(x => txToGene(x));
        //vb = vb.attribute(vcfCodes.geneIDs , geneList.mkString(","));
        
        val vTypesList = v.getInfoArray(vcfCodes.vType_TAG);
        val vLVL       = v.getInfoList(vcfCodes.vMutLVL_TAG);
        val vMutPList = v.getInfoArray(vcfCodes.vMutP_TAG);
        val vMutCList = v.getInfoArray(vcfCodes.vMutC_TAG);
        val vMutInfoList = v.getInfoArray(vcfCodes.vMutINFO_TAG).map{k => k.map(x => {internalUtils.TXUtil.getPvarInfoFromString(x)})};
        
        val variantIV = commonSeqUtils.GenomicInterval(chrom,'.', start = pos - 1, end = pos + math.max(v.ref.length,1));
        
        /*
        val isRepetitive = locusIsRepetitive(variantIV);
        val isConserved  = locusIsConserved(variantIV);
        val isHotspot    = locusIsHotspot(variantIV);
        locusIsMappable match {
          case Some(locusMapFunc) => {
            val isMappable = locusMapFunc(variantIV);
            vb.addInfo(vcfCodes.assess_IsMappable , if(isMappable) "1" else "0");
          }
          case None => {
            //do nothing
          }
        }
        
        locusIsPseudogene match {
          case Some(locusMapFunc) => {
            val isLocus = locusMapFunc(variantIV);
            vb.addInfo(vcfCodes.assess_pseudogene , if(isLocus) "1" else "0");
          }
          case None => {
            //do nothing
          }
        }
        * 
        */
        //vb.addInfo(vcfCodes.assess_IsRepetitive , if(isRepetitive) "1" else "0");
        //vb.addInfo(vcfCodes.assess_IsConserved ,  if(isConserved)  "1" else "0");
        //vb.addInfo(vcfCodes.assess_IsHotspot ,    if(isHotspot)    "1" else "0");
        
        
        if(altAlleles.length > 1){
          error("FATAL ERROR: Cannot deal with multiallelic variants! Split variants to single-allelic!");
        }
        
    
        //vb = vb.attribute(vcfCodes.assess_domain ,       if(isHotspot)    "1" else "0");
        
        val (alt,altIdx) = altAlleles.head;
        
        //val out : Vector[ACMGCritSet] = altAlleles.map{ case (alt,altIdx) => {
          val vTypes = vTypesList(altIdx).map(_.split("_"));
          val vMutP = vMutPList(altIdx);
          val vMutC = vMutCList(altIdx);
          val vInfo = vMutInfoList(altIdx);
          val combo = txList.zip(vInfo).zip(vMutC).zipWithIndex.map{case (((tx,info),c),i) => (txToGene(tx),tx,info,c,i)}
          val canonCombo = combo.filter{ case (g,tx,info,c,i) => isRefSeq(tx) }
          
          if(combo.exists{ case (g,tx,info,c,i) => { info.subType == "???" }}){
            problemList = problemList + "VT|OddballVariantType" + "VT|???";
          }
          if(combo.exists{ case (g,tx,info,c,i) => { info.subType == "FullExonIndel" }}){
            problemList = problemList + "VT|OddballVariantType" + "VT|FullExonIndel";
          }
          if(combo.exists{ case (g,tx,info,c,i) => { info.subType == "FullIntronIndel" }}){
            problemList = problemList + "VT|OddballVariantType" + "VT|FullIntronIndel";
          }
          if(combo.exists{ case (g,tx,info,c,i) => { info.subType == "total-loss" }}){
            problemList = problemList + "VT|OddballVariantType" + "VT|totalloss";
          }
          
          //***************************HotSpot:
          vb.addInfo(vcfCodes.assess_PM1 , if(isHotspot) "1" else "0"); //CONVERT TO GENEWISE
          crits.addCrit(new ACMGCrit("PM",1,isHotspot,Seq[String]()))
          canonCrits.addCrit(new ACMGCrit("PM",1,isHotspot,Seq[String]()))
          
          //*****************ALLELE FREQS:
          val splitIdx : Int = string2int(v.info.getOrElse(vcfCodes.splitIdx_TAG,None).getOrElse("0"));
          val numSplit : Int = string2int(v.info.getOrElse(vcfCodes.numSplit_TAG,None).getOrElse("1"));
          val ctrlAlleFreqs = ctrlAlleFreqKeys.map(key => {
            val afList = v.getInfoList(key).map(x => x.toString()).map(x => {
              if(x == ".") 0.toDouble;
              else string2double(x);
            })
            if(afList.length == 0){
              0.toDouble;
            } else if(afList.length == 1){
              afList(0);
            } else if(afList.length == numSplit){
              afList(splitIdx);
            } else {
              problemList = problemList + "AF|popAFalleleMismatch";
              warning("Warning: allele freq annotation doesn't match numSplit: afList.length = "+afList.length+", numSplit = "+numSplit+"\n"+
                      "   "+"ref["+refAlle+"],ALTs=["+altAlleles.map(_._1).mkString(",")+"]"+key+"=["+v.getInfoList(key).map(_.toString()).mkString(",")+"]"+
                      (if(internalUtils.optionHolder.OPTION_DEBUGMODE) "\n   "+v.getSimpleVcfString() else ""),
                      "POPAF_FORMAT_WARNING",25);
              0.toDouble;
            }
          })
    
          //val (minAF, maxAF) = ctrlAlleFreqs.foldLeft( (1.toDouble,0.toDouble) ){ case ((minSF,maxSF),af) => {
          //  
           // val afOpt = if(afString == "."){
          //    None
           // } else Some(string2double(afString));
          //  (minOption(Some(minSF),afOpt).get, maxOption(Some(maxSF),afOpt).get)
          //}}
          val maxAF = ctrlAlleFreqs.foldLeft(0.toDouble){case (maxSF,af) => {
            math.max(maxSF,af);
          }}
          //vb =  vb.attribute(vcfCodes.assess_ctrlAFMIN , minAF.toString);
          vb.addInfo(vcfCodes.assess_ctrlAFMAX , maxAF.toString);
          vb.addInfo(vcfCodes.assess_BA1 , if(maxAF >= BA1_AF) "1" else "0"); //leave as boolean
          vb.addInfo(vcfCodes.assess_PM2 , if(maxAF <= PM2_AF) "1" else "0"); //leave as boolean
          crits.addCrit(new ACMGCrit("BA",1,maxAF >= BA1_AF,Seq[String]()));
          crits.addCrit(new ACMGCrit("PM",2,maxAF <= PM2_AF,Seq[String]()));
          canonCrits.addCrit(new ACMGCrit("BA",1,maxAF >= BA1_AF,Seq[String]()));
          canonCrits.addCrit(new ACMGCrit("PM",2,maxAF <= PM2_AF,Seq[String]()));
          
          //***************************genes:
          
          //val geneList = combo.map{case (g,tx,info,c,i) => g};
          val geneSet = geneList.toSet.toVector.sorted
          val geneSetCanon = canonCombo.map{ case (g,tx,info,c,i) => g}.toSet.toVector.sorted;
          
          vb.addInfo(vcfCodes.assess_ACMG_numGenes , geneSet.size.toString);
          vb.addInfo(vcfCodes.assess_ACMG_numGenes_CANON , geneSetCanon.size.toString);
          
          vb.addInfo(vcfCodes.assess_geneList , geneSet.padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_geneTxList , geneSet.map((g)  => { geneToTx(g).toVector.sorted.mkString("|") }).padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_refSeqKnown, geneSet.map( g => g + ":" + geneToTx(g).count(tx => isRefSeq(tx))).padTo(1,".").mkString(",") );
          
          if(geneSet.size > 1){
            problemList = problemList + "MG|MultipleGene";
          }
          
          val numVariantTypes = Seq[Boolean](
            (combo.exists{case (g,tx,info,c,i) => {
              info.severityType == "LLOF" || info.subType == "START-LOSS" || info.subType == "splice"
            }}),
            (combo.exists{case (g,tx,info,c,i) => {
              info.subType == "insAA" || info.subType == "delAA" || info.subType == "indelAA"
            }}),
            (combo.exists{case (g,tx,info,c,i) => {
              info.subType == "swapAA"
            }}),
            (combo.exists{case (g,tx,info,c,i) => {
              info.subType == "cds-synon"
            }})
          ).count(b => b);
          
          if(numVariantTypes > 1){
            problemList = problemList + "MV|MultipleVariantTypes";
          }
          
          //***************************LOF:
          val LofTX = combo.filter{case (g,tx,info,c,i) => {
            info.severityType == "LLOF" || info.subType == "START-LOSS" || info.subType == "splice"
          }}
          
          val LofGenes = LofTX.map{case (g,tx,info,c,i) => {g}}.toSet.toList.sorted;
    
          vb.addInfo(vcfCodes.assess_LofGenes , LofGenes.padTo(1,".").mkString(","));
          val LofTxList = LofTX.map{case (g,tx,info,c,i) => tx};
          vb.addInfo(vcfCodes.assess_LofTX , LofTxList.padTo(1,".").mkString(","))
          
          val geneLofTx = geneSet.map(g => {
            val txSet = geneToTx(g);
            val txLofCt = LofTxList.count{tx => { txSet.contains(tx) }};
            (g,txLofCt, txSet.size)
          });
          vb.addInfo(vcfCodes.assess_geneLofTxRatio , geneLofTx.map{case (g,a,b) => g+":"+a+"/"+b}.padTo(1,".").mkString(","))
          
          val refSeqLof = geneSet.filter{g => {
            val txSet = geneToTx(g);
            val txLof = LofTxList.filter(tx => {txSet.contains(tx)});
            txLof.exists(tx => {
              isRefSeq(tx);
            })
          }}
          vb.addInfo(vcfCodes.assess_LofGenes+"_CANON" , refSeqLof.padTo(1,".").mkString(","));
          
          val pvs1flag = LofGenes.filter(g => {
            geneIsLofSensitive(g);
          }).toSet.toVector.sorted;
          val pvs1flag_canon = refSeqLof.filter{g => {
            geneIsLofSensitive(g);
          }}.toSet.toVector.sorted;
          crits.addCrit(new ACMGCrit("PVS",1,pvs1flag.length > 0,Seq[String](),geneWiseFlag = Some(pvs1flag.toSet)));
          canonCrits.addCrit(new ACMGCrit("PVS",1,pvs1flag_canon.length > 0,Seq[String](),geneWiseFlag = Some(pvs1flag_canon.toSet)));
          
          vb.addInfo(vcfCodes.assess_PVS1 , if(pvs1flag.length > 0) "1" else "0"); //CONVERT TO GENEWISE
          vb.addInfo(vcfCodes.assess_PVS1_CANON , if(pvs1flag_canon.length > 0) "1" else "0");  //CONVERT TO GENEWISE
          
          if(includeGenewise) vb.addInfo(vcfCodes.assess_PVS1 + "_GENEWISE" , pvs1flag.padTo(1,".").mkString(",")); //CONVERT TO GENEWISE
          if(includeGenewise) vb.addInfo(vcfCodes.assess_PVS1_CANON + "_GENEWISE" , pvs1flag_canon.padTo(1,".").mkString(","));  //CONVERT TO GENEWISE
          
          //***************************Mis:
          val MisTX = combo.filter{case (g,tx,info,c,i) => {
            info.subType == "swapAA";
          }}
          val MisGenes = MisTX.map{case (g,tx,info,c,i) => {
            g
          }}.toSet.toList.sorted;
          
          val MisTxList = MisTX.map{case (g,tx,info,c,i) => tx};
          vb.addInfo(vcfCodes.assess_MisGenes , MisGenes.padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_MisTX , MisTxList.padTo(1,".").mkString(","));
          
          val geneMisTx = geneSet.map(g => {
            val txSet = geneToTx(g);
            val txMisCt = MisTxList.count{tx => { txSet.contains(tx) }};
            (g,txMisCt, txSet.size)
          });
          vb.addInfo(vcfCodes.assess_geneMisTxRatio , geneMisTx.map{case (g,a,b) => g+":"+a+"/"+b}.padTo(1,".").mkString(","))
          
          val refSeqMis = geneSet.filter{g => {
            val txSet = geneToTx(g);
            val txMis = MisTxList.filter(tx => {txSet.contains(tx)});
            txMis.exists(tx => {
              isRefSeq(tx);
            })
          }}
          vb.addInfo(vcfCodes.assess_MisGenes+"_CANON", refSeqMis.padTo(1,".").mkString(","));
          
          //Any Coding:
          val codingTX = combo.filter{case (g,tx,info,c,i) => {
            info.severityType == "PLOF" || info.severityType == "LLOF" || info.severityType == "NONSYNON" || info.severityType == "SYNON"
          }}
          val codingGenes = codingTX.map{case (g,tx,info,c,i) => {
            g
          }}.toSet.toList.sorted;
          val codingTxList = codingTX.map{case (g,tx,info,c,i) => tx};
          vb.addInfo(vcfCodes.assess_CodingGenes , codingGenes.padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_CodingTX , codingTxList.padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_CodingGenes+"_CANON" , codingTX.withFilter{case (g,tx,info,c,i) => isRefSeq(tx)}.map(_._1).toSet.toVector.sorted.padTo(1,".").mkString(","));
    
          //   ***************************Any NS:
          val nsTX = combo.filter{case (g,tx,info,c,i) => {
            info.severityType == "LLOF" || info.severityType == "PLOF" || info.severityType == "NONSYNON"
          }}
          val nsGenes = nsTX.map{case (g,tx,info,c,i) => {
            g
          }}.toSet.toList.sorted;
          val nsTxList = nsTX.map{case (g,tx,info,c,i) => tx};
          vb.addInfo(vcfCodes.assess_NonsynonGenes , nsGenes.padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_NonsynonTX ,    nsTxList.padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_NonsynonGenes+"_CANON" , nsTX.withFilter{case (g,tx,info,c,i) => isRefSeq(tx)}.map(_._1).toSet.toVector.sorted.padTo(1,".").mkString(","));
    
          //***************************
          val pp2flag = MisGenes.filter(g => geneIsMisSensitive(g));
          val bp1flag = MisGenes.filter(g => geneIsMisInsensitive(g));
          val simplebp1flag = (pvs1flag.length == 0) && (MisGenes.length > 0) && MisGenes.forall(g => {
            geneIsMisInsensitive(g);
          })
          val simplebp1flag_canon = (pvs1flag_canon.length == 0) && (refSeqMis.length > 0) && refSeqMis.forall(g => {
            geneIsMisInsensitive(g);
          })
          
          val bp1list = MisGenes.filter(g => {
            geneIsMisInsensitive(g)  && ! pvs1flag.contains(g)
          })
          val bp1list_canon = refSeqMis.filter(g => {
            geneIsMisInsensitive(g) && ! pvs1flag_canon.contains(g)
          })
          
          val pp2list = MisGenes.filter(g => {
            geneIsMisSensitive(g);
          });
          val pp2list_canon = refSeqMis.filter(g => {
            geneIsMisSensitive(g);
          });
          /*
          val pp2flag = MisGenes.exists(g => {
            geneIsMisSensitive(g);
          });
          val bp1flag = (MisGenes.length > 0) && MisGenes.forall(g => {
            geneIsMisInsensitive(g);
          })
          
          val pp2flag_canon = refSeqMis.exists(g => {
            geneIsMisSensitive(g);
          });
          val bp1flag_canon = (pvs1flag_canon == ".") && (refSeqMis.length > 0) && refSeqMis.forall(g => {
            geneIsMisInsensitive(g);
          })*/
          
          vb.addInfo(vcfCodes.assess_PP2 , if(pp2list.length > 0) "1" else "0");  //CONVERT TO GENEWISE
          vb.addInfo(vcfCodes.assess_BP1 , if(simplebp1flag) "1" else "0"); //CONVERT TO GENEWISE
          vb.addInfo(vcfCodes.assess_PP2_CANON , if(pp2list_canon.length > 0) "1" else "0");  //CONVERT TO GENEWISE
          vb.addInfo(vcfCodes.assess_BP1_CANON , if(simplebp1flag_canon) "1" else "0");  //CONVERT TO GENEWISE
           
          if(includeGenewise) vb.addInfo(vcfCodes.assess_PP2 + "_GENEWISE" , pp2list.toVector.sorted.padTo(1,".").mkString(","));  //CONVERT TO GENEWISE
          if(includeGenewise) vb.addInfo(vcfCodes.assess_BP1 + "_GENEWISE", bp1list.toVector.sorted.padTo(1,".").mkString(",")); //CONVERT TO GENEWISE
          if(includeGenewise) vb.addInfo(vcfCodes.assess_PP2_CANON + "_GENEWISE", pp2list_canon.toVector.sorted.padTo(1,".").mkString(","));  //CONVERT TO GENEWISE
          if(includeGenewise) vb.addInfo(vcfCodes.assess_BP1_CANON + "_GENEWISE", bp1list_canon.toVector.sorted.padTo(1,".").mkString(","));  //CONVERT TO GENEWISE
          
          crits.addCrit(new ACMGCrit("PP",2,pp2list.length > 0,Seq[String](), geneWiseFlag = Some(pp2list.toSet)));
          crits.addCrit(new ACMGCrit("BP",1,bp1list.length > 0,Seq[String](), geneWiseFlag = Some(bp1list.toSet)));
          canonCrits.addCrit(new ACMGCrit("PP",2,pp2list_canon.length > 0,Seq[String](), geneWiseFlag = Some(pp2list_canon.toSet)));
          canonCrits.addCrit(new ACMGCrit("BP",1,bp1list_canon.length > 0,Seq[String](), geneWiseFlag = Some(bp1list_canon.toSet)));
          
          //******************************* Length change variants:
          val pm4flag = {
            ((! isRepetitive) && ({
               combo.exists{case (g,tx,info,c,i) => {
                 info.subType == "insAA" || info.subType == "delAA" || info.subType == "indelAA"
               }}
            })) || combo.exists{case (g,tx,info,c,i) => {
                 info.pType == "STOP-LOSS"
               }}
          }
          val pm4flag_canon = {
            ((! isRepetitive) && ({
               canonCombo.exists{case (g,tx,info,c,i) => {
                 info.subType == "insAA" || info.subType == "delAA" || info.subType == "indelAA"
               }}
            })) || canonCombo.exists{case (g,tx,info,c,i) => {
                 info.pType == "STOP-LOSS"
               }}
          }
          val pm4list = combo.filter{case (g,tx,info,c,i) => {
            ((! isRepetitive) && (info.subType == "insAA" || info.subType == "delAA" || info.subType == "indelAA")) || (info.pType == "STOP-LOSS")
          }}.map{case (g,tx,info,c,i) => g}.toSet.toVector.sorted
          val pm4list_canon = canonCombo.filter{case (g,tx,info,c,i) => {
            ((! isRepetitive) && (info.subType == "insAA" || info.subType == "delAA" || info.subType == "indelAA")) || (info.pType == "STOP-LOSS")
          }}.map{case (g,tx,info,c,i) => g}.toSet.toVector.sorted
          
          vb.addInfo(vcfCodes.assess_PM4,if(pm4flag) "1" else "0"); //CONVERT TO GENEWISE
          vb.addInfo(vcfCodes.assess_PM4_CANON,if(pm4flag_canon) "1" else "0");  //CONVERT TO GENEWISE
          if(includeGenewise) vb.addInfo(vcfCodes.assess_PM4+"_GENEWISE",pm4list.padTo(1,".").mkString(",")); //CONVERT TO GENEWISE
          if(includeGenewise) vb.addInfo(vcfCodes.assess_PM4_CANON+"_GENEWISE",pm4list_canon.padTo(1,".").mkString(","));  //CONVERT TO GENEWISE
          
          val bp3flag = {
            (isRepetitive) && (! isHotspot) && ({
               combo.exists{case (g,tx,info,c,i) => {
                 info.subType == "insAA" || info.subType == "delAA" || info.subType == "indelAA"
               }}
            })
          }
          val bp3flag_canon = {
            (isRepetitive) && (! isHotspot) && ({
               canonCombo.exists{case (g,tx,info,c,i) => {
                 info.subType == "insAA" || info.subType == "delAA" || info.subType == "indelAA"
               }}
            })
          }
          vb.addInfo(vcfCodes.assess_BP3,if(bp3flag) "1" else "0"); //leave as boolean
          vb.addInfo(vcfCodes.assess_BP3_CANON,if(bp3flag_canon) "1" else "0"); //leave as boolean
          
          crits.addCrit(new ACMGCrit("BP",3,bp3flag,Seq[String]()));
          crits.addCrit(new ACMGCrit("PM",4,pm4flag,Seq[String](),geneWiseFlag = Some(pm4list.toSet)));
          canonCrits.addCrit(new ACMGCrit("BP",3,bp3flag_canon,Seq[String]()));
          canonCrits.addCrit(new ACMGCrit("PM",4,pm4flag_canon,Seq[String](),geneWiseFlag = Some(pm4list_canon.toSet)));
          
          //******************************* ClinVar matching:
          
          var ps1 = ACMGCrit("PS",1,false,Seq[String]());
          var pm5 = ACMGCrit("PM",5,false,Seq[String]());
          var ps1RS = Set[String]();
          var pm5RS = Set[String](); 
          val variantString = v.chrom + ":"+v.getInfo(vcfCodes.vMutG_TAG);
          val varInfo = clinVarVariantSet.get(variantString);
          //val (exactAllRS,exactAllSig,exactAllRawSig) = clinVarVariantSet.getOrElse(variantString,("",1,""))
          val exactRS = if( varInfo.isDefined && varInfo.get.isPatho ){
            varInfo.get.idString();
          } else {
            ""
          }
          
          var downstreamPathoLOFRS = Set[String]();
          var codonPathoLOFRS = Set[String]();
          var downstreamPathoLOFRS_CANON = Set[String]();
          var codonPathoLOFRS_CANON = Set[String]();
          combo.filter{ case (g,tx,info,c,i) => {
            info.severityType == "LLOF" || info.pType == "START-LOSS" || info.subType == "splice" || info.subType == "possSplice"
          }}.foreach{ case (g,tx,info,c,i) => {
            val txvar = clinVarVariants_LOFSet(tx);
            val txvar_downstream_patho = txvar.filter{ p => {
              p.start >= info.start && (p.isPatho)
            }}
            val txvar_downstream_patho_rs = txvar_downstream_patho.map{ p => p.ID }
            val txvar_codon_patho = txvar_downstream_patho.filter{ p => {
              p.start == info.start;
            }}
            val txvar_codon_patho_rs = txvar_codon_patho.map{p => p.ID}
            downstreamPathoLOFRS = downstreamPathoLOFRS ++ txvar_downstream_patho_rs;
            codonPathoLOFRS = codonPathoLOFRS ++ txvar_codon_patho_rs;
            if(isRefSeq(tx)){
              downstreamPathoLOFRS_CANON = downstreamPathoLOFRS_CANON ++ txvar_downstream_patho_rs;
              codonPathoLOFRS_CANON = codonPathoLOFRS_CANON ++ txvar_codon_patho_rs;
            }
          }}
          
    
          //vb =  vb.attribute(vcfCodes.assess_forBT_exactLOF,      exactPathoRS.toList.sorted.map{_.toString}.padTo(1,".").mkString(","));
          
          vb.addInfo(vcfCodes.assess_forBT_codonLOF,      codonPathoLOFRS.toList.sorted.map{_.toString}.padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_forBT_codonLOF_CANON,codonPathoLOFRS_CANON.toList.sorted.map{_.toString}.padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_forBT_downstreamLOF,      downstreamPathoLOFRS.toList.sorted.map{_.toString}.padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_forBT_downstreamLOF_CANON,downstreamPathoLOFRS_CANON.toList.sorted.map{_.toString}.padTo(1,".").mkString(","));
          
          var geneHasDownstreamLOF = Set[String]();
          var txHasDownstreamLOF = Set[String]();
          var geneHasDownstreamLOF_CANON = Set[String]();
          var txHasDownstreamLOF_CANON = Set[String]();
          
          var geneHasStartLoss = Set[String]();
          var txHasStartLoss = Set[String]();
          var geneHasStartLoss_CANON = Set[String]();
          var txHasStartLoss_CANON = Set[String]();
          
          var geneHasStopLoss = Set[String]();
          var txHasStopLoss = Set[String]();
          var geneHasStopLoss_CANON = Set[String]();
          var txHasStopLoss_CANON = Set[String]();
          
          combo.foreach{ case (g,tx,info,c,i) => {
            val hasDownstreamPatho = clinVarVariants_LOFSet.getOrElse(tx,Set()).exists{ p => {
              p.start >= info.start && (p.isPatho)
            }}
            if(hasDownstreamPatho){
              geneHasDownstreamLOF = geneHasDownstreamLOF + g;
              txHasDownstreamLOF = txHasDownstreamLOF + tx;
              if(isRefSeq(tx)){
                geneHasDownstreamLOF_CANON = geneHasDownstreamLOF_CANON + g;
                txHasDownstreamLOF_CANON = txHasDownstreamLOF_CANON + tx;
              }
            }
            
            val hasStartLoss = clinVarVariants_startLoss.getOrElse(tx,Set()).exists{ p => p.isPatho}
            if(hasStartLoss){
              geneHasStartLoss = geneHasStartLoss + g;
              txHasStartLoss = txHasStartLoss + tx;
              if(isRefSeq(tx)){
                geneHasStartLoss_CANON = geneHasStartLoss_CANON + g;
                txHasStartLoss_CANON = txHasStartLoss_CANON + tx;
              }
            }
            
            val hasStopLoss = clinVarVariants_stopLoss(tx).exists{ p => p.isPatho}
            if(hasStopLoss){
              geneHasStopLoss = geneHasStopLoss + g;
              txHasStopLoss = txHasStopLoss + tx;
              if(isRefSeq(tx)){
                geneHasStopLoss_CANON = geneHasStopLoss_CANON + g;
                txHasStopLoss_CANON = txHasStopLoss_CANON + tx;
              }
            }
          }}
          
          vb.addInfo(vcfCodes.assess_forBT_geneHasDownstreamLOF,        geneHasDownstreamLOF.toList.sorted.map{_.toString}.padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_forBT_geneHasDownstreamLOF_CANON,  geneHasDownstreamLOF_CANON.toList.sorted.map{_.toString}.padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_forBT_geneHasStartLoss,            geneHasStartLoss.toList.sorted.map{_.toString}.padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_forBT_geneHasStartLoss_CANON,      geneHasStartLoss_CANON.toList.sorted.map{_.toString}.padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_forBT_geneHasStopLoss,             geneHasStopLoss.toList.sorted.map{_.toString}.padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_forBT_geneHasStopLoss_CANON,       geneHasStopLoss_CANON.toList.sorted.map{_.toString}.padTo(1,".").mkString(","));
          
           // assess_forBT_geneHasDownstreamLOF : String = TOP_LEVEL_VCF_TAG+"ASSESS_geneList_hasPathoDownstreamLOF",
          //  assess_forBT_geneHasStartLoss : String = TOP_LEVEL_VCF_TAG+"ASSESS_geneList_hasPathoStartLoss",
          //  assess_forBT_geneHasStopLoss : String = TOP_LEVEL_VCF_TAG+"ASSESS_geneList_hasPathoStopLoss",
          //  assess_forBT_geneHasDownstreamLOF_CANON : String = TOP_LEVEL_VCF_TAG+"ASSESS_geneList_hasPathoDownstreamLOF_CANON",
          //  assess_forBT_geneHasStartLoss_CANON : String = TOP_LEVEL_VCF_TAG+"ASSESS_geneList_hasPathoStartLoss_CANON",
          //  assess_forBT_geneHasStopLoss_CANON : String = TOP_LEVEL_VCF_TAG+"ASSESS_geneList_hasPathoStopLoss_CANON",
          
          val aminoMatchInfo = scala.collection.mutable.AnyRefMap[TXUtil.KnownVariantInfoHolder,Set[String]]();
          val nearMatchInfo = scala.collection.mutable.AnyRefMap[TXUtil.KnownVariantInfoHolder,Set[String]]();
          
          var ps1RS_canon = Set[String]();
          var pm5RS_canon = Set[String]();
          var ps1_canon = ACMGCrit("PS",1,false,Seq[String]());
          var pm5_canon = ACMGCrit("PM",5,false,Seq[String]());
          var aminoMatchInfo_canon =  scala.collection.mutable.AnyRefMap[TXUtil.KnownVariantInfoHolder,Set[String]]();
          var nearMatchInfo_canon =  scala.collection.mutable.AnyRefMap[TXUtil.KnownVariantInfoHolder,Set[String]]();
          
          combo.withFilter{case (g,tx,info,c,i) => {
            info.severityType == "NONSYNON"
          }}.foreach{case (g,tx,info,c,i) => {
            val txvar = clinVarVariants(tx) //.filter(cvinfo => cvinfo.isPatho)
            val aminoMatchSet = txvar.filter{cvinfo => {cvinfo.start == info.start && cvinfo.subType == info.subType && cvinfo.pvar == info.pvar}};
            val newAminoMatchInfo = aminoMatchSet.map(cvinfo => cvinfo.dbVarInfo.get).toSet
            newAminoMatchInfo.foreach{ ami => {
              aminoMatchInfo(ami) = aminoMatchInfo.getOrElse(ami,Set[String]()) + tx;
            }}
            //aminoMatchInfo = aminoMatchInfo ++ newAminoMatchInfo
            if(isRefSeq(tx)) {
              //aminoMatchInfo_canon = aminoMatchInfo_canon ++ newAminoMatchInfo;
              newAminoMatchInfo.foreach{ ami => {
                aminoMatchInfo_canon(ami) = aminoMatchInfo_canon.getOrElse(ami,Set[String]()) + tx;
              }}
            }
            val aminoPathoMatch = aminoMatchSet.filter(cvinfo => cvinfo.isPatho);
            val aminoBenignMatch = aminoMatchSet.filter(cvinfo => cvinfo.isBenign);
            
            if(aminoPathoMatch.size > 0){
              ps1 = ps1.merge(ACMGCrit("PS",1,true,Seq[String](tx + ":" + info.pvar)));
              notice("Adding aminoMatch: rsnums:[\""+aminoPathoMatch.map(_.ID).toSet.mkString("\",\"")+"\"] \n"+
                      "                          [\""+aminoPathoMatch.map{(cvinfo) => { cvinfo.txid+":"+cvinfo.pvar+":"+cvinfo.ID }}.mkString("\",\"")+"\"]",
                      "addAminoMatchRS",5);
              ps1RS = ps1RS ++ aminoPathoMatch.map(_.ID).toSet
              
              if(isRefSeq(tx)){
                ps1_canon = ps1_canon.merge(ACMGCrit("PS",1,true,Seq[String](tx + ":" + info.pvar)));
                ps1RS_canon = ps1RS_canon ++ aminoPathoMatch.map(_.ID).toSet
              }
            }
            
            if(info.subType == "swapAA"){
              val partialMatch = txvar.filter{(cvinfo) => {cvinfo.subType == "swapAA" && cvinfo.start == info.start && cvinfo.altAA != info.altAA}};
              val partialPathoMatch = partialMatch.filter(cvinfo => cvinfo.isPatho);
              val newNearMatchInfo = partialMatch.map(cvinfo => cvinfo.dbVarInfo.get).toSet;
              //pm5RS = pm5RS ++ partialPathoMatch.map(_.ID).toSet;
              pm5RS = pm5RS ++ partialPathoMatch.map( m => m.dbVarInfo.get.pathoIdString() ).toSet;
              //nearMatchInfo = nearMatchInfo ++ newNearMatchInfo;
              newNearMatchInfo.foreach{ ami => {
                nearMatchInfo(ami) = nearMatchInfo.getOrElse(ami,Set[String]()) + tx;
              }}
              if(isRefSeq(tx)){
                //pm5RS_canon = pm5RS_canon ++ partialPathoMatch.map(_.ID).toSet;
                pm5RS_canon = pm5RS_canon ++ partialPathoMatch.map( m => m.dbVarInfo.get.pathoIdString() ).toSet;
                //nearMatchInfo_canon = nearMatchInfo_canon ++ newNearMatchInfo;
                newNearMatchInfo.foreach{ ami => {
                  nearMatchInfo_canon(ami) = nearMatchInfo_canon.getOrElse(ami,Set[String]()) + tx;
                }}
              }
              if(partialPathoMatch.size > 0 && aminoPathoMatch.size == 0){
                pm5 = pm5.merge(ACMGCrit("PM",5,true,Seq[String](tx+":"+info.pvar+"Vs"+partialPathoMatch.head.altAA)));
                if(isRefSeq(tx)) pm5_canon = pm5_canon.merge(ACMGCrit("PM",5,true,Seq[String](tx+":"+info.pvar+"Vs"+partialPathoMatch.head.altAA)));
              }
            } else if(info.pType == "STOP-LOSS"){
              //do stuff?
            } else if(info.subType == "indelAA" || info.subType == "insAA" || info.subType == "delAA"){ //what about "multSwapAA"?
              //do stuff?
            }
          }}
          
          if(ps1.flag){
            vb.addInfo(vcfCodes.assess_PS1,"1"); //CONVERT TO GENEWISE
            vb.addInfo(vcfCodes.assess_PM5,"0"); //CONVERT TO GENEWISE
          } else if(pm5.flag){
            vb.addInfo(vcfCodes.assess_PS1,"0"); //CONVERT TO GENEWISE
            vb.addInfo(vcfCodes.assess_PM5,"1"); //CONVERT TO GENEWISE
          } else {
            vb.addInfo(vcfCodes.assess_PS1,"0"); //CONVERT TO GENEWISE
            vb.addInfo(vcfCodes.assess_PM5,"0"); //CONVERT TO GENEWISE
          }
          if(ps1_canon.flag){
            vb.addInfo(vcfCodes.assess_PS1_CANON,"1"); //CONVERT TO GENEWISE
            vb.addInfo(vcfCodes.assess_PM5_CANON,"0"); //CONVERT TO GENEWISE
          } else if(pm5_canon.flag){
            vb.addInfo(vcfCodes.assess_PS1_CANON,"0"); //CONVERT TO GENEWISE
            vb.addInfo(vcfCodes.assess_PM5_CANON,"1"); //CONVERT TO GENEWISE
          } else {
            vb.addInfo(vcfCodes.assess_PS1_CANON,"0"); //CONVERT TO GENEWISE
            vb.addInfo(vcfCodes.assess_PM5_CANON,"0"); //CONVERT TO GENEWISE
          }
          
          val ps1set = aminoMatchInfo.filter{ case (ami,txSet) => { ami.isPatho }}.flatMap{ case (ami,txSet) => txSet.map{txToGene(_)} }.toSet
          val pm5set = nearMatchInfo.filter{ case (ami,txSet) => { ami.isPatho }}.flatMap{ case (ami,txSet) => txSet.map{txToGene(_)} }.toSet -- ps1set;
          val ps1set_canon = aminoMatchInfo_canon.filter{ case (ami,txSet) => { ami.isPatho }}.flatMap{ case (ami,txSet) => txSet.map{txToGene(_)} }.toSet
          val pm5set_canon = nearMatchInfo_canon.filter{ case (ami,txSet) => { ami.isPatho }}.flatMap{ case (ami,txSet) => txSet.map{txToGene(_)} }.toSet -- ps1set_canon;
          
          if(includeGenewise) vb.addInfo(vcfCodes.assess_PS1+"_GENEWISE",ps1set.toVector.distinct.sorted.mkString(","));
          if(includeGenewise) vb.addInfo(vcfCodes.assess_PM5+"_GENEWISE",pm5set.toVector.distinct.sorted.mkString(","));
          if(includeGenewise) vb.addInfo(vcfCodes.assess_PS1_CANON+"_GENEWISE",ps1set_canon.toVector.distinct.sorted.mkString(","));
          if(includeGenewise) vb.addInfo(vcfCodes.assess_PM5_CANON+"_GENEWISE",pm5set_canon.toVector.distinct.sorted.mkString(","));
          
          crits.addCrit(ps1.addGeneWise(ps1set));
          crits.addCrit(pm5.addGeneWise(pm5set));
          canonCrits.addCrit(ps1_canon.addGeneWise(ps1set_canon));
          canonCrits.addCrit(pm5_canon.addGeneWise(pm5set_canon));
          
          //vb.addInfo(vcfCodes.assess_pathoExactMatchRS, if(exactRS == "") "." else exactRS);
          //varInfo.get.idString();
          vb.addInfo(vcfCodes.assess_pathoExactMatchRS, getOrFunc(varInfo, ".")(vi => vi.pathoIdString()));
          vb.addInfo(vcfCodes.assess_pathoAminoMatchRS, ps1RS.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_pathoNearMatchRS,  pm5RS.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_pathoAminoMatchRS_CANON, ps1RS_canon.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_pathoNearMatchRS_CANON,  pm5RS_canon.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
          
          vb.addInfo(vcfCodes.assess_pathoExactMatchRS+"_ClinVar", (varInfo match {
            case Some(vinf) => {
              if(vinf.isPathogenic(true,false)){
                vinf.clinVarID.getOrElse(".")
              } else {
                "."
              }
            }
            case None => ".";
          }));
          vb.addInfo(vcfCodes.assess_pathoAminoMatchRS+"_ClinVar", aminoMatchInfo.filter{ case (b,txSet) => b.isPathogenic(true,false) && b.clinVarID.isDefined}.map{ case (b,txSet) => {
            b.clinVarID.getOrElse(".")
          }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_pathoNearMatchRS+"_ClinVar",  nearMatchInfo.filter{ case (b,txSet) => b.isPathogenic(true,false) && b.clinVarID.isDefined}.map{ case (b,txSet) => {
            b.clinVarID.getOrElse(".")
          }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_pathoAminoMatchRS_CANON+"_ClinVar", aminoMatchInfo_canon.filter{ case (b,txSet) => b.isPathogenic(true,false) && b.clinVarID.isDefined}.map{ case (b,txSet) => {
            b.clinVarID.getOrElse(".")
          }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_pathoNearMatchRS_CANON+"_ClinVar",  nearMatchInfo_canon.filter{ case (b,txSet) => b.isPathogenic(true,false) && b.clinVarID.isDefined}.map{ case (b,txSet) => {
            b.clinVarID.getOrElse(".")
          }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
          
          
          //vb.addInfo(vcfCodes.assess_pathoExactMatchRS, getOrFunc(varInfo, ".")(vi => vi.pathoIdString()));
          vb.addInfo(vcfCodes.assess_benignExactMatchRS+"_ClinVar", getOrFunc(varInfo, ".")(vi => if(vi.isBenign(true,false)) vi.clinVarID.get else "." ));
          vb.addInfo(vcfCodes.assess_benignAminoMatchRS+"_ClinVar", aminoMatchInfo.filter{ case (b,txSet) => b.isBenign(true,false) && b.clinVarID.isDefined}.map{ case (b,txSet) => {
            b.clinVarID.get
          }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_benignAminoMatchRS_CANON+"_ClinVar", aminoMatchInfo_canon.filter{ case (b,txSet) => b.isBenign(true,false) && b.clinVarID.isDefined}.map{ case (b,txSet) => {
            b.clinVarID.get
          }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_benignNearMatchRS+"_ClinVar",  nearMatchInfo.filter{ case (b,txSet) => b.isBenign(true,false) && b.clinVarID.isDefined}.map{ case (b,txSet) => {
            b.clinVarID.get
          }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_benignNearMatchRS_CANON+"_ClinVar",  nearMatchInfo_canon.filter{ case (b,txSet) => b.isBenign(true,false) && b.clinVarID.isDefined}.map{ case (b,txSet) => {
            b.clinVarID.get
          }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
          
          
          vb.addInfo(vcfCodes.assess_pathoExactMatchRS+"_HGMD", (varInfo match {
            case Some(vinf) => {
              if(vinf.isPathogenic(false,true)){
                vinf.hgmdID.getOrElse(".")
              } else {
                "."
              }
            }
            case None => ".";
          }));
          vb.addInfo(vcfCodes.assess_pathoAminoMatchRS+"_HGMD", aminoMatchInfo.filter{ case (b,txSet) => b.isPathogenic(false,true) && b.hgmdID.isDefined}.map{ case (b,txSet) => {
            b.hgmdID.getOrElse(".")
          }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_pathoNearMatchRS+"_HGMD",  nearMatchInfo.filter{ case (b,txSet) => b.isPathogenic(false,true) && b.hgmdID.isDefined}.map{ case (b,txSet) => {
            b.hgmdID.getOrElse(".")
          }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_pathoAminoMatchRS_CANON+"_HGMD", aminoMatchInfo_canon.filter{ case (b,txSet) => b.isPathogenic(false,true) && b.hgmdID.isDefined}.map{ case (b,txSet) => {
            b.hgmdID.getOrElse(".")
          }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_pathoNearMatchRS_CANON+"_HGMD",  nearMatchInfo_canon.filter{ case (b,txSet) => b.isPathogenic(false,true) && b.hgmdID.isDefined}.map{ case (b,txSet) => {
            b.hgmdID.getOrElse(".")
          }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
          
          //        assess_exactMatchInfo : String = TOP_LEVEL_VCF_TAG+"ACMG_cvInfo_ExactMatch",
          //  assess_aminoMatchInfo : String = TOP_LEVEL_VCF_TAG+"ACMG_cvInfo_AminoMatch",
          //  assess_nearMatchInfo : String = TOP_LEVEL_VCF_TAG+"ACMG_cvInfo_NearMatch",
          
            val varInfoString = varInfo match {
              case Some(vinf) => {
                vinf.fullInfoString()
              }
              case None => {
                ".";
              }
            }
            
          vb.addInfo(vcfCodes.assess_exactMatchInfo,  varInfoString);
          vb.addInfo(vcfCodes.assess_aminoMatchInfo, aminoMatchInfo.toVector.sortBy{ case (b,txSet) => b.id }.map{case (b,txSet) => b.fullInfoString() + "|" + txSet.toVector.sorted.mkString("/")+"|"+txSet.map{tx => txToGene(tx)}.toVector.sorted.mkString("/")}.padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_nearMatchInfo,  nearMatchInfo.toVector.sortBy{ case (b,txSet) => b.id }.map{case (b,txSet) => b.fullInfoString() + "|" + txSet.toVector.sorted.mkString("/")+"|"+txSet.map{tx => txToGene(tx)}.toVector.sorted.mkString("/")}.padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_aminoMatchInfo_CANON, aminoMatchInfo_canon.toVector.sortBy{ case (b,txSet) => b.id }.map{case (b,txSet) => b.fullInfoString() + "|" + txSet.toVector.sorted.mkString("/")+"|"+txSet.map{tx => txToGene(tx)}.toVector.sorted.mkString("/")}.padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_nearMatchInfo_CANON,  nearMatchInfo_canon.toVector.sortBy{ case (b,txSet) => b.id }.map{case (b,txSet) => b.fullInfoString() + "|" + txSet.toVector.sorted.mkString("/")+"|"+txSet.map{tx => txToGene(tx)}.toVector.sorted.mkString("/")}.padTo(1,".").mkString(","));
          
          vb.addInfo(vcfCodes.assess_exactMatchInfo+"_ClinVar", (varInfo match {
            case Some(vinf) => {
              vinf.infoString(clinVar=true,hgmd=false)
            }
            case None => ".";
          }));
          
          vb.addInfo(vcfCodes.assess_aminoMatchInfo+"_ClinVar", aminoMatchInfo.filter{ case (b,txSet) => b.clinVarID.isDefined}.map{ case (b,txSet) => {
            b.infoString(clinVar = true,hgmd=false);
          }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_nearMatchInfo+"_ClinVar",  nearMatchInfo.filter{ case (b,txSet) => b.clinVarID.isDefined}.map{ case (b,txSet) => {
            b.infoString(clinVar = true,hgmd=false);
          }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_aminoMatchInfo_CANON+"_ClinVar", aminoMatchInfo_canon.filter{ case (b,txSet) => b.clinVarID.isDefined}.map{ case (b,txSet) => {
            b.infoString(clinVar = true,hgmd=false);
          }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_nearMatchInfo_CANON+"_ClinVar",  nearMatchInfo_canon.filter{ case (b,txSet) => b.clinVarID.isDefined}.map{ case (b,txSet) => {
            b.infoString(clinVar = true,hgmd=false);
          }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
          
          vb.addInfo(vcfCodes.assess_exactMatchInfo+"_HGMD", (varInfo match {
            case Some(vinf) => {
              vinf.infoString(clinVar=false,hgmd=true)
            }
            case None => ".";
          }));
          vb.addInfo(vcfCodes.assess_aminoMatchInfo+"_HGMD", aminoMatchInfo.filter{ case (b,txSet) => b.hgmdID.isDefined}.map{ case (b,txSet) => {
            b.infoString(clinVar = false,hgmd=true);
          }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_nearMatchInfo+"_HGMD",  nearMatchInfo.filter{ case (b,txSet) => b.hgmdID.isDefined}.map{ case (b,txSet) => {
            b.infoString(clinVar = false,hgmd=true);
          }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_aminoMatchInfo_CANON+"_HGMD", aminoMatchInfo_canon.filter{ case (b,txSet) => b.hgmdID.isDefined}.map{ case (b,txSet) => {
            b.infoString(clinVar = false,hgmd=true);
          }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
          vb.addInfo(vcfCodes.assess_nearMatchInfo_CANON+"_HGMD",  nearMatchInfo_canon.filter{ case (b,txSet) => b.hgmdID.isDefined}.map{ case (b,txSet) => {
            b.infoString(clinVar = false,hgmd=true);
          }}.toSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
          
          //******************************* Insilico:
          //inSilicoMin
          //val numSplit = v.getAttributeAsInt(vcfCodes.numSplit_TAG,1);
          val rawAlles = v.getInfoList(vcfCodes.splitAlle_TAG);
          
          if(inSilicoFuns.length > 0){
            val (inSilicoResult,algSummaries) = inSilicoSummary(v);
            
            val isDamaging = inSilicoResult == "Damaging"
            val isBenign   = inSilicoResult == "Benign"
            
            crits.addCrit(new ACMGCrit("PP",3,isDamaging,Seq[String]()));
            crits.addCrit(new ACMGCrit("BP",4,isBenign,Seq[String]()));
            canonCrits.addCrit(new ACMGCrit("PP",3,isDamaging,Seq[String]()));
            canonCrits.addCrit(new ACMGCrit("BP",4,isBenign,Seq[String]()));
            vb.addInfo(vcfCodes.assess_PP3, if(isDamaging) "1" else "0" ); //Leave as boolean
            vb.addInfo(vcfCodes.assess_BP4, if(isBenign) "1" else "0" ); //Leave as boolean
            
            if(inSilicoResult != "."){
              vb.addInfo(vcfCodes.assess_inSilicoSummary, inSilicoResult );
            }
            
            //inSilicoFuns.map(_._1).foreach{sf => {
                    //new VCFInfoHeaderLine(vcfCodes.assess_inSilicoSummary + "_"+ sf
              inSilicoFuns.zip(algSummaries).foreach{ case ((tag,damFun,benFun),status) => {  
                vb.addInfo(vcfCodes.assess_inSilicoSummary + "_"+ tag,status);
              }}
              
            //}}
          }
          
          //******************************* BP7: Synonymous w/ no splice impact, not highly conserved:
          val bp7flag = (! isConserved) && combo.forall{case (g,tx,info,c,i) => {
            info.severityType == "SYNON" || info.severityType == "PSYNON" || info.severityType == "UNK"
          }}
          val bp7list = if(isConserved){
            (combo.filter{ case (g,tx,info,c,i) => info.severityType == "SYNON" }.map{ case (g,tx,info,c,i) => g }.toSet -- 
            combo.filter{case (g,tx,info,c,i) => {
              ! (info.severityType == "SYNON" || info.severityType == "PSYNON" || info.severityType == "UNK")
            }}.map{ case (g,tx,info,c,i) => { g }}.toSet).toVector.sorted;
          } else {
            Vector[String]();
          }
          val bp7list_CANON = if(isConserved){
            (canonCombo.filter{ case (g,tx,info,c,i) => info.severityType == "SYNON" }.map{ case (g,tx,info,c,i) => g }.toSet -- 
            canonCombo.filter{case (g,tx,info,c,i) => {
              ! (info.severityType == "SYNON" || info.severityType == "PSYNON" || info.severityType == "UNK")
            }}.map{ case (g,tx,info,c,i) => { g }}.toSet).toVector.sorted;
          } else {
            Vector[String]();
          }
          vb.addInfo(vcfCodes.assess_BP7, if(bp7flag) "1" else "0" ); //CONVERT TO GENEWISE
          vb.addInfo(vcfCodes.assess_BP7 + "_GENEWISE", bp7list.padTo(1,".").mkString(",") );
          vb.addInfo(vcfCodes.assess_BP7_CANON + "_GENEWISE", bp7list_CANON.padTo(1,".").mkString(",") );
          crits.addCrit(new ACMGCrit("BP",7,bp7flag,Seq[String](),geneWiseFlag = Some(bp7list.toSet)));
          
          val bp7flag_CANON = (! isConserved) && canonCombo.forall{ case (g,tx,info,c,i) => {
            info.severityType == "SYNON" || info.severityType == "PSYNON" || info.severityType == "UNK"
          }}
          vb.addInfo(vcfCodes.assess_BP7_CANON, if(bp7flag_CANON) "1" else "0" ); //CONVERT TO GENEWISE
          canonCrits.addCrit(new ACMGCrit("BP",7,bp7flag_CANON,Seq[String](),geneWiseFlag = Some(bp7list_CANON.toSet)));
          
          //*******************************
          
          for((critLvl, critNum, critTAG) <- VcfTool.UNAUTOMATED_ACMG_PARAMS){
            val critVal = v.getInfo(critTAG)
            if(critVal != "."){
              crits.addCrit(new ACMGCrit(critLvl,critNum,critVal == "1",Seq[String]()));
              canonCrits.addCrit(new ACMGCrit(critLvl,critNum,critVal == "1",Seq[String]()));
            }
          }
          
          //CONVERT TO GENEWISE:
          val rating = CalcACMGVar.getACMGPathogenicityRating(crits.getCritSet);
          val rating_CANON = CalcACMGVar.getACMGPathogenicityRating(canonCrits.getCritSet);
          
          vb.addInfo(vcfCodes.assess_RATING, rating );
          vb.addInfo(vcfCodes.assess_RATING_CANON, rating_CANON );
          if(includeGenewise) {
            vb.addInfo(vcfCodes.assess_RATING + "_GENEWISE", CalcACMGVar.getGenewiseACMGPathogenicityRating(geneSet,crits.getCritSet).map{ case (g,pr) => g + ":"+pr }.mkString(","));
            vb.addInfo(vcfCodes.assess_RATING_CANON + "_GENEWISE", CalcACMGVar.getGenewiseACMGPathogenicityRating(geneSetCanon,canonCrits.getCritSet).map{ case (g,pr) => g + ":"+pr }.mkString(","));
          }
           //       assess_ACMG_numGenes : String = TOP_LEVEL_VCF_TAG+"ACMG_NUM_GENES",
           // assess_ACMG_numGenes_CANON : String = TOP_LEVEL_VCF_TAG+"ACMG_NUM_GENES_CANON",
            
          /*
                new VCFInfoHeaderLine(vcfCodes.assess_PVS1, 1, VCFHeaderLineType.Integer,    ""),
                new VCFInfoHeaderLine(vcfCodes.assess_PS1, 1, VCFHeaderLineType.Integer,      "Variant has the same amino acid change as a pathogenic variant from ClinVar."),
                new VCFInfoHeaderLine(vcfCodes.assess_PM1, 1, VCFHeaderLineType.Integer,      "Located in a known domain. (currently just any domain)"),
                new VCFInfoHeaderLine(vcfCodes.assess_PM2, 1, VCFHeaderLineType.Integer,      "Alt allele frequency less than or equal to "+PM2_AF+" in all control datasets ("+ctrlAlleFreqKeys.mkString(",")+")"),
                new VCFInfoHeaderLine(vcfCodes.assess_PM4, 1, VCFHeaderLineType.Integer,      "Protein length change in nonrepeat region OR stop-loss variant."),
                new VCFInfoHeaderLine(vcfCodes.assess_PM5, 1, VCFHeaderLineType.Integer,      "Novel missense variant change at amino acid where a different amino acid change is known pathogenic in ClinVar."),
                new VCFInfoHeaderLine(vcfCodes.assess_PP2, 1, VCFHeaderLineType.Integer,      "Missense variant in gene that is missense-sensitive (missense variants are at least 10pct of known pathogenic variants in clinvar)."),
                new VCFInfoHeaderLine(vcfCodes.assess_BP1, 1, VCFHeaderLineType.Integer,      "Missense variant in gene that is NOT missense-sensitive (less than 10pct of pathogenic variants are missense)"),
                new VCFInfoHeaderLine(vcfCodes.assess_BP3, 1, VCFHeaderLineType.Integer,      "In-frame indels in a repetitive region that does NOT overlap with any known domain."),
                new VCFInfoHeaderLine(vcfCodes.assess_BP7, 1, VCFHeaderLineType.Integer,      "Synonymous variant that does NOT intersect with a conserved element region."),
                new VCFInfoHeaderLine(vcfCodes.assess_BA1,    1, VCFHeaderLineType.Integer,   "Allele frequency greater than 5 percent in one or more control dataset ("+ctrlAlleFreqKeys.mkString(",")+")"),
                            
                new VCFInfoHeaderLine(vcfCodes.assess_PVS1_CANON, 1, VCFHeaderLineType.Integer,    "(Considering the canonical TX only) Loss-of-function variant in gene that is LOF-sensitive. Loss-of-function is defined as one of:"+
                                                                                             "stop-gain, frameshift, total-gene-indel, or splice junction indel. "+
                                                                                             "A gene is defined as LOF-sensitive if at least 10pct of pathogenic "+
                                                                                             "clinvar variants are LOF-type variants."),
                new VCFInfoHeaderLine(vcfCodes.assess_PS1_CANON, 1, VCFHeaderLineType.Integer,"(Considering the canonical TX only) Variant has the same amino acid change as a pathogenic variant from ClinVar."),
                new VCFInfoHeaderLine(vcfCodes.assess_PP2_CANON, 1, VCFHeaderLineType.Integer,"(Considering the canonical TX only) Missense variant in gene that is missense-sensitive (missense variants are at least 10pct of known pathogenic variants in clinvar)."),
                new VCFInfoHeaderLine(vcfCodes.assess_BP1_CANON, 1, VCFHeaderLineType.Integer,"(Considering the canonical TX only) Missense variant in gene that is NOT missense-sensitive (less than 10pct of pathogenic variants are missense)"),
                new VCFInfoHeaderLine(vcfCodes.assess_BP3_CANON, 1, VCFHeaderLineType.Integer,"(Considering the canonical TX only) In-frame indels in a repetitive region that does NOT overlap with any known domain."),
                new VCFInfoHeaderLine(vcfCodes.assess_PM4_CANON, 1, VCFHeaderLineType.Integer,      "(Considering the canonical TX only) Protein length change in nonrepeat region OR stop-loss variant."),
                new VCFInfoHeaderLine(vcfCodes.assess_PM5_CANON, 1, VCFHeaderLineType.Integer,      "(Considering the canonical TX only) Novel missense variant change at amino acid where a different amino acid change is known pathogenic in ClinVar."),
                new VCFInfoHeaderLine(vcfCodes.assess_BP7_CANON, 1, VCFHeaderLineType.Integer,      "(Considering the canonical TX only) Synonymous variant that does NOT intersect with a conserved element region."),
                new VCFInfoHeaderLine(vcfCodes.assess_RATING_CANON, 1, VCFHeaderLineType.String,    "(Considering the canonical TX only) ACMG Pathogenicity rating: PATHO - pathogenic. LPATH - likely pathogenic, VUS - variant, unknown significance, LB - likely benign, B - benign."),
           */
        //vb =  vb.attribute(vcfCodes.assess_PP3,if(inSilicoFlag,"1","0"));
          //val inSilico
          
          
          
          //BP3:
          
          //crits;
        //}}.toVector;
        
        //return (out,vb.make());
    } else {
      notice("No tx found for variant","NO_TX_FOUND",0);
    }
    
    vb.addInfo(vcfCodes.assess_WARNFLAG, if(problemList.isEmpty) "0" else "1" );
    vb.addInfo(vcfCodes.assess_WARNINGS, problemList.toVector.sorted.padTo(1,".").mkString(",") );
    
    return vb
  }
  
  
  def getClinVarVariants(infile : String, chromList : Option[List[String]], vcfCodes : VCFAnnoCodes = VCFAnnoCodes()) : (scala.collection.Map[String,String],scala.collection.Map[String,Set[(String,internalUtils.TXUtil.pVariantInfo,String)]]) = {
    val (vcIter,vcfHeader) = internalUtils.VcfTool.getVcfIterator(infile, 
                                                                  chromList = chromList,
                                                                  vcfCodes = vcfCodes);
    
    val out = new scala.collection.mutable.AnyRefMap[String,Set[(String,internalUtils.TXUtil.pVariantInfo,String)]](((k : String) => Set[(String,internalUtils.TXUtil.pVariantInfo,String)]()));
    var gset = new scala.collection.mutable.AnyRefMap[String,String]();
    
    reportln("Starting VCF read/write...","progress");
    for(v <- vcIter){
      try {
      val rsnum = v.getAttributeAsString("RS","unknownRSNUM") //v.getID();
      val refAlle = v.getReference();
      val altAlleles = Range(0,v.getNAlleles()-1).map((a) => v.getAlternateAllele(a));
      //val vTypesList = v.getAttributeAsList(vcfCodes.vType_TAG).toVector.map(_.toString.split(vcfCodes.delims(1)).toVector);
      val txList = v.getAttributeAsList(vcfCodes.txList_TAG).asScala.toVector.map(_.toString).filter(_ != ".");
      //val vMutPList = v.getAttributeAsList(vcfCodes.vMutP_TAG).toVector.map(_.toString.split(vcfCodes.delims(1)).toVector);
      val vMutCListRaw = v.getAttributeAsList(vcfCodes.vMutP_TAG).asScala.toVector.filter(_ != ".");
      val vMutCList = vMutCListRaw.map(_.toString.split("\\|").toVector);
      val chrom = v.getContig();
      
      val vMutGList = v.getAttributeAsList(vcfCodes.vMutG_TAG).asScala.toVector.filter(_ != ".");

      
      val vMutInfoList = if(txList.length > 0) {
          v.getAttributeAsList(vcfCodes.vMutINFO_TAG).asScala.toVector.map( (attrObj) => {
          val attrString = attrObj.toString();
          val attrSplit = attrString.split("\\|").toVector
          //reportln("vMutInfoListDebug: attrString = \""+attrString+"\"","debug");
          //reportln("vMutInfoListDebug: attrSplit["+attrSplit.length+"] = [\""+attrSplit.mkString("\", \"")+"\"]","debug");
          
          attrSplit.map(x => {
            internalUtils.TXUtil.getPvarInfoFromString(x, ID = rsnum);
          })
        })
      } else {
        Vector();
      }
      val vClnSig = v.getAttributeAsList("CLNSIG").asScala.toVector.map(_.toString());
      
      
      if(txList.length > 0) { 
        for((alle,altIdx) <- altAlleles.zipWithIndex){
          
      //vMutGList.foreach(g => {
      //  gset(chrom + ":" + g) = rsnum;
      //})
           
           //val vTypes = vTypesList(altIdx);
           //val vMutP = vMutPList(altIdx);
           val vMutC = vMutCList(altIdx);
           val vInfo = vMutInfoList(altIdx);
           val clnSig = vClnSig(altIdx).split("\\|");
           val hasBenign = clnSig.exists(p => p == "2" || p == "3");
           val hasPatho  = clnSig.exists(p => p == "4" || p == "5");
           
           if(vMutC.length != txList.length){
             reportln("vMutC.length = "+vMutC.length+", txList = "+txList+"\nvMutC = [\""+vMutC.mkString("\",\"")+"\"]","debug");
           }
           if(hasPatho && (! hasBenign)){
             for(i <- Range(0,txList.length)){
               val tx = txList(i);
               out(tx) = out(tx) + ((vMutC(i),vInfo(i),rsnum));
             }
             gset(chrom + ":" + vMutGList(altIdx)) = rsnum;
           }
        }
      }

      } catch {
        case e : Exception => {
          reportln("Caught Exception on line:","note");
          reportln(v.toStringWithoutGenotypes(),"note");
          throw e;
        }
      }
    }
    
    out.keySet.toVector.slice(0,10).foreach(tx => {
      val varSeq = out(tx);
      reportln("Example ClinVar TX: "+tx,"debug");
      varSeq.slice(0,10).foreach{ case (cvc,info,rsnum) => {
        reportln("      " + info.txid + ":" + info.pvar + ":"+rsnum,"debug");
      }}
    })
    
    return (gset,out);
  }
  
  

  //output: tx => Set[(HGVDc,pVariantInfo])]
  def getFullClinVarVariantsNew(infile : String, chromList : Option[List[String]], vcfCodes : VCFAnnoCodes = VCFAnnoCodes(), hgmdVarVcf : Option[String] = None,
                                hgmdPathogenicClasses : Seq[String]
                            ) : (scala.collection.mutable.Map[String,TXUtil.KnownVariantInfoHolder],scala.collection.mutable.Map[String,Set[internalUtils.TXUtil.pVariantInfo]]) = {
    val hgmdPathogenicClassSet = hgmdPathogenicClasses.toSet
    val out = new scala.collection.mutable.AnyRefMap[String,Set[internalUtils.TXUtil.pVariantInfo]](((k : String) => Set[internalUtils.TXUtil.pVariantInfo]()));
    
    //gvariation
    //val gset = new scala.collection.mutable.AnyRefMap[String,TXUtil.KnownVariantInfoHolder]();
    
    val variantInfo = new scala.collection.mutable.AnyRefMap[String,internalUtils.TXUtil.KnownVariantInfoHolder]();
    
    if(true){
      val (vcIter,vcfHeader) = internalUtils.VcfTool.getVcfIterator(infile, 
                                                                  chromList = chromList,
                                                                  vcfCodes = vcfCodes);
      reportln("Starting ClinVar VCF read...","progress");
      for(v <- vcIter){
        try {
          val rsnum = "rs"+v.getAttributeAsString("RS","UNK") //v.getID();
          val refAlle = v.getReference();
          val altAlleles = Range(0,v.getNAlleles()-1).map((a) => v.getAlternateAllele(a));
          //val vTypesList = v.getAttributeAsList(vcfCodes.vType_TAG).toVector.map(_.toString.split(vcfCodes.delims(1)).toVector);
          val txList = v.getAttributeAsList(vcfCodes.txList_TAG).asScala.toVector.map(_.toString).filter(_ != ".");
          //val vMutPList = v.getAttributeAsList(vcfCodes.vMutP_TAG).toVector.map(_.toString.split(vcfCodes.delims(1)).toVector);
          val vMutCListRaw = v.getAttributeAsList(vcfCodes.vMutP_TAG).asScala.toVector.filter(_ != ".");
          val vMutCList = vMutCListRaw.map(_.toString.split("\\|").toVector);
          val chrom = v.getContig();
          
          val vMutGList = v.getAttributeAsList(vcfCodes.vMutG_TAG).asScala.toVector.filter(_ != ".");
          //val vClnSig = v.getAttributeAsList("CLNSIG").asScala.toVector.map(_.toString());
          //val clnsig = v.getAttributeAsList("CLNSIG").asScala.toVector.map(_.toString());
          val isPatho = v.getAttributeAsInt("CLNSIG_ANYPATHO",0);
          val isBenign = v.getAttributeAsInt("CLNSIG_ANYBENIGN",0);
          val clnsig = if(isPatho == 1) 5 else if(isBenign == 1) 2 else 1;
          val clnInfo = v.getAttributeAsList("CLNSIG").asScala.mkString(":")  + "|" + v.getAttributeAsList("CLNSIGCONF").asScala.mkString(":") + "|"+ v.getAttributeAsList("CLNREVSTAT").asScala.mkString(":")
          /*vClnSig.zipWithIndex.map{case (sigstr,altidx) => {
            val clnSig = sigstr.split("\\|");
            (getSummaryClinSig(clnSig),clnSig.mkString(":"));
          }}*/
          val altAlleIndexed =   altAlleles.zipWithIndex.filter{case (a,i) => { a.getBaseString() != "*" }}
          
          
          
          val varids = altAlleIndexed.map{ case (alle,altIdx) => {
            chrom + ":" + vMutGList(altIdx)
          }}
          val dbVarInfoList = altAlleIndexed.zip(varids).map{ case ((alle,altIdx),varid) => {
            TXUtil.KnownVariantInfoHolder(id = varid, clinVarID = Some(rsnum), 
                                                               clinVarPatho = Some(clnsig),  clinVarFullPatho = Some(clnInfo),
                                                               hgmdID = None, hgmdClass = None,
                                                               hgmdPathogenicClassSet=hgmdPathogenicClassSet
                                                               )
          }}
          dbVarInfoList.zip(varids).foreach{ case (dbVarInfo,varid) => {
            variantInfo(varid) = dbVarInfo;
          }}
          
          if(txList.length > 0) {
            //v.getContig() + ":"+v.getAttributeAsString(vcfCodes.vMutG_TAG,"?!?");
            val vMutInfoList : Vector[Vector[TXUtil.pVariantInfo]] = {
              v.getAttributeAsList(vcfCodes.vMutINFO_TAG).asScala.toVector.zipWithIndex.map{ case (attrObj, altIdx) => {
                val attrString = attrObj.toString();
                val attrSplit = attrString.split("\\|").toVector
                //reportln("vMutInfoListDebug: attrString = \""+attrString+"\"","debug");
                //reportln("vMutInfoListDebug: attrSplit["+attrSplit.length+"] = [\""+attrSplit.mkString("\", \"")+"\"]","debug");
                val varid = chrom + ":" + vMutGList(altIdx)
                val dbVarInfo = dbVarInfoList(altIdx);
                attrSplit.map{ case (x) => {
                  //val clnSig = vClnSig(altIdx).split("\\|");
                  //0 - Uncertain significance, 1 - not provided, 2 - Benign, 3 - Likely benign, 4 - Likely pathogenic, 5 - Pathogenic, 6 - drug response, 7 - histocompatibility, 255 - other                        
                  internalUtils.TXUtil.getPvarInfoFromString(x, ID = rsnum,dbVarInfo=Some(dbVarInfo));
                }}
              }}
            }
            
            for((alle,altIdx) <- altAlleIndexed){
               //vMutGList.foreach(g => {
               //  gset(chrom + ":" + g) = rsnum;
               //})
               //val vTypes = vTypesList(altIdx);
               //val vMutP = vMutPList(altIdx);
               val vMutC = vMutCList(altIdx);
               val vInfo = vMutInfoList(altIdx);
               
               if(vMutC.length != txList.length){
                 reportln("vMutC.length = "+vMutC.length+", txList = "+txList+"\nvMutC = [\""+vMutC.mkString("\",\"")+"\"]","debug");
               }
               //if(hasPatho && (! hasBenign)){
               //val varid = chrom + ":" + vMutGList(altIdx)
                                      
               for(i <- Range(0,txList.length)){
                 val tx = txList(i);
                 out(tx) = out(tx) + (vInfo(i));
               }
               
               
               
               //gset(varid) = (rsnum,clnsig(altIdx)._1,clnsig(altIdx)._2);
               //val clinVarPathoScore = clnsig(altIdx)._1
               //val isPatho = (clinVarPathoScore == 4 || clinVarPathoScore == 5);
               //val isBenign = (clinVarPathoScore == 2 || clinVarPathoScore == 3);
               //val isUnk = (! isPatho) && (! isBenign)
                 
               //}
            }
          }
  
        } catch {
          case e : Exception => {
            reportln("Caught Exception on line:","note");
            reportln(v.toStringWithoutGenotypes(),"note");
            throw e;
          }
        }
      }
      
      out.keySet.toVector.slice(0,10).foreach(tx => {
        val varSeq = out(tx);
        reportln("Example ClinVar TX: "+tx,"debug");
        varSeq.slice(0,10).foreach{ (info) => {
          reportln("      " + info.debugStatusString(),"debug");
        }}
      })
    }
    
    hgmdVarVcf match {
      case None => {
        //do nothing!
      }
      case Some(hgmdVcf) => {
        val (vcIter,vcfHeader) = internalUtils.VcfTool.getVcfIterator(hgmdVcf, 
                                                                  chromList = chromList,
                                                                  vcfCodes = vcfCodes);
        reportln("Starting HGMD VCF read...","progress");
        
        for(v <- vcIter){
          try {
            val rsnum = v.getAttributeAsString("ACC_NUM","UNK") //v.getID();
            val refAlle = v.getReference();
            val altAlleles = Range(0,v.getNAlleles()-1).map((a) => v.getAlternateAllele(a));
            //val vTypesList = v.getAttributeAsList(vcfCodes.vType_TAG).toVector.map(_.toString.split(vcfCodes.delims(1)).toVector);
            val txList = v.getAttributeAsList(vcfCodes.txList_TAG).asScala.toVector.map(_.toString).filter(_ != ".");
            //val vMutPList = v.getAttributeAsList(vcfCodes.vMutP_TAG).toVector.map(_.toString.split(vcfCodes.delims(1)).toVector);
            val vMutCListRaw = v.getAttributeAsList(vcfCodes.vMutC_TAG).asScala.toVector.filter(_ != ".");
            val vMutCList = vMutCListRaw.map(_.toString.split("\\|").toVector);
            val chrom = v.getContig();
            
            val vMutGList = v.getAttributeAsList(vcfCodes.vMutG_TAG).asScala.toVector.map(_.toString()).filter(_ != ".");
            
            val hgmdClass = v.getAttributeAsString("CLASS","UNK")
            
            val altAlleIndexed =   altAlleles.zipWithIndex.filter{case (a,i) => { a.getBaseString() != "*" }}
            val varids = altAlleIndexed.map{ case (alle,altIdx) => {
              chrom + ":" + vMutGList(altIdx)
            }}
            val dbVarInfoList = altAlleIndexed.zip(varids).map{ case ((alle,altIdx),varid) => {
              variantInfo.get(varid) match {
                    case Some(vi) =>{
                      vi.mergeHgmdVariant(rsnum, hgmdClass)
                      vi;
                    }
                    case None =>{
                      val vi = internalUtils.TXUtil.KnownVariantInfoHolder(id = varid, clinVarID = None, clinVarPatho =None, clinVarFullPatho =None,
                                                   hgmdID = Some(rsnum),
                                                   hgmdClass =Some(hgmdClass),
                                                   hgmdPathogenicClassSet =hgmdPathogenicClassSet)
                      variantInfo(varid) = vi;
                      vi;
                    }
              }
            }}
            
            
            if(txList.length > 0) {
              val vMutInfoList = {
                v.getAttributeAsList(vcfCodes.vMutINFO_TAG).asScala.toVector.zipWithIndex.map{ case (attrObj, altIdx) => {
                  val attrString = attrObj.toString();
                  val attrSplit = attrString.split("\\|").toVector
                  val varid = chrom + ":" + vMutGList(altIdx)
                  val dbVarInfo = dbVarInfoList(altIdx);
                  
                  attrSplit.map{ case (x) => {
                    //val clnSig = vClnSig(altIdx).split("\\|");
                    //0 - Uncertain significance, 1 - not provided, 2 - Benign, 3 - Likely benign, 4 - Likely pathogenic, 5 - Pathogenic, 6 - drug response, 7 - histocompatibility, 255 - other                        
                    internalUtils.TXUtil.getPvarInfoFromString(x, ID = ""+rsnum,dbVarInfo=Some(dbVarInfo));
                  }}
                }}
              }
            
              for((alle,altIdx) <- altAlleIndexed){
                 val vMutC = vMutCList(altIdx);
                 val vInfo = vMutInfoList(altIdx);
                 
                 if(vMutC.length != txList.length){
                   reportln("vMutC.length = "+vMutC.length+", txList = "+txList+"\nvMutC = [\""+vMutC.mkString("\",\"")+"\"]","debug");
                 }
                 for(i <- Range(0,txList.length)){
                   val tx = txList(i);
                   out(tx) = out(tx) + (vInfo(i));
                 }
                 //val varid = chrom + ":" + vMutGList(altIdx)
                /* if(gset.containsKey(varid)){
                   val oldVal = gset(varid);
                   //gset(varid) = (oldVal._1+"/"+rsnum,5,oldVal._3+"/HGMD_"+hgmdClass);
                   variantInfo(varid).mergeHgmdVariant(rsnum, hgmdClass)
                 } else {
                   //gset(varid) = (""+rsnum,5,hgmdClass);
                   variantInfo(varid) = internalUtils.TXUtil.KnownVariantInfoHolder(id = varid, clinVarID = None, clinVarPatho =None, clinVarFullPatho =None,
                                                 hgmdID = Some(rsnum),
                                                 hgmdClass =Some(hgmdClass),
                                                 hgmdPathogenicClassSet =hgmdPathogenicClassSet)
                 }*/
              }
            }
            
          }  catch {
            case e : Exception => {
              reportln("Caught Exception on line:","note");
              reportln(v.toStringWithoutGenotypes(),"note");
              throw e;
            }
          }
        }
      }
    }
    
    return (variantInfo,out);
  }
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  

  //output: tx => Set[(HGVDc,pVariantInfo])]
  def getFullClinVarVariants(infile : String, chromList : Option[List[String]], vcfCodes : VCFAnnoCodes = veeOneSeven_vcfAnnoCodes, hgmdVarVcf : Option[String] = None,
                             hgmdPathogenicClasses : Seq[String]
                            ) : (scala.collection.mutable.Map[String,TXUtil.KnownVariantInfoHolder],scala.collection.mutable.Map[String,Set[internalUtils.TXUtil.pVariantInfo]]) = {
    val hgmdPathogenicClassSet = hgmdPathogenicClasses.toSet
    val out = new scala.collection.mutable.AnyRefMap[String,Set[internalUtils.TXUtil.pVariantInfo]](((k : String) => Set[internalUtils.TXUtil.pVariantInfo]()));
    
    //gvariation
    //val gset = new scala.collection.mutable.AnyRefMap[String,TXUtil.KnownVariantInfoHolder]();
    
    val variantInfo = new scala.collection.mutable.AnyRefMap[String,internalUtils.TXUtil.KnownVariantInfoHolder]();
    
    if(true){
      val (vcIter,vcfHeader) = internalUtils.VcfTool.getVcfIterator(infile, 
                                                                  chromList = chromList,
                                                                  vcfCodes = vcfCodes);
      reportln("Starting ClinVar VCF read...","progress");
      for(v <- vcIter){
        try {
          val rsnum = "rs"+v.getAttributeAsString("RS","UNK") //v.getID();
          val refAlle = v.getReference();
          val altAlleles = Range(0,v.getNAlleles()-1).map((a) => v.getAlternateAllele(a));
          //val vTypesList = v.getAttributeAsList(vcfCodes.vType_TAG).toVector.map(_.toString.split(vcfCodes.delims(1)).toVector);
          val txList = v.getAttributeAsList(vcfCodes.txList_TAG).asScala.toVector.map(_.toString).filter(_ != ".");
          //val vMutPList = v.getAttributeAsList(vcfCodes.vMutP_TAG).toVector.map(_.toString.split(vcfCodes.delims(1)).toVector);
          val vMutCListRaw = v.getAttributeAsList(vcfCodes.vMutP_TAG).asScala.toVector.filter(_ != ".");
          val vMutCList = vMutCListRaw.map(_.toString.split("\\|").toVector);
          val chrom = v.getContig();
          
          val vMutGList = v.getAttributeAsList(vcfCodes.vMutG_TAG).asScala.toVector.filter(_ != ".");
          val vClnSig = v.getAttributeAsList("CLNSIG").asScala.toVector.map(_.toString());
          val clnsig = vClnSig.zipWithIndex.map{case (sigstr,altidx) => {
            val clnSig = sigstr.split("\\|");
            (getSummaryClinSig(clnSig),clnSig.mkString(":"));
          }}
          val altAlleIndexed =   altAlleles.zipWithIndex.filter{case (a,i) => { a.getBaseString() != "*" }}
          val varids = altAlleIndexed.map{ case (alle,altIdx) => {
            chrom + ":" + vMutGList(altIdx)
          }}
          val dbVarInfoList = altAlleIndexed.zip(varids).map{ case ((alle,altIdx),varid) => {
            val (cs,csRaw) = clnsig(altIdx)
            TXUtil.KnownVariantInfoHolder(id = varid, clinVarID = Some(rsnum), 
                                                               clinVarPatho = Some(cs),  clinVarFullPatho = Some(csRaw),
                                                               hgmdID = None, hgmdClass = None,
                                                               hgmdPathogenicClassSet=hgmdPathogenicClassSet
                                                               )
          }}
          dbVarInfoList.zip(varids).foreach{ case (dbVarInfo,varid) => {
            variantInfo(varid) = dbVarInfo;
          }}
          
          if(txList.length > 0) {
            //v.getContig() + ":"+v.getAttributeAsString(vcfCodes.vMutG_TAG,"?!?");
            val vMutInfoList : Vector[Vector[TXUtil.pVariantInfo]] = {
              v.getAttributeAsList(vcfCodes.vMutINFO_TAG).asScala.toVector.zipWithIndex.map{ case (attrObj, altIdx) => {
                val attrString = attrObj.toString();
                val attrSplit = attrString.split("\\|").toVector
                //reportln("vMutInfoListDebug: attrString = \""+attrString+"\"","debug");
                //reportln("vMutInfoListDebug: attrSplit["+attrSplit.length+"] = [\""+attrSplit.mkString("\", \"")+"\"]","debug");
                val varid = chrom + ":" + vMutGList(altIdx)
                val dbVarInfo = dbVarInfoList(altIdx);
                attrSplit.map{ case (x) => {
                  //val clnSig = vClnSig(altIdx).split("\\|");
                  //0 - Uncertain significance, 1 - not provided, 2 - Benign, 3 - Likely benign, 4 - Likely pathogenic, 5 - Pathogenic, 6 - drug response, 7 - histocompatibility, 255 - other                        
                  internalUtils.TXUtil.getPvarInfoFromString(x, ID = rsnum,dbVarInfo=Some(dbVarInfo));
                }}
              }}
            }
            
            for((alle,altIdx) <- altAlleIndexed){
               //vMutGList.foreach(g => {
               //  gset(chrom + ":" + g) = rsnum;
               //})
               //val vTypes = vTypesList(altIdx);
               //val vMutP = vMutPList(altIdx);
               val vMutC = vMutCList(altIdx);
               val vInfo = vMutInfoList(altIdx);
               
               if(vMutC.length != txList.length){
                 reportln("vMutC.length = "+vMutC.length+", txList = "+txList+"\nvMutC = [\""+vMutC.mkString("\",\"")+"\"]","debug");
               }
               //if(hasPatho && (! hasBenign)){
               //val varid = chrom + ":" + vMutGList(altIdx)
                                      
               for(i <- Range(0,txList.length)){
                 val tx = txList(i);
                 out(tx) = out(tx) + (vInfo(i));
               }
               
               
               
               //gset(varid) = (rsnum,clnsig(altIdx)._1,clnsig(altIdx)._2);
               //val clinVarPathoScore = clnsig(altIdx)._1
               //val isPatho = (clinVarPathoScore == 4 || clinVarPathoScore == 5);
               //val isBenign = (clinVarPathoScore == 2 || clinVarPathoScore == 3);
               //val isUnk = (! isPatho) && (! isBenign)
                 
               //}
            }
          }
  
        } catch {
          case e : Exception => {
            reportln("Caught Exception on line:","note");
            reportln(v.toStringWithoutGenotypes(),"note");
            throw e;
          }
        }
      }
      
      out.keySet.toVector.slice(0,10).foreach(tx => {
        val varSeq = out(tx);
        reportln("Example ClinVar TX: "+tx,"debug");
        varSeq.slice(0,10).foreach{ (info) => {
          reportln("      " + info.debugStatusString(),"debug");
        }}
      })
    }
    
    hgmdVarVcf match {
      case None => {
        //do nothing!
      }
      case Some(hgmdVcf) => {
        val (vcIter,vcfHeader) = internalUtils.VcfTool.getVcfIterator(hgmdVcf, 
                                                                  chromList = chromList,
                                                                  vcfCodes = vcfCodes);
        reportln("Starting HGMD VCF read...","progress");
        
        for(v <- vcIter){
          try {
            val rsnum = v.getAttributeAsString("ACC_NUM","UNK") //v.getID();
            val refAlle = v.getReference();
            val altAlleles = Range(0,v.getNAlleles()-1).map((a) => v.getAlternateAllele(a));
            //val vTypesList = v.getAttributeAsList(vcfCodes.vType_TAG).toVector.map(_.toString.split(vcfCodes.delims(1)).toVector);
            val txList = v.getAttributeAsList(vcfCodes.txList_TAG).asScala.toVector.map(_.toString).filter(_ != ".");
            //val vMutPList = v.getAttributeAsList(vcfCodes.vMutP_TAG).toVector.map(_.toString.split(vcfCodes.delims(1)).toVector);
            val vMutCListRaw = v.getAttributeAsList(vcfCodes.vMutC_TAG).asScala.toVector.filter(_ != ".");
            val vMutCList = vMutCListRaw.map(_.toString.split("\\|").toVector);
            val chrom = v.getContig();
            
            val vMutGList = v.getAttributeAsList(vcfCodes.vMutG_TAG).asScala.toVector.map(_.toString()).filter(_ != ".");
            
            val hgmdClass = v.getAttributeAsString("CLASS","UNK")
            
            val altAlleIndexed =   altAlleles.zipWithIndex.filter{case (a,i) => { a.getBaseString() != "*" }}
            val varids = altAlleIndexed.map{ case (alle,altIdx) => {
              chrom + ":" + vMutGList(altIdx)
            }}
            val dbVarInfoList = altAlleIndexed.zip(varids).map{ case ((alle,altIdx),varid) => {
              variantInfo.get(varid) match {
                    case Some(vi) =>{
                      vi.mergeHgmdVariant(rsnum, hgmdClass)
                      vi;
                    }
                    case None =>{
                      val vi = internalUtils.TXUtil.KnownVariantInfoHolder(id = varid, clinVarID = None, clinVarPatho =None, clinVarFullPatho =None,
                                                   hgmdID = Some(rsnum),
                                                   hgmdClass =Some(hgmdClass),
                                                   hgmdPathogenicClassSet =hgmdPathogenicClassSet)
                      variantInfo(varid) = vi;
                      vi;
                    }
              }
            }}
            
            
            if(txList.length > 0) {
              val vMutInfoList = {
                v.getAttributeAsList(vcfCodes.vMutINFO_TAG).asScala.toVector.zipWithIndex.map{ case (attrObj, altIdx) => {
                  val attrString = attrObj.toString();
                  val attrSplit = attrString.split("\\|").toVector
                  val varid = chrom + ":" + vMutGList(altIdx)
                  val dbVarInfo = dbVarInfoList(altIdx);
                  
                  attrSplit.map{ case (x) => {
                    //val clnSig = vClnSig(altIdx).split("\\|");
                    //0 - Uncertain significance, 1 - not provided, 2 - Benign, 3 - Likely benign, 4 - Likely pathogenic, 5 - Pathogenic, 6 - drug response, 7 - histocompatibility, 255 - other                        
                    internalUtils.TXUtil.getPvarInfoFromString(x, ID = ""+rsnum,dbVarInfo=Some(dbVarInfo));
                  }}
                }}
              }
            
              for((alle,altIdx) <- altAlleIndexed){
                 val vMutC = vMutCList(altIdx);
                 val vInfo = vMutInfoList(altIdx);
                 
                 if(vMutC.length != txList.length){
                   reportln("vMutC.length = "+vMutC.length+", txList = "+txList+"\nvMutC = [\""+vMutC.mkString("\",\"")+"\"]","debug");
                 }
                 for(i <- Range(0,txList.length)){
                   val tx = txList(i);
                   out(tx) = out(tx) + (vInfo(i));
                 }
                 //val varid = chrom + ":" + vMutGList(altIdx)
                /* if(gset.containsKey(varid)){
                   val oldVal = gset(varid);
                   //gset(varid) = (oldVal._1+"/"+rsnum,5,oldVal._3+"/HGMD_"+hgmdClass);
                   variantInfo(varid).mergeHgmdVariant(rsnum, hgmdClass)
                 } else {
                   //gset(varid) = (""+rsnum,5,hgmdClass);
                   variantInfo(varid) = internalUtils.TXUtil.KnownVariantInfoHolder(id = varid, clinVarID = None, clinVarPatho =None, clinVarFullPatho =None,
                                                 hgmdID = Some(rsnum),
                                                 hgmdClass =Some(hgmdClass),
                                                 hgmdPathogenicClassSet =hgmdPathogenicClassSet)
                 }*/
              }
            }
            
          }  catch {
            case e : Exception => {
              reportln("Caught Exception on line:","note");
              reportln(v.toStringWithoutGenotypes(),"note");
              throw e;
            }
          }
        }
      }
    }
    
    return (variantInfo,out);
  }
  
  def getSummaryClinSig(clnSig : Seq[String]) : Int = {
      var cs = 1;
      
      if( clnSig.contains("1")){ // "Not Provided"
        cs = 1;
      }
      if( clnSig.contains("0") ){ // "Uncertain significance"
        cs = 0;
      }
      if( clnSig.contains("255")){ // "other"
       cs = 255;
      }
      if( clnSig.contains("6")){ // "drug response"
        cs = 6;
      }
      if( clnSig.contains("7")){ // "histocompatibility"
        cs = 7;
      }
      if(clnSig.contains("3")){ // "likely benign"
        cs = 3;
      }
      if(clnSig.contains("2")){ // "benign"
        cs = 2;
      }
      // 4 = "Likely Pathogenic"
      // 5 = "Pathogenic"
      
      if(clnSig.contains("5") && (! clnSig.contains("2"))){
        return 5;
      } else if(clnSig.contains("5") && clnSig.contains("2")){
        return 0;
      } else if(clnSig.contains("2")){
        return 2;
      } else if(clnSig.contains("3") && clnSig.contains("4")){
        return 0;
      } else if(clnSig.contains("3")){
        return 3;
      } else if(clnSig.contains("4")){
        return 4;
      }
      
      return cs;
  }
  
//##INFO=<ID=CLNHGVS,Number=.,Type=String,Description="Variant names from HGVS.    The order of these variants corresponds to the order of the info in the other clinical  INFO tags.">
//##INFO=<ID=CLNALLE,Number=.,Type=Integer,Description="Variant alleles from REF or ALT columns.  0 is REF, 1 is the first ALT allele, etc.  This is used to match alleles with other corresponding clinic
//##INFO=<ID=CLNSRC,Number=.,Type=String,Description="Variant Clinical Chanels">
//##INFO=<ID=CLNORIGIN,Number=.,Type=String,Description="Allele Origin. One or more of the following values may be added: 0 - unknown; 1 - germline; 2 - somatic; 4 - inherited; 8 - paternal; 16 - matern
//##INFO=<ID=CLNSRCID,Number=.,Type=String,Description="Variant Clinical Channel IDs">
//##INFO=<ID=CLNSIG,Number=.,Type=String,Description="Variant Clinical Significance, 0 - Uncertain significance, 1 - not provided, 2 - Benign, 3 - Likely benign, 4 - Likely pathogenic, 5 - Pathogenic, 6
//##INFO=<ID=CLNDSDB,Number=.,Type=String,Description="Variant disease database name">
//##INFO=<ID=CLNDSDBID,Number=.,Type=String,Description="Variant disease database ID">
//##INFO=<ID=CLNDBN,Number=.,Type=String,Description="Variant disease name">
//##INFO=<ID=CLNREVSTAT,Number=.,Type=String,Description="no_assertion - No assertion provided, no_criteria - No assertion criteria provided, single - Criteria provided single submitter, mult - Criteria
//##INFO=<ID=CLNACC,Number=.,Type=String,Description="Variant Accession and Versions">
  
  case class ClinVariantHolder(txid : String, varp : String, varc : String, varType : Array[String]){
    lazy val isSwap : Boolean = varType.last == "swapAA";
    lazy val posAA : Int = if(isSwap){
      string2int(varc.slice(5,varc.length-3));
    } else {
      -1; //NOT IMPLEMENTED YET!
    }
  }
  
  def getRepetitiveFunction(rmskFile : String) : ((String,Int) => Boolean) = {
    val geneArray : internalUtils.genomicAnnoUtils.GenomicArrayOfSets[String] = internalUtils.genomicAnnoUtils.GenomicArrayOfSets[String](false);
    
    for(line <- getLinesSmartUnzip(rmskFile)){
      val cells = line.split("\t");
      val (chrom,start,end,repClass) = (cells(5),string2int(cells(6)),string2int(cells(7)),cells(11));
      geneArray.addSpan(internalUtils.commonSeqUtils.GenomicInterval(chrom,'.',start,end),repClass);
    }
    
    val finalArray = geneArray.finalizeStepVectors;
    
    return ((chrom : String, pos : Int) => {
      ! finalArray.findSetAtPosition(chrom,pos,'.').isEmpty
    })
  }
  
  def assessVariantOLD(v : VariantContext, 
                    clinVarVariants : scala.collection.Map[String,Set[(String,internalUtils.TXUtil.pVariantInfo)]],
                    txIsLofSensitive : (String => Boolean) = ((s : String) => true),
                    txIsMisSensitive : (String => Boolean) = ((s : String) => true),
                    locusIsRepetitive : ((String,Int) => Boolean) = ((s : String,i : Int) => false),
                    locusIsConserved  : ((String,Int) => Boolean) = ((s : String,i : Int) => true ),
                    locusIsHotspot    : ((String,Int) => Boolean) = ((s : String,i : Int) => false ),
                    ctrlAlleFreqKeys : Seq[String] = Seq("1KG_AF","ESP_EA_AF","ExAC_ALL","SWH_AF_GRP_CTRL"),
                    
                    vcfCodes : VCFAnnoCodes = VCFAnnoCodes()
                    //clinVarVariants?
                    ) : (Vector[ACMGCritSet],VariantContext) = {
    var vb = new htsjdk.variant.variantcontext.VariantContextBuilder(v);
    
    val chrom = v.getContig();
    val pos = v.getStart();
    
    val refAlle = v.getReference();
    val altAlleles = Range(0,v.getNAlleles()-1).map((a) => v.getAlternateAllele(a)).zipWithIndex.filter{case (alt,altIdx) => { alt.getBaseString() != "*" }}
    
    val vTypesList = v.getAttributeAsList(vcfCodes.vType_TAG).asScala.toVector.map(_.toString.split(vcfCodes.delims(1)).toVector);
    val vLVL       = v.getAttributeAsList(vcfCodes.vMutLVL_TAG).asScala.toVector.map(_.toString);
    val txList = v.getAttributeAsList(vcfCodes.txList_TAG).asScala.toVector.map(_.toString);
    val vMutPList = v.getAttributeAsList(vcfCodes.vMutP_TAG).asScala.toVector.map(_.toString.split(vcfCodes.delims(1)).toVector);
    val vMutCList = v.getAttributeAsList(vcfCodes.vMutC_TAG).asScala.toVector.map(_.toString.split(vcfCodes.delims(1)).toVector);
    val vMutInfoList = v.getAttributeAsList(vcfCodes.vMutINFO_TAG).asScala.toVector.map(_.toString.split(vcfCodes.delims(1)).toVector.map(x => {internalUtils.TXUtil.getPvarInfoFromString(x)}));
    
    val isRepetitive = locusIsRepetitive(v.getContig(),v.getStart());
    val isConserved  = locusIsConserved(v.getContig(),v.getStart());
    val isHotspot    = locusIsHotspot(v.getContig(),v.getStart());
    
    if(altAlleles.length > 1){
      error("FATAL ERROR: Cannot deal with multiallelic variants! Split variants to single-allelic!");
    }
    
    vb = vb.attribute(vcfCodes.assess_IsRepetitive , if(isRepetitive) "1" else "0");
    vb = vb.attribute(vcfCodes.assess_IsConserved ,  if(isConserved)  "1" else "0");
    vb = vb.attribute(vcfCodes.assess_IsHotspot ,    if(isHotspot)    "1" else "0");
    //vb = vb.attribute(vcfCodes.assess_domain ,       if(isHotspot)    "1" else "0");

    val (alt,altIdx) = altAlleles.head;
    
    //val out : Vector[ACMGCritSet] = altAlleles.map{ case (alt,altIdx) => {
      val crits = new ACMGCritSet();
      val vTypes = vTypesList(altIdx).map(_.split("_"));
      val vMutP = vMutPList(altIdx);
      val vMutC = vMutCList(altIdx);
      val vInfo = vMutInfoList(altIdx);
      
      val (pm2,ba1) = checkDbAlleFreq(v,altIdx);
      crits.addCrit(pm2);
      crits.addCrit(ba1);
      crits.addCrit(checkLOF(vTypes,txList,vMutP,txIsLofSensitive));
      
      var bp7 = ACMGCrit("BP",7,false,Seq[String]());
      if( vTypes.forall(vt => {vt(1) != "LLOF" && vt(1) != "PLOF" && vt(1) != "NONSYNON"}) ){
        if(locusIsConserved(chrom,pos)) bp7 = ACMGCrit("BP",7,true, Seq[String]());
        //else                              bp7 = ACMGCrit("BP",7,false,Seq[String]());
      }
      
      var pm4 = ACMGCrit("PM",4,false,Seq[String]());
      val pm4List = vTypes.filter(_(1) == "NONSYNON").filter(vt => vt(2) == "STOP-LOSS" || vt.last == "insAA" || vt.last == "delAA" || vt.last == "indelAA")
      if( pm4List.length > 0 ){
        if(! isRepetitive) pm4 = ACMGCrit("PM",4,true, Seq[String]());
      }
      val combo = txList.zip(vInfo).zip(vMutC).zipWithIndex.map{case (((tx,info),c),i) => (tx,info,c,i)}
      
      var ps1 = ACMGCrit("PS",1,false,Seq[String]());
      var pm5 = ACMGCrit("PM",5,false,Seq[String]());
      var bpa3 = ACMGCrit("BPa",3,false,Seq[String]());
      
      combo.foreach{case (tx,info,c,i) => {
        val cvExactMatch = clinVarVariants(tx).filter{case (cvc,cvinfo) => {cvinfo.start == info.start && cvinfo.subType == info.subType && cvinfo.pvar == info.pvar}};
        if(cvExactMatch.size > 0){
          ps1 = ps1.merge(ACMGCrit("PS",1,true,Seq[String](tx + ":" + info.pvar)));
        }
        if(info.subType == "swapAA"){
          if(cvExactMatch.size == 0){
            val partialMatch = clinVarVariants(tx).filter{case (cvc,cvinfo) => {cvinfo.subType == "swapAA" && cvinfo.start == info.start && cvinfo.altAA != info.altAA}};
            if(partialMatch.size > 0){
              pm5 = pm5.merge(ACMGCrit("PM",5,true,Seq[String](tx+":"+info.pvar+"Vs"+partialMatch.head._2.altAA)));
            }
          }
        }
        if( info.subType == "delAA" || info.subType == "insAA" || info.subType == "multSwapAA" || info.subType == "indelAA" ){
          if(isRepetitive) bpa3.merge(ACMGCrit("BP",3,true,Seq[String](tx+":"+info.pvar)));
        }
      }}

      crits.addCrit(ps1);
      crits.addCrit(pm5);
      crits.addCrit(bpa3);
      //BP3:
      
      //crits;
    //}}.toVector;
    
    //return (out,vb.make());
      
    return null;
  }
  
  //def checkInFrameIndels
  
  def checkLOF(vTypes : Vector[Array[String]], txList : Vector[String], vMutP : Vector[String], txIsLofSensitive : (String => Boolean) = ((s : String) => true)) : ACMGCrit = {
    vTypes.zip(txList).zip(vMutP).map{case ((vtype,tx),aa) => {
      val VTP = variantTypeIsPoLOF(vtype);
      val NS  = txIsLofSensitive(tx)
      if(NS && VTP){
        (true,Seq(tx+":"+vtype+":"+aa+":LS"));
      } else if(VTP){
        (false,Seq(tx+":"+vtype+":"+aa+":NLS"));
      } else {
        (false,Seq[String]());
      }
    }}.foldLeft( ACMGCrit("PVS",1,false,Seq[String]()) ){case (soFar,(isFlagged,opStr)) => {
      ACMGCrit("PVS",1,isFlagged,opStr).merge(soFar);
    }}
  }
  
  def variantTypeIsPoLOF(vtype : Array[String]) : Boolean = {
    vtype(1) == "LLOF" || vtype(1) == "PLOF";
  }
  
  
  def getAttributeDoubleListOption(v : VariantContext, tag : String, ct : Int, defaultVal : Double = -1.0) : Vector[Double] = {
    if(v.hasAttribute(tag)) v.getAttributeAsList(tag).asScala.toVector.map(x => if(x.toString == ".") defaultVal 
                                                                        else string2double(x.toString)) 
    else repToSeq(defaultVal,ct).toVector;
  }
  def checkDatabaseAlleleFreq(af : Vector[Double], altIdx : Int, tag : String, soFar : ACMGCrit, thresh : Double = 0.05) : (Boolean,ACMGCrit) = {
    //val attrAF = getAttributeDoubleListOption(v,tag,0);
        if(af(altIdx) > thresh){
          return (true, ACMGCrit("BA",1,true,Vector(tag+"="+af(altIdx))).merge(soFar))
        } else {
          return (true, ACMGCrit("BA",1,false,Vector(tag+"="+af(altIdx))).merge(soFar))
        }
  }
      
  def checkDbAlleFreq(v : VariantContext, altIdx : Int, thresh : Double = 0.05) : (ACMGCrit,ACMGCrit) = {
    val TKG = getAttributeDoubleListOption(v,"1KG_AF",0);
    val ESP = getAttributeDoubleListOption(v,"ESP_EA_AF",0);
    val EXAC = getAttributeDoubleListOption(v,"dbNSFP_ExAC_AF",0);
    
    //var out : Option[ACMGCrit] = None;
    
    val (tkgFlag,step0)  = checkDatabaseAlleleFreq(TKG,altIdx,"1KG_AF",ACMGCrit("BA",1,false,Seq[String]()),thresh=thresh);
    val (espFlag,step1)  = checkDatabaseAlleleFreq(ESP,altIdx,"ESP_EA_AF",step0,thresh=thresh);
    val (exacFlag,step2) = checkDatabaseAlleleFreq(EXAC,altIdx,"dbNSFP_ExAC_AF",step1,thresh=thresh);
    
    val PM1 = if(TKG(altIdx) <= 0 && ESP(altIdx) <= 0 && EXAC(altIdx) <= 0){
      ACMGCrit("PM",1,true,Vector())
    } else {
      ACMGCrit("PM",1,false,Vector())
    }
    return (PM1,step2);
  }
  
  case class ProteinVariant(tx : String, varType : String, start : Int, end : Int, ref : String, alt : String){
    
  }
  /*case class ProteinVariantDEL(tx : String, pos : Int, end:Int) extends ProteinVariant(tx,pos) {
    def isSevere : Boolean = false;
  }
  case class ProteinVariantMIS(tx : String, pos : Int, alt : String) extends ProteinVariant(tx,pos) {
    def isSevere : Boolean = false;
  }
  case class ProteinVariantINS(tx : String, pos : Int, alt : String) extends ProteinVariant(tx,pos) {
    def isSevere : Boolean = false;
  }
  case class ProteinVariantFS(tx : String, pos : Int, alt :String) extends ProteinVariant(tx,pos){
    def isSevere : Boolean = true;
  }
  case class ProteinVariantDELINS(tx : String, pos : Int, end : Int, alt : String) extends ProteinVariant(tx,pos) {
    def isSevere : Boolean = false;
  }
  case class ProteinVariantSPLICE(tx : String, pos : Int, end : Int, alt : String) extends ProteinVariant(tx,pos) {
    def isSevere : Boolean = true;
  }
  case class ProteinVariantSTOPLOSS(tx : String, pos : Int) extends ProteinVariant(tx,pos) {
    def isSevere : Boolean = false;
  }
  case class ProteinVariantSTARTLOSS(tx : String, pos : Int) extends ProteinVariant(tx,pos) {
    def isSevere : Boolean = true;
  }
  case class ProteinVariantNONS(tx : String, pos : Int) extends ProteinVariant(tx,pos) {
    def isSevere : Boolean = true;
  }*/
  
  
  /*
   * Bottom-level output commands:
   */
  
  case class ACMGCrit(x : String, y : Int, flag : Boolean, anno : Seq[String], geneWiseFlag : Option[Set[String]] = None) {
    def checkValid : Boolean = {
      return true;
    }
    def merge(c : ACMGCrit) : ACMGCrit = {
      if(c.x != x && c.y != y) error("Error! Attempt to merge unequal ACMGCrit!");
      val ngwf = geneWiseFlag match {
        case Some(gwf) => {
          c.geneWiseFlag match {
            case Some(gwf2) => gwf ++ gwf2;
            case None => gwf;
          }
        }
        case None => {
          c.geneWiseFlag match {
            case Some(gwf) => gwf;
            case None => None;
          }
        }
      }
      return ACMGCrit(x,y,flag || c.flag, c.anno ++ this.anno);
    }
    def addGeneWise(gwf : Set[String]) : ACMGCrit = {
      return ACMGCrit(x=x,y=y,flag=flag,anno=anno,geneWiseFlag = Some(gwf));
    }
  }
  
  implicit object ACMGCritOrdering extends Ordering[ACMGCrit] {
    val ord = Vector("BP","BS","BA","PP","PM","PS","PVS").reverse;
    def compare(x : ACMGCrit, y : ACMGCrit) : Int = {
      val xx = ord.indexOf(x.x);
      val yx = ord.indexOf(y.x);
      if(xx == yx){
        return implicitly[Ordering[Int]].compare(xx,yx);
      } else {
        return implicitly[Ordering[Int]].compare(x.y,y.y);
      }
    }
  }
  
  class ACMGCritSet() {
    var critSet : Set[ACMGCrit] = Set[ACMGCrit]();
    def addCrit(c : ACMGCrit){
      critSet = critSet + c;
    }
    def addCrit(c : Option[ACMGCrit]){
      c match {
        case Some(c) => {
          addCrit(c);
        }
        case None => {
          //do nothing.
        }
      }
    }
    def getCritSet : Seq[ACMGCrit] = critSet.toVector.sorted;
  }

  def getPathogenicityFromCts(numPVS : Int, numPS : Int, numPM : Int,numPP : Int, numBA : Int, numBS : Int, numBP : Int, annotated : Boolean = false) : String = {
    val isPathogenic = (numPVS >= 1 && (
                           (numPS >= 1) || 
                           (numPM >= 2) ||
                           (numPM + numPP >= 2) ||
                           (numPP >= 2)
                       )) || 
                       ( numPS >= 2 ) ||
                       ( numPS >= 1 && (
                           (numPM >= 3) ||
                           (numPM >= 2 && numPP >= 2) ||
                           (numPM >= 1 && numPP >= 4)
                       ));
    
    val isLikelyPatho = (numPVS >= 1 && numPM >= 1) ||
                        (numPS  >= 1 && numPM >= 1) ||
                        (numPS  >= 1 && numPP >= 2) || //CHECK ME???
                        (numPM  >= 3) ||
                        (numPM  >= 2 && numPP >= 2) ||
                        (numPM  >= 1 && numPP >= 4);
    
    val isLikelyBenign = (numBS >= 1 && numBP >= 1) ||
                         (numBP >= 2);
    val isBenign  = (numBA >= 1) ||
                    (numBS >= 2);
    
    val rating = if(isPathogenic){
      if(isBenign){
        "VUS";
      } else {
        "PATHO";
      }
    } else if(isBenign){
      "B";
    } else if(isLikelyPatho){
      if(isLikelyBenign){
        "VUS";
      } else {
        "LPATH";
      }
    } else  if(isLikelyBenign){
      "LB";
    } else {
      "VUS"
    }
    
    return rating + (if(annotated){"["+Seq(numPVS,numPS,numPM,numPP, numBA,numBS,numBP).mkString(",")+"]"} else {""})
  }
  
  def getGenewiseACMGPathogenicityRating(geneList : Seq[String], inputCriteria : Seq[ACMGCrit]) : Seq[(String,String)] = {
    geneList.map{ g => {
      val criteria = inputCriteria.filter( c => {
        c.geneWiseFlag match {
          case Some(gwf) => gwf.contains(g);
          case None => c.flag;
        }
      });
      val numPVS = criteria.count{(c) => c.x == "PVS"}
      val numPS  = criteria.count{(c) => c.x == "PS"}
      val numPM  = criteria.count{(c) => c.x == "PM"}
      val numPP  = criteria.count{(c) => c.x == "PP"}
      val numBA  = criteria.count{(c) => c.x == "BA"}
      val numBS  = criteria.count{(c) => c.x == "BS"}
      val numBP  = criteria.count{(c) => c.x == "BP"}
      (g,getPathogenicityFromCts(numPVS =numPVS, numPS =numPS, numPM =numPM,numPP = numPP, numBA =numBA, numBS =numBS, numBP = numBP));
    }}
  }

  
  def getACMGPathogenicityRating(inputCriteria : Seq[ACMGCrit], annotated : Boolean = false) : String = {
    val criteria = inputCriteria.filter(_.flag);
    val numPVS = criteria.count{(c) => c.x == "PVS"}
    val numPS  = criteria.count{(c) => c.x == "PS"}
    val numPM  = criteria.count{(c) => c.x == "PM"}
    val numPP  = criteria.count{(c) => c.x == "PP"}
    val numBA  = criteria.count{(c) => c.x == "BA"}
    val numBS  = criteria.count{(c) => c.x == "BS"}
    val numBP  = criteria.count{(c) => c.x == "BP"}
    
    val isPathogenic = (numPVS >= 1 && (
                           (numPS >= 1) || 
                           (numPM >= 2) ||
                           (numPM + numPP >= 2) ||
                           (numPP >= 2)
                       )) || 
                       ( numPS >= 2 ) ||
                       ( numPS >= 1 && (
                           (numPM >= 3) ||
                           (numPM >= 2 && numPP >= 2) ||
                           (numPM >= 1 && numPP >= 4)
                       ));
    
    val isLikelyPatho = (numPVS >= 1 && numPM >= 1) ||
                        (numPS  >= 1 && numPM >= 1) ||
                        (numPS  >= 1 && numPP >= 2) || //CHECK ME???
                        (numPM  >= 3) ||
                        (numPM  >= 2 && numPP >= 2) ||
                        (numPM  >= 1 && numPP >= 4);
    
    val isLikelyBenign = (numBS >= 1 && numBP >= 1) ||
                         (numBP >= 2);
    val isBenign  = (numBA >= 1) ||
                    (numBS >= 2);
    
    val rating = if(isPathogenic){
      if(isBenign){
        "VUS";
      } else {
        "PATHO";
      }
    } else if(isBenign){
      "B";
    } else if(isLikelyPatho){
      if(isLikelyBenign){
        "VUS";
      } else {
        "LPATH";
      }
    } else  if(isLikelyBenign){
      "LB";
    } else {
      "VUS"
    }
    return rating + (if(annotated){"["+Seq(numPVS,numPS,numPM,numPP, numBA,numBS,numBP).mkString(",")+"]"} else {""})

    /*
    if(isPathogenic){
      if(isBenign){
        return "VUS";
      } else {
        return "PATHO";
      }
    }
    if(isBenign){
      return("B");
    }
    if(isLikelyPatho){
      if(isLikelyBenign){
        return("VUS");
      } else {
        return("LPATH");
      }
    }
    if(isLikelyBenign){
      return("LB");
    }
    return("VUS")*/
  }
  
}









