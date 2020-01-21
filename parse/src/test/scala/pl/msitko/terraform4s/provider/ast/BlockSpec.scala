package pl.msitko.terraform4s.provider.ast

import pl.msitko.terraform4s.common.UnitSpec

class BlockSpec extends UnitSpec {

  val in = Block(
    List(
      "requiredInput0" -> attr(required = Some(true)),
      // not very realistic example, if computed is set it's always set to true
      "requiredInput1"            -> attr(required = Some(true), computed = Some(false)),
      "optionalInput0"            -> attr(optional = Some(true), computed = Some(true)),
      "optionalNonComputedInput0" -> attr(optional = Some(true)),
      "optionalNonComputedInput1" -> attr(optional = Some(true), computed = Some(false)),
      // not very realistic example, if optional is set it's always set to true
      "nonInput0" -> attr(computed = Some(true), optional = Some(false)),
      "nonInput1" -> attr(computed = Some(true))
    ))

  "requiredInputs" should {
    "work" in {
      assert(in.requiredInputs.map(_._1).toSet === Set("requiredInput0", "requiredInput1"))
    }
  }

  "optionalInputs" should {
    "work" in {
      assert(in.optionalInputs.map(_._1).toSet === Set("optionalInput0"))
    }
  }

  "optionalNonComputedInputs" should {
    "work" in {
      assert(
        in.optionalNonComputedInputs.map(_._1).toSet === Set("optionalNonComputedInput0", "optionalNonComputedInput1"))
    }
  }

  "nonInput" should {
    "work" in {
      assert(in.nonInputs.map(_._1).toSet === Set("nonInput0", "nonInput1"))
    }
  }

  def attr(
      optional: Option[Boolean] = None,
      computed: Option[Boolean] = None,
      required: Option[Boolean] = None
  ): AttributeValue =
    AttributeValue(HCLString, optional, computed, required)

}
