package tests.test27_quantrenaming

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test27 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test27_quantrenaming"
  val resultfile = s"$TEST/dejavu-results"
  val spec1 = s"$TEST/spec1.qtl"
  val spec2 = s"$TEST/spec2.qtl"
  val log1 = s"$TEST/log1.csv"
  val log2 = s"$TEST/log2.csv"

  @Test def test1_1(): Unit = {
    Verify("--execution", "0", "--specfile", spec1, "--logfile", log1, "--resultfile", resultfile)
    checkResults(resultfile)
  }

  @Test def test1_2(): Unit = {
    Verify("--execution", "0", "--specfile", spec1, "--logfile", log2, "--resultfile", resultfile)
    checkResults(resultfile,5,6,8,9)
  }

  @Test def test2_1(): Unit = {
    Verify("--execution", "0", "--specfile", spec2, "--logfile", log1, "--resultfile", resultfile)
    checkResults(resultfile)
  }

  @Test def test2_2(): Unit = {
    Verify("--execution", "0", "--specfile", spec2, "--logfile", log2, "--resultfile", resultfile)
    checkResults(resultfile,5,6)
  }
}
