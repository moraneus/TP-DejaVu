package tests.test70_prediction

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test70 extends TestCase {
  val TEST: String = PATH_TO_TESTS + "/test70_prediction"
  val resultfile = s"$TEST/dejavu-results"
  val spec1 = s"$TEST/spec1.qtl"
  val log1 = s"$TEST/log1.csv"

  @Test def test1_1(): Unit = {
    Verify("--execution", "0", "--specfile", spec1, "--logfile", log1, "--resultfile", resultfile, "--bits", "10")
    // *=0 for success, *=1 for failure
    val expected = List[String]()
    checkResults(resultfile, expected:_*)
  }
}

