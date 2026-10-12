(* Scala 3 extraction backend: local mutual fixpoints - a block of
   several local [def]s, each with its type, monomorphic and inside a
   polymorphic definition. *)

From ScalaExtraction Require Import ScalaExtraction.

Extraction Language Scala.

Definition parity (n : nat) : bool * bool :=
  ((fix ev (k : nat) : bool :=
      match k with
      | 0 => true
      | S p => od p
      end
    with od (k : nat) : bool :=
      match k with
      | 0 => false
      | S p => ev p
      end
    for ev) n,
   (fix ev (k : nat) : bool :=
      match k with
      | 0 => true
      | S p => od p
      end
    with od (k : nat) : bool :=
      match k with
      | 0 => false
      | S p => ev p
      end
    for od) n).

Definition evenLength (A : Type) (l : list A) : bool :=
  (fix ev (l : list A) : bool :=
     match l with
     | nil => true
     | cons _ t => od t
     end
   with od (l : list A) : bool :=
     match l with
     | nil => false
     | cons _ t => ev t
     end
   for ev) l.

Recursive Extraction parity evenLength.
