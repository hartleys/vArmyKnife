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

import htsjdk.samtools.SAMFileReader;
import htsjdk.samtools.SAMRecord;
import internalUtils.SamTool._;

import htsjdk.samtools.SAMFileHeader;


//import com.timgroup.iterata.ParIterator.Implicits._;

object SVcfBamWalker {
  
  case class BamDepthWalker(bamfile : String, 
                            tagid : String = "BAM_DEPTH", 
                            tagDesc : Option[String],
                            isSingleEnd: Boolean) extends SVcfWalker {
    def walkerName : String = "BamDepthWalker"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("tagPrefix", bamfile)
    );
    
    var (bamReader,bamHeader,bamAttrib) :  (Iterator[(SAMRecord,SAMRecord)],SAMFileHeader,internalUtils.SamTool.SamFileAttributes) = 
      getPairedEndReader(bamfile,isSingleEnd,sortPairsByFirstPosition=true,
                          unsorted=false,
                          maxPhredScore=41,
                          stopAfterNReads = None,
                          testRunLineCt = 0,
                          testRun = false,
                          maxReadLength = None,
                          strict = true);
    
    def findChrom(chr : String){
      bamReader = bamReader.dropWhile{ case (a,b) => a.getContig() != chr}
      if(! bamReader.hasNext ){
        bamReader = 
          (getPairedEndReader(bamfile,isSingleEnd,sortPairsByFirstPosition=true,
                              unsorted=false,
                              maxPhredScore=41,
                              stopAfterNReads = None,
                              testRunLineCt = 0,
                              testRun = false,
                              maxReadLength = None,
                              strict = true))._1
        bamReader = bamReader.dropWhile{ case (a,b) => a.getContig() != chr}
        if(! bamReader.hasNext){
          warning("Warning: chromosome not found in bam file: \""+chr+"\"","CHROM_NOT_IN_BAM",100);
          bamReader = Iterator[(SAMRecord,SAMRecord)]();
        }
      }
    }
    var currChrom = "";
    var currPos = 0;
    
    
    /*
     getPairedEndReader(inbam : String, 
                         isSingleEnd : Boolean,
                         sortPairsByFirstPosition : Boolean = false,
                         unsorted : Boolean = true,
                         maxPhredScore : Int = 41,
                         stopAfterNReads : Option[Int],
                         testRunLineCt : Int,
                         testRun : Boolean,
                         maxReadLength : Option[Int],
                         strict : Boolean = true) : (Iterator[(SAMRecord,SAMRecord)],SAMFileHeader,SamFileAttributes) 
     */
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      var errCt = 0;
      
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      var idx = 0;
      outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",tagid,Number=".",Type="String",
                               desc=tagDesc.map{_+" "}.getOrElse("")+"Read depth found in bam file: \""+bamfile+"\""));
      
      
      
      
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        //val vc = v.getOutputLine()
        val vc = v.getOutputLine();
        
        
        
        vc.addInfo(tagid,".")
        idx = idx + 1;
        vc
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
      
    }
  }
  
  
}









 


















