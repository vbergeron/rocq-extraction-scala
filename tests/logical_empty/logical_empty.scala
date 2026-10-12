object Main {

sealed trait Nat
final case class O() extends Nat
final case class S(_1: Nat) extends Nat


/* even : logical inductive
   with constructors : even0 evenSS */
type Void = Unit // empty inductive


def absurd[t1](__x1: Void): t1 =
  throw new RuntimeException("absurd case")

val fromVoid: Void => Nat = absurd[Nat]

}

