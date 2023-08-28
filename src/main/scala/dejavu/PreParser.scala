package dejavu

import java.io.{BufferedReader, FileReader}
import java.text.ParseException
import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex
import scala.util.parsing.combinator.JavaTokenParsers
import scala.util.control.Breaks._


object FetchingHelper {

  /**
   * Extracts the name (or value) from a TypedIdentifier.
   *
   * This function pattern matches against the various subtypes of TypedIdentifier
   * and retrieves the contained name or value.
   *
   * @param ident A TypedIdentifier instance from which the name is to be extracted.
   * @return The name or value contained within the given TypedIdentifier.
   * @throws IllegalArgumentException if the provided TypedIdentifier does not match any known subtypes.
   */
  def fetchNameFromIdent(ident: TypedIdentifier): String = ident match {
    case _IdentInt(name) => name
    case _IdentFloat(name) => name
    case _IdentDouble(name) => name
    case _IdentStr(name) => name
    case _IdentBool(name) => name
    case _ => throw new IllegalArgumentException("Unknown identifier type")
  }
}

/**
 * Represents the base trait for property definitions.
 */
sealed trait PProperty

/**
 * Represents the Initiate block, defining initial variable types.
 * @param variables List of (name, type) pairs and their optional default values.
 */
case class Initiate(vars: List[(TypedIdentifier, String, Option[String])])

/**
 * Represents an event operation block, defining event behavior.
 * @param name Event's name.
 * @param params Event's parameters.
 * @param assignments Assignments during the event.
 */
case class EventOperation(
                           name: String,
                           params: List[(TypedIdentifier, String)],
                           assignments: List[Assignment]
                         ) extends PProperty

sealed trait Output extends PProperty
case class FunctionOutput(name: String, params: List[String]) extends Output
case class ITEOutput(iteFunction: ITEFunction) extends Output

/**
 * Represents an assignment.
 * @param variable Variable being assigned.
 * @param variableType Variable type.
 * @param expression Assignment expression.
 */
case class Assignment(variable: String, variableType: String, expression: String) extends PProperty

case class ITEFunction(boolExpr: BooleanExpression, function1: FunctionCall, function2: FunctionCall)

sealed trait TypedIdentifier
case class _IdentInt(name: String) extends TypedIdentifier
case class _IdentDouble(name: String) extends TypedIdentifier
case class _IdentFloat(name: String) extends TypedIdentifier
case class _IdentStr(name: String) extends TypedIdentifier
case class _IdentBool(name: String) extends TypedIdentifier
sealed trait BooleanExpression
case object _TrueExpr extends BooleanExpression
case object _FalseExpr extends BooleanExpression
case class _BooleanVar(name: String) extends BooleanExpression
case class _Not(expr: BooleanExpression) extends BooleanExpression
case class _And(left: BooleanExpression, right: BooleanExpression) extends BooleanExpression
case class _Or(left: BooleanExpression, right: BooleanExpression) extends BooleanExpression
case class _Xor(left: BooleanExpression, right: BooleanExpression) extends BooleanExpression
case class _Implication(left: BooleanExpression, right: BooleanExpression) extends BooleanExpression
case class _Biconditional(left: BooleanExpression, right: BooleanExpression) extends BooleanExpression
trait NumericExpression
case class _Value(value: Double) extends NumericExpression
case class _NumericVar(name: String) extends NumericExpression
case class _SpecialVar(name: String) extends NumericExpression
case class NumComparison(left: NumericExpression, op: String, right: NumericExpression) extends BooleanExpression
case class FunctionCall(name: String, params: List[String])

/**
 * Represents a parser for the property DSL language.
 */
class PrePropertyParser extends JavaTokenParsers {
  override val whiteSpace: Regex = "[ \t\r\f\n]+".r

  /** Parses variable's type. */
  private def varType: Parser[String] =
    "int" | "double" | "float" | "string" | "str" | "bool"

  /** Parses a variable and its type. */
  private def variable: Parser[(TypedIdentifier, String)] =
    ident ~ (":" ~> varType) ^^ {
      case id ~ "int" => (_IdentInt(id), "int")
      case id ~ "float" => (_IdentFloat(id), "float")
      case id ~ "double" => (_IdentDouble(id), "double")
      case id ~ "str" => (_IdentStr(id), "str")
      case id ~ "bool" => (_IdentBool(id), "bool")
    }


  /** Main parser for all boolean expressions, starting with the highest precedence, OR. */
  private def booleanExpression: Parser[BooleanExpression] = orExpr

  /** Parses OR boolean expressions. */
  private def orExpr: Parser[BooleanExpression] =
    andExpr * ("||" ^^^ { (a: BooleanExpression, b: BooleanExpression) => _Or(a, b) })

  /** Parses AND boolean expressions. */
  private def andExpr: Parser[BooleanExpression] =
    xorExpr * ("&&" ^^^ { (a: BooleanExpression, b: BooleanExpression) => _And(a, b) })

  /** Parses XOR (exclusive OR) boolean expressions. */
  private def xorExpr: Parser[BooleanExpression] =
    implicationExpr * ("^" ^^^ { (a: BooleanExpression, b: BooleanExpression) => _Xor(a, b) })

  /** Parses implication (->) boolean expressions. */
  private def implicationExpr: Parser[BooleanExpression] =
    biconditionalExpr * ("->" ^^^ { (a: BooleanExpression, b: BooleanExpression) => _Implication(a, b) })

  /** Parses biconditional (<->) boolean expressions. */
  private def biconditionalExpr: Parser[BooleanExpression] =
    notExpr * ("<->" ^^^ { (a: BooleanExpression, b: BooleanExpression) => _Biconditional(a, b) })

  /** Parses NOT (!) boolean expressions. */
  private def notExpr: Parser[BooleanExpression] =
    "!" ~> simpleExpr ^^ _Not | simpleExpr

  /** Parses numeric comparison expressions like `5 < 10`, `3.14 == 3.14` or `speed <= @speed`.
   * Supported operators are: <, >, <=, >=, ==, !=
   * Supports Int, Float, and Double.
   */
  private def numComparison: Parser[NumComparison] = {
    // Number parsers
    val integer: Parser[NumericExpression] = """\d+""".r ^^ { num => _Value(num.toDouble) }
    val float: Parser[NumericExpression] = """\d+(\.\d+)?[fF]""".r ^^ { str => _Value(str.replace("f", "").replace("F", "").toDouble) }
    val double: Parser[NumericExpression] = """\d+(\.\d+)?(?!f|F)""".r ^^ { str => _Value(str.toDouble) }
    val regularVar: Parser[NumericExpression] = ident ^^ _NumericVar
    val specialVar: Parser[NumericExpression] = "@" ~> ident ^^ _SpecialVar
    val numberOrVar = float | double | integer | specialVar | regularVar // order matters

    val op = ("<" | ">" | "<=" | ">=" | "==" | "!=")

    numberOrVar ~ op ~ numberOrVar ^^ {
      case left ~ operator ~ right => NumComparison(left, operator, right)
    }
  }

  /** Parses the simplest boolean expressions like variables, true, false,
   * as well as numeric comparisons.
   */
  private def simpleExpr: Parser[BooleanExpression] =
    numComparison |
      ident ^^ _BooleanVar |
      "true" ^^^ _TrueExpr |
      "false" ^^^ _FalseExpr |
      "(" ~> booleanExpression <~ ")"


  /** Parses the "initiate" block. */
  private def initiate: Parser[Initiate] =
    ("initiate" | "Initiate" | "INITIATE") ~> rep(initiateBlockVariable) ^^ {
      vars =>
        val uniqueIds = vars.map(variable => FetchingHelper.fetchNameFromIdent(variable._1)).toSet
        if (uniqueIds.size != vars.size) {
          throw new ParseException("Duplicate variable identifier detected in Initiate Block", -1)
        }
        Initiate(vars)
    }

  private def initiateBlockVariable: Parser[(TypedIdentifier, String, Option[String])] =
    ident ~ (":" ~> varType) ~ opt(":=" ~> restOfLine) ^^ {
      case id ~ "int" ~ Some(value) if value.matches("""-?\d+""") => (_IdentInt(id), "int", Some(value))
      case id ~ "int" ~ None => (_IdentInt(id), "int", None)

      case id ~ "double" ~ Some(value) if value.matches("""-?\d+(\.\d*)?""") => (_IdentDouble(id), "double", Some(value))
      case id ~ "double" ~ None => (_IdentDouble(id), "double", None)

      case id ~ "float" ~ Some(value) if value.matches("""-?\d+(\.\d*)?(f)?""") => (_IdentFloat(id), "float", Some(value))
      case id ~ "float" ~ None => (_IdentFloat(id), "float", None)

      case id ~ "str" ~ Some(value) if value.startsWith("\"") && value.endsWith("\"") => (_IdentStr(id), "str", Some(value))
      case id ~ "str" ~ None => (_IdentStr(id), "str", None)

      case id ~ "bool" ~ Some(value) if value == "true" || value == "false" => (_IdentBool(id), "bool", Some(value))
      case id ~ "bool" ~ None => (_IdentBool(id), "bool", None)

      case _ => throw new ParseException(s"Invalid value assigment for variable (Initiate Block)", -1)
    }

  /** Parses until end of line. */
  private def restOfLine: Parser[String] = """.*""".r

  /** Parses an assignment statement. */
  private def assignment: Parser[Assignment] =
    ident ~ (":" ~> varType) ~ (":=" ~> restOfLine) ^^ {
      case variable ~ varType ~ expr =>
        Assignment(variable, varType, expr.toString)
    }

  /** Parses the main event operation block. */

  private def eventOperation: Parser[EventOperation] =
    ("on" | "On" | "ON") ~> ident ~ ("(" ~> repsep(variable, ",") <~ ")") ~ rep(assignment) ^^ {
      case name ~ params ~ assignments =>
        EventOperation(name, params, assignments)
    }

  /** Parses function call parameters. */
  private def parameter: Parser[String] = "@" ~ ident ^^ { case atSign ~ id => atSign + id } | ident | decimalNumber | "false" | "true"

  /** Parses the ITE function. */
  private def iteFunction: Parser[ITEFunction] =
    ("ite" ~> "(" ~> booleanExpression ~ ("," ~> functionCall) ~ ("," ~> functionCall) <~ ")") ^^ {
      case boolExpr ~ function1 ~ function2 =>
        ITEFunction(boolExpr, function1, function2)
    }


  /** Parses function calls or a "skip" ident. */
  private def functionCall: Parser[FunctionCall] =
    (ident ~ ("(" ~> repsep(parameter, ",") <~ ")") ^^ {
      case name ~ params => FunctionCall(name, params)
    }) | ("skip" ^^^ FunctionCall("skip", List()))

  /** Parses the "output" block. */
  private def output: Parser[Output] =
    ("output" | "Output" | "OUTPUT") ~> (iteFunction | functionCall) ^^ {
      case function: FunctionCall => FunctionOutput(function.name, function.params)
      case ite: ITEFunction => ITEOutput(ite)
    }

  /** Parses a combined event operation block and ensures both eventOperation and output are present. */
  private def combinedEvent: Parser[(EventOperation, Output)] =
    eventOperation ~ output ^^ {
      case operation ~ out => (operation, out)
    } |
      failure("Missing 'on' or 'output' line in event block.")

  /** Main parser function. */
  def parsedProperty: Parser[PreProperty] =
    opt(initiate) ~ rep(combinedEvent) ^^ {
      case maybeInit ~ blocks =>
        val events = blocks.map(_._1)
        val outs = blocks.map(_._2)
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
        outputs: List[Output]
      )



/**
 * The CodeGenerator object provides functionality to transform the parsed DSL into actual Scala code.
 */
object CodeGenerator {


  /**
   * Converts custom type strings to their respective Scala types.
   *
   * @param typeStr A string representing a custom type.
   * @return The equivalent Scala type as a string.
   * @throws IllegalArgumentException If the provided type string is unsupported.
   */
  private def toScalaType(typeStr: String): String = typeStr.toLowerCase match {
    case "int"               => "Int"
    case "double"            => "Double"
    case "float"             => "Float"
    case "bool"              => "Boolean"
    case "str" | "string"    => "String"
    case _                   => throw new IllegalArgumentException(s"Unsupported type: $typeStr")
  }


  /**
   * Provides default values for a given Scala type.
   *
   * @param scalaType A string representing a Scala type.
   * @return The default value for the given type as a string.
   * @throws IllegalArgumentException If the provided Scala type is unsupported.
   */
  private def defaultValue(scalaType: String): String = scalaType match {
    case "Int"         => "0"
    case "Double"      => "0.0"
    case "Float"       => "0f"
    case "Boolean"     => "false"
    case "String"      => """"""""
    case _             => throw new IllegalArgumentException(s"Unsupported type: $scalaType")
  }

  /**
   * Wrap the custom 'in' operator expr with parentheses.
   *
   * @param expr A string expression that may contain custom 'in' syntax.
   * @return A string with custom 'in' expr wrapped with parentheses.
   */

//  private def translateInExpression(input: String): String = {
//    val pattern = """((?:[a-zA-Z_]\w*)|[-+]?\d*\.?\d+(?:[eE][+-]?\d+)?[fF]?)\s+in\s+\(""".r
//    val output = new StringBuilder
//    var lastIndex = 0
//
//    pattern.findAllMatchIn(input).foreach { m =>
//      val startIdx = m.start
//      output.append(input.substring(lastIndex, startIdx))
//
//      var openParenCount = 1
//      var idx = m.end
//      while (openParenCount > 0 && idx < input.length) {
//        if (input(idx) == '(') openParenCount += 1
//        if (input(idx) == ')') openParenCount -= 1
//        idx += 1
//      }
//      output.append("(" + input.substring(startIdx, idx) + ")")
//      lastIndex = idx
//    }
//
//    output.append(input.substring(lastIndex))
//    output.toString()
//  }



  /**
   * Converts 'ite' expressions in the given input string into one-line if statements.
   * It handles nested 'ite' expressions by converting them from the innermost to outermost.
   *
   * @param input The input string possibly containing 'ite' expressions.
   * @return The modified string with 'ite' expressions converted to one-line if statements.
   */
  private def convertITEtoOnelineIfStatement(input: String): String = {
    val pattern = """ite\(""".r

    /**
     * Extracts the 'ite' expression from the given starting index and converts it
     * into a one-line if statement.
     *
     * @param startIndex The index in the input where the 'ite' expression starts.
     * @return A tuple containing the converted expression and the index where the 'ite' expression ends.
     */
    def extractITEExpression(startIndex: Int): (String, Int) = {
      val args = new scala.collection.mutable.ArrayBuffer[String]()
      var depth = 0
      var end = startIndex
      var extractStart = startIndex + 4 // to skip "ite("

      // Process the 'ite' expression and split it into its three components
      breakable {
        for (i <- startIndex + 4 until input.length) {
          input(i) match {
            case '(' => depth += 1
            case ')' =>
              depth -= 1
              if (depth < 0) {
                args += input.substring(extractStart, i).trim
                end = i + 1
                break()
              }
            case ',' if depth == 0 =>
              args += input.substring(extractStart, i).trim
              extractStart = i + 1
            case _ =>
          }
        }
      }

      // If any of the args has an 'ite', convert it recursively.
      val convertedArgs = args.map(arg => {
        if (arg.contains("ite(")) convertITEtoOnelineIfStatement(arg) else arg
      })

      if (convertedArgs.length == 3) {
        (s"(if (${convertedArgs(0)}) (${convertedArgs(1)}) else (${convertedArgs(2)}))", end)
      } else {
        (input.substring(startIndex, end), end)
      }
    }

    var result = new StringBuilder()
    var currentIndex = 0

    // Process the entire input string to handle multiple 'ite' expressions and expressions embedded within others
    while (currentIndex < input.length) {
      pattern.findFirstMatchIn(input.substring(currentIndex)) match {
        case Some(matched) =>
          val iteStart = matched.start + currentIndex
          result.append(input.substring(currentIndex, iteStart))

          val (convertedIte, iteEnd) = extractITEExpression(iteStart)
          result.append(convertedIte)

          currentIndex = iteEnd

        case None =>
          result.append(input.substring(currentIndex))
          currentIndex = input.length
      }
    }

    result.toString()
  }


private def translateInExpression(input: String): String = {
  val ident = """(?:[a-zA-Z_]\w*|\d+(?:\.\d+)?(?:[eE][+-]?\d+)?|"[^"]*")"""
  val pattern = s"""($ident\\sin\\s\\[.*?\\])"""
  val patternRegex = pattern.r
  patternRegex.replaceAllIn(input, m => s"(${m.group(0).replace('[', '(').replace(']', ')')})")
}


  /**
   * Replaces the '@' symbol in a given string, but avoids replacing '@@'.
   *
   * @param expr A string expression that may contain the '@' symbol.
   * @return A string with the '@' symbol replaced by 'prev_', but '@@' remains unchanged.
   */
  private def translatePrevExpression(expr: String): String = {
    val pattern: Regex = """(?<!@)@""".r
    pattern.replaceAllIn(expr, "prev_")
  }

  /**
   * Converts a Boolean expression to its string representation.
   *
   * @param expr The Boolean expression to convert.
   * @return The string representation of the given Boolean expression.
   */
  private def expressionToString(expr: BooleanExpression): String = {

    expr match {
      case _BooleanVar(name) => name
      case NumComparison(left, op, right) =>
        s"(${numericExpressionToString(left)} $op ${numericExpressionToString(right)})"
      case _Not(innerExpr) => s"!(${expressionToString(innerExpr)})"
      case _And(left, right) => s"(${expressionToString(left)} && ${expressionToString(right)})"
      case _Or(left, right) => s"(${expressionToString(left)} || ${expressionToString(right)})"
      case _Xor(left, right) => s"(${expressionToString(left)} ^ ${expressionToString(right)})"
      case _Implication(left, right) => s"(${expressionToString(left)} -> ${expressionToString(right)})"
      case _Biconditional(left, right) => s"(${expressionToString(left)} <-> ${expressionToString(right)})"
      case _TrueExpr => "true"
      case _FalseExpr => "false"
    }
  }

  private def numericExpressionToString(expr: NumericExpression): String = {
    expr match {
      case _Value(value) => value.toString
      case _NumericVar(name) => name
      case _SpecialVar(name) => "prev_" + name
    }
  }

  def generate(property: PreProperty): String = {
    val sb = new StringBuilder

    // Helper methods and implicit conversions for the generated code
    sb.append(
      """
        |object PreMonitor extends PreMonitorTrait {
        |  /**
        |   * Extension methods for various types.
        |   */
        |
        |
        | /**
        | * Provides an implicit class to enable the use of the `in` infix operator
        | * for checking the presence of an element within a collection.
        | *
        | * @param left the element to be checked for presence within a collection.
        | * @tparam T the type of the element.
        | */
        | implicit class InfixIn[T](val left: T) {
        |
        |   /**
        |   * Checks if the `left` element is present in the given elements.
        |   *
        |   * @param right a sequence of elements to check against.
        |   * @return true if `left` is present in the `right` sequence, false otherwise.
        |   */
        |   def in(right: T*): Boolean = right.contains(left)
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
        | /**
        |  * Implicit class to provide power operation `^^` for `Double` base values.
        |  *
        |  * @param a the base of type Double.
        |  */
        | implicit class ExtendedDouble(val a: Double) extends AnyVal {
        |
        |  /**
        |    * Raises the base `a` to the power of an `Int` exponent `b`.
        |    *
        |    * @param b the exponent of type Int.
        |    * @return the result as a Double.
        |    */
        |   def ^^(b: Int): Double = scala.math.pow(a, b.toDouble)
        |
        |   /**
        |     * Raises the base `a` to the power of a `Float` exponent `b`.
        |     *
        |     * @param b the exponent of type Float.
        |     * @return the result as a Double.
        |     */
        |   def ^^(b: Float): Double = scala.math.pow(a, b.toDouble)
        |
        |   /**
        |     * Raises the base `a` to the power of a `Double` exponent `b`.
        |     *
        |     * @param b the exponent of type Double.
        |     * @return the result as a Double.
        |     */
        |   def ^^(b: Double): Double = scala.math.pow(a, b)
        | }
        |
        | /**
        |   * Implicit class to provide power operation `^^` for `Int` base values.
        |   *
        |   * @param a the base of type Int.
        |   */
        | implicit class ExtendedInt(val a: Int) extends AnyVal {
        |
        |   /**
        |     * Raises the base `a` to the power of an `Int` exponent `b`.
        |     *
        |     * @param b the exponent of type Int.
        |     * @return the result as a Double.
        |     */
        |   def ^^(b: Int): Double = scala.math.pow(a.toDouble, b.toDouble)
        |
        |   /**
        |     * Raises the base `a` to the power of a `Float` exponent `b`.
        |     *
        |     * @param b the exponent of type Float.
        |     * @return the result as a Double.
        |     */
        |   def ^^(b: Float): Double = scala.math.pow(a.toDouble, b.toDouble)
        |
        |   /**
        |     * Raises the base `a` to the power of a `Double` exponent `b`.
        |     *
        |     * @param b the exponent of type Double.
        |     * @return the result as a Double.
        |     */
        |   def ^^(b: Double): Double = scala.math.pow(a.toDouble, b)
        | }
        |
        | /**
        |   * Implicit class to provide power operation `^^` for `Float` base values.
        |   *
        |   * @param a the base of type Float.
        |   */
        | implicit class ExtendedFloat(val a: Float) extends AnyVal {
        |
        |   /**
        |     * Raises the base `a` to the power of an `Int` exponent `b`.
        |     *
        |     * @param b the exponent of type Int.
        |     * @return the result as a Float.
        |     */
        |   def ^^(b: Int): Float = scala.math.pow(a.toDouble, b.toDouble).toFloat
        |
        |   /**
        |     * Raises the base `a` to the power of a `Float` exponent `b`.
        |     *
        |     * @param b the exponent of type Float.
        |     * @return the result as a Float.
        |     */
        |   def ^^(b: Float): Float = scala.math.pow(a.toDouble, b.toDouble).toFloat
        |
        |   /**
        |     * Raises the base `a` to the power of a `Double` exponent `b`.
        |     *
        |     * @param b the exponent of type Double.
        |     * @return the result as a Float.
        |     */
        |   def ^^(b: Double): Float = scala.math.pow(a.toDouble, b).toFloat
        | }
        |
        |
        |  /**
        |   * A type class defining absolute operation on a type.
        |   *
        |   * @tparam A the type for which the absolute operation is defined.
        |   */
        |  trait AbsOps[T] {
        |
        |    /**
        |     * Computes the absolute value of `value`.
        |     *
        |     * @param value the input value.
        |     * @return absolute value of `value`.
        |     */
        |    def abs(value: T): T
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
        |  def abs[T](value: T)(implicit ops: AbsOps[T]): T = ops.abs(value)
        |
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

        // Extract and initialize event parameters
        val eventParams = events.flatMap(_.params).distinct.toMap
        val totalPrevVarsInEvents = extractPrevVariablesFromEvents(events)

        // Extract used variable names and types
        val declaredVariables = init.vars.map(variable => FetchingHelper.fetchNameFromIdent(variable._1)).toSet
        val assignedVariables = extractAssignedVariables(events, declaredVariables)

        val uninitiatePrevVarsInEvents = initVariables(init, eventParams, totalPrevVarsInEvents, sb)
        val assignedVariableTypes = events.flatMap(_.assignments).map(a => a.variable -> a.variableType).toMap

        // Initialize any undeclared assigned variables
        initAssignedVariables(assignedVariables, assignedVariableTypes, sb)

        sb.append(s"\n\n")

        generateEventAndOutputFunctions(events, outputs, sb)
        generateEvaluateFunction(events, sb, declaredVariables, uninitiatePrevVarsInEvents)

        sb.append("}\n")
        sb.toString
    }
  }

  /**
   * Initializes variables based on the given `Initiate` object. Variables will be initialized
   * to their given values, or to a default value for their type if no value is provided.
   * Initialized variables are added to the provided StringBuilder.
   *
   * @param init            The `Initiate` object containing variable information.
   * @param eventParams     A map of event names to their types.
   * @param prevEventParams A set of variable names from previous events.
   * @param sb              The StringBuilder to which the initialized variables will be appended.
   */
  private def initVariables(
               init: Initiate,
               eventParams: Map[TypedIdentifier, String],
               prevEventParams: Set[String],
               sb: StringBuilder): Set[String] = {

    // To keep track of initialized variables
    val initializedVariables = scala.collection.mutable.Set[String]()

    init.vars.foreach {
      case (typedVar, dataType, maybeValue) =>
        val varName = FetchingHelper.fetchNameFromIdent(typedVar)
        val scalaType = toScalaType(dataType)

        // Append to the StringBuilder
        sb.append(s"\tprivate var ${varName}: $scalaType = ${maybeValue.getOrElse(defaultValue(scalaType))}\n")
        sb.append(s"\tprivate var prev_$varName: $scalaType = ${maybeValue.getOrElse(defaultValue(scalaType))}\n")

        // Add the varName to the set of initialized variables
        initializedVariables += varName
    }

    eventParams.foreach {
      case (typedVar, dataType) =>
        val varName = FetchingHelper.fetchNameFromIdent(typedVar)

        // Only initialize if the variable hasn't been initialized already
        if (!initializedVariables.contains(varName)) {
          val scalaType = toScalaType(dataType)
          sb.append(s"\tprivate var $varName: $scalaType = ${defaultValue(scalaType)}\n")
        }
    }

    // Create a set to collect the calculated prevVarNames
    val prevVarNamesSet = scala.collection.mutable.Set[String]()

    // Handling for prevEventParams
    prevEventParams.foreach { prevVarName =>
      if (!initializedVariables.contains(prevVarName) &&
        eventParams.exists(pair => FetchingHelper.fetchNameFromIdent(pair._1) == prevVarName)) {
        val scalaType = toScalaType(eventParams.find(pair => FetchingHelper.fetchNameFromIdent(pair._1) == prevVarName).get._2)
        sb.append(s"\tprivate var prev_$prevVarName: $scalaType = ${defaultValue(scalaType)}\n")

        // Add the calculated name to the set
        prevVarNamesSet += s"$prevVarName"
      }
    }
    // Return the set of calculated prevVarNames
    prevVarNamesSet.toSet
  }


  /**
   * Extracts names of variables that are assigned values within the provided list of events.
   *
   * @param events             A list of `EventOperation` objects.
   * @param declaredVariables  A set of variable names that have been declared.
   * @return A set of variable names that are assigned within the events but not declared.
   */
  private def extractAssignedVariables(events: List[EventOperation], declaredVariables: Set[String]): Set[String] = {
    events.flatMap(_.assignments.map(_.variable)).toSet.diff(declaredVariables)
  }

  /**
   * Extracts variable names that are referenced with a preceding '@' within assignments in the provided list of events.
   *
   * @param events A list of `EventOperation` objects.
   * @return A set of extracted variable names.
   */
  private def extractPrevVariablesFromEvents(events: List[EventOperation]): Set[String] = {
    events.flatMap(e =>
      e.assignments.flatMap(a =>
        """@(\w+)""".r.findAllMatchIn(a.expression).map(_.group(1))
      )
    ).toSet
  }

  /**
   * Initializes the assigned variables that have been identified but not explicitly declared.
   * The initialized variables are added to the provided StringBuilder.
   *
   * @param assignedVariables      A set of variable names that have been assigned but not declared.
   * @param assignedVariableTypes  A map of variable names to their data types.
   * @param sb                     The StringBuilder to which the initialized variables will be appended.
   */
  private def initAssignedVariables(assignedVariables: Set[String], assignedVariableTypes: Map[String, String], sb: StringBuilder): Unit = {
    assignedVariables.foreach { name =>
      val scalaType = toScalaType(assignedVariableTypes.getOrElse(name, "int"))
      sb.append(s"\tprivate var $name: $scalaType = ${defaultValue(scalaType)}\n")
    }
  }


  /**
   * Generates Scala code for event and output functions based on provided events and outputs.
   * The generated code is appended to the given StringBuilder instance.
   *
   * @param events       A list of `EventOperation` objects representing events.
   * @param outputs      A list of `Output` objects representing outputs.
   * @param sb           The StringBuilder to which the generated code will be appended.
   * @param eventParams  A map of event names to their parameter strings.
   */
  private def generateEventAndOutputFunctions(events: List[EventOperation], outputs: List[Output], sb: StringBuilder): Unit = {

    // Create a mapping between event names and their corresponding outputs.
    val eventNameToOutputMap = events.zip(outputs).map {
      case (event, output) => event.name -> Some(output)
    }.toMap

    // Handle event definitions
    events.foreach { event =>
      val EventOperation(name, params, assignments) = event

      // Generate function definition signature for each event.
      val formattedParams = params.map { case (paramName, typeStr) => s"${FetchingHelper.fetchNameFromIdent(paramName)}: ${toScalaType(typeStr)}" }.mkString(", ")
      sb.append(s"\tprivate def on_$name($formattedParams): Unit = {\n")

      // Process assignments inside each event.
      assignments.foreach {
        case Assignment(variable, _, expression) =>
          // Handle special expressions and operators in the provided code.
//          var updatedExpression = expression.replace("ite(", "Operators.ite(")
          var updatedExpression = convertITEtoOnelineIfStatement(expression)
          updatedExpression = translateInExpression(updatedExpression)
          updatedExpression = translatePrevExpression(updatedExpression)

          sb.append(s"\t\tthis.$variable = $updatedExpression\n")
      }
      sb.append("\t}\n\n")

      // Generate output functions for each event.
      sb.append(s"\tprivate def ${name}_output(): Any = {\n")

      // For each event, try to find its output and generate the corresponding output function.
      eventNameToOutputMap.get(name).foreach {
        case Some(output: FunctionOutput) => {
          sb.append(s"\t\t")
          sb.append(outputHelper(sb, output.name, output.params))
          sb.append(s"""\n""")
        }
        case Some(output: ITEOutput) => {
          val iteCondition = expressionToString(output.iteFunction.boolExpr)
          val ifTrue = output.iteFunction.function1
          val ifFalse = output.iteFunction.function2
          var outputITEExpr = s"""\t\tite($iteCondition, """
//          sb.append(s"""\t\tOperators.ite($iteCondition, """)
          outputITEExpr += outputHelper(sb, ifTrue.name, ifTrue.params)
          outputITEExpr += s""", """
//          sb.append(s""", """)
//          outputITEExpr += s""", """
          outputITEExpr += outputHelper(sb, ifFalse.name, ifFalse.params)
          outputITEExpr += s""")\n"""
          val formattedITEOutput = convertITEtoOnelineIfStatement(outputITEExpr)
//          sb.append(s""")\n""")
          sb.append(formattedITEOutput)
        }
      }
      sb.append("\t}\n\n")
    }
  }


  /**
   * A utility function to generate the appropriate output string format based on
   * the given output name and its parameters. The output is appended to the provided StringBuilder.
   *
   * @param sb         The StringBuilder to which the formatted output will be appended.
   * @param outputName The name of the output.
   * @param params     A list of parameters associated with the output. These parameters
   *                   can be in different formats and might include special '@' expressions.
   *
   * @return The formatted output as a String.
   */
  private def outputHelper(sb: StringBuilder, outputName: String, params: List[String]): String = {

    var stringOutput = ""
    // If the output has no parameters, simply append the output name.
    if (params.isEmpty) {
      stringOutput += s"""("$outputName")\n"""
      //      sb.append(s"""("$outputName")\n""")
    } else {
      // Use a ListBuffer to accumulate the parsed parameters.
      val parsed_params = ListBuffer[String]()

      params.foreach { param =>
        // If the parameter starts with '@', it's considered special and is translated accordingly.
        if (param.trim.startsWith("@")) {
          // Handle '@' expressions
          parsed_params += s"${translatePrevExpression(param)}"
        } else {
          // Otherwise, just append the parameter's string representation.
          parsed_params += s"$param.toString"
        }
      }

      // Once all parameters are processed, append them in a formatted manner.
      stringOutput += s"""List("$outputName", List(${parsed_params.mkString(", ")}))"""
      //      sb.append(s"""List("$outputName", List(${parsed_params.mkString(", ")}))""")
    }
//    sb.append(stringOutput)
    stringOutput
  }


  private def generateEvaluateFunction(
                      events: List[EventOperation],
                      sb: StringBuilder,
                      declaredVariables: Set[String],
                      uninitiatePrevVarsInEvents: Set[String]): Unit = {
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
            sb.append(s"""\t\t\t\t\tcase Success(value) => this.${FetchingHelper.fetchNameFromIdent(paramName)} = value\n""")
            sb.append(s"""\t\t\t\t\tcase Failure(e) => println(s"Failed to convert to ${toScalaType(typeStr)}: """)
            sb.append("""$e")""")
            sb.append(s"""\n\t\t\t\t}\n""")
        }

        val paramNamesSeq = eventParams.map(variable => FetchingHelper.fetchNameFromIdent(variable._1)).mkString(", ")
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
    uninitiatePrevVarsInEvents.foreach { name =>
      sb.append(s"\t\tprev_$name = $name\n")
    }

    sb.append("\t\tSome(event)\n") // Return the event at the end
    sb.append("\t}\n")

    sb.toString
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

    var generatedCode: String = ""

    parser.parseAll(parser.parsedProperty, preInputProperty) match {
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