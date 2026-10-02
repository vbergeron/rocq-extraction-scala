(* Scala 3 extraction backend: [Set Extraction Scala Package] and the
   [object] wrapper every file gets regardless. The filename-derived
   object name is checked by filename.v. *)

From ScalaExtraction Require Import ScalaExtraction.

Extraction Language Scala.

Definition answer : nat := 42.

(* No package set yet: only the wrapping object appears. *)
Recursive Extraction answer.

Set Extraction Scala Package "com.foo.bar".

(* A valid package prints a [package com.foo.bar] line before the
   object. *)
Recursive Extraction answer.

(* Rejected outright, instead of emitting Scala that doesn't compile. *)
Fail Set Extraction Scala Package "1bad.pkg".
Fail Set Extraction Scala Package "bad name".

Unset Extraction Scala Package.

(* Back to no package line, same as the very first extraction above. *)
Recursive Extraction answer.
