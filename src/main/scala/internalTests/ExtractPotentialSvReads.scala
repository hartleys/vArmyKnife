package internalTests


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

import htsjdk.samtools._;
import internalUtils.fileUtils._;

import internalUtils.dbUtils._;
import internalUtils.commandLineUI._;

object ExtractPotentialSvReads {
  

class CmdExtractPotentialSvReads extends CommandLineRunUtil {
     override def priority = 20;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "extractPotentialSvReads", 
          quickSynopsis = "", 
          synopsis = "", 
          description = ""+ALPHA_WARNING,
          argList = 

                    new BinaryArgument[Int](
                                         name = "gapSize", 
                                         arg = List("--gapSize"), 
                                         valueName = "500",  
                                         argDesc =  "Minimum gap size.",
                                         defaultValue = Some(500)
                                        ) ::
                    new FinalArgument[String](
                                         name = "infile",
                                         valueName = "infile",
                                         argDesc = "Input bam file."// description
                                        ) ::
                    new FinalArgument[String](
                                         name = "outfile",
                                         valueName = "outfile.bam",
                                         argDesc = "The output bam file."// description
                                        ) ::
                    internalUtils.commandLineUI.CLUI_UNIVERSAL_ARGS );

     def run(args : Array[String]) {
       val out = parser.parseArguments(args.toList.tail);
       if(out){
         extractPotentialSvReads(
             infile = parser.get[String]("infile"),
             outfile = parser.get[String]("outfile"),
             gapSize = parser.get[Int]("gapSize")
         )
       }
     }
  }

  def extractPotentialSvReads( infile : String, outfile : String, gapSize : Int,
                              dropDuplicate : Boolean = true,
                              dropImproperPair : Boolean = false,
                              dropNonPrimaryAlign : Boolean = true){
    val readerFactory = SamReaderFactory.makeDefault();

    val reader = if(infile == "-"){
      readerFactory.open(SamInputResource.of(System.in));
    } else {
      readerFactory.open(new File(infile));
    }
    val header = reader.getFileHeader();
    val outHeader = header.clone();
    val writer = if(outfile == "-"){
      (new SAMFileWriterFactory()).makeSAMWriter(outHeader, false, System.out );
    } else {
      (new SAMFileWriterFactory()).makeBAMWriter( outHeader, false, new java.io.File( outfile ))
    }
      
      //(new SAMFileWriterFactory()).makeBAMWriter( outHeader, false, new java.io.File( outfile ))
    
    /*
     internalUtils.stdUtils.wrapIteratorWithAdvancedProgressReporter[SVcfInputVariantLine](
                           //rawVariantLines.map(line => SVcfInputVariantLine(line,header)),
                           bufLines.map(line => SVcfInputVariantLine(line).headerInput(header)), 
                           internalUtils.stdUtils.AdvancedIteratorProgressReporter_ThreeLevelAuto[SVcfInputVariantLine](
                                elementTitle = "lines", lineSec = 60,
                                reportFunction  = ((vc : SVcfInputVariantLine, i : Int) => " " + vc.chrom +" "+ internalUtils.stdUtils.MemoryUtil.memInfo )
                           )
                         )
     */
    var keepCt = 0;
    var passFiltCt = 0;
    var diffChromCt = 0;
    var distPosCt = 0;
    val readIterator = internalUtils.stdUtils.wrapIteratorWithAdvancedProgressReporter[SAMRecord](
            reader.iterator().asScala.filter{ rr : SAMRecord => {
                val passFilt = ( ! rr.getMateUnmappedFlag() ) &&
                           ( ! rr.getReadUnmappedFlag() ) &&
                           ( ( ! dropDuplicate ) || ( ! rr.getDuplicateReadFlag() ) ) &&
                           ( ( ! dropImproperPair ) || ( rr.getProperPairFlag() ) ) && 
                           ( ( ! dropNonPrimaryAlign) || ( ! rr.getNotPrimaryAlignmentFlag() ) )
                val diffChrom = ( rr.getReferenceIndex() != rr.getMateReferenceIndex() )
                val sameChrom = ( rr.getReferenceIndex() == rr.getMateReferenceIndex() )
                val distPos   = sameChrom && (
                         ( rr.getAlignmentStart() < rr.getMateAlignmentStart() && rr.getAlignmentStart() + gapSize < rr.getMateAlignmentStart() ) ||
                         ( rr.getAlignmentStart() > rr.getMateAlignmentStart() && rr.getAlignmentStart() > gapSize + rr.getMateAlignmentStart() )
                    )
                
                val keep = passFilt && ( diffChrom || distPos )
                if( passFilt ){ passFiltCt += 1; }
                if( passFilt && diffChrom ){ diffChromCt += 1; }
                if( passFilt && distPos ){ distPosCt   += 1; }
                if( keep                  ){ keepCt += 1; }
                keep
              }},
            internalUtils.stdUtils.AdvancedIteratorProgressReporter_ThreeLevelAuto[SAMRecord](
                elementTitle = "lines", lineSec = 60,
                reportFunction  = ((rr : SAMRecord, i : Int) => " " + rr.getReferenceName()+":"+rr.getAlignmentStart() +": [PF="+passFiltCt+", diffChrom="+diffChromCt+", distPos="+distPosCt+", keep="+keepCt+", total="+i+"] "+ internalUtils.stdUtils.MemoryUtil.memInfo )
             )
        )
    
    pullSplitReads(readIterator, gapSize = gapSize, 
        dropDuplicate=dropDuplicate,dropImproperPair=dropImproperPair,dropNonPrimaryAlign=dropNonPrimaryAlign).foreach{ rr : SAMRecord => {
           writer.addAlignment(rr)
        }}
    writer.close();
  }
  
  def pullSplitReads( iter : Iterator[SAMRecord], gapSize : Int = 500,
                        dropDuplicate : Boolean = true,
                        dropImproperPair : Boolean = false,
                        dropNonPrimaryAlign : Boolean = true,
                        ) : Iterator[SAMRecord] = {
    
    iter.filter{ rr : SAMRecord => {
      val keep = ( ! rr.getMateUnmappedFlag() ) &&
                 ( ! rr.getReadUnmappedFlag() ) &&
                 ( ( ! dropDuplicate ) || ( ! rr.getDuplicateReadFlag() ) ) &&
                 ( ( ! dropImproperPair ) || ( rr.getProperPairFlag() ) ) && 
                 ( ( ! dropNonPrimaryAlign) || ( ! rr.getNotPrimaryAlignmentFlag() ) )
      keep && (
          ( rr.getReferenceIndex() != rr.getMateReferenceIndex() ) ||
          ( rr.getAlignmentStart() < rr.getMateAlignmentStart() + gapSize ) ||
          ( rr.getAlignmentStart() + gapSize > rr.getMateAlignmentStart() )
      )
    }}
    
  }
  
  
  /*
                            internalUtils.stdUtils.wrapIteratorWithAdvancedProgressReporter[SVcfInputVariantLine](
                           //rawVariantLines.map(line => SVcfInputVariantLine(line,header)),
                           bufLines.map(line => SVcfInputVariantLine(line).headerInput(header)), 
                           internalUtils.stdUtils.AdvancedIteratorProgressReporter_ThreeLevelAuto[SVcfInputVariantLine](
                                elementTitle = "lines", lineSec = 60,
                                reportFunction  = ((vc : SVcfInputVariantLine, i : Int) => " " + vc.chrom +" "+ internalUtils.stdUtils.MemoryUtil.memInfo )
                           )
                         )
   * 
   */
  
  
  
  
  //def extractData
  
  
  
}