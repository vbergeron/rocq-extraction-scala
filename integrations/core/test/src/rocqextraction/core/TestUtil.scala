package rocqextraction.core

import java.nio.file.{Files, Path}

object TestUtil {
  def tempDir(): Path = Files.createTempDirectory("rocq-extraction-test")

  def write(file: Path, content: String): Path = {
    Files.createDirectories(file.getParent)
    Files.write(file, content.getBytes("UTF-8"))
    file
  }

  def read(file: Path): String = new String(Files.readAllBytes(file), "UTF-8")

  /** Whether `rocq` with the Scala extraction is on the PATH. Without it,
    * the tests that need it are skipped, unless `ROCQ_INTEGRATION=required`. */
  lazy val rocqAvailable: Boolean = {
    val ok =
      try {
        Toolchain().check(tempDir())
        true
      } catch { case _: RocqException => false }
    if (!ok && sys.env.get("ROCQ_INTEGRATION").contains("required"))
      throw new AssertionError("ROCQ_INTEGRATION=required but rocq with rocq-extraction-scala is not available")
    ok
  }

  def withRocq(body: => Unit): Unit =
    if (rocqAvailable) body else println("skipped: rocq with rocq-extraction-scala not found")
}
