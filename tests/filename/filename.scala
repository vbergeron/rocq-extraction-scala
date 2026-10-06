object Answer_file {

sealed trait Nat
final case class O() extends Nat
final case class S(_1: Nat) extends Nat


val answer: Nat = S(S(O()))

}
