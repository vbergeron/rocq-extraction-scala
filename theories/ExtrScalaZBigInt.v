(** Extraction of [positive], [N] and [Z] to Scala's arbitrary-precision
    [BigInt], the Scala analogue of [ExtrOCamlZBigInt]: [Require] it next
    to [ScalaExtraction] to opt in. Each operation below is replaced by
    its [BigInt] counterpart, with the exact same result as the Rocq
    function on every input: [Z.div]/[Z.modulo] round towards minus
    infinity (unlike [BigInt]'s own [/] and [%], which give [Z.quot] and
    [Z.rem]), a division by zero gives 0 and leaves the dividend as
    remainder, [Pos.sub] stops at 1 and [N.sub] at 0. Re-exports
    [ExtrScalaBasic], since the comparisons return a Scala [Boolean].

    [BigInt(0)] rather than a bare [0]: an [Int] literal only becomes a
    [BigInt] where Scala expects one, and a match branch is an [Any]
    position. *)

From Corelib Require Import BinNums.PosDef BinNums.NatDef BinNums.IntDef.
From ScalaExtraction Require Import ScalaExtraction.
From ScalaExtraction Require Export ExtrScalaBasic.

(** Constructors in declaration order: [xI], [xO], [xH]; [N0], [Npos];
    [Z0], [Zpos], [Zneg]. *)

Extract Inductive positive => "BigInt"
  [ "((p: BigInt) => p * 2 + 1)" "((p: BigInt) => p * 2)" "BigInt(1)" ]
  "((fI: Any => Any) => (fO: Any => Any) => (fH: Any => Any) => (p: BigInt) => if (p == 1) fH(()) else if (p.testBit(0)) fI(p >> 1) else fO(p >> 1))".

Extract Inductive N => "BigInt"
  [ "BigInt(0)" "((p: BigInt) => p)" ]
  "((f0: Any => Any) => (fp: Any => Any) => (n: BigInt) => if (n == 0) f0(()) else fp(n))".

Extract Inductive Z => "BigInt"
  [ "BigInt(0)" "((p: BigInt) => p)" "((p: BigInt) => -p)" ]
  "((f0: Any => Any) => (fp: Any => Any) => (fn: Any => Any) => (z: BigInt) => if (z == 0) f0(()) else if (z > 0) fp(z) else fn(-z))".

Extract Constant Pos.succ => "p => p + 1".
Extract Constant Pos.add => "a => b => a + b".
Extract Constant Pos.mul => "a => b => a * b".
Extract Constant Pos.sub => "a => b => (a - b) max BigInt(1)".
Extract Constant Pos.eqb => "a => b => a == b".
Extract Constant Pos.leb => "a => b => a <= b".

Extract Constant N.sub => "a => b => (a - b) max BigInt(0)".
Extract Constant N.leb => "a => b => a <= b".

Extract Constant Z.add => "a => b => a + b".
Extract Constant Z.opp => "a => -a".
Extract Constant Z.sub => "a => b => a - b".
Extract Constant Z.mul => "a => b => a * b".
Extract Constant Z.pow => "a => b => if (b < 0) BigInt(0) else a.pow(b.toInt)".
Extract Constant Z.eqb => "a => b => a == b".
Extract Constant Z.leb => "a => b => a <= b".
Extract Constant Z.ltb => "a => b => a < b".
Extract Constant Z.max => "a => b => a max b".
Extract Constant Z.min => "a => b => a min b".
Extract Constant Z.even => "z => !z.testBit(0)".
Extract Constant Z.double => "z => z * 2".
Extract Constant Z.div2 => "z => z >> 1".
Extract Constant Z.div => "a => b => if (b == 0) BigInt(0) else { val q = a / b; if (a % b != 0 && ((a < 0) != (b < 0))) q - 1 else q }".
Extract Constant Z.modulo => "a => b => if (b == 0) a else { val r = a % b; if (r != 0 && ((r < 0) != (b < 0))) r + b else r }".
Extract Constant Z.quot => "a => b => if (b == 0) BigInt(0) else a / b".
Extract Constant Z.rem => "a => b => if (b == 0) a else a % b".
Extract Constant Z.shiftl => "a => n => if (n >= 0) a << n.toInt else a >> (-n).toInt".
Extract Constant Z.shiftr => "a => n => if (n >= 0) a >> n.toInt else a << (-n).toInt".
