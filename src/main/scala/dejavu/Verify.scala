package dejavu

import java.io.File
import scala.io.Source

/**
  * This class offers a main method that runs DejaVu on a log file.
  * DejaVu can also be executed in online mode, monitoring an executing
  * application. However, the current setup is focused on analysis of
  * log files.
  *
  * The main method can be run in two modes:
  *
  * (1) in development mode (when the code is being developed).
  *     This situation is detected by the main method being provided the trace file
  *     as well as the spec file as argument, and any other arguments.
  *     When run in development mode, the main method performs all tasks, such
  *     as compiling the generated monitor, and running the resulting monitor on the
  *     trace.
  *
  * (2) in command line mode in a shell using the dejavu script.
  *     When run in command line mode, the main method is just called on the spec
  *     file (and not the trace file), which generates the monitor code, and the script
  *     will itself compile and run the generated monitor.
  */

object Verify {

  /**
    * Flag indicating whether long tests (> 7 seconds) should be executed.
    */

  private val LONGTEST : Boolean = true

  /**
    * Flag indicating whether to clear generated files and folder (relevent in development mode).
    */

  private var CLEAR: Boolean = true

  /**
    * Flag indicating whether to work in production or development mode.
    * false is set for development.
    * this execution mode affects where the output files are created.
    * * In production mode they are stored locally in the current directory, while in development mode they
    * are stored in the /src/test/scala/sandbox/generated_monitors/ path.
    */

  private var EXECUTION: Boolean = false

  /**
    * Flag indicating whether a test took place. Becomes false when a long test is executed but the <code>LONGTEST</code> flag is false.
    * Is called by the result verification functions.
    */

  var verified : Boolean = false

  /**
    * Method for timing the execution of a block of code.
    *
    * @param text this text is printed as part of the timing information.
    * @param block the code to be executed.
    * @tparam R the result type of the block to be executed.
    * @return the result of ececution the block.
    */

  def time[R](text: String)(block: => R): R = {
    val t1 = System.currentTimeMillis()
    val result = block
    val t2 = System.currentTimeMillis()
    val ms = (t2 - t1).toFloat
    val sec = ms / 1000
    println(s"\n--- Elapsed $text time: " + sec + "s" + "\n")
    result
  }

  /**
    * Executes a UNIX shell command from within the Scala program. Some messages on the input stream
    * and error stream are ignored.
    *
    * @param cmd the shell command to be executed. The vararg solution is needed in case one of
    *            the arguments contains a <code>*</code>.
    */

  def exec(cmd: String*) {
    // println(s"\n--- [${cmd.mkString(" ")}] ---\n")
    val cmdArray = cmd.toArray
    val process =
      if (cmdArray.length == 1) Runtime.getRuntime exec cmd(0) else Runtime.getRuntime exec cmdArray
    val input = process.getInputStream
    val error = process.getErrorStream
    val inputSource = Source fromInputStream input
    val errorSource = Source fromInputStream error
    for (line <- inputSource.getLines()) {
      if (!line.startsWith("Could not load BDD") && !line.startsWith("Resizing node table")) {
        println(line)
      }
    }
    for (line <- errorSource.getLines()) {
      if (!line.contains("warning") && !line.startsWith("Garbage collection")) {
        println(line)
      }
    }
  }

  /**
    * Compiles the generated monitor <code>TraceMonitor.scala</code> and runs it
    * on the trace file (and other options) provided as a string argument.
    *
    * @param args
    */

  def compileAndExecute(args: String, generatedMonitorsPath: String): Unit = {
    val lib = Settings.PROJECT_DIR + "/lib"
    time("monitor compilation") {
      println("Compiling generated monitor ...")
      exec(s"scalac -cp .:$lib/commons-csv-1.1.jar:$lib/javabdd-1.0b2.jar $generatedMonitorsPath/TraceMonitor.scala")
    }
    time("trace analysis") {
      println("Analyzing trace ...")
      exec(s"scala -J-Xmx16g -cp .:$lib/commons-csv-1.1.jar:$lib/javabdd-1.0b2.jar $generatedMonitorsPath/TraceMonitor.scala $args")
    }
    exec("sh", "-c", "rm *.class") // multi-argument call needed due to occurrence of *
    if (CLEAR) exec("sh", "-c", s"rm -rf $generatedMonitorsPath") // Delete generated files
  }

  /**
    * Main method for executing DejaVu. If only one argument is provided (the specification
    * file - which happens when the main method is called from the dejavu shell script),
    * this main method only generates the monitor <code>TraceMonitor.scala</code>. The script
    * will then compile and run it. If also the trace is provided we are (likely) in development
    * mode, and this main method will also compile and run <code>TraceMonitor.scala</code>. This
    * latter mode is useful when e.g. working in an IDE such as IntelliJ where no script exists.
    *
    * @param arguments following the pattern:
    *                  <code>(--specfile <filename>) [--logfile <filename>] [--bits numOfBits]
    *                  [--mode (debug | profile)] [--prediction num] [--prediction_type (smart | brute)]
    *                  [--result <filename>] [--clear (0 | 1)] [--execution (0 | 1)]</code>
    */

  def main(arguments: Array[String]): Unit = {
    val usage =
      """Usage:
        |
        |      (--specfile <filename>) [--logfile <filename>] [--bits numOfBits] [--mode (debug | profile)]
        |      [--prediction num] [--prediction_type (smart | brute)] [--result <filename>]
        |      [--clear (0 | 1)] [--execution (0 | 1)]
        |
        |Options:
        |   --specfile          the path to a file containing the specification document. This is a mandatory field.
        |   --logfile           the path to a file containing the log in CSV format to be analyzed.
        |   --bits              number indicating how many bits should be assigned to each variable in the BDD representation. If nothing is specified, the default value is 20 bits.
        |   --mode              specifies output modes. by default no one is active.
        |   --prediction        indicates whether prediction is required, along with the size of the prediction parameters.
        |   --prediction_type   specifies prediction approach. by default smart approach is activated.
        |   --result            the path to a result filename. If not specify the default is the running DejaVu folder. For development mode.
        |   --execution         indicating whether to work in production or development mode. this execution mode affects where the output files are created.
        |   --clear             indicating whether to clear generated files and folder. For development mode.
        """.stripMargin
    SymbolTable.reset()

    val args = arguments.toList

    if (2 <= arguments.length && arguments.length <= 18 && arguments.length % 2 == 0) {
      val argMap = Map.newBuilder[String, Any]
      arguments.sliding(2, 2).toList.collect {
        case Array("--specfile", specfile: String) => argMap.+=("specfile" -> specfile)
        case Array("--logfile", logfile: String) => argMap.+=("logfile" -> logfile)
        case Array("--bits", numOfBits: String) => argMap.+=("bits" -> numOfBits)
        case Array("--mode", mode: String) => argMap.+=("mode" -> mode)
        case Array("--prediction", predictionLength: String) => argMap.+=("prediction" -> predictionLength)
        case Array("--prediction_type", predictionType: String) => argMap.+=("prediction_type" -> predictionType)
        case Array("--resultfile", resultfile: String) => argMap.+=("resultfile" -> resultfile)
        case Array("--execution", execution: String) => argMap.+=("execution" -> execution)
        case Array("--clear", mode: String) => argMap.+=("clear" -> mode)
      }

      // Validate the spec file argument
      val specFile = argMap.result().get("specfile")
      val specFilePath = specFile match {
        case Some(value) => value.toString
        case None =>
          println(s"*** program must be called with specfile argument ($usage)")
          return
      }

      var dir = new File(specFilePath)
      if (!dir.exists) {
        println(s"*** given specfile is not a valid file ($specFilePath)")
        return
      }

      // Validate the trace file argument
      val logFile = argMap.result().get("logfile")
      val logFilePath = logFile match {
        case Some(value) => value.toString
        case None => null
      }

      if (logFilePath != null) {
        dir = new File(logFilePath)
        if (!dir.exists) {
          println(s"*** given logfile is not a valid file ($logFilePath)")
          return
        }
      }

      // Validate the result file argument
      val resultFile = argMap.result().get("resultfile")
      val resultFilePath = resultFile match {
        case Some(value) => value.toString
        case None => null
      }

      if (resultFilePath != null) {
        dir = new File(resultFilePath)
        if (!dir.getParentFile.exists) {
          println(s" ***resultfile parent is not a valid folder")
          return
        }
      }


      // Validate the number of used bits argument
      val bits = argMap.result().get("bits")
      bits match {
        case Some(value) =>
          if (!value.toString.matches("""\d+""")) {
            println(s"*** bits argument must be an integer, and not ${value.toString}")
            return
          }
        case None => println("bits argument not activated")
      }

      // Validate the prediction arguments
      val prediction = argMap.result().get("prediction")
      prediction match {
        case Some(value) =>
          if (!value.toString.matches("""\d+""")) {
            println(s"*** prediction argument must be an integer, and not ${value.toString}")
            return
        }
        case None => println("prediction argument not activated")
      }

      val predictionType = argMap.result().get("prediction_type")
      predictionType match {
        case Some(value) =>
          val predictionTypeValue = value.toString.toLowerCase()
          if (!(predictionTypeValue != "smart" || predictionTypeValue != "brute")) {
            println(s"*** prediction type argument must be: smart or brute, and not ${value.toString}")
            return
          }
        case None => println("prediction type argument not activated")
      }

      // Validate the output mode argument
      val mode = argMap.result().get("mode")
      mode match {
        case Some(value) =>
          val modeValue = value.toString.toLowerCase()
          if (!(modeValue != "debug" || modeValue != "profile")) {
            println(s"*** mode argument must be: debug or profile, and not ${value.toString}")
            return
          }
        case None => println("mode argument not activated")
      }

      // Validate the clear files argument
      val clear = argMap.result().get("clear")
      clear match {
        case Some(value) =>
          if (!(value != "0" || value != "1")) {
            println(s"*** clear argument must be: 1 or 0, and not ${value.toString}")
            return
          }
          if (value == "0") CLEAR = false else CLEAR = true
        case None => println(s"clear is set to default value (CLEAR=$CLEAR)")
      }

      // Validate the execution mode argument
      val execution = argMap.result().get("execution")
      execution match {
        case Some(value) =>
          if (!(value != "0" || value != "1")) {
            println(s"*** execution argument must be: 1 or 0, and not ${value.toString}")
            return
          }
          if (value == "0") EXECUTION = false else EXECUTION = true
        case None => println(s"execution is set to default value development (EXECUTION=$EXECUTION)")
      }

      var generatedMonitorsPath: String = null

      time("total") {
        time("monitor synthesis") {
          val p = new Parser
          val spec = p.parseFile(specFilePath)
          println(spec)
          spec.translate(EXECUTION)
          generatedMonitorsPath = spec.generatedMonitorsPath
        }

        // Removes unnecessary arguments for the evaluation step.
        val specfileIndex = args.indexOf("--specfile")
        var evaluationArguments = args.take(specfileIndex) ++
                                  args.drop(specfileIndex + 2)
        val executionIndex = evaluationArguments.indexOf("--execution")
        evaluationArguments = evaluationArguments.take(executionIndex) ++
                              evaluationArguments.drop(executionIndex + 2)


        // Execute the evaluation step.
        if (args.length > 2 && logFilePath != null)
          compileAndExecute(evaluationArguments.mkString(" "), generatedMonitorsPath)
      }
    } else {
      println(s"$usage")
    }
  }

  def apply(arguments : String*): Unit = {
    main(arguments.toArray)
    verified = true
  }

  def long(arguments : String*): Unit = {
    if (LONGTEST) {
      main(arguments.toArray)
      verified = true
    } else {
      verified = false
    }
  }
}
