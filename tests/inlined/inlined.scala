object Main {

sealed trait Nat
final case class O() extends Nat
final case class S(_1: Nat) extends Nat


def map[t1, t2]: (t1 => t2) => List[t1] => List[t2] = f => l => l.map(f)

def negTwice(b: Boolean): Boolean =
  ((b: Boolean) => !b)(((b: Boolean) => !b)(b))

def negAll(l: List[Boolean]): List[Boolean] =
  map[Boolean, Boolean](((b: Boolean) => !b))(l)

def myId[t1]: t1 => t1 = x => x

def useId(b: Boolean): Tuple2[Boolean, List[Boolean]] =
  (myId[Boolean](b), myId[List[Boolean]](Nil))

val answer: Nat = S(S(O()))

val useAnswer: Nat = S(answer)

}

