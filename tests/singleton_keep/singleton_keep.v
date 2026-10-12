(* Scala 3 extraction backend: [Set Extraction KeepSingleton] - a
   single-constructor, single-field inductive and [sig] are kept as
   case classes instead of being erased to their field. *)

From ScalaExtraction Require Import ScalaExtraction.

Extraction Language Scala.

Set Extraction KeepSingleton.

Inductive box (A : Type) : Type := MkBox : A -> box A.

Arguments MkBox {A} _.

Definition unbox (A : Type) (b : box A) : A := match b with MkBox a => a end.

Definition mapBox (A B : Type) (f : A -> B) (b : box A) : box B := MkBox (f (unbox A b)).

Definition sigEx (n : nat) : {m : nat | m = n} := exist _ n eq_refl.

Definition sigVal (n : nat) : nat := proj1_sig (sigEx n).

Recursive Extraction mapBox sigVal.
