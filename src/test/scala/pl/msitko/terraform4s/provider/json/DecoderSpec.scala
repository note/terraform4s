package pl.msitko.terraform4s.provider.json

import java.nio.ByteBuffer

import org.scalatest.WordSpec
import io.circe.jawn.parseByteBuffer
import org.apache.commons.io.IOUtils
import pl.msitko.terraform4s.provider.ast.ProviderSchema

class DecoderSpec extends WordSpec {
  "ProviderSchema decoder" should {
    "work for sample file" in {
      val classloader = Thread.currentThread.getContextClassLoader
      val is = classloader.getResourceAsStream("part-of-aws-provider.json")
      val buffer = ByteBuffer.wrap(IOUtils.toByteArray(is))

      val json = parseByteBuffer(buffer).toOption.get

      val res = json.as[ProviderSchema].toOption.get

      println(res)
    }
  }
}
