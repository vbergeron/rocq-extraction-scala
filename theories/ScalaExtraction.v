(** Loads the extraction plugin together with its Scala 3 backend.
    Then [Extraction Language Scala.] selects Scala as the target
    language of the extraction commands. *)

From Corelib Require Export Extraction.

Declare ML Module "rocq-extraction-scala.plugin".
