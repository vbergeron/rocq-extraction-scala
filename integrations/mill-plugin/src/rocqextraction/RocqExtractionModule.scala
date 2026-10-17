package rocqextraction

import mill.*
import mill.api.PathRef
import mill.scalalib.ScalaModule
import rocqextraction.core as c

/** A Scala module whose generated sources include Scala code extracted
  * from Rocq: from its own `.v` files (see [[RocqModule]]) and those of
  * the Rocq modules among its `moduleDeps`.
  *
  * {{{
  * object app extends ScalaModule, RocqExtractionModule {
  *   def scalaVersion     = "3.7.3"
  *   def rocqScalaPackage = Some("com.example.verified")
  *   def rocqExtractions  = Seq(RocqExtraction("Arith", "App.Arith", Seq("addNat")))
  * }
  * }}}
  */
trait RocqExtractionModule extends ScalaModule, RocqModule {

  override def rocqModuleDeps: Seq[RocqModule] = moduleDeps.collect { case m: RocqModule => m }

  /** The `ExtrScala*` files to import, `Basic` by default. */
  def rocqPreludes: T[Seq[RocqPrelude]] = Task { Seq(RocqPrelude.Basic) }

  /** The package of the extracted files (`Set Extraction Scala Package`). */
  def rocqScalaPackage: T[Option[String]] = Task { Option.empty[String] }

  /** Prints function types with pure arrows (`Set Extraction Scala Pure
    * Functions`). */
  def rocqPureFunctions: T[Boolean] = Task { false }

  /** What to extract. Each extraction produces one file. */
  def rocqExtractions: T[Seq[RocqExtraction]] = Task { Seq.empty[RocqExtraction] }

  /** Runs the extractions; returns the directory of the extracted
    * files. */
  def rocqExtract: T[Seq[PathRef]] = Task(persistent = true) {
    val input = c.Extractor.Input(
      rocqCompiledLoadPaths().map(_.toCore),
      rocqFlags(),
      rocqExtractions().map(_.toCore),
      c.ExtractionOptions(rocqPreludes().map(_.toCore), rocqScalaPackage(), rocqPureFunctions())
    )
    val out = Task.dest / "src"
    RocqModule.handle {
      c.Extractor.extract(input, rocqToolchain(), (Task.dest / "work").toNIO, out.toNIO, RocqModule.logger(Task.log))
      Seq(PathRef(out))
    }
  }

  override def generatedSources: T[Seq[PathRef]] = Task {
    super.generatedSources() ++ rocqExtract()
  }
}
