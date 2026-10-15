package rocqextraction.core

import java.nio.file.{Files, Path}

/** Moves freshly extracted files into the generated source directory. */
object Sync {
  private val PackageLine = """^package\s+([\w.]+)""".r

  /** Copies each file of `generated` to `outputDir`, under the directories
    * of its `package` clause, writing only the files whose content changed;
    * deletes the `.scala` files of `outputDir` that are not produced any
    * more. Returns the files of `outputDir`. */
  def apply(generated: Seq[Path], outputDir: Path): Seq[Path] = {
    val targets = generated.map { g =>
      val content = Files.readAllBytes(g)
      val pkg = IO.read(g).linesIterator.collectFirst {
        case l if PackageLine.findFirstIn(l).isDefined => PackageLine.findFirstMatchIn(l).get.group(1)
      }
      val dir = pkg.fold(outputDir)(p => p.split('.').foldLeft(outputDir)((d, s) => d.resolve(s)))
      val target = dir.resolve(g.getFileName.toString)
      IO.writeIfChanged(target, content)
      target
    }
    val keep = targets.toSet
    IO.files(outputDir).filter(_.getFileName.toString.endsWith(".scala")).filterNot(keep).foreach(Files.delete)
    IO.deleteEmptyDirs(outputDir)
    targets.sortBy(_.toString)
  }
}
