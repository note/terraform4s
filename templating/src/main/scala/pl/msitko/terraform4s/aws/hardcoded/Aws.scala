package pl.msitko.terraform4s.aws.hardcoded

import java.util.UUID

import pl.msitko.terraform4s.{
  Field,
  OutStringVal,
  OutVal,
  PartialDynamoResourceOut,
  PartialOutStringVal,
  PartialOutVal,
  PartialResourceOut,
  ProvidersRoot,
  Resource,
  TypedBool,
  TypedString,
  Val
}

final case class DynamoResourceOut(output1: OutStringVal, output2: OutVal[Boolean])

final case class DynamoResource(input1: Val[String], input2: Val[Boolean])(implicit r: ProvidersRoot)
    extends Resource[DynamoResourceOut](r) {

  def out = DynamoResourceOut(
    OutStringVal(schemaName, resourceName, "output_1"),
    OutVal[Boolean](schemaName, resourceName, "output_2")
  )

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

final case class KinesisResource(input1: Val[String], input2: Val[Boolean])(implicit r: ProvidersRoot)
    extends Resource[PartialUnitResourceOut.type](r) {
  override def out = PartialUnitResourceOut

  override def fields: List[Field] = List(
    Field("input_1", TypedString(input1)),
    Field("input_2", TypedBool(input2))
  )

  override def schemaName: String = "aws_kinesis_stream"
}

final case class S3Bucket(bucket: Val[String], acl: Val[String])(implicit r: ProvidersRoot)
    extends Resource[PartialUnitResourceOut.type](r) {
  override def out = PartialUnitResourceOut

  override def fields: List[Field] = List(
    Field("bucket", TypedString(bucket)),
    Field("acl", TypedString(acl))
  )

  // https://www.terraform.io/docs/providers/aws/r/s3_bucket.html
  override def schemaName: String = "aws_s3_bucket"
}
//class Aws {
//  def s3Bucket(bucket: Val[String], acl: Val[String])(implicit providersRoot: ProvidersRoot): S3Bucket = {
//    val r = S3Bucket(bucket, acl)
//    providersRoot.add(r)
//    r
//  }
//
//  def s3BucketEffectless(bucket: Val[String], acl: Val[String]): S3Bucket =
//    S3Bucket(bucket, acl)
//}
//
//object Aws {
//
//}
