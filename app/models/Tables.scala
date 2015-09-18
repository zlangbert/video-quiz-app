package models
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.driver.MySQLDriver
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.driver.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema = Array(Answer.schema, Course.schema, CourseQuiz.schema, PlayEvolutions.schema, Question.schema, Quiz.schema, QuizQuestion.schema, User.schema, UserCourse.schema, UserQuiz.schema).reduceLeft(_ ++ _)
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table Answer
   *  @param userId Database column user_id SqlType(VARCHAR), Length(24,true)
   *  @param quizId Database column quiz_id SqlType(INT)
   *  @param questionId Database column question_id SqlType(INT)
   *  @param isCorrect Database column is_correct SqlType(BIT) */
  case class AnswerRow(userId: String, quizId: Int, questionId: Int, isCorrect: Boolean)
  /** GetResult implicit for fetching AnswerRow objects using plain SQL queries */
  implicit def GetResultAnswerRow(implicit e0: GR[String], e1: GR[Int], e2: GR[Boolean]): GR[AnswerRow] = GR{
    prs => import prs._
    AnswerRow.tupled((<<[String], <<[Int], <<[Int], <<[Boolean]))
  }
  /** Table description of table answer. Objects of this class serve as prototypes for rows in queries. */
  class Answer(_tableTag: Tag) extends Table[AnswerRow](_tableTag, "answer") {
    def * = (userId, quizId, questionId, isCorrect) <> (AnswerRow.tupled, AnswerRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(userId), Rep.Some(quizId), Rep.Some(questionId), Rep.Some(isCorrect)).shaped.<>({r=>import r._; _1.map(_=> AnswerRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(VARCHAR), Length(24,true) */
    val userId: Rep[String] = column[String]("user_id", O.Length(24,varying=true))
    /** Database column quiz_id SqlType(INT) */
    val quizId: Rep[Int] = column[Int]("quiz_id")
    /** Database column question_id SqlType(INT) */
    val questionId: Rep[Int] = column[Int]("question_id")
    /** Database column is_correct SqlType(BIT) */
    val isCorrect: Rep[Boolean] = column[Boolean]("is_correct")

    /** Primary key of Answer (database name answer_PK) */
    val pk = primaryKey("answer_PK", (userId, quizId, questionId))

    /** Foreign key referencing Question (database name answer_ibfk_3) */
    lazy val questionFk = foreignKey("answer_ibfk_3", questionId, Question)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing Quiz (database name answer_ibfk_2) */
    lazy val quizFk = foreignKey("answer_ibfk_2", quizId, Quiz)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing User (database name answer_ibfk_1) */
    lazy val userFk = foreignKey("answer_ibfk_1", userId, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Answer */
  lazy val Answer = new TableQuery(tag => new Answer(tag))

  /** Entity class storing rows of table Course
   *  @param id Database column id SqlType(INT), AutoInc, PrimaryKey
   *  @param code Database column code SqlType(VARCHAR), Length(8,true)
   *  @param semester Database column semester SqlType(VARCHAR), Length(3,true)
   *  @param section Database column section SqlType(INT) */
  case class CourseRow(id: Int, code: String, semester: String, section: Int)
  /** GetResult implicit for fetching CourseRow objects using plain SQL queries */
  implicit def GetResultCourseRow(implicit e0: GR[Int], e1: GR[String]): GR[CourseRow] = GR{
    prs => import prs._
    CourseRow.tupled((<<[Int], <<[String], <<[String], <<[Int]))
  }
  /** Table description of table course. Objects of this class serve as prototypes for rows in queries. */
  class Course(_tableTag: Tag) extends Table[CourseRow](_tableTag, "course") {
    def * = (id, code, semester, section) <> (CourseRow.tupled, CourseRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(code), Rep.Some(semester), Rep.Some(section)).shaped.<>({r=>import r._; _1.map(_=> CourseRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column code SqlType(VARCHAR), Length(8,true) */
    val code: Rep[String] = column[String]("code", O.Length(8,varying=true))
    /** Database column semester SqlType(VARCHAR), Length(3,true) */
    val semester: Rep[String] = column[String]("semester", O.Length(3,varying=true))
    /** Database column section SqlType(INT) */
    val section: Rep[Int] = column[Int]("section")
  }
  /** Collection-like TableQuery object for table Course */
  lazy val Course = new TableQuery(tag => new Course(tag))

  /** Entity class storing rows of table CourseQuiz
   *  @param courseId Database column course_id SqlType(INT)
   *  @param quizId Database column quiz_id SqlType(INT)
   *  @param openTime Database column open_time SqlType(DATETIME)
   *  @param closeTime Database column close_time SqlType(DATETIME) */
  case class CourseQuizRow(courseId: Int, quizId: Int, openTime: java.sql.Timestamp, closeTime: java.sql.Timestamp)
  /** GetResult implicit for fetching CourseQuizRow objects using plain SQL queries */
  implicit def GetResultCourseQuizRow(implicit e0: GR[Int], e1: GR[java.sql.Timestamp]): GR[CourseQuizRow] = GR{
    prs => import prs._
    CourseQuizRow.tupled((<<[Int], <<[Int], <<[java.sql.Timestamp], <<[java.sql.Timestamp]))
  }
  /** Table description of table course_quiz. Objects of this class serve as prototypes for rows in queries. */
  class CourseQuiz(_tableTag: Tag) extends Table[CourseQuizRow](_tableTag, "course_quiz") {
    def * = (courseId, quizId, openTime, closeTime) <> (CourseQuizRow.tupled, CourseQuizRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(courseId), Rep.Some(quizId), Rep.Some(openTime), Rep.Some(closeTime)).shaped.<>({r=>import r._; _1.map(_=> CourseQuizRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column course_id SqlType(INT) */
    val courseId: Rep[Int] = column[Int]("course_id")
    /** Database column quiz_id SqlType(INT) */
    val quizId: Rep[Int] = column[Int]("quiz_id")
    /** Database column open_time SqlType(DATETIME) */
    val openTime: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("open_time")
    /** Database column close_time SqlType(DATETIME) */
    val closeTime: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("close_time")

    /** Primary key of CourseQuiz (database name course_quiz_PK) */
    val pk = primaryKey("course_quiz_PK", (courseId, quizId))

    /** Foreign key referencing Course (database name course_quiz_ibfk_1) */
    lazy val courseFk = foreignKey("course_quiz_ibfk_1", courseId, Course)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing Quiz (database name course_quiz_ibfk_2) */
    lazy val quizFk = foreignKey("course_quiz_ibfk_2", quizId, Quiz)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table CourseQuiz */
  lazy val CourseQuiz = new TableQuery(tag => new CourseQuiz(tag))

  /** Entity class storing rows of table PlayEvolutions
   *  @param id Database column id SqlType(INT), PrimaryKey
   *  @param hash Database column hash SqlType(VARCHAR), Length(255,true)
   *  @param appliedAt Database column applied_at SqlType(TIMESTAMP)
   *  @param applyScript Database column apply_script SqlType(MEDIUMTEXT), Length(16777215,true), Default(None)
   *  @param revertScript Database column revert_script SqlType(MEDIUMTEXT), Length(16777215,true), Default(None)
   *  @param state Database column state SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param lastProblem Database column last_problem SqlType(MEDIUMTEXT), Length(16777215,true), Default(None) */
  case class PlayEvolutionsRow(id: Int, hash: String, appliedAt: java.sql.Timestamp, applyScript: Option[String] = None, revertScript: Option[String] = None, state: Option[String] = None, lastProblem: Option[String] = None)
  /** GetResult implicit for fetching PlayEvolutionsRow objects using plain SQL queries */
  implicit def GetResultPlayEvolutionsRow(implicit e0: GR[Int], e1: GR[String], e2: GR[java.sql.Timestamp], e3: GR[Option[String]]): GR[PlayEvolutionsRow] = GR{
    prs => import prs._
    PlayEvolutionsRow.tupled((<<[Int], <<[String], <<[java.sql.Timestamp], <<?[String], <<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table play_evolutions. Objects of this class serve as prototypes for rows in queries. */
  class PlayEvolutions(_tableTag: Tag) extends Table[PlayEvolutionsRow](_tableTag, "play_evolutions") {
    def * = (id, hash, appliedAt, applyScript, revertScript, state, lastProblem) <> (PlayEvolutionsRow.tupled, PlayEvolutionsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(hash), Rep.Some(appliedAt), applyScript, revertScript, state, lastProblem).shaped.<>({r=>import r._; _1.map(_=> PlayEvolutionsRow.tupled((_1.get, _2.get, _3.get, _4, _5, _6, _7)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
    /** Database column hash SqlType(VARCHAR), Length(255,true) */
    val hash: Rep[String] = column[String]("hash", O.Length(255,varying=true))
    /** Database column applied_at SqlType(TIMESTAMP) */
    val appliedAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("applied_at")
    /** Database column apply_script SqlType(MEDIUMTEXT), Length(16777215,true), Default(None) */
    val applyScript: Rep[Option[String]] = column[Option[String]]("apply_script", O.Length(16777215,varying=true), O.Default(None))
    /** Database column revert_script SqlType(MEDIUMTEXT), Length(16777215,true), Default(None) */
    val revertScript: Rep[Option[String]] = column[Option[String]]("revert_script", O.Length(16777215,varying=true), O.Default(None))
    /** Database column state SqlType(VARCHAR), Length(255,true), Default(None) */
    val state: Rep[Option[String]] = column[Option[String]]("state", O.Length(255,varying=true), O.Default(None))
    /** Database column last_problem SqlType(MEDIUMTEXT), Length(16777215,true), Default(None) */
    val lastProblem: Rep[Option[String]] = column[Option[String]]("last_problem", O.Length(16777215,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table PlayEvolutions */
  lazy val PlayEvolutions = new TableQuery(tag => new PlayEvolutions(tag))

  /** Entity class storing rows of table Question
   *  @param id Database column id SqlType(INT), AutoInc, PrimaryKey
   *  @param `type` Database column type SqlType(INT)
   *  @param prompt Database column prompt SqlType(TEXT) */
  case class QuestionRow(id: Int, `type`: Int, prompt: String)
  /** GetResult implicit for fetching QuestionRow objects using plain SQL queries */
  implicit def GetResultQuestionRow(implicit e0: GR[Int], e1: GR[String]): GR[QuestionRow] = GR{
    prs => import prs._
    QuestionRow.tupled((<<[Int], <<[Int], <<[String]))
  }
  /** Table description of table question. Objects of this class serve as prototypes for rows in queries.
   *  NOTE: The following names collided with Scala keywords and were escaped: type */
  class Question(_tableTag: Tag) extends Table[QuestionRow](_tableTag, "question") {
    def * = (id, `type`, prompt) <> (QuestionRow.tupled, QuestionRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(`type`), Rep.Some(prompt)).shaped.<>({r=>import r._; _1.map(_=> QuestionRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column type SqlType(INT)
     *  NOTE: The name was escaped because it collided with a Scala keyword. */
    val `type`: Rep[Int] = column[Int]("type")
    /** Database column prompt SqlType(TEXT) */
    val prompt: Rep[String] = column[String]("prompt")
  }
  /** Collection-like TableQuery object for table Question */
  lazy val Question = new TableQuery(tag => new Question(tag))

  /** Entity class storing rows of table Quiz
   *  @param id Database column id SqlType(INT), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(VARCHAR), Length(128,true)
   *  @param description Database column description SqlType(TEXT) */
  case class QuizRow(id: Int, name: String, description: String)
  /** GetResult implicit for fetching QuizRow objects using plain SQL queries */
  implicit def GetResultQuizRow(implicit e0: GR[Int], e1: GR[String]): GR[QuizRow] = GR{
    prs => import prs._
    QuizRow.tupled((<<[Int], <<[String], <<[String]))
  }
  /** Table description of table quiz. Objects of this class serve as prototypes for rows in queries. */
  class Quiz(_tableTag: Tag) extends Table[QuizRow](_tableTag, "quiz") {
    def * = (id, name, description) <> (QuizRow.tupled, QuizRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(name), Rep.Some(description)).shaped.<>({r=>import r._; _1.map(_=> QuizRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(VARCHAR), Length(128,true) */
    val name: Rep[String] = column[String]("name", O.Length(128,varying=true))
    /** Database column description SqlType(TEXT) */
    val description: Rep[String] = column[String]("description")
  }
  /** Collection-like TableQuery object for table Quiz */
  lazy val Quiz = new TableQuery(tag => new Quiz(tag))

  /** Entity class storing rows of table QuizQuestion
   *  @param quizId Database column quiz_id SqlType(INT)
   *  @param questionId Database column question_id SqlType(INT) */
  case class QuizQuestionRow(quizId: Int, questionId: Int)
  /** GetResult implicit for fetching QuizQuestionRow objects using plain SQL queries */
  implicit def GetResultQuizQuestionRow(implicit e0: GR[Int]): GR[QuizQuestionRow] = GR{
    prs => import prs._
    QuizQuestionRow.tupled((<<[Int], <<[Int]))
  }
  /** Table description of table quiz_question. Objects of this class serve as prototypes for rows in queries. */
  class QuizQuestion(_tableTag: Tag) extends Table[QuizQuestionRow](_tableTag, "quiz_question") {
    def * = (quizId, questionId) <> (QuizQuestionRow.tupled, QuizQuestionRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(quizId), Rep.Some(questionId)).shaped.<>({r=>import r._; _1.map(_=> QuizQuestionRow.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column quiz_id SqlType(INT) */
    val quizId: Rep[Int] = column[Int]("quiz_id")
    /** Database column question_id SqlType(INT) */
    val questionId: Rep[Int] = column[Int]("question_id")

    /** Primary key of QuizQuestion (database name quiz_question_PK) */
    val pk = primaryKey("quiz_question_PK", (quizId, questionId))

    /** Foreign key referencing Question (database name quiz_question_ibfk_2) */
    lazy val questionFk = foreignKey("quiz_question_ibfk_2", questionId, Question)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing Quiz (database name quiz_question_ibfk_1) */
    lazy val quizFk = foreignKey("quiz_question_ibfk_1", quizId, Quiz)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table QuizQuestion */
  lazy val QuizQuestion = new TableQuery(tag => new QuizQuestion(tag))

  /** Entity class storing rows of table User
   *  @param id Database column id SqlType(VARCHAR), PrimaryKey, Length(24,true)
   *  @param provider Database column provider SqlType(VARCHAR), Length(24,true)
   *  @param email Database column email SqlType(VARCHAR), Length(64,true)
   *  @param username Database column username SqlType(VARCHAR), Length(64,true)
   *  @param firstName Database column first_name SqlType(VARCHAR), Length(64,true), Default(None)
   *  @param lastName Database column last_name SqlType(VARCHAR), Length(64,true), Default(None)
   *  @param avatarUrl Database column avatar_url SqlType(VARCHAR), Length(256,true), Default(None)
   *  @param isInstructor Database column is_instructor SqlType(BIT), Default(Some(false)) */
  case class UserRow(id: String, provider: String, email: String, username: String, firstName: Option[String] = None, lastName: Option[String] = None, avatarUrl: Option[String] = None, isInstructor: Option[Boolean] = Some(false))
  /** GetResult implicit for fetching UserRow objects using plain SQL queries */
  implicit def GetResultUserRow(implicit e0: GR[String], e1: GR[Option[String]], e2: GR[Option[Boolean]]): GR[UserRow] = GR{
    prs => import prs._
    UserRow.tupled((<<[String], <<[String], <<[String], <<[String], <<?[String], <<?[String], <<?[String], <<?[Boolean]))
  }
  /** Table description of table user. Objects of this class serve as prototypes for rows in queries. */
  class User(_tableTag: Tag) extends Table[UserRow](_tableTag, "user") {
    def * = (id, provider, email, username, firstName, lastName, avatarUrl, isInstructor) <> (UserRow.tupled, UserRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(provider), Rep.Some(email), Rep.Some(username), firstName, lastName, avatarUrl, isInstructor).shaped.<>({r=>import r._; _1.map(_=> UserRow.tupled((_1.get, _2.get, _3.get, _4.get, _5, _6, _7, _8)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(VARCHAR), PrimaryKey, Length(24,true) */
    val id: Rep[String] = column[String]("id", O.PrimaryKey, O.Length(24,varying=true))
    /** Database column provider SqlType(VARCHAR), Length(24,true) */
    val provider: Rep[String] = column[String]("provider", O.Length(24,varying=true))
    /** Database column email SqlType(VARCHAR), Length(64,true) */
    val email: Rep[String] = column[String]("email", O.Length(64,varying=true))
    /** Database column username SqlType(VARCHAR), Length(64,true) */
    val username: Rep[String] = column[String]("username", O.Length(64,varying=true))
    /** Database column first_name SqlType(VARCHAR), Length(64,true), Default(None) */
    val firstName: Rep[Option[String]] = column[Option[String]]("first_name", O.Length(64,varying=true), O.Default(None))
    /** Database column last_name SqlType(VARCHAR), Length(64,true), Default(None) */
    val lastName: Rep[Option[String]] = column[Option[String]]("last_name", O.Length(64,varying=true), O.Default(None))
    /** Database column avatar_url SqlType(VARCHAR), Length(256,true), Default(None) */
    val avatarUrl: Rep[Option[String]] = column[Option[String]]("avatar_url", O.Length(256,varying=true), O.Default(None))
    /** Database column is_instructor SqlType(BIT), Default(Some(false)) */
    val isInstructor: Rep[Option[Boolean]] = column[Option[Boolean]]("is_instructor", O.Default(Some(false)))
  }
  /** Collection-like TableQuery object for table User */
  lazy val User = new TableQuery(tag => new User(tag))

  /** Entity class storing rows of table UserCourse
   *  @param userId Database column user_id SqlType(VARCHAR), Length(24,true)
   *  @param courseId Database column course_id SqlType(INT) */
  case class UserCourseRow(userId: String, courseId: Int)
  /** GetResult implicit for fetching UserCourseRow objects using plain SQL queries */
  implicit def GetResultUserCourseRow(implicit e0: GR[String], e1: GR[Int]): GR[UserCourseRow] = GR{
    prs => import prs._
    UserCourseRow.tupled((<<[String], <<[Int]))
  }
  /** Table description of table user_course. Objects of this class serve as prototypes for rows in queries. */
  class UserCourse(_tableTag: Tag) extends Table[UserCourseRow](_tableTag, "user_course") {
    def * = (userId, courseId) <> (UserCourseRow.tupled, UserCourseRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(userId), Rep.Some(courseId)).shaped.<>({r=>import r._; _1.map(_=> UserCourseRow.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(VARCHAR), Length(24,true) */
    val userId: Rep[String] = column[String]("user_id", O.Length(24,varying=true))
    /** Database column course_id SqlType(INT) */
    val courseId: Rep[Int] = column[Int]("course_id")

    /** Primary key of UserCourse (database name user_course_PK) */
    val pk = primaryKey("user_course_PK", (userId, courseId))

    /** Foreign key referencing Course (database name user_course_ibfk_2) */
    lazy val courseFk = foreignKey("user_course_ibfk_2", courseId, Course)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing User (database name user_course_ibfk_1) */
    lazy val userFk = foreignKey("user_course_ibfk_1", userId, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table UserCourse */
  lazy val UserCourse = new TableQuery(tag => new UserCourse(tag))

  /** Entity class storing rows of table UserQuiz
   *  @param userId Database column user_id SqlType(VARCHAR), Length(24,true)
   *  @param quizId Database column quiz_id SqlType(INT) */
  case class UserQuizRow(userId: String, quizId: Int)
  /** GetResult implicit for fetching UserQuizRow objects using plain SQL queries */
  implicit def GetResultUserQuizRow(implicit e0: GR[String], e1: GR[Int]): GR[UserQuizRow] = GR{
    prs => import prs._
    UserQuizRow.tupled((<<[String], <<[Int]))
  }
  /** Table description of table user_quiz. Objects of this class serve as prototypes for rows in queries. */
  class UserQuiz(_tableTag: Tag) extends Table[UserQuizRow](_tableTag, "user_quiz") {
    def * = (userId, quizId) <> (UserQuizRow.tupled, UserQuizRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(userId), Rep.Some(quizId)).shaped.<>({r=>import r._; _1.map(_=> UserQuizRow.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(VARCHAR), Length(24,true) */
    val userId: Rep[String] = column[String]("user_id", O.Length(24,varying=true))
    /** Database column quiz_id SqlType(INT) */
    val quizId: Rep[Int] = column[Int]("quiz_id")

    /** Primary key of UserQuiz (database name user_quiz_PK) */
    val pk = primaryKey("user_quiz_PK", (userId, quizId))

    /** Foreign key referencing Quiz (database name user_quiz_ibfk_2) */
    lazy val quizFk = foreignKey("user_quiz_ibfk_2", quizId, Quiz)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing User (database name user_quiz_ibfk_1) */
    lazy val userFk = foreignKey("user_quiz_ibfk_1", userId, User)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table UserQuiz */
  lazy val UserQuiz = new TableQuery(tag => new UserQuiz(tag))
}
