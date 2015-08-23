package models

import slick.driver.MySQLDriver.api._
import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.duration.Duration
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import java.sql.Timestamp
import java.util.Date
import Tables._

/**
 * @author mlewis
 */
object Queries {
  // Courses
  def coursesFor(userid: Int, db: Database): Future[Seq[CoursesRow]] = {
    db.run {
      (for {
        uca <- UserCourseAssoc
        if uca.userid === userid
        c <- Courses
        if c.courseid === uca.courseid
      } yield c).result.map(_.distinct)
    }
  }

  // Users
  def validLogin(user: UsersRow, db: Database): Future[Int] = {
    val matches = db.run(Users.filter(u => u.username === user.username && u.trinityid === user.trinityid).result)
    matches.map(us => if (us.isEmpty) -1 else us.head.userid)
  }

  def fetchUserByName(username: String, db: Database): Future[UsersRow] = {
    db.run { Users.filter(u => u.username === username).result.head }
  }

  // Quizzes
  def allQuizzesForClass(courseid: Int, db: Database): Future[Seq[(QuizzesRow, Timestamp)]] = {
    db.run {
      (for {
        qca <- QuizCourseCloseAssoc
        if qca.courseid === courseid
        q <- Quizzes
        if q.quizid === qca.quizid
      } yield (q, qca.closeTime)).result
    }
  }

  def currentQuizzesForClass(courseid: Int, db: Database): Future[Seq[QuizzesRow]] = {
    val now = new Timestamp(new Date().getTime)
    db.run {
      (for {
        qca <- QuizCourseCloseAssoc
        if qca.courseid === courseid && qca.closeTime > now
        q <- Quizzes
        if q.quizid === qca.quizid
      } yield q).result
    }
  }

  def numberOfQuestions(quizid: Int, db: Database): Future[Int] = {
    val mcCount = db.run {
      MultipleChoiceAssoc.filter(_.quizid === quizid).length.result
    }
    val funcCount = db.run {
      FunctionAssoc.filter(_.quizid === quizid).length.result
    }
    val lambdaCount = db.run {
      LambdaAssoc.filter(_.quizid === quizid).length.result
    }
    val exprCount = db.run {
      ExpressionAssoc.filter(_.quizid === quizid).length.result
    }
    for(mc <- mcCount; fc <- funcCount; lc <- lambdaCount; ec <- exprCount) yield mc+fc+lc+ec
  }
  
  def numberOfCorrectQuestions(quizid: Int, userid: Int, db: Database): Future[Int] = {
    val mcCount = db.run {
      McAnswers.filter(a => a.quizid === quizid && a.userid === userid && a.correct).map(_.mcQuestionId).result
    }
    val codeCount = db.run {
      CodeAnswers.filter(a => a.quizid === quizid && a.userid === userid && a.correct)
        .map(a => a.questionId -> a.questionType).result
    }
    for{
      mc <- mcCount
      code <- codeCount
    } yield {
      mc.distinct.length + code.distinct.length
    }
  }

  def quizScore(quizid: Int, userid: Int, db: Database): Future[(Int, Int)] = {
    val total = numberOfQuestions(quizid,db)
    val correct = numberOfCorrectQuestions(quizid, userid, db);
    for(t <- total; c <- correct) yield (c,t)
  }

}