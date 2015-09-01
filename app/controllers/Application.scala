package controllers

import forms.Forms
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
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.ZoneId

class Application extends Controller {
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  import dbConfig.driver.api._

  val newCourseForm = Form(
    mapping(
      "Course Code" -> nonEmptyText(7, 8),
      "Semester" -> nonEmptyText(3, 3),
      "Section" -> number(0, 99),
      "Instructor Names" -> text,
      "Student Data" -> nonEmptyText)(NewCourseData.apply)(NewCourseData.unapply))

  // GET Actions

  def index = Action(implicit request => {
    Ok(views.html.mainMenu(Forms.userForm))
  })

  def quizList = AuthenticatedAction(implicit request => {
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

  def viewQuiz(quizid: Int) = AuthenticatedAction { implicit request =>
    {
      val quizData = Queries.quizData(quizid, request.session.get("userid").getOrElse("-1").toInt, dbConfig.db)
      quizData.map(qd => Ok(views.html.viewQuiz(qd)))
    }
  }

  def takeQuiz(quizid: Int) = AuthenticatedAction { implicit request =>
    {
      val quizData = Queries.quizData(quizid, request.session.get("userid").getOrElse("-1").toInt, dbConfig.db)
      quizData.map(qd => Ok(views.html.takeQuiz(qd)))
    }
  }

  def fetch(user: String) = AuthenticatedAction { implicit request =>
    Queries.fetchUserByName(user, dbConfig.db).map(user => Ok(Queries.coursesFor(user.userid, dbConfig.db).toString()))
  }

  def instructorPage = AuthenticatedInstructorAction { implicit request =>
    val db = dbConfig.db
    val courses = Queries.instructorCourseRows(request.session.get("userid").get.toInt, db)
    val quizzes = db.run(Quizzes.result)
    val mcQuestions = db.run(MultipleChoiceQuestions.result)
    val funcQuestions = db.run(FunctionQuestions.result)
    val lambdaQuestions = db.run(LambdaQuestions.result)
    val exprQuestions = db.run(ExpressionQuestions.result)
    for {
      ic <- courses
      q <- quizzes
      mc <- mcQuestions
      func <- funcQuestions
      lambda <- lambdaQuestions
      expr <- exprQuestions
    } yield {
      Ok(views.html.instructorMainPage(ic, q, mc, func, lambda, expr))
    }
  }

  def editQuiz(quizid: Int) = AuthenticatedInstructorAction { implicit request =>
    if (quizid < 1) {
      Future { Ok(views.html.editQuiz(QuizzesRow(quizid, null, null), false, Nil)) }
    } else {
      val db = dbConfig.db
      val quizRow = db.run(Quizzes.filter(_.quizid === quizid).result.head)
      val mcAnswers = db.run(McAnswers.filter(_.quizid === quizid).result)
      val codeAnswers = db.run(CodeAnswers.filter(_.quizid === quizid).result)
      val quizSpecs = Queries.quizSpecs(quizid, db)
      for {
        qr <- quizRow
        mca <- mcAnswers
        ca <- codeAnswers
        specs <- quizSpecs
      } yield Ok(views.html.editQuiz(qr, mca.nonEmpty || ca.nonEmpty, specs))
    }
  }

  def multipleChoiceEdit(id: Int) = AuthenticatedInstructorAction { implicit requext =>
    if (id < 1) {
      Future { Ok(views.html.multipleChoiceEdit(MultipleChoice(id, "", Nil, -1))) }
    } else {
      ProblemSpec.multipleChoice(id, dbConfig.db).map { ps =>
        Ok(views.html.multipleChoiceEdit(ps))
      }
    }
  }

  def writeFunctionEdit(id: Int) = TODO

  def writeLambdaEdit(id: Int) = TODO

  def writeExpressionEdit(id: Int) = TODO

  def addCourse = AuthenticatedInstructorAction { implicit request =>
    Future { Ok(views.html.addCourse(newCourseForm)) }
  }

  def viewCourse(courseid: Int) = AuthenticatedInstructorAction { implicit request =>
    Queries.loadCourseData(courseid, dbConfig.db).map(cd => Ok(views.html.viewCourse(cd)))
  } 

  def setupDatabase = Action { implicit request =>
    dbConfig.db.run(Users.filter(_.username === "mlewis").result).map(s =>
      if (s.isEmpty) {
        dbConfig.db.run(DBIO.seq(
          Users += UsersRow(0, "mlewist", "0123456"),
          Users += UsersRow(0, "mlewis", "0123456"),
          Courses += CoursesRow(0, "CSCI1302", "F15", 6),
          UserCourseAssoc += UserCourseAssocRow(Some(1), Some(1), Queries.Student),
          UserCourseAssoc += UserCourseAssocRow(Some(2), Some(1), Queries.Instructor),
          Quizzes += QuizzesRow(0, "Quiz #1", "A test quiz."),
          Quizzes += QuizzesRow(0, "Quiz #2", "A test quiz."),
          QuizCourseCloseAssoc += QuizCourseCloseAssocRow(Some(2), Some(1), Timestamp.valueOf("2015-12-12 12:12:12")),
          QuizCourseCloseAssoc += QuizCourseCloseAssocRow(Some(1), Some(1), Timestamp.valueOf("2015-06-12 12:12:12")),
          MultipleChoiceQuestions += MultipleChoiceQuestionsRow(0, "MC question 1", "true", "false", None, None, None, None, None, None, 1),
          MultipleChoiceQuestions += MultipleChoiceQuestionsRow(0, "MC question 2", "blue", "red", Some("green"), None, None, None, None, None, 2),
          MultipleChoiceQuestions += MultipleChoiceQuestionsRow(0, "MC question 3", "mammal", "bird", Some("fish"), None, None, None, None, None, 3),
          MultipleChoiceAssoc += MultipleChoiceAssocRow(Some(1), Some(1)),
          MultipleChoiceAssoc += MultipleChoiceAssocRow(Some(1), Some(2)),
          MultipleChoiceAssoc += MultipleChoiceAssocRow(Some(2), Some(3)),
          FunctionQuestions += FunctionQuestionsRow(0, "Function question 1", "good code", "foo", 10),
          FunctionQuestions += FunctionQuestionsRow(0, "Write a function called bar() that returns 42.", "def bar() = 42", "bar", 10),
          FunctionQuestions += FunctionQuestionsRow(0, "Write a factorial function called fact.", "def fact(n:Int) = (1 to n).product", "fact", 10),
          VariableSpecifications += VariableSpecificationsRow(3, 1, 0, 0, "n", Some(0), Some(10)),
          FunctionAssoc += FunctionAssocRow(Some(1), Some(1)),
          FunctionAssoc += FunctionAssocRow(Some(2), Some(2)),
          FunctionAssoc += FunctionAssocRow(Some(2), Some(3)),
          McAnswers += McAnswersRow(Some(1), Some(1), Some(1), 0, true),
          McAnswers += McAnswersRow(Some(1), Some(1), Some(2), 0, false),
          CodeAnswers += CodeAnswersRow(Some(1), Some(1), 1, 1, "code", false),
          CodeAnswers += CodeAnswersRow(Some(1), Some(1), 1, 1, "code", true)))
      })
    Ok("Setup complete")
  }

  // POST Actions

  def verifyLogin = Action.async(implicit request => {
    Forms.userForm.bindFromRequest().fold(
      formWithErrors => {
        Future { Ok(views.html.mainMenu(formWithErrors)) }
      },
      value => {
        val db = dbConfig.db
        Queries.validLogin(value, db).flatMap(_ match {
          case -1 =>
            Future(Redirect(routes.ApplicationController.index))
          case n =>
            val instructorCourses = Queries.instructorCourseIds(n, db)
            instructorCourses.map(_ match {
              case Seq() =>
                Redirect(routes.Application.quizList).withSession(request.session + ("username" -> value.username) + ("userid" -> n.toString))
              case _ =>
                Redirect(routes.Application.instructorPage).withSession(request.session + ("username" -> value.username) + ("userid" -> n.toString) + ("instructor" -> "yes"))
            })
        })
      })
  })

  def submitQuiz = AuthenticatedAction { implicit request =>
    {
      val db = dbConfig.db
      val userid = request.session("userid").toInt
      request.body.asFormUrlEncoded match {
        case Some(params) =>
          val quizid = params("quizid")(0).toInt
          // Get quiz specs from database and check correctness
          for (key <- params.keys; if key.startsWith("mc-")) {
            val mcid = key.drop(3).toInt
            val pspec = ProblemSpec(ProblemSpec.MultipleChoiceType, mcid, db)
            println("You answered "+params(key)(0))
            val correct = pspec.map(_.checkResponse(params(key)(0)))
            val selection = try { params(key)(0).toInt } catch { case e: NumberFormatException => -1 }
            correct.map(c => db.run(McAnswers += McAnswersRow(Some(userid), Some(quizid), Some(mcid), selection, c)))
          }
          for (key <- params.keys; if key.startsWith("code-")) {
            val Array(codeid, qtype) = key.drop(5).split("-")
            val pspec = ProblemSpec(qtype.toInt, codeid.toInt, db)
            val correct = pspec.map(_.checkResponse(params(key)(0)))
            correct.map(c => db.run(CodeAnswers += CodeAnswersRow(Some(userid), Some(quizid), codeid.toInt, qtype.toInt, params(key)(0), c)))
          }
        case None =>
      }
      Future(Redirect(routes.Application.quizList()).flashing("refresh-delay" -> "5"))
    }
  }

  def addCoursePost = AuthenticatedInstructorAction { implicit request =>
    {
      newCourseForm.bindFromRequest().fold(
        formWithErrors => {
          Future { Ok(views.html.addCourse(formWithErrors)) }
        },
        value => {
          val db = dbConfig.db
          val userid = request.session("userid").toInt
          Queries.addCourse(value, userid, db)
          Future { Redirect(routes.Application.instructorPage) }
        })
    }
  }

  def editQuizPost = AuthenticatedInstructorAction { implicit request =>
    val db = dbConfig.db
    val userid = request.session("userid").toInt
    request.body.asFormUrlEncoded match {
      case Some(params) =>
        val quizid = params("quizid")(0).toInt
        val name = params("name")(0)
        val description = params("description")(0)
        if (quizid < 1) {
          db.run(Quizzes += QuizzesRow(quizid, description, name))
        } else {
          db.run(Quizzes.filter(_.quizid === quizid).update(QuizzesRow(quizid, description, name)))
        }
        Future { Redirect(routes.Application.instructorPage).flashing("message" -> "Quiz saved.") }
      case None =>
        Future { Redirect(routes.Application.instructorPage).flashing("message" -> "Quiz not saved, no data.") }
    }
  }

  def multipleChoiceEditPost = AuthenticatedInstructorAction { implicit request =>
    val db = dbConfig.db
    val userid = request.session("userid").toInt
    request.body.asFormUrlEncoded match {
      case Some(params) =>
        val id = params("id")(0).toInt
        val prompt = params("prompt")(0)
        val opt1 = params("opt-0")(0)
        val opt2 = params("opt-1")(0)
        val opts = (2 to 7) map { i => val opt = params.get("opt-" + i); if (opt.isEmpty || opt.get(0).trim.isEmpty) None else opt.map(_(0)) }
        val correct = params("correct")(0).toInt
        if (id < 1) {
          db.run(MultipleChoiceQuestions += MultipleChoiceQuestionsRow(id, prompt, opt1, opt2, opts(0), opts(1), opts(2), opts(3), opts(4), opts(5), correct))
        } else {
          db.run(MultipleChoiceQuestions.filter(_.mcQuestionId === id).update(MultipleChoiceQuestionsRow(id, prompt, opt1, opt2, opts(0), opts(1), opts(2), opts(3), opts(4), opts(5), correct)))
        }
        Future { Redirect(routes.Application.instructorPage).flashing("message" -> "Question saved.") }
      case None =>
        Future { Redirect(routes.Application.instructorPage).flashing("message" -> "Question not saved, no data.") }
    }
  }

  // AJAX Calls

  def associateMCQuestionWithQuiz(questionid: Int, quizid: Int) = AuthenticatedInstructorAction { implicit request =>
    dbConfig.db.run(MultipleChoiceAssoc += MultipleChoiceAssocRow(Some(quizid), Some(questionid)))
    Future { Ok("good") }
  }

  def associateFuncQuestionWithQuiz(questionid: Int, quizid: Int) = TODO

  def associateLambdaQuestionWithQuiz(questionid: Int, quizid: Int) = TODO

  def associateExprQuestionWithQuiz(questionid: Int, quizid: Int) = TODO

  def associateQuizWithCourse(quizid: Int, courseid: Int, dateTime: String) = AuthenticatedInstructorAction { implicit request =>
    val zoneId = ZoneId.of("America/Chicago")
    val time = Timestamp.from(java.time.Instant.from(ZonedDateTime.of(LocalDateTime.parse(dateTime), zoneId)))
    dbConfig.db.run(QuizCourseCloseAssoc += QuizCourseCloseAssocRow(Some(quizid), Some(courseid), time))
    Future { Ok("good") }
  }

  def createUser(username: String, id: String) = AuthenticatedInstructorAction { implicit request =>
    val db = dbConfig.db
    db.run(Users.filter(_.username === username).result).foreach(s => {
      if (s.isEmpty) db.run(Users += UsersRow(0, username, id))
    })
    Future { Ok("good") }
  }

  def removeQuestionQuizAssoc(questionid: Int, questionType: Int, quizid: Int) = AuthenticatedInstructorAction { implicit request =>
    println("Associate mc question " + questionid + " with " + quizid)
    val db = dbConfig.db
    import slick.driver.MySQLDriver.api._
    questionType match {
      case ProblemSpec.MultipleChoiceType =>
        db.run(MultipleChoiceAssoc.filter(a => a.mcQuestionId === questionid && a.quizid === quizid).delete)
      case ProblemSpec.FunctionType =>
        db.run(FunctionAssoc.filter(a => a.funcQuestionId === questionid && a.quizid === quizid).delete)
      case ProblemSpec.LambdaType =>
        db.run(LambdaAssoc.filter(a => a.lambdaQuestionId === questionid && a.quizid === quizid).delete)
      case ProblemSpec.ExpressionType =>
        db.run(ExpressionAssoc.filter(a => a.exprQuestionId === questionid && a.quizid === quizid).delete)
    }
    Future { Ok("good") }
  }

  // Other methods

  private def authenticate(request: Request[AnyContent]): Boolean = {
    request.session.get("username") match {
      case None => false
      case Some(uname) => true
    }
  }

  private def isInstructor(request: Request[AnyContent]): Boolean = {
    request.session.get("instructor") match {
      case Some("yes") => true
      case _ => false
    }
  }

  private def AuthenticatedAction(f: Request[AnyContent] => Future[Result]): Action[AnyContent] = {
    Action.async { request =>
      if (authenticate(request)) {
        f(request)
      } else {
        Future { Redirect(routes.ApplicationController.index()) }
      }
    }
  }

  private def AuthenticatedInstructorAction(f: Request[AnyContent] => Future[Result]): Action[AnyContent] = {
    Action.async { request =>
      if (authenticate(request) && isInstructor(request)) {
        f(request)
      } else {
        Future { Redirect(routes.ApplicationController.index()) }
      }
    }
  }
}
