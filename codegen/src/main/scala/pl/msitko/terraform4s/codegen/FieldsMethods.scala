package pl.msitko.terraform4s.codegen

import pl.msitko.terraform4s.provider.ast._

import scala.meta.{Defn, Lit, Mod, Term, Type}

object FieldsMethods {

  def generate(requiredFields: List[(String, AttributeValue)]): Defn.Def = {
    val fields = requiredFields.map {
      case (fieldName, _) =>
        Term.Apply(Term.Name("Field"), List(Lit.String(fieldName), Term.Name(fieldName)))
    }

    overridenMethod(
      methodName = "fields",
      tpe = Type.Apply(Type.Name("List"), List(Type.Name("Field"))),
      term = Term.Apply(Term.Name("List"), fields)
    )
  }

  def generateOptionalFields(optionalFields: List[(String, AttributeValue)]): Defn.Def = {
    val fields = optionalFields.map {
      case (fieldName, attr) =>
        Term.Apply(
          Term.Select(Term.Name(fieldName), Term.Name("map")),
          List(
            Term.Function(
              List(Term.Param(Nil, Term.Name("i"), None, None)),
              Term.Apply(Term.Name("Field"), List(Lit.String(fieldName), Term.Name("i"))))))
    }

    overridenMethod(
      methodName = "optionalFields",
      tpe = Type.Apply(Type.Name("List"), List(Type.Apply(Type.Name("Option"), List(Type.Name("Field"))))),
      term = Term.Apply(Term.Name("List"), fields)
    )
  }

  private def overridenMethod(methodName: String, tpe: Type, term: Term) =
    Defn.Def(List(Mod.Override()), Term.Name(methodName), Nil, Nil, Some(tpe), term)
}
