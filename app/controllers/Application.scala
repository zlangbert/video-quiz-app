package controllers

import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data.Form
import models._

class Application extends Controller {

  val userForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText)(User.apply)(User.unapply))

  def index = Action(request => {
    Ok(views.html.mainMenu(userForm))
  })

  def quizList = Action(request => {
    request.session.get("username") match {
      case None =>
        println("No username")
        Redirect(routes.Application.index())
      case Some(uname) =>
        println("Username is "+uname)
        val classes = ClassInfo.classesFor(uname)
        val problems = for(c <- classes; (n,(ps,d)) <- c.problemSets) yield ps
        Ok(views.html.quizList(problems))
    }
  })

  def fetch(user: String) = Action {
    Ok(ClassInfo.classesFor(user).toString())
  }

  def verifyLogin = Action(implicit request => {
    userForm.bindFromRequest().fold(
      formWithErrors => 
        Redirect(routes.Application.index),
      value =>
        if(value.username=="mlewis" && value.password=="password") {
          Redirect(routes.Application.quizList).withSession(request.session + ("username" -> "mlewis"))
        } else {
          Redirect(routes.Application.index)
        }
    )
  })

  // Other methods

  private def ifValidUser(session: Session)(result: User => Result): Result = {
    fetchUser(session) match {
      case Some(user) =>
        result(user)
      case None =>
        Ok(views.html.index())
    }
  }

  private def fetchUser(session: Session): Option[User] = {
    session.get("login") match {
      case Some(sessionKey) => Users.lookupUser(sessionKey)
      case None             => None
    }
  }
}
