package pl.msitko.terraform4s.aws.kinesis

import scala.concurrent.duration._
import enumeratum._

final case class KinesisStream(arguments: KinesisStreamArguments, attributes: KinesisStreamAttributes)

// https://www.terraform.io/docs/providers/aws/r/kinesis_stream.html
final case class KinesisStreamArguments(
                                         name: String,
                                         shardCount: Int,
                                         retentionPeriod: Duration = 24.hours,
                                         // https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/kinesis/model/MetricsName.html ?
                                         shardLevelMetrics: List[String] = List.empty,
                                         enforceConsumerDeletion: Boolean = false,
                                         encryption: Option[Encryption],
                                         tags: Set[String]
                                       )

// TODO: more precise types needed
final case class KinesisStreamAttributes(
                                          id: String,
                                          name: String,
                                          shardCount: String,
                                          arn: String
                                        )




sealed trait EncryptionType extends EnumEntry
object EncryptionType extends Enum[EncryptionType] {
  val values = findValues

  case object None extends EncryptionType
  case object KMS extends EncryptionType
}

final case class Encryption(`type`: EncryptionType, kmsKeyId: String)


