package pl.msitko.terraform4s.codegen.classes

import pl.msitko.terraform4s.codegen.CodegenContext
import pl.msitko.terraform4s.provider.ast.{
  HCLAny,
  HCLBool,
  HCLList,
  HCLMap,
  HCLNumber,
  HCLObject,
  HCLSet,
  HCLString,
  HCLType
}

import scala.meta.Type

object TypeSignatureCodegen {

  // TODO: non tail-recursive recursion
  def typeStringFromType(v: HCLType, ctx: CodegenContext): Type = v match {
    case HCLString => Type.Name("String")
    // TODO: according to https://www.terraform.io/docs/configuration/types.html#primitive-types: The number type can represent both whole numbers like 15 and fractional values such as 6.283185
    case HCLNumber  => Type.Name("Int")
    case HCLBool    => Type.Name("Boolean")
    case HCLAny     => Type.Name("Any")
    case HCLList(t) => Type.Apply(Type.Name("List"), List(typeStringFromType(t, ctx)))
    case HCLMap(t)  => Type.Apply(Type.Name("Map"), List(Type.Name("String"), typeStringFromType(t, ctx)))
    case HCLSet(t)  => Type.Apply(Type.Name("Set"), List(typeStringFromType(t, ctx)))
    case obj: HCLObject =>
      ctx.getNameOf(obj) match {
        case Some(name) => Type.Name(name)
        case None       => throw new RuntimeException(s"No synthetic name found for $obj")
      }
  }

}
