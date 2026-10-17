package rocqextraction

import utest.*

object RocqModuleTests extends TestSuite {
  val tests = Tests {
    test("defaultLogicalName") {
      assert(RocqModule.defaultLogicalName(Seq("app")) == "App")
      assert(RocqModule.defaultLogicalName(Seq("foo", "mill-plugin")) == "Mill_plugin")
      assert(RocqModule.defaultLogicalName(Seq("2d")) == "M2d")
      assert(RocqModule.defaultLogicalName(Nil) == "Main")
    }
  }
}
