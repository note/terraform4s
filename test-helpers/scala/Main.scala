package example

import io.circe.Printer
import pl.msitko.aws.{AwsDynamodbTable, AwsKinesisStream, AwsS3Bucket}
import pl.msitko.terraform4s.ProvidersRoot
import java.nio.file.{Paths, Files}
import java.nio.charset.StandardCharsets

object Main {
  def main(args: Array[String]): Unit = {
    import ProvidersRoot.Default

    val kinesisStream = AwsKinesisStream("terraform4s-example", 2)
    AwsS3Bucket(bucket = Some(kinesisStream.out.name), acl = Some("private"))

    val res = ProvidersRoot.Default.getJson.printWith(Printer.spaces2)

    Files.write(Paths.get("main.tf.json"), res.getBytes(StandardCharsets.UTF_8))
  }
}
