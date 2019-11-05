package pl.msitko.terraform4s

sealed trait TypedValue

final case class TypedString(v: Val[String]) extends TypedValue
// revisit Int as HCL number covers both integral and floating point numbers
final case class TypedNumber(v: Val[Int])   extends TypedValue
final case class TypedBool(v: Val[Boolean]) extends TypedValue

final case class Field(originalName: String, value: TypedValue)

abstract class Resource[T] {
//  type OutT
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

final case class InVal[T](v: T)             extends Val[T]
abstract class OutVal[T](fieldName: String) extends Val[T]

final case class OutStringVal(fieldName: String) extends OutVal[String](fieldName) {
  // This is added to demonstrate how we can implement `supported subset of map`
  // As you see it requires quite a lot of code as we need to express result of each operation as separate
  // data type, in this case OutAppendedStringVal
  def append(s: String): Val[String] = OutAppendedStringVal(fieldName, s)
}

final case class OutAppendedStringVal(fieldName: String, appendedPart: String) extends Val[String]

object OutVal {
  def create[T](fieldName: String) = new OutVal[T](fieldName) {}
}

object Val {
  implicit def fromString(s: String): InVal[String]    = new InVal[String](s)
  implicit def fromBoolean(b: Boolean): InVal[Boolean] = new InVal[Boolean](b)
}

// TODO: is name really needed? Probably can be generated and end user does not care about exact name
final case class NamedResource[T](name: String, resource: Resource[T]) {
  def out = resource.out
}
