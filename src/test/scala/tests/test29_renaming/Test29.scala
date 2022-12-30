package tests.test29_renaming

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test29 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test29_renaming"
  val resultfile = s"$TEST/dejavu-results"
  val spec1 = s"$TEST/spec1.qtl"
  val spec2 = s"$TEST/spec2.qtl"
  val log1 = s"$TEST/log1.csv"
  val log2 = s"$TEST/log2.csv"

  @Test def test1_1(): Unit = {
    Verify("--specfile", spec1, "--logfile", log1, "--resultfile", resultfile)
    checkResults(resultfile)
  }

  @Test def test1_2(): Unit = {
    Verify("--specfile", spec1, "--logfile", log2, "--resultfile", resultfile)
    checkResults(resultfile)
  }
}
