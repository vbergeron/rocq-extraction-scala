(* Scala 3 extraction backend: local [fix] and [cofix], printed as
   inner [def]s with one parameter list per parameter - applied, partially
   applied, unapplied, mutually recursive, and corecursive. *)

From ScalaExtraction Require Import ScalaExtraction.

Extraction Language Scala.

(* Applied to all its arguments: the call is inside the block. *)
Definition sumTo (n : nat) : nat :=
  let fix go (k acc : nat) : nat :=
    match k with
    | 0 => acc
    | S p => go p (k + acc)
    end
  in go n 0.

(* Partially applied, under a constructor so that extraction keeps it
   local. *)
Definition addTo (n : nat) : nat * (nat -> nat) :=
  (n,
   let fix go (k acc : nat) : nat :=
     match k with
     | 0 => acc
     | S p => S (go p acc)
     end
   in go n).

(* Mutually recursive, and not applied: a function value. *)
Definition isEven (n : nat) : bool :=
  (fix even (k : nat) : bool :=
    match k with
    | 0 => true
    | S p => odd p
    end
  with odd (k : nat) : bool :=
    match k with
    | 0 => false
    | S p => even p
    end
  for even) n.

CoInductive stream (A : Type) : Type :=
  | Scons : A -> stream A -> stream A.

Arguments Scons {A} _ _.

(* A local [cofix] with a parameter. *)
Definition nats (n : nat) : stream nat :=
  let cofix from (k : nat) : stream nat := Scons k (from (S k)) in from n.

(* A local [cofix] without parameters, under a constructor. *)
Definition repeat (n : nat) : stream nat * nat :=
  (let cofix ones : stream nat := Scons n ones in ones, n).

Recursive Extraction sumTo addTo isEven nats repeat.
