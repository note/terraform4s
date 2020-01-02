package pl.msitko.terraform4s.codegen

import java.io.File
import java.nio.file.{Files, Paths}

import org.scalatest.{Matchers, WordSpec}
import pl.msitko.terraform4s.provider.ast.{ProviderSchema, Resource}
import org.apache.commons.io.IOUtils
import java.nio.charset.StandardCharsets

import pl.msitko.terraform4s.provider.json._

import scala.meta.Term

class ItSpec extends WordSpec with Matchers {
  "it" should {
    "work" in {
      val inputFile       = new File("output.json")
      val json            = io.circe.jawn.parseFile(inputFile).toOption.get
      val resourcesOutput = json.as[ProviderSchema].toOption.get

      val resourceSchemas: Map[String, Resource] = resourcesOutput.provider_schemas.get("aws").get.resource_schemas

      assert(resourceSchemas.size === 539)
      val simpleEnoughResource = resourceSchemas.get("aws_kinesis_stream").get

      val packageName = Term.Select(Term.Select(Term.Name("pl"), Term.Name("msitko")), Term.Name("example"))

      val source =
        Codegen.generateResource("AwsKinesisStream", simpleEnoughResource, packageName, new DefaultCodegenContext)

      import org.scalafmt.interfaces.Scalafmt
      val scalafmt = Scalafmt.create(this.getClass.getClassLoader)

      val conf = Paths.get(".scalafmt.conf")

      val actual = scalafmt.format(conf, Paths.get("whatever.scala"), source.syntax)

      val classloader = Thread.currentThread.getContextClassLoader
      val stream      = classloader.getResourceAsStream("expected")

      val expected = IOUtils.toString(stream, StandardCharsets.UTF_8)

      assert(actual === expected)
    }
  }
}
