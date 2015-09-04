package auth

object Role {
  sealed trait Role
  case object Instructor extends Role
}