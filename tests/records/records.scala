object Main {

sealed trait Nat
case object O extends Nat
final case class S(_1: Nat) extends Nat


final case class MkPoint(px: Nat, py: Nat)
type Point = MkPoint

def swap(p: Point): Point =
  MkPoint(p.py, p.px)

type Box[a] = a
// singleton inductive, whose constructor was MkBox

def mapBox[t1, t2](f: t1 => t2): t1 => t2 =
  f

}

