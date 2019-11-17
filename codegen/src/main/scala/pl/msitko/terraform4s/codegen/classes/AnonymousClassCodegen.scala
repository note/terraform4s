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

import scala.meta.{Ctor, Defn, Mod, Name, Self, Template, Term, Type}

object AnonymousClassCodegen {

  def fromHCLObject(name: String, obj: HCLObject, ctx: CodegenContext): Defn.Class = {
    val params: List[Term.Param] = obj.attributes.map {
      case (k, v) =>
        Term.Param(mods = Nil, name = Term.Name(k), decltpe = Some(typeStringFromType(v, ctx)), default = None)
    }

    caseClass(name, params)
  }

  private def caseClass(name: String, params: List[Term.Param]): Defn.Class =
    Defn.Class(
      mods = List(Mod.Final(), Mod.Case()),
      name = Type.Name(name),
      tparams = Nil,
      ctor = Ctor.Primary(Nil, Name(""), List(params)),
      templ = Template(Nil, Nil, Self(Name(""), None), Nil))

  // TODO: non recursive recursion
  private def typeStringFromType(v: HCLType, ctx: CodegenContext): Type = v match {
    case HCLString => Type.Name("String")
    // TODO: according to https://www.terraform.io/docs/configuration/types.html#primitive-types: The number type can represent both whole numbers like 15 and fractional values such as 6.283185
    case HCLNumber    => Type.Name("Int")
    case HCLBool      => Type.Name("Boolean")
    case HCLAny       => Type.Name("Any")
    case HCLList(t)   => Type.Apply(Type.Name("List"), List(typeStringFromType(t, ctx)))
    case HCLMap(t)    => Type.Apply(Type.Name("Map"), List(Type.Name("String"), typeStringFromType(t, ctx)))
    case HCLSet(t)    => Type.Apply(Type.Name("Set"), List(typeStringFromType(t, ctx)))
    case HCLObject(t) => ???
  }

}
