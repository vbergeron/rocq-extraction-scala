object Main {

sealed trait Nat
final case class O() extends Nat
final case class S(_1: Nat) extends Nat


val chA: Char = 'a'

val chQuote: Char = '\''

val chBackslash: Char = '\\'

val chNewline: Char = '\n'

val hi: String = "hi"

val escapes: String = "\"'\\\n"

val empty: String = ""

def mkChar(b: Boolean): Char =
  ((b0: Boolean, b1: Boolean, b2: Boolean, b3: Boolean, b4: Boolean, b5: Boolean, b6: Boolean, b7: Boolean) => List(b0, b1, b2, b3, b4, b5, b6, b7).zipWithIndex.map((b, i) => if (b) 1 << i else 0).sum.toChar)(b, false, false, false, false, true, true, false)

def prepend(c: Char)(s: String): String =
  ((c: Char, s: String) => c.toString + s)(c, s)

def strLength(s: String): Nat =
  (((fE: Any => Any) => (fS: Any => Any) => (s: String) =>
      if (s.isEmpty) fE(()) else fS(s.head).asInstanceOf[Any => Any](s.tail))
     ((_: Any) => O())
     (((_: Any) => (t: String) => S(strLength(t))).asInstanceOf[Any => Any => Any])
     (s)).asInstanceOf[Nat]

def headOr(c: Char)(s: String): Char =
  (((fE: Any => Any) => (fS: Any => Any) => (s: String) =>
      if (s.isEmpty) fE(()) else fS(s.head).asInstanceOf[Any => Any](s.tail))
     ((_: Any) => c)
     (((_c: Char) => (_: Any) => _c).asInstanceOf[Any => Any => Any])
     (s)).asInstanceOf[Char]

}

