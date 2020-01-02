package pl.msitko.terraform4s

import pl.msitko.terraform4s.aws.hardcoded._
import io.circe.Printer

import scala.meta.Term

object ExampleMain {

  def main(args: Array[String]): Unit = {

    import ProvidersRoot.Default
    val dynamo  = DynamoResource("something", false)
    val dynamo2 = DynamoResource("somethingElse", false)

    KinesisResource(dynamo.out.output1.append("-stream"), dynamo2.out.output2)
    S3Bucket("terraform4s-test", "private")

    println(ProvidersRoot.Default.getJson.printWith(Printer.spaces2))
//
//    val output = Encoding.encode(List(bucket)).printWith(Printer.spaces2)
//    println(output)
//
//    Files.write(new File("bucket.tf.json").toPath, output.getBytes(StandardCharsets.UTF_8))

//    val output = Encoding.encode(List(kinesis, dynamo, dynamo2)).printWith(Printer.spaces2)
//    println(output)
  }
}

object QuickCheck {
  import scala.meta._

  def main(args: Array[String]): Unit = {
    val r =
      """package pl.msitko.example
        |
        |import pl.msitko.terraform4s.Resource
        |final case class DynamoResourceOut(output1: OutStringVal, output2: OutVal[Boolean], input3: Option[Val[String]] = None)
        |
        |final case class DynamoResource(input1: Val[String], input2: Val[Boolean])(implicit r: ProvidersRoot)
        |    extends Resource[DynamoResourceOut](r) {
        |
        |  def out = DynamoResourceOut(
        |    OutStringVal(schemaName, resourceName, "output_1"),
        |    OutVal[Boolean](schemaName, resourceName, "output_2")
        |  )
        |
        |  override def fields: List[Field] = List(
        |    Field("input_1", input1),
        |    Field("input_2", input2)
        |  )
        |
        |  override def optionalFields: List[Option[Field]] = List(
        |    input3.map(i => Field("input_3", i))
        |  )
        |
        |  override def sthelse: List[Option[Field]] = List()
        |
        |  override def schemaName: String = "aws_dynamodb_table"
        |
        |  val test: Set[Set[String]] = ???
        |}""".stripMargin.parse[Source].get.structure

    println(r)
  }

//  def test = {
//    Source(
//      List(
//        Import(List(Importer(Term.Select(Term.Name("pl"), Term.Name("msitko")), List(Importee.Name(Name("terraform4s")))))),
//        Defn.Class(List(Mod.Final(), Mod.Case()), Type.Name("DynamoResourceOut"), Nil, Ctor.Primary(Nil, Name(""), List(
//          List(
//            Term.Param(Nil, Term.Name("output1"), Some(Type.Name("OutStringVal")), None),
//            Term.Param(Nil, Term.Name("output2"), Some(Type.Apply(Type.Name("OutVal"), List(Type.Name("Boolean")))), None)))), Template(Nil, Nil, Self(Name(""), None), Nil)),
//        Defn.Class(List(Mod.Final(), Mod.Case()), Type.Name("DynamoResource"), Nil, Ctor.Primary(Nil, Name(""), List(
//          List(
//            Term.Param(Nil, Term.Name("input1"), Some(Type.Apply(Type.Name("Val"), List(Type.Name("String")))), None),
//            Term.Param(Nil, Term.Name("input2"), Some(Type.Apply(Type.Name("Val"), List(Type.Name("Boolean")))), None),
//            Term.Param(Nil, Term.Name("input3"), Some(Type.Apply(Type.Name("Option"), List(Type.Apply(Type.Name("Val"), List(Type.Name("String")))))), Some(Term.Name("None")))
//          ),
//          List(Term.Param(List(Mod.Implicit()), Term.Name("r"), Some(Type.Name("ProvidersRoot")), None)))),
//          Template(Nil, List(Init(Type.Apply(Type.Name("Resource"), List(Type.Name("DynamoResourceOut"))), Name(""), List(List(Term.Name("r"))))), Self(Name(""), None),
//            List(
//              // def out =
//              Defn.Def(List(Mod.Override()), Term.Name("out"), Nil, Nil, None, Term.Apply(Term.Name("DynamoResourceOut"),
//                List(
//                  Term.Apply(Term.Name("OutStringVal"), List(Term.Name("schemaName"), Term.Name("resourceName"), Lit.String("output_1"))),
//                  Term.Apply(Term.ApplyType(Term.Name("OutVal"), List(Type.Name("Boolean"))), List(Term.Name("schemaName"), Term.Name("resourceName"), Lit.String("output_2")))))),
//              // def fields =
//              Defn.Def(List(Mod.Override()), Term.Name("fields"), Nil, Nil, Some(Type.Apply(Type.Name("List"), List(Type.Name("Field")))), Term.Apply(Term.Name("List"),
//                List(
//                  Term.Apply(Term.Name("Field"), List(Lit.String("input_1"), Term.Name("input1"))),
//                  Term.Apply(Term.Name("Field"), List(Lit.String("input_2"), Term.Name("input2")))))),
//              // def optionalFields =
//              Defn.Def(List(Mod.Override()), Term.Name("optionalFields"), Nil, Nil, Some(Type.Apply(Type.Name("List"), List(Type.Apply(Type.Name("Option"), List(Type.Name("Field")))))),
//                Term.Apply(Term.Name("List"), List(Term.Apply(Term.Select(Term.Name("input3"), Term.Name("map")), List(Term.Function(List(Term.Param(Nil, Term.Name("i"), None, None)), Term.Apply(Term.Name("Field"), List(Lit.String("input_3"), Term.Name("i"))))))))),
//              Defn.Def(List(Mod.Override()), Term.Name("schemaName"), Nil, Nil, Some(Type.Name("String")), Lit.String("aws_dynamodb_table")),
//              Defn.Val(Nil, List(Pat.Var(Term.Name("test"))), Some(Type.Apply(Type.Name("Set"), List(Type.Apply(Type.Name("Set"), List(Type.Name("String")))))), Term.Name("???")))))))
//  }

  def tt = {
//    Source(List(Import(List(Importer(Term.Select(Term.Name("pl"), Term.Name("msitko")), List(Importee.Name(Name("terraform4s")))))), Defn.Class(List(Mod.Final(), Mod.Case()), Type.Name("DynamoResourceOut"), Nil, Ctor.Primary(Nil, Name(""), List(List(Term.Param(Nil, Term.Name("output1"), Some(Type.Name("OutStringVal")), None), Term.Param(Nil, Term.Name("output2"), Some(Type.Apply(Type.Name("OutVal"), List(Type.Name("Boolean")))), None), Term.Param(Nil, Term.Name("input3"), Some(Type.Apply(Type.Name("Option"), List(Type.Apply(Type.Name("Val"), List(Type.Name("String")))))), Some(Term.Name("None")))))), Template(Nil, Nil, Self(Name(""), None), Nil)), Defn.Class(List(Mod.Final(), Mod.Case()), Type.Name("DynamoResource"), Nil, Ctor.Primary(Nil, Name(""), List(List(Term.Param(Nil, Term.Name("input1"), Some(Type.Apply(Type.Name("Val"), List(Type.Name("String")))), None), Term.Param(Nil, Term.Name("input2"), Some(Type.Apply(Type.Name("Val"), List(Type.Name("Boolean")))), None)), List(Term.Param(List(Mod.Implicit()), Term.Name("r"), Some(Type.Name("ProvidersRoot")), None)))), Template(Nil, List(Init(Type.Apply(Type.Name("Resource"), List(Type.Name("DynamoResourceOut"))), Name(""), List(List(Term.Name("r"))))), Self(Name(""), None), List(Defn.Def(Nil, Term.Name("out"), Nil, Nil, None, Term.Apply(Term.Name("DynamoResourceOut"), List(Term.Apply(Term.Name("OutStringVal"), List(Term.Name("schemaName"), Term.Name("resourceName"), Lit.String("output_1"))), Term.Apply(Term.ApplyType(Term.Name("OutVal"), List(Type.Name("Boolean"))), List(Term.Name("schemaName"), Term.Name("resourceName"), Lit.String("output_2")))))), Defn.Def(List(Mod.Override()), Term.Name("fields"), Nil, Nil, Some(Type.Apply(Type.Name("List"), List(Type.Name("Field")))), Term.Apply(Term.Name("List"), List(Term.Apply(Term.Name("Field"), List(Lit.String("input_1"), Term.Name("input1"))), Term.Apply(Term.Name("Field"), List(Lit.String("input_2"), Term.Name("input2")))))), Defn.Def(List(Mod.Override()), Term.Name("optionalFields"), Nil, Nil, Some(Type.Apply(Type.Name("List"), List(Type.Apply(Type.Name("Option"), List(Type.Name("Field")))))), Term.Apply(Term.Name("List"), List(Term.Apply(Term.Select(Term.Name("input3"), Term.Name("map")), List(Term.Function(List(Term.Param(Nil, Term.Name("i"), None, None)), Term.Apply(Term.Name("Field"), List(Lit.String("input_3"), Term.Name("i"))))))))), Defn.Def(List(Mod.Override()), Term.Name("schemaName"), Nil, Nil, Some(Type.Name("String")), Lit.String("aws_dynamodb_table")), Defn.Val(Nil, List(Pat.Var(Term.Name("test"))), Some(Type.Apply(Type.Name("Set"), List(Type.Apply(Type.Name("Set"), List(Type.Name("String")))))), Term.Name("???")))))))
  }
}
