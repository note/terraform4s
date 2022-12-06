package pl.msitko.terraform4s.codegen.classes

import pl.msitko.terraform4s.codegen.CodegenContext
import pl.msitko.terraform4s.provider.ast._

import scala.meta.{Term, Type}

object InputParamsCodegen {

  def requiredParams(params: List[(String, HCLType)], ctx: CodegenContext): List[Term.Param] =
    params.map { case (fieldName, attrType) =>
      Commons.param(fieldName, toType(attrType, ctx))
    }

  def optionalParams(params: List[(String, HCLType)], ctx: CodegenContext): List[Term.Param] =
    params.map { case (fieldName, attrType) =>
      val tpe = Type.Apply(Type.Name("Option"), List(toType(attrType, ctx)))
      Commons.param(fieldName, tpe, Some(Term.Name("None")))
    }

  private def toType(tpe: HCLType, ctx: CodegenContext): Type =
    Type.Apply(Type.Name("Val"), List(TypeSignatureCodegen.fromHCLType(tpe, ctx)))

}

object Commons {

  def param(name: String, tpe: Type, default: Option[Term.Name] = None): Term.Param =
    Term.Param(
      mods = Nil,
      name = Term.Name(name),
      decltpe = Some(tpe),
      default = default
    )
}
