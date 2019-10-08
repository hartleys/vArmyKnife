package internalTests

import internalUtils.Reporter._;
import internalUtils.stdUtils._;
import internalUtils.fileUtils._;
import internalUtils.commandLineUI._;
import internalUtils.commonSeqUtils._;
import internalUtils.optionHolder._;

import htsjdk.samtools._;
import internalUtils.fileUtils._;
import java.io.File._;
import scala.util.Random._;

import internalUtils.genomicAnnoUtils.GenomicArrayOfSets
import scala.collection.JavaConverters._

object SplitExomeIntoParts {
   
 
class CmdSplitExomeIntoParts extends CommandLineRunUtil {
     override def priority = 20;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "splitExomeIntoParts", 
          quickSynopsis = "", 
          synopsis = "", 
          description = ""+ALPHA_WARNING,
          argList = 

                    new BinaryOptionArgument[List[String]](
                                         name = "chromList", 
                                         arg = List("--chromList"), 
                                         valueName = "chr1,chr2,...",  
                                         argDesc =  "List of chromosomes. If supplied, then all analysis will be restricted to these chromosomes. All other chromosomes will be ignored. "+
                                                    "For a VCF that contains only one chromosome this option will improve runtime, since the utility will not have to load and process "+
                                                    "annotation data for the other chromosomes."
                                        ).meta(false,"Preprocessing") ::
                                        
                    /*new UnaryArgument( name = "infileList",
                                         arg = List("--infileList"), // name of value
                                         argDesc = "If this option is set, then the input file is a text file containing a list of input VCF files (one per line), rather than a simple path to a single VCF file. "+
                                                   "Multiple VCF files will be concatenated and used as input. Note that only the first file's headers will be used, "+
                                                   "and if any of the subsequent files have tags or fields that are not present in the first VCF file then errors may occur. "+
                                                   "Also note that if the VCF file includes sample genotypes then the samples MUST be in the same order."+
                                                   ""+
                                                   "" // description
                                       ).meta(false,"Input Parameters",0) ::*/
                                        
                    new BinaryMonoToListArgument[String](
                                         name = "nonSplitIntervalSet", 
                                         arg = List("--nonSplitIntervalSet"),
                                         valueName = "ivfile.bed|ivname[|buffersize]",
                                         argDesc =  ""
                                        ).meta(false,"Annotation") :: 
                                        
                    new BinaryMonoToListArgument[String](
                                         name = "nonSplitOverHighCoverageRegions", 
                                         arg = List("--nonSplitOverHighCoverageRegions"),
                                         valueName = "coverage.wig|ivname|threshold|buffersize",
                                         argDesc =  ""
                                        ).meta(false,"Annotation") :: 
                    new BinaryMonoToListArgument[String](
                                         name = "nonSplitGeneIntervals", 
                                         arg = List("--nonSplitGeneIntervals"),
                                         valueName = "geneAnno.gtf or .bed|geneSetName[|buffersize]",
                                         argDesc =  ""
                                        ).meta(false,"Annotation") :: 
                                        
                    new BinaryMonoToListArgument[Int](
                                         name = "subSplitByFactor", 
                                         arg = List("--subSplitByFactor"),
                                         valueName = "x",
                                         argDesc =  ""
                                        ).meta(false,"Annotation") :: 
                                        
                    new BinaryOptionArgument[String](
                                         name = "targetRegionBed", 
                                         arg = List("--targetRegionBed"), 
                                         valueName = "targetRegionBed.bed",  
                                         argDesc =  "bed file of target regions. This tool will attempt to break up the genome such that each slice will have an equal number of bp covered by this target region."
                                        ).meta(false,"Preprocessing") ::
                                        
                                        
                    new FinalArgument[String](
                                         name = "chromLengthFile",
                                         valueName = "chromLengthFile.txt",
                                         argDesc = "The chrom lengths in a tab delimited file."// description
                                        ) ::
                    new FinalArgument[Int](
                                         name = "partct",
                                         valueName = "partct",
                                         argDesc = "The "// description
                                        ) ::
                    new FinalArgument[String](
                                         name = "outputDir",
                                         valueName = "outputDir",
                                         argDesc = "The output directory."// description
                                        ) ::
                    internalUtils.commandLineUI.CLUI_UNIVERSAL_ARGS );

     def run(args : Array[String]) {
       val out = parser.parseArguments(args.toList.tail);
       if(out){
         
         SplitExomeIntoParts.runSplitExomeIntoParts(
              outputDir = parser.get[String]("outputDir"),
              partct = parser.get[Int]("partct"),
              chromList = parser.get[Option[List[String]]]("chromList"),
              chromLengthFile = parser.get[String]("chromLengthFile"),
              nonSplitIntervalSet = parser.get[List[String]]("nonSplitIntervalSet"),
              nonSplitOverHighCoverageRegions = parser.get[List[String]]("nonSplitOverHighCoverageRegions"),
              nonSplitGeneIntervals = parser.get[List[String]]("nonSplitGeneIntervals"),
              targetRegionBed = parser.get[Option[String]]("targetRegionBed"),
              subSplitByFactor = parser.get[List[Int]]("subSplitByFactor")
             )
         
         /*if(parser.get[Int]("bufferDist") == 0){
         genHomopolymerBedNoOverlap(
             infile = parser.get[String]("infile"),
             outfile = parser.get[String]("outfile"),
             chromListFile = parser.get[String]("chromListFile"),
             homopolyLen = parser.get[Int]("homopolyLen")
         ).run();
         } else {
         genHomopolymerBed(
             infile = parser.get[String]("infile"),
             outfile = parser.get[String]("outfile"),
             chromListFile = parser.get[String]("chromListFile"),
             homopolyLen = parser.get[Int]("homopolyLen"),
             bufferDist = parser.get[Int]("bufferDist")
         ).run();
         }*/
       }
     }
  }

def runSplitExomeIntoParts(outputDir : String, partct : Int, chromList : Option[List[String]], chromLengthFile : String, 
                           nonSplitIntervalSet : List[String],
                           nonSplitOverHighCoverageRegions : List[String],
                           nonSplitGeneIntervals : List[String],
                           targetRegionBed : Option[String],
                           subSplitByFactor : List[Int]){
  (new java.io.File( outputDir )).mkdir();
  
  reportln("Initializing SplitExomeIntoParts ["+getDateAndTimeString+"]","note");
  val chromLengthList : Vector[(String,Int)] = getLinesSmartUnzip(chromLengthFile).map{ line => {
    val cells = line.split("\t");
    (cells(0), string2int(cells(1)))
  }}.toVector;
  val chromLengthMap : Map[String,Int] = chromLengthList.toMap;
  reportln("ChromLengthMap read finished. Found: "+chromLengthMap.size+"chroms examples: ["+chromLengthMap.take(3).map{_._1}.mkString(",")+"] ["+getDateAndTimeString+"]","note");

  
  val targetArray : GenomicArrayOfSets[String] = GenomicArrayOfSets[String](false);
  val stdGtfCodes = new internalUtils.GtfTool.GtfCodes();

  reportln("Reading target region ["+getDateAndTimeString+"]","note");
  targetRegionBed match { 
    case Some(trb) => {
      reportln("     Reading target region file: "+trb+" ["+getDateAndTimeString+"]","note");
      add_bedfile(targetArray, bedfile=trb, bedTitle = "TGT",chromLengthMap,0);
    }
    case None => {
      reportln("     No target region, using whole genome ["+getDateAndTimeString+"]","note");
      chromLengthMap.foreach{ case (chromName,chromLen) => {
        val iv = new GenomicInterval(chromName,'.',0,chromLen)
        targetArray.addSpan(iv, "genome");
      }}
    }
  }
  targetArray.finalizeStepVectors;
  reportln("Target Region Finalized ["+getDateAndTimeString+"]","note");
  
  val arr : GenomicArrayOfSets[String] = GenomicArrayOfSets[String](false);
  nonSplitIntervalSet.foreach{ ivset => {
    val cells = ivset.split("[|]");
    val bedfile = cells(0);
    val bedtitle = cells(1);
    val bedbuffer = string2int(cells.lift(2).getOrElse("0"));
    reportln("Parsing nonSplitBed: "+bedtitle+":"+bedfile+" ["+getDateAndTimeString+"]","note");
    add_bedfile(arr,bedfile,bedtitle,chromLengthMap,bedbuffer);
  }}
  
  

  nonSplitOverHighCoverageRegions.map{ wigfile => {
    
    val cells = wigfile.split("[|]");
    val bedfile = cells(0);
    val bedtitle = cells(1);
    val t = string2double( cells(2) );
    val bedbuffer = string2int(cells.lift(3).getOrElse("0"));
    reportln("Parsing coverage wiggle file: "+bedtitle+":"+bedfile+" ["+getDateAndTimeString+"]","note");
    (bedtitle,t,bedbuffer, addwig(arr,bedfile,bedtitle,t,bedbuffer))
  }}.foreach{ case (bedtitle,t,bedbuffer,warr) => {
    val ww = openWriterSmart(outputDir+"/wig."+bedtitle+".covGT"+t+".buffer"+bedbuffer+".bed.gz");
    chromLengthList.map{ case (chrom,clen) => {
      if(warr.hasChrom(chrom)){
        warr.getSteps(chrom,'.').filter{ case (iv,ss) => ss.size > 0 }.foreach{ case (iv,ss) => {
          ww.write(chrom+"\t"+iv.start+"\t"+iv.end+"\n");
        }}
      } //else do nothing
    }}
    ww.close();
  }}
  
  nonSplitGeneIntervals.map{ ivset => {
    val cells = ivset.split("[|]");
    val bedfile = cells(0);
    val bedtitle = cells.lift(1).getOrElse("GENE");
    val bedbuffer = string2int(cells.lift(2).getOrElse("0"));
    reportln("Parsing gene GTF file: "+bedtitle+":"+bedfile+" ["+getDateAndTimeString+"]","note");
    (bedtitle,bedbuffer,add_qcGetGeneCounts_geneArea_regions(arr,bedfile,bedtitle,codes=stdGtfCodes,buffer = bedbuffer, chromLens=chromLengthMap));
  }}.foreach{ case (bedtitle,bedbuffer,warr) => {
    val ww = openWriterSmart(outputDir+"/gtfGeneSpans."+bedtitle+".buffer"+bedbuffer+".bed.gz");
    chromLengthList.map{ case (chrom,clen) => {
      if(warr.hasChrom(chrom)){
        warr.getSteps(chrom,'.').filter{ case (iv,ss) => ss.size > 0 }.foreach{ case (iv,ss) => {
          ww.write(chrom+"\t"+iv.start+"\t"+iv.end+"\t"+ss.mkString(",")+"\n");
        }}
      } //else do nothing
    }}
    ww.close();
  }}



  chromLengthMap.foreach{ case (chrom,clen) => {
    if(!  arr.hasChrom(chrom) ){
      arr.addChrom(chrom);
    }
  }}
  
  arr.finalizeStepVectors;
  
  /////////////////////////////////////////////////////////////////////////////////
  
  reportln("Starting breakpoint calcs ["+getDateAndTimeString+"]","note");

  val breakablePoints = chromLengthList.map{ case (chrom,clen) => {
    (chrom, arr.getSteps(chrom,'.').filter{ case (iv,ivset) => {
      ivset.size == 0 && iv.start + 5 < iv.end && iv.end < chromLengthMap(chrom)
    }}.map{ case (iv,ivset) => ((iv.start + iv.end) / 2) }.toVector)
  }}
  val chroms = breakablePoints.map{ _._1 }
  
  reportln("Generating Spans ["+getDateAndTimeString+"]","note");

  val unbreakableSpans = breakablePoints.map{ case (chrom,bp) => {
    reportln("   breakPointsCt("+chrom+")="+bp.length,"note");
    if( bp.length > 1 ){
      (chrom, bp, (0 +: bp).zip(bp :+ chromLengthMap(chrom)) );
    } else {
      (chrom,bp,Vector((0,chromLengthMap(chrom))))
    }
  }}
  
  reportln("Annotating Spans ["+getDateAndTimeString+"]","note");

  val unbreakableSpanInfo = unbreakableSpans.map{ case (chrom,bp,ubs) => {
    (chrom, ubs.map{ case (s,e) => {
      (s,e,targetArray.findIntersectingSteps( GenomicInterval(chrom,'.',s,e)).filter{ case (iv,ivset) => ivset.size > 0 }.map{ case (iv,ivset) => iv.end - iv.start}.sum)
    }})
    //targetArray.findIntersectingSteps( GenomicInterval(chrom,'.',ubs)) 
  }}
  
  /////////////////////////////////////////////////////////////////////////////////
  
  reportln("Writing breakpoint summary ["+getDateAndTimeString+"]","note");
  val breakout = openWriter(outputDir+"/possible.breakpoints.txt");
  breakout.write("chrom\tpos\n");
  breakablePoints.foreach{ case (chrom,bp) => {
    bp.foreach{ b => {
      breakout.write(chrom+"\t"+b+"\n");
    }}
  }}
  breakout.close();
  
  reportln("Writing genomeArray summary ["+getDateAndTimeString+"]","note");

  val gstatout = openWriter(outputDir+"/genomeArray.status.txt");
  gstatout.write("chrom\tstart\tend\tstatus\n");
  chromLengthList.map{ case (chrom,clen) => {
    arr.getSteps(chrom,'.').foreach{ case (iv,ivset) => {
      gstatout.write(chrom+"\t"+iv.start+"\t"+iv.end+"\t"+ivset.toVector.sorted.mkString("/")+"\n");
    }}
  }}
  gstatout.close();
  
  reportln("Writing span summary ["+getDateAndTimeString+"]","note");
  
  val spanSummaryOut = openWriter(outputDir+"/genomic.span.info.txt");
  spanSummaryOut.write("chrom\tstart\tend\tlen\texomeLen\n");
  unbreakableSpanInfo.foreach{ case (chrom,dd) => {
    dd.foreach{ case (s,e,ixbp) => {
      spanSummaryOut.write(chrom+"\t"+s+"\t"+e+"\t"+(e-s)+"\t"+ixbp+"\n");
    }}
  }}
  spanSummaryOut.close();
  
  /////////////////////////////////////////////////////////////////////
  
  reportln("Summarizing Chroms ["+getDateAndTimeString+"]","note");

  val chromTotals = unbreakableSpanInfo.map{ case (chrom,ubsi) => {
    val out = ubsi.map{ case (s,e,bp) => bp.toDouble}.sum
    reportln("    "+chrom+":"+out+" ["+getDateAndTimeString+"]","note");
    (chrom,out);
  }}
  val sumTotal = chromTotals.map{ case (c,t) => t}.sum
  
  val partCountPerChromDoubles = chromTotals.map{ case (c,t) => {
      (partct.toDouble * t / sumTotal)
  }}
  val partCountPerChromInitial = partCountPerChromDoubles .map{ bc => {
    math.max(1, math.floor(bc).toInt)
  }}
  partCountPerChromInitial.zip(chroms).foreach{ case (pc,chrom) => {
        reportln("   Initial Part Ct: "+chrom+"="+pc,"note");
  }}
  val partCountPerChromRemainder = partCountPerChromDoubles.zip(partCountPerChromInitial).zip(chroms).zipWithIndex.map{ case (((dbl,flr),chrom),i) => {
    ((dbl - flr.toDouble),chrom,i,dbl,flr)
  }}.sorted.reverse
  
  val totalPartCountInitial = partCountPerChromInitial.sum;
  reportln("Total Initial Part Count: "+totalPartCountInitial,"note");
  if( totalPartCountInitial > partct){
    error("Error: impossible state? Initial breakcount is greater than target breakcount??? totalParts="+totalPartCountInitial+", target partct="+partct);
  }
  val partsLeft = partct - totalPartCountInitial;
  if(partsLeft > chromTotals.size){
    error("Error: impossible state? Initial breakcount requires the addition of more than chromCt parts??? totalParts="+totalPartCountInitial+", target partct="+partct+", chromCt="+chromTotals.size);
  }
  reportln("   Parts Left: "+partsLeft,"note");
  
  val partCountPerChrom = partCountPerChromRemainder.zipWithIndex.map{ case ((rem,chrom,i, dbl, flr),j) => {
    val remString = "%.4f".format(rem);
    val dblString = "%.4f".format(dbl);
    val flrString = ""+flr
    if( j < partsLeft){
      reportln("chrom["+chrom+"]["+dblString+" - "+flrString+" = "+remString+"] [ADD 1]","note")
      (i,chrom,partCountPerChromInitial(i) + 1,""+partCountPerChromInitial(i)+"+1");
    } else {
      reportln("chrom["+chrom+"]["+dblString+" - "+flrString+" = "+remString+"] [ADD 0]","note")
      (i,chrom,partCountPerChromInitial(i),""+partCountPerChromInitial(i));
    }
  }}.sorted;
  
  partCountPerChrom.foreach{ case (i,chrom,pc,addString) => {
        reportln("   Final Part Ct: "+chrom+"="+pc +" ("+addString+")","note");
  }}
  val finalPartCountAllChroms = partCountPerChrom.map{_._3}.sum;
  reportln("Final Part Count: "+finalPartCountAllChroms,"note");
  
  ////////////////////////////////////////////////////////////////////////////
  
  val finalBreakpoints = partCountPerChrom.map{ case (i,chrom,pc,addString) => {
    val chromTotalSpan = unbreakableSpanInfo(i)._2.map{ case (s,e,ct) => ct }.sum
    if(pc == 1){
      (i,chrom,Vector((0,chromLengthMap(chrom),chromTotalSpan)))
    } else {
    
      val partSpan = chromTotalSpan / pc;
      // output: (runningBuffer, vector(start,end,ct))
      val finalIVs =  unbreakableSpanInfo(i)._2.foldLeft( (0,Vector[(Int,Int,Int)]()) ){ case ((runTotal,breaksSoFar),(s,e,ct)) => {
        if(runTotal + ct >= partSpan){
          val ivStart = breaksSoFar.lastOption.map{ case (ivS,ivE,ivCt) => { ivE }}.getOrElse(0);
          (0, breaksSoFar :+ (ivStart,e,runTotal + ct))
        } else {
          (runTotal + ct, breaksSoFar);
        }
      }}._2
      val lastIVstart = finalIVs.last._2
      val lastIVend   = chromLengthMap(chrom)
      val lastIVct    = unbreakableSpanInfo(i)._2.filter{ case (s,e,ct) => s >= lastIVstart }.map{ case (s,e,ct) => ct}.sum
      (i,chrom, finalIVs :+ (lastIVstart,lastIVend,lastIVct));
    }
  }}
  
  ///////////////////////////////////////////////////////
  val numZeros = getNumZeros(partCountPerChrom.map{ case (i,chrom,pc,addString) => pc }.max)

  reportln("Writing all-breakpoint summary ["+getDateAndTimeString+"]","note");
  
  val finalBreakOut = openWriter(outputDir+"/chunkInfo.txt");
  finalBreakOut.write("chrom\tstart\tend\tchunkID\tlen\texomeLen\trelativeLen\n");
  finalBreakpoints.foreach{ case (i,chrom,fbp) => {
    fbp.zipWithIndex.foreach{ case ((s,e,ct),segmentIdx) => {
      val spanTitle = chrom+"."+("%0"+numZeros+"d").format(segmentIdx) 
      finalBreakOut.write(chrom+"\t"+s+"\t"+e+"\t"+spanTitle+"\t"+(e-s)+"\t"+ct+"\t"+ "%.4f".format(ct.toDouble * partct / sumTotal)+"\n");
    }}
  }}
  finalBreakOut.close();
  
  
  reportln("Writing all-target summary ["+getDateAndTimeString+"]","note");

  val finaltgt = openWriter(outputDir+"/breakpoint.targetSpans.all.txt");
  finaltgt.write("chrom\tstart\tend\ttitle\tgSpan\teSpan\n");
  finalBreakpoints.foreach{ case (i,chrom,fbp) => {
    fbp.zipWithIndex.foreach{ case ((s,e,ct), segmentIdx) => {
      val spanTitle = chrom+"."+("%0"+numZeros+"d").format(segmentIdx) 
      unbreakableSpanInfo(i)._2.zipWithIndex.filter{ case ((tgtS,tgtE,tgtCT),ivIdx) => {
        s <= tgtS && e >= tgtE
      }}.foreach{ case ((tgtS,tgtE,tgtCT),ivIdx) => {
        finaltgt.write(chrom+"\t"+tgtS+"\t"+tgtE+"\t"+spanTitle+"\t"+(tgtE-tgtS)+"\t"+tgtCT+"\t"+ivIdx+"\n");
      }}
      //finaltgt.write(chrom+"\t"+s+"\t"+e+"\t"+(e-s)+"\t"+ct+"\n");
    }}
  }}
  finaltgt.close();
  
  reportln("Writing tgtSpan files ["+getDateAndTimeString+"]","note");

  (new java.io.File( outputDir + "/tgtSpans" )).mkdir();
  finalBreakpoints.foreach{ case (i,chrom,fbp) => {
    fbp.zipWithIndex.foreach{ case ((s,e,ct), segmentIdx) => {
      val spanTitle = chrom+"."+("%0"+numZeros+"d").format(segmentIdx) 
      val spangt = openWriter(outputDir+"/tgtSpans/spans."+spanTitle+".bed");
      targetArray.findWhollyContainedSteps(new GenomicInterval( chrom,'.',s,e )).filter{ case (iv,stepset) => {
        stepset.size > 0
      }}.foreach{ case (iv,stepset) => {
        spangt.write(chrom+"\t"+iv.start+"\t"+iv.end+"\n");
      }}
      spangt.close();
    }}
  }}
  
  reportln("Writing chrom.chunks files ["+getDateAndTimeString+"]","note");

  (new java.io.File( outputDir + "/chrom.chunks/" )).mkdir();
  finalBreakpoints.foreach{ case (i,chrom,fbp) => {
    val spangt = openWriter(outputDir+"/chrom.chunks/chunkList."+chrom+".txt");
    val spangt2 = openWriter(outputDir+"/chrom.chunks/chunkPairs."+chrom+".txt");
    val spangt3 = openWriter(outputDir+"/chrom.chunks/chunkSpans."+chrom+".bed");
    fbp.zipWithIndex.foreach{ case ((s,e,ct), segmentIdx) => {
      val spanTitle = chrom+"."+("%0"+numZeros+"d").format(segmentIdx) 
      spangt.write(spanTitle+"\n");
      spangt2.write(chrom+"\t"+("%0"+numZeros+"d").format(segmentIdx) +"\n");
      spangt3.write(chrom+"\t"+s+"\t"+e+"\t"+spanTitle+"\n");
    }}
    spangt.close();
    spangt2.close();
    spangt3.close();
  }}
  
  reportln("Writing chunklist files ["+getDateAndTimeString+"]","note");

  val chunkListOut = openWriter(outputDir+"/chunkList.txt");
  val chunkPairOut = openWriter(outputDir+"/chunkPairList.txt");
  finalBreakpoints.foreach{ case (i,chrom,fbp) => {
    fbp.zipWithIndex.foreach{ case ((s,e,ct), segmentIdx) => {
      val spanXX = ("%0"+numZeros+"d").format(segmentIdx) 
      val spanTitle = chrom+"."+spanXX
      chunkListOut.write(spanTitle+"\n");
      chunkPairOut.write(chrom+"\t"+spanXX+"\n");
    }}
  }}
  chunkListOut.close();
  chunkPairOut.close();
  
  subSplitByFactor.foreach{ breakFactor => {
    writeSubSplit(breakFactor = breakFactor,
                  fullFinalBreakpoints = finalBreakpoints,
                  unbreakableSpanInfo = unbreakableSpanInfo,
                  numZeros = numZeros,
                  outputDir = outputDir+"/merged."+breakFactor+"/",
                  sumTotal = sumTotal,
                  targetArray = targetArray
        );
  }}
  
  
}
  
/*
************************************************************************************************************************
* ************************************************************************************************************************
* ************************************************************************************************************************
* ************************************************************************************************************************
* ************************************************************************************************************************
* ************************************************************************************************************************
* ************************************************************************************************************************
*/

  def writeSubSplit(breakFactor : Int, 
                  fullFinalBreakpoints : Vector[(Int,String,Vector[(Int,Int,Int)])], 
                  unbreakableSpanInfo  : Vector[(String,Vector[(Int,Int,Int)])],
                  numZeros : Int, outputDir : String,
                  sumTotal : Double,
                  targetArray : GenomicArrayOfSets[String]){
    
  //var addExtra
  (new java.io.File( outputDir + "/" )).mkdir();

    val finalBreakpoints = fullFinalBreakpoints.map{ case (i,chrom,allbp) => {
      val (chunkCt,soFarTotal,chunkStart,ct,chunkList,prevChunkEnd) = allbp.zipWithIndex.foldLeft( (1, Vector[(Int,Int,Int,Vector[String])](), 0,0, Vector[String](),0) ){ case ((chunkCt, soFar, chunkStart, ct, chunkList,prevChunkEnd),((currStart,currEnd,currCt),segmentIdx)) => {
        val chunkTitle = ("%0"+numZeros+"d").format(segmentIdx) 
        if(chunkCt == breakFactor){
          (1,soFar :+ (chunkStart,currEnd,ct + currCt,chunkList :+ chunkTitle),currEnd,0,Vector[String](),0);
        } else {
          (chunkCt + 1, soFar, chunkStart, ct + currCt, chunkList :+ chunkTitle, currEnd)
        }
      }}
      val outfoldres = (if(prevChunkEnd == 0){
        soFarTotal
      } else {
        soFarTotal :+ (chunkStart,prevChunkEnd,ct,chunkList)
      })
      (i,chrom,outfoldres)
    }}
  val partct = finalBreakpoints.map{ _._3.length }.sum
    
  reportln("Writing all-breakpoint summary ["+getDateAndTimeString+"]","note");
  
  val finalBreakOut = openWriter(outputDir+"/chunkInfo.txt");
  finalBreakOut.write("chrom\tstart\tend\tchunkID\tlen\texomeLen\trelativeLen\tfullChunkList\n");
  finalBreakpoints.foreach{ case (i,chrom,fbp) => {
    fbp.zipWithIndex.foreach{ case ((s,e,ct,chunkList),segmentIdx) => {
      val rawSpanTitle = ("%0"+numZeros+"d").format(segmentIdx) 
      val spanTitle = chrom+".M"+breakFactor+"."+rawSpanTitle+"."+chunkList.head+"."+chunkList.last;
      finalBreakOut.write(chrom+"\t"+s+"\t"+e+"\t"++"\t"+(e-s)+"\t"+ct+"\t"+ "%.4f".format(ct.toDouble * partct / sumTotal)+"\t"+chunkList.map{c => chrom+"."+c}.mkString(",")+"\n");
    }}
  }}
  finalBreakOut.close();
  
  //val numZeros = getNumZeros(partCountPerChrom.map{ case (i,chrom,pc,addString) => pc }.max)
  
  reportln("Writing all-target summary ["+getDateAndTimeString+"]","note");

  val finaltgt = openWriter(outputDir+"/breakpoint.targetSpans.all.txt");
  finaltgt.write("chrom\tstart\tend\ttitle\tgSpan\teSpan\n");
  finalBreakpoints.foreach{ case (i,chrom,fbp) => {
    fbp.zipWithIndex.foreach{ case ((s,e,ct,chunkList), segmentIdx) => {
      val rawSpanTitle = ("%0"+numZeros+"d").format(segmentIdx) 
      val spanTitle = chrom+".M"+breakFactor+"."+rawSpanTitle+"."+chunkList.head+"."+chunkList.last;
      unbreakableSpanInfo(i)._2.zipWithIndex.filter{ case ((tgtS,tgtE,tgtCT),ivIdx) => {
        s <= tgtS && e >= tgtE
      }}.foreach{ case ((tgtS,tgtE,tgtCT),ivIdx) => {
        finaltgt.write(chrom+"\t"+tgtS+"\t"+tgtE+"\t"+spanTitle+"\t"+(tgtE-tgtS)+"\t"+tgtCT+"\t"+ivIdx+"\n");
      }}
      //finaltgt.write(chrom+"\t"+s+"\t"+e+"\t"+(e-s)+"\t"+ct+"\n");
    }}
  }}
  finaltgt.close();
  
  reportln("Writing tgtSpan files ["+getDateAndTimeString+"]","note");

  (new java.io.File( outputDir + "/tgtSpans" )).mkdir();
  finalBreakpoints.foreach{ case (i,chrom,fbp) => {
    fbp.zipWithIndex.foreach{ case ((s,e,ct,chunkList), segmentIdx) => {
      //val spanTitle = chrom+"."+("%0"+numZeros+"d").format(segmentIdx) 
      val rawSpanTitle = ("%0"+numZeros+"d").format(segmentIdx) 
      val spanTitle = chrom+".M"+breakFactor+"."+rawSpanTitle+"."+chunkList.head+"."+chunkList.last;
      val spangt = openWriter(outputDir+"/tgtSpans/spans."+spanTitle+".bed");
      unbreakableSpanInfo(i)._2.zipWithIndex.filter{ case ((tgtS,tgtE,tgtCT),ivIdx) => {
        s <= tgtS && e >= tgtE
      }}.foreach{ case ((tgtS,tgtE,tgtCT),ivIdx) => {
        spangt.write(chrom+"\t"+tgtS+"\t"+tgtE+"\n");
      }}
      spangt.close();
    }}
  }}
  
  reportln("Writing chrom.chunks files ["+getDateAndTimeString+"]","note");

  (new java.io.File( outputDir + "/chrom.chunks/" )).mkdir();
  finalBreakpoints.foreach{ case (i,chrom,fbp) => {
    val spangt = openWriter(outputDir+"/chrom.chunks/chunkList."+chrom+".txt");
    val spangt2 = openWriter(outputDir+"/chrom.chunks/chunkPairs."+chrom+".txt");
    val spangt3 = openWriter(outputDir+"/chrom.chunks/chunkSpans."+chrom+".bed");
    fbp.zipWithIndex.foreach{ case ((s,e,ct,chunkList), segmentIdx) => {
      //val spanTitle = chrom+"."+("%0"+numZeros+"d").format(segmentIdx)
      val rawSpanTitle = ("%0"+numZeros+"d").format(segmentIdx) 
      val spanTitle = "M"+breakFactor+"."+rawSpanTitle+"."+chunkList.head+"."+chunkList.last;
      spangt.write(spanTitle+"\n");
      spangt2.write(chrom+"\t"+spanTitle+"\n");
      spangt3.write(chrom+"\t"+s+"\t"+e+"\t"+spanTitle+"\n");
    }}
    spangt.close();
    spangt2.close();
    spangt3.close();
  }}
  
  reportln("Writing chunklist files ["+getDateAndTimeString+"]","note");

  val chunkListOut = openWriter(outputDir+"/chunkList.txt");
  val chunkPairOut = openWriter(outputDir+"/chunkPairList.txt");
  finalBreakpoints.foreach{ case (i,chrom,fbp) => {
    fbp.zipWithIndex.foreach{ case ((s,e,ct,chunkList), segmentIdx) => {
      //val spanXX = ("%0"+numZeros+"d").format(segmentIdx) 
      //val spanTitle = chrom+"."+spanXX
      val rawSpanTitle = ("%0"+numZeros+"d").format(segmentIdx)
      val spanTitle = "M"+breakFactor+"."+rawSpanTitle+"."+chunkList.head+"."+chunkList.last;
      chunkListOut.write(chrom+"."+spanTitle+"\n");
      chunkPairOut.write(chrom+"\t"+spanTitle+"\n");
    }}
  }}
  chunkListOut.close();
  chunkPairOut.close();

  }

/////////////////////////////////////////////////////////////////////////////////////////////


  def addwig(arr :  GenomicArrayOfSets[String], bedfile : String,bedTitle : String, t : Double, buffer : Int = 0) : GenomicArrayOfSets[String] = {
    val iter = getLinesSmartUnzip(bedfile).buffered;
    val warr : GenomicArrayOfSets[String] = GenomicArrayOfSets[String](false);
    def getChrom(chromLine : String) : String = {
      reportln("chromLine = "+chromLine,"debug");
      chromLine.split("\\s+").map{ cc => {
        val ccArray = cc.split("[=]");
        reportln("chromArray: [\""+ccArray.mkString("\",\"")+"]","debug")
        (cc.split("[=]")(0),cc.split("[=]").lift(1))
      }}.find{ cc => cc._1 == "chrom"}.map{ _._2.getOrElse({
        error("malformatted wiggle file line!");
        "chr?"
      }) }.getOrElse({
        error("malformatted wiggle file line!");
        "chr?"
      });
    }
    var line = iter.next;
    while(! line.startsWith("fixedStep")){
      line = iter.next;
    }
    var currChrom = getChrom(line);
    
    val reportCT = 1000000;
    
    var lnct = 0;
    var pos = 0;
    var blockct = 0;
    def readNext( note : String = "."){
      lnct = lnct + 1;
      pos = pos + 1;
      if(lnct % reportCT == 0){
        report(".","progress");
        if(lnct % (reportCT * 5) == 0){
          report(" ","progress");
          if(lnct % (reportCT * 20) == 0){
            report(" read "+"%,d".format(lnct)+" lines. ["+currChrom+":"+pos+"] ["+blockct+" blocks] ["+note+"] ["+getDateAndTimeString+"]\n","progress");
          }
        }
      }
      line = iter.next;
    }

    while(iter.hasNext){
      while( iter.hasNext && (! line.startsWith("fixedStep"))){
        while(iter.hasNext && (! line.startsWith("fixedStep")) && string2double(line) < t){
          readNext("LowCoverageSection");
        }
        if(iter.hasNext && (! line.startsWith("fixedStep")) && string2double(line) >= t){
          warning("Entering High Coverage section! "+currChrom+":"+pos,"START_HICOV_SECTION",10);
          blockct = blockct + 1;
          var start = pos;
          while(iter.hasNext && (! line.startsWith("fixedStep")) && string2double(line) >= t){
            readNext("HighCoverageSection");
          }
          warning("Leaving High Coverage section! "+currChrom+":"+pos,"END_HICOV_SECTION",10);

          val s = math.max( start - buffer,0)
          val iv = new GenomicInterval(currChrom,'.',s,pos);
          arr.addSpan(iv,bedTitle);
          warr.addSpan(iv,bedTitle);
        }
      }
      if(iter.hasNext){
        currChrom = getChrom(line);
        readNext("newChrom");
        pos = 0;
      }
    }
   
    report(" read "+lnct+" lines. ["+currChrom+":"+pos+"] ["+getDateAndTimeString+"]\n","progress");
    return warr.finalizeStepVectors;
  }

  def add_bedfile( arr : GenomicArrayOfSets[String], bedfile : String,bedTitle : String, chromLens : Map[String,Int], buffer : Int = 0){
    
    for(line <- getLinesSmartUnzip(bedfile)){
      val cells = line.split("\t");
      val chrom = cells(0);
      if( chromLens.contains(chrom)){      
         val start = math.max( string2int(cells(1)) - buffer,0)
         val end = math.min( chromLens(cells(0)), buffer + string2int(cells(2)));
         val iv = new GenomicInterval(cells(0),'.',start,end)
         arr.addSpan(iv, bedTitle);
      } else {
        warning("Chrom Not Found: " + chrom+" in bed file: "+bedTitle,"CHROM_NOT_FOUND_IN_BED_"+bedTitle,10);
      }
    }
    
  }
  def extractGeneId(gtfLine : internalUtils.GtfTool.GtfLine, codes : internalUtils.GtfTool.GtfCodes) : String = {
    return gtfLine.getAttributeOrDie(codes.GENE_ID_ATTRIBUTE_KEY);
  }
  
  
  def add_qcGetGeneCounts_geneArea_regions( arr : GenomicArrayOfSets[String], gtffile : String,gtftitle : String, 
            codes : internalUtils.GtfTool.GtfCodes, 
            buffer : Int = 0, chromLens : Map[String,Int]) : GenomicArrayOfSets[String]= {
    val stranded = false;
   // return buildGenomicArrayOfSets_fromGtf(stranded, gtffile, (gtfLine : GtfLine) => gtfLine.featureType == codes.STD_CDS_TYPE_CODE, (gtfLine : GtfLine) => extractGeneId(gtfLine, codes));
    val gtfReader = internalUtils.GtfTool.GtfReader.getGtfReader(gtffile, stranded, true, "\\s+");
    val warr : GenomicArrayOfSets[String] = GenomicArrayOfSets[String](false);
    reportln("      (Loading gene regions)","debug");
    
    val spanArray = scala.collection.mutable.AnyRefMap[String,GenomicInterval]();
    var lnct = 0;
    for(gtfLine <- gtfReader){
      lnct = lnct + 1;
      if(lnct % 25000 == 0){
        report(".","progress");
        if(lnct % 125000 == 0){
          report(" ","progress");
          if(lnct % 500000 == 0){
            report(" parsed "+lnct+" lines. ["+getDateAndTimeString+"]\n","progress");
          }
        }
      }
      if(gtfLine.featureType == codes.STD_EXON_TYPE_CODE || gtfLine.featureType == codes.STD_CDS_TYPE_CODE){
        val geneID = extractGeneId(gtfLine, codes);
        val curriv = gtfLine.getGenomicInterval.usingStrandedness(stranded);
        if( chromLens.contains(curriv.chromName)){
          spanArray.get(geneID) match {
            case Some(newiv) => {
              spanArray(geneID) = GenomicInterval(curriv.chromName,curriv.strand, math.min(curriv.start,newiv.start), math.max(curriv.end,newiv.end));
            }
            case None => {
              spanArray(geneID) = GenomicInterval(curriv.chromName,curriv.strand,curriv.start, curriv.end);
            }
          }
        } else {
           warning("Chrom Not Found: " + curriv.chromName+" in gtf file: "+gtftitle,"CHROM_NOT_FOUND_IN_GTF_"+gtftitle,10);
        }
      }
    }
    reportln("      (Generating region array)","debug");
    
    //val geneArray : GenomicArrayOfSets[String] = GenomicArrayOfSets[String](stranded);
    for(geneID <- spanArray.keySet){
      val iv = spanArray(geneID);
      val start = math.max( iv.start - buffer,0)
      val end = math.min( chromLens(iv.chromName), buffer + iv.end);
      val biv = GenomicInterval(iv.chromName,'.',start,end);
      arr.addSpan(biv, gtftitle + "." + geneID);
      warr.addSpan(biv,gtftitle+"."+geneID);
    }
    return warr.finalizeStepVectors;
    
    //reportln("      (Region array complete)","debug");
    //return arr;
  }

}


























