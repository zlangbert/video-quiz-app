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

  /**
   * Gets all the quiz info for a user
   * @param user The user to get info for
   * @return A map of course to their quiz info
   *
   * @note This whole thing is probably sub-optimal. There is most likely a way to
   *       reduce the number of queries run.
   */
  override def forUser(user: models.User): Future[Map[CourseRow, CourseQuizzes]] = {

    /**
     * Converts db quiz data to our model
     */
    def toCourseQuizMap(data: Seq[(CourseRow, QuizRow, CourseQuizRow)],
                        scores: Map[Int, (Int, Int)]): Map[CourseRow, CourseQuizzes] = {

      def toQuizInfo(data: (CourseRow, QuizRow, CourseQuizRow), scores: Map[Int, (Int, Int)]): UserQuizInfo = {
        val (_, quiz, cq) = data
        val score = scores.getOrElse(quiz.id, -1 -> -1)
        UserQuizInfo(quiz.id, quiz.name, quiz.description,
          cq.openTime.toLocalDateTime, cq.closeTime.toLocalDateTime, score)
      }

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

    /**
     * Gets the number of questions for each quiz
     */
    def numQuestions(data: Seq[(Tables.CourseRow, Tables.QuizRow, Tables.CourseQuizRow)]) = {

      def forQuiz(quizId: Int) = (for {
        qq <- Tables.QuizQuestion if qq.quizId === quizId
        q <- qq.questionFk
      } yield qq -> q).groupBy(_._1.quizId).map { case (qi, group) =>
        qi -> group.map(_._2).length
      }.result

      data.map { case (_, q, _) =>
        forQuiz(q.id)
      }
    }

    /**
     * Gets the number of correct questions for each quiz
     */
    def numCorrect(data: Seq[(Tables.CourseRow, Tables.QuizRow, Tables.CourseQuizRow)]) = {

      def forQuiz(quizId: Int) = (for {
        a <- Tables.Answer if a.userId === user.id && a.quizId === quizId && a.isCorrect === true
      } yield a).groupBy(_.quizId).map { case (qi, group) =>
        qi -> group.length
      }.result

      data.map { case (_, q, _) =>
        forQuiz(q.id)
      }
    }

    val quizData = (for {
      userCourse <- Tables.UserCourse if userCourse.userId === user.id
      course <- userCourse.courseFk
      courseQuiz <- Tables.CourseQuiz if courseQuiz.courseId === course.id
      userQuiz <- Tables.UserQuiz if courseQuiz.quizId === userQuiz.quizId
      quiz <- userQuiz.quizFk
    } yield (course, quiz, courseQuiz)).result

    db run {
      (for {
        data <- quizData
        numQuestions <- DBIO.sequence(numQuestions(data)).transactionally
        numCorrect <- DBIO.sequence(numCorrect(data))
      } yield {
          val numCorrectMap = numCorrect.flatten.toMap
          val scores = numQuestions.flatten.toMap.map { case (quizId, nc) =>
            (quizId, (numCorrectMap.getOrElse(quizId, 0), nc))
          }
        toCourseQuizMap(data, scores)
      }).transactionally
    }
  }
}