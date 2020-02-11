package pl.msitko.terraform4s.codegen

import java.io.File
import java.time.{Instant, LocalDate, ZoneOffset}

import org.scalafmt.Scalafmt
import pl.msitko.terraform4s.cli.{CodegenConfig, Versions}
import pl.msitko.terraform4s.codegen.common.UnitSpec
import pl.msitko.terraform4s.provider.ast.ProviderSchema
import pl.msitko.terraform4s.provider.json._

class CodegenItSpec extends UnitSpec {
  val ActualOutputDir   = "actual-output"
  val ExpectedOutputDir = "expected-output"

  "generateAndSave" should {
    "yield expected output" in {
      val inputFile       = new File("output.json")
      val json            = io.circe.jawn.parseFile(inputFile).toOption.get
      val resourcesOutput = json.as[ProviderSchema].toOption.get

      val packageName = List("pl", "msitko", "example")

      val versions = Versions(terraformVersion = "0.12.19", Map("aws" -> "2.43.0"))

      val scalafmtCfg = Scalafmt.parseHoconConfig(os.read(os.pwd / ".scalafmt.conf")).get
      val cfg         = CodegenConfig(packageName, resourcesOutput, versions, (os.pwd / ActualOutputDir), scalafmtCfg)

      val someHardcodedInstant =
        Instant.ofEpochSecond(LocalDate.parse("2020-01-15").atStartOfDay().toEpochSecond(ZoneOffset.ofHours(0)))
      val res = Codegen.generateAndSave(cfg, someHardcodedInstant)

      res.toEither match {
        case Right(_) =>
          val r = os.proc("diff", "-r", ActualOutputDir, ExpectedOutputDir).call()
          os.proc("rm", "-r", ActualOutputDir).call()
        case Left(e) =>
          e.printStackTrace()
      }
    }
  }
}
