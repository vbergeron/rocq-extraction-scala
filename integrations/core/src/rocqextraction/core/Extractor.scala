package rocqextraction.core

import java.nio.file.{Files, Path}

/** Runs the declared extractions. */
object Extractor {

  final case class Input(
      loadPaths: Seq[LoadPath],
      flags: Seq[String],
      extractions: Seq[Extraction],
      options: ExtractionOptions
  )

  /** Extracts into a fresh directory under `workDir`, then syncs the result
    * into `outputDir` (see [[Sync]]). Returns the files of `outputDir`. */
  def extract(input: Input, toolchain: Toolchain, workDir: Path, outputDir: Path, logger: Logger): Seq[Path] = {
    Validation.check(input.extractions, input.options)
    Files.createDirectories(outputDir)
    if (input.extractions.isEmpty) Sync(Nil, outputDir)
    else {
      val tmp = workDir.resolve("extracted")
      IO.deleteRecursively(tmp)
      Files.createDirectories(tmp)
      val driverDir = workDir.resolve("driver")
      IO.deleteRecursively(driverDir)
      val driver = driverDir.resolve(Driver.fileName)
      IO.write(driver, Driver.source(input.extractions, input.options, tmp))
      logger.info(s"Rocq: extracting ${input.extractions.map(_.name).mkString(", ")}")
      val args = Seq("c", "-q") ++ input.loadPaths.flatMap(_.args) ++ input.flags :+ Driver.fileName
      val r = toolchain.run(args, driverDir)
      if (!r.ok)
        throw new RocqException(s"Rocq extraction failed:\n${r.output.trim}", Problems.parse(r.output, driverDir))
      if (r.output.trim.nonEmpty) logger.warn(r.output.trim)
      val produced = IO.files(tmp).filter(_.getFileName.toString.endsWith(".scala"))
      val missing = input.extractions.map(_.name + ".scala").filterNot(n => produced.exists(_.getFileName.toString == n))
      if (missing.nonEmpty)
        throw new RocqException(s"Rocq extraction did not produce ${missing.mkString(", ")}:\n${r.output.trim}")
      Sync(produced, outputDir)
    }
  }
}
