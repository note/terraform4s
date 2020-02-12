import sbt._

object Dependencies {
  lazy val bom = "software.amazon.awssdk" % "bom" % "2.10.30"
  lazy val logging = "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.13.0"
  lazy val kcl2 = "software.amazon.kinesis" % "amazon-kinesis-client" % "2.2.4"
  lazy val s3   = "software.amazon.awssdk" % "s3" % "2.10.61"
  lazy val circe = "io.circe" %% "circe-parser" % "0.12.3"
	lazy val scalatest = "org.scalatest" %% "scalatest" % "3.1.0" % Test
}
