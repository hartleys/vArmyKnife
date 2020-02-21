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
        } else if(s == "GENO"){
          GENO
        } else if(s == "FILE"){
          FILE
        } else {
          CONST
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

    def getParsedParam(param : String, h : SVcfHeader) : VcfFcnParsedParam = {
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
        VcfFcnParsedParam(ParamSrc.FILE,ParamType.STRING,".",this, param.drop(5))
      } else if(param.toUpperCase().startsWith("INFO:") || param.toUpperCase().startsWith("GENO:")){
        val SRC = param.toUpperCase().take(4)
        val SRCSRC = ParamSrc.getFromString( SRC );
        if( ! TYS.exists{ ss => ss.startsWith(SRC+"_")} ){
          error("Fatal Error: param "+id+" is not permitted to be an "+SRC+" field! It must be one of these types: "+ty);
        }
        checkIsInfoOrGeno( param.substring(5), SRC).map{ ln => {
          val num = ln.Number;
          if(ln.Type == "String"){
            //if(TYS.contains("FILE_STRING")){
            //  ("FILE","String",num)
            //} else 
            if(TYS.contains(SRC+"_STRING")){
              VcfFcnParsedParam(SRCSRC,ParamType.STRING,num,this, param.drop(5))
            } else if(TYS.contains(SRC+"_FLOAT")){
              warning("Attempting to coerce String "+SRC+" field "+ param.substring(5)+" into a float.","COERCE_"+SRC+"_STRING_TO_FLOAT",-1)
              VcfFcnParsedParam(SRCSRC,ParamType.FLOAT,num,this, param.drop(5))
            } else if(TYS.contains(SRC+"_INT")){
              warning("Attempting to coerce String "+SRC+" field "+ param.substring(5)+" into an int.","COERCE_"+SRC+"_STRING_TO_INT",-1)
              VcfFcnParsedParam(SRCSRC,ParamType.INT,num,this, param.drop(5))
            } else {
              VcfFcnParsedParam(SRCSRC,ParamType.ERR,num,this, param.drop(5))
            }
          } else if(ln.Type == "Float"){
            if(TYS.contains(SRC+"_FLOAT")){
              VcfFcnParsedParam(SRCSRC,ParamType.FLOAT,num,this, param.drop(5))
            } else if(TYS.contains(SRC+"_STRING")){
              VcfFcnParsedParam(SRCSRC,ParamType.STRING,num,this, param.drop(5))
            } else if(TYS.contains(SRC+"_INT")){
              warning("Attempting to coerce Float "+SRC+" field "+ param.substring(5)+" into an INT.","COERCE_"+SRC+"_FLOAT_TO_INT",-1)
              VcfFcnParsedParam(SRCSRC,ParamType.INT,num,this, param.drop(5))
            } else {
              VcfFcnParsedParam(SRCSRC,ParamType.ERR,num,this, param.drop(5))
            }
          } else if(ln.Type == "Integer"){
            if(TYS.contains(SRC+"_INT")){
              VcfFcnParsedParam(SRCSRC,ParamType.INT,num,this, param.drop(5))
            } else if(TYS.contains(SRC+"_FLOAT")){
              VcfFcnParsedParam(SRCSRC,ParamType.FLOAT,num,this, param.drop(5))
            } else if(TYS.contains(SRC+"_STRING")){
              VcfFcnParsedParam(SRCSRC,ParamType.STRING,num,this, param.drop(5))
            } else {
              VcfFcnParsedParam(SRCSRC,ParamType.ERR,num,this, param.drop(5))
            }
          } else if(ln.Type == "Flag"){
              VcfFcnParsedParam(SRCSRC,ParamType.INT,num,this, param.drop(5))
          } else {
              VcfFcnParsedParam(SRCSRC,ParamType.ERR,num,this, param.drop(5))
          }
        }}.getOrElse({
          error(param.substring(5) + " field not found in header!");
          VcfFcnParsedParam(SRCSRC,ParamType.ERR,num,this, param.drop(5))
        })
      } else if(checkIsInt(param)){
        val num = param.split(":").length.toString;
        VcfFcnParsedParam(ParamSrc.CONST,ParamType.INT,num,this, param)
      } else if(checkIsFloat(param)){
        val num = param.split(":").length.toString;
        VcfFcnParsedParam(ParamSrc.CONST,ParamType.FLOAT,num,this, param)
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
        VcfFcnParsedParam(ParamSrc.CONST,ParamType.STRING,num,this, param)
      }
      
    }

    
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
        if( ! TYS.exists{ ss => ss.startsWith("INFO_")} ){
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
    }
    
    def checkParamType( param : String, h : SVcfHeader) : Boolean = {
      getFinalInputType(param,h)._2 != "?"
    }
  }
  
  
  
  case class TFParamParser[T]( param : String, reader : VcfTagFunctionParamReader[T] ){
    def get(v : SVcfVariantLine) : Option[T] = reader.get(v);
  }

  abstract class VcfTagFunctionParamReader[+T](){
    def get(v : SVcfVariantLine) : Option[T]
    def isConst() : Boolean;
  }
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
      def checkVcfTagParams() : Boolean = {
          md.params.padTo( pv.length, md.params.last ).zip(pv).exists{ case (vtfp, pvv) => {
              ! vtfp.checkParamType(pvv, h);
          }}
      }
      
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
    

    def getTypeInfo(fcParam : Seq[VcfTagFunctionParam], params : Seq[String], h : SVcfHeader) : Vector[(String,String,VcfTagFunctionParam,String)] = {
     fcParam.padTo(params.length, fcParam.last).zip(params).map{ case (pp,pv) => {
            val tpair = pp.getFinalInputType(pv,h);
            val pvv = if(tpair._1 == "INFO" || tpair._1 == "GENO"){
              pv.drop(5)
            } else {
              pv
            }
            (tpair._1,tpair._2,pp,pvv)
      }}.toVector
    }
    def getSuperType(typeInfo : Vector[(String,String,VcfTagFunctionParam,String)]) : String = {
      val types = typeInfo.map{_._2}
      if( types.forall( tt => tt == "Integer" ) ) {
        "Integer"
      } else if( types.contains( "String" ) ) {
        "String"
      } else {
        "Float"
      }
    }
    
     def metadata : VcfTagFcnMetadata;
     def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn;
  }
  
  val vcfTagFunMap : Map[String,VcfTagFcnFactory] = Seq[VcfTagFcnFactory](
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "SUM",synon = Seq(),
              shortDesc = "Sum of several tags or numeric constants.",
              desc = "Input should be a set of info tags specified as INFO:tagID, and numeric constants. "+
                     "Output field will be the sum of the inputs. Missing INFO fields will be treated as zeros "+
                     "unless all params are INFO fields and all are missing, in which case the output will be missing. "+
                     "Output field type will be an integer if all inputs are integers and otherwise a float.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INT|FLOAT|INFO_Int|INFO_Float",req=true,dotdot=true )
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
                Left(typeInfo.map{ case (tt,tp,pp,pv) => VcfTagFunctionParamReader_IntSeq(pv,tt)})
              } else {
                Right(typeInfo.map{ case (tt,tp,pp,pv) => VcfTagFunctionParamReader_FloatSeq(pv,tt)})
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
              id = "PRODUCT.ARRAY",synon = Seq(),
              shortDesc = "",
              desc = "Input should be a set of numeric constants or info tags specified as INFO:tagID. "+
                     "Output field will be the product of the inputs. Missing INFO fields will be treated as ones "+
                     "unless all params are INFO fields and all are missing, in which case the output will be missing. "+
                     "Output field type will be an integer if all inputs are integers and otherwise a float.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INT|FLOAT|INFO_Int|INFO_Float",req=true,dotdot=true )
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
                Left(typeInfo.map{ case (tt,tp,pp,pv) => VcfTagFunctionParamReader_IntSeq(pv,tt)})
              } else {
                Right(typeInfo.map{ case (tt,tp,pp,pv) => VcfTagFunctionParamReader_FloatSeq(pv,tt)})
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
              desc = "Input should be a pair of numeric constants and/or info fields specified as INFO:tagID. "+
                     "Output field will be the product of the two inputs. Missing INFO fields will be treated as ZEROS "+
                     "unless all params are INFO fields and all are missing, in which case the output will be missing. "+
                     "Output field type will be an integer if all inputs are integers and otherwise a float.",
                     params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INT|FLOAT|INFO_Int|INFO_Float",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "y", ty = "INT|FLOAT|INFO_Int|INFO_Float",req=true,dotdot=false )
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
                Left(typeInfo.map{ case (tt,tp,pp,pv) => VcfTagFunctionParamReader_Int(pv,tt)})
              } else {
                Right(typeInfo.map{ case (tt,tp,pp,pv) => VcfTagFunctionParamReader_Float(pv,tt)})
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
              desc = "Input should be a pair of numeric constants and/or info fields specified as INFO:tagID. "+
                     "Output field will be the product of the two inputs. Missing INFO fields will be treated as ZEROS "+
                     "unless all params are INFO fields and all are missing, in which case the output will be missing. "+
                     "Output field type will be a float.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INT|FLOAT|INFO_Int|INFO_Float",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "y", ty = "INT|FLOAT|INFO_Int|INFO_Float",req=true,dotdot=false )
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
              val ddf : Vector[VcfTagFunctionParamReader[Double]]  = typeInfo.map{ case (tt,tp,pp,pv) => VcfTagFunctionParamReader_Float(pv,tt)}
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
              desc = "Input should be a pair of numeric constants and/or info fields specified as INFO:tagID. "+
                     "Output field will be the difference of the two inputs (ie x - y). Missing INFO fields will be treated as ZEROS "+
                     "unless all params are INFO fields and all are missing, in which case the output will be missing. "+
                     "Output field type will be an integer if all inputs are integers and otherwise a float.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INT|FLOAT|INFO_Int|INFO_Float",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "y", ty = "INT|FLOAT|INFO_Int|INFO_Float",req=true,dotdot=false )
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
              val dd : Either[Vector[VcfTagFunctionParamReader_Int], Vector[VcfTagFunctionParamReader_Float]] = if(outType == "Integer"){ 
                Left(typeInfo.map{ case (tt,tp,pp,pv) => VcfTagFunctionParamReader_Int(pv,tt)})
              } else {
                Right(typeInfo.map{ case (tt,tp,pp,pv) => VcfTagFunctionParamReader_Float(pv,tt)})
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
              id = "CONVERT.TO.INT",synon = Seq(),
              shortDesc = "",
              desc = "Input should be an INFO field",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INFO_String",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "defaultValue", ty = "Int",req=false,dotdot=false )
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
                vc.info.getOrElse(x,None).foreach{ xx => {
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
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "CONVERT.TO.FLOAT",synon = Seq(),
              shortDesc = "",
              desc = "Input should be an INFO field. Converts to a numeric float. If no defaultValue is supplied then non-floats will be dropped. Note that NaN and Inf will be dropped / replaced with the default.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INFO_String",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "defaultValue", ty = "Float",req=false,dotdot=false )
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
              desc = "Input should be a set of infoFields and optionally a name, with the format tagID:name. "+
                     "If names are omitted, then the name will be equal to the tagID. "+
                     "Output field will be the set of names for which the respective info field is equal to 1. Any value other than 1, including missing fields, will be treated as 0.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "INFO_Int",req=true,dotdot=true )
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
              val ddi : Vector[VcfTagFunctionParamReader[Int]] = typeInfo.map{ case (tt,tp,pp,pv) => VcfTagFunctionParamReader_Int(pv,tt)}
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
                  VcfTagFunctionParam( id = "x", ty = "INFO_String",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "decoder", ty = "FILE_String",req=true,dotdot=false ),
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
              
              val ddTag : VcfTagFunctionParamReader[Vector[String]] = VcfTagFunctionParamReader_StringSeq(typeInfo.head._4,typeInfo.head._1);
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
              id = "PICK.RANDOM",synon = Seq(),
              shortDesc = "Picks randomly from a given set.",
              desc = "The first parameter must be either '.' or a supplied random seed for the random number generator. "+
                     "You can then provide either a single additional parameter and the output field will be a randomly picked element from that parameter. "+
                     "In this case the output will be chosen from this one input parameter (which is assumed to be a list of some sort), which can be a string constant list delimited with colons, an INFO field specified as INFO:fieldName, or a text file specified as FILE:filename. "+
                     "Alternately: you can provide several additional parameters, in which case it will select randomly from the set of parameters.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "seed", ty = "String",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "x", ty = "String|INFO_String|FILE_String",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "y", ty = "String|INFO_String",req=false,dotdot=true )
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
              val dds : Vector[VcfTagFunctionParamReader[Vector[String]]] = typeInfo.map{ case (tt,tp,pp,pv) => VcfTagFunctionParamReader_StringSeq(pv,tt)}
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
                  VcfTagFunctionParam( id = "x", ty = "String|INFO_String|FILE_String",req=true,dotdot=true )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params,pv,h)
              override val outType = "String";
              override val outNum = ".";
              val dds : Vector[VcfTagFunctionParamReader[Vector[String]]] = typeInfo.map{ case (tt,tp,pp,pv) => VcfTagFunctionParamReader_StringSeq(pv,tt)}
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
                  VcfTagFunctionParam( id = "x", ty = "String|INFO_String|INT|FLOAT|INFO_Int|INFO_Float|FILE_String",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "y", ty = "String|INFO_String|INT|FLOAT|INFO_Int|INFO_Float|FILE_String",req=true,dotdot=false )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params,pv,h)
              override val outType = "String";
              override val outNum = ".";
              val dds : Vector[VcfTagFunctionParamReader[Vector[String]]] = typeInfo.map{ case (tt,tp,pp,pv) => VcfTagFunctionParamReader_StringSeq(pv,tt)}
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
              id = "CONCAT",synon = Seq(),
              shortDesc = "Concatenates the input",
              desc = "This simple function concatenates the values of the input parameters. Input parameters can be any combination of INFO fields or constant strings.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "x", ty = "String|INFO_String",req=true,dotdot=true )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params,pv,h)
              
                //reportln("SETS.UNION.run: ","deepDebug");
                //reportln("  SETS.UNION.typeInfo: ","deepDebug");
                //typeInfo.foreach{ case (tt,tp,pp,pv) => {
                //   reportln("  ["+tt+","+tp+","+pp.id+"/"+pp.ty+","+pv+"] ","deepDebug");
                //}}

              override val outType = "String";
              override val outNum = ".";
              val dds : Vector[VcfTagFunctionParamReader[Vector[String]]] = typeInfo.map{ case (tt,tp,pp,pv) => VcfTagFunctionParamReader_StringSeq(pv,tt)}
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
                  VcfTagFunctionParam( id = "x", ty = "String|INFO_String|FILE_String",req=true,dotdot=true )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params,pv,h)
              
                //reportln("SETS.UNION.run: ","deepDebug");
                //reportln("  SETS.UNION.typeInfo: ","deepDebug");
                //typeInfo.foreach{ case (tt,tp,pp,pv) => {
                //   reportln("  ["+tt+","+tp+","+pp.id+"/"+pp.ty+","+pv+"] ","deepDebug");
                //}}

              override val outType = "String";
              override val outNum = ".";
              val dds : Vector[VcfTagFunctionParamReader[Vector[String]]] = typeInfo.map{ case (tt,tp,pp,pv) => VcfTagFunctionParamReader_StringSeq(pv,tt)}
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
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params,pv,h)
              override val outType = getSuperType(typeInfo);
              override val outNum = "1";
               
              val dd = if(outType == "Integer"){
                Left(typeInfo.map{ case (tt,tp,pp,pv) => VcfTagFunctionParamReader_IntSeq(pv,tt)})
              } else {
                Right(typeInfo.map{ case (tt,tp,pp,pv) => VcfTagFunctionParamReader_FloatSeq(pv,tt)})
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
                  VcfTagFunctionParam( id = "x", ty = "INT|FLOAT|INFO_Int|INFO_Float",req=true,dotdot=true )
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
                Left(typeInfo.map{ case (tt,tp,pp,pv) => VcfTagFunctionParamReader_IntSeq(pv,tt)})
              } else {
                Right(typeInfo.map{ case (tt,tp,pp,pv) => VcfTagFunctionParamReader_FloatSeq(pv,tt)})
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
              val typeInfo = getTypeInfo(md.params,pv,h)
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
              shortDesc = "",
              desc = "",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "oldField", ty = "INFO_Float|INFO_Double|INFO_String",req=true,dotdot=false )//,
                  //VcfTagFunctionParam( id = "newField", ty = "String",req=true,dotdot=false )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params,pv,h)
              val oldField = pv(0);
              //val newField = pv(1);
              override val outType = h.infoLines.find{ _.ID == oldField }.map{ _.Type }.getOrElse("???")
              override val outNum = h.infoLines.find{ _.ID == oldField }.map{ _.Number }.getOrElse("???")
              val thresh = string2double(pv.head);
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
                      "The x and y parameters can each be either a constant or an INFO field. The output field will be equal to x if "+
                      "the logical expression is TRUE, and otherwise will be y.",
              params = Seq[VcfTagFunctionParam](
                  VcfTagFunctionParam( id = "expr", ty = "STRING",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "x", ty = "INFO_Int|INFO_Float|INFO_String|Int|Float|String",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "y", ty = "INFO_Int|INFO_Float|INFO_String|Int|Float|String",req=false,dotdot=false )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params.tail,pv.tail,h)
              override val outType = getSuperType(typeInfo);
              override val outNum = "1";
              val dd = typeInfo.map{ case (tt,tp,pp,pv) => VcfTagFunctionParamReader_String(pv,tt)}.padTo(2,VcfTagFunctionParamReader_String(".","CONST"))
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
              id = "EXPR",synon = Seq(),
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
        }
      ).map{ ff => {
        (ff.metadata.id,ff)
      }}.toMap
  

  class AddFunctionTag(func : String, newTag : String, paramTags : Seq[String], digits : Option[Int] = None, desc : Option[String] = None ) extends internalUtils.VcfTool.SVcfWalker { 
    def walkerName : String = "FunctionInfoTag."+newTag
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
                                                   lines = Seq("","Info Tag Functions are simple functions that take  "+
                                                               "one variant at a time. When more than one function is specified in a run, these functions are performed in the order "+
                                                               "that they appear in the command line, after all other operations have been carried out (excluding output ops). "+
                                                               "",
                                                               "Basic Syntax:",
                                                               "    --FCN addIntoTag|newTagID|fcn=infoTagFunction|params=p1,p2,..."), level = 1, indentTitle = 0, indentBlock = 2, indentFirst = 2),
       internalUtils.commandLineUI.UserManualBlock(title=Some("Available Functions:"),
                                                   lines = Seq(""), level = 2,indentTitle = 2)
   ) ++ vcfTagFunMap.flatMap{ case (fcnID,mf) => {
      val fcnTitleLine =  internalUtils.commandLineUI.UserManualBlock(title=Some( fcnID + "("+mf.metadata.params.filter{ pp => ! pp.hidden }.map{ pp => pp.id + (if(pp.dotdot){"..."}else{""}) }.mkString(",")+")"),
                                                   lines = Seq("",
                                                               mf.metadata.desc), 
                                                   level = 3, indentTitle = 4, indentBlock = 8, indentFirst=4)
      Seq(fcnTitleLine) ++
          mf.metadata.params.filter{ pp => ! pp.hidden }.toSeq.map{ pp => {
        internalUtils.commandLineUI.UserManualBlock(lines = Seq(pp.id+(if(pp.dotdot){"..."}else{""})+" "+
                                                            pp.desc +{ if(pp.req){
                                                              ""
                                                            } else {
                                                              "(Optional) "
                                                            }}+"("+pp.ty+")" ), indentTitle = 4, indentBlock = 12, indentFirst=8)
      }}
   }}
     /*
   def MAPFUNCTIONS_getBlockStringManual : String = MAPFUNCTIONS_USERMANUALBLOCKS.map{ umb => {
     umb.getBlockString()
   }}.mkString("\n")
   def MAPFUNCTIONS_getMarkdownStringManual : String = MAPFUNCTIONS_USERMANUALBLOCKS.map{ umb => {
     umb.getMarkdownString();
   }}.mkString("\n")*/
      
      
      
      
      
      
      
      
}









 


















