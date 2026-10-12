(* Scala 3 extraction backend: covariant type parameters and case
   objects. A parameter is covariant ([+a]) when it occurs only at
   covariant positions in the constructor fields - not under a function
   domain, again under two - including through another covariant type
   of the structure. A constructor without fields is a [case object]
   only when every parameter of its type is covariant. *)

From ScalaExtraction Require Import ScalaExtraction.

Extraction Language Scala.

Inductive mylist (A : Type) : Type :=
  | MyNil : mylist A
  | MyCons : A -> mylist A -> mylist A.

Arguments MyNil {A}.
Arguments MyCons {A} _ _.

(* Under a function domain: invariant, and [PNone] stays a case class. *)
Inductive pred (A : Type) : Type :=
  | PNone : pred A
  | PSome : (A -> bool) -> pred A.

(* Under two function domains: covariant again. *)
Inductive cont (A : Type) : Type :=
  | CNone : cont A
  | CSome : ((A -> bool) -> bool) -> cont A.

(* [A] covariant, [B] not: [MEmpty] stays a case class. *)
Inductive mixed (A B : Type) : Type :=
  | MEmpty : mixed A B
  | MLeft : A -> mixed A B
  | MRight : (B -> bool) -> mixed A B.

(* Covariant through [mylist], itself covariant. *)
Inductive rose (A : Type) : Type :=
  | MkRose : A -> mylist (rose A) -> rose A.

Definition pnone (A : Type) : pred A := PNone A.

Definition cnone (A : Type) : cont A := CNone A.

Definition mempty (A B : Type) : mixed A B := MEmpty A B.

Definition isNil (A : Type) (l : mylist A) : bool :=
  match l with
  | MyNil => true
  | MyCons _ _ => false
  end.

Definition single (A : Type) (a : A) : rose A := MkRose A a MyNil.

Recursive Extraction pnone cnone mempty isNil single.
