package internalUtils

/*
 * This is a standard 
 * 
 * I use this in most of my scala programs.
 */

object Reporter {

  
  private abstract class ReportLogger(){
    
    
    def reportln(str : String, verb : String){
      if(isWorthy(verb)){
        freshenLine;
        report_basefunction(str + "\n");
      }
    } 
     
    def startReport(str : String, verb : String){
      if(isWorthy(verb)){
        freshenLine;
        report_basefunction(str);
        isMidline = str.last != '\n';
      }
    }
    
    /*def freshen(verb : String){
      if(isWorthy(verb)){
        freshenLine;
        isMidline = false;
      }
    }*/
    def report(str : String, verb : String){
      if(isWorthy(verb)){
        report_basefunction(str);
        isMidline = str.length > 0 && str.last != '\n';
      }
    }
    
    /*
     * Do not use the below functions:
     */
    
    final val verbosityNames = List("output","error","warn","report","note","progress","debug","deepdebug","deepestdebug");
    var isMidline = false;
    def isWorthy(verb : String) : Boolean = {
      if(verbosityNames.indexOf(verb) == -1) true;
      else verbositySetting(verbosityNames.indexOf(verb))
    }
    def freshenLine() {
      if(isMidline) report_basefunction("\n");
      isMidline = false;
    }
    
    /*
     * These must be defined in the inheriting classes:
     */
    //def init(filename : String);
    val verbositySetting: Array[Boolean]
    //def open()
    def report_basefunction(str : String)
    def close()
  }
 // case class FileReportLogger(var verbositySetting: Array[Boolean])(file : fileUtils.WriterUtil) extends ReportLogger {
    
 // }
  
  private case class FileReportLogger(vset : Array[Boolean], outfile : String) extends ReportLogger {
    val verbositySetting = vset;
    
    val writer : fileUtils.WriterUtil = fileUtils.openWriter(outfile);
    
    def report_basefunction(str : String){
      writer.write(str);
      writer.flush;
    }
    def close() {
      fileUtils.close(writer);
    }
  }
  
  private case class ConsoleReportLogger(vset : Array[Boolean]) extends ReportLogger {
    val verbositySetting = vset;
    
    def report_basefunction(str : String){
      print(str);
    }
    def close() {
      //do nothing.
    }
  }
  
  private case class ErrConsoleReportLogger(vset : Array[Boolean]) extends ReportLogger {
    val verbositySetting = vset;
    
    def report_basefunction(str : String){
      Console.err.print(str);
    }
    def close() {
      //do nothing.
    }
  }
  
  private case class StringReportLogger(vset : Array[Boolean]) extends ReportLogger {
    val verbositySetting = vset;
    val sb : StringBuilder = new StringBuilder();
    
    def report_basefunction(str : String){
      sb.append(str);
    }
    def close() {
      //do nothing.
    }
    def getLogString() : String = return sb.toString();
  }
  
  private case class AddedFileReportLogger(logfile : String, srl : StringReportLogger) extends ReportLogger {
    val verbositySetting = srl.verbositySetting;
    val writer = fileUtils.openWriter(logfile);
    writer.write(srl.getLogString());
    
    def report_basefunction(str : String){
      writer.write(str);
      writer.flush();
    }
    def close() {
      writer.close();
      /*try {
        
        writer.write(srl.getLogString());
        
      } catch {
        case e : Exception => {
          //do nothing!
        }
      }*/
    }
  }
  /*
   * 
   *     } catch {
      case e : Exception => {
        internalUtils.Reporter.reportln("============================FATAL_ERROR============================\n"+
                                        "QoRTs encountered a FATAL ERROR. For general help, use command:\n"+
                                        "          java -jar path/to/jar/QoRTs.jar --help\n"+
                                        "============================FATAL_ERROR============================\n"+
                                        "Error info:","note");
        throw e;
      }
    }
   */
 
  private var loggers : List[ReportLogger] = List[ReportLogger]();

  
/*
 * USAGE METHODS:
 */
  
  //final val verbosityNames = List("output","error","warn","report","note","progress","debug","deepdebug");
  val DEFAULT_CONSOLE_VERBOSITY = Array(false,true,true,true,true,true,false,false,false);
  val QUIET_CONSOLE_VERBOSITY = Array(false,true,true,false,false,false,false,false,false);
  val VERBOSE_CONSOLE_VERBOSITY = Array(false,true,true,true,true,true,true,true,false);
  val DEBUG_CONSOLE_VERBOSITY = Array(false,true,true,true,true,true,true,true,true);
  val OUTPUT_VERBOSITY = Array(true, false, false, false, false,false,false,false,false);
  
  val logVerbositySetting = Array(false,true,true,true,true,true,true,true);
  val debugLogVerbositySetting = Array(false,true,true,true,true,true,true,true);
  val warningLogVerbositySetting = Array(false,true,true,false,false,false,false,false);
  
  //default internal logger:
  private val internalLog : StringReportLogger = StringReportLogger(logVerbositySetting);
  private val warningLog : StringReportLogger = StringReportLogger(warningLogVerbositySetting);
  private val outputLog : ConsoleReportLogger = ConsoleReportLogger(OUTPUT_VERBOSITY);
  
  def getWarnings : String = warningLog.getLogString();
  /*
   * Initializers:
   */
  def init_full(logDir : String) {
    val logfile = logDir + "log.log";
    val debugLogfile = logDir + "debugLog.log";
    
    val fileLogger = FileReportLogger(logVerbositySetting, logfile);
    val debugFileLogger = FileReportLogger(debugLogVerbositySetting, debugLogfile);
    val consoleLogger = ConsoleReportLogger(DEFAULT_CONSOLE_VERBOSITY);
    
    loggers = fileLogger :: debugFileLogger :: consoleLogger :: loggers;
  }
  
  def init_simple(logDir : String) {
    val logfile = logDir + "log.log";
    //val debugLogfile = logDir + "debugLog.log";
    
    val fileLogger = FileReportLogger(logVerbositySetting, logfile);
    val consoleLogger = ConsoleReportLogger(DEFAULT_CONSOLE_VERBOSITY);
    
    loggers = fileLogger :: consoleLogger :: loggers;
  }
  
  def init_logfilefree {
    val consoleLogger = ConsoleReportLogger(DEFAULT_CONSOLE_VERBOSITY);
    loggers = consoleLogger :: loggers;
  }
  
  def init_stderrOnly(verbositySetting : Array[Boolean] = DEFAULT_CONSOLE_VERBOSITY) {
    val errLogger = ErrConsoleReportLogger(verbositySetting);
    loggers = errLogger :: loggers;
  }
  
  def init_completeLogFile(logfile : String) {
    val fileLogger = AddedFileReportLogger(logfile, internalLog)
    loggers = fileLogger :: loggers;
  }
  def init_warningLogFile(logfile : String) {
    val fileLogger = AddedFileReportLogger(logfile, warningLog)
    loggers = fileLogger :: loggers;
  }
  
  def init_base(){
    loggers = internalLog :: loggers;
    loggers = warningLog :: loggers;
    loggers = outputLog :: loggers;
    
  }
  
  /*
   * Reporting options:
   */
  
  var anyWarning : Boolean = false;
  
  def hasWarningOccurred() : Boolean = anyWarning;
  
  def reportln(str : String, verb : String) {
    if(verb == "warn"){
      anyWarning = true;
    }
    
    loggers.map((logger) => logger.reportln(str,verb))
  }
  
  def report(str : String, verb : String){
    if(verb == "warn"){
      anyWarning = true;
    }
    loggers.map((logger) => logger.report(str,verb))
  }
  
  def startReport(str : String, verb : String){
    if(verb == "warn"){
      anyWarning = true;
    }
    loggers.map((logger) => logger.startReport(str,verb))
  }
  
  def attachProgressReporter[A](pr : stdUtils.AdvancedIteratorProgressReporter[A]){
    
  }
  
  var PROGRESS_NEEDS_NEWLINE = true;
  
  def progressReport(i : Int, s : String, verb : String = "progress"){
    report("] " + (s +: getProgressReportStrings(i).map{s => "      "+s} ).mkString("\n")+"\n",verb=verb);
  }
  def progressDot(i : Int, dotsPerGroup : Int = 5, groupsPerLine : Int = 4, blankSpacer : String = "-", verb : String = "progress"){
    if(PROGRESS_NEEDS_NEWLINE){
      startProgressLine(i-1,dotsPerGroup = dotsPerGroup, groupsPerLine = groupsPerLine, verb=verb,spacer = blankSpacer);
      PROGRESS_NEEDS_NEWLINE = false;
    }
    if(i != (groupsPerLine * dotsPerGroup) && i % dotsPerGroup == 0){
      report(". ",verb);
    } else {
      report(".",verb);
    }
    
  }
  def startProgressLine(blankSpaces : Int, spacer : String = "x", groupSpacer : String = " ", dotsPerGroup : Int = 5, groupsPerLine : Int = 4, verb : String = "progress"){
    var out = "[";
    var grpNum = blankSpaces / dotsPerGroup;
    out = out + stdUtils.repString(stdUtils.repString(spacer,dotsPerGroup)+groupSpacer,grpNum);
    out = out + stdUtils.repString(spacer,blankSpaces % dotsPerGroup);
    report(out,verb);
  }
  
  
  
  val warningCount = scala.collection.mutable.Map[String,Int]().withDefault((x : String) => 0);
  def warningLazy(str : () => String, warnType : String = "default", limit : Int = -1){
    val warnCt = warningCount(warnType)
    if(limit < 0 || warnCt <= limit){
      PROGRESS_NEEDS_NEWLINE = true;
      reportln("  #### WARNING ("+warnType+" "+(warnCt+1)+"):","warn");
      reportln("    >> "+str().split("\n").mkString("\n    >> "),"warn");
      if(limit > 0 && warnCt == limit) reportln("       (("+limit+"+ warnings of type "+warnType+". Further warnings of this type will be silent.))","warn");
    }
    warningCount(warnType) += 1;
  }
  def warning(str : String, warnType : String = "default", limit : Int = -1){
    val warnCt = warningCount(warnType)
    if(limit < 0 || warnCt <= limit){
      PROGRESS_NEEDS_NEWLINE = true;
      reportln("  #### WARNING ("+warnType+" "+(warnCt+1)+"):","warn");
      reportln("    >> "+str.split("\n").mkString("\n    >> "),"warn");
      if(limit > 0 && warnCt == limit) reportln("       (("+limit+"+ warnings of type "+warnType+". Further warnings of this type will be silent.))","warn");
    }
    warningCount(warnType) += 1;
  }
  
  val noticeCount = scala.collection.mutable.Map[String,Int]().withDefault((x : String) => 0);
  def notice(str : String, warnType : String = "default", limit : Int = -1){
    val warnCt = noticeCount(warnType)
    if(limit < 0 || warnCt <= limit){
      PROGRESS_NEEDS_NEWLINE = true;
      reportln("  #### NOTE ("+warnType+" "+(warnCt+1)+"):","warn");
      reportln("    >> "+str.split("\n").mkString("\n    >> "),"warn");
      if(limit > 0 && warnCt == limit) reportln("       (("+limit+"+ notices of type "+warnType+". Further warnings of this type will be silent.))","warn");
    }
    noticeCount(warnType) += 1;
  }
  def initNotice(warnType : String){
    noticeCount(warnType) = 0
  }
  
  val tallies = scala.collection.mutable.Map[String,Int]().withDefault((x : String) => 0);
  val talliesCol2 = scala.collection.mutable.Map[String,Int]().withDefault((x : String) => 0);
  val tallyFloat = scala.collection.mutable.Map[String,Double]().withDefault((x : String) => 0.0);

  def tally(str : String,v : Int){
    tallies.update(str,tallies(str)+v);
  }
  def tally(str : String,v : Double){
    tallyFloat.update(str,tallyFloat(str)+v);
  }
  def tally(str : String, v: Double,v2 : Int){
    tallies.update(str,tallies(str)+v2);
    tallyFloat.update(str,tallyFloat(str)+v);
  }
  def tally(str : String,v : Int, v2 : Int){
    tallies.update(str,tallies(str)+v);
    talliesCol2.update(str,talliesCol2(str)+v2);
  }
  
  def tallyWithDebugReport(str : String,v : Int){
    PROGRESS_NEEDS_NEWLINE = true;
    reportln("    Tallying str:\""+str+"\": "+ v + "/"+tallies(str),"debug");
    tallies.update(str,tallies(str)+v);
  }
  def tallyWithDebugReport(str : String,v : Double){
    PROGRESS_NEEDS_NEWLINE = true;
    reportln("    Tallying str:\""+str+"\": "+ v + "/"+tallyFloat(str),"debug");
    tallyFloat.update(str,tallyFloat(str)+v);
  }
  
  def error(str : String){
    reportln("<====== FATAL ERROR! ======>","error");
    reportln("----------------------------","error");
    reportln("     Error message: \"" + str + "\"","error");
    reportln("     Stack Trace:","error");
    val stackTrace = Thread.currentThread.getStackTrace;
    stackTrace.map((ste) => reportln("        " + ste.toString, "error"))
    
    reportln("<==========================>","error");
    closeLogs;
    throw new Exception(str);
  }
  def error(e : Exception){
    reportln("<====== FATAL ERROR! ======>","error");
    reportln("----------------------------","error");
    reportln("     Exception message: \"" + e.toString + "\"","error");
    reportln("     Stack Trace:","error");
    val stackTrace = e.getStackTrace;
    stackTrace.map((ste) => reportln("        " + ste.toString, "error"))
    
    reportln("<==========================>","error");
    closeLogs;
    throw e;
  }
  
  def getWarningAndNoticeTallies(indent : String = "   ", subIndent : String = "  ") : Seq[String] = {
    Seq[String](indent+"---------------")++
    Seq[String](indent+"Warnings So Far:")++
      warningCount.keySet.toSeq.sorted.map(x => indent+subIndent+warningCount(x)+": "+x) ++ 
    Seq[String](indent+"Notices So Far:")++
      noticeCount.keySet.toSeq.sorted.map(x => indent+subIndent+noticeCount(x)+": "+x)++
    (if(tallies.size > 0){
      Seq[String](indent+"Counts:")++
      tallies.keySet.toSeq.sorted.map(x => indent+subIndent+tallies(x)+": "+x)
    } else {
       Seq[String]()
    }) ++
    (if(tallyFloat.size > 0){
      Seq[String](indent+"Counts:")++
      tallyFloat.keySet.toSeq.sorted.map(x => indent+subIndent+tallyFloat(x)+": "+x)
    } else {
       Seq[String]()
    }) ++
    Seq[String](indent+"---------------")
  }
  def getWarningAndNoticeTalliesTable(delim : String = "\t") : Seq[String] = {
    if( talliesCol2.size > 0 ){
      warningCount.keySet.toSeq.sorted.map(x => x+delim+"WARNING"+delim+warningCount(x)+delim+".") ++ 
      noticeCount.keySet.toSeq.sorted.map(x => x+delim+"NOTICE"+delim+noticeCount(x)+delim+".")++
      (tallies.keySet).toSeq.sorted.map(x => x+delim+"TALLY"+delim+tallies(x)+delim+talliesCol2.getOrElse(x,"."))
    } else if(tallyFloat.size > 0){
      warningCount.keySet.toSeq.sorted.map(x => x+delim+"WARNING"+delim+warningCount(x)+delim+".") ++ 
      noticeCount.keySet.toSeq.sorted.map(x => x+delim+"NOTICE"+delim+noticeCount(x)+delim+".")++
      (tallies.keySet ++ tallyFloat.keySet).toSeq.sorted.map(x => x+delim+"TALLY"+delim+tallyFloat(x)+delim+tallies.getOrElse(x,"."))
    } else {
      warningCount.keySet.toSeq.sorted.map(x => x+delim+"WARNING"+delim+warningCount(x)) ++ 
      noticeCount.keySet.toSeq.sorted.map(x => x+delim+"NOTICE"+delim+noticeCount(x))++
      tallies.keySet.toSeq.sorted.map(x => x+delim+"TALLY"+delim+tallies(x))
    }
  }
  
  
  def closeLogs() {
    if(! warningCount.keys.isEmpty){
      reportln("<------->","warn");
      reportln("   WARNING: "+warningCount.keySet.map(warningCount(_)).sum+" Warnings Thrown:","warn");
      warningCount.keySet.foreach(x => {
        reportln("   "+warningCount(x)+"\t"+x,"warn");
      })
      reportln("<------->","warn");
    }
    if(! noticeCount.keys.isEmpty){
      reportln("<------->","note");
      reportln("   Note: "+noticeCount.keySet.map(noticeCount(_)).sum+" Notices Thrown:","note");
      noticeCount.keySet.foreach(x => {
        reportln("   "+noticeCount(x)+"\t"+x,"note");
      })
      reportln("<------->","note");
    }    
    loggers.map((logger) => logger.close);
    loggers = loggers.filter( lgr => {
      val isConsole = lgr match {
        case ConsoleReportLogger(vrb) => true;
        case ErrConsoleReportLogger(vrb) => true;
        case _ => false;
      }
      isConsole;
    })
  }
  
  var PROGRESS_REPORT_FUNCTIONS : Vector[(Int) => String] = Vector();
  
  def getProgressReportStrings(i : Int) : Vector[String] = {
    PROGRESS_REPORT_FUNCTIONS.map{f => {
      f(i)
    }}
  }
  def addProgressReportFunction(f : (Int) => String){
    PROGRESS_REPORT_FUNCTIONS = PROGRESS_REPORT_FUNCTIONS :+ f;
  }
  def clearProgressReportFunctions(){
    PROGRESS_REPORT_FUNCTIONS = Vector()
  }
}
