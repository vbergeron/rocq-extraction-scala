(** Extraction of the basic Corelib datatypes to their Scala standard
    library counterparts, the Scala analogue of [ExtrOcamlBasic]:
    [Require] it next to [ScalaExtraction] to opt in. Each mapping is
    exact - a Scala value of the target type is always a valid value
    of the Rocq type and conversely. *)

From Corelib Require Import ListDef.
From ScalaExtraction Require Import ScalaExtraction.

Extract Inductive bool => "Boolean" [ "true" "false" ].
Extract Inductive option => "Option" [ "Some" "None" ].
Extract Inductive unit => "Unit" [ "()" ].
Extract Inductive list => "List" [ "Nil" "::" ].
(* An empty constructor name prints [(a, b)], a Scala tuple, both as a
   term and as a pattern. *)
Extract Inductive prod => "Tuple2" [ "" ].
Extract Inductive sum => "Either" [ "Left" "Right" ].

(** Same as [ExtrOcamlBasic]: [sumbool] and [sumor] as [bool] and
    [option], forgetting their logical content. *)

Extract Inductive sumbool => "Boolean" [ "true" "false" ].
Extract Inductive sumor => "Option" [ "Some" "None" ].

(** Their basic operations, as Scala's own. [Extract Constant], not
    [Extract Inlined Constant]: each one is then declared with its real
    type (e.g. [val andb: Boolean => Boolean => Boolean = ...]), which
    lets the lambdas below leave their parameter types to Scala's
    inference, and keeps their callers free of casts. *)

Extract Constant andb => "a => b => a && b".
Extract Constant orb => "a => b => a || b".
Extract Constant implb => "a => b => !a || b".
Extract Constant xorb => "a => b => a != b".
Extract Constant negb => "b => !b".

Extract Constant fst => "p => p._1".
Extract Constant snd => "p => p._2".

Extract Constant option_map => "f => o => o.map(f)".

Extract Constant app => "l => m => l ++ m".
Extract Constant ListDef.map => "f => l => l.map(f)".

(** Same as [ExtrOcamlBasic]: the computational content of a dependent
    rewrite is the rewritten term itself. *)

Extract Inlined Constant eq_rew_r_dep => "((_: Any) => (_: Any) => (t: Any) => t)".
