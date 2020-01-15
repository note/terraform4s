package pl.msitko.terraform4s

import java.io.File
import java.nio.file.{Path, Paths}

import cats.implicits._
import com.monovore.decline._
import pl.msitko.terraform4s.cli.NotResolvedConfig
import pl.msitko.terraform4s.codegen.Codegen
import pl.msitko.terraform4s.provider.ast._
import pl.msitko.terraform4s.provider.json._

/**
  * Generates scala code out of `terraform providers schema -json`
  */
object Main {

  val codegenOpts: Opts[NotResolvedConfig] = {
    val scalafmtPathOpt = Opts.option[Path]("scalafmt-path", help = "Path to scalafmt configuration file").orNone

    val outDirOpt = Opts
      .option[Path]("out-dir", help = "Output directory")
      .withDefault(Paths.get("out"))

    val versionsPathOpt = Opts
      .option[Path](
        "versions-path",
        help =
          "Path to the file with terraform providers versions. The file is supposed to be generated with command `terraform version`")
      .withDefault(Paths.get("versions"))

    val schemaPathOpt = Opts.option[Path](
      "schema-path",
      help =
        "Path to Schema file. The Schema file is supposed to be generated with command `terraform providers schema -json`")

    val packageNameOpt = Opts.option[String](
      "out-package-name",
      help =
        "Package name used for output. If you specify package name as 'org.company' then the code for provider 'abc' will be generated in package 'org.company.abc'")

    (scalafmtPathOpt, outDirOpt, versionsPathOpt, schemaPathOpt, packageNameOpt).mapN {
      (scalafmtPath, outDir, versionsPath, schemaPath, packageName) =>
        NotResolvedConfig(
          packageNamePrefix = packageName,
          schemaPath = schemaPath,
          versionsPath = versionsPath,
          outPath = outDir,
          scalafmtPath = scalafmtPath
        )
    }
  }

  def main(args: Array[String]): Unit = {
    val codegen =
      Command(name = "codegen", header = "Generate Scala code out of terraform provider's schema")(codegenOpts)

    codegen.parse(args) match {
      case Right(cfg) =>
        cfg.resolve match {
          case Right(resolvedConfig) =>
            Codegen.generateAndSave(resolvedConfig)
          case Left(msg) =>
            System.err.println(msg)
            sys.exit(1)
        }
      case Left(help) =>
        System.err.println(help)
        sys.exit(1)
    }

    val versionsMap = Map("aws" -> "2.43.0")

    // inputFilePath is expected to be output of `terraform providers schema -json`
    val inputFilePath = args.headOption match {
      case Some(path) =>
        path
      case None =>
        println("Incorrect invocation: You have to provider input file name as first parameter")
        sys.exit(1)
    }

    val inputFile = new File(inputFilePath)
    val json = io.circe.jawn.parseFile(inputFile) match {
      case Right(json) =>
        json
      case Left(e) =>
        println(s"$inputFilePath cannot be parsed as JSON: $e")
        sys.exit(1)
    }

    val resourcesOutput: ProviderSchema = json.as[ProviderSchema] match {
      case Right(v) => v
      case Left(e) =>
        println(s"$inputFilePath JSON cannot be parsed as TerraformResourcesOutput: $e")
        println(e.getMessage())
        println(e.getCause())
        e.printStackTrace()
        sys.exit(1)
    }

    println(s"$inputFile parsed as TerraformResourcesOutput")
    println(resourcesOutput.provider_schemas.head._2.resource_schemas.size)

  }

}
