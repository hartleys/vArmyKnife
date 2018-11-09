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

 
import internalUtils.stdUtils._;

object LegacyVcfWalkers {
  
  
  class addTXAnno extends CommandLineRunUtil {
     override def priority = 10;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "addTxInfoToVCF", 
          quickSynopsis = "", 
          synopsis = "", 
          description = "This utility adds an array of new VCF tags with information about the transcriptional changes caused by each variant. "+BETA_WARNING,
          argList = 
                    new BinaryOptionArgument[String](
                                         name = "inputSavedTxFile", 
                                         arg = List("--inputSavedTxFile"), 
                                         valueName = "txdata.data.txt.gz",  
                                         argDesc =  "Loads a saved TXdata file. Either this parameter OR the --genomeFA parameter must be set. Using this file will be much faster than regenerating the tx data from the gtf/fasta."
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "genomeFA", 
                                         arg = List("--genomeFA"), 
                                         valueName = "genome.fa.gz",  
                                         argDesc =  "The genome fasta file. Can be gzipped or in plaintext. Either this parameter OR the --inputSavedTxFile parameter must be set!"
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "summaryFile", 
                                         arg = List("--summaryFile"), 
                                         valueName = "filename.txt",  
                                         argDesc =  "An optional extra output file that contains debugging information."
                                        ) ::
                    new UnaryArgument( name = "cdsRegionContainsStop",
                                         arg = List("--cdsRegionContainsStop"), // name of value
                                         argDesc = "Use this flag if the input GTF annotation file includes the STOP codon in the CDS region. Depending on the source of the annotation file, some GTF files include the STOP codon, some omit it. The UCSC knowngenes annotation file does NOT include CDS regions."+
                                                   "" // description
                                       ) ::
                    new UnaryArgument( name = "addSummaryCLNSIG",
                                         arg = List("--addSummaryCLNSIG"), // name of value
                                         argDesc = "Special-purpose flag for use with specialized ClinVar VCFs. NOT FOR GENERAL USE!"+
                                                   "" // description
                                       ) ::
                    new BinaryOptionArgument[String]( name = "addCanonicalTags",
                                         arg = List("--addCanonicalTags"), // name of value
                                         valueName = "knownCanonical.txt",
                                         argDesc = "Supply a list of canonical transcripts, add tags that indicate canonical-transcript-only variant info."+
                                                   "" // description
                                       ) ::
                    new UnaryArgument( name = "splitMultiAllelics",
                                         arg = List("--splitMultiAllelics"), // name of value
                                         argDesc = "If this flag is used, multiallelic variants will be split into multiple separate VCF lines."+
                                                   "" // description
                                       ) ::
                    new UnaryArgument( name = "geneVariantsOnly",
                                         arg = List("--geneVariantsOnly"), // name of value
                                         argDesc = "If this flag is used, only variants that fall on or near known genes will be written."+
                                                   "" // description
                                       ) ::
                    new UnaryArgument( name = "nonNullVariantsOnly",
                                         arg = List("--nonNullVariantsOnly"), // name of value
                                         argDesc = "If this flag is used, only write variants that have non-null alt alleles."+
                                                   "" // description
                                       ) ::
                    new BinaryOptionArgument[List[String]](
                                         name = "addBedTags", 
                                         arg = List("--addBedTags"), 
                                         valueName = "TAGTITLE:filedesc:bedfile.bed,TAGTITLE2:filedesc2:bedfile2.bed",  
                                         argDesc =  "List of tags and bed files that define said tags. For each tag, the variant will have a tag value of 1 iff the variant appears on the bed file region, and 0 otherwise."
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "txInfoFile", 
                                         arg = List("--txInfoFile"), 
                                         valueName = "txInfoFile.txt",  
                                         argDesc =  "Outputs an optional debugging file."
                                        ) ::

                    new BinaryOptionArgument[String](
                                         name = "outputSavedTxFile", 
                                         arg = List("--outputSavedTxFile"), 
                                         valueName = "txdata.data.txt.gz",  
                                         argDesc =  "Creates a saved TXdata file, for faster loading in future runs. This contains metadata about each transcript in a machine-readable format."
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "txToGeneFile", 
                                         arg = List("--txToGeneFile"), 
                                         valueName = "txToGene.txt",  
                                         argDesc =  "File containing the mapping of transcript names to gene symbols. This file must have 2 columns: the txID and the geneID. No header line."
                                        ) :: 
                    new BinaryOptionArgument[String](
                                         name = "groupFile", 
                                         arg = List("--groupFile"), 
                                         valueName = "groups.txt",  
                                         argDesc =  "File containing a group decoder. This is a simple 2-column file (no header line). The first column is the sample ID, the 2nd column is the group ID."
                                        ) :: 
                    new BinaryOptionArgument[String](
                                         name = "superGroupList", 
                                         arg = List("--superGroupList"), 
                                         valueName = "sup1,grpA,grpB,...;sup2,grpC,grpD,...",  
                                         argDesc =  "A list of top-level supergroups. Requires the --groupFile parameter to be set."
                                        ) :: 
                    new BinaryOptionArgument[List[String]](
                                         name = "chromList", 
                                         arg = List("--chromList"), 
                                         valueName = "chr1,chr2,...",  
                                         argDesc =  "List of chromosomes. If supplied, then all analysis will be restricted to these chromosomes. All other chromosomes wil be ignored."
                                        ) ::
                    new UnaryArgument( name = "infileList",
                                         arg = List("--infileList"), // name of value
                                         argDesc = "Use this option if you want to provide input file(s) containing a list of input files rather than a single input file"+
                                                   "" // description
                                       ) ::
                    new FinalArgument[String](
                                         name = "infile",
                                         valueName = "variants.vcf",
                                         argDesc = "input VCF file. Can be gzipped or in plaintext." // description
                                        ) ::
                    new FinalArgument[String](
                                         name = "gtffile",
                                         valueName = "gtffile.gtf.gz",
                                         argDesc = "A gene annotation GTF file. Can be gzipped or in plaintext." // description
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
         /*if(parser.get[Boolean]("newParser")){
          runSAddTXAnno( parser.get[String]("infile"),
                       parser.get[String]("outfile"),
                       gtffile = parser.get[String]("gtffile"),
                       genomeFA = parser.get[Option[String]]("genomeFA"),
                       txInfoFile = parser.get[Option[String]]("txInfoFile"),
                       summaryFile = parser.get[Option[String]]("summaryFile"),
                       addStopCodon = ! parser.get[Boolean]("cdsRegionContainsStop"),
                       inputSavedTxFile = parser.get[Option[String]]("inputSavedTxFile"),
                       outputSavedTxFile = parser.get[Option[String]]("outputSavedTxFile"),
                       chromList = parser.get[Option[List[String]]]("chromList"),
                       txToGeneFile = parser.get[Option[String]]("txToGeneFile"),
                       groupFile = parser.get[Option[String]]("groupFile"),
                       superGroupList = parser.get[Option[String]]("superGroupList"),
                       splitMultiAllelics = parser.get[Boolean]("splitMultiAllelics"),
                       addSummaryCLNSIG = parser.get[Boolean]("addSummaryCLNSIG"),
                       addCanonicalTags = parser.get[Option[String]]("addCanonicalTags"),
                       geneVariantsOnly = parser.get[Boolean]("geneVariantsOnly"),
                       nonNullVariantsOnly = parser.get[Boolean]("nonNullVariantsOnly"),
                       addBedTags = parser.get[Option[List[String]]]("addBedTags"),
                       infileList = parser.get[Boolean]("infileList"),
                       genoFilter = parser.get[Option[String]]("genoFilter"),
                       filterTag = parser.get[String]("filterTag"),
                       postFilterGtTag = parser.get[String]("postFilterGtTag"),
                       preFilterGtTag = parser.get[String]("preFilterGtTag"),
                       unFilterGtTag = parser.get[String]("unFilterGtTag"),
                       dbnsfpFile = None,
                       dbnsfpTags = None,
                       singleCallerVcfs = parser.get[Option[List[String]]]("singleCallerVcfs"),
                       singleCallerVcfNames = parser.get[Option[List[String]]]("singleCallerVcfNames"),
                       singleCallerMaster = parser.get[Option[String]]("singleCallerMaster"),
                       numLinesRead = parser.get[Option[Int]]("numLinesRead"),
                       addInfoVcfs = parser.get[Option[List[String]]]("addInfoVcfs"),
                       dropVariantsExpression = parser.get[Option[String]]("dropVariantsExpression"),
                       convertToStandardVcf = parser.get[Boolean]("convertToStandardVcf")
                       
             )
         } else {*/
         
         runAddTXAnno( parser.get[String]("infile"),
                       parser.get[String]("outfile"),
                       gtffile = parser.get[String]("gtffile"),
                       genomeFA = parser.get[Option[String]]("genomeFA"),
                       txInfoFile = parser.get[Option[String]]("txInfoFile"),
                       summaryFile = parser.get[Option[String]]("summaryFile"),
                       addStopCodon = ! parser.get[Boolean]("cdsRegionContainsStop"),
                       inputSavedTxFile = parser.get[Option[String]]("inputSavedTxFile"),
                       outputSavedTxFile = parser.get[Option[String]]("outputSavedTxFile"),
                       chromList = parser.get[Option[List[String]]]("chromList"),
                       txToGeneFile = parser.get[Option[String]]("txToGeneFile"),
                       groupFile = parser.get[Option[String]]("groupFile"),
                       superGroupList = parser.get[Option[String]]("superGroupList"),
                       splitMultiAllelics = parser.get[Boolean]("splitMultiAllelics"),
                       addSummaryCLNSIG = parser.get[Boolean]("addSummaryCLNSIG"),
                       addCanonicalTags = parser.get[Option[String]]("addCanonicalTags"),
                       geneVariantsOnly = parser.get[Boolean]("geneVariantsOnly"),
                       nonNullVariantsOnly = parser.get[Boolean]("nonNullVariantsOnly"),
                       addBedTags = parser.get[Option[List[String]]]("addBedTags"),
                       infileList = parser.get[Boolean]("infileList")
             )
         //}
       }
     }
  }

  def runAddTXAnno(vcffile : String, outfile : String, 
                gtffile : String, 
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
                bufferSize : Int = 32, 
                vcfCodes : VCFAnnoCodes = VCFAnnoCodes()
                ){
    
    val (vcIter,vcfHeader) = internalUtils.VcfTool.getVcfFilesIter(infile = vcffile, 
                                       chromList = chromList,infileList = infileList,
                                       vcfCodes = vcfCodes);
    
    val summaryWriter = if(summaryFile.isEmpty) None else Some(openWriterSmart(summaryFile.get));
    
    val walkers = Seq[VCFWalker](
          AddTxAnnoWalker(
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
                addBedTags = addBedTags,
                vcfCodes =vcfCodes
                )
        ) ++ (
            if(nonNullVariantsOnly){
              Seq[VCFWalker]( FilterNonVariantWalker() );
            } else {
              Seq[VCFWalker]();
            }
        ) ++ (
            if(groupFile.isEmpty){
              Seq[VCFWalker]();
            } else {
              Seq[VCFWalker](AddGroupInfoAnno(groupFile = groupFile, groupList = None, superGroupList  = superGroupList, chromList = chromList))
            }
        ) ++ (
            if(splitMultiAllelics){
              Seq[VCFWalker](SplitMultiAllelics(vcfCodes = vcfCodes, clinVarVariants = false, splitSimple = false));
            } else {
              Seq[VCFWalker]();
            }
        ) ++ (
            if(addSummaryCLNSIG){
              Seq[VCFWalker](AddSummaryCln(vcfCodes = vcfCodes));
            } else {
              Seq[VCFWalker]();
            }
        ) ++ (
            if(addCanonicalTags.isDefined){
              Seq[VCFWalker](AddCanonicalInfo(canonicalTxFile = addCanonicalTags.get));
            } else {
              Seq[VCFWalker]();
            }
        )
    
        
    val (finalIter, finalHeader) = walkers.foldLeft((vcIter,vcfHeader)){case ((iter,header),walker) => {
      walker.walkVCF(vcIter = iter, vcfHeader=header)
    }}
        
    val vcfWriter = internalUtils.VcfTool.getVcfWriter(outfile, header = finalHeader);

    finalIter.foreach(vc => {
      vcfWriter.add(vc)
    })
    vcfWriter.close();
    
    if(! summaryWriter.isEmpty) summaryWriter.get.close();
  }
  
  
  case class FilterChromWalker(chromList : Option[List[String]]) extends internalUtils.VcfTool.VCFWalker {
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
        (vcIter : Iterator[VariantContext],vcfHeader : VCFHeader) => (vcIter.filter(p => cs.contains(p.getContig())),vcfHeader);
      }
      case None => {
        (vcIter : Iterator[VariantContext],vcfHeader : VCFHeader) => (vcIter,vcfHeader);
      }
    }
    def walkVCF(vcIter : Iterator[VariantContext],vcfHeader : VCFHeader, verbose : Boolean = true) : (Iterator[VariantContext],VCFHeader) = walkerFunc(vcIter,vcfHeader);
  }
  
  val DEFAULT_CMDADDCANONICALINFO_TAGLIST = DefaultVCFAnnoCodes.txList_TAG + ","+
                                               DefaultVCFAnnoCodes.vMutC_TAG + ","+
                                               DefaultVCFAnnoCodes.vMutP_TAG + ","+
                                               DefaultVCFAnnoCodes.vMutINFO_TAG + ","+
                                               DefaultVCFAnnoCodes.vType_TAG + ","+
                                               DefaultVCFAnnoCodes.vMutR_TAG + ","+
                                               DefaultVCFAnnoCodes.geneIDs;
  val DEFAULT_CMDADDCANONICALINFO_TXTAG = DefaultVCFAnnoCodes.txList_TAG
  
  class CmdAddCanonicalInfo extends CommandLineRunUtil {
     override def priority = 100;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "AddCanonicalInfo", 
          quickSynopsis = "", 
          synopsis = "", 
          description = " " + ALPHA_WARNING,   
          argList = 
            
            /*
                     vMutC_TAG : String = TOP_LEVEL_VCF_TAG+"varC",
        vMutP_TAG : String = TOP_LEVEL_VCF_TAG+"varPredP",
             */
            
                    new BinaryArgument[String](name = "tagList",
                                           arg = List("--tagList"),  
                                           valueName = "tag1,tag2,tag3,...", 
                                           argDesc = "", 
                                           defaultValue = Some(
                                               DEFAULT_CMDADDCANONICALINFO_TAGLIST
                                           )) ::
                    new BinaryOptionArgument[String](
                                         name = "chromList", 
                                         arg = List("--chromList"), 
                                         valueName = "chromList.txt",  
                                         argDesc =  ""
                                        ) ::   
                    new BinaryArgument[String](name = "txListTag",
                                           arg = List("--txListTag"),  
                                           valueName = OPTION_TAGPREFIX+"TXLIST", 
                                           argDesc = "", 
                                           defaultValue = Some(
                                               DEFAULT_CMDADDCANONICALINFO_TXTAG
                                           )) ::
                    new FinalArgument[String](
                                         name = "invcf",
                                         valueName = "variants.vcf",
                                         argDesc = "infput VCF file" // description
                                        ) ::
                    new FinalArgument[String](
                                         name = "canonicalTxFile",
                                         valueName = "canonicalTxFile.txt",
                                         argDesc = "Canonical tx file." // description
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
       
       AddCanonicalInfo(
                   canonicalTxFile = parser.get[String]("canonicalTxFile"),
                   tagList = parser.get[String]("tagList"),
                   txTag = parser.get[String]("txListTag")
                   ).walkVCFFile(
                   infile    = parser.get[String]("invcf"),
                   outfile   = parser.get[String]("outvcf"),
                   chromList = parser.get[Option[List[String]]]("chromList")
                   )
     }   
    }
  }
  

  case class AddCanonicalInfo(canonicalTxFile : String, tagList : String = DEFAULT_CMDADDCANONICALINFO_TAGLIST, txTag : String = DEFAULT_CMDADDCANONICALINFO_TXTAG) extends internalUtils.VcfTool.VCFWalker {
    val tagSet = tagList.split(",").toSet;
    
    
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
    
    def walkVCF(vcIter : Iterator[VariantContext], vcfHeader : VCFHeader, verbose : Boolean = true) : (Iterator[VariantContext],VCFHeader) = {
      val oldHeaderLines = vcfHeader.getInfoHeaderLines().asScala.filter(hl => tagSet.contains(hl.getID()));
      
      val addedHeaderLines = oldHeaderLines.map{ h => {
        if(h.getCountType() == VCFHeaderLineCount.A){
          new VCFInfoHeaderLine(h.getID() + "_CANON", VCFHeaderLineCount.A, h.getType(), "(For the canonical transcript(s) only) "+h.getDescription());
        } else if(h.getCountType() == VCFHeaderLineCount.UNBOUNDED){
          new VCFInfoHeaderLine(h.getID() + "_CANON", VCFHeaderLineCount.UNBOUNDED, h.getType(), "(For the canonical transcript(s) only) "+h.getDescription());
        } else {
          error("FATAL ERROR: ");
          null;
        }
      }}
      val isByAllele = oldHeaderLines.map{ h => {
        if(h.getCountType() == VCFHeaderLineCount.A){
          (h.getID(),true)
        } else if(h.getCountType() == VCFHeaderLineCount.UNBOUNDED){
          (h.getID(),false)
        } else {
          error("FATAL ERROR: ");
          null;
        }
      }}.filter(_._1 != txTag).toVector
      
      val newHeader = internalUtils.VcfTool.addHeaderLines(vcfHeader, addedHeaderLines.toVector);
      
      return (vcIter.map{ vc => {
        walkLine(vc,isByAllele=isByAllele,txTag=txTag);
      }}, newHeader);
    }
    
    def walkLine(vc : VariantContext, isByAllele : Vector[(String,Boolean)], txTag : String) : VariantContext = {
      var vb = new VariantContextBuilder(vc);
      
      val txList = getAttributeAsStringList(vc,txTag);
      vb = vb.attribute(txTag + "_CANON",txList.filter(isCanon(_)).padTo(1,".").mkString(","));
      
      if(txList.exists(tx => isCanon(tx))){
        isByAllele.foreach{ case (tag,isA) => {
          val attr = getAttributeAsStringList(vc,tag);
          if(isA){
            vb = vb.attribute(tag+"_CANON",attr.map{a => {
              a.split("\\|").zip(txList).filter{ case (aa,tx) => {
                isCanon(tx);
              }}.map(_._1).mkString("|");
            }}.mkString(","))
          } else {
            vb = vb.attribute(tag+"_CANON",attr.zip(txList).filter{ case (aa,tx) => {
                     isCanon(tx)
                   }}.map(_._1).mkString(","))
          }
        }}
        
      }
      
      return vb.make();
    }
  }
  

  
  case class AddSummaryCln(
                 vcfCodes : VCFAnnoCodes = VCFAnnoCodes()
                ) extends internalUtils.VcfTool.VCFWalker {
    
    def walkVCF(vcIter : Iterator[VariantContext], vcfHeader : VCFHeader, verbose : Boolean = true) : (Iterator[VariantContext],VCFHeader) = {
      
      val newHeaderLines = List(
            new VCFInfoHeaderLine(vcfCodes.CLNVAR_SUMSIG,     1, VCFHeaderLineType.Integer, "Summary clinical significance level, from CLNSIG tag. Collapsed multiple reports into a single reported significance level.")      
      );
      
      val newHeader = internalUtils.VcfTool.addHeaderLines(vcfHeader,newHeaderLines);
      reportln("Walking input VCF...","note")

      return ((vcIter.map(v => {
         var vb = new htsjdk.variant.variantcontext.VariantContextBuilder(v);
         //val clnsig = v.getAttributeAsList("CLNSIG").asScala.map(_.toString()).toSeq;
         val clnsig = getAttributeAsStringList(v,"CLNSIG")
         
         val cs = internalUtils.CalcACMGVar.getSummaryClinSig(clnsig);
         vb = vb.attribute(vcfCodes.CLNVAR_SUMSIG,cs);
         
         vb.make();
      }), newHeader));
      
    }
  }

  class CmdSplitMultiAllelics extends CommandLineRunUtil {
     override def priority = 15;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "splitMultiAllelics", 
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
                    new UnaryArgument( name = "clinVarVariants",
                                         arg = List("--clinVarVariants"), // name of value
                                         argDesc = ""+
                                                   ""
                                       ) ::
                    new UnaryArgument( name = "infileList",
                                         arg = List("--infileList"), // name of value
                                         argDesc = "If this option is used, then instead of a single input file the input file(s) will be assumed "+
                                                   "to be a file containing a list of input files to parse in order. If multiple VCF files are specified, "+
                                                   "the vcf lines will be concatenated and the header will be taken from the first file."+
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
       SplitMultiAllelics(clinVarVariants = parser.get[Boolean]("clinVarVariants"), 
                          splitSimple = parser.get[Boolean]("clinVarVariants")
                        ).walkVCFFiles(
                              infile = parser.get[String]("invcf"),
                              outfile = parser.get[String]("outvcf"), 
                              chromList = parser.get[Option[List[String]]]("chromList"), 
                              infileList = parser.get[Boolean]("infileList")
                        )
     }
  }
  }
  
  /*def runSplitMultiAllelics(infile : String, outfile : String, chromList : Option[List[String]], clinVarVariants : Boolean, infileList : Boolean, vcfCodes : VCFAnnoCodes = VCFAnnoCodes()){
    val (vcIter,vcfHeader) = internalUtils.VcfTool.getVcfIterator(infile, 
                                       chromList = chromList,
                                       vcfCodes = vcfCodes);
        
    val (vcIter2, newHeader) = SplitMultiAllelics(vcfCodes = vcfCodes, clinVarVariants = clinVarVariants, splitSimple = clinVarVariants).walkVCF(vcIter,vcfHeader);
    
    val vcfWriter = internalUtils.VcfTool.getVcfWriter(outfile, header = newHeader);
    
    vcIter2.foreach(vc => {
      vcfWriter.add(vc)
    })
    vcfWriter.close();
  }*/
  
  val multiAllelicIndexStream = Iterator.from(0).toStream.map(i => {
    Range(0,i).flatMap(k => Range(k,i).map(z => (k,z)))
  }) 
  
  
  val NUMREPORTBADLEN=5;
  case class SplitMultiAllelics(vcfCodes : VCFAnnoCodes = VCFAnnoCodes(), 
                                clinVarVariants : Boolean, splitSimple : Boolean = false) extends internalUtils.VcfTool.VCFWalker {
      val infoCLN = if(clinVarVariants){
        Set[String]("CLNHGVS","CLNSRC","CLNORIGIN","CLNSRCID","CLNSIG","CLNDSDB","CLNDSDBID","CLNREVSTAT","CLNACC","CLNDBN");
      } else {
        Set[String]();
      }
      
    val alleCtCt = new scala.collection.mutable.HashMap[Int, Int]().withDefaultValue(0)
    val badCtCt = new scala.collection.mutable.HashMap[Int, Int]().withDefaultValue(0)
      
    def walkVCF(vcIter : Iterator[VariantContext], vcfHeader : VCFHeader, verbose : Boolean = true) : (Iterator[VariantContext], VCFHeader) = {
      val newHeaderLines = List[VCFHeaderLine](
          //new VCFInfoHeaderLine(vcfCodes.isSplitMulti_TAG, 0, VCFHeaderLineType.Flag,    "Indicates that this line was split apart from a multiallelic VCF line."),
          new VCFInfoHeaderLine(vcfCodes.splitIdx_TAG,     1, VCFHeaderLineType.Integer, "Indicates the index of this biallelic line in the set of biallelic lines extracted from the same multiallelic VCF line."),
          new VCFInfoHeaderLine(vcfCodes.numSplit_TAG,     1, VCFHeaderLineType.Integer, "Indicates the number of biallelic lines extracted from the same multiallelic VCF line as this line."),
          new VCFInfoHeaderLine(vcfCodes.splitAlle_TAG,     VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String, "The original set of alternative alleles."),
           new VCFInfoHeaderLine(vcfCodes.splitAlleWARN_TAG,     VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String, "Warnings produced by the multiallelic allele split. These may occur when certain tags have the wrong number of values.")
      ) ++ (if(clinVarVariants){
        List[VCFHeaderLine](
            new VCFInfoHeaderLine("CLNPROBLEM",     1, VCFHeaderLineType.Integer, "Indicates whether the splitting of the Clinvar CLN tags was successful. 1=yes, 0=no.")
        ) ++ infoCLN.map(key => {
            new VCFInfoHeaderLine("ORIG"+key,     VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String, "")
        })
      } else {List[VCFHeaderLine]()})
      
      val newHeader = internalUtils.VcfTool.addHeaderLines(vcfHeader,newHeaderLines);
      
      val infoA = newHeader.getMetaDataInInputOrder().asScala.filter(_ match {
        case x : VCFInfoHeaderLine => {
          x.getCountType() == VCFHeaderLineCount.A;
        }
        case _ => {
          false;
        }
      }).map(_ match {
        case x : VCFInfoHeaderLine => {
          x.getID()
        }
      })
      val infoR = newHeader.getMetaDataInInputOrder().asScala.filter(_ match {
        case x : VCFInfoHeaderLine => {
          x.getCountType() == VCFHeaderLineCount.R;
        }
        case _ => {
          false;
        }
      }).map(_ match {
        case x : VCFInfoHeaderLine => {
          x.getID()
        }
      })
      
      val tagWarningMap = new scala.collection.mutable.AnyRefMap[String,Int](tag => 0);
      
      /*infoCLN.foreach(x => {
        reportln("INFO Line "+x+" is of type CLN","debug");
      })
      infoA.foreach(x => {
        reportln("INFO Line "+x+" is of type A","debug");
      })
      infoR.foreach(x => {
        reportln("INFO Line "+x+" is of type R","debug");
      })*/
      
      val sampNames = vcfHeader.getSampleNamesInOrder();
      return (addIteratorCloseAction(vcIter.flatMap(vc => {
        var warningSet = Set[String]();
        
        if(vc.getNAlleles() <= 2 && (! splitSimple)){
          var vb = new htsjdk.variant.variantcontext.VariantContextBuilder(vc);
          vb = vb.attribute(vcfCodes.numSplit_TAG, "1" );
          vb = vb.attribute(vcfCodes.splitIdx_TAG, "0" );
          alleCtCt(1) = alleCtCt(1) + 1;
          List[VariantContext](vb.make());
        } else {
          val alleles = internalUtils.VcfTool.getAllelesInOrder(vc).toVector;
          val numAlle = alleles.length;
          val refAlle = alleles.head;
          val altAlleles = alleles.tail;
          alleCtCt(altAlleles.length) = alleCtCt(altAlleles.length) + 1;
          val attr = vc.getAttributes().asScala.map{case (key,obj) => { (key,vc.getAttributeAsList(key).asScala.map(_.toString).mkString(",")  ) }}.toMap
          val (copyAttrA,tempAttrSet)    = attr.partition{case (key,obj) => { infoA.contains(key) }};
          val (copyAttrCLN,tempAttrSet2)    = tempAttrSet.partition{case (key,obj) => { infoCLN.contains(key) }};
          val (copyAttrR,simpleCopyAttr) = tempAttrSet2.partition{case (key,obj) => { infoR.contains(key) }};
          
          val attrA = copyAttrA.map{case (key,attr) => {
            val a = {
              val atr = attr.split(",",-1);
              if(atr.length != numAlle - 1){
                warning("WARNING: ATTR: \""+key+"\"=\""+attr+"\" of type \"A\"\n   VAR:\n      atr.length() = "+atr.length + " numAlle = "+numAlle+
                    (if(internalUtils.optionHolder.OPTION_DEBUGMODE) "\n   "+vc.toStringWithoutGenotypes() else ""),
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
              val atr = attr.split(",",-1);
              if(atr.length != numAlle){
                warning("WARNING: ATTR: \""+key+"\"=\""+attr+"\" of type \"R\"\n   VAR:\n      atr.length() = "+atr.length + " numAlle = "+numAlle+
                       (if(internalUtils.optionHolder.OPTION_DEBUGMODE) "\n   "+vc.toStringWithoutGenotypes() else ""),
                       "INFO_LENGTH_WRONG",1);
                warningSet = warningSet + ("INFO_LENGTH_WRONG_R."+key)
                tagWarningMap(key) += 1;
                repToSeq(atr.mkString(","),numAlle);
              } else {
                atr.toSeq;
              }
            }
            (key,a)
          }}

          val ANNSTR =  trimBrackets(attr.getOrElse("ANN","")).split(",",-1).map(ann => { (ann.trim.split("\\|",-1)(0),ann.trim) });
          
          //warning("ANNSTR: \n      "+ANNSTR.map{case (a,b) => { "(\""+a+"\",\""+b+"\")" }}.mkString("\n      ")+"","ANN_FIELD_TEST",100);
          
          val ANN = if(attr.contains("ANN")) Some(altAlleles.map(_.getBaseString()).map(aa => {
             ANNSTR.filter{case (ahead,ann) => {ahead ==  aa }}.map{case (ahead,ann) => {ann}}.mkString(",");
          })) else None;
          
          //ANN match {
          //    case Some(annVector) => {
          //      warning("ANN VECTOR: \n      \""+annVector.mkString("\"\n      \"")+"\"","ANN_FIELD_TEST",100);
          //    }
          //    case None => {
          //      //do nothing
          //    }
          //}
          var isBad = false;
          
          val out = altAlleles.zipWithIndex.map{case (alt,altIdx) => {
            var vb = new htsjdk.variant.variantcontext.VariantContextBuilder();
            val altAlleIdx = altIdx + 1;
            vb.loc(vc.getContig(),vc.getStart(),vc.getEnd());
            vb.filters(vc.getFiltersMaybeNull());
            vb.id(vc.getID());
            vb.log10PError(vc.getLog10PError());
            
            val alleleSet = if(alt != Allele.SPAN_DEL) List(refAlle,alt,Allele.SPAN_DEL) else List(refAlle,alt);
            vb.alleles(alleleSet.asJava);
            vb.attribute(vcfCodes.splitAlle_TAG, altAlleles.map(a => {a.getBaseString()}).mkString(",") )
            vb.attribute(vcfCodes.numSplit_TAG, altAlleles.length.toString );
            vb.attribute(vcfCodes.splitIdx_TAG, altIdx.toString );
            
            var malformedADCT = 0;
            
            if(vc.hasGenotypes()){
            vb.genotypes(GenotypesContext.create(
                new java.util.ArrayList[Genotype](vc.getGenotypesOrderedBy(sampNames).asScala.map{gt => {
                  var gb = new GenotypeBuilder(gt.getSampleName());
                  if(gt.hasAD()){
                    val ad = gt.getAD();
                    if(ad.length != altAlleles.length + 1){
                      if( malformedADCT < 10 ){
                        warning("Malformed AD tag! AD is: [\""+ad.mkString("\",\"")+"\"], Alles are: [\""+refAlle.getBaseString()+"\"][\""+altAlleles.map{a => a.getBaseString()}.mkString("\",\"")+"\"]\n"+
                                "Original VCF String: "+vc.toStringWithoutGenotypes() +"","Malformed_AD_Tag",200);
                        malformedADCT = malformedADCT + 1;
                      }
                    } else {
                      val refAD = ad.head;
                      val altAD = ad(altAlleIdx);
                      val othAD = ad.zipWithIndex.filter{case (a,i) => {i != 0 && i != altAlleIdx}}.map(_._1).sum
                      gb = gb.AD(Array(refAD,altAD,othAD));
                    }
                  }
                  if(gt.hasPL()){
                    val genoPL  = gt.getLikelihoods().getAsPLs();
                    val genoL   = genoPL.map(x => math.pow(10,- x.toDouble / 10.toDouble));
                    gb.PL(Array[Int]( 
                        genoPL(0),
                        genoPL(GenotypeLikelihoods.calculatePLindex(0,altAlleIdx)),
                        genoPL(GenotypeLikelihoods.calculatePLindex(altAlleIdx,altAlleIdx)),
                        (- 10.toDouble * math.log10( multiAllelicIndexStream(numAlle).filter(kp => kp._1 == 0 && kp._2 != altAlleIdx).map(x => genoL(GenotypeLikelihoods.calculatePLindex(x._1,x._2))).sum )).round.toInt,
                        (- 10.toDouble * math.log10( multiAllelicIndexStream(numAlle).filter(kp => (kp._1 == altAlleIdx && kp._2 != altAlleIdx && kp._2 != 0) || (kp._1 != altAlleIdx && kp._2 == altAlleIdx && kp._1 != 0)).map(x => genoL(GenotypeLikelihoods.calculatePLindex(x._1,x._2))).sum )).round.toInt,
                        (- 10.toDouble * math.log10( multiAllelicIndexStream(numAlle).filter(kp => kp._1 != 0 && kp._2 != 0 && kp._1 != altAlleIdx && kp._2 != altAlleIdx).map(x => genoL(GenotypeLikelihoods.calculatePLindex(x._1,x._2))).sum )).round.toInt
                    ));
                  }
                  
                  if(gt.hasDP()) gb = gb.DP(gt.getDP());
                  if(gt.hasGQ()) gb = gb.GQ(gt.getGQ());
                  gb.alleles(gt.getAlleles.asScala.map(a => {
                    if(a.isNoCall()) a;
                    else if(refAlle == a || alt == a) a;
                    else Allele.SPAN_DEL
                  }).asJava); 
                  for((key,attr) <- gt.getExtendedAttributes().asScala){
                    gb.attribute(key,attr);
                  }
                  
                  gb.make();
                }}.toVector.asJava)
            ))}
            simpleCopyAttr.foreach{case (key,attr) => {
              vb.attribute(key,attr);
            }}
            if(clinVarVariants){
              vb.attribute("CLNPROBLEM","0");
              copyAttrCLN.foreach{case(key,attr) => {
                    vb.attribute("ORIG"+key,attr);
                 }}
              val clnAlleList = vc.getAttributeAsList("CLNALLE").asScala.map(x => string2int(x.toString()))
              val clnIdx = clnAlleList.indexWhere(x => x == altAlleIdx);
              if(clnIdx == -1){
                  copyAttrCLN.foreach{case(key,attr) => {
                    vb.attribute(key,"NA");
                  }}
                  warning("CLINVAR CLNALLE TAG IS MALFORMED: CLNALLE="+vc.getAttributeAsString("CLNALLE","?")+" does not contain altAlleIdx = "+altAlleIdx + " (nAlle="+altAlleles.length.toString+")\n"+
                          "       CLNSIG=\""+vc.getAttributeAsList("CLNSIG").asScala.mkString(",")+"\"",
                          "Malformed_ClinVar_CLNALLE_TAG",25);
                  isBad = true;
                  
                  vb.attribute("CLNPROBLEM","1");
              } else {
                //val attrCLN = copyAttrCLN.map{case (key,attr) => {
                copyAttrCLN.foreach{case(key,attr) => {
                  val attrCells = attr.split(",",-1);
                  if(clnIdx >= attrCells.length){
                      warning("CLINVAR "+key+" TAG IS MALFORMED: \""+attr+"\" does not contain clnIdx = "+clnIdx+" (altAlleIdx="+altAlleIdx+", CLNALLE="+vc.getAttributeAsString("CLNALLE","?")+")","Malformed_ClinVar_TAG",25);
                      vb.attribute("CLNPROBLEM","1");
                      isBad = true;
                  } else {
                    vb.attribute(key,attrCells(clnIdx));
                  }
                  if(clnAlleList.length != attrCells.length){
                      warning("CLINVAR "+key+" TAG IS MALFORMED: \""+attr+"\" does not have the same length as CLNALLE="+clnAlleList.mkString(",")+" (altAlleIdx="+altAlleIdx+", CLNALLE="+vc.getAttributeAsString("CLNALLE","?")+")","Malformed_ClinVar_TAG",25);
                      isBad = true;
                  }
                }}
                //}}
              }

            }
            attrA.foreach{case (key,attrArray) => {
              vb.attribute(key,attrArray(altIdx));
            }}
            attrR.foreach{case (key,attrArray) => {
              vb.attribute(key,attrArray(0) + "," + attrArray(altAlleIdx));
            }}
            ANN match {
              case Some(annVector) => {
                vb.attribute("ANN",annVector(altIdx));
              }
              case None => {
                //do nothing
              }
            }
            vb.attribute(vcfCodes.splitAlleWARN_TAG, warningSet.toVector.sorted.map(_.toString()).padTo(1,".").mkString(","));
            
            /*copyAttrA.foreach{case (key,attr) => {
              val kx = vc.getAttributeAsString(key,"[]").tail.init.split(",")
              vb.attribute(key,kx(altIdx).trim);
            }}
            copyAttrR.foreach{case (key,attr) => {
              val kx = vc.getAttributeAsString(key,"[]").tail.init.split(",")
              vb.attribute(key,kx(0).trim+","+kx(altAlleIdx).trim);
            }}*/
            
            vb.make();
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
  
  case class FilterNonVariantWalker() extends internalUtils.VcfTool.VCFWalker {
    def walkVCF(vcIter : Iterator[VariantContext],vcfHeader : VCFHeader, verbose : Boolean = true) : (Iterator[VariantContext],VCFHeader) = {
      (vcIter.filter{vc => {
        vc.getAlternateAlleles().asScala.filter(_.getBaseString() != "*").length > 0;
      }},vcfHeader)
    }
  }

  case class AddTxAnnoWalker(gtffile : String, 
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
                vcfCodes : VCFAnnoCodes = VCFAnnoCodes()
                ) extends internalUtils.VcfTool.VCFWalker {
      
      reportln("Starting TX parse...","progress");
       
      val bedTags : Seq[(String,String,internalUtils.commonSeqUtils.GenomicInterval => Boolean)] = addBedTags match {
        case Some(bt) => {
          bt.map{b => {
            val pair : Array[String] = b.split(":");
            if(pair.length != 3) error("Each comma-delimited element of parameter addBedTags must have exactly 3 colon-delimited elements (tag:desc:filename.bed).")
            val (t,desc,f) : (String,String,String) = (pair(0),pair(1),pair(2));
            val arr : internalUtils.genomicAnnoUtils.GenomicArrayOfSets[String] = internalUtils.genomicAnnoUtils.GenomicArrayOfSets[String](false);
            val lines = getLinesSmartUnzip(f);
            lines.foreach(line => {
              val cells = line.split("\t");
              val (chrom,start,end) = (cells(0),string2int(cells(1)),string2int(cells(2)))
              arr.addSpan(internalUtils.commonSeqUtils.GenomicInterval(chrom, '.', start,end), "CE");
            })
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
      
      /*
       * 
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
       * 
       */
      
      
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
      
      val TXSeq : scala.collection.mutable.Map[String,TXUtil] = inputSavedTxFile match {
        case Some(txf) => {
          val ipr = internalUtils.stdUtils.IteratorProgressReporter_ThreeLevel("lines", 200, 1000 , 2000 )
          val wrappedIter = internalUtils.stdUtils.wrapIteratorWithProgressReporter(getLinesSmartUnzip(txf) , ipr )
          val txs = scala.collection.mutable.AnyRefMap[String,TXUtil]();
          wrappedIter.foreach{line => {
            val tx = buildTXUtilFromString(line);
            if(keepChromFunc(tx.chrom)) txs += (tx.txID,tx)
          }}
          txs;
        }
        case None => {
          if(genomeFA.isEmpty){
            error("FATAL ERROR: Either the --inputSavedTxFile parameter must be set, OR --genomeFA must be set!")
          }
          buildTXUtilsFromAnnotation(gtffile,genomeFA.get,addStopCodon=addStopCodon,chromSet=chromSet);
        }
      }
      reportln("Finished TX parse.","progress");
      
      outputSavedTxFile match {
        case Some(txf) => {
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
          val txWriter = openWriterSmart(txFile,false);
          TXSeq.foreach{ case (txID,tx) => {
            try {
              txWriter.write("txid\tensid\tisValidCoding\tissues\tchrom\tstart\tend\n");
              TXSeq.foreach{ case (txID,tx) => {
                txWriter.write(txID+"\t"+tx.geneID+"\t"+tx.isValidFullLenTX+"\t"+tx.getIssueList.padTo(1,".").mkString(",")+"\t"+tx.chrom+"\t"+tx.gStart+"\t"+tx.gEnd+"\n");
              }}
              txWriter.close();
            } catch {
              case e : Exception => {
                reportln("Caught error on TX:","warn");
                reportln(tx.toStringShort()+"\n","warn");
                throw e;
              }
            }
          }}
          txWriter.close();
        }
        case None => {
          //do nothing
        }
      }
      reportln("Valid Full-Length TX: "+TXSeq.count{case (txID,tx) => {tx.isValidFullLenTX}}+"/"+TXSeq.size+"\n"+
               "                      (Note: "+TXSeq.count{case (txID,tx) => {tx.isValidFullLenTX && tx.extendedStopCodon}}+" did not include the stop codon, but a valid stop codon was found)","debug");
      
      
      
      reportln("Starting TX GenomicArrayOfSets...","progress");
      val txgaos = new internalUtils.qcGtfAnnotationBuilder(gtffile=gtffile, flatgtffile = None, stranded = false, stdCodes = GtfCodes(), flatCodes = GtfCodes()).txArrayWithBuffer
      reportln("Finished TX GenomicArrayOfSets.","progress");
    
      
    def walkVCF(vcIter : Iterator[VariantContext],vcfHeader : VCFHeader, verbose : Boolean = true) : (Iterator[VariantContext],VCFHeader) = {
      val newHeaderLines = List(
            new VCFInfoHeaderLine(vcfCodes.txList_TAG, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String, "List of known transcripts found to overlap with the variant."),
            new VCFInfoHeaderLine(vcfCodes.vType_TAG, VCFHeaderLineCount.A, VCFHeaderLineType.String, "For each allele, a |-delimited list indicating the deletion type for each overlapping transcript (see "+vcfCodes.txList_TAG+" for the transcripts and "+vcfCodes.vMutLVL_TAG+" for info on the type description)"),
            new VCFInfoHeaderLine(vcfCodes.vMutG_TAG, VCFHeaderLineCount.A, VCFHeaderLineType.String, "For each allele, the genomic change, in HGVS format."),
            new VCFInfoHeaderLine(vcfCodes.vMutR_TAG, VCFHeaderLineCount.A, VCFHeaderLineType.String, "For each allele, a |-delimited list indicating mRNA change (in HGVS format) for each transcript (see "+vcfCodes.txList_TAG+")"),
            new VCFInfoHeaderLine(vcfCodes.vMutC_TAG, VCFHeaderLineCount.A, VCFHeaderLineType.String, "For each allele, a |-delimited list indicating cDNA change (in HGVS format) for each transcript (see "+vcfCodes.txList_TAG+")"),
            new VCFInfoHeaderLine(vcfCodes.vMutP_TAG, VCFHeaderLineCount.A, VCFHeaderLineType.String, "For each allele, a |-delimited list indicating amino-acid change (in HGVS format) for each transcript (see "+vcfCodes.txList_TAG+")"),
            new VCFInfoHeaderLine(vcfCodes.vTypeShort_TAG, VCFHeaderLineCount.A, VCFHeaderLineType.String, "For each allele, the worst amino acid change type found over all transcripts."),
            new VCFInfoHeaderLine(vcfCodes.vMutPShort_TAG, VCFHeaderLineCount.A, VCFHeaderLineType.String, "For each allele, one of the protein changes for the worst variant type found over all transcripts"),
            new VCFInfoHeaderLine(vcfCodes.vMutINFO_TAG, VCFHeaderLineCount.A, VCFHeaderLineType.String, "Raw variant info for each allele."),
            new VCFInfoHeaderLine(vcfCodes.vMutLVL_TAG, VCFHeaderLineCount.A, VCFHeaderLineType.String, 
                                       "For each allele, the rough deliteriousness level of the variant, over all transcripts. Possible values, in order of deliteriousness "+
                                       "SYNON (synonymous mutation), "+
                                       "PSYNON (Probably-synonymous, indicates that the variant is within a transcript's introns or near a genes endpoints), "+
                                       "UNK (unknown), "+
                                       "NONSYNON (Changes one or more amino acids, or loss of the stop codon), "+
                                       "PLOF (possible loss of function, includes variants that might break splice junctions, and loss of the start codon), and "+
                                       "LLOF (likely loss of function, includes frameshift indels and variants that add early stop codons")
      ) ++ ( if(txToGeneFile.isEmpty) List() else {
        List(
          new VCFInfoHeaderLine(vcfCodes.geneIDs, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String, "Gene Symbol for each tx.")    
        )
      }) ++ bedTags.map{ case (tagString,descString,bedFunction) => {
          new VCFInfoHeaderLine(tagString, 1, VCFHeaderLineType.Integer, "Variant is found on bed file "+descString)
      }}
      
      val newHeader = internalUtils.VcfTool.addHeaderLines(vcfHeader,newHeaderLines);
      
      reportln("Starting VCF read/write...","progress");
      
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
      
      //val writer = if(summaryFile.isEmpty) None else Some(openWriterSmart(summaryFile.get));
      
      
      return ((
        vcIter.map(v => {
             annotateVcfStreamOpt(v,summaryWriter,vcfCodes,bufferSize,txgaos,TXSeq,txToGene,geneVariantsOnly=geneVariantsOnly,bedTags=bedTags)
        }).filter(_.isDefined).map(_.get), newHeader ));
      }
    }
   
  def annotateVcfStreamOpt(v : VariantContext, writer : Option[WriterUtil], vcfCodes : VCFAnnoCodes,
                        bufferSize : Int, txgaos : GenomicArrayOfSets[String],
                        TXSeq : scala.collection.mutable.Map[String,TXUtil],
                        txToGene : Option[(String => String)],
                        geneVariantsOnly : Boolean,
                        bedTags : Seq[(String,String,internalUtils.commonSeqUtils.GenomicInterval => Boolean)]) : Option[VariantContext] = {
      var vb = new htsjdk.variant.variantcontext.VariantContextBuilder(v);
      var vTypeList = Vector[Vector[String]]();
      var vMutG = Vector[String]();
      var vMutR = Vector[Vector[String]]();
      var vMutC = Vector[Vector[String]]();
      var vMutP = Vector[Vector[String]]();
      
      var vMutPshort = Vector[String]();
      var vTypeListShort = Vector[String]();
      var vLevel = Vector[String]();
      
      var vInfo = Vector[Vector[String]]();
      
      val refAlle = v.getReference();
      val altAlleles = Range(0,v.getNAlleles()-1).map((a) => v.getAlternateAllele(a)).filter{ alt => alt.getBaseString() != "*" }
      if(altAlleles.length > 0){
        val start = v.getStart - 1
        val end = start + refAlle.length() // math.max(refAlle.length(), altAlleles.map(a => a.length()).max)
        val txList = txgaos.findIntersectingSteps(GenomicInterval(chromName = v.getContig(), strand = '.', start = start - bufferSize, end = end + bufferSize)).foldLeft(Set[String]()){case (soFar,(iv,gset))=>{ soFar ++ gset }}.filter(TXSeq.contains(_)).filter{TXSeq(_).isValidFullLenTX}.toVector.sorted;
        if(geneVariantsOnly && txList.length == 0) return None;
        
        txToGene match {
          case Some(fun) => {
            vb = vb.attribute(vcfCodes.geneIDs, txList.map(x => fun(x)).toList.asJava);
          }
          case None => {
            //do nothing
          }
        }
        if(! writer.isEmpty) {
              writer.get.write(v.getContig()+"\t"+start+"\t"+end+"\t"+""+"\t"+""+"\t"+""+"\t"+""+"\t"+""+"\t"+""+"\t"+""+"\t"+""+"\t \n");
              writer.get.write("\t\t\t"+v.getAttributeAsString("ANN","NA")+"\t"+v.getAttributeAsString("LOF","NA")+"\n");
        }
        
        for(altAlle <- altAlleles){
          val (ref,alt) = (refAlle.getBaseString(), altAlle.getBaseString());
          val mutG = "g."+getMutString(ref, alt, pos = start, getPosString = {(a) => (a + 1).toString},swapStrand=false);
          vMutG = vMutG :+ mutG
          vMutR = vMutR :+ Vector[String]();
          vMutC = vMutC :+ Vector[String]();
          vMutP = vMutP :+ Vector[String]();
          vTypeList = vTypeList :+ Vector[String]();
          vInfo = vInfo :+ Vector[String]();
          
          for(tx <- txList.map(TXSeq(_))){
            val mutR = "r."+tx.getRnaMut(ref,alt,start)  //getMutString(ref,alt, pos = start, getPosString = {(g) => tx.getRPos(g)}, swapStrand = (tx.strand == '-'));
            val mutC = "c."+tx.getCdsMut(ref,alt,start)  //getMutString(ref,alt, pos = start, getPosString = {(g) => tx.getCPos(g)}, swapStrand = (tx.strand == '-'));
            try{
              val mutP = tx.getProteinMut(ref,alt,gPos=start);
              vMutR = vMutR.updated(vMutR.length-1,vMutR.last :+ mutR);
              vMutC = vMutC.updated(vMutC.length-1,vMutC.last :+ mutC);
              
              
              vMutP = vMutP.updated(vMutP.length-1,vMutP.last :+ mutP.pvar);
              vTypeList = vTypeList.updated(vTypeList.length-1,vTypeList.last :+ mutP.varType);
              vInfo = vInfo.updated(vInfo.length-1,vInfo.last :+ mutP.saveToString());
              if(! writer.isEmpty) {
                writer.get.write(v.getContig()+":"+start+"-"+end+"\t"+ref+"\t"+alt+"\t"+tx.txID+"\t"+tx.strand+"\t"+
                                 mutG+"\t"+mutR+"\t"+mutC+"\t"+mutP.pvar+"\t"+mutP.varType+"\t"+mutP.cType+"\t"+mutP.severityType+"\t"+mutP.pType+"\t"+mutP.subType+
                                 "\n");
              }
            } catch {
              case e : Exception => {
                reportln("Error:","warn");
                reportln("TX:","warn");
                reportln(tx.toStringVerbose(),"warn");
                reportln("and variant: ","warn");
                reportln(mutG + "\t"+mutR+"\t"+mutC,"warn");
                throw e;
              }
            }
          }
          
          try{
            val (mutPshort,typeShort,vLvl) = internalUtils.TXUtil.getWorstProteinMut(vMutP.last.zip(vTypeList.last),txList);
            vMutPshort = vMutPshort :+ mutPshort;
            vTypeListShort = vTypeListShort :+ typeShort;
            vLevel = vLevel :+ vLvl;
          } catch {
              case e : Exception => {
                reportln("ERROR:","debug");
                reportln(v.getContig()+":"+start+"-"+end+"\t"+ref+"\t"+alt+"\t"+txList.mkString(",")+"\t"+ vMutP.map(_.mkString("|")).mkString(",")+"\t"+ vTypeList.map(_.mkString("|")).mkString(",")+"\n","debug");
                if( ! writer.isEmpty) writer.get.close();
                throw e;
              }
          }
        }
        
        //vb = vb.attribute(vcfCodes.txList_TAG, mkSubDelimString(txList,vcfCodes.delims));
        vb = vb.attribute(vcfCodes.txList_TAG, txList.toList.asJava);
        //vb = vb.attribute(vcfCodes.vType_TAG, mkSubDelimList(vTypeList,vcfCodes.delims).toList.asJava);
        vb = vb.attribute(vcfCodes.vMutG_TAG, vMutG.toList.asJava);
        vb = vb.attribute(vcfCodes.vMutR_TAG, mkSubDelimList(vMutR,vcfCodes.delims));
        vb = vb.attribute(vcfCodes.vMutC_TAG, mkSubDelimList(vMutC,vcfCodes.delims));
        vb = vb.attribute(vcfCodes.vMutP_TAG, mkSubDelimList(vMutP,vcfCodes.delims));
        vb = vb.attribute(vcfCodes.vType_TAG, mkSubDelimList(vTypeList,vcfCodes.delims));
        vb = vb.attribute(vcfCodes.vTypeShort_TAG, vTypeListShort.toList.asJava);
        vb = vb.attribute(vcfCodes.vMutPShort_TAG, vMutPshort.toList.asJava);
        vb = vb.attribute(vcfCodes.vMutLVL_TAG,  vLevel.toList.asJava);
        vb = vb.attribute(vcfCodes.vMutINFO_TAG, mkSubDelimList(vInfo,vcfCodes.delims));
        
        if(! bedTags.isEmpty){
          val variantIV = internalUtils.commonSeqUtils.GenomicInterval(v.getContig(),'.', start = v.getStart() - 1, end = math.max(v.getEnd(),v.getStart+1));
          bedTags.foreach{ case (tagString,desc,bedFunction) => {
            vb = vb.attribute(tagString, if(bedFunction(variantIV)) "1" else "0");
          }}
        }
      } else if(geneVariantsOnly){
        return None;
      }
      return Some(vb.make());
  }
  
  def annotateVcfStream(v : VariantContext, writer : Option[WriterUtil], vcfCodes : VCFAnnoCodes,
                        bufferSize : Int, txgaos : GenomicArrayOfSets[String],
                        TXSeq : scala.collection.mutable.Map[String,TXUtil],
                        txToGene : Option[(String => String)]) : VariantContext = {
      var vb = new htsjdk.variant.variantcontext.VariantContextBuilder(v);
      var vTypeList = Vector[Vector[String]]();
      var vMutG = Vector[String]();
      var vMutR = Vector[Vector[String]]();
      var vMutC = Vector[Vector[String]]();
      var vMutP = Vector[Vector[String]]();
      
      var vMutPshort = Vector[String]();
      var vTypeListShort = Vector[String]();
      var vLevel = Vector[String]();
      
      var vInfo = Vector[Vector[String]]();
      
      val refAlle = v.getReference();
      val altAlleles = Range(0,v.getNAlleles()-1).map((a) => v.getAlternateAllele(a));
      if(altAlleles.length > 0){
        val start = v.getStart - 1
        val end = start + refAlle.length() // math.max(refAlle.length(), altAlleles.map(a => a.length()).max)
        val txList = txgaos.findIntersectingSteps(GenomicInterval(chromName = v.getContig(), strand = '.', start = start - bufferSize, end = end + bufferSize)).foldLeft(Set[String]()){case (soFar,(iv,gset))=>{ soFar ++ gset }}.filter(TXSeq.contains(_)).filter{TXSeq(_).isValidFullLenTX}.toVector.sorted;
        txToGene match {
          case Some(fun) => {
            vb = vb.attribute(vcfCodes.geneIDs, txList.map(x => fun(x)).toList.asJava);
          }
          case None => {
            //do nothing
          }
        }
        if(! writer.isEmpty) {
              writer.get.write(v.getContig()+"\t"+start+"\t"+end+"\t"+""+"\t"+""+"\t"+""+"\t"+""+"\t"+""+"\t"+""+"\t"+""+"\t"+""+"\t \n");
              writer.get.write("\t\t\t"+v.getAttributeAsString("ANN","NA")+"\t"+v.getAttributeAsString("LOF","NA")+"\n");
        }
        
        for(altAlle <- altAlleles){
          val (ref,alt) = (refAlle.getBaseString(), altAlle.getBaseString());
          val mutG = "g."+getMutString(ref, alt, pos = start, getPosString = {(a) => (a + 1).toString},swapStrand=false);
          vMutG = vMutG :+ mutG
          vMutR = vMutR :+ Vector[String]();
          vMutC = vMutC :+ Vector[String]();
          vMutP = vMutP :+ Vector[String]();
          vTypeList = vTypeList :+ Vector[String]();
          vInfo = vInfo :+ Vector[String]();
          
          for(tx <- txList.map(TXSeq(_))){
            val mutR = "r."+tx.getRnaMut(ref,alt,start)  //getMutString(ref,alt, pos = start, getPosString = {(g) => tx.getRPos(g)}, swapStrand = (tx.strand == '-'));
            val mutC = "c."+tx.getCdsMut(ref,alt,start)  //getMutString(ref,alt, pos = start, getPosString = {(g) => tx.getCPos(g)}, swapStrand = (tx.strand == '-'));
            try{
              val mutP = tx.getProteinMut(ref,alt,gPos=start);
              vMutR = vMutR.updated(vMutR.length-1,vMutR.last :+ mutR);
              vMutC = vMutC.updated(vMutC.length-1,vMutC.last :+ mutC);
              
              
              vMutP = vMutP.updated(vMutP.length-1,vMutP.last :+ mutP.pvar);
              vTypeList = vTypeList.updated(vTypeList.length-1,vTypeList.last :+ mutP.varType);
              vInfo = vInfo.updated(vInfo.length-1,vInfo.last :+ mutP.saveToString());
              if(! writer.isEmpty) {
                writer.get.write(v.getContig()+":"+start+"-"+end+"\t"+ref+"\t"+alt+"\t"+tx.txID+"\t"+tx.strand+"\t"+
                                 mutG+"\t"+mutR+"\t"+mutC+"\t"+mutP.pvar+"\t"+mutP.varType+"\t"+mutP.cType+"\t"+mutP.severityType+"\t"+mutP.pType+"\t"+mutP.subType+
                                 "\n");
              }
            } catch {
              case e : Exception => {
                reportln("Error:","warn");
                reportln("TX:","warn");
                reportln(tx.toStringVerbose(),"warn");
                reportln("and variant: ","warn");
                reportln(mutG + "\t"+mutR+"\t"+mutC,"warn");
                throw e;
              }
            }
          }
          
          try{
            val (mutPshort,typeShort,vLvl) = internalUtils.TXUtil.getWorstProteinMut(vMutP.last.zip(vTypeList.last),txList);
            vMutPshort = vMutPshort :+ mutPshort;
            vTypeListShort = vTypeListShort :+ typeShort;
            vLevel = vLevel :+ vLvl;
          } catch {
              case e : Exception => {
                reportln("ERROR:","debug");
                reportln(v.getContig()+":"+start+"-"+end+"\t"+ref+"\t"+alt+"\t"+txList.mkString(",")+"\t"+ vMutP.map(_.mkString("|")).mkString(",")+"\t"+ vTypeList.map(_.mkString("|")).mkString(",")+"\n","debug");
                if( ! writer.isEmpty) writer.get.close();
                throw e;
              }
          }
          
        }
        
        //vb = vb.attribute(vcfCodes.txList_TAG, mkSubDelimString(txList,vcfCodes.delims));
        vb = vb.attribute(vcfCodes.txList_TAG, txList.toList.asJava);
        //vb = vb.attribute(vcfCodes.vType_TAG, mkSubDelimList(vTypeList,vcfCodes.delims).toList.asJava);
        vb = vb.attribute(vcfCodes.vMutG_TAG, vMutG.toList.asJava);
        vb = vb.attribute(vcfCodes.vMutR_TAG, mkSubDelimList(vMutR,vcfCodes.delims));
        vb = vb.attribute(vcfCodes.vMutC_TAG, mkSubDelimList(vMutC,vcfCodes.delims));
        vb = vb.attribute(vcfCodes.vMutP_TAG, mkSubDelimList(vMutP,vcfCodes.delims));
        vb = vb.attribute(vcfCodes.vType_TAG, mkSubDelimList(vTypeList,vcfCodes.delims));
        vb = vb.attribute(vcfCodes.vTypeShort_TAG, vTypeListShort.toList.asJava);
        vb = vb.attribute(vcfCodes.vMutPShort_TAG, vMutPshort.toList.asJava);
        vb = vb.attribute(vcfCodes.vMutLVL_TAG,  vLevel.toList.asJava);
        vb = vb.attribute(vcfCodes.vMutINFO_TAG, mkSubDelimList(vInfo,vcfCodes.delims));
      }
      return vb.make();
  }
  

  case class AddGroupInfoAnno(groupFile : Option[String], groupList : Option[String], superGroupList  : Option[String], chromList : Option[List[String]], 
             addCounts : Boolean = true, addFreq : Boolean = true, addMiss : Boolean = true, 
             addAlle : Boolean= true, addHetHom : Boolean = true, 
             sepRef : Boolean = true, countMissing : Boolean = true,
             noMultiAllelics : Boolean = false,
             vcfCodes : VCFAnnoCodes = VCFAnnoCodes()) extends internalUtils.VcfTool.VCFWalker {
    
    def walkVCF(vcIter : Iterator[VariantContext], vcfHeader : VCFHeader, verbose : Boolean = true) : (Iterator[VariantContext], VCFHeader) = {
  
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
  }
  
  class AddVariantDomainUtil extends CommandLineRunUtil {
     override def priority = 100;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "AddVariantDomains", 
          quickSynopsis = "", 
          synopsis = "", 
          description = " " + ALPHA_WARNING,   
          argList = 
                    new UnaryArgument( name = "countClinVar",
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
                                        ) ::
                    new FinalArgument[String](
                                         name = "domainSummaryOutFile",
                                         valueName = "domainSummaryOutFile.txt",
                                         argDesc = "output domain info file." // description
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
       
       AddVariantDomains(
                   domainFile = parser.get[String]("domainFile"),
                   countClinVar = parser.get[Boolean]("countClinVar"),
                   summaryFile = parser.get[String]("domainSummaryOutFile"),
                   gtfFile = parser.get[Option[String]]("gtfFile"),
                   txToGeneFile = parser.get[Option[String]]("txToGeneFile")
                   ).walkVCFFile(
                   infile    = parser.get[String]("invcf"),
                   outfile   = parser.get[String]("outvcf"),
                   chromList = parser.get[Option[List[String]]]("chromList")
                   )
     }   
    }
  }

  case class AddVariantDomains(
                 domainFile : String,
                 gtfFile : Option[String],
                 txToGeneFile : Option[String],
                 countClinVar : Boolean,
                 summaryFile : String,
                 vcfCodes : VCFAnnoCodes = VCFAnnoCodes()
             ) extends internalUtils.VcfTool.VCFWalker {
    
    
    reportln("Reading txToGene map... ["+internalUtils.stdUtils.getDateAndTimeString+"]","debug");
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
    reportln("Reading geneToTX map... ["+internalUtils.stdUtils.getDateAndTimeString+"]","debug");
    
    reportln("Reading txdata ... ["+internalUtils.stdUtils.getDateAndTimeString+"]","debug");
    val txData = gtfFile match {
      case Some(txf) => {
          val ipr = internalUtils.stdUtils.IteratorProgressReporter_ThreeLevel("lines", 200, 1000 , 2000 )
          val wrappedIter = internalUtils.stdUtils.wrapIteratorWithProgressReporter(getLinesSmartUnzip(txf) , ipr )
          val txArray : GenomicArrayOfSets[String] = GenomicArrayOfSets[String](false);
          wrappedIter.foreach{line => {
            val tx = buildTXUtilFromString(line);
            tx.gSpans.foreach{ case (start,end) => {
              txArray.addSpan(GenomicInterval(chromName = tx.chrom, strand = '.', start = start, end = end), tx.txID);
            }}
          }}
         txArray.finalizeStepVectors;
      }
      case None => GenomicArrayOfSets[String](false).finalizeStepVectors;
    }
    reportln("Reading txdata... ["+internalUtils.stdUtils.getDateAndTimeString+"]","debug"); 
    
    val domainClinCount = scala.collection.mutable.AnyRefMap[String,Array[Int]]();
    val domainClinCountBySev = scala.collection.mutable.AnyRefMap[String,Array[Array[Int]]]();
    
    val domainInfo = scala.collection.mutable.AnyRefMap[String,Seq[String]]();
    val domainLoci = scala.collection.mutable.AnyRefMap[String,(String,String,Set[(Int,Int)],(Int,Int))]();
    
    
    val domainTx = scala.collection.mutable.AnyRefMap[String,Set[String]]().withDefault(s => Set[String]());
    val geneArrayTemp : GenomicArrayOfSets[String] = GenomicArrayOfSets[String](false);

    getTableFromLines(getLinesSmartUnzip(domainFile),colNames = Seq("chrom","start","end","protSymbol","domainUID","txID","domainStartAA","domainEndAA")).zipWithIndex.foreach{
      //case (chrom :: start :: end :: protSymbol :: domainUID :: emptyList, lnct) => {
      case (lst,lnct) => {
        val chrom = lst(0);
        val start = string2int(lst(1));
        val end = string2int(lst(2));
        val protSymbol = lst(3);
        val domainUID = lst(4);
        val tx = lst(5);
        val aaStart = lst(6);
        val aaEndRaw = lst(7);
        
        val aaEnd = if(aaEndRaw.head == '>'){
          aaEndRaw.tail;
        } else {
          aaEndRaw;
        }
        
        val iv = GenomicInterval(chromName = chrom, strand = '.', start = start, end = end);
        geneArrayTemp.addSpan(iv,domainUID);
        domainClinCount(domainUID) = Array.ofDim[Int](9);
        domainClinCountBySev(domainUID) = Array.ofDim[Int](2,9);
        domainInfo(domainUID) = Vector(chrom , start.toString() , end.toString());
        if(domainLoci.contains(domainUID)){
          domainLoci(domainUID) = (chrom,tx, domainLoci(domainUID)._3 + ((start,end)), (string2int(aaStart),string2int(aaEnd))   );
        } else {
          domainLoci(domainUID) = (chrom,tx, Set[(Int,Int)]((start,end)), (string2int(aaStart),string2int(aaEnd))   );
        }
        
        val domainTxSet = txData.findIntersectingSteps(iv).foldLeft(Set[String]()){case (soFar,(iv,stepSet)) => soFar ++ stepSet};
        domainTx(domainUID) = domainTx(domainUID) ++ domainTxSet;
      }
      //case (x,lnct) => {
      //  error("Malformed table: "+domainFile + " line "+lnct+" has wrong number of columns ("+x.length+", should be 5)");
      //}
    }
    val geneArray = geneArrayTemp.finalizeStepVectors;
    
    val lvlList = Seq("SYNON","PSYNON","UNK","NONSYNON","PLOF","LLOF");
    
    def walkVCF(vcIter : Iterator[VariantContext], vcfHeader : VCFHeader, verbose : Boolean = true) : (Iterator[VariantContext],VCFHeader) = {
      
      val newHeaderLines = List(
            new VCFInfoHeaderLine(vcfCodes.domainIds,      VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String, "List of domains that the variant intersects with.")      
      );
      
      val newHeader = internalUtils.VcfTool.addHeaderLines(vcfHeader,newHeaderLines);
      reportln("Walking input VCF...","note")

      return ((addIteratorCloseAction(vcIter.map(v => {
         var vb = new htsjdk.variant.variantcontext.VariantContextBuilder(v);
         val start = v.getStart();
         val chrom = v.getContig();
         val domainList = geneArray.findIntersectingSteps(GenomicInterval(chromName = chrom, strand = '.', start = start, end = start + 1)).foldLeft(Set[String]()){ case (soFar, (iv,currSet)) => {
           soFar ++ currSet;
         }}.toVector.sorted;
         
         if(countClinVar){
           val clnSigRaw = v.getAttributeAsInt(vcfCodes.CLNVAR_SUMSIG, 0);
           val clnSig = if(clnSigRaw == 255) 8 else clnSigRaw;
           domainList.foreach(domainUID => {
             domainClinCount(domainUID)(clnSig) = domainClinCount(domainUID)(clnSig) + 1;
           })
           val varLevel = v.getAttributeAsString(vcfCodes.vMutLVL_TAG,"?");
           val nonSynonStatus = if(varLevel == "NONSYNON" || varLevel == "PLOF" || varLevel == "LLOF") 1 else 0;
           domainList.foreach(domainUID => {
             domainClinCountBySev(domainUID)(nonSynonStatus)(clnSig) = domainClinCountBySev(domainUID)(nonSynonStatus)(clnSig) + 1;
           })
         }
         
         vb = vb.attribute( vcfCodes.domainIds, domainList.mkString(",") );
         //val cs = internalUtils.CalcACMGVar.getSummaryClinSig(clnsig);
         //vb = vb.attribute(vcfCodes.CLNVAR_SUMSIG,cs);
         
         vb.make();
      }), closeAction = () => {
        val writer = openWriterSmart(summaryFile + ".txt.gz");
        val writer2 = openWriterSmart(summaryFile + ".onGene.txt.gz");
        //writer.write("");
        
        val titleLine = "domainUID\tchrom\tgStart\tgEnd\tgenomicSpans\ttxID\taaStart\taaEnd\t"+
                     "txList\tgeneList\t"+
                     Range(0,8).map("ClnSig_"+_).mkString("\t")+"\t"+"ClnSig_255"+"\t"+
                     Range(0,8).map("SYN_ClnSig_"+_).mkString("\t")+"\t"+"SYN_ClnSig_255"+"\t"+
                     Range(0,8).map("NS_ClnSig_"+_).mkString("\t")+"\t"+"NS_ClnSig_255"+"\n";
        writer.write(titleLine);
        writer2.write(titleLine);
        
        domainClinCount.keys.toVector.sorted.foreach(d => {
          val txList = domainTx(d).toVector.sorted;
          val geneList = domainTx(d).map(tx => txToGene(tx)).toSet.toVector.sorted;
            writer.write(d + "\t"+ 
                       domainLoci(d)._1 + "\t" +
                       domainLoci(d)._3.map(_._1).min + "\t"+
                       domainLoci(d)._3.map(_._2).max + "\t"+
                       domainLoci(d)._3.toVector.sorted.map{case (s,e) => { s + "-" + e}}.mkString(",")+"\t"+
                       domainLoci(d)._2 + "\t"+
                       domainLoci(d)._4._1+"\t"+
                       domainLoci(d)._4._2+"\t"+
                       txList.mkString(",") + "\t" + 
                       geneList.mkString(",") + "\t"+
                       domainClinCount(d).mkString("\t")+"\t"+
                       domainClinCountBySev(d)(0).mkString("\t")+"\t"+
                       domainClinCountBySev(d)(1).mkString("\t")+
                       "\n");
        })
        writer.close();

        domainClinCount.keys.toVector.map(d => {
          val geneList = domainTx(d).map(tx => txToGene(tx)).toSet.toVector.sorted;
          val (start,end) = (domainLoci(d)._3.map(_._1).min,domainLoci(d)._3.map(_._2).max);
          (geneList,(start,end),d);
        }).filter{case (geneList,(start,end),d) => {
          geneList.size > 0;
        }}.sortBy{case (geneList,(start,end),d) => {
          (geneList.mkString(","),start,end);
        }}.foreach{ case (geneList,(start,end),d) => {
            val txList = domainTx(d).toVector.sorted;
            writer2.write(d + "\t"+
                       domainLoci(d)._1 + "\t" +
                       domainLoci(d)._3.map(_._1).min + "\t"+
                       domainLoci(d)._3.map(_._2).max + "\t"+
                       domainLoci(d)._3.toVector.sorted.map{case (s,e) => { s + "-" + e}}.mkString(",")+"\t"+
                       domainLoci(d)._2 + "\t"+
                       domainLoci(d)._4._1+"\t"+
                       domainLoci(d)._4._2+"\t"+
                       txList.mkString(",") + "\t" + 
                       geneList.mkString(",") + "\t"+
                       domainClinCount(d).mkString("\t")+"\t"+
                       domainClinCountBySev(d)(0).mkString("\t")+"\t"+
                       domainClinCountBySev(d)(1).mkString("\t")+
                       "\n");
        }}
        writer2.close();
      }), newHeader));
    }
  }
  

  case class RedoDBNSFPannotation( dbnsfpfile : String, chromStyle : String, chromList : Option[List[String]], 
                                   posFieldTitle : String,chromFieldTitle : String, altFieldTitle : String,
                                   singleDbFile : Boolean,
                                   dbFileDelim : String,
                                   tagPrefix : String,
                                   dropTags : Option[List[String]],keepTags : Option[List[String]]
                                  ) extends internalUtils.VcfTool.VCFWalker {
    
    //if(! filesByChrom){
    //  error("Fatal error: operation with the --singleDbFile flag is not yet supported!");
    //}
    
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
    
    def walkVCF(vcIter : Iterator[VariantContext], vcfHeader : VCFHeader, verbose : Boolean = true) : (Iterator[VariantContext],VCFHeader) = { 
      
      val newHeaderLines = List(
        new VCFInfoHeaderLine(tagPrefix+"FoundAtPos", 1, VCFHeaderLineType.Integer, "Equal to 1 if and only if any dbNSFP line was found at the given position."),
        new VCFInfoHeaderLine(tagPrefix+"Found", 1, VCFHeaderLineType.Integer, "Equal to the number of lines found at the given position and matching the given alt allele.")
      ) ++ tagsToWrite.toList.zipWithIndex.map{case (title,i) => {
        new VCFInfoHeaderLine(tagPrefix+title, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String, "Info from column "+title+" (col "+i+") of dbNSFP file ("+dbnsfpfile+")");
      }}
      
      val newHeader = internalUtils.VcfTool.addHeaderLines(vcfHeader,newHeaderLines);
      
      ((vcIter.map(vc => walkVC(vc)),newHeader));
      
    }
    
    def walkVC(vc : VariantContext) : VariantContext = {
      var vb = new htsjdk.variant.variantcontext.VariantContextBuilder(vc);
      
      val chrom = vc.getContig();
      val pos = vc.getStart();
      val isInDB = shiftToPosition(chrom,pos);
      //val altAlleles = Range(0,vc.getNAlleles()-1).map((a) => vc.getAlternateAllele(a)).zipWithIndex.filter{case (alt,altIdx) => { alt.getBaseString() != "*" }}
      val altAllelesRaw = internalUtils.VcfTool.getAllelesInOrder(vc).toVector.tail.zipWithIndex
      val altAlles = altAllelesRaw.filter{case (alt,altIdx) => { alt.getBaseString() != "*" }}
      if(altAlles.length > 1){
        error("Fatal Error: wrong number of alt alleles!")
      }
      if(isInDB) vb = vb.attribute(tagPrefix+"FoundAtPos","1");
      else vb = vb.attribute(tagPrefix+"FoundAtPos","0");
      
      if(isInDB && altAlles.length > 0 && currPositionMap.exists{ case (key,c) => key == altAlles.head._1.getBaseString() } ){
        val alt = altAlles.head._1.getBaseString();
        val matches = currPositionMap.filter{ case (key,c) => key == alt }.map{ case (key,c) => c};
        
        if(matches.length == 1){
          matches.head.zip(dbHeader).filter{ case (v,title) => {
            tagsToWrite.contains(title)
          }}.foreach{ case (v,title) => {
            vb = vb.attribute(tagPrefix+title,cleanInfoField(v));
          }}
        } else {
          dbHeader.zipWithIndex.filter{ case (tag,idx) => {
            tagsToWrite.contains(tag);
          }}.foreach{ case (tag,idx) => {
            vb = vb.attribute(tagPrefix+tag,matches.map{ case varray => cleanInfoField(varray(idx)) }.filter( v => v != "." ).padTo(1,".").mkString(","));
          }}
        }
 
        vb = vb.attribute(tagPrefix+"Found",matches.length);
      } else {
        tagsToWrite.foreach(title => {
          vb = vb.attribute(tagPrefix+title,".");
        })
        vb = vb.attribute(tagPrefix+"Found","0");
      }
      
      return vb.make();
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
  
  
  class redoDBNSFP extends CommandLineRunUtil {
     override def priority = 20;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "redoDBNSFP", 
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
                                         name = "dbFileDelim", 
                                         arg = List("--dbFileDelim"), 
                                         valueName = "delim",  
                                         argDesc =  ".",
                                         defaultValue = Some("\t")
                                        ) ::
                    new BinaryArgument[String](
                                         name = "chromStyle", 
                                         arg = List("--chromStyle"), 
                                         valueName = "hg19",  
                                         argDesc =  ".",
                                         defaultValue = Some("hg19")
                                        ) ::
                    new BinaryOptionArgument[List[String]](
                                         name = "chromList", 
                                         arg = List("--chromList"), 
                                         valueName = "chr1,chr2,...",  
                                         argDesc =  "List of chromosomes. If supplied, then all analysis will be restricted to these chromosomes. All other chromosomes wil be ignored."
                                        ) ::
                    new BinaryArgument[String](
                                         name = "posFieldTitle", 
                                         arg = List("--posFieldTitle"), 
                                         valueName = "pos",  
                                         argDesc =  ".",
                                         defaultValue = Some("pos(1-coor)")
                                        ) ::
                    new BinaryArgument[String](
                                         name = "chromFieldTitle", 
                                         arg = List("--chromFieldTitle"), 
                                         valueName = "chr",  
                                         argDesc =  ".",
                                         defaultValue = Some("chr")
                                        ) ::     
                    new BinaryArgument[String](
                                         name = "altFieldTitle", 
                                         arg = List("--altFieldTitle"), 
                                         valueName = "alt",  
                                         argDesc =  ".",
                                         defaultValue = Some("alt")
                                        ) ::     
                    new BinaryArgument[String](
                                         name = "tagPrefix", 
                                         arg = List("--tagPrefix"), 
                                         valueName = "prefix",  
                                         argDesc =  ".",
                                         defaultValue = Some(OPTION_TAGPREFIX+"dbNSFP_")
                                        ) :: 
                    new BinaryOptionArgument[List[String]](
                                         name = "keepTags", 
                                         arg = List("--keepTags"), 
                                         valueName = "tag1,tag2,...",  
                                         argDesc =  ""
                                        ) ::
                    new BinaryOptionArgument[List[String]](
                                         name = "dropTags", 
                                         arg = List("--dropTags"), 
                                         valueName = "tag1,tag2,...",  
                                         argDesc =  ""
                                        ) ::
                    new FinalArgument[String](
                                         name = "infile",
                                         valueName = "variants.vcf",
                                         argDesc = "input VCF file. Can be gzipped or in plaintext." // description
                                        ) ::
                    new FinalArgument[String](
                                         name = "dbnsfpfile",
                                         valueName = "dbNSFP3.0b2a.txt.gz",
                                         argDesc = "A gene annotation GTF file. Can be gzipped or in plaintext." // description
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
         RedoDBNSFPannotation(
             dbnsfpfile = parser.get[String]("dbnsfpfile"),
             chromStyle = parser.get[String]("chromStyle"), 
             chromList = parser.get[Option[List[String]]]("chromList"),
            // filesByChrom = ! parser.get[Boolean]("singleDbFile"),
             posFieldTitle = parser.get[String]("posFieldTitle"),
             chromFieldTitle = parser.get[String]("chromFieldTitle"),
             altFieldTitle = parser.get[String]("altFieldTitle"),
             singleDbFile = parser.get[Boolean]("singleDbFile"),
             dbFileDelim = parser.get[String]("dbFileDelim"),
             tagPrefix = parser.get[String]("tagPrefix"),
             dropTags = parser.get[Option[List[String]]]("dropTags"),
             keepTags = parser.get[Option[List[String]]]("keepTags")
         ).walkVCFFile(
             infile = parser.get[String]("infile"),
             outfile = parser.get[String]("outfile"),
             chromList = parser.get[Option[List[String]]]("chromList")
         )
       }
     }
    
  }
  
  

  case class DEPRECATED_FixEnsemblMerge2(inputVCFs : Seq[String], inputVcfTypes : Seq[String], 
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
            new SVcfCompoundHeaderLine(in_tag = "INFO",ID = OPTION_TAGPREFIX+""+ mc + "_" + infoLine.ID, Number = num, Type = infoLine.Type, desc = "For the caller "+mc+", " + cleanQuotes(infoLine.desc))
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
                   (OPTION_TAGPREFIX+""+ masterCaller.get + "_" + infoTag , infoValue)
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
  
/*
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
            (infoLine.ID,OPTION_TAGPREFIX+""+ mc + "_" + infoLine.ID,new SVcfCompoundHeaderLine(in_tag = "INFO",ID = OPTION_TAGPREFIX+""+ mc + "_" + infoLine.ID, Number = num, Type = infoLine.Type, desc = "For the caller "+mc+", " + cleanQuotes(infoLine.desc)))
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
    
  }*/
  
}




