object Main {

sealed trait Nat
final case class O() extends Nat
final case class S(_1: Nat) extends Nat


def addNat(n: Nat)(m: Nat): Nat =
  n match {
  case O() => m
  case S(p) => S(addNat(p)(m))
  }

sealed trait Tree[a]
final case class Leaf[a]() extends Tree[a]
final case class Node[a](_1: Tree[a], _2: a, _3: Tree[a]) extends Tree[a]


def treeSum(t: Tree[Nat]): Nat =
  t match {
  case Leaf() => O()
  case Node(l, x, r) => addNat(treeSum(l))(addNat(x)(treeSum(r)))
  }

def treeMap[t1, t2](f: t1 => t2)(t: Tree[t1]): Tree[t2] =
  t match {
  case Leaf() => Leaf[t2]()
  case Node(l, x, r) =>
    Node[t2](treeMap[t1, t2](f)(l), f(x), treeMap[t1, t2](f)(r))
  }

sealed trait MyOption[a]
final case class MyNone[a]() extends MyOption[a]
final case class MySome[a](_1: a) extends MyOption[a]


def optionMap[t1, t2](f: t1 => t2)(o: MyOption[t1]): MyOption[t2] =
  o match {
  case MyNone() => MyNone[t2]()
  case MySome(a) => MySome[t2](f(a))
  }

}

