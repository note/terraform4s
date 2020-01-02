package pl.msitko.terraform4s.codegen

import org.scalatest.{Matchers, WordSpec}
import pl.msitko.terraform4s.codegen.classes.InputParamsCodegen
import pl.msitko.terraform4s.provider.ast.{HCLAny, HCLBool, HCLList, HCLMap, HCLNumber, HCLObject, HCLSet, HCLString}

import scala.meta._

class InputParamsCodegenSpec extends WordSpec with Matchers {
  val syntheticName = "Anonymous0"

  val defaultCtx = new CodegenContext {
    override def getNextAnonymousClassName: String = ???

    override def registerAnonymousClass(className: String, classInput: HCLObject): Unit = ???

    override def getNameOf(classInput: HCLObject): Option[String] = Some(syntheticName)
  }

  "InputParamsCodegen.requiredParams" should {
    "work for primitive types" in {
      val in =
        List("some_string" -> HCLString, "some_number" -> HCLNumber, "some_bool" -> HCLBool, "some_any" -> HCLAny)
      val res = InputParamsCodegen.requiredParams(in, defaultCtx)

      val expected = List(
        param("some_string", "Val[String]"),
        param("some_number", "Val[Int]"),
        param("some_bool", "Val[Boolean]"),
        param("some_any", "Val[Any]")
      )

      assert(res.structure === expected.structure)
    }

    "work for collection types" in {
      val in = List("some_list" -> HCLList(HCLString), "some_set" -> HCLSet(HCLNumber), "some_map" -> HCLMap(HCLBool))

      val res = InputParamsCodegen.requiredParams(in, defaultCtx)

      val expected = List(
        param("some_list", "Val[List[String]]"),
        param("some_set", "Val[Set[Int]]"),
        param("some_map", "Val[Map[String, Boolean]]")
      )

      assert(res.structure === expected.structure)
    }

    "work for nested collection types" in {
      val in = List("some_nested" -> HCLList(HCLSet(HCLMap(HCLString))))

      val res = InputParamsCodegen.requiredParams(in, defaultCtx)

      val expected = List(
        param("some_nested", "Val[List[Set[Map[String, String]]]]")
      )

      assert(res.structure === expected.structure)
    }

    "work for HCL objects" in {
      val in = List(
        "some_object" -> HCLObject(
          List("some_string" -> HCLString, "some_bool" -> HCLBool, "some_list" -> HCLList(HCLSet(HCLNumber)))))

      val res = InputParamsCodegen.requiredParams(in, defaultCtx)

      val expected = List(
        param("some_object", (s"Val[$syntheticName]"))
      )

      assert(res.structure === expected.structure)
    }

  }
  "InputParamsCodegen.optionalParams" should {
    "work for primitive types" in {
      val in =
        List("some_string" -> HCLString, "some_number" -> HCLNumber, "some_bool" -> HCLBool, "some_any" -> HCLAny)
      val res = InputParamsCodegen.optionalParams(in, defaultCtx)

      val expected = List(
        param("some_string", "Option[Val[String]]"),
        param("some_number", "Option[Val[Int]]"),
        param("some_bool", "Option[Val[Boolean]]"),
        param("some_any", "Option[Val[Any]]")
      )

      assert(res.structure === expected.structure)
    }

    "work for collection types" in {
      val in = List("some_list" -> HCLList(HCLString), "some_set" -> HCLSet(HCLNumber), "some_map" -> HCLMap(HCLBool))

      val res = InputParamsCodegen.optionalParams(in, defaultCtx)

      val expected = List(
        param("some_list", "Option[Val[List[String]]]"),
        param("some_set", "Option[Val[Set[Int]]]"),
        param("some_map", "Option[Val[Map[String, Boolean]]]")
      )

      assert(res.structure === expected.structure)
    }

    "work for nested collection types" in {
      val in = List("some_nested" -> HCLList(HCLSet(HCLMap(HCLString))))

      val res = InputParamsCodegen.optionalParams(in, defaultCtx)

      val expected = List(
        param("some_nested", "Option[Val[List[Set[Map[String, String]]]]]")
      )

      assert(res.structure === expected.structure)
    }

    "work for HCL objects" in {
      val in = List(
        "some_object" -> HCLObject(
          List("some_string" -> HCLString, "some_bool" -> HCLBool, "some_list" -> HCLList(HCLSet(HCLNumber)))))

      val res = InputParamsCodegen.optionalParams(in, defaultCtx)

      val expected = List(
        param("some_object", s"Option[Val[$syntheticName]]")
      )

      assert(res.structure === expected.structure)
    }
  }

  def toTpe(s: String): Type =
    s.parse[Type].get

  def param(termName: String, tpe: String) =
    Term.Param(Nil, Term.Name(termName), Some(toTpe(tpe)), None)
}
