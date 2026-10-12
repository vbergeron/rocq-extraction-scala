(* Scala 3 extraction backend: a larger, real-world extraction - the
   binary arithmetic of Corelib ([positive], [N], [Z]) and [nat] - to be
   compiled by scalac. It caught a bug the small tests did not. *)

From ScalaExtraction Require Import ScalaExtraction.
From Corelib Require Import BinNums.PosDef BinNums.NatDef BinNums.IntDef ListDef.

Extraction Language Scala.

Recursive Extraction Pos.add Pos.mul Pos.sub Pos.compare Pos.to_nat Pos.sqrtrem Pos.div2
  N.sub N.compare
  Z.add Z.mul Z.sub Z.div Z.modulo Z.quot Z.rem Z.pow Z.of_nat Z.to_nat Z.compare
  Nat.add Nat.mul Nat.sub Nat.div Nat.modulo Nat.pow Nat.gcd Nat.sqrt Nat.log2 Nat.eqb Nat.leb Nat.iter Nat.of_num_uint
  ListDef.map.
