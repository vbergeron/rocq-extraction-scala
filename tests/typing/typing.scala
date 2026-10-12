File "./typing.v", line 90, characters 0-112:
Warning: The following axiom must be realized in the extracted code:
 ax.
 [extraction-axiom-to-realize,extraction,default]
object Main {

val __ : Unit = ()

sealed trait Bool
final case class True() extends Bool
final case class False() extends Bool


sealed trait Nat
final case class O() extends Nat
final case class S(_1: Nat) extends Nat


sealed trait Prod[a, b]
final case class Pair[a, b](_1: a, _2: b) extends Prod[a, b]


sealed trait List[a]
final case class Nil[a]() extends List[a]
final case class Cons[a](_1: a, _2: List[a]) extends List[a]


type Sig[a] = a
// singleton inductive, whose constructor was exist

def add: Nat => Nat => Nat =
    (n: Nat) => (m: Nat) => 
    n match {
    case O() => m
    case S(p) => S(add(p)(m))
    }

val letPoly: Nat => Prod[Nat, Bool] =
  (n: Nat) => 
    { def id[t1]: t1 => t1 = (x: t1) => x;
    Pair[Nat, Bool](id[Nat](n), id[Bool](True())) }

val sumTo: Nat => Nat =
  (n: Nat) => 
    ({
       def go: Nat => Nat => Nat =
           (k: Nat) => (acc: Nat) => 
           k match {
           case O() => acc
           case S(p) => go(p)(add(k)(acc))
           }
       go
     })(n)(O())

val dep: Bool => Any =
  (b: Bool) => 
    b match {
    case True() => O()
    case False() => True()
    }

val useDep: Nat = dep(True()).asInstanceOf[Nat]

val applyPoly: (Unit => Any => Any) => Prod[Nat, Bool] =
  (f: Unit => Any => Any) => 
    Pair[Nat, Bool](f(__)(O()).asInstanceOf[Nat], f(__)(True()).asInstanceOf[
  Bool])

val usePoly: Prod[Nat, Bool] = applyPoly((_: Unit) => (x: Any) => x)

def mapNat: (Nat => Nat) => List[Nat] => List[Nat] =
    (f: Nat => Nat) => (l: List[Nat]) => 
    l match {
    case Nil() => Nil[Nat]()
    case Cons(a, t) => Cons[Nat](f(a), mapNat(f)(t))
    }

val succAll: List[Nat] => List[Nat] =
  (l: List[Nat]) => 
    mapNat((x: Nat) => S(x))(l)

val addAll: Nat => List[Nat] => List[Nat] =
  (n: Nat) => (l: List[Nat]) => 
    mapNat(add(n))(l)

val caseFun: Bool => Nat => Nat =
  (b: Bool) => (n: Nat) => 
    b match {
    case True() => S(n)
    case False() => n
    }

val predSafe: Nat => Nat =
  (n: Nat) => 
    n match {
    case O() => O()
    case S(p) => p
    }

val usePred: Nat = predSafe(S(O()))

val ax: Nat => Nat =
  throw new RuntimeException("AXIOM TO BE REALIZED (typing.ax)")

val useAx: Nat = ax(O())

val sigEx: Nat => Nat = (n: Nat) => 
    n

val sigVal: Nat => Nat = sigEx

final case class MkPair[a, b](pfst: a, psnd: b)
type Pair0[a, b] = MkPair[a, b]

def swapPair[t1, t2]: Pair0[t1, t2] => Pair0[t2, t1] =
  (p: Pair0[t1, t2]) => 
    MkPair[t2, t1](p.psnd, p.pfst)

val primes: Nat => Nat =
  (n_ : Nat) => 
    { val m_ : Nat = S(n_); add(m_)(m_) }

}

