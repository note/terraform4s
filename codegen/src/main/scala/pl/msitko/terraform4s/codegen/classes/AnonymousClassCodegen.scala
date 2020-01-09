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
        Term.Param(
          mods = Nil,
          name = Term.Name(k),
          decltpe = Some(TypeSignatureCodegen.typeStringFromType(v, ctx)),
          default = None)
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

}
