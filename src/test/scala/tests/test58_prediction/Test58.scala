package tests.test58_prediction

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test58 extends TestCase {
  val TEST: String = PATH_TO_TESTS + "/test58_prediction"
  val resultfile = s"$TEST/dejavu-results"
  val spec1 = s"$TEST/spec1.qtl"
  val log1 = s"$TEST/log1.csv"
  val log2 = s"$TEST/log2.csv"

  @Test def test1_1(): Unit = {
    Verify("--specfile", spec1, "--logfile", log1, "--resultfile", resultfile, "--prediction", "2", "--bits", "5")
    // *=0 for success, *=1 for failure
    val expected = List[String](
      "1",
      "2",
      "q(21)=1;q(211)=1",
      "q(21)=1;q(21)=1",
      "q(21)=1;q(1)=1",
      "q(1)=1;q(21)=1",
      "q(1)=1;q(1)=1",
      "q(1)=1;q(2)=1",
      "q(2)=1;q(21)=1",
      "q(2)=1;q(1)=1",
      "q(2)=1;q(2)=1"
    )
    checkResults(resultfile, expected:_*)
  }

  @Test def test1_2(): Unit = {
    Verify("--specfile", spec1, "--logfile", log2, "--resultfile", resultfile, "--prediction", "2")
    // *=0 for success, *=1 for failure
    val expected = List[String](
      "1",
      "2",
      "3",
      "q(2)=1;q(2)=1",
      "q(2)=1;q(4)=1",
      "q(2)=1;q(61)=1",
      "q(6)=1;q(2)=1",
      "q(6)=1;q(6)=1",
      "q(6)=1;q(61)=1",
      "q(61)=1;q(2)=1",
      "q(61)=1;q(61)=1",
      "q(61)=1;q(611)=1")
    checkResults(resultfile, expected:_*)
  }
}

