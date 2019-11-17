package pl.msitko.terraform4s.codegen

import pl.msitko.terraform4s.codegen.classes.{AnonymousClassCodegen, InputParamsCodegen, OutClassCodegen}
import pl.msitko.terraform4s.codegen.methods.{FieldsMethods, OutMethodCodegen}
import pl.msitko.terraform4s.provider.ast.{HCLObject, _}

import scala.meta._

trait CodegenContext {
  def getNextAnonymousClassName: String
  def registerAnonymousClass(className: String, classInput: HCLObject): Unit
  def getNameOf(classInput: HCLObject): Option[String]
}

class DefaultCodegenContext extends CodegenContext {
  private var map: Map[HCLObject, String] = Map.empty

  override def getNextAnonymousClassName: String = s"Anonymous${map.size}"

  override def registerAnonymousClass(className: String, classInput: HCLObject): Unit =
    map += (classInput -> className)

  override def getNameOf(classInput: HCLObject): Option[String] =
    map.get(classInput)
}

object Codegen {

  def fromResource(name: String, resource: Resource, ctx: CodegenContext): List[Defn.Class] = {
    // so far we are only interested in arguments, very naive logic (validate later):
    val arguments: List[(String, AttributeValue)] = resource.block.attributes.filter {
      case (_, v) => v.optional.isDefined || v.required == Some(true)
    }

    // needed for objects, way too naive to handle e.g. nested objects
    val anonymousClasses = arguments.map(_._2.`type`).collect {
      case obj: HCLObject => obj
    }

    val anonymousClassesDefs = anonymousClasses.map { obj =>
      val syntheticClassName = ctx.getNextAnonymousClassName
      AnonymousClassCodegen.fromHCLObject(syntheticClassName, obj, ctx)
    }

    anonymousClassesDefs ++ generateResourceClass(name, resource, ctx)
  }

  private def generateResourceClass(name: String, v: Resource, ctx: CodegenContext): List[Defn.Class] = {
    val outTypeName = name + "Out"

    val requiredParams = InputParamsCodegen.requiredParams(v.block.requiredInputs.map(t => (t._1, t._2.`type`)))
    val optionalParams = InputParamsCodegen.optionalParams(v.block.optionalInputs.map(t => (t._1, t._2.`type`)))

    // format: off
    List(
      OutClassCodegen.out(outTypeName, v.block.outputs),
      Defn.Class(List(Mod.Final(), Mod.Case()), Type.Name(name), Nil, Ctor.Primary(Nil, Name(""), List(
        requiredParams ++ optionalParams,
        List(Term.Param(List(Mod.Implicit()), Term.Name("r"), Some(Type.Name("ProvidersRoot")), None))
      )),
        Template(Nil, List(Init(Type.Apply(Type.Name("Resource"), List(Type.Name(outTypeName))), Name(""), List(List(Term.Name("r"))))), Self(Name(""), None),
          List(
            OutMethodCodegen.generate(name + "Out", v.block.outputs),
            FieldsMethods.generate(v.block.requiredInputs),
            FieldsMethods.generateOptionalFields(v.block.optionalInputs),
            Defn.Def(List(Mod.Override()), Term.Name("schemaName"), Nil, Nil, Some(Type.Name("String")), Lit.String(name))
          )
        )
      )
    )
    // format: on
  }

}
