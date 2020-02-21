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

import jigwig.BigWigFile;

//import com.timgroup.iterata.ParIterator.Implicits._;

object SVcfTXanno {

  
    val SNVVARIANT_BASESWAP_LIST = Seq( (("A","C"),("T","G")),
                            (("A","T"),("T","A")),
                            (("A","G"),("T","C")),
                            (("C","A"),("G","T")),
                            (("C","T"),("G","A")),
                            (("C","G"),("G","C"))
                          );
  val  vcfc : VCFAnnoCodes = VCFAnnoCodes()
/*
  case class createTxBed(
             //tx annotation:
                gtffile : Option[String], 
                genomeFA : Option[String], 
                txInfoFile : Option[String],
             // tags:
                
             //options:
                
                summaryWriter : Option[WriterUtil],
                
                addStopCodon : Boolean,
                inputSavedTxFile : Option[String],
                outputSavedTxFile : Option[String],
                chromList : Option[List[String]],
                txToGeneFile : Option[String],
                bufferSize : Int = 32, 
                
                geneList : Option[List[String]] = None,
                txTypes : Option[List[String]] = None
                ) extends internalUtils.VcfTool.SVcfWalker {
    
  }*/

  case class AddTxAnno(gtffile : Option[String], 
                genomeFA : Option[String], 
                //outfile : String, 
                //summaryFile : Option[String],
                summaryWriter : Option[WriterUtil],
                txInfoFile : Option[String],
                addStopCodon : Boolean,
                inputSavedTxFile : Option[String],
                outputSavedTxFile : Option[String],
                chromList : Option[List[String]],
                txToGeneFile : Option[String],
                bufferSize : Int = 32, 
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
                                                                   ("chromList",fmtOptionList(chromList)),
                                                                   ("txToGeneFile",txToGeneFile.getOrElse("None")),
                                                                   ("bufferSize",bufferSize.toString)
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
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vType_TAG, ".", "String", "For each allele, a |-delimited list indicating the deletion type for each overlapping transcript (see "+vcfCodes.txList_TAG+" for the transcripts and "+vcfCodes.vMutLVL_TAG+" for info on the type description)"),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutG_TAG, ".", "String", "For each allele, the genomic change, in HGVS format."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutR_TAG, ".", "String", "For each allele, a |-delimited list indicating mRNA change (in HGVS format) for each transcript (see "+vcfCodes.txList_TAG+")"),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutC_TAG+"_offset", ".", "String", "Realign offsets for field "+vcfCodes.vMutC_TAG),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutC_TAG+"_offsetVAR", ".", "String", "variant info with Realign offsets for field "+vcfCodes.vMutC_TAG),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutC_TAG, ".", "String", "For each allele, a |-delimited list indicating cDNA change (in HGVS format) for each transcript (see "+vcfCodes.txList_TAG+")"),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutP_TAG, ".", "String", "For each allele, a |-delimited list indicating amino-acid change (in HGVS format) for each transcript (see "+vcfCodes.txList_TAG+")"),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutP_TAG+"_offset", ".", "String", "With 3prime realign. For each allele, a |-delimited list indicating amino-acid change (in HGVS format) for each transcript (see "+vcfCodes.txList_TAG+")"),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vTypeShort_TAG, ".", "String", "For each allele, the worst amino acid change type found over all transcripts."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutPShort_TAG, ".", "String", "For each allele, one of the protein changes for the worst variant type found over all transcripts"),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutINFO_TAG, ".", "String", "Raw variant info for each allele."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutLVL_TAG, ".", "String", 
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
      }) ++ rightAligner.toList.flatMap{raln => {
        
        List(
            new SVcfCompoundHeaderLine(in_tag = "INFO",OPTION_TAGPREFIX+"txMeta_alnRawVar", ".", "String", "Original aligned version of the variant."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",OPTION_TAGPREFIX+"txMeta_alnRgtVar", ".", "String", "Right-aligned version of the variant."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutC_TAG+"_alnRgt", ".", "String", "Right-aligned version of field "+vcfCodes.vMutC_TAG),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutC_TAG+"_alnRaw", ".", "String", "Raw-aligned version of field "+vcfCodes.vMutC_TAG),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutP_TAG+"_alnRgt", ".", "String", "Right-aligned version of field "+vcfCodes.vMutP_TAG),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vType_TAG+"_alnRgt", ".", "String", "Right-aligned version of field "+vcfCodes.vType_TAG),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vTypeShort_TAG+"_alnRgt", ".", "String", "Right aligned version. For each allele, the worst amino acid change type found over all transcripts."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutPShort_TAG+"_alnRgt", ".", "String", "Right aligned version. For each allele, one of the protein changes for the worst variant type found over all transcripts"),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutINFO_TAG+"_alnRgt", ".", "String", "Right aligned version. Raw variant info for each allele."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutLVL_TAG+"_alnRgt", ".", "String", "Right aligned version. See "+vcfCodes.vMutLVL_TAG),
            new SVcfCompoundHeaderLine(in_tag = "INFO",OPTION_TAGPREFIX+"txSummary_WARN_lvlChange", ".", "String", "Flag. Equals 1 iff the summary variant changes varLvl between PLOF, LLOF, and NONSYNON levels when right aligned."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",OPTION_TAGPREFIX+"txSummary_WARN_typeChange", ".", "String", "Flag. Equals 1 iff the summary variant major type changes between or among types contained in PLOF, LLOF, and NONSYNON variant levels when right aligned."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",OPTION_TAGPREFIX+"tx_WARN_lvlChange", ".", "String", "Flag. Equals 1 iff the variant changes varLvl between PLOF, LOF, and NONSYNON levels when right aligned. Equals dot if the variant is synon, unk, or psynon for both alignments."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",OPTION_TAGPREFIX+"tx_WARN_typeChange", ".", "String", "Flag. For each TX, Equals 1 iff the variant changes major type when right aligned. Equals dot if the variant is synon, unk, or psynon for both alignments.")
        )
      }}
      //vcfCodes.vTypeShort_TAG+"_alnRgt"
      //vcfCodes.vMutPShort_TAG+"_alnRgt"
      //vcfCodes.vMutLVL_TAG+"_alnRgt"
      
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
       newHeader.reportAddedInfos(this)
      return (vcFlatMap(vcIter)(v => {
             annotateSVcfStreamOpt(v,summaryWriter,vcfCodes,bufferSize,txgaos,TXSeq,txToGene,overwriteInfos=overwriteInfos)
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
                        overwriteInfos : Set[String]
                        ) : Option[SVcfVariantLine] = {
      
      
      var vb = v.getOutputLine();
      vb.dropInfo(overwriteInfos);

      
      val refAlle = v.ref;
      val altAlleleSet = v.alt.filter{ alt => alt != internalUtils.VcfTool.UNKNOWN_ALT_TAG_STRING && alt != internalUtils.VcfTool.MISSING_VALUE_STRING }
      if(altAlleleSet.length > 1){
        error("ERROR: cannot perform TX annotation on multiallelics. Split multiallelics first!");
      }
      
      if(altAlleleSet.length == 1){
        val start = v.pos - 1
        val end = start + refAlle.length() // math.max(refAlle.length(), altAlleles.map(a => a.length()).max)
        val altAlle = altAlleleSet.head
        val txList = txgaos.findIntersectingSteps(
            GenomicInterval(chromName = v.chrom, strand = '.', start = start - bufferSize, end = end + bufferSize)
        ).foldLeft(Set[String]()){case (soFar,(iv,gset))=>{ soFar ++ gset }}.filter(TXSeq.contains(_)).filter{TXSeq(_).isValidFullLenTX}.toVector.sorted;
        if(txList.length > 0){
        
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
        case class TxVarInfo( var vType : Option[String] = None, var vMutR : Option[String] = None, var vMutC : Option[String] = None, 
             var vMutP : Option[pVariantInfo] = None, var vMutPra : Option[pVariantInfo] = None,var  vMutCoffset : Option[Int] = None, 
             var vMutCoffsetvar : Option[String] = None, var vLvl : Option[String] = None, var vInfo : Option[String] = None){
          
        }
        
        val (ref,alt) = (refAlle, altAlle);
        val mutG = "g."+getMutString(ref, alt, pos = start, getPosString = {(a) => (a + 1).toString},swapStrand=false);
        val (trimPos,trimRef,trimAlt) = trimmed(v.pos,ref,alt);
        val trimString = v.chrom+":"+trimPos+":"+trimRef+">"+trimAlt
        val rightAln = rightAligner.map{ rta => rta.rightAlignIndelFromFA(v.chrom,v.pos,ref,alt) }
        val rgtAlnString = rightAln.map{ case (posrgt,refrgt,altrgt) => {
            val startrgt = posrgt - 1;
            v.chrom+":"+posrgt+":"+refrgt+">"+altrgt
        }}
        
        val txct = txList.length;
        
        val txInfoList = txList.zipWithIndex.map{ case (txid,txix) => (txid,TXSeq(txid),txix) }.map{ case (txid,tx,txix) => {
          
          val txdata = TxVarInfo();
          
          txdata.vMutR   = Some("r."+tx.getRnaMut(ref,alt,start))  //getMutString(ref,alt, pos = start, getPosString = {(g) => tx.getRPos(g)}, swapStrand = (tx.strand == '-'));
          val (mutCraw,(racRef,racAlt,racPos),racOffset) = tx.getCdsMutInfo(ref,alt,start)
          txdata.vMutC   = Some("c."+mutCraw)
          txdata.vMutP   = Some(tx.getProteinMut(ref,alt,gPos=start))
          txdata.vMutPra = Some(tx.getProteinMut(racRef,racAlt,gPos=racPos))
          txdata.vMutCoffset = Some(racOffset)
          txdata.vMutCoffsetvar = Some((racPos+":"+racRef+">"+racAlt))
          txdata.vType = Some(  txdata.vMutP.get.varType )
          txdata.vLvl = Some(txdata.vMutP.get.severityType)
          txdata.vInfo = Some(txdata.vMutP.get.saveToString());
          txdata;
        }}

        val txInfoListRight = rightAln.map{ case (posrgt,refrgt,altrgt) => {
            txList.zipWithIndex.map{ case (txid,txix) => (txid,TXSeq(txid),txix) }.map{ case (txid,tx,txix) => {
              val startrgt = posrgt - 1;
              val txdataRGT = TxVarInfo();
              val txdataRAW = TxVarInfo();
              
              val mutPrgt   = tx.getProteinMut(refrgt,altrgt,gPos=startrgt);
              val mutCraw   = tx.getSimpleCdsMutString(ref,alt,gPos=start);
              val mutCrgt   = tx.getSimpleCdsMutString(refrgt,altrgt,gPos=startrgt);
              
              txdataRGT.vMutC = Some(mutCrgt);
              txdataRAW.vMutC = Some(mutCraw);
              txdataRGT.vMutP = Some(mutPrgt);
              txdataRGT.vType = Some(mutPrgt.varType)
              txdataRGT.vLvl  = Some(mutPrgt.severityType)
              txdataRGT.vInfo = Some(mutPrgt.saveToString())
              (txdataRGT,txdataRAW)
            }}
        }}
        val (mutPshort,typeShort,vLvl) = internalUtils.TXUtil.getWorstProteinMut(txInfoList.map{ txdata => {
          (txdata.vMutP.get.pvar,txdata.vType.get)
        }},txList);
        val lvlInfoRgt = rightAligner.map{ ra => {
          internalUtils.TXUtil.getWorstProteinMut(txInfoList.map{ txdata => {
              (txdata.vMutP.get.pvar,txdata.vType.get)
          }},txList);
        }}
        
        
         /* rightAligner.foreach{ ra => {
            val (mutPshortrgt,typeShortrgt,vLvlrgt) = internalUtils.TXUtil.getWorstProteinMut(vMutPrgt.last.zip(vTypeListrgt.last),txList);
            vMutPshortRgt = vMutPshortRgt :+ mutPshortrgt;
            vTypeListShortRgt = vTypeListShortRgt :+ typeShortrgt;
            vLevelRgt = vLevelRgt :+ vLvlrgt;
            val (lvlDiff, typeDiff) = vLevelList.last.zip(vLevelListRgt.last).zip(vTypeList.last).zip(vTypeListrgt.last).map{ case (((lvlRaw,lvlRgt),typeRaw),typeRgt) => {
              if( NONSYNON_LEVELS.contains(lvlRaw) || NONSYNON_LEVELS.contains(lvlRgt) ){
                ((if(lvlRaw != lvlRgt){"1"}else{"0"}) , (if(typeRaw != typeRgt){"1"}else{"0"}))
              } else {
                (".",".")
              }
            }}.unzip
            vLvlDiff = vLvlDiff :+ lvlDiff
            vTypeDiff = vTypeDiff :+ typeDiff
            
            
          }}*/
        //}

        vb.addInfo(vcfCodes.txList_TAG, txList.padTo(1,".").mkString(","));
        vb.addInfo(vcfCodes.vMutG_TAG, mutG);
        vb.addInfo(vcfCodes.vMutR_TAG, (txInfoList.map{_.vMutR.get}.padTo(1,".")).mkString(","));
        vb.addInfo(vcfCodes.vMutC_TAG, (txInfoList.map{_.vMutC.get}.padTo(1,".")).mkString(","));
        vb.addInfo(vcfCodes.vMutC_TAG+"_offset", (txInfoList.map{_.vMutCoffset.get}.padTo(1,".")).mkString(","));
        vb.addInfo(vcfCodes.vMutC_TAG+"_offsetVAR", (txInfoList.map{_.vMutCoffsetvar.get}.padTo(1,".")).mkString(","));
        vb.addInfo(vcfCodes.vMutP_TAG, (txInfoList.map{_.vMutP.get.pvar}.padTo(1,".")).mkString(","));
        vb.addInfo(vcfCodes.vMutP_TAG+"_offset", (txInfoList.map{_.vMutPra.get.pvar}.padTo(1,".")).mkString(","));

        vb.addInfo(vcfCodes.vType_TAG, (txInfoList.map{_.vType.get}.padTo(1,".")).mkString(","));
        vb.addInfo(vcfCodes.vMutINFO_TAG, (txInfoList.map{_.vInfo.get}.padTo(1,".")).mkString(","));

        
        vb.addInfo(vcfCodes.vTypeShort_TAG, typeShort);
        vb.addInfo(vcfCodes.vMutPShort_TAG, mutPshort);
        vb.addInfo(vcfCodes.vMutLVL_TAG, vLvl);
        
        txInfoListRight.foreach{ td => {
          //trimString rgtAlnString
          // (txdataRGT,txdataRAW)
          val (mutPshortRGT,typeShortRGT,vLvlRGT) = internalUtils.TXUtil.getWorstProteinMut(td.map{ case (txdata,txdataRAW) => {
                (txdata.vMutP.get.pvar,txdata.vType.get)
            }},txList);
          //val (mutPshortRAW,typeShortRAW,vLvlRAW) = internalUtils.TXUtil.getWorstProteinMut(td.map{ case (txdataRGT,txdata) => {
          //      (txdata.vMutP.get.pvar,txdata.vType.get)
          //  }},txList);
          vb.addInfo(OPTION_TAGPREFIX+"txMeta_alnRawVar",trimString);
          vb.addInfo(OPTION_TAGPREFIX+"txMeta_alnRgtVar",rgtAlnString.get);
          vb.addInfo(vcfCodes.vMutP_TAG+"_alnRgt",(td.map{ _._1.vMutP.get.pvar }.padTo(1,".")).mkString(","));
          vb.addInfo(vcfCodes.vType_TAG+"_alnRgt",(td.map{ _._1.vType.get      }.padTo(1,".")).mkString(","));
          vb.addInfo(vcfCodes.vMutC_TAG+"_alnRaw",(td.map{ _._2.vMutC.get      }.padTo(1,".")).mkString(","));
          vb.addInfo(vcfCodes.vMutC_TAG+"_alnRgt",(td.map{ _._1.vMutC.get      }.padTo(1,".")).mkString(","));
          
          val vLvlDiff = td.zip(txInfoList).map{ case ((txdRGT,txdRAW),txd) => {
            if( txdRGT.vLvl.get != txd.vLvl.get ){
              "1"
            } else "0"
          }}
          val vTypeDiff = td.zip(txInfoList).map{ case ((txdRGT,txdRAW),txd) => {
            if(txdRGT.vType.get != txd.vType.get ){
              "1"
            } else "0"
          }}
          vb.addInfo(OPTION_TAGPREFIX+"tx_WARN_lvlChange",(vLvlDiff.padTo(1,".")).mkString(","));
          vb.addInfo(OPTION_TAGPREFIX+"tx_WARN_typeChange",(vTypeDiff.padTo(1,".")).mkString(","));
          
          vb.addInfo(vcfCodes.vTypeShort_TAG+"_alnRgt",typeShortRGT);
          vb.addInfo(vcfCodes.vMutPShort_TAG+"_alnRgt",mutPshortRGT);
          vb.addInfo(vcfCodes.vMutLVL_TAG+"_alnRgt",   vLvlRGT);
          
          val sumLvlDiff = if( vLvlDiff.contains("1")){
            "1"
          } else "0"
          val sumTypeDiff = if( vTypeDiff.contains("1")){
            "1"
          } else "0"
          
          vb.addInfo(OPTION_TAGPREFIX+"txSummary_WARN_lvlChange",sumLvlDiff);
          vb.addInfo(OPTION_TAGPREFIX+"txSummary_WARN_typeChange",sumTypeDiff);
          
        }}
        

      }}
      return Some(vb);
  }
    
    }
   

    
    
  
}









 


















