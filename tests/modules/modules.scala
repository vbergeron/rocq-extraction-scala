object Main {

sealed trait Nat
final case class O() extends Nat
final case class S(_1: Nat) extends Nat


sealed trait Prod[a, b]
final case class Pair[a, b](_1: a, _2: b) extends Prod[a, b]


def f(n: Nat): Nat =
  S(n)

def g(n: Nat): Nat =
  f(f(n))

def f0(n: Nat): Nat =
  g(f(n))

type T = Nat

val zero: T = O()

val f1: Prod[T, T] = Pair[T, T](zero, zero)

}

