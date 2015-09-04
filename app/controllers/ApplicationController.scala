package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.User
import play.api.i18n.MessagesApi

class ApplicationController @Inject()(val messagesApi: MessagesApi,
                                      val env: Environment[User, CookieAuthenticator])
  extends Silhouette[User, CookieAuthenticator] {

  def index = SecuredAction { implicit request =>
    Redirect(routes.Application.quizList())
  }
}