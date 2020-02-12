package pl.msitko.terraform4s

import java.time.Instant

import cats.implicits._
import com.monovore.decline._
import pl.msitko.terraform4s.cli.{ConfigBase, NotResolvedConfig, PathOpt, ScriptConfig}
import pl.msitko.terraform4s.codegen.Codegen

import scala.util.{Failure, Success}

/**
  * Generates scala code out of `terraform providers schema -json`
  */
object Main {

  def main(args: Array[String]): Unit = {
    val codegen =
      Opts.subcommand(
        Command(name = "codegen", header = "Generate Scala code out of terraform provider's schema")(codegenOpts))

    val newSbtProject =
      Opts.subcommand(
        Command(
          name = "newSbtProject",
          header =
            "Execute any prerequisite steps for code generation, generate Scala code and create sbt project containing that code")(
          scriptOps))

    val commands: Opts[ConfigBase] = codegen.orElse(newSbtProject)
    Command(name = "terraform4s-codegen", header = "")(commands).parse(args) match {
      case Right(cfg: NotResolvedConfig) =>
        println("parsed: codegen")
        cfg.resolve match {
          case Right(resolvedConfig) =>
            Codegen.generateAndSave(resolvedConfig, Instant.now)
          case Left(msg) =>
            System.err.println(msg)
            sys.exit(1)
        }
      case Right(cfg: ScriptConfig) =>
        println("parsed: script")
        val tmpDir           = os.pwd / ".terraform4s-tmp"
        val newSbtProjectDir = cfg.pathForNewSbtProject

        makeDirIfNotExist(tmpDir)
        makeDirIfNotExist(newSbtProjectDir)

        os.write(
          newSbtProjectDir / "build.sbt",
          s"""
             |scalaVersion := "2.13.1"
             |
             |organization := "${cfg.sbtOrgName}"
             |name         := "${cfg.sbtProjectName}"
             |version      := "${cfg.sbtProjectVersion}"
             |
             |libraryDependencies += "pl.msitko" %% "terraform4s-templating" % "0.1.0"
             |""".stripMargin)

        cfg.resolve(tmpDir) match {
          case Right(resolvedConfig) =>
            println("generating...")
            Codegen.generateAndSave(resolvedConfig, Instant.now) match {
              case Success(_) =>
                println("Finished generating")
                os.remove.all(tmpDir)
              case Failure(e) =>
                e.printStackTrace()
                println(s"Error when generating: $e")
            }
          case Left(msg) =>
            System.err.println(msg)
            os.remove.all(tmpDir)
            sys.exit(1)
        }
      case Left(help) =>
        System.err.println(help)
        sys.exit(1)
    }
  }

  private def makeDirIfNotExist(dir: os.Path): Unit =
    if (os.exists(dir)) {
      System.err.println(s"$dir exists. Ensure it can be removed, remove it and rerun the command")
      sys.exit(1)
    } else {
      os.makeDir(dir)
    }

  private val packageNameOpt = Opts.option[String](
    "out-package-name",
    help =
      "Package name used for output. If you specify package name as 'org.company' then the code for provider 'abc' will be generated in package 'org.company.abc'")

  private lazy val codegenOpts: Opts[NotResolvedConfig] = {
    val scalafmtPathOpt = PathOpt("scalafmt-path", help = "Path to scalafmt configuration file").orNone

    val outDirOpt = PathOpt("out-dir", help = "Output directory")
      .withDefault(os.pwd / "out")

    val versionsPathOpt = PathOpt(
      "versions-path",
      help =
        "Path to the file with terraform providers versions. The file is supposed to be generated with command `terraform version`")
      .withDefault(os.pwd / "versions")

    val schemaPathOpt = PathOpt(
      "schema-path",
      help =
        "Path to Schema file. The Schema file is supposed to be generated with command `terraform providers schema -json`")

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

  private lazy val scriptOps: Opts[ScriptConfig] = {
    val sbtOrgNameOpt =
      Opts.option[String]("sbt-org-name", help = "organization name used in newly created sbt project")
    val sbtProjectNameOpt =
      Opts.option[String]("sbt-project-name", help = "project name used in newly created sbt project")
    val sbtProjectVersionOpt =
      Opts.option[String]("sbt-project-version", help = "project version used in newly created sbt project")
    val providerNameOpt         = Opts.option[String]("provider-name", help = "Provider name (e.g. aws)")
    val providerVersionOpt      = Opts.option[String]("provider-version", help = "Provider version (e.g. 2.43.0)")
    val pathForNewSbtProjectOpt = PathOpt("sbt-project-path", help = "path used to create new sbt project")
    val scalafmtPathOpt         = PathOpt("scalafmt-path", help = "Path to scalafmt configuration file").orNone
    (
      sbtOrgNameOpt,
      sbtProjectNameOpt,
      sbtProjectVersionOpt,
      providerNameOpt,
      providerVersionOpt,
      pathForNewSbtProjectOpt,
      packageNameOpt,
      scalafmtPathOpt).mapN {
      (
          sbtOrgName,
          sbtProjectName,
          sbtProjectVersion,
          providerName,
          providerVersion,
          pathForNewSbtProject,
          packageName,
          scalafmtPath) =>
        ScriptConfig(
          sbtOrgName = sbtOrgName,
          sbtProjectName = sbtProjectName,
          sbtProjectVersion = sbtProjectVersion,
          providerName = providerName,
          providerVersion = providerVersion,
          pathForNewSbtProject = pathForNewSbtProject,
          packageNamePrefix = packageName,
          scalafmtPath = scalafmtPath
        )
    }
  }
}
