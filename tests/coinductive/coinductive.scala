object Main {

final class RocqLazy[+T](thunk: => T) {
  lazy val force: T = thunk
}
object RocqLazy {
  def apply[T](thunk: => T): RocqLazy[T] = new RocqLazy(thunk)
}

sealed trait Nat
final case class O() extends Nat
final case class S(_1: Nat) extends Nat


sealed trait List[a]
final case class Nil[a]() extends List[a]
final case class Cons[a](_1: a, _2: List[a]) extends List[a]


sealed trait MyList[a]
final case class MyNil[a]() extends MyList[a]
final case class MyCons[a](_1: a, _2: MyList[a]) extends MyList[a]


sealed trait __Stream[a]
final case class Scons[a](_1: a, _2: Stream[a]) extends __Stream[a]

type Stream[a] = RocqLazy[__Stream[a]]

def const_stream(n: Nat): Stream[Nat] =
  RocqLazy(Scons[Nat](n, const_stream(n)))

def mapStream[t1, t2](f: t1 => t2)(s: Stream[t1]): Stream[t2] =
  s.force match {
  case Scons(a, t) => RocqLazy(Scons[t2](f(a), mapStream[t1, t2](f)(t)))
  }

def take(n: Nat)(s: Stream[Nat]): List[Nat] =
  n match {
  case O() => Nil[Nat]()
  case S(p) => s.force match {
               case Scons(h, t) => Cons[Nat](h, take(p)(t))
               }
  }

def takeList[t1](n: Nat)(s: Stream[t1]): MyList[t1] =
  n match {
  case O() => MyNil[t1]()
  case S(p) =>
    s.force match {
    case Scons(a, t) => MyCons[t1](a, takeList[t1](p)(t))
    }
  }

}

