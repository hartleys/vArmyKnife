package internalUtils

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

import htsjdk.variant._;
import htsjdk.variant.variantcontext._;
import htsjdk.variant.vcf._;

import internalUtils.genomicUtils._;
import internalUtils.commonSeqUtils._;

import scala.collection.mutable.AnyRefMap;



object VcfTool {
  
  val UNKNOWN_ALT_TAG_STRING = "*";
  val MISSING_VALUE_STRING = ".";
   
  val TOP_LEVEL_VCF_TAG : String = OPTION_TAGPREFIX;
  
  def getAttributeAsStringList(vc : VariantContext, tag : String) : Seq[String] = {
    val lst = vc.getAttributeAsList(tag).asScala;
    if(lst.length == 1){
      return( lst.head.toString.split(",") );
    } else {
      return( lst.map(_.toString()) );
    }
  }
  //abstract class SVcfHeaderCode[A](tag : String, Number : String, Type: String, desc : 
  
  //object SVcfHeaderCodes{
    
  //}
  //vMutINFO_TAG,numSplit_TAG,splitIdx_TAG,splitAlle_TAG
   
    
  case class VCFAnnoCodes(
        txList_TAG : String = TOP_LEVEL_VCF_TAG+ "tx_txList",
        vType_TAG : String = TOP_LEVEL_VCF_TAG+"tx_varType",
        vTxLvl_TAG : String = TOP_LEVEL_VCF_TAG+"tx_varLvl",
        vMutR_TAG : String = TOP_LEVEL_VCF_TAG+"tx_varR",
        vMutC_TAG : String = TOP_LEVEL_VCF_TAG+"tx_varC",
        vMutP_TAG : String = TOP_LEVEL_VCF_TAG+"tx_varPredP",
        
        vMutG_TAG : String = TOP_LEVEL_VCF_TAG+"txSummary_varG",
        vTypeShort_TAG : String = TOP_LEVEL_VCF_TAG+"txSummary_varType",
        vMutPShort_TAG : String = TOP_LEVEL_VCF_TAG+"txSummary_varPredP",
        vMutLVL_TAG : String = TOP_LEVEL_VCF_TAG + "txSummary_varLvl",
        vMutINFO_TAG : String = TOP_LEVEL_VCF_TAG + "varRAWDATA",
        
        /*
                   List(
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.grpAF_TAG + g, anum, "Float", "The alt allele frequency"+groupDesc+adesc+"."),
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.grpHomFrq_TAG + g, anum, "Float", "The frequency of homalt calls"+groupDesc  +adesc+ "."),
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.grpHetFrq_TAG + g, anum, "Float", "The frequency of het calls"+groupDesc  +adesc+ "."),
          new SVcfCompoundHeaderLine("INFO" ,vcfCodes.grpAltFrq_TAG + g, anum, "Float", "The frequency of calls that are het or homalt"+groupDesc  +adesc+ "."),
          new SVcfCompoundHeaderLine("INFO" ,vcfCodes.grpMisFrq_TAG + g, anum, "Integer", "The frequency of calls that are missing"+groupDesc  +adesc+ "."),
          new SVcfCompoundHeaderLine("INFO" ,vcfCodes.grpOthFrq_TAG + g, anum, "Integer", "The frequency of calls include a different allele"+groupDesc  +adesc+ ".")
          )
        } else { List() }) ++ 
        (if(addCounts){
          List(
          new SVcfCompoundHeaderLine("INFO" ,vcfCodes.grpHomCt_TAG + g, anum, "Integer", "The number of homalt calls"+groupDesc +adesc+ "."),
          new SVcfCompoundHeaderLine("INFO" ,vcfCodes.grpHetCt_TAG + g, anum, "Integer", "The number of het calls"+groupDesc +adesc+ "."),
          new SVcfCompoundHeaderLine("INFO" ,vcfCodes.grpAltCt_TAG + g,   anum, "Integer", "The number of calls that are het or homalt"+groupDesc  +adesc+ "."),
          new SVcfCompoundHeaderLine("INFO" ,vcfCodes.grpMisCt_TAG + g, anum, "Integer", "The number of calls that are missing"+groupDesc  +adesc+ "."),
          new SVcfCompoundHeaderLine("INFO" ,vcfCodes.grpOthCt_TAG + g, anum,  "Integer", "The number of calls include a different allele"+groupDesc  +adesc+ "."),
        )
         * 
         */

        //OLD ones:
        grpAC_TAG : String = TOP_LEVEL_VCF_TAG+"CT_AC_GRP_",
        grpAF_TAG : String = TOP_LEVEL_VCF_TAG+"CT_AF_GRP_",
        grpHomCt_TAG : String = TOP_LEVEL_VCF_TAG+"CT_HomCt_GRP_",
        grpHetCt_TAG : String = TOP_LEVEL_VCF_TAG+"CT_HetCt_GRP_",
        grpHomFrq_TAG : String = TOP_LEVEL_VCF_TAG+"CT_HomFrq_GRP_",
        grpHetFrq_TAG : String = TOP_LEVEL_VCF_TAG+"CT_HetFrq_GRP_",
        grpMisCt_TAG : String = TOP_LEVEL_VCF_TAG+"CT_MisCt_GRP_",
        grpMisFrq_TAG : String = TOP_LEVEL_VCF_TAG+"CT_MisFrq_GRP_",
        grpRefAC_TAG : String = TOP_LEVEL_VCF_TAG+"CT_AC_REF_GRP_",
        grpRefAF_TAG : String = TOP_LEVEL_VCF_TAG+"CT_AF_REF_GRP_",
        
        grpAltAC_TAG : String = TOP_LEVEL_VCF_TAG+"CT_AC_ALT_GRP_",
        grpAltHomCt_TAG : String = TOP_LEVEL_VCF_TAG+"CT_HomCt_ALT_GRP_",
        grpAltHetCt_TAG : String = TOP_LEVEL_VCF_TAG+"CT_HetCt_ALT_GRP_",
        grpRefHomCt_TAG : String = TOP_LEVEL_VCF_TAG+"CT_HomCt_REF_GRP_",
        grpRefHetCt_TAG : String = TOP_LEVEL_VCF_TAG+"CT_HetCt_REF_GRP_",
        
        
        
        
        CLNVAR_SUMSIG : String = TOP_LEVEL_VCF_TAG+"ClinVar_SumSig",
        CLNVAR_SUMSIGWARN : String = TOP_LEVEL_VCF_TAG+"ClinVar_SumSig_Warning",
        CLNVAR_SUMSIG_SAFE : String = TOP_LEVEL_VCF_TAG+"ClinVar_SumSig_NoWarn",
        
        domainIds : String = TOP_LEVEL_VCF_TAG+"domainIdList",
        
        isSplitMulti_TAG : String = TOP_LEVEL_VCF_TAG+"split_isSplitMult",
        splitIdx_TAG     : String = TOP_LEVEL_VCF_TAG+"split_alleIdx",
        numSplit_TAG     : String = TOP_LEVEL_VCF_TAG+"split_numAlle",
        splitAlle_TAG     : String = TOP_LEVEL_VCF_TAG+"split_fullAlleList",
        splitAlleWARN_TAG : String = TOP_LEVEL_VCF_TAG+"split_WARN",
        
        assess_IsRepetitive : String = TOP_LEVEL_VCF_TAG+"locusIsRep",
        assess_IsConserved : String = TOP_LEVEL_VCF_TAG+"locusIsCons",
        assess_IsHotspot : String = TOP_LEVEL_VCF_TAG+"locusIsHotspot",
        assess_domain : String = TOP_LEVEL_VCF_TAG+"locusDomain",
        assess_IsMappable : String = TOP_LEVEL_VCF_TAG+"locusIsMappable",
        assess_pseudogene : String = TOP_LEVEL_VCF_TAG+"locusIsPseudogene", 

        assess_criteria : String = TOP_LEVEL_VCF_TAG+"ACMG_criteria",
        
        assess_GeneSenseLOF :String = TOP_LEVEL_VCF_TAG+"ACMG_GeneSenseLOF",
        assess_GeneSenseMis :String = TOP_LEVEL_VCF_TAG+"ACMG_GeneSenseMis",
        assess_ctrlAFMIN : String = TOP_LEVEL_VCF_TAG+"ACMG_ctrlAFMIN",
        assess_ctrlAFMAX : String = TOP_LEVEL_VCF_TAG+"ACMG_ctrlAFMAX",
        
        assess_geneList : String =  TOP_LEVEL_VCF_TAG+"ANNO_genes",
        assess_geneTxList : String =  TOP_LEVEL_VCF_TAG+"ANNO_genes_allTxForGene",
        assess_geneLofTxRatio : String =  TOP_LEVEL_VCF_TAG+"ANN_genes_lofTxRatio",
        assess_geneMisTxRatio : String =  TOP_LEVEL_VCF_TAG+"ANNO_genes_misTxRatio",
        assess_refSeqKnown : String = TOP_LEVEL_VCF_TAG+"ANNO_debug_genes_canonIsKnown",

        assess_LofGenes : String =  TOP_LEVEL_VCF_TAG+"ANNO_geneList_LOF",
        assess_MisGenes : String =  TOP_LEVEL_VCF_TAG+"ANNO_geneList_mis",
        assess_CodingGenes : String =  TOP_LEVEL_VCF_TAG+"ANNO_geneList_coding",
        assess_NonsynonGenes : String = TOP_LEVEL_VCF_TAG+"ANNO_geneList_nonSynon",
        
        assess_LofTX : String =  TOP_LEVEL_VCF_TAG+"ANNO_txList_LOF",
        assess_MisTX : String =  TOP_LEVEL_VCF_TAG+"ANNO_txList_mis",
        assess_CodingTX : String = TOP_LEVEL_VCF_TAG+"ANNO_txList_coding",
        assess_NonsynonTX : String = TOP_LEVEL_VCF_TAG+"ANNO_txList_nonSynon",
        
        //assess_refSeqLOF : String = TOP_LEVEL_VCF_TAG+"ACMG_geneCanonLOF",
        //assess_refSeqMis : String = TOP_LEVEL_VCF_TAG+"ACMG_geneCanonMis",
        
        assess_PVS1 : String = TOP_LEVEL_VCF_TAG+"ACMG_PVS1",
        assess_PS1 : String = TOP_LEVEL_VCF_TAG+"ACMG_PS1",
        assess_PM1 : String = TOP_LEVEL_VCF_TAG+"ACMG_PM1",
        assess_PM2 : String = TOP_LEVEL_VCF_TAG+"ACMG_PM2",
        assess_PM4 : String = TOP_LEVEL_VCF_TAG+"ACMG_PM4",
        assess_PM5 : String = TOP_LEVEL_VCF_TAG+"ACMG_PM5",
        assess_PP2 : String = TOP_LEVEL_VCF_TAG+"ACMG_PP2",
        assess_PP3 : String = TOP_LEVEL_VCF_TAG+"ACMG_PP3",
        assess_BP1 : String = TOP_LEVEL_VCF_TAG+"ACMG_BP1",
        assess_BP2 : String = TOP_LEVEL_VCF_TAG+"ACMG_BP2",
        assess_BP3 : String = TOP_LEVEL_VCF_TAG+"ACMG_BP3",
        assess_BP4 : String = TOP_LEVEL_VCF_TAG+"ACMG_BP4",
        assess_BP7 : String = TOP_LEVEL_VCF_TAG+"ACMG_BP7",
        assess_BS1 : String = TOP_LEVEL_VCF_TAG+"ACMG_BS1",
        assess_BS2 : String = TOP_LEVEL_VCF_TAG+"ACMG_BS2",
        assess_BA1 : String = TOP_LEVEL_VCF_TAG+"ACMG_BA1",
        
        assess_RATING : String = TOP_LEVEL_VCF_TAG+"ACMG_RATING",
        assess_WARNINGS : String = TOP_LEVEL_VCF_TAG+"ACMG_WARN",
        assess_WARNFLAG : String = TOP_LEVEL_VCF_TAG+"ACMG_WARNFLAG",
        
        assess_PVS1_CANON : String = TOP_LEVEL_VCF_TAG+"ACMG_PVS1_CANON",
        assess_PP2_CANON : String = TOP_LEVEL_VCF_TAG+"ACMG_PP2_CANON",
        assess_BP1_CANON : String = TOP_LEVEL_VCF_TAG+"ACMG_BP1_CANON",
        assess_BP3_CANON : String = TOP_LEVEL_VCF_TAG+"ACMG_BP3_CANON",
        assess_PM4_CANON : String = TOP_LEVEL_VCF_TAG+"ACMG_PM4_CANON",
        assess_PS1_CANON : String = TOP_LEVEL_VCF_TAG+"ACMG_PS1_CANON",
        assess_PM5_CANON : String = TOP_LEVEL_VCF_TAG+"ACMG_PM5_CANON",
        assess_BP7_CANON : String = TOP_LEVEL_VCF_TAG+"ACMG_BP7_CANON",
        assess_RATING_CANON : String = TOP_LEVEL_VCF_TAG+"ACMG_RATING_CANON", 
        
        //NEW: 
        assess_ACMG_numGenes : String = TOP_LEVEL_VCF_TAG+"ACMG_NUM_GENES",
        assess_ACMG_numGenes_CANON : String = TOP_LEVEL_VCF_TAG+"ACMG_NUM_GENES_CANON",
        
        
        assess_forBT_codonLOF : String = TOP_LEVEL_VCF_TAG+"ASSESS_LOF_codonPathoRS",
        assess_forBT_downstreamLOF  : String = TOP_LEVEL_VCF_TAG+"ASSESS_LOF_downstreamPathoRS",
        
        assess_forBT_codonLOF_CANON : String = TOP_LEVEL_VCF_TAG+"ASSESS_LOF_codonPathoRS_CANON",
        assess_forBT_downstreamLOF_CANON : String = TOP_LEVEL_VCF_TAG+"ASSESS_LOF_downstreamPathoRS_CANON",
        
        assess_forBT_geneHasDownstreamLOF : String = TOP_LEVEL_VCF_TAG+"GENEINFO_geneHasPatho_DownstreamLOF",
        assess_forBT_geneHasStartLoss : String = TOP_LEVEL_VCF_TAG+"GENEINFO_geneHasPatho_StartLoss",
        assess_forBT_geneHasStopLoss : String = TOP_LEVEL_VCF_TAG+"GENEINFO_geneHasPatho_StopLoss",
        assess_forBT_geneHasDownstreamLOF_CANON : String = TOP_LEVEL_VCF_TAG+"GENEINFO_geneHasPatho_DownstreamLOF_CANON",
        assess_forBT_geneHasStartLoss_CANON : String = TOP_LEVEL_VCF_TAG+"GENEINFO_geneHasPatho_StartLoss_CANON",
        assess_forBT_geneHasStopLoss_CANON : String = TOP_LEVEL_VCF_TAG+"GENEINFO_geneHasPatho_StopLoss_CANON",
        
        //NEW???????????????
        assess_effectMatchInfo : String = TOP_LEVEL_VCF_TAG+"ACMG_cvInfo_EffectMatch",
        assess_nearEffectMatchInfo : String = TOP_LEVEL_VCF_TAG+"ACMG_cvInfo_NearEffectMatch",
        assess_effectMatchInfo_CANON : String = TOP_LEVEL_VCF_TAG+"ACMG_cvInfo_EffectMatch_CANON",
        assess_nearEffectMatchInfo_CANON : String = TOP_LEVEL_VCF_TAG+"ACMG_cvInfo_NearEffectMatch_CANON",
        
        assess_pathoEffectMatchRS : String = TOP_LEVEL_VCF_TAG+"ACMG_cvPath_EffectMatchRS",
        assess_pathoNearEffectMatchRS : String = TOP_LEVEL_VCF_TAG+"ACMG_cvPath_NearEffectMatchRS",
        assess_pathoEffectMatchRS_CANON : String = TOP_LEVEL_VCF_TAG+"ACMG_cvPath_EffectMatchRS_CANON",
        assess_pathoNearEffectMatchRS_CANON : String = TOP_LEVEL_VCF_TAG+"ACMG_cvPath_NearEffectMatchRS_CANON",
        
        assess_benEffectMatchRS : String = TOP_LEVEL_VCF_TAG+"ACMG_cvBen_EffectMatchRS",
        assess_benNearEffectMatchRS : String = TOP_LEVEL_VCF_TAG+"ACMG_cvBen_NearEffectMatchRS",
        assess_benEffectMatchRS_CANON : String = TOP_LEVEL_VCF_TAG+"ACMG_cvBen_EffectMatchRS_CANON",
        assess_benNearEffectMatchRS_CANON : String = TOP_LEVEL_VCF_TAG+"ACMG_cvBen_NearEffectMatchRS_CANON",
        ////////////
        assess_varInfoPrefix : String = TOP_LEVEL_VCF_TAG+"varDB",
         
        
        assess_exactMatchInfo : String = TOP_LEVEL_VCF_TAG+"varDB_info_ExactMatch",
        assess_aminoMatchInfo : String = TOP_LEVEL_VCF_TAG+"varDB_info_AminoMatch",
        assess_nearMatchInfo : String = TOP_LEVEL_VCF_TAG+"varDB_info_NearMatch",
        
        assess_pathoExactMatchRS : String = TOP_LEVEL_VCF_TAG+"varDB_pathoID_ExactMatch",
        assess_pathoAminoMatchRS : String = TOP_LEVEL_VCF_TAG+"varDB_pathoID_AminoMatch",
        assess_pathoNearMatchRS : String = TOP_LEVEL_VCF_TAG+"varDB_pathoID_NearMatch",
        assess_benignExactMatchRS : String = TOP_LEVEL_VCF_TAG+"varDB_benignID_ExactMatch",
        assess_benignAminoMatchRS : String = TOP_LEVEL_VCF_TAG+"varDB_benignID_AminoMatch",
        assess_benignNearMatchRS : String = TOP_LEVEL_VCF_TAG+"varDB_benignID_NearMatch",
        
        assess_pathoAminoMatchRS_CANON : String = TOP_LEVEL_VCF_TAG+"varDB_pathoID_AminoMatch_CANON",
        assess_pathoNearMatchRS_CANON : String = TOP_LEVEL_VCF_TAG+"varDB_pathoID_NearMatch_CANON",
        assess_aminoMatchInfo_CANON : String = TOP_LEVEL_VCF_TAG+"varDB_info_AminoMatch_CANON",
        assess_nearMatchInfo_CANON : String = TOP_LEVEL_VCF_TAG+"varDB_info_NearMatch_CANON",
        

        assess_benignAminoMatchRS_CANON : String = TOP_LEVEL_VCF_TAG+"varDB_benignID_AminoMatch_CANON",
        assess_benignNearMatchRS_CANON : String = TOP_LEVEL_VCF_TAG+"varDB_benignID_NearMatch_CANON",
        
        
        assess_inSilicoSummary : String = TOP_LEVEL_VCF_TAG+"ACMG_inSilicoSummary",
        
        geneIDs : String = TOP_LEVEL_VCF_TAG+"txGeneIDs",
        
        ec_CallMismatch : String = TOP_LEVEL_VCF_TAG+"EC_CallMismatch",
        ec_CallMismatchStrict : String = TOP_LEVEL_VCF_TAG+"EC_CallMismatch_Strict",
        ec_EnsembleWarnings : String = TOP_LEVEL_VCF_TAG+"EC_ENSEMBLE_WARNINGS",
        ec_alle_callerSets : String = TOP_LEVEL_VCF_TAG+"EC_alle_callerSets",
        ec_singleCallerAllePrefix : String = TOP_LEVEL_VCF_TAG+"EC_altAlle_",
        delims : Vector[String] = Vector(",","|"),
        
        CT_INFIX : String = ""
      ){
    
        var nAF_TAG : String = TOP_LEVEL_VCF_TAG+"CT_"+CT_INFIX+"AF"
        var nHomCt_TAG : String = TOP_LEVEL_VCF_TAG+"CT_"+CT_INFIX+"HomCt"
        var nHetCt_TAG : String = TOP_LEVEL_VCF_TAG+"CT_"+CT_INFIX+"HetCt"
        var nAltCt_TAG : String = TOP_LEVEL_VCF_TAG+"CT_"+CT_INFIX+"AltCt"
        var nMahCt_TAG : String = TOP_LEVEL_VCF_TAG+"CT_"+CT_INFIX+"MultiHetCt"
        var nMisCt_TAG : String = TOP_LEVEL_VCF_TAG+"CT_"+CT_INFIX+"MisCt"
        var nOthCt_TAG : String = TOP_LEVEL_VCF_TAG+"CT_"+CT_INFIX+"OtherCt"
        var nNMissCt_TAG : String = TOP_LEVEL_VCF_TAG+"CT_"+CT_INFIX+"NonMissCt"
        var nHomRefCt_TAG : String = TOP_LEVEL_VCF_TAG+"CT_"+CT_INFIX+"HomRefCt"
        
        var nNonrefCt_TAG : String = TOP_LEVEL_VCF_TAG+"CT_"+CT_INFIX+"NonrefCt"
        var nHomFrq_TAG : String = TOP_LEVEL_VCF_TAG+"CT_"+CT_INFIX+"HomFreq"
        var nHetFrq_TAG : String = TOP_LEVEL_VCF_TAG+"CT_"+CT_INFIX+"HetFreq"
        var nAltFrq_TAG : String = TOP_LEVEL_VCF_TAG+"CT_"+CT_INFIX+"AltFreq"
        var nMahFrq_TAG : String = TOP_LEVEL_VCF_TAG+"CT_"+CT_INFIX+"MultiHetFreq"
        var nNMissFrq_TAG : String = TOP_LEVEL_VCF_TAG+"CT_"+CT_INFIX+"NonMissFreq"
        var nHomRefFrq_TAG : String = TOP_LEVEL_VCF_TAG+"CT_"+CT_INFIX+"HomRefFreq"
        
        var nMisFrq_TAG : String = TOP_LEVEL_VCF_TAG+"CT_"+CT_INFIX+"MisFreq"
        var nOthFrq_TAG : String = TOP_LEVEL_VCF_TAG+"CT_"+CT_INFIX+"OtherFreq"
        var nNonrefFrq_TAG : String = TOP_LEVEL_VCF_TAG+"CT_"+CT_INFIX+"NonrefFreq"
        
        var nFiltCt_TAG : String = TOP_LEVEL_VCF_TAG+"CT_"+CT_INFIX+"FiltCt"
        var nFiltFrq_TAG : String = TOP_LEVEL_VCF_TAG+"CT_"+CT_INFIX+"FiltFreq"
        var nPremisCt_TAG : String = TOP_LEVEL_VCF_TAG+"CT_"+CT_INFIX+"PrefiltMisCt"
        var nPremisFrq_TAG : String = TOP_LEVEL_VCF_TAG+"CT_"+CT_INFIX+"PrefiltMisFreq"
        var nPreNonrefCt_TAG : String = TOP_LEVEL_VCF_TAG+"CT_"+CT_INFIX+"PrefiltNonRefCt"
        var nPreNonrefFrq_TAG : String = TOP_LEVEL_VCF_TAG+"CT_"+CT_INFIX+"PrefiltNonRefFreq"
        
  }

  val DefaultVCFAnnoCodes = VCFAnnoCodes();
  val veeOneSeven_vcfAnnoCodes = VCFAnnoCodes(
        vMutG_TAG  = TOP_LEVEL_VCF_TAG+"varG",
        vTypeShort_TAG  = TOP_LEVEL_VCF_TAG+"ShortVARTYPE",
        vMutPShort_TAG  = TOP_LEVEL_VCF_TAG+"ShortVarPredP",
        vMutLVL_TAG  = TOP_LEVEL_VCF_TAG + "varLVL",
        vMutINFO_TAG  = TOP_LEVEL_VCF_TAG + "varRAWDATA",
        isSplitMulti_TAG  = TOP_LEVEL_VCF_TAG+"isSplitMult",
        splitIdx_TAG      = TOP_LEVEL_VCF_TAG+"splitIdx",
        numSplit_TAG      = TOP_LEVEL_VCF_TAG+"numSplit",
        splitAlle_TAG     = TOP_LEVEL_VCF_TAG+"fullAlleList",
        splitAlleWARN_TAG = TOP_LEVEL_VCF_TAG+"splitAlleWarn",
        
        txList_TAG  = TOP_LEVEL_VCF_TAG+ "TXLIST",
        vType_TAG  = TOP_LEVEL_VCF_TAG+"VARTYPE",
        vMutR_TAG  = TOP_LEVEL_VCF_TAG+"varR",
        vMutC_TAG  = TOP_LEVEL_VCF_TAG+"varC",
        vMutP_TAG  = TOP_LEVEL_VCF_TAG+"varPredP"
      );
  
  val UNAUTOMATED_ACMG_PARAMS = Seq[(String,Int,String)](
        ("PS",2 , TOP_LEVEL_VCF_TAG+"ACMG_PS2"),
        ("PS",3 , TOP_LEVEL_VCF_TAG+"ACMG_PS3"),
        ("PS",4 , TOP_LEVEL_VCF_TAG+"ACMG_PS4"),
        ("PM",3 , TOP_LEVEL_VCF_TAG+"ACMG_PM3"),
        ("PM",6, TOP_LEVEL_VCF_TAG+"ACMG_PM6"),
        ("PP",1 , TOP_LEVEL_VCF_TAG+"ACMG_PP1"),
        ("PP",4 , TOP_LEVEL_VCF_TAG+"ACMG_PP4"),
        ("PP",5 , TOP_LEVEL_VCF_TAG+"ACMG_PP5"),
        ("BP",2 , TOP_LEVEL_VCF_TAG+"ACMG_BP2"),
        ("BP",5 , TOP_LEVEL_VCF_TAG+"ACMG_BP5"),
        ("BP",6 , TOP_LEVEL_VCF_TAG+"ACMG_BP6"),
        ("BS",1 , TOP_LEVEL_VCF_TAG+"ACMG_BS1"),
        ("BS",2 , TOP_LEVEL_VCF_TAG+"ACMG_BS2"),
        ("BS",3 , TOP_LEVEL_VCF_TAG+"ACMG_BS3"),
        ("BS",4 , TOP_LEVEL_VCF_TAG+"ACMG_BS4")
      )
  
    //  val isRepetitive = locusIsRepetitive(v.getContig(),v.getStart());
    //val isConserved  = locusIsConserved(v.getContig(),v.getStart());
    //val isHotspot    = locusIsHotspot(v.getContig(),v.getStart());
    
  def getVcfFilesIter(infile : String, chromList : Option[List[String]],infileList : Boolean, vcfCodes : VCFAnnoCodes = VCFAnnoCodes(), verbose : Boolean = true) : (Iterator[VariantContext], VCFHeader) = {
    if(infileList){
        val chromSet = chromList match {
          case Some(lst) => Some(lst.toSet);
          case None => None;
        }
        val (peekList,fileiter) = peekIterator(getLinesSmartUnzip(infile),1000);
        val denominator = if(peekList.length == 1000) "?" else peekList.length.toString;
        val readerIter = fileiter.map{ file => {
          val vcfReader = new VCFFileReader(new File(file),false);
          (file,vcfReader)
        }}.buffered
        val vcfHeader = readerIter.head._2.getFileHeader();
        
        val iteriter = readerIter.zipWithIndex.map{case ((file,vcfReader),idx) => {
            val vcfIterator : Iterator[VariantContext] = vcfReader.asScala.iterator;
           // val finalIter = chromFilterVcfIterator(vcfIterator,chromSet=chromSet);
            addIteratorCloseAction[VariantContext](iter = vcfIterator, closeAction = (() => {reportln("finished reading file: "+file + "("+getDateAndTimeString+")" + "("+(idx+1)+"/"+denominator+")","note")}))
        }}
        (chromFilterVcfIterator(flattenIterators(iteriter),chromSet=chromSet), vcfHeader);
    } else {
      getVcfIterator(infile, chromList = chromList, vcfCodes = vcfCodes)
    }
  }
  
  trait VCFWalker{
    def walkVCF(vcIter : Iterator[VariantContext], vcfHeader : VCFHeader, verbose : Boolean = true) : (Iterator[VariantContext],VCFHeader);
    
    def walkVCFFile(infile : String, outfile : String, chromList : Option[List[String]], vcfCodes : VCFAnnoCodes = VCFAnnoCodes(), verbose : Boolean = true){
      val (vcIter,vcfHeader) = internalUtils.VcfTool.getVcfIterator(infile, 
                                       chromList = chromList,
                                       vcfCodes = vcfCodes);
      val (vcIter2, newHeader) = this.walkVCF(vcIter = vcIter, vcfHeader = vcfHeader)
    
      val vcfWriter = internalUtils.VcfTool.getVcfWriter(outfile, header = newHeader);
    
      vcIter2.foreach(vc => {
        vcfWriter.add(vc)
      })
      vcfWriter.close();
    }
    
    def walkVCFFiles(infile : String, outfile : String, chromList : Option[List[String]], infileList : Boolean, vcfCodes : VCFAnnoCodes = VCFAnnoCodes(), verbose : Boolean = true) {
      if(infileList){
        val (vcIter,vcfHeader) = getVcfFilesIter(infile = infile, chromList = chromList, infileList = infileList, vcfCodes = vcfCodes);
        val (vcIter2, newHeader) = this.walkVCF(vcIter = vcIter, vcfHeader = vcfHeader)
        val vcfWriter = internalUtils.VcfTool.getVcfWriter(outfile, header = newHeader);
        vcIter2.foreach(vc => {
          vcfWriter.add(vc)
        })
        vcfWriter.close();
      } else {
        walkVCFFile(infile=infile,outfile=outfile,chromList=chromList,vcfCodes=vcfCodes,verbose=verbose);
      }
    }
    
    def chain(walker2 : VCFWalker, flag : Boolean = true) : VCFWalker = {
      if(flag){
        val parent : VCFWalker = this;
        return new VCFWalker {
          def walkVCF(vcIter : Iterator[VariantContext], vcfHeader : VCFHeader, verbose : Boolean = true) : (Iterator[VariantContext],VCFHeader) = {
            val (iter2,header2) = parent.walkVCF(vcIter = vcIter, vcfHeader = vcfHeader);
            walker2.walkVCF(vcIter = iter2, vcfHeader = header2);
          }
        }
      } else {
        return this;
      }
    }
    
  }
  
    def getAltAllelesInOrder(vc : VariantContext) : Seq[Allele] = {
      Range(0,vc.getNAlleles()-1).map(i => {
        vc.getAlternateAllele(i);
      })
    }
    def getAllelesInOrder(vc : VariantContext) : Seq[Allele] = {
      vc.getReference() +: getAltAllelesInOrder(vc);
    }
  
  //htsjdk.variant.variantcontext.writer.VariantContextWriter
  
  def splitHeaderLinesByType(headerLines : Seq[VCFHeaderLine]) : (Seq[VCFHeaderLine],Seq[VCFFormatHeaderLine],Seq[VCFInfoHeaderLine]) = {
    val newFormatHeaderLines : (Seq[VCFHeaderLine],Seq[VCFFormatHeaderLine],Seq[VCFInfoHeaderLine]) = headerLines.foldLeft((Seq[VCFHeaderLine](),Seq[VCFFormatHeaderLine](),Seq[VCFInfoHeaderLine]())){ case ((otherLines,fmtLines,infoLines),curr) => {
      curr match {
        case hl : VCFFormatHeaderLine => {
          (otherLines,fmtLines :+ hl, infoLines)
        }
        case hl : VCFInfoHeaderLine => {
          (otherLines,fmtLines,infoLines :+ hl)
        }
        case hl : VCFHeaderLine => {
          (otherLines :+ hl, fmtLines,infoLines)
        }
      }
    }}
    newFormatHeaderLines;
  }
    
  def addHeaderLines(vcfHeader : VCFHeader, headerLines : Seq[VCFHeaderLine]) : VCFHeader = {
    val (newOtherLines,newFmtLines,newInfoLines) = splitHeaderLinesByType(headerLines);
    val (oldOtherLines,oldFmtLines,oldInfoLines) = splitHeaderLinesByType(vcfHeader.getMetaDataInInputOrder().asScala.toList);
    
    val oldHeaderLines : List[VCFHeaderLine] = vcfHeader.getMetaDataInInputOrder().asScala.toList;
    
    val newFmtSet = newFmtLines.map(hl => hl.getID).toSet;
    val newInfoSet = newInfoLines.map(hl => hl.getID).toSet;
    
    val filtFmtLines = oldFmtLines.filter(hl => ! newFmtSet.contains(hl.getID()));
    val filtInfoLines = oldInfoLines.filter(hl => ! newInfoSet.contains(hl.getID()));
    
    val newHeaderLines : Seq[VCFHeaderLine] = (newOtherLines ++ oldOtherLines ++ filtFmtLines ++ newFmtLines ++ filtInfoLines ++ newInfoLines);
    
    return new VCFHeader(newHeaderLines.toSet.asJava,
                         vcfHeader.getSampleNamesInOrder().asInstanceOf[java.util.List[String]]);
  }
  def replaceHeaderLines(vcfHeader : VCFHeader, headerLines : Seq[VCFHeaderLine]) : VCFHeader = {
    val newHeaderLines : List[VCFHeaderLine] = headerLines.toList;

    return new VCFHeader(newHeaderLines.toSet.asJava,
                         vcfHeader.getSampleNamesInOrder().asInstanceOf[java.util.List[String]]);
  }
  
  
  def getVcfWriter(outfile : String, header : VCFHeader) : htsjdk.variant.variantcontext.writer.VariantContextWriter = {
    val vcfb = new htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder();
    
    //vcfb.setReferenceDictionary(vcfHeader.getSequenceDictionary());
    vcfb.unsetOption(htsjdk.variant.variantcontext.writer.Options.INDEX_ON_THE_FLY);
    vcfb.setOutputFile(outfile);
    
    if(outfile.takeRight(3) == ".gz"){
      vcfb.setOutputFileType(htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder.OutputType.BLOCK_COMPRESSED_VCF);
    } else {
      vcfb.setOutputFileType(htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder.OutputType.VCF);
    }
        
    val vcfWriter : htsjdk.variant.variantcontext.writer.VariantContextWriter = vcfb.build();
    
    vcfWriter.writeHeader(header);
    
    return vcfWriter;
  }
  
  def getVcfIterator(   infile : String, 
                        chromList : Option[List[String]],
                        vcfCodes : VCFAnnoCodes = VCFAnnoCodes(),
                        progressVerbosity : (Int,Int,Int) = (10000,50000,100000)
                     ) : (Iterator[VariantContext], VCFHeader) = {
      val chromSet = chromList match {
        case Some(lst) => Some(lst.toSet);
        case None => None;
      }
      reportln("Starting VCF read...","progress");
      
      val vcfReader = new VCFFileReader(new File(infile),false);
      val vcfHeader = vcfReader.getFileHeader();
      val vcfIterator : Iterator[VariantContext] = vcfReader.asScala.iterator; //chromSet match {
      val (a,b,c) = progressVerbosity;
      
      val finalIterator = chromFilterVcfIterator(vcfIterator,chromSet=chromSet,vcfCodes=vcfCodes,progressVerbosity=progressVerbosity);
      return (finalIterator,vcfHeader);
  }
  
  def chromFilterVcfIterator(vcfIterator : Iterator[VariantContext],
                        chromSet : Option[Set[String]],
                        vcfCodes : VCFAnnoCodes = VCFAnnoCodes(),
                        progressVerbosity : (Int,Int,Int) = (10000,50000,100000)) : Iterator[VariantContext] = {
      val (a,b,c) = progressVerbosity;
      
      val finalIterator = chromSet match {
        case Some(cs) => {
          if(cs.size == 1){
            val chr = cs.head;
            
            val preProgRep = internalUtils.stdUtils.AdvancedIteratorProgressReporter_ThreeLevelAuto[VariantContext](
                elementTitle  = "skipped lines", lineSec = 60,
                reportFunction  = ((vc : VariantContext, i : Int) => " " + vc.getContig())
            );
            val postProgRep = internalUtils.stdUtils.AdvancedIteratorProgressReporter_ThreeLevelAuto[VariantContext](
                elementTitle  = "processed lines", lineSec = 60,
                reportFunction  = ((vc : VariantContext, i : Int) => " " + vc.getContig())
            );
            
            var i = 1;
            var j = 1;
            vcfIterator.dropWhile{ v => {
              preProgRep.reportProgress(i,v);
              i = i + 1;
              v.getContig() != chr;
            }}.takeWhile{ v => {
              postProgRep.reportProgress(j,v);
              j = j + 1;
              v.getContig() == chr;
            }}
          } else {
            internalUtils.stdUtils.wrapIteratorWithAdvancedProgressReporter[VariantContext](vcfIterator.filter((p : VariantContext) => cs.contains(p.getContig())),
                internalUtils.stdUtils.AdvancedIteratorProgressReporter_ThreeLevelAuto[VariantContext](elementTitle = "lines",lineSec = 60, reportFunction = ((a : VariantContext,i : Int) => a.getContig()))).filter(v =>  cs.contains(v.getContig())); 
          }

        }
        case None => {
          internalUtils.stdUtils.wrapIteratorWithAdvancedProgressReporter[VariantContext](vcfIterator,
              internalUtils.stdUtils.AdvancedIteratorProgressReporter_ThreeLevelAuto[VariantContext](elementTitle = "lines", lineSec = 60))
        }
      }
      
      return finalIterator;
  }

  
  
/*
   abstract class VcfMetaLine {
     def key : String;
     def value : String;
     override def toString() : String = "##"+key+"="+value;
   }
   case class VcfAnnoLine(k : String, v : String) extends VcfMetaLine {
     def key : String = k;
     def value : String = v;
   }
   case class VcfInfoLine(ID : String, Number : String, Type : String, Description : String, Source:Option[String] = None, Version:Option[String] = None) extends VcfMetaLine {
     def key : String = "INFO";
     def value : String = "<" + 
                          "ID="+ ID+""+
                          ",Number="+Number+""+
                          ",Type="+Type+""+
                          ",Description="+Description+
                          (if(Source.isEmpty) "" else ",Source="+Source.get)+
                          (if(Version.isEmpty) "" else ",Version="+Version.get)+
                          ">";
   }
   case class VcfFilterLine(ID : String, Description : String) extends VcfMetaLine {
     def key : String = "FILTER";
     def value : String = "<"+
                          "ID="+ ID+""+
                          ",Description="+Description+
                          ">";
   }
   case class VcfFormatLine(ID : String, Number : String, Type : String, Description : String) extends VcfMetaLine {
     def key : String = "FORMAT";
     def value : String = "<" + 
                          "ID="+ ID+""+
                          ",Number="+Number+""+
                          ",Type="+Type+""+
                          ",Description="+Description+
                          ">";
   }
   
   private def parseVcfMetadataValue(s : String) : Seq[(String,String)] = {
     if(s.length < 2 || s.head != '<' || s.last != '>'){
       error("FATAL ERROR: Malformed metadata line in VCF file: value is too short or isn't bound by angle-brackets (errCode VcfTool:parseVcfMetadataValue:65)!");
     }
     val valCells = internalUtils.stdUtils.parseTokens(s.init.tail,',');
     val valPairs = valCells.map((v) => {
       val pair = internalUtils.stdUtils.parseTokens(v,'=');
       if(pair.length != 2) error("FATAL ERROR: Malformed metadata line in VCF file (errCode VcfTool:76)!");
       (pair(0),pair(1));
     });
     return valPairs;
   }
   
   def readVcfMetadata(lines : Iterable[String], sampleLine : String) : VcfMetadata = {
     val metaLines : Seq[VcfMetaLine] = lines.map((s : String) => readVcfMetaLine(s)).toSeq;
     val (info : Seq[VcfInfoLine], filter : Seq[VcfFilterLine], format : Seq[VcfFormatLine], anno : Seq[VcfAnnoLine]) = {
       metaLines.foldLeft((Seq[VcfInfoLine](),Seq[VcfFilterLine](),Seq[VcfFormatLine](),Seq[VcfAnnoLine]()))((soFar,curr) => {
         curr match {
           case x : VcfInfoLine => (soFar._1 :+ x, soFar._2, soFar._3, soFar._4);
           case x : VcfFilterLine => (soFar._1, soFar._2 :+ x, soFar._3, soFar._4);
           case x : VcfFormatLine => (soFar._1 , soFar._2, soFar._3 :+ x, soFar._4);
           case x : VcfAnnoLine => (soFar._1, soFar._2, soFar._3, soFar._4 :+ x);
         }
       }) 
     }
     val sampleCells = sampleLine.split("\t");
     if(sampleCells.length < 9) error("FATAL ERROR: Malformed Vcf Header line. Less than 9 columns! (errCode VcfTool:readVcfMetadata:72)\n offending line:\""+sampleLine+"\"");
     val sampleID = sampleCells.slice(9,sampleCells.length);
     
     return VcfMetadata(info, filter, format, anno, sampleID);
   }
   
   def readVcfMetaLine(line : String) : VcfMetaLine = {
     if(line.substring(0,2) != "##"){
       error("FATAL ERROR: Impossible state! readVcfMetaLine has been given a line that does not start with \"##\"! (errcode VcfTool:readVcfMetaLine:78)");
     }
     val cells = line.substring(2).split("=",2);
     if(cells(0) == "INFO"){
       readVcfInfoLine(cells(1));
     } else if(cells(0) == "FILTER"){
       readVcfFilterLine(cells(1));
     } else if(cells(0) == "FORMAT"){
       readVcfFormatLine(cells(1));
     } else {
       VcfAnnoLine(cells(0), cells(1));
     }
   }
   private def readVcfInfoLine(v : String) : VcfInfoLine = {
       val valMap = parseVcfMetadataValue(v).toMap;
       if(! valMap.contains("ID")) error("FATAL ERROR: Malformed INFO metadata line in VCF file: no ID key! (errCode VcfTool:readVcfInfoLine:84)");
       if(! valMap.contains("Number")) error("FATAL ERROR: Malformed INFO metadata line in VCF file: no Number key! (errCode VcfTool:readVcfInfoLine:85)");
       if(! valMap.contains("Type")) error("FATAL ERROR: Malformed INFO metadata line in VCF file: no Type key! (errCode VcfTool:readVcfInfoLine:86)");
       if(! valMap.contains("Description")) error("FATAL ERROR: Malformed INFO metadata line in VCF file: no Description key! (errCode VcfTool:readVcfInfoLine:87)");
       return VcfInfoLine(valMap("ID"), valMap("Number"), valMap("Type"), valMap("Description"), valMap.get("Source"), valMap.get("Version"));
   }
   private def readVcfFilterLine(v : String) : VcfFilterLine = {
       val valMap = parseVcfMetadataValue(v).toMap;
       if(! valMap.contains("ID")) error("FATAL ERROR: Malformed FILTER metadata line in VCF file: no ID key! (errCode VcfTool:readVcfFilterLine:84)");
       if(! valMap.contains("Description")) error("FATAL ERROR: Malformed FILTER metadata line in VCF file: no Description key! (errCode VcfTool:readVcfFilterLine:87)");
       return VcfFilterLine(valMap("ID"), valMap("Description"));
   }
   private def readVcfFormatLine(v : String) : VcfFormatLine = {
       val valMap = parseVcfMetadataValue(v).toMap;
       if(! valMap.contains("ID")) error("FATAL ERROR: Malformed FORMAT metadata line in VCF file: no ID key! (errCode VcfTool:readVcfFormatLine:84)");
       if(! valMap.contains("Number")) error("FATAL ERROR: Malformed FORMAT metadata line in VCF file: no Number key! (errCode VcfTool:readVcfFormatLine:85)");
       if(! valMap.contains("Type")) error("FATAL ERROR: Malformed FORMAT metadata line in VCF file: no Type key! (errCode VcfTool:readVcfFormatLine:86)");
       if(! valMap.contains("Description")) error("FATAL ERROR: Malformed FORMAT metadata line in VCF file: no Description key! (errCode VcfTool:readVcfFormatLine:87)");
       return VcfFormatLine(valMap("ID"), valMap("Number"), valMap("Type"), valMap("Description"));
   }
   
   case class VcfMetadata(info : Seq[VcfInfoLine],filter : Seq[VcfFilterLine], format : Seq[VcfFormatLine], anno : Seq[VcfAnnoLine], sampleID : Seq[String]) {
     lazy val metaLines   : Seq[VcfMetaLine] = info ++ filter ++ format ++ anno;

     lazy val infoMap : Map[String,VcfInfoLine] = info.map((v) =>{
       (v.ID,v);
     }).toMap;
     lazy val filterMap : Map[String,VcfFilterLine] = filter.map((v) =>{
       (v.ID,v);
     }).toMap;
     lazy val formatMap : Map[String,VcfFormatLine] = format.map((v) =>{
       (v.ID,v);
     }).toMap;
     
   }
   
   abstract class VcfLine {
     def CHROM : String;
     def POS : Int;
     def ID : String;
     def REF : String;
     def ALT: String;
     def QUAL : Double;
     def FILTER: String;
     def INFO: String;
     def FORMAT : String;
     def GENOTYPES: Seq[String];
     def metadata : VcfMetadata;
     
     override def toString() : String = CHROM +"\t"+POS+"\t"+ID+"\t"+REF+"\t"+ALT+"\t"+QUAL+"\t"+FILTER+"\t"+INFO+"\t"+FORMAT+"\t"+GENOTYPES.mkString("\t");
     
     def fmt : Seq[String];

     lazy val idSeq : Seq[String] = ID.split(";");
     lazy val altSeq : Seq[String] = ALT.split(",");
     
     lazy val fmtMeta : Seq[VcfFormatLine] = fmt.map((f : String) =>{
       metadata.formatMap.get(f) match {
         case Some(x) => x;
         case None => {
           error("FATAL VCF PARSING ERROR: FORMAT ID not found!\n offending line:\""+toString()+"\"");
           null;
         }
       }
     })
     lazy val fmtInfo : Seq[(String,String)] = fmtMeta.map((m : VcfFormatLine)=> (m.Number,m.Type));
   }
   
   case class InputVcfLine(line : String, meta : VcfMetadata) extends VcfLine {
     def metadata = meta;
     lazy val cells = {
       val c = line.split("\t");
       if(c.length < 9) error("FATAL ERROR: Vcf Line has fewer than 9 columns:\n  Offending line: \""+line+"\"");
       c;
     }
     def CHROM : String = cells(0);
     lazy val pos : Int = internalUtils.stdUtils.string2int(cells(1));
     def POS : Int = pos;
     def ID : String = cells(2);
     def REF : String = cells(3);
     def ALT : String = cells(4);
     lazy val qual : Double = internalUtils.stdUtils.string2double(cells(5));
     def QUAL : Double = qual;
     def FILTER : String = cells(6);
     def INFO : String = cells(7);
     def FORMAT : String = cells(8);
     def GENOTYPES : Seq[String] = cells.slice(9,cells.length);
     
     //def passFilter : Boolean = 
     
     lazy val FMT : Seq[String] = FORMAT.split(":");
     def fmt : Seq[String] = FMT;
     
     lazy val genoTableBySample : Seq[Seq[String]] = GENOTYPES.map((g : String) => {
       val out = g.split(":").toSeq;
       if(out.length != fmt.length) error("FATAL ERROR: Vcf genotype has the wrong number of elements.\n offending line: \""+line+"\"");
       out;
     });
     lazy val genoTable : Seq[Seq[String]] = internalUtils.stdUtils.transposeMatrix(genoTableBySample);
   }
   //case class OutputVcfLine(chrom : String, pos : Int, id : String, ref : String, alt : String, qual : Double, filter : String, Info : String, genotypes : Seq[String]){
     
   //}
   
   /*
   abstract class VcfMetadata {
     def getMetaLines   : Seq[VcfMetaLine];
     def getInfoLines   : Seq[VcfInfoLine];
     def getFilterLines : Seq[VcfFilterLine];
     def getFormatLines : Seq[VcfFormatLine];
     def getInfoMap : Map[String,VcfInfoLine] = getInfoLines.map((v) =>{
       (v.ID,v);
     }).toMap;
     def getFormatMap : Map[String,VcfFormatLine] = getFormatLines.map((v) =>{
       (v.ID,v);
     }).toMap;
     def getFilterMap : Map[String,VcfFilterLine] = getFilterLines.map((v) =>{
       (v.ID,v);
     }).toMap;
   }
   */
   
   abstract class VcfNumberField {
     def isInteger : Boolean;
     def isSpecial : Boolean;
     def isKnown : Boolean;
   }
   case class VcfNumberFieldDot() extends VcfNumberField {
     def isInteger : Boolean = false;
     def isSpecial : Boolean = true;
     def isKnown : Boolean = false;
   }
   abstract class VcfNumberFieldKnown extends VcfNumberField {
     def isKnown : Boolean = true;
     def getFieldCount(altAlleleCt : Int, genotypeCt : Int) : Int;
   }
   case class VcfNumberFieldInt(value : Int) extends VcfNumberFieldKnown {
     def isInteger : Boolean = true;
     def isSpecial : Boolean = false;
     def getFieldCount(altAlleleCt : Int, genotypeCt : Int) : Int = value;
   }
   case class VcfNumberFieldA() extends VcfNumberFieldKnown {
     def isInteger : Boolean = false;
     def isSpecial : Boolean = true;
     def getFieldCount(altAlleleCt : Int, genotypeCt : Int) : Int = altAlleleCt;
   }
   case class VcfNumberFieldR() extends VcfNumberFieldKnown {
     def isInteger : Boolean = false;
     def isSpecial : Boolean = true;
     def getFieldCount(altAlleleCt : Int, genotypeCt : Int) : Int = altAlleleCt + 1;
   }
   case class VcfNumberFieldG() extends VcfNumberFieldKnown {
     def isInteger : Boolean = false;
     def isSpecial : Boolean = true;
     def getFieldCount(altAlleleCt : Int, genotypeCt : Int) : Int = genotypeCt;
   }
   
   abstract class VcfVal {
     def isInt : Boolean = false;
     def isFloat : Boolean = false;
     def isChar : Boolean = false;
     def isString : Boolean = false;
   }
   case class VcfInt(v : Int) extends VcfVal {
     override def toString() = v.toString();
     override def isInt : Boolean = true;
   }
   case class VcfFloat(v : Double) extends VcfVal {
     override def toString() = v.toString();
     override def isFloat : Boolean = true;
   }
   case class VcfChar(v : Char) extends VcfVal {
     override def toString() = v.toString();
     override def isChar : Boolean = true;
   }
   case class VcfString(v : String) extends VcfVal {
     override def toString() = v.toString();
     override def isString : Boolean = true;
   }
   
   def readVcfVal(v : String, fmt : String) : VcfVal = {
     if(fmt == "Integer"){
       return VcfInt(internalUtils.stdUtils.string2int(v));
     } else if(fmt == "Float"){
       return VcfFloat(internalUtils.stdUtils.string2double(v));
     } else if(fmt == "Char"){
       if(v.length > 1) error("FATAL ERROR: Malformed VCF. Found 'char' formatted field with length > 1!");
       return VcfChar(v.charAt(0));
     } else if(fmt == "String"){
       return VcfString(v);
     } else {
       error("FATAL ERROR: Malformed VCF. Unrecognized format: \""+fmt+"\"");
       return null;
     }
   } 
   
   
   def getVcfReader(lines : Iterator[String]) : (VcfMetadata, Iterator[InputVcfLine]) = {
     val (metaLines, bodyLines) = internalUtils.stdUtils.splitIterator(lines, (ln : String) => {
       ln.startsWith("##");
     });
     if(! bodyLines.hasNext) error("FATAL ERROR: Vcf File does not have header or body lines.");
     val headerLine = bodyLines.next;
     if(! headerLine.startsWith("#")) error("FATAL ERROR: Vcf File header line not present or does not start with \"#\"");
     if(! bodyLines.hasNext) error("FATAL ERROR: Vcf File does not have body lines.");
     
     val meta : VcfMetadata = readVcfMetadata(metaLines,headerLine);
     
     val vcfLines : Iterator[InputVcfLine] = bodyLines.map( (line : String) => {
       InputVcfLine(line,meta);
     });
     
     return ((meta,vcfLines));
   }
   
  */
  

  
  object SVcfLine {
    

    def memMergeVcfFiles(infiles : List[String], sampids : List[String],  
                         sumInfoFields : Seq[String] = Seq[String](),     //implemented
                         splitInfoFields : Seq[String] = Seq[String](),   //not implemented
                         firstInfoFields : Seq[String] = Seq[String](),   //implemented
                         gtInfoFields : Seq[String] = Seq[String](),      //implemented
                         genomeFA : String, latWindow : Int = 200, idxTagString : String = "IDX.",
                         leftAlignAndTrim : Boolean = true, splitMultiAllelics : Boolean = true) : (SVcfHeader, Iterator[SVcfVariantLine]) = {
      val keepInfoFields = sumInfoFields ++ splitInfoFields ++ firstInfoFields ++ gtInfoFields

      //walkVCFFile(infile :String, outfile : String, chromList : Option[List[String]], numLinesRead : Option[Int] = None, dropGenotypes : Boolean = false)
      
      val BUFprewalker : SVcfWalker = chainSVcfWalkers(Seq(
                internalTests.SVcfWalkerUtils.FilterTags(keepInfoTags = Some(keepInfoFields.toList)),
                (new internalTests.SVcfWalkerUtils.AddVariantIdx(tag = "",idxPrefix = Some(""))),
                internalTests.SVcfWalkerUtils.SSplitMultiAllelics(),
                internalUtils.GatkPublicCopy.LeftAlignAndTrimWalker(genomeFa = genomeFA,windowSize = latWindow, useGatkLibCall = false)
      ))
      val (bufiter,rawHeader) = getSVcfIterator(infiles.head,chromList =None, numLinesRead=None);
      val header = BUFprewalker.walkVCF(bufiter,rawHeader)._2;
      
      val outHeader = header.copyHeader
      outHeader.titleLine = new SVcfTitleLine(sampids)
      
      val fmtseq = outHeader.formatLines.map{ ff => ff.ID }.sortBy{ f => if( f == "GT"){ 0 } else { 1 } }
      
      reportln("keepInfoFields["+keepInfoFields.length+"]:"+keepInfoFields.mkString(","),"note");
      
      gtInfoFields.foreach{ gif => {
        val infoline = header.infoLines.find( ff => ff.ID == gif )
        infoline.foreach( oldline => {
          outHeader.addFormatLine(
            new  SVcfCompoundHeaderLine("FORMAT",ID=oldline.ID,Number = oldline.Number, Type = oldline.Type,desc = "(copied from the single-sample INFO field) "+oldline.desc)
          )
        })
      }}
      
      reportln("STARTING MAIN FILE READ.","note");
      
      (outHeader,{
        
        val lineMap : scala.collection.mutable.Map[(String,Int,String,String),Vector[(String,SVcfVariantLine,Int)]] = 
                ( new scala.collection.mutable.AnyRefMap[(String,Int,String,String),Vector[(String,SVcfVariantLine,Int)]]).withDefault( x => Vector() )
                
        /*
               val variantLines = if(withProgress){
                         internalUtils.stdUtils.wrapIteratorWithAdvancedProgressReporter[SVcfInputVariantLine](
                           //rawVariantLines.map(line => SVcfInputVariantLine(line,header)),
                           bufLines.map(line => SVcfInputVariantLine(line)), 
                           internalUtils.stdUtils.AdvancedIteratorProgressReporter_ThreeLevelAuto[SVcfInputVariantLine](
                                elementTitle = "lines", lineSec = 60,
                                reportFunction  = ((vc : SVcfInputVariantLine, i : Int) => " " + vc.chrom +" "+ internalUtils.stdUtils.MemoryUtil.memInfo )
                           )
                         )
         */
        
        internalUtils.stdUtils.wrapIteratorWithAdvancedProgressReporter[String](infiles.iterator,
                                                             internalUtils.stdUtils.AdvancedIteratorProgressReporter_ThreeLevelAuto[String](
                                                                   elementTitle = "files", lineSec = 60,
                                                                   reportFunction  = ((vc : String, i : Int) => " " + sampids.lift(i).getOrElse(".") +" "+ internalUtils.stdUtils.MemoryUtil.memInfo )
        )).zipWithIndex.foreach{ case (ff,sidx) => {
        //infiles.zipWithIndex.iterator.foreach{ case (ff, sidx) => {

          val prewalker : SVcfWalker = chainSVcfWalkers(Seq(
                internalTests.SVcfWalkerUtils.FilterTags(keepInfoTags = Some(keepInfoFields.toList)),
                (new internalTests.SVcfWalkerUtils.AddVariantIdx(tag = "varIDX",idxPrefix = Some(sampids(sidx)+"."))),
                internalTests.SVcfWalkerUtils.SSplitMultiAllelics(),
                internalUtils.GatkPublicCopy.LeftAlignAndTrimWalker(genomeFa = genomeFA,windowSize = latWindow, useGatkLibCall = false)
          ))
          val iter = getLinesSmartUnzip(ff).buffered
          skipWhile(iter)(line => line.startsWith("#"));
          val lines =  prewalker.walkVCF( iter.map(line => SVcfInputVariantLine(line)),rawHeader)._1; 
          //val (lines,h) = prewalker.walkVCF(getSVcfIterator(infiles.head,chromList =None, numLinesRead=None)._1, header)
          
          /*
                 val allRawHeaderLines = extractWhile(bufLines)(line => line.startsWith("#"));
      val header = readVcfHeader(allRawHeaderLines);
      
      val variantLines = if(withProgress){
                         internalUtils.stdUtils.wrapIteratorWithAdvancedProgressReporter[SVcfInputVariantLine](
                           //rawVariantLines.map(line => SVcfInputVariantLine(line,header)),
                           bufLines.map(line => SVcfInputVariantLine(line)), 
                           internalUtils.stdUtils.AdvancedIteratorProgressReporter_ThreeLevelAuto[SVcfInputVariantLine](
                                elementTitle = "lines", lineSec = 60,
                                reportFunction  = ((vc : SVcfInputVariantLine, i : Int) => " " + vc.chrom +" "+ internalUtils.stdUtils.MemoryUtil.memInfo )
                           )
                         )
           */
          
          //val (vheader,lines) = prewalker.walkVCF( iter.map(line => SVcfInputVariantLine(line)),header)._1; 
          lines.foreach{ v => {
            val ix = (v.chrom,v.pos,v.ref,v.alt.head);
            //val sofar = lineMap(ix);
            //lineMap.update( ix, lineMap(ix) :+ ((sampids(sidx),sidx,v)) );
            //val lix = lineMap(ix)
            //lix.update( sampids(sidx), v );
            lineMap.update( ix, lineMap(ix) :+ ((sampids(sidx),v,sidx)) );
          }}
        }}
        
        val varList = lineMap.keys.toVector.sorted
        
        reportln("Generating merged iterator...","note");
        

        
        varList.iterator.map{ vid => {
          val vlines = lineMap(vid);
          val vs     = vlines.head._1;
          val vb = vlines.head._2.getOutputLine();
          
          if( vlines.exists{ case (ss,vv,sidx) => {
            vv.alt.length > 1
          }} && vb.alt.length == 1){
            vb.in_alt = vb.alt :+ "*";
          }
          
          /*
          vb.genotypes.fmt = fmtseq.filter{ fgt => {
            vlines.exists{ case (ss,vv,sidx) => {
              vv.genotypes.fmt.contains(fgt);
            }}
          }}
          if(vb.genotypes.fmt.length == 0 || vb.genotypes.fmt.head != "GT"){
            vb.genotypes.fmt = "GT" +: vb.genotypes.fmt
          }*/
          vb.genotypes.fmt = Seq("GT");
          vlines.foreach{ case (ss,vv,sidx) => {
            vv.genotypes.fmt.foreach{ gg => {
              if(! vb.genotypes.fmt.contains(gg)){
                vb.genotypes.fmt = vb.genotypes.fmt :+ gg
              }
            }}
          }}
          
          vb.genotypes.genotypeValues = vb.genotypes.fmt.toArray.map{ fgt => {
            val aa = Array.fill[String](sampids.length)(".");
            vlines.foreach{ case (ss,vv,sidx) => {
              val fidx = vv.genotypes.fmt.indexOf(fgt);
              if(fidx >= 0){
                aa(sidx) = vv.genotypes.genotypeValues(fidx).head;
              }
            }}
            aa;
          }}
          
          
          gtInfoFields.foreach{ infotag => {
            if( vlines.exists{ case (ss,vv,sidx) => vv.info.contains( infotag) } ){
               val aa = Array.fill[String](sampids.length)(".");
               vlines.foreach{ case (ss,vv,sidx) => {
                 vv.info.getOrElse(infotag,None).foreach{ dd => {
                   aa(sidx) = dd;
                 }}
               }}
               vb.genotypes.genotypeValues = vb.genotypes.genotypeValues :+ aa;
               vb.genotypes.fmt = vb.in_genotypes.fmt :+ infotag;
            }
          }}
          
          sumInfoFields.foreach{ infotag => {
            if( vlines.exists{ case (ss,vv,sidx) => vv.info.contains( infotag) } ){
              val isum = 
                if( header.infoLines.find( ffl => ffl.ID == infotag).get.Type == "Integer") {
                  vlines.map{ case (ss,vv,sidx) => {
                    vv.info.get(infotag).getOrElse(None).map{ string2int(_) }.getOrElse(0);
                  }}.sum + ""
                } else {
                  vlines.map{ case (ss,vv,sidx) => {
                    vv.info.get(infotag).getOrElse(None).map{ string2double(_) }.getOrElse(0.0);
                  }}.sum + ""
                }
              vb.addInfo(infotag,""+isum);
            }
          }}

          
          
          vb;
          //SVcfGenotypeSet(var fmt : Seq[String],
                    //         var genotypeValues : Array[Array[String]])
          //vb.in_genotypes.genotypeValues = 
        }}
      });
    }
    
    
    def readVcfs(lines : BufferedIterator[BufferedIterator[String]], withProgress : Boolean = true) : (SVcfHeader,Iterator[SVcfInputVariantLine]) = {
      //val (allRawHeaderLines,rawVariantLines0) = lines(0).span(line => line.startsWith("#"));
      //val header = readVcfHeader(allRawHeaderLines.toVector);
      //val rawVariantLines = rawVariantLines0 ++ lines.tail.flatten.filter{line => ! line.startsWith("#")};
      val headLines = lines.head;
      val header = readVcfHeader(extractWhile(headLines)( line => line.startsWith("#")));
      //val lineIterArray : List[BufferedIterator[String]] = (headLines :: lines.tail.map{_.buffered}.toList)
      //lineIterArray.tail.foreach{ iter => {
      //  skipWhile(iter)(line => line.startsWith("#"));
      //}}
      val rawVariantLines : Iterator[String] = flattenIterators[String]( lines.map{ lg => {
        skipWhile(lg)(line => line.startsWith("#"));
        lg;
      }})
      
      /*var currLines : BufferedIterator[String] = headLines;
      var remLines : List[Iterator[String]] = lines.tail.toList;
      val rawVariantLines : Iterator[String] = new Iterator[String]{
        def hasNext : Boolean = currLines.hasNext;
        def next : String = {
          val n = currLines.next;
          if(! currLines.hasNext){
            currLines = remLines.head.buffered;
            remLines = remLines.tail;
            skipWhile(currLines)( line => line.startsWith("#"));
          }
          n;
        }
      }*/
      
      
      val variantLines = if(withProgress){
                         internalUtils.stdUtils.wrapIteratorWithAdvancedProgressReporter[SVcfInputVariantLine](
                           //rawVariantLines.map(line => SVcfInputVariantLine(line,header)),
                           rawVariantLines.map(line => SVcfInputVariantLine(line)), 
                           internalUtils.stdUtils.AdvancedIteratorProgressReporter_ThreeLevelAuto[SVcfInputVariantLine](
                                elementTitle = "lines", lineSec = 60,
                                reportFunction  = ((vc : SVcfInputVariantLine, i : Int) => " " + vc.chrom +" "+ internalUtils.stdUtils.MemoryUtil.memInfo )
                           )
                         )
      } else {
        //rawVariantLines.map(line => SVcfInputVariantLine(line,header))
        rawVariantLines.map(line => SVcfInputVariantLine(line))
      }
      (header,variantLines);
    }

    def readTableToVcfAdv(lines : Iterator[String], withProgress : Boolean = true, hasIDcol : Boolean = true) : (SVcfHeader,Iterator[SVcfVariantLine]) = {
      if(hasIDcol){
        return(readTableToVcf(lines=lines,withProgress=withProgress))
      } else {
        return(readTableToVcfNoID(lines=lines,withProgress=withProgress))
      }
    }
    
    def readTableToVcf(lines : Iterator[String], withProgress : Boolean = true) : (SVcfHeader,Iterator[SVcfVariantLine]) = {
      val bufLines = lines.buffered;
      if(! bufLines.head.startsWith("#CHROM\tPOS\tID\tREF\tALT")){
        error("Error: table must begin with string: \"#CHROM\tPOS\tID\tREF\tALT\"");
      }
      val allRawHeaderLines = extractWhile(bufLines)(line => line.startsWith("#"));
      val header = readTableHeader(allRawHeaderLines.head);
      val headerColumns = allRawHeaderLines.head.split("\t").toSeq.drop(5)
      
      val variantLines = if(withProgress){
                         internalUtils.stdUtils.wrapIteratorWithAdvancedProgressReporter[SVcfVariantLine](
                           //rawVariantLines.map(line => SVcfInputVariantLine(line,header)),
                           bufLines.map(line => SVcfInputTableVariantLine(line,headerColumns)), 
                           internalUtils.stdUtils.AdvancedIteratorProgressReporter_ThreeLevelAuto[SVcfVariantLine](
                                elementTitle = "lines", lineSec = 60,
                                reportFunction  = ((vc : SVcfVariantLine, i : Int) => " " + vc.chrom +" "+ internalUtils.stdUtils.MemoryUtil.memInfo )
                           )
                         )
      
      } else {
        //rawVariantLines.map(line => SVcfInputVariantLine(line,header))
        bufLines.map(line => SVcfInputTableVariantLine(line,headerColumns))
      }
      
      (header,variantLines);
    }
    
    def readTableToVcfNoID(lines : Iterator[String], withProgress : Boolean = true) : (SVcfHeader,Iterator[SVcfVariantLine]) = {
      val bufLines = lines.buffered;
      val headerLine = bufLines.next
      val header = readTableHeaderNoID(headerLine);
      val headerColumns = headerLine.split("\t").toSeq.drop(4)
      
      val variantLines = if(withProgress){
                         internalUtils.stdUtils.wrapIteratorWithAdvancedProgressReporter[SVcfVariantLine](
                           //rawVariantLines.map(line => SVcfInputVariantLine(line,header)),
                           bufLines.map(line => SVcfInputTableVariantLineNoID(line,headerColumns)), 
                           internalUtils.stdUtils.AdvancedIteratorProgressReporter_ThreeLevelAuto[SVcfVariantLine](
                                elementTitle = "lines", lineSec = 60,
                                reportFunction  = ((vc : SVcfVariantLine, i : Int) => " " + vc.chrom +" "+ internalUtils.stdUtils.MemoryUtil.memInfo )
                           )
                         )
      
      } else {
        //rawVariantLines.map(line => SVcfInputVariantLine(line,header))
        bufLines.map(line => SVcfInputTableVariantLineNoID(line,headerColumns))
      }
      
      (header,variantLines);
    }
    /*
    def readTableToVcfAdv(lines : Iterator[String], withProgress : Boolean = true,
                   skipLines : Int = 0, 
                   columnNameCHROM : String = "CHROM",columnNamePOS : String  = "POS", columnNameREF : String = "REF",columnNameALT : String = "ALT"
    ) : (SVcfHeader,Iterator[SVcfVariantLine]) = {
      val bufLines = lines.drop(skipLines).buffered;
      //if(! bufLines.head.startsWith("#CHROM\tPOS\tID\tREF\tALT")){
      //  error("Error: table must begin with string: \"#CHROM\tPOS\tID\tREF\tALT\"");
      //}
      
      val headerLine = bufLines.next();
      val headerColumnsRaw = headerLine.split("\t").toSeq
      val headerElemList = Seq("CHROM","POS","REF","ALT");
      val headerIdxList = headerElemList.map{ h => {
        headerColumnsRaw.
      }}
      val tableLines = bufLines.map{ 
        
      }
      
      val variantLines = if(withProgress){
                         internalUtils.stdUtils.wrapIteratorWithAdvancedProgressReporter[SVcfVariantLine](
                           //rawVariantLines.map(line => SVcfInputVariantLine(line,header)),
                           bufLines.map(line => SVcfInputTableVariantLine(line,headerColumns)), 
                           internalUtils.stdUtils.AdvancedIteratorProgressReporter_ThreeLevelAuto[SVcfVariantLine](
                                elementTitle = "lines", lineSec = 60,
                                reportFunction  = ((vc : SVcfVariantLine, i : Int) => " " + vc.chrom +" "+ internalUtils.stdUtils.MemoryUtil.memInfo )
                           )
                         )
      
      } else {
        //rawVariantLines.map(line => SVcfInputVariantLine(line,header))
        bufLines.map(line => SVcfInputTableVariantLine(line,headerColumns))
      }
      
      (header,variantLines);
    }*/
    
    
    def readVcf(lines : Iterator[String], withProgress : Boolean = true) : (SVcfHeader,Iterator[SVcfInputVariantLine]) = {
      reportln("     readVcf() init: buffering lines ... ("+getDateAndTimeString+")","debug")
      val bufLines = lines.buffered;
      reportln("     readVcf() init: lines buffered, extracting header lines... ("+getDateAndTimeString+")","debug")
      val allRawHeaderLines = extractWhile(bufLines)(line => line.startsWith("#"));
      reportln("     readVcf() init: headerlines extracted, processing header... ("+getDateAndTimeString+")","debug")
      val header = readVcfHeader(allRawHeaderLines);
      reportln("     readVcf() iterator initialized... ("+getDateAndTimeString+")","debug")

      val variantLines = if(withProgress){
        reportln("     readVcf() adding progress reporting... ("+getDateAndTimeString+")","debug")
                         internalUtils.stdUtils.wrapIteratorWithAdvancedProgressReporter[SVcfInputVariantLine](
                           //rawVariantLines.map(line => SVcfInputVariantLine(line,header)),
                           bufLines.map(line => SVcfInputVariantLine(line).headerInput(header)), 
                           internalUtils.stdUtils.AdvancedIteratorProgressReporter_ThreeLevelAuto[SVcfInputVariantLine](
                                elementTitle = "lines", lineSec = 60,
                                reportFunction  = ((vc : SVcfInputVariantLine, i : Int) => " " + vc.chrom +" "+ internalUtils.stdUtils.MemoryUtil.memInfo )
                           )
                         )
      } else {
        reportln("     readVcf() skipping progress reporting... ("+getDateAndTimeString+")","debug")
        //rawVariantLines.map(line => SVcfInputVariantLine(line,header))
        bufLines.map(line => SVcfInputVariantLine(line).headerInput(header))
      }
      reportln("     readVcf() done... ("+getDateAndTimeString+")","debug")
      (header,variantLines);
    }
    
    
    /*
    SVcfCompoundHeaderLine(val in_tag : String, val ID : String, val Number : String, val Type : String, val desc : String, 
                               val vakUtil : Option[String] = None, val vakStepNum : Option[String] = None, val vakVer : Option[String] = None,val subType : Option[String] = None,
                               val extraFields : Map[String,String] = Map[String,String]())
     */
    //Read Header Lines:
    def readTableHeader( line : String ) : SVcfHeader = {
      var infoLines = line.split("\t").drop(5).toSeq.map{ ss => {
        new SVcfCompoundHeaderLine(in_tag = "INFO",ID=ss,Number = ".", Type = "String",desc = "No desc available");
      }}//SVcfHeaderLine(in_tag : String, var in_value : String)
      var formatLines = Seq[SVcfCompoundHeaderLine]();
      var otherHeaderLines = Seq[SVcfHeaderLine]( new SVcfHeaderLine("fileformat","VCFv4.2") );
      var walkLines = Seq[SVcfWalkHeaderLine]();
      var titleLine = SVcfTitleLine(Seq[String]())
      
      SVcfHeader(infoLines, formatLines, otherHeaderLines,walkLines, titleLine);
    }
    def readTableHeaderNoID( line : String ) : SVcfHeader = {
      var infoLines = line.split("\t").drop(4).toSeq.map{ ss => {
        new SVcfCompoundHeaderLine(in_tag = "INFO",ID=ss,Number = ".", Type = "String",desc = "No desc available");
      }}
      var formatLines = Seq[SVcfCompoundHeaderLine]();
      var otherHeaderLines = Seq[SVcfHeaderLine]( new SVcfHeaderLine("fileformat","VCFv4.2") );
      var walkLines = Seq[SVcfWalkHeaderLine]();
      var titleLine = SVcfTitleLine(Seq[String]())
      
      SVcfHeader(infoLines, formatLines, otherHeaderLines,walkLines, titleLine);
    }
    
    def readVcfHeader(lines : Seq[String]) : SVcfHeader = {
      reportln("     readVcfHeader() init ... ("+getDateAndTimeString+")","debug")
      var infoLines = Seq[SVcfCompoundHeaderLine]();
      var formatLines = Seq[SVcfCompoundHeaderLine]();
      var otherHeaderLines = Seq[SVcfHeaderLine]();
      var walkLines = Seq[SVcfWalkHeaderLine]();
      
      reportln("     readVcfHeader() partitioning taglines ... ("+getDateAndTimeString+")","debug")
      val (tagLines, nonTagLines) = lines.partition(line => line.startsWith("##"));
      
      reportln("     readVcfHeader() loading metadata into memory ... ("+getDateAndTimeString+")","debug")
      tagLines.foreach(line => {
        if(line.startsWith("##INFO=")){
          infoLines = infoLines :+ makeCompoundLineFromString(line);
        } else if(line.startsWith("##FORMAT=")){
          formatLines = formatLines :+ makeCompoundLineFromString(line);
        } else if(line.startsWith("##SVCFWALK=")){
          walkLines = walkLines :+ makeWalkHeaderLineFromString(line);
        } else {
          otherHeaderLines = otherHeaderLines :+ makeSimpleHeaderLineFromString(line);
        }
      })
      if(nonTagLines.length != 1){
         warning("VCF header line malformed? Found multiple header rows that do not start with \"##\""+ nonTagLines.map{line => "        \""+line+"\""}.mkString("\n"),"MALFORMED_VCF_HEADER_LINE",100);
      }
      reportln("     readVcfHeader() reading table title-line ... ("+getDateAndTimeString+")","debug")
      val titleLine = SVcfTitleLine(nonTagLines.filter{_.startsWith("#CHROM")}.head.split("\t").drop(9));
      reportln("     readVcfHeader() done ... ("+getDateAndTimeString+")","debug")
      SVcfHeader(infoLines, formatLines, otherHeaderLines,walkLines, titleLine);
    }
    
    def makeSimpleHeaderLineFromString(line : String) : SVcfHeaderLine = {
      val tagPair = line.drop(2).split("=",2);
      if(tagPair.length != 2){
        warning("VCF header line malformed?\n\""+line+"\"","MALFORMED_VCF_HEADER_LINE",100);
      }
      val tag = tagPair(0);
      val value = tagPair(1);
      new SVcfHeaderLine(tag,value);
    }
    
    def makeCompoundLineFromString(line : String) : SVcfCompoundHeaderLine = {
      val tagPair = line.drop(2).split("=",2);
      val tag = tagPair(0);
      var hasWarn = false;
      val tagmap = tagPair(1).tail.init.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)").map(compoundString => {
        val ctp = compoundString.split("=",2);
        (ctp(0),(if(ctp.length > 1){
          cleanQuotes(ctp(1))
        } else {
          hasWarn = true;
          warning("Warning: Malformed compound header line:\n\""+line+"\"","Malformed_Compound_Header_Line",-1);
          "."
        }));
      }).toMap;
      
      if(hasWarn){
        warning("SVcfCompoundHeaderLine(tag = "+tag+", ID="+tagmap("ID")+", Number="+tagmap("Number")+", Type="+tagmap("Type")+", desc="+tagmap.getOrElse("Description",".")+")","Malformed_Compound_Header_Line_Continued",-1);
      }
// val vakUtil : Option[String] = None, val vakStepNum : Option[String] = None, val vakVer : Option[String] = None,val subType : Option[String] = None,

      val knownFields = Set("ID","Number","Type","Description","subType","vakUtil","vakStepNum","vakVer");
      
      val extraTagMapVector = tagmap.filter{ case (t,v) => ! knownFields.contains(t)}.toVector;
      val extraTagMap = extraTagMapVector.toMap
      
      new SVcfCompoundHeaderLine(in_tag = tag, ID = tagmap("ID"), Number = tagmap("Number") ,  Type = tagmap("Type") , desc = tagmap.getOrElse("Description","."),
          subType = tagmap.get("subType"),vakUtil=tagmap.get("walkerName"),vakStepNum=tagmap.get("walkerNum"),vakVer=tagmap.get("walkerVer"),
          extraFields=extraTagMap
      ); 
    }
    
    /////////// Read Variant Lines:
    
  }
  /*
  case class SVcfAdvancedFileIterator(infile : String, indexFile : Option[String]) extends Iterator[SVcfVariantLine] {
    val indataRawHeaderReader = getLinesSmartUnzip(infile).buffered
    val header = SVcfLine.readVcfHeader(extractWhile(indataRawHeaderReader)( line => line.startsWith("#")))
    
    if(indexFile.isDefined){
      htsjdk.tribble.readers.TabixReader.TabixReader(infile, indexFile.get) 
      indataRawHeaderReader.close();
    }
    
    
    
  }*/
  
  /*
   * ,//##SVCFSTATS='
   * 
    def getBool(s : String) : Option[Boolean] = {
      stats.get(s).map{b => b == "TRUE"}
    }
    def get(s : String) : Option[String] = {
      stats.get(s)
    }
    
    def add(a : String, b : String) : SVcfSStatLine = {
      SVcfSStatLine(stats = stats.updated(a,b))
    }
   * 
                        var svcfStatLine :
   */
  
  case class SVcfHeader(var infoLines : Seq[SVcfCompoundHeaderLine], 
                        var formatLines : Seq[SVcfCompoundHeaderLine], 
                        var otherHeaderLines : Seq[SVcfHeaderLine],
                        var walkLines : Seq[SVcfWalkHeaderLine],
                        var titleLine : SVcfTitleLine,
                        var addedInfos : Set[String] = Set[String](),
                        var addedFmts : Set[String] = Set[String](),
                        var sStatLine : Option[SVcfSStatLine] = None,
                        var metadataLines : Seq[SVcfCompoundHeaderLine] = Seq()){
    def getVcfLines : Seq[String] = (otherHeaderLines ++ sStatLine ++ walkLines ++ infoLines ++ formatLines :+ titleLine).map(_.getVcfString);
    
    def getVcfHeaderLines : Seq[SVcfHeaderLine] = otherHeaderLines ++ sStatLine ++ walkLines ++ infoLines ++ formatLines;
    
    def reportAddedInfos(walker : SVcfWalker){
      reportln("    "+walker.walkerName+" adds INFO lines:   "+addedInfos.toVector.sorted.padTo(1,"(NONE)").mkString(","),"debug")
      reportln("    "+walker.walkerName+" adds FORMAT lines: "+addedFmts.toVector.sorted.padTo(1,"(NONE)").mkString(","),"debug")
    }
    
    def addStat(statID : String, statVal : String){
      sStatLine = Some(sStatLine.getOrElse( SVcfSStatLine() ).add( statID, statVal ))
    }
    def addStatBool(statID : String, statVal : Boolean){
      sStatLine = Some(sStatLine.getOrElse( SVcfSStatLine() ).add( statID, statVal.toString() ))
    }
    def getStat(statID : String) : Option[String] = {
      sStatLine.getOrElse( SVcfSStatLine() ).get( statID )
    }
    def getStatBool(statID : String) : Option[Boolean] = {
      sStatLine.getOrElse( SVcfSStatLine() ).get( statID ).map{ ss => ss == "true" }
    }
    def isSplitMA : Boolean = {
      sStatLine.getOrElse( SVcfSStatLine() ).get( "isSplitMultAlle" ).map{ ss => ss == "true" }.getOrElse(false)
    }
    def isSplitStarMA : Boolean = {
      sStatLine.getOrElse( SVcfSStatLine() ).get( "isSplitMultAlleStar" ).map{ ss => ss == "true" }.getOrElse(false)
    }
    
    /*def addFormatLine(line : SVcfCompoundHeaderLine, walker : Option[SVcfWalker] = None){
      val idx = formatLines.indexWhere{(p : SVcfCompoundHeaderLine) => {p.ID == line.ID}}
      if( idx != -1 ){
        formatLines = formatLines.updated(idx,line);
      } else {
        formatLines = formatLines :+ line;
      }
    }*/
    def addInfoLine(line : SVcfCompoundHeaderLine, walker : Option[SVcfWalker] = None){
      if(line.in_tag != "INFO"){
        error("Impossible state! INFO field assigned with tag=\""+line.in_tag+"\" (rather than INFO)")
      }
      val lineOut : SVcfCompoundHeaderLine = walker match {
        case Some(w) => {
          new SVcfInfoHeaderLine(ID=line.ID,Number=line.Number,Type=line.Type,desc = line.desc,
              subType = line.subType,
              vakUtil= Some(w.walkerName),vakStepNum=w.walkerNumber,vakVer=Some(runner.runner.UTIL_COMPLETE_VERSION),
              extraFields=line.extraFields
          )
        }
        case None => line;
      }
      addedInfos = addedInfos + line.ID;
      val idx = infoLines.indexWhere{(p : SVcfCompoundHeaderLine) => {p.ID == line.ID}}
      if( idx != -1 ){
        infoLines = infoLines.updated(idx,line);
      } else {
        infoLines = infoLines :+ line;
      }
      
    }
    
    
    def addMetaLine(line : SVcfCompoundHeaderLine){
      metadataLines = metadataLines :+ line.updateTag(OPTION_TAGPREFIX+"METADATA_"+line.in_tag);
    }
    
    def addFormatLine(line : SVcfCompoundHeaderLine, walker : Option[SVcfWalker] = None){
      if(line.in_tag != "FORMAT"){
        error("Impossible state! FORMAT field assigned with tag=\""+line.in_tag+"\" (rather than FORMAT)")
      }
      val lineOut : SVcfCompoundHeaderLine = walker match {
        case Some(w) => {
          new SVcfFormatHeaderLine(ID=line.ID,Number=line.Number,Type=line.Type,desc = line.desc,
              subType = line.subType,
              vakUtil= Some(w.walkerName),vakStepNum=w.walkerNumber,vakVer=Some(runner.runner.UTIL_COMPLETE_VERSION),
              extraFields=line.extraFields
          )
        }
        case None => line;
      }
      addedFmts = addedFmts + line.ID;
      val idx = formatLines.indexWhere{(p : SVcfCompoundHeaderLine) => {p.ID == line.ID}}
      if( idx != -1 ){
        formatLines = formatLines.updated(idx,line);
      } else {
        formatLines = formatLines :+ line;
      }
    }
    
    def getSampleList : Seq[String] = {
      titleLine.sampleList;
    }
    def getSampleIdx(sampID : String) : Int = {
      getSampleList.indexOf(sampID);
    }
    def sampleCt : Int = {
      getSampleList.length;
    }
    
    def copyHeader : SVcfHeader = {
      SVcfHeader(infoLines = infoLines,
                 formatLines = formatLines,
                 otherHeaderLines = otherHeaderLines,
                 walkLines = walkLines,
                 titleLine = titleLine,
                 sStatLine = sStatLine);
    }
    
    def addWalk(walker : SVcfWalker){
      val walkNum = if(walkLines.length == 0) 1 else (walkLines.last.n + 1)
      walker.walkerNumber = Some(walkNum.toString);
      walkLines = walkLines :+ SVcfWalkHeaderLine(ID=walker.walkerName,n=walkNum,params=walker.walkerParams)
    }
    def addWalkerLikeCommand(ID : String, params : Seq[(String,String)]){
      val walkNum = if(walkLines.length == 0) 1 else (walkLines.last.n + 1)
      walkLines = walkLines :+ SVcfWalkHeaderLine(ID=ID,n=walkNum,params=params)
    }
    
    
    def isSplitMultiallelic() : Boolean = {
      walkLines.exists( w => w.ID == "SSplitMultiAllelics")
    }
    
  }
  
  abstract class SVcfLine {
    def getVcfString : String;
  }
  class SVcfHeaderLine(in_tag : String, var in_value : String) extends SVcfLine{
    var tag : String = in_tag;
    var value : String = in_value;
    
    def getVcfString : String = "##" + tag + "=" + value;
    
    def convertToContigHeaderLine() : Option[SVcfContigHeaderLine] = {
      if(tag == "contig"){
          val tagmap = value.tail.init.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)").map(compoundString => {
            val ctp = compoundString.split("=",2);
            (ctp(0),(if(ctp.length > 1){
              cleanQuotes(ctp(1))
            } else {
              warning("Warning: Malformed contig header line:\n\""+this.getVcfString+"\"","Malformed_Contig_Header_Line",-1);
              "."
            }));
          }).toMap;
          Some(SVcfContigHeaderLine(ID = tagmap("ID"),length = tagmap("length")))
      } else {
        None;
      }
    }
  }
  //##SVCFSTATS=
  def parseSStatLine(s : String) : SVcfSStatLine = {
    var currString = s;
    if(! currString.startsWith("##")){
      error("SVcfSStatLine must be a header line. Impossible state!")
    }
    currString = currString.drop(2).trim;
    if(! currString.startsWith("SVCFSTATS")){
      error("SVcfSStatLine must start with SVCFSTATS. Impossible state!")
    }
    currString = currString.drop("SVCFSTATS".length).trim;
    if(! currString.startsWith("=")){
      error("Impossible state! Malformed SStatLine");
    }
    currString = currString.drop("=".length).trim;
    if(! currString.startsWith("<")){
      error("Impossible state! Malformed SStatLine");
    }
    currString = currString.drop("<".length).trim;
    if(! (currString.last == '>')){
      error("Impossible state! Malformed SStatLine");
    }
    currString = currString.dropRight(1);
    
    SVcfSStatLine(currString.split(",").map{ ss => ss.trim.split("=").map{sss => sss.trim} }.map{ ssc => (ssc(0),ssc(1)) }.toMap )

  }
  
  val safeReplacementsForCompoundHeaderLine : Map[String,String] = Map[String,String]( ("=","&#61"),("<","&#60"),(">","&#62"),(",","&#44") )
  
  case class SVcfSStatLine(stats : Map[String,String] = Map[String,String]()) extends SVcfHeaderLine("SVCFSTATS", 
                                                                                        Seq("<",
                                                                                            (stats.map{ case (a,b) => a +"="+b }).mkString(","),
                                                                                        ">").mkString("")){
    lazy val isLAT : Option[Boolean] = stats.get("isLAT").map{b => b == "TRUE"}
    lazy val isMultAlleSplit : Option[Boolean] = stats.get("isMultAlleSplit").map{b => b == "TRUE"}
    lazy val isStarMultAlleSplit : Option[Boolean] = stats.get("isStarMultAlleSplit").map{b => b == "TRUE"}
    def getBool(s : String) : Option[Boolean] = {
      stats.get(s).map{b => b == "TRUE"}
    }
    def get(s : String) : Option[String] = {
      stats.get(s)
    }
    
    def add(a : String, b : String) : SVcfSStatLine = {
      SVcfSStatLine(stats = stats.updated(a,b))
    }
  }
  case class SVcfTitleLine(sampleList : Seq[String]) extends SVcfLine {
    def getVcfString : String = if(sampleList.length == 0){
      "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO";
    } else {
      "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT\t"+sampleList.mkString("\t");
    }
  }
  
  val subtype_GtStyle    = "GtStyle"
  val subtype_GtStyleUnsplit = "GtStyle.unsplit"
  val subtype_GtStyle3   = "GtStyle.triAlle"

  val subtype_ListOfLists = "ListOfLists"
  val subtype_AlleleCounts = "AlleleCounts"
  val subtype_AlleleCountsUnsplit = "AlleleCounts.unsplit"
  val subtype_MapList = "MapList:";
  val subtype_AlleleList = "AlleleList"
  val subtype_AlleleListSubMap = "AlleleList|SubMapList:"
  // addQuoteIfNeededAndMakeSafe( s : String, replacements : Map[String,String])
  
  
  class SVcfCompoundHeaderLine(val in_tag : String, val ID : String, val Number : String, val Type : String, val desc : String, 
                               val vakUtil : Option[String] = None, val vakStepNum : Option[String] = None, val vakVer : Option[String] = None,val subType : Option[String] = None,
                               val extraFields : Map[String,String] = Map[String,String]()) extends 
              SVcfHeaderLine(in_tag,"<ID="+ID+",Number="+Number+",Type="+Type+","+
                  (Seq(Some(addQuotesIfNeeded(desc)),subType,vakUtil,vakStepNum,vakVer).zip(Seq("Description","subType","vakUtil","vakStepNum","vakVer")).flatMap{ case (v,t) => {
                    v match {
                      case Some(vv) => {
                        Some(t+"="+addQuoteIfNeededAndMakeSafe(vv,safeReplacementsForCompoundHeaderLine));
                      }
                      case None => None;
                    }
                  }} ++ 
                  extraFields.toVector.sorted.map{case (t,v) => { t+"="+addQuotesIfNeeded(v) }}).mkString(",")+">") {
    
    def addWalker(w : SVcfWalker) : SVcfCompoundHeaderLine = {
      
      /*
                 new SVcfInfoHeaderLine(ID=line.ID,Number=line.Number,Type=line.Type,desc = line.desc,
              subType = line.subType,
              vakUtil= Some(w.walkerName),vakStepNum=w.walkerNumber,vakVer=Some(runner.runner.UTIL_COMPLETE_VERSION),
              extraFields=line.extraFields
          )
       */
      
      new SVcfCompoundHeaderLine(in_tag = in_tag, ID = ID, Number = Number, Type = Type, desc = desc,subType = subType,extraFields=extraFields,
                             vakUtil= Some(w.walkerName),vakStepNum=w.walkerNumber,vakVer=Some(runner.runner.UTIL_COMPLETE_VERSION)
                             );
                             
    }
    def hasMetadata() : Boolean = {
      vakUtil.isDefined  || vakStepNum.isDefined || vakVer.isDefined || subType.isDefined || extraFields.nonEmpty
    }
    
    def addExtraField(t : String, v : String) : SVcfCompoundHeaderLine = {
      new SVcfCompoundHeaderLine(in_tag = in_tag, ID = ID, Number = Number, Type = Type, desc = desc,subType = subType,extraFields=extraFields + ((t,v)),
                             vakUtil= vakUtil,vakStepNum=vakStepNum,vakVer=vakVer
                             );
    }
    def updateNumber(n : String) : SVcfCompoundHeaderLine = {
      new SVcfCompoundHeaderLine(in_tag = in_tag, ID = ID, Number = n, Type = Type, desc = desc,subType = subType,extraFields=extraFields,
                             vakUtil= vakUtil,vakStepNum=vakStepNum,vakVer=vakVer
                             );
    }
    def updateType(t : String) : SVcfCompoundHeaderLine = {
      new SVcfCompoundHeaderLine(in_tag = in_tag, ID = ID, Number = Number, Type = t, desc = desc,subType = subType,extraFields=extraFields,
                             vakUtil= vakUtil,vakStepNum=vakStepNum,vakVer=vakVer
                             );
    }
    def updateSubtype(t : Option[String]) : SVcfCompoundHeaderLine = {
      new SVcfCompoundHeaderLine(in_tag = in_tag, ID = ID, Number = Number, Type = Type, desc = desc,subType = t,extraFields=extraFields,
                             vakUtil= vakUtil,vakStepNum=vakStepNum,vakVer=vakVer
                             );
    }
    def updateID(newid : String) : SVcfCompoundHeaderLine = {
      new SVcfCompoundHeaderLine(in_tag = in_tag, ID = newid, Number = Number, Type = Type, desc = desc,
                             subType = subType,extraFields=extraFields,
                             vakUtil= vakUtil,vakStepNum=vakStepNum,vakVer=vakVer
                             );
    }
    def updateDesc(d : String) : SVcfCompoundHeaderLine = {
      new SVcfCompoundHeaderLine(in_tag = in_tag, ID = ID, Number = Number, Type = Type, desc = d,
                             subType = subType,extraFields=extraFields,
                             vakUtil= vakUtil,vakStepNum=vakStepNum,vakVer=vakVer
                             );
    }
    
    def updateTag(tt : String) : SVcfCompoundHeaderLine = {
      new SVcfCompoundHeaderLine(in_tag = tt, ID = ID, Number = Number, Type = Type, desc = desc,
                             subType = subType,extraFields=extraFields,
                             vakUtil= vakUtil,vakStepNum=vakStepNum,vakVer=vakVer
                             );
    }
    
    /*
               new SVcfInfoHeaderLine(ID=line.ID,Number=line.Number,Type=line.Type,desc = line.desc,
              subType = line.subType,
              vakUtil= Some(w.walkerName),vakStepNum=w.walkerNumber,vakVer=Some(runner.runner.UTIL_COMPLETE_VERSION),
              extraFields=line.extraFields
          )
     
     */
    
    /*def addWalker(walker : SVcfWalker) : SVcfCompoundHeaderLine = {
       return new SVcfCompoundHeaderLine(in_tag = in_tag, ID = ID, Number = Number, Type = Type, desc = desc, 
                               vakUtil = , val vakStepNum : Option[String] = None, val vakVer : Option[String] = None,val subType : Option[String] = None,
                               val extraFields : scala.collection.mutable.Map[String,String] = new AnyRefMap[String,String]()) 
    }*/
    
    //var tag : String = in_tag;
    //var value : String = "<ID="+ID+",Number="+Number+",Type="+Type+",Description=\""+desc+"\">";
  }

  class SVcfFormatHeaderLine(ID : String, Number : String, Type : String, desc : String, 
                           vakUtil : Option[String] = None, vakStepNum : Option[String] = None, vakVer : Option[String] = None,
                           subType : Option[String] = None,
                           extraFields : Map[String,String] = Map[String,String]()) extends 
                           SVcfCompoundHeaderLine(in_tag="FORMAT",ID=ID,Number=Number,Type=Type,desc=desc,
                               vakUtil =vakUtil,vakStepNum=vakStepNum,vakVer=vakVer,subType=subType,
                               extraFields=extraFields){
    
  }
  
  class SVcfInfoHeaderLine(ID : String, Number : String, Type : String, desc : String, 
                           vakUtil : Option[String] = None, vakStepNum : Option[String] = None, vakVer : Option[String] = None,
                           subType : Option[String] = None,
                           extraFields : Map[String,String] = Map[String,String]()) extends 
                           SVcfCompoundHeaderLine(in_tag="INFO",ID=ID,Number=Number,Type=Type,desc=desc,
                               vakUtil =vakUtil,vakStepNum=vakStepNum,vakVer=vakVer,subType=subType,
                               extraFields=extraFields){
    
  }
  
  case class SVcfContigHeaderLine(var ID : String, var length : String) extends SVcfHeaderLine("contig","<ID="+ID+",length="+length+">") {
    
  }
  
  
  def makeWalkHeaderLineFromString(line : String) : SVcfWalkHeaderLine = {
      val tagPair = line.drop(2).split("=",2);
      val tag = tagPair(0);
      //var hasWarn = false;
      val tagCells = tagPair(1).tail.init.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
      if(! tagCells(0).startsWith("ID=")) error("Malformed Walk Header Line: tag list must start with ID!\n   "+line);
      val tagarray = tagCells.map(compoundString => {
        val ctp = compoundString.split("=",2);
        (ctp(0),(if(ctp.length > 1){
          cleanQuotes(ctp(1))
        }else{
          //hasWarn = true;
          warning("Warning: Malformed walker header line:\n\""+line+"\"","MALFORMED_WALKER_LINE",100);
          "."
        }));
      })
      val taglist = tagarray.toSeq.map{_._1}
      val tagmap = tagarray.toMap;
      
      //if(tagmap.length < 6) error("Malformed WalkHeader Line: tag list must have at least 6 elements!\n   "+line);
      //if(hasWarn){
       // warning("SVcfWalkHeaderLine(tag = "+tag+", ID="+tagmap("ID")+")","Malformed_Walker_Header_Line_Continued",-1);
      //}
      SVcfWalkHeaderLine(ID=tagmap.getOrElse("ID","???"),
                         n = string2int(tagmap.getOrElse("n","-1")),
                         params = taglist.filter{t => t.startsWith("PARAM_")}.map{t => (t.drop(6),tagmap(t))},
                         timeStamp = string2long(tagmap.getOrElse("timeStamp","-1")),
                         dateTime = tagmap.getOrElse("dateTime","???"),
                         v = tagmap.getOrElse("v","???"),
                         vCompileStamp=string2long(tagmap.getOrElse("vCompileStamp","-1"))
                         );
    
  }
  
  case class SVcfWalkHeaderLine(ID : String, n : Int, params : Seq[(String,String)],
                                                      timeStamp : Long = runner.runner.RUNNER_EXECUTION_STARTTIME_MILLIS,
                                                      dateTime : String = runner.runner.RUNNER_EXECUTION_STARTTIME_SIMPLESTRING,
                                                      v : String = runner.runner.UTIL_VERSION,
                                                      vCompileStamp : Long = runner.runner.UTIL_COMPILE_TIME
                                                      ) extends
               SVcfHeaderLine("SVCFWALK",
                              "<"+(
                                     Seq("ID="+ID,
                                       "n="+n,
                                       "timeStamp="+timeStamp,
                                       "dateTime="+"\""+dateTime+"\"",
                                       "v="+v,
                                       "vCompileStamp="+vCompileStamp
                                     ) ++ params.map{case (i,s) => "PARAM_"+i+"="+addQuotesIfNeeded(s)+""}
                                   ).mkString(",")+">"){
     
  }
  
  
  
  //vMutINFO_TAG,numSplit_TAG,splitIdx_TAG,splitAlle_TAG
  case class SVcfParamLine(
                           isMultiSplit : Boolean,
                           vMutINFO_TAG : Option[String],
                           numSplit_TAG : Option[String],
                           splitIdx_TAG : Option[String],
                           splitAlle_TAG : Option[String],
                           iTimeStamp : Long,
                           iDateTime : String,
                           lmTimeStamp : Long,
                           lmDateTime : String
      ) extends SVcfHeaderLine("SVcfPARAMETERS","<"+(
                                     Seq(
                                       "initTimeStamp="+iTimeStamp,
                                       "initDateTime="+"\""+iDateTime+"\"",
                                       "lastModTimeStamp="+lmTimeStamp,
                                       "lastModDateTime="+"\""+lmDateTime+"\""
                                     ) ++ Seq(("vMutINFO_TAG",vMutINFO_TAG),
                                              ("numSplit_TAG",numSplit_TAG),
                                              ("splitIdx_TAG",splitIdx_TAG),
                                              ("splitAlle_TAG",splitAlle_TAG)).flatMap{ case (tag,v) => {
                                       v match {
                                         case Some(vv) => {
                                           Some(tag + "=\""+vv+"\"")
                                         }
                                         case None => {
                                           None
                                         }
                                       }
                                     }}
                                   ).mkString(",")+">") {
    
  }
  
  def convertCountTypeToString(infoLine : VCFInfoHeaderLine) : String = {
        val Number : String= if( infoLine.getCountType() == VCFHeaderLineCount.UNBOUNDED ){
          "."
        } else if(infoLine.getCountType() == VCFHeaderLineCount.A ){
          "A"
        } else if(infoLine.getCountType() == VCFHeaderLineCount.R ){
          "R"
        } else if(infoLine.getCountType() == VCFHeaderLineCount.G ){
          "G"
        } else if(infoLine.getCountType() == VCFHeaderLineCount.INTEGER ){
          infoLine.getCount().toString;
        } else {
          "???"
        }
        return Number;
  }
  
  def convertInfoLineFormat(infoLine : VCFInfoHeaderLine) : SVcfCompoundHeaderLine = {
        val Key : String = infoLine.getID()
        val Number : String= if( infoLine.getCountType() == VCFHeaderLineCount.UNBOUNDED ){
          "."
        } else if(infoLine.getCountType() == VCFHeaderLineCount.A ){
          "A"
        } else if(infoLine.getCountType() == VCFHeaderLineCount.R ){
          "R"
        } else if(infoLine.getCountType() == VCFHeaderLineCount.G ){
          "G"
        } else if(infoLine.getCountType() == VCFHeaderLineCount.INTEGER ){
          infoLine.getCount().toString;
        } else {
          "???"
        }
        
        val desc : String= infoLine.getDescription();
        val Type : String = if(infoLine.getType() == VCFHeaderLineType.Character){
          "Character"
        } else if(infoLine.getType() == VCFHeaderLineType.Integer){
          "Integer"
        } else if(infoLine.getType() == VCFHeaderLineType.Float){
          "Float"
        } else if(infoLine.getType() == VCFHeaderLineType.Flag){
          "Flag"
        } else if(infoLine.getType() == VCFHeaderLineType.String){
          "String"
        } else {
          "???"
        }
        new SVcfCompoundHeaderLine(in_tag="INFO",ID = Key,Number = Number, Type = Type, desc = desc);
  }
  
  /*class SVcfVariantLine extends SVcfLine {
    def getChrom : String;
    def setChrom : Unit;
    def getPos : String;
    def setPos : Unit;
    def getRef : String;
    def setRef : Unit;
    def getAlt : String;
    def setAlt : Unit;
    def 
  }*/
  
  object SVcfVariantLine {
    val dummyHeaderTag : String = "TESTSNPEFFDUMMYVAR";
    def getDummyIterator() : (org.snpeff.fileIterator.VcfFileIterator,org.snpeff.vcf.VcfHeader) = {
      val dummyIter = new org.snpeff.fileIterator.VcfFileIterator();
      dummyIter.setIgnoreChromosomeErrors(true)
      val header = new org.snpeff.vcf.VcfHeader()
      header.addInfo(new org.snpeff.vcf.VcfHeaderInfo("##INFO=<ID="+dummyHeaderTag+",Number=1,Type=String,Description=\"TestFieldPleaseIgnore\">"))
      dummyIter.setIgnoreChromosomeErrors(true)
      dummyIter.setVcfHeader(header);
      (dummyIter,header);
    }
  }
  
  val PATTERN_BASES_ONLY = java.util.regex.Pattern.compile("^[ATCGNatcgn]*$")
  
  val hardCodedChromMap : Map[String,Int] = Map[String,Int](
         ("X",1e8.toInt),
         ("Y",2e8.toInt),
         ("XY",3e8.toInt),
         ("M",4e8.toInt),
         ("MT",5e8.toInt)
      )

  case class Chrom( s : String ) extends Ordered[Chrom] {
    def nochrID : String = s.replaceAll("^[Cc][Hh][Rr]","");
    def chrNum : Option[Int] = string2intOpt(nochrID) match {
      case Some(x) => Some(x);
      case None => {
        hardCodedChromMap.get(nochrID)
      }
    }
    def isNum : Boolean = chrNum.isDefined;
    
    def compare( that : Chrom ): Int = {
      chrNum match {
        case Some(thisn) => {
          that.chrNum match {
            case Some(thatn) => {
              thisn.compare(thatn)
            }
            case None => {
              -1
            }
          }
        }
        case None => {
          that.chrNum match {
            case Some(thatn) => {
              1
            }
            case None => {
              nochrID.compare(that.nochrID);
            }
          }
        }
      }
    }
  }
  case class ChromPos( chr : Chrom, pos : Int ){

  }
    
  object SVbnd {
    
    def makeSVbnd( chr : String, pos : Int, ref : String, strand : String ) : SVbnd = {
      val chrpos = chr+":"+pos;
      if(strand == "++"){
        SVbnd( ref+"["+chrpos+"[" )
      } else if(strand == "+-"){
        SVbnd( ref+"]"+chrpos+"]" )
      } else if(strand == "-+"){
        SVbnd( "["+chrpos+"["+ref )
      } else if(strand == "--"){
        SVbnd( "]"+chrpos+"]"+ref )
      } else {
        error("ERROR: ILLEGAL SV: "+chr+"/"+pos+"/"+strand);
        null;
      }
    }
    
    
    //SVbnd.makeSVbnd( svb.getChrom, svb.getPos,svb.bases,svb.strand);
  }
  
  case class SVbnd( in_alt : String ) {
    def svalt : String = in_alt;
    def isValid : Boolean = {
      val cca = alt.split("\\[",-1)
      val ccb = alt.split("\\]",-1);
      if( ! ( ( cca.length == 1 && ccb.length == 3 ) || ( cca.length == 3 && ccb.length == 1 ) ) ) return false;
      val cxx = alt.split("[\\[\\]]",-1)
      val cmx = cxx(1)
      val cx  = cmx.split(":",-1);
      if( cx.length != 2 ) return false;
      if( string2intOpt(cx(1)).isEmpty ) return false;
      if( ! (( cxx(0) == "" && cxx(2) != "" ) || ( cxx(0) != "" && cxx(2) == "" )) ) return false
      val cbp = if(cxx(0) == ""){ cxx(2) } else { cxx(0) }
      if( ! PATTERN_BASES_ONLY.matcher(cbp).matches() ) return false;
      return true;
    }
    def invalidIssue : Seq[String] = {
      var xx : Seq[String] = Seq();
      val cca = alt.split("\\[",-1)
      val ccb = alt.split("\\]",-1);
      if( ! ( ( cca.length == 1 && ccb.length == 3 ) || ( cca.length == 3 && ccb.length == 1 ) ) ) xx = xx :+ "Bracket Count";
      val cxx = alt.split("[\\[\\]]",-1)
      val cmx = cxx(1)
      val cx  = cmx.split(":",-1);
      if( cx.length != 2 ) xx = xx :+ "position isn't two elements" ;
      if( string2intOpt(cx(1)).isEmpty ) xx = xx :+ "position POS isn't an integer" ;
      if( ! (( cxx(0) == "" && cxx(2) != "" ) || ( cxx(0) != "" && cxx(2) == "" )) ) xx = xx :+ "bases not only on one side"
      val cbp = if(cxx(0) == ""){ cxx(2) } else { cxx(0) }
      if( ! PATTERN_BASES_ONLY.matcher(cbp).matches() ) xx = xx :+ "Bases arent ACTGN"
      return xx;
    }
    def validate : Option[SVbnd] = if(isValid){ Some(this) } else { None }
    
    def getChrom : String = {
      if( ! isValid){
        error("attempted to get SV chrom from invalid BND alt allele");
      }
      var xx : Seq[String] = Seq();
      val cca = alt.split("\\[",-1)
      val ccb = alt.split("\\]",-1);
      if( ! ( ( cca.length == 1 && ccb.length == 3 ) || ( cca.length == 3 && ccb.length == 1 ) ) ) xx = xx :+ "Bracket Count";
      val cxx = alt.split("[\\[\\]]",-1)
      val cmx = cxx(1)
      val cx  = cmx.split(":",-1);      
      cx(0)
    } 
    def getPos : Int = {
      if( ! isValid){
        error("attempted to get SV chrom from invalid BND alt allele");
      }
      var xx : Seq[String] = Seq();
      val cca = alt.split("\\[",-1)
      val ccb = alt.split("\\]",-1);
      if( ! ( ( cca.length == 1 && ccb.length == 3 ) || ( cca.length == 3 && ccb.length == 1 ) ) ) xx = xx :+ "Bracket Count";
      val cxx = alt.split("[\\[\\]]",-1)
      val cmx = cxx(1)
      val cx  = cmx.split(":",-1);      
      string2int( cx(1) )
    }
    
    
    lazy val alt : String = in_alt;
    lazy val bracket : Char = alt.replaceAll("[^\\[\\]]","").head;
    lazy val bases : String = {
      val x = alt.split("[\\[\\]]",-1)
      if( x.head == "" ){
        x.last
      } else {
        x.head
      }
    }
    lazy val bndBreakEndString : String = {
      alt.split("[\\[\\]]",-1)(1)
    }
    def bndBreakEnd : (String,Int) = {
      val cells = bndBreakEndString.split(":")
      (cells(0),string2int(cells(1)));
    }
    def basesBefore : Boolean = {
      val x = alt.split("[\\[\\]]",-1)
      x.head != "";
    }
    def basesAfter : Boolean = ! basesBefore;
    def bracketOpensRight : Boolean = bracket == '['
    def strands : String = {
      ( if( basesBefore ){ "+" } else { "-" } ) + ( if( bracket == '[' ){ "+" } else { "-" } )
    }
    def strandswap : String = {
      val ss = strands;
      if( ss == "++"){
        "--" 
      } else if(ss == "+-"){
        "+-" 
      } else if(ss == "-+"){
        "-+" 
      } else {
        "++"
      }
    }
  }
  def strandSwapSVstrand( ss : String ) : String = {
      if( ss == "++"){
        "--" 
      } else if(ss == "+-"){
        "+-" 
      } else if(ss == "-+"){
        "-+" 
      } else {
        "++"
      }
  }
  
  abstract class SVcfVariantLine extends SVcfLine {
    def chrom : String
    def pos : Int
    def id : String
    def ref : String
    def alt : Seq[String]
    def qual : String
    def filter : String
    def info : Map[String,Option[String]];
    def format : Seq[String]
    def genotypes : SVcfGenotypeSet;
    lazy val variantIV = internalUtils.commonSeqUtils.GenomicInterval(chrom,'.', start = pos - 1, end = pos + math.max(1,ref.length)); 
    
    def getSVbnd() : Option[SVbnd] = {
      if( info.getOrElse("SVTYPE",None).getOrElse("") == "BND" ){
        if( alt.length == 1 ){
          SVbnd( alt.head ).validate match {
            case Some(s) => {
              Some(s)
            }
            case None => {
              warning("Warning: SV-BND is INVALID: \n\""+this.getVcfStringNoGenotypes+"\"\n    ","SV_INVALID",10)
              None;
            }
          }
        } else {
          warning("Warning: SV-BND is MULTIALLELIC: \n\""+this.getVcfStringNoGenotypes+"\"","SV_MULTIALLELIC",10)
          None;
        }
      } else {
        None
      }
    }

    def getSVdirOrDie() : String = {
      val svb = getSVbnd().get;
      val (chrA,chrB) = (this.chrom,svb.bndBreakEnd._1);
      val (posA,posB) = (this.pos,svb.bndBreakEnd._2);
      val dir    = if( chrA == chrB ){
                     if( posA < posB ){
                       ">"
                     } else {
                       "<"
                     }
                   } else if( Chrom(chrA).compare(Chrom(chrB)) < 0){
                     ">"
                   } else {
                     "<"
                   }
      dir
    }
    def getSVstrdirOrDie() : String = {
      val svb = getSVbnd().get;
      svb.strands + getSVdirOrDie();
    }
    def getSVstrdirSwapOrDie() : String = {
      val svb = getSVbnd().get;
      svb.strandswap + getSVdirOrDie();
    }
    
    def setHeader( h : SVcfHeader )
    def getHeader() : SVcfHeader
    
    def header( h : SVcfHeader ) : SVcfVariantLine = {
      this.setHeader(h);
      return this;
    }
    
    //Note: zero based!
    def start : Int = pos;
    def end : Int = pos + ref.length;
    
    def is_BND : Boolean = info.getOrElse("SVTYPE",None).map{ x => x == "BND" }.getOrElse(false)
    
    //def sv_bpIsBefore : Seq[Boolean] = 
    
    /*def sv_breakEndPoint : Seq[(String,Int)] = if(is_BND){
      alt.map{ aa => {
        
      }}
    } else { Seq() }*/
    
    def getVcfString : String = chrom + "\t"+pos+"\t"+id+"\t"+ref+"\t"+alt.mkString(",")+"\t"+
                                qual+"\t"+filter+"\t"+info.keySet.toSeq.sorted.map{ case t => {
                                  val v = info(t);
                                  v match {
                                    case Some(sv) => t + "="+sv
                                    case None => t
                                  }
                                }}.mkString(";")+(
                                  if(format.length > 0){
                                    "\t"+format.mkString(":")+"\t"+
                                    genotypes.getGenotypeStrings.mkString("\t")
                                  } else {
                                    ""
                                  }
                                );
    //def header : SVcfHeader;
    //def getSampleList : Seq[String];
    def getVcfStringNoGenotypes : String = chrom + "\t"+pos+"\t"+id+"\t"+ref+"\t"+alt.mkString(",")+"\t"+
                                qual+"\t"+filter+"\t"+info.keySet.toSeq.sorted.map{ case t => {
                                  val v = info(t);
                                  v match {
                                    case Some(sv) => t + "="+sv
                                    case None => t
                                  }
                                }}.mkString(";");
    def getVcfStringGenoOpt( withGenotypes : Boolean = true ) = if(withGenotypes) getVcfString else getVcfStringNoGenotypes
    
    def getTableString( vcfHeader : SVcfHeader ) : String = {
      chrom + "\t"+pos+"\t"+id+"\t"+ref+"\t"+alt.mkString(",")+"\t"+vcfHeader.infoLines.map{ infoLine => {
        info.get(infoLine.ID).getOrElse(None).getOrElse(".")
      }}.mkString("\t")
    }

    def getOutputLine() : SVcfOutputVariantLine = {
      SVcfOutputVariantLine(
       in_chrom = chrom,
       in_pos = pos,
       in_id = id,
       in_ref = ref,
       in_alt = alt,
       in_qual = qual,
       in_filter = filter, 
       in_info = info,
       in_format = format,
       in_genotypes = genotypes//,
       //in_header = header
      )
    }
    def getOutputLineCopy() : SVcfOutputVariantLine = {
      SVcfOutputVariantLine(
       in_chrom = chrom,
       in_pos = pos,
       in_id = id,
       in_ref = ref,
       in_alt = alt,
       in_qual = qual,
       in_filter = filter, 
       in_info = info,
       in_format = format ,
       in_genotypes = genotypes.copyGenotypeSet()
      )
    }
    def getLazyOutputLine() : SVcfOutputVariantLine = getOutputLine()
    
    def isOutputLine : Boolean = false;
    def getSimpleVcfString() : String = chrom + "\t"+pos+"\t"+id+"\t"+ref+"\t"+alt.mkString(",")+"\t"+qual+"\t"+filter;
    
    def getVcfTable(includeVariantTags : List[String] = List[String](),delim : String = "\t", missing : String = "NA") : String = {
      Seq(chrom,pos,id,ref,alt.filter(_ != "*").mkString(","),qual,filter).mkString(delim) + delim +
        includeVariantTags.map{tag => {
          info.getOrElse(tag,Some(missing)).getOrElse(missing);
        }}.mkString(delim);
    }
    
    def getGt(gtTag : String) : Array[String] = {
      error("Op not supported!");
      null;
    }
    
    def alleles : Seq[String] = ref +: alt;
    
    def altClean = alt.zipWithIndex.filter{ case (alt,altIdx) => alt != UNKNOWN_ALT_TAG_STRING }
    
    def getInfo(tag : String) : String = {
      this.info.getOrElse(tag,None) match {
        case Some(v) => {
          v
        }
        case None => "."
      }
    }
    def getInfoList(tag : String, delim : String = ",") : Vector[String] = {
      this.info.getOrElse(tag,None) match {
        case Some(v) => {
          if(v == "."){
            Vector[String]();
          } else {
            v.split(delim).toVector
          }
        }
        case None => Vector[String]();
      }
    }
    def getInfoArray(tag : String, delim : String = ",", subDelim : String = "\\|") : Vector[Vector[String]] = {
      this.info.getOrElse(tag,None) match {
        case Some(v) => {
          if(v == "."){
            Vector[Vector[String]]();
          } else {
            v.split(delim).toVector.map{ ss => {
              if(ss == "."){
                Vector[String]();
              } else {
                ss.split(subDelim).toVector;
              }
            }}
          }
        }
        case None => Vector[Vector[String]]();
      }
    }
    
    def makeSnpeffVariantEntry(dummyIter : org.snpeff.fileIterator.VcfFileIterator) : org.snpeff.vcf.VcfEntry = {
      val out = new org.snpeff.vcf.VcfEntry( dummyIter,  this.chrom+"\t"+this.pos+"\t.\t"+this.ref+"\t"+this.alt.head+"\t20\t.\tTESTSNPEFFDUMMYVAR=BLAH;", 1, true);
      out;
    }
    
    def makeGatkVariantContext() : htsjdk.variant.variantcontext.VariantContext = {
      internalUtils.GatkPublicCopy.makeSimpleVariantContext(this);
    }
  }
  
  case class SVcfInputTableVariantLine(inputLine : String, headerColumns : Seq[String]) extends SVcfVariantLine {
    lazy val cells : Array[String] = inputLine.split("\t");
    lazy val lzy_chrom : String = cells(0);
    lazy val lzy_pos : Int = string2int(cells(1));
    lazy val lzy_id : String = cells(2);
    lazy val lzy_ref : String = cells(3);
    lazy val lzy_alt : Array[String] = cells(4).split(",");
    lazy val lzy_qual : String  = if(headerColumns.contains("QUAL")){
      cells( headerColumns.indexOf("QUAL") )
    } else {
      "0"
    }
    lazy val lzy_filter : String = if(headerColumns.contains("FILTER")){
      cells( headerColumns.indexOf("FILTER") )
    } else {
      "0"
    }
    lazy val lzy_info : Map[String,Option[String]] = headerColumns.zip(cells.drop(5)).map{ case (colName,colVal) => {
      if(colVal == ""){
        (colName,Some("."))
      } else {
        (colName,Some(colVal))
      }
    }}.toMap
    
    def chrom = lzy_chrom;
    def pos = lzy_pos;
    def id = lzy_id;
    def ref = lzy_ref;
    def alt = lzy_alt;
    def qual = lzy_qual;
    def filter = lzy_filter;
    def info = lzy_info;
    def format = Array[String]()
    lazy val lzy_genotypeStrings = Array[String]()
    lazy val lzy_genotypes = SVcfGenotypeSet.getGenotypeSet(lzy_genotypeStrings, format);
    def genotypes = lzy_genotypes;
    
    private var header : SVcfHeader = null;
    def setHeader( h : SVcfHeader ){
      header = h;
    }
    def getHeader() : SVcfHeader = {
      header;
    }
    
  }
  case class SVcfInputTableVariantLineNoID(inputLine : String, headerColumns : Seq[String]) extends SVcfVariantLine {
    lazy val cells : Array[String] = inputLine.split("\t");
    lazy val lzy_chrom : String = cells(0);
    lazy val lzy_pos : Int = string2int(cells(1));
    lazy val lzy_id : String = "."
    lazy val lzy_ref : String = cells(2);
    lazy val lzy_alt : Array[String] = cells(3).split(",");
    lazy val lzy_qual : String  = if(headerColumns.contains("QUAL")){
      cells( headerColumns.indexOf("QUAL") )
    } else {
      "0"
    }
    lazy val lzy_filter : String = if(headerColumns.contains("FILTER")){
      cells( headerColumns.indexOf("FILTER") )
    } else {
      "0"
    }
    lazy val lzy_info : Map[String,Option[String]] = headerColumns.zip(cells.drop(4)).map{ case (colName,colVal) => {
      if(colVal == ""){
        (colName,Some("."))
      } else {
        (colName,Some(colVal))
      }
    }}.toMap
    
    def chrom = lzy_chrom;
    def pos = lzy_pos;
    def id = lzy_id;
    def ref = lzy_ref;
    def alt = lzy_alt;
    def qual = lzy_qual;
    def filter = lzy_filter;
    def info = lzy_info;
    def format = Array[String]()
    lazy val lzy_genotypeStrings = Array[String]()
    lazy val lzy_genotypes = SVcfGenotypeSet.getGenotypeSet(lzy_genotypeStrings, format);
    def genotypes = lzy_genotypes;
    
    private var header : SVcfHeader = null;
    def setHeader( h : SVcfHeader ){
      header = h;
    }
    def getHeader() : SVcfHeader = {
      header;
    }
    
  }
  
  
  case class SVcfInputVariantLine(inputLine : String) extends SVcfVariantLine {
    override def getVcfString :  String = inputLine;
    
    lazy val cells : Array[String] = inputLine.split("\t");
    
    lazy val lzy_chrom : String = cells(0);
    lazy val lzy_pos : Int = string2int(cells(1));
    lazy val lzy_id : String = cells(2);
    lazy val lzy_ref : String = cells(3);
    lazy val lzy_alt : Array[String] = cells(4).split(",");
    lazy val lzy_qual : String  = cells(5);
    lazy val lzy_filter : String = cells(6);
    lazy val lzy_info : Map[String,Option[String]] = if(cells.isDefinedAt(7)){ 
      if(cells(7) == "" || cells(7) == "." || cells(7) == ".;" || cells(7) == ";"){
        scala.collection.immutable.Map[String,Option[String]]()
      } else {
        cells(7).split(";").map(s => {
          val c = s.split("=",2);
          if(c.length == 1){
            (c(0),None)
          } else {
            (c(0),Some(c(1)));
          }
        }).toMap
      }
    } else {
      warning("Vcf file has no info column. Setting info to empty.","VCF_MALFORMAT_NO_INFO",10);
      Map[String,Option[String]]();
    }
    lazy val lzy_format = if(cells.isDefinedAt(8)){
      if(cells(8) == "." || cells(8) == ""){
        Array[String]();
      } else {
        cells(8).split(":") 
      }
    } else Array[String]();
    lazy val lzy_genotypeStrings = if(cells.isDefinedAt(9)) cells.drop(9) else Array[String]();
    
    def chrom = lzy_chrom;
    def pos = lzy_pos;
    def id = lzy_id;
    def ref = lzy_ref;
    def alt = lzy_alt;
    def qual = lzy_qual;
    def filter = lzy_filter;
    def info = lzy_info;
    def format = lzy_format;
    //def genotypeStrings = lzy_genotypeStrings;
    lazy val lzy_genotypes = SVcfGenotypeSet.getGenotypeSet(lzy_genotypeStrings, format);
    def genotypes = lzy_genotypes;

    override def getGt(gtTag : String) : Array[String] = {
      val gtIdx : Int = format.indexOf(gtTag);
      if(gtIdx == -1) error("GT tag not found!");
      lzy_genotypeStrings.map( gts => (gts.split(":",-1).padTo(gtIdx+1,".")).apply(gtIdx))
    }
    
    private var header : SVcfHeader = null;
    def setHeader( h : SVcfHeader ){
      header = h;
    }
    def getHeader() : SVcfHeader = {
      header;
    }
    def headerInput( h : SVcfHeader ) : SVcfInputVariantLine = {
      this.setHeader(h);
      return this;
    }
    //def getSampleList : Seq[String] = in_header.titleLine.sampleList;
    //def header = in_header;
  }
  
  case class SVcfOutputVariantLine(
      var in_chrom : String,
      var in_pos : Int,
      var in_id : String,
      var in_ref : String,
      var in_alt : Seq[String],
      var in_qual : String,
      var in_filter : String,
      var in_info : Map[String,Option[String]],
      in_format : Seq[String],
      var in_genotypes : SVcfGenotypeSet//,
      //var in_header : SVcfHeader
      ) extends SVcfVariantLine {
    def chrom = in_chrom
    def pos = in_pos
    def id = in_id
    def ref = in_ref
    def alt = in_alt
    def qual = in_qual
    def filter = in_filter
    def info = in_info
    def genotypes = in_genotypes
    def format = genotypes.fmt
    //def header = in_header;
    
    def addInfo(infoTag : String, infoVal : String){
      in_info = in_info + ((infoTag,Some(infoVal)));
    }
    def addInfoOpt(infoTag : String, infoVal : Option[String]){
      in_info = in_info + ((infoTag,infoVal));
    }
    def dropInfo(tags : Set[String]){
      in_info = in_info -- tags
    }
    override def isOutputLine : Boolean = true;
    override def getLazyOutputLine : SVcfOutputVariantLine = this;
    
    def removeGenotypeInfo(){
      in_genotypes = SVcfGenotypeSet.getGenotypeSet(genotypeStrings = Seq[String](), fmt = Seq[String]());
    }
    /*
       def makeSimpleVariantContext(vc : SVcfVariantLine) : VariantContext = {
    val alleles = (Seq[Allele](Allele.create( vc.ref, true) , Allele.create( vc.alt.head, false) )).asJava
    var vm =  ((new VariantContextBuilder()).chr(vc.chrom).start( vc.pos ).alleles(alleles))
    vm = vm.computeEndFromAlleles(alleles, vc.pos).noID().unfiltered()
    
    vm.make();
  }
     */
    private var header : SVcfHeader = null;
    def setHeader( h : SVcfHeader ){
      header = h;
    }
    def getHeader() : SVcfHeader = {
      header;
    }
    def headerOutput(h : SVcfHeader) : SVcfOutputVariantLine = {
      this.setHeader(h);
      return this;
    }

  }
  
  object SVcfGenotypeSet {
    def getGenotypeSet(genotypeStrings : Seq[String], fmt : Seq[String]) : SVcfGenotypeSet = {
      val out = Array.ofDim[String](fmt.length,genotypeStrings.length)
      //val fmtLines = fmt.map(f => {
      //  header.formatLines.find(_.ID == f).get;
      //});
      
      val cells = genotypeStrings.map(_.split(":",-1).padTo(fmt.length,"."));
      
      try{
        cells.indices.foreach(i => {
          (0 until fmt.length).foreach(j => {
            out(j)(i) = cells(i)(j);
          })
        })
      } catch {
        case e : Exception => {
          warning("Error attempting to decode genotypes:\n" + 
                "   FMT: "+fmt.mkString(",") + "\n"+
                "   GENOS: "+genotypeStrings.mkString("\t")+"\n",
                "GENOTYPE_DECODER_ERROR",100
                );
          throw e;
        }
      }
      
      SVcfGenotypeSet(fmt = fmt, genotypeValues = out);
    }
  }
  
  case class SVcfGenotypeSet(var fmt : Seq[String],
                             var genotypeValues : Array[Array[String]]){
    
    var sampList : List[String] = List();
    var sampGrp : Option[scala.collection.mutable.AnyRefMap[String,Set[String]]] = None
    def setSampList( samps : List[String]){
      sampList = samps;
    }
    def copyGenotypeSet() : SVcfGenotypeSet = {
      val out = SVcfGenotypeSet(fmt=fmt,genotypeValues=genotypeValues)
      out.sampList = out.sampList;
      out.sampGrp = out.sampGrp;
      out;
    }
    def deepCopyGenotypeSet() : SVcfGenotypeSet = {
      val out = SVcfGenotypeSet(fmt=fmt,genotypeValues= genotypeValues.toVector.map{ gg => gg.toVector.toArray }.toArray )
      out.sampList = out.sampList;
      out.sampGrp = out.sampGrp;
      out;
    }
    
    def idxIsGrp(idx : Int, grp : String) : Boolean = {
      if(sampList.length == 0){
        false 
      } else {
        sampGrp.map{ grpMap => {
          grpMap(sampList(idx)).contains(grp);
        }}.getOrElse(false);
      }
    }
    
    def getGenotypeStrings : Seq[String] = if(genotypeValues.isDefinedAt(0)) genotypeValues(0).indices.map(j => {
      genotypeValues.indices.map(i => {
        genotypeValues(i)(j)
      }).mkString(":")
    }).toSeq else Seq[String]();
    
    def getGtTag(tag : String) : Option[Array[String]] = {
      val idx = fmt.indexOf(tag);
      if(idx == -1){
        None
      } else {
        Some(genotypeValues(idx));
      }
    }
    
    def addGenotypeArray(fmtid : String, gval : Array[String]) : Int = {
      val idx = fmt.indexWhere((p : String) => { p == fmtid });
      if(idx != -1){
        genotypeValues(idx) = gval;
        return idx;
      } else {
        fmt = fmt :+ fmtid
        genotypeValues = genotypeValues :+ gval;
        return genotypeValues.length - 1;
      }
    }
    def getPloidy() : Int = genotypeValues(0).map{ (g : String) => {g.split("/").length}}.max;
    
    def dropGenotypeArray(fmtid : String) : Boolean = {
      val idx = fmt.indexWhere((p : String) => { p == fmtid });
      if(idx != -1){
        false
      } else if(idx == 0){
        //GT field is mandatory!
        genotypeValues = genotypeValues.updated(0,Array.fill[String](genotypeValues.head.length)("."))
        true
      } else {
        fmt = fmt.drop(idx)
        genotypeValues = genotypeValues.drop(idx)
        true
      }
    }
    
    def addGenotypeArrayIfNew(fmtid : String, gval : Array[String]) : Int = {
      val idx = fmt.indexWhere((p : String) => { p == fmtid });
      if(idx != -1){
        return idx;
      } else {
        fmt = fmt :+ fmtid
        genotypeValues = genotypeValues :+ gval;
        return genotypeValues.length - 1;
      }
    }
    
    def addGenotypeValue(fmtid : String,sampIdx : Int, gval : String) : Int = {
      val idx = fmt.indexWhere((p : String) => { p == fmtid });
      if(idx == -1){
        fmt = fmt :+ fmtid
        genotypeValues = genotypeValues :+ Array.fill[String](genotypeValues.head.length)(".");
      } else {
        genotypeValues(idx)(sampIdx) = gval;
      }
      return idx;
    }
    /*
    case class SVcfEmptyGenotypeSet() extends SVcfGenotypeSet(fmt = Seq[String](), genotypeValues = Array[Array[String]]()){
      override def getGenotypeStrings : Seq[String] = Seq[String]();
      override def addGenotypeArray(fmtid : String, gval : Array[String]) : Int = {
        error("ERROR: CANNOT ADD GENOTYPE DATA TO EMPTY GENOTYPE SET!")
        -1
      }
      override def getPloidy() : Int = {
        error("ERROR: CANNOT ADD GENOTYPE DATA TO EMPTY GENOTYPE SET!")
        -1
      }
      override def addGenotypeArrayIfNew(fmtid : String, gval : Array[String]) : Int = {
        error("ERROR: CANNOT ADD GENOTYPE DATA TO EMPTY GENOTYPE SET!")
        -1;
      }
    }*/
    
  }
  

  
  def getSVcfIterator(infile : String, chromList : Option[List[String]],numLinesRead : Option[Int]) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      val (vcfHeader,vcIter) = if(chromList.isEmpty){
        SVcfLine.readVcf(getLinesSmartUnzip(infile),withProgress = true)
      } else if(chromList.get.length == 1){
        val chrom = chromList.get.head;
        SVcfLine.readVcf(getLinesSmartUnzip(infile).filter{line => {
          line.startsWith(chrom+"\t") || line.startsWith("#")
        }},withProgress = true)
      } else {
        val chromSet = chromList.get.toSet;
        val (vh,vi) = SVcfLine.readVcf(getLinesSmartUnzip(infile),withProgress = true)
        (vh,vi.filter(line => { chromSet.contains(line.chrom) }))
      }
      
      val vcIter2 = if(numLinesRead.isDefined){
        vcIter.take(numLinesRead.get);
      } else {
        vcIter
      }
      (vcIter2,vcfHeader);
  }
  def getSVcfIterators(infileString : String, chromList : Option[List[String]],
      numLinesRead : Option[Int], inputFileList : Boolean = false, withProgress : Boolean = true, 
      infixes : Vector[String] = Vector(),
      extractInterval : Option[String] = None) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      reportln("     getSVcfIterators() init... ("+getDateAndTimeString+")","debug")
      val indata = if(inputFileList){
        val (infilePeek,infiles) = peekIterator(getLinesSmartUnzip(infileString),1000);
        val denominator = if(infilePeek.length < 1000) infilePeek.length.toString else "???";
        val headerLines = extractWhile(getLinesSmartUnzip(infilePeek.head).buffered)( a => a.startsWith("#") )
        val allInputLines = flattenIterators(infiles.zipWithIndex.map{case (inf,idx) => addIteratorCloseAction(iter =getLinesSmartUnzip(inf), closeAction = (() => {reportln("finished reading file: "+inf + "("+getDateAndTimeString+")" + "("+(idx+1)+"/"+denominator+")","note")}))}).buffered
        //val headerLines = extractWhile(allInputLines)( a => a.startsWith("#"));
        val remainderLines = allInputLines.filter( a => ! a.startsWith("#"));
        headerLines.iterator ++ remainderLines;
      } else if(infileString.contains(',')){
        val infiles = infileString.split(",");
        val denominator = infiles.length.toString;
        val headerLines = extractWhile(getLinesSmartUnzip(infiles.head).buffered)( a => a.startsWith("#") )
        val allInputLines = flattenIterators(infiles.iterator.zipWithIndex.map{case (inf,idx) => addIteratorCloseAction(iter =getLinesSmartUnzip(inf), closeAction = (() => {reportln("finished reading file: "+inf + "("+getDateAndTimeString+")"+  "("+(idx+1)+"/"+denominator+")","note")}))}).buffered
        //val headerLines = extractWhile(allInputLines)( a => a.startsWith("#"));
        val remainderLines = allInputLines.filter( a => ! a.startsWith("#"));
        headerLines.iterator ++ remainderLines;
      } else {
        getLinesSmartUnzip(infileString,allowStdin=true)
      }
      reportln("     getSVcfIterators() input file open... ("+getDateAndTimeString+")","debug")

    
    val (vcfHeader,vcIter) = if(chromList.isEmpty){
        reportln("     getSVcfIterators() input file open: reading simple VCF... ("+getDateAndTimeString+")","debug")
        SVcfLine.readVcf(indata,withProgress = withProgress)
      } else if(chromList.get.length == 1){
        reportln("     getSVcfIterators() input file open: reading VCF + extracting single chromosome... ("+getDateAndTimeString+")","debug")
        val chrom = chromList.get.head;
        SVcfLine.readVcf(indata.filter{line => {
          line.startsWith(chrom+"\t") || line.startsWith("#")
        }},withProgress = withProgress)
      } else {
        reportln("     getSVcfIterators() input file open: reading VCF + extracting multiple chromosomes... ("+getDateAndTimeString+")","debug")
        val chromSet = chromList.get.toSet;
        val (vh,vi) = SVcfLine.readVcf(indata,withProgress = withProgress)
        (vh,vi.filter(line => { chromSet.contains(line.chrom) }))
      }
      reportln("     getSVcfIterators() vcIter initialized... ("+getDateAndTimeString+")","debug")
      val vcIter1p5 = extractInterval.map{ ivString => {
        reportln("     getSVcfIterators() extracting interval: \""+ivString+"\"... ("+getDateAndTimeString+")","debug")
        val cells = ivString.split("[:-]");
        val chrom = cells(0);
        val start = string2int(cells(1));
        val end = string2int(cells(2));
        //(chrom,start,end);
        new Iterator[SVcfVariantLine]{
          val itr = vcIter.buffered;
          var hasHitInterval = false;
          while( itr.hasNext && itr.head.chrom == chrom && itr.head.pos < start){
            itr.next;
          }
          if(itr.hasNext){
            reportln("Arrived at interval: \""+ivString+"\": "+itr.head.chrom+":"+itr.head.pos,"note");
          } else {
            reportln("VCF does not contain interval: \""+ivString+"\"!","note");
          }
          
          def hasNext : Boolean = {
            itr.hasNext && itr.head.chrom == chrom && itr.head.pos < end;
          }
          def next : SVcfVariantLine = {
            itr.next;
          }
        }
      }}.getOrElse(vcIter);
      reportln("     getSVcfIterators() vcIter1p5 initialized... ("+getDateAndTimeString+")","debug")
      val vcIter2 = if(numLinesRead.isDefined){
        vcIter1p5.take(numLinesRead.get);
      } else {
        vcIter1p5
      }
      reportln("     getSVcfIterators() done ... ("+getDateAndTimeString+")","debug")
      (vcIter2,vcfHeader);
  }
  
  
  def getSVcfIteratorsFromTable(infileString : String, chromList : Option[List[String]],numLinesRead : Option[Int], 
      inputFileList : Boolean = false, withProgress : Boolean = true, infixes : Vector[String] = Vector(),
      extractInterval : Option[String] = None, hasIDcol : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      val indata = if(inputFileList){
        val (infilePeek,infiles) = peekIterator(getLinesSmartUnzip(infileString),1000);
        val denominator = if(infilePeek.length < 1000) infilePeek.length.toString else "???";
        val headerLines = extractWhile(getLinesSmartUnzip(infilePeek.head).buffered)( a => a.startsWith("#") )
        val allInputLines = flattenIterators(infiles.zipWithIndex.map{case (inf,idx) => addIteratorCloseAction(iter =getLinesSmartUnzip(inf), closeAction = (() => {reportln("finished reading file: "+inf + "("+getDateAndTimeString+")" + "("+(idx+1)+"/"+denominator+")","note")}))}).buffered
        //val headerLines = extractWhile(allInputLines)( a => a.startsWith("#"));
        val remainderLines = allInputLines.filter( a => ! a.startsWith("#"));
        headerLines.iterator ++ remainderLines;
      } else if(infileString.contains(',')){
        val infiles = infileString.split(",");
        val denominator = infiles.length.toString;
        val headerLines = extractWhile(getLinesSmartUnzip(infiles.head).buffered)( a => a.startsWith("#") )
        val allInputLines = flattenIterators(infiles.iterator.zipWithIndex.map{case (inf,idx) => addIteratorCloseAction(iter =getLinesSmartUnzip(inf), closeAction = (() => {reportln("finished reading file: "+inf + "("+getDateAndTimeString+")"+  "("+(idx+1)+"/"+denominator+")","note")}))}).buffered
        //val headerLines = extractWhile(allInputLines)( a => a.startsWith("#"));
        val remainderLines = allInputLines.filter( a => ! a.startsWith("#"));
        headerLines.iterator ++ remainderLines;
      } else {
        getLinesSmartUnzip(infileString,allowStdin=true)
      }
    
    val (vcfHeader,vcIter) = if(chromList.isEmpty){
        SVcfLine.readTableToVcfAdv(indata,withProgress = withProgress,hasIDcol=hasIDcol)
      } else if(chromList.get.length == 1){
        val chrom = chromList.get.head;
        SVcfLine.readTableToVcfAdv(indata.filter{line => {
          line.startsWith(chrom+"\t") || line.startsWith("#")
        }},withProgress = withProgress,hasIDcol=hasIDcol)
      } else {
        val chromSet = chromList.get.toSet;
        val (vh,vi) = SVcfLine.readTableToVcfAdv(indata,withProgress = withProgress,hasIDcol=hasIDcol)
        (vh,vi.filter(line => { chromSet.contains(line.chrom) }))
      }
      
      val vcIter1p5 = extractInterval.map{ ivString => {
        val cells = ivString.split("[:-]");
        val chrom = cells(0);
        val start = string2int(cells(1));
        val end = string2int(cells(2));
        //(chrom,start,end);
        new Iterator[SVcfVariantLine]{
          val itr = vcIter.buffered;
          var hasHitInterval = false;
          while( itr.hasNext && itr.head.chrom == chrom && itr.head.pos < start){
            itr.next;
          }
          if(itr.hasNext){
            reportln("Arrived at interval: \""+ivString+"\": "+itr.head.chrom+":"+itr.head.pos,"note");
          } else {
            reportln("VCF does not contain interval: \""+ivString+"\"!","note");
          }
          
          def hasNext : Boolean = {
            itr.hasNext && itr.head.chrom == chrom && itr.head.pos < end;
          }
          def next : SVcfVariantLine = {
            itr.next;
          }
        }
      }}.getOrElse(vcIter);
    
      val vcIter2 = if(numLinesRead.isDefined){
        vcIter1p5.take(numLinesRead.get);
      } else {
        vcIter1p5
      }
      (vcIter2,vcfHeader);
  }
  

  object chainSVcfWalkersDEFAULT extends SVcfWalkerInfo {
    def walkerName : String = "ChainedWalker"
  }
  
  
  def chainSVcfWalkersDEFAULT(walkers : Seq[SVcfWalker]) : SVcfWalker = {
    new SVcfWalker {
      def walkerName : String = "ChainedWalker"
      def walkerInfo : SVcfWalkerInfo = chainSVcfWalkersDEFAULT;
      def walkerParams : Seq[(String,String)] = Seq[(String,String)](("walkers","\""+walkers.map{ w => {
        w.walkerName
      }}.mkString(",")+"\""))
      reportln("   Chaining SVcfWalkers ...","debug");
      walkers.foreach{ w => {
        reportln("      Chained:" + w.walkerName,"debug");
      }}
      def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
        walkers.foldLeft((vcIter,vcfHeader)){ case ((oldIter,oldHeader),w) => {
          w.walkVCF(oldIter,oldHeader);
        }}
      }
    }
  }
  
  def chainSVcfWalkers(walkers : Seq[SVcfWalker]) : SVcfWalker = chainSVcfWalkersDEFAULT(walkers)
  
  
  object chainSVcfWalkersBenchmark extends SVcfWalkerInfo {
    def walkerName : String = "ChainedWalkerBenchMarked"
  }
  def chainSVcfWalkersBenchmark(walkers : Seq[SVcfWalker]) : SVcfWalker = {
    new SVcfWalker {
      def walkerName : String = "ChainedWalkerBenchMarked"
      def walkerInfo : SVcfWalkerInfo = chainSVcfWalkersBenchmark;
      def walkerParams : Seq[(String,String)] = Seq[(String,String)](("walkers","\""+walkers.map{ w => {
        w.walkerName
      }}.mkString(",")+"\""))
      walkers.foreach{ w => {
        reportln("Chained walker: " + w.walkerName,"debug");
      }}
      def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
        val (outWalker, outHeader) = walkers.foldLeft((vcIter,vcfHeader)){ case ((oldIter,oldHeader),w) => {
          val (newIter,newHeader) = w.walkVCF(oldIter,oldHeader);
          (new Iterator[SVcfVariantLine]{
            var iterCt = 0;
            def hasNext : Boolean = newIter.hasNext
            def next : SVcfVariantLine = {
              //reportln("   walker: "+w.walkerName+" ("+iterCt+") "+getDateAndTimeString,"debug");
              iterCt = iterCt + 1;
              newIter.next;
            }
          },newHeader)
        }}
        (outWalker, outHeader)
      }
    }
  }
  
  object chainSVcfWalkersDeepDebug extends SVcfWalkerInfo {
    def walkerName : String = "ChainedWalkerDebug"
  }
  
  def chainSVcfWalkersDeepDebug(walkers : Seq[SVcfWalker]) : SVcfWalker = {
    new SVcfWalker {
      def walkerName : String = "ChainedWalkerDebug"
      def walkerInfo : SVcfWalkerInfo = chainSVcfWalkersBenchmark
      def walkerParams : Seq[(String,String)] = Seq[(String,String)](("walkers","\""+walkers.map{ w => {
        w.walkerName
      }}.mkString(",")+"\""))
      walkers.foreach{ w => {
        reportln("Chained walker: " + w.walkerName,"debug");
      }}
      def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
        walkers.foldLeft((vcIter,vcfHeader)){ case ((oldIter,oldHeader),w) => {
          val (newIter,newHeader) = w.walkVCF(oldIter,oldHeader);
          (new Iterator[SVcfVariantLine]{
            var iterCt = 0;
            def hasNext : Boolean = newIter.hasNext
            def next : SVcfVariantLine = {
              reportln("   walker: "+w.walkerName+" ("+iterCt+") "+getDateAndTimeString,"debug");
              iterCt = iterCt + 1;
              newIter.next;
            }
          },newHeader)
        }}
      }
    }
  }
  
  trait DualFormatVcfWalker extends SVcfWalker {
    def oldWalkVCF(vcIter : Iterator[VariantContext], vcfHeader : VCFHeader, verbose : Boolean = true) : (Iterator[VariantContext],VCFHeader);
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader)
    def getOldVcfWalker : VCFWalker = new VCFWalker {
      def walkVCF(vcIter : Iterator[VariantContext], vcfHeader : VCFHeader, verbose : Boolean = true) : (Iterator[VariantContext],VCFHeader) = {
        oldWalkVCF(vcIter =vcIter, vcfHeader =vcfHeader, verbose = verbose)
      }
    }
  }
  
  class SVcfWalkerState extends AnyRef {
      
  }
  
  //object SVcfWalker {
  //  def headerLines : Seq[SVcfHeaderLine]
  //}
  
  trait SVcfWalkerInfo {
    def walkerName : String;
    
    def infoLines : Seq[SVcfCompoundHeaderLine] = Seq()
    def formatLines : Seq[SVcfCompoundHeaderLine] = Seq()
    def otherHeaderLines : Seq[SVcfHeaderLine] = Seq()
    def walkLines : Seq[SVcfWalkHeaderLine] = Seq()
  }
  
  trait SVcfWalker {
    def walkerName : String;
    def walkerParams : Seq[(String,String)];
    //def walkerInfo : SVcfWalkerInfo;
    
    var walkerNumber : Option[String] = None;
    var walkerTitle : String = "";
    
    def fmtOption[A](a : Option[A]) : String = a match {
      case Some(x) => ""+x.toString+"";
      case None => "None"
    }
    def fmtOptionList[A](a : Option[scala.collection.GenTraversableOnce[A]], delim : String = ",") : String = a match {
      case Some(x) => ""+a.mkString(delim)+""
      case None => "None"
    }
    def fmtList[A](a : scala.collection.GenTraversableOnce[A], delim : String = ",") : String =  ""+a.toSeq.padTo(1,"None").mkString(delim)+"";
    
    //def initVCFWalker(vcfHeader : SVcfHeader) : (SVcfHeader)
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader)
    def walkVCFFile(infile :String, outfile : String, chromList : Option[List[String]], numLinesRead : Option[Int] = None, dropGenotypes : Boolean = false){
      val (vcIter2, vcfHeader) = getSVcfIterator(infile,chromList,numLinesRead);
      
      val (newIter,newHeader) = walkVCF(vcIter2,vcfHeader);
      
      val writer = openWriterSmart(outfile);
      newHeader.getVcfLines.foreach{line => {
        writer.write(line+"\n");
      }}
      if(dropGenotypes){
        newIter.foreach{ line => {
          writer.write(line.getVcfStringNoGenotypes+"\n");
        }}
      } else {
        newIter.foreach{ line => {
          writer.write(line.getVcfString+"\n");
        }}
      }
      writer.close();
    }
    
    //
    
    def walkVCFFiles(infiles : String, outfile : String, chromList : Option[List[String]], numLinesRead : Option[Int], inputFileList : Boolean, 
                    dropGenotypes : Boolean = false, infixes : Vector[String] = Vector(),
                    splitFuncOpt : Option[(String,Int) => Option[String]] = None){
      val (vcIterRaw, vcfHeader) = getSVcfIterators(infiles,chromList=chromList,numLinesRead=numLinesRead,inputFileList = inputFileList, infixes = infixes);
      reportln("SVcfIterators Initialized... ("+getDateAndTimeString+")","debug")
      //val (newIter,newHeader) = walkVCF(vcIterRaw,vcfHeader);
      walkToFileSplit(outfile=outfile, vcIter = vcIterRaw, vcfHeader = vcfHeader, dropGenotypes = dropGenotypes, splitFuncOpt = splitFuncOpt);
      
      /*val writer = openWriterSmart(outfile,true);
      newHeader.getVcfLines.foreach{line => {
        writer.write(line+"\n");
      }}
      if(dropGenotypes){
        newIter.foreach{ line => {
          writer.write(line.getVcfStringNoGenotypes+"\n");
        }}
      } else {
        newIter.foreach{ line => {
          writer.write(line.getVcfString+"\n");
        }}
      }
      writer.close();*/
    }
    
    def walkToFile(outfile : String, vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader, dropGenotypes : Boolean =false){
      val (newIter,newHeader) = walkVCF(vcIter,vcfHeader);
      writeToFile(outfile=outfile,vcIter=newIter,vcfHeader=newHeader,dropGenotypes=dropGenotypes);
    }
    def walkToFileSplit(outfile : String, vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader, dropGenotypes : Boolean =false, splitFuncOpt : Option[(String,Int) => Option[String]] = None){
      val (newIter,newHeader) = walkVCF(vcIter,vcfHeader);
      reportln("    walkVCF() ("+getDateAndTimeString+")","debug")
      writeToFileSplit(outfile=outfile,vcIter=newIter,vcfHeader=newHeader,dropGenotypes=dropGenotypes, splitFuncOpt=splitFuncOpt);
    }
    

    def writeToTableFile(outfile : String, vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader){
      val writer = openWriterSmart(outfile,true);
      vcfHeader.getVcfLines.foreach{line => {
        writer.write(line+"\n");
      }}
      writer.write("#CHROM\tPOS\tID\tREF\tALT\t"+vcfHeader.infoLines.map{ vv => vv.ID}+"\n")
        vcIter.foreach{ line => {
          writer.write(line.getTableString(vcfHeader)+"\n");
        }}
      writer.close();
    }
    
    def writeToFile(outfile : String, vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader, dropGenotypes : Boolean =false){
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
    }

    
    def writeToFileSplit(outfile : String, vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader, dropGenotypes : Boolean =false, splitFuncOpt : Option[(String,Int) => Option[String]] = None){
      splitFuncOpt match {
        case None => {
          reportln("No output file split. Outputting to file: "+outfile,"note")
          writeToFile(outfile=outfile,vcIter = vcIter, vcfHeader = vcfHeader, dropGenotypes = dropGenotypes)
        }
        case Some(splitFunc) => {
          reportln("Splitting output files. ("+outfile+")","note")
          //val bufIter = vcIter.buffered;
          
          def splitFuncVc(vc : SVcfVariantLine) : Option[String] = {
            splitFunc(vc.chrom, vc.pos);
          }
          
          val (outPrefix,outSuffix) : (String,String) = if(outfile.split("[|]").length == 2){
            (outfile.split("[|]")(0), outfile.split("[|]")(1))
          } else {
            (outfile,".vcf.gz")
          }
          
          def outFileName(s : String) = {
            outPrefix + s + outSuffix;
          }
          var currOutFileInfix : Option[String] = None //splitFuncVc(bufIter.head);
          var currOut : Option[WriterUtil] = None
          //BUGFIX: don't walkVCF twice! fixed 2022-05-12, v3.2.87
          //val (newIter,newHeader) = walkVCF(vcIter,vcfHeader);
          
          def setOutfile(vc : SVcfVariantLine){
            val newInfix = splitFuncVc(vc);
            if(newInfix.isEmpty && currOut.isDefined){
              currOut.foreach(out => {
                reportln("Finished with file: "+outPrefix+currOutFileInfix.get+outSuffix+"." ,"progress");
                out.close()
              })
              currOut = None;
            } else if(newInfix.isDefined && ( currOutFileInfix.isEmpty || newInfix.get != currOutFileInfix.get )){
              currOut.foreach(out => {
                reportln("Finished with file: "+outPrefix+currOutFileInfix.get+outSuffix+"." ,"progress");
                out.close()
              })
              currOutFileInfix = newInfix
              reportln("Starting new file: "+outPrefix+currOutFileInfix.get+outSuffix,"progress")
              currOut = Some( openWriterSmart(outPrefix+currOutFileInfix.get+outSuffix) )
              currOut.foreach(out => {
                vcfHeader.getVcfLines.foreach{ line => {
                  out.write(line+"\n")
                }}
              })
            }
          }
          
          if(dropGenotypes){
            vcIter.foreach{ line => {
              setOutfile(line);
              currOut.foreach{ writer => {
                writer.write(line.getVcfStringNoGenotypes+"\n");
              }}
            }}
          } else {
            vcIter.foreach{ line => {
              setOutfile(line);
              currOut.foreach{ writer => {
                writer.write(line.getVcfString+"\n");
              }}
            }}
          }
          currOut.foreach(out => {
            reportln("Finished with file: "+outPrefix+currOutFileInfix+outSuffix+"." ,"progress");
            out.close()
          })
        }
      }
      

    }
    

    def writeVariantsToFileWithIndex(outfile : String, vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader, dropGenotypes : Boolean =false){
      val writer = new htsjdk.samtools.util.BlockCompressedOutputStream(outfile);
      vcfHeader.getVcfLines.foreach{line => {
        writer.write((line+"\n").getBytes());
      }}
        val htseqHeader = if(dropGenotypes){
          new htsjdk.variant.vcf.VCFHeader( vcfHeader.getVcfHeaderLines.map{ v => new htsjdk.variant.vcf.VCFHeaderLine( v.tag, v.value ) }.toSet.asJava )
        } else {
          new htsjdk.variant.vcf.VCFHeader( vcfHeader.getVcfHeaderLines.map{ v => new htsjdk.variant.vcf.VCFHeaderLine( v.tag, v.value ) }.toSet.asJava, vcfHeader.titleLine.sampleList.toSet.asJava)
        }
        val vcfcodec = new htsjdk.variant.vcf.VCFCodec()
        vcfcodec.setVCFHeader(htseqHeader,htsjdk.variant.vcf.VCFHeaderVersion.VCF4_2);
        val idx = new htsjdk.tribble.index.tabix.TabixIndexCreator(htsjdk.tribble.index.tabix.TabixFormat.VCF)
        
        vcIter.foreach{ line => {
          val pos = writer.getFilePointer();
          val lnstr = line.getVcfStringGenoOpt( ! dropGenotypes )
          val htsln = vcfcodec.decode(lnstr);
          writer.write((lnstr+"\n").getBytes());
          idx.addFeature(htsln,pos);
        }}
      writer.flush();
      writer.close();
      val index = idx.finalizeIndex(writer.getFilePointer());
      index.writeBasedOnFeatureFile( new File(outfile ) )
    }
    
    
    def walkVCFFilesSplitByChrom( infiles : String, outfilePrefix : String, chromList : Option[List[String]], numLinesRead : Option[Int], inputFileList : Boolean, dropGenotypes : Boolean = false){
      val (vcIterRaw, vcfHeader) = getSVcfIterators(infiles,chromList=chromList,numLinesRead=numLinesRead,inputFileList = inputFileList);
      val (newIterRaw,newHeader) = walkVCF(vcIterRaw,vcfHeader);
      val newIter = newIterRaw.buffered;
      var currChrom = newIter.head.chrom;
      
      def switchChrom(line : SVcfVariantLine): WriterUtil = {
        val newWriter = openWriterSmart(outfilePrefix+"."+line.chrom+".vcf.gz");
        newHeader.getVcfLines.foreach{line => {
          newWriter.write(line+"\n");
        }}
        currChrom = line.chrom;
        newWriter
      }
      var writer = switchChrom(newIter.head);

      newIter.foreach{ line => {
          if(line.chrom != currChrom){
            writer.close();
            writer = switchChrom(line);
          }
          if(dropGenotypes){
            writer.write(line.getVcfStringNoGenotypes+"\n");
          } else {
            writer.write(line.getVcfString+"\n");
          }
          
      }}
      writer.close();
    }
    
    def vcMap(iter : Iterator[SVcfVariantLine])(f : SVcfVariantLine => SVcfVariantLine) : Iterator[SVcfVariantLine] = vcMap_BENCHMARKED(iter)(f);
    def vcFlatMap(iter : Iterator[SVcfVariantLine])(f : SVcfVariantLine => scala.collection.GenTraversableOnce[SVcfVariantLine]) : Iterator[SVcfVariantLine] = vcFlatMap_BENCHMARKED(iter)(f);
    def vcGroupedFlatMap(iter : Iterator[Seq[SVcfVariantLine]])(f : Seq[SVcfVariantLine] => scala.collection.GenTraversableOnce[SVcfVariantLine]) : Iterator[SVcfVariantLine] = vcGroupedFlatMap_BENCHMARKED(iter)(f);

    val BURN_IN_CT = 200;
    var BURNING_IN = true;
    
    def addBenchmarkProgressReporting(bmi :  BenchmarkIterator[SVcfVariantLine]) : BenchmarkIterator[SVcfVariantLine] = {
      addProgressReportFunction(f = (x) => {
        if(BURNING_IN && x >= BURN_IN_CT){
          bmi.reset();
          BURNING_IN = false;
        }
        val i = bmi.iterCt;
        val ns = bmi.nanosElapsed;
        "(Walker: "+walkerName+", ["+x+"]/"+bmi.getStatusString()+"])"
      })
      bmi;
    }
    
    def vcMap_BENCHMARKED(iter : Iterator[SVcfVariantLine])(f : SVcfVariantLine => SVcfVariantLine) : Iterator[SVcfVariantLine] = {
      //benchMap[A,B](iter : Iterator[A])(f : A => B) : BenchmarkIterator[B]
      val bmi : BenchmarkIterator[SVcfVariantLine] = benchMap[SVcfVariantLine,SVcfVariantLine](iter)(f);
      addBenchmarkProgressReporting(bmi);
    }
    def vcFlatMap_BENCHMARKED(iter : Iterator[SVcfVariantLine])(f : SVcfVariantLine => scala.collection.GenTraversableOnce[SVcfVariantLine]) : Iterator[SVcfVariantLine] = {
      val bmi : BenchmarkIterator[SVcfVariantLine] = benchFlatMap[SVcfVariantLine,SVcfVariantLine](iter)(f);
      addBenchmarkProgressReporting(bmi);
    }
    def vcGroupedFlatMap_BENCHMARKED(iter : Iterator[Seq[SVcfVariantLine]])(f : Seq[SVcfVariantLine] => scala.collection.GenTraversableOnce[SVcfVariantLine]) : Iterator[SVcfVariantLine] = {
      val bmi : BenchmarkIterator[SVcfVariantLine] = benchFlatMap[Seq[SVcfVariantLine],SVcfVariantLine](iter)(f);
      addBenchmarkProgressReporting(bmi);
    }
    
    
    def vcMap_DEFAULT(iter : Iterator[SVcfVariantLine])(f : SVcfVariantLine => SVcfVariantLine) : Iterator[SVcfVariantLine] = {
      iter.map(f);
    }
    def vcFlatMap_DEFAULT(iter : Iterator[SVcfVariantLine])(f : SVcfVariantLine => scala.collection.GenTraversableOnce[SVcfVariantLine]) : Iterator[SVcfVariantLine] = {
      iter.flatMap(f);
    }
    def vcGroupedFlatMap_DEFAULT(iter : Iterator[Seq[SVcfVariantLine]])(f : Seq[SVcfVariantLine] => scala.collection.GenTraversableOnce[SVcfVariantLine]) : Iterator[SVcfVariantLine] = {
      iter.flatMap(f);
    }
    
  }
  
  
  
  
  
  def parseDebugReport(s : String){
    reportln(s,"debug");
  }
  
  abstract class SFilterLogic[A] {
    def keep(vc : A) : Boolean;
    def printTree() : String;
    def getChildren : Seq[SFilterLogic[A]]
    def hasChildren : Boolean = getChildren.length > 0;
    def getDef : Option[ FilterFunction[A] ] = None
    def getDefSeq : Seq[(FilterFunction[A],Seq[String])] = Seq();
  }
  
  case class SFilterTrue[A]() extends SFilterLogic[A] {
    def keep(vc : A) : Boolean = true;
    def printTree() : String = "[TRUE]"
    def getChildren : Seq[SFilterLogic[A]] = Seq();
  }
  
  case class SFilterByFunc[A](fun : (A => Boolean), params : Seq[String], filterName : String = "UNK", ff : FilterFunction[A]) extends SFilterLogic[A] {
    def keep(vc : A) : Boolean = fun(vc);
    def printTree() : String = "["+filterName+" " +params.mkString(" ")+"]";
    def getChildren : Seq[SFilterLogic[A]] = Seq();
    override def getDef : Option[FilterFunction[A]] = Some(ff);
    override def getDefSeq : Seq[(FilterFunction[A],Seq[String])] = Seq((ff,params));
  }
  
  case class SFilterAND[A](a1: SFilterLogic[A], a2: SFilterLogic[A]) extends SFilterLogic[A] {
    def keep(vc : A) : Boolean = {
      if(! a1.keep(vc)) false;
      else a2.keep(vc);
    }
    def printTree() : String = "["+a1.printTree()+" AND "+a2.printTree()+"]";
    def getChildren : Seq[SFilterLogic[A]] = Seq(a1,a2);
    override def getDefSeq : Seq[(FilterFunction[A],Seq[String])] = a1.getDefSeq ++ a2.getDefSeq;
  }
  case class SFilterOR[A](a1: SFilterLogic[A], a2: SFilterLogic[A]) extends SFilterLogic[A] {
    def keep(vc : A) : Boolean = {
      if(a1.keep(vc)) true;
      else a2.keep(vc);
    }
    def printTree() : String = "["+a1.printTree()+" OR "+a2.printTree()+"]";
    def getChildren : Seq[SFilterLogic[A]] = Seq(a1,a2);
    override def getDefSeq : Seq[(FilterFunction[A],Seq[String])] = a1.getDefSeq ++ a2.getDefSeq;
  }
  case class SFilterNOT[A](a1 : SFilterLogic[A]) extends SFilterLogic[A] {
    def keep(vc : A) : Boolean = {
      ! a1.keep(vc);
    }
    def printTree() : String = "[NOT: "+a1.printTree()+"]";
    def getChildren : Seq[SFilterLogic[A]] = Seq(a1);
    override def getDefSeq : Seq[(FilterFunction[A],Seq[String])] = a1.getDefSeq;
  }

  def findMatchedParenIdx(strs : Seq[String], openParen : String = "(", closeParen : String = ")") : Int = {
      if(strs.head != openParen){
        error("findMatchedParenIdx run on non-paren char!");
      }
      val (depth, idx) = strs.tail.zipWithIndex.iterator.foldLeft((1,1)){ case ((currDepth,currIdx),(currChar,idx)) => {
        if(currDepth == 0){
          (currDepth, currIdx);
        } else if(currChar == openParen){
          (currDepth + 1, currIdx + 1)
        } else if(currChar == closeParen){
          (currDepth - 1, currIdx + 1)
        } else {
          (currDepth, currIdx + 1)
        }
      }}
      idx;
  }
  def findMatchedParenIdxFromStr(str : String, openParen : Char = '(', closeParen : Char = ')') : Int = {
      if(str.head != openParen){
        error("findMatchedParenIdxFromStr run on non-paren char!");
      }
      val (depth, idx) = str.tail.zipWithIndex.iterator.foldLeft((1,1)){ case ((currDepth,currIdx),(currChar,idx)) => {
        if(currDepth == 0){
          (currDepth, currIdx);
        } else if(currChar == openParen){
          (currDepth + 1, currIdx + 1)
        } else if(currChar == closeParen){
          (currDepth - 1, currIdx + 1)
        } else {
          (currDepth, currIdx + 1)
        }
      }}
      idx;
  }
  
  class FunctionExpression(args : Seq[String])
  /*
  case class ExpressionFunction[A,B](funcName : String, numParam : Int, desc : String, paramNames : Seq[String]){
    def getFunctionInfo() : String = {
      funcName+"\t"+numParam+"\t"+desc;
    }
    //def getReadyFunc
  }
  case class ReadyExpressionFunction[A,B](exprFunc : ExpressionFunction[A,B],
                                          params : Vector[ReadyExpressionFunction[A,B]],
  */
  //object FunctionExpressionParser {
    //ExpressionFunction[A,B](funcName : String, numParam : Int, desc : String, paramNames : Seq[String], metaFunc : ((SVcfVariantLine,Seq[ExpressionFunction[SVcfVariantLine,String]]) => ((SVcfVariantLine) => String)))
    
    /*
    val exprFuncSet : Vector[ExpressionFunction[SVcfVariantLine,String]]] = Vector[ExpressionFunction[SVcfVariantLine,String]]](
        ExpressionFunction[SVcfVariantLine,String]("AND",numParam=-1,desc = "", paramNames = c("expr1","expr2","..."),
            metaFunc = { (paramSeq : Seq[ExpressionFunction[SVcfVariantLine,String]]) => {
               ((vc : SVcfVariantLine) => {
                 paramSeq.forAll( subexpr => subexpr
               })
            }}
        )
    )*/
  //}
  
    case class FilterFunction[A](funcName : String, 
                              numParam : Int, 
                              desc : String, 
                              paramNames : Seq[String], paramTypes : Seq[String],
                              metaFunc : ((Seq[String]) => ((A) => Boolean)),
                              isHidden : Boolean = false){
      def getFunctionInfo() : String = {
        funcName+"\t"+numParam+"\t"+desc;
      }
      def checkFunction() : Boolean = {
        true;
      }
    }
    
  
  abstract class SFilterLogicParser[A](){
    def filterManualTitle() : String
    def filterManualDesc() : String
    
    def logicManualTitle : String = "BASIC SYNTAX:";
    def logicManualRaw : Seq[(Option[String],String)] = 
      Seq[(Option[String],String)]( 
                                  (None,
                                        "Variant expressions are logical expressions that are performed at the "+
                                        "variant level. They are used by several parts of vArmyKnife, usually when "+
                                        "filtering or differentiating variants based on it's properties/stats. "+
                                        "For any given variant, a variant expression will return either TRUE "+
                                        "or FALSE. Variant expressions are parsed as a series of logical functions "+
                                        "connected with AND, OR, NOT, and parentheses. All expressions MUST "+
                                        "be separated with whitespace, though it does not matter how much "+
                                        "whitespace or what kind. Alternatively, expressions can be read "+
                                        "directly from file by setting the expression to"+
                                        "EXPRESSIONFILE:filepath."
                                  ),
                                  (None,"Variant Expression functions are all of the format "+
                                        "FILTERNAME:PARAM1:PARAM2:etc. Some filters have no parameters; "+
                                        "other filters can accept a variable number of parameters. All "+
                                        "expression functions return TRUE or FALSE. Filters can be inverted using the "+
                                        "NOT operator before the filter (with whitespace in between)."
                                  )
      );
    def logicManualFmt : Seq[UserManualBlock] = logicManualRaw.map{ case (t,ln) => {
      UserManualBlock( Seq(ln),t , indentTitle = 0, indentBlock = 4, indentFirst = 0);
    }} ++ Seq(
        UserManualBlock(Seq(""),Some(""))
    ) ++ this.filterFunctionSet.toVector.filter{ x => ! x.isHidden }.sortBy( x => x.funcName ).flatMap( x => {
      Seq(
        UserManualBlock(title = None,lines = Seq( x.funcName+"("+x.paramNames.mkString(",")+")"), indentBlock = 8,  indentFirst = 4),
        UserManualBlock(title = None,lines = Seq( x.desc)                                   , indentBlock = 8,  indentFirst = 8),
        UserManualBlock(title = None,lines = Seq( "(Param Types: "+ x.paramTypes.mkString(",") + ({ if(x.numParam == -1) ",...)" else ")" })),
                                                                                            indentBlock = 12, indentFirst = 12)
      )
    })
      
      /*
         case class UserManualBlock( lines : Seq[String],
                              title : Option[String] = None, 
                              level : Int = 1, isCodeBlock : Boolean = true,
                              indentTitle : Int = 4, indentBlock : Int = 8,
                              indentFirst:Int= 8, 
                              titleIndentChar : String = " ", firstLineIndentChar : String = " ", indentChar : String = " ")
       UserManualBlock( lines : Seq[String],
                              title : Option[String] = None, 
                              level : Int = 1, isCodeBlock : Boolean = true)
       */
    
    def getManualString(title : Option[String] = None,dsc : Option[String] = None) : String = {
      val t = title match {
        case Some(x) => x;
        case None => filterManualTitle() 
      }
      val d = dsc match {
        case Some(dx) => dx;
        case None => filterManualDesc() 
      }
      t+"\n" +
      wrapLineWithIndent(d,internalUtils.commandLineUI.CLUI_CONSOLE_LINE_WIDTH,4)+"\n\n"+
      logicManualRaw.map{case (lmrt,lmrd) => {
        "" + (lmrt match {
          case Some(tx) => "  "+tx+"\n";
          case None => "";
        }) + wrapLineWithIndent(lmrd,internalUtils.commandLineUI.CLUI_CONSOLE_LINE_WIDTH,4)
      }}.mkString("\n")+
      "\nFilter Functions:\n"+
      this.filterFunctionSet.toVector.sortBy( x => x.funcName ).map( x => {
          "    "+x.funcName  +"("+x.paramNames.mkString(",")+ ")\n"+
          wrapLineWithIndent(x.desc,internalUtils.commandLineUI.CLUI_CONSOLE_LINE_WIDTH,8)
      }).mkString("\n") + "\n"
    }
    
    def getMarkdownManualString(title : Option[String] = None,dsc : Option[String] = None) : String = {
      val t = title match {
        case Some(x) => x;
        case None => filterManualTitle() 
      }
      val d = dsc match {
        case Some(dx) => dx;
        case None => filterManualDesc() 
      }
      "# "+escapeToMarkdown(t)+"\n\n" +
      wrapLineWithIndent(escapeToMarkdown(d),internalUtils.commandLineUI.CLUI_CONSOLE_LINE_WIDTH,0)+"\n\n"+
      logicManualRaw.map{case (lmrt,lmrd) => {
        "" + (lmrt match {
          case Some(tx) => "### "+escapeToMarkdown(tx)+"\n\n";
          case None => "";
        }) + wrapLineWithIndent(escapeToMarkdown(lmrd),internalUtils.commandLineUI.CLUI_CONSOLE_LINE_WIDTH,0)+"\n"
      }}.mkString("\n")+
      "### True/False Functions:\n\n"+
      this.filterFunctionSet.toVector.sortBy( x => x.funcName ).map( x => {
          "#### "+escapeToMarkdown(x.funcName) +(if(x.paramNames.length == 0){""}else{"("+x.paramNames.mkString(",")+ ")"})+"\n\n> "+escapeToMarkdown(x.desc)+"\n\n"+
          (if(x.paramNames.length == x.paramTypes.length){ x.paramNames.zip(x.paramTypes).map{ case (pn,pt) => {
            "    "+pn+": "+pt
          }}.mkString("\n")} else {""})
      }).mkString("\n") + "\n"
    }
    
    def parseString_NEW(str : String) : SFilterLogic[A] = {
      if( str.count( cc => cc == '(' ) != str.count(cc => cc == ')')){
        error("ERROR parsing logical expression: different number of open and closed perentheses!\n"+
              "      Found "+str.count( cc => cc == '(')+" open perens and " + str.count(cc => cc == ')') + " closed perens in string:"+
              "      \""+str+"\""
            );
      }
      var s = str.trim().replaceAll("AND\\(","AND \\(").replaceAll("OR\\(","OR \\(").replaceAll("NOT\\(","NOT \\(").replaceAll("\\(NOT","\\( NOT").replaceAll("\t"," ").replaceAll("\n"," ").replaceAll("\\s+"," ");
      var ss = s.replaceAll("\\(\\(","\\( \\(").replaceAll("\\)\\)","\\) \\)")
      while( s != ss ){
        s = ss;
        ss = s.replaceAll("\\(\\(","\\( \\(").replaceAll("\\)\\)","\\) \\)").trim();
      }
      //parseStringArray_NEW(s.split("\\s+"));
      
      return null;
    }
    /*
    abstract class Token {
      
    }
    case class AtomicToken( s : String ) extends Token {
      
    }
    case class NotToken( s : Token ) extends Token {
      
    }
    case class AndToken( a : Token, b : Token ) extends Token {
      
    }
    case class OrToken( a : Token, b : Token ) extends Token {
      
    }
    def buildToken( s : String ) : Token = {
      if(s.head == '(' && s.last == ')'){
        buildToken(s.tail.init);
      } else if(s.startsWith("NOT ")){
        return NotToken( buildToken( s.drop(3).trim() )) ;
      } else if(s.head == '('){
        val closeIdx = findMatchedParenIdxFromStr(s);
        val parenStr = s.take(closeIdx).tail.init;
        val remainder = s.drop(closeIdx).trim();
        
        if( remainder.startsWith("AND")){
          AndToken(buildToken(parenStr), buildToken(remainder));
        } else if( remainder.startsWith("OR")){
          OrToken(buildToken(parenStr), buildToken(remainder));          
        } else {
          error("Bad logical expression: new phrase does not start with AND, OR, or NOT");
          return null;
        }
      } else if(
    }*/
    
    
    /*
    
    def parseStringArray_NEW(strs : Seq[String]) : SFilterLogic[A] = {
      if( strs.head == "(" && strs.last == ")"){
        return parseStringArray_NEW( strs.tail.init );
      } else if( strs.head == "(" ) {
        val closeIdx = findMatchedParenIdx(strs);
        val parenStr = strs.take(closeIdx).tail.init;
        
      } else {
   
        val ss = str.split("\\s+")
        
        
        
      }
      return null;
    }
    def parseLogicFunc_NEW(a : SFilterLogic[A], strs : Seq[String]) : SFilterLogic[A] = {
      if(strs.length == 0){
        a;
      } else if(strs.head == "AND"){
        SFilterAND[A](a,parseStringArray(strs.tail));
      } else if(strs.head == "OR"){
        SFilterOR[A](a,parseStringArray(strs.tail));
      } else {
        error("Filter logic parse error! Unrecognized logic func: "+strs.head);
        null;
      }
    }*/

    
    def parseString(str : String) : SFilterLogic[A] = {
      if( str.count( cc => cc == '(' ) != str.count(cc => cc == ')')){
        error("ERROR parsing logical expression: different number of open and closed perentheses!\n"+
              "      Found "+str.count( cc => cc == '(')+" open perens and " + str.count(cc => cc == ')') + " closed perens in string:"+
              "      \""+str+"\""
            );
      }
      
      parseStringArray(str.trim().replaceAll("\\("," \\( ").replaceAll("\\)"," \\) ").trim().split("\\s+").toSeq);
    }
  
    def parseStringArray_OLD(strs : Seq[String]) : SFilterLogic[A] = {
      if(strs.length == 0){
        error("Null SVcfFilterLogic!");
      }
      val str = strs.head;
      
      if(str == "("){
        val closeIdx = findMatchedParenIdx(strs);
        val parenStr = strs.take(closeIdx).tail.init;
        //parseDebugReport("Entering Parenthetical: [\"" + parenStr.mkString("\",\"") + "\"]" + " with remainder: [\"" + strs.drop(closeIdx).mkString("\",\"")+"\"]");
        parseLogicFunc(parseStringArray(parenStr),strs.drop(closeIdx));
      } else if(str == "NOT"){
        SFilterNOT[A](parseStringArray(strs.tail));
      } else { //if(strs.head.startsWith("FILT:")) {
        //parseDebugReport("Parsing Binary: [" + strs.head +"]");
        parseLogicFunc(parseFilter(strs.head),strs.drop(1));
      } //else {
        //null;
     // }
    }
    
    def parseLogicFunc(a : SFilterLogic[A], strs : Seq[String]) : SFilterLogic[A] = {
      if(strs.length == 0){
        a;
      } else if(strs.head == "AND"){
        SFilterAND[A](a,parseStringArray(strs.tail));
      } else if(strs.head == "OR"){
        SFilterOR[A](a,parseStringArray(strs.tail));
      } else {
        error("Filter logic parse error! Unrecognized logic func: "+strs.head);
        null;
      }
    }
    
    def parseStringArray(strs : Seq[String]) : SFilterLogic[A] = {
      if(strs.length == 0){
        error("Null SVcfFilterLogic!");
      }
      val str = strs.head;
      
      if(str == "("){
        val closeIdx = findMatchedParenIdx(strs);
        val parenStr = strs.take(closeIdx).tail.init;
        //parseDebugReport("Entering Parenthetical: [\"" + parenStr.mkString("\",\"") + "\"]" + " with remainder: [\"" + strs.drop(closeIdx).mkString("\",\"")+"\"]");
        parseLogicFunc(parseStringArray(parenStr),strs.drop(closeIdx));
      } else if(str == "NOT"){
        SFilterNOT[A](parseStringArray(strs.tail));
      } else { //if(strs.head.startsWith("FILT:")) {
        //parseDebugReport("Parsing Binary: [" + strs.head +"]");
        
        if( ! strs.head.contains(':') && strs.length > 1 && strs(1) == "(" ){
          //PAREN-STYLE:
          val closeIdx = findMatchedParenIdx(strs.tail);
          val filterID = strs.head
          //val fcnString = filterID+":"+strs.tail.take(closeIdx).tail.init.mkString("").split(",").mkString(":")
          val fcnString = filterID+":"+strs.tail.take(closeIdx).tail.init.mkString("").split(",").mkString(":")
          parseLogicFunc( parseFilter(fcnString),strs.tail.drop(closeIdx) );
        } else {
          parseLogicFunc(parseFilter(strs.head),strs.drop(1));
        }
      } //else {
        //null;
     // }
    }

    def parseFilter(str : String) : SFilterLogic[A] = {
      //if(! str.startsWith("FILT:")){
      //  error("Filter Logic Parse Error! Filters must begin with \"FILT\"");
      //}
      val cells = str.split(":")
      if(cells.length < 1){
        error("Filter Logic Parse Error! Filter must have at least 1 element!");
      }
      val funID = cells(0);
      val params = cells.drop(1)
      val (fun,ff) = getFilterFunction(funID,params);
      SFilterByFunc[A](fun=fun,params=params,filterName=funID,ff=ff);
    }
    

    
    /*case class FilterFunction(funcName : String, 
                              numParam : Int, 
                              desc : String, 
                              paramNames : Seq[String], paramTypes : Seq[String],
                              metaFunc : ((Seq[String]) => ((A) => Boolean))){
      def getFunctionInfo() : String = {
        funcName+"\t"+numParam+"\t"+desc;
      }
      def checkFunction() : Boolean = {
        true;
      }
    }*/
    
    def filterFunctionSet : Set[FilterFunction[A]];
    def filterFunctionMap : Map[String,FilterFunction[A]];
    
    def getAllFunctionInfo() : String = {
      var out = "funcName\tnumParam\tdesc\n"
      filterFunctionSet.toSeq.sortBy(f => f.funcName).foreach{ ff => {
        out = out + ff.getFunctionInfo()+"\n";
      }}
      return out;
    }
    
    def getFilterFunction(func : String, params : Seq[String]) : ((A => Boolean),FilterFunction[A]) = {
      filterFunctionMap.get(func) match {
        case Some(ff) => {
          if(ff.numParam != -1 && params.length != ff.numParam){
            error("Filter Logic Parse Error: Filter Function " + func + " requres "+ff.numParam + " parameters. Found: Params=[\""+params.mkString("\",\"")+"\"]");
          }
          (ff.metaFunc(params),ff);
        }
        case None => {
          error("Filter Logic Parse Error: Filter function " + func + " not recognized! Legal functions are: \n"+getAllFunctionInfo());
          return null;
        }
      }
    }
  }
  
  //,
  //                            testFunc : ((SVcfHeader) => Boolean) = (h : SVcfHeader) =>{ true }
  
  //case class SVcfFilterLogicParser() extends SFilterLogicParser[SVcfVariantLine]{
  def checkSVcfFilterLogicParse[A]( filterLogic : SFilterLogic[A], vcfHeader : SVcfHeader ) : Boolean = {
    val ffs = filterLogic.getDefSeq;
    ! ffs.exists{ case (ff,pp) => {
      val pt = if(ff.numParam == -1){
        if(pp.length < ff.paramTypes.length){
          error("ERROR in filter logic: filter function "+ff.funcName+" requires at least "+ff.paramTypes.length+" parameters!");
        }
        ff.paramTypes ++ repToSeq(ff.paramTypes.last, pp.length - ff.paramTypes.length )
      } else {
        ff.paramTypes
      }
      pp.zip(pt).zipWithIndex.foreach{ case ((p,t),i) => {
        if(t.toUpperCase() == "INFO"){
          if(!  vcfHeader.infoLines.exists( f => { f.ID == p } ) ){
            error("ERROR: INFO field \""+p+"\" DOES NOT EXIST! logical expression function \""+ff.funcName+"\" requires an INFO field in parameter "+i)
          }
        }
      }}
      pp.zip(pt).zipWithIndex.foreach{ case ((p,t),i) => {
        if(t.toUpperCase() == "INFO"){
          if(!  vcfHeader.infoLines.exists( f => { f.ID == p } ) ){
            error("ERROR: INFO field \""+p+"\" DOES NOT EXIST! logical expression function \""+ff.funcName+"\" requires an INFO field in parameter "+i)
          }
        } else if( t.toUpperCase() == "info"){
          if(!  vcfHeader.infoLines.exists( f => { f.ID == p } ) ){
            if(string2doubleOpt(p).isEmpty){
              error("ERROR: INFO field \""+p+"\" DOES NOT EXIST / numeric misformatted! logical expression function \""+ff.funcName+"\" requires an INFO field OR a numeric value in parameter "+i)
            }            
          }
        } else if( t.toUpperCase() == "GENO"){
          if(!  vcfHeader.formatLines.exists( f => { f.ID == p } ) ){
            error("ERROR: FORMAT field \""+p+"\" DOES NOT EXIST! logical expression function \""+ff.funcName+"\" requires a FORMAT field in parameter "+i)
          }
        } else if( t.toUpperCase() == "INT"){
          if( string2intOpt(p).isEmpty ){
            error("ERROR: Field \""+p+"\" is not an integer! logical expression function \""+ff.funcName+"\" requires an INTEGER in parameter "+i)
          }
        } else if( t.toUpperCase() == "NUMBER"){
          if( string2doubleOpt(p).isEmpty ){
            error("ERROR: Field \""+p+"\" is not a number! logical expression function \""+ff.funcName+"\" requires a NUMBER in parameter "+i)
          }
        } else if( t.toUpperCase() == "STRING"){
          //do nothing, always ok.
        } else if( t.toUpperCase() == "INFILE"){
          if( !  (new File(p)).exists() ){
            error("ERROR: File \""+p+"\" does not exists! logical expression function \""+ff.funcName+"\" requires an INPUT FILE in parameter "+i)
          }
        }
      }}
      
      false;
    }}
  }
  
  
  
  val sVcfFilterLogicParser : SFilterLogicParser[SVcfVariantLine] = new SFilterLogicParser[SVcfVariantLine]{
    
    def filterManualTitle() : String = "VCF Line Filters:";
    def filterManualDesc() : String = "";
    
    def tagNonMissing(tag : String, a : SVcfVariantLine) : Boolean = {
      a.info.contains(tag) && a.info(tag).get != "."
    }
    def tagMissing(tag : String, a : SVcfVariantLine) : Boolean = {
      (! a.info.contains(tag)) || a.info(tag).get == "."
    } 
    def testFilterFunction(str : String, h : SVcfHeader){
      //parseStringArray(strs : Seq[String]) 
      val strs = str.trim().replaceAll("\\("," \\( ").replaceAll("\\)"," \\) ").trim().split("\\s+").toSeq;
      val funcs = strs.filter( ss => ! Set("AND","OR","NOT",")","(").contains(ss) )
      funcs.foreach{ ff => { 
        val cells = ff.split(":")
        if(cells.length < 1){
          error("Filter Logic Parse Error! Filter must have at least 1 element!");
        }
        val funID = cells(0);
        val params = cells.drop(1)
        val fun = filterFunctionMap(funID);
        params.zip(fun.paramTypes).foreach{ case (pp,pt) => {
          if(pt == "INFO"){
            if( ! h.infoLines.exists{ info => {
              info.ID == pp;
            }} ){
              error("FATAL ERROR in Logic Parser! FORMAT tag not found: "+pp+" in function: "+ff+" in logic string: \""+str+"\"");
            }
          } else if(pt == "GENO"){
            if( ! h.formatLines.exists{ info => {
              info.ID == pp;
            }} ){
              error("FATAL ERROR in Logic Parser! FORMAT tag not found: "+pp+" in function: "+ff+" in logic string: \""+str+"\"");
            }
          }
        }}
      }}
      
    }
    


    
    //Map(funcID) = (numParam,desc,metaFunction(params) => function(vc))
    val filterFunctionSetVal : Set[FilterFunction[SVcfVariantLine]] = Set[FilterFunction[SVcfVariantLine]]( 
        FilterFunction(funcName="INFO.eq",numParam=2,desc="TRUE iff INFO field t is nonmissing and equal to k.",paramNames=Seq("t","k"),paramTypes=Seq("info","string"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = params(1);
                          (a : SVcfVariantLine) => {
                            tagNonMissing(tag,a) && a.info(tag).get == v;
                          }
                        }
                      ), 
        FilterFunction(funcName="INFO.ne",numParam=2,desc="TRUE iff INFO field t is either missing or not equal to k.",paramNames=Seq("t","k"),paramTypes=Seq("info","string"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = params(1);
                          (a : SVcfVariantLine) => {
                            (! a.info.contains(tag)) || a.info(tag).get != v;
                          }
                        }
                      ),
        FilterFunction(funcName="INFO.nm",numParam=1,desc="TRUE iff INFO field t is nonmissing.",paramNames=Seq("t"),paramTypes=Seq("info"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          (a : SVcfVariantLine) => {
                            tagNonMissing(tag,a)
                          }
                        }
                      ),
        FilterFunction(funcName="INFO.m",numParam=1,desc="TRUE iff INFO field t is missing.",paramNames=Seq("t"),paramTypes=Seq("info"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          (a : SVcfVariantLine) => {
                            tagMissing(tag,a)
                          }
                        }
                      ),
        FilterFunction(funcName="INFO.gt",numParam=2,desc="TRUE iff INFO field t is nonmissing and greater than k.",paramNames=Seq("t","k"),paramTypes=Seq("info","number"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = string2double(params(1));
                          (a : SVcfVariantLine) => {
                            tagNonMissing(tag,a) && string2double(a.info(tag).get) > v ;
                          }
                        }
                      ),
        FilterFunction(funcName="INFO.lt",numParam=2,desc="TRUE iff INFO field t is nonmissing and less than k.",paramNames=Seq("t","k"),paramTypes=Seq("info","number"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = string2double(params(1));
                          (a : SVcfVariantLine) => {
                            tagNonMissing(tag,a) && string2double(a.info(tag).get) < v ;
                          }
                        }
                      ),
        FilterFunction(funcName="INFO.ge",numParam=2,desc="TRUE iff INFO field t is nonmissing and greater than or equal to k.", paramNames=Seq("t","k"),paramTypes=Seq("info","number"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = string2double(params(1));
                          (a : SVcfVariantLine) => {
                            tagNonMissing(tag,a) && string2double(a.info(tag).get) >= v ;
                          }
                        }
                      ),
        FilterFunction(funcName="INFO.le",numParam=2,desc="TRUE iff INFO field t is nonmissing and less than or equal to k.", paramNames=Seq("t","k"),paramTypes=Seq("info","number"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = string2double(params(1));
                          (a : SVcfVariantLine) => {
                            tagNonMissing(tag,a) && string2double(a.info(tag).get) <= v ;
                          }
                        }
                      ),
        FilterFunction(funcName="INFO.len.gt",numParam=2,desc="TRUE iff INFO field t is nonmissing and has length greater than k.", paramNames=Seq("t","k"),paramTypes=Seq("info","int"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = string2int(params(1));
                          (a : SVcfVariantLine) => {
                            tagNonMissing(tag,a) && (a.info(tag).map{_.split(",").length}.getOrElse(0)) > v ;
                          }
                        }
                      ),
        FilterFunction(funcName="INFO.len.lt",numParam=2,desc="TRUE iff INFO field t is nonmissing and has length less than k.", paramNames=Seq("t","k"),paramTypes=Seq("info","int"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = string2int(params(1));
                          (a : SVcfVariantLine) => {
                            tagNonMissing(tag,a) && (a.info(tag).map{_.split(",").length}.getOrElse(0)) < v ;
                          }
                        }
                      ),
        FilterFunction(funcName="INFO.len.eq",numParam=2,desc="TRUE iff INFO field t is nonmissing and has length equal to k.", paramNames=Seq("t","k"),paramTypes=Seq("info","int"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = string2int(params(1));
                          (a : SVcfVariantLine) => {
                            tagNonMissing(tag,a) && (a.info(tag).map{_.split(",").length}.getOrElse(0)) == v ;
                          }
                        }
                      ),

        FilterFunction(funcName="INFO.any.gt",numParam=2,desc="TRUE iff INFO field t is nonmissing and less than or equal to k.", paramNames=Seq("t","k"),paramTypes=Seq("info","number"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = string2double(params(1));
                          (a : SVcfVariantLine) => {
                            tagNonMissing(tag,a) && a.info(tag).get.split(",").filter(_ != ".").map{string2double(_)}.exists(_ > v) ;
                          }
                        }
                      ),
        FilterFunction(funcName="INFO.any.lt",numParam=2,desc="TRUE iff INFO field t is nonmissing and less than or equal to k.", paramNames=Seq("t","k"),paramTypes=Seq("info","number"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = string2double(params(1));
                          (a : SVcfVariantLine) => {
                            tagNonMissing(tag,a) && a.info(tag).get.split(",").filter(_ != ".").map{string2double(_)}.exists(_ < v) ;
                          }
                        }
                      ),
                      
        FilterFunction(funcName="INFO.gtm",numParam=2,desc="TRUE iff INFO field t is missing or greater than k.",paramNames=Seq("t","k"),paramTypes=Seq("info","number"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = string2double(params(1));
                          (a : SVcfVariantLine) => {
                            tagMissing(tag,a) || string2double(a.info(tag).get) > v ;
                          }
                        }
                      ),
        FilterFunction(funcName="INFO.ltm",numParam=2,desc="TRUE iff INFO field t is missing or less than k.",paramNames=Seq("t","k"),paramTypes=Seq("info","number"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = string2double(params(1));
                          (a : SVcfVariantLine) => {
                            tagMissing(tag,a) || string2double(a.info(tag).get) < v ;
                          }
                        }
                      ),
        FilterFunction(funcName="INFO.gem",numParam=2,desc="TRUE iff INFO field t is missing or greater than or equal to k.",paramNames=Seq("t","k"),paramTypes=Seq("info","number"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = string2double(params(1));
                          (a : SVcfVariantLine) => {
                            tagMissing(tag,a) || string2double(a.info(tag).get) >= v ;
                          }
                        }
                      ),
        FilterFunction(funcName="INFO.lem",numParam=2,desc="TRUE iff INFO field t is missing or less than or equal to k",paramNames=Seq("t","k"),paramTypes=Seq("info","number"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = string2double(params(1));
                          (a : SVcfVariantLine) => {
                            tagMissing(tag,a) || string2double(a.info(tag).get) <= v ;
                          }
                        }
                      ),
        FilterFunction(funcName="INFO.mempty",numParam=1,desc="TRUE iff INFO field t is missing or less than or equal to k",paramNames=Seq("t"),paramTypes=Seq("info"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          //val v = string2double(params(1));
                          (a : SVcfVariantLine) => {
                            tagMissing(tag,a) || a.info(tag).get == "."
                          }
                        }
                      ),
                      
        FilterFunction(funcName="INFO.in",numParam=2,desc="TRUE iff INFO field t is a comma delimited list that contains string k.",paramNames=Seq("t","k"),paramTypes=Seq("info","string"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = params(1);
                          (a : SVcfVariantLine) => {
                            tagNonMissing(tag,a) && a.info(tag).get.split(",").contains(v) ;
                          }
                        }
                      ),
        FilterFunction(funcName="INFO.notIn",numParam=2,desc="TRUE iff INFO field t is missing or is a comma delimited list that does NOT contain string k.",paramNames=Seq("t","k"),paramTypes=Seq("info","string"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = params(1);
                          (a : SVcfVariantLine) => {
                            tagMissing(tag,a) || a.info(tag).get.split(",").contains(v) ;
                          }
                        }
                      ), 
        FilterFunction(funcName="INFO.inAny", numParam = 2,desc="TRUE if INFO field t is a list delimited with commas and bars, and contains string k.",paramNames=Seq("t","k"),paramTypes=Seq("info","string"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = params(1);
                          (a : SVcfVariantLine) => {
                            tagNonMissing(tag,a) && a.info(tag).get.split(",").flatMap{s => s.split("\\|")}.contains(v) ;
                          }
                        }
                      ),
        FilterFunction(funcName="INFO.notInAny", numParam = 2,desc="TRUE if INFO field t is a list delimited with commas and bars, and does not contain string k.",paramNames=Seq("t","k"),paramTypes=Seq("info","string"),
                        (params : Seq[String]) => { 
                          val tag = params(0);
                          val v = params(1);
                          (a : SVcfVariantLine) => {
                            tagMissing(tag,a) || a.info(tag).get.split(",").flatMap{s => s.split("\\|")}.contains(v) ;
                          }
                        }
                      ),
        FilterFunction(funcName="INFO.inAnyOf", numParam = -1,desc="TRUE iff INFO field t is a list delimited with commas and bars, and contains any of the parameters k1,k2,...",paramNames=Seq("t","k1","k2","..."),paramTypes=Seq("info","string"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = params.tail.toSet;
                          (a : SVcfVariantLine) => {
                            tagNonMissing(tag,a) && a.info(tag).get.split(",").flatMap{s => s.split("\\|")}.toSet.intersect(v).size > 0 ;
                          }
                        }
                      ),
        FilterFunction(funcName="INFO.inAnyOfN", numParam = -1,desc="TRUE iff INFO field t is a list delimited with commas, bars, slashes, OR COLONS, and contains any of the parameters k1,k2,...",paramNames=Seq("t","k1","k2","..."),paramTypes=Seq("info","string"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = params.tail.toSet;
                          (a : SVcfVariantLine) => {
                            tagNonMissing(tag,a) && a.info(tag).get.split(",").flatMap{s => s.split("[:\\|/]")}.toSet.intersect(v).size > 0 ;
                          }
                        }
                      ),
        FilterFunction(funcName="INFO.inAnyOfND", numParam = -1,desc="TRUE iff INFO field t is a list delimited with commas, bars, slashes, colons, or dashes, and contains any of the parameters k1,k2,...",paramNames=Seq("t","k1","k2","..."),paramTypes=Seq("info","string"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = params.tail.toSet;
                          (a : SVcfVariantLine) => {
                            tagNonMissing(tag,a) && a.info(tag).get.split(",").flatMap{s => s.split("[:\\|/-]")}.toSet.intersect(v).size > 0 ;
                          }
                        }
                      ),
        FilterFunction(funcName="INFO.subsetOf", numParam = -1,desc="TRUE iff INFO field t is a comma delimited list and is a subset of k1,k2,etc",paramNames=Seq("t","k1","k2","..."),paramTypes=Seq("info","string"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = params.tail.toSet;
                          (a : SVcfVariantLine) => {
                            tagNonMissing(tag,a) && a.info(tag).get.split(",").toSet.subsetOf(v)
                          }
                        }
                      ),
        FilterFunction(funcName="INFO.subsetOfFileList", numParam = 2,desc="TRUE iff INFO field t is a comma delimited list and is a subset of the list contained in file f",paramNames=Seq("t","f"),paramTypes=Seq("info","infile"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = getLinesSmartUnzip(params(1)).toSet
                          (a : SVcfVariantLine) => {
                            tagNonMissing(tag,a) && a.info(tag).get.split(",").toSet.subsetOf(v)
                          }
                        }
                      ),

        FilterFunction(funcName="REF.len.eq", numParam = 1,desc="TRUE iff the REF allele is of length k.",paramNames=Seq("k"),paramTypes=Seq("int"),
                        (params : Seq[String]) => {
                          val len = string2int(params(0));
                          (a : SVcfVariantLine) => {
                            a.ref.length == len
                          }
                        }
                      ),
        FilterFunction(funcName="ALT.len.eq", numParam = 1,desc="TRUE iff the first ALT allele is of length k.",paramNames=Seq("k"),paramTypes=Seq("int"),
                        (params : Seq[String]) => {
                          val len = string2int(params(0));
                          (a : SVcfVariantLine) => {
                            a.alt.head.length == len
                          }
                        }
                      ),
        FilterFunction(funcName="REF.len.gt", numParam = 1,desc="TRUE iff the REF allele is of length gt k.",paramNames=Seq("k"),paramTypes=Seq("int"),
                        (params : Seq[String]) => {
                          val len = string2int(params(0));
                          (a : SVcfVariantLine) => {
                            a.ref.length > len
                          }
                        }
                      ),
        FilterFunction(funcName="ALT.len.gt", numParam = 1,desc="TRUE iff the first ALT allele is of length gt k.",paramNames=Seq("k"),paramTypes=Seq("int"),
                        (params : Seq[String]) => {
                          val len = string2int(params(0));
                          (a : SVcfVariantLine) => {
                            a.alt.head.length > len
                          }
                        }
                      ),
        FilterFunction(funcName="REF.eq", numParam = 1,desc="TRUE iff the REF allele equals k.",paramNames=Seq("k"),paramTypes=Seq("string"),
                        (params : Seq[String]) => {
                          val gt = (params(0));
                          (a : SVcfVariantLine) => {
                            a.ref == gt
                          }
                        }
                      ),
        FilterFunction(funcName="ALT.eq", numParam = 1,desc="TRUE iff the first ALT allele equals k.",paramNames=Seq("k"),paramTypes=Seq("string"),
                        (params : Seq[String]) => {
                          val gt = (params(0));
                          (a : SVcfVariantLine) => {
                            a.alt.head == gt
                          }
                        }
                      ),
        FilterFunction(funcName="REF.isOneOf", numParam = -1,desc="TRUE iff the REF allele is one of k1,k2,...",paramNames=Seq("k1","k2","..."),paramTypes=Seq("string"),
                        (params : Seq[String]) => {
                          val gtset = params.toSet;
                          (a : SVcfVariantLine) => {
                            gtset.contains(a.ref)
                          }
                        }
                      ),
        FilterFunction(funcName="ALT.isOneOf", numParam = -1,desc="TRUE iff the first ALT allele is one of k1,k2,...",paramNames=Seq("k1","k2","..."),paramTypes=Seq("string"),
                        (params : Seq[String]) => {
                          val gtset = params.toSet;
                          (a : SVcfVariantLine) => {
                            gtset.contains(a.alt.head)
                          }
                        }
                      ),
        FilterFunction(funcName="INFO.tagsMismatch", numParam = 2,desc="TRUE iff the INFO-field t1 and t2 are both found on a given line but are not equal.",paramNames=Seq("t1","t2"),paramTypes=Seq("info","info"),
                        (params : Seq[String]) => {
                          val tag1 = params(0);
                          val tag2 = params(1);
                          (a : SVcfVariantLine) => {
                            if(tagMissing(tag1,a) || tagMissing(tag2,a)){
                              false
                            } else {
                              a.info(tag1).get != a.info(tag2).get
                            }
                          }
                        }
                      ),
        FilterFunction(funcName="INFO.tagsDiff", numParam = 2,desc="TRUE iff the INFO-field t1 and t2 are different, including when one is missing and the other is not.",paramNames=Seq("t1","t2"),paramTypes=Seq("info","info"),
                        (params : Seq[String]) => {
                          val tag1 = params(0);
                          val tag2 = params(1);
                          (a : SVcfVariantLine) => {
                            if(tagMissing(tag1,a) && tagMissing(tag2,a)){
                              false
                            } else if(tagMissing(tag1,a) || tagMissing(tag2,a)){
                              true
                            } else {
                              a.info(tag1).get != a.info(tag2).get
                            }
                          }
                        }
                      ),
        FilterFunction(funcName="GENO.hasTagPairMismatch", numParam = 2,desc="TRUE iff the genotype-field t1 and t2 are both found on a given line but are not always equal for all samples.",paramNames=Seq("t1","t2"),paramTypes=Seq("geno","geno"),
                        (params : Seq[String]) => {
                          val tag1 = params(0);
                          val tag2 = params(1);
                          (a : SVcfVariantLine) => {
                            val idx1 = a.genotypes.fmt.indexOf(tag1);
                            val idx2 = a.genotypes.fmt.indexOf(tag2);
                            if(idx1 != -1 && idx2 != -1){
                              a.genotypes.genotypeValues(idx1).zip(a.genotypes.genotypeValues(idx2)).exists{ case (v1,v2) => {
                                v1 != v2;
                              }}
                            } else {
                              false;
                            }
                          }
                        }
                      ),
        FilterFunction(funcName="GENO.hasTagPairGtStyleMismatch", numParam = 2,desc="TRUE iff the genotype-field t1 and t2 are both found on a given line and have at least 1 sample where both tags are not set to missing but they do not have the same value.",paramNames=Seq("t1","t2"),paramTypes=Seq("geno","geno"),
                        (params : Seq[String]) => {
                          val tag1 = params(0);
                          val tag2 = params(1);
                          (a : SVcfVariantLine) => {
                            val idx1 = a.genotypes.fmt.indexOf(tag1);
                            val idx2 = a.genotypes.fmt.indexOf(tag2);
                            if(idx1 != -1 && idx2 != -1){
                              a.genotypes.genotypeValues(idx1).zip(a.genotypes.genotypeValues(idx2)).exists{ case (v1,v2) => {
                                (! v1.contains('.')) && (! v2.contains('.')) && v1 != v2;
                              }}
                            } else {
                              false;
                            }
                          }
                        }
                      ),
        FilterFunction(funcName="GENO.MAFgt", numParam = 2,desc="TRUE iff the genotype-field tag t is a genotype-style-formatted field and the minor allele frequency is greater than k.",paramNames=Seq("t","k"),paramTypes=Seq("geno"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val frac = string2float(params(1));
                          (a : SVcfVariantLine) => {
                            val idx = a.genotypes.fmt.indexOf(tag);
                            if(idx != -1){
                              (a.genotypes.genotypeValues(idx).count{ x => {
                                x.split("/").contains("1")
                              }}.toFloat / a.genotypes.genotypeValues(idx).length.toFloat) > frac
                            } else {
                              false;
                            }
                          }
                        }
                      ),
        FilterFunction(funcName="GENO.MAFlt", numParam = 2,desc="TRUE iff the genotype-field tag t is a genotype-style-formatted field and the minor allele frequency is less than k.",paramNames=Seq("t","k"),paramTypes=Seq("geno","number"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val frac = string2float(params(1));
                          (a : SVcfVariantLine) => {
                            val idx = a.genotypes.fmt.indexOf(tag);
                            if(idx != -1){
                              (a.genotypes.genotypeValues(idx).count{ x => {
                                x.split("/").contains("1")
                              }}.toFloat / a.genotypes.genotypeValues(idx).length.toFloat) < frac
                            } else {
                              false;
                            }
                          }
                        }
                      ),
        FilterFunction(funcName="isSNV",numParam=0,desc="TRUE iff the variant is an SNV.",paramNames=Seq(),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          (a : SVcfVariantLine) => {
                            a.ref.length == 1 && a.alt.head.length == 1;
                          }
                        }
                      ),
        FilterFunction(funcName="simpleSNV",numParam=0,desc="TRUE iff the variant is a biallelic SNV.",paramNames=Seq(),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          (a : SVcfVariantLine) => {
                            a.ref.length == 1 && a.alt.length == 1 && a.alt.head.length == 1
                          }
                        }
                      ),
        FilterFunction(funcName="isVariant",numParam=0,desc="FALSE iff the variant has no alt alleles, or if the only alt allele is exactly equal to the ref allele.",paramNames=Seq(),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          (a : SVcfVariantLine) => {
                            val af = a.alt.filter(aa => aa != "." && aa != "*")
                            ( af.length > 0 ) && (! ( af.length == 1 && af.head.toUpperCase() == a.ref.toUpperCase() ))
                          }
                        }
                      ),
        FilterFunction(funcName="allelesHaveNoNs",numParam=0,desc="FALSE iff the variant has unknown bases, ie N, in the ALT or REF alleles.",paramNames=Seq(),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          (a : SVcfVariantLine) => {
                            (! a.ref.contains("N")) && (! a.ref.contains("n")) && (! a.alt.mkString("").contains("N")) && (! a.alt.mkString("").contains("n"))
                          }
                        }
                      ),
                      
        FilterFunction(funcName="FILTER.eq",numParam=1,desc="TRUE iff the FILTER column is equal to k.",paramNames=Seq("k"),paramTypes=Seq("String"),
                        (params : Seq[String]) => {
                          val v = params(0);
                          (a : SVcfVariantLine) => {
                            a.filter == v;
                          }
                        }
                      ),
        FilterFunction(funcName="FILTER.ne",numParam=1,desc="TRUE iff the FILTER column is not equal to k.",paramNames=Seq("k"),paramTypes=Seq("String"),
                        (params : Seq[String]) => {
                          val v = params(0);
                          (a : SVcfVariantLine) => {
                            a.filter != v;
                          }
                        }
                      ),
        FilterFunction(funcName="QUAL.gt",numParam=1,desc="TRUE iff the QUAL column is greater than k.",paramNames=Seq("k"),paramTypes=Seq("String"),
                        (params : Seq[String]) => {
                          val v = string2float( params(0) );
                          (a : SVcfVariantLine) => {
                            string2floatOpt(a.qual).map{q => q > v}.getOrElse(false)
                          }
                        }
                      ),
        FilterFunction(funcName="QUAL.gtm",numParam=1,desc="TRUE iff the QUAL column is greater than k, OR qual is missing.",paramNames=Seq("k"),paramTypes=Seq("String"),
                        (params : Seq[String]) => {
                          val v = string2float( params(0) );
                          (a : SVcfVariantLine) => {
                            string2floatOpt(a.qual).map{q => q > v}.getOrElse(true)
                          }
                        }
                      ),
                      
        FilterFunction(funcName="TRUE",numParam=0,desc="Always TRUE",paramNames=Seq(),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          (a : SVcfVariantLine) => {
                            true;
                          }
                        }
                      ),
        FilterFunction(funcName="FALSE",numParam=0,desc="Never TRUE",paramNames=Seq(),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          (a : SVcfVariantLine) => {
                            false;
                          }
                        }
                      ),
        FilterFunction(funcName="CHROM.inAnyOf",numParam = -1,desc="TRUE iff the variant is one one of the given chromosomes",paramNames=Seq("chrX","..."),paramTypes=Seq("String"),
                        (params : Seq[String]) => {
                          //val tag = params(0);
                          val v = params.toSet;
                          (a : SVcfVariantLine) => {
                            v.contains( a.chrom );
                          }
                        }
                      ),
        FilterFunction(funcName="LOCUS.eq",numParam=2,desc="TRUE if the variant is at the given chromosome and position",paramNames=Seq("chrom","pos"),paramTypes=Seq("String","number"),
                        (params : Seq[String]) => {
                          val chrom = params.head;
                          val pos = string2int(params(1));
                          (a : SVcfVariantLine) => {
                            a.chrom == chrom && a.pos == pos;
                          }
                        }
                      ),
        FilterFunction(funcName="LOCUS.near",numParam=3,desc="TRUE if the variant is within k bases from the given chromosome and position",paramNames=Seq("k","chrom","pos"),paramTypes=Seq("number","String","number"),
                        (params : Seq[String]) => {
                          val chrom = params.head;
                          val pos = string2int(params(1));
                          (a : SVcfVariantLine) => {
                            a.chrom == chrom && a.pos == pos;
                          }
                        }
                      ),
        FilterFunction(funcName="LOCUS.range",numParam=3,desc="TRUE if the variant is at the given chromosome and between the given positions (0-based)",paramNames=Seq("chrom","from","to"),paramTypes=Seq("String","number"),
                        (params : Seq[String]) => {
                          val chrom = params.head;
                          val pfrom = string2int(params(1));
                          val pto   = string2int(params(2));
                          (a : SVcfVariantLine) => {
                            a.chrom == chrom && a.pos >= pfrom && a.pos < pto;
                          }
                        }
                      ),
        FilterFunction(funcName="POS.inAnyOf",numParam = -1,desc="TRUE iff the variant is at one of the given positions",paramNames=Seq("pos1","..."),paramTypes=Seq("String","String"),
                        (params : Seq[String]) => {
                          val v = params.map{ss => string2int(ss)}.toSet;
                          (a : SVcfVariantLine) => {
                            v.contains( a.pos );
                          }
                        }
                      ),
        FilterFunction(funcName="POS.gt",numParam=1,desc="TRUE iff the variant is at a position greater than the given position",paramNames=Seq("pos"),paramTypes=Seq("number"),
                        (params : Seq[String]) => {
                          val v = string2int(params.head);
                          (a : SVcfVariantLine) => {
                            v < a.pos;
                          }
                        }
                      ),
                      
                      
        FilterFunction(funcName="GTAG.any.gt", numParam = -1,desc="TRUE iff any one of the samples have a value for their genotype-tag entry greater than k.",paramNames=Seq("gtTag","k"),paramTypes=Seq("geno","number"),
                        (params : Seq[String]) => {
                          val gtTag = params.head;
                          val gtK = string2double( params(1) );
                          
                          (a : SVcfVariantLine) => {
                            val idx = a.genotypes.fmt.indexOf( gtTag );
                            if(idx == -1){
                              false
                            } else {
                              a.genotypes.genotypeValues(idx).exists{ vv => {
                                string2doubleOpt(vv).map{ vvv => vvv > gtK }.getOrElse(false);
                              }}
                            }
                          }
                        }
                      ),
                      
                      
        FilterFunction(funcName="AnyGtPass", numParam = -1,desc="TRUE iff any one of the samples pass the supplied GT filter.",paramNames=Seq("simpleGtFiltExpression","k1","..."),paramTypes=Seq("String","String"),
                        (params : Seq[String]) => {
                          val gtFiltName = params.head;
                          val gtParams = params.tail;
                          val (gtFilt,gff) = sGenotypeFilterLogicParser.getFilterFunction(gtFiltName, gtParams)
                          (a : SVcfVariantLine) => {
                            Range(0,a.genotypes.genotypeValues.head.length).exists{ i => {
                              gtFilt(a,i);
                            }}
                          }
                        }
                      ),
        FilterFunction(funcName="AnyGtNonRef", numParam = 1,desc="TRUE iff gtTag has an alt allele for any sample. gtTag must be a GT-formatted genotype field.",paramNames=Seq("gtTag"),paramTypes=Seq("geno"),
                        (params : Seq[String]) => {
                          val gtTag = params.head;
                          (a : SVcfVariantLine) => {
                            a.genotypes.genotypeValues.lift(a.genotypes.fmt.indexOf(gtTag)).map{ gtArray => {
                              gtArray.exists{ gt => gt.split("[[/\\|]]").contains("1") }
                            }}.getOrElse(false);
                          }
                        }
                      )/*,
        FilterFunction(funcName="INFO.altGtOnSampIdx", numParam = -1,desc="UNTESTED ALPHA FUNCTION. NOT FOR GENERAL USE. FORMAT field g is the genotype tag, file f is a list of sample indices starting from 0. TRUE iff the alt genotype is found on a member of the given sample subset. Warning: may fail for non-split multiallelics with more than 9 alleles.",paramNames=Seq("g","f"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = getLinesSmartUnzip(params(1)).map{string2int(_)}.toSet
                          (a : SVcfVariantLine) => {
                            val idx = a.genotypes.fmt.indexOf(tag);
                            if(idx == -1){
                              false
                            } else {
                               ! v.exists{ i => {
                                 a.genotypes.genotypeValues(idx)(i).contains('1')
                               }}
                               //a.genotypes.genotypeValues(idx).iterator.zipWithIndex.exists{ case (gg,i) => {
                               //  gg.contains('1') && ( ! v.contains(i) )
                               //}}
                            }
                          }
                        }
                      ),*/
    ); 
    
    val filterFunctionMapVal : Map[String,FilterFunction[SVcfVariantLine]] = {
      filterFunctionSet.map{ ff => {
        (ff.funcName,ff)
      }}.toMap
    }
    def filterFunctionSet : Set[FilterFunction[SVcfVariantLine]] = filterFunctionSetVal;
    def filterFunctionMap : Map[String,FilterFunction[SVcfVariantLine]] = filterFunctionMapVal;
  }
  
  //case class SGenotypeFilterLogicParser() extends SFilterLogicParser[(SVcfVariantLine,Int)]{
  val sGenotypeFilterLogicParser : SFilterLogicParser[(SVcfVariantLine,Int)] = new SFilterLogicParser[(SVcfVariantLine,Int)]{

    override def logicManualRaw : Seq[(Option[String],String)] = 
      Seq[(Option[String],String)]( 
                                  (None,
                                        "Genotype-level expressions are logical expressions that are performed at the "+
                                        "GENOTYPE level. In other words, for each sample in each variant line. "+
                                        "They are used by several parts of vArmyKnife, usually when "+
                                        "filtering or differentiating genotypes based on their properties/stats. "+
                                        "For any given variant, a genotype expression will return either TRUE "+
                                        "or FALSE for each sample. Genotype expressions are parsed as a series of logical functions "+
                                        "connected with AND, OR, NOT, and parentheses. All expressions MUST "+
                                        "be separated with whitespace, though it does not matter how much "+
                                        "whitespace or what kind. Alternatively, expressions can be read "+
                                        "directly from file by setting the expression to "+
                                        "EXPRESSIONFILE:filepath."
                                  ),
                                  (None,"Genotype Expression functions are all of the format "+
                                        "FILTERNAME:PARAM1:PARAM2:etc. Some filters have no parameters; "+
                                        "other filters can accept a variable number of parameters. All "+
                                        "expression functions return TRUE or FALSE. Filters can be inverted using the "+
                                        "NOT operator before the filter (with whitespace in between)."
                                  )
      );
    
    def filterManualTitle() : String = "";
    def filterManualDesc() : String = "";
    
    
    
    def getTag(tag : String, a : (SVcfVariantLine,Int)) : Option[String] = {
      if(a._1.format.contains(tag)){
        val value = a._1.genotypes.genotypeValues(a._1.format.indexOf(tag))(a._2);
        if(value == ".") None;
        else Some(value);
      } else {
        None;
      }
    }
    def getTagArray(tag : String, a : (SVcfVariantLine,Int)) : Option[String] = {
      if(a._1.format.contains(tag)){
        val value = a._1.genotypes.genotypeValues(a._1.format.indexOf(tag))(a._2);
        if(value == ".") None;
        else Some(value);
      } else {
        None;
      }
    }
    
    //Map(funcID) = (numParam,desc,metaFunction(params) => function(vc))
    val filterFunctionSetVal : Set[FilterFunction[(SVcfVariantLine,Int)]] = Set[FilterFunction[(SVcfVariantLine,Int)]]( 
        FilterFunction(funcName="GTAG.eq",numParam=2,desc="TRUE iff GT field t equals the string s. DROP if tag t is not present or set to missing.",paramNames=Seq("t","s"),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = params(1);
                          (a : (SVcfVariantLine,Int)) => {
                            val value = getTag(tag,a);
                            value.isDefined && value.get == v;
                          }
                        }
                      ), 
        FilterFunction(funcName="GTAG.ne",numParam=2,desc="TRUE iff GT field t does not equal the string s. DROP if tag t is not present or set to missing.",paramNames=Seq("t","k"),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = params(1);
                          (a : (SVcfVariantLine,Int)) => {
                            val value = getTag(tag,a);
                            (! value.isDefined) || value.get != v;
                          }
                        }
                      ),
        FilterFunction(funcName="GTAG.nm",numParam=1,desc="TRUE iff the GT field t is present and not set to missing.",paramNames=Seq("t"),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          (a : (SVcfVariantLine,Int)) => {
                            getTag(tag,a).isDefined
                          }
                        }
                      ),
        FilterFunction(funcName="GTAG.nmg",numParam=1,desc="TRUE iff the GT field t is present and not set to missing and is not set to a missing genotype (./.).",paramNames=Seq("t"),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          (a : (SVcfVariantLine,Int)) => {
                            getTag(tag,a).map{ x => x != "." && x != "./." }.getOrElse(false);
                          }
                        }
                      ),
        FilterFunction(funcName="GTAG.mg",numParam=1,desc="TRUE iff the GT field t is not present or is missing or is set to a missing genotype (./.).",paramNames=Seq("t"),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          (a : (SVcfVariantLine,Int)) => {
                            getTag(tag,a).map{ x => x == "." || x == "./." }.getOrElse(true);
                          }
                        }
                      ),

        FilterFunction(funcName="GTAG.m",numParam=1,desc="TRUE iff the GT field t is is not present or set to missing.",paramNames=Seq("t","k"),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          (a : (SVcfVariantLine,Int)) => {
                            ! getTag(tag,a).isDefined
                          }
                        }
                      ),
        FilterFunction(funcName="GTAG.gt",numParam=2,desc="TRUE iff tag t is present and not set to missing, and is a number greater than k.",paramNames=Seq("t","k"),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = string2double(params(1));
                          (a : (SVcfVariantLine,Int)) => {
                            val value = getTag(tag,a);
                            value.isDefined && string2double(value.get) > v ;
                          }
                        }
                      ),
        FilterFunction(funcName="GTAG.lt",numParam=2,desc="TRUE iff tag t is present and not set to missing, and is a number less than k.",paramNames=Seq("t","k"),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = string2double(params(1));
                          (a : (SVcfVariantLine,Int)) => {
                            val value = getTag(tag,a);
                            value.isDefined && string2double(value.get) < v ;
                          }
                        }
                      ),
        FilterFunction(funcName="GTAG.ge",numParam=2,desc="TRUE iff tag t is present and not set to missing, and is a number greater than or equal to k.", paramNames=Seq("t","k"),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = string2double(params(1));
                          (a : (SVcfVariantLine,Int)) => {
                            val value = getTag(tag,a);
                            value.isDefined && string2double(value.get) >= v ;
                          }
                        }
                      ),
        FilterFunction(funcName="GTAG.le",numParam=2,desc="TRUE iff tag t is present and not set to missing, and is a number less than or equal to k.", paramNames=Seq("t","k"),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = string2double(params(1));
                          (a : (SVcfVariantLine,Int)) => {
                            val value = getTag(tag,a);
                            value.isDefined && string2double(value.get) <= v ;
                          }
                        }
                      ),
        FilterFunction(funcName="GTAG.gtm",numParam=2,desc="TRUE iff tag t is either not present, set to missing, or a number greater than k.",paramNames=Seq("t","k"),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = string2double(params(1));
                          (a : (SVcfVariantLine,Int)) => {
                            val value = getTag(tag,a);
                            value.isEmpty || string2double(value.get) > v ;
                          }
                        }
                      ),
        FilterFunction(funcName="GTAG.ltm",numParam=2,desc="TRUE iff tag t is either not present, set to missing, or a number less than k.",paramNames=Seq("t","k"),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = string2double(params(1));
                          (a : (SVcfVariantLine,Int)) => {
                            val value = getTag(tag,a);
                            value.isEmpty || string2double(value.get) < v ;
                          }
                        }
                      ),
        FilterFunction(funcName="GTAG.gem",numParam=2,desc="TRUE iff tag t is either not present, set to missing, or a number greater than or equal to k.",paramNames=Seq("t","k"),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = string2double(params(1));
                          (a : (SVcfVariantLine,Int)) => {
                            val value = getTag(tag,a);
                            value.isEmpty || string2double(value.get) >= v ;
                          }
                        }
                      ),
        FilterFunction(funcName="GTAG.lem",numParam=2,desc="TRUE iff tag t is either not present, set to missing, or a number less than or equal to k.",paramNames=Seq("t","k"),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = string2double(params(1));
                          (a : (SVcfVariantLine,Int)) => {
                            val value = getTag(tag,a);
                            value.isEmpty || string2double(value.get) <= v ;
                          }
                        }
                      ),
        FilterFunction(funcName="TAGPAIR.match",numParam=2,desc="TRUE iff the two tags t1 and t2 are both present and not set to missing, and are equal to one another.",paramNames=Seq("t1","t2"),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          val tag1 = params(0);
                          val tag2 = params(1);
                          (a : (SVcfVariantLine,Int)) => {
                            val t1 = getTag(tag1,a);
                            val t2 = getTag(tag2,a);
                            t1.isDefined && t2.isDefined && t1 == t2;
                          }
                        }
                      ),
                      
/*
         FilterFunction(funcName="INFO.inAnyOfN", numParam = -1,desc="TRUE iff INFO field t is a list delimited with commas, bars, slashes, OR COLONS, and contains any of the parameters k1,k2,...",paramNames=Seq("t","k1","k2","..."),paramTypes=Seq("info","string"),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = params.tail.toSet;
                          (a : SVcfVariantLine) => {
                            tagNonMissing(tag,a) && a.info(tag).get.split(",").flatMap{s => s.split("[:\\|/]")}.toSet.intersect(v).size > 0 ;
                          }
                        }
                      ),
 * 
 */
        FilterFunction(funcName="GTAG.inAnyOf",numParam=2,desc="TRUE iff the first parameter, a FORMAT field, is equal to any of the following parameters or is a list containing any of the following parameters, using commas, bars, or slashes as delimiters.",paramNames=Seq("t","k"),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = params.tail.toSet;
                          (a : (SVcfVariantLine,Int)) => {
                            val values = getTag(tag,a).toSeq.map{ s => s.split("[,\\|/]").toSeq }.flatten.toSet
                            ! v.intersect(values).isEmpty
                          }
                        }
                      ),
                      

        FilterFunction(funcName="GTAGARRAY.gt",numParam=3,desc="TRUE iff the tag t is present and not set to missing, and is a list with at least i elements, and the i-th element of which is greater than k.",paramNames=Seq("t","i","k"),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val idx = string2int(params(1));
                          val v = string2double(params(2));
                          (a : (SVcfVariantLine,Int)) => {
                            val value = getTag(tag,a);
                            if(value.isEmpty) false;
                            else {
                              val arr = value.get.split(",");
                              if(arr.length < idx) false;
                              else if(arr(idx) == ".") false;
                              else {
                                string2double(arr(idx)) > v;
                              }
                            }
                          }
                        }
                      ),
        FilterFunction(funcName="GTAGARRAY.lt",numParam=3,desc="TRUE iff the tag t is present and not set to missing, and is a list with at least i elements, and the i-th element of which is less than k.",paramNames=Seq("t","i","k"),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val idx = string2int(params(1));
                          val v = string2double(params(2));
                          (a : (SVcfVariantLine,Int)) => {
                            val value = getTag(tag,a);
                            if(value.isEmpty) false;
                            else {
                              val arr = value.get.split(",");
                              if(arr.length < idx) false;
                              else if(arr(idx) == ".") false;
                              else {
                                string2double(arr(idx)) < v;
                              }
                            }
                          }
                        }
                      ),
                      
                      
        FilterFunction(funcName="GTAGARRAYSUM.gt",numParam=2,desc="TRUE iff the tag t is present and not set to missing, and is a list of numbers the sum of which is greater than k.",paramNames=Seq("t","k"),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = string2double(params(1));
                          (a : (SVcfVariantLine,Int)) => {
                            val value = getTag(tag,a);
                            if(value.isEmpty) false;
                            else {
                              value.get.split(",").map{(s) => if(s == ".") 0.toDouble else string2double(s)}.sum > v
                            }
                          }
                        }
                      ),
        FilterFunction(funcName="GTAGARRAYSUM.lt",numParam=2,desc="TRUE iff the tag t is present and not set to missing, and is a list of numbers the sum of which is greater than k.",paramNames=Seq("t","k"),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = string2double(params(1));
                          (a : (SVcfVariantLine,Int)) => {
                            val value = getTag(tag,a);
                            if(value.isEmpty) false;
                            else {
                              value.get.split(",").map{(s) => if(s == ".") 0.toDouble else string2double(s)}.sum < v
                            }
                          }
                        }
                      ),
        FilterFunction(funcName="GTAG.isHet",numParam=1,desc="TRUE iff the tag t, which must be a genotype-style-formatted field, is present and not set to missing and is heterozygous.",paramNames=Seq("t"),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          (a : (SVcfVariantLine,Int)) => {
                            val v = getTag(tag,a);
                            if(v.isEmpty) false;
                            else {
                              val g = v.get.split("[/\\|]");
                              g.contains("1") && g.exists{ x => x != "1" }
                            }
                          }
                        }
                      ),
        FilterFunction(funcName="GTAG.isCleanHet",numParam=1,desc="TRUE iff the tag t, which must be a genotype-style-formatted field, is present and not set to missing and is heterozygous between the alt and reference allele.",paramNames=Seq("t"),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          (a : (SVcfVariantLine,Int)) => {
                            val v = getTag(tag,a);
                            if(v.isEmpty) false;
                            else {
                              val g = v.get.split("[/\\|]");
                              g.contains("1") && g.contains("0")
                            }
                          }
                        }
                      ),
        FilterFunction(funcName="GTAG.isAnyAlt",numParam=1,desc="TRUE iff the tag t, which must be a genotype-style-formatted field, is present and not set to missing and contains the alt allele.",paramNames=Seq("t"),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          (a : (SVcfVariantLine,Int)) => {
                            val v = getTag(tag,a);
                            if(v.isEmpty) false;
                            else {
                              val g = v.get.split("[/\\|]");
                              g.contains("1")
                            }
                          }
                        }
                      ),
        FilterFunction(funcName="GTAG.isHomRef",numParam=1,desc="TRUE iff the tag t, which must be a genotype-style-formatted field, is present and not set to missing and is homozygous-reference.",paramNames=Seq("t"),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          (a : (SVcfVariantLine,Int)) => {
                            val v = getTag(tag,a);
                            if(v.isEmpty) false;
                            else {
                              val g = v.get.split("[/\\|]");
                              ! g.exists{ x => x != "0"}
                            }
                          }
                        }
                      ),
        FilterFunction(funcName="GTAG.isHomAlt",numParam=1,desc="TRUE iff the tag t, which must be a genotype-style-formatted field, is present and not set to missing and is homozygous-alt.",paramNames=Seq("t"),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          (a : (SVcfVariantLine,Int)) => {
                            val v = getTag(tag,a);
                            if(v.isEmpty) false;
                            else {
                              val g = v.get.split("[/\\|]");
                              ! g.exists{ x => x != "1"}
                            }
                          }
                        }
                      ),
                      
        FilterFunction(funcName="SAMPGRP.in",numParam=1,desc="TRUE iff the sample is a member of group g.",paramNames=Seq("g"),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          val g = params(0);
                          (a : (SVcfVariantLine,Int)) => {
                            a._1.genotypes.idxIsGrp( a._2, g );
                          }
                        }
                      ),

        FilterFunction(funcName="GTAG.altProportion.lt",numParam=2,desc="TRUE iff the tag t, which must be a AD-style-formatted field, has an observed alt-allele-frequency less than k.",paramNames=Seq("t","k"),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          val tag = params(0);
                          val v = string2double(params(1));
                          (a : (SVcfVariantLine,Int)) => {
                            val value = getTag(tag,a);
                            if(value.isEmpty) false;
                            else {
                              val varray = value.get.split(",").map{(s) => if(s == ".") 0.toDouble else string2double(s)};
                              val total = varray.sum;
                              if(total == 0){
                                false;
                              } else {
                                varray(1) / total < v
                              }
                            }
                          }
                        }
                      ),
        FilterFunction(funcName="GTAG.altProportion.gt",numParam=2,desc="TRUE iff the tag t, which must be a AD-style-formatted field, has an observed alt-allele-frequency greater than k.",paramNames=Seq("t","k"),paramTypes=Seq(),isHidden = true,
                        metaFunc=(params : Seq[String]) => {
                          val tag = params(0);
                          val v = string2double(params(1));
                          (a : (SVcfVariantLine,Int)) => {
                            val value = getTag(tag,a);
                            if(value.isEmpty) false;
                            else {
                              val varray = value.get.split(",").map{(s) => if(s == ".") 0.toDouble else string2double(s)};
                              val total = varray.sum;
                              if(total == 0){
                                false;
                              } else {
                                varray(1) / total > v
                              }
                            }
                          }
                        }
                      ),
        FilterFunction(funcName="GTAG.SC.altProportion.lt",numParam=3,desc="TRUE iff the tag t, which must be a single-caller-AD-style-formatted field, has an observed alt-allele-frequency greater than k.",paramNames=Seq("splitIdxTag","t","v"),paramTypes=Seq(),isHidden = true,
                        metaFunc=(params : Seq[String]) => {
                          val splitIdxTag = params(0);
                          val adTag = params(1);
                          val fracThresh = string2double(params(2));
                          (a : (SVcfVariantLine,Int)) => {
                            val alleIdxRaw = getTag(splitIdxTag,a);
                            val value = getTag(adTag,a);
                            if(alleIdxRaw.isEmpty || alleIdxRaw == "." || value.isEmpty){
                              false;
                            } else {
                              val alleIdx = string2int(alleIdxRaw.get);
                              val varray = value.get.split(",").map{(s) => if(s == ".") 0.toDouble else string2double(s)};
                              val total = (varray(alleIdx) + varray(0)).toDouble;
                              if(total == 0){
                                false;
                              } else {
                                ( varray(alleIdx) / total ) < fracThresh
                              }
                            }
                          }
                        }
                      ),   
        FilterFunction(funcName="GTAG.SC.altProportion.gt",numParam=3,desc="TRUE iff the tag t, which must be a single-caller-AD-style-formatted field, has an observed alt-allele-frequency greater than k.",paramNames=Seq("splitIdxTag","t","v"),paramTypes=Seq(),isHidden = true,
                        metaFunc=(params : Seq[String]) => {
                          val splitIdxTag = params(0);
                          val adTag = params(1);
                          val fracThresh = string2double(params(2));
                          (a : (SVcfVariantLine,Int)) => {
                            val alleIdxRaw = getTag(splitIdxTag,a);
                            val value = getTag(adTag,a);
                            if(alleIdxRaw.isEmpty || alleIdxRaw == "." || value.isEmpty){
                              false;
                            } else {
                              val alleIdx = string2int(alleIdxRaw.get);
                              val varray = value.get.split(",").map{(s) => if(s == ".") 0.toDouble else string2double(s)};
                              val total = (varray(alleIdx) + varray(0)).toDouble;
                              if(total == 0){
                                false;
                              } else {
                                ( varray(alleIdx) / total ) > fracThresh
                              }
                            }
                          }
                        }
                      ),
        FilterFunction(funcName="GTAG.altDepthForAlle.gt",numParam=3,desc="TRUE iff for AD-style tag ad and GT-style tag gt, the sample is called as having an allele K while having less than v reads covering said allele.",paramNames=Seq("gt","ad","v"),paramTypes=Seq(),
                        (params : Seq[String]) => {
                          val gtTag = params(0);
                          val adTag = params(1);
                          val thresh = string2int(params(2));
                          (a : (SVcfVariantLine,Int)) => {
                            val gtRaw : String = getTag(gtTag,a).getOrElse(".");
                            if(! gtRaw.contains('.')){
                              val gtSet : Set[Int] = gtRaw.split("[\\|/]").toSet.map{ (g : String) => string2int(g)} - 0
                              if(gtSet.size == 0){
                                true
                              } else {
                                getTag(adTag,a) match {
                                  case Some(ad) => {
                                    val adArray = ad.split(",");
                                    if(! adArray.isDefinedAt(gtSet.max)){
                                      warning("WARNING: ADtag \""+adTag+"\" does not have enough entries to match GTtag \""+gtTag+"\"on line:\n   \""+a._1.getSimpleVcfString()+"\"","GTAGaltDepthForAllegt_BAD_GT_AD_TAGS",10);
                                      false;
                                    }
                                    gtSet.foldLeft{true}{ case (soFar,g) => {
                                      soFar && adArray(g) != "." && string2int(adArray(g)) > thresh
                                    }}
                                  }
                                  case None => false;
                                }
                              }
                            } else {
                              true
                            }
                          }
                        }
                      ), 
        FilterFunction(funcName="GTAG.SC.altDepth.lt",numParam=3,desc="TRUE iff the tag t, which must be a single-caller-AD-style-formatted field, has an observed alt-allele-frequency greater than k.",paramNames=Seq("splitIdxTag","t","v"),paramTypes=Seq(),isHidden=true,
                        metaFunc=(params : Seq[String]) => {
                          val splitIdxTag = params(0);
                          val adTag = params(1);
                          val fracThresh = string2double(params(2));
                          (a : (SVcfVariantLine,Int)) => {
                            val alleIdxRaw = getTag(splitIdxTag,a);
                            val value = getTag(adTag,a);
                            if(alleIdxRaw.isEmpty || alleIdxRaw == "." || value.isEmpty){
                              false;
                            } else {
                              val alleIdx = string2int(alleIdxRaw.get);
                              val varray = value.get.split(",").map{(s) => if(s == ".") 0.toDouble else string2double(s)};
                              val total = (varray(alleIdx) + varray(0)).toDouble;
                              varray(alleIdx) < fracThresh
                            }
                          }
                        }
                      ),   
        FilterFunction(funcName="GTAG.SC.altDepth.gt",numParam=3,desc="TRUE iff the tag t, which must be a single-caller-AD-style-formatted field, has an observed alt-allele-frequency greater than k.",paramNames=Seq("splitIdxTag","t","v"),paramTypes=Seq(),isHidden = true,
                        metaFunc=(params : Seq[String]) => {
                          val splitIdxTag = params(0);
                          val adTag = params(1);
                          val fracThresh = string2double(params(2));
                          (a : (SVcfVariantLine,Int)) => {
                            val alleIdxRaw = getTag(splitIdxTag,a);
                            val value = getTag(adTag,a);
                            if(alleIdxRaw.isEmpty || alleIdxRaw == "." || value.isEmpty){
                              false;
                            } else {
                              val alleIdx = string2int(alleIdxRaw.get);
                              val varray = value.get.split(",").map{(s) => if(s == ".") 0.toDouble else string2double(s)};
                              varray(alleIdx) > fracThresh
                            }
                          }
                        }
                      ),
                      
        FilterFunction(funcName="TRUE",numParam=0,desc="Always pass",paramNames=Seq(),paramTypes=Seq(),
                        metaFunc=(params : Seq[String]) => {
                          (a : (SVcfVariantLine,Int)) => {
                            true;
                          }
                        }
                      ),
        FilterFunction(funcName="FALSE",numParam=0,desc="Never pass",paramNames=Seq(),paramTypes=Seq(),
                        metaFunc=(params : Seq[String]) => {
                          (a : (SVcfVariantLine,Int)) => {
                            false;
                          }
                        }
                      ),
        FilterFunction(funcName="VAREXPR",numParam = -1,desc="Variant passes variant-level logical function. Note that you cannot include AND/OR/NOT inside this logical function, it must be a single variant-level logical function.",paramNames=Seq("fcn","params"), paramTypes=Seq(),
                        (params : Seq[String]) => {
                          val gtFiltName = params.head;
                          val gtParams = params.tail;                          
                          val (ff,vff) = sVcfFilterLogicParser.getFilterFunction(gtFiltName, gtParams)
                          (a : (SVcfVariantLine,Int)) => {
                            ff(a._1);
                          }
                        }
                      ),
             FilterFunction(funcName="VARIANT",numParam = -1, desc = "Equivalent to VAREXPR.",paramNames=Seq("fcn","params"), paramTypes=Seq(),isHidden=true,
                        metaFunc=(params : Seq[String]) => {
                          val varfcn = sVcfFilterLogicParser.filterFunctionMap(params.head);
                          val ffn = varfcn.metaFunc(params.tail);
                          (a : (SVcfVariantLine,Int)) => {
                            ffn(a._1);
                          }
                        }
                      )
                      
    ); 
    /*
                   val expr = paramValues.head;
              val parser : SFilterLogicParser[SVcfVariantLine] = internalUtils.VcfTool.sVcfFilterLogicParser;
              val filter : SFilterLogic[SVcfVariantLine] = parser.parseString(expr);
             FilterFunction(funcName="VARIANT",numParam=-1, desc = "Variant passes variant-level logical function. Note that you cannot include AND/OR/NOT inside this logical function, it must be a single variant-level logical function.",paramNames=Seq("fcn","params"), paramTypes=Seq(),
                        metaFunc=(params : Seq[String]) => { 
                          (a : (SVcfVariantLine,Int)) => {
                            false
                          }
                        }
                      )
     */
    
    val filterFunctionMapVal : Map[String,FilterFunction[(SVcfVariantLine,Int)]] = {
      filterFunctionSet.map{ ff => {
        (ff.funcName,ff)
      }}.toMap
    }
    def filterFunctionSet : Set[FilterFunction[(SVcfVariantLine,Int)]] = filterFunctionSetVal;
    def filterFunctionMap : Map[String,FilterFunction[(SVcfVariantLine,Int)]] = filterFunctionMapVal;
  }
  
  /*
          val alleIdx : Int = v.info.getOrElse(tagSingleCallerAlles.get,None) match {
            case Some(tval) => {
              if(tval == "."){
                -1;
              } else {
                string2int(tval);
              }
            }
            case None => -1;
          }
          val adIdx = v.genotypes.fmt.indexOf(tagAD.get);
          if(alleIdx != -1 && adIdx != -1){
            val ads = v.genotypes.genotypeValues(adIdx).map{ a => {
              if(a == "."){
                (-1,-1)
              } else {
                val cells = a.split(",").map{aa => if(aa == ".") -1 else string2int(aa)};
                if(cells.length == 2 && alleIdx == 0){
                  (cells(0), cells(1))
                } else {
                  if(! cells.isDefinedAt(alleIdx + 1)){
                    warning("Malformed "+tagAD.get+" tag: altAlle="+altAlle+", alleIdx="+alleIdx+", a="+a+", cells=["+cells.mkString(",")+"]"+"\n   on variant: "+v.getSimpleVcfString(),"MALFORMED_ADTAG_"+tagAD.get,10);
                  }
                  (cells.zipWithIndex.map{case (aa,i) => if(i == alleIdx + 1) 0 else aa}.sum,cells(alleIdx + 1))
                }
              }
            }} 
   
   */
  
  /*val vcfOpBinaryFuncMap :  Map[String,((Seq[String]) => ((SVcfVariantLine,String,String) => Boolean))] = Map[String,((Seq[String]) => ((SVcfVariantLine,String,String) => Boolean))](
        ("INFO:eq",(params : Seq[String]) => {
          ((a : SVcfVariantLine, v1 : String, v2 : String) => {
            a.info.containsKey(v1) && a.info(v1).get == v2;
          })
        })
      );*/
  
/*
         ("INFO:ne",(a : SVcfVariantLine, v1 : String, v2 : String) => {
          (! a.info.containsKey(v1)) || a.info(v1).get != v2;
        }),
        ("INFO:nm",(a : SVcfVariantLine, v1 : String, v2 : String) => {
          a.info.containsKey(v1) && a.info(v1).get != "."
        }),
        ("INFO:m",(a : SVcfVariantLine, v1 : String, v2 : String) => {
          (! a.info.containsKey(v1)) || a.info(v1).get == "."
        }),
        ("INFO:gt",(a : SVcfVariantLine, v1 : String, v2 : String) => {
          a.info.containsKey(v1) && a.info(v1).get != "." && string2double(a.info(v1).get) > string2double(v2);
        }),
        ("INFO:lt",(a : SVcfVariantLine, v1 : String, v2 : String) => {
          a.info.containsKey(v1) && a.info(v1).get != "." && string2double(a.info(v1).get) < string2double(v2);
        }),
        ("INFO:gtm",(a : SVcfVariantLine, v1 : String, v2 : String) => {
          (! a.info.containsKey(v1)) || a.info(v1).get == "." || string2double(a.info(v1).get) > string2double(v2);
        }),
        ("INFO:ltm",(a : SVcfVariantLine, v1 : String, v2 : String) => {
          a.info.containsKey(v1) && string2double(a.info(v1).get) < string2double(v2);
        }),
        ("INFO:ge",(a : SVcfVariantLine, v1 : String, v2 : String) => {
          a.info.containsKey(v1) && a.info(v1).get != "." && string2double(a.info(v1).get) >= string2double(v2);
        }),
        ("INFO:le",(a : SVcfVariantLine, v1 : String, v2 : String) => {
          a.info.containsKey(v1) && a.info(v1).get != "." && string2double(a.info(v1).get) <= string2double(v2);
        }),
        ("INFO:gem",(a : SVcfVariantLine, v1 : String, v2 : String) => {
          (! a.info.containsKey(v1)) || a.info(v1).get == "." || string2double(a.info(v1).get) >= string2double(v2);
        }),
        ("INFO:lem",(a : SVcfVariantLine, v1 : String, v2 : String) => {
          (! a.info.containsKey(v1)) || a.info(v1).get == "." || string2double(a.info(v1).get) <= string2double(v2);
        }),
        ("INFO:in",(a : SVcfVariantLine, v1 : String, v2 : String) => {
          a.info.containsKey(v1) && a.info(v1).get != "." && a.info(v1).get.split(",").contains(v2)
        }),
        ("INFO:notIn",(a : SVcfVariantLine, v1 : String, v2 : String) => {
          (! a.info.containsKey(v1)) || a.info(v1).get == "." || (! a.info(v1).get.split(",").contains(v2))
        }),
        ("FILTER:eq",(a : SVcfVariantLine, v1 : String, v2 : String) => {
          a.filter == v2;
        })
 */
  
  //val SVcfFilterParser = new SFilterLogicParser[SVcfVariantLine](vcfOpBinaryFuncMap);
  
  
  //def trimAlign(vc : VariantContext,
  
  
}
























