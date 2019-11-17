package pl.msitko.terraform4s.codegen.methods

import pl.msitko.terraform4s.provider.ast._

import scala.meta.{Defn, Lit, Mod, Term, Type}

object OutMethodCodegen {

  def generate(outType: String, outFields: List[(String, AttributeValue)]): Defn.Def = {
    val params = outFields.map {
      case (fieldName, attr) =>
        Term.Apply(
          termForType(attr.`type`),
          List(Term.Name("schemaName"), Term.Name("resourceName"), Lit.String(fieldName)))
    }

    Defn.Def(List(Mod.Override()), Term.Name("out"), Nil, Nil, None, Term.Apply(Term.Name(outType), params))
  }

  def termForType(tpe: HCLType): Term = tpe match {
    case HCLString => Term.Name("OutStringVal")
    case HCLNumber =>
      Term.ApplyType(Term.Name("OutVal"), List(Type.Name("Int"))) // TODO: change to more general representation that can hold doubles
    case HCLBool => Term.ApplyType(Term.Name("OutVal"), List(Type.Name("Boolean")))
    case HCLAny  => Term.ApplyType(Term.Name("OutVal"), List(Type.Name("Any")))
    case _       => ???
  }
}
