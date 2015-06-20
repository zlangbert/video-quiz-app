package models

import akka.actor.Actor
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object ResponseChecker {
  case class CheckResponse(psr: ProblemSetResponse, 
//      c: RemoteClient,
      ps: ProblemSet)
}

class ResponseChecker extends Actor {
  def receive = {
//    case ResponseChecker.CheckResponse(psr, c, ps) =>
    case ResponseChecker.CheckResponse(psr, ps) =>
      val server = sender
      Future {
//        val state = checkResponses(psr, ps, s => c.generalResponse(s))
//        server ! ServerActor.CheckDone(psr, state)
//        server ! ServerActor.RequestActivityList(psr.student, c)
      }
  }

  def checkResponses(psr: ProblemSetResponse, ps: ProblemSet, send: String => Unit): ProblemSetState.State = {
    val correct = ps.checkResponses(psr)
    psr.record(correct.forall(b => b))
    val result = if (correct.forall(b => b)) {
      send("Correct.")
      ProblemSetState.Correct
    } else {
      send("You had the wrong answer to problem(s): "+correct.zipWithIndex.filter(t => !t._1).map(t => t._2 + 1).mkString(", "))
      ProblemSetState.Submitted
    }
    result
  }

}