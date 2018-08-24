package runner

import internalUtils.commandLineUI._;
import internalUtils.Reporter._;

object HelloWorld {
  
  

  class SayHelloWorld extends CommandLineRunUtil {
     override def priority = 100;
     val parser : CommandLineArgParser = 
       new CommandLineArgParser(
          command = "HelloWorld", 
          quickSynopsis = "", 
          synopsis = "", 
          description = "Test utility that just says hello world.",   
          argList = 
                    
                    new UnaryArgument( name = "version",
                                         arg = List("--version"), // name of value
                                         argDesc = "Flag. If raised, print version." // description
                                       ) ::
                    internalUtils.commandLineUI.CLUI_UNIVERSAL_ARGS );
      
     def run(args : Array[String]) { 
       val out = parser.parseArguments(args.toList.tail);
      
       if(out){
         sayHelloWorld(
             parser.get[Boolean]("infileDir")
             );
       }
     }
   }
  
  def sayHelloWorld(ver : Boolean){
    reportln("Hello World","note");
    if(ver){
      reportln("Version: "+runner.UTIL_VERSION,"note");
      reportln("Date: "+runner.UTIL_COMPILE_DATE,"note");
    }
  }
}