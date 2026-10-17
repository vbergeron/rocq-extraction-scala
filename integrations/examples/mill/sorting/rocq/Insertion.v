(** Insertion sort on natural numbers, with proofs that its result is
    sorted and is a permutation of its input. Only Corelib is used. *)

Open Scope list_scope.

Fixpoint insert (x : nat) (l : list nat) : list nat :=
  match l with
  | nil => x :: nil
  | y :: t => if Nat.leb x y then x :: y :: t else y :: insert x t
  end.

Fixpoint sort (l : list nat) : list nat :=
  match l with
  | nil => nil
  | x :: t => insert x (sort t)
  end.

(** * The result is sorted *)

Inductive sorted : list nat -> Prop :=
  | sorted_nil : sorted nil
  | sorted_one x : sorted (x :: nil)
  | sorted_cons x y l :
      Nat.leb x y = true -> sorted (y :: l) -> sorted (x :: y :: l).

Lemma leb_false_flip x y : Nat.leb x y = false -> Nat.leb y x = true.
Proof.
  revert y; induction x as [|x IH]; intros [|y] H; simpl in *;
    try reflexivity; try discriminate.
  apply IH, H.
Qed.

Lemma insert_sorted x l : sorted l -> sorted (insert x l).
Proof.
  intros H; induction H as [|y|y z l Hyz Hs IH]; simpl.
  - constructor.
  - destruct (Nat.leb x y) eqn:E.
    + constructor; [exact E | constructor].
    + constructor; [apply leb_false_flip, E | constructor].
  - destruct (Nat.leb x y) eqn:Exy.
    + constructor; [exact Exy | constructor; assumption].
    + simpl in IH; destruct (Nat.leb x z) eqn:Exz.
      * constructor; [apply leb_false_flip, Exy | exact IH].
      * constructor; [exact Hyz | exact IH].
Qed.

Theorem sort_sorted l : sorted (sort l).
Proof.
  induction l as [|x t IH]; simpl.
  - constructor.
  - apply insert_sorted, IH.
Qed.

(** * The result is a permutation of the input *)

Fixpoint count (n : nat) (l : list nat) : nat :=
  match l with
  | nil => 0
  | y :: t => if Nat.eqb n y then S (count n t) else count n t
  end.

Lemma insert_count n x l : count n (insert x l) = count n (x :: l).
Proof.
  induction l as [|y t IH]; simpl.
  - reflexivity.
  - destruct (Nat.leb x y); simpl.
    + reflexivity.
    + rewrite IH; simpl.
      destruct (Nat.eqb n x), (Nat.eqb n y); reflexivity.
Qed.

Theorem sort_count n l : count n (sort l) = count n l.
Proof.
  induction l as [|x t IH]; simpl.
  - reflexivity.
  - rewrite insert_count; simpl; rewrite IH; reflexivity.
Qed.
