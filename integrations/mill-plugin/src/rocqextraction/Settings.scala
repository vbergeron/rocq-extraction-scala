package rocqextraction

import mill.api.PathRef
import upickle.default.ReadWriter
import rocqextraction.core as c

/** Compiled Rocq files bound to a logical name: `-Q dir name`, or `-R dir
  * name` when `recursive`. The directory is a `PathRef`, so that a change
  * of its content invalidates the tasks using it. */
final case class RocqLoadPath(dir: PathRef, logicalName: String, recursive: Boolean = false)
    derives ReadWriter {
  def toCore: c.LoadPath = c.LoadPath(dir.path.toNIO, logicalName, recursive)
}

/** The `ExtrScala*` files imported before extracting. */
enum RocqPrelude derives ReadWriter {
  /** `bool`, `option`, `list`, ... to Scala's own types. */
  case Basic
  /** `nat` to `BigInt` (includes `Basic`). */
  case NatBigInt
  /** `positive`, `N` and `Z` to `BigInt` (includes `Basic`). */
  case ZBigInt

  def toCore: c.Prelude = this match {
    case Basic     => c.Prelude.Basic
    case NatBigInt => c.Prelude.NatBigInt
    case ZBigInt   => c.Prelude.ZBigInt
  }
}

/** `Extraction "name" module.def1 module.def2 ...`: extracts `definitions`
  * of the Rocq module `module` (a logical path, such as `MyProject.Arith`)
  * to `name.scala`, holding `object name`. `scalaPackage` and
  * `pureFunctions` override the module's `rocqScalaPackage` and
  * `rocqPureFunctions`. */
final case class RocqExtraction(
    name: String,
    module: String,
    definitions: Seq[String],
    scalaPackage: Option[String] = None,
    pureFunctions: Option[Boolean] = None
) derives ReadWriter {
  def toCore: c.Extraction = c.Extraction(name, module, definitions, scalaPackage, pureFunctions)
}
