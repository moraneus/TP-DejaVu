package tests.test17

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test17 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test17"
  val resultfile = s"$TEST/dejavu-results"
  val spec = s"$TEST/spec.qtl"
  val log1 = s"$TEST/log1.csv"
  val log2 = s"$TEST/log2.csv"

  @Test def test1(): Unit = {
    Verify("--specfile", spec, "--logfile", log1, "--resultfile", resultfile, "--bits", "3")
    checkResults(resultfile, 6,7)
  }

  @Test def test2(): Unit = {
    Verify("--specfile", spec, "--logfile", log2, "--resultfile", resultfile, "--bits", "3")
    checkResults(resultfile, 1,3,5,7)
  }
}

