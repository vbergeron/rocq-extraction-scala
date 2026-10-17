package rocqextraction.core

import java.nio.file.Files
import utest._

object FilesTests extends TestSuite {
  import TestUtil._

  val tests = Tests {
    test("sync") {
      val tmp = tempDir()
      val out = tempDir()
      val a = write(tmp.resolve("A.scala"), "package com.ex\n\nobject A\n")
      val b = write(tmp.resolve("B.scala"), "object B\n")
      val stale = write(out.resolve("old/Old.scala"), "object Old\n")
      val kept = write(out.resolve("README"), "not Scala")
      val files = Sync(Seq(a, b), out)
      val targetA = out.resolve("com/ex/A.scala")
      assert(files == Seq(out.resolve("B.scala"), targetA))
      assert(read(targetA) == "package com.ex\n\nobject A\n")
      assert(!Files.exists(stale), !Files.exists(stale.getParent), Files.exists(kept))

      // Unchanged files are not rewritten
      val time = java.nio.file.attribute.FileTime.fromMillis(0)
      Files.setLastModifiedTime(targetA, time)
      Sync(Seq(a, b), out)
      assert(Files.getLastModifiedTime(targetA) == time)
    }
  }
}
