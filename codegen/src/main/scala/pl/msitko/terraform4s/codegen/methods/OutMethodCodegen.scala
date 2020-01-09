package pl.msitko.terraform4s.codegen.methods

import pl.msitko.terraform4s.codegen.CodegenContext
import pl.msitko.terraform4s.provider.ast._

import scala.meta.{Defn, Lit, Mod, Term, Type}

object OutMethodCodegen {

  def generate(outType: String, outFields: List[(String, AttributeValue)], ctx: CodegenContext): Defn.Def = {
    val params = outFields.map {
      case (fieldName, attr) =>
        Term.Apply(
          termForType(attr.`type`, ctx),
          List(Term.Name("schemaName"), Term.Name("resourceName"), Lit.String(fieldName)))
    }

    Defn.Def(List(Mod.Override()), Term.Name("out"), Nil, Nil, None, Term.Apply(Term.Name(outType), params))
  }

  private def termForType(tpe: HCLType, ctx: CodegenContext): Term = tpe match {
    case HCLString => Term.Name("OutStringVal")
    case HCLNumber =>
      Term.ApplyType(Term.Name("OutVal"), List(Type.Name("Int"))) // TODO: change to more general representation that can hold doubles
    case HCLBool       => Term.ApplyType(Term.Name("OutVal"), List(Type.Name("Boolean")))
    case HCLAny        => Term.ApplyType(Term.Name("OutVal"), List(Type.Name("Any")))
    case somethingElse => Term.ApplyType(Term.Name("OutVal"), List(nestedToType(somethingElse, ctx)))
  }

  private def nestedToType(tpe: HCLType, ctx: CodegenContext): Type = tpe match {
    case HCLString       => Type.Name("String")
    case HCLNumber       => Type.Name("Int") // TODO: change to more general representation that can hold double
    case HCLBool         => Type.Name("Boolean")
    case HCLAny          => Type.Name("Any")
    case HCLList(nested) => Type.Apply(Type.Name("List"), List(nestedToType(nested, ctx)))
    case HCLSet(nested)  => Type.Apply(Type.Name("Set"), List(nestedToType(nested, ctx)))
    case HCLMap(nested)  => Type.Apply(Type.Name("Map"), List(Type.Name("String"), nestedToType(nested, ctx)))
    case obj: HCLObject =>
      ctx.getNameOf(obj) match {
        case Some(name) => Type.Name(name)
        case None       => throw new RuntimeException(s"No synthetic name found for $obj")
      }
  }
}
