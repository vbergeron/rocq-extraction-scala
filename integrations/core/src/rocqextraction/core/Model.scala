package rocqextraction.core

import java.nio.file.Path

/** A directory of Rocq files bound to a logical name: `-Q dir name`, or
  * `-R dir name` when `recursive`. */
final case class LoadPath(dir: Path, logicalName: String, recursive: Boolean = false) {
  def args: Seq[String] = Seq(if (recursive) "-R" else "-Q", dir.toString, logicalName)
}

/** The `ExtrScala*` files a driver imports before extracting. */
sealed abstract class Prelude(val module: String)

object Prelude {
  case object Basic extends Prelude("ExtrScalaBasic")
  case object NatBigInt extends Prelude("ExtrScalaNatBigInt")
  case object ZBigInt extends Prelude("ExtrScalaZBigInt")

  val all: Seq[Prelude] = Seq(Basic, NatBigInt, ZBigInt)
}

/** One `Extraction "name" module.def1 module.def2 ...` command: writes
  * `name.scala`, holding `object name`. The options left at `None` take
  * the values of [[ExtractionOptions]]. */
final case class Extraction(
    name: String,
    module: String,
    definitions: Seq[String],
    scalaPackage: Option[String] = None,
    pureFunctions: Option[Boolean] = None
)

/** Options shared by all the extractions of a driver. */
final case class ExtractionOptions(
    preludes: Seq[Prelude] = Seq(Prelude.Basic),
    scalaPackage: Option[String] = None,
    pureFunctions: Boolean = false
)

/** A Rocq message with a position, parsed from Rocq's output. */
final case class Problem(
    file: Path,
    line: Int,
    startColumn: Int,
    endColumn: Int,
    severity: Problem.Severity,
    message: String
) {
  override def toString: String = {
    val kind = severity match {
      case Problem.Error   => "error"
      case Problem.Warning => "warning"
    }
    s"$file:$line:${startColumn + 1}: $kind: $message"
  }
}

object Problem {
  sealed trait Severity
  case object Error extends Severity
  case object Warning extends Severity
}

/** Thrown for any failure the user has to fix: a Rocq error, a missing
  * toolchain, an invalid setting. The message is meant to be shown as is. */
final class RocqException(message: String, val problems: Seq[Problem] = Nil)
    extends RuntimeException(message)

/** Where the core reports progress. Each build tool adapts its own logger. */
trait Logger {
  def debug(msg: String): Unit
  def info(msg: String): Unit
  def warn(msg: String): Unit
  def error(msg: String): Unit
}

object Logger {
  object silent extends Logger {
    def debug(msg: String): Unit = ()
    def info(msg: String): Unit = ()
    def warn(msg: String): Unit = ()
    def error(msg: String): Unit = ()
  }
}
