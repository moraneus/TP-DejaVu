package tests.test60_prediction

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

/**
  * This test should not run the prediction since some of
  * the predicates in the specification get more than one value.
  */
class Test60 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test60_prediction"
  val resultfile = s"$TEST/dejavu-results"
  val spec1 = s"$TEST/spec1.qtl"
  val spec2 = s"$TEST/spec2.qtl"
  val log1 = s"$TEST/log1.csv"
  val log2 = s"$TEST/log2.csv"

  @Test def test1(): Unit = {
    Verify("--specfile", spec1, "--logfile", log1, "--resultfile", resultfile, "--bits", "3", "--prediction", "10")
    checkResults(resultfile, 1,2,3,4,5,6,7)
  }

  @Test def test2(): Unit = {
    Verify("--specfile", spec1, "--logfile", log2, "--resultfile", resultfile, "--bits", "3", "--prediction", "10")
    checkResults(resultfile, 1,2,3,4,5,6,7,8)
  }

  @Test def test3(): Unit = {
    Verify("--specfile", spec2, "--logfile", log1, "--resultfile", resultfile, "--bits", "3", "--prediction", "10")
    checkResults(resultfile, 2,3,4,5,6)
  }

  @Test def test4(): Unit = {
    Verify("--specfile", spec2, "--logfile", log2, "--resultfile", resultfile, "--bits", "3", "--prediction", "10")
    checkResults(resultfile, 1,3,5,7)
  }
}

