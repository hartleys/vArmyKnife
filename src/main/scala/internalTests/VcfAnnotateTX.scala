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

//import com.timgroup.iterata.ParIterator.Implicits._;

object VcfAnnotateTX {
   
  class CmdMultiStepPipeline extends CommandLineRunUtil {
     override def priority = 1;
     
     val vcfFilterManualTitle = "VCF Line Filter Expressions"
     val gtFilterManualTitle = "Genotype Filter Expressions"
     
     val vcfFilterManualDesc = ""+
                              ""+
                              ""+
                              ""+
                              ""
     val gtFilterManualDesc = ""+
                              ""+
                              ""+
                              ""+
                              ""

      
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
     val altCommandDocText : Seq[String] = Seq("Secondary Commands:",
                            "In addition to the standard command which parses a VCF or variant table, vArmyKnife includes a few ancillary tools "+
                            "which perform other tasks. ",
                            "These tools can be invoked with the command:",
                            "    varmyknife --CMD commandName [options]",
                            "For more information, use the command:" ,
                            "    varmyknife --help CMD",
                            "For a listing of all secondary commands, use the command: ",
                            "    varmyknife --help secondaryCommands") 
                            
     val altCommandDocMd : Seq[String] = Seq("## Secondary Commands:",
                            "In addition to the standard command which parses a VCF or variant table, vArmyKnife includes a few ancillary tools "+
                            "which perform other tasks. ",
                            "These tools can be invoked with the command:",
                            "    varmyknife --CMD commandName [options]",
                            "For more information see the [secondary command page](secondaryCommands.html), or use the command:" ,
                            "    varmyknife --help CMD",
                            "For a listing of all secondary commands, use the command: ",
                            "    varmyknife --help secondaryCommands") 
     val manualExtras = sVcfFilterLogicParser.getManualString(Some(vcfFilterManualTitle),Some(vcfFilterManualDesc)) +
                        sGenotypeFilterLogicParser.getManualString(Some(gtFilterManualTitle),Some(gtFilterManualDesc)) +
                        altCommandDocText.map{ acm => {
                          wrapLinesWithIndent(acm, internalUtils.commandLineUI.CLUI_CONSOLE_LINE_WIDTH, "        ", false) 
                        }}.mkString("\n");
                        
     val markdownManualExtras = sVcfFilterLogicParser.getMarkdownManualString(Some(vcfFilterManualTitle),Some(vcfFilterManualDesc)) +
                        sGenotypeFilterLogicParser.getMarkdownManualString(Some(gtFilterManualTitle),Some(gtFilterManualDesc))  +
                        altCommandDocMd.map{ acm => {
                          wrapLinesWithIndent(acm, internalUtils.commandLineUI.CLUI_CONSOLE_LINE_WIDTH, "        ", false) 
                        }}.mkString("\n");
     
     /*
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
          aliases = Seq("multiStepPipeline"),
          quickSynopsis = "", 
          synopsis = "", 
          description = "This utility performs a series of transformations on an input VCF file and adds an array of informative tags.",
          argList = 
                    new UnaryArgument( name = "tableInput",
                                         arg = List("--tableInput"), // name of value
                                         argDesc = "todo write desc"+
                                                   "" // description
                                       ).meta(true,"ZZ ALPHA PARAMS, not for general use", 9000) ::
                    new UnaryArgument( name = "tableOutput",
                                         arg = List("--tableOutput"), // name of value
                                         argDesc = "todo write desc"+
                                                   "" // description
                                       ).meta(true,"ZZ ALPHA PARAMS, not for general use", 9000) ::
                    new BinaryOptionArgument[String](
                                         name = "inputSavedTxFile", 
                                         arg = List("--inputSavedTxFile"), 
                                         valueName = "txdata.data.txt.gz",  
                                         argDesc =  "Loads a saved TXdata file in order to add transcript annotation. To generate TX annotation, either this parameter OR the --genomeFA parameter must be set. Using this file will be much faster than regenerating the tx data from the gtf/fasta. "+
                                                    "this TXdata file must be generated using the GenerateTranscriptAnnotation command"
                                        ).meta(false,"Transcript Annotation") ::
                    new BinaryOptionArgument[List[String]](
                                         name = "geneList", 
                                         arg = List("--geneList"), 
                                         valueName = "gene1,gene2,...",  
                                         argDesc =  "ONLY PARTIALLY IMPLEMENTED: A gene list for adding transcript annotation, amino-acid matching, and similar. Genes not on this list will be ignored."
                                        ).meta(true,"Annotation") ::
                    new BinaryOptionArgument[List[String]](
                                         name = "ensGeneList", 
                                         arg = List("--ensGeneList"), 
                                         valueName = "gene1,gene2,...",  
                                         argDesc =  "List of ensemble geneIDs to use for searching an extracting genes from the ANN SnpEff field."
                                        ).meta(true,"Annotation") ::
                    new BinaryOptionArgument[List[String]](
                                         name = "txTypes", 
                                         arg = List("--txTypes"), 
                                         valueName = "protein_coding,...",  
                                         argDesc =  "List of transcript biotypes to include. Only works if biotype info is available in the TX annotation."
                                        ).meta(false,"Transcript Annotation") ::
                                        
                    new BinaryMonoToListArgument[String](
                                         name = "addLocalGcInfo", 
                                         arg = List("--addLocalGcInfo"), 
                                         valueName = "tagPrefix,windowsize1|ws2|...,numDigits",  
                                         argDesc =  "Will add VCF INFO tags that indicate the GC fraction of flanking bases. "+
                                                    "Requires an indexed genome fasta to be set with the --genomeFA option. "+
                                                    "The parameter must be formatted in 3 comma-delimited parts: first the prefix to "+
                                                    "append to the INFO tags, then a bar-delimited list of window sizes, and then the number of digits to include in the output."
                                        ).meta(false,"Annotation") ::
                                        
                    new BinaryMonoToListArgument[String](
                                         name = "calcBurdenCounts", 
                                         arg = List("--calcBurdenCounts"), 
                                         valueName = "....",  
                                         argDesc =  "BETA: not for production use!"
                                        ).meta(true,"Sample Stats") ::
                    new BinaryOptionArgument[String](
                                         name = "burdenCountsFile",
                                         arg = List("--burdenCountsFile"), 
                                         valueName = "table.file.txt",
                                         argDesc = "BETA: not for production use!" // description
                                        ).meta(true,"Sample Stats") ::
                                        
                    new BinaryOptionArgument[String](
                                         name = "gtfFile",
                                         arg = List("--gtfFile"), 
                                         valueName = "gtfFile.gtf.gz",
                                         argDesc = "A gene annotation GTF file. Can be gzipped or in plaintext." // description
                                        ).meta(false,"Transcript Annotation") ::
                    new BinaryOptionArgument[String](
                                         name = "genomeFA", 
                                         arg = List("--genomeFA"), 
                                         valueName = "genome.fa.gz",  
                                         argDesc =  "The genome fasta file. Can be gzipped or in plaintext."
                                        ).meta(false,"Universal Parameters",1) ::
                    new BinaryOptionArgument[String](
                                         name = "summaryFile", 
                                         arg = List("--summaryFile"), 
                                         valueName = "filename.txt",  
                                         argDesc =  "An optional extra output file that contains debugging information."
                                        ).meta(true,"ZZ ALPHA PARAMS, not for general use", 9000) ::
                    new UnaryArgument( name = "addTxSummaryInfo",
                                         arg = List("--addTxSummaryInfo"), // name of value
                                         argDesc = "todo write desc"+
                                                   "" // description
                                       ).meta(true) ::
                    new UnaryArgument( name = "cdsRegionContainsStop",
                                         arg = List("--cdsRegionContainsStop"), // name of value
                                         argDesc = "Use this flag if the input GTF annotation file includes the STOP codon in the CDS region. Depending on the source of the annotation file, some GTF files include the STOP codon, some omit it. The UCSC knowngenes annotation file does NOT include CDS regions."+
                                                   "" // description
                                       ).meta(true,"Transcript Annotation") ::
                                       
                    new UnaryArgument( name = "addSummaryCLNSIG",
                                         arg = List("--addSummaryCLNSIG"), // name of value
                                         argDesc = "Special-purpose flag for use with specialized ClinVar VCFs. NOT FOR GENERAL USE!"+
                                                   "" // description
                                       ).meta(true,"ZZ ALPHA PARAMS, not for general use") ::
                                       
                    new BinaryOptionArgument[String]( name = "addCanonicalTags",
                                         arg = List("--addCanonicalTags"), // name of value
                                         valueName = "knownCanonical.txt",
                                         argDesc = "Supply a list of canonical transcripts, add tags that indicate canonical-transcript-only variant info."+
                                                   ""// description
                                       ).meta(false,"Transcript Annotation") ::
                    new UnaryArgument( name = "splitMultiAllelics",
                                         arg = List("--splitMultiAllelics"), // name of value
                                         argDesc = "If this flag is used, multiallelic variants will be split into multiple separate VCF lines. "+
                                                   "In order to preserve cross-allele genotypes and multiallelic FORMAT fields, the star allele will be "+
                                                   "used to indicate a different alt allele on a different variant line. The star-allele encoding can be "+
                                                   "deactivated using the --splitMultiAllelicsNoStarAllele option instead." // description
                                       ).meta(false,"Preprocessing") ::
                                       
                    new UnaryArgument( name = "splitMultiAllelicsNoStarAlle",
                                         arg = List("--splitMultiAllelicsNoStarAlle"), // name of value
                                         argDesc = "If this flag is used, multiallelic variants will be split into multiple separate VCF lines. "+
                                                   "Two copies of the AD tag will be created, AD and AD_multAlle." // description
                                       ).meta(false,"Preprocessing") ::
                    new UnaryArgument( name = "dropSpanIndels",
                                         arg = List("--dropSpanIndels"), // name of value
                                         argDesc = "Requires splitMultiAllelic. Drops spanning indels, which are marked with an asterisk allele." // description
                                       ).meta(false,"Preprocessing") ::
                    new UnaryArgument( name = "convertROAOtoAD",
                                         arg = List("--convertROAOtoAD"), // name of value
                                         argDesc = "If this flag is used, then the RO/AO FORMAT fields used by certain callers (freebayes) "+
                                                   "will be copied over into a new AD tag." // description
                                       ).meta(false,"Preprocessing") ::
                    new UnaryArgument( name = "splitAlleleGroupCounts",
                                         arg = List("--splitAlleleGroupCounts"), // name of value
                                         argDesc = "..."+
                                                   "" // description
                                       ).meta(true,"Preprocessing") ::
                    new UnaryArgument( name = "geneVariantsOnly",
                                         arg = List("--geneVariantsOnly"), // name of value
                                         argDesc = "If this flag is used, only output variants that fall on or near known genes. This is only functional when gene annotation is supplied."+
                                                   "" // description
                                       ).meta(true,"Transcript Annotation",15) ::
                    new UnaryArgument( name = "nonNullVariantsOnly",
                                         arg = List("--nonNullVariantsOnly"), // name of value
                                         argDesc = "If this flag is used, only write variants that have non-null alt alleles."+
                                                   "" // description
                                       ).meta(false,"Preprocessing",2) ::
                    new UnaryArgument( name = "rmDup",
                                         arg = List("--rmDup"), // name of value
                                         argDesc = "If this flag is used, duplicate lines will be dropped. VCF must be sorted."+
                                                   "" // description
                                       ).meta(false,"Preprocessing") ::
                    new UnaryArgument( name = "alphebetizeHeader",
                                         arg = List("--alphebetizeHeader"), // name of value
                                         argDesc = "Alphebetizes the INFO and FORMAT header lines."
                                       ).meta(false,"Postprocessing",100) ::
                    new BinaryMonoToListArgument[String](
                                         name = "snpSiftAnnotate", 
                                         arg = List("--snpSiftAnnotate"), 
                                         valueName = "tagPrefix,snpSiftParameters",  
                                         argDesc =  "Options for a SnpSift annotate run. Provide all the options for the standard run. "+
                                                    " Do not include the annotate command itself or the input VCF. "+
                                                    "This will call the internal SnpSift annotate methods, not merely run an external instance of SnpSift. "
                                        ).meta(false,"Annotation", 10) ::
                                        
                    new BinaryOptionArgument[String](
                                         name = "snpEffAnnotate", 
                                         arg = List("--snpEffAnnotate"), 
                                         valueName = "tagPrefix,snpEffParameters",  
                                         argDesc =  "Options for a SnpEff ann run. Provide all the options for the standard run. "+
                                                    " Do not include the annotate command itself or the input VCF. "+
                                                    "This will call the internal SnpEff ann methods, not merely run an external instance of SnpEff. "
                                        ).meta(false,"Annotation") ::
                                        
                    new BinaryMonoToListArgument[String](
                                         name = "snpSiftDbnsfp", 
                                         arg = List("--snpSiftDbnsfp"), 
                                         valueName = "...",  
                                         argDesc =  "Options for a SnpSift Dbnsfp run. Provide all the options for the standard run. Do not include the Dbnsfp command itself or the input VCF. "+
                                                    "This will call the internal SnpSift annotate commands, not merely run an external instance of SnpSift. "
                                        ).meta(false,"Annotation") ::
                    new BinaryMonoToListArgument[String](
                                         name = "copyFmtTag", 
                                         arg = List("--copyFmtTag"), 
                                         valueName = "...",  
                                         argDesc =  "For advanced users only. "
                                        ).meta(true,"ZZ ALPHA PARAMS, not for general use") ::
                                        
                                        
                    new BinaryOptionListArgument[String](
                                         name = "addBedTags", 
                                         arg = List("--addBedTags"), 
                                         valueName = "TAGTITLE:bufferLen:filedesc:bedfile.bed,TAGTITLE2:bufferLen:filedesc2:bedfile2.bed.gz,...",  
                                         argDesc =  "List of tags and bed files that define said tags. "+
                                                    "For each tag, the variant will have a tag value of 1"+
                                                    " iff the variant appears on the bed file region, and "+
                                                    "0 otherwise. This should be a comma-delimited list consisting of "+
                                                    "one or more colon-delimited lists. Each element in the comma delimited list "+
                                                    "must have 4 colon-delimited sub-elements: the tag title (ie, the tag that "+
                                                    "will be added to the VCF file, the buffer distance, the tag description (for the VCF header), and the bedfile path. "+
                                                    "Bed files may be gzipped or zipped."
                                        ).meta(false,"Annotation") ::
                                        
                    new BinaryMonoToListArgument[String](
                                         name = "addContextBases", 
                                         arg = List("--addContextBases"), 
                                         valueName = "windowSize[:tagInfix]",  
                                         argDesc =  "Adds fields containing the sequence flanking the variant with the assigned windowsize."
                                        ).meta(false,"Annotation") ::
                                        
                    new BinaryOptionArgument[String](
                                         name = "homopolymerRunStats", 
                                         arg = List("--homopolymerRunStats"), 
                                         valueName = "tagID|bedfile.bed|hrunThreshold",  
                                         argDesc =  "Adds a new tag that indicates when a variant adds to or deletes from a homopolymer run. Requires a homopolymer run bed file."
                                        ).meta(false,"Annotation") ::
                                        
                                        //homopolymerRunStats
                                        
                    new BinaryOptionArgument[String](
                                         name = "txInfoFile", 
                                         arg = List("--txInfoFile"), 
                                         valueName = "txInfoFile.txt",  
                                         argDesc =  "Outputs an optional debugging file."
                                        ).meta(true,"ZZ ALPHA PARAMS, not for general use") ::
                    new BinaryOptionArgument[String](
                                         name = "addVariantPosInfo", 
                                         arg = List("--addVariantPosInfo"), 
                                         valueName = "tagPrefix",  
                                         argDesc =  "Copies the chrom, start, ref, and alt columns into an info field. This "+
                                                    "can be useful for keeping track of variants before and after variant transformations "+
                                                    "such as leftAlignAndTrim or multiallelic splits."
                                        ).meta(false,"Annotation") ::
                    /*new BinaryOptionArgument[String](
                                         name = "outputSavedTxFile", 
                                         arg = List("--outputSavedTxFile"), 
                                         valueName = "txdata.data.txt.gz",  
                                         argDesc =  "Creates a saved TXdata file, for faster loading in future runs. This contains metadata about each transcript in a machine-readable format."
                                        ) ::*/
                    new BinaryOptionArgument[String](
                                         name = "txToGeneFile", 
                                         arg = List("--txToGeneFile"), 
                                         valueName = "txToGene.txt",  
                                         argDesc =  "File containing the mapping of transcript names to gene symbols. This file must have 2 columns: the txID and the geneID. No header line."
                                        ).meta(false,"Transcript Annotation") :: 
                    new UnaryArgument(
                                         name = "noGroupStats", 
                                         arg = List("--noGroupStats"), 
                                         argDesc =  "Do NOT calculate group stats. Normally if you specify sample groups then group stats will automatically be calculated."
                                        ).meta(false,"Sample Stats",12) :: 
                    new BinaryOptionArgument[String](
                                         name = "groupFile", 
                                         arg = List("--groupFile"), 
                                         valueName = "groups.txt",  
                                         argDesc =  "File containing a group decoder. This is a simple 2-column file (no header line). The first column is the sample ID, the 2nd column is the group ID."
                                        ).meta(false,"Sample Stats") :: 
                    new BinaryOptionArgument[String](
                                         name = "superGroupList", 
                                         arg = List("--superGroupList"), 
                                         valueName = "sup1,grpA,grpB,...;sup2,grpC,grpD,...",  
                                         argDesc =  "A list of top-level supergroups. Requires the --groupFile parameter to be set."
                                        ).meta(false,"Sample Stats") :: 
                                        
                                        //SAddSampCountWithMultVector(tagID : String, gtTag : String, desc : String, vectorFile : String)
                    new BinaryMonoToListArgument[String](
                                         name = "addSampCountWithMultVector", 
                                         arg = List("--addSampCountWithMultVector"), 
                                         valueName = "tagID|desc|sampMultFile.txt",  
                                         argDesc =  "Beta feature: not for general use."
                                        ).meta(false,"Sample Stats") ::
                                        
                    new BinaryOptionArgument[List[String]](
                                         name = "chromList", 
                                         arg = List("--chromList"), 
                                         valueName = "chr1,chr2,...",  
                                         argDesc =  "List of chromosomes. If supplied, then all analysis will be restricted to these chromosomes. All other chromosomes will be ignored. "+
                                                    "For a VCF that contains only one chromosome this option will improve runtime, since the utility will not have to load and process "+
                                                    "annotation data for the other chromosomes."
                                        ).meta(false,"Preprocessing") ::
                    new UnaryArgument( name = "infileList",
                                         arg = List("--infileList"), // name of value
                                         argDesc = "If this option is set, then the input file is a text file containing a list of input VCF files (one per line), rather than a simple path to a single VCF file. "+
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
                                        
                                        
                    new UnaryArgument( name = "splitOutputByChrom",
                                         arg = List("--splitOutputByChrom"), // name of value
                                         argDesc = "If this option is set, the output will be split up into parts by chromosome. "+
                                                   "NOTE: The outfile parameter must be either a file prefix (rather than a full filename), "+
                                                   "or must be a file prefix and file suffix separated by a bar character. In other worse, rather than being " +
                                                   "'outputFile.vcf.gz', it should be either just 'outputFile' or 'outputFile.|.vcf.gz'. "+
                                                   ""// description
                                       ).meta(false,"Output Parameters", 110) ::
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
                                       ).meta(false,"Preprocessing") ::
                                       
                                       //makeFirstBaseMatch
                    new UnaryArgument(
                                         name = "makeFirstBaseMatch", 
                                         arg = List("--makeFirstBaseMatch"),
                                         argDesc =  "Reformats multibase indels so that the first bases match. Without this option, raw output from certain callers will not be properly trimmed by GATK leftAlignAndTrim. Requires genomeFA to be set."
                                        ).meta(true,"Preprocessing")  :: 
                                       
                                       
                    new UnaryArgument(
                                         name = "leftAlignAndTrim", 
                                         arg = List("--leftAlignAndTrim"),
                                         argDesc =  "Left align and trim the primary input VCF using a modified "+
                                                    "and ported version of the GATK v3.8-2 LeftAlignAndTrim walker."
                                        ).meta(false,"Preprocessing")  :: 
                                       
                    new BinaryOptionArgument[Int]( name = "leftAlignAndTrimWindow",
                                         arg = List("--leftAlignAndTrimWindow"), // name of value\
                                         valueName = "N",
                                         argDesc = "Set the window size used for left align and trim. Indels larger than this will not be left aligned."+
                                                   "" +
                                                   ""+
                                                   ""// description
                                       ).meta(false,"Preprocessing") ::

                   /* new UnaryArgument( name = "leftAlignAndTrimSecondarys",
                                         arg = List("--leftAlignAndTrimSecondarys"), // name of value
                                         argDesc = "Left align and trim any secondary input VCFs using a modified and ported version of the GATK v1.8-2 LeftAlignAndTrim walker."+
                                                   "" +
                                                   ""+
                                                   ""// description
                                       ).meta(false,"Preprocessing") ::*/
                                       
                    new UnaryArgument( name = "fixDotAltIndels",
                                         arg = List("--fixDotAltIndels"), // name of value
                                         argDesc = "Some callers return variant lines that use a dot or dashin the alt column, especially if the VCFs were converted over from ANNOVAR files. "+
                                                   "technically per the VCF spec this should be interpreted as the absence of any "+
                                                   "variant alleles (an allele list of length 0). But some tools seem to sometimes use "+
                                                   "this encoding when they actually mean to indicate an indel. If you use this parameter, then dot-alt indels will be converted to proper form before any other "+
                                                   "processing. Note: the genomeFA parameter is required in order to use this option, as we need to be able to find the reference sequence for the previous base."
                                       ).meta(false,"Preprocessing") ::

                    new BinaryOptionArgument[List[String]]( name = "unPhaseAndSortGenotypes",
                                         arg = List("--unPhaseAndSortGenotypes"), // name of value
                                         valueName = "GT,GT_PREFILT,etc",
                                         argDesc = "Replace phased genotypes with unphased and reorder genotypes so that heterozygote alleles are always written in ascending order."+
                                                   "" // description
                                       ).meta(false,"Preprocessing") ::
                                       
                                       
                                       
                    new BinaryOptionArgument[String](
                                         name = "genoFilter", 
                                         arg = List("--genoFilter"), 
                                         valueName = "",  
                                         argDesc =  "A genotype filter expression. See the help section on genotype filtering expressions, below."+
                                                    ""+
                                                    ""+
                                                    ""+
                                                    ""
                                        ).meta(false,"Filtering, Genotype-Level",35) ::
                    new BinaryOptionArgument[String](
                                         name = "filterTag", 
                                         arg = List("--filterTag"), 
                                         valueName = "",  
                                         argDesc =  "This sets the field ID for a new FORMAT field that "+
                                                    "will be 0 when the --genoFilter PASSES and 1 otherwise."+
                                                    ""+
                                                    ""+
                                                    ""//,
                                         //defaultValue = Some(OPTION_TAGPREFIX+"FILTER_GT")
                                        ).meta(false,"Filtering, Genotype-Level") ::
                                        
                    new BinaryArgument[String](
                                         name = "filterInputGtTag", 
                                         arg = List("--filterInputGtTag"), 
                                         valueName = "",  
                                         argDesc =  "Requires that --genoFilter be set. This parameter indicates which "+
                                                    "FORMAT (genotype) field should be used as the raw genotype for the purposes of "+
                                                    "genotype filtering via --genoFilter. "+
                                                    "By default the genotype filter will take the GT field and "+
                                                    "copy it to GT_PREFILT and then replace GT with the post-filtering version."+
                                                    ""+
                                                    "",
                                         defaultValue = Some("GT")
                                        ).meta(false,"Filtering, Genotype-Level") ::
                    new BinaryArgument[String](
                                         name = "filterOutputGtTag", 
                                         arg = List("--filterOutputGtTag"), 
                                         valueName = "",  
                                         argDesc =  "Requires that --genoFilter be set. This parameter indicates which FORMAT field "+
                                                    "should be used to store the post-filtered genotypes. "+
                                                    "Note that genotypes that fail the --genoFilter filters will be set to missing. "+
                                                    "By default the GT field will be overwritten with post-filtered genotypes."+
                                                    "",
                                         defaultValue = Some("GT")
                                        ).meta(false,"Filtering, Genotype-Level") ::
                    new BinaryOptionArgument[String](
                                         name = "unfilteredGtTag", 
                                         arg = List("--unfilteredGtTag"), 
                                         valueName = "",  
                                         argDesc =  "This parameter indicates where the raw genotypes should "+
                                                    "be copied before filtering the genotypes with the --genoFilter expression."+
                                                    ""+
                                                    ""+
                                                    ""//,
                                        // defaultValue = Some("GT_PREFILT")
                                        ).meta(false,"Filtering, Genotype-Level") ::

                                        
                                        
                    new UnaryArgument( name = "runEnsembleMerger",
                                         arg = List("--runEnsembleMerger"), // name of value
                                         argDesc = "If this parameter is raised, then the input VCF should instead be formatted as a "+
                                                   "comma delimited list of N VCF files. Each of the N files will be run through an initial subset of the final VCF " +
                                                   "walkers including any of the following that are indicated by the other options: addVariantIdx,nonVariantFilter,chromosome converter,inputTag filters,addVariantPosInfo,splitMultiAllelics,leftAlignAndTrim, and convertROtoAD "+
                                                   "The variant data output stream from these walkers will be merged and final GT, AD, and GQ fields will be added if the requisite information is available. Final genotypes will be assigned by plurality rule if any genotype has a simple plurality of all nonmissing caller calls, "+
                                                   "and if no genotype has a plurality then the genotype will be chosen from the highest priority caller, chosen in the order they are named in the "
                                       ).meta(false,"Merge Multicaller VCFs",50) ::
                                       
                    new BinaryArgument[String](name = "ensembleGenotypeDecision",
                                           arg = List("--ensembleGenotypeDecision"),  
                                           valueName = "priority", 
                                           argDesc = "The merge rule for calculating ensemble-merged GT and AD tags. Valid options are priority, prioritySkipMissing, and majority_priorityOnTies. Default is simple priority.",
                                           defaultValue = Some("priority")
                                           ).meta(false,"Merge Multicaller VCFs",50) ::
                                       //"first","firstSkipMissing","majority_firstOnTies","majority_missOnTies",
                                       //"priority","prioritySkipMissing","majority_priorityOnTies"
                                       
                    new BinaryOptionArgument[List[String]](
                                         name = "singleCallerVcfsOld", 
                                         arg = List("--singleCallerVcfs"), 
                                         valueName = "haplotypeCaller.vcf.gz,unifiedGenotype.vcf.gz,etc",  
                                         argDesc =  "This parameter allows the foldover of single-caller metadata to the ensemble-caller VCF. "+
                                                    "It should be a comma-delimited list of single-caller VCF files."
                                        ).meta(true,"DEPRECATED",9990) ::

                    new BinaryOptionArgument[List[String]](
                                         name = "singleCallerVcfsNew", 
                                         arg = List("--singleCallerVcfsNew"), 
                                         valueName = "haplotypeCaller.vcf.gz,unifiedGenotype.vcf.gz,etc",  
                                         argDesc =  "This parameter allows the foldover of single-caller metadata to the ensemble-caller VCF. "+
                                                    "It should be a comma-delimited list of single-caller VCF files."
                                        ).meta(true,"DEPRECATED") ::

                   new BinaryOptionArgument[List[String]](
                                         name = "singleCallerVcfNames", 
                                         arg = List("--singleCallerVcfNames"), 
                                         valueName = "",  
                                         argDesc =  "This parameter should be a comma delimited list of names, with the same length as --singleCallerVcfs. "+
                                                    "These names will be used in the folder-over VCF tags."
                                        ).meta(false,"Merge Multicaller VCFs") ::
                   new BinaryOptionArgument[List[String]](
                                         name = "singleCallerPriority", 
                                         arg = List("--singleCallerPriority"), 
                                         valueName = "",  
                                         argDesc =  "This parameter should be a comma delimited list composed of a subset of the VCF names "+
                                                    "from the --singleCallerVcfNames parameter. Single callers can be left off this list and their calls will not be "+
                                                    "used to determine genotype, allele depth, and genotype quality information. Genotype calls are assigned using the most common call across "+
                                                    "all callers. Ties are broken using the priority list above, in order of highest priority (most trusted) to lowest priority (least trusted)."
                                        ).meta(false,"Merge Multicaller VCFs") ::
                    new BinaryOptionArgument[List[String]](
                                         name = "singleCallerMaster", 
                                         arg = List("--singleCallerMaster"), 
                                         valueName = "",  
                                         argDesc =  "This optional parameter should be a list of names from --singleCallerVcfNames. INFO field data from the "+
                                                    "VCF file will be folded over along with the genotype data."
                                        ).meta(true,"DEPRECATED") ::

                                        
                    new BinaryOptionArgument[Int](
                                         name = "numLinesRead", 
                                         arg = List("--numLinesRead"), 
                                         valueName = "N",  
                                         argDesc =  "If this parameter is set, then the utility will stop after reading in N variant lines. Intended for testing purposes."
                                        ).meta(false,"Input Parameters") ::
                    new UnaryArgument( name = "testRun",
                                         arg = List("--testRun","--test","-t"), // name of value
                                         argDesc = "Only read the first 1000 lines. Equivalent to --numLinesRead 1000. Intended for testing purposes."// description
                                       ).meta(false,"Input Parameters") ::
                                        
                    new BinaryOptionListArgument[String](
                                         name = "addInfoVcfs", 
                                         arg = List("--addInfoVcfs"), 
                                         valueName = "vcfTitle:vcfFilename:List|of|tags",
                                         argDesc =  "A comma delimited list with each element containing 3 colon-delimited parts consisting of the "+
                                                    "title, filename, and the desired tags for a secondary VCF file. The indicated tags from each VCF "+
                                                    "file will be copied over."
                                        ).meta(false,"Annotation") :: //
                                        
                    new UnaryArgument( name = "convertToStandardVcf",
                                         arg = List("--convertToStandardVcf"), // name of value
                                         argDesc = "Final output file will be a standard VCF that does not use the asterisk to indicate multiallelic variants. "+
                                                   "Some utilities do not implement the full VCF standard and may have trouble with asterisk alleles."+
                                                   "" +
                                                   ""+
                                                   ""// description
                                       ).meta(false,"Postprocessing") ::
                    new BinaryOptionArgument[String](
                                         name = "thirdAlleleChar", 
                                         arg = List("--thirdAlleleChar"),
                                         valueName = "tag",
                                         argDesc =  "Some users may not prefer the use of the star allele to indicate an other allele in multiallelic variants. "+
                                                    "If this tag is raised, then the star allele will be removed from multiallelics. In addition, in the "+
                                                    "GT field all other-allele "+
                                                    "For multiallelic-split variants, this defines the character used for the 'other' allele. "+
                                                    "If this is used, the original version will be copied as a backup."
                                        ).meta(false,"Annotation") :: 
                    new UnaryArgument( name = "dropGenotypeData",
                                         arg = List("--dropGenotypeData"), // name of value
                                         argDesc = "If this flag is included, then ALL sample-level columns will be stripped from the output VCF. "+
                                                   "This greatly reduces the file size, and can be useful for making portable variant set VCFs." +
                                                   ""+
                                                   ""// description
                                       ).meta(false,"Postprocessing") ::
                    new UnaryArgument( name = "addDummyGenotypeColumn",
                                         arg = List("--addDummyGenotypeColumn"), // name of value
                                         argDesc = "If this flag is included, then the genotype data will be stripped and replaced with a dummy column with 1 sample and 1 het genotype on every line." +
                                                   ""+
                                                   ""// description
                                       ).meta(false,"Postprocessing") ::
                    new UnaryArgument( name = "dropSymbolicAlleleLines",
                                         arg = List("--dropSymbolicAlleleLines"), // name of value
                                         argDesc = "Drop all variant lines that contain symbolic alleles. If this flag is used with splitMultiAllelic, then the non-symbolic alleles of mixed-type variants will be preserved."
                                       ).meta(false,"Preprocessing") ::
                    new UnaryArgument( name = "simpleCompileDuplicateLines", //RemoveDuplicateLinesWalker
                                         arg = List("--simpleCompileDuplicateLines"), // name of value
                                         argDesc = "."
                                       ).meta(true,"ZZ ALPHA PARAMS, not for general use") ::
                    new BinaryOptionArgument[String](
                                         name = "addVariantIdx", 
                                         arg = List("--addVariantIdx"),
                                         valueName = "tag",
                                         argDesc =  "."
                                        ).meta(false,"Annotation") :: 
                    new BinaryOptionArgument[String](
                                         name = "keepVariantsExpression", 
                                         arg = List("--keepVariantsExpression"),
                                         valueName = "vcfLineFilterExpression",
                                         argDesc =  "If this parameter is set, then VCF lines will be dropped if and only if they FAIL the provided vcf "+
                                                    "line filter expression. "+
                                                    "See the section on VCF line filtering below. "
                                        ).meta(false,"Filtering, Variant-Level",40) :: 
                    new BinaryOptionArgument[String](
                                         name = "keepVariantsExpressionPrefilter", 
                                         arg = List("--keepVariantsExpressionPrefilter"),
                                         valueName = "vcfLineFilterExpression",
                                         argDesc =  "If this parameter is set, then VCF lines will be parsed in if and only if they pass the given "+
                                                    "line filter expression. Note that this will be performed BEFORE other processing, so all referenced tags "+
                                                    "must be in the input VCF."+
                                                    "See the section on VCF line filtering below. "
                                        ).meta(false,"Filtering, Variant-Level",40) :: 
                    new BinaryOptionArgument[String](
                                         name = "dropVariantsExpression", 
                                         arg = List("--dropVariantsExpression"),
                                         valueName = "vcfLineFilterExpression",
                                         argDesc =  "OPTION REMOVED, DO NOT USE."
                                        ).meta(true,"DEPRECATED") :: 
                                        

                                        
                    new BinaryMonoToListArgument[String](
                                         name = "tagVariantsExpression", 
                                         arg = List("--tagVariantsExpression"),
                                         valueName = "newTagID|desc|variantExpression",
                                         argDesc =  "If this parameter is set, then additional tags will be generated based on the given expression(s). "+
                                                    "The list of expressions must be comma delimited. Each element in the comma-delimited list must begin with "+
                                                    "the tag ID, then a bar-symbol, followed by the tag description, then a bar-symbol, and finally with the expression. "+
                                                    "The expressions are always booleans, and follow the same rules for VCF line filtering. See the section on VCF line filtering, below."
                                        ).meta(false,"Annotation") :: 
                    new BinaryOptionArgument[String](
                                         name = "duplicatesTag", 
                                         arg = List("--duplicatesTag"),
                                         valueName = "duplicateTagPrefix",
                                         argDesc =  "If this parameter is set, duplicates will be detected and two new tags will be added: "+
                                                    "duplicateTagPrefix_CT and duplicateTagPrefix_IDX. The CT will indicate how many duplicates were found "+
                                                    "matching the current variant, and the IDX will number each duplicate with a unique identifier, counting from 0. All nonduplicates will "+
                                                    "be marked with CT=1 and IDX=0."
                                        ).meta(false,"Annotation") :: 
                                        
                                        //duplicateTag
                                        

                    new UnaryArgument( name = "dropVariantsWithNs", //RemoveDuplicateLinesWalker
                                         arg = List("--dropVariantsWithNs"), // name of value
                                         argDesc = "Drop any variants that contain Ns in the ref or alt allele."
                                       ).meta(false,"Preprocessing") ::
                                    
                    new BinaryMonoToListArgument[String](
                                         name = "tagVariantsGtCountExpression", 
                                         arg = List("--tagVariantsGtCountExpression"),
                                         valueName = "newTagID|desc|genotypeExpression[|type]",
                                         argDesc =  "If this parameter is set, then additional tags will be generated based on the given GENOTYPE expression(s). "+
                                                    "Each element in the comma-delimited list must begin with "+
                                                    "the tag ID, then a bar-symbol, followed by the tag description, then a bar-symbol, and finally with the genotype expression. "+
                                                    "The expressions are always booleans, and follow the same rules for VCF genotype-level filtering. See the section on VCF genotype filtering, below. "+
                                                    "Optionally, a fourth element in the list can define the type which can be either FRAC, PCT, or CT, which defines the output as either a fraction, a percentage, or a count."
                                        ).meta(false,"Annotation") :: 
                                        
                    new BinaryMonoToListArgument[String](
                                         name = "tagVariantsFunction", 
                                         arg = List("--tagVariantsFunction"),
                                         valueName = "newTagID|desc|funcName|param1,param2,...",
                                         argDesc =  ""
                                        ).meta(false,"Annotation") :: 

                    new BinaryMonoToListArgument[String](
                                         name = "mergeBooleanTags", 
                                         arg = List("--mergeBooleanTags"),
                                         valueName = "tag1,tag2,...",
                                         argDesc =  "TODO: write desc"
                                         ).meta(true,"Annotation") :: 
                    new BinaryMonoToListArgument[String](
                                         name = "addRatioTag", 
                                         arg = List("--addRatioTag"),
                                         valueName = "ratioField:numeratorField:denominatorField",
                                         argDesc =  "If this parameter is used, a ratio will be calculated between the numerator field and the denominator field."
                                        ).meta(true,"ZZ ALPHA PARAMS, not for general use") :: 
                    new BinaryOptionListArgument[String](
                                         name = "variantStatExpression", 
                                         arg = List("--variantStatExpression"),
                                         valueName = "vcfLineFilterExpression",
                                         argDesc =  ""+
                                                    ""+
                                                    ""+
                                                    ""
                                        ).meta(true,"ZZ ALPHA PARAMS, not for general use") :: 
                    new BinaryOptionArgument[List[String]](
                                         name = "convertGtToMatrix", 
                                         arg = List("--convertGtToMatrix"),
                                         valueName = "GT,GTFILT,etc.",
                                         argDesc =  ""+
                                                    ""
                                        ).meta(true,"ZZ ALPHA PARAMS, not for general use")  :: 
                    new UnaryArgument(
                                         name = "convertMatrixToGt", 
                                         arg = List("--convertMatrixToGt"),
                                         argDesc =  ""+
                                                    ""
                                        ).meta(true,"ZZ ALPHA PARAMS, not for general use")  :: 
                                        
                                        
                    new BinaryOptionArgument[String](
                                         name = "calcRunInfix", 
                                         arg = List("--calcRunInfix"),
                                         valueName = "subsetA",
                                         argDesc =  ". "+
                                                    ""
                                        ).meta(true) :: 
                    new BinaryOptionArgument[String](
                                         name = "addSampTag", 
                                         arg = List("--addSampTag"),
                                         valueName = "N",
                                         argDesc =  "If this parameter is set, then a tags will be added with sample IDs that possess alt genotypes. "+
                                                    "Samples will only be printed if the number of samples that possess a given genotype are less than N. "+
                                                    ""
                                        ).meta(false,"Sample Stats") :: 
                    new BinaryOptionArgument[String](
                                         name = "addDepthStats", 
                                         arg = List("--addDepthStats"),
                                         valueName = "AD,.,.,Stat",
                                         argDesc =  ""+
                                                    ""+
                                                    ""
                                        ).meta(true,"Sample Stats") :: 
                    new BinaryMonoToListArgument[String](
                                         name = "addWiggleAnnotation", 
                                         arg = List("--addWiggleAnnotation"),
                                         valueName = "tagID|desc|wigfile.wig",
                                         argDesc =  "Supply a wiggle (.wig) file. The value in the wiggle file at position POS will be added as an INFO tag."+
                                                    ""+
                                                    ""
                                        ).meta(true,"Annotation") :: 
                    new BinaryOptionArgument[List[String]](
                                         name = "inputKeepSamples", 
                                         arg = List("--inputKeepSamples"), 
                                         valueName = "samp1,samp2,...",  
                                         argDesc =  "If this parameter is set, all samples other than the listed samples will be removed PRIOR to processing."
                                        ).meta(false,"Preprocessing") ::
                    new BinaryOptionArgument[List[String]](
                                         name = "inputKeepInfoTags", 
                                         arg = List("--inputKeepInfoTags"), 
                                         valueName = "tag1,tag2,...",  
                                         argDesc =  "List of tags to keep from the input file before processing. All other tags will be dropped "+
                                                    "before processing. This can be useful for updating a file with a new version or annotation, as it "+
                                                    "can be used to ensure a clean input."
                                        ).meta(false,"Preprocessing") ::
                    new BinaryOptionArgument[List[String]](
                                         name = "inputDropInfoTags", 
                                         arg = List("--inputDropInfoTags"), 
                                         valueName = "tag1,tag2,...",  
                                         argDesc =  "List of tags to DROP from the input file before processing. All other tags will be dropped "+
                                                    "before processing. This can be useful for updating a file with a new version or annotation, as it "+
                                                    "can be used to ensure a clean input."
                                        ).meta(false,"Preprocessing") ::
                    new BinaryOptionArgument[List[String]](
                                         name = "renameInputInfoTags", 
                                         arg = List("--renameInputInfoTags"), 
                                         valueName = "oldtag1:newtag1,oldtag2:newtag2,...",  
                                         argDesc =  "This allows you to rename INFO tags in the input file prior to processing. This can be useful for annotating a dataset with multiple versions or annotations."
                                        ).meta(true,"DEPRECATED") ::
                    new BinaryOptionArgument[List[String]](
                                         name = "renameInputGenoTags", 
                                         arg = List("--renameInputGenoTags"), 
                                         valueName = "oldtag1:newtag1,oldtag2:newtag2,...",  
                                         argDesc =  "This allows you to rename GENO tags in the input file prior to processing. This can be useful for annotating a dataset with multiple versions or annotations."
                                        ).meta(true,"DEPRECATED") ::
                    new BinaryOptionArgument[String](
                                         name = "sampleRenameFile", 
                                         arg = List("--sampleRenameFile"), 
                                         valueName = "sampleRenameFile",  
                                         argDesc =  "File must be in tab-delimited format, with columns labeled oldID and newID."
                                        ).meta(true,"DEPRECATED") ::
                                        
                                        
                    new BinaryArgument[Double](name = "BA1_AF",
                                           arg = List("--acmg_BA1_AF"),  
                                           valueName = "val", 
                                           argDesc = "The allele frequency cutoff to assign BA1 (benign) status.",
                                           defaultValue = Some(0.05)
                                           ).meta(true,"DEPRECATED") ::
                    new BinaryArgument[Double](name = "PM2_AF",
                                           arg = List("--acmg_PM2_AF"),  
                                           valueName = "val", 
                                           argDesc = "The allele frequency cutoff to assign PM2 (moderate pathogenic) status.",
                                           defaultValue = Some(0.0001)
                                           ).meta(true,"DEPRECATED") ::
                    new BinaryOptionArgument[String](
                                         name = "hgmdVarVcf", 
                                         arg = List("--hgmdVarVcf"), 
                                         valueName = "HGMD.vcf.gz",  
                                         argDesc =  "File containing HGMD variants All variants will be assumed to be likely pathogenic."
                                        ).meta(true,"INCOMPLETE",9995) :: 
                    new BinaryOptionArgument[String](
                                         name = "clinVarVcf", 
                                         arg = List("--clinVarVcf"), 
                                         valueName = "clinVarVcf.vcf",  
                                         argDesc =  "Processed clinvar variant vcf file. This file must have been processed by the addTxInfoToVCF command."
                                        ).meta(true,"INCOMPLETE") :: 
                                        //ctrlAlleFreqKeys
                    new BinaryMonoToListArgument[String](
                                         name = "ctrlAlleFreqKeys", 
                                         arg = List("--ctrlAlleFreqKeys"), 
                                         valueName = "...",  
                                         argDesc =  "A comma-delimited list of field IDs to use to calculate the maximum of several included allele frequency fields. "+
                                                    "Optionally, the list can be preceded by the output tag ID, followed by a colon. "+
                                                    "Can be specified more than once with different parameters and a different output tag ID."
                                        ).meta(false,"Annotation") :: 
                    new BinaryOptionArgument[String](
                                         name = "inSilicoParams", 
                                         arg = List("--inSilicoKeys"), 
                                         valueName = "...",  
                                         argDesc =  ""
                                        ).meta(true,"Annotation") :: 
                    new BinaryOptionArgument[String](
                                         name = "locusRepetitiveTag", 
                                         arg = List("--locusRepetitiveTag"), 
                                         valueName = "infotag",  
                                         argDesc =  ""
                                        ).meta(true,"DEPRECATED") :: 
                    new BinaryOptionArgument[String](
                                         name = "locusDomainTag", 
                                         arg = List("--locusDomainTag"), 
                                         valueName = "infotag",  
                                         argDesc =  ""
                                        ).meta(true,"DEPRECATED") :: 
                    new BinaryOptionArgument[String](
                                         name = "locusConservedTag", 
                                         arg = List("--locusConservedTag"), 
                                         valueName = "infotag",  
                                         argDesc =  ""
                                        ).meta(true,"DEPRECATED") :: 
                    new BinaryOptionArgument[String](
                                         name = "locusMappableTag", 
                                         arg = List("--locusMappableTag"), 
                                         valueName = "infotag",  
                                         argDesc =  ""
                                        ).meta(true,"DEPRECATED") :: 
                    new BinaryOptionArgument[String](
                                         name = "locusPseudoTag", 
                                         arg = List("--locusPseudoTag"), 
                                         valueName = "infotag",  
                                         argDesc =  ""
                                        ).meta(true,"DEPRECATED") ::
                    new BinaryOptionArgument[String](
                                         name = "toleranceFile", 
                                         arg = List("--toleranceFile"), 
                                         valueName = "geneVariantToleranceTable.txt",  
                                         argDesc =  ""
                                        ).meta(true,"DEPRECATED") ::
                                        
                    new BinaryOptionArgument[String](
                                         name = "inputChromDecoder", 
                                         arg = List("--inputChromDecoder"), 
                                         valueName = "chromDecoder.txt",  
                                         argDesc =  "Assigns a chromosome name decoder file. All chromosomes will be renamed based on this decoder. "+
                                                    "By default, the first column will be the FROM column and the second column will be the TO column. "+
                                                    "The FROM and TO column numbers can be changed with the --inputChromDecoderFromCol and --inputChromDecoderToCol "+
                                                    "parameters. These parameters can be used to translate chromosome names between the chr1,chr2,... and 1,2,... style "+
                                                    "chromosome name conventions. Any chrom names not found in this file will be left as is, throwing a warning. "+
                                                    "Note: the translation will take place BEFORE any other processing."
                                        ).meta(false,"Preprocessing") ::
                    new BinaryArgument[Int](name = "inputChromDecoderFromCol",
                                           arg = List("--inputChromDecoderFromCol"),  
                                           valueName = "val", 
                                           argDesc = "Define the column in the chromDecoder text file that contains the chrom names found in the original input VCF.",
                                           defaultValue = Some(0)
                                           ).meta(false,"Preprocessing") ::
                                           //calcStatGtTag
                    new BinaryArgument[Int](name = "inputChromDecoderToCol",
                                           arg = List("--inputChromDecoderToCol"),  
                                           valueName = "val", 
                                           argDesc = "Define the column in the chromDecoder text file that contains the chrom names that are to be used in the new output VCF.",
                                           defaultValue = Some(1)
                                           ).meta(false,"Preprocessing") ::
                                           
                                           
                    new BinaryArgument[String](name = "calcStatGtTag",
                                           arg = List("--calcStatGtTag"),  
                                           valueName = "GT", 
                                           argDesc = "The genotype tag used to calculate stats like het count, Het-AD-balance, etc.",
                                           defaultValue = Some("GT")
                                           ).meta(false,"Sample Stats") ::
                                           

                    new BinaryOptionListArgument[String](
                                         name = "outputKeepInfoTags", 
                                         arg = List("--outputKeepInfoTags"), 
                                         valueName = "tag1,tag2,...",  
                                         argDesc =  "List of tags to include in the final output"
                                        ).meta(false,"Postprocessing") ::
                    new BinaryOptionListArgument[String](
                                         name = "outputDropInfoTags", 
                                         arg = List("--outputDropInfoTags"), 
                                         valueName = "tag1,tag2,...",  
                                         argDesc =  "List of tags to drop from the final output"
                                        ).meta(false,"Postprocessing") ::
                    new BinaryOptionListArgument[String](
                                         name = "outputKeepGenoTags", 
                                         arg = List("--outputKeepGenoTags"), 
                                         valueName = "tag1,tag2,...",  
                                         argDesc =  "List of tags to include in the final output"
                                        ).meta(false,"Postprocessing") ::
                    new BinaryOptionListArgument[String](
                                         name = "outputDropGenoTags", 
                                         arg = List("--outputDropGenoTags"), 
                                         valueName = "tag1,tag2,...",  
                                         argDesc =  "List of tags to drop from the final output"
                                        ).meta(false,"Postprocessing") ::
                    new BinaryOptionListArgument[String](
                                         name = "outputConvertGenoTags", 
                                         arg = List("--outputConvertGenoTags"), 
                                         valueName = "tag1,tag2,...",  
                                         argDesc =  "Copy certain GENO tags to INFO. UNIMPLEMENTED."
                                        ).meta(true,"INCOMPLETE") ::
                                        
                    new BinaryOptionListArgument[String](
                                         name = "outputKeepSamples", 
                                         arg = List("--outputKeepSamples"), 
                                         valueName = "samp1,samp2,...",  
                                         argDesc =  "List of samples to include in the final output"
                                        ).meta(false,"Postprocessing") ::
                    new BinaryOptionArgument[String](
                                         name = "copyQualToInfo", 
                                         arg = List("--copyQualToInfo"), 
                                         valueName = "infoFieldName",  
                                         argDesc =  "Use this parameter to copy the QUAL field into an info field."
                        ).meta(true,"Preprocessing") ::  
                    new BinaryOptionArgument[String](
                                         name = "copyFilterToInfo", 
                                         arg = List("--copyFilterToInfo"), 
                                         valueName = "infoFieldName",  
                                         argDesc =  "Use this parameter to copy the FILTER field into an info field."
                        ).meta(true,"Preprocessing") ::
                    new BinaryOptionArgument[String](
                                         name = "copyIdToInfo", 
                                         arg = List("--copyIdToInfo"), 
                                         valueName = "infoFieldName",  
                                         argDesc =  "Use this parameter to copy the ID field into an info field."
                        ).meta(true,"Preprocessing") ::
                    new BinaryOptionArgument[String](
                                         name = "annTag", 
                                         arg = List("--annTag"), 
                                         valueName = "ANN",  
                                         argDesc =  "SnpEff ANN tag for info extract"
                        ).meta(false,"SnpEff Annotation Processing",20) ::
                    new BinaryOptionArgument[String](
                                         name = "snpEffTagPrefix", 
                                         arg = List("--snpEffTagPrefix"), 
                                         valueName = "ANNEX_",  
                                         argDesc =  "Prefix for SnpEff extracted info tags"
                        ).meta(false,"SnpEff Annotation Processing") ::
                    new BinaryOptionArgument[List[String]](
                                         name = "snpEffBiotypeKeepList", 
                                         arg = List("--snpEffBiotypeKeepList"), 
                                         valueName = "ANN",  
                                         argDesc =  "todo desc"
                        ).meta(false,"SnpEff Annotation Processing") ::
                    new BinaryOptionArgument[List[String]](
                                         name = "snpEffEffectKeepList", 
                                         arg = List("--snpEffEffectKeepList"), 
                                         valueName = "ANN",  
                                         argDesc =  "todo desc"
                        ).meta(false,"SnpEff Annotation Processing") ::
                    new BinaryOptionArgument[List[String]](
                                         name = "snpEffWarningDropList", 
                                         arg = List("--snpEffWarningDropList"), 
                                         valueName = "ANN",  
                                         argDesc =  "todo desc"
                        ).meta(false) ::
                    new BinaryOptionArgument[List[String]](
                                         name = "snpEffKeepIdx", 
                                         arg = List("--snpEffKeepIdx"), 
                                         valueName = "0,1,2,...",  
                                         argDesc =  "todo desc"
                        ).meta(false,"SnpEff Annotation Processing") ::
                        

                    new BinaryArgument[Int](name = "snpEffBiotypeIdx",
                                           arg = List("--snpEffBiotypeIdx"),  
                                           valueName = "7", 
                                           argDesc = "The index of the field in the annotation column that contains the biotype.",
                                           defaultValue = Some(7)
                                           ).meta(false,"SnpEff Annotation Processing") ::
                    new BinaryArgument[Int](name = "snpEffWarnIdx",
                                           arg = List("--snpEffWarnIdx"),  
                                           valueName = "7", 
                                           argDesc = "The index of the field in the annotation column that contains warnings or flags.",
                                           defaultValue = Some(15)
                                           ).meta(false,"SnpEff Annotation Processing") ::
                    new BinaryArgument[Int](name = "snpEffFieldLen",
                                           arg = List("--snpEffFieldLen"),  
                                           valueName = "16", 
                                           argDesc = "The the length of an annotation entry.",
                                           defaultValue = Some(16)
                                           ).meta(false,"SnpEff Annotation Processing") ::
                    new BinaryOptionArgument[List[String]](
                                         name = "snpEffFields", 
                                         arg = List("--snpEffFields"), 
                                         valueName = "Allele,Consequence,...",  
                                         argDesc =  "The names of the fields in the annotation entries."
                        ).meta(false,"SnpEff Annotation Processing") ::

                    new BinaryMonoToListArgument[String](name = "snpEffVarExtract",
                                           arg = List("--snpEffVarExtract"),  
                                           valueName = "outTag|0,1,...|description", 
                                           argDesc = "todo desc"
                                           ).meta(true,"SnpEff Annotation Processing") ::
                    new BinaryArgument[String](name = "snpEffGeneListTagInfix",
                                           arg = List("--snpEffGeneListTagInfix"),  
                                           valueName = "myGeneListAbbrev", 
                                           argDesc = "A short abbreviation for your selected snpeff gene list, for use in the INFO tag IDs.",
                                           defaultValue = Some("OnList_")
                                           ).meta(false,"SnpEff Annotation Processing") ::
                    new BinaryMonoToListArgument[String](
                                         name = "varMatchDbName", 
                                         arg = List("--varMatchDbName"),  
                                         valueName = "...",  
                                         argDesc =  "For advanced users only. Seriously don't mess with this unless you've talked to the author."
                                        ).meta(true,"INCOMPLETE") ::
                    new BinaryMonoToListArgument[String](
                                         name = "varMatchDbFile", 
                                         arg = List("--varMatchDbFile"), 
                                         valueName = "...",  
                                         argDesc =  "For advanced users only. Seriously don't mess with this unless you've talked to the author."
                                        ).meta(true,"INCOMPLETE") ::
                    new BinaryMonoToListArgument[String](
                                         name = "varMatchIdTag", 
                                         arg = List("--varMatchIdTag"), 
                                         valueName = "...",  
                                         argDesc =  "For advanced users only. Seriously don't mess with this unless you've talked to the author."
                                        ).meta(true,"INCOMPLETE") ::
                    new BinaryMonoToListArgument[String](
                                         name = "varMatchPathoExpression", 
                                         arg = List("--varMatchPathoExpression"), 
                                         valueName = "...",  
                                         argDesc =  "For advanced users only. Seriously don't mess with this unless you've talked to the author."
                                        ).meta(true,"INCOMPLETE") ::                                        
                    new BinaryMonoToListArgument[String](
                                         name = "varMatchBenignExpression", 
                                         arg = List("--varMatchBenignExpression"), 
                                         valueName = "...",  
                                         argDesc =  "For advanced users only. Seriously don't mess with this unless you've talked to the author."
                                        ).meta(true,"INCOMPLETE") ::   
                    new BinaryMonoToListArgument[String](
                                         name = "varMatchMetadataList", 
                                         arg = List("--varMatchMetadataList"), 
                                         valueName = "...",  
                                         argDesc =  "For advanced users only. Seriously don't mess with this unless you've talked to the author."
                                        ).meta(true,"INCOMPLETE") ::
                    new BinaryMonoToListArgument[String](
                                         name = "varMatchGeneList", 
                                         arg = List("--varMatchGeneList"), 
                                         valueName = "...",  
                                         argDesc =  "For advanced users only. Seriously don't mess with this unless you've talked to the author."
                                        ).meta(true,"INCOMPLETE") ::
                                        
                                        

                    new BinaryMonoToListArgument[String](
                                         name = "addHeaderLineSubtype", 
                                         arg = List("--addHeaderLineSubtype"), 
                                         valueName = "...",  
                                         argDesc =  "For advanced users only. Seriously don't mess with this unless you've talked to the author."
                                        ).meta(true,"INCOMPLETE") ::

                    new BinaryOptionArgument[String](
                                         name = "findComplexVarIdx", 
                                         arg = List("--findComplexVarIdx"), 
                                         valueName = ".",  
                                         argDesc =  ""
                                        ).meta(true,"INCOMPLETE") ::
                    new UnaryArgument( name = "findComplexVars",
                                         arg = List("--findComplexVars"), // name of value
                                         argDesc = ""
                                       ).meta(true,"INCOMPLETE") ::
                    new BinaryOptionArgument[List[String]](
                                         name = "findComplexVarMergeInfoTags", 
                                         arg = List("--findComplexVarMergeInfoTags"), 
                                         valueName = "",  
                                         argDesc =  ""
                                        ).meta(true,"INCOMPLETE") ::


                                        
                    new FinalArgument[String](
                                         name = "infile",
                                         valueName = "variants.vcf",
                                         argDesc = "input VCF file. Can be gzipped or in plaintext." // description
                                        ).meta(false,"Mandatory Inputs") ::

                    new FinalArgument[String](
                                         name = "outfile",
                                         valueName = "outfile.vcf.gz",
                                         argDesc = "The output file. Can be gzipped or in plaintext."// description
                                        ).meta(false,"Mandatory Inputs") ::
                    internalUtils.commandLineUI.CLUI_UNIVERSAL_ARGS,

             manualExtras = manualExtras,
             markdownManualExtras = markdownManualExtras
       );

     
     
     
         /*manualExtras = QC_DEFAULT_ON_FUNCTION_MAP.foldLeft("DEFAULT SUB-FUNCTIONS\n")((soFar,curr) => {
           val depString = QC_FUNCTION_DEPENDANCIES.get(curr._1) match {
             case Some(depset) => " [Depends: "+depset.toList.mkString(", ")+"]";
             case None => "";
           }
           soFar + "    "+curr._1+"\n"+wrapLineWithIndent(curr._2 + depString,internalUtils.commandLineUI.CLUI_CONSOLE_LINE_WIDTH,8)+"\n";
         }) + QC_DEFAULT_OFF_FUNCTION_MAP.foldLeft("NON-DEFAULT SUB-FUNCTIONS\n")((soFar,curr) => {
           val depString = QC_FUNCTION_DEPENDANCIES.get(curr._1) match {
             case Some(depset) => " [Depends: "+depset.toList.mkString(", ")+"]";
             case None => "";
           }
           soFar + "    "+curr._1+"\n"+wrapLineWithIndent(curr._2+depString,internalUtils.commandLineUI.CLUI_CONSOLE_LINE_WIDTH,8)+"\n";
         }),
         markdownManualExtras = QC_DEFAULT_ON_FUNCTION_MAP.foldLeft("## DEFAULT SUB-FUNCTIONS:\n")((soFar,curr) => {
           val depString = QC_FUNCTION_DEPENDANCIES.get(curr._1) match {
             case Some(depset) => " [Depends: "+depset.toList.mkString(", ")+"]";
             case None => "";
           }
           //"### "+(getFullSyntax()).replaceAll("_","\\\\_")+":\n\n> "+(describe()).replaceAll("_","\\\\_")+ (" ("+argType+")\n\n").replaceAll("_","\\\\_");
           soFar + "* " + curr._1 + ": " + curr._2 + depString + "\n\n";
         }) + QC_DEFAULT_OFF_FUNCTION_MAP.foldLeft("## NON-DEFAULT SUB-FUNCTIONS:\n")((soFar,curr) => {
           val depString = QC_FUNCTION_DEPENDANCIES.get(curr._1) match {
             case Some(depset) => " [Depends: "+depset.toList.mkString(", ")+"]";
             case None => "";
           }
           soFar + "* " + curr._1 + ": " + curr._2 + depString + "\n\n";
         })*/
     
     
     def run(args : Array[String]) {
       val out = parser.parseArguments(args.toList.tail);
       if(out){
         
         if(parser.get[Option[String]]("dropVariantsExpression").isDefined){
           error("ERROR: parameter --dropVariantsExpression has been replaced with --keepVariantsExpression!");
         }
         
          runSAddTXAnno( parser.get[String]("infile"),
                       parser.get[String]("outfile"),
                       gtffile = parser.get[Option[String]]("gtfFile"),
                       genomeFA = parser.get[Option[String]]("genomeFA"),
                       txInfoFile = parser.get[Option[String]]("txInfoFile"),
                       summaryFile = parser.get[Option[String]]("summaryFile"),
                       addStopCodon = ! parser.get[Boolean]("cdsRegionContainsStop"),
                       inputSavedTxFile = parser.get[Option[String]]("inputSavedTxFile"),
                       outputSavedTxFile = None, //parser.get[Option[String]]("outputSavedTxFile"),
                       chromList = parser.get[Option[List[String]]]("chromList"),
                       txToGeneFile = parser.get[Option[String]]("txToGeneFile"),
                       groupFile = parser.get[Option[String]]("groupFile"),
                       superGroupList = parser.get[Option[String]]("superGroupList"),
                       splitMultiAllelics = parser.get[Boolean]("splitMultiAllelics") || parser.get[Boolean]("splitMultiAllelicsNoStarAlle"),
                       addSummaryCLNSIG = parser.get[Boolean]("addSummaryCLNSIG"),
                       addCanonicalTags = parser.get[Option[String]]("addCanonicalTags"),
                       geneVariantsOnly = parser.get[Boolean]("geneVariantsOnly"),
                       nonNullVariantsOnly = parser.get[Boolean]("nonNullVariantsOnly"),
                       addBedTags = parser.get[Option[List[String]]]("addBedTags"),
                       infileList = parser.get[Boolean]("infileList"),
                       infileListInfix = parser.get[Option[String]]("infileListInfix"),
                       genoFilter = parser.get[Option[String]]("genoFilter"),
                       filterTag = parser.get[Option[String]]("filterTag"),
                       filterOutputGtTag = parser.get[String]("filterOutputGtTag"),
                       filterInputGtTag = parser.get[String]("filterInputGtTag"),
                       unfilteredGtTag = parser.get[Option[String]]("unfilteredGtTag"),
                       dbnsfpFile = None,
                       dbnsfpTags = None,
                       singleCallerVcfs = parser.get[Option[List[String]]]("singleCallerVcfsOld"),
                       singleCallerVcfsNew = parser.get[Option[List[String]]]("singleCallerVcfsNew"),
                       singleCallerVcfNames = parser.get[Option[List[String]]]("singleCallerVcfNames"),
                       singleCallerPriority = parser.get[Option[List[String]]]("singleCallerPriority"),
                       singleCallerMaster = parser.get[Option[List[String]]]("singleCallerMaster"),
                       singleCallerFileListFile = None,
                       numLinesRead = parser.get[Option[Int]]("numLinesRead"),
                       addInfoVcfs = parser.get[Option[List[String]]]("addInfoVcfs"),
                       dropVariantsExpression = parser.get[Option[String]]("keepVariantsExpression"),
                       tagVariantsExpression = parser.get[List[String]]("tagVariantsExpression"),
                       convertToStandardVcf = parser.get[Boolean]("convertToStandardVcf"),
                       addSampTag = parser.get[Option[String]]("addSampTag"),
                       addDepthStats = parser.get[Option[String]]("addDepthStats"),
                       splitAlleleGroupCounts = parser.get[Boolean]("splitAlleleGroupCounts"),
                       BA1_AF = parser.get[Double]("BA1_AF"),
                       PM2_AF = parser.get[Double]("PM2_AF"),
                       hgmdVarVcf = parser.get[Option[String]]("hgmdVarVcf"),
                       clinVarVcf = parser.get[Option[String]]("clinVarVcf"),
                       ctrlAlleFreqKeys = parser.get[List[String]]("ctrlAlleFreqKeys"),
                inSilicoParams = parser.get[Option[String]]("inSilicoParams"),
                locusRepetitiveTag = parser.get[Option[String]]("locusRepetitiveTag"),
                locusDomainTag = parser.get[Option[String]]("locusDomainTag"),
                locusConservedTag = parser.get[Option[String]]("locusConservedTag"),
                locusMappableTag = parser.get[Option[String]]("locusMappableTag"),
                locusPseudoTag = parser.get[Option[String]]("locusPseudoTag"),
                toleranceFile = parser.get[Option[String]]("toleranceFile"),
                alphebetizeHeader = parser.get[Boolean]("alphebetizeHeader"),
                inputKeepInfoTags = parser.get[Option[List[String]]]("inputKeepInfoTags"),
                inputDropInfoTags = parser.get[Option[List[String]]]("inputDropInfoTags"),
                renameInputInfoTags = parser.get[Option[List[String]]]("renameInputInfoTags"),
                renameInputGenoTags = parser.get[Option[List[String]]]("renameInputGenoTags"),
                dropGenotypeData = parser.get[Boolean]("dropGenotypeData"),
                outputKeepInfoTags = parser.get[Option[List[String]]]("outputKeepInfoTags"),
                outputDropInfoTags = parser.get[Option[List[String]]]("outputDropInfoTags"),
                outputKeepGenoTags = parser.get[Option[List[String]]]("outputKeepGenoTags"),
                outputDropGenoTags = parser.get[Option[List[String]]]("outputDropGenoTags"),
                outputKeepSamples = parser.get[Option[List[String]]]("outputKeepSamples"),
                inputKeepSamples = parser.get[Option[List[String]]]("inputKeepSamples"),
                variantStatExpression = parser.get[Option[List[String]]]("variantStatExpression"),
                inputChromDecoder = parser.get[Option[String]]("inputChromDecoder"),
                inputChromDecoderFromCol = parser.get[Int]("inputChromDecoderFromCol"),
                inputChromDecoderToCol = parser.get[Int]("inputChromDecoderToCol"),
                sampleRenameFile = parser.get[Option[String]]("sampleRenameFile"),
                unPhaseAndSortGenotypes = parser.get[Option[List[String]]]("unPhaseAndSortGenotypes"),
                calcStatGtTag = parser.get[String]("calcStatGtTag"),
                addVariantIdx = parser.get[Option[String]]("addVariantIdx"),
                addVariantPosInfo = parser.get[Option[String]]("addVariantPosInfo"),
                leftAlignAndTrim = parser.get[Boolean]("leftAlignAndTrim"),
                leftAlignAndTrimWindow = parser.get[Option[Int]]("leftAlignAndTrimWindow"),
                runEnsembleMerger = parser.get[Boolean]("runEnsembleMerger"),
                convertROAOtoAD = parser.get[Boolean]("convertROAOtoAD"),
                snpSiftAnnotate = parser.get[List[String]]("snpSiftAnnotate"),
                fixDotAltIndels = parser.get[Boolean]("fixDotAltIndels"),
                dropSymbolicAlleleLines = parser.get[Boolean]("dropSymbolicAlleleLines"),
                addHeaderLineSubtype = parser.get[List[String]]("addHeaderLineSubtype"),
                addRatioTag = parser.get[List[String]]("addRatioTag"),
                calcRunInfix = parser.get[Option[String]]("calcRunInfix"),
                snpSiftDbnsfp = parser.get[List[String]]("snpSiftDbnsfp"),
                rmDup = parser.get[Boolean]("rmDup"),
                convertGtToMatrix = parser.get[Option[List[String]]]("convertGtToMatrix"),
                convertMatrixToGt = parser.get[Boolean]("convertMatrixToGt"),
                varMatchDbName = parser.get[List[String]]("varMatchDbName"),
                varMatchDbFile = parser.get[List[String]]("varMatchDbFile"),
                varMatchIdTag = parser.get[List[String]]("varMatchIdTag"),
                varMatchPathoExpression = parser.get[List[String]]("varMatchPathoExpression"),
                varMatchBenignExpression = parser.get[List[String]]("varMatchBenignExpression"),
                varMatchMetadataList = parser.get[List[String]]("varMatchMetadataList"),
                varMatchGeneList = parser.get[List[String]]("varMatchGeneList"),
                geneList = parser.get[Option[List[String]]]("geneList"),
                addTxSummaryInfo = parser.get[Boolean]("addTxSummaryInfo"),
                findComplexVars = parser.get[Boolean]("findComplexVars"),
                complexVarIdx = parser.get[Option[String]]("findComplexVarIdx"),
                findComplexVarMergeInfoTags = parser.get[Option[List[String]]]("findComplexVarMergeInfoTags").getOrElse(List[String]()),
                copyFmtTag = parser.get[List[String]]("copyFmtTag"),
                
                annTag = parser.get[Option[String]]("annTag"),
                snpEffBiotypeKeepList = parser.get[Option[List[String]]]("snpEffBiotypeKeepList"),
                snpEffEffectKeepList = parser.get[Option[List[String]]]("snpEffEffectKeepList"),
                snpEffWarningDropList = parser.get[Option[List[String]]]("snpEffWarningDropList"),
                snpEffKeepIdx = parser.get[Option[List[String]]]("snpEffKeepIdx"),
                
                snpEffTagPrefix = parser.get[Option[String]]("snpEffTagPrefix"),
                ensGeneList = parser.get[Option[List[String]]]("ensGeneList"),
                txTypes = parser.get[Option[List[String]]]("txTypes"),
                
                addLocalGcInfo = parser.get[List[String]]("addLocalGcInfo"),
                
                mergeBooleanTags = parser.get[List[String]]("mergeBooleanTags"),
                
                copyQualToInfo = parser.get[Option[String]]("copyQualToInfo"),
                copyFilterToInfo = parser.get[Option[String]]("copyFilterToInfo"),
                copyIdToInfo  = parser.get[Option[String]]("copyIdToInfo"),
                noGroupStats  = parser.get[Boolean]("noGroupStats"),
                splitMultiAllelicsNoStarAlle = parser.get[Boolean]("splitMultiAllelicsNoStarAlle"),
                
                splitOutputByChrom = parser.get[Boolean]("splitOutputByChrom"),
                splitOutputByBed = parser.get[Option[String]]("splitOutputByBed"),
                
                tagVariantsFunction = parser.get[List[String]]("tagVariantsFunction"),
                
                tableInput = parser.get[Boolean]("tableInput"),
                tableOutput = parser.get[Boolean]("tableOutput"),
                tagVariantsGtCountExpression = parser.get[List[String]]("tagVariantsGtCountExpression"),
                makeFirstBaseMatch = parser.get[Boolean]("makeFirstBaseMatch"),
                
                thirdAlleleChar = parser.get[Option[String]]("thirdAlleleChar"),
                
                dropSpanIndels = parser.get[Boolean]("dropSpanIndels"),
                dropVariantsWithNs = parser.get[Boolean]("dropVariantsWithNs"),
                //tallyFile = parser.get[Option[String]]("tallyFile"),
                
                duplicateTag = parser.get[Option[String]]("duplicatesTag"),
                
                ensembleGenotypeDecision = parser.get[String]("ensembleGenotypeDecision"),
                addDummyGenotypeColumn = parser.get[Boolean]("addDummyGenotypeColumn"),
                snpEffVarExtract = parser.get[List[String]]("snpEffVarExtract"),
                snpEffGeneListTagInfix = parser.get[String]("snpEffGeneListTagInfix"),
                
                homopolymerRunStats = parser.get[Option[String]]("homopolymerRunStats"),
                addContextBases = parser.get[List[String]]("addContextBases"),
                addSampCountWithMultVector = parser.get[List[String]]("addSampCountWithMultVector"),
                snpEffBiotypeIdx = parser.get[Int]("snpEffBiotypeIdx"),
                snpEffWarnIdx = parser.get[Int]("snpEffWarnIdx"),
                snpEffFieldLen = parser.get[Int]("snpEffFieldLen"),
                snpEffFields = parser.get[Option[List[String]]]("snpEffFields"),
                addWiggleAnnotation = parser.get[List[String]]("addWiggleAnnotation"),
                snpEffAnnotate = parser.get[Option[String]]("snpEffAnnotate"),
                
                keepVariantsExpressionPrefilter = parser.get[Option[String]]("keepVariantsExpressionPrefilter"),
                calcBurdenCounts = parser.get[List[String]]("calcBurdenCounts"),
                burdenCountsFile = parser.get[Option[String]]("burdenCountsFile")
                
                // calcBurdenCounts burdenCountsFile
                //ensembleGenotypeDecision : String = "majority_firstOnTies"
                //dropVariantsWithNs, tallyFile   
             )
       }
     }
  }

    /*SAddSampCountWithMultVector(tagID : String, gtTag : String, desc : String, vectorFile : String)
                    new BinaryMonoToListArgument[String](name = "snpEffVarExtract",
                                           arg = List("--snpEffVarExtract"),  
                                           valueName = "outTag|0,1,...|description", 
                                           argDesc = "todo desc",
                                           ).meta(true,"SnpEff Annotation Processing") ::
                    new BinaryArgument[String](name = "snpEffGeneListTagInfix",
     ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    */

  def runSAddTXAnno(vcffile : String, outfile : String, 
                gtffile : Option[String], 
                genomeFA : Option[String],
                summaryFile : Option[String],
                txInfoFile : Option[String],
                addStopCodon : Boolean,
                inputSavedTxFile : Option[String],
                outputSavedTxFile : Option[String],
                chromList : Option[List[String]],
                txToGeneFile : Option[String],
                groupFile : Option[String],
                superGroupList : Option[String],
                splitMultiAllelics : Boolean,
                addSummaryCLNSIG : Boolean,
                addCanonicalTags : Option[String],
                geneVariantsOnly : Boolean,
                nonNullVariantsOnly : Boolean,
                addBedTags : Option[List[String]],
                infileList : Boolean = false,
                infileListInfix : Option[String] = None,
                bufferSize : Int = 32, 
                vcfCodes : VCFAnnoCodes = VCFAnnoCodes(),
                genoFilter : Option[String] = None,
                filterTag : Option[String] = Some(OPTION_TAGPREFIX+"FILT_GT"),
                filterOutputGtTag : String = "GT",
                filterInputGtTag : String = "GT",
                unfilteredGtTag : Option[String] = Some("GT_PREFILT"),
                dbnsfpFile : Option[String] = None,
                dbnsfpTags : Option[List[String]] = None,
                singleCallerVcfs : Option[List[String]] = None,
                singleCallerVcfsNew : Option[List[String]] = None,
                singleCallerVcfNames : Option[List[String]] = None,
                singleCallerPriority : Option[List[String]] = None,
                singleCallerMaster : Option[List[String]] = None,
                singleCallerFileListFile : Option[String] = None,
                numLinesRead : Option[Int] = None,
                addInfoVcfs : Option[List[String]] = None,
                dropVariantsExpression : Option[String] = None,
                tagVariantsExpression : List[String] = List[String](),
                convertToStandardVcf : Boolean = false,
                addSampTag : Option[String] = None,
                addDepthStats : Option[String] = None,
                splitAlleleGroupCounts : Boolean = false,
                BA1_AF : Double = 0.0,
                PM2_AF : Double = 0.0,
                clinVarVcf : Option[String] = None,
                hgmdVarVcf : Option[String] = None,
                ctrlAlleFreqKeys : List[String] = List[String](),
                inSilicoParams : Option[String] = None,
                locusRepetitiveTag : Option[String] = None,
                locusDomainTag : Option[String] = None,
                locusConservedTag : Option[String] = None,
                locusMappableTag : Option[String] = None,
                locusPseudoTag : Option[String] = None,
                toleranceFile : Option[String] = None,
                alphebetizeHeader : Boolean = false,
                inputKeepInfoTags : Option[List[String]] = None,
                inputDropInfoTags : Option[List[String]] = None,
                renameInputInfoTags : Option[List[String]] = None,
                renameInputGenoTags : Option[List[String]] = None,
                dropGenotypeData : Boolean = false,
                
                outputKeepInfoTags : Option[List[String]] = None,
                outputDropInfoTags : Option[List[String]] = None,
                outputKeepGenoTags : Option[List[String]] = None,
                outputDropGenoTags : Option[List[String]] = None,
                outputKeepSamples : Option[List[String]] = None,
                inputKeepSamples : Option[List[String]] = None,
                variantStatExpression : Option[List[String]] = None,
                
                inputChromDecoder : Option[String] = None,
                inputChromDecoderFromCol : Int = 0,
                inputChromDecoderToCol : Int = 1,
                
                sampleRenameFile : Option[String] = None,
                unPhaseAndSortGenotypes : Option[List[String]] = None,
                calcStatGtTag : String = "GT",
                addVariantIdx : Option[String] = None,
                
                addVariantPosInfo : Option[String] = None,
                
                makeFirstBaseMatch : Boolean = false,
                leftAlignAndTrim : Boolean = false,
                leftAlignAndTrimWindow : Option[Int] = None,
                
                runEnsembleMerger : Boolean = false,
                convertROAOtoAD : Boolean = false,
                snpSiftAnnotate : List[String] = List[String](),
                snpEffAnnotate : Option[String] = None,
                fixDotAltIndels : Boolean = false,
                dropSymbolicAlleleLines : Boolean = false,
                addHeaderLineSubtype : List[String] = List[String](),
                addRatioTag : List[String] = List[String](),
                calcRunInfix : Option[String] = None,
                snpSiftDbnsfp : List[String] = List[String](),
                rmDup : Boolean = false,
                convertGtToMatrix : Option[List[String]] = None,
                convertMatrixToGt : Boolean = false,
                varMatchDbName : List[String] = List[String](),
                varMatchDbFile : List[String] = List[String](),
                varMatchIdTag : List[String] = List[String](),
                varMatchPathoExpression : List[String] = List[String](),
                varMatchBenignExpression : List[String] = List[String](),
                varMatchMetadataList : List[String] = List[String](),
                varMatchGeneList : List[String] = List[String](),
                
                geneList : Option[List[String]] = None,
                addTxSummaryInfo : Boolean = false,
                
                findComplexVars : Boolean = false,
                complexVarIdx : Option[String] = None,
                findComplexVarMergeInfoTags : List[String] = List[String](),
                copyFmtTag : List[String] = List[String](),
                
                annTag : Option[String]  = None,
                snpEffBiotypeKeepList : Option[List[String]] = None,
                snpEffEffectKeepList : Option[List[String]] = None,
                snpEffWarningDropList : Option[List[String]] = None,
                snpEffKeepIdx : Option[List[String]] = None,
                
                snpEffTagPrefix : Option[String] = None,
                ensGeneList : Option[List[String]] = None,
                txTypes : Option[List[String]] = None,
                
                addLocalGcInfo : List[String] = List(),
                
                mergeBooleanTags : List[String] = List(),
                copyQualToInfo : Option[String] = None,
                copyFilterToInfo : Option[String] = None,
                copyIdToInfo : Option[String] = None,
                
                noGroupStats : Boolean = false,
                splitMultiAllelicsNoStarAlle : Boolean = false,
                
                splitOutputByChrom : Boolean = false,
                splitOutputByBed : Option[String] = None,
                
                tagVariantsFunction : List[String] = List[String](),
                
                tableInput : Boolean = false,
                tableOutput : Boolean = false,
                
                tagVariantsGtCountExpression : List[String]  = List[String](),
                
                thirdAlleleChar : Option[String] = None,
                dropSpanIndels : Boolean = false,
                
                dropVariantsWithNs : Boolean = false,
                //tallyFile : Option[String] = None,
                
                duplicateTag : Option[String] = None,
                
                ensembleGenotypeDecision : String = "majority_firstOnTies",
                addDummyGenotypeColumn : Boolean = false,
                
                snpEffVarExtract : List[String] = List[String](),
                snpEffGeneListTagInfix : String = "onList",
                
                homopolymerRunStats : Option[String] = None,
                addContextBases : List[String] = List(),
                addSampCountWithMultVector : List[String] = List(),
                
                snpEffBiotypeIdx : Int = 7,
                snpEffWarnIdx : Int = 15,
                snpEffFieldLen : Int = 16,
                snpEffFields : Option[List[String]] = None,
                
                addWiggleAnnotation : List[String] = List[String](),
                
                keepVariantsExpressionPrefilter : Option[String] = None,
                calcBurdenCounts : List[String] = List[String](),
                burdenCountsFile : Option[String] = None
                ){

                                            /*
                         *snpEffBiotypeIdx : Int = 7,
                          snpEffWarnIdx : Int = 15,
                          snpEffFieldLen : Int = 16,
                          snpEffFields : Option[List[String]] = None
                         */
    /*
     * SAddSampCountWithMultVector(tagID : String, gtTag : String, desc : String, vectorFile : String)
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
    
    

            def getMergeBooleanTags(mbt : String) : SVcfWalker = {
              val cells : Array[String] = mbt.split(",")
              val tagID : String = cells(0);
              val mergeTags : List[String] = cells(1).split( "[|]" ).toList
              val tagNames : Option[List[String]] = cells.lift(2).map{ cc => cc.split("[|]").toList }
              
              new MergeBooleanTags(tagID = tagID, mergeTags = mergeTags, tagNames = tagNames)
            }
            def getTagVariantsFunction(ftString : String) : SVcfWalker = {
              val cells = ftString.split("[|]",-1)
              val tagID = cells(0);
              val desc  = cells(1);
              val funcString = cells(2);
              val paramTags = cells.lift(3).map{_.split(",").toSeq}.getOrElse(Seq[String]());
              val outDigits = cells.lift(4).map{ _.toInt }
              
              new AddFuncTag(func = funcString, newTag=tagID, paramTags=paramTags, digits = outDigits, desc = Some(desc));
            }
            def getTagVariantsExpression(exprStringRaw : String) : SVcfWalker = {
               val cells : Vector[String] = exprStringRaw.split("\\|").toVector.map{s => s.trim()};
                  if(cells.length > 5){
                    error("Each comma-delimited element of parameter tagVariantsExpression must have three bar-delimited parts: tag, desc, and expression. Found cells=["+cells.mkString("|")+"]");
                  }
                  if(cells.head.startsWith("MODE=")){
                    val mode = cells.head.drop(5);
                    if(mode == "VAR"){
                      if(cells.length != 4){
                        error("for MODE=VAR, tagVariantsExpression must be a |-delimited list of length 4.")
                      }
                      reportln("Creating tagging utility, MODE=VAR. "+getDateAndTimeString,"debug");
                      val (tagID,tagDesc,expr) = (cells(1),cells(2),cells(3));
                      VcfExpressionTag(expr = expr, tagID = tagID, tagDesc = tagDesc);
                    } else if(mode == "GENELIST"){
                      reportln("Creating tagging utility, MODE=GENELIST. "+getDateAndTimeString,"debug");
                      val (tagID,tagDesc,expr) = (cells(1),cells(2),cells(3));
                      val geneTagString = cells.lift(4);
                      val subGeneTagString = cells.lift(5);
                      VcfExpressionTag(expr = expr, tagID = tagID, tagDesc = tagDesc, 
                                     geneTagString = geneTagString, subGeneTagString = subGeneTagString, geneList = geneList);
                    } else if(mode.startsWith("GT")){
                      reportln("Creating tagging utility, MODE="+mode+". "+getDateAndTimeString,"debug");
                      val (tagID,tagDesc,expr) = (cells(1),cells(2),cells(3));
                      //val styleOpt = cells.lift(4);
                      VcfGtExpressionTag( expr=expr,tagID=tagID,tagDesc=tagDesc,style = mode,                  
                                          groupFile = groupFile, groupList = None, superGroupList  = superGroupList )
                    } else {
                      error("UNKNOWN/INVALID tagVariantsExpression MODE:\""+mode+"\"!")
                      new PassThroughSVcfWalker()
                    }
                  } else if(cells.length == 3){
                    val (tagID,tagDesc,expr) = (cells(0),cells(1),cells(2));
                    reportln("Creating Variant tagging utility ("+cells(0)+") ... "+getDateAndTimeString,"debug");
                     VcfExpressionTag(expr = expr, tagID = tagID, tagDesc = tagDesc);
                  } else {
                    error("Invalid tagVariantsExpression tag: \""+exprStringRaw+"\"");
                     new PassThroughSVcfWalker()
                  }
            }
            
            
    val summaryWriter = if(summaryFile.isEmpty) None else Some(openWriterSmart(summaryFile.get));
    val burdenWriter = burdenCountsFile.map{ bcf => openWriterSmart(bcf)};
    
    burdenWriter.foreach{ bw => {
      //out.write(tagID + "\t" + g+"\t"+mtr.count( pp => pp > 0)+"\t"+altCt+"\t"+varCt+"\n");
      bw.write("tagID\tgeneID\tburdenCt\taltCt\tvarCt\n");
    }}
    reportln("Initializing VCF walker ... "+getDateAndTimeString,"debug");

    val deepDebugMode = false;
    val debugMode = true;
    
    val initWalkers : Seq[SVcfWalker] =         (
            if(debugMode){
              Seq[SVcfWalker]( PassThroughSVcfWalker("inputVCF",true) )
            } else {
              Seq[SVcfWalker]()
            }
        ) ++ (
            if(deepDebugMode){
              Seq[SVcfWalker]( DebugSVcfWalker() )
            } else {
              Seq[SVcfWalker]()
            }
        ) ++ (
            if(addVariantIdx.isDefined){
                val (tagString,idxPrefix,addIdxAtStart) = (addVariantIdx.get.split(",").head, addVariantIdx.get.split(",").lift(1), addVariantIdx.get.split(",").drop(2).toSet )
                if(addIdxAtStart.contains("0")){
                  Seq[SVcfWalker](new AddVariantIdx(tag = tagString,idxPrefix = idxPrefix))
                } else {
                  Seq[SVcfWalker]()
                }
            } else {
                Seq[SVcfWalker]()
            }
        ) ++ (
            addHeaderLineSubtype.map{_.split(":")}.map{hls => (hls(0).split(","),hls.tail.map{_.split("=")})}.map{ case (tagList,opts) => {
              //EditInfoAndFormatTags(tagList : Seq[String], subtype : Option[String] = None, num : Option[String] = None,typ : Option[String] = None)
              val num : Option[String] = opts.find{ x => x(0) == "Number"}.map{x => x(1)};
              val typ : Option[String] = opts.find{ x => x(0) == "Type"}.map{x => x(1)};
              val subtype : Option[String] = opts.find{ x => x(0) == "subType"}.map{x => x(1)};
              new EditInfoAndFormatTags(tagList,subtype = subtype,num=num,typ=typ);
            }}
        ) ++ (
            if(fixDotAltIndels){
              Seq[SVcfWalker]( new internalUtils.GatkPublicCopy.FixDotAltVcfLines(genomeFa = genomeFA.get ) )
            } else {
              Seq[SVcfWalker]()
            }
        ) ++ (
            if(nonNullVariantsOnly){
              Seq[SVcfWalker]( SFilterNonVariantWalker() )
            } else {
              Seq[SVcfWalker]()
            }
        ) ++ (
            inputChromDecoder match {
              case Some(icd) => {
                Seq[SVcfWalker](ChromosomeConverter(chromDecoder = icd, fromCol= inputChromDecoderFromCol, toCol = inputChromDecoderToCol))
              }
              case None => Seq[SVcfWalker]();
            }
        ) ++ (
            
            keepVariantsExpressionPrefilter.map{ expr => {
               reportln("Creating Variant Filter utility (PREFILTER) ... "+getDateAndTimeString,"debug");
               VcfExpressionFilter(filterExpr = expr);
            }}
        
        ) ++ (
            if(inputDropInfoTags.isDefined || inputKeepInfoTags.isDefined || renameInputGenoTags.isDefined || renameInputInfoTags.isDefined || inputKeepSamples.isDefined || unPhaseAndSortGenotypes.isDefined){
              Seq[SVcfWalker](
                  FilterTags(
                     keepGenotypeTags = None,
                     dropGenotypeTags = List[String](),
                     keepInfoTags = inputKeepInfoTags,
                     dropInfoTags = inputDropInfoTags.getOrElse(List[String]()),
                     dropAsteriskAlleles = false,
                     keepSamples = inputKeepSamples,
                     dropSamples = List[String](),
                     alphebetizeHeader = false,
                     renameInfoTags = renameInputInfoTags,
                     renameGenoTags = renameInputGenoTags,
                     sampleRenameFile = sampleRenameFile,
                     unPhaseAndSortGenotypes = unPhaseAndSortGenotypes
                  )
              )
            } else {
              Seq[SVcfWalker]()
            }
        ) ++ (
            copyFmtTag.map{ cft => {
              val cftCells = cft.split(",");
              if(cftCells.length < 2){
                error("Error: copyFmtTags requires 2 elements");
              }
              val oldTag = cftCells(0)
              val newTag = cftCells(1)
              new CopyFmtTag( oldTag = oldTag, newTag = newTag, false);
            }}
            
            
        ) ++ (
            
            addVariantPosInfo match {
              case Some(avpi) => {
                Seq[SVcfWalker](AddVariantPosInfoWalker(avpi));
              }
              case None => {
                Seq[SVcfWalker]();
              }
            }
        ) ++ (
            if(copyQualToInfo.isDefined || copyFilterToInfo.isDefined || copyIdToInfo.isDefined){
               Seq(new CopyFieldsToInfo(qualTag = copyQualToInfo, filterTag = copyFilterToInfo, idTag = copyIdToInfo))
            } else {
              Seq()
            }
        ) ++ (
            (if(splitMultiAllelics){
                reportln("Creating multiallelic-split utility ... "+getDateAndTimeString,"debug");
                Seq[SVcfWalker](SSplitMultiAllelics(vcfCodes = vcfCodes, clinVarVariants = false, splitSimple = false));
              } else {
                Seq[SVcfWalker]();
              })
        ) ++ (
            (if(dropSymbolicAlleleLines){
                reportln("Creating symbolic allele filter ... "+getDateAndTimeString,"debug");
                Seq[SVcfWalker](new FilterSymbolicAlleleLines());
              } else {
                Seq[SVcfWalker]();
              })
        ) ++ (
            (if(dropSpanIndels){
                reportln("Creating symbolic allele filter ... "+getDateAndTimeString,"debug");
                Seq[SVcfWalker](new DropSpanIndels());
              } else {
                Seq[SVcfWalker]();
              })
        ) ++ (
            (if(dropVariantsWithNs){
                reportln("Creating filter to drop variants with Ns ... "+getDateAndTimeString,"debug");
                Seq[SVcfWalker](new DropNs());
              } else {
                Seq[SVcfWalker]();
              })
              //dropVariantsWithNs
        ) ++ (
            if(leftAlignAndTrim){
              if(genomeFA.isEmpty){
                error("ERROR: in order to left align and trim, you MUST specify a genome fasta file with the --genomeFA parameter");
              }
              (if(makeFirstBaseMatch){
                Seq(internalUtils.GatkPublicCopy.FixFirstBaseMismatch(genomeFa = genomeFA.get,windowSize = leftAlignAndTrimWindow.getOrElse(200)))
              } else {
                Seq[SVcfWalker]()
              }) ++ Seq[SVcfWalker](
                    //internalUtils.GatkPublicCopy.FixFirstBaseMismatch(genomeFa = genomeFA.get,windowSize = leftAlignAndTrimWindow.getOrElse(200)), useGatkLibCall = leftAlignAndTrimWindow.isEmpty
                    internalUtils.GatkPublicCopy.LeftAlignAndTrimWalker(genomeFa = genomeFA.get,windowSize = leftAlignAndTrimWindow.getOrElse(200), useGatkLibCall = false)
              );
            } else {
              Seq[SVcfWalker]();
            }
            
        ) ++ (
            if(convertROAOtoAD){
              Seq[SVcfWalker](new recodeROAOtoAD(roTag = "RO",aoTag = "AO", adTag = "AD", desc = "Allele depths, calculated from RO and AO tags."))
            } else {
              Seq[SVcfWalker]();
            }
        ) ++ (
            if(rmDup){
              Seq[SVcfWalker](new RemoveDuplicateLinesWalker())
            } else {
              Seq[SVcfWalker]();
            }
        ) ++ (
            if(findComplexVars){
              Seq[SVcfWalker](new internalUtils.GatkPublicCopy.FindComplexAlleles(genomeFA = genomeFA.get,idxTag = complexVarIdx.get, gtTag = calcStatGtTag, mergeTags=findComplexVarMergeInfoTags))
            } else {
              Seq[SVcfWalker]();
            }
            //FindComplexAlleles(genomeFA : String,idxTag : String,gtTag : String = "GT"
        )
        
        
    /*
(
            
          singleCallerVcfs match {
            case Some(scv) => {
              infileListInfix match {
                case Some(ifi) => {
                  val scm = singleCallerMaster match {
                    case Some(ss) => Some(ss.head);
                    case None => None
                  }
                  if( ! scv.forall( _.contains('|') )) error("Error: infileListInfix is set, the singleCallerVcfs parameters must all contain a bar symbol (\"|\")");
                  val scvOut = scv.map{ s => {
                    val (prefix,suffix) = (s.split("\\|").head,s.split("\\|")(1));
                    getLinesSmartUnzip(ifi).map{ infix => {
                      prefix+infix+suffix
                    }}.mkString("|");
                  }}
                  reportln("Creating Ensemble Merger... "+getDateAndTimeString,"debug");
                  Seq(
                      FixEnsemblMerge2(
                         inputVCFs = scvOut,
                         inputVcfTypes = singleCallerVcfNames.get,
                         masterCaller = scm,
                         summaryFile = None
                      )
                  );
                }
                case None => {
                  val scm = singleCallerMaster match {
                    case Some(ss) => Some(ss.head);
                    case None => None
                  }
                  reportln("Creating Ensemble Merger... "+getDateAndTimeString,"debug");
                  Seq(
                      FixEnsemblMerge2(
                         inputVCFs = scv,
                         inputVcfTypes = singleCallerVcfNames.get,
                         masterCaller = scm,
                         summaryFile = None
                      )
                  );
                }
              }
            }
            case None => Seq();
          }
          
        ) ++  (
          singleCallerVcfsNew match {
            case Some(scv) => {
              val scvList = infileListInfix match {
                case Some(ifi) => {
                  if( ! scv.forall( _.contains('|') )) error("Error: infileListInfix is set, the singleCallerVcfs parameters must all contain a bar symbol (\"|\")");
                  val scvOut = scv.map{ s => {
                    val (prefix,suffix) = (s.split("\\|").head,s.split("\\|")(1));
                    getLinesSmartUnzip(ifi).map{ infix => {
                      prefix+infix+suffix
                    }}.mkString("|");
                  }}
                  reportln("Creating Ensemble Merger... "+getDateAndTimeString,"debug");
                  scvOut;
                }
                case None => {
                  reportln("Creating Ensemble Merger... "+getDateAndTimeString,"debug");
                  scv
                }
              }
              if(genomeFA.isEmpty){
                error("Cannot perform proper merge without a genome fasta!");
              }
              
              Seq(
                  FixEnsemblMerge3(
                     inputVCFs = scvList,
                     inputVcfTypes = singleCallerVcfNames.get,
                     masterCaller = singleCallerMaster.getOrElse(List[String]()),
                     summaryFile = None,
                     genomeFA = genomeFA
                  )
              );
            }
            case None => Seq();
          }
        ) ++ 
     */
        
        
    val postWalkers : Seq[SVcfWalker] = (
            
            snpSiftAnnotate.toSeq.map{ssa => {
              new SnpSiftAnnotater(ssa.split(",",2)(0),ssa.split(",",2)(1))
            }}
            
        ) ++ (
            
            snpSiftDbnsfp.toSeq.map{ssa => {
              new SnpSiftDbnsfp(ssa.split(",",2)(0),ssa.split(",",2)(1))
            }}
            
        ) ++ (
            
            snpEffAnnotate.toSeq.map{ssa => {
              new SnpEffAnnotater(ssa.split(",",2)(0),ssa.split(",",2)(1),ssa.split(",",2)(0) )
            }}
            
        ) ++ (
            addLocalGcInfo.map{ ss => {
              if(genomeFA.isEmpty){
                error("in order to perform local GC info add, you must include the genomeFA parameter");
              }
              val scells = ss.split(",");
              val prefix = scells(0);
              val wins = scells(1).split("[|]").map{ string2int(_) }.toSeq
              val digits = scells.lift(2).map{ string2int(_) }
              localGcInfoWalker(tagPrefix = prefix, windows = wins, genomeFa = genomeFA.get, roundDigits = digits);
            }}
            //addLocalGcInfo.map{ss => {
              //val 
              //new SnpSiftAnnotater(ssa.split(",",2)(0),ssa.split(",",2)(1))
            //}}
        ) ++ (
            //addWiggleDepthWalker(wigFile : String, tag : String, desc : String)
            addWiggleAnnotation.map{ ss => {
              val scells = ss.split("[|]");
              if(scells.length != 3){
                error("addWiggleAnnotation error: must have 3 elements: tagID, desc, wig");
              }
              val (tagID,desc,wigfile) = ((scells(0),scells(1),scells(2)));
              new addWiggleDepthWalker(wigFile = wigfile, tag = tagID, desc = desc);
            }}
            
        ) ++ (
          genoFilter match {
            case Some(gf) => {
              reportln("Creating GT Filter ... "+getDateAndTimeString,"debug");
              
              val ft = filterTag.getOrElse(OPTION_TAGPREFIX+"FILTER_GT")
              val ungt = unfilteredGtTag.getOrElse("GT_PREFILT")
              val noRawGt = unfilteredGtTag.isEmpty
              //groupFile : Option[String] = None, groupList : Option[String] = None, superGroupList  : Option[String] = None
              Seq(
              FilterGenotypesByStat( 
                 filter = gf,
                 filterTag = ft,
                 gtTag = filterInputGtTag,
                 rawGtTag = ungt,
                 noRawGt = noRawGt,
                 newGTTag = filterOutputGtTag,
                 groupFile = groupFile, 
                 groupList = None, 
                 superGroupList  = superGroupList
              ))
            }
            case None => {
              Seq()
            }
          }
        ) ++ (
          convertGtToMatrix.toSeq.map{ cgtm => {
            ConvertToMatrixWalker(gtTags = cgtm, gtTagPrefix = "MATRIX_");
          }}
        ) ++ (
          if(convertMatrixToGt) {
            Seq[SVcfWalker](ConvertToGtWalker(gtTagPrefix = "MATRIX_"));
          } else {
            Seq[SVcfWalker]();
          }
        ) ++ (
            
            addBedTags match {
              case Some(bt) => {
                bt.toSeq.map{ btag => {
                  val bt = btag.split(":")
                  if(bt.length != 5) error("Each element in addBedTags must have 5 colon-delimited parts: the tagID, the distance, the type, the description, and the bedfile.");
                  val (t,bufferDist,style,desc,bedFile) = (bt(0),bt(1),bt(2),bt(3),bt(4));
                  reportln("bedInfo: (\""+bt.mkString("\",\"")+"\")","note");
                  if(! Set("+","-","s").contains(style)) error("Element 3 of addBedTags must be the tag style, which should be one of \"+\", \"-\", or \"s\". Found:\""+style+"\"\n   For bt: \""+btag+"\""+
                      ""+
                      "");
                  try {
                    string2int(bufferDist)
                  } catch {
                    case e : NumberFormatException => {
                      error("Element 2 of addBedTags must be an integer.")
                      throw e;
                    }
                  }
                  
                  if((new File(bedFile+".tbi")).exists()){
                    AddIdxBedFile(bedFile = bedFile, bedIdx = bedFile + ".tbi", tag =t, bufferDist = string2int(bufferDist), desc =desc, chromList = chromList,style=style);
                  } else {
                    AddTxBedFile(bedFile = bedFile, tag =t, bufferDist = string2int(bufferDist), desc =desc, chromList = chromList,style=style);
                  }
                }}
              }
              case None => Seq[SVcfWalker]();
            }
        ) ++ (
            homopolymerRunStats.map{ hrs => {
              val bt = hrs.split(":",-1)
              if(bt.length == 0 || bt.length > 2) error("homopolymerRunStats must have 1-2 colon-delimited parts: threshold and tagIDinfix.");
              if(genomeFA.isEmpty) error("homopolymerRunStats requires genomeFA!")
              val tagPrefix = bt.lift(1).getOrElse("")
              val lenThreshold = string2int(bt(0));
              HomopolymerRunStats(tagPrefix=tagPrefix,genomeFa=genomeFA.get, lenThreshold = lenThreshold);

            }}
        ) ++ (
            addContextBases.map{ hrs => {
              val bt = hrs.split(":",-1)
              if(bt.length == 0 || bt.length > 2) error("AddContextBases must have 1 or 2 colon-delimited parts: threshold and tagid.");
              if(genomeFA.isEmpty) error("AddContextBases requires genomeFA!")
              val tagPrefix = bt.lift(1).getOrElse("")
              val lenThreshold = string2int(bt(0));
              AddContextBases(tagPrefix=tagPrefix,genomeFa=genomeFA.get, len = lenThreshold);
            }}
            
        ) ++ (
            if(inputSavedTxFile.isDefined | gtffile.isDefined){
                reportln("Creating TX utility ... "+getDateAndTimeString,"debug");
                Seq(AddTxAnnoSVcfWalker(
                    gtffile =gtffile, 
                    genomeFA =genomeFA, 
                    summaryWriter =summaryWriter,
                    txInfoFile = txInfoFile,
                    addStopCodon =addStopCodon,
                    inputSavedTxFile =inputSavedTxFile,
                    outputSavedTxFile =outputSavedTxFile,
                    chromList =chromList,
                    txToGeneFile = txToGeneFile,
                    geneVariantsOnly = geneVariantsOnly,
                    bufferSize = bufferSize,
                    //addBedTags = addBedTags,
                    addBedTags = None,
                    vcfCodes =vcfCodes,
                    geneList = geneList,
                    txTypes = txTypes
                    ), 
                    new internalUtils.CalcACMGVar.AddMoreGeneAnno(txToGeneFile =txToGeneFile, refSeqFile =addCanonicalTags)
                )
            } else {
                Seq()
            }
          
         ) ++ (
             if(addTxSummaryInfo){
                Seq( new internalUtils.CalcACMGVar.AddMoreGeneAnno(txToGeneFile =txToGeneFile, refSeqFile =addCanonicalTags) )
             } else {
                Seq()
             }
            
         /*) ++ (
            if(groupFile.isEmpty || splitAlleleGroupCounts){
              Seq[SVcfWalker]();
            } else {
              reportln("Creating groupInfo utility ... "+getDateAndTimeString,"debug");
              //              val ft = filterTag.getOrElse(OPTION_TAGPREFIX+"FILTER_GT")
              //val ungt = unfilteredGtTag.getOrElse("GT_PREFILT")
              Seq[SVcfWalker](SAddGroupInfoAnno(groupFile = groupFile, 
                                                groupList = None, 
                                                superGroupList  = superGroupList, 
                                                chromList = chromList,
                                                tagFilter = filterTag,
                                                tagPreFiltGt = unfilteredGtTag,
                                                GTTag = calcStatGtTag))
            }
        ) ++ (
            if(splitMultiAllelics && ( ! leftAlignAndTrim ) ){
              reportln("Creating multiallelic-split utility ... "+getDateAndTimeString,"debug");
              Seq[SVcfWalker](SSplitMultiAllelics(vcfCodes = vcfCodes, clinVarVariants = false, splitSimple = false));
            } else {
              Seq[SVcfWalker]();
            }*/
         ) ++ (
            if((! noGroupStats) && ( groupFile.isDefined || superGroupList.isDefined )){
              reportln("Creating groupInfo utility ... "+getDateAndTimeString,"debug");
              val tagInfix = calcRunInfix.getOrElse("");
              val vcfCodes : VCFAnnoCodes = VCFAnnoCodes(CT_INFIX = tagInfix)
              Seq[SVcfWalker](SAddGroupInfoAnno(groupFile = groupFile, 
                                                groupList = None, 
                                                superGroupList  = superGroupList, 
                                                chromList = chromList,
                                                noMultiAllelics= splitAlleleGroupCounts || splitMultiAllelics,
                                                tagFilter = filterTag,
                                                tagPreFiltGt = unfilteredGtTag,GTTag = calcStatGtTag,
                                                vcfCodes = vcfCodes))
            } else {
              Seq[SVcfWalker]();
            }
        ) ++ (
            addSampCountWithMultVector.map{ asc => asc.split("[|]")}.map{ cc => {
              val tagID = cc(0);
              val gtTag = calcStatGtTag;
              val desc = cc(1);
              val vectorFile = cc(2);
              new SAddSampCountWithMultVector(tagID =tagID, gtTag = gtTag, desc = desc, vectorFile = vectorFile);
              //SAddSampCountWithMultVector(tagID : String, gtTag : String, desc : String, vectorFile : String)
            }}
            
        ) ++ (
          dbnsfpFile match {
            case Some(df) => {
             reportln("Creating DBNSFP utility ... "+getDateAndTimeString,"debug");
             Seq[SVcfWalker](SRedoDBNSFPannotation(
               dbnsfpfile = df,
               chromStyle = "hg19", 
               chromList = chromList,
               posFieldTitle = "pos(1-coor)",
               chromFieldTitle = "chr",
               altFieldTitle = "alt",
               singleDbFile = false,
               dbFileDelim = "\t",
               tagPrefix = OPTION_TAGPREFIX+"dbNSFP_",
               dropTags = None,
               keepTags = dbnsfpTags
             ))
            }
            case None => Seq[SVcfWalker]();
          }
        ) ++ ( 
          addInfoVcfs match {
            case Some(infoVcfs) => {
              
              infoVcfs.toSeq.map{ v => {
                val cells =  v.split(":")
                if(cells.length != 3 && cells.length != 2) error("each element of addInfoVcfs must have 2-3 elements separated by colons!");
                val vcfTag = cells(0);
                val vcfFile = cells(1);
                val tagSet : Option[List[String]]= if(cells.isDefinedAt(2)) Some(cells(2).split("\\|").toList) else None
                reportln("Creating addVCFtags ("+vcfTag+") utility ... "+getDateAndTimeString,"debug");
                mergeSecondaryVcf(inputVCF = vcfFile, inputVcfTag = vcfTag, inputVcfName = vcfTag, getTags = tagSet, tagPrefix = "");
              }}
            }
            case None => {
              Seq[SVcfWalker]();
            }
          }
        ) ++ (
            if(addSummaryCLNSIG){
              reportln("Creating clinVar utility ... "+getDateAndTimeString,"debug");
              Seq[SVcfWalker](SAddSummaryCln(vcfCodes = vcfCodes));
            } else {
              Seq[SVcfWalker]();
            }
        ) ++ (
            if(addCanonicalTags.isDefined){
              reportln("Creating AddCanonicalInfo utility ... "+getDateAndTimeString,"debug");
              Seq[SVcfWalker](SAddCanonicalInfo(canonicalTxFile = addCanonicalTags.get));
            } else {
              Seq[SVcfWalker]();
            }
        ) ++ (

            addDepthStats match {
              case Some(depthStatString) => {
                val depthStatCells = depthStatString.split(",")
                val (tagAD) = depthStatCells(0)
                val tagSingleCallerAlles = depthStatCells.lift(1).flatMap{s => if(s == ".") None else Some(s) }
                val tagDP = depthStatCells.lift(2).flatMap{s => if(s == ".") None else Some(s) }
                  //if(depthStatCells.isDefinedAt(2) && depthStatCells(2) != ".") Some(depthStatCells(2)) else None;
                val tagGT = calcStatGtTag
                val outputTagPrefix = depthStatCells.lift(3).getOrElse(OPTION_TAGPREFIX+"STAT_");
                  //if(depthStatCells.isDefinedAt(3)) depthStatCells(3) else OPTION_TAGPREFIX+"STAT_";
                
                Seq[SVcfWalker](AddStatDistributionTags(tagAD = Some(tagAD), 
                                            tagGT = tagGT, tagDP = tagDP,
                                            tagSingleCallerAlles = tagSingleCallerAlles,
                                            outputTagPrefix=outputTagPrefix,
                                            variantStatExpression = variantStatExpression
                                            
                ))
              }
              case None => {
                Seq[SVcfWalker]()
              }
            }
        ) ++ (
            
            // AddAltSampLists(tagGT : String = "GT",
            //                 outputTagPrefix : String = OPTION_TAGPREFIX+"SAMPLIST_",
            //                 printLimit : Int = 25
            //                )
             
            addSampTag match {
              case Some(st) => {
                val stCells = st.split(",");
                val printLimit = stCells.head.toInt;
                val tagPrefix = if(stCells.isDefinedAt(1)) stCells(1) else OPTION_TAGPREFIX+"SAMPLIST_";
                
                Seq[SVcfWalker](
                    AddAltSampLists(tagGT = calcStatGtTag,
                             outputTagPrefix  = tagPrefix,
                             printLimit = printLimit,
                             groupFile = groupFile, groupList = None, superGroupList  = superGroupList
                            )
                )
              }
              case None => {
                Seq[SVcfWalker]()
              }
            }
         ) ++ (
             annTag match {
               case Some( at ) => {
                 Seq[SVcfWalker](
                 new SnpEffInfoExtract(tagID = at, 
                     tagPrefix = snpEffTagPrefix.getOrElse(at + "EX_"),
                     geneList = ensGeneList,
                     snpEffBiotypeKeepList = snpEffBiotypeKeepList,
                     snpEffEffectKeepList = snpEffEffectKeepList,
                     snpEffWarningDropList = snpEffWarningDropList,
                     snpEffKeepIdx = snpEffKeepIdx,
                     snpEffVarExtract = snpEffVarExtract,
                     geneListTagInfix = snpEffGeneListTagInfix,
                     snpEffBiotypeIdx=snpEffBiotypeIdx,
                     snpEffWarnIdx=snpEffWarnIdx,
                     snpEffFieldLen=snpEffFieldLen,
                     snpEffFields=snpEffFields
                     )
                 );
               }
               case None => {
                 Seq[SVcfWalker]()
               }
             }
                                                         /*
                         *snpEffBiotypeIdx : Int = 7,
                          snpEffWarnIdx : Int = 15,
                          snpEffFieldLen : Int = 16,
                          snpEffFields : Option[List[String]] = None
                         */
             
        ) ++ (
            ctrlAlleFreqKeys.toSeq.map{ cafk => {
                val cells = cafk.split(":");
                val mafTags = cells.last.split(",");
                val maxMafString = if(cells.length == 2) cells.head else OPTION_TAGPREFIX+"ACMG_ctrlAFMAX"
                AddMafSummary(mafTags.toList, maxAfTag = maxMafString)
            }}
        ) ++ (
            addRatioTag.map{ rt => {
              val cells = rt.split(",");
              val newTag = cells(0);
              val nTag = cells(1);
              val dTag = cells(2);
              val digits = cells.lift(3).map{string2int(_)}.getOrElse(4);
              val desc = cells.lift(4)
              
              new AddRatioTag(newTag=newTag,nTag=nTag,dTag=dTag,digits = digits,desc=desc);
              //AddRatioTag(newTag : String, nTag : String, dTag : String, digits : Int = 4, desc : Option[String] = None )
            }}
            
       ) ++ ({
            /*
             *                 varMatchDbName : List[String] = List[String](),
                varMatchDbFile : List[String] = List[String](),
                varMatchIdTag : List[String] = List[String](),
                varMatchPathoExpression : List[String] = List[String](),
                varMatchBenignExpression : List[String] = List[String](),
                varMatchMetadataList : List[String] = List[String]()
                varMatchGeneList : List[String] = List[String]();
                
             */
            if(varMatchDbName.length > 0){
              if(varMatchDbName.length == varMatchDbFile.length && 
                 varMatchDbName.length == varMatchPathoExpression.length && 
                 varMatchDbName.length == varMatchIdTag.length){
                val varMatchPathoExprMap  = varMatchPathoExpression.map{_.split("\\|")}.map{x => (x(0),x(1))}.toMap;
                val varMatchBenignExprMap = varMatchBenignExpression.map{_.split("\\|")}.map{x => (x(0),x(1))}.toMap;
                val varMatchMetadataMap   = varMatchMetadataList.map{_.split("\\|")}.map{x => (x(0),x(1).split(",").toSeq)}.toMap;
                val varMatchIdTagMap      = varMatchIdTag.map{_.split("\\|")}.map{x => (x(0),x(1))}.toMap;

                
                
                var varMatchGeneSet = if(varMatchGeneList.isEmpty){ None } else { Some(varMatchGeneList.toSet) }
                val dbdata : Seq[(String,String,String,Option[String],Option[String],Seq[String])] = 
                  varMatchDbName.zip(varMatchDbFile).map{ case (dbName,dbFile) => {
                    (dbName,dbFile,varMatchIdTagMap(dbName),
                        varMatchPathoExprMap.get(dbName),
                        varMatchBenignExprMap.get(dbName),
                        varMatchMetadataMap.getOrElse(dbName,List[String]())
                        )
                  }}
                Seq[SVcfWalker](
                  new internalUtils.CalcACMGVar.AddDatabaseMatching(txToGeneFile =txToGeneFile , refSeqFile =addCanonicalTags,
                            chromList = chromList, 
                            geneSet = varMatchGeneSet,
                            dbdata = dbdata,
                               //dbName,dbFile,idTag,pathoExpression,benExpression,metaDataTags
                            tagInfix = ""
                            )
                )
              } else {
                error("varMatchDbName, varMatchDbFile, and varMatchPathoExpression must all have the same length!");
                 Seq[SVcfWalker]()   
              }
            } else {
              Seq[SVcfWalker]()
            }
            
       }) ++ (
            clinVarVcf match {
              case Some(cvVcf) => {
              
              //val clinVarVcf = None
              //val hgmdVarVcf = None
              
              
              //val toleranceFile : Option[String] = None
              //val BA1_AF : Double = 0.0
              //val PM2_AF : Double = 0.0

              val includeGenewise = true
              val hgmdPathogenicClasses = List("DM")
              //val txToGeneFile : Option[String] = txToGeneFile
              //val cvVcf : String = clinVarVcf.get;
              val alleFreqKeys = ctrlAlleFreqKeys.lastOption.getOrElse{
                warning("WARNING: cannot perform pathogenicity rating without allele frequencies!","NO_ALLELE_FREQS_FOUND",10);
                ""
              }
              
              Seq[SVcfWalker](internalUtils.CalcACMGVar.AssessACMGWalker(
                   chromList = chromList,
                   clinVarVcf = cvVcf,
                   txToGeneFile = txToGeneFile,
                   ctrlAlleFreqKeys = alleFreqKeys.split(",").toList,
                   toleranceFile = toleranceFile,
                   domainFile = None,
                   conservedElementFile = None,
                   BA1_AF =BA1_AF,
                   PM2_AF = PM2_AF,
                   rmskFile = None,
                   refSeqFile = addCanonicalTags,
                   inSilicoParams  = inSilicoParams,
                   inSilicoMergeMethod  = "smart",
                   hgmdVarVcf = hgmdVarVcf,
                   lowMapBed = None,
                   pseudoGeneGTF  = None,
                   hgmdPathogenicClasses  = hgmdPathogenicClasses,
                   includeGenewise = true,
                   
                   locusRepetitiveTag  = locusRepetitiveTag,
                   locusDomainTag   = locusDomainTag,
                   locusConservedTag   = locusConservedTag,
                   locusMappableTag   = locusMappableTag,
                   locusPseudoTag  = locusPseudoTag
                 //domainSummaryFile : Option[String] = None
                ))
            }
              case None => Seq[SVcfWalker]()
            }
        ) ++ (
            duplicateTag.toSeq.map{ dt => {
              new DuplicateStats(dt)
            }}
        ) ++ ({
    //getTagVariantsFunction tagVariantsFunction
    //getTagVariantsExpression tagVariantsExpression
    //getMergeBooleanTags mergeBooleanTags


             val tvf : Vector[(Double,SVcfWalker)] = tagVariantsFunction.filter( _.startsWith("ORDER=-") ).toVector.map{ ss => {
               if(! ss.contains("|")) error("Invalid tagVariantsFunction entry: \""+ss+"\"")
               val NORD = ss.drop(6);
               val p = string2double( NORD.split("[|]",2)(0) )
               ((p, NORD.split("[|]",2)(1) ))
             }}.sortBy{case (p,ss) => p}.map{ case (p,ss) => {
               ((p, getTagVariantsFunction(ss)))
             }}
             val tve : Vector[(Double,SVcfWalker)] = tagVariantsExpression.filter( _.startsWith("ORDER=-") ).toVector.map{ ss => {
               if(! ss.contains("|")) error("Invalid tagVariantsExpression entry: \""+ss+"\"")
               val NORD = ss.drop(6);
               val p = string2double( NORD.split("[|]",2)(0) )
               ((p, NORD.split("[|]",2)(1) ))
             }}.sortBy{case (p,ss) => p}.map{ case (p,ss) => {
               ((p,getTagVariantsExpression(ss)))
             }}
             val mbt : Vector[(Double,SVcfWalker)] = mergeBooleanTags.filter( _.startsWith("ORDER=-") ).toVector.map{ ss => {
               if(! ss.contains("|")) error("Invalid mergeBooleanTags entry: \""+ss+"\"")
               val NORD = ss.drop(6);
               val p = string2double( NORD.split("[|]",2)(0) )
               ((p, NORD.split("[|]",2)(1) ))
             }}.sortBy{case (p,ss) => p}.map{ case (p,ss) => {
               ((p,getMergeBooleanTags(ss)))
             }}
             val ssq = (tvf ++ tve ++ mbt).sortBy{ case (p,w) => p}.map{_._2}
             if(ssq.length > 0){
               reportln("-----Chaining "+ssq.length+" negative-ordered tag function walkers:","note")
             }
             ssq.foreach( sss => {
               reportln("     Adding: "+sss.walkerName,"note")
             })
             ssq
        }) ++ ({
             val tvf : Vector[SVcfWalker] = tagVariantsFunction.filter( ! _.startsWith("ORDER=") ).toVector.map{ ss => {
               ((0, ss ))
             }}.map{ case (p,ss) => {
               getTagVariantsFunction(ss);
             }}
             val tve : Vector[SVcfWalker] = tagVariantsExpression.filter( ! _.startsWith("ORDER=") ).toVector.map{ ss => {
               ((0, ss ))
             }}.map{ case (p,ss) => {
               getTagVariantsExpression(ss);
             }}
             val mbt : Vector[SVcfWalker] = mergeBooleanTags.filter( ! _.startsWith("ORDER=") ).toVector.map{ ss => {
               ((0, ss ))
             }}.map{ case (p,ss) => {
               getMergeBooleanTags(ss);
             }}
             val ssq = (tvf ++ tve ++ mbt)
             if(ssq.length > 0){
               reportln("-----Chaining "+ssq.length+" default-ordered tag function walkers:","note")
             }
             ssq.foreach( sss => {
               reportln("     Adding: "+sss.walkerName,"note")
             })
             ssq
        }) ++ ({
             val tvf : Vector[(Double,SVcfWalker)] = tagVariantsFunction.filter( x => x.startsWith("ORDER=") && (!x.startsWith("ORDER=-"))  ).toVector.map{ ss => {
               if(! ss.contains("|")) error("Invalid tagVariantsFunction entry: \""+ss+"\"")
               val NORD = ss.drop(6);
               val p = string2double( NORD.split("[|]",2)(0) )
               ((p, NORD.split("[|]",2)(1) ))
             }}.sortBy{case (p,ss) => p}.map{ case (p,ss) => {
               ((p, getTagVariantsFunction(ss)))
             }}
             val tve : Vector[(Double,SVcfWalker)] = tagVariantsExpression.filter(  x => x.startsWith("ORDER=") && (!x.startsWith("ORDER=-"))   ).toVector.map{ ss => {
               if(! ss.contains("|")) error("Invalid tagVariantsExpression entry: \""+ss+"\"")
               val NORD = ss.drop(6);
               val p = string2double( NORD.split("[|]",2)(0) )
               ((p, NORD.split("[|]",2)(1) ))
             }}.sortBy{case (p,ss) => p}.map{ case (p,ss) => {
               ((p,getTagVariantsExpression(ss)))
             }}
             val mbt : Vector[(Double,SVcfWalker)] = mergeBooleanTags.filter(  x => x.startsWith("ORDER=") && (!x.startsWith("ORDER=-")) ).toVector.map{ ss => {
               if(! ss.contains("|")) error("Invalid mergeBooleanTags entry: \""+ss+"\"")
               val NORD = ss.drop(6);
               val p = string2double( NORD.split("[|]",2)(0) )
               ((p, NORD.split("[|]",2)(1) ))
             }}.sortBy{case (p,ss) => p}.map{ case (p,ss) => {
               ((p,getMergeBooleanTags(ss)))
             }}
             val ssq = (tvf ++ tve ++ mbt).sortBy{ case (p,w) => p}.map{_._2}
             if(ssq.length > 0){
               reportln("-----Chaining "+ssq.length+" positive-ordered tag function walkers:","note")
             }
             ssq.foreach( sss => {
               reportln("     Adding: "+sss.walkerName,"note")
             })
             ssq
             /*
        }) ++ ({
            //AddFuncTag(func : String, newTag : String, paramTags : Seq[String], digits : Option[Int] = None, desc : Option[String] = None )
            
            tagVariantsFunction.map{ ftString => {
              val cells = ftString.split("[|]")
              val tagID = cells(0);
              val desc  = cells(1);
              val funcString = cells(2);
              val paramTags = cells.lift(3).map{_.split(",").toSeq}.getOrElse(Seq[String]());
              val outDigits = cells.lift(4).map{ _.toInt }
              
              new AddFuncTag(func = funcString, newTag=tagID, paramTags=paramTags, digits = outDigits, desc = Some(desc));
            }}

        }) ++ ({
            /*
             * also requires: groupFile, superGroupList, geneList
             */
          

            tagVariantsExpression.toSeq.map{ exprStringRaw => exprStringRaw.split("\\|").toVector.map{s => s.trim()}}.map{ cells => {
                  if(cells.length > 5){
                    error("Each comma-delimited element of parameter tagVariantsExpression must have three bar-delimited parts: tag, desc, and expression. Found cells=["+cells.mkString("|")+"]");
                  }
                  if(cells.head.startsWith("MODE=")){
                    val mode = cells.head.drop(5);
                    if(mode == "VAR"){
                      if(cells.length != 4){
                        error("for MODE=VAR, tagVariantsExpression must be a |-delimited list of length 4.")
                      }
                      reportln("Creating tagging utility, MODE=VAR. "+getDateAndTimeString,"debug");
                      val (tagID,tagDesc,expr) = (cells(1),cells(2),cells(3));
                      VcfExpressionTag(expr = expr, tagID = tagID, tagDesc = tagDesc);
                    } else if(mode == "GENELIST"){
                      reportln("Creating tagging utility, MODE=GENELIST. "+getDateAndTimeString,"debug");
                      val (tagID,tagDesc,expr) = (cells(1),cells(2),cells(3));
                      val geneTagString = cells.lift(4);
                      val subGeneTagString = cells.lift(5);
                      VcfExpressionTag(expr = expr, tagID = tagID, tagDesc = tagDesc, 
                                     geneTagString = geneTagString, subGeneTagString = subGeneTagString, geneList = geneList);
                    } else if(mode.startsWith("GT")){
                      reportln("Creating tagging utility, MODE="+mode+". "+getDateAndTimeString,"debug");
                      val (tagID,tagDesc,expr) = (cells(1),cells(2),cells(3));
                      //val styleOpt = cells.lift(4);
                      VcfGtExpressionTag( expr=expr,tagID=tagID,tagDesc=tagDesc,style = mode,                  
                                          groupFile = groupFile, groupList = None, superGroupList  = superGroupList )
                    } else {
                      error("UNKNOWN/INVALID tagVariantsExpression MODE:\""+mode+"\"!")
                      new PassThroughSVcfWalker()
                    }
                  } else if(cells.length == 3){
                    val (tagID,tagDesc,expr) = (cells(0),cells(1),cells(2));
                    reportln("Creating Variant tagging utility ("+cells(0)+") ... "+getDateAndTimeString,"debug");
                     VcfExpressionTag(expr = expr, tagID = tagID, tagDesc = tagDesc);
                  } else {
                    reportln("Creating Variant genelist-tagging utility ("+cells(0)+") ... "+getDateAndTimeString,"debug");
                    val (tagID,tagDesc,expr) = (cells(0),cells(1),cells(2));
                    val geneTagString = cells.lift(3);
                    val subGeneTagString = cells.lift(4);
                    VcfExpressionTag(expr = expr, tagID = tagID, tagDesc = tagDesc, 
                                     geneTagString = geneTagString, subGeneTagString = subGeneTagString, geneList = geneList);
                  }
                }}

            
        }) ++ ({

            
            mergeBooleanTags.map{ mbt => {
              val cells : Array[String] = mbt.split(",")
              val tagID : String = cells(0);
              val mergeTags : List[String] = cells(1).split( "[|]" ).toList
              val tagNames : Option[List[String]] = cells.lift(2).map{ cc => cc.split("[|]").toList }
              
              new MergeBooleanTags(tagID = tagID, mergeTags = mergeTags, tagNames = tagNames)
            } }
            */
             
        }) ++ (
            dropVariantsExpression match {
              case Some(dve) => {
                reportln("Creating Variant Filter utility ... "+getDateAndTimeString,"debug");
                Seq[SVcfWalker](VcfExpressionFilter(filterExpr = dve))
              }
              case None => {
                Seq[SVcfWalker]()
              }
            }
        ) ++ (
                //calcBurdenCounts : List[String] = List[String](),
               // burdenCountsFile : Option[String] = None
            
            calcBurdenCounts.map{ cbc => {
              new generateBurdenMatrix(cbc, burdenWriter.get)
            }}
        ) ++ (
            if(addVariantIdx.isDefined){
                val (tagString,idxPrefix) = (addVariantIdx.get.split(",").head, addVariantIdx.get.split(",").lift(1) )
                Seq[SVcfWalker](new AddVariantIdx(tag = tagString,idxPrefix = idxPrefix))
            } else {
                Seq[SVcfWalker]()
            }
        ) ++ (
            if(convertToStandardVcf){
                Seq[SVcfWalker](new StdVcfConverter(thirdAlleleChar = thirdAlleleChar))
            } else if( splitMultiAllelicsNoStarAlle) {
                Seq[SVcfWalker](new StdVcfConverter(cleanHeaderLines = false, 
                        cleanInfoFields  = false, 
                        cleanMetaData  = false,
                        collapseStarAllele  = true,
                        thirdAlleleChar = thirdAlleleChar))
            } else if(thirdAlleleChar.isDefined){
              Seq[SVcfWalker](new StdVcfConverter(cleanHeaderLines = false, 
                        cleanInfoFields  = false, 
                        cleanMetaData  = false,
                        collapseStarAllele  = true, 
                        thirdAlleleChar = thirdAlleleChar))
            } else {
                Seq[SVcfWalker]()
            }
        ) ++ (
            if(outputDropInfoTags.isDefined || outputDropGenoTags.isDefined || outputKeepInfoTags.isDefined || outputKeepGenoTags.isDefined || outputKeepSamples.isDefined){
              Seq[SVcfWalker](
                  FilterTags(
                     keepGenotypeTags = outputKeepGenoTags,
                     dropGenotypeTags = outputDropGenoTags.getOrElse(List[String]()),
                     keepInfoTags = outputKeepInfoTags,
                     dropInfoTags = outputDropInfoTags.getOrElse(List[String]()),
                     dropAsteriskAlleles = false,
                     keepSamples = outputKeepSamples,
                     dropSamples = List[String](),
                     alphebetizeHeader = false,
                     renameInfoTags = None
                  )
              )
            } else {
              Seq[SVcfWalker]()
            }
        ) ++ (
            if(dropGenotypeData || addDummyGenotypeColumn){
                Seq[SVcfWalker](StripGenotypeData(addDummyGenotypeColumn=addDummyGenotypeColumn))
            } else {
                Seq[SVcfWalker]()
            }
        ) ++ (
            if(alphebetizeHeader){
                Seq[SVcfWalker](AlphebetizeHeader())
            } else {
                Seq[SVcfWalker]()
            }
        ) ++ ( 
            if(debugMode){
              Seq[SVcfWalker]( PassThroughSVcfWalker("outputVCF") )
            } else {
              Seq[SVcfWalker]()
            }
        )
        
        
            /*
             --varMatch
               --varMatchDbName
               --varMatchDbFile
               --varMatchPathoExpression
               --varMatchBenignExpression
               --varMatchMetadataList
            AddDatabaseMatching(txToGeneFile =txToGeneFile , refSeqFile =addCanonicalTags,
                            chromList = chromList, 
                            geneSet : Option[Set[String]],
                            dbdata : Seq[(String,String,String,Option[String],Option[String],Seq[String])],
                               //dbName,dbFile,idTag,pathoExpression,benExpression,metaDataTags
                            tagInfix : String = ""
                            
                            )
            */
        
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
      
    if(runEnsembleMerger){
      val preWalker : SVcfWalker = chainSVcfWalkers(initWalkers);
      
      if( ! vcffile.contains(';') ) error("Error: runEnsembleMerger is set, the infile parameter must contain semicolons (\";\")");
      val vcfList = vcffile.split(";");
      val (iterSeq,headerSeq) = vcfList.toSeq.zipWithIndex.map{ case (vf,idx) => {
          val (infiles,infixes) : (String,Vector[String]) = infileListInfix match {
            case Some(ili) => {
              val (infilePrefix,infileSuffix) = (vf.split("\\|").head,vf.split("\\|")(1));
              val infixes = getLinesSmartUnzip(ili).toVector
              (infixes.map{ infix => infilePrefix+infix+infileSuffix }.mkString(","), infixes)
            }
            case None => {
              (vf,Vector());
            }
          }
          val ifl = infileListInfix match {
            case Some(ifi) => false;
            case None => infileList;
          }
          val (vcIterRaw, vcfHeaderRaw) = getSVcfIterators(infiles,chromList=chromList,numLinesRead=numLinesRead,inputFileList = ifl, withProgress = idx == 0, infixes = infixes);
          val (vcIter,vcfHeader) = preWalker.walkVCF(vcIterRaw,vcfHeaderRaw);
          val vcIterBuf = vcIter.buffered;
          (vcIterBuf,vcfHeader);
      }}.unzip;
      
      val inputNames = singleCallerVcfNames.getOrElse(headerSeq.indices.map{"C"+_.toString});
      val (ensIter,ensHeader) = ensembleMergeVariants(iterSeq,headerSeq,inputVcfTypes = singleCallerVcfNames.getOrElse(headerSeq.indices.map{"C"+_.toString}),
                                                        genomeFA = genomeFA,windowSize = 200);
      val finalWalker : SVcfWalker = chainSVcfWalkers(Seq[SVcfWalker](
          new EnsembleMergeMetaDataWalker(inputVcfTypes = singleCallerPriority.getOrElse(inputNames),
                                          decision = ensembleGenotypeDecision)
          /*
                                    simpleMergeInfoTags : Seq[String] = Seq[String](),
                                    simpleMergeFmtTags : Seq[String] = Seq[String]("GQ|Final Ensemble genotype quality score"),
                                    gtStyleFmtTags : Seq[String] = Seq[String]("GT|Final ensemble genotype"),
                                    adStyleFmtTags : Seq[String] = Seq[String]("AD|Final ensemble allele depths"),
                                    decision : String = "majority_firstOnTies") */
      ) ++ postWalkers);
      
      finalWalker.walkToFileSplit(outfile,ensIter,ensHeader, splitFuncOpt = splitFuncOpt);
      
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
            reportln("No table input and no table output. Reading/writing in VCF format.","debug")
            finalWalker.walkVCFFiles(vcffile,outfile, chromList, numLinesRead = numLinesRead, inputFileList = infileList, dropGenotypes = false, splitFuncOpt = splitFuncOpt);
          } else {
            val (vcIterRaw, vcfHeader) = if(!tableInput){
              reportln("No --tableInput param set. Reading in VCF format.","debug")
              getSVcfIterators(infileString=vcffile,chromList=chromList,numLinesRead=numLinesRead,inputFileList = infileList);
            } else {
              reportln("PARAM --tableInput set. Reading in TABLE format.","debug")
              getSVcfIteratorsFromTable(infileString=vcffile,chromList=chromList,numLinesRead=numLinesRead,inputFileList = infileList);
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
    
    burdenWriter.foreach{ bw => {
      bw.close();
    }}
    if(! summaryWriter.isEmpty) summaryWriter.get.close();
  }
  
  
  /*
   * runEnsembleMerger
   *     def walkVCFFiles(infiles : String, outfile : String, chromList : Option[List[String]], numLinesRead : Option[Int], inputFileList : Boolean, dropGenotypes : Boolean = false){
   * 
     def ensembleMergeVariants(vcIters : Seq[Iterator[SVcfVariantLine]], 
                            headers : Seq[SVcfHeader], 
                            inputVcfTypes : Seq[String], genomeFA : Option[String],
                            windowSize : Int = 200) :  (Iterator[SVcfVariantLine],SVcfHeader) = {
   */
  
  
/*
  class CmdAddVcfInfo extends CommandLineRunUtil {
     override def priority = 1;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "AddVcfInfo", 
          quickSynopsis = "", 
          synopsis = "", 
          description = "" + ALPHA_WARNING,
          argList = 
                    new BinaryOptionArgument[List[String]](
                                         name = "chromList", 
                                         arg = List("--chromList"), 
                                         valueName = "chr1,chr2,...",  
                                         argDesc =  "List of chromosomes. If supplied, then all analysis will be restricted to these chromosomes. All other chromosomes wil be ignored."
                                        ) ::
                    new UnaryArgument( name = "inputFileList",
                                         arg = List("--inputFileList"), // name of value
                                         argDesc = ""+
                                                   "" // description
                                       ) ::
                    new BinaryArgument[String](
                                         name = "infoPrefix", 
                                         arg = List("--infoPrefix"), 
                                         valueName = "prefix",  
                                         argDesc =  "",
                                         defaultValue = Some("")
                                        ) ::
                    new FinalArgument[String](
                                         name = "infile1",
                                         valueName = "variants.vcf",
                                         argDesc = "input VCF file. Can be gzipped or in plaintext." // description
                                        ) ::
                    new FinalArgument[String](
                                         name = "infile2",
                                         valueName = "variants.vcf",
                                         argDesc = "input VCF file. Can be gzipped or in plaintext." // description
                                        ) ::
                    new FinalArgument[String](
                                         name = "outfile",
                                         valueName = "outfile.vcf.gz",
                                         argDesc = "The output file. Can be gzipped or in plaintext."// description
                                        ) ::
                    internalUtils.commandLineUI.CLUI_UNIVERSAL_ARGS );

     def run(args : Array[String]) {
       val out = parser.parseArguments(args.toList.tail);
       if(out){
         AddVcfInfo(
             infile2 = parser.get[String]("infile2")
         ).walkVCFFiles(
             infiles = parser.get[String]("infile1"),
             outfile = parser.get[String]("outfile"),
             chromList = parser.get[Option[List[String]]]("chromList"),
             numLinesRead = None,
             inputFileList = parser.get[Boolean]("inputFileList"),
             dropGenotypes = false
         )
       }
     }
  }

  case class AddVcfInfo(infile2 : String) extends SVcfWalker {
    
    //val readers = fileList.map{case (infile,t) => SVcfLine.readVcf(getLinesSmartUnzip(infile), withProgress = false)};
    //val headers = readers.map(_._1);
    //val iteratorArray : Array[BufferedIterator[SVcfVariantLine]] = readers.map(_._2.buffered).toArray;
    //    val currPos = vcSeq.head.pos;
     //   val currChrom = vcSeq.head.chrom;
    //    iteratorArray.indices.foreach{i => {
    //      //iteratorArray(i) = iteratorArray(i).dropWhile(vAlt => vAlt.pos < currPos);
    //      skipWhile(iteratorArray(i))(vAlt => vAlt.chrom != currChrom);
    //      skipWhile(iteratorArray(i))(vAlt => vAlt.pos < currPos && vAlt.chrom == currChrom);
     //   }}
    //    val otherVcAtPos = iteratorArray.indices.map{i => {
     //     extractWhile(iteratorArray(i))(vAlt => vAlt.pos == currPos && vAlt.chrom == currChrom);
    //    }}
    
    ////val (header2,rr) = SVcfLine.readVcf(getLinesSmartUnzip(infile2), withProgress = false);
    
    class vcfLineCircleIterator(f : String) extends BufferedIterator[SVcfVariantLine] {
      var (header,currIterTemp) = SVcfLine.readVcf(getLinesSmartUnzip(f), withProgress = false)
      var currIter = currIterTemp.buffered;
      def head = currIter.head;
      def hasNext : Boolean = true;
      def next : SVcfVariantLine = if(currIter.hasNext){
        currIter.next;
      } else {
        currIter = SVcfLine.readVcf(getLinesSmartUnzip(f), withProgress = false)._2.buffered;
        currIter.next;
      }
    }
    
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      
    }
  }*/

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









 


















