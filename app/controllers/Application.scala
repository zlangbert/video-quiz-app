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
    println("Username is " + uname)
    val db = dbConfig.db
    val classes = Queries.coursesFor(uid, db)
    val quizzes = (for (s <- classes) yield {
      Future.sequence(for (c <- s) yield {
        val classQuizzes = Queries.allQuizzesForClass(c.courseid, db)
        val now = new Timestamp(new Date().getTime)
        val quizTuple = classQuizzes.flatMap(quizzes => {
          val (open, completed) = quizzes.partition(now.getTime < _._2.getTime)
          val completeData = for {
            (p, t) <- completed
            tot = Queries.numberOfQuestions(p.quizid, db)
            corr = Queries.numberOfCorrectQuestions(p.quizid, uid, db)
          } yield for {
            correct <- corr
            total <- tot
          } yield (p, correct, total)
          Future.sequence(completeData).map(cd => open -> cd)
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
    // Get quiz specs from database
    // Check correctness of each element and write to database
    Future(Redirect(routes.Application.quizList()))
  } }

  def fetch(user: String) = AuthenticatedAction { request =>
    Queries.fetchUserByName(user, dbConfig.db).map(user => Ok(Queries.coursesFor(user.userid, dbConfig.db).toString()))
  }

  def verifyLogin = Action.async(implicit request => {
    println("Verify")
    userForm.bindFromRequest().fold(
      formWithErrors => {
        println(formWithErrors)
        println(userForm)
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
