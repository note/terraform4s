package pl.msitko.terraform4s.aws.hardcoded

import pl.msitko.terraform4s._

final case class DynamoResourceOut(output1: OutStringVal, output2: OutVal[Boolean])

final case class DynamoResource(input1: Val[String], input2: Val[Boolean], input3: Option[Val[String]] = None)(
    implicit r: ProvidersRoot)
    extends Resource[DynamoResourceOut](r) {

  def out = DynamoResourceOut(
    OutStringVal(schemaName, resourceName, "output_1"),
    OutVal[Boolean](schemaName, resourceName, "output_2")
  )

  override def fields: List[Field] = List(
    Field("input_1", input1),
    Field("input_2", input2)
  )

  override def optionalFields: List[Option[Field]] = List(
    input3.map(i => Field("input_3", i))
  )

  override def schemaName: String = "aws_dynamodb_table"
}

final case class KinesisResource(input1: Val[String], input2: Val[Boolean])(implicit r: ProvidersRoot)
    extends Resource[Unit](r) {

  override def out: Unit = ()

  override def fields: List[Field] = List(
    Field("input_1", input1),
    Field("input_2", input2)
  )

  override def optionalFields: List[Option[Field]] = List.empty

  override def schemaName: String = "aws_kinesis_stream"
}

final case class S3Bucket(bucket: Val[String], acl: Val[String])(implicit r: ProvidersRoot) extends Resource[Unit](r) {

  override def out: Unit = ()

  override def fields: List[Field] = List(
    Field("bucket", bucket),
    Field("acl", acl)
  )

  override def optionalFields: List[Option[Field]] = List.empty

  // https://www.terraform.io/docs/providers/aws/r/s3_bucket.html
  override def schemaName: String = "aws_s3_bucket"
}
