package models

/**
 * @author mlewis
 */
case class QuizData(
    quizid:Int,
    userid:Int,
    name:String,
    description:String,
    multipleChoice:Seq[MultipleChoiceData],
    codeQuestions:Seq[CodeQuestionData])

case class MultipleChoiceData(
    mcid:Int,
    prompt:String,
    options:Seq[String],
    correctAnswer:Int,
    answer:Option[Int])

case class CodeQuestionData(
    questionid:Int,
    questionType:Int,
    prompt:String,
    lastCode:Option[String],
    correct:Boolean) {
  def typeString = questionType match {
    case 1 => "function"
    case 2 => "lambda"
    case 3 => "expression"
  }
}