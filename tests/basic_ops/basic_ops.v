(* Scala 3 extraction backend: [ExtrScalaBasic]'s operations - the
   boolean connectives, [fst]/[snd], [app], [ListDef.map] and
   [option_map] - declared with their real types next to their Scala
   implementations, and called like any other definition. *)

From Corelib Require Import ListDef.
From ScalaExtraction Require Import ScalaExtraction ExtrScalaBasic.

Extraction Language Scala.

Definition boolOps (a b : bool) : bool :=
  xorb (implb (negb a) (orb a b)) (andb a b).

Definition pairList (A : Type) (p : A * A) : list A :=
  fst p :: snd p :: nil.

Definition appOne (l : list bool) : list bool := app l (true :: nil).

Definition negAll (l : list bool) : list bool := map negb (true :: l).

Definition negOpt (b : bool) : option bool := option_map negb (Some b).

Recursive Extraction boolOps pairList appOne negAll negOpt.
