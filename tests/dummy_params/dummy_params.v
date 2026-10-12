(* Scala 3 extraction backend: anonymous, type and logical parameters
   of top-level definitions and lambdas, erased or renamed, and their
   callers. *)

From ScalaExtraction Require Import ScalaExtraction.

Extraction Language Scala.

Definition ignoreFirst (_ : nat) (m : nat) : nat := m.

Definition useIgnore : nat := ignoreFirst 0 1.

Definition constFun : nat -> nat := fun _ => 0.

Definition withProof (n : nat) (H : n = n) : nat := n.

Definition useProof : nat := withProof 0 eq_refl.

Definition withType (A : Type) (n : nat) : nat := n.

Definition useType : nat := withType bool 0.

(* A logical parameter in a function passed as a value. *)
Definition applyProof (f : forall n : nat, n = n -> nat) : nat := f 0 eq_refl.

Definition useApplyProof : nat := applyProof withProof.

Recursive Extraction useIgnore constFun useProof useType useApplyProof.
