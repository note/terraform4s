package pl.msitko.terraform4s.codegen

import pl.msitko.terraform4s.provider.ast.{
  AttributeValue,
  HCLAny,
  HCLBool,
  HCLMap,
  HCLNumber,
  HCLSet,
  HCLString,
  HCLType
}

import scala.meta._

object InputParamsCodegen {

  def requiredParams(params: List[(String, AttributeValue)]): List[Term.Param] =
    params.map {
      case (fieldName, attr) =>
        Term.Param(Nil, Term.Name(fieldName), Some(toType(attr.`type`)), None)
    }

  def optionalParams(params: List[(String, AttributeValue)]): List[Term.Param] =
    params.map {
      case (fieldName, attr) =>
        Term.Param(Nil, Term.Name(fieldName), Some(Type.Apply(Type.Name("Option"), List(toType(attr.`type`)))), None)
    }

  private def toType(tpe: HCLType): Type = tpe match {
    case HCLString        => Type.Name("String")
    case HCLNumber        => Type.Name("Int") // TODO: change to more general representation that can hold doubles
    case HCLBool          => Type.Name("Boolean")
    case HCLAny           => Type.Name("Any")
    case HCLSet(innerTpe) => Type.Apply(Type.Name("Set"), List(toType(innerTpe)))
    case HCLMap(innerTpe) => Type.Apply(Type.Name("map"), List(Type.Name("String"), toType(innerTpe)))
    case e =>
      println("unexpected: " + e)
      ???
  }
}
