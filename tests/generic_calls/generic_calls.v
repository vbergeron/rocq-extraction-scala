(* Scala 3 extraction backend: calls to a generic definition other than
   the caller itself. The callee's type is phrased in its own type
   variables; the caller translates them into its own types, from the
   arguments, the way Scala infers them:

   - [negFst]: [fst p] is known to be a [Boolean], so it is passed to
     [negb] uncast.
   - [nestedFst]: the inner [fst] call's own [T1] is not the caller's
     [A], and the outer [fst] call's argument gets cast to
     [Tuple2[Any, Any]], so its result is an [Any], cast back to [A].
   - [appMap]: [negb] pins [map]'s type variables to [Boolean], so
     [l] is checked against [List[Boolean]] rather than falling back to
     [Any => Any]; [map negb l] is then a [List[Boolean]], and so is
     [app]'s result: no cast at all. *)

From Corelib Require Import ListDef.
From ScalaExtraction Require Import ScalaExtraction ExtrScalaBasic.

Extraction Language Scala.

Definition negFst (p : bool * bool) : bool := negb (fst p).

Definition nestedFst (A B : Type) (p : A * B) (q : B * A) : A := fst (fst (p, q)).

Definition appMap (l : list bool) : list bool := app (map negb l) l.

Recursive Extraction negFst nestedFst appMap.
