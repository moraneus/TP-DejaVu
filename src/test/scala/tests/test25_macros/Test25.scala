package tests.test25_macros

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test25 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test25_macros"
  val resultfile = s"$TEST/dejavu-results"
  val spec1 = s"$TEST/spec1.qtl"
  val spec2 = s"$TEST/spec2.qtl"
  val spec3 = s"$TEST/spec3.qtl"
  val log1 = s"$TEST/log1.csv"
  val log2 = s"$TEST/log2.csv"

  @Test def test1_1(): Unit = {
    Verify("--execution", "0", "--specfile", spec1, "--logfile", log1, "--bits", "3", "--resultfile", resultfile)
    checkResults(resultfile)
  }

  @Test def test1_2(): Unit = {
    Verify("--execution", "0", "--specfile", spec1, "--logfile", log2, "--resultfile", resultfile)
    checkResults(resultfile,19,40,59,79,99)
  }

  @Test def test2_1(): Unit = {
    Verify("--execution", "0", "--specfile", spec2, "--logfile", log1, "--resultfile", resultfile, "--bits", "3")
    checkResults(resultfile)
  }

  @Test def test2_2(): Unit = {
    Verify("--execution", "0", "--specfile", spec2, "--logfile", log2, "--resultfile", resultfile)
    checkResults(resultfile,19,40,59,79,99)
  }

  @Test def test3_1(): Unit = {
    Verify("--execution", "0", "--specfile", spec3, "--logfile", log1, "--resultfile", resultfile, "--bits", "3")
    checkResults(resultfile)
  }

  @Test def test3_2(): Unit = {
    Verify("--execution", "0", "--specfile", spec3, "--logfile", log2, "--resultfile", resultfile)
    checkResults(resultfile,19,40,59,79,99)
  }
  
}
