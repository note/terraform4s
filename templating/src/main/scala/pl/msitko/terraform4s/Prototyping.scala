package pl.msitko.terraform4s

import java.util.UUID

sealed trait TypedValue

final case class TypedString(v: Val[String]) extends TypedValue
// revisit Int as HCL number covers both integral and floating point numbers
final case class TypedNumber(v: Val[Int])   extends TypedValue
final case class TypedBool(v: Val[Boolean]) extends TypedValue

final case class Field(originalName: String, value: TypedValue)

class ProvidersRoot {
  private var resources = Vector.empty[Resource[_]]

  private def generateUniqueResourceName(schemaName: String): String =
    UUID.randomUUID().toString // TODO: change it to sth sensible

  def add(r: Resource[_]): String = {
    val resourceName = generateUniqueResourceName(r.schemaName)
    resources = resources.prepended(r)
    resourceName
  }

  def getJson = Encoding.encode(resources)
}

object ProvidersRoot {
  implicit lazy val Default: ProvidersRoot = new ProvidersRoot
}

abstract class Resource[T](root: ProvidersRoot) {
  val resourceName: String = root.add(this)

  def out: T
  def fields: List[Field]
  def schemaName: String // e.g. aws_kinesis_stream, useful only for generating .tf.json
}

sealed trait Val[T] {
  // that's the fundamental problem here
  // as we don't have output value available during templating phase it will never be available on scala side
  // Thus, there's no way to implement `map` generically
  // At best we can try to express all HCL abilities as a scala AST and then try to generate HCL expressions out of this
  // While doable it would require a lot of effort
//  def map(fn: T => T): Val[T]
}

final case class InVal[T](v: T) extends Val[T]

// is type T useful here?
trait OutValBase[T] extends Val[T] {
  def resolve: String
}

// in `${aws_route53_zone.primary.zone_id}`:
// schemaName = aws_route53_zone
// resourceName = primary
// fieldName = zone_id
abstract class CommonOutVal[T] extends OutValBase[T] {
  def schemaName: String
  def resourceName: String
  def fieldName: String

  def resolve: String = s"$${$schemaName.$resourceName.$fieldName}"
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
  override def resolve: String = s"$${$schemaName.$resourceName.$fieldName}$appendedPart"
}

abstract class PartialOutVal[T](val fieldName: String)

final case class PartialOutStringVal(override val fieldName: String) extends PartialOutVal[String](fieldName) {}

object PartialOutVal {
  def create[T](fieldName: String) = new PartialOutVal[T](fieldName) {}
}

object Val {
  implicit def fromString(s: String): InVal[String]    = new InVal[String](s)
  implicit def fromBoolean(b: Boolean): InVal[Boolean] = new InVal[Boolean](b)
}

// TODO: is name really needed? Probably can be generated and end user does not care about exact name
//final case class NamedResource[Out <: PartialResourceOut](name: String, resource: Resource[Out]) {
//  def out: Out#Resolved = resource.resolveOut(resource.schemaName, name)
//}
