package pl.msitko.terraform4s.codegen

import pl.msitko.terraform4s.provider.ast.HCLObject

trait CodegenContext {
  def getNextAnonymousClassName: String
  def registerAnonymousClass(className: String, classInput: HCLObject): Unit
  def getNameOf(classInput: HCLObject): Option[String]
  def out: String
}

class DefaultCodegenContext extends CodegenContext {
  private var map: Map[HCLObject, String] = Map.empty

  // TODO: should be in scope of some package
  override def getNextAnonymousClassName: String = s"Anonymous${map.size}"

  override def registerAnonymousClass(className: String, classInput: HCLObject): Unit =
    map += (classInput -> className)

  override def getNameOf(classInput: HCLObject): Option[String] =
    map.get(classInput)

  override def out: String = map.toString()
}
