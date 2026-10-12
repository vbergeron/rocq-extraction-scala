(* Scala 3 extraction backend: binders whose name ends with [_] or ['] -
   a space keeps the colon of their type ascription out of the
   identifier - as lambda parameters, [let] and [match] binders, and in
   a local fixpoint's parameters. *)

From ScalaExtraction Require Import ScalaExtraction.

Extraction Language Scala.

Fixpoint mapNat (f : nat -> nat) (l : list nat) : list nat :=
  match l with
  | nil => nil
  | cons a t => cons (f a) (mapNat f t)
  end.

Definition lam (l : list nat) : list nat := mapNat (fun w_ => S w_) (mapNat (fun v' => v') l).

Definition under (x_ : nat) : nat :=
  let y_ := S x_ in
  match y_ with
  | 0 => 0
  | S z_ => z_ + y_
  end.

Definition localFix (n_ : nat) : nat :=
  let fix go (k_ acc' : nat) : nat :=
    match k_ with
    | 0 => acc'
    | S p_ => go p_ (S acc')
    end
  in go n_ 0.

Recursive Extraction lam under localFix.
