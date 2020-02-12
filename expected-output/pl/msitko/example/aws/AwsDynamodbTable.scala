/**
  * This file has been generated by terraform4s at 2020-01-15T00:00:00Z
  * Terraform version: 0.12.19
  * Terraform provider name: aws
  * Terraform provider version: 2.43.0
  */
package pl.msitko.example.aws

import pl.msitko.terraform4s._

final case class AwsDynamodbTableOut(
    hashKey: Val[String],
    name: Val[String],
    id: Val[String],
    streamViewType: Val[String],
    billingMode: Val[Option[String]],
    rangeKey: Val[Option[String]],
    readCapacity: Val[Option[Int]],
    streamEnabled: Val[Option[Boolean]],
    tags: Val[Option[Map[String, String]]],
    writeCapacity: Val[Option[Int]],
    arn: Val[String],
    streamArn: Val[String],
    streamLabel: Val[String])

final case class AwsDynamodbTable(
    hashKey: Val[String],
    name: Val[String],
    id: Option[Val[String]] = None,
    streamViewType: Option[Val[String]] = None,
    billingMode: Option[Val[String]] = None,
    rangeKey: Option[Val[String]] = None,
    readCapacity: Option[Val[Int]] = None,
    streamEnabled: Option[Val[Boolean]] = None,
    tags: Option[Val[Map[String, String]]] = None,
    writeCapacity: Option[Val[Int]] = None)(implicit r: ProvidersRoot)
    extends Resource[AwsDynamodbTableOut](r) {

  override def out =
    AwsDynamodbTableOut(
      OutStringVal(schemaName, resourceName, "hashKey"),
      OutStringVal(schemaName, resourceName, "name"),
      OutStringVal(schemaName, resourceName, "id"),
      OutStringVal(schemaName, resourceName, "streamViewType"),
      OutVal[Option[String]](schemaName, resourceName, "billingMode"),
      OutVal[Option[String]](schemaName, resourceName, "rangeKey"),
      OutVal[Option[Int]](schemaName, resourceName, "readCapacity"),
      OutVal[Option[Boolean]](schemaName, resourceName, "streamEnabled"),
      OutVal[Option[Map[String, String]]](schemaName, resourceName, "tags"),
      OutVal[Option[Int]](schemaName, resourceName, "writeCapacity"),
      OutStringVal(schemaName, resourceName, "arn"),
      OutStringVal(schemaName, resourceName, "streamArn"),
      OutStringVal(schemaName, resourceName, "streamLabel"))
  override def fields: List[Field] = List(Field("hashKey", hashKey), Field("name", name))

  override def optionalFields: List[Option[Field]] =
    List(
      id.map(i => Field("id", i)),
      streamViewType.map(i => Field("streamViewType", i)),
      billingMode.map(i => Field("billingMode", i)),
      rangeKey.map(i => Field("rangeKey", i)),
      readCapacity.map(i => Field("readCapacity", i)),
      streamEnabled.map(i => Field("streamEnabled", i)),
      tags.map(i => Field("tags", i)),
      writeCapacity.map(i => Field("writeCapacity", i)))
  override def schemaName: String = "AwsDynamodbTable"
}
