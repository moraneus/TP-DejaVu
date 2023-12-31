package tests.test20

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test20 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test20"
  val resultfile = s"$TEST/dejavu-results"
  val spec1 = s"$TEST/spec1.qtl"
  val spec2 = s"$TEST/spec2.qtl"
  val spec3 = s"$TEST/spec3.qtl"
  val spec4 = s"$TEST/spec4.qtl"
  val spec5 = s"$TEST/spec5.qtl"
  val log1 = s"$TEST/log1.csv"
  val log2 = s"$TEST/log2.csv"
  val log3 = s"$TEST/log3.csv"
  val log4 = s"$TEST/log4.csv"

  @Test def test1_1(): Unit = {
    Verify("--execution", "0", "--specfile", spec1, "--logfile", log1, "--resultfile", resultfile, "--bits", "3")
    checkResults(resultfile, 7)
  }

  @Test def test2_1(): Unit = {
    Verify("--execution", "0", "--specfile", spec2, "--logfile", log1, "--resultfile", resultfile, "--bits", "3")
    checkResults(resultfile, 7)
  }

  @Test def test3_2(): Unit = {
    Verify("--execution", "0", "--specfile", spec3, "--logfile", log2, "--resultfile", resultfile, "--bits", "3")
    checkResults(resultfile, 10)
  }

  @Test def test4_2(): Unit = {
    Verify("--execution", "0", "--specfile", spec4, "--logfile", log2, "--resultfile", resultfile, "--bits", "3")
    checkResults(resultfile, 10)
  }

  @Test def test5_3(): Unit = {
    Verify("--execution", "0", "--specfile", spec5, "--logfile", log3, "--resultfile", resultfile, "--bits", "3")
    checkResults(resultfile, 7)
  }

  @Test def test5_4(): Unit = {
    Verify("--execution", "0", "--specfile", spec5, "--logfile", log4, "--resultfile", resultfile, "--bits", "3")
    checkResults(resultfile, 3)
  }
}



