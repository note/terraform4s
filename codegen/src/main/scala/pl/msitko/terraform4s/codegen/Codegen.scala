package pl.msitko.terraform4s.codegen

import java.nio.file.{Path, Paths}

import org.scalafmt.interfaces.Scalafmt
import pl.msitko.terraform4s.codegen.classes.{AnonymousClassCodegen, InputParamsCodegen, OutClassCodegen}
import pl.msitko.terraform4s.codegen.methods.{FieldsMethods, OutMethodCodegen}
import pl.msitko.terraform4s.provider.ast.{HCLObject, _}

import scala.meta._
import scala.util.Try

object Codegen {

  def generateAndSave(
      resources: Map[String, Resource],
      packageName: List[String],
      outputPath: Path,
      scalafmtConfPath: Path,
      ctx: CodegenContext): Try[Unit] = Try {

    val scalafmt = Scalafmt.create(this.getClass.getClassLoader)

    val outputWithPackagePath = packageName.foldLeft(os.Path(outputPath)) { (acc, curr) =>
      acc / curr
    }
    os.makeDir.all(outputWithPackagePath)

    resources.map {
      case (k, v) =>
        val nameInCC = toCamelCase(k)
        val source   = generateResource(nameInCC, v, toTermSelect(packageName), ctx)

        // TODO: document "whatever.scala" part
        val formatted = scalafmt.format(scalafmtConfPath, Paths.get("whatever.scala"), source.syntax)

        os.write(outputWithPackagePath / (nameInCC + ".scala"), formatted)
    }
  }

  def generateResource(name: String, resource: Resource, packageName: Option[Term.Ref], ctx: CodegenContext): Source = {
    // so far we are only interested in arguments, very naive logic (validate later):
    val arguments: List[(String, AttributeValue)] = resource.block.attributes.filter {
      case (_, v) => v.optional.isDefined || v.required == Some(true)
    }

    // needed for objects, way too naive to handle e.g. nested objects
    val anonymousClasses = arguments.map(_._2.`type`).collect {
      case obj: HCLObject => obj
    }

    val anonymousClassesDefs = anonymousClasses.map { obj =>
      val syntheticClassName = ctx.getNextAnonymousClassName
      AnonymousClassCodegen.fromHCLObject(syntheticClassName, obj, ctx)
    }

    val imports = List(
      Import(
        List(
          Importer(
            Term.Select(Term.Select(Term.Name("pl"), Term.Name("msitko")), Term.Name("terraform4s")),
            List(Importee.Name(Name("Resource")))))))
    val classDefs = anonymousClassesDefs ++ generateResourceClass(name, resource, ctx)

    packageName.fold(Source(imports ++ classDefs)) { pkgName =>
      Source(List(Pkg(pkgName, imports ++ classDefs)))
    }
  }

  Term.Select(Term.Select(Term.Select(Term.Name("a"), Term.Name("b")), Term.Name("c")), Term.Name("d"))

  def toTermSelect(packageComponents: List[String]): Option[Term.Ref] =
    if (packageComponents.nonEmpty) {
      Some(
        packageComponents.tail.foldLeft(Term.Name(packageComponents.head): Term.Ref) { (acc, curr) =>
          Term.Select(acc, Term.Name(curr))
        }
      )
    } else {
      None
    }

  val packageName = Term.Select(Term.Select(Term.Name("pl"), Term.Name("msitko")), Term.Name("example"))

  // copied from https://stackoverflow.com/a/37619752
  private def toCamelCase(in: String) =
    "_([a-z\\d])".r.replaceAllIn(in, _.group(1).toUpperCase)

  private def generateResourceClass(name: String, v: Resource, ctx: CodegenContext): List[Defn.Class] = {
    val outTypeName = name + "Out"

    val requiredParams = InputParamsCodegen.requiredParams(v.block.requiredInputs.map(t => (t._1, t._2.`type`)), ctx)
    val optionalParams = InputParamsCodegen.optionalParams(v.block.optionalInputs.map(t => (t._1, t._2.`type`)), ctx)

    // format: off
    List(
      OutClassCodegen.out(outTypeName, v.block.outputs),
      Defn.Class(List(Mod.Final(), Mod.Case()), Type.Name(name), Nil, Ctor.Primary(Nil, Name(""), List(
        requiredParams ++ optionalParams,
        List(Term.Param(List(Mod.Implicit()), Term.Name("r"), Some(Type.Name("ProvidersRoot")), None))
      )),
        Template(Nil, List(Init(Type.Apply(Type.Name("Resource"), List(Type.Name(outTypeName))), Name(""), List(List(Term.Name("r"))))), Self(Name(""), None),
          List(
            OutMethodCodegen.generate(name + "Out", v.block.outputs),
            FieldsMethods.generate(v.block.requiredInputs),
            FieldsMethods.generateOptionalFields(v.block.optionalInputs),
            Defn.Def(List(Mod.Override()), Term.Name("schemaName"), Nil, Nil, Some(Type.Name("String")), Lit.String(name))
          )
        )
      )
    )
    // format: on
  }

}
