(* Scala 3 extraction backend: calls to a generic definition other than
   the caller itself. The callee's type is phrased in its own type
   variables; the caller translates them into its own types, from the
   arguments, the way Scala infers them:

   - [negFst]: [fst p] is known to be a [Boolean], so it is passed to
     [negb] uncast.
   - [nestedFst]: the pair [(p, q)] is known to be a
     [Tuple2[Tuple2[A, B], Tuple2[B, A]]] from its fields, so the inner
     [fst] call is a [Tuple2[A, B]] and the outer one an [A]: no cast.
   - [appMap]: [negb] pins [map]'s type variables to [Boolean], so
     [l] is checked against [List[Boolean]] rather than falling back to
     [Any => Any]; [map negb l] is then a [List[Boolean]], and so is
     [app]'s result: no cast at all.
   - [noneMap]: [f] pins [option_map]'s type variables to the caller's
     [A], but [None] alone is known only as some [Option]: it is cast
     to the translated [Option[A]], in the caller's own type names. *)

From Corelib Require Import ListDef.
From ScalaExtraction Require Import ScalaExtraction ExtrScalaBasic.

Extraction Language Scala.

Definition negFst (p : bool * bool) : bool := negb (fst p).

Definition nestedFst (A B : Type) (p : A * B) (q : B * A) : A := fst (fst (p, q)).

Definition appMap (l : list bool) : list bool := app (map negb l) l.

Definition noneMap (A : Type) (f : A -> A) : option A := option_map f None.

Recursive Extraction negFst nestedFst appMap noneMap.
