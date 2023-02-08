package tests.test57_synthesis_time

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test57 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test57_synthesis_time"
  val resultfile = s"$TEST/dejavu-results"
  val spec = s"$TEST/spec.qtl"
  val log = s"$TEST/spec.csv"

  @Test def test1_1(): Unit = {
    Verify("--execution", "0", "--specfile", spec, "--logfile", log, "--resultfile", resultfile, "--bits", "3")
    checkResults(resultfile)
  }
}
