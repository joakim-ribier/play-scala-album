package models.fr.joakimribier.playalbum

import org.joda.time.DateTime

import anorm._
import db.fr.joakimribier.playalbum.notification.MessageDB
import db.fr.joakimribier.playalbum.notification.MessageNotificationDB
import db.fr.joakimribier.playalbum.notification.NotificationDB
import db.fr.joakimribier.playalbum.notification.NotificationUserDB
import play.api.Logger
import play.api.i18n.Lang
import play.api.i18n.Messages

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
  
  def getActiveOrNotDefined(username: String) : Option[NotificationMessage] = {
    val notifications = NotificationDB.findAllBetween(DateTime.now())
    for (notification <- notifications) {
      val closed = NotificationUserDB.findClosedBy(username, notification.id.get)
			if (!(closed.isDefined && closed.get == true)) {
			  val messages = MessageNotificationDB.findMessagesById(notification.id.get)
				return Option.apply(new NotificationMessage(notification, messages))
			}
    }
    return Option.empty
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
      case e: Throwable => {
        Logger.error(e.getMessage(), e)
      	throw new IllegalArgumentException(Messages("app.global.error.invalid.field.form")(Lang("fr")))
      }
    }
  }
  
  def removeMessage(messageId: Long) = MessageDB.delete(messageId)
  
  def remove(notificationId: Long) : Int = {
    return NotificationDB.delete(notificationId)
  }
  
  def setClosed(username: String, notificationId: Long) : Boolean = {
    val user = User.findUser(Option.apply(username))
    if (user.isDefined) {
      val id = NotificationUserDB.insert(user.get.id.get, notificationId, true)
      return id.isInstanceOf[Long]
    }
    return false
  }
}