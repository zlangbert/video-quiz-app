package models

import java.io.File
import java.io.PrintWriter
import sys.process._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

object ProblemSpec {
  def apply(n: xml.Node, baseDir: File): ProblemSpec = {
    (n \ "@type").text match {
      case "mchoice" =>
        val options = (n \ "option")
        MultipleChoice((n \ "prompt").text.trim, options map (_.text.trim), options.indexWhere(on => (on \ "@correct").text == "true"))
      case "function" =>
        val codeFile = (n \ "@codeFile").text.trim
        val correctCode = if (codeFile.isEmpty()) (n \ "correctCode").text.trim else {
          val source = io.Source.fromFile(new File(baseDir, codeFile))
          val text = source.mkString
          source.close
          text
        }
        val name = (n \ "@functionName").text
        val varSpecs = (n \ "varSpec").map(n => VariableSpec(n))
        val numRuns = if ((n \ "@numRuns").isEmpty) 100 else (n \ "@numRuns").text.toInt
        WriteFunction((n \ "prompt").text.trim, correctCode, name, varSpecs, numRuns)
      case "literal" =>
        val codeFile = (n \ "@codeFile").text.trim
        val correctCode = if (codeFile.isEmpty()) (n \ "correctCode").text.trim else {
          val source = io.Source.fromFile(new File(baseDir, codeFile))
          val text = source.mkString
          source.close
          text
        }
        val returnType = (n \ "@returnType").text
        val varSpecs = (n \ "varSpec").map(n => VariableSpec(n))
        val numRuns = if ((n \ "@numRuns").isEmpty) 100 else (n \ "@numRuns").text.toInt
        WriteFunctionLiteral((n \ "prompt").text.trim, correctCode, returnType, varSpecs, numRuns)
      case "expression" =>
        val codeFile = (n \ "@codeFile").text.trim
        val correctCode = if (codeFile.isEmpty()) (n \ "correctCode").text.trim else {
          val source = io.Source.fromFile(new File(baseDir, codeFile))
          val text = source.mkString
          source.close
          text
        }
        val varSpecs = (n \ "varSpec").map(n => VariableSpec(n))
        val generalSetup = (n \ "generalSetup").text.trim
        val numRuns = if ((n \ "@numRuns").isEmpty) 100 else (n \ "@numRuns").text.toInt
        WriteExpression((n \ "prompt").text.trim, correctCode, varSpecs, generalSetup, numRuns)
      case "iocode" => ???
      case "code completion" => ???
      case "code compiles" => ???
      case "regex matching" => ???
      case "file submit" => ???
    }
  }

  def nestTest(testCode: String, numRuns: Int): String = {
    s"""import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
    
Future {
   Thread.sleep(10000)
   sys.exit(1)
}
for(i <- 1 to $numRuns) {
  $testCode
}
sys.exit(0)
"""
  }

  def runCode(code: String, input: String, numRuns: Int): Boolean = {
    val tmpFile = File.createTempFile("test", ".scala")
    tmpFile.deleteOnExit()
    val pw = new PrintWriter(tmpFile)
    val nestedCode = nestTest(code, numRuns)
    println(nestedCode)
    pw.println(nestedCode)
    pw.close
    val process = s"scala ${tmpFile.getAbsolutePath()}".run()
    val ret = process.exitValue == 0
    println("Done running - "+ret)
    ret
  }
}

sealed trait ProblemSpec {
  def checkResponse(response: ProblemResponse): Boolean
}

case class MultipleChoice(prompt: String, options: Seq[String], correct: Int) extends ProblemSpec {
  def checkResponse(response: ProblemResponse): Boolean = {
    response.text.nonEmpty && response.text.head.toInt == correct
  }
}

case class WriteFunction(prompt: String, correctCode: String, functionName: String, varSpecs: Seq[VariableSpec], numRuns: Int) extends ProblemSpec {
  def checkResponse(response: ProblemResponse): Boolean = {
    val code = s"""
      ${correctCode.replaceAll(functionName, functionName+"Correct")}
      ${response.text.head}
      ${varSpecs.map(_.codeGenerator).mkString("\n")}
      val theirFunc = $functionName(${varSpecs.map(_.name).mkString(",")})
      val correctFunc = ${functionName}Correct(${varSpecs.map(_.name).mkString(",")})
      if(theirFunc != correctFunc) sys.exit(1)
      """
    ProblemSpec.runCode(code, "", numRuns)
  }
}

case class WriteFunctionLiteral(prompt: String, correctCode: String, returnType: String, varSpecs: Seq[VariableSpec], numRuns: Int) extends ProblemSpec {
  def checkResponse(response: ProblemResponse): Boolean = {
    val funcType = s"(${varSpecs.map(_.typeName).mkString(", ")}) => $returnType"
    val args = varSpecs.map(_.name).mkString(", ")
    val code = s"""
      def tester(f1:$funcType, f2:$funcType):Unit = {
        ${varSpecs.map(_.codeGenerator).mkString("\n")}
        if(f1($args) != f2($args)) sys.exit(1)
      }
      tester(${response.text.head},$correctCode)
      """
    ProblemSpec.runCode(code, "", numRuns)
  }
}

case class WriteExpression(prompt: String, correctCode: String, varSpecs: Seq[VariableSpec], generalSetup: String, numRuns: Int) extends ProblemSpec {
  def checkResponse(response: ProblemResponse): Boolean = {
    val code = s"""
      ${varSpecs.map(_.codeGenerator).mkString("\n")}
      $generalSetup
      if({${response.text.head}} != {$correctCode}) sys.exit(1)
      """
    ProblemSpec.runCode(code, "", numRuns)
  }
}

case class IOCode(prompt: String) extends ProblemSpec {
  def checkResponse(response: ProblemResponse): Boolean = ???
}

case class CodeCompletion(prompt: String, code: String, varSpecs: Seq[VariableSpec], numRuns: Int) extends ProblemSpec {
  def checkResponse(response: ProblemResponse): Boolean = ???
}

case class CodeCompiles(prompt: String) extends ProblemSpec {
  def checkResponse(response: ProblemResponse): Boolean = ???
}

case class RegExMatching(prompt: String) extends ProblemSpec {
  def checkResponse(response: ProblemResponse): Boolean = ???
}

//case class CodeTracing(prompt:String) extends ProblemSpec {
//}

case class FileSubmission(prompt: String) extends ProblemSpec {
  def checkResponse(response: ProblemResponse): Boolean = ???

}