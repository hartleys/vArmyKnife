package internalTests

import htsjdk.variant._;
import htsjdk.variant.variantcontext._;
import htsjdk.variant.vcf._;
import java.io.File;
import scala.collection.JavaConversions._
import java.io._;
import internalUtils.commandLineUI._;
import internalUtils.Reporter._;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import java.io.FileInputStream;
import java.io.BufferedReader;
 
import internalUtils.stdUtils._;

object trackToSpan {
  
  class trackToSpan extends CommandLineRunUtil {
     override def priority = 100;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "GerpTrackToSpans", 
          quickSynopsis = "", 
          synopsis = "", 
          description = " " + ALPHA_WARNING,   
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
                    new UnaryArgument( name = "version",
                                         arg = List("--version"), // name of value
                                         argDesc = "Flag. If raised, print version." // description
                                       ) ::
                    new FinalArgument[String](
                                         name = "infile",
                                         valueName = "infile",
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
        //trackToSpan.run(
        trackToSpan.runConservedRegionSpans(
             parser.get[String]("infile"),
             parser.get[String]("outfile")
           );
     }
   }
   
   def run(infile : String, outfile : String) {
    val tarInput : TarArchiveInputStream = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(infile)));
    
    val writer = internalUtils.fileUtils.openWriterSmart(outfile);
    
    var currentEntry : TarArchiveEntry = tarInput.getNextTarEntry();
    while (currentEntry != null) {
        val br : BufferedReader = new BufferedReader(new InputStreamReader(tarInput)); // Read directly from tarInput
        val intarfilename = currentEntry.getName();
        reportln("Reading file from tarball: "+intarfilename,"note");
        
        val chrom = intarfilename.split("\\.")(0);
        reportln("writing data for chrom: " + chrom,"note");
        
        var line = br.readLine();
        var currScore = string2double(line.split("\t")(0)).toInt;
        var currStart = 0;
        var pos = 0;
        pos = pos + 1;
        
        var spanCt = 0;
        report("   ","progress");
        line = br.readLine();
        while(line != null){
          val score = string2double(line.split("\t")(0)).toInt;
          if(currScore != score){
            if(currScore >= 2){
              writer.write(chrom+"\t"+currStart+"\t"+pos+"\t"+currScore+"\n");
              spanCt = spanCt + 1;
            }
            currStart = pos;
            currScore = score;
          }
          pos = pos + 1;
          line = br.readLine();
          if(pos % 2000000 == 0){
            if(pos % 10000000 == 0){
              if(pos % 40000000 == 0){
                report(". ["+pos+" bp.] [Time: "+getDateAndTimeString+"] [spanCt=" + spanCt+", "+math.round(pos.toFloat / spanCt.toFloat)+" bp/span]\n   ","progress")
              } else {
                report(". ","progress")
              }
            } else {
              report(".","progress")
            }
          }
        }
        reportln("chrom: " + chrom + " done. "+ pos + "bp, " + spanCt + " spans." ,"note");
        
        //System.out.println("For File = " + currentEntry.getName());
        //var line : String = null;
        //while ((line = br.readLine()) != null) {
        //    System.out.println("line="+line);
        //}
        currentEntry = tarInput.getNextTarEntry(); // You forgot to iterate to the next file
    }
    writer.close();
   }
   def runConservedRegionSpans(infile : String, outfile : String, thresh : Int = 2) {
    val tarInput : TarArchiveInputStream = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(infile)));
    
    val writer = internalUtils.fileUtils.openWriterSmart(outfile);
    
    var currentEntry : TarArchiveEntry = tarInput.getNextTarEntry();
    while (currentEntry != null) {
        val br : BufferedReader = new BufferedReader(new InputStreamReader(tarInput)); // Read directly from tarInput
        val intarfilename = currentEntry.getName();
        reportln("Reading file from tarball: "+intarfilename,"note");
        
        val chrom = intarfilename.split("\\.")(0);
        reportln("writing data for chrom: " + chrom,"note");
        
        var line = br.readLine();
        //var currScore = string2double(line.split("\t")(0)).toInt;
        var currIsHigh = string2double(line.split("\t")(0)).toInt >= thresh;
        var currStart = 0;
        var pos = 0;
        pos = pos + 1;
        
        var spanCt = 0;
        var spanRegion = 0;
        report("   ","progress");
        line = br.readLine();
        while(line != null){
          val score = string2double(line.split("\t")(0)).toInt;
          if(currIsHigh && score < thresh){
            writer.write(chrom+"\t"+currStart+"\t"+pos+"\n");
            spanRegion = spanRegion + (pos - currStart);
            spanCt = spanCt + 1;
          } else if((! currIsHigh) && score >= thresh){
            currStart = pos;
          }
          currIsHigh = score >= thresh;
          pos = pos + 1;
          line = br.readLine();
          if(pos % 2000000 == 0){
            if(pos % 10000000 == 0){
              if(pos % 40000000 == 0){
                report(". ["+pos+" bp.][Time: "+getDateAndTimeString+"][spanCt=" + spanCt+", "+math.round(pos.toFloat / spanCt.toFloat)+" bp/span, "+spanRegion+"bp spanned]\n   ","progress")
              } else {
                report(". ","progress")
              }
            } else {
              report(".","progress")
            }
          }
        }
        reportln("chrom: " + chrom + " done. "+ pos + "bp, " + spanCt + " spans." ,"note");
        
        //System.out.println("For File = " + currentEntry.getName());
        //var line : String = null;
        //while ((line = br.readLine()) != null) {
        //    System.out.println("line="+line);
        //}
        currentEntry = tarInput.getNextTarEntry(); // You forgot to iterate to the next file
    }
    
    writer.close();
   }
  
  
/////////////////////////////////////////////////////////////////////
  
  
  
  
  
}