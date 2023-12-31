package tests.test19_relations

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test19 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test19_relations"
  val resultfile = s"$TEST/dejavu-results"
  val spec1 = s"$TEST/spec1.qtl"
  val spec2 = s"$TEST/spec2.qtl"
  val spec3 = s"$TEST/spec3.qtl"
  val spec4 = s"$TEST/spec4.qtl"
  val log1 = s"$TEST/log1.csv"
  val log2 = s"$TEST/log2.csv"

  @Test def test1_1(): Unit = {
    Verify("--execution", "0", "--specfile", spec1, "--logfile", log1, "--resultfile", resultfile, "--bits", "3")
    checkResultsBrief(resultfile, 7)
  }

  @Test def test2_1(): Unit = {
    Verify("--execution", "0", "--specfile", spec2, "--logfile", log1, "--resultfile", resultfile, "--bits", "3")
    checkResultsBrief(resultfile, 8,11)
  }

  @Test def test3_1(): Unit = {
    Verify("--execution", "0", "--specfile", spec3, "--logfile", log1, "--resultfile", resultfile, "--bits", "3")
    checkResultsBrief(resultfile, 12)
  }

  @Test def test4_1(): Unit = {
    Verify("--execution", "0", "--specfile", spec4, "--logfile", log1, "--resultfile", resultfile, "--bits", "3")
    checkResultsBrief(resultfile, 9)
  }

  // TODO: large traces stress testing relations
}

