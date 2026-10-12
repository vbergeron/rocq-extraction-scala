(* Scala 3 extraction backend: [Set Extraction Scala Pure Functions]
   with an [Extract Inductive] match function: each branch is cast to
   a pure function type [Any -> Any], which the match function, typed
   with impure arrows, accepts. *)

From ScalaExtraction Require Import ScalaExtraction ExtrScalaNatBigInt.

Extraction Language Scala.

Set Extraction Scala Pure Functions.

Fixpoint fact (n : nat) : nat :=
  match n with
  | 0 => 1
  | S p => n * fact p
  end.

Recursive Extraction fact.
