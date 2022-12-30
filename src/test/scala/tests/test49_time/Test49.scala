package tests.test49_time

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test49 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test49_time"
  val resultfile = s"$TEST/dejavu-results"
  val spec1 = s"$TEST/spec1.qtl" // untimed
  val spec2 = s"$TEST/spec2.qtl" // timed
  val log1 = s"$TEST/log1.timed.csv"

   @Test def test1(): Unit = {
     Verify("--specfile", spec1, "--logfile", log1, "--resultfile", resultfile, "--bits", "3", "--mode", "debug")
     checkResults(resultfile)
  }

  @Test def test2(): Unit = {
    Verify("--specfile", spec2, "--logfile", log1, "--resultfile", resultfile, "--bits", "3", "--mode", "debug")
    checkResults(resultfile)
  }
}

