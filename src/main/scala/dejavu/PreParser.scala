package dejavu

import java.io.{BufferedReader, FileReader}
import java.text.ParseException
import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex
import scala.util.parsing.combinator._

/**
 * Base trait for property definitions.
 */
sealed trait PProperty

/**
 * Represents the Initiate block which defines initial variable types.
 * @param variables List of (name, type) pairs and their optional default values.
 */
case class Initiate(variables: List[(String, String, Option[String])]) extends PProperty

/**
 * Represents an event operation block defining event behavior.
 * @param name Event's name.
 * @param params Event's parameters.
 * @param assignments Assignments during the event.
 */
case class EventOperation(
                           name: String,
                           params: List[(String, String)],
                           assignments: List[Assignment]) extends PProperty

/**
 * Represents an output block, defining the event's output format.
 * @param name Output's name.
 * @param params Output's parameters.
 */
case class Output(name: String, params: List[String]) extends PProperty

/**
 * Represents an assignment.
 * @param variable Variable being assigned.
 * @param variableType Variable type.
 * @param expression Assignment expression.
 */
case class Assignment(variable: String, variableType: String, expression: String) extends PProperty

/**
 * Parser for the property DSL.
 */
class PrePropertyParser extends JavaTokenParsers {

  /**
   * Unwanted patterns in the DSL.
   */
  private def allUnwantedPatterns: Parser[String] =
    ("@@" ^^ { _ => failure("Pattern '@@' is not allowed.").toString }) |
      ("^^^" ^^ { _ => failure("Pattern '^^^' is not allowed.").toString }) |
      ("[]" ^^ { _ => failure("Pattern '[]' is not allowed.").toString })


  /**
   * Parse the DSL ensuring unwanted patterns are absent.
   */
  def guardedParsedProperty: Parser[PreProperty] =
    not(allUnwantedPatterns) ~> parsedProperty

  override val whiteSpace: Regex = "[ \t\r\f\n]+".r


  /**
   * Parse until end of line - useful for capturing assignments.
   */
  private def restOfLine: Parser[String] = """.*""".r

  // Supported data types.
  private def varType: Parser[String] = "int" | "double" | "float" | "string" | "str" | "bool"

  // Match a variable and its associated type.
  private def variable: Parser[(String, String)] = ident ~ (":" ~> varType) ^^ {
    case id ~ t => (id, t)
  }

  // Match the "initiate" block used for initializing variables.
  private def initiate: Parser[Initiate] =
    ("initiate" | "Initiate" | "INITIATE") ~> rep(variableWithDefaultValue) ^^ Initiate

  // Match a variable with its type and optional default value.
  private def variableWithDefaultValue: Parser[(String, String, Option[String])] =
    ident ~ (":" ~> varType) ~ opt(":=" ~> restOfLine) ^^ {
      case id ~ t ~ maybeValue => (id, t, maybeValue)
    }

  // Match an assignment statement.
  private def assignment: Parser[Assignment] =
    ident ~ (":" ~> varType) ~ (":=" ~> restOfLine) ^^ {
      case variable ~ varType ~ expression => Assignment(variable, varType, expression)
    }

  // Match the main event operation block.
  private def eventOperation: Parser[EventOperation] =
    ("on" | "On" | "ON") ~> ident ~ ("(" ~> repsep(variable, ",") <~ ")") ~ rep(assignment) ^^ {
      case name ~ params ~ assignments => EventOperation(name, params, assignments)
    }

  // Match either the special identifier or the standard identifier.
  private def parameter: Parser[String] =
    "@" ~> ident | ident

  // Match the "output" block.
  private def output: Parser[Output] =
    ("output" | "Output" | "OUTPUT") ~> ident ~ ("(" ~> repsep(parameter, ",") <~ ")") ^^ {
      case name ~ params => Output(name, params)
    }

  // Main parser function.
  private def parsedProperty: Parser[PreProperty] =
    opt(initiate) ~ rep(eventOperation | output) ^^ {
      case maybeInit ~ blocks =>
        val events = blocks.collect { case e: EventOperation => e }
        val outs = blocks.collect { case o: Output => o }
        PreProperty(maybeInit.getOrElse(Initiate(Nil)), events, outs)
    }
}

/**
 * Represents the entire parsed property.
 * @param initiate Initiate block.
 * @param operations Event operation blocks.
 * @param outputs Output blocks.
 */
case class PreProperty(
                        initiate: Initiate,
                        operations: List[EventOperation],
                        outputs: List[Output]) extends PProperty


/**
 * The CodeGenerator object provides functionality to transform the parsed DSL into actual Scala code.
 */
object CodeGenerator {


  // Convert custom types to Scala types
  private def toScalaType(typeStr: String): String = typeStr.toLowerCase match {
    case "int"               => "Int"
    case "double"            => "Double"
    case "bool"              => "Boolean"
    case "str" | "string"    => "String"
    case _                   => throw new IllegalArgumentException(s"Unsupported type: $typeStr")
  }

  // Provide a default value for a given Scala type
  private def defaultValue(scalaType: String): String = scalaType match {
    case "Int"         => "0"
    case "Double"      => "0.0"
    case "Boolean"     => "false"
    case "String"      => """"""""
    case _             => throw new IllegalArgumentException(s"Unsupported type: $scalaType")
  }

  // Replace the 'in' expression
  private def translateInExpression(expr: String): String = {
    val inPattern: Regex = """(?i)in\(\[(.*?)\]\)""".r
    inPattern.replaceAllIn(expr, m => s"in(List(${m.group(1)}))")
  }

  // Replace the '@' symbol while avoiding '@@'
  private def translatePrevExpression(expr: String): String = {
    val pattern: Regex = """(?<!@)@""".r
    pattern.replaceAllIn(expr, "prev_")
  }

  def generate(property: PreProperty): String = {
    val sb = new StringBuilder

    // Helper methods and implicit conversions for the generated code
    sb.append(
      """
        |object PreMonitor extends PreMonitorTrait {
        |  /**
        |   * Extension methods for various types.
        |   * This provides utilities for better readability and concise code writing.
        |   */
        |
        |  /**
        |   * Extension methods for all types.
        |   *
        |   * @param self an element of type T.
        |   * @tparam T the type of `self`.
        |   */
        |  implicit class ExtendedAny[T](val self: T) {
        |
        |    /**
        |     * Checks if the element is present in a list.
        |     *
        |     * @param lst the list to check against.
        |     * @return true if `self` is present in `lst`, false otherwise.
        |     */
        |    def in(lst: List[T]): Boolean = lst.contains(self)
        |
        |    /**
        |     * Checks if the element is not present in a list.
        |     *
        |     * @param lst the list to check against.
        |     * @return true if `self` is not present in `lst`, false otherwise.
        |     */
        |    def notin(lst: List[T]): Boolean = !lst.contains(self)
        |  }
        |
        |  /**
        |   * Extension methods for Boolean type.
        |   *
        |   * @param a the source Boolean value.
        |   */
        |  implicit class extendedBoolean(a: Boolean) {
        |
        |    /**
        |     * Logical implication.
        |     *
        |     * @param b target Boolean value.
        |     * @return true if either `a` is false or `b` is true, otherwise false.
        |     */
        |    def ->(b: => Boolean): Boolean = !a || b
        |
        |    /**
        |     * Logical biconditional (equivalence).
        |     *
        |     * @param b target Boolean value.
        |     * @return true if `a` and `b` are both true or both false, otherwise false.
        |     */
        |    def <->(b: => Boolean): Boolean = a == b
        |  }
        |
        |  /**
        |   * Power operation for Double.
        |   *
        |   * @param a base value.
        |   */
        |  implicit class extendedDouble(a: Double) {
        |
        |    /**
        |     * Raises `a` to the power of `b`.
        |     *
        |     * @param b the exponent.
        |     * @return result of a raised to the power b.
        |     */
        |    def ^^(b: Double): Double = scala.math.pow(a, b)
        |  }
        |
        |  /**
        |   * Power operation for Int.
        |   *
        |   * @param a base value.
        |   */
        |  implicit class extendedInt(a: Int) {
        |
        |    /**
        |     * Raises `a` to the power of `b`.
        |     *
        |     * @param b the exponent.
        |     * @return result of a raised to the power b as an Int.
        |     */
        |    def ^^(b: Int): Int = scala.math.pow(a.toDouble, b.toDouble).toInt
        |  }
        |
        |  /**
        |   * Power operation for Float.
        |   *
        |   * @param a base value.
        |   */
        |  implicit class extendedFloat(a: Float) {
        |
        |    /**
        |     * Raises `a` to the power of `b`.
        |     *
        |     * @param b the exponent.
        |     * @return result of a raised to the power b as a Float.
        |     */
        |    def ^^(b: Float): Float = scala.math.pow(a.toDouble, b.toDouble).toFloat
        |  }
        |
        |  /**
        |   * A type class defining absolute operation on a type.
        |   *
        |   * @tparam A the type for which the absolute operation is defined.
        |   */
        |  trait AbsOps[A] {
        |
        |    /**
        |     * Computes the absolute value of `value`.
        |     *
        |     * @param value the input value.
        |     * @return absolute value of `value`.
        |     */
        |    def abs(value: A): A
        |  }
        |
        |  // Below are instances for the AbsOps type class for supported types: Int, Float, and Double.
        |
        |  implicit object IntAbsOps extends AbsOps[Int] {
        |    def abs(value: Int): Int = math.abs(value)
        |  }
        |
        |  implicit object FloatAbsOps extends AbsOps[Float] {
        |    def abs(value: Float): Float = math.abs(value)
        |  }
        |
        |  implicit object DoubleAbsOps extends AbsOps[Double] {
        |    def abs(value: Double): Double = math.abs(value)
        |  }
        |
        |  /**
        |   * Generic utility to compute the absolute value for supported types.
        |   *
        |   * @param value the input value.
        |   * @param ops implicit evidence of the AbsOps type class instance for type A.
        |   * @tparam A type of the value.
        |   * @return absolute value of `value`.
        |   */
        |  def abs[A](value: A)(implicit ops: AbsOps[A]): A = ops.abs(value)
        |
        |
        |  /**
        |   * A type class defining checksum operation on a type.
        |   *
        |   * @tparam A the type for which the checksum operation is defined.
        |   */
        |  trait ChecksumOps[A] {
        |    def sha256(value: A): String
        |  }
        |
        |  // Generic instance for any type that can be converted to a string.
        |  implicit def genericChecksumOps[A]: ChecksumOps[A] = new ChecksumOps[A] {
        |    def sha256(value: A): String = {
        |      val strRepresentation = value.toString
        |      val digest = MessageDigest.getInstance("SHA-256")
        |      val hash = digest.digest(strRepresentation.getBytes("UTF-8"))
        |      hash.map("%02x".format(_)).mkString
        |    }
        |  }
        |
        |  /**
        |   * Generic utility to compute the checksum for any supported type.
        |   *
        |   * @param value the input value.
        |   * @param ops   implicit evidence of the ChecksumOps type class instance for type A.
        |   * @tparam A type of the value.
        |   * @return SHA-256 checksum of `value`.
        |   */
        |  def sha256[A](value: A)(implicit ops: ChecksumOps[A]): String = ops.sha256(value)
        |
        |  /**
        |   * Provides utility methods for common operations.
        |   */
        |  object Operators {
        |
        |    /**
        |     * If-Then-Else operation.
        |     *
        |     * @param condition a Boolean condition to check.
        |     * @param ifTrue result to return if `condition` is true.
        |     * @param ifFalse result to return if `condition` is false.
        |     * @tparam T type of the results.
        |     * @return `ifTrue` if `condition` is true, `ifFalse` otherwise.
        |     */
        |    def ite[T](condition: Boolean, ifTrue: T, ifFalse: T): T = if (condition) ifTrue else ifFalse
        |  }
        |""".stripMargin)


    property match {
      case PreProperty(init, events, outputs) =>

        // Validate the number of events matches the number of outputs
        if (events.length != outputs.length) {
          throw new IllegalStateException("Number of events does not match number of outputs.")
        }

        // Extract and initialize event parameters
        val eventParams = events.flatMap(_.params).distinct.toMap
        initVariables(init, eventParams, sb)

        // Extract used variable names and types
        val declaredVariables = init.variables.map(_._1).toSet
        val assignedVariables = extractAssignedVariables(events, declaredVariables)
        val prevVariablesFromEvents = extractPrevVariablesFromEvents(events)
        validateInitiateVariables(prevVariablesFromEvents, declaredVariables)

        val assignedVariableTypes = events.flatMap(_.assignments).map(a => a.variable -> a.variableType).toMap

        // Initialize any undeclared assigned variables
        initAssignedVariables(assignedVariables, assignedVariableTypes, sb)

        sb.append(s"\n\n")

        generateEventAndOutputFunctions(events, outputs, sb, eventParams)
        generateEvaluateFunction(events, sb, declaredVariables)

        sb.append("}\n")
        sb.toString
    }
  }

  private def initVariables(init: Initiate, eventParams: Map[String, String], sb: StringBuilder): Unit = {
    init.variables.foreach {
      case (name, dataType, maybeValue) =>
        val scalaType = toScalaType(dataType)
        sb.append(s"\tprivate var $name: $scalaType = ${maybeValue.getOrElse(defaultValue(scalaType))}\n")
        sb.append(s"\tprivate var prev_$name: $scalaType = ${maybeValue.getOrElse(defaultValue(scalaType))}\n")
    }

    eventParams.foreach {
      case (name, dataType) =>
        if (!init.variables.map(_._1).contains(name)) {
          val scalaType = toScalaType(dataType)
          sb.append(s"\tprivate var $name: $scalaType = ${defaultValue(scalaType)}\n")
        }
    }
  }

  private def extractAssignedVariables(events: List[EventOperation], declaredVariables: Set[String]): Set[String] = {
    events.flatMap(_.assignments.map(_.variable)).toSet.diff(declaredVariables)
  }

  private def extractPrevVariablesFromEvents(events: List[EventOperation]): Set[String] = {
    events.flatMap(e =>
      e.assignments.flatMap(a =>
        """@(\w+)""".r.findAllMatchIn(a.expression).map(_.group(1))
      )
    ).toSet
  }

  private def validateInitiateVariables(prevVariablesFromEvents: Set[String], declaredVariables: Set[String]): Unit = {
    prevVariablesFromEvents.foreach { variable =>
      if (!declaredVariables.contains(variable)) {
        throw new IllegalStateException(s"@$variable was not initialized in the initiate block.")
      }
    }
  }

  private def initAssignedVariables(assignedVariables: Set[String], assignedVariableTypes: Map[String, String], sb: StringBuilder): Unit = {
    assignedVariables.foreach { name =>
      val scalaType = toScalaType(assignedVariableTypes.getOrElse(name, "int"))
      sb.append(s"\tprivate var $name: $scalaType = ${defaultValue(scalaType)}\n")
    }
  }

  private def generateEventAndOutputFunctions(events: List[EventOperation], outputs: List[Output], sb: StringBuilder, eventParams: Map[String, String]): Unit = {
    val eventNameToOutputMap = events.zip(outputs).map {
      case (event, output) => event.name -> Some(output)
    }.toMap

    // Handle event definitions
    events.foreach { event =>
      val EventOperation(name, params, assignments) = event

      // Generate the event functions as you did
      val formattedParams = params.map { case (paramName, typeStr) => s"$paramName: ${toScalaType(typeStr)}" }.mkString(", ")
      sb.append(s"\tprivate def on_$name($formattedParams): Unit = {\n")
      assignments.foreach {
        case Assignment(variable, _, expression) =>

          // Replace 'ite' with 'Operators.ite'
          var updatedExpression = expression.replace("ite(", "Operators.ite(")

          // Handle in/notIn operator
          updatedExpression = translateInExpression(updatedExpression)

          // Handle '@' expressions
          updatedExpression = translatePrevExpression(updatedExpression)

          sb.append(s"\t\tthis.$variable = $updatedExpression\n")
      }

      sb.append("\t}\n\n")

      // Now, for each event, try to find its output and generate the corresponding output function
      eventNameToOutputMap.get(name).foreach {
        case Some(Output(outputName, params)) =>
          sb.append(s"\tprivate def ${name}_output(): Any = {\n")
          if (params.isEmpty) {
            sb.append(s"""\t\t("$outputName")\n""")
          } else {
            val parsed_params = ListBuffer[String]()
            params.foreach { param =>

              if (param.startsWith("@")) {
                // Handle '@' expressions
                parsed_params += translatePrevExpression(param)
              } else {
                parsed_params += s"$param.toString"
              }
            }
            sb.append(s"""\t\tList("$outputName", List(${parsed_params.mkString(", ")}))\n""")
          }
          sb.append("\t}\n\n")
      }
    }
  }

  private def generateEvaluateFunction(events: List[EventOperation], sb: StringBuilder, declaredVariables: Set[String]): Unit = {
    // Start the generation of the evaluate function
    sb.append("\tdef evaluate(event_name: String, params: Any*): Option[Any] = {\n")
    sb.append("\t\tvar event : Any = null\n\n") // Added this line

    sb.append("\t\tevent_name match {\n")

    events.foreach {
      case EventOperation(name, eventParams, _) =>
        sb.append(s"""\t\t\tcase "$name" => {\n""")
        if (eventParams.nonEmpty) {
          sb.append(s"""\t\t\t\tif (params.length != ${eventParams.size}) throw new IllegalArgumentException("Incorrect number of parameters for event $name")\n""")
        }

        eventParams.zipWithIndex.foreach {
          case ((paramName, typeStr), index) =>
            sb.append(s"""\t\t\t\tTry(params($index).toString.trim.to${toScalaType(typeStr)}) match {\n""")
            sb.append(s"""\t\t\t\t\tcase Success(value) => this.$paramName = value\n""")
            sb.append(s"""\t\t\t\t\tcase Failure(e) => println(s"Failed to convert to ${toScalaType(typeStr)}: """)
            sb.append("""$e")""")
            sb.append(s"""\n}\n""")
        }

        val paramNamesSeq = eventParams.map(_._1).mkString(", ")
        sb.append(s"\t\t\t\ton_$name($paramNamesSeq)\n")
        sb.append(s"\t\t\t\tevent = ${name}_output()\n") // This line was changed to remove the extra event name prefix
        sb.append("\t\t\t}\n")
    }

    sb.append("\t\t\tcase _ => event = List(event_name, params.toList)\n")
    sb.append("\t\t\t}\n")

    // Add logic to update the previous variables
    declaredVariables.foreach { name =>
      sb.append(s"\t\tprev_$name = $name\n")
    }

    sb.append("\t\tSome(event)\n") // Return the event at the end
    sb.append("\t}\n")

    sb.toString
  }
}


object PPropertyValidation {

  /**
   * Validates event blocks to ensure that each 'on' is followed by an 'output'.
   *
   * The function scans through lines of the DSL input. It ensures that each block that starts
   * with 'on' is followed by an 'output' line. If any 'on' block does not have a corresponding
   * 'output', or if any 'output' does not have a preceding 'on', an exception is thrown.
   * If validation passes, a success message is printed.
   *
   * @param inputLines List of strings, each representing a line from the input DSL.
   * @throws IllegalArgumentException if any block does not adhere to the 'on' followed by 'output' pattern.
   */
  def validateEventBlocks(inputLines: List[String]): Unit = {
    var insideBlock = false
    var currentOnLine: Option[(Int, String)] = None
    var line_number = 0

    inputLines.foreach { line =>
      line_number += 1

      if (line.toLowerCase.startsWith("on ")) {
        if (insideBlock) {
          throw new IllegalArgumentException(s"Missing 'output' for the block starting at line " +
            s"${currentOnLine.get._1}: '${currentOnLine.get._2}'")
        }
        insideBlock = true
        currentOnLine = Some(line_number, line.trim)
      } else if (line.toLowerCase.startsWith("output ")) {
        if (!insideBlock) {
          throw new IllegalArgumentException(s"Found 'output' at line $line_number without a preceding 'on'.")
        }
        insideBlock = false
        currentOnLine = None
      }
    }

    if (insideBlock) {
      throw new IllegalArgumentException(s"Missing 'output' for the block starting at line " +
        s"${currentOnLine.get._1}: '${currentOnLine.get._2}'")
    }

    println("Event block validation passed.")
  }

  /**
   * Validates the initialization block of the provided DSL input.
   *
   * The function verifies that variables are correctly initialized based on their specified type.
   * It supports the following types: int, bool, double, float, string/str.
   * The validation stops when encountering a line that starts with "on" (case insensitive).
   *
   * @param lines A list of strings, each representing a line from the input DSL.
   * @throws IllegalArgumentException if any line has an incorrect format or an invalid initialization value.
   */
  def validateInitiateBlock(lines: List[String]): Unit = {
    for (line <- lines) {

      // If line starts with "on" (in any case), stop the validation
      if (line.trim.toLowerCase.startsWith("on")) {
        println("Initiate block validation passed.")
        return
      }

      val parts = line.split(":=").map(_.trim)

      // Skip empty lines or the line starts with "initiate"
      if (line.trim.isEmpty | line.trim.toLowerCase.startsWith("initiate")) { }
      else if (parts.length == 1 | line == "") { }

      else if (parts.length == 2 | line == "") {
        val typeAndName = parts(0).split(":").map(_.trim)
        val value = parts(1)

        typeAndName(1) match {
          case "int" =>
            try {
              value.toInt
            } catch {
              case _: NumberFormatException =>
                throw new IllegalArgumentException(s"Expected an integer value but found" +
                  s" '$value' for variable '${typeAndName(0)}'.")
            }

          case "bool" =>
            if (value != "true" && value != "false") {
              throw new IllegalArgumentException(s"Expected a boolean value but found " +
                s"'$value' for variable '${typeAndName(0)}'.")
            }

          case "double" =>
            try {
              value.toDouble
            } catch {
              case _: NumberFormatException =>
                throw new IllegalArgumentException(s"Expected a double value but found " +
                  s"'$value' for variable '${typeAndName(0)}'.")
            }

          case "float" =>
            try {
              value.toFloat
            } catch {
              case _: NumberFormatException =>
                throw new IllegalArgumentException(s"Expected a float value but found " +
                  s"'$value' for variable '${typeAndName(0)}'.")
            }

          case "str" | "string" =>
            if (!value.startsWith("\"") || !value.endsWith("\"")) {
              throw new IllegalArgumentException(s"Expected a string value but found " +
                s"'$value' for variable '${typeAndName(0)}'. Strings should be enclosed in double quotes.")
            }

          case _ =>
            throw new IllegalArgumentException(s"Unknown type '${typeAndName(1)}' for variable " +
              s"'${typeAndName(0)}' (Supported type: int, double, float, double, str).")
        }
      } else {
        throw new IllegalArgumentException(s"Invalid line format: $line")
      }
    }
  }
}



/**
 * Main application to demonstrate the parser and code generator functionality.
 * The app parses a sample DSL input and prints the generated Scala code.
 */
object PreParser {
  //object PreParser extends App {
  private val parser = new PrePropertyParser
  //  parse("""/Users/moraneus/Documents/Studies/phd/Dejavu-With-Pre-Proccess-Evaluation/Code/PPEE-DejaVu/src/test/scala/tests/pre_proccess/test1/spec2.pqtl""")

  private def readerFromFile(filename: String): String = {
    val reader = new BufferedReader(new FileReader(filename))
    var content = ""

    try {
      content = Stream.continually(reader.readLine()).takeWhile(_ != null).mkString("\n")
    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
      reader.close()
    }
    content
  }

  def parse(filename: String): (String, String) = {
    //  def parse(filename: String): Unit = {
    val preInputProperty = readerFromFile(filename)

    // Do some validations
    if (preInputProperty.isEmpty) throw new IllegalArgumentException(s"Pre-Property is empty...")
    PPropertyValidation.validateInitiateBlock(preInputProperty.split("\n").toList)
    PPropertyValidation.validateEventBlocks(preInputProperty.split("\n").toList)


    var generatedCode: String = ""

    parser.parseAll(parser.guardedParsedProperty, preInputProperty) match {
      case parser.Success(parsed, _) =>
        println("Pre-Property Parsing succeeded.")
        generatedCode = CodeGenerator.generate(parsed)
      case parser.Failure(msg, next) => throw new ParseException(s"Failure: $msg at line " +
        s"${next.pos.line}, column ${next.pos.column}", next.pos.line)
      case parser.Error(msg, next) => throw new ParseException(s"Error: $msg at line " +
        s"${next.pos.line}, column ${next.pos.column}", next.pos.line)
    }
    print(generatedCode)
    (preInputProperty, generatedCode)
  }
}