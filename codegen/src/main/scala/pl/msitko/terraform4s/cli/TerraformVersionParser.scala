package pl.msitko.terraform4s.cli

import cats.syntax.either._

object TerraformVersionParser {

  val terraformVersionRegex = "Terraform v(([0-9]|\\.)+)".r
  val providerRegex         = "\\+ provider\\.((\\S)*) v(([0-9]|\\.)+)".r

  /** It parses the output of the command `terraform version`
    */
  def parse(input: String): Either[String, Versions] =
    (for {
      terraformVersion <- terraformVersionRegex.findFirstMatchIn(input).map(_.group(1))
      providers = providerRegex.findAllMatchIn(input).toList.map(matched => matched.group(1) -> matched.group(3)).toMap
    } yield Versions(terraformVersion, providers))
      .fold("Cannot parse terraform version".asLeft[Versions])(vs => vs.asRight[String])
}
