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

    anonymousClassesDefs ++ List(generateResourceClass(name, arguments, hclObjectsCache))
  }

  private def generateResourceClass(
      name: String,
      v: List[(String, AttributeValue)],
      hclObjectsCache: CacheType): Defn.Class = {
    val params = v.map {
      case (attrName, attr) =>
        Term.Param(
          mods = Nil,
          name = Term.Name(attrName),
          decltpe = Some(typeStringFromType(attr.`type`, hclObjectsCache)),
          default = None)
    }

    caseClass(name, params)
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
