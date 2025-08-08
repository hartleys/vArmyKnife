package runner


import internalUtils.Reporter._;
import internalUtils.stdUtils._;
import internalUtils.fileUtils._;
import internalUtils.commandLineUI._;
import internalUtils.commonSeqUtils._;
import internalUtils.optionHolder._;

import internalUtils.commonSeqUtils._;
import internalUtils.genomicUtils._;
import internalUtils.genomicAnnoUtils._;
import internalUtils.GtfTool._;
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._


object helpDocs {
  
  val MIT_LICENSE = List[String](
 "The MIT License",
 "Copyright (c) 2009 The Broad Institute",
 "Permission is hereby granted, free of charge, to any person obtaining a copy "+
 "of this software and associated documentation files (the \"Software\"), to deal "+
 "in the Software without restriction, including without limitation the rights "+
 "to use, copy, modify, merge, publish, distribute, sublicense, and/or sell "+
 "copies of the Software, and to permit persons to whom the Software is "+
 "furnished to do so, subject to the following conditions: ",
 
 "The above copyright notice and this permission notice shall be included in "+
 "all copies or substantial portions of the Software. ",
 
 "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR "+
 "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, "+
 "FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE "+
 "AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER "+
 "LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, "+
 "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN "+
 "THE SOFTWARE."
  );
  
  val AUTHOR = internalUtils.commandLineUI.DEFAULT_AUTHOR;
  val LEGAL = internalUtils.commandLineUI.DEFAULT_LEGAL;
  
  val DESCRIPTION = List[String](
      ""
  );
  
  //val DESCRIPTION = List[String](
  //    "<TODO>: Write description!"
  //);
  //test

  case class HelpTopic( topicName : String, desc : String = "", 
                        topicManual : Seq[internalUtils.commandLineUI.UserManualBlock]){
   def blockManual : String = topicManual.map{ umb => {
     umb.getBlockString()
   }}.mkString("\n")
   def markdownManual : String = topicManual.map{ umb => {
     umb.getMarkdownString();
   }}.mkString("\n")
  }
  
  val helpTopics : Seq[HelpTopic] = Seq[HelpTopic](
      HelpTopic("MapFunctions","Functions that perform a single operation on a variant file.",internalTests.SVcfMapFunctions.MAPFUNCTIONS_USERMANUALBLOCKS),
      HelpTopic("VariantExpressions","Syntax for logical Expressions that return either true or false for a variant. Used by various other functions.",internalUtils.VcfTool.sVcfFilterLogicParser.logicManualFmt ),
      HelpTopic("GenotypeExpressions","Syntax for logical Expressions that return either true or false for a given sample and a given variant. Used by various other functions.", internalUtils.VcfTool.sGenotypeFilterLogicParser.logicManualFmt ),
      HelpTopic("addInfo","Syntax for functions executed using: --fcn \"addInfo|newInfoName|fcn(param1,param2,...)\"",internalTests.SVcfTagFunctions.TAGFUNCTIONS_USERMANUALBLOCKS)

  )
  val helpTopicMap : Map[String,HelpTopic] = helpTopics.map{ht => (ht.topicName,ht)}.toMap;
  
  def runHelp(args : Array[String]){
    //report("Help:","output");
    if(args.exists{ a => a == "--verbose" || a == "-v"}){
      internalUtils.optionHolder.OPTION_VERBOSE = true
    } else if(args.exists{ a => a == "--debugMode"}){
      internalUtils.optionHolder.OPTION_VERBOSE = true
      internalUtils.optionHolder.OPTION_DEBUGMODE = true
    }
    
    val filtArgs = args.filter{ arg => ! Set("--verbose","--debugMode","v").contains(arg) }
    val helpCommand = filtArgs.lift(1).getOrElse("?");
        
    if(helpCommand == "?" || helpCommand == "--man" || helpCommand == "--help" || helpCommand == "help" || helpCommand == "-help" || helpCommand == "man" || helpCommand == "-man"){
      //generalHelp;
      val commandList : Map[String, () => CommandLineRunUtil] = runner.commandList;
      val cmd = commandList.get("walkVcf");
      
      report("HELP: vArmyKnife\n","output");
      reportln("AVAILABLE HELP TOPICS:","output");
      helpTopics.foreach{ ht => {
        reportln( wrapLinesWithIndent(ht.topicName+":"+ht.desc, internalUtils.commandLineUI.CLUI_CONSOLE_LINE_WIDTH, "    ", false) ,"output");
      }}
      
      (cmd.get)().parser.reportManual();
      
    } else if(helpCommand == "secondaryCommands"){
      generalHelp;
    } else if(helpCommand == "generateMarkdownPages") {
      writeMarkdownHelp("./");
    } else if(helpCommand.toUpperCase() == "LICENSES" || helpCommand.toUpperCase() == "LICENSE" || helpCommand.toUpperCase() == "COPYING"){
        val licenseArg = filtArgs.lift(2).headOption.getOrElse("base");
        if(licenseArg == "LGPLv3"){
          ( new java.io.BufferedReader( new java.io.InputStreamReader( this.getClass().getClassLoader().getResourceAsStream("LGPLv3.txt") )) ).lines().iterator().asScala.foreach{ line => {
            report(line + "\n","output");
          }}
        } else if(licenseArg == "LGPLv2.1"){
          ( new java.io.BufferedReader( new java.io.InputStreamReader( this.getClass().getClassLoader().getResourceAsStream("LGPLv2.1.txt") )) ).lines().iterator().asScala.foreach{ line => {
            report(line + "\n","output");
          }}
        } else {
          ( new java.io.BufferedReader( new java.io.InputStreamReader( this.getClass().getClassLoader().getResourceAsStream("library.LICENSES.txt") )) ).lines().iterator().asScala.foreach{ line => {
            report(line + "\n","output");
          }}
        }
    } else if(helpTopicMap.contains(helpCommand)){
      report( helpTopicMap(helpCommand).blockManual,"output" );
    } else {
      //Print help for a specific function:
      
      val commandList : Map[String, () => CommandLineRunUtil] = runner.commandList;
      val cmd = commandList.get(helpCommand);
      
      report("HELP: " + helpCommand +"\n","output");
      
        cmd match {
          case Some(makerFcn) => {
            val cmdRunner = makerFcn();
            cmdRunner.parser.reportManual();
          }
          case None => {
            val cmdOld = runner.depreciated_commandList.get(helpCommand);
            cmdOld match {
              case Some(c) => reportln("Command " + helpCommand +" is DEPRECIATED. No help info found!","output");
              case None => reportln("Command " + helpCommand + " not found!","output");
            }
          }
        }
    }
  }
  def generalHelp {
    //Print general help.
    reportln("GENERAL HELP:","output");
    
    reportln(getGeneralHelp,"output");
  }
   
  def getGeneralHelp : String = {
      val sb = new StringBuilder("");
      sb.append(runner.UTILITY_TITLE+"\n")
      sb.append("version: " + runner.UTIL_VERSION + "\n");
      sb.append("\n");
      //sb.append("SYNOPSIS\n");
      //sb.append( lineseq2string(wrapLinesWithIndent(SYNOPSIS, internalUtils.commandLineUI.CLUI_CONSOLE_LINE_WIDTH , "    ", false))  + "\n");
      //sb.append("\n");
      sb.append("DESCRIPTION:\n");
      sb.append( lineseq2string(wrapLinesWithIndent(DESCRIPTION, internalUtils.commandLineUI.CLUI_CONSOLE_LINE_WIDTH, "    ", false)) + "\n");
      sb.append("    NOTE: if you run into OutOfMemoryExceptions, \n    try adding the java options: \"-Xmx18000M -Xms5000M\""+"\n");
      sb.append("\n");
      
      sb.append("GENERAL SYNTAX:\n\n");
      sb.append("    varmyknife [java_options] walkVcf"+" [options] infile outfile"+"\n");
      sb.append("    OR"+"\n");
      sb.append("    varmyknife [java_options]  walkVcf"+" [options] infile - > outfile"+"\n");
      sb.append("    OR"+"\n");
      sb.append("    varmyknife [java_options]  command"+" [options]"+"\n");

      sb.append("\n");
      
      sb.append("COMMANDS:\n");
      
      for((arg, cmdMaker) <- runner.sortedCommandList.filter{ case (arg,cmdMk) => { ! cmdMk().isAlpha }} ){
        val parser = cmdMaker().parser;
        //Note: Hack! the line below must be at least as long as COMMAND_MAX_LENGTH!
        val blank = "                                                                                                                                                                           ";
        sb.append("    "+arg+":" + blank.substring(0,runner.COMMAND_MAX_LENGTH - arg.length) + parser.getQuickSynopsis + "\n");
        sb.append(wrapLinesWithIndent(parser.getDescription, internalUtils.commandLineUI.CLUI_CONSOLE_LINE_WIDTH, "        ", false) + "\n");
        sb.append(wrapLinesWithIndent(parser.getForMoreHelp, internalUtils.commandLineUI.CLUI_CONSOLE_LINE_WIDTH, "        ", false) + "\n");
      }
      if(internalUtils.optionHolder.OPTION_VERBOSE){
        sb.append("EXPERIMENTAL COMMANDS:\n");
        sb.append(wrapLinesWithIndent("These commands have not been fully tested and are not for production use. Documentation may be incomplete or nonexistant. Some are nonfunctional, or have been subsumed into other commands, or were only intended for internal testing purposes. Use at your own risk!\n",
                                      internalUtils.commandLineUI.CLUI_CONSOLE_LINE_WIDTH, "    ", false))

        for((arg, cmdMaker) <- runner.sortedCommandList.filter{ case (arg,cmdMk) => { cmdMk().isAlpha }} ){
          val parser = cmdMaker().parser;
          //Note: Hack! the line below must be at least as long as COMMAND_MAX_LENGTH!
          val blank = "                                                                                                                                                                           ";
          sb.append("    "+arg+":" + blank.substring(0,runner.COMMAND_MAX_LENGTH - arg.length) + parser.getQuickSynopsis + "\n");
          sb.append(wrapLinesWithIndent(parser.getDescription, internalUtils.commandLineUI.CLUI_CONSOLE_LINE_WIDTH, "        ", false) + "\n");
          sb.append(wrapLinesWithIndent(parser.getForMoreHelp, internalUtils.commandLineUI.CLUI_CONSOLE_LINE_WIDTH, "        ", false) + "\n");
        }
        for((arg, cmdMaker) <- runner.depreciated_commandList ){
          val parser = cmdMaker().parser;
          //Note: Hack! the line below must be at least as long as COMMAND_MAX_LENGTH!
          val blank = "                                                                                                                                                                           ";
          sb.append("    "+arg+":" + blank.substring(0,runner.COMMAND_MAX_LENGTH - arg.length) + parser.getQuickSynopsis + "\n");
          sb.append(wrapLinesWithIndent(parser.getDescription, internalUtils.commandLineUI.CLUI_CONSOLE_LINE_WIDTH, "        ", false) + "\n");
          sb.append(wrapLinesWithIndent(parser.getForMoreHelp, internalUtils.commandLineUI.CLUI_CONSOLE_LINE_WIDTH, "        ", false) + "\n");
        }
      }
      sb.append("AUTHORS:\n");
      sb.append(lineseq2string(wrapLinesWithIndent(AUTHOR, internalUtils.commandLineUI.CLUI_CONSOLE_LINE_WIDTH, "    ", false)) + "\n");
      
      sb.append("LEGAL:\n");
      sb.append(lineseq2string(wrapLinesWithIndent(LEGAL, internalUtils.commandLineUI.CLUI_CONSOLE_LINE_WIDTH, "    ", false)) + "\n");
      
      return sb.toString;
  }
  
  def writeMarkdownHelp(outdir : String) {
    
      val sb = new StringBuilder("");
      sb.append("# "+runner.UTILITY_TITLE+"\n");
      sb.append("> Version" + runner.UTIL_VERSION + " (Updated " + runner.UTIL_COMPILE_DATE +")\n\n");
      sb.append("> ([back to help base](docs/secondaryCommands.html))\n\n");
      sb.append("## General Help\n\n");
      
      sb.append("## DESCRIPTION:\n\n");
      sb.append( (DESCRIPTION.mkString(" ")).replaceAll("_","\\\\_") + "\n\n");
      sb.append("NOTE: if you run into OutOfMemoryExceptions, try adding the java options: \"-Xmx8G\""+"\n\n");
      
      sb.append("## GENERAL SYNTAX:\n\n");
      sb.append("    varmyknife [java_options] "+" [options] infile outfile"+"\n");
      sb.append("    OR"+"\n");
      sb.append("    varmyknife [java_options] "+" [options] infile - > outfile"+"\n");
      sb.append("    OR"+"\n");
      sb.append("    varmyknife [java_options] "+"--CMD commandName"+" [options]"+"\n");
      
      sb.append("## COMMANDS:\n");
      for((arg, cmdMaker) <- runner.sortedCommandList.filter{ case (arg,cmdMk) => { ! cmdMk().isAlpha }} ){
        val parser = cmdMaker().parser;
        if(arg == "walkVcf"){
           sb.append("### ["+arg+"](index.html)\n\n");
          sb.append("> "+(parser.getDescription).replaceAll("_","\\\\_") + "\n\n");
        } else {
          sb.append("### ["+arg+"]("+arg+".html)\n\n");
          sb.append("> "+(parser.getDescription).replaceAll("_","\\\\_") + "\n\n");
        }
      }
      
      //if( runner.sortedCommandList.exists{ case (arg,cmdMk) => { cmdMk().isAlpha }}){
        sb.append("## EXPERIMENTAL COMMANDS: \n");
        sb.append("These commands have not been fully tested and are not for production use. Documentation may be incomplete or nonexistant. Some are nonfunctional, or have been subsumed into other commands, or were only intended for internal testing purposes. Use at your own risk!\n");
        for((arg, cmdMaker) <- runner.sortedCommandList.filter{ case (arg,cmdMk) => { cmdMk().isAlpha }} ){
          val parser = cmdMaker().parser;
          sb.append("### ["+arg+"]("+arg+".html)\n\n");
          sb.append("> "+(parser.getDescription).replaceAll("_","\\\\_") + "\n\n");
        }
        
        for((arg, cmdMaker) <- runner.depreciated_commandList ){
          val parser = cmdMaker().parser;
          sb.append("### ["+arg+"]("+arg+".html)\n\n");
          sb.append("> "+(parser.getDescription).replaceAll("_","\\\\_") + "\n\n");
        }
      //}
      
        
        
      sb.append("## AUTHORS:\n\n");
      sb.append((AUTHOR.mkString(", ")).replaceAll("_","\\\\_") + "\n");
      
      sb.append("## LEGAL:\n\n");
      sb.append(lineseq2string(wrapLinesWithIndent(LEGAL, internalUtils.commandLineUI.CLUI_CONSOLE_LINE_WIDTH, "    ", false)) + "\n");
      
      val indexwriter = openWriter(outdir+"secondaryCommands.md");
      indexwriter.write(sb.toString);
      indexwriter.close();
      
      for((arg, cmdMaker) <- runner.sortedCommandList ++ runner.depreciated_commandList){
        val parser = cmdMaker().parser;
        val filename = arg
          
        //if(arg == "walkVcf"){
        //  "index"
        //} else {
        //  arg
        //}
        val writer = openWriter(outdir+filename+".md");
        writer.write(parser.getMarkdownManual());
        writer.close();
        
        if(arg == "walkVcf"){
          val writer = openWriter(outdir+"index"+".md");
          writer.write(parser.SPECIAL_getMainManualMarkdown());
          writer.close();
        }
      }
      
      
      
      for((topicName,ht) <- helpTopicMap){
        val writer = openWriter(outdir+topicName+".md");
        writer.write(ht.markdownManual);
        writer.close();
      }
  }
}

class helpDocs extends CommandLineRunUtil {
    val parser : CommandLineArgParser = 
      new CommandLineArgParser(
          command = "", 
          quickSynopsis = "", 
          synopsis = "", 
          description = "",   
          argList =  internalUtils.commandLineUI.CLUI_UNIVERSAL_ARGS
      );
    
    def run(args : Array[String]){
      helpDocs.runHelp(args);
    }
    
    def generalHelp {
      helpDocs.generalHelp;
    }
 }