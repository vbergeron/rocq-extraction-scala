(** Extraction of the basic Corelib datatypes to their Scala standard
    library counterparts, the Scala analogue of [ExtrOcamlBasic]:
    [Require] it next to [ScalaExtraction] to opt in. Each mapping is
    exact - a Scala value of the target type is always a valid value
    of the Rocq type and conversely. *)

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
