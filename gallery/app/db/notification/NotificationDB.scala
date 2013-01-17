package models

import play.api.db._
import play.api.Play.current
import anorm.SqlParser._
import anorm._
import anorm.Row
import utils._
import java.util.Date
import org.joda.time.DateTime

object NotificationDB {

  private val _DB_TBL_NOTIFICATION: String = Configuration.getStringValue(Configuration._TABLE_NOTIFICATION_KEY)
  private val simple = {
    get[Pk[Long]](_DB_TBL_NOTIFICATION + ".id") ~
    get[Date](_DB_TBL_NOTIFICATION + ".start_datetime_display")~
    get[Date](_DB_TBL_NOTIFICATION + ".end_datetime_display") map {
      case id~startDate~endDate => Notification(id, new DateTime(startDate), new DateTime(endDate))
    }
  }
  
  def insert(startDate: DateTime, endDate: DateTime) : Long = {
    return DB.withConnection { implicit connection =>
      SQL(
        """
          INSERT INTO """ + _DB_TBL_NOTIFICATION + """ (start_datetime_display, end_datetime_display) VALUES (
            {startDate}, {endDate}
          ) RETURNING id
        """
      ).on(
        'startDate -> startDate.toDate(),
        'endDate -> endDate.toDate()
      ).as(long("id").single)
    }
  }
  
  def findAll() : Seq[Notification] = {
    return DB.withConnection { implicit connection =>
      val sql = SQL(
        """
          SELECT * FROM """ + _DB_TBL_NOTIFICATION + """ ORDER BY start_datetime_display asc
        """
      )
      sql.as(NotificationDB.simple *)
    }
  }
  
  def delete(notificationId: Long) : Int = {
    return DB.withTransaction { implicit connection =>
      SQL("DELETE FROM " + MessageNotificationDB._DB_TBL_MESSAGE_NOTIFICATION + " WHERE notification = {notificationId}"
    	    ).on('notificationId -> notificationId).executeUpdate()
      
    	SQL("DELETE FROM " + _DB_TBL_NOTIFICATION + " WHERE id = {notificationId}"
          ).on('notificationId -> notificationId).executeUpdate()
    }
  }
}