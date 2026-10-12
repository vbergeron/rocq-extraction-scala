(* Scala 3 extraction backend: [ExtrScalaZBigInt], [positive], [N] and
   [Z] as Scala's [BigInt] - the custom matches of [positive] and [Z],
   their constructors, and operations replaced by their [BigInt]
   counterparts. *)

From Corelib Require Import BinNums.PosDef BinNums.NatDef BinNums.IntDef.
From ScalaExtraction Require Import ScalaExtraction ExtrScalaZBigInt.

Extraction Language Scala.

Definition zops (a b : Z) : Z :=
  Z.add (Z.mul a b) (Z.opp (Z.add (Z.div a b) (Z.modulo a b))).

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

Definition nsub (a b : N) : N := N.sub b a.

Recursive Extraction zops zclass psize nsub.
