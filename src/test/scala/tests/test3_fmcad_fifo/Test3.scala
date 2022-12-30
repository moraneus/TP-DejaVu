package tests.test3_fmcad_fifo

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test3 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test3_fmcad_fifo"
  val resultfile = s"$TEST/dejavu-results"
  val spec = s"$TEST/spec.qtl"
  val log1 = s"$TEST/log1.csv"
  val log2 = s"$TEST/log2.csv"

  @Test def test1(): Unit = {
    Verify("--specfile", spec, "--logfile", log1, "--resultfile", resultfile)
    checkResults(resultfile,6,14,16,17,21)
  }

  @Test def test2(): Unit = {
    Verify.long("--specfile", spec, "--logfile", log2, "--resultfile", resultfile, "--bits", "13")
    checkResults(resultfile,5051)
  }
}

