package pl.msitko.terraform4s.provider.ast

import pl.msitko.terraform4s.common.UnitSpec

class TransformationsSpec extends UnitSpec {
  "camelCaseAttributes" should {
    "work" in {
      // format: off
      val in = ProviderSchema(
        provider_schemas = Map("abc" -> Provider(
          resource_schemas = Map("a" -> Resource(
            version = 1,
            block = Block(
              attributes = List(
                "abc" -> attr(HCLString),
                "abc_def_ghc" -> attr(HCLString),
                "obj" -> attr(HCLObject(List(
                  "abc_xyz" -> HCLNumber
                ))),
                "obj2" -> attr(HCLList(HCLMap(HCLSet(HCLObject(List(
                  "abc_def_xyz" -> HCLNumber
                )))))),
                // object defined in terms of other object - I am not even sure it's allowed according to HCL spec
                "nested_obj" -> attr(HCLObject(List("abc_abc" -> HCLObject(List("def_def" -> HCLString)))))
              )
            )
          ))
        ))
      )
      // format: on

      val res = Transformations.camelCaseAttributes(in)

      // format: off
      assert(
        res === ProviderSchema(
          provider_schemas = Map("abc" -> Provider(
            resource_schemas = Map("a" -> Resource(
              version = 1,
              block = Block(
                attributes = List(
                  "abc" -> attr(HCLString),
                  "abcDefGhc" -> attr(HCLString),
                  "obj" -> attr(HCLObject(List(
                    "abcXyz" -> HCLNumber
                  ))),
                  "obj2" -> attr(HCLList(HCLMap(HCLSet(HCLObject(List(
                    "abcDefXyz" -> HCLNumber
                  )))))),
                  "nestedObj" -> attr(HCLObject(List("abcAbc" -> HCLObject(List("defDef" -> HCLString)))))
                )
              )
            ))
          ))
        )
      )
      // format: on
    }

//    ProviderSchema(Map(abc -> Provider(Map(a -> Resource(1,Block(List((abc,Attribu
    //    teValue(pl.msitko.terraform4s.provider.ast.HCLString$@c22644e,None,None,None)), (abcDefGhc,AttributeValue(pl.msitko.terraform4s.provider.ast.HCLString$@c22644e,None,None,None)), (obj,AttributeValue(HCLObject(List((abcXyz,pl.msitko.terraform4s.provider.ast.HCLNumber$@74c8c3a2))),None,None,None)), (obj2,AttributeValue(HCLList(HCLMap(HCLSet(HCLObject(List((abcDefXyz,pl.msitko.terraform4s.provider.ast.HCLNumber$@74c8c3a2)))))),None,None,None)), (nestedObj,AttributeValue(HCLObject(List((abcAbc,HCLObject(List((defDef,pl.msitko.terraform4s.provider.ast.HCLString$@c22644e)))))),None,None,None)))))))))
  }

  def attr(tpe: HCLType) = AttributeValue(tpe, None, None, None)
}
