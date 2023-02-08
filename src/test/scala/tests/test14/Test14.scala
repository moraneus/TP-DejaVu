package tests.test14

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test14 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test14"
  val resultfile = s"$TEST/dejavu-results"
  val spec = s"$TEST/spec.qtl"
  val log1 = s"$TEST/log1.csv"
  val log2 = s"$TEST/log2.csv"

  @Test def test1(): Unit = {
    Verify("--execution", "0", "--specfile", spec, "--logfile", log1, "--resultfile", resultfile, "--bits", "3")
    checkResults(resultfile, 1,2,3)
  }

  @Test def test2(): Unit = {
    Verify("--execution", "0", "--specfile", spec, "--logfile", log2, "--resultfile", resultfile, "--bits", "3")
    checkResults(resultfile,1,2,3,4,5)
  }
}

