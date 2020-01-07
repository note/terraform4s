package pl.msitko.terraform4s.codegen

import pl.msitko.terraform4s.codegen.common.UnitSpec

import scala.meta._

class CodegenSpec extends UnitSpec {
  "toTermSelect" should {
    "handle 0 items" in {
      val in = List.empty[String]

      val res = Codegen.toTermSelect(in)

      assert(res === None)
    }

    "handle 1 item" in {
      val in = List("a")

      val res = Codegen.toTermSelect(in)

      assert(res.get.structure === "a".parse[Term].get.structure)
    }

    "handle 2 items" in {
      val in = List("a", "b")

      val res = Codegen.toTermSelect(in)

      assert(res.get.structure === "a.b".parse[Term].get.structure)
    }

    "handle more than 2 items" in {
      val in = List("a", "b", "c")

      val res = Codegen.toTermSelect(in)

      assert(res.get.structure === "a.b.c".parse[Term].get.structure)
    }
  }
}
