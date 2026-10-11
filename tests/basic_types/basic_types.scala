object Main {

def map[t1, t2]: (t1 => t2) => List[t1] => List[t2] =
    (f: t1 => t2) => (l: List[t1]) => 
    (l match {
     case Nil => Nil
     case ::(a, t) => ::(f(a), map[t1, t2](f)(t))
     }).asInstanceOf[List[t2]]

def headOpt[t1]: List[t1] => Option[t1] =
  (l: List[t1]) => 
    (l match {
     case Nil => None
     case ::(a, _) => Some(a)
     }).asInstanceOf[Option[t1]]

def swap[t1, t2]: Tuple2[t1, t2] => Tuple2[t2, t1] =
  (p: Tuple2[t1, t2]) => 
    (p match {
     case (a, b) => (b, a)
     }).asInstanceOf[Tuple2[t2, t1]]

def swapSum[t1, t2]: Either[t1, t2] => Either[t2, t1] =
  (s: Either[t1, t2]) => 
    (s match {
     case Left(a) => Right(a)
     case Right(b) => Left(b)
     }).asInstanceOf[Either[t2, t1]]

def isEmpty[t1]: List[t1] => Boolean =
  (l: List[t1]) => 
    (l match {
     case Nil => true
     case ::(_, _) => false
     }).asInstanceOf[Boolean]

val ttv: Unit = (()).asInstanceOf[Unit]

}

