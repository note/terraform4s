package pl.msitko.terraform4s.provider

import cats.implicits._
import io.circe.Decoder.Result
import io.circe.generic.semiauto.deriveDecoder
import io.circe.{Decoder, DecodingFailure, HCursor, Json}
import pl.msitko.terraform4s.provider.ast._

package object json {

  implicit lazy val hclTypeDecoder: Decoder[HCLType] = new Decoder[HCLType] {

    override def apply(c: HCursor): Result[HCLType] = {
      val primitiveTypePf: String => Either[DecodingFailure, PrimitiveType] = {
        case "string" => HCLString.asRight[DecodingFailure]
        case "number" => HCLNumber.asRight[DecodingFailure]
        case "bool"   => HCLBool.asRight[DecodingFailure]
        case "any"    => HCLAny.asRight[DecodingFailure]
        case _        => DecodingFailure("todo", c.history).asLeft[PrimitiveType]
      }

      val nonPrimitive: String => HCursor => Either[DecodingFailure, HCLType] = {
        case "list" => hcursor => this.apply(hcursor).map(HCLList.apply)
        case "map"  => hcursor => this.apply(hcursor).map(HCLMap.apply)
        case "set"  => hcursor => this.apply(hcursor).map(HCLSet.apply)
        case "object" =>
          hcursor =>
            hcursor.value.asObject match {
              case Some(obj) =>
                val v = obj.toList.map(t => (t._1, tmp(t._2)))
                if (v.forall(_._2.isRight)) {
                  val r = v.map(t => (t._1, t._2.toOption.get))
                  HCLObject(r).asRight[DecodingFailure]
                } else {
                  DecodingFailure("todo7", hcursor.history).asLeft[HCLType]
                }
              case None => DecodingFailure("todo2", hcursor.history).asLeft[HCLType]
            }
      }

      c.value.asString.fold {
        try {
          val first  = c.downN(0).focus.get.asString.get
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

  private val listS   = Json.fromString("list")
  private val mapS    = Json.fromString("map")
  private val setS    = Json.fromString("set")
  private val objectS = Json.fromString("object")

  // TODO: non tailrec recursion
  def tmp(json: Json): Either[String, HCLType] = {
    json.asString match {
      case Some("string") => HCLString.asRight[String]
      case Some("number") => HCLNumber.asRight[String]
      case Some("bool")   => HCLBool.asRight[String]
      case Some("any")    => HCLAny.asRight[String]
      case Some(_)        => "todo3".asLeft[HCLType]
      case None =>
        json.asArray.map(_.toList) match {
          case Some(`listS` :: tpe :: Nil) => tmp(tpe).map(HCLList.apply)
          case Some(`mapS` :: tpe :: Nil)  => tmp(tpe).map(HCLMap.apply)
          case Some(`setS` :: tpe :: Nil)  => tmp(tpe).map(HCLSet.apply)
          case Some(`objectS` :: tpe :: Nil) =>
            tpe.asObject match {
              case Some(obj) =>
                val v1 = obj.toList.map(t => (t._1, tmp(t._2)))
                if (v1.forall(_._2.isRight)) {
                  val r = v1.map(t => (t._1, t._2.toOption.get))
                  HCLObject(r).asRight[String]
                } else {
                  "todo6".asLeft[HCLType]
                }
              case None => "todo5".asLeft[HCLType]
            }
          case w =>
            s"todo 634: $w".asLeft[HCLType]
        }
    }
  }

  implicit lazy val attributeValueDecoder: Decoder[AttributeValue] = deriveDecoder[AttributeValue]

  // derived decoder for Block does not handle `List[(String, Json)]` (it was working with Map[String,
  implicit lazy val blockDecoder: Decoder[Block] = new Decoder[Block] {

    override def apply(c: HCursor): Result[Block] = {
      c.downField("attributes").focus.flatMap(_.asObject) match {
        case Some(obj) =>
          val attrs = obj.toList.map(t => (t._1, t._2.as[AttributeValue]))
          if (attrs.forall(_._2.isRight)) {
            Block(attrs.map(t => (t._1, t._2.toOption.get))).asRight[DecodingFailure]
          } else {
            attrs.collectFirst {
              case (key, Left(decodingFailure)) =>
                DecodingFailure(s"Cannot decode Attribute value under $key within Block", decodingFailure.history)
                  .asLeft[Block]
            }.get
          }

        case None => DecodingFailure("Cannot decode Block", c.history).asLeft[Block]
      }
    }
  }

  implicit lazy val resourceDecoder: Decoder[Resource]             = deriveDecoder[Resource]
  implicit lazy val providerDecoder: Decoder[Provider]             = deriveDecoder[Provider]
  implicit lazy val providerSchemaDecoder: Decoder[ProviderSchema] = deriveDecoder[ProviderSchema]
}
