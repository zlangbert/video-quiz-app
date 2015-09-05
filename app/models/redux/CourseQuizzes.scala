package models.redux

case class CourseQuizzes(open: Seq[UserQuizInfo], closed: Seq[UserQuizInfo])