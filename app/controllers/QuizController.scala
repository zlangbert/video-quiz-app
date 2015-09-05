package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.User
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits._
import services.QuizService

import scala.language.implicitConversions

class QuizController @Inject()(val messagesApi: MessagesApi,
                               val env: Environment[User, CookieAuthenticator],
                               quizService: QuizService)
  extends Silhouette[User, CookieAuthenticator] {

  def list = SecuredAction.async { implicit request =>
    implicit val user = request.identity
    for {
      quizzes <- quizService.forUser(request.identity)
    } yield {
      Ok(views.html.v2.quizzes(quizzes))
    }
  }
}