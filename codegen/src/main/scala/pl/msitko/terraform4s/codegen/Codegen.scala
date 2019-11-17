package pl.msitko.terraform4s.codegen

import pl.msitko.terraform4s.provider.ast._

import scala.meta._

object Codegen {
  private type CacheType = Map[Long, String]

  def fromResource(name: String, resource: Resource, hclObjectsCache: CacheType): List[Defn.Class] = {
    // so far we are only interested in arguments, very naive logic (validate later):
    val arguments: List[(String, AttributeValue)] = resource.block.attributes.filter {
      case (_, v) => v.optional.isDefined || v.required == Some(true)
    }

    // needed for objects, way too naive to handle e.g. nested objects
    val anonymousClasses = arguments.map(_._2.`type`).collect {
      case obj: HCLObject => obj
    }

    val anonymousClassesDefs = anonymousClasses.map { obj =>
      val syntheticClassName = hclObjectsCache.apply(obj.hashCode())
      fromHCLObject(syntheticClassName, obj, hclObjectsCache)
    }

    anonymousClassesDefs ++ generateResourceClass(name, resource, hclObjectsCache)
  }

  private def generateResourceClass(name: String, v: Resource, hclObjectsCache: CacheType): List[Defn.Class] = {
    val outTypeName = name + "Out"

    val requiredParams = InputParamsCodegen.requiredParams(v.block.requiredInputs.map(t => (t._1, t._2.`type`)))
    val optionalParams = InputParamsCodegen.optionalParams(v.block.optionalInputs.map(t => (t._1, t._2.`type`)))

    // format: off
    List(
      OutCodegen.out(outTypeName, v.block.outputs),
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

  // TODO: non recursive recursion
  private def typeStringFromType(v: HCLType, hclObjectsCache: CacheType): Type = v match {
    case HCLString => Type.Name("String")
    // TODO: according to https://www.terraform.io/docs/configuration/types.html#primitive-types: The number type can represent both whole numbers like 15 and fractional values such as 6.283185
    case HCLNumber    => Type.Name("Long")
    case HCLBool      => Type.Name("Boolean")
    case HCLAny       => Type.Name("Any")
    case HCLList(t)   => Type.Apply(Type.Name("List"), List(typeStringFromType(t, hclObjectsCache)))
    case HCLMap(t)    => Type.Apply(Type.Name("Map"), List(Type.Name("String"), typeStringFromType(t, hclObjectsCache)))
    case HCLSet(t)    => Type.Apply(Type.Name("Set"), List(typeStringFromType(t, hclObjectsCache)))
    case HCLObject(t) => Type.Name(hclObjectsCache.apply(t.hashCode())) // TODO: comment why apply is safe
  }

  private def fromHCLObject(name: String, obj: HCLObject, hclObjectsCache: CacheType): Defn.Class = {
    val params: List[Term.Param] = obj.attributes.map {
      case (k, v) =>
        Term.Param(
          mods = Nil,
          name = Term.Name(k),
          decltpe = Some(typeStringFromType(v, hclObjectsCache)),
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

trait ToScalaCode[T] {
  def toScala(v: T): String
}

object ToScalaCode {

  def toScala(name: String, v: HCLObject): String =
    s"""final case class $name()"""
}
