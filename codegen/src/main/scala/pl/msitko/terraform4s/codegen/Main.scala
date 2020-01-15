package pl.msitko.terraform4s

import java.nio.file.{Path, Paths}

import cats.implicits._
import com.monovore.decline._
import pl.msitko.terraform4s.cli.NotResolvedConfig
import pl.msitko.terraform4s.codegen.Codegen

/**
  * Generates scala code out of `terraform providers schema -json`
  */
object Main {

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
  }

  private val codegenOpts: Opts[NotResolvedConfig] = {
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

}
