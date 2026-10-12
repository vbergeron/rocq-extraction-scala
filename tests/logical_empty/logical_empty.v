(* Scala 3 extraction backend: a logical inductive (in [Prop]), printed
   as a comment, and an empty inductive, printed as [Unit], with a
   function eliminating it. *)

From ScalaExtraction Require Import ScalaExtraction.

Extraction Language Scala.

Inductive even : nat -> Prop :=
  | even0 : even 0
  | evenSS : forall n, even n -> even (S (S n)).

Inductive void : Set := .

Definition absurd (A : Type) (v : void) : A := match v with end.

Definition fromVoid (v : void) : nat := absurd nat v.

Recursive Extraction even void fromVoid.
