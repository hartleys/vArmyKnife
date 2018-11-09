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

import org.broadinstitute.gatk.utils.contexts.ReferenceContext;
import htsjdk.samtools.Cigar;
import htsjdk.samtools.CigarElement;
import htsjdk.samtools.CigarOperator;
import org.broadinstitute.gatk.utils.refdata.RefMetaDataTracker;
import org.broadinstitute.gatk.utils.variant.GATKVariantContextUtils;
import org.broadinstitute.gatk.utils.sam.AlignmentUtils;
 
import VcfTool._;

object GatkPublicCopy {
  
  var REFERENCE_ALLELE_TOO_LONG_MSG = "Reference allele is too long";
  
/*
 * Note: the below function does not touch the genotypes. Genotypes may get messed up.
 */
  
  //TO DO:
  def makeSimpleVariantContext(vc : SVcfVariantLine) : VariantContext = {
    val alleles = (Seq[Allele](Allele.create( vc.ref, true) , Allele.create( vc.alt.head, false) )).asJava
    var vm =  ((new VariantContextBuilder()).chr(vc.chrom).start( vc.pos ).alleles(alleles))
    vm = vm.computeEndFromAlleles(alleles, vc.pos).noID().unfiltered()
    
    vm.make();
  }
  

  
  def makeSimpleSVcfLine(vc : VariantContext, orig : Option[SVcfVariantLine] = None) : SVcfOutputVariantLine = {
     val alt = Seq[String](vc.getAlternateAllele(0).getBaseString()) ++ ( orig match {
       case Some(v) => {
         v.alt.tail;
       }
       case None => Seq[String]();
     })
     val (id,qual,filter,info,format,genotypes) = orig match {
       case Some(v) => {
         (v.id,v.qual,v.filter,v.info,v.format,v.genotypes);
       }
       case None => {
         (".","0",".",Map[String,Option[String]](),Seq[String](),SVcfGenotypeSet(Seq[String](),Array[Array[String]]()));
       }
     }
     
      SVcfOutputVariantLine(
       in_chrom = vc.getContig(),
       in_pos = vc.getStart(),
       in_id = id,
       in_ref = vc.getReference().getBaseString(),
       in_alt = alt,
       in_qual = qual,
       in_filter = filter, 
       in_info = info,
       in_format = format,
       in_genotypes = genotypes
      )
  }
  


  case class FindComplexAlleles1(genomeFA : String,idxTag : String,gtTag : String = "GT",
                                unmatchedCountThresh : Int = 5, 
                                unmatchedRatioThresh : Double = 0.5, 
                                baseBuffer : Int = 10,
                                onlyKeepComplex : Boolean = false) extends SVcfWalker {
    def walkerName : String = "FindComplexAllelesV1"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("unmatchedCountThresh", ""+ unmatchedCountThresh),
        ("unmatchedRatioThresh", ""+ unmatchedRatioThresh),
        ("baseBuffer", ""+ baseBuffer),
        ("gtTag", ""+ gtTag)
    );
    
    val refFile = new File(genomeFA)
    val refSeqFile : htsjdk.samtools.reference.ReferenceSequenceFile = 
                     htsjdk.samtools.reference.ReferenceSequenceFileFactory.getReferenceSequenceFile(refFile);
    val refDataSource = new org.broadinstitute.gatk.engine.datasources.reference.ReferenceDataSource(refFile);
    val genLocParser = new org.broadinstitute.gatk.utils.GenomeLocParser(refSeqFile)
    
    def getBaseAtPos(chrom : String, pos : Int) : String = {
      if(pos < 1 || pos > refSeqFile.getSequenceDictionary.getSequence(chrom).getSequenceLength){
        return "N";
      } else {
        refSeqFile.getSubsequenceAt(chrom,pos,pos).getBaseString.toUpperCase
      }
    }
    
    
    def getGenos( gta : Array[String] ) : Vector[Int] = {
      gta.toVector.map{ gg => {
            if(gg == "0/1"){
              1
            } else if(gg == "1/1"){
              2
            } else {
              0
            }
          }}
    }
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      var errCt = 0;
      
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      var idx = 0;
      var compIdx = 0;
      outHeader.addInfoLine(  new SVcfCompoundHeaderLine("INFO","SWH_MPAR_isMPAR",Number="1",Type="Integer",desc="Equal to 1 iff the variant is a complex composite variant."));
      outHeader.addInfoLine(  new SVcfCompoundHeaderLine("INFO","SWH_MPAR_hasMPAR",Number="1",Type="Integer",desc="greater than 0 iff the variant has one or more complex composite variants attached."));
      outHeader.addInfoLine(  new SVcfCompoundHeaderLine("INFO","SWH_MPAR_parentIdx",Number=".",Type="Integer",desc="Parent indices from idx tag "+idxTag));
      outHeader.addFormatLine(new SVcfCompoundHeaderLine("INFO",gtTag+"_MPAR",Number=".",Type="String",desc="Based on ("+gtTag+")"));

      
      val sampRango = Range(0,vcfHeader.titleLine.sampleList.length);
      
      var bufferMap = new scala.collection.immutable.TreeMap[Int,SVcfVariantLine]();

      (addIteratorCloseAction( iter = vcFlatMap(vcIter){v => {
        //val vc = v.getOutputLine()
        val startPos = v.start;
        val endPos = v.end;
        val vc = v.getOutputLine();
        idx = idx + 1;
        
        //val othrChromSeq : Seq[SVcfVariantLine] = bufferMap.filter{ case (p,ov) => ov.chrom != v.chrom}.map{_._2}.toSeq;
        //bufferMap = bufferMap.filter{ case (p,ov) => ov.chrom == v.chrom }
        
        //val outSeq : Seq[SVcfVariantLine] = bufferMap.takeWhile{ case (p,ov) => ov.chrom != v.chrom || p < startPos - baseBuffer}.map{_._2}.toSeq;
        bufferMap = bufferMap.filter{ case (p,ov) => ov.chrom == v.chrom }
        bufferMap = bufferMap.dropWhile{ case (p,ov) => p < startPos - baseBuffer};
        
        val currBuffer : Vector[SVcfVariantLine] = bufferMap.map{_._2}.toVector.filter{ ov => ! (ov.start < v.end && ov.end > v.start) };
        
        bufferMap = bufferMap + ((endPos,v));
        
        val genosOpt = Some(v.genotypes.fmt.indexOf(gtTag)).filter{_ != -1}.map{ gidx => {
           v.genotypes.genotypeValues(gidx)
        }}
        
        val currNewSeq : Seq[SVcfVariantLine] = genosOpt.toSeq.flatMap{ genosRaw => {
            val genos = getGenos(genosRaw);
            val altCt = genos.count(gg => gg > 0)
            val bufferGenos = currBuffer.flatMap{ ov => {
                Some(ov.genotypes.fmt.indexOf(gtTag)).filter{_ != -1}.map{ gidx => {
                  getGenos( ov.genotypes.genotypeValues(gidx) )
                }}.flatMap{ ogt => {
                  val matchBools = genos.zip(ogt).map{ case (g,og) => {
                    g == og && g > 0
                  }}
                  if(matchBools.count(x => x) > 0){
                    Some( (ov,matchBools) )
                  } else {
                    None
                  }
                }}
              //ogtOpt.flatMap{
            }}
            
            if(bufferGenos.length == 0){
              Seq();
            } else {
              
              val genoArray  = bufferGenos.map{ case (vo,matchIdx) => matchIdx.map{ xx => if(xx) "1" else "0" }};
              val genoBlocks = sampRango.map{ j => Range(0,bufferGenos.length).map{ i => { genoArray(i)(j) } }.mkString("")};
              val genoBlockSet = genoBlocks.distinct.toSeq;
              
              genoBlockSet.zipWithIndex.map{ case (gbs,blockIdx) => {
                val genoArraySet = gbs.zipWithIndex.filter{ case (c,i) => c == '1' }.map{ _._2 }.toSet
                val ovs = bufferGenos.zipWithIndex.filter{ case ((ov,matchBools),i) => {
                  genoArraySet.contains(i);
                }}.map{ case ((ov,matchBools),i) => ov }.toVector
                val allv : Vector[SVcfVariantLine] = ovs :+ v
                val newPos =  allv.map{vv => vv.pos}.min;
                //val rangos =  allv.map{vv => Range(vv.pos,vv.ref.length)}
                val (newRef,newAlt,newEndPos) = allv.foldLeft( ("","",newPos) ){ case ((refSoFar,altSoFar,posSoFar),vv) => {
                  if(posSoFar == vv.pos){
                    (refSoFar + vv.ref,altSoFar + vv.alt.head,posSoFar + vv.ref.length)
                  } else {
                    val missingBases = Range(posSoFar,vv.pos).map{ pp => getBaseAtPos(v.chrom,pp) }.mkString("")
                    (refSoFar +missingBases+ vv.ref,altSoFar  +missingBases+ vv.alt.head,vv.pos + vv.ref.length)
                  }
                }}
                compIdx += 1;
                
                
                val gtvals = genoBlocks.zip(genosRaw).map{ case (gb,gg) => {
                                  if(gb != gbs){
                                    "."
                                  } else {
                                    gg
                                  }
                                }}.toArray
                val rawGtVals = gtvals.clone();
                val svgs = SVcfGenotypeSet(  fmt = Seq[String](gtTag,gtTag+"_MPAR"),
                                             genotypeValues = Array[Array[String]](rawGtVals, gtvals)
                                          )
                
                
                val cvc = SVcfOutputVariantLine(
                      in_chrom = v.chrom,
                      in_pos = newPos,
                      in_id = "COMPOSITE_"+idx+"_"+compIdx,
                       in_ref = newRef,
                       in_alt = Seq(newAlt),
                       in_qual = ".",
                       in_filter = ".",
                       in_info = Map[String,Option[String]](),
                       in_format = svgs.fmt,
                       in_genotypes = svgs
                      );

                vc.addInfo("SWH_MPAR_hasMPAR","1")
                cvc.addInfo("SWH_MPAR_isMPAR","1")
                cvc.addInfo("SWH_MPAR_parentIdx", allv.flatMap{ vv => vv.info.getOrElse(idxTag,None).toSeq.flatMap{ s => s.split(",").toSeq } }.padTo(1,".").mkString(","));
                cvc.addInfo(idxTag,v.info.getOrElse(idxTag,None).getOrElse("UNK") + "/" + blockIdx);
                cvc
              }}
              
            }
            
        }}
        //currNewSeq.foreach{ vv => {
        //          bufferMap = bufferMap + ((vv.end,vv));
        //}}
        vc +: currNewSeq
        //outSeq
      }}, closeAction = (() => {
        reportln("Finished run. bufferMap.length = "+bufferMap.size,"note");
      })),outHeader)
    }
  }
  
  
  case class FindComplexAlleles(genomeFA : String,idxTag : String,gtTag : String = "GT",
                                unmatchedCountThresh : Int = 5, 
                                unmatchedRatioThresh : Double = 0.5, 
                                baseBuffer : Int = 10,
                                onlyKeepComplex : Boolean = false,
                                mergeTags : List[String] = List[String]()) extends SVcfWalker {
    def walkerName : String = "FindComplexAllelesV2"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("unmatchedCountThresh", ""+ unmatchedCountThresh),
        ("unmatchedRatioThresh", ""+ unmatchedRatioThresh),
        ("baseBuffer", ""+ baseBuffer),
        ("gtTag", ""+ gtTag)
    );
    
    val refFile = new File(genomeFA)
    val refSeqFile : htsjdk.samtools.reference.ReferenceSequenceFile = 
                     htsjdk.samtools.reference.ReferenceSequenceFileFactory.getReferenceSequenceFile(refFile);
    val refDataSource = new org.broadinstitute.gatk.engine.datasources.reference.ReferenceDataSource(refFile);
    val genLocParser = new org.broadinstitute.gatk.utils.GenomeLocParser(refSeqFile)
    
    def getBaseAtPos(chrom : String, pos : Int) : String = {
      if(pos < 1 || pos > refSeqFile.getSequenceDictionary.getSequence(chrom).getSequenceLength){
        return "N";
      } else {
        refSeqFile.getSubsequenceAt(chrom,pos,pos).getBaseString.toUpperCase
      }
    }
    
    
    def getGenos( gta : Array[String] ) : Vector[Int] = {
      gta.toVector.map{ gg => {
            if(gg == "0/1"){
              1
            } else if(gg == "1/1"){
              2
            } else {
              0
            }
          }}
    }
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      var errCt = 0;
      
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      var idx = 0;
      var compIdx = 0;
      outHeader.addInfoLine(  new SVcfCompoundHeaderLine("INFO","SWH_MPAR_FLAG",Number="1",Type="Integer",desc="Equal to 1 iff the variant is involved with any Misleading PARtial variant."));
      outHeader.addInfoLine(  new SVcfCompoundHeaderLine("INFO","SWH_MPAR_isMPAR",Number="1",Type="Integer",desc="Equal to 1 iff the variant is a complex composite variant."));
      outHeader.addInfoLine(  new SVcfCompoundHeaderLine("INFO","SWH_MPAR_hasPrimaryChild",Number="1",Type="Integer",desc="greater than 0 iff the variant has one or more complex composite variants attached."));
      outHeader.addInfoLine(  new SVcfCompoundHeaderLine("INFO","SWH_MPAR_hasSecondaryChild",Number="1",Type="Integer",desc="greater than 0 iff the variant has one or more complex composite variants attached."));
      outHeader.addInfoLine(  new SVcfCompoundHeaderLine("INFO","SWH_MPAR_parentIdx",Number=".",Type="Integer",desc="Parent indices from idx tag "+idxTag));
      outHeader.addInfoLine(  new SVcfCompoundHeaderLine("INFO","SWH_MPAR_primaryParent",Number=".",Type="Integer",desc="Parent indices from idx tag "+idxTag));
      outHeader.addInfoLine(  new SVcfCompoundHeaderLine("INFO","SWH_MPAR_secondaryParents",Number=".",Type="Integer",desc="Parent indices from idx tag "+idxTag));
      //outHeader.addInfoLine(  new SVcfCompoundHeaderLine("INFO","SWH_MPAR_numChild",Number=".",Type="Integer",desc="Parent indices from idx tag "+idxTag));
      //outHeader.addInfoLine(  new SVcfCompoundHeaderLine("INFO","SWH_MPAR_numSibs",Number=".",Type="Integer",desc="Parent indices from idx tag "+idxTag));
      
      outHeader.addFormatLine(new SVcfCompoundHeaderLine("FORMAT",gtTag+"_MPAR",Number=".",Type="String",desc="Based on ("+gtTag+")"));
      
      mergeTags.foreach{ mtag => {
        val oldhl = vcfHeader.infoLines.find( line =>  line.ID == mtag).get
        outHeader.addInfoLine( new SVcfCompoundHeaderLine("INFO",mtag,Number=".",Type=oldhl.Type,desc=oldhl.desc ));
        outHeader.addInfoLine( new SVcfCompoundHeaderLine("INFO","SWH_MPAR_childSet_"+mtag,Number=".",Type=oldhl.Type,desc=oldhl.desc ));
      }}
      val sampCt = vcfHeader.titleLine.sampleList.length
      val sampRango = Range(0,vcfHeader.titleLine.sampleList.length);
      
      var bufferMap : scala.collection.immutable.Map[(String,Int),Set[SVcfOutputVariantLine]] = new scala.collection.immutable.TreeMap[(String,Int),Set[SVcfOutputVariantLine]]()
      def addToBuffer(vv : SVcfOutputVariantLine){
        val newSet = bufferMap.getOrElse((vv.chrom,vv.end),Set[SVcfOutputVariantLine]()) + vv;
        bufferMap = bufferMap.updated((vv.chrom,vv.end), newSet)
      }
      var vcIterBuf = vcIter.buffered;
      var currChrom = vcIterBuf.head.chrom;
      
      val zeroString = repString("0",sampCt);

      
      (addIteratorCloseAction( iter = vcFlatMap(vcIterBuf){v => {
        //val vc = v.getOutputLine()
        val startPos = v.start;
        val endPos = v.end;
        val vc = v.getOutputLine();
        vc.addInfo("SWH_MPAR_isMPAR","0")
        vc.addInfo("SWH_MPAR_FLAG","0")
        idx = idx + 1;
        
        if( v.chrom != currChrom) {
          bufferMap.flatMap{_._2}.toSeq
          addToBuffer(vc);
          currChrom = v.chrom;
          Seq()
        } else {
          
          val prevChromSeq = bufferMap.filter{ case ((c,p),ov) => c != v.chrom }.flatMap{_._2}.toSeq
          bufferMap = bufferMap.filter{ case ((c,p),ov) => c == v.chrom }
          val outSeq : Seq[SVcfOutputVariantLine] = prevChromSeq ++ 
                                                    bufferMap.takeWhile{ case ((c,p),ov) => p < startPos - baseBuffer}.flatMap{_._2}.toSeq;
          
          bufferMap = bufferMap.dropWhile{ case ((c,p),ov) => p < startPos - baseBuffer};
          
          val currBuffer : Vector[SVcfOutputVariantLine] = bufferMap.map{_._2}.toVector.flatMap{ ovs => ovs.filter{ ov => ! (ov.start < v.end && ov.end > v.start) }}.filter{ ov => ov.getInfo("SWH_MPAR_isMPAR") != "1" }
          
          addToBuffer(vc);
          //bufferMap = bufferMap + ((endPos,vc));
          
          val genosOpt = Some(v.genotypes.fmt.indexOf(gtTag)).filter{_ != -1}.map{ gidx => {
             v.genotypes.genotypeValues(gidx)
          }}
          
          val currNewSeq : Seq[SVcfOutputVariantLine] = genosOpt.toSeq.flatMap{ genosRaw => {
              val genos = getGenos(genosRaw);
              val altCt = genos.count(gg => gg > 0)
              val bufferGenos = currBuffer.flatMap{ ov => {
                  Some(ov.genotypes.fmt.indexOf(gtTag)).filter{_ != -1}.map{ gidx => {
                    getGenos( ov.genotypes.genotypeValues(gidx) )
                  }}.flatMap{ ogt => {
                    val matchBools = genos.zip(ogt).map{ case (g,og) => {
                      g == og && g > 0
                    }}
                    if(matchBools.count(x => x) > 0){
                      Some( (ov,matchBools) )
                    } else {
                      None
                    }
                  }}
                //ogtOpt.flatMap{
              }}
              
              if(bufferGenos.length == 0){
                Seq();
              } else {
                
                val genoArray  = bufferGenos.map{ case (vo,matchIdx) => matchIdx.map{ xx => if(xx) "1" else "0" }};
                val genoBlocks = sampRango.map{ j => Range(0,bufferGenos.length).map{ i => { genoArray(i)(j) } }.mkString("")};
                val genoBlockSet = genoBlocks.distinct.sortBy{ (s : String) => ((s.count(c => c == '0'),s)) }.filter{ (s : String) => s.contains('1') }.toVector
                
                genoBlockSet.zipWithIndex.flatMap{ case (gbs,blockIdx) => {
                  val genoArraySet = gbs.zipWithIndex.filter{ case (c,i) => c == '1' }.map{ _._2 }.toSet
                  val ovs = bufferGenos.zipWithIndex.filter{ case ((ov,matchBools),i) => {
                    genoArraySet.contains(i);
                  }}.map{ case ((ov,matchBools),i) => ov }.toVector
                  if(ovs.length == 0){
                    None;
                  } else { 
                    val allv : Vector[SVcfOutputVariantLine] = ovs :+ vc
                    val newPos =  allv.map{vv => vv.pos}.min;
                    //val rangos =  allv.map{vv => Range(vv.pos,vv.ref.length)}
                    val (newRef,newAlt,newEndPos) = allv.foldLeft( ("","",newPos) ){ case ((refSoFar,altSoFar,posSoFar),vv) => {
                      if(posSoFar == vv.pos){
                        (refSoFar + vv.ref,altSoFar + vv.alt.head,posSoFar + vv.ref.length)
                      } else {
                        val missingBases = Range(posSoFar,vv.pos).map{ pp => getBaseAtPos(v.chrom,pp) }.mkString("")
                        (refSoFar +missingBases+ vv.ref,altSoFar  +missingBases+ vv.alt.head,vv.pos + vv.ref.length)
                      }
                    }}
                    compIdx += 1;
                    
                    
                    val gtvals = genoBlocks.zip(genosRaw).map{ case (gb,gg) => {
                                      if(gb != gbs){
                                        "."
                                      } else {
                                        gg
                                      }
                                    }}.toArray
                    val rawGtVals = gtvals.clone();
                    val svgs = SVcfGenotypeSet(  fmt            = Seq[String](gtTag,gtTag+"_MPAR"),
                                                 genotypeValues = Array[Array[String]](rawGtVals, gtvals)
                                              )
                    
                    
                    val cvc = SVcfOutputVariantLine(
                          in_chrom = v.chrom,
                          in_pos = newPos,
                          in_id = "COMPOSITE_"+idx+"_"+compIdx,
                           in_ref = newRef,
                           in_alt = Seq(newAlt),
                           in_qual = ".",
                           in_filter = ".",
                           in_info = Map[String,Option[String]](),
                           in_format = svgs.fmt,
                           in_genotypes = svgs
                          );
                    
                    vc.addInfo("SWH_MPAR_hasPrimaryChild","1")
                    vc.addInfo("SWH_MPAR_FLAG","1")
                    
                    //TO DO: add secondary child tag! will require type change to buffer!
                    ovs.foreach{ ovv => {
                      ovv.addInfo("SWH_MPAR_hasSecondaryChild","1")
                      ovv.addInfo("SWH_MPAR_FLAG","1")
                    }}
                    
                    cvc.addInfo("SWH_MPAR_isMPAR","1")
                    cvc.addInfo("SWH_MPAR_parentIdx",     allv.flatMap{ vv => vv.info.getOrElse(idxTag,None).toSeq.flatMap{ s => s.split(",").toSeq } }.padTo(1,".").mkString(","));
                    cvc.addInfo("SWH_MPAR_primaryParent",    v.getInfo(idxTag));
                    cvc.addInfo("SWH_MPAR_secondaryParents", ovs.flatMap{ vv => vv.info.getOrElse(idxTag,None).toSeq.flatMap{ s => s.split(",").toSeq } }.padTo(1,".").mkString(","));
                    cvc.addInfo("SWH_MPAR_FLAG","1")
                    mergeTags.foreach{ mtag => {
                      val mergedTagSeq = allv.flatMap{ vv => {
                        vv.info.getOrElse(mtag,None)
                      }}
                      val mergedTagString = mergedTagSeq.mkString(",");
                      
                      cvc.addInfo(mtag,mergedTagString);
                      allv.foreach{ ovv => {
                        val mergedTagChildSet : Set[String] = ovv.info.getOrElse("SWH_MPAR_childSet_"+mtag,None).toSeq.toSet.flatMap{ (ii : String) => ii.split(",").toVector.toSet } ++ mergedTagSeq.toSet
                        //val temp = ovv.info.getOrElse("SWH_MPAR_childSet_"+mtag,None).map{ii => (ii.split(",").toVector.toSet ++ mergedTagSeq.toSet).toVector.filter(_ != ".").sorted.mkString(",")}.getOrElse(".")
                        ovv.addInfo("SWH_MPAR_childSet_"+mtag, mergedTagChildSet.toVector.sorted.padTo(1,".").mkString(",")  )
                      }}
                    }}
                    
                    cvc.addInfo(idxTag,v.info.getOrElse(idxTag,None).getOrElse("UNK") + "/" + blockIdx);
                    Some(cvc)
                  }
                }}
                
              }
              
          }}
          currNewSeq.foreach{ vv => {
                    //bufferMap = bufferMap + ((vv.end,vv));
                    addToBuffer(vv)
          }}
          outSeq    
        }
      }} ++ (new Iterator[SVcfVariantLine]{
        var zinit : Boolean = false
        var ziter : Iterator[SVcfVariantLine] = Iterator[SVcfVariantLine](); 
        def hasNext : Boolean = (! zinit) || ziter.hasNext;
        def next : SVcfVariantLine = {
          if(zinit){
            ziter.next;
          } else {
            zinit = true
            val zvector = bufferMap.toVector.map{_._2}.map{_.toVector.sortBy(vv => vv.getInfo(idxTag))}.flatten
            ziter = zvector.iterator
            bufferMap.drop(zvector.length);
            next
          }
        }
      }), closeAction = (() => {
        reportln("Finished run. bufferMap.length = "+bufferMap.size,"note");
        bufferMap.foreach{ case (p,vvs) => {
          //reportln("   "+vvs,"note");
          vvs.foreach{ vv => {
            reportln("   "+vv.getSimpleVcfString(),"note");
          }}
        }}
      })),outHeader)
      
    }
  }
  /*
  case class FlagComplexAlleles(genomeFA : String,idxTag : String,gtTag : String = "GT",
                                unmatchedCountThresh : Int = 5, 
                                unmatchedRatioThresh : Double = 0.5, 
                                baseBuffer : Int = 10,
                                onlyKeepComplex : Boolean = false,
                                mergeTags : List[String] = List[String]()) extends SVcfWalker {
    def walkerName : String = "FlagComplexAlleles"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)]();
    
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      var errCt = 0;
      
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      (addIteratorCloseAction( iter = vcGroupedFlatMap(groupBySpan(vcIter.buffered)(vc => vc.pos)){ vcSeq => {
          vcSeq
      }}, closeAction = (() => {
          //do nothing?
      })),outHeader)
    }
  }*/
    
  
  case class FixDotAltVcfLines(genomeFa : String, windowSize : Int = 200, tag : String = "SWH_FixedDotAltAllele") extends SVcfWalker {
    def walkerName : String = "FixDotAltVcfLines"
    def walkerParams : Seq[(String,String)] = Seq[(String,String)](("genomeFa",genomeFa));
    
    val refFile = new File(genomeFa)
    val refSeqFile : htsjdk.samtools.reference.ReferenceSequenceFile = 
                     htsjdk.samtools.reference.ReferenceSequenceFileFactory.getReferenceSequenceFile(refFile);
    val refDataSource = new org.broadinstitute.gatk.engine.datasources.reference.ReferenceDataSource(refFile);
    val genLocParser = new org.broadinstitute.gatk.utils.GenomeLocParser(refSeqFile)
    
    def getBaseAtPos(chrom : String, pos : Int) : String = {
      if(pos < 1 || pos > refSeqFile.getSequenceDictionary.getSequence(chrom).getSequenceLength){
        return "N";
      } else {
        refSeqFile.getSubsequenceAt(chrom,pos,pos).getBaseString.toUpperCase
      }
    }
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      //  def bufferedResorting[A](iter : Iterator[A], bufferSize : Int)(f : (A => Int)) : Iterator[A] = {
      val newHeader = vcfHeader.copyHeader;
      newHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tag,Number = "1", Type = "Integer", desc = "Equal to 1 if and only if the original VCF line contained a dot-allele which was then converted into a properly formatted indel.").addWalker(this));
      newHeader.addWalk(this);
      var noticeShownA = true;
      var noticeShownR = true;
      (vcMap(vcIter){ vc => {
        val vb = vc.getOutputLine();
        if(vb.ref == VcfTool.MISSING_VALUE_STRING || vb.ref == "-"){
          vb.addInfo(tag,"1");
          val prevBase = getBaseAtPos(vc.chrom,vc.pos);
          vb.in_ref = prevBase;
          vb.in_alt = vc.alt.map{a => {
            if(a == VcfTool.UNKNOWN_ALT_TAG_STRING){
              a
            } else if(a == VcfTool.MISSING_VALUE_STRING){
              prevBase;
            } else {
              prevBase + a;
            }
          }}
          if(noticeShownR){
            reportln("Fixed malformed VCF line: found a '.' ref allele\n"+
                 "   Technically this is a violation of the VCF specification. The variant has been fixed.",
                 "debug");
            noticeShownR = false;
          }
          notice("Fixed malformed VCF line: found a '.' ref allele\n"+
                 "   old line: "+vc.getSimpleVcfString()+"\n"+
                 "   new line: "+vb.getSimpleVcfString()+"\n",
                 "Fixed_malformed_vcf_dot_refAllele",1);
        } else if(vb.alt.head == VcfTool.MISSING_VALUE_STRING || vb.alt.head == "-"){
          vb.addInfo(tag,"1");
          vb.in_pos = vb.in_pos - 1;
          val prevBase = getBaseAtPos(vc.chrom,vc.pos-1);
          vb.in_ref = prevBase + vc.ref;
          vb.in_alt = vc.alt.map{a => {
            if(a == VcfTool.UNKNOWN_ALT_TAG_STRING){
              a
            } else if(a == VcfTool.MISSING_VALUE_STRING | a == "-"){
              prevBase;
            } else {
              prevBase + a;
            }
          }}
          if(noticeShownA){
            reportln("Fixed malformed VCF line: found a '.' alt allele\n"+
                 "   Technically this should be interpreted as \"no alleles found\", but some callers use it to mean "+
                 "   an indel. The variant has been fixed.",
                 "debug");
            noticeShownA = false;
          }

          notice("Fixed malformed VCF line: found a '.' alt allele\n"+
                 "   old line: "+vc.getSimpleVcfString()+"\n"+
                 "   new line: "+vb.getSimpleVcfString()+"\n",
                 "Fixed_malformed_vcf_dot_altAllele",1);
        }
        vb;
      }},newHeader);
      
    }
  }

  case class FixFirstBaseMismatch(genomeFa : String, windowSize : Int = 200, changeTag : Option[String] = Some("GATK_LAT_FIRSTBASEMM")) extends SVcfWalker {
    def walkerName : String = "FixFirstBaseMismatch"
    def walkerParams : Seq[(String,String)] = Seq[(String,String)](("genomeFa",genomeFa));
    
    val refFile = new File(genomeFa)
    val refSeqFile : htsjdk.samtools.reference.ReferenceSequenceFile = 
                     htsjdk.samtools.reference.ReferenceSequenceFileFactory.getReferenceSequenceFile(refFile);
    val refDataSource = new org.broadinstitute.gatk.engine.datasources.reference.ReferenceDataSource(refFile);
    val genLocParser = new org.broadinstitute.gatk.utils.GenomeLocParser(refSeqFile)
    
    def getBaseAtPos(chrom : String, pos : Int) : String = {
      if(pos < 1 || pos > refSeqFile.getSequenceDictionary.getSequence(chrom).getSequenceLength){
        return "N";
      } else {
        refSeqFile.getSubsequenceAt(chrom,pos,pos).getBaseString.toUpperCase
      }
    }
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      //  def bufferedResorting[A](iter : Iterator[A], bufferSize : Int)(f : (A => Int)) : Iterator[A] = {
      val newHeader = vcfHeader.copyHeader;
      changeTag.foreach{ tag => {
        newHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tag,Number = "1", Type = "Integer", desc = "Equal to 1 if and only if the original VCF line contained an indel in which the first bases dont match up. Some tools do not accept variants in this form.").addWalker(this));
      }}
      newHeader.addWalk(this);
      var noticeShownA = true;
      (vcMap(vcIter){ vc => {
        val vb = vc.getOutputLine();
        if( vc.alt.head != VcfTool.MISSING_VALUE_STRING && (vc.ref.length > 1 || vc.alt.head.length > 1) && (vc.ref.head != vc.alt.head.head)){
          changeTag.foreach{ tag => {
            vb.addInfo(tag,"1");
          }}
          vb.in_pos = vb.in_pos - 1;
          val prevBase = getBaseAtPos(vc.chrom,vc.pos-1);
          vb.in_ref = prevBase + vc.ref;
          vb.in_alt = vc.alt.map{a => {
            if(a == VcfTool.UNKNOWN_ALT_TAG_STRING){
              a
            } else if(a == VcfTool.MISSING_VALUE_STRING){
              prevBase;
            } else {
              prevBase + a;
            }
          }}
          if(noticeShownA){
            reportln("Fixed malformed VCF line: found an indel with mismatching first bases.",
                     "debug");
            noticeShownA = false;
          }

          notice("Fixed malformed VCF line: found a '.' alt allele\n"+
                 "   old line: "+vc.getSimpleVcfString()+"\n"+
                 "   new line: "+vb.getSimpleVcfString()+"\n",
                 "Fixed_malformed_vcf_indel_firstBaseMM",1);
          vb.getOutputLine();
        } else {
          changeTag.foreach{ tag => {
            vb.addInfo(tag,"0");
          }}
          vb;
        }
      }},newHeader);
      
    }
  }
  

  def rightAlignIndel(ref : String, alt : String, pos : Int, flankingSeq : String) : (Int,String,String) = {
    if(ref == alt){
      return (pos,ref,alt);
    } else if(ref.length == 1 && alt.length == 1){
      return (pos,ref,alt);
    } else if(ref.length > 0 && alt.length > 0 && ref.head == alt.head){
      return rightAlignIndel(ref.tail,alt.tail,pos = pos + 1, flankingSeq.tail);
    } else if(ref.length > 0 && alt.length > 0 && ref.last == alt.last){
      return rightAlignIndel(ref.init, alt.init, pos = pos, flankingSeq=flankingSeq);
    } else {
      val variantLen = ref.length;
      val refStr = ref + flankingSeq.drop(ref.length);
      val altStr = alt + flankingSeq.drop(ref.length);
      
      val minLen = math.min(refStr.length,altStr.length);
      if(refStr.take(minLen) == altStr.take(minLen)){
        return (pos+minLen,refStr.drop(minLen),altStr.drop(minLen));
      }
      
      val offset = refStr.zip(altStr).indexWhere{case (r,a) => r != a}
      
      val fref = refStr.slice(offset,offset+ref.length)
      val falt = altStr.slice(offset,offset+alt.length)
      
      (pos + offset, fref, falt)
    }
  }
  class RightAligner(genomeFa: String, windowSize : Int = 200){
    val refFile = new File(genomeFa)
    val refSeqFile : htsjdk.samtools.reference.ReferenceSequenceFile = 
                     htsjdk.samtools.reference.ReferenceSequenceFileFactory.getReferenceSequenceFile(refFile);
    val refDataSource = new org.broadinstitute.gatk.engine.datasources.reference.ReferenceDataSource(refFile);
    val genLocParser = new org.broadinstitute.gatk.utils.GenomeLocParser(refSeqFile)
    
    def getBasesForIv(chrom : String, start : Int, end : Int) : String = {
      if(start < 1){
        return this.getBasesForIv(chrom=chrom,start=1,end=end);
      } else if(end > refSeqFile.getSequenceDictionary.getSequence(chrom).getSequenceLength){
        return this.getBasesForIv(chrom=chrom,start=start,end=refSeqFile.getSequenceDictionary.getSequence(chrom).getSequenceLength);
      } else {
        refSeqFile.getSubsequenceAt(chrom,start,end).getBaseString.toUpperCase
      }
    }
    
    def rightAlignIndelFromFA(chrom : String, pos : Int, ref : String, alt : String) : (Int,String,String) = {
      rightAlignIndel(ref,alt,pos,this.getBasesForIv( chrom=chrom,start=pos,end = pos + ref.length + windowSize ));
    }
    
  }
  
  case class refFastaTool(genomeFa : String){
    val refFile = new File(genomeFa)
    val refSeqFile : htsjdk.samtools.reference.ReferenceSequenceFile = 
                     htsjdk.samtools.reference.ReferenceSequenceFileFactory.getReferenceSequenceFile(refFile);
    val refDataSource = new org.broadinstitute.gatk.engine.datasources.reference.ReferenceDataSource(refFile);
    val genLocParser = new org.broadinstitute.gatk.utils.GenomeLocParser(refSeqFile)
    def getBasesForIv(chrom : String, start : Int, end : Int) : String = {
      if(start < 1){
        return this.getBasesForIv(chrom=chrom,start=1,end=end);
      } else if(end > refSeqFile.getSequenceDictionary.getSequence(chrom).getSequenceLength){
        return this.getBasesForIv(chrom=chrom,start=start,end=refSeqFile.getSequenceDictionary.getSequence(chrom).getSequenceLength);
      } else {
        refSeqFile.getSubsequenceAt(chrom,start,end).getBaseString.toUpperCase
      }
    }
    def getBaseAtPos(chrom : String, pos : Int) : String = {
      if(pos < 1 || pos > refSeqFile.getSequenceDictionary.getSequence(chrom).getSequenceLength){
        return "N";
      } else {
        refSeqFile.getSubsequenceAt(chrom,pos,pos).getBaseString.toUpperCase
      }
    }
  }
  
    def refToUpper(rs : Array[Byte]) : Array[Byte] = {
      rs.map{ c => c.toChar.toUpper.toByte }
    }
  
  case class LeftAlignAndTrimWalker(genomeFa : String, windowSize : Int = 200, tagPrefix : Option[String] = Some("GATK_LAT_"),
                                    useGatkLibCall : Boolean = false) extends SVcfWalker {
    def walkerName : String = "LeftAlignAndTrim"
    def walkerParams : Seq[(String,String)] = Seq[(String,String)](("genomeFa",genomeFa),("windowSize",""+windowSize));
    
    
    val latMethod = if(useGatkLibCall){
      "GATKcall"
    } else {
      "GATKcode"
    }
    //def walkerInfo : SVcfWalkerInfo;
    val refFile = new File(genomeFa)
    val refSeqFile : htsjdk.samtools.reference.ReferenceSequenceFile = 
                     htsjdk.samtools.reference.ReferenceSequenceFileFactory.getReferenceSequenceFile(refFile);
    val refDataSource = new org.broadinstitute.gatk.engine.datasources.reference.ReferenceDataSource(refFile);
    val genLocParser = new org.broadinstitute.gatk.utils.GenomeLocParser(refSeqFile)
    //var referenceSequence  : htsjdk.samtools.reference.ReferenceSequence = 
    //                         refSeqFile.getSubsequenceAt(refSeqFile.getSequenceDictionary().getSequences().asScala.head.getSequenceName(), 1, windowSize);
      
    /*class Provider(offset : Int, len : Int) extends ReferenceContext.ReferenceContextRefProvider {
      def getBases() : Array[Byte] = {
        val bases = referenceSequence.getBases();
        val out :  Array[Byte] = Array.ofDim[Byte](len);
        System.arraycopy(referenceSequence.getBases(), offset, bases, 0, len);
        bases;
      } 
    }*/
    
    //class SDP() extends org.broadinstitute.gatk.engine.datasources.providers.ShardDataProvider {
     // 
    //}
    
    //class RefView(gparser : org.broadinstitute.gatk.utils.GenomeLocParser, 
    //              refFile : htsjdk.samtools.reference.ReferenceSequenceFile) extends org.broadinstitute.gatk.engine.datasources.providers.ReferenceView {
    //  
    //}
    
    //val shards = refDataSource.
    
    //var walkerNumber : Option[String] = None;
    
    def getReferenceContext(chrom : String, start : Int, end : Int) : ReferenceContext = {
      val startWin = math.max(1,start-windowSize)
      val chromLen = refSeqFile.getSequenceDictionary().getSequence(chrom).getSequenceLength()
      val endWin = math.min(chromLen,end + windowSize)
      
      val genomeLoc = genLocParser.createGenomeLoc( chrom, refSeqFile.getSequenceDictionary().getSequenceIndex(chrom),start,end )
      val window = genLocParser.createGenomeLoc( chrom,refSeqFile.getSequenceDictionary().getSequenceIndex(chrom),startWin,endWin )
      val len = window.size();
      val refseq =  refSeqFile.getSubsequenceAt(chrom, startWin, endWin);
      new ReferenceContext( genLocParser, genomeLoc, window, refToUpper(refseq.getBases()));
    }

    
    val rft = refFastaTool(genomeFa = genomeFa)
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      //  def bufferedResorting[A](iter : Iterator[A], bufferSize : Int)(f : (A => Int)) : Iterator[A] = {
      val newHeader = vcfHeader.copyHeader;
      newHeader.addWalk(this);
      tagPrefix.foreach{ tp => {
        newHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tp+"OFFSET",Number="1",Type="String",desc="The left alignment offset.").addExtraField("latWindow",windowSize.toString).addExtraField("latMethod",latMethod),walker=Some(this));
        newHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tp+"LAT",Number="1",Type="Integer",desc="Equals 1 iff the leftAlignAndTrim changed the variant encoding.").addExtraField("latWindow",windowSize.toString).addExtraField("latMethod",latMethod),walker=Some(this));
        newHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",ID=tp+"TRIMBP",Number="1",Type="Integer",desc="The number of bases trimmed from both ref and alt by the trim function, prior to left aligning.").addExtraField("latWindow",windowSize.toString).addExtraField("latMethod",latMethod),walker=Some(this));
      }}
      
      ((addIteratorCloseAction(bufferedGroupedResorting(vcMap(vcIter){ vc => {
        if(vc.alt.length == 2){
          if(vc.alt.last != "*"){
            error("ERROR: Cannot have multiallelic variants in leftAlignAndTrim. \n   Offending Variant:\n   "+vc.getSimpleVcfString());
          }
        } else if(vc.alt.length != 1){
          error("ERROR: Cannot have multiallelic variants in leftAlignAndTrim. \n   Offending Variant:\n   "+vc.getSimpleVcfString());
        }
        val rc = getReferenceContext(vc.chrom,vc.start,vc.end);
        if(useGatkLibCall){
          trimAlign(vc,rc,tagPrefix)._1;
        } else {
          trimAlignNoLib(vc,rc,tagPrefix,windowSize)._1;
        }
      }},windowSize * 4){vc => vc.pos}{vc => vc.chrom}{vc => (vc.pos,vc.ref,vc.alt.head)}, closeAction = (() => {
        reportln("Trimmed "+trimCt+" lines and leftAligned "+leftAlignCt+" lines.","note");
      }))
      ), newHeader);
      
    }
    
    //def oldWalkVCF(vcIter : Iterator[VariantContext], vcfHeader : VCFHeader, verbose : Boolean = true) : (Iterator[VariantContext],VCFHeader) = {
     // 
    //}
  }
  

  
  var leftAlignCt = 0;
  var trimCt = 0;
  
  def trimAlignNoLib(v : SVcfVariantLine, ref : ReferenceContext, tagPrefix : Option[String] = None, windowSize : Int = 200) : (SVcfVariantLine,Int) = {

    
    val vc = makeSimpleVariantContext(v);
    //val (latVC,diff) = trimAlignVC(vc,ref);
    val tvc = GATKVariantContextUtils.trimAlleles(vc, true, true)
    val trimBP = vc.getReference().getBaseString().length - tvc.getReference().getBaseString().length;
    if(tvc.getReference().getBaseString().toUpperCase != vc.getReference().getBaseString().toUpperCase){
      tally("TRIMMED",1)
      trimCt += 1;
    }
    val (latVC,diff) =  alignVC(tvc,ref,windowSize = windowSize);
    //alignAndWrite(final VariantContext vc, final ReferenceContext ref)
    //val (latVC,diff) = (latVCpair.first,latVCpair.second)
    if(diff != 0){
      tally("SHIFTED_LEFTALIGN",1)
      leftAlignCt += 1;
    }
    val outV = makeSimpleSVcfLine(latVC,Some(v));
    val isChanged : String = if( v.pos != outV.pos || v.ref != outV.ref || v.alt != outV.alt){
      tally("CHANGE_LEFTALIGNANDTRIM",1)
      "1"
    } else {
      "0"
    }
    tagPrefix.foreach{ tp => {
      outV.addInfo(tp+"OFFSET",""+diff);
      outV.addInfo(tp+"LAT",isChanged);
      outV.addInfo(tp+"TRIMBP",""+trimBP);
    }}
    (outV,diff);
  }
  
  def trimAlign(v : SVcfVariantLine, ref : ReferenceContext, tagPrefix : Option[String] = None) : (SVcfVariantLine,Int) = {
    val vc = makeSimpleVariantContext(v);
    //val (latVC,diff) = trimAlignVC(vc,ref);
    val tvc = GATKVariantContextUtils.trimAlleles(vc, true, true)
    val trimBP = vc.getReference().getBaseString().length - tvc.getReference().getBaseString().length;
    if(tvc.getReference().getBaseString().toUpperCase != vc.getReference().getBaseString().toUpperCase){
      trimCt += 1;
    }
    val latVCpair =  org.broadinstitute.gatk.tools.walkers.variantutils.LeftAlignAndTrimVariants.alignAndWrite(tvc,ref);
    //alignAndWrite(final VariantContext vc, final ReferenceContext ref)
    val (latVC,diff) = (latVCpair.first,latVCpair.second)
    if(diff != 0){
      leftAlignCt += 1;
    }
    val outV = makeSimpleSVcfLine(latVC,Some(v));
    val isChanged : String = if( v.pos != outV.pos || v.ref != outV.ref || v.alt.head != outV.alt.head){
      "1"
    } else {
      "0"
    }
    tagPrefix.foreach{ tp => {
      outV.addInfo(tp+"OFFSET",""+diff);
      outV.addInfo(tp+"TRIMBP",""+trimBP);
      outV.addInfo(tp+"LAT",isChanged);
    }}
    (outV,diff);
  }
  
  /*def leftAlign_translated(vc : VariantContext, ref : ReferenceContext, windowSize : Int) : (VariantContext,Int) = {
        if (!vc.isIndel() || vc.isComplexIndel() ) {
            return (vc,0);
        }
        val indelLength = if ( vc.isSimpleDeletion() ){
            vc.getReference().length() - 1;
        } else { 
            vc.getAlternateAllele(0).length() - 1;
        }
        if(indelLength > windowSize){
           return (vc,0)
        }
        if (vc.getReference().getBases().head != vc.getAlternateAllele(0).getBases().head){
          return (vc,0)
        }
        
          
  }*/
  

  def alignVC(tvc : VariantContext, ref : ReferenceContext, 
        //numBiallelics: Int = 0,
         //dontTrimAlleles : Boolean = false,
         //splitMultiallelics  : Boolean = false,
         windowSize : Int = 200
      ) : (VariantContext, Int) = {
    val MAX_INDEL_LENGTH = windowSize;

    val refAlle = tvc.getReference()
    val refAlleStr = tvc.getReference().getBaseString()
    val altAlle = tvc.getAlternateAlleles().asScala.head
    val alt = altAlle.getBaseString();
    
    if(refAlleStr.length != 1 || alt.length != 1){
        // get the indel length
        val indelLength = if ( tvc.isSimpleDeletion() ){  tvc.getReference().length() - 1;
                           } else { tvc.getAlternateAllele(0).length() - 1 }
        if ( indelLength > MAX_INDEL_LENGTH ){
          return (tvc,0)
        }
        if(refAlleStr.head != alt.head){
          return (tvc,0)
        }
        val refSeq = refToUpper(ref.getBases());
        val originalIndex = tvc.getStart() - ref.getWindow().getStart() + 1;
        if(originalIndex < 0 || originalIndex >= ref.getBases().length){
          return (tvc,0)
        }
        try{
          
        val  originalIndel = makeHaplotype(tvc, refSeq, originalIndex, indelLength);

        var elements = new java.util.ArrayList[CigarElement]();
        elements.add(new CigarElement(originalIndex, CigarOperator.M));
        val op = if(tvc.isSimpleDeletion()){CigarOperator.D} else {CigarOperator.I}
        elements.add(new CigarElement(indelLength, op));
        elements.add(new CigarElement(refSeq.length - originalIndex, CigarOperator.M));
        val originalCigar = new Cigar(elements);
        val newCigar = AlignmentUtils.leftAlignIndel(originalCigar, refSeq, originalIndel, 0, 0, true);
        if ( !newCigar.equals(originalCigar) && newCigar.numCigarElements() > 1 ) {
          val difference = originalIndex - newCigar.getCigarElement(0).getLength();
          
          val nvc = new VariantContextBuilder(tvc).start(tvc.getStart()-difference).stop(tvc.getEnd()-difference).make();
          val indelIndex = originalIndex-difference;
          val newBases = Array.ofDim[Byte](indelLength + 1);
          newBases(0) = refSeq(indelIndex-1);
          val rr = if(tvc.isSimpleDeletion()) refSeq else originalIndel
          System.arraycopy(rr, indelIndex, newBases, 1, indelLength);
          val newAllele = Allele.create(newBases, tvc.isSimpleDeletion());
          val newAlles = if(tvc.isSimpleDeletion()){
            List( newAllele, Allele.create(newAllele.getBases()(0),false) )
          } else {
            List( Allele.create(newAllele.getBases()(0), true), newAllele )
          }
          var newVC = new VariantContextBuilder(nvc).alleles(newAlles.asJava).make()
          //var newVC = new VariantContextBuilder(tvc).start(tvc.getStart()-difference).stop(tvc.getEnd()-difference).alleles(newAlles.asJava).make();
          //newVC = updateAllele(newVC, newAllele);
          return (newVC,1);
        } else {
          return (tvc,0);
        }
        } catch {
          case e : Throwable => {
            reportln("caught error:","note")
            reportln("tvc("+tvc.getContig()+":"+tvc.getStart()+":"+tvc.getReference().getBaseString()+">"+altAlle.getBaseString(),"note");
            reportln("refSeq.length = "+refSeq.length,"note");
            reportln("originalIndex="+originalIndex,"note")
            reportln("indelLength="+indelLength,"note")
            reportln("","note")
            reportln("","note")
            throw e;
          }
        }
        return (tvc,0);
    } else {
      return (tvc,0);
    }
    
  }
  
  def trimAlignVC(vc : VariantContext, ref : ReferenceContext, 
        //numBiallelics: Int = 0,
         //dontTrimAlleles : Boolean = false,
         //splitMultiallelics  : Boolean = false,
         windowSize : Int = 200
      ) : (VariantContext, Int) = {
    val MAX_INDEL_LENGTH = windowSize;
    val refLength = vc.getReference().length();
    if ( refLength > MAX_INDEL_LENGTH ) {
            notice(REFERENCE_ALLELE_TOO_LONG_MSG+" ("+refLength+") at position "+vc.getContig()+":"+vc.getStart()+"; skipping that record.","REFALLE_TOO_LONG",10);
            return (vc,0);
    }
    val tvc = GATKVariantContextUtils.trimAlleles(vc, true, true)
    val refAlle = tvc.getReference()
    val refAlleStr = tvc.getReference().getBaseString()
    val altAlle = tvc.getAlternateAlleles().asScala.head
    val alt = altAlle.getBaseString();
    
    if(refAlleStr.length != 1 || alt.length != 1){
        // get the indel length
        val indelLength = if ( tvc.isSimpleDeletion() ) tvc.getReference().length() - 1;
                          else tvc.getAlternateAllele(0).length() - 1;
        if ( indelLength > MAX_INDEL_LENGTH ){
          return (tvc,0)
        }
        if(refAlleStr.head != alt.head){
          return (tvc,0)
        }
        val refSeq = ref.getBases();
        val originalIndex = tvc.getStart() - ref.getWindow().getStart() + 1;
        if(originalIndex < 0 || originalIndex >= ref.getBases().length){
          return (tvc,0)
        }
        val  originalIndel = makeHaplotype(tvc, refSeq, originalIndex, indelLength);
        var elements = new java.util.ArrayList[CigarElement]();
        elements.add(new CigarElement(originalIndex, CigarOperator.M));
        val op = if(tvc.isSimpleDeletion()){CigarOperator.D} else {CigarOperator.I}
        elements.add(new CigarElement(indelLength, op));
        elements.add(new CigarElement(refSeq.length - originalIndex, CigarOperator.M));
        val originalCigar = new Cigar(elements);
        val newCigar = AlignmentUtils.leftAlignIndel(originalCigar, refSeq, originalIndel, 0, 0, true);
        if ( !newCigar.equals(originalCigar) && newCigar.numCigarElements() > 1 ) {
          val difference = originalIndex - newCigar.getCigarElement(0).getLength();
          val indelIndex = originalIndex-difference;
          val newBases = Array.ofDim[Byte](indelLength + 1);
          newBases(0) = refSeq(indelIndex-1);
          val rr = if(tvc.isSimpleDeletion()) refSeq else originalIndel
          System.arraycopy(rr, indelIndex, newBases, 1, indelLength);
          val newAllele = Allele.create(newBases, tvc.isSimpleDeletion());
          val newAlles = if(tvc.isSimpleDeletion()){
            List( tvc.getReference(),Allele.create(newAllele.getBases()(0),false) )
          } else {
            List( Allele.create(newAllele.getBases()(0), true), altAlle )
          }
          var newVC = new VariantContextBuilder(tvc).start(tvc.getStart()-difference).stop(tvc.getEnd()-difference).alleles(newAlles.asJava).make();
          //newVC = updateAllele(newVC, newAllele);
          return (newVC,1);
        } else {
          return (tvc,0);
        }
    } else {
      return (tvc,0);
    }
    
  }
  
  def makeHaplotype(vc : VariantContext, ref : Array[Byte], indexOfRef : Int, indelLength : Int) : Array[Byte] = {
        var indexOfRefVar = indexOfRef
        val hapLen = if(vc.isSimpleDeletion()){
          ref.length - indelLength
        } else {
          ref.length + indelLength
        }
        val hap : Array[Byte] = Array.ofDim[Byte](hapLen);

        // add the bases before the indel
        System.arraycopy(ref, 0, hap, 0, indexOfRef);
        var currentPos = indexOfRef;

        // take care of the indel
        if ( vc.isSimpleDeletion() ) {
            indexOfRefVar += indelLength;
        } else {
            System.arraycopy(vc.getAlternateAllele(0).getBases(), 1, hap, currentPos, indelLength);
            currentPos += indelLength;
        }

        
        try{
        // add the bases after the indel
           System.arraycopy(ref, indexOfRefVar, hap, currentPos, ref.length - indexOfRefVar);
        } catch {
          case e : Throwable => {
            reportln("caught error:","note")
            reportln("tvc("+vc.getContig()+":"+vc.getStart()+":"+vc.getReference().getBaseString()+">"+vc.getAlternateAlleles().asScala.head.getBaseString(),"note");
            reportln("ref.length = "+ref.length,"note");
            reportln("indexOfRef="+indexOfRef,"note")
            reportln("indelLength="+indelLength,"note")
            reportln("indexOfRefVar="+indexOfRefVar,"note")
            reportln("currentPos="+currentPos,"note")
            reportln("hap.length="+hap.length,"note")
            reportln("","note")
            reportln("","note")
            throw e;
          }
        }
        
        
        return hap;
    }
}
