package pl.msitko.terraform4s.codegen

import java.time.Instant

import org.scalafmt.{Formatted, Scalafmt}
import pl.msitko.terraform4s.cli.CodegenConfig
import pl.msitko.terraform4s.codegen.classes.{AnonymousClassCodegen, InputParamsCodegen, OutClassCodegen}
import pl.msitko.terraform4s.codegen.comments.FileLevelCommentCodegen
import pl.msitko.terraform4s.codegen.methods.{FieldsMethods, OutMethodCodegen}
import pl.msitko.terraform4s.provider.ast._

import scala.meta._
import scala.util.Try

object Codegen {

  // TODO: is try a good return type?
  def generateAndSave(config: CodegenConfig, generatedAt: Instant): Try[Unit] = Try {
    val processedSchemas = Transformations.camelCaseAttributes(config.providerSchemas)

    processedSchemas.provider_schemas.foreach {
      case (providerName, providerSchema) =>
        val ctx = new DefaultCodegenContext

        val packageName = config.packageNamePrefix :+ providerName

        val outputWithPackagePath = packageName.foldLeft(config.outPath) { (acc, curr) =>
          acc / curr
        }
        os.makeDir.all(outputWithPackagePath)

        providerSchema.resource_schemas.map {
          case (k, v) =>
            val nameInCC = toPascalCase(k)
            val source   = generateResource(nameInCC, v, toTermSelect(packageName), ctx)

            val comment = {
              val tfVersion       = config.versions.terraformVersion
              val providerVersion = config.versions.providersVersions.get(providerName)
              FileLevelCommentCodegen.generate(generatedAt, tfVersion, providerName, providerVersion)
            }

            val formattedCode = Scalafmt.format(source.syntax, config.scalafmtConf) match {
              case Formatted.Success(formatted) =>
                formatted
              case Formatted.Failure(e) =>
                throw new RuntimeException(s"Formatting with scalafmt failed: $e", e)
            }

            os.write(outputWithPackagePath / (nameInCC + ".scala"), comment)
            os.write.append(outputWithPackagePath / (nameInCC + ".scala"), formattedCode)
        }
    }
  }

  def generateResource(name: String, resource: Resource, packageName: Option[Term.Ref], ctx: CodegenContext): Source = {
    // needed for objects, way too naive to handle e.g. nested objects
    val anonymousClasses = resource.block.allObjects

    anonymousClasses.foreach { anonymousClass =>
      ctx.getNameOf(anonymousClass) match {
        case None => ctx.registerAnonymousClass(ctx.getNextAnonymousClassName, anonymousClass)
        case _    => // do nothing
      }
    }

    val anonymousClassesDefs = anonymousClasses.map { obj =>
      // we can call .get safely as we call registerAnonymousClass a few lines above for all anonymousClasses
      val syntheticClassName = ctx.getNameOf(obj).get
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

  private def toPascalCase(in: String) =
    Transformations.toCamelCase(in).capitalize

  private def generateResourceClass(name: String, v: Resource, ctx: CodegenContext): List[Defn.Class] = {
    val outTypeName = name + "Out"

    val requiredParams = InputParamsCodegen.requiredParams(v.block.requiredInputs.map(t => (t._1, t._2.`type`)), ctx)
    val optionalParams = InputParamsCodegen.optionalParams(v.block.optionalInputs.map(t => (t._1, t._2.`type`)), ctx)

    // format: off
    println(s"bazinga $outTypeName: ${v.block.optionalOutputs.map(_._1)}")
    List(
      OutClassCodegen.out(outTypeName, v.block.alwaysPresentOutputs, v.block.optionalOutputs, ctx),
      Defn.Class(List(Mod.Final(), Mod.Case()), Type.Name(name), Nil, Ctor.Primary(Nil, Name(""), List(
        requiredParams ++ optionalParams,
        List(Term.Param(List(Mod.Implicit()), Term.Name("r"), Some(Type.Name("ProvidersRoot")), None))
      )),
        Template(Nil, List(Init(Type.Apply(Type.Name("Resource"), List(Type.Name(outTypeName))), Name(""), List(List(Term.Name("r"))))), Self(Name(""), None),
          List(
            OutMethodCodegen.generate(name + "Out", v.block.alwaysPresentOutputs, ctx),
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
