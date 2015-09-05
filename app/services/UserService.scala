package services

import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import models.User

import scala.concurrent.Future

trait UserService extends IdentityService[User] {

  /**
   * Saves the social profile for a user.
   *
   * If a user exists for this profile then update the user, otherwise create a v2 user with the given profile.
   *
   * @param profile The social profile to save.
   * @return The user for whom the profile was saved.
   */
  def save(profile: CommonSocialProfile): Future[User]
}

object UserService {

  /**
   * Thrown when the google account being used is not
   * a trinity account
   */
  class InvalidAccountException extends Exception
}