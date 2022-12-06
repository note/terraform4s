package pl.msitko.terraform4s

import scala.annotation.tailrec

abstract class Resource[T](root: ProvidersRoot) {
  val resourceName: String = root.add(this)

  def out: T

  // Those 3 internal values are prepended with underscores to avoid name clashes with whatever attributes that
  // are defined in terraform provider
  def __fields: List[Field]                 // used only by terraform generator, should not be used at DSL level
  def __optionalFields: List[Option[Field]] // used only by terraform generator, should not be used at DSL level
  def __schemaName: String                  // e.g. aws_kinesis_stream, useful only for generating .tf.json
}

final case class Field(originalName: String, value: Val[_])

sealed trait TypedValue

// Should I use TypedTag?
// I should start with modelling TypeValues first
//final case class TypedString(v: Val[String]) extends TypedValue
//// revisit Int as HCL number covers both integral and floating point numbers
//final case class TypedNumber(v: Val[Int])   extends TypedValue
//final case class TypedBool(v: Val[Boolean]) extends TypedValue
// TODO: How to represent Set/Map?
