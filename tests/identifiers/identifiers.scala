object Main {

sealed trait Nat
final case class O() extends Nat
final case class S(_1: Nat) extends Nat


sealed trait List[a]
final case class Nil[a]() extends List[a]
final case class Cons[a](_1: a, _2: List[a]) extends List[a]


def add(n: Nat)(m: Nat): Nat =
  n match {
  case O() => m
  case S(p) => S(add(p)(m))
  }

def mapNat(f: Nat => Nat)(l: List[Nat]): List[Nat] =
  l match {
  case Nil() => Nil[Nat]()
  case Cons(a, t) => Cons[Nat](f(a), mapNat(f)(t))
  }

def lam(l: List[Nat]): List[Nat] =
  mapNat((w_ : Nat) => S(w_))(mapNat((v_ : Nat) => v_)(l))

def under(x_ : Nat): Nat =
  { val y_ : Nat = S(x_);
  y_ match {
  case O() => O()
  case S(z_) => add(z_)(y_)
  } }

def localFix(n_ : Nat): Nat =
  ({
     def go: Nat => Nat => Nat =
         (k_ : Nat) => (acc_ : Nat) => 
         k_ match {
         case O() => acc_
         case S(p_) => go(p_)(S(acc_))
         }
     go
   })(n_)(O())

}

