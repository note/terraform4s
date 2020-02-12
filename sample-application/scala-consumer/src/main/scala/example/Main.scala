package example

import software.amazon.kinesis.coordinator.Scheduler
import software.amazon.kinesis.retrieval.polling.PollingConfig
import java.util.UUID

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.kinesis.common.ConfigsBuilder
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient
import software.amazon.kinesis.common.KinesisClientUtil
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient
import software.amazon.awssdk.services.s3.S3Client

object Main {

  def main(args: Array[String]): Unit = {
    println("Hello world!")

    kinesis("terraform4s-example", Region.EU_CENTRAL_1)
  }

  def kinesis(streamName: String, region: Region) = {
    val provider = ProfileCredentialsProvider.builder().profileName("michal").build()

    val kinesisClient = {
      val builder = KinesisAsyncClient.builder.region(region).credentialsProvider(provider)
      KinesisClientUtil.createKinesisAsyncClient(builder)
    }
    val dynamoClient     = DynamoDbAsyncClient.builder.region(region).credentialsProvider(provider).build()
    val cloudWatchClient = CloudWatchAsyncClient.builder.region(region).credentialsProvider(provider).build

    val s3Ops = {
      val s3 = S3Client.builder.region(region).credentialsProvider(provider).build
      new S3Operations(s3, "terraform4s-example-bucket")
    }

    val configsBuilder = new ConfigsBuilder(
      streamName,
      streamName,
      kinesisClient,
      dynamoClient,
      cloudWatchClient,
      UUID.randomUUID.toString,
      new RecordProcessorFactory(s3Ops)).tableName("")

    val scheduler = new Scheduler(
      configsBuilder.checkpointConfig(),
      configsBuilder.coordinatorConfig(),
      configsBuilder.leaseManagementConfig(),
      configsBuilder.lifecycleConfig(),
      configsBuilder.metricsConfig(),
      configsBuilder.processorConfig(),
      configsBuilder.retrievalConfig().retrievalSpecificConfig(new PollingConfig(streamName, kinesisClient))
    )

    println("hello a")
    val schedulerThread = new Thread(scheduler)
    schedulerThread.setDaemon(true)
    schedulerThread.start()
    println("hello b")
  }
}
