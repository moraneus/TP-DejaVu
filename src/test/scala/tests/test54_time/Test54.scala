package tests.test54_time

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test54 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test54_time"
  val resultfile = s"$TEST/dejavu-results"
  val spec1 = s"$TEST/spec1.qtl"
  val spec2 = s"$TEST/spec2.qtl"
  val spec3 = s"$TEST/spec3.qtl"
  val spec4 = s"$TEST/spec4.qtl"
  val log1 = s"$TEST/log1.timed.csv"

  @Test def test1(): Unit = {
    Verify("--execution", "0", "--specfile", spec1, "--logfile", log1, "--resultfile", resultfile, "--bits", "3", "--mode", "debug")
    checkResults(resultfile, 4, 7, 9)
  }

  @Test def test2(): Unit = {
    Verify("--execution", "0", "--specfile", spec2, "--logfile", log1, "--resultfile", resultfile, "--bits", "3", "--mode", "debug")
    checkResults(resultfile, 5, 8)
  }

  @Test def test3(): Unit = {
    Verify("--execution", "0", "--specfile", spec3, "--logfile", log1, "--resultfile", resultfile, "--bits", "3", "--mode", "debug")
    checkResults(resultfile, 5, 8)
  }

  @Test def test4(): Unit = {
    Verify("--execution", "0", "--specfile", spec4, "--logfile", log1, "--resultfile", resultfile, "--bits", "3", "--mode", "debug")
    checkResults(resultfile, 4, 7, 9)
  }
}

