package rocqextraction.core

import java.nio.file.{Files, Path}
import utest._

/** Runs the real `rocq`, see [[TestUtil.withRocq]]. */
object RocqTests extends TestSuite {
  import TestUtil._

  final class Recorder extends Logger {
    val infos = scala.collection.mutable.ArrayBuffer.empty[String]
    def debug(msg: String): Unit = ()
    def info(msg: String): Unit = infos += msg
    def warn(msg: String): Unit = ()
    def error(msg: String): Unit = ()
  }

  def project(): (Path, Path) = {
    val dir = tempDir()
    val src = dir.resolve("rocq")
    write(src.resolve("Sub/Base.v"), "Definition two : nat := 2.\n")
    write(src.resolve("Arith.v"), "Require My.Sub.Base.\nDefinition addNat (n m : nat) : nat := n + m + My.Sub.Base.two.\n")
    (dir, src)
  }

  val tests = Tests {
    test("compileAndExtract") - withRocq {
      val (dir, src) = project()
      val out = dir.resolve("out")
      val input = Compiler.Input(Seq(LoadPath(src, "My")), Nil, Nil)
      val log = new Recorder
      val loadPaths = Compiler.compile(input, Toolchain(), out.resolve("compile"), log)
      assert(log.infos.toSeq == Seq("Rocq: compiling My/Sub/Base.v", "Rocq: compiling My/Arith.v"))
      // Nothing written next to the sources
      assert(IO.files(src).forall(_.toString.endsWith(".v")))

      val files = Extractor.extract(
        Extractor.Input(
          loadPaths,
          Nil,
          Seq(Extraction("Arith", "My.Arith", Seq("addNat"))),
          ExtractionOptions(Seq(Prelude.NatBigInt), Some("com.ex"))
        ),
        Toolchain(),
        out.resolve("work"),
        out.resolve("src"),
        log
      )
      assert(files == Seq(out.resolve("src/com/ex/Arith.scala")))
      val code = read(files.head)
      assert(code.startsWith("package com.ex"), code.contains("def addNat(n: BigInt)(m: BigInt): BigInt"))

      // Nothing changed: nothing recompiled
      log.infos.clear()
      Compiler.compile(input, Toolchain(), out.resolve("compile"), log)
      assert(log.infos.isEmpty)

      // A changed dependency recompiles its dependents
      write(src.resolve("Sub/Base.v"), "Definition two : nat := 1 + 1.\n")
      Compiler.compile(input, Toolchain(), out.resolve("compile"), log)
      assert(log.infos.toSeq == Seq("Rocq: compiling My/Sub/Base.v", "Rocq: compiling My/Arith.v"))

      // A removed file's outputs are removed
      Files.delete(src.resolve("Arith.v"))
      Compiler.compile(input, Toolchain(), out.resolve("compile"), log)
      assert(IO.files(out.resolve("compile/roots/My")).forall(_.toString.contains("Sub")))
    }

    test("upstreamChange") - withRocq {
      // `Lib` compiled on its own, then used through a load path
      val dir = tempDir()
      val lib = dir.resolve("lib")
      write(lib.resolve("Base.v"), "Definition two : nat := 2.\n")
      val app = dir.resolve("app")
      write(app.resolve("Arith.v"), "Require Lib.Base.\nDefinition three : nat := S Lib.Base.two.\n")
      def build(log: Logger): Unit = {
        val libPaths = Compiler.compile(Compiler.Input(Seq(LoadPath(lib, "Lib")), Nil, Nil), Toolchain(), dir.resolve("out/lib"), log)
        Compiler.compile(Compiler.Input(Seq(LoadPath(app, "App")), libPaths, Nil), Toolchain(), dir.resolve("out/app"), log)
      }
      build(Logger.silent)
      write(lib.resolve("Base.v"), "Definition two : nat := 1 + 1.\n")
      val log = new Recorder
      build(log)
      assert(log.infos.toSeq == Seq("Rocq: compiling Lib/Base.v", "Rocq: compiling App/Arith.v"))
    }

    test("errors") - withRocq {
      val (dir, src) = project()
      val bad = write(src.resolve("Bad.v"), "Definition x : nat := true.\n")
      val e = assertThrows[RocqException] {
        Compiler.compile(Compiler.Input(Seq(LoadPath(src, "My")), Nil, Nil), Toolchain(), dir.resolve("out"), Logger.silent)
      }
      // Reported on the source file, not its copy
      assert(e.problems.map(p => (p.file, p.line, p.severity)) == Seq((bad, 1, Problem.Error)))
      assert(e.getMessage.contains(bad.toString))
    }

    test("missingToolchain") {
      val e = assertThrows[RocqException](Toolchain(binary = "rocq-does-not-exist").check(tempDir()))
      assert(e.getMessage.contains("opam install rocq-extraction-scala"))
    }
  }
}
