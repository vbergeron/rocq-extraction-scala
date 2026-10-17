import com.example.verified.{Sorting, Stats}

/** Sorts the numbers given as arguments with the extracted sort. */
@main def run(args: String*): Unit = {
  val numbers = (if (args.nonEmpty) args else Seq("5", "3", "8", "1", "3")).map(BigInt(_)).toList
  println(s"input:    ${numbers.mkString(" ")}")
  println(s"sorted:   ${Sorting.sort(numbers).mkString(" ")}")
  println(s"distinct: ${Stats.distinct(numbers).mkString(" ")}")
  println(s"minimum:  ${Stats.minimum(numbers).fold("none")(_.toString)}")
}
