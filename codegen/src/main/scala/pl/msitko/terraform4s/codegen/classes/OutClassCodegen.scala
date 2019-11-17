package pl.msitko.terraform4s.codegen.classes

import pl.msitko.terraform4s.provider.ast._

import scala.meta.{Ctor, Defn, Mod, Name, Self, Template, Term, Type}

/**
  * It's responsible for generating that part (just an example):
  * final case class AwsKinesisStreamOut(arn: OutStringVal, id: OutStringVal)
  */
object OutClassCodegen {

  def out(name: String, outputs: List[(String, AttributeValue)]): Defn.Class = {
    Defn.Class(
      List(Mod.Final(), Mod.Case()),
      Type.Name(name),
      Nil,
      Ctor.Primary(
        Nil,
        Name(""),
        List(
          outFields(outputs)
        )),
      Template(Nil, Nil, Self(Name(""), None), Nil))
  }

  private def outFields(outputs: List[(String, AttributeValue)]): List[Term.Param] =
    outputs.map {
      case (fieldName, attr) =>
        Term.Param(Nil, Term.Name(fieldName), Some(outType(attr.`type`)), None)
    }

  def outType(tpe: HCLType): Type = tpe match {
    case HCLString => Type.Name("OutStringVal")
    case HCLNumber =>
      Type.Apply(Type.Name("OutVal"), List(Type.Name("Int"))) // TODO: change to more general representation that can hold doubles
    case HCLBool => Type.Apply(Type.Name("OutVal"), List(Type.Name("Boolean")))
    case HCLAny  => Type.Apply(Type.Name("OutVal"), List(Type.Name("Any")))
    case _       => ???
  }
}
