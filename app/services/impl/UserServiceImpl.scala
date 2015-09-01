package services.impl

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.User
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import services.UserService

import scala.collection.mutable
import scala.concurrent.Future

class UserServiceImpl extends UserService {

  import UserServiceImpl._

  /**
   * @inheritdoc
   */
  override def save(profile: CommonSocialProfile): Future[User] = {
    val user = User(profile.loginInfo, profile.firstName, profile.lastName, profile.fullName,
      profile.email, profile.avatarURL)
    users += profile.loginInfo -> user
    Future.successful(user)
  }

  /**
   * Gets a user from [[LoginInfo]] if they exist
   * @param loginInfo The login info
   * @return The user if they exist
   */
  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    val user = users.get(loginInfo)
    Future.successful(user)
  }
}

object UserServiceImpl {

  val users = mutable.Map[LoginInfo, User]()
}