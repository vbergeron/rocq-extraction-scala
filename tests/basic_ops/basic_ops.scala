object Main {

val andb: Boolean => Boolean => Boolean = a => b => a && b

val orb: Boolean => Boolean => Boolean = a => b => a || b

val implb: Boolean => Boolean => Boolean = a => b => !a || b

val xorb: Boolean => Boolean => Boolean = a => b => a != b

val negb: Boolean => Boolean = b => !b

def option_map[t1, t2]: (t1 => t2) => Option[t1] => Option[t2] = f => o => o.map(f)

def fst[t1, t2]: Tuple2[t1, t2] => t1 = p => p._1

def snd[t1, t2]: Tuple2[t1, t2] => t2 = p => p._2

def app[t1]: List[t1] => List[t1] => List[t1] = l => m => l ++ m

def map[t1, t2]: (t1 => t2) => List[t1] => List[t2] = f => l => l.map(f)

def boolOps(a: Boolean)(b: Boolean): Boolean =
  xorb((implb((negb(a)))((orb(a)(b)))))((andb(a)(b)))

def pairList[t1](p: Tuple2[t1, t1]): List[t1] =
  ::(fst(p), ::(snd(p), Nil))

def appOne(l: List[Boolean]): List[Boolean] =
  app(l)(::(true, Nil))

def negAll(l: List[Boolean]): List[Boolean] =
  map(negb)(::(true, l))

def negOpt(b: Boolean): Option[Boolean] =
  option_map(negb)(Some(b))

}

