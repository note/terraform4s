package pl.msitko.terraform4s.codegen.classes

import pl.msitko.terraform4s.codegen.CodegenContext
import pl.msitko.terraform4s.provider.ast._

import scala.meta.{Term, Type}

object InputParamsCodegen {

  def requiredParams(params: List[(String, HCLType)], ctx: CodegenContext): List[Term.Param] =
    params.map {
      case (fieldName, attrType) =>
        Term.Param(Nil, Term.Name(fieldName), Some(toType(attrType, ctx)), None)
    }

  def optionalParams(params: List[(String, HCLType)], ctx: CodegenContext): List[Term.Param] =
    params.map {
      case (fieldName, attrType) =>
        Term.Param(Nil, Term.Name(fieldName), Some(Type.Apply(Type.Name("Option"), List(toType(attrType, ctx)))), None)
    }

  private def toType(tpe: HCLType, ctx: CodegenContext): Type =
    Type.Apply(Type.Name("Val"), nestedToType(tpe, ctx))

  // recursive but non-tailrec
  private def nestedToType(tpe: HCLType, ctx: CodegenContext): List[Type] =
    List(tpe match {
      case HCLString =>
        Type.Name("String")
      case HCLNumber =>
        Type.Name("Int") // TODO: change to more general representation that can hold doubles
      case HCLBool =>
        Type.Name("Boolean")
      case HCLAny =>
        Type.Name("Any")
      case HCLList(innerTpe) =>
        Type.Apply(Type.Name("List"), nestedToType(innerTpe, ctx))
      case HCLSet(innerTpe) =>
        Type.Apply(Type.Name("Set"), nestedToType(innerTpe, ctx))
      case HCLMap(innerTpe) =>
        Type.Apply(Type.Name("Map"), List(Type.Name("String")) ++ nestedToType(innerTpe, ctx))
      case obj: HCLObject =>
        ctx.getNameOf(obj) match {
          case Some(name) =>
            Type.Name(name)
          case None =>
            throw new RuntimeException(s"Cannot resolve name for HCLObject: $obj")
        }
    })
}
