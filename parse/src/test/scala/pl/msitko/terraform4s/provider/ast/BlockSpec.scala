package pl.msitko.terraform4s.provider.ast

import pl.msitko.terraform4s.common.UnitSpec

class BlockSpec extends UnitSpec {

  val in = Block(
    List(
      "requiredInput0"       -> attr(required = Some(true)),
      "requiredInput1"       -> attr(required = Some(true), computed = Some(false)),
      "optionalInput0"       -> attr(optional = Some(true)),
      "optionalInput1"       -> attr(optional = Some(true), computed = Some(false)),
      "alwaysPresentOutput0" -> attr(computed = Some(true), optional = Some(true)),
      "alwaysPresentOutput1" -> attr(computed = Some(true)),
      "optionalOutput0"      -> attr(computed = Some(true), optional = Some(false))
    ))

  "requiredInputs" should {
    "work" in {
      assert(in.requiredInputs.map(_._1).toSet === Set("requiredInput0", "requiredInput1"))
    }
  }

  "optionalInputs" should {
    "work" in {
      assert(in.optionalInputs.map(_._1).toSet === Set("optionalInput0", "optionalInput1"))
    }
  }

  "alwaysPresentOutputs" should {
    "work" in {
      assert(in.alwaysPresentOutputs.map(_._1).toSet === Set("alwaysPresentOutput0", "alwaysPresentOutput1"))
    }
  }

  "optionalOutputs" should {
    "work" in {
      assert(in.optionalOutputs.map(_._1).toSet === Set("optionalOutput0"))
    }
  }

  def attr(
      optional: Option[Boolean] = None,
      computed: Option[Boolean] = None,
      required: Option[Boolean] = None
  ): AttributeValue =
    AttributeValue(HCLString, optional, computed, required)

}
