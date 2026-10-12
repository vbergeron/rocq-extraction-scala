(* Scala 3 extraction backend: [Extract Inlined Constant], whose
   replacement is printed at every use and never declared; a
   polymorphic [Extract Constant], declared as a [def] with type
   parameters; and an axiom realized by [Extract Constant]. *)

From Corelib Require Import ListDef.
From ScalaExtraction Require Import ScalaExtraction ExtrScalaBasic.

Extraction Language Scala.

Definition myNeg (b : bool) : bool :=
  match b with
  | true => false
  | false => true
  end.

Extract Inlined Constant myNeg => "((b: Boolean) => !b)".

Definition negTwice (b : bool) : bool := myNeg (myNeg b).

Definition negAll (l : list bool) : list bool := ListDef.map myNeg l.

Definition myId (A : Type) (x : A) : A := x.

Extract Constant myId => "x => x".

Definition useId (b : bool) : bool * list bool := (myId bool b, myId (list bool) nil).

Axiom answer : nat.

Extract Constant answer => "S(S(O()))".

Definition useAnswer : nat := S answer.

Recursive Extraction negTwice negAll useId useAnswer.
