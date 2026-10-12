File "./type_defs.v", line 34, characters 0-52:
Warning: The following axiom must be realized in the extracted code:
 opaque.
 [extraction-axiom-to-realize,extraction,default]
object Main {

sealed trait Nat
final case class O() extends Nat
final case class S(_1: Nat) extends Nat


sealed trait Prod[a, b]
final case class Pair[a, b](_1: a, _2: b) extends Prod[a, b]


type NatAlias = Nat

val three: NatAlias = S(S(S(O())))

type PairOf[a] = Prod[a, a]

def dup[t1](x: t1): Prod[t1, t1] =
  Pair[t1, t1](x, x)

type MachineInt = Int

def idInt(i: MachineInt): MachineInt =
  i

type Vec[a] = Vector[a]

def idVec[t1](v: Vec[t1]): Vec[t1] =
  v

type Opaque = Any /* AXIOM TO BE REALIZED */

def idOpaque(o: Opaque): Opaque =
  o

}

