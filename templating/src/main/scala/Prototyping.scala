import Example.dynamo

sealed trait TypedValue

final case class TypedString(v: Val[String]) extends TypedValue
// revisit Int as HCL number covers both integral and floating point numbers
final case class TypedNumber(v: Val[Int]) extends TypedValue
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

final case class InVal[T](v: T) extends Val[T]
abstract class OutVal[T](fieldName: String) extends Val[T]
final case class OutStringVal(fieldName: String) extends OutVal[String](fieldName)

object OutVal {
  def create[T](fieldName: String) = new OutVal[T](fieldName) {}
}

object Val {
  implicit def fromString(s: String): InVal[String] = new InVal[String](s)
  implicit def fromBoolean(b: Boolean): InVal[Boolean] = new InVal[Boolean](b)
}



final case class NamedResource[T](name: String, resource: Resource[T]) {
  def out = resource.out
}


final case class DynamoResourceOut(output1: OutVal[String], output2: OutVal[Boolean])
final case class DynamoResource(input1: Val[String], input2: Val[Boolean]) extends Resource[DynamoResourceOut] {
//  type OutT = DynamoResourceOut
  def out = DynamoResourceOut(OutStringVal("output_1"), OutVal.create[Boolean]("output_2"))

  override def fields: List[Field] = List(
    Field("input_1", TypedString(input1)),
    Field("input_2", TypedBool(input2))
  )

  override def schemaName: String = "aws_dynamodb_table"
}

final case class KinesisResource(input1: Val[String], input2: Val[Boolean]) extends Resource[Unit] {
  override def out: Unit = Unit

  override def fields: List[Field] = List(
    Field("input_1", TypedString(input1)),
    Field("input_2", TypedBool(input2))
  )

  override def schemaName: String = "aws_kinesis_stream"
}

object Example {
  val dynamo = NamedResource("animals", DynamoResource("something", false))

  val a = dynamo.out
  dynamo.out.output1
  val kinesis = NamedResource("animals_stream", KinesisResource(dynamo.out.output1, false))
}


