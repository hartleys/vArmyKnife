package runner

//import fileConversionUtils._
//import annotationUtils._
//import internalUtils._
//import miscLittleJobUtils._

import internalUtils.commandLineUI._;

object runner {
  
  val UTIL_VERSION = "3.1.53"; // REPLACE_THIS_QORTS_VERSION_VARIABLE_WITH_VERSION_NUMBER          (note this exact text is used in a search-and-replace. Do not change it.)
  val UTIL_COMPILE_DATE = "Fri Sep  4 14:59:46 EDT 2020"; // REPLACE_THIS_QORTS_DATE_VARIABLE_WITH_DATE          (note this exact text is used in a search-and-replace. Do not change it.)
  val UTIL_COMPILE_TIME : Long = 1599245986; // REPLACE_THIS_QORTS_DATE_VARIABLE_WITH_TIME          (note this exact text is used in a search-and-replace. Do not change it.)

  val UTIL_MAJOR_VERSION = UTIL_VERSION.split("\\.")(0);
  val UTIL_MINOR_VERSION = UTIL_VERSION.split("\\.")(1);
  val UTIL_PATCH_VERSION = UTIL_VERSION.split("-")(0).split("\\+")(0).split("\\.")(2);
  
  val UTIL_COMPLETE_VERSION = UTIL_VERSION + "+" + UTIL_COMPILE_TIME;
  
  val UTILITY_TITLE= "vArmyKnife"

  //final val FOR_HELP_STRING = "For help, use command: "
  
  val Runner_ThisProgramsExecutableJarFileName : String = "vArmyKnife.jar";
  val allowDepreciated : Boolean = true;
  val COMMAND_MAX_LENGTH = 30;
  
  var RUNNER_EXECUTION_STARTTIME_DATE : java.util.Date = java.util.Calendar.getInstance().getTime();
  var RUNNER_EXECUTION_STARTTIME_MILLIS : Long = RUNNER_EXECUTION_STARTTIME_DATE.getTime();
  var RUNNER_EXECUTION_STARTTIME_STRING = RUNNER_EXECUTION_STARTTIME_DATE.toString();
  var RUNNER_EXECUTION_STARTTIME_SIMPLESTRING = internalUtils.stdUtils.getDateAndTimeStringFromDate(RUNNER_EXECUTION_STARTTIME_DATE);
  var RUNNER_EXECUTION_ENDTIME_DATE : Option[java.util.Date] = None;
  var RUNNER_EXECUTION_ENDTIME_MILLIS : Option[Long] = None;
  var RUNNER_EXECUTION_ENDTIME_STRING : Option[String] = None;
  var RUNNER_EXECUTION_ENDTIME_SIMPLESTRING : Option[String] = None;

    
  //Command name -> (execution call, summary, syntax)
  val utilList : Seq[CommandLineRunUtil] = Seq(
    new internalTests.SVcfWalkerMain.WalkVcf,
    new internalTests.SVcfWalkerUtils.CommandVcfToMatrix,
    new internalTests.SplitExomeIntoParts.CmdSplitExomeIntoParts    
  );
  
  val depreciatedUtilList : Seq[CommandLineRunUtil] = Seq(
      //BETA:
    new internalTests.SVcfWalkerUtils.GenerateTxAnnotation,
    (new internalUtils.CalcACMGVar.CmdAssessACMG),
    new internalTests.minorUtils.CmdMemMergeVCF,

    
    
       //FUNCTIONALITY REPLACED:
          new internalTests.VcfAnnotateTX.CmdMultiStepPipeline,
    (new internalTests.SVcfWalkerUtils.AddGroupSummaries),
    new internalTests.SVcfWalkerUtils.CommandFilterVCF,
    new internalTests.SVcfWalkerUtils.CmdFilterGenotypesByStat,
    new internalTests.SVcfWalkerUtils.CmdAddTxBed,
    new internalTests.SVcfWalkerUtils.CmdCalcGenotypeStatTable,
    new internalTests.SVcfWalkerUtils.CmdFixVcfInfoWhitespace,
    new internalTests.SVcfWalkerUtils.CmdExtractSingletons,
    new internalTests.SVcfWalkerUtils.CmdConvertChromNames,
    new internalTests.SVcfWalkerUtils.CmdFilterTags,
    new internalTests.SVcfWalkerUtils.CmdConvertToStandardVcf,
    new internalUtils.fileUtils.CmdZip,
    new internalTests.SVcfWalkerUtils.VcfParserTest,
    new internalTests.SVcfWalkerUtils.compareVcfs,

    

       //SPECIAL USE:
        (new internalTests.trackToSpan.trackToSpan),
    (new internalTests.SVcfWalkerUtils.CmdRecodeClinVarCLN),
    (new internalTests.SVcfWalkerUtils.ConvertAminoRangeToGenoRange),
    new internalTests.makeBedWiggle.CmdGenHomopolymerBed,

    
    
       //UNFINISHED OR UNTESTED:
        (new internalTests.SimSeqError.GenerateSimulatedError),
    new internalTests.SimSeqError.GenerateSimulatedError,
    //new internalTests.VcfAnnotateTX.RedoEnsemblMerge,
    new internalTests.ibdSimulator.ibdSimulator,
    
    //NOT SURE:
    new internalTests.SVcfWalkerUtils.RunCalcVariantCountSummary,
    new internalTests.makeBedWiggle.CmdCodingCoverageStats,
    new internalTests.makeBedWiggle.CmdBuildSummaryTracks,
    new internalTests.SVcfWalkerUtils.CmdCreateVariantSampleTableV2,
    new internalTests.SVcfWalkerUtils.CmdCreateVariantSampleTable
  )
  
  val utilCommandList : Map[String, () => CommandLineRunUtil] = utilList.map((util) => {
    (util.parser.getCommandString,() => util)
  }).toMap
  val aliasCommandList : Map[String, () => CommandLineRunUtil] = utilList.flatMap{ util => {
    util.parser.getAliases.map{ aa => (aa,() => util) }
  }}.toMap
  
  //final val utilCommandList : Map[String, () => CommandLineRunUtil] = 
  //    Map(
  //        //NOTE: All commands MUST be of length < COMMAND_MAX_LENGTH!
  //       ("addTxInfoToVCF"   -> (() => new internalTests.VcfAnnotateTX.addTXAnno)),
  //       ("addGroupSummaries"   -> (() => new internalTests.VcfAnnotateTX.AddGroupSummaries)),
  //       ("splitMultiAllelics" -> (() => new internalTests.VcfAnnotateTX.CmdSplitMultiAllelics))
           //("QC" -> (() => new qcUtils.runAllQC.allQC_runner)),
           //("makeFlatGff" -> (() => new fileConversionUtils.prepFlatGtfFile.prepFlatGtfFile_runner)),
           //("mergeWig" ->  (() => new fileConversionUtils.SumWigglesFast.SumWigglesFast_runner)),
           //("mergeAllCounts" ->  (() => new fileConversionUtils.mergeQcOutput.multiMerger)),
           //("mergeCounts" ->  (() => new fileConversionUtils.mergeQcOutput.merger)),
           //("bamToWiggle" ->  (() => new fileConversionUtils.bamToWiggle.wiggleMaker)),
           ////("makeSpliceBed" ->  (() => new fileConversionUtils.convertSpliceCountsToBed.converter)),
           //("mergeNovelSplices" -> (() => new fileConversionUtils.addNovelSplices.mergeNovelSplices)),
           //("makeJunctionTrack" -> (() => new fileConversionUtils.makeSpliceJunctionBed.converter)),
           //("generateSamplePlots" -> (() => new fileConversionUtils.generatePlotsWithR.genSimplePlots)),
           //("makeOrphanJunctionTrack" -> (() => new fileConversionUtils.makeOrphanJunctionBed.converter)),
           //("longReadClassifier" -> (() => new fileConversionUtils.runFeatureComboCt.rFCC_runner)),
           //("makeSimpleJunctionTrack" -> (() => new fileConversionUtils.convertSpliceCountsToBed.converter2))
           ////(("prepFlatGtfFile",((fileConversionUtils.prepFlatGtfFile.run(_), "", "")))),
           ////(("QC", ((qcUtils.runAllQC.run(_)),"",""))),
           ////(("convertSoftToHardClipping", ((fileConversionUtils.convertSoftToHardClipping.run(_)),"",""))),
           ////(("bamToWiggle", ((fileConversionUtils.bamToWiggle.run(_)),"",""))),
           ////(("sumWiggles", ((fileConversionUtils.SumWigglesFast.run(_)),"","")))
    //     )
  val sortedCommandList : Seq[(String, () => CommandLineRunUtil)] = utilCommandList.toVector.sortBy{ case (cmd,util) => (util().priority,cmd)};     
         
  val helpCommandList : Map[String, () => CommandLineRunUtil] = 
    (internalUtils.commandLineUI.HELP_COMMAND_LIST ++ internalUtils.commandLineUI.MANUAL_COMMAND_LIST).map( (c : String) => {
      (c, (() => new helpDocs()));
    }).toMap;
  
  val commandList = utilCommandList ++ helpCommandList;
         
  val depreciated_commandList : Map[String, () => CommandLineRunUtil] = 
     Map(
         ("runVcfTest" -> (() => new internalTests.VcfUtilTests.runVcfUtilTests)),
         ("HelloWorld" -> (() => new HelloWorld.SayHelloWorld)),
         ("testTXSeqUtil" -> (() => new internalTests.SVcfWalkerUtils.testTXSeqUtil)),
         ("legacyTxAnno" -> (() => (new internalTests.LegacyVcfWalkers.addTXAnno))),
         ("legacySplit" -> (() => (new internalTests.LegacyVcfWalkers.CmdSplitMultiAllelics))),
         ("legacyCanonInfo" -> (() => (new internalTests.LegacyVcfWalkers.CmdAddCanonicalInfo))),
         ("legacyAddDomainInfo" -> (() => (new internalTests.LegacyVcfWalkers.AddVariantDomainUtil))),
         ("legacyRedoDbnsfp" -> (() => (new internalTests.LegacyVcfWalkers.redoDBNSFP)))
         //("makeBedFromGtf" -> (() => new fileConversionUtils.makeBedFromGtf.converter))
         ) ++ depreciatedUtilList.map((util) => {
                (util.parser.getCommandString,() => util)
              }).toMap

  def main(args: Array[String]){
    //println("Initializing...");
    internalUtils.optionHolder.TOPLEVEL_COMMAND_LINE_ARGS = args;
    
    if(args.contains("--debugMode")){
      internalUtils.optionHolder.OPTION_DEBUGMODE = true;
    }
    if(args.contains("--verbose")){
      internalUtils.optionHolder.OPTION_VERBOSE = true;
    }
    
    
    if(args.contains("--debugMode")){
      internalUtils.Reporter.init_base();
      internalUtils.Reporter.init_stderrOnly(internalUtils.Reporter.DEBUG_CONSOLE_VERBOSITY);
      internalUtils.Reporter.reportln("Console verbosity: debugMode","debug")
    } else if(args.contains("--verbose")){
      internalUtils.Reporter.init_base();
      internalUtils.Reporter.init_stderrOnly(internalUtils.Reporter.VERBOSE_CONSOLE_VERBOSITY);
      internalUtils.Reporter.reportln("Console verbosity: debugMode","note")
    } else if(args.contains("--quiet")){
      internalUtils.Reporter.init_base();
      internalUtils.Reporter.init_stderrOnly(internalUtils.Reporter.QUIET_CONSOLE_VERBOSITY);
    } else {
      internalUtils.Reporter.init_base();
      internalUtils.Reporter.init_stderrOnly();
    }

    
    if(args.contains("--createRunningFile")){
      val idx = args.indexOf("--createRunningFile");
    }
    
    internalUtils.Reporter.reportln("Starting Util v"+UTIL_VERSION+" (Compiled " + UTIL_COMPILE_DATE + ")","note");
    internalUtils.Reporter.reportln("Starting time: ("+RUNNER_EXECUTION_STARTTIME_STRING+")","note");
    
    if(System.getProperty("sun.arch.data.model") == "32"){
      internalUtils.Reporter.reportln(
               "> Warning: 32-bit Java detected! 32-bit JVMs generally have a built-in hard ceiling on their memory usage. \n"+
               ">          Usually between 1.5 and 3 gigabytes of RAM (The precise ceiling varies depending on the version, the hardware, and the operating system).\n"+
               ">          It is generally recommended that you install a 64-bit version of Java, if available.\n"+
               ">          This can be downloaded from \"www.java.com\".","warn")
    } 
    
    try{
	    if(args.length == 0){
	      internalUtils.Reporter.reportln("No command given!","output");
	      helpDocs.generalHelp;
	    } else {
		    val cmd = commandList.get(args(0));
		    cmd match {
		      case Some(makerFcn) => {
		        val cmdRunner = makerFcn();
		        cmdRunner.run(args);
		        cmdRunner.parser.close();
		      }
		      case None => {
		        aliasCommandList.get(args(0)) match {
		          case Some(makerFcn) => {
    		        val cmdRunner = makerFcn();
    		        cmdRunner.run(args);
    		        cmdRunner.parser.close();
		          }
		          case None => {
    		        if(! allowDepreciated) {
    		          internalUtils.Reporter.reportln("[runner.runner Error]: Command " + args(0) + " not found, and depreciated tools are deactivated!","output");
    		          helpDocs.generalHelp;
    		        } else {
    		          val cmdOld = depreciated_commandList.get(args(0));
    		          cmdOld match {
    		            case Some(makerFcn) => {
    		              internalUtils.Reporter.reportln("WARNING: Running Beta tool: " + args(0),"warn");
    		              val cmdRunner = makerFcn();
    		              cmdRunner.run(args);
    		              cmdRunner.parser.close();
    		            }
    		            case None => {
    		              internalUtils.Reporter.reportln("[runner.runner Error]: Command " + args(0) + " not found!","output");
    		              helpDocs.generalHelp;
    		            }
    		          }
    		        }
		          }
		        }
		      }
		    }
	    }
    } catch {
      case e : Exception => {
        internalUtils.Reporter.reportln("============================FATAL_ERROR============================\n"+
                                        "QoRTs encountered a FATAL ERROR. For general help, use command:\n"+
                                        "          java -jar path/to/jar/QoRTs.jar --man\n"+
                                        "============================FATAL_ERROR============================\n"+
                                        "Error info:","note");
        throw e;
      }
    }
    
   // helloWorld.run(args);
    //} catch {
    //  case e : Exception => {
    //    internalUtils.Reporter.reportln("Error Caught. General Help:","note");
    //    helpDocs.generalHelp;
    //    throw e;
    //  }
    //}
    
    /*
     *     tallyFile.foreach(tt => {
      val w = openWriter(tt)
      
      getWarningAndNoticeTalliesTable("\t").foreach{ss => {
        w.write(ss+"\n")
      }}
      
      w.close();
    })
     */
    
    internalUtils.Reporter.reportln("Done. (" + (new java.util.Date()).toString + ")","note");
    internalUtils.Reporter.closeLogs;
  }
  
}
