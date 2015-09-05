package services.impl

import java.time.LocalDateTime
import javax.inject.Inject

import models.Tables
import models.Tables.{CourseQuizRow, CourseRow, QuizRow}
import models.redux.{CourseQuizzes, UserQuizInfo}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits._
import services.QuizService
import slick.driver.JdbcProfile

import scala.concurrent.Future

class QuizServiceImpl @Inject()(dbConfigProvider: DatabaseConfigProvider) extends QuizService {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db

  import dbConfig.driver.api._

  override def forUser(user: models.User): Future[Map[CourseRow, CourseQuizzes]] = {

    def toCourseQuizMap(data: Seq[(CourseRow, QuizRow, CourseQuizRow)],
                        scores: Map[Int, (Int, Int)]): Map[CourseRow, CourseQuizzes] = {
      val now = LocalDateTime.now()
      data.groupBy(_._1).map { case (course, quizzes) =>
        val (open, closed) = quizzes.partition { case (_, quiz, courseQuiz) =>
          now.isAfter(courseQuiz.closeTime.toLocalDateTime)
        }
        val courseQuizzes = CourseQuizzes(
          open.map(toQuizInfo(_, scores)),
          closed.map(toQuizInfo(_, scores))
        )
        course -> courseQuizzes
      }
    }

    def toQuizInfo(data: (CourseRow, QuizRow, CourseQuizRow), scores: Map[Int, (Int, Int)]): UserQuizInfo = {
      val (_, quiz, cq) = data
      val score = scores.getOrElse(quiz.id, -1 -> -1)
      UserQuizInfo(quiz.id, quiz.name, quiz.description,
        cq.openTime.toLocalDateTime, cq.closeTime.toLocalDateTime, score)
    }

    def numQuestions(quizId: Int) = (for {
      qq <- Tables.QuizQuestion if qq.quizId === quizId
      q <- qq.questionFk
    } yield qq -> q).groupBy(_._1.quizId).map { case (_, css) =>
      css.map(_._2).length
    }.result

    /*val scores = (for {
      userQuiz <- Tables.UserQuiz if userQuiz.userId === user.id
      quiz <- userQuiz.quizFk
      answer <- Tables.Answer if answer.userId === user.id && answer.quizId === quiz.id
    } yield (quiz, answer)).groupBy(_._1).map { case (quiz, css) =>
      val correct = css.map(_._2).filter(_.isCorrect).length
      val total = css.map(_._2).length
      (quiz.id, (correct, total))
    }.result*/

    val quizData = (for {
      userCourse <- Tables.UserCourse if userCourse.userId === user.id
      course <- userCourse.courseFk
      courseQuiz <- Tables.CourseQuiz if courseQuiz.courseId === course.id
      userQuiz <- Tables.UserQuiz if courseQuiz.quizId === userQuiz.quizId
      quiz <- userQuiz.quizFk
    } yield (course, quiz, courseQuiz)).result
    quizData.statements.foreach(println)

    db run {
      (for {
        //s <- scores
        q <- quizData
      } yield {
        toCourseQuizMap(q, Map.empty)
      }).transactionally
    }
  }
}