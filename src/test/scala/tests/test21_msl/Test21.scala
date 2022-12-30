package tests.test21_msl

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test21 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test21_msl"
  val resultfile = s"$TEST/dejavu-results"
  val spec1 = s"$TEST/spec1.qtl"
  val spec2 = s"$TEST/spec2.qtl"
  val spec1Timed = s"$TEST/spec1_timed.qtl"
  val logMSL = s"$TEST/log_msl.csv"
  val logMSLTimed = s"$TEST/log_msl_timed.csv"
  val log1 = s"$TEST/log1.csv"
  val log2 = s"$TEST/log2.csv"
  val log3 = s"$TEST/log3.csv"
  val log4 = s"$TEST/log4.csv"
  val log5 = s"$TEST/log5.csv"

  @Test def test1_msl(): Unit = {
    Verify("--specfile", spec1, "--logfile", logMSL, "--resultfile", resultfile, "--bits", "20")
    checkResults(resultfile,
      1672,1673,
      1678,1679,
      15807,
      32616,32617,
      32715,32716,
      48735,48736,48737,48738,48739,48740,48741,48742,48743,48744,48745,48746,48747,48748)
  }

  @Test def test1_msl_5bits(): Unit = {
    Verify("--specfile", spec1, "--logfile", logMSL, "--resultfile", resultfile, "--bits", "5")
    checkResultsBrief(resultfile,
      1672,1673,
      1678,1679,
      15807,
      32616,32617,
      32715,32716,
      48735,48736,48737,48738,48739,48740,48741,48742,48743,48744,48745,48746,48747,48748, 8118 gc)
  }

  // TODO:

  @Test def test2_msl(): Unit = {
    Verify("--specfile", spec2, "--logfile", logMSL, "--resultfile", resultfile, "--bits", "20")
    checkResults(resultfile)
  }

  // =============================
  // Experimentation in the large:
  // =============================

  /**
    * The following test yields false positives due to the use of quantification.
    */

  @Test def test1Timed_msl(): Unit = {
    Verify.long("--specfile", spec1Timed, "--logfile", logMSLTimed, "--resultfile", resultfile, "--bits", "20")
    checkResults(resultfile,
      1672,1673,
      1678,1679,
      15807,
      26763, // extra
      28620, // extra
      28633, // extra
      28711, // extra
      28724, // extra
      32616,32617,
      32715,32716,
      48735,48736,48737,48738,48739,48740,48741,48742,48743,48744,48745,48746,48747,48748)
  }

  // =============================
  // Experimentation in the small:
  // =============================

  @Test def test1_1(): Unit = {
    Verify("--specfile", spec1, "--logfile", log1, "--resultfile", resultfile, "--bits", "3")
    checkResults(resultfile, 7)
  }

  @Test def test1_2(): Unit = {
    Verify("--specfile", spec1, "--logfile", log2, "--resultfile", resultfile, "--bits", "20")
    checkResults(resultfile,
      2,3,
      6,7,
      10,
      28,29,
      32,33,
      36,37,38,39,40,41,42,43,44,45,46,47,48,49)
  }

  @Test def test1Timed_3(): Unit = {
    Verify("--specfile", spec1Timed, "--logfile", log3, "--resultfile", resultfile, "--bits", "20")
    checkResults(resultfile,
      2,3,
      6,7,
      10,
      13, // extra
      16, // extra
      19, // extra
      22, // extra
      25, // extra
      28,29,
      32,33,
      36,37,38,39,40,41,42,43,44,45,46,47,48,49)
  }

  @Test def test1_4(): Unit = {
    Verify("--specfile", spec1, "--logfile", log4, "--resultfile", resultfile, "--bits", "3")
    checkResults(resultfile,2)
  }

  @Test def test1Timed_5(): Unit = {
    Verify("--specfile", spec1Timed, "--logfile", log5, "--resultfile", resultfile, "--bits", "3")
    checkResults(resultfile,2,5)
  }

}

//Interval(telecom0208,515132224,515132256,Map())
//Interval(telecom0208,512912640,512912672,Map())
//Interval(telecom0208,514587328,514587360,Map())
//Interval(telecom0208,514586496,514586528,Map())
//
// cmdExec :- CMD_DISPATCH before CMD_COMPLETE
//   where CMD_DISPATCH.cmd = CMD_COMPLETE.cmd
//   map {cmd -> CMD_DISPATCH.cmd}
//
// telecom0208 :- TLM_TR_ERROR during cmdExec
//   where cmdExec.cmd = "MOB_NAV_PRM_SET" | cmdExec.cmd = "ARM_PRM_SETDMP"
