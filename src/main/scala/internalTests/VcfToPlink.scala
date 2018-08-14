package main.scala.internalTests

import htsjdk.variant._;
import htsjdk.variant.variantcontext._;
import htsjdk.variant.vcf._;
import java.io.File;
//import scala.collection.JavaConversions._
import java.io._;
import internalUtils.commandLineUI._;
import internalUtils.Reporter._;
import internalUtils.stdUtils._;
import internalUtils.fileUtils._;
import scala.collection.JavaConverters._

import java.io.FileOutputStream._;

/*
 * WARNING: UNTESTED!
 */
object VcfToPlink {
  
  class runVcfToPlink extends CommandLineRunUtil {
     override def priority = 100;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "VcfToPlink", 
          quickSynopsis = "", 
          synopsis = "", 
          description = "Converts VCF file to Plink files.",   
          argList = 
                    
                    new UnaryArgument( name = "version",
                                         arg = List("--version"), // name of value
                                         argDesc = "Flag. If raised, print version." // description
                                       ) ::
                    new UnaryArgument( name = "rsOnly",
                                         arg = List("--rsOnly"), // name of value
                                         argDesc = "Flag. If raised, only include variants with a single RS number as their ID." // description
                                       ) ::
                   // new UnaryArgument( name = "bin",
                   //                      arg = List("--binaryFmt"), // name of value
                   //                     argDesc = "Flag. If raised, output in binary-PED format (BED)." // description
                   //                    ) ::
                    new FinalArgument[String](
                                         name = "infile",
                                         valueName = "infile.vcf",
                                         argDesc = "infile" // description
                                        ) ::
                    new FinalArgument[String](
                                         name = "outfileprefix",
                                         valueName = "outfileprefix",
                                         argDesc = "The output file prefix."// description
                                        ) ::
                    internalUtils.commandLineUI.CLUI_UNIVERSAL_ARGS );
      
     def run(args : Array[String]) { 
       val out = parser.parseArguments(args.toList.tail);
      
       if(out){
         convert(
             parser.get[String]("infile"),
             parser.get[String]("outfileprefix"),
             parser.get[Boolean]("rsOnly")//,
             //parser.get[Boolean]("bin")
             //parser.get[Boolean]("infileDir")
             );
       }
     }
   }
  
  def convert(infile : String, outfileprefix : String, rsOnly : Boolean){
    val vcfReader = new VCFFileReader(new File(infile),false);
    val vcfHeader = vcfReader.getFileHeader();
    
    val vcfIterator = vcfReader.asScala.iterator;
    val vcfIteratorProg = wrapIteratorWithProgressReporter( vcfIterator, new IteratorProgressReporter_ThreeLevel("lines", 100000, 500000 , 1000000 ) );
    
    val sampleNames = vcfHeader.getGenotypeSamples().asScala;
    val famwriter = new BufferedWriter(new FileWriter(outfileprefix+".fam"));
    for(s <- sampleNames){
      famwriter.write(s+"\t"+s+"\t0\t0\t0\t0\n");
    }
    famwriter.close();
    
    val bedwriter = new FileOutputStream(outfileprefix + ".bed");
    val bimwriter = new BufferedWriter(new FileWriter(outfileprefix+".bim"));
    
    val plinkMagicNumbers : Array[Byte] = Array[Byte](0x6c,0x1b,0x01);
    bedwriter.write(plinkMagicNumbers);
    
    for(vc <- vcfIteratorProg){
      //if rsOnly, take only variants with a single rsID:
      if((! rsOnly) || ((! vc.emptyID())) && (! vc.getID().contains(";"))){ 
        val alle = vc.getAlleles().asScala.toVector;
        //only include biallelic variants:
        if(alle.length == 2){
          //only include SNV variants:
          if(alle.forall(a => a.length() == 1)){
            val A = alle.head.getBaseString();
            val B = alle.last.getBaseString();
            bimwriter.write(vc.getContig()+"\t"+
                            vc.getID()+"\t"+
                            "0\t"+
                            vc.getStart()+"\t"+
                            A + "\n"+
                            B + "\n"+
                            "\n"
                            );
            val genoBits = getGenotypeBits(vc);
            for(b <- genoBits){
              bedwriter.write(b);
            }
          }
        }
      }
    }
    
    bedwriter.close();
    bimwriter.close();
  }
  val b0 = 0;
  val b1 = 1;
  val b2 = 2;
  val b3 = 3;
  
  val offset1 = 0;
  val offset2 = 4;
  val offset3 = 16;
  val offset4 = 64;
  val offsets = Array[Byte](0,4,16,64);
  
  def getGenotypeBits(vc : VariantContext) : Array[Byte] = {
    val genoByte = vc.getGenotypes().asScala.toVector.map(g => {
        val gt = g.getType();
        if(gt.equals(GenotypeType.HOM_REF)) b0
        else if(gt.equals(GenotypeType.HOM_REF))  b1;
        else if(gt.equals(GenotypeType.HOM_VAR))  b3;
        else b2;
    });
    val ex = if(genoByte.length % 4 == 0) 0 else 1;
    val out = Array.ofDim[Byte](ex +  genoByte.length / 4)
    
    genoByte.iterator.zip(getNaturalNumberIterator(0)).foreach{case (x : Int, i : Int)=>{
      out(i / 4) = (out(i / 4) + offsets(i % 4)).toByte;
    }}
    
    return out;
  }
  
}

















