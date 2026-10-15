package rocqextraction.core

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}

final case class ProcessResult(exitCode: Int, output: String) {
  def ok: Boolean = exitCode == 0
}

/** How to run Rocq: the `rocq` binary, preceded by an optional wrapper
  * (`opam exec --switch=s --`, `nix develop -c`, ...), with extra
  * environment variables. */
final case class Toolchain(
    binary: String = "rocq",
    commandPrefix: Seq[String] = Nil,
    env: Map[String, String] = Map.empty
) {

  /** Runs `rocq args...` in `cwd`, stdout and stderr merged. */
  def run(args: Seq[String], cwd: Path): ProcessResult = {
    val command = commandPrefix ++ (binary +: args)
    val javaCommand = new java.util.ArrayList[String]
    command.foreach(javaCommand.add)
    val builder = new ProcessBuilder(javaCommand)
      .directory(cwd.toFile)
      .redirectErrorStream(true)
    env.foreach { case (k, v) => builder.environment().put(k, v) }
    val process =
      try builder.start()
      catch {
        case e: java.io.IOException =>
          throw new RocqException(
            s"Cannot run `${command.mkString(" ")}`: ${e.getMessage}\n" + Toolchain.installHint
          )
      }
    process.getOutputStream.close()
    val out = new ByteArrayOutputStream
    val in = process.getInputStream
    val buf = new Array[Byte](8192)
    var n = in.read(buf)
    while (n >= 0) {
      out.write(buf, 0, n)
      n = in.read(buf)
    }
    ProcessResult(process.waitFor(), new String(out.toByteArray, StandardCharsets.UTF_8))
  }

  /** The output of `rocq --version`, which identifies the toolchain in
    * cache keys. */
  def version(cwd: Path): String = {
    val r = run(Seq("--version"), cwd)
    if (!r.ok) throw new RocqException(s"`$binary --version` failed:\n${r.output}\n" + Toolchain.installHint)
    r.output.trim
  }

  /** Checks that Rocq can load the Scala extraction plugin, by compiling a
    * file that requires it in `workDir`. */
  def check(workDir: Path): Unit = {
    Files.createDirectories(workDir)
    val file = workDir.resolve("RocqExtractionCheck.v")
    Files.write(file, "From ScalaExtraction Require Import ScalaExtraction.\n".getBytes(StandardCharsets.UTF_8))
    val r = run(Seq("c", "-q", file.getFileName.toString), workDir)
    if (!r.ok)
      throw new RocqException(
        s"`$binary` cannot load the Scala extraction plugin (rocq-extraction-scala):\n${r.output.trim}\n" +
          Toolchain.installHint
      )
  }
}

object Toolchain {
  val installHint: String =
    "The Scala extraction needs Rocq with rocq-extraction-scala installed: " +
      "`opam install rocq-extraction-scala`, see https://github.com/vbergeron/rocq-extraction-scala#installation"
}
