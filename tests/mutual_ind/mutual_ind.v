(* Scala 3 extraction backend: mutually inductive types, polymorphic,
   whose constructors refer to each other, and mutually recursive
   functions over them. *)

From ScalaExtraction Require Import ScalaExtraction.

Extraction Language Scala.

Inductive tree (A : Type) : Type :=
  | Node : A -> forest A -> tree A
with forest (A : Type) : Type :=
  | Empty : forest A
  | Grove : tree A -> forest A -> forest A.

Arguments Node {A} _ _.
Arguments Empty {A}.
Arguments Grove {A} _ _.

Fixpoint tsize (A : Type) (t : tree A) : nat :=
  match t with
  | Node _ f => S (fsize A f)
  end
with fsize (A : Type) (f : forest A) : nat :=
  match f with
  | Empty => 0
  | Grove t f' => tsize A t + fsize A f'
  end.

Fixpoint tmap (A B : Type) (g : A -> B) (t : tree A) : tree B :=
  match t with
  | Node a f => Node (g a) (fmap A B g f)
  end
with fmap (A B : Type) (g : A -> B) (f : forest A) : forest B :=
  match f with
  | Empty => Empty
  | Grove t f' => Grove (tmap A B g t) (fmap A B g f')
  end.

Recursive Extraction tsize tmap.
