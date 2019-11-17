package pl.msitko.terraform4s.codegen.classes

import org.scalatest.{Matchers, WordSpec}
import pl.msitko.terraform4s.codegen.DefaultCodegenContext
import pl.msitko.terraform4s.provider.ast.{HCLList, HCLNumber, HCLObject, HCLString}
import scala.meta._

class AnonymousClassCodegenSpec extends WordSpec with Matchers {
  "AnonymousClassCodegen.fromHCLObject" should {
    "work" in {
      val obj =
        HCLObject(List("some_string" -> HCLString, "some_number" -> HCLNumber, "some_list" -> HCLList(HCLString)))
      val res = AnonymousClassCodegen.fromHCLObject("Anonymous0", obj, new DefaultCodegenContext)

      val Source(List(expected)) =
        "final case class Anonymous0(some_string: String, some_number: Int, some_list: List[String])".parse[Source].get

      assert(res.structure === expected.structure)
    }
  }
}
