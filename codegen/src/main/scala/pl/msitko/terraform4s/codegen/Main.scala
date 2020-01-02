package pl.msitko.terraform4s

import java.io.File
import java.nio.file.Paths

import pl.msitko.terraform4s.codegen.{Codegen, DefaultCodegenContext}
import pl.msitko.terraform4s.provider.ast._
import pl.msitko.terraform4s.provider.json._
import org.scalafmt.interfaces.Scalafmt

import scala.meta.Term

/**
  * Generates scala code out of `terraform providers schema -json`
  */
object Main {

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

    val packageName = Term.Select(Term.Select(Term.Name("pl"), Term.Name("msitko")), Term.Name("example"))
    val ctx         = new DefaultCodegenContext
    val src         = Codegen.generateResource("AwsKinesisStream", kinesisStream, packageName, ctx)

    val scalafmt = Scalafmt.create(this.getClass.getClassLoader)

    val conf = Paths.get(".scalafmt.conf")

    val formatted = scalafmt.format(conf, Paths.get("whatever.scala"), src.syntax)
    println("--------- FORMATTED -------")
    println(formatted)
  }
}

import pl.msitko.terraform4s.Resource
final case class AwsKinesisStreamOut(arn: OutStringVal, id: OutStringVal)

final case class AwsKinesisStream(
    name: OutStringVal,
    shard_count: OutVal[Int],
    encryption_type: Option[OutStringVal],
    enforce_consumer_deletion: Option[OutVal[Boolean]],
    kms_key_id: Option[OutStringVal],
    retention_period: Option[OutVal[Int]],
    shard_level_metrics: Option[OutVal[Set[String]]],
    tags: Option[OutVal[Map[String, String]]])(implicit r: ProvidersRoot)
    extends Resource[AwsKinesisStreamOut](r) {

  override def out =
    AwsKinesisStreamOut(OutStringVal(schemaName, resourceName, "arn"), OutStringVal(schemaName, resourceName, "id"))
  override def fields: List[Field] = List(Field("name", name), Field("shard_count", shard_count))

  override def optionalFields: List[Option[Field]] =
    List(
      encryption_type.map(i => Field("encryption_type", i)),
      enforce_consumer_deletion.map(i => Field("enforce_consumer_deletion", i)),
      kms_key_id.map(i => Field("kms_key_id", i)),
      retention_period.map(i => Field("retention_period", i)),
      shard_level_metrics.map(i => Field("shard_level_metrics", i)),
      tags.map(i => Field("tags", i)))
  override def schemaName: String = "AwsKinesisStream"
}
