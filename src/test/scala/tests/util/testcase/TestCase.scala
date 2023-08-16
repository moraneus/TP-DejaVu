package tests.util.testcase

import dejavu.Settings
import dejavu.Verify
import dejavu.Options
import org.junit.Before

import scala.io.Source

class TestCase {
  Options.UNIT_TEST = true

  val PATH_TO_TESTS = Settings.PROJECT_DIR + "/src/test/scala/tests"

  var wfErrorOccurred: Boolean = false

  @Before def before(): Unit = {
    wfErrorOccurred = false
    // println("before executed")
  }

  def VerifyNotWF(spec: String): Unit = {
    try {
      Verify("--specfile", spec)
    } catch {
      case e: Throwable if e.getMessage == "WF_ERROR" =>
        wfErrorOccurred = true
        // println(s"wfErrorOccurred = $wfErrorOccurred")
    }
    assert(wfErrorOccurred)
  }

  def readResult(resultfile: String): List[String] = {
    val data = Source.fromFile(resultfile)
    val result = data.getLines.toList
    data.close()
    result
  }

  def checkResults(resultfile: String, lines: Any*): Unit = {
    if (Verify.verified) {
      val expect = lines.toList.map(_.toString).sorted
      val result = readResult(resultfile).sorted
      // val result = readResult(resultfile).distinct.sorted

      assert(expect == result,
        s"""
           |expected : (${expect.mkString(",")})
           |=/=
           |observed : (${result.mkString(",")})
       """.stripMargin)
    }
  }

  def checkResultsBrief(resultfile: String, lines: Any*): Unit = {
    if (Verify.verified) {
      val expect = lines.toList.map(_.toString).sorted
      val result = readResult(resultfile).sorted
      //  val result = readResult(resultfile).distinct.sorted
      val gcSize = result.filter(_.contains("--")).size // GC: 'lineNr -- value'
      println(s"GARBAGE COLLECTED $gcSize VALUES")
      val resultWithoutGC = result.filterNot(_.contains("--")).sorted
      val resultGCSum = resultWithoutGC ++ (if (gcSize > 0) List(s"$gcSize gc") else Nil).sorted
      assert(expect == resultGCSum,
        s"""
           |expected : (${expect.mkString(",")})
           |=/=
           |observed : (${resultGCSum.mkString(",")})
       """.stripMargin)
    }
  }

  implicit def liftInt(number: Int) = new {
    def --(value: Any) : String = {
      s"$number -- $value" // line number and value garbage collected
    }

    def oom: String = {
      s"$number oom" // line number in which we ran out of BDD memory (not enough bits)
    }

    def gc: String = {
      s"$number gc" // number of values garbage collected in total
    }
  }
}
