import com.softwaremill.SbtSoftwareMillCommon.commonSmlBuildSettings
import com.softwaremill.Publish.ossPublishSettings
import org.scalafmt.sbt.ScalafmtPlugin.autoImport.scalafmtOnCompile
import sbt.Keys.{name, organization, scalaVersion, version}
import sbt.Project
import sbt._
import sbt.Keys._

object Common {
  implicit class ProjectFrom(project: Project) {
    def commonSettings(nameArg: String, versionArg: String): Project = project.settings(
      name := nameArg,
      organization := "pl.msitko",
      version := versionArg,

      scalaVersion := "2.13.10",
      scalafmtOnCompile := true,

      commonSmlBuildSettings,
      ossPublishSettings
    )
  }
}