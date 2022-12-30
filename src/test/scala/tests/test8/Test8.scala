package tests.test8

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test8 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test8"
  val resultfile = s"$TEST/dejavu-results"
  val spec = s"$TEST/spec.qtl"
  val log1 = s"$TEST/log1.csv"
  val log2 = s"$TEST/log2.csv"
  val log3 = s"$TEST/log3.csv"

  @Test def test1(): Unit = {
    Verify(spec,log1, "3")
    checkResults(resultfile)
  }

  @Test def test2(): Unit = {
    Verify(spec,log2, "3")
    checkResults(resultfile, 7)
  }

  @Test def test3(): Unit = {
    Verify(spec,log3, "3")
    checkResults(resultfile, 7, 8)
  }
}

