(* Scala 3 extraction backend: [ascii] and [string] extracted to
   Scala's [Char] and [String], with constant characters and strings
   printed as Scala literals (escaped), and non-constant ones built
   with the given constructors. [ascii] and [string] are defined here,
   as they are not in Corelib, and registered as Stdlib registers
   them. *)

From ScalaExtraction Require Import ScalaExtraction ExtrScalaBasic.

Extraction Language Scala.

Inductive ascii : Set := Ascii (_ _ _ _ _ _ _ _ : bool).

Register ascii as core.ascii.type.
Register Ascii as core.ascii.ascii.

Inductive string : Set :=
  | EmptyString : string
  | String : ascii -> string -> string.

Register string as core.string.type.
Register EmptyString as core.string.empty.
Register String as core.string.string.

Extract Inductive ascii => "Char"
  [ "((b0: Boolean, b1: Boolean, b2: Boolean, b3: Boolean, b4: Boolean, b5: Boolean, b6: Boolean, b7: Boolean) => List(b0, b1, b2, b3, b4, b5, b6, b7).zipWithIndex.map((b, i) => if (b) 1 << i else 0).sum.toChar)" ].

Extract Inductive string => "String"
  [ """""" "((c: Char, s: String) => c.toString + s)" ]
  "((fE: Any => Any) => (fS: Any => Any) => (s: String) =>
      if (s.isEmpty) fE(()) else fS(s.head).asInstanceOf[Any => Any](s.tail))".

(* Characters, least significant bit first. *)
Abbreviation ch_a := (Ascii true false false false false true true false).
Abbreviation ch_h := (Ascii false false false true false true true false).
Abbreviation ch_i := (Ascii true false false true false true true false).
Abbreviation ch_quote := (Ascii true true true false false true false false).
Abbreviation ch_dquote := (Ascii false true false false false true false false).
Abbreviation ch_backslash := (Ascii false false true true true false true false).
Abbreviation ch_newline := (Ascii false true false true false false false false).

Definition chA : ascii := ch_a.
Definition chQuote : ascii := ch_quote.
Definition chBackslash : ascii := ch_backslash.
Definition chNewline : ascii := ch_newline.

Definition hi : string := String ch_h (String ch_i EmptyString).
Definition escapes : string :=
  String ch_dquote (String ch_quote (String ch_backslash (String ch_newline EmptyString))).
Definition empty : string := EmptyString.

(* Not constant: built with the given constructors. *)
Definition mkChar (b : bool) : ascii := Ascii b false false false false true true false.
Definition prepend (c : ascii) (s : string) : string := String c s.

Fixpoint strLength (s : string) : nat :=
  match s with
  | EmptyString => 0
  | String _ t => S (strLength t)
  end.

Definition headOr (c : ascii) (s : string) : ascii :=
  match s with
  | EmptyString => c
  | String c' _ => c'
  end.

Recursive Extraction chA chQuote chBackslash chNewline hi escapes empty
  mkChar prepend strLength headOr.
