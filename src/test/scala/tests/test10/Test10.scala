package tests.test10

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

/**
  * Testing garbage collection in version 1.1.
  */

class Test10 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test10"
  val resultfile = s"$TEST/dejavu-results"
  val spec = s"$TEST/spec.qtl"
  val log1 = s"$TEST/log1.csv"
  val log2 = s"$TEST/log2.csv"
  val log3 = s"$TEST/log3.csv"
  val log4 = s"$TEST/log4.csv"
  val log5 = s"$TEST/log5.csv"
  val log6 = s"$TEST/log6.csv"

  @Test def test1(): Unit = {
    Verify("--specfile", spec, "--logfile", log1, "--resultfile", resultfile, "--bits", "3")
    checkResults(resultfile)
  }

  @Test def test2(): Unit = {
    Verify("--specfile", spec, "--logfile", log2, "--resultfile", resultfile, "--bits", "3")
    checkResults(resultfile)
  }

  @Test def test3(): Unit = {
    Verify("--specfile", spec, "--logfile", log3, "--resultfile", resultfile, "--bits", "3")
    checkResults(resultfile,5)
  }

  @Test def test4(): Unit = {
    Verify.long("--specfile", spec, "--logfile", log4, "--resultfile", resultfile)
    checkResults(resultfile, 1003001)
  }

  @Test def test5(): Unit = {
    Verify("--specfile", spec, "--logfile", log5, "--resultfile", resultfile, "--bits", "3", "--mode", "debug")
    checkResults(resultfile,
      10 -- 2,
      10 -- 3,
      10 -- 1,
      13
    )
  }

  @Test def test6(): Unit = {
    Verify("--specfile", spec, "--logfile", log6, "--resultfile", resultfile, "--bits", "2")
    checkResults(resultfile,
      6 -- 1,
      6 -- 2,
      6 -- 3,
      9,
      11 -- 4,
      11 -- 5,
      11 -- 2,
      16 -- 6,
      17
    )
  }
}
