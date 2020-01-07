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

      val packageName = List("pl", "msitko", "example")
      val ctx         = new DefaultCodegenContext

      Codegen.generateAndSave(
        resourceSchemas.take(3),
        packageName,
        (os.pwd / "out").toNIO,
        (os.pwd / ".scalafmt.conf").toNIO,
        ctx)

//      val expected = IOUtils.toString(stream, StandardCharsets.UTF_8)
//
//      assert(actual === expected)
    }
  }
}
