package tests.test15

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test15 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test15"
  val resultfile = s"$TEST/dejavu-results"
  val spec = s"$TEST/spec.qtl"
  val log1 = s"$TEST/log1.csv"
  val log2 = s"$TEST/log2.csv"

  @Test def test1(): Unit = {
    Verify("--execution", "0", "--specfile", spec, "--logfile", log1, "--resultfile", resultfile, "--bits", "3")
    checkResults(resultfile, 7,8,9,10 -- 5,10 -- 7,10 -- 6,10,11)
  }

  @Test def test2(): Unit = {
    Verify("--execution", "0", "--specfile", spec, "--logfile", log2, "--resultfile", resultfile, "--bits", "3")
    checkResults(resultfile, 1,2,3,6,7,8,9 -- 5,9 -- 7,9 -- 6,9,10)
  }
}

