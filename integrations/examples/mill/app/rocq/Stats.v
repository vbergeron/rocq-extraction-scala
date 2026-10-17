(** Statistics on top of the verified sort of the [sorting] module. *)

Require Sorting.Insertion.

Open Scope list_scope.

(** The smallest element, if any. *)
Definition minimum (l : list nat) : option nat :=
  match Sorting.Insertion.sort l with
  | nil => None
  | x :: _ => Some x
  end.

(** The elements in increasing order, without duplicates. *)
Fixpoint dedup_sorted (l : list nat) : list nat :=
  match l with
  | x :: (y :: _) as t => if Nat.eqb x y then dedup_sorted t else x :: dedup_sorted t
  | _ => l
  end.

Definition distinct (l : list nat) : list nat :=
  dedup_sorted (Sorting.Insertion.sort l).
