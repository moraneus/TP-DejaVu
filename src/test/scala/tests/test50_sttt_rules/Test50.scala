package tests.test50_sttt_rules

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test50 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test50_sttt_rules"
  val resultfile = s"$TEST/dejavu-results"
  val spec = s"$TEST/spec.qtl"
  val log1 = s"$TEST/log1.csv"
  val log2 = s"$TEST/log2.csv"

  @Test def test1(): Unit = {
    Verify(spec,log1,"3", "debug")
    checkResults(resultfile)
  }

  @Test def test2(): Unit = {
    Verify(spec,log2,"3")
    checkResults(resultfile, 5)
  }
}

