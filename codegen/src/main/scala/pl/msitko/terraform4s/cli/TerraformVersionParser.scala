package pl.msitko.terraform4s.cli

object TerraformVersionParser {

  /**
    * It parses the output of the command `terraform version`
    */
  def parse(input: String): Either[String, Versions] = ???
}

final case class Versions(
    terraformVersion: String,
    providersVersions: Map[String, String]
)
