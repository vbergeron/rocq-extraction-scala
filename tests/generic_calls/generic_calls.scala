object Main {

val negb: Boolean => Boolean = b => !b

def option_map[t1, t2]: (t1 => t2) => Option[t1] => Option[t2] = f => o => o.map(f)

def fst[t1, t2]: Tuple2[t1, t2] => t1 = p => p._1

def app[t1]: List[t1] => List[t1] => List[t1] = l => m => l ++ m

def map[t1, t2]: (t1 => t2) => List[t1] => List[t2] = f => l => l.map(f)

val negFst: Tuple2[Boolean, Boolean] => Boolean =
  (p: Tuple2[Boolean, Boolean]) => 
    negb((fst(p)))

def nestedFst[t1, t2]: Tuple2[t1, t2] => Tuple2[t2, t1] => t1 =
  (p: Tuple2[t1, t2]) => (q: Tuple2[t2, t1]) => 
    fst((fst((p, q))))

val appMap: List[Boolean] => List[Boolean] =
  (l: List[Boolean]) => 
    app((map(negb)(l)))(l)

def noneMap[t1]: (t1 => t1) => Option[t1] =
  (f: t1 => t1) => 
    option_map(f)((None).asInstanceOf[Option[t1]])

}

