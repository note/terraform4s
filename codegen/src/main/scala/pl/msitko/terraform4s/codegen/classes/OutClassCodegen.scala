package pl.msitko.terraform4s.codegen.classes

import pl.msitko.terraform4s.codegen.CodegenContext
import pl.msitko.terraform4s.provider.ast._

import scala.meta.{Ctor, Defn, Mod, Name, Self, Template, Term, Type}

/**
  * It's responsible for generating that part (just an example):
  * final case class AwsKinesisStreamOut(arn: OutStringVal, id: OutStringVal)
  */
object OutClassCodegen {

  def out(name: String, outputs: List[(String, AttributeValue)], ctx: CodegenContext): Defn.Class = {
    Defn.Class(
      List(Mod.Final(), Mod.Case()),
      Type.Name(name),
      Nil,
      Ctor.Primary(
        Nil,
        Name(""),
        List(
          outFields(outputs, ctx)
        )),
      Template(Nil, Nil, Self(Name(""), None), Nil))
  }

  private def outFields(outputs: List[(String, AttributeValue)], ctx: CodegenContext): List[Term.Param] =
    outputs.map {
      case (fieldName, attr) =>
        Term.Param(
          mods = Nil,
          name = Term.Name(fieldName),
          decltpe = Some(TypeSignatureCodegen.typeStringFromType(attr.`type`, ctx)),
          default = None)
    }

}
