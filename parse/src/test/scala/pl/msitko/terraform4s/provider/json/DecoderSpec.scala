package pl.msitko.terraform4s.provider.json

import java.nio.ByteBuffer

import io.circe.Json
import io.circe.jawn.parseByteBuffer
import io.circe.parser.parse
import org.apache.commons.io.IOUtils
import pl.msitko.terraform4s.common.UnitSpec
import pl.msitko.terraform4s.provider.ast._

class DecoderSpec extends UnitSpec {
  "ProviderSchema decoder" should {
    "work for sample file" in {
      val classloader = Thread.currentThread.getContextClassLoader
      // part-of-aws-provider.json is part of output of `terraform providers schema -json` for
      // Terraform v0.12.12, provider.aws v2.33.0
      val is     = classloader.getResourceAsStream("part-of-aws-provider.json")
      val buffer = ByteBuffer.wrap(IOUtils.toByteArray(is))

      val json = parseByteBuffer(buffer).toOption.get

      val res = json.as[ProviderSchema].toOption.get

      val rschemas = res.provider_schemas.apply("aws").resource_schemas
      assert(rschemas.size === 4)

      val expected = Resource(
        0,
        Block(
          List(
            ("certificate_arn", AttributeValue(HCLString, None, None, Some(true))),
            ("id", AttributeValue(HCLString, Some(true), Some(true), None)),
            ("validation_record_fqdns", AttributeValue(HCLSet(HCLString), Some(true), None, None))
          )
        ))

      assert(rschemas.apply("aws_acm_certificate_validation") === expected)
    }
  }

  "HCLType decoder" should {
    "decode simple types" in {
      assert(Json.fromString("string").as[HCLType].toOption.get === HCLString)
      assert(Json.fromString("number").as[HCLType].toOption.get === HCLNumber)
      assert(Json.fromString("bool").as[HCLType].toOption.get === HCLBool)
      assert(Json.fromString("any").as[HCLType].toOption.get === HCLAny)
    }

    "decode collection types" in {
      val str = """[
                  |  "list",
                  |  [
                  |    "object",
                  |    {
                  |      "domain_name": "string",
                  |      "resource_record_name": "string",
                  |      "resource_record_type": "string",
                  |      "resource_record_value": "string"
                  |    }
                  |  ]
                  |]""".stripMargin

      val expected = HCLList(
        HCLObject(
          List(
            "domain_name"           -> HCLString,
            "resource_record_name"  -> HCLString,
            "resource_record_type"  -> HCLString,
            "resource_record_value" -> HCLString,
          )))
      assert(parse(str).toOption.get.as[HCLType].toOption.get === expected)
    }
  }

  // TODO: add cases for failure
  // TODO: add proper handling of `AttributeValue` attributes so we have ADT of required/input etc
}
