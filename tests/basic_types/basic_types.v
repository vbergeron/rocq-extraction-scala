(* Scala 3 extraction backend: [ExtrScalaBasic], mapping [list],
   [option], [prod], [bool] and [unit] to Scala's [List], [Option],
   tuples, [Boolean] and [Unit]. *)

From ScalaExtraction Require Import ScalaExtraction ExtrScalaBasic.

Extraction Language Scala.

Fixpoint map (A B : Type) (f : A -> B) (l : list A) : list B :=
  match l with
  | nil => nil
  | cons a t => cons (f a) (map A B f t)
  end.

Definition headOpt (A : Type) (l : list A) : option A :=
  match l with
  | nil => None
  | cons a _ => Some a
  end.

Definition swap (A B : Type) (p : A * B) : B * A :=
  match p with
  | (a, b) => (b, a)
  end.

Definition isEmpty (A : Type) (l : list A) : bool :=
  match l with
  | nil => true
  | cons _ _ => false
  end.

Definition ttv : unit := tt.

Recursive Extraction map headOpt swap isEmpty ttv.
