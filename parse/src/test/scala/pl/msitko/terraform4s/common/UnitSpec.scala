package pl.msitko.terraform4s.common

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{Matchers, WordSpec}

abstract class UnitSpec extends WordSpec with Matchers with TypeCheckedTripleEquals
