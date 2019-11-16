package pl.msitko.terraform4s.codegen

import java.io.File
import java.nio.file.Paths

import org.scalatest.{Matchers, WordSpec}
import pl.msitko.terraform4s.provider.ast.ProviderSchema
import org.apache.commons.io.IOUtils
import java.nio.charset.StandardCharsets
import pl.msitko.terraform4s.provider.json._

class ItSpec extends WordSpec with Matchers {
  "it" should {
    "work" in {
      val inputFile       = new File("output.json")
      val json            = io.circe.jawn.parseFile(inputFile).toOption.get
      val resourcesOutput = json.as[ProviderSchema].toOption.get

      val resourceSchemas = resourcesOutput.provider_schemas.get("aws").get.resource_schemas

      assert(resourceSchemas.size === 527)
      val simpleEnoughResource = resourceSchemas.get("aws_kinesis_stream").get

      val classDefs = Codegen.fromResource("AwsKinesisStream", simpleEnoughResource, Map.empty)
      classDefs.map(_.syntax).foreach(println)

      import org.scalafmt.interfaces.Scalafmt
      val scalafmt = Scalafmt.create(this.getClass.getClassLoader)

      val conf = Paths.get(".scalafmt.conf")

      import scala.meta._
      val imports = Import(
        List(
          Importer(
            Term.Select(Term.Select(Term.Name("pl"), Term.Name("msitko")), Term.Name("terraform4s")),
            List(Importee.Name(Name("Resource"))))))

      val src       = Source(List(imports) ++ classDefs)
      val formatted = scalafmt.format(conf, Paths.get("whatever.scala"), src.syntax)

      val classloader = Thread.currentThread.getContextClassLoader
      val stream      = classloader.getResourceAsStream("expected")

      val expected = IOUtils.toString(stream, StandardCharsets.UTF_8)

      assert(formatted === expected)
    }
  }
}
