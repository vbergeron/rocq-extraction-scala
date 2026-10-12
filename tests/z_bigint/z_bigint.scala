object Main {

val succ: BigInt => BigInt = p => p + 1

val sub: BigInt => BigInt => BigInt = a => b => (a - b) max BigInt(0)

val add: BigInt => BigInt => BigInt = a => b => a + b

val opp: BigInt => BigInt = a => -a

val mul: BigInt => BigInt => BigInt = a => b => a * b

val div: BigInt => BigInt => BigInt = a => b => if (b == 0) BigInt(0) else { val q = a / b; if (a % b != 0 && ((a < 0) != (b < 0))) q - 1 else q }

val modulo: BigInt => BigInt => BigInt = a => b => if (b == 0) a else { val r = a % b; if (r != 0 && ((r < 0) != (b < 0))) r + b else r }

def zops(a: BigInt)(b: BigInt): BigInt =
  add((mul(a)(b)))((opp((add((div(a)(b)))((modulo(a)(b)))))))

def zclass(z: BigInt): BigInt =
  ((((f0: Any => Any) => (fp: Any => Any) => (fn: Any => Any) => (z: BigInt) => if (z == 0) f0(()) else if (z > 0) fp(z) else fn(-z))
      (((_: Any) => ((p: BigInt) => p)(((p: BigInt) => p * 2)(BigInt(1)))))
      (((_: Any) => ((p: BigInt) => p)(BigInt(1))))
      (((_: Any) => ((p: BigInt) => -p)(BigInt(1))))
      (z))).asInstanceOf[BigInt]

def psize(p: BigInt): BigInt =
  ((((fI: Any => Any) => (fO: Any => Any) => (fH: Any => Any) => (p: BigInt) => if (p == 1) fH(()) else if (p.testBit(0)) fI(p >> 1) else fO(p >> 1))
      (((q: Any) => succ((psize((q).asInstanceOf[BigInt])))))
      (((q: Any) => succ((psize((q).asInstanceOf[BigInt])))))
      (((_: Any) => BigInt(1)))
      (p))).asInstanceOf[BigInt]

def nsub(a: BigInt)(b: BigInt): BigInt =
  sub(b)(a)

}

