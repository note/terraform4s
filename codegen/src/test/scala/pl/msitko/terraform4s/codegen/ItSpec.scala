package pl.msitko.terraform4s.codegen

import java.io.File

import pl.msitko.terraform4s.codegen.common.UnitSpec
import pl.msitko.terraform4s.provider.ast.ProviderSchema
import pl.msitko.terraform4s.provider.json._

class ItSpec extends UnitSpec {
  "it" should {
    "work" in {
      val inputFile       = new File("output.json")
      val json            = io.circe.jawn.parseFile(inputFile).toOption.get
      val resourcesOutput = json.as[ProviderSchema].toOption.get

      val resourceSchemas = resourcesOutput.provider_schemas.get("aws").get.resource_schemas

      assert(resourceSchemas.size === 539)

      resourceSchemas.foreach { schema =>
        val (name, resource) = schema

        val used = resource.block.optionalInputs ++ resource.block.requiredInputs ++ resource.block.outputs

        if (used.toSet.size != resource.block.attributes.size) {
          val diff = resource.block.attributes.toSet.diff(used.toSet).map(_._1)
          println(s"found unused for $name: $diff")
        }
      }

      val packageName = List("pl", "msitko", "example")
      val ctx         = new DefaultCodegenContext

      val selectedResourceNames = List(
        "aws_accessanalyzer_analyzer",
        "aws_acm_certificate",
        "aws_acm_certificate_validation"
      )

      val selectedResources = resourceSchemas.view.filterKeys(selectedResourceNames.contains(_)).toMap

      println(s"selected resources: ${selectedResources.size}")

      val res = Codegen.generateAndSave(
        selectedResources,
        packageName,
        (os.pwd / "out").toNIO,
        (os.pwd / ".scalafmt.conf").toNIO,
        ctx)

      println("res: " + res)

//      val expected = IOUtils.toString(stream, StandardCharsets.UTF_8)
//
//      assert(actual === expected)
    }
  }
}
