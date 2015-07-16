package controllers

import play.api._
import play.api.mvc._


/**
 * @author mlewis
 */
class RoutingDemo extends Controller {
  def todo = TODO

  def singleIntArgument(arg: Int) = Action {
    Ok("arg = " + arg)
  }

  def twoArguments(i: Int, s: String) = Action {
    Ok(s"i = $i, s = $s")
  }

  def singleStringArgument(arg: String) = Action {
    Ok("arg = " + arg)
  }

}