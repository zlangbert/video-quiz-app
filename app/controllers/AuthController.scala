package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.{LoginEvent, Logger, Environment, Silhouette}
import com.mohiva.play.silhouette.impl.User
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.{SocialProviderRegistry, CommonSocialProfileBuilder, SocialProvider}
import forms.Forms
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc._
import services.UserService
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

class AuthController @Inject()(val messagesApi: MessagesApi,
                               val env: Environment[User, CookieAuthenticator],
                               userService: UserService,
                               authInfoRepository: AuthInfoRepository,
                               socialProviderRegistry: SocialProviderRegistry)
  extends Silhouette[User, CookieAuthenticator] with Logger {

  def login = Action { implicit request =>
    Ok(views.html.mainMenu(Forms.userForm))
  }

  def authenticate(provider: String) = Action.async { implicit request =>
    (socialProviderRegistry.get[SocialProvider](provider) match {
      case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
        p.authenticate().flatMap {
          case Left(result) => Future.successful(result)
          case Right(authInfo) => for {
            profile <- p.retrieveProfile(authInfo)
            user <- userService.save(profile)
            authInfo <- authInfoRepository.save(profile.loginInfo, authInfo)
            authenticator <- env.authenticatorService.create(profile.loginInfo)
            value <- env.authenticatorService.init(authenticator)
            result <- env.authenticatorService.embed(value, Redirect(routes.Application.index()))
          } yield {
              env.eventBus.publish(LoginEvent(user, request, request2Messages))
              result
            }
        }
      case _ => Future.failed(new ProviderException(s"Cannot authenticate with unexpected social provider $provider"))
    }).recover {
      case e: ProviderException =>
        logger.error("Unexpected provider error", e)
        Redirect(routes.Application.index()).flashing("error" -> Messages("could.not.authenticate"))
    }
  }
}