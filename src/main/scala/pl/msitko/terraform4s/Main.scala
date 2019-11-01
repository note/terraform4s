package pl.msitko.terraform4s

import scala.meta._

// TODO: to remove
object Main {

  def main(arr: Array[String]): Unit = {
    println("here")

    val program =
      """package hello.there
        |final case class Here(a: Int, b: Option[String], c: Boolean, d: Any)
        |final case class Everywhere(a: Int, b: Option[Here])
        |""".stripMargin

    val tree = program.parse[Source].get.structure
    println(tree)
  }
}
