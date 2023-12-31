package tests.test11

import dejavu.Verify
import org.junit.Test
import tests.util.testcase.TestCase

class Test11 extends TestCase {
  val TEST = PATH_TO_TESTS + "/test11"
  val resultfile = s"$TEST/dejavu-results"
  val spec1 = s"$TEST/spec1.qtl"
  val spec2 = s"$TEST/spec2.qtl"
  val spec3 = s"$TEST/spec3.qtl"
  val spec4 = s"$TEST/spec4.qtl"
  val log1 = s"$TEST/log1.csv"
  val log2 = s"$TEST/log2.csv"

  // --- spec 1: ---

  @Test def test1(): Unit = { // b(1,10).a(1).c(10).b(1,20).a(1).c(20)
    Verify("--execution", "0", "--specfile", spec1, "--logfile", log1, "--resultfile", resultfile, "--bits", "3")   // |=
    checkResults(resultfile)         // forall x . (a(x) -> exists y . !c(y) S b(x,y))
  }

  @Test def test2(): Unit = { // b(1,10).a(1).c(10).b(1,10).a(1).c(10)
    Verify("--execution", "0", "--specfile", spec1, "--logfile", log2, "--resultfile", resultfile, "--bits", "3")   // |=
    checkResults(resultfile)         // forall x . (a(x) -> exists y . !c(y) S b(x,y))
  }

  // --- spec 2: ---

  @Test def test3(): Unit = { // b(1,10).a(1).c(10).b(1,20).a(1).c(20)
    Verify("--execution", "0", "--specfile", spec2, "--logfile", log1, "--resultfile", resultfile, "--bits", "3")   // |=
    checkResults(resultfile)         // forall x . exists y . (a(x) -> !c(y) S b(x,y))
  }

  @Test def test4(): Unit = { // b(1,10).a(1).c(10).b(1,10).a(1).c(10)
    Verify("--execution", "0", "--specfile", spec2, "--logfile", log2, "--resultfile", resultfile, "--bits", "3")   // |=
    checkResults(resultfile)         // forall x . exists y . (a(x) -> !c(y) S b(x,y))
  }

  // --- spec 3: ---

  @Test def test5(): Unit = { // b(1,10).a(1).c(10).b(1,20).a(1).c(20)
    Verify("--execution", "0", "--specfile", spec3, "--logfile", log1, "--resultfile", resultfile, "--bits", "3")   // |=
    checkResults(resultfile)         // forall x . H ((a(x) -> exists y . [ b(x,y) , c(y) )))
  }

  @Test def test6(): Unit = { // b(1,10).a(1).c(10).b(1,10).a(1).c(10)
    Verify("--execution", "0", "--specfile", spec3, "--logfile", log2, "--resultfile", resultfile, "--bits", "3")   // |=
    checkResults(resultfile)         // forall x . H ((a(x) -> exists y . [ b(x,y) , c(y) )))
  }

  // --- spec 4: ---

  @Test def test7(): Unit = { // b(1,10).a(1).c(10).b(1,20).a(1).c(20)
    Verify("--execution", "0", "--specfile", spec4, "--logfile", log1, "--resultfile", resultfile, "--bits", "3")   // not |=
    checkResults(resultfile, 5,6)         // forall x . exists y . H ((a(x) -> [ b(x,y) , c(y) )))
  }

  @Test def test8(): Unit = { // b(1,10).a(1).c(10).b(1,10).a(1).c(10)
    Verify("--execution", "0", "--specfile", spec4, "--logfile", log2, "--resultfile", resultfile, "--bits", "3")   // |=
    checkResults(resultfile)         // forall x . exists y . H ((a(x) -> [ b(x,y) , c(y) )))
  }

}

