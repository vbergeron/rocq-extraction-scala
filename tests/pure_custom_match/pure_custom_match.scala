import language.experimental.pureFunctions

object Main {

val mul: BigInt -> BigInt -> BigInt = a => b => a * b

def fact(n: BigInt): BigInt =
  (((fO: Any => Any) => (fS: Any => Any) => (n: BigInt) => if (n == 0) fO(()) else fS(n - 1))
     ((_: Any) => ((n: BigInt) => n + 1)(BigInt(0)))
     (((p: BigInt) => mul(n)(fact(p))).asInstanceOf[Any -> Any])
     (n)).asInstanceOf[BigInt]

}

