package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import models._

/**
 * @author mlewis
 */
class JsonDemo extends Controller {
  def simpleObject = Action {
    val map = Map("city" -> "San Antonio", "event" -> "SA WebDev Meetup", "month" -> "July", "day" -> "16", "year" -> "2015")
    Ok(Json.toJson(map))
  }
  
  def students(course:String,section:String,semester:String) = Action {
    Ok(Json.toJson(ClassInfo.findClass(course, section, semester).map(_.students)))
  }
}