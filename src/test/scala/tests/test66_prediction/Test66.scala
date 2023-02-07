package tests.test66_prediction

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test66 extends TestCase {
  val TEST: String = PATH_TO_TESTS + "/test66_prediction"
  val resultfile = s"$TEST/dejavu-results"
  val spec1 = s"$TEST/spec1.qtl"
  val spec2 = s"$TEST/spec2.qtl"
  val log1 = s"$TEST/log1.csv"

  @Test def test1_1(): Unit = {
    Verify("--specfile", spec1, "--logfile", log1, "--resultfile", resultfile, "--bits", "5", "--prediction", "2", "--prediction_type", "smart")
    // *=0 for success, *=1 for failure
    val expected = List[String](
    )
    checkResults(resultfile, expected:_*)
  }
}
