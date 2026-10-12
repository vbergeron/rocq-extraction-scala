object Main {

def add: Int => Int => Int = ((n: Int) => (m: Int) => n + m)

def mul: Int => Int => Int = ((n: Int) => (m: Int) => n * m)

def fact(n: Int): Int =
  ((((fO: Any => Any) => (fS: Any => Any) => (n: Int) =>
      if (n == 0) fO(()) else fS(n - 1))
      (((_: Any) => ((n: Int) => n + 1)(0)))
      (((p: Any) => mul(n)((fact((p).asInstanceOf[Int])))))
      (n))).asInstanceOf[Int]

}

