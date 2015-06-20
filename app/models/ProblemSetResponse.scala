package models

import java.io.File
import java.io.PrintWriter

object ProblemSetResponse {
  def loadStates(student: String, course: String, set: String):ProblemSetState.State = {
    val dir = new File(s"./$course/$set/$student")
    if (dir.exists) {
      var ret = ProblemSetState.Submitted
      var sub = 0
      var subDir = new File(dir, s"Sub$sub")
      while (subDir.exists && ret<ProblemSetState.Correct) {
        val source = io.Source.fromFile(new File(subDir,"correct.txt"))
        if(source.mkString.startsWith("true")) ret = ProblemSetState.Correct
        source.close
        sub += 1
        subDir = new File(dir, s"Sub$sub")
      }
      ret
    } else {
      ProblemSetState.Unattempted
    }
  }
  
  def apply(student: String, course: String, set: String, numProblems: Int): Seq[(ProblemSetResponse, ProblemSetState.State)] = {
    val dir = new File(s"./$course/$set/$student")
    if (dir.exists) {
      var ret = List[(ProblemSetResponse, ProblemSetState.State)]()
      var sub = 0
      var subDir = new File(dir, s"Sub$sub")
      while (subDir.exists) {
        ret ::= loadResponseDir(student, course, set, subDir, numProblems)
        sub += 1
        subDir = new File(dir, s"Sub$sub")
      }
      ret
    } else {
      Nil
    }
  }

  def loadResponseDir(student: String, course: String, set: String, dir: File, numProblems: Int): (ProblemSetResponse, ProblemSetState.State) = {
    val source1 = io.Source.fromFile(new File(dir, "correct.txt"))
    val state = if (source1.mkString == "true") ProblemSetState.Correct else ProblemSetState.Submitted
    source1.close
    val responses = for(i <- 0 until numProblems) yield {
      val source2 = io.Source.fromFile(new File(dir, s"Prob$i.txt"))
      val text = source2.mkString.split("\n\\.\n")
      source2.close
      ProblemResponse(text)
    } 
    (ProblemSetResponse(student,course,set,responses), state)
  }
}

case class ProblemSetResponse(student: String, course: String, set: String, responses: Seq[ProblemResponse]) {
  def record(correct: Boolean): Unit = {
    val dir = new File(s"./$course/$set/$student")
    if (!dir.exists()) dir.mkdirs()
    var sub = 0
    var subDir = new File(dir, s"Sub$sub")
    while (subDir.exists()) {
      sub += 1
      subDir = new File(dir, s"Sub$sub")
    }
    subDir.mkdirs()
    val cpw = new PrintWriter(new File(subDir, "correct.txt"))
    cpw.println(correct)
    cpw.close()
    for (i <- responses.indices) {
      val pw = new PrintWriter(new File(subDir, s"Prob$i.txt"))
      responses(i).text.foreach(s => {
        pw.println(s)
        pw.println(".")
      })
      pw.close()
    }
  }
}