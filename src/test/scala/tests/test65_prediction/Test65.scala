package tests.test65_prediction

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test65 extends TestCase {
  val TEST: String = PATH_TO_TESTS + "/test65_prediction"
  val resultfile = s"$TEST/dejavu-results"
  val spec1 = s"$TEST/spec1.qtl"
  val spec2 = s"$TEST/spec2.qtl"
  val log1 = s"$TEST/log1.csv"

  /**
    * Should success only for g(a).
    */
  @Test def test1_1(): Unit = {
    Verify("--execution", "0", "--specfile", spec1, "--logfile", log1, "--resultfile", resultfile, "--bits", "7", "--prediction", "2")
    // *=0 for success, *=1 for failure
    val expected = List[String](
      "1",
      "g(x1)=1;g(x11)=1",
      "g(x1)=1;p(x11)=1",
      "g(x1)=1;g(x)=1",
      "g(x1)=1;p(x)=1",
      "g(x1)=1;g(x1)=1",
      "g(x1)=1;p(x1)=1",
      "p(x1)=1;g(x11)=1",
      "p(x1)=1;p(x11)=1",
      "p(x1)=1;g(x)=1",
      "p(x1)=1;p(x)=1",
      "p(x1)=1;g(x1)=0",
      "p(x1)=1;p(x1)=1",
      "g(x)=1;g(x1)=1",
      "g(x)=1;p(x1)=1",
      "g(x)=1;g(x)=1",
      "g(x)=1;p(x)=1",
      "p(x)=1;g(x1)=1",
      "p(x)=1;p(x1)=1",
      "p(x)=1;g(x)=1",
      "p(x)=1;p(x)=1"
    )
    checkResults(resultfile, expected:_*)
  }

  /**
    * Should success only for g(a).
    */
  @Test def test1_2(): Unit = {
    Verify("--execution", "0", "--specfile", spec1, "--logfile", log1, "--resultfile", resultfile, "--bits", "5", "--prediction", "3")
    // *=0 for success, *=1 for failure
    val expected = List[String](
      "1",
      "g(x1)=1;g(x11)=1;g(x)=1",
      "g(x1)=1;g(x11)=1;p(x)=1",
      "g(x1)=1;g(x11)=1;g(x11)=1",
      "g(x1)=1;g(x11)=1;p(x11)=1",
      "g(x1)=1;g(x11)=1;g(x1)=1",
      "g(x1)=1;g(x11)=1;p(x1)=1",
      "g(x1)=1;p(x11)=1;g(x)=1",
      "g(x1)=1;p(x11)=1;p(x)=1",
      "g(x1)=1;p(x11)=1;g(x11)=0",
      "g(x1)=1;p(x11)=1;p(x11)=1",
      "g(x1)=1;p(x11)=1;g(x1)=1",
      "g(x1)=1;p(x11)=1;p(x1)=1",
      "g(x1)=1;g(x)=1;g(x11)=1",
      "g(x1)=1;g(x)=1;p(x11)=1",
      "g(x1)=1;g(x)=1;g(x)=1",
      "g(x1)=1;g(x)=1;p(x)=1",
      "g(x1)=1;g(x)=1;g(x1)=1",
      "g(x1)=1;g(x)=1;p(x1)=1",
      "g(x1)=1;p(x)=1;g(x11)=1",
      "g(x1)=1;p(x)=1;p(x11)=1",
      "g(x1)=1;p(x)=1;g(x)=0",
      "g(x1)=1;p(x)=1;p(x)=1",
      "g(x1)=1;p(x)=1;g(x1)=1",
      "g(x1)=1;p(x)=1;p(x1)=1",
      "g(x1)=1;g(x1)=1;g(x)=1",
      "g(x1)=1;g(x1)=1;p(x)=1",
      "g(x1)=1;g(x1)=1;g(x1)=1",
      "g(x1)=1;g(x1)=1;p(x1)=1",
      "g(x1)=1;p(x1)=1;g(x)=1",
      "g(x1)=1;p(x1)=1;p(x)=1",
      "g(x1)=1;p(x1)=1;g(x1)=0",
      "g(x1)=1;p(x1)=1;p(x1)=1",
      "p(x1)=1;g(x11)=1;g(x)=1",
      "p(x1)=1;g(x11)=1;p(x)=1",
      "p(x1)=1;g(x11)=1;g(x11)=1",
      "p(x1)=1;g(x11)=1;p(x11)=1",
      "p(x1)=1;g(x11)=1;g(x1)=1",
      "p(x1)=1;g(x11)=1;p(x1)=1",
      "p(x1)=1;p(x11)=1;g(x)=1",
      "p(x1)=1;p(x11)=1;p(x)=1",
      "p(x1)=1;p(x11)=1;g(x11)=0",
      "p(x1)=1;p(x11)=1;p(x11)=1",
      "p(x1)=1;p(x11)=1;g(x1)=1",
      "p(x1)=1;p(x11)=1;p(x1)=1",
      "p(x1)=1;g(x)=1;g(x11)=1",
      "p(x1)=1;g(x)=1;p(x11)=1",
      "p(x1)=1;g(x)=1;g(x)=1",
      "p(x1)=1;g(x)=1;p(x)=1",
      "p(x1)=1;g(x)=1;g(x1)=1",
      "p(x1)=1;g(x)=1;p(x1)=1",
      "p(x1)=1;p(x)=1;g(x11)=1",
      "p(x1)=1;p(x)=1;p(x11)=1",
      "p(x1)=1;p(x)=1;g(x)=0",
      "p(x1)=1;p(x)=1;p(x)=1",
      "p(x1)=1;p(x)=1;g(x1)=1",
      "p(x1)=1;p(x)=1;p(x1)=1",
      "p(x1)=1;g(x1)=0;g(x)=1",
      "p(x1)=1;g(x1)=0;p(x)=1",
      "p(x1)=1;g(x1)=0;g(x1)=1",
      "p(x1)=1;g(x1)=0;p(x1)=1",
      "p(x1)=1;p(x1)=1;g(x)=1",
      "p(x1)=1;p(x1)=1;p(x)=1",
      "p(x1)=1;p(x1)=1;g(x1)=1",
      "p(x1)=1;p(x1)=1;p(x1)=1",
      "g(x)=1;g(x1)=1;g(x11)=1",
      "g(x)=1;g(x1)=1;p(x11)=1",
      "g(x)=1;g(x1)=1;g(x)=1",
      "g(x)=1;g(x1)=1;p(x)=1",
      "g(x)=1;g(x1)=1;g(x1)=1",
      "g(x)=1;g(x1)=1;p(x1)=1",
      "g(x)=1;p(x1)=1;g(x11)=1",
      "g(x)=1;p(x1)=1;p(x11)=1",
      "g(x)=1;p(x1)=1;g(x)=1",
      "g(x)=1;p(x1)=1;p(x)=1",
      "g(x)=1;p(x1)=1;g(x1)=0",
      "g(x)=1;p(x1)=1;p(x1)=1",
      "g(x)=1;g(x)=1;g(x1)=1",
      "g(x)=1;g(x)=1;p(x1)=1",
      "g(x)=1;g(x)=1;g(x)=1",
      "g(x)=1;g(x)=1;p(x)=1",
      "g(x)=1;p(x)=1;g(x1)=1",
      "g(x)=1;p(x)=1;p(x1)=1",
      "g(x)=1;p(x)=1;g(x)=1",
      "g(x)=1;p(x)=1;p(x)=1",
      "p(x)=1;g(x1)=1;g(x11)=1",
      "p(x)=1;g(x1)=1;p(x11)=1",
      "p(x)=1;g(x1)=1;g(x)=1",
      "p(x)=1;g(x1)=1;p(x)=1",
      "p(x)=1;g(x1)=1;g(x1)=1",
      "p(x)=1;g(x1)=1;p(x1)=1",
      "p(x)=1;p(x1)=1;g(x11)=1",
      "p(x)=1;p(x1)=1;p(x11)=1",
      "p(x)=1;p(x1)=1;g(x)=1",
      "p(x)=1;p(x1)=1;p(x)=1",
      "p(x)=1;p(x1)=1;g(x1)=0",
      "p(x)=1;p(x1)=1;p(x1)=1",
      "p(x)=1;g(x)=1;g(x1)=1",
      "p(x)=1;g(x)=1;p(x1)=1",
      "p(x)=1;g(x)=1;g(x)=1",
      "p(x)=1;g(x)=1;p(x)=1",
      "p(x)=1;p(x)=1;g(x1)=1",
      "p(x)=1;p(x)=1;p(x1)=1",
      "p(x)=1;p(x)=1;g(x)=1",
      "p(x)=1;p(x)=1;p(x)=1"
    )
    checkResults(resultfile, expected: _*)
  }
}

