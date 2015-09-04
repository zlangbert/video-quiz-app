package services.impl

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import models.{Tables, User}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits._
import services.UserService
import services.UserService.InvalidAccountException
import slick.driver.JdbcProfile

import scala.concurrent.Future

class UserServiceImpl @Inject()(dbConfigProvider: DatabaseConfigProvider) extends UserService {

  private val EmailPattern = """^(\w{2,8}\d{0,2})@trinity\.edu$""".r

  private val profile = dbConfigProvider.get[JdbcProfile]
  private val db = profile.db

  import profile.driver.api._

  /**
   * @inheritdoc
   */
  override def save(profile: CommonSocialProfile): Future[User] = {
    assert(profile.email.isDefined, "Attempting to save profile with empty email")

    val (email, username) = profile.email.map {
      case e@EmailPattern(u) => e -> u
      case _ => throw new InvalidAccountException
    }.get

    db.run(
      Tables.User insertOrUpdate Tables.UserRow(profile.loginInfo.providerKey, profile.loginInfo.providerID,
        email, username, profile.firstName, profile.lastName, profile.avatarURL)
    ).map { _ =>
      User(profile.loginInfo.providerKey, profile.loginInfo.providerID, email, username,
        profile.firstName, profile.lastName, profile.avatarURL, isInstructor = false)
    }
  }

  /**
   * Gets a user from [[LoginInfo]] if they exist
   * @param loginInfo The login info
   * @return The user if they exist
   */
  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    db.run(
      Tables.User.filter(_.id === loginInfo.providerKey).result.headOption
    ).map {
      _.map { u =>
        User(u.id, u.provider, u.email, u.username, u.firstName, u.lastName,
          u.avatarUrl, u.isInstructor.getOrElse(false))
      }
    }
  }
}