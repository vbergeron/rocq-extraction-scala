object Main {

sealed trait Nat
final case class O() extends Nat
final case class S(_1: Nat) extends Nat


sealed trait List[a]
final case class Nil[a]() extends List[a]
final case class Cons[a](_1: a, _2: List[a]) extends List[a]


def add(n: Nat)(m: Nat): Nat =
  n match {
  case O() => m
  case S(p) => S(add(p)(m))
  }

def mapNat(f: Nat => Nat)(l: List[Nat]): List[Nat] =
  l match {
  case Nil() => Nil[Nat]()
  case Cons(a, t) => Cons[Nat](f(a), mapNat(f)(t))
  }

def lam(l: List[Nat]): List[Nat] =
  mapNat((w: Nat) => S(w))(mapNat((v: Nat) => v)(l))

def under(x: Nat): Nat =
  { val y: Nat = S(x); y match {
                       case O() => O()
                       case S(z) => add(z)(y)
                       } }

def localFix(n: Nat): Nat =
  {
    def go(k: Nat)(acc: Nat): Nat =
      k match {
      case O() => acc
      case S(p) => go(p)(S(acc))
      }
    go(n)(O())
  }

def both(x: Nat)(x0: Nat): Nat =
  add(add(x0)(x))(x0)

def shadow(n: Nat): Nat =
  { val n0: Nat = S(n); add(n0)(n) }

def plus(n: Nat): Nat =
  n

def plus0(n: Nat): Nat =
  S(n)

def usePlus(n: Nat): Nat =
  plus(plus0(n))

}

