package internalTests

import internalUtils.Reporter._;
import internalUtils.stdUtils._;
import internalUtils.fileUtils._;
import internalUtils.commandLineUI._;
import internalUtils.commonSeqUtils._;
import internalUtils.optionHolder._;

import scala.util.Random._;

object ibdSimulator {
   
  class ibdSimulator extends CommandLineRunUtil {
     override def priority = 20;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "ibdSimulator", 
          quickSynopsis = "", 
          synopsis = "", 
          description = ""+ALPHA_WARNING,
          argList = 
                    new FinalArgument[Int](
                                         name = "iterCt",
                                         valueName = "ct",
                                         argDesc = "The number of iterations."// description
                                        ) ::
                    new FinalArgument[String](
                                         name = "outfile",
                                         valueName = "outfile.txt",
                                         argDesc = "The output file. Can be gzipped or in plaintext."// description
                                        ) ::
                    internalUtils.commandLineUI.CLUI_UNIVERSAL_ARGS );

     def run(args : Array[String]) {
       val out = parser.parseArguments(args.toList.tail);
       if(out){ 
         ibdSimulator.run(
             iterCt = parser.get[Int]("iterCt"),
             outfile = parser.get[String]("outfile")
         )
       }
     }
  }
  

  
  val rand = new scala.util.Random();
  
  var GENOTYPE_REGISTRY : Set[Int] = Set();
  
  abstract class Person {
    def getGenotypes : Iterator[(Int,Int)];
    def getRelatednessCountMatrix(p2 : Person, iter : Int) = {
      this.getGenotypes.zip(p2.getGenotypes).take(iter).foldLeft((0,0,0)){case ((z0,z1,z2),((g1a,g1b),(g2a,g2b))) => {
        if(g1a == g2a){
          if(g1b == g2b){
            (z0,z1,z2+1);
          } else {
            (z0,z1+1,z2);
          }
        } else if(g1a == g2b){
          if(g1b == g2a){
            (z0,z1,z2+1);
          } else {
            (z0,z1+1,z2);
          }
        } else if(g1b == g2a){
          (z0,z1+1,z2);
        } else if(g1b == g2b){
          (z0,z1+1,z2);
        } else {
          (z0+1,z1,z2);
        }
      }}
    }
    def getRelatednessRateMatrix(p2 : Person, iter : Int) = {
      val (z0,z1,z2) = getRelatednessCountMatrix(p2,iter);
      (z0.toDouble / iter.toDouble,z1.toDouble / iter.toDouble, z2.toDouble/iter.toDouble)
    }
    
    def mateWith(p2 : Person) : Offspring = {
      Offspring(this,p2);
    }
  }
  
  /*
  case class Progenitor(id : String) extends Person {
    def getGenotypes : Iterator[(String,String)] = {
      Iterator.continually( (id+"A",id+"B") )
    }
  }
  case class Offspring(p1 : Person, p2 : Person) extends Person {
    def getGenotypes : Iterator[(String,String)] = {
      p1.getGenotypes.zip(p2.getGenotypes).map{ case ((g1a,g1b),(g2a,g2b)) => {
        (
          if(rand.nextBoolean()){ g1a } else { g1b },
          if(rand.nextBoolean()){ g2a } else { g2b }
        )
      }}
    }
  }*/
  
  case class Progenitor( genoID : (Int,Int) ) extends Person {
    def getGenotypes : Iterator[(Int,Int)] = {
      Iterator.continually( genoID );
    }
  }
  
  //case class InbredProgenitor( genoID : (Int,Int), homozygByDescentPct : Double) extends Person {
  //  def getGenotypes : Iterator[(Int,Int)] = {
  //    Iterator.continually( {} );
  //  }
  //}
  
  case class Offspring( p1 : Person, p2 : Person) extends Person {
    lazy val geno : Stream[(Int,Int)] = p1.getGenotypes.zip(p2.getGenotypes).toStream.map{ case ((g1a,g1b),(g2a,g2b)) => {
        (
           if(rand.nextBoolean()){ g1a } else { g1b },
           if(rand.nextBoolean()){ g2a } else { g2b }
        )
    }}
    
    def getGenotypes : Iterator[(Int,Int)] = geno.iterator;
  }
  
  
  var progRegistry : Set[String] = Set[String]();
  
  def getUnregisteredID() : Int = {
    var newName = rand.nextInt();
    while(GENOTYPE_REGISTRY.contains(newName)){
      newName = rand.nextInt();
    }
    GENOTYPE_REGISTRY = GENOTYPE_REGISTRY + newName;
    newName;
  }
  
  def makeProg() : Progenitor = { 
     val genoIDs = (getUnregisteredID(),getUnregisteredID())
    return Progenitor(genoIDs);
  }
  
  def run(iterCt : Int, outfile : String){
    val p1 = makeProg();
    val p2 = makeProg();
    val p3 = makeProg();
    val p4 = makeProg();
    val p5 = makeProg();
    val p6 = makeProg();
    val p7 = makeProg();
    val p8 = makeProg();
    
    val c12  = p1.mateWith(p2);
    val c12b = p1.mateWith(p2);
    val halfSib = p1.mateWith(p3);
    val c34  = p3.mateWith(p4);
    val c34b = p3.mateWith(p4);
    
    val grandkid = c12.mateWith(c34);
    val halfSibCousin1 = p1.mateWith(c34);
    val halfSibCousin2 = p1.mateWith(c34b);
    val cousin1 = c12.mateWith(p3);
    val cousin2 = c12b.mateWith(p4);
    
    val secondCousin1 = cousin1.mateWith(p5);
    val secondCousin2 = cousin2.mateWith(p6);
    
    val childOfSecondCousin1 = secondCousin1.mateWith(secondCousin2);
    val childOfSecondCousin2 = secondCousin1.mateWith(secondCousin2);
    
    val childOfCousins1 = cousin1.mateWith(cousin2);
    val childOfCousins2 = cousin1.mateWith(cousin2);
    
    val doubleCousin1 = c12.mateWith(c34);
    val doubleCousin2 = c12b.mateWith(c34b);
    val dblCousinSibs1 = doubleCousin1.mateWith(doubleCousin2);
    val dblCousinSibs2 = doubleCousin1.mateWith(doubleCousin2);
    
    val inbredChild = childOfCousins1.mateWith(p5);
    val doubleInbredChild = dblCousinSibs1.mateWith(p5);
    
    reportln("Finished building Person classes. Printing output.","note");
    
    val writer = openWriterSmart(outfile, allowStdout = true);
    def printRel(id : String, p1 : Person, p2 : Person){
      val (z0,z1,z2) = p1.getRelatednessRateMatrix(p2,iterCt);
      val pi = (z1 + (z2*2.toDouble)) / 2.toDouble
      writer.write(id+"\t"+z0+"\t"+z1+"\t"+z2+"\t"+pi+"\n");
      report(".","note");
    }
    
    printRel("Parent-Child", p1,c12)
    printRel("Siblings", c12,c12b)
    printRel("Cousins", cousin1,cousin2)
    printRel("uncle-niece", c12,cousin2)
    printRel("Sibs_offsprOfCousins", childOfCousins1,childOfCousins2)
    printRel("Sibs_offsprDblCousins", dblCousinSibs1,dblCousinSibs2)
    printRel("Sibs_offspr2ndCousins", childOfSecondCousin1,childOfSecondCousin2)

    printRel("DoubleCousins", doubleCousin1,doubleCousin2)
    printRel("HalfSib", halfSib,c12)
    printRel("HalfSib_Cousins", halfSibCousin1,halfSibCousin2)
    printRel("Grandchild", p1,grandkid)
    printRel("SecondCousin",secondCousin1,secondCousin2);
    printRel("Par-ChldOfCousins", childOfCousins1,cousin1)
    printRel("Par-ChldDblCousins", dblCousinSibs1,doubleCousin1)
    
    writer.close();
  }
  
}


























