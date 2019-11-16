package pl.msitko.terraform4s

import io.circe.Json

sealed trait Val[T] {
  // that's the fundamental problem here
  // as we don't have output value available during templating phase it will never be available on scala side
  // Thus, there's no way to implement `map` generically
  // At best we can try to express all HCL abilities as a scala AST and then try to generate HCL expressions out of this
  // While doable it would require a lot of effort
  //  def map(fn: T => T): Val[T]

  def toTerraform: Json
}

// abstract class or trait?
abstract class InVal[T](v: T) extends Val[T]

// is type T useful here?
trait OutValBase[T] extends Val[T]

// in `${aws_route53_zone.primary.zone_id}`:
// schemaName = aws_route53_zone
// resourceName = primary
// fieldName = zone_id
abstract class CommonOutVal[T] extends OutValBase[T] {
  def schemaName: String
  def resourceName: String
  def fieldName: String

  def toTerraform: Json = Json.fromString(s"$${$schemaName.$resourceName.$fieldName}")
}

final case class OutVal[T](schemaName: String, resourceName: String, fieldName: String) extends CommonOutVal[T]

final case class OutStringVal(schemaName: String, resourceName: String, fieldName: String)
    extends CommonOutVal[String] {
  // This is added to demonstrate how we can implement `supported subset of map`
  // As you see it requires quite a lot of code as we need to express result of each operation as separate
  // data type, in this case OutAppendedStringVal
  def append(s: String): Val[String] = OutAppendedStringVal(schemaName, resourceName, fieldName, s)
}

final case class OutAppendedStringVal(schemaName: String, resourceName: String, fieldName: String, appendedPart: String)
    extends OutValBase[String] {
  override def toTerraform: Json = Json.fromString(s"$${$schemaName.$resourceName.$fieldName}$appendedPart")
}

object Val {

  implicit def fromString(str: String): InVal[String] = new InVal[String](str) {
    // https://www.terraform.io/docs/configuration/expressions.html#string
    override def toTerraform: Json = Json.fromString(str)
  }

  implicit def fromBoolean(b: Boolean): InVal[Boolean] = new InVal[Boolean](b) {
    // https://www.terraform.io/docs/configuration/expressions.html#bool
    override def toTerraform: Json = Json.fromBoolean(b)
  }
}
