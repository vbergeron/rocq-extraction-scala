package rocqextraction.core

import java.nio.file.{Path, Paths}
import scala.util.matching.Regex

/** Parses Rocq's messages, `File "f.v", line 3, characters 4-9:` followed
  * by `Error: ...` or `Warning: ...` and the rest of the message, until
  * the next position line. */
object Problems {
  private val Position: Regex = """^File "(.*)", line (\d+), characters (\d+)-(\d+):\s*$""".r
  private val Kind: Regex = """^(Error|Warning):?\s*(.*)$""".r

  /** `relativeTo` resolves relative file names, as printed by a Rocq run
    * in that directory. */
  def parse(output: String, relativeTo: Path): Seq[Problem] = {
    val lines = output.linesIterator.toVector
    val starts = lines.indices.filter(i => Position.findFirstIn(lines(i)).isDefined)
    starts.zipWithIndex.flatMap { case (start, k) =>
      val end = if (k + 1 < starts.size) starts(k + 1) else lines.size
      val position = Position.findFirstMatchIn(lines(start)).get
      val body = lines.slice(start + 1, end)
      body.headOption.flatMap(h => Kind.findFirstMatchIn(h)).map { m =>
        val severity = if (m.group(1) == "Error") Problem.Error else Problem.Warning
        val message = (m.group(2) +: body.tail).map(_.trim).filter(_.nonEmpty).mkString("\n")
        Problem(
          relativeTo.resolve(Paths.get(position.group(1))).normalize,
          position.group(2).toInt,
          position.group(3).toInt,
          position.group(4).toInt,
          severity,
          message
        )
      }
    }
  }
}
