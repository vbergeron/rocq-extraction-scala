object Main {

sealed trait Nat
final case class O() extends Nat
final case class S(_1: Nat) extends Nat


def add(n: Nat)(m: Nat): Nat =
  n match {
  case O() => m
  case S(p) => S(add(p)(m))
  }

sealed trait Tree[a]
final case class Node[a](_1: a, _2: Forest[a]) extends Tree[a]

sealed trait Forest[a]
final case class Empty[a]() extends Forest[a]
final case class Grove[a](_1: Tree[a], _2: Forest[a]) extends Forest[a]


def tsize[t1](t: Tree[t1]): Nat =
  t match {
  case Node(_, f) => S(fsize[t1](f))
  }

def fsize[t1](f: Forest[t1]): Nat =
  f match {
  case Empty() => O()
  case Grove(t, f0) => add(tsize[t1](t))(fsize[t1](f0))
  }

def tmap[t1, t2](g: t1 => t2)(t: Tree[t1]): Tree[t2] =
  t match {
  case Node(a, f) => Node[t2](g(a), fmap[t1, t2](g)(f))
  }

def fmap[t1, t2](g: t1 => t2)(f: Forest[t1]): Forest[t2] =
  f match {
  case Empty() => Empty[t2]()
  case Grove(t, f0) => Grove[t2](tmap[t1, t2](g)(t), fmap[t1, t2](g)(f0))
  }

}

