package models

import com.mohiva.play.silhouette.api.Identity

case class User(id: String,
                provider: String,
                email: String,
                username: String,
                firstName: Option[String],
                lastName: Option[String],
                avatarUrl: Option[String]) extends Identity