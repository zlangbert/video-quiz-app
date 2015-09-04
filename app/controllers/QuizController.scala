package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.User
import play.api.i18n.MessagesApi
import services.QuizService

import scala.concurrent.Future

class QuizController @Inject()(val messagesApi: MessagesApi,
                               val env: Environment[User, CookieAuthenticator],
                               quizService: QuizService)
  extends Silhouette[User, CookieAuthenticator] {

  def list = SecuredAction.async { implicit request =>
    Future.successful(Ok(request.identity.toString))
    /*val quizzes = ???
    Future.successful(Ok(views.html.quizList(quizzes)))*/
  }
}