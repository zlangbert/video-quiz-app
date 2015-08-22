package models

import slick.driver.MySQLDriver.api._
import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.duration.Duration
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class User(userid:Int, username:String, trinityid:String)

class Users(tag:Tag) extends Table[User](tag,"users") {
  def userid = column[Int]("userid", O.PrimaryKey)
  def username = column[String]("username")
  def trinityid = column[String]("trinityid")
  
  def * = (userid, username, trinityid) <> (User.tupled, User.unapply)
}

object Users {
  val users = TableQuery[Users]
  val sessionMap = new ConcurrentHashMap[String,User]();
  
  def main(args:Array[String]):Unit = {
    println("Get database")
//    val db = Database.forConfig("mysql1")
    val db = Database.forURL("jdbc:mysql://localhost/video_quizzes", user="mlewis", password="password", driver="com.mysql.jdbc.Driver")
    Await.result(db.run(
      users += User(0,"mlewis","0123456")
    ), Duration.Inf)
    db.close()
  }
  
  def validLogin(user:User, db:Database):Future[Boolean] = {
    val matches = db.run(users.filter(u => u.username === user.username && u.trinityid === user.trinityid).result)
    matches.map(!_.isEmpty)
  }
  
  def lookupUser(sessionKey:String):Option[User] = {
    if(sessionMap.containsKey(sessionKey)) {
      Some(sessionMap.get(sessionKey))
    } else {
      None
    }
  }
}