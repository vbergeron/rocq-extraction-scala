object Main {

sealed trait Nat
final case class O() extends Nat
final case class S(_1: Nat) extends Nat


def val0(n: Nat): Nat =
  n

val type0: Nat => Nat = val0

def toString0(this0: Nat): Nat =
  S(this0)

def given0[t1](yield0: t1): t1 =
  yield0

sealed trait Class
final case class Trait() extends Class
final case class Object() extends Class


def equals0(c: Class): Class =
  c match {
  case Trait() => Object()
  case Object() => Trait()
  }

sealed trait Lazy[var0]
final case class Thunk[var0]() extends Lazy[var0]
final case class Forced[var0](_1: var0) extends Lazy[var0]


def force[t1](default: t1)(l: Lazy[t1]): t1 =
  l match {
  case Thunk() => default
  case Forced(while0) => while0
  }

}

