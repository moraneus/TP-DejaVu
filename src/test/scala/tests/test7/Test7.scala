package tests.test7

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test7 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test7"
  val resultfile = s"$TEST/dejavu-results"
  val spec = s"$TEST/spec.qtl"
  val log1 = s"$TEST/log1.csv"
  val log2 = s"$TEST/log2.csv"

  @Test def test1(): Unit = {
    // println(System.getProperty("user.dir"))
    Verify("--execution", "0", "--specfile", spec, "--logfile", log1, "--resultfile", resultfile)
    checkResults(resultfile, 7,9)
  }

  @Test def test2(): Unit = {
    Verify("--execution", "0", "--specfile", spec, "--logfile", log2, "--resultfile", resultfile)
    checkResults(resultfile)
  }
}
