package models

import java.io.File

object ProblemSet {
  def apply(pdir:File, name:String):ProblemSet = {
    val psetXML = xml.XML.loadFile(new File(pdir,name+".xml"))
    val setDir = new File(pdir,name)
    val problems = (psetXML \ "problem") map (n => ProblemSpec(n,setDir))
    ProblemSet(name,problems)
  }
}

/**
 * This represents a set of problems that can be added to a class.
 */
case class ProblemSet(name:String, problems:Seq[ProblemSpec]) {
  def checkResponses(psr:ProblemSetResponse):Seq[Boolean] = {
    (problems,psr.responses).zipped.map((p,r) => p.checkResponse(r))
  }
}