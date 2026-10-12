(* Scala 3 extraction backend: [Set Extraction File Comment], printed
   first, then the [package] clause, then the pure functions import,
   then the wrapping [object]. *)

From ScalaExtraction Require Import ScalaExtraction.

Extraction Language Scala.

Set Extraction File Comment "Extracted from header_comment.v.".
Set Extraction Scala Package "com.foo".
Set Extraction Scala Pure Functions.

Definition twice (f : nat -> nat) (n : nat) : nat := f (f n).

Recursive Extraction twice.
