package services.impl

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.User
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import services.UserService

import scala.concurrent.Future

class UserServiceImpl extends UserService {

  /**
   * @inheritdoc
   */
  override def save(profile: CommonSocialProfile): Future[User] = ???

  /**
   * Gets a user from [[LoginInfo]] if they exist
   * @param loginInfo The login info
   * @return The user if they exist
   */
  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = ???
}
