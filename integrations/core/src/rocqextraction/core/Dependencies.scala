package rocqextraction.core

import java.nio.file.{Path, Paths}
import scala.collection.mutable

/** The dependency graph of a set of `.v` files, from the make-style output
  * of `rocq dep`: `A.vo A.glob ...: A.v B.vo /lib/Init.vo`. */
object Dependencies {

  /** Each project file with the project files it requires. Files outside
    * `files` (the standard library, other load paths) are dropped. Paths in
    * the output are resolved against `cwd`. */
  def parse(output: String, files: Set[Path], cwd: Path): Map[Path, Set[Path]] = {
    val joined = output.replace("\\\r\n", " ").replace("\\\n", " ")
    val deps = mutable.Map.empty[Path, Set[Path]]
    files.foreach(f => deps(f) = Set.empty)
    joined.linesIterator.foreach { line =>
      val colon = line.indexOf(": ")
      if (colon > 0) {
        val targets = line.substring(0, colon).trim.split("\\s+")
        val sources = line.substring(colon + 2).trim.split("\\s+").filter(_.nonEmpty)
        targets.headOption.filter(_.endsWith(".vo")).foreach { target =>
          val file = toSource(target, cwd)
          if (files.contains(file)) {
            val required = sources.toSeq
              .filter(_.endsWith(".vo"))
              .map(toSource(_, cwd))
              .filter(f => f != file && files.contains(f))
            deps(file) = deps(file) ++ required
          }
        }
      }
    }
    deps.toMap
  }

  private def toSource(vo: String, cwd: Path): Path =
    cwd.resolve(Paths.get(vo.stripSuffix("o"))).normalize

  /** The files in an order where each comes after the files it requires,
    * ties broken by path so that the order is stable. */
  def topologicalOrder(graph: Map[Path, Set[Path]]): Seq[Path] = {
    val order = mutable.ArrayBuffer.empty[Path]
    val done = mutable.Set.empty[Path]
    val visiting = mutable.LinkedHashSet.empty[Path]
    def visit(f: Path): Unit =
      if (!done(f)) {
        if (visiting(f)) {
          val cycle = visiting.toSeq.dropWhile(_ != f) :+ f
          throw new RocqException("Rocq files require each other in a cycle: " + cycle.mkString(" -> "))
        }
        visiting += f
        graph.getOrElse(f, Set.empty).toSeq.sortBy(_.toString).foreach(visit)
        visiting -= f
        done += f
        order += f
      }
    graph.keys.toSeq.sortBy(_.toString).foreach(visit)
    order.toList
  }
}
