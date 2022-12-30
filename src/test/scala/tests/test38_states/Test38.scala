package tests.test38_states

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test38 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test38_states"
  val resultfile = s"$TEST/dejavu-results"
  val spec = s"$TEST/spec.qtl"
  val log1 = s"$TEST/log1.csv"
  val log2 = s"$TEST/log2.csv"

  @Test def test1(): Unit = {
    Verify("--specfile", spec, "--logfile", log1, "--resultfile", resultfile, "--bits", "4")
    checkResults(resultfile,11)
  }

  @Test def test2(): Unit = {
    Verify("--specfile", spec, "--logfile", log2, "--resultfile", resultfile, "--bits", "4")
    checkResults(resultfile,12)
  }
}

