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

  abstract class VcfTagFunctionParamReader[+T](){
    def get(v : SVcfVariantLine) : Option[T]
    def isConst() : Boolean;
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
      reportln("param:"+param+"/inputType:"+inputType+" RETURNS SOME("+x+")","deepDebug");
      Some(x);
    } else if(inputType == "INFO"){
      reportln("param:"+param+"/inputType:"+inputType,"deepDebug");
      v.info.getOrElse( param, None).map{ z => {
        reportln(">     "+z,"deepDebug");
        z.split(",").toVector;
      }}
    } else {
      reportln("param:"+param+"/inputType:"+inputType+" RETURNS NONE","deepDebug");
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
              def run(vc : SVcfOutputVariantLine){
                dd match {
                  case Left(ddi) => {
                    val v = ddi.flatMap{ d => d.get(vc).getOrElse(Vector()) }
                    if(v.length > 0){
                      writeNum(vc,v.sum)
                    }
                  }
                  case Right(ddf) => {
                    val v = ddf.flatMap{ d => d.get(vc).getOrElse(Vector()) }
                    if(v.length > 0){
                      writeNum(vc,v.sum)
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
              def run(vc : SVcfOutputVariantLine){
                dd match {
                  case Left(ddi) => {
                    val v = ddi.flatMap{ d => d.get(vc).getOrElse(Vector()) }
                    if(v.length > 0){
                      writeNum(vc,v.product)
                    }
                  }
                  case Right(ddf) => {
                    val v = ddf.flatMap{ d => d.get(vc).getOrElse(Vector()) }
                    if(v.length > 0){
                      writeNum(vc,v.product)
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
              def run(vc : SVcfOutputVariantLine){
                dd match {
                  case Left(ddi) => {
                    val v = ddi.flatMap{ d => d.get(vc) }
                    if(v.length == 2){
                      writeNum(vc,v.product)
                    } else if(v.length == 1){
                      writeNum(vc,0)
                    }
                  }
                  case Right(ddf) => {
                    val v = ddf.flatMap{ d => d.get(vc) }
                    if(v.length == 2){
                      writeNum(vc,v.product)
                    } else if(v.length == 1){
                      writeNum(vc,0.0)
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
              def run(vc : SVcfOutputVariantLine){
                  val v = ddf.flatMap{ d => d.get(vc) }
                  if(v.length == 2){
                    writeNum(vc,v(0) / v(1))
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
              val dd = if(outType == "Integer"){
                Left(typeInfo.map{ case (tt,tp,pp,pv) => VcfTagFunctionParamReader_Int(pv,tt)})
              } else {
                Right(typeInfo.map{ case (tt,tp,pp,pv) => VcfTagFunctionParamReader_Float(pv,tt)})
              }
              def run(vc : SVcfOutputVariantLine){
                dd match {
                  case Left(ddi) => {
                    val v = ddi.map{ d => d.get(vc) }
                    if(v.forall(vv => vv.isDefined)){
                      writeNum(vc,v(0).get - v(1).get)
                    } else if(v(0).isDefined){
                      writeNum(vc,v(0).get)
                    } else if(v(1).isDefined){
                      writeNum(vc,-v(1).get)
                    }
                  }
                  case Right(ddf) => {
                    val v = ddf.map{ d => d.get(vc) }
                    if(v.forall(vv => vv.isDefined)){
                      writeNum(vc,v(0).get - v(1).get)
                    } else if(v(0).isDefined){
                      writeNum(vc,v(0).get)
                    } else if(v(1).isDefined){
                      writeNum(vc,-v(1).get)
                    }
                  }
                }
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
              def run(vc : SVcfOutputVariantLine){
                val v = ddi.map{ d => d.get(vc) }.zip(ppn).filter{ case (d,n) => d.map{ _ == 1 }.getOrElse(false) }.map{ _._2 }.padTo(1,".").mkString(",")
                writeString(vc,v);
              }
            }
          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "SETS.INTERSECT",synon = Seq(),
              shortDesc = "",
              desc = "Input should be a series of Info fields, specified as INFO:tagID, or set constants delimited with colons. "+
                     "Output field will be a comma delimited string containing the intersect between the supplied sets.",
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
              override val outType = "String";
              override val outNum = ".";
              val dds : Vector[VcfTagFunctionParamReader[Vector[String]]] = typeInfo.map{ case (tt,tp,pp,pv) => VcfTagFunctionParamReader_StringSeq(pv,tt)}
              def run(vc : SVcfOutputVariantLine){
                val out = dds.tail.foldLeft(dds.head.get(vc).getOrElse(Vector()).toSet ){ case (soFar,curr) => {
                  soFar.intersect( curr.get(vc).getOrElse(Vector()).toSet )
                }}.toVector.sorted.mkString(",")
                writeString(vc,out)
              }
            }
          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "SETS.UNION",synon = Seq(),
              shortDesc = "",
              desc = "",
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
              
                reportln("SETS.UNION.run: ","deepDebug");
                reportln("  SETS.UNION.typeInfo: ","deepDebug");
                typeInfo.foreach{ case (tt,tp,pp,pv) => {
                   reportln("  ["+tt+","+tp+","+pp.id+"/"+pp.ty+","+pv+"] ","deepDebug");
                }}

              override val outType = "String";
              override val outNum = ".";
              val dds : Vector[VcfTagFunctionParamReader[Vector[String]]] = typeInfo.map{ case (tt,tp,pp,pv) => VcfTagFunctionParamReader_StringSeq(pv,tt)}
              def run(vc : SVcfOutputVariantLine){
                
                val out = dds.foldLeft(Set[String]() ){ case (soFar,curr) => {
                  soFar ++ curr.get(vc).getOrElse(Vector()).toSet
                }}.toVector.sorted.mkString(",")
                reportln("SETS.UNION.run:"+out,"deepDebug");

                writeString(vc,out)
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
              def run(vc : SVcfOutputVariantLine){
                dd match {
                  case Left(ddi) => {
                    val v = ddi.flatMap{ d => d.get(vc).getOrElse(Vector()) }
                    if(v.length > 0){
                      writeNum(vc,v.min)
                    }
                  }
                  case Right(ddf) => {
                    val v = ddf.flatMap{ d => d.get(vc).getOrElse(Vector()) }
                    if(v.length > 0){
                      writeNum(vc,v.min)
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
              def run(vc : SVcfOutputVariantLine){
                dd match {
                  case Left(ddi) => {
                    val v = ddi.flatMap{ d => d.get(vc).getOrElse(Vector()) }
                    if(v.length > 0){
                      writeNum(vc,v.max)
                    }
                  }
                  case Right(ddf) => {
                    val v = ddf.flatMap{ d => d.get(vc).getOrElse(Vector()) }
                    if(v.length > 0){
                      writeNum(vc,v.max)
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
              def run(vc : SVcfOutputVariantLine){
                val out = if(rand.nextDouble() < thresh ){
                  writeNum(vc,1)
                } else {
                  writeNum(vc,0)
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
                  VcfTagFunctionParam( id = "oldField", ty = "INFO_Float|INFO_Double|INFO_String",req=true,dotdot=false ),
                  VcfTagFunctionParam( id = "newField", ty = "String",req=true,dotdot=false )
              )
          );
          def metadata = mmd;
          def gen(paramValues : Seq[String], outHeader: SVcfHeader, newTag : String, digits : Option[Int] = None) : VcfTagFcn = {
            new VcfTagFcn(){
              def h = outHeader; def pv : Seq[String] = paramValues; def dgts : Option[Int] = digits; def md : VcfTagFcnMetadata = mmd; def tag = newTag;
              def init : Boolean = true;
              val typeInfo = getTypeInfo(md.params,pv,h)
              val oldField = pv(0);
              val newField = pv(1);
              override val outType = h.infoLines.find{ _.ID == oldField }.map{ _.Type }.getOrElse("???")
              override val outNum = h.infoLines.find{ _.ID == oldField }.map{ _.Number }.getOrElse("???")
              val thresh = string2double(pv.head);
              def run(vc : SVcfOutputVariantLine){
                vc.info.getOrElse(oldField,None).foreach{ vv => writeString(vc,vv) }
              }
            }

          }
        },/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        new VcfTagFcnFactory(){
          val mmd =  new VcfTagFcnMetadata(
              id = "EXPR",synon = Seq(),
              shortDesc = "",
              desc = "",
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
              def run(vc : SVcfOutputVariantLine){
                val k = if( filter.keep(vc) ){
                  "1"
                } else {
                  "0"
                }
                notice(newTag+"="+k+" tagged for variant:\n    "+vc.getSimpleVcfString(),"TAGGED_"+newTag+"_"+k,1);
                writeString(vc,k);
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
        fcn.run(vc);
        vc;
      }}, closeAction = (() => {
        
      })),outHeader)
    }
  }
  
}









 


















