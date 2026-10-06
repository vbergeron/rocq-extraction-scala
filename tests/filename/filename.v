(* Scala 3 extraction backend: extraction to a named file wraps its
   content in an [object] named after the file, so the file name must
   be a valid identifier. Modular extraction is not supported. *)

From ScalaExtraction Require Import ScalaExtraction.

Extraction Language Scala.

Set Extraction Output Directory ".".

Definition answer : nat := 2.

Extraction "answer_file" answer.

Fail Extraction "bad-name" answer.

Fail Separate Extraction answer.
