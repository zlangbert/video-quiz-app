package models

import com.mohiva.play.silhouette.api.Identity

case class User(id: String,
                provider: String,
                email: String,
                username: String,
                firstName: Option[String],
                lastName: Option[String],
                avatarUrl: Option[String],
                isInstructor: Boolean) extends Identity

object User {

  implicit class UserOps(user: User) {
    def name: String = user.firstName.getOrElse("") + " " + user.lastName.getOrElse("")
  }
}