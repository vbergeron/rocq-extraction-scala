object Main {

sealed trait Nat
final case class O() extends Nat
final case class S(_1: Nat) extends Nat


def pred2(n: Nat): Nat =
  n match {
  case O() => O()
  case S(n0) => n0 match {
                case O() => O()
                case S(m) => m
                }
  }

def both(p: Tuple2[Boolean, Boolean]): Boolean =
  p match {
  case (b, b0) => b match {
                  case true => b0
                  case false => false
                  }
  }

def second[t1](d: t1)(l: List[t1]): t1 =
  l match {
  case Nil => d
  case ::(_, l0) => l0 match {
                    case Nil => d
                    case ::(x, _) => x
                    }
  }

def firstOfPair[t1, t2](d: t1)(l: List[Tuple2[t1, t2]]): t1 =
  l match {
  case Nil => d
  case ::(p, _) => p match {
                   case (a, _) => a
                   }
  }

}

