package controllers

import javax.inject.Inject

import play.api.mvc._
import services.QuizService

import scala.concurrent.Future

class QuizController @Inject()(quizService: QuizService) extends Controller {

  def list = Action.async { implicit request =>
    val quizzes = ???
    Future.successful(Ok(views.html.quizList(quizzes)))
  }
}