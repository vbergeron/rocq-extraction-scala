object Main {

val add: BigInt => BigInt => BigInt = a => b => a + b

val mul: BigInt => BigInt => BigInt = a => b => a * b

val sub: BigInt => BigInt => BigInt = a => b => (a - b) max BigInt(0)

val leb: BigInt => BigInt => Boolean = a => b => a <= b

val div: BigInt => BigInt => BigInt = a => b => if (b == 0) BigInt(0) else a / b

val modulo: BigInt => BigInt => BigInt = a => b => if (b == 0) a else a % b

def fact(n: BigInt): BigInt =
  ((((fO: Any => Any) => (fS: Any => Any) => (n: BigInt) => if (n == 0) fO(()) else fS(n - 1))
      (((_: Any) => ((n: BigInt) => n + 1)(BigInt(0))))
      (((p: Any) => mul(n)((fact((p).asInstanceOf[BigInt])))))
      (n))).asInstanceOf[BigInt]

def ops(a: BigInt)(b: BigInt): Boolean =
  leb((sub(a)(b)))((add((div(a)(b)))((modulo(a)(b)))))

}

