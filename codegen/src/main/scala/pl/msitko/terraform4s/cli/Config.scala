package pl.msitko.terraform4s.cli

import java.nio.file.Path

import metaconfig.Configured
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

final case class Versions(
    terraformVersion: String,
    providersVersions: Map[String, String]
)

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
        Right(NotResolvedConfig.getDefaultScalafmtConfig)
    }
  }

  private def readSchema(inputPath: Path): Either[String, ProviderSchema] =
    for {
      inputFile <- Try(inputPath.toFile).toEither.left.map(e => s"Cannot open $inputPath: $e")
      json      <- io.circe.jawn.parseFile(inputFile).left.map(e => s"Cannot parse $inputPath as JSON: $e")
      schema    <- json.as[ProviderSchema].left.map(e => s"Cannot parse $inputPath as ProviderSchema: $e")
    } yield schema
}

object NotResolvedConfig {

  val DefaultScalafmtConfig = {
    val input = """align = more
                  |assumeStandardLibraryStripMargin = true
                  |binPack.parentConstructors = true
                  |danglingParentheses = false
                  |indentOperator = spray
                  |maxColumn = 120
                  |newlines.alwaysBeforeTopLevelStatements = true
                  |rewrite.redundantBraces.maxLines = 5
                  |rewrite.rules = [RedundantBraces, RedundantParens, SortImports, SortModifiers, PreferCurlyFors]
                  |runner.optimizer.forceConfigStyleOnOffset = -1
                  |trailingCommas = preserve
                  |verticalMultiline.arityThreshold = 120""".stripMargin

    Scalafmt.parseHoconConfig(input)
  }

  def getDefaultScalafmtConfig = DefaultScalafmtConfig match {
    case Configured.Ok(cfg) => cfg
    case Configured.NotOk(err) =>
      System.err.println(s"Cannot parse default scalafmt config due to $err. Falling back to ScalafmtConfig.default")
      ScalafmtConfig.default
  }
}
