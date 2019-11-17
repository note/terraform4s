package pl.msitko.terraform4s.codegen

import org.scalatest.{Matchers, WordSpec}
import pl.msitko.terraform4s.provider.ast.{HCLAny, HCLBool, HCLList, HCLMap, HCLNumber, HCLSet, HCLString}

import scala.meta._

class InputParamsCodegenSpec extends WordSpec with Matchers {
  "InputParamsCodegen.requiredParams" should {
    "work for primitive types" in {
      val in =
        List("some_string" -> HCLString, "some_number" -> HCLNumber, "some_bool" -> HCLBool, "some_any" -> HCLAny)
      val res = InputParamsCodegen.requiredParams(in)

      val expected = List(
        Term.Param(Nil, Term.Name("some_string"), Some(toTpe("OutStringVal")), None),
        Term.Param(Nil, Term.Name("some_number"), Some(toTpe("OutVal[Int]")), None),
        Term.Param(Nil, Term.Name("some_bool"), Some(toTpe("OutVal[Boolean]")), None),
        Term.Param(Nil, Term.Name("some_any"), Some(toTpe("OutVal[Any]")), None)
      )

      assert(res.structure === expected.structure)
    }

    "work for collection types" in {
      val in = List("some_list" -> HCLList(HCLString), "some_set" -> HCLSet(HCLNumber), "some_map" -> HCLMap(HCLBool))

      val res = InputParamsCodegen.requiredParams(in)

      val expected = List(
        Term.Param(Nil, Term.Name("some_list"), Some(toTpe("OutVal[List[String]]")), None),
        Term.Param(Nil, Term.Name("some_set"), Some(toTpe("OutVal[Set[Int]]")), None),
        Term.Param(Nil, Term.Name("some_map"), Some(toTpe("OutVal[Map[String, Boolean]]")), None)
      )

      assert(res.structure === expected.structure)
    }

    "work for nested collection types" in {
      val in = List("some_nested" -> HCLList(HCLSet(HCLMap(HCLString))))

      val res = InputParamsCodegen.requiredParams(in)

      val expected = List(
        Term.Param(Nil, Term.Name("some_nested"), Some(toTpe("OutVal[List[Set[Map[String, String]]]]")), None)
      )

      assert(res.structure === expected.structure)
    }

    "work for HCL objects" in pending
  }

  "InputParamsCodegen.optionalParams" should {
    "work for primitive types" in {
      val in =
        List("some_string" -> HCLString, "some_number" -> HCLNumber, "some_bool" -> HCLBool, "some_any" -> HCLAny)
      val res = InputParamsCodegen.optionalParams(in)

      val expected = List(
        Term.Param(Nil, Term.Name("some_string"), Some(toTpe("Option[OutStringVal]")), None),
        Term.Param(Nil, Term.Name("some_number"), Some(toTpe("Option[OutVal[Int]]")), None),
        Term.Param(Nil, Term.Name("some_bool"), Some(toTpe("Option[OutVal[Boolean]]")), None),
        Term.Param(Nil, Term.Name("some_any"), Some(toTpe("Option[OutVal[Any]]")), None)
      )

      assert(res.structure === expected.structure)
    }

    "work for collection types" in {
      val in = List("some_list" -> HCLList(HCLString), "some_set" -> HCLSet(HCLNumber), "some_map" -> HCLMap(HCLBool))

      val res = InputParamsCodegen.optionalParams(in)

      val expected = List(
        Term.Param(Nil, Term.Name("some_list"), Some(toTpe("Option[OutVal[List[String]]]")), None),
        Term.Param(Nil, Term.Name("some_set"), Some(toTpe("Option[OutVal[Set[Int]]]")), None),
        Term.Param(Nil, Term.Name("some_map"), Some(toTpe("Option[OutVal[Map[String, Boolean]]]")), None)
      )

      assert(res.structure === expected.structure)
    }

    "work for nested collection types" in {
      val in = List("some_nested" -> HCLList(HCLSet(HCLMap(HCLString))))

      val res = InputParamsCodegen.optionalParams(in)

      val expected = List(
        Term.Param(Nil, Term.Name("some_nested"), Some(toTpe("Option[OutVal[List[Set[Map[String, String]]]]]")), None)
      )

      assert(res.structure === expected.structure)
    }

    "work for HCL objects" in pending
  }

  def toTpe(s: String) =
    s.parse[Type].get
}
