package models

import slick.driver.H2Driver.api._

/**
 * @author mlewis
 */
object DBAccess {
  val db = Database.forConfig("h2mem1")
  
}