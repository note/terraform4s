package pl.msitko.terraform4s.cli

import java.nio.ByteBuffer

import metaconfig.Configured
import org.scalafmt.Scalafmt
import org.scalafmt.config.ScalafmtConfig
import pl.msitko.terraform4s.provider.ast.ProviderSchema
import pl.msitko.terraform4s.provider.json._

import scala.util.Try

sealed trait ConfigBase extends Product with Serializable

final case class ScriptConfig(
    sbtOrgName: String,
    sbtProjectName: String,
    sbtProjectVersion: String,
    providerName: String,
    providerVersion: String,
    pathForNewSbtProject: os.Path,
    packageNamePrefix: String,
    scalafmtPath: Option[os.Path]
) extends ConfigBase {

  def resolve(tmpDir: os.Path): Either[String, CodegenConfig] = {
    os.write(
      tmpDir / "main.tf",
      s"""
         |terraform {
         |  required_version = ">= 0.12.0"
         |}
         |
         |provider "$providerName" {
         |  version = "~> $providerVersion"
         |}
         |""".stripMargin)

    val terraformInitRes = os.proc("terraform", "init").call(cwd = tmpDir)
    if (terraformInitRes.exitCode == 0) {
      val terraformVersions = os.proc("terraform", "version").call(cwd = tmpDir).out.text
      val bytes             = ByteBuffer.wrap(os.proc("terraform providers schema -json".split(' ')).call(cwd = tmpDir).out.bytes)
      for {
        versions     <- TerraformVersionParser.parse(terraformVersions)
        schema       <- readSchema(bytes)
        scalafmtConf <- NotResolvedConfig.readScalafmtConf(scalafmtPath)
      } yield CodegenConfig(
        packageNamePrefix.split('.').toList,
        schema,
        versions,
        pathForNewSbtProject / "src" / "main" / "scala",
        scalafmtConf)
    } else {
      Left(s"terraform init exited with ${terraformInitRes.exitCode}, ${terraformInitRes.out.text}")
    }

  }

  private def readSchema(in: ByteBuffer): Either[String, ProviderSchema] =
    for {
      json   <- io.circe.jawn.parseByteBuffer(in).left.map(e => s"Cannot parse output of terraform cmd as JSON: $e")
      schema <- json.as[ProviderSchema].left.map(e => s"Cannot parse output of terraform cmd as ProviderSchema: $e")
    } yield schema
}

final case class CodegenConfig(
    packageNamePrefix: List[String],
    providerSchemas: ProviderSchema,
    versions: Versions,
    outPath: os.Path,
    scalafmtConf: ScalafmtConfig)

final case class Versions(
    terraformVersion: String,
    providersVersions: Map[String, String]
)

final case class NotResolvedConfig(
    packageNamePrefix: String,
    schemaPath: os.Path,
    versionsPath: os.Path,
    outPath: os.Path,
    scalafmtPath: Option[os.Path])
    extends ConfigBase {

  def resolve: Either[String, CodegenConfig] =
    for {
      scalafmtConf <- NotResolvedConfig.readScalafmtConf(scalafmtPath)
      schema       <- readSchema(schemaPath)
      versions <- Try(os.read(versionsPath)).toEither.left.map(e => s"Cannot open $versionsPath: $e").flatMap {
        fileContent =>
          TerraformVersionParser.parse(fileContent)
      }
    } yield CodegenConfig(
      packageNamePrefix = packageNamePrefix.split('.').toList,
      providerSchemas = schema,
      versions = versions,
      outPath = outPath,
      scalafmtConf = scalafmtConf
    )

  private def readSchema(inputPath: os.Path): Either[String, ProviderSchema] =
    for {
      inputFile <- Try(inputPath.toIO).toEither.left.map(e => s"Cannot open $inputPath: $e")
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

  def readScalafmtConf(scalafmtPath: Option[os.Path]): Either[String, ScalafmtConfig] = {
    def parseConfig(path: os.Path): Either[String, ScalafmtConfig] =
      for {
        fileContent <- Try(os.read(path)).toEither.left.map(e => s"Cannot open $path: $e")
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

  def getDefaultScalafmtConfig = DefaultScalafmtConfig match {
    case Configured.Ok(cfg) => cfg
    case Configured.NotOk(err) =>
      System.err.println(s"Cannot parse default scalafmt config due to $err. Falling back to ScalafmtConfig.default")
      ScalafmtConfig.default
  }
}
