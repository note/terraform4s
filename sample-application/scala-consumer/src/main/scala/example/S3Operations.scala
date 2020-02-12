package example

import java.nio.ByteBuffer

import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{PutObjectRequest, PutObjectResponse}

class S3Operations(s3: S3Client, bucket: String) {

  def put(key: String, b: ByteBuffer): PutObjectResponse = {
    val req = PutObjectRequest.builder().bucket(bucket).key(key).build()

    s3.putObject(req, RequestBody.fromByteBuffer(b))
  }
}
