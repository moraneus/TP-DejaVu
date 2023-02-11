package tests.test67_prediction

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test67 extends TestCase {
  val TEST: String = PATH_TO_TESTS + "/test67_prediction"
  val resultfile = s"$TEST/dejavu-results"
  val spec1 = s"$TEST/spec1.qtl"
  val spec2 = s"$TEST/spec2.qtl"
  val log1 = s"$TEST/log1.csv"

  @Test def test1_1(): Unit = {
    Verify("--execution", "0", "--specfile", spec1, "--logfile", log1, "--resultfile", resultfile, "--bits", "5", "--prediction", "2", "--prediction_type", "smart")
    // *=0 for success, *=1 for failure
    val expected = List[String](
    )
    checkResults(resultfile, expected:_*)
  }
}

