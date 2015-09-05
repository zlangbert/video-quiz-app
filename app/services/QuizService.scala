package services

import models.Tables.CourseRow
import models.User
import models.redux.CourseQuizzes

import scala.concurrent.Future

trait QuizService {

  def forUser(user: User): Future[Map[CourseRow, CourseQuizzes]]
}