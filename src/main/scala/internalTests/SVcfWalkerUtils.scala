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

object SVcfWalkerUtils {
  
    def scrapFunctionForTesting(){
      val path : java.nio.file.Path = java.nio.file.Paths.get("testbw.bw");
      val rbc : java.nio.channels.SeekableByteChannel = java.nio.file.Files.newByteChannel(path, java.util.EnumSet.of(java.nio.file.StandardOpenOption.READ));
      val bwf : jigwig.BigWigFile = new jigwig.BigWigFile(rbc)
      val iter : jigwig.BigWigFileIterator = bwf.query("chr11|chr12", 112643206, 112658727, 0);
      
      while( iter.hasNext()){
        val r : jigwig.BigWigFileIteratorType = iter.next;
        val s : jigwig.BigWigSummaryRecord = r.getSummary();
        report("","")
      }
      
      //val bwf = BigWigFile.read("test.bw")
      
    }
    val DEFAULT_VCF_CODES = VCFAnnoCodes();
    
    val SNVVARIANT_BASESWAP_LIST = Seq( (("A","C"),("T","G")),
                            (("A","T"),("T","A")),
                            (("A","G"),("T","C")),
                            (("C","A"),("G","T")),
                            (("C","T"),("G","A")),
                            (("C","G"),("G","C"))
                          );

  
  
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
  * 
  */
/*
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
    
    
  /*case class SV_MergeBreakendMetadata( tmpfile : String ) extends SVcfWalker {
    def walkerName : String = "SV_MergeBreakendMetadata";
    def walkerparams : Seq[(String,String)] = Seq[(String,String)](
           ("tmpfile",tmpfile)
        )
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {

      
    }
  }*/
  class dropBackwardsSvLine() extends SVcfWalker {
    def walkerName : String = "dropBackwardsSvLine"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)]();

    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader;
      
      //add tags!
      
      outHeader.addWalk(this);
      var dropct = 0;
      outHeader.reportAddedInfos(this)
      (addIteratorCloseAction( iter = vcFlatMap(vcIter){v => {
        val vc = v.getOutputLine();
        
        if( v.alt.length == 1 && v.info.getOrElse("SVTYPE",None).getOrElse(".") == "BND"){
          v.getSVbnd() match {
            case Some(sv) => {
              val chromDiff = Chrom( v.chrom ).compare( Chrom( sv.bndBreakEnd._1 ))
              val posDiff   = v.pos.compare( sv.bndBreakEnd._2 )
              if( chromDiff == 0 ){
                if( posDiff <= 0 ){
                  Some(vc)
                } else {
                  None
                }
              } else if(chromDiff < 0){
                Some(vc);
              } else {
                None
              }
            }
            case None => {
              warning("WARNING: invalid SV! \n\""+v.getVcfStringNoGenotypes+"\"","dropBackwardSVline.invalidSV",10);
              Some(vc);
            }
          }
        } else {
          Some(vc);
        }
      }}, closeAction = (() => {
        reportln("Dropped "+dropct+" variant/allele lines due to the presence of symbolic alleles","note");
      })),outHeader)
    }
  }
  class addBackwardsSvLine() extends SVcfWalker {
    def walkerName : String = "addBackwardsSvLine"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)]();

    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader;
      
      //add tags!
      
      outHeader.addWalk(this);
      var dropct = 0;
      outHeader.reportAddedInfos(this)
      (addIteratorCloseAction( iter = vcFlatMap(vcIter){v => {
        val vc = v.getOutputLine();
        
        if( v.alt.length == 1 && v.info.getOrElse("SVTYPE",None).getOrElse(".") == "BND"){
          v.getSVbnd() match {
            case Some(sv) => {
              val (chr2,pos2) = sv.bndBreakEnd
              val ss = sv.strands;
              val vc2 = vc.getOutputLineCopy();
              vc2.in_chrom = chr2
              vc2.in_pos   = pos2
              //vc2.in_alt = Seq(  )
              val str2 = sv.strandswap;
              vc2.in_alt = Seq( SVbnd.makeSVbnd( v.chrom,v.pos,v.ref,str2 ).svalt )
              Seq(vc,vc2);
            }
            case None => {
              warning("WARNING: invalid SV! \n\""+v.getVcfStringNoGenotypes+"\"","dropBackwardSVline.invalidSV",10);
              Some(vc);
            }
          }
        } else {
          Some(vc);
        }
      }}, closeAction = (() => {
        reportln("Dropped "+dropct+" variant/allele lines due to the presence of symbolic alleles","note");
      })),outHeader)
    }
  }
  
  class dropInvalidBNDSV() extends SVcfWalker {
    def walkerName : String = "dropInvalidBNDSV"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)]();

    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader;
      
      //add tags!
      
      outHeader.addWalk(this);
      var dropct = 0;
      outHeader.reportAddedInfos(this)
      (addIteratorCloseAction( iter = vcFlatMap(vcIter){v => {
        val vc = v.getOutputLine();
        if( v.alt.length == 1 && v.info.getOrElse("SVTYPE",None).getOrElse(".") == "BND"){
          v.getSVbnd() match {
            case Some(svbnd) => Some(v);
            case None => None
          }
        } else {
          None
        }
      }}, closeAction = (() => {
        reportln("Dropped "+dropct+" variant/allele lines due to the presence of symbolic alleles","note");
      })),outHeader)
    }
  }
    
  class convertSVtoBND() extends SVcfWalker {
    def walkerName : String = "convertSVtoBND"
    
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)]();
    
    def isAltSV(a : String) : Boolean = a.startsWith("<INS") || a.startsWith("<DEL") || a.startsWith("<DUP") || a.startsWith("<INV") || a.startsWith("<CNV");
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader;
      
      //add tags!
      outHeader.addInfoLine(new SVcfCompoundHeaderLine(in_tag="INFO",ID="RAWSVTYPE",Number="1",Type="String",desc="Original source SV type, before INS/DEL/etc were converted into BND type SVs."),Some(this));
      //new SVcfCompoundHeaderLine(in_tag = "FORMAT",ID = t + "_" + fhl.ID, Number = ct, Type = fhl.Type, desc = "
      outHeader.addWalk(this);
      //var dropct = 0;
      outHeader.reportAddedInfos(this)
      (addIteratorCloseAction( iter = vcFlatMap(vcIter){v => {
        val vc = v.getOutputLine();
        v.info.getOrElse("SVTYPE",None).foreach{ bb => {
          vc.addInfo("RAWSVTYPE",bb)
        }}
        if(v.alt.exists{ a => a.startsWith("<")}){
          if(v.alt.length > 1){
            if( v.alt.exists{ a => { isAltSV(a) }} ){
              warning("WARNING: multiallelic symbolic allele found! Cannot deal with multiallelic SVs: \n\""+v.getVcfStringNoGenotypes+"\"","multiAlleSV",10);
            }
            Some(vc)
          } else {
            val a = v.alt.head;
            if( isAltSV(a) ) {
              if( a.startsWith("<DEL") ){
                val end : Int = v.info.getOrElse("END",None).map{ string2intOpt(_) }.getOrElse(None) match {
                  case Some(e) => {
                    e
                  }
                  case None => {
                    v.info.getOrElse("SVLEN",None).map{ string2intOpt(_) }.getOrElse(None) match {
                      case Some(svlen) => {
                        v.pos - svlen
                      }
                      case None => {
                        warning("WARNING: DELETION WITH NO VALID END or SVLEN tags!\n\""+v.getVcfStringNoGenotypes+"\"","delWithNoEnd",10);
                        -1;
                      }
                    }
                  }
                }
                if(end == -1){
                  Some(vc);
                } else {
                  vc.in_alt = Seq( vc.ref + "[" +v.chrom+":"+ end +"[" )
                  vc.addInfo("SVTYPE","BND");
                  val vc2 = vc.getOutputLineCopy();
                  vc2.addInfo("SVTYPE","BND");
                  vc2.in_pos = end;
                  vc2.in_alt = Seq( "]"+v.chrom+":"+v.pos+"]"+v.ref );
                  Seq(vc,vc2);
                }
              } else if( a.startsWith("<INV")){
                val end : Int = v.info.getOrElse("END",None).map{ string2intOpt(_) }.getOrElse(None) match {
                  case Some(e) => {
                    e
                  }
                  case None => {
                    v.info.getOrElse("SVLEN",None).map{ string2intOpt(_) }.getOrElse(None) match {
                      case Some(svlen) => {
                        v.pos + math.abs(svlen)
                      }
                      case None => {
                        warning("WARNING: DELETION WITH NO VALID END or SVLEN tags!\n\""+v.getVcfStringNoGenotypes+"\"","delWithNoEnd",10);
                        -1;
                      }
                    }
                  }
                }
                if(end == -1){
                  Some(vc);
                } else {
                  vc.in_alt = Seq( vc.ref + "]"+ v.chrom+":"+ end +"]" )
                  vc.addInfo("SVTYPE","BND");
                  val vc2 = vc.getOutputLineCopy();
                  vc2.in_pos = end;
                  vc2.in_alt = Seq( v.ref+"]"+v.chrom+":"+v.pos+"]" );
                  val vc3 = vc.getOutputLineCopy();
                  vc3.in_pos = v.pos+1;
                  vc3.in_alt = Seq( "["+v.chrom+":"+(end+1)+"["+v.ref );
                  val vc4 = vc.getOutputLineCopy();
                  vc4.in_pos = end+1;
                  vc4.in_alt = Seq( "["+v.chrom+":"+(v.pos+1)+"["+v.ref );
                  Seq(vc,vc2,vc3,vc4);
                }
              } else if( a.startsWith("<DUP")){
                val end : Int = v.info.getOrElse("END",None).map{ string2intOpt(_) }.getOrElse(None) match {
                  case Some(e) => {
                    e
                  }
                  case None => {
                    v.info.getOrElse("SVLEN",None).map{ string2intOpt(_) }.getOrElse(None) match {
                      case Some(svlen) => {
                        v.pos - svlen
                      }
                      case None => {
                        warning("WARNING: DELETION WITH NO VALID END or SVLEN tags!\n\""+v.getVcfStringNoGenotypes+"\"","delWithNoEnd",10);
                        -1;
                      }
                    }
                  }
                }
                if(end == -1){
                  Some(vc);
                } else {
                  vc.in_alt = Seq( vc.ref + "[" +v.chrom+":"+ end +"[" )
                  vc.addInfo("SVTYPE","BND");
                  val vc2 = vc.getOutputLineCopy();
                  vc2.addInfo("SVTYPE","BND");
                  vc2.in_pos = end;
                  vc2.in_alt = Seq( "]"+v.chrom+":"+v.pos+"]"+v.ref );
                  Seq(vc,vc2);
                }
              //} else if( a.startsWith("<DUP:TANDEM")){
              //  //ADD SUPPORT FOR TANDEMDUP?"
              //  Some(vc)
              } else if( a.startsWith("<INS") || a.startsWith("<CNV")){
                Some(vc);
              } else {
                Some(vc)
              }
            } else {
              Some(vc)
            }
          }
        } else {
          Some(vc);
        }
      }}, closeAction = (() => {
        reportln("Done with concordanceCallerSV merge","note");
      })),outHeader)
    }
  }
  
  
  class annotateCCsvWithSVset(annotateVCF : String, copyOverInfoTags : Seq[String], copyInfoPrefix : String, 
                              crossChromWin : Int = 500,withinChromWin : Int = 500) extends SVcfWalker {
    def walkerName : String = "annotateCCsvWithSVset"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("copyOverInfoTags",copyOverInfoTags.mkString("|")),
        ("copyInfoPrefix",copyInfoPrefix),
        ("crossChromWin",crossChromWin.toString),
        ("withinChromWin",withinChromWin.toString)
    )
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader;
      
      //  var spanWindowSet : Map[String,Vector[(ChrSpanPair,Vector[Vector[SVcfVariantLine]])]] = strdirOptions.map{ str => (str,Vector()) }.toMap
      
      val infoFieldDelimiter = ","
      
      val (avHeader,avIter) = SVcfLine.readVcf(getLinesSmartUnzip(annotateVCF),withProgress = false);
      
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
      def strdirSwapSVstrand( ss : String ) : String = {
        val d = ss.last;
        val ds = if(d == '>') '<' else '>';
        strandSwapSVstrand(ss.take(2)) + ds;
      }

      val copyInfo : Map[String,(String,SVcfCompoundHeaderLine)] = copyOverInfoTags.map{ itag => {
        val hline = avHeader.infoLines.find{ pp => { pp.ID == itag }}.orElse{
          error("annotateCCsvWithSVset ERROR (annotate SVs with an external SV vcf): file \""+annotateVCF+"\" does not contain INFO field: \""+itag+"\"");
          None;
        }.get
        //if(hlineOpt.isEmpty){
        //  error("annotateCCsvWithSVset ERROR (annotate SVs with an external SV vcf): file \""+annotateVCF+"\" does not contain INFO field: \""+itag+"\"");
        //}
        //val hline = hlineOpt.get;
        val nhline = new SVcfCompoundHeaderLine("INFO",ID=copyInfoPrefix+hline.ID,Number=hline.Number,Type=hline.Type,desc="(Copied from "+annotateVCF+") "+hline.desc);
        outHeader.addInfoLine(nhline);
        (hline.ID,(copyInfoPrefix+hline.ID,nhline))
      }}.toMap
      
      
      
      
      //val avmap = 
        //      chrA,chrB,strdir
        // Map[(String,String,String),scala.collection.immutable.TreeMap[Int,TreeMap[Int,Seq[(Int,Int,Boolean,SVcfVariantLine)]]]]
      var avmap = Map[(String,String,String),scala.collection.immutable.TreeMap[Int,scala.collection.immutable.TreeMap[Int,Seq[SVcfVariantLine]]]]();
      
      //def addBlankToAvMap( chrA:String,chrB:String,strdir:String){
        
      //}
      
      def addToTreeMap( v : SVcfVariantLine ){
        val (chrA,posA) = (v.chrom,v.pos);
        val (chrB,posB) = v.getSVbnd().get.bndBreakEnd;
        val strdir = v.getSVstrdirOrDie();
        val swapstrdir = v.getSVstrdirSwapOrDie();
        
        if( ! avmap.contains( (chrA,chrB,strdir) )){
          val xxmap = scala.collection.immutable.TreeMap[Int,scala.collection.immutable.TreeMap[Int,Seq[SVcfVariantLine]]](
              (posA,
                scala.collection.immutable.TreeMap[Int,Seq[SVcfVariantLine]](
                  (posB,Seq(v))
                )
              )
          )
          //new scala.collection.immutable.TreeMap[Int,scala.collection.immutable.TreeMap[Int,Seq[(Int,Int,Boolean,SVcfVariantLine)]]]()
          avmap = avmap.updated((chrA,chrB,strdir),xxmap)
        } else {
          val xxmap = avmap((chrA,chrB,strdir));
          val xx = xxmap.getOrElse( posA, scala.collection.immutable.TreeMap[Int,Seq[SVcfVariantLine]]() ).getOrElse( posB, Seq() ) :+ v
          avmap = avmap.updated( (chrA,chrB,strdir), 
               xxmap.updated(posA,
                 xxmap.getOrElse( posA, scala.collection.immutable.TreeMap[Int,Seq[SVcfVariantLine]]() ).updated(
                     posB,xx
                 )
               )
          )
        }
      }
      
      avIter.foreach{ v => {
        addToTreeMap(v);
      }}
      
      def getMatches(chrA : String, chrB:String, strdir:String, posA: Int, posB:Int, bothDirections: Boolean = true) : Seq[SVcfVariantLine] = {
        val currwin = if(chrA==chrB){ withinChromWin } else { crossChromWin };
        val swapstrdir = strdirSwapSVstrand(strdir);
        val fwdMatches = avmap.get((chrA,chrB,strdir)).map{ xxmap => xxmap.range(posA-currwin,posA+currwin).flatMap{ case (posXA,xmap) => {
          xmap.range(posB-currwin,posB+currwin).flatMap{ case (posXB,vvseq) => {
            vvseq
          }}.toSeq
        }}.toSeq }.getOrElse(Seq())
        val revMatches = avmap.get((chrB,chrA,swapstrdir)).map{ xxmap => xxmap.range(posB-currwin,posB+currwin).flatMap{ case (posXB,xmap) => {
          xmap.range(posA-currwin,posA+currwin).flatMap{ case (posXA,vvseq) => {
            vvseq
          }}.toSeq
        }}.toSeq}.getOrElse(Seq())
        
        fwdMatches ++ revMatches;
      }
      
      //val (vcfHeader,vcIter) = if(chromList.isEmpty){
      //  SVcfLine.readVcf(getLinesSmartUnzip(infile),withProgress = true)
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
         val vc = v.getOutputLine();
         val sv = v.getSVbnd().get;
         val vvm = getMatches(v.chrom,sv.bndBreakEnd._1,v.getSVstrdirOrDie(),v.pos,sv.bndBreakEnd._2)
         copyInfo.foreach{ case (oldID,(newID,headerLine)) => {
           val ival = vvm.map{ vvx => {
             vvx.info.getOrElse(oldID,None)
           }}
           if( ival.exists( _.isDefined )){
             vc.addInfo( newID, ival.map{ _.getOrElse(".") }.mkString(infoFieldDelimiter) );
           }
           
         }}
         
         vc.addInfo(copyInfoPrefix+"CHROM",vvm.map{ vvx => {
           vvx.chrom
         }}.mkString(","))
         vc.addInfo(copyInfoPrefix+"POS",vvm.map{ vvx => {
           vvx.pos
         }}.mkString(","))
         vc.addInfo(copyInfoPrefix+"ALT",vvm.map{ vvx => {
           vvx.alt.head
         }}.mkString(","))
         
         vc;
      }}, closeAction = (() => {
        reportln("","note");
      })),outHeader)
    }
  }

  def ensembleMergeSV(vcIters : Seq[Iterator[SVcfVariantLine]], 
                            crossChromWin : Int = 500,
                            withinChromWin : Int = 500,
                            headers : Seq[SVcfHeader], 
                            inputVcfTypes : Seq[String], 
                            nonStrictChecking : Boolean = false,
                            CC_ignoreSampleIds : Boolean = false //, 
                            //CC_ignoreSampleOrder : Boolean = false //NOT IMPLEMENTED
                            ) :  (Iterator[SVcfVariantLine],SVcfHeader) = {
    

    case class ChrPos( chr : Chrom, pos : Int )
    case class ChrSpan(chr : Chrom, fr : Int, to : Int)
    case class ChrSpanPair( a : ChrSpan, b : ChrSpan ){
      def expandTo( a2 : ChrPos, b2 : ChrPos ) : ChrSpanPair = {
        val currwin = if( a.chr == b.chr ){ withinChromWin } else { crossChromWin }
        ChrSpanPair(
            ChrSpan( a.chr, math.min(a.fr,a2.pos-currwin), math.max(a.to,a2.pos+currwin) ),
            ChrSpan( b.chr, math.min(b.fr,b2.pos-currwin), math.max(b.to,b2.pos+currwin) )
        )
      }
      def merge( cc : ChrSpanPair ) : ChrSpanPair = {
        ChrSpanPair(
            ChrSpan( a.chr, math.min(a.fr,cc.a.fr), math.max(a.to,cc.a.to) ),
            ChrSpan( b.chr, math.min(b.fr,cc.b.fr), math.max(b.to,cc.b.to) )
        )
      }
      def pairOverlaps( a2 : ChrPos, b2 : ChrPos ) : Boolean = {
        //val currwin = if( a.chr == b.chr ){ withinChromWin } else { crossChromWin }
        a.chr == a2.chr && 
        b.chr == b2.chr && 
        a.fr  <= a2.pos && a2.pos <= a.to &&
        b.fr  <= b2.pos && b2.pos <= b.to
      }
    }
    val strdirOptions = Seq("++>","+->","-+>","-->","++<","+-<","-+<","--<");
    
    val vcfCodes = VCFAnnoCodes();
    val CC_ignoreSampleOrder : Boolean = false;
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
    
    val callerFmtTags: Map[String,Map[String,(String,SVcfCompoundHeaderLine)]] = headers.zip(inputVcfTypes).map{ case (h,t) => {
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
    
    val callerInfoTags : Map[String,Map[String,(String,SVcfCompoundHeaderLine)]] = inputVcfTypes.map{ mc => {
          val masterIdx = inputVcfTypes.indexOf(mc)
          val masterHeader = headers(masterIdx);
          (mc,masterHeader.infoLines.map{ infoLine => {
            //val num = if(infoLine.Number == "R" || infoLine.Number == "G" || infoLine.Number == "A") "." else infoLine.Number;
            val num = if(infoLine.Number == "G") "." else infoLine.Number;
            (infoLine.ID,(mc + "_" + infoLine.ID,new SVcfCompoundHeaderLine(in_tag = "INFO",ID = ""+ mc + "_" + infoLine.ID, Number = num, Type = infoLine.Type, desc = "For the caller "+mc+", " + cleanQuotes(infoLine.desc), subType = infoLine.subType)))
          }}.toMap)
    }}.toMap
    
    val customInfo = Seq[SVcfCompoundHeaderLine](
      new SVcfCompoundHeaderLine("INFO",  "CCsv_callerSet",   "1", "String", "The list of callers (from the set: \""+inputVcfTypes.mkString(",")+"\") that supports this SV. Generated by ConcordanceCallerSV."),
      new SVcfCompoundHeaderLine("INFO",  "CCsv_duplicateCt",   ".", "Integer", "For each of the "+inputVcfTypes.length+" callers ("+inputVcfTypes.mkString(",")+"), the number of duplicate SVs. Generated by ConcordanceCallerSV."),
      new SVcfCompoundHeaderLine("INFO",  "CCsv_hasDupSV",   ".", "Integer", "Equal to 1 if there are any duplicate SVs within any single caller. 0 otherwise. Generated by ConcordanceCallerSV."),
      new SVcfCompoundHeaderLine("INFO",  "CCsv_chrA",   "1", "String", ". Generated by ConcordanceCallerSV."),
      new SVcfCompoundHeaderLine("INFO",  "CCsv_chrB",   "1", "String", ". Generated by ConcordanceCallerSV."),
      new SVcfCompoundHeaderLine("INFO",  "CCsv_posRange_A",   "1", "String", ". Generated by ConcordanceCallerSV."),
      new SVcfCompoundHeaderLine("INFO",  "CCsv_posRange_B",   "1", "String", ". Generated by ConcordanceCallerSV."),
      new SVcfCompoundHeaderLine("INFO",  "CCsv_posSpan_A",   "1", "String", ". Generated by ConcordanceCallerSV."),
      new SVcfCompoundHeaderLine("INFO",  "CCsv_posSpan_B",   "1", "String", ". Generated by ConcordanceCallerSV."),
      new SVcfCompoundHeaderLine("INFO",  "CCsv_strand",   "1", "String", ". Generated by ConcordanceCallerSV."),
      new SVcfCompoundHeaderLine("INFO",  "CCsv_strdir",   "1", "String", ". Generated by ConcordanceCallerSV.")
    ) ++ inputVcfTypes.flatMap{ cid => {
      Seq(
        new SVcfCompoundHeaderLine("INFO",  "CCsv_"+cid+"_pos_A",   ".", "String", ". Generated by ConcordanceCallerSV."),
        new SVcfCompoundHeaderLine("INFO",  "CCsv_"+cid+"_pos_B",   ".", "String", ". Generated by ConcordanceCallerSV."),
        new SVcfCompoundHeaderLine("INFO",  "CCsv_"+cid+"_ALT",   ".", "String", ". Generated by ConcordanceCallerSV."),
        new SVcfCompoundHeaderLine("INFO",  "CCsv_"+cid+"_strand",   ".", "String", ". Generated by ConcordanceCallerSV.")
        )
    }}
    
    val customFmt = Seq[SVcfCompoundHeaderLine](
      new SVcfCompoundHeaderLine("FORMAT",  "GT",   "1", "String", "Final Genotype Call.")
    )
    
    val vcfHeader = headers.head.copyHeader;
    vcfHeader.infoLines = Seq[SVcfCompoundHeaderLine]();
    vcfHeader.formatLines = Seq[SVcfCompoundHeaderLine]();
    vcfHeader.addWalkerLikeCommand("concordanceCallerSV",Seq[(String,String)](
        ("inputVcfTypes",inputVcfTypes.mkString("|")),
        ("crossChromWin",crossChromWin.toString()),
        ("withinChromWin",withinChromWin.toString())
      ))
    
    customInfo.foreach{ infoLine => {
      vcfHeader.addInfoLine(infoLine);
    }}
    customFmt.foreach{ infoLine => {
      vcfHeader.addFormatLine(infoLine);
    }}
    callerInfoTags.foreach{ case (c,tagMap) => {
      tagMap.foreach{ case (oldTag,(newTag,headerLine)) => {
        vcfHeader.addInfoLine(headerLine);
      }}
    }}
    callerFmtTags.foreach{ case (c,tagMap) => {
      tagMap.foreach{ case (oldTag,(newTag,headerLine)) => {
        vcfHeader.addFormatLine(headerLine);
      }}
    }}
    val callerHeaders = headers.zipWithIndex
    val callersWithSamps = callerHeaders.filter{ case (f,pi) => { f.titleLine.sampleList.length > 0 }}
    if( callersWithSamps.length != 0 && callersWithSamps.length != inputVcfTypes.length){
      error("ERROR: Some input VCFs have samples, some do not! ConcordanceCallerSV requires all or none.")
    }
      //headers.zipWithIndex.filter{ case (f,x) => { f.titleLine.sampleList.length > 0 }}
    val sampNames = if(callersWithSamps.length > 0){
      callersWithSamps.head._1.titleLine.sampleList;
    } else {
      Seq[String]()
    }
    val sampCt = sampNames.length;
    val sampSet = sampNames.toSet;
    
    callersWithSamps.foreach{ case (f,pi) => {
      if( f.titleLine.sampleList.length != sampCt) {
          error("Different number of samples found in the VCFs! ConcordanceCaller requires that the sample set be the same between the different VCFs. IF they are different and you still want to "+
                "merge, you can specify the overlapping sample set using the --inputKeepSamples parameter. Note that you may have to also add the --ccAllowSampleOrderDiff flag, if the samples are not "+
                "in the same order across all VCFs.")
      }
    }}
    val ignoreSampleIds = sampCt == 1 || CC_ignoreSampleIds;
        
    if(sampCt > 0){

      
      // CC_ignoreSampleIds,   CC_ignoreSampleOrder
      if( ! ((ignoreSampleIds) | (CC_ignoreSampleOrder))) {
        callersWithSamps.foreach{ case (f,pi) => {
          f.titleLine.sampleList.zip(sampNames).foreach{ case (s1,s2) => {
            if(s1 != s2){
              error("Sample IDs DO NOT MATCH! By default, ConcordanceCaller requires that the sample IDs be exactly the same and in the same order in each callers VCF file. "+
                    "If the samples have the same IDs but are in a different order, then use the ignoreSampleOrder flag. If instead the samples are in the same order but just have "+
                    "different sample IDs, then use ignoreSampleIds to ignore the sample IDs. The output ordering and/or names will come from the first VCF.")
            }
          }}
        }}
      }

      headers.zipWithIndex.foreach{ case (h,i) => {
        if(h.titleLine.sampleList.length != sampNames.length){
          error("Header for VCF "+inputVcfTypes(i)+" has "+h.titleLine.sampleList.length+" samples instead of "+sampNames.length);
        }
      }}
      
    }
    

    val bufIters : Seq[BufferedIterator[SVcfVariantLine]] = vcIters.map{ viter => {
      viter.filter{ v => {
         val svt = v.info.getOrElse("SVTYPE",None).getOrElse(".");
         val bnd = v.getSVbnd()
         if(svt != "BND" || bnd.isEmpty){
           warning("WARNING: invalid SV: \""+v.getVcfStringNoGenotypes+"\"","SV_INVALID",10)
           if( ! nonStrictChecking){
             error("INVALID SV")
           }
         }
         svt == "BND" && bnd.nonEmpty;
      }}.buffered
    }};
    
    val sortedGroupIters : BufferedIterator[Seq[Seq[SVcfVariantLine]]] = groupBySpanMulti(bufIters)(v => v.chrom)(v => v.pos).buffered;
    


    val safetyBuffer = 10;
    val callerCt = vcIters.length;
    
    //val strandOptions = Seq("++","+-","-+","--");

    
    val grpIter : Iterator[Seq[Seq[SVcfVariantLine]]] = ( new Iterator[Seq[Seq[SVcfVariantLine]]]{
      
      // [distinct SV][N callers][ variant line(s) for given callers ]
      var buffer : Vector[Vector[Vector[SVcfVariantLine]]] = Vector()
      var spanWindowSet : Map[String,Vector[(ChrSpanPair,Vector[Vector[SVcfVariantLine]])]] = strdirOptions.map{ str => (str,Vector()) }.toMap
      //var currline : SVcfVariantLine = null;
      //var spanWindowSet : Vector[((String,Int,Int),(String,Int,Int),Vector[SVcfVariantLine])]
      
      def shiftRemainderToBuffer( x : ChrPos ){
        strdirOptions.foreach{ strdir => {
          val (swsBuf, swsKeep) = spanWindowSet(strdir).partition{ case (cc,lines) => {
            cc.a.chr != x.chr || cc.a.to + safetyBuffer < x.pos;
          }}
          spanWindowSet = spanWindowSet.updated(strdir,swsKeep);
          //spanWindowSet = swsKeep;
          buffer = buffer ++ swsBuf.map{ case (cc,lines) => lines }
        }}
      }
      
      def addLineToSet( v : SVcfVariantLine, callerIdx : Int ){
        val xa = ChrPos( Chrom(v.chrom), v.pos );
        val xsv = v.getSVbnd().get
        //val str = xsv.strands;
        val strdir = v.getSVstrdirOrDie();
        val (xbChr,xbPos) = xsv.bndBreakEnd
        val xb = ChrPos( Chrom(xbChr),xbPos );
        val (matchSet,nonMatchSet) = spanWindowSet(strdir).partition{ case (cc,lines) => {
          cc.pairOverlaps(xa,xb)
        }}
        if( matchSet.length == 0 ){
          val xwin = if(xa.chr == xb.chr){ withinChromWin } else { crossChromWin };
          val newspan = ChrSpanPair( ChrSpan(xa.chr,xa.pos-xwin,xa.pos+xwin), ChrSpan(xb.chr, xb.pos-xwin,xb.pos+xwin) );
          val lines = Vector.fill[Vector[SVcfVariantLine]]( callerCt )( Vector() ).updated( callerIdx, Vector( v ) )
          spanWindowSet = spanWindowSet.updated(strdir, spanWindowSet(strdir) :+ (newspan, lines));
        } else if( matchSet.length == 1){
          val (cc,lines) = matchSet.head;
          val newcc = cc.expandTo(xa,xb);
          val newln = lines.updated(callerIdx,lines(callerIdx) :+ v)
          spanWindowSet = spanWindowSet.updated(strdir, nonMatchSet :+ (newcc,newln))
        } else if( matchSet.length >= 2){
          warning("WARNING: Merging formerly-separate SVs due to an intermediate SV that is close to both on both ends! \n\""+v.getVcfStringNoGenotypes+"\"","SV_MERGE_SV_SET_DUE_TO_MID_SV",100);
          val newcc = matchSet.tail.foldLeft(matchSet.head._1){ case (soFar,( cc, lines )) => {
            soFar.merge(cc)
          }}.expandTo(xa,xb)
          var newln = matchSet.tail.foldLeft(matchSet.head._2){ case (soFar,( cc, lines )) => {
            soFar.zip(lines).map{ case (z1,z2) => {
              z1 ++ z2
            }}
          }}
          newln = newln.updated(callerIdx,newln(callerIdx) :+ v)
          spanWindowSet = spanWindowSet.updated(strdir, nonMatchSet :+ (newcc,newln))
        }
      }
      
      def hasNext : Boolean = sortedGroupIters.hasNext || buffer.nonEmpty || spanWindowSet.nonEmpty ;
      def next : Seq[Seq[SVcfVariantLine]] = {
        if(buffer.nonEmpty){
          val nx = buffer.head;
          buffer = buffer.tail;
          nx;
        } else if( sortedGroupIters.hasNext ){
          val nextPosVariants = sortedGroupIters.next();
          nextPosVariants.zipWithIndex.foreach{ case (vvs,idx) => {
            vvs.foreach{ v => {
              addLineToSet(v,idx)
            }}
          }}
          val vex = nextPosVariants.flatten.head
          shiftRemainderToBuffer( ChrPos( Chrom(vex.chrom), vex.pos ) );
          next;
        } else if( spanWindowSet.nonEmpty ) {
          // if we've finished reading from file, dump the remainder into the buffer, empty spanWindowSet
          strdirOptions.foreach{ str => {
            val swsBuf = spanWindowSet(str)
            //spanWindowSet = swsKeep;
            buffer = buffer ++ swsBuf.map{ case (cc,lines) => lines }
          }}
          spanWindowSet = Map();
          next;
        } else {
          error("ILLEGAL OPERATION: NEXT ON AN EMPTY ITERATOR, ChrSpanIterator in ConcordanceCallerSV")
          null;
        }
      }
    })
    val blankGeno = Array.fill[String](sampCt)(".")
    
    val finIter = grpIter.zipWithIndex.map{ case (vcSeqSeq,iix) => {
      val exvc = vcSeqSeq.flatten.head;
      val vb = SVcfOutputVariantLine(
           in_chrom = exvc.chrom,
           in_pos = exvc.pos,
           in_id = iix.toString(),
           in_ref = exvc.ref,
           in_alt = exvc.alt,
           in_qual = ".",
           in_filter = ".",
           in_info = Map[String,Option[String]](),
           in_format = Seq[String](),
           in_genotypes = SVcfGenotypeSet(Seq[String]("GT"), Array.fill(1,sampCt)("."))
      )
      val callerSupport = vcSeqSeq.zip(inputVcfTypes).filter{ case ( vss, callerID ) => {
         vss.nonEmpty
      }}.map{ case (vss, callerID) => callerID }
      val callerIx = vcSeqSeq.zipWithIndex.zip(inputVcfTypes).filter{ case (( vss, callerIDX ),callerID) => {
         vss.nonEmpty
      }}.map{ case (( vss, callerIDX ),callerID) => (callerIDX,callerID) }
      val callerVarCt = vcSeqSeq.zip(inputVcfTypes).map{ case ( vss, callerID ) => {
         vss.length
      }}
      vb.addInfo("CCsv_callerSet",callerSupport.mkString(","));
      vb.addInfo("CCsv_duplicateCt",callerSupport.mkString(","));
      vb.addInfo("CCsv_hasDupSV",if(callerVarCt.max > 1){ "1" } else { "0" });
      
      vb.addInfo("CCsv_chrA", vb.chrom);
      vb.addInfo("CCsv_chrB", vcSeqSeq.flatten.head.getSVbnd().get.bndBreakEnd._1 );
      
      callerIx.foreach{ case (cix,cid) => {
        vb.addInfo("CCsv_"+cid+"_pos_A",vcSeqSeq(cix).map{ v => v.pos }.mkString(","));
        vb.addInfo("CCsv_"+cid+"_ALT",vcSeqSeq(cix).map{   v => v.alt.head }.mkString(","));
        vb.addInfo("CCsv_"+cid+"_pos_B",vcSeqSeq(cix).map{ v => v.getSVbnd().get.bndBreakEnd._2 }.mkString(","));
        vb.addInfo("CCsv_"+cid+"_strand",vcSeqSeq(cix).map{ v => v.getSVbnd().get.strands }.mkString(","));
      }}
      val posA = vcSeqSeq.flatten.map{ v => {
        v.pos
      }}
      val posB = vcSeqSeq.flatten.map{ v => {
        v.getSVbnd().get.bndBreakEnd._2
      }}
      val chrA = vcSeqSeq.flatten.head.chrom;
      val chrB = vcSeqSeq.flatten.head.getSVbnd().get.bndBreakEnd._1;
      val strand = vcSeqSeq.flatten.head.getSVbnd().get.strands
      val dir    = if( chrA == chrB ){
        if( posA.head < posB.head ){
          ">"
        } else {
          "<"
        }
      } else if( Chrom(chrA).compare(Chrom(chrB)) < 0){
        ">"
      } else {
        "<"
      }
      if( chrA == chrB && posA.min < posB.max && posB.min < posA.max ){
        warning("WARNING: OVERLAPPING SV START-END RANGES! Merge may behave oddly! \n   \"" +vcSeqSeq.flatten.map{ v => v.getVcfStringNoGenotypes }.mkString("\"\n   \"") +"\"","overlappingSVstartEndRanges",10)
        vb.addInfo("CCsv_WARN_overlapEnds", "1" );
      }

      vb.addInfo("CCsv_posRange_A", posA.min+"-"+posA.max );
      vb.addInfo("CCsv_posRange_B", posB.min+"-"+posB.max );
      vb.addInfo("CCsv_posSpan_A", (posA.max-posA.min)+"" );
      vb.addInfo("CCsv_posSpan_B", (posB.max-posB.min)+"" );
      vb.addInfo("CCsv_strand", strand );
      vb.addInfo("CCsv_strdir",strand+dir);

      callerIx.foreach{ case (cix, cid) => {
         callerInfoTags(cid).map{ case (fmtID,(newFmtID,newFmtHeaderLine)) => {
           (newFmtID,vcSeqSeq(cix).map{ v => {
             v.info.getOrElse( fmtID, None )
           }})
         }}.filter{  case (newFmtID,xx) => {
           xx.exists( k => k.nonEmpty )
         }}.foreach{ case (newFmtID,xx) => {
           vb.addInfo(newFmtID,xx.map{ k => k.getOrElse(".") }.mkString(","));
         }}
      }}
      callerIx.foreach{ case (cix,cid) => {
        callerFmtTags(cid).map{ case (fmtID,(newFmtID,newFmtHeaderLine)) => {
          (newFmtID,vcSeqSeq(cix).map{ v => {
             (v,v.genotypes.fmt.indexOf(fmtID))
          }})
        }}.filter{ case (newFmtID,fidxSeq) => {
          fidxSeq.exists{ case (v,gii) => gii != -1 }
        }}.foreach{ case (newFmtID,fidxSeq) => {
          val g = fidxSeq.map{ case (v,gii) => {
             if(gii != -1){
               v.genotypes.genotypeValues(gii)
             } else {
               blankGeno
             }
          }}
          vb.genotypes.addGenotypeArray(newFmtID, 
              g.tail.foldLeft( g.head ){ case (soFar, curr) => {
                soFar.zip(curr).map{ case (a,b) => { a+";"+b }}
              }})
          //
        }}
      }}
      
      vb
    }}
    
    (finIter, vcfHeader)

}
      /*
     callerFmtTags:  Map[callerID, Map[ originalFmtID,  (newFmtID,  newFmtHeaderLine  )]]
     callerInfoTags: Map[callerID, Map[ originalInfoID, (newInfoID, newInfoHeaderLine )]]
     sampMatchIdx: Map[callerID, Idx Seq]
     
     */
    
  
  /*
         new SVcfCompoundHeaderLine("INFO",  OPTION_TAGPREFIX+"CCsv_callerSets",   "1", "String", "The list of callers (from the set: \""+inputVcfTypes.mkString(",")+"\") that supports this SV. Generated by ConcordanceCallerSV."),
      new SVcfCompoundHeaderLine("INFO",  OPTION_TAGPREFIX+"CCsv_duplicateCt",   ".", "Integer", "For each of the "+inputVcfTypes.length+" callers ("+inputVcfTypes.mkString(",")+"), the number of duplicate SVs. Generated by ConcordanceCallerSV."),
      new SVcfCompoundHeaderLine("INFO",  OPTION_TAGPREFIX+"CCsv_hasDupSV",   ".", "Integer", "Equal to 1 if there are any duplicate SVs within any single caller. 0 otherwise. Generated by ConcordanceCallerSV.")
   
   */
  
        /*
        sortedGroupIters.flatMap{ posSeqSeq : Seq[Seq[SVcfVariantLine]] => {
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
              if(! currInfoLines.contains(oldTag)){
                report("ERROR INCOMING: Cannot find tag \""+oldTag+"\" in callerName=\""+callerName+"\"\n"+
                    "    available tags are: ["+currInfoLines.map{_._1}.mkString(",")+"]",
                    "note")
              }
              
              val (newTag,infoLine) = currInfoLines(oldTag);
              vb.addInfoOpt(newTag,fieldVal);
            }}
            if( vc.genotypes.genotypeValues.nonEmpty && vc.genotypes.fmt.nonEmpty ){
              val sampidx = sampMatchIdx.get(callerName);
              if(sampidx.isEmpty){
                error("IMPOSSIBLE STATE: sampidx.isEmpty! This should never happen. If you see this error, than you have somehow exposed a bug. Please submit a bug report to the issues page: \"https://github.com/hartleys/vArmyKnife/issues\"")
              }
              vc.genotypes.fmt.zipWithIndex.foreach{ case (oldTag,idx) => {
                if(! currFmtLines.contains(oldTag)){
                  warning("Fatal error: Tag: "+oldTag+" not found in given header!\n Found header tags: "+currFmtLines.keys.toSeq.sorted.mkString(","),"PREERROR_WARNING",-1)
                }
                val (newTag,fmtLine) = currFmtLines(oldTag);
                sampidx.get match {
                  case Some(sindices) => {
                    vb.genotypes.addGenotypeArray(newTag,sindices.toArray.map{ si => {
                      vc.genotypes.genotypeValues(idx)(si);
                    }})
                  }
                  case None => {
                    vb.genotypes.addGenotypeArray(newTag,vc.genotypes.genotypeValues(idx));
                  }
                }
                
              }}
            }
            
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
            
            //if( vcSeq.head.genotypes.genotypeValues.nonEmpty && vcSeq.head.genotypes.fmt.nonEmpty ){
              vbb.genotypes.genotypeValues = vbb.genotypes.genotypeValues.zip(vbb.genotypes.fmt).withFilter{ case (garray,fmt) => {
                ! newFmtFieldSet.contains(fmt);
              }}.map{ case (garray,fmt) => garray }
              vbb.genotypes.fmt = vbb.genotypes.fmt.filter{ fmt => {
                ! newFmtFieldSet.contains(fmt);
              }}
              vc.genotypes.fmt.zipWithIndex.foreach{ case (oldTag,idx) => {
                val (newTag,fmtLine) = currFmtLines(oldTag);
                vbb.genotypes.addGenotypeArray(newTag,vc.genotypes.genotypeValues(idx));
              }}
            //}
            vc.info.map{ case (oldTag,fieldVal) => {
              val (newTag,infoLine) = currInfoLines(oldTag);
              vbb.addInfoOpt(newTag,fieldVal);
            }}

            vbb
          }}
        }}
        vbSeq.zipWithIndex.map{ case (vbb,vbbIdx) => {
          vbb.addInfo(OPTION_TAGPREFIX+"EC_duplicateCt",vbSeq.length.toString);
          vbb.addInfo(OPTION_TAGPREFIX+"EC_duplicateIdx",vbbIdx.toString);
        }}
        vbSeq;
      }}
      
    }
    
    })*/
    
  //}
  
  case class CountMatchMatrix(matchFile : String, gtTag : String = "GT", matchPctCutoff : Double = 0.5 ) extends SVcfWalker {
    
    def walkerName : String = "CountMatchMatrix"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("gtTag", gtTag)
    );
    
    reportln("CountMatchMatrix: file="+matchFile+", gt="+gtTag+", pctCutoff="+matchPctCutoff,"note");
    
    //val refFastaTool = internalUtils.GatkPublicCopy.refFastaTool(genomeFa = genomeFa);
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      
      
      val matchct = scala.Array.ofDim[Int](vcfHeader.sampleCt,vcfHeader.sampleCt)
      val totalct = scala.Array.ofDim[Int](vcfHeader.sampleCt)


      (addIteratorCloseAction( iter = vcMap(vcIter){v => {        
        //val vc = v.getOutputLine();
        val gtidx = v.genotypes.fmt.indexOf(gtTag);
        if(gtidx >= 0){
           val gt = v.genotypes.genotypeValues(gtidx).zipWithIndex;
           val ff = gt.filter{ case (g,i) => {
             g.contains("1");
           }}
           warning("Found "+ff.length+" alts","ALT_CT",10);
           ff.foreach{ case (a,i) => {
             totalct(i) = totalct(i) + 1;
             ff.foreach{ case (b,j) => {
               if( a == b && i != j ){
                 matchct(i)(j) = matchct(i)(j) + 1
               }
             }}
           }}
        } else {
          warning("No GT ("+gtTag+") found: ["+v.genotypes.fmt.mkString(",")+"]","NO_GT_FOUND",10);
        }
        v
      }}, closeAction = (() => {
        var out = openWriter(matchFile+".txt");
        reportln("Writing matches...","note");
        Range(1,vcfHeader.sampleCt).foreach{ i => {
          Range(0,i).foreach{ j => {
        //Range(0,vcfHeader.sampleCt).foreach{ i => {
        //  Range(0,vcfHeader.sampleCt).foreach{ j => {
            val m = matchct(i)(j)
            val a = m.toDouble / totalct(i).toDouble;
            val b = m.toDouble / totalct(j).toDouble;
            if(a > matchPctCutoff || b > matchPctCutoff ){
                warning("Found match: ["+a+","+b+"]","MATCH_FOUND",10);
                val maxab = math.max(a,b);
                val minab = math.min(a,b);
                out.write(vcfHeader.titleLine.sampleList(i)+"\t"+vcfHeader.titleLine.sampleList(j)+"\t"+m+"\t"+totalct(i)+"\t"+totalct(j)+"\t"+a+"\t"+b+"\t"+minab+"\t"+maxab+"\n");
            }
          }}
        }}
        out.close();
        
        out = openWriter(matchFile+".totalCt.txt");
        reportln("Writing totals...","note");
        Range(0,vcfHeader.sampleCt).foreach{ i => {
               out.write(vcfHeader.titleLine.sampleList(i)+"\t"+totalct(i)+"\n");
        }}
        out.close();
      })),outHeader)
      
    }
  }

  case class ExtractFormatMatrix(matchFile : String, gtTag : String = "GT", infoFields : Seq[String] = Seq(), longForm : Boolean = false, noVarInfo : Boolean = false) extends SVcfWalker {
    
    def walkerName : String = "ExtractFormatMatrix"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("gtTag", gtTag)
    );
    
    reportln("CountMatchMatrix: file="+matchFile+", gt="+gtTag,"note");
    
    //val refFastaTool = internalUtils.GatkPublicCopy.refFastaTool(genomeFa = genomeFa);
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      
      
      //val matchct = scala.Array.ofDim[Int](vcfHeader.sampleCt,vcfHeader.sampleCt)
      //val totalct = scala.Array.ofDim[Int](vcfHeader.sampleCt)
      val out = openWriter(matchFile+".txt");
      
      val gtTagList = gtTag.split(",").toVector;
      val emptySeq = vcfHeader.titleLine.sampleList.map{ x => "." }.toArray;
      val sampIndices = vcfHeader.titleLine.sampleList.indices;
      val titleRowStart = if(noVarInfo) "" else "#CHROM\tPOS\tID\tREF\tALT\t"
      
      if(longForm){
        out.write(titleRowStart+infoFields.mkString("\t")+( if( infoFields.length > 0) "\t" else "" )+"sampleID\t"+gtTagList.mkString("\t")+"\n");
      } else {
         out.write(titleRowStart+infoFields.mkString("\t")+( if( infoFields.length > 0) "\t" else "" )+vcfHeader.titleLine.sampleList.mkString("\t")+"\n");
      }
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {        
        //val vc = v.getOutputLine();
        //val gtidx = v.genotypes.fmt.indexOf(gtTag);
        val gtval = gtTagList.map{ gg => {
          val gtidx = v.genotypes.fmt.indexOf(gg)
          if( gtidx < 0 ){
            emptySeq
          } else {
            v.genotypes.genotypeValues(gtidx)
          }
        }}
        val ifval = infoFields.map{ ff => { 
          v.info.getOrElse(ff,None).getOrElse(".")
        }}
        //val gt = v.genotypes.genotypeValues(gtidx);
        val rowStart = if( noVarInfo ) "" else (v.chrom+"\t"+v.pos+"\t"+v.id+"\t"+v.ref+"\t"+v.alt.mkString(",")+"\t");
        if(longForm){
          sampIndices.foreach{ ii => {
            out.write(rowStart+ ifval.mkString("\t")+( if( ifval.length > 0) "\t" else "" )+vcfHeader.titleLine.sampleList(ii)+"\t"+ gtval.map{ ggv => {
                ggv(ii)
              }}.mkString("\t")+"\n");
          }}
        } else {
          out.write(rowStart+ ifval.mkString("\t")+( if( ifval.length > 0) "\t" else "" )+ sampIndices.map{ ii => {
            gtval.map{ ggv => {
              ggv(ii)
            }}.mkString("|");
          }}.mkString("\t") +"\n");
        }

        v
      }}, closeAction = (() => {
        out.close();
      })),outHeader)
      
    }
  }
  
    
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
  
  case class RemoveDuplicateLinesWalker(dupTag : String = OPTION_TAGPREFIX+"remDup", compileTags : Boolean = true) extends SVcfWalker {
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
                                      geneTag : List[String] = List[String](OPTION_TAGPREFIX+"ANNO_geneList_LOF"),
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
                                      geneTag : List[String] = List[String](OPTION_TAGPREFIX+"ANNO_geneList_LOF"),
                                      sectionBy : String = "sampGroup",
                                      subtractGeneTags : Option[List[String]] = None
                                      
              */
             
              val cvat = CreateVariantAFTable( keepVariantExpressions = parser.get[List[String]]("keepVariantExpressions"),
                  geneList = parser.get[Option[List[String]]]("geneList").get,
                  keepVarExprNames = parser.get[Option[List[String]]]("keepVarExprNames").get,
                  countColumns = alleCtFields,
                  countColumnNames = alleCtFields,
                  outfilePrefix = parser.get[Option[String]]("outfilePrefix").get,
                  geneTag = parser.get[Option[List[String]]]("geneTag").getOrElse(List[String](OPTION_TAGPREFIX+"ANNO_geneList_LOF")),
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
                            geneTag = parser.get[Option[List[String]]]("geneTag").getOrElse(List[String](OPTION_TAGPREFIX+"ANNO_geneList_LOF")),
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
                                      geneTag : List[String] = List[String](OPTION_TAGPREFIX+"ANNO_geneList_LOF"),
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
   
  case class ReorderSamples(sampleOrdering : Seq[String], sort : Boolean = false) extends SVcfWalker {
    def walkerName : String = "ReorderSamples"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("sampOrd",   if(sort){ "alphabeticalSort" } else {sampleOrdering.take(10).mkString(",") + ( if(sampleOrdering.length > 10 ){"..."}else{""})})
    );
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      val missingSamp = sampleOrdering.find{ s => {
        ! vcfHeader.titleLine.sampleList.contains(s);
      }}
      missingSamp.foreach{ s =>{
        error("Sample reordering failed! Cannot find sample: "+s);
      }} 
      val sampIX = if(sort){
        vcfHeader.titleLine.sampleList.zipWithIndex.sorted.toArray
      } else {
        sampleOrdering.map{ s => {
          vcfHeader.titleLine.sampleList.zipWithIndex.find{ case(ss,ix) => ss == s }.get
        }}.toArray
      }
      outHeader.titleLine = SVcfTitleLine(sampIX.map{ case (ss,ix) => ss });
      
      if(sampIX.zipWithIndex.exists{ case ((ss,ix),ixx) => {
        ix != ixx
      }}){
        reportln("ReorderSamples: Samples have been reordered!","note")
      } else {
        reportln("ReorderSamples: no change in order, samples are already in order!","note")
      }
      
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine();
        vc.genotypes.genotypeValues = vc.genotypes.genotypeValues.map{ gg => {
          sampIX.map{ case (ss,ix) => gg(ix)}
        }}
         
        vc
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
      //vc.dropInfo(overwriteInfos);
    }
  }
  //               Seq(new CopyFieldsToInfo(qualTag = copyQualToInfo, filterTag = copyFilterToInfo, idTag = copyIdToInfo, copyFilterToGeno=copyFilterToGeno, copyInfoToGeno=copyInfoToGeno))


  case class CopyFieldsToInfo(qualTag : Option[String], filterTag : Option[String], idTag : Option[String], 
                              refTag : Option[String] = None, altTag : Option[String] = None,
                              copyFilterToGeno : Option[String] = None,copyQualToGeno : Option[String]= None,
                              copyInfoToGeno : List[String]= List[String]()) extends SVcfWalker {
    def walkerName : String = "CopyFieldsToInfo"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("qualTag",   qualTag.getOrElse("None")),
        ("filterTag", filterTag.getOrElse("None")),
        ("idTag", idTag.getOrElse("None")),
        ("copyFilterToGeno",copyFilterToGeno.getOrElse("None")),
        ("copyInfoToGeno", copyInfoToGeno.padTo(1,".").mkString(",") )
    );
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      qualTag.map{ tagID => {
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",tagID,Number="1",Type="Float",desc="QUAL field"));
      }}
      filterTag.map{ tagID => {
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",tagID,Number="1",Type="String",desc="FILTER field"));
      }}
      idTag.map{ tagID => {
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",tagID,Number="1",Type="String",desc="ID field"));
      }}
      altTag.map{ tagID => {
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",tagID,Number="1",Type="String",desc="ALT field"));
      }}
      refTag.map{ tagID => {
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",tagID,Number="1",Type="String",desc="REF field"));
      }}
      copyFilterToGeno.map{ tagID => {
        outHeader.addFormatLine(new SVcfCompoundHeaderLine("FORMAT",tagID,Number="1",Type="String",desc="ID field"))
      }}
      copyQualToGeno.map{ tagID => {
        outHeader.addFormatLine(new SVcfCompoundHeaderLine("FORMAT",tagID,Number="1",Type="Float",desc="ID field"))
      }}
      val copyInfoPairs = copyInfoToGeno.map{ t => {
        val c = t.split(",");
        if( c.length != 2){
          error("Error: copyInfoToGeno must have 2 comma delimited values: the info tag and the format tag!")
        }
        (c(0),c(1))
      } }
      
      val sampleCt = vcfHeader.titleLine.sampleList.length;
      
      copyInfoPairs.map{ case (info,geno) => {
        outHeader.addFormatLine(new SVcfCompoundHeaderLine("FORMAT",geno,Number="1",Type="Float",desc="Info column copied from "+info+" (todo copy over info)"))
      }}
      
      val overwriteInfos : Set[String] = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( outHeader.addedInfos );
      if( overwriteInfos.nonEmpty ){
        notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      }
      outHeader.reportAddedInfos(this)
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine();
        //vc.dropInfo(overwriteInfos);
        qualTag.map{ tagID => {
          vc.addInfo(tagID, v.qual)
        }}
        filterTag.map{ tagID => {
          vc.addInfo(tagID, v.filter.replaceAll("[;= ]","."))
        }}
        idTag.map{ tagID => {
          vc.addInfo(tagID, v.id.replaceAll("[;= ]","."))
        }}
        refTag.map{ tagID => {
          vc.addInfo(tagID, v.ref)
        }}
        altTag.map{ tagID => {
          vc.addInfo(tagID, v.alt.mkString(","))
        }}
        
        copyInfoPairs.map{ case (info,geno) => {
          v.info.getOrElse(info,None).map{ infovalue => {
            vc.genotypes.addGenotypeArray(geno,Array.fill(sampleCt)( infovalue ))
          }}
          //addGenotypeArray
        }}
        copyFilterToGeno.map{ tagID => {
          
          vc.genotypes.addGenotypeArray(tagID,Array.fill(sampleCt)( v.filter.replaceAll("[,;= ]",".") ))
          //addGenotypeArray
        }}
        copyQualToGeno.map{ tagID => {
          vc.genotypes.addGenotypeArray(tagID,Array.fill(sampleCt)( v.qual ))
        }}
        vc
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
      //vc.dropInfo(overwriteInfos);
    }
  }
  
  case class copyInfoToField(infoField : String, columnID: String) extends SVcfWalker {
    def walkerName : String = "copyInfoToField"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("infoField",   infoField),
        ("columnID", columnID)
    );
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);

      val overwriteInfos : Set[String] = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( outHeader.addedInfos );
      if( overwriteInfos.nonEmpty ){
        notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      }
      outHeader.reportAddedInfos(this)
      
      val columnIDoptions = Seq("CHROM","POS","ID","REF","ALT","QUAL","FILTER")
      if( ! columnIDoptions.contains( columnID )) {
        error("ERROR: copyInfoToField: columnID param must be one of: "+columnIDoptions.mkString(","));
      }
      
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine();
        val x = v.info.getOrElse(infoField,None).getOrElse(".");
        columnID match {
          case "CHROM"   => vc.in_chrom = x;
          case "POS"     => vc.in_pos = string2int(x);
          case "ID"      => vc.in_id  = x;
          case "REF"     => vc.in_ref = x;
          case "ALT"     => vc.in_alt = x.split(",").toSeq;
          case "QUAL"    => vc.in_qual = x;
          case "FILTER"  => vc.in_filter = x;
        }
        //vc.dropInfo(overwriteInfos);
        /*qualTag.map{ tagID => {
          vc.addInfo(tagID, v.qual)
        }}
        filterTag.map{ tagID => {
          vc.addInfo(tagID, v.filter.replaceAll("[;= ]","."))
        }}
        idTag.map{ tagID => {
          vc.addInfo(tagID, v.id.replaceAll("[;= ]","."))
        }}
        refTag.map{ tagID => {
          vc.addInfo(tagID, v.ref)
        }}
        altTag.map{ tagID => {
          vc.addInfo(tagID, v.alt.mkString(","))
        }}
        
        copyInfoPairs.map{ case (info,geno) => {
          v.info.getOrElse(info,None).map{ infovalue => {
            vc.genotypes.addGenotypeArray(geno,Array.fill(sampleCt)( infovalue ))
          }}
          //addGenotypeArray
        }}
        copyFilterToGeno.map{ tagID => {
          
          vc.genotypes.addGenotypeArray(tagID,Array.fill(sampleCt)( v.filter.replaceAll("[,;= ]",".") ))
          //addGenotypeArray
        }}
        copyQualToGeno.map{ tagID => {
          vc.genotypes.addGenotypeArray(tagID,Array.fill(sampleCt)( v.qual ))
        }}*/
        vc
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
      //vc.dropInfo(overwriteInfos);
    }
  }
  case class addHeaderLine(headerLineText : String) extends SVcfWalker {
    def walkerName : String = "addHeaderLine"
    var headerType : String = "unk"
    var headerID : String = "unk"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
         ("headerLineType",headerType),
         ("headerLineID",headerID)        
    );
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader
        val line = headerLineText;
        if(line.startsWith("##INFO=")){
          headerType = "INFO"
          val ln = internalUtils.VcfTool.SVcfLine.makeCompoundLineFromString(line);
          headerID = ln.ID;
          outHeader.addInfoLine(ln);
        } else if(line.startsWith("##FORMAT=")){
          headerType = "FORMAT"
          val ln = internalUtils.VcfTool.SVcfLine.makeCompoundLineFromString(line);
          headerID = ln.ID;
          outHeader.addFormatLine(ln);
        } else {
          headerType = "GENERIC"
          val ln = internalUtils.VcfTool.SVcfLine.makeSimpleHeaderLineFromString(line);
          outHeader.otherHeaderLines = outHeader.otherHeaderLines :+ ln;
        }
      outHeader.addWalk(this);
      (vcIter,outHeader)
    }
  }
  case class removeUnannotatedFields() extends SVcfWalker {
    def walkerName : String = "removeUnannotatedFields"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        
    );
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      val infoset = vcfHeader.infoLines.map{ hh => hh.ID }.toSet
      val fmtset  = vcfHeader.formatLines.map{ hh => hh.ID }.toSet
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine();
          
        vc.in_info = vc.info.filter{ case (xx,yy) => {
          infoset.contains(xx);
        }} 
        val (gt,gtkeepset) = vc.genotypes.fmt.zipWithIndex.filter{ case (gg,ii) => {
          fmtset.contains(gg)
        }}.unzip
        vc.genotypes.fmt = gt;
        vc.genotypes.genotypeValues = vc.genotypes.genotypeValues.zipWithIndex.filter{ case (gx,ii) => {
          gtkeepset.contains(ii);
        }}.map{ _._1 }
        vc
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
    }
  }
  
 /* case class rmHeaderLine(headerLineTextPrefix : String) extends SVcfWalker {
    def walkerName : String = "addHeaderLine"
    var headerType : String = "unk"
    var headerID : String = "unk"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
         ("headerLineType",headerType),
         ("headerLineID",headerID)        
    );
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader
        val line = headerLineText;
        if(line.startsWith("##INFO=")){
          headerType = "INFO"
          val ln = internalUtils.VcfTool.SVcfLine.makeCompoundLineFromString(line);
          headerID = ln.ID;
          outHeader.addInfoLine(ln);
        } else if(line.startsWith("##FORMAT=")){
          headerType = "FORMAT"
          val ln = internalUtils.VcfTool.SVcfLine.makeCompoundLineFromString(line);
          headerID = ln.ID;
          outHeader.addFormatLine(ln);
        } else {
          headerType = "GENERIC"
          val ln = internalUtils.VcfTool.SVcfLine.makeSimpleHeaderLineFromString(line);
          outHeader.otherHeaderLines = outHeader.otherHeaderLines :+ ln;
        }
      outHeader.addWalk(this);
      (vcIter,outHeader)
    }
  }*/
  
  case class reorderGT(gtOrder:Seq[String]) extends SVcfWalker {
    def walkerName : String = "reorderGT"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("gtOrder",   gtOrder.mkString("/")),
    );
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);

      val overwriteInfos : Set[String] = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( outHeader.addedInfos );
      if( overwriteInfos.nonEmpty ){
        notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      }
      outHeader.reportAddedInfos(this)
      
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine();
        
        val ordGeno = gtOrder.flatMap{ gg => {
          v.genotypes.getGtTag(gg)
        }}
        val extraGeno = v.genotypes.fmt.zipWithIndex.filter{ case (gg,gi) => {
          ! gtOrder.contains(gg)
        }}.map{ case (gg,gi) => {
          v.genotypes.genotypeValues(gi);
        }}
        
        //vc.in_genotypes = ( ++ ).toArray
        
        vc
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
      //vc.dropInfo(overwriteInfos);
    }
  }
  
  case class CopyInfoToGeno(info : String, geno : String) extends SVcfWalker {
    def walkerName : String = "CopyInfoToGeno"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("info",  info),
        ("geno",geno)
    );
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      val copyInfoPairs = Seq((info,geno));
      
      val sampleCt = vcfHeader.titleLine.sampleList.length;
      
     outHeader.addFormatLine(new SVcfCompoundHeaderLine("FORMAT",geno,Number=".",Type="String",desc="Info column copied from "+info+" (todo copy over info)"))
      
      val overwriteInfos : Set[String] = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( outHeader.addedInfos );
      if( overwriteInfos.nonEmpty ){
        notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      }
      outHeader.reportAddedInfos(this)
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine();
        vc.genotypes.dropGenotypeArray( geno );
          v.info.getOrElse(info,None).map{ infovalue => {
            val cleanvalue = infovalue.replace(":","/");
            
            vc.genotypes.addGenotypeArray(geno,Array.fill(sampleCt)( cleanvalue ))
          }}
        vc
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
      //vc.dropInfo(overwriteInfos);
    }
  }
  
  case class CopyAllToGeno() extends SVcfWalker {
    def walkerName : String = "CopyAllToGeno"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        
    );
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      //val copyInfoPairs = Seq((info,geno));
      
      val sampleCt = vcfHeader.titleLine.sampleList.length;
      
      vcfHeader.infoLines.foreach{ vi => {
        if( vi.Type == "Flag" ){
           outHeader.addFormatLine(new SVcfCompoundHeaderLine("FORMAT",vi.ID,Number="1",Type="Integer",desc="Info flag copied and converted to numeric, "+vi.desc))
        } else {         
           outHeader.addFormatLine(new SVcfCompoundHeaderLine("FORMAT",vi.ID,Number=vi.Number,Type=vi.Type,desc="Info column copied, "+vi.desc))
        }
      }}
      outHeader.addFormatLine(new SVcfCompoundHeaderLine("FORMAT","FILTER",Number=".",Type="String",desc="FILTER column copied to FORMAT."))
     
      
      val overwriteInfos : Set[String] = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( outHeader.addedInfos );
      if( overwriteInfos.nonEmpty ){
        notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      }
      outHeader.reportAddedInfos(this)
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine();
        vcfHeader.infoLines.foreach{ vi => {
          vc.genotypes.dropGenotypeArray( vi.ID );
          if( vi.Type == "Flag" ){
            val cleanvalue = if( v.info.contains( vi.ID ) ) "1" else "0";
            vc.genotypes.addGenotypeArray(vi.ID,Array.fill(sampleCt)( cleanvalue ))
          } else {
            v.info.getOrElse(vi.ID,None).foreach{ infovalue => {
              val cleanvalue = infovalue.replace(":","/");
              vc.genotypes.addGenotypeArray(vi.ID,Array.fill(sampleCt)( cleanvalue ))
            }}
          }
        }}
        vc.genotypes.addGenotypeArray("FILTER",Array.fill(sampleCt)( v.filter.replace(":","/") ))
          
        vc
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
      //vc.dropInfo(overwriteInfos);
    }
  }
  case class FixFormatFieldMetadata( field : String, num : Option[String], desc : Option[String], ty : Option[String] , removeMeta : Boolean ) extends SVcfWalker {
    def walkerName : String = "FixFormatFieldMetadata"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("field",field),
        ("num",num.getOrElse("None")),
        ("desc",desc.getOrElse("None")),
        ("ty",ty.getOrElse("None"))
    );
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      //val copyInfoPairs = Seq((info,geno));
      var headerLine = vcfHeader.formatLines.find{ hh => {
        hh.ID == field
      }}.get
      
      if(removeMeta){
        headerLine = new SVcfCompoundHeaderLine("FORMAT",headerLine.ID,Number=headerLine.Number,Type=headerLine.Type,desc=headerLine.desc)
      }
      
      num match {
        case Some(x) => {
          headerLine = headerLine.updateNumber(x)
        }
        case None => ()
      }
      desc match {
        case Some(x) => {
          headerLine = headerLine.updateDesc(x)
        }
        case None => ()
      }
      ty match {
        case Some(x) => {
          headerLine = headerLine.updateType(x)
        }
        case None => ()
      }
      outHeader.addFormatLine(headerLine);

      val overwriteInfos : Set[String] = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( outHeader.addedInfos );
      if( overwriteInfos.nonEmpty ){
        notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      }
      outHeader.reportAddedInfos(this)
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        v
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
      //vc.dropInfo(overwriteInfos);
    }
  }
  case class FixInfoFieldMetadata( field : String, num : Option[String], desc : Option[String], ty : Option[String], removeMeta : Boolean) extends SVcfWalker {
    def walkerName : String = "FixFormatFieldMetadata"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("field",field),
        ("num",num.getOrElse("None")),
        ("desc",desc.getOrElse("None")),
        ("ty",ty.getOrElse("None"))
    );
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      //val copyInfoPairs = Seq((info,geno));
      var headerLine = vcfHeader.infoLines.find{ hh => {
        hh.ID == field
      }}.get
      if(removeMeta){
        headerLine = new SVcfCompoundHeaderLine("INFO",headerLine.ID,Number=headerLine.Number,Type=headerLine.Type,desc=headerLine.desc)
      }
      num match {
        case Some(x) => {
          headerLine = headerLine.updateNumber(x)
        }
        case None => ()
      }
      desc match {
        case Some(x) => {
          headerLine = headerLine.updateDesc(x)
        }
        case None => ()
      }
      ty match {
        case Some(x) => {
          headerLine = headerLine.updateType(x)
        }
        case None => ()
      }
      outHeader.addInfoLine(headerLine);

      val overwriteInfos : Set[String] = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( outHeader.addedInfos );
      if( overwriteInfos.nonEmpty ){
        notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      }
      outHeader.reportAddedInfos(this)
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        v
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
      //vc.dropInfo(overwriteInfos);
    }
  }
  
  
  case class MergeSamplesIntoSingleColumn(prefixes : Seq[String], sampID : String) extends SVcfWalker {
    def walkerName : String = "MergeSamplesIntoSingleColumn"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("prefixes",  prefixes.mkString("/"))
    );
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      
      val samps = vcfHeader.titleLine.sampleList
      val sampleCt = vcfHeader.titleLine.sampleList.length;
      if( prefixes.length != sampleCt){
        error("MergeSamplesIntoSingleColumn requires a comma-delimited list with length equal to the sample ct");
      }
      var prx = prefixes;
      if( prefixes.head.split(":").length == 2 ){
        val prxdec = prefixes.map{ x => { x.split(":") }}.map{ x => {
          if( x.length != 2){
            error("MergeSamplesIntoSingleColumn attempting to assign prefixes but some prefix entries have colons and some do not");
          }
          (x(0),x(1))
        }}.toMap;
        prx = samps.map{ ss => {
          if( ! prxdec.contains(ss) ){
            error("MergeSamplesIntoSingleColumn attempting to assign prefixes but some names not found in the decoder: \""+ss+"\" (samps in file are: \""+samps.mkString("\",\"")+"\""+") (samps in decoder are: \""+prxdec.keySet.toSeq.mkString("\",\"")+"\")");
          }
          prxdec(ss);
        }}
      }
      val prz = prx.zip(samps);
      
      outHeader.titleLine = SVcfTitleLine( Seq(sampID) );
      outHeader.formatLines = Seq[SVcfCompoundHeaderLine]()
      
      for( fxx <- vcfHeader.formatLines ){
        for( (sxx,sid) <- prz ){
          outHeader.addFormatLine(new SVcfCompoundHeaderLine("FORMAT", fxx.ID+sxx, Number=fxx.Number,Type=fxx.Type,desc="Merged column "+fxx.ID+" for sample "+sid+". "+fxx.desc))
        }
      }

      
      
     //outHeader.addFormatLine(new SVcfCompoundHeaderLine("FORMAT",geno,Number=".",Type="String",desc="Info column copied from "+info+" (todo copy over info)"))
      
      val overwriteInfos : Set[String] = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( outHeader.addedInfos );
      if( overwriteInfos.nonEmpty ){
        notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      }
      outHeader.reportAddedInfos(this)
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine();
        val genotypeVAL = v.genotypes.genotypeValues.map{ gv => {
          gv.toSeq.map{ gg => {
            Array[String](gg);
          }}
        }}.flatten.toArray
        
        val genotypeFMT = v.genotypes.fmt.map{ gx => {
          prz.map{ case (sxx,sid) => {
            gx + sxx;
          }}
        }}.flatten
        
        /*val genotypeVAL = vcfHeader.formatLines.map{ fxx => fxx.ID }.map{ gx => {
           v.genotypes.getGtTag(gx) match {
             case Some(garr) => {
               garr.toSeq.map{ gg => {
                 Array[String](gg);
               }}
             }
             case None => {
               Seq[Array[String]]()
             }
           }
        }}.flatten.toArray
        val genotypeFMT = vcfHeader.formatLines.map{ fxx => fxx.ID }.map{ gx => {
           v.genotypes.getGtTag(gx) match {
             case Some(garr) => {
                prz.map{ case (sxx,sid) => {
                  gx + sxx;
                }}
             }
             case None => {
               Seq[String]()
             }
           }

        }}.flatten*/
        
        if( genotypeFMT.length != genotypeVAL.length ){
          warning("genotype FMT and VAL do not match length: "+genotypeFMT.length+" vs "+genotypeVAL.length,"GENOTYPE_FMT_VAL_NONMATCH_LEN",30);
        }
        
        vc.in_genotypes = SVcfGenotypeSet(
             fmt = genotypeFMT,
             genotypeValues = genotypeVAL
        )
        /*
         SVcfGenotypeSet(var fmt : Seq[String],
                             var genotypeValues : Array[Array[String]])
         */
       
        vc
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
      //vc.dropInfo(overwriteInfos);
    }
  }
  
  class addDistToFeature(file : String, tag : String, desc : String) extends SVcfWalker {
    
    def walkerName : String = "addDistToFeature"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("file", file)
    );
    val features = scala.collection.mutable.AnyRefMap[String,List[Int]]().withDefault( k => List[Int]() );
    
    getLinesSmartUnzip(file).filter{ line => ! line.startsWith("#") }.map{ line => {
      val x = line.split("\t")
      if(x.length != 2){
        error("ERROR: addDistToFeature: file must contain 2 columns, chrom and start!");
      }
      (x(0),string2int(x(1)));
    }}.foreach{ case (chrom,pos) => {
      features(chrom) = features(chrom) :+ pos;
    }}
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",tag,Number="1",Type="Integer",desc=desc+"Distance from nearest feature in file: "+file+")"));
      outHeader.reportAddedInfos(this)
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine();
        val c = v.chrom
        val p = v.start;
        val distList = features(c).map{ featPos => {
          math.abs(p - featPos);
        }}
        if(distList.length > 0){
           vc.addInfo(tag,""+distList.min);
        }
        vc
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
    }
    
  }
  
  
  class addWiggleDepthWalker(wigFile : String, tag : String, desc : String) extends SVcfWalker {
    
    def walkerName : String = "addWiggleDepthWalker"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("wigFile", wigFile)
    );
    
    var wigparser = new internalUtils.genomicAnnoUtils.SimpleEfficientWiggleParser(wigFile);
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",tag,Number="1",Type="Float",desc=desc+"(value from wiggle file:"+wigFile+")"));
      outHeader.reportAddedInfos(this)
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine();
        val c = v.chrom
        val p = v.start;
        vc.addInfo(tag,""+wigparser.getValueAtPos(c,p));
        vc
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
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
      outHeader.reportAddedInfos(this)
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
  
  
  
  
  
  case class HomopolymerRunStats(tagPrefix : String, genomeFa : String, lenThreshold : Int) extends SVcfWalker {
    
    def walkerName : String = "HomopolymerRunStats"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("tagPrefix", tagPrefix)
    );
    
    val refFastaTool = internalUtils.GatkPublicCopy.refFastaTool(genomeFa = genomeFa);
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      
      val tagid = Map[String,String](    ("ADD",internalUtils.VcfTool.TOP_LEVEL_VCF_TAG+tagPrefix+"HRUN_ADD"),
                                         ("DEL",internalUtils.VcfTool.TOP_LEVEL_VCF_TAG+tagPrefix+"HRUN_DEL"),
                                         ("STAT",internalUtils.VcfTool.TOP_LEVEL_VCF_TAG+tagPrefix+"HRUN_STATUS") );
      val len = 2 * lenThreshold
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",tagid("ADD"),Number="1",Type="Integer",desc="Equal to 1 iff the variant adds to an existing homopolymer run of length "+lenThreshold));
      outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",tagid("DEL"),Number="1",Type="Integer",desc="Equal to 1 iff the variant deletes part of an existing homopolymer run of length "+lenThreshold));
      outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",tagid("STAT"),Number="1",Type="String",desc="Homopolymer run warning status (homopolymer runs defined as length >="+lenThreshold+")"));
      outHeader.reportAddedInfos(this)
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        //val vc = v.getOutputLine()
        
        val vc = v.getOutputLine();
        //vc.addInfo(tagPrefix,v.chrom+":"+v.pos+","+v.ref+","+v.alt.mkString(","))
        //val ctrPos = v.pos + (v.ref.length / 2)
        
        val after = refFastaTool.getBasesForIv(chrom = v.chrom,start = v.pos + v.ref.length, end = v.pos + v.ref.length + len-1);
        val before = refFastaTool.getBasesForIv(chrom = v.chrom,start = v.pos - len + 1, end = v.pos-1);
        val maxseq = refFastaTool.getBasesForIv(chrom = v.chrom,start = v.pos + v.ref.length, end = v.pos + v.ref.length + 10);
        
        //for testing, remove later:
        //if(v.ref.last != refFastaTool.getBasesForIv(chrom = v.chrom,start = v.pos + v.ref.length-1, end = v.pos + v.ref.length + 10).head){
        //  warning("    v.pos="+v.pos+", v.ref="+v.ref+", maxseq="+refFastaTool.getBasesForIv(chrom = v.chrom,start = v.pos + v.ref.length-1, end = v.pos + v.ref.length + 10),"TEST10001",1000)
        //}
        val refrunFwd = v.ref.zipWithIndex.find{ case (c,i) => { c != v.ref.head }}.map{_._2}.getOrElse(v.ref.length);
        val refrunRev = v.ref.reverse.zipWithIndex.find{ case (c,i) => {c != v.ref.last}}.map{_._2}.getOrElse(v.ref.length);
        val aftRun = after.zipWithIndex.find{         case (c,i) => { c != v.ref.last }}.map{_._2}.getOrElse(after.length)
        val befRun = before.reverse.zipWithIndex.find{case (c,i) => { c != v.ref.head }}.map{_._2}.getOrElse(after.length)
        var midRun = if(refrunFwd == v.ref.length+1){
          v.ref.length + aftRun + befRun;
        } else {
          0;
        }
          
        if(v.ref.length <= v.alt.head.length){
          if(after.slice(0,lenThreshold).forall(s => s == v.alt.head.last) ){
             vc.addInfo(tagid("ADD"), "1");
             vc.addInfo(tagid("DEL"), "0");
             vc.addInfo(tagid("STAT"), "ADD-RT-"+v.alt.head.last);
          } else if(before.reverse.slice(0,lenThreshold).forall(s => s == v.alt.head.head) ) {
             vc.addInfo(tagid("ADD"), "1");
             vc.addInfo(tagid("DEL"), "0");
             vc.addInfo(tagid("STAT"), "ADD-LT-"+v.alt.head.last);
          } else if(refrunRev + aftRun >= lenThreshold & v.alt.head.last == after.head ){
             vc.addInfo(tagid("ADD"), "1");
             vc.addInfo(tagid("DEL"), "0");
             vc.addInfo(tagid("STAT"), "ADD-RTv2-"+v.alt.head.last);
          } else if(refrunFwd + befRun >= lenThreshold & v.alt.head.head == before.last ){
             vc.addInfo(tagid("ADD"), "1");
             vc.addInfo(tagid("DEL"), "0");
             vc.addInfo(tagid("STAT"), "ADD-LTv2-"+v.alt.head.last);
          } else if(midRun >= lenThreshold && (v.ref.head == v.alt.head.head || v.ref.head == v.alt.head.last) ){
             vc.addInfo(tagid("ADD"), "1");
             vc.addInfo(tagid("DEL"), "0");
             vc.addInfo(tagid("STAT"), "ADD-MID-"+v.ref.head);
          } else {
             /*val nearbyHrun = maxseq.tail.scanLeft((maxseq.head,0)){case ((prev,ct),cc) => {
                if(prev == cc){
                  (prev,ct + 1)
                } else {
                  (cc,0)
                }
             }}.sortBy{case (p,cc) => cc}.last*/
             vc.addInfo(tagid("ADD"), "0");
             vc.addInfo(tagid("DEL"), "0");
             vc.addInfo(tagid("STAT"), ".");
          }
        } else {
          //maxseq.slice(0,5).forall(s => s == v.alt.head.last)
          if( refrunRev + aftRun >= lenThreshold){
             val altrunRev = v.alt.head.reverse.zipWithIndex.find{ case (c,i) => {c != v.ref.last}}.map{_._2}.getOrElse(v.alt.head.length);
             if(altrunRev < refrunRev){
               vc.addInfo(tagid("ADD"), "0");
               vc.addInfo(tagid("DEL"), "1");
               vc.addInfo(tagid("STAT"), "DEL-RT-"+v.ref.last);
             } else {
               vc.addInfo(tagid("ADD"), "0");
               vc.addInfo(tagid("DEL"), "0");
               vc.addInfo(tagid("STAT"), "(NEAR-DEL-RT)");
             }
          } else if(refrunFwd + befRun >= lenThreshold){
             val altrunFwd = v.alt.head.zipWithIndex.find{ case (c,i) => { c != v.ref.head }}.map{_._2}.getOrElse(v.alt.head.length);
             if(altrunFwd < refrunFwd){
               vc.addInfo(tagid("ADD"), "0");
               vc.addInfo(tagid("DEL"), "1");
               vc.addInfo(tagid("STAT"), "DEL-LT-"+v.ref.head);
             } else {
               vc.addInfo(tagid("ADD"), "0");
               vc.addInfo(tagid("DEL"), "0");
               vc.addInfo(tagid("STAT"), "(NEAR-DEL-LT)");
             }
          } else {
             vc.addInfo(tagid("ADD"), "0");
             vc.addInfo(tagid("DEL"), "0");
             vc.addInfo(tagid("STAT"), ".");
          }
        }
        
        
        /*
        windows.foreach{ currWin => {
          val winDiff = maxWin - currWin;
          val currSeq = maxseq.slice(winDiff,maxseq.length - winDiff);
          val nonMissCt = currSeq.count{_ != 'N'}.toDouble
          val missPct = 1 - (nonMissCt / currSeq.length)
          val gcCt = currSeq.count{xx => xx == 'G' || xx == 'C'}.toDouble
          val gcPct = gcCt / nonMissCt;
          
          vc.addInfo(tagPrefix+"_gcPct_"+currWin, fmtString.format(gcPct));
          vc.addInfo(tagPrefix+"_nPct_"+currWin, fmtString.format(missPct));
        }}*/
        
        vc
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
      
    }
  }

  case class AddAltSequence(tagString : Option[String], genomeFa : String, len : Int) extends SVcfWalker {
          val tagid = tagString.getOrElse( internalUtils.VcfTool.TOP_LEVEL_VCF_TAG+"altSeq"+len )

    def walkerName : String = "AddAltSequence"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("tagString", tagid),
        ("len",""+len)
    );
    
    val refFastaTool = internalUtils.GatkPublicCopy.refFastaTool(genomeFa = genomeFa);
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      //internalUtils.VcfTool.TOP_LEVEL_VCF_TAG+tagPrefix+"seqContext"+len
      outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",tagid,Number="1",Type="String",desc="The sequence of the alt allele, with "+len + " flanking bp on each side."));
      outHeader.reportAddedInfos(this)
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        //val vc = v.getOutputLine()
        
        val vc = v.getOutputLine();
        
        val alts = v.alt.filter(p => p != "*");
        //vc.addInfo(tagPrefix,v.chrom+":"+v.pos+","+v.ref+","+v.alt.mkString(","))
        //val ctrPos = v.pos + (v.ref.length / 2)
        val after = refFastaTool.getBasesForIv(chrom = v.chrom,start = v.pos + v.ref.length, end = v.pos + v.ref.length + len-1);
        val before = refFastaTool.getBasesForIv(chrom = v.chrom,start = v.pos - len + 1, end = v.pos-1);
        //vc.addInfo(internalUtils.VcfTool.TOP_LEVEL_VCF_TAG+tagPrefix+"seqContext"+len+"_BEFORE",before);
        //vc.addInfo(internalUtils.VcfTool.TOP_LEVEL_VCF_TAG+tagPrefix+"seqContext"+len+"_AFTER",after);
        //vc.addInfo(internalUtils.VcfTool.TOP_LEVEL_VCF_TAG+tagPrefix+"seqContext"+len+"",before+"["+v.ref+">"+v.alt.mkString("|")+"]"+after);
        //vc.addInfo(tagid,before+v.alt.head+after);
        vc.addInfo(tagid,alts.map{a => { before+a+after }}.mkString(","));
        /*
        windows.foreach{ currWin => {
          val winDiff = maxWin - currWin;
          val currSeq = maxseq.slice(winDiff,maxseq.length - winDiff);
          val nonMissCt = currSeq.count{_ != 'N'}.toDouble
          val missPct = 1 - (nonMissCt / currSeq.length)
          val gcCt = currSeq.count{xx => xx == 'G' || xx == 'C'}.toDouble
          val gcPct = gcCt / nonMissCt;
          
          vc.addInfo(tagPrefix+"_gcPct_"+currWin, fmtString.format(gcPct));
          vc.addInfo(tagPrefix+"_nPct_"+currWin, fmtString.format(missPct));
        }}*/
        
        vc
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
      
    }
  }

  case class AddTrinucleotideComplexity(tagPrefix : String, genomeFa : String, windowSize : Int) extends SVcfWalker {
    
    def walkerName : String = "AddTrinucleotideComplexity"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("tagPrefix", tagPrefix)
    );
    
    val refFastaTool = internalUtils.GatkPublicCopy.refFastaTool(genomeFa = genomeFa);
    
    val len = (windowSize / 3) * 3
    val tnct = len*2 / 3
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      //outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",internalUtils.VcfTool.TOP_LEVEL_VCF_TAG+tagPrefix+"seqContext"+len+"_BEFORE",Number="1",Type="String",desc="The "+len+" ref bases before the variant"));
      //outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",internalUtils.VcfTool.TOP_LEVEL_VCF_TAG+tagPrefix+"seqContext"+len+"_AFTER",Number="1",Type="String",desc="The "+len+" ref bases after the variant"));
      outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",tagPrefix,Number="1",Type="Float",desc="The sum of 64 trinucleotide proportions squared across a "+len+" bp window."));
      outHeader.reportAddedInfos(this)
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine();
        //val after = refFastaTool.getBasesForIv(chrom = v.chrom,start = v.pos, end = v.pos + len-1);
        //val before = refFastaTool.getBasesForIv(chrom = v.chrom,start = v.pos - len + 1, end = v.pos-1);
        val bp = refFastaTool.getBasesForIv(chrom = v.chrom,start = v.pos - len, end = v.pos + len-1)
       // reportln("BP = "+bp,"debug");
        val tn = Range(0,tnct).toList.map{ i => {
          bp.slice(i*3,i*3+3)
        }}
        
        val cts = new scala.collection.mutable.AnyRefMap[String,Int]().withDefault( k => 0 );
        tn.foreach{ trinuc => {
          cts(trinuc) = cts(trinuc) + 1;
        }}
        val complexityScore = cts.map{ case (trinuc,ct) => {
          (ct * ct).toDouble
        }}.sum / 4096
        
        vc.addInfo(tagPrefix,""+complexityScore);
        
        //vc.addInfo(internalUtils.VcfTool.TOP_LEVEL_VCF_TAG+tagPrefix+"seqContext"+len+"_BEFORE",before);
        //vc.addInfo(internalUtils.VcfTool.TOP_LEVEL_VCF_TAG+tagPrefix+"seqContext"+len+"_AFTER",after);
        //vc.addInfo(internalUtils.VcfTool.TOP_LEVEL_VCF_TAG+tagPrefix+"seqContext"+len+"",before+"["+v.ref+">"+v.alt.mkString("|")+"]"+after);
        
        
        vc
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
      
    }
  }
  
  
  case class AddContextBases(tagPrefix : String, genomeFa : String, len : Int) extends SVcfWalker {
    
    def walkerName : String = "AddContextBases"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("tagPrefix", tagPrefix)
    );
    
    val refFastaTool = internalUtils.GatkPublicCopy.refFastaTool(genomeFa = genomeFa);
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",internalUtils.VcfTool.TOP_LEVEL_VCF_TAG+tagPrefix+"seqContext"+len+"_BEFORE",Number="1",Type="String",desc="The "+len+" ref bases before the variant"));
      outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",internalUtils.VcfTool.TOP_LEVEL_VCF_TAG+tagPrefix+"seqContext"+len+"_AFTER",Number="1",Type="String",desc="The "+len+" ref bases after the variant"));
      outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",internalUtils.VcfTool.TOP_LEVEL_VCF_TAG+tagPrefix+"seqContext"+len,Number="1",Type="String",desc="The context around the variant in a window of size "+len+". Note that this uses the ref allele from the VCF. If the REF allele from the VCF is incorrect then this will also be incorrect." ));
      outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",internalUtils.VcfTool.TOP_LEVEL_VCF_TAG+tagPrefix+"seqContext"+len+"_REF",Number="1",Type="String",desc="The sequence of the REF allele with the nearby sequence context of window "+len+". Note that the allele itself will be capitalized. Note that this uses the reference sequence from the genome itself. If the VCF is incorrect then the ref allele will not match the VCF."));
      outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",internalUtils.VcfTool.TOP_LEVEL_VCF_TAG+tagPrefix+"seqContext"+len+"_ALT",Number="1",Type="String",desc="The sequence of the ALT allele with the nearby sequence context of window "+len+". Note that the allele itself will be capitalized."));

      outHeader.reportAddedInfos(this)
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        //val vc = v.getOutputLine()
        
        val vc = v.getOutputLine();
        //vc.addInfo(tagPrefix,v.chrom+":"+v.pos+","+v.ref+","+v.alt.mkString(","))
        //val ctrPos = v.pos + (v.ref.length / 2)
        val after = refFastaTool.getBasesForIv(chrom = v.chrom,start = v.pos + v.ref.length, end = v.pos + v.ref.length + len-1);
        val before = refFastaTool.getBasesForIv(chrom = v.chrom,start = v.pos - len, end = v.pos-1);
        val REF = refFastaTool.getBasesForIv(chrom = v.chrom,start = v.pos, end = v.pos + v.ref.length-1);
        vc.addInfo(internalUtils.VcfTool.TOP_LEVEL_VCF_TAG+tagPrefix+"seqContext"+len+"_BEFORE",before);
        vc.addInfo(internalUtils.VcfTool.TOP_LEVEL_VCF_TAG+tagPrefix+"seqContext"+len+"_AFTER",after);
        vc.addInfo(internalUtils.VcfTool.TOP_LEVEL_VCF_TAG+tagPrefix+"seqContext"+len+"",before+"["+v.ref+">"+v.alt.mkString("|")+"]"+after);
        vc.addInfo(internalUtils.VcfTool.TOP_LEVEL_VCF_TAG+tagPrefix+"seqContext"+len+"_REF",before.toLowerCase()+REF+after.toLowerCase());
        vc.addInfo(internalUtils.VcfTool.TOP_LEVEL_VCF_TAG+tagPrefix+"seqContext"+len+"_ALT",v.alt.filter{ aa => aa != "*" && aa != "." }.map{ aa => { before.toLowerCase()+aa+after.toLowerCase() }}.mkString("|") );

        /*
        windows.foreach{ currWin => {
          val winDiff = maxWin - currWin;
          val currSeq = maxseq.slice(winDiff,maxseq.length - winDiff);
          val nonMissCt = currSeq.count{_ != 'N'}.toDouble
          val missPct = 1 - (nonMissCt / currSeq.length)
          val gcCt = currSeq.count{xx => xx == 'G' || xx == 'C'}.toDouble
          val gcPct = gcCt / nonMissCt;
          
          vc.addInfo(tagPrefix+"_gcPct_"+currWin, fmtString.format(gcPct));
          vc.addInfo(tagPrefix+"_nPct_"+currWin, fmtString.format(missPct));
        }}*/
        
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
      outHeader.reportAddedInfos(this)
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
      outHeader.reportAddedInfos(this)
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

  
  class FilterInvalidAlleleLines() extends SVcfWalker {
    def walkerName : String = "FilterInvalidAlleleLines"
    
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)]();
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader;
      outHeader.addWalk(this);
      var dropct = 0;
      outHeader.reportAddedInfos(this)
      
      val validRefChecker : java.util.regex.Pattern = java.util.regex.Pattern.compile("[ACTGNactgn]+")
      val validAltChecker : java.util.regex.Pattern = java.util.regex.Pattern.compile("[ACTGNactgn]+?")

      (addIteratorCloseAction( iter = vcFlatMap(vcIter){v => {
        val refValid = ( v.ref.startsWith("<") && v.ref.endsWith(">") ) || 
                       ( validRefChecker.matcher(v.ref).matches() )
        val altValid = ( v.alt.length == 1 && v.alt.head == ".") || ( v.alt.forall{ a => {
          a == "*" || (a.startsWith("<") && a.endsWith(">")) || validRefChecker.matcher(a).matches()
        }} )
        if( ( ! refValid ) || ( ! altValid ) ){
          dropct = dropct + 1;
          notice("Dropped invalid allele variant:\n    "+v.getSimpleVcfString(),"Dropped_Invalid_Variant",10);
          None          
        } else {
          Some(v);
        }
      }}, closeAction = (() => {
        reportln("Dropped "+dropct+" variant/allele lines due to the presence of invalid alleles","note");
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
      outHeader.reportAddedInfos(this)
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
  
  /*

    val params = paramString.split(",");
    val tagID = params(0);
    def walkerName : String = "GenerateBurdenTable."+tagID;
    val geneTag = params(1);
    //val outfile = params(2);
    val filterExpressionString = params.find( pp => pp.startsWith("keepVariantsExpression=")).map{pp => pp.drop( "keepVariantsExpression=".length )}.getOrElse("TRUE");
    val sampSubset = params.find( pp => pp.startsWith("samples=")).map{pp => pp.drop( "samples=".length ).split("[|]").toSet};
    val sampGroup  = params.find( pp => pp.startsWith("group=")).map{pp => pp.drop( "group=".length )}

    val gtTag = params.find( pp => pp.startsWith("gtTag=")).map{pp => pp.drop( "gtTag=".length )}.getOrElse("GT");

    val filterExpr : SFilterLogic[SVcfVariantLine] = internalUtils.VcfTool.sVcfFilterLogicParser.parseString( filterExpressionString )



   */
  
 // class generateBurdenMatrix(paramString : String, out : WriterUtil, 
 //                            groupFile : Option[String], groupList : Option[String], superGroupList  : Option[String]) extends internalUtils.VcfTool.SVcfWalker { 

  
  /*
   *             val (sampleToGroupMap,groupToSampleMap,groups) : (scala.collection.mutable.AnyRefMap[String,Set[String]],
                           scala.collection.mutable.AnyRefMap[String,Set[String]],
                           Vector[String]) = getGroups(groupFile, None, superGroupList);
   * 
   */

     class calcBurdenCountsSetWalker( tagID : String, out : WriterUtil,
                                   geneTag : String,
                                   filterExpressionString : String,
                                   sampGroups :  Seq[String], 
                                   gtTag : String,
                                   sampleToGroupMap : scala.collection.mutable.AnyRefMap[String,Set[String]],
                                   groupToSampleMap : scala.collection.mutable.AnyRefMap[String,Set[String]],
                                   groups : Vector[String]) extends internalUtils.VcfTool.SVcfWalker {

    def walkerName : String = "GenerateBurdenTable."+tagID;
    val filterExpr : SFilterLogic[SVcfVariantLine] = internalUtils.VcfTool.sVcfFilterLogicParser.parseString( filterExpressionString )

    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("tagID",tagID),
        ("filterExpressionString",filterExpressionString),
        ("gtTag",gtTag)
    );
    reportln("burden counter initialized: "+tagID,"note");
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      var errCt = 0;
       checkSVcfFilterLogicParse( filterLogic = filterExpr, vcfHeader = vcfHeader );

      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      //outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",newTag,Number="1",Type="Float",desc=desc.getOrElse("Simple ratio between fields "+nTag+" and "+dTag+".")));
      //val overwriteInfos : Set[String] = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( outHeader.addedInfos );
      //if( overwriteInfos.nonEmpty ){
      //  notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      //}
      //defaultEntry = ( (s : String) => new Array[Int]( outHeader.titleLine.sampleList.length ) ) 
      val burdenMatrix = new scala.collection.mutable.AnyRefMap[String,Array[Int]]()
      val varCounts = new scala.collection.mutable.AnyRefMap[String,Int](defaultEntry = ( (s : String) => 0 ));
      val altCounts = new scala.collection.mutable.AnyRefMap[String,Int](defaultEntry = ( (s : String) => 0 ));
      val fullSampList =  vcfHeader.titleLine.sampleList
      outHeader.reportAddedInfos(this)
      
      reportln("burden counter setup complete: "+tagID,"note");
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine();
        if(filterExpr.keep(v)){
          val geneList = v.info.get(geneTag).getOrElse(None).map{a => a.split(",")}.getOrElse( new Array[String](0) );
          //geneList.foreach{ g => {
            val cts = geneList.map{ g => {
              burdenMatrix.getOrElseUpdate(g,
                 new Array[Int]( outHeader.titleLine.sampleList.length )
              )
            }}
            notice("genelist found: \""+geneList.mkString("/")+"\"","GENELIST_FOUND",25);
            var numAlt = 0;
            v.genotypes.getGtTag(gtTag).map{ gta => {
              gta.indices.foreach{ sampIdx => {
                val gt = gta(sampIdx);
                //notice("ALT GT: samp="+sampIdx+", gt=\""+gt+"\", geneList="+geneList.mkString("/"),"GT_FOUND",5000);
                if( gt.split("[|/]").contains("1")){
                  //notice("ALT GT FOUND: samp="+sampIdx+", gt=\""+gt+"\", geneList="+geneList.mkString("/"),"ALT_GT_FOUND",10);
                  numAlt = numAlt + 1;
                  cts.map{ ctsCurr => {
                    ctsCurr(sampIdx) = ctsCurr(sampIdx) + 1;
                  }}
                  geneList.foreach{g => {
                    val altCt =  altCounts.getOrElse(g,0);
                    altCounts.update(g,altCt + 1);
                  }}
                }
              }}
            }}
            if(numAlt > 0){
                  geneList.foreach{g => {
                    val altCt =  varCounts.getOrElse(g,0);
                    varCounts.update(g,altCt + 1);
                  }}
            }
        }
        vc
      }}, closeAction = (() => {
        //val out = openWriter(outfile);
        
        val groupSampSet = sampGroups.map{ g =>{
          groupToSampleMap.getOrElse(g, {
            error("sample group "+g+" not found!")
            Set[String]();
          });
        }}
        
        groupSampSet.zip(sampGroups).foreach{ case (gss,grp) => {
          out.write("#counts\t"+tagID+"/"+grp+"gtTag="+gtTag+"\t"+"filter="+filterExpressionString+"\t"+"grp="+grp+"\n");
          reportln("#counts\t"+tagID+"/"+grp+"gtTag="+gtTag+"\t"+"filter="+filterExpressionString+"\t"+"grp="+grp,"note")
          burdenMatrix.keys.toVector.sorted.foreach{ g => {
            val mtr = burdenMatrix(g).zip(fullSampList).filter{ case (ct,ss) => gss.contains(ss) }.map{ case (ct,ss) => ct}
            val altCt = mtr.sum
            val varCt = varCounts(g);
            out.write(tagID +"/"+grp+ "\t" + g+"\t"+mtr.count( pp => pp > 0)+"\t"+altCt+"\t"+varCt+"\t"+mtr.count( pp => pp > 1)+"\n");
          }}
        }}
        out.flush();

        //out.close();
        //burdenMatrix.keys.toVector.sorted
      })),outHeader)
      
    }
  }
  
     class calcBurdenCountsWalker( tagID : String, out : WriterUtil,
                                   geneTag : String,
                                   filterExpressionString : String,
                                   sampSubset : Option[Set[String]],
                                   sampGroup :  Option[String], gtTag : String,
                                   sampleToGroupMap : scala.collection.mutable.AnyRefMap[String,Set[String]],
                                   groupToSampleMap : scala.collection.mutable.AnyRefMap[String,Set[String]],
                                   groups : Vector[String]) extends internalUtils.VcfTool.SVcfWalker { 
    /*
    val params = paramString.split(",");
    val tagID = params(0);
    def walkerName : String = "GenerateBurdenTable."+tagID;
    val geneTag = params(1);
    //val outfile = params(2);
    val filterExpressionString = params.find( pp => pp.startsWith("keepVariantsExpression=")).map{pp => pp.drop( "keepVariantsExpression=".length )}.getOrElse("TRUE");
    val sampSubset = params.find( pp => pp.startsWith("samples=")).map{pp => pp.drop( "samples=".length ).split("[|]").toSet};
    val sampGroup  = params.find( pp => pp.startsWith("group=")).map{pp => pp.drop( "group=".length )}

    val gtTag = params.find( pp => pp.startsWith("gtTag=")).map{pp => pp.drop( "gtTag=".length )}.getOrElse("GT");

    val filterExpr : SFilterLogic[SVcfVariantLine] = internalUtils.VcfTool.sVcfFilterLogicParser.parseString( filterExpressionString )
     */
    def walkerName : String = "GenerateBurdenTable."+tagID;
    val filterExpr : SFilterLogic[SVcfVariantLine] = internalUtils.VcfTool.sVcfFilterLogicParser.parseString( filterExpressionString )

    
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("tagID",tagID),
        ("filterExpressionString",filterExpressionString),
        ("gtTag",gtTag)
    );
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      var errCt = 0;
       checkSVcfFilterLogicParse( filterLogic = filterExpr, vcfHeader = vcfHeader );

      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      //outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",newTag,Number="1",Type="Float",desc=desc.getOrElse("Simple ratio between fields "+nTag+" and "+dTag+".")));
      //val overwriteInfos : Set[String] = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( outHeader.addedInfos );
      //if( overwriteInfos.nonEmpty ){
      //  notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      //}
      //defaultEntry = ( (s : String) => new Array[Int]( outHeader.titleLine.sampleList.length ) ) 
      val burdenMatrix = new scala.collection.mutable.AnyRefMap[String,Array[Int]]()
      val varCounts = new scala.collection.mutable.AnyRefMap[String,Int](defaultEntry = ( (s : String) => 0 ));
      val altCounts = new scala.collection.mutable.AnyRefMap[String,Int](defaultEntry = ( (s : String) => 0 ));
      val fullSampList =  vcfHeader.titleLine.sampleList
      val keepSampSet = sampSubset.getOrElse(fullSampList.toSet);
      val groupSampSet = sampGroup.map{ g =>{
        groupToSampleMap.getOrElse(g, {
          error("sample group "+g+" not found!")
          Set[String]();
        });
      }}
      
      val finalKeepSampSet = groupSampSet.map{ gss => {
        keepSampSet.filter( ss => { gss.contains(ss) })
      }}.getOrElse( keepSampSet );
      val sampIdxList = fullSampList.zipWithIndex.filter{ case (samp,idx)=> { finalKeepSampSet.contains(samp) }}.map{ case (samp,idx) => idx };

      notice("BTcount: parsed sample list (sampSubset: "+sampSubset.map(_.size).getOrElse(-1)+"), (keepSampSet: "+keepSampSet.size+"), (finalKeepSampSet: "+finalKeepSampSet.size+"), (sampIdxList: "+sampIdxList.length+") first 10 sample idx: ["+sampIdxList.slice(0,10).mkString("/")+"]","sampIdxListFound",10);
      outHeader.reportAddedInfos(this)
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine();
        if(filterExpr.keep(v)){
          val geneList = v.info.get(geneTag).getOrElse(None).map{a => a.split(",")}.getOrElse( new Array[String](0) );
          //geneList.foreach{ g => {
            val cts = geneList.map{ g => {
              burdenMatrix.getOrElseUpdate(g,
                 new Array[Int]( outHeader.titleLine.sampleList.length )
              )
            }}
            notice("genelist found: \""+geneList.mkString("/")+"\"","GENELIST_FOUND",25);
            var numAlt = 0;
            v.genotypes.getGtTag(gtTag).map{ gta => {
              sampIdxList.foreach{ sampIdx => {
                val gt = gta(sampIdx);
                //notice("ALT GT: samp="+sampIdx+", gt=\""+gt+"\", geneList="+geneList.mkString("/"),"GT_FOUND",5000);
                if( gt.split("[|/]").contains("1")){
                  //notice("ALT GT FOUND: samp="+sampIdx+", gt=\""+gt+"\", geneList="+geneList.mkString("/"),"ALT_GT_FOUND",10);
                  numAlt = numAlt + 1;
                  cts.map{ ctsCurr => {
                    ctsCurr(sampIdx) = ctsCurr(sampIdx) + 1;
                  }}
                  geneList.foreach{g => {
                    val altCt =  altCounts.getOrElse(g,0);
                    altCounts.update(g,altCt + 1);
                  }}
                }
              }}
              
            }}
            if(numAlt > 0){
                  geneList.foreach{g => {
                    val altCt =  varCounts.getOrElse(g,0);
                    varCounts.update(g,altCt + 1);
                  }}
            }
             
          //}}
        }
        vc
      }}, closeAction = (() => {
        //val out = openWriter(outfile);
        out.write("#counts\t"+tagID+"gtTag="+gtTag+"\t"+"filter="+filterExpressionString+"\t"+"sampCt="+sampIdxList.length+"\n");
        reportln("#counts\t"+tagID+"gtTag="+gtTag+"\t"+"filter="+filterExpressionString+"\t"+"sampCt="+sampIdxList.length,"note")
        burdenMatrix.keys.toVector.sorted.foreach{ g => {
          val mtr = burdenMatrix(g);
          val altCt = altCounts(g);
          val varCt = varCounts(g);
          out.write(tagID + "\t" + g+"\t"+mtr.count( pp => pp > 0)+"\t"+altCt+"\t"+varCt+"\t"+mtr.count( pp => pp > 1)+"\n");
        }}
        
        //out.close();
        //burdenMatrix.keys.toVector.sorted
      })),outHeader)
      
    }
  }


     class calcBurdenMatrixWalker( tagID : String, outfile : String,
                                   geneTag : String,
                                   filterExpressionString : String,
                                   sampSubset : Option[Set[String]],
                                   sampGroup :  Option[String], gtTag : String,
                                   sampleToGroupMap : scala.collection.mutable.AnyRefMap[String,Set[String]],
                                   groupToSampleMap : scala.collection.mutable.AnyRefMap[String,Set[String]],
                                   groups : Vector[String],
                                   geneList : Option[Seq[String]], 
                                   pathwayList : Seq[String],
                                   printFullGeneList : Boolean
     ) extends internalUtils.VcfTool.SVcfWalker {

    def walkerName : String = "GenerateBurdenMatrix."+tagID;

    val filterExpr : SFilterLogic[SVcfVariantLine] = internalUtils.VcfTool.sVcfFilterLogicParser.parseString( filterExpressionString )

    
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("tagID",tagID),
        ("filterExpressionString",filterExpressionString),
        ("gtTag",gtTag)
    );
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      var errCt = 0;
       checkSVcfFilterLogicParse( filterLogic = filterExpr, vcfHeader = vcfHeader );

      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      //outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",newTag,Number="1",Type="Float",desc=desc.getOrElse("Simple ratio between fields "+nTag+" and "+dTag+".")));
      //val overwriteInfos : Set[String] = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( outHeader.addedInfos );
      //if( overwriteInfos.nonEmpty ){
      //  notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      //}
      //defaultEntry = ( (s : String) => new Array[Int]( outHeader.titleLine.sampleList.length ) ) 
      val burdenMatrix = new scala.collection.mutable.AnyRefMap[String,Array[Int]]()
      val varCounts = new scala.collection.mutable.AnyRefMap[String,Int](defaultEntry = ( (s : String) => 0 ));
      val altCounts = new scala.collection.mutable.AnyRefMap[String,Int](defaultEntry = ( (s : String) => 0 ));
      val fullSampList =  vcfHeader.titleLine.sampleList
      val keepSampSet = sampSubset.getOrElse(fullSampList.toSet);
      val groupSampSet = sampGroup.map{ g =>{
        groupToSampleMap.getOrElse(g, {
          error("sample group "+g+" not found!")
          Set[String]();
        });
      }}
      
      val finalKeepSampSet = groupSampSet.map{ gss => {
        keepSampSet.filter( ss => { gss.contains(ss) })
      }}.getOrElse( keepSampSet );
      val sampIdxList = fullSampList.zipWithIndex.filter{ case (samp,idx)=> { finalKeepSampSet.contains(samp) }}.map{ case (samp,idx) => idx };
      val sampct = sampIdxList.length

      notice("BTcount: parsed sample list (sampSubset: "+sampSubset.map(_.size).getOrElse(-1)+"), (keepSampSet: "+keepSampSet.size+"), (finalKeepSampSet: "+finalKeepSampSet.size+"), (sampIdxList: "+sampIdxList.length+") first 10 sample idx: ["+sampIdxList.slice(0,10).mkString("/")+"]","sampIdxListFound",10);
      outHeader.reportAddedInfos(this)
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine();
        if(filterExpr.keep(v)){
          val geneList = v.info.get(geneTag).getOrElse(None).map{a => a.split(",")}.getOrElse( new Array[String](0) );
          //geneList.foreach{ g => {
            val cts = geneList.map{ g => {
              burdenMatrix.getOrElseUpdate(g,
                 new Array[Int]( outHeader.titleLine.sampleList.length )
              )
            }}
            notice("genelist found: \""+geneList.mkString("/")+"\"","GENELIST_FOUND",25);
            var numAlt = 0;
            v.genotypes.getGtTag(gtTag).map{ gta => {
              sampIdxList.foreach{ sampIdx => {
                val gt = gta(sampIdx);
                //notice("ALT GT: samp="+sampIdx+", gt=\""+gt+"\", geneList="+geneList.mkString("/"),"GT_FOUND",5000);
                if( gt.split("[|/]").contains("1")){
                  //notice("ALT GT FOUND: samp="+sampIdx+", gt=\""+gt+"\", geneList="+geneList.mkString("/"),"ALT_GT_FOUND",10);
                  numAlt = numAlt + 1;
                  cts.map{ ctsCurr => {
                    ctsCurr(sampIdx) = ctsCurr(sampIdx) + 1;
                  }}
                  geneList.foreach{g => {
                    val altCt =  altCounts.getOrElse(g,0);
                    altCounts.update(g,altCt + 1);
                  }}
                }
              }}
              
            }}
            if(numAlt > 0){
                  geneList.foreach{g => {
                    val altCt =  varCounts.getOrElse(g,0);
                    varCounts.update(g,altCt + 1);
                  }}
            }
             
          //}}
        }
        vc
      }}, closeAction = (() => {
        //val out = openWriter(outfile);
        //out.write("#counts\t"+tagID+"gtTag="+gtTag+"\t"+"filter="+filterExpressionString+"\t"+"sampCt="+sampIdxList.length+"\n");
        //reportln("#counts\t"+tagID+"gtTag="+gtTag+"\t"+"filter="+filterExpressionString+"\t"+"sampCt="+sampIdxList.length,"note")
        val tagIdPrefix = if(tagID == ""){
          ""
        } else {
          tagID + "\t"
        }
        val out = openWriterSmart(outfile);
        out.write(tagIdPrefix+"geneID\t"+outHeader.titleLine.sampleList.zipWithIndex.filter{ case (g,i) => sampIdxList.contains(i) }.map{ case (g,i) => g}.mkString("\t")+"\n");
        
        if(printFullGeneList){
          geneList.get.foreach{ g => {
            val mtr = burdenMatrix.getOrElse(g,new Array[Int](outHeader.titleLine.sampleList.length)).zipWithIndex.filter{ case (g,i) => sampIdxList.contains(i) }.map{ case (g,i) => g}
            out.write( tagIdPrefix + g+"\t"+mtr.mkString("\t") +"\n" );
          }}
        } else {
        burdenMatrix.keys.toVector.sorted.foreach{ g => {
            val mtr = burdenMatrix(g).zipWithIndex.filter{ case (g,i) => sampIdxList.contains(i) }.map{ case (g,i) => g}
            //val altCt = altCounts(g);
            //val varCt = varCounts(g);
            if(geneList.isEmpty || geneList.get.contains(g)){
              out.write( tagIdPrefix + g+"\t"+mtr.mkString("\t") +"\n" );
            }
          }}
        }

        
        pathwayList.map{ pwString => {
          val pwCells = pwString.trim().split(":",2)
          val id      = pwCells.head.trim();
          val genes   = pwCells(1).split("[,]").map{ g => g.trim() }.toSet;
          val mtr = burdenMatrix.filter{ case (g,bm) => {
            genes.contains(g);
          }}.foldLeft(new Array[Int]( outHeader.titleLine.sampleList.length )  ){ case (soFar,(g,bm)) => {
            soFar.zip(bm).map{ case (a,b) => {
              a+b
            }}
          }}.zipWithIndex.filter{ case (g,i) => sampIdxList.contains(i) }.map{ case (g,i) => g}
          out.write( tagIdPrefix + id+"\t"+mtr.mkString("\t") +"\n" );
        }}
        
        out.close();
        //burdenMatrix.keys.toVector.sorted
      })),outHeader)
      
    }
  }


  
  case class VcfTagFunctionParam( id : String, 
                                  ty : String, 
                                  req: Boolean = true, 
                                  defval : String = "", 
                                  dotdot : Boolean = false ,
                                  num : String = "."){
      
    
    val tys = ty.split("[|]")
    val TYS = tys.map{ tt => tt.toUpperCase() }

    
    def getFinalInputType(param : String, h : SVcfHeader) : (String,String) = {
      def checkIsInt(pp : String) : Boolean = {
        string2intOpt(pp).nonEmpty;
      }
      def checkIsFloat(pp : String) : Boolean = {
        string2doubleOpt(pp).nonEmpty;
      }
      def checkIsInfo(pp : String) : Option[SVcfCompoundHeaderLine] = {
          h.infoLines.find{ ln => {
            ln.ID == pp
          }}
      }
      def checkIsGeno(pp : String) : Option[SVcfCompoundHeaderLine] = {
          h.formatLines.find{ ln => {
            ln.ID == pp
          }}
      }
      if(param.toUpperCase().startsWith("INFO:")){
        if( ! TYS.exists{ ss => ss.startsWith("INFO_")} ){
          error("Fatal Error: param "+id+" is not permitted to be an INFO field! It must be one of these types: "+ty);
        }
        checkIsInfo( param.substring(5) ).map{ ln => {
          if(ln.Type == "String"){
            if(TYS.contains("INFO_STRING")){
              ("INFO","String")
            } else if(TYS.contains("INFO_FLOAT")){
              warning("Attempting to coerce String INFO field "+ param.substring(5)+" into a float.","COERCE_INFO_STRING_TO_FLOAT",-1)
              ("INFO","Float")
            } else if(TYS.contains("INFO_INT")){
              ("INFO","Integer")
            } else {
              ("INFO","?")
            }
          } else if(ln.Type == "Float"){
            if(TYS.contains("INFO_FLOAT")){
              ("INFO","Float")
            } else if(TYS.contains("INFO_STRING")){
              ("INFO","String")
            } else if(TYS.contains("INFO_INT")){
              warning("Attempting to coerce Float INFO field "+ param.substring(5)+" into an INT.","COERCE_INFO_FLOAT_TO_INT",-1)
              ("INFO","Integer")
            } else {
              ("INFO","?")
            }
          } else if(ln.Type == "Integer"){
            if(TYS.contains("INFO_INT")){
              ("INFO","Integer")
            } else if(TYS.contains("INFO_FLOAT")){
              ("INFO","Float")
            } else if(TYS.contains("INFO_STRING")){
              ("INFO","String")
            } else {
              ("INFO","?")
            }
          } else {
            ("?","?")
          }
        }}.getOrElse( ("ERROR","String") )
        //if( TYS.contains("INFO_FLOAT") && 
      } else if(param.toUpperCase().startsWith("GENO:")){
        if( ! TYS.exists{ ss => ss.startsWith("GENO_")} ){
          error("Fatal Error: param "+id+" is not permitted to be a GENO field! It must be one of these types: "+ty);
        }
        checkIsGeno( param.substring(5) ).map{ ln => {
          if(ln.Type == "String"){
            if(TYS.contains("GENO_STRING")){
              ("GENO","String")
            } else if(TYS.contains("GENO_FLOAT")){
              warning("Attempting to coerce String GENO field "+ param.substring(5)+" into a float.","COERCE_GENO_STRING_TO_FLOAT",-1)
              ("GENO","Float")
            } else if(TYS.contains("GENO_INT")){
              ("GENO","Integer")
            } else {
              ("GENO","?")
            }
          } else if(ln.Type == "Float"){
            if(TYS.contains("GENO_FLOAT")){
              ("GENO","Float")
            } else if(TYS.contains("GENO_STRING")){
              ("GENO","String")
            } else if(TYS.contains("GENO_INT")){
              warning("Attempting to coerce Float GENO field "+ param.substring(5)+" into an INT.","COERCE_GENO_FLOAT_TO_INT",-1)
              ("GENO","Integer")
            } else {
              ("GENO","?")
            }
          } else if(ln.Type == "Integer"){
            if(TYS.contains("GENO_INT")){
              ("GENO","Integer")
            } else if(TYS.contains("GENO_FLOAT")){
              ("GENO","Float")
            } else if(TYS.contains("GENO_STRING")){
              ("GENO","String")
            } else {
              ("GENO","?")
            }
          } else {
            ("?","?")
          }
        }}.getOrElse( ("ERROR","String") )
      } else if(checkIsInt(param)){
        ("CONST","Int")
      } else if(checkIsFloat(param)){
        ("CONST","Float")
      } else {
        ("CONST","String")
      }
    }
    
    def checkParamType( param : String, h : SVcfHeader) : Boolean = {
      getFinalInputType(param,h)._2 != "?"
    }
  }
  
  
  
  case class TFParamParser[T]( param : String, reader : VcfTagFunctionParamReader[T] ){
    def get(v : SVcfVariantLine) : Option[T] = reader.get(v);
  }

  abstract class VcfTagFunctionParamReader[T](){
    def get(v : SVcfVariantLine) : Option[T]
  }
  case class VcfTagFunctionParamReader_Int(param : String, inputType : String) extends VcfTagFunctionParamReader[Int] {
    val x = if(inputType == "CONST"){
      string2int(param);
    } else {
      -1
    }
    def get(v : SVcfVariantLine) : Option[Int] = if(inputType == "CONST"){
      Some(x);
    } else if(inputType == "INFO"){
      v.info.getOrElse( param, None).map{ z => {
        string2int(z)
      }}
    } else {
      None
    }
  }
  case class VcfTagFunctionParamReader_Float(param : String, inputType : String) extends VcfTagFunctionParamReader[Float] {
    val x : Float = if(inputType == "CONST"){
      string2float(param);
    } else {
      string2float("-1")
    }
    def get(v : SVcfVariantLine) : Option[Float] = if(inputType == "CONST"){
      Some(x);
    } else if(inputType == "INFO"){
      v.info.getOrElse( param, None).map{ z => {
        string2float(z)
      }}
    } else {
      None
    }
  }
  case class VcfTagFunctionParamReader_String(param : String, inputType : String) extends VcfTagFunctionParamReader[String] {
    val x : String = if(inputType == "CONST"){
      param;
    } else {
      "?"
    }
    def get(v : SVcfVariantLine) : Option[String] = if(inputType == "CONST"){
      Some(x);
    } else if(inputType == "INFO"){
      v.info.getOrElse( param, None)
    } else {
      None
    }
  }
  object VcfTagFun {
    
  }
  
  case class VcfTagFun(md : VcfTagFcnMetadata,
                       h : SVcfHeader,
                       pv : Seq[String],
                       dgts : Option[Int]) {
    
    
    
  }
  
  abstract class VcfTagFcn() {
    def md : VcfTagFcnMetadata;
    def h : SVcfHeader;
    def pv : Seq[String];
    def dgts : Option[Int];
    def init() : Boolean;
    def run(vc : SVcfOutputVariantLine);
    
      def checkIsInt(pp : String) : Boolean = {
        string2intOpt(pp).nonEmpty;
      }
      def checkIsFloat(pp : String) : Boolean = {
        string2doubleOpt(pp).nonEmpty;
      }
      def checkIsInfo(pp : String) : Boolean = {
          h.infoLines.exists{ ln => {
            ln.ID == pp
          }}
      }
      def checkIsGeno(pp : String) : Boolean = {
          h.formatLines.exists{ ln => {
            ln.ID == pp
          }}
      }
      def checkVcfTagParams() : Boolean = {
          md.params.padTo( pv.length, md.params.last ).zip(pv).exists{ case (vtfp, pvv) => {
              ! vtfp.checkParamType(pvv, h);
          }}
      }
      
      def writeDouble(vc : SVcfOutputVariantLine, tag : String,dd : Double){
              dgts match {
                case Some(dd) => {
                  vc.addInfo(tag, ("%."+dd+"f").format( dd ) );
                }
                case None => {
                  vc.addInfo(tag, ""+(dd));
                }
              }
      }
      def writeInt(vc : SVcfOutputVariantLine, tag : String,dd : Int){
                vc.addInfo(tag, ""+(dd));
      }
      def writeString(vc : SVcfOutputVariantLine, tag : String,dd : String){
                vc.addInfo(tag, ""+(dd));
      }
  }
  case class VcfTagFcnMetadata( id : String, synon : Seq[String],
                              shortDesc : String,
                              desc : String,
                              params : Seq[VcfTagFunctionParam]){
     def getID : String = id;
     def getShort : String = shortDesc;
     def getDesc : String = desc;
     def getParams :  Seq[VcfTagFunctionParam] = params;
  }
  
  abstract class VcfTagFcnFactory(){
     def metadata : VcfTagFcnMetadata;
     def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn;
  }
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  val vcfTagFunctionMap : Seq[VcfTagFcnFactory] = Seq[VcfTagFcnFactory](
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "SUM",synon = Seq(),
              shortDesc = "",
              desc = "",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INT|FLOAT|INFO_Int|INFO_Float",req=true,dotdot=true )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader;
              def pv : Seq[String] = paramValues;
              def dgts : Option[Int] = digits;
              def md : VcfTagFcnMetadata = mmd;
              var isInteger : Boolean = true;
              var const : Either[Int,Double] = Right[Int,Double](0);
              var infoParams : Seq[String] = Seq[String]();
              def init() : Boolean = {
                isInteger = pv.exists{ pp => {
                  h.infoLines.find{ info => { info.ID == pp }}.map{ info => { info.Type != "Integer" }}.getOrElse( ! string2intOpt(pp).isDefined )
                }}
                pv.foreach{ pp => {
                  h.infoLines.find{ info => { info.ID == pp }}.foreach{ info => { 
                    if(info.Number != "1") warning("Warning: running SUM function on tag: "+info.ID+", which is a list. Will sum across all elements in each list. Is this what you intended?","NUMERIC_FCN_ON_LIST",100);
                  }}
                }}
                pv.foreach{ pp => {
                  h.infoLines.find{ info => { info.ID == pp }}.foreach{ info => { 
                    if(info.Type != "Integer" && info.Type != "Float") warning("Warning: running SUM function on tag: "+info.ID+", which is of type "+info.Type+". Will attempt to coerce values to a float or a list of floats. Is this what you intended?","NUMERIC_FCN_ON_LIST",100);
                  }}
                }}
                infoParams = pv.filter{pp => h.infoLines.exists(hh => hh.ID == pp ) }
                if(isInteger){
                  const = Left[Int,Double]( pv.filter{pp => ! h.infoLines.exists(hh => hh.ID == pp ) }.map{ string2int(_) }.sum )
                } else {
                  const = Right[Int,Double]( pv.filter{pp => ! h.infoLines.exists(hh => hh.ID == pp ) }.map{ string2double(_) }.sum )
                }
                checkVcfTagParams();
              }
              def run(vc : SVcfOutputVariantLine){
                const match {
                  case Left(k) => {
                    val out = k + infoParams.flatMap{ tag => vc.info.getOrElse(tag,None).toSeq.flatMap{_.split(",")}}.filter( vv => vv != "." ).map{vv => string2int(vv)}.sum
                    writeInt(vc,newTag,out);
                  }
                  case Right(k) => {
                    val out = k + infoParams.flatMap{ tag => vc.info.getOrElse(tag,None).toSeq.flatMap{_.split(",")}}.filter( vv => vv != "." ).map{vv => string2double(vv)}.sum
                    writeDouble(vc,newTag,out);
                  }
                }
              }
            }
          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "PROD",synon = Seq(),
              shortDesc = "",
              desc = "",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INT|FLOAT|INFO_Int|INFO_Float",req=true,dotdot=true )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader;
              def pv : Seq[String] = paramValues;
              def dgts : Option[Int] = digits;
              def md : VcfTagFcnMetadata = mmd;
              var isInteger : Boolean = true;
              var const : Either[Int,Double] = Right[Int,Double](0);
              var infoParams : Seq[String] = Seq[String]();
              def init() : Boolean = {
                isInteger = pv.exists{ pp => {
                  h.infoLines.find{ info => { info.ID == pp }}.map{ info => { info.Type != "Integer" }}.getOrElse( ! string2intOpt(pp).isDefined )
                }}
                pv.foreach{ pp => {
                  h.infoLines.find{ info => { info.ID == pp }}.foreach{ info => { 
                    if(info.Number != "1") warning("Warning: running "+md.id+" function on tag: "+info.ID+", which is a list. Will sum across all elements in each list. Is this what you intended?","NUMERIC_FCN_ON_LIST",100);
                  }}
                }}
                pv.foreach{ pp => {
                  h.infoLines.find{ info => { info.ID == pp }}.foreach{ info => { 
                    if(info.Type != "Integer" && info.Type != "Float") warning("Warning: running SUM function on tag: "+info.ID+", which is of type "+info.Type+". Will attempt to coerce values to a float or a list of floats. Is this what you intended?","NUMERIC_FCN_ON_LIST",100);
                  }}
                }}
                infoParams = pv.filter{pp => h.infoLines.exists(hh => hh.ID == pp ) }
                if(isInteger){
                  const = Left[Int,Double]( pv.filter{pp => ! h.infoLines.exists(hh => hh.ID == pp ) }.map{ string2int(_) }.product )
                } else {
                  const = Right[Int,Double]( pv.filter{pp => ! h.infoLines.exists(hh => hh.ID == pp ) }.map{ string2double(_) }.product )
                }
                checkVcfTagParams();
              }
              def run(vc : SVcfOutputVariantLine){
                const match {
                  case Left(k) => {
                    val out = k + infoParams.flatMap{ tag => vc.info.getOrElse(tag,None).toSeq.flatMap{_.split(",")}}.filter( vv => vv != "." ).map{vv => string2int(vv)}.product
                    writeInt(vc,newTag,out);
                  }
                  case Right(k) => {
                    val out = k + infoParams.flatMap{ tag => vc.info.getOrElse(tag,None).toSeq.flatMap{_.split(",")}}.filter( vv => vv != "." ).map{vv => string2double(vv)}.product
                    writeDouble(vc,newTag,out);
                  }
                }
              }
            }
          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "DIV",synon = Seq(),
              shortDesc = "",
              desc = "",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INT|FLOAT|INFO_Int|INFO_Float",req=true,dotdot=true )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader;
              def pv : Seq[String] = paramValues;
              def dgts : Option[Int] = digits;
              def md : VcfTagFcnMetadata = mmd;
              var isInteger : Boolean = true;
              var const : Double = 0.0;
              var infoParams : Seq[String] = Seq[String]();
              var eitherParams : Seq[Either[Double,String]] = Seq[Either[Double,String]]();
              
              def init() : Boolean = {
                isInteger = pv.exists{ pp => {
                  h.infoLines.find{ info => { info.ID == pp }}.map{ info => { info.Type != "Integer" }}.getOrElse( ! string2intOpt(pp).isDefined )
                }}
                pv.foreach{ pp => {
                  h.infoLines.find{ info => { info.ID == pp }}.foreach{ info => { 
                    if(info.Number != "1") warning("Warning: running "+md.id+" function on tag: "+info.ID+", which is a list. Will sum across all elements in each list. Is this what you intended?","NUMERIC_FCN_ON_LIST",100);
                  }}
                }}
                pv.foreach{ pp => {
                  h.infoLines.find{ info => { info.ID == pp }}.foreach{ info => { 
                    if(info.Type != "Integer" && info.Type != "Float") warning("Warning: running SUM function on tag: "+info.ID+", which is of type "+info.Type+". Will attempt to coerce values to a float or a list of floats. Is this what you intended?","NUMERIC_FCN_ON_LIST",100);
                  }}
                }}
                eitherParams = pv.map{ pp => {
                  if(h.infoLines.exists(hh => hh.ID == pp )){
                    Right( pp );
                  } else {
                    if(  string2doubleOpt(pp).isEmpty) error("Attempting to divide by "+pp+", which isn't numeric!");
                    Left( string2double(pp) );
                  }
                }}
                checkVcfTagParams();
              }
              def run(vc : SVcfOutputVariantLine){
                val outSeq : Seq[Option[Double]] = eitherParams.map{ ep => {
                  ep match {
                    case Left(x) => Some(x);
                    case Right(x) => vc.info.getOrElse(x,None).filter( vv => vv != "." ).map{ vv => string2double(vv) }
                  }
                }}
                val out : Option[Double] = outSeq.tail.foldLeft(outSeq.head){ case (soFar,curr) => {
                  if(curr.contains(0.0)){
                    None;
                  } else {
                    soFar.flatMap{ sf => { curr.map{ cc => sf / cc }}}
                  }
                }}
                out.foreach{ vv => {
                  writeDouble(vc,newTag,vv);
                }}
                
              }
            }
          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "LEN",synon = Seq(),
              shortDesc = "",
              desc = "",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INFO_list",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "delim", ty = "Char",req=false,dotdot=true )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader;
              def pv : Seq[String] = paramValues;
              def dgts : Option[Int] = digits;
              def md : VcfTagFcnMetadata = mmd;
              var delims : String = "";
              
              def init() : Boolean = {
                if(paramValues.length < 1){
                  error("Function "+md.id+" requires at least 1 parameter!");
                }
                delims = paramValues.tail.mkString("")
                if(delims.startsWith("^")){
                  delims = "[\\"+delims+"]";
                } else if(delims == ""){
                  delims = "[,]";
                } else {
                  delims = "["+delims+"]";
                }
                checkVcfTagParams();
              }
              def run(vc : SVcfOutputVariantLine){
                //vc.addInfo(tag, ""+(dd));
                vc.info.getOrElse(paramValues.head,None).foreach{ vv => {
                  if(vv == "."){
                    vc.addInfo(newTag, "0")
                  } else {
                    vc.addInfo(newTag, ""+vv.split(delims).length)
                  }
                }}
              }
            }
          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "LEN",synon = Seq(),
              shortDesc = "",
              desc = "",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INFO_list",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "delim", ty = "Char",req=false,dotdot=true )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader;
              def pv : Seq[String] = paramValues;
              def dgts : Option[Int] = digits;
              def md : VcfTagFcnMetadata = mmd;
              var delims : String = "";
              
              def init() : Boolean = {
                if(paramValues.length < 1){
                  error("Function "+md.id+" requires at least 1 parameter!");
                }
                delims = paramValues.tail.mkString("")
                if(delims.startsWith("^")){
                  delims = "[\\"+delims+"]";
                } else if(delims == ""){
                  delims = "[,]";
                } else {
                  delims = "["+delims+"]";
                }
                checkVcfTagParams();
              }
              def run(vc : SVcfOutputVariantLine){
                //vc.addInfo(tag, ""+(dd));
                vc.info.getOrElse(paramValues.head,None).foreach{ vv => {
                  if(vv == "."){
                    vc.addInfo(newTag, "0")
                  } else {
                    vc.addInfo(newTag, ""+vv.split(delims).length)
                  }
                }}
              }
            }
          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "DIFF",synon = Seq("SUBTRACT"),
              shortDesc = "",
              desc = "",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INT|FLOAT|INFO_Int|INFO_Float",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "x", ty = "INT|FLOAT|INFO_Int|INFO_Float",req=true,dotdot=false )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader;
              def pv : Seq[String] = paramValues;
              def dgts : Option[Int] = digits;
              def md : VcfTagFcnMetadata = mmd;
              var isInteger : Boolean = true;
              var runparams : Seq[Either[Either[Int,Double],String]] = Seq();
              def init() : Boolean = {
                if(paramValues.length != 2){
                  error("Function "+md.id+" requires exactly 2 parameters!");
                }
                isInteger = pv.exists{ pp => {
                  h.infoLines.find{ info => { info.ID == pp }}.map{ info => { info.Type != "Integer" }}.getOrElse( ! string2intOpt(pp).isDefined )
                }}
                pv.foreach{ pp => {
                  h.infoLines.find{ info => { info.ID == pp }}.foreach{ info => { 
                    if(info.Number != "1") warning("Warning: running "+md.id+" function on tag: "+info.ID+", which is a list. Will go across all elements in each list. Is this what you intended?","NUMERIC_FCN_ON_LIST",100);
                  }}
                }}
                pv.foreach{ pp => {
                  h.infoLines.find{ info => { info.ID == pp }}.foreach{ info => { 
                    if(info.Type != "Integer" && info.Type != "Float") warning("Warning: running "+info.ID+" function on tag: "+info.ID+", which is of type "+info.Type+". Will attempt to coerce values to a float or a list of floats. Is this what you intended?","NUMERIC_FCN_ON_LIST",100);
                  }}
                }}
                runparams = pv.map{ pp => {
                  if( h.infoLines.exists{ ff => ff.ID == pp } ){
                    Right(pp)
                  } else if(isInteger){
                    Left(Left( string2int(pp)));
                  } else {
                    Left(Right(string2double(pp)));
                  }
                }}
                checkVcfTagParams();
              }
              def run(vc : SVcfOutputVariantLine){
                
                val outseq : Seq[Either[Int,Double]]= runparams.map{ ppe => {
                  ppe match {
                    case Right(x) => if(isInteger){
                      Left(string2intOpt(x).getOrElse(0));
                    } else {
                      Right(string2doubleOpt(x).getOrElse(0.0));
                    }
                    case Left(Right(x)) => Right(x);
                    case Left(Left(x)) => Left(x);
                  }
                }}
                if(isInteger){
                  val a = outseq(0).left.toOption.getOrElse(0);
                  val b = outseq(1).left.toOption.getOrElse(0);
                  writeInt(vc,newTag,(a - b));
                } else {
                  val a = outseq(0).right.toOption.getOrElse(0.0);
                  val b = outseq(1).right.toOption.getOrElse(0.0);
                  writeDouble(vc,newTag,(a - b));
                }
              }
            }
          }
        },        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "MAX",synon = Seq(),
              shortDesc = "",
              desc = "",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INT|FLOAT|INFO_Int|INFO_Float",req=true,dotdot=true )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader;
              def pv : Seq[String] = paramValues;
              def dgts : Option[Int] = digits;
              def md : VcfTagFcnMetadata = mmd;
              var isInteger : Boolean = true;
              var const : Either[Int,Double] = Right[Int,Double](0);
              var infoParams : Seq[String] = Seq[String]();
              def init() : Boolean = {
                isInteger = pv.exists{ pp => {
                  h.infoLines.find{ info => { info.ID == pp }}.map{ info => { info.Type != "Integer" }}.getOrElse( ! string2intOpt(pp).isDefined )
                }}
                pv.foreach{ pp => {
                  h.infoLines.find{ info => { info.ID == pp }}.foreach{ info => { 
                    if(info.Number != "1") warning("Warning: running SUM function on tag: "+info.ID+", which is a list. Will sum across all elements in each list. Is this what you intended?","NUMERIC_FCN_ON_LIST",100);
                  }}
                }}
                pv.foreach{ pp => {
                  h.infoLines.find{ info => { info.ID == pp }}.foreach{ info => { 
                    if(info.Type != "Integer" && info.Type != "Float") warning("Warning: running SUM function on tag: "+info.ID+", which is of type "+info.Type+". Will attempt to coerce values to a float or a list of floats. Is this what you intended?","NUMERIC_FCN_ON_LIST",100);
                  }}
                }}
                infoParams = pv.filter{pp => h.infoLines.exists(hh => hh.ID == pp ) }
                if(isInteger){
                  const = Left[Int,Double]( pv.filter{pp => ! h.infoLines.exists(hh => hh.ID == pp ) }.map{ string2int(_) }.max )
                } else {
                  const = Right[Int,Double]( pv.filter{pp => ! h.infoLines.exists(hh => hh.ID == pp ) }.map{ string2double(_) }.max )
                }
                checkVcfTagParams();
              }
              def run(vc : SVcfOutputVariantLine){
                const match {
                  case Left(k) => {
                    val out = (infoParams.flatMap{ tag => vc.info.getOrElse(tag,None).toSeq.flatMap{_.split(",")}}.filter( vv => vv != "." ).map{vv => string2int(vv)} :+ k).max
                    writeInt(vc,newTag,out);
                  }
                  case Right(k) => {
                    val out = (infoParams.flatMap{ tag => vc.info.getOrElse(tag,None).toSeq.flatMap{_.split(",")}}.filter( vv => vv != "." ).map{vv => string2double(vv)} :+ k).max
                    writeDouble(vc,newTag,out);
                  }
                }
              }
            }
          }
        },        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "MIN",synon = Seq(),
              shortDesc = "",
              desc = "",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INT|FLOAT|INFO_Int|INFO_Float",req=true,dotdot=true )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader;
              def pv : Seq[String] = paramValues;
              def dgts : Option[Int] = digits;
              def md : VcfTagFcnMetadata = mmd;
              var isInteger : Boolean = true;
              var const : Either[Int,Double] = Right[Int,Double](0);
              var infoParams : Seq[String] = Seq[String]();
              def init() : Boolean = {
                isInteger = pv.exists{ pp => {
                  h.infoLines.find{ info => { info.ID == pp }}.map{ info => { info.Type != "Integer" }}.getOrElse( ! string2intOpt(pp).isDefined )
                }}
                pv.foreach{ pp => {
                  h.infoLines.find{ info => { info.ID == pp }}.foreach{ info => { 
                    if(info.Number != "1") warning("Warning: running SUM function on tag: "+info.ID+", which is a list. Will sum across all elements in each list. Is this what you intended?","NUMERIC_FCN_ON_LIST",100);
                  }}
                }}
                pv.foreach{ pp => {
                  h.infoLines.find{ info => { info.ID == pp }}.foreach{ info => { 
                    if(info.Type != "Integer" && info.Type != "Float") warning("Warning: running SUM function on tag: "+info.ID+", which is of type "+info.Type+". Will attempt to coerce values to a float or a list of floats. Is this what you intended?","NUMERIC_FCN_ON_LIST",100);
                  }}
                }}
                infoParams = pv.filter{pp => h.infoLines.exists(hh => hh.ID == pp ) }
                if(isInteger){
                  const = Left[Int,Double]( pv.filter{pp => ! h.infoLines.exists(hh => hh.ID == pp ) }.map{ string2int(_) }.min )
                } else {
                  const = Right[Int,Double]( pv.filter{pp => ! h.infoLines.exists(hh => hh.ID == pp ) }.map{ string2double(_) }.min )
                }
                checkVcfTagParams();
              }
              def run(vc : SVcfOutputVariantLine){
                const match {
                  case Left(k) => {
                    val out = (infoParams.flatMap{ tag => vc.info.getOrElse(tag,None).toSeq.flatMap{_.split(",")}}.filter( vv => vv != "." ).map{vv => string2int(vv)} :+ k).min
                    writeInt(vc,newTag,out);
                  }
                  case Right(k) => {
                    val out = (infoParams.flatMap{ tag => vc.info.getOrElse(tag,None).toSeq.flatMap{_.split(",")}}.filter( vv => vv != "." ).map{vv => string2double(vv)} :+ k).min
                    writeDouble(vc,newTag,out);
                  }
                }
              }
            }
          }
        }
        
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
  )


  /*class VcfTagFcnFact(paramValues : Seq[String], outHeader : SVcfHeader, fcn : VcfTagFunctionHolder){
    def checkParams() : Boolean = {
      params.padTo( paramValues.length, params.last ).zip(params).map{ case (vtfp, pp) => {
          vtfp.checkParamType(pp, outHeader);
      }}
    }
    def init(outHeader: SVcfHeader) : Boolean;
    def run(vc : SVcfOutputVariantLine)
  }*/
  
  class SanitizeVcf(fixInfo : Boolean = true) extends internalUtils.VcfTool.SVcfWalker { 
    def walkerName : String = "sanitize";
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        
        );

    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      
      return ( vcMap(vcIter)(vc => {
        val vb = vc.getOutputLine()
        
        vc.info.foreach{ case (infoTag,infoValue) => {
          val newvalue = infoValue.map{ vv => {
            vv.replace(" ","_").replace("=","&eq");
          }}
          vb.in_info = vb.info.updated(infoTag,newvalue); 
          
        }}
        
        vb;
      }), outHeader)
    }
    
  }

  
  

  class AddFuncTag(func : String, newTag : String, paramTags : Seq[String], digits : Option[Int] = None, desc : Option[String] = None ) extends internalUtils.VcfTool.SVcfWalker { 
    def walkerName : String = "AddFuncTag."+newTag
    //keywords: tagVariantFunction tagVariantsFunction Variant Function
    val f : String = func.toUpperCase;
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("newTag",newTag),
        ("func",func),
        ("paramTags",paramTags.mkString("|"))
    );

    /*
      Parameter Format:
        --tagVariantsFunction [ORDER:N|]tagid|desc|funcString|param1,param2,...|floatOutputDigits
              val cells = ftString.split("[|]",-1)
              val tagID = cells(0);
              val desc  = cells(1);
              val funcString = cells(2);
              val paramTags = cells.lift(3).map{_.split(",").toSeq}.getOrElse(Seq[String]());
              val outDigits = cells.lift(4).map{ _.toInt }
     */
    
    
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
              warning("WARN: cannot find tag: \""+param+"\"")
            }
            "?"
          }
        }
      }}
      val outType = 
      if(Set("TAG.TALLY.IFEXPR").contains(f)){
        if( paramTypes.lift(2).map{pp => pp == "Float"}.getOrElse(false) ){
          "Float"
        } else {
          "Integer"
        }
      } else  if(Set("CONVERT.TO.INT","TAG.TALLY","TAG.TALLY.IF","RANDFLAG","LEN").contains(f)  || f.startsWith("TAG.TALLY") ){
        "Integer"
      } else if(Set("CONVERT.TO.FLOAT","MULT.BY.K","DIV.BY.K","RATIO","PROD").contains(f)){
        "Float"
      } else if( Set("LEN").contains(f) ){
        "Integer"
      } else if(Set("SUM","MIN","MAX","DIFF","MAX.WITH.DEFAULT","MIN.WITH.DEFAULT").contains(f)){
         if(paramTypes.forall(pt => pt == "Integer")){
           "Integer"
         } else {
           "Float"
         }
      } else {
        "String"
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
      if(Set("COPY","RENAME").contains(f)){
        val oldName = paramTags.head;
        val oldLine = vcfHeader.infoLines.find{ln => {ln.ID == oldName }}.getOrElse({
            error("ERROR: attempting to "+f+" an INFO field: \""+oldName+"\" which DOES NOT EXIST!");
            null;
        })
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",newTag,Number=outNum,Type=outType,desc=desc.getOrElse(oldLine.desc+" (Result of performing function "+func+" on tags: "+paramTags.mkString(",")+".)")));
        if(f == "RENAME"){
          outHeader.infoLines = outHeader.infoLines.filter{ ln => ln.ID != oldName }
        }
      } else {
        outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",newTag,Number=outNum,Type=outType,desc=desc.getOrElse("Result of performing function "+func+" on tags: "+paramTags.mkString(",")+".")));
      }
      val overwriteInfos : Set[String] = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( outHeader.addedInfos );
      if( overwriteInfos.nonEmpty ){
        notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      }
      
      val countMap = new scala.collection.mutable.AnyRefMap[String,Int](defaultEntry = ( (s : String) => 0 ) )
      val countMapFloat = new scala.collection.mutable.AnyRefMap[String,Double](defaultEntry = ( (s : String) => 0.0 ) )

      val varcountMap = new scala.collection.mutable.AnyRefMap[String,Int](defaultEntry = ( (s : String) => 0 ) )
      //def getInfo(vv : SVcfVariantLine, tt : String) : Option[
      
      //val parser : SFilterLogicParser[SVcfVariantLine] = internalUtils.VcfTool.sVcfFilterLogicParser;
      //val filter : SFilterLogic[SVcfVariantLine] = parser.parseString(filterExpr);
      val filterExpr : Option[SFilterLogic[SVcfVariantLine]] = if(f == "TAG.TALLY.IFEXPR"){
            if( paramTags.length != 2 && paramTags.length != 3){
              error("TAG.TALLY.IFEXPR requires 2 or 3 comma-delimited parameters: (geneListTag,expression,[countVariable])")
            }
        Some(
           internalUtils.VcfTool.sVcfFilterLogicParser.parseString( paramTags(1) )
        )
      } else {
        None;
      }
      val rand : scala.util.Random = (if(Set("RANDFLAG").contains(f)){
        paramTags.lift(1).map{ pp => {
          string2long(pp)
        }}.map{ seed => {
          new scala.util.Random(seed);
        }}
      } else {
        None;
      }).getOrElse(new scala.util.Random());
      
      outHeader.reportAddedInfos(this)
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
        } else if(f == "RATIO"){
            val paramVals : Seq[Double] = paramTags.flatMap{ param => {
              v.info.get(param).getOrElse(None)
            }}.filter(paramVal => paramVal != ".").map{ paramVal => {
              paramVal.toDouble
            }}
            if(paramVals.length == 2){
              val out = if(paramVals(1) == 0.0){
                vc.addInfo(newTag, ".");
              } else {
                digits match {
                  case Some(dd) => {
                    vc.addInfo(newTag, ("%."+dd+"f").format(paramVals(0) / paramVals(1)) );
                  }
                  case None => {
                    vc.addInfo(newTag, ""+(paramVals(0) / paramVals(1)));
                  }
                }
              }
            } else {
              vc.addInfo(newTag, ".");
            }
        } else if(f == "FLAGSET"){
          //val tags = paramTags.map{ p => { p.split("=")(0) }};
          val tagNames = paramTags.map{ p => { 
            val cells = p.split("=");
            if(cells.length > 2){
              error("Error: each parameter in tagVariantFunction FLAGSET must be of the form: tagID or tagID=tagName. Offending param: \""+p+"\"");
            }
            (cells(0),cells.lift(1).getOrElse(p));
          }}
          val sets = tagNames.filter{ case (t,tid) => {
            v.info.get(t) match {
              case Some(Some(vv)) => {
                vv == "1";
              }
              case Some(None) => {
                true
              }
              case None => {
                false
              }
            }
          }}.map{_._2}
          vc.addInfo(newTag, (sets).toVector.sorted.padTo(1,".").mkString(","));
          //val tt = tags.zip(tagNames
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
        } else if(f == "SWITCH.AB"){
            val switchParam = v.info.get(paramTags(0))
            val A = v.info.getOrElse(paramTags(1),None);
            val B = v.info.getOrElse(paramTags(2),None);
            
            switchParam.foreach{ s => {
              if( s == "1"){
                A.foreach{ a => {
                  vc.addInfo(newTag,a);
                }}
              } else if(s == "0"){
                B.foreach{ b => {
                  vc.addInfo(newTag,b);
                }}
              }
            }}
            
        } else if(f == "SETS.INTERSECT"){
            val paramVals : Seq[Set[String]] = paramTags.map{ param => {
              v.info.get(param).getOrElse(None).filter{ ss => ss != "." }.map{ ss => ss.split("[,|]").toSet }.getOrElse(Set[String]())
            }}
            val intersect = paramVals.tail.foldLeft(paramVals.head){ case (soFar,curr) => { soFar.intersect(curr) }}
            vc.addInfo(newTag, (intersect).toVector.sorted.padTo(1,".").mkString(","));
        } else if(f == "LEN"){
            val sumLen = paramTags.map{ param => {
              v.info.get(param).getOrElse(None).filter{ ss => ss != "." }.map{ ss => ss.split("[,|]").toVector }.getOrElse(Vector[String]()).size
            }}.sum
            vc.addInfo(newTag, sumLen + "");
        } else if(f == "CONVERT.TO.INT"){
          val out = v.info.get(paramTags.head).getOrElse(None).map{z => math.round( string2double(z) ).toInt.toString}.getOrElse(".");
          //val out = v.info.get(paramTags.head).getOrElse(None).map{z => string2doubleOpt(z).map{zz => math.round(zz).toInt.toString}.getOrElse(".")}.getOrElse(".");
          vc.addInfo(newTag,out);
        } else if(f == "CONVERT.TO.FLOAT"){
          val out = v.info.get(paramTags.head).getOrElse(None).map{z => ( string2double(z) ).toString}.getOrElse(".");
          //val out = v.info.get(paramTags.head).getOrElse(None).map{z => string2doubleOpt(z).map{zz => zz.toString}.getOrElse(".")}.getOrElse(".");
          vc.addInfo(newTag,out);
        } else if(f == "MERGE.BOOLEAN.TAGS"){
          val tagNames = paramTags.lift(1).getOrElse(paramTags.head).split("[:]").toSeq;
          val out = paramTags.head.split("[:]").toSeq.zip(tagNames).flatMap{ case (pp,nn) => {
            v.info.getOrElse(pp,None).filter(vv => vv == "1").map{ vv => nn }
          }}.toSeq.padTo(1,".").mkString(",")
          vc.addInfo(newTag,out);
          
        } else if(f == "MIN" || f == "MIN.WITH.DEFAULT"){
          val pt = if(f == "MIN.WITH.DEFAULT"){ paramTags.tail } else { paramTags };
          val defaultString = if( f == "MIN.WITH.DEFAULT"){ paramTags.head } else { "." };
          if(outType == "Integer"){
            val paramVals : Seq[Int] = pt.flatMap{ param => {
              v.info.get(param).getOrElse(None)
            }}.filter(paramVal => paramVal != ".").flatMap{ paramVal => {
              paramVal.split(",").flatMap{ss => string2intOpt(ss)}
            }}
            if(paramVals.nonEmpty){
              vc.addInfo(newTag, ""+paramVals.min);
            } else {
              vc.addInfo(newTag, defaultString);
            }
          } else {
            val paramVals : Seq[Double] = pt.flatMap{ param => {
              v.info.get(param).getOrElse(None)
            }}.filter(paramVal => paramVal != ".").flatMap{ paramVal => {
              paramVal.split(",").flatMap{ss => string2doubleOpt(ss)}
            }}
            if(paramVals.nonEmpty){
              digits match {
                case Some(dd) => {
                  vc.addInfo(newTag, ("%."+dd+"f").format(paramVals.min) );
                }
                case None => {
                  vc.addInfo(newTag, ""+paramVals.min);
                }
              }
            } else {
              vc.addInfo(newTag, defaultString);
            }
          }
        } else if(f == "MAX" || f == "MAX.WITH.DEFAULT"){
          val pt = if(f == "MAX.WITH.DEFAULT"){ paramTags.tail } else { paramTags };
          val defaultString = if( f == "MAX.WITH.DEFAULT"){ paramTags.head } else { "." };
          if(outType == "Integer"){
            val paramVals : Seq[Int] = pt.flatMap{ param => {
              v.info.get(param).getOrElse(None)
            }}.filter(paramVal => paramVal != ".").flatMap{ paramVal => {
              paramVal.split(",").flatMap{ss => string2intOpt(ss)}
            }}
            if(paramVals.nonEmpty){
              vc.addInfo(newTag, ""+paramVals.max);
            } else {
              vc.addInfo(newTag, defaultString);
            }
          } else {
            val paramVals : Seq[Double] = pt.flatMap{ param => {
              v.info.get(param).getOrElse(None)
            }}.filter(paramVal => paramVal != ".").flatMap{ paramVal => {
              paramVal.split(",").flatMap{ss => string2doubleOpt(ss)}
            }}
            if(paramVals.nonEmpty){
              digits match {
                case Some(dd) => {
                  vc.addInfo(newTag, ("%."+dd+"f").format(paramVals.max) );
                }
                case None => {
                  vc.addInfo(newTag, ""+paramVals.max);
                }
              }
            } else {
              vc.addInfo(newTag, defaultString);
            }
          }
        } else if(f == "PROD"){
          val paramVals : Seq[Double] = paramTags.flatMap{ param => {
            string2doubleOpt(param) match {
              case Some(dd) => {
                Some(dd)
              }
              case None => {
                v.info.get(param).getOrElse(None).filter(paramVal => paramVal != ".").map{ paramVal => {
                  paramVal.toDouble
                }}
              }
            }
          }}
            if(paramVals.nonEmpty){
              val out = paramVals.product
              digits match {
                case Some(dd) => {
                  vc.addInfo(newTag, ("%."+dd+"f").format(out) );
                }
                case None => {
                  vc.addInfo(newTag, ""+out);
                }
              }
            } else {
              vc.addInfo(newTag, ".");
            }
        } else if(f == "RANDFLAG"){
          val thresh = string2double(paramTags.head);
          //if( scala.util.
          if(rand.nextDouble < thresh){
             vc.addInfo(newTag, "1" );
          } else {
             vc.addInfo(newTag, "0" );
          }
        } else if(f == "MULT.BY.K"){
            val k = string2double(paramTags(1));
            v.info.get(paramTags.head).getOrElse(None) match {
              case Some(pp) => {
                val out = string2double(pp) * k;
                digits match {
                  case Some(dd) => {
                    vc.addInfo(newTag, ("%."+dd+"f").format(out) );
                  }
                  case None => {
                    vc.addInfo(newTag, ""+out);
                  }
                }
              }
              case None => {
                vc.addInfo(newTag, ".");
              }
            }
        } else if(f == "DIV.BY.K"){
            val k = string2double(paramTags(1));
            v.info.get(paramTags.head).getOrElse(None) match {
              case Some(pp) => {
                val out = string2double(pp) / k;
                digits match {
                  case Some(dd) => {
                    vc.addInfo(newTag, ("%."+dd+"f").format(out) );
                  }
                  case None => {
                    vc.addInfo(newTag, ""+out);
                  }
                }
              }
              case None => {
                vc.addInfo(newTag, ".");
              }
            }
        } else if(f == "TAG.TALLY.IFEXPR"){

            filterExpr.foreach{ fe =>{
              if( fe.keep(v) ){
                warning("", newTag+":TOTAL_1",1 ) 
                val g : Set[String] = v.info.get(paramTags.head).getOrElse(None).filter{ ss => ss != "." }.map{ ss => ss.split("[,|]").toSet }.getOrElse(Set[String]())
                
                if(outType == "Integer"){
                  val vv : Int = paramTags.lift(2).map{ ss => v.info.get(ss).getOrElse(None).filter{ ss => ss != "." }.map{ ss => string2int(ss) }.getOrElse(0) }.getOrElse(1);
                    //v.info.get(paramTags(2)).getOrElse(None).filter{ ss => ss != "." }.map{ ss => string2int(ss) }.getOrElse(0)
                  if(vv > 0){
                    g.foreach{ gg => {
                      countMap.update( gg, countMap(gg) + vv );
                      varcountMap.update( gg, varcountMap(gg) + 1 );
                    }}
                    
                  }
                } else {
                  val vv : Double = paramTags.lift(2).map{ ss => v.info.get(ss).getOrElse(None).filter{ ss => ss != "." }.map{ ss => string2double(ss) }.getOrElse(0.0) }.getOrElse(1.0);
                    //v.info.get(paramTags(2)).getOrElse(None).filter{ ss => ss != "." }.map{ ss => string2int(ss) }.getOrElse(0)
                  if(vv > 0){
                    g.foreach{ gg => {
                      countMapFloat.update( gg, countMapFloat(gg) + vv );
                      varcountMap.update( gg, varcountMap(gg) + 1 );
                    }}
                    
                  }
                }
              } else {
                warning("", newTag+":TOTAL_0",1 ) 
              }
            }}
            
        } else if(f == "TAG.TALLY.IF" && paramTags.length >= 2){
            val g : Set[String] = v.info.get(paramTags.head).getOrElse(None).filter{ ss => ss != "." }.map{ ss => ss.split("[,|]").toSet }.getOrElse(Set[String]())
            val tagif : Int = v.info.get(paramTags(1)).getOrElse(None).filter{ ss => ss != "." }.map{ ss => string2int(ss) }.getOrElse(0)
            val vv : Int = paramTags.lift(2).map{ ss => v.info.get(ss).getOrElse(None).filter{ ss => ss != "." }.map{ ss => string2int(ss) }.getOrElse(0) }.getOrElse(1);
            if(tagif == 1 && vv > 0){
               g.foreach{ gg => {
                 countMap.update( gg, countMap(gg) + vv );
               }}
            }
        } else if(f == "TAG.TALLY" && paramTags.length > 1){
            val g : Set[String] = v.info.get(paramTags.head).getOrElse(None).filter{ ss => ss != "." }.map{ ss => ss.split("[,|]").toSet }.getOrElse(Set[String]())
            val vv : Int = v.info.get(paramTags.last).getOrElse(None).filter{ ss => ss != "." }.map{ ss => string2int(ss) }.getOrElse(0)
            g.foreach{ gg => {
              countMap.update( gg, countMap(gg) + vv );
            }}
        } else if(f == "TAG.TALLY" && paramTags.length == 1){
            val g : Set[String] = v.info.get(paramTags.head).getOrElse(None).filter{ ss => ss != "." }.map{ ss => ss.split("[,|]").toSet }.getOrElse(Set[String]())
            g.foreach{ gg => {
              countMap.update( gg, countMap(gg) + 1 );
            }}
        } else if(Set("COPY","RENAME").contains(f)){
          val oldName = paramTags.head;
          val newName = paramTags.last;
          v.info.getOrElse( oldName, None ).foreach{ ii => {
              vc.addInfo(newTag, ii);
          }}
          if(f == "RENAME"){
            vc.dropInfo(Set(oldName));
          }
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
        if(f == "TAG.TALLY" || f == "TAG.TALLY.IF" || f.startsWith("TAG.TALLY")){
          if(outType == "Integer"){
            countMap.foreach{ case (gg, vv) => {
              varcountMap.get( gg ) match {
                case Some(vvv) => {
                  tally(newTag+":"+paramTags.head+":"+gg,vv,vvv);
                }
                case None => {
                  tally(newTag+":"+paramTags.head+":"+gg,vv); 
                }
              } 
              
            }} 
          } else {
            countMapFloat.foreach{ case (gg, vv) => {
              varcountMap.get( gg ) match {
                case Some(vvv) => {
                  tally(newTag+":"+paramTags.head+":"+gg,vv,vvv);
                }
                case None => {
                  tally(newTag+":"+paramTags.head+":"+gg,vv);
                }
              }
              
            }}
          }
        }
      })),outHeader)
      
    }
  } 
    
  class TallyFuncTagLEGACY(func : String, newTag : String, paramTags : Seq[String], digits : Option[Int] = None, desc : Option[String] = None ) extends internalUtils.VcfTool.SVcfWalker { 
    def walkerName : String = "AddFuncTag."+newTag
    //keywords: tagVariantFunction tagVariantsFunction Variant Function
    val f : String = func.toUpperCase;
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("newTag",newTag),
        ("func",func),
        ("paramTags",paramTags.mkString("|"))
    );

    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      var errCt = 0;
      
      val paramTypes = paramTags.zipWithIndex.map{ case (param,pidx) => {
        vcfHeader.infoLines.find(infoline => infoline.ID == param) match {
          case Some(paramLine) => {
            paramLine.Type
          }
          case None => {
            if(! (Set("STATICSET.INTERSECT").contains(func) && pidx > 0)){
              warning("WARN: cannot find tag: \""+param+"\"")
            }
            "?"
          }
        }
      }}
      
      val outType = 
      if(Set("TAG.TALLY.IFEXPR").contains(f)){
        if( paramTypes.lift(2).map{pp => pp == "Float"}.getOrElse(false) ){
          "Float"
        } else {
          "Integer"
        }
      } else  if(Set("CONVERT.TO.INT","TAG.TALLY","TAG.TALLY.IF","RANDFLAG","LEN").contains(f)  || f.startsWith("TAG.TALLY") ){
        "Integer"
      } else if(Set("CONVERT.TO.FLOAT","MULT.BY.K","DIV.BY.K","RATIO","PROD").contains(f)){
        "Float"
      } else if( Set("LEN").contains(f) ){
        "Integer"
      } else if(Set("SUM","MIN","MAX","DIFF","MAX.WITH.DEFAULT","MIN.WITH.DEFAULT").contains(f)){
         if(paramTypes.forall(pt => pt == "Integer")){
           "Integer"
         } else {
           "Float"
         }
      } else {
        "String"
      }
      
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      
      val countMap = new scala.collection.mutable.AnyRefMap[String,Int](defaultEntry = ( (s : String) => 0 ) )
      val countMapFloat = new scala.collection.mutable.AnyRefMap[String,Double](defaultEntry = ( (s : String) => 0.0 ) )
      val varcountMap = new scala.collection.mutable.AnyRefMap[String,Int](defaultEntry = ( (s : String) => 0 ) )
      
      val filterExpr : Option[SFilterLogic[SVcfVariantLine]] = if(f == "TAG.TALLY.IFEXPR"){
            if( paramTags.length != 2 && paramTags.length != 3){
              error("TAG.TALLY.IFEXPR requires 2 or 3 comma-delimited parameters: (geneListTag,expression,[countVariable])")
            }
        Some(
           internalUtils.VcfTool.sVcfFilterLogicParser.parseString( paramTags(1) )
        )
      } else {
        None;
      }
      outHeader.reportAddedInfos(this)
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine();
        //vc.dropInfo(overwriteInfos);
        if(f == "TAG.TALLY.IFEXPR"){
            filterExpr.foreach{ fe =>{
              if( fe.keep(v) ){
                warning("", newTag+":TOTAL_1",1 ) 
                val g : Set[String] = v.info.get(paramTags.head).getOrElse(None).filter{ ss => ss != "." }.map{ ss => ss.split("[,|]").toSet }.getOrElse(Set[String]())
                
                if(outType == "Integer"){
                  val vv : Int = paramTags.lift(2).map{ ss => v.info.get(ss).getOrElse(None).filter{ ss => ss != "." }.map{ ss => string2int(ss) }.getOrElse(0) }.getOrElse(1);
                    //v.info.get(paramTags(2)).getOrElse(None).filter{ ss => ss != "." }.map{ ss => string2int(ss) }.getOrElse(0)
                  if(vv > 0){
                    g.foreach{ gg => {
                      countMap.update( gg, countMap(gg) + vv );
                      varcountMap.update( gg, varcountMap(gg) + 1 );
                    }}
                    
                  }
                } else {
                  val vv : Double = paramTags.lift(2).map{ ss => v.info.get(ss).getOrElse(None).filter{ ss => ss != "." }.map{ ss => string2double(ss) }.getOrElse(0.0) }.getOrElse(1.0);
                    //v.info.get(paramTags(2)).getOrElse(None).filter{ ss => ss != "." }.map{ ss => string2int(ss) }.getOrElse(0)
                  if(vv > 0){
                    g.foreach{ gg => {
                      countMapFloat.update( gg, countMapFloat(gg) + vv );
                      varcountMap.update( gg, varcountMap(gg) + 1 );
                    }}
                    
                  }
                }
              } else {
                warning("", newTag+":TOTAL_0",1 ) 
              }
            }}
            
        } else if(f == "TAG.TALLY.IF" && paramTags.length >= 2){
            val g : Set[String] = v.info.get(paramTags.head).getOrElse(None).filter{ ss => ss != "." }.map{ ss => ss.split("[,|]").toSet }.getOrElse(Set[String]())
            val tagif : Int = v.info.get(paramTags(1)).getOrElse(None).filter{ ss => ss != "." }.map{ ss => string2int(ss) }.getOrElse(0)
            val vv : Int = paramTags.lift(2).map{ ss => v.info.get(ss).getOrElse(None).filter{ ss => ss != "." }.map{ ss => string2int(ss) }.getOrElse(0) }.getOrElse(1);
            if(tagif == 1 && vv > 0){
               g.foreach{ gg => {
                 countMap.update( gg, countMap(gg) + vv );
               }}
            }
        } else if(f == "TAG.TALLY" && paramTags.length > 1){
            val g : Set[String] = v.info.get(paramTags.head).getOrElse(None).filter{ ss => ss != "." }.map{ ss => ss.split("[,|]").toSet }.getOrElse(Set[String]())
            val vv : Int = v.info.get(paramTags.last).getOrElse(None).filter{ ss => ss != "." }.map{ ss => string2int(ss) }.getOrElse(0)
            g.foreach{ gg => {
              countMap.update( gg, countMap(gg) + vv );
            }}
        } else if(f == "TAG.TALLY" && paramTags.length == 1){
            val g : Set[String] = v.info.get(paramTags.head).getOrElse(None).filter{ ss => ss != "." }.map{ ss => ss.split("[,|]").toSet }.getOrElse(Set[String]())
            g.foreach{ gg => {
              countMap.update( gg, countMap(gg) + 1 );
            }}
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
        if(f == "TAG.TALLY" || f == "TAG.TALLY.IF" || f.startsWith("TAG.TALLY")){
          if(outType == "Integer"){
            countMap.foreach{ case (gg, vv) => {
              varcountMap.get( gg ) match {
                case Some(vvv) => {
                  tally(newTag+":"+paramTags.head+":"+gg,vv,vvv);
                }
                case None => {
                  tally(newTag+":"+paramTags.head+":"+gg,vv); 
                }
              } 
              
            }} 
          } else {
            countMapFloat.foreach{ case (gg, vv) => {
              varcountMap.get( gg ) match {
                case Some(vvv) => {
                  tally(newTag+":"+paramTags.head+":"+gg,vv,vvv);
                }
                case None => {
                  tally(newTag+":"+paramTags.head+":"+gg,vv);
                }
              }
              
            }}
          }
        }
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
      outHeader.reportAddedInfos(this)
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

      outHeader.reportAddedInfos(this)
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
      outHeader.reportAddedInfos(this)
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
          outHeader.reportAddedInfos(this)
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
  
  /*
       case class VarExtract(tagID : String, columnIx : Seq[Int], desc : String, collapseUniques : Boolean, 
                          tagSet : Set[String] = Set[String]("HIGH","MODERATE","LOW"),
                          listSet : Set[String] = Set[String]("ALL","onList")){
   */
  
  class SnpEffExtractElement(tagPrefix : String, tagPrefixOutput : Option[String],
                             fieldInfix : String,
                             columnIx : Seq[Int], desc : String , collapseUniques : Boolean,
                             tagSet : Seq[String] = Seq[String]("HIGH","MODERATE","LOW","NS","ANY")) extends internalUtils.VcfTool.SVcfWalker { 
    def walkerName : String = "SnpEffFieldExtract"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
           ("tagPrefix",tagPrefix),
           ("tagPrefixOutput",tagPrefixOutput.getOrElse(tagPrefix)),
           ("columnIx",columnIx.mkString("|")),
           ("fieldInfix",fieldInfix),
           ("desc",desc),
           ("tagSet",tagSet.mkString("|"))
           )
    val thisWalker = this;
    val tagOut = tagPrefixOutput.getOrElse(tagPrefix);
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      
      val makeTags = tagSet.map{ ts => {
          val exFromDesc = if( Seq("HIGH","MODERATE","LOW").contains(ts) ){
            ts
          } else if( ts == "NS"){
            "HIGH/MODERATE"
          } else {
            "HIGH/MODERATE/LOW"
          }
          val tsFix = if(ts == "ANY"){
            ""
          } else {
            ts
          }
          val suffixSet = if( Seq("HIGH","MODERATE","LOW").contains(ts) ){
            ts
          } else if( ts == "NS"){
            "HIGH/MODERATE"
          } else {
            "HIGH/MODERATE/LOW"
          }
          val tsFixSet = if( Seq("HIGH","MODERATE","LOW").contains(ts) ){
            Seq(ts)
          } else if( ts == "NS"){
            Seq("HIGH","MODERATE")
          } else {
            Seq("HIGH","MODERATE","LOW")
          }
          
          outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = tagOut+"_"+fieldInfix+"_"+tsFix, ".", "String", desc+". Extracted from the columns "+columnIx.map{ _.toString }.mkString("/")+", from fields(s): "+tagPrefix+"_"+exFromDesc+". ")).addWalker(this))
          
          (tsFixSet.map{ tsfs => {
            tagPrefix+"_"+tsfs
          }}, tagOut+"_"+fieldInfix+"_"+tsFix)
      }}
      
      //outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = svexTag+"HIGH", ".", "String", "Extracted from ANN, high only. "+svexFullDesc)).addWalker(this))

      val overwriteInfos : Set[String] = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( outHeader.addedInfos );
      if( overwriteInfos.nonEmpty ){
        notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      }
      outHeader.reportAddedInfos(this)
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine()
        vc.dropInfo(overwriteInfos);
        
        makeTags.foreach{ case (tt,outTagId) => {
          val outS1 = tt.flatMap{ ttt => {
            v.info.get(ttt).getOrElse(None).map{ ann => {
              val cells = ann.split(",");
              columnIx.map{ cix => {
                cells.lift(cix).getOrElse(".")
              }}.mkString("/")
            }}
          }}
          val out = if(collapseUniques){
            outS1.distinct
          } else {
            outS1
          }
          vc.addInfo( outTagId,     out.padTo(1,".").mkString(","));
        }}
        
        vc
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
    }
  }
  
  /*
  class SnpEffExtractGeneList(tagPrefix : String, 
                             geneListTagInfix : String,
                             fieldInfix : String,
                             columnIx : Seq[Int], desc : String , collaseUniques : Boolean,
                             tagSet : Seq[String] = Seq[String]("HIGH","MODERATE","LOW","NS","ANY")) extends internalUtils.VcfTool.SVcfWalker { 
    def walkerName : String = "SnpEffFieldExtract"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
           ("tagPrefix",tagPrefix),
           ("tagPrefixOutput",tagPrefixOutput.getOrElse(tagPrefix)),
           ("geneListTagPrefix",geneListTagInfix),
           ("columnIx",columnIx.mkString("|")),
           ("fieldInfix",fieldInfix),
           ("desc",desc),
           ("tagSet",tagSet.mkString("|"))
           )
    val thisWalker = this;
    val tagOut = tagPrefixOutput.getOrElse(tagPrefix);
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      
      val makeTags = tagSet.map{ ts => {
          val exFromDesc = if( Seq("HIGH","MODERATE","LOW").contains(ts) ){
            ts
          } else if( ts == "NS"){
            "HIGH/MODERATE"
          } else {
            "HIGH/MODERATE/LOW"
          }
          val tsFix = if(ts == "ANY"){
            ""
          } else {
            ts
          }
          val suffixSet = if( Seq("HIGH","MODERATE","LOW").contains(ts) ){
            ts
          } else if( ts == "NS"){
            "HIGH/MODERATE"
          } else {
            "HIGH/MODERATE/LOW"
          }
          val tsFixSet = if( Seq("HIGH","MODERATE","LOW").contains(ts) ){
            Seq(ts)
          } else if( ts == "NS"){
            Seq("HIGH","MODERATE")
          } else {
            Seq("HIGH","MODERATE","LOW")
          }
          
          outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = tagOut+"_"+fieldInfix+"_"+tsFix, ".", "String", desc+". Extracted from the columns "+columnIx.map{ _.toString }.mkString("/")+", from fields(s): "+tagPrefix+"_"+exFromDesc+". ")).addWalker(this))
          
          (tsFixSet.map{ tsfs => {
            tagPrefix+"_"+tsfs
          }}, tagOut+"_"+fieldInfix+"_"+tsFix)
      }}
      
      //outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = svexTag+"HIGH", ".", "String", "Extracted from ANN, high only. "+svexFullDesc)).addWalker(this))

      val overwriteInfos : Set[String] = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( outHeader.addedInfos );
      if( overwriteInfos.nonEmpty ){
        notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      }
      
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine()
        vc.dropInfo(overwriteInfos);
        
        makeTags.foreach{ case (tt,outTagId) => {
          val outS1 = tt.flatMap{ ttt => {
            v.info.get(ttt).getOrElse(None).map{ ann => {
              val cells = ann.split(",");
              columnIx.map{ cix => {
                cells.lift(cix).getOrElse(".")
              }}.mkString("/")
            }}
          }}
          val out = if(collaseUniques){
            outS1.distinct
          } else {
            outS1
          }
          vc.addInfo( outTagId,     out.padTo(1,".").mkString(","));
        }}
        
        vc
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
    }
  }*/
  
  class SnpEffInfoExtract(tagID : String = "ANN", 
                          tagPrefix : String = "ANNEX_",
                          geneList : Option[List[String]] = None,
                          snpEffBiotypeKeepList : Option[List[String]] = None,
                          snpEffEffectKeepList : Option[List[String]] = None,
                          snpEffWarningDropList : Option[List[String]] = None,
                          snpEffKeepIdx : Option[List[String]] = None,
                          geneListName : Option[String] = None,
                          snpEffVarExtract : List[String] = List[String](),
                          snpEffInfoExtract : List[String] = List[String](),
                          geneListTagInfix : String = "onList_",
                          snpEffBiotypeIdx : Int = 7,
                          snpEffWarnIdx : Int = 15,
                          snpEffFieldLen : Int = 16,
                          snpEffFields : Option[List[String]] = None,
                          snpEffGeneNameIdx : Option[Int] = None,
                          severityListSet : Option[String] = None
                          ) extends internalUtils.VcfTool.SVcfWalker { 
    
    
    def walkerName : String = "SnpEffInfoExtract"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("tagID",tagID),
        ("tagPrefix",tagPrefix),
        ("snpEffBiotypeKeepList",snpEffBiotypeKeepList.map{ s => s.mkString("|") }.getOrElse("None")),
        ("snpEffEffectKeepList",snpEffEffectKeepList.map{ s => s.mkString("|") }.getOrElse("None")),
        ("snpEffWarningDropList",snpEffWarningDropList.map{ s => s.mkString("|") }.getOrElse("None"))
    );
    //def walkerInfo : SVcfWalkerInfo = StdVcfConverter;
    
    val annidxAllele = 0
    val annidxEffect = 1 
    val annidxImpact = 2
    val annidxGeneName = 3
    val annidxGeneID  = snpEffGeneNameIdx.getOrElse(4)
    //val annidxTxType = 5
    //val annidxTxID = 6
    val annidxTxBiotype=snpEffBiotypeIdx
    //val annidxRank = 8
    //val annidxCdot= 9
    //val annidxPdot=10
    //val annidxCpos = 11
    val annidxWarn=snpEffWarnIdx
    
    
    
    val snpEffBiotypeKeepSet : Option[Set[String]] = snpEffBiotypeKeepList.map{_.toSet}
    val snpEffEffectKeepSet : Option[Set[String]] = snpEffEffectKeepList.map{_.toSet}
    val snpEffWarningDropSet : Option[Set[String]] = snpEffWarningDropList.map{_.toSet}
    val geneSet : Option[Set[String]] = geneList.map{_.toSet}
    
    val keepIdx : Set[Int] = snpEffKeepIdx.map{ ki => {
      ki.map(string2int(_)).toSet
    }}.getOrElse( Range(0,snpEffFieldLen).toSet )
    
    def getSevSet(s : Option[String]) : Set[String] = {
      val ss = s.getOrElse(severityListSet.getOrElse("HIGH/MODERATE/LOW"))
      ss.split("/").toSet
    }
    //val sevListSet = getSevSet(severityListSet)
    //good default keepIdx set: 1,2,3,4,7,10,15,16
    
    //  
    //                                                0       1        2        3           4      5        6      7           8      9         10        11           12             13                14                 15     16
    val snpEffIdxDesc = snpEffFields.getOrElse(Seq("allele","effect","impact","geneName","geneID","txType","txID","txBiotype","rank","HGVS.c","HGVS.p","cDNAposition","cdsPosition","proteinPosition","distToFeature","warnings","errors"));
    val snpEffFmtDescString = "A comma delimited list with bar-delimited entries in the format: "+snpEffIdxDesc.mkString("|") +"."
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
    
    val snpEffVarExtractFinal = snpEffVarExtract :+ ("GENES:"+annidxGeneName+":Extracted variant gene list. :"+severityListSet.getOrElse("HIGH/MODERATE/LOW"))
    
    val svexIdxStringsDesc = Map( ("EFFECT","(Effects with the effects that are not in the effectSet removed.)"),
                                  ("FIRSTEFFECT","(The first effect in each entry)"))
    val svexIdxStrings = svexIdxStringsDesc.keys.toVector;

    val severityList = Vector[Set[String]](Set("HIGH"),Set("MODERATE"),Set("LOW","MODIFIER"))
    val severityNames = Vector[String]("HIGH","MODERATE","LOW")
    
    val severitySets = Vector[(String,Set[String])](
          ("HIGH",Set("HIGH")),
          ("MODERATE",Set("MODERATE")),
          ("LOW",Set("LOW")),
          ("MODIFIER",Set("MODIFIER")),
          ("NonNS",Set("LOW","MODIFIER")),
          ("NS",Set("HIGH","MODERATE")),
          ("ANY",Set("HIGH","MODERATE","LOW","MODIFIER"))
        )
    val severityMap = severitySets.toMap;
    val effSevList = Vector("HIGH","MODERATE","LOW","MODIFIER");
    
    
    val svexInfo = snpEffVarExtractFinal.map{ svexRAW => {
      val svex = svexRAW.trim();
      val svexCellsRaw = svex.split("[:]").map{ svxs => svxs.trim() }
      val noCollapse = svexCellsRaw.contains("noCollapseUniques");
      val svexCells = svexCellsRaw.filter( svr => svr != "noCollapseUniques" );
      val (svexOutTag,svexIdxList,svexDesc,svexSevSet) = (svexCells(0),svexCells(1),svexCells.lift(2).getOrElse("(Extracted from "+tagID+")"), getSevSet(svexCells.lift(3)));
      
      val svexIdx : Vector[Either[String,Int]] = svexIdxList.split("/").map{xx => {
        try {
          Right(xx.toInt)
        } catch {
          case e : Exception => {
            if( ! svexIdxStrings.contains(xx)){
              error("ERROR: extractFields element 2 must be a slash delimited list of either integer indices or one of the following strings: "+svexIdxStrings.mkString(","))
            }
            Left(xx)
          }
        }
      }}.toVector
      val svexFmtDescString = svexDesc+". Formatted as a comma delimited list with colon-delimited entries in the format: "+svexIdx.map{i => {
        i match {
          case Left(xx) => svexIdxStringsDesc(xx)
          case Right(i) => snpEffIdxDesc(i)
        }
      }}.mkString(":") +". "
      val svexFullDesc = snpEffFmtDescString + bioTypeDesc + effectListDesc + warnListDesc
      (tagPrefix+svexOutTag+"_",svexIdx,svexFullDesc,svexSevSet, noCollapse)
    }}

    val thisWalker = this;
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      
      //outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = tagPrefix+"HIGH", ".", "String", "Shortened info extracted from ANN, high only. "+annFmtDesc)).addWalker(this))
      //outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = tagPrefix+"MODERATE", ".", "String", "Extract from ANN, moderate only. "+annFmtDesc)).addWalker(this))
      //outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = tagPrefix+"LOW", ".", "String", "Extract from ANN, LOW or MODIFIER only. "+annFmtDesc)).addWalker(this))
      
      /*sevListSet.map{ sev => {
        val sevNote = if(sev != severityMap(sev).mkString("?")){
          sev + " (including "+severityMap(sev).mkString("/")+")";
        } else {
          sev
        }
        outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = tagPrefix+"GENES_"+sev, ".", "String", "Gene list with "+sevNote+" effect, extracted from "+tagID+". "+bioTypeDesc+effectListDesc+warnListDesc)).addWalker(this))
        geneList.foreach{ gl => {
          outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = tagPrefix+geneListTagInfix+"GENES_"+sev, ".", "String", "Gene list with "+sevNote+" effect. "+geneListDesc+" Extracted from "+tagID+". "+bioTypeDesc+effectListDesc+warnListDesc)).addWalker(this))
        }}
      }}*/
      
      //outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = tagPrefix+"GENES_HIGH", ".", "String", "Gene list with HIGH effect. "+bioTypeDesc+effectListDesc+warnListDesc)).addWalker(this))
      //outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = tagPrefix+"GENES_MODERATE", ".", "String", "Gene list with MODERATE effect. "+bioTypeDesc+effectListDesc+warnListDesc)).addWalker(this))
      //outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = tagPrefix+"GENES_LOW", ".", "String", "Gene list with LOW or MODIFIER effect. "+bioTypeDesc+effectListDesc+warnListDesc)).addWalker(this))
      
      //geneList.foreach{ gl => {
        //outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = tagPrefix+geneListTagInfix+"HIGH", ".", "String", "Extracted from ANN, high only. "+geneListDesc+annFmtDesc)).addWalker(this))
        //outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = tagPrefix+geneListTagInfix+"MODERATE", ".", "String", "Extracted from ANN, moderate effects only. "+geneListDesc+annFmtDesc)).addWalker(this))
        //outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = tagPrefix+geneListTagInfix+"LOW", ".", "String", "Extracted from ANN, low or modifier effects only. "+geneListDesc+annFmtDesc)).addWalker(this))

        //outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = tagPrefix+geneListTagInfix+"GENES_HIGH", ".", "String", "Gene list with HIGH effect. "+geneListDesc+bioTypeDesc+effectListDesc+warnListDesc)).addWalker(this))
        //outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = tagPrefix+geneListTagInfix+"GENES_MODERATE", ".", "String", "Gene list with MODERATE effect. "+geneListDesc+bioTypeDesc+effectListDesc+warnListDesc)).addWalker(this))
        //outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = tagPrefix+geneListTagInfix+"GENES_LOW", ".", "String", "Gene list with LOW effect. "+geneListDesc+bioTypeDesc+effectListDesc+warnListDesc)).addWalker(this))
      
      //}}
      svexInfo.foreach{ case (svexTag,svexIdx,svexFullDesc,svexSevSet, noCollapse) => {
          svexSevSet.map{ sev => {
            val sevNote = if(sev != severityMap(sev).mkString("?")){
              sev + " (including "+severityMap(sev).mkString("/")+")";
            } else {
              sev
            }
            outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = svexTag+sev, ".", "String", " with "+sevNote+" effect, extracted from "+tagID+". "+bioTypeDesc+effectListDesc+warnListDesc)).addWalker(this))
            geneList.foreach{ gl => {
              outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,ID = svexTag+geneListTagInfix+sev, ".", "String", "With "+sevNote+" effect, "+geneListDesc+svexFullDesc)).addWalker(this))
            }}
          }}
      }}
      
      val overwriteInfos : Set[String] = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( outHeader.addedInfos );
      if( overwriteInfos.nonEmpty ){
        notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      }
      //vc.dropInfo(overwriteInfos);
 

      outHeader.reportAddedInfos(this)
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine()
        vc.dropInfo(overwriteInfos);
        val anndata = effSevList.toArray.map{ es => {
          Vector[Array[String]]();
        }}
          //Array.fill[Vector[Array[String]]](effSevList.length)
        /*
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
        var gLoG = Vector[String]();*/
        
        val svexData       : Array[Array[Vector[String]]] = svexInfo.map{ x => effSevList.toArray.map{ z => Vector[String]() } }.toArray;
        val svexDataOnList : Array[Array[Vector[String]]] = svexInfo.map{ x => effSevList.toArray.map{ z => Vector[String]() } }.toArray;

        //reportln("###################################################################","debug");
        //reportln("###################################################################","debug");
        
        v.info.get(tagID).getOrElse(None).foreach{ ann => {
          val annCells = ann.split(",").map{ aa => aa.split("[|]",-1) }
          
          annCells.foreach{ cells => {
            //reportln("ANN: "+cells.mkString("|"),"debug");
            val impact = cells(annidxImpact);
            val effects = cells.lift(annidxEffect).map(_.split("&").toVector).getOrElse(Vector());
            val warnings = cells.lift(annidxWarn).map(_.split("&").toSet).getOrElse(Set());
            val biotype = cells(annidxTxBiotype)
            val geneid = cells.lift(annidxGeneID).getOrElse("")
            val effectsToKeep = effects.filter{ ee => snpEffEffectKeepSet.map{ ks => ks.contains(ee) }.getOrElse(true) }
              //snpEffEffectKeepSet.map{ ks => ks.intersect(effects) }.getOrElse( effects ).toVector.sorted
            
            val keepEffect = snpEffEffectKeepSet.map{ ks => effectsToKeep.nonEmpty }.getOrElse(true)
            val keepGene = geneSet.map{ gg =>  gg.contains(geneid) }.getOrElse(false)
            val dropWarn = snpEffWarningDropSet.map{ kk => kk.intersect(warnings).nonEmpty }.getOrElse(false);
            val keepbt = snpEffBiotypeKeepList.map{ kk => kk.contains(biotype) }.getOrElse(true);
            val geneName = cells.lift(annidxGeneName).getOrElse(".")
            if( keepEffect && ( ! dropWarn) && keepbt ){
              //val outCells = cells.zipWithIndex.filter{ case (c,i) => keepIdx.contains(i) }.map{_._1}
              //val svexCells = svexInfo.map{ case (svexTag,svexIdx,svexDesc,svexSevSet, noCollapse) => {
              //  svexIdx.map{i => cells(i)}.mkString(":")
              //}}
              svexInfo.zipWithIndex.foreach{ case ((svexTag,svexIdx,svexDesc,svexSevSet, noCollapse),i) => {
                val sevidx = effSevList.indexOf(impact);
                if(sevidx >= 0){
                  val ss = svexIdx.map{k => {
                    k match {
                      case Left(xx) => {
                        if(xx == "EFFECT"){
                          effectsToKeep.mkString("&");
                        } else if(xx == "FIRSTEFFECT"){
                          effectsToKeep.head
                        } else {
                          error("SnpEff Info Extract: "+xx+" not implemented!");
                          "???"
                        }
                      }
                      case Right(kk) => cells(kk)
                    }
                  }}.mkString(":")
                  svexData(i)(sevidx) = svexData(i)(sevidx) :+ ss
                  if(keepGene){
                    svexDataOnList(i)(sevidx) = svexDataOnList(i)(sevidx) :+ ss
                  }
                }
              }}
            }
          }}

          /*svexData.zipWithIndex.foreach{ case (svd,i) => {
            reportln("--------------------------------------------------","debug");
            reportln("svexData["+i+"].length = "+svd.length,"debug");
          }}
          svexData.zipWithIndex.foreach{ case (svd,i) => {
            reportln("--------------------------------------------------","debug");
            reportln("svexData["+i+"]:","debug");
            svd.zipWithIndex.foreach{ case (svv,j) => {
              reportln("svexData["+i+"]["+j+"].length = "+svv.length,"debug");
            }}
            svd.zipWithIndex.foreach{ case (svv,j) => {
              reportln("svexData["+i+"]["+j+"]: "+svv.mkString(","),"debug");
            }}
          }}*/
          
          svexData.zip(svexInfo).foreach{ case (ssdat, (svexTag,svexIdx,svexDesc,svexSevSet, noCollapse)) => {
            severitySets.filter{ case (sev,sevset) => {
              svexSevSet.contains(sev);
            }}.foreach{ case (sev,sevset) => {
              val ss = ssdat.zip(effSevList).filter{ case (ssd,esev) => {
                sevset.contains(esev)
              }}.map{ case (ssd,esev) => {
                ssd
              }}.toVector.flatten.padTo(1,".");
              val sso = if(noCollapse){
                ss.mkString(",");
              } else {
                ss.distinct.sorted.mkString(",")
              }
              vc.addInfo( svexTag+sev,sso);
            }}
          }}
          
          geneList.foreach{ gl => {
            svexDataOnList.zip(svexInfo).foreach{ case (ssdat, (svexTag,svexIdx,svexDesc,svexSevSet, noCollapse)) => {
              severitySets.filter{ case (sev,sevset) => {
                svexSevSet.contains(sev);
              }}.foreach{ case (sev,sevset) => {
                val ss = ssdat.zip(effSevList).filter{ case (ssd,esev) => {
                  sevset.contains(esev)
                }}.map{ case (ssd,esev) => {
                  ssd
                }}.toVector.flatten.padTo(1,".")
                val sso = if(noCollapse){
                  ss.mkString(",");
                } else {
                  ss.distinct.sorted.mkString(",")
                }
                vc.addInfo( svexTag+geneListTagInfix+sev,sso);
              }}
            }}
          }}
          
          //vc.addInfo( tagPrefix+"HIGH",     annHi.map{ xx => xx.mkString("|") }.padTo(1,".").mkString(","));
          //vc.addInfo( tagPrefix+"MODERATE", annMd.map{ xx => xx.mkString("|") }.padTo(1,".").mkString(","));
          //vc.addInfo( tagPrefix+"LOW",      annLo.map{ xx => xx.mkString("|") }.padTo(1,".").mkString(","));
          //vc.addInfo( tagPrefix+"GENES_HIGH",     gHi.padTo(1,".").distinct.sorted.mkString(","));
          //vc.addInfo( tagPrefix+"GENES_MODERATE", gMd.padTo(1,".").distinct.sorted.mkString(","));
          //vc.addInfo( tagPrefix+"GENES_LOW",      gLo.padTo(1,".").distinct.sorted.mkString(","));
          
          //geneList.foreach{ gl => {
            //vc.addInfo( tagPrefix+geneListTagInfix+"HIGH",     annHiG.map{ xx => xx.mkString("|") }.padTo(1,".").mkString(","));
            //vc.addInfo( tagPrefix+geneListTagInfix+"MODERATE", annMdG.map{ xx => xx.mkString("|") }.padTo(1,".").mkString(","));
            //vc.addInfo( tagPrefix+geneListTagInfix+"LOW",      annLoG.map{ xx => xx.mkString("|") }.padTo(1,".").mkString(","));
          //  vc.addInfo( tagPrefix+geneListTagInfix+"GENES_HIGH",     gHiG.padTo(1,".").distinct.sorted.mkString(","));
          //  vc.addInfo( tagPrefix+geneListTagInfix+"GENES_MODERATE", gMdG.padTo(1,".").distinct.sorted.mkString(","));
          //  vc.addInfo( tagPrefix+geneListTagInfix+"GENES_LOW",      gLoG.padTo(1,".").distinct.sorted.mkString(","));
          //}}
          
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
      initNotice("DROP_STAR_ALLE");
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      outHeader.reportAddedInfos(this)
      
      (addIteratorCloseAction( iter = vcFlatMap(vcIter){v => {
        if(v.alt.length > 2 || (v.alt.length == 2 && v.alt.last != "*")){
          error("ERROR: You must split multiallelics before attempting to drop spanning indel variants!")
          None;
        } else if(v.alt.head == "*"){
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
  class DropNs() extends internalUtils.VcfTool.SVcfWalker { 
    def walkerName : String = "DropVariantsWithNs"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)]();
    
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      var errCt = 0;
      initNotice("DROP_VAR_N");
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      outHeader.reportAddedInfos(this)
      (addIteratorCloseAction( iter = vcFlatMap(vcIter){v => {
        if(v.alt.head.contains('N') || v.ref.contains('N')){
          notice("dropping variant with Ns: "+v.chrom+":"+v.pos+":"+v.ref+">"+v.alt.head,"DROP_VAR_N",5);
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
      
      outHeader.reportAddedInfos(this)
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

  
  /*object StdVcfConverter extends SVcfWalkerInfo {
    def walkerName : String = "StdVcfConverter";
    
    //def infoLines : Seq[SVcfCompoundHeaderLine] = Seq()
    //def formatLines : Seq[SVcfCompoundHeaderLine] = Seq()
    //def otherHeaderLines : Seq[SVcfHeaderLine] = Seq()
    //def walkLines : Seq[SVcfWalkHeaderLine] = Seq()
    //def walkerInfo : SVcfWalkerInfo = StdVcfConverter
  }*/
  
  class StdVcfConverter(cleanHeaderLines : Boolean = true, 
                        cleanInfoFields : Boolean = true, 
                        cleanMetaData : Boolean = true,
                        collapseStarAllele : Boolean = true,
                        deleteUnannotatedFields : Boolean = true,
                        thirdAlleleChar : Option[String] = None,
                        multAlleInfoTag : Option[String] = None,
                        treatOtherAlleleAsRef : Boolean = false) extends internalUtils.VcfTool.SVcfWalker { 
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
    //def walkerInfo : SVcfWalkerInfo = StdVcfConverter;
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
        
        multAlleInfoTag.foreach{ tt => {
          outHeader.addInfoLine(
                new SVcfCompoundHeaderLine("FORMAT",ID=tt,"1","String","Equal to 1 if this line is a multiallelic converted to biallelic.")
              )
        }}
      }
      
      if(cleanHeaderLines){
        outHeader.formatLines.foreach{ fl => {
          if( fl.hasMetadata()){
            outHeader.addMetaLine(fl)
          }
          
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
          if( fl.hasMetadata()){
            outHeader.addMetaLine(fl)
          }
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
      outHeader.reportAddedInfos(this)

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
            multAlleInfoTag.foreach{ tt => {
              vc.addInfo(tt,"1")
            }}
          } else {
            oldFmtLines.foreach{ fl => {
              val fmtIdx = v.format.indexOf(fl.ID);
              if(fmtIdx == -1){
                //do nothing
              } else {
                vc.genotypes.addGenotypeArray( fl.ID +multAlleSuffix, v.genotypes.genotypeValues(fmtIdx).clone() );
              }
            }}
            multAlleInfoTag.foreach{ tt => {
              vc.addInfo(tt,"0")
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

  /*
   * UNFINISHED:
   */
  class collapseStarAllele(ADtypeFields : Seq[String] = Seq("AD"),
                           GTtypeFields : Seq[String] = Seq("GT"),
                           OtherARfields : Seq[String] = Seq(),
                           treatOtherAlleleAsRef : Boolean = false) extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "StdVcfConverter"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
      ("treatOtherAlleleAsRef",treatOtherAlleleAsRef.toString()),
    );
    val talle = if(treatOtherAlleleAsRef){ "0" } else { "." }
    //def walkerInfo : SVcfWalkerInfo = StdVcfConverter;
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
      
        oldFmtLines.foreach{ fl => {
          outHeader.addFormatLine(
            new SVcfCompoundHeaderLine("FORMAT" ,ID = fl.ID + multAlleSuffix, ".", fl.Type, "(For multiallelic variants, an additional value is included in this version to indicate the value for the other alt alleles) "+fl.desc)
          )
        }}
        outHeader.addStatBool("isSplitMultAlleStar",false)
        
        outHeader.addFormatLine(
            new SVcfCompoundHeaderLine("FORMAT" ,ID = "GT" + multAlleSuffix, "1", "String", "Raw GT tag, prior to lossy conversion to universally readable VCF. Note that for split multiallelics there will be three possible alleles, 0, 1, and 2, where 2 represents any or all other alt alleles. FOR MOST PURPOSES, THIS IS THE GT TAG THAT SHOULD BE USED.")
        )
        
        vcfHeader.infoLines.find{ info => info.ID == "GT" }.foreach{ oldGt => {
          outHeader.addFormatLine(
            new SVcfCompoundHeaderLine("FORMAT" ,ID = "GT", "1", "String", "Collapsed GT tag, for back-compatibility with software tools that cannot parse VCF v4.2. For split multiallelic variants the third allele will be collapsed with the ref allele. WARNING: It is NOT recommended that this GT tag be used for most purposes, as multiallelic alt alleles have been simplified. It is preferable to use the "+"GT" + multAlleSuffix+" tag, which may contain 3 possible alleles.")
            )
        }}
        
      
      outHeader.reportAddedInfos(this)

      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        //val vc = v.getOutputLine()
        
        val vc = v.getOutputLine
        //vc.info.withFilter{ case (key,info) => info.getOrElse("miss") == "" }.foreach{ case (tag,info) => {
        //  vc.addInfo(tag,".");
        //}}

        if(v.genotypes.genotypeValues.length > 0 ){
          if(v.alt.length == 2 && v.alt.last == (internalUtils.VcfTool.UNKNOWN_ALT_TAG_STRING)){
            oldFmtLines.foreach( fl => {
              val fmtIdx = v.format.indexOf(fl.ID);
              if(fmtIdx == -1){
                //do nothing
              } else {
                val expectedCt = if(fl.Number == "R"){ 3 } else if(fl.Number == "A"){ 1 } else { 2 }
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
                                     outputTagPrefix : String = OPTION_TAGPREFIX+"STAT_",
                                     variantStatExpression : Option[List[String]] = None,
                                     depthCutoffs : Option[List[Int]] = Some(List(10,20,40)),
                                     groupFile : Option[String] = None, groupList : Option[String] = None, superGroupList : Option[String] = None,
                                     restrictToGroup : Option[String] = None
                                     ) extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "AddStatDistributionTags" 
      val MISS : Int = 0;
      val HOMREF : Int = 1
      val HET : Int = 2
      val HOMALT : Int = 3;
      val OTHER : Int = 4;
      val genoClasses = Seq("Miss","HomRef","Het","HomAlt","Other");
      val genoClassDesc = genoClasses.map{ gc => "Number of samples with the "+gc+" genotype for this variant (based on tag "+tagGT+")" }
     
      
    def walkerParams : Seq[(String,String)] = Seq[(String,String)](
        ("tagAD",tagAD.toString),("tagDP",tagDP.toString),
        ("tagGT",tagGT),
        ("tagSingleCallerAlles",tagSingleCallerAlles.toString),
        ("outputTagPrefix",outputTagPrefix))

    var samps : Seq[String] = null;
    val (sampleToGroupMap,groupToSampleMap,groups) = getGroups(groupFile,groupList,superGroupList);
    val logicParser : internalUtils.VcfTool.SFilterLogicParser[(SVcfVariantLine,Int)] = internalUtils.VcfTool.sGenotypeFilterLogicParser;
    
    /*val genoStatExpressionInfo : List[(String,String,SFilterLogic[(SVcfVariantLine,Int)])] = variantStatExpression match {
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
    }*/
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
      
      val keepsampct = restrictToGroup.map{ rg => {vcfHeader.titleLine.sampleList.toArray.count{ ss => {
            sampleToGroupMap.get(ss).map{ gg => { gg.contains(rg) }}.getOrElse(false)
      }}}}.getOrElse(samps.length);
      
      medianIdx = keepsampct / 2;
      lqIdx = keepsampct / 4;
      uqIdx = math.min(keepsampct-1,math.ceil( keepsampct.toDouble * 3.toDouble / 4.toDouble ).toInt)
      
      //val fixList = Seq("GL","PL","QA","AO","AD");
      
      val hetFracTagDesc = "(Based on tags: "+tagAD.getOrElse(".")+"/"+tagSingleCallerAlles.getOrElse(".")+")"
      
      /*genoClasses.zip(genoClassDesc).foreach{ case (gClass,desc) => {
        outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO" ,//NEWTAGS
                                                         outputTagPrefix+"AD_SAMPCT_"+gClass, 
                                                         "1", 
                                                         "Integer", 
                                                         desc)).addWalker(this)
        )
      }}*/
      
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
      
      /*val groupAnno = fullGroupList.zipWithIndex.map{ case (g,i) => g match {
          case Some(gg) => {
            (g,i,"_GRP_"+gg," for samples in group "+gg,groupToSampleMap(gg).size)
          }
          case None => {
            (g,i,""," across all samples",samps.length)
          }
      }}*/

      /*groupAnno.foreach{ case (grp,i,gtag,groupDesc,groupSize) => {
        genoStatExpressionInfo.foreach{ case (tagCore,descCore,expr) => {
          outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO" ,
                                                         tagCore+gtag, 
                                                         "1", 
                                                         "Integer", 
                                                         descCore+groupDesc
                                                         ))
        }}
      }}*/
      //val nsamp = samps.length;
      //val sampGroupIdx : Vector[Vector[Int]] = samps.toVector.map{ sampid => {
      //    val groupSet : Set[String] = sampleToGroupMap(sampid);
      //    val groupIdx : Vector[Int] = (groups.zipWithIndex.flatMap{ case (g,i) => if(groupSet.contains(g)){ Some(i) } else None } :+ groups.length).toVector;
      //    groupIdx
      //}}
      outHeader.reportAddedInfos(this)
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine()
        vc.dropInfo( overwriteInfos );
        val gtidxRaw = vc.genotypes.fmt.indexOf(splitGtTag);
        val gtIdx = if(gtidxRaw == -1) vc.genotypes.fmt.indexOf(stdGtTag) else gtidxRaw;
        if(gtIdx == -1) {
          warning("Cannot find \""+splitGtTag+"\" or \""+stdGtTag+"\" in fmt line: \""+vc.genotypes.fmt.mkString(",")+"\"","NO_GENO_FIELD_WARNING",25);
        } else {
        val extractSamples : (Array[String] => Array[String]) = restrictToGroup.map{ rg => {
          val ix : Array[Boolean] = vcfHeader.titleLine.sampleList.toArray.map{ ss => {
            sampleToGroupMap.get(ss).map{ gg => { gg.contains(rg) }}.getOrElse(false);
          }}
          (g : Array[String]) => {
            g.zip(ix).filter{ case (gg,tf) => tf }.map{_._1}
          }
        }}.getOrElse({ (g : Array[String]) => {
          g;
        }})
          
        val gt : Vector[String] = extractSamples(v.genotypes.genotypeValues(gtIdx)).toVector;
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
        /*
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
        }}*/
        

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
            val ads = extractSamples(v.genotypes.genotypeValues(adIdx)).map{ a => {
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
              extractSamples(v.genotypes.genotypeValues(dpIdx)).zip(ads).map{ case (d,(r,a)) => {
                if(d == "."){
                  0 
                } else {
                  string2intOpt(d).getOrElse({
                    warning("WARNING: AddStatDistrubutionWalker: Malformed DP string, falling back to sumAD (offending string: \""+d+"\" from tag \""+tagDP.get+"\")\n   VCF Line:"+vc.getSimpleVcfString() ,"MALFORMED_DP",100)
                    r + a
                  })
                }
              }}.sorted
            }
            val depthMedian = sumDepthSorted.lift(medianIdx).map{ _.toString }.getOrElse(".")
            val depthLQ = sumDepthSorted.lift(lqIdx).map{ _.toString }.getOrElse(".")
            val depthUQ = sumDepthSorted.lift(uqIdx).map{ _.toString }.getOrElse(".")
            
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
            
            //genoClasses.zipWithIndex.foreach{ case (c,i) => {
            //  vc.addInfo(outputTagPrefix+"AD_SAMPCT_"+c,sampCts(i).toString);
              //vc.addInfo(outputTagPrefix+"AD_"+c+"_REF",refCts(i));
              //vc.addInfo(outputTagPrefix+"AD_"+c+"_ALT",altCts(i));
              //vc.addInfo(outputTagPrefix+"AD_"+c+"_ALT",altCts(i));
            //}}
            if(sampCts(HET) > 0){
              vc.addInfo(outputTagPrefix+"AD_HET_REF",refCts(HET).toString);
              vc.addInfo(outputTagPrefix+"AD_HET_ALT",altCts(HET).toString);
              vc.addInfo(outputTagPrefix+"AD_HET_FRAC",(math.floor(10000 * altCts(HET).toDouble / (refCts(HET).toDouble+altCts(HET).toDouble)) / 10000.toDouble).toString);
              vc.addInfo(outputTagPrefix+"AD_HET_FRAC_MIN",(math.floor(10000 * hetMinRatio) / 10000.toDouble).toString);
            }
            
          }
        }
        }
        vc

      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
      
    }
  }
  
  
  
  class SAddSampCountWithMultVector(tagID : String, gtTag : String, desc : String, vectorFile : String) extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "SAddGroupInfoAnno"
    def walkerParams : Seq[(String,String)] = Seq[(String,String)]();
        def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
          val sampMultMap : Map[String,Int] = getLinesSmartUnzip(vectorFile).map{ line => line.split("\t")}.map{ cells => (cells(0),string2int(cells(1)))}.toMap.withDefaultValue(0)
          val outHeader = vcfHeader.copyHeader;
          outHeader.addInfoLine( (new SVcfCompoundHeaderLine("INFO" ,tagID, "1", "Integer",desc)).addWalker(this) );
          val sampNames = outHeader.titleLine.sampleList;
          val sampMult = sampNames.map{ ss => {
            sampMultMap(ss)
          }}
          val dropInfoTag = Set(tagID);
          return (vcMap(vcIter)(vc => {
            val vb = vc.getOutputLine();
            vb.dropInfo(dropInfoTag)
            
            val gtidx = vc.genotypes.fmt.indexOf(gtTag);
            val gct = vc.genotypes.genotypeValues(gtidx).map{_.split("/")}.zip(sampMult).map{ case (geno,mm) => {
               if(geno.contains("1")){
                 mm
               } else {
                 0
               }
            }}.sum
            vb.addInfo(tagID, gct.toString);
            vb;
          }),outHeader);
        }
  }
  
  //vcfCodes : VCFAnnoCodes = VCFAnnoCodes(CT_INFIX = tagPrefix.getOrElse(""))
  case class SAddGroupInfoAnno(groupFile : Option[String], groupList : Option[String], superGroupList  : Option[String], chromList : Option[List[String]], 
             addCounts : Boolean = true, addFreq : Boolean = true, addMiss : Boolean = true, 
             addAlle : Boolean= true, addHetHom : Boolean = true, 
             addOtherCountsCalc : Boolean = false,
             sepRef : Boolean = true, countMissing : Boolean = true,
             addMultiHet : Boolean = true,
             noMultiAllelics : Boolean = false,
             GTTag : String = "GT",
             tagPrefix : String = "",
             tagFilter : Option[String] = None,
             tagPreFiltGt : Option[String] = None,
             vcfCodes : VCFAnnoCodes = VCFAnnoCodes(),
             expr : Option[String] = None) extends internalUtils.VcfTool.SVcfWalker {
    
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
          ("addOtherCountsCalc",addOtherCountsCalc.toString),
          ("GTTag",GTTag),
          ("tagPrefix",if(tagPrefix == "") "None" else tagPrefix),
          ("vcfCodesInfix",vcfCodes.CT_INFIX)
        );
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
  
      val (sampleToGroupMap,groupToSampleMap,groups) = getGroups(groupFile,groupList,superGroupList);

      val filterExpr : Option[SFilterLogic[SVcfVariantLine]] = expr.map{filterExpressionString => internalUtils.VcfTool.sVcfFilterLogicParser.parseString( filterExpressionString )}

      
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
          /*List(
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nAF_TAG + gtag, anum, "Float", "The alt allele frequency"+groupDesc+adesc+"."),
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nHomFrq_TAG + gtag, anum, "Float", "The frequency of homalt calls"+groupDesc  +adesc+ "."),
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nHetFrq_TAG + gtag, anum, "Float", "The frequency of het calls"+groupDesc  +adesc+ "."),
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nAltFrq_TAG + gtag, anum, "Float", "The frequency of calls that are het or homalt"+groupDesc  +adesc+ "."),
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nMisFrq_TAG + gtag, "1", "Integer", "The frequency of calls that are missing"+groupDesc  +adesc+ "."),
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nOthFrq_TAG + gtag, "1", "Integer", "The frequency of calls include a different allele"+groupDesc  +adesc+ "."),
            new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nNonrefFrq_TAG + gtag, "1", "Integer", "The frequency of calls include any nonref allele "+groupDesc 
            
            addOtherCountsCalc
            
             +adesc+ ".")
          )*/
          List(new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nAF_TAG + gtag, anum, "Float", "The alt allele frequency"+groupDesc+adesc+"."))++
          (if(addHetHom){
            List(
              new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nHomFrq_TAG + gtag, anum, "Integer", "The frequency of homalt calls"+groupDesc +adesc+ "."),
              new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nHetFrq_TAG + gtag, anum, "Integer", "The frequency of het calls"+groupDesc +adesc+ "."),
              new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nNonrefFrq_TAG + gtag, "1",  "Integer", "The frequency of calls include any nonref allele "+groupDesc  +adesc+ "."),
              new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nOthFrq_TAG + gtag, "1",  "Integer", "The frequency of calls include a different allele"+groupDesc  +adesc+ ".")
            )
          } else { List() }) ++
          (if(addMiss){
            List(
              new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nMisFrq_TAG + gtag, "1", "Integer", "The frequency of calls that are missing"+groupDesc  +adesc+ ".")
            )
          } else { List() }) ++
          (if(addOtherCountsCalc){
            List(
              new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nNMissFrq_TAG + gtag, "1", "Integer", "The frequency of calls that are NOT missing"+groupDesc  +adesc+ "."),
              new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nHomRefFrq_TAG + gtag, "1", "Integer", "The frequency of calls that are homozygous for the REFERENCE allele "+groupDesc  +adesc+ ".")
            )
          } else { List() }) ++
          (if(addAlle){
            List(
              new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nAltFrq_TAG + gtag,   anum, "Integer", "The frequency of calls that include the alt allele"+groupDesc  +adesc+ ".")
            )
          } else { List() }) ++
          (if(addMultiHet){
            List(
              new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nMahFrq_TAG + gtag,   anum, "Integer", "The frequency of genotype calls that include both the alt allele and some other alt allele (multiallelic het) "+groupDesc  +adesc+ ".")
            )
          } else { List() })
          
        } else { List() }) ++ 
        (if(addCounts){
          (if(addHetHom){
            List(
              new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nHomCt_TAG + gtag, anum, "Integer", "The number of homalt calls"+groupDesc +adesc+ "."),
              new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nHetCt_TAG + gtag, anum, "Integer", "The number of het calls"+groupDesc +adesc+ "."),
              new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nNonrefCt_TAG + gtag, "1",  "Integer", "The number of calls include any nonref allele "+groupDesc  +adesc+ "."),
              new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nOthCt_TAG + gtag, "1",  "Integer", "The number of calls include a different allele"+groupDesc  +adesc+ ".")
            )
          } else { List() }) ++
          (if(addMiss){
            List(
              new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nMisCt_TAG + gtag, "1", "Integer", "The number of calls that are missing"+groupDesc  +adesc+ ".")
            )
          } else { List() }) ++
          (if(addOtherCountsCalc){
            List(
              new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nNMissCt_TAG + gtag, "1", "Integer", "The number of calls that are NOT missing"+groupDesc  +adesc+ "."),
              new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nHomRefCt_TAG + gtag, "1", "Integer", "The number of calls that are homozygous for the REFERENCE allele "+groupDesc  +adesc+ ".")
            )
          } else { List() }) ++
          (if(addAlle){
            List(
              new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nAltCt_TAG + gtag,   anum, "Integer", "The number of calls that include the alt allele"+groupDesc  +adesc+ ".")
            )
          } else { List() }) ++
          (if(addMultiHet){
            List(
              new SVcfCompoundHeaderLine("INFO" ,vcfCodes.nMahCt_TAG + gtag,   anum, "Integer", "The number of genotype calls that include both the alt allele and some other alt allele (multiallelic het) "+groupDesc  +adesc+ ".")
            )
          } else { List() })
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
        if((filterExpr.map{fe => fe.keep(vc)}.getOrElse(true))){
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
          //no longer a recommended method:
          //val isSplit = vc.alt.contains(internalUtils.VcfTool.UNKNOWN_ALT_TAG_STRING) && vc.alt.length == 2;
          val numAlt = vc.alt.filter(_ != internalUtils.VcfTool.UNKNOWN_ALT_TAG_STRING).length;
          
          if(numAlt == 1){
            val homCts = Array.fill[Int](fullGroupList.length)(0);
            val hetCts = Array.fill[Int](fullGroupList.length)(0);
            val misCts = Array.fill[Int](fullGroupList.length)(0);
            val othAltCts = Array.fill[Int](fullGroupList.length)(0);
            val othCts = Array.fill[Int](fullGroupList.length)(0);
            val unkCts = Array.fill[Int](fullGroupList.length)(0);
            
            val homRefCts = Array.fill[Int](fullGroupList.length)(0);
            val nMissCts = Array.fill[Int](fullGroupList.length)(0);
            
            //val mahCts = Array.fill[Int](fullGroupList.length)(0);
            
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
              
              if(addOtherCountsCalc){
                if(gt.forall(_ == "0")){
                  var ii = 0;
                  while(ii < groupIdx.length){
                    homRefCts(groupIdx(ii)) += 1;
                    ii += 1;
                  }
                }
                if(gt.exists(_ != ".")){
                  var ii = 0;
                  while(ii < groupIdx.length){
                    nMissCts(groupIdx(ii)) += 1;
                    ii += 1;
                  }
                }
              }
              
              i += 1;
            }
            groupAnno.foreach{case (grp,i,gtag,groupDesc,groupSize) => {
              if(addCounts){
                 if(addHetHom) vb.addInfo(vcfCodes.nHomCt_TAG + gtag, homCts(i).toString);
                 if(addHetHom) vb.addInfo(vcfCodes.nHetCt_TAG + gtag, hetCts(i).toString);
                 if(addMiss) vb.addInfo(vcfCodes.nMisCt_TAG + gtag, misCts(i).toString);
                 if(addHetHom) vb.addInfo(vcfCodes.nOthCt_TAG + gtag, (othCts(i) + othAltCts(i)).toString);
                 if(addAlle) vb.addInfo(vcfCodes.nAltCt_TAG + gtag, (homCts(i) + hetCts(i) + othAltCts(i)).toString);
                 if(addHetHom) vb.addInfo(vcfCodes.nNonrefCt_TAG + gtag, (homCts(i) + hetCts(i) + othAltCts(i) + othCts(i)).toString);
                 if(addMultiHet) vb.addInfo(vcfCodes.nMahCt_TAG + gtag, (othAltCts(i)).toString);
                 if(addOtherCountsCalc){
                   // homRefCts nMissCts
                   vb.addInfo(vcfCodes.nNMissCt_TAG + gtag, nMissCts(i).toString);
                   vb.addInfo(vcfCodes.nHomRefCt_TAG + gtag, homRefCts(i).toString);
                 }
                 if(addHetHom) vb.addInfo(vcfCodes.nHomCt_TAG + gtag, homCts(i).toString);
                 
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
                 if(addHetHom) vb.addInfo(vcfCodes.nHomFrq_TAG + gtag, (homCts(i).toDouble / n).toString);
                 if(addHetHom) vb.addInfo(vcfCodes.nHetFrq_TAG + gtag, (hetCts(i).toDouble / n).toString);
                 if(addMiss) vb.addInfo(vcfCodes.nMisFrq_TAG + gtag, (misCts(i).toDouble / n).toString);
                 if(addHetHom) vb.addInfo(vcfCodes.nOthFrq_TAG + gtag, ((othCts(i) + othAltCts(i)).toDouble / n).toString);
                 if(addAlle) vb.addInfo(vcfCodes.nAltFrq_TAG + gtag, ((homCts(i) + hetCts(i) + othAltCts(i)).toDouble / n).toString);
                 if(addHetHom) vb.addInfo(vcfCodes.nNonrefFrq_TAG + gtag, ((homCts(i) + hetCts(i) + othAltCts(i) + othCts(i)).toDouble / n).toString);
                 if(addMultiHet) vb.addInfo(vcfCodes.nMahFrq_TAG + gtag, ((othAltCts(i)).toDouble / n).toString);
                 if(addOtherCountsCalc){
                   // homRefCts nMissCts
                   vb.addInfo(vcfCodes.nNMissFrq_TAG + gtag, (nMissCts(i).toDouble / n).toString);
                   vb.addInfo(vcfCodes.nHomRefFrq_TAG + gtag, (homRefCts(i).toDouble / n).toString);
                 }
                 
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
            error("Multiallelic group counts are not currently supported!\n" + vc.getSimpleVcfString())
            vb;
          }
        }
        vb
        } else {
          vc
        }
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
                                        
      
   
           
           
ALT VERSION: allows title line!
      groupFile match {
        case Some(gf) => {
          val r = getLinesSmartUnzip(gf).buffered //.drop(1);
          val columnTitles = r.head.split("\t").tail
          if(r.head.startsWith("#sample")){
            report("Detected a title column in the group decoder:\n       \"" + r.head + "\"","note");
            r.next;
          }
          r.foreach(line => {
            val cells = line.split("\t");
            if(cells.length < 2) error("ERROR: group file must have at least 1 column. the sample.ID!");
            val sampid = cells.head
            cells.tail.zip(columnTitles.padTo(cells.tail.length,"")).foreach{ case (c,tt) => {
              if(c != "" && c != "."){
                val ggg = if(c == "1" || c == "0"){
                  tt + c
                } else {
                  c
                }
                groupToSampleMap(ggg) = groupToSampleMap(ggg) + sampid;
                sampleToGroupMap(sampid) = sampleToGroupMap(sampid) + ggg;
                groupSet = groupSet + ggg;
              }
            }}
          })
        }
        case None => {
          //do nothing
        }
      }
           
                   
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
          val r = getLinesSmartUnzip(gf).buffered //.drop(1);
          r.foreach(line => {
            val cells = line.split("\t");
            if(cells.length == 0) error("ERROR: group file must have at least 1 column. the sample.ID!");
            val sampid = cells.head
            cells.tail.filter( c => c != "" && c != "." ).foreach{ case ggg => {
                groupToSampleMap(ggg) = groupToSampleMap(ggg) + sampid;
                sampleToGroupMap(sampid) = sampleToGroupMap(sampid) + ggg;
                groupSet = groupSet + ggg;
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
                             outputTagPrefix : String = OPTION_TAGPREFIX+"SAMPLIST_",
                             printLimit : Option[Int] = None,
                             groupFile : Option[String] = None, groupList : Option[String] = None, superGroupList : Option[String] = None,
                             expr : Option[String] = None
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
      val filterExpr : Option[SFilterLogic[SVcfVariantLine]] = expr.map{filterExpressionString => internalUtils.VcfTool.sVcfFilterLogicParser.parseString( filterExpressionString )}

      val printLim = printLimit.getOrElse(vcfHeader.sampleCt+10);
      
      val outHeader = initVCF(vcfHeader);
      val overwriteInfos = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( outHeader.addedInfos );
      if( overwriteInfos.nonEmpty ){
        notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      }
      //vc.dropInfo(overwriteInfos);
      outHeader.reportAddedInfos(this)
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine()
        vc.dropInfo(overwriteInfos);
        
        val gtIdx = v.genotypes.fmt.indexOf(tagGT);
        if(gtIdx == -1 || (! filterExpr.map{fe => fe.keep(vc)}.getOrElse(true))){
          warning("Missing genotype tag ("+gtIdx+")","MISSING_GT_TAG",10);
        } else {
          val gt : Vector[Array[String]] = v.genotypes.genotypeValues(gtIdx).toVector.map{ g => g.split("[/|]") }
          val simpleClass : Vector[Int] = gt.map{ genoString => {
            if(genoString.contains(".")){
              OTHER
            } else if(genoString.contains("1")){
              if(genoString.contains("0")){
                HET
              } else if(genoString.contains("2")){
                MALLE
              } else{
                HOMALT
              }
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
              if(s.length <= printLim){
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
                vc.addInfo(outputTagPrefix+""+gClass,"TOO_MANY_TO_PRINT,"+s.length+","+s.take(printLim).mkString(","));
                groups.foreach( grp => {
                  val tag = outputTagPrefix+"GRP_"+grp+"_"+gClass
                  val groupSet = groupToSampleMap(grp);
                  val gss = s.filter{ss => groupSet.contains(ss)}
                  if(gss.length > printLim){
                    vc.addInfo(tag,"TOO_MANY_TO_PRINT,"+gss.length+","+gss.take(printLim).mkString(","));
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
      outHeader.reportAddedInfos(this)
      (addIteratorCloseAction( iter = vcIter.flatMap{v => {
        val vc = v.getOutputLine()
        vc.in_chrom = translateChrom(vc.in_chrom);
        Some(vc)
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
      
    }
  }
    
  case class ChromosomeConverterAdv(chromDecoder : String, 
                 fromToColumnNames : Option[(String,String)] = None,
                 fromToIdx : Option[(String,String)] = None,
                 skipFirstRow : Boolean = true,
                       quiet : Boolean = false) extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "ConvertChromosomeName"
    val rawcelllist = getLinesSmartUnzip(chromDecoder).map{ line => line.split("\t") }.toVector;
    val celllist = if(skipFirstRow){
      rawcelllist.tail
    } else {
      rawcelllist
    }
    
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](("chromDecoder",chromDecoder.toString),
                                                                    ("fromToColumnNames",fromToColumnNames.map{ case (a,b) => a+"_to_"+b}.getOrElse("None")),
                                                                    ("fromToIdx",fromToIdx.map{ case (a,b) => a+"_to_"+b}.getOrElse("None")),
                                                                    ("skipFirstRow",skipFirstRow.toString),
                                                                    ("quiet",quiet.toString)
                                                                    );
    
    val chromMap : Map[String,String] = fromToColumnNames.map{ case (f,t) => {
      val headerLine = celllist.head;
      val fromIDX = celllist.indexOf(f);
      val toIDX = celllist.indexOf(t);
      if(fromIDX == -1){
        error("Error in ConvertChromosomeName: Cannot find column named \""+f+"\""+" in file: \""+chromDecoder+"\"\nColumns found are:[\""+ headerLine.mkString( "\",\"" ) +"\"");
      }
      if(toIDX == -1){
        error("Error in ConvertChromosomeName: Cannot find column named \""+t+"\""+" in file: \""+chromDecoder+"\"\nColumns found are:[\""+ headerLine.mkString( "\",\"" ) +"\"");
      }
      celllist.tail.map{ cells => {
        ( cells(fromIDX), cells(toIDX) )
      }}.toMap
    }}.getOrElse({
      fromToIdx.map{ case (f,t) => {
        val fromIDX = string2intOpt(f);
        val toIDX = string2intOpt(t);
        if( fromIDX.isEmpty ){
          error("Error in ConvertChromosomeName: column IDX \""+f+"\" is not an integer! to identify a column via a title line, use the columnNames parameter!")
        }
        if( toIDX.isEmpty ){
          error("Error in ConvertChromosomeName: column IDX \""+t+"\" is not an integer! to identify a column via a title line, use the columnNames parameter!")
        }
        celllist.map{ cells => {
          ( cells(fromIDX.get), cells(toIDX.get) )
        }}.toMap
      }}.getOrElse({
          error("ConvertChromosomeName must specify which columns to convert to/from, using either the columnNames or columnIdx parameters.");
          null;
      })
    })
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
      outHeader.reportAddedInfos(this)
      (addIteratorCloseAction( iter = vcIter.flatMap{v => {
        val vc = v.getOutputLine()
        vc.in_chrom = translateChrom(vc.in_chrom);
        Some(vc)
      }}, closeAction = (() => {
        //do nothing
      })),outHeader)
      
    }
  }

  case class SampleRenameAdv(decoder : String, 
                 fromToColumnNames : Option[(String,String)] = None,
                 fromToIdx : Option[(String,String)] = None,
                 skipFirstRow : Boolean = true,
                       quiet : Boolean = false) extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "SampleRename"
    val rawcelllist = getLinesSmartUnzip(decoder).map{ line => line.split("\t") }.toVector;
    val celllist = if(skipFirstRow){
      rawcelllist.tail
    } else {
      rawcelllist
    }
    
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](("decoder",decoder.toString),
                                                                    ("fromToColumnNames",fromToColumnNames.map{ case (a,b) => a+"_to_"+b}.getOrElse("None")),
                                                                    ("fromToIdx",fromToIdx.map{ case (a,b) => a+"_to_"+b}.getOrElse("None")),
                                                                    ("skipFirstRow",skipFirstRow.toString),
                                                                    ("quiet",quiet.toString)
                                                                    );
    
    val sampMap : Map[String,String] = fromToColumnNames.map{ case (f,t) => {
      val headerLine = celllist.head;
      val fromIDX = headerLine.indexOf(f);
      val toIDX = headerLine.indexOf(t);
      if(fromIDX == -1){
        error("Error in SampleRename: Cannot find column named \""+f+"\""+" in file: \""+decoder+"\"\nColumns found are:[\""+ headerLine.mkString( "\",\"" ) +"\"");
      }
      if(toIDX == -1){
        error("Error in SampleRename: Cannot find column named \""+t+"\""+" in file: \""+decoder+"\"\nColumns found are:[\""+ headerLine.mkString( "\",\"" ) +"\"");
      }
      celllist.tail.map{ cells => {
        ( cells(fromIDX), cells(toIDX) )
      }}.toMap
    }}.getOrElse({
      fromToIdx.map{ case (f,t) => {
        val fromIDX = string2intOpt(f);
        val toIDX = string2intOpt(t);
        if( fromIDX.isEmpty ){
          error("Error in ConvertChromosomeName: column IDX \""+f+"\" is not an integer! to identify a column via a title line, use the columnNames parameter!")
        }
        if( toIDX.isEmpty ){
          error("Error in ConvertChromosomeName: column IDX \""+t+"\" is not an integer! to identify a column via a title line, use the columnNames parameter!")
        }
        celllist.map{ cells => {
          ( cells(fromIDX.get), cells(toIDX.get) )
        }}.toMap
      }}.getOrElse({
          error("ConvertChromosomeName must specify which columns to convert to/from, using either the columnNames or columnIdx parameters.");
          null;
      })
    })
    def translateSamp(c : String) : String = {
      sampMap.get(c) match {
        case Some(tc) => {
          if(! quiet) warning("CONVERTING SAMPLEID: \""+c+"\"->\""+tc+"\"","SAMPNAME_CHANGED",100);
          tc
        }
        case None => {
          if(! quiet) warning("Could not find samp: "+c,"SAMPNAME_NOT_CHANGED",100);
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
      outHeader.reportAddedInfos(this)
      outHeader.titleLine = SVcfTitleLine(outHeader.titleLine.sampleList.map{ s => {
        translateSamp(s);
      }})
      (vcIter,outHeader)
      
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
                                         valueName = OPTION_TAGPREFIX+"SINGLETON_ID",  
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
                                         valueName = OPTION_TAGPREFIX+"SINGLETON_ID",  
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
  case class StripGenotypeData(addDummyGenotypeColumn : Boolean = false, columnID : String = "DUMMYVAR") extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "StripGenotypeData"
    def walkerParams : Seq[(String,String)] = Seq[(String,String)]()
    def walkVCF(vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      
      outHeader.addFormatLine(new SVcfCompoundHeaderLine("FORMAT",ID="GT",Number="1",Type="String",desc="dummy GT column, always equal to het."));
      //                new  SVcfCompoundHeaderLine("INFO",ID=newid,Number = f.Number, Type = f.Type,desc = f.desc);

      outHeader.titleLine = if(addDummyGenotypeColumn){
        SVcfTitleLine(Seq[String](columnID))
      }else {
        SVcfTitleLine(Seq[String]())
      }
      
      val gs = SVcfGenotypeSet.getGenotypeSet(Seq[String]("0/1"),Seq[String]("GT"))
      
      return ( vcMap(vcIter)(vc => {
        val vb = vc.getLazyOutputLine()
        vb.removeGenotypeInfo();
        vb.in_genotypes = gs.copyGenotypeSet();
        vb;
      }), outHeader)
    }
    
  }
      
  case class FilterTags(keepGenotypeTags : Option[List[String]] = None, 
                        dropGenotypeTags : List[String] = List[String](), 
                        keepInfoTags : Option[List[String]] = None, 
                        dropInfoTags : List[String] = List[String](),
                        dropAsteriskAlleles : Boolean = false,
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
      
      def makeRegexFunc(h : Option[List[String]], d: Boolean) : Option[(String => Boolean)] ={
        h.map{k => {
          val rx = k.map{ kk => java.util.regex.Pattern.compile(kk) }
          ((s : String) => {
            rx.exists( rr => rr.matcher(s).matches() )
          })
        }}
      }
      
      val kirFunc = makeRegexFunc(keepInfoRegex,true)
      val dirFunc = makeRegexFunc(dropInfoRegex,false)
      val kgrFunc = makeRegexFunc(keepGenoRegex,true)
      val dgrFunc = makeRegexFunc(dropGenoRegex,false)
      
      val infoTags = vcfHeader.infoLines.map{infoline => infoline.ID}.toSet;
      val genoTags = vcfHeader.formatLines.map{infoline => infoline.ID}.toSet;

      val keepInfoSet = infoTags.filter{ i => keepInfoTags.map{ kit => kit.contains(i) }.getOrElse(true) && (! (dropInfoTags.contains(i))) }
      val keepGenoSet = genoTags.filter{ i => keepGenotypeTags.map{ kit => kit.contains(i)}.getOrElse(true) && (! dropGenotypeTags.contains(i))}
      /*
      val keepInfoSet = keepInfoTags match {
        case Some(kit) => {
          infoTags.filter{ i => kit.contains(i) && (! (dropInfoTags.contains(i))) }
        }
        case None => {
          infoTags.filter{ i => kit.contains(i) && (! (dropInfoTags.contains(i))) }
        }
      }*/
      
      /*
      val keepInfoSet = keepInfoTags match {
        case Some(kit) => {
            infoTags.filter{ i => (kit.contains(i) || 
                                       (
                                           kirFunc.map{ kk => { kk(i) } }.getOrElse(false) || 
                                           dirFunc.map{ kk => ! kk(i)}.getOrElse(false)
                                       )
                                  ) && (! (dropInfoTags.contains(i)))}.toSet
          }
        }
        case None => {
          infoTags.filter{ i => ((kirFunc(i) && (! dirFunc(i))) ) && (! (dropInfoTags.contains(i)))}.toSet
        }
      }
      val keepGenoSet = keepGenotypeTags match {
        case Some(kit) => {
          genoTags.filter{ i => (kit.contains(i) || (kgrFunc(i) && (! dgrFunc(i))) ) && (! dropGenotypeTags.contains(i))}.toSet
        }
        case None => {
          genoTags.filter{ i => (                   (kgrFunc(i) && (! dgrFunc(i))) ) && (! dropGenotypeTags.contains(i))}.toSet
        }
      }*/
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
            if(cells.length != 2) error("FORMAT.rename must consist of comma delimited list of pairs, with each pair separated by a colon (from:to)");
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
            if(cells.length != 2) error("INFO.rename must consist of comma delimited pairs, with each pair separated by a colon (from:to)");
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
      outHeader.reportAddedInfos(this)
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
                    gv.split("[\\|/]").sortBy{ gg => string2intOpt(gg) }.mkString("/");
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
                                         valueName = OPTION_TAGPREFIX+"SINGLETON_ID",  
                                         argDesc =  "",
                                         defaultValue = Some(OPTION_TAGPREFIX+"SINGLETON_ID")
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

  case class ExtractSingletons(gttag : String = "GT", infoTag : String = OPTION_TAGPREFIX+"SINGLETON_ID", dropNonSingletons : Boolean = true, dropGenotypes : Boolean = true,
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
      
      val bedIdTag = OPTION_TAGPREFIX+"INTERVAL_IVID"
      
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
      outHeader.reportAddedInfos(this)
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
      outHeader.reportAddedInfos(this)
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

  case class AddMafSummary(ctrlAlleFreqKeys : Seq[String] = Seq("1KG_AF","ESP_EA_AF","ExAC_ALL",OPTION_TAGPREFIX+"AF_GRP_CTRL"),
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
        
        val maxAF : Double = ctrlAlleFreqKeys.map{key => v.info.getOrElse(key,None).getOrElse("0")}.map{s => if(! (scala.util.control.Exception.allCatch opt s.toDouble).isDefined) 0.toDouble else string2double(s)}.max;
        vb.addInfo(maxAfTag , maxAF.toString);
        vb;
      }},newHeader);
      
    }
  }
  case class AddComplexMafSummary(ctrlAlleFreqKeys : Seq[String] = Seq("1KG_AF","ESP_EA_AF","ExAC_ALL",OPTION_TAGPREFIX+"AF_GRP_CTRL"),
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
  


  case class HomopolymerAdjacency(bedFile : String, bedIdx : String, tag : String, desc : String) extends SVcfWalker {
    def walkerName : String = "homopolymerAdjacency."+tag;
    
    def walkerParams : Seq[(String,String)]=  Seq[(String,String)](
          ("bedFile","\""+bedFile+"\""),
          ("tag",tag),
          ("desc",desc)
     )
    var currChrom : String=  "???"
    var arr : internalUtils.genomicAnnoUtils.GenomicArrayOfSets[String] = internalUtils.genomicAnnoUtils.GenomicArrayOfSets[String](false);
    
    val initReader = getLinesSmartUnzip(bedFile).buffered
    skipWhile(initReader)(_.startsWith("#"));
    
    val isGtf = bedFile.toUpperCase().endsWith(".GTF") || bedFile.toUpperCase().endsWith(".GTF.GZ") || bedFile.toUpperCase().endsWith(".GTF.ZIP");
    //WARNING: GTF maybe not implemented?
    val lines : BufferedIterator[String] = initReader.buffered
    val isComplexBed = (! isGtf) && (lines.head.split("\t").length >= 12);
    
    val tabixReader = new htsjdk.tribble.readers.TabixReader(bedFile,bedIdx)
    
    def getCellIterator( itr : htsjdk.tribble.readers.TabixReader.Iterator ) : Iterator[Array[String]] = {
      (new Iterator[String] {
        var buf : Option[String] = Option(itr.next())
        def hasNext : Boolean = buf.isDefined;
        def next : String = {
          val out = buf
          buf = Option(itr.next());
          out.get
        }
      }).map{ _.split("\t") }.buffered
    }
    def getIvIterator( cellIterator : Iterator[Array[String]] ) : Iterator[(internalUtils.commonSeqUtils.GenomicInterval,String)] = {
          cellIterator.map(cells => {
            val (chrom,start,end) = (cells(0),string2int(cells(1)),string2int(cells(2)))
            (internalUtils.commonSeqUtils.GenomicInterval(chrom, '.', math.max(start,0),end),cells(3))
          })
    }
    
    def loadChrom(chrom : String){
      if(chrom != currChrom){
        //notice("Loading chromosome "+chrom+" in bed file " +
        reportln("Loading chromosome "+chrom+" from bed file "+bedFile+" ["+internalUtils.stdUtils.getDateAndTimeString+"]","debug");
        arr = internalUtils.genomicAnnoUtils.GenomicArrayOfSets[String](false);
        getIvIterator(getCellIterator( tabixReader.query(chrom) )).foreach{ case (iv,n) => {
          arr.addSpan(iv, n);
        }}
        arr.finalizeStepVectors;
        reportln("Finished loading chromosome "+chrom+" from bed file "+bedFile+" ["+internalUtils.stdUtils.getDateAndTimeString+"]","deepDebug");
      }
    }

    /*def bedFunc(iv : internalUtils.commonSeqUtils.GenomicInterval) : String = {
        if(style == "+"){
                    if(arr.findIntersectingSteps(iv).exists{ case (iv,currSet) => ! currSet.isEmpty }) "1" else "0"
        } else if(style == "-"){
                    if(arr.findIntersectingSteps(iv).exists{ case (iv,currSet) => ! currSet.isEmpty }) "0" else "1"
        } else {
                    arr.findIntersectingSteps(iv).flatMap{ case (iv,currSet) => currSet }.toVector.distinct.sorted.padTo(1,".").mkString(",")
        }
        
    }*/

    def walkVCF(vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      val newHeader = vcfHeader.copyHeader
      /*if(style != "+" && style != "-"){
        newHeader.addInfoLine(new  SVcfCompoundHeaderLine(in_tag = "INFO", ID = tag, Number = ".", Type = "String", desc = desc));
      } else {
        newHeader.addInfoLine(new  SVcfCompoundHeaderLine(in_tag = "INFO", ID = tag, Number = "1", Type = "Integer", desc = desc));
      }*/
      newHeader.addWalk(this);
      newHeader.addInfoLine(new  SVcfCompoundHeaderLine(in_tag = "INFO", ID = tag, Number = ".", Type = "String", desc = "Indicates whether variant is an extension or deletion of a homopolymer run. "+ desc));
      
      (vcMap(vcIter){v => {
        var vb = v.getLazyOutputLine()
        loadChrom(v.chrom);
        //val out = bedFunc(vb.variantIV);
        if(v.ref.length < v.alt.head.length){
          val newBase = v.ref.last;
          val endIV = internalUtils.commonSeqUtils.GenomicInterval(v.chrom,'.', start = v.pos-1, end = v.pos + math.max(1,v.ref.length)+1); 
          val ixSteps = arr.findIntersectingSteps(endIV).filter{ case (iv,currSet) => ! currSet.isEmpty }.toVector
          if(ixSteps.nonEmpty && ixSteps.head._2.head.head == newBase){
            vb.addInfo(tag,"DEL-"+newBase);
          } else if(ixSteps.nonEmpty){
            vb.addInfo(tag,"NEAR-"+newBase);
          }
        } else {
          val newBase = v.alt.head.last;
          val endIV = internalUtils.commonSeqUtils.GenomicInterval(v.chrom,'.', start = v.pos + math.max(1,v.ref.length), end = v.pos + math.max(1,v.ref.length)+1); 
          val ixSteps = arr.findIntersectingSteps(endIV).filter{ case (iv,currSet) => ! currSet.isEmpty }.toVector
          if(ixSteps.nonEmpty && ixSteps.head._2.head.head == newBase){
            vb.addInfo(tag,"ADD-"+newBase);
          } else if(ixSteps.nonEmpty){
            vb.addInfo(tag,"NEAR-"+newBase);
          }
        }
        
        //vb.addInfo(tag,bedFunc(vb.variantIV));
        vb
      }},newHeader)
   }
    
  }
  
  case class AddIdxBedFile(bedFile : String, bedIdx : String, tag : String, bufferDist : Int, desc : String, chromList : Option[List[String]], style : String = "+") extends SVcfWalker {
    def walkerName : String = "AddBedAnno."+tag;
    
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
    var currChrom : String=  "???"
    var arr : internalUtils.genomicAnnoUtils.GenomicArrayOfSets[String] = internalUtils.genomicAnnoUtils.GenomicArrayOfSets[String](false);
    
    val initReader = getLinesSmartUnzip(bedFile).buffered
    skipWhile(initReader)(_.startsWith("#"));
    
    val isGtf = bedFile.toUpperCase().endsWith(".GTF") || bedFile.toUpperCase().endsWith(".GTF.GZ") || bedFile.toUpperCase().endsWith(".GTF.ZIP");
    //WARNING: GTF maybe not implemented?
    val lines : BufferedIterator[String] = initReader.buffered
    val isComplexBed = (! isGtf) && (lines.head.split("\t").length >= 12);
    
    val tabixReader = new htsjdk.tribble.readers.TabixReader(bedFile,bedIdx)
    
    def getCellIterator( itr : htsjdk.tribble.readers.TabixReader.Iterator ) : Iterator[Array[String]] = {
      (new Iterator[String] {
        var buf : Option[String] = Option(itr.next())
        def hasNext : Boolean = buf.isDefined;
        def next : String = {
          val out = buf
          buf = Option(itr.next());
          out.get
        }
      }).map{ _.split("\t") }.buffered
    }
    def getIvIterator( cellIterator : Iterator[Array[String]] ) : Iterator[(internalUtils.commonSeqUtils.GenomicInterval,String)] = {
        if(isGtf){
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
    }
    
    def loadChrom(chrom : String){
      if(chrom != currChrom){
        //notice("Loading chromosome "+chrom+" in bed file " +
        reportln("Loading chromosome "+chrom+" from bed file "+bedFile+" ["+internalUtils.stdUtils.getDateAndTimeString+"]","debug");
        arr = internalUtils.genomicAnnoUtils.GenomicArrayOfSets[String](false);
        getIvIterator(getCellIterator( tabixReader.query(chrom) )).foreach{ case (iv,n) => {
          arr.addSpan(iv, n);
        }}
        arr.finalizeStepVectors;
        currChrom = chrom;
        reportln("Finished loading chromosome "+chrom+" from bed file "+bedFile+" ["+internalUtils.stdUtils.getDateAndTimeString+"]","deepDebug");
      }
    }

    def bedFunc(iv : internalUtils.commonSeqUtils.GenomicInterval) : String = {
        if(style == "+"){
                    if(arr.findIntersectingSteps(iv).exists{ case (iv,currSet) => ! currSet.isEmpty }) "1" else "0"
        } else if(style == "-"){
                    if(arr.findIntersectingSteps(iv).exists{ case (iv,currSet) => ! currSet.isEmpty }) "0" else "1"
        } else {
                    arr.findIntersectingSteps(iv).flatMap{ case (iv,currSet) => currSet }.toVector.distinct.sorted.padTo(1,".").mkString(",")
        }
    }

    def walkVCF(vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      val newHeader = vcfHeader.copyHeader
      if(style != "+" && style != "-"){
        newHeader.addInfoLine(new  SVcfCompoundHeaderLine(in_tag = "INFO", ID = tag, Number = ".", Type = "String", desc = desc));
      } else {
        newHeader.addInfoLine(new  SVcfCompoundHeaderLine(in_tag = "INFO", ID = tag, Number = "1", Type = "Integer", desc = desc));
      }
      newHeader.addWalk(this);
      
      (vcMap(vcIter){v => {
        var vb = v.getLazyOutputLine()
        loadChrom(v.chrom);
        val out = bedFunc(vb.variantIV);
        //if(out == "1"){
          
        //}
        vb.addInfo(tag,bedFunc(vb.variantIV));
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
    
    
    val bufferDist = bufferDistOpt.getOrElse(0);
    
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
  
  case class PassThroughSVcfWalker(name : String = "nullWalker", processLine : Boolean = false, groupLines : Boolean = false,params : Seq[(String,String)] = Seq[(String,String)]()) extends SVcfWalker {
    def walkerName : String = name;
        def walkerParams : Seq[(String,String)]=  Seq[(String,String)](
          ("name",name)
        ) ++ (
           if(processLine || groupLines){
             if(processLine && groupLines){
               Seq(("processingSteps","processAndGroup"))
             } else if(processLine){
               Seq(("processingSteps","process"))
             } else {
               Seq(("processingSteps","group"))
             }
           } else {
             Seq();
           }
        ) ++ params 
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
        val filtLen = vc.alt.filter(x => x != "." && x != internalUtils.VcfTool.UNKNOWN_ALT_TAG_STRING).length
        if(filtLen > 1){
          error("ERROR: must split multiallelics before attempting to drop nonvariant lines!");
          false
        } else if(filtLen == 0){
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
 /* case class EndWarningBufferedIterator[A]( ii : Iterator[A] ) extends Iterator[A] {
     val iter : BufferedIterator = BufferedIterator(ii);
     val isNonEmpty : Boolean = iter.hasNext;
     var onLast : Boolean = false;
     var thisHasNext : Boolean = isNonEmpty;
     val buffer : A = if( isNonEmpty ){
       val b = iter.next;
       onLast = iter.hasNext;
       b;
     } else {
       null;
     }
     def hasNext : Boolean = thisHasNext;
     def next : A = {
       
     }
     def head : A = iter.head;
  }*/

  def foldIterator[A,B]( iter : Iterator[A] )( init : B )( fcn : ( (A,B) => ( Iterator[A], B ) ) )( closer : ( B => Iterator[A] )  ) : Iterator[A] = {
    new Iterator[A]{
      var buffer : Iterator[A] = Iterator[A]();
      var runner : B = init;
      var isClosed : Boolean = false;
      def hasNext : Boolean = buffer.hasNext || {
        while( (! buffer.hasNext) && iter.hasNext ){
          val iab = fcn( iter.next, runner );
          buffer = iab._1;
          runner = iab._2;
          //(buffer,runner) = fcn( iter.next, runner );
        }
        if((! buffer.hasNext) && ( !isClosed )){
          buffer = closer(runner);
          isClosed = true;
        }
        buffer.hasNext;
      }
      def next : A = {
        if(buffer.hasNext){
          buffer.next
        } else {
          while( (! buffer.hasNext) && iter.hasNext ){
            val iab = fcn( iter.next, runner );
            buffer = iab._1;
            runner = iab._2;
          }
          if((! buffer.hasNext) && ( !isClosed )){
            buffer = closer(runner);
            isClosed = true;
          }
          next;
        }
      }
    }
  }
  
  //, isPositionWindowSize : Boolean = false
  case class RunningWindowSort(windowSize : Int = 10000) extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "RunningWindowSort"
    val isPositionWindowSize = false
    //if( variantWindowSize.isEmpty && positionWindowSize.isEmpty){
    //  error("Input parameter error: RunningWindowSort: you must specify either variantWindowSize or positionWindowSize.")
    //}
    
    def walkerParams : Seq[(String,String)] = Seq[(String,String)](
        (if( ! isPositionWindowSize ){
          ("variantWindowSize",""+windowSize)
        } else {
          ("positionWindowSize",""+windowSize)
        })
    )
/*
         if(buffer.contains(k)){
          buffer = buffer + ((k,buffer(k) :+ (v)))
        } else {
          buffer = buffer + ((k,Vector[A](v)))
        }
 */
    def walkVCF(vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader;
      outHeader.addWalk(this);
      val overwriteInfos : Set[String] = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( outHeader.addedInfos );
      if( overwriteInfos.nonEmpty ){
        notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      }
      outHeader.reportAddedInfos(this)
      
      //var buffer = new scala.collection.immutable.TreeMap[Int,Vector[A]]();
      
      (foldIterator(vcIter)( new scala.collection.immutable.TreeMap[Int,Vector[SVcfVariantLine]]() ){ case ( v : SVcfVariantLine, vmap : scala.collection.immutable.TreeMap[Int,Vector[SVcfVariantLine]] ) => {
        if( vmap.size > 0 && v.chrom != vmap.head._2.head.chrom ){
          (vmap.map{ case (p,v) => { v }}.flatten.iterator,
           new scala.collection.immutable.TreeMap[Int,Vector[SVcfVariantLine]]() + ((v.pos,Vector(v)))  )
        } else if( vmap.contains( v.pos ) ){
          ( Seq[SVcfVariantLine]().iterator, vmap + ((v.pos,vmap(v.pos) :+ (v) )) )
        } else if( vmap.size < windowSize ){
          ( Seq[SVcfVariantLine]().iterator, vmap + ((v.pos, Vector(v) )) )
        } else {
          (vmap.head._2.iterator,vmap.tail + ((v.pos, Vector(v) )) )
        }
      }}{ vmap => {
        vmap.map{ case (p,vv) => { vv } }.toVector.flatten.iterator
      }}
      ,outHeader)
    }
  }
  
    /*
  case class MergeMultiNucleotideVariants(splitPrefix : String = "" ,sizeLimit: Option[Int] = None) extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "MergeMultiNucleotideVariants"
    def walkerParams : Seq[(String,String)] = Seq[(String,String)](
        ( "sizeLimit",sizeLimit.getOrElse("None"))
     )

    def walkVCF(vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader;
      outHeader.addWalk(this);
      
      outHeader.addInfoLine(new SVcfCompoundHeaderLine(in_tag="INFO",ID=splitPrefix+"multiNucleoMerge",Number="1",Type="Integer",desc="Will be 1 if the variant was originally coded as a multi-nucleotide variant."));
      outHeader.addInfoLine(new SVcfCompoundHeaderLine(in_tag="INFO",ID=splitPrefix+"multiNucleoSplitCt",Number="1",Type="Integer",desc="If the variant was originally coded as a multi-nucleotide variant, this will be the number of nucleotides."));
      outHeader.addInfoLine(new SVcfCompoundHeaderLine(in_tag="INFO",ID=splitPrefix+"multiNucleoSplitIdx",Number="1",Type="Integer",desc="If the variant was originally coded as a multi-nucleotide variant, this will be the position of this variant in the multinucleotide split."));
      outHeader.addInfoLine(new SVcfCompoundHeaderLine(in_tag="INFO",ID=splitPrefix+"multiNucleoSplitPos",Number="1",Type="Integer",desc="If the variant was originally coded as a multi-nucleotide variant, this will be the genomic position of the start of the original multinucleotide variant."));
      outHeader.addInfoLine(new SVcfCompoundHeaderLine(in_tag="INFO",ID=splitPrefix+"multiNucleoSplitREF",Number="1",Type="Integer",desc="If the variant was originally coded as a multi-nucleotide variant, this will be the original REF allele."));
      outHeader.addInfoLine(new SVcfCompoundHeaderLine(in_tag="INFO",ID=splitPrefix+"multiNucleoSplitALT",Number="1",Type="Integer",desc="If the variant was originally coded as a multi-nucleotide variant, this will be the original ALT allele."));

      val overwriteInfos : Set[String] = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( outHeader.addedInfos );
      if( overwriteInfos.nonEmpty ){
        notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      }
      //vc.dropInfo(overwriteInfos);
       outHeader.reportAddedInfos(this)
      
      //            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.txList_TAG, ".", "String", "List of known transcripts found to overlap with the variant."),

      tally("isMNP",0)
      tally("isDNP",0)
      tally("isMNPgt2",0)

       
      (vcIter.flatMap{v => {
        
        if( v.alt.length == 1 & v.ref.length > 1 & v.ref.length == v.alt(0).length ){
          v.ref.zip(v.alt(0)).zipWithIndex.toSeq.map{ case ((r,a),i) => {
            val vc = v.getOutputLine()
            vc.in_pos = v.pos + i;
            vc.in_ref = r+"";
            vc.in_alt = Seq(a+"");
            vc.in_genotypes = v.genotypes.deepCopyGenotypeSet();
            vc.addInfo(splitPrefix+"multiNucleoSplit","1");
            vc.addInfo(splitPrefix+"multiNucleoSplitCt",""+v.ref.length);
            vc.addInfo(splitPrefix+"multiNucleoSplitIdx",""+i);
            vc.addInfo(splitPrefix+"multiNucleoSplitPos",""+v.pos);
            vc.addInfo(splitPrefix+"multiNucleoSplitREF",""+v.ref);
            vc.addInfo(splitPrefix+"multiNucleoSplitALT",""+v.alt(0));
            tally("isMNP",1)
            if( v.ref.length == 2 ){
              tally("isDNP",1)
            } else {
              tally("isMNPgt2",1)
            }
            tally("MNP.len."+v.ref.length,1)
            
            vc
          }}
        } else {
          val vc = v.getOutputLine()
          vc.addInfo(splitPrefix+"multiNucleoSplit","0");
          Seq(vc);
        }
      }},outHeader)
    }
  }*/
  
  case class SSplitMultiNucleotideVariants(splitPrefix : String = "" ,dnpOnly : Boolean = false) extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "SSplitMultiNucleotideVariants"
    def walkerParams : Seq[(String,String)] = Seq[(String,String)]()

    def walkVCF(vcIter : Iterator[SVcfVariantLine],vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
      val outHeader = vcfHeader.copyHeader;
      outHeader.addWalk(this);
      
      outHeader.addInfoLine(new SVcfCompoundHeaderLine(in_tag="INFO",ID=splitPrefix+"multiNucleoSplit",Number="1",Type="Integer",desc="Will be 1 if the variant was originally coded as a multi-nucleotide variant."));
      outHeader.addInfoLine(new SVcfCompoundHeaderLine(in_tag="INFO",ID=splitPrefix+"multiNucleoSplitCt",Number="1",Type="Integer",desc="If the variant was originally coded as a multi-nucleotide variant, this will be the number of nucleotides."));
      outHeader.addInfoLine(new SVcfCompoundHeaderLine(in_tag="INFO",ID=splitPrefix+"multiNucleoSplitIdx",Number="1",Type="Integer",desc="If the variant was originally coded as a multi-nucleotide variant, this will be the position of this variant in the multinucleotide split."));
      outHeader.addInfoLine(new SVcfCompoundHeaderLine(in_tag="INFO",ID=splitPrefix+"multiNucleoSplitPos",Number="1",Type="Integer",desc="If the variant was originally coded as a multi-nucleotide variant, this will be the genomic position of the start of the original multinucleotide variant."));
      outHeader.addInfoLine(new SVcfCompoundHeaderLine(in_tag="INFO",ID=splitPrefix+"multiNucleoSplitREF",Number="1",Type="String",desc="If the variant was originally coded as a multi-nucleotide variant, this will be the original REF allele."));
      outHeader.addInfoLine(new SVcfCompoundHeaderLine(in_tag="INFO",ID=splitPrefix+"multiNucleoSplitALT",Number="1",Type="String",desc="If the variant was originally coded as a multi-nucleotide variant, this will be the original ALT allele."));

      val overwriteInfos : Set[String] = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( outHeader.addedInfos );
      if( overwriteInfos.nonEmpty ){
        notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      }
      //vc.dropInfo(overwriteInfos);
       outHeader.reportAddedInfos(this)
      
      //            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.txList_TAG, ".", "String", "List of known transcripts found to overlap with the variant."),

      tally("isMNP",0)
      tally("isDNP",0)
      tally("isMNPgt2",0)

       
      (vcIter.flatMap{v => {
        
        if( v.alt.length == 1 & v.ref.length > 1 & v.ref.length == v.alt(0).length ){
          v.ref.zip(v.alt(0)).zipWithIndex.toSeq.map{ case ((r,a),i) => {
            val vc = v.getOutputLine()
            vc.in_pos = v.pos + i;
            vc.in_ref = r+"";
            vc.in_alt = Seq(a+"");
            vc.in_genotypes = v.genotypes.deepCopyGenotypeSet();
            vc.addInfo(splitPrefix+"multiNucleoSplit","1");
            vc.addInfo(splitPrefix+"multiNucleoSplitCt",""+v.ref.length);
            vc.addInfo(splitPrefix+"multiNucleoSplitIdx",""+i);
            vc.addInfo(splitPrefix+"multiNucleoSplitPos",""+v.pos);
            vc.addInfo(splitPrefix+"multiNucleoSplitREF",""+v.ref);
            vc.addInfo(splitPrefix+"multiNucleoSplitALT",""+v.alt(0));
            tally("isMNP",1)
            if( v.ref.length == 2 ){
              tally("isDNP",1)
            } else {
              tally("isMNPgt2",1)
            }
            tally("MNP.len."+v.ref.length,1)
            
            vc
          }}
        } else {
          val vc = v.getOutputLine()
          vc.addInfo(splitPrefix+"multiNucleoSplit","0");
          Seq(vc);
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
            new SVcfCompoundHeaderLine(in_tag = "INFO",OPTION_TAGPREFIX+"txMeta_alnRawVar", "A", "String", "Original aligned version of the variant."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",OPTION_TAGPREFIX+"txMeta_alnRgtVar", "A", "String", "Right-aligned version of the variant."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutC_TAG+"_alnRgt", "A", "String", "Right-aligned version of field "+vcfCodes.vMutC_TAG),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutC_TAG+"_alnRaw", "A", "String", "Raw-aligned version of field "+vcfCodes.vMutC_TAG),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutP_TAG+"_alnRgt", "A", "String", "Right-aligned version of field "+vcfCodes.vMutP_TAG),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vType_TAG+"_alnRgt", "A", "String", "Right-aligned version of field "+vcfCodes.vType_TAG),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vTypeShort_TAG+"_alnRgt", "A", "String", "Right aligned version. For each allele, the worst amino acid change type found over all transcripts."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutPShort_TAG+"_alnRgt", "A", "String", "Right aligned version. For each allele, one of the protein changes for the worst variant type found over all transcripts"),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutINFO_TAG+"_alnRgt", "A", "String", "Right aligned version. Raw variant info for each allele."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutLVL_TAG+"_alnRgt", "A", "String", "Right aligned version. See "+vcfCodes.vMutLVL_TAG),
            new SVcfCompoundHeaderLine(in_tag = "INFO",OPTION_TAGPREFIX+"txSummary_WARN_lvlChange", "A", "String", "Flag. Equals 1 iff the summary variant changes varLvl between PLOF, LLOF, and NONSYNON levels when right aligned."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",OPTION_TAGPREFIX+"txSummary_WARN_typeChange", "A", "String", "Flag. Equals 1 iff the summary variant major type changes between or among types contained in PLOF, LLOF, and NONSYNON variant levels when right aligned."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",OPTION_TAGPREFIX+"tx_WARN_lvlChange", "A", "String", "Flag. Equals 1 iff the variant changes varLvl between PLOF, LOF, and NONSYNON levels when right aligned. Equals dot if the variant is synon, unk, or psynon for both alignments."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",OPTION_TAGPREFIX+"tx_WARN_typeChange", "A", "String", "Flag. For each TX, Equals 1 iff the variant changes major type when right aligned. Equals dot if the variant is synon, unk, or psynon for both alignments.")
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
      
      var vLevelRgt = Vector[String]();
      var vMutPshortRgt = Vector[String]();
      var vTypeListShortRgt = Vector[String]();
      
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
              vMutCraw = vMutCraw.updated(vMutCraw.length-1,vMutCraw.last :+ mutCraw);
              vMutPrgt = vMutPrgt.updated(vMutPrgt.length-1,vMutPrgt.last :+ mutPrgt.pvar);
              vTypeListrgt = vTypeListrgt.updated(vTypeListrgt.length - 1, vTypeListrgt.last :+ mutPrgt.varType)
              vLevelListRgtTx(txidx) = mutPrgt.severityType;
            }
            vLevelListRgt = vLevelListRgt :+ vLevelListRgtTx.toVector
            //vTypeDiff = vTypeDiff :+ vTypeDiffTx.toVector
            //vTypeListrgt = vTypeListrgt :+ vTypeListrgtTx.toVector
            /*
            new SVcfCompoundHeaderLine(in_tag = "INFO",OPTION_TAGPREFIX+"txMeta_alnRawVar", "A", "String", "Original aligned version of the variant."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",OPTION_TAGPREFIX+"txMeta_alnRgtVar", "A", "String", "Right-aligned version of the variant."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutC_TAG+"_alnRgt", "A", "String", "Right-aligned version of field "+vcfCodes.vMutC_TAG),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutC_TAG+"_alnRaw", "A", "String", "Raw-aligned version of field "+vcfCodes.vMutC_TAG),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutP_TAG+"_alnRgt", "A", "String", "Right-aligned version of field "+vcfCodes.vMutP_TAG),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vTypeShort_TAG+"_alnRgt", "A", "String", "Right aligned version. For each allele, the worst amino acid change type found over all transcripts."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutPShort_TAG+"_alnRgt", "A", "String", "Right aligned version. For each allele, one of the protein changes for the worst variant type found over all transcripts"),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutINFO_TAG+"_alnRgt", "A", "String", "Right aligned version. Raw variant info for each allele."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",vcfCodes.vMutLVL_TAG+"_alnRgt", "A", "String", "Right aligned version. See "+vcfCodes.vMutLVL_TAG),
            new SVcfCompoundHeaderLine(in_tag = "INFO",OPTION_TAGPREFIX+"txSummary_WARN_lvlChange", "A", "String", "Flag. Equals 1 iff the summary variant changes varLvl between PLOF, LLOF, and NONSYNON levels when right aligned."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",OPTION_TAGPREFIX+"txSummary_WARN_typeChange", "A", "String", "Flag. Equals 1 iff the summary variant major type changes between or among types contained in PLOF, LLOF, and NONSYNON variant levels when right aligned."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",OPTION_TAGPREFIX+"tx_WARN_lvlChange", "A", "String", "Flag. Equals 1 iff the variant changes varLvl between PLOF, LOF, and NONSYNON levels when right aligned."),
            new SVcfCompoundHeaderLine(in_tag = "INFO",OPTION_TAGPREFIX+"tx_WARN_typeChange", "A", "String", "Flag. Equals 1 iff the variant changes major type when right aligned.")
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
          
      //vcfCodes.vTypeShort_TAG+"_alnRgt"
      //vcfCodes.vMutPShort_TAG+"_alnRgt"
      //vcfCodes.vMutLVL_TAG+"_alnRgt"
     //var vLevelRgt = Vector[String]();
      //var vMutPshortRgt = Vector[String]();
      //var vTypeListShortRgt = Vector[String]();
          
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
            
            
          }}
        }
        /*

OPTION_TAGPREFIX+"txMeta_alnRawVar", "A", "String", "Original aligned version of the variant."),
OPTION_TAGPREFIX+"txMeta_alnRgtVar", "A", "String", "Right-aligned version of the variant."),
vcfCodes.vMutC_TAG+"_alnRgt", "A", "String", "Right-aligned version of field "+vcfCodes.vMutC_TAG),
vcfCodes.vMutC_TAG+"_alnRaw", "A", "String", "Raw-aligned version of field "+vcfCodes.vMutC_TAG),
vcfCodes.vMutP_TAG+"_alnRgt", "A", "String", "Right-aligned version of field "+vcfCodes.vMutP_TAG),
vcfCodes.vType_TAG+"_alnRgt", "A", "String", "Right-aligned version of field "+vcfCodes.vType_TAG),
vcfCodes.vTypeShort_TAG+"_alnRgt", "A", "String", "Right aligned version. For each allele, the worst amino acid change type found over all transcripts."),
vcfCodes.vMutPShort_TAG+"_alnRgt", "A", "String", "Right aligned version. For each allele, one of the protein changes for the worst variant type found over all transcripts"),
vcfCodes.vMutINFO_TAG+"_alnRgt", "A", "String", "Right aligned version. Raw variant info for each allele."),
vcfCodes.vMutLVL_TAG+"_alnRgt", "A", "String", "Right aligned version. See "+vcfCodes.vMutLVL_TAG),
OPTION_TAGPREFIX+"txSummary_WARN_lvlChange", "A", "String", "Flag. Equals 1 iff the summary variant changes varLvl between PLOF, LLOF, and NONSYNON levels when right aligned."),
OPTION_TAGPREFIX+"txSummary_WARN_typeChange", "A", "String", "Flag. Equals 1 iff the summary variant major type changes between or among types contained in PLOF, LLOF, and NONSYNON variant levels when right aligned."),
OPTION_TAGPREFIX+"tx_WARN_lvlChange", "A", "String", "Flag. Equals 1 iff the variant changes varLvl between PLOF, LOF, and NONSYNON levels when right aligned."),
OPTION_TAGPREFIX+"tx_WARN_typeChange", "A", "String", "Flag. Equals 1 iff the variant changes major type when right aligned.")
     
      var rightAlignList = Vector[String]();
      var rawAlignList = Vector[String]();
      var vMutCrgt = Vector[Vector[String]]();
      var vMutCraw = Vector[Vector[String]]();
      var vMutPrgt = Vector[Vector[String]]();
      var vTypeListrgt = Vector[Vector[String]]();
          new SVcfCompoundHeaderLine(in_tag = "INFO",OPTION_TAGPREFIX+"txSummary_WARN_lvlChange", "A", "String", "Flag. Equals 1 iff the summary variant changes varLvl between PLOF, LLOF, and NONSYNON levels when right aligned."),
          new SVcfCompoundHeaderLine(in_tag = "INFO",OPTION_TAGPREFIX+"txSummary_WARN_typeChange", "A", "String", "Flag. Equals 1 iff the summary variant major type changes between or among types contained in PLOF, LLOF, and NONSYNON variant levels when right aligned."),
         
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
          vb.addInfo(OPTION_TAGPREFIX+"txMeta_alnRawVar",rawAlignList.mkString(","));
          vb.addInfo(OPTION_TAGPREFIX+"txMeta_alnRgtVar",rightAlignList.mkString(","));
          vb.addInfo(vcfCodes.vMutP_TAG+"_alnRgt",mkSubDelimListScala(vMutPrgt,vcfCodes.delims).mkString(","));
          vb.addInfo(vcfCodes.vType_TAG+"_alnRgt",mkSubDelimListScala(vTypeListrgt,vcfCodes.delims).mkString(","));
          vb.addInfo(vcfCodes.vMutC_TAG+"_alnRaw",mkSubDelimListScala(vMutCraw,vcfCodes.delims).mkString(","));
          vb.addInfo(vcfCodes.vMutC_TAG+"_alnRgt",mkSubDelimListScala(vMutCrgt,vcfCodes.delims).mkString(","));
          vb.addInfo(OPTION_TAGPREFIX+"tx_WARN_lvlChange",mkSubDelimListScala(vLvlDiff,vcfCodes.delims).mkString(","));
          vb.addInfo(OPTION_TAGPREFIX+"tx_WARN_typeChange",mkSubDelimListScala(vTypeDiff,vcfCodes.delims).mkString(","));
          
          vb.addInfo(vcfCodes.vTypeShort_TAG+"_alnRgt",vTypeListShortRgt.toList.mkString(","));
          vb.addInfo(vcfCodes.vMutPShort_TAG+"_alnRgt",vMutPshortRgt.toList.mkString(","));
          vb.addInfo(vcfCodes.vMutLVL_TAG+"_alnRgt", vLevelRgt.toList.mkString(","));
          
          val sumLvlDiff = vLvlDiff.map{ vv => {
            if( vv.contains("1") ){
              "1"
            } else if( vv.contains("0")){
              "0"
            } else {
              "."
            }
          }}
          val sumTypeDiff = vTypeDiff.map{ vv => {
            if( vv.contains("1") ){
              "1"
            } else if( vv.contains("0")){
              "0"
            } else {
              "."
            }
          }}
          
          vb.addInfo(OPTION_TAGPREFIX+"txSummary_WARN_lvlChange",sumLvlDiff.mkString(","));
          vb.addInfo(OPTION_TAGPREFIX+"txSummary_WARN_typeChange",sumTypeDiff.mkString(","));
          /*
      //vcfCodes.vTypeShort_TAG+"_alnRgt"
      //vcfCodes.vMutPShort_TAG+"_alnRgt"
      //vcfCodes.vMutLVL_TAG+"_alnRgt"
            vMutPshortRgt = vMutPshortRgt :+ mutPshortrgt;
            vTypeListShortRgt = vTypeListShortRgt :+ typeShortrgt;
            vLevelRgt = vLevelRgt :+ vLvlrgt;
           * 
           */
          
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
  

  case class SnpEffAnnotater(cmdId : String,snpSiftAnnoCmd : String, tagPrefix : String = "") extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "snpEffAnnotate"+cmdId
    def walkerParams : Seq[(String,String)] = Seq[(String,String)](
        ("cmdId",cmdId.toString)
    )

    var args = snpSiftAnnoCmd.split("\\s+");
    notice("Attempting SnpEff annotation with cmdId="+cmdId+"\n   "+args.mkString("\n   ")+"\n","SNPSIFT_ANNOTATE",-1);
    
    if(args.contains("-c") || args.contains("-config")){
      notice("Using custom SNPEff config file","note");
    } else {
      //val currdir =  (new File(SVcfWalkerUtils.SnpEffAnnotater.getClass.getProtectionDomain().getCodeSource().getLocation().toURI())).getParent();
      //val cfg = currdir+"snpEff.config"
      val wd = System.getProperty("user.dir");
      if(! (new File( wd+"/snpeff" )).exists()){
        (new File(wd+"/snpeff")).mkdirs();
      }
      
      val cfg = wd+"/snpeff/snpEff.config"
      if(! (new File(cfg)).exists() ){
        warning("Creating new snpEff config file (and database) at: "+wd+"/snpeff/"+". In future runs you may want to invoke SnpEff using the added parameters: \"-c "+cfg+"\"","SNPEFF_CONFIG",-1);
      val cfgReader = ( new java.io.BufferedReader( new java.io.InputStreamReader( this.getClass().getClassLoader().getResourceAsStream("snpEff.config") )) )
        val cfgWriter = new BufferedWriter(new FileWriter(cfg));
        cfgReader.lines().iterator().asScala.foreach{ line => {
          cfgWriter.write(line);
        }}
        cfgWriter.close();
        args = Array(args.head,"-c",cfg) ++ args.tail
      }
    }
    
    //val ssa = new org.snpsift.SnpSiftCmdAnnotate()
    //ssa.parseArgs(args)
    //ssa.init()
    val finalArgs : Array[String] = args
    val ss : org.snpeff.SnpEff = new org.snpeff.SnpEff(finalArgs);
    val ssa : org.snpeff.snpEffect.commandLine.SnpEffCmdEff = ss.cmd().asInstanceOf[org.snpeff.snpEffect.commandLine.SnpEffCmdEff]
    ssa.load()
    //val ssa : org.snpeff.snpEffect.commandLine.SnpEffCmdEff = new org.snpeff.snpEffect.commandLine.SnpEffCmdEff()
    
    
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
    
    val newHeaderLines : Seq[(String,String,SVcfCompoundHeaderLine)] = 
      Vector[(String,String,SVcfCompoundHeaderLine)](
            (("ANN",
                tagPrefix+"ANN",
                (new SVcfCompoundHeaderLine(in_tag="INFO",ID = tagPrefix+"ANN",Number = ".", Type = "String", desc = "SNPEFF ANN field")).addWalker(this).addExtraField("source","SnpEff.via.vArmyKnife").addExtraField("SnpSiftVer",org.snpsift.SnpSift.VERSION_SHORT).addExtraField("SnpEffVer",org.snpeff.SnpEff.VERSION_SHORT)
           ))
      )
    reportln("    SnpSiftAnno: extracting infolines: [\""+newHeaderLines.map{_._1}.padTo(1,".").mkString("\",\"")+"\"]","debug");
    reportln("    SnpSiftAnno: adding infolines: [\""+newHeaderLines.map{_._2}.padTo(1,".").mkString("\",\"")+"\"]","debug");

    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val newHeader = vcfHeader.copyHeader;
      newHeaderLines.foreach{ case (tag,outTag,hl) => {
        newHeader.addInfoLine(hl);
      }}
      newHeader.reportAddedInfos(this)
      var neverAddedAnno = true;
      (vcMap(vcIter){ v => {
        val vb = v.getOutputLine();
        val ve = vb.makeSnpeffVariantEntry(dummyIter);
        ssa.annotate(ve);
        var addedAnno = false;
        newHeaderLines.foreach{ case (tag,outTag,hl) => {
          Option(ve.getInfo(tag)).foreach{ v => {
            addedAnno = true;
            vb.addInfo(outTag,v);
          }}
        }}
        if(addedAnno) notice("Added ANN info with SnpEff.","SNPEFF_ANNOTATE_"+cmdId,1);
        //Need to find a way to pull VCF header info!
        vb
      }},newHeader);
    }
  }
  
  case class SnpSiftFilter( snpSiftAnnoStrings : List[String] ) extends internalUtils.VcfTool.SVcfWalker {
    val dropMatch = snpSiftAnnoStrings.map{ ssas => ssas.split("|")(1) == "DROP_MATCH" }
    val snpSiftAnnoCmds = snpSiftAnnoStrings.map{ ssas => ssas.split("|")(2) }
    val snpSiftAnnoTitles = snpSiftAnnoStrings.map{ ssas => ssas.split("|")(0) }
    
    def walkerName : String = "SnpSiftFilter"
    val dropOrKeep = dropMatch.map{ dm => {
      if(dm){
        "DROP_MATCH"
      } else {
        "KEEP_MATCH"
      }
    }}
    def walkerParams : Seq[(String,String)] = Seq[(String,String)](
       ("cmdsAndFilter",snpSiftAnnoCmds.zip(dropOrKeep).zip(snpSiftAnnoTitles).map{ case ((cmd,dm),tt) => {
         dm+"/"+tt+"/"+cmd.replaceAll("\\s","_").replaceAll("[^A-Za-z._-]","_")
       }}.mkString("|"))
    )
    
    val argsList = snpSiftAnnoCmds.map{ ssac => {  Array[String]("annotate") ++ ssac.split("\\s+") ++ Array[String]("dummyvar") }}
    val (dummyIter,snpSiftHeader) = SVcfVariantLine.getDummyIterator();
    
    val ssList = argsList.map{ args => {
      val ss = new org.snpsift.SnpSift(args);
      val ssa = ss.cmd();
      ssa.annotateInit(dummyIter);
      val initilizerExampleVariant = new org.snpeff.vcf.VcfEntry( dummyIter,  "chr???\t100\t.\tA\tC\t20\t.\tTESTSNPEFFDUMMYVAR=BLAH;", 1, true);
      ssa.annotate(initilizerExampleVariant)
      ssa
    }}
    val sss = ssList.zip(dropMatch);
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val newHeader = vcfHeader.copyHeader;
       newHeader.reportAddedInfos(this)
      (vcFlatMap(vcIter){ v => {
        val vb = v.getLazyOutputLine();
        val ve = vb.makeSnpeffVariantEntry(dummyIter);
        
        val dropIdx = sss.indexWhere{ case (ssa,dm) => {
          val isMatched = ssa.annotate(ve);
          dm == isMatched
          //  DROPMATCH + isMatched = DROP
          // !DROPMATCH + !isMatched = DROP
        }}
        if(dropIdx == -1){
          notice("Passed all snpsift match filters","SNPSIFT_MATCHFILT_PASS",10);
          //notice("Found variant with SnpSift."+cmdId+" annotation match.","SNPSIFT_ANNOTATE_"+cmdId,1);
          Some(v);
        } else {
          notice("Failed Snpsift match filter: "+snpSiftAnnoTitles(dropIdx),"SNPSIFT_MATCHFILT_FAIL_"+snpSiftAnnoTitles(dropIdx),10);
          None
        }
      }},newHeader);
    }
    
    
  }
  //cmdId : String,snpSiftAnnoCmd : String, tagPrefix : String = ""
  
case class SnpSiftAnnotaterMulti(cmdTriples : Seq[(String,String)]) extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "snpSiftAnnotate"+cmdTriples.map{ _._1 }.mkString(".")
    def walkerParams : Seq[(String,String)] = Seq[(String,String)](
    )

    val argSeq = cmdTriples.map{ case (cmdId, cmd) => {
      val args = cmd.split("\\s+") 
      notice("Attempting SnpSift annotation with cmdId="+cmdId+"\n   "+args.mkString("\n   ")+"\n","SNPSIFT_ANNOTATE",-1);
      args
    }}
    //
    
    val infoListSeq = argSeq.map{ args => {
      val infoList = args.zipWithIndex.find{ case (arg,idx) => { arg == "-info"}}.flatMap{ case (arg,idx) => {
        args.lift(idx+1).map{_.split(",")}
      }}.getOrElse(Array[String]())
      reportln("    SnpSiftAnno: found infoList: [\""+infoList.padTo(1,".").mkString("\",\"")+"\"]","debug");
      infoList
    }}
    

    val namePrefixSeq = argSeq.map{ args => {
      val namePrefix = args.zipWithIndex.find{ case (arg,idx) => { arg == "-name"}}.flatMap{ case (arg,idx) => {
        args.lift(idx+1)
      }}.getOrElse("");
      reportln("    SnpSiftAnno: found namePrefix: \""+namePrefix+"\"","debug");
      namePrefix
    }}
    
    
    //val ssa = new org.snpsift.SnpSiftCmdAnnotate()
    //ssa.parseArgs(args)
    //ssa.init()
    val finalArgSeq : Seq[Array[String]] = argSeq.map{ args => {
      Array[String]("annotate") ++ args ++ Array[String]("dummyvar")
    }}
    val ssSeq = finalArgSeq.map{ finalArgs => { new org.snpsift.SnpSift(finalArgs) }}
    val ssaSeq = ssSeq.map{ ss => ss.cmd() }
    
    //HACK HACK HACK:
    //      SnpSiftCmdAnnotate requires an instance of its special entry iterator class.
    val dummySeq = ssaSeq.map{ ssa => {
      SVcfVariantLine.getDummyIterator();
    }}
    
    ssaSeq.zip(dummySeq).foreach{ case (ssa,(dummyIter,snpSiftHeader)) => {
      ssa.annotateInit(dummyIter);
    }}
    
    //val (dummyIter,snpSiftHeader) = SVcfVariantLine.getDummyIterator();
    //ssaSeq.foreach{ ssa => {
    //  ssa.annotateInit(dummyIter);
    //}}
    
    
    
    //HACK HACK HACK: 
    //      Force SnpSift to process the header so I can access the header info.
    //      Can't directly invoke the parseHeader commands because they're all private or protected methods.
    val initilizerExampleVariant = new org.snpeff.vcf.VcfEntry( dummySeq.head._1,  "chr???\t100\t.\tA\tC\t20\t.\tTESTSNPEFFDUMMYVAR=BLAH;", 1, true);
    ssaSeq.foreach{ssa => {
      ssa.annotate(initilizerExampleVariant)      
    }}
    ssaSeq.zip(dummySeq).foreach{ case (ssa,(dummyIter,snpSiftHeader)) => {
      reportln("    SnpSiftAnno: snpSiftHeader has the following info fields: [\""+dummyIter.getVcfHeader().getVcfHeaderInfo().asScala.map{h => h.getId()}.toVector.padTo(1,".").mkString("\",\"")+"\"]","debug");
    }}
    
    val newHeaderLinesSeq : Seq[Seq[(String,String,SVcfCompoundHeaderLine)]] = infoListSeq.zip(ssaSeq).zip(dummySeq).zip(cmdTriples).zip(namePrefixSeq).map{ case ((((infoList,ssa),(dummyIter,snpSiftHeader)),(cmdId,cmd)),namePrefix) => {
      infoList.toSeq.map{ tag => {
        dummyIter.getVcfHeader().getVcfHeaderInfo().asScala.find{ ssline => ssline.getId() == namePrefix + tag }.map{ ssline => {
          val numRaw = ssline.getVcfInfoNumber().toString().padTo(1,'0');
          val num = if(numRaw == "A") "1" else numRaw;
          val desc = ssline.getDescription()
          val ty = ssline.getVcfInfoType().name;
          val id = ssline.getId;
          val outTag = namePrefix + id;
          (namePrefix + tag,outTag,new SVcfCompoundHeaderLine(in_tag="INFO",ID = outTag,Number = num, Type = ty, desc = desc).
                                   addWalker(this).addExtraField("source","SnpSift.via.vArmyKnife").
                                   addExtraField("SnpSiftVer",org.snpsift.SnpSift.VERSION_SHORT).
                                   addExtraField("SnpEffVer",org.snpeff.SnpEff.VERSION_SHORT))
        }}.getOrElse(
          (namePrefix + tag,namePrefix + tag,
            new SVcfCompoundHeaderLine(in_tag="INFO",ID = namePrefix + tag,Number = ".", Type = "String", desc = "Annotation tag "+tag+" from "+cmdId).
                                   addWalker(this).addExtraField("source","SnpSift.via.vArmyKnife").
                                   addExtraField("SnpSiftVer",org.snpsift.SnpSift.VERSION_SHORT).
                                   addExtraField("SnpEffVer",org.snpeff.SnpEff.VERSION_SHORT))
        );
      }}
    }}
      
    newHeaderLinesSeq.zipWithIndex.foreach{ case (newHeaderLines,i) => {
      reportln("    SnpSiftAnno: For annotation set: "+i,"debug");
      reportln("       SnpSiftAnno: extracting infolines: [\""+newHeaderLines.map{_._1}.padTo(1,".").mkString("\",\"")+"\"]","debug");
      reportln("       SnpSiftAnno: adding infolines: [\""+newHeaderLines.map{_._2}.padTo(1,".").mkString("\",\"")+"\"]","debug");  
    }}


    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      val newHeader = vcfHeader.copyHeader;
      newHeaderLinesSeq.foreach{ newHeaderLines => {
        newHeaderLines.foreach{ case (tag,outTag,hl) => {
          newHeader.addInfoLine(hl);
        }}  
      }}
      var neverAddedAnno = true;
      newHeader.reportAddedInfos(this)
      (vcMap(vcIter){ v => {
        val vb = v.getOutputLine();
        val ve = vb.makeSnpeffVariantEntry(dummySeq.head._1);
        //ssa.annotate(ve);
        for( i <- newHeaderLinesSeq.indices ){
          ssaSeq(i).annotate(ve);
          val cmdId = cmdTriples(i)._1;
          var addedAnno = false;
          newHeaderLinesSeq(i).foreach{ case (tag,outTag,hl) => {
            Option(ve.getInfo(tag)).foreach{ v => {
              addedAnno = true;
              vb.addInfo(tag,v);
            }}
          }}
          if(addedAnno) notice("Found variant with SnpSift."+cmdId+" annotation match.","SNPSIFT_ANNOTATE_"+cmdId,1);
        }
        //Need to find a way to pull VCF header info?
        vb
      }},newHeader);
    }


  }
  
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
      newHeader.reportAddedInfos(this)
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
      newHeader.reportAddedInfos(this)
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
                                 forceSplit : Boolean = false,
                                 silent : Boolean = false
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
    //mergeSecondaryVcf
    val alleCtCt = new scala.collection.mutable.HashMap[Int, Int]().withDefaultValue(0)
    val badCtCt = new scala.collection.mutable.HashMap[Int, Int]().withDefaultValue(0)
    
    val singleCallerFmtKeySumIntAltsCells = singleCallerFmtKeySumIntAlts.map{_.split("\\|").toVector}.toVector;
    
    val noteCt = if(silent){ 0 } else { 5 }
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      
      if( vcfHeader.infoLines.contains(vcfCodes.splitIdx_TAG) || vcfHeader.isSplitMA ){
        warning("Multiallelics appear to have already been split in this file!","ALLELES_ALREADY_SPLIT",10);
      }
      if(vcfHeader.walkLines.exists(walkline => walkline.ID == "SSplitMultiAllelics") || vcfHeader.isSplitMA ){
          warning("Splitmultiallelic walker found in this VCF file's header. Skipping multiallelic split!","ALLELES_ALREADY_SPLIT",10);
          //if(! forceSplit){
            return((vcIter,vcfHeader));
          //}
      }
       
      //if(vcfHeader.titleLine.sampleList.length > 0){
        vcfHeader.formatLines.find(fline => fline.ID == "GT").foreach{ fline => {
          val nl = new SVcfCompoundHeaderLine("FORMAT",fline.ID, "1","String", fline.desc, subType = Some(VcfTool.subtype_GtStyleUnsplit))
          notice("Adding GtStyleUnsplit subtype to tag: "+fline.ID+"\n"+
               "    fline:"+fline.getVcfString+"\n"+
               "    nl:"+nl.getVcfString,"GtStyleUnsplit",noteCt);
          vcfHeader.addFormatLine(nl,walker=Some(this));
        }}
        vcfHeader.formatLines.find(fline => fline.ID == "AD").foreach{ fline => {
          val nl = new SVcfCompoundHeaderLine("FORMAT",fline.ID, "R","Integer", fline.desc, subType = Some(VcfTool.subtype_AlleleCountsUnsplit))
          notice("Adding subtype_AlleleCountsUnsplit subtype to tag: "+fline.ID+"\n"+
               "    fline:"+fline.getVcfString+"\n"+
               "    nl:"+nl.getVcfString,"GtStyleUnsplit",noteCt);
          vcfHeader.addFormatLine(nl,walker=Some(this));
        }}
        vcfHeader.formatLines.find(fline => fline.ID == "AO").foreach{ fline => {
          val nl = new SVcfCompoundHeaderLine("FORMAT",fline.ID, "A","Integer", fline.desc, subType = Some(VcfTool.subtype_AlleleCountsUnsplit))
          notice("Adding subtype_AlleleCountsUnsplit subtype to tag: "+fline.ID+"\n"+
               "    fline:"+fline.getVcfString+"\n"+
               "    nl:"+nl.getVcfString,"GtStyleUnsplit",noteCt);
          vcfHeader.addFormatLine(nl,walker=Some(this));
        }}
        fmtKeySumIntAlts.foreach{t => vcfHeader.formatLines.find(fline => fline.ID == t && fline.subType.isEmpty).foreach{ fline => {
          val nl = new SVcfCompoundHeaderLine("FORMAT",fline.ID, fline.Number,fline.Type, fline.desc, subType = Some(VcfTool.subtype_AlleleCountsUnsplit))
          notice("Adding subtype_AlleleCountsUnsplit subtype to tag: "+fline.ID+"\n"+
               "    fline:"+fline.getVcfString+"\n"+
               "    nl:"+nl.getVcfString,"GtStyleUnsplit",noteCt);
          vcfHeader.addFormatLine(nl,walker=Some(this));
        }}}
        fmtKeyGenotypeStyle.foreach{t => vcfHeader.formatLines.find(fline => fline.ID == t && fline.subType.isEmpty).foreach{ fline => {
          val nl = new SVcfCompoundHeaderLine("FORMAT",fline.ID, fline.Number,fline.Type, fline.desc, subType = Some(VcfTool.subtype_GtStyleUnsplit))
          notice("Adding GtStyleUnsplit subtype to tag: "+fline.ID+"\n"+
               "    fline:"+fline.getVcfString+"\n"+
               "    nl:"+nl.getVcfString,"GtStyleUnsplit",noteCt);
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
      
      var copyPresplits = scala.collection.Set[String]();
      
      val gtSplitLines : Seq[(String,String,SVcfCompoundHeaderLine,SVcfCompoundHeaderLine)] = vcfHeader.formatLines.flatMap{ fline => {
        fline.subType match {
          case Some(st) => {
            if(st == VcfTool.subtype_GtStyleUnsplit){
              //reportln("Adding new GT-Style FORMAT tag: "+fline.ID+"_presplit, based on "+fline.ID+": "+fline.getVcfString,"note");
              val ol = new SVcfCompoundHeaderLine("FORMAT",ID=fline.ID, Number=fline.Number, Type=fline.Type, desc="(Recoded for multiallelic split) "+fline.desc, subType = Some(VcfTool.subtype_GtStyle))
              val nl = new SVcfCompoundHeaderLine("FORMAT",ID=fline.ID + "_presplit",  Number="1", Type="String", desc="(Raw value prior to multiallelic split) "+fline.desc, subType = Some(VcfTool.subtype_GtStyleUnsplit))
              //reportln("    ol:"+ol.getVcfString,"note");
              //reportln("    nl:"+nl.getVcfString,"note");
              newHeader.addFormatLine(ol,walker=Some(this));
              newHeader.addFormatLine(nl,walker=Some(this));
              copyPresplits = copyPresplits + fline.ID;
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
              //reportln("Adding new Allele-Count FORMAT tag: "+fline.ID+"_presplit, based on "+fline.ID+": "+fline.getVcfString,"note");
              val ol = new SVcfCompoundHeaderLine("FORMAT",ID=fline.ID,Number= fline.Number, Type=fline.Type, desc="(Recoded for multiallelic split) "+fline.desc, subType = Some(VcfTool.subtype_AlleleCounts))
              val nl = new SVcfCompoundHeaderLine("FORMAT",ID=fline.ID + "_presplit", Number=".", Type=fline.Type, desc="(Raw value prior to multiallelic split) "+fline.desc, subType = Some(VcfTool.subtype_AlleleCountsUnsplit))
              //reportln("    ol:"+ol.getVcfString,"note");
              //reportln("    nl:"+nl.getVcfString,"note");
              copyPresplits = copyPresplits + fline.ID;
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
      val otherSplitLines : Seq[(String,String,SVcfCompoundHeaderLine,SVcfCompoundHeaderLine)] = vcfHeader.formatLines.filter{ fline => {
        adSplitLines.indexWhere{ case (oldID,newID,ohl,hl) => {
          fline.ID == oldID
        }} == -1 && (fline.Number == "R" || fline.Number == "A")
      }}.flatMap{ fline => {
              //reportln("Adding new FORMAT tag: "+fline.ID+"_presplit, based on "+fline.ID+": "+fline.getVcfString,"note");
              val ol = new SVcfCompoundHeaderLine("FORMAT",ID=fline.ID,Number= fline.Number, Type=fline.Type, desc="(Recoded for multiallelic split) "+fline.desc)
              val nl = new SVcfCompoundHeaderLine("FORMAT",ID=fline.ID + "_presplit", Number=".", Type=fline.Type, desc="(Raw value prior to multiallelic split) "+fline.desc)
              newHeader.addFormatLine(ol,walker=Some(this));
              newHeader.addFormatLine(nl,walker=Some(this));
              copyPresplits = copyPresplits + fline.ID;
              Some((fline.ID,fline.ID, ol,nl))
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
      for( i <- Range(1,10)){
        tally("multiAlleSplit_nAlle"+i,0)
      }
      newHeader.reportAddedInfos(this)
      val sampNames = vcfHeader.titleLine.sampleList;
      newHeader.reportAddedInfos(this)
      return (addIteratorCloseAction(vcFlatMap(vcIter)(vc => {
        
        var warningSet = Set[String]();
        val vb = vc.getOutputLine();
        tally("multiAlleSplit_nAlle"+vc.alt.length,1)
        
        copyPresplits.foreach{ s => {
           vc.info.getOrElse( s, None ) match {
             case Some(k) => {
               vb.addInfo(s+"_presplit",k);
             }
             case None => {
               //do nothing
             }
           }
        }}
        
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
                      //g
                      g.split("[\\|/]").map{gg => if(gg == "0") "0" else if(gg == alleIdxString) "1" else if(gg == ".") "." else "2"}.sorted.mkString("/")
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
                    gs.addGenotypeArray(oldID,gs.genotypeValues(fIdx).map{adString => {
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
              otherSplitLines.foreach{ case (oldID,newID,ohl,hl) => {
                val fIdx = gs.fmt.indexOf(oldID);
                if(fIdx != -1){
                  gs.addGenotypeArray(newID,gs.genotypeValues(fIdx).clone());
                  if(ohl.Number == "R"){
                    gs.addGenotypeArray(oldID,gs.genotypeValues(fIdx).map{adString => {
                      if(adString == "."){
                        "."
                      } else {
                        val ad = adString.split(",") //.map{ aa => if(aa == ".") 0 else string2int(aa) }
                        if(! ad.isDefinedAt(alleIdx)){
                          warning("Attempting to split "+oldID+" tag failed! Offending String: "+adString+" for variant:\n     "+vc.getSimpleVcfString(),"BADSECONDARYADTAG_SPLITMULTALLE",10);
                          "."
                        } else {
                          ad(0) + "," + ad(alleIdx) + ",.";
                        }
                        //  g.split("[\\|/]").map{gg => if(gg == "0") "0" else if(gg == alleIdxString) "1" else "2"}.sorted.mkString("/")
                      }
                    }})
                  } else if(ohl.Number == "A"){
                    gs.addGenotypeArray(oldID,gs.genotypeValues(fIdx).map{adString => {
                      if(adString == "."){
                        "."
                      } else {
                        val ad = adString.split(",") //.map{ aa => if(aa == ".") 0 else string2int(aa) }
                        if(! ad.isDefinedAt(alleIdx-1)){
                          warning("Attempting to split "+oldID+" tag failed! Offending String: "+adString+" for variant:\n     "+vc.getSimpleVcfString(),"BADSECONDARYADTAG_SPLITMULTALLE",10);
                          "."
                        } else {
                          ad(alleIdx-1) + ",.";
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
  
  
  
  case class SSplitMultiAllelics2(vcfCodes : VCFAnnoCodes = VCFAnnoCodes(), 
                                  treatOtherAsRef : Boolean = true,
                                 fmtKeySumIntAlts : Seq[String] = Seq[String]("AD","AO"),
                                 fmtKeyIgnoreAlts : Seq[String] = Seq[String](),
                                 fmtKeyGenotypeStyle : Seq[String] = Seq[String]("GT"),
                                 splitSimple : Boolean = false,
                                 rawPrefix : Option[String] = Some("UNSPLIT_"),
                                 forceSplit : Boolean = false,
                                 silent : Boolean = false
                               ) extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "SSplitMultiAllelics"
    def walkerParams : Seq[(String,String)] = Seq[(String,String)](
        ("fmtKeySumIntAlts",fmtList(fmtKeySumIntAlts)),
        ("fmtKeyIgnoreAlts",fmtList(fmtKeyIgnoreAlts)),
        ("fmtKeyGenotypeStyle",fmtList(fmtKeyGenotypeStyle)),
        ("splitSimple",splitSimple.toString),
        ("rawPrefix",rawPrefix.toString)
    )

    val threeAllelePrefix = "a3";
    val otherAlleleChar = if(treatOtherAsRef) "0" else ".";
    
    
    val alleCtCt = new scala.collection.mutable.HashMap[Int, Int]().withDefaultValue(0)
    val badCtCt = new scala.collection.mutable.HashMap[Int, Int]().withDefaultValue(0)
    
    
    val noteCt = if(silent){ 0 } else { 5 }
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      
      if( vcfHeader.infoLines.contains(vcfCodes.splitIdx_TAG) || vcfHeader.isSplitMA ){
        warning("Multiallelics appear to have already been split in this file!","ALLELES_ALREADY_SPLIT",10);
      }
      if(vcfHeader.walkLines.exists(walkline => walkline.ID == "SSplitMultiAllelics") || vcfHeader.isSplitMA ){
          warning("Splitmultiallelic walker found in this VCF file's header. Skipping multiallelic split!","ALLELES_ALREADY_SPLIT",10);
          //if(! forceSplit){
            return((vcIter,vcfHeader));
          //}
      }
       
      //if(vcfHeader.titleLine.sampleList.length > 0){
        vcfHeader.formatLines.find(fline => fline.ID == "GT").foreach{ fline => {
          val nl = new SVcfCompoundHeaderLine("FORMAT",fline.ID, "1","String", fline.desc, subType = Some(VcfTool.subtype_GtStyleUnsplit))
          notice("Adding GtStyleUnsplit subtype to tag: "+fline.ID+"\n"+
               "    fline:"+fline.getVcfString+"\n"+
               "    nl:"+nl.getVcfString,"GtStyleUnsplit",noteCt);
          vcfHeader.addFormatLine(nl,walker=Some(this));
        }}
        vcfHeader.formatLines.find(fline => fline.ID == "AD").foreach{ fline => {
          val nl = new SVcfCompoundHeaderLine("FORMAT",fline.ID, "R","Integer", fline.desc, subType = Some(VcfTool.subtype_AlleleCountsUnsplit))
          notice("Adding subtype_AlleleCountsUnsplit subtype to tag: "+fline.ID+"\n"+
               "    fline:"+fline.getVcfString+"\n"+
               "    nl:"+nl.getVcfString,"GtStyleUnsplit",noteCt);
          vcfHeader.addFormatLine(nl,walker=Some(this));
        }}
        vcfHeader.formatLines.find(fline => fline.ID == "AO").foreach{ fline => {
          val nl = new SVcfCompoundHeaderLine("FORMAT",fline.ID, "A","Integer", fline.desc, subType = Some(VcfTool.subtype_AlleleCountsUnsplit))
          notice("Adding subtype_AlleleCountsUnsplit subtype to tag: "+fline.ID+"\n"+
               "    fline:"+fline.getVcfString+"\n"+
               "    nl:"+nl.getVcfString,"GtStyleUnsplit",noteCt);
          vcfHeader.addFormatLine(nl,walker=Some(this));
        }}
        fmtKeySumIntAlts.foreach{t => vcfHeader.formatLines.find(fline => fline.ID == t && fline.subType.isEmpty).foreach{ fline => {
          val nl = new SVcfCompoundHeaderLine("FORMAT",fline.ID, fline.Number,fline.Type, fline.desc, subType = Some(VcfTool.subtype_AlleleCountsUnsplit))
          notice("Adding subtype_AlleleCountsUnsplit subtype to tag: "+fline.ID+"\n"+
               "    fline:"+fline.getVcfString+"\n"+
               "    nl:"+nl.getVcfString,"GtStyleUnsplit",noteCt);
          vcfHeader.addFormatLine(nl,walker=Some(this));
        }}}
        fmtKeyGenotypeStyle.foreach{t => vcfHeader.formatLines.find(fline => fline.ID == t && fline.subType.isEmpty).foreach{ fline => {
          val nl = new SVcfCompoundHeaderLine("FORMAT",fline.ID, fline.Number,fline.Type, fline.desc, subType = Some(VcfTool.subtype_GtStyleUnsplit))
          notice("Adding GtStyleUnsplit subtype to tag: "+fline.ID+"\n"+
               "    fline:"+fline.getVcfString+"\n"+
               "    nl:"+nl.getVcfString,"GtStyleUnsplit",noteCt);
          vcfHeader.addFormatLine(nl,walker=Some(this));
        }}}
      
      val newHeader =vcfHeader.copyHeader

      newHeader.addStatBool("isSplitMultAlle",true)
      newHeader.addStatBool("isSplitMultAlleStar",false)
      
      val newHeaderLines = List[SVcfCompoundHeaderLine](
          //new VCFInfoHeaderLine(vcfCodes.isSplitMulti_TAG, 0, VCFHeaderLineType.Flag,    "Indicates that this line was split apart from a multiallelic VCF line."),
          new SVcfCompoundHeaderLine("INFO",vcfCodes.splitIdx_TAG, "1", "Integer", "Indicates the index of this biallelic line in the set of biallelic lines extracted from the same multiallelic VCF line."),
          new SVcfCompoundHeaderLine("INFO",vcfCodes.numSplit_TAG,     "1", "Integer", "Indicates the number of biallelic lines extracted from the same multiallelic VCF line as this line."),
          new SVcfCompoundHeaderLine("INFO",vcfCodes.splitAlle_TAG,     ".", "String", "The original set of alternative alleles."),
           new SVcfCompoundHeaderLine("INFO",vcfCodes.splitAlleWARN_TAG,     ".", "String", "Warnings produced by the multiallelic allele split. These may occur when certain tags have the wrong number of values.")
      )
      
      var copyPresplits = scala.collection.Set[String]();
      
      val gtSplitLines : Seq[(String,String,SVcfCompoundHeaderLine,SVcfCompoundHeaderLine)] = vcfHeader.formatLines.flatMap{ fline => {
        fline.subType match {
          case Some(st) => {
            if(st == VcfTool.subtype_GtStyleUnsplit){
              reportln("Adding new GT-Style FORMAT tag: "+fline.ID+"_presplit, based on "+fline.ID+": "+fline.getVcfString,"note");
              val ol = new SVcfCompoundHeaderLine("FORMAT",ID=fline.ID, Number=fline.Number, Type=fline.Type, desc="(Recoded for multiallelic split) "+fline.desc, subType = Some(VcfTool.subtype_GtStyle))
              val nl = new SVcfCompoundHeaderLine("FORMAT",ID=fline.ID + "_presplit",  Number="1", Type="String", desc="(Raw value prior to multiallelic split) "+fline.desc, subType = Some(VcfTool.subtype_GtStyleUnsplit))
              val ml = new SVcfCompoundHeaderLine("FORMAT",ID=fline.ID + threeAllelePrefix, Number="1", Type=fline.Type, desc="(Recoded for multiallelic split, with all other alleles collapsed into a 3rd allele) "+fline.desc, subType = Some(VcfTool.subtype_GtStyle3))
              reportln("    ol:"+ol.getVcfString,"note");
              reportln("    nl:"+nl.getVcfString,"note");
              reportln("    ml:"+ml.getVcfString,"note");

              newHeader.addFormatLine(ol,walker=Some(this));
              newHeader.addFormatLine(nl,walker=Some(this));
              newHeader.addFormatLine(ml,walker=Some(this));
              
              //copyPresplits = copyPresplits + fline.ID;
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
              val newNum = if(fline.Number == "R") "3" else "2";
              reportln("Adding new Allele-Count FORMAT tag: "+fline.ID+"_presplit, based on "+fline.ID+": "+fline.getVcfString,"note");
              val ol = new SVcfCompoundHeaderLine("FORMAT",ID=fline.ID,Number= fline.Number, Type=fline.Type, desc="(Recoded for multiallelic split) "+fline.desc, subType = Some(VcfTool.subtype_AlleleCounts))
              val nl = new SVcfCompoundHeaderLine("FORMAT",ID=fline.ID + "_presplit", Number=".", Type=fline.Type, desc="(Raw value prior to multiallelic split) "+fline.desc, subType = Some(VcfTool.subtype_AlleleCountsUnsplit))
              val ml = new SVcfCompoundHeaderLine("FORMAT",ID=fline.ID + threeAllelePrefix, Number=newNum, Type=fline.Type, desc="(Recoded for multiallelic split, with all other alleles collapsed into a 3rd allele) "+fline.desc, subType = Some(VcfTool.subtype_GtStyle3))
              reportln("    ol:"+ol.getVcfString,"note");
              reportln("    nl:"+nl.getVcfString,"note");
              reportln("    ml:"+ml.getVcfString,"note");
              //copyPresplits = copyPresplits + fline.ID;
              newHeader.addFormatLine(ol,walker=Some(this));
              newHeader.addFormatLine(nl,walker=Some(this));
              newHeader.addFormatLine(ml,walker=Some(this));

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
      val otherSplitLines : Seq[(String,String,SVcfCompoundHeaderLine,SVcfCompoundHeaderLine)] = vcfHeader.formatLines.filter{ fline => {
        adSplitLines.indexWhere{ case (oldID,newID,ohl,hl) => {
          fline.ID == oldID
        }} == -1 && (fline.Number == "R" || fline.Number == "A")
      }}.flatMap{ fline => {
              val newNum = if(fline.Number == "R") "3" else "2";
              reportln("Adding new FORMAT tag (otherSplitLines): "+fline.ID+"_presplit, based on "+fline.ID+": "+fline.getVcfString,"note");
              val ol = new SVcfCompoundHeaderLine("FORMAT",ID=fline.ID,Number= fline.Number, Type=fline.Type, desc="(Recoded for multiallelic split) "+fline.desc)
              val nl = new SVcfCompoundHeaderLine("FORMAT",ID=fline.ID + "_presplit", Number=".", Type=fline.Type, desc="(Raw value prior to multiallelic split) "+fline.desc)
              val ml = new SVcfCompoundHeaderLine("FORMAT",ID=fline.ID + threeAllelePrefix, Number=newNum, Type=fline.Type, desc="(Recoded for multiallelic split, with all other alleles collapsed into a 3rd allele) "+fline.desc, subType = Some(VcfTool.subtype_GtStyle3))
              reportln("    ol:"+ol.getVcfString,"note");
              reportln("    nl:"+nl.getVcfString,"note");
              reportln("    ml:"+ml.getVcfString,"note");
              newHeader.addFormatLine(ol,walker=Some(this));
              newHeader.addFormatLine(nl,walker=Some(this));
              newHeader.addFormatLine(ml,walker=Some(this));
              //copyPresplits = copyPresplits + fline.ID;
              Some((fline.ID,fline.ID, ol,nl))
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
      
      infoA.foreach{ oldID => {
         val fline = vcfHeader.infoLines.find{ ff => ff.ID == oldID }.get
         val nl = new SVcfCompoundHeaderLine("INFO",ID=fline.ID + "_presplit", Number=".", Type=fline.Type, desc="(Raw value prior to multiallelic split) "+fline.desc)
         newHeader.addInfoLine(nl,walker=Some(this));
      }}
      infoR.foreach{ oldID => {
         val fline = vcfHeader.infoLines.find{ ff => ff.ID == oldID }.get
         val nl = new SVcfCompoundHeaderLine("INFO",ID=fline.ID + "_presplit", Number=".", Type=fline.Type, desc="(Raw value prior to multiallelic split) "+fline.desc)
         newHeader.addInfoLine(nl,walker=Some(this));
      }}
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
      for( i <- Range(1,10)){
        tally("multiAlleSplit_nAlle"+i,0)
      }
      newHeader.reportAddedInfos(this)
      val sampNames = vcfHeader.titleLine.sampleList;
      newHeader.reportAddedInfos(this)
      return (addIteratorCloseAction(vcFlatMap(vcIter)(vc => {
        
        var warningSet = Set[String]();
        val vb = vc.getOutputLine();
        tally("multiAlleSplit_nAlle"+vc.alt.length,1)
        
        copyPresplits.foreach{ s => {
           vc.info.getOrElse( s, None ) match {
             case Some(k) => {
               vb.addInfo(s+"_presplit",k);
             }
             case None => {
               //do nothing
             }
           }
        }}
        
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
                  vb.genotypes.addGenotypeArray(oldTag+threeAllelePrefix,vc.genotypes.genotypeValues(fIdx).clone());
                }
            }}
            gtSplitLines.foreach{ case (oldTag,newTag,rawHeaderLine,headerLine) => {
                val fIdx = vc.genotypes.fmt.indexOf(oldTag);
                if(fIdx != -1){
                  vb.genotypes.addGenotypeArray(newTag,vc.genotypes.genotypeValues(fIdx).clone());
                  vb.genotypes.addGenotypeArray(oldTag+threeAllelePrefix,vc.genotypes.genotypeValues(fIdx).clone());
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
          val (copyAttrR,simpleCopyAttr) = tempAttrSet.partition{case (key,obj) => { infoR.contains(key) }};
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
              in_alt = Seq[String](alt),
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
                var fIdx = gs.fmt.indexOf(oldID);
                if(fIdx != -1){
       //reportln("===> GT-RECODE 1: fIdx="+fIdx+", oldID="+oldID+", newID="+newID+", alleIdxString="+alleIdxString,"deepDebug")
                  
                  gs.addGenotypeArray(newID,gs.genotypeValues(fIdx).clone());
                  fIdx = gs.fmt.indexOf(oldID);
       //reportln("===> GT-RECODE 2: fIdx="+fIdx+", oldID="+oldID+", newID="+newID+", alleIdxString="+alleIdxString,"deepDebug")
                  gs.addGenotypeArray(oldID+threeAllelePrefix,gs.genotypeValues(fIdx).map{g => {
                    if(g.contains('.')){
                      //g
                      g.split("[\\|/]").map{gg => if(gg == "0") "0" else if(gg == alleIdxString) "1" else if(gg == ".") "." else "2"}.sorted.mkString("/")
                    } else {
                      g.split("[\\|/]").map{gg => if(gg == "0") "0" else if(gg == alleIdxString) "1" else "2"}.sorted.mkString("/")
                    }
                  }})
                  fIdx = gs.fmt.indexOf(oldID);
                  gs.addGenotypeArray(oldID,gs.genotypeValues(fIdx).map{g => {
                    if(g.contains('.')){
                      //g
                      g.split("[\\|/]").map{gg => if(gg == "0") "0" else if(gg == alleIdxString) "1" else if(gg == ".") "." else otherAlleleChar}.sorted.mkString("/")
                    } else {
                      g.split("[\\|/]").map{gg => if(gg == "0") "0" else if(gg == alleIdxString) "1" else otherAlleleChar}.sorted.mkString("/")
                    }
                  }})
                }
              }}
              adSplitLines.foreach{ case (oldID,newID,ohl,hl) => {
                val fIdx = gs.fmt.indexOf(oldID);
                if(fIdx != -1){
                  gs.addGenotypeArray(newID,gs.genotypeValues(fIdx).clone());
                  if(ohl.Number == "R"){
                    gs.addGenotypeArray(oldID+threeAllelePrefix,gs.genotypeValues(fIdx).map{adString => {
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
                    
                    gs.addGenotypeArray(oldID,gs.genotypeValues(fIdx).map{adString => {
                      if(adString == "."){
                        "."
                      } else {
                        val ad = adString.split(",").map{ aa => if(aa == ".") 0 else string2int(aa) }
                        if(! ad.isDefinedAt(alleIdx)){
                          warning("Attempting to split "+oldID+" tag failed! Offending String: "+adString+" for variant:\n     "+vc.getSimpleVcfString(),"BADSECONDARYADTAG_SPLITMULTALLE",10);
                          "."
                        } else if(treatOtherAsRef) {
                          (ad.sum - ad(alleIdx)) + "," + ad(alleIdx);
                        } else {
                          ad(0) + "," + ad(alleIdx);
                        }
                        //  g.split("[\\|/]").map{gg => if(gg == "0") "0" else if(gg == alleIdxString) "1" else "2"}.sorted.mkString("/")
                      }
                    }})
                    
                  } else if(ohl.Number == "A"){
                    gs.addGenotypeArray(oldID+threeAllelePrefix,gs.genotypeValues(fIdx).map{adString => {
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
                    
                    gs.addGenotypeArray(oldID,gs.genotypeValues(fIdx).map{adString => {
                      if(adString == "."){
                        "."
                      } else {
                        val ad = adString.split(",").map{ aa => if(aa == ".") 0 else string2int(aa) }
                        if(! ad.isDefinedAt(alleIdx)){
                          warning("Attempting to split "+oldID+" tag failed! Offending String: "+adString+" for variant:\n     "+vc.getSimpleVcfString(),"BADSECONDARYADTAG_SPLITMULTALLE",10);
                          "."
                        } else {
                          ""+ad(alleIdx);
                        }
                      }

                      //  g.split("[\\|/]").map{gg => if(gg == "0") "0" else if(gg == alleIdxString) "1" else "2"}.sorted.mkString("/")
                    }})
                  } else {
                    warning("Attempting to split "+oldID+" tag failed!","IMPOSSIBLESTATE_BADSECONDARYADTAG_HASBADTYPE_SPLITMULTALLE",10);
                  }
                }
              }}
              otherSplitLines.foreach{ case (oldID,newID,ohl,hl) => {
                val fIdx = gs.fmt.indexOf(oldID);
                if(fIdx != -1){
                  gs.addGenotypeArray(newID,gs.genotypeValues(fIdx).clone());
                  if(ohl.Number == "R"){
                    gs.addGenotypeArray(oldID,gs.genotypeValues(fIdx).map{adString => {
                      if(adString == "."){
                        "."
                      } else {
                        val ad = adString.split(",") //.map{ aa => if(aa == ".") 0 else string2int(aa) }
                        if(! ad.isDefinedAt(alleIdx)){
                          warning("Attempting to split "+oldID+" tag failed! Offending String: "+adString+" for variant:\n     "+vc.getSimpleVcfString(),"BADSECONDARYADTAG_SPLITMULTALLE",10);
                          "."
                        } else {
                          ad(0) + "," + ad(alleIdx) + "";
                        }
                        //  g.split("[\\|/]").map{gg => if(gg == "0") "0" else if(gg == alleIdxString) "1" else "2"}.sorted.mkString("/")
                      }
                    }})
                  } else if(ohl.Number == "A"){
                    gs.addGenotypeArray(oldID,gs.genotypeValues(fIdx).map{adString => {
                      if(adString == "."){
                        "."
                      } else {
                        val ad = adString.split(",")  //.map{ aa => if(aa == ".") 0 else string2int(aa) }
                        if(! ad.isDefinedAt(alleIdx-1)){
                          warning("Attempting to split "+oldID+" tag failed! Offending String: "+adString+" for variant:\n     "+vc.getSimpleVcfString(),"BADSECONDARYADTAG_SPLITMULTALLE",10);
                          "."
                        } else {
                          ad(alleIdx-1) + "";
                        }
                      }

                      //  g.split("[\\|/]").map{gg => if(gg == "0") "0" else if(gg == alleIdxString) "1" else "2"}.sorted.mkString("/")
                    }})
                  } else {
                    warning("Attempting to split "+oldID+" tag failed!","IMPOSSIBLESTATE_BADSECONDARYADTAG_HASBADTYPE_SPLITMULTALLE",10);
                  }
                }
              }}
              
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
       
       SVcfWalkerUtils.RecodeClinVarCLN(
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
       newHeader.reportAddedInfos(this)
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
       outHeader.reportAddedInfos(this)
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
                                            valueName = OPTION_TAGPREFIX+"GTFILT", 
                                            argDesc = "",
                                            defaultValue = Some(OPTION_TAGPREFIX+"GTFILT")
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
  
  
  
  case class FilterGenotypesByStat(filter : String, filterTag : String = OPTION_TAGPREFIX+"GTFILT", gtTag : String, 
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
       outHeader.reportAddedInfos(this)
      val outIter = vcMap(vcIter){ vc => {
        val gtIdx = vc.genotypes.fmt.indexOf(gtTag);
        if(gtIdx > -1){
          vc.genotypes.sampList = sampList;
          vc.genotypes.sampGrp = Some(sampleToGroupMap);
          val vb = vc.getOutputLine();
          //vb.genotypes.genotypeValues = vb.genotypes.genotypeValues :+ Array.fill[String](vb.genotypes.genotypeValues(0).length)("0");
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
        } else {
          vc.getOutputLine();
        }
      }}
      
      return (outIter,outHeader);
    }
  }

  case class SelectGenotypesByStat(filter : String, gtTagA : String, gtTagB : String, missingString : Option[String] = None,
                                   newGTTag : String,
                                   groupFile : Option[String] = None, groupList : Option[String] = None, superGroupList  : Option[String] = None) extends SVcfWalker {
    def walkerName : String = "FilterGenotypesByStat"
    def walkerParams : Seq[(String,String)] = Seq[(String,String)](
        ("filter","\""+filter+"\""),
        ("gtTagA",gtTagA),
        ("gtTagB",gtTagB),
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
      outHeader.addFormatLine(new SVcfCompoundHeaderLine(in_tag = "FORMAT",ID = newGTTag, Number = "1", Type = "String", desc = "A composite genotype based on "+gtTagA+" and "+gtTagB+" based on predicate: [ "+filter+ " ] ",subType=Some(VcfTool.subtype_GtStyleUnsplit)), walker = Some(this) );

      outHeader.addWalk(this);
      val missingGeno = missingString.getOrElse("./."); // Range(0,ploidy).map{_ => "."}.mkString("/");
      
      val missingGenoArray = Array.fill[String](sampCt)(missingGeno);
      
      val addRawGt = false;
      val rango = Range(0,vcfHeader.sampleCt);
       outHeader.reportAddedInfos(this)
      val outIter = vcMap(vcIter){ vc => {
        val gtIdxA = vc.genotypes.fmt.indexOf(gtTagA);
        val gtIdxB = vc.genotypes.fmt.indexOf(gtTagB);
        
        if(gtIdxA > -1 || gtIdxB > -1){
          vc.genotypes.sampList = sampList;
          vc.genotypes.sampGrp = Some(sampleToGroupMap);
          val vb = vc.getOutputLine();
          //vb.genotypes.genotypeValues = vb.genotypes.genotypeValues :+ Array.fill[String](vb.genotypes.genotypeValues(0).length)("0");
          val gtValsA = if( gtIdxA == -1){ missingGenoArray } else { vc.genotypes.genotypeValues(gtIdxA) }
          val gtValsB = if( gtIdxB == -1){ missingGenoArray } else { vc.genotypes.genotypeValues(gtIdxB) }
          
          val newGtIdx = vb.genotypes.addGenotypeArray(newGTTag, gtValsB.clone());
          
          rango.withFilter{(i) => filterLogic.keep(vc,i)}.foreach{ (i) => {
              vb.genotypes.genotypeValues(newGtIdx)(i) = gtValsA(i);
          }}
          vb;
        } else {
          vc.getOutputLine();
        }
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
  
  
  /*
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
     
  }*/
  
  case class mergeSecondaryVcf(inputVCF : String, inputVcfTag : String, inputVcfName : String,
                               getTags : Option[List[String]] = None, dropTags : List[String] = List(),
                               tagPrefix : String = OPTION_TAGPREFIX+"",
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
          
          val vb = vc.getLazyOutputLine();
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
            notice("FOUND_MATCH: "+inputVcfTag,"MATCH_"+inputVcfTag+"_1",5);
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
          } else {
            notice("NONMATCH: "+inputVcfTag,"MATCH_"+inputVcfTag+"_0",5);
            
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
      
       newHeader.reportAddedInfos(this)
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
    val dModeList = Seq[String]("first","firstSkipMissing","majority_firstOnTies","majority_missOnTies",
                                "priority","prioritySkipMissing","majority_priorityOnTies")
    val dMode = dModeList.indexOf(decision);
    if(dMode == -1){
      error("ERROR: Illegal mode: "+decision+". Decision mode must be set to one of: "+dModeList.mkString(","));
    }
    val dModeImplementedList =  Seq[String]("first","firstSkipMissing","majority_firstOnTies",
                                "priority","prioritySkipMissing","majority_priorityOnTies") // Seq[String]("majority_firstOnTies");
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
          newHeader.addFormatLine(disLine);
          newHeader.addFormatLine(supLine);
          reportln("Merging GT-style tag "+t+" from callers: ["+foundTags.unzip._1.mkString(",")+"]","note");
          Some((t,foundTags));
        }
      }}
      val adFmtTags : Seq[(String,Seq[(String,String)])] = adStyleFmtTags.map{ tagString => { tagString.split("\\|") }}.map{ tagCells => (tagCells.head,tagCells.lift(1).getOrElse("No Desc")) }.flatMap{ case (t, desc) => {
        val nl = new SVcfCompoundHeaderLine("FORMAT",t, "R","Integer", desc, subType = Some(VcfTool.subtype_AlleleCounts)).addWalker(this);
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
       newHeader.reportAddedInfos(this)
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
            if(decision == "majority_firstOnTies" || decision == "majority_priorityOnTies"){
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
            } else if(decision == "first" || decision == "priority"){
              val sc = scTagIdx.map{ case (sc,tt,scIdx) => sc }
              rango.foreach{ i => {
                val gt = scGt.map{ gtArray => gtArray(i) };
                //val gtFilt = gt.filter{ g => ! g.contains('.')}
                //val scFilt = gt.zip(scTagIdx).withFilter{ case (g,(sc,tt,scIdx)) => ! g.contains('.')}.map{ case (g,(sc,tt,scIdx)) => sc }
                //if(! gtFilt.isEmpty){
                  val (gtFinal,disagree,supportSet) = getFirst(gt,sc);
                  mergedGt(i) = gtFinal;
                  disagreeGt(i) = disagree;
                  supportGt(i) = supportSet.padTo(1,".").mkString(",");
                //}
              }}
            } else if(decision == "firstSkipMissing" || decision == "prioritySkipMissing"){
              rango.foreach{ i => {
                val gt = scGt.map{ gtArray => gtArray(i) };
                
                val gtFilt = gt.filter{ g => ! g.contains('.')}
                val scFilt = gt.zip(scTagIdx).withFilter{ case (g,(sc,tt,scIdx)) => ! g.contains('.')}.map{ case (g,(sc,tt,scIdx)) => sc }
                if(! gtFilt.isEmpty){
                  val (gtFinal,disagree,supportSet) = getFirstNonMissing(gtFilt,scFilt);
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
    
    def getFirst(gt : Array[String],sc : Seq[String]) : (String,Int,Seq[String]) = {
      if(gt.distinct.length == 1){
        return ((gt.head,0,sc));
      }
      if(gt.head.contains('.')){
        return ((gt.head,0,Seq[String]()))
      }
      return ((gt.head,1,Range(0,gt.length).filter(ii => gt(ii) == gt.head).map{ii => sc(ii)}))
    }
    def getFirstNonMissing(gtFilt : Array[String],scFilt : Seq[String]) : (String,Int,Seq[String]) = {
      if(gtFilt.distinct.length == 1){
        return ((gtFilt.head,0,scFilt));
      }
      return ((gtFilt.head,1,Range(0,gtFilt.length).filter(ii => gtFilt(ii) == gtFilt.head).map{ii => scFilt(ii)}))
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
      new SVcfCompoundHeaderLine("INFO",  OPTION_TAGPREFIX+"EC_duplicateCt",   "1", "Integer", "The Number of duplicates found for this variant including this one."),
      new SVcfCompoundHeaderLine("INFO",  OPTION_TAGPREFIX+"EC_duplicateIdx",   "1", "Integer", "The index of this duplicate. If there are no duplicates of this variant this will just be equal to 0.")
    );
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
           new SVcfCompoundHeaderLine(in_tag = "INFO",ID = OPTION_TAGPREFIX+"ECINFO_QUAL_"+ mc, Number = "1", Type = "Float", desc = "QUAL field for caller "+mc)
       );
       vcfHeader.addInfoLine(
           new SVcfCompoundHeaderLine(in_tag = "INFO",ID = OPTION_TAGPREFIX+"ECINFO_ID_"+ mc, Number = "1", Type = "String", desc = "ID field for caller "+mc)
       );
       vcfHeader.addInfoLine(
           new SVcfCompoundHeaderLine(in_tag = "INFO",ID = OPTION_TAGPREFIX+"ECINFO_FILTER_"+ mc, Number = "1", Type = "String", desc = "FILTER field for caller "+mc)
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
          vbb.addInfo(OPTION_TAGPREFIX+"EC_duplicateCt",vbSeq.length.toString);
          vbb.addInfo(OPTION_TAGPREFIX+"EC_duplicateIdx",vbbIdx.toString);
        }}
        vbSeq;
      }}
      
    }}), vcfHeader)
    
  }*/
  
  /////////////////////////////////////////  /////////////////////////////////////////  /////////////////////////////////////////


  class DuplicateStats(countDupTag : String, byValue : Seq[String] = Seq[String]()) extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "DuplicateStat"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)]();
    
    //groupBySpan[A,B](iter : BufferedIterator[A])(f : (A => B))
    val cdt = countDupTag;
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      var errCt = 0;
      initNotice("DROP_VAR_N");
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      //countDupTag.foreach{ cdt => {
      outHeader.addInfoLine( new SVcfCompoundHeaderLine(in_tag = "INFO",ID = cdt+"_CT", Number = "1", Type = "Integer", desc = "Number of duplicates"));
      outHeader.addInfoLine( new SVcfCompoundHeaderLine(in_tag = "INFO",ID = cdt+"_IDX", Number = "1", Type = "Integer", desc = "Number of duplicates"));
      //}}
      /*val byValue.map{_.split(",")}.foreach{ bvraw => {
        val (bvprefix,bvtag) = (bvraw(0),bvraw(1))
        outHeader.addInfoLine( new SVcfCompoundHeaderLine(in_tag = "INFO",ID = bvprefix+"_CT", Number = "1", Type = "Integer", desc = "Number of duplicates"));
        outHeader.addInfoLine( new SVcfCompoundHeaderLine(in_tag = "INFO",ID = bvprefix+"_IDX", Number = "1", Type = "Integer", desc = "Number of duplicates"));
      }}*/
      tally(cdt+"_DUPSETCT",0)
      tally(cdt+"_DUPCT",0)
      outHeader.reportAddedInfos(this)
      (addIteratorCloseAction( iter = groupBySpan(vcIter.buffered){ v => { (v.chrom,v.pos) } }.flatMap{vg => {
        val swaps = vg.map{ v => (v.ref,v.alt.head)}.distinct.sorted
        swaps.flatMap{ case (r,a) => {
          val vbgRAW = vg.filter{ v => v.ref == r && v.alt.head == a }
          val vbg = vbgRAW.zipWithIndex.map{ case (v,ii) => {
            val vb = v.getOutputLine()
            vb.addInfo(cdt+"_CT",vbgRAW.length+"");
            vb.addInfo(cdt+"_IDX",ii+"");
            vb
          }}
          if(vbg.length > 1){
            tally(cdt+"_DUPSETCT",1);
            tally(cdt+"_DUPCT",vbg.length);
          }
          vbg
        }}
      }}, closeAction = (() => { 
        //do nothing
      })),outHeader)
    }
  }
  
  class longInfoVcfToStandardFormatVcf(countDupTag : String, sampField : String, samplist : Seq[String]) extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "DuplicateStat"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)]();
    
    //groupBySpan[A,B](iter : BufferedIterator[A])(f : (A => B))
    val cdt = countDupTag;
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      var errCt = 0;
      initNotice("DROP_VAR_N");
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      //countDupTag.foreach{ cdt => {
      outHeader.addInfoLine( new SVcfCompoundHeaderLine(in_tag = "INFO",ID = cdt+"_CT", Number = "1", Type = "Integer", desc = "Number of duplicates"));
      
      val infolines = vcfHeader.infoLines.map{ ff => ff.ID };
      val infolinesToCopy = infolines.filter{ ff => ff != sampField }
      
      vcfHeader.infoLines.foreach{ fx => {
          outHeader.addFormatLine( new SVcfCompoundHeaderLine(in_tag="FORMAT", ID=fx.ID, Number=fx.Number, Type=fx.Type, desc=fx.desc ));
      }}
      //if( ! infolines.contains("GT") ){
      //  outHeader.addFormatLine( new SVcfCompoundHeaderLine(in_tag="FORMAT", ID="GT", Number="1", Type="String", desc="Dummy GT" ));
      //}
      outHeader.titleLine = SVcfTitleLine( samplist );
      
      tally(cdt+"_DUPSETCT",0)
      tally(cdt+"_DUPCT",0)
      outHeader.reportAddedInfos(this)
      (addIteratorCloseAction( iter = groupBySpan(vcIter.buffered){ v => { (v.chrom,v.pos) } }.flatMap{vg => {
        val swaps = vg.map{ v => (v.ref,v.alt.head)}.distinct.sorted
        swaps.map{ case (r,a) => {          
          val vbgRAW = vg.filter{ v => v.ref == r && v.alt.head == a }
          val vbMAP = vbgRAW.map{ v => v.info.getOrElse(sampField,None) }.zip(vbgRAW).filter{ case (ss,vv) => {
            ss.isDefined
          }}.map{ case (ss,vv) => {
            (ss.get,vv)
          }}.toMap
          
          //filter{ v => v.info.getOrElse(sampField,None).isDefined }.map{ v => (v.info.getOrElse,v)}
          val vb = vbgRAW.head.getOutputLine();
          vcfHeader.infoLines.map{ fx => fx.ID }.foreach{ fx => {
            val vvi = vbgRAW.zipWithIndex.map{ case (v,ii) => {
              v.info.getOrElse(fx,None).getOrElse(".").replaceAll(",","/");
            }}.mkString(",");
            vb.addInfo(fx,vvi)
          }}
          //vb.genotypes = getGenotypeSet(genotypeStrings : vcfHeader.infoLines.map{ fx => fx.ID }.toSeq,)
//SVcfGenotypeSet(var fmt : Seq[String],
//                             var genotypeValues : Array[Array[String]])
          vb.in_genotypes = SVcfGenotypeSet( infolinesToCopy.toSeq,
                 infolinesToCopy.map{fx => {
                   samplist.map{ ss => {
                     vbMAP.get(ss).map{ vv => {
                       vv.info.getOrElse(fx,None).getOrElse(".").replaceAll(":","-")
                     }}.getOrElse(".")
                   }}.toArray
                 }}.toArray )
          
          vb.addInfo(cdt+"_CT",vbgRAW.length+"");
          
          if(vbgRAW.length > 1){
            tally(cdt+"_DUPSETCT",1);
            tally(cdt+"_DUPCT",vbgRAW.length);
          }
          vb
        }}
      }}, closeAction = (() => { 
        //do nothing
      })),outHeader)
    }
  }

  class DuplicateMerge(countDupTag : String) extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "DuplicateStat"
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)]();
    
    //groupBySpan[A,B](iter : BufferedIterator[A])(f : (A => B))
    val cdt = countDupTag;
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      var errCt = 0;
      initNotice("DROP_VAR_N");
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      
      outHeader.infoLines.foreach{ infoln => {
        outHeader.addInfoLine( new SVcfCompoundHeaderLine(in_tag = infoln.tag,ID = infoln.ID, Number = ".", Type = infoln.Type, desc = infoln.desc));
      }}
      outHeader.addInfoLine( new SVcfCompoundHeaderLine(in_tag = "INFO",ID = cdt+"_CT", Number = "1", Type = "Integer", desc = "Number of duplicates"));

      tally(cdt+"_DUPSETCT",0)
      tally(cdt+"_DUPCT",0)
      outHeader.reportAddedInfos(this)
      (addIteratorCloseAction( iter = groupBySpan(vcIter.buffered){ v => { (v.chrom,v.pos) } }.flatMap{vg => {
        val swaps = vg.map{ v => (v.ref,v.alt.head)}.distinct.sorted
        swaps.flatMap{ case (r,a) => {
          val vbgRAW = vg.filter{ v => v.ref == r && v.alt.head == a }
          val vb = vbgRAW.head.getOutputLine()
          if(vbgRAW.length > 1){
            tally(cdt+"_DUPSETCT",1);
            tally(cdt+"_DUPCT",vbgRAW.length);
            val currInfos = vbgRAW.flatMap{ vv => vv.info.keySet }.toSet
            currInfos.foreach{ cc => {
              vb.addInfo( cc, vbgRAW.map{ vv => {
                  vv.info.getOrElse(cc, None)
                }}.filter{ ii => {
                  ii.isDefined
                }}.map{ ii => ii.get }.mkString(",")
              )
            }}
          }
          vb.addInfo(cdt+"_CT",vbgRAW.length+"");
          Seq(vb)
          /*val vbg = vbgRAW.zipWithIndex.map{ case (v,ii) => {
            val vb = v.getOutputLine()
            vb.addInfo(cdt+"_CT",vbgRAW.length+"");
            vb.addInfo(cdt+"_IDX",ii+"");
            vb
          }}
          if(vbg.length > 1){
            tally(cdt+"_DUPSETCT",1);
            tally(cdt+"_DUPCT",vbg.length);
          }
          vbg*/
        }}
      }}, closeAction = (() => { 
        //do nothing
      })),outHeader)
    }
  }
  

  def ensembleMergeVariants(vcIters : Seq[Iterator[SVcfVariantLine]], 
                            headers : Seq[SVcfHeader], 
                            inputVcfTypes : Seq[String], 
                            //genomeFA : Option[String],
                            //windowSize : Int = 200, 
                            CC_ignoreSampleIds : Boolean = false, CC_ignoreSampleOrder : Boolean = false, 
                            singleCallerPriority : Seq[String]
                               // CC_ignoreSampleIds,   CC_ignoreSampleOrder
                            ) :  (Iterator[SVcfVariantLine],SVcfHeader) = {
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
            (infoLine.ID,(OPTION_TAGPREFIX+""+ mc + "_" + infoLine.ID,new SVcfCompoundHeaderLine(in_tag = "INFO",ID = OPTION_TAGPREFIX+""+ mc + "_" + infoLine.ID, Number = num, Type = infoLine.Type, desc = "For the caller "+mc+", " + cleanQuotes(infoLine.desc), subType = infoLine.subType)))
          }}.toMap)
    }}.toMap
    
    val customInfo = Seq[SVcfCompoundHeaderLine](
      new SVcfCompoundHeaderLine("INFO",  vcfCodes.ec_alle_callerSets,   "1", "String", "For each alt allele, which callers included the given allele."),
      new SVcfCompoundHeaderLine("INFO",  OPTION_TAGPREFIX+"EC_duplicateCt",   "1", "Integer", "The Number of duplicates found for this variant including this one."),
      new SVcfCompoundHeaderLine("INFO",  OPTION_TAGPREFIX+"EC_duplicateIdx",   "1", "Integer", "The index of this duplicate. If there are no duplicates of this variant this will just be equal to 0.")
    );
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
           new SVcfCompoundHeaderLine(in_tag = "INFO",ID = OPTION_TAGPREFIX+"ECINFO_QUAL_"+ mc, Number = "1", Type = "Float", desc = "QUAL field for caller "+mc)
       );
       vcfHeader.addInfoLine(
           new SVcfCompoundHeaderLine(in_tag = "INFO",ID = OPTION_TAGPREFIX+"ECINFO_ID_"+ mc, Number = "1", Type = "String", desc = "ID field for caller "+mc)
       );
       vcfHeader.addInfoLine(
           new SVcfCompoundHeaderLine(in_tag = "INFO",ID = OPTION_TAGPREFIX+"ECINFO_FILTER_"+ mc, Number = "1", Type = "String", desc = "FILTER field for caller "+mc)
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
    val singleCallerPriorityFull = singleCallerPriority ++ ( inputVcfTypes.filter( ivt => ! singleCallerPriority.contains(ivt) ) )
    val callerHeaders = singleCallerPriorityFull.zipWithIndex.map{ case (c,pi) => {
      val vi = inputVcfTypes.indexOf(c)
      (headers(vi),vi,pi)
    }};
    val callersWithSamps = callerHeaders.filter{ case (f,vi,pi) => { f.titleLine.sampleList.length > 0 }}
      
      //headers.zipWithIndex.filter{ case (f,x) => { f.titleLine.sampleList.length > 0 }}
    val sampNames = if(callersWithSamps.length > 0){
      callersWithSamps.head._1.titleLine.sampleList;
    } else {
      Seq[String]()
    }
    val sampCt = sampNames.length;
    val sampSet = sampNames.toSet;
    
    callersWithSamps.foreach{ case (f,vi,pi) => {
      if( f.titleLine.sampleList.length != sampCt) {
          error("Different number of samples found in the VCFs! ConcordanceCaller requires that the sample set be the same between the different VCFs. IF they are different and you still want to "+
                "merge, you can specify the overlapping sample set using the --inputKeepSamples parameter. Note that you may have to also add the --ccAllowSampleOrderDiff flag, if the samples are not "+
                "in the same order across all VCFs.")
      }
    }}
    
    val sampMatchIdx = (callersWithSamps.map{ case (f,vi,pi) => {
      ( if(pi == 0){
        (singleCallerPriorityFull(pi),None)
      } else if(CC_ignoreSampleIds){
        (singleCallerPriorityFull(pi),None)
      } else if(CC_ignoreSampleOrder){
        val currsamps = f.titleLine.sampleList;
        val currset : Set[String] = currsamps.toSet; 
        if( currset.size != f.titleLine.sampleList.length ){
          error("FORMATTING ERROR in VCF["+singleCallerPriorityFull(pi)+"]: sample IDs are not unique!")
        }
        /*if( sampSet.intersect( currset ).size != sampSet.size ){
              error("Different samples found in the VCFs! ConcordanceCaller requires that the sample set be the same between the different VCFs. IF they are different and you still want to "+
                    "merge, you can specify the overlapping sample set using the --inputKeepSamples parameter. Note that you may have to also add the --ccAllowSampleOrderDiff flag, if the samples are not "+
                    "in the same order across all VCFs.")
        }*/
        val ixx = sampNames.map{ ss => {
          val x = currsamps.indexOf(ss)
          if( x == -1) {
              error("Different samples found in the VCFs! "+
                    "Cannot find sample: "+ss+" in vcf: "+
                    "ConcordanceCaller requires that the sample set be the same between the different VCFs. IF they are different and you still want to "+
                    "merge, you can specify the overlapping sample set using the --inputKeepSamples parameter. Note that you may have to also add the --ccAllowSampleOrderDiff flag, if the samples are not "+
                    "in the same order across all VCFs.")
          }
          x
        }}.toSeq
        (singleCallerPriorityFull(pi),Some(ixx))
      } else {
        (singleCallerPriorityFull(pi),None)
      })
    }}).toMap
    
    // sampMatchIdx, callersWithSamps , singleCallerPriorityFull
    
    if(sampCt > 0){
      
      // CC_ignoreSampleIds,   CC_ignoreSampleOrder
      if( ! ((CC_ignoreSampleIds) | (CC_ignoreSampleOrder))) {
        callersWithSamps.foreach{ case (f,vi,pi) => {
          f.titleLine.sampleList.zip(sampNames).foreach{ case (s1,s2) => {
            if(s1 != s2){
              error("Sample IDs DO NOT MATCH! By default, ConcordanceCaller requires that the sample IDs be exactly the same and in the same order in each callers VCF file. "+
                    "If the samples have the same IDs but are in a different order, then use the --ccIgnoreSampIds flag. If instead the samples are in the same order but just have "+
                    "different sample IDs, then use --ccAllowSampleOrderDiff to ignore the sample IDs. The output ordering and/or names will come from the highest priority VCF "+
                    "(or just the first VCF, if no priority list is specified).")
            }
          }}
        }}
      }
    }
    
    
    
    headers.zipWithIndex.foreach{ case (h,i) => {
      if(h.titleLine.sampleList.length != sampNames.length){
        error("Header for VCF "+inputVcfTypes(i)+" has "+h.titleLine.sampleList.length+" samples instead of "+sampNames.length);
      }
    }}
    /*headers.zipWithIndex.foreach{ case (h,i) => {
      h.titleLine.sampleList.zip(sampNames).zipWithIndex.foreach{ case ((n1,n2),j) => {
        if( n1 != n2){
          error("Sample name "+j+" does not match between VCFs! VCF "+inputVcfTypes.head+" has sample "+n1+", VCF "+inputVcfTypes(i)+" has sample "+n2);
        }
      }}
    }}*/
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
              if(! currInfoLines.contains(oldTag)){
                report("ERROR INCOMING: Cannot find tag \""+oldTag+"\" in callerName=\""+callerName+"\"\n"+
                    "    available tags are: ["+currInfoLines.map{_._1}.mkString(",")+"]",
                    "note")
              }
              
              val (newTag,infoLine) = currInfoLines(oldTag);
              /*val fixedFieldVal = if(numAlle > 1 && vc.alt.length == 1 && (infoLine.Number == "R" || infoLine.Number == "A")){
                fieldVal + ",.";
              } else {
                fieldVal;
              }*/
              vb.addInfoOpt(newTag,fieldVal);
            }}
            if( vc.genotypes.genotypeValues.nonEmpty && vc.genotypes.fmt.nonEmpty ){
              val sampidx = sampMatchIdx.get(callerName);
              if(sampidx.isEmpty){
                error("IMPOSSIBLE STATE: sampidx.isEmpty! This should never happen. If you see this error, than you have somehow exposed a bug. Please submit a bug report to the issues page: \"https://github.com/hartleys/vArmyKnife/issues\"")
              }
              vc.genotypes.fmt.zipWithIndex.foreach{ case (oldTag,idx) => {
                if(! currFmtLines.contains(oldTag)){
                  warning("Fatal error: Tag: "+oldTag+" not found in given header!\n Found header tags: "+currFmtLines.keys.toSeq.sorted.mkString(","),"PREERROR_WARNING",-1)
                }
                val (newTag,fmtLine) = currFmtLines(oldTag);
                sampidx.get match {
                  case Some(sindices) => {
                    vb.genotypes.addGenotypeArray(newTag,sindices.toArray.map{ si => {
                      vc.genotypes.genotypeValues(idx)(si);
                    }})
                  }
                  case None => {
                    vb.genotypes.addGenotypeArray(newTag,vc.genotypes.genotypeValues(idx));
                  }
                }
                
              }}
              /*val currGt = vc.genotypes.genotypeValues(0);
              currGt.zipWithIndex.foreach{ case (gtString,i) => {
                val gt = gtString.split("[\\|/]").sorted(genotypeOrdering).mkString("/");
                if(finalGt(i) == "./."){
                  finalGt(i) = gt;
                } else {
                  //do nothing, higher priority call takes precedence!
                }
              }}
              vb.genotypes.genotypeValues(0) = finalGt*/
            }
            
          }}
          /*if( vcSeq.head.genotypes.genotypeValues.nonEmpty && vcSeq.head.genotypes.fmt.nonEmpty ){
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
          }*/
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
            
            //if( vcSeq.head.genotypes.genotypeValues.nonEmpty && vcSeq.head.genotypes.fmt.nonEmpty ){
              vbb.genotypes.genotypeValues = vbb.genotypes.genotypeValues.zip(vbb.genotypes.fmt).withFilter{ case (garray,fmt) => {
                ! newFmtFieldSet.contains(fmt);
              }}.map{ case (garray,fmt) => garray }
              vbb.genotypes.fmt = vbb.genotypes.fmt.filter{ fmt => {
                ! newFmtFieldSet.contains(fmt);
              }}
              vc.genotypes.fmt.zipWithIndex.foreach{ case (oldTag,idx) => {
                val (newTag,fmtLine) = currFmtLines(oldTag);
                vbb.genotypes.addGenotypeArray(newTag,vc.genotypes.genotypeValues(idx));
              }}
            //}
            vc.info.map{ case (oldTag,fieldVal) => {
              val (newTag,infoLine) = currInfoLines(oldTag);
              vbb.addInfoOpt(newTag,fieldVal);
            }}

            vbb
          }}
        }}
        vbSeq.zipWithIndex.map{ case (vbb,vbbIdx) => {
          vbb.addInfo(OPTION_TAGPREFIX+"EC_duplicateCt",vbSeq.length.toString);
          vbb.addInfo(OPTION_TAGPREFIX+"EC_duplicateIdx",vbbIdx.toString);
        }}
        vbSeq;
      }}
      
    }}), vcfHeader)
    
  }
  
//INCOMPLETE!!!
  def mergeCompareVariants(vcIters : Seq[Iterator[SVcfVariantLine]], 
                            headers : Seq[SVcfHeader], 
                            inputVcfTypes : Seq[String], //genomeFA : Option[String],
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
            (infoLine.ID,(OPTION_TAGPREFIX+""+ mc + "_" + infoLine.ID,new SVcfCompoundHeaderLine(in_tag = "INFO",ID = OPTION_TAGPREFIX+""+ mc + "_" + infoLine.ID, Number = num, Type = infoLine.Type, desc = "For the caller "+mc+", " + cleanQuotes(infoLine.desc), subType = infoLine.subType)))
          }}.toMap)
    }}.toMap
    
    val customInfo = Seq[SVcfCompoundHeaderLine](
      new SVcfCompoundHeaderLine("INFO",  vcfCodes.ec_alle_callerSets,   "1", "String", "For each alt allele, which callers included the given allele."),
      new SVcfCompoundHeaderLine("INFO",  OPTION_TAGPREFIX+"EC_duplicateCt",   "1", "Integer", "The Number of duplicates found for this variant including this one."),
      new SVcfCompoundHeaderLine("INFO",  OPTION_TAGPREFIX+"EC_duplicateIdx",   "1", "Integer", "The index of this duplicate. If there are no duplicates of this variant this will just be equal to 0.")
    );
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
          vbb.addInfo(OPTION_TAGPREFIX+"EC_duplicateCt",vbSeq.length.toString);
          vbb.addInfo(OPTION_TAGPREFIX+"EC_duplicateIdx",vbbIdx.toString);
        }}
        vbSeq;
      }}
      
    }}), vcfHeader)
    
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
  
  case class VcfExtractRegionFromSorted(region : String, windowSize : Option[Int] = None) extends SVcfWalker {
    def walkerName : String = "VcfExtractRegionFromSorted"
    def walkerParams : Seq[(String,String)]= Seq[(String,String)](
        ("region","\""+region+"\""),
        ("windowSize","\""+windowSize.getOrElse(0)+"\"")
    )
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
       val outHeader = vcfHeader.copyHeader;
       outHeader.addWalk(this);
       
       val chrom = region.split("[:]").head;
       val start = region.split("[:]").lastOption.map{ x => {
         x.split("-",2).head
       }}.filter( _ != "" ).map{s => {
         val xx = string2intOpt(s);
         if(xx.isEmpty){
           error("Error extracting genomic location: \""+region+"\": "+s+" is not an integer!");
         }
         xx.get - windowSize.getOrElse(0)
       }}
       val end = region.split("[:]").lastOption.flatMap{ x => {
         x.split("-",2).lift(1);
       }}.filter( _ != "" ).map{s => {
         val xx = string2intOpt(s);
         if(xx.isEmpty){
           error("Error extracting genomic location: \""+region+"\": "+s+" is not an integer!");
         }
         xx.get + windowSize.getOrElse(0)
       }}
       
       if(! vcIter.hasNext){
         return (vcIter,outHeader);
       } else {
         val vcBiter = vcIter.buffered;
         if(chrom.startsWith("chr") != vcBiter.head.chrom.startsWith("chr")){
           warning("WARNING: region to extract: \""+region+"\" appears to use a different chromosome format than the VCF chromosome: "+vcBiter.head.chrom,"CHROM_FORMAT_MISMATCH",-1)
         }
         reportln("Scanning through VCF for region: \""+region+"\"","note")
         skipWhile(vcBiter){ vc => {
           vc.chrom != chrom || start.map{ s => vc.pos < s }.getOrElse(false)
         }}
         reportln("Entered region: \""+region+"\"","note")
         (vcBiter.takeWhile( vc => { vc.chrom == chrom && end.map{ e => vc.pos <= e }.getOrElse(true) }), outHeader)
       }
       

    }
  }
  
  case class VcfExpressionFilter(filterExpr : String, explainFile : Option[String] = None, filterID : Option[String] = None) extends SVcfWalker {

    /*def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
       (vcIter, vcfHeader)
    }*/
    def walkerName : String = "VcfExpressionFilter"
    def walkerParams : Seq[(String,String)]= Seq[(String,String)](
        ("filterExpr","\""+filterExpr+"\""),
        ("filterID","\""+filterID.getOrElse("None")+"\""),
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
       checkSVcfFilterLogicParse( filterLogic = filter, vcfHeader = vcfHeader );
       
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
  
  case class VcfGtExpressionTag( expr : String, tagID : String, tagDesc : String, style : String = "GTCT",
                                 groupFile : Option[String] = None, groupList : Option[String] = None, superGroupList  : Option[String] = None ) extends SVcfWalker {
    def walkerName : String = "VcfGtExpressionTag."+tagID;
    //val style = styleOpt.getOrElse("CT")
    def walkerParams : Seq[(String,String)]= Seq[(String,String)](
        ("expr","\""+expr+"\""),
        ("tagID","\""+tagID+"\""),
        ("tagDesc","\""+tagDesc+"\""),
        ("style","\""+style+"\"")
    )
    val parser : SFilterLogicParser[(SVcfVariantLine,Int)] = internalUtils.VcfTool.sGenotypeFilterLogicParser;
    val filter : SFilterLogic[(SVcfVariantLine,Int)] = parser.parseString(expr);
    val styleType = if(style == "GTCT" || style == "GT"){
      "Integer"
    } else if(style == "GTFRAC" || style == "GTPCT"){
      "Float"
    } else { 
      "String"
    }
    //if(!Set("GTCT","GTPCT","GTFRAC","GTTAG").contains(style)){
    //  error("Unrecognized/invalid gt expression style: \""+style+"\"")
    //}
    
    val (sampleToGroupMap,groupToSampleMap,groups) : (scala.collection.mutable.AnyRefMap[String,Set[String]],
                           scala.collection.mutable.AnyRefMap[String,Set[String]],
                           Vector[String]) = getGroups(groupFile, groupList, superGroupList);
    
    
    
    //val parser = internalUtils.VcfTool.SVcfFilterLogicParser();
    //val filter = parser.parseString(filterExpr);
    reportln("Parsed filter:\n   "+filter.printTree(),"note");
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
       val sampList = vcfHeader.titleLine.sampleList.toList;
       val sampCt   = vcfHeader.sampleCt;
       val outHeader = vcfHeader.copyHeader;
       var (rawTag,filtTag,newTag,backTag,overWrite,recodeStyle) : (String,String,String,Option[String],Boolean,String) = ("","","",None,false,"");
       if(style == "GTTAG"){
         outHeader.addFormatLine(new SVcfCompoundHeaderLine("FORMAT",tagID,Number = "1", Type="Integer",desc=tagDesc).addWalker(this));
       } else if(style == "GTrecodeMultAlle"){
         val tags = tagID.split(",");
         if(tags.length != 3){
           error("for GTrecodeMultAlle function, tagID field must have 3 entries: fromTag, toTag, and other-alt-allele recode string!");
         }
         rawTag = tags(0); newTag = tags(1); recodeStyle = tags(2);
         outHeader.addFormatLine(new SVcfCompoundHeaderLine("FORMAT",tagID,Number = "1", Type="String", desc=tagDesc).addWalker(this));
       } else if(style == "GTTAGANDCOUNT"){
         outHeader.addFormatLine(new SVcfCompoundHeaderLine("FORMAT",tagID,Number = "1", Type="Integer", desc=tagDesc).addWalker(this));
         outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",tagID+"_CT",Number = "1", Type="Integer",desc="Number of samples tagged as "+tagID+". " +tagDesc).addWalker(this));
       } else if(style == "GTTAGRENAME"){
         val tags = tagID.split(",");
         if(tags.length != 2 && tags.length != 3){
           error("for GTTAGRENAME expression, tagID field must have 2 entries: fromTag, toTag!");
         }
         rawTag = tags(0); newTag = tags(1);
         val idx = outHeader.formatLines.indexWhere( rt => rt.ID == rawTag)
         if(idx == -1){
           error("Error: rawTag "+rawTag + " not found in header!")
         }
         outHeader.formatLines = outHeader.formatLines.updated(idx,outHeader.formatLines(idx).updateID(newTag).updateDesc(tagDesc))
       } else if(style == "GTTAGCOPY"){
         val tags = tagID.split(",");
         if(tags.length != 2 && tags.length != 3){
           error("for GTCOPY expression, tagID field must have 2 entries: fromTag, toTag!");
         }
         rawTag = tags(0); newTag = tags(1);
         overWrite = vcfHeader.formatLines.find(rt => rt.ID == rawTag).isDefined;
         val oldDesc = vcfHeader.formatLines.find(rt => rt.ID == rawTag).map{rt => {
             outHeader.addFormatLine(rt.updateID(newTag).updateDesc(tagDesc));
             rt.desc
         }}.getOrElse({
             error("Error: rawTag "+rawTag + " not found in header!")
             ""
         })
       } else if(style == "GTFILT"){
         val tags = tagID.split(",");
         if(tags.length != 3 && tags.length != 4){
           error("for GTFILT expression, tagID field must have 3-4 entries: rawGTtag, filterTag, filteredGtTag and optionally RawCopyGtTag!");
         }
         rawTag = tags(0); filtTag = tags(1); newTag = tags(2); backTag = tags.lift(3);
         //val (rawTag,filtTag,newTag, backTag) = (tags(0),tags(1),tags(2), tags.lift(3));
         outHeader.addFormatLine(new SVcfCompoundHeaderLine("FORMAT",newTag,Number = "1", Type="String",desc="Filtered genotype value, "+tagDesc + " (Based on GT tag "+rawTag+", see also tag "+filtTag+")").addWalker(this).updateSubtype(Some("GtStyle")));
         outHeader.addFormatLine(new SVcfCompoundHeaderLine("FORMAT",filtTag,Number = "1", Type="Integer",desc="Filter tag. 1 if genotype is filtered "+tagDesc + " (See also tags "+rawTag+" and "+newTag+")").addWalker(this));
         backTag.foreach{ bt => {
           val oldDesc = vcfHeader.formatLines.find(rt => rt.ID == rawTag).map{rt => {
             rt.desc
           }}.getOrElse({
             error("Error: rawTag "+rawTag + " not found in header!")
             ""
           })
           outHeader.addFormatLine(new SVcfCompoundHeaderLine("FORMAT",newTag,Number = "1", Type="String",desc="Pre-filtered genotype value, "+tagDesc + " (Copied from GT tag "+rawTag+", see also tag "+filtTag+") (old desc: "+oldDesc).addWalker(this).updateSubtype(Some("GtStyle")))
         }}
       } else if(style == "GT" || style == "GTCT" || style == "GTPCT" || style == "GTFRAC"){
         outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",tagID,Number = "1", Type=styleType,desc=tagDesc).addWalker(this));
       } else {
         error("Unrecognized genotype function: " +style+". "+
               "\n   Legal options are: \n"+
               "     GTTAG: adds new FORMAT field with a boolean expression\n"+
               "     GTTAGRENAME: Renames an existing FORMAT field with a new ID and description.\n"+
               "     GTTAGCOPY: Copies an existing FORMAT field, making a second identical field with a different name and description.\n")+
               "     GTFILT: Creates a new genotype field or replaces an existing one with a copy of an existing one, but filtered based on a given genotype expression\n"+
               "     GT or GTCT: Counts the number of samples that satisfy a genotype expression\n"+
               "     GTPCT: Counts the percentage of samples that satisfy a genotype expression\n"+
               "     GTFRAC: Counts the fraction of samples that satisfy a genotype expression\n"+
               "     GTTAGANDCOUNT: adds new FORMAT field as the GTTAG function and also creates a count INFO field as the CT function. The info field will be named tagID_CT"
       }
       outHeader.addWalk(this);
        outHeader.reportAddedInfos(this)
       (vcMap(vcIter){ vc => {
             vc.genotypes.sampList = sampList;
             vc.genotypes.sampGrp = Some(sampleToGroupMap);
             val vb = vc.getOutputLine();
             if(style == "GTTAG" || style == "GTTAGANDCOUNT"){
               val gttag = Range(0,vcfHeader.sampleCt).map{ii => {
                 if(filter.keep((vc,ii))){
                   "1"
                 } else {
                   "0"
                 }
               }}.toArray
               vb.genotypes.addGenotypeArray(tagID,gttag);
               if(style == "GTTAGANDCOUNT"){
                 val ct = gttag.count(xx => xx == "1");
                 vb.addInfo(tagID+"_CT","" + ct);
                 tally(""+tagID+"_CT",ct)
               }
               
             } else if(style == "GTTAGCOPY"){
               val gtidx =  vb.genotypes.fmt.indexOf(rawTag);
               if(gtidx == -1 && overWrite){
                 vb.genotypes.dropGenotypeArray(newTag);
               } else {
                 vb.genotypes.addGenotypeArray(newTag,vb.genotypes.genotypeValues(gtidx).clone());
               }
             } else if(style == "GTTAGRENAME"){
               val gtidx =  vb.genotypes.fmt.indexOf(rawTag);
               if(gtidx != -1){
                 vb.genotypes.fmt = vb.genotypes.fmt.updated(gtidx,newTag);
               }
             } else if(style == "GTrecodeMultAlle"){
               val gtidx =  vb.genotypes.fmt.indexOf(rawTag);
               if(gtidx == -1 && overWrite){
                 vb.genotypes.dropGenotypeArray(newTag);
               } else if(recodeStyle.length == 1){
                 val recodeChar = recodeStyle.head
                 vb.genotypes.addGenotypeArray(newTag,vb.genotypes.genotypeValues(gtidx).map{g => {
                   g.replace('2',recodeChar)
                 }});
               } else {
                 vb.genotypes.addGenotypeArray(newTag,vb.genotypes.genotypeValues(gtidx).map{g => {
                   g.replaceAll("2",recodeStyle)
                 }});
               }
             } else if(style == "GTFILT"){
               val gtidx =  vb.genotypes.fmt.indexOf(rawTag);
               backTag.foreach{ bt => {
                 if(gtidx == -1){
                   vb.genotypes.addGenotypeArray(bt,Array.fill[String](vcfHeader.sampleCt)("./."))
                 } else {
                   vb.genotypes.addGenotypeArray(bt,vc.genotypes.genotypeValues(gtidx).clone())
                 }
               }}
               
               val (filtgt,filtbool) = if(gtidx == -1){
                 (Array.fill[String](vcfHeader.sampleCt)("./."),
                  Array.fill[String](vcfHeader.sampleCt)("0"))
               } else {
                 val gt = vc.genotypes.genotypeValues(gtidx)
                 gt.zipWithIndex.map{ case (g,ii) => {
                    if(filter.keep((vc,ii))){
                      (g,"0")
                    } else {
                      ("./.","1")
                    }
                 }}.toArray.unzip
               }
               vb.genotypes.addGenotypeArray(newTag,filtgt)
               vb.genotypes.addGenotypeArray(filtTag,filtbool)

             } else if(style == "GT" || style == "GTCT" || style == "GTPCT" || style == "GTFRAC"){
               val ct = Range(0,vcfHeader.sampleCt).count{ii => {
                 filter.keep((vc,ii))
               }}
               tally(""+tagID+"",ct)
               if(style == "GTCT" || style == "GT"){
                 vb.addInfo(tagID,"" + ct);
               } else if(style == "GTPCT"){
                 vb.addInfo(tagID,"" + (100.toDouble * ct.toDouble / vcfHeader.sampleCt.toDouble));
               } else if(style == "GTFRAC"){
                 vb.addInfo(tagID,"" + (ct.toDouble / vcfHeader.sampleCt.toDouble));
               }
             }
             
             vb
       }}, outHeader)
       
    }
    
  }

  
  //checkSVcfFilterLogicParse[A]( filterLogic : SFilterLogic[A], vcfHeader : SVcfHeader )
  case class VcfExpressionTag(expr : String, tagID : String, tagDesc : String, geneTagString : Option[String] = None, subGeneTagString : Option[String] = None, geneList : Option[List[String]] = None) extends SVcfWalker {

    /*def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
       (vcIter, vcfHeader)
    }*/
    //walkerTitle = "tagVariantsExpression."+tagID
    
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
       initNotice("TAGGED_"+tagID+"_0")
       initNotice("TAGGED_"+tagID+"_1")
       
       checkSVcfFilterLogicParse( filterLogic = filter, vcfHeader = vcfHeader );
       
       if(isGeneTagExpr){
           outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",tagID,Number = ".", Type="String",desc=tagDesc).addWalker(this));
           outHeader.addWalk(this);
            outHeader.reportAddedInfos(this)
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
            outHeader.reportAddedInfos(this)
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

  
  

  //checkSVcfFilterLogicParse[A]( filterLogic : SFilterLogic[A], vcfHeader : SVcfHeader )
  case class VcfSimpleExpressionTag(expr : String, tagID : String, tagDesc : String) extends SVcfWalker {

    def walkerName : String = "VcfExpressionTag."+tagID;
    def walkerParams : Seq[(String,String)]= Seq[(String,String)](
        ("expr","\""+expr+"\""),
        ("tagID","\""+tagID+"\""),
        ("tagDesc","\""+tagDesc+"\"")
    )

    val parser : SFilterLogicParser[SVcfVariantLine] = internalUtils.VcfTool.sVcfFilterLogicParser;
    val filter : SFilterLogic[SVcfVariantLine] = parser.parseString(expr);
    //val parser = internalUtils.VcfTool.SVcfFilterLogicParser();
    //val filter = parser.parseString(filterExpr);
    reportln("Parsed filter:\n   "+filter.printTree(),"note");
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine],SVcfHeader) = {
       val outHeader = vcfHeader.copyHeader;
       initNotice("TAGGED_"+tagID+"_0")
       initNotice("TAGGED_"+tagID+"_1")
       
       checkSVcfFilterLogicParse( filterLogic = filter, vcfHeader = vcfHeader );
           outHeader.addInfoLine(new SVcfCompoundHeaderLine("INFO",tagID,Number = "1", Type="Integer",desc=tagDesc).addWalker(this));
           outHeader.addWalk(this);
            outHeader.reportAddedInfos(this)
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









 


















