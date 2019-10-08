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

import internalUtils.VcfTool._;


import htsjdk.variant._;
import htsjdk.variant.variantcontext._;
import htsjdk.variant.vcf._;

import internalUtils.genomicUtils._;
import internalUtils.commonSeqUtils._;

object ClinVarAssertUtil {



def cleanForInfoLevel2(s : String) : String = {
  s.replaceAll("['\"]","").replaceAll("[ =/;]+","_").replaceAll("[_]*[|,][_]*","|").replaceAll("[^a-zA-Z0-9_/|]","").replaceAll("^[_]+|[_]+$","")
}
def cleanForInfoLevel3(s : String) : String = {
  s.replaceAll("[ =/]+","_").replaceAll("[_]*[:|,;][_]*","/").replaceAll("[^a-zA-Z0-9_/]","").replaceAll("^[_]+|[_]+$","")
}

case class ClinVarAssertInfo(
    accessionID : String,date : String,
    orgID : String,orgType : String,orgName : String,
    recordStatus: String,origins : String,methods : String,
    interpretation : String,
    interpretationDate : String,
    disease : Seq[String],
    diseaseLookup : Seq[String] = Seq(),
    diseaseMEDGEN : Seq[String] = Seq(),
    alleID : String = ".",
    varID : String = ".",
    hapID : String = ".",
    isMultiVarHaplo : Int = -1
   ){
     def getSimpleString(delim : String = "|") : String = {
       Seq(accessionID,date,orgID,orgType,orgName,recordStatus,origins,methods,interpretation,interpretationDate,disease.mkString("/"),
           alleID,varID,hapID,isMultiVarHaplo+"",
              diseaseLookup.padTo(1,".").mkString("/"), diseaseMEDGEN.padTo(1,".").mkString("/")).map{x => {
         cleanForInfoLevel3(x)
       }}.mkString(delim);
     }
     def getAdvString(delim : String = "|", aid : String, vid : String) : String = {
       Seq(   accessionID,date,orgID,orgType,orgName,recordStatus,origins,methods,interpretation,interpretationDate,disease.mkString("/"),
              aid,vid,hapID,isMultiVarHaplo+"",
              diseaseLookup.padTo(1,".").mkString("/"), diseaseMEDGEN.padTo(1,".").mkString("/")
           ).map{x => {
              cleanForInfoLevel3(x)
           }}.mkString(delim);
     }
     
}


case class ClinVarAlle(
     chrom : String, pos : String, ref : String, alt : String,
     varID : String = ".", alleID : String = "."
   ){
   
  def alleString(delim : String = "-") : String = {
    Seq(chrom,pos,ref,alt).mkString(delim);
  }
  def cvaString(delim : String = "-") : String = {
    Seq(chrom,pos,ref,alt,alleID,varID).mkString(delim);
  }
}
//object ClinVarAlle {
  def getClinVarAlle(vc : SVcfVariantLine, varID : String, alleID : String) : ClinVarAlle = {
    new ClinVarAlle(chrom=vc.chrom,pos=vc.pos.toString,ref=vc.ref,alt=vc.alt.head,varID=varID,alleID=alleID);
  }
//}

val DEFAULT_badgeIdList = Set("61756","25969","1012","505952","505698","505849","320494","500060","26957","505504","504895","500026","500031","21766","506098","500110","1238")
val effectScoreSet = Set("PATHOGENIC","LIKELY_PATHOGENIC","BENIGN","LIKELY_BENIGN","UNCERTAIN_SIGNIFICANCE");

val EFFECT_CLINASSERT_SET = Set("PATHOGENIC","LIKELY_PATHOGENIC","BENIGN","LIKELY_BENIGN","UNCERTAIN_SIGNIFICANCE");
val LEGAL_CLINASSERT_SET = Set("Affects","Pathogenic","Likely pathogenic","drug response","confers sensitivity","risk factor","other","association","Uncertain significance","Likely benign","association not found","Benign","protective","not provided","conflicting data from submitters").map{s => s.toUpperCase.replaceAll(" ","_")}


case class ClinVarMetadata(
   hapID : Option[String],
   vcfLocus : Seq[ClinVarAlle],
   assertList : Seq[ClinVarAssertInfo]
      ){
   val badgeIdList = DEFAULT_badgeIdList;
   def mainID : String = {
     hapID.getOrElse(vcfLocus.headOption.map{cva => cva.varID}.getOrElse("."));
   }
   def getOutputStrings(delim : String = "|", subDelim : String = ":") : Seq[String] = {
      vcfLocus.map{cva => {
        cva.chrom+"\t"+cva.pos+"\t.\t"+cva.ref+"\t"+cva.alt+"\t.\t.\t"+
        (Seq(("metadata_varID",""+cva.varID),
        ("metadata_alleID",""+cva.alleID),
        ("metadata_rawdata",""+assertList.map{ cvInfo => {
          cvInfo.getSimpleString(delim=subDelim);
        }}.mkString(delim))
        ).map{ case (t,v) => {
          v
        }}).mkString("\t")
      }}
   }
   def getVCFStrings(delim : String = "|", subDelim : String = ":") : Seq[String] = {
      vcfLocus.map{cva => {
        val varid = cva.varID;
        val alleid = cva.alleID;
        
        cva.chrom+"\t"+cva.pos+"\t.\t"+cva.ref+"\t"+cva.alt+"\t.\t.\t"+
        (Seq(("varID",""+cva.varID),
        ("alleID",""+cva.alleID),
        ("rawdata",""+assertList.map{ cvInfo => {
          cvInfo.getAdvString(delim=subDelim, aid=alleid,vid=varid);
        }}.mkString(delim))
        ).map{ case (t,v) => {
          t+"="+v
        }}).mkString(";")
      }}
   }
}

  def getClinVarAssertFromString(s : String, subDelim : String = ":") : ClinVarAssertInfo = {
    val cells = s.split(subDelim);
    if(cells.length < 10){
      warning("ClinVar Assert String too short: \n"+
              "   string= \""+s+"\"\n"+
              "   with delim=\""+subDelim+"\"","assertStringTooShort",10);
    }
    val accessionID = cells(0);
    val date = cells(1);
    val orgID = cells(2);
    val orgType = cells(3);
    val orgName = cells(4)
    val recordStatus=cells(5);
    val origins = cells(6);
    val methods = cells(7)
    val interpretation = cells(8);
    val interpretationDate = cells(9);
    val disease = cells(10).split("/").toSeq
    
    val alleID = cells(11);
    val varID = cells(12);
    val hapID = cells(13);
    val isMultiVarHaplo = string2int(cells(14));
    
    val diseaseLookup = cells.lift(15).toSeq.flatMap{ x => x.split("/").toSeq }
    val diseaseMEDGEN = cells.lift(16).toSeq.flatMap{ x => x.split("/").toSeq }
    
    new ClinVarAssertInfo(
      accessionID =accessionID,date =date,
      orgID =orgID,orgType =orgType,orgName =orgName,
      recordStatus=recordStatus,origins =origins,methods =methods,
      interpretation =interpretation,
      interpretationDate =interpretationDate,
      disease =disease,
      alleID = alleID,
      varID = varID,
      hapID = hapID,
      isMultiVarHaplo = isMultiVarHaplo,
      diseaseLookup = diseaseLookup,
      diseaseMEDGEN = diseaseMEDGEN
    )
  }
  def getClinVarAssertSeqFromString(s : String, delims : String = "[|,]", subDelim : String = ":") : Seq[ClinVarAssertInfo] = {
    s.split(delims).toSeq.withFilter{ ss => ss != "." && ss != "" }.map{ss => {
      getClinVarAssertFromString(ss,subDelim);
    }}
  }

  
  ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// //////////////////
    ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// //////////////////
  ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// //////////////////
  ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// //////////////////
  ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// //////////////////
  ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// //////////////////
  ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// //////////////////
  ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// //////////////////
  ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// //////////////////
  ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// ////////////////// //////////////////

  
  
class ClinVarAssessor(tagPrefix : String = "", badgeIdList : Set[String] = DEFAULT_badgeIdList) extends SVcfWalker {
  val rawDataTag = tagPrefix+"rawdata"
  val alleIdTag = tagPrefix+"alleID";
  val varIdTag = tagPrefix+"varID"
  
  val effectScoreSet = Set("PATHOGENIC","LIKELY_PATHOGENIC","BENIGN","LIKELY_BENIGN","UNCERTAIN_SIGNIFICANCE");
    def isGermline( cva : ClinVarAssertInfo) : Boolean = {
      cva.origins.toUpperCase == "GERMLINE"
    }
    def isGermlineOrUnk( cva : ClinVarAssertInfo) : Boolean = {
      cva.origins.toUpperCase == "GERMLINE" || cva.origins.toUpperCase == "UNKNOWN"
    }
    def isBadged( cva : ClinVarAssertInfo) : Boolean = {
      badgeIdList.contains(cva.orgID)
    }
    
    val subsetFuncs : Seq[(String,String, ClinVarAssertInfo => Boolean)] = Seq(
        ("","",( (cva : ClinVarAssertInfo) => true )),
        ("germLine_"," origin eq germline",( (cva : ClinVarAssertInfo) => isGermline(cva) )),
        ("germOrUnk_"," origin eq germline or unknown", ( (cva : ClinVarAssertInfo) => isGermlineOrUnk(cva) )),
        ("NH_","out haplotype assertions", ( (cva : ClinVarAssertInfo) => cva.isMultiVarHaplo == 0 )),
        ("HAPLO_"," ONLY haplotype assertions", ( (cva : ClinVarAssertInfo) => cva.isMultiVarHaplo > 0 ))
      ).flatMap{ case (ssName,ssDesc, f) => {
        val (allDescFmt,bdgDescFmt) = if(ssDesc == ""){
          ("(All reports)","(Badged lab reports only)")
        } else {
          ("(Restricted to: reports with"+ssDesc+")",
           "(Restricted to: badged lab reports with"+ssDesc+")")
        }
        
        Seq(
            (ssName,allDescFmt,f),
            ("badged_"+ssName,bdgDescFmt, ((cva : ClinVarAssertInfo) => f(cva) && isBadged(cva)))
        );
      }}
    
    def walkerName : String = "ClinVarAssessor"
    def walkerParams : Seq[(String,String)] = Seq[(String,String)]();
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {

      val outHeader = vcfHeader.copyHeader
      
      subsetFuncs.foreach{ case (ssName,ssDesc,ssFunc) => {
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tagPrefix+ssName+"numVUS",Number="1",Type="Integer",   desc=ssDesc+" Num reports asserting Unknown Significance status."));
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tagPrefix+ssName+"numP",Number="1",Type="Integer",     desc=ssDesc+" Num reports asserting Pathogenic status"));
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tagPrefix+ssName+"numLP",Number="1",Type="Integer",    desc=ssDesc+" Num reports asserting Likely Pathogenic status."));
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tagPrefix+ssName+"numB",Number="1",Type="Integer",     desc=ssDesc+" Num reports asserting Benign status."));
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tagPrefix+ssName+"numLB",Number="1",Type="Integer",    desc=ssDesc+" Num reports asserting Likely Benign status."));
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tagPrefix+ssName+"numMisc",Number="1",Type="Integer",  desc=ssDesc+" Num reports asserting any status other than VUS,P,LP,B,LB."));
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tagPrefix+ssName+"numMalformat",Number="1",Type="Integer",  desc=ssDesc+" Num reports asserting an illegal status string. Possibly a misspelling or similar."));

        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tagPrefix+ssName+"num",Number="1",Type="Integer",      desc=ssDesc+" Num reports."));
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tagPrefix+ssName+"originSet",Number=".",Type="String", desc=ssDesc+" Set of all observed reported origins."));
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tagPrefix+ssName+"assertSet",Number=".",Type="String", desc=ssDesc+" The set of all observed pathogenicity assertion levels."));
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tagPrefix+ssName+"isPerfAgree",Number="1",Type="Integer", desc=ssDesc+" Equal to 1 iff all non-misc reports have the exact same asserted status."));
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tagPrefix+ssName+"perfAgree",Number="1",Type="String", desc=ssDesc+" Equal to the agreed-upon assertion level iff all non-misc reports have the exact same asserted status. Otherwise set to missing."));
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tagPrefix+ssName+"call",Number="1",Type="String", desc=ssDesc+" Composite pathogenicity call. Considering only badged lab reports, "+
                                                                                                                                  "status is decided in two stages. First status is determined between "+
                                                                                                                                  "three main categories of P/LP, B/LB, and VUS by plurality rules "+
                                                                                                                                  "(ie, whichever category has the most reports). Ties go to VUS. For P/LP "+
                                                                                                                                  "and B/LB, the final status is decided by majority between the two options, "+
                                                                                                                                  "with ties going to the LB or LP subcategory."));
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tagPrefix+ssName+"warn_majorConflict",Number="1",Type="String", desc=ssDesc+" Equal to 1 iff reports include a mix of opposing pathogenicity assertions. Ie: B/LB and P/LP."));
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tagPrefix+ssName+"warn_minorConflict",Number="1",Type="String", desc=ssDesc+" Equal to 1 iff reports include a mix of VUS and non-VUS pathogenicity assertions. For example: P or LP and VUS"));
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tagPrefix+ssName+"warn_minorConflictP",Number="1",Type="String", desc=ssDesc+" Equal to 1 iff reports include a mix of VUS and PLP pathogenicity assertions."));
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tagPrefix+ssName+"warn_minorConflictB",Number="1",Type="String", desc=ssDesc+" Equal to 1 iff reports include a mix of VUS and BLB pathogenicity assertions."));
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tagPrefix+ssName+"warn_tieBrokenByDate",Number="1",Type="Integer", desc=ssDesc+" Equal to 1 iff the TBbD call relied on tiebreaking by most date of last update."));
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tagPrefix+ssName+"warn_any",Number="1",Type="Integer", desc=ssDesc+" Equal to 1 iff any of the warnings are 1."));

        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tagPrefix+ssName+"warn_malformatSet",Number=".",Type="String", desc=ssDesc+" Set of illegal assertion desc strings."));

        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tagPrefix+ssName+"TBbD_call",Number="1",Type="String", desc=ssDesc+" Composite pathogenicity call, with Ties Broken by Date (TBbD). Considering only badged lab reports, "+
                                                                                                                                  "status is decided in two stages. First status is determined between "+
                                                                                                                                  "three main categories of P/LP, B/LB, and VUS by plurality rules "+
                                                                                                                                  "(ie, whichever category has the most reports). For P/LP "+
                                                                                                                                  "and B/LB, the final status is decided by majority between the two options. "+
                                                                                                                                  "Whenever there is a tie at either step, the tie is broken by taking the most "+
                                                                                                                                  "recently updated call from among the options that are tied."));

        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tagPrefix+ssName+"mostRecentCall",Number="1",Type="String",ssDesc + " the most recently updated ClinAssert with a legal non-misc call."))
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tagPrefix+ssName+"mostRecentDate",Number="1",Type="String",ssDesc + " the date of the most recently updated ClinAssert with a legal non-misc call."))
        
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tagPrefix+ssName+"diseaseSet",Number=".",Type="String", desc=ssDesc+" Set of all observed reported diseases, when disease is listed by name."))
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tagPrefix+ssName+"diseaseMedgen",Number=".",Type="String", desc=ssDesc+" Set of all observed reported disease MedGen IDs."))
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tagPrefix+ssName+"diseaseCrossref",Number=".",Type="String", desc=ssDesc+" Set of all observed reported disease listings, when listed by cross-reference."))

        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tagPrefix+ssName+"diseaseSetPLP",Number=".",Type="String", desc=ssDesc+" Set of all observed reported diseases, when disease is listed by name in reports labelled Pathogenic or Likely Pathogenic."))
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tagPrefix+ssName+"diseaseMedgenPLP",Number=".",Type="String", desc=ssDesc+" Set of all observed reported disease MedGen IDs, in reports labelled Pathogenic or Likely Pathogenic."))
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tagPrefix+ssName+"diseaseCrossrefPLP",Number=".",Type="String", desc=ssDesc+" Set of all observed reported disease listings, when listed by cross-reference in reports labelled Pathogenic or Likely Pathogenic."))

      }}

      def getAssertLevel(a : ClinVarAssertInfo) : String = {
        if(a.interpretation == "UNCERTAIN_SIGNIFICANCE"){ "VUS";
        } else if(a.interpretation == "BENIGN"){ "B";
        } else if(a.interpretation == "LIKELY_BENIGN"){ "LB";
        } else if(a.interpretation == "PATHOGENIC"){ "P";
        } else if(a.interpretation == "LIKELY_PATHOGENIC"){ "LP";
        } else { "MISC";
        }
      }
      
      (vcMap(vcIter){ v => {
        val vc  = v.getOutputLine();
        val assertField = v.info.getOrElse(rawDataTag,None).getOrElse(".")
        val alleID = v.info.getOrElse(alleIdTag,None).getOrElse(".").split(",").toVector.distinct.sorted.mkString(",");
        val varID = v.info.getOrElse(varIdTag,None).getOrElse(".").split(",").toVector.distinct.sorted.mkString(",");
        vc.addInfo(alleIdTag,alleID);
        vc.addInfo(varIdTag, varID);
        
        if(assertField != "."){
          
          val assertDataFull = getClinVarAssertSeqFromString(assertField)
          
          subsetFuncs.foreach{ case (ssName,ssDesc, ssFunc) => {
              val assertData = assertDataFull.filter{cva => ssFunc(cva)}
              val (us,b,lb,p,lp,othr) = (
                  assertData.count{p => p.interpretation == "UNCERTAIN_SIGNIFICANCE"},
                  assertData.count{p => p.interpretation == "BENIGN"},
                  assertData.count{p => p.interpretation == "LIKELY_BENIGN"},
                  assertData.count{p => p.interpretation == "PATHOGENIC"},
                  assertData.count{p => p.interpretation == "LIKELY_PATHOGENIC"},
                  assertData.count{p => ! effectScoreSet.contains(p.interpretation)}
                )
              val bad = assertData.count{ p => ! LEGAL_CLINASSERT_SET.contains(p.interpretation) }
              val assertsSortedByDate = assertData.sortBy( a => a.interpretationDate ).map{a => getAssertLevel(a)}
              val assertsSortedByDateWithDate = assertData.sortBy( a => a.interpretationDate ).map{a => (getAssertLevel(a),a.interpretationDate)}
              val assertsPLP = assertData.filter{p => p.interpretation == "PATHOGENIC" || p.interpretation == "LIKELY_PATHOGENIC"} 
              
              val interpCts = Seq(us,b,lb,p,lp)
              val interpSeq = Seq(us,b,lb,p,lp).zip(Seq("VUS","B","LB","P","LP"));
              val (plp,blb) = (p + lp, b + lb);
              val interpSum = interpCts.sum
              
              //vc.addInfo(tagPrefix+"metadata_badged_perfAgree",     (if( interpSeq.filter(_._1 > 0).length == 1 ){ interpSeq.filter(_._1 > 0).head._2 } else { "." }));
              vc.addInfo(tagPrefix+ssName+"numVUS",""+us);
              vc.addInfo(tagPrefix+ssName+"numB",""+b);
              vc.addInfo(tagPrefix+ssName+"numLB",""+lb);
              vc.addInfo(tagPrefix+ssName+"numP",""+p);
              vc.addInfo(tagPrefix+ssName+"numLP",""+lp);
              vc.addInfo(tagPrefix+ssName+"numMisc",""+othr);
              vc.addInfo(tagPrefix+ssName+"numMalformat",""+bad);

              vc.addInfo(tagPrefix+ssName+"num",""+assertData.length);
              vc.addInfo(tagPrefix+ssName+"originSet",  assertData.map{_.origins}.toVector.distinct.sorted.padTo(1,".").mkString(","));
              val assertSet = interpSeq.filter{case (ct,lvl) => ct > 0}.map{_._2}.toSet
              vc.addInfo(tagPrefix+ssName+"assertSet",  assertSet.toVector.sorted.padTo(1,".").mkString(","));
              val badSet = assertData.map{p => p.interpretation}.filter{ p => ! LEGAL_CLINASSERT_SET.contains(p) }.toVector.distinct.sorted;
              vc.addInfo(tagPrefix+ssName+"warn_malformatSet",  badSet.padTo(1,".").mkString(","));
              
              vc.addInfo(tagPrefix+ssName+"diseaseSet",         assertData.toSet.flatMap{ (p : ClinVarAssertInfo) => p.disease.toSet       }.filter{aa => ! ( aa == "" || aa == ".")}.toVector.sorted.padTo(1,".").mkString(","));
              vc.addInfo(tagPrefix+ssName+"diseaseMedGen",      assertData.toSet.flatMap{ (p : ClinVarAssertInfo) => p.diseaseMEDGEN.toSet }.filter{aa => ! ( aa == "" || aa == ".")}.toVector.sorted.padTo(1,".").mkString(","));
              vc.addInfo(tagPrefix+ssName+"diseaseCrossref",    assertData.toSet.flatMap{ (p : ClinVarAssertInfo) => p.diseaseLookup.toSet }.filter{aa => ! ( aa == "" || aa == ".")}.toVector.sorted.padTo(1,".").mkString(","));
              
              vc.addInfo(tagPrefix+ssName+"diseaseSetPLP",      assertsPLP.toSet.flatMap{ (p : ClinVarAssertInfo) => p.disease.toSet       }.filter{aa => ! ( aa == "" || aa == ".")}.toVector.sorted.padTo(1,".").mkString(","));
              vc.addInfo(tagPrefix+ssName+"diseaseMedGenPLP",   assertsPLP.toSet.flatMap{ (p : ClinVarAssertInfo) => p.diseaseMEDGEN.toSet }.filter{aa => ! ( aa == "" || aa == ".")}.toVector.sorted.padTo(1,".").mkString(","));
              vc.addInfo(tagPrefix+ssName+"diseaseCrossrefPLP", assertsPLP.toSet.flatMap{ (p : ClinVarAssertInfo) => p.diseaseLookup.toSet }.filter{aa => ! ( aa == "" || aa == ".")}.toVector.sorted.padTo(1,".").mkString(","));

              vc.addInfo(tagPrefix+ssName+"isPerfAgree", (if(assertSet.size == 1) "1" else "0") );
              vc.addInfo(tagPrefix+ssName+"perfAgree", (if(assertSet.size == 1) assertSet.head else ".") );
              vc.addInfo(tagPrefix+ssName+"warn_majorConflict", (if(plp > 0 && blb > 0) "1" else "0") );
              vc.addInfo(tagPrefix+ssName+"warn_minorConflictP", (if(( us > 0 && plp > 0)) "1" else "0") );
              vc.addInfo(tagPrefix+ssName+"warn_minorConflictB", (if((us > 0 && blb > 0)) "1" else "0") );
              vc.addInfo(tagPrefix+ssName+"warn_minorConflict", (if((us > 0 && (plp > 0 || blb > 0))) "1" else "0") );
              vc.addInfo(tagPrefix+ssName+"call", (              if(interpSum == 0){
                                                                   "."
                                                                 } else if( plp > blb && plp > us ) {
                                                                   if(p > lp){
                                                                     "P"
                                                                   } else {
                                                                     "LP"
                                                                   }
                                                                 } else if( us >= blb && us >= plp ){
                                                                     "VUS"
                                                                 } else if( blb > plp && blb > us ) {
                                                                   if(b > lb){
                                                                     "B"
                                                                   } else {
                                                                     "LB"
                                                                   }
                                                                 } else {
                                                                   "ERROR"
                                                                 }));
              
              val mostRecentCall =  assertsSortedByDate.filter{ a => Set("P","LP","B","LB","VUS").contains(a)}.lastOption.getOrElse(".")
              vc.addInfo(tagPrefix+ssName+"mostRecentCall", assertsSortedByDate.filter{ a => Set("P","LP","B","LB","VUS").contains(a)}.lastOption.getOrElse("."));
              vc.addInfo(tagPrefix+ssName+"mostRecentDate", assertsSortedByDateWithDate.filter{ case (a,d) => Set("P","LP","B","LB","VUS").contains(a)}.lastOption.getOrElse( (".",".") )._2);
              
              val tbbdWarn = (if(interpSum == 0){
                                                                   "0"
                                                                 } else if( (plp > blb && plp > us) ) {
                                                                   if(p > lp){
                                                                     "0"
                                                                   } else if(p == lp){
                                                                     "1"
                                                                   } else {
                                                                     "0"
                                                                   }
                                                                 } else if( us > blb && us > plp ){ 
                                                                   "0"
                                                                 } else if( us >= blb && us >= plp ){
                                                                   if( plp == us && us > blb ){
                                                                     "1"
                                                                   } else if( us == plp && us == blb ){
                                                                     "1"
                                                                   } else {
                                                                     "1"
                                                                   }
                                                                 } else if( blb > plp && blb > us ) {
                                                                   if(b > lb){
                                                                     "0"
                                                                   } else if(b == lb){
                                                                     "1"
                                                                   } else {
                                                                     "0"
                                                                   }
                                                                 } else {
                                                                   "0"
                                                                 })
              vc.addInfo(tagPrefix+ssName+"warn_tieBrokenByDate", tbbdWarn);
              val anyWarn = if(tbbdWarn == "1" ||  plp > 0 && blb > 0 || (us > 0 && (plp > 0 || blb > 0)) || badSet.size > 0){
                "1"
              } else {
                "0"
              }
              vc.addInfo(tagPrefix+ssName+"warn_any", anyWarn);

              val tbbdCall = (if(interpSum == 0){
                                                                   "."
                                                                 } else if( (plp > blb && plp > us) ) {
                                                                   if(p > lp){
                                                                     "P"
                                                                   } else if(p == lp){
                                                                     assertsSortedByDate.filter{a => Set("P","LP").contains(a)}.last;
                                                                   } else {
                                                                     "LP"
                                                                   }
                                                                 } else if( us > blb && us > plp ){ 
                                                                   "VUS"
                                                                 } else if( us >= blb && us >= plp ){
                                                                   if( plp == us && us > blb ){
                                                                     assertsSortedByDate.filter{ a => Set("P","LP","VUS").contains(a)}.last
                                                                   } else if( us == plp && us == blb ){
                                                                     assertsSortedByDate.filter{ a => Set("P","LP","B","LB","VUS").contains(a)}.last
                                                                   } else {
                                                                     assertsSortedByDate.filter{ a => Set("B","LB","VUS").contains(a)}.last
                                                                   }
                                                                 } else if( blb > plp && blb > us ) {
                                                                   if(b > lb){
                                                                     "B"
                                                                   } else if(b == lb){
                                                                     assertsSortedByDate.filter{a => Set("B","LB").contains(a)}.last;
                                                                   } else {
                                                                     "LB"
                                                                   }
                                                                 } else {
                                                                   "ERROR"
                                                                 })
              //assertsSortedByDateWithDate
              vc.addInfo(tagPrefix+ssName+"TBbD_call", tbbdCall);
          }}
          
        }
        vc
      }}, outHeader)
    }
}

  
  
  
}









