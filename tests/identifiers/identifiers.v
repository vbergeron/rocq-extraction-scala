(* Scala 3 extraction backend: names ending with [_] or ['], which
   Scala identifiers must not (a colon after [x_] would be part of the
   identifier [x_:]): their trailing underscores move to the front, [x_]
   and [x'] both becoming [_x], told apart by a number when both are in
   scope. As definitions, lambda parameters, [let] and [match] binders,
   and a local fixpoint and its parameters. *)

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
  let fix go_ (k_ acc' : nat) : nat :=
    match k_ with
    | 0 => acc'
    | S p_ => go_ p_ (S acc')
    end
  in go_ n_ 0.

(* [x_] and [x'] in the same scope. *)
Definition both (x_ x' : nat) : nat := x_ + x'.

(* A global name. *)
Definition plus' (n : nat) : nat := S n.

Definition usePlus (n : nat) : nat := plus' n.

Recursive Extraction lam under localFix both usePlus.
