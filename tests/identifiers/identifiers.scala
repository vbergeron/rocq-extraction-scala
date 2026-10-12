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
  mapNat((_w: Nat) => S(_w))(mapNat((_v: Nat) => _v)(l))

def under(_x: Nat): Nat =
  { val _y: Nat = S(_x);
  _y match {
  case O() => O()
  case S(_z) => add(_z)(_y)
  } }

def localFix(_n: Nat): Nat =
  ({
     def _go: Nat => Nat => Nat =
         (_k: Nat) => (_acc: Nat) => 
         _k match {
         case O() => _acc
         case S(_p) => _go(_p)(S(_acc))
         }
     _go
   })(_n)(O())

def both(_x: Nat)(_x0: Nat): Nat =
  add(add(_x0)(_x))(_x0)

def _plus(n: Nat): Nat =
  S(n)

val usePlus: Nat => Nat = _plus

}

