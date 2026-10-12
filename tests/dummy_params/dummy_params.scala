object Main {

val __ : Unit = ()

sealed trait Nat
final case class O() extends Nat
final case class S(_1: Nat) extends Nat


def ignoreFirst(__x1: Nat)(m: Nat): Nat =
  m

val useIgnore: Nat = ignoreFirst(O())(S(O()))

def constFun(__x1: Nat): Nat =
  O()

def withProof(n: Nat): Nat =
  n

val useProof: Nat = withProof(O())

def withType(n: Nat): Nat =
  n

val useType: Nat = withType(O())

def applyProof(f: Nat => Unit => Nat): Nat =
  f(O())(__)

val useApplyProof: Nat = applyProof((x: Nat) => (_: Unit) => withProof(x))

}

