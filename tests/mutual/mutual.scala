object Main {

sealed trait Nat
final case class O() extends Nat
final case class S(_1: Nat) extends Nat


def isEven(n: Nat): Boolean =
  n match {
  case O() => true
  case S(p) => isOdd(p)
  }

def isOdd(n: Nat): Boolean =
  n match {
  case O() => false
  case S(p) => isEven(p)
  }

def myNot(b: Boolean): Boolean =
  b match {
  case true => false
  case false => true
  }

}

