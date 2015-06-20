package models

object ProblemSetState extends Enumeration {
  type State = Value
  val Unattempted, Submitted, Correct = Value
}