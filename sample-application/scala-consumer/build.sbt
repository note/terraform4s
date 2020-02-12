import Common._
import Dependencies._



lazy val root = (project in file("."))
  .commonSettings("scala-consumer", "0.1.0")
  .settings(
    libraryDependencies ++= Seq(
      logging,
      kcl2,
      s3,
      circe,
      scalatest
    )
  )
