(* Scala 3 extraction backend: definitions in nested modules and in a
   functor application, flattened into the single [object] of the
   extracted file, with names clashing across modules made unique. *)

From ScalaExtraction Require Import ScalaExtraction.

Extraction Language Scala.

Module M.
  Definition f (n : nat) : nat := S n.
  Module Inner.
    Definition g (n : nat) : nat := f (f n).
  End Inner.
End M.

Definition f (n : nat) : nat := M.Inner.g (M.f n).

Module Type T.
  Parameter t : Type.
  Parameter zero : t.
End T.

Module NatT <: T.
  Definition t := nat.
  Definition zero : t := 0.
End NatT.

Module Make (X : T).
  Definition f : X.t * X.t := (X.zero, X.zero).
End Make.

Module MN := Make NatT.

Recursive Extraction f MN.f.
