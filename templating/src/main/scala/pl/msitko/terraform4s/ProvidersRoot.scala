package pl.msitko.terraform4s

import scala.annotation.tailrec

class ProvidersRoot {
  private var resources = Vector.empty[Resource[_]]

  @tailrec
  private def generateUniqueResourceName(schemaName: String, n: Int = 0): String = {
    val guess = s"${schemaName}_${n.toString}"
    if (!resources.exists(_.resourceName == guess)) {
      guess
    } else {
      generateUniqueResourceName(schemaName, n + 1)
    }
  }

  def add(r: Resource[_]): String = {
    val resourceName = generateUniqueResourceName(r.__schemaName)
    resources = resources.prepended(r)
    resourceName
  }

  def getJson = TerraformGenerator.encode(resources)
}

object ProvidersRoot {
  implicit lazy val Default: ProvidersRoot = new ProvidersRoot
}
