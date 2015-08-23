package util

/**
 * @author mlewis
 */
object CodeGen extends App {
  slick.codegen.SourceCodeGenerator.main(
    Array("slick.driver.MySQLDriver", "com.mysql.jdbc.Driver", 
        "jdbc:mysql://localhost/video_quizzes?user=mlewis&password=password", "/home/mlewis/workspaceWeb/video-quiz-app/app/", "models", "mlewis", "password")
  )
}