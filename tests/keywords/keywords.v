(* Scala 3 extraction backend: Rocq identifiers that are Scala keywords
   or reserved names - definitions, parameters, local binders, an
   inductive type, its constructors and its type parameter - renamed so
   that the extracted code compiles. *)

From ScalaExtraction Require Import ScalaExtraction.

Extraction Language Scala.

Definition val (n : nat) : nat := n.

Definition type (def : nat) : nat := val def.

Definition toString (this : nat) : nat := let new := S this in new.

Definition given (A : Type) (yield : A) : A := yield.

Inductive class : Type := trait | object.

Definition equals (c : class) : class :=
  match c with
  | trait => object
  | object => trait
  end.

Inductive lazy (var : Type) : Type :=
  | Thunk : lazy var
  | Forced : var -> lazy var.

Arguments Thunk {var}.
Arguments Forced {var} _.

Definition force (var : Type) (default : var) (l : lazy var) : var :=
  match l with
  | Thunk => default
  | Forced while => while
  end.

Recursive Extraction type toString given equals force.
