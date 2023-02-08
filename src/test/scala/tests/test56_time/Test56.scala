package tests.test56_time

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test56 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test56_time"
  val resultfile = s"$TEST/dejavu-results"
  val spec1 = s"$TEST/spec1.qtl"
  val log1 = s"$TEST/log1.timed.csv"

  @Test def test1_1(): Unit = {
    Verify("--execution", "0", "--specfile", spec1, "--logfile", log1, "--resultfile", resultfile, "--bits", "3", "--mode", "debug")
    checkResults(resultfile, 1674,1680,15808,32618,32717,38605,42991,48749)
  }
}
