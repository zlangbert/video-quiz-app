package models

import slick.driver.H2Driver.api._
import java.util.concurrent.ConcurrentHashMap


case class User(username:String,password:String)

class Users(tag:Tag) extends Table[User](tag,"USERS") {
  def username = column[String]("USERNAME", O.PrimaryKey)
  def password = column[String]("PASSWORD")
  
  def * = (username, password) <> (User.tupled, User.unapply)
}

object Users {
  val users = TableQuery[Users]
  val sessionMap = new ConcurrentHashMap[String,User]();
  
  def main(args:Array[String]):Unit = {
    users += User("mlewis","password")
  }
  
  def checkUser(uname:String,passwd:String):Option[User] = {
//    val result = for(u <- users; if u.username === uname && u.password === passwd) yield u.first
    None
  }
  
  def lookupUser(sessionKey:String):Option[User] = {
    if(sessionMap.containsKey(sessionKey)) {
      Some(sessionMap.get(sessionKey))
    } else {
      None
    }
  }
}