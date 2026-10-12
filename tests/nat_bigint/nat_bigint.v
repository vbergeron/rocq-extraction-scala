(* Scala 3 extraction backend: [ExtrScalaNatBigInt], [nat] as Scala's
   [BigInt] - a recursive function through the custom [nat] match, and
   operations replaced by their [BigInt] counterparts. *)

From ScalaExtraction Require Import ScalaExtraction ExtrScalaNatBigInt.

Extraction Language Scala.

Fixpoint fact (n : nat) : nat :=
  match n with
  | 0 => 1
  | S p => n * fact p
  end.

Definition ops (a b : nat) : bool :=
  Nat.leb (a - b) (Nat.div a b + Nat.modulo a b).

Recursive Extraction fact ops.
