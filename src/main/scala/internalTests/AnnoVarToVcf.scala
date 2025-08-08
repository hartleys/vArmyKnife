package internalTests

import htsjdk.variant._;
import htsjdk.variant.variantcontext._;
import htsjdk.variant.vcf._;
import java.io.File;
//import scala.collection.JavaConversions._
import java.io._;
import internalUtils.commandLineUI._;
import internalUtils.Reporter._;
import scala.collection.JavaConverters._

object VcfUtilTests {
  
  class runVcfUtilTests extends CommandLineRunUtil {
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
                    new UnaryArgument( name = "version",
                                         arg = List("--version"), // name of value
                                         argDesc = "Flag. If raised, print version." // description
                                       ) ::
                    new FinalArgument[String](
                                         name = "infile",
                                         valueName = "infile.vcf",
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
      
       if(out && parser.get[String]("subCommand") == "stdtests"){
         runTests(
             parser.get[String]("infile"),
             parser.get[String]("outfile")
             //parser.get[Boolean]("infileDir")
             );
       } else if(out && parser.get[String]("subCommand") == "extractClinVarLOF") {
         extractClinVarLOF(
             parser.get[String]("infile"),
             parser.get[String]("outfile")
             //parser.get[Boolean]("infileDir")
             );
       } else {
         error("Sub-command not found! Options are: \"" + subCommandList.mkString("\",\"")+"\"");
       }
     }
   }
  
  def extractClinVarLOF(infile : String, outfile : String){
    val vcfReader = new VCFFileReader(new File(infile),false);
    val vcfHeader = vcfReader.getFileHeader();
    val vcfIterator = vcfReader.iterator().asScala;
    
    val vcfb = new htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder();
    
    //vcfb.setReferenceDictionary(vcfHeader.getSequenceDictionary());
    vcfb.unsetOption(htsjdk.variant.variantcontext.writer.Options.INDEX_ON_THE_FLY);
    vcfb.setOutputFileType(htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder.OutputType.VCF);
    //vcfHeader.getSequenceDictionary().getSequences().foreach(x => reportln("   "+x.getSequenceName(),"note"));
    
    vcfb.setOutputFile(outfile);
    val vcfWriter : htsjdk.variant.variantcontext.writer.VariantContextWriter = vcfb.build();
    
    vcfWriter.writeHeader(vcfHeader);
    
    val ipr = internalUtils.stdUtils.IteratorProgressReporter_ThreeLevel("lines", 10000, 50000 , 100000 )
    val wrappedVcfIterator = internalUtils.stdUtils.wrapIteratorWithProgressReporter(vcfIterator , ipr )
    
    val flagLOF = Vector[String]("NSF","NSN");
    
    for(v <- wrappedVcfIterator){
      val isLOF = flagLOF.exists(f => v.hasAttribute(f));
      if(isLOF){
        if(v.hasAttribute("CLNSIG")){
          if(v.getAttribute("CLNSIG").toString() == "5"){
            vcfWriter.add(v);
          }
        }
      }
    }
    vcfWriter.close();
  }
  
  def runTests(infile : String, outfile : String){
    reportln("starting runTests...","debug");

    val vcfReader = new VCFFileReader(new File(infile),false);
    val vcfIterator = vcfReader.iterator().asScala;
    reportln("VCF opened and initialized...","debug");

    val infoKeyList = List(
        "dbNSFP_SIFT_pred",
        "dbNSFP_Polyphen2_HDIV_pred",
        "dbNSFP_Polyphen2_HVAR_pred",
        "dbNSFP_MutationTaster_pred",
        "dbNSFP_MutationAssessor_pred",
        "dbNSFP_LRT_pred",
        "dbNSFP_FATHMM_pred",
        "dbNSFP_MetaLR_pred",
        "dbNSFP_MetaSVM_pred",
        "dbNSFP_PROVEAN_pred"
        );
    val infoKeyTitle = List(
        "SIFT",
        "Polyphen2_HDIV",
        "Polyphen2_HVAR",
        "MutationTaster",
        "MutationAssessor",
        "LRT",
        "FATHMM",
        "MetaLR",
        "MetaSVM",
        "PROVEAN"
        );
    val severityLevels = List(
        List("NoVal","T","D"), //SIFT
        List("NoVal","B","P","D"), //Polyphen2_HDIV
        List("NoVal","B","P","D"), //Polyphen2_HVAR 
        List("NoVal","P","N","D","A"), //MutationTaster
        List("NoVal","N","L","M","H"), //MutationAssessor
        List("NoVal","U","N","D"), //LRT
        List("NoVal","T","D"), //FATHMM
        List("NoVal","T","D"), //MetaLR
        List("NoVal","T","D"), //MetaSVM
        List("NoVal","N","D") //PROVEAN
        )
    val keyLevelZip = infoKeyList.zip(severityLevels);
    
    /*val severityLevels = List(
        List("D","T"), //SIFT
        List("P","B","D"), //Polyphen2_HDIV
        List("P","B","D"), //Polyphen2_HVAR 
        List("D","N","A","P"), //MutationTaster
        List("M","L","N","H"), //MutationAssessor
        List("U","D","N"), //LRT
        List("D","T") //FATHMM
        )*/
    
    //MutationAssess|MutationTaster|Polyphen2|SIFT|FATHMM|dbNSFP_LRT
    //dbNSFP_SIFT_pred
    //dbNSFP_Polyphen2_HDIV_score
    //dbNSFP_Polyphen2_HVAR_pred
    //dbNSFP_MutationTaster_pred
    //dbNSFP_MutationAssessor_pred
    //dbNSFP_LRT_pred
    //dbNSFP_FATHMM_pred
    
    //maybe missing:
    //dbNSFP_PROVEAN_pred
    
    //vcfIterator.filter{(vc : VariantContext) => {
    //  vc.isIndel();
    //}}
    
    val countMap : scala.collection.mutable.Map[Vector[String],Int] = scala.collection.mutable.Map[Vector[String],Int]().withDefault(d => 0);
    
    reportln("Starting VCF iteration...","debug");
    
    val badLineWriter = new BufferedWriter(new FileWriter("badLines.vcf"));
    
    val ipr = internalUtils.stdUtils.IteratorProgressReporter_ThreeLevel("lines", 100000, 500000 , 1000000 )
  
    val wrappedVcfIterator = internalUtils.stdUtils.wrapIteratorWithProgressReporter(vcfIterator , ipr )
    
    for(v <- wrappedVcfIterator){
      if(v.getAlternateAlleles.asScala.length > 1){
        reportln("Alt Alleles: "+v.getAlternateAlleles.asScala.length,"debug");
      }
      
      
      //for(i <- Range(0,v.getAlternateAlleles.length)){
        val valueList = infoKeyList.zip(severityLevels).map{
          case (infoKey: String, lvls : List[String]) => {
            if(v.hasAttribute(infoKey)){
              val valueList = v.getAttributeAsList(infoKey).asScala
              valueList.foldLeft("NoVal")((soFar,v)=> {
                lvls( math.max(lvls.indexOf(soFar),lvls.indexOf(v)) );
              }).toString();
            } else {
              "NoVal";
            }
          }
        }.toVector;
        countMap.put(valueList,countMap(valueList) + 1);
      //}
    }
    reportln("Finished VCF iteration...","debug");
    badLineWriter.close();

    val writer = new BufferedWriter(new FileWriter(outfile));
    writer.write(infoKeyTitle.mkString("\t")+"\t"+"count\n");
    
    for(key <- countMap.keySet.toList){
      val value = countMap(key);
      writer.write(key.mkString("\t")+"\t"+value+"\n");
    }
    writer.close();
  }
  
  
/////////////////////////////////////////////////////////////////////
  
  
  
  
  
}