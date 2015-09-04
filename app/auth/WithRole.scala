package auth

import auth.Role.{Instructor, Role}
import com.mohiva.play.silhouette.api.Authorization
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.User
import play.api.i18n.Messages
import play.api.mvc.Request

import scala.concurrent.Future

case class WithRole(role: Role) extends Authorization[User, CookieAuthenticator] {
  override def isAuthorized[B](identity: User, authenticator: CookieAuthenticator)
                              (implicit request: Request[B], messages: Messages): Future[Boolean] = {
    Future.successful {
      role match {
        case Instructor => identity.isInstructor
      }
    }
  }
}