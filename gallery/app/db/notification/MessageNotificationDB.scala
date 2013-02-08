package db.notification

import play.api.db._
import play.api.Play.current
import anorm.SqlParser._
import anorm._
import anorm.Row
import utils._
import java.util.Date
import org.joda.time.DateTime
import models.notification.Message

object MessageNotificationDB {

  val _DB_TBL_MESSAGE_NOTIFICATION: String = Configuration.getStringValue(Configuration._TABLE_MESSAGE_NOTIFICATION_KEY)

  def insert(messageId: Long, notificationId: Long) : Long = {
    return DB.withConnection { implicit connection =>
      SQL(
        """
          INSERT INTO """ + _DB_TBL_MESSAGE_NOTIFICATION + """ (message, notification) VALUES (
            {messageId}, {notificationId}
          ) RETURNING id
        """
      ).on(
        'messageId -> messageId,
        'notificationId -> notificationId
      ).as(long("id").single)
    }
  }
  
  def findMessagesById(id: Long): Seq[Message] = {
    return DB.withConnection { implicit connection =>
      SQL(
          """
          SELECT * FROM """ + _DB_TBL_MESSAGE_NOTIFICATION + """
          JOIN """ + MessageDB._DB_TBL_MESSAGE + """
          ON (""" + _DB_TBL_MESSAGE_NOTIFICATION + """.message = """ + MessageDB._DB_TBL_MESSAGE + """.id)
          where """ + _DB_TBL_MESSAGE_NOTIFICATION + """.notification = {id}"""
      ).on('id-> id
      ).as(MessageDB.simple *)
    }
  }
  
  def delete(notificationId: Long) : Int = {
    return DB.withConnection { implicit connection =>
      SQL(
        """
          DELETE FROM """ + _DB_TBL_MESSAGE_NOTIFICATION + """ WHERE notification = {notificationId}
        """
      ).on(
        'notificationId -> notificationId
      ).executeUpdate()
    }
  }
}