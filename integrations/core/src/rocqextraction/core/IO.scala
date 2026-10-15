package rocqextraction.core

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import java.security.MessageDigest

/** File helpers on `java.nio`, written to compile with Scala 2.12 and 3
  * alike (no collection converters). */
private[core] object IO {

  /** The regular files under `dir`, recursively, sorted. */
  def files(dir: Path): Seq[Path] =
    if (!Files.isDirectory(dir)) Nil
    else {
      val stream = Files.walk(dir)
      try {
        val b = Seq.newBuilder[Path]
        val it = stream.iterator()
        while (it.hasNext) {
          val p = it.next()
          if (Files.isRegularFile(p)) b += p
        }
        b.result().sortBy(_.toString)
      } finally stream.close()
    }

  /** The directories under `dir`, deepest first. */
  def dirs(dir: Path): Seq[Path] =
    if (!Files.isDirectory(dir)) Nil
    else {
      val stream = Files.walk(dir)
      try {
        val b = Seq.newBuilder[Path]
        val it = stream.iterator()
        while (it.hasNext) {
          val p = it.next()
          if (Files.isDirectory(p)) b += p
        }
        b.result().sortBy(-_.getNameCount)
      } finally stream.close()
    }

  def read(file: Path): String = new String(Files.readAllBytes(file), StandardCharsets.UTF_8)

  /** Writes `content` unless the file already holds it, so that unchanged
    * files keep their timestamps. Returns whether it wrote. */
  def writeIfChanged(file: Path, content: Array[Byte]): Boolean =
    if (Files.isRegularFile(file) && java.util.Arrays.equals(Files.readAllBytes(file), content)) false
    else {
      Option(file.getParent).foreach(Files.createDirectories(_))
      Files.write(file, content)
      true
    }

  def write(file: Path, content: String): Unit = {
    Option(file.getParent).foreach(Files.createDirectories(_))
    Files.write(file, content.getBytes(StandardCharsets.UTF_8))
  }

  def deleteRecursively(path: Path): Unit = {
    files(path).foreach(Files.delete)
    dirs(path).foreach(Files.delete)
    Files.deleteIfExists(path)
  }

  /** Removes the empty directories under `dir`, keeping `dir`. */
  def deleteEmptyDirs(dir: Path): Unit =
    dirs(dir).filter(_ != dir).foreach { d =>
      val s = Files.list(d)
      val empty = try !s.iterator().hasNext finally s.close()
      if (empty) Files.delete(d)
    }

  def sha256(parts: Seq[String]): String = {
    val md = MessageDigest.getInstance("SHA-256")
    parts.foreach { p =>
      md.update(p.getBytes(StandardCharsets.UTF_8))
      md.update(0.toByte)
    }
    md.digest().map(b => f"${b & 0xff}%02x").mkString
  }
}
