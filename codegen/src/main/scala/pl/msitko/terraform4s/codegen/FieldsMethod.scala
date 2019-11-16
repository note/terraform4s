package pl.msitko.terraform4s.codegen

import pl.msitko.terraform4s.provider.ast._

import scala.meta.{Defn, Lit, Mod, Term, Type}

object FieldsMethod {

  def generate(requiredFields: List[(String, AttributeValue)]): Defn.Def = {
    println(s"bazinga ${requiredFields}")
    val fields = requiredFields.map {
      case (fieldName, _) =>
        Term.Apply(Term.Name("Field"), List(Lit.String(fieldName), Term.Name(fieldName)))
    }

    Defn.Def(
      List(Mod.Override()),
      Term.Name("fields"),
      Nil,
      Nil,
      Some(Type.Apply(Type.Name("List"), List(Type.Name("Field")))),
      Term.Apply(Term.Name("List"), fields))
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

    Defn.Def(
      List(Mod.Override()),
      Term.Name("optionalFields"),
      Nil,
      Nil,
      Some(Type.Apply(Type.Name("List"), List(Type.Apply(Type.Name("Option"), List(Type.Name("Field")))))),
      Term.Apply(Term.Name("List"), fields))
  }

  // TODO: non-recursive recursion
//  private def fromTpe(tpe: HCLType): Term = tpe match {
//    case HCLString => Term.Name("TypedString")
//    case HCLNumber => Term.Name("TypedNumber")
//    case HCLBool   => Term.Name("TypeBool")
//    case HCLSet    => Term.Apply(Type.Name("Set"), List)
//    case _         => ???
//  }
}
