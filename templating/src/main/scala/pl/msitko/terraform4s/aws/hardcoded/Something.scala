package pl.msitko.terraform4s.aws.hardcoded

//import pl.msitko.terraform4s._
//final case class AwsKinesisStreamOut(arn: OutStringVal, id: OutStringVal)
//
//final case class AwsKinesisStream(
//    name: OutStringVal,
//    shard_count: OutVal[Int],
//    encryption_type: Option[OutStringVal],
//    enforce_consumer_deletion: Option[OutVal[Boolean]],
//    kms_key_id: Option[OutStringVal],
//    retention_period: Option[OutVal[Int]],
//    shard_level_metrics: Option[OutVal[Set[String]]],
//    tags: Option[OutVal[Map[String, String]]])(implicit r: ProvidersRoot)
//    extends Resource[AwsKinesisStreamOut](r) {
//
//  override def out =
//    AwsKinesisStreamOut(OutStringVal(schemaName, resourceName, "arn"), OutStringVal(schemaName, resourceName, "id"))
//  override def fields: List[Field] = List(Field("name", name), Field("shard_count", shard_count))
//
//  override def optionalFields: List[Option[Field]] =
//    List(
//      encryption_type.map(i => Field("encryption_type", i)),
//      enforce_consumer_deletion.map(i => Field("enforce_consumer_deletion", i)),
//      kms_key_id.map(i => Field("kms_key_id", i)),
//      retention_period.map(i => Field("retention_period", i)),
//      shard_level_metrics.map(i => Field("shard_level_metrics", i)),
//      tags.map(i => Field("tags", i)))
//  override def schemaName: String = "AwsKinesisStream"
//}
