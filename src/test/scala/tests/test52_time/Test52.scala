package tests.test52_time

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test52 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test52_time"
  val resultfile = s"$TEST/dejavu-results"
  val spec = s"$TEST/spec.qtl"
  val log1 = s"$TEST/log1.timed.csv"
  val log2 = s"$TEST/log2.timed.csv"
  val log3 = s"$TEST/log3.timed.csv"

  @Test def test1(): Unit = {
    Verify(spec, log1, "3", "debug")
    checkResults(resultfile)
  }

  @Test def test2(): Unit = {
    Verify(spec, log2, "3", "debug")
    checkResults(resultfile, 3, 4)
  }

  @Test def test3(): Unit = {
    Verify(spec, log3, "3", "debug")
    checkResults(resultfile, 4, 10, 13)
  }
}

