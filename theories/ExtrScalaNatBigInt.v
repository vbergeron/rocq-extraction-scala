(** Extraction of [nat] to Scala's arbitrary-precision [BigInt], the
    Scala analogue of [ExtrOcamlNatBigInt]: [Require] it next to
    [ScalaExtraction] to opt in. Each operation below is replaced by its
    [BigInt] counterpart, with the exact same result as the Rocq
    function on every input (truncated subtraction, division by zero
    giving 0, ...). Re-exports [ExtrScalaBasic], since the comparisons
    return a Scala [Boolean].

    [BigInt(0)] rather than a bare [0]: an [Int] literal only becomes a
    [BigInt] where Scala expects one, and a match branch is an [Any]
    position. *)

From ScalaExtraction Require Import ScalaExtraction.
From ScalaExtraction Require Export ExtrScalaBasic.

Extract Inductive nat => "BigInt"
  [ "BigInt(0)" "((n: BigInt) => n + 1)" ]
  "((fO: Any => Any) => (fS: Any => Any) => (n: BigInt) => if (n == 0) fO(()) else fS(n - 1))".

Extract Constant Nat.succ => "n => n + 1".
Extract Constant Nat.pred => "n => (n - 1) max BigInt(0)".
Extract Constant Nat.add => "a => b => a + b".
Extract Constant Nat.mul => "a => b => a * b".
Extract Constant Nat.sub => "a => b => (a - b) max BigInt(0)".
Extract Constant Nat.double => "n => n * 2".
Extract Constant Nat.square => "n => n * n".
Extract Constant Nat.pow => "a => b => a.pow(b.toInt)".

Extract Constant Nat.eqb => "a => b => a == b".
Extract Constant Nat.leb => "a => b => a <= b".
Extract Constant Nat.ltb => "a => b => a < b".
Extract Constant Nat.max => "a => b => a max b".
Extract Constant Nat.min => "a => b => a min b".
Extract Constant Nat.even => "n => !n.testBit(0)".
Extract Constant Nat.odd => "n => n.testBit(0)".

Extract Constant Nat.div => "a => b => if (b == 0) BigInt(0) else a / b".
Extract Constant Nat.modulo => "a => b => if (b == 0) a else a % b".
Extract Constant Nat.gcd => "a => b => a.gcd(b)".
Extract Constant Nat.div2 => "n => n >> 1".
Extract Constant Nat.sqrt => "n => BigInt(n.bigInteger.sqrt())".
Extract Constant Nat.log2 => "n => if (n <= 0) BigInt(0) else BigInt(n.bitLength - 1)".
