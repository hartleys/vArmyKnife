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

//import com.timgroup.iterata.ParIterator.Implicits._;

object VcfAnnotateTX {
  
    val SNVVARIANT_BASESWAP_LIST = Seq( (("A","C"),("T","G")),
                            (("A","T"),("T","A")),
                            (("A","G"),("T","C")),
                            (("C","A"),("G","T")),
                            (("C","T"),("G","A")),
                            (("C","G"),("G","C"))
                          );
   
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
     
                              
                              
     val manualExtras = sVcfFilterLogicParser.getManualString(Some(vcfFilterManualTitle),Some(vcfFilterManualDesc)) +
                        sGenotypeFilterLogicParser.getManualString(Some(gtFilterManualTitle),Some(gtFilterManualDesc)) 
     val markdownManualExtras = sVcfFilterLogicParser.getMarkdownManualString(Some(vcfFilterManualTitle),Some(vcfFilterManualDesc)) +
                        sGenotypeFilterLogicParser.getMarkdownManualString(Some(gtFilterManualTitle),Some(gtFilterManualDesc)) 
     
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
          description = "This utility performs a series of transformations on an input VCF file and adds an array of informative tags."+BETA_WARNING,
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
                                         valueName = "...",  
                                         argDesc =  "Options for a SnpSift annotate run. Provide all the options for the standard run. "+
                                                    " Do not include the annotate command itself or the input VCF. "+
                                                    "This will call the internal SnpSift annotate commands, not merely run an external instance of SnpSift. "
                                        ).meta(false,"Annotation", 10) ::
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
                                        ).meta(true,"Preprocessing")  :: 
                                       
                    new BinaryOptionArgument[Int]( name = "leftAlignAndTrimWindow",
                                         arg = List("--leftAlignAndTrimWindow"), // name of value\
                                         valueName = "N",
                                         argDesc = "Set the window size used for left align and trim. Indels larger than this will not be left aligned."+
                                                   "" +
                                                   ""+
                                                   ""// description
                                       ).meta(false,"Preprocessing") ::

                    new UnaryArgument( name = "leftAlignAndTrimSecondarys",
                                         arg = List("--leftAlignAndTrimSecondarys"), // name of value
                                         argDesc = "Left align and trim any secondary input VCFs using a modified and ported version of the GATK v1.8-2 LeftAlignAndTrim walker."+
                                                   "" +
                                                   ""+
                                                   ""// description
                                       ).meta(false,"Preprocessing") ::
                                       
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
                                         //defaultValue = Some("SWH_FILTER_GT")
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
                                         argDesc =  "If this parameter is set, then the utility will stop after reading in N variant lines."
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
                                         argDesc =  "For multiallelic-split variants, this defines the character used for the 'other' allele. "+
                                                    "If this is used, the original version will be copied as a backup."
                                        ).meta(false,"Annotation") :: 
                    new UnaryArgument( name = "dropGenotypeData",
                                         arg = List("--dropGenotypeData"), // name of value
                                         argDesc = "If this flag is included, then ALL sample-level columns will be stripped from the output VCF. "+
                                                   "This greatly reduces the file size, and can be useful for making portable variant set VCFs." +
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
                        ).meta(true,"SnpEff Annotation Processing",20) ::
                    new BinaryOptionArgument[String](
                                         name = "snpEffTagPrefix", 
                                         arg = List("--snpEffTagPrefix"), 
                                         valueName = "ANNEX_",  
                                         argDesc =  "Prefix for SnpEff extracted info tags"
                        ).meta(true,"SnpEff Annotation Processing") ::
                    new BinaryOptionArgument[List[String]](
                                         name = "snpEffBiotypeKeepList", 
                                         arg = List("--snpEffBiotypeKeepList"), 
                                         valueName = "ANN",  
                                         argDesc =  "todo desc"
                        ).meta(true,"SnpEff Annotation Processing") ::
                    new BinaryOptionArgument[List[String]](
                                         name = "snpEffEffectKeepList", 
                                         arg = List("--snpEffEffectKeepList"), 
                                         valueName = "ANN",  
                                         argDesc =  "todo desc"
                        ).meta(true,"SnpEff Annotation Processing") ::
                    new BinaryOptionArgument[List[String]](
                                         name = "snpEffWarningDropList", 
                                         arg = List("--snpEffWarningDropList"), 
                                         valueName = "ANN",  
                                         argDesc =  "todo desc"
                        ).meta(true,"SnpEff Annotation Processing") ::
                    new BinaryOptionArgument[List[String]](
                                         name = "snpEffKeepIdx", 
                                         arg = List("--snpEffKeepIdx"), 
                                         valueName = "0,1,2,...",  
                                         argDesc =  "todo desc"
                        ).meta(true,"SnpEff Annotation Processing") ::

                        
                        
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
                
                thirdAlleleChar = parser.get[Option[String]]("thirdAlleleChar")
             )
       }
     }
  }


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
                filterTag : Option[String] = Some("SWH_FILT_GT"),
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
                
                thirdAlleleChar : Option[String] = None
                ){
                /*
                 * 
               AddFuncTag(func : String, newTag : String, paramTags : Seq[String], digits : Option[Int] = None, desc : Option[String] = None )
                */
    
    /*val (vcIter,vcfHeader) = internalUtils.VcfTool.getVcfFilesIter(infile = vcffile, 
                                       chromList = chromList,infileList = infileList,
                                       vcfCodes = vcfCodes);
    */
    val summaryWriter = if(summaryFile.isEmpty) None else Some(openWriterSmart(summaryFile.get));
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
            if(leftAlignAndTrim){
              if(genomeFA.isEmpty){
                error("ERROR: in order to left align and trim, you MUST specify a genome fasta file with the --genomeFA parameter");
              }
              (if(makeFirstBaseMatch){
                Seq(internalUtils.GatkPublicCopy.FixFirstBaseMismatch(genomeFa = genomeFA.get,windowSize = leftAlignAndTrimWindow.getOrElse(200)))
              } else {
                Seq[SVcfWalker]()
              }) ++ Seq[SVcfWalker](
                    //internalUtils.GatkPublicCopy.FixFirstBaseMismatch(genomeFa = genomeFA.get,windowSize = leftAlignAndTrimWindow.getOrElse(200)),
                    internalUtils.GatkPublicCopy.LeftAlignAndTrimWalker(genomeFa = genomeFA.get,windowSize = leftAlignAndTrimWindow.getOrElse(200), useGatkLibCall = leftAlignAndTrimWindow.isEmpty)
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
        
        
    
    val postWalkers : Seq[SVcfWalker] =  (
            
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
          
        ) ++ (

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
        ) ++ (
            
            snpSiftAnnotate.toSeq.map{ssa => {
              new SnpSiftAnnotater(ssa.split(",",2)(0),ssa.split(",",2)(1))
            }}
            
        ) ++ (
            
            snpSiftDbnsfp.toSeq.map{ssa => {
              new SnpSiftDbnsfp(ssa.split(",",2)(0),ssa.split(",",2)(1))
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
          genoFilter match {
            case Some(gf) => {
              reportln("Creating GT Filter ... "+getDateAndTimeString,"debug");
              
              val ft = filterTag.getOrElse("SWH_FILTER_GT")
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
                  
                  AddTxBedFile(bedFile = bedFile, tag =t, bufferDist = string2int(bufferDist), desc =desc, chromList = chromList,style=style);
                }}
              }
              case None => Seq[SVcfWalker]();
            }
            
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
              //              val ft = filterTag.getOrElse("SWH_FILTER_GT")
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
               tagPrefix = "SWH_dbNSFP_",
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
                val outputTagPrefix = depthStatCells.lift(3).getOrElse("SWH_STAT_");
                  //if(depthStatCells.isDefinedAt(3)) depthStatCells(3) else "SWH_STAT_";
                
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
            //                 outputTagPrefix : String = "SWH_SAMPLIST_",
            //                 printLimit : Int = 25
            //                )
             
            addSampTag match {
              case Some(st) => {
                val stCells = st.split(",");
                val printLimit = stCells.head.toInt;
                val tagPrefix = if(stCells.isDefinedAt(1)) stCells(1) else "SWH_SAMPLIST_";
                
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
                     snpEffKeepIdx = snpEffKeepIdx)
                 );
               }
               case None => {
                 Seq[SVcfWalker]()
               }
             }

        ) ++ (
            ctrlAlleFreqKeys.toSeq.map{ cafk => {
                val cells = cafk.split(":");
                val mafTags = cells.last.split(",");
                val maxMafString = if(cells.length == 2) cells.head else "SWH_ACMG_ctrlAFMAX"
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

        ) ++ (
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
                      val styleOpt = cells.lift(4);
                      VcfGtExpressionTag( expr=expr,tagID=tagID,tagDesc=tagDesc,styleOpt = styleOpt,                  
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

            
        ) ++ (
            mergeBooleanTags.map{ mbt => {
              val cells : Array[String] = mbt.split(",")
              val tagID : String = cells(0);
              val mergeTags : List[String] = cells(1).split( "[|]" ).toList
              val tagNames : Option[List[String]] = cells.lift(2).map{ cc => cc.split("[|]").toList }
              
              new MergeBooleanTags(tagID = tagID, mergeTags = mergeTags, tagNames = tagNames)
            } }
            
        ) ++ (
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
            if(dropGenotypeData){
                Seq[SVcfWalker](StripGenotypeData())
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
                val nameValid = validStringRegex.matcher(name).find();
                if(! nameValid){
                  notice("","IntervalNameNotValid",1)
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
          new EnsembleMergeMetaDataWalker(inputVcfTypes = singleCallerPriority.getOrElse(inputNames))
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

  class CmdConvertToStandardVcf extends CommandLineRunUtil {
     override def priority = 1;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "ConvertToStandardVcf", 
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
                    new FinalArgument[String](
                                         name = "infile",
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
         new StdVcfConverter(

         ).walkVCFFiles(
             infiles = parser.get[String]("infile"),
             outfile = parser.get[String]("outfile"),
             chromList = parser.get[Option[List[String]]]("chromList"),
             numLinesRead = None,
             inputFileList = parser.get[Boolean]("inputFileList"),
             dropGenotypes = false
         )
       }
     }
    
  }


  case class ConvertToMatrixWalker(gtTags : List[String] = List[String]("GT"), gtTagPrefix : String = "MATRIX_") extends SVcfWalker {
    def convertGtToString(gt : Array[String]) : String = {
      gt.map{ gg => {
        if(gg.contains('.')){
          "."
        } else if(gg == "0" || gg == "0/0"){
          "0";
        } else if(gg == "0/1"){
          "1";
        } else if(gg == "1/1" || gg == "1"){
          "2";
        } else if(gg == "1/2" || gg == "2/1"){
          "3";
        } else if(gg == "0/2"){
          "H"
        } else if(gg == "2/2"){
          "F"
        } else {
          error("Error: unrecognized variant genotype, cannot convert to matrix.");
          "?"
        }
      }}.mkString("");
    }
    
    def walkerName : String = "ConvertToMatrixWalker"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("gtTags", gtTags.mkString("|"))
    );
    val gtTagArray : Array[String] = gtTags.toArray;
    val outFmt : Seq[String] = Seq[String]("GT") ++ gtTags.map{ gtt => gtTagPrefix + gtt };
    val blankArray : Array[Array[String]] = Array.ofDim[String](1,1);
    blankArray(0)(0) = "1/1";
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      
      gtTags.foreach{ gg => {
        outHeader.addFormatLine((new SVcfCompoundHeaderLine("FORMAT",gtTagPrefix+gg,Number="1",Type="String",desc="Text matrix of genotype field "+gg)).addWalker(this).addExtraField("sampIDs", vcfHeader.titleLine.sampleList.mkString("|")) )
      }}
      //outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",dupTag,Number="1",Type="Integer",desc="Equal to 1 iff the line was duplicated and duplicates were removed."));
      outHeader.titleLine = new SVcfTitleLine(List("MATRIX"))

      (addIteratorCloseAction( vcMap(vcIter){ v => {
        val vc = v.getOutputLine();
        val genoOut = gtTagArray.flatMap{gtag => {
          val idx = v.genotypes.fmt.indexOf(gtag);
          if(idx == -1){
            None
          } else {
            Some(Array[String](convertGtToString(v.genotypes.genotypeValues(idx))))
          }
        }}
        
        vc.in_genotypes = new SVcfGenotypeSet(fmt = outFmt, genotypeValues = blankArray ++ genoOut);
        
        vc;
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
    }

  }
  

  case class ConvertToGtWalker(sampleNames : Option[List[String]] = None,gtTagPrefix : String = "MATRIX_") extends SVcfWalker {
    def convertGtToString(gt : Array[String]) : String = {
      gt.map{ gg => {
        if(gg.contains('.')){
          "."
        } else if(gg == "0" || gg == "0/0"){
          "0";
        } else if(gg == "0/1"){
          "1";
        } else if(gg == "1/1" || gg == "1"){
          "2";
        } else if(gg == "1/2" || gg == "2/1"){
          "3";
        } else if(gg == "0/2"){
          "H"
        } else if(gg == "2/2"){
          "F"
        } else {
          error("Error: unrecognized variant genotype, cannot convert to matrix.");
          "?"
        }
      }}.mkString("");
    }
    
    def convertStringToGt(gt : String) : Array[String] = {
      gt.map{ gg => {
        if(gg == '.'){
          "./."
        } else if(gg == '0'){
          "0/0";
        } else if(gg == '1'){
          "0/1";
        } else if(gg == '2'){
          "1/1";
        } else if(gg == '3'){
          "1/2";
        } else if(gg == 'H'){
          "0/2"
        } else if(gg == 'F'){
          "2/2"
        } else {
          error("Error: unrecognized variant genotype, cannot convert to matrix.");
          "?"
        }
      }}.toArray;
    }
    
    def walkerName : String = "ConvertMatrixToGtWalker"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](

    );
    //val gtTagArray : Array[String] = gtTags.toArray;
    //val outFmt : Seq[String] = Seq[String]("GT") ++ gtTags.map{ gtt => gtTagPrefix + gtt };
    
    //blankArray(0)(0) = "1/1";
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val vcBuf = vcIter.buffered;
      val fmtLines = if(vcfHeader.formatLines.length == 0){
        vcfHeader.infoLines.withFilter{fline => fline.ID.startsWith(gtTagPrefix)}.map{fline => fline.ID.drop(gtTagPrefix.length)}
      } else {
        vcfHeader.formatLines.withFilter{fline => fline.ID.startsWith(gtTagPrefix)}.map{fline => fline.ID.drop(gtTagPrefix.length)}
      }
      val defaultSamps =  Range(0,vcBuf.head.genotypes.genotypeValues(1)(0).length).map{"SAMP_"+_}.toVector ;
      val samps : Vector[String] = if(vcfHeader.formatLines.length == 0){
        sampleNames.getOrElse( defaultSamps ).toVector
      } else {
        vcfHeader.formatLines.tail.head.extraFields.get("sampIDs").map{_.split("\\|").toVector}.getOrElse( defaultSamps ).toVector;
      }
      val blankArray : Array[Array[String]] = Array.fill[String](1,samps.length)("./.");
      val fmt = Seq("GT") ++ fmtLines;
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      
      fmtLines.foreach{ gg => {
        outHeader.addInfoLine((new SVcfCompoundHeaderLine("FORMAT",gg,Number="1",Type="String",desc="Genotype field "+gg)).addWalker(this) )
      }}
      //outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",dupTag,Number="1",Type="Integer",desc="Equal to 1 iff the line was duplicated and duplicates were removed."));
      outHeader.titleLine = new SVcfTitleLine(samps.toList)

      (addIteratorCloseAction( vcMap(vcIter){ v => {
        val vc = v.getOutputLine();
        
        val genoOut = blankArray ++ v.genotypes.genotypeValues.tail.map{ gmat => {
          if(gmat.head == "."){
            blankArray(0);
          } else {
            if(gmat.head.length != samps.length){
              warning("SAMPS and GT LENGTH DONT MATCH: "+gmat.head.length + " vs "+ samps.length+"\n"+
                      "   fmt:\""+fmt.mkString(",")+"\"\n"+
                      "   gtString:\""+gmat.head+"\""+
                      "","SAMPS_AND_GT_LENGTH_DONT_MATCH",10);
            }
            convertStringToGt(gmat.head);
          }
        }}
        
        /*if(genoOut.length != fmt.length){
          warning("GENO_AND_FMT_LENGTH_DONT_MATCH: "+ genoOut.length + " vs "+fmt.length,"GENO_AND_FMT_LENGTH_DONT_MATCH",10);
        }
        genoOut.zipWithIndex.find{ case (gg,idx) => gg.length != samps.length }.foreach{ case (gg,idx) => {
          
        }}*/
        

        vc.in_genotypes = new SVcfGenotypeSet(fmt = fmt, genotypeValues = genoOut);
        
        vc;
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
    }

  }
  
  case class RemoveDuplicateLinesWalker(dupTag : String = "SWH_remDup", compileTags : Boolean = true) extends SVcfWalker {
    def walkerName : String = "RemoveDuplicateLinesWalker"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("dupTag", dupTag)
    );
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      var idx = 0;
      outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",dupTag,Number="1",Type="Integer",desc="Equal to 1 iff the line was duplicated and duplicates were removed."));
      var dupLocusCt = 0;
      var dupRemovedCt = 0;

      (addIteratorCloseAction( vcGroupedFlatMap(groupBySpan(vcIter.buffered)(vc => vc.pos)){ vcSeq => {
        //val vc = v.getOutputLine()
        
        val vars = vcSeq.map{vc => (vc.chrom,vc.pos,vc.ref,vc.alt.head)}.distinct;
        if(vars.length != vcSeq.length){
          dupLocusCt += 1;
          vars.map{ case (chrom,pos,ref,alt) => {
            val vcs = vcSeq.filter{vc => vc.chrom == chrom && vc.pos == pos && vc.ref == ref && vc.alt.head == alt};
            val vc = vcs.head.getOutputLine();
            if(vcs.length > 1){
              vc.addInfo(dupTag,""+vcs.length)
              dupRemovedCt += (vcs.length - 1);
              if(compileTags){
                vc.info.foreach{ case (k,vv) => {
                  vc.addInfo(k,vcs.map{ vvc => vvc.info.getOrElse(k,None).getOrElse(".") }.mkString(","));
                }}
              }
            } else {
              vc.addInfo(dupTag,"1")
            }
            vc;
          }}
        } else {
          vcSeq.map{ v => {
            val vc = v.getOutputLine()
            vc.addInfo(dupTag,"1");
            vc
          }}
        }
      }}, closeAction = (() => {
        notice("Removed "+dupRemovedCt+" duplicates from "+dupLocusCt+" loci.","DUPLICATE_LOCUS_NOTE",100);
      })),outHeader)
    }

  }
  
  

  

  class CmdCreateVariantSampleTableV2 extends CommandLineRunUtil {
     override def priority = 1;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "CreateVariantSampleTableV2", 
          quickSynopsis = "", 
          synopsis = "", 
          description = "" + ALPHA_WARNING,
          argList = 
                    new BinaryOptionArgument[String](
                                         name = "sampleDecoder", 
                                         arg = List("--sampleDecoder"), 
                                         valueName = "...",  
                                         argDesc =  ""
                                        ) ::
                                        
                                        

                    new BinaryMonoToListArgument[String](
                                         name = "keepVariantExpressions", 
                                         arg = List("--keepVariantExpressions"),
                                         valueName = "",
                                         argDesc =  ""
                                        ) :: 
                    new BinaryOptionArgument[List[String]](
                                         name = "kveName", 
                                         arg = List("--kveName"), 
                                         valueName = "...",  
                                         argDesc =  ""
                                        ) ::
                    new BinaryOptionArgument[List[String]](
                                         name = "kveGeneTag", 
                                         arg = List("--kveGeneTag"), 
                                         valueName = "...",  
                                         argDesc =  ""
                                        ) ::
                    new BinaryOptionArgument[List[String]](
                                         name = "kveSubtractGeneTags", 
                                         arg = List("--kveSubtractGeneTags"), 
                                         valueName = "...",  
                                         argDesc =  ""
                                        ) ::
                                        
                                        
                                        
                    new BinaryOptionArgument[List[String]](
                                         name = "geneList", 
                                         arg = List("--geneList"), 
                                         valueName = "...",  
                                         argDesc =  ""
                                        ) ::
                    new BinaryOptionArgument[List[String]](
                                         name = "decoderColumns", 
                                         arg = List("--decoderColumns"), 
                                         valueName = "...",  
                                         argDesc =  ""
                                        ) ::
                    new BinaryOptionArgument[List[String]](
                                         name = "decoderColumnNames", 
                                         arg = List("--decoderColumnNames"), 
                                         valueName = "...",  
                                         argDesc =  ""
                                        ) ::
                                        
                    new BinaryOptionArgument[String](
                                         name = "outfilePrefix", 
                                         arg = List("--outfilePrefix"), 
                                         valueName = "...",  
                                         argDesc =  ""
                                        ) ::
                                        
                    new BinaryOptionArgument[String](
                                         name = "gtTag", 
                                         arg = List("--gtTag"), 
                                         valueName = "...",  
                                         argDesc =  ""
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "sectionBy", 
                                         arg = List("--sectionBy"), 
                                         valueName = "...",  
                                         argDesc =  ""
                                        ) ::
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
                                       
                    new FinalArgument[String](
                                         name = "infile",
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
         new CreateVariantSampleTable(
                       sampleDecoder = parser.get[Option[String]]("sampleDecoder").get, 
                        keepVariantExpressions = parser.get[List[String]]("keepVariantExpressions"),
                        kveNames = parser.get[List[String]]("kveNames"),
                        kveGeneTag = parser.get[List[String]]("kveGeneTag"),
                        kveSubtractGeneTags = parser.get[List[String]]("kveSubtractGeneTags"),
                        
                        geneList = parser.get[Option[List[String]]]("geneList").get,
                        decoderColumns = parser.get[Option[List[String]]]("decoderColumns").get,
                        decoderColumnNames = parser.get[Option[List[String]]]("decoderColumnNames"),
                        //keepVarExprNames : Option[Seq[String]],
                        outfilePrefix = parser.get[Option[String]]("outfilePrefix").get,
                        gtTag  =  parser.get[Option[String]]("gtTag").getOrElse("GT"),
                        sectionBy  = parser.get[Option[String]]("sectionBy").getOrElse("sampGroup")
         ).walkVCFFiles(
             infiles = parser.get[String]("infile"),
             outfile = parser.get[String]("outfile"),
             chromList = parser.get[Option[List[String]]]("chromList"),
             numLinesRead = None,
             inputFileList = parser.get[Boolean]("inputFileList"),
             dropGenotypes = false
         )
       }
     }
    
  }
  

  case class CreateVariantSampleTable(sampleDecoder : String, 
      
                                      var keepVariantExpressions : List[String],
                                      var kveNames : List[String],
                                      var kveGeneTag : List[String],
                                      var kveSubtractGeneTags : List[String],
                                      
                                      geneList : Seq[String],
                                      decoderColumns : Seq[String],
                                      decoderColumnNames : Option[Seq[String]],
                                      //keepVarExprNames : Option[Seq[String]],
                                      
                                      outfilePrefix : String,
                                      gtTag : String = "GT",
                                      sectionBy : String = "sampGroup"
                                      ) extends SVcfWalker {
    def walkerName : String = "CreateVariantSampleTable"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("sampleDecoder",sampleDecoder),
        ("geneList",geneList.mkString("|"))
    );
    
    if( kveNames.length != keepVariantExpressions.length ){
      warning("kveNames.length != kve.length. Creating default names!","kve_warn",-1)
      kveNames = keepVariantExpressions.zipWithIndex.map{ case (kve,i) => "EXPR_"+i }
    }
    if( kveGeneTag.length != keepVariantExpressions.length ){
      if(kveGeneTag.length == 1){
        kveGeneTag = keepVariantExpressions.map{ kve => kveGeneTag.head }
      } else {
        error("Error: kveGeneTag.length != kve.length. You must set the same number of keepVariantExpressions as there are kveGeneTags, OR set only one kveGeneTag!")
      }
    }
    if( kveSubtractGeneTags.length != keepVariantExpressions.length ){
      if(kveSubtractGeneTags.length == 0){
        kveSubtractGeneTags = keepVariantExpressions.map{ kve => "." }
      } else if(kveSubtractGeneTags.length == 1){
        kveSubtractGeneTags = keepVariantExpressions.map{ kve => kveSubtractGeneTags.head }
      } else {
        error("Error: kveSubtractGeneTags.length != kve.length and kveSubtractGeneTags.length != 1. You must set the same number of keepVariantExpressions as there are kveGeneTags, OR set only one kveGeneTag!")
      }
    }
    
    val fullGeneList =  Seq( geneList.mkString(":") ) ++ geneList
    val geneNames = Seq("allGenes") ++ geneList
    
    case class KVE( exprString : String, name : String, geneTagString : String, subGeneTagString : String ) {
      
      lazy val varFcn = internalUtils.VcfTool.sVcfFilterLogicParser.parseString(exprString);
      lazy val geneTag : Seq[String] = geneTagString.split(",").toSeq;
      lazy val subtractGeneTags : Seq[String] = if(subtractGeneTags == ".") {
        Seq[String]()
      } else {
        subGeneTagString.split(",").toSeq
      }
      
      lazy val varOnGeneFcn  : Seq[SFilterLogic[SVcfVariantLine]] = fullGeneList.map{ g => {
        val kve : String = (
            Seq[String]( "("+
                   geneTag.map{ tt => {
                     "INFO.inAnyOf:"+tt+":"+g
                   }}.mkString(" OR ") + 
                ")"
            ) ++ 
            subtractGeneTags.map{ subTag => {
              " ( NOT INFO.inAnyOf:"+subTag+":"+g+" ) "
            }}
          ).mkString(" AND ")
  
        internalUtils.VcfTool.sVcfFilterLogicParser.parseString(kve);
      }}
      
    }
    
    val kves : Seq[KVE] = keepVariantExpressions.zip(kveNames).zip(kveGeneTag).zip(kveSubtractGeneTags).map{ case (((kveString,kveName),kveGT),kveSGT) => {
      KVE(exprString = kveString, name = kveName, geneTagString = kveGT, subGeneTagString = kveSGT);
    }}
    
    val keepVarFcn : Seq[SFilterLogic[SVcfVariantLine]] = keepVariantExpressions.map{ kve => {
          internalUtils.VcfTool.sVcfFilterLogicParser.parseString(kve);
    }}
    
    val decoderLines = getLinesSmartUnzip(sampleDecoder)
    val titleLine = decoderLines.next.split("\t");
    val decoderArray = decoderLines.toArray.map{ line => line.split("\t")};
    
    val sampLists : Seq[(String,Set[String])] = decoderColumns.zip{ decoderColumnNames.getOrElse(decoderColumns) }.flatMap{ case (colName,colTitle) => {
      val colIdx = titleLine.indexOf(colName);
      if(colIdx == -1){
        error("Column not found in decoder file: \""+colName+"\"\n Found columns: [\""+titleLine.mkString("\",\"")+"\"]\n"+"\n in file: "+sampleDecoder);
      }
      val valueList = decoderArray.map{cells => {
        cells(colIdx)
      }}.toSet.filter{ c => c != "0" }
      
      valueList.toVector.sorted.map{ xx => {
        val outName = if(xx == "1"){
          ""
        } else {
          "_"+xx
        }
        (colTitle+outName,decoderArray.withFilter{cells => {
          cells(colIdx) == xx
        }}.map{_.head}.toSet)
      }}
    }}
    
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val samps = vcfHeader.titleLine.sampleList;
      val sampGroups = sampLists.unzip._1;
      
      val varCtArray       = Array.fill[Int](sampGroups.length,keepVariantExpressions.length,fullGeneList.length)(0);
      val varSampCtArray   = Array.fill[Int](sampGroups.length,keepVariantExpressions.length,fullGeneList.length)(0);
      val varSampCtArrayHom   = Array.fill[Int](sampGroups.length,keepVariantExpressions.length,fullGeneList.length)(0);
      val varSampBoolArray = Array.fill[Boolean](sampGroups.length,keepVariantExpressions.length,fullGeneList.length, samps.length)(false);
      
      val sampFlags : Seq[Array[Boolean]] = sampLists.map{ case (setName, subsamps) => {
        samps.toArray.map{ s => subsamps.contains(s) }
      }}

      (addIteratorCloseAction( vcMap(vcIter){ v => {
        //DO STUFF
        
        kves.zipWithIndex.foreach{ case (kve,j) => {
          
          val onGene = kve.varOnGeneFcn.map{ gFcn => gFcn.keep(v) }.zipWithIndex.filter{ case (kv,j) => kv }.map{ _._2 }
          //val keepVar = keepVarFcn.map{ kFcn => kFcn.keep(v) }.zipWithIndex.filter{ case (kv,j) => kv }.map{ _._2 }
          
          if(kve.varFcn.keep(v) && onGene.nonEmpty){
            val gtIdx = v.genotypes.fmt.indexOf(gtTag);
            if(gtIdx == -1){
              error("Genotype tag: \""+gtTag+"\" not found!");
            }
            val anyAlt = v.genotypes.genotypeValues(gtIdx).map{ gtString => {
              gtString.contains('1');
            }}
            val anyHom = v.genotypes.genotypeValues(gtIdx).map{ gtString => gtString == "1/1" }
            
            Range(0,sampFlags.length).foreach{ i => {
              val isAlt = anyAlt.zip{sampFlags(i)}.zipWithIndex.filter{ case ((ia,ib),z) => ia && ib }.map{_._2}
              val isHom = anyHom.zip{sampFlags(i)}.zipWithIndex.filter{ case ((ia,ib),z) => ia && ib }.map{_._2}
              val numAlt = isAlt.length
              val numHom = isHom.length;
              if(numAlt > 0){
                  onGene.foreach{ k => {
                    varCtArray(i)(j)(k) += 1;
                    varSampCtArray(i)(j)(k) += numAlt
                    varSampCtArrayHom(i)(j)(k) += numHom
                    isAlt.foreach{ z => {
                      varSampBoolArray(i)(j)(k)(z) = true
                    }}
                  }}
              }
            }}
          }
          
        }}
        v
      }}, closeAction = (() => {
        //do nothing
        if(sectionBy == "sampGroup"){
          sampGroups.zip(sampFlags).zipWithIndex.foreach{ case ((dd,sampBools),i) => {
            val writer = openWriter(outfilePrefix + dd + ".table.txt");
            
            writer.write(dd+"\t"+sampBools.count{x => x}+"\n");
            writer.write("geneID\t"+kves.zipWithIndex.map{ case (kve,j) => {
                ""+kve.name+"\t-\t-\t-\t-"
            }}.mkString("\t") + "\n")
            writer.write("geneID\t"+kves.zipWithIndex.flatMap{ case (kve,j) => {
                Seq("varCt","altGenoCt",  "sampVarCt","homGenoCt","alleCt")
            }}.mkString("\t") + "\n")
            writer.write("geneID\t"+kves.zipWithIndex.flatMap{ case (kve,j) => {
                Seq(kve.name + "_varCt",kve.name + "_altGenoCt", kve.name + "_sampVarCt",kve.name +"_homGenoCt",kve.name+"_alleCt")
            }}.mkString("\t") + "\n")
              
            fullGeneList.zip(geneNames).zipWithIndex.foreach{ case ((gExpr,g),k) => {
              writer.write(
                  g + "\t"+  kves.zipWithIndex.map{ case (kve,j) => {
                    "" +varCtArray(i)(j)(k)+"\t"+ 
                        varSampCtArray(i)(j)(k)+"\t"+
                        varSampBoolArray(i)(j)(k).count{x => x}+"\t"+
                        varSampCtArrayHom(i)(j)(k)+"\t"+
                        (varSampCtArrayHom(i)(j)(k)+varSampCtArray(i)(j)(k))
                  }}.mkString("\t") + 
                  "\n"
              );
            }}
            
            writer.close();
          }}
        } else if(sectionBy == "varGroup"){
           kves.zipWithIndex.map{ case (kve,j) => {
             val writer = openWriter(outfilePrefix + kve.name + ".table.txt");
              writer.write("geneID\t"+sampGroups.zipWithIndex.flatMap{ case (dd,i) => {
                 dd+"\t-\t-\t-\t-"
              }}.mkString("\t") + "\n")
              writer.write("geneID\t"+sampGroups.zipWithIndex.flatMap{ case (dd,i) => {
                Seq("varCt","altGenoCt","sampVarCt","homGenoCt","alleCt")
              }}.mkString("\t") + "\n")
              writer.write("geneID\t"+sampGroups.zipWithIndex.flatMap{ case (dd,i) => {
                Seq(dd + "_varCt",dd + "_altGenoCt", dd + "_sampVarCt",dd +"_homGenoCt",dd+"_alleCt")
              }}.mkString("\t") + "\n")
              
              writer.write("sampleCts\t"+sampGroups.zip(sampFlags).zipWithIndex.flatMap{ case ((dd,sampBools),i) => {
                Seq(""+sampBools.count{x => x},"-","-","-","-")
              }}.mkString("\t") + "\n")
              
              geneNames.zipWithIndex.foreach{ case (g,k) => {
                writer.write(
                  g + "\t"+  sampGroups.zipWithIndex.map{ case (dd,i) => {
                    "" +varCtArray(i)(j)(k)+"\t"+ 
                        varSampCtArray(i)(j)(k)+"\t"+
                        varSampBoolArray(i)(j)(k).count{x => x}+"\t"+
                        varSampCtArrayHom(i)(j)(k)+"\t"+
                        (varSampCtArrayHom(i)(j)(k)+varSampCtArray(i)(j)(k))
                  }}.mkString("\t") + 
                  "\n"
                );
              }}
             writer.close();
           }}
        }
                                    //  keepVarExprNames : Option[Seq[String]],
      })),vcfHeader)
    }

  }
  
  
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  case class CreateVariantAFTable_OLD(
                                      var keepVariantExpressions : List[String],
                                      var kveNames : List[String],
                                      var kveGeneTag : List[String],
                                      var kveSubtractGeneTags : List[String],
                                      
                                      geneList : Seq[String],
                                      countColumns : Seq[String],
                                      countColumnNames : Option[Seq[String]],
                                      
                                      outfilePrefix : String,
                                      sectionBy : String = "sampGroup"
                                      ) extends SVcfWalker {
    def walkerName : String = "CreateVariantAFTable"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("geneList",geneList.mkString("|"))
    );
    
    if( kveNames.length != keepVariantExpressions.length ){
      warning("kveNames.length != kve.length. Creating default names!","kve_warn",-1)
      kveNames = keepVariantExpressions.zipWithIndex.map{ case (kve,i) => "EXPR_"+i }
    }
    if( kveGeneTag.length != keepVariantExpressions.length ){
      if(kveGeneTag.length == 1){
        kveGeneTag = keepVariantExpressions.map{ kve => kveGeneTag.head }
      } else {
        error("Error: kveGeneTag.length != kve.length. You must set the same number of keepVariantExpressions as there are kveGeneTags, OR set only one kveGeneTag!")
      }
    }
    if( kveSubtractGeneTags.length != keepVariantExpressions.length ){
      if(kveSubtractGeneTags.length == 0){
        kveSubtractGeneTags = keepVariantExpressions.map{ kve => "." }
      } else if(kveSubtractGeneTags.length == 1){
        kveSubtractGeneTags = keepVariantExpressions.map{ kve => kveSubtractGeneTags.head }
      } else {
        error("Error: kveSubtractGeneTags.length != kve.length and kveSubtractGeneTags.length != 1. You must set the same number of keepVariantExpressions as there are kveGeneTags, OR set only one kveGeneTag!")
      }
    }
    
    val fullGeneList =  Seq( geneList.mkString(":") ) ++ geneList
    val geneNames = Seq("allGenes") ++ geneList
    
    case class KVE( exprString : String, name : String, geneTagString : String, subGeneTagString : String ) {
      
      lazy val varFcn = internalUtils.VcfTool.sVcfFilterLogicParser.parseString(exprString);
      lazy val geneTag : Seq[String] = geneTagString.split(",").toSeq;
      lazy val subtractGeneTags : Seq[String] = if(subtractGeneTags == ".") {
        Seq[String]()
      } else {
        subGeneTagString.split(",").toSeq
      }
      
      lazy val varOnGeneFcn  : Seq[SFilterLogic[SVcfVariantLine]] = fullGeneList.map{ g => {
        val kve : String = (
            Seq[String]( "("+
                   geneTag.map{ tt => {
                     "INFO.inAnyOf:"+tt+":"+g
                   }}.mkString(" OR ") + 
                ")"
            ) ++ 
            subtractGeneTags.map{ subTag => {
              " ( NOT INFO.inAnyOf:"+subTag+":"+g+" ) "
            }}
          ).mkString(" AND ")
  
        internalUtils.VcfTool.sVcfFilterLogicParser.parseString(kve);
      }}
      
    }
    
    val kves : Seq[KVE] = keepVariantExpressions.zip(kveNames).zip(kveGeneTag).zip(kveSubtractGeneTags).map{ case (((kveString,kveName),kveGT),kveSGT) => {
      KVE(exprString = kveString, name = kveName, geneTagString = kveGT, subGeneTagString = kveSGT);
    }}
    
    val keepVarFcn : Seq[SFilterLogic[SVcfVariantLine]] = keepVariantExpressions.map{ kve => {
          internalUtils.VcfTool.sVcfFilterLogicParser.parseString(kve);
    }}
    
    //val decoderLines = getLinesSmartUnzip(sampleDecoder)
    //val titleLine = decoderLines.next.split("\t");
    //val decoderArray = decoderLines.toArray.map{ line => line.split("\t")};
    /*val sampLists : Seq[(String,Set[String])] = decoderColumns.zip{ decoderColumnNames.getOrElse(decoderColumns) }.flatMap{ case (colName,colTitle) => {
      val colIdx = titleLine.indexOf(colName);
      if(colIdx == -1){
        error("Column not found in decoder file: \""+colName+"\"\n Found columns: [\""+titleLine.mkString("\",\"")+"\"]\n"+"\n in file: "+sampleDecoder);
      }
      val valueList = decoderArray.map{cells => {
        cells(colIdx)
      }}.toSet.filter{ c => c != "0" }
      
      valueList.toVector.sorted.map{ xx => {
        val outName = if(xx == "1"){
          ""
        } else {
          "_"+xx
        }
        (colTitle+outName,decoderArray.withFilter{cells => {
          cells(colIdx) == xx
        }}.map{_.head}.toSet)
      }}
    }}*/
    
    
    //countColumns : Seq[String],
    //countColumnNames : Option[Seq[String]],
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      //val samps = vcfHeader.titleLine.sampleList;
      //val sampGroups = sampLists.unzip._1;
      
      val varCtArray       = Array.fill[Int](countColumns.length,keepVariantExpressions.length,fullGeneList.length)(0);
      val varSumArray      = Array.fill[Int](countColumns.length,keepVariantExpressions.length,fullGeneList.length)(0);
      //val varSampCtArrayHom   = Array.fill[Int](countColumns.length,keepVariantExpressions.length,fullGeneList.length)(0);
      //val varSampBoolArray = Array.fill[Boolean](countColumns.length,keepVariantExpressions.length,fullGeneList.length, samps.length)(false);
      
      //val sampFlags : Seq[Array[Boolean]] = sampLists.map{ case (setName, subsamps) => {
      //  samps.toArray.map{ s => subsamps.contains(s) }
      //}}

      (addIteratorCloseAction( vcMap(vcIter){ v => {
        //DO STUFF
        
        kves.zipWithIndex.foreach{ case (kve,j) => {
          
          val onGene = kve.varOnGeneFcn.map{ gFcn => gFcn.keep(v) }.zipWithIndex.filter{ case (kv,j) => kv }.map{ _._2 }
          //val keepVar = keepVarFcn.map{ kFcn => kFcn.keep(v) }.zipWithIndex.filter{ case (kv,j) => kv }.map{ _._2 }
          
          if(kve.varFcn.keep(v) && onGene.nonEmpty){
            Range(0,countColumns.length).foreach{ i => {
              val ct = v.info.get(countColumns(i)).getOrElse(None).flatMap{ xx => if(xx == ".") None else Some(xx.toInt) }.getOrElse(0)
              if(ct > 0){
                onGene.foreach{ k => {
                  varCtArray(i)(j)(k) += 1;
                  varSumArray(i)(j)(k) += ct;
                }}
              }
            }}
          }
          
        }}
        v
      }}, closeAction = (() => {
        //do nothing
        //if(sectionBy == "sampGroup"){
          countColumns.zip(countColumnNames).zipWithIndex.foreach{ case ((grp,grpName),i) => {
            val writer = openWriter(outfilePrefix + grpName + ".table.txt");
            
            writer.write("geneID\t"+kves.zipWithIndex.map{ case (kve,j) => {
                ""+kve.name+"\t"+kve.name+"\t"+kve.name
            }}.mkString("\t") + "\n")
            writer.write("geneID\t"+kves.zipWithIndex.flatMap{ case (kve,j) => {
                Seq("varCt","varSum")
            }}.mkString("\t") + "\n")
            writer.write("geneID\t"+kves.zipWithIndex.flatMap{ case (kve,j) => {
                Seq(kve.name + "_varCt",kve.name + "_varSum")
            }}.mkString("\t") + "\n")
              
            fullGeneList.zip(geneNames).zipWithIndex.foreach{ case ((gExpr,g),k) => {
              writer.write(
                  g + "\t"+  kves.zipWithIndex.map{ case (kve,j) => {
                    "" +varCtArray(i)(j)(k)+"\t"+ 
                        varSumArray(i)(j)(k)
                  }}.mkString("\t") + 
                  "\n"
              );
            }}
            
            writer.close();
          }}

                                    //  keepVarExprNames : Option[Seq[String]],
      })),vcfHeader)
    }

  }


  case class CreateVariantAFTable(keepVariantExpressions : List[String],
                                      geneList : List[String],
                                      keepVarExprNames : List[String],
                                      countColumns : List[String],
                                      countColumnNames : List[String],
                                      outfilePrefix : String,
                                      geneTag : List[String] = List[String]("SWH_ANNO_geneList_LOF"),
                                      sectionBy : String = "sampGroup",
                                      subtractGeneTags : Option[List[String]] = None
                                      ) extends SVcfWalker {
    def walkerName : String = "CreateVariantSampleTable"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("geneList",geneList.mkString("|"))
    );
    
    val keepVarFcn : Seq[SFilterLogic[SVcfVariantLine]] = keepVariantExpressions.map{ kve => {
          internalUtils.VcfTool.sVcfFilterLogicParser.parseString(kve);
    }}
    val fullGeneList =  Seq( geneList.toVector.map{ gg => {
      gg.split("[|]").last
    }}.distinct.mkString(":") ) ++ geneList.toVector.map{ gg => {
      gg.split("[|]").last
    }}
    val geneNames = Seq("allGenes") ++ geneList.toVector.map{ gg => {
      gg.split("[|]").head
    }}
    
    //"INFO.inAnyOf:"+geneTag+":"+g
    val varOnGeneFcn : Seq[SFilterLogic[SVcfVariantLine]] = fullGeneList.map{ g => {
      val kve : String = (
          Seq[String]( "("+
                 geneTag.map{ tt => {
                   "INFO.inAnyOf:"+tt+":"+g
                 }}.mkString(" OR ") + 
              ")"
          ) ++ 
          subtractGeneTags.getOrElse(List[String]()).map{ subTag => {
            " ( NOT INFO.inAnyOf:"+subTag+":"+g+" ) "
          }}
        ).mkString(" AND ")

      internalUtils.VcfTool.sVcfFilterLogicParser.parseString(kve);
    }}
    val geneListColons = fullGeneList.mkString(":")
    
    val varOnAnyGeneFcn : SFilterLogic[SVcfVariantLine] = {
     // val kve = "INFO.inAnyOf:"+geneTag+":"+geneList.mkString(":")
      val kve : String = (
          Seq[String]( "("+
                 geneTag.map{ tt => {
                   "INFO.inAnyOf:"+tt+":"+geneListColons
                 }}.mkString(" OR ") + 
              ")"
          ) ++ 
          subtractGeneTags.getOrElse(List[String]()).map{ subTag => {
            " ( NOT INFO.inAnyOf:"+subTag+":"+geneListColons+" ) "
          }}
        ).mkString(" AND ")

      internalUtils.VcfTool.sVcfFilterLogicParser.parseString(kve)
    }
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      
      val varCtArray       = Array.fill[Int](countColumns.length,keepVariantExpressions.length,fullGeneList.length)(0);
      val varSumArray      = Array.fill[Int](countColumns.length,keepVariantExpressions.length,fullGeneList.length)(0);

      (addIteratorCloseAction( vcMap(vcIter){ v => {
        //DO STUFF
        val onGene = varOnGeneFcn.map{ gFcn => gFcn.keep(v) }.zipWithIndex.filter{ case (kv,j) => kv }.map{ _._2 }
        val keepVar = keepVarFcn.map{ kFcn => kFcn.keep(v) }.zipWithIndex.filter{ case (kv,j) => kv }.map{ _._2 }
        if( onGene.nonEmpty && keepVar.nonEmpty ){
          Range(0,countColumns.length).foreach{ i => {
            val ct = v.info.get(countColumns(i)).getOrElse(None).flatMap{ xx => if(xx == ".") None else Some(xx.toInt) }.getOrElse(0)

            if(ct > 0){
              keepVar.foreach{ j => {
                onGene.foreach{ k => {
                  varCtArray(i)(j)(k) += 1;
                  varSumArray(i)(j)(k) += ct
                }}
              }}
            }
          }} 
        }
        
        /*Range(0,decoderColumns.length).foreach{ i => {
          
        }}*/
        v
      }}, closeAction = (() => {
        if(sectionBy == "sampGroup"){
          countColumns.zip(countColumnNames).zipWithIndex.foreach{ case ((cc,cname),i) => {
            val writer = openWriter(outfilePrefix + cname + ".table.txt");
            
            writer.write("geneID\t"+keepVarExprNames.zipWithIndex.map{ case (kv,j) => {
                ""+kv+"\t"+kv
            }}.mkString("\t") + "\n")
            writer.write("geneID\t"+keepVarExprNames.zipWithIndex.flatMap{ case (kv,j) => {
                Seq(kv + "_varCt",kv + "_varSum")
            }}.mkString("\t") + "\n")
              
            geneNames.zipWithIndex.foreach{ case (g,k) => {
              writer.write(
                  g + "\t"+  keepVarExprNames.zipWithIndex.map{ case (kv,j) => {
                    "" +varCtArray(i)(j)(k)+"\t"+ 
                        varSumArray(i)(j)(k)
                  }}.mkString("\t") + 
                  "\n"
              );
            }}
            
            writer.close();
          }}
        } else if(sectionBy == "varGroup"){
          
        }
                                    //  keepVarExprNames : Option[Seq[String]],
      })),vcfHeader)
    }

  }
  
  
  class CmdCreateVariantSampleTable extends CommandLineRunUtil {
     override def priority = 1;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "CreateVariantSampleTable", 
          quickSynopsis = "", 
          synopsis = "", 
          description = "" + ALPHA_WARNING,
          argList = 
                    new BinaryOptionArgument[String](
                                         name = "sampleDecoder", 
                                         arg = List("--sampleDecoder"), 
                                         valueName = "...",  
                                         argDesc =  ""
                                        ) ::
                                        
                    new BinaryOptionArgument[List[String]]( name = "calcViaAlleleCountField",
                                         arg = List("--calcViaAlleleCountField"), // name of value
                                         valueName = "ctField1,ctField2,...",
                                         argDesc = ""+
                                                   "" // description
                                         
                                       ) ::

                    new BinaryMonoToListArgument[String](
                                         name = "keepVariantExpressions", 
                                         arg = List("--keepVariantExpressions"),
                                         valueName = "",
                                         argDesc =  ""
                                        ) :: 
                    new BinaryOptionArgument[List[String]](
                                         name = "keepVarExprNames", 
                                         arg = List("--keepVarExprNames"), 
                                         valueName = "...",  
                                         argDesc =  ""
                                        ) ::
                    new BinaryOptionArgument[List[String]](
                                         name = "geneTag", 
                                         arg = List("--geneTag"), 
                                         valueName = "...",  
                                         argDesc =  ""
                                        ) ::
                    new BinaryOptionArgument[List[String]](
                                         name = "subtractGeneTags", 
                                         arg = List("--subtractGeneTags"), 
                                         valueName = "...",  
                                         argDesc =  ""
                                        ) ::
                                        
                    new BinaryOptionArgument[List[String]](
                                         name = "geneList", 
                                         arg = List("--geneList"), 
                                         valueName = "...",  
                                         argDesc =  ""
                                        ) ::
                    new BinaryOptionArgument[List[String]](
                                         name = "decoderColumns", 
                                         arg = List("--decoderColumns"), 
                                         valueName = "...",  
                                         argDesc =  ""
                                        ) ::
                    new BinaryOptionArgument[List[String]](
                                         name = "decoderColumnNames", 
                                         arg = List("--decoderColumnNames"), 
                                         valueName = "...",  
                                         argDesc =  ""
                                        ) ::
                                        
                    new BinaryOptionArgument[String](
                                         name = "outfilePrefix", 
                                         arg = List("--outfilePrefix"), 
                                         valueName = "...",  
                                         argDesc =  ""
                                        ) ::
                                        
                    new BinaryOptionArgument[String](
                                         name = "gtTag", 
                                         arg = List("--gtTag"), 
                                         valueName = "...",  
                                         argDesc =  ""
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "sectionBy", 
                                         arg = List("--sectionBy"), 
                                         valueName = "...",  
                                         argDesc =  ""
                                        ) ::
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
                                       
                    new FinalArgument[String](
                                         name = "infile",
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
         
         parser.get[Option[List[String]]]("calcViaAlleleCountField") match {
           case Some(alleCtFields) => {
             
             /*
              * keepVariantExpressions : List[String],
                                      geneList : List[String],
                                      keepVarExprNames : List[String],
                                      countColumns : List[String],
                                      countColumnNames : List[String],
                                      outfilePrefix : String,
                                      geneTag : List[String] = List[String]("SWH_ANNO_geneList_LOF"),
                                      sectionBy : String = "sampGroup",
                                      subtractGeneTags : Option[List[String]] = None
                                      
              */
             
              val cvat = CreateVariantAFTable( keepVariantExpressions = parser.get[List[String]]("keepVariantExpressions"),
                  geneList = parser.get[Option[List[String]]]("geneList").get,
                  keepVarExprNames = parser.get[Option[List[String]]]("keepVarExprNames").get,
                  countColumns = alleCtFields,
                  countColumnNames = alleCtFields,
                  outfilePrefix = parser.get[Option[String]]("outfilePrefix").get,
                  geneTag = parser.get[Option[List[String]]]("geneTag").getOrElse(List[String]("SWH_ANNO_geneList_LOF")),
                  sectionBy = parser.get[Option[String]]("sectionBy").getOrElse("sampGroup"),
                  subtractGeneTags = parser.get[Option[List[String]]]("subtractGeneTags")
              )
             
              cvat.walkVCFFiles(
                 infiles = parser.get[String]("infile"),
                 outfile = parser.get[String]("outfile"),
                 chromList = parser.get[Option[List[String]]]("chromList"),
                 numLinesRead = None,
                 inputFileList = parser.get[Boolean]("inputFileList"),
                 dropGenotypes = false
             )
           }
           case None => {
             new CreateVariantSampleTable_OLD(
                           sampleDecoder = parser.get[Option[String]]("sampleDecoder").get, 
                            keepVariantExpressions = parser.get[List[String]]("keepVariantExpressions"),
                            geneList = parser.get[Option[List[String]]]("geneList").get,
                            decoderColumns = parser.get[Option[List[String]]]("decoderColumns").get,
                            decoderColumnNames = parser.get[Option[List[String]]]("decoderColumnNames"),
                            //keepVarExprNames : Option[Seq[String]],
                            keepVarExprNames = parser.get[Option[List[String]]]("keepVarExprNames").get,
                            outfilePrefix = parser.get[Option[String]]("outfilePrefix").get,
                            geneTag = parser.get[Option[List[String]]]("geneTag").getOrElse(List[String]("SWH_ANNO_geneList_LOF")),
                            gtTag  =  parser.get[Option[String]]("gtTag").getOrElse("GT"),
                            sectionBy  = parser.get[Option[String]]("sectionBy").getOrElse("sampGroup"),
                            subtractGeneTags  = parser.get[Option[List[String]]]("subtractGeneTags")
             ).walkVCFFiles(
                 infiles = parser.get[String]("infile"),
                 outfile = parser.get[String]("outfile"),
                 chromList = parser.get[Option[List[String]]]("chromList"),
                 numLinesRead = None,
                 inputFileList = parser.get[Boolean]("inputFileList"),
                 dropGenotypes = false
             )
           }
         }
       }
     }
    
  }
  

  
  case class CreateVariantSampleTable_OLD(sampleDecoder : String, 
                                      keepVariantExpressions : Seq[String],
                                      geneList : Seq[String],
                                      decoderColumns : Seq[String],
                                      decoderColumnNames : Option[Seq[String]],
                                      //keepVarExprNames : Option[Seq[String]],
                                      keepVarExprNames : Seq[String],
                                      outfilePrefix : String,
                                      geneTag : List[String] = List[String]("SWH_ANNO_geneList_LOF"),
                                      gtTag : String = "GT",
                                      sectionBy : String = "sampGroup",
                                      subtractGeneTags : Option[List[String]] = None
                                      ) extends SVcfWalker {
    def walkerName : String = "CreateVariantSampleTable"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("sampleDecoder",sampleDecoder),
        ("geneList",geneList.mkString("|"))
    );
    
    val keepVarFcn : Seq[SFilterLogic[SVcfVariantLine]] = keepVariantExpressions.map{ kve => {
          internalUtils.VcfTool.sVcfFilterLogicParser.parseString(kve);
    }}
    val fullGeneList =  Seq( geneList.toVector.map{ gg => {
      gg.split("[|]").last
    }}.distinct.mkString(":") ) ++ geneList.toVector.map{ gg => {
      gg.split("[|]").last
    }}
    val geneNames = Seq("allGenes") ++ geneList.toVector.map{ gg => {
      gg.split("[|]").head
    }}
    
    //"INFO.inAnyOf:"+geneTag+":"+g
    val varOnGeneFcn : Seq[SFilterLogic[SVcfVariantLine]] = fullGeneList.map{ g => {
      val kve : String = (
          Seq[String]( "("+
                 geneTag.map{ tt => {
                   "INFO.inAnyOf:"+tt+":"+g
                 }}.mkString(" OR ") + 
              ")"
          ) ++ 
          subtractGeneTags.getOrElse(List[String]()).map{ subTag => {
            " ( NOT INFO.inAnyOf:"+subTag+":"+g+" ) "
          }}
        ).mkString(" AND ")

      internalUtils.VcfTool.sVcfFilterLogicParser.parseString(kve);
    }}
    val geneListColons = fullGeneList.mkString(":")
    
    val varOnAnyGeneFcn : SFilterLogic[SVcfVariantLine] = {
     // val kve = "INFO.inAnyOf:"+geneTag+":"+geneList.mkString(":")
      val kve : String = (
          Seq[String]( "("+
                 geneTag.map{ tt => {
                   "INFO.inAnyOf:"+tt+":"+geneListColons
                 }}.mkString(" OR ") + 
              ")"
          ) ++ 
          subtractGeneTags.getOrElse(List[String]()).map{ subTag => {
            " ( NOT INFO.inAnyOf:"+subTag+":"+geneListColons+" ) "
          }}
        ).mkString(" AND ")

      internalUtils.VcfTool.sVcfFilterLogicParser.parseString(kve)
    }
    val decoderLines = getLinesSmartUnzip(sampleDecoder)
    val titleLine = decoderLines.next.split("\t");
    val decoderArray = decoderLines.toArray.map{ line => line.split("\t")};
    
    val sampLists : Seq[(String,Set[String])] = decoderColumns.zip{ decoderColumnNames.getOrElse(decoderColumns) }.flatMap{ case (colName,colTitle) => {
      val colIdx = titleLine.indexOf(colName);
      if(colIdx == -1){
        error("Column not found in decoder file: \""+colName+"\"\n Found columns: [\""+titleLine.mkString("\",\"")+"\"]\n"+"\n in file: "+sampleDecoder);
      }
      val valueList = decoderArray.map{cells => {
        cells(colIdx)
      }}.toSet.filter{ c => c != "0" }
      
      valueList.toVector.sorted.map{ xx => {
        val outName = if(xx == "1"){
          ""
        } else {
          "_"+xx
        }
        (colTitle+outName,decoderArray.withFilter{cells => {
          cells(colIdx) == xx
        }}.map{_.head}.toSet)
      }}
    }}
    
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val samps = vcfHeader.titleLine.sampleList;
      val sampGroups = sampLists.unzip._1;
      
      val varCtArray       = Array.fill[Int](sampGroups.length,keepVariantExpressions.length,fullGeneList.length)(0);
      val varSampCtArray   = Array.fill[Int](sampGroups.length,keepVariantExpressions.length,fullGeneList.length)(0);
      val varSampCtArrayHom   = Array.fill[Int](sampGroups.length,keepVariantExpressions.length,fullGeneList.length)(0);
      val varSampBoolArray = Array.fill[Boolean](sampGroups.length,keepVariantExpressions.length,fullGeneList.length, samps.length)(false);
      //val varSampVctArray = Array.fill[Int](sampGroups.length,keepVariantExpressions.length,geneList.length, samps.length)(0);

      val anyVarCtArray       = Array.fill[Int](sampGroups.length,keepVariantExpressions.length)(0);
      val anyVarSampCtArray   = Array.fill[Int](sampGroups.length,keepVariantExpressions.length)(0);
      val anyVarSampCtArrayHom   = Array.fill[Int](sampGroups.length,keepVariantExpressions.length)(0);
      val anyVarSampBoolArray = Array.fill[Boolean](sampGroups.length,keepVariantExpressions.length, samps.length)(false);
      val anyVarSampVctArray = Array.fill[Int](samps.length,keepVariantExpressions.length)(0);
      
      val sampIdx : Seq[Array[Boolean]] = sampLists.map{ case (setName, subsamps) => {
        samps.toArray.map{ s => subsamps.contains(s) }
      }}
      
      //sampIdx.zip(sampGroups).foreach{ case (idx,grp) => {
      //  reportln(grp+"\t"+idx.map{xx => if(xx) "1" else "0"}.mkString(""),"debug");
      //}}

      (addIteratorCloseAction( vcMap(vcIter){ v => {
        //DO STUFF
        val onGene = varOnGeneFcn.map{ gFcn => gFcn.keep(v) }.zipWithIndex.filter{ case (kv,j) => kv }.map{ _._2 }
        val keepVar = keepVarFcn.map{ kFcn => kFcn.keep(v) }.zipWithIndex.filter{ case (kv,j) => kv }.map{ _._2 }
        if( onGene.nonEmpty && keepVar.nonEmpty ){
          val gtIdx = v.genotypes.fmt.indexOf(gtTag);
          if(gtIdx == -1){
            error("Genotype tag: \""+gtTag+"\" not found!");
          }
          val anyAlt = v.genotypes.genotypeValues(gtIdx).map{ gtString => {
            gtString.contains('1');
          }}
          val anyHom = v.genotypes.genotypeValues(gtIdx).map{ gtString => gtString == "1/1" }
          anyAlt.zipWithIndex.withFilter{_._1}.foreach{ case (isAlt,k) => { 
            keepVar.foreach{ j => {
              anyVarSampVctArray(k)(j) += 1;
            }}
          }}
          Range(0,sampIdx.length).foreach{ i => {
            val isAlt = anyAlt.zip{sampIdx(i)}.zipWithIndex.filter{ case ((ia,ib),z) => ia && ib }.map{_._2}
            val isHom = anyHom.zip{sampIdx(i)}.zipWithIndex.filter{ case ((ia,ib),z) => ia && ib }.map{_._2}
            val numAlt = isAlt.length
            val numHom = isHom.length;
            if(numAlt > 0){
              keepVar.foreach{ j => {
                anyVarCtArray(i)(j) += 1;
                anyVarSampCtArray(i)(j) += numAlt;
                anyVarSampCtArrayHom(i)(j) += numHom
                isAlt.foreach{ z => {
                  anyVarSampBoolArray(i)(j)(z) = true;
                  //anyVarSampVctArray(i)(j)(z) += 1;
                }}
                onGene.foreach{ k => {
                  varCtArray(i)(j)(k) += 1;
                  varSampCtArray(i)(j)(k) += numAlt
                  varSampCtArrayHom(i)(j)(k) += numHom
                  isAlt.foreach{ z => {
                    varSampBoolArray(i)(j)(k)(z) = true
                    //varSampVctArray(i)(j)(k)(z) += 1;
                  }}
                }}
              }}
            }
          }} 
        }
        
        /*Range(0,decoderColumns.length).foreach{ i => {
          
        }}*/
        v
      }}, closeAction = (() => {
        //do nothing
        val sampwriter = openWriter(outfilePrefix + "sampVarCt" + ".table.txt");
        sampwriter.write( "samp.ID\t"+keepVarExprNames.mkString("\t")+"\n");
        samps.zipWithIndex.foreach{ case (s,k) => {
          sampwriter.write( s + "\t"+ anyVarSampVctArray(k).mkString("\t") +"\n" );
        }}
        sampwriter.close();
        
        
        if(sectionBy == "sampGroup"){
          sampGroups.zip(sampIdx).zipWithIndex.foreach{ case ((dd,sampBools),i) => {
            val writer = openWriter(outfilePrefix + dd + ".table.txt");
            
            writer.write(dd+"\t"+sampBools.count{x => x}+"\n");
            writer.write("geneID\t"+keepVarExprNames.zipWithIndex.map{ case (kv,j) => {
                ""+kv+"\t-\t-\t-\t-"
            }}.mkString("\t") + "\n")
            writer.write("geneID\t"+keepVarExprNames.zipWithIndex.flatMap{ case (kv,j) => {
                Seq(kv + "_varCt",kv + "_altGenoCt", kv + "_sampVarCt",kv +"_homGenoCt",kv+"_alleCt")
            }}.mkString("\t") + "\n")
            writer.write("anyGene\t"+ keepVarExprNames.zipWithIndex.map{ case (kv,j) => {
                "" +anyVarCtArray(i)(j)+"\t"+ anyVarSampCtArray(i)(j)+"\t"+anyVarSampBoolArray(i)(j).count{x => x}+"\t"+ anyVarSampCtArrayHom(i)(j)+"\t"+(anyVarSampCtArray(i)(j)+anyVarSampCtArrayHom(i)(j))
            }}.mkString("\t") + "\n")
              
              
            geneNames.zipWithIndex.foreach{ case (g,k) => {
              writer.write(
                  g + "\t"+  keepVarExprNames.zipWithIndex.map{ case (kv,j) => {
                    "" +varCtArray(i)(j)(k)+"\t"+ 
                        varSampCtArray(i)(j)(k)+"\t"+
                        varSampBoolArray(i)(j)(k).count{x => x}+"\t"+
                        varSampCtArrayHom(i)(j)(k)+"\t"+
                        (varSampCtArrayHom(i)(j)(k)+varSampCtArray(i)(j)(k))
                  }}.mkString("\t") + 
                  "\n"
              );
            }}
            
            writer.close();
          }}
        } else if(sectionBy == "varGroup"){
          
           keepVarExprNames.zipWithIndex.map{ case (kv,j) => {
             val writer = openWriter(outfilePrefix + kv + ".table.txt");
              writer.write("geneID\t"+sampGroups.zipWithIndex.flatMap{ case (dd,i) => {
                Seq(dd + "_varCt",dd + "_altGenoCt", dd + "_sampVarCt",dd +"_homGenoCt",dd+"_alleCt")
              }}.mkString("\t") + "\n")
              writer.write("sampleCts\t"+sampGroups.zip(sampIdx).zipWithIndex.flatMap{ case ((dd,sampBools),i) => {
                Seq(""+sampBools.count{x => x},"-","-","-","-")
              }}.mkString("\t") + "\n")
              writer.write("anyGene\t"+ sampGroups.zipWithIndex.map{ case (dd,i) => {
                "" +anyVarCtArray(i)(j)+"\t"+ anyVarSampCtArray(i)(j)+"\t"+anyVarSampBoolArray(i)(j).count{x => x}+"\t"+ anyVarSampCtArrayHom(i)(j)+"\t"+(anyVarSampCtArray(i)(j)+anyVarSampCtArrayHom(i)(j))
              }}.mkString("\t") + "\n")
              
              geneNames.zipWithIndex.foreach{ case (g,k) => {
                writer.write(
                  g + "\t"+  sampGroups.zipWithIndex.map{ case (dd,i) => {
                    "" +varCtArray(i)(j)(k)+"\t"+ 
                        varSampCtArray(i)(j)(k)+"\t"+
                        varSampBoolArray(i)(j)(k).count{x => x}+"\t"+
                        varSampCtArrayHom(i)(j)(k)+"\t"+
                        (varSampCtArrayHom(i)(j)(k)+varSampCtArray(i)(j)(k))
                  }}.mkString("\t") + 
                  "\n"
                );
              }}
             writer.close();
           }}
        }
                                    //  keepVarExprNames : Option[Seq[String]],
      })),vcfHeader)
    }

  }
  
  
  

  case class CopyFieldsToInfo(qualTag : Option[String], filterTag : Option[String], idTag : Option[String]) extends SVcfWalker {
    def walkerName : String = "localGcInfoWalker"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("qualTag",   qualTag.getOrElse("None")),
        ("filterTag", filterTag.getOrElse("None")),
        ("idTag", idTag.getOrElse("None"))
    );
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      qualTag.map{ tagID => {
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",tagID,Number="1",Type="Float",desc="QUAL field"));
      }}
      filterTag.map{ tagID => {
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",tagID,Number="1",Type="Float",desc="FILTER field"));
      }}
      idTag.map{ tagID => {
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",tagID,Number="1",Type="Float",desc="ID field"));
      }}
      val overwriteInfos : Set[String] = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( outHeader.addedInfos );
      if( overwriteInfos.nonEmpty ){
        notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      }
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine();
        //vc.dropInfo(overwriteInfos);
        qualTag.map{ tagID => {
          vc.addInfo(tagID, v.qual)
        }}
        filterTag.map{ tagID => {
          vc.addInfo(tagID, v.filter)
        }}
        idTag.map{ tagID => {
          vc.addInfo(tagID, v.id)
        }}
        vc
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
      //vc.dropInfo(overwriteInfos);
    }
  }

  
  case class localGcInfoWalker(tagPrefix : String, windows : Seq[Int], genomeFa : String, roundDigits : Option[Int] = None) extends SVcfWalker {
    val digits = roundDigits.getOrElse(5);
    val fmtString =  "%1."+digits+"f";
    
    def walkerName : String = "localGcInfoWalker"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("tagPrefix", tagPrefix),
        ("windows", "" + windows.mkString(":"))
    );
    
    val refFastaTool = internalUtils.GatkPublicCopy.refFastaTool(genomeFa = genomeFa);
    val maxWin = windows.max
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      windows.foreach{ currWin => {
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",tagPrefix+"_gcPct_"+currWin,Number="1",Type="Float",desc="GC percentage found within "+currWin+" bases in either direction. (note: If the variant is an insdel or a deletion, it will count the GC from the center of the ref allele. Also: does not count N bases.)"));
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",tagPrefix+"_nPct_"+currWin,Number="1",Type="Float",desc="Reference-N percentage found within "+currWin+" bases in either direction. (note: If the variant is an insdel or a deletion, it will count the GC from the center of the ref allele.)"));
      }}
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        //val vc = v.getOutputLine()
        
        val vc = v.getOutputLine();
        //vc.addInfo(tagPrefix,v.chrom+":"+v.pos+","+v.ref+","+v.alt.mkString(","))
        val ctrPos = v.pos + (v.ref.length / 2)
        val maxseq = refFastaTool.getBasesForIv(chrom = v.chrom,start = ctrPos - maxWin, end = ctrPos + maxWin);
        
        windows.foreach{ currWin => {
          val winDiff = maxWin - currWin;
          val currSeq = maxseq.slice(winDiff,maxseq.length - winDiff);
          val nonMissCt = currSeq.count{_ != 'N'}.toDouble
          val missPct = 1 - (nonMissCt / currSeq.length)
          val gcCt = currSeq.count{xx => xx == 'G' || xx == 'C'}.toDouble
          val gcPct = gcCt / nonMissCt;
          
          vc.addInfo(tagPrefix+"_gcPct_"+currWin, fmtString.format(gcPct));
          vc.addInfo(tagPrefix+"_nPct_"+currWin, fmtString.format(missPct));
        }}
        
        vc
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
      
    }
  }
   
  
  case class AddVariantPosInfoWalker(tagPrefix : String = "RAWVARIANT") extends SVcfWalker {
    def walkerName : String = "AddVariantPosWalker"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("tagPrefix", tagPrefix)
    );
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      var errCt = 0;
      
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      var idx = 0;
      outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",tagPrefix,Number=".",Type="String",desc="Original Chrom:Pos:Alle from VCF line"));

      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        //val vc = v.getOutputLine()
        
        val vc = v.getOutputLine();
        vc.addInfo(tagPrefix,v.chrom+":"+v.pos+","+v.ref+","+v.alt.mkString(","))
        idx = idx + 1;
        vc
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
      
    }
  }
   
  class FilterSymbolicAlleleLines() extends SVcfWalker {
    def walkerName : String = "FilterSymbolicAlleleLines"
    
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)]();
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader;
      outHeader.addWalk(this);
      var dropct = 0;
      (addIteratorCloseAction( iter = vcFlatMap(vcIter){v => {        
        if(v.ref.startsWith("<") || v.alt.exists{ a => a.startsWith("<")}){
          dropct = dropct + 1;
          notice("Dropped symbolic allele variant:\n    "+v.getSimpleVcfString(),"Dropped_Symbolic_Variant",10);
          None          
        } else {
          Some(v);
        }
      }}, closeAction = (() => {
        reportln("Dropped "+dropct+" variant/allele lines due to the presence of symbolic alleles","note");
      })),outHeader)
    }
  }
  class MergeBooleanTags(tagID : String, mergeTags : List[String], tagNames : Option[List[String]]) extends SVcfWalker {
    def walkerName : String = "MergeBooleanTags"
    val tagTitles = tagNames.getOrElse(mergeTags);
    
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
       ("tagID",tagID),
       ("mergeTags",mergeTags.mkString(",")),
       ("tagNames",tagTitles.mkString(","))
    );
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader;
      outHeader.addWalk(this);
      outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",tagID,Number=".",Type="String",desc="Merge of boolean tags: "+mergeTags.mkString(",")));
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {        
        val vc = v.getOutputLine();
        
        val tagListString = mergeTags.zip(tagTitles).withFilter{ case (t,tt) => {
          val vv : String = v.info.getOrElse(t,None).getOrElse(".")
          vv == "1"
        }}.map{ case (t,tt) => {
          tt
        }}.padTo(1,".").mkString(",")
         
        vc.addInfo(tagID,tagListString);
        
        vc
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
    }
  }

   
  class EditInfoAndFormatTags(tagList : Seq[String], subtype : Option[String] = None, num : Option[String] = None,typ : Option[String] = None) extends SVcfWalker {
    def walkerName : String = "EditInfoAndFormatTags"
    
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)]();
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader;
      outHeader.addWalk(this);
      //var dropct = 0;
      vcfHeader.infoLines.filter{hl => tagList.contains(hl.ID)}.foreach{ hl => {
        var newHeaderLine = subtype.map{st => hl.updateSubtype(Some(st))}.getOrElse(hl)
        newHeaderLine = num.map{n => hl.updateNumber(n)}.getOrElse(newHeaderLine);
        newHeaderLine = typ.map{t => hl.updateType(t)}.getOrElse(newHeaderLine);
        outHeader.addInfoLine(newHeaderLine);
      }}
      vcfHeader.formatLines.filter{hl => tagList.contains(hl.ID)}.foreach{ hl => {
        var newHeaderLine = subtype.map{st => hl.updateSubtype(Some(st))}.getOrElse(hl)
        newHeaderLine = num.map{n => hl.updateNumber(n)}.getOrElse(newHeaderLine);
        newHeaderLine = typ.map{t => hl.updateType(t)}.getOrElse(newHeaderLine);
        outHeader.addFormatLine(newHeaderLine);
      }}
      (vcIter,outHeader)
    }
  }
  


  class AddFuncTag(func : String, newTag : String, paramTags : Seq[String], digits : Option[Int] = None, desc : Option[String] = None ) extends internalUtils.VcfTool.SVcfWalker { 
    def walkerName : String = "AddFuncTag."+newTag
    val f : String = func.toUpperCase;
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("newTag",newTag),
        ("func",func),
        ("paramTags",paramTags.mkString("|"))
    );
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      var errCt = 0;
      
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);

      val paramTypes = paramTags.zipWithIndex.map{ case (param,pidx) => {
        vcfHeader.infoLines.find(infoline => infoline.ID == param) match {
          case Some(paramLine) => {
            paramLine.Type
          }
          case None => {
            if(! (Set("STATICSET.INTERSECT").contains(func) && pidx > 0)){
              warning("WARN: cannot find tag: "+param)
            }
            "?"
          }
        }
      }}
      val outType = if(! Set("SUM","MIN","MAX","DIFF").contains(f)){
        "String"
      } else if(paramTypes.forall(pt => pt == "Integer")){
        "Integer"
      } else {
        "Float"
      }
      val outNum = if(f.startsWith("SETS.") || f.startsWith("STATICSET.")){
        "."
      } else {
        "1"
      }
      reportln("TAG: "+newTag+" will be of type: "+outType + " and length: "+outNum,"note");
      if(f == "DIFF" && paramTags.length != 2){
        error("ERROR: function DIFF requires exactly 2 params");
      }
      
      outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",newTag,Number=outNum,Type=outType,desc=desc.getOrElse("Result of performing function "+func+" on tags: "+paramTags.mkString(",")+".")));
      val overwriteInfos : Set[String] = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( outHeader.addedInfos );
      if( overwriteInfos.nonEmpty ){
        notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      }
      
      
      //def getInfo(vv : SVcfVariantLine, tt : String) : Option[
      
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine();
        vc.dropInfo(overwriteInfos);
        if(f == "SUM"){
          if(outType == "Integer"){
            val paramVals : Seq[Int] = paramTags.flatMap{ param => {
              v.info.get(param).getOrElse(None)
            }}.filter(paramVal => paramVal != ".").map{ paramVal => {
              paramVal.toInt
            }}
            if(paramVals.nonEmpty){
              vc.addInfo(newTag, ""+paramVals.sum);
            } else {
              vc.addInfo(newTag, ".");
            }
          } else {
            val paramVals : Seq[Double] = paramTags.flatMap{ param => {
              v.info.get(param).getOrElse(None)
            }}.filter(paramVal => paramVal != ".").map{ paramVal => {
              paramVal.toDouble
            }}
            if(paramVals.nonEmpty){
              digits match {
                case Some(dd) => {
                  vc.addInfo(newTag, ("%."+dd+"f").format(paramVals.sum) );
                }
                case None => {
                  vc.addInfo(newTag, ""+paramVals.sum);
                }
              }
            } else {
              vc.addInfo(newTag, ".");
            }
          }
        } else if(f == "DIFF"){
          if(outType == "Integer"){
            val paramVals : Seq[Int] = paramTags.flatMap{ param => {
              v.info.get(param).getOrElse(None)
            }}.filter(paramVal => paramVal != ".").map{ paramVal => {
              paramVal.toInt
            }}
            if(paramVals.length == 2){
              vc.addInfo(newTag, ""+ (paramVals(0) - paramVals(1)));
            } else {
              vc.addInfo(newTag, ".");
            }
          } else {
            val paramVals : Seq[Double] = paramTags.flatMap{ param => {
              v.info.get(param).getOrElse(None)
            }}.filter(paramVal => paramVal != ".").map{ paramVal => {
              paramVal.toDouble
            }}
            if(paramVals.length == 2){
              digits match {
                case Some(dd) => {
                  vc.addInfo(newTag, ("%."+dd+"f").format(paramVals(0) - paramVals(1)) );
                }
                case None => {
                  vc.addInfo(newTag, ""+(paramVals(0) - paramVals(1)));
                }
              }
            } else {
              vc.addInfo(newTag, ".");
            }
          }
        } else if(f == "STATICSET.INTERSECT"){
          val param = paramTags.head;
          val staticSet = paramTags.tail.toSet;
          val paramVal =  v.info.get(param).getOrElse(None).filter{ ss => ss != "." }.map{ ss => ss.split("[,|]").toSet }.getOrElse(Set[String]())
          vc.addInfo(newTag, (paramVal.intersect(staticSet)).toVector.sorted.padTo(1,".").mkString(","));
        } else if(f == "SETS.DIFF"){
            val paramVals : Seq[Set[String]] = paramTags.map{ param => {
              v.info.get(param).getOrElse(None).filter{ ss => ss != "." }.map{ ss => ss.split("[,|]").toSet }.getOrElse(Set[String]())
            }}
            vc.addInfo(newTag, (paramVals(0) -- paramVals(1)).toVector.sorted.padTo(1,".").mkString(","));
        } else if(f == "SETS.UNION"){
            val paramVals : Set[String] = paramTags.flatMap{ param => {
              v.info.get(param).getOrElse(None).filter{ ss => ss != "." }.map{ ss => ss.split("[,|]").toSet }.getOrElse(Set[String]())
            }}.toSet
            vc.addInfo(newTag, (paramVals).toVector.sorted.padTo(1,".").mkString(","));
        } else if(f == "SETS.INTERSECT"){
            val paramVals : Seq[Set[String]] = paramTags.map{ param => {
              v.info.get(param).getOrElse(None).filter{ ss => ss != "." }.map{ ss => ss.split("[,|]").toSet }.getOrElse(Set[String]())
            }}
            val intersect = paramTags.tail.foldLeft(paramTags.head){ case (soFar,curr) => { soFar.intersect(curr) }}
            vc.addInfo(newTag, (intersect).toVector.sorted.padTo(1,".").mkString(","));
        } else if(f == "MIN"){
          error("MIN function not yet implemented")
        } else if(f == "MAX"){
          error("MAX function not yet implemented")
        } else {
          error("Unrecognized function: " +f);
        }
        
        /*v.info.getOrElse(nTag,None).foreach{ nStr => {
          v.info.getOrElse(dTag,None).foreach{ dStr => {
            if(nStr == "." || dStr == "."){
              vc.addInfo(newTag, "0");
            } else {
              val (n,d) = (string2float(nStr),string2float(dStr));
              val ratio = n/d
              vc.addInfo(newTag, ("%."+digits+"f").format(ratio));
            }

          }}
        }}*/
         
        vc
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
      
    }
  }
  
  
  class AddRatioTag(newTag : String, nTag : String, dTag : String, digits : Int = 4, desc : Option[String] = None ) extends internalUtils.VcfTool.SVcfWalker { 
    def walkerName : String = "AddRatioTag."+newTag
    
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("newTag",newTag),
        ("nTag",nTag),
        ("dTag",dTag)
    );
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      var errCt = 0;
      
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",newTag,Number="1",Type="Float",desc=desc.getOrElse("Simple ratio between fields "+nTag+" and "+dTag+".")));
      val overwriteInfos : Set[String] = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( outHeader.addedInfos );
      if( overwriteInfos.nonEmpty ){
        notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      }
      
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine();
        vc.dropInfo(overwriteInfos);
        v.info.getOrElse(nTag,None).foreach{ nStr => {
          v.info.getOrElse(dTag,None).foreach{ dStr => {
            if(nStr == "." || dStr == "."){
              vc.addInfo(newTag, "0");
            } else {
              val (n,d) = (string2float(nStr),string2float(dStr));
              val ratio = n/d
              vc.addInfo(newTag, ("%."+digits+"f").format(ratio));
            }

          }}
        }}
         
        vc
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
      
    }
  }
  
  
  class AddVariantIdx(tag : String, desc : String = "unique variant index", idxPrefix : Option[String] = None) extends internalUtils.VcfTool.SVcfWalker { 
    def walkerName : String = "AddVariantIdx"
    
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("tag",tag),
        ("idxPrefix",idxPrefix.getOrElse("None"))
    );
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      var errCt = 0;
      
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      var idx = 0;
      if(idxPrefix.isEmpty){
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",tag,Number="1",Type="Integer",desc=desc));
      } else {
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",tag,Number="1",Type="String",desc=desc));
      }

      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        //val vc = v.getOutputLine()
        
        val vc = v.getOutputLine();
        vc.addInfo(tag,idxPrefix.getOrElse("")+idx)
        idx = idx + 1;
        vc
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
      
    }
  }
  

  class CopyFmtTag(oldTag : String, newTag : String, overWriteBlanks : Boolean) extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "CopyFmtTag"
    
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("oldTag",oldTag),
        ("newTag",newTag)
    );
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      var errCt = 0;
      
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      var idx = 0;
      val ohl = vcfHeader.formatLines.find( xx => xx.ID == oldTag ).get
      
      outHeader.addFormatLine(new SVcfCompoundHeaderLine("FORMAT",newTag,Number=ohl.Number,Type=ohl.Type,desc=ohl.desc));

      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        //val vc = v.getOutputLine()
        
        val vc = v.getOutputLine();
        val fmtIdx = v.genotypes.fmt.indexOf(oldTag)
        if(fmtIdx >= 0){
          vc.genotypes.addGenotypeArray(newTag, v.genotypes.genotypeValues(fmtIdx) )
        } else if(overWriteBlanks){
          error("OPTION NOT IMPLEMENTED!");
        }
        
        idx = idx + 1;
        vc
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
      
    }
  }
  


  class CmdExpandIntervar extends CommandLineRunUtil {
     override def priority = 1;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "CmdExtractIntervarField", 
          quickSynopsis = "", 
          synopsis = "", 
          description = "" + ALPHA_WARNING,
          argList = 
                    new BinaryOptionArgument[String](
                                         name = "tagPrefix", 
                                         arg = List("--tagPrefix"), 
                                         valueName = "...",  
                                         argDesc =  ""
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "intervarTag", 
                                         arg = List("--intervarTag"), 
                                         valueName = "...",  
                                         argDesc =  ""
                                        ) ::
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
                                       
                    new FinalArgument[String](
                                         name = "infile",
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
         new IntervarExtract(
                       intervarTag = parser.get[Option[String]]("intervarTag").get,
                       tagPrefix = parser.get[Option[String]]("tagPrefix").get
         ).walkVCFFiles(
             infiles = parser.get[String]("infile"),
             outfile = parser.get[String]("outfile"),
             chromList = parser.get[Option[List[String]]]("chromList"),
             numLinesRead = None,
             inputFileList = parser.get[Boolean]("inputFileList"),
             dropGenotypes = false
         )
       }
     }
    
  }
  
  class IntervarExtract(intervarTag : String, tagPrefix : String) extends internalUtils.VcfTool.SVcfWalker { 
    def walkerName : String = "IntervarExtractWalker"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("intervarTag",intervarTag),
        ("tagPrefix",tagPrefix)
    );
    val intervarPathStringPairs = Vector[(String,String)](
          ("Pathogenic_","P"), 
          ("Likely_pathogenic_","LP"),
          ("Uncertain_significance_","VUS"),
          ("Likely_benign_","LB"),
          ("Benign_","B")
        )
    
    val critList = Vector[String](
          "PVS","PS",
          "PM","PP",
          "BA","BS","BP"
        )
    val critCts = Vector[(String,Int)](
          ("PVS",1),("PS",5),
          ("PM",7),("PP",6),
          ("BA",1),("BS",5),("BP",8)
        )
        
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      
      critCts.foreach{ case (crit,ct) => {
        Range(0,ct).foreach{ c => {
          outHeader.addInfoLine(
            new SVcfCompoundHeaderLine("FORMAT" ,ID = tagPrefix+crit+"_"+(c+1), ".", "Integer", "Criteria "+crit+(c+1)+" from Intervar field "+intervarTag).addWalker(this)
          )
        }}
      }}
          outHeader.addInfoLine(
            new SVcfCompoundHeaderLine("FORMAT" ,ID = tagPrefix+"CALL", ".", "String", "Final call from Intervar field "+intervarTag).addWalker(this)
          )
          
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine();
        
        
        v.info.getOrElse(intervarTag,None).foreach{ intervarFullString => {
          
          intervarFullString.split(",").zipWithIndex.foreach{ case (intervarString,intervarIdx) => {
          
            var currString = intervarString;
            if(! currString.startsWith("Intervar_")){
              warning("Intervar field does not begin with \"Intervar\". Intervar field will NOT be properly extracted. Errors may follow. Field = \""+intervarString+"\"","Intervar_Fmt_Error",10)
            }
            currString = currString.drop(9);
            
            val pathChar = intervarPathStringPairs.find{ case (pathString,pathChar) => {
              currString.startsWith(pathString);
            }} match {
              case None => {
                error("Intervar field does not contain a pathogenicity call. Field = \""+intervarString+"\"")
                "ERROR"
              }
              case Some((pathString,pathChar)) => {
                currString = currString.drop(pathString.length);
                 val currTag = tagPrefix+"CALL"
                if(intervarIdx == 0){
                    vc.addInfo(currTag,pathChar)
                } else {
                      val currVal = vc.info.getOrElse(currTag,None).getOrElse("???")
                      vc.addInfo(currTag,currVal +","+pathChar)
                }
                pathChar
              }
            }
            currString = currString.replace("PVS1","PVS_").replace("BA1","BA_");
            val cells = currString.split("_").tail;
            var currGrp = "";
            var currCt = 1;
            

            cells.foreach{ c => {
              if( critList.contains(c) ){
                currGrp = c;
                currCt = 1;
              } else {
                val currTag = tagPrefix+currGrp+"_"+currCt
                if(intervarIdx == 0){
                  vc.addInfo(currTag,c)
                } else {
                  val currVal = vc.info.getOrElse(currTag,None).getOrElse("???")
                  vc.addInfo(currTag,currVal +","+c)
                }
              }
            }}
          }}
        }}
        
        vc;
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
    }

  }
  
  
  
  class SnpEffInfoExtract(tagID : String = "ANN", 
                          tagPrefix : String = "ANNEX_",
                          geneList : Option[List[String]] = None,
                          snpEffBiotypeKeepList : Option[List[String]] = None,
                          snpEffEffectKeepList : Option[List[String]] = None,
                          snpEffWarningDropList : Option[List[String]] = None,
                          snpEffKeepIdx : Option[List[String]] = None,
                          geneListName : Option[String] = None
                          ) extends internalUtils.VcfTool.SVcfWalker { 
    
    
    def walkerName : String = "SnpEffInfoExtract"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("tagID",tagID),
        ("tagPrefix",tagPrefix),
        ("snpEffBiotypeKeepList",snpEffBiotypeKeepList.map{ s => s.mkString("|") }.getOrElse("None")),
        ("snpEffEffectKeepList",snpEffEffectKeepList.map{ s => s.mkString("|") }.getOrElse("None")),
        ("snpEffWarningDropList",snpEffWarningDropList.map{ s => s.mkString("|") }.getOrElse("None"))
    );
    def walkerInfo : SVcfWalkerInfo = StdVcfConverter;
    
    val annidxAllele = 0
    val annidxEffect = 1 
    val annidxImpact = 2
    val annidxGeneName = 3
    val annidxGeneID  = 4
    val annidxTxType = 5
    val annidxTxID = 6
    val annidxTxBiotype=7
    val annidxRank = 8
    val annidxCdot= 9
    val annidxPdot=10
    val annidxCpos = 11
    val annidxWarn=15
    
    val snpEffBiotypeKeepSet : Option[Set[String]] = snpEffBiotypeKeepList.map{_.toSet}
    val snpEffEffectKeepSet : Option[Set[String]] = snpEffEffectKeepList.map{_.toSet}
    val snpEffWarningDropSet : Option[Set[String]] = snpEffWarningDropList.map{_.toSet}
    val geneSet : Option[Set[String]] = geneList.map{_.toSet}
    
    val keepIdx : Set[Int] = snpEffKeepIdx.map{ ki => {
      ki.map(string2int(_)).toSet
    }}.getOrElse( Range(0,16).toSet )
    
    val snpEffIdxDesc =  Seq("allele","effect","impact","geneName","geneID","txType","txID","txBiotype","rank","HGVS.c","HGVS.p","cDNAposition","cdsPosition","proteinPosition","distToFeature","warnings","errors");
    val snpEffFmtDescString = "A comma delimited list with bar-delimited entries in the format: "+keepIdx.map{i => snpEffIdxDesc(i)}.mkString("|") +"."
    val bioTypeDesc = snpEffBiotypeKeepList.map{ blist => {
      "Limited to the following biotypes: "+blist.mkString(",")+". "
    }}.getOrElse("all biotypes. ")
    val effectListDesc = snpEffEffectKeepList.map{ blist => {
      "Limited to the following effect types: "+blist.mkString(", ")+". "
    }}.getOrElse("all effect types. ")
    val warnListDesc = snpEffWarningDropList.map{ blist => {
      "Ignoring entries with any of the following warnings: "+blist.mkString(", ")+". "
    }}.getOrElse("Ignoring all warnings. ")
    val geneListDesc = geneList.map{ glist => {
      "Limited to the "+glist.length+" genes on the "+geneListName.getOrElse("user-specified")+" gene list. "
    }}.getOrElse("Ignoring all warnings. ")
    
    val annFmtDesc = snpEffFmtDescString + bioTypeDesc + effectListDesc + warnListDesc
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      
      outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = tagPrefix+"HIGH", ".", "String", "Shortened info extracted from ANN, high only. "+annFmtDesc)).addWalker(this))
      outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = tagPrefix+"MODERATE", ".", "String", "Extract from ANN, moderate only. "+annFmtDesc)).addWalker(this))
      outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = tagPrefix+"LOW", ".", "String", "Extract from ANN, LOW only. "+annFmtDesc)).addWalker(this))
      
      outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = tagPrefix+"GENES_HIGH", ".", "String", "Gene list with HIGH effect. "+bioTypeDesc+effectListDesc+warnListDesc)).addWalker(this))
      outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = tagPrefix+"GENES_MODERATE", ".", "String", "Gene list with MODERATE effect. "+bioTypeDesc+effectListDesc+warnListDesc)).addWalker(this))
      outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = tagPrefix+"GENES_LOW", ".", "String", "Gene list with LOW effect. "+bioTypeDesc+effectListDesc+warnListDesc)).addWalker(this))
      
      
      geneList.foreach{ gl => {
        outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = tagPrefix+"OnList_HIGH", ".", "String", "Extracted from ANN, high only. "+geneListDesc+annFmtDesc)).addWalker(this))
        outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = tagPrefix+"OnList_MODERATE", ".", "String", "Extracted from ANN, high only. "+geneListDesc+annFmtDesc)).addWalker(this))
        outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = tagPrefix+"OnList_LOW", ".", "String", "Extracted from ANN, high only. "+geneListDesc+annFmtDesc)).addWalker(this))

        outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = tagPrefix+"OnList_GENES_HIGH", ".", "String", "Gene list with HIGH effect. "+geneListDesc+bioTypeDesc+effectListDesc+warnListDesc)).addWalker(this))
        outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = tagPrefix+"OnList_GENES_MODERATE", ".", "String", "Gene list with MODERATE effect. "+geneListDesc+bioTypeDesc+effectListDesc+warnListDesc)).addWalker(this))
        outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = tagPrefix+"OnList_GENES_LOW", ".", "String", "Gene list with LOW effect. "+geneListDesc+bioTypeDesc+effectListDesc+warnListDesc)).addWalker(this))
      
      }}
      
      val overwriteInfos : Set[String] = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( outHeader.addedInfos );
      if( overwriteInfos.nonEmpty ){
        notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      }
      //vc.dropInfo(overwriteInfos);
 
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine()
        vc.dropInfo(overwriteInfos);
        var annHi = Vector[Array[String]]();
        var annMd = Vector[Array[String]]();
        var annLo = Vector[Array[String]]();
        var annHiG = Vector[Array[String]]();
        var annMdG = Vector[Array[String]]();
        var annLoG = Vector[Array[String]]();
        
        var gHi = Vector[String]();
        var gMd = Vector[String]();
        var gLo = Vector[String]();
        var gHiG = Vector[String]();
        var gMdG = Vector[String]();
        var gLoG = Vector[String]();
        
        v.info.get(tagID).getOrElse(None).foreach{ ann => {
          val annCells = ann.split(",").map{ aa => aa.split("[|]",-1) }
          annCells.foreach{ cells => {
            val impact = cells(annidxImpact);
            val effects = cells.lift(annidxEffect).map(_.split("&").toSet).getOrElse(Set());
            val warnings = cells.lift(annidxWarn).map(_.split("&").toSet).getOrElse(Set());
            val biotype = cells(annidxTxBiotype)
            val geneid = cells.lift(annidxGeneID).getOrElse("")
            
            val keepEffect = snpEffEffectKeepSet.map{ ks => ks.intersect(effects).nonEmpty }.getOrElse(true)
            val keepGene = geneSet.map{ gg =>  gg.contains(geneid) }.getOrElse(true)
            val dropWarn = snpEffWarningDropSet.map{ kk => kk.intersect(warnings).nonEmpty }.getOrElse(false);
            val keepbt = snpEffBiotypeKeepList.map{ kk => kk.contains(biotype) }.getOrElse(true);
            val geneName = cells.lift(annidxGeneName).getOrElse(".")
            if( keepEffect && ( ! dropWarn) && keepbt ){
              val outCells = cells.zipWithIndex.filter{ case (c,i) => keepIdx.contains(i) }.map{_._1}
              if(impact == "HIGH"){
                annHi = annHi :+ outCells
                gHi = gHi :+ geneName
              } else if(impact == "MODERATE"){
                annMd = annMd :+ outCells
                gMd = gMd :+ geneName
              } else if(impact == "LOW"){
                annLo = annLo :+ outCells
                gLo = gLo :+ geneName
              }
              if(keepGene){
                if(impact == "HIGH"){
                  annHiG = annHiG :+ outCells
                  gHiG = gHiG :+ geneName
                } else if(impact == "MODERATE"){
                  annMdG = annMdG :+ outCells
                  gMdG = gMdG :+ geneName
                } else if(impact == "LOW"){
                  annLoG = annLoG :+ outCells
                  gLoG = gLoG :+ geneName
                }
              }
            }
          }}
          
          vc.addInfo( tagPrefix+"HIGH",     annHi.map{ xx => xx.mkString("|") }.padTo(1,".").mkString(","));
          vc.addInfo( tagPrefix+"MODERATE", annMd.map{ xx => xx.mkString("|") }.padTo(1,".").mkString(","));
          vc.addInfo( tagPrefix+"LOW",      annLo.map{ xx => xx.mkString("|") }.padTo(1,".").mkString(","));
          
          
          vc.addInfo( tagPrefix+"GENES_HIGH",     gHi.padTo(1,".").distinct.sorted.mkString(","));
          vc.addInfo( tagPrefix+"GENES_MODERATE", gMd.padTo(1,".").distinct.sorted.mkString(","));
          vc.addInfo( tagPrefix+"GENES_LOW",      gLo.padTo(1,".").distinct.sorted.mkString(","));
          
          geneList.foreach{ gl => {
            vc.addInfo( tagPrefix+"OnList_HIGH",     annHiG.map{ xx => xx.mkString("|") }.padTo(1,".").mkString(","));
            vc.addInfo( tagPrefix+"OnList_MODERATE", annMdG.map{ xx => xx.mkString("|") }.padTo(1,".").mkString(","));
            vc.addInfo( tagPrefix+"OnList_LOW",      annLoG.map{ xx => xx.mkString("|") }.padTo(1,".").mkString(","));
            vc.addInfo( tagPrefix+"GENES_OnList_HIGH",     gHiG.padTo(1,".").distinct.sorted.mkString(","));
            vc.addInfo( tagPrefix+"GENES_OnList_MODERATE", gMdG.padTo(1,".").distinct.sorted.mkString(","));
            vc.addInfo( tagPrefix+"GENES_OnList_LOW",      gLoG.padTo(1,".").distinct.sorted.mkString(","));
          }}
          
        }}
        vc
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)

    }
    
  }
  
  class DropSpanIndels() extends internalUtils.VcfTool.SVcfWalker { 
    def walkerName : String = "DropSpanIndels"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)]();
    
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      var errCt = 0;
      
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      
      (addIteratorCloseAction( iter = vcFlatMap(vcIter){v => {
        if(v.alt.head == "*"){
          notice("dropping spanning indel (star allele).","DROP_STAR_ALLE",5);
          None
        } else {
          Some(v);
        }
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
    }
  }
  
  class MergeSpanIndels() extends internalUtils.VcfTool.SVcfWalker { 
    def walkerName : String = "MergeSpanIndels"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)]();
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      var errCt = 0;
      
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      
      val samps = outHeader.getSampleList
      
      val fixList = Seq("GL","PL","QA","AO","AD");
      
      val multAlleIdx : Int = Stream.from(1).find{ xx => {
        val xs = if(xx == 1) "" else xx.toString;
        (Seq("GT") ++ fixList).forall{ gg => {
          ! vcfHeader.formatLines.exists{ fl => fl.ID == gg + "_multAlle" + xs }
        }}
      }}.get
      val multAlleSuffix = if(multAlleIdx == 1) "" else multAlleIdx.toString()
      
      val oldFmtLines = outHeader.formatLines.filter{ fl => fixList.contains(fl.ID) && Set("R","A","G").contains(fl.Number)  };
      oldFmtLines.foreach{ fl => {
        outHeader.addFormatLine(
          new SVcfCompoundHeaderLine("FORMAT" ,ID = fl.ID + multAlleSuffix, ".", fl.Type, "(For multiallelic variants, an additional value is included in this version to indicate the value for the other alt alleles) "+fl.desc)
        )
      }}
      outHeader.addFormatLine(
          new SVcfCompoundHeaderLine("FORMAT" ,ID = "GT" + multAlleSuffix, "1", "String", "Raw GT tag, prior to conversion to universally readable VCF")
      )
      
      
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine();
        if(v.genotypes.genotypeValues.length > 0){
          if(v.alt.length == 2 && v.alt.last == (internalUtils.VcfTool.UNKNOWN_ALT_TAG_STRING)){
            oldFmtLines.foreach( fl => {
              val fmtIdx = v.format.indexOf(fl.ID);
              if(fmtIdx == -1){
                //do nothing
              } else {
                val expectedCt = if(fl.Number == "G"){ 3 } else if(fl.Number == "A"){ 1 } else { 2 }
                val gv = v.genotypes.genotypeValues(fmtIdx).map{g => { g.split(",") }}
                if(gv.exists{ g => {
                  g.length > expectedCt
                }}){
                  vc.genotypes.fmt = vc.genotypes.fmt.updated(fmtIdx,fl.ID + multAlleSuffix)
                  if(fl.Number == "A"){
                    vc.genotypes.addGenotypeArray(fl.ID, gv.map{ gg => {
                      gg.head
                    }})
                  } else if(fl.Number == "R"){
                    vc.genotypes.addGenotypeArray(fl.ID, gv.map{ gg => {
                      gg.take(2).padTo(1,".").mkString(",")
                    }})
                  }
                } else {
                  vc.genotypes.addGenotypeArray( fl.ID + multAlleSuffix, v.genotypes.genotypeValues(fmtIdx).clone() );
                }
                /*v.genotypes.genotypeValues(fmtIdx).zipWithIndex.foreach{ case (g,i) => {
                  val gc = g.split(",");
                  if(gc.length > expectedCt){
                    
                  }
                }}*/
              }
            })
            vc.genotypes.addGenotypeArray("GT"+multAlleSuffix,v.genotypes.genotypeValues(0).clone);
            v.genotypes.genotypeValues(0).map{g => (g.split("[/\\|]"),g.contains('|'))}.zipWithIndex.foreach{ case ((g,isPhased),i) => {
              vc.genotypes.genotypeValues(0)(i) = g.map{ a => if(a == ".") "." else {
                val aint = string2int(a);
                if(aint == 1) "1" else "0";
              }}.mkString((if(isPhased) "|" else "/"));
            }}
          }  else {
            vc.genotypes.addGenotypeArray("GT"+multAlleSuffix,v.genotypes.genotypeValues(0).clone);
            oldFmtLines.foreach{ fl => {
              val fmtIdx = v.format.indexOf(fl.ID);
              if(fmtIdx == -1){
                //do nothing
              } else {
                vc.genotypes.addGenotypeArray( fl.ID +multAlleSuffix, v.genotypes.genotypeValues(fmtIdx).clone() );
              }
            }}
          }
        }
        vc;
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
    }
  }

  
  object StdVcfConverter extends SVcfWalkerInfo {
    def walkerName : String = "StdVcfConverter";
    
    //def infoLines : Seq[SVcfCompoundHeaderLine] = Seq()
    //def formatLines : Seq[SVcfCompoundHeaderLine] = Seq()
    //def otherHeaderLines : Seq[SVcfHeaderLine] = Seq()
    //def walkLines : Seq[SVcfWalkHeaderLine] = Seq()
    //def walkerInfo : SVcfWalkerInfo = StdVcfConverter
  }
  
  class StdVcfConverter(cleanHeaderLines : Boolean = true, 
                        cleanInfoFields : Boolean = true, 
                        cleanMetaData : Boolean = true,
                        collapseStarAllele : Boolean = true,
                        deleteUnannotatedFields : Boolean = true,
                        thirdAlleleChar : Option[String] = None) extends internalUtils.VcfTool.SVcfWalker { 
    def walkerName : String = "StdVcfConverter"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
      ("cleanHeaderLines",cleanHeaderLines.toString()),
      ("cleanInfoFields",cleanInfoFields.toString()),
      ("cleanMetaData",cleanMetaData.toString()),
      ("collapseStarAllele",collapseStarAllele.toString()),
      ("deleteUnannotatedFields",deleteUnannotatedFields.toString()),
      ("thirdAlleleChar",thirdAlleleChar.getOrElse("None"))
    );
    val talle = thirdAlleleChar.getOrElse("0")
    def walkerInfo : SVcfWalkerInfo = StdVcfConverter;
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      var errCt = 0;
      
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      //outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO" ,infoTag, "1", "String", "If the variant is a singleton, then this will be the ID of the sample that has the variant."))
      val samps = outHeader.getSampleList
      
      val fixList = Seq("GT", "GL","PL","QA","AO","AD");
      
      val multAlleIdx : Int = Stream.from(1).find{ xx => {
        val xs = if(xx == 1) "" else xx.toString;
        (fixList).forall{ gg => {
          ! vcfHeader.formatLines.exists{ fl => fl.ID == gg + "_multAlle" + xs }
        }}
      }}.get
      val multAlleSuffix = "_multAlle" + (if(multAlleIdx == 1) "" else multAlleIdx.toString())
      val oldFmtLines = outHeader.formatLines.filter{ fl => fixList.contains(fl.ID) };
      
      if(collapseStarAllele || thirdAlleleChar.isDefined){
        oldFmtLines.foreach{ fl => {
          outHeader.addFormatLine(
            new SVcfCompoundHeaderLine("FORMAT" ,ID = fl.ID + multAlleSuffix, ".", fl.Type, "(For multiallelic variants, an additional value is included in this version to indicate the value for the other alt alleles) "+fl.desc)
          )
        }}
        outHeader.addStatBool("isSplitMultAlleStar",false)
      }
      if(collapseStarAllele || thirdAlleleChar.isDefined){
        outHeader.addFormatLine(
            new SVcfCompoundHeaderLine("FORMAT" ,ID = "GT" + multAlleSuffix, "1", "String", "Raw GT tag, prior to lossy conversion to universally readable VCF. Note that for split multiallelics there will be three possible alleles, 0, 1, and 2, where 2 represents any or all other alt alleles. FOR MOST PURPOSES, THIS IS THE GT TAG THAT SHOULD BE USED.")
        )
        
        vcfHeader.infoLines.find{ info => info.ID == "GT" }.foreach{ oldGt => {
          outHeader.addFormatLine(
            new SVcfCompoundHeaderLine("FORMAT" ,ID = "GT", "1", "String", "Collapsed GT tag, for back-compatibility with software tools that cannot parse VCF v4.2. For split multiallelic variants the third allele will be collapsed with the ref allele. WARNING: It is NOT recommended that this GT tag be used for most purposes, as multiallelic alt alleles have been simplified. It is preferable to use the "+"GT" + multAlleSuffix+" tag, which may contain 3 possible alleles.")
            )
        }}
      }
      
      if(cleanHeaderLines){
        outHeader.formatLines.foreach{ fl => {
          outHeader.addFormatLine(({
            val n = if(fl.Number.head == '-') "." else fl.Number;
            val d = if(fl.desc == "") "." else fl.desc;
            var t = fl.Type;
            t = if(t == "INTEGER"){ "Integer"
            } else if(t == "STRING"){ "String"
            } else if(t == "FLOAT"){ "Float"
            } else { t }
            new SVcfCompoundHeaderLine("FORMAT" ,ID = fl.ID , n, t, d)
          }))
        }}
        outHeader.infoLines.foreach{ fl => {
          outHeader.addInfoLine(({
            val n = if(fl.Number.head == '-') "." else fl.Number;
            val d = if(fl.desc == "") "." else fl.desc;
            var t = fl.Type;
            t = if(t == "INTEGER"){ "Integer"
            } else if(t == "STRING"){ "String"
            } else if(t == "FLOAT"){ "Float"
            } else { t }
            new SVcfCompoundHeaderLine("INFO" ,ID = fl.ID , n, t, d)
          }))
        }}
      }
      

      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        //val vc = v.getOutputLine()
        
        val vc = if(cleanMetaData) { 
            SVcfOutputVariantLine(
             in_chrom = v.chrom,
             in_pos = v.pos,
             in_id = v.id,
             in_ref = v.ref,
             in_alt = v.alt.take(1),
             in_qual = v.qual,
             in_filter = v.filter, 
             in_info = v.info,
             in_format = v.format,
             in_genotypes = v.genotypes//,
             //in_header = header
          )
        } else {
          v.getOutputLine();
        }
        //vc.info.withFilter{ case (key,info) => info.getOrElse("miss") == "" }.foreach{ case (tag,info) => {
        //  vc.addInfo(tag,".");
        //}}
        if(cleanInfoFields){
          vc.info.foreach{ case (tag,info) => {
            info.foreach{ infoString => {
              if((! deleteUnannotatedFields) || vcfHeader.infoLines.exists{ infoLine => infoLine.ID == tag }){
                if(infoString == ""){
                  vc.addInfo(tag,".");
                } else if(infoString.contains('=') || infoString.contains(';') || infoString.contains(' ')){
                  vc.addInfo(tag,infoString.replaceAll("[=]","%3D").replaceAll("[ ]","_").replaceAll("[;]",","));
                } 
              }
            }}
          }}
        }
        if(v.genotypes.genotypeValues.length > 0 && collapseStarAllele){
          if(v.alt.length == 2 && v.alt.last == (internalUtils.VcfTool.UNKNOWN_ALT_TAG_STRING)){
            oldFmtLines.foreach( fl => {
              val fmtIdx = v.format.indexOf(fl.ID);
              if(fmtIdx == -1){
                //do nothing
              } else {
                val expectedCt = if(fl.Number == "G"){ 3 } else if(fl.Number == "A"){ 1 } else { 2 }
                val gv = v.genotypes.genotypeValues(fmtIdx).map{g => { g.split(",") }}
                if(gv.exists{ g => {
                  g.length > expectedCt
                }}){
                  vc.genotypes.fmt = vc.genotypes.fmt.updated(fmtIdx,fl.ID + multAlleSuffix)
                  if(fl.Number == "A"){
                    vc.genotypes.addGenotypeArray(fl.ID, gv.map{ gg => {
                      gg.head
                    }})
                  } else if(fl.Number == "R"){
                    vc.genotypes.addGenotypeArray(fl.ID, gv.map{ gg => {
                      gg.take(2).padTo(1,".").mkString(",")
                    }})
                  }
                } else {
                  vc.genotypes.addGenotypeArray( fl.ID + multAlleSuffix, v.genotypes.genotypeValues(fmtIdx).clone() );
                }
                /*v.genotypes.genotypeValues(fmtIdx).zipWithIndex.foreach{ case (g,i) => {
                  val gc = g.split(",");
                  if(gc.length > expectedCt){
                    
                  }
                }}*/
              }
            })
          } else {
            oldFmtLines.foreach{ fl => {
              val fmtIdx = v.format.indexOf(fl.ID);
              if(fmtIdx == -1){
                //do nothing
              } else {
                vc.genotypes.addGenotypeArray( fl.ID +multAlleSuffix, v.genotypes.genotypeValues(fmtIdx).clone() );
              }
            }}
          }
        }
        if(v.genotypes.genotypeValues.length > 0 && (collapseStarAllele || thirdAlleleChar.isDefined)){
          if(v.alt.length == 2 && v.alt.last == (internalUtils.VcfTool.UNKNOWN_ALT_TAG_STRING)){
            vc.genotypes.addGenotypeArray("GT"+multAlleSuffix,v.genotypes.genotypeValues(0).clone);
            v.genotypes.genotypeValues(0).map{g => (g.split("[/\\|]"),g.contains('|'))}.zipWithIndex.foreach{ case ((g,isPhased),i) => {
              vc.genotypes.genotypeValues(0)(i) = g.map{ a => if(a == ".") "." else {
                if(a == "2") talle else a;
              }}.mkString((if(isPhased) "|" else "/"));
            }}
          } else {
            vc.genotypes.addGenotypeArray("GT"+multAlleSuffix,v.genotypes.genotypeValues(0).clone);
          }
        }
        vc
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
      
    }
  }

  case class AddStatDistributionTags(tagAD : Option[String], tagDP : Option[String] = None,
                                     tagGT : String = "GT",
                                     tagSingleCallerAlles : Option[String] = None,
                                     outputTagPrefix : String = "SWH_STAT_",
                                     variantStatExpression : Option[List[String]] = None,
                                     depthCutoffs : Option[List[Int]] = Some(List(10,20,40)),
                                     groupFile : Option[String] = None, groupList : Option[String] = None, superGroupList : Option[String] = None
                                     ) extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "AddStatDistributionTags" 
      val MISS : Int = 0;
      val HOMREF : Int = 1
      val HET : Int = 2
      val HOMALT : Int = 3;
      val OTHER : Int = 4;
      val genoClasses = Seq("Miss","HomRef","Het","HomAlt","Other");
      val genoClassDesc = genoClasses.map{ gc => "Number of samples with the "+gc+" genotype for this variant (based on tag "+tagGT+")" }
     
      
    def walkerParams : Seq[(String,String)] = Seq[(String,String)](("tagAD",tagAD.toString),("tagGT",tagGT),("tagSingleCallerAlles",tagSingleCallerAlles.toString),("outputTagPrefix",outputTagPrefix))

    var samps : Seq[String] = null;
    val (sampleToGroupMap,groupToSampleMap,groups) = getGroups(groupFile,groupList,superGroupList);
    val logicParser : internalUtils.VcfTool.SFilterLogicParser[(SVcfVariantLine,Int)] = internalUtils.VcfTool.sGenotypeFilterLogicParser;
    
    val genoStatExpressionInfo : List[(String,String,SFilterLogic[(SVcfVariantLine,Int)])] = variantStatExpression match {
      case Some(gseList) => {
        gseList.map{ gseString => {
          val cells = gseString.split("\\|");
          if(cells.length != 3){
            error("Error: genotypeStatExpressionList must be a list of elements where each element is a bar-delimited list of length 3");
          }
          (cells(0),cells(1),logicParser.parseString(cells(2)))
        }}
      }
      case None => {
        List();
      }
    }
    val fullGroupList = (groups.map{g => Some(g)} :+ None )

    var medianIdx = -1
    var lqIdx = -1
    var uqIdx = -1
    var depthCutoffSeq : Seq[Int] = Seq[Int]();
    
    def initVCF(vcfHeader : SVcfHeader, verbose : Boolean = true) : SVcfHeader = {
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      //outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO" ,infoTag, "1", "String", "If the variant is a singleton, then this will be the ID of the sample that has the variant."))
      samps = outHeader.getSampleList
      
      medianIdx = samps.length / 2;
      lqIdx = samps.length / 4;
      uqIdx = math.min(samps.length-1,math.ceil( samps.length.toDouble * 3.toDouble / 4.toDouble ).toInt)
      
      //val fixList = Seq("GL","PL","QA","AO","AD");
      
      val hetFracTagDesc = "(Based on tags: "+tagAD.getOrElse(".")+"/"+tagSingleCallerAlles.getOrElse(".")+")"
      
      genoClasses.zip(genoClassDesc).foreach{ case (gClass,desc) => {
        outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,//NEWTAGS
                                                         outputTagPrefix+"AD_SAMPCT_"+gClass, 
                                                         "1", 
                                                         "Integer", 
                                                         desc)).addWalker(this)
        )
      }}
      
        outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,
                                                         outputTagPrefix+"AD_HET_FRAC_MIN", 
                                                         "1", 
                                                         "Float", 
                                                         "The lowest ratio of REF allele depth to ALT allele depth across all HETEROZYGOUS samples. ("+hetFracTagDesc+", See also tag "+outputTagPrefix+"SAMPCT_"+"HET"+")"
                                                         )).addWalker(this))
        outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,
                                                         outputTagPrefix+"AD_HET_REF", 
                                                         "1", 
                                                         "Integer", 
                                                         "Sum total of the REF allele depth across all HETEROZYGOUS samples. ("+hetFracTagDesc+", See also tag "+outputTagPrefix+"SAMPCT_"+"HET"+")"
                                                         )).addWalker(this))
        outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,
                                                         outputTagPrefix+"AD_HET_ALT", 
                                                         "1", 
                                                         "Integer", 
                                                         "Sum total of the REF allele depth across all HETEROZYGOUS samples. ("+hetFracTagDesc+", See also tag "+outputTagPrefix+"SAMPCT_"+"HET"+")"
                                                         )).addWalker(this))
        outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,
                                                         outputTagPrefix+"AD_HET_FRAC", 
                                                         "1", 
                                                         "Float", 
                                                         "Fraction of the total depth that are ALT, across all HETEROZYGOUS samples. ("+hetFracTagDesc+", See also tag "+outputTagPrefix+"SAMPCT_"+"HET"+")"
                                                         )).addWalker(this))
        
        
        outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,
                                                         outputTagPrefix+"DEPTH_MEDIAN", 
                                                         "1", 
                                                         "Integer", 
                                                         "Median depth across all samples (based on tag: "+tagAD.getOrElse(".")+"/"+tagDP.getOrElse(".")+")"
                                                         )).addWalker(this))
        outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,
                                                         outputTagPrefix+"DEPTH_LQ", 
                                                         "1", 
                                                         "Integer", 
                                                         "Lower Quartile depth across all samples (based on tag: "+tagAD.getOrElse(".")+"/"+tagDP.getOrElse(".")+")"
                                                         )).addWalker(this))
        outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,
                                                         outputTagPrefix+"DEPTH_UQ", 
                                                         "1", 
                                                         "Integer", 
                                                         "Upper Quartile depth across all samples (based on tag: "+tagAD.getOrElse(".")+"/"+tagDP.getOrElse(".")+")"
                                                         )).addWalker(this))
        depthCutoffSeq = depthCutoffs.toSeq.flatten.sorted;
        depthCutoffSeq.map{ c => {
           outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,
                                                         outputTagPrefix+"DepthSampCt_"+c,
                                                         "1",
                                                         "Integer",
                                                         "The number of samples at total allele depth "+c + " (based on tag: "+tagAD.getOrElse(".")+"/"+tagDP.getOrElse(".")+")"
                                                         )).addWalker(this))
        }}
        
        outHeader;
    }
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      //var errCt = 0;
      val outHeader = initVCF(vcfHeader,verbose);
      
      val overwriteInfos = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( outHeader.addedInfos );
      if( overwriteInfos.nonEmpty ){
        notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      }
      
      val (splitGtTag,stdGtTag) = if(vcfHeader.isSplitMultiallelic() && vcfHeader.formatLines.exists( f => f.ID == tagGT + "_split" )){
        (tagGT+"_split",tagGT)
      } else {
        (tagGT,tagGT)
      }
      
      val groupAnno = fullGroupList.zipWithIndex.map{ case (g,i) => g match {
          case Some(gg) => {
            (g,i,"_GRP_"+gg," for samples in group "+gg,groupToSampleMap(gg).size)
          }
          case None => {
            (g,i,""," across all samples",samps.length)
          }
      }}

      groupAnno.foreach{ case (grp,i,gtag,groupDesc,groupSize) => {
        genoStatExpressionInfo.foreach{ case (tagCore,descCore,expr) => {
          outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO" ,
                                                         tagCore+gtag, 
                                                         "1", 
                                                         "Integer", 
                                                         descCore+groupDesc
                                                         ))
        }}
      }}
      val nsamp = samps.length;
      val sampGroupIdx : Vector[Vector[Int]] = samps.toVector.map{ sampid => {
          val groupSet : Set[String] = sampleToGroupMap(sampid);
          val groupIdx : Vector[Int] = (groups.zipWithIndex.flatMap{ case (g,i) => if(groupSet.contains(g)){ Some(i) } else None } :+ groups.length).toVector;
          groupIdx
      }}
      
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine()
        vc.dropInfo( overwriteInfos );
        val gtidxRaw = vc.genotypes.fmt.indexOf(splitGtTag);
        val gtIdx = if(gtidxRaw == -1) vc.genotypes.fmt.indexOf(stdGtTag) else gtidxRaw;
        if(gtIdx == -1) error("Cannot find \""+splitGtTag+"\" or \""+stdGtTag+"\" in fmt line: \""+vc.genotypes.fmt.mkString(",")+"\"");
        val gt : Vector[String] = v.genotypes.genotypeValues(gtIdx).toVector;
        val simpleClass : Vector[Int] = gt.map{ genoString => {
          val gc = genoString.split("[/\\|]")
          if( gc.exists( g => g == ".")){
            MISS
          } else if(gc.forall(g => g == "0")){
            HOMREF
          } else if(gc.exists(g => g == "0") && gc.exists(g => g == "1")){
            HET
          } else if(gc.forall(g => g == "1")){
            HOMALT
          } else {
            OTHER
          }
        }}
        val altAlle = v.alt.head;
        
        genoStatExpressionInfo.foreach{ case (tagCore,descCore,expr) => {
          val cts = Array.fill[Int](fullGroupList.length)(0);
          var i = 0
          while(i < nsamp){
            if( expr.keep(v,i) ){
              sampGroupIdx(i).map{ grpIdx => {
                cts(grpIdx) += 1;
              }}
            }
            i += 1;
          }
          groupAnno.foreach{ case (grp,i,gtag,groupDesc,groupSize) => {
            val tag = tagCore+gtag;
            vc.addInfo(tag,cts(i).toString);
          }}
        }}
        

        if(tagAD.isDefined){
          /*val alleList : Seq[String] = tagSingleCallerAlles match {
            case Some(t) => {
              v.info.getOrElse(t,None) match {
                case Some(scAlles) => {
                  if(scAlles == "."){
                    Seq[String]();
                  } else {
                    scAlles.split(",").toSeq;
                  }
                }
                case None => Seq[String]();
              }
            }
            case None => v.alt;
          }*/
          //val alleIdx = alleList.indexOf(altAlle);
          
          val alleIdx : Int = tagSingleCallerAlles.map{ scaTag => v.info.getOrElse(scaTag,None) }.map{ tval => {
            if(tval.isEmpty || tval.get == ".") {
              -1
            } else {
              string2int(tval.get)
            }
          }}.getOrElse(0);
            
            /*v.info.getOrElse(tagSingleCallerAlles.get,None) match {
            case Some(tval) => {
              if(tval == "."){
                -1;
              } else {
                string2int(tval);
              }
            }
            case None => -1;
          } */
          val adIdx = v.genotypes.fmt.indexOf(tagAD.get);
          if(alleIdx != -1 && adIdx != -1){
            val ads = v.genotypes.genotypeValues(adIdx).map{ a => {
              if(a == "."){
                (0,0)
              } else {
                val cells = a.split(",").map{aa => if(aa == ".") 0 else string2int(aa)};
                if(cells.length == 2 && alleIdx == 0){
                  (cells(0), cells(1))
                } else {
                  if(! cells.isDefinedAt(alleIdx + 1)){
                    warning("Malformed "+tagAD.get+" tag: altAlle="+altAlle+", alleIdx="+alleIdx+", a="+a+", cells=["+cells.mkString(",")+"]"+"\n   on variant: "+v.getSimpleVcfString(),"MALFORMED_ADTAG_"+tagAD.get,10);
                  }
                  (cells.sum - cells(alleIdx + 1),cells(alleIdx + 1))
                }
              }
            }}
            val dpIdx = tagDP match {
              case Some(tad) => v.genotypes.fmt.indexOf(tad);
              case None => -2;
            }
            
            //val sumDepthSorted = ads.map{case (r,a) => {r + a}}.sorted;
            
            val sumDepthSorted = if(dpIdx == -2){
              ads.map{case (r,a) => {r + a}}.sorted;
            } else if(dpIdx == -1) {
              ads.map{case (r,a) => 0}
            } else {
              v.genotypes.genotypeValues(dpIdx).map{ a => {
                if(a == ".") 0 else string2int(a);
              }}.sorted
            }
            val depthMedian = sumDepthSorted(medianIdx);
            val depthLQ = sumDepthSorted(lqIdx);
            val depthUQ = sumDepthSorted(uqIdx);
            
            vc.addInfo(outputTagPrefix+"DEPTH_MEDIAN",depthMedian.toString);
            vc.addInfo(outputTagPrefix+"DEPTH_LQ",depthLQ.toString);
            vc.addInfo(outputTagPrefix+"DEPTH_UQ",depthUQ.toString);
            
            depthCutoffSeq.foreach{ cutoff => {
              val nonDepthCt = sumDepthSorted.indexWhere( dp => { dp > cutoff})
              val depthCt = if(nonDepthCt == -1){
                -1
              } else {
                samps.length - nonDepthCt
              }
              vc.addInfo(outputTagPrefix+"DepthSampCt_"+cutoff,depthCt.toString);
            }}
            
            val sampCts = Array.fill[Int](genoClasses.length + 1)(0);
            val refCts = Array.fill[Int](genoClasses.length + 1)(0);
            val altCts = Array.fill[Int](genoClasses.length + 1)(0);
            var hetMinRatio : Double = 2.0;
            
            simpleClass.zip(ads).foreach{ case (c,(refCt,altCt)) => {
                if(refCt != -1 && altCt != -1){
                  sampCts(c) += 1;
                  refCts(c) += refCt;
                  altCts(c) += altCt;
                  if(c == HET) hetMinRatio = math.min(hetMinRatio,altCt.toDouble / (altCt+refCt).toDouble);
                }
                //
                //if(refCt != -1) refCts(c) += refCt;
                //if(altCt != -1) altCts(c) += altCt;
              }}
            //val hetMinRatio = simpleClass.zip(ads).foreach{ case (c,(refCt,altCt)) => {
            if(hetMinRatio == 2.0) hetMinRatio = -1.0;
            
            genoClasses.zipWithIndex.foreach{ case (c,i) => {
              vc.addInfo(outputTagPrefix+"AD_SAMPCT_"+c,sampCts(i).toString);
              //vc.addInfo(outputTagPrefix+"AD_"+c+"_REF",refCts(i));
              //vc.addInfo(outputTagPrefix+"AD_"+c+"_ALT",altCts(i));
              //vc.addInfo(outputTagPrefix+"AD_"+c+"_ALT",altCts(i));
            }}
            if(sampCts(HET) > 0){
              vc.addInfo(outputTagPrefix+"AD_HET_REF",refCts(HET).toString);
              vc.addInfo(outputTagPrefix+"AD_HET_ALT",altCts(HET).toString);
              vc.addInfo(outputTagPrefix+"AD_HET_FRAC",(math.floor(10000 * altCts(HET).toDouble / (refCts(HET).toDouble+altCts(HET).toDouble)) / 10000.toDouble).toString);
              vc.addInfo(outputTagPrefix+"AD_HET_FRAC_MIN",(math.floor(10000 * hetMinRatio) / 10000.toDouble).toString);

            }
            
          }
        }

        vc
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
      
    }
  }
  
  
  //vcfCodes : VCFAnnoCodes = VCFAnnoCodes(CT_INFIX = tagPrefix.getOrElse(""))
  case class SAddGroupInfoAnno(groupFile : Option[String], groupList : Option[String], superGroupList  : Option[String], chromList : Option[List[String]], 
             addCounts : Boolean = true, addFreq : Boolean = true, addMiss : Boolean = true, 
             addAlle : Boolean= true, addHetHom : Boolean = true, 
             sepRef : Boolean = true, countMissing : Boolean = true,
             noMultiAllelics : Boolean = false,
             GTTag : String = "GT",
             tagPrefix : String = "",
             tagFilter : Option[String] = None,
             tagPreFiltGt : Option[String] = None,
             vcfCodes : VCFAnnoCodes = VCFAnnoCodes()) extends internalUtils.VcfTool.SVcfWalker {
    
    def walkerName : String = "SAddGroupInfoAnno"
    def walkerParams : Seq[(String,String)] = Seq[(String,String)](
          ("groupFile",groupFile.getOrElse("None")),
          ("groupList",groupList.getOrElse("None")),
          ("superGroupList",superGroupList.getOrElse("None")),
          ("chromList",fmtOptionList(chromList)),
          ("addCounts",addCounts.toString),
          ("addFreq",addFreq.toString),
          ("addMiss",addMiss.toString),
          ("addAlle",addAlle.toString),
          ("addHetHom",addHetHom.toString),
          ("sepRef",sepRef.toString),
          ("countMissing",countMissing.toString),
          ("noMultiAllelics",noMultiAllelics.toString),
          ("GTTag",GTTag),
          ("tagPrefix",if(tagPrefix == "") "None" else tagPrefix)
        );
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
  
      val (sampleToGroupMap,groupToSampleMap,groups) = getGroups(groupFile,groupList,superGroupList);

  
      val sampNames = vcfHeader.getSampleList;
      val nsamp = sampNames.length
  
      reportln("Final Groups:","debug");
      for((g,i) <- groups.zipWithIndex){
        reportln("Group "+g+" ("+groupToSampleMap(g).size+")","debug");
      }
      
      //addCounts : Boolean = true, addFreq : Boolean = true, addAlle, addHetHom : Boolean = true, sepRef : Boolean = true,
      val forEachString = "for each possible allele (including the ref)"; //if(sepRef) "for each possible ALT allele (NOT including the ref)" else "for each possible allele (including the ref)";
      val countingString = if(countMissing) " counting uncalled alleles." else " not counting uncalled alleles."
      
      val anum = if(noMultiAllelics) "1" else "A";
      val adesc = if(noMultiAllelics) "(Based on tag "+GTTag+")" else " (for each alt allele. Based on tag "+GTTag+"))"
      val fullGroupList = (groups.map{g => Some(g)} :+ None )
      val groupAnno = fullGroupList.zipWithIndex.map{ case (g,i) => g match {
        case Some(gg) => {
          (g,i,"_GRP_"+gg," for samples in group "+gg,groupToSampleMap(gg).size)
        }
        case None => {
          (g,i,""," across all samples",sampNames.length)
        }
      }}
      
      val (splitGtTag,stdGtTag) = if(vcfHeader.isSplitMultiallelic() && vcfHeader.formatLines.exists( f => f.ID == GTTag + "_split" )){
        if( vcfHeader.formatLines.find(f => f.ID == GTTag + "_split").get.subType == internalUtils.VcfTool.subtype_GtStyle ){
          (GTTag+"_split",GTTag)
        } else {
          (GTTag,GTTag)
        }
      } else {
        (GTTag,GTTag)
      }
      
      /*
       *         nFiltCt_TAG : String = TOP_LEVEL_VCF_TAG+"CT_FiltCt",
        nFiltFrq_TAG : String = TOP_LEVEL_VCF_TAG+"CT_FiltFreq",
        nPremisCt_TAG : String = TOP_LEVEL_VCF_TAG+"CT_PrefiltMisCt",
        nPremisFrq_TAG : String = TOP_LEVEL_VCF_TAG+"CT_PrefiltMisFreq",
       */
      
      
      val newHeaderLines = groupAnno.flatMap{case (grp,i,gtag,groupDesc,groupSize) => {
        
        (if(addFreq){
          List(
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nAF_TAG + gtag, anum, "Float", "The alt allele frequency"+groupDesc+adesc+"."),
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nHomFrq_TAG + gtag, anum, "Float", "The frequency of homalt calls"+groupDesc  +adesc+ "."),
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nHetFrq_TAG + gtag, anum, "Float", "The frequency of het calls"+groupDesc  +adesc+ "."),
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nAltFrq_TAG + gtag, anum, "Float", "The frequency of calls that are het or homalt"+groupDesc  +adesc+ "."),
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nMisFrq_TAG + gtag, "1", "Integer", "The frequency of calls that are missing"+groupDesc  +adesc+ "."),
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nOthFrq_TAG + gtag, "1", "Integer", "The frequency of calls include a different allele"+groupDesc  +adesc+ "."),
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nNonrefFrq_TAG + gtag, "1", "Integer", "The frequency of calls include any nonref allele "+groupDesc  +adesc+ ".")
          )
        } else { List() }) ++ 
        (if(addCounts){
          List(
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nHomCt_TAG + gtag, anum, "Integer", "The number of homalt calls"+groupDesc +adesc+ "."),
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nHetCt_TAG + gtag, anum, "Integer", "The number of het calls"+groupDesc +adesc+ "."),
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nAltCt_TAG + gtag,   anum, "Integer", "The number of calls that are het or homalt"+groupDesc  +adesc+ "."),
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nMisCt_TAG + gtag, "1", "Integer", "The number of calls that are missing"+groupDesc  +adesc+ "."),
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nOthCt_TAG + gtag, "1",  "Integer", "The number of calls include a different allele"+groupDesc  +adesc+ "."),
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nNonrefCt_TAG + gtag, "1",  "Integer", "The number of calls include any nonref allele "+groupDesc  +adesc+ ".")
          )  
        } else { List() }) ++ (
          tagFilter match {
            case Some(t) => {
              (if(addFreq){
                List( new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nFiltFrq_TAG + gtag, anum, "Float", "The genotypefilter frequency "+groupDesc+adesc+".")                )
              } else { List() }) ++ (if(addCounts){
                List( new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nFiltCt_TAG + gtag, anum, "Float", "The number of genotype calls that were filtered "+groupDesc+adesc+"."))
              } else { List() })
            }
            case None => {
              List()
            }
          }
        ) ++ (
          tagPreFiltGt match {
            case Some(t) => {
              (if(addFreq){
                List( new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nPremisFrq_TAG + gtag, anum, "Float", "The prefilter missing rate before genotype filtering "+groupDesc+adesc+"."),
                      new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nPreNonrefFrq_TAG + gtag, "1",  "Integer", "The fraction of calls include any nonref allele "+groupDesc  +adesc+ ".")
/*
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nAF_TAG +"_prefilt"++ gtag, anum, "Float", "The alt allele frequency before genotype filtering "+groupDesc+adesc+"."),
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nHomFrq_TAG +"_prefilt"++ gtag, anum, "Float", "The frequency of homalt calls before genotype filtering "+groupDesc  +adesc+ "."),
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nHetFrq_TAG +"_prefilt"++ gtag, anum, "Float", "The frequency of het calls before genotype filtering "+groupDesc  +adesc+ "."),
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nAltFrq_TAG +"_prefilt"++ gtag, anum, "Float", "The frequency of calls that are het or homalt before genotype filtering "+groupDesc  +adesc+ "."),
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nMisFrq_TAG +"_prefilt"++ gtag, "1", "Integer", "The frequency of calls that are missing before genotype filtering "+groupDesc  +adesc+ "."),
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nOthFrq_TAG +"_prefilt"++ gtag, "1", "Integer", "The frequency of calls include a different allele before genotype filtering "+groupDesc  +adesc+ ".")*/

                )
              } else { List() }) ++ (if(addCounts){
                List( new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nPremisCt_TAG + gtag, anum, "Float", "The number of genotype calls that were marked missing before genotype filtering "+groupDesc+adesc+"."),
                      new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nPreNonrefCt_TAG + gtag, "1",  "Integer", "The number of calls include any nonref allele "+groupDesc  +adesc+ ".")

                    /*
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nHomCt_TAG +"_prefilt"+ gtag, anum, "Integer", "The number of homalt calls before genotype filtering "+groupDesc +adesc+ "."),
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nHetCt_TAG +"_prefilt"+ gtag, anum, "Integer", "The number of het calls before genotype filtering "+groupDesc +adesc+ "."),
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nAltCt_TAG +"_prefilt"+ gtag,   anum, "Integer", "The number of calls that are het or homalt before genotype filtering "+groupDesc  +adesc+ "."),
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nMisCt_TAG +"_prefilt"+ gtag, "1", "Integer", "The number of calls that are missing before genotype filtering "+groupDesc  +adesc+ "."),
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nOthCt_TAG +"_prefilt"+ gtag, "1",  "Integer", "The number of calls include a different allele before genotype filtering "+groupDesc  +adesc+ ".")*/
                )
              } else { List() })
            }
            case None => {
              List()
            }
          }
        )
      }}

      val outHeader = vcfHeader.copyHeader;
      
      newHeaderLines.foreach{hl => {
        outHeader.addInfoLine(hl);
      }}
      outHeader.addWalk(this);
      
      val overwriteInfos = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( outHeader.addedInfos );
      if( overwriteInfos.nonEmpty ){
        notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      }
      
      return (vcMap(vcIter)(vc => {
        var vb = vc.getOutputLine()
        vb.dropInfo(overwriteInfos)
        val gtidxRaw = vc.genotypes.fmt.indexOf(splitGtTag);
        val gtidx = if(gtidxRaw == -1) vc.genotypes.fmt.indexOf(stdGtTag) else gtidxRaw;
        if(gtidx == -1){
          warning("SAddGroupsInfo: Cannot find \""+splitGtTag+"\" or \""+stdGtTag+"\" in fmt line: \""+vc.genotypes.fmt.mkString(",")+"\"","WARNING_GT_TAG_NOT_FOUND",10);
        } else {
  
          val genoData : Array[(Array[String],String,Set[String],Array[Int])] = vc.genotypes.genotypeValues(gtidx).map{_.split("/")}.zip(sampNames).map{ case (geno,sampid) => { 
            val groupSet = sampleToGroupMap(sampid);
            val groupIdx = (groups.zipWithIndex.flatMap{ case (g,i) => if(groupSet.contains(g)){ Some(i) } else None } :+ groups.length).toArray;
            (geno,sampid,groupSet,groupIdx)
          }};
          //val alleles = Range(0,vc.alt.length + 1).map(_.toString());
          //val alleles = Vector("0") ++ vc.alt.toVector.zipWithIndex.filter{case (a,i) => a != internalUtils.VcfTool.UNKNOWN_ALT_TAG_STRING }.map{ case (a,i) => (i + 1).toString }
          val isSplit = vc.alt.contains(internalUtils.VcfTool.UNKNOWN_ALT_TAG_STRING) && vc.alt.length == 2;
          val numAlt = vc.alt.filter(_ != internalUtils.VcfTool.UNKNOWN_ALT_TAG_STRING).length;
          
          if(numAlt == 1){
            val homCts = Array.fill[Int](fullGroupList.length)(0);
            val hetCts = Array.fill[Int](fullGroupList.length)(0);
            val misCts = Array.fill[Int](fullGroupList.length)(0);
            val othAltCts = Array.fill[Int](fullGroupList.length)(0);
            val othCts = Array.fill[Int](fullGroupList.length)(0);
            val unkCts = Array.fill[Int](fullGroupList.length)(0);
            
            val filtCts = Array.fill[Int](fullGroupList.length)(0);
            val rawGtMissing = Array.fill[Int](fullGroupList.length)(0);
            
            val rawGtNonRef = Array.fill[Int](fullGroupList.length)(0);
            
            val lastIdx = fullGroupList.length - 1;
            
            tagFilter match {
              case Some(t) => {
                val tagIdx = vc.genotypes.fmt.indexOf(t);
                if(tagIdx != -1){
                  val filt = vc.genotypes.genotypeValues(tagIdx)
                  var i = 0;
                  while(i < nsamp){
                    if( filt(i) == "1" ){
                      val groupIdx : Array[Int] = genoData(i)._4;
                      var ii = 0;
                      while(ii < groupIdx.length){
                        filtCts(groupIdx(ii)) += 1;
                        ii += 1;
                      }
                    }
                    i += 1;
                  }
                }
              }
              case None => {
                //do nothing
              }
            }
            
            tagPreFiltGt match {
              case Some(t) => {
                val tagIdx = vc.genotypes.fmt.indexOf(t);
                if(tagIdx != -1){
                  val x = vc.genotypes.genotypeValues(tagIdx)
                  var i = 0;
                  while(i < nsamp){
                    if( x(i).charAt(0) == '.' ){
                      val groupIdx : Array[Int] = genoData(i)._4;
                      var ii = 0;
                      while(ii < groupIdx.length){
                        rawGtMissing(groupIdx(ii)) += 1;
                        ii += 1;
                      }
                    } else if (x(i).split("[\\|/]").exists(xx => xx != "0")){
                      val groupIdx : Array[Int] = genoData(i)._4;
                      var ii = 0;
                      while(ii < groupIdx.length){
                        rawGtNonRef(groupIdx(ii)) += 1;
                        ii += 1;
                      }
                    }
                    i += 1;
                  }
                }
              }
              case None => {
                //do nothing
              }
            }
            
            var i = 0;
            while(i < nsamp){
              val (gt,sampid,grps,groupIdx) : (Array[String],String,Set[String],Array[Int]) = genoData(i);
              val arr = if(gt.contains(".")){
                misCts
              } else if(gt.contains("0") && gt.contains("1")){
                hetCts
              } else if(gt.forall(_ == "1")){
                homCts
              } else if(gt.contains("2")){
                if(gt.contains("1")){
                  othAltCts
                } else {
                  othCts
                }
              } else {
                unkCts
              }
              var ii = 0;
              while(ii < groupIdx.length){
                arr(groupIdx(ii)) += 1;
                ii += 1;
              }
              i += 1;
            }
            groupAnno.foreach{case (grp,i,gtag,groupDesc,groupSize) => {
              if(addCounts){
                 vb.addInfo(vcfCodes.nHomCt_TAG + gtag, homCts(i).toString);
                 vb.addInfo(vcfCodes.nHetCt_TAG + gtag, hetCts(i).toString);
                 vb.addInfo(vcfCodes.nMisCt_TAG + gtag, misCts(i).toString);
                 vb.addInfo(vcfCodes.nOthCt_TAG + gtag, (othCts(i) + othAltCts(i)).toString);
                 vb.addInfo(vcfCodes.nAltCt_TAG + gtag, (homCts(i) + hetCts(i) + othAltCts(i)).toString);
                 vb.addInfo(vcfCodes.nNonrefCt_TAG + gtag, (homCts(i) + hetCts(i) + othAltCts(i) + othCts(i)).toString);
                 if(tagFilter.isDefined){
                   vb.addInfo(vcfCodes.nFiltCt_TAG + gtag, (filtCts(i)).toString);
                 }
                 if(tagPreFiltGt.isDefined){
                   vb.addInfo(vcfCodes.nPremisCt_TAG + gtag, (rawGtMissing(i)).toString);
                   vb.addInfo(vcfCodes.nPreNonrefCt_TAG + gtag, rawGtNonRef(i).toString);
                 }
              }
              if(addFreq){
                val n = groupSize.toDouble
                 vb.addInfo(vcfCodes.nAF_TAG + gtag, ((homCts(i) * 2 + hetCts(i) ).toDouble / (n * 2.toDouble)).toString);
                 vb.addInfo(vcfCodes.nHomFrq_TAG + gtag, (homCts(i).toDouble / n).toString);
                 vb.addInfo(vcfCodes.nHetFrq_TAG + gtag, (hetCts(i).toDouble / n).toString);
                 vb.addInfo(vcfCodes.nMisFrq_TAG + gtag, (misCts(i).toDouble / n).toString);
                 vb.addInfo(vcfCodes.nOthFrq_TAG + gtag, ((othCts(i) + othAltCts(i)).toDouble / n).toString);
                 vb.addInfo(vcfCodes.nAltFrq_TAG + gtag, ((homCts(i) + hetCts(i) + othAltCts(i)).toDouble / n).toString);
                 vb.addInfo(vcfCodes.nNonrefFrq_TAG + gtag, ((homCts(i) + hetCts(i) + othAltCts(i) + othCts(i)).toDouble / n).toString);
  
                 if(tagFilter.isDefined){
                   vb.addInfo(vcfCodes.nFiltFrq_TAG + gtag, (filtCts(i).toDouble / n).toString);
                 }
                 if(tagPreFiltGt.isDefined){
                   vb.addInfo(vcfCodes.nPremisFrq_TAG + gtag, (rawGtMissing(i).toDouble / n).toString);
                   vb.addInfo(vcfCodes.nPreNonrefFrq_TAG + gtag, (rawGtNonRef(i).toDouble / n).toString);
                 }
                 
              }
            }}
          } else {
            error("Multiallelic group counts are not currently supported!")
            vb;
          }
        }
        vb
      }),outHeader);
    }
  }
  
  
  /*
   * 
                       new BinaryOptionArgument[String](
                                         name = "groupFile", 
                                         arg = List("--groupFile"), 
                                         valueName = "file.txt",  
                                         argDesc =  "..."
                                        ) ::
                    new BinaryArgument[String](
                                         name = "GTTag", 
                                         arg = List("--GTTag"), 
                                         valueName = "GT",  
                                         argDesc =  ".",
                                         defaultValue = Some("GT")
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "groupList", 
                                         arg = List("--groupList"), 
                                         valueName = "grpA,A1,A2,...;grpB,B1,...",  
                                         argDesc =  "..."
                                        ) ::
                    new BinaryOptionArgument[Int](
                                         name = "numLinesRead", 
                                         arg = List("--numLinesRead"), 
                                         valueName = "n",  
                                         argDesc =  "..."
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "superGroupList", 
                                         arg = List("--superGroupList"), 
                                         valueName = "sup1,grpA,grpB,...;sup2,grpC,grpD,...",  
                                         argDesc =  "..."
                                        ) ::
                                        
      
   
                   
   */
  
  def getGroups(groupFile : Option[String] = None, groupList : Option[String] = None, superGroupList : Option[String] = None) 
                        : (scala.collection.mutable.AnyRefMap[String,Set[String]],
                           scala.collection.mutable.AnyRefMap[String,Set[String]],
                           Vector[String]) = {
    
      val sampleToGroupMap = new scala.collection.mutable.AnyRefMap[String,Set[String]](x => Set[String]());
      val groupToSampleMap = new scala.collection.mutable.AnyRefMap[String,Set[String]](x => Set[String]());
      var groupSet : Set[String] = Set[String]();

      groupFile match {
        case Some(gf) => {
          val r = getLinesSmartUnzip(gf).drop(1);
          r.foreach(line => {
            val cells = line.split("\t");
            if(cells.length < 2) error("ERROR: group file must have at least 2 columns. sample.ID and group.ID!");
            
            cells.tail.foreach{ c => {
              groupToSampleMap(c) = groupToSampleMap(c) + cells(0);
              sampleToGroupMap(cells(0)) = sampleToGroupMap(cells(0)) + c;
              groupSet = groupSet + c;
            }}
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
          if(g == "ALL"){
            //do nothing
          } else {
            val r = g.split(";");
            r.foreach(grp => {
              val cells = grp.split(",");
              val grpID = cells.head;
              cells.tail.foreach(subGrp => {
                groupToSampleMap(subGrp).map{ s => {
                  sampleToGroupMap(s) = sampleToGroupMap(s) + grpID;
                }}
                groupToSampleMap(grpID) = groupToSampleMap(grpID) ++ groupToSampleMap(subGrp);
                
              })
              groupSet = groupSet + grpID;
            })
          }
        }
        case None => {
          //do nothing
        }
      }
      //val sampNames = vcfHeader.getSampleList;
      val groups = groupSet.toVector.sorted;
      
      return ((sampleToGroupMap,groupToSampleMap,groups));
  }
  
  
  case class AddAltSampLists(tagGT : String = "GT",
                             outputTagPrefix : String = "SWH_SAMPLIST_",
                             printLimit : Int = 25,
                             groupFile : Option[String] = None, groupList : Option[String] = None, superGroupList : Option[String] = None
                            ) extends internalUtils.VcfTool.SVcfWalker {
     def walkerName : String = "AddAltSampLists"
     val HET : Int = 0
     val HOMALT : Int = 1;
     val MALLE : Int = 2;
     val OTHER : Int = -1;
     val genoClasses = Seq("Het","HomAlt","mAlleHet");
     val genoClassDesc = Seq("List of samples with the HET genotype unless there are more than "+printLimit+" such  samples.",
                             "List of samples with the Homozygous Alt genotype unless there are more than "+printLimit+" such  samples.",
                             "List of samples with a multiallelic heterozygous genotype unless there are more than "+printLimit+" such samples.");
     
    val (sampleToGroupMap,groupToSampleMap,groups) = getGroups(groupFile,groupList,superGroupList);
      
      reportln("Final Groups:","debug");
      for((g,i) <- groups.zipWithIndex){
        reportln("Group "+g+" ("+groupToSampleMap(g).size+")","debug");
      }
     
    var samps : Seq[String] = null;
    def walkerParams : Seq[(String,String)] = Seq[(String,String)](("tagGT",tagGT),("outputTagPrefix",outputTagPrefix),("printLimit",printLimit.toString))
    
    val addInfoTagSet : Set[String] = genoClasses.zip(genoClassDesc).flatMap{ case (gClass,desc) => {
      Vector( outputTagPrefix+gClass ) ++ groups.toVector.map( grp => {
        outputTagPrefix+"GRP_"+grp+"_"+gClass
      })
    }}.toSet
    
    def initVCF(vcfHeader : SVcfHeader, verbose : Boolean = true) : SVcfHeader = {
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this)
      //outHeader.addInfoLine(new  SVcfCompoundHeaderLine("INFO" ,infoTag, "1", "String", "If the variant is a singleton, then this will be the ID of the sample that has the variant."))
      samps = outHeader.getSampleList
      
/*      oldFmtLines.foreach{ fl => {
        outHeader.addFormatLine(
          new SVcfCompoundHeaderLine("INFO" ,ID = fl.ID + "_multAlle", ".", fl.Type, "(For multiallelic variants, an additional value is included in this version to indicate the value for the other alt alleles) "+fl.desc)
        );
      }}*/
            
      genoClasses.zip(genoClassDesc).foreach{ case (gClass,desc) => {
        outHeader.addInfoLine((new  SVcfCompoundHeaderLine("INFO" ,
                                                         outputTagPrefix+gClass, 
                                                         ".", 
                                                         "String", 
                                                         desc + "(based on genotype field "+tagGT+")")).addWalker(this)
        )
        groups.foreach( grp => {
          outHeader.addInfoLine((new  SVcfCompoundHeaderLine("INFO" ,
                                                         outputTagPrefix+"GRP_"+grp+"_"+gClass, 
                                                         ".", 
                                                         "String", 
                                                         "(In group "+grp+") "+desc  + "(based on genotype field "+tagGT+")")).addWalker(this)
          )
        })
        
      }}
      
      outHeader
    }
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = initVCF(vcfHeader);
      val overwriteInfos = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( outHeader.addedInfos );
      if( overwriteInfos.nonEmpty ){
        notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      }
      //vc.dropInfo(overwriteInfos);
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine()
        vc.dropInfo(overwriteInfos);
        
        val gtIdx = v.genotypes.fmt.indexOf(tagGT);
        if(gtIdx == -1){
          warning("Missing genotype tag ("+gtIdx+")","MISSING_GT_TAG",10);
        } else {
          val gt : Vector[String] = v.genotypes.genotypeValues(gtIdx).toVector;
          val simpleClass : Vector[Int] = gt.map{ genoString => {
            if(genoString.contains('.')){
              OTHER
            } else if(genoString == "0/1"){
              HET
            } else if(genoString == "1/1"){
              HOMALT
            } else if(genoString == "1/2"){
              MALLE
            } else {
              OTHER
            }
          }}
          val altAlle = v.alt.head;
          genoClasses.zipWithIndex.foreach{ case (gClass,gClassIdx) => {
            val s = simpleClass.zip(samps).filter{ case (currClass,sampID) => currClass == gClassIdx}.map{ case (currClass,sampID) => sampID}
            if(s.length == 0){
              //do nothing
            } else {
              if(s.length <= printLimit){
                vc.addInfo(outputTagPrefix+""+gClass,s.mkString(","));
                groups.foreach( grp => {
                  val groupSet = groupToSampleMap(grp);
                  val gss = s.filter{ss => groupSet.contains(ss)}
                  val tag = outputTagPrefix+"GRP_"+grp+"_"+gClass
                  //if(gss.length > printLimit) {
                  //  vc.addInfo(tag,"TOO_MANY_TO_PRINT");
                  //} else if(gss.length > 0) {
                  if(gss.length > 0){
                    vc.addInfo(tag,gss.mkString(","));
                  }
                })
              } else {
                vc.addInfo(outputTagPrefix+""+gClass,"TOO_MANY_TO_PRINT");
                groups.foreach( grp => {
                  val tag = outputTagPrefix+"GRP_"+grp+"_"+gClass
                  val groupSet = groupToSampleMap(grp);
                  val gss = s.filter{ss => groupSet.contains(ss)}
                  if(gss.length > printLimit){
                    vc.addInfo(tag,"TOO_MANY_TO_PRINT");
                  } else {
                    vc.addInfo(tag,gss.mkString(","));
                  }
                })
              }
  
            }
  
          }}
        }
        //vc.addInfo(outputTagPrefix+"SAMPCT_"+c,sampCts(i).toString);

        vc
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
      
    }
  }
  
    
  class CmdConvertChromNames extends CommandLineRunUtil {
     override def priority = 1;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "ConvertChromNames", 
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
                    new BinaryArgument[Int](
                                         name = "fromCol", 
                                         arg = List("--fromCol"), 
                                         valueName = "0",  
                                         argDesc =  "",
                                         defaultValue = Some(0)
                                        ) ::
                    new BinaryArgument[Int](
                                         name = "toCol", 
                                         arg = List("--toCol"), 
                                         valueName = "1",  
                                         argDesc =  "",
                                         defaultValue = Some(1)
                                        ) ::
                    new FinalArgument[String](
                                         name = "infile",
                                         valueName = "variants.vcf",
                                         argDesc = "input VCF file. Can be gzipped or in plaintext." // description
                                        ) ::
                    new FinalArgument[String](
                                         name = "chromDecoder",
                                         valueName = "chromDecoder.txt",
                                         argDesc = "" // description
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
         ChromosomeConverter(
             chromDecoder = parser.get[String]("chromDecoder"),
             fromCol = parser.get[Int]("fromCol"),
             toCol = parser.get[Int]("toCol")
         ).walkVCFFiles(
             infiles = parser.get[String]("infile"),
             outfile = parser.get[String]("outfile"),
             chromList = parser.get[Option[List[String]]]("chromList"),
             numLinesRead = None,
             inputFileList = parser.get[Boolean]("inputFileList"),
             dropGenotypes = false
         )
       }
     }
    
  }


  case class ChromosomeConverter(chromDecoder : String, fromCol : Int = 0, toCol : Int = 1, quiet : Boolean = false) extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "ChromosomeConverter"
    val chromMap = getLinesSmartUnzip(chromDecoder).map{ line => line.split("\t") }.map{ cells => (cells(fromCol),cells(toCol)) }.toMap
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](("chromDecoder",chromDecoder.toString),
                                                                    ("fromCol",fromCol.toString),
                                                                    ("toCol",toCol.toString),
                                                                    ("quiet",quiet.toString)
                                                                    );
    
    def translateChrom(c : String) : String = {
      chromMap.get(c) match {
        case Some(tc) => {
          tc
        }
        case None => {
          if(! quiet) warning("Could not find chrom: "+c,"CHROM_NOT_FOUND",100);
          c;
        }
      }
    }
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      var errCt = 0;
      
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      //outHeader.addInfoLine(new  SVcfCompoundHeaderLine("INFO" ,infoTag, "1", "String", "If the variant is a singleton, then this will be the ID of the sample that has the variant."))
      val samps = outHeader.getSampleList
      outHeader.otherHeaderLines = outHeader.otherHeaderLines.map{ hl => {
        if(hl.tag == "contig"){
          val chl = hl.convertToContigHeaderLine().get;
          SVcfContigHeaderLine(translateChrom(chl.ID),chl.length);
        } else {
          hl
        }
      }}
      
      (addIteratorCloseAction( iter = vcIter.flatMap{v => {
        val vc = v.getOutputLine()
        vc.in_chrom = translateChrom(vc.in_chrom);
        Some(vc)
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
      
    }
  }
    
    

  class CmdFilterTags extends CommandLineRunUtil {
     override def priority = 1;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "RemoveUnwantedFields", 
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
                    new BinaryOptionArgument[List[String]](
                                         name = "keepGenotypeTags", 
                                         arg = List("--keepGenotypeTags"), 
                                         valueName = "SWH_SINGLETON_ID",  
                                         argDesc =  ""
                                        ) ::
                    new BinaryArgument[List[String]](
                                         name = "dropGenotypeTags", 
                                         arg = List("--dropGenotypeTags"), 
                                         valueName = "tag1,tag2,...",  
                                         argDesc =  "",
                                         defaultValue = Some(List[String]())
                                        ) ::
                    new BinaryOptionArgument[List[String]](
                                         name = "keepInfoTags", 
                                         arg = List("--keepInfoTags"), 
                                         valueName = "SWH_SINGLETON_ID",  
                                         argDesc =  ""
                                        ) ::
                    new BinaryArgument[List[String]](
                                         name = "dropInfoTags", 
                                         arg = List("--dropInfoTags"), 
                                         valueName = "tag1,tag2,...",  
                                         argDesc =  "",
                                         defaultValue = Some(List[String]())
                                        ) ::
                    new BinaryOptionArgument[List[String]](
                                         name = "renameInfoTags", 
                                         arg = List("--renameInfoTags"), 
                                         valueName = "oldtag1:newtag1,oldtag2:newtag2,...",  
                                         argDesc =  ""
                                        ) ::
                    new BinaryOptionArgument[List[String]](
                                         name = "keepSamples", 
                                         arg = List("--keepSamples"), 
                                         valueName = "samp1,samp2,samp3,...",  
                                         argDesc =  ""
                                        ) ::
                    new BinaryArgument[List[String]](
                                         name = "dropSamples", 
                                         arg = List("--dropSamples"), 
                                         valueName = "samp1,samp2,samp3,...",  
                                         argDesc =  "",
                                         defaultValue = Some(List[String]())
                                        ) ::
                    new UnaryArgument( name = "dropAllGenotypes",
                                         arg = List("--dropAllGenotypes"), // name of value
                                         argDesc = ""
                                       ) ::
                    new UnaryArgument( name = "alphebetizeHeader",
                                         arg = List("--alphebetizeHeader"), // name of value
                                         argDesc = ""
                                       ) ::
                    new UnaryArgument( name = "inputFileList",
                                         arg = List("--inputFileList"), // name of value
                                         argDesc = ""+
                                                   "" // description
                                       ) ::
                    new UnaryArgument( name = "dropAsteriskAlleles",
                                         arg = List("--dropAsteriskAlleles"), // name of value
                                         argDesc = ""
                                       ) ::
                    new FinalArgument[String](
                                         name = "infile",
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
         FilterTags(
             keepGenotypeTags = parser.get[Option[List[String]]]("keepGenotypeTags"),
             dropGenotypeTags = parser.get[List[String]]("dropGenotypeTags"),
             keepInfoTags = parser.get[Option[List[String]]]("keepInfoTags"),
             dropInfoTags = parser.get[List[String]]("dropInfoTags"),
             dropAsteriskAlleles = parser.get[Boolean]("dropAsteriskAlleles"),
             keepSamples = parser.get[Option[List[String]]]("keepSamples"),
             dropSamples = parser.get[List[String]]("dropSamples"),
             alphebetizeHeader = parser.get[Boolean]("alphebetizeHeader"),
             renameInfoTags = parser.get[Option[List[String]]]("renameInfoTags")
         ).walkVCFFiles(
             infiles = parser.get[String]("infile"),
             outfile = parser.get[String]("outfile"),
             chromList = parser.get[Option[List[String]]]("chromList"),
             numLinesRead = None,
             inputFileList = parser.get[Boolean]("inputFileList"),
             dropGenotypes = parser.get[Boolean]("dropAllGenotypes")
         )
       }
     }
    
  }

  case class AlphebetizeHeader() extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "AlphebetizeHeader"
    def walkerParams : Seq[(String,String)] = Seq[(String,String)]()
    def walkVCF(vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      outHeader.infoLines = outHeader.infoLines.sortBy( chi => chi.ID )
      outHeader.formatLines = outHeader.formatLines.sortBy( chi => chi.ID )
      return ( vcIter, outHeader)
    }
    
  }
  case class StripGenotypeData() extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "StripGenotypeData"
    def walkerParams : Seq[(String,String)] = Seq[(String,String)]()
    def walkVCF(vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      outHeader.titleLine = SVcfTitleLine(Seq[String]())
      
      return ( vcMap(vcIter)(vc => {
        val vb = vc.getLazyOutputLine()
        vb.removeGenotypeInfo();
        vb;
      }), outHeader)
    }
    
  }
      
  case class FilterTags(keepGenotypeTags : Option[List[String]], dropGenotypeTags : List[String], keepInfoTags : Option[List[String]], dropInfoTags : List[String],dropAsteriskAlleles : Boolean,
                        keepSamples : Option[List[String]] = None, dropSamples : List[String] = List[String](),
                        alphebetizeHeader : Boolean = true,
                        renameInfoTags : Option[List[String]] = None,
                        renameGenoTags : Option[List[String]] = None,
                        sampleRenameFile : Option[String] = None,
                        unPhaseAndSortGenotypes : Option[List[String]] = None,
                        
                        keepInfoRegex : Option[List[String]] = None,
                        dropInfoRegex : Option[List[String]] = None,
                        keepGenoRegex : Option[List[String]] = None,
                        dropGenoRegex : Option[List[String]] = None
                        ) extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "FilterTags"
    def walkerParams : Seq[(String,String)] = Seq[(String,String)](("keepGenotypeTags",fmtOptionList(keepGenotypeTags)),
                                                                   ("dropGenotypeTags",fmtList(dropGenotypeTags)),
                                                                   ("keepInfoTags",fmtOptionList(keepInfoTags)),
                                                                   ("dropInfoTags",fmtList(dropInfoTags)),
                                                                   ("dropAsteriskAlleles",dropAsteriskAlleles.toString),
                                                                   ("sampleRenameFile",sampleRenameFile.getOrElse("."))
                                                                   );
      
    val sampleDecoder : ((String) => String) = sampleRenameFile match {
      case Some(srf) => {
        val lines = getLinesSmartUnzip(srf)
        val table = getTableFromLines(lines, colNames = Seq("oldID","newID"), errorName = "File "+srf).map{ cells => (cells(0),cells(1))}.toMap;
        ((s : String) => {
          table.getOrElse(s,s);
        })
      }
      case None => {
        ((s : String) => s)
      }
    }
                                                                   
    def walkVCF(vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      var errCt = 0;
      
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      outHeader.titleLine = SVcfTitleLine(outHeader.getSampleList.map{s => sampleDecoder(s)});
      //outHeader.addInfoLine(new  SVcfCompoundHeaderLine("INFO" ,infoTag, "1", "String", "If the variant is a singleton, then this will be the ID of the sample that has the variant."))
      val samps = outHeader.getSampleList
      
      def makeRegexFunc(h : Option[List[String]], d: Boolean) : (String => Boolean) ={
        h.map{k => {
          val rx = k.map{ kk => java.util.regex.Pattern.compile(kk) }
          ((s : String) => {
            rx.exists( rr => rr.matcher(s).matches() )
          })
        }}.getOrElse( (s : String) => d )
      }
      
      val kirFunc = makeRegexFunc(keepInfoRegex,true)
      val dirFunc = makeRegexFunc(dropInfoRegex,false)
      val kgrFunc = makeRegexFunc(keepGenoRegex,true)
      val dgrFunc = makeRegexFunc(dropGenoRegex,false)
      
      val infoTags = vcfHeader.infoLines.map{infoline => infoline.ID}.toSet;
      val keepInfoSet = keepInfoTags match {
        case Some(kit) => {
          infoTags.filter{ i => (kit.contains(i) || (kirFunc(i) && (! dirFunc(i))) ) && (! (dropInfoTags.contains(i)))}.toSet
        }
        case None => {
          infoTags.filter{ i => ((kirFunc(i) && (! dirFunc(i))) ) && (! (dropInfoTags.contains(i)))}.toSet
        }
      }
      val genoTags = vcfHeader.formatLines.map{infoline => infoline.ID}.toSet;
      val keepGenoSet = keepGenotypeTags match {
        case Some(kit) => {
          genoTags.filter{ i => (kit.contains(i) || (kgrFunc(i) && (! dgrFunc(i))) ) && (! dropGenotypeTags.contains(i))}.toSet
        }
        case None => {
          genoTags.filter{ i => (                   (kgrFunc(i) && (! dgrFunc(i))) ) && (! dropGenotypeTags.contains(i))}.toSet
        }
      }
      val keepSampIndices = keepSamples match {
        case Some(x) => {
          Some(samps.zipWithIndex.filter{case (s,idx) => ( x.contains(s) ) && (! dropSamples.contains(s))}.map{_._2}.toArray)
        }
        case None => {
          if(dropSamples.size == 0){
            None
          } else {
            Some(samps.zipWithIndex.filter{case (s,idx) => ! dropSamples.contains(s)}.map{_._2}.toArray)
          }
        }
      }
      
      outHeader.formatLines = outHeader.formatLines.filter{f => keepGenoSet.contains(f.ID)}
      outHeader.infoLines = outHeader.infoLines.filter{f => keepInfoSet.contains(f.ID)}
      
      val renameGenoMapFunc : String => String = renameGenoTags match {
        case Some(renamePairStrings) => {
          val rmap = renamePairStrings.map{x => {
            val cells = x.split(":")
            if(cells.length != 2) error("renameInfoCells must consist of comma delimited pairs, with each pair separated by a colon");
            ((cells(0),cells(1)))
          }}.toMap
          outHeader.formatLines = outHeader.formatLines.map{ f => {
            rmap.get(f.ID) match {
              case Some(newid) => {
                new  SVcfCompoundHeaderLine("INFO",ID=newid,Number = f.Number, Type = f.Type,desc = f.desc);
              }
              case None => f;
            }
          }}
          (s : String) => rmap.getOrElse(s,s);
        }
        case None => {
          (s : String) => s;
        }
      }
      
      val renameMap : Option[Map[String,String]] = renameInfoTags match {
        case Some(renamePairStrings) => {
          val rmap = renamePairStrings.map{x => {
            val cells = x.split(":")
            if(cells.length != 2) error("renameInfoCells must consist of comma delimited pairs, with each pair separated by a colon");
            ((cells(0),cells(1)))
          }}.toMap
          outHeader.infoLines = outHeader.infoLines.map{ f => {
            rmap.get(f.ID) match {
              case Some(newid) => {
                new  SVcfCompoundHeaderLine("INFO",ID=newid,Number = f.Number, Type = f.Type,desc = f.desc);
              }
              case None => f;
            }
          }}
          Some(rmap);
        }
        case None => {
          None;
        }
      }
      
      
      if(alphebetizeHeader){
        outHeader.infoLines = outHeader.infoLines.sortBy( chi => chi.ID )
        outHeader.formatLines = outHeader.formatLines.sortBy( chi => chi.ID )
      }

      keepSampIndices match {
        case Some(ksi) => {
          outHeader.titleLine = SVcfTitleLine(ksi.map{ i => outHeader.titleLine.sampleList(i) }.toSeq)
        }
        case None => {
          //do nothing
        }
      }
      
      (addIteratorCloseAction( iter = vcIter.flatMap{v => {
        val vc = v.getOutputLine()
        //if(alphebetizeINFO){
        //  vc.in_info = vc.in_info.filter{ case (infoTag,infoLine) => keepInfoSet.contains(infoTag)}.
        //} else {
          
        renameMap match {
          case Some(rmap) => {
            vc.in_info = vc.in_info.map{ case (infoTag,infoLine) => {
              (rmap.getOrElse(infoTag,infoTag),infoLine);
            }}.filter{ case (infoTag,infoLine) => keepInfoSet.contains(infoTag)}
          }
          case None => {
            vc.in_info = v.info.filter{ case (infoTag,infoLine) => keepInfoSet.contains(infoTag)}
          }
        }

        
        //}
        keepSampIndices match {
          case Some(ksi) => {
            vc.genotypes.genotypeValues = v.genotypes.fmt.toArray.zipWithIndex.withFilter{ case (fmt,i) => {
              keepGenoSet.contains(fmt)
            }}.map{ case (fmt,i) => {
              ksi.map{ j => v.genotypes.genotypeValues(i)(j) }
            }}
            vc.genotypes.fmt = v.genotypes.fmt.filter{ fmt => keepGenoSet.contains(fmt) }.map{ fmt => renameGenoMapFunc(fmt) }
          }
          case None => {
            vc.genotypes.genotypeValues = v.genotypes.fmt.toArray.zipWithIndex.withFilter{ case (fmt,i) => keepGenoSet.contains(fmt)}.map{ case (fmt,i) => vc.genotypes.genotypeValues(i) }
            vc.genotypes.fmt = v.genotypes.fmt.filter{ fmt => keepGenoSet.contains(fmt) }.map{ fmt => renameGenoMapFunc(fmt) }
          }
        }
        val gtGenoIdx = vc.genotypes.fmt.indexOf("GT");
        if(gtGenoIdx > 0){
          val temp : Array[String] = vc.genotypes.genotypeValues( 0 )
          vc.genotypes.genotypeValues(0) = vc.genotypes.genotypeValues(gtGenoIdx);
          vc.genotypes.genotypeValues(gtGenoIdx) = temp;
          vc.genotypes.fmt = vc.genotypes.fmt.updated(gtGenoIdx,vc.genotypes.fmt(0));
          vc.genotypes.fmt = vc.genotypes.fmt.updated(0,"GT");
        }
        
        unPhaseAndSortGenotypes match {
          case Some(gtags) => {
            gtags.withFilter{ gtag => vc.genotypes.fmt.contains(gtag) }.foreach{ gtag => {
              val tagidx =  vc.genotypes.fmt.indexOf(gtag)
              if(tagidx != -1){
                vc.genotypes.genotypeValues(tagidx) = vc.genotypes.genotypeValues(tagidx).map{ gv => {
                  if(gv.contains('.')){
                    "."
                  } else {
                    gv.split("[\\|/]").sortBy{ gg => gg.toInt}.mkString("/");
                  }
                }}
              }
            }}
          }
          case None => {
            //do nothing
          }
        }
        
        Some(vc)
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
      
    }
  }
    
    
  class CmdExtractSingletons extends CommandLineRunUtil {
     override def priority = 20;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "extractSingletons", 
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
                    new BinaryOptionArgument[String](
                                         name = "intervalBedFile", 
                                         arg = List("--intervalBedFile"), 
                                         valueName = "bedfile.bed",  
                                         argDesc =  ""
                                        ) ::
                    new BinaryArgument[String](
                                         name = "GenoTag", 
                                         arg = List("--GenoTag"), 
                                         valueName = "GT",  
                                         argDesc =  "The genotype tag used for the first file.",
                                         defaultValue = Some("GT")
                                        ) ::
                    new BinaryArgument[String](
                                         name = "outputTag", 
                                         arg = List("--outputTag"), 
                                         valueName = "SWH_SINGLETON_ID",  
                                         argDesc =  "",
                                         defaultValue = Some("SWH_SINGLETON_ID")
                                        ) ::
                    new UnaryArgument( name = "dropGenotypes",
                                         arg = List("--dropGenotypes"), // name of value
                                         argDesc = ""
                                       ) ::
                    new UnaryArgument( name = "keepOnlySingletons",
                                         arg = List("--keepOnlySingletons"), // name of value
                                         argDesc = ""+
                                                   "" // description
                                       ) ::
                    new FinalArgument[String](
                                         name = "infile",
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
         ExtractSingletons(
             gttag = parser.get[String]("GenoTag"),
             dropNonSingletons = parser.get[Boolean]("keepOnlySingletons"),
             infoTag = parser.get[String]("outputTag"),
             bedFile = parser.get[Option[String]]("intervalBedFile")
         ).walkVCFFile(
             infile = parser.get[String]("infile"),
             outfile = parser.get[String]("outfile"),
             chromList = parser.get[Option[List[String]]]("chromList"),
             numLinesRead = None,
             dropGenotypes = parser.get[Boolean]("dropGenotypes")
         )
       }
     }
    
  }

  case class ExtractSingletons(gttag : String = "GT", infoTag : String = "SWH_SINGLETON_ID", dropNonSingletons : Boolean = true, dropGenotypes : Boolean = true,
                               bedFile : Option[String] = None) extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "ExtractSingletons"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](("gttag",gttag.toString),
                                                                    ("infoTag",infoTag.toString),
                                                                    ("dropNonSingletons",dropNonSingletons.toString),
                                                                    ("dropGenotypes",dropGenotypes.toString),
                                                                    ("bedFile",bedFile.getOrElse("None"))
                                                                    );
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      var errCt = 0;
      
      val bedIdTag = "SWH_INTERVAL_IVID"
      
      val outHeader = vcfHeader;
      outHeader.addInfoLine(new  SVcfCompoundHeaderLine("INFO" ,infoTag, "1", "String", "If the variant is a singleton, then this will be the ID of the sample that has the variant."))
      outHeader.addWalk(this);
      
      val ivMap = bedFile match {
        case Some(f) => {
          outHeader.addInfoLine(new  SVcfCompoundHeaderLine("INFO" ,bedIdTag, ".", "String", "The name of the interval region where this variant is found."))
          val arr : internalUtils.genomicAnnoUtils.GenomicArrayOfSets[String] = internalUtils.genomicAnnoUtils.GenomicArrayOfSets[String](false);
          reportln("   Beginning bed file read: "+f+" ("+getDateAndTimeString+")","debug");
          val lines = getLinesSmartUnzip(f);
          lines.map{line => line.split("\t")}.foreach(cells => {
              val (chrom,start,end,name) = (cells(0),string2int(cells(1)),string2int(cells(2)),cells(3))
              if(start != end){ 
                arr.addSpan(internalUtils.commonSeqUtils.GenomicInterval(chrom, '.', start,end),name);
              }
          })
          arr.finalizeStepVectors;
          ((c : String, p : Int) => {
            val currIvNames = arr.findIntersectingSteps(internalUtils.commonSeqUtils.GenomicInterval(c, '.', p,p+1)).foldLeft(Set[String]()){ case (soFar,(iv,currSet)) => {
              soFar ++ currSet
            }}.toList.sorted
            if(currIvNames.size > 1){
              warning("Warning: Variant spans multiple IVs","VariantSpansMultipleIVs",100)
            }
            currIvNames
          })
          
        }
        case None => {
          ((c : String, p : Int) => List[String]())
        }
      }
      
      val samps = outHeader.getSampleList
      
      (addIteratorCloseAction( iter = vcIter.flatMap{v => {
        val vc = v.getOutputLine()
        val gtidx = vc.genotypes.fmt.indexOf(gttag);
        val gt = vc.genotypes.genotypeValues(gtidx);
        val numAlt = gt.zipWithIndex.filter{ case (g,i) => { g.split("[/\\|]").exists(_ == "1") }};
        
        if(numAlt.length == 1){
          vc.addInfo(infoTag, samps(numAlt.head._2))
          val ivNames = ivMap(vc.chrom,vc.pos);
          if(ivNames.length > 0){
            vc.addInfo(bedIdTag, ivNames.mkString(","))
          }
          Some(vc);
        } else if(dropNonSingletons){
          None;
        } else {
          val ivNames = ivMap(vc.chrom,vc.pos);
          if(ivNames.length > 0){
            vc.addInfo(bedIdTag, ivNames.mkString(","))
          }
          Some(vc)
        }
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
      
    }

    
  }
    
  class CmdCalcGenotypeStatTable  extends CommandLineRunUtil{
     override def priority = 40;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "CalcGenotypeStatTable", 
          quickSynopsis = "Generates stat tables for genotype statistics (eg GQ, AD)", 
          synopsis = "", 
          description = "This takes a stat table for genotype statistics (such as GQ or AD). Warning: does not function on phased genotypes! " + ALPHA_WARNING,
          argList = 
                    new BinaryOptionArgument[List[String]](
                                         name = "chromList", 
                                         arg = List("--chromList"), 
                                         valueName = "chr1,chr2,...",  
                                         argDesc =  "List of chromosomes. If supplied, then all analysis will be restricted to "+
                                                    "these chromosomes. All other chromosomes wil be ignored."
                                        ) ::
                                        /*
                    new BinaryOptionArgument[String](
                                         name = "sampleDecoder", 
                                         arg = List("--sampleDecoder"), 
                                         valueName = "sampleDecoder.txt",  
                                         argDesc =  ""
                                        ) ::
                    new BinaryArgument[String](
                                         name = "GenoTag1", 
                                         arg = List("--GenoTag1"), 
                                         valueName = "GT",  
                                         argDesc =  "",
                                         defaultValue = Some("GT")
                                        ) ::
                    new UnaryArgument( name = "infileList",
                                         arg = List("--infileList"), // name of value
                                         argDesc = ""+
                                                   "" // description
                                       ) ::*/
                                        
                    new BinaryArgument[String](
                                         name = "GenoTag", 
                                         arg = List("--GenoTag"), 
                                         valueName = "GT",  
                                         argDesc =  "The tag used to indicate genotype.",
                                         defaultValue = Some("GT")
                                        ) ::
                    new UnaryArgument( name = "infileList",
                                         arg = List("--infileList"), // name of value
                                         argDesc = "If this option is used, then instead of a single input file the input file(s) will be assumed "+
                                                   "to be a file containing a list of input files to parse in order. If multiple VCF files are specified, "+
                                                   "the vcf lines will be concatenated and the header will be taken from the first file."+
                                                   "" // description
                                       ) ::
                    new UnaryArgument( name = "byBaseSwap",
                                         arg = List("--byBaseSwap"), // name of value
                                         argDesc = "Placeholder: NOT YET IMPLEMENTED!"+
                                                   "" // description
                                       ) ::
                    new BinaryOptionArgument[String](
                                         name = "subFilterExpressionSets", 
                                         arg = List("--subFilterExpressionSets"), 
                                         valueName = "",  
                                         argDesc =  "Placeholder: NOT YET IMPLEMENTED!"
                                        ) ::
                    new FinalArgument[String](
                                         name = "infile",
                                         valueName = "variants.vcf",
                                         argDesc = "input VCF file. Can be gzipped or in plaintext." // description
                                        ) ::
                    new FinalArgument[String](
                                         name = "tagFile",
                                         valueName = "tagFile.txt",
                                         argDesc = "Can be gzipped or in plaintext. This is a special text file that specifies which genotype tags to examine and "+
                                                   "what stats to collect. It must be a tab-delimited file with a variable number of rows. "+
                                                   "The first row must be the line type, which is either \"TAG\" or \"PAIR\". "+
                                                   "For TAG lines: the second column is the title to be used for the tag. "+
                                                   "The third column is the tag ID as it appears in the VCF file. "+
                                                   "The fourth column is the format or function to collect. Options are: "+
                                                   "Int (the tag is a simple Int), sumInt (the tag is a series of Ints, collect the sum), get0 (the tag is a series of ints, collect the first value), get1 (the tag is a series of ints, collect the second value). "+
                                                   "The fifth should specify the counting bins to use, as a comma-delimited list of underscore-delimited numbers. The bins are specified as lower-bound-inclusive. "+
                                                   "PAIR rows should just have 2 additional columns: one specifying one of the tag titles that appears in one of the TAG lines in this file. "+
                                                   "This will cause the utility to count a crosswise table comparing the two specified stats across the specified windows."
                                        ) ::
                    new FinalArgument[String](
                                         name = "outfileprefix",
                                         valueName = "outfileprefix",
                                         argDesc = "The output file."// description
                                        ) ::
                    internalUtils.commandLineUI.CLUI_UNIVERSAL_ARGS );

     def run(args : Array[String]) {
       val out = parser.parseArguments(args.toList.tail);
       if(out){
         CalcGenotypeTableStat(
                          infile = parser.get[String]("infile"),
                          outfile = parser.get[String]("outfileprefix"),
                          tagFile = parser.get[String]("tagFile"),
                          chromList = parser.get[Option[List[String]]]("chromList"),
                          genoTag = parser.get[String]("GenoTag"),
                          infileList = parser.get[Boolean]("infileList"),
                          filterExpressionSet = parser.get[Option[String]]("subFilterExpressionSets")
         ).run()
       }
     }
    
  }
  
  case class CalcGenotypeTableStat(infile : String,
                              outfile : String,
                              tagFile : String,
                              chromList : Option[List[String]],
                              genoTag : String,
                              infileList : Boolean,
                              filterExpressionSet : Option[String],
                              byBaseSwap : Boolean = false) {
    
    val NUM_GT_IDXES = 6;
    val GT_IDXES_LABELS = Vector("Miss","HomRef","Het","HomAlt","OtherAlt","Other");
    
    def getGTIdx(gt : String) : Int = {
          if(gt.contains('.')){
            0
          } else if(gt == "0/0"){
            1
          } else if(gt == "0/1"){
            2
          } else if(gt == "1/1"){
            3
          } else if(gt.split("[/|]").contains("1")){
            4
          } else {
            5
          }
    }
    
    val tagSet = getLinesSmartUnzip(tagFile).filter(line => line.startsWith("TAG")).toVector.map{ line =>{
      val cells = line.split("\t").tail;
      val tagTitle = cells(0);
      val tagID = cells(1);
      val tagFmt = cells(2);
      val tagReadFunc : (String => Int) = if(tagFmt == "Int"){
        ((tv : String) => if(tv == "."){0} else {string2int(tv)})
      } else if(tagFmt == "sumInt"){
        ((tv : String) => if(tv == "."){0} else {tv.split(",").map{string2int(_)}.sum})
      } else if(tagFmt == "get0"){
        ((tv : String) => if(tv == "."){0} else {string2int(tv.split(",")(0))} )
      } else if(tagFmt == "get1"){
        ((tv : String) => if(tv == "."){0} else {string2int(tv.split(",")(1))} )
      } else {
        error("Fatal error: unsupported TAG format!");
        ((tv : String) => 0)
      }
      
      val tagIV = cells(3).split(",").map{cell => {cell.split("_").map{string2int(_)}}}.map{s => (s(0),s(1))}
      val arrayLen = tagIV.length + 3;
      val lowIdx = tagIV.length;
      val highIdx = tagIV.length + 1;
      val naIdx = tagIV.length + 2;
      val arrayLabels = tagIV.map{ case (l : Int,h : Int) => l+"-"+h} ++ Vector[String]("<"+tagIV.head._1,">="+tagIV.last._2,"NA")
      val getIndices = ((gs : SVcfGenotypeSet) => {
        val tagIdx = gs.fmt.indexOf(tagID);
        if(tagIdx != -1){
          gs.genotypeValues(0).indices.toArray.map{ sampleIdx => {
            val tagVal = tagReadFunc(gs.genotypeValues(tagIdx)(sampleIdx));
            if(tagVal < tagIV.head._1){
              lowIdx
            } else if(tagVal >= tagIV.last._2){
              highIdx
            } else {
              tagIV.indexWhere{case (l : Int,h : Int) => tagVal >= l && tagVal < h}
            }
          }}
        } else {
          Array.fill[Int](gs.genotypeValues(0).length)(naIdx);
        }
      })
      
      (tagTitle,tagID,tagFmt,arrayLabels,getIndices);
    }}
    
    val tagPairs = getLinesSmartUnzip(tagFile).filter(line => line.startsWith("PAIR")).toVector.map{ line =>{
      val cells = line.split("\t");
      if(cells.length != 3) error("Malformed tagfile! PAIR lines must have 3 columns!")
      val tagTitles = cells.tail;
      val tagIdxes = tagTitles.map{tt => tagSet.indexWhere{ case (tagTitle,tagID,tagFmt,arrayLabels,getIndicesFunc) => { 
        tagTitle == tt
      }}}
      (tagIdxes(0), tagIdxes(1))
    }}
    
    def getAllIndices(vc : SVcfVariantLine) : (Array[Int],Array[Array[Int]]) = {
      val gtTagIdx = vc.genotypes.fmt.indexOf(genoTag);
      val gtIndices = vc.genotypes.genotypeValues(gtTagIdx).map{gt => {getGTIdx(gt)}}.toArray
      val tagIndices = tagSet.map{ case (tagTitle,tagID,tagFmt,arrayLabels,getIndicesFunc) => {
        getIndicesFunc(vc.genotypes);
      }}.toArray
      (gtIndices, tagIndices);
    }
    
    class VariantCountSetByAllTags(ct : Int, prefix : String = "") {
      val tagCts = tagSet.map{ case (tagTitle,tagID,tagFmt,arrayLabels,getIndicesFunc) => {
        Array.fill[Int](arrayLabels.length,ct,NUM_GT_IDXES)(0);
      }}.toArray;
      val tagPairCts = tagPairs.map{ case (idx1,idx2) => {
        Array.fill[Int](tagSet(idx1)._4.length,tagSet(idx2)._4.length,ct,NUM_GT_IDXES)(0);
      }}.toArray;
      
      def addVC(gtIndices : Array[Int], tagIndices : Array[Array[Int]]){
        tagIndices.indices.foreach{ i =>
          tagIndices(i).indices.foreach{ j => {
            tagCts(i)(tagIndices(i)(j))(j)(gtIndices(j)) += 1;
          }}
        }
        tagPairs.indices.map{ case i => {
          val (i1,i2) = tagPairs(i);
          tagIndices(i1).indices.foreach{ j => {
            //warning("Attempting to access tagPairCts("+i+")(tagIndices("+i1+")("+j+"))(tagIndices("+i2+")("+j+"))("+j+")(gtIndices("+j+"))","debugWarn",-1);
            //warning("                     tagPairCts("+i+")("+tagIndices(i1)(j)+")("+tagIndices(i2)(j)+")("+j+")("+gtIndices(j)+")","debugWarn",-1);
            tagPairCts(i)(tagIndices(i1)(j))(tagIndices(i2)(j))(j)(gtIndices(j)) += 1;
          }}
        }}
      }
      
      def getOutputLines() : Iterator[String] = {
        tagSet.zipWithIndex.iterator.flatMap{case ((tagTitle,tagID,tagFmt,arrayLabels,getIndicesFunc),i) => {
          arrayLabels.indices.flatMap{ j => {
            (0 until NUM_GT_IDXES).map{ gtidx => {
              prefix+tagTitle+":"+arrayLabels(j)+":"+GT_IDXES_LABELS(gtidx)+"\t"+
              (0 until ct).map{sampIdx => {
                tagCts(i)(j)(sampIdx)(gtidx);
              }}.mkString("\t");
            }}
          }}
        }} ++ 
        tagPairs.zipWithIndex.iterator.flatMap{ case ((i1,i2),i) => {
          val (tagTitle1,tagID1,tagFmt1,arrayLabels1,getIndicesFunc1) = tagSet(i1);
          val (tagTitle2,tagID2,tagFmt2,arrayLabels2,getIndicesFunc2) = tagSet(i2);
          arrayLabels1.indices.flatMap{ j1 => {
            arrayLabels2.indices.flatMap{ j2 => {
              (0 until NUM_GT_IDXES).map{ gtidx => {
                prefix+"TAGPAIR:"+tagTitle1+":"+tagTitle2+":"+arrayLabels1(j1)+":"+arrayLabels2(j2)+":"+GT_IDXES_LABELS(gtidx)+"\t"+
                (0 until ct).map{sampIdx => {
                  tagPairCts(i)(j1)(j2)(sampIdx)(gtidx);
                }}.mkString("\t");
              }}
            }}
          }}
        }}
      }
    }
    
    /*
      val subsetList : Seq[(String,SFilterLogic[SVcfVariantLine],VariantCountSetSet)] = subFilterExpressionSets match { 
        case Some(sfes) => {
          val sfesSeq = sfes.split(",")
          sfesSeq.map{ sfe =>
            val sfecells = sfe.split("=").map(_.trim());
            if(sfecells.length != 2) error("ERROR: subfilterExpression must have format: subfiltertitle=subfilterexpression");
            val (sfName,sfString) = (sfecells(0),sfecells(1));
            val parser : SVcfFilterLogicParser = internalUtils.VcfTool.SVcfFilterLogicParser();
            val filter : SFilterLogic[SVcfVariantLine] = parser.parseString(sfString);
            (sfName+"_",filter,makeVariantCountSetSet(sampleCt))
          }
        }
        case None => {
          Seq();
        }
      }
      val setList : Seq[(String,SFilterLogic[SVcfVariantLine],VariantCountSetSet)] = Seq(("",SFilterTrue[SVcfVariantLine](),makeVariantCountSetSet(sampleCt))) ++ subsetList;
       */
    
    def run(){
      val (vcIterRaw, vcfHeader) = getSVcfIterators(infile,chromList,None,inputFileList = infileList);
      val vcIter = vcIterRaw.buffered;
      
      val cts = new VariantCountSetByAllTags(vcfHeader.sampleCt);
      val snvCts = new VariantCountSetByAllTags(vcfHeader.sampleCt,"SNV:");
      //val indelCts = new VariantCountSetByAllTags(vcfHeader.sampleCt,"INDEL:");
      val biSnvCts = new VariantCountSetByAllTags(vcfHeader.sampleCt,"BIALLESNV:");
      val counters = Seq(cts,snvCts,biSnvCts);
        
      vcIter.foreach{vc => {
        if(vc.filter == "."){
          val (gtIndices, tagIndices) = getAllIndices(vc);
          cts.addVC(gtIndices,tagIndices);
          if(vc.ref.length == 1 && vc.alt.head.length == 1){
            snvCts.addVC(gtIndices,tagIndices);
            if(vc.alt.length == 1){
              biSnvCts.addVC(gtIndices,tagIndices);
            }
          } //else {
            //indelCts.addVC(gtIndices,tagIndices);
          //}
        }
      }}
      
      val writer = openWriterSmart(outfile);
      writer.write("FIELD\t"+vcfHeader.getSampleList.mkString("\t")+"\n");
      counters.foreach{ c => {
        c.getOutputLines().foreach{ line => {
          writer.write(line+"\n");
        }}
      }}
      
      //cts.getOutputLines().foreach{ line => {
      //  writer.write(line+"\n");
      //}}
      writer.close();
    }
  }
    
    
    
  class compareVcfs extends CommandLineRunUtil {
     override def priority = 25;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "compareVcfs", 
          quickSynopsis = "Compares two variant call builds", 
          synopsis = "", 
          description = "Compares two different VCFs containing different builds with overlapping sample sets." + ALPHA_WARNING,
          argList = 
                    new BinaryOptionArgument[List[String]](
                                         name = "chromList", 
                                         arg = List("--chromList"), 
                                         valueName = "chr1,chr2,...",  
                                         argDesc =  "List of chromosomes. If supplied, then all analysis will be restricted to these chromosomes. All other chromosomes wil be ignored."
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "sampleDecoder", 
                                         arg = List("--sampleDecoder"), 
                                         valueName = "sampleDecoder.txt",  
                                         argDesc =  ""
                                        ) ::
                    new BinaryArgument[String](
                                         name = "GenoTag1", 
                                         arg = List("--GenoTag1"), 
                                         valueName = "GT",  
                                         argDesc =  "The genotype tag used for the first file.",
                                         defaultValue = Some("GT")
                                        ) ::
                    new BinaryArgument[String](
                                         name = "GenoTag2", 
                                         arg = List("--GenoTag2"), 
                                         valueName = "GT",  
                                         argDesc =  "The genotype tag used for the second file.",
                                         defaultValue = Some("GT")
                                        ) ::
                    new UnaryArgument( name = "infileList",
                                         arg = List("--infileList"), // name of value
                                         argDesc = "If this option is used, then instead of a single input file the input file(s) will be assumed "+
                                                   "to be a file containing a list of input files to parse in order. If multiple VCF files are specified, "+
                                                   "the vcf lines will be concatenated and the header will be taken from the first file."+
                                                   "" // description
                                       ) ::
                    new UnaryArgument( name = "noGzipOutput",
                                         arg = List("--noGzipOutput"), // name of value
                                         argDesc = ""+
                                                   "" // description
                                       ) ::
                    new BinaryOptionArgument[String](
                                         name = "filterExpression1", 
                                         arg = List("--filterExpression1"), 
                                         valueName = "filterExpr",  
                                         argDesc =  ""
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "filterExpression2", 
                                         arg = List("--filterExpression2"), 
                                         valueName = "filterExpr",  
                                         argDesc =  ""
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "bedFile", 
                                         arg = List("--bedFile"), 
                                         valueName = "bedFile.bed.gz",  
                                         argDesc =  ""
                                        ) ::
                    new FinalArgument[String](
                                         name = "infile1",
                                         valueName = "variants1.vcf",
                                         argDesc = "input VCF file. Can be gzipped or in plaintext." // description
                                        ) ::
                    new FinalArgument[String](
                                         name = "infile2",
                                         valueName = "variants2.vcf",
                                         argDesc = "input VCF file. Can be gzipped or in plaintext." // description
                                        ) :: 
                    new FinalArgument[String](
                                         name = "outfileprefix",
                                         valueName = "outfileprefix",
                                         argDesc = "The output file."// description
                                        ) ::
                    internalUtils.commandLineUI.CLUI_UNIVERSAL_ARGS );

     def run(args : Array[String]) {
       val out = parser.parseArguments(args.toList.tail);
       if(out){
         VcfStreamCompare(
                          infile1 = parser.get[String]("infile1"),
                          infile2 = parser.get[String]("infile2"),
                          outfile = parser.get[String]("outfileprefix"),
                          chromList = parser.get[Option[List[String]]]("chromList"),
                          genoTag1 = parser.get[String]("GenoTag1"),
                          genoTag2 = parser.get[String]("GenoTag2"),
                          infileList = parser.get[Boolean]("infileList"),
                          gzipOutput = ! parser.get[Boolean]("noGzipOutput"),
                          sampleDecoder = parser.get[Option[String]]("sampleDecoder"),
                          filterExpression1 = parser.get[Option[String]]("filterExpression1"),
                          filterExpression2 = parser.get[Option[String]]("filterExpression2"),
                          bedFile = parser.get[Option[String]]("bedFile")
         ).run()
       }
     }
    
  }
  

  
  
  case class VcfStreamCompare(infile1 : String, infile2 : String, outfile : String,
                              chromList : Option[List[String]],
                              genoTag1 : String,
                              genoTag2 : String,
                              file2tag : String = "B_",
                              file2Desc : String = "(For Alt Build) ",
                              infileList : Boolean,
                              gzipOutput : Boolean,
                              sampleDecoder : Option[String],
                              filterExpression1: Option[String],
                              filterExpression2 : Option[String],
                              bedFile : Option[String] = None,
                              debugMode : Boolean = true){
    
    val bedFilter : Option[(SVcfVariantLine => Boolean)] = bedFile match {
      case Some(f) => {
            val chromFunc = chromList match {
              case Some(cl) => {
                val chromSet = cl.toSet;
                (chr : String) => chromSet.contains(chr);
              }
              case None => {
                (chr : String) => true
              }
            }
            val arr : internalUtils.genomicAnnoUtils.GenomicArrayOfSets[String] = internalUtils.genomicAnnoUtils.GenomicArrayOfSets[String](false);
            reportln("   Beginning bed file read: "+f+" ("+getDateAndTimeString+")","debug");
            val lines = getLinesSmartUnzip(f);
            lines.map{line => line.split("\t")}.withFilter{cells => { chromFunc(cells(0)) }}.foreach(cells => {
              val (chrom,start,end) = (cells(0),string2int(cells(1)),string2int(cells(2)))
              arr.addSpan(internalUtils.commonSeqUtils.GenomicInterval(chrom, '.', start,end), "CE");
            })
            arr.finalizeStepVectors;
            Some(((vc : SVcfVariantLine) => {
              val iv = internalUtils.commonSeqUtils.GenomicInterval(vc.chrom,'.', start = vc.pos - 1, end = math.max(vc.pos,vc.pos + vc.ref.length - 1));
              arr.findIntersectingSteps(iv).exists{ case (iv, currSet) => { ! currSet.isEmpty }}
            }))
      }
      case None => {
        None
      }
    }
    //val variantIV = internalUtils.commonSeqUtils.GenomicInterval(v.getContig(),'.', start = v.getStart() - 1, end = math.max(v.getEnd(),v.getStart+1));
    val filterparser : SFilterLogicParser[SVcfVariantLine] = internalUtils.VcfTool.sVcfFilterLogicParser;
    val filter1 : SFilterLogic[SVcfVariantLine] = filterExpression1 match {
      case Some(sfString) => filterparser.parseString(sfString);
      case None => SFilterTrue[SVcfVariantLine]();
    }
    val filter2 : SFilterLogic[SVcfVariantLine] = filterExpression2 match {
      case Some(sfString) => filterparser.parseString(sfString);
      case None => SFilterTrue[SVcfVariantLine]();
    }
    
    val (vcIterRaw1, vcfHeader1) = getSVcfIterators(infile1,chromList,None,inputFileList = infileList);
    val (vcIterRaw2, vcfHeader2) = getSVcfIterators(infile2,chromList,None,inputFileList = infileList, withProgress = false);
    val (vcIter1,vcIter2) = (vcIterRaw1.filter(vc => filter1.keep(vc)).buffered, vcIterRaw2.filter(vc => filter2.keep(vc)).buffered)
    var currChrom = vcIter1.head.chrom;
    
    val outfileSuffix = if(gzipOutput) ".gz" else "";
    
    val matchIdx : Seq[(String,Int,Int)] = sampleDecoder match {
      case Some(decoderFile) => {
        getLinesSmartUnzip(decoderFile).flatMap{ line => {
          val cells = line.split("\t");
          val (s1,s2) = (cells(0),cells(1));
          val idx1 = vcfHeader1.titleLine.sampleList.indexOf(s1)
          val idx2 = vcfHeader2.titleLine.sampleList.indexOf(s2)
          if(idx1 != -1 && idx2 != -1){
            Some((s1,idx1,idx2));
          } else {
            None;
          }
        }}.toSeq
      }
      case None => {
        vcfHeader1.titleLine.sampleList.zipWithIndex.flatMap{ case (s,idx1) => {
          val idx2 = vcfHeader2.titleLine.sampleList.indexOf(s)
          if(idx2 == -1){
            None
          } else {
            Some((s,idx1,idx2))
          }
        }}.toSeq
      }
    }
    
    val matchIdxIdx = matchIdx.indices;
    
    if(debugMode){
      reportln("MATCHES:","debug");
      matchIdx.foreach{case (matchName,idx1,idx2) => {
        reportln("   "+matchName+"\t"+idx1+"\t"+idx2,"debug")
      }}
    }
    
    /*
     * A>C, T>G
     * A>T, T>A
     * A>G, T>C
     * C>A, G>T
     * C>T, G>A
     * C>G, G>C
     */

    val fGenoTag2 = file2tag + genoTag2;

    val vcfHeaderOut = SVcfHeader(infoLines = vcfHeader1.infoLines, 
                        formatLines = vcfHeader1.formatLines ++ vcfHeader2.formatLines.map{ fl => {
                          new  SVcfCompoundHeaderLine(in_tag = fl.in_tag, ID = file2tag+fl.ID, Number = fl.Number, Type = fl.Type, desc = file2Desc + fl.desc)
                        }},
                        otherHeaderLines = vcfHeader1.otherHeaderLines,
                        walkLines = vcfHeader1.walkLines,
                        titleLine = SVcfTitleLine(sampleList = matchIdx.map{_._1}.toSeq)
                     )

    
    def isFirst : Boolean = vcIter1.hasNext && (
                        (! vcIter2.hasNext) || 
                        ( vcIter1.head.chrom == vcIter2.head.chrom && vcIter1.head.pos <= vcIter2.head.pos) ||
                        ( vcIter1.head.chrom == currChrom && vcIter2.head.chrom != currChrom ) 
                   );
    
    val outM = openWriterSmart(outfile+"shared.vcf"+outfileSuffix);
    //val outMM = openWriterSmart(outfile+"sharedMis.vcf.gz");
    val out1 = openWriterSmart(outfile+"F1.vcf"+outfileSuffix);
    val out2 = openWriterSmart(outfile+"F2.vcf"+outfileSuffix);
    val writers = Seq(outM,out1,out2);
    
    val matchWriter = openWriterSmart(outfile+"summary.match.txt"+outfileSuffix);
    val AWriter = openWriterSmart(outfile+"summary.A.txt"+outfileSuffix);
    val BWriter = openWriterSmart(outfile+"summary.B.txt"+outfileSuffix);

    writers.foreach(out => {
      vcfHeaderOut.getVcfLines.foreach{ l =>
        out.write(l+"\n");
      }
    })
    
    var matchCt = 0;
    var at1not2ct = 0;
    var at2not1ct = 0;
    var alleAt1not2ct = 0;
    var alleAt2not1ct = 0;
    
    def run(){
      //reportln("> ITERATE()","debug");
      while(vcIter1.hasNext || vcIter2.hasNext){
        //reportln("> ITERATE()","debug");
        iterate();
      }
      writers.foreach(out => {
        out.close();
      })
      matchWriter.write(
            "sample.ID\t"+matchCountFunctionList_FINAL.map{ case (id,arr,varFcn,fcn) => {
              id
            }}.mkString("\t")+"\n"
      );
      AWriter.write(
            "sample.ID\t"+mmCountFunctionList_A.map{ case (id,arr,varFcn,fcn) => {
              id
            }}.mkString("\t")+"\n"
      );
      BWriter.write(
            "sample.ID\t"+mmCountFunctionList_B.map{ case (id,arr,varFcn,fcn) => {
              id
            }}.mkString("\t")+"\n"
      );
      
      
      matchIdx.zipWithIndex.foreach{ case ((sampid,idx1,idx2),i) => {
        matchWriter.write(
            sampid+"\t"+matchCountFunctionList_FINAL.map{ case (id,arr,varFcn,fcn) => {
              arr(i);
            }}.mkString("\t")+"\n"
        );
        AWriter.write(
            sampid+"\t"+mmCountFunctionList_A.map{ case (id,arr,varFcn,fcn) => {
              arr(i);
            }}.mkString("\t")+"\n"
        );
        BWriter.write(
            sampid+"\t"+mmCountFunctionList_B.map{ case (id,arr,varFcn,fcn) => {
              arr(i);
            }}.mkString("\t")+"\n"
        );
      }}
      matchWriter.close();
      AWriter.close();
      BWriter.close();
      
    }
    
    def iterate(){
      val vcos = if(
                    (vcIter1.hasNext & ! vcIter2.hasNext)  || (vcIter2.hasNext & ! vcIter1.hasNext) || 
                    (vcIter1.head.pos != vcIter2.head.pos) || (vcIter1.head.chrom != vcIter2.head.chrom)){
        //reportln("   Iterating Isolated position.","deepDebug");
        if(isFirst){
          val vc = vcIter1.next();
          //reportln("   Iterating VC1 Isolated Position: "+vc.chrom+":"+vc.pos,"debug");
          at1not2ct += 1;
          writeVC1(vc);
        } else {
          val vc = vcIter2.next().getOutputLine();
          //reportln("   Iterating VC2 Isolated Position: "+vc.chrom+":"+vc.pos,"debug");
          at2not1ct += 1;
          writeVC2(vc);
        };
      } else {
        //reportln("   Iterating Shared Position: "+vcIter1.head.chrom+":"+vcIter1.head.pos,"debug");
        iteratePos();
      }
    }
    
    def isCalled(vc : SVcfVariantLine, idx : Int, i : Int) : Boolean = {
      (idx != -1) && ! vc.genotypes.genotypeValues(idx)(i).contains('.')
    }
    def isCalled(vc : SVcfVariantLine, idx1 : Int, idx2 : Int, i : Int) : Boolean = {
      isCalled(vc,idx1,i) && isCalled(vc,idx2,i);
    }
    def isMatch(vc : SVcfVariantLine,idx1 : Int, idx2: Int, i : Int) : Boolean = {
      isCalled(vc,idx1,i) && vc.genotypes.genotypeValues(idx1)(i) == vc.genotypes.genotypeValues(idx2)(i)
    }
    def isMisMatch(vc : SVcfVariantLine,idx1 : Int, idx2: Int, i : Int) : Boolean = {
      isCalled(vc,idx1,i) && isCalled(vc,idx2,i) && vc.genotypes.genotypeValues(idx1)(i) != vc.genotypes.genotypeValues(idx2)(i)
    }
    def isSNV(vc : SVcfVariantLine) : Boolean = {
      vc.ref.length == vc.alt.head.length && vc.ref.length == 1
    }
    def isRef(vc : SVcfVariantLine, idx : Int, i : Int) : Boolean = {
       isCalled(vc,idx,i) && vc.genotypes.genotypeValues(idx)(i) == "0/0";
    }
    def isHomAlt(vc : SVcfVariantLine, idx : Int, i : Int) : Boolean = {
       isCalled(vc,idx,i) && vc.genotypes.genotypeValues(idx)(i) == "1/1";
    }
    def isHet(vc : SVcfVariantLine, idx : Int, i : Int) : Boolean = {
      isCalled(vc,idx,i) && vc.genotypes.genotypeValues(idx)(i) == "0/1";
    }
    def isAnyAlt(vc : SVcfVariantLine, idx : Int, i : Int) : Boolean = {
      //isHet(vc,idx,i) || isHomAlt(vc,idx,i);
      isCalled(vc,idx,i) && (vc.genotypes.genotypeValues(idx)(i)(0) == '1' || (vc.genotypes.genotypeValues(idx)(i).length == 3 && vc.genotypes.genotypeValues(idx)(i)(2) == '1'))
    }
    def isOther(vc : SVcfVariantLine, idx : Int, i : Int) : Boolean = {
      isCalled(vc,idx,i) && (vc.genotypes.genotypeValues(idx)(i).length != 3 || vc.genotypes.genotypeValues(idx)(i).charAt(0) == '2' || vc.genotypes.genotypeValues(idx)(i).charAt(2) == '2')
    }
    def isClean(vc : SVcfVariantLine, idx : Int, i : Int) : Boolean = {
      if(! isCalled(vc,idx,i)){
        false;
      }
      if(vc.genotypes.genotypeValues(idx)(i).length != 3){
        false
      } else {
        val (a,b) = (vc.genotypes.genotypeValues(idx)(i)(0), vc.genotypes.genotypeValues(idx)(i)(2))
        (a == '0' || a == '1') && (b == '0' || b == '1')
      }
    }
    def isCleanMismatch(vc : SVcfVariantLine,idx1 : Int, idx2: Int, i : Int) : Boolean = {
      isClean(vc,idx1,i) && isClean(vc,idx2,i) && vc.genotypes.genotypeValues(idx1)(i) != vc.genotypes.genotypeValues(idx2)(i)
    }
    
    val matchCountFunctionList_BASE : Vector[(String, Array[Int], (SVcfVariantLine,SVcfVariantLine) => Boolean, (SVcfVariantLine,Int,Int,Int) => Boolean)] = Vector[(String, Array[Int], (SVcfVariantLine,SVcfVariantLine) => Boolean, (SVcfVariantLine,Int,Int,Int) => Boolean)](
        ("noCall",Array.fill[Int](matchIdx.length)(0), (vc : SVcfVariantLine, vc2 : SVcfVariantLine) => true,(vc : SVcfVariantLine, idx1: Int, idx2 :Int, i : Int) => {
              ! isCalled(vc,idx1,i) && ! isCalled(vc,idx2,i)
        }),
        ("calledA",Array.fill[Int](matchIdx.length)(0), (vc : SVcfVariantLine, vc2 : SVcfVariantLine) => true,(vc : SVcfVariantLine, idx1: Int, idx2 :Int, i : Int) => {
              isCalled(vc,idx1,i) && ! isCalled(vc,idx2,i)
        }),
        ("calledB",Array.fill[Int](matchIdx.length)(0), (vc : SVcfVariantLine, vc2 : SVcfVariantLine) => true,(vc : SVcfVariantLine, idx1: Int, idx2 :Int, i : Int) => {
              ! isCalled(vc,idx1,i) && isCalled(vc,idx2,i)
        }),
        ("called",Array.fill[Int](matchIdx.length)(0), (vc : SVcfVariantLine, vc2 : SVcfVariantLine) => true,(vc : SVcfVariantLine, idx1: Int, idx2 :Int, i : Int) => {
              isCalled(vc,idx1,i) && isCalled(vc,idx2,i)
        }),
        ("match",Array.fill[Int](matchIdx.length)(0), (vc : SVcfVariantLine, vc2 : SVcfVariantLine) => true,(vc : SVcfVariantLine, idx1: Int, idx2 :Int, i : Int) => {
              isMatch(vc,idx1,idx2,i)
        }),
        ("mismatch",Array.fill[Int](matchIdx.length)(0), (vc : SVcfVariantLine, vc2 : SVcfVariantLine) => true,(vc : SVcfVariantLine, idx1: Int, idx2 :Int, i : Int) => {
              isMisMatch(vc,idx1,idx2,i)
        }),
        ("mismatch.clean",Array.fill[Int](matchIdx.length)(0), (vc : SVcfVariantLine, vc2 : SVcfVariantLine) => true,(vc : SVcfVariantLine, idx1: Int, idx2 :Int, i : Int) => {
              isCleanMismatch(vc,idx1,idx2,i)
        }),
        ("mismatch.Ref.Alt",Array.fill[Int](matchIdx.length)(0), (vc : SVcfVariantLine, vc2 : SVcfVariantLine) => true,(vc : SVcfVariantLine, idx1: Int, idx2 :Int, i : Int) => {
              isMisMatch(vc,idx1,idx2,i) && isRef(vc,idx1,i) && isAnyAlt(vc,idx2,i);
        }),
        ("mismatch.Alt.Ref",Array.fill[Int](matchIdx.length)(0), (vc : SVcfVariantLine, vc2 : SVcfVariantLine) => true,(vc : SVcfVariantLine, idx1: Int, idx2 :Int, i : Int) => {
              isMisMatch(vc,idx1,idx2,i) && isRef(vc,idx2,i) && isAnyAlt(vc,idx1,i);
        }),
        ("mismatch.NRef.NRef",Array.fill[Int](matchIdx.length)(0), (vc : SVcfVariantLine, vc2 : SVcfVariantLine) => true,(vc : SVcfVariantLine, idx1: Int, idx2 :Int, i : Int) => {
              isMisMatch(vc,idx1,idx2,i) && (! isRef(vc,idx1,i)) && (! isRef(vc,idx2,i));
        }),
        ("mismatch.Ha.Het",Array.fill[Int](matchIdx.length)(0), (vc : SVcfVariantLine, vc2 : SVcfVariantLine) => true,(vc : SVcfVariantLine, idx1: Int, idx2 :Int, i : Int) => {
              isMisMatch(vc,idx1,idx2,i) && isHomAlt(vc,idx1,i) && isHet(vc,idx2,i);
        }),
        ("mismatch.Het.Ha",Array.fill[Int](matchIdx.length)(0), (vc : SVcfVariantLine, vc2 : SVcfVariantLine) => true,(vc : SVcfVariantLine, idx1: Int, idx2 :Int, i : Int) => {
              isMisMatch(vc,idx1,idx2,i) && isHomAlt(vc,idx2,i) && isHet(vc,idx1,i);
        }),
        ("mismatch.Ref.Het",Array.fill[Int](matchIdx.length)(0), (vc : SVcfVariantLine, vc2 : SVcfVariantLine) => true,(vc : SVcfVariantLine, idx1: Int, idx2 :Int, i : Int) => {
              isMisMatch(vc,idx1,idx2,i) && isRef(vc,idx1,i) && isHet(vc,idx2,i);
        }),
        ("mismatch.Het.Ref",Array.fill[Int](matchIdx.length)(0), (vc : SVcfVariantLine, vc2 : SVcfVariantLine) => true,(vc : SVcfVariantLine, idx1: Int, idx2 :Int, i : Int) => {
              isMisMatch(vc,idx1,idx2,i) && isRef(vc,idx2,i) && isHet(vc,idx1,i);
        }),
        ("mismatch.Ref.Ha",Array.fill[Int](matchIdx.length)(0), (vc : SVcfVariantLine, vc2 : SVcfVariantLine) => true,(vc : SVcfVariantLine, idx1: Int, idx2 :Int, i : Int) => {
              isMisMatch(vc,idx1,idx2,i) && isRef(vc,idx1,i) && isHomAlt(vc,idx2,i);
        }),
        ("mismatch.Ha.Ref",Array.fill[Int](matchIdx.length)(0), (vc : SVcfVariantLine, vc2 : SVcfVariantLine) => true,(vc : SVcfVariantLine, idx1: Int, idx2 :Int, i : Int) => {
              isMisMatch(vc,idx1,idx2,i) && isRef(vc,idx2,i) && isHomAlt(vc,idx1,i);
        }),
        ("mismatch.Other",Array.fill[Int](matchIdx.length)(0), (vc : SVcfVariantLine, vc2 : SVcfVariantLine) => true,(vc : SVcfVariantLine, idx1: Int, idx2 :Int, i : Int) => {
              isMisMatch(vc,idx1,idx2,i) && (isOther(vc,idx1,i) || isOther(vc,idx2,i))
        })
    );
    val matchCountFunctionList_BYTYPE = matchCountFunctionList_BASE.map{ case (id,arr,varFcn,fcn) => {
      (id + "_SNV",Array.fill[Int](matchIdx.length)(0),(vc : SVcfVariantLine, vc2 : SVcfVariantLine) => isSNV(vc),fcn)
    }} ++ matchCountFunctionList_BASE.map{ case (id,arr,varFcn,fcn) => {
      (id + "_INDEL",Array.fill[Int](matchIdx.length)(0), (vc : SVcfVariantLine, vc2 : SVcfVariantLine) => ! isSNV(vc),fcn)
    }}
    
    val matchCountFunctionList_BYSWAP = matchCountFunctionList_BASE.flatMap{ case (id,arr,varFcn,fcn) => {
      SNVVARIANT_BASESWAP_LIST.map{ case ((r1,a1),(r2,a2)) => {
        (id+"_SNV_SWAP."+r1+a1,Array.fill[Int](matchIdx.length)(0), (vc : SVcfVariantLine, vc2 : SVcfVariantLine) => {
          isSNV(vc) && ((vc.ref == r1 && vc.alt.head == a1) || (vc.ref == r2 && vc.alt.head == a2))
        },fcn)
      }}
    }}
    
    val matchCountFunctionList_BASE2 : Vector[(String, Array[Int], (SVcfVariantLine, SVcfVariantLine) => Boolean, (SVcfVariantLine,Int,Int,Int) => Boolean)] = matchCountFunctionList_BASE ++ matchCountFunctionList_BYTYPE ++ matchCountFunctionList_BYSWAP;
    val matchCountFunctionList_BASE2_CFILTSET = matchCountFunctionList_BASE2.map{ case (id,arr, varFcn,fcn) => {
      ("CPASSAB_"+id,Array.fill[Int](matchIdx.length)(0), (vc : SVcfVariantLine, vc2 : SVcfVariantLine) => {
        varFcn(vc,vc2) && vc.filter == "."
      },fcn)
    }} ++ matchCountFunctionList_BASE2.map{ case (id,arr, varFcn,fcn) => {
      ("CPASSA_"+id,Array.fill[Int](matchIdx.length)(0), (vc : SVcfVariantLine, vc2 : SVcfVariantLine) => {
        varFcn(vc,vc2) && vc.filter != "." && vc.filter.split(",")(0) == ".";
      },fcn)
    }} ++ matchCountFunctionList_BASE2.map{ case (id,arr, varFcn,fcn) => {
      ("CPASSB_"+id,Array.fill[Int](matchIdx.length)(0), (vc : SVcfVariantLine, vc2 : SVcfVariantLine) => {
        varFcn(vc,vc2) && vc.filter != "." && vc.filter.split(",")(1) == ".";
      },fcn)
    }} ++ matchCountFunctionList_BASE2.map{ case (id,arr, varFcn,fcn) => {
      ("CPASSN_"+id,Array.fill[Int](matchIdx.length)(0), (vc : SVcfVariantLine, vc2 : SVcfVariantLine) => {
        varFcn(vc,vc2) && vc.filter != "." && vc.filter.split(",")(1) != "." && vc.filter.split(",")(0) != "."
      },fcn)
    }}
    
    val matchCountFunctionList_3 : Vector[(String, Array[Int], (SVcfVariantLine,SVcfVariantLine) => Boolean, (SVcfVariantLine,Int,Int,Int) => Boolean)] = matchCountFunctionList_BASE2 ++ matchCountFunctionList_BASE2_CFILTSET
    
    val matchCountFunctionList_4 : Vector[(String, Array[Int], (SVcfVariantLine,SVcfVariantLine) => Boolean, (SVcfVariantLine,Int,Int,Int) => Boolean)] = matchCountFunctionList_3 ++ matchCountFunctionList_3.map{
      case (id,arr, varFcn,fcn) => {
        ("BIALLE_"+id,Array.fill[Int](matchIdx.length)(0),(vc : SVcfVariantLine, vc2 : SVcfVariantLine) => {varFcn(vc,vc2) && vc.alt.length == 1 && vc2.alt.length == 1},fcn)
      }
    }
    
    val matchCountFunctionList_FINAL : Vector[(String, Array[Int], (SVcfVariantLine, SVcfVariantLine) => Boolean, (SVcfVariantLine,Int,Int,Int) => Boolean)] =  matchCountFunctionList_4 ++ (
      bedFilter match{
        case Some(bfilt) => {matchCountFunctionList_4.map{
          case (id,arr, varFcn,fcn) => {
            ("ONBED_"+id,Array.fill[Int](matchIdx.length)(0),(vc : SVcfVariantLine, vc2 : SVcfVariantLine) => {varFcn(vc,vc2) && bfilt(vc)},fcn)
          }  
        }}
        case None => {
          Vector();
        }
      }
    )
    
    ////////////////////////////////////////////////////////////////////
    
    val mmCountFunctionList_BASE : Vector[(String, Array[Int], SVcfVariantLine => Boolean, (SVcfVariantLine,Int,Int) => Boolean)] = Vector[(String, Array[Int], SVcfVariantLine => Boolean, (SVcfVariantLine,Int,Int) => Boolean)](
        ("noCall",Array.fill[Int](matchIdx.length)(0), (vc : SVcfVariantLine) => true,(vc : SVcfVariantLine, idx: Int, i : Int) => {
              ! isCalled(vc,idx,i)
        }),
        ("called",Array.fill[Int](matchIdx.length)(0), (vc : SVcfVariantLine) => true,(vc : SVcfVariantLine, idx: Int, i : Int) => {
              isCalled(vc,idx,i)
        }),
        ("HomRef",Array.fill[Int](matchIdx.length)(0), (vc : SVcfVariantLine) => true,(vc : SVcfVariantLine, idx: Int, i : Int) => {
              isCalled(vc,idx,i) & isRef(vc,idx,i)
        }),
        ("Het",Array.fill[Int](matchIdx.length)(0), (vc : SVcfVariantLine) => true,(vc : SVcfVariantLine, idx: Int, i : Int) => {
              isCalled(vc,idx,i) & isHet(vc,idx,i)
        }),
        ("HomAlt",Array.fill[Int](matchIdx.length)(0), (vc : SVcfVariantLine) => true,(vc : SVcfVariantLine, idx: Int, i : Int) => {
              isCalled(vc,idx,i) & isHomAlt(vc,idx,i)
        }),
        ("Other",Array.fill[Int](matchIdx.length)(0), (vc : SVcfVariantLine) => true,(vc : SVcfVariantLine, idx: Int, i : Int) => {
              isCalled(vc,idx,i) & isOther(vc,idx,i)
        })
    );
    val mmCountFunctionList_BYTYPE  : Vector[(String, Array[Int], SVcfVariantLine => Boolean, (SVcfVariantLine,Int,Int) => Boolean)]  = mmCountFunctionList_BASE.map{ case (id,arr,varFcn,fcn) => {
      (id + "_SNV",Array.fill[Int](matchIdx.length)(0),(vc : SVcfVariantLine) => {
        isSNV(vc);
      },fcn)
    }} ++ mmCountFunctionList_BASE.map{ case (id,arr,varFcn,fcn) => {
      (id + "_INDEL",Array.fill[Int](matchIdx.length)(0),(vc : SVcfVariantLine) => {
        ! isSNV(vc);
      },fcn)
    }}
    
    val mmCountFunctionList_BYSWAP  : Vector[(String, Array[Int], SVcfVariantLine => Boolean, (SVcfVariantLine,Int,Int) => Boolean)] =  mmCountFunctionList_BASE.flatMap{ case (id,arr,varFcn,fcn) => {
       SNVVARIANT_BASESWAP_LIST.map{ case ((r1,a1),(r2,a2)) => {
         (id + "_SNV_SWAP."+r1+a1,Array.fill[Int](matchIdx.length)(0),(vc : SVcfVariantLine) => {
           isSNV(vc) && ((vc.ref == r1 && vc.alt.head == a1) || (vc.ref == r2 && vc.alt.head == a2))
         },fcn)
       }}
    }}
    
    
    val mmCountFunctionList_BASE2  : Vector[(String, Array[Int], SVcfVariantLine => Boolean, (SVcfVariantLine,Int,Int) => Boolean)] = mmCountFunctionList_BASE ++ mmCountFunctionList_BYTYPE ++ mmCountFunctionList_BYSWAP;
    
    val mmCountFunctionList_CPASS  : Vector[(String, Array[Int], SVcfVariantLine => Boolean, (SVcfVariantLine,Int,Int) => Boolean)] = mmCountFunctionList_BASE2.map{ case (id,arr,varFcn,fcn) => {
      ("CPASS_"+id,Array.fill[Int](matchIdx.length)(0),(vc : SVcfVariantLine) => {
        varFcn(vc) && vc.filter == "."
      },fcn)
    }}
    
    val mmCountFunctionList_3 : Vector[(String, Array[Int], SVcfVariantLine => Boolean, (SVcfVariantLine,Int,Int) => Boolean)] = mmCountFunctionList_BASE2 ++ mmCountFunctionList_CPASS
    
    val mmCountFunctionList_4 : Vector[(String, Array[Int], SVcfVariantLine => Boolean, (SVcfVariantLine,Int,Int) => Boolean)] = mmCountFunctionList_3 ++ mmCountFunctionList_3.map{
      case (id,arr, varFcn,fcn) => {
        ("BIALLE_"+id,Array.fill[Int](matchIdx.length)(0),(vc : SVcfVariantLine) => {varFcn(vc) && vc.alt.length == 1},fcn)
      }
    }
    
    val mmCountFunctionList_A  : Vector[(String, Array[Int], SVcfVariantLine => Boolean, (SVcfVariantLine,Int,Int) => Boolean)] = mmCountFunctionList_4 ++ (bedFilter match {
        case Some(bfilt) => {mmCountFunctionList_4.map{
          case (id,arr, varFcn,fcn) => {
            ("ONBED_"+id,Array.fill[Int](matchIdx.length)(0),(vc : SVcfVariantLine) => {varFcn(vc) && bfilt(vc)},fcn)
          }
        }}
      case None => {
        Vector();
      }
    })
    
    val mmCountFunctionList_B = mmCountFunctionList_A.map{ case (id,arr,varFcn,fcn) => {
      (id,Array.fill[Int](matchIdx.length)(0),varFcn,fcn)
    }}
    
    ////////////////////////////////////////////////////////////////////

    def iteratePos() : Seq[SVcfVariantLine] = {
      val (chrom,pos) = (vcIter1.head.chrom,vcIter1.head.pos);
      val v1s = extractWhile(vcIter1)( vc => { vc.pos ==  pos && vc.chrom == chrom});
      val v2s = extractWhile(vcIter2)( vc => { vc.pos ==  pos && vc.chrom == chrom});
      val alts : Vector[String] = (v1s.map{vc => vc.alt.head}.toSet ++ v2s.map{vc => vc.alt.head}).toSet.toVector.sorted;
      //reportln("      Found "+alts.length+" alt alleles at this position: ["+alts.mkString(",")+"]","debug")
      
      alts.map{ alt => {
        val v1idx = v1s.indexWhere{ vc => { vc.alt.head == alt }};
        val v2idx = v2s.indexWhere{ vc => { vc.alt.head == alt }};
        if(v1idx == -1 ){
          val vc = v2s(v2idx).getOutputLine();
          alleAt2not1ct += 1;
          //reportln("      Allele found only on iter2: "+alt,"debug")
          writeVC2(vc);
        } else if(v2idx == -1) {
          val vc = v1s(v1idx).getOutputLine();
          alleAt1not2ct += 1;
          //reportln("      Allele found only on iter1: "+alt,"debug")
          writeVC1(vc);
        } else {
          val vc1 = v1s(v1idx).getOutputLine();
          val vc2 = v2s(v2idx).getOutputLine();
          matchCt += 1;
          //reportln("      Allele found on both: "+alt,"debug")
          writeJointVC(vc1,vc2);
        }
      }}
    }
    
    def writeVC1(vc : SVcfVariantLine) : SVcfVariantLine = {
      val gt = vc.genotypes;
      val gto = SVcfGenotypeSet(vc.format, gt.genotypeValues.map{ ga => { matchIdx.map{ case (samp,idx1,idx2) => {
        ga(idx1);
      }}}.toArray});
      val vco = SVcfOutputVariantLine(
       in_chrom = vc.chrom,in_pos = vc.pos,in_id = vc.id,in_ref = vc.ref,in_alt = vc.alt,in_qual = vc.qual,in_filter = vc.filter, in_info = vc.info,
       in_format = vc.format,
       in_genotypes = gto
      )
      out1.write(vco.getVcfString+"\n");
      val idx = vco.format.indexOf(genoTag1)
        mmCountFunctionList_A.foreach{ case (id,arr,varFcn,fcn) => {
          if(varFcn(vco)){
            matchIdxIdx.foreach{ i => {
              if(fcn(vco,idx,i)) arr(i) += 1;
            }}
          }
        }}

      vco;
    }
    def writeVC2(vc : SVcfVariantLine) : SVcfVariantLine = {
      val gt = vc.genotypes;
      val fmt = vc.format.map{ f => { file2tag + f }}
      val gto = SVcfGenotypeSet(fmt, gt.genotypeValues.map{ ga => { matchIdx.map{ case (samp,idx1,idx2) => {
        ga(idx2);
      }}}.toArray});
      val vco = SVcfOutputVariantLine(
       in_chrom = vc.chrom,in_pos = vc.pos,in_id = vc.id,in_ref = vc.ref,in_alt = vc.alt,in_qual = vc.qual,in_filter = vc.filter, in_info = vc.info,
       in_format = fmt,
       in_genotypes = gto
      )
      out2.write(vco.getVcfString+"\n");
      val idx = vco.format.indexOf(fGenoTag2);
        mmCountFunctionList_B.foreach{ case (id,arr,varFcn,fcn) => {
          if(varFcn(vco)){
            matchIdxIdx.foreach{ i => {
              if(fcn(vco,idx,i)) arr(i) += 1;
            }}
          }
        }}
      vco;
    }

    def writeJointVC(vc1 : SVcfVariantLine, vc2 : SVcfVariantLine) : SVcfVariantLine = {
      val gt1 = vc1.genotypes;
      val fmt1 = vc1.format//.map{ f => { file2tag + f }}
      val gt2 = vc2.genotypes;
      val fmt2 = vc2.format.map{ f => { file2tag + f }}
      val fmt = fmt1 ++ fmt2;
      val gt = SVcfGenotypeSet(fmt, 
        gt1.genotypeValues.map{ ga => { matchIdx.map{ case (samp,idx1,idx2) => {
          ga(idx1);
        }}}.toArray} ++
        gt2.genotypeValues.map{ ga => { matchIdx.map{ case (samp,idx1,idx2) => {
          ga(idx2);
        }}}.toArray}
      );
      val filt = if(vc1.filter == "." && vc2.filter == ".") { "." } else if(vc1.filter == vc2.filter) {
        vc1.filter + "," + vc2.filter;
      } else {
        vc1.filter + "," + vc2.filter;
      }
      val vco = SVcfOutputVariantLine(
       in_chrom = vc1.chrom,in_pos = vc1.pos,in_id = vc1.id,in_ref = vc1.ref,in_alt = vc1.alt,in_qual = vc1.qual,
       in_filter = filt, in_info = vc1.info,
       in_format = fmt,
       in_genotypes = gt
      )
      outM.write(vco.getVcfString+"\n");
      
        val (idx1,idx2) = (vco.format.indexOf(genoTag1),vco.format.indexOf(fGenoTag2));
        if(idx1 != -1 && idx2 != -1){
          matchCountFunctionList_FINAL.foreach{ case (id,arr,varFcn,fcn) => {
            if(varFcn(vco,vc2)){
              matchIdxIdx.foreach{ i => {
                if(fcn(vco,idx1,idx2,i)) arr(i) += 1;
              }}
            }
          }}
        }

      vco;
    }
    
    def iterateMissingVariant(first : Boolean){
      if(first){
        val vc = vcIter1.next();
        writeVC1(vc);
        at1not2ct += 1;
      } else {
        val vc = vcIter2.next().getOutputLine();
        //vc.genotypes.fmt = vc.genotypes.fmt.map{f => {file2tag + f}}
        //out2.write(vc.getVcfString+"\n");
        writeVC2(vc);
        at2not1ct += 1;
      }
    }
  }
  
  
  



  case class SRedoDBNSFPannotation( dbnsfpfile : String, chromStyle : String, chromList : Option[List[String]], 
                                   posFieldTitle : String,chromFieldTitle : String, altFieldTitle : String,
                                   singleDbFile : Boolean,
                                   dbFileDelim : String,
                                   tagPrefix : String,
                                   dropTags : Option[List[String]],keepTags : Option[List[String]]
                                  ) extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "SRedoDBNSFPannotation"
    //if(! filesByChrom){
    //  error("Fatal error: operation with the --singleDbFile flag is not yet supported!");
    //}
    def walkerParams : Seq[(String,String)]=  Seq[(String,String)](
          ("dbnsfpfile",dbnsfpfile),
          ("chromStyle",chromStyle),
          ("chromList",fmtOptionList(chromList)),
          ("posFieldTitle",posFieldTitle),
          ("chromFieldTitle",chromFieldTitle),
          ("altFieldTitle",altFieldTitle),
          ("singleDbFile",singleDbFile.toString),
          ("dbFileDelim",dbFileDelim),
          ("tagPrefix",tagPrefix),
          ("dropTags",fmtOptionList(dropTags)),
          ("keepTags",fmtOptionList(keepTags))
        )
    var currChrom = "chr20";

    var fileCells = if(singleDbFile){
      getLinesSmartUnzip(dbnsfpfile).map(_.split(dbFileDelim))
    } else {
      getLinesSmartUnzip(dbnsfpfile + currChrom).map(_.split(dbFileDelim))
    }
    val dbHeader = fileCells.next.zipWithIndex.map{case (s,i) => if(s.head == '#') s.tail else s}.map(s => { s.trim() });
    
    reportln("DBNSFP file ("+dbnsfpfile+") header: ","debug");
    dbHeader.zipWithIndex.foreach{ case (s,i) => reportln("    "+i+"=\""+s+"\"","debug")}
    
    val keyMap = dbHeader.zipWithIndex.toMap;
    val altMap = Map[String,Int](("A" -> 0),("C" -> 1),("G" -> 2),("T" -> 3));
    
    val posIdx = keyMap(posFieldTitle);
    val alleIdx = keyMap("alt");
    
    val zeroString = Array.ofDim[String](dbHeader.size).map(_ => ".");
    
    var currPositionMap : Seq[(String,Array[String])] = Seq[(String,Array[String])]()
    
    val chromFieldIdx = keyMap(chromFieldTitle);
    val posFieldIdx = keyMap(posFieldTitle);
    val altFieldIdx = keyMap(altFieldTitle);
    
    var currIterator : BufferedIterator[(String,Int,String,Array[String])] = fileCells.map( cells => {
        val chrom = if(singleDbFile) cells(chromFieldIdx) else currChrom;
        val pos = string2int(cells(posFieldIdx));
        val alle = cells(altFieldIdx);
        (chrom,pos,alle,cells)
      }).buffered
    
    //var currCells = Array.ofDim[String](dbHeader.length) //currReader.next.split("\t");
    //var currPos = string2int(currCells(keyMap("hg19_pos(1-based)")));
    var currPos = -1;
    var lastRequestedPos = -1;
    
    def setPos(){
      if(currIterator.hasNext){
        val (chr, pos, alle, cells) = currIterator.head;
        val currPosVector = extractWhile(currIterator){ case (ch,p,a,c) => { ch == chr && p == pos } }
        
        //val (currPosIter, remainderIter) = currIterator.span{ case (p,a,c) => { p == pos }};
        //currIterator = remainderIter;
        currPositionMap = currPosVector.map{case (chr,p,a,c) => ((a,c))}
        currPos = pos;
        currChrom = chr;
      }
    }
    
    def shiftToPosition(chrom : String, pos : Int) : Boolean = {
      if(chrom == currChrom){
        if(currPos > pos){
          if(lastRequestedPos > pos){
            warning("Illegal backward reference in DBNSFP file! Is VCF sorted? ("+chrom+":"+pos+")","DBNSFP_BACKREF",100);
          }
          lastRequestedPos = pos;
          return false;
        } else if(currPos == pos){
          //do nothing!
          lastRequestedPos = pos;
          return true;
        } else {
          //currIterator = currIterator.dropWhile{ case (p,alle,cells) => p < pos }
          skipWhile(currIterator){ case (chr,p,alle,cells) => chr == chrom && p < pos }
          if((! currIterator.hasNext) || (currIterator.head._1 != chrom)){
            lastRequestedPos = pos;
            return false;
          }
          setPos();
          lastRequestedPos = pos;
          return pos == currPos && chrom == currChrom;
        }
      } else {
        reportln("Switching to chromosome: "+currChrom +" ["+ getDateAndTimeString+"]","debug");          
        if(singleDbFile){
          skipWhile(currIterator){ case (chr,p,alle,cells) => chr != chrom }
          if(! currIterator.hasNext){
            reportln("Reached file-end, returning to start of file... "+" ["+ getDateAndTimeString+"]","debug");
            currIterator = getLinesSmartUnzip(dbnsfpfile).drop(1).map( line => {
                 val cells = line.split(dbFileDelim);
                 val chrom = cells(chromFieldIdx);
                 val pos = string2int(cells(posFieldIdx));
                 val alle = cells(altFieldIdx);
                 (chrom,pos,alle,cells)
            }).buffered
            skipWhile(currIterator){ case (chr,p,alle,cells) => chr != chrom }
            if(! currIterator.hasNext){
              warning("Chromosome "+chrom+" not found!","CHROM_NOT_FOUND_WARNING",100);
            }
          }
        } else {
          currIterator = getLinesSmartUnzip(dbnsfpfile + chrom).drop(1).map( line => {
            val cells = line.split(dbFileDelim);
            val pos = string2int(cells(keyMap(posFieldTitle)));
            val alle = cells(keyMap("alt"));
            (chrom,pos,alle,cells)
          }).buffered
        }
        currPos = -1;
        currChrom = chrom;
        lastRequestedPos = -1;
        reportln("Switched to chromosome: "+currChrom +" ["+ getDateAndTimeString+"]","debug");          
        return shiftToPosition(chrom,pos);
      }
    }
    
    val tagsToWrite : Seq[String] = dbHeader.filter(t => {
      dropTags match {
        case Some(dt) => {
          ! dt.contains(t);
        }
        case None => true;
      }
    }).filter(t => {
      keepTags match {
        case Some(dt) => {
          dt.contains(t);
        }
        case None => true;
      }
    })
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = { 
      
      val newHeaderLines = List(
        new  SVcfCompoundHeaderLine("INFO",tagPrefix+"FoundAtPos", "1", "Integer", "Equal to 1 if and only if any dbNSFP line was found at the given position."),
        new  SVcfCompoundHeaderLine("INFO",tagPrefix+"Found", "1", "Integer", "Equal to the number of lines found at the given position and matching the given alt allele.")
      ) ++ tagsToWrite.toList.zipWithIndex.map{ case (title,i) => {
        new  SVcfCompoundHeaderLine("INFO",tagPrefix+title, ".", "String", "Info from column "+title+" (col "+i+") of dbNSFP file ("+dbnsfpfile+")");
      }}
      
      val newHeader = vcfHeader.copyHeader;
      newHeaderLines.foreach{ hl => {
        newHeader.addInfoLine(hl);
      }}
      
      ((vcIter.map(vc => walkVC(vc)),newHeader));
    }
    
    def walkVC(vc : SVcfVariantLine) : SVcfVariantLine = {
      var vb = vc.getOutputLine();
      
      val chrom = vc.chrom
      val pos = vc.pos
      val isInDB = shiftToPosition(chrom,pos);
      //val altAlleles = Range(0,vc.getNAlleles()-1).map((a) => vc.getAlternateAllele(a)).zipWithIndex.filter{case (alt,altIdx) => { alt.getBaseString() != "*" }}
      val altAllelesRaw = vc.alt.zipWithIndex;
      val altAlles = altAllelesRaw.filter{case (alt,altIdx) => { alt != internalUtils.VcfTool.UNKNOWN_ALT_TAG_STRING}}
      if(altAlles.length > 1){
        error("Fatal Error: wrong number of alt alleles! You must split multiallelics!")
      }
      vb.addInfo(tagPrefix+"FoundAtPos",if(isInDB) "1" else "0");
      
      if(isInDB && altAlles.length > 0 && currPositionMap.exists{ case (key,c) => key == altAlles.head._1 } ){
        val alt = altAlles.head._1
        val matches = currPositionMap.filter{ case (key,c) => key == alt }.map{ case (key,c) => c};
        
        if(matches.length == 1){
          matches.head.zip(dbHeader).filter{ case (v,title) => {
            tagsToWrite.contains(title)
          }}.foreach{ case (v,title) => {
            vb.addInfo(tagPrefix+title,cleanInfoField(v));
          }}
        } else {
          dbHeader.zipWithIndex.filter{ case (tag,idx) => {
            tagsToWrite.contains(tag);
          }}.foreach{ case (tag,idx) => {
            vb.addInfo(tagPrefix+tag,matches.map{ case varray => cleanInfoField(varray(idx)) }.filter( v => v != "." ).padTo(1,".").mkString(","));
          }}
        }
        vb.addInfo(tagPrefix+"Found",""+matches.length);
      } else {
        tagsToWrite.foreach(title => {
          vb.addInfo(tagPrefix+title,".");
        })
        vb.addInfo(tagPrefix+"Found","0");
      }
      
      return vb
    }
    
    //def cleanInfoField(s : String) : String = {
    //  var out = s;
    //  
    //  out = out.replaceAll("\\||\\.|;",",").replaceAll("/| |-|:","_").replaceAll("[\\(\\)\\[\\]]","").replaceAll("[_]+","_").replaceAll("[,]+",",").replaceAll("_$|,$","");
    //  
    //  return out;
    //}
    def cleanInfoField(s : String) : String = {
      var out = s;
      
      //out = out.replaceAll("[;|.]+",",").replaceAll("[/ ;-]","_").replaceAll("[\\(\\)\\[\\]]","").replaceAll("[_,]+$|^[_,]+","");
//      out = out.replaceAll("[/ :-]+","_").replaceAll("[\\(\\)\\[\\]]","").split("[;|.]+").map(k => k.replaceAll("[_]+$|^[_]+","")).filter(k => k != "").padTo(1,".").mkString(",");

      out = out.replaceAll("[/ :-]+","_").replaceAll("[\\(\\)\\[\\]]","").split("[;|,]+").map(k => k.replaceAll("[_]+$|^[_]+","")).filter(k => k != "").padTo(1,".").mkString(",");
      return out;
    }

    
  }
  

  class CmdFixVcfInfoWhitespace extends CommandLineRunUtil {
     override def priority = 20;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "fixVcfInfoWhitespace", 
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
                    new FinalArgument[String](
                                         name = "infile",
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
         FixVcfInfoWhitespace(
         ).walkVCFFile(
             infile = parser.get[String]("infile"),
             outfile = parser.get[String]("outfile"),
             chromList = parser.get[Option[List[String]]]("chromList"),
             numLinesRead = None
         )
       }
     }
    
  }

  case class FixVcfInfoWhitespace() extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "FixVcfInfoWhitespace"
        def walkerParams : Seq[(String,String)]=  Seq[(String,String)](
          
        )
    def walkVCF(vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      var errCt = 0;
      val outHeader = vcfHeader.copyHeader;
      outHeader.addWalk(this);
      
      (addIteratorCloseAction( iter = vcIter.map{v => {
        val vc = v.getOutputLine()
        if(vc.in_info.exists{ case (tag, value) => {
            value match {
              case Some(valString) => {
                valString.contains(' ')
              }
              case None => {
                false
              }
            }
          }}
        ){
          vc.in_info = vc.in_info.map{ case (tag,value) => {
            value match {
              case Some(valString) => {
                (tag,Some(valString.replaceAll(" ","_")))
              }
              case None => {
                (tag,None)
              }
            }
            
          }}
          errCt = errCt + 1;
        }
        vc;
      }}, closeAction = (() => {
        val warningType = if(errCt == 0) "NOTE: " else "WARNING: ";
        reportln(warningType+"Found "+errCt +" lines with whitespace in the INFO field!","debug")
      })),outHeader)
      
    }

    
  }
  
  class CmdAddTxBed extends CommandLineRunUtil {
     override def priority = 20;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "addTxBed", 
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
                    new BinaryOptionArgument[Int](
                                         name = "bufferDist", 
                                         arg = List("--bufferDist"), 
                                         valueName = "k",  
                                         argDesc =  "Buffer size. If this optional parameter is used, then elements within k base-pairs from a marked span will be tagged."
                                        ) ::
                    new FinalArgument[String](
                                         name = "infile",
                                         valueName = "variants.vcf",
                                         argDesc = "input VCF file. Can be gzipped or in plaintext." // description
                                        ) ::
                    new FinalArgument[List[String]](
                                         name = "bedfiles",
                                         valueName = "tagID:filedesc:bedfile.bed,tagID2:filedesc2:bedfile2.bed,...",
                                         argDesc = "A comma-delimited list of tags strings, descriptions, and bed files to add to the VCF." // description
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
         AddTxBed(
             bt = parser.get[List[String]]("bedfiles"),
             chromList = parser.get[Option[List[String]]]("chromList"),
             bufferDistOpt = parser.get[Option[Int]]("bufferDist")
         ).walkVCFFile(
             infile = parser.get[String]("infile"),
             outfile = parser.get[String]("outfile"),
             chromList = parser.get[Option[List[String]]]("chromList")
         )
       }
     }
    
  }

  case class AddMafSummary(ctrlAlleFreqKeys : Seq[String] = Seq("1KG_AF","ESP_EA_AF","ExAC_ALL","SWH_AF_GRP_CTRL"),
                           maxAfTag : String = VCFAnnoCodes().assess_ctrlAFMAX) extends SVcfWalker {
    def walkerName : String = "addMafSummarySimple"
    def walkerParams : Seq[(String,String)]=  Seq[(String,String)](
          ("ctrlAlleFreqKeys","\""+ctrlAlleFreqKeys.mkString(",")+"\""),
          ("maxAfTag",maxAfTag)
    )
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      
      val newHeaderline = convertInfoLineFormat(new VCFInfoHeaderLine(maxAfTag, 1, VCFHeaderLineType.Float,    "The highest alt allele frequency found across the control datasets ("+ctrlAlleFreqKeys.mkString(",")+")"))
      var problemList = Set[String]();
      val newHeader = vcfHeader.copyHeader//  internalUtils.VcfTool.addHeaderLines(vcfHeader,newHeaderLines);
      newHeader.addInfoLine(newHeaderline);
      newHeader.addWalk(this);
      
      val overwriteInfos : Set[String] = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( newHeader.addedInfos );
      if( overwriteInfos.nonEmpty ){
        notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      }
      
      (vcMap(vcIter){v => {
        
        var vb = v.getOutputLine();
        vb.dropInfo(overwriteInfos);
        
        val refAlle = v.ref
        val altAlleles = v.alt.zipWithIndex.filter{ case (alt,altIdx) => alt != VcfTool.UNKNOWN_ALT_TAG_STRING };

        if(altAlleles.length > 1){
          error("FATAL ERROR: Cannot deal with multiallelic variants! Split variants to single-allelic!");
        }
        
        val maxAF : Double = ctrlAlleFreqKeys.map{key => v.info.getOrElse(key,None).getOrElse("0")}.map{s => if(s == ".") 0.toDouble else string2double(s)}.max;
        vb.addInfo(maxAfTag , maxAF.toString);
        vb;
      }},newHeader);
      
    }
  }
  case class AddComplexMafSummary(ctrlAlleFreqKeys : Seq[String] = Seq("1KG_AF","ESP_EA_AF","ExAC_ALL","SWH_AF_GRP_CTRL"),
                           maxAfTag : String = VCFAnnoCodes().assess_ctrlAFMAX,
                           splitIdxTag : Option[String] = Some(VCFAnnoCodes().splitIdx_TAG),
                           numSplitTag : Option[String] = Some(VCFAnnoCodes().numSplit_TAG)) extends SVcfWalker {
    def walkerName : String = "addMafSummaryComplex"
    def walkerParams : Seq[(String,String)]=  Seq[(String,String)](
          ("ctrlAlleFreqKeys","\""+ctrlAlleFreqKeys.mkString(",")+"\""),
          ("maxAfTag",maxAfTag)
    )
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      
      val newHeaderline = convertInfoLineFormat(new VCFInfoHeaderLine(maxAfTag, 1, VCFHeaderLineType.Float,    "The highest alt allele frequency found across the control datasets ("+ctrlAlleFreqKeys.mkString(",")+")"))
      var problemList = Set[String]();
      val newHeader = vcfHeader.copyHeader//  internalUtils.VcfTool.addHeaderLines(vcfHeader,newHeaderLines);
      newHeader.addInfoLine(newHeaderline);
      newHeader.addWalk(this);
      
      (vcIter.map{v => {
        var vb = v.getOutputLine();
        val refAlle = v.ref
        val altAlleles = v.alt.zipWithIndex.filter{ case (alt,altIdx) => alt != VcfTool.UNKNOWN_ALT_TAG_STRING };

        if(altAlleles.length > 1){
          error("FATAL ERROR: Cannot deal with multiallelic variants! Split variants to single-allelic!");
        }
          //val splitIdx : Int = string2int(v.info.getOrElse(splitIdxTag,None).getOrElse("0"));
          //val numSplit : Int = string2int(v.info.getOrElse(numSplitTag,None).getOrElse("1"));
          val splitIdx : Int = splitIdxTag.map{ ii => v.info.getOrElse(ii,None).map{ii => string2int(ii)}.getOrElse(0)}.getOrElse(0);
          val numSplit : Int = numSplitTag.map{ ii => v.info.getOrElse(ii,None).map{ii => string2int(ii)}.getOrElse(1)}.getOrElse(1);
          
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
          val maxAF = ctrlAlleFreqs.foldLeft(0.toDouble){case (maxSF,af) => {
            math.max(maxSF,af);
          }}

        vb.addInfo(maxAfTag , maxAF.toString);

      
        vb
      }},newHeader)
   }
  }

  
  case class AddTxBed(bt : List[String], chromList : Option[List[String]], bufferDistOpt : Option[Int]) extends SVcfWalker {
    def walkerName : String = "AddTxBed"
        def walkerParams : Seq[(String,String)]=  Seq[(String,String)](
          ("bt","\""+bt.mkString(",")+"\""),
          ("chromList",fmtOptionList(chromList)),
          ("bufferDistOpt",bufferDistOpt.getOrElse(0).toString)
        )
    val bufferDist = bufferDistOpt match {
      case Some(k) => k;
      case None => 0;
    }
    
    val chromFunc : (String => Boolean) = chromList match {
      case Some(cl) => {
        val chromSet = cl.toSet;
        ((chr : String) => chromSet.contains(chr))
      }
      case None =>{
        ((chr : String) => true)
      }
    }
    
    val bedTags : Seq[(String,String,internalUtils.commonSeqUtils.GenomicInterval => Boolean)] = 
          bt.map{b => {
            val pair : Array[String] = b.split(":");
            if(pair.length != 3) error("Each comma-delimited element of parameter addBedTags must have exactly 3 colon-delimited elements (tag:desc:filename.bed).")
            val (t,desc,f) : (String,String,String) = (pair(0),pair(1),pair(2));
            val arr : internalUtils.genomicAnnoUtils.GenomicArrayOfSets[String] = internalUtils.genomicAnnoUtils.GenomicArrayOfSets[String](false);
            reportln("   Beginning bed file read: "+f+" ("+getDateAndTimeString+")","debug");
            val lines = internalUtils.stdUtils.wrapIteratorWithAdvancedProgressReporter[String](getLinesSmartUnzip(f),
                           internalUtils.stdUtils.AdvancedIteratorProgressReporter_ThreeLevelAuto[String](
                                elementTitle = "lines", lineSec = 60,
                                reportFunction  = ((vc : String, i : Int) => " " + vc.split("\t").head +" "+ internalUtils.stdUtils.MemoryUtil.memInfo )
                           )
                         )
            
            lines.map{line => line.split("\t")}.withFilter{cells => { chromFunc(cells(0)) }}.foreach(cells => {
              val (chrom,start,end) = (cells(0),string2int(cells(1)),string2int(cells(2)))
              arr.addSpan(internalUtils.commonSeqUtils.GenomicInterval(chrom, '.', math.max(start-bufferDist,0),end+bufferDist), "CE");
            })
            arr.finalizeStepVectors;
            reportln("   Finished bed file read: "+f+" ("+getDateAndTimeString+")","debug");
            val isOnBedFunc : (internalUtils.commonSeqUtils.GenomicInterval => Boolean) = {
              (iv : internalUtils.commonSeqUtils.GenomicInterval) => {
                //! arr.findIntersectingSteps(iv).foldLeft(Set[String]()){case (soFar,(iv,currSet)) => {
                //  soFar ++ currSet;
                //}}.isEmpty
                arr.findIntersectingSteps(iv).exists{ case (iv,currSet) => ! currSet.isEmpty }
              }
            }
            (t,desc,isOnBedFunc)
    }}
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      
      val newHeaderLines = bedTags.map{ case (tagString,descString,bedFunction) => {
          //new VCFInfoHeaderLine(tagString, 1, VCFHeaderLineType.Integer, descString)
        new  SVcfCompoundHeaderLine(in_tag = "INFO", ID = tagString, Number = "1", Type = "Integer", desc = descString)
      }}
      
      val newHeader = vcfHeader.copyHeader//  internalUtils.VcfTool.addHeaderLines(vcfHeader,newHeaderLines);
      newHeaderLines.foreach{ hl => {
        newHeader.addInfoLine(hl);
      }}
      newHeader.addWalk(this);
      
      (vcIter.map{v => {
        var vb = v.getOutputLine();
        val variantIV = internalUtils.commonSeqUtils.GenomicInterval(v.chrom,'.', start = v.pos - 1, end = v.pos + math.max(1,v.ref.length));
        bedTags.foreach{ case (tagString,desc,bedFunction) => {
          //vb = vb.attribute(tagString, if(bedFunction(variantIV)) "1" else "0");
          vb.addInfo(tagString,if(bedFunction(variantIV)) "1" else "0");
        }}
        vb
      }},newHeader)
   }
    
  }
  
  
  case class AddTxBedFile(bedFile : String, tag : String, bufferDist : Int, desc : String, chromList : Option[List[String]], style : String = "+") extends SVcfWalker {
    def walkerName : String = "AddTxBed."+tag;
    
    def walkerParams : Seq[(String,String)]=  Seq[(String,String)](
          ("bedFile","\""+bedFile+"\""),
          ("chromList",fmtOptionList(chromList)),
          ("bufferDist",bufferDist.toString),
          ("tag",tag),
          ("style",style),
          ("desc",desc)
     )
    
    val chromFunc : (String => Boolean) = chromList match {
      case Some(cl) => {
        val chromSet = cl.toSet;
        ((chr : String) => chromSet.contains(chr))
      }
      case None =>{
        ((chr : String) => true)
      }
    }
    val arr : internalUtils.genomicAnnoUtils.GenomicArrayOfSets[String] = internalUtils.genomicAnnoUtils.GenomicArrayOfSets[String](false);
    reportln("   Beginning bed file read: "+bedFile+" ("+getDateAndTimeString+")","debug");
    val reader = getLinesSmartUnzip(bedFile).buffered
    skipWhile(reader)(_.startsWith("#"));
    val lines : BufferedIterator[String] = internalUtils.stdUtils.wrapIteratorWithAdvancedProgressReporter[String](reader,
                   internalUtils.stdUtils.AdvancedIteratorProgressReporter_ThreeLevelAuto[String](
                        elementTitle = "lines", lineSec = 60,
                        reportFunction  = ((vc : String, i : Int) => " " + vc.split("\t").head +" "+ internalUtils.stdUtils.MemoryUtil.memInfo )
                   )
                 ).buffered;
    val isGtf = bedFile.toUpperCase().endsWith(".GTF") || bedFile.toUpperCase().endsWith(".GTF.GZ") || bedFile.toUpperCase().endsWith(".GTF.ZIP");
    val isComplexBed = (! isGtf) && (lines.head.split("\t").length >= 12);
      
    val cellIterator : Iterator[Array[String]] = lines.map{line => line.split("\t")}.withFilter{cells => { chromFunc(cells(0)) }}
    val ivIterator : Iterator[(internalUtils.commonSeqUtils.GenomicInterval,String)] = if(isGtf){
      cellIterator.map(cells => {
        val (chrom,start,end) = (cells(0),string2int(cells(3))-1,string2int(cells(4)))
        
        (internalUtils.commonSeqUtils.GenomicInterval(chrom, '.', math.max(start-bufferDist,0),end+bufferDist),"CE")
      })
    } else if(isComplexBed){
      cellIterator.map{cells => new internalUtils.GtfTool.InputBedLineCells(cells)}.flatMap(b => {
        b.getIVs.map{ iv => (iv,b.name.get) }
      })
    } else {
      cellIterator.map(cells => {
        val (chrom,start,end) = (cells(0),string2int(cells(1)),string2int(cells(2)))
        val n = if(cells.isDefinedAt(3)) cells(3) else "CE"
        (internalUtils.commonSeqUtils.GenomicInterval(chrom, '.', math.max(start-bufferDist,0),end+bufferDist),n)
      })
    }
    ivIterator.foreach{ case (iv,n) =>{
      arr.addSpan(iv, n);
    }}
    arr.finalizeStepVectors;
    reportln("   Finished bed file read: "+bedFile+" ("+getDateAndTimeString+")","debug");
    val bedFunc : (internalUtils.commonSeqUtils.GenomicInterval => String) = if(style == "+"){
              (iv : internalUtils.commonSeqUtils.GenomicInterval) => {
                //! arr.findIntersectingSteps(iv).foldLeft(Set[String]()){case (soFar,(iv,currSet)) => {
                //  soFar ++ currSet;
                //}}.isEmpty
                if(arr.findIntersectingSteps(iv).exists{ case (iv,currSet) => ! currSet.isEmpty }) "1" else "0"
              }
    } else if(style == "-"){
              (iv : internalUtils.commonSeqUtils.GenomicInterval) => {
                if(arr.findIntersectingSteps(iv).exists{ case (iv,currSet) => ! currSet.isEmpty }) "0" else "1"
              }
    } else {
              (iv : internalUtils.commonSeqUtils.GenomicInterval) => {
                arr.findIntersectingSteps(iv).flatMap{ case (iv,currSet) => currSet }.toVector.distinct.sorted.padTo(1,".").mkString(",")
              }
    }

    def walkVCF(vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      val newHeader = vcfHeader.copyHeader
      if(style == "s"){
        newHeader.addInfoLine(new  SVcfCompoundHeaderLine(in_tag = "INFO", ID = tag, Number = ".", Type = "String", desc = desc));
      } else {
        newHeader.addInfoLine(new  SVcfCompoundHeaderLine(in_tag = "INFO", ID = tag, Number = "1", Type = "Integer", desc = desc));
      }
      newHeader.addWalk(this);
      
      (vcMap(vcIter){v => {
        var vb = v.getLazyOutputLine()
        val out = bedFunc(vb.variantIV);
        //if(out == "1"){
          
        //}
        vb.addInfo(tag,bedFunc(vb.variantIV));
        vb
      }},newHeader)
   }
    
  }
  
  
  case class AddComplexBed(bt : List[String], chromList : Option[List[String]], bufferDistOpt : Option[Int]) extends SVcfWalker {
    def walkerName : String = "AddComplexBed"
        def walkerParams : Seq[(String,String)]=  Seq[(String,String)](
          ("bt","\""+bt.mkString(",")+"\""),
          ("chromList",fmtOptionList(chromList)),
          ("bufferDistOpt",bufferDistOpt.getOrElse(0).toString)
        )
    
    
    val bufferDist = bufferDistOpt match {
      case Some(k) => k;
      case None => 0;
    }
    
    val chromFunc : (String => Boolean) = chromList match {
      case Some(cl) => {
        val chromSet = cl.toSet;
        ((chr : String) => chromSet.contains(chr))
      }
      case None =>{
        ((chr : String) => true)
      }
    }
    //tag, desc, booleanFunction, :
    val bedTags : Seq[(String,String,internalUtils.commonSeqUtils.GenomicInterval => Boolean)] = 
          bt.map{b => {
            val pair : Array[String] = b.split(":");
            if(pair.length != 3) error("Each comma-delimited element of parameter addBedTags must have exactly 3 colon-delimited elements (tag:desc:filename.bed).")
            val (t,desc,f) : (String,String,String) = (pair(0),pair(1),pair(2));
            val arr : internalUtils.genomicAnnoUtils.GenomicArrayOfSets[Int] = internalUtils.genomicAnnoUtils.GenomicArrayOfSets[Int](false);
            reportln("   Beginning bed file read: "+f+" ("+getDateAndTimeString+")","debug");
            val lines = getLinesSmartUnzip(f).buffered;
            val headerLine = if(lines.head.startsWith("#")) Some(lines.head.split("\t")) else None;
            var bedLineCt = 0;
            var bedLineInfo = scala.collection.mutable.ListBuffer[(Option[String],Option[Int],Seq[String])]();
            lines.withFilter(! _.startsWith("#")).map{line => new internalUtils.GtfTool.InputBedLine(line)}.withFilter{b => { chromFunc(b.chrom) }}.foreach(b => {
              bedLineInfo += ((b.name,b.score,b.anno));
              b.getIVs.foreach{ iv => {
                arr.addSpan(iv, bedLineCt);
              }}
              bedLineCt = bedLineCt + 1;
            })
            arr.finalizeStepVectors;
            reportln("   Finished bed file read: "+f+" ("+getDateAndTimeString+")","debug");
            val bedOverlaps : (internalUtils.commonSeqUtils.GenomicInterval => Set[Int]) = {
              (iv : internalUtils.commonSeqUtils.GenomicInterval) => {
                arr.findIntersectingSteps(iv).foldLeft(Set[Int]()){ case (soFar,(iv,currSet)) => soFar ++ currSet }
              }
            }
            
            val isOnBedFunc : (internalUtils.commonSeqUtils.GenomicInterval => Boolean) = {
              (iv : internalUtils.commonSeqUtils.GenomicInterval) => {
                ! bedOverlaps(iv).isEmpty
              }
            }
            (t,desc,isOnBedFunc)
    }}
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      
      val newHeaderLines = bedTags.map{ case (tagString,descString,bedFunction) => {
          //new VCFInfoHeaderLine(tagString, 1, VCFHeaderLineType.Integer, descString)
        new SVcfCompoundHeaderLine(in_tag = "INFO", ID = tagString, Number = "1", Type = "Integer", desc = descString)
      }}
      
      val newHeader = vcfHeader.copyHeader//  internalUtils.VcfTool.addHeaderLines(vcfHeader,newHeaderLines);
      newHeaderLines.foreach{ hl => {
        newHeader.addInfoLine(hl);
      }}
      
      (vcIter.map{v => {
        var vb = v.getOutputLine();
        val variantIV = internalUtils.commonSeqUtils.GenomicInterval(v.chrom,'.', start = v.pos - 1, end = v.pos + math.max(1,v.ref.length));
        bedTags.foreach{ case (tagString,desc,bedFunction) => {
          //vb = vb.attribute(tagString, if(bedFunction(variantIV)) "1" else "0");
          vb.addInfo(tagString,if(bedFunction(variantIV)) "1" else "0");
        }}
        vb
      }},newHeader)
   }
    
  }

  

  
  class VcfParserTest extends CommandLineRunUtil {
     override def priority = 10;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "VcfParserTest", 
          quickSynopsis = "", 
          synopsis = "", 
          description = "This utility adds an array of new VCF tags with information about the transcriptional changes caused by each variant. "+BETA_WARNING,
          argList = 
                    new BinaryOptionArgument[List[String]](
                                         name = "chromList", 
                                         arg = List("--chromList"), 
                                         valueName = "chr1,chr2,...",  
                                         argDesc =  "List of chromosomes. If supplied, then all analysis will be restricted to these chromosomes. All other chromosomes wil be ignored."
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "infileListInfix", 
                                         arg = List("--infileListInfix"), 
                                         valueName = "infileList.txt",  
                                         argDesc =  ""+
                                                    ""
                                        ) ::
                    new UnaryArgument( name = "infileList",
                                         arg = List("--infileList"), // name of value
                                         argDesc = "Use this option if you want to provide input file(s) containing a list of input files rather than a single input file"+
                                                   "" // description
                                       ) ::
                    new UnaryArgument( name = "processLine",
                                         arg = List("--processLine"), // name of value
                                         argDesc = ""+
                                                   "" // description
                                       ) ::
                    new UnaryArgument( name = "groupLines",
                                         arg = List("--groupLines"), // name of value
                                         argDesc = ""+
                                                   "" // description
                                       ) ::
                    new BinaryOptionArgument[Int](
                                         name = "numLinesRead", 
                                         arg = List("--numLinesRead"), 
                                         valueName = "N",  
                                         argDesc =  "If this parameter is set, then the utility will stop after reading in N variant lines."
                                        ) ::
                    new FinalArgument[String](
                                         name = "infile",
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
         val infile = parser.get[String]("infile");
         val outfile = parser.get[String]("outfile");
         val chromList = parser.get[Option[List[String]]]("chromList")
         val numLinesRead = parser.get[Option[Int]]("numLinesRead")
         val walker = PassThroughSVcfWalker("testWalker",processLine=parser.get[Boolean]("processLine"),
                                                         groupLines=parser.get[Boolean]("groupLines"))
         val infileList = parser.get[Boolean]("infileList")
         
         parser.get[Option[String]]("infileListInfix") match {
            case Some(ili) => {
              
              val (infilePrefix,infileSuffix) = (infile.split("\\|").head,infile.split("\\|")(1));
              val infiles = getLinesSmartUnzip(ili).map{ infix => infilePrefix+infix+infileSuffix }.mkString(",")
              walker.walkVCFFiles(infiles,outfile, chromList, numLinesRead = numLinesRead, inputFileList = false, dropGenotypes = false);
            }
            case None => {
              walker.walkVCFFiles(infile,outfile, chromList, numLinesRead = numLinesRead, inputFileList = infileList, dropGenotypes = false);
            }
         }
         
       }
     }
  }
  
  case class PassThroughSVcfWalker(name : String = "nullWalker", processLine : Boolean = false, groupLines : Boolean = false) extends SVcfWalker {
    def walkerName : String = name;
        def walkerParams : Seq[(String,String)]=  Seq[(String,String)](
          ("name",name),
          ("processLine",processLine.toString),
          ("groupLines",groupLines.toString)
        )
    def walkVCF(vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader;
      outHeader.addWalk(this);
      if(processLine){
        (vcMap(vcIter){vc => {
          vc.getOutputLine();
        }},vcfHeader)
      } else if(groupLines){
        (vcGroupedFlatMap(groupBySpan(vcIter.buffered)(vc => vc.pos))( vseq => vseq.map{vc => vc.getOutputLine()} ),vcfHeader)
      } else {
        (vcMap(vcIter){vc => {
          vc;
        }},outHeader)
      }
    }
  }
  
  case class DebugSVcfWalker(name : String = "debuggingWalker", verbosity : Int = -1) extends SVcfWalker {
    def walkerName : String = name;
    def walkerParams : Seq[(String,String)] = Seq[(String,String)](("verbosity",verbosity.toString))
    def walkVCF(vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader;
      outHeader.addWalk(this);
      (vcMap(vcIter){vc => {
        notice("  "+vc.getSimpleVcfString(),"VCLINE",verbosity);
        vc;
      }},outHeader)
    }
  }
   
  
  case class SFilterNonVariantWalker(dropEqualRefAlt : Boolean = true) extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "SFilterNonVariantWalker"
    def walkerParams : Seq[(String,String)] = Seq[(String,String)]()

    def walkVCF(vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader;
      outHeader.addWalk(this);
      
      (vcIter.filter{vc => {
        val filt = vc.alt.filter(x => x != "." && x != internalUtils.VcfTool.UNKNOWN_ALT_TAG_STRING).length > 0
        if(!filt){
          notice("Filtering variant with no alt alleles:\n    "+vc.getSimpleVcfString(),"DROPPED_NOALT_VARIANT",10);
          false
        } else if(dropEqualRefAlt && vc.alt.head == vc.ref) {
          notice("Filtering variant with equal ref and alt allele:\n    "+vc.getSimpleVcfString(),"DROPPED_REFeqALT_VARIANT",10);
          false
        } else {
          true
        }
      }},outHeader)
    }
  }
  

  
  case class SFilterChromWalker(chromList : Option[List[String]]) extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "SFilterChromWalker"
    def walkerParams : Seq[(String,String)] = Seq[(String,String)](("chromList",fmtOptionList(chromList)))
    val chromSet = chromList match {
      case Some(cl) => {
        Some(cl.toSet);
      }
      case None => {
        None;
      }
    }
    
    val walkerFunc = chromSet match {
      case Some(cs) => {
        (vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader) => {
          val outHeader = vcfHeader.copyHeader;
          outHeader.addWalk(this);
          (vcIter.filter(p => cs.contains(p.chrom)),outHeader);
        }
      }
      case None => {
        (vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader) => (vcIter,vcfHeader);
      }
    }
    def walkVCF(vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = walkerFunc(vcIter,vcfHeader);
  }
  
  class GenerateTxAnnotation extends CommandLineRunUtil {
     override def priority = 10;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "GenerateTranscriptAnnotation", 
          quickSynopsis = "", 
          synopsis = "", 
          description = "This utility ... "+BETA_WARNING,
          argList = 
                    new BinaryOptionArgument[List[String]](
                                         name = "chromList", 
                                         arg = List("--chromList"), 
                                         valueName = "chr1,chr2,...",  
                                         argDesc =  "List of chromosomes. If supplied, then all analysis will be restricted to these chromosomes. All other chromosomes wil be ignored."
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "debugFile", 
                                         arg = List("--debugFile"), 
                                         valueName = "debugfile.txt.gz",  
                                         argDesc =  "..."
                                        ) ::
                    new UnaryArgument( name = "cdsRegionContainsStop",
                                         arg = List("--cdsRegionContainsStop"), // name of value
                                         argDesc = "Use this flag if the input GTF annotation file includes the STOP codon in the CDS region. Depending on the source of the annotation file, some GTF files include the STOP codon, some omit it. The UCSC knowngenes annotation file does NOT include CDS regions."+
                                                   "" // description
                                       ) ::
                                       
                    new BinaryOptionArgument[List[String]](
                                         name = "bioTypes", 
                                         arg = List("--bioTypes"), 
                                         valueName = "output only these biotypes",  
                                         argDesc =  "..."
                                        ) ::
                                        
                    new FinalArgument[String](
                                         name = "genomeFA",
                                         valueName = "genome.fa.gz",
                                         argDesc = "input fasta file. Can be gzipped or in plaintext." // description
                                        ) ::
                    new FinalArgument[String](
                                         name = "gtffile",
                                         valueName = "annotation.gtf.gz",
                                         argDesc = "input gtf gene annotation file. Can be gzipped or in plaintext." // description
                                        ) ::
                    new FinalArgument[String](
                                         name = "outfile",
                                         valueName = "outfile.txt.gz",
                                         argDesc = "The output file. Can be gzipped or in plaintext."// description
                                        ) ::
                    internalUtils.commandLineUI.CLUI_UNIVERSAL_ARGS );

     def run(args : Array[String]) {
       val out = parser.parseArguments(args.toList.tail);
       if(out){ 
         
         loadAndWriteTranscriptAnnotation(genomeFA = parser.get[String]("genomeFA"),
                                          gtffile = parser.get[String]("gtffile"),
                                          outputSavedTxFile = parser.get[String]("outfile"),
                                          debugFile = parser.get[Option[String]]("debugFile"),
                                          addStopCodon = ! parser.get[Boolean]("cdsRegionContainsStop"),
                                          chromList = parser.get[Option[List[String]]]("chromList")
                                          );
       }
     }
  }

  def loadAndWriteTranscriptAnnotation(genomeFA : String, gtffile : String,  outputSavedTxFile : String, debugFile : Option[String], addStopCodon : Boolean, chromList : Option[List[String]]) {
      val chromSet = chromList.map( _.toSet )
      val TXSeq : scala.collection.mutable.Map[String,TXUtil] = buildTXUtilsFromAnnotation(gtffile,genomeFA,addStopCodon=addStopCodon,chromSet=chromSet);
      val txWriter = openWriterSmart(outputSavedTxFile,false);
      TXSeq.foreach{ case (txID,tx) => {
            txWriter.write(tx.saveToString()+"\n");
      }}
      txWriter.close();
      
      debugFile match {
        case Some(txFile) => {
          val txWriter2 = openWriterSmart(txFile + ".1.txt.gz",false);
          txWriter2.write("txid\tensid\tisValidCoding\tissues\tchrom\tstart\tend\n");
          TXSeq.foreach{ case (txID,tx) => {
            txWriter2.write(txID+"\t"+tx.geneID+"\t"+tx.isValidFullLenTX+"\t"+tx.getIssueList.padTo(1,".").mkString(",")+"\t"+tx.chrom+"\t"+tx.gStart+"\t"+tx.gEnd+"\n");
          }}
          txWriter2.close();
          
          val txWriter3 = openWriterSmart(txFile + ".2.txt.gz",false);
          //txWriter3.write("txid\tensid\tisValidCoding\tissues\tchrom\tstart\tend\n");
          TXSeq.take(100).foreach{ case (txID,tx) => {
              txWriter3.write(tx.toStringVerbose()+"\n");
          }}
          txWriter3.close();
        }
        case None => {
          //do nothing!
        }
      }
  }
  
  case class AddTxAnnoSVcfWalker(gtffile : Option[String], 
                genomeFA : Option[String], 
                //outfile : String, 
                //summaryFile : Option[String],
                summaryWriter : Option[WriterUtil],
                txInfoFile : Option[String],
                addStopCodon : Boolean,
                inputSavedTxFile : Option[String],
                outputSavedTxFile : Option[String],
                geneVariantsOnly : Boolean,
                chromList : Option[List[String]],
                txToGeneFile : Option[String],
                bufferSize : Int = 32, 
                addBedTags : Option[List[String]],
                vcfCodes : VCFAnnoCodes = VCFAnnoCodes(),
                geneList : Option[List[String]] = None,
                txTypes : Option[List[String]] = None
                ) extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "AddTxAnnoSVcfWalker"
    def walkerParams : Seq[(String,String)] = Seq[(String,String)](("gtffile",gtffile.getOrElse("None")),
                                                                   ("genomeFA",genomeFA.getOrElse("None")),
                                                                   ("summaryWriter",if(summaryWriter.isDefined)"Yes" else "No"),
                                                                   ("txInfoFile",txInfoFile.getOrElse("None")),
                                                                   ("addStopCodon",addStopCodon.toString),
                                                                   ("inputSavedTxFile",inputSavedTxFile.getOrElse("None")),
                                                                   ("outputSavedTxFile",outputSavedTxFile.getOrElse("None")),
                                                                   ("geneVariantsOnly",geneVariantsOnly.toString),
                                                                   ("chromList",fmtOptionList(chromList)),
                                                                   ("txToGeneFile",txToGeneFile.getOrElse("None")),
                                                                   ("bufferSize",bufferSize.toString),
                                                                   ("addBedTags",fmtOptionList(addBedTags))
    )
      reportln("Starting AddTxAnnoSVcfWalker... "+getDateAndTimeString,"debug");
       
      val chromSet = chromList match {
        case Some(lst) => Some(lst.toSet);
        case None => None;
      }
      
      val keepChromFunc = chromSet match {
        case Some(cs) => {
          ((x : String) => cs.contains(x));
        }
        case None => ((x : String) => true);
      }
      
      val txTypeSet : Option[Set[String]] = txTypes.map{ tt => tt.toSet }
      val keepTxTypeFunc : (TXUtil) => Boolean = txTypeSet.map{ tt => {
        ((txu : TXUtil) => { txu.txType.map{ ty => tt.contains(ty) }.getOrElse(false)  })
      }}.getOrElse( ((txu : TXUtil) => { true  }) )
      
      val bedTags : Seq[(String,String,internalUtils.commonSeqUtils.GenomicInterval => Boolean)] = addBedTags match {
        case Some(bt) => {
          reportln("Reading BED files to add BED tags... ["+getDateAndTimeString+"]","debug");
          bt.map{b => {
            val pair : Array[String] = b.split(":");
            if(pair.length != 3) error("Each comma-delimited element of parameter addBedTags must have exactly 3 colon-delimited elements (tag:desc:filename.bed).")
            
            val (t,desc,f) : (String,String,String) = (pair(0),pair(1),pair(2));
            reportln("Reading "+t+" BED file: \""+f+"\" ["+getDateAndTimeString+"]","debug");
            report("   [","debug");
            val arr : internalUtils.genomicAnnoUtils.GenomicArrayOfSets[String] = internalUtils.genomicAnnoUtils.GenomicArrayOfSets[String](false);
            val lines = getLinesSmartUnzip(f);
            lines.zipWithIndex.foreach{case (line,currLineIdx) => {
              val cells = line.split("\t");
              val chrom = cells(0)
              if(keepChromFunc(chrom)){
                val (start,end) = (string2int(cells(1)),string2int(cells(2)))
                arr.addSpan(internalUtils.commonSeqUtils.GenomicInterval(chrom, '.', start,end), "CE");
              }
              if(currLineIdx % 1000000 == 0) report(".","debug");
            }}
            report("]\n","debug");
            arr.finalizeStepVectors;
            val isOnBedFunc : (internalUtils.commonSeqUtils.GenomicInterval => Boolean) = {
              (iv : internalUtils.commonSeqUtils.GenomicInterval) => {
                ! arr.findIntersectingSteps(iv).foldLeft(Set[String]()){case (soFar,(iv,currSet)) => {
                  soFar ++ currSet;
                }}.isEmpty
              }
            }
            (t,desc,isOnBedFunc)
          }}
        }
        case None => {
          Seq();
        }
      }
      
      
      reportln("Reading TX Data... "+getDateAndTimeString,"debug");
      val TXSeq : scala.collection.mutable.Map[String,TXUtil] = inputSavedTxFile match {
        case Some(txf) => {
          val ipr = internalUtils.stdUtils.IteratorProgressReporter_ThreeLevel("lines", 200, 1000 , 2000 )
          val wrappedIter = internalUtils.stdUtils.wrapIteratorWithProgressReporter(getLinesSmartUnzip(txf) , ipr )
          val txs = scala.collection.mutable.AnyRefMap[String,TXUtil]();
          wrappedIter.foreach{ line => {
            val tx = buildTXUtilFromString(line);
            if(keepChromFunc(tx.chrom) && keepTxTypeFunc(tx) && tx.isValidFullLenTX) txs += (tx.txID,tx)
          }}
          txs;
        }
        case None => {
          if(genomeFA.isEmpty){
            error("FATAL ERROR: Either the --inputSavedTxFile parameter must be set, OR --genomeFA must be set!")
          }
          if(gtffile.isEmpty){
            error("FATAL ERROR: Either the --inputSavedTxFile parameter must be set, OR --genomeFA and --gtfFile must be set!")
          }
          buildTXUtilsFromAnnotation(gtffile.get,genomeFA.get,addStopCodon=addStopCodon,chromSet=chromSet).filter{ case (txid,tx) => {keepTxTypeFunc(tx) && tx.isValidFullLenTX}}
        }
      }
      reportln("Finished TX parse.","progress");
      
      /*
      reportln("Valid Full-Length TX: "+TXSeqRaw.count{case (txID,tx) => {tx.isValidFullLenTX}}+"/"+TXSeqRaw.size+"\n"+
               "                      (Note: "+TXSeqRaw.count{case (txID,tx) => {tx.isValidFullLenTX && tx.extendedStopCodon}}+" did not include the stop codon, but a valid stop codon was found)"+
               (if(txTypes.isDefined){
             "\n                      "+TXSeqRaw.count{case (txID,tx) => {tx.isValidFullLenTX && keepTxTypeFunc(tx)}}+"/"+TXSeqRaw.count{case (txID,tx) => {keepTxTypeFunc(tx)}}+" of types: ("+txTypes.map(_.mkString(",")).getOrElse(".")+")\n"+
               "                      (Note: "+TXSeqRaw.count{case (txID,tx) => {tx.isValidFullLenTX && keepTxTypeFunc(tx) && tx.extendedStopCodon}}+" did not include the stop codon, but a valid stop codon was found)"               
               } else {""})+
               "","debug");
      
      val TXSeq : scala.collection.mutable.Map[String,TXUtil] = TXSeqRaw.filter{ case (txid,txu) => {
        keepTxTypeFunc(txu) && txu.isValidFullLenTX
      }}
      reportln("Using "+TXSeq.size+" transcripts.","note");
      */
      /*
       * .filter{ case (txid,txu) => {
            keepTxTypeFunc(tx)
          }}
       */

      outputSavedTxFile match {
        case Some(txf) => {
          reportln("Saving TX data.","progress");
          val txWriter = openWriterSmart(txf,false);
          TXSeq.foreach{ case (txID,tx) => {
            txWriter.write(tx.saveToString()+"\n");
          }}
          txWriter.close();
        }
        case None => {
          //do nothing!
        }
      }

      txInfoFile match {
        case Some(txFile) => {
          reportln("Saving TX info file.","progress");
          val txWriter = openWriterSmart(txFile,false);
          TXSeq.take(100).foreach{ case (txID,tx) => {
            try {
              txWriter.write(tx.toStringVerbose()+"\n");
            } catch {
              case e : Exception => {
                reportln("Caught error on TX:","warn");
                reportln(tx.toStringShort()+"\n","warn");
                throw e;
              }
            }
          }}
          txWriter.write("#######################################################\n");
          txWriter.write("txid\tensid\tisValidCoding\tissues\tchrom\tstart\tend\n");
          TXSeq.foreach{ case (txID,tx) => {
            txWriter.write(txID+"\t"+tx.geneID+"\t"+tx.isValidFullLenTX+"\t"+tx.getIssueList.padTo(1,".").mkString(",")+"\t"+tx.chrom+"\t"+tx.gStart+"\t"+tx.gEnd+"\n");
          }}
          txWriter.close();
        }
        case None => {
          //do nothing
        }
      }

      reportln("Starting TX GenomicArrayOfSets...","progress");
      val txgaos = gtffile match {
        case Some(gtf) => {
          new internalUtils.qcGtfAnnotationBuilder(gtffile=gtf, flatgtffile = None, stranded = false, stdCodes = GtfCodes(), flatCodes = GtfCodes()).txArrayWithBuffer
        }
        case None => {
          val txArray : GenomicArrayOfSets[String] = GenomicArrayOfSets[String](false);
          TXSeq.foreach{ case (txid,tx) => {
            tx.gSpans.foreach{ case (s,e) => {
              txArray.addSpan(GenomicInterval(tx.chrom,'.',s,e),tx.txID);
            }}
            if(tx.strand == '+'){
              txArray.addSpan(GenomicInterval(tx.chrom,'.',tx.gStart-2000,tx.gStart),tx.txID);
              txArray.addSpan(GenomicInterval(tx.chrom,'.',tx.gEnd,tx.gEnd + 500),tx.txID);
            } else {
              txArray.addSpan(GenomicInterval(tx.chrom,'.',tx.gStart-500,tx.gStart),tx.txID);
              txArray.addSpan(GenomicInterval(tx.chrom,'.',tx.gEnd,tx.gEnd + 2000),tx.txID);
            }
          }}
          txArray.finalizeStepVectors;
          txArray;
        }
      }
      
      
      reportln("Finished TX GenomicArrayOfSets.","progress");
    
    val rightAligner = genomeFA.map{ gfa => new internalUtils.GatkPublicCopy.RightAligner(genomeFa = gfa) }
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {    
      val newHeaderLines = List(
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.txList_TAG, ".", "String", "List of known transcripts found to overlap with the variant."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vType_TAG, "A", "String", "For each allele, a |-delimited list indicating the deletion type for each overlapping transcript (see "+vcfCodes.txList_TAG+" for the transcripts and "+vcfCodes.vMutLVL_TAG+" for info on the type description)"),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutG_TAG, "A", "String", "For each allele, the genomic change, in HGVS format."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutR_TAG, "A", "String", "For each allele, a |-delimited list indicating mRNA change (in HGVS format) for each transcript (see "+vcfCodes.txList_TAG+")"),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutC_TAG+"_offset", "A", "String", "Realign offsets for field "+vcfCodes.vMutC_TAG),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutC_TAG+"_offsetVAR", "A", "String", "variant info with Realign offsets for field "+vcfCodes.vMutC_TAG),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutC_TAG, "A", "String", "For each allele, a |-delimited list indicating cDNA change (in HGVS format) for each transcript (see "+vcfCodes.txList_TAG+")"),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutP_TAG, "A", "String", "For each allele, a |-delimited list indicating amino-acid change (in HGVS format) for each transcript (see "+vcfCodes.txList_TAG+")"),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutP_TAG+"_offset", "A", "String", "With 3prime realign. For each allele, a |-delimited list indicating amino-acid change (in HGVS format) for each transcript (see "+vcfCodes.txList_TAG+")"),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vTypeShort_TAG, "A", "String", "For each allele, the worst amino acid change type found over all transcripts."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutPShort_TAG, "A", "String", "For each allele, one of the protein changes for the worst variant type found over all transcripts"),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutINFO_TAG, "A", "String", "Raw variant info for each allele."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutLVL_TAG, "A", "String", 
                                       "For each allele, the rough deliteriousness level of the variant, over all transcripts. Possible values, in order of deliteriousness "+
                                       "SYNON (synonymous mutation), "+
                                       "PSYNON (Probably-synonymous, indicates that the variant is within a transcript's introns or near a genes endpoints), "+
                                       "UNK (unknown), "+
                                       "NONSYNON (Changes one or more amino acids, or loss of the stop codon), "+
                                       "PLOF (possible loss of function, includes variants that might break splice junctions, and loss of the start codon), and "+
                                       "LLOF (likely loss of function, includes frameshift indels and variants that add early stop codons")
      ) ++ ( if(txToGeneFile.isEmpty) List() else {
        List(
          new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.geneIDs, ".", "String", "Gene Symbol for each tx.")    
        )
      }) ++ bedTags.map{ case (tagString,descString,bedFunction) => {
          new SVcfCompoundHeaderLine(in_tag = "INFO",tagString, "1", "Integer", "Variant is found on bed file "+descString)
      }} ++ rightAligner.toList.flatMap{raln => {
        
        List(
            new SVcfCompoundHeaderLine(in_tag = "INFO","SWH_txMeta_alnRawVar", "A", "String", "Original aligned version of the variant."),
            new SVcfCompoundHeaderLine(in_tag = "INFO","SWH_txMeta_alnRgtVar", "A", "String", "Right-aligned version of the variant."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutC_TAG+"_alnRgt", "A", "String", "Right-aligned version of field "+vcfCodes.vMutC_TAG),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutC_TAG+"_alnRaw", "A", "String", "Raw-aligned version of field "+vcfCodes.vMutC_TAG),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutP_TAG+"_alnRgt", "A", "String", "Right-aligned version of field "+vcfCodes.vMutP_TAG),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vType_TAG+"_alnRgt", "A", "String", "Right-aligned version of field "+vcfCodes.vType_TAG),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vTypeShort_TAG+"_alnRgt", "A", "String", "Right aligned version. For each allele, the worst amino acid change type found over all transcripts."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutPShort_TAG+"_alnRgt", "A", "String", "Right aligned version. For each allele, one of the protein changes for the worst variant type found over all transcripts"),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutINFO_TAG+"_alnRgt", "A", "String", "Right aligned version. Raw variant info for each allele."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutLVL_TAG+"_alnRgt", "A", "String", "Right aligned version. See "+vcfCodes.vMutLVL_TAG),
            new SVcfCompoundHeaderLine(in_tag = "INFO","SWH_txSummary_WARN_lvlChange", "A", "String", "Flag. Equals 1 iff the summary variant changes varLvl between PLOF, LLOF, and NONSYNON levels when right aligned."),
            new SVcfCompoundHeaderLine(in_tag = "INFO","SWH_txSummary_WARN_typeChange", "A", "String", "Flag. Equals 1 iff the summary variant major type changes between or among types contained in PLOF, LLOF, and NONSYNON variant levels when right aligned."),
            new SVcfCompoundHeaderLine(in_tag = "INFO","SWH_tx_WARN_lvlChange", "A", "String", "Flag. Equals 1 iff the variant changes varLvl between PLOF, LOF, and NONSYNON levels when right aligned. Equals dot if the variant is synon, unk, or psynon for both alignments."),
            new SVcfCompoundHeaderLine(in_tag = "INFO","SWH_tx_WARN_typeChange", "A", "String", "Flag. For each TX, Equals 1 iff the variant changes major type when right aligned. Equals dot if the variant is synon, unk, or psynon for both alignments.")
        )
      }}
      
      
      val newHeader = vcfHeader.copyHeader
      newHeaderLines.foreach{ hl => {
        newHeader.addInfoLine(hl);
      }}
      newHeader.addWalk(this);
      
      val txToGene : Option[(String => String)] = txToGeneFile match {
        case Some(f) => {
          val txToGeneMap = getLinesSmartUnzip(f).map(line => {
            val cells = line.split("\t");
            (cells(0),cells(1));
          }).toMap
          Some(((s : String) => txToGeneMap.getOrElse(s,s)));
        } 
        case None => {
          None;
        }
      }
      
      val overwriteInfos : Set[String] = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( newHeader.addedInfos );
      if( overwriteInfos.nonEmpty ){
        notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      }
      //vc.dropInfo(overwriteInfos);
      
      return (vcFlatMap(vcIter)(v => {
             annotateSVcfStreamOpt(v,summaryWriter,vcfCodes,bufferSize,txgaos,TXSeq,txToGene,geneVariantsOnly=geneVariantsOnly,bedTags=bedTags,overwriteInfos=overwriteInfos)
        }),
        newHeader);
      }
    
  
    
  def trimmed(pos : Int, ref : String, alt : String) : (Int,String,String) = {
    if(ref.length == 1 && alt.length == 1){
      (pos,ref,alt);
    } else if(ref.length > 0 && alt.length > 0 && ref.head == alt.head){
      return trimmed(pos = pos + 1,ref.tail,alt.tail);
    } else if(ref.length > 0 && alt.length > 0 && ref.last == alt.last){
      return trimmed(pos = pos,ref.init, alt.init);
    } else {
      (pos,ref,alt)
    }
  }
  
  val NONSYNON_LEVELS = Set("LLOF","PLOF","NONSYNON")
  def levelIsDifferent(p1 : TXUtil.pVariantInfo, p2 : TXUtil.pVariantInfo) : Boolean = {
    val (x1,x2) = (p1.severityType,p2.severityType)
    (NONSYNON_LEVELS.contains(x1) || NONSYNON_LEVELS.contains(x2)) && (x1 != x2)
  }
  def typeIsDifferent(p1 : TXUtil.pVariantInfo, p2 : TXUtil.pVariantInfo) : Boolean = {
    val (x1,x2) = (p1.severityType,p2.severityType)
    val (y1,y2) = (p1.varType,p2.varType)
    (NONSYNON_LEVELS.contains(x1) || NONSYNON_LEVELS.contains(x2)) && (y1 != y2)
  }
  
  def annotateSVcfStreamOpt(v : SVcfVariantLine, writer : Option[WriterUtil], vcfCodes : VCFAnnoCodes,
                        bufferSize : Int, txgaos : GenomicArrayOfSets[String],
                        TXSeq : scala.collection.mutable.Map[String,TXUtil],
                        txToGene : Option[(String => String)],
                        geneVariantsOnly : Boolean,
                        bedTags : Seq[(String,String,internalUtils.commonSeqUtils.GenomicInterval => Boolean)],
                        overwriteInfos : Set[String]
                        ) : Option[SVcfVariantLine] = {
      
      
      var vb = v.getOutputLine();
      vb.dropInfo(overwriteInfos);
      var vTypeList = Vector[Vector[String]]();
      var vMutG = Vector[String]();
      var vMutR = Vector[Vector[String]]();
      var vMutC = Vector[Vector[String]]();
      var vMutP = Vector[Vector[String]]();
      var vMutPra = Vector[Vector[String]]();
      var vMutCoffset = Vector[Vector[String]]();
      var vMutCoffsetvar = Vector[Vector[String]]();
      
      var vMutPshort = Vector[String]();
      var vTypeListShort = Vector[String]();
      var vLevel = Vector[String]();
      var vLevelList = Vector[Vector[String]]();
      var vLevelListRgt = Vector[Vector[String]]();
      
      var vInfo = Vector[Vector[String]]();
      
      var rightAlignList = Vector[String]();
      var rawAlignList = Vector[String]();
      var vMutCrgt = Vector[Vector[String]]();
      var vMutCraw = Vector[Vector[String]]();
      var vMutPrgt = Vector[Vector[String]]();
      var vTypeListrgt = Vector[Vector[String]]();
      var vLvlDiff  = Vector[Vector[String]]();
      var vTypeDiff = Vector[Vector[String]]();
      
      val refAlle = v.ref;
      val altAlleles = v.alt.filter{ alt => alt != internalUtils.VcfTool.UNKNOWN_ALT_TAG_STRING && alt != internalUtils.VcfTool.MISSING_VALUE_STRING }
      if(altAlleles.length > 0){
        val start = v.pos - 1
        val end = start + refAlle.length() // math.max(refAlle.length(), altAlleles.map(a => a.length()).max)
        val txList = txgaos.findIntersectingSteps(GenomicInterval(chromName = v.chrom, strand = '.', start = start - bufferSize, end = end + bufferSize)).foldLeft(Set[String]()){case (soFar,(iv,gset))=>{ soFar ++ gset }}.filter(TXSeq.contains(_)).filter{TXSeq(_).isValidFullLenTX}.toVector.sorted;
        if(geneVariantsOnly && txList.length == 0){
          notice("Dropping variant due to lack of nearby genes: \n"+v.getSimpleVcfString(),"DROPPED_INTERGENIC_VARIANT",5);
          return None;
        }
        
        txToGene match {
          case Some(fun) => {
            vb.addInfo(  vcfCodes.geneIDs, txList.map(x => fun(x)).padTo(1,".").mkString(",")  );
          }
          case None => {
            //do nothing
          }
        }
        if(! writer.isEmpty) {
              writer.get.write(v.chrom+"\t"+start+"\t"+end+"\t"+""+"\t"+""+"\t"+""+"\t"+""+"\t"+""+"\t"+""+"\t"+""+"\t"+""+"\t \n");
              writer.get.write("\t\t\t"+v.info.getOrElse("ANN","NA")+"\t"+v.info.getOrElse("LOF","NA")+"\n");
        }
        
        for(altAlle <- altAlleles){
          val (ref,alt) = (refAlle, altAlle);
          val mutG = "g."+getMutString(ref, alt, pos = start, getPosString = {(a) => (a + 1).toString},swapStrand=false);
          val (trimPos,trimRef,trimAlt) = trimmed(v.pos,ref,alt);
          val trimString = v.chrom+":"+trimPos+":"+trimRef+">"+trimAlt
          val rightAln = rightAligner.map{ rta => rta.rightAlignIndelFromFA(v.chrom,v.pos,ref,alt) }
          rightAln.foreach{ case (posrgt,refrgt,altrgt) => {
            val startrgt = posrgt - 1;
            rightAlignList = rightAlignList :+ v.chrom+":"+posrgt+":"+refrgt+">"+altrgt
            rawAlignList = rawAlignList :+ v.chrom+":"+trimPos+":"+trimRef+">"+trimAlt
            vMutCrgt = vMutCrgt :+ Vector[String]();
            vMutCraw = vMutCraw :+ Vector[String]();
            vMutPrgt = vMutPrgt :+ Vector[String]();
            vTypeListrgt = vTypeListrgt :+ Vector[String]();
            //val vLvlDiffTx  = Array.ofDim[String](txList.length);
            //val vTypeDiffTx = Array.ofDim[String](txList.length);
            val vLevelListRgtTx = Array.ofDim[String](txList.length)
            for((tx,txidx) <- txList.map(TXSeq(_)).zipWithIndex){
              val mutPrgt   = tx.getProteinMut(refrgt,altrgt,gPos=startrgt);
              val mutCraw   = tx.getSimpleCdsMutString(ref,alt,gPos=start);
              val mutCrgt   = tx.getSimpleCdsMutString(refrgt,altrgt,gPos=startrgt);
              vMutCrgt = vMutCrgt.updated(vMutCrgt.length-1,vMutCrgt.last :+ mutCrgt);
              vMutCraw = vMutCrgt.updated(vMutCraw.length-1,vMutCraw.last :+ mutCraw);
              vMutPrgt = vMutCrgt.updated(vMutPrgt.length-1,vMutPrgt.last :+ mutPrgt.pvar);
              vTypeListrgt = vTypeListrgt.updated(vTypeListrgt.length - 1, vTypeListrgt.last :+ mutPrgt.varType)
              vLevelListRgtTx(txidx) = mutPrgt.severityType;
            }
            vLevelListRgt = vLevelListRgt :+ vLevelListRgtTx.toVector
            //vTypeDiff = vTypeDiff :+ vTypeDiffTx.toVector
            //vTypeListrgt = vTypeListrgt :+ vTypeListrgtTx.toVector
            /*
            new SVcfCompoundHeaderLine(in_tag = "INFO","SWH_txMeta_alnRawVar", "A", "String", "Original aligned version of the variant."),
            new SVcfCompoundHeaderLine(in_tag = "INFO","SWH_txMeta_alnRgtVar", "A", "String", "Right-aligned version of the variant."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutC_TAG+"_alnRgt", "A", "String", "Right-aligned version of field "+vcfCodes.vMutC_TAG),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutC_TAG+"_alnRaw", "A", "String", "Raw-aligned version of field "+vcfCodes.vMutC_TAG),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutP_TAG+"_alnRgt", "A", "String", "Right-aligned version of field "+vcfCodes.vMutP_TAG),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vTypeShort_TAG+"_alnRgt", "A", "String", "Right aligned version. For each allele, the worst amino acid change type found over all transcripts."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutPShort_TAG+"_alnRgt", "A", "String", "Right aligned version. For each allele, one of the protein changes for the worst variant type found over all transcripts"),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutINFO_TAG+"_alnRgt", "A", "String", "Right aligned version. Raw variant info for each allele."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutLVL_TAG+"_alnRgt", "A", "String", "Right aligned version. See "+vcfCodes.vMutLVL_TAG),
            new SVcfCompoundHeaderLine(in_tag = "INFO","SWH_txSummary_WARN_lvlChange", "A", "String", "Flag. Equals 1 iff the summary variant changes varLvl between PLOF, LLOF, and NONSYNON levels when right aligned."),
            new SVcfCompoundHeaderLine(in_tag = "INFO","SWH_txSummary_WARN_typeChange", "A", "String", "Flag. Equals 1 iff the summary variant major type changes between or among types contained in PLOF, LLOF, and NONSYNON variant levels when right aligned."),
            new SVcfCompoundHeaderLine(in_tag = "INFO","SWH_tx_WARN_lvlChange", "A", "String", "Flag. Equals 1 iff the variant changes varLvl between PLOF, LOF, and NONSYNON levels when right aligned."),
            new SVcfCompoundHeaderLine(in_tag = "INFO","SWH_tx_WARN_typeChange", "A", "String", "Flag. Equals 1 iff the variant changes major type when right aligned.")
            * */
          }}
          
          vMutG = vMutG :+ mutG
          vMutR = vMutR :+ Vector[String]();
          vMutC = vMutC :+ Vector[String]();
          vMutCoffset = vMutCoffset :+ Vector[String]();
          vMutCoffsetvar = vMutCoffsetvar :+ Vector[String]();
          vMutP = vMutP :+ Vector[String]();
          vMutPra = vMutPra :+ Vector[String]();
          
          vTypeList = vTypeList :+ Vector[String]();
          vInfo = vInfo :+ Vector[String]();
          val vLevelListTx = Array.ofDim[String](txList.length)
          
          for((tx,txidx) <- txList.map(TXSeq(_)).zipWithIndex){
            val mutR = "r."+tx.getRnaMut(ref,alt,start)  //getMutString(ref,alt, pos = start, getPosString = {(g) => tx.getRPos(g)}, swapStrand = (tx.strand == '-'));
            //val mutC = "c."+tx.getCdsMut(ref,alt,start)  //getMutString(ref,alt, pos = start, getPosString = {(g) => tx.getCPos(g)}, swapStrand = (tx.strand == '-'));
            val (mutCraw,(racRef,racAlt,racPos),racOffset) = tx.getCdsMutInfo(ref,alt,start)
            val mutC = "c."+mutCraw
            
            //try{
              val mutP   = tx.getProteinMut(ref,alt,gPos=start);
              val mutPra = tx.getProteinMut(racRef,racAlt,gPos=racPos);
              vMutR = vMutR.updated(vMutR.length-1,vMutR.last :+ mutR);
              vMutC = vMutC.updated(vMutC.length-1,vMutC.last :+ mutC);
              vMutCoffset    = vMutCoffset.updated( vMutCoffset.length - 1, vMutCoffset.last :+ (""+racOffset) );
              vMutCoffsetvar = vMutCoffsetvar.updated( vMutCoffsetvar.length - 1, vMutCoffsetvar.last :+ (racPos+":"+racRef+">"+racAlt) );
              
              vMutP = vMutP.updated(vMutP.length-1,vMutP.last :+ mutP.pvar);
              vMutPra = vMutP.updated(vMutPra.length-1,vMutPra.last :+ mutPra.pvar);
              
              vTypeList = vTypeList.updated(vTypeList.length-1,vTypeList.last :+ mutP.varType);
              vInfo = vInfo.updated(vInfo.length-1,vInfo.last :+ mutP.saveToString());
              vLevelListTx(txidx) = mutP.severityType;
              if(! writer.isEmpty) {
                writer.get.write(v.chrom+":"+start+"-"+end+"\t"+ref+"\t"+alt+"\t"+tx.txID+"\t"+tx.strand+"\t"+
                                 mutG+"\t"+mutR+"\t"+mutC+"\t"+mutP.pvar+"\t"+mutP.varType+"\t"+mutP.cType+"\t"+mutP.severityType+"\t"+mutP.pType+"\t"+mutP.subType+
                                 "\n");
              }
            /*} catch {
              case e : Exception => {
                reportln("Error:","warn");
                reportln("TX:","warn");
                reportln(tx.toStringVerbose(),"warn");
                reportln("and variant: ","warn");
                reportln(mutG + "\t"+mutR+"\t"+mutC,"warn");
                throw e;
              }
            }*/
          }
          vLevelList = vLevelList :+ vLevelListTx.toVector;
          
          //try{
            val (mutPshort,typeShort,vLvl) = internalUtils.TXUtil.getWorstProteinMut(vMutP.last.zip(vTypeList.last),txList);
            vMutPshort = vMutPshort :+ mutPshort;
            vTypeListShort = vTypeListShort :+ typeShort;
            vLevel = vLevel :+ vLvl;
          /*} catch {
              case e : Exception => {
                reportln("ERROR:","debug");
                reportln(v.chrom+":"+start+"-"+end+"\t"+ref+"\t"+alt+"\t"+txList.mkString(",")+"\t"+ vMutP.map(_.mkString("|")).mkString(",")+"\t"+ vTypeList.map(_.mkString("|")).mkString(",")+"\n","debug");
                if( ! writer.isEmpty) writer.get.close();
                throw e;
              }
          }*/
          
          rightAligner.foreach{ ra => {
            val (lvlDiff, typeDiff) = vLevelList.last.zip(vLevelListRgt.last).zip(vTypeList.last).zip(vTypeListrgt.last).map{ case (((lvlRaw,lvlRgt),typeRaw),typeRgt) => {
              if( NONSYNON_LEVELS.contains(lvlRaw) || NONSYNON_LEVELS.contains(lvlRgt) ){
                ((if(lvlRaw != lvlRgt){"1"}else{"0"}) , (if(typeRaw != typeRgt){"1"}else{"0"}))
              } else {
                (".",".")
              }
            }}.unzip
            vLvlDiff = vLvlDiff :+ lvlDiff
            vTypeDiff = vTypeDiff :+ typeDiff
            
            
          }}
        }
        /*

"SWH_txMeta_alnRawVar", "A", "String", "Original aligned version of the variant."),
"SWH_txMeta_alnRgtVar", "A", "String", "Right-aligned version of the variant."),
vcfCodes.vMutC_TAG+"_alnRgt", "A", "String", "Right-aligned version of field "+vcfCodes.vMutC_TAG),
vcfCodes.vMutC_TAG+"_alnRaw", "A", "String", "Raw-aligned version of field "+vcfCodes.vMutC_TAG),
vcfCodes.vMutP_TAG+"_alnRgt", "A", "String", "Right-aligned version of field "+vcfCodes.vMutP_TAG),
vcfCodes.vType_TAG+"_alnRgt", "A", "String", "Right-aligned version of field "+vcfCodes.vType_TAG),
vcfCodes.vTypeShort_TAG+"_alnRgt", "A", "String", "Right aligned version. For each allele, the worst amino acid change type found over all transcripts."),
vcfCodes.vMutPShort_TAG+"_alnRgt", "A", "String", "Right aligned version. For each allele, one of the protein changes for the worst variant type found over all transcripts"),
vcfCodes.vMutINFO_TAG+"_alnRgt", "A", "String", "Right aligned version. Raw variant info for each allele."),
vcfCodes.vMutLVL_TAG+"_alnRgt", "A", "String", "Right aligned version. See "+vcfCodes.vMutLVL_TAG),
"SWH_txSummary_WARN_lvlChange", "A", "String", "Flag. Equals 1 iff the summary variant changes varLvl between PLOF, LLOF, and NONSYNON levels when right aligned."),
"SWH_txSummary_WARN_typeChange", "A", "String", "Flag. Equals 1 iff the summary variant major type changes between or among types contained in PLOF, LLOF, and NONSYNON variant levels when right aligned."),
"SWH_tx_WARN_lvlChange", "A", "String", "Flag. Equals 1 iff the variant changes varLvl between PLOF, LOF, and NONSYNON levels when right aligned."),
"SWH_tx_WARN_typeChange", "A", "String", "Flag. Equals 1 iff the variant changes major type when right aligned.")
     
      var rightAlignList = Vector[String]();
      var rawAlignList = Vector[String]();
      var vMutCrgt = Vector[Vector[String]]();
      var vMutCraw = Vector[Vector[String]]();
      var vMutPrgt = Vector[Vector[String]]();
      var vTypeListrgt = Vector[Vector[String]]();
         
        */
        vb.addInfo(vcfCodes.txList_TAG, txList.padTo(1,".").mkString(","));
        vb.addInfo(vcfCodes.vMutG_TAG, vMutG.toList.mkString(","));
        vb.addInfo(vcfCodes.vMutR_TAG, mkSubDelimListScala(vMutR,vcfCodes.delims).mkString(","));
        vb.addInfo(vcfCodes.vMutC_TAG, mkSubDelimListScala(vMutC,vcfCodes.delims).mkString(","));
        vb.addInfo(vcfCodes.vMutC_TAG+"_offset", mkSubDelimListScala(vMutCoffset,vcfCodes.delims).mkString(","));
        vb.addInfo(vcfCodes.vMutC_TAG+"_offsetVAR", mkSubDelimListScala(vMutCoffsetvar,vcfCodes.delims).mkString(","));
        vb.addInfo(vcfCodes.vMutP_TAG, mkSubDelimListScala(vMutP,vcfCodes.delims).mkString(","));
        vb.addInfo(vcfCodes.vMutP_TAG+"_offset", mkSubDelimListScala(vMutPra,vcfCodes.delims).mkString(","));

        vb.addInfo(vcfCodes.vType_TAG, mkSubDelimListScala(vTypeList,vcfCodes.delims).mkString(","));
        vb.addInfo(vcfCodes.vTypeShort_TAG, vTypeListShort.toList.mkString(","));
        vb.addInfo(vcfCodes.vMutPShort_TAG, vMutPshort.toList.mkString(","));
        vb.addInfo(vcfCodes.vMutLVL_TAG,  vLevel.toList.mkString(","));
        vb.addInfo(vcfCodes.vMutINFO_TAG, mkSubDelimListScala(vInfo,vcfCodes.delims).mkString(","));
        
        rightAligner.foreach{ ra => {
          vb.addInfo("SWH_txMeta_alnRawVar",rawAlignList.mkString(","));
          vb.addInfo("SWH_txMeta_alnRgtVar",rightAlignList.mkString(","));
          vb.addInfo(vcfCodes.vMutP_TAG+"_alnRgt",mkSubDelimListScala(vMutPrgt,vcfCodes.delims).mkString(","));
          vb.addInfo(vcfCodes.vType_TAG+"_alnRgt",mkSubDelimListScala(vTypeListrgt,vcfCodes.delims).mkString(","));
          vb.addInfo(vcfCodes.vMutC_TAG+"_alnRaw",mkSubDelimListScala(vMutCraw,vcfCodes.delims).mkString(","));
          vb.addInfo(vcfCodes.vMutC_TAG+"_alnRgt",mkSubDelimListScala(vMutCrgt,vcfCodes.delims).mkString(","));
          vb.addInfo("SWH_tx_WARN_lvlChange",mkSubDelimListScala(vLvlDiff,vcfCodes.delims).mkString(","));
          vb.addInfo("SWH_tx_WARN_typeChange",mkSubDelimListScala(vTypeDiff,vcfCodes.delims).mkString(","));
        }}
        
        if(! bedTags.isEmpty){
          val variantIV = internalUtils.commonSeqUtils.GenomicInterval(v.chrom,'.', start = v.pos - 1, end = v.pos + math.max(1,v.ref.length));
          bedTags.foreach{ case (tagString,desc,bedFunction) => {
            //vb = vb.attribute(tagString, if(bedFunction(variantIV)) "1" else "0");
            vb.addInfo(tagString, if(bedFunction(variantIV)) "1" else "0");
          }}
        }
      } else if(geneVariantsOnly){
        notice("Dropping variant due to lack of nearby genes: \n"+v.getSimpleVcfString(),"DROPPED_INTERGENIC_VARIANT",5);
        return None;
      }
      return Some(vb);
  }
    
    }
   


  
  class testTXSeqUtil extends CommandLineRunUtil {
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
                    new UnaryArgument( name = "cdsRegionContainsStop",
                                         arg = List("--cdsRegionContainsStop"), // name of value
                                         argDesc = ""+
                                                   "" // description
                                       ) ::
                    new UnaryArgument( name = "version",
                                         arg = List("--version"), // name of value
                                         argDesc = "Flag. If raised, print version." // description
                                       ) ::
                    new FinalArgument[String](
                                         name = "gtffile",
                                         valueName = "infile.gtf",
                                         argDesc = "infile" // description
                                        ) ::
                    new FinalArgument[String](
                                         name = "genomeFA",
                                         valueName = "genomeFA.fa",
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
       if(out){
         testTXSeqUtil(parser.get[String]("gtffile"),
                       parser.get[String]("genomeFA"),
                       parser.get[String]("outfile"),
                       addStopCodon = ! parser.get[Boolean]("cdsRegionContainsStop")
             )
       }
       
     }
   }
  
  def testTXSeqUtil(gtffile : String, genomeFA : String, outfile : String, addStopCodon : Boolean){
    val genomeSeq = internalUtils.genomicAnnoUtils.buildEfficientGenomeSeqContainer(Seq(genomeFA));
    val writer = openWriterSmart(outfile,true);
    
    genomeSeq.shiftBufferTo("chr20",200000);
    
    reportln("genomeSeq test:","debug");
    writer.write("genomeSeq test:\n");
    for(i <- Range(0,100)){
      val (start,end) = (200000 + i * 1000,200000 + (i+1) * 1000);
      writer.write("chr20:"+(start+1)+"-"+end+" "+genomeSeq.getSeqForInterval("chr20",start,end)+"\n");
    }
    //chr20:251503-251908
    writer.write("chr20:"+(251503+1)+"-"+251908+" "+genomeSeq.getSeqForInterval("chr20",251503,251908)+"\n");
    writer.flush();
    //chr20:251504-251908
    
    val TXSeq : scala.collection.mutable.Map[String,TXUtil] = buildTXUtilsFromAnnotation(gtffile,genomeFA, addStopCodon = addStopCodon,debugMode = true);
    
    reportln("Starting output write...","progress");

    
    for(((txID,tx),i) <- TXSeq.iterator.zipWithIndex.take(200)){
      
      val ts = tx.toStringVerbose()
      writer.write(ts+"");

      /*
      writer.write(txID+":\n");
      writer.write(tx.gSpans.map{case (i,j) => "("+i + ","+j+")"}.mkString(" ")+"\n");
      writer.write(tx.rSpansGS.map{case (i,j) => "("+i + ","+j+")"}.mkString(" ")+"\n");
      writer.write(tx.rSpans.map{case (i,j) => "("+i + ","+j+")"}.mkString(" ")+"\n");
      writer.write(tx.seqGS.mkString("")+"\n");
      writer.write(tx.rSpansGS.map{case (i,j) => repString(" ",j-i-1) + "|"}.mkString("")+"\n");
      writer.write(tx.seq.mkString("")+"\n");
      writer.write(tx.rSpans.map{case (i,j) => repString(" ",j-i-1) + "|"}.mkString("")+"\n");
      writer.write(tx.cSeq.mkString("")+"\n");
      */
      /*if(i < 10){
        reportln(".","progress");
        reportln(ts,"progress");
        reportln("ts.length = "+ts.length,"progress");
        //reportln(txID+": "+tx.aa.size,"debug");
        //reportln(tx.aa.mkString("  "),"debug");
      }*/
      //writer.write(internalUtils.commonSeqUtils.getAminoAcidFromSeq(tx.seq,0).mkString("")+"\n");
    }
    reportln("TX anno complete.","progress");
    writer.flush();
    
    val txids = Vector("TESTTX001","TESTTX002","TESTTX003","TESTTX004");
    /*
    val start = 0;
    val end = 140;
    val txList = txids.map(TXSeq(_));
    for(i <- Range(start,end)){
      writer.write(i+"\t"+txList.map(_.getCPos(i)).mkString("\t")+"\n");
      
    }*/
    
    reportln("Output write complete.","progress");
    writer.close();
    reportln("Output writer closed.","progress");
  }
  
  class ConvertGenoPosToCPos extends CommandLineRunUtil {
     override def priority = 100;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "ConvertGtoC", 
          quickSynopsis = "", 
          synopsis = "", 
          description = "Test utility.",   
          argList = 
                    new FinalArgument[String](
                                         name = "infile",
                                         valueName = "infile.txt",
                                         argDesc = "infile" // description
                                        ) ::
                    new FinalArgument[String](
                                         name = "gtffile",
                                         valueName = "gtffile.gtf",
                                         argDesc = "gtffile" // description
                                        ) ::
                    new FinalArgument[String](
                                         name = "genomeFA",
                                         valueName = "genomeFA.fa",
                                         argDesc = "genomeFA" // description
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
       if(out){
         ConvertGenoPosToCPos(parser.get[String]("infile"),
                       parser.get[String]("gtffile"),
                       parser.get[String]("genomeFA"),
                       parser.get[String]("outfile")
             )
       }
     }
   }
  
  def ConvertGenoPosToCPos(infile : String, gtffile : String, genomeFA : String,  outfile : String){
    val TXSeq : scala.collection.mutable.Map[String,TXUtil] = buildTXUtilsFromAnnotation(gtffile,genomeFA);
    val writer = openWriterSmart(outfile,true);
      
    //
    
    val lines = getLinesSmartUnzip(infile,true);
    
    for(line <- lines){
      val cells = line.split("\\s+");
      val txid = cells(0);
      val pos = string2int(cells(1));
      writer.write(line+"\t"+TXSeq(txid).getCPos(pos));
    }
    
    writer.close();
  }
  
  

  class ConvertAminoRangeToGenoRange extends CommandLineRunUtil {
     override def priority = 100;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "ConvertAminoRangeToGenoRange", 
          quickSynopsis = "", 
          synopsis = "", 
          description = " " + ALPHA_WARNING,   
          argList = 
                    new BinaryArgument[Int](   name = "txCol",
                                               arg = List("--txCol"),  
                                               valueName = "0", 
                                               argDesc = "", 
                                               defaultValue = Some(0)
                                           ) ::
                    new BinaryArgument[Int](   name = "startCol",
                                               arg = List("--startCol"),  
                                               valueName = "1", 
                                               argDesc = "", 
                                               defaultValue = Some(1)
                                           ) ::
                    new BinaryArgument[Int](   name = "endCol",
                                               arg = List("--endCol"),  
                                               valueName = "2", 
                                               argDesc = "", 
                                               defaultValue = Some(2)
                                           ) ::
                    new BinaryOptionArgument[String](
                                         name = "badSpanFile", 
                                         arg = List("--badSpanFile"), 
                                         valueName = "file.txt",  
                                         argDesc =  "..."
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "unkTxFile", 
                                         arg = List("--unkTxFile"), 
                                         valueName = "file.txt",  
                                         argDesc =  "..."
                                        ) ::     
                    new FinalArgument[String](
                                         name = "infile",
                                         valueName = "infile.txt",
                                         argDesc = "infile" // description
                                        ) ::
                    new FinalArgument[String](
                                         name = "txdataFile",
                                         valueName = "txdataFile.txt.gz",
                                         argDesc = "txdataFile" // description
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
       if(out){
         ConvertAminoRangeToGenoRange(parser.get[String]("infile"),
                       parser.get[String]("txdataFile"),
                       parser.get[String]("outfile"),
                       parser.get[Int]("txCol"),
                       parser.get[Int]("startCol"),
                       parser.get[Int]("endCol"),
                       badSpanFile =  parser.get[Option[String]]("badSpanFile"),
                       unkTxFile =  parser.get[Option[String]]("unkTxFile")
             )
       }
     }
   }
  
  
  def ConvertAminoRangeToGenoRange(infile : String, txdataFile : String, outfile : String,
                                   txIdx : Int = 0, startIdx : Int = 1, endIdx : Int = 2, 
                                   badSpanFile : Option[String] = None,
                                   unkTxFile : Option[String] = None){
    //val TXSeq : scala.collection.mutable.Map[String,TXUtil] = buildTXUtilsFromAnnotation(gtffile,genomeFA);
    
    val TXSeq : scala.collection.mutable.Map[String,TXUtil] = scala.collection.mutable.AnyRefMap[String,TXUtil]();
    
    reportln("Reading TX file...","progress");
    wrapIteratorWithAdvancedProgressReporter(getLinesSmartUnzip(txdataFile),AdvancedIteratorProgressReporter_ThreeLevelAuto[String]()).foreach(line => {
      val tx = buildTXUtilFromString(line);
      TXSeq.put(tx.txID,tx);
    });
    reportln("Finished with TX file.","progress");
      
    val writer = openWriterSmart(outfile,true);

    val (firstLine,lines) = peekIterator(wrapIteratorWithAdvancedProgressReporter(getLinesSmartUnzip(infile,true),AdvancedIteratorProgressReporter_ThreeLevelAuto[String]()));
    val firstCells = firstLine.split("\t");
    val copyCols = (Range(0,firstCells.size).toSet -- Set(txIdx,startIdx,endIdx)).toVector.sorted;
    
    writer.write("#chrom\tstart\tend\tprotSymbol\tprotLen\ttxID\tdomainStartAA\tdomainEndAA\tdomainID\ttxLenAA\ttxStart\ttxEnd\tdomainUID\n")
    
    var badSpanSet = Set[(String,Int,Int,String)]();
    var unkTx = Set[String]();
    
    var uidSet = Set[String]();
    
    for((line,lnct) <- lines.zipWithIndex){
      val cells = line.split("\t");
      val txid  = cells(txIdx);
      val protLenAA = string2int(cells(1));
      //val txLenAA = string2int(cells(7));
      val startAA = string2int(cells(startIdx)) - 1;
      val endAA   = if(cells(endIdx).head == '>') string2int(cells(endIdx).tail) else string2int(cells(endIdx))
      val startC = startAA * 3;
      val endC   = endAA * 3;
      
      val cleanID = cells(5).replaceAll("\\||/| |\\.|,|-|:|;","_").replaceAll("[\\(\\)\\[\\]]","").replaceAll("[_]+","_").replaceAll("_$","");
      //cells(5).replaceAllLiterally(" ","_").replaceAllLiterally("|","_").replaceAllLiterally(".","_").replaceAllLiterally("-","_").replaceAllLiterally(",","_").replaceAllLiterally("/","_").replaceAllLiterally("(","").replaceAllLiterally(")","")
      var idNum = 1;
      var uid = cells(0) + ":" + cleanID + ":" + idNum;
      while(uidSet.contains(uid)){
        idNum = idNum + 1;
        uid = cells(0) + ":" + cleanID + ":" + idNum;
      }
      
      uidSet = uidSet + uid;
      
      if(TXSeq.contains(txid)){
          TXSeq.get(txid) match {
            case Some(tx) => {
              val txLenAA = tx.cLen/3;
              if(protLenAA + 1 == txLenAA){
                val gSpans = tx.convertCSpanToGSpan(startC,endC);
                
                gSpans.foreach{case (s,e) => {
                  writer.write(tx.chrom+"\t"+s+"\t"+e+"\t"+line+"\t"+txLenAA+"\t"+tx.gStart+"\t"+tx.gEnd+"\t"+uid+"\n");
                }}
              } else {
                badSpanSet = badSpanSet + ((tx.chrom,tx.gStart,tx.gEnd,txid)); 
              }
            }
            case None => {
              //writer.write(line+"\t-1\t-1\t-1\n");
              //impossible state!
            }
          }
      } else {
        unkTx = unkTx + txid;
      }
      //writer.write(line+"\t"+TXSeq(txid).getCPos(pos));
    }
    badSpanFile match {
      case Some(f) => {
        val w = openWriterSmart(f);
        w.write("chrom\tstart\tend\ttxID\n")
        val badSpanList = badSpanSet.toVector.sorted;
        badSpanList.foreach{case (chrom,start,end,txid) => {
          w.write(chrom+"\t"+start+"\t"+end+"\t"+txid+"\n");
        }}
        w.close();
      }
      case None => {
        //do nothing
      }
    }
    unkTxFile match {
      case Some(f) => {
        val w = openWriterSmart(f);
        //w.write("chrom\tstart\tend\ttxID")
        val txList = unkTx.toVector.sorted;
        txList.foreach{txid => {
          w.write(txid+"\n");
        }}
        w.close();
      }
      case None => {
        //do nothing
      }
    }
    
    writer.close();
  }
  
  
  
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  class AddGroupSummaries extends CommandLineRunUtil {
     override def priority = 100;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "addGroupSummaries", 
          quickSynopsis = "", 
          synopsis = "", 
          description = " " + ALPHA_WARNING,
          argList = 
                    new BinaryOptionArgument[List[String]](
                                         name = "chromList", 
                                         arg = List("--chromList"), 
                                         valueName = "chr1,chr2,...",  
                                         argDesc =  "..."
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "groupFile", 
                                         arg = List("--groupFile"), 
                                         valueName = "file.txt",  
                                         argDesc =  "..."
                                        ) ::
                    new BinaryArgument[String](
                                         name = "GTTag", 
                                         arg = List("--GTTag"), 
                                         valueName = "GT",  
                                         argDesc =  ".",
                                         defaultValue = Some("GT")
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "groupList", 
                                         arg = List("--groupList"), 
                                         valueName = "grpA,A1,A2,...;grpB,B1,...",  
                                         argDesc =  "..."
                                        ) ::
                    new BinaryOptionArgument[Int](
                                         name = "numLinesRead", 
                                         arg = List("--numLinesRead"), 
                                         valueName = "n",  
                                         argDesc =  "..."
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "superGroupList", 
                                         arg = List("--superGroupList"), 
                                         valueName = "sup1,grpA,grpB,...;sup2,grpC,grpD,...",  
                                         argDesc =  "..."
                                        ) ::
                    new UnaryArgument( name = "noCts",
                                         arg = List("--noCts"), // name of value
                                         argDesc = ""+
                                                   "" // description
                                       ) :: 
                    new UnaryArgument( name = "noFrq",
                                         arg = List("--noFrq"), // name of value
                                         argDesc = ""+
                                                   "" // description
                                       ) :: 
                    new UnaryArgument( name = "noAlle",
                                         arg = List("--noAlle"), // name of value
                                         argDesc = ""+
                                                   "" // description
                                       ) ::     
                    new UnaryArgument( name = "noGeno",
                                         arg = List("--noGeno"), // name of value
                                         argDesc = ""+
                                                   "" // description
                                       ) :: 
                    new UnaryArgument( name = "noMiss",
                                         arg = List("--noMiss"), // name of value
                                         argDesc = ""+
                                                   "" // description
                                       ) :: 
                    new UnaryArgument( name = "noMultiAllelics",
                                         arg = List("--noMultiAllelics"), // name of value
                                         argDesc = ""+
                                                   "" // description
                                       ) :: 
                    new UnaryArgument( name = "fallbackParser",
                                         arg = List("--fallbackParser"), // name of value
                                         argDesc = ""+
                                                   "" // description
                                       ) :: 
                    new FinalArgument[String](
                                         name = "invcf",
                                         valueName = "variants.vcf",
                                         argDesc = "infput VCF file" // description
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
       if(parser.get[Boolean]("fallbackParser")){
       
       LegacyVcfWalkers.AddGroupInfoAnno(groupFile = parser.get[Option[String]]("groupFile"),
                            groupList = parser.get[Option[String]]("groupList"),
                            superGroupList = parser.get[Option[String]]("superGroupList"),
                            chromList = parser.get[Option[List[String]]]("chromList"),
                            addCounts = ! parser.get[Boolean]("noCts"),
                            addFreq   = ! parser.get[Boolean]("noFrq"),
                            addMiss   = ! parser.get[Boolean]("noMiss"),
                            addAlle   = ! parser.get[Boolean]("noAlle"),
                            addHetHom = ! parser.get[Boolean]("noGeno"),
                            noMultiAllelics = parser.get[Boolean]("noMultiAllelics")
                          ).walkVCFFile(
                              parser.get[String]("invcf"), 
                              parser.get[String]("outvcf"),
                              chromList = parser.get[Option[List[String]]]("chromList")
                          );
       } else {
       SAddGroupInfoAnno(groupFile = parser.get[Option[String]]("groupFile"),
                            groupList = parser.get[Option[String]]("groupList"),
                            superGroupList = parser.get[Option[String]]("superGroupList"),
                            chromList = parser.get[Option[List[String]]]("chromList"),
                            addCounts = ! parser.get[Boolean]("noCts"),
                            addFreq   = ! parser.get[Boolean]("noFrq"),
                            addMiss   = ! parser.get[Boolean]("noMiss"),
                            addAlle   = ! parser.get[Boolean]("noAlle"),
                            addHetHom = ! parser.get[Boolean]("noGeno"),
                            noMultiAllelics = parser.get[Boolean]("noMultiAllelics"),
                            GTTag = parser.get[String]("GTTag")
                          ).walkVCFFile(
                              parser.get[String]("invcf"), 
                              parser.get[String]("outvcf"),
                              chromList = parser.get[Option[List[String]]]("chromList"),
                              numLinesRead = parser.get[Option[Int]]("numLinesRead")
                          );
       }
       /*runAddGroupInfoAnno( parser.get[String]("invcf"),
                            parser.get[String]("outvcf"),
                            groupFile = parser.get[Option[String]]("groupFile"),
                            groupList = parser.get[Option[String]]("groupList"),
                            superGroupList = parser.get[Option[String]]("superGroupList"),
                            chromList = parser.get[Option[List[String]]]("chromList"),
                            addCounts = ! parser.get[Boolean]("noCts"),
                            addFreq   = ! parser.get[Boolean]("noFrq"),
                            addMiss   = ! parser.get[Boolean]("noMiss"),
                            addAlle   = ! parser.get[Boolean]("noAlle"),
                            addHetHom = ! parser.get[Boolean]("noGeno")                            
           )*/
     }
  }
  }
  
  /*def runAddGroupInfoAnno(infile : String, outfile : String, groupFile : Option[String], groupList : Option[String], superGroupList  : Option[String],
                          chromList : Option[List[String]], 
                          addCounts : Boolean = true, addFreq : Boolean = true, addMiss : Boolean = true, 
                          addAlle : Boolean= true, addHetHom : Boolean = true, 
                          sepRef : Boolean = true, countMissing : Boolean = true,
                          vcfCodes : VCFAnnoCodes = VCFAnnoCodes()){
     
    val (vcIter,vcfHeader) = internalUtils.VcfTool.getVcfIterator(infile, 
                                       chromList = chromList,
                                       vcfCodes = vcfCodes);
        
    val (vcIter2, newHeader) = AddGroupInfoAnno(groupFile=groupFile,groupList=groupList,superGroupList=superGroupList,chromList=chromList,
                                                addCounts = addCounts, addFreq=addFreq, addMiss=addMiss,
                                                addAlle=addAlle, addHetHom=addHetHom, sepRef=sepRef,
                                                vcfCodes=vcfCodes).walkVCF(vcIter,vcfHeader);
    
    val vcfWriter = internalUtils.VcfTool.getVcfWriter(outfile, header = newHeader);
    
    vcIter2.foreach(vc => {
      vcfWriter.add(vc)
    })
    vcfWriter.close();
  }*/
  
  

  /*
  case class SAddGroupInfoAnno(groupFile : Option[String], groupList : Option[String], superGroupList  : Option[String], chromList : Option[List[String]], 
             addCounts : Boolean = true, addFreq : Boolean = true, addMiss : Boolean = true, 
             addAlle : Boolean= true, addHetHom : Boolean = true, 
             sepRef : Boolean = true, countMissing : Boolean = true,
             noMultiAllelics : Boolean = false,
             vcfCodes : VCFAnnoCodes = VCFAnnoCodes()) extends internalUtils.VcfTool.SVcfWalker {
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
  
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
  
      val sampNames = vcfHeader.getSampleNamesInOrder().asScala.toVector;
      val groups = groupSet.toVector.sorted;
  
      reportln("Final Groups:","debug");
      for((g,i) <- groups.zipWithIndex){
        reportln("Group "+g+" ("+groupToSampleMap(g).size+")","debug");
      }
      
      //addCounts : Boolean = true, addFreq : Boolean = true, addAlle, addHetHom : Boolean = true, sepRef : Boolean = true,
      val forEachString = "for each possible allele (including the ref)"; //if(sepRef) "for each possible ALT allele (NOT including the ref)" else "for each possible allele (including the ref)";
      val countingString = if(countMissing) " counting uncalled alleles." else " not counting uncalled alleles."
      val newHeaderLines = groups.map(g => {
        (if(addCounts && addAlle)   List(new VCFInfoHeaderLine(vcfCodes.grpAC_TAG + g, VCFHeaderLineCount.R, VCFHeaderLineType.Integer, "For the "+groupToSampleMap(g).size+" samples in group "+g+", the number of alleles called "+forEachString+","+countingString)) else List[VCFHeaderLine]()) ++
        (if(addFreq   && addAlle)   List(new VCFInfoHeaderLine(vcfCodes.grpAF_TAG + g, VCFHeaderLineCount.A, VCFHeaderLineType.Float, "For the "+groupToSampleMap(g).size+" samples in group "+g+", the proportion of alleles called "+"for each alt allele (not including ref)"+","+countingString)) else List[VCFHeaderLine]()) ++
        (if(addCounts && addHetHom) List(new VCFInfoHeaderLine(vcfCodes.grpHomCt_TAG + g, VCFHeaderLineCount.R, VCFHeaderLineType.Integer, "For the "+groupToSampleMap(g).size+" samples in group "+g+", the number of homozygous genotypes called "+forEachString+","+countingString   )) else List[VCFHeaderLine]()) ++
        (if(addCounts && addHetHom) List(new VCFInfoHeaderLine(vcfCodes.grpHetCt_TAG + g, VCFHeaderLineCount.R, VCFHeaderLineType.Integer, "For the "+groupToSampleMap(g).size+" samples in group "+g+", the number of heterozygous genotypes called "+forEachString+","+countingString )) else List[VCFHeaderLine]()) ++
        (if(addFreq   && addHetHom) List(new VCFInfoHeaderLine(vcfCodes.grpHomFrq_TAG + g, VCFHeaderLineCount.A, VCFHeaderLineType.Float, "For the "+groupToSampleMap(g).size+" samples in group "+g+", the fraction of homozygous genotypes called "+"for each alt allele (not including ref)"+","+countingString  )) else List[VCFHeaderLine]()) ++
        (if(addFreq   && addHetHom) List(new VCFInfoHeaderLine(vcfCodes.grpHetFrq_TAG + g, VCFHeaderLineCount.A, VCFHeaderLineType.Float, "For the "+groupToSampleMap(g).size+" samples in group "+g+", the fraction of heterozygous genotypes called "+"for each alt allele (not including ref)"+","+countingString)) else List[VCFHeaderLine]()) ++
        (if(addCounts && addMiss)   List(new VCFInfoHeaderLine(vcfCodes.grpMisCt_TAG + g,  1, VCFHeaderLineType.Integer, "For the "+groupToSampleMap(g).size+" samples in group "+g+", the number of alleles missing (non-called).")) else List[VCFHeaderLine]()) ++
        (if(addFreq   && addMiss)   List(new VCFInfoHeaderLine(vcfCodes.grpMisFrq_TAG + g, 1, VCFHeaderLineType.Float, "For the "+groupToSampleMap(g).size+" samples in group "+g+", the fraction of alleles missing (non-called).")) else List[VCFHeaderLine]()) ++
        (if(addCounts && addAlle)   List(new VCFInfoHeaderLine(vcfCodes.grpAltAC_TAG + g, VCFHeaderLineCount.A, VCFHeaderLineType.Integer, "For the "+groupToSampleMap(g).size+" samples in group "+g+", the number of alleles called for the alt allele(s)")) else List[VCFHeaderLine]()) ++
        (if(addCounts && addHetHom)   List(new VCFInfoHeaderLine(vcfCodes.grpAltHetCt_TAG + g, VCFHeaderLineCount.A, VCFHeaderLineType.Integer, "For the "+groupToSampleMap(g).size+" samples in group "+g+", the number of genotypes called as heterozygous for the alt allele(s)")) else List[VCFHeaderLine]()) ++
        (if(addCounts && addHetHom)   List(new VCFInfoHeaderLine(vcfCodes.grpAltHomCt_TAG + g, VCFHeaderLineCount.A, VCFHeaderLineType.Integer, "For the "+groupToSampleMap(g).size+" samples in group "+g+", the number of genotypes called as homozygous for the alt allele(s)")) else List[VCFHeaderLine]()) ++
        (if(addCounts && addHetHom)   List(new VCFInfoHeaderLine(vcfCodes.grpRefHomCt_TAG + g, 1, VCFHeaderLineType.Integer, "For the "+groupToSampleMap(g).size+" samples in group "+g+", the number of genotypes called as homozygous for the reference allele")) else List[VCFHeaderLine]()) ++
        (if(addCounts && addHetHom)   List(new VCFInfoHeaderLine(vcfCodes.grpRefHetCt_TAG + g, 1, VCFHeaderLineType.Integer, "For the "+groupToSampleMap(g).size+" samples in group "+g+", the number of genotypes called as heterozygous for the reference allele")) else List[VCFHeaderLine]()) ++
        (if(addCounts && addAlle)   List(new VCFInfoHeaderLine(vcfCodes.grpRefAC_TAG + g, 1, VCFHeaderLineType.Integer, "For the "+groupToSampleMap(g).size+" samples in group "+g+", the number of alleles called for the reference allele")) else List[VCFHeaderLine]()) ++
        List[VCFHeaderLine]()
      }).flatten.toList
      
      //        (if(addFreq && addAlle)     List(new VCFInfoHeaderLine(vcfCodes.grpRefAF_TAG + g, 1, VCFHeaderLineType.Float, "For the "+groupToSampleMap(g).size+" samples in group "+g+", the proportion of alleles called for the reference allele")) else List[VCFHeaderLine]()) ++

  
        //      grpMisCt_TAG : String = TOP_LEVEL_VCF_TAG+"MisCt_GRP_",
        //  grpMisFrq_TAG : String = TOP_LEVEL_VCF_TAG+"MisFrq_GRP_",
        //  grpRefAC_TAG : String = TOP_LEVEL_VCF_TAG+"RefAC_GRP_",
        //  grpRefAF_TAG : String = TOP_LEVEL_VCF_TAG+"RefAF_GRP_",
      
      val newHeader = internalUtils.VcfTool.addHeaderLines(vcfHeader,newHeaderLines);
      
      return (vcIter.map(vc => {
        var vb = new htsjdk.variant.variantcontext.VariantContextBuilder(vc);
        val gt = vc.getGenotypes().iterateInSampleNameOrder(sampNames.asJava).asScala.toVector.zip(sampNames);
        val alleles = internalUtils.VcfTool.getAllelesInOrder(vc).toVector;
        
        val groupAlleCts = alleles.map(alle => { groups.map(grp => {
            val sampSet = groupToSampleMap(grp);
            gt.foldLeft(0){case (soFar,(g,samp)) => {
              if(sampSet.contains(samp)){
                soFar + g.countAllele(alle);
              } else {
                soFar;
              }
            }}
          })
        });
        
        val groupMisCts = groups.map(grp => {
            val sampSet = groupToSampleMap(grp);
            gt.foldLeft(0){case (soFar,(g,samp)) => {
              if(sampSet.contains(samp)){
                soFar + g.countAllele(Allele.NO_CALL);
              } else {
                soFar;
              }
            }}
          })
        
        val groupHomCts = alleles.map(alle => { groups.map(grp => {
            val sampSet = groupToSampleMap(grp);
            gt.foldLeft(0){case (soFar,(g,samp)) => {
              if(sampSet.contains(samp) && g.countAllele(alle) == 2){
                soFar + 1;
              } else {
                soFar;
              }
            }}
          })
        });
        val groupHetCts = alleles.map(alle => { groups.map(grp => {
            val sampSet = groupToSampleMap(grp);
            gt.foldLeft(0){case (soFar,(g,samp)) => {
              if(sampSet.contains(samp) && g.countAllele(alle) == 1){
                soFar + 1;
              } else {
                soFar;
              }
            }}
          })
        });
        
  
        val groupGenoSums = groups.map(grp => groupToSampleMap(grp).size.toDouble )
        val groupAlleAllSums = groups.zipWithIndex.map{case (grp,gi) => {
            val sampSet = groupToSampleMap(grp);
            gt.foldLeft(0){case (soFar,(g,samp)) => {
              if(sampSet.contains(samp)){
                soFar + g.getPloidy();
              } else {
                soFar;
              }
            }}.toDouble
        }}
        val groupAlleSums = if(countMissing){
          groupAlleAllSums;
        } else {
          groups.zipWithIndex.map{case (grp,gi) => {
            groupAlleCts.map(_(gi)).sum.toDouble
          }}
        }
        
        val groupAlleAF = groupAlleCts.map{groupAC => {
          groupAC.zip(groupAlleSums).map{case (ct,sumCt) => ct.toDouble / sumCt};
        }}
        
        val groupHomFrq = groupHomCts.map{groupCt => {
          groupCt.zip(groupGenoSums).map{case (ct,sumCt) => { ct.toDouble / sumCt }}
          //groupCt.map(_.toDouble / sumCt);
        }}
        val groupHetFrq = groupHetCts.map{groupCt => {
          groupCt.zip(groupGenoSums).map{case (ct,sumCt) => { ct.toDouble / sumCt }}
        }}
        val groupMisFrq = groupMisCts.zip(groupAlleAllSums).map{case (ct,sumCt) => { ct.toDouble / sumCt }}
        
        for((g,i) <- groups.zipWithIndex){
          if(addCounts && addAlle) vb = vb.attribute(vcfCodes.grpAC_TAG + g, groupAlleCts.map(_(i)).toList.asJava);
          if(addCounts && addHetHom) vb = vb.attribute(vcfCodes.grpHomCt_TAG  + g, groupHomCts.map(_(i)).toList.asJava);
          if(addCounts && addHetHom) vb = vb.attribute(vcfCodes.grpHetCt_TAG  + g, groupHetCts.map(_(i)).toList.asJava);
          if(addCounts   && addMiss) vb = vb.attribute(vcfCodes.grpMisCt_TAG + g,  groupMisCts(i));
          if(addFreq     && addMiss) vb = vb.attribute(vcfCodes.grpMisFrq_TAG + g, groupMisFrq(i));
          
          if(noMultiAllelics){
            if(addFreq && addAlle) vb = vb.attribute(vcfCodes.grpAF_TAG + g, groupAlleAF.tail.head(i).toString());
            if(addFreq && addHetHom) vb = vb.attribute(vcfCodes.grpHomFrq_TAG + g, groupHomFrq.tail.head(i).toString());
            if(addFreq && addHetHom) vb = vb.attribute(vcfCodes.grpHetFrq_TAG + g, groupHetFrq.tail.head(i).toString());
            if(addCounts && addAlle)   vb = vb.attribute(vcfCodes.grpAltAC_TAG + g,    groupAlleCts.tail.head(i).toString());
            if(addCounts && addHetHom) vb = vb.attribute(vcfCodes.grpAltHetCt_TAG + g, groupHetCts.tail.head(i).toString());
            if(addCounts && addHetHom) vb = vb.attribute(vcfCodes.grpAltHomCt_TAG + g, groupHomCts.tail.head(i).toString());
          } else {
            if(addFreq && addAlle) vb = vb.attribute(vcfCodes.grpAF_TAG + g, groupAlleAF.tail.map(_(i)).toList.asJava);
            if(addFreq && addHetHom) vb = vb.attribute(vcfCodes.grpHomFrq_TAG + g, groupHomFrq.tail.map(_(i)).toList.asJava);
            if(addFreq && addHetHom) vb = vb.attribute(vcfCodes.grpHetFrq_TAG + g, groupHetFrq.tail.map(_(i)).toList.asJava);
            if(addCounts && addAlle)   vb = vb.attribute(vcfCodes.grpAltAC_TAG + g,    groupAlleCts.tail.map(_(i)).toList.asJava);
            if(addCounts && addHetHom) vb = vb.attribute(vcfCodes.grpAltHetCt_TAG + g, groupHetCts.tail.map(_(i)).toList.asJava);
            if(addCounts && addHetHom) vb = vb.attribute(vcfCodes.grpAltHomCt_TAG + g, groupHomCts.tail.map(_(i)).toList.asJava);
          }
          if(addCounts && addHetHom) vb = vb.attribute(vcfCodes.grpRefHomCt_TAG + g, groupHomCts.head(i).toString());
          if(addCounts && addHetHom) vb = vb.attribute(vcfCodes.grpRefHetCt_TAG + g, groupHetCts.head(i).toString());
          if(addCounts && addAlle) vb = vb.attribute(vcfCodes.grpRefAC_TAG + g, groupAlleCts.head(i).toString());
          /*
           (if(addCounts && addAlle)   List(new VCFInfoHeaderLine(vcfCodes.grpAltAC_TAG + g, VCFHeaderLineCount.A, VCFHeaderLineType.Integer, "For the "+groupToSampleMap(g).size+" samples in group "+g+", the number of alleles called for the alt allele(s)")) else List[VCFHeaderLine]()) ++
        (if(addCounts && addHetHom)   List(new VCFInfoHeaderLine(vcfCodes.grpAltHetCt_TAG + g, VCFHeaderLineCount.A, VCFHeaderLineType.Integer, "For the "+groupToSampleMap(g).size+" samples in group "+g+", the number of genotypes called as heterozygous for the alt allele(s)")) else List[VCFHeaderLine]()) ++
        (if(addCounts && addHetHom)   List(new VCFInfoHeaderLine(vcfCodes.grpAltHomCt_TAG + g, VCFHeaderLineCount.A, VCFHeaderLineType.Integer, "For the "+groupToSampleMap(g).size+" samples in group "+g+", the number of genotypes called as homozygous for the alt allele(s)")) else List[VCFHeaderLine]()) ++
        (if(addCounts && addHetHom)   List(new VCFInfoHeaderLine(vcfCodes.grpRefHomCt_TAG + g, 1, VCFHeaderLineType.Integer, "For the "+groupToSampleMap(g).size+" samples in group "+g+", the number of genotypes called as homozygous for the reference allele")) else List[VCFHeaderLine]()) ++
        (if(addCounts && addHetHom)   List(new VCFInfoHeaderLine(vcfCodes.grpRefHetCt_TAG + g, 1, VCFHeaderLineType.Integer, "For the "+groupToSampleMap(g).size+" samples in group "+g+", the number of genotypes called as heterozygous for the reference allele")) else List[VCFHeaderLine]()) ++
        (if(addCounts && addAlle)   List(new VCFInfoHeaderLine(vcfCodes.grpRefAC_TAG + g, 1, VCFHeaderLineType.Integer, "For the "+groupToSampleMap(g).size+" samples in group "+g+", the number of alleles called for the reference allele")) else List[VCFHeaderLine]()) ++
        (if(addCounts && addAlle)   List(new VCFInfoHeaderLine(vcfCodes.grpRefAF_TAG + g, 1, VCFHeaderLineType.Float, "For the "+groupToSampleMap(g).size+" samples in group "+g+", the proportion of alleles called for the reference allele")) else List[VCFHeaderLine]()) ++
        
           */
          
        }
        
        vb.make();
      }),newHeader);
    }
  }*/
  
  
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  

  //hc_GT_FIX, fb_GT_FIX, ug_GT_FIX
  //hc_AD, ug_AD
  //
  
  case class SnpSiftAnnotater(cmdId : String,snpSiftAnnoCmd : String, tagPrefix : String = "") extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "snpSiftAnnotate"+cmdId
    def walkerParams : Seq[(String,String)] = Seq[(String,String)](
        ("cmdId",cmdId.toString)
    )

    val args = snpSiftAnnoCmd.split("\\s+");
    notice("Attempting SnpSift annotation with cmdId="+cmdId+"\n   "+args.mkString("\n   ")+"\n","SNPSIFT_ANNOTATE",-1);
    
    val infoList = args.zipWithIndex.find{ case (arg,idx) => { arg == "-info"}}.flatMap{ case (arg,idx) => {
      args.lift(idx+1).map{_.split(",")}
    }}.getOrElse(Array[String]());
    reportln("    SnpSiftAnno: found infoList: [\""+infoList.padTo(1,".").mkString("\",\"")+"\"]","debug");

    val namePrefix = args.zipWithIndex.find{ case (arg,idx) => { arg == "-name"}}.flatMap{ case (arg,idx) => {
      args.lift(idx+1)
    }}.getOrElse("");
    reportln("    SnpSiftAnno: found namePrefix: \""+namePrefix+"\"","debug");
    
    //val ssa = new org.snpsift.SnpSiftCmdAnnotate()
    //ssa.parseArgs(args)
    //ssa.init()
    val finalArgs : Array[String] = Array[String]("annotate") ++ args ++ Array[String]("dummyvar")
    val ss = new org.snpsift.SnpSift(finalArgs);
    val ssa = ss.cmd();
    
    //HACK HACK HACK:
    //      SnpSiftCmdAnnotate requires an instance of its special entry iterator class.
    val (dummyIter,snpSiftHeader) = SVcfVariantLine.getDummyIterator();
    ssa.annotateInit(dummyIter);
    
    //HACK HACK HACK: 
    //      Force SnpSift to process the header so I can access the header info.
    //      Can't directly invoke the parseHeader commands because they're all private or protected methods.
    val initilizerExampleVariant = new org.snpeff.vcf.VcfEntry( dummyIter,  "chr???\t100\t.\tA\tC\t20\t.\tTESTSNPEFFDUMMYVAR=BLAH;", 1, true);
    ssa.annotate(initilizerExampleVariant)
    
    reportln("    SnpSiftAnno: snpSiftHeader has the following info fields: [\""+dummyIter.getVcfHeader().getVcfHeaderInfo().asScala.map{h => h.getId()}.toVector.padTo(1,".").mkString("\",\"")+"\"]","debug");
    
    val newHeaderLines : Seq[(String,String,SVcfCompoundHeaderLine)] = infoList.toSeq.map{ tag => {
      dummyIter.getVcfHeader().getVcfHeaderInfo().asScala.find{ ssline => ssline.getId() == namePrefix + tag }.map{ ssline => {
        val numRaw = ssline.getVcfInfoNumber().toString().padTo(1,'0');
        val num = if(numRaw == "A") "1" else numRaw;
        val desc = ssline.getDescription()
        val ty = ssline.getVcfInfoType().name;
        val id = ssline.getId;
        val outTag = tagPrefix + namePrefix + id;
        (namePrefix + tag,outTag,new SVcfCompoundHeaderLine(in_tag="INFO",ID = outTag,Number = num, Type = ty, desc = desc).
                                   addWalker(this).addExtraField("source","SnpSift.via.vArmyKnife").
                                   addExtraField("SnpSiftVer",org.snpsift.SnpSift.VERSION_SHORT).
                                   addExtraField("SnpEffVer",org.snpeff.SnpEff.VERSION_SHORT))
      }}.getOrElse(
        (namePrefix + tag,tagPrefix + namePrefix + tag,
            new SVcfCompoundHeaderLine(in_tag="INFO",ID = tagPrefix + namePrefix + tag,Number = ".", Type = "String", desc = "Annotation tag "+tag+" from "+cmdId).
                                   addWalker(this).addExtraField("source","SnpSift.via.vArmyKnife").
                                   addExtraField("SnpSiftVer",org.snpsift.SnpSift.VERSION_SHORT).
                                   addExtraField("SnpEffVer",org.snpeff.SnpEff.VERSION_SHORT))
      );
    }}
    reportln("    SnpSiftAnno: extracting infolines: [\""+newHeaderLines.map{_._1}.padTo(1,".").mkString("\",\"")+"\"]","debug");
    reportln("    SnpSiftAnno: adding infolines: [\""+newHeaderLines.map{_._2}.padTo(1,".").mkString("\",\"")+"\"]","debug");

    
    /*
    val newHeaderLines : Seq[(String,String,SVcfCompoundHeaderLine)] = snpSiftHeader.getVcfHeaderInfo().asScala.map{ssline => {
      val num = ssline.getVcfInfoNumber().toString().padTo(1,'0');
      val desc = ssline.getDescription()
      val ty = ssline.getVcfInfoType().name;
      val id = ssline.getId;
      val outTag = tagPrefix + id;
      (id,outTag,new SVcfCompoundHeaderLine(in_tag="INFO",ID = tagPrefix + id,Number = num, Type = ty, desc = desc).
                                 addWalker(this).addExtraField("source","SnpSift.via.vArmyKnife").
                                 addExtraField("SnpSiftVer",org.snpsift.SnpSift.VERSION_SHORT).
                                 addExtraField("SnpEffVer",org.snpeff.SnpEff.VERSION_SHORT))
    }}.filter{ case (tag,outTag,hl) => tag != SVcfVariantLine.dummyHeaderTag }.toSeq
      */
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val newHeader = vcfHeader.copyHeader;
      newHeaderLines.foreach{ case (tag,outTag,hl) => {
        newHeader.addInfoLine(hl);
      }}
      var neverAddedAnno = true;
      (vcMap(vcIter){ v => {
        val vb = v.getOutputLine();
        val ve = vb.makeSnpeffVariantEntry(dummyIter);
        ssa.annotate(ve);
        var addedAnno = false;
        newHeaderLines.foreach{ case (tag,outTag,hl) => {
          Option(ve.getInfo(tag)).foreach{ v => {
            addedAnno = true;
            vb.addInfo(tag,v);
          }}
        }}
        if(addedAnno) notice("Found variant with SnpSift."+cmdId+" annotation match.","SNPSIFT_ANNOTATE_"+cmdId,1);
        //Need to find a way to pull VCF header info!
        vb
      }},newHeader);
    }


  }
  

  case class SnpSiftDbnsfp(cmdId : String,snpSiftAnnoCmd : String, tagPrefix : String = "") extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "snpSiftDbnsfp"+cmdId
    def walkerParams : Seq[(String,String)] = Seq[(String,String)](
        ("cmdId",cmdId.toString)
    )

    val args = snpSiftAnnoCmd.split("\\s+");
    notice("Attempting SnpSiftDbnsfp annotation with cmdId="+cmdId+"\n   "+args.mkString("\n   ")+"\n","SNPSIFT_ANNOTATE",-1);
    
    val infoList = args.zipWithIndex.find{ case (arg,idx) => { arg == "-f"}}.flatMap{ case (arg,idx) => {
      args.lift(idx+1).map{_.split(",")}
    }}.getOrElse(Array[String]());
    reportln("    SnpSiftDbnsfp: found infoList: [\""+infoList.padTo(1,".").mkString("\",\"")+"\"]","debug");

    /*val namePrefix = args.zipWithIndex.find{ case (arg,idx) => { arg == "-name"}}.flatMap{ case (arg,idx) => {
      args.lift(idx+1)
    }}.getOrElse("");*/
    val namePrefix = "dbNSFP_"
    reportln("    SnpSiftDbnsfp: found namePrefix: \""+namePrefix+"\"","debug");
    
    val finalArgs : Array[String] = Array[String]("DbNsfp") ++ args ++ Array[String]("dummyvar")
    val ss = new org.snpsift.SnpSift(finalArgs);
    val ssa = ss.cmd();
    
    
    //val ssa = new org.snpsift.SnpSiftCmdAnnotate()
    //ssa.parseArgs(args)
    //ssa.init()
    val (dummyIter,snpSiftHeader) = SVcfVariantLine.getDummyIterator();
    ssa.annotateInit(dummyIter);
    //HACK HACK HACK: 
    //      Force SnpSift to process the header so I can access the header info.
    //      Can't directly invoke the parseHeader commands because they're all private or protected methods.
    val initilizerExampleVariant = new org.snpeff.vcf.VcfEntry( dummyIter,  "chr???\t100\t.\tA\tC\t20\t.\tTESTSNPEFFDUMMYVAR=BLAH;", 1, true);
    ssa.annotate(initilizerExampleVariant)
    
    reportln("    SnpSiftAnno: snpSiftHeader has the following info fields: [\""+dummyIter.getVcfHeader().getVcfHeaderInfo().asScala.map{h => h.getId()}.toVector.padTo(1,".").mkString("\",\"")+"\"]","debug");
    
    val newHeaderLines : Seq[(String,String,SVcfCompoundHeaderLine)] = infoList.toSeq.map{ tag => {
      dummyIter.getVcfHeader().getVcfHeaderInfo().asScala.find{ ssline => ssline.getId() == namePrefix + tag }.map{ ssline => {
        val numRaw = ssline.getVcfInfoNumber().toString().padTo(1,'0');
        val num = if(numRaw == "A") "1" else numRaw;
        val desc = ssline.getDescription()
        val ty = ssline.getVcfInfoType().name;
        val id = ssline.getId;
        val outTag = tagPrefix + namePrefix + id;
        (namePrefix + tag,outTag,new SVcfCompoundHeaderLine(in_tag="INFO",ID = outTag,Number = num, Type = ty, desc = desc).
                                   addWalker(this).addExtraField("source","SnpSift.via.vArmyKnife").
                                   addExtraField("SnpSiftVer",org.snpsift.SnpSift.VERSION_SHORT).
                                   addExtraField("SnpEffVer",org.snpeff.SnpEff.VERSION_SHORT))
      }}.getOrElse(
        (namePrefix + tag,tagPrefix + namePrefix + tag,
            new SVcfCompoundHeaderLine(in_tag="INFO",ID = tagPrefix + namePrefix + tag,Number = ".", Type = "String", desc = "Annotation tag "+tag+" from "+cmdId).
                                   addWalker(this).addExtraField("source","SnpSift.via.vArmyKnife").
                                   addExtraField("SnpSiftVer",org.snpsift.SnpSift.VERSION_SHORT).
                                   addExtraField("SnpEffVer",org.snpeff.SnpEff.VERSION_SHORT))
      );
    }}
    reportln("    SnpSiftAnno: extracting infolines: [\""+newHeaderLines.map{_._1}.padTo(1,".").mkString("\",\"")+"\"]","debug");
    reportln("    SnpSiftAnno: adding infolines: [\""+newHeaderLines.map{_._2}.padTo(1,".").mkString("\",\"")+"\"]","debug");

    
    /*
    val newHeaderLines : Seq[(String,String,SVcfCompoundHeaderLine)] = snpSiftHeader.getVcfHeaderInfo().asScala.map{ssline => {
      val num = ssline.getVcfInfoNumber().toString().padTo(1,'0');
      val desc = ssline.getDescription()
      val ty = ssline.getVcfInfoType().name;
      val id = ssline.getId;
      val outTag = tagPrefix + id;
      (id,outTag,new SVcfCompoundHeaderLine(in_tag="INFO",ID = tagPrefix + id,Number = num, Type = ty, desc = desc).
                                 addWalker(this).addExtraField("source","SnpSift.via.vArmyKnife").
                                 addExtraField("SnpSiftVer",org.snpsift.SnpSift.VERSION_SHORT).
                                 addExtraField("SnpEffVer",org.snpeff.SnpEff.VERSION_SHORT))
    }}.filter{ case (tag,outTag,hl) => tag != SVcfVariantLine.dummyHeaderTag }.toSeq
      */
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val newHeader = vcfHeader.copyHeader;
      newHeaderLines.foreach{ case (tag,outTag,hl) => {
        newHeader.addInfoLine(hl);
      }}
      var neverAddedAnno = true;
      (vcMap(vcIter){ v => {
        val vb = v.getOutputLine();
        val ve = vb.makeSnpeffVariantEntry(dummyIter);
        var isAnno = ssa.annotate(ve);
        var addedAnno = false;
        newHeaderLines.foreach{ case (tag,outTag,hl) => {
          Option(ve.getInfo(tag)).foreach{ v => {
            addedAnno = true;
            vb.addInfo(tag,v);
          }}
        }}
        if(isAnno)    notice("SnpSift.annotate = true. "+cmdId+" annotation match.","snpSiftDbnsfp."+cmdId,1);
        if(addedAnno) notice("Found variant with SnpSift."+cmdId+" annotation match.","snpSiftDbnsfp."+cmdId,1);
        //Need to find a way to pull VCF header info!
        vb
      }},newHeader);
    }


  }
  
  val multiAllelicIndexStream = Iterator.from(0).toStream.map(i => {
    Range(0,i).flatMap(k => Range(k,i).map(z => (k,z)))
  }) 
  val NUMREPORTBADLEN=5;
  case class SSplitMultiAllelics(vcfCodes : VCFAnnoCodes = VCFAnnoCodes(), 
                                 clinVarVariants : Boolean = false, 
                                 fmtKeySumIntAlts : Seq[String] = Seq[String]("AD","AO"),
                                 fmtKeyIgnoreAlts : Seq[String] = Seq[String](),
                                 fmtKeyGenotypeStyle : Seq[String] = Seq[String]("GT"),
                                 singleCallerAlleFieldId : Option[List[String]] = None,
                                 singleCallerFmtKeySumIntAlts : List[String] = List[String](),
                                 singleCallerAlleFixPrefix : String = "SA_",
                                 splitSimple : Boolean = false,
                                 rawPrefix : Option[String] = Some("UNSPLIT_"),
                                 forceSplit : Boolean = false
                               ) extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "SSplitMultiAllelics"
    def walkerParams : Seq[(String,String)] = Seq[(String,String)](
        ("clinVarVariants",clinVarVariants.toString),
        ("fmtKeySumIntAlts",fmtList(fmtKeySumIntAlts)),
        ("fmtKeyIgnoreAlts",fmtList(fmtKeyIgnoreAlts)),
        ("fmtKeyGenotypeStyle",fmtList(fmtKeyGenotypeStyle)),
        ("singleCallerAlleFieldId",fmtOptionList(singleCallerAlleFieldId)),
        ("singleCallerFmtKeySumIntAlts",fmtList(singleCallerFmtKeySumIntAlts)),
        ("singleCallerAlleFixPrefix",singleCallerAlleFixPrefix),
        ("splitSimple",splitSimple.toString),
        ("rawPrefix",rawPrefix.toString)
    )
    val infoCLN = if(clinVarVariants){
        Set[String]("CLNHGVS","CLNSRC","CLNORIGIN","CLNSRCID","CLNSIG","CLNDSDB","CLNDSDBID","CLNREVSTAT","CLNACC","CLNDBN");
    } else {
        Set[String]();
    }
    
    val alleCtCt = new scala.collection.mutable.HashMap[Int, Int]().withDefaultValue(0)
    val badCtCt = new scala.collection.mutable.HashMap[Int, Int]().withDefaultValue(0)
    
    val singleCallerFmtKeySumIntAltsCells = singleCallerFmtKeySumIntAlts.map{_.split("\\|").toVector}.toVector;
    
    
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      
      if( vcfHeader.infoLines.contains(vcfCodes.splitIdx_TAG) || vcfHeader.isSplitMA ){
        warning("Multiallelics appear to have already been split in this file!","ALLELES_ALREADY_SPLIT",-1);
      }
      if(vcfHeader.walkLines.exists(walkline => walkline.ID == "SSplitMultiAllelics") || vcfHeader.isSplitMA ){
          warning("Splitmultiallelic walker found in this VCF file's header. Skipping multiallelic split!","ALLELES_ALREADY_SPLIT",-1);
          //if(! forceSplit){
            return((vcIter,vcfHeader));
          //}
      }
       
      //if(vcfHeader.titleLine.sampleList.length > 0){
        vcfHeader.formatLines.find(fline => fline.ID == "GT").foreach{ fline => {
          val nl = new SVcfCompoundHeaderLine("FORMAT",fline.ID, "1","String", fline.desc, subType = Some(VcfTool.subtype_GtStyleUnsplit))
          reportln("Adding GtStyleUnsplit subtype to tag: "+fline.ID,"note");
          reportln("    fline:"+fline.getVcfString,"note");
          reportln("    nl:"+nl.getVcfString,"note");
          vcfHeader.addFormatLine(nl,walker=Some(this));
        }}
        vcfHeader.formatLines.find(fline => fline.ID == "AD").foreach{ fline => {
          val nl = new SVcfCompoundHeaderLine("FORMAT",fline.ID, "R","Integer", fline.desc, subType = Some(VcfTool.subtype_AlleleCountsUnsplit))
          reportln("Adding subtype_AlleleCountsUnsplit subtype to tag: "+fline.ID,"note");
          reportln("    fline:"+fline.getVcfString,"note");
          reportln("    nl:"+nl.getVcfString,"note");
          vcfHeader.addFormatLine(nl,walker=Some(this));
        }}
        vcfHeader.formatLines.find(fline => fline.ID == "AO").foreach{ fline => {
          val nl = new SVcfCompoundHeaderLine("FORMAT",fline.ID, "A","Integer", fline.desc, subType = Some(VcfTool.subtype_AlleleCountsUnsplit))
          reportln("Adding subtype_AlleleCountsUnsplit subtype to tag: "+fline.ID,"note");
          reportln("    fline:"+fline.getVcfString,"note");
          reportln("    nl:"+nl.getVcfString,"note");
          vcfHeader.addFormatLine(nl,walker=Some(this));
        }}
        fmtKeySumIntAlts.foreach{t => vcfHeader.formatLines.find(fline => fline.ID == t && fline.subType.isEmpty).foreach{ fline => {
          val nl = new SVcfCompoundHeaderLine("FORMAT",fline.ID, fline.Number,fline.Type, fline.desc, subType = Some(VcfTool.subtype_AlleleCountsUnsplit))
          reportln("Adding subtype_AlleleCountsUnsplit subtype to tag: "+fline.ID,"note");
          reportln("    fline:"+fline.getVcfString,"note");
          reportln("    nl:"+nl.getVcfString,"note");
          vcfHeader.addFormatLine(nl,walker=Some(this));
        }}}
        fmtKeyGenotypeStyle.foreach{t => vcfHeader.formatLines.find(fline => fline.ID == t && fline.subType.isEmpty).foreach{ fline => {
          val nl = new SVcfCompoundHeaderLine("FORMAT",fline.ID, fline.Number,fline.Type, fline.desc, subType = Some(VcfTool.subtype_GtStyleUnsplit))
          reportln("Adding GtStyleUnsplit subtype to tag: "+fline.ID,"note");
          reportln("    fline:"+fline.getVcfString,"note");
          reportln("    nl:"+nl.getVcfString,"note");
          vcfHeader.addFormatLine(nl,walker=Some(this));
        }}}
      
      val newHeader =vcfHeader.copyHeader

      newHeader.addStatBool("isSplitMultAlle",true)
      newHeader.addStatBool("isSplitMultAlleStar",true)
      
      val newHeaderLines = List[SVcfCompoundHeaderLine](
          //new VCFInfoHeaderLine(vcfCodes.isSplitMulti_TAG, 0, VCFHeaderLineType.Flag,    "Indicates that this line was split apart from a multiallelic VCF line."),
          new SVcfCompoundHeaderLine("INFO",vcfCodes.splitIdx_TAG, "1", "Integer", "Indicates the index of this biallelic line in the set of biallelic lines extracted from the same multiallelic VCF line."),
          new SVcfCompoundHeaderLine("INFO",vcfCodes.numSplit_TAG,     "1", "Integer", "Indicates the number of biallelic lines extracted from the same multiallelic VCF line as this line."),
          new SVcfCompoundHeaderLine("INFO",vcfCodes.splitAlle_TAG,     ".", "String", "The original set of alternative alleles."),
           new SVcfCompoundHeaderLine("INFO",vcfCodes.splitAlleWARN_TAG,     ".", "String", "Warnings produced by the multiallelic allele split. These may occur when certain tags have the wrong number of values.")
      ) ++ (if(clinVarVariants){
        List[SVcfCompoundHeaderLine](
            new SVcfCompoundHeaderLine("INFO","CLNPROBLEM",     "1", "Integer", "Indicates whether the splitting of the Clinvar CLN tags was successful. 1=yes, 0=no.")
        ) ++ infoCLN.map(key => {
            new SVcfCompoundHeaderLine("INFO","ORIG"+key,     ".", "String", "")
        })
      } else {List[SVcfCompoundHeaderLine]()}) ++ (
          singleCallerAlleFieldId match {
            case Some(scafi) => {
              scafi.toSeq.zip(singleCallerFmtKeySumIntAlts).map{ case (alleField,adField) => {
                val adFieldHeader = vcfHeader.infoLines.filter{ i => i.ID == adField }
                if(adFieldHeader.length != 1) error("Error in header: could not find unique INFO ID = "+adField);
                val desc = adFieldHeader.head.desc;
                new SVcfCompoundHeaderLine("INFO",singleCallerAlleFixPrefix+adField, ".", "Integer", "(After allele splitting) "+desc);
              }}
            }
            case None => Seq();
          }
      )
        
      val gtSplitLines : Seq[(String,String,SVcfCompoundHeaderLine,SVcfCompoundHeaderLine)] = vcfHeader.formatLines.flatMap{ fline => {
        fline.subType match {
          case Some(st) => {
            if(st == VcfTool.subtype_GtStyleUnsplit){
              reportln("Adding new GT-Style FORMAT tag: "+fline.ID+"_presplit, based on "+fline.ID+": "+fline.getVcfString,"note");
              val ol = new SVcfCompoundHeaderLine("FORMAT",ID=fline.ID, Number=fline.Number, Type=fline.Type, desc="(Recoded for multiallelic split) "+fline.desc, subType = Some(VcfTool.subtype_GtStyle))
              val nl = new SVcfCompoundHeaderLine("FORMAT",ID=fline.ID + "_presplit",  Number="1", Type="String", desc="(Recoded for multiallelic split) "+fline.desc, subType = Some(VcfTool.subtype_GtStyleUnsplit))
              reportln("    ol:"+ol.getVcfString,"note");
              reportln("    nl:"+nl.getVcfString,"note");
              newHeader.addFormatLine(ol,walker=Some(this));
              newHeader.addFormatLine(nl,walker=Some(this));
              Some((fline.ID,fline.ID + "_presplit",ol,nl));
            } else {
              None
            }
          }
          case None => {
            None
          }
        }
      }}
      val adSplitLines : Seq[(String,String,SVcfCompoundHeaderLine,SVcfCompoundHeaderLine)] = vcfHeader.formatLines.flatMap{ fline => {
        fline.subType match {
          case Some(st) => {
            if(st == VcfTool.subtype_AlleleCountsUnsplit){
              if(fline.Number != "R" && fline.Number != "A"){
                warning("Warning: Tag with subtype: subtype_AlleleCountsUnsplit must have Number R or A","MalformedFormatTag",10)
              }
              reportln("Adding new Allele-Count FORMAT tag: "+fline.ID+"_presplit, based on "+fline.ID+": "+fline.getVcfString,"note");
              val ol = new SVcfCompoundHeaderLine("FORMAT",ID=fline.ID,Number= fline.Number, Type=fline.Type, desc="(Recoded for multiallelic split) "+fline.desc, subType = Some(VcfTool.subtype_AlleleCounts))
              val nl = new SVcfCompoundHeaderLine("FORMAT",ID=fline.ID + "_presplit", Number=".", Type=fline.Type, desc="(Raw value prior to multiallelic split) "+fline.desc, subType = Some(VcfTool.subtype_AlleleCountsUnsplit))
              reportln("    ol:"+ol.getVcfString,"note");
              reportln("    nl:"+nl.getVcfString,"note");
              newHeader.addFormatLine(ol,walker=Some(this));
              newHeader.addFormatLine(nl,walker=Some(this));
              Some((fline.ID,fline.ID + "_presplit",ol,nl));
            } else {
              None
            }
          }
          case None => {
            None
          }
        }
      }}
      //}
      
      newHeaderLines.foreach{hl => {
        newHeader.addInfoLine(hl,walker=Some(this));
      }}
      
      /*gtSplitLines.foreach{ case (oldID,newID,hl) => {
        newHeader.addFormatLine(hl,walker=Some(this));
      }}
      adSplitLines.foreach{ case (oldID,newID,hl) => {
        newHeader.addFormatLine(hl,walker=Some(this));
      }}*/
      
      newHeader.addWalk(this);
      
      /*
      rawPrefix.toSeq.flatMap{rawpre => (fmtKeyGenotypeStyle ++ fmtKeySumIntAlts).flatMap{gtTag => {
             vcfHeader.formatLines.find{ fl => fl.ID == gtTag }.map{ fl => {
               new SVcfCompoundHeaderLine("FORMAT",rawpre+fl.ID, ".", "String", "(Raw, before allele splitting) "+fl.desc) 
             }}
           }}
      }.toSeq.foreach{ hl => {
        newHeader.addFormatLine(hl,walker=Some(this));
      }}*/
      
      val infoA = newHeader.infoLines.withFilter(_.Number == "A").map(_.ID).toSet
      val infoR = newHeader.infoLines.withFilter(hl => hl.Number == "R").map(_.ID).toSet
      
      val infoRrev = newHeader.infoLines.withFilter(hl => hl.Number == "R" && hl.subType.getOrElse("") == "refAlleLast").map(_.ID).toSet
      val infoAtruncate = newHeader.infoLines.withFilter(hl => hl.Number == "A" && hl.subType.getOrElse("") == "truncateOtherAlts").map(_.ID).toSet
      
      val tagWarningMap = new scala.collection.mutable.AnyRefMap[String,Int](tag => 0);
      /*
      infoCLN.foreach(x => {
        reportln("INFO Line "+x+" is of type CLN","debug");
      })
      infoA.foreach(x => {
        reportln("INFO Line "+x+" is of type A","debug");
      })
      infoR.foreach(x => {
        reportln("INFO Line "+x+" is of type R","debug");
      })*/
      
      val sampNames = vcfHeader.titleLine.sampleList;
      return (addIteratorCloseAction(vcFlatMap(vcIter)(vc => {
        var warningSet = Set[String]();
        val vb = vc.getOutputLine();
        if(vc.alt.length <= 1 && (! splitSimple)){
          vb.addInfo(vcfCodes.numSplit_TAG, ""+vc.alt.length);
          vb.addInfo(vcfCodes.splitIdx_TAG, "0");
          vb.addInfo(vcfCodes.splitAlle_TAG, vc.alt.padTo(1,".").mkString(","));
          alleCtCt(vc.alt.length) = alleCtCt(vc.alt.length) + 1;
          
          vb.info.withFilter{case (t,v) => infoAtruncate.contains(t)}.map{ case (t,v) => {
            v.foreach{ vv => {
              val vvcells = vv.split(",");
              if(vvcells.length > 1){
                notice("Truncating overlong A-Numbered INFO field (in biallelic variant): "+t+". Len="+vvcells.length+", numAltAlle=1","TRUNCATING_ANUMBERED_INFO_BIALLE",10);
                vb.addInfo(t,vvcells.head);
              }
            }}
          }}
          
          if(vc.genotypes.genotypeValues.length > 0){
            adSplitLines.foreach{ case (oldTag,newTag,rawHeaderLine,headerLine) => {
                val fIdx = vc.genotypes.fmt.indexOf(oldTag);
                if(fIdx != -1){
                  vb.genotypes.addGenotypeArray(newTag,vc.genotypes.genotypeValues(fIdx).clone());
                }
            }}
            gtSplitLines.foreach{ case (oldTag,newTag,rawHeaderLine,headerLine) => {
                val fIdx = vc.genotypes.fmt.indexOf(oldTag);
                if(fIdx != -1){
                  vb.genotypes.addGenotypeArray(newTag,vc.genotypes.genotypeValues(fIdx).clone());
                }
            }}
          }
          Seq(vb);
        } else {
          val alleles = vc.alleles;
          val numAlle = alleles.length;
          val refAlle = alleles.head;
          val altAlleles = alleles.tail;
          alleCtCt(altAlleles.length) = alleCtCt(altAlleles.length) + 1;
          val attrMap = vc.info
          val (copyAttrA,tempAttrSet)    = attrMap.partition{case (key,obj) => { infoA.contains(key) }};
          val (copyAttrCLNraw,tempAttrSet2)    = tempAttrSet.partition{case (key,obj) => { infoCLN.contains(key) }};
          val (copyAttrR,simpleCopyAttr) = tempAttrSet2.partition{case (key,obj) => { infoR.contains(key) }};
          val copyAttrCLN = copyAttrCLNraw.map{case (key,a) => (key,a.getOrElse(""))};
          //infoRrev, infoAtruncate
          val attrA = copyAttrA.map{case (key,attr) => {
            val a = {
              val atr = if(attr.isEmpty) Array[String]() else attr.getOrElse("").split(",",-1);
              if(atr.length > numAlle - 1 && infoAtruncate.contains(key)){
                notice("Truncating overlong A-Numbered INFO field: "+key+". Len="+atr.length+", numAltAlle="+(numAlle-1),"TRUNCATING_ANUMBERED_INFO",10);
                atr.take(numAlle - 1).toSeq;
              } else if(atr.length != numAlle - 1){
                  warning("WARNING: ATTR: \""+key+"\"=\""+attr+"\" of type \"A\"\n   VAR:\n      atr.length() = "+atr.length + " numAlle = "+numAlle+
                      (if(internalUtils.optionHolder.OPTION_DEBUGMODE) "\n   "+vc.getVcfStringNoGenotypes else ""),
                      "INFO_LENGTH_WRONG_"+key,NUMREPORTBADLEN);
                  warningSet = warningSet + ("INFO_LENGTH_WRONG_A."+key)
                  tagWarningMap(key) += 1; 
                  repToSeq(atr.mkString(","),numAlle - 1);
              } else {
                atr.toSeq;
              }
            }
            (key,a)
          }}
          val attrR = copyAttrR.map{case (key,attr) => {
            val a = {
              val atr = if(attr.isEmpty) Array[String]() else attr.getOrElse("").split(",",-1);
              if(atr.length != numAlle){
                warning("WARNING: ATTR: \""+key+"\"=\""+attr+"\" of type \"R\"\n   VAR:\n      atr.length() = "+atr.length + " numAlle = "+numAlle+
                       (if(internalUtils.optionHolder.OPTION_DEBUGMODE) "\n   "+vc.getVcfStringNoGenotypes else ""),
                       "INFO_LENGTH_WRONG_"+key,NUMREPORTBADLEN);
                warningSet = warningSet + ("INFO_LENGTH_WRONG_R."+key)
                tagWarningMap(key) += 1;
                repToSeq(atr.mkString(","),numAlle);
              } else {
                atr.toSeq;
              }
            }
            (key,a)
          }}

          //var attrOut = simpleCopyAttr ++ attrA ++ attrR

          val ANNSTR =  trimBrackets(attrMap.getOrElse("ANN",Some(".")).getOrElse(".")).split(",",-1).map(ann => { (ann.trim.split("\\|",-1)(0),ann.trim) });
          
          //warning("ANNSTR: \n      "+ANNSTR.map{case (a,b) => { "(\""+a+"\",\""+b+"\")" }}.mkString("\n      ")+"","ANN_FIELD_TEST",100);
          
          val ANN = if(attrMap.contains("ANN")) Some(altAlleles.map(aa => {
             ANNSTR.filter{case (ahead,ann) => { ahead == aa }}.map{case (ahead,ann) => {ann}}.mkString(",");
          })) else None;
          
          var isBad = false;
          
          val out = altAlleles.zipWithIndex.map{ case (alt,altIdx) => {
            
            val gs = internalUtils.VcfTool.SVcfGenotypeSet(vc.genotypes.fmt,vc.genotypes.genotypeValues.map{g => g.clone()})
            val alleIdx = altIdx + 1;
            val alleIdxString = alleIdx.toString;
            
            val vb = internalUtils.VcfTool.SVcfOutputVariantLine(in_chrom = vc.chrom,
              in_pos  = vc.pos,
              in_id = vc.id,
              in_ref = vc.ref,
              in_alt = Seq[String](alt,internalUtils.VcfTool.UNKNOWN_ALT_TAG_STRING),
              in_qual = vc.qual,
              in_filter = vc.filter,
              in_info = simpleCopyAttr,
              in_format = vc.format,
              in_genotypes = gs
            )
            

            //vb.log10PError(vc.getLog10PError());
            //val alleleSet = if(alt != Allele.SPAN_DEL) List(refAlle,alt,Allele.SPAN_DEL) else List(refAlle,alt);
            //vb.alleles(alleleSet.asJava);
            //vb.attribute(vcfCodes.splitAlle_TAG, altAlleles.map(a => {a.getBaseString()}).mkString(",") )
            //vb.attribute(vcfCodes.numSplit_TAG, altAlleles.length.toString );
            //vb.attribute(vcfCodes.splitIdx_TAG, altIdx.toString );
            vb.addInfo(vcfCodes.splitAlle_TAG, altAlleles.mkString(",") );
            vb.addInfo(vcfCodes.numSplit_TAG, altAlleles.length.toString );
            vb.addInfo(vcfCodes.splitIdx_TAG, altIdx.toString);
            
            var malformedADCT = 0;
            
            if(vc.genotypes.genotypeValues.length > 0){
              
              //gs.addGenotypeArray(fmtid , gval : Array[String])
              gtSplitLines.foreach{ case (oldID,newID,ohl,hl) => {
                val fIdx = gs.fmt.indexOf(oldID);
                if(fIdx != -1){
                  gs.addGenotypeArray(newID,gs.genotypeValues(fIdx).clone());
                  gs.addGenotypeArray(oldID,gs.genotypeValues(fIdx).map{g => {
                    if(g.contains('.')){
                      g
                    } else {
                      g.split("[\\|/]").map{gg => if(gg == "0") "0" else if(gg == alleIdxString) "1" else "2"}.sorted.mkString("/")
                    }
                  }})
                }
              }}
              adSplitLines.foreach{ case (oldID,newID,ohl,hl) => {
                val fIdx = gs.fmt.indexOf(oldID);
                if(fIdx != -1){
                  gs.addGenotypeArray(newID,gs.genotypeValues(fIdx).clone());
                  if(ohl.Number == "R"){
                    gs.addGenotypeArray(oldID,gs.genotypeValues(fIdx).map{adString => {
                      if(adString == "."){
                        "."
                      } else {
                        val ad = adString.split(",").map{ aa => if(aa == ".") 0 else string2int(aa) }
                        if(! ad.isDefinedAt(alleIdx)){
                          warning("Attempting to split "+oldID+" tag failed! Offending String: "+adString+" for variant:\n     "+vc.getSimpleVcfString(),"BADSECONDARYADTAG_SPLITMULTALLE",10);
                          "."
                        } else {
                          ad(0) + "," + ad(alleIdx) + "," + (ad.sum - ad(0) - ad(alleIdx));
                        }
                        //  g.split("[\\|/]").map{gg => if(gg == "0") "0" else if(gg == alleIdxString) "1" else "2"}.sorted.mkString("/")
                      }
                    }})
                  } else if(ohl.Number == "A"){
                    gs.addGenotypeArray(newID,gs.genotypeValues(fIdx).map{adString => {
                      if(adString == "."){
                        "."
                      } else {
                        val ad = adString.split(",").map{ aa => if(aa == ".") 0 else string2int(aa) }
                        if(! ad.isDefinedAt(alleIdx-1)){
                          warning("Attempting to split "+oldID+" tag failed! Offending String: "+adString+" for variant:\n     "+vc.getSimpleVcfString(),"BADSECONDARYADTAG_SPLITMULTALLE",10);
                          "."
                        } else {
                          ad(alleIdx-1) + "," + (ad.sum - ad(alleIdx-1));
                        }
                      }

                      //  g.split("[\\|/]").map{gg => if(gg == "0") "0" else if(gg == alleIdxString) "1" else "2"}.sorted.mkString("/")
                    }})
                  } else {
                    warning("Attempting to split "+oldID+" tag failed!","IMPOSSIBLESTATE_BADSECONDARYADTAG_HASBADTYPE_SPLITMULTALLE",10);
                  }
                }
              }}
              
              /*
              fmtKeyGenotypeStyle.foreach{ gttag => {
                val gtidx = vb.genotypes.fmt.indexOf(gttag);
                if( gtidx != -1 ){
                  rawPrefix match {
                    case Some(p) => { vb.genotypes.addGenotypeArray(p+gttag,vb.genotypes.genotypeValues(gtidx).clone()); }
                    case None => {}
                  }
                  vb.genotypes.genotypeValues(gtidx).indices.foreach{ i => {
                    val gtraw = vb.genotypes.genotypeValues(gtidx)(i);
                    if(gtraw != "."){
                      val gt = gtraw.split("/|\\|");
                      val newgt = gt.map{ g => {
                        if(g == "." || g == "0"){
                          g
                        } else if(g == alleIdxString){
                          "1"
                        } else {
                          "2"
                        }
                      }}.sorted.mkString("/");
                      vb.genotypes.genotypeValues(gtidx)(i) = newgt;
                    }
                  }}
                }
              }}
              
              fmtKeySumIntAlts.foreach{ gttag => {
                val gtidx = vb.genotypes.fmt.indexOf(gttag);
                if(gtidx != -1){
                  rawPrefix match {
                    case Some(p) => { vb.genotypes.addGenotypeArray(p+gttag,vb.genotypes.genotypeValues(gtidx).clone()); }
                    case None => {}
                  }
                  var badFmtTag = false;
                  vb.genotypes.genotypeValues(gtidx).indices.foreach{ i => {
                    val gtraw = vb.genotypes.genotypeValues(gtidx)(i);
                    if(gtraw != "."){
                      val gtcells = gtraw.split(",").map{string2int(_)};
                      
                      if(gtcells.length != numAlle){
                        warning("Bad "+gttag+" Tag! AlleleCt="+numAlle+", gtcells.length="+gtcells.length+", alleles=\""+alleles.mkString(",")+"\", gtcells=\""+gtraw+"\"","BAD_"+gttag+"_FMT_TAG",1);
                        vb.genotypes.genotypeValues(gtidx)(i) = ".";
                        badFmtTag = true;
                      } else {
                        val gtout = Array.fill[Int](3)(0);
                        gtout(0) = gtcells(0);
                        gtout(1) = gtcells(alleIdx);
                        gtout(2) = gtcells.drop(alleIdx).tail.sum
                      }
                    }
                  }}
                  if(badFmtTag) warning("Line has bad "+gttag+" Tag!\n    LINE="+vc.getSimpleVcfString(),"BAD_"+gttag+"_FMT_TAG_LINE",10);
                }
              }}*/
              
              /*
               fmtKeySumAlts.foreach{ gttag => {
              
                val gtidx = vb.genotypes.fmt.indexOf(gttag);
                if(gtidx != -1){
                  rawPrefix match {
                    case Some(p) => { vb.genotypes.addGenotypeArray(p+gttag,vb.genotypes.genotypeValues(gtidx).clone()); }
                    case None => {}
                  }
                  vb.genotypes.genotypeValues(gtidx).indices.foreach{ i => {
                    val gtraw = vb.genotypes.genotypeValues(gtidx)(i);
                    
                  }}
                }
              }}*/
            }
            
            if(clinVarVariants){
              vb.addInfo("CLNPROBLEM","0");
              copyAttrCLN.foreach{case(key,attr) => {
                    vb.addInfo("ORIG"+key,attr);
                 }}
              val clnAlleList = vc.info.getOrElse("CLNALLE",None).getOrElse("").split(",").map(x => string2int(x.toString()))
              val clnIdx = clnAlleList.indexWhere(x => x == alleIdx);
              if(clnIdx == -1){
                  copyAttrCLN.foreach{case(key,attr) => {
                    vb.addInfo(key,"NA");
                  }}
                  warning("CLINVAR CLNALLE TAG IS MALFORMED: CLNALLE="+vc.info.getOrElse("CLNALLE",None).getOrElse("???")+" does not contain altAlleIdx = "+alleIdx + " (nAlle="+altAlleles.length.toString+")\n"+
                          "       CLNSIG=\""+vc.info.getOrElse("CLNSIG",None).getOrElse("???")+"\"",
                          "Malformed_ClinVar_CLNALLE_TAG",25);
                  isBad = true;
                  
                  vb.addInfo("CLNPROBLEM","1");
              } else {
                //val attrCLN = copyAttrCLN.map{case (key,attr) => {
                copyAttrCLN.foreach{case(key,attr) => {
                  val attrCells = attr.split(",",-1);
                  if(clnIdx >= attrCells.length){
                      //warning("CLINVAR "+key+" TAG IS MALFORMED: \""+attr+"\" does not contain clnIdx = "+clnIdx+" (altAlleIdx="+alleIdx+", CLNALLE="+vc.info.getOrElse("CLNALLE",None).getOrElse("???")+")","Malformed_ClinVar_TAG",25);
                      vb.addInfo("CLNPROBLEM","1");
                      isBad = true;
                  } else {
                    vb.addInfo(key,attrCells(clnIdx));
                  }
                  if(clnAlleList.length != attrCells.length){
                      //warning("CLINVAR "+key+" TAG IS MALFORMED: \""+attr+"\" does not have the same length as CLNALLE="+clnAlleList.mkString(",")+" (altAlleIdx="+alleIdx+", CLNALLE="+vc.info.getOrElse("CLNALLE",None).getOrElse("???")+")","Malformed_ClinVar_TAG",25);
                      isBad = true;
                  }
                }}
                //}}
              }

            }
            attrA.foreach{case (key,attrArray) => {
              vb.addInfo(key,attrArray(altIdx));
            }}//truncateOtherAlts
            attrR.foreach{case (key,attrArray) => {
              if(infoRrev.contains(key)){
                vb.addInfo(key,attrArray(alleIdx) + "," + attrArray.last);
              } else {
                vb.addInfo(key,attrArray(0) + "," + attrArray(alleIdx));
              }
              
            }}

            ANN match {
              case Some(annVector) => {
                vb.addInfo("ANN",annVector(altIdx));
              }
              case None => {
                //do nothing
              }
            }
            /*
          singleCallerAlleFieldId match {
            case Some(scafi) => {
              scafi.toSeq.zip(singleCallerFmtKeySumIntAlts).foreach{ case (alleField,adField) => {
                val allesOpt = vb.info.getOrElse(alleField,None);
                val adsOpt = vb.genotypes.getGtTag(adField);
                (allesOpt,adsOpt) match {
                  case (Some(alles),Some(ads)) => {
                    val alleIdx = alles.indexOf(alt);
                    val adsCells = ads.map{a => a.split(",")}
                    val adRef = adsCells.head;
                    if(alleIdx == -1){
                      val outStrings = adsCells.map{ ac => {
                        if(ac.forall(a => a == ".")){
                          "."
                        } else if(ac.length != alles.length){
                          warning("Marformed AD-type string (alleIdx=-1) (" +adField+") alt="+alt+", alles=["+alles.mkString(",")+"], ac=["+ac.mkString(",")+"]","MALFORMED_ADTYPE_FIELD_"+adField,10);
                          "."
                        } else {
                          val altSum = ac.tail.zipWithIndex.map{ case (a,i) => {
                            if(a == ".") 0;
                            else string2int(a)
                          }}.sum
                          ac(0)+",.,"+altSum;
                        }
                      }}
                      vb.genotypes.addGenotypeArray(singleCallerAlleFixPrefix+adField,outStrings);
                    } else if(alles.length == 1){
                      vb.genotypes.addGenotypeArray(singleCallerAlleFixPrefix+adField,ads.clone())
                      //if(adsCells.length == 2){
                      //  vb.genotypes.addGenotypeArray(singleCallerAlleFixPrefix+adField,ads.clone());
                      //} else {
                      //  warning("Marformed AD-type string (" +adField+") alt="+alt+", alles=["+alles.mkString(",")+"], 
                      //  vb.genotypes.addGenotypeArray(singleCallerAlleFixPrefix+adField,adsCells.map{ac => "."});
                     // }
                    } else {
                      val outStrings = adsCells.map{ ac => { 
                        if(ac.forall(a => a == ".")){
                          "."
                        } else if(ac.length != alles.length){
                          warning("Marformed AD-type string (" +adField+") alt="+alt+", alles=["+alles.mkString(",")+"], ac=["+ac.mkString(",")+"]","MALFORMED_ADTYPE_FIELD_"+adField,10);
                          "."
                        } else {
                          val altSum = ac.tail.zipWithIndex.map{ case (a,i) => {
                            if(a == ".") 0;
                            else if(i == alleIdx) 0;
                            else string2int(a)
                          }}.sum
                          if( ac.isDefinedAt(alleIdx+1) ){
                            ac(0)+","+ac(alleIdx+1)+","+altSum
                          } else {
                            warning("Marformed AD-type string (" +adField+") alt="+alt+", alles=["+alles.mkString(",")+"], ac=["+ac.mkString(",")+"]","MALFORMED_ADTYPE_FIELD_"+adField,10);
                            "."
                          }
                        }

                      }}
                      vb.genotypes.addGenotypeArray(singleCallerAlleFixPrefix+adField,outStrings);
                    }
                  }
                  case _ => {
                    //do nothing
                  }
                }
              }}
            }
            case None => {
              // do nothing
            }
          }*/
            
            vb.addInfo(vcfCodes.splitAlleWARN_TAG, warningSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
            
            vb
          }}
          if(isBad){
            badCtCt(altAlleles.length) = badCtCt(altAlleles.length) + 1;
          }
          out;
        }
      }), closeAction = () => {
        if(alleCtCt.size > 0){
        Range(1,alleCtCt.keys.max+1).foreach{k => {
          reportln("altCt("+k+"): "+ alleCtCt(k) + " VCF lines","debug");
        }}}
        if(badCtCt.size > 0){
        Range(1,badCtCt.keys.max+1).foreach{k => {
          reportln("badAltCt("+k+"): "+ badCtCt(k) + " VCF lines","debug");
        }}}
        tagWarningMap.toVector.sortBy{ case (tag,ct) => tag }.foreach{ case (tag,ct) => {
          reportln("WARNING_PROBLEMATIC_TAG\t"+tag+"\t"+ct,"debug");
        }}
        
      }),newHeader)
    }
  }
  
  
  
  //AA,AB,BB,AC,BC,CC
  //0,60,900
  
  class CmdRecodeClinVarCLN extends CommandLineRunUtil {
     override def priority = 100;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "RecodeClinVarCLN", 
          quickSynopsis = "", 
          synopsis = "", 
          description = " " + ALPHA_WARNING,   
          argList = 
                    new BinaryOptionArgument[List[String]](
                                         name = "chromList", 
                                         arg = List("--chromList"), 
                                         valueName = "chr1,chr2,...",  
                                         argDesc =  "..."
                                        ) ::                     
                    new FinalArgument[String](
                                         name = "invcf",
                                         valueName = "variants.vcf",
                                         argDesc = "infput VCF file" // description
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
       
       VcfAnnotateTX.RecodeClinVarCLN(
                   chromList = parser.get[Option[List[String]]]("chromList")
                   ).walkVCFFile(
                   infile    = parser.get[String]("invcf"),
                   outfile   = parser.get[String]("outvcf"),
                   chromList = parser.get[Option[List[String]]]("chromList")
                   )
     }   
    }
  }
  
  case class RecodeClinVarCLN(
                 chromList : Option[List[String]],
                 vcfCodes : VCFAnnoCodes = VCFAnnoCodes()
                ) extends internalUtils.VcfTool.VCFWalker {
    
    reportln("Creating recodeClinVarCLN()","note")

    val infoCLN = Set[String]("CLNHGVS","CLNSRC","CLNORIGIN","CLNSRCID","CLNSIG","CLNDSDB","CLNDSDBID","CLNREVSTAT","CLNACC","CLNDBN");
    val infoCLNALL = infoCLN ++ Set[String]("CLNALLE");
    
    val alleCtCt = new scala.collection.mutable.HashMap[Int, Int]().withDefaultValue(0)
    val badCtCt = new scala.collection.mutable.HashMap[Int, Int]().withDefaultValue(0)
    
    def walkVCF(vcIter : Iterator[VariantContext], vcfHeader : VCFHeader, verbose : Boolean = true) : (Iterator[VariantContext],VCFHeader) = {
      val oldHeaderLines =  vcfHeader.getMetaDataInInputOrder().asScala.toList;
      
      val (keepOldLines,oldClnLines) = oldHeaderLines.partition(headerline => {
        headerline match {
          case h : VCFInfoHeaderLine => {
            ! infoCLNALL.contains(h.getID()); 
          }
          case _ => true;
        }
      })
      val oldClnHeader = oldClnLines.map(headerLine => {
        headerLine match {
          case h : VCFInfoHeaderLine => h;
          case _ => null;
        }
      })
       

      val newHeaderLines = oldClnHeader.map(h => {
            new VCFInfoHeaderLine(h.getID(), VCFHeaderLineCount.A, h.getType(), h.getDescription())
          }) ++ List(
            new VCFInfoHeaderLine("CLNERROR",     1, VCFHeaderLineType.String, "")
          ) ++ oldClnHeader.map(h => {
            new VCFInfoHeaderLine("ORIG"+h.getID(),     VCFHeaderLineCount.UNBOUNDED, h.getType(), h.getDescription() + " (ORIGINAL UNFIXED VALUE FROM CLINVAR)")
          })
      
      val replacementHeaderLines = keepOldLines ++ newHeaderLines;
          
      val newHeader = internalUtils.VcfTool.replaceHeaderLines(vcfHeader,replacementHeaderLines);
      
      reportln("Walking input VCF...","note")
      return ( 
      addIteratorCloseAction(vcIter.map(v => {
        runRecodeClinVarCLN(v);
      }), closeAction = () => {
        if(alleCtCt.size > 0){
        Range(1,alleCtCt.keys.max+1).foreach{k => {
          reportln("altCt("+k+"): "+ alleCtCt(k) + " VCF lines","debug");
        }}}
        if(badCtCt.size > 0){
        Range(1,badCtCt.keys.max+1).foreach{k => {
          reportln("badAltCt("+k+"): "+ badCtCt(k) + " VCF lines","debug");
        }}}
      }), newHeader );
    }
    
    def runRecodeClinVarCLN(v : VariantContext) : VariantContext = {
      var vb = new htsjdk.variant.variantcontext.VariantContextBuilder(v);
      vb = vb.attribute("CLNERROR","OK");
      var isErr = false;
      val clnAlle = v.getAttributeAsList("CLNALLE").asScala.map(x => string2int(x.toString())).toVector;
          val alleles = internalUtils.VcfTool.getAllelesInOrder(v).toVector;
          val numAlle = alleles.length;
          val refAlle = alleles.head;
          val altAlleles = alleles.tail;
          alleCtCt(altAlleles.length) = alleCtCt(altAlleles.length) + 1;
      val clnMap = Range(1,alleles.length).map(alleIdx => {
        clnAlle.indexWhere(clnAlleNum => {
          clnAlleNum == alleIdx;
        })
      }).toVector
      
      infoCLN.foreach(key => {
        val attr = v.getAttributeAsList(key).asScala.map(_.toString());
        vb = vb.attribute("ORIG"+key, attr.mkString(","));
        if(attr.length != clnAlle.length){
          isErr = true;
           vb = vb.attribute("CLNERROR","ERR");
        } else {
          val vcfAllelesNotFound = clnMap.count(_ == -1);
          val clnAllelesNotFound = clnAlle.count(i => { i == -1 });
          
          if(vcfAllelesNotFound > 0 && clnAllelesNotFound > 0){
              isErr = true;
              vb = vb.attribute("CLNERROR","ERR");
          }
          
          val newAttr = clnMap.map(clnIdx => {
            if(clnIdx == -1){
              //isErr = true;
              //vb.attribute("CLNERROR","ERR");
              "NA";
            } else {
              attr(clnIdx);
            }
          });
          vb = vb.attribute(key, newAttr.mkString(","));
        }
      })
      vb = vb.attribute("ORIGCLNALLE",v.getAttributeAsList("CLNALLE").asScala.map(_.toString()).mkString(","));
      vb = vb.attribute("CLNALLE",Range(0,altAlleles.length).map(x => (x + 1).toString()).mkString(","));
      if(isErr){
        badCtCt(altAlleles.length) = badCtCt(altAlleles.length) + 1;
      }
      
      return vb.make();
    }
    
    reportln("recodeClinVarCLN() Created...","note")
    
  }
  
  
  case class SAddSummaryCln(
                 vcfCodes : VCFAnnoCodes = VCFAnnoCodes()
                ) extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "SAddSummaryCln"
    def walkerParams : Seq[(String,String)] = Seq[(String,String)](

    )
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      
      val newHeaderLines = List(
            new SVcfCompoundHeaderLine("INFO",vcfCodes.CLNVAR_SUMSIG,     "1", "Integer", "Summary clinical significance level, from CLNSIG tag. Collapsed multiple reports into a single reported significance level.")      
      );
      
      val newHeader = vcfHeader.copyHeader;
      newHeaderLines.foreach{ hl => {
        newHeader.addInfoLine(hl);
      }}
      newHeader.addWalk(this);
      reportln("Walking input VCF...","note")

      return ((vcIter.map(vc => {
         var vb = vc.getOutputLine;
         //val clnsig = v.getAttributeAsList("CLNSIG").asScala.map(_.toString()).toSeq;
         //val clnsig = getAttributeAsStringList(vc,"CLNSIG")
         vc.info.getOrElse("CLNSIG",None) match {
           case Some(clnsig) => {
             if(clnsig != "."){
               val clnsigcells = clnsig.split(",")
               val cs = internalUtils.CalcACMGVar.getSummaryClinSig(clnsigcells);
               vb.addInfo(vcfCodes.CLNVAR_SUMSIG,cs + "");
             }
           }
           case None => {
             //do nothing
           }
         }
         vb
      }), newHeader));
      
    }
  }
  
  
  


  

  
  
  val DEFAULT_CMDADDCANONICALINFO_TAGLIST = DefaultVCFAnnoCodes.txList_TAG + ","+
                                               DefaultVCFAnnoCodes.vMutC_TAG + ","+
                                               DefaultVCFAnnoCodes.vMutP_TAG + ","+
                                               DefaultVCFAnnoCodes.vMutINFO_TAG + ","+
                                               DefaultVCFAnnoCodes.vType_TAG + ","+
                                               DefaultVCFAnnoCodes.vMutR_TAG + ","+
                                               DefaultVCFAnnoCodes.geneIDs;
  val DEFAULT_CMDADDCANONICALINFO_TXTAG = DefaultVCFAnnoCodes.txList_TAG
  
  
  case class SAddCanonicalInfo(canonicalTxFile : String, 
                               tagList : String = DEFAULT_CMDADDCANONICALINFO_TAGLIST, 
                               txTag : String = DEFAULT_CMDADDCANONICALINFO_TXTAG,
                               canonSuffix : String = "_CANON",
                               canonDescPrefix : String = "(For the canonical transcript(s) only) ") extends internalUtils.VcfTool.SVcfWalker {
    val tagSet = tagList.split(",").toSet;
    def walkerName : String = "SAddCanonicalInfo"
    def walkerParams : Seq[(String,String)] = Seq[(String,String)](
        ("canonicalTxFile",canonicalTxFile.toString),
        ("tagList","\""+tagList+"\""),
        ("txTag",txTag),
        ("canonSuffix","\""+canonSuffix+"\"")
    )
    
    val isCanon : (String => Boolean) = {
        val lines = getLinesSmartUnzip(canonicalTxFile);
        val table = getTableFromLines(lines,colNames = Seq("transcript"), errorName = "File "+canonicalTxFile);
        var refSeqSet : Set[String] = table.map(tableCells => {
          val tx = tableCells(0);
          tx
        }).toSet
        reportln("   found: "+refSeqSet.size+" Canonical transcripts.","debug");
        ((s : String) => {
          refSeqSet.contains(s);
        })
      }
    

    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      val oldHeaderLines = vcfHeader.infoLines.filter{ln => tagSet.contains(ln.ID)}; //vcfHeader.getInfoHeaderLines().asScala.filter(hl => tagSet.contains(hl.getID()));
      
      val addedHeaderLines = oldHeaderLines.map{ h => {
        if(h.Number == "A"){
          new SVcfCompoundHeaderLine(h.tag,h.ID + canonSuffix, "A", h.Type, canonDescPrefix+h.desc);
        } else if(h.Number == "."){
          new SVcfCompoundHeaderLine(h.tag,h.ID + canonSuffix, ".", h.Type, canonDescPrefix+h.desc);
        } else {
          error("FATAL ERROR: ");
          null;
        }
      }}
      val isByAllele = oldHeaderLines.map{ h => {
        if(h.Number == "A"){
          (h.ID,true)
        } else {
          (h.ID,false)
        }
      }}.filter(_._1 != txTag).toVector
      
      val newHeader = vcfHeader.copyHeader 
      //internalUtils.VcfTool.addHeaderLines(vcfHeader, addedHeaderLines.toVector);
      addedHeaderLines.foreach{ hl => {
        newHeader.addInfoLine(hl);
      }}
      newHeader.addWalk(this);
      
    val overwriteInfos : Set[String] = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect(newHeader.addedInfos );
      if( overwriteInfos.nonEmpty ){
        notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      }
    
      
      return (vcMap(vcIter){ vc => {
        val vv = vc.getOutputLine();
        vv.dropInfo(overwriteInfos)
        walkLine(vv,isByAllele=isByAllele,txTag=txTag);
      }}, newHeader);
    }
    
    def walkLine(vc : SVcfVariantLine, isByAllele : Vector[(String,Boolean)], txTag : String) : SVcfVariantLine = {
      var vb = vc.getOutputLine();
      
      vc.info.getOrElse(txTag,None) match {
        case Some(txListString) => {
          val txList = txListString.split(",");
          vb.addInfo(txTag+canonSuffix,txList.filter(isCanon(_)).padTo(1,".").mkString(","));
          if(txList.exists(tx => isCanon(tx))){
            isByAllele.foreach{ case (tag,isA) => {
              vc.info.getOrElse(tag,None) match {
                case Some(attrib) => {
                  val acells = attrib.split(",");
                  if(isA){
                    vb.addInfo(tag+"_CANON",acells.map{ astring => {
                      val asubcells = astring.split("\\|");
                      asubcells.zip(txList).filter{ case (a,tx) => isCanon(tx) }.map{_._1}.mkString("|")
                    }}.mkString(",") );
                  } else {
                    vb.addInfo(tag+canonSuffix,acells.zip(txList).filter{ case (a,tx) => isCanon(tx) }.map{_._1}.mkString(","));
                  }
                }
                case None => {
                  //do nothing
                }
              }

            }}
          }
        }
        case None => {
          //do nothing
        }
      }
      
      return vb
    }
  }
  
  class CmdSummarizeGenotypeStats extends CommandLineRunUtil {
     override def priority = 20;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "summarizeGenotypeStats", 
          quickSynopsis = "", 
          synopsis = "", 
          description = "" + ALPHA_WARNING,
          argList = 
                    new BinaryOptionArgument[Int](
                                         name = "numLinesRead", 
                                         arg = List("--numLinesRead"), 
                                         valueName = "N",  
                                         argDesc =  "Limit file read to the first N lines"
                                        ) ::
                    new BinaryOptionArgument[List[String]](
                                         name = "chromList", 
                                         arg = List("--chromList"), 
                                         valueName = "chr1,chr2,...",  
                                         argDesc =  "List of chromosomes. If supplied, then all analysis will be restricted to these chromosomes. All other chromosomes wil be ignored."
                                        ) ::
                    new FinalArgument[String](
                                         name = "infile",
                                         valueName = "variants.vcf",
                                         argDesc = "master input VCF file. Can be gzipped or in plaintext." // description
                                        ) ::
                    new FinalArgument[List[String]](
                                         name = "summarizeStatList",
                                         valueName = "stat1,stat2,...",
                                         argDesc = "" // description
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
         SummarizeGenotypeStats(
             summarizeStatList = parser.get[List[String]]("summarizeStatList")
         ).walkVCFFile( 
             infile = parser.get[String]("infile"),
             outfile = parser.get[String]("outfile"),
             chromList = parser.get[Option[List[String]]]("chromList"),
             numLinesRead = parser.get[Option[Int]]("numLinesRead")
         ) 
       }
     }
  }
  
  case class SummarizeGenotypeStats(summarizeStatList : List[String]) extends SVcfWalker {
    def walkerName : String = "SummarizeGenotypeStats"
    def walkerParams : Seq[(String,String)] = Seq[(String,String)](
        ("summarizeStatList","\""+summarizeStatList.toString+"\"")
    )
    val statCommandPairs = summarizeStatList.map(a => {
      val arr = a.split(":"); 
      (arr(0),arr(1))
    })
    
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      //vcfHeader.formatLines = vcfHeader.formatLines ++ Some(new SVcfCompoundHeaderLine(in_tag = "FORMAT",ID = filterTag, Number = "1", Type = "Integer", desc = "Equal to 1 if and only if the genotype was filtered due to post-caller quality filters. ("+filter+")"))
      //vcfHeader.addFormatLine(new SVcfCompoundHeaderLine(in_tag = "FORMAT",ID = filterTag, Number = "1", Type = "Integer", desc = "Equal to 1 if and only if the genotype was filtered due to post-caller quality filters. ("+filter+")") );
      //vcfHeader.addFormatLine(new SVcfCompoundHeaderLine(in_tag = "FORMAT",ID = rawGtTag, Number = "1", Type = "String", desc = "The original GT genotype, prior to genotype-level filtering.") );
      val outHeader = vcfHeader.copyHeader;
      outHeader.addWalk(this);
      val outIter = vcMap(vcIter){ vc => {
        vc
      }}
      
      return (outIter,outHeader);
    }
  }
  
  
  
  
  class CmdFilterGenotypesByStat extends CommandLineRunUtil {
     override def priority = 20;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "filterGenotypesByStat", 
          quickSynopsis = "", 
          synopsis = "", 
          description = "" + ALPHA_WARNING,
          argList = 
                    new BinaryOptionArgument[Int](
                                         name = "numLinesRead", 
                                         arg = List("--numLinesRead"), 
                                         valueName = "N",  
                                         argDesc =  "Limit file read to the first N lines"
                                        ) ::
                    new BinaryOptionArgument[List[String]](
                                         name = "chromList", 
                                         arg = List("--chromList"), 
                                         valueName = "chr1,chr2,...",  
                                         argDesc =  "List of chromosomes. If supplied, then all analysis will be restricted to these chromosomes. All other chromosomes wil be ignored."
                                        ) ::
                    new BinaryArgument[String](name = "GTTag",
                                            arg = List("--GTTag"),  
                                            valueName = "GT", 
                                            argDesc = "",
                                            defaultValue = Some("GT")
                                       ) :: 
                    new BinaryArgument[String](name = "newGTTag",
                                            arg = List("--newGTTag"),  
                                            valueName = "GT", 
                                            argDesc = "",
                                            defaultValue = Some("GT")
                                       ) :: 
                    new BinaryArgument[String](name = "oldGTTag",
                                            arg = List("--oldGTTag"),  
                                            valueName = "RAW_GT", 
                                            argDesc = "",
                                            defaultValue = Some("RAW_GT")
                                       ) :: 
                    new BinaryArgument[String](name = "filtTag",
                                            arg = List("--filtTag"),  
                                            valueName = "SWHGTFILT", 
                                            argDesc = "",
                                            defaultValue = Some("SWHGTFILT")
                                       ) :: 
                    new UnaryArgument( name = "noRawGT",
                                         arg = List("--noRawGT"), // name of value
                                         argDesc = ""+
                                                   "" // description
                                       ) ::
                    new FinalArgument[String](
                                         name = "infile",
                                         valueName = "variants.vcf",
                                         argDesc = "master input VCF file. Can be gzipped or in plaintext." // description
                                        ) ::
                    new FinalArgument[String](
                                         name = "filter",
                                         valueName = "filterExpr",
                                         argDesc = "" // description
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
         FilterGenotypesByStat(
             filter = parser.get[String]("filter"),
             filterTag = parser.get[String]("filtTag"),
             gtTag = parser.get[String]("GTTag"),
             rawGtTag = parser.get[String]("oldGTTag"),
             noRawGt = parser.get[Boolean]("noRawGT"),
             newGTTag = parser.get[String]("newGTTag")
         ).walkVCFFile(
             infile = parser.get[String]("infile"),
             outfile = parser.get[String]("outfile"),
             chromList = parser.get[Option[List[String]]]("chromList"),
             numLinesRead = parser.get[Option[Int]]("numLinesRead")
         )
       }
     }
  }
  case class FilterGenotypesByStat(filter : String, filterTag : String = "SWHGTFILT", gtTag : String, 
                                   rawGtTag : String, noRawGt : Boolean, newGTTag : String,
                                   groupFile : Option[String] = None, groupList : Option[String] = None, superGroupList  : Option[String] = None) extends SVcfWalker {
    def walkerName : String = "FilterGenotypesByStat"
    def walkerParams : Seq[(String,String)] = Seq[(String,String)](
        ("filter","\""+filter+"\""),
        ("filterTag",filterTag),
        ("gtTag",gtTag),
        ("rawGtTag",rawGtTag),
        ("noRawGt",noRawGt.toString),
        ("newGTTag",newGTTag)
    )
    
    /*
     groupFile : Option[String], groupList : Option[String], superGroupList  : Option[String]
      
     getGroups(groupFile : Option[String] = None, groupList : Option[String] = None, superGroupList : Option[String] = None) 
                        : (scala.collection.mutable.AnyRefMap[String,Set[String]],
                           scala.collection.mutable.AnyRefMap[String,Set[String]],
                           Vector[String])
    */
    val (sampleToGroupMap,groupToSampleMap,groups) : (scala.collection.mutable.AnyRefMap[String,Set[String]],
                           scala.collection.mutable.AnyRefMap[String,Set[String]],
                           Vector[String]) = getGroups(groupFile, groupList, superGroupList);
    
    
    
    val logicParser : internalUtils.VcfTool.SFilterLogicParser[(SVcfVariantLine,Int)] = internalUtils.VcfTool.sGenotypeFilterLogicParser;
    val filterLogic = logicParser.parseString(filter);
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      //vcfHeader.formatLines = vcfHeader.formatLines ++ Some(new SVcfCompoundHeaderLine(in_tag = "FORMAT",ID = filterTag, Number = "1", Type = "Integer", desc = "Equal to 1 if and only if the genotype was filtered due to post-caller quality filters. ("+filter+")"))
      val sampList = vcfHeader.titleLine.sampleList.toList
      val sampCt   = vcfHeader.sampleCt;
      val outHeader = vcfHeader.copyHeader;
      val filterLine = new SVcfCompoundHeaderLine(in_tag = "FORMAT",ID = filterTag, Number = "1", Type = "Integer", desc = "Equal to 1 if and only if the genotype was filtered due to post-caller quality filters. ("+filter+")") 
      val rawgtLine = new SVcfCompoundHeaderLine(in_tag = "FORMAT",ID = rawGtTag, Number = "1", Type = "String", desc = "The original GT genotype, prior to genotype-level filtering.",subType=Some(VcfTool.subtype_GtStyleUnsplit)) 
      outHeader.addFormatLine(filterLine, walker = Some(this));
      if(!noRawGt) outHeader.addFormatLine(rawgtLine);
      vcfHeader.formatLines.find( fline => fline.ID == newGTTag) match {
        case Some(fline) => {
          outHeader.addFormatLine(new SVcfCompoundHeaderLine(in_tag = "FORMAT",ID = newGTTag, Number = "1", Type = "String", desc = "(After filtering) "+fline.desc,subType=Some(VcfTool.subtype_GtStyleUnsplit)), walker = Some(this) );
        }
        case None => {
          outHeader.addFormatLine(new SVcfCompoundHeaderLine(in_tag = "FORMAT",ID = newGTTag, Number = "1", Type = "String", desc = "The genotype after filtering.",subType=Some(VcfTool.subtype_GtStyleUnsplit)), walker = Some(this) );
        }
      }
      outHeader.addWalk(this);
      val missingGeno = "./." // Range(0,ploidy).map{_ => "."}.mkString("/");
      
      val addRawGt = (! noRawGt) && (rawGtTag != gtTag);
      val rango = Range(0,vcfHeader.sampleCt);
      val outIter = vcMap(vcIter){ vc => {
        vc.genotypes.sampList = sampList;
        vc.genotypes.sampGrp = Some(sampleToGroupMap);
        val vb = vc.getOutputLine();
        //vb.genotypes.genotypeValues = vb.genotypes.genotypeValues :+ Array.fill[String](vb.genotypes.genotypeValues(0).length)("0");
        val gtIdx = vc.genotypes.fmt.indexOf(gtTag);
        val gtVals = vc.genotypes.genotypeValues(gtIdx)
        val newGtIdx = vb.genotypes.addGenotypeArray(newGTTag, gtVals.clone());
        
        //val ploidy = vb.genotypes.getPloidy();
        
        if( addRawGt ){
          vb.genotypes.addGenotypeArray( rawGtTag,gtVals.clone() );
        }
        val ftIdx = vb.genotypes.addGenotypeArrayIfNew(filterTag,Array.fill[String](sampCt)("0"));

        rango.withFilter{(i) => gtVals(i).head != '.' && (!filterLogic.keep((vc,i)))}.foreach{ (i) => {
            vb.genotypes.genotypeValues(newGtIdx)(i) = missingGeno;
            vb.genotypes.genotypeValues(ftIdx)(i) = "1";
        }}
        if(internalUtils.optionHolder.OPTION_DEBUGMODE){
          tally("NUM_GT_FILTERED",vb.genotypes.genotypeValues(ftIdx).count{x => x == "1"})
        }
        vb;
      }}
      
      return (outIter,outHeader);
    }
  }

  
    case class VariantCountSet(
        ctHet : Array[Long],
        ctRef : Array[Long],
        ctAlt : Array[Long],
        ctOthNoAlt : Array[Long],
        ctOthWithAlt : Array[Long],
        ctMis : Array[Long]){
      def addGT(sampGT : String, sampIdx : Int){
          if(sampGT == "./." || sampGT == "."){
            ctMis(sampIdx) = ctMis(sampIdx) + 1;
          } else if(sampGT == "0/0"){
            ctRef(sampIdx) = ctRef(sampIdx) + 1;
          } else if(sampGT == "0/1"){
            ctHet(sampIdx) = ctHet(sampIdx) + 1;
          } else if(sampGT == "1/1"){
            ctAlt(sampIdx) = ctAlt(sampIdx) + 1;
          } else if(sampGT.contains('1')){
            ctOthWithAlt(sampIdx) = ctOthWithAlt(sampIdx) + 1;
          } else {
            ctOthNoAlt(sampIdx) = ctOthNoAlt(sampIdx) + 1;
          }
      }
      def addVC(gt : Array[String]){
        Range(0,gt.length).foreach(idx => addGT(gt(idx),idx));
      }
      def getAll(idx : Int) : String = {
        ctRef(idx) + "\t"+ ctHet(idx) + "\t"+ctAlt(idx)+"\t"+ctOthWithAlt(idx)+"\t"+ctOthNoAlt(idx)
      }
      def getAlt(idx : Int) : String = {
        (ctHet(idx)+ctAlt(idx)+ctOthWithAlt(idx))+"\t"+ctHet(idx) + "\t"+ctAlt(idx)+"\t"+ctOthWithAlt(idx)
      }
      def getNcalled(idx : Int) : Long = {
        ctRef(idx) + ctHet(idx)+ctAlt(idx)+ctOthWithAlt(idx)+ctOthNoAlt(idx)
      }
    }
    
    def makeVariantCountSet(ct : Int) : VariantCountSet = {
      VariantCountSet(
          Array.fill[Long](ct)(0),
          Array.fill[Long](ct)(0),
          Array.fill[Long](ct)(0),
          Array.fill[Long](ct)(0),
          Array.fill[Long](ct)(0),
          Array.fill[Long](ct)(0)
      )
    }
  
  case class CalcVariantCountSummary(genotypeTag : String = "GT", keepAltChrom : Boolean = false, singletonGTfile : Option[String] = None,
                                     subFilterExpressionSets : Option[String] = None,
                                     bySwapCounts : Boolean = true) {
    

    
    //SNVVARIANT_BASESWAP_LIST = Seq( (("A","C"),("T","G")),...
    
    case class VariantCountSetSet(varCt : VariantCountSet,
                                  singCt : VariantCountSet,
                                  indelCt : VariantCountSet,
                                  indelSingCt : VariantCountSet,
                                  snvCt : VariantCountSet,
                                  snvSingCt : VariantCountSet,
                                  snvCtBySwap : Map[String,VariantCountSet],
                                  snvSingCtBySwap : Map[String,VariantCountSet]
                                 ){
      def addVariant(gt : Array[String],isSingleton : Boolean, isIndel : Boolean, ref : String, alt : String){
        varCt.addVC(gt);
        if(isSingleton){
          singCt.addVC(gt);
        }
        if(isIndel){
          indelCt.addVC(gt);
          if(isSingleton){
            indelSingCt.addVC(gt);
          }
        } else {
          snvCt.addVC(gt);
          snvCtBySwap(ref+alt).addVC(gt);
          if(isSingleton){
            snvSingCt.addVC(gt);
            snvSingCtBySwap(ref+alt).addVC(gt);
          }
        }
      }
    }
    def makeVariantCountSetSet(ct : Int) : VariantCountSetSet = {
      val snvCtBySwap_P1 : Map[String,VariantCountSet] = SNVVARIANT_BASESWAP_LIST.map{ case ((a1,b1),(a2,b2)) => (a1+b1,makeVariantCountSet(ct)) }.toMap
      val snvCtBySwap : Map[String,VariantCountSet] = snvCtBySwap_P1 ++ SNVVARIANT_BASESWAP_LIST.map{ case ((a1,b1),(a2,b2)) => (a2+b2,snvCtBySwap_P1(a1+b1)) }.toMap;
      val snvSingCtBySwap_P1 : Map[String,VariantCountSet] = SNVVARIANT_BASESWAP_LIST.map{ case ((a1,b1),(a2,b2)) => (a1+b1,makeVariantCountSet(ct)) }.toMap
      val snvSingCtBySwap : Map[String,VariantCountSet] = snvSingCtBySwap_P1 ++ SNVVARIANT_BASESWAP_LIST.map{ case ((a1,b1),(a2,b2)) => (a2+b2,snvSingCtBySwap_P1(a1+b1)) }.toMap;
      
      VariantCountSetSet(varCt = makeVariantCountSet(ct),
                                  singCt  = makeVariantCountSet(ct),
                                  indelCt  = makeVariantCountSet(ct), 
                                  indelSingCt  = makeVariantCountSet(ct),
                                  snvCt  = makeVariantCountSet(ct),
                                  snvSingCt  = makeVariantCountSet(ct),
                                  snvCtBySwap = snvCtBySwap,
                                  snvSingCtBySwap = snvSingCtBySwap
                                 )
    }
    
    val singletonWriter = singletonGTfile match {
      case Some(f) => {
        Some(openWriterSmart(f))
      }
      case None => None;
    }
    
    def writeSingGT(s : String){
      singletonWriter match {
        case None => {
          //do nothing
        }
        case Some(w) => {
          w.write(s);
        }
      }
    }
    
    def walkVCFFile(infile :String, inputFileList : Boolean, outfile : String, chromList : Option[List[String]], numLinesRead : Option[Int]){
      val (vcIter, vcfHeader) = getSVcfIterators(infile,chromList,numLinesRead,inputFileList);
      val sampleCt = vcfHeader.sampleCt;
      
      val varCt = makeVariantCountSet(sampleCt);
      val singCt = makeVariantCountSet(sampleCt);
      val indelCt = makeVariantCountSet(sampleCt);
      val indelSingCt = makeVariantCountSet(sampleCt);
      val snvCt = makeVariantCountSet(sampleCt);
      val snvSingCt = makeVariantCountSet(sampleCt);
      
      var numVariants = 0;
      var numSingletons = 0;
      var numIndel = 0;
      var numSnv = 0;
      var numFiltVar = 0;
      
      //val snvCtBySwap_P1 : Map[String,VariantCountSet] = SNVVARIANT_BASESWAP_LIST.map{ case ((a1,b1),(a2,b2)) => (a1+b1,makeVariantCountSet(sampleCt)) }.toMap
      //val snvCtBySwap : Map[String,VariantCountSet] = snvCtBySwap_P1 ++ SNVVARIANT_BASESWAP_LIST.map{ case ((a1,b1),(a2,b2)) => (a2+b2,snvCtBySwap_P1(a1+b1)) }.toMap;
      //val snvSingCtBySwap_P1 : Map[String,VariantCountSet] = SNVVARIANT_BASESWAP_LIST.map{ case ((a1,b1),(a2,b2)) => (a1+b1,makeVariantCountSet(sampleCt)) }.toMap
      //val snvSingCtBySwap : Map[String,VariantCountSet] = snvSingCtBySwap_P1 ++ SNVVARIANT_BASESWAP_LIST.map{ case ((a1,b1),(a2,b2)) => (a2+b2,snvSingCtBySwap_P1(a1+b1)) }.toMap;
      
      val subsetList : Seq[(String,SFilterLogic[SVcfVariantLine],VariantCountSetSet)] = subFilterExpressionSets match { 
        case Some(sfes) => {
          val sfesSeq = sfes.split(",")
          sfesSeq.map{ sfe =>
            val sfecells = sfe.split("=").map(_.trim());
            if(sfecells.length != 2) error("ERROR: subfilterExpression must have format: subfiltertitle=subfilterexpression");
            val (sfName,sfString) = (sfecells(0),sfecells(1));
            val parser : SFilterLogicParser[SVcfVariantLine] = internalUtils.VcfTool.sVcfFilterLogicParser;
            val filter : SFilterLogic[SVcfVariantLine] = parser.parseString(sfString);
            (sfName+"_",filter,makeVariantCountSetSet(sampleCt))
          }
        }
        case None => {
          Seq();
        }
      }
      val setList : Seq[(String,SFilterLogic[SVcfVariantLine],VariantCountSetSet)] = Seq(("",SFilterTrue[SVcfVariantLine](),makeVariantCountSetSet(sampleCt))) ++ subsetList;
         
        /*subfilterExpression match {
        case Some(filterExpr) => {
          val parser : SVcfFilterLogicParser = internalUtils.VcfTool.SVcfFilterLogicParser();
          val filter : SFilterLogic[SVcfVariantLine] = parser.parseString(filterExpr);
          Some((filter,makeVariantCountSetSet(sampleCt)))
        }
        case None => None;
      }*/
      
      vcIter.filter{vc => vc.genotypes.fmt.contains(genotypeTag) & vc.alt.length > 0}.foreach{vc => {
        if(vc.alt.length > 2) error("Fatal error: multiallelic variant! This utility is only intended to work after splitting multiallelic variants!")
        val alt : String = vc.alt.head;
        val ref : String = vc.ref;
        val gt : Array[String] = vc.getGt(genotypeTag);
        val numAlt = gt.count{ gt => gt.contains('1') }
        val isSingleton = (numAlt == 1);
        val isIndel = ref.length != 1 || alt.length != 1;
        
        numVariants = numVariants + 1;
          varCt.addVC(gt);
          
          if(isSingleton){
            numSingletons = numSingletons + 1;
            //singCt.addVC(gt);
          }
          if(isIndel){
            numIndel = numIndel + 1;
            //indelCt.addVC(gt);
            //if(isSingleton){
            //  indelSingCt.addVC(gt);
            //}
          } else {
            numSnv = numSnv + 1;
            //snvCt.addVC(gt);
            //if(isSingleton){
            //  snvSingCt.addVC(gt);
            //}
          }
          
        setList.foreach{ case (sfName,filter,countSetSet) => {
            if(filter.keep(vc)) {
              countSetSet.addVariant(gt=gt,isSingleton=isSingleton,isIndel=isIndel,ref=ref,alt=alt);
            }
        }}
      }}
      
      val allTitles : Seq[String] = Seq("HomRef","Het","HomAlt","OtherAlt","Other")
      val altTitles : Seq[String] = Seq("AnyAlt","Het","HomAlt","OtherAlt")
      val swapList : Seq[String] = SNVVARIANT_BASESWAP_LIST.map{ case ((a1,b1),(a2,b2)) => { a1+b1 }};
      
      val writer = openWriterSmart(outfile);
      writer.write("sample.ID\t"+
                   (setList.map{ case (sfName,filter,countSetSet) => { 
                        sfName+"numCalled\t"+
                        sfName+"numMiss\t"+
                        allTitles.map(t => sfName+"" + t).mkString("\t")+"\t"+
                        allTitles.map(t => sfName+"SNV_" + t).mkString("\t")+"\t"+
                        allTitles.map(t => sfName+"INDEL_" + t).mkString("\t")+"\t"+
                        altTitles.map(t => sfName+"SING_" + t).mkString("\t")+"\t"+
                        altTitles.map(t => sfName+"SINGSNV_" + t).mkString("\t")+"\t"+
                        altTitles.map(t => sfName+"SINGINDEL_" + t).mkString("\t")+"\t"+
                        (if(bySwapCounts){
                          swapList.map{ swap => { allTitles.map(t => sfName+"SNV."+swap+"_"+t).mkString("\t") }}.mkString("\t")+"\t"+
                          swapList.map{ swap => { altTitles.map(t => sfName+"SINGSNV."+swap+"_"+t).mkString("\t") }}.mkString("\t")
                        } else "")
                   }}.mkString("\t"))+
                   "\n")
      Range(0,sampleCt).foreach(i => {
        writer.write(
            vcfHeader.titleLine.sampleList(i) + "\t"+
        (subsetList.map{ case (sfName,filter,css) => { 
              (Seq(
                  css.varCt.getNcalled(i).toString(),
                  css.varCt.ctMis(i),
                  css.varCt.getAll(i),
                  css.snvCt.getAll(i),
                  css.indelCt.getAll(i),
                  css.singCt.getAlt(i),
                  css.snvSingCt.getAlt(i),
                  css.indelSingCt.getAlt(i)
                  ) ++ 
                  (if(bySwapCounts){Seq(                
                    swapList.map{ swap => { css.snvCtBySwap(swap).getAll(i) }}.mkString("\t"),
                    swapList.map{ swap => { css.snvSingCtBySwap(swap).getAlt(i) }}.mkString("\t")
                  )} else Seq())
              ).mkString("\t")
        }}.mkString("\t")) +
        "\n");
      })
      writer.close();
    }
  }

  class RunCalcVariantCountSummary extends CommandLineRunUtil {
     override def priority = 20;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "CalcVariantCountSummary", 
          quickSynopsis = "", 
          synopsis = "", 
          description = "" + ALPHA_WARNING,
          argList = 
                    new BinaryArgument[String](
                                         name = "genotypeTag", 
                                         arg = List("--genotypeTag"), 
                                         valueName = "GT",  
                                         argDesc =  ".",
                                         defaultValue = Some("GT")
                                        ) ::
                    new UnaryArgument( name = "inputFileList",
                                         arg = List("--inputFileList"), // name of value
                                         argDesc = ""+
                                                   "" // description
                                       ) ::
                    new BinaryOptionArgument[Int](
                                         name = "numLinesRead", 
                                         arg = List("--numLinesRead"), 
                                         valueName = "N",  
                                         argDesc =  "Limit file read to the first N lines"
                                        ) ::
                    new BinaryOptionArgument[List[String]](
                                         name = "chromList", 
                                         arg = List("--chromList"), 
                                         valueName = "chr1,chr2,...",  
                                         argDesc =  "List of chromosomes. If supplied, then all analysis will be restricted to these chromosomes. All other chromosomes wil be ignored."
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "subFilterExpressionSets", 
                                         arg = List("--subFilterExpressionSets"), 
                                         valueName = "",  
                                         argDesc =  ""
                                        ) ::
                    new FinalArgument[String](
                                         name = "infile",
                                         valueName = "variants.vcf",
                                         argDesc = "master input VCF file. Can be gzipped or in plaintext." // description
                                        ) ::
                    new FinalArgument[String](
                                         name = "outfile",
                                         valueName = "summaryInfo.txt",
                                         argDesc = "The output file. Can be gzipped or in plaintext."// description
                                        ) ::
                    internalUtils.commandLineUI.CLUI_UNIVERSAL_ARGS );

     def run(args : Array[String]) {
       val out = parser.parseArguments(args.toList.tail);
       if(out){
         CalcVariantCountSummary(
             genotypeTag = parser.get[String]("genotypeTag"),
             subFilterExpressionSets = parser.get[Option[String]]("subFilterExpressionSets")
         ).walkVCFFile(
             infile = parser.get[String]("infile"), inputFileList = parser.get[Boolean]("inputFileList"),
             outfile = parser.get[String]("outfile"),
             chromList = parser.get[Option[List[String]]]("chromList"),
             numLinesRead = parser.get[Option[Int]]("numLinesRead")
         )
       }
     }
     
  }
  
  class RedoEnsemblMerge extends CommandLineRunUtil {
     override def priority = 20;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "RedoEnsemblMerge", 
          quickSynopsis = "", 
          synopsis = "", 
          description = "" + ALPHA_WARNING,
          argList = 
                    new UnaryArgument( name = "singleDbFile",
                                         arg = List("--singleDbFile"), // name of value
                                         argDesc = "NOT CURRENTLY SUPPORTED"+
                                                   "" // description
                                       ) ::
                    new BinaryArgument[String](
                                         name = "chromStyle", 
                                         arg = List("--chromStyle"), 
                                         valueName = "hg19",  
                                         argDesc =  ".",
                                         defaultValue = Some("hg19")
                                        ) ::
                    new BinaryOptionArgument[Int](
                                         name = "numLinesRead", 
                                         arg = List("--numLinesRead"), 
                                         valueName = "N",  
                                         argDesc =  "Limit file read to the first N lines"
                                        ) ::
                    new BinaryOptionArgument[List[String]](
                                         name = "chromList", 
                                         arg = List("--chromList"), 
                                         valueName = "chr1,chr2,...",  
                                         argDesc =  "List of chromosomes. If supplied, then all analysis will be restricted to these chromosomes. All other chromosomes wil be ignored."
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "masterCaller", 
                                         arg = List("--masterCaller"), 
                                         valueName = "hc",  
                                         argDesc =  "A caller from which to import ALL info tags."
                                        ) :: 
                    new BinaryOptionArgument[String](
                                         name = "summaryFile", 
                                         arg = List("--summaryFile"), 
                                         valueName = "summaryData.txt",  
                                         argDesc =  ""
                                        ) :: 
                    new FinalArgument[String](
                                         name = "infile",
                                         valueName = "variants.vcf",
                                         argDesc = "master input VCF file. Can be gzipped or in plaintext." // description
                                        ) ::
                    new FinalArgument[List[String]](
                                         name = "scVcfFiles",
                                         valueName = "",
                                         argDesc = "" // description
                                        ) ::
                    new FinalArgument[List[String]](
                                         name = "scVcfNames",
                                         valueName = "",
                                         argDesc = "" // description
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
         FixEnsemblMerge2(
             inputVCFs = parser.get[List[String]]("scVcfFiles"),
             inputVcfTypes = parser.get[List[String]]("scVcfNames"),
             masterCaller = parser.get[Option[String]]("masterCaller"),
             summaryFile = parser.get[Option[String]]("summaryFile")
         ).walkVCFFile(
             infile = parser.get[String]("infile"),
             outfile = parser.get[String]("outfile"),
             chromList = parser.get[Option[List[String]]]("chromList"),
             numLinesRead = parser.get[Option[Int]]("numLinesRead")
         )
       }
     }
     
  }
  
  case class mergeSecondaryVcf(inputVCF : String, inputVcfTag : String, inputVcfName : String,
                               getTags : Option[List[String]] = None, dropTags : List[String] = List(),
                               tagPrefix : String = "SWH_",
                               failOnBadChrom : Boolean = false) extends SVcfWalker {
    def walkerName : String = "mergeSecondaryVcf_"+inputVcfTag;
    def walkerParams : Seq[(String,String)] = Seq[(String,String)](
        ("inputVCF","\""+inputVCF+"\""),
        ("inputVcfTag","\""+inputVcfTag+"\""),
        ("inputVcfName","\""+inputVcfName+"\""),
        ("getTags",fmtOptionList(getTags)),
        ("dropTags",fmtList(dropTags)),
        ("tagPrefix","\""+tagPrefix+"\"")
    )
    val (altHeader,altReaderRaw) = SVcfLine.readVcf(getLinesSmartUnzip(inputVCF), withProgress = false);
    var altReader : BufferedIterator[SVcfVariantLine] = altReaderRaw.buffered;
    var altChrom : String = altReader.head.chrom;
    var missingChrom : Set[String] = Set[String]();
    
    def getForPosition(chrom : String, pos : Int, loopBack : Boolean = true) : Seq[SVcfVariantLine] = {
      if(altChrom != chrom){
        if(missingChrom.contains(chrom)){
          return Seq[SVcfVariantLine]();
        } else {
          reportln("Searching for chromosome: \""+chrom+"\" in VCF file: " +inputVCF+" ["+getDateAndTimeString+"]","note");
          skipWhileWithProgress(altReader,60)(vAlt => vAlt.chrom != chrom, (vAlt) => "["+vAlt.chrom +"]");
          if(! altReader.hasNext){
            if(loopBack){
              reportln("    Looping back to start of VCF file: " +inputVCF+" "+getDateAndTimeString,"note");
              altReader = SVcfLine.readVcf(getLinesSmartUnzip(inputVCF), withProgress = false)._2.buffered;
              return getForPosition(chrom=chrom,pos=pos,loopBack = false);
            } else {                             
              warning("    Cannot find chromosome: \""+chrom+"\" in VCF file: " +inputVCF+" "+getDateAndTimeString,"CHROM_NOT_FOUND",10);
              if(failOnBadChrom) error("    Cannot find chromosome: \""+chrom+"\" in VCF file: " +inputVCF+" "+getDateAndTimeString);
              missingChrom += chrom;
              return Seq[SVcfVariantLine]();
            }
          } else {
            reportln("   Found chromosome: \""+chrom+"\" in VCF file: " +inputVCF+" ["+getDateAndTimeString+"]","note");
            altChrom = chrom;
          }
        }
      }
      skipWhile(altReader)(vAlt => vAlt.pos < pos && vAlt.chrom == chrom);
      return extractWhile(altReader)(vAlt => vAlt.pos == pos && vAlt.chrom == chrom);
    }
    
    
    /*val newFmtTags = 
      altHeader.formatLines.map{ fhl => {
           val ct = if(fhl.ID == "AD"){
             "R"
           } else {
             fhl.Number
           }
           (fhl.ID, new SVcfCompoundHeaderLine(in_tag = "FORMAT",ID = t + "_" + fhl.ID, Number = ct, Type = fhl.Type, desc = "From the VCF "+inputVcfName+", " + cleanQuotes(fhl.desc)))
      }}.toSeq*/
    
    val genotypeOrdering = new Ordering[String]{
                        def compare(x : String, y : String) : Int = {
                          if(x == y) 0;
                          else if(x == ".") -1;
                          else if(y == ".") 1;
                          else {
                            val xi = string2int(x);
                            val yi = string2int(y);
                            if(xi < yi) -1;
                            else 1;
                          }
                        }
                      }
    
    val keepLines = {getTags match {
      case Some(gtags) => {
        gtags.map{ t => {
          altHeader.infoLines.find( info => info.ID == t) match {
            case Some(info) => info;
            case None => {
              altHeader.formatLines.find( info => info.ID == t ) match {
                case Some(fmt) => {
                  fmt
                }
                case None => {
                  error("Error: cannot find field: "+t);
                  null;
                }
              }
            }
          }
        }}

      }
      case None => altHeader.infoLines
    }}.filter{ x => ! dropTags.contains(x) }
    
    val newInfoTags : Seq[(SVcfCompoundHeaderLine,SVcfCompoundHeaderLine)] = {
          keepLines.map{ infoLine => {
            val num = if(infoLine.Number == "G" || infoLine.Number == "1" || infoLine.Number == "A") "." else infoLine.Number;
            (infoLine,new SVcfCompoundHeaderLine(in_tag = "INFO",ID = tagPrefix+inputVcfTag + "_" + infoLine.ID, Number = num, Type = infoLine.Type, desc = "From the VCF"+inputVcfName+", " + cleanQuotes(infoLine.desc)))
          }}
    }
    //val newInfoA = newInfoTags.filter{ case (oldTag,newTag) => newTag.Number == "A"}
    //val newInfoR = newInfoTags.filter{  case (oldTag,newTag) => newTag.Number == "R"}
    //val newInfoOther = newInfoTags.filter{  case (oldTag,newTag) => newTag.Number != "R" && newTag.Number != "A"}
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      val vcfCodes = VCFAnnoCodes();
      
      val newHeader = vcfHeader.copyHeader;
      newInfoTags.foreach{  case (oldTag,newTag) => {
        newHeader.addInfoLine(newTag, walker=Some(this));
      }}
      newHeader.addWalk(this);
      
      val sampNames = vcfHeader.titleLine.sampleList;
      val sampCt = sampNames.length;
      
      val out = vcGroupedFlatMap(groupBySpan(vcIter.buffered)(vc => vc.pos))( vcSeq => {
        val currPos = vcSeq.head.pos;
        val currChrom = vcSeq.head.chrom;
        
        val altVcAtPos = getForPosition(currChrom,currPos);
        if(altVcAtPos.exists{ avc => { avc.alt.filter{ a => a != internalUtils.VcfTool.UNKNOWN_ALT_TAG_STRING }.length > 1 }}){
          error("mergeSecondaryVcf utility requires multiallelic variants be split in the secondary VCF!");
        }
        
        vcSeq.iterator.map(vc => {
          
          val vb = vc.getOutputLine();
          val altAlles = vc.alt.filter{ a => a != internalUtils.VcfTool.UNKNOWN_ALT_TAG_STRING };
          if(altAlles.length > 1) error("mergeSecondaryVcf utility requires multiallelic variants be split in the primary VCF!");
          val altAlle = altAlles.head;
          var ensembleWarnings = Set[String]();
          
          val matchVc = altVcAtPos.zipWithIndex.flatMap{ case (avc,xxi) => {
            if(avc.ref.take(math.min(avc.ref.length,vc.ref.length)) != vc.ref.take(math.min(avc.ref.length,vc.ref.length)) ){
              warning("REFERENCE ALLE DOES NOT MATCH!\n   "+vc.getVcfStringNoGenotypes+"\n   vs\n   "+avc.getVcfStringNoGenotypes,"REF_MISMATCH",100);
            }
            //val xxi = avc.alt.indexOf(altAlle)
            if(avc.alt.isDefinedAt(0) && avc.ref == vc.ref && avc.alt.head == altAlle ){
              Some((avc,xxi));
            } else {
              None;
            }
          }}
          if( matchVc.length > 1){
            notice("Duplicate lines found in secondary VCF "+inputVCF+"\n"+
                  "   for primary line:"+vc.getSimpleVcfString()+"\n"+
                  "   with secondary matches:\n"+
                  "   "+matchVc.map{ case (avc,xxi) => avc.getSimpleVcfString() }.mkString("\n   ")+"\n",
                  "DUPLICATE_SECONDARY_VCF_LINES",10
            );

          } 
          if(matchVc.length >= 1){
           // val (avc,avcAlleIdx) = matchVc.head;
            newInfoTags.foreach{ case (oldTag,newTag) => {
              
              /*
              val tagValue = if(oldTag.Number == "A"){
                 matchVc.flatMap{ case (avc,avcAlleIdx) => { avc.info.getOrElse(oldTag.ID,None) match {
                  case Some(t) => {
                    val c = t.split(",");
                    if(c.isDefinedAt(avcAlleIdx)){
                       Some(c(avcAlleIdx)) ;
                    } else {
                       warning("MALFORMED A-FIELD: " +oldTag.ID,"SECVCF_MALFORMED_FIELD_"+oldTag.ID,10);
                       None ;
                    }
                  }
                  case None => None ;
                }}}.padTo(1,".").mkString(",");
              } else if(oldTag.Number == "R"){
                val (ta,tb) : (Seq[String],Seq[String]) = matchVc.flatMap{ case (avc,avcAlleIdx) => { avc.info.getOrElse(oldTag.ID,None) match {
                  case Some(t) => {
                    val c = t.split(",");
                    if(c.isDefinedAt(avcAlleIdx+1)){
                       Some((c(0),c(avcAlleIdx) ));
                    } else {
                       warning("MALFORMED R-FIELD: " +oldTag.ID,"SECVCF_MALFORMED_FIELD_"+oldTag.ID,10);
                       None
                    }
                  }
                  case None => None;
                }}}.padTo(1,(".",".")).unzip
                ta.mkString("|")+","+tb.mkString("|");
              } else {
                matchVc.flatMap{ case (avc,xxi) => { avc.info.getOrElse(oldTag.ID,None) match {
                  case Some(t) => Some(t);
                  case None => None;
                }}}.padTo(1,".").mkString(",")
              }*/
              val tagValue : Seq[String] = matchVc.flatMap{ case (avc,avcAlleIdx) => {
                if(oldTag.tag == "INFO"){
                  avc.info.getOrElse(oldTag.ID,None)
                } else {
                  val tagIdx = avc.genotypes.fmt.indexOf(oldTag.ID);
                  if(tagIdx == -1){
                    None
                  } else {
                    Some(avc.genotypes.genotypeValues(tagIdx).head)
                  }
                }
                
              }}
              
              vb.addInfo(newTag.ID,tagValue.padTo(1,".").mkString(","));
            }}
          }
          vb;
        })
      })
      
      (out,newHeader);
    };
    
  }
  

  case class FixEnsemblMerge2(inputVCFs : Seq[String], inputVcfTypes : Seq[String], 
                              masterCaller : Option[String], summaryFile : Option[String]) extends SVcfWalker {
    def walkerName : String = "FixEnsemblMerge2"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("inputVCFs",fmtList(inputVCFs)),
        ("inputVcfTypes",fmtList(inputVcfTypes)),
        ("masterCaller",masterCaller.getOrElse("None")),
        ("summaryFile",summaryFile.getOrElse("None"))
    )

    //val LEGAL_VCF_TYPES : Set[String] = Set("hc","fb","ug");
    
    //if(inputVcfTypes.exists(! LEGAL_VCF_TYPES.contains(_))){
    //  error("Illegal VCF TYPE. Must be one of: [\"" + LEGAL_VCF_TYPES.toSeq.sorted.mkString("\",\"") + "\"]");
    //}
    //LEGAL_VCF_TYPES.foreach(t => {
    //  if(inputVcfTypes.count(_ == t) > 1){
    //    error("Illegal VCF TYPES. CANNOT HAVE MORE THAN ONE VCF OF EACH TYPE. FOUND "+inputVcfTypes.count(_ == t) + " VCF's with given type = \""+t+"\"");
    //  }
    //})
    
    val fileList = inputVCFs.zip(inputVcfTypes);
    val readers = fileList.map{case (infile,t) => {
      if(infile.contains('|')){
        SVcfLine.readVcfs(infile.split("\\|").iterator.map{f => getLinesSmartUnzip(f).buffered}.buffered, withProgress = false)
      } else {
        SVcfLine.readVcf(getLinesSmartUnzip(infile), withProgress = false)
      }
    }}; 
    val headers = readers.map(_._1);
    val iteratorArray : Array[BufferedIterator[SVcfVariantLine]] = readers.map(_._2.buffered).toArray;
    
    val fmtTags = headers.zip(inputVcfTypes).map{ case (h,t) => {
      h.formatLines.map{ fhl => {
           val ct = if(fhl.ID == "AD"){
             "R"
           } else {
             fhl.Number
           }
           val subType = if(fhl.ID == "AD"){
             Some(VcfTool.subtype_AlleleCountsUnsplit)
           } else if(fhl.ID == "GT"){
             Some(VcfTool.subtype_GtStyleUnsplit)
           } else {
             None
           }
           (fhl.ID, new SVcfCompoundHeaderLine(in_tag = "FORMAT",ID = t + "_" + fhl.ID, Number = ct, Type = fhl.Type, desc = "For the caller "+t+", " + cleanQuotes(fhl.desc),subType=subType))
      }}.toSeq
    }}
    
    val genotypeOrdering = new Ordering[String]{
                        def compare(x : String, y : String) : Int = {
                          if(x == y) 0;
                          else if(x == ".") -1;
                          else if(y == ".") 1;
                          else {
                            val xi = string2int(x);
                            val yi = string2int(y);
                            if(xi < yi) -1;
                            else 1;
                          }
                        }
                      }
    
    val masterCallerIdx = masterCaller match {
      case Some(mc) => {
        inputVcfTypes.indexOf(mc)
      }
      case None => None;
    }
    val masterCallerInfoTags : Seq[SVcfCompoundHeaderLine] = masterCaller match {
      case Some(mc) => {
        if( inputVcfTypes.contains(mc)){
          val masterIdx = inputVcfTypes.indexOf(mc)
          val masterHeader = headers(masterIdx);
          masterHeader.infoLines.map{ infoLine => {
            val num = if(infoLine.Number == "R" || infoLine.Number == "G" || infoLine.Number == "A") "." else infoLine.Number;
            new SVcfCompoundHeaderLine(in_tag = "INFO",ID = "SWH_"+ mc + "_" + infoLine.ID, Number = num, Type = infoLine.Type, desc = "For the caller "+mc+", " + cleanQuotes(infoLine.desc))
          }}
        } else {
          error("ERROR: Master Caller Not Found! Must be one of: " + inputVcfTypes.mkString(","));
          Seq[SVcfCompoundHeaderLine]();
        }
      }
      case None => {
        Seq[SVcfCompoundHeaderLine]();
      }
    }
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      val vcfCodes = VCFAnnoCodes();

      
      val customInfoLines : Seq[Seq[SVcfCompoundHeaderLine]]= inputVcfTypes.map{t => {
                                                Seq(
                                                    new SVcfCompoundHeaderLine("INFO", vcfCodes.ec_singleCallerAllePrefix+t, ".", "String", "Alt Alleles for caller "+(t))
                                                   )
                                        }}
      

      val extraInfoLines : Seq[SVcfCompoundHeaderLine]= Seq(
               new SVcfCompoundHeaderLine("INFO", vcfCodes.ec_CallMismatch,        "1", "Integer", "Num genotypes that actively disagree (ie called in different ways)"),
               new SVcfCompoundHeaderLine("INFO",  vcfCodes.ec_CallMismatchStrict, "1", "Integer", "Num genotypes that do not give the exact same call (including no-call vs call)"),
               new SVcfCompoundHeaderLine("INFO",  vcfCodes.ec_EnsembleWarnings,   ".", "String", "List of warnings related to the ensemble calling"),
               new SVcfCompoundHeaderLine("INFO",  vcfCodes.ec_alle_callerSets,   "A", "String", "For each alt allele, which callers included the given allele.")
          ) ++ masterCallerInfoTags;
          
          
      val extraFmtLines : Seq[SVcfCompoundHeaderLine]= Seq(
                new SVcfCompoundHeaderLine("FORMAT", "MISMATCH", "1", "String", "All callers do not actively disagree."),
                new SVcfCompoundHeaderLine("FORMAT", "MISMATCH_STRICT", "1", "String", "All callers provide the same call."),
                new SVcfCompoundHeaderLine("FORMAT", "CallerSupport", ".", "String", "List of callers that support the final call."),
                new SVcfCompoundHeaderLine("FORMAT", "HasSupport", "1", "String", "Final call is supported by at least one caller."),
                new SVcfCompoundHeaderLine("FORMAT", "ENS_WARN", ".", "String", "Warnings related to the ensemble calls.")
              );
      
      val customFmtLines : Seq[Seq[SVcfCompoundHeaderLine]] = inputVcfTypes.map{t =>{
                                                Seq(
                                                    new SVcfCompoundHeaderLine("FORMAT", t+"_GT_RAW", ".", "String", "Raw Genotype Call for caller "+t, extraFields = Seq((internalUtils.VcfTool.subtype_GtStyle,"")).toMap),
                                                    new SVcfCompoundHeaderLine("FORMAT", t+"_GT_FIX", "1", "String", "Recoded Genotype Call for caller "+t)
                                                )
                                        }}
      
      val newFmtLines : Seq[SVcfCompoundHeaderLine]= fmtTags.flatMap{ newTagLines =>
                                        newTagLines.map(_._2)
      } ++ customFmtLines.flatten ++ extraFmtLines;
      
      val newHeader = vcfHeader.copyHeader;
      (customInfoLines.flatten ++ extraInfoLines).foreach{ hl => {
        newHeader.addInfoLine(hl, walker = Some(this));
      }}
      (newFmtLines).foreach{ hl => {
        newHeader.addFormatLine(hl, walker = Some(this));
      }}
      
      //val newHeader = SVcfHeader(infoLines = vcfHeader.infoLines ++ customInfoLines.flatten ++ extraInfoLines, 
      //                          formatLines = newFmtLines,
      //                          otherHeaderLines = vcfHeader.otherHeaderLines,
      //                           walkLines = vcfHeader.walkLines, 
      //                          titleLine = vcfHeader.titleLine);
      newHeader.addWalk(this);
      val sampNames = vcfHeader.titleLine.sampleList;
      val sampCt = sampNames.length;
      //var currIter = vcIter.buffered;
      
      /*
      val out = groupBySpan(vcIter)(vc => vc.pos).flatMap( vcSeq => {
        val currPos = vcSeq.head.pos;
        iteratorArray.indices.foreach{i => {
          //iteratorArray(i) = iteratorArray(i).dropWhile(vAlt => vAlt.pos < currPos);
          
        }}
        val otherVcAtPos = iteratorArray.indices.map{i => {
          val (a,r) = spanVector(iteratorArray(i))(vAlt => vAlt.pos == currPos);
          iteratorArray(i) = r;
          a
        }}*/
      //val out = vcFlatMap(vcIter)(
      val out = vcGroupedFlatMap(groupBySpan(vcIter.buffered)(vc => vc.pos))( vcSeq => {
        val currPos = vcSeq.head.pos;
        val currChrom = vcSeq.head.chrom;
        iteratorArray.indices.foreach{i => {
          //iteratorArray(i) = iteratorArray(i).dropWhile(vAlt => vAlt.pos < currPos);
          skipWhile(iteratorArray(i))(vAlt => vAlt.chrom != currChrom);
          skipWhile(iteratorArray(i))(vAlt => vAlt.pos < currPos && vAlt.chrom == currChrom);
        }}
        val otherVcAtPos = iteratorArray.indices.map{i => {
          extractWhile(iteratorArray(i))(vAlt => vAlt.pos == currPos && vAlt.chrom == currChrom);
        }}
        vcSeq.iterator.map(vc => {
          
          val vb = vc.getOutputLine();
          val altAlles = vc.alt;
          var ensembleWarnings = Set[String]();
          val ploidy = vc.genotypes.genotypeValues(0).map{ _.split("/").length }.max
          
          if(ploidy > 2){
            warning("Ploidy greater than 2! Ploidy = "+ploidy,"POLYPLOID",100)
          } else if(ploidy == 1){
            warning("Haploid! Ploidy = "+ploidy,"HAPLOID",100)
          }
          
          if(altAlles.length > 1){
            ensembleWarnings = ensembleWarnings + ("MULTIALLELIC");
          }
          
          var callerSets = Array.fill[Set[String]](altAlles.length)(Set[String]());
          var sampleWarn = Array.fill[Set[String]](sampCt)(Set[String]()); 
          
          try{
          otherVcAtPos.zip(fmtTags).zipWithIndex.foreach{ case ((otherLines,otherFmtTags),otherFileIdx) => {
            val otherFileType = inputVcfTypes(otherFileIdx);
            val matchIdx = otherLines.zipWithIndex.flatMap{ case (otherVC, otherLineIdx) => {
              otherVC.alt.zipWithIndex.filter{case (a,idx) => {a != "*"}}.flatMap{ case (otherAlle, otherAlleIdx) => {
                vb.alt.zipWithIndex.filter{case (alle,idx) => {alle == otherAlle}}.map{case (currAlle,currAlleIdx) => ((currAlleIdx,(otherLineIdx,otherAlleIdx)))}
              }}
            }}.toMap;
            val linesWithMatch = matchIdx.map{case (currAlleIdx,(otherLineIdx,otherAlleIdx)) => otherLineIdx}.toSet.toSeq.sorted;
            val linesWithoutMatch = (otherLines.indices.toSet -- linesWithMatch.toSet).toVector.sorted;
            val numLinesWithMatch = linesWithMatch.size;
            
            if(otherLines.length > 1){
              notice("Multiple lines found at location (Caller "+inputVcfTypes(otherFileIdx)+", POS="+vcSeq.head.chrom+":"+currPos+")","MULTILINE_LOCUS_"+otherFileType,5);
              ensembleWarnings = ensembleWarnings + ("MULTILINELOCUS_"+inputVcfTypes(otherFileIdx));
            }
            
            if(numLinesWithMatch > 1){
              notice("Multiple lines found at location that contain matches (Caller "+inputVcfTypes(otherFileIdx)+", POS="+vcSeq.head.chrom+":"+currPos+")","MULTIMATCHLINE_LOCUS_"+otherFileType,5);
            }
            
            if(! matchIdx.isEmpty){
              val alts = altAlles.zipWithIndex.filter{case (a,idx) => {a != "*"}};
              val fmtA = fmtTags(otherFileIdx).filter{case (rawFmtTag,fmtLine) => {
                fmtLine.Number == "A"
              }}.map{ case (rawFmtTag,fmtLine) => {
                (Array.fill[String](sampCt,alts.length)("."),rawFmtTag,fmtLine);
              }}
              val fmtR = fmtTags(otherFileIdx).filter{case (rawFmtTag,fmtLine) => {
                fmtLine.Number == "R"
              }}.map{ case (rawFmtTag,fmtLine) => {
                (Array.fill[String](sampCt,alts.length + 1)("."),rawFmtTag,fmtLine);
              }}
              val fmtOther = fmtTags(otherFileIdx).filter{case (rawFmtTag,fmtLine) => {
                fmtLine.Number != "R" && fmtLine.Number != "A";
              }}.map{ case (rawFmtTag,fmtLine) => {
                (Array.fill[String](sampCt,numLinesWithMatch)("."),rawFmtTag,fmtLine);
              }}
              
              val altAlleArray = Array.fill[String](otherLines.length)(".");
              val altGtArray = Array.fill[String](sampCt,otherLines.length)("./.");
              val altGtFixedArray = Array.fill[String](sampCt,2)(".");
              
              matchIdx.groupBy{ case (currAlleIdx,(otherLineIdx,otherAlleIdx)) => { otherLineIdx }}.foreach{ case (otherLineIdx, s) => {
                
                val matchLineIdx = linesWithMatch.indexOf(otherLineIdx);
                val otherAlts = otherLines(otherLineIdx).alt.zipWithIndex
                try{
                  val otherGenotypeFormat = otherLines(otherLineIdx).format;
                  val otherGenotypeArray = otherLines(otherLineIdx).genotypes.genotypeValues;
                  val rawArrayFmtA = fmtA.zipWithIndex.filter{ case ((gArray,rawFmtTag,fmtLine),i) => {
                    otherGenotypeFormat.contains(rawFmtTag);
                  }}.map{ case ((gArray,rawFmtTag,fmtLine),i) => {
                    val fmtIdx = otherGenotypeFormat.indexOf(rawFmtTag);
                    (otherGenotypeArray(fmtIdx).map(otherG => {
                      if(otherG == ".") {
                        Array.fill[String](otherAlts.length)(".");
                      } else {
                        otherG.split(",")
                      }
                    }),i);
                  }}
                  val rawArrayFmtR = fmtR.zipWithIndex.filter{ case ((gArray,rawFmtTag,fmtLine),i) => {
                    otherGenotypeFormat.contains(rawFmtTag);
                  }}.map{ case ((gArray,rawFmtTag,fmtLine),i) => {
                    val fmtIdx = otherGenotypeFormat.indexOf(rawFmtTag);
                    (otherGenotypeArray(fmtIdx).map(otherG => {
                      if(otherG == ".") {
                        Array.fill[String](otherAlts.length + 1)(".");
                      } else {
                        otherG.split(",")
                      }
                    }),i);
                  }}
                  val rawArrayFmtOther = fmtOther.zipWithIndex.filter{ case ((gArray,rawFmtTag,fmtLine),i) => {
                    otherGenotypeFormat.contains(rawFmtTag);
                  }}.map{ case ((gArray,rawFmtTag,fmtLine),i) => {
                    val fmtIdx = otherGenotypeFormat.indexOf(rawFmtTag);
                    (otherGenotypeArray(fmtIdx),i);
                  }}
                  
                  fmtOther.zipWithIndex.filter{ case ((gArray,rawFmtTag,fmtLine),i) => {
                    otherGenotypeFormat.contains(rawFmtTag);
                  }}.foreach{ case ((gArray,rawFmtTag,fmtLine),i) => {
                    val fmtIdx = otherGenotypeFormat.indexOf(rawFmtTag);
                    val gArray = otherGenotypeArray(fmtIdx)
                    Range(0,sampCt).foreach{sampIdx => {
                      val currArray = fmtOther(i)._1;
                      currArray(sampIdx)(matchLineIdx) = gArray(sampIdx);
                    }}
                  }}
                  
                  rawArrayFmtR.foreach{ case (gArray,i) => {
                      val currArray = fmtR(i)._1;
                      Range(0,sampCt).foreach{sampIdx => {
                        currArray(sampIdx)(0) = gArray(sampIdx)(0);
                      }}
                  }}
                  
                  s.foreach{ case (currAlleIdx,(oli,otherAlleIdx)) => {
                    callerSets(currAlleIdx) = callerSets(currAlleIdx) + inputVcfTypes(otherFileIdx);
                    rawArrayFmtA.foreach{ case (gArray,i) => {
                      val currArray = fmtA(i)._1;
                      Range(0,sampCt).foreach{sampIdx => {
                        if( otherAlleIdx >= gArray(sampIdx).length) warning("Found problem with genotype "+sampIdx+": otherAlleIdx="+otherAlleIdx+", but for tag "+fmtA(i)._2+", length is: "+gArray(sampIdx).length,"MALFORMED_GENOTYPE",100);
                        currArray(sampIdx)(currAlleIdx) = gArray(sampIdx)(otherAlleIdx);
                      }}
                    }}
                    rawArrayFmtR.foreach{ case (gArray,i) => {
                      val currArray = fmtR(i)._1;
                      Range(0,sampCt).foreach{sampIdx => {
                        currArray(sampIdx)(currAlleIdx + 1) = gArray(sampIdx)(otherAlleIdx + 1);
                      }}
                    }}
                    Range(0,sampCt).foreach{sampIdx => {
                      val geno = otherGenotypeArray(0)(sampIdx).split("/");
                      geno.zipWithIndex.foreach{case (g,i) => {
                        if(g == (otherAlleIdx + 1).toString()){
                          if(altGtFixedArray(sampIdx)(i) != "." && altGtFixedArray(sampIdx)(i) != (currAlleIdx + 1).toString){
                              warning("Overwriting existing variant on a multiline merger!","OVERWRITE_VARIANT_ON_MULTILINE_MERGE_"+otherFileType,5);
                              ensembleWarnings = ensembleWarnings + ("OVERWRITE_VARIANT_ON_MULTILINE_MERGE_"+otherFileType);
                              sampleWarn(sampIdx) = sampleWarn(sampIdx) + ("OVERWRITE_VARIANT_ON_MULTILINE_MERGE_"+otherFileType);
                          }
                          altGtFixedArray(sampIdx)(i) = (currAlleIdx + 1).toString;
                        }
                      }}
                    }}
                  }}
                  altAlleArray(otherLineIdx) = otherLines(otherLineIdx).alt.mkString(",");
                  Range(0,sampCt).foreach{ sampIdx => {
                    altGtArray(sampIdx)(otherLineIdx) = otherGenotypeArray(0)(sampIdx);
                  }}
                  Range(0,sampCt).foreach{sampIdx => {
                      val geno = otherGenotypeArray(0)(sampIdx).split("/");
                      geno.zipWithIndex.foreach{case (g,i) => {
                        if(g == "0"){
                          if(altGtFixedArray(sampIdx)(i) != "." && altGtFixedArray(sampIdx)(i) != "0"){
                              warning("Overwriting existing variant on a multiline merger!","OVERWRITE_REFVARIANT_ON_MULTILINE_MERGE_"+otherFileType,5);
                              ensembleWarnings = ensembleWarnings + ("OVERWRITE_REFVARIANT_ON_MULTILINE_MERGE_"+otherFileType);
                              sampleWarn(sampIdx) = sampleWarn(sampIdx) + ("OVERWRITE_VARIANT_ON_MULTILINE_MERGE_"+otherFileType);
                          } else {
                            altGtFixedArray(sampIdx)(i) = "0";
                          }
                        }
                      }}
                  }}
                } catch {
                  case e : Exception => {
                    reportln("Caught exception in matchIDX iteration.\n"+
                       "###CURRENT OTHER VCF LINE:\n"+
                       otherLines(otherLineIdx).getVcfString + "\n"+
                       s.map{ case (currAlleIdx,(oli,otherAlleIdx)) => {
                          "   (currAlleIdx="+currAlleIdx+", otherLineIdx="+otherLineIdx+", otherAlleIdx="+otherAlleIdx+", matchLineIdx="+matchLineIdx+")"
                       }}.mkString("\n")+"\n"+
                       
                       "###Match IDX:"+
                       matchIdx.map{ case (cai,(oli,oai)) => {
                         "   (currAlleIdx="+cai+", otherLineIdx="+oli+", otherAlleIdx="+oai+")\n"+
                         otherLines(oli).getVcfString
                       }}.mkString("\n")+"\n"+
                       "","note");
                    throw e;
                  }
                }
              }}
              
              linesWithoutMatch.foreach{otherLineIdx => {
                val otherGenotypeArray = otherLines(otherLineIdx).genotypes.genotypeValues;
                Range(0,sampCt).foreach{sampIdx => {
                    val geno = otherGenotypeArray(0)(sampIdx).split("/");
                    geno.zipWithIndex.foreach{case (g,i) => {
                      if(g == "0"){
                        if(altGtFixedArray(sampIdx)(i) != "." && altGtFixedArray(sampIdx)(i) != "0"){
                            warning("Conflicting existing refvariant on a multiline merger!","OVERWRITE_REFVARIANT_ON_MULTILINE_MERGE_"+otherFileType,5);
                            ensembleWarnings = ensembleWarnings + ("OVERWRITE_REFVARIANT_ON_MULTILINE_MERGE_"+otherFileType);
                            sampleWarn(sampIdx) = sampleWarn(sampIdx) + ("OVERWRITE_VARIANT_ON_MULTILINE_MERGE_"+otherFileType);
                        } else {
                          altGtFixedArray(sampIdx)(i) = "0";
                        }
                      }
                    }}
                }}
              }}
              
              if(ploidy > 1){
                Range(0,sampCt).foreach{sampIdx => {
                      altGtFixedArray(sampIdx) = altGtFixedArray(sampIdx).sortBy(s => s)(genotypeOrdering)
                }}
              }
              //vb.genotypes.fmt = vb.genotypes.fmt ++ fmtA.map{_._3} ++ fmtR.map{_._3} ++ fmtOther.map{_._3} ++ customFmtLines(otherFileIdx)
              vb.genotypes.fmt = vb.genotypes.fmt ++ fmtA.map{_._3.ID} ++ fmtR.map{_._3.ID} ++ fmtOther.map{_._3.ID} ++ customFmtLines(otherFileIdx).map(_.ID);
              //vb.in_format = vb.in_format ++ fmtA.map{_._3.ID} ++ fmtR.map{_._3.ID} ++ fmtOther.map{_._3.ID} ++ customFmtLines(otherFileIdx).map(_.ID);
              vb.genotypes.genotypeValues = ( vb.genotypes.genotypeValues ++ fmtA.map{_._1.map(_.mkString(","))} ++ 
                                              fmtR.map{_._1.map(_.mkString(","))} ++ 
                                              fmtOther.map{_._1.map(_.mkString("|"))} ) ++
                                             Array(
                                                 altGtArray.map(_.mkString("|")),
                                                 altGtFixedArray.map(_.mkString("/"))
                                             )
              vb.in_info = vb.in_info ++ Map(
                    (vcfCodes.ec_singleCallerAllePrefix+otherFileType,Some(altAlleArray.mkString("|")))
                  )
              if(masterCaller.isDefined && masterCaller.get == inputVcfTypes(otherFileIdx)){
                 if(linesWithMatch.size > 1){
                   warning("WARNING: Master Caller should only ever have 1 line per position. Ambiguous output!","BAD_MASTER_CALLER",5);
                 }
                 val otherLineIdx = linesWithMatch.head;
                 val otherInfo = otherLines(otherLineIdx).info;
                 vb.in_info = vb.in_info ++ otherInfo.map{ case (infoTag,infoValue) => {
                   ("SWH_"+ masterCaller.get + "_" + infoTag , infoValue)
                 }}
              }
            }  
          }}
          
          
          val masterGT = vb.genotypes.genotypeValues(0);
          val mm = Array.fill[Boolean](sampCt)(false);
          val mmStrict = Array.fill[Boolean](sampCt)(false);
          inputVcfTypes.foreach{ivt => {
              val gtTag = ivt+"_GT_FIX";
              val gtIdx = vb.format.indexOf(gtTag);
              if(gtIdx != -1){
                val otherGT = vb.genotypes.genotypeValues(gtIdx);
                masterGT.indices.foreach{sampIdx => {
                  mmStrict(sampIdx) = mmStrict(sampIdx) || masterGT(sampIdx) != otherGT(sampIdx);
                  val mgt = masterGT(sampIdx).split("/");
                  val ogt = otherGT(sampIdx).split("/");
                  mgt.zip(ogt).foreach{case (m,o) => {
                    if(m != "." && o != "." && m != o){
                      mm(sampIdx) = true;
                      sampleWarn(sampIdx) = sampleWarn(sampIdx) + ("CALLER_MISMATCH_"+ivt);
                    }
                  }}
                }}
              }
          }}
          val gtTags = inputVcfTypes.map{ivt => { ivt+"_GT_FIX" }};
          val gtSet  = gtTags.zip(gtTags.map{vb.format.indexOf(_)}).zip(inputVcfTypes).filter{ case ((t,i),f) => i != -1};
          
          val gtCallerSupport = masterGT.indices.map{sampIdx => {
            val mgt = masterGT(sampIdx);
            if(! mgt.contains('.')){
              gtSet.flatMap{ case ((otherTag,otherIdx),ivt) => {
                if(vb.genotypes.genotypeValues(otherIdx)(sampIdx) == mgt){
                  Some(ivt);
                } else {
                  None;
                }
              }}.mkString(",");
            } else {
              "NA"
            }
          }}.toArray;
          val gtIsSupported = masterGT.indices.map{sampIdx => {
            val mgt = masterGT(sampIdx).split("/");
            val isSupported = mgt.forall{m => {
              m == "." || {
                gtSet.exists{ case ((otherTag,otherIdx),ivt) => {
                  val ogt = vb.genotypes.genotypeValues(otherIdx)(sampIdx).split("/");
                  ogt.contains(m);
                }}
              }
            }}
            if(! isSupported){
              sampleWarn(sampIdx) = sampleWarn(sampIdx) + ("UNSUPPORTED");
            }
            isSupported
          }}
          val numUnsupportedGT = gtIsSupported.count(! _);
          if(numUnsupportedGT > 0){
            val warnMsg = "UNSUPPORTED_GT"
            warningLazy(() => 
                    "Found "+numUnsupportedGT+ " unsupported genotypes!\n"+
                    "    "+vc.getSimpleVcfString()+"\n"+
                    "    Other VC at pos:\n"+
                    "        "+inputVcfTypes.zip(otherVcAtPos).map{ case (vt,otherVcList) => vt+":\n           "+otherVcList.map{ovc => ovc.getSimpleVcfString()}.mkString("\n           ")}.mkString("\n        ")+"\n"+
                    "",
                    warnMsg,5);
            ensembleWarnings = ensembleWarnings + (warnMsg);
          }
          
          //vb.genotypes.fmt = vb.genotypes.fmt ++ extraFmtLines
          vb.genotypes.fmt = vb.genotypes.fmt ++ extraFmtLines.map{efl => efl.ID}
          //vb.in_format = vb.in_format ++ extraFmtLines.map{efl => efl.ID}
          vb.genotypes.genotypeValues = vb.genotypes.genotypeValues ++ 
                                               Array(mm.map(if(_) "1" else "0"),
                                                     mmStrict.map(if(_) "1" else "0"),
                                                     gtCallerSupport,
                                                     gtIsSupported.map(if(_) "1" else "0").toArray,
                                                     sampleWarn.map{s => s.toSeq.padTo(1,".").sorted.mkString(",")})
          
                           
          vb.in_info = vb.in_info ++ Map(
                ( vcfCodes.ec_CallMismatch,Some( mm.count(x => x).toString )),
                ( vcfCodes.ec_CallMismatchStrict,Some( mmStrict.count(x => x).toString )),
                (vcfCodes.ec_EnsembleWarnings,Some( ensembleWarnings.toSeq.sorted.padTo(1,".").mkString(",") )),
                (vcfCodes.ec_alle_callerSets,Some(callerSets.map{s => s.toSeq.padTo(1,".").sorted.mkString("|")}.padTo(1,".").mkString(",")))
              );
          
          } catch {
            case e : Exception => {
              reportln("Caught exception in VCF iteration.\n"+
                       "###MASTER VCF LINE:\n"+
                       vc.getVcfString+"\n"+
                       (if(vcSeq.length > 1){ "###All master lines at position:\n" + vcSeq.map(v => v.getVcfString).mkString("\n") + "\n" } else { "" }) +
                       "###All VCF Lines at position:\n"+
                       otherVcAtPos.zip(inputVcfTypes).map{ case (otherVCs,fileID) => {
                         otherVCs.map(v => fileID + "\t" + v.getVcfString).mkString("\n")
                       }}.mkString("\n") + "\n"+
                       "","note");
              
              throw e;
            }
          }
          vb;
          
        })
      })
      
      (out,newHeader);
    };
    
  }
  
  
  class recodeROAOtoAD(roTag: String,aoTag : String, adTag : String, desc : String = "No desc.") extends SVcfWalker {
    def walkerName : String = "recodeROAOtoAD"
    val  splitSecondary : Boolean = true
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("roTag",roTag),
        ("aoTag",aoTag),
        ("adTag",adTag)
    )
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      
      val newHeader = vcfHeader.copyHeader;
      newHeader.addWalk(this);
      newHeader.addFormatLine(new SVcfCompoundHeaderLine(in_tag = "FORMAT",ID = adTag, Number = "R", Type = "Integer", desc = desc.padTo(1,'.'), subType = Some(VcfTool.subtype_AlleleCountsUnsplit)).addWalker(this));
      
      
      ((vcMap(vcIter){v => {
        val vb = v.getOutputLine();
        /*
        v.genotypes.fmt.index(t => t == roTag).foreach{ roVal => {
          v.info.get(aoTag).foreach{ aoVal => {
            vb.addInfo(adTag,  roVal.getOrElse(".") +","+ aoVal.getOrElse( repToVector(".",v.alt.length).mkString(",") ) )
          }}
        }}*/
        val alleCt = v.alt.length;
        val roIdx = v.genotypes.fmt.indexOf(roTag);
        val aoIdx = v.genotypes.fmt.indexOf(aoTag);
        if(roIdx >= 0 && aoIdx >= 0){
          vb.genotypes.addGenotypeArray(adTag,v.genotypes.genotypeValues(roIdx).zip(v.genotypes.genotypeValues(aoIdx)).map{case (ro,ao) => ro + "," + ao.split(",").padTo(alleCt,".").mkString(",")})
        }
        
        vb
      }},newHeader))
    }
  }
  
class EnsembleMergeMetaDataWalker(inputVcfTypes : Seq[String],
                                    simpleMergeInfoTags : Seq[String] = Seq[String](),
                                    simpleMergeFmtTags : Seq[String] = Seq[String]("GQ|Final Ensemble genotype quality score"),
                                    gtStyleFmtTags : Seq[String] = Seq[String]("GT|Final ensemble genotype"),
                                    adStyleFmtTags : Seq[String] = Seq[String]("AD|Final ensemble allele depths","GQ|Final ensemble genotype quality"),
                                    decision : String = "majority_firstOnTies") extends SVcfWalker {
    //decision parameter can be either "first", "majority_firstOnTies", "majority_missOnTies";
    val dModeList = Seq[String]("first","majority_firstOnTies","majority_missOnTies")
    val dMode = dModeList.indexOf(decision);
    if(dMode == -1){
      error("ERROR: Illegal mode: "+decision+". Decision mode must be set to one of: "+dModeList.mkString(","));
    }
    val dModeImplementedList = Seq[String]("majority_firstOnTies");
    if(! dModeImplementedList.contains(decision)){
      error("ERROR: Unimplemented beta mode: "+decision+". Decision mode must be set to one of: "+dModeImplementedList.mkString(","));
    }
        
    def walkerName : String = "EnsembleMergeMetaDataWalker"
    val  splitSecondary : Boolean = true
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("inputVcfTypes",inputVcfTypes.mkString(",")),
        ("gtStyleFmtTags",gtStyleFmtTags.mkString(",")),
        ("adStyleFmtTags",adStyleFmtTags.mkString(","))
    )
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      
      val newHeader = vcfHeader.copyHeader;
      newHeader.addWalk(this);
      
      /*simpleMergeInfoTags.map{ tagString => { tagString.split("\\|") }}.foreach{ tagCells => {
        val (tag,desc) = (tagCells(0),tagCells.lift(1).getOrElse("No Desc"));
        newHeader.addInfoLine(new SVcfCompoundHeaderLine(in_tag = "INFO",ID = , Number = "R", Type = "Integer", desc = desc));
      }}*/
      
      val gtFmtTags : Seq[(String,Seq[(String,String)])] = gtStyleFmtTags.map{ tagString => { tagString.split("\\|") }}.map{ tagCells => (tagCells.head,tagCells.lift(1).getOrElse("No Desc")) }.flatMap{ case (t, desc) => {
        val nl = new SVcfCompoundHeaderLine("FORMAT",t, "1","String", desc, subType = Some(VcfTool.subtype_GtStyle)).addWalker(this);
        val disLine = new SVcfCompoundHeaderLine("FORMAT",t+"_DISAGREE", "1","Integer", "Equal to 1 if and only if there is no active disagreement between callers.").addWalker(this);
        val supLine = new SVcfCompoundHeaderLine("FORMAT",t+"_SUPPORT", ".","String", "Comma delimited list of callers that agree with a call.").addWalker(this);

        //reportln("Adding GtStyleUnsplit subtype to tag: "+nl.ID,"note");
        //reportln("    nl:"+nl.getVcfString,"note");
        val foundTags = inputVcfTypes.map{vt => (vt,vt+"_"+t)}.filter{ case (vt,vtag) => vcfHeader.formatLines.exists{ hl => hl.ID == vtag } };
        if(foundTags.isEmpty){
          None
        } else {
          newHeader.addFormatLine(nl);
          reportln("Merging GT-style tag "+t+" from callers: ["+foundTags.unzip._1.mkString(",")+"]","note");
          Some((t,foundTags));
        }
      }}
      val adFmtTags : Seq[(String,Seq[(String,String)])] = adStyleFmtTags.map{ tagString => { tagString.split("\\|") }}.map{ tagCells => (tagCells.head,tagCells.lift(1).getOrElse("No Desc")) }.flatMap{ case (t, desc) => {
        val nl = new SVcfCompoundHeaderLine("FORMAT",t, "A","Integer", desc, subType = Some(VcfTool.subtype_AlleleCounts)).addWalker(this);
        //reportln("Adding GtStyleUnsplit subtype to tag: "+nl.ID,"note");
        //reportln("    nl:"+nl.getVcfString,"note");
        val foundTags = inputVcfTypes.map{vt => (vt,vt+"_"+t)}.filter{ case (vt,vtag) => vcfHeader.formatLines.exists{ hl => hl.ID == vtag } };
        if(foundTags.isEmpty){
          None
        } else {
          newHeader.addFormatLine(nl);
          reportln("Merging AD-style tag "+t+" from callers: ["+foundTags.unzip._1.mkString(",")+"]","note");
          Some((t,foundTags));
        }
      }}
      
      val sampCt = vcfHeader.titleLine.sampleList.length;
      val rango = Range(0,sampCt);
      
      ((vcMap(vcIter){v => {
        val vb = v.getOutputLine();
        if(! v.genotypes.fmt.isEmpty){
          val supportSeq = gtFmtTags.map{ case (t,scTagSeq) => {
            val scTagIdx : Seq[(String,String,Int)] = scTagSeq.map{case (sc,tt) => (sc,tt,v.genotypes.fmt.indexOf(tt))}.filter{ case (sc,tt,idx) => idx != -1 };
            (t,scTagIdx)
          }}.withFilter{ case (t,scTagIdx) => {
            ! scTagIdx.isEmpty
          }}.map{ case (t,scTagIdx) => {
            val mergedGt = Array.fill(sampCt)("./.");
            val disagreeGt = Array.fill(sampCt)(0);
            val supportGt = Array.fill(sampCt)(".");
            val scGt : Array[Array[String]] = scTagIdx.toArray.map{ case (sc,tt,scIdx) => v.genotypes.genotypeValues(scIdx) };
            if(decision == "majority_firstOnTies"){
              //val total = scTagIdx.length;
              //val half = total / 2;
              rango.foreach{ i => {
                val gt = scGt.map{ gtArray => gtArray(i) };
                
                val gtFilt = gt.filter{ g => ! g.contains('.')}
                val scFilt = gt.zip(scTagIdx).withFilter{ case (g,(sc,tt,scIdx)) => ! g.contains('.')}.map{ case (g,(sc,tt,scIdx)) => sc }
                if(! gtFilt.isEmpty){
                  val (gtFinal,disagree,supportSet) = getFinalMajorityThenFirst(gtFilt,scFilt);
                  mergedGt(i) = gtFinal;
                  disagreeGt(i) = disagree;
                  supportGt(i) = supportSet.padTo(1,".").mkString(",");
                }
              }}
            }
            vb.genotypes.addGenotypeArray(t,mergedGt);
            vb.genotypes.addGenotypeArray(t+"_DISAGREE",disagreeGt.map{_.toString});
            vb.genotypes.addGenotypeArray(t+"_SUPPORT",supportGt);
            supportGt;
          }}
          
          supportSeq.headOption.map{supportArray => supportArray.map{ss => ss.split(",").filter(_ == ".")}}.foreach{ support => {
            adFmtTags.map{ case (t,scTagSeq) => {
              val scTagIdx : Seq[(String,String,Int)] = scTagSeq.map{case (sc,tt) => (sc,tt,v.genotypes.fmt.indexOf(tt))}.filter{ case (sc,tt,idx) => idx != -1 };
              (t,scTagIdx)
            }}.withFilter{ case (t,scTagIdx) => {
              ! scTagIdx.isEmpty
            }}.foreach{ case (t,scTagIdx) => {
              val mergedAD = Array.fill(sampCt)(".");
              val scGt : Array[Array[String]] = scTagIdx.toArray.map{ case (sc,tt,scIdx) => v.genotypes.genotypeValues(scIdx) };
              rango.foreach{ i => {
                val gt = scGt.map{ gtArray => gtArray(i) };
                val currSup = support(i);
                val scFilt = gt.zip(scTagIdx).filter{ case (g,(sc,tt,scIdx)) => (! g.startsWith("."))}.map{ case (g,(sc,tt,scIdx)) => (g,sc) }
                
                if(! scFilt.isEmpty){
                  scFilt.find{ case (ad,sc) => {  currSup.contains(sc) }} match {
                    case Some((ad,sc)) => {
                      mergedAD(i) = ad;
                    }
                    case None => {
                      mergedAD(i) = scFilt.head._1;
                    }
                  }
                }
              }}
              vb.genotypes.addGenotypeArray(t,mergedAD);
            }}
          }}
        }
        vb
      }},newHeader))
    }
        
    val possGenoSet = Seq(
          ("0/0"),
          ("0/1"),
          ("1/1"),
          ("0/2"),
          ("1/2"),
          ("2/2")
        )
    
    def getFinalPluralityThenFirst(gtFilt : Array[String],scFilt : Seq[String]) : (String,Int,Seq[String]) = {
      val total = gtFilt.length;
      val half = total / 2;
      
      val gtSet = gtFilt.distinct
      if(gtSet.length == 1){
        return (gtSet.head,0,scFilt)
      }
      val gtSetCts = gtSet.map{pg => (pg,gtFilt.count(_ == pg))}
      val mostCommonCt = gtSetCts.unzip._2.max;
      val gtZip = gtFilt.zip(scFilt);
      val gtSetCtsMC = gtSetCts.filter{ case (pg,cts) => { cts == mostCommonCt }};
      
      if(gtSetCtsMC.length == 1){
        val (pg,cts) = gtSetCtsMC.head
        return ((pg,1,gtZip.filter{case (sc,gt) => gt == pg}.unzip._1))
      } else {
        val (gt,sc) = gtZip.find{ case (gt,sc) => {
          gtSetCtsMC.exists{ case (pg,cts) => gt == pg}
        }}.get  //hack alert. unchecked get. It's probably fine.
        return ((gt,1,gtZip.filter{ case (sc,g) => gt == g}.unzip._1))
      }
      

      /*
      if(gtSetCtsMC.length == 1){
        gtSetCtsMC.headOption.foreach{ case (pg,cts) => {
          return ((pg,1,gtZip.filter{case (sc,gt) => gt == pg}.unzip._1))
        }}
      } else {
        return ((gtFilt.head,1,gtZip.filter{ case (sc,gt) => gt == gtFilt.head}.unzip._1))
      }*/
      
      /*gtSetCts.find{ case (pg,cts) => cts > half}.foreach{ case (pg,cts) => {
        return ((pg,1, gtZip.filter{ case (sc,gt) => gt == pg}.unzip._1))
      }}*/
      
      
      
      /*
      val (homRef,nonHomRef) = gtFilt.partition{ g => g.forall(gg => gg == "0") };
      val (cleanHet,nonCH) = nonHomRef.partition{ g => g(0) == "0" && g(1) == "1" }
      val (multHet,nonHet) = nonCH.partition{ g => g.contains("1") }
      val (homAlt,nonHomAlt) = nonHet.partition{ g => g.forall(gg => gg == "1") }
      val (multiAlleHet,multiAlleHom) = nonHomAlt.partition{ g => g.contains("0") }
      */
    }
        
    def getFinalMajorityThenFirst(gtFilt : Array[String],scFilt : Seq[String]) : (String,Int,Seq[String]) = {
      val total = gtFilt.length;
      val half = total / 2;
      
      val gtSet = gtFilt.distinct
      if(gtSet.length == 1){
        return (gtSet.head,0,scFilt)
      }
      val gtSetCts = gtSet.map{pg => (pg,gtFilt.count(_ == pg))}
      val gtZip = gtFilt.zip(scFilt);
      gtSetCts.find{ case (pg,cts) => cts > half}.foreach{ case (pg,cts) => {
        return ((pg,1, gtZip.filter{ case (sc,gt) => gt == pg}.unzip._1))
      }}
      
      return ((gtFilt.head,1,gtZip.filter{ case (sc,gt) => gt == gtFilt.head}.unzip._1))
      
      /*
      val (homRef,nonHomRef) = gtFilt.partition{ g => g.forall(gg => gg == "0") };
      val (cleanHet,nonCH) = nonHomRef.partition{ g => g(0) == "0" && g(1) == "1" }
      val (multHet,nonHet) = nonCH.partition{ g => g.contains("1") }
      val (homAlt,nonHomAlt) = nonHet.partition{ g => g.forall(gg => gg == "1") }
      val (multiAlleHet,multiAlleHom) = nonHomAlt.partition{ g => g.contains("0") }
      */
    }
    
  }
  
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  /*def multiSampleMergeVariants(vcIters : Seq[Iterator[SVcfVariantLine]], 
                            headers : Seq[SVcfHeader], 
                            inputVcfTypes : Seq[String], genomeFA : Option[String],
                            windowSize : Int = 200) :  (Iterator[SVcfVariantLine],SVcfHeader) = {
    val vcfCodes = VCFAnnoCodes();

    val genotypeOrdering = new Ordering[String]{
                        def compare(x : String, y : String) : Int = {
                          if(x == y) 0;
                          else if(x == ".") -1;
                          else if(y == ".") 1;
                          else {
                            val xi = string2int(x);
                            val yi = string2int(y);
                            if(xi < yi) -1;
                            else 1;
                          }
                        }
                      }
    
    val outputFmtTags : scala.collection.mutable.Map[String,SVcfCompoundHeaderLine] = new scala.collection.mutable.AnyRefMap[String,SVcfCompoundHeaderLine]();
    val fmtTags: Map[String,Set[String]] = headers.zip(inputVcfTypes).map{ case (h,t) => {
      ((t, h.formatLines.map{ fhl => {
           val ct = if(fhl.ID == "AD"){
             "R"
           } else {
             fhl.Number
           }
           val subType = if(fhl.subType == None){
             fhl.subType;
           } else if(fhl.ID == "AD"){
             Some(VcfTool.subtype_AlleleCounts)
           } else if(fhl.ID == "GT"){
             Some(VcfTool.subtype_GtStyle)
           } else {
             None
           }
           val chl = new SVcfCompoundHeaderLine(in_tag = "FORMAT",ID = t + "_" + fhl.ID, Number = ct, Type = fhl.Type, desc = cleanQuotes(fhl.desc),subType=subType)
           outputFmtTags.update(fhl.ID,chl)
           (fhl.ID)
      }}.toSet))
    }}.toMap
    val unionFmt = fmtTags.flatMap{ case (t,fset) => {
      fset
    }}.toVector.distinct

    
    val masterCallerInfoTags : Map[String,Set[String]] = inputVcfTypes.map{ mc => {
          val masterIdx = inputVcfTypes.indexOf(mc)
          val masterHeader = headers(masterIdx);
          (mc,masterHeader.infoLines.map{ infoLine => {
            //val num = if(infoLine.Number == "R" || infoLine.Number == "G" || infoLine.Number == "A") "." else infoLine.Number;
            val num = if(infoLine.Number == "G") "." else infoLine.Number;
            val chl = new SVcfCompoundHeaderLine(in_tag = "FORMAT",ID = infoLine.ID, Number = num, Type = infoLine.Type, desc = cleanQuotes(infoLine.desc), subType = infoLine.subType)
            infoLine.ID
          }}.toSet)
    }}.toMap
    val unionInfo = masterCallerInfoTags.flatMap{ case (t,sset) => {
      sset
    }}.toVector.distinct
    
    
    
    val customInfo = Seq[SVcfCompoundHeaderLine](
      new SVcfCompoundHeaderLine("INFO",  vcfCodes.ec_alle_callerSets,   "1", "String", "For each alt allele, which callers included the given allele."),
      new SVcfCompoundHeaderLine("INFO",  "SWH_EC_duplicateCt",   "1", "Integer", "The Number of duplicates found for this variant including this one."),
      new SVcfCompoundHeaderLine("INFO",  "SWH_EC_duplicateIdx",   "1", "Integer", "The index of this duplicate. If there are no duplicates of this variant this will just be equal to 0.")
    );//SWH_EC_duplicateIdx
    val customFmt = Seq[SVcfCompoundHeaderLine](
      new SVcfCompoundHeaderLine("FORMAT",  "GT",   "1", "String", "Final Genotype Call.")
    )
    
    val vcfHeader = headers.head.copyHeader;
    vcfHeader.infoLines = Seq[SVcfCompoundHeaderLine]();
    vcfHeader.formatLines = Seq[SVcfCompoundHeaderLine]();
    vcfHeader.addWalkerLikeCommand("multiSampleMergeVariants",Seq[(String,String)](("inputVcfTypes",inputVcfTypes.mkString("|"))))
    
    /*
    inputVcfTypes.foreach{ mc => {
       vcfHeader.addInfoLine(
           new SVcfCompoundHeaderLine(in_tag = "INFO",ID = "SWH_ECINFO_QUAL_"+ mc, Number = "1", Type = "Float", desc = "QUAL field for caller "+mc)
       );
       vcfHeader.addInfoLine(
           new SVcfCompoundHeaderLine(in_tag = "INFO",ID = "SWH_ECINFO_ID_"+ mc, Number = "1", Type = "String", desc = "ID field for caller "+mc)
       );
       vcfHeader.addInfoLine(
           new SVcfCompoundHeaderLine(in_tag = "INFO",ID = "SWH_ECINFO_FILTER_"+ mc, Number = "1", Type = "String", desc = "FILTER field for caller "+mc)
       );
    }}*/
    customInfo.foreach{ infoLine => {
      vcfHeader.addInfoLine(infoLine);
    }}
    customFmt.foreach{ infoLine => {
      vcfHeader.addFormatLine(infoLine);
    }}

     
    (unionFmt ++ unionInfo).foreach{ fmtID => {
      outputFmtTags.foreach{ case (tagID,headerLine) => {
        vcfHeader.addFormatLine(headerLine);
      }}
    }}
    
    
    val sampNames = headers.flatMap{h => h.titleLine.sampleList};
    val sampCt = sampNames.length;
    
    //vcfHeader
    val bufIters : Seq[BufferedIterator[SVcfVariantLine]] = vcIters.map{_.buffered};
    
    //def mergeGroupBySpan[A,B](iterSeq : Seq[BufferedIterator[A]])(f : (A => B))(implicit ord : math.Ordering[B]) : Iterator[Seq[Seq[A]]]
    //groupBySpanMulti[A,B,C](iterSeq : Seq[BufferedIterator[A]])(g : A => C, f : (A => B))(implicit ord : math.Ordering[B], ordG : math.Ordering[C])
    //val sortedGroupIters : BufferedIterator[Seq[Seq[SVcfVariantLine]]] = (mergeGroupBySpanWithSupergroup(bufIters){vc => vc.chrom}{vc => vc.pos}).buffered;
    val sortedGroupIters : BufferedIterator[Seq[Seq[SVcfVariantLine]]] = groupBySpanMulti(bufIters)(v => v.chrom)(v => v.pos).buffered;
    
    ((sortedGroupIters.flatMap{ posSeqSeq : Seq[Seq[SVcfVariantLine]] => {
      val swaps = posSeqSeq.flatMap{ vcSeq => vcSeq.map{ vc => (vc.ref, vc.alt.head)}}.distinct.sorted;
      
      swaps.flatMap{ case (currRef,currAlt) => {
        val vcSeqSeq : Seq[Seq[SVcfVariantLine]] = posSeqSeq.map{ posSeq => posSeq.filter{vc => vc.ref == currRef && vc.alt.head == currAlt}};
        
        val finalGt = Array.fill[String](sampCt)("./.");
        var callerList = Set[String]();
        var sampleCallerList = Array.fill[Set[String]](sampCt)(Set[String]());
        
        val exVc = vcSeqSeq.find(vcSeq => ! vcSeq.isEmpty).get.head
        val chrom = exVc.chrom;
        val pos = exVc.pos;
        val id = exVc.id;
        val ref = currRef;
        val alt = if(vcSeqSeq.exists{ vcSeq => vcSeq.exists{ vc => vc.alt.length > 1 }}){
          Seq[String](exVc.alt.head,"*");
        } else {
          Seq[String](currAlt)
        }
        val numAlle = alt.length;
        val filt = exVc.filter;
        val qual = exVc.qual;
        
        val vb = SVcfOutputVariantLine(
           in_chrom = chrom,
           in_pos = pos,
           in_id = id,
           in_ref = ref,
           in_alt = alt,
           in_qual = qual,
           in_filter = filt,
           in_info = Map[String,Option[String]](),
           in_format = Seq[String](),
           in_genotypes = SVcfGenotypeSet(Seq[String]("GT"), Array.fill(1,sampCt)("."))
        )
        var callerSupport = Set[String]();
        
        vcSeqSeq.zip(inputVcfTypes).filter{ case (vcSeq,callerName) => vcSeq.length > 0 }.map{ case (vcSeq,callerName) => {
          val currInfoLines = masterCallerInfoTags(callerName);
          val currFmtLines = fmtTags(callerName);
          vcSeq.headOption.foreach{ vc => {
            callerSupport = callerSupport + callerName;
            vc.info.map{ case (oldTag,fieldVal) => {
              val (newTag,infoLine) = currInfoLines(oldTag);
              vb.addInfoOpt(newTag,fieldVal);
            }}
            vc.genotypes.fmt.zipWithIndex.foreach{ case (oldTag,idx) => {
              if(! currFmtLines.contains(oldTag)){
                warning("Fatal error: Tag: "+oldTag+" not found in given header!\n Found header tags: "+currFmtLines.keys.toSeq.sorted.mkString(","),"PREERROR_WARNING",-1)
              }
              val (newTag,fmtLine) = currFmtLines(oldTag);
              vb.genotypes.addGenotypeArray(newTag,vc.genotypes.genotypeValues(idx));
            }}
            val currGt = vc.genotypes.genotypeValues(0);
            currGt.zipWithIndex.foreach{ case (gtString,i) => {
              val gt = gtString.split("[\\|/]").sorted(genotypeOrdering).mkString("/");
              if(finalGt(i) == "./."){
                finalGt(i) = gt;
              } else {
                //do nothing, higher priority call takes precedence!
              }
            }}
            vb.genotypes.genotypeValues(0) = finalGt
            
          }}
          vcSeq.tail.foreach{ vc => {
            val currGt = vc.genotypes.genotypeValues(0);
            currGt.zipWithIndex.foreach{ case (gtString,i) => {
              val gt = gtString.split("[\\|/]").sorted(genotypeOrdering).mkString("/");
              if(finalGt(i) == "./."){
                finalGt(i) = gt;
              } else {
                //do nothing, higher priority call takes precedence!
              }
            }}
            vb.genotypes.genotypeValues(0) = finalGt
          }}
        }}
        vb.addInfo(vcfCodes.ec_alle_callerSets,callerSupport.toVector.sorted.mkString(","));
        
        val vbSeq = Seq(vb) ++ vcSeqSeq.zip(inputVcfTypes).withFilter{case (vcSeq,callerName) => vcSeq.length > 1}.flatMap{ case (vcSeq,callerName) => {
          val newInfoFieldSet = masterCallerInfoTags(callerName);
          val newFmtFieldSet = fmtTags(callerName);
          //val newInfoFieldSet = currInfoLines.map{ case (t,(v,x)) => v }.toSet;
          //val newFmtFieldSet = currInfoLines.map{ case (t,(v,x)) => v }.toSet;
          warning("Warning: duplicate lines found for caller: "+callerName,"DUPLICATE_VCF_LINES_"+callerName,10)
          
          vcSeq.tail.map{ vc => {
            
            val vbb = SVcfOutputVariantLine(
             in_chrom = vb.chrom,
             in_pos = vb.pos,
             in_id = vb.id,
             in_ref = vb.ref,
             in_alt = vb.alt,
             in_qual = vb.qual,
             in_filter = vb.filter,
             in_info = vb.info,
             in_format = vb.genotypes.fmt,
             in_genotypes = SVcfGenotypeSet(vb.genotypes.fmt,vb.genotypes.genotypeValues.map{_.clone()})
            )
            vbb.in_info = vbb.info.filter{ case (newTag,fieldValue) => ! newInfoFieldSet.contains(newTag) }
            vbb.genotypes.genotypeValues = vbb.genotypes.genotypeValues.zip(vbb.genotypes.fmt).withFilter{ case (garray,fmt) => {
              ! newFmtFieldSet.contains(fmt);
            }}.map{ case (garray,fmt) => garray }
            vbb.genotypes.fmt = vbb.genotypes.fmt.filter{ fmt => {
              ! newFmtFieldSet.contains(fmt);
            }}
            vc.info.map{ case (oldTag,fieldVal) => {
              val (newTag,infoLine) = currInfoLines(oldTag);
              vbb.addInfoOpt(newTag,fieldVal);
            }}
            vc.genotypes.fmt.zipWithIndex.foreach{ case (oldTag,idx) => {
              val (newTag,fmtLine) = currFmtLines(oldTag);
              vbb.genotypes.addGenotypeArray(newTag,vc.genotypes.genotypeValues(idx));
            }}
            vbb
          }}
        }}
        vbSeq.zipWithIndex.map{ case (vbb,vbbIdx) => {
          vbb.addInfo("SWH_EC_duplicateCt",vbSeq.length.toString);
          vbb.addInfo("SWH_EC_duplicateIdx",vbbIdx.toString);
        }}
        vbSeq;
      }}
      
    }}), vcfHeader)
    
  }*/
  
  /////////////////////////////////////////  /////////////////////////////////////////  /////////////////////////////////////////




  def ensembleMergeVariants(vcIters : Seq[Iterator[SVcfVariantLine]], 
                            headers : Seq[SVcfHeader], 
                            inputVcfTypes : Seq[String], genomeFA : Option[String],
                            windowSize : Int = 200) :  (Iterator[SVcfVariantLine],SVcfHeader) = {
    val vcfCodes = VCFAnnoCodes();

    val genotypeOrdering = new Ordering[String]{
                        def compare(x : String, y : String) : Int = {
                          if(x == y) 0;
                          else if(x == ".") -1;
                          else if(y == ".") 1;
                          else {
                            val xi = string2int(x);
                            val yi = string2int(y);
                            if(xi < yi) -1;
                            else 1;
                          }
                        }
                      }
    
    val fmtTags: Map[String,Map[String,(String,SVcfCompoundHeaderLine)]] = headers.zip(inputVcfTypes).map{ case (h,t) => {
      ((t, h.formatLines.map{ fhl => {
           val ct = if(fhl.ID == "AD"){
             "R"
           } else {
             fhl.Number
           }
           val subType = if(fhl.subType == None){
             fhl.subType;
           } else if(fhl.ID == "AD"){
             Some(VcfTool.subtype_AlleleCounts)
           } else if(fhl.ID == "GT"){
             Some(VcfTool.subtype_GtStyle)
           } else {
             None
           }
           (fhl.ID, (t + "_" + fhl.ID,new SVcfCompoundHeaderLine(in_tag = "FORMAT",ID = t + "_" + fhl.ID, Number = ct, Type = fhl.Type, desc = "For the caller "+t+", " + cleanQuotes(fhl.desc),subType=subType)))
      }}.toMap))
    }}.toMap
    
    val masterCallerInfoTags : Map[String,Map[String,(String,SVcfCompoundHeaderLine)]] = inputVcfTypes.map{ mc => {
          val masterIdx = inputVcfTypes.indexOf(mc)
          val masterHeader = headers(masterIdx);
          (mc,masterHeader.infoLines.map{ infoLine => {
            //val num = if(infoLine.Number == "R" || infoLine.Number == "G" || infoLine.Number == "A") "." else infoLine.Number;
            val num = if(infoLine.Number == "G") "." else infoLine.Number;
            (infoLine.ID,("SWH_"+ mc + "_" + infoLine.ID,new SVcfCompoundHeaderLine(in_tag = "INFO",ID = "SWH_"+ mc + "_" + infoLine.ID, Number = num, Type = infoLine.Type, desc = "For the caller "+mc+", " + cleanQuotes(infoLine.desc), subType = infoLine.subType)))
          }}.toMap)
    }}.toMap
    
    val customInfo = Seq[SVcfCompoundHeaderLine](
      new SVcfCompoundHeaderLine("INFO",  vcfCodes.ec_alle_callerSets,   "1", "String", "For each alt allele, which callers included the given allele."),
      new SVcfCompoundHeaderLine("INFO",  "SWH_EC_duplicateCt",   "1", "Integer", "The Number of duplicates found for this variant including this one."),
      new SVcfCompoundHeaderLine("INFO",  "SWH_EC_duplicateIdx",   "1", "Integer", "The index of this duplicate. If there are no duplicates of this variant this will just be equal to 0.")
    );//SWH_EC_duplicateIdx
    val customFmt = Seq[SVcfCompoundHeaderLine](
      new SVcfCompoundHeaderLine("FORMAT",  "GT",   "1", "String", "Final Genotype Call.")
    )
    
    val vcfHeader = headers.head.copyHeader;
    vcfHeader.infoLines = Seq[SVcfCompoundHeaderLine]();
    vcfHeader.formatLines = Seq[SVcfCompoundHeaderLine]();
    vcfHeader.addWalkerLikeCommand("ensembleMergeVariants",Seq[(String,String)](("inputVcfTypes",inputVcfTypes.mkString("|"))))
    
    /*
    inputVcfTypes.foreach{ mc => {
       vcfHeader.addInfoLine(
           new SVcfCompoundHeaderLine(in_tag = "INFO",ID = "SWH_ECINFO_QUAL_"+ mc, Number = "1", Type = "Float", desc = "QUAL field for caller "+mc)
       );
       vcfHeader.addInfoLine(
           new SVcfCompoundHeaderLine(in_tag = "INFO",ID = "SWH_ECINFO_ID_"+ mc, Number = "1", Type = "String", desc = "ID field for caller "+mc)
       );
       vcfHeader.addInfoLine(
           new SVcfCompoundHeaderLine(in_tag = "INFO",ID = "SWH_ECINFO_FILTER_"+ mc, Number = "1", Type = "String", desc = "FILTER field for caller "+mc)
       );
    }}*/
    customInfo.foreach{ infoLine => {
      vcfHeader.addInfoLine(infoLine);
    }}
    customFmt.foreach{ infoLine => {
      vcfHeader.addFormatLine(infoLine);
    }}
    masterCallerInfoTags.foreach{ case (c,tagMap) => {
      tagMap.foreach{ case (oldTag,(newTag,headerLine)) => {
        vcfHeader.addInfoLine(headerLine);
      }}
    }}
    fmtTags.foreach{ case (c,tagMap) => {
      tagMap.foreach{ case (oldTag,(newTag,headerLine)) => {
        vcfHeader.addFormatLine(headerLine);
      }}
    }}
    
    
    val sampNames = vcfHeader.titleLine.sampleList;
    val sampCt = sampNames.length;
    
    //vcfHeader
    val bufIters : Seq[BufferedIterator[SVcfVariantLine]] = vcIters.map{_.buffered};
    
    //def mergeGroupBySpan[A,B](iterSeq : Seq[BufferedIterator[A]])(f : (A => B))(implicit ord : math.Ordering[B]) : Iterator[Seq[Seq[A]]]
    //groupBySpanMulti[A,B,C](iterSeq : Seq[BufferedIterator[A]])(g : A => C, f : (A => B))(implicit ord : math.Ordering[B], ordG : math.Ordering[C])
    //val sortedGroupIters : BufferedIterator[Seq[Seq[SVcfVariantLine]]] = (mergeGroupBySpanWithSupergroup(bufIters){vc => vc.chrom}{vc => vc.pos}).buffered;
    val sortedGroupIters : BufferedIterator[Seq[Seq[SVcfVariantLine]]] = groupBySpanMulti(bufIters)(v => v.chrom)(v => v.pos).buffered;
    
    ((sortedGroupIters.flatMap{ posSeqSeq : Seq[Seq[SVcfVariantLine]] => {
      val swaps = posSeqSeq.flatMap{ vcSeq => vcSeq.map{ vc => (vc.ref, vc.alt.head)}}.distinct.sorted;
      
      swaps.flatMap{ case (currRef,currAlt) => {
        val vcSeqSeq : Seq[Seq[SVcfVariantLine]] = posSeqSeq.map{ posSeq => posSeq.filter{vc => vc.ref == currRef && vc.alt.head == currAlt}};
        
        val finalGt = Array.fill[String](sampCt)("./.");
        var callerList = Set[String]();
        var sampleCallerList = Array.fill[Set[String]](sampCt)(Set[String]());
        
        val exVc = vcSeqSeq.find(vcSeq => ! vcSeq.isEmpty).get.head
        val chrom = exVc.chrom;
        val pos = exVc.pos;
        val id = exVc.id;
        val ref = currRef;
        val alt = if(vcSeqSeq.exists{ vcSeq => vcSeq.exists{ vc => vc.alt.length > 1 }}){
          Seq[String](exVc.alt.head,"*");
        } else {
          Seq[String](currAlt)
        }
        val numAlle = alt.length;
        val filt = exVc.filter;
        val qual = exVc.qual;
        
        val vb = SVcfOutputVariantLine(
           in_chrom = chrom,
           in_pos = pos,
           in_id = id,
           in_ref = ref,
           in_alt = alt,
           in_qual = qual,
           in_filter = filt,
           in_info = Map[String,Option[String]](),
           in_format = Seq[String](),
           in_genotypes = SVcfGenotypeSet(Seq[String]("GT"), Array.fill(1,sampCt)("."))
        )
        var callerSupport = Set[String]();
        
        vcSeqSeq.zip(inputVcfTypes).filter{ case (vcSeq,callerName) => vcSeq.length > 0 }.map{ case (vcSeq,callerName) => {
          val currInfoLines = masterCallerInfoTags(callerName);
          val currFmtLines = fmtTags(callerName);
          vcSeq.headOption.foreach{ vc => {
            callerSupport = callerSupport + callerName;
            vc.info.map{ case (oldTag,fieldVal) => {
              val (newTag,infoLine) = currInfoLines(oldTag);
              /*val fixedFieldVal = if(numAlle > 1 && vc.alt.length == 1 && (infoLine.Number == "R" || infoLine.Number == "A")){
                fieldVal + ",.";
              } else {
                fieldVal;
              }*/
              vb.addInfoOpt(newTag,fieldVal);
            }}
            vc.genotypes.fmt.zipWithIndex.foreach{ case (oldTag,idx) => {
              if(! currFmtLines.contains(oldTag)){
                warning("Fatal error: Tag: "+oldTag+" not found in given header!\n Found header tags: "+currFmtLines.keys.toSeq.sorted.mkString(","),"PREERROR_WARNING",-1)
              }
              val (newTag,fmtLine) = currFmtLines(oldTag);
              vb.genotypes.addGenotypeArray(newTag,vc.genotypes.genotypeValues(idx));
            }}
            val currGt = vc.genotypes.genotypeValues(0);
            currGt.zipWithIndex.foreach{ case (gtString,i) => {
              val gt = gtString.split("[\\|/]").sorted(genotypeOrdering).mkString("/");
              if(finalGt(i) == "./."){
                finalGt(i) = gt;
              } else {
                //do nothing, higher priority call takes precedence!
              }
            }}
            vb.genotypes.genotypeValues(0) = finalGt
            
          }}
          vcSeq.tail.foreach{ vc => {
            val currGt = vc.genotypes.genotypeValues(0);
            currGt.zipWithIndex.foreach{ case (gtString,i) => {
              val gt = gtString.split("[\\|/]").sorted(genotypeOrdering).mkString("/");
              if(finalGt(i) == "./."){
                finalGt(i) = gt;
              } else {
                //do nothing, higher priority call takes precedence!
              }
            }}
            vb.genotypes.genotypeValues(0) = finalGt
          }}
        }}
        vb.addInfo(vcfCodes.ec_alle_callerSets,callerSupport.toVector.sorted.mkString(","));
        
        val vbSeq = Seq(vb) ++ vcSeqSeq.zip(inputVcfTypes).withFilter{case (vcSeq,callerName) => vcSeq.length > 1}.flatMap{ case (vcSeq,callerName) => {
          val currInfoLines = masterCallerInfoTags(callerName);
          val currFmtLines = fmtTags(callerName);
          val newInfoFieldSet = currInfoLines.map{ case (t,(v,x)) => v }.toSet;
          val newFmtFieldSet = currInfoLines.map{ case (t,(v,x)) => v }.toSet;
          warning("Warning: duplicate lines found for caller: "+callerName,"DUPLICATE_VCF_LINES_"+callerName,10)
          
          vcSeq.tail.map{ vc => {
            
            val vbb = SVcfOutputVariantLine(
             in_chrom = vb.chrom,
             in_pos = vb.pos,
             in_id = vb.id,
             in_ref = vb.ref,
             in_alt = vb.alt,
             in_qual = vb.qual,
             in_filter = vb.filter,
             in_info = vb.info,
             in_format = vb.genotypes.fmt,
             in_genotypes = SVcfGenotypeSet(vb.genotypes.fmt,vb.genotypes.genotypeValues.map{_.clone()})
            )
            vbb.in_info = vbb.info.filter{ case (newTag,fieldValue) => ! newInfoFieldSet.contains(newTag) }
            vbb.genotypes.genotypeValues = vbb.genotypes.genotypeValues.zip(vbb.genotypes.fmt).withFilter{ case (garray,fmt) => {
              ! newFmtFieldSet.contains(fmt);
            }}.map{ case (garray,fmt) => garray }
            vbb.genotypes.fmt = vbb.genotypes.fmt.filter{ fmt => {
              ! newFmtFieldSet.contains(fmt);
            }}
            vc.info.map{ case (oldTag,fieldVal) => {
              val (newTag,infoLine) = currInfoLines(oldTag);
              vbb.addInfoOpt(newTag,fieldVal);
            }}
            vc.genotypes.fmt.zipWithIndex.foreach{ case (oldTag,idx) => {
              val (newTag,fmtLine) = currFmtLines(oldTag);
              vbb.genotypes.addGenotypeArray(newTag,vc.genotypes.genotypeValues(idx));
            }}
            vbb
          }}
        }}
        vbSeq.zipWithIndex.map{ case (vbb,vbbIdx) => {
          vbb.addInfo("SWH_EC_duplicateCt",vbSeq.length.toString);
          vbb.addInfo("SWH_EC_duplicateIdx",vbbIdx.toString);
        }}
        vbSeq;
      }}
      
    }}), vcfHeader)
    
  }
  
//INCOMPLETE!!!
  def mergeCompareVariants(vcIters : Seq[Iterator[SVcfVariantLine]], 
                            headers : Seq[SVcfHeader], 
                            inputVcfTypes : Seq[String], genomeFA : Option[String],
                            windowSize : Int = 200) :  (Iterator[SVcfVariantLine],SVcfHeader) = {
    val vcfCodes = VCFAnnoCodes();

    val genotypeOrdering = new Ordering[String]{
                        def compare(x : String, y : String) : Int = {
                          if(x == y) 0;
                          else if(x == ".") -1;
                          else if(y == ".") 1;
                          else {
                            val xi = string2int(x);
                            val yi = string2int(y);
                            if(xi < yi) -1;
                            else 1;
                          }
                        }
                      }
    
    val fmtTags: Map[String,Map[String,(String,SVcfCompoundHeaderLine)]] = headers.zip(inputVcfTypes).map{ case (h,t) => {
      ((t, h.formatLines.map{ fhl => {
           val ct = if(fhl.ID == "AD"){
             "R"
           } else {
             fhl.Number
           }
           val subType = if(fhl.subType == None){
             fhl.subType;
           } else if(fhl.ID == "AD"){
             Some(VcfTool.subtype_AlleleCounts)
           } else if(fhl.ID == "GT"){
             Some(VcfTool.subtype_GtStyle)
           } else {
             None
           }
           (fhl.ID, (t + "_" + fhl.ID,new SVcfCompoundHeaderLine(in_tag = "FORMAT",ID = t + "_" + fhl.ID, Number = ct, Type = fhl.Type, desc = "For the caller "+t+", " + cleanQuotes(fhl.desc),subType=subType)))
      }}.toMap))
    }}.toMap
    
    val masterCallerInfoTags : Map[String,Map[String,(String,SVcfCompoundHeaderLine)]] = inputVcfTypes.map{ mc => {
          val masterIdx = inputVcfTypes.indexOf(mc)
          val masterHeader = headers(masterIdx);
          (mc,masterHeader.infoLines.map{ infoLine => {
            //val num = if(infoLine.Number == "R" || infoLine.Number == "G" || infoLine.Number == "A") "." else infoLine.Number;
            val num = if(infoLine.Number == "G") "." else infoLine.Number;
            (infoLine.ID,("SWH_"+ mc + "_" + infoLine.ID,new SVcfCompoundHeaderLine(in_tag = "INFO",ID = "SWH_"+ mc + "_" + infoLine.ID, Number = num, Type = infoLine.Type, desc = "For the caller "+mc+", " + cleanQuotes(infoLine.desc), subType = infoLine.subType)))
          }}.toMap)
    }}.toMap
    
    val customInfo = Seq[SVcfCompoundHeaderLine](
      new SVcfCompoundHeaderLine("INFO",  vcfCodes.ec_alle_callerSets,   "1", "String", "For each alt allele, which callers included the given allele."),
      new SVcfCompoundHeaderLine("INFO",  "SWH_EC_duplicateCt",   "1", "Integer", "The Number of duplicates found for this variant including this one."),
      new SVcfCompoundHeaderLine("INFO",  "SWH_EC_duplicateIdx",   "1", "Integer", "The index of this duplicate. If there are no duplicates of this variant this will just be equal to 0.")
    );//SWH_EC_duplicateIdx
    val customFmt = Seq[SVcfCompoundHeaderLine](
      new SVcfCompoundHeaderLine("FORMAT",  "GT",   "1", "String", "Final Genotype Call.")
    )
    
    val vcfHeader = headers.head.copyHeader;
    vcfHeader.infoLines = Seq[SVcfCompoundHeaderLine]();
    vcfHeader.formatLines = Seq[SVcfCompoundHeaderLine]();
    vcfHeader.addWalkerLikeCommand("ensembleMergeVariants",Seq[(String,String)](("inputVcfTypes",inputVcfTypes.mkString("|"))))
    
    
    customInfo.foreach{ infoLine => {
      vcfHeader.addInfoLine(infoLine);
    }}
    customFmt.foreach{ infoLine => {
      vcfHeader.addFormatLine(infoLine);
    }}
    masterCallerInfoTags.foreach{ case (c,tagMap) => {
      tagMap.foreach{ case (oldTag,(newTag,headerLine)) => {
        vcfHeader.addInfoLine(headerLine);
      }}
    }}
    fmtTags.foreach{ case (c,tagMap) => {
      tagMap.foreach{ case (oldTag,(newTag,headerLine)) => {
        vcfHeader.addFormatLine(headerLine);
      }}
    }}
    
    val sampNames = vcfHeader.titleLine.sampleList;
    val sampCt = sampNames.length;
    
    //vcfHeader
    val bufIters : Seq[BufferedIterator[SVcfVariantLine]] = vcIters.map{_.buffered};
    
    //def mergeGroupBySpan[A,B](iterSeq : Seq[BufferedIterator[A]])(f : (A => B))(implicit ord : math.Ordering[B]) : Iterator[Seq[Seq[A]]]
    //groupBySpanMulti[A,B,C](iterSeq : Seq[BufferedIterator[A]])(g : A => C, f : (A => B))(implicit ord : math.Ordering[B], ordG : math.Ordering[C])
    //val sortedGroupIters : BufferedIterator[Seq[Seq[SVcfVariantLine]]] = (mergeGroupBySpanWithSupergroup(bufIters){vc => vc.chrom}{vc => vc.pos}).buffered;
    val sortedGroupIters : BufferedIterator[Seq[Seq[SVcfVariantLine]]] = groupBySpanMulti(bufIters)(v => v.chrom)(v => v.pos).buffered;
    
    ((sortedGroupIters.flatMap{ posSeqSeq : Seq[Seq[SVcfVariantLine]] => {
      val swaps = posSeqSeq.flatMap{ vcSeq => vcSeq.map{ vc => (vc.ref, vc.alt.head)}}.distinct.sorted;
      
      swaps.flatMap{ case (currRef,currAlt) => {
        val vcSeqSeq : Seq[Seq[SVcfVariantLine]] = posSeqSeq.map{ posSeq => posSeq.filter{vc => vc.ref == currRef && vc.alt.head == currAlt}};
        
        val finalGt = Array.fill[String](sampCt)("./.");
        var callerList = Set[String]();
        var sampleCallerList = Array.fill[Set[String]](sampCt)(Set[String]());
        
        val exVc = vcSeqSeq.find(vcSeq => ! vcSeq.isEmpty).get.head
        val chrom = exVc.chrom;
        val pos = exVc.pos;
        val id = exVc.id;
        val ref = currRef;
        val alt = if(vcSeqSeq.exists{ vcSeq => vcSeq.exists{ vc => vc.alt.length > 1 }}){
          Seq[String](exVc.alt.head,"*");
        } else {
          Seq[String](currAlt)
        }
        val numAlle = alt.length;
        val filt = exVc.filter;
        val qual = exVc.qual;
        
        val vb = SVcfOutputVariantLine(
           in_chrom = chrom,
           in_pos = pos,
           in_id = id,
           in_ref = ref,
           in_alt = alt,
           in_qual = qual,
           in_filter = filt,
           in_info = Map[String,Option[String]](),
           in_format = Seq[String](),
           in_genotypes = SVcfGenotypeSet(Seq[String]("GT"), Array.fill(1,sampCt)("."))
        )
        var callerSupport = Set[String]();
        
        vcSeqSeq.zip(inputVcfTypes).filter{ case (vcSeq,callerName) => vcSeq.length > 0 }.map{ case (vcSeq,callerName) => {
          val currInfoLines = masterCallerInfoTags(callerName);
          val currFmtLines = fmtTags(callerName);
          vcSeq.headOption.foreach{ vc => {
            callerSupport = callerSupport + callerName;
            vc.info.map{ case (oldTag,fieldVal) => {
              val (newTag,infoLine) = currInfoLines(oldTag);
              /*val fixedFieldVal = if(numAlle > 1 && vc.alt.length == 1 && (infoLine.Number == "R" || infoLine.Number == "A")){
                fieldVal + ",.";
              } else {
                fieldVal;
              }*/
              vb.addInfoOpt(newTag,fieldVal);
            }}
            vc.genotypes.fmt.zipWithIndex.foreach{ case (oldTag,idx) => {
              if(! currFmtLines.contains(oldTag)){
                warning("Fatal error: Tag: "+oldTag+" not found in given header!\n Found header tags: "+currFmtLines.keys.toSeq.sorted.mkString(","),"PREERROR_WARNING",-1)
              }
              val (newTag,fmtLine) = currFmtLines(oldTag);
              vb.genotypes.addGenotypeArray(newTag,vc.genotypes.genotypeValues(idx));
            }}
            val currGt = vc.genotypes.genotypeValues(0);
            currGt.zipWithIndex.foreach{ case (gtString,i) => {
              val gt = gtString.split("[\\|/]").sorted(genotypeOrdering).mkString("/");
              if(finalGt(i) == "./."){
                finalGt(i) = gt;
              } else {
                //do nothing, higher priority call takes precedence!
              }
            }}
            vb.genotypes.genotypeValues(0) = finalGt
            
          }}
          vcSeq.tail.foreach{ vc => {
            val currGt = vc.genotypes.genotypeValues(0);
            currGt.zipWithIndex.foreach{ case (gtString,i) => {
              val gt = gtString.split("[\\|/]").sorted(genotypeOrdering).mkString("/");
              if(finalGt(i) == "./."){
                finalGt(i) = gt;
              } else {
                //do nothing, higher priority call takes precedence!
              }
            }}
            vb.genotypes.genotypeValues(0) = finalGt
          }}
        }}
        vb.addInfo(vcfCodes.ec_alle_callerSets,callerSupport.toVector.sorted.mkString(","));
        
        val vbSeq = Seq(vb) ++ vcSeqSeq.zip(inputVcfTypes).withFilter{case (vcSeq,callerName) => vcSeq.length > 1}.flatMap{ case (vcSeq,callerName) => {
          val currInfoLines = masterCallerInfoTags(callerName);
          val currFmtLines = fmtTags(callerName);
          val newInfoFieldSet = currInfoLines.map{ case (t,(v,x)) => v }.toSet;
          val newFmtFieldSet = currInfoLines.map{ case (t,(v,x)) => v }.toSet;
          warning("Warning: duplicate lines found for caller: "+callerName,"DUPLICATE_VCF_LINES_"+callerName,10)
          
          vcSeq.tail.map{ vc => {
            
            val vbb = SVcfOutputVariantLine(
             in_chrom = vb.chrom,
             in_pos = vb.pos,
             in_id = vb.id,
             in_ref = vb.ref,
             in_alt = vb.alt,
             in_qual = vb.qual,
             in_filter = vb.filter,
             in_info = vb.info,
             in_format = vb.genotypes.fmt,
             in_genotypes = SVcfGenotypeSet(vb.genotypes.fmt,vb.genotypes.genotypeValues.map{_.clone()})
            )
            vbb.in_info = vbb.info.filter{ case (newTag,fieldValue) => ! newInfoFieldSet.contains(newTag) }
            vbb.genotypes.genotypeValues = vbb.genotypes.genotypeValues.zip(vbb.genotypes.fmt).withFilter{ case (garray,fmt) => {
              ! newFmtFieldSet.contains(fmt);
            }}.map{ case (garray,fmt) => garray }
            vbb.genotypes.fmt = vbb.genotypes.fmt.filter{ fmt => {
              ! newFmtFieldSet.contains(fmt);
            }}
            vc.info.map{ case (oldTag,fieldVal) => {
              val (newTag,infoLine) = currInfoLines(oldTag);
              vbb.addInfoOpt(newTag,fieldVal);
            }}
            vc.genotypes.fmt.zipWithIndex.foreach{ case (oldTag,idx) => {
              val (newTag,fmtLine) = currFmtLines(oldTag);
              vbb.genotypes.addGenotypeArray(newTag,vc.genotypes.genotypeValues(idx));
            }}
            vbb
          }}
        }}
        vbSeq.zipWithIndex.map{ case (vbb,vbbIdx) => {
          vbb.addInfo("SWH_EC_duplicateCt",vbSeq.length.toString);
          vbb.addInfo("SWH_EC_duplicateIdx",vbbIdx.toString);
        }}
        vbSeq;
      }}
      
    }}), vcfHeader)
    
  }
  
  case class FixEnsemblMerge3(inputVCFs : Seq[String], inputVcfTypes : Seq[String], genomeFA : Option[String],
                              masterCaller : Seq[String], summaryFile : Option[String],
                              leftAlignAndTrimSecondary : Boolean = false,
                              windowSize : Int = 200) extends SVcfWalker {
    def walkerName : String = "FixEnsemblMerge3"
    val  splitSecondary : Boolean = true
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("inputVCFs",fmtList(inputVCFs)),
        ("inputVcfTypes",fmtList(inputVcfTypes)),
        ("masterCaller",masterCaller.padTo(1,".").mkString(",")),
        ("summaryFile",summaryFile.getOrElse("None"))
    )

    //val LEGAL_VCF_TYPES : Set[String] = Set("hc","fb","ug");
    
    //if(inputVcfTypes.exists(! LEGAL_VCF_TYPES.contains(_))){
    //  error("Illegal VCF TYPE. Must be one of: [\"" + LEGAL_VCF_TYPES.toSeq.sorted.mkString("\",\"") + "\"]");
    //}
    //LEGAL_VCF_TYPES.foreach(t => {
    //  if(inputVcfTypes.count(_ == t) > 1){
    //    error("Illegal VCF TYPES. CANNOT HAVE MORE THAN ONE VCF OF EACH TYPE. FOUND "+inputVcfTypes.count(_ == t) + " VCF's with given type = \""+t+"\"");
    //  }
    //})
    
    val fileList = inputVCFs.zip(inputVcfTypes);
    val readers = fileList.map{case (infile,t) => {
      if(infile.contains('|')){
        SVcfLine.readVcfs(infile.split("\\|").iterator.map{f => getLinesSmartUnzip(f).buffered}.buffered, withProgress = false)
      } else {
        SVcfLine.readVcf(getLinesSmartUnzip(infile), withProgress = false)
      }
    }}.map{ case (h,iter) => (iter,h) }
    
    
    val subWalkers : Seq[SVcfWalker] = Seq[SVcfWalker](AddVariantPosInfoWalker("VAK_MRG_RawVAR"),new AddVariantIdx("VAK_MRG_lineNum","Ensemble Merger internal raw line number.")) ++ (if(splitSecondary){
          Seq[SVcfWalker](SSplitMultiAllelics())
        } else {
          Seq[SVcfWalker]();
        }) ++ (if(leftAlignAndTrimSecondary){
          Seq[SVcfWalker](internalUtils.GatkPublicCopy.LeftAlignAndTrimWalker(genomeFa = genomeFA.get,windowSize=windowSize ))
        } else {
          Seq[SVcfWalker]();
        })
    val finalSubWalker : SVcfWalker =  chainSVcfWalkers(subWalkers)
    val finalReaders = readers.map{ case (vcIter,hdr) => {
      finalSubWalker.walkVCF(vcIter,hdr);
    }}
    val iteratorArray : Array[BufferedIterator[SVcfVariantLine]] = finalReaders.map{ case (vcIter,hdr) => {
      vcIter.buffered;
    }}.toArray
    val headers = finalReaders.map(_._2);
    
    val fmtTags = headers.zip(inputVcfTypes).map{ case (h,t) => {
      h.formatLines.map{ fhl => {
           val ct = if(fhl.ID == "AD"){
             "R"
           } else {
             fhl.Number
           }
           val subType = if(fhl.ID == "AD"){
             Some(VcfTool.subtype_AlleleCountsUnsplit)
           } else if(fhl.ID == "GT"){
             Some(VcfTool.subtype_GtStyleUnsplit)
           } else {
             None
           }
           (fhl.ID, new SVcfCompoundHeaderLine(in_tag = "FORMAT",ID = t + "_" + fhl.ID, Number = ct, Type = fhl.Type, desc = "For the caller "+t+", " + cleanQuotes(fhl.desc),subType=subType))
      }}.toSeq
    }}
    
    val genotypeOrdering = new Ordering[String]{
                        def compare(x : String, y : String) : Int = {
                          if(x == y) 0;
                          else if(x == ".") -1;
                          else if(y == ".") 1;
                          else {
                            val xi = string2int(x);
                            val yi = string2int(y);
                            if(xi < yi) -1;
                            else 1;
                          }
                        }
                      }
    
    //val masterCallerIdx = masterCaller match {
    //  case Some(mc) => {
    //    inputVcfTypes.indexOf(mc)
    //  }
    //  case None => None;
    //}
    val masterCallerInfoTags : Map[String,Seq[(String,String,SVcfCompoundHeaderLine)]] = masterCaller.map{ mc => {
          val masterIdx = inputVcfTypes.indexOf(mc)
          val masterHeader = headers(masterIdx);
          (mc,masterHeader.infoLines.map{ infoLine => {
            val num = if(infoLine.Number == "R" || infoLine.Number == "G" || infoLine.Number == "A") "." else infoLine.Number;
            (infoLine.ID,"SWH_"+ mc + "_" + infoLine.ID,new SVcfCompoundHeaderLine(in_tag = "INFO",ID = "SWH_"+ mc + "_" + infoLine.ID, Number = num, Type = infoLine.Type, desc = "For the caller "+mc+", " + cleanQuotes(infoLine.desc)))
          }}.toSeq)
    }}.toMap
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      val vcfCodes = VCFAnnoCodes();

      
      val customInfoLines : Seq[Seq[SVcfCompoundHeaderLine]]= inputVcfTypes.map{t => {
                                                Seq(
                                                    new SVcfCompoundHeaderLine("INFO", vcfCodes.ec_singleCallerAllePrefix+t, ".", "String", "Alt Alleles for caller "+(t))
                                                   )
                                        }}
      

      val extraInfoLines : Seq[SVcfCompoundHeaderLine]= Seq(
               new SVcfCompoundHeaderLine("INFO", vcfCodes.ec_CallMismatch,        "1", "Integer", "Num genotypes that actively disagree (ie called in different ways)"),
               new SVcfCompoundHeaderLine("INFO",  vcfCodes.ec_CallMismatchStrict, "1", "Integer", "Num genotypes that do not give the exact same call (including no-call vs call)"),
               new SVcfCompoundHeaderLine("INFO",  vcfCodes.ec_EnsembleWarnings,   ".", "String", "List of warnings related to the ensemble calling"),
               new SVcfCompoundHeaderLine("INFO",  vcfCodes.ec_alle_callerSets,   "A", "String", "For each alt allele, which callers included the given allele.")
          ) ++ masterCallerInfoTags.flatMap{ case (c,mct) => mct.map{case (oldTag,newTag,infoLine) => infoLine} }
          
          
      val extraFmtLines : Seq[SVcfCompoundHeaderLine]= Seq(
                new SVcfCompoundHeaderLine("FORMAT", "MISMATCH", "1", "String", "All callers do not actively disagree."),
                new SVcfCompoundHeaderLine("FORMAT", "MISMATCH_STRICT", "1", "String", "All callers provide the same call."),
                new SVcfCompoundHeaderLine("FORMAT", "CallerSupport", ".", "String", "List of callers that support the final call."),
                new SVcfCompoundHeaderLine("FORMAT", "HasSupport", "1", "String", "Final call is supported by at least one caller."),
                new SVcfCompoundHeaderLine("FORMAT", "ENS_WARN", ".", "String", "Warnings related to the ensemble calls.")
              );
      
      val customFmtLines : Seq[Seq[SVcfCompoundHeaderLine]] = inputVcfTypes.map{t =>{
                                                Seq(
                                                    new SVcfCompoundHeaderLine("FORMAT", t+"_GT_RAW", ".", "String", "Raw Genotype Call for caller "+t, extraFields = Seq((internalUtils.VcfTool.subtype_GtStyle,"")).toMap),
                                                    new SVcfCompoundHeaderLine("FORMAT", t+"_GT_FIX", "1", "String", "Recoded Genotype Call for caller "+t)
                                                )
                                        }}
      
      //val newFmtLines : Seq[SVcfCompoundHeaderLine]= fmtTags.flatMap{ newTagLines =>
      //                                  newTagLines.map(_._2)
      //} ++ customFmtLines.flatten ++ extraFmtLines;
      
      val newHeader = vcfHeader.copyHeader;
      (customInfoLines.flatten ++ extraInfoLines).foreach{ hl => {
        newHeader.addInfoLine(hl, walker = Some(this));
      }}
      //(newFmtLines).foreach{ hl => {
      //  newHeader.addFormatLine(hl, walker = Some(this));
      //}}
      fmtTags.zip(inputVcfTypes).foreach{ case (fmtTagList,c) => {
        fmtTagList.foreach{ case (oldTag,fmtLine) => {
          newHeader.addFormatLine(fmtLine,walker = Some(this));
        }}
      }}
      
      //val newHeader = SVcfHeader(infoLines = vcfHeader.infoLines ++ customInfoLines.flatten ++ extraInfoLines, 
      //                          formatLines = newFmtLines,
      //                          otherHeaderLines = vcfHeader.otherHeaderLines,
      //                           walkLines = vcfHeader.walkLines, 
      //                          titleLine = vcfHeader.titleLine);

      newHeader.addWalk(this);
      val sampNames = vcfHeader.titleLine.sampleList;
      val sampCt = sampNames.length;
      //var currIter = vcIter.buffered;
      
      /*
      val out = groupBySpan(vcIter)(vc => vc.pos).flatMap( vcSeq => {
        val currPos = vcSeq.head.pos;
        iteratorArray.indices.foreach{i => {
          //iteratorArray(i) = iteratorArray(i).dropWhile(vAlt => vAlt.pos < currPos);
          
        }}
        val otherVcAtPos = iteratorArray.indices.map{i => {
          val (a,r) = spanVector(iteratorArray(i))(vAlt => vAlt.pos == currPos);
          iteratorArray(i) = r;
          a
        }}*/
      //val out = vcFlatMap(vcIter)(
      val out = vcGroupedFlatMap(groupBySpan(vcIter.buffered)(vc => vc.pos))( vcSeq => {
        val currPos = vcSeq.head.pos;
        val currChrom = vcSeq.head.chrom;
        iteratorArray.indices.foreach{i => {
          //iteratorArray(i) = iteratorArray(i).dropWhile(vAlt => vAlt.pos < currPos);
          skipWhile(iteratorArray(i))(vAlt => vAlt.chrom != currChrom);
          skipWhile(iteratorArray(i))(vAlt => vAlt.pos < currPos && vAlt.chrom == currChrom);
        }}
        val otherVcAtPos = iteratorArray.indices.map{i => {
          extractWhile(iteratorArray(i))(vAlt => vAlt.pos == currPos && vAlt.chrom == currChrom);
        }}
        vcSeq.iterator.map(vc => {
          
          val vb = vc.getOutputLine();
          val altAlles = vc.alt;
          var ensembleWarnings = Set[String]();
          val ploidy = vc.genotypes.genotypeValues(0).map{ _.split("/").length }.max
          
          val alt = vc.alt.head;
          
          if(ploidy > 2){
            warning("Ploidy greater than 2! Ploidy = "+ploidy,"POLYPLOID",100)
          } else if(ploidy == 1){
            warning("Haploid! Ploidy = "+ploidy,"HAPLOID",100)
          }
          
          if(altAlles.length > 1){
            ensembleWarnings = ensembleWarnings + ("MULTIALLELIC");
          }
          
          var callerSets = Array.fill[Set[String]](altAlles.length)(Set[String]());
          var sampleWarn = Array.fill[Set[String]](sampCt)(Set[String]()); 
          
          otherVcAtPos.zip(fmtTags).zipWithIndex.foreach{ case ((otherLines,otherFmtTags),otherFileIdx) => {
            val otherFileType = inputVcfTypes(otherFileIdx);
            /*val matchIdx = otherLines.zipWithIndex.flatMap{ case (otherVC, otherLineIdx) => {
              otherVC.alt.zipWithIndex.filter{case (a,idx) => {a != "*"}}.flatMap{ case (otherAlle, otherAlleIdx) => {
                vb.alt.zipWithIndex.filter{case (alle,idx) => {alle == otherAlle}}.map{case (currAlle,currAlleIdx) => ((currAlleIdx,(otherLineIdx,otherAlleIdx)))}
              }}
            }}.toMap;
            val linesWithMatch = matchIdx.map{case (currAlleIdx,(otherLineIdx,otherAlleIdx)) => otherLineIdx}.toSet.toSeq.sorted;
            val linesWithoutMatch = (otherLines.indices.toSet -- linesWithMatch.toSet).toVector.sorted;
            */
            
            
            val numLinesWithMatch = otherLines.count{ otherLine => otherLine.alt.head == alt }
            
            //if(otherLines.length > 1){
            //  notice("Multiple lines found at location (Caller "+inputVcfTypes(otherFileIdx)+", POS="+vcSeq.head.chrom+":"+currPos+")","MULTILINE_LOCUS_"+otherFileType,5);
            //  ensembleWarnings = ensembleWarnings + ("MULTILINELOCUS_"+inputVcfTypes(otherFileIdx));
            //}
            
            if(numLinesWithMatch > 1){
              notice("Duplicate lines found at location that contain matches (Caller "+inputVcfTypes(otherFileIdx)+", POS="+vcSeq.head.chrom+":"+currPos+")","MULTIMATCHLINE_LOCUS_"+otherFileType,5);
            } else if(numLinesWithMatch == 0){
              //do nothing
            } else {
              
              otherLines.withFilter{ otherLine => otherLine.alt.head == alt }.foreach{ otherLine => {
                masterCallerInfoTags(otherFileType).map{ case (oldTag,newTag,infoLine) => {
                  otherLine.info.get(oldTag) match {
                    case Some(vv) => {
                      vb.addInfoOpt(newTag,vv);
                    }
                    case None => {
                      //do nothing
                    }
                  }
                }}
                fmtTags(otherFileIdx).map{ case (oldTag,fmtLine) => {
                  val fmtIdx = otherLine.genotypes.fmt.indexOf(oldTag);
                  val newTag = fmtLine.ID;
                  if(fmtIdx >= 0){
                    vb.genotypes.addGenotypeArray(newTag,otherLine.genotypes.genotypeValues(fmtIdx));
                  }
                }}
                
              }}
            }
          }}
          vb;
          
        })
      })
      
      (out,newHeader);
    }
    
  }
  
  
  class CommandFilterVCF extends CommandLineRunUtil {
     override def priority = 20;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "FilterVCF", 
          quickSynopsis = "", 
          synopsis = "", 
          description = "" + ALPHA_WARNING,
          argList = 
                    new BinaryOptionArgument[Int](
                                         name = "numLinesRead", 
                                         arg = List("--numLinesRead"), 
                                         valueName = "N",  
                                         argDesc =  "Limit file read to the first N lines"
                                        ) ::
                    new BinaryOptionArgument[List[String]](
                                         name = "chromList", 
                                         arg = List("--chromList"), 
                                         valueName = "chr1,chr2,...",  
                                         argDesc =  "List of chromosomes. If supplied, then all analysis will be restricted to these chromosomes. All other chromosomes wil be ignored."
                                        ) ::
                    new UnaryArgument( name = "infileList",
                                         arg = List("--infileList"), // name of value
                                         argDesc = "If this option is used, then instead of a single input file the input file(s) will be assumed "+
                                                   "to be a file containing a list of input files to parse in order. If multiple VCF files are specified, "+
                                                   "the vcf lines will be concatenated and the header will be taken from the first file."+
                                                   "" // description
                                       ) ::
                    new FinalArgument[String](
                                         name = "infile",
                                         valueName = "infile.vcf.gz",
                                         argDesc = "" // description
                                        ) ::
                    new FinalArgument[String](
                                         name = "variantFilterExpression",
                                         valueName = "expr",
                                         argDesc = "" // description
                                        ) ::
                    new FinalArgument[String](
                                         name = "outfile",
                                         valueName = "outfile.vcf.gz",
                                         argDesc = "The output file or a comma-delimited list of files. Can be gzipped or in plaintext."// description
                                        ) ::
                    internalUtils.commandLineUI.CLUI_UNIVERSAL_ARGS );

     def run(args : Array[String]) {
       val out = parser.parseArguments(args.toList.tail);
       if(out){
         VcfExpressionFilter(
             filterExpr = parser.get[String]("variantFilterExpression")
         ).walkVCFFiles(
             infiles = parser.get[String]("infile"),
             outfile = parser.get[String]("outfile"),
             chromList = parser.get[Option[List[String]]]("chromList"),
             numLinesRead = parser.get[Option[Int]]("numLinesRead"),
             inputFileList = parser.get[Boolean]("infileList")
         )
       }
     }
  }

  class CommandVcfToMatrix extends CommandLineRunUtil {
     override def priority = 20;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "VcfToMatrix", 
          quickSynopsis = "", 
          synopsis = "", 
          description = "" + ALPHA_WARNING,
          argList = 
                    new BinaryOptionArgument[Int](
                                         name = "numLinesRead", 
                                         arg = List("--numLinesRead"), 
                                         valueName = "N",  
                                         argDesc =  "Limit file read to the first N lines"
                                        ) ::
                    new BinaryOptionArgument[List[String]](
                                         name = "chromList", 
                                         arg = List("--chromList"), 
                                         valueName = "chr1,chr2,...",  
                                         argDesc =  "List of chromosomes. If supplied, then all analysis will be restricted to these chromosomes. All other chromosomes wil be ignored."
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "variantFile", 
                                         arg = List("--variantFile"), 
                                         valueName = "varfile.txt",  
                                         argDesc =  ""
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "variantFilterExpression", 
                                         arg = List("--variantFilterExpression"), 
                                         valueName = "...",  
                                         argDesc =  "Variant-level filter expression."
                                        ) ::
                    new UnaryArgument( name = "variantsAsRows",
                                         arg = List("--variantsAsRows"), // name of value
                                         argDesc = "..."+
                                                   "" // description
                                       ) ::
                    new UnaryArgument( name = "variantsAsColumns",
                                         arg = List("--variantsAsColumns"), // name of value
                                         argDesc = "Default behavior, this parameter has no effect."+
                                                   "" // description
                                       ) ::
                    new UnaryArgument( name = "writeTitleColumn",
                                         arg = List("--writeTitleColumn"), // name of value
                                         argDesc = "..."+
                                                   "" // description
                                       ) ::
                    new UnaryArgument( name = "writeHeaderRow",
                                         arg = List("--writeHeaderRow"), // name of value
                                         argDesc = "..."+
                                                   "" // description
                                       ) ::
                    new UnaryArgument( name = "dropInvariant",
                                         arg = List("--dropInvariant"), // name of value
                                         argDesc = "..."+
                                                   "" // description
                                       ) ::
                    new UnaryArgument( name = "keepMultiAlleHets",
                                         arg = List("--keepMultiAlleHets"), // name of value
                                         argDesc = "..."+
                                                   "" // description
                                       ) ::
                    new BinaryArgument[String](name = "naString",
                                           arg = List("--naString"),  
                                           valueName = "NA", 
                                           argDesc = "", 
                                           defaultValue = Some("NA")
                                           ) :: 
                    new BinaryArgument[String](name = "otherString",
                                           arg = List("--otherString"),  
                                           valueName = "NA", 
                                           argDesc = "", 
                                           defaultValue = Some("NA")
                                           ) :: 
                    new BinaryArgument[List[String]](name = "includeVariantTags",
                                           arg = List("--includeVariantTags"),  
                                           valueName = "Tag1,Tag2,...", 
                                           argDesc = "", 
                                           defaultValue = Some(List[String]())
                                           ) :: 
                    new BinaryArgument[List[String]](name = "dropSamples",
                                           arg = List("--dropSamples"),  
                                           valueName = "samp1,samp2,...", 
                                           argDesc = "", 
                                           defaultValue = Some(List[String]())
                                           ) :: 
                    new BinaryOptionArgument[String](name = "dropSampleFile",
                                           arg = List("--dropSampleFile"),  
                                           valueName = "dropSampFile.txt", 
                                           argDesc = ""
                                           ) :: 
                    new BinaryOptionArgument[String](
                                         name = "gtAnnoFilePrefix", 
                                         arg = List("--gtAnnoFilePrefix"), 
                                         valueName = "...",  
                                         argDesc =  ""
                                        ) ::
                    new BinaryArgument[List[String]](name = "gtAnnoTags",
                                           arg = List("--gtAnnoTags"),  
                                           valueName = "Tag1,Tag2,...", 
                                           argDesc = "", 
                                           defaultValue = Some(List[String]())
                                           ) :: 
                    new BinaryArgument[String](name = "delimString",
                                           arg = List("--delimString"),  
                                           valueName = "\\t", 
                                           argDesc = "", 
                                           defaultValue = Some("\t")
                                           ) :: 
                    new BinaryArgument[String](name = "gtTagString",
                                           arg = List("--gtTagString"),  
                                           valueName = "\\t", 
                                           argDesc = "", 
                                           defaultValue = Some("GT")
                                           ) ::   
                                           
                    new FinalArgument[String](
                                         name = "infile",
                                         valueName = "infile.vcf.gz",
                                         argDesc = "" // description
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
         VcfToMatrix(
             variantsAsRows = parser.get[Boolean]("variantsAsRows"),
             columnDelim = parser.get[String]("delimString"),
             naString = parser.get[String]("naString"),
             multiAlleString = parser.get[String]("otherString"),
             writeHeader = parser.get[Boolean]("writeHeaderRow"),
             writeTitleColumn = parser.get[Boolean]("writeTitleColumn"),
             variantFile = parser.get[Option[String]]("variantFile"),
             gtTagString = parser.get[String]("gtTagString"),
             includeVariantTags = parser.get[List[String]]("includeVariantTags"),
             dropInvariant = parser.get[Boolean]("dropInvariant"),
             gtAnnoFilePrefix = parser.get[Option[String]]("gtAnnoFilePrefix"),
             gtAnnoTags = parser.get[List[String]]("gtAnnoTags"),
             dropSampleList = parser.get[List[String]]("dropSamples"),
             dropSampleFile = parser.get[Option[String]]("dropSampleFile"),
             keepMultiAlleHets = parser.get[Boolean]("keepMultiAlleHets")
         ).walkVCFFile(
             infile = parser.get[String]("infile"),
             outfile = parser.get[String]("outfile"),
             chromList = parser.get[Option[List[String]]]("chromList"),
             numLinesRead = parser.get[Option[Int]]("numLinesRead"),
             variantFilterExpression = parser.get[Option[String]]("variantFilterExpression")
         )
       }
     }
  }
  
  
  case class VcfToMatrix(variantsAsRows : Boolean = false, columnDelim : String = "\t", 
                         naString : String = "NA", multiAlleString : String = "NA",
                         writeHeader : Boolean = false, writeTitleColumn : Boolean = false,
                         variantFile : Option[String] = None,
                         gtTagString : String = "GT",
                         includeVariantTags : List[String] = List[String](),
                         dropInvariant : Boolean = false,
                         gtAnnoFilePrefix : Option[String] = None,
                         gtAnnoTags : List[String] = List[String](),
                         dropSampleList : List[String] = List[String](),
                         dropSampleFile : Option[String] = None,
                         keepMultiAlleHets : Boolean = true) {
    
    
    //val dropSamps = dropSampleList.toSet;
    val dropSamps  = dropSampleFile match {
      case Some(f) => {
        dropSampleList.toSet ++ getLinesSmartUnzip(f).toSet;
      }
      case None => {
        dropSampleList.toSet;
      }
    }
    
    val (gtAnnoWrite,gtAnnoWriters) = gtAnnoFilePrefix match {
      case Some(filePrefix) => {
        val w = gtAnnoTags.map{ tag => {
          openWriterSmart(filePrefix + "." + tag + ".txt.gz");
        }}
        ((vc : SVcfVariantLine, keepSamps : Seq[Boolean]) => {
          w.zip(gtAnnoTags).foreach{ case (writer,tag) => {
            val tagidx = vc.format.indexOf(tag);
            if(tagidx == -1){
              ".";
            } else {
              writer.write(vc.genotypes.genotypeValues(tagidx).zip(keepSamps).filter{ case (g,k) => k }.unzip._1.mkString(columnDelim)+"\n");
            }
          }}
        },w);
      }
      case None => {
        ((vc : SVcfVariantLine, keepSamps : Seq[Boolean]) => {
          //do nothing
        },Seq())
      }
    }
    
    def walkVCFFile(infile : String, outfile : String, 
                    variantFilterExpression : Option[String] = None,
                    chromList : Option[List[String]],numLinesRead : Option[Int],
                    allowVcfList : Boolean = true){
      
      val indata = if(infile.contains(',') && allowVcfList){
        val infiles = infile.split(",");
        val allInputLines = flattenIterators(infiles.iterator.map{inf => addIteratorCloseAction(iter =getLinesSmartUnzip(inf), closeAction = (() => {reportln("finished reading file: "+inf,"note")}))}).buffered
        val headerLines = extractWhile(allInputLines)( a => a.startsWith("#"));
        val remainderLines = allInputLines.filter( a => ! a.startsWith("#"));
        headerLines.iterator ++ remainderLines;
      } else {
        getLinesSmartUnzip(infile)
      }
      
      val (vcfHeader,vcIter) = if(chromList.isEmpty){
        SVcfLine.readVcf(indata,withProgress = true)
      } else if(chromList.get.length == 1){
        val chrom = chromList.get.head;
        SVcfLine.readVcf(indata.filter{line => {
          line.startsWith(chrom+"\t") || line.startsWith("#")
        }},withProgress = true)
      } else {
        val chromSet = chromList.get.toSet;
        val (vh,vi) = SVcfLine.readVcf(indata,withProgress = true)
        (vh,vi.filter(line => { chromSet.contains(line.chrom) }))
      } 
      
      
      val (vcIter2,vcfHeader2) = variantFilterExpression match {
        case Some(expr) => {
          VcfExpressionFilter(expr).walkVCF(vcIter,vcfHeader)
        }
        case None => {
          (vcIter,vcfHeader)
        }
      }
      
      val vcIter3 = if(numLinesRead.isDefined){
        vcIter2.take(numLinesRead.get);
      } else {
        vcIter2
      }
      
      val lineIter = walkVCF(vcIter3,vcfHeader2,verbose=true);
      
      var lnct = 0;
      val writer = openWriterSmart(outfile);
      lineIter.foreach{line => {
        lnct = lnct + 1;
        writer.write(line+"\n");
      }}
      writer.close();
      if(varWriter.isDefined){
        varWriter.get.close();
      }
      gtAnnoWriters.foreach{ w => w.close()}
      reportln("Wrote "+lnct + " lines.","note");
    }
    
    val varWriter : Option[WriterUtil] = variantFile match {
      case Some(f) => {
          val writer = openWriterSmart(f);
          Some(writer);
      }
      case None => None;
    }
    val writeVar : (String => Unit) = varWriter match {
      case Some(w) => {
        (s : String) => {
          w.write(s);
        }
      }
      case None => {
        (s : String) => {
          //do nothing
        }
      }
    }
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : Iterator[String] = {
      val sampleList = vcfHeader.titleLine.sampleList
      val keepSamps = sampleList.map{ s => ! dropSamps.contains(s)}
      val keepSampList = sampleList.filter{s => ! dropSamps.contains(s)}
      
      if(variantsAsRows){
        (
          if(writeHeader){
            writeVar((Seq("chrom","pos","id","ref","alt","qual","filter") ++ includeVariantTags).mkString(columnDelim) + "\n");
            gtAnnoWriters.foreach{ w => w.write(keepSampList.mkString(columnDelim) + "\n") }
            Iterator[String](keepSampList.mkString(columnDelim))
          } else {
            Iterator[String]();
          }
        ) ++ vcIter.zipWithIndex.flatMap{ case (vc,i) => {
          val gtIdx = vc.format.indexOf(gtTagString);
          if(dropInvariant && ! vc.genotypes.genotypeValues(gtIdx).zip(keepSamps).filter{ case (g,k) => k }.unzip._1.exists{ g => g == "0/1" || g == "1/1" }){
            None
          } else {
            writeVar(vc.getVcfTable(includeVariantTags=includeVariantTags)+"\n");
            gtAnnoWrite(vc,keepSamps);
            Some((if(writeTitleColumn){ i+columnDelim } else {""}) + vc.genotypes.genotypeValues(gtIdx).zip(keepSamps).filter{ case (g,k) => k }.unzip._1.map{ g => {
              if(g == "./." || g == "."){
                naString
              } else if(g == "0/0" || g == "0"){
                "0"
              } else if(g == "0/1" || (keepMultiAlleHets && g == "1/2")){
                "1"
              } else if(g == "1/1" || g == "1"){
                "2"
              } else {
                multiAlleString
              }
            }}.mkString(columnDelim));
          }
        }}
      } else {
        val (genoArray,varArray) = vcIter.flatMap{ vc => {
          val gtIdx = vc.format.indexOf(gtTagString);
          if(dropInvariant && ! vc.genotypes.genotypeValues(gtIdx).exists{ g => g == "0/1" || g == "1/1" }){
            None
          } else {
            Some((vc.genotypes.genotypeValues(gtIdx).map{ g => {
              if(g == "./." || g == "."){
                naString
              } else if(g == "0/0" || g == "0"){
                "0"
              } else if(g == "0/1" || (keepMultiAlleHets && g == "1/2")){
                "1"
              } else if(g == "1/1" || g == "1"){
                "2"
              } else {
                multiAlleString
              }
            }},
              vc.getVcfTable(includeVariantTags=includeVariantTags)
            ))
          }
        }}.toVector.unzip
        if(genoArray.isEmpty){
          warning("Writing 0 variants!","WRITING_ZERO_VARIANTS",-1);
          Iterator[String]();
        } else {
          reportln("Writing "+genoArray.length+" variants, "+genoArray(0).length+" samples.","note");
          if(writeHeader){ 
            writeVar((Seq("chrom","pos","id","ref","alt","qual","filter") ++ includeVariantTags).mkString("\t") + "\n");
          }
          varArray.foreach{v => {
                writeVar(v + "\n");
          }}
          (
            if(writeHeader){
              Iterator[String](genoArray(0).indices.mkString(columnDelim))
            } else {
              Iterator[String]();
            }
          ) ++ genoArray(0).indices.iterator.map{ j => {
            (if(writeTitleColumn){ sampleList(j) + columnDelim} else {""})+ genoArray.indices.map{ i => {
              genoArray(i)(j)
            }}.mkString(columnDelim)
          }}
        }
        
      }
    }
  }
  
  case class VcfExpressionFilter(filterExpr : String, explainFile : Option[String] = None) extends SVcfWalker {

    /*def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
       (vcIter, vcfHeader)
    }*/
    def walkerName : String = "VcfExpressionFilter"
    def walkerParams : Seq[(String,String)]= Seq[(String,String)](
        ("filterExpr","\""+filterExpr+"\""),
        ("explainFile","\""+explainFile.getOrElse("None")+"\"")
    )
    val parser : SFilterLogicParser[SVcfVariantLine] = internalUtils.VcfTool.sVcfFilterLogicParser;
    val filter : SFilterLogic[SVcfVariantLine] = parser.parseString(filterExpr);
    //val parser = internalUtils.VcfTool.SVcfFilterLogicParser();
    //val filter = parser.parseString(filterExpr);
    reportln("Parsed filter:\n   "+filter.printTree(),"note");
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
       val outHeader = vcfHeader.copyHeader;
       outHeader.addWalk(this);
       (vcFlatMap(vcIter){ vc => {
         val k = filter.keep(vc);
         if(!k){
           notice("DROP variant due to VCF filter:\n    "+vc.getSimpleVcfString(),"DROP_VARIANT_FOR_FILTER",5);
           None
         } else {
           notice("KEEP variant due to VCF filter:\n    "+vc.getSimpleVcfString(),"KEEP_VARIANT_FOR_FILTER",5);
           Some(vc);
         }
       }}, outHeader)
    }
  }

  //        vc.genotypes.sampList = sampList;
  //      vc.genotypes.sampGrp = Some(sampleToGroupMap);
  
  case class VcfGtExpressionTag( expr : String, tagID : String, tagDesc : String, styleOpt : Option[String] = Some("CT"),
                                 groupFile : Option[String] = None, groupList : Option[String] = None, superGroupList  : Option[String] = None ) extends SVcfWalker {
    def walkerName : String = "VcfGtExpressionTag."+tagID;
    val style = styleOpt.getOrElse("CT")
    def walkerParams : Seq[(String,String)]= Seq[(String,String)](
        ("expr","\""+expr+"\""),
        ("tagID","\""+tagID+"\""),
        ("tagDesc","\""+tagDesc+"\""),
        ("style","\""+style+"\"")
    )
    val parser : SFilterLogicParser[(SVcfVariantLine,Int)] = internalUtils.VcfTool.sGenotypeFilterLogicParser;
    val filter : SFilterLogic[(SVcfVariantLine,Int)] = parser.parseString(expr);
    val styleType = if(style == "CT"){
      "Integer"
    } else {
      "Float"
    }
    if(!Set("CT","PCT","FRAC","GTTAG").contains(style)){
      error("Unrecognized/invalid gt expression style: \""+style+"\"")
    }
    
    val (sampleToGroupMap,groupToSampleMap,groups) : (scala.collection.mutable.AnyRefMap[String,Set[String]],
                           scala.collection.mutable.AnyRefMap[String,Set[String]],
                           Vector[String]) = getGroups(groupFile, groupList, superGroupList);
    
    
    
    //val parser = internalUtils.VcfTool.SVcfFilterLogicParser();
    //val filter = parser.parseString(filterExpr);
    reportln("Parsed filter:\n   "+filter.printTree(),"note");
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
       val sampList = vcfHeader.titleLine.sampleList.toList
       val sampCt   = vcfHeader.sampleCt;
       val outHeader = vcfHeader.copyHeader;
       if(style == "GTTAG"){
         outHeader.addFormatLine(new SVcfCompoundHeaderLine("FORMAT",tagID,Number = "1", Type="Integer",desc=tagDesc).addWalker(this));
       } else {
         outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",tagID,Number = "1", Type=styleType,desc=tagDesc).addWalker(this));
       }
       outHeader.addWalk(this);
       (vcMap(vcIter){ vc => {
             vc.genotypes.sampList = sampList;
             vc.genotypes.sampGrp = Some(sampleToGroupMap);
             val vb = vc.getOutputLine();
             if(style == "GTTAG"){
               val gttag = Range(0,vcfHeader.sampleCt).map{ii => {
                 if(filter.keep((vc,ii))){
                   "1"
                 } else {
                   "0"
                 }
               }}.toArray
               vb.genotypes.addGenotypeArray(tagID,gttag);
             } else {
               val ct = Range(0,vcfHeader.sampleCt).count{ii => {
                 filter.keep((vc,ii))
               }}
               tally(""+tagID+"",ct)
               if(style == "CT"){
                 vb.addInfo(tagID,"" + ct);
               } else if(style == "PCT"){
                 vb.addInfo(tagID,"" + (100.toDouble * ct.toDouble / vcfHeader.sampleCt.toDouble));
               } else if(style == "FRAC"){
                 vb.addInfo(tagID,"" + (ct.toDouble / vcfHeader.sampleCt.toDouble));
               }
             }
             
             vb
       }}, outHeader)
       
    }
    
  }

  
  
  case class VcfExpressionTag(expr : String, tagID : String, tagDesc : String, geneTagString : Option[String] = None, subGeneTagString : Option[String] = None, geneList : Option[List[String]] = None) extends SVcfWalker {

    /*def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
       (vcIter, vcfHeader)
    }*/
    val isGeneTagExpr : Boolean = geneTagString.isDefined;
    val geneSet = geneList.map{ g => g.toSet }
    val geneTags : Set[String] = geneTagString.map{ gts => {
      gts.split("[:]").toSet;
    }}.getOrElse(Set())
    
    val subtractGeneTags : Set[String] = subGeneTagString.map{ sgts => {
      if(sgts == ".") {
        Set[String]()
      } else {
        sgts.split("[:]").toSet
      }
    }}.getOrElse(Set())
    
    def getGeneSet(v : SVcfVariantLine) : Vector[String] = {
      val rawGeneSet : Set[String] = geneTags.flatMap{gt => {
        v.info.get(gt).getOrElse(None).filter(_ != ".").map{ gg => {
          gg.split(",").toSet
        }}.getOrElse(Set())
      }} -- subtractGeneTags.flatMap{ gt => {
        v.info.get(gt).getOrElse(None).filter(_ != ".").map{ gg => {
          gg.split(",").toSet
        }}.getOrElse(Set())
      }}
      (geneSet match {
        case Some(gs) => {
          rawGeneSet.intersect(gs)
        }
        case None => {
          rawGeneSet
        }
      }).toVector.sorted
    }
    
    def walkerName : String = "VcfExpressionTag."+tagID;
    def walkerParams : Seq[(String,String)]= Seq[(String,String)](
        ("expr","\""+expr+"\""),
        ("tagID","\""+tagID+"\""),
        ("tagDesc","\""+tagDesc+"\""),
        ("geneTag","\""+geneTagString.getOrElse("None")+"\""),
        ("subGeneTag","\""+subGeneTagString.getOrElse("None")+"\""),
        ("geneTag","\""+geneList.getOrElse("None")+"\"")
    )

    val parser : SFilterLogicParser[SVcfVariantLine] = internalUtils.VcfTool.sVcfFilterLogicParser;
    val filter : SFilterLogic[SVcfVariantLine] = parser.parseString(expr);
    //val parser = internalUtils.VcfTool.SVcfFilterLogicParser();
    //val filter = parser.parseString(filterExpr);
    reportln("Parsed filter:\n   "+filter.printTree(),"note");
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
       val outHeader = vcfHeader.copyHeader;
       
       if(isGeneTagExpr){
           outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",tagID,Number = ".", Type="String",desc=tagDesc).addWalker(this));
           outHeader.addWalk(this);
           (vcMap(vcIter){ vc => {
             val vb = vc.getOutputLine();
             val k = if(filter.keep(vc)) "1" else "0";
             if( k == "1"){
               val gs = getGeneSet(vc);
               vb.addInfo(tagID,gs.padTo(1,".").mkString(","));
             } else {
               vb.addInfo(tagID,".");
             }
             notice(tagID+"="+k+" tagged for variant:\n    "+vc.getSimpleVcfString(),"TAGGED_"+tagID+"_"+k,1);
             vb
           }}, outHeader)
       } else {
           outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",tagID,Number = "1", Type="Integer",desc=tagDesc).addWalker(this));
           outHeader.addWalk(this);
           (vcMap(vcIter){ vc => {
             val vb = vc.getOutputLine();
             val k = if(filter.keep(vc)) "1" else "0";
             notice(tagID+"="+k+" tagged for variant:\n    "+vc.getSimpleVcfString(),"TAGGED_"+tagID+"_"+k,1);
             vb.addInfo(tagID,k);
             vb
           }}, outHeader)
       }

    }
  }

  
  
}









 


















