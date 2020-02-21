package internalUtils

import internalUtils.Reporter._;

/*
 * Contained below are various misc shortcut methods.
 * 
 * Author - Stephen Hartley
 */

object stdUtils {

  //def levenshteinDistance(a : String, b : String) : Int = {
  //  def min3(i1: Int, i2: Int, i3:Int) = math.min(math.min(i1,i2),i3);
  //  
  //}
  
//object ParamStrType extends Enumeration {
//  type ParamStrType = Value
//  val psString, psInt, psDouble, psFlag = Value
//}
  /*
  def getParamStrSet( s : String , , delim = "[|]", innerDelim = "[=]") : Seq[String] = {
    ParamStrSet(s.split(delim).toSeq.map{ ss => {
      val sc = ss.split(innerDelim);
      val ps = 
    }})
  }
  
  
  def getParamStr(s : String, pss : ParamStrSet) : String = {
    val ps = pss.
  }*/
  
  abstract class Threither[A,B,C] extends AnyRef {
    def isLeft : Boolean = false;
    def isRight : Boolean = false;
    def isMid : Boolean = false;
  }
  case class TLeft[A,B,C](x : A) extends Threither[A,B,C] {
    override def isLeft : Boolean = true;
  }
  case class TRight[A,B,C](x : B) extends Threither[A,B,C] {
    override def isRight : Boolean = true;
  }
  case class TMid[A,B,C](x : C) extends Threither[A,B,C] {
    override def isMid : Boolean = true;
  }
  
  
  
  
  //Types: Flag, String, Int, Double
  case class ParamStr( id : String, 
                       synon: Seq[String], 
                       ty : String,
                       defaultValue : Option[String] = None,
                       valueString : String = "x",
                       desc : String = "no description provided",
                       req : Boolean = false,
                       initParam : Boolean = false,
                       hidden : Boolean = false,
                       defaultDesc : Option[String] = None
                       ){
    
  }
  case class ParamStrSet(mapType : String, desc : String, pp : Seq[ParamStr], synon : Seq[String] = Seq[String]() ) {
    val pm = pp.map{ ppp => (ppp.id,ppp) }.toMap;
    val nameSetHolder = ( Set(mapType) ++ synon.toSet );
    def paramMap = pm;
    def nameSet : Set[String] = nameSetHolder
    def isMatch(s : String) : Boolean = nameSetHolder.contains(s);
    
  }
  case class ParsedParamStrSet( sc : Seq[String], pss : ParamStrSet, delim : String = "[|]", innerDelim : String = "[=]"){
    val paramMap = pss.paramMap;
    //val sc = ss.split(delim).toSeq;
    var rawParams = sc.map{ cell => {
       val x= cell.split(innerDelim,2);
       (x.head.trim(), x.lift(1).map{ _.trim() } )
    }}.map{ case ( paramID, paramValue ) => {
      if(! paramMap.contains(paramID)){
        error("Parameter \""+paramID+"\" not found! legal params are: "+paramMap.map{ _._2.id }.mkString(",")  );
      }
      val prm = paramMap(paramID);
      paramValue match {
        case Some(pv) => {
          (paramID,(prm,Some(pv)))
        }
        case None => {
          if(prm.ty == "Flag"){
            (paramID,(prm,Some("1")))
          } else {
            error("ERROR: param \""+paramID+"\" is not of type \"Flag\"")
            (paramID,(prm,None))
          }
        }
      }
    }}.toMap
    var params : Map[String,(ParamStr,Option[String],Boolean)] = Map[String,(ParamStr,Option[String],Boolean)]();
    var paramsHaveBeenSet = false;
    def setDefaults() {
      setDefaults(defaultParamKV = Map[String,String]())
    }
    //defaults added as a defaultParamKV are counted as SET for the purposes of required params!
    def setDefaults(defaultParamKV : Map[String,String]) {
      params = pss.pp.map{ ps => {
        rawParams.get(ps.id) match {
          case Some((a,b)) => {
            (ps.id,(a,b,true))
          }
          case None => {
            defaultParamKV.get(ps.id) match {
              case Some(dpv) => {
                (ps.id,(ps,Some(dpv),true));
              }
              case None => {
                (ps.id,(ps,ps.defaultValue,false))
              }
            }
          }
        }
      }}.toMap;
      params.filter{ case (paramid,(ps,pv,ss)) => {
        ps.req && (! ss)
      }}.foreach{ case (paramid,(ps,pv,isSet)) => {
        error("ERROR: parameter "+paramid+" is REQUIRED for function "+pss.mapType + 
                           "\n     Set params: ["+params.filter{ case (pid,(ps,pv,ss)) => ss }.map{ case (pid,(ps,pv,ss)) => pid }.mkString(",")+"] "+
                           "\n     avail params: ["+pss.pp.map{p => p.id}.mkString(",")+"]"+
                           "\n     paramMap: ["+params.map{ case (a,b) => a+":"+b}.mkString(",")+"]");
      }}
      
      
      paramsHaveBeenSet = true;
    }
    def getFlag(pid : String) : Boolean = {
      rawParams.contains(pid);
    }
    def set(paramID : String, paramValue : String){
      //reportln("Setting param: "+paramID+" for fcn "+pss.mapType,"debug")
      rawParams = rawParams.updated(paramID, ( pss.paramMap(paramID),Some(paramValue) )  )
    }
    def isSet(paramID : String) : Boolean = {
      params.get(paramID).map{ _._3}.getOrElse(false);
    }
    
      def get(pid : String): Option[String] = {
        if(! paramsHaveBeenSet) setDefaults();
        params(pid)._2;
      }
      def getOrElse(pid : String, ee : String) : String = {
        if(! paramsHaveBeenSet) setDefaults();
        params(pid)._2.getOrElse(ee);
      }
      def getOrDie(pid : String) : String = {
        get(pid).getOrElse({
          error("Mandatory field not set! "+pid);
          ".";
        })
      }
      def getOrDot(pid : String) : String   = {
        getOrElse(pid,".");
      }
      def apply(pid: String) : String = {
        getOrDie(pid);
      }

    /*def get(pid : String): Option[String] = params.get(pid).map{ case (prm,pv) => pv }.getOrElse(None);
      def getOrDef(pid : String) : String = params.get(pid).map{ case (prm,pv) => pv }.getOrElse(None).getOrElse(".");
      def getOrElse(pid : String, ee : String) : String = params.get(pid).map{ case (prm,pv) => pv }.getOrElse(None).getOrElse(ee);*/
  }
  
  
  
   def levenshtein(s1: String, s2: String) = {
      import scala.math._
      def minimum(i1: Int, i2: Int, i3: Int) = min(min(i1, i2), i3)
      val dist = Array.tabulate(s2.length + 1, s1.length + 1) { (j, i) => if (j == 0) i else if (i == 0) j else 0 }
      for (j <- 1 to s2.length; i <- 1 to s1.length)
         dist(j)(i) = if (s2(j - 1) == s1(i - 1)) dist(j - 1)(i - 1)
         else minimum(dist(j - 1)(i) + 1, dist(j)(i - 1) + 1, dist(j - 1)(i - 1) + 1)
      dist(s2.length)(s1.length)
   }
  
  /**************************************************************************************************************************
   * 
   **************************************************************************************************************************/
  lazy val SIMPLE_CHARSTRING_BYTE_MAP_CHARLIST : String = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_.abcdefghijklmnopqrstuvwxyz"
  lazy val SIMPLE_CHARSTRING_BYTE_MAP_CHARLIST_CHARPT : Array[Int] = SIMPLE_CHARSTRING_BYTE_MAP_CHARLIST.toArray.map{_.toInt};
  lazy val SIMPLE_CHARSTRING_BYTE_MAP_TOINT    : Array[Int]    = Range(0,255).toArray.map{i => {
    SIMPLE_CHARSTRING_BYTE_MAP_CHARLIST_CHARPT.zipWithIndex.find{ case (charInt,j) => {
      charInt == i;
    }}.map{ case (charInt,j) => j }.getOrElse(-1);
  }}
  lazy val SIMPLE_CHARSTRING_BYTE_MAP_TOSTRING : Array[String] = SIMPLE_CHARSTRING_BYTE_MAP_CHARLIST.toArray.map{_.toString}
  
  
  def decodeStringToBytes64(s : String) : Iterator[Int] = {
    s.iterator.map{ c => SIMPLE_CHARSTRING_BYTE_MAP_TOINT(c.toInt) }
  }
  
  def decodeStringToTwoBit(s : String) : Iterator[Int] = {
    s.iterator.flatMap{ c => {
      val bit64 = SIMPLE_CHARSTRING_BYTE_MAP_TOINT(c.toInt)
      Range(0,32).iterator.map{ i => {
        ((bit64 >> (2 * i)) & 0x3)
      }}
    }}
  }
  
  /*def encodeTwoBitToString(iter : Iterator[Int]) : Iterator[String] = {
    iter.flatMap{ c => {
      val bit64 = SIMPLE_CHARSTRING_BYTE_MAP_TOINT(c.toInt)
      Range(0,32).iterator.map{ i => {
        ((bit64 >> (2 * i)) & 0x3)
      }}
    }}
  }*/
  
  /**************************************************************************************************************************
   * Simple sugars:
   **************************************************************************************************************************/
  
  def getOrFunc[A,B](x : Option[A], orelse : B)(fun : (A) => B) : B = {
    x match {
      case Some(a) => {
        fun(a);
      }
      case None => {
        orelse;
      }
    }
  }
  
  /**************************************************************************************************************************
   * Math:
   **************************************************************************************************************************/
  
  def getAllPossiblePairs(n : Int, includeIdentityPair : Boolean = false) : Seq[(Int,Int)] = {
    if(includeIdentityPair){
      (0 until n).flatMap{ i => {
        (i until n).map{ j => {
          (i,j)
        }}
      }}
    } else {
      (0 until n).flatMap{ i => {
        (i+1 until n).map{ j => {
          (i,j)
        }}
      }}
    }
  }
  
  case class ReservoirSampler[A](K : Int, seed : Option[Int] = None)(implicit arg0: scala.reflect.ClassTag[A]){
    val reservoir : Array[A] = arg0.newArray(K);
    val rand : scala.util.Random = seed match {
      case Some(s) => new scala.util.Random(s);
      case None => new scala.util.Random();
    }
    var n : Int = 1;
    def sample(a : A){
      if(n <= K){
        reservoir(n-1) = a;
      } else {
        val r = rand.nextInt(n)+1;
        if(r <= K){
          reservoir(r-1) = a;
        }
      }
      n = n + 1;
    }
    def getSample : Array[A] = {
      reservoir.filter(_ != null);
    }
  }
  
  def getFirstDigitOfInt(x : Int) : Int = {
    val xs = x.toString();
    if(xs.head == '-'){
      string2int(xs.charAt(1).toString());
    } else {
      string2int(xs.head.toString());
    }
  }
  def getNumZeros(c : Int) : Int = { math.log10((c + 1).toDouble).ceil.toInt }

  /*
   * Utility Classes:
   */
  
  def addIteratorCloseAction[A](iter : Iterator[A], closeAction : (() => Unit)) : Iterator[A] = {
    new Iterator[A] {
      //var isOpen = true;
      def next = {
        val out = iter.next;
        if(! iter.hasNext){
          closeAction();
        }
        out
      }
      def hasNext = iter.hasNext;
    }
  }
  def addIteratorCloseAction[A](iter : Iterator[A], closeAction : ((A) => Unit)) : Iterator[A] = {
    new Iterator[A] {
      //var isOpen = true;
      def next = {
        val out = iter.next;
        if(! iter.hasNext){
          closeAction(out);
        }
        out
      }
      def hasNext = iter.hasNext;
    }
  }
  
  
  def addQuotesIfNeeded(s : String) : String = {
    if(s.length == 0){
      return "\"\""
    } else if(s.head == '\"' && s.last == '\"'){
      return s;
    } else {
      return "\"" + s + "\"";
    }
  }
  
  
  def trimBrackets(s : String) : String = {
    var out = s;
    if(s.length == 0) return out;
    if(s.head == '[') out = out.tail;
    if(s.length == 0) return out;
    if(s.last == ']') out = out.init;
    return out;
  }
  
  def generalizedTo(f : Int, t : Int) : Seq[Int] = {
    if(f == t) return (f until t);
    else if(f < t) return (f to t);
    else return Range(f, t-1, -1);
  }
  
  def getRandomString(len : Int) : String = {
    scala.util.Random.alphanumeric.slice(0,len).toVector.mkString("");
  }
  
  //splits delim characters, unless they appear between two double-quotes.
  // Does NOT properly deal with escaped-double-quotes.
  def splitRespectQuote(s : String, delim : String) : Array[String] = {
    s.split(delim+"(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
  }
  
  def parseTokens(s : String, delim : Char, strict : Boolean = false, quoteChar : Char = '"') : Array[String] = {
    var tokens : List[String] = List[String]();
    
    var inEscape : Boolean = false;
    var inQuote : Boolean = false;
    var buffer : String = "";
    
    for(c <- s){
      if(c == quoteChar){
        if(! inEscape){
          inQuote = ! inQuote;
        }
        buffer += c;
      } else if(inQuote){
        buffer += c;
      } else if(c == delim){
        tokens = tokens :+ buffer;
        buffer = "";
      } else {
        buffer += c;
      }
      
      if((! inEscape) && c == '\\'){
        inEscape = true;
      } else {
        inEscape = false;
      }
    }
    
    if(strict){
      if(inEscape){
        error("PARSING ERROR: excape character found at end of parse!");
      }
      if(inQuote){
        error("PARSING ERROR: unmatched quote!")
      }
    }
    
    return tokens.toArray;
  }
  
  
  /**************************************************************************************************************************
   * Specialty:
   **************************************************************************************************************************/
  def circularIterator[A]( lst : Seq[A] ) : Iterator[A] = {
    new Iterator[A] {
      var i = 0;
      def next : A = {
        if(i < lst.length){
          i = i + 1
          lst(i-1);
        } else {
          i = 1;
          lst(0);
        }
      }
      def hasNext = true;
    }
  }
  
  
  def calculateGeometricSizeFactorsForMatrix(m : Seq[Seq[Int]], verbose : Boolean = true) : Vector[Double] = {
    if(verbose) reportln(">   Calculating size factors","debug");
    
    val nrow = m.size;
    val ncol = m.head.size;
    if(m.exists(_.size != ncol)) error("FATAL ERROR in size factor calculations: count matrix does not have consistent dimensions.");
    
    if(verbose) reportln(">      nrow = "+nrow,"debug");
    if(verbose) reportln(">      ncol = "+ncol,"debug");

    val logGeoMeans : Vector[Double] = Range(0,ncol).toVector.map((j : Int) => {
      Range(0,nrow).toVector.map((i : Int) => {
        scala.math.log(m(i)(j));
      }).sum / nrow.toDouble;
    });
    //m.map( (x : Seq[Int]) => (x.map(scala.math.log(_)).sum / x.size) ).toVector;
    if(verbose) reportln(">      Calculated logGeoMeans.","debug");
    if(verbose) reportln(">      logGeoMeans.size = "+logGeoMeans.size,"debug");

    if(verbose) reportln(">      SF iteration:","debug");
    val sf : Vector[Double] = m.map((cts : Seq[Int]) => {
      val v = cts.zip(logGeoMeans).filter{
        case (ct : Int, lgm : Double) => 
          (ct > 0) && (! lgm.isInfinite)
      }.map{ 
        case (ct : Int, lgm : Double) => 
          scala.math.log(ct) - lgm
      }
      val med = scala.math.exp(median(v));
      if(verbose) reportln(">         v.size = "+v.size+", median = "+med,"debug");
      med;
    }).toVector;
    if(verbose) reportln(">      SF iteration done.","debug");
    if(verbose) reportln(">      sf.size = "+sf.size,"debug");

    if(verbose) reportln(">   Size factor calculation complete.","debug");

    return sf;
  } 
  
  def median(v : Seq[Double]) : Double = {
    val x = v.sorted;
    if(x.size == 0){
      return(Double.NaN);
    } else if(x.size % 2 == 0){
      (x((x.size/2) - 1) + x(x.size/2)) / 2.0;
    } else {
      x( x.size / 2 );
    }
  }
  
  /**************************************************************************************************************************
   * timestamp / memory-usage utilities
   **************************************************************************************************************************/
  
  def standardStatusReport(initialTimeStamp : TimeStampUtil, lineStart : String = "", verbosity : String = "note", printOnThreeLines : Boolean = false) {
    if(printOnThreeLines){
      standardStatusReport_threeLine(initialTimeStamp,lineStart, verbosity);
    } else {
      standardStatusReport_singleLine(initialTimeStamp,lineStart, verbosity);
    }
  }
  def standardStatusReport_threeLine(initialTimeStamp : TimeStampUtil, lineStart : String = "", verbosity : String = "note") {
    val currTime = TimeStampUtil();
    
    val rawLineSeq : Seq[(String,String)] = Seq[(String,String)]( 
        (lineStart + "[Time:",          currTime.toString + "]"),
        (lineStart + "[Mem usage:",     MemoryUtil.memInfo +"]"),
        (lineStart + "[Elapsed Time:",  TimeStampUtil.timeDifferenceFormatter(currTime.compareTo(initialTimeStamp)) +"]")
    );
    val fmtLineSeq : Seq[String] = leftRightJustifySeq(rawLineSeq);
    
    fmtLineSeq.foreach((str) =>{
      reportln(str,verbosity);
    });
  }
  def standardStatusReport_singleLine(initialTimeStamp : TimeStampUtil, lineStart : String = "", verbosity : String = "note") {
    val currTime = TimeStampUtil();
    
    reportln(lineStart + "[Time: " + currTime.toString + "] [Mem usage: " + MemoryUtil.memInfo + "] [Elapsed Time: "+ TimeStampUtil.timeDifferenceFormatter(currTime.compareTo(initialTimeStamp)) +"]",
             verbosity);
  }
  
  val STANDARD_DATE_AND_TIME_FORMATTER = (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
  
  def getDateAndTimeString : String = {
    return STANDARD_DATE_AND_TIME_FORMATTER.format(java.util.Calendar.getInstance().getTime());
  }
  def getDateAndTimeStringFromDate(d : java.util.Date) : String = {
    return STANDARD_DATE_AND_TIME_FORMATTER.format(d);
  }
  def nanoTimeDifferenceFormat(nanos : Long, verbose : Boolean = false) : String = {
      val time = nanos / 1000000;
      val millis_col = zeroPad( (time % 1000).toInt , 4);
      val secs = time / 1000;
      val secs_col = zeroPad( (secs % 60).toInt , 2);
      val mins = secs / 60;
      val mins_col = zeroPad( (mins % 60).toInt , 2);
      val hours = mins / 60;
      val hours_col = zeroPad( (hours % 24).toInt , 2);
      val days = hours / 24;
      
      if(days > 0 && verbose) return ""+days+" days "+hours_col+":"+mins_col+":"+secs_col+"."+millis_col;
      else ""+hours_col+":"+mins_col+":"+secs_col+"."+millis_col;
  }
  
   //TimeStampUtil.timeDifferenceFormatter(time)
  object TimeStampUtil {
    def apply() : TimeStampUtil = new TimeStampUtil(java.util.Calendar.getInstance.getTimeInMillis());
    
    def timeDifferenceFormatterNanos(time : Long) : String = {
      timeDifferenceFormatter(time / 1000000);
    }
    
    def timeDifferenceFormatter(time : Long) : String = {
      val millis_col = zeroPad( (time % 1000).toInt , 4);
      val secs = time / 1000;
      val secs_col = zeroPad( (secs % 60).toInt , 2);
      val mins = secs / 60;
      val mins_col = zeroPad( (mins % 60).toInt , 2);
      val hours = mins / 60;
      val hours_col = zeroPad( (hours % 24).toInt , 2);
      val days = hours / 24;
      
      if(days > 0) return ""+days+" days "+hours_col+":"+mins_col+":"+secs_col+"."+millis_col;
      else ""+hours_col+":"+mins_col+":"+secs_col+"."+millis_col;
    }
  }
  case class TimeStampUtil(ts : Long){
    def compareTo(b : TimeStampUtil) : Long = this.ts - b.ts;
    override def toString : String = {
      val date = new java.util.Date(ts);
      STANDARD_DATE_AND_TIME_FORMATTER.format(date);
    }
  }
  
  object MemoryUtil {
    def totalMem : Long = {
      return Runtime.getRuntime().totalMemory();
    }
    def freeMem : Long = {
      return Runtime.getRuntime().freeMemory();
    }
    def usedMem : Long = {
      return totalMem - freeMem;
    }
    
    def memInfo : String = {
      //val numFormatter : java.text.NumberFormat = java.text.NumberFormat.getInstance();
      return "[" + formatByteCount(usedMem) + " / " + formatByteCount(totalMem) + "]";
    }
  }
  
  def getMaxMemoryXmxInGigs : Double = {
    Runtime.getRuntime().maxMemory().toDouble / 1000000000.00;
  }
  
  /**************************************************************************************************************************
   * Sequence/iterator utilities:
   **************************************************************************************************************************/
  
  object RevIntOrdering extends Ordering[Int] {
    def compare(x: Int, y: Int) = java.lang.Integer.compare(x, y)
  }
  
  /*class IntTreeMapOfSets[B] extends scala.collection.immutable.TreeMap[Int,Set[B]] {
    def addPair(k : Int, v :B) : IntTreeMapOfSets[B] = {
      if(this.contains(k)){
        return this + ((k,this(k) + v))
      } else {
        return this + ((k,Set[B](v)))
      }
    }
  }*/
  def bufferedGroupedResortingByGroupUnsort[A](iter : Iterator[A], bufferSize : Int)(f : (A => Int))(g : (A => String)) : Iterator[Seq[A]] = {
    if(! iter.hasNext){
      return Iterator[Seq[A]]();
    }
    val itr = iter.buffered;

    return new Iterator[Seq[A]] {
      var buffer = new scala.collection.immutable.TreeMap[Int,Set[A]]();
      //var subBuffer : Iterator[A] = Iterator[A]();
      def addPair(k : Int, v : A){
        if(buffer.contains(k)){
          buffer = buffer + ((k,buffer(k) + (v)))
        } else {
          buffer = buffer + ((k,Set[A](v)))
        }
      }
      //var currPos : Int = buffer.head._1;
      var currGrp : String = g(itr.head);
      //var fitr : Iterator[A] = Iterator[A]();
      def hasNext : Boolean = itr.hasNext || buffer.size > 0;
      def next : Seq[A] = {
        //if(buffer.size > 0 && buffer.head._1 < buffer.last._1 - bufferSize){
        if(buffer.size > 0 && (( buffer.head._1 < buffer.last._1 - bufferSize ) || ( ! itr.hasNext ) || (g(itr.head) != currGrp))){
          //val (out,keep) = buffer.span{ case (pp,x) => { pp < buffer.last._1 - bufferSize }}
          val (pos,out) = buffer.head;
          buffer = buffer.tail;
          out.toVector
        } else if(itr.hasNext && g(itr.head) == currGrp){
          val n = itr.next;
          addPair(f(n),n);
          next;
        } else { // itr.hasNext must be true
          val n = itr.next;
          currGrp = g(n);
          addPair(f(n),n);
          next;
        }
      }
    }
  }
  

  def bufferedGroupedResortingByGroup[A,B](iter : Iterator[A], bufferSize : Int)(f : (A => Int))(g : (A => String))(h : (A => B))(implicit ord : math.Ordering[B]) : Iterator[Seq[A]] = {
    if(! iter.hasNext){
      return Iterator[Seq[A]]();
    }
    val itr = iter.buffered;

    return new Iterator[Seq[A]] {
      var buffer = new scala.collection.immutable.TreeMap[Int,Set[A]]();
      //var subBuffer : Iterator[A] = Iterator[A]();
      def addPair(k : Int, v : A){
        if(buffer.contains(k)){
          buffer = buffer + ((k,buffer(k) + (v)))
        } else {
          buffer = buffer + ((k,Set[A](v)))
        }
      }
      //var currPos : Int = buffer.head._1;
      var currGrp : String = g(itr.head);
      //var fitr : Iterator[A] = Iterator[A]();
      def hasNext : Boolean = itr.hasNext || buffer.size > 0;
      def next : Seq[A] = {
        //if(buffer.size > 0 && buffer.head._1 < buffer.last._1 - bufferSize){
        if(buffer.size > 0 && (( buffer.head._1 < buffer.last._1 - bufferSize ) || ( ! itr.hasNext ) || (g(itr.head) != currGrp))){
          //val (out,keep) = buffer.span{ case (pp,x) => { pp < buffer.last._1 - bufferSize }}
          val (pos,out) = buffer.head;
          buffer = buffer.tail;
          out.toVector.sortBy(h)(ord);
        } else if(itr.hasNext && g(itr.head) == currGrp){
          val n = itr.next;
          addPair(f(n),n);
          next;
        } else { // itr.hasNext must be true
          val n = itr.next;
          currGrp = g(n);
          addPair(f(n),n);
          next;
        }
      }
    }
  }
  
  
  def bufferedGroupedResorting[A,B](iter : Iterator[A], bufferSize : Int)(f : (A => Int))(g : (A => String))(h : (A => B))(implicit ord : math.Ordering[B]) : Iterator[A] = {
    if(! iter.hasNext){
      return Iterator[A]();
    }
    val itr = iter.buffered;

    return new Iterator[A] {
      var buffer = new scala.collection.immutable.TreeMap[Int,Vector[A]]();
      var subBuffer : Iterator[A] = Iterator[A]();
      def addPair(k : Int, v : A){
        if(buffer.contains(k)){
          buffer = buffer + ((k,buffer(k) :+ (v)))
        } else {
          buffer = buffer + ((k,Vector[A](v)))
        }
      }
      //var currPos : Int = buffer.head._1;
      var currGrp : String = g(itr.head);
      //var fitr : Iterator[A] = Iterator[A]();
      def hasNext : Boolean = itr.hasNext || subBuffer.hasNext || buffer.size > 0;
      def next : A = {
        if(subBuffer.hasNext){
          subBuffer.next;
        } else if(buffer.size > 0 && buffer.head._1 < buffer.last._1 - bufferSize){
          val (out,keep) = buffer.span{ case (pp,x) => { pp < buffer.last._1 - bufferSize }}
          buffer = keep;
          subBuffer = out.unzip._2.map{x => x.toVector.sortBy(h)(ord)}.flatten.iterator;
          next;
        } else if(itr.hasNext && g(itr.head) == currGrp){
          val n = itr.next;
          addPair(f(n),n);
          next;
        } else if(buffer.size > 0){
          val (pos,vcSet) = buffer.head;
          buffer = buffer.tail;
          subBuffer = vcSet.toVector.sortBy(h)(ord).iterator;
          next;
        } else { // itr.hasNext must be true
          val n = itr.next;
          currGrp = g(n);
          addPair(f(n),n);
          next;
        }
      }
    }
    
    /*
    val ritr = itr.flatMap{ ii => {
      val pos = f(ii);
      buffer = addPair(pos,ii);
      //buffer = buffer + ((pos,ii));
      //buffer.addPair(itr.head._1,itr.next._2);
      val lastPos = buffer.last._1;
      if(buffer.head._1 >= buffer.last._1 - bufferSize){
        None;
      } else {
        val (out,keep) = buffer.span{ case (pp,x) => { pp < lastPos - bufferSize }}
        buffer = keep
        out.unzip._2.map{x => x.toVector}.flatten;
      }
    }}
    
    return new Iterator[A] {
      var fitr : Option[Iterator[A]] = None;
      def hasNext : Boolean = if(ritr.hasNext){
        true
      } else {
        if(fitr.isEmpty){
          fitr = Some(buffer.unzip._2.map{x => x.toVector}.flatten.iterator)
        }
        fitr.get.hasNext
      }
      def next : A = {
        if(ritr.hasNext) ritr.next;
        else {
          if(fitr.isEmpty){
            fitr = Some(buffer.unzip._2.map{x => x.toVector}.flatten.iterator)
          }
          fitr.get.next;
        }
      }
    }*/
    
  }
  
  
  
  def groupBySpan_OLD[A,B](iter : Iterator[A])(f : (A => B)) : Iterator[Vector[A]] = {
    var currIter = iter;
    
    return new Iterator[Vector[A]]{
      def hasNext : Boolean = currIter.hasNext;
      def next : Vector[A] = {
        val nextA = currIter.next;
        val nextB = f(nextA);
        val (curr,remain) = currIter.span(c => f(c) == nextB);
        val out = nextA +: curr.toVector;
        currIter = remain;
        out;
      }
    }
  }
  

  def mergeGroupBySpanWithSupergroup[A,B,C](iterSeq : Seq[BufferedIterator[A]])(g : (A => C))(f : (A => B))(implicit ord : math.Ordering[B]) : Iterator[Seq[Seq[A]]] = {
    //var currIter = iter;
    return new Iterator[Seq[Seq[A]]]{
      val groupedIter : Seq[BufferedIterator[Seq[A]]] = iterSeq.map{ii => groupBySpan(ii)(f).buffered};
      var currG : C = g(iterSeq.find(ii => ii.hasNext).get.head);
      
      
      def hasNext : Boolean = iterSeq.exists{_.hasNext};
      def next : Seq[Seq[A]] = {
        if( groupedIter.exists{ ii => {
          ii.hasNext && g(ii.head.head) == currG
        }}) {
          val currMin : B = groupedIter.flatMap{ ii => {
            if(ii.hasNext && g(ii.head.head) == currG){
              Some(f(ii.head.head))
            } else {
              None
            }
          }}.min(ord)
          groupedIter.map{ ii => {
            if(ii.hasNext){
              if(f(ii.head.head) == currMin){
                ii.next;
              } else {
                Seq[A]();
              }
            } else {
              Seq[A]();
            }
          }}
        } else {
          currG = g(iterSeq.find(ii => ii.hasNext).get.head);
          next;
        }
        
        //val nextA = currIter.next;
        //val nextB = f(iter.head);
        //extractWhile(iter)(a => f(a) == nextB)
      }
    }
  }
  
  
  def mergeGroupBySpan[A,B](iterSeq : Seq[BufferedIterator[A]])(f : (A => B))(implicit ord : math.Ordering[B]) : Iterator[Seq[Seq[A]]] = {
    //var currIter = iter;
    return new Iterator[Seq[Seq[A]]]{
      val groupedIter : Seq[BufferedIterator[Seq[A]]] = iterSeq.map{ii => groupBySpan(ii)(f).buffered};
      
      def hasNext : Boolean = iterSeq.exists{_.hasNext};
      def next : Seq[Seq[A]] = {
        val currMin : B = groupedIter.flatMap{ ii => {
          if(ii.hasNext){
            Some(f(ii.head.head))
          } else {
            None
          }
        }}.min(ord)
        groupedIter.map{ ii => {
          if(ii.hasNext){
            if(f(ii.head.head) == currMin){
              ii.next;
            } else {
              Seq[A]();
            }
          } else {
            Seq[A]();
          }
        }}
        //val nextA = currIter.next;
        //val nextB = f(iter.head);
        //extractWhile(iter)(a => f(a) == nextB)
      }
    }
  }
  
  /*
   * Not sure I like the batchiterator?
   */
  /*
  class BatchIterator[A]{
    def hasNext : Boolean;
    def next : A;
    def hasNextBlock : Boolean;
    def nextBlock;
  }
  def makeBatchIteratorByBreakpoints[A]( inputIter : BufferedIterator[A])(f : (A => Boolean) ) : BatchIterator[(A,Int)] = {
    val iter = inputIter.buffered;
    new BatchIterator[(A,Int)]{
      var ii = 0;
      def hasNext : Boolean = {
        iter.hasNext && (! f( iter.head ));
      }
      def next : (A,Int) = {
        ii = ii + 1;
        (iter.next,ii-1);
      }
      def hasNextBlock : Boolean = {
        iter.hasNext
      }
      def nextBlock{
        ii = 0;
        (iter.next,0);
      }
    }
  }*/
  
  
  def groupBySpan[A,B](iter : BufferedIterator[A])(f : (A => B)) : Iterator[Seq[A]] = {
    //var currIter = iter;
    if(! iter.hasNext){
      return Iterator[Seq[A]]();
    }
    return new Iterator[Seq[A]]{
      def hasNext : Boolean = iter.hasNext;
      def next : Seq[A] = {
        //val nextA = currIter.next;
        val nextB = f(iter.head);
        extractWhile(iter)(a => f(a) == nextB)
      }
    }
  }
  
  def iterHeadOption[A](iter : BufferedIterator[A]) : Option[A] = {
    if(iter.hasNext) Some(iter.head);
    else None;
  }
  
  def groupBySpanMulti[A,B,C](iterSeq : Seq[BufferedIterator[A]])(g : A => C)(f : (A => B))(implicit ord : math.Ordering[B], ordG : math.Ordering[C]) : Iterator[Seq[Seq[A]]] = {
    if( iterSeq.exists(iter => iter.hasNext) ){
      return new Iterator[Seq[Seq[A]]]{
        var currG : C = iterSeq.flatMap(iter => iterHeadOption(iter)).map{g}.min(ordG);
        def hasNext : Boolean = iterSeq.exists{ iter => { iter.hasNext }};
        def next : Seq[Seq[A]] = {
          //val nextA = currIter.next;
          //val nextB = f(iter.head);
          //extractWhile(iter)(a => f(a) == nextB)
          if( iterSeq.exists{ iter => iterHeadOption(iter).exists{g(_) == currG} } ){
            val nextB : B = iterSeq.flatMap{iter => if((!iter.hasNext) || (g(iter.head) != currG)){None} else Some(f(iter.head))}.min(ord);
            iterSeq.map{ iter => extractWhile(iter)(a => g(a) == currG && f(a) == nextB) }
          } else {
            val nextGset = iterSeq.flatMap{iter => iterHeadOption(iter).map{g}}.distinct;
            if(nextGset.length != 1){
              warning("WARNING WARNING WARNING: groupBySpanMulti has found multiple possibilities for the next group: \""+nextGset.mkString("\",\"")+"\" appears to follow: "+currG+". Assuming that "+nextGset.min+" is next. Is your input data sorted properly?","CRITICAL_WARNING_BAD_ITERATOR_ORDERING",100)
            }
            currG = nextGset.min;
            next;
          }

        }
      }
    } else {
      Iterator[Seq[Seq[A]]]();
    }
  }
  
  
  def extractWhile[A](iter : BufferedIterator[A])(f : (A => Boolean)) : Vector[A] = {
      var out = scala.collection.mutable.Queue[A]();
      while(iter.hasNext && f(iter.head)){
        out += iter.next;
      }
      return out.toVector;
  }
  def skipWhile[A](iter : BufferedIterator[A])(f : (A => Boolean)){
    while(iter.hasNext && f(iter.head)){
      iter.next;
    }
  }
  def skipWhileWithProgress[A](iter : BufferedIterator[A], targetSec : Int = 60)(f : (A => Boolean), reportFunc : (A => String) = (a : A) => ""){
    var iterCt = 0;
    var dotCt = -1;
    val startTime = System.nanoTime();
           // val elapsedSec = (currTime - lastLineTime) / 1000000000;
    while(iter.hasNext && f(iter.head)){
      iterCt += 1;
      val n = iter.next;
      if(dotCt == -1 && (System.nanoTime() - startTime) / 1000000000 > targetSec){
        dotCt = iterCt
        report("(dot="+dotCt+") [.","progress");
      } else if(dotCt != -1 && iterCt % dotCt == 0){
        if(iterCt % (dotCt * 5) == 0){
          report(".]"+reportFunc(n)+"[","progress");
        } else {
          report(".","progress");
        }
      }
      
    }
    report("]\n","progress");
  }
  
  def skipWhileCt[A](iter : BufferedIterator[A])(f : (A => Boolean)) : Int = {
    var ct = 0;
    while(iter.hasNext && f(iter.head)){
      ct = ct + 1;
      iter.next;
    }
    ct;
  }
  
  
  def flattenIterators[A](ii : Iterator[Iterator[A]]) : Iterator[A] = {
    val iif = ii.filter(i => i.hasNext);
    if(! iif.hasNext){
      return Iterator[A]();
    } else {
      var currIter = iif.next;
      return new Iterator[A]{
        def hasNext : Boolean = currIter.hasNext
        def next : A = {
          val next = currIter.next;
          if((! currIter.hasNext) && iif.hasNext){
            currIter = iif.next;
          }
          next;
        }
      }
    }
  }
  
  abstract class BenchmarkIterator[A] extends Iterator[A] {
    var ns : Long = 0;
    var i = 0;
    def next : A;
    def hasNext : Boolean;
    //def head : A;
    def iterCt : Int = i;
    def nanosElapsed : Long = ns
    def reset(){
        ns = 0;
        i = 0;
    }
    def getStatusString() : String = {
      "["+i+"]/["+nanoTimeDifferenceFormat(ns)+"]"
    }
  }
  
  def benchMap[A,B](iter : Iterator[A])(f : A => B) : BenchmarkIterator[B] = {
    new BenchmarkIterator[B] {

      def next : B = {
        i += 1;
        val nInput = iter.next;
        val t0 = System.nanoTime();
        val n = f(nInput);
        val t1 = System.nanoTime();
        ns += (t1 - t0)
        n;
      }
      def hasNext : Boolean = iter.hasNext;

    }
  }
  val EmptyIterator : Iterator[Nothing] = new Iterator[Nothing]{
        def hasNext: Boolean = false
        def next(): Nothing = throw new NoSuchElementException("next on empty iterator")
  }

  def benchFlatMap[A,B](iter : Iterator[A])(f : A => scala.collection.GenTraversableOnce[B]) : BenchmarkIterator[B] = {
    new BenchmarkIterator[B] {
      var nsGrp : Long = 0;
      private var cur : Iterator[B] = EmptyIterator
      //private def nextCur() { cur = f(iter.next()).toIterator }
      private def nextCur(){
        i += 1;
        val nInput = iter.next;
        val t0 = System.nanoTime();
        val n = f(nInput).toIterator;
        val t1 = System.nanoTime();
        ns += (t1 - t0)
        nsGrp += (t1 - t0);
        cur = n;
      }
      def hasNext: Boolean = {
        // Equivalent to cur.hasNext || self.hasNext && { nextCur(); hasNext }
        // but slightly shorter bytecode (better JVM inlining!)
        while(!cur.hasNext) {
          if(! iter.hasNext ) return false
          nextCur()
        }
        true
      }
      def next(): B = {
        if(hasNext){
          val t0 = System.nanoTime();
          val n = cur.next();
          val t1 = System.nanoTime();
          ns += (t1 - t0)
          n;
        } else {
          throw new NoSuchElementException("next on empty iterator")
        }
      }
      override def reset(){
        ns = 0;
        i = 0;
        nsGrp = 0;
      }
      override def getStatusString() : String = {
        "["+i+"]/["+nanoTimeDifferenceFormat(ns)+"]["+nanoTimeDifferenceFormat(nsGrp)+"/"+nanoTimeDifferenceFormat(ns-nsGrp)+"]"
      }
    }
  }
  
  
  
  /*def groupBySpan_OLD[A,B](iter : Iterator[A])(f : (A => B)) : Iterator[Seq[A]] = {
    var currIter = iter;
    
    return new Iterator[Seq[A]]{
      def hasNext : Boolean = currIter.hasNext;
      def next : Seq[A] = {
        val nextA = currIter.next;
        val nextB = f(nextA);
        val (curr,remain) = spanVector(currIter)(c => f(c) == nextB);
        val out = nextA +: curr;
        currIter = remain;
        out;
      }
    }
  }*/
  def spanVector_OLD[A](iter : Iterator[A])(f : (A => Boolean)) : (Vector[A],Iterator[A]) = {
    if(iter.hasNext){
      var out = Vector[A]();
      var curr = iter.next;
      if(f(curr)){
          out = out :+ curr;
      }
      while(iter.hasNext && f(curr)){
        curr = iter.next;
        if(f(curr)){
          out = out :+ curr;
        }
      }
      if(f(curr)){
        return (out,iter);
      } else {
        return (out,Iterator(curr) ++ iter);
      }
    } else {
      return (Vector[A](),iter);
    }
  }
  
  object AlphabetOrderingChar extends Ordering[Char] {
    def compare(x : Char, y : Char) : Int = {
      if(x == y) {
        return 0;
      } else if(x.toUpper == y.toUpper){
        if(x.isUpper) return -1;
        else return 1;
      } else {
        scala.math.Ordering.Char.compare(x,y);
      }
    }
  }
  
  object AlphabetOrdering extends Ordering[String] {
    def compare(x : String, y : String) : Int = {
      if(x == y){
        return 0;
      } else {
        x.toSeq.zip(y.toSeq).find{ case (a,b) => {
          AlphabetOrderingChar.compare(a,b) != 0;
        }} match {
          case Some((a,b)) => {
            return AlphabetOrderingChar.compare(a,b);
          }
          case None => {
            if(x.length < y.length) return -1;
            else return 1;
          }
        }
      }
    }
  }
  
  def peekIterator[A](iter : Iterator[A]) : (A, Iterator[A]) = {
    val peek : A = iter.next();
    return ((peek, Iterator[A](peek) ++ iter  ));
  }
  
  /*def peekIterator[A](iter : Iterator[A], peekCt : Int) : (Vector[A], Iterator[A]) = {
    var peek : Vector[A] = Vector[A]();
    var i : Int = 0;
    while(iter.hasNext && i < peekCt){
      peek = peek :+ iter.next;
      i += 1;
    }
    return (( peek, peek.iterator ++ iter));
  }*/

  def peekIterator[B](iter : Iterator[B], peekCt : Int) : (Vector[B],Iterator[B]) = {
    var soFar : Vector[B] = Vector[B]();
    for(i <- Range(0,peekCt)){
      if(iter.hasNext){
        soFar = soFar :+ iter.next;
      } else {
        return (soFar, soFar.iterator);
      }
    }
    return (soFar, soFar.iterator ++ iter);
  }
  
  def splitIterator[A](iter : Iterator[A], p : (A) => Boolean) : (Vector[A], Iterator[A]) = {
    var peek : Vector[A] = Vector[A]();
    var itr : Iterator[A] = iter;
    if(! iter.hasNext) return ((Vector[A](), Iterator[A]()));
    var curr = iter.next;
    
    while(itr.hasNext && p(curr)){
      peek = peek :+ curr;
      curr = itr.next;
    }
    if(p(curr)){
      peek = peek :+ curr;
    } else {
      itr = Iterator[A](curr) ++ itr;
    }
    return (( peek ,  itr ));
  }

  case class DummyIterator[A](v : A) extends Iterator[A] {
    def hasNext : Boolean = true;
    def next : A = v;
  }
  def DummyStream[A](v : A) : Stream[A] = {
    DummyIterator(v).toStream;
  }

  
  def transposeMatrix[A](m : Seq[Seq[A]]) : Seq[Seq[A]] = {
    if(m.exists((seq : Seq[A]) => {
      seq.length != m(0).length;
    })) error("Error transposing matrix: not all elements in the matrix are of the same length.");
    
    Range(0,m(0).length).map((i : Int) => {
      m.map((seq : Seq[A]) => {
        seq(i);
      });
    });
  }
  
  def bufferIterator[A](iter : Iterator[A], bufferSize : Int) : Iterator[A] = {
    return new Iterator[A](){
      val groupIter = iter.grouped(bufferSize);
      var buffer : Seq[A] = Seq[A]();
      def hasNext : Boolean = if(buffer.isEmpty) groupIter.hasNext else true;
      def next : A = {
        if(buffer.isEmpty){
          buffer = groupIter.next;
          next();
        } else {
          val out = buffer.head;
          buffer = buffer.tail;
          out; 
        }
      }
    }
  }
  def filterWithCount[A >: Null](iter : Iterator[A],filterFunction : (A => Boolean)) : IterWithCount[A] = {
    if(iter.hasNext){
      new FilterWithCountNonEmpty(iter,filterFunction);
    } else {
      new FilterWithCountEmpty();
    }
  }
  
  abstract class IterWithCount[A >: Null] extends Iterator[A] {
    def next : A
    def hasNext : Boolean
    def getCt : Int
  }
  
  class FilterWithCountEmpty[A >: Null]() extends IterWithCount[A] {
    def next : A = null;
    def hasNext : Boolean = false;
    def getCt : Int = 0;
  }
  class FilterWithCountNonEmpty[A >: Null](iter : Iterator[A], filterFunction : (A => Boolean)) extends IterWithCount[A] {
    private var biter = iter.buffered;
    private val skipFunction = (a : A) => ! filterFunction(a)
    private var dropCt : Int = skipWhileCt[A](biter)(skipFunction);
    private var (nextHolder,iterHasNext) : (A,Boolean) = if(iter.hasNext) (iter.next,true) else (null,false);
    
    def getCt : Int = dropCt;
    def hasNext : Boolean = iterHasNext;
    def next : A = {
      val out = nextHolder;
      dropCt = dropCt + skipWhileCt[A](biter)(skipFunction);
      if(iter.hasNext){
        nextHolder = iter.next;
      } else {
        iterHasNext = false;
      }
      return(out);
        
    }
  }
  
  def seqIsSortedBy[A](s : Seq[A], compareToFunc : ((A,A) => Boolean)) : Boolean = {
    if(s.length <= 1){ 
      true;
    } else {
      s.zip(s.tail).forall( a12 => {
        compareToFunc(a12._1,a12._2); 
      })
    }
  }
  
  
  //Natural Number Iterator:
  def getNaturalNumberIterator(start : Int) : Iterator[Int] = {
    return new Iterator[Int](){
      var curr = start - 1;
      def hasNext : Boolean = true;
      def next : Int = {
        curr += 1;
        return curr;
      }
    }
  }  
   
  //zip an iterator with a count.
  def zipIteratorWithCount[T](iter : Iterator[T], start : Int = 0) : Iterator[(T,Int)] = {
    iter.zip(getNaturalNumberIterator(start));
  }
  
  def repToSeq[T](toCopy : T, times : Int): Seq[T] = {
    if(times == 0) return Seq()
    else if(times == 1) return Seq(toCopy);
    else {
      return ((0 until times).map((x) => toCopy) ).toSeq;
    }
  }
  def repToVector[T](toCopy : T, times : Int): Vector[T] = {
    if(times == 0) return Vector[T]()
    else if(times == 1) return Vector[T](toCopy);
    else {
      return ((0 until times).map((x) => toCopy) ).toVector;
    }
  }
  
  def repString(toCopy : String, times : Int) : String = {
    if(times == 0) return ""
    else if(times == 1) return toCopy;
    else {
      return (0 until times).foldLeft(""){ case (soFar,i) => { soFar + toCopy } }
    }
  }
  
  def wrapIteratorWithCutoff[B](iter : Iterator[B], cutoff : Int) : Iterator[B] = {
    new Iterator[B] {
      var iterCt = 0;
      def hasNext : Boolean = iterCt < cutoff && iter.hasNext;
      def next : B = {
        iterCt += 1;
        return iter.next;
      }
    }
  }
  

  
  /**************************************************************************************************************************
   * Iterator progress reporting
   **************************************************************************************************************************/
  
  abstract class IteratorProgressReporter {
    def reportProgress(iterCt : Int);
    
    def wrapIterator[B](iter : Iterator[B]) : Iterator[B] = {
      val ips = this;
      new Iterator[B]{
        var iterCt = 0;
        def hasNext : Boolean = iter.hasNext;
        def next : B = {
          iterCt += 1;
          ips.reportProgress(iterCt);
          return iter.next;
        }
      }
    }
  }
  case class IteratorProgressReporter_ThreeLevel(elementTitle : String, dotThreshold : Int, dotSpaceThreshold : Int , dotNewlineThreshold : Int ) extends IteratorProgressReporter  {
    def reportProgress(iterCt : Int){
      if(iterCt % dotThreshold == 0){ if(iterCt % dotSpaceThreshold == 0) { if(iterCt % dotNewlineThreshold == 0) report(".["+iterCt+" "+elementTitle+" processed] [Time: "+getDateAndTimeString+"] \n","progress"); else report(". ","progress"); } else { report(".","progress");}}
    }
  }
  
  abstract class AdvancedIteratorProgressReporter[A] {
    def reportProgress(iterCt : Int, a : A);
    
    def reportStart(iterCt : Int, a : A) = {
      //reportProgress(iterCt,a);
      //default: do nothing!
    }
    def reportEnd(iterCt : Int, a : A) = {
      //reportProgress(iterCt,a);
      //default: do nothing!
    }
  }
  case class AdvancedIteratorProgressReporter_ThreeLevel[A](elementTitle : String, 
                                                            dotThreshold : Int, 
                                                            dotSpaceThreshold : Int , 
                                                            dotNewlineThreshold : Int, 
                                                            reportFunction : ((A,Int) => String) = ((a : A,i : Int) => "")) extends AdvancedIteratorProgressReporter[A]  {
    def reportProgress(iterCt : Int, a : A){
      if(iterCt % dotThreshold == 0){ if(iterCt % dotSpaceThreshold == 0) { if(iterCt % dotNewlineThreshold == 0) report(".["+iterCt+" "+elementTitle+" processed] [Time: "+getDateAndTimeString+"] "+reportFunction(a,iterCt)+"\n","progress"); else report(". ","progress"); } else { report(".","progress");}}
    }
  }
  case class AdvancedIteratorProgressReporter_ThreeLevelAccel[A](elementTitle : String, 
                                                                 accelFactor : Int = 10,
                                                                 maxAccel : Int = 1000,
                                                                 var dotThreshold : Int = 1,
                                                                 var dotSpaceThreshold : Int = 5,
                                                                 var dotNewlineThreshold : Int = 10,
                                                                 reportFunction : ((A,Int) => String) = ((a : A,i : Int) => "")) extends AdvancedIteratorProgressReporter[A]  {
    var accel = 1;
    var space = "x";
    
    def reportProgress(iterCt : Int, a : A){
      if(iterCt % dotThreshold == 0){
        if(iterCt % dotSpaceThreshold == 0) { if(iterCt % dotNewlineThreshold == 0){ 
          if(accel < maxAccel){
            accel = accel * accelFactor;
            dotThreshold        = dotThreshold  * accelFactor;
            dotSpaceThreshold   = dotSpaceThreshold  * accelFactor;
            dotNewlineThreshold = dotNewlineThreshold * accelFactor;
          } else if(space == "x"){
            space = "";
          }
          report(".["+iterCt+" "+elementTitle+" processed] [Time: "+getDateAndTimeString+"] "+reportFunction(a,iterCt)+"\n"+space,"progress");
        } else report(". ","progress");} else { 
          report(".","progress");
        }
      }
    }
  }
  case class AdvancedIteratorProgressReporter_ThreeLevelAuto_OLD[A](elementTitle : String = "lines", lineSec : Int = 300,
                                                                 reportFunction : ((A,Int) => String) = ((a : A,i : Int) => ""),
                                                                 linePrefix : String = "") extends AdvancedIteratorProgressReporter[A]  {
    var accel = 1;
    var space = "x";
    val accelFactor : Int = 10
    val accelFactors : Iterator[Float] = circularIterator(Seq(2.0.toFloat,2.5.toFloat,2.0.toFloat));
    var dotThreshold : Int = 1
    var dotSpaceThreshold : Int = 5
    var dotNewlineThreshold : Int = 20
    
    var lastLineTime = System.nanoTime();
    var lastLineLen = 1.0;
    var i = 1;
    
    def bufferProgressBar(bufferChar : String = "x"){
        val numX = (i % dotNewlineThreshold) / dotThreshold;
        val numSpaces = numX / 5;
        val numFinalX = numX % 5;
        space = repString("xxxxx ",numSpaces) + repString("x",numFinalX);
        
    }
    
    def reportProgress(iterCt : Int, a : A){
      if(i == 1) report(linePrefix,"progress");
      i = iterCt;
      if(iterCt % dotThreshold == 0){ 
        if(iterCt % dotSpaceThreshold == 0) { 
          if(iterCt % dotNewlineThreshold == 0){ 
            val currTime = System.nanoTime();
            val elapsedSec = (currTime - lastLineTime) / 1000000000;
            val accelerate = elapsedSec.toDouble / lastLineLen < lineSec;
            space = "";
            if(accelerate){
              //accel = accel * accelFactor;
              dotThreshold = math.round(dotThreshold.toFloat * accelFactors.next)
              dotSpaceThreshold = dotThreshold * 5;
              dotNewlineThreshold = dotThreshold * 20;
              //dotThreshold        = dotThreshold  * accelFactor;
              //dotSpaceThreshold   = dotSpaceThreshold  * accelFactor;
              //dotNewlineThreshold = dotNewlineThreshold * accelFactor;
              val numX = (iterCt % dotNewlineThreshold) / dotThreshold;
              lastLineLen = (20-numX).toDouble / 20.toDouble;
              val numSpaces = numX / 5;
              val numFinalX = numX % 5;
              space = repString("xxxxx ",numSpaces) + repString("x",numFinalX);
            }
            lastLineTime = currTime;
            report(".["+iterCt+" "+elementTitle+" processed] [Time: "+getDateAndTimeString+"] ("+elapsedSec+"s)"+reportFunction(a,iterCt)+"\n"+
                   {if(accelerate) "                       [AutoProgressReporter: "+dotThreshold+" "+elementTitle+" per dot] [TargetTime = "+lineSec+"s]"+"\n" else ""}+
                   linePrefix +
                   space,"progress");
          } else report(". ","progress");
        } else { 
          report(".","progress");
        }
      }
    }
  }
  

  case class AdvancedIteratorProgressReporter_ThreeLevelAuto[A](elementTitle : String = "lines", lineSec : Int = 60,
                                                                 reportFunction : ((A,Int) => String) = ((a : A,i : Int) => "")) extends AdvancedIteratorProgressReporter[A]  {
    var accel = 1;
    var space = "x";
    val accelFactor : Int = 10
    val accelFactors : Iterator[Float] = circularIterator(Seq(2.0.toFloat,2.5.toFloat,2.0.toFloat));
    var dotThreshold : Int = 1
    var dotSpaceThreshold : Int = 5
    var dotNewlineThreshold : Int = 20
    
    var lastLineTime = System.nanoTime();
    var lastLineLen = 1.0;
    var i = 1;
    
    var lastLineNum = 20;
    val BURN_IN_CT = 200;
    var startNanos = System.nanoTime();
    
    def bufferProgressBar(bufferChar : String = "x"){
        val numX = (i % dotNewlineThreshold) / dotThreshold;
        val numSpaces = numX / 5;
        val numFinalX = numX % 5;
        space = repString("xxxxx ",numSpaces) + repString("x",numFinalX);
    }
    
    def reportProgress(iterCt : Int, a : A){
      i = iterCt;
      if(i == BURN_IN_CT){
        startNanos = System.nanoTime();
        progressReport(iterCt,"["+iterCt+" "+elementTitle+" processed] BURN IN TIME COMPLETE [Time: "+getDateAndTimeString+"] "+reportFunction(a,iterCt));
      }
      if(iterCt % dotThreshold == 0){
        val numX = if(i % dotNewlineThreshold == 0) 20 else (i % dotNewlineThreshold) / dotThreshold;
        progressDot(i = numX, dotsPerGroup = 5, groupsPerLine = 4, blankSpacer = "-", verb = "progress");
        
        if(iterCt % dotNewlineThreshold == 0){
            val currTime = System.nanoTime();
            val elapsedSec = (currTime - lastLineTime) / 1000000000;
            val elapsedMillis = (currTime - lastLineTime) / 1000000;
            val elapsedMilliString = zeroPad(  (elapsedMillis % 1000).toInt, cols = 3);
            val linesPerSec   = (lastLineNum.toDouble) / (elapsedMillis.toDouble / 1000.toDouble);
            
            val speedString = if(linesPerSec < 10){
              val linesPerMin = (lastLineNum * 60).toDouble / (elapsedMillis.toDouble / 1000.toDouble);
              ((math.rint(linesPerMin * 100)) / 100) +" "+elementTitle+ " per min";
            } else if(linesPerSec < 1000){
              ((math.rint(linesPerSec * 100)) / 100) +" "+elementTitle+ " per sec";
            } else {
              math.round(linesPerSec) +" "+elementTitle+ " per sec";
            }
            val accelerate = elapsedSec.toDouble / lastLineLen < lineSec;
            if(accelerate){
              dotThreshold = math.round(dotThreshold.toFloat * accelFactors.next)
              dotSpaceThreshold = dotThreshold * 5;
              dotNewlineThreshold = dotThreshold * 20;
            }
            val newNumX = (i % dotNewlineThreshold) / dotThreshold;
            lastLineLen = (20-newNumX).toDouble / 20.toDouble;
            lastLineNum = dotNewlineThreshold - (iterCt % dotNewlineThreshold);
            lastLineTime = currTime;
            progressReport(iterCt,"["+iterCt+" "+elementTitle+" processed] [Time: "+getDateAndTimeString+"] ("+elapsedSec+"."+elapsedMilliString+"s; "+speedString+"; Elapsed="+nanoTimeDifferenceFormat(currTime - startNanos)+")"+reportFunction(a,iterCt));
            if(accelerate){
              reportln("[AutoProgressReporter: "+dotThreshold+" "+elementTitle+" per dot] [TargetTime = "+lineSec+"s]","progress")
            }
            startProgressLine(blankSpaces = newNumX, spacer = "x", groupSpacer = " ", dotsPerGroup = 5, groupsPerLine = 4, verb = "progress")
        }
      }
    }
    var initTime = System.nanoTime();
    override def reportStart(iterCt : Int, a : A){
      progressReport(iterCt,"[STARTING ITERATION:] [Time: "+getDateAndTimeString+"]"+reportFunction(a,iterCt));
      val currTime = System.nanoTime();
      lastLineTime = currTime;
      initTime = currTime;
    }
    override def reportEnd(iterCt : Int, a : A){
      val currTime = System.nanoTime();
      val elapsedMillis = (currTime - initTime) / 1000000;
      val elapsedString = TimeStampUtil.timeDifferenceFormatterNanos(currTime - initTime);
      val linesPerSec   = (iterCt.toDouble) / (elapsedMillis.toDouble / 1000.toDouble);
      
      val speedString = if(linesPerSec < 10){
              val linesPerMin = (iterCt * 60).toDouble / (elapsedMillis.toDouble / 1000.toDouble);
              ((math.rint(linesPerMin * 100)) / 100) +" "+elementTitle+ " per min";
      } else if(linesPerSec < 1000){
              ((math.rint(linesPerSec * 100)) / 100) +" "+elementTitle+ " per sec";
      } else {
              math.round(linesPerSec) +" "+elementTitle+ " per sec";
      }
      progressReport(iterCt,"[FINISHED ITERATION: "+iterCt+" "+elementTitle+" processed] [Time: "+getDateAndTimeString+"] (Elapsed: "+elapsedString+"; "+speedString+")"+reportFunction(a,iterCt));
    }
  }
  
  def wrapIteratorWithAdvancedProgressReporter2[B]( iter : Iterator[B] , ipr : AdvancedIteratorProgressReporter[B] ) : Iterator[B] = {
    iter.zip(Iterator.from(1)).map{case (b,i) => {
      ipr.reportProgress(i,b);
      b;
    }}
  }
  
  def wrapIteratorWithAdvancedProgressReporter[B]( iter : Iterator[B] , ipr : AdvancedIteratorProgressReporter[B] ) : Iterator[B] = {
    //iter.zip(Iterator.from(1)).map{case (b,i) => {
    //  ipr.reportProgress(i,b);
    //  b;
    //}}
    new Iterator[B]{
      var i = 1;
      def hasNext : Boolean = iter.hasNext;
      def next : B = {
        val n = iter.next;
        if(i == 1) ipr.reportStart(i,n);
        ipr.reportProgress(i,n);
        if(! hasNext) ipr.reportEnd(i,n);
        i = i + 1;
        n;
      }
    }
  }
  
  
  def wrapIteratorWithProgressReporter[B]( iter : Iterator[B] , ipr : IteratorProgressReporter ) : Iterator[B] = {
    new Iterator[B]{
      var iterCt = 0;
      def hasNext : Boolean = iter.hasNext;
      def next : B = {
        iterCt += 1;
        ipr.reportProgress(iterCt);
        return iter.next;
      }
    }
  }
  //stdUtils.presetProgressReporters.wrapIterator_readPairs(iter,verbose,cutoff)
  object presetProgressReporters {
    val DEFAULT_ITERATOR_PROGRESS_REPORTER_READPAIRS : IteratorProgressReporter = IteratorProgressReporter_ThreeLevel("Read-Pairs", 100000, 1000000, 1000000);
    
    def wrapIterator_readPairs[B](iter : Iterator[B], verbose : Boolean = true, cutoff : Int = -1) : Iterator[B] = {
      if((! verbose) && (cutoff == -1)) return iter;
      else if((  verbose) && (cutoff == -1)) return DEFAULT_ITERATOR_PROGRESS_REPORTER_READPAIRS.wrapIterator(iter);
      else if((  verbose) && (cutoff != -1)) return wrapIteratorWithCutoff(DEFAULT_ITERATOR_PROGRESS_REPORTER_READPAIRS.wrapIterator(iter), cutoff);
      else return wrapIteratorWithCutoff(iter, cutoff);
    }
  }
  
  /**************************************************************************************************************************
   * String conversion:
   **************************************************************************************************************************/
  
  
  
  def escapeToMarkdown(s : String) : String = {
    escapifyString(s, List("`","\\*","_","\\{","\\}","\\[","\\]","\\(","\\)","\\#","\\+","-","\\.","!"));
  }
  def escapifyString(s : String, escapifyStrings : Seq[String], escapeString : String = "\\\\") : String = {
    escapifyStrings.foldLeft[String](s)((soFar, curr) => {
      soFar.replaceAll(curr,escapeString+curr);
    });
  }
  
  def string2float(s : String) : Float = {
    augmentString(s).toFloat
  }
  def string2int(s : String) : Int = {
    augmentString(s).toInt
  }
  def string2long(s : String) : Long = {
    augmentString(s).toLong
  } 
  def string2double(s : String) : Double = {
    augmentString(s).toDouble
  }
  
  def hexstring2int(s : String) : Int = {
    Integer.parseInt(s,16);
  }
  
  def string2floatOpt(s : String) : Option[Float] = {
    try {
        Some(s.toFloat)
    } catch {
        case e: NumberFormatException => None
    }
  }
  def string2doubleOpt(s : String) : Option[Double] = {
    try {
        Some(s.toDouble)
    } catch {
        case e: NumberFormatException => None
    }
  }
  def string2intOpt(s : String) : Option[Int] = {
    try {
        Some(s.toInt)
    } catch {
        case e: NumberFormatException => None
    }
  }
  
  //note on string formatting:
  // "%.3f".format(1.092512) returns "1.093"
  
  /**************************************************************************************************************************
   * String formatting
   **************************************************************************************************************************/
  
  
  
  def indentifyLines(s : String, indent : String) : String = {
    indent + s.split("\n").toVector.mkString("\n" + indent) + (if(s.charAt(s.length - 1) == '\n') "\n" else "");
  }
  
  def stripFinalNewline(s : String) : String = {
    if(s.charAt(s.length - 1) == '\n'){
      return(s.substring(0,s.length-1));
    } else {
      return(s);
    }
  }
  
  def addQuotes(s : String) : String = {
    "\"" + s + "\"";
  }
  
  def cleanQuotes(s : String) : String = {
    if(s.length < 2) s;
    else if(s.charAt(0) == '"' && s.charAt(s.length - 1) == '"'){
      s.substring(1,s.length - 1)
    } else {
      s
    }
  }  
  
  def formatByteCount(b : Long) : String = {
    if(b < 5000L) b.toString + "B";
    else if(b < 5000000L) (b / 1000L).toString + "kB";
    else if(b < 5000000000L) (b / 1000000L).toString + "MB";
    else if(b < 5000000000000L) (b / 1000000000L).toString + "GB";
    else (b / 5000000000000L).toString + "TB";
  }
   
  def zeroPad(i : Int, cols : Int) : String = {
    val s = i.toString;
    return 0.toString * (cols - s.length) + s;
  }
  
  def padString(s : String, cols : Int, truncate : Boolean = false, padChar : String = " ", alignment : String = "right") : String = {
    if((! truncate) && s.length() > cols){
      error("ERROR! String too long for given column, and no truncation allowed: col="+cols+", string=\"" + s + "\"!");
    }
    if(alignment == "right"){
      padChar * (cols - s.length) + s;
    } else if(alignment == "left"){
      s + padChar * (cols - s.length);
    } else {
      error("ERROR! String Justification Method Not Recongized: \"" + alignment + "\"!");
      return null;
    }
  }
  
  def convertIntToShortStringForm(i : Int, byteStyle : Boolean = false, fullStyle : Boolean = false) : String = {
    val sRaw = i.toString();
    val sign = if(sRaw.head == '-') "-" else "";
    val s = if(sRaw.head == '-') sRaw.tail else sRaw;
    val numZeroPad = sRaw.reverse.takeWhile(_ == '0').length;
    if(numZeroPad < 3){
      return sRaw;
    } else if(numZeroPad < 6){
      return sign + s.dropRight(3) + ( if(byteStyle){ "k" } else if(fullStyle){ " thousand" } else { "k" } )
    } else if(numZeroPad < 9){
      return sign + s.dropRight(6) + ( if(byteStyle){ "M" } else if(fullStyle){ " million" } else { "M" } )
    } else {
      return sign + s.dropRight(9) + ( if(byteStyle){ "G" } else if(fullStyle){ " billion" } else { "b" } )
    }
    
  } 
  
  /**************************************************************************************************************************
   * Line formatting:
   **************************************************************************************************************************/
  
  def leftRightJustify(str1 : String, str2 : String, width : Int = -1, padChar : String = " ", bufferSpacing : Int = 5) : String = {
    val useWidth = if(width == -1){ str1.length() + str2.length() + bufferSpacing } else { width };
    val rightWidth = useWidth - str1.length();
    return str1 + padString(str2, rightWidth, truncate = false, padChar = padChar, alignment = "right");
  }
  
  def leftRightJustifySeq(strings : Seq[(String,String)], width : Int = -1, padChar : String = " ", bufferSpacing : Int = 5) : Seq[String] = {
    val minReqWidth = strings.map{case (s1,s2) => s1.length + s2.length + bufferSpacing}.max;
    val useWidth = if(width == -1){ minReqWidth } else {width};
    strings.map{case (s1,s2) => leftRightJustify(s1,s2,width = useWidth, padChar = padChar)}
  }
  
  
  def lineseq2string(ss : Seq[String]) : String = {
    //ss.foldLeft("")(_ +"\n"+ _) + "\n";
    ss.mkString("\n");
  }
  private val whiteSpaceString = "                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          ";
  private val maxWrapWidth = whiteSpaceString.length;
  
  
  /**************************************************************************************************************************
   * Line Wrapping:
   **************************************************************************************************************************/
  
  def wrapSimpleLineWithIndent_staggered(line : String, width : Int, indent : String, firstLineIndent : String) : String = {
    if(line.length + firstLineIndent.length < width) {
      return firstLineIndent+line;
    } else {
      val (firstLine,paraRemainder) = wrapLinesWithIndent_tailRecursiveHelper_buildLine(firstLineIndent,line,width, firstLineIndent.length);
      return firstLine + "\n" + wrapLinesWithIndent_tailRecursive(Seq(), paraRemainder, width, indent).mkString("\n");
    }
  }
  def wrapSimpleLineWithIndent_staggeredCt(line : String, width : Int, indent : Int, firstLineIndent : Int) : String = {
    if(line.length + firstLineIndent < width) {
      return repString(" ",firstLineIndent)+line;
    } else {
      val (firstLine,paraRemainder) = wrapLinesWithIndent_tailRecursiveHelper_buildLine(repString(" ",firstLineIndent),line,width, firstLineIndent);
      return firstLine + "\n" + wrapLinesWithIndent_tailRecursive(Seq(), paraRemainder, width, repString(" ",indent)).mkString("\n");
    }
  }
   
  def wrapLineWithIndent(line : String, width : Int, indent : Int) : String = {
    wrapLinesWithIndent(line, width, repString(" ",indent), false);
  } 
  
  def wrapLinesWithIndent(lines : String, width : Int, indent : String, removeExistingWrapBreaks : Boolean) : String = {
    val linesSeq : Seq[String] = lines.split("\n");
    return lineseq2string(wrapLinesWithIndent(linesSeq,width, indent, removeExistingWrapBreaks));
  }
  
  def wrapLinesWithIndent(lines : Seq[String], width : Int, indent : String, removeExistingWrapBreaks : Boolean) : Seq[String] = {
    if(removeExistingWrapBreaks){
      val paras : Seq[String] = lines.foldLeft(Vector[String]())((soFar,line) => {
        if(line == ""){
          soFar :+ ""
        } else {
          soFar.init :+ (soFar.last + line);
        }
      });
      wrapLinesWithIndent_helper(paras, width, indent);
    } else {
      wrapLinesWithIndent_helper(lines , width , indent);
    }
  }
   
  def wrapLinesWithIndent_helper(paras : Seq[String], width : Int, indent : String) : Seq[String] = {
    paras.foldLeft(Vector[String]())( (soFar, curr) =>{
      soFar ++ wrapLinesWithIndent_tailRecursive(Vector[String](), curr, width, indent);
    })
  }
  
  /*private def wrapLinesWithIndent_tailRecursive_DEPRECIATED(acc : Seq[String], para : String, width : Int, indent : String) :  Seq[String] = {
    if(para.length + indent.length <= width) acc :+ (indent + para);
    else  {
      wrapLinesWithIndent_tailRecursive( acc :+ (indent + para.substring(0,width)), para.substring(width), width , indent);
    }
  }*/
  
  private def wrapLinesWithIndent_tailRecursive(acc : Seq[String], para : String, width : Int, indent : String) :  Seq[String] = {
    if(para.length + indent.length <= width) acc :+ (indent + para);
    else  {
      val (nextLine, paraRemainder) = wrapLinesWithIndent_tailRecursiveHelper_buildLine(indent, para, width, indent.length);
      return wrapLinesWithIndent_tailRecursive(acc :+ nextLine, paraRemainder, width, indent);
    }
  }
  
  private def wrapLinesWithIndent_tailRecursiveHelper_buildLine(lineSoFar : String, untrimmedPara : String, width : Int, indentLen : Int) : (String,String) = {
    val para = untrimmedPara.trim
    if(para.length == 0) return (lineSoFar,para);
    
    val indexOfNextSpace = para.indexOf(' ');
    val wordLen = if(indexOfNextSpace == -1) para.length; else indexOfNextSpace;
    //if(lineSoFar == ""){
    //  return wrapLinesWithIndent_tailRecursiveHelper_buildLine(  )
    //}
   
    
    if(indentLen + wordLen > width) {
      if(width - lineSoFar.length - 1 < 0){
        error("Impossible state: cannot collapse string:\n   (indexOfNextSpace="+indexOfNextSpace+",wordLen="+wordLen+",width="+width+"indentLen="+indentLen+")   \n lineSoFar: \""+lineSoFar+"\"\n"+
               "   \n paraLeft= \""+para+"\"")
      }

      return (lineSoFar + para.substring(0, width - lineSoFar.length - 1) + "-", 
          para.substring(width - lineSoFar.length - 1));
      

    }
                                            
    if(lineSoFar.length + wordLen >= width) return (lineSoFar,para);

    val word = para.substring(0,wordLen);
    val paraRemainder = para.substring(wordLen);
    return( wrapLinesWithIndent_tailRecursiveHelper_buildLine(lineSoFar + word + " ", paraRemainder, width, indentLen) );
  }
  
  /**************************************************************************************************************************
   * Parallelization:
   **************************************************************************************************************************/
  
  def parConvert[T](v : Seq[T], numThreads : Int) : scala.collection.GenSeq[T] = {
    if(numThreads == 1) {
      return v;
    } else {
      val vpar = v.par;
      //vpar.tasksupport = new scala.collection.parallel.ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(numThreads));
      vpar;
    }
  } 
  

  /**************************************************************************************************************************
   * Min Option:
   **************************************************************************************************************************/
  
  def minOption[T](x : Option[T],y : Option[T])(implicit ord : Ordering[T]) : Option[T] = {
    x match {
      case Some(xi) => {
        y match {
          case Some(yi) => {
            if( ord.compare(xi,yi) <= 0) Some(xi);
            else Some(yi);
          }
          case None => {
            Some(xi);
          }
        }
      }
      case None => {
        y match {
          case Some(yi) => {
            Some(yi);
          }
          case None => {
            None;
          }
        }
      }
    }
  }
  def maxOption[T](x : Option[T],y : Option[T])(implicit ord : Ordering[T]) : Option[T] = {
    x match {
      case Some(xi) => {
        y match {
          case Some(yi) => {
            if( ord.compare(xi,yi) <= 0) Some(yi);
            else Some(xi);
          }
          case None => {
            Some(xi);
          }
        }
      }
      case None => {
        y match {
          case Some(yi) => {
            Some(yi);
          }
          case None => {
            None;
          }
        }
      }
    }
  }
  
  /*
  def minOption(x: Option[Int], y : Option[Int]) : Option[Int] = {
    x match {
      case Some(xi) => {
        y match {
          case Some(yi) => {
            Some(math.min(xi,yi));
          }
          case None => {
            Some(xi);
          }
        }
      }
      case None => {
        y match {
          case Some(yi) => {
            Some(yi);
          }
          case None => {
            None;
          }
        }
      }
    }
  }
  def minOption(x: Option[Float], y : Option[Float]) : Option[Float] = {
    x match {
      case Some(xi) => {
        y match {
          case Some(yi) => {
            Some(math.min(xi,yi));
          }
          case None => {
            Some(xi);
          }
        }
      }
      case None => {
        y match {
          case Some(yi) => {
            Some(yi);
          }
          case None => {
            None;
          }
        }
      }
    }
  }
  def minOption(x: Option[Double], y : Option[Double]) : Option[Double] = {
    x match {
      case Some(xi) => {
        y match {
          case Some(yi) => {
            Some(math.min(xi,yi));
          }
          case None => {
            Some(xi);
          }
        }
      }
      case None => {
        y match {
          case Some(yi) => {
            Some(yi);
          }
          case None => {
            None;
          }
        }
      }
    }
  }
  def minOption(x: Option[Long], y : Option[Long]) : Option[Long] = {
    x match {
      case Some(xi) => {
        y match {
          case Some(yi) => {
            Some(math.min(xi,yi));
          }
          case None => {
            Some(xi);
          }
        }
      }
      case None => {
        y match {
          case Some(yi) => {
            Some(yi);
          }
          case None => {
            None;
          }
        }
      }
    }
  }*/

  /**************************************************************************************************************************
   * Max Option:
   **************************************************************************************************************************/
  
  
  /*
  def maxOption(x: Option[Int], y : Option[Int]) : Option[Int] = {
    x match {
      case Some(xi) => {
        y match {
          case Some(yi) => {
            Some(math.max(xi,yi));
          }
          case None => {
            Some(xi);
          }
        }
      }
      case None => {
        y match {
          case Some(yi) => {
            Some(yi);
          }
          case None => {
            None;
          }
        }
      }
    }
  }
  def maxOption(x: Option[Float], y : Option[Float]) : Option[Float] = {
    x match {
      case Some(xi) => {
        y match {
          case Some(yi) => {
            Some(math.max(xi,yi));
          }
          case None => {
            Some(xi);
          }
        }
      }
      case None => {
        y match {
          case Some(yi) => {
            Some(yi);
          }
          case None => {
            None;
          }
        }
      }
    }
  }
  def maxOption(x: Option[Double], y : Option[Double]) : Option[Double] = {
    x match {
      case Some(xi) => { 
        y match {
          case Some(yi) => {
            Some(math.max(xi,yi));
          }
          case None => {
            Some(xi);
          }
        }
      }
      case None => {
        y match {
          case Some(yi) => {
            Some(yi);
          }
          case None => {
            None;
          }
        }
      }
    }
  }
  
  def maxOption(x: Option[Long], y : Option[Long]) : Option[Long] = {
    x match {
      case Some(xi) => {
        y match {
          case Some(yi) => {
            Some(math.max(xi,yi));
          }
          case None => {
            Some(xi);
          }
        }
      }
      case None => {
        y match {
          case Some(yi) => {
            Some(yi);
          }
          case None => {
            None;
          }
        }
      }
    }
  }
  
  
  */
}
















