package rocqextraction.core

/** Checks of the settings, before running Rocq, so that a mistake is
  * reported in terms of the build and not as a Rocq error. */
object Validation {
  private val Identifier = "[A-Za-z_][A-Za-z0-9_]*"
  private val ScalaPackage = s"$Identifier(\\.$Identifier)*".r
  // Rocq qualified names also allow primes and non-ASCII letters
  private val RocqIdentifier = """[\p{L}_][\p{L}\p{N}_']*"""
  private val RocqQualid = s"$RocqIdentifier(\\.$RocqIdentifier)*".r

  def errors(extractions: Seq[Extraction], options: ExtractionOptions): Seq[String] = {
    def pkg(where: String, p: Option[String]): Seq[String] =
      p.toSeq.filterNot(ScalaPackage.pattern.matcher(_).matches).map { p =>
        s"$where: Scala package `$p` is not a dotted sequence of identifiers"
      }
    val global = pkg("rocqScalaPackage", options.scalaPackage)
    val each = extractions.flatMap { e =>
      val where = s"extraction `${e.name}`"
      Seq(
        if (Identifier.r.pattern.matcher(e.name).matches) None
        else Some(s"$where: the name must be a valid identifier, as it names the file and its object"),
        if (RocqQualid.pattern.matcher(e.module).matches) None
        else Some(s"$where: `${e.module}` is not a Rocq module path"),
        if (e.definitions.nonEmpty) None else Some(s"$where: no definitions to extract")
      ).flatten ++
        e.definitions.filterNot(RocqQualid.pattern.matcher(_).matches).map { d =>
          s"$where: `$d` is not a Rocq name"
        } ++ pkg(where, e.scalaPackage)
    }
    val duplicates = extractions
      .groupBy(e => (e.scalaPackage.orElse(options.scalaPackage), e.name))
      .collect { case ((_, name), es) if es.size > 1 => s"several extractions are named `$name`" }
      .toSeq
      .sorted
    global ++ each ++ duplicates
  }

  def check(extractions: Seq[Extraction], options: ExtractionOptions): Unit = {
    val errs = errors(extractions, options)
    if (errs.nonEmpty) throw new RocqException(errs.mkString("Invalid Rocq extraction settings:\n  ", "\n  ", ""))
  }
}
