import sbt.Keys.organization
import Common._

lazy val parse = (project in file("parse"))
  .commonSettings("terraform4s-parse", "0.1.0")
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-parser" % "0.12.3",
      "io.circe" %% "circe-generic" % "0.12.3",
      "org.scalatest" %% "scalatest" % "3.0.8" % Test,
      "commons-io" % "commons-io" % "2.6" % Test
    )
  )

lazy val codegen = (project in file("codegen"))
  .commonSettings("terraform4s-codegen", "0.1.0")
  .settings(
    libraryDependencies ++= Seq(
      "software.amazon.awssdk" % "sdk-core" % "2.9.26", // TODO: is it needed?
      "com.beachape" %% "enumeratum" % "1.5.13",
      "org.scalameta" %% "scalameta" % "4.2.3"
    )
  )
  .dependsOn(parse)

lazy val templating = (project in file("templating"))
  .commonSettings("terraform4s-templating", "0.1.0")
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-generic" % "0.12.3",
      "com.lihaoyi" %% "sourcecode" % "0.1.8"
    )
  )
