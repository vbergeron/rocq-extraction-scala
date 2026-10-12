object Main {

final class RocqLazy[+T](thunk: => T) {
  lazy val force: T = thunk
}
object RocqLazy {
  def apply[T](thunk: => T): RocqLazy[T] = new RocqLazy(thunk)
}

sealed trait Bool
final case class True() extends Bool
final case class False() extends Bool


sealed trait Nat
final case class O() extends Nat
final case class S(_1: Nat) extends Nat


sealed trait Prod[a, b]
final case class Pair[a, b](_1: a, _2: b) extends Prod[a, b]


def add(n: Nat)(m: Nat): Nat =
  n match {
  case O() => m
  case S(p) => S(add(p)(m))
  }

def sumTo(n: Nat): Nat =
  {
    def go(k: Nat)(acc: Nat): Nat =
      k match {
      case O() => acc
      case S(p) => go(p)(add(k)(acc))
      }
    go(n)(O())
  }

def addTo(n: Nat): Prod[Nat, Nat => Nat] =
  Pair[Nat, Nat => Nat](n, {
                             def go(k: Nat)(acc: Nat): Nat =
                               k match {
                               case O() => acc
                               case S(p) => S(go(p)(acc))
                               }
                             go(n)
                           })

val isEven: Nat => Bool =
  {
    def even(k: Nat): Bool =
      k match {
      case O() => True()
      case S(p) => odd(p)
      }
    def odd(k: Nat): Bool =
      k match {
      case O() => False()
      case S(p) => even(p)
      }
    even
  }

sealed trait __Stream[a]
final case class Scons[a](_1: a, _2: Stream[a]) extends __Stream[a]

type Stream[a] = RocqLazy[__Stream[a]]

def nats(k: Nat): Stream[Nat] =
  RocqLazy(Scons[Nat](k, nats(S(k))))

def repeat(n: Nat): Prod[Stream[Nat], Nat] =
  Pair[Stream[Nat], Nat]({
                           def ones: Stream[Nat] =
                             RocqLazy(Scons[Nat](n, ones))
                           ones
                         }, n)

}

