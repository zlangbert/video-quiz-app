package models

/**
 * @author mlewis
 */
object VariableType extends Enumeration {
  type Type = Value
  val Int, Double, String = Value
}

object VariableSpec {
  def apply(n: xml.Node): VariableSpec = {
    (n \ "@type").text match {
      case "Int" =>
        val name = (n \ "@name").text
        val min = (n \ "@min").text.toInt
        val max = (n \ "@max").text.toInt
        IntSpec(name, min, max)
      case "Double" =>
        val name = (n \ "@name").text
        val min = (n \ "@min").text.toDouble
        val max = (n \ "@max").text.toDouble
        DoubleSpec(name, min, max)
      case "String" =>
        val name = (n \ "@name").text
        val length = (n \ "@length").text.toInt
        val genCode = (n \ "genCode").text.trim
        StringSpec(name, length,genCode)
      case "List[Int]" =>
        val name = (n \ "@name").text
        val minLen = (n \ "@minLen").text.toInt
        val maxLen = (n \ "@maxLen").text.toInt
        val min = (n \ "@min").text.toInt
        val max = (n \ "@max").text.toInt
        ListIntSpec(name, minLen, maxLen, min, max)
      case "Array[Int]" =>
        val name = (n \ "@name").text
        val minLen = (n \ "@minLen").text.toInt
        val maxLen = (n \ "@maxLen").text.toInt
        val min = (n \ "@min").text.toInt
        val max = (n \ "@max").text.toInt
        ArrayIntSpec(name, minLen, maxLen, min, max)
      case "List[String]" =>
        val name = (n \ "@name").text
        val minLen = (n \ "@minLen").text.toInt
        val maxLen = (n \ "@maxLen").text.toInt
        val stringLength = (n \ "@stringLength").text.toInt
        val genCode = (n \ "genCode").text.trim
        ListStringSpec(name, minLen, maxLen, stringLength, genCode)
    }
  }
}

sealed trait VariableSpec {
  val name: String
  val typeName: String
  def codeGenerator(): String // Return a string that is code to generate this value. 
}

case class IntSpec(name: String, min: Int, max: Int) extends VariableSpec {
  val typeName = "Int"

  def codeGenerator(): String = {
    s"val $name = util.Random.nextInt(($max)-($min))+($min)"
  }
}

case class DoubleSpec(name: String, min: Double, max: Double) extends VariableSpec {
  val typeName = "Double"

  def codeGenerator(): String = {
    s"val $name = math.random*(($max)-($min))+($min)"
  }
}

case class StringSpec(name: String, length: Int, genCode: String) extends VariableSpec {
  val typeName = "String"

  def codeGenerator(): String = {
    if(genCode.isEmpty())
      s"val $name = (for(i <- 0 until $length) yield { ('a'+util.Random.nextInt(26)).toChar }).mkString"
    else
      s"val $name = $genCode"
  }
}

case class ListIntSpec(name: String, minLen:Int, maxLen:Int, min: Int, max: Int) extends VariableSpec {
  val typeName = "List[Int]"

  def codeGenerator(): String = {
    s"val $name = List.fill(util.Random.nextInt(($maxLen)-($minLen))+($minLen))(util.Random.nextInt(($max)-($min))+($min))"
  }
}

case class ArrayIntSpec(name: String, minLen:Int, maxLen:Int, min: Int, max: Int) extends VariableSpec {
  val typeName = "Array[Int]"

  def codeGenerator(): String = {
    s"val $name = Array.fill(util.Random.nextInt(($maxLen)-($minLen))+($minLen))(util.Random.nextInt(($max)-($min))+($min))"
  }
}

case class ListStringSpec(name: String, minLen:Int, maxLen:Int, stringLength: Int, genCode: String) extends VariableSpec {
  val typeName = "String"

  def codeGenerator(): String = {
    if(genCode.isEmpty())
      s"val $name = List.fill(util.Random.nextInt(($maxLen)-($minLen))+($minLen)){(for(i <- 0 until $stringLength) yield { ('a'+util.Random.nextInt(26)).toChar }).mkString}"
    else
      s"val $name = List.fill(util.Random.nextInt(($maxLen)-($minLen))+($minLen)){$genCode}"
  }
}
