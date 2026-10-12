object Main {

sealed trait Bool
case object True extends Bool
case object False extends Bool


sealed trait Mylist[+a]
case object MyNil extends Mylist[Nothing]
final case class MyCons[a](_1: a, _2: Mylist[a]) extends Mylist[a]


sealed trait Pred[a]
final case class PNone[a]() extends Pred[a]
final case class PSome[a](_1: a => Bool) extends Pred[a]


sealed trait Cont[+a]
case object CNone extends Cont[Nothing]
final case class CSome[a](_1: (a => Bool) => Bool) extends Cont[a]


sealed trait Mixed[+a, b]
final case class MEmpty[a, b]() extends Mixed[a, b]
final case class MLeft[a, b](_1: a) extends Mixed[a, b]
final case class MRight[a, b](_1: b => Bool) extends Mixed[a, b]


sealed trait Rose[+a]
final case class MkRose[a](_1: a, _2: Mylist[Rose[a]]) extends Rose[a]


def pnone[t1]: Pred[t1] = PNone[t1]()

def cnone[t1]: Cont[t1] = CNone

def mempty[t1, t2]: Mixed[t1, t2] = MEmpty[t1, t2]()

def isNil[t1](l: Mylist[t1]): Bool =
  l match {
  case MyNil => True
  case MyCons(_, _) => False
  }

def single[t1](a: t1): Rose[t1] =
  MkRose[t1](a, MyNil)

}

