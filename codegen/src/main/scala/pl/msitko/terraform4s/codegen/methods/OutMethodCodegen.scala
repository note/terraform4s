package pl.msitko.terraform4s.codegen.methods

import pl.msitko.terraform4s.codegen.CodegenContext
import pl.msitko.terraform4s.codegen.classes.OutClassCodegen.{optionalOutFields, outFields}
import pl.msitko.terraform4s.provider.ast.{AttributeValue, _}

import scala.meta.{Defn, Lit, Mod, Term, Type}

object OutMethodCodegen {

  // TODO: it's very tighly connected with OutClassCodegen.out but it's not expressed on code level at all
  def generate(
      outType: String,
      requiredInputs: List[(String, AttributeValue)],
      optionalInputs: List[(String, AttributeValue)],
      optionalNonComputedInputs: List[(String, AttributeValue)],
      nonInputs: List[(String, AttributeValue)],
      preferOption: Boolean,
      ctx: CodegenContext): Defn.Def = {

    val preferred = if (preferOption) {
      termForOptionalType _
    } else {
      termForType _
    }

    val params = termForType(requiredInputs, ctx) ++ preferred(optionalInputs, ctx) ++ termForOptionalType(
      optionalNonComputedInputs,
      ctx) ++ preferred(nonInputs, ctx)

    Defn.Def(List(Mod.Override()), Term.Name("out"), Nil, Nil, None, Term.Apply(Term.Name(outType), params))
  }

  private def termForType(attrs: List[(String, AttributeValue)], ctx: CodegenContext): List[Term] =
    attrs.map { case (attrName, attr) =>
      Term.Apply(
        attr.`type` match {
          case HCLString => Term.Name("OutStringVal")
          case HCLNumber =>
            outValOf(Type.Name("Int")) // TODO: change to more general representation that can hold doubles
          case HCLBool       => outValOf(Type.Name("Boolean"))
          case HCLAny        => outValOf(Type.Name("Any"))
          case somethingElse => outValOf(nestedToType(somethingElse, ctx))
        },
        List(Term.Name("schemaName"), Term.Name("resourceName"), Lit.String(attrName))
      )
    }

  private def termForOptionalType(attrs: List[(String, AttributeValue)], ctx: CodegenContext): List[Term] =
    attrs.map { case (attrName, attr) =>
      Term.Apply(
        attr.`type` match {
          case HCLString => optionalOutValOf(Type.Name("String"))
          case HCLNumber =>
            optionalOutValOf(Type.Name("Int")) // TODO: change to more general representation that can hold doubles
          case HCLBool       => optionalOutValOf(Type.Name("Boolean"))
          case HCLAny        => optionalOutValOf(Type.Name("Any"))
          case somethingElse => optionalOutValOf(nestedToType(somethingElse, ctx))
        },
        List(Term.Name("schemaName"), Term.Name("resourceName"), Lit.String(attrName))
      )
    }

  private def outValOf(tpe: Type) = Term.ApplyType(Term.Name("OutVal"), List(tpe))

  private def optionalOutValOf(tpe: Type) =
    Term.ApplyType(Term.Name("OutVal"), List(Type.Apply(Type.Name("Option"), List(tpe))))

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
