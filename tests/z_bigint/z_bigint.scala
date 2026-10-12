object Main {

val succ: BigInt => BigInt = p => p + 1

val opp: BigInt => BigInt = a => -a

def zclass(z: BigInt): BigInt =
  (((f0: Any => Any) => (fp: Any => Any) => (fn: Any => Any) => (z: BigInt) => if (z == 0) f0(()) else if (z > 0) fp(z) else fn(-z))
     ((_: Any) => ((p: BigInt) => p)(((p: BigInt) => p * 2)(BigInt(1))))
     ((_: Any) => ((p: BigInt) => p)(BigInt(1)))
     ((_: Any) => ((p: BigInt) => -p)(BigInt(1)))
     (z)).asInstanceOf[BigInt]

def psize(p: BigInt): BigInt =
  (((fI: Any => Any) => (fO: Any => Any) => (fH: Any => Any) => (p: BigInt) => if (p == 1) fH(()) else if (p.testBit(0)) fI(p >> 1) else fO(p >> 1))
     (((q: BigInt) => succ(psize(q))).asInstanceOf[Any => Any])
     (((q: BigInt) => succ(psize(q))).asInstanceOf[Any => Any])
     ((_: Any) => BigInt(1))
     (p)).asInstanceOf[BigInt]

def nclass(n: BigInt): BigInt =
  (((f0: Any => Any) => (fp: Any => Any) => (n: BigInt) => if (n == 0) f0(()) else fp(n))
     ((_: Any) => ((p: BigInt) => p)(BigInt(1)))
     ((_: Any) => BigInt(0))
     (n)).asInstanceOf[BigInt]

def zneg2(z: BigInt): BigInt =
  opp(opp(z))

}

