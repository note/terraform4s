package pl.msitko.terraform4s

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import io.circe.Printer

//trait ResourceBase {
//  type Resolved
//  def complete(schemaName: String, resourceName: String): Resolved
//}

trait PartialResourceOut {
  type Resolved
  def complete(schemaName: String, resourceName: String): Resolved
}

final case class PartialDynamoResourceOut(output1: PartialOutStringVal, output2: PartialOutVal[Boolean])
    extends PartialResourceOut {
  type Resolved = DynamoResourceOut

  override def complete(schemaName: String, resourceName: String): DynamoResourceOut =
    DynamoResourceOut(
      OutStringVal(schemaName, resourceName, output1.fieldName),
      OutVal[Boolean](schemaName, resourceName, output2.fieldName))
}

final case class DynamoResourceOut(output1: OutStringVal, output2: OutVal[Boolean])

final case class DynamoResource(input1: Val[String], input2: Val[Boolean])(implicit e: sourcecode.Enclosing)
    extends Resource[PartialDynamoResourceOut] {
  def out       = PartialDynamoResourceOut(PartialOutStringVal("output_1"), PartialOutVal.create[Boolean]("output_2"))
  def enclosing = e.value

  override def fields: List[Field] = List(
    Field("input_1", TypedString(input1)),
    Field("input_2", TypedBool(input2))
  )

  override def schemaName: String = "aws_dynamodb_table"
}

case object PartialUnitResourceOut extends PartialResourceOut {
  override type Resolved = Unit

  override def complete(schemaName: String, resourceName: String): Unit = ()
}

final case class KinesisResource(input1: Val[String], input2: Val[Boolean])
    extends Resource[PartialUnitResourceOut.type] {
  override def out = PartialUnitResourceOut

  override def fields: List[Field] = List(
    Field("input_1", TypedString(input1)),
    Field("input_2", TypedBool(input2))
  )

  override def schemaName: String = "aws_kinesis_stream"
}

final case class S3Bucket(bucket: Val[String], acl: Val[String]) extends Resource[PartialUnitResourceOut.type] {
  override def out = PartialUnitResourceOut

  override def fields: List[Field] = List(
    Field("bucket", TypedString(bucket)),
    Field("acl", TypedString(acl))
  )

  // https://www.terraform.io/docs/providers/aws/r/s3_bucket.html
  override def schemaName: String = "aws_s3_bucket"
}

object Example {
  val dynR    = DynamoResource("something", false)
  val dynR2   = DynamoResource("somethingElse", false)
  val dynamo  = NamedResource("animals", dynR)
  val dynamo2 = NamedResource("animalsElse", dynR2)

  println("dynamo: " + dynR.enclosing)
  println("dynamo 2: " + dynR2.enclosing)

  val kinesis =
    NamedResource("animals_stream", KinesisResource(dynamo.out.output1.append("-stream"), dynamo2.out.output2))

  def main(args: Array[String]): Unit = {
//    val bucket = NamedResource("s3_bucket_test", S3Bucket("terraform4s-test", "private"))
//
//    val output = Encoding.encode(List(bucket)).printWith(Printer.spaces2)
//    println(output)
//
//    Files.write(new File("bucket.tf.json").toPath, output.getBytes(StandardCharsets.UTF_8))

    val output = Encoding.encode(List(kinesis, dynamo, dynamo2)).printWith(Printer.spaces2)
    println(output)
  }
}
