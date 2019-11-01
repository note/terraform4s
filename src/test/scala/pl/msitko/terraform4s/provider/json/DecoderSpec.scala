package pl.msitko.terraform4s.provider.json

import java.nio.ByteBuffer

import com.softwaremill.diffx.scalatest.DiffMatcher
import org.scalatest.{Matchers, WordSpec}
import io.circe.jawn.parseByteBuffer
import org.apache.commons.io.IOUtils
import pl.msitko.terraform4s.provider.ast._

class DecoderSpec extends WordSpec with Matchers with DiffMatcher {
  "ProviderSchema decoder" should {
    "work for sample file" in {
      val classloader = Thread.currentThread.getContextClassLoader
      val is = classloader.getResourceAsStream("part-of-aws-provider.json")
      val buffer = ByteBuffer.wrap(IOUtils.toByteArray(is))

      val json = parseByteBuffer(buffer).toOption.get

      val res = json.as[ProviderSchema].toOption.get

      val rschemas = res.provider_schemas.apply("aws").resource_schemas
      rschemas.size should equal(4)

      val expected = Resource(0,Block(
        List(
          ("certificate_arn", AttributeValue(HCLString, None, None, Some(true))),
          ("id", AttributeValue(HCLString, Some(true), Some(true), None)),
          ("validation_record_fqdns", AttributeValue(HCLSet(HCLString), Some(true), None, None))
        )
      ))

      rschemas.apply("aws_acm_certificate_validation") should equal(expected)
    }
  }
}
