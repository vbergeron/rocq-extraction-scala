package rocqextraction

import mill.*
import mill.api.{BuildCtx, PathRef, Result}
import rocqextraction.core as c

/** A module holding Rocq files, under `rocq/`, compiled to `.vo` files
  * out of the source tree. Other Rocq modules listed in `rocqModuleDeps`
  * see its files under `rocqLogicalName`. */
trait RocqModule extends Module {

  /** The directory of the module's `.v` files. A file `rocq/A/B.v` is the
    * Rocq module `<rocqLogicalName>.A.B`. */
  def rocqSources: T[Seq[PathRef]] = Task.Sources("rocq")

  /** The logical name of the module's files: the module's name,
    * capitalized. */
  def rocqLogicalName: T[String] = Task { RocqModule.defaultLogicalName(moduleSegments.parts) }

  /** The Rocq modules whose files this module's files require. */
  def rocqModuleDeps: Seq[RocqModule] = Seq.empty

  /** Load paths of Rocq libraries built elsewhere. Libraries installed
    * with opam need none. */
  def rocqLoadPaths: T[Seq[RocqLoadPath]] = Task { Seq.empty[RocqLoadPath] }

  /** The `rocq` executable. */
  def rocqBinary: T[String] = Task { "rocq" }

  /** A command running `rocq`, such as `Seq("opam", "exec", "--switch=s",
    * "--")`. */
  def rocqCommandPrefix: T[Seq[String]] = Task { Seq.empty[String] }

  /** Extra environment variables for `rocq`. */
  def rocqEnv: T[Map[String, String]] = Task { Map.empty[String, String] }

  /** Extra arguments of every `rocq c`. */
  def rocqFlags: T[Seq[String]] = Task { Seq.empty[String] }

  def rocqToolchain: Task[c.Toolchain] = Task.Anon {
    c.Toolchain(rocqBinary(), rocqCommandPrefix(), rocqEnv())
  }

  /** Changes on every run, to re-run `rocqVersion`. */
  private def rocqRunId: T[String] = Task.Input { java.util.UUID.randomUUID().toString }

  /** `rocq --version`, checked on every run, so that a new Rocq invalidates
    * everything compiled by the old one (the tasks using it only re-run
    * when it changes). */
  def rocqVersion: T[String] = Task {
    rocqRunId()
    RocqModule.handle(rocqToolchain().version(BuildCtx.workspaceRoot.toNIO))
  }

  /** Checks once per Rocq version that Rocq can load the Scala extraction
    * plugin, to fail early with installation instructions. */
  def rocqCheck: T[String] = Task {
    val version = rocqVersion()
    RocqModule.handle(rocqToolchain().check(Task.dest.toNIO))
    version
  }

  /** The load paths of the compiled `rocqModuleDeps` and `rocqLoadPaths`. */
  def rocqUpstreamLoadPaths: T[Seq[RocqLoadPath]] = Task {
    (Task.traverse(rocqModuleDeps)(_.rocqCompiledLoadPaths)().flatten ++ rocqLoadPaths()).distinct
  }

  /** Compiles the module's `.v` files; returns the load path of the
    * result. */
  def rocqCompile: T[Seq[RocqLoadPath]] = Task(persistent = true) {
    rocqCheck()
    val logical = rocqLogicalName()
    val roots = rocqSources().map(_.path).filter(os.isDir)
    RocqModule.handle {
      if (roots.size > 1)
        throw new c.RocqException(s"Only one Rocq source directory per module is supported: ${roots.mkString(", ")}")
      val input = c.Compiler.Input(
        roots.map(r => c.LoadPath(r.toNIO, logical)),
        rocqUpstreamLoadPaths().map(_.toCore),
        rocqFlags()
      )
      c.Compiler.compile(input, rocqToolchain(), Task.dest.toNIO, RocqModule.logger(Task.log))
        .map(lp => RocqLoadPath(PathRef(os.Path(lp.dir)), lp.logicalName, lp.recursive))
    }
  }

  /** `rocqUpstreamLoadPaths` and this module's compiled files. */
  def rocqCompiledLoadPaths: T[Seq[RocqLoadPath]] = Task {
    (rocqUpstreamLoadPaths() ++ rocqCompile()).distinct
  }
}

object RocqModule {

  /** `foo-bar` becomes `Foo_bar`; the root module is `Main`. */
  def defaultLogicalName(segments: Seq[String]): String =
    segments.lastOption.filter(_.nonEmpty).fold("Main") { s =>
      val cleaned = s.map(ch => if (ch.isLetterOrDigit || ch == '_') ch else '_')
      val ident = if (cleaned.head.isLetter || cleaned.head == '_') cleaned else "M" + cleaned
      ident.capitalize
    }

  def logger(log: mill.api.Logger): c.Logger = new c.Logger {
    def debug(msg: String): Unit = log.debug(msg)
    def info(msg: String): Unit = log.info(msg)
    def warn(msg: String): Unit = log.warn(msg)
    def error(msg: String): Unit = log.error(msg)
  }

  /** Turns the core's errors, meant for the user, into task failures
    * without a stack trace. */
  def handle[T](body: => T): Result[T] =
    try Result.Success(body)
    catch { case e: c.RocqException => Result.Failure(e.getMessage) }
}
