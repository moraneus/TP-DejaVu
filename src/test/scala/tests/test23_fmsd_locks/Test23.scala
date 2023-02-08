package tests.test23_fmsd_locks

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test23 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test23_fmsd_locks"
  val resultfile = s"$TEST/dejavu-results"
  val spec1 = s"$TEST/spec1.qtl"
  val spec2 = s"$TEST/spec2.qtl"
  val spec3 = s"$TEST/spec3.qtl"
  val spec4 = s"$TEST/spec4.qtl" // the union
  val spec5 = s"$TEST/spec5.qtl"
  val log1 = s"$TEST/log1.csv"
  val log2 = s"$TEST/log2.csv"
  val log3 = s"$TEST/log3.csv"
  val log4 = s"$TEST/log4.csv"
  val log5 = s"$TEST/log5.csv"
  val log6 = s"$TEST/log6.csv"
  val log7 = s"$TEST/log7.csv"
  val log8 = s"$TEST/log8.csv"
  val log9 = s"$TEST/log9.csv"
  val log10 = s"$TEST/log10.csv"
  val log11 = s"$TEST/log11.csv"
  val log12 = s"$TEST/log12.csv"

  @Test def test1_1(): Unit = {
    Verify("--execution", "0", "--specfile", spec1, "--logfile", log1, "--resultfile", resultfile)
    checkResults(resultfile)
  }

  @Test def test1_2(): Unit = {
    Verify("--execution", "0", "--specfile", spec1, "--logfile", log2, "--resultfile", resultfile)
    checkResults(resultfile,5,10,11)
  }

  @Test def test2_3(): Unit = {
    Verify("--execution", "0", "--specfile", spec2, "--logfile", log3, "--resultfile", resultfile)
    checkResults(resultfile)
  }

  @Test def test2_4(): Unit = {
    Verify("--execution", "0", "--specfile", spec2, "--logfile", log4, "--resultfile", resultfile)
    checkResults(resultfile,10)
  }

  @Test def test3_5(): Unit = {
    Verify("--execution", "0", "--specfile", spec3, "--logfile", log5, "--resultfile", resultfile)
    checkResults(resultfile)
  }

  @Test def test3_6(): Unit = {
    Verify("--execution", "0", "--specfile", spec3, "--logfile", log6, "--resultfile", resultfile)
    checkResults(resultfile,5,13)
  }

  @Test def test4_7(): Unit = {
    Verify("--execution", "0", "--specfile", spec4, "--logfile", log7, "--resultfile", resultfile)
    checkResults(resultfile)
  }

  @Test def test4_8(): Unit = {
    Verify("--execution", "0", "--specfile", spec4, "--logfile", log8, "--resultfile", resultfile)
    checkResults(resultfile,5,6)
  }

  @Test def test4_9(): Unit = {
    Verify("--execution", "0", "--specfile", spec4, "--logfile", log9, "--resultfile", resultfile)
    checkResults(resultfile,7)
  }

  @Test def test5_10(): Unit = {
    Verify("--execution", "0", "--specfile", spec5, "--logfile", log10, "--resultfile", resultfile)
    checkResults(resultfile)
  }

  @Test def test5_11(): Unit = {
    Verify("--execution", "0", "--specfile", spec5, "--logfile", log11, "--resultfile", resultfile)
    checkResults(resultfile,5,10,11)
  }

  // TODO: testing large traces

  // cycles:
  @Test def test2_12(): Unit = {
    Verify("--execution", "0", "--specfile", spec2, "--logfile", log12, "--resultfile", resultfile)
    checkResults(resultfile)
  }

  // data races:
  @Test def test4_12(): Unit = {
    Verify("--execution", "0", "--specfile", spec4, "--logfile", log12, "--resultfile", resultfile)
    checkResults(resultfile,37)
  }

  // basic:
  @Test def test5_12(): Unit = {
    Verify("--execution", "0", "--specfile", spec5, "--logfile", log12, "--resultfile", resultfile)
    checkResults(resultfile)
  }
}
