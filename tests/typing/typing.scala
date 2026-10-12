File "./typing.v", line 90, characters 0-112:
Warning: The following axiom must be realized in the extracted code:
 ax.
 [extraction-axiom-to-realize,extraction,default]
object Main {

val __ : Unit = ()

sealed trait Bool
case object True extends Bool
case object False extends Bool


sealed trait Nat
case object O extends Nat
final case class S(_1: Nat) extends Nat


sealed trait Prod[+a, +b]
final case class Pair[a, b](_1: a, _2: b) extends Prod[a, b]


sealed trait List[+a]
case object Nil extends List[Nothing]
final case class Cons[a](_1: a, _2: List[a]) extends List[a]


type Sig[a] = a
// singleton inductive, whose constructor was exist

def add(n: Nat)(m: Nat): Nat =
  n match {
  case O => m
  case S(p) => S(add(p)(m))
  }

def letPoly(n: Nat): Prod[Nat, Bool] =
  { def id[t1]: t1 => t1 = (x: t1) => x;
  Pair[Nat, Bool](id[Nat](n), id[Bool](True)) }

def sumTo(n: Nat): Nat =
  ({
     def go: Nat => Nat => Nat =
         (k: Nat) => (acc: Nat) => 
         k match {
         case O => acc
         case S(p) => go(p)(add(k)(acc))
         }
     go
   })(n)(O)

def dep(b: Bool): Any =
  b match {
  case True => O
  case False => True
  }

val useDep: Nat = dep(True).asInstanceOf[Nat]

def applyPoly(f: Unit => Any => Any): Prod[Nat, Bool] =
  Pair[Nat, Bool](f(__)(O).asInstanceOf[Nat], f(__)(True).asInstanceOf[
Bool])

val usePoly: Prod[Nat, Bool] = applyPoly((_: Unit) => (x: Any) => x)

def mapNat(f: Nat => Nat)(l: List[Nat]): List[Nat] =
  l match {
  case Nil => Nil
  case Cons(a, t) => Cons[Nat](f(a), mapNat(f)(t))
  }

def succAll(l: List[Nat]): List[Nat] =
  mapNat((x: Nat) => S(x))(l)

def addAll(n: Nat)(l: List[Nat]): List[Nat] =
  mapNat(add(n))(l)

def caseFun(b: Bool)(n: Nat): Nat =
  b match {
  case True => S(n)
  case False => n
  }

def predSafe(n: Nat): Nat =
  n match {
  case O => O
  case S(p) => p
  }

val usePred: Nat = predSafe(S(O))

val ax: Nat => Nat =
  throw new RuntimeException("AXIOM TO BE REALIZED (typing.ax)")

val useAx: Nat = ax(O)

def sigEx(n: Nat): Nat =
  n

val sigVal: Nat => Nat = sigEx

final case class MkPair[a, b](pfst: a, psnd: b)
type Pair0[a, b] = MkPair[a, b]

def swapPair[t1, t2](p: Pair0[t1, t2]): Pair0[t2, t1] =
  MkPair[t2, t1](p.psnd, p.pfst)

def primes(n_ : Nat): Nat =
  { val m_ : Nat = S(n_); add(m_)(m_) }

}

