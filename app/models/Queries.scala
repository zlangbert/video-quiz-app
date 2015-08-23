package models

import slick.driver.MySQLDriver.api._
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
  
  def multipleChoiceData(quizid:Int, userid:Int, db:Database): Future[Seq[MultipleChoiceData]] = {
    val rows = db.run {
      (for {
        (mcq,mca) <- MultipleChoiceQuestions join MultipleChoiceAssoc on (_.mcQuestionId === _.mcQuestionId)
        if mca.quizid === quizid
      } yield mcq).result
    }
    val data = rows.flatMap(s => Future.sequence { 
      for {
        row <- s
      } yield {
        val options = Seq(row.option1, row.option2) ++ Seq(row.option3, row.option4, row.option5, row.option6,
            row.option7, row.option8).filter(_.nonEmpty).map(_.get)
        val userAnswer = db.run((for{
          ans <- McAnswers
          if ans.userid === userid && ans.quizid === quizid && ans.mcQuestionId === row.mcQuestionId
        } yield ans.selection).result)
        userAnswer.map(ans => MultipleChoiceData(row.mcQuestionId, row.prompt, options, row.correctOption, if(ans.isEmpty) None else Some(ans.head)))
      }
    })
    data
  }

  def codeQuestionData(quizid:Int, userid:Int, db:Database): Future[Seq[CodeQuestionData]] = {
    val funcRows = db.run {
      (for {
        (fq,fa) <- FunctionQuestions join FunctionAssoc on (_.funcQuestionId === _.funcQuestionId)
        if fa.quizid === quizid
      } yield fq).result
    }
    val funcData = funcRows.flatMap(s => Future.sequence { 
      for {
        row <- s
      } yield {
        val userAnswers = db.run((for{
          ans <- CodeAnswers
          if ans.userid === userid && ans.quizid === quizid && ans.questionId === row.funcQuestionId && ans.questionType === 1
        } yield ans).result)
        userAnswers.map(ans => CodeQuestionData(row.funcQuestionId, 1, row.prompt, if(ans.isEmpty) None else Some(ans.last.answer), ans.exists(_.correct)))
      }
    })
    val lambdaRows = db.run {
      (for {
        (lq,la) <- LambdaQuestions join LambdaAssoc on (_.lambdaQuestionId === _.lambdaQuestionId)
        if la.quizid === quizid
      } yield lq).result
    }
    val lambdaData = lambdaRows.flatMap(s => Future.sequence { 
      for {
        row <- s
      } yield {
        val userAnswers = db.run((for{
          ans <- CodeAnswers
          if ans.userid === userid && ans.quizid === quizid && ans.questionId === row.lambdaQuestionId && ans.questionType === 2
        } yield ans).result)
        userAnswers.map(ans => CodeQuestionData(row.lambdaQuestionId, 2, row.prompt, if(ans.isEmpty) None else Some(ans.last.answer), ans.exists(_.correct)))
      }
    })
    val exprRows = db.run {
      (for {
        (eq,ea) <- ExpressionQuestions join ExpressionAssoc on (_.exprQuestionId === _.exprQuestionId)
        if ea.quizid === quizid
      } yield eq).result
    }
    val exprData = exprRows.flatMap(s => Future.sequence { 
      for {
        row <- s
      } yield {
        val userAnswers = db.run((for{
          ans <- CodeAnswers
          if ans.userid === userid && ans.quizid === quizid && ans.questionId === row.exprQuestionId && ans.questionType === 3
        } yield ans).result)
        userAnswers.map(ans => CodeQuestionData(row.exprQuestionId, 3, row.prompt, if(ans.isEmpty) None else Some(ans.last.answer), ans.exists(_.correct)))
      }
    })
    for {
      fd <- funcData
      ld <- lambdaData
      ed <- exprData
    } yield fd ++ ld ++ ed
  }

  def quizData(quizid:Int, userid:Int, db:Database): Future[QuizData] = {
    val quizRow = db.run(Quizzes.filter(_.quizid === quizid).result.head)
    val mcQuestions = multipleChoiceData(quizid, userid, db)
    val codeQuestions = codeQuestionData(quizid, userid, db)
    for {
      qr <- quizRow
      mc <- mcQuestions
      cq <- codeQuestions
    } yield QuizData(quizid,userid,qr.name,qr.description,mc,cq)
  }
}