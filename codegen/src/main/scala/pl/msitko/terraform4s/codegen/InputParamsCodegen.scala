package pl.msitko.terraform4s.codegen

import pl.msitko.terraform4s.provider.ast.{HCLAny, HCLBool, HCLList, HCLMap, HCLNumber, HCLSet, HCLString, HCLType}

import scala.meta._

object InputParamsCodegen {

  def requiredParams(params: List[(String, HCLType)]): List[Term.Param] =
    params.map {
      case (fieldName, attrType) =>
        Term.Param(Nil, Term.Name(fieldName), Some(toType(attrType)), None)
    }

  def optionalParams(params: List[(String, HCLType)]): List[Term.Param] =
    params.map {
      case (fieldName, attrType) =>
        Term.Param(Nil, Term.Name(fieldName), Some(Type.Apply(Type.Name("Option"), List(toType(attrType)))), None)
    }

  // @tailrec , non tailrec
  private def toType(tpe: HCLType): Type = tpe match {
    case HCLString => Type.Name("OutStringVal")
    case HCLNumber =>
      Type.Apply(Type.Name("OutVal"), List(Type.Name("Int"))) // TODO: change to more general representation that can hold doubles
    case HCLBool => Type.Apply(Type.Name("OutVal"), List(Type.Name("Boolean")))
    case HCLAny  => Type.Apply(Type.Name("OutVal"), List(Type.Name("Any")))
    case HCLList(innerTpe) =>
      Type.Apply(Type.Name("OutVal"), List(Type.Apply(Type.Name("List"), nestedToType((innerTpe)))))
    case HCLSet(innerTpe) => Type.Apply(Type.Name("OutVal"), List(Type.Apply(Type.Name("Set"), nestedToType(innerTpe))))
    case HCLMap(innerTpe) =>
      Type.Apply(
        Type.Name("OutVal"),
        List(Type.Apply(Type.Name("Map"), List(Type.Name("String")) ++ nestedToType(innerTpe))))
    case e =>
      println("unexpected: " + e)
      ???
  }

  // non tailrec
  private def nestedToType(tpe: HCLType): List[Type] = tpe match {
    case HCLString => List(Type.Name("String"))
    case HCLNumber => List(Type.Name("Int"))
    case HCLBool   => List(Type.Name("Boolean"))
    case HCLAny    => List(Type.Name("Any"))
    case HCLList(innerTpe) =>
      List(Type.Apply(Type.Name("OutVal"), List(Type.Apply(Type.Name("List"), nestedToType((innerTpe))))))
    case HCLSet(innerTpe) => List(Type.Apply(Type.Name("Set"), nestedToType(innerTpe)))
    case HCLMap(innerTpe) => List(Type.Apply(Type.Name("Map"), List(Type.Name("String")) ++ nestedToType(innerTpe)))
    case e =>
      println("unexpected2: " + e)
      ???
  }
}
