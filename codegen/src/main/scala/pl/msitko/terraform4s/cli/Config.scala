package pl.msitko.terraform4s.cli

import java.nio.file.Path

import org.scalafmt.Scalafmt
import org.scalafmt.config.ScalafmtConfig
import pl.msitko.terraform4s.provider.ast.ProviderSchema

import pl.msitko.terraform4s.provider.json._

import scala.util.Try

final case class Config(
    packageNamePrefix: List[String],
    providerSchemas: ProviderSchema,
    versions: Versions,
    outPath: Path,
    scalafmtConf: ScalafmtConfig)

final case class NotResolvedConfig(
    packageNamePrefix: String,
    schemaPath: Path,
    versionsPath: Path,
    outPath: Path,
    scalafmtPath: Option[Path]) {

  def resolve: Either[String, Config] =
    for {
      scalafmtConf <- readScalafmtConf(scalafmtPath)
      schema       <- readSchema(schemaPath)
      versions <- Try(os.read(os.Path(versionsPath))).toEither.left.map(e => s"Cannot open $versionsPath: $e").flatMap {
        fileContent =>
          TerraformVersionParser.parse(fileContent)
      }
    } yield Config(
      packageNamePrefix = packageNamePrefix.split('.').toList,
      providerSchemas = schema,
      versions = versions,
      outPath = outPath,
      scalafmtConf = scalafmtConf
    )

  private def readScalafmtConf(scalafmtPath: Option[Path]): Either[String, ScalafmtConfig] = {
    def parseConfig(path: Path): Either[String, ScalafmtConfig] =
      for {
        fileContent <- Try(os.read(os.Path(path))).toEither.left.map(e => s"Cannot open $path: $e")
        conf <- Scalafmt
          .parseHoconConfig(fileContent)
          .toEither
          .left
          .map(confError => s"Cannot parse $path as scalafmt conf: $confError")
      } yield conf

    scalafmtPath match {
      case Some(path) =>
        parseConfig(path)
      case None =>
        Right(ScalafmtConfig.default)
    }
  }

  private def readSchema(inputPath: Path): Either[String, ProviderSchema] =
    for {
      inputFile <- Try(inputPath.toFile).toEither.left.map(e => s"Cannot open $inputPath: $e")
      json      <- io.circe.jawn.parseFile(inputFile).left.map(e => s"Cannot parse $inputPath as JSON: $e")
      schema    <- json.as[ProviderSchema].left.map(e => s"Cannot parse $inputPath as ProviderSchema: $e")
    } yield schema
}
