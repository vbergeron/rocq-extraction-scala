package rocqextraction.core

import java.nio.file.{Files, Path}

/** Compiles the `.v` files of some source roots into `.vo` files, out of
  * the source tree.
  *
  * Each root is mirrored into `outDir/roots/<logical name>` (the `.v` files
  * are copied there, then compiled in place), so the source tree gets no
  * `.vo` files and Rocq sees one directory per logical name. File names in
  * Rocq's messages are mapped back to the sources. A file is recompiled
  * only when its content, the content of a file it requires, a compiled
  * library of `loadPaths` (by size and date), the flags or the Rocq version
  * changed. */
object Compiler {

  final case class Input(
      roots: Seq[LoadPath],
      loadPaths: Seq[LoadPath],
      flags: Seq[String]
  )

  /** Returns the load paths under which the compiled roots are found. */
  def compile(input: Input, toolchain: Toolchain, outDir: Path, logger: Logger): Seq[LoadPath] = {
    val duplicates = input.roots.groupBy(_.logicalName).collect { case (n, rs) if rs.size > 1 => n }
    if (duplicates.nonEmpty)
      throw new RocqException(s"Several Rocq source roots have the logical name ${duplicates.mkString(", ")}")

    val mirrorBase = outDir.resolve("roots")
    val mirrors = input.roots.map { r =>
      val mirror = mirrorBase.resolve(r.logicalName)
      (r, mirror, mirrorRoot(r.dir, mirror))
    }
    // Roots of earlier runs that are gone
    IO.files(mirrorBase).map(p => mirrorBase.relativize(p).getName(0).toString).distinct
      .filterNot(n => input.roots.exists(_.logicalName == n))
      .foreach(n => IO.deleteRecursively(mirrorBase.resolve(n)))

    val compiled = mirrors.map { case (r, m, _) => r.copy(dir = m) }
    val files = mirrors.flatMap(_._3)
    if (files.nonEmpty) {
      val loadArgs = compiled.flatMap(_.args) ++ input.loadPaths.flatMap(_.args)
      def fromMirror(text: String): String =
        mirrors.foldLeft(text) { case (t, (r, m, _)) => t.replace(m.toString, r.dir.toString) }

      val dep = toolchain.run(Seq("dep") ++ loadArgs ++ files.map(_.toString), outDir)
      if (!dep.ok) fail("rocq dep failed", fromMirror(dep.output), outDir)
      val graph = Dependencies.parse(dep.output, files.toSet, outDir)
      val order = Dependencies.topologicalOrder(graph)

      val version = toolchain.version(outDir)
      // Compiled libraries the files may require: any change recompiles all
      val upstream = IO.sha256(input.loadPaths.flatMap { lp =>
        IO.files(lp.dir).filter(_.getFileName.toString.endsWith(".vo")).map { f =>
          s"$f ${Files.size(f)} ${Files.getLastModifiedTime(f).toMillis}"
        }
      })
      val statePath = outDir.resolve("state")
      val state = readState(statePath)
      val hashes = scala.collection.mutable.Map.empty[Path, String]
      var newState = Map.empty[String, String]
      order.foreach { f =>
        val key = mirrorBase.relativize(f).toString
        val hash = IO.sha256(
          Seq(version, upstream, loadArgs.mkString(" "), input.flags.mkString(" "), IO.read(f)) ++
            graph(f).toSeq.sortBy(_.toString).map(hashes)
        )
        hashes(f) = hash
        val vo = f.resolveSibling(f.getFileName.toString + "o")
        if (state.get(key).contains(hash) && Files.isRegularFile(vo))
          logger.debug(s"Rocq: $key is up to date")
        else {
          logger.info(s"Rocq: compiling $key")
          // Out of date from now on, in case compiling it fails
          writeState(statePath, newState ++ (state - key))
          val r = toolchain.run(Seq("c", "-q") ++ loadArgs ++ input.flags :+ f.toString, outDir)
          val output = fromMirror(r.output)
          if (!r.ok) fail(s"Rocq: compiling $key failed", output, outDir)
          if (output.trim.nonEmpty) logger.warn(output.trim)
        }
        newState += key -> hash
      }
      writeState(statePath, newState)
    }
    compiled
  }

  /** Copies the `.v` files of `src` into `mirror`, deletes the files of
    * `.v` files that no longer exist, and returns the copies. */
  private def mirrorRoot(src: Path, mirror: Path): Seq[Path] = {
    val sources = IO.files(src).filter(_.getFileName.toString.endsWith(".v"))
    val rels = sources.map(s => src.relativize(s).toString)
    val stems = rels.map(_.stripSuffix(".v")).toSet
    // Rocq writes A.vo, A.vos, A.vok, A.glob and .A.aux next to A.v
    IO.files(mirror).foreach { p =>
      val rel = mirror.relativize(p)
      val name = rel.getFileName.toString.stripPrefix(".")
      val stem = Option(rel.getParent).fold("")(_.toString + java.io.File.separator) + name.takeWhile(_ != '.')
      if (!stems(stem)) Files.delete(p)
    }
    IO.deleteEmptyDirs(mirror)
    sources.zip(rels).map { case (s, rel) =>
      val copy = mirror.resolve(rel)
      IO.writeIfChanged(copy, Files.readAllBytes(s))
      copy
    }
  }

  private def fail(what: String, output: String, cwd: Path): Nothing =
    throw new RocqException(s"$what:\n${output.trim}", Problems.parse(output, cwd))

  private def readState(path: Path): Map[String, String] =
    if (!Files.isRegularFile(path)) Map.empty
    else
      IO.read(path).linesIterator.flatMap { l =>
        l.split('\t') match {
          case Array(k, v) => Some(k -> v)
          case _           => None
        }
      }.toMap

  private def writeState(path: Path, state: Map[String, String]): Unit =
    IO.write(path, state.toSeq.sorted.map { case (k, v) => s"$k\t$v\n" }.mkString)
}
