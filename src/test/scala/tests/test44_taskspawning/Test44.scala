package tests.test44_taskspawning

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test44 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test44_taskspawning"
  val resultfile = s"$TEST/dejavu-results"
  val spec = s"$TEST/spec.qtl"
  val log1 = s"$TEST/log1.csv"
  val log2 = s"$TEST/log2.csv"

  @Test def test1(): Unit = {
    Verify("--execution", "0", "--specfile", spec, "--logfile", log1, "--resultfile", resultfile)
    checkResults(resultfile,16,22,27,30)
  }

  @Test def test2(): Unit = {
    Verify("--execution", "0", "--specfile", spec, "--logfile", log2, "--resultfile", resultfile)
    checkResults(resultfile)
  }
}

