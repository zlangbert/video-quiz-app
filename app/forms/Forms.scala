package forms

import models.Tables.UsersRow
import play.api.data.Form
import play.api.data.Forms._

object Forms {

  val userForm = Form(
    mapping(
      "Username" -> nonEmptyText,
      "Trinity ID" -> nonEmptyText)((un, id) => UsersRow(0, un, id))(u => Some(u.username -> u.trinityid)))
}