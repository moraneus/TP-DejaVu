package tests.test58_prediction

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test58 extends TestCase {
  val TEST: String = PATH_TO_TESTS + "/test58_prediction"
  val resultfile = s"$TEST/dejavu-results"
  val spec1 = s"$TEST/spec1.qtl"
  val log1 = s"$TEST/log1.csv"
  val log2 = s"$TEST/log2.csv"

  @Test def test1_1(): Unit = {
    Verify("--specfile", spec1, "--logfile", log1, "--resultfile", resultfile)
    checkResults(resultfile,4,5,6)
  }

  @Test def test1_2(): Unit = {
    Verify(spec1, log2)
    checkResults(resultfile, 2,3,5,6)
  }
}

