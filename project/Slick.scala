import sbt.Keys._
import sbt._

object Slick {

  lazy val slick = TaskKey[Seq[File]]("gen-tables")

  lazy val slickCodeGenTask = (sourceManaged, dependencyClasspath in Compile, runner in Compile, streams).map {
    (dir, cp, r, s) =>
      val outputDir = "app/"
      val url = "jdbc:mysql://localhost:3307/video_quizzes"
      val user = "quiz"
      val password = "quiz"
      val jdbcDriver = "com.mysql.jdbc.Driver"
      val slickDriver = "slick.driver.MySQLDriver"
      val pkg = "models"
      toError(r.run("slick.codegen.SourceCodeGenerator", cp.files,
        Array(slickDriver, jdbcDriver, url, outputDir, pkg, user, password), s.log))
      val fname = outputDir + "Tables.scala"
      Seq(file(fname))
  }
}