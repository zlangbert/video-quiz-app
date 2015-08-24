package controllers

import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data.Form
import models._
import Tables._
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import java.sql.Timestamp
import java.util.Date

class Application extends Controller {
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  import dbConfig.driver.api._

  val userForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "trinityid" -> nonEmptyText)((un, id) => UsersRow(0, un, id))(u => Some(u.username -> u.trinityid)))

  def index = Action(request => {
    Ok(views.html.mainMenu(userForm))
  })

  def quizList = AuthenticatedAction(request => {
    val uname = request.session.get("username").getOrElse("No name")
    val uid = request.session.get("userid").getOrElse("-1").toInt
    val db = dbConfig.db
    val classes = Queries.coursesFor(uid, db)
    val quizzes = (for (s <- classes) yield {
      Future.sequence(for (c <- s) yield {
        val classQuizzes = Queries.allQuizzesForClass(c.courseid, db)
        val now = new Timestamp(new Date().getTime)
        val quizTuple = classQuizzes.flatMap(quizzes => {
          val quizData = for {
            (p, t) <- quizzes
            tot = Queries.numberOfQuestions(p.quizid, db)
            corr = Queries.numberOfCorrectQuestions(p.quizid, uid, db)
          } yield for {
            correct <- corr
            total <- tot
          } yield (p, t, correct, total)
          Future.sequence(quizData).map(qd => qd.partition(now.getTime < _._2.getTime))
        })
        quizTuple.map(qt => (c.code + "-" + c.section + "-" + c.semester, qt._1, qt._2))
      })
    }).flatMap(f => f)
    quizzes.map(qs => Ok(views.html.quizList(qs)))
  })
  
  def viewQuiz(quizid:Int) = AuthenticatedAction { request => {
    val quizData = Queries.quizData(quizid,request.session.get("userid").getOrElse("-1").toInt, dbConfig.db)
    quizData.map(qd => Ok(views.html.viewQuiz(qd)))
  } }

  def takeQuiz(quizid:Int) = AuthenticatedAction { request => {
    val quizData = Queries.quizData(quizid,request.session.get("userid").getOrElse("-1").toInt, dbConfig.db)
    quizData.map(qd => Ok(views.html.takeQuiz(qd)))
  } }
  
  def submitQuiz = AuthenticatedAction { request => {
    val db = dbConfig.db
    val userid = request.session("userid").toInt
    request.body.asFormUrlEncoded match {
      case Some(params) =>
        val quizid = params("quizid")(0).toInt
        // Get quiz specs from database and check correctness
        for(key <- params.keys; if key.startsWith("mc-")) {
          val mcid = key.drop(3).toInt
          val pspec = ProblemSpec(ProblemSpec.MultipleChoiceType,mcid,db)
          val correct = pspec.map(_.checkResponse(params(key)(0)))
          val selection = try { params(key)(0).toInt } catch { case e:NumberFormatException => -1 }
          correct.map(c => db.run(McAnswers += McAnswersRow(Some(userid),Some(quizid),Some(mcid),selection,c)))
        }
        for(key <- params.keys; if key.startsWith("code-")) {
          val Array(codeid,qtype) = key.drop(5).split("-")
          val pspec = ProblemSpec(qtype.toInt,codeid.toInt,db)
          val correct = pspec.map(_.checkResponse(params(key)(0)))
          correct.map(c => db.run(CodeAnswers += CodeAnswersRow(Some(userid),Some(quizid),codeid.toInt,qtype.toInt,params(key)(0),c)))
        }
      case None =>
    }
    Future(Redirect(routes.Application.quizList()))
  } }

  def fetch(user: String) = AuthenticatedAction { request =>
    Queries.fetchUserByName(user, dbConfig.db).map(user => Ok(Queries.coursesFor(user.userid, dbConfig.db).toString()))
  }

  def verifyLogin = Action.async(implicit request => {
    userForm.bindFromRequest().fold(
      formWithErrors => {
        Future { Redirect(routes.Application.index) }
      },
      value =>
        Queries.validLogin(value, dbConfig.db).map(_ match {
          case -1 =>
            println(value + " Bad")
            Redirect(routes.Application.index)
          case n =>
            println(value + " Good")
            Redirect(routes.Application.quizList).withSession(request.session + ("username" -> value.username) + ("userid" -> n.toString))
        }))
  })

  def logout = Action { implicit request =>
    Redirect(routes.Application.index()).withSession(request.session - "username" - "userid")
  }

  // Other methods

  private def authenticate(request: Request[AnyContent]): Boolean = {
    request.session.get("username") match {
      case None => false
      case Some(uname) => true
    }
  }

  private def AuthenticatedAction(f: Request[AnyContent] => Future[Result]): Action[AnyContent] = {
    Action.async { request =>
      if (authenticate(request)) {
        f(request)
      } else {
        Future { Redirect(routes.Application.index()) }
      }
    }
  }

}
