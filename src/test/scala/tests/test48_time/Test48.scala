package tests.test48_time

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test48 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test48_time"
  val resultfile = s"$TEST/dejavu-results"
  val spec = s"$TEST/spec.qtl"
  val log1 = s"$TEST/log1.timed.csv"

  @Test def test1(): Unit = {
    Verify(spec,log1, "3", "debug")
    checkResults(resultfile, 5)
  }
}

