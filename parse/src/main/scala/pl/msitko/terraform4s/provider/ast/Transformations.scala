package pl.msitko.terraform4s.provider.ast

object Transformations {

  def camelCaseAttributes(in: ProviderSchema): ProviderSchema =
    in.copy(provider_schemas = in.provider_schemas.map { case (providerName, provider) =>
      (
        providerName,
        provider.copy(resource_schemas = provider.resource_schemas.map { case (resourceName, resource) =>
          (
            resourceName,
            resource.copy(
              block = resource.block.copy(attributes = resource.block.attributes.map(camelCaseAttribute))
            ))
        }))
    })

  // copied from https://stackoverflow.com/a/37619752
  def toCamelCase(in: String) =
    "_([a-z\\d])".r.replaceAllIn(in, _.group(1).toUpperCase)

  private def camelCaseAttribute(in: (String, AttributeValue)): (String, AttributeValue) =
    (toCamelCase(in._1), in._2.copy(`type` = camelCaseType(in._2.`type`)))

  // non-tail recursion, but should not matter as we expect the most nested type to be of depth 4-5
  private def camelCaseType(in: HCLType): HCLType = in match {
    case pt: PrimitiveType => pt
    case HCLList(tpe)      => HCLList(camelCaseType(tpe))
    case HCLMap(tpe)       => HCLMap(camelCaseType(tpe))
    case HCLSet(tpe)       => HCLSet(camelCaseType(tpe))
    case HCLObject(attrs) =>
      HCLObject(attrs.map { case (attrName, tpe) =>
        (toCamelCase(attrName), camelCaseType(tpe))
      })
  }

}
