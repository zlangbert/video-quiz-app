package services.impl

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.User
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import models.Tables
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits._
import services.UserService
import slick.driver.JdbcProfile

import scala.concurrent.Future

class UserServiceImpl @Inject()(dbConfigProvider: DatabaseConfigProvider) extends UserService {

  val profile = dbConfigProvider.get[JdbcProfile]
  val db = profile.db

  import profile.driver.api._

  /**
   * @inheritdoc
   */
  override def save(profile: CommonSocialProfile): Future[User] = {
    assert(profile.email.isDefined, "Attempting to save profile with empty email")

    val user = User(profile.loginInfo, profile.firstName, profile.lastName, profile.fullName,
      profile.email, profile.avatarURL)
    db.run(
      Tables.User += Tables.UserRow(profile.loginInfo.providerKey, profile.loginInfo.providerID,
        profile.email.get, profile.firstName, profile.lastName)
    ).map { _ =>
      user
    }
  }

  /**
   * Gets a user from [[LoginInfo]] if they exist
   * @param loginInfo The login info
   * @return The user if they exist
   */
  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    implicit def s2Opt(s: String): Option[String] = Option(s)
    db.run(
      Tables.User.filter(_.id === loginInfo.providerKey).result.headOption
    ).map {
      _.map { u =>
        User(LoginInfo(u.provider, u.id), u.firstName, u.lastName, None, u.email, None)
      }
    }
  }
}