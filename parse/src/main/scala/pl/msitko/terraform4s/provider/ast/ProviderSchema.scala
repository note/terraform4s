package pl.msitko.terraform4s.provider.ast

/**
  * Models JSON output of `terraform providers schema -json`
  *
  * Current AST includes only attributes that are used by Scala codegen
  * In future we may add attributes present in `terraform providers schema -json`
  */
final case class ProviderSchema(provider_schemas: Map[String, Provider])

final case class Provider(resource_schemas: Map[String, Resource])
final case class Resource(version: Int, block: Block)

// List[(String, AttributeValue)] so we can preserve the original ordering of fields (which is probably non critical but
// the least surprising)
final case class Block(attributes: List[(String, AttributeValue)]) {

  // TODO: test all 4 methods
  // https://www.terraform.io/docs/extend/schemas/schema-methods.html
  def requiredInputs: List[(String, AttributeValue)] =
    attributes.filter(_._2.required.getOrElse(false))

  def optionalInputs: List[(String, AttributeValue)] =
    attributes.filter(t => t._2.optional.getOrElse(false) && !t._2.computed.getOrElse(false))

  def alwaysPresentOutputs: List[(String, AttributeValue)] =
    attributes.filter(t => t._2.optional.getOrElse(true) && t._2.computed.getOrElse(false))

  def optionalOutputs: List[(String, AttributeValue)] =
    attributes.filter(t => !t._2.optional.getOrElse(true) && t._2.computed.getOrElse(false))

  def allObjects: List[HCLObject] =
    attributes.map(_._2.`type`.allObjects).flatten
}

final case class AttributeValue(
    `type`: HCLType,
    optional: Option[Boolean],
    computed: Option[Boolean],
    required: Option[Boolean]
)

// https://www.terraform.io/docs/configuration/types.html
sealed trait HCLType {
  // Not sure if it should live here as it can be seen as codegen implementation detail as opposed to part of AST
  def allObjects: List[HCLObject]
}

// https://www.terraform.io/docs/configuration/types.html#primitive-types
sealed trait PrimitiveType extends HCLType {
  override def allObjects: List[HCLObject] = List.empty
}

// https://www.terraform.io/docs/configuration/types.html#collection-types
sealed trait CollectionType extends HCLType {
  def `type`: HCLType

  override def allObjects: List[HCLObject] = `type`.allObjects
}

// https://www.terraform.io/docs/configuration/types.html#structural-types
sealed trait StructuralType extends HCLType

final object HCLString extends PrimitiveType
final object HCLNumber extends PrimitiveType
final object HCLBool   extends PrimitiveType
final object HCLAny    extends PrimitiveType

final case class HCLList(`type`: HCLType) extends CollectionType
final case class HCLMap(`type`: HCLType)  extends CollectionType
final case class HCLSet(`type`: HCLType)  extends CollectionType

// List[(String, HCLType)] so we can preserve the original ordering of fields (which is probably non critical but
// the least surprising)
final case class HCLObject(attributes: List[(String, HCLType)]) extends StructuralType {
  override def allObjects: List[HCLObject] = this :: attributes.flatMap(_._2.allObjects)
}
// there's not `tuple` type in output of `terraform provider` for AWS so leaving it out for future:
//final case class HCLTuple(`type`: HCLType) extends CollectionTypes
