package controllers

import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data.Form
import models._
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class Application extends Controller {
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  import dbConfig.driver.api._

  val userForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "trinityid" -> nonEmptyText)((un,id) => User(0,un,id))(u => Some(u.username -> u.trinityid)))

  def index = Action(request => {
    Ok(views.html.mainMenu(userForm))
  })
  
  def index2(name: String) = Action.async { implicit request =>
    val resultingUsers: Future[Seq[User]] = dbConfig.db.run(Users.users.filter(_.username === name).result)
    resultingUsers.map(users => Ok(users.toString)) //views.html.index()))
  }

  def quizList = AuthenticatedAction(request => {
        val uname = request.session.get("username").getOrElse("No name")
        println("Username is "+uname)
        val classes = ClassInfo.classesFor(uname)
        val problems = for(c <- classes; (n,(ps,d)) <- c.problemSets) yield ps
        Ok(views.html.quizList(problems))
  })

  def fetch(user: String) = AuthenticatedAction { request =>
    Ok(ClassInfo.classesFor(user).toString())
  }

  def verifyLogin = Action.async (implicit request => {
    println("Verify")
    userForm.bindFromRequest().fold(
      formWithErrors => {
        println(formWithErrors)
        println(userForm)
        Future { Redirect(routes.Application.index) }},
      value =>
        Users.validLogin(value,dbConfig.db).map(_ match {
          case true =>
            println(value+" Good")
            Redirect(routes.Application.quizList).withSession(request.session + ("username" -> value.username))
          case false => 
            println(value+" Bad")
            Redirect(routes.Application.index)
        })
    )
  })
  
  def logout = Action { implicit request =>
    Redirect(routes.Application.index()).withSession(request.session - "username")
  }

  // Other methods
  
  private def authenticate(request:Request[AnyContent]):Boolean = {
    request.session.get("username") match {
      case None => false
      case Some(uname) => true
    }
  }
  
  private def AuthenticatedAction(f: Request[AnyContent] => Result):Action[AnyContent] = {
    Action { request =>
      if(authenticate(request)) {
        f(request)
      } else {
        Redirect(routes.Application.index())
      }
    }
  }

}
