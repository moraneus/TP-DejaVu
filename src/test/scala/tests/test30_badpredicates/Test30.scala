package tests.test30_badpredicates

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test30 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test30_badpredicates"
  val resultfile = s"$TEST/dejavu-results"
  val spec1 = s"$TEST/spec1.qtl"
  val spec2 = s"$TEST/spec2.qtl"
  val log1 = s"$TEST/log1.csv"
  val log2 = s"$TEST/log2.csv"

  @Test def test1(): Unit = {
    VerifyNotWF(spec1)
  }

  @Test def test2_1(): Unit = {
    Verify("--execution", "0", "--specfile", spec2, "--logfile", log1, "--resultfile", resultfile)
    checkResults(resultfile)
  }
}
