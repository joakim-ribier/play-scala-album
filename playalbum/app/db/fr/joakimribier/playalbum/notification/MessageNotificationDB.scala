package db.fr.joakimribier.playalbum.notification

import anorm.SQL
import anorm.SqlParser.long
import anorm.sqlToSimple
import anorm.toParameterValue
import models.fr.joakimribier.playalbum.Message
import play.api.Play.current
import play.api.db.DB
import utils.fr.joakimribier.playalbum.ConfigurationUtils

object MessageNotificationDB {

  val _DB_TBL_MESSAGE_NOTIFICATION: String = ConfigurationUtils.getStringValue(ConfigurationUtils._TABLE_MESSAGE_NOTIFICATION_KEY)

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