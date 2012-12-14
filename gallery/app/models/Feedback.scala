package models

object FeedbackClass extends Enumeration {
  type FeedbackClass = Value
  val ok, ko = Value
}
import FeedbackClass._

case class Feedback(message: String, feedbackClass: FeedbackClass)