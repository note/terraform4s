package pl.msitko.terraform4s.codegen

import java.io.File

import org.scalafmt.Scalafmt
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

      val packageName = List("pl", "msitko", "example")

      val versions = Versions(terraformVersion = "0.12.19", Map("aws" -> "2.43.0"))

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
  }
}
