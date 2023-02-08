package tests.test41_formalise_statemachine

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test41 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test41_formalise_statemachine"
  val resultfile = s"$TEST/dejavu-results"
  val spec = s"$TEST/spec.qtl"
  val log1 = s"$TEST/log1.csv"
  val log2 = s"$TEST/log2.csv"
  val biglog100k = s"$TEST/biglog100k.csv"
  val biglog1000k = s"$TEST/biglog1000k.csv"
  val biglog5000k = s"$TEST/biglog5000k.csv"
  val biglog10000k = s"$TEST/biglog10000k.csv"

  @Test def test1(): Unit = {
    Verify("--execution", "0", "--specfile", spec, "--logfile", log1, "--resultfile", resultfile, "--bits", "3")
    checkResults(resultfile,1,4,9,14,21,24,26)
  }

  @Test def test2(): Unit = {
    Verify("--execution", "0", "--specfile", spec, "--logfile", log2, "--resultfile", resultfile, "--bits", "3")
    checkResults(resultfile)
  }

  // --- long traces: ---

  // repeat: repeat the following
  // repeat_toggle: open this many channels
  // repeat_telem: send this many messages on each channel

  // val (repeat, repeat_toggle, repeat_telem) = (10,100,100) // 100k
  // Processed 102001 events
  // Elapsed trace analysis time: 1.57s
  @Test def test3(): Unit = {
    Verify("--execution", "0", "--specfile", spec, "--logfile", biglog100k, "--resultfile", resultfile)
    checkResults(resultfile,102001)
  }

  // STTT trace T1:
  // ==============
  // val (repeat, repeat_toggle, repeat_telem) = (100,1000,10) // 1_000k
  // Processed 1200001 events
  // Elapsed trace analysis time: 4.423s
  @Test def test4(): Unit = {
    Verify("--execution", "0", "--specfile", spec, "--logfile", biglog1000k, "--resultfile", resultfile)
    checkResults(resultfile,1200001)
  }

  // STTT trace T2:
  // ==============
  // val (repeat, repeat_toggle, repeat_telem) = (1000,100,50) // 5_000k
  // Processed 5200001 events
  // Elapsed trace analysis time: 9.885s
  @Test def test5(): Unit = {
    Verify("--execution", "0", "--specfile", spec, "--logfile", biglog5000k, "--resultfile", resultfile)
    checkResults(resultfile,5200001)
  }

  // STTT trace T3:
  // ==============
  // val (repeat, repeat_toggle, repeat_telem) = (1000,100,100) // 10_000k
  // Processed 10200001 events
  // Elapsed trace analysis time: 17.897s
  @Test def test6(): Unit = {
    Verify("--execution", "0", "--specfile", spec, "--logfile", biglog10000k, "--resultfile", resultfile)
    checkResults(resultfile,10200001)
  }
}

