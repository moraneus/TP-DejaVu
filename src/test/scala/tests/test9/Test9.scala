package tests.test9

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

/**
  * Testing that quantification is over "all values" in the (infinite) domain,
  * and not just over the values seen (or the values in the trace as in QEA).
  */

class Test9 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test9"
  val resultfile = s"$TEST/dejavu-results"
  val spec = s"$TEST/spec.qtl"
  val log1 = s"$TEST/log1.csv"
  val log2 = s"$TEST/log2.csv"
  val log3 = s"$TEST/log3.csv"
  val log4 = s"$TEST/log4.csv"

  @Test def test1(): Unit = {
    Verify("--execution", "0", "--specfile", spec, "--logfile", log1, "--resultfile", resultfile, "--bits", "3")
    checkResults(resultfile)
  }

  @Test def test2(): Unit = {
    Verify("--execution", "0", "--specfile", spec, "--logfile", log2, "--resultfile", resultfile, "--bits", "3")
    checkResults(resultfile, 1)
  }

  @Test def test3(): Unit = {
    Verify("--execution", "0", "--specfile", spec, "--logfile", log3, "--resultfile", resultfile, "--bits", "3")
    checkResults(resultfile, 1, 2)
  }

  @Test def test4(): Unit = {
    Verify("--execution", "0", "--specfile", spec, "--logfile", log4, "--resultfile", resultfile, "--bits", "2")
    checkResults(resultfile, 1, 2, 3)
  }
}

