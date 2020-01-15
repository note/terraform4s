package pl.msitko.terraform4s.codegen

import java.io.File

import org.scalafmt.Scalafmt
import org.scalafmt.config.ScalafmtConfig
import pl.msitko.terraform4s.cli.{Config, Versions}
import pl.msitko.terraform4s.codegen.common.UnitSpec
import pl.msitko.terraform4s.provider.ast.ProviderSchema
import pl.msitko.terraform4s.provider.json._

class ItSpec extends UnitSpec {
  "it" should {
    "work" in {
      val inputFile       = new File("output.json")
      val json            = io.circe.jawn.parseFile(inputFile).toOption.get
      val resourcesOutput = json.as[ProviderSchema].toOption.get

//      val resourceSchemas = resourcesOutput.provider_schemas.get("aws").get.resource_schemas

//      assert(resourceSchemas.size === 539)
//
//      resourceSchemas.foreach { schema =>
//        val (name, resource) = schema
//
//        val block = resource.block
//        val used  = block.optionalInputs ++ block.requiredInputs ++ block.alwaysPresentOutputs ++ block.optionalOutputs
//
//        if (used.toSet.size != resource.block.attributes.size) {
//          val diff = resource.block.attributes.toSet.diff(used.toSet).map(_._1)
//          println(s"found unused for $name: $diff")
//        }
//
//        println(s"optional outputs: $name" + resource.block.optionalOutputs.map(_._1))
//      }

      val packageName = List("pl", "msitko", "example")

      val versions = Versions(terraformVersion = "0.12.19", Map("aws" -> "2.43.0"))

      resourcesOutput.provider_schemas.foreach {
        case (providerName, provider) =>
          println(s"Verifying $providerName")

          val scalafmtCfg = Scalafmt.parseHoconConfig(os.read(os.pwd / ".scalafmt.conf")).get
          val cfg         = Config(packageName, resourcesOutput, versions, (os.pwd / "out").toNIO, scalafmtCfg)
          val res         = Codegen.generateAndSave(cfg)

          println("res: " + res)

          res.toEither match {
            case Right(_) =>
            case Left(e) =>
              e.printStackTrace()
          }
      }

//      val expected = IOUtils.toString(stream, StandardCharsets.UTF_8)
//
//      assert(actual === expected)
    }
  }
}
