package pl.msitko.terraform4s

import io.circe.{Encoder, Json, JsonObject}
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax._

object Encoding {

  def encode(resources: Seq[Resource[_]]): Json =
    Json.fromJsonObject(
      JsonObject.singleton("resource", resources.asJson)
    )

  // https://www.terraform.io/docs/configuration/syntax-json.html#json-file-structure
  implicit private lazy val resourceEncoder: Encoder[Resource[_]] = new Encoder[Resource[_]] {

    override def apply(namedResource: Resource[_]): Json =
      Json.fromJsonObject(
        JsonObject.singleton(
          namedResource.schemaName,
          Json.fromJsonObject(
            JsonObject.singleton(
              namedResource.resourceName,
              Json.fromJsonObject(JsonObject.fromMap(namedResource.fields.foldLeft(Map.empty[String, Json]) {
                (acc, curr) =>
                  acc.+(curr.originalName -> encodeTypeValue(curr.value))
              }))
            ))
        ))
  }

  def encodeTypeValue(v: TypedValue): Json = v match {
    case TypedString(InVal(v))         => Json.fromString(v)
    case TypedBool(InVal(v))           => Json.fromBoolean(v)
    case TypedString(v: OutValBase[_]) => Json.fromString(v.resolve)
    case TypedBool(v: OutValBase[_])   => Json.fromString(v.resolve)
    case e =>
      println(e)
      ???
  }

}
