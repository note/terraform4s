package pl.msitko.terraform4s

import pl.msitko.terraform4s.aws.hardcoded._
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

final case class Abc(a: Int, b: Boolean, c: String)(implicit d: Int)

object Example {

  def main(args: Array[String]): Unit = {

    import ProvidersRoot.Default
    val dynamo  = DynamoResource("something", false)
    val dynamo2 = DynamoResource("somethingElse", false)

    KinesisResource(dynamo.out.output1.append("-stream"), dynamo2.out.output2)
    S3Bucket("terraform4s-test", "private")

    println(ProvidersRoot.Default.getJson.printWith(Printer.spaces2))
//
//    val output = Encoding.encode(List(bucket)).printWith(Printer.spaces2)
//    println(output)
//
//    Files.write(new File("bucket.tf.json").toPath, output.getBytes(StandardCharsets.UTF_8))

//    val output = Encoding.encode(List(kinesis, dynamo, dynamo2)).printWith(Printer.spaces2)
//    println(output)
  }
}
