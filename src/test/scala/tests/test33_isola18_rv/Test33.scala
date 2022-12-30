package tests.test33_isola18_rv

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test33 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test33_isola18_rv"
  val resultfile = s"$TEST/dejavu-results"
  val spec = s"$TEST/spec.qtl"
  val log1 = s"$TEST/log1.csv"

  @Test def test1(): Unit = {
    Verify("--specfile", spec, "--logfile", log1, "--resultfile", resultfile, "--bits", "3")
    checkResults(resultfile,1)
  }
}

