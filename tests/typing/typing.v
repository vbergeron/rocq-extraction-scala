(* Scala 3 extraction backend: terms whose types the [Mltyping] pass
   has to reconstruct beyond the declared signatures - local
   definitions, polymorphic local definitions, terms that extraction
   itself types with [Obj.magic]-like coercions, and partial
   applications. *)

From ScalaExtraction Require Import ScalaExtraction.

Extraction Language Scala.

(* A local polymorphic definition, used at two types: printed as a
   local generic [def]. *)
Definition letPoly (n : nat) : nat * bool :=
  let id := fun (A : Type) (x : A) => x in (id nat n, id bool true).

(* A local fixpoint, its type inferred. *)
Definition sumTo (n : nat) : nat :=
  let fix go (k acc : nat) : nat :=
    match k with
    | 0 => acc
    | S p => go p (k + acc)
    end
  in go n 0.

(* A dependent type: extracted as [Any], cast back where used at a
   precise type. *)
Definition dep (b : bool) : if b then nat else bool :=
  match b with
  | true => 0
  | false => true
  end.

Definition useDep : nat := dep true.

(* A polymorphic argument, used at two types. *)
Definition applyPoly (f : forall A, A -> A) : nat * bool := (f nat 0, f bool true).

Definition usePoly : nat * bool := applyPoly (fun A x => x).

(* Partial applications of a constructor and of a function. *)
Fixpoint mapNat (f : nat -> nat) (l : list nat) : list nat :=
  match l with
  | nil => nil
  | cons a t => cons (f a) (mapNat f t)
  end.

Definition succAll (l : list nat) : list nat := mapNat S l.

Definition addAll (n : nat) (l : list nat) : list nat := mapNat (Nat.add n) l.

(* A match returning a function, applied. *)
Definition caseFun (b : bool) (n : nat) : nat :=
  (match b with
   | true => S
   | false => fun x => x
   end) n.

(* A logical argument. *)
Definition predSafe (n : nat) (H : n <> 0) : nat :=
  match n with
  | 0 => 0
  | S p => p
  end.

Definition usePred : nat := predSafe 1 (fun H => match H with end).

(* An axiom. *)
Axiom ax : nat -> nat.

Definition useAx : nat := ax 0.

(* [sig]: a singleton, erased to its field. *)
Definition sigEx (n : nat) : {m : nat | m = n} := exist _ n eq_refl.

Definition sigVal (n : nat) : nat := proj1_sig (sigEx n).

(* A polymorphic record and its projections. *)
Record pair (A B : Type) := MkPair { pfst : A ; psnd : B }.

Arguments MkPair {A B} _ _.
Arguments pfst {A B} _.
Arguments psnd {A B} _.

Definition swapPair (A B : Type) (p : pair A B) : pair B A := MkPair (psnd p) (pfst p).

(* Primes: a Scala identifier cannot contain one, nor end with an
   underscore ([n_: Nat] would read as the identifier [n_:]), so [n']
   becomes [_n]. *)
Definition primes (n' : nat) : nat := let m' := S n' in m' + m'.

Recursive Extraction primes letPoly sumTo useDep usePoly succAll addAll caseFun usePred useAx
  sigVal swapPair.
