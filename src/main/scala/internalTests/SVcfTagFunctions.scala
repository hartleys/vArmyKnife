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

object SVcfTagFunctions {

  
  
  
  
    val SNVVARIANT_BASESWAP_LIST = Seq( (("A","C"),("T","G")),
                            (("A","T"),("T","A")),
                            (("A","G"),("T","C")),
                            (("C","A"),("G","T")),
                            (("C","T"),("G","A")),
                            (("C","G"),("G","C"))
                          );

  
    object ParamSrc extends Enumeration {
      type ParamSrc = Value;
      val INFO, GENO, FILE, CONST,ERR = Value;
      def getFromString(s : String) : ParamSrc = {
        if(s == "INFO"){
          INFO
        } else if(s == "GENO" || s == "FORMAT"){
          GENO
        } else if(s == "FILE"){
          FILE
        } else {
          CONST
        }
      }
      def getString(p : ParamSrc.ParamSrc) : String = {
        p match {
          case ParamSrc.INFO => "INFO";
          case ParamSrc.GENO => "GENO";
          case ParamSrc.FILE => "FILE";
          case ParamSrc.CONST => "CONST";
          case ParamSrc.ERR => "ERR";
        }
      }
      def getPrefix(p : ParamSrc.ParamSrc) : String = {
        p match {
          case ParamSrc.INFO => "INFO:";
          case ParamSrc.GENO => "GENO:";
          case ParamSrc.FILE => "FILE:";
          case ParamSrc.CONST => "";
          case ParamSrc.ERR => "ERR:";
        }
      }
    }
    object ParamType extends Enumeration {
      type ParamType = Value;
      val INT,FLOAT,STRING,ERR = Value;
      def getFromString(s : String) : ParamType = {
        if(s == "Integer"){
          INT
        } else if(s == "Float"){
          FLOAT
        } else {
          STRING
        }
      }
      def getString( p : ParamType.ParamType) : String = {
        p match {
          case ParamType.INT => "Integer";
          case ParamType.FLOAT => "Float";
          case ParamType.STRING => "String";
          case ParamType.ERR => "ERR";
        }
      }
    }
    object ParamNum extends Enumeration {
      type ParamNum = Value;
      val dot,A,R,G = Value;
    }
    
    case class VcfFcnParsedParam(SRC : ParamSrc.ParamSrc, 
                                 TYPE : ParamType.ParamType, 
                                 NUM : String, 
                                 PARAM : VcfTagFunctionParam, 
                                 VAL : String){
      
      
      
  }
    
    
  case class VcfTagFunctionParam( id : String, 
                                  ty : String, 
                                  req: Boolean = true, 
                                  defval : String = "", 
                                  dotdot : Boolean = false ,
                                  desc : String = "",
                                  num : String = ".", hidden : Boolean = false){
      
    
    val tys = ty.split("[|]")
    val TYS = tys.map{ tt => tt.toUpperCase() }
    val TYA = TYS.map{ tt => {
        tt.split("[:]").last
    }}.toSet
    val defaultTagType = TYS.map{ tt => {
        tt.split("[:]").head
    }}.filter{ tt => {
        tt == "GENO" || tt == "FORMAT" || tt == "INFO" || tt == "FILE"
    }}.map{ tt => {
        if(tt == "GENO" || tt == "FORMAT"){
          ParamSrc.GENO
        } else if(tt == "FILE"){
          ParamSrc.FILE
        } else {
          ParamSrc.INFO
        }
    }}.headOption.getOrElse( ParamSrc.INFO )
      
    def getParsedParam(param : String, h : SVcfHeader) : VcfFcnParsedParam = {
      reportln("  getParsedParam("+param+") defaultSRC="+ParamSrc.getString(defaultTagType),"debug")
      
      def checkIsInt(pp : String) : Boolean = {
        pp.split(":").forall{ p => {
          string2intOpt(p).nonEmpty;
        }}
      }
      def checkIsFloat(pp : String) : Boolean = {
        pp.split(":").forall{ p => {
          string2doubleOpt(p).nonEmpty;
        }}
        //string2doubleOpt(pp).nonEmpty;
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
      def checkIsInfoOrGeno(pp : String, paramSubstring : String) : Option[SVcfCompoundHeaderLine] = {
        if(paramSubstring == "GENO"){
          checkIsGeno(pp)
        } else {
          checkIsInfo(pp)
        }
      }
      def getHeaderLine(pp : String, PS : ParamSrc.ParamSrc) : Option[SVcfCompoundHeaderLine] = {
        if(PS == ParamSrc.GENO){
          checkIsGeno(pp)
        } else {
          checkIsInfo(pp)
        }
      }
      
      val PP = if(param.toUpperCase().startsWith("FILE:")){
        param.drop(5)
      } else if(param.toUpperCase().startsWith("CONST:")){
        param.drop(6)
      } else if(param.toUpperCase().startsWith("GENO:")){
        param.drop(5)
      } else if( param.toUpperCase().startsWith("FORMAT:")){
        param.drop(7)
      } else if( param.toUpperCase().startsWith("INFO:")){
        param.drop(5)
      } else{
        param;
      }
      val PS = if(param.toUpperCase().startsWith("FILE:")){
        ParamSrc.FILE
      } else if(param.toUpperCase().startsWith("CONST:")){
        ParamSrc.CONST
      } else if(param.toUpperCase().startsWith("GENO:") || param.toUpperCase().startsWith("FORMAT:")){
        ParamSrc.GENO
      } else if(param.toUpperCase().startsWith("INFO:")){
        ParamSrc.INFO
      } else {
        defaultTagType
      }
      reportln("  getParsedParam("+param+") PS="+ParamSrc.getString(PS),"debug")

      
      val PTactual = PS match {
        case ParamSrc.FILE => {
          ParamType.STRING
        }
        case ParamSrc.CONST => {
          if( PP.split(":").forall{ z => string2intOpt(z).isDefined } ){
            ParamType.INT
          } else if(PP.split(":").forall{ z => string2doubleOpt(z).isDefined } ){
            ParamType.FLOAT
          } else {
            ParamType.STRING
          }
        }
        case ParamSrc.GENO => {
          val LN = getHeaderLine(PP,PS);
          LN match {
            case Some(ln) => {
              if(ln.Type == "Float") ParamType.FLOAT
              else if(ln.Type == "Integer") ParamType.INT
              else ParamType.STRING
            }
            case None => {
              error("ERROR: INFO field: "+PP+" NOT FOUND IN VCF HEADER!");
              ParamType.STRING
            }
          }
        }
        case ParamSrc.INFO => {
          val LN = getHeaderLine(PP,PS);
          LN match {
            case Some(ln) => {
              if(ln.Type == "Float") ParamType.FLOAT
              else if(ln.Type == "Integer") ParamType.INT
              else ParamType.STRING
            }
            case None => {
              error("ERROR: INFO field: "+PP+" NOT FOUND IN VCF HEADER!");
              ParamType.STRING
            }
          }
        }
      }
      val SRC = ParamSrc.getString(PS);
      val PT = PTactual match {
        case ParamType.INT => {
          if(TYA.contains("INT")){
            ParamType.INT
          } else if(TYA.contains("FLOAT")){
            ParamType.FLOAT
          } else if(TYA.contains("STRING")){
            ParamType.STRING
          } else {
            error("Impossible State: Illegal param! "+param +" with options: "+ty+".TYA="+TYA.mkString("/"))
            ParamType.ERR
          }
        }
        case ParamType.FLOAT => {
          if(TYA.contains("FLOAT")){
            ParamType.FLOAT
          } else if(TYA.contains("STRING")){
            ParamType.STRING
          } else if(TYA.contains("INT")){
            warning("Warning: Forcing a Float value into a Int: "+param,"FORCE_"+SRC+"_FLOAT_TO_INT",-1)
            ParamType.INT
          } else {
            error("Impossible State: Illegal param! "+param +" with options: "+ty+".TYA="+TYA.mkString("/"))
            ParamType.ERR
          }
        }
        case ParamType.STRING => {
          if(TYA.contains("STRING")){
            ParamType.STRING
          } else if(TYA.contains("FLOAT")){
            warning("Warning: Forcing a String value into a Float: "+param,"FORCE_"+SRC+"_STRING_TO_FLOAT",-1)
            ParamType.FLOAT
          } else if(TYA.contains("INT")){
            warning("Warning: Forcing a String value into a Int: "+param,"FORCE_"+SRC+"_STRING_TO_INT",-1)
            ParamType.INT
          } else {
            error("Impossible State: Illegal param! "+param +" with options: "+ty+".TYA="+TYA.mkString("/"))
            ParamType.ERR
          }
        }
      }
      val PN = PS match {
        case ParamSrc.FILE => ".";
        case ParamSrc.CONST => {
          if( PP.split(":").length == 1){
            "1"
          } else {
            "."
          }
        }
        case ParamSrc.GENO => {
          val LN = getHeaderLine(PP,PS);
          LN match {
            case Some(ln) => {
              ln.Number
            }
            case None => {
              error("ERROR: FORMAT field: "+PP+" NOT FOUND IN VCF HEADER!");
              "."
            }
          }
        }
        case ParamSrc.INFO => {
          val LN = getHeaderLine(PP,PS);
          LN match {
            case Some(ln) => {
              ln.Number
            }
            case None => {
              error("ERROR: INFO field: "+PP+" NOT FOUND IN VCF HEADER!");
              "."
            }
          }
        }
      }
      
      return VcfFcnParsedParam(PS,PT,PN,this, PP)
      
    }

    /*
    def getFinalInputType(param : String, h : SVcfHeader) : (String,String,String) = {
      def checkIsInt(pp : String) : Boolean = {
        pp.split(":").forall{ p => {
          string2intOpt(p).nonEmpty;
        }}
      }
      def checkIsFloat(pp : String) : Boolean = {
        pp.split(":").forall{ p => {
          string2doubleOpt(p).nonEmpty;
        }}
        //string2doubleOpt(pp).nonEmpty;
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
      def checkIsInfoOrGeno(pp : String, paramSubstring : String) : Option[SVcfCompoundHeaderLine] = {
        if(paramSubstring == "GENO"){
          checkIsGeno(pp)
        } else {
          checkIsInfo(pp)
        }
      }
      
      if(param.toUpperCase().startsWith("FILE:")){
        if(! TYS.contains("FILE_STRING")){
          error("Fatal Error: param "+id+" is not permitted to be a FILE! It must be one of these types: "+ty);
        }
        ("FILE","String",".");
      } else if(param.toUpperCase().startsWith("INFO:") || param.toUpperCase().startsWith("GENO:")){
        val SRC = param.toUpperCase().take(4);
        if( ! TYS.exists{ ss => ss.startsWith("INFO")} ){
          error("Fatal Error: param "+id+" is not permitted to be an INFO field! It must be one of these types: "+ty);
        }
        checkIsInfoOrGeno( param.substring(5), SRC).map{ ln => {
          val num = ln.Number;
          if(ln.Type == "String"){
            //if(TYS.contains("FILE_STRING")){
            //  ("FILE","String",num)
            //} else 
            if(TYS.contains(SRC+"_STRING")){
              (SRC,"String",num)
            } else if(TYS.contains(SRC+"_FLOAT")){
              warning("Attempting to coerce String "+SRC+" field "+ param.substring(5)+" into a float.","COERCE_"+SRC+"_STRING_TO_FLOAT",-1)
              (SRC,"Float",num)
            } else if(TYS.contains(SRC+"_INT")){
              (SRC,"Integer",num)
            } else {
              (SRC,"?",num)
            }
          } else if(ln.Type == "Float"){
            if(TYS.contains(SRC+"_FLOAT")){
              (SRC,"Float",num)
            } else if(TYS.contains(SRC+"_STRING")){
              (SRC,"String",num)
            } else if(TYS.contains(SRC+"_INT")){
              warning("Attempting to coerce Float "+SRC+" field "+ param.substring(5)+" into an INT.","COERCE_"+SRC+"_FLOAT_TO_INT",-1)
              (SRC,"Integer",num)
            } else {
              (SRC,"?",num)
            }
          } else if(ln.Type == "Integer"){
            if(TYS.contains(SRC+"_INT")){
              (SRC,"Integer",num)
            } else if(TYS.contains(SRC+"_FLOAT")){
              (SRC,"Float",num)
            } else if(TYS.contains(SRC+"_STRING")){
              (SRC,"String",num)
            } else {
              (SRC,"?",num)
            }
          } else {
            ("?","?",num)
          }
        }}.getOrElse( ("ERROR","String",".") )
        //if( TYS.contains("INFO_FLOAT") && 
      } else if(checkIsInt(param)){
        val num = param.split(":").length.toString;
        ("CONST","Int",num)
      } else if(checkIsFloat(param)){
        val num = param.split(":").length.toString;
        ("CONST","Float",num)
      } else {
        val num = param.split(":").length.toString;
        checkIsInfo(param).foreach{ ln => {
          warning("WARNING: parameter "+param+" is being interpreted as a string constant, but also matches the name of an INFO field! "+
                  "Did you intend for this parameter to be an INFO field? If so, you must start the parameter with 'INFO:'","WARN_STRING_MIGHT_BE_INFO_FIELD",-1)
        }}
        checkIsGeno(param).foreach{ ln => {
          warning("WARNING: parameter "+param+" is being interpreted as a string constant, but also matches the name of an GENOTYPE/FORMAT field! "+
                  "Did you intend for this parameter to be an GENOTYPE/FORMAT field? If so, you must start the parameter with 'INFO:'","WARN_STRING_MIGHT_BE_INFO_FIELD",-1)
        }}
        ("CONST","String",num)
      }
    }*/
    
    /*def checkParamType( param : String, h : SVcfHeader) : Boolean = {
      getFinalInputType(param,h)._2 != "?"
    }*/
  }
  
  
  
  case class TFParamParser[T]( param : String, reader : VcfTagFunctionParamReader[T] ){
    def get(v : SVcfVariantLine) : Option[T] = reader.get(v);
  }

  abstract class VcfTagFunctionParamReader[+T](){
    def get(v : SVcfVariantLine) : Option[T]
    def isConst() : Boolean;
  }
  
/*
 * 
     case class VcfFcnParsedParam(SRC : ParamSrc.ParamSrc, 
                                 TYPE : ParamType.ParamType, 
                                 NUM : String, 
                                 PARAM : VcfTagFunctionParam, 
                                 VAL : String){
      
      
      
  }
 */
  case class VcfTagFunctionParamReaderFile(pprm : VcfFcnParsedParam) extends VcfTagFunctionParamReader[Vector[String]] {
    def isConst() : Boolean = true;
    if(!  (new File(pprm.VAL)).exists() ){
      error("ERROR: File \""+pprm.VAL+"\" DOES NOT EXIST!");
    }
    val lines = getLinesSmartUnzip(pprm.VAL).toVector
    def get(v : SVcfVariantLine) : Option[Vector[String]] = {
      Some(lines);
    }
  }
  
  case class VcfTagFunctionParamReaderInt(pprm : VcfFcnParsedParam) extends VcfTagFunctionParamReader[Int] {
    def isConst() : Boolean = { pprm.SRC == ParamSrc.CONST }
    val x = if(isConst()){
      string2int(pprm.VAL)
    } else {
      0
    }
    def get(v : SVcfVariantLine) : Option[Int] = if(isConst()){
      Some(x);
    } else if(pprm.SRC == ParamSrc.INFO){
      v.info.getOrElse( pprm.VAL, None).filter{ s => s != "." }.map{ z => {
        string2int(z)
      }}
    } else {
      None
    }
  }
  case class VcfTagFunctionParamReaderFloat(pprm : VcfFcnParsedParam) extends VcfTagFunctionParamReader[Double] {
    def isConst() : Boolean = { pprm.SRC == ParamSrc.CONST }
    val x = if(isConst()){
      string2double(pprm.VAL)
    } else {
      0.0
    }
    def get(v : SVcfVariantLine) : Option[Double] = if(isConst()){
      Some(x);
    } else if(pprm.SRC == ParamSrc.INFO){
      v.info.getOrElse( pprm.VAL, None).filter{ s => s != "." }.map{ z => {
        string2double(z)
      }}
    } else {
      None
    }
  }
  case class VcfTagFunctionParamReaderString(pprm : VcfFcnParsedParam) extends VcfTagFunctionParamReader[String] {
    def isConst() : Boolean = { pprm.SRC == ParamSrc.CONST }
    def get(v : SVcfVariantLine) : Option[String] = if(isConst()){
      Some(pprm.VAL)
    } else if(pprm.SRC == ParamSrc.INFO){
      v.info.getOrElse( pprm.VAL, None).filter{ s => s != "." }
    } else {
      None
    }
  }
  case class VcfTagFunctionParamReaderIntSeq(pprm : VcfFcnParsedParam) extends VcfTagFunctionParamReader[Vector[Int]] {
    def isConst() : Boolean = { pprm.SRC == ParamSrc.CONST }
    val x = if(isConst()){
      pprm.VAL.split(":").map{string2int(_)}.toVector
    } else {
      Vector()
    }
    def get(v : SVcfVariantLine) : Option[Vector[Int]] = if(isConst()){
      Some( x );
    } else if(pprm.SRC == ParamSrc.INFO){
      v.info.getOrElse( pprm.VAL, None).filter{z => z != "."}.map{ z => {
        z.split(",").filter{a => a != "."}.map{string2int(_)}.toVector;
      }}
    } else {
      None
    }
  }
  case class VcfTagFunctionGenoParamReaderIntSeq(pprm : VcfFcnParsedParam) extends VcfTagFunctionParamReader[Vector[Option[Int]]] {
    def isConst() : Boolean = { pprm.SRC == ParamSrc.CONST }
    val x = if(isConst()){
      Some( string2int(pprm.VAL) )
    } else {
      None
    }
    def get(v : SVcfVariantLine) : Option[Vector[Option[Int]]] = if(isConst()){
      Some( repToVector(x,v.genotypes.genotypeValues.head.length) );
    } else if(pprm.SRC == ParamSrc.INFO){
      v.info.getOrElse( pprm.VAL, None).filter{z => z != "."}.filter{z => string2intOpt(z).isDefined}.map{ z => {
        //z.split(",").filter{a => a != "."}.map{string2int(_)}.toVector;
        //string2intOpt(z)
        repToVector(string2intOpt(z),v.genotypes.genotypeValues.head.length) ;
      }}
    } else if(pprm.SRC == ParamSrc.GENO){
      val gtIdx = v.genotypes.fmt.indexOf( pprm.VAL );
      if(gtIdx == -1){
        None
      } else {
        Some( v.genotypes.genotypeValues(gtIdx).toVector.map{ g => string2intOpt(g) } )
      }
    } else {
      None
    }
  }
  case class VcfTagFunctionGenoParamReaderFloatSeq(pprm : VcfFcnParsedParam) extends VcfTagFunctionParamReader[Vector[Option[Float]]] {
    def isConst() : Boolean = { pprm.SRC == ParamSrc.CONST }
    val x = if(isConst()){
      Some( string2float(pprm.VAL) )
    } else {
      None
    }
    def get(v : SVcfVariantLine) : Option[Vector[Option[Float]]] = if(isConst()){
      Some( repToVector(x,v.genotypes.genotypeValues.head.length) );
    } else if(pprm.SRC == ParamSrc.INFO){
      v.info.getOrElse( pprm.VAL, None).filter{z => z != "."}.filter{z => string2floatOpt(z).isDefined}.map{ z => {
        //z.split(",").filter{a => a != "."}.map{string2int(_)}.toVector;
        //string2intOpt(z)
        repToVector(string2floatOpt(z),v.genotypes.genotypeValues.head.length) ;
      }}
    } else if(pprm.SRC == ParamSrc.GENO){
      val gtIdx = v.genotypes.fmt.indexOf( pprm.VAL );
      if(gtIdx == -1){
        None
      } else {
        Some( v.genotypes.genotypeValues(gtIdx).toVector.map{ g => string2floatOpt(g) } )
      }
    } else {
      None
    }
  }
  
  case class VcfTagFunctionParamReaderFloatSeq(pprm : VcfFcnParsedParam) extends VcfTagFunctionParamReader[Vector[Double]] {
    def isConst() : Boolean = { pprm.SRC == ParamSrc.CONST }
    val x = if(isConst()){
      pprm.VAL.split(":").map{string2double(_)}.toVector;
    } else {
      Vector()
    }
    def get(v : SVcfVariantLine) : Option[Vector[Double]] = if(isConst()){
      Some(x);
    } else if(pprm.SRC == ParamSrc.INFO){
      v.info.getOrElse( pprm.VAL, None).filter{z => z != "."}.map{ z => {
        z.split(",").filter{a => a != "."}.map{string2double(_)}.toVector;
      }}
    } else {
      None
    }
  }
  case class VcfTagFunctionParamReaderStringSeq(pprm : VcfFcnParsedParam) extends VcfTagFunctionParamReader[Vector[String]] {
    def isConst() : Boolean = { pprm.SRC == ParamSrc.CONST }
    val x = if(isConst()){
      pprm.VAL.split(":").toVector;
    } else {
      Vector()
    }
    def get(v : SVcfVariantLine) : Option[Vector[String]] = if(isConst()){
      //reportln("param:"+param+"/inputType:"+inputType+" RETURNS SOME("+x+")","deepDebug");
      Some(x);
    } else if(pprm.SRC == ParamSrc.INFO){
      //reportln("param:"+param+"/inputType:"+inputType,"deepDebug");
      v.info.getOrElse( pprm.VAL, None).filter{z => z != "."}.map{ z => {
        //reportln(">     "+z,"deepDebug");
        z.split(",").toVector;
      }}
    } else {
      //reportln("param:"+param+"/inputType:"+inputType+" RETURNS NONE","deepDebug");
      None
    }
  }
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  
  
  
  case class VcfTagFunctionFileReader(param : String, inputType : String) extends VcfTagFunctionParamReader[Vector[String]] {
    def isConst() : Boolean = true;
    if(!  (new File(param)).exists() ){
      error("ERROR: File \""+param+"\" DOES NOT EXIST!");
    }
    val lines = getLinesSmartUnzip(param).toVector
    def get(v : SVcfVariantLine) : Option[Vector[String]] = {
      Some(lines);
    }
  }
  
  case class VcfTagFunctionParamReader_Int(param : String, inputType : String) extends VcfTagFunctionParamReader[Int] {
    def isConst() : Boolean = { inputType == "CONST" }
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
  case class VcfTagFunctionParamReader_Float(param : String, inputType : String) extends VcfTagFunctionParamReader[Double] {
    def isConst() : Boolean = { inputType == "CONST" }
    val x : Double = if(inputType == "CONST"){
      string2double(param);
    } else {
      string2double("-1")
    }
    def get(v : SVcfVariantLine) : Option[Double] = if(inputType == "CONST"){
      Some(x);
    } else if(inputType == "INFO"){
      v.info.getOrElse( param, None).map{ z => {
        string2double(z)
      }}
    } else {
      None
    }
  }
  case class VcfTagFunctionParamReader_String(param : String, inputType : String) extends VcfTagFunctionParamReader[String] {
    def isConst() : Boolean = { inputType == "CONST" }
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
  case class VcfTagFunctionParamReader_IntSeq(param : String, inputType : String) extends VcfTagFunctionParamReader[Vector[Int]] {
    def isConst() : Boolean = { inputType == "CONST" }
    val x = if(inputType == "CONST"){
      param.split(":").map{string2int(_)}.toVector;
    } else {
      Vector()
    }
    def get(v : SVcfVariantLine) : Option[Vector[Int]] = if(inputType == "CONST"){
      Some(x);
    } else if(inputType == "INFO"){
      v.info.getOrElse( param, None).map{ z => {
        z.split(",").map{string2int(_)}.toVector;
      }}
    } else {
      None
    }
  }
  case class VcfTagFunctionParamReader_FloatSeq(param : String, inputType : String) extends VcfTagFunctionParamReader[Vector[Double]] {
    def isConst() : Boolean = { inputType == "CONST" }
    val x = if(inputType == "CONST"){
      param.split(":").map{string2double(_)}.toVector;
    } else {
      Vector()
    }
    def get(v : SVcfVariantLine) : Option[Vector[Double]] = if(inputType == "CONST"){
      Some(x);
    } else if(inputType == "INFO"){
      v.info.getOrElse( param, None).map{ z => {
        z.split(",").map{string2double(_)}.toVector;
      }}
    } else {
      None
    }
  }
  case class VcfTagFunctionParamReader_StringSeq(param : String, inputType : String) extends VcfTagFunctionParamReader[Vector[String]] {
    def isConst() : Boolean = { inputType == "CONST" }
    val x = if(inputType == "CONST"){
      param.split(":").toVector;
    } else {
      Vector()
    }
    def get(v : SVcfVariantLine) : Option[Vector[String]] = if(inputType == "CONST"){
      //reportln("param:"+param+"/inputType:"+inputType+" RETURNS SOME("+x+")","deepDebug");
      Some(x);
    } else if(inputType == "INFO"){
      //reportln("param:"+param+"/inputType:"+inputType,"deepDebug");
      v.info.getOrElse( param, None).map{ z => {
        //reportln(">     "+z,"deepDebug");
        z.split(",").toVector;
      }}
    } else {
      //reportln("param:"+param+"/inputType:"+inputType+" RETURNS NONE","deepDebug");
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
    def tag : String;
    val outType : String = "String";
    val outNum : String = ".";
    
    def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine);
    
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
      /*def checkVcfTagParams() : Boolean = {
          md.params.padTo( pv.length, md.params.last ).zip(pv).exists{ case (vtfp, pvv) => {
              ! vtfp.checkParamType(pvv, h);
          }}
      }*/
      
      def writeDouble(vc : SVcfOutputVariantLine,dd : Double){
              dgts match {
                case Some(dd) => {
                  vc.addInfo(tag, ("%."+dd+"f").format( dd ) );
                }
                case None => {
                  vc.addInfo(tag, ""+(dd));
                }
              }
      }
      def writeInt(vc : SVcfOutputVariantLine,dd : Int){
                vc.addInfo(tag, ""+(dd));
      }
      def writeString(vc : SVcfOutputVariantLine,dd : String){
                vc.addInfo(tag, ""+(dd));
      }
      
      def writeDouble(vc : SVcfOutputVariantLine, tt : String,dd : Double){
              dgts match {
                case Some(dd) => {
                  vc.addInfo(tt, ("%."+dd+"f").format( dd ) );
                }
                case None => {
                  vc.addInfo(tt, ""+(dd));
                }
              }
      }
      def writeInt(vc : SVcfOutputVariantLine, tt : String,dd : Int){
                vc.addInfo(tt, ""+(dd));
      }
      def writeString(vc : SVcfOutputVariantLine, tt : String,dd : String){
                vc.addInfo(tt, ""+(dd));
      }
      def writeNum(vc : SVcfOutputVariantLine,dd : Int){
        writeInt(vc,dd)
      }
      def writeNum(vc  : SVcfOutputVariantLine,dd : Double){
        writeDouble(vc,dd)
      }
      
  }
  abstract class VcfTagFcnSideEffecting() extends VcfTagFcn() {
    def close()
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
  
  //object VcfFcnTypeInfo {

  //}
  
  //("FILE","String",num)

  
  abstract class VcfTagFcnFactory(){
    /*def getTypeInfoOLD(fcParam : Seq[VcfTagFunctionParam], params : Seq[String], h : SVcfHeader) : Vector[(String,String,VcfTagFunctionParam,String)] = {
     fcParam.padTo(params.length, fcParam.last).zip(params).map{ case (pp,pv) => {
            val tpair = pp.getFinalInputType(pv,h);
            val pvv = if(tpair._1 == "INFO" || tpair._1 == "GENO"){
              pv.drop(5)
            } else {
              pv
            }
            (tpair._1,tpair._2,pp,pvv)
      }}.toVector
    }*/
    //def getTypeInfo(
    /*def getSuperTypeOLD(typeInfo : Vector[(String,String,VcfTagFunctionParam,String)]) : String = {
      val types = typeInfo.map{_._2}
      if( types.forall( tt => tt == "Integer" ) ) {
        "Integer"
      } else if( types.contains( "String" ) ) {
        "String"
      } else {
        "Float"
      }
    }*/
    
    def getTypeInfo(fcParam : Seq[VcfTagFunctionParam], params : Seq[String], h : SVcfHeader) : Seq[VcfFcnParsedParam] = {
      if(fcParam.length < params.length && (! fcParam.last.dotdot)){
        error("ERROR: Function has too many parameters: "+params);
      } else if( fcParam.filter{ fp => fp.req }.length > params.length ){
        error("ERROR: Function has too few parameters: "+params);
      }
      fcParam.padTo(params.length, fcParam.last).zip(params).map{ case (fpp,param) => {
        fpp.getParsedParam(param ,h)
      }}
    }
    //def getTypeInfo(
    def getSuperType(pprm : Seq[VcfFcnParsedParam]) : String = {
      ParamType.getString( if( pprm.forall( p => p.TYPE == ParamType.INT ) ){
        ParamType.INT
      } else if( pprm.forall( p => p.TYPE == ParamType.INT || p.TYPE == ParamType.FLOAT ) ){
        ParamType.FLOAT
      } else {
        ParamType.STRING
      } )
    }
    
     def metadata : VcfTagFcnMetadata;
     def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn;
  }

  
  abstract class VcfTagFcnFactorySideEffecting(){
    def getTypeInfo(fcParam : Seq[VcfTagFunctionParam], params : Seq[String], h : SVcfHeader) : Seq[VcfFcnParsedParam] = {
      if(fcParam.length < params.length && (! fcParam.last.dotdot)){
        error("ERROR: Function has too many parameters: "+params);
      } else if( fcParam.filter{ fp => fp.req }.length > params.length ){
        error("ERROR: Function has too few parameters: "+params);
      }
      fcParam.padTo(params.length, fcParam.last).zip(params).map{ case (fpp,param) => {
        fpp.getParsedParam(param ,h)
      }}
    }
    //def getTypeInfo(
    def getSuperType(pprm : Seq[VcfFcnParsedParam]) : String = {
      ParamType.getString( if( pprm.forall( p => p.TYPE == ParamType.INT ) ){
        ParamType.INT
      } else if( pprm.forall( p => p.TYPE == ParamType.INT || p.TYPE == ParamType.FLOAT ) ){
        ParamType.FLOAT
      } else {
        ParamType.STRING
      } )
    }
     def metadata : VcfTagFcnMetadata;
     def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcnSideEffecting;
  }

  /*
                 val expr = paramValues.head;
              val parser : SFilterLogicParser[SVcfVariantLine] = internalUtils.VcfTool.sVcfFilterLogicParser;
              val filter : SFilterLogic[SVcfVariantLine] = parser.parseString(expr);
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                val k = if( filter.keep(vc) ){
                  dd(0).get(vc).getOrElse(".")
                } else {
                  dd(1).get(vc).getOrElse(".")
                }
   * 
   */
  
  val vcfTagFcnMap_sideEffecting : Map[String,VcfTagFcnFactorySideEffecting] = Seq[VcfTagFcnFactorySideEffecting](
        new VcfTagFcnFactorySideEffecting(){
          val mmd =  new VcfTagFcnMetadata(
              id = "TALLY.SUM.IF",synon = Seq(),
              shortDesc = "Calculates a conditional sum across the whole file.",
              desc = "Takes as input a variant expression expr and a constant or INFO field x. "+
                     "Output will be the sum of all x where expr is TRUE. Set x to CONST:1 to simply count variants."+
                     ""+
                     "",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "expr", ty = "String",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "x", ty = "INFO:Int|INFO:Float|CONST:Int|CONST:Float",req=true,dotdot=true )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcnSideEffecting = {
            new VcfTagFcnSideEffecting(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params.tail,pv.tail,h)
              override val outType = getSuperType(typeInfo);
              override val outNum = "1";
              val dd = if(outType == "Integer"){
                tally(newTag,0);
                Left(typeInfo.map{ pprm => VcfTagFunctionParamReaderIntSeq(pprm)})
              } else {
                tally(newTag,0.0);
                Right(typeInfo.map{ pprm => VcfTagFunctionParamReaderFloatSeq(pprm)})
              }
              val expr = paramValues.head;
              val parser : SFilterLogicParser[SVcfVariantLine] = internalUtils.VcfTool.sVcfFilterLogicParser;
              val filter : SFilterLogic[SVcfVariantLine] = parser.parseString(expr);
              
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                dd match {
                  case Left(ddi) => {
                    val v = ddi.flatMap{ d => d.get(vc).getOrElse(Vector()) }
                    if(v.length > 0 && filter.keep(vc)){
                      tally(newTag,v.sum)
                    }
                  }
                  case Right(ddf) => {
                    val v = ddf.flatMap{ d => d.get(vc).getOrElse(Vector()) }
                    if(v.length > 0 && filter.keep(vc)){
                      tally(newTag,v.sum)
                    }
                  }
                }
              }
              def close(){
                
              }
            }

          }
        },
        new VcfTagFcnFactorySideEffecting(){
          val mmd =  new VcfTagFcnMetadata(
              id = "TALLY.SUM.IF.byGROUP",synon = Seq("TALLY.SUM.IF.GROUP"),
              shortDesc = "Sum of several tags or numeric constants.",
              desc = "Takes a variant expression expr, an INFO field \"group\" and an INFO field x. "+
                     "Will output an entry for each unique value of the group variable. "+
                     "For each distinct value of the group variable g, will output the sum of all "+
                     "x in which the group variable equals g AND where expr is TRUE. "+
                     "This is especially useful for generating counts for each gene.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "expr", ty = "String",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "group", ty = "INFO:String",req=true,dotdot=true ),
                  VcfTagFunctionParam( id = "x", ty = "INFO:Int|INFO:Float|CONST:Int|CONST:Float",req=true,dotdot=true )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcnSideEffecting = {
            new VcfTagFcnSideEffecting(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params.drop(2),pv.drop(2),h)
              override val outType = getSuperType(typeInfo);
              override val outNum = "1";
              val dd = if(outType == "Integer"){
                Left(typeInfo.map{ pprm => VcfTagFunctionParamReaderIntSeq(pprm)})
              } else {
                Right(typeInfo.map{ pprm => VcfTagFunctionParamReaderFloatSeq(pprm)})
              }
              val expr = paramValues.head;
              val parser : SFilterLogicParser[SVcfVariantLine] = internalUtils.VcfTool.sVcfFilterLogicParser;
              val filter : SFilterLogic[SVcfVariantLine] = parser.parseString(expr);
              val grp = if( pv(1).startsWith("INFO:")) pv(1).drop(5) else pv(1);
              
              var rct = 0;
              var tallyfuncInt : ( (String,Int) => Unit )    = internalUtils.Reporter.tallyWithDebugReport
              var tallyfuncDub : ( (String,Double) => Unit ) = internalUtils.Reporter.tallyWithDebugReport

              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                if( filter.keep(vc) ){
                  if(rct == 20){
                    tallyfuncInt = internalUtils.Reporter.tally;
                    tallyfuncDub = internalUtils.Reporter.tally;
                  }
                  if(rct < 20){
                    reportln("Starting: "+dd+":","debug")
                  }
                  rct = rct + 1;
                    dd match {
                      case Left(ddi) => {
                        val v = ddi.flatMap{ d => d.get(vc).getOrElse(Vector()) }.sum
                        val g = vc.info.getOrElse( grp,None).filter( z => z != "." ).toSeq.flatMap{ z => z.split(",").toSeq }
                        g.foreach{ gg => {
                          tallyfuncInt(newTag+"\t"+gg,v)
                        }}
                      }
                      case Right(ddf) => {
                        val v = ddf.flatMap{ d => d.get(vc).getOrElse(Vector()) }.sum
                        val g = vc.info.getOrElse( grp,None).filter( z => z != "." ).toSeq.flatMap{ z => z.split(",").toSeq }
                        g.foreach{ gg => {
                          tallyfuncDub(newTag+"\t"+gg,v)
                        }}
                      }
                    }
                }
              }
              def close(){
                
              }
            }

          }
        }
    ).map{ ff => {
        (ff.metadata.id,ff)
      }}.toMap
  
      
  val vcfFormatFunMap : Map[String,VcfTagFcnFactory] = Seq[VcfTagFcnFactory](
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "SUM",synon = Seq(),
              shortDesc = "Sum of several tags or numeric constants.",
              desc = "Input should be a set of format tags and/or numeric constants (which must be specified as CONST:n) or info tags (which must be specified as INFO:n). "+
                     "Output field will be the sum of the inputs. Missing fields will be treated as zeros "+
                     "unless all params are INFO/FORMAT fields and all are missing, in which case the output will be missing. "+
                     "Output field type will be an integer if all inputs are integers and otherwise a float.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "GENO:Int|GENO:Float|INFO:Int|INFO:Float|CONST:Int|CONST:Float",req=true,dotdot=true )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params,pv,h)
              override val outType = getSuperType(typeInfo);
              override val outNum = "1";
              val dd = if(outType == "Integer"){
                Left(typeInfo.map{  pprm => VcfTagFunctionGenoParamReaderIntSeq(pprm)})
              } else {
                Right(typeInfo.map{ pprm => VcfTagFunctionGenoParamReaderFloatSeq(pprm)})
              }
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                dd match {
                  case Left(ddi) => {
                    val v = ddi.map{ d => d.get(vc) }.filter{ d => d.isDefined }.map{ d => d.get }
                    if(v.length > 0){
                      val arr = Array.ofDim[Int](vc.genotypes.genotypeValues.head.length);
                      v.foreach{ d => {
                        d.zipWithIndex.foreach{ case (dd,i) => {
                          arr(i) = arr(i) + dd.getOrElse(0);
                        }}
                      }}
                      vout.genotypes.addGenotypeArray( newTag, arr.map{_.toString} );
                    //  writeNum(vout,v.sum)
                    }
                  }
                  case Right(ddf) => {
                    val v = ddf.map{ d => d.get(vc) }.filter{ d => d.isDefined }.map{ d => d.get }
                    if(v.length > 0){
                      val arr = Array.ofDim[Double](vc.genotypes.genotypeValues.head.length);
                      v.foreach{ d => { 
                        d.zipWithIndex.foreach{ case (dd,i) => {
                          arr(i) = arr(i) + dd.getOrElse(0.toFloat);
                        }}
                      }}
                      vout.genotypes.addGenotypeArray( newTag, arr.map{_.toString} );
                    //  writeNum(vout,v.sum)
                    }
                  }
                }
                /*
                val gtIdx = vc.genotypes.fmt.indexOf(gtTag)
                if( gtIdx > -1 ){
                  vout.genotypes.addGenotypeArray( newTag, vout.genotypes.genotypeValues(gtIdx).map{ gg => {
                    gg.split(delim).lift(extractIDX).getOrElse(".");
                  }})
                }
                 */
              }
            }

          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "DIV",synon = Seq(),
              shortDesc = "result of dividing x and y.",
              desc = "Input should be a set of format tags and/or numeric constants (which must be specified as CONST:n) or info tags (which must be specified as INFO:n). "+
                     "Output field will be the sum of the inputs. Any missing values result in a missing result.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "GENO:Int|GENO:Float|INFO:Int|INFO:Float|CONST:Int|CONST:Float",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "y", ty = "GENO:Int|GENO:Float|INFO:Int|INFO:Float|CONST:Int|CONST:Float",req=true,dotdot=false )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params,pv,h)
              override val outType = "Float";
              override val outNum = "1";
              val ddx = VcfTagFunctionGenoParamReaderFloatSeq(typeInfo.head);
              val ddy = VcfTagFunctionGenoParamReaderFloatSeq(typeInfo(1));
              
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                    //val v = ddf.map{ d => d.get(vc) }.filter{ d => d.isDefined }.map{ d => d.get }
                val vx = ddx.get(vc)
                val vy = ddy.get(vc);
                
                if( vx.isDefined && vy.isDefined ){
                  val arr = vx.get.zip(vy.get).map{ case (x,y) => {
                    if( x.isDefined && y.isDefined ){
                      (x.get / y.get).toString;
                    } else {
                      "."
                    }
                  }}.toArray
                  vout.genotypes.addGenotypeArray( newTag, arr );
                }
              }
            }

          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "extractIDX",synon = Seq(),
              shortDesc = "",
              desc = " "+
                     " "+
                     " "+
                     "",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "GENO:Int|GENO:Float|GENO:String",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "i", ty = "CONST:Int",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "delim", ty = "CONST:String",req=false,dotdot=false )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true; 
              val gtTag = paramValues(0);
              val extractIDX = string2int( paramValues(1) )
              val delim = paramValues.lift(2).getOrElse(",");
              override val outType = outHeader.formatLines.find( ff => ff.ID == gtTag ).get.Type
              override val outNum = "1"; 
              
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                val gtIdx = vc.genotypes.fmt.indexOf(gtTag)
                if( gtIdx > -1 ){
                  vout.genotypes.addGenotypeArray( newTag, vout.genotypes.genotypeValues(gtIdx).map{ gg => {
                    gg.split(delim).lift(extractIDX).getOrElse(".");
                  }})
                }
              }
            }

          }
        },
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "EXPR",synon = Seq(),
              shortDesc = "",
              desc = " "+
                     " "+
                     " "+
                     "",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "gtExpr", ty = "CONST:String",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "varExpr", ty = "CONST:String",req=false,dotdot=false )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true; 
              override val outType = "Integer";
              override val outNum = "1"; 
              val gtexpr = paramValues.head;
              val gtparser : SFilterLogicParser[(SVcfVariantLine,Int)] = internalUtils.VcfTool.sGenotypeFilterLogicParser;
              val varparser : SFilterLogicParser[SVcfVariantLine] = internalUtils.VcfTool.sVcfFilterLogicParser;
              
              val gtfilter : SFilterLogic[(SVcfVariantLine,Int)] = gtparser.parseString(gtexpr);
              val varfilter : SFilterLogic[SVcfVariantLine] = paramValues.tail.headOption match {
                case Some(ee) => {
                  varparser.parseString(ee);
                }
                case None => {
                  varparser.parseString("TRUE");
                }
              }
              val zeroArray = Array.fill[String](outHeader.titleLine.sampleList.length)("0");
              
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                val k = if( varfilter.keep(vc) ){
                  Range(0,outHeader.sampleCt).map{ii => {
                    if(  gtfilter.keep((vc,ii)) ){
                      "1"
                    } else {
                      "0"
                    }
                  }}.toArray
                } else {
                  zeroArray
                }
                vout.genotypes.addGenotypeArray( newTag, k);
                
                //notice(newTag+"="+k+" tagged for variant:\n    "+vc.getSimpleVcfString(),"TAGGED_"+newTag,5);
                //writeString(vout,k);
              }
            }

          }
        }
        ).map{ ff => {
        (ff.metadata.id,ff)
      }}.toMap
      
  val vcfTagFunMap : Map[String,VcfTagFcnFactory] = Seq[VcfTagFcnFactory](
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "SUM",synon = Seq(),
              shortDesc = "Sum of several tags or numeric constants.",
              desc = "Input should be a set of info tags and/or numeric constants (which must be specified as CONST:n). "+
                     "Output field will be the sum of the inputs. Missing INFO fields will be treated as zeros "+
                     "unless all params are INFO fields and all are missing, in which case the output will be missing. "+
                     "Output field type will be an integer if all inputs are integers and otherwise a float.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INFO:Int|INFO:Float|CONST:Int|CONST:Float",req=true,dotdot=true )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params,pv,h)
              override val outType = getSuperType(typeInfo);
              override val outNum = "1";
              val dd = if(outType == "Integer"){
                Left(typeInfo.map{ pprm => VcfTagFunctionParamReaderIntSeq(pprm)})
              } else {
                Right(typeInfo.map{ pprm => VcfTagFunctionParamReaderFloatSeq(pprm)})
              }
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                dd match {
                  case Left(ddi) => {
                    val v = ddi.flatMap{ d => d.get(vc).getOrElse(Vector()) }
                    if(v.length > 0){
                      writeNum(vout,v.sum)
                    }
                  }
                  case Right(ddf) => {
                    val v = ddf.flatMap{ d => d.get(vc).getOrElse(Vector()) }
                    if(v.length > 0){
                      writeNum(vout,v.sum)
                    }
                  }
                }
              }
            }

          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "LOG10",synon = Seq(),
              shortDesc = "The log10 of a field",
              desc = "Input should be a numeric INFO field. output will be the log10 of that field.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INFO:Int|INFO:Float|CONST:Int|CONST:Float",req=true,dotdot=false )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params,pv,h)
              override val outType = "Float";
              override val outNum = "1";
              val ddf : Vector[VcfTagFunctionParamReader[Double]]  = typeInfo.toVector.map{ pprm => VcfTagFunctionParamReaderFloat(pprm)}
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                  val v = ddf.flatMap{ d => d.get(vc) }
                  if(v.length == 1){
                    
                    writeNum(vout, math.log10(v(0)))
                  }
              }
            }
          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "LN",synon = Seq(),
              shortDesc = "The natural log of a field",
              desc = "Input should be a numeric INFO field. output will be the natural log of that field.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INFO:Int|INFO:Float|CONST:Int|CONST:Float",req=true,dotdot=false )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params,pv,h)
              override val outType = "Float";
              override val outNum = "1";
              val ddf : Vector[VcfTagFunctionParamReader[Double]]  = typeInfo.toVector.map{ pprm => VcfTagFunctionParamReaderFloat(pprm)}
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                  val v = ddf.flatMap{ d => d.get(vc) }
                  if(v.length == 1){
                    
                    writeNum(vout, math.log(v(0)))
                  }
              }
            }
          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "PRODUCT.ARRAY",synon = Seq(),
              shortDesc = "Multiplicative product",
              desc = "Input should be a set of info fields and/or numeric constants (which must be specified as CONST:n). "+
                     "Output field will be the product of the inputs. Missing INFO fields will be treated as ones "+
                     "unless all params are INFO fields and all are missing, in which case the output will be missing. "+
                     "Output field type will be an integer if all inputs are integers and otherwise a float.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INFO:Int|INFO:Float|CONST:Int|CONST:Float",req=true,dotdot=true )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params,pv,h)
              override val outType = getSuperType(typeInfo);
              override val outNum = "1";
              val dd = if(outType == "Integer"){
                Left(typeInfo.map{ pprm => VcfTagFunctionParamReaderIntSeq(pprm)})
              } else {
                Right(typeInfo.map{ pprm => VcfTagFunctionParamReaderFloatSeq(pprm)})
              }
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                dd match {
                  case Left(ddi) => {
                    val v = ddi.flatMap{ d => d.get(vc).getOrElse(Vector()) }
                    if(v.length > 0){
                      writeNum(vout,v.product)
                    }
                  }
                  case Right(ddf) => {
                    val v = ddf.flatMap{ d => d.get(vc).getOrElse(Vector()) }
                    if(v.length > 0){
                      writeNum(vout,v.product)
                    }
                  }
                }
              }
            }

          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "MULT",synon = Seq(),
              shortDesc = "",
              desc = "Input should be a pair of info fields and/or numeric constants (which must be specified as CONST:n). "+
                     "Output field will be the product of the two inputs. Missing INFO fields will be treated as ZEROS "+
                     "unless all params are INFO fields and all are missing, in which case the output will be missing. "+
                     "Output field type will be an integer if all inputs are integers and otherwise a float.",
                     params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INFO:Int|INFO:Float|CONST:Int|CONST:Float",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "y", ty = "INFO:Int|INFO:Float|CONST:Int|CONST:Float",req=true,dotdot=false )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params,pv,h)
              override val outType = getSuperType(typeInfo);
              override val outNum = "1";
              val dd = if(outType == "Integer"){
                Left(typeInfo.map{ pprm => VcfTagFunctionParamReaderInt(pprm)})
              } else {
                Right(typeInfo.map{ pprm => VcfTagFunctionParamReaderFloat(pprm)})
              }
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                dd match {
                  case Left(ddi) => {
                    val v = ddi.flatMap{ d => d.get(vc) }
                    if(v.length == 2){
                      writeNum(vout,v.product)
                    } else if(v.length == 1){
                      writeNum(vout,0)
                    }
                  }
                  case Right(ddf) => {
                    val v = ddf.flatMap{ d => d.get(vc) }
                    if(v.length == 2){
                      writeNum(vout,v.product)
                    } else if(v.length == 1){
                      writeNum(vout,0.0)
                    }
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
              desc = "Input should be a pair of info fields and/or numeric constants (which must be specified as CONST:n). "+
                     "Output field will be the product of the two inputs. Missing INFO fields will be treated as ZEROS "+
                     "unless all params are INFO fields and all are missing, in which case the output will be missing. "+
                     "Output field type will be a float.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INFO:Int|INFO:Float|CONST:Int|CONST:Float",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "y", ty = "INFO:Int|INFO:Float|CONST:Int|CONST:Float",req=true,dotdot=false )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params,pv,h)
              override val outType = "Float";
              override val outNum = "1";
              val ddf : Vector[VcfTagFunctionParamReader[Double]]  = typeInfo.toVector.map{ pprm => VcfTagFunctionParamReaderFloat(pprm)}
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                  val v = ddf.flatMap{ d => d.get(vc) }
                  if(v.length == 2){
                    writeNum(vout,v(0) / v(1))
                  }
              }
            }
          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "DIFF",synon = Seq(),
              shortDesc = "",
              desc = "Input should be a pair of info fields and/or numeric constants (which must be specified as CONST:n). "+
                     "Output field will be the difference of the two inputs (ie x - y). Missing INFO fields will be treated as ZEROS "+
                     "unless all params are INFO fields and all are missing, in which case the output will be missing. "+
                     "Output field type will be an integer if all inputs are integers and otherwise a float.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INFO:Int|INFO:Float|CONST:Int|CONST:Float",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "y", ty = "INFO:Int|INFO:Float|CONST:Int|CONST:Float",req=true,dotdot=false )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params,pv,h)
              override val outType = getSuperType(typeInfo);
              override val outNum = "1";
              val dd = if(outType == "Integer"){
                Left(typeInfo.map{ pprm => VcfTagFunctionParamReaderInt(pprm)})
              } else {
                Right(typeInfo.map{ pprm => VcfTagFunctionParamReaderFloat(pprm)})
              }
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                dd match {
                  case Left(ddi) => {
                    val v = ddi.map{ d => d.get(vc) }
                    if(v.forall(vv => vv.isDefined)){
                      writeNum(vout,v(0).get - v(1).get)
                    } else if(v(0).isDefined){
                      writeNum(vout,v(0).get)
                    } else if(v(1).isDefined){
                      writeNum(vout,-v(1).get)
                    }
                  }
                  case Right(ddf) => {
                    val v = ddf.map{ d => d.get(vc) }
                    if(v.forall(vv => vv.isDefined)){
                      writeNum(vout,v(0).get - v(1).get)
                    } else if(v(0).isDefined){
                      writeNum(vout,v(0).get)
                    } else if(v(1).isDefined){
                      writeNum(vout,-v(1).get)
                    }
                  }
                }
              }
            }
          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        

        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "CONST",synon = Seq(),
              shortDesc = "Creates a new tag variable that is always equal to a given string",
              desc = "Input should be a simple string of characters",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "CONST:String",req=true,dotdot=false )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val x = paramValues.head;
              override val outType = "String";
              override val outNum = "1";
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                   
                    writeString(vout, x)
                  
              }
            }
          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "LN",synon = Seq(),
              shortDesc = "The natural log of a field",
              desc = "Input should be a numeric INFO field. output will be the natural log of that field.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INFO:Int|INFO:Float|CONST:Int|CONST:Float",req=true,dotdot=false )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params,pv,h)
              override val outType = "Float";
              override val outNum = "1";
              val ddf : Vector[VcfTagFunctionParamReader[Double]]  = typeInfo.toVector.map{ pprm => VcfTagFunctionParamReaderFloat(pprm)}
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                  val v = ddf.flatMap{ d => d.get(vc) }
                  if(v.length == 1){
                    
                    writeNum(vout, math.log(v(0)))
                  }
              }
            }
          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "CONVERT.TO.INT",synon = Seq(),
              shortDesc = "",
              desc = "Input should be an INFO field. Converts field to a Integer.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INFO:String",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "defaultValue", ty = "CONST:Int",req=false,dotdot=false )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              override val outType = "Integer"
              override val outNum = "1";
              val x = if( paramValues.head.startsWith("INFO:") ){
                paramValues.head.drop(5)
              } else {
                paramValues.head
              }
              val dv = paramValues.lift(1).map{ z => string2int(z) }
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                vc.info.getOrElse(x,None).filter{xx => xx != "."}.foreach{ xx => {
                  (string2intOpt(xx) match {
                    case Some(zz) => Some(zz);
                    case None => dv
                  }).foreach{ zz => {
                    writeInt(vout,zz);
                  }}
                }}
              }
            }
          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //xx.split(",").flatMap{zz => string2intOpt(zz)}
                  
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "CONVERT.TO.FLOAT",synon = Seq(),
              shortDesc = "",
              desc = "Input should be an INFO field. Converts to a numeric float. If no defaultValue is supplied then non-floats will be dropped. Note that NaN and Inf will be dropped / replaced with the default.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INFO:String",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "defaultValue", ty = "CONST:Float",req=false,dotdot=false )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              override val outType = "Float"
              override val outNum = "1";
              val x = if( paramValues.head.startsWith("INFO:") ){
                paramValues.head.drop(5)
              } else {
                paramValues.head
              }
              val dv = paramValues.lift(1).map{ z => string2double(z) }
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                vc.info.getOrElse(x,None).foreach{ xx => {
                  (string2doubleOpt(xx).filter{ zz => ! ( zz.isNaN() || zz.isInfinite() ) } match {
                    case Some(zz) => Some(zz);
                    case None => dv
                  }).foreach{ zz => {
                    writeDouble(vout,zz);
                  }}
                }}
              }
            }
          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "FLAGSET",synon = Seq(),
              shortDesc = "",
              desc = "Input should be a set of infoFields and optionally a name, with the format tagID:name or just tagID. "+
                     "If names are omitted, then the name will be equal to the tagID. "+
                     "Output field will be the set of names for which the respective info field is equal to 1. Any value other than 1, including missing fields, will be treated as 0.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INFO:Int",req=true,dotdot=true )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val ppv = pv.map{ vv => {
                val ppva = vv.split(":")
                "INFO:"+ppva.head
              }}
              val ppn = pv.map{ vv => {
                val ppva = vv.split(":")
                ppva.last
              }}
              val typeInfo = getTypeInfo(md.params,ppv,h)
              override val outType = "String"
              override val outNum = "."
              val ddi : Vector[VcfTagFunctionParamReader[Int]] = typeInfo.toVector.map{ pprm => VcfTagFunctionParamReaderInt(pprm)}
              //val ddf : Vector[VcfTagFunctionParamReader[Double]]  = typeInfo.map{ case (tt,tp,pp,pv) => VcfTagFunctionParamReader_Float(pv,tt)}
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                val v = ddi.map{ d => d.get(vc) }.zip(ppn).filter{ case (d,n) => d.map{ _ == 1 }.getOrElse(false) }.map{ _._2 }.padTo(1,".").mkString(",")
                writeString(vout,v);
              }
            }
          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "DECODE",synon = Seq(),
              shortDesc = "",
              desc = "",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INFO:String",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "decoder", ty = "FILE:String",req=true,dotdot=false ),
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params,pv,h)
              
              val decoder = getLinesSmartUnzip(pv(1)).map{ s => s.split("\t") }.map{ ss => (ss(0),ss(1)) }.toVector.toMap;
              
              //val dds : Vector[VcfTagFunctionParamReader[Vector[String]]] = typeInfo.map{ case (tt,tp,pp,pv) => VcfTagFunctionParamReader_StringSeq(pv,tt)}
              
              val ddTag : VcfTagFunctionParamReader[Vector[String]] = VcfTagFunctionParamReaderStringSeq(typeInfo.head);
              //val ffTag : VcfTagFunctionParamReader[Vector[String]] = VcfTagFunctionFileReader(typeInfo.head._4,typeInfo.head._1);
              
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                
                /*val out = dds.foldLeft(Set[String]() ){ case (soFar,curr) => {
                  soFar ++ curr.get(vc).getOrElse(Vector()).toSet
                }}.toVector.sorted.mkString(",")*/
                //reportln("SETS.UNION.run:"+out,"deepDebug");
                //error("NOT YET IMPLEMENTED!");
                val out = vc.info.getOrElse(pv(0).drop(5),None).map{ vv => {
                  vv.split(",").map{ vvv => { decoder.getOrElse(vvv,vvv) }}.padTo(1,".").mkString(",")
                }}.getOrElse(".");
                writeString(vout,out)
              }
            }
          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "STRING_REPLACE",synon = Seq(),
              shortDesc = "",
              desc = "",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "old", ty = "CONST:String",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "new", ty = "CONST:String",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "info", ty = "INFO:String",req=true,dotdot=false ),
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              //val typeInfo = getTypeInfo(md.params,pv,h)
              
              val ostr : String = pv(0);
              val nstr : String = pv(1);
              val info : String = pv(2);
              
              //val ddTag : VcfTagFunctionParamReader[Vector[String]] = VcfTagFunctionParamReaderStringSeq(typeInfo(2));
              //val ffTag : VcfTagFunctionParamReader[Vector[String]] = VcfTagFunctionFileReader(typeInfo.head._4,typeInfo.head._1);
              
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                
                /*val out = dds.foldLeft(Set[String]() ){ case (soFar,curr) => {
                  soFar ++ curr.get(vc).getOrElse(Vector()).toSet
                }}.toVector.sorted.mkString(",")*/
                //reportln("SETS.UNION.run:"+out,"deepDebug");
                //error("NOT YET IMPLEMENTED!");
                val out = vc.info.getOrElse(info,None).map{ xx => {
                  xx.replace(ostr,nstr);
                }}.getOrElse(".");
                writeString(vout,out)
              }
            }
          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "PICK.RANDOM",synon = Seq(),
              shortDesc = "Picks randomly from a given set.",
              desc = "The first parameter must be either '.' or a supplied random seed for the random number generator. "+
                     "You can then provide either a single additional parameter and the output field will be a randomly picked element from that parameter. "+
                     "In this case the output will be chosen from this one input parameter (which is assumed to be a list of some sort), which can be a string constant list delimited with colons and beginning with CONST:, an INFO field, or a text file specified as FILE:filename. "+
                     "Alternately: you can provide several additional parameters, in which case it will select randomly from the set of parameters.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "seed", ty = "String",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "x", ty = "INFO:String|FILE:String|CONST:String",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "y", ty = "INFO:String|CONST:String",req=false,dotdot=true )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params.tail,pv.tail,h)
              override val outType = getSuperType(typeInfo);
              override val outNum = ".";
              val dds : Vector[VcfTagFunctionParamReader[Vector[String]]] = typeInfo.toVector.map{ pprm => VcfTagFunctionParamReaderStringSeq(pprm)}
              val isInternalPick = (typeInfo.length == 1)
              
              val rand = if(pv.head == "auto" || pv.head == "."){
                new scala.util.Random()
              } else {
                val seed = string2intOpt(pv.head);
                if(seed.isEmpty){
                  error("ERROR: malformatted seed: seed must be an int, or else 'auto' or '.' to autoselect a seed.");
                }
                new scala.util.Random( seed.get )
              }
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                val outUnion = if(isInternalPick){
                  dds.head.get(vc).getOrElse(Vector("."))
                } else {
                  dds.map{ ss => ss.get(vc).getOrElse(Vector(".")).mkString(",") }
                }
                if(outUnion.length > 0){
                  val out = outUnion( rand.nextInt( outUnion.length ) )
                  writeString(vout,out)
                } else {
                  writeString(vout,".")
                }
                
              }
            }
          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "SETS.INTERSECT",synon = Seq(),
              shortDesc = "",
              desc = "Input should be a pair of sets that are either INFO fields specified as INFO:tagID, text files specified as FILE:fileName, or a constant set delimited with colons. "+
                     "Output field will be a comma delimited string containing the intersect between the supplied sets.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INFO:String|FILE:String|CONST:String",req=true,dotdot=true )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params,pv,h)
              override val outType = getSuperType(typeInfo);
              override val outNum = ".";
              val dds : Vector[VcfTagFunctionParamReader[Vector[String]]] = typeInfo.toVector.map{ pprm => VcfTagFunctionParamReaderStringSeq(pprm)}
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                val out = dds.tail.foldLeft(dds.head.get(vc).getOrElse(Vector()).toSet ){ case (soFar,curr) => {
                  soFar.intersect( curr.get(vc).getOrElse(Vector()).toSet )
                }}.toVector.sorted.mkString(",")
                writeString(vout,out)
              }
            }
          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "SETS.DIFF",synon = Seq(),
              shortDesc = "",
              desc = "Input should be a pair of sets that are either INFO fields specified as INFO:tagID, text files specified as FILE:fileName, or a constant set delimited with colons."+
                     "Output field will be a comma delimited string containing the elements in the first set with the second set subtracted out.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INFO:String|INFO:Int|INFO:Float|FILE:String|CONST:String|CONST:Int|CONST:Float",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "y", ty = "INFO:String|INFO:Int|INFO:Float|FILE:String|CONST:String|CONST:Int|CONST:Float",req=true,dotdot=false )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params,pv,h).toVector
              override val outType = getSuperType(typeInfo);
              override val outNum = ".";
              val dds : Vector[VcfTagFunctionParamReader[Vector[String]]] = typeInfo.map{ pprm => VcfTagFunctionParamReaderStringSeq(pprm)}
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                val out = dds.tail.foldLeft(dds.head.get(vc).getOrElse(Vector()).toSet ){ case (soFar,curr) => {
                  soFar.intersect( curr.get(vc).getOrElse(Vector()).toSet )
                }}.toVector.sorted.mkString(",")
                writeString(vout,out)
              }
            }
          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "COLLATE",synon = Seq(),
              shortDesc = "Collates multiple ordered info fields",
              desc = "This takes multiple ordered info fields and collates them. The new output "+
                     "INFO field will be a list of lists. The first list in the list of lists will be composed of "+
                     "The first element of the first field, the first element of the second field, the first element of the third field, and so on. "+
                     "The second list in the list of lists will be composed of the second element of the first field, the second element of the second field, and so on. "+
                     "Delimiters in the input lists as well as the two delimiters used in the output can have the following names: colon, comma, bar, slash, period, or ampersand. "+
                     "Note that the input delim can be a slash-delimited list of delimiter names.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "inputDelimName", ty = "CONST:String",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "outputDelimOuter", ty = "CONST:String",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "outputDelimInner", ty = "CONST:String",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "x", ty = "INFO:String",req=true,dotdot=true )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              override val outType = "String";
              override val outNum = ".";
              //val dds : Vector[VcfTagFunctionParamReader[Vector[String]]] = typeInfo.map{ pprm => VcfTagFunctionParamReaderStringSeq(pprm)}
              val inDelim = paramValues.head.split("[/]");
              
              def getDelim(delim : String) : String = {
                    if(delim.toUpperCase() == "COLON"){
                      ":"
                    } else if(delim.toUpperCase() == "SLASH"){
                      "/"
                    } else if(delim.toUpperCase() == "COMMA"){
                      ","
                    } else if(delim.toUpperCase() == "BAR"){
                      "|"
                    } else if(delim.toUpperCase() == "AMPERSAND"){
                      "&"
                    } else if(delim.toUpperCase() == "PERIOD"){
                      "."
                    } else {
                      error("ERROR: Unrecognized delimiter name: "+delim+"! Legal options are: slash, comma, bar, ampersand, and period.");
                      ""
                    }
              }
              
              def getDelimRegex(delimList : Seq[String]) : String = {
                 "["+delimList.map{ delim => {
                      getDelim(delim);
                 }}.mkString("")+"]"
              }
              
              val inDelimRegex = getDelimRegex( inDelim );
              val outDelim1 = getDelim(  paramValues(1)  );
              val outDelim2 = getDelim(  paramValues(2)  );
              val infoFields = paramValues.drop(3);
              
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                /*val out = dds.tail.foldLeft(dds.head.get(vc).getOrElse(Vector()).toSet ){ case (soFar,curr) => {
                  soFar.intersect( curr.get(vc).getOrElse(Vector()).toSet )
                }}.toVector.sorted.mkString(",")
                writeString(vout,out)*/
                val infoseq = infoFields.map{ ff => { vc.info.getOrElse(ff,None).map{ kk => kk.split(inDelimRegex).toSeq }.getOrElse(Seq()) }}
                val infolens = infoseq.map{ xx => xx.length }
                if( (infolens.toSet - 1).size > 1){
                  error("ERROR: Attempting to COLLATE info fields, but info fields have different lengths: ("+
                           infoFields.zip(infolens).map{ case (z1,z2) => { z1+":"+z2 } }.mkString(", ")+"):");
                }
                val infolen = infolens.max;
                val collatedSeq = infoseq.foldLeft( Range(0,infolen).toSeq.map{ yy => { Seq[String]() }} ){ case (soFar,xx) => {
                  if( xx.length == 1){
                    soFar.map{ y => { y :+ xx.head }};
                  } else {
                    soFar.zip(xx).map{ case (y,x) => { y :+ x }};
                  }
                }}
                writeString(vout, collatedSeq.map{ kk => { kk.mkString(outDelim2)} }.mkString(outDelim1) )
              }
            }
          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "CONCAT",synon = Seq(),
              shortDesc = "Concatenates the input",
              desc = "This simple function concatenates the values of the input parameters. Input parameters can be any combination of INFO fields or constant strings.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INFO:String|CONST:String",req=true,dotdot=true )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params,pv,h).toVector
              
                //reportln("SETS.UNION.run: ","deepDebug");
                //reportln("  SETS.UNION.typeInfo: ","deepDebug");
                //typeInfo.foreach{ case (tt,tp,pp,pv) => {
                //   reportln("  ["+tt+","+tp+","+pp.id+"/"+pp.ty+","+pv+"] ","deepDebug");
                //}}

              override val outType = "String";
              override val outNum = ".";
              val dds : Vector[VcfTagFunctionParamReader[Vector[String]]] = typeInfo.map{ pprm => VcfTagFunctionParamReaderStringSeq(pprm)}
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                
                val out = dds.foldLeft(Set[String]() ){ case (soFar,curr) => {
                  soFar ++ curr.get(vc).getOrElse(Vector())
                }}.toVector.mkString("")
                //reportln("SETS.UNION.run:"+out,"deepDebug");

                writeString(vout,out)
              }
            }
          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "SETS.UNION",synon = Seq(),
              shortDesc = "Takes the union from input sets.",
              desc = "The new field will be equal to the union of the inputs. Inputs can either be INFO fields specified with 'INFO:tagName', "+
                      "can point to a text file with 'FILE:filename', or can be constants (delimited with colons). "+
                      "The output will be the union of the given parameters, in alphabetical order. ",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "String|INFO:String|FILE:String",req=true,dotdot=true )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params,pv,h).toVector
              
                //reportln("SETS.UNION.run: ","deepDebug");
                //reportln("  SETS.UNION.typeInfo: ","deepDebug");
                //typeInfo.foreach{ case (tt,tp,pp,pv) => {
                //   reportln("  ["+tt+","+tp+","+pp.id+"/"+pp.ty+","+pv+"] ","deepDebug");
                //}}

              override val outType = getSuperType(typeInfo);
              override val outNum = ".";
              val dds : Vector[VcfTagFunctionParamReader[Vector[String]]] = typeInfo.map{ pprm => VcfTagFunctionParamReaderStringSeq(pprm)}
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                
                val out = dds.foldLeft(Set[String]() ){ case (soFar,curr) => {
                  soFar ++ curr.get(vc).getOrElse(Vector()).toSet
                }}.toVector.sorted.mkString(",")
                //reportln("SETS.UNION.run:"+out,"deepDebug");

                writeString(vout,out)
              }
            }
          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "LEN",synon = Seq(),
              shortDesc = "Length",
              desc = "The new field will be an integer field equal to the length of the input field. Will be missing if the input field is missing.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INFO:String|INFO:Int|INFO:Float",req=true,dotdot=false )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params,pv,h).toVector
              
                //reportln("SETS.UNION.run: ","deepDebug");
                //reportln("  SETS.UNION.typeInfo: ","deepDebug");
                //typeInfo.foreach{ case (tt,tp,pp,pv) => {
                //   reportln("  ["+tt+","+tp+","+pp.id+"/"+pp.ty+","+pv+"] ","deepDebug");
                //}}

              override val outType = "Integer";
              override val outNum = ".";
              val dd :VcfTagFunctionParamReader[Vector[String]] = VcfTagFunctionParamReaderStringSeq(typeInfo.head)
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                
                val out = dd.get(vc).map{ x => {
                  writeInt(vout,x.length)
                }}
                //reportln("SETS.UNION.run:"+out,"deepDebug");

                //writeString(vout,out)
              }
            }
          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "MIN",synon = Seq(),
              shortDesc = "",
              desc = "",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INFO:Int|INFO:Float|CONST:Int|CONST:Float",req=true,dotdot=true )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params,pv,h).toVector
              override val outType = getSuperType(typeInfo);
              override val outNum = "1";
               
              val dd = if(outType == "Integer"){
                Left(typeInfo.map{ pprm => VcfTagFunctionParamReaderIntSeq(pprm)})
              } else {
                Right(typeInfo.map{ pprm => VcfTagFunctionParamReaderFloatSeq(pprm)})
              }
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                dd match {
                  case Left(ddi) => {
                    val v = ddi.flatMap{ d => d.get(vc).getOrElse(Vector()) }
                    if(v.length > 0){
                      writeNum(vout,v.min)
                    }
                  }
                  case Right(ddf) => {
                    val v = ddf.flatMap{ d => d.get(vc).getOrElse(Vector()) }
                    if(v.length > 0){
                      writeNum(vout,v.min)
                    }
                  }
                }
              }
            }

          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "MAX",synon = Seq(),
              shortDesc = "",
              desc = "",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INFO:Int|INFO:Float|CONST:Int|CONST:Float",req=true,dotdot=true )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params,pv,h).toVector
              override val outType = getSuperType(typeInfo);
              override val outNum = "1";
              
              val dd = if(outType == "Integer"){
                Left(typeInfo.map{ pprm => VcfTagFunctionParamReaderIntSeq(pprm)})
              } else {
                Right(typeInfo.map{ pprm => VcfTagFunctionParamReaderFloatSeq(pprm)})
              }
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                dd match {
                  case Left(ddi) => {
                    val v = ddi.flatMap{ d => d.get(vc).getOrElse(Vector()) }
                    if(v.length > 0){
                      writeNum(vout,v.max)
                    }
                  }
                  case Right(ddf) => {
                    val v = ddf.flatMap{ d => d.get(vc).getOrElse(Vector()) }
                    if(v.length > 0){
                      writeNum(vout,v.max)
                    }
                  }
                }
              }
            }

          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "RANDFLAG",synon = Seq(),
              shortDesc = "",
              desc = "",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "FLOAT",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "seed", ty = "INT",req=true,dotdot=false )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              override val outType = "Integer";
              override val outNum = "1";
              val rand = pv.lift(1).map{ s => {
                new scala.util.Random(string2long(s))
              }}.getOrElse( new scala.util.Random() )
              val thresh = string2double(pv.head);
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                val out = if(rand.nextDouble() < thresh ){
                  writeNum(vout,1)
                } else {
                  writeNum(vout,0)
                }
              }
            }

          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "COPY",synon = Seq(),
              shortDesc = "Copies an INFO field",
              desc = "",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "oldField", ty = "INFO:INT|INFO:Float|INFO:String",req=true,dotdot=false )//,
                  //VcfTagFunctionParam( id = "newField", ty = "String",req=true,dotdot=false )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params,pv,h).toVector
              val oldField = pv(0);
              //val newField = pv(1);
              override val outType = h.infoLines.find{ _.ID == oldField }.map{ _.Type }.getOrElse("???")
              override val outNum = h.infoLines.find{ _.ID == oldField }.map{ _.Number }.getOrElse("???")
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                vc.info.getOrElse(oldField,None).foreach{ vv => writeString(vout,vv) }
              }
            }

          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "SWITCH.EXPR",synon = Seq(),
              shortDesc = "Switches between two options depending on a logical expression.",
              desc = "Switches between two options depending on a logical expression. "+
                      "The 'expr' expression parameter must be formatted like standard variant-level expressions. "+
                      "The A and B parameters can each be either a constant or an INFO field. The output field will be equal to A if "+
                      "the logical expression is TRUE, and otherwise will be B.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "expr", ty = "STRING",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "A", ty = "INFO:Int|INFO:Float|INFO:String|CONST:Int|CONST:Float|CONST:String",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "B", ty = "INFO:Int|INFO:Float|INFO:String|CONST:Int|CONST:Float|CONST:String",req=false,dotdot=false )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params.tail,pv.tail,h).toVector
              override val outType = getSuperType(typeInfo);
              override val outNum = "1";
              val dd = typeInfo.map{ pprm => VcfTagFunctionParamReaderString(pprm)}.padTo(2,VcfTagFunctionParamReader_String(".","CONST"))
              /*val dd = if(outType == "Integer"){
                Left(typeInfo.map{ case (tt,tp,pp,pv) => VcfTagFunctionParamReader_Int(pv,tt)})
              } else if(outType == "Float"){
                Right(Left( typeInfo.map{ case (tt,tp,pp,pv) => VcfTagFunctionParamReader_Float(pv,tt)} ))
              } else {
                Right(Right( typeInfo.map{ case (tt,tp,pp,pv) => VcfTagFunctionParamReader_String(pv,tt)} ))
              }*/
              val expr = paramValues.head;
              val parser : SFilterLogicParser[SVcfVariantLine] = internalUtils.VcfTool.sVcfFilterLogicParser;
              val filter : SFilterLogic[SVcfVariantLine] = parser.parseString(expr);
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                val k = if( filter.keep(vc) ){
                  dd(0).get(vc).getOrElse(".")
                } else {
                  dd(1).get(vc).getOrElse(".")
                }
                //notice(newTag+"="+k+" tagged for variant:\n    "+vc.getSimpleVcfString(),"TAGGED_"+newTag+"_"+k,1);
                writeString(vout,k);
              }
            }
          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        new VcfTagFcnFactory(){ 
          val mmd =  new VcfTagFcnMetadata(
              id = "EXPR",synon = Seq("expr"),
              shortDesc = "Creates a true/false field based on an expression",
              desc = "The new field will be an integer field which will be equal to 1 if and only if the expression is TRUE, and 0 otherwise. "+
                     "See the expression format definition for more information on how the logical expression syntax works.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "expr", ty = "STRING",req=true,dotdot=false ),
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              override val outType = "Integer";
              override val outNum = "1";
              val expr = paramValues.head;
              val parser : SFilterLogicParser[SVcfVariantLine] = internalUtils.VcfTool.sVcfFilterLogicParser;
              val filter : SFilterLogic[SVcfVariantLine] = parser.parseString(expr);
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                val k = if( filter.keep(vc) ){
                  "1"
                } else {
                  "0"
                }
                notice(newTag+"="+k+" tagged for variant:\n    "+vc.getSimpleVcfString(),"TAGGED_"+newTag+"_"+k,1);
                writeString(vout,k);
              }
            }

          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        new VcfTagFcnFactory(){ 
          val mmd =  new VcfTagFcnMetadata(
              id = "GT.EXPR",synon = Seq("expr"),
              shortDesc = "Creates a count field, counting the number of samples that satisfy a genotype-level expression",
              desc = "The new field will be an integer field which will be equal to the number of samples that satisfy a given genotype-level expression. "+
                     "See the expression format definition for more information on how the logical expression syntax works. "+
                     "You can also specify a variant-level expression which, if false, will return missing.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "gtExpr", ty = "STRING",req=false,dotdot=false ),
                  VcfTagFunctionParam( id = "varExpr", ty = "STRING",req=false,dotdot=false )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              override val outType = "Integer";
              override val outNum = "1";
              val gtexpr = paramValues.head;
              val gtparser : SFilterLogicParser[(SVcfVariantLine,Int)] = internalUtils.VcfTool.sGenotypeFilterLogicParser;
              val varparser : SFilterLogicParser[SVcfVariantLine] = internalUtils.VcfTool.sVcfFilterLogicParser;
              
              val gtfilter : SFilterLogic[(SVcfVariantLine,Int)] = gtparser.parseString(gtexpr);
              val varfilter : SFilterLogic[SVcfVariantLine] = paramValues.tail.headOption match {
                case Some(ee) => {
                  varparser.parseString(ee);
                }
                case None => {
                  varparser.parseString("TRUE");
                }
              }
              def run(vc : SVcfVariantLine, vout : SVcfOutputVariantLine){
                val k = if( varfilter.keep(vc) ){
                  Range(0,outHeader.sampleCt).count{ii => {
                    gtfilter.keep((vc,ii))
                  }}.toString
                } else {
                  "."
                }
                notice(newTag+"="+k+" tagged for variant:\n    "+vc.getSimpleVcfString(),"TAGGED_"+newTag,5);
                writeString(vout,k);
              }
            }

          }
        }
      ).map{ ff => {
        (ff.metadata.id,ff)
      }}.toMap
  

  class RunTally(func : String, newTag : String, paramTags : Seq[String], digits : Option[Int] = None, desc : Option[String] = None , sampleToGroupMap : scala.collection.mutable.AnyRefMap[String,Set[String]] ) extends internalUtils.VcfTool.SVcfWalker { 
    def walkerName : String = "RunFunction."+newTag
    //keywords: tagVariantFunction tagVariantsFunction Variant Function
    val f : String = func.toUpperCase;
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("newTag",newTag),
        ("func",func),
        ("paramTags",paramTags.mkString("|"))
    );
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      var errCt = 0;
      
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);

      val fcn = vcfTagFcnMap_sideEffecting( func ).gen( paramValues = paramTags, outHeader = outHeader, newTag = newTag, digits = digits);
      
      val minParamsRequired = fcn.md.params.filter{ pp => pp.req }.length
      val maxParamsAllowed  = if( fcn.md.params.exists( pp => pp.dotdot ) ){
        None
      } else {
        Some(fcn.md.params.length)
      }
      if(paramTags.length < minParamsRequired){
        error("   ERROR in function setup: tagFunction "+func+" has "+minParamsRequired+" mandatory parameters. We found "+paramTags.length+": \""+paramTags.mkString(",")+"\"");
      }
      maxParamsAllowed.foreach{ mp => {
        if(paramTags.length > mp){
          error("   ERROR in function setup: tagFunction "+func+" can only take "+mp+" parameters. We found "+paramTags.length+": \""+paramTags.mkString(",")+"\"");
        }
      }}
      //
      //outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO",newTag,Number=fcn.outNum,Type=fcn.outType,desc=desc.getOrElse("") +" (Result of performing function "+func+" on params: "+paramTags.mkString(",")+".)")),Some(this));

      //val overwriteInfos : Set[String] = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( outHeader.addedInfos );
      //if( overwriteInfos.nonEmpty ){
      //  notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      //}
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine();
        //vc.dropInfo(overwriteInfos);
        fcn.run(v,vc);
        vc;
      }}, closeAction = (() => {
        fcn.close();
      })),outHeader)
    }
  }
  

  class AddFunctionFormat(func : String, newTag : String, paramTags : Seq[String], desc : Option[String],
                          sampleToGroupMap : scala.collection.mutable.AnyRefMap[String,Set[String]]  ) extends internalUtils.VcfTool.SVcfWalker {
    def walkerName : String = "FunctionInfoTag."+newTag
    //keywords: tagVariantFunction tagVariantsFunction Variant Function
    val f : String = func.toUpperCase;
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("newTag",newTag),
        ("func",func),
        ("paramTags",paramTags.mkString("|"))
    );

    /*val (sampleToGroupMap,groupToSampleMap,groups) : (,
                           scala.collection.mutable.AnyRefMap[String,Set[String]],
                           Vector[String]) = internalTests.SVcfWalkerUtils.getGroups(groupFile, groupList, superGroupList);
      */
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      var errCt = 0;
      
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);

      val fcn = vcfFormatFunMap( func ).gen( paramValues = paramTags, outHeader = outHeader, newTag = newTag, digits = None);
      
      val minParamsRequired = fcn.md.params.filter{ pp => pp.req }.length
      val maxParamsAllowed  = if( fcn.md.params.exists( pp => pp.dotdot ) ){
        None
      } else {
        Some(fcn.md.params.length)
      }
      if(paramTags.length < minParamsRequired){
        error("   ERROR in function setup: tagFunction "+func+" has "+minParamsRequired+" mandatory parameters. We found "+paramTags.length+": \""+paramTags.mkString(",")+"\"");
      }
      maxParamsAllowed.foreach{ mp => {
        if(paramTags.length > mp){
          error("   ERROR in function setup: tagFunction "+func+" can only take "+mp+" parameters. We found "+paramTags.length+": \""+paramTags.mkString(",")+"\"");
        }
      }}
      //
      outHeader.addFormatLine((new SVcfCompoundHeaderLine("FORMAT",newTag,Number=fcn.outNum,Type=fcn.outType,desc=desc.getOrElse("") +" (Result of performing function "+func+" on params: "+paramTags.mkString(",")+".)")),Some(this));

      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine();
        vc.genotypes.sampList = vcfHeader.titleLine.sampleList.toList;
        vc.genotypes.sampGrp = Some(sampleToGroupMap);
        fcn.run(v,vc);
        vc;
      }}, closeAction = (() => {
        
      })),outHeader)
    }
  }

  class AddFunctionTag(func : String, newTag : String, paramTags : Seq[String], digits : Option[Int], desc : Option[String],
                         sampleToGroupMap : scala.collection.mutable.AnyRefMap[String,Set[String]]
                        ) extends internalUtils.VcfTool.SVcfWalker { 
    def walkerName : String = "FunctionInfoTag."+newTag
    //keywords: tagVariantFunction tagVariantsFunction Variant Function
    val f : String = func.toUpperCase;
    def walkerParams : Seq[(String,String)] =  Seq[(String,String)](
        ("newTag",newTag),
        ("func",func),
        ("paramTags",paramTags.mkString("|"))
    );
    
   /* val (sampleToGroupMap,groupToSampleMap,groups) : (scala.collection.mutable.AnyRefMap[String,Set[String]],
                           scala.collection.mutable.AnyRefMap[String,Set[String]],
                           Vector[String]) = internalTests.SVcfWalkerUtils.getGroups(groupFile, groupList, superGroupList);
      */
    
    def walkVCF(vcIter : Iterator[SVcfVariantLine], vcfHeader : SVcfHeader, verbose : Boolean = true) : (Iterator[SVcfVariantLine], SVcfHeader) = {
      var errCt = 0;
      
      val outHeader = vcfHeader.copyHeader
      outHeader.addWalk(this);
      
      val fcn = vcfTagFunMap( func ).gen( paramValues = paramTags, outHeader = outHeader, newTag = newTag, digits = digits);
      
      val minParamsRequired = fcn.md.params.filter{ pp => pp.req }.length
      val maxParamsAllowed  = if( fcn.md.params.exists( pp => pp.dotdot ) ){
        None
      } else {
        Some(fcn.md.params.length)
      }
      if(paramTags.length < minParamsRequired){
        error("   ERROR in function setup: tagFunction "+func+" has "+minParamsRequired+" mandatory parameters. We found "+paramTags.length+": \""+paramTags.mkString(",")+"\"");
      }
      maxParamsAllowed.foreach{ mp => {
        if(paramTags.length > mp){
          error("   ERROR in function setup: tagFunction "+func+" can only take "+mp+" parameters. We found "+paramTags.length+": \""+paramTags.mkString(",")+"\"");
        }
      }}
      //
      outHeader.addInfoLine((new SVcfCompoundHeaderLine("INFO",newTag,Number=fcn.outNum,Type=fcn.outType,desc=desc.getOrElse("") +" (Result of performing function "+func+" on params: "+paramTags.mkString(",")+".)")),Some(this));

      val overwriteInfos : Set[String] = vcfHeader.infoLines.map{ii => ii.ID}.toSet.intersect( outHeader.addedInfos );
      if( overwriteInfos.nonEmpty ){
        notice("  Walker("+this.walkerName+") overwriting "+overwriteInfos.size+" INFO fields: \n        "+overwriteInfos.toVector.sorted.mkString(","),"OVERWRITE_INFO_FIELDS",-1)
      }
      (addIteratorCloseAction( iter = vcMap(vcIter){v => {
        val vc = v.getOutputLine();
        vc.dropInfo(overwriteInfos);
        fcn.run(v,vc);
        vc;
      }}, closeAction = (() => {
        
      })),outHeader)
    }
  }
  
      
      
      
      
      
   val TAGFUNCTIONS_USERMANUALBLOCKS : Seq[internalUtils.commandLineUI.UserManualBlock] = Seq[internalUtils.commandLineUI.UserManualBlock](
       internalUtils.commandLineUI.UserManualBlock(title=Some("INFO TAG FUNCTIONS"),
                                                   lines = Seq("","Info Tag Functions are simple modular functions that take  "+
                                                               "one variant at a time and add a new INFO field. "+
                                                               "",
                                                               "Basic Syntax:",
                                                               "    --FCN addInfoTag|newTagID|FCN( param1, param2, etc. )"), level = 1, indentTitle = 0, indentBlock = 2, indentFirst = 2),
       internalUtils.commandLineUI.UserManualBlock(title=Some("Available Functions:"),
                                                   lines = Seq(""), level = 2,indentTitle = 2, indentBlock = 2, indentFirst = 2)
   ) ++ vcfTagFunMap.flatMap{ case (fcnID,mf) => {
      val fcnTitleLine =  internalUtils.commandLineUI.UserManualBlock(title=Some( fcnID + "("+mf.metadata.params.filter{ pp => ! pp.hidden }.map{ pp => pp.id + (if(pp.dotdot){"..."}else{""}) }.mkString(",")+")"),
                                                   lines = Seq("",
                                                               mf.metadata.desc), 
                                                   level = 3, indentTitle = 4, indentBlock = 8, indentFirst=8)
      Seq(fcnTitleLine) ++
          mf.metadata.params.filter{ pp => ! pp.hidden }.toSeq.map{ pp => {
        internalUtils.commandLineUI.UserManualBlock(lines = Seq(pp.id+(if(pp.dotdot){"..."}else{""})+" "+
                                                            pp.desc +{ if(pp.req){
                                                              ""
                                                            } else {
                                                              "(Optional) "
                                                            }}+"("+pp.ty+") "+pp.desc ), indentTitle = 4, indentBlock = 12, indentFirst=10,mdCaret=true)
      }}
   }}
   
     
   def TAGFUNCTIONS_getBlockStringManual : String = TAGFUNCTIONS_USERMANUALBLOCKS.map{ umb => {
     umb.getBlockString()
   }}.mkString("\n")
   def TAGFUNCTIONS_getMarkdownStringManual : String = TAGFUNCTIONS_USERMANUALBLOCKS.map{ umb => {
     umb.getMarkdownString();
   }}.mkString("\n")
      
      
      
      
      
      
      
      
}









 


















