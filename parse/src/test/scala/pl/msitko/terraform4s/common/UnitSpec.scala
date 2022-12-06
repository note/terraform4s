package pl.msitko.terraform4s.common

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

abstract class UnitSpec extends AnyWordSpecLike with Matchers with TypeCheckedTripleEquals
