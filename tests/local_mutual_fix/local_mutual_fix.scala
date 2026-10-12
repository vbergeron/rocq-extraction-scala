object Main {

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


def parity(n: Nat): Prod[Bool, Bool] =
  Pair[Bool, Bool]({
                     def ev(k: Nat): Bool =
                       k match {
                       case O() => True()
                       case S(p) => od(p)
                       }
                     def od(k: Nat): Bool =
                       k match {
                       case O() => False()
                       case S(p) => ev(p)
                       }
                     ev(n)
                   }, {
                        def ev(k: Nat): Bool =
                          k match {
                          case O() => True()
                          case S(p) => od(p)
                          }
                        def od(k: Nat): Bool =
                          k match {
                          case O() => False()
                          case S(p) => ev(p)
                          }
                        od(n)
                      })

def evenLength[t1]: List[t1] => Bool =
  {
    def ev(l: List[t1]): Bool =
      l match {
      case Nil() => True()
      case Cons(_, t) => od(t)
      }
    def od(l: List[t1]): Bool =
      l match {
      case Nil() => False()
      case Cons(_, t) => ev(t)
      }
    ev
  }

}

