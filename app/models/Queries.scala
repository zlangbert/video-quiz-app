package models

import java.sql.Timestamp
import java.util.Date

import models.Tables._
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.MySQLDriver.api._

import scala.concurrent.Future

/**
 * @author mlewis
 */
object Queries {
  def main(args: Array[String]) {
  }

  val Student = 1
  val Instructor = 2

  // Courses
  def coursesFor(userId: String, db: Database): Future[Seq[CoursesRow]] = {
    db.run {
      (for {
        uca <- UserCourseAssoc
        if uca.userId === userId
        c <- Courses
        if c.courseid === uca.courseid
      } yield c).result.map(_.distinct)
    }
  }

  def addCourse(ncd: NewCourseData, userId: String, db: Database): Unit = {
    ???
    /*val StudentRegex = """(\d{7})|(\w{2,8})@trinity\.edu""".r
    // Create course entry
    db.run(Courses += CoursesRow(0, ncd.code, ncd.semester, ncd.section)).foreach { cnt =>
      if (cnt > 0) {
        db.run(Courses.filter(cr => cr.code === ncd.code && cr.semester === ncd.semester && cr.section === ncd.section).result.head).foreach(cr => {
          // Add current user as instructor
          db.run(UserCourseAssoc += UserCourseAssocRow(Some(userId), Some(cr.courseid), Instructor))
          // Parse instructors and add associations
          val instructors = ncd.instructorNames.split("\\s+").filter(_.trim.nonEmpty)
          instructors.foreach(i => {
            db.run(Users.filter(u => u.username === i).result).foreach { matches => {
              if (matches.isEmpty) println("Attempt to add non-existant instructor: " + i)
              else db.run(UserCourseAssoc += UserCourseAssocRow(Some(matches.head.userid), Some(cr.courseid), Instructor))
            }
            }
          })
          // Parse students and add associations
          ncd.studentData.split("\n").foreach(l => println("Line = " + l))
          val d = (for (StudentRegex(id, uname) <- ncd.studentData.split("\n").map(_.trim)) yield {
            println("Matching " + id + ", " + uname)
            (id, uname)
          }).dropWhile(_._1 == null)
          d foreach println
          if (d.length > 1) {
            val userTuples = for (((a, null), (null, d)) <- d.zip(d.tail)) yield (a, d)
            userTuples foreach println
            for ((id, uname) <- userTuples) {
              db.run(Users.filter(u => u.username === uname).result).foreach { matches => {
                if (matches.isEmpty) {
                  db.run(Users += UsersRow(0, uname, id)).foreach(_ =>
                    db.run(Users.filter(u => u.username === uname).result).foreach { newUser =>
                      db.run(UserCourseAssoc += UserCourseAssocRow(Some(newUser.head.userid), Some(cr.courseid), Student))
                    })
                } else {
                  db.run(UserCourseAssoc += UserCourseAssocRow(Some(matches.head.userid), Some(cr.courseid), Student))
                }
              }
              }
            }
          }
        })
      } else {
        println("cnt was " + cnt + " on insert of course")
      }
    }*/
  }

  def loadCourseData(courseid: Int, db: Database): Future[CourseData] = {
    ???
    /*val courseRow = db.run(Courses.filter(_.courseid === courseid).result.head)
    val instructors = db.run(
      (for {
        (row, assoc) <- Users join UserCourseAssoc on (_.userid === _.userid)
        if assoc.courseid === courseid && assoc.role === Instructor
      } yield row).result
    )
    val quizzes = db.run(
      (for {
        (quiz, assoc) <- Quizzes join QuizCourseCloseAssoc on (_.quizid === _.quizid)
        if assoc.courseid === courseid
      } yield (quiz, assoc)).result
    )
    val totals = quizzes.flatMap(s => Future.sequence(s.map(q => numberOfQuestions(q._1.quizid, db))))
    val students = db.run(
      (for {
        (row, assoc) <- Users join UserCourseAssoc on (_.userid === _.userid)
        if assoc.courseid === courseid && assoc.role === Student
      } yield row).result
    )
    val studentData = students.flatMap(sur => Future.sequence(sur.map(sr => {
      quizzes.flatMap(sq => Future.sequence(sq.map(q => numberOfCorrectQuestions(q._1.quizid, sr.userid, db)))).map(correct =>
        StudentData(sr.userid, sr.username, correct.sum))
    })))
    for {
      cr <- courseRow
      is <- instructors
      t <- totals
      sd <- studentData
      q <- quizzes
    } yield {
      CourseData(cr, is, sd, q.map(t => CourseQuizData(t._1, t._2.closeTime)), t.sum)
    }*/
  }

  // Users
  def validLogin(user: UsersRow, db: Database): Future[Int] = {
    val matches = db.run(Users.filter(u => u.username === user.username && u.trinityid === user.trinityid).result)
    matches.map(us => if (us.isEmpty) -1 else us.head.userid)
  }

  def fetchUserByName(username: String, db: Database): Future[UsersRow] = {
    db.run {
      Users.filter(u => u.username === username).result.head
    }
  }

  def instructorCourseIds(userid: String, db: Database): Future[Seq[Option[Int]]] = {
    //db.run(UserCourseAssoc.filter(uca => uca.userid === userid && uca.role === Instructor).map(_.courseid).result)
    ???
  }

  def instructorCourseRows(userid: String, db: Database): Future[Seq[CoursesRow]] = {
    /*db.run {
      (for {
        (ucar, cr) <- UserCourseAssoc join Courses on (_.courseid === _.courseid)
        if ucar.userid === userid && ucar.role === Instructor
      } yield {
          cr
        }).result
    }*/
    ???
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
    for (mc <- mcCount; fc <- funcCount; lc <- lambdaCount; ec <- exprCount) yield mc + fc + lc + ec
  }

  def numberOfCorrectQuestions(quizid: Int, userid: String, db: Database): Future[Int] = {
    /*val mcCount = db.run {
      McAnswers.filter(a => a.quizid === quizid && a.userid === userid && a.correct).map(_.mcQuestionId).result
    }
    val codeCount = db.run {
      CodeAnswers.filter(a => a.quizid === quizid && a.userid === userid && a.correct)
        .map(a => a.questionId -> a.questionType).result
    }
    for {
      mc <- mcCount
      code <- codeCount
    } yield {
      mc.distinct.length + code.distinct.length
    }*/
    ???
  }

  def quizScore(quizid: Int, userid: Int, db: Database): Future[(Int, Int)] = {
    /*val total = numberOfQuestions(quizid, db)
    val correct = numberOfCorrectQuestions(quizid, userid, db);
    for (t <- total; c <- correct) yield (c, t)*/
    ???
  }

  def multipleChoiceData(quizid: Int, userid: Int, db: Database): Future[Seq[MultipleChoiceData]] = {
    val rows = db.run {
      (for {
        (mcq, mca) <- MultipleChoiceQuestions join MultipleChoiceAssoc on (_.mcQuestionId === _.mcQuestionId)
        if mca.quizid === quizid
      } yield mcq).result
    }
    val data = rows.flatMap(s => Future.sequence {
      for {
        row <- s
      } yield {
        val options = Seq(row.option1, row.option2) ++ Seq(row.option3, row.option4, row.option5, row.option6,
          row.option7, row.option8).filter(_.nonEmpty).map(_.get)
        val userAnswer = db.run((for {
          ans <- McAnswers
          if ans.userid === userid && ans.quizid === quizid && ans.mcQuestionId === row.mcQuestionId
        } yield ans.selection).result)
        userAnswer.map(ans => MultipleChoiceData(row.mcQuestionId, row.prompt, options, row.correctOption, if (ans.isEmpty) None else Some(ans.head)))
      }
    })
    data
  }

  def codeQuestionData(quizid: Int, userid: Int, db: Database): Future[Seq[CodeQuestionData]] = {
    val funcRows = db.run {
      (for {
        (fq, fa) <- FunctionQuestions join FunctionAssoc on (_.funcQuestionId === _.funcQuestionId)
        if fa.quizid === quizid
      } yield fq).result
    }
    val funcData = funcRows.flatMap(s => Future.sequence {
      for {
        row <- s
      } yield {
        val userAnswers = db.run((for {
          ans <- CodeAnswers
          if ans.userid === userid && ans.quizid === quizid && ans.questionId === row.funcQuestionId && ans.questionType === 1
        } yield ans).result)
        userAnswers.map(ans => CodeQuestionData(row.funcQuestionId, 1, row.prompt, if (ans.isEmpty) None else Some(ans.last.answer), ans.exists(_.correct)))
      }
    })
    val lambdaRows = db.run {
      (for {
        (lq, la) <- LambdaQuestions join LambdaAssoc on (_.lambdaQuestionId === _.lambdaQuestionId)
        if la.quizid === quizid
      } yield lq).result
    }
    val lambdaData = lambdaRows.flatMap(s => Future.sequence {
      for {
        row <- s
      } yield {
        val userAnswers = db.run((for {
          ans <- CodeAnswers
          if ans.userid === userid && ans.quizid === quizid && ans.questionId === row.lambdaQuestionId && ans.questionType === 2
        } yield ans).result)
        userAnswers.map(ans => CodeQuestionData(row.lambdaQuestionId, 2, row.prompt, if (ans.isEmpty) None else Some(ans.last.answer), ans.exists(_.correct)))
      }
    })
    val exprRows = db.run {
      (for {
        (eq, ea) <- ExpressionQuestions join ExpressionAssoc on (_.exprQuestionId === _.exprQuestionId)
        if ea.quizid === quizid
      } yield eq).result
    }
    val exprData = exprRows.flatMap(s => Future.sequence {
      for {
        row <- s
      } yield {
        val userAnswers = db.run((for {
          ans <- CodeAnswers
          if ans.userid === userid && ans.quizid === quizid && ans.questionId === row.exprQuestionId && ans.questionType === 3
        } yield ans).result)
        userAnswers.map(ans => CodeQuestionData(row.exprQuestionId, 3, row.prompt, if (ans.isEmpty) None else Some(ans.last.answer), ans.exists(_.correct)))
      }
    })
    for {
      fd <- funcData
      ld <- lambdaData
      ed <- exprData
    } yield fd ++ ld ++ ed
  }

  def quizData(quizid: Int, userid: Int, db: Database): Future[QuizData] = {
    val quizRow = db.run(Quizzes.filter(_.quizid === quizid).result.head)
    val mcQuestions = multipleChoiceData(quizid, userid, db)
    val codeQuestions = codeQuestionData(quizid, userid, db)
    for {
      qr <- quizRow
      mc <- mcQuestions
      cq <- codeQuestions
    } yield QuizData(quizid, userid, qr.name, qr.description, mc, cq)
  }

  def quizSpecs(quizid: Int, db: Database): Future[Seq[ProblemSpec]] = {
    val mcQuestions = db.run(MultipleChoiceAssoc.filter(_.quizid === quizid).result).flatMap(f => Future.sequence(f.map(mca => {
      db.run(MultipleChoiceQuestions.filter(_.mcQuestionId === mca.mcQuestionId).result)
    })))
    val mcSpecs = mcQuestions.flatMap(mcqSeq => Future.sequence(mcqSeq.flatten.map(mcq =>
      ProblemSpec(ProblemSpec.MultipleChoiceType, mcq.mcQuestionId, db))))
    val funcQuestions = db.run(FunctionAssoc.filter(_.quizid === quizid).result).flatMap(f => Future.sequence(f.map(fa => {
      db.run(FunctionQuestions.filter(_.funcQuestionId === fa.funcQuestionId).result)
    })))
    val funcSpecs = funcQuestions.flatMap(fqSeq => Future.sequence(fqSeq.flatten.map(fq =>
      ProblemSpec(ProblemSpec.FunctionType, fq.funcQuestionId, db))))
    val lambdaQuestions = db.run(LambdaAssoc.filter(_.quizid === quizid).result).flatMap(f => Future.sequence(f.map(la => {
      db.run(LambdaQuestions.filter(_.lambdaQuestionId === la.lambdaQuestionId).result)
    })))
    val lambdaSpecs = lambdaQuestions.flatMap(lqSeq => Future.sequence(lqSeq.flatten.map(lq =>
      ProblemSpec(ProblemSpec.LambdaType, lq.lambdaQuestionId, db))))
    val exprQuestions = db.run(ExpressionAssoc.filter(_.quizid === quizid).result).flatMap(f => Future.sequence(f.map(ea => {
      db.run(ExpressionQuestions.filter(_.exprQuestionId === ea.exprQuestionId).result)
    })))
    val exprSpecs = exprQuestions.flatMap(eqSeq => Future.sequence(eqSeq.flatten.map(eq =>
      ProblemSpec(ProblemSpec.ExpressionType, eq.exprQuestionId, db))))
    for {
      mcqs <- mcSpecs
      fqs <- funcSpecs
      lqs <- lambdaSpecs
      eqs <- exprSpecs
    } yield {
      mcqs ++ fqs ++ lqs ++ eqs
    }
  }
}