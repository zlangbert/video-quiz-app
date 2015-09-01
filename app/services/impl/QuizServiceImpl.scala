package services.impl

import javax.inject.Inject

import models.Tables._
import play.api.db.slick.DatabaseConfigProvider
import services.QuizService
import slick.driver.JdbcProfile

import scala.concurrent.Future

class QuizServiceImpl @Inject()(dbConfigProvider: DatabaseConfigProvider) extends QuizService {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db

  def listForUser(): Future[QuizzesRow] = {
    ???
  }
}