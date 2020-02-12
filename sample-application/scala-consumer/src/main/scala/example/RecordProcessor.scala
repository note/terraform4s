package example

import io.circe.jawn.parseByteBuffer
import software.amazon.kinesis.lifecycle.events._
import software.amazon.kinesis.processor.{ShardRecordProcessor, ShardRecordProcessorFactory}

import scala.jdk.CollectionConverters._
import scala.util.Random

class RecordProcessorFactory(s3Ops: S3Operations) extends ShardRecordProcessorFactory {
  override def shardRecordProcessor(): ShardRecordProcessor = new RecordProcessor(s3Ops)
}

class RecordProcessor(s3Ops: S3Operations) extends ShardRecordProcessor {

  override def initialize(initializationInput: InitializationInput): Unit =
    println("Initialized")

  override def processRecords(processRecordsInput: ProcessRecordsInput): Unit = {
    val now = System.currentTimeMillis()

    processRecordsInput.records().asScala.map { record =>
      parseByteBuffer(record.data) match {
        case Right(json) =>
          (for {
            obj <- json.asObject
            v   <- obj.apply("x")
            x   <- v.asNumber.flatMap(_.toInt)
          } yield x) match {
            case Some(x) if x % 2 == 0 =>
              val key = s"aggregated-$now-$x"
              println(s"putting to s3: $key")
              s3Ops.put(key, getRandomByteBuffer(10))
            case Some(_) =>
              println("Ignoring")
            case None =>
              println("Unexpected input")
          }
      }
      record.data
    }
  }

  override def leaseLost(leaseLostInput: LeaseLostInput): Unit =
    println("Lost lease")

  override def shardEnded(shardEndedInput: ShardEndedInput): Unit = {
    println("shard ended - checkpointing...")
    shardEndedInput.checkpointer().checkpoint()
  }

  override def shutdownRequested(shutdownRequestedInput: ShutdownRequestedInput): Unit = {
    println("shutdown requested - checkpointing...")
    shutdownRequestedInput.checkpointer().checkpoint()
  }

  import java.nio.ByteBuffer

  private def getRandomByteBuffer(size: Int): ByteBuffer = {
    val b = new Array[Byte](size)
    Random.nextBytes(b)
    ByteBuffer.wrap(b)
  }
}
