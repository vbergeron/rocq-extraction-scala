(* Scala 3 extraction backend: type definitions ([Dtype]) - a plain
   alias, a parameterized alias, a type given by [Extract Constant]
   (with and without type parameters), and an axiomatized type, printed
   as [Any] until realized. *)

From ScalaExtraction Require Import ScalaExtraction.

Extraction Language Scala.

Definition natAlias : Type := nat.

Definition three : natAlias := 3.

Definition pairOf (A : Type) : Type := (A * A)%type.

Definition dup (A : Type) (x : A) : pairOf A := (x, x).

Definition machineInt : Type := nat.

Extract Constant machineInt => "Int".

Definition idInt (i : machineInt) : machineInt := i.

Definition vec (A : Type) : Type := list A.

Extract Constant vec "a" => "Vector[a]".

Definition idVec (A : Type) (v : vec A) : vec A := v.

Parameter opaque : Type.

Definition idOpaque (o : opaque) : opaque := o.

Recursive Extraction three dup idInt idVec idOpaque.
