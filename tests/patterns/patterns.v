(* Scala 3 extraction backend: nested patterns and wildcards, on case
   classes, on tuples ([prod] extracted to a Scala tuple) and on
   [List]. *)

From ScalaExtraction Require Import ScalaExtraction ExtrScalaBasic.

Extraction Language Scala.

Local Open Scope list_scope.

Definition pred2 (n : nat) : nat :=
  match n with
  | S (S m) => m
  | _ => 0
  end.

Definition both (p : bool * bool) : bool :=
  match p with
  | (true, true) => true
  | _ => false
  end.

Definition second (A : Type) (d : A) (l : list A) : A :=
  match l with
  | _ :: x :: _ => x
  | _ => d
  end.

Definition firstOfPair (A B : Type) (d : A) (l : list (A * B)) : A :=
  match l with
  | (a, _) :: _ => a
  | nil => d
  end.

Recursive Extraction pred2 both second firstOfPair.
