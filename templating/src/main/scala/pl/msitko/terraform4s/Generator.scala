package pl.msitko.terraform4s

import io.circe.{Encoder, Json, JsonObject, Printer}
import io.circe.syntax._

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}

object Generator {

  def generateJson(outputFile: Path)(dslCode: pl.msitko.terraform4s.ProvidersRoot => Unit): Unit = {
    val root = pl.msitko.terraform4s.ProvidersRoot.Default
    dslCode(root)

    val res = root.getJson.printWith(Printer.spaces2)
    Files.write(outputFile, res.getBytes(StandardCharsets.UTF_8))
  }

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
