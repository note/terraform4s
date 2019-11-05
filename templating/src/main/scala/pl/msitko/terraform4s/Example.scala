package pl.msitko.terraform4s

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import io.circe.Printer

final case class DynamoResourceOut(output1: OutStringVal, output2: OutVal[Boolean])

final case class DynamoResource(input1: Val[String], input2: Val[Boolean]) extends Resource[DynamoResourceOut] {
  def out = DynamoResourceOut(OutStringVal("output_1"), OutVal.create[Boolean]("output_2"))

  override def fields: List[Field] = List(
    Field("input_1", TypedString(input1)),
    Field("input_2", TypedBool(input2))
  )

  override def schemaName: String = "aws_dynamodb_table"
}

final case class KinesisResource(input1: Val[String], input2: Val[Boolean]) extends Resource[Unit] {
  override def out: Unit = ()

  override def fields: List[Field] = List(
    Field("input_1", TypedString(input1)),
    Field("input_2", TypedBool(input2))
  )

  override def schemaName: String = "aws_kinesis_stream"
}

final case class S3Bucket(bucket: Val[String], acl: Val[String]) extends Resource[Unit] {
  override def out: Unit = ()

  override def fields: List[Field] = List(
    Field("bucket", TypedString(bucket)),
    Field("acl", TypedString(acl))
  )

  // https://www.terraform.io/docs/providers/aws/r/s3_bucket.html
  override def schemaName: String = "aws_s3_bucket"
}

object Example {
  val dynamo = NamedResource("animals", DynamoResource("something", false))

  val a = dynamo.out
  dynamo.out.output1
  val kinesis = NamedResource("animals_stream", KinesisResource(dynamo.out.output1.append("-stream"), false))

  def main(args: Array[String]): Unit = {
    val bucket = NamedResource("s3_bucket_test", S3Bucket("terraform4s-test", "private"))

    val output = Encoding.encode(List(bucket)).printWith(Printer.spaces2)
    println(output)

    Files.write(new File("bucket.tf.json").toPath, output.getBytes(StandardCharsets.UTF_8))
  }
}
