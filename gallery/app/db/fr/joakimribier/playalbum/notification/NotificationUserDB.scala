package db.fr.joakimribier.playalbum.notification

import play.api.db._
import play.api.Play.current
import anorm.SqlParser._
import anorm._
import utils._
import utils.fr.joakimribier.playalbum.ConfigurationUtils

object NotificationUserDB {

  private val _DB_TBL_USER: String = ConfigurationUtils.getStringValue(ConfigurationUtils._TABLE_USER_KEY)
  val _DB_TBL_NOTIFICATION_USER: String = ConfigurationUtils.getStringValue(ConfigurationUtils._TABLE_NOTIFICATION_USER_KEY)

  def findClosedBy(username: String, notificationId: Long): Option[Boolean] = {
    return DB.withConnection { implicit connection =>
      SQL(
          """
          SELECT * FROM """ + _DB_TBL_NOTIFICATION_USER + """
          JOIN """ + _DB_TBL_USER + """
          ON (""" + _DB_TBL_NOTIFICATION_USER + """.user_id = """ + _DB_TBL_USER + """.id)
          where """ + _DB_TBL_NOTIFICATION_USER + """.notification = {notificationId}
          and """ + _DB_TBL_USER + """.login = {login}
          """
      ).on(
          'notificationId -> notificationId,
          'login -> username
      ).as(bool("closed").singleOpt)
    }
  }
  
  def insert(userId: Long, notificationId: Long, isClosed: Boolean) : Long = {
    return DB.withConnection { implicit connection =>
      SQL(
        """
          INSERT INTO """ + _DB_TBL_NOTIFICATION_USER + """ (user_id, notification, closed) VALUES (
            {userId}, {notificationId}, {isClosed}
          ) RETURNING id
        """
      ).on(
        'userId -> userId,
        'notificationId -> notificationId,
        'isClosed -> isClosed 
      ).as(long("id").single)
    }
  }
}