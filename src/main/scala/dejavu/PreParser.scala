package dejavu

import java.io.{BufferedReader, FileReader}
import scala.util.matching.Regex
import scala.util.parsing.combinator._

/**
 * PProperty represents the base trait for property definitions.
 */
sealed trait PProperty

/**
 * Represents the Initiate block which defines initial variable types.
 * @param variables List of (name, type) pairs and their optional default values.
 */
case class Initiate(variables: List[(String, String, Option[String])]) extends PProperty

/**
 * Represents an event operation block which defines an event's behavior.
 * @param name Event's name.
 * @param params Event's parameters as a list of (name, type) pairs.
 * @param assignments List of assignments during the event.
 */
case class EventOperation(
             name: String,
             params: List[(String, String)],
             assignments: List[Assignment]) extends PProperty

/**
 * Represents an output block, which defines the event's output format.
 * @param name Output's name.
 * @param params Output's parameters.
 */
case class Output(name: String, params: List[String]) extends PProperty

/**
 * Represents an assignment statement within an event block.
 * @param variable The variable being assigned.
 * @param variableType The variable type.
 * @param expression The expression used for assignment.
 */
case class Assignment(variable: String, variableType: String, expression: String) extends PProperty

/**
 * Main parser for the property DSL.
 */
class PrePropertyParser extends JavaTokenParsers {

  override val whiteSpace: Regex = "[ \t\r\f\n]+".r

  // Data types supported by the DSL
  private def varType: Parser[String] = "int" | "double" | "float" | "str" | "bool"

  // Parse a variable and its type
  def variable: Parser[(String, String)] = ident ~ (":" ~> varType) ^^ {
    case id ~ t => (id, t)
  }

  // Parse the Initiate block which initializes variables with optional default values
  private def initiate: Parser[Initiate] = ("initiate" | "Initiate" | "INITIATE") ~> rep(variableWithDefaultValue) ^^ Initiate

  // Parse variables with their type and optional default value
  private def variableWithDefaultValue: Parser[(String, String, Option[String])] =
    ident ~ (":" ~> varType) ~ opt(":=" ~> restOfLine) ^^ {
      case id ~ t ~ maybeValue => (id, t, maybeValue)
    }

  // Parse assignment statements within events
  def assignment: Parser[Assignment] = ident ~ (":" ~> varType) ~ (":=" ~> restOfLine) ^^ {
    case variable ~ varType ~ expression => Assignment(variable, varType, expression)
  }


  // Parse the main event operation block
  def eventOperation: Parser[EventOperation] =
    ("on" | "On" | "ON") ~> ident ~ ("(" ~> repsep(variable, ",") <~ ")") ~ rep(assignment) ^^ {
      case name ~ params ~ assignments => EventOperation(name, params, assignments)
    }

  // Parse the output block to define the format of event outputs
  def output: Parser[Output] =
    ("output" | "Output" | "OUTPUT") ~> ident ~ ("(" ~> repsep(ident, ",") <~ ")") ^^ {
      case name ~ params => Output(name, params)
    }

  // The main parser function which parses the entire DSL input
  def parsedProperty: Parser[PreProperty] = opt(initiate) ~ rep(eventOperation | output) ^^ {
    case maybeInit ~ blocks =>
      val events = blocks.collect { case e: EventOperation => e }
      val outs = blocks.collect { case o: Output => o }
      PreProperty(maybeInit.getOrElse(Initiate(Nil)), events, outs)
  }

  // Parse until end of line - useful for capturing assignments
  def restOfLine: Parser[String] = """.*""".r
}

/**
 * Represents the entire parsed property.
 * @param initiate The Initiate block.
 * @param operations List of event operation blocks.
 * @param outputs List of output blocks.
 */
case class PreProperty(
                   initiate: Initiate,
                   operations: List[EventOperation],
                   outputs: List[Output]) extends PProperty

/**
 * The CodeGenerator object provides functionality to transform the parsed DSL into actual Scala code.
 */
object CodeGenerator {

  /** Transforms a DSL type into a native Scala type. */
  private def toScalaType(dslType: String): String = dslType match {
    case "int"         => "Integer"
    case "double"      => "Double"
    case "float"       => "Float"
    case "str"         => "String"
    case "bool"        => "Boolean"
    case other         => throw new IllegalArgumentException(s"Unsupported type: $other")
  }

  private def defaultValue(scalaType: String): String = scalaType match {
    case "Integer" | "Double" | "Float" => "0"
    case "String" => "\"\""
    case "Boolean" => "false"
    case _ => throw new IllegalArgumentException(s"Unsupported Scala type: $scalaType")
  }

  private def translateInExpression(expr: String): String = {
    val inPattern: Regex = """(?i)in\(\[(.*?)\]\)""".r
    inPattern.replaceAllIn(expr, m => s"in(List(${m.group(1)}))")
  }

  private def translateAbsExpression(expr: String): String = {
    val inPattern: Regex = """\|\s*(.*?)\s*\|""".r
    inPattern.replaceAllIn(expr, m => s"(${m.group(1)}).abs")
  }

  private def translatePrevExpression(expr: String): String = {
    val inPattern: Regex = """@(\w+)\[(\w+|\d+\.?\d*|"[\w\s]+"|true|false)\]""".r
    val transformed = inPattern.replaceAllIn(expr, m => {
      val varName = m.group(1)
      val defaultValue = m.group(2)
      s"Operators.ite(prev_$varName == null, $defaultValue, prev_$varName)"
    })
    transformed
  }

  def generate(property: PreProperty): String = {
    val sb = new StringBuilder

    // Helper methods and implicit conversions for the generated code
    sb.append(
      """
        |object PreMonitor {
        | implicit class ExtendedAny[T](val self: T) {
        |   def in(lst: List[T]): Boolean = lst.contains(self)
        |   def notin(lst: List[T]): Boolean = !lst.contains(self)
        | }
        |
        |
        | object Operators {
        |   def ite[T](condition: Boolean, ifTrue: T, ifFalse: T): T = if (condition) ifTrue else ifFalse
        |   def xor(left: Boolean, right: Boolean): Boolean = left ^ right
        | }
        |""".stripMargin)

    // Code generation logic
    property match {
      case PreProperty(init, events, outputs) =>

        // Extract parameters from events
        val eventParams = events.flatMap(_.params).distinct.toMap

        // Collect declared variable names from the 'init' block into a list
        val declaredVariableNames = init.variables.map {
          case (name, _, _) => name
        }.toList

        // Initialize declared variables
        init.variables.foreach {
          case (name, dataType, maybeValue) =>
            val scalaType = toScalaType(dataType)
            sb.append(s"\tprivate var $name: $scalaType = ${maybeValue.getOrElse(defaultValue(scalaType))}\n")
        }

        // Write initialization for these parameters first
        eventParams.foreach {
          case (name, dataType) =>
            // Check if name is not in declaredVariableNames (initiate block)
            if (!declaredVariableNames.contains(name)) {
              val scalaType = toScalaType(dataType)
              sb.append(s"\tprivate var $name: $scalaType = ${defaultValue(scalaType)}\n")
            }
        }

        val declaredVariables = init.variables.map(_._1).toSet
        val assignedVariables = events.flatMap(_.assignments.map(_.variable)).toSet.diff(declaredVariables)
        val prevVariables = events.flatMap(e =>
          e.assignments.flatMap(a =>
            """@(\w+)""".r.findAllMatchIn(a.expression).map(_.group(1))
          )
        ).toSet

        val assignedVariableTypes = events.flatMap(_.assignments).map(a => a.variable -> a.variableType).toMap

        // Initialize assigned variables that were not declared
        assignedVariables.foreach { name =>
          val scalaType = toScalaType(assignedVariableTypes.getOrElse(name, "int"))
          sb.append(s"\tprivate var $name: $scalaType = ${defaultValue(scalaType)}\n")
        }

        // Initialize "prev_" variables
        prevVariables.foreach { name =>
          val scalaType = toScalaType(assignedVariableTypes.getOrElse(name, "int"))
          sb.append(s"\tprivate var prev_$name: $scalaType = null \n")
        }

        sb.append("\n")

        // Validate if the numbers of event is matched to the number of outputs
        if (events.length != outputs.length) {
          throw new IllegalStateException("Number of events does not match number of outputs.")
        }

        // Create a map of event names to their corresponding output names and parameters
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

              // Handle |...| expression
              updatedExpression = translateAbsExpression(updatedExpression)

              // Handle '@' expressions
              updatedExpression = translatePrevExpression(updatedExpression)

              sb.append(s"\t\tthis.$variable = $updatedExpression\n")
          }
          sb.append("\t}\n\n")

          // Now, for each event, try to find its output and generate the corresponding output function
          eventNameToOutputMap.get(name).foreach {
            case Some(Output(outputName, params)) =>
              sb.append(s"\tprivate def ${name}_output(): Any = {\n")
              sb.append(s"""\t\t("$outputName", ${params.mkString(", ")})\n""")
              sb.append("\t}\n\n")
          }
        }



        // Start the generation of the evaluate function
        sb.append("\tdef evaluate(event_name: String, params: Any*): Any = {\n")
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
                sb.append(s"""\t\t\t\tthis.$paramName = params($index).asInstanceOf[${toScalaType(typeStr)}]\n""")
            }

            val paramNamesSeq = eventParams.map(_._1).mkString(", ")
            sb.append(s"\t\t\t\ton_$name($paramNamesSeq)\n")
            sb.append(s"\t\t\t\tevent = ${name}_output()\n") // This line was changed to remove the extra event name prefix
            sb.append("\t\t\t}\n")
        }

        sb.append("\t\t\tcase _ => event = (event_name, params)\n")
        sb.append("\t\t\t}\n")

        // Add logic to update the previous variables
        prevVariables.foreach { name =>
          sb.append(s"\t\tprev_$name = $name\n")
        }

        sb.append("\t\tevent\n") // Return the event at the end
        sb.append("\t}\n")

        sb.toString
    }

    sb.append("}\n")
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

          case "str" =>
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
  private val parser = new PrePropertyParser

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
    val preInputProperty = readerFromFile(filename)

    // Do some validations
    if (preInputProperty.isEmpty) throw new IllegalArgumentException(s"Pre-Property is empty...")
    PPropertyValidation.validateInitiateBlock(preInputProperty.split("\n").toList)
    PPropertyValidation.validateEventBlocks(preInputProperty.split("\n").toList)


    var generatedCode: String = ""

    parser.parseAll(parser.parsedProperty, preInputProperty) match {
      case parser.Success(parsed, _) =>
        println("Pre-Property Parsing succeeded.")
        generatedCode = CodeGenerator.generate(parsed)
//        println(generatedCode)
      case parser.Failure(msg, next) => println(s"Failure: $msg at ${next.pos}")
      case parser.Error(msg, next) => println(s"Error: $msg at ${next.pos}")
    }

    (preInputProperty, generatedCode)
  }
}
