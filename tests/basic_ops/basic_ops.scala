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

val boolOps: Boolean => Boolean => Boolean =
  (a: Boolean) => (b: Boolean) => 
    xorb(implb(negb(a))(orb(a)(b)))(andb(a)(b))

def pairList[t1]: Tuple2[t1, t1] => List[t1] =
  (p: Tuple2[t1, t1]) => 
    ::(fst[t1, t1](p), ::(snd[t1, t1](p), Nil))

val appOne: List[Boolean] => List[Boolean] =
  (l: List[Boolean]) => 
    app[Boolean](l)(::(true, Nil))

val negAll: List[Boolean] => List[Boolean] =
  (l: List[Boolean]) => 
    map[Boolean, Boolean](negb)(::(true, l))

val negOpt: Boolean => Option[Boolean] =
  (b: Boolean) => 
    option_map[Boolean, Boolean](negb)(Some(b))

}

