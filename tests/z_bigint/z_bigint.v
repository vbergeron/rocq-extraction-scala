(* Scala 3 extraction backend: [ExtrScalaZBigInt], [positive], [N] and
   [Z] as Scala's [BigInt] - the custom matches of all three, their
   constructors, and operations replaced by their [BigInt] counterparts.

   Only operations whose own Rocq body refers to nothing else
   ([Pos.succ], [Z.opp]): extraction still visits the bodies of
   realized constants, and keeps what they refer to whenever it lives
   in a nested module, as [Pos], [N] and [Z] do. *)

From Corelib Require Import BinNums.PosDef BinNums.NatDef BinNums.IntDef.
From ScalaExtraction Require Import ScalaExtraction ExtrScalaZBigInt.

Extraction Language Scala.

Definition zclass (z : Z) : Z :=
  match z with
  | Z0 => Zpos (xO xH)
  | Zpos _ => Zpos xH
  | Zneg _ => Zneg xH
  end.

(* The number of binary digits. *)
Fixpoint psize (p : positive) : positive :=
  match p with
  | xI q => Pos.succ (psize q)
  | xO q => Pos.succ (psize q)
  | xH => xH
  end.

Definition nclass (n : N) : N :=
  match n with
  | N0 => Npos xH
  | Npos _ => N0
  end.

Definition zneg2 (z : Z) : Z := Z.opp (Z.opp z).

Recursive Extraction zclass psize nclass zneg2.
