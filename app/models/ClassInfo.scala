package models

import java.util.Date
import java.text.DateFormat
import java.io.File

case class ProblemStateKey(student:String, set:String)

object ClassInfo {
  def apply(n:xml.Node):ClassInfo = {
    val course = (n \ "@course").text
    val section = (n \ "@section").text
    val semester = (n \ "@semester").text
    val instructor = (n \ "@instructor").text
    val students = (n \ "students").text.trim.split("\n").map(_.trim)
    val problemSets = (n \ "problemSet").map(dn => {
      val name = (dn \ "@name").text
      val closes = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).parse((dn \ "@closes").text)
      (ProblemSet(new File(makeID(course,section,semester)),name),closes)
    }).map(set => set._1.name -> set).toMap
    val setResponses = (for(s <- students; (set,_) <- problemSets) yield ProblemStateKey(s,set) -> ProblemSetResponse.loadStates(s, makeID(course,section,semester), set)).toMap
    ClassInfo(course,section,semester,instructor,students,problemSets,setResponses)
  }
  
  def makeID(course:String,section:String,semester:String):String = {
    s"$semester-$course-$section"
  }
}

/**
 * This class represents a specific meeting time for a course. It keeps track of what students are in that class
 * as well as what problem sets the class has and when they close.
 */
case class ClassInfo(course:String,
    section:String,
    semester:String,
    instructor:String,
    students:Seq[String],
    problemSets:Map[String,(ProblemSet,Date)],
    problemSetStates:Map[ProblemStateKey,ProblemSetState.State]) {
  def id = ClassInfo.makeID(course,section,semester)
  
  def addResponse(psr:ProblemSetResponse, state:ProblemSetState.State) = copy(problemSetStates = problemSetStates + {
    val key = ProblemStateKey(psr.student, psr.set)
    val curState = problemSetStates.getOrElse(key, ProblemSetState.Unattempted)
    if(state>curState) key -> state else key -> curState
  })
}