package models

import play.api.db._
import play.api.Play.current
import anorm.SqlParser._
import anorm._
import anorm.Row
import utils._
import java.util.Date
import org.joda.time.DateTime

object NotificationUserDB {

  val _DB_TBL_NOTIFICATION_USER: String = Configuration.getStringValue(Configuration._TABLE_NOTIFICATION_USER_KEY)

  def findClosedBy(username: String, notificationId: Long): Option[Boolean] = {
    return DB.withConnection { implicit connection =>
      SQL(
          """
          SELECT * FROM """ + _DB_TBL_NOTIFICATION_USER + """
          JOIN """ + UserDB._DB_TBL_USER + """
          ON (""" + _DB_TBL_NOTIFICATION_USER + """.user_id = """ + UserDB._DB_TBL_USER + """.id)
          where """ + _DB_TBL_NOTIFICATION_USER + """.notification = {notificationId}
          and """ + UserDB._DB_TBL_USER + """.login = {login}
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