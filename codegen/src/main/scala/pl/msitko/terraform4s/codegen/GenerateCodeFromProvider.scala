package pl.msitko.terraform4s

import java.io.File

import pl.msitko.terraform4s.codegen.Codegen
import pl.msitko.terraform4s.provider.ast._
import pl.msitko.terraform4s.provider.json._

/**
  * Generates scala code out of `terraform providers schema -json`
  */
object GenerateCodeFromProvider {

  def main(args: Array[String]): Unit = {
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

    val resourcesOutput = json.as[ProviderSchema] match {
      case Right(v) => v
      case Left(e) =>
        println(s"$inputFilePath JSON cannot be parsed as TerraformResourcesOutput: $e")
        println(e.getMessage())
        println(e.getCause())
        e.printStackTrace()
        sys.exit(1)
    }

    println(s"$inputFile parsed as TerraformResourcesOutput")
    println(resourcesOutput.provider_schemas.get("aws").get.resource_schemas.size)
    val kinesisStream: Resource =
      resourcesOutput.provider_schemas.get("aws").get.resource_schemas.get("aws_kinesis_stream").get

    val classDefs = Codegen.fromResource("AwsKinesisStream", kinesisStream, Map.empty)
//    println(s"structure: ${classDefs.structure}")
//    println(s"show: ${classDefs.show}")
    println(s"syntax2 : ${classDefs.head.syntax}")
//    println(s"structure2: ${classDefs.head.structure}")
//    println(s"show 2: ${classDefs.head.show}")
  }
}
