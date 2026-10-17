import com.example.verified.{Sorting, Stats}
import utest.*

/** Checks the extracted code against Scala's own collections. */
object VerifiedTests extends TestSuite {
  val random = new scala.util.Random(42)
  val samples: Seq[List[BigInt]] =
    Seq(Nil, List(BigInt(7))) ++
      Seq.fill(200)(List.fill(random.nextInt(30))(BigInt(random.nextInt(20))))

  val tests = Tests {
    test("sort") {
      samples.foreach(l => assert(Sorting.sort(l) == l.sorted))
    }
    test("minimum") {
      samples.foreach(l => assert(Stats.minimum(l) == l.minOption))
    }
    test("distinct") {
      samples.foreach(l => assert(Stats.distinct(l) == l.distinct.sorted))
    }
    test("bigNumbers") {
      val l = List(BigInt("123456789012345678901234567890"), BigInt(0), BigInt(2).pow(100))
      assert(Sorting.sort(l) == l.sorted)
    }
  }
}
