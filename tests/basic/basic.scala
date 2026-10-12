object Main {

sealed trait Nat
case object O extends Nat
final case class S(_1: Nat) extends Nat


def addNat(n: Nat)(m: Nat): Nat =
  n match {
  case O => m
  case S(p) => S(addNat(p)(m))
  }

def mulNat(n: Nat)(m: Nat): Nat =
  n match {
  case O => O
  case S(p) => addNat(m)(mulNat(p)(m))
  }

}

