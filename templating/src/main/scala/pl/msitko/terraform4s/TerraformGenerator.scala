package pl.msitko.terraform4s

import io.circe.{Encoder, Json, JsonObject}
import io.circe.syntax._

object TerraformGenerator {

  def encode(resources: Seq[Resource[_]]): Json =
    Json.fromJsonObject(
      JsonObject.singleton("resource", resources.asJson)
    )

  // https://www.terraform.io/docs/configuration/syntax-json.html#json-file-structure
  implicit private lazy val resourceEncoder: Encoder[Resource[_]] = new Encoder[Resource[_]] {

    override def apply(namedResource: Resource[_]): Json =
      Json.fromJsonObject(
        JsonObject.singleton(
          namedResource.__schemaName,
          Json.fromJsonObject(
            JsonObject.singleton(
              namedResource.resourceName,
              Json.fromFields(
                jsonFromListOfFields(namedResource.__fields) ++
                  jsonFromListOfFields(namedResource.__optionalFields.flatten)
              )
            ))
        ))
  }

  private def jsonFromListOfFields(fields: List[Field]): Map[String, Json] =
    fields.foldLeft(Map.empty[String, Json]) { (acc, curr) =>
      acc.+(curr.originalName -> curr.value.toTerraform)
    }

}
