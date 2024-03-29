package internalUtils

import internalUtils.Reporter._;
import internalUtils.stdUtils._;

//import scala.reflect.runtime.universe._


object commandLineUI {

  case class UserManualBlock( lines : Seq[String],
                              title : Option[String] = None, subtitle : Option[String] = None,
                              level : Int = 1, 
                              indentTitle : Int = 4, indentBlock : Int = 8, 
                              indentFirst:Int= 8, 
                              mdIndentBlock : Int = 8,
                              mdIndentFirst : Int = 4,
                              titleIndentChar : String = " ", firstLineIndentChar : String = " ", indentChar : String = " ",
                              mdCaret : Boolean = false,
                              mdBold : Boolean = false,
                              hiddenInPlain : Boolean = false,
                              category : String = "Misc"){
    def getBlockString(lineLen : Int = internalUtils.commandLineUI.CLUI_CONSOLE_LINE_WIDTH,
                       baseIndent : Int = 0) : String = {
      title.map{ t => "\n"+repString(titleIndentChar,baseIndent + indentTitle)+t }.getOrElse("") + 
        subtitle.map{ t => ": "+t }.getOrElse("") + 
        lines.zipWithIndex.map{ case (line,i) => {
        //wrapLineWithIndent(line,lineLen,4)
        wrapSimpleLineWithIndent_staggered(line, width = lineLen, 
                                           indent = repString(indentChar,baseIndent + indentBlock), 
                                           firstLineIndent = repString(firstLineIndentChar,baseIndent + indentFirst));
      }}.mkString("\n");
      /*
      
      title.map{case (lmrt,lmrd) => {
        "" + (lmrt match {
          case Some(tx) => "### "+escapeToMarkdown(tx)+"\n\n";
          case None => "";
        }) + wrapLineWithIndent(escapeToMarkdown(lmrd),internalUtils.commandLineUI.CLUI_CONSOLE_LINE_WIDTH,0)+"\n"
      }}.mkString("\n")+*/
    }
    def getMarkdownString(lineLen : Int = internalUtils.commandLineUI.MD_CONSOLE_LINE_WIDTH,
                          baseLevel : Int = 0) : String = {
      val LPrefS = if(mdCaret) "> " else "";
      val LPrefE = if(mdCaret) "\n" else "";
      //val TLn = title.map{ tx => "\n"+repString("#",baseLevel + level)+" "+escapeToMarkdown(tx)+"\n\n" }.getOrElse("") 
      
      title.map{ tx => "\n"+repString("#",baseLevel + level)+" "+escapeToMarkdown(tx)+"\n\n" }.getOrElse("") + 
      subtitle.map{ tx => "> "+" "+escapeToMarkdown(tx)+"\n\n" }.getOrElse("") + 
      lines.zipWithIndex.map{ case (line,i) => {
        //wrapLineWithIndent(line,lineLen,4)
        wrapSimpleLineWithIndent_staggered(line, width = lineLen, 
                                           indent = repString(" ",mdIndentBlock), 
                                           firstLineIndent = repString(" ",mdIndentFirst));
      }}.mkString("\n");
      
      /*lines.map{line => {
        //wrapLineWithIndent(escapeToMarkdown(line),lineLen, mdBlockIndent );
        LPrefS+escapeToMarkdown(line)+LPrefE
      }}.mkString("\n");*/
    }
    
  }
  
  

  abstract class StringParser[T]{
    def parse(s : String)(respectQuotes : Boolean, stripQuotes : Boolean) : T;
    def argType : String;
    def unsetValue : T;
  }
  implicit object stringStringParser extends StringParser[String]{
    def parse(s : String)(respectQuotes : Boolean = true, stripQuotes : Boolean = true) : String = {
      (if(stripQuotes){ stdUtils.cleanQuotes(s) }else s);
    }
    def argType : String = "String";
    def unsetValue : String = "";
  }
  implicit object intStringParser extends StringParser[Int]{
    def parse(s : String)(respectQuotes : Boolean = true, stripQuotes : Boolean = true) : Int = {
      string2int((if(stripQuotes){ stdUtils.cleanQuotes(s) }else s));
    }
    def argType : String = "Int";
    def unsetValue : Int = -1;
  }
  implicit object doubleStringParser extends StringParser[Double]{
    def parse(s : String)(respectQuotes : Boolean = true, stripQuotes : Boolean = true) : Double = {
      string2double((if(stripQuotes){ stdUtils.cleanQuotes(s) }else s));
    }
    def argType : String = "Double";
    def unsetValue : Double = -1;
  }
  implicit object floatStringParser extends StringParser[Float]{
    def parse(s : String)(respectQuotes : Boolean = true, stripQuotes : Boolean = true) : Float = {
      string2float((if(stripQuotes){ stdUtils.cleanQuotes(s) }else s));
    }
    def argType : String = "Float";
    def unsetValue : Float = -1;
  }
  implicit object commaListStringParser extends StringParser[List[String]]{
    def parse(s : String)(respectQuotes : Boolean = true, stripQuotes : Boolean = true) : List[String] = {
      (if(stripQuotes){ stdUtils.cleanQuotes(s) }else s).split((if(respectQuotes) ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)" else ",")).toList;
    }
    def argType : String = "CommaDelimitedListOfStrings";
    def unsetValue : List[String] = List();
  }
  implicit object commaListDoubleParser extends StringParser[List[Double]]{
    def parse(s : String)(respectQuotes : Boolean = true, stripQuotes : Boolean = true) : List[Double] = {
      (if(stripQuotes){ stdUtils.cleanQuotes(s) }else s).split((if(respectQuotes) ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)" else ",")).map(string2double(_)).toList;
    }
    def argType : String = "CommaDelimitedListOfDoubles";
    def unsetValue : List[Double] = List();
  }
  implicit object commaListIntParser extends StringParser[List[Int]]{
    def parse(s : String)(respectQuotes : Boolean = true, stripQuotes : Boolean = true) : List[Int] = {
      (if(stripQuotes){ stdUtils.cleanQuotes(s) }else s).split((if(respectQuotes) ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)" else ",")).map(string2int(_)).toList;
    }
    def argType : String = "CommaDelimitedListOfDoubles";
    def unsetValue : List[Int] = List();
  }
  implicit object commaListFloatParser extends StringParser[List[Float]]{
    def parse(s : String)(respectQuotes : Boolean = true, stripQuotes : Boolean = true) : List[Float] = {
      (if(stripQuotes){ stdUtils.cleanQuotes(s) }else s).split((if(respectQuotes) ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)" else ",")).map(string2float(_)).toList;
    }
    def argType : String = "CommaDelimitedListOfDoubles";
    def unsetValue : List[Float] = List();
  }
  
  final val HELP_COMMAND_LIST : List[String] = List();
  final val MANUAL_COMMAND_LIST : List[String] = List("?","'?'", "\"?\"","man","-man","--man", "help", "-help", "--help");
  
  var CLUI_CONSOLE_LINE_WIDTH = 68;
  var MD_CONSOLE_LINE_WIDTH = 102;
  
  final val ALWAYS_DEBUG_MODE = true;
  final val DEBUG_MODE_FLAG = "--debug";
  
  val DEFAULT_QUICK_SYNOPSIS = "<Synopsis not written>";
  val DEFAULT_SYNOPSIS = "<Synopsis not written>";
  val DEFAULT_DESCRIPTION = "<Description not written>";
  val DEFAULT_AUTHOR = List("Stephen W. Hartley, Ph.D. stephen.hartley (at nih dot gov)");
  val DEFAULT_LEGAL =  
List(
"Written 2017-2019 by Stephen Hartley, PhD ",
"National Cancer Institute (NCI), Division of Cancer Epidemiology and Genetics (DCEG), Human Genetics Program",
"As a work of the United States Government, this software package and all related documentation and information is in the public domain within the United States. Additionally, the National Institutes of Health and the National Cancer Institute waives copyright and related rights in the work worldwide through the CC0 1.0 Universal Public Domain Dedication (which can be found at https://creativecommons.org/publicdomain/zero/1.0/).",
"Although all reasonable efforts have been taken to ensure the accuracy and reliability of the software and data, the National Human Genome Research Institute (NHGRI), the National Cancer Institute (NCI) and the U.S. Government does not and cannot warrant the performance or results that may be obtained by using this software or data. NHGRI, NCI and the U.S. Government disclaims all warranties as to performance, merchantability or fitness for any particular purpose.",
"In work or products derived from this material, proper attribution of the authors as the source of the software or data may be made using \"NCI Division of Cancer Epidemiology and Genetics, Human Genetics Program\" as the citation.",
"This package uses (but is not derived from) several externally-developed, open-source libraries which have been distributed under various open-source licenses. vArmyKnife is distributed with compiled versions of these packages.",
"Additional License information can be accessed using the command:",
"    vArmyKnife --help LICENSES",
"And can also found in the distributed source code in:",
"    src/main/resources/library.LICENSES.txt");
   
  //type Command = ((Array[String]) => Unit);

  //WARNING: STRIPQUOTES IS CURRENTLY NONFUNCTIONAL!
  
  abstract class CommandLineRunUtil {
    val parser : CommandLineArgParser;
    def priority : Int = 255;
    def run(args : Array[String]);
    
    val isAlpha : Boolean = false;
    val isBeta : Boolean = false;
    
  }
  
  final val ALPHA_WARNING = "UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!";
  final val BETA_WARNING = "WARNING: THIS UTILITY IS AN UNTESTED BETA, AND MAY CONTAIN MAJOR FLAWS. NOT FOR PRODUCTION USE! USE AT YOUR OWN RISK!"
  
  /*final val helpCommandList : Map[String, () => CommandLineRunUtil] = 
    Map(
           ("'?'" -> (() => new helpDocs())),
           ("\"?\"" -> (() => new helpDocs())),
           ("?" -> (() => new helpDocs())),
           ("help" -> (() => new helpDocs())),
           ("man" -> (() => new helpDocs())),
           ("--help" -> (() => new helpDocs())),
           ("--man" -> (() => new helpDocs())),
           ("-help" -> (() => new helpDocs())),
           ("-man" -> (() => new helpDocs()))
       );*/
  
  
  //
  //command: the name of the command used to execute the utility
  //quickSynopsis: a short (less than 48-char) description of what the utility does.
  //synopsis: DEPRECIATED. The synopsis is now generated automatically.
  //description: a multi-line or even multi-paragraph description of the utility.
  
  val CLUI_UNIVERSAL_ARGS : List[Argument[Any]] = 
                    new UnaryArgument( name = "verbose",
                                         arg = List("--verbose"), // name of value
                                         argDesc = "Flag to indicate that debugging information and extra progress information should be sent to stderr." // description
                                       ) ::
                    new UnaryArgument( name = "quiet",
                                         arg = List("--quiet","-s"), // name of value
                                         argDesc = "Flag to indicate that only errors and warnings should be sent to stderr." // description
                                       ) :: 
                    //new UnaryArgument( name = "ignoreUnknownParams",
                    //                     arg = List("--ignoreUnknownParams"), // name of value
                    //                     argDesc = "Flag to indicate that unrecognized parameters will be ignored." // description
                    //                   ) :: 
                    new BinaryArgument[String](name = "universalTagPrefix",
                                           arg = List("--universalTagPrefix"),  
                                           valueName = "VAK_", 
                                           argDesc = "Set the universal tag prefix for all vArmyKnife INFO and FORMAT tags. By default it is VAK_. Warning: if you change this at any point, then all subsequent vArmyKnife commands may need to be changed to match, as vArmyKnife sometimes expects its own tag formatting.",
                                           defaultValue = Some("VAK_")
                                           ).meta(false,"Preprocessing") ::
                    new ParameterFileArgument( name = "PARAMFILE",
                                               arg = List("--paramFile"),
                                               valueName = "paramFile.txt",
                                               argDesc = "A file containing additional parameters and options. Each parameter must begin with a dash. Leading whitespace will be ignored, and newlines preceded by a backslash are similarly ignored. "+
                                                          "Lines that begin with a pound sign will be skipped. "+
                                                          "Trailing parameters (infile, outfile, etc) CANNOT be defined using a parameter file."+
                                                          ""+
                                                          "").meta(false,"Other Inputs") ::
                    new UnaryArgument( name = "debugMode",
                                         arg = List("--debugMode"), // name of value
                                         argDesc = "Flag to indicate that much more debugging information should be sent to stderr." // description
                                       ) :: 
                    new BinaryOptionArgument[String](
                                         name = "tallyFile", 
                                         arg = List("--tallyFile"),
                                         valueName = "file.txt",
                                         argDesc =  "Write a file with a table containing counts for all tallies, warnings and notices reported during the run."
                                        ).meta(false,"Annotation") :: 
                    new BinaryOptionArgument[String](
                                         name = "createRunningFile", 
                                         arg = List("--createRunningFile"), 
                                         valueName = "filename.txt",  
                                         argDesc =  "A file to create when this utility starts, to be deleted on a clean exit. The file WILL be deleted even if errors are caught. It will only remain if uncaught errors are thrown or if the process is killed externally."
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "successfulCompletionFile", 
                                         arg = List("--successfulCompletionFile"), 
                                         valueName = "filename.txt",  
                                         argDesc =  "A file to create if and when this utility successfully completes without fatal errors."
                                        ) ::
                    new BinaryOptionArgument[String](
                                         name = "warnCompletionFile", 
                                         arg = List("--warnCompletionFile"), 
                                         valueName = "filename.txt",  
                                         argDesc =  "NOT YET IMPLEMENTED: A file to create if and when this utility successfully completes with WARNINGS OR ERRORS."
                                        ).meta(true,"INCOMPLETE") ::
                    new BinaryOptionArgument[String](
                                         name = "errorCompletionFile", 
                                         arg = List("--errorCompletionFile"), 
                                         valueName = "filename.txt",  
                                         argDesc =  "NOT YET IMPLEMENTED: A file to create if and when this utility successfully completes with caught ERRORS. Note that if the utility is killed externally or if errors are thrown to the JVM, this file will NOT be created."
                                        ).meta(true,"INCOMPLETE") ::
                                       List();
   
  
  
  class CommandLineArgParser(command : String, quickSynopsis : String, synopsis : String, description : String,  
                             argList : List[Argument[Any]], 
                             authors : List[String] = DEFAULT_AUTHOR, legal : List[String] = DEFAULT_LEGAL, 
                             manualExtras : String = "", 
                             markdownManualExtras : String = "",
                             aliases : Seq[String] = Seq[String]()) {
    val getAliases : Seq[String] = aliases;
    def getCommandString : String = command;
    //private var argMap = Map.empty[String,(Type, Any)];
    //private def registerArg[T](name : String, item : T)(implicit t : TypeTag[T]){
    //  argMap = argMap.updated(name, typeOf[T] -> item);
    //}
    
    //private var argMap = Map.empty[String,(Type, Any)];
    //private def registerArg[T](name : String, item : T)(implicit t : TypeTag[T]){
    //  argMap = argMap.updated(name, typeOf[T] -> item);
    //}
    
    //private 
    def open(){
      this.get[Option[String]]("createRunningFile") match {
        case Some(f) => {
            fileUtils.createDummyFile(f=f,
                      message  = "# Note: if this file EXISTS, then either a job is currently running, or else the job crashed.",
                      existsWarn= "Warning: Run File Already Exists! Is this a rerun?");
        }
        case None => {
          //do nothing
        }
      }
      
      
    }
    def close(){
      
      runner.runner.RUNNER_EXECUTION_ENDTIME_DATE = Some(java.util.Calendar.getInstance().getTime())
      runner.runner.RUNNER_EXECUTION_ENDTIME_MILLIS = Some(runner.runner.RUNNER_EXECUTION_ENDTIME_DATE.get.getTime());
      runner.runner.RUNNER_EXECUTION_ENDTIME_STRING = Some(runner.runner.RUNNER_EXECUTION_ENDTIME_DATE.get.toString());
      runner.runner.RUNNER_EXECUTION_ENDTIME_SIMPLESTRING = Some(internalUtils.stdUtils.getDateAndTimeStringFromDate(runner.runner.RUNNER_EXECUTION_ENDTIME_DATE.get));

      reportln("Closing logfiles and logstreams...","note")
      this.get[Option[String]]("tallyFile").foreach{ tt => {
          val w = fileUtils.openWriter(tt)
          reportln("Writing tallyfile: \""+tt+"\"","note")
          getWarningAndNoticeTalliesTable("\t").foreach{ss => {
            w.write(ss+"\n")
          }}
          
          w.close();
      }}
      
      if( internalUtils.optionHolder.OPTION_VERBOSE ){
          reportln("<--> NOTICES <-->","note")
          getWarningAndNoticeTalliesTable("\t").foreach{ss => {
            reportln(ss,"debug")
          }}
          reportln("<--> NOTICES <-->","note")

      }
      
      this.get[Option[String]]("successfulCompletionFile") match {
        case Some(f) => {
            val runtimesec = (runner.runner.RUNNER_EXECUTION_STARTTIME_MILLIS - runner.runner.RUNNER_EXECUTION_ENDTIME_MILLIS.getOrElse(runner.runner.RUNNER_EXECUTION_STARTTIME_MILLIS)) / 1000000;
            fileUtils.createDummyFile(f=f,
                      message  = Seq("# Note: if this file EXISTS, then the job completed without fatal errors.",
                                     "STARTTIME_MILLIS\t"+runner.runner.RUNNER_EXECUTION_STARTTIME_MILLIS,
                                     "ENDTIME_MILLIS\t"+runner.runner.RUNNER_EXECUTION_ENDTIME_MILLIS.getOrElse(runner.runner.RUNNER_EXECUTION_STARTTIME_MILLIS),
                                     "STARTTIME\t"+runner.runner.RUNNER_EXECUTION_STARTTIME_SIMPLESTRING,
                                     "ENDTIME\t"+runner.runner.RUNNER_EXECUTION_ENDTIME_SIMPLESTRING,
                                     "RUNTIME_SEC\t"+runtimesec
                                     ).mkString("\n"),
                      existsWarn= "Warning: Completion File Already Exists! Is this a rerun?");
        }
        case None => {
          //do nothing
        }
      }
      this.get[Option[String]]("createRunningFile") match {
        case Some(f) => {
            val file = new java.io.File(f);
            if(! file.exists()) warning("WARNING WARNING WARNING: RUNNING FILE NO LONGER EXISTS!!! Multiple concurrent runs using the same run file?","FileDoesNotExist",-1);
            else {
              file.delete();
            }
        }
        case None => {
          //do nothing
        }
      }
    }
    
    private def filterOutFinalArgs[T](arg : Argument[T]) : Boolean = {
      arg match {
        case a : FinalArgument[T] => true;
        case a : Any => false;
      }
    }
    val finalArgList = argList.filter(filterOutFinalArgs);
    val paramArgList = argList.filterNot(filterOutFinalArgs);
    
    def get[T](key : String) : T = {
      argList.find(arg => arg.getName() == key) match {
        case Some(arg) => {
          val s = arg.getValue();
          return s.asInstanceOf[T];
        }
        case None => {
          error("FATAL INTERNAL ERROR: IMPOSSIBLE STATE! parameter "+key +" does not exist!");
          return None.get;
        }
      }
    }
    //def get[T](key : String)(implicit m : TypeTag[T]): T = {
    //  val (t,s) = argMap(key)
    //  s.asInstanceOf[T];
    //  //getArgOption[T](key).get
    //}
    
    def getQuickSynopsis : String = quickSynopsis;
    def getDescription : String = description;
    def getSynopsis : String = description;
    
    //def getArgOption[T](key : String)(implicit m : TypeTag[T]): Option[T] = {
    //  argMap.get(key).flatMap{
    //    case (t, s) => if (t <:< typeOf[T]) Some(s.asInstanceOf[T]) else {error("FATAL INTERNAL ERROR: PARAMETER " + key + " OF WRONG TYPE! Found: " + t + " expected: " + typeOf[T]); None};
    //  }
    //}

    //case (t, s) => if (t <:< typeOf[T]) Some(s.asInstanceOf[T]) else {error("FATAL INTERNAL ERROR: PARAMETER " + key + " OF WRONG TYPE!"); None};
    def parseArguments(args : List[String], debugMode : Boolean = internalUtils.optionHolder.OPTION_verboseInputInfo) : Boolean = {
      try {
      if(args.length < 1){
        reportShortHelp();
        false;
      } else if(MANUAL_COMMAND_LIST.contains(args(0))){
        reportManual();
        false;
      } else if(HELP_COMMAND_LIST.contains(args(0))){
        reportShortHelp();
        false;
      } else {
        parseArgs_master(args.toList, debugMode);
        this.open();
        true;
      }
      } catch {
        case e : Exception => {
          reportln("Syntax Error? Syntax must be:","warn");
          reportShortHelp();
          reportln("For more information, use option --man","warn");
          reportln("Error is:","warn");
          throw e;
        }
      }
    }
    
    def reportManual(verb : String = "output") {
      report(getManual(),verb);
    }
    def getManual() : String = {
      val sb = new StringBuilder("");
      
      sb.append("NAME\n");
      sb.append("	" + command + "\n");
      if(aliases.length > 0){
        sb.append("   ALIASES:\n");
        aliases.foreach{ aa => {
          sb.append("	    " + aa + "\n");
        }}
      }
      sb.append("   Version: " + runner.runner.UTIL_VERSION + " (Updated " + runner.runner.UTIL_COMPILE_DATE + ")\n\n");
      sb.append("USAGE\n"); 
      sb.append(wrapLinesWithIndent(getShortHelp(),CLUI_CONSOLE_LINE_WIDTH,"        ", false).substring(4)+"\n");
      sb.append("\n");
      sb.append("DESCRIPTION:\n");
      sb.append(wrapLinesWithIndent(description,CLUI_CONSOLE_LINE_WIDTH,"    ",false)+"\n");
      //lineseq2string(wrapLinesWithIndent(DESCRIPTION, internalUtils.commandLineUI.CLUI_CONSOLE_LINE_WIDTH, "    ", false))
      sb.append("\n");
      sb.append("REQUIRED ARGUMENTS:\n");
      for(arg <- argList.filter(_.argIsMandatory)){
        sb.append(arg.getFullDescription+"\n");
      }
      sb.append("\n");
      
      sb.append("OPTIONS:\n");
      
      val filtArgs = if(optionHolder.OPTION_DEBUGMODE){
        argList.filter(! _.argIsMandatory)
      } else {
        argList.filter{ a => ! a.alpha }.filter(! _.argIsMandatory)
      }
      
      val cats = filtArgs.flatMap{ a => a.catName }.distinct.sorted
      if(cats.length > 0){
        val filtPriorityList : List[(String,Int)] = cats.map{ currCat => {
          (currCat, argList.find{ aa => aa.catPriority.isDefined && aa.catName.isDefined && aa.catName.getOrElse("") == currCat }.flatMap{ aa => aa.catPriority }.getOrElse(Int.MaxValue) )
        }}.sortBy{ case (currCat, catPriority) => (catPriority, currCat) }
        
        for( (currCat, currPrior) <- filtPriorityList){
        //for(currCat <- cats){
          sb.append(" "+currCat+":\n");
          for(arg <- filtArgs.filter{a => a.catName == Some(currCat)}){
            sb.append(arg.getFullDescription+"\n");
            sb.append("\n");
          }
        }
        sb.append(" OTHER OPTIONS:\n");
        for(arg <- filtArgs.filter{a => a.catName.isEmpty}){
          sb.append(arg.getFullDescription+"\n");
          sb.append("\n");
        }
      } else {
        for(arg <- filtArgs){
          sb.append(arg.getFullDescription+"\n");
          sb.append("\n");
        }
      }

      sb.append(manualExtras);
      
      sb.append("AUTHORS:\n");
      sb.append(lineseq2string(wrapLinesWithIndent(authors,CLUI_CONSOLE_LINE_WIDTH,"    ",false)) + "\n");
      
      sb.append("LEGAL:\n");
      sb.append(lineseq2string(wrapLinesWithIndent(legal,CLUI_CONSOLE_LINE_WIDTH,"    ",false)) + "\n");
      
      return sb.toString;
    }
    
    def getMarkdownManual_ARGS() : String = {
      val sb = new StringBuilder("");
      sb.append("## REQUIRED ARGUMENTS:\n");
      for(arg <- argList.filter(_.argIsMandatory)){
        sb.append(arg.getFullMarkdownDescription+"\n");
      }
      sb.append("\n");
      
      sb.append("## OPTIONAL ARGUMENTS:\n");
      
      val filtArgs = if(optionHolder.OPTION_DEBUGMODE){
        argList.filter(! _.argIsMandatory)
      } else {
        argList.filter{ a => ! a.alpha }.filter(! _.argIsMandatory)
      }
      val cats = filtArgs.flatMap{ a => a.catName }.distinct.sorted
      if(cats.length > 0){
        val filtPriorityList : List[(String,Int)] = cats.map{ currCat => {
          (currCat, argList.find{ aa => aa.catPriority.isDefined && aa.catName.isDefined && aa.catName.getOrElse("") == currCat }.flatMap{ aa => aa.catPriority }.getOrElse(Int.MaxValue) )
        }}.sortBy{ case (currCat, catPriority) => (catPriority, currCat) }
        
        for( (currCat, currPrior) <- filtPriorityList){
          sb.append("### "+currCat+":\n");
          for(arg <- filtArgs.filter{a => a.catName == Some(currCat)}){
            sb.append(arg.getFullMarkdownDescription);
          }
        } 
        sb.append("### OTHER OPTIONS:\n");
        for(arg <- filtArgs.filter{a => a.catName.isEmpty}){
          sb.append(arg.getFullMarkdownDescription);
        }
      } else {
        for( arg <- filtArgs ){
          sb.append(arg.getFullMarkdownDescription);
        }
      }
      sb.toString;
    }
    
    def getMarkdownManual(backToIndexPath : String = "index.html") : String = {
      val sb = new StringBuilder("");
      
      sb.append("# "+runner.runner.UTILITY_TITLE+"\n");
      sb.append("> Version " + runner.runner.UTIL_VERSION + " (Updated " + runner.runner.UTIL_COMPILE_DATE +")\n\n");
      sb.append("> ([back to main](../index.html)) ([back to java-utility help]("+backToIndexPath+"))\n\n");
      sb.append("## Help for vArmyKnife command \""+escapeToMarkdown(command)+"\"\n\n");
      if(aliases.length > 0){
        sb.append("ALIASES: "+aliases.mkString(",")+"\n");
      }
 
      sb.append("## USAGE:\n\n"); 
      sb.append(""+getShortHelp()+"\n\n");
      sb.append("## DESCRIPTION:\n\n");
      sb.append(escapeToMarkdown(description)+"\n\n");

      sb.append( getMarkdownManual_ARGS() )
      /*
      for(arg <- argList.filter(! _.argIsMandatory)){
        sb.append( arg.getFullMarkdownDescription);
      }*/
      sb.append(markdownManualExtras);
      
      sb.append("## AUTHORS:\n\n");
      sb.append(escapeToMarkdown(authors.mkString(", "))+ "\n\n");
      
      sb.append("## LEGAL:\n\n");
      sb.append(escapeToMarkdown(legal.mkString(" "))+ "\n\n");
      
      return sb.toString;
    }
    
    def SPECIAL_getMainManualMarkdown() : String = {
      val sb = new StringBuilder("");
      
      sb.append("> Version " + runner.runner.UTIL_VERSION + " (Updated " + runner.runner.UTIL_COMPILE_DATE +")\n\n");
 
      sb.append("# vArmyKnife: \n\n");
      sb.append("vArmyKnife is A multipurpose software tool for manipulating, processing, filtering, and annotating VCF files and similar variant data formats.\n\n");
      sb.append("vArmyKnife reads VCF files and passes variants along a chain of sequential functions, each modifying the VCF in turn. "+
                "It is designed to be fast, efficient, and flexible, and is intended to allow numerous processing steps to be merged into a single step. \n");
      
      sb.append("## Installation: \n\n");
      sb.append("The easiest way to install the most recent version of vArmyKnife is to clone from github: \n\n");
      sb.append("    git clone https://github.com/hartleys/vArmyKnife\n\n");
      sb.append("If you have root access you can then copy the two executable files 'vArmyKnife.jar' and 'varmyknife' to /usr/local/bin:\n\n");
      sb.append("    cd vArmyKnife\n");
      sb.append("    cp vArmyKnife.jar /usr/local/bin\n");
      sb.append("    cp varmyknife /usr/local/bin\n\n");
      sb.append("OR you can just add the directory to the PATH:\n");
      sb.append("    export PATH=\"/your/path/to/vArmyKnife:${PATH}\"\n\n");
      sb.append("(If don't want to have to execute the above line every time, you can add it to your .bashrc and/or .bash_profile scripts.)\n");

      sb.append("# USAGE AND GENERAL SYNTAX:\n\n");
      
      sb.append("The primary method is \"walkVcf\". Most runs will look something like this:\n\n");
      sb.append("    varmyknife [java_options] walkVcf [options] \\"+"\n");
      sb.append("         --FCN \"fcnName1|stepName1|param1|param2\"\\"+"\n");
      sb.append("         --FCN \"fcnName2|stepName2|param1|param2\"\\"+"\n");
      sb.append("         ...etc...\\"+"\n");
      sb.append("         infile.vcf.gz outfile.vcf.gz"+"\n");
      sb.append(""+"\n\n");
      sb.append("Note: \"fcnName1\" and \"fcnName2\" are varmyknife functions, see the section below. "+"\n");
      sb.append("\"stepName1\" and \"stepName2\" are user-supplied names that are used to identify or describe the step. "+
                "Some functions use the stepName in some way. For example: function addInfo will create a new INFO field named stepName"+"\n");
      sb.append(""+"\n");
      sb.append("Either infile or outfile can generally be set to '-' to read from STDIN or STDOUT respectively. (Note: this cannot be combined with options that split the input or output, respectively. eg: --infileListInfix or --splitOutputByChrom)\n\n");
      sb.append("    varmyknife [java_options] walkVcf [options] \\"+"\n");
      sb.append("         --FCN \"fcnName1|stepName1|param1|param2\"\\"+"\n");
      sb.append("         infile.vcf.gz - | bgzip > outfile.vcf.gz"+"\n");
      sb.append(""+"\n");      
      sb.append("vArmyKnife also comes packaged with a few other tools that don't run on VCF files."+"\n");
      sb.append("To use these other secondary tools:"+"\n\n");
      sb.append("    varmyknife [java_options] "+"otherCommandName"+" [options]"+"\n");
      sb.append(""+"\n");

      sb.append(escapeToMarkdown(description)+"\n\n");

      sb.append( getMarkdownManual_ARGS() )
      /*
      for(arg <- argList.filter(! _.argIsMandatory)){
        sb.append( arg.getFullMarkdownDescription);
      }*/
      sb.append(markdownManualExtras);
      
      sb.append("# AUTHORS:\n\n");
      sb.append(escapeToMarkdown(authors.mkString(", "))+ "\n\n");
      
      sb.append("# LEGAL:\n\n");
      sb.append(escapeToMarkdown(legal.mkString(" "))+ "\n\n");
      
      return sb.toString;
    }
    
    private def sectionFormat(s : String) : String = {
      return(s);
    }
    private def sectionFormat(ss : Seq[String]) : String = {
      return(ss.foldLeft("")((soFar,s) => soFar + "\n" + s));
    }
    
    def reportShortHelp(verb : String = "output"){
      report(getShortHelp(),verb);
    }
    def getShortHelp() : String = {
      if(command == "walkVcf"){
      var sb = new StringBuilder();
      sb.append("GENERAL SYNTAX:\n\n");
      
      sb.append("    The main method is walkVcf:\n");
      sb.append("        varmyknife [java_options] "+"walkVcf [options] infile outfile"+"\n");
      sb.append("    which can also output to STDOUT: "+"\n");
      sb.append("        varmyknife [java_options] "+"walkVcf [options] infile - > outfile"+"\n");
      sb.append("    To use other secondary tools:"+"\n");
      sb.append("        varmyknife [java_options] "+"otherCommandName"+" [options]"+"\n");
      return sb.toString();
      
      } else {
      
      var sb = new StringBuilder();
      sb.append("GENERAL SYNTAX:\n\n");
      sb.append("    varmyknife [java_options] "+"--CMD "+command+" [options] "+argList.map(_.getShortSyntax()).filter(_ != "").mkString(" ")+"\n");
      return sb.toString();
      }
      //"    varmyknife [java options] " + command + " [options] " + argList.map(_.getShortSyntax()).filter(_ != "").mkString(" ")+"\n"+
      //"      or \n"+
      //"    java -jar "+runner.runner.Runner_ThisProgramsExecutableJarFileName+" [java options] " + command + " [options] " + argList.map(_.getShortSyntax()).filter(_ != "").mkString(" ")+"\n"+
         //argList.foldLeft[String]("")((soFar : String, curr : Argument[Any]) => soFar +" "+ curr.getShortSyntax()) +
       //  "\n"
         //getForMoreHelp();
    }
    
    def getForMoreHelp() : String = {
      "For more info, use:\nvarmyknife " + command + "--man"
    }
     
    def getMandatoryArgumentsHelp() : String = {
      val sb = new StringBuilder("");
      for(arg <- argList){
        arg match {
          case (a : BinaryArgument[Any]) => {
            sb.append("");
          }
          case (a : FinalArgument[Any]) =>{
            
          }
        }
      }
      return sb.toString();
    }
    
    private def parseArgs_master(inputArguments : List[String], debugMode : Boolean = internalUtils.optionHolder.OPTION_verboseInputInfo){
      if(inputArguments.length < finalArgList.length){
         throwSyntaxErrorMessage("Not enough arguments: Require at least " + finalArgList.length + " arguments!\nRequired syntax is:\n" + getShortHelp());
      }
      
      val (inputParamArgs, inputFinalArgs) = inputArguments.splitAt( inputArguments.length - finalArgList.length );
      
      reportln("INPUT_COMMAND("+command+")","note");
      
      for((p,arg) <- inputFinalArgs.zip(finalArgList)){
        arg.parse(List(p));
        if(debugMode) reportln("  INPUT_ARG("+arg.getName()+")="+arg(),"note");
      }
      parseParamArgs(inputParamArgs, debugMode);
      
      //Now check to make sure all mandatory parameters are set:
      argList.find(! _.isReady()) match {
        case Some(unreadyArg) =>{
          throwSyntaxErrorMessage("Mandatory argument is not set: " + unreadyArg.getShortSyntax());
        } 
        case None =>{
          //do nothing
        }
      }
      //////for(a <- argList){
      //////  registerArg(a.getName,a.getValue());
      //////}
    }
    
    //var IGNORE_UNKNOWN_PARAMS : Boolean = false

    private def parseParamArgs(inputArguments : List[String], debugMode : Boolean){
      if(inputArguments.length != 0){
        //if(inputArguments.exists(isArgument(_))){
          //if(isArgument(inputArguments.head)){
          //if(inputArguments.head == "--ignoreUnknownParams"){
          //    IGNORE_UNKNOWN_PARAMS = true;
          //    parseParamArgs(inputArguments.tail,debugMode);
          //} else {
            argList.find(_.isNamed(inputArguments.head)) match {
              case Some(arg) => {
                val remainder = arg.parse(inputArguments);
                //if(debugMode) reportln("  INPUT_ARG("+arg.getName()+")="+arg(),"note");
                parseParamArgs(remainder, debugMode);
              }
              case None => {
                //if(IGNORE_UNKNOWN_PARAMS){
                //  if(debugMode) reportln("  Ignoring INPUT_ARG("+arg.getName()+")="+arg(),"note");
                //} else {
                  throwSyntaxErrorMessage("Unrecognized Argument: " + inputArguments.head);
                //}
              }
            }
         //}
         // } else {
          // throwSyntaxErrorMessage("Unexpected string (not recognized as an option name or argument): " + inputArguments.head);
         // }
        //} else {
        //  throwSyntaxErrorMessage("Unexpected/unrecognized commands/options/arguments: " + inputArguments);
        //}
      }
    }
  }
  
  private def throwSyntaxErrorMessage(s : String){
     error("SYNTAX ERROR! "+ s);
  }
  private def looksLikeArgument(arg : String) : Boolean = {
      if(arg.length == 0) false;
      else if(arg.charAt(0) == '-'){
        if(arg.length == 1) false;
        else true;
      } else false;
  }
  
  abstract class Argument[+T] {
    
    def getName() : String;
    def getValue() : T;
    //def getType()(m : TypeTag[T]) : reflect.runtime.universe.Type;
    def isNamed(argName : String) : Boolean;
    //def setValue(t : Any) : Unit;
    def parse(args : List[String]) : List[String];
    def isReady() : Boolean;
    
    var alpha : Boolean = false;
    var catName : Option[String] = None
    //var priority : Option[Int] = None
    var catPriority : Option[Int] = None
    //setPriority : Int,
    def meta(isAlpha : Boolean, cat : String,  setCatPriority : Int) : Argument[T] = {
      alpha = isAlpha;
      catName = Some(cat);
      //priority = Some(setPriority)
      catPriority = Some(setCatPriority);
      return this
    }
    def meta(isAlpha : Boolean, cat : String) : Argument[T] = {
      alpha = isAlpha;
      catName = Some(cat);
      return this
    }
    def meta(isAlpha : Boolean) : Argument[T]  = {
      alpha = isAlpha
      return this
    }
    def meta(cat : String) : Argument[T]  = {
      catName = Some(cat);
      return this
    }
    
    def describe() : String;
    def getShortSyntax() : String;
    def getFullSyntax() : String;
    def argMasterType : String;
    def argSubType : String;
    def argType : String;
     
    def apply() : T = getValue();
    def getFullDescription() : String = {
      "    "+getFullSyntax()+"\n"+wrapLineWithIndent(describe(),CLUI_CONSOLE_LINE_WIDTH,8)+"\n        ("+argType+")";
    }
    def getFullMarkdownDescription() : String = {
      //"### "+escapeToMarkdown(getFullSyntax())+":\n\n"+escapeToMarkdown(describe())+ escapeToMarkdown(" ("+argType+")\n\n");
      "#### "+(getFullSyntax()).replaceAll("_","\\\\_")+":\n\n> "+(describe()).replaceAll("_","\\\\_")+ (" ("+argType+")\n\n").replaceAll("_","\\\\_");
    }
    
    def argIsMandatory : Boolean;
  }

  case class UnaryArgument(name : String, arg : List[String], argDesc : String, defaultValue : Boolean = false, isImportant : Boolean = false) extends Argument[Boolean] {
    def argIsMandatory = false;
    var value : Boolean = defaultValue;
    def getName = name;
    //def getType()(implicit m : TypeTag[Boolean]) = typeOf[Boolean];
    def describe() : String = {
      argDesc;
    }
    def getShortSyntax() : String = {
      if(isImportant) "["+arg(0)+"]";
      else "";
    }
    def getFullSyntax() : String = {
      arg(0);
    }
    def argMasterType() : String = "flag";
    def argSubType() : String = "";
    def argType() : String = "flag";
    
    def getValue() : Boolean = value;
    def isNamed(an : String) = {
      if(arg.exists(_ == an)) true;
      else {
        if(an.length > 1){
          val substr = an.substring(0,2);
          arg.exists(_ == substr);
        } else false;
      }
    }
    def setValue(t : Boolean) {
      value = t;
    }
    def isReady : Boolean = true;
    def parse(args : List[String]) : List[String] = {
//if(debugMode) reportln("  INPUT_ARG("+arg.getName()+")="+arg(),"note");
      if(internalUtils.optionHolder.OPTION_verboseInputInfo) reportln("   INPUT_ARG("+this.getName()+")=true","note");
      if(args.head.charAt(0) == '-'){
        if(args.head.charAt(1) == '-'){
          setValue(true);
          args.tail;
        } else {
          if(args.head.length == 2){
            setValue(true);
            args.tail;
          } else if(args.head.length < 2){
            throwSyntaxErrorMessage("?ErrorMessageNotAdded?");
            List();
          } else {            
            val newArg = "-" + args.head.substring(2);
            newArg :: args.tail;
          }
        }
      } else {
        throwSyntaxErrorMessage("?ErrorMessageNotAdded?");
        List();
      }
    }
  }
  val varPattern = java.util.regex.Pattern.compile("\\$\\{\\w+\\}");

  case class ParameterFileArgument(name : String, 
                                   arg: List[String], 
                                   valueName : String,  
                                   argDesc : String, 
                                   defaultValue : Option[String] = None, 
                                   isMandatory : Boolean = false, 
                                   isImportant : Boolean = false, 
                                   stripQuotes : Boolean = false) extends Argument[Option[String]] {
    def argIsMandatory = isMandatory;
    def getName = name;
    var value : Option[String] = defaultValue;
    
    def getShortSyntax() : String = {
      if(isMandatory) ""+arg(0)+" "+valueName+"";
      else if(isImportant) "["+arg(0)+" "+valueName+"]";
      else "";
    }
    def getFullSyntax() : String = {
      if(isMandatory) ""+arg(0)+" "+valueName+"";
      else ""+arg(0)+" "+valueName+"";
    }
    def argMasterType() : String = "monadic";
    def argSubType() : String = "paramFile";
    def argType() : String = argSubType();
    
    def describe() : String = {
      argDesc;
    }
    def getValue() : Option[String] = value;
    def isNamed(an : String) = arg.exists(_ == an);
    def setValue(t : Option[String]){
      value = t;
    }
    def isReady : Boolean = true;
    def parse(args : List[String]) : List[String] = {
      if(args.length < 2) throwSyntaxErrorMessage("Variable " + arg(0) + " not set to anything!");
      
      val argFile = args.tail.head;
      value = Some(argFile);
      if(internalUtils.optionHolder.OPTION_verboseInputInfo) reportln("   INPUT_ARG("+this.getName()+")="+argFile,"note");
      val parsedArgs = parseArgumentFile(argFile)
      parsedArgs ++ args.drop(2);
    }
    
    var internalVarMap = new scala.collection.mutable.AnyRefMap[String,String]();
    
    //val startVarPattern = java.util.regex.Pattern.compile(java.util.regex.Pattern.quote("${"));
    //val endVarPattern = java.util.regex.Pattern.compile(java.util.regex.Pattern.quote("}"));

    def expandString(s : String) : String = {
      if(s.contains('$')){
        var m = varPattern.matcher(s);
        var ss : String = s;
        while(m.find()){
          val varStringRaw = m.group();
          if(! varStringRaw.isDefinedAt(2)){
            error("Bad internal variable declaration in parameterFile. All lines must start with \"--\" or must contain a variable declaration of the form \"varName=varValue\".\n    Parse error encountered on line: \""+s+"\"");
          }
          val varString = varStringRaw.drop(2).init;
          val vv = internalVarMap.get(varString) match {
            case Some(vs) => {
              vs;
            }
            case None => {
              error("Variable not found (\""+varString+"\") in declaration line: \""+s+"\"");
              "";
            }
          }
          ss = m.replaceFirst(vv);
          m = varPattern.matcher(ss);
        }
        ss;
      } else {
        s;
      }
    }
    
    def parseArgumentFile(f : String) : List[String] = {
      var out = List[String]();
      
      val lines = internalUtils.fileUtils.getLinesSmartUnzip(f).map{s => s.replaceAll("""^\s+(?m)""","")}.filter{s => ! s.startsWith("#")}.buffered;
      if(! lines.hasNext){
        return out;
      }
      var curr = lines.head;
      while(lines.hasNext){
        curr = expandString(lines.next().replaceAll("""^\s+(?m)""",""));
        if(curr == ""){
          //do nothing, ignore blank lines.
        } else if(curr.startsWith("-")){
          val cells = curr.split("\\s+",2);
          out = out :+ cells.head;
          if(cells.isDefinedAt(1)){
            val buf = new StringBuilder
            if(cells.isDefinedAt(1)) buf ++= cells(1).replaceAll("[\\\\]$","")
            while(curr.endsWith("\\") && lines.hasNext){
              curr = expandString(lines.next());
              buf ++= curr.replaceAll("[\\\\]$","");
            }
            out = out :+ buf.toString();
          }
        //} else if(curr.startsWith("#")){
          //do nothing
        } else {
          val expr = curr.split("=",2);
          if(expr.length < 2){
            error("Parsing argument file "+f+"failed: all parameters must start with dash or be simple assignment expressions. Failed on line:\n   \""+curr+"\"");
          } else {
            reportln("        VAR: \""+expr(0)+"\" = \""+expr(1)+"\"","debug")
            val expandedExpr = expandString(expr(1));
            if(expandedExpr != expr(1)){
              reportln("        VAR: \""+expr(0)+"\" = \""+expandedExpr+"\"","debug")
            }
            internalVarMap += (expr(0),expandedExpr)
          }
        }
      }
      
      return out;
      
      /*getLinesSmartUnzip(f).foldLeft(List[String]()){ case (soFar,s) => {
        
      }};*/
    }
    
  }
  
  
  case class BinaryOptionArgument[T](name : String, arg: List[String], valueName : String,  argDesc : String,
                                     defaultValue : Option[T] = None, isMandatory : Boolean = false, 
                                     isImportant : Boolean = false, stripQuotes : Boolean = true, respectQuotes : Boolean = true)(implicit stringParser : StringParser[T]) extends Argument[Option[T]] {
    def argIsMandatory = isMandatory;
    def getName = name;
    var value : Option[T] = defaultValue;
    
    def getShortSyntax() : String = {
      if(isMandatory) ""+arg(0)+" "+valueName+"";
      else if(isImportant) "["+arg(0)+" "+valueName+"]";
      else "";
    }
    def getFullSyntax() : String = {
      if(isMandatory) ""+arg(0)+" "+valueName+"";
      else ""+arg(0)+" "+valueName+"";
    }
    def argMasterType() : String = "monadic";
    def argSubType() : String = stringParser.argType;
    def argType() : String = argSubType();
    
    def describe() : String = {
      argDesc;
    }
    def getValue() : Option[T] = value;
    def isNamed(an : String) = arg.exists(_ == an);
    def setValue(t : T){
      value = Some(t);
    }
    def isReady : Boolean = ! ((value.isEmpty) && (isMandatory));
    def parse(args : List[String]) : List[String] = {
      if(args.length < 2) throwSyntaxErrorMessage("Variable " + arg(0) + " not set to anything!");

      val valueString = args.tail.head;
      setValue(stringParser.parse(valueString)(respectQuotes=respectQuotes,stripQuotes=stripQuotes));
      if(internalUtils.optionHolder.OPTION_verboseInputInfo) reportln("   INPUT_ARG("+this.getName()+")="+value.toString(),"note");

      args.tail.tail;
    }
  }
  
  case class BinaryOptionListArgument[T](name : String, arg: List[String], valueName : String,  argDesc : String, 
                                         defaultValue : Option[List[T]] = None, isMandatory : Boolean = false, 
                                         isImportant : Boolean = false, stripQuotes : Boolean = true, respectQuotes : Boolean = true)(implicit stringParser : StringParser[List[T]]) extends Argument[Option[List[T]]] {
    def argIsMandatory = isMandatory;
    def getName = name;
    var value : Option[List[T]] = defaultValue;
    
    def getShortSyntax() : String = {
      if(isMandatory) ""+arg(0)+" "+valueName+"";
      else if(isImportant) "["+arg(0)+" "+valueName+"]";
      else "";
    }
    def getFullSyntax() : String = {
      if(isMandatory) ""+arg(0)+" "+valueName+"";
      else ""+arg(0)+" "+valueName+"";
    }
    def argMasterType() : String = "repeatable";
    def argSubType() : String = stringParser.argType;
    def argType() : String = argMasterType()+" "+argSubType()
    
    def describe() : String = {
      argDesc;
    }
    def getValue() : Option[List[T]] = value;
    def isNamed(an : String) = arg.exists(_ == an);
    def setValue(t : List[T]){
      value = Some(t);
    }
    def addToValue(t : List[T]){
      value match {
        case Some(v) => {
          value = Some(v ++ t)
        }
        case None => {
          value = Some(t);
        }
      }
    }
    def isReady : Boolean = ! ((value.isEmpty) && (isMandatory));
    def parse(args : List[String]) : List[String] = {
      if(args.length < 2) throwSyntaxErrorMessage("Variable " + arg(0) + " not set to anything!");
      val valueString = args.tail.head;
      //setValue(stringParser.parse(valueString));
      val addme = stringParser.parse(valueString)(respectQuotes=respectQuotes,stripQuotes=stripQuotes);
      addToValue(addme);
      if(internalUtils.optionHolder.OPTION_verboseInputInfo) reportln("   INPUT_ARG("+this.getName()+")="+addme.map{_.toString()}.mkString(","),"note");

      args.tail.tail;
    }
  }
  
  case class BinaryMonoToListArgument[T](name : String, arg: List[String], valueName : String,  argDesc : String, 
                                         defaultValue : List[T] = List[T](), isMandatory : Boolean = false, 
                                         isImportant : Boolean = false, stripQuotes : Boolean = true, respectQuotes : Boolean = true)(implicit stringParser : StringParser[T]) extends Argument[List[T]] {
    def argIsMandatory = isMandatory;
    def getName = name;
    var value : List[T] = defaultValue;
    
    def getShortSyntax() : String = {
      if(isMandatory) ""+arg(0)+" "+valueName+"";
      else if(isImportant) "["+arg(0)+" "+valueName+"]";
      else "";
    }
    def getFullSyntax() : String = {
      if(isMandatory) ""+arg(0)+" "+valueName+"";
      else ""+arg(0)+" "+valueName+"";
    }
    def argMasterType() : String = "repeatable";
    def argSubType() : String = stringParser.argType;
    def argType() : String = argMasterType()+" "+argSubType()
    
    def describe() : String = {
      argDesc;
    }
    def getValue() : List[T] = value;
    def isNamed(an : String) = arg.exists(_ == an);
    def setValue(t : List[T]){
      value = t
    }
    def addToValue(t : T){
      value = value ++ List[T](t);
    }
    def isReady : Boolean = ! ((value.isEmpty) && (isMandatory));
    def parse(args : List[String]) : List[String] = {
      if(args.length < 2) throwSyntaxErrorMessage("Variable " + arg(0) + " not set to anything!");
      val valueString = args.tail.head;
      if(internalUtils.optionHolder.OPTION_verboseInputInfo) reportln("   INPUT_ARG("+this.getName()+")+="+valueString,"note");
      //setValue(stringParser.parse(valueString));
      addToValue(stringParser.parse(valueString)(respectQuotes=respectQuotes,stripQuotes=stripQuotes));
      args.tail.tail;
    }
  }
  /*
  case class SectionedListArgument(name : String, arg: List[String], valueName : String,  argDesc : String, 
                                         isMandatory : Boolean = false, 
                                         subArgList : List[Argument[Option[String]]] = List(),
                                         isImportant : Boolean = false, stripQuotes : Boolean = true, respectQuotes : Boolean = true)(implicit stringParser : StringParser[T]) 
                                         extends Argument[scala.collection.mutable.Map[String,String]] {
    
    def argIsMandatory = isMandatory;
    def getName = name;
    var value : scala.collection.mutable.Map[String,String] = scala.collection.mutable.AnyRefMap[String,String]();
    
    def getShortSyntax() : String = {
      if(isMandatory) ""+arg(0)+" "+valueName+"";
      else if(isImportant) "["+arg(0)+" "+valueName+"]";
      else "";
    }
    def getFullSyntax() : String = {
      if(isMandatory) ""+arg(0)+" "+valueName+"";
      else ""+arg(0)+" "+valueName+"";
    }
    def argMasterType() : String = "repeatable";
    def argSubType() : String = stringParser.argType;
    def argType() : String = argMasterType()+" "+argSubType()
    
    def describe() : String = {
      argDesc;
    }
    def getValue() : scala.collection.mutable.Map[String,String] = value;
    def isNamed(an : String) = arg.exists(_ == an);
    def setValue(t : scala.collection.mutable.Map[String,String]){
      value = t
    }
    def addToValue(k : String,v : String){
      value(k) = v;
    }
    def isReady : Boolean = ! ((value.isEmpty) && (isMandatory));
    def parse(args : List[String]) : List[String] = {
      if(args.length < 2) throwSyntaxErrorMessage("Variable " + arg(0) + " not set to anything!");
      val valueString = args.tail.head;
      //setValue(stringParser.parse(valueString));
      //addToValue(stringParser.parse(valueString)(respectQuotes=respectQuotes,stripQuotes=stripQuotes));
      
      args.tail.tail;
    }
  }*/
  
  case class BinaryArgument[T](name : String, arg: List[String], valueName : String,  argDesc : String, defaultValue : Option[T] = None, isMandatory : Boolean = false, isImportant : Boolean = false, 
                               stripQuotes : Boolean = true, respectQuotes : Boolean = true)(implicit stringParser : StringParser[T]) extends Argument[T] {
    def argIsMandatory = isMandatory;
    def getName = name;
    //def getType()(implicit m : TypeTag[T]) : reflect.runtime.universe.Type = typeOf[T];
    var isSet = false;
    var hasDefault = false;
    var value : T = defaultValue match {
      case Some(t) => {isSet = true; hasDefault = true; t;} 
      case None => stringParser.unsetValue;
    }
    
    def getShortSyntax() : String = {
      if(isMandatory) ""+arg(0)+" "+valueName+"";
      else if(isImportant) "["+arg(0)+" "+valueName+"]";
      else "";
    }
    def getFullSyntax() : String = {
      ""+arg(0)+" "+valueName+"";
    }
    def argMasterType() : String = "monadic";
    def argSubType() : String = stringParser.argType;
    def argType() : String = argSubType();
    
    def describe() : String = {
      argDesc;
    }
    def getValue() : T = value;
    def isNamed(an : String) = arg.exists(_ == an);
    def setValue(t : T){
      isSet = true;
      value = t;
    }
    def isReady : Boolean = (isSet || hasDefault) && (isSet || (! isMandatory));
    def parse(args : List[String]) : List[String] = {
      if(args.length < 2) throwSyntaxErrorMessage("Variable " + arg(0) + " not set to anything!");
      
      val valueString = args.tail.head;
      val addme = stringParser.parse(valueString)(respectQuotes=respectQuotes,stripQuotes=stripQuotes)      
      setValue(addme);
      if(internalUtils.optionHolder.OPTION_verboseInputInfo) reportln("   INPUT_ARG("+this.getName()+")+="+addme.toString(),"note");
      
      args.tail.tail;
    }
  }
  case class ListArgument[T](name : String, arg: List[String], valueName : String, argDesc : String, 
      defaultValue : Option[List[T]] = None, isMandatory : Boolean = false, isImportant : Boolean = false, 
      stripQuotes : Boolean = true, respectQuotes : Boolean = true)(implicit stringParser : StringParser[T]) extends Argument[List[T]] {
    def argIsMandatory = isMandatory;
    def getName = name;
    var isSet = false;
    var hasDefault = false;
    var value : List[T] = defaultValue match {
      case Some(t) => {isSet = true; hasDefault = true; t;} 
      case None => List();
    }
    
    def getShortSyntax() : String = {
      if(isMandatory) ""+arg(0)+" "+valueName+"";
      else if(isImportant) "["+arg(0)+" "+valueName+" ..."+"]";
      else "";
    }
    def getFullSyntax() : String = {
      if(isMandatory) ""+arg(0)+" "+valueName+"";
      else "["+arg(0)+" "+valueName+"]";
    }
    def argMasterType() : String = "WhiteSpaceDelimitedList";
    def argSubType() : String = stringParser.argType;
    def argType() : String = "WhiteSpaceDelimitedList of "+argSubType();
    
    def describe() : String = {
      argDesc;
    }
    def getValue() : List[T] = value;
    def setValue(t : List[T]) {value = t;}
    def addToValue(t : T) {
      value = value ++ List(t);
    }
    def isNamed(an : String) = arg.exists(_ == an);
    def isReady : Boolean = (isSet || hasDefault) && (isSet || (! isMandatory));
    
    def parse(args : List[String]) : List[String] = {
      if(args.length < 2) throwSyntaxErrorMessage("Variable " + arg(0) + " not set to anything!");
      
      val stringList = args.takeWhile(! looksLikeArgument(_));
      val theRestList = args.takeRight(args.length - stringList.length);
      
      val valueList = stringList.map(s => stringParser.parse(s)(respectQuotes=respectQuotes,stripQuotes=stripQuotes));
      if(internalUtils.optionHolder.OPTION_verboseInputInfo) reportln("   INPUT_ARG("+this.getName()+")+="+valueList.map{_.toString()}.mkString(","),"note");

      setValue(valueList);
      
      theRestList;
    }
  }
  
  case class FinalArgument[T](name : String, valueName : String,  argDesc : String, isImportant : Boolean = false, 
      stripQuotes : Boolean = true, respectQuotes : Boolean = true)(implicit stringParser : StringParser[T]) extends Argument[T] {
    def argIsMandatory = isMandatory;
    def getName = name;
    val defaultValue : Option[T] = None;
    val isMandatory : Boolean = true;
    var isSet = false;
    var hasDefault = false;
    var value : T = stringParser.unsetValue;
    
    def getShortSyntax() : String = {
      ""+valueName+"";
    }
    def getFullSyntax() : String = {
      ""+valueName+"";
    }
    def argMasterType() : String = "trailingMonadic";
    def argSubType() : String = stringParser.argType;
    def argType() : String = argSubType();
    
    def describe() : String = argDesc;
    def getValue() : T = if(isSet) value; else{ error("Syntax error!"); value; }
    def setValue(t : T) { isSet = true; value = t;}
    def isNamed(an : String) : Boolean = ! isSet;
    def isReady : Boolean = isSet;
    def parse(args : List[String]) : List[String] = {
      val valueString = args.head;
      setValue(stringParser.parse(valueString)(respectQuotes=respectQuotes,stripQuotes=stripQuotes));
      args.tail;
    }
  }
  
  
  
}