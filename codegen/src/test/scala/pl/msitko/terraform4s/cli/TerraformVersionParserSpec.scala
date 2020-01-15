package pl.msitko.terraform4s.cli

import pl.msitko.terraform4s.codegen.common.UnitSpec

class TerraformVersionParserSpec extends UnitSpec {
  "method parse" should {
    "work" in {
      val in =
        """Terraform v0.12.19
          |+ provider.aws v2.43.0
          |+ provider.digitalocean v1.12.0
          |+ provider.fastly v0.11.1
          |+ provider.github v2.2.1
          |""".stripMargin

      val res = TerraformVersionParser.parse(in)

      val expected = Versions(
        terraformVersion = "0.12.19",
        providersVersions = Map(
          "aws"          -> "2.43.0",
          "digitalocean" -> "1.12.0",
          "fastly"       -> "0.11.1",
          "github"       -> "2.2.1"
        )
      )

      assert(res === Right(expected))
    }

    "skip incorrect lines" in {
      val in =
        """Terraform v0.12.19
          |
          |+ provider.aws v2.43.0
          |+ something not following expectations
          |+ provider.fastly v0.11.1
          |""".stripMargin

      val res = TerraformVersionParser.parse(in)

      val expected = Versions(
        terraformVersion = "0.12.19",
        providersVersions = Map(
          "aws"    -> "2.43.0",
          "fastly" -> "0.11.1",
        )
      )

      assert(res === Right(expected))
    }

    "fail if terraform version is missing" in {
      val in =
        """+ provider.aws v2.43.0
          |+ provider.fastly v0.11.1
          |""".stripMargin

      val res = TerraformVersionParser.parse(in)

      assert(res === Left("Missing terraform version"))
    }

  }
}
