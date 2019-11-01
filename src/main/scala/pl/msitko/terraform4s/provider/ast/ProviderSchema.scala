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
final case class Block(attributes: List[(String, AttributeValue)])

final case class AttributeValue(
                                 `type`: HCLType,
                                 optional: Option[Boolean],
                                 computed: Option[Boolean],
                                 required: Option[Boolean]
                               )

// https://www.terraform.io/docs/configuration/types.html
sealed trait HCLType

// https://www.terraform.io/docs/configuration/types.html#primitive-types
sealed trait PrimitiveType extends HCLType
// https://www.terraform.io/docs/configuration/types.html#collection-types
sealed trait CollectionType extends HCLType
// https://www.terraform.io/docs/configuration/types.html#structural-types
sealed trait StructuralType extends HCLType

final object HCLString extends PrimitiveType
final object HCLNumber extends PrimitiveType
final object HCLBool extends PrimitiveType
final object HCLAny extends PrimitiveType

final case class HCLList(`type`: HCLType) extends CollectionType
final case class HCLMap(`type`: HCLType) extends CollectionType
final case class HCLSet(`type`: HCLType) extends CollectionType

// List[(String, HCLType)] so we can preserve the original ordering of fields (which is probably non critical but
// the least surprising)
final case class HCLObject(attributes: List[(String, HCLType)]) extends StructuralType
// there's not `tuple` type in output of `terraform provider` for AWS so leaving it out for future:
//final case class HCLTuple(`type`: HCLType) extends CollectionTypes
