package models

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import slick.driver.MySQLDriver.api._
import scala.concurrent.duration.Duration
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import Tables._

/**
 * @author mlewis
 */
object VariableType extends Enumeration {
  type Type = Value
  val Int, Double, String = Value
}

object VariableSpec {
  val IntSpecType = 0
  val DoubleSpecType = 1
  val StringSpecType = 2
  val IntListSpecType = 3
  val IntArraySpecType = 4
  val StringListSpecType = 5

  def apply(vs:VariableSpecificationsRow): VariableSpec = vs.specType match {
    case IntSpecType =>
      IntSpec(vs.name, vs.min.get, vs.max.get)
    case DoubleSpecType =>
      DoubleSpec(vs.name, vs.min.get, vs.max.get)
    case StringSpecType =>
      StringSpec(vs.name, vs.length.get, vs.genCode.getOrElse(""))
    case IntListSpecType =>
      ListIntSpec(vs.name, vs.minLength.get, vs.maxLength.get, vs.min.get, vs.max.get)
    case IntArraySpecType =>
      ArrayIntSpec(vs.name, vs.minLength.get, vs.maxLength.get, vs.min.get, vs.max.get)
    case StringListSpecType =>
      ListStringSpec(vs.name, vs.minLength.get, vs.maxLength.get, vs.length.get, vs.genCode.getOrElse(""))
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
