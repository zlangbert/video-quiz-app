package app

import javax.inject.Inject

import com.google.inject.Provider
import com.mohiva.play.silhouette.api.SecuredErrorHandler
import controllers.routes
import play.api.http.DefaultHttpErrorHandler
import play.api.i18n.Messages
import play.api.mvc.RequestHeader
import play.api.mvc.Results._
import play.api.routing.Router
import play.api.{Configuration, Environment, OptionalSourceMapper}

import scala.concurrent.Future

class ErrorHandler @Inject()(env: Environment,
                             config: Configuration,
                             sourceMapper: OptionalSourceMapper,
                             router: Provider[Router])
  extends DefaultHttpErrorHandler(env, config, sourceMapper, router) with SecuredErrorHandler {

  override def onNotAuthenticated(request: RequestHeader, messages: Messages) = {
    Some(Future.successful(Redirect(routes.AuthController.login())))
  }
}