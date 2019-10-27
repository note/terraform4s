package pl.msitko.terraform4s

import java.io.File

import cats.data.NonEmptyList
import io.circe.Decoder.Result
import io.circe.Json.JString
import io.circe.{Decoder, DecodingFailure, HCursor, Json}
import io.circe.optics.JsonPath._
import io.circe.generic.semiauto._

import scala.collection.MapView
import scala.util.{Success, Try}

/**
 * Generates scala code out of `terraform providers schema -json`
 */
object GenerateCodeFromProvider {
  def main(args: Array[String]): Unit = {
    // inputFilePath is expected to be output of `terraform providers schema -json`
    val inputFilePath = args.headOption match {
      case Some(path) =>
        path
      case None =>
        println("Incorrect invocation: You have to provider input file name as first parameter")
        sys.exit(1)
    }

    val inputFile = new File(inputFilePath)
    val json = io.circe.jawn.parseFile(inputFile) match {
      case Right(json) =>
        json
      case Left(e) =>
        println(s"$inputFilePath cannot be parsed as JSON: $e")
        sys.exit(1)
    }

    val resourcesOutput = json.as[TerraformResourcesOutput] match {
      case Right(v) => v
      case Left(e) =>
        println(s"$inputFilePath JSON cannot be parsed as TerraformResourcesOutput: $e")
        sys.exit(1)
    }

    println(s"$inputFile parsed as TerraformResourcesOutput")
    println(resourcesOutput.provider_schemas.get("aws").get.resource_schemas.size)

  }
}



// Those types are loosely defined here, e.g. BasicAttributeType(name: String) instead of 2 types: String, Number, Bool
// This is not to be too strict
final case class TerraformResourcesOutput(provider_schemas: Map[String, Provider])

object TerraformResourcesOutput {
  import HCLType._

  implicit lazy val attributeValueDecoder: Decoder[AttributeValue] = deriveDecoder[AttributeValue]
  implicit lazy val blockDecoder: Decoder[Block] = deriveDecoder[Block]
  implicit lazy val resourceDecoder: Decoder[Resource] = deriveDecoder[Resource]
  implicit lazy val providerDecoder: Decoder[Provider] = deriveDecoder[Provider]
  implicit lazy val decoder: Decoder[TerraformResourcesOutput] = deriveDecoder[TerraformResourcesOutput]
}

final case class Provider(resource_schemas: Map[String, Resource])
final case class Resource(version: Int, block: Block)
final case class Block(attributes: Map[String, AttributeValue])
final case class AttributeValue(
  `type`: HCLType,
  optional: Option[Boolean],
  computed: Option[Boolean],
  required: Option[Boolean]
)


// https://www.terraform.io/docs/configuration/types.html
sealed trait HCLType

// https://www.terraform.io/docs/configuration/types.html#primitive-types
sealed trait PrimitiveType extends HCLType
sealed trait CollectionType extends HCLType
// https://www.terraform.io/docs/configuration/types.html#structural-types
sealed trait StructuralType extends HCLType

final object HCLString extends PrimitiveType
final object HCLNumber extends PrimitiveType
final object HCLBool extends PrimitiveType
final object HCLAny extends PrimitiveType

final case class HCLList(`type`: HCLType) extends CollectionType
final case class HCLMap(`type`: HCLType) extends CollectionType
final case class HCLSet(`type`: HCLType) extends CollectionType

final case class HCLObject(attributes: Map[String, HCLType]) extends StructuralType
// there's not `tuple` type in output of `terraform provider` for AWS so leaving it out for future:
//final case class HCLTuple(`type`: HCLType) extends CollectionTypes


object HCLType {
  import cats.implicits._

  implicit lazy val decoder: Decoder[HCLType] = new Decoder[HCLType] {
    override def apply(c: HCursor): Result[HCLType] = {
      val primitiveTypePf: String => Either[DecodingFailure, PrimitiveType] = {
        case "string" => HCLString.asRight[DecodingFailure]
        case "number" => HCLNumber.asRight[DecodingFailure]
        case "bool"   => HCLBool.asRight[DecodingFailure]
        case "any"    => HCLAny.asRight[DecodingFailure]
        case _    => DecodingFailure("todo", c.history).asLeft[PrimitiveType]
      }

      val nonPrimitive: String => HCursor => Either[DecodingFailure, HCLType] = {
        case "list" => hcursor => this.apply(hcursor).map(HCLList.apply)
        case "map" => hcursor => this.apply(hcursor).map(HCLMap.apply)
        case "set" => hcursor => this.apply(hcursor).map(HCLSet.apply)
        case "object" => hcursor => hcursor.value.asObject match {
          case Some(obj) =>
            val v = obj.toMap.view.mapValues(tmp)
            if (v.forall(_._2.isRight)) {
              val r = v.mapValues(_.right.get)
              HCLObject(r.toMap).asRight[DecodingFailure]
            } else {
              DecodingFailure("todo7", hcursor.history).asLeft[HCLType]
            }
          case None => DecodingFailure("todo2", hcursor.history).asLeft[HCLType]
        }
      }

      c.value.asString.fold {
        try {
          val first = c.downN(0).focus.get.asString.get
          val second = c.downN(1).success.get
          nonPrimitive(first)(second)
        } catch {
          case e: Throwable =>
            println(c.history)
            println(e)
            e.printStackTrace()
            throw new RuntimeException("when: " + c.value)
        }
      }(primitiveTypePf)
    }

  }

  val listS = Json.fromString("list")
  val mapS = Json.fromString("map")
  val setS = Json.fromString("set")
  val objectS = Json.fromString("object")

  // TODO: non tailrec recursion
  def tmp(json: Json): Either[String, HCLType] =
    json.asString match {
      case Some("string") => HCLString.asRight[String]
      case Some("number") => HCLNumber.asRight[String]
      case Some("bool") => HCLBool.asRight[String]
      case Some("any") => HCLAny.asRight[String]
      case Some(_)    => "todo3".asLeft[HCLType]
      case None =>
        json.asArray.map(_.toList) match {
          case Some(`listS` :: tpe :: Nil) => tmp(tpe).map(HCLList.apply)
          case Some(`mapS` :: tpe :: Nil) => tmp(tpe).map(HCLMap.apply)
          case Some(`setS` :: tpe :: Nil) => tmp(tpe).map(HCLSet.apply)
          case Some(`objectS` :: tpe :: Nil) =>
            tpe.asObject match {
              case Some(obj) =>
                val v1: MapView[String, Either[String, HCLType]] = obj.toMap.view.mapValues(tmp)
                if (v1.forall(_._2.isRight)) {
                  val r = v1.mapValues(_.right.get)
                  HCLObject(r.toMap).asRight[String]
                } else {
                  "todo6".asLeft[HCLType]
                }
              case None => "todo5".asLeft[HCLType]
            }
        }
    }

//  implicit lazy val decoder: Decoder[AttributeType] =
//    Decoder.decodeNonEmptyList[String].or(Decoder.decodeString.map(s => NonEmptyList.of(s))).map(AttributeType.apply)
}
