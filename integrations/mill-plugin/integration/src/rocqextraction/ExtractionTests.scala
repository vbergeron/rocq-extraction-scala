package rocqextraction

import mill.testkit.IntegrationTester
import utest.*

/** Runs Mill on `resources/extraction-project`. Needs `rocq` with
  * rocq-extraction-scala; skipped without it, unless
  * `ROCQ_INTEGRATION=required`. */
object ExtractionTests extends TestSuite {

  lazy val rocqAvailable: Boolean = {
    val ok =
      try os.proc("rocq", "--version").call(check = false).exitCode == 0
      catch { case _: java.io.IOException => false }
    if (!ok && sys.env.get("ROCQ_INTEGRATION").contains("required"))
      throw new java.lang.AssertionError("ROCQ_INTEGRATION=required but rocq is not on the PATH")
    ok
  }

  def withTester(body: IntegrationTester => Unit): Unit =
    if (!rocqAvailable) println("skipped: rocq not found")
    else {
      val tester = IntegrationTester(
        daemonMode = true,
        workspaceSourcePath = os.Path(sys.env("MILL_TEST_RESOURCE_DIR")) / "extraction-project",
        millExecutable = os.Path(sys.env("MILL_EXECUTABLE_PATH"))
      )
      try {
        tester.modifyFile(tester.workspacePath / "build.mill", _.replace("PLUGIN_VERSION", sys.env("PLUGIN_VERSION")))
        body(tester)
      } finally tester.close()
    }

  val tests = Tests {
    test("extraction") - withTester { tester =>
      val res1 = tester.eval("app.run")
      assert(res1.isSuccess)
      assert(res1.out.contains("addNat: 9"))
      assert(res1.err.contains("Rocq: compiling Lib/Base.v"), res1.err.contains("Rocq: compiling App/Arith.v"))

      val extracted = tester.workspacePath / "out/app/rocqExtract.dest/src/com/example/verified/Arith.scala"
      assert(os.read(extracted).contains("def addNat(n: BigInt)(m: BigInt): BigInt"))
      // No build output next to the sources
      assert(os.walk(tester.workspacePath / "lib/rocq").map(_.last) == Seq("Base.v"))

      // Nothing changed: nothing recompiled
      val res2 = tester.eval("app.run")
      assert(res2.isSuccess, !res2.err.contains("Rocq: compiling"))

      // A change in `lib` recompiles `app`'s files too
      tester.modifyFile(tester.workspacePath / "lib/rocq/Base.v", _.replace(":= 2", ":= 5"))
      val res3 = tester.eval("app.run")
      assert(res3.isSuccess, res3.out.contains("addNat: 12"))
      assert(res3.err.contains("Rocq: compiling Lib/Base.v"), res3.err.contains("Rocq: compiling App/Arith.v"))
    }

    test("rocqError") - withTester { tester =>
      tester.modifyFile(tester.workspacePath / "app/rocq/Arith.v", _ + "\nDefinition oops : nat := true.\n")
      val res = tester.eval("app.compile")
      assert(!res.isSuccess)
      // Reported on the source file
      assert(res.err.contains((tester.workspacePath / "app/rocq/Arith.v").toString))
      assert(res.err.contains("has type \"bool\""))
    }

    test("invalidSettings") - withTester { tester =>
      tester.modifyFile(tester.workspacePath / "build.mill", _.replace("\"com.example.verified\"", "\"1bad\""))
      val res = tester.eval("app.compile")
      assert(!res.isSuccess)
      assert(res.err.contains("Scala package `1bad` is not a dotted sequence of identifiers"))
    }
  }
}
