import sbt.Keys.organization

lazy val root = (project in file("."))
  .settings(
    name := "terraform4s",
    organization := "pl.msitko",
    version := """0.1.0""",

    scalaVersion := "2.13.1",
    scalafmtOnCompile := true,

    libraryDependencies ++= Seq(
      "software.amazon.awssdk" % "sdk-core" % "2.9.26",
      "com.beachape" %% "enumeratum" % "1.5.13",
      "io.circe" %% "circe-parser" % "0.12.3",
      "io.circe" %% "circe-generic" % "0.12.3",
      "io.circe" %% "circe-optics" % "0.12.0",
      "org.scalameta" %% "scalameta" % "4.2.3",
      "com.softwaremill.diffx" %% "diffx-scalatest" % "0.3.8" % Test,
      "org.scalatest" %% "scalatest" % "3.0.8" % Test,
      "commons-io" % "commons-io" % "2.6" % Test
    ),
    commonSmlBuildSettings,
    wartRemoverSettings,
    ossPublishSettings
  )

