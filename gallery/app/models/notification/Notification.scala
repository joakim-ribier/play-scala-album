package models
import anorm.Pk
import anorm.NotAssigned
import org.joda.time.DateTime
import play.api.Logger
import play.api.i18n.Messages
import play.api.i18n.Lang

case class Message(id: Pk[Long] = NotAssigned, message: String, created: DateTime)
case class Notification(id: Pk[Long] = NotAssigned, startDate: DateTime, endDate: DateTime)
case class NotificationMessage(notification: Notification, messages: Seq[Message])

object Notification {

  def createMessage(message: String) : Long = {
   if (message == null || message.isEmpty()) {
  	 throw new IllegalArgumentException("message argument cannot be null")
   }
   return MessageDB.insert(message, DateTime.now())
  }
  
  def listMessages() = MessageDB.findAll()
  
  def list() : Seq[NotificationMessage] = {
    val notifications = NotificationDB.findAll()
    var notificationMessages = Seq[NotificationMessage]()
    for (notification <- notifications) {
    	val messages = MessageNotificationDB.findMessagesById(notification.id.get)
    	notificationMessages +:= new NotificationMessage(notification, messages)
    }
    return notificationMessages
  }
  
  def add(startDate: String, endDate: String, messageIds: Seq[String]) : Boolean = {
    if (messageIds.size < 1) {
    	throw new IllegalArgumentException(Messages("app.global.error.invalid.field.form")(Lang("fr")))
    }
    
    try {
    	val startDateTime = new DateTime(startDate)
    	val endDateTime = new DateTime(endDate)
    	val notificationDbId = NotificationDB.insert(startDateTime, endDateTime)
    	if (notificationDbId.isInstanceOf[Long]) {
    		for (messageId <- messageIds) {
    		  MessageNotificationDB.insert(messageId.toLong, notificationDbId)
    		}
    		return true
    	}
    	
    	return false
    } catch {
      case e => {
        Logger.error(e.getMessage(), e)
      	throw new IllegalArgumentException(Messages("app.global.error.invalid.field.form")(Lang("fr")))
      }
    }
  }
  
  def removeMessage(messageId: Long) = MessageDB.delete(messageId)
  
  def remove(notificationId: Long) : Int = {
    return NotificationDB.delete(notificationId)
  }
}