package pl.msitko.terraform4s.codegen.classes

import pl.msitko.terraform4s.codegen.CodegenContext
import pl.msitko.terraform4s.provider.ast._

import scala.meta.{Ctor, Defn, Mod, Name, Self, Template, Term, Type}

/** It's responsible for generating that part (just an example): final case class AwsKinesisStreamOut(arn: OutStringVal,
  * id: OutStringVal)
  */
object OutClassCodegen {

  def out(
      name: String,
      requiredInputs: List[(String, AttributeValue)],
      optionalInputs: List[(String, AttributeValue)],
      optionalNonComputedInputs: List[(String, AttributeValue)],
      nonInputs: List[(String, AttributeValue)],
      preferOption: Boolean,
      ctx: CodegenContext): Defn.Class = {

    val preferredOutFields = respectPreference(preferOption) _

    Defn.Class(
      List(Mod.Final(), Mod.Case()),
      Type.Name(name),
      Nil,
      Ctor.Primary(
        Nil,
        Name(""),
        List(
          outFields(requiredInputs, ctx) ++
            preferredOutFields(optionalInputs, ctx) ++
            // in the below preference does not matter: https://discuss.hashicorp.com/t/providers-schema-how-to-distinguish-attributes-from-arguments/5029
            // With much richer templating we could express optionality (or its lack) with path dependant types
            optionalOutFields(optionalNonComputedInputs, ctx) ++
            preferredOutFields(nonInputs, ctx)
        )),
      Template(Nil, Nil, Self(Name(""), None), Nil))
  }

  private def outFields(outputs: List[(String, AttributeValue)], ctx: CodegenContext): List[Term.Param] =
    outputs.map { case (fieldName, attr) =>
      val tpeSignature = valOf(TypeSignatureCodegen.fromHCLType(attr.`type`, ctx))
      Commons.param(fieldName, tpeSignature)
    }

  private def optionalOutFields(outputs: List[(String, AttributeValue)], ctx: CodegenContext): List[Term.Param] =
    outputs.map { case (fieldName, attr) =>
      val tpeSignature = TypeSignatureCodegen.fromHCLType(attr.`type`, ctx)
      Commons.param(fieldName, valOf(Type.Apply(Type.Name("Option"), List(tpeSignature))))
    }

  private def valOf(tpe: Type): Type = Type.Apply(Type.Name("Val"), List(tpe))

  private def respectPreference(
      preferOption: Boolean)(fields: List[(String, AttributeValue)], ctx: CodegenContext): List[Term.Param] =
    if (preferOption) {
      optionalOutFields(fields, ctx)
    } else {
      outFields(fields, ctx)
    }
}
