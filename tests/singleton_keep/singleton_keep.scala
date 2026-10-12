object Main {

sealed trait Nat
final case class O() extends Nat
final case class S(_1: Nat) extends Nat


sealed trait Sig[a]
final case class Exist[a](_1: a) extends Sig[a]


sealed trait Box[a]
final case class MkBox[a](_1: a) extends Box[a]


def unbox[t1](b: Box[t1]): t1 =
  b match {
  case MkBox(a) => a
  }

def mapBox[t1, t2](f: t1 => t2)(b: Box[t1]): Box[t2] =
  MkBox[t2](f(unbox[t1](b)))

def sigEx(n: Nat): Sig[Nat] =
  Exist[Nat](n)

def sigVal(n: Nat): Nat =
  sigEx(n) match {
  case Exist(a) => a
  }

}

