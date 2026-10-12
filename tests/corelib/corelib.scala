object Main {

sealed trait Bool
final case class True() extends Bool
final case class False() extends Bool


sealed trait Nat
final case class O() extends Nat
final case class S(_1: Nat) extends Nat


sealed trait Prod[a, b]
final case class Pair[a, b](_1: a, _2: b) extends Prod[a, b]


def fst[t1, t2](p: Prod[t1, t2]): t1 =
  p match {
  case Pair(x, _) => x
  }

def snd[t1, t2](p: Prod[t1, t2]): t2 =
  p match {
  case Pair(_, y) => y
  }

sealed trait List[a]
final case class Nil[a]() extends List[a]
final case class Cons[a](_1: a, _2: List[a]) extends List[a]


sealed trait Comparison
final case class Eq() extends Comparison
final case class Lt() extends Comparison
final case class Gt() extends Comparison


def compOpp(r: Comparison): Comparison =
  r match {
  case Eq() => Eq()
  case Lt() => Gt()
  case Gt() => Lt()
  }

sealed trait Uint
final case class Nil0() extends Uint
final case class D0(_1: Uint) extends Uint
final case class D1(_1: Uint) extends Uint
final case class D2(_1: Uint) extends Uint
final case class D3(_1: Uint) extends Uint
final case class D4(_1: Uint) extends Uint
final case class D5(_1: Uint) extends Uint
final case class D6(_1: Uint) extends Uint
final case class D7(_1: Uint) extends Uint
final case class D8(_1: Uint) extends Uint
final case class D9(_1: Uint) extends Uint


sealed trait Uint0
final case class Nil1() extends Uint0
final case class D10(_1: Uint0) extends Uint0
final case class D11(_1: Uint0) extends Uint0
final case class D12(_1: Uint0) extends Uint0
final case class D13(_1: Uint0) extends Uint0
final case class D14(_1: Uint0) extends Uint0
final case class D15(_1: Uint0) extends Uint0
final case class D16(_1: Uint0) extends Uint0
final case class D17(_1: Uint0) extends Uint0
final case class D18(_1: Uint0) extends Uint0
final case class D19(_1: Uint0) extends Uint0
final case class Da(_1: Uint0) extends Uint0
final case class Db(_1: Uint0) extends Uint0
final case class Dc(_1: Uint0) extends Uint0
final case class Dd(_1: Uint0) extends Uint0
final case class De(_1: Uint0) extends Uint0
final case class Df(_1: Uint0) extends Uint0


sealed trait Uint1
final case class UIntDecimal(_1: Uint) extends Uint1
final case class UIntHexadecimal(_1: Uint0) extends Uint1


def pred(n: Nat): Nat =
  n match {
  case O() => n
  case S(u) => u
  }

def add(n: Nat)(m: Nat): Nat =
  n match {
  case O() => m
  case S(p) => S(add(p)(m))
  }

def mul(n: Nat)(m: Nat): Nat =
  n match {
  case O() => O()
  case S(p) => add(m)(mul(p)(m))
  }

def sub(n: Nat)(m: Nat): Nat =
  n match {
  case O() => n
  case S(k) => m match {
               case O() => n
               case S(l) => sub(k)(l)
               }
  }

def eqb(n: Nat)(m: Nat): Bool =
  n match {
  case O() => m match {
              case O() => True()
              case S(_) => False()
              }
  case S(n_) => m match {
                case O() => False()
                case S(m_) => eqb(n_)(m_)
                }
  }

def leb(n: Nat)(m: Nat): Bool =
  n match {
  case O() => True()
  case S(n_) => m match {
                case O() => False()
                case S(m_) => leb(n_)(m_)
                }
  }

def pow(n: Nat)(m: Nat): Nat =
  m match {
  case O() => S(O())
  case S(m0) => mul(n)(pow(n)(m0))
  }

def tail_add(n: Nat)(m: Nat): Nat =
  n match {
  case O() => m
  case S(n0) => tail_add(n0)(S(m))
  }

def tail_addmul(r: Nat)(n: Nat)(m: Nat): Nat =
  n match {
  case O() => r
  case S(n0) => tail_addmul(tail_add(m)(r))(n0)(m)
  }

def tail_mul(n: Nat)(m: Nat): Nat =
  tail_addmul(O())(n)(m)

def of_uint_acc(d: Uint)(acc: Nat): Nat =
  d match {
  case Nil0() => acc
  case D0(d0) =>
    of_uint_acc(d0)(tail_mul(S(S(S(S(S(S(S(S(S(S(O())))))))))))(acc))
  case D1(d0) =>
    of_uint_acc(d0)(S(tail_mul(S(S(S(S(S(S(S(S(S(S(O())))))))))))(acc)))
  case D2(d0) =>
    of_uint_acc(d0)(S(S(tail_mul(S(S(S(S(S(S(S(S(S(S(O())))))))))))(acc))))
  case D3(d0) =>
    of_uint_acc(d0)(S(S(S(tail_mul(S(S(S(S(S(S(S(S(S(S(O())))))))))))(acc)))))
  case D4(d0) =>
    of_uint_acc(d0)(S(S(S(S(tail_mul(S(S(S(S(S(S(S(S(S(S(O())))))))))))(acc))))))
  case D5(d0) =>
    of_uint_acc(d0)(S(S(S(S(S(tail_mul(S(S(S(S(S(S(S(S(S(S(O())))))))))))(acc)))))))
  case D6(d0) =>
    of_uint_acc(d0)(S(S(S(S(S(S(tail_mul(S(S(S(S(S(S(S(S(S(S(O())))))))))))(acc))))))))
  case D7(d0) =>
    of_uint_acc(d0)(S(S(S(S(S(S(S(tail_mul(S(S(S(S(S(S(S(S(S(S(O())))))))))))(acc)))))))))
  case D8(d0) =>
    of_uint_acc(d0)(S(S(S(S(S(S(S(S(tail_mul(S(S(S(S(S(S(S(S(S(S(O())))))))))))(acc))))))))))
  case D9(d0) =>
    of_uint_acc(d0)(S(S(S(S(S(S(S(S(S(tail_mul(S(S(S(S(S(S(S(S(S(S(O())))))))))))(acc)))))))))))
  }

def of_uint(d: Uint): Nat =
  of_uint_acc(d)(O())

def of_hex_uint_acc(d: Uint0)(acc: Nat): Nat =
  d match {
  case Nil1() => acc
  case D10(d0) =>
    of_hex_uint_acc(d0)(tail_mul(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(O())))))))))))))))))(acc))
  case D11(d0) =>
    of_hex_uint_acc(d0)(S(tail_mul(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(O())))))))))))))))))(acc)))
  case D12(d0) =>
    of_hex_uint_acc(d0)(S(S(tail_mul(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(O())))))))))))))))))(acc))))
  case D13(d0) =>
    of_hex_uint_acc(d0)(S(S(S(tail_mul(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(O())))))))))))))))))(acc)))))
  case D14(d0) =>
    of_hex_uint_acc(d0)(S(S(S(S(tail_mul(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(O())))))))))))))))))(acc))))))
  case D15(d0) =>
    of_hex_uint_acc(d0)(S(S(S(S(S(tail_mul(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(O())))))))))))))))))(acc)))))))
  case D16(d0) =>
    of_hex_uint_acc(d0)(S(S(S(S(S(S(tail_mul(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(O())))))))))))))))))(acc))))))))
  case D17(d0) =>
    of_hex_uint_acc(d0)(S(S(S(S(S(S(S(tail_mul(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(O())))))))))))))))))(acc)))))))))
  case D18(d0) =>
    of_hex_uint_acc(d0)(S(S(S(S(S(S(S(S(tail_mul(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(O())))))))))))))))))(acc))))))))))
  case D19(d0) =>
    of_hex_uint_acc(d0)(S(S(S(S(S(S(S(S(S(tail_mul(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(O())))))))))))))))))(acc)))))))))))
  case Da(d0) =>
    of_hex_uint_acc(d0)(S(S(S(S(S(S(S(S(S(S(tail_mul(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(O())))))))))))))))))(acc))))))))))))
  case Db(d0) =>
    of_hex_uint_acc(d0)(S(S(S(S(S(S(S(S(S(S(S(tail_mul(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(O())))))))))))))))))(acc)))))))))))))
  case Dc(d0) =>
    of_hex_uint_acc(d0)(S(S(S(S(S(S(S(S(S(S(S(S(tail_mul(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(O())))))))))))))))))(acc))))))))))))))
  case Dd(d0) =>
    of_hex_uint_acc(d0)(S(S(S(S(S(S(S(S(S(S(S(S(S(tail_mul(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(O())))))))))))))))))(acc)))))))))))))))
  case De(d0) =>
    of_hex_uint_acc(d0)(S(S(S(S(S(S(S(S(S(S(S(S(S(S(tail_mul(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(O())))))))))))))))))(acc))))))))))))))))
  case Df(d0) =>
    of_hex_uint_acc(d0)(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(tail_mul(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(S(O())))))))))))))))))(acc)))))))))))))))))
  }

def of_hex_uint(d: Uint0): Nat =
  of_hex_uint_acc(d)(O())

def of_num_uint(d: Uint1): Nat =
  d match {
  case UIntDecimal(d0) => of_uint(d0)
  case UIntHexadecimal(d0) => of_hex_uint(d0)
  }

def divmod(x: Nat)(y: Nat)(q: Nat)(u: Nat): Prod[Nat, Nat] =
  x match {
  case O() => Pair[Nat, Nat](q, u)
  case S(x_) =>
    u match {
    case O() => divmod(x_)(y)(S(q))(y)
    case S(u_) => divmod(x_)(y)(q)(u_)
    }
  }

def div(x: Nat)(y: Nat): Nat =
  y match {
  case O() => y
  case S(y_) => fst[Nat, Nat](divmod(x)(y_)(O())(y_))
  }

def modulo(x: Nat)(y: Nat): Nat =
  y match {
  case O() => x
  case S(y_) => sub(y_)(snd[Nat, Nat](divmod(x)(y_)(O())(y_)))
  }

def gcd(a: Nat)(b: Nat): Nat =
  a match {
  case O() => b
  case S(a_) => gcd(modulo(b)(S(a_)))(S(a_))
  }

def sqrt_iter(k: Nat)(p: Nat)(q: Nat)(r: Nat): Nat =
  k match {
  case O() => p
  case S(k_) =>
    r match {
    case O() => sqrt_iter(k_)(S(p))(S(S(q)))(S(S(q)))
    case S(r_) => sqrt_iter(k_)(p)(q)(r_)
    }
  }

def sqrt(n: Nat): Nat =
  sqrt_iter(n)(O())(O())(O())

def log2_iter(k: Nat)(p: Nat)(q: Nat)(r: Nat): Nat =
  k match {
  case O() => p
  case S(k_) =>
    r match {
    case O() => log2_iter(k_)(S(p))(S(q))(q)
    case S(r_) => log2_iter(k_)(p)(S(q))(r_)
    }
  }

def log2(n: Nat): Nat =
  log2_iter(pred(n))(O())(S(O()))(O())

def iter[t1](n: Nat)(f: t1 => t1)(x: t1): t1 =
  n match {
  case O() => x
  case S(n0) => f(iter[t1](n0)(f)(x))
  }

sealed trait Positive
final case class XI(_1: Positive) extends Positive
final case class XO(_1: Positive) extends Positive
final case class XH() extends Positive


sealed trait N
final case class N0() extends N
final case class Npos(_1: Positive) extends N


sealed trait Z
final case class Z0() extends Z
final case class Zpos(_1: Positive) extends Z
final case class Zneg(_1: Positive) extends Z


def succ(x: Positive): Positive =
  x match {
  case XI(p) => XO(succ(p))
  case XO(p) => XI(p)
  case XH() => XO(XH())
  }

def add0(x: Positive)(y: Positive): Positive =
  x match {
  case XI(p) =>
    y match {
    case XI(q) => XO(add_carry(p)(q))
    case XO(q) => XI(add0(p)(q))
    case XH() => XO(succ(p))
    }
  case XO(p) =>
    y match {
    case XI(q) => XI(add0(p)(q))
    case XO(q) => XO(add0(p)(q))
    case XH() => XI(p)
    }
  case XH() =>
    y match {
    case XI(q) => XO(succ(q))
    case XO(q) => XI(q)
    case XH() => XO(XH())
    }
  }

def add_carry(x: Positive)(y: Positive): Positive =
  x match {
  case XI(p) =>
    y match {
    case XI(q) => XI(add_carry(p)(q))
    case XO(q) => XO(add_carry(p)(q))
    case XH() => XI(succ(p))
    }
  case XO(p) =>
    y match {
    case XI(q) => XO(add_carry(p)(q))
    case XO(q) => XI(add0(p)(q))
    case XH() => XO(succ(p))
    }
  case XH() =>
    y match {
    case XI(q) => XI(succ(q))
    case XO(q) => XO(succ(q))
    case XH() => XI(XH())
    }
  }

def pred_double(x: Positive): Positive =
  x match {
  case XI(p) => XI(XO(p))
  case XO(p) => XI(pred_double(p))
  case XH() => XH()
  }

sealed trait Mask
final case class IsNul() extends Mask
final case class IsPos(_1: Positive) extends Mask
final case class IsNeg() extends Mask


def succ_double_mask(x: Mask): Mask =
  x match {
  case IsNul() => IsPos(XH())
  case IsPos(p) => IsPos(XI(p))
  case IsNeg() => IsNeg()
  }

def double_mask(x: Mask): Mask =
  x match {
  case IsPos(p) => IsPos(XO(p))
  case x0 => x0
  }

def double_pred_mask(x: Positive): Mask =
  x match {
  case XI(p) => IsPos(XO(XO(p)))
  case XO(p) => IsPos(XO(pred_double(p)))
  case XH() => IsNul()
  }

def sub_mask(x: Positive)(y: Positive): Mask =
  x match {
  case XI(p) =>
    y match {
    case XI(q) => double_mask(sub_mask(p)(q))
    case XO(q) => succ_double_mask(sub_mask(p)(q))
    case XH() => IsPos(XO(p))
    }
  case XO(p) =>
    y match {
    case XI(q) => succ_double_mask(sub_mask_carry(p)(q))
    case XO(q) => double_mask(sub_mask(p)(q))
    case XH() => IsPos(pred_double(p))
    }
  case XH() => y match {
               case XH() => IsNul()
               case _ => IsNeg()
               }
  }

def sub_mask_carry(x: Positive)(y: Positive): Mask =
  x match {
  case XI(p) =>
    y match {
    case XI(q) => succ_double_mask(sub_mask_carry(p)(q))
    case XO(q) => double_mask(sub_mask(p)(q))
    case XH() => IsPos(pred_double(p))
    }
  case XO(p) =>
    y match {
    case XI(q) => double_mask(sub_mask_carry(p)(q))
    case XO(q) => succ_double_mask(sub_mask_carry(p)(q))
    case XH() => double_pred_mask(p)
    }
  case XH() => IsNeg()
  }

def sub0(x: Positive)(y: Positive): Positive =
  sub_mask(x)(y) match {
  case IsPos(z) => z
  case _ => XH()
  }

def mul0(x: Positive)(y: Positive): Positive =
  x match {
  case XI(p) => add0(y)(XO(mul0(p)(y)))
  case XO(p) => XO(mul0(p)(y))
  case XH() => y
  }

def iter0[t1](f: t1 => t1)(x: t1)(n: Positive): t1 =
  n match {
  case XI(n_) => f(iter0[t1](f)(iter0[t1](f)(x)(n_))(n_))
  case XO(n_) => iter0[t1](f)(iter0[t1](f)(x)(n_))(n_)
  case XH() => f(x)
  }

def div2(p: Positive): Positive =
  p match {
  case XI(p0) => p0
  case XO(p0) => p0
  case XH() => XH()
  }

def compare_cont(r: Comparison)(x: Positive)(y: Positive): Comparison =
  x match {
  case XI(p) =>
    y match {
    case XI(q) => compare_cont(r)(p)(q)
    case XO(q) => compare_cont(Gt())(p)(q)
    case XH() => Gt()
    }
  case XO(p) =>
    y match {
    case XI(q) => compare_cont(Lt())(p)(q)
    case XO(q) => compare_cont(r)(p)(q)
    case XH() => Gt()
    }
  case XH() => y match {
               case XH() => r
               case _ => Lt()
               }
  }

val compare: Positive => Positive => Comparison = compare_cont(Eq())

def leb0(x: Positive)(y: Positive): Bool =
  compare(x)(y) match {
  case Gt() => False()
  case _ => True()
  }

def sqrtrem_step(f: Positive => Positive)(g: Positive => Positive)(p: 
  Prod[Positive, Mask]): Prod[Positive, Mask] =
  p match {
  case Pair(s, y) =>
    y match {
    case IsPos(r) =>
      { val s_ : Positive = XI(XO(s));
      { val r_ : Positive = g(f(r));
      leb0(s_)(r_) match {
      case True() => Pair[Positive, Mask](XI(s), sub_mask(r_)(s_))
      case False() => Pair[Positive, Mask](XO(s), IsPos(r_))
      } } }
    case _ => Pair[Positive, Mask](XO(s), sub_mask(g(f(XH())))(XO(XO(XH()))))
    }
  }

def sqrtrem(p: Positive): Prod[Positive, Mask] =
  p match {
  case XI(p0) =>
    p0 match {
    case XI(p1) =>
      sqrtrem_step((x: Positive) => XI(x))((x: Positive) => XI(x))(sqrtrem(p1))
    case XO(p1) =>
      sqrtrem_step((x: Positive) => XO(x))((x: Positive) => XI(x))(sqrtrem(p1))
    case XH() => Pair[Positive, Mask](XH(), IsPos(XO(XH())))
    }
  case XO(p0) =>
    p0 match {
    case XI(p1) =>
      sqrtrem_step((x: Positive) => XI(x))((x: Positive) => XO(x))(sqrtrem(p1))
    case XO(p1) =>
      sqrtrem_step((x: Positive) => XO(x))((x: Positive) => XO(x))(sqrtrem(p1))
    case XH() => Pair[Positive, Mask](XH(), IsPos(XH()))
    }
  case XH() => Pair[Positive, Mask](XH(), IsNul())
  }

def iter_op[t1](op: t1 => t1 => t1)(p: Positive)(a: t1): t1 =
  p match {
  case XI(p0) => op(a)(iter_op[t1](op)(p0)(op(a)(a)))
  case XO(p0) => iter_op[t1](op)(p0)(op(a)(a))
  case XH() => a
  }

def to_nat(x: Positive): Nat =
  iter_op[Nat](add)(x)(S(O()))

def of_succ_nat(n: Nat): Positive =
  n match {
  case O() => XH()
  case S(x) => succ(of_succ_nat(x))
  }

def succ_double(x: N): N =
  x match {
  case N0() => Npos(XH())
  case Npos(p) => Npos(XI(p))
  }

def double(n: N): N =
  n match {
  case N0() => N0()
  case Npos(p) => Npos(XO(p))
  }

def sub1(n: N)(m: N): N =
  n match {
  case N0() => N0()
  case Npos(n_) =>
    m match {
    case N0() => n
    case Npos(m_) =>
      sub_mask(n_)(m_) match {
      case IsPos(p) => Npos(p)
      case _ => N0()
      }
    }
  }

def compare0(n: N)(m: N): Comparison =
  n match {
  case N0() => m match {
               case N0() => Eq()
               case Npos(_) => Lt()
               }
  case Npos(n_) =>
    m match {
    case N0() => Gt()
    case Npos(m_) => compare(n_)(m_)
    }
  }

def leb1(x: N)(y: N): Bool =
  compare0(x)(y) match {
  case Gt() => False()
  case _ => True()
  }

def pos_div_eucl(a: Positive)(b: N): Prod[N, N] =
  a match {
  case XI(a_) =>
    pos_div_eucl(a_)(b) match {
    case Pair(q, r) =>
      { val r_ : N = succ_double(r);
      leb1(b)(r_) match {
      case True() => Pair[N, N](succ_double(q), sub1(r_)(b))
      case False() => Pair[N, N](double(q), r_)
      } }
    }
  case XO(a_) =>
    pos_div_eucl(a_)(b) match {
    case Pair(q, r) =>
      { val r_ : N = double(r);
      leb1(b)(r_) match {
      case True() => Pair[N, N](succ_double(q), sub1(r_)(b))
      case False() => Pair[N, N](double(q), r_)
      } }
    }
  case XH() =>
    b match {
    case N0() => Pair[N, N](N0(), Npos(XH()))
    case Npos(p) =>
      p match {
      case XH() => Pair[N, N](Npos(XH()), N0())
      case _ => Pair[N, N](N0(), Npos(XH()))
      }
    }
  }

def double0(x: Z): Z =
  x match {
  case Z0() => Z0()
  case Zpos(p) => Zpos(XO(p))
  case Zneg(p) => Zneg(XO(p))
  }

def succ_double0(x: Z): Z =
  x match {
  case Z0() => Zpos(XH())
  case Zpos(p) => Zpos(XI(p))
  case Zneg(p) => Zneg(pred_double(p))
  }

def pred_double0(x: Z): Z =
  x match {
  case Z0() => Zneg(XH())
  case Zpos(p) => Zpos(pred_double(p))
  case Zneg(p) => Zneg(XI(p))
  }

def pos_sub(x: Positive)(y: Positive): Z =
  x match {
  case XI(p) =>
    y match {
    case XI(q) => double0(pos_sub(p)(q))
    case XO(q) => succ_double0(pos_sub(p)(q))
    case XH() => Zpos(XO(p))
    }
  case XO(p) =>
    y match {
    case XI(q) => pred_double0(pos_sub(p)(q))
    case XO(q) => double0(pos_sub(p)(q))
    case XH() => Zpos(pred_double(p))
    }
  case XH() =>
    y match {
    case XI(q) => Zneg(XO(q))
    case XO(q) => Zneg(pred_double(q))
    case XH() => Z0()
    }
  }

def add1(x: Z)(y: Z): Z =
  x match {
  case Z0() => y
  case Zpos(x_) =>
    y match {
    case Z0() => x
    case Zpos(y_) => Zpos(add0(x_)(y_))
    case Zneg(y_) => pos_sub(x_)(y_)
    }
  case Zneg(x_) =>
    y match {
    case Z0() => x
    case Zpos(y_) => pos_sub(y_)(x_)
    case Zneg(y_) => Zneg(add0(x_)(y_))
    }
  }

def opp(x: Z): Z =
  x match {
  case Z0() => Z0()
  case Zpos(x0) => Zneg(x0)
  case Zneg(x0) => Zpos(x0)
  }

def sub2(m: Z)(n: Z): Z =
  add1(m)(opp(n))

def mul1(x: Z)(y: Z): Z =
  x match {
  case Z0() => Z0()
  case Zpos(x_) =>
    y match {
    case Z0() => Z0()
    case Zpos(y_) => Zpos(mul0(x_)(y_))
    case Zneg(y_) => Zneg(mul0(x_)(y_))
    }
  case Zneg(x_) =>
    y match {
    case Z0() => Z0()
    case Zpos(y_) => Zneg(mul0(x_)(y_))
    case Zneg(y_) => Zpos(mul0(x_)(y_))
    }
  }

def pow_pos(z: Z): Positive => Z =
  iter0[Z](mul1(z))(Zpos(XH()))

def pow0(x: Z)(y: Z): Z =
  y match {
  case Z0() => Zpos(XH())
  case Zpos(p) => pow_pos(x)(p)
  case Zneg(_) => Z0()
  }

def compare1(x: Z)(y: Z): Comparison =
  x match {
  case Z0() =>
    y match {
    case Z0() => Eq()
    case Zpos(_) => Lt()
    case Zneg(_) => Gt()
    }
  case Zpos(x_) => y match {
                   case Zpos(y_) => compare(x_)(y_)
                   case _ => Gt()
                   }
  case Zneg(x_) =>
    y match {
    case Zneg(y_) => compOpp(compare(x_)(y_))
    case _ => Lt()
    }
  }

def leb2(x: Z)(y: Z): Bool =
  compare1(x)(y) match {
  case Gt() => False()
  case _ => True()
  }

def ltb(x: Z)(y: Z): Bool =
  compare1(x)(y) match {
  case Lt() => True()
  case _ => False()
  }

def to_nat0(z: Z): Nat =
  z match {
  case Zpos(p) => to_nat(p)
  case _ => O()
  }

def of_nat(n: Nat): Z =
  n match {
  case O() => Z0()
  case S(n0) => Zpos(of_succ_nat(n0))
  }

def of_N(n: N): Z =
  n match {
  case N0() => Z0()
  case Npos(p) => Zpos(p)
  }

def pos_div_eucl0(a: Positive)(b: Z): Prod[Z, Z] =
  a match {
  case XI(a_) =>
    pos_div_eucl0(a_)(b) match {
    case Pair(q, r) =>
      { val r_ : Z = add1(mul1(Zpos(XO(XH())))(r))(Zpos(XH()));
      ltb(r_)(b) match {
      case True() => Pair[Z, Z](mul1(Zpos(XO(XH())))(q), r_)
      case False() =>
        Pair[Z, Z](add1(mul1(Zpos(XO(XH())))(q))(Zpos(XH())), sub2(r_)(b))
      } }
    }
  case XO(a_) =>
    pos_div_eucl0(a_)(b) match {
    case Pair(q, r) =>
      { val r_ : Z = mul1(Zpos(XO(XH())))(r);
      ltb(r_)(b) match {
      case True() => Pair[Z, Z](mul1(Zpos(XO(XH())))(q), r_)
      case False() =>
        Pair[Z, Z](add1(mul1(Zpos(XO(XH())))(q))(Zpos(XH())), sub2(r_)(b))
      } }
    }
  case XH() =>
    leb2(Zpos(XO(XH())))(b) match {
    case True() => Pair[Z, Z](Z0(), Zpos(XH()))
    case False() => Pair[Z, Z](Zpos(XH()), Z0())
    }
  }

def div_eucl(a: Z)(b: Z): Prod[Z, Z] =
  a match {
  case Z0() => Pair[Z, Z](Z0(), Z0())
  case Zpos(a_) =>
    b match {
    case Z0() => Pair[Z, Z](Z0(), a)
    case Zpos(_) => pos_div_eucl0(a_)(b)
    case Zneg(b_) =>
      pos_div_eucl0(a_)(Zpos(b_)) match {
      case Pair(q, r) =>
        r match {
        case Z0() => Pair[Z, Z](opp(q), Z0())
        case _ => Pair[Z, Z](opp(add1(q)(Zpos(XH()))), add1(b)(r))
        }
      }
    }
  case Zneg(a_) =>
    b match {
    case Z0() => Pair[Z, Z](Z0(), a)
    case Zpos(_) =>
      pos_div_eucl0(a_)(b) match {
      case Pair(q, r) =>
        r match {
        case Z0() => Pair[Z, Z](opp(q), Z0())
        case _ => Pair[Z, Z](opp(add1(q)(Zpos(XH()))), sub2(b)(r))
        }
      }
    case Zneg(b_) =>
      pos_div_eucl0(a_)(Zpos(b_)) match {
      case Pair(q, r) => Pair[Z, Z](q, opp(r))
      }
    }
  }

def div0(a: Z)(b: Z): Z =
  div_eucl(a)(b) match {
  case Pair(q, _) => q
  }

def modulo0(a: Z)(b: Z): Z =
  div_eucl(a)(b) match {
  case Pair(_, r) => r
  }

def quotrem(a: Z)(b: Z): Prod[Z, Z] =
  a match {
  case Z0() => Pair[Z, Z](Z0(), Z0())
  case Zpos(a0) =>
    b match {
    case Z0() => Pair[Z, Z](Z0(), a)
    case Zpos(b0) =>
      pos_div_eucl(a0)(Npos(b0)) match {
      case Pair(q, r) => Pair[Z, Z](of_N(q), of_N(r))
      }
    case Zneg(b0) =>
      pos_div_eucl(a0)(Npos(b0)) match {
      case Pair(q, r) => Pair[Z, Z](opp(of_N(q)), of_N(r))
      }
    }
  case Zneg(a0) =>
    b match {
    case Z0() => Pair[Z, Z](Z0(), a)
    case Zpos(b0) =>
      pos_div_eucl(a0)(Npos(b0)) match {
      case Pair(q, r) => Pair[Z, Z](opp(of_N(q)), opp(of_N(r)))
      }
    case Zneg(b0) =>
      pos_div_eucl(a0)(Npos(b0)) match {
      case Pair(q, r) => Pair[Z, Z](of_N(q), opp(of_N(r)))
      }
    }
  }

def quot(a: Z)(b: Z): Z =
  fst[Z, Z](quotrem(a)(b))

def rem(a: Z)(b: Z): Z =
  snd[Z, Z](quotrem(a)(b))

def map[t1, t2](f: t1 => t2)(l: List[t1]): List[t2] =
  l match {
  case Nil() => Nil[t2]()
  case Cons(a, l0) => Cons[t2](f(a), map[t1, t2](f)(l0))
  }

}

