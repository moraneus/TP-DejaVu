package tests.test59_prediction

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test59 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test59_prediction"
  val resultfile = s"$TEST/dejavu-results"
  val spec = s"$TEST/spec.qtl"
  val log1 = s"$TEST/log1.csv"

  @Test def test1(): Unit = {
    Verify("--specfile", spec, "--logfile", log1, "--resultfile", resultfile, "--bits", "3", "--prediction", "1")
    // *=0 for success, *=1 for failure
    val expected = List[String](
      "6",
      "dispatch(off)=0",
      "dispatch(stop)=0",
      "dispatch(stop1)=0",
      "fail(off)=0",
      "fail(stop)=0",
      "fail(stop1)=0",
      "success(off)=1",
      "success(stop)=0",
      "success(stop1)=1")
    checkResults(resultfile, expected:_*)
  }

  @Test def test2(): Unit = {
    Verify("--specfile", spec, "--logfile", log1, "--resultfile", resultfile, "--bits", "3", "--prediction", "2")
    // *=0 for success, *=1 for failure
    val expected = List[String](
      "6",
      "success(stop)=0;dispatch(off)=0",
      "success(stop)=0;dispatch(stop)=0",
      "success(stop)=0;fail(off)=0",
      "success(stop)=0;fail(stop)=0",
      "success(stop)=0;success(off)=1",
      "success(stop)=0;success(stop)=0",
      "success(stop1)=1;dispatch(off)=0",
      "success(stop1)=1;dispatch(stop)=0",
      "dispatch(stop)=0;dispatch(off)=0",
      "dispatch(stop)=0;dispatch(stop)=0",
      "dispatch(stop)=0;fail(off)=0",
      "dispatch(stop)=0;fail(stop)=0",
      "dispatch(stop)=0;success(off)=1",
      "dispatch(stop)=0;success(stop)=0",
      "dispatch(stop1)=0;dispatch(off)=0",
      "dispatch(stop1)=0;dispatch(stop)=0",
      "dispatch(stop1)=0;dispatch(stop1)=0",
      "dispatch(stop1)=0;fail(off)=0",
      "dispatch(stop1)=0;fail(stop)=0",
      "dispatch(stop1)=0;fail(stop1)=0",
      "dispatch(stop1)=0;success(off)=1",
      "dispatch(stop1)=0;success(stop)=0",
      "dispatch(stop1)=0;success(stop1)=0",
      "fail(off)=0;dispatch(off)=0",
      "fail(off)=0;dispatch(stop)=0",
      "fail(off)=0;dispatch(stop1)=0",
      "fail(off)=0;fail(off)=0",
      "fail(off)=0;fail(stop)=0",
      "fail(off)=0;fail(stop1)=0",
      "fail(off)=0;success(off)=1",
      "fail(off)=0;success(stop)=0",
      "fail(off)=0;success(stop1)=1",
      "fail(stop)=0;dispatch(off)=0",
      "fail(stop)=0;dispatch(stop)=0",
      "fail(stop)=0;fail(off)=0",
      "fail(stop)=0;fail(stop)=0",
      "fail(stop)=0;success(off)=1",
      "fail(stop)=0;success(stop)=1",
      "fail(stop1)=0;dispatch(off)=0",
      "fail(stop1)=0;dispatch(stop)=0",
      "fail(stop1)=0;dispatch(stop1)=0",
      "fail(stop1)=0;fail(off)=0",
      "fail(stop1)=0;fail(stop)=0",
      "fail(stop1)=0;fail(stop1)=0",
      "fail(stop1)=0;success(off)=1",
      "fail(stop1)=0;success(stop)=0",
      "fail(stop1)=0;success(stop1)=1",
      "success(off)=1;dispatch(off)=0",
      "success(off)=1;dispatch(stop)=0",
      "dispatch(off)=0;dispatch(off)=0",
      "dispatch(off)=0;dispatch(stop)=0",
      "dispatch(off)=0;dispatch(stop1)=0",
      "dispatch(off)=0;fail(off)=0",
      "dispatch(off)=0;fail(stop)=0",
      "dispatch(off)=0;fail(stop1)=0",
      "dispatch(off)=0;success(off)=0",
      "dispatch(off)=0;success(stop)=0",
      "dispatch(off)=0;success(stop1)=1",
      "success(off)=1;dispatch(stop1)=0",
      "success(off)=1;fail(off)=0",
      "success(off)=1;fail(stop)=0",
      "success(off)=1;fail(stop1)=0",
      "success(off)=1;success(off)=1",
      "success(off)=1;success(stop)=0",
      "success(off)=1;success(stop1)=1",
      "success(stop1)=1;dispatch(stop1)=0",
      "success(stop1)=1;fail(off)=0",
      "success(stop1)=1;fail(stop)=0",
      "success(stop1)=1;fail(stop1)=0",
      "success(stop1)=1;success(off)=1",
      "success(stop1)=1;success(stop)=0",
      "success(stop1)=1;success(stop1)=1"
    )
    val b = expected.distinct
    checkResults(resultfile, expected:_*)
  }
}


