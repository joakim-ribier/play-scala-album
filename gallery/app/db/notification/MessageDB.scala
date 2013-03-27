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

object MessageDB {

  val _DB_TBL_MESSAGE: String = ConfigurationUtils.getStringValue(ConfigurationUtils._TABLE_MESSAGE_KEY)
  val simple = {
    get[Pk[Long]](_DB_TBL_MESSAGE + ".id") ~
    get[String](_DB_TBL_MESSAGE + ".html")~
    get[Date](_DB_TBL_MESSAGE + ".created") map {
      case id~message~created => Message(id, message, new DateTime(created))
    }
  }

  def insert(message: String, created: DateTime) : Long = {
    return DB.withConnection { implicit connection =>
      SQL(
        """
          INSERT INTO """ + _DB_TBL_MESSAGE + """ (html, created) VALUES (
            {html}, {created}
          ) RETURNING id
        """
      ).on(
        'html -> message,
        'created -> created.toDate()
      ).as(long("id").single)
    }
  }
  
  def findAll() : Seq[Message] = {
    return DB.withConnection { implicit connection =>
      val sql = SQL(
        """
          SELECT * FROM """ + _DB_TBL_MESSAGE + """ ORDER BY created DESC
        """
      )
      sql.as(MessageDB.simple *)
    }
  }
  
  def delete(messageId: Long) : Int = {
    return DB.withConnection { implicit connection =>
      SQL(
        """
          DELETE FROM """ + _DB_TBL_MESSAGE + """ WHERE id = {messageId}
        """
      ).on(
        'messageId -> messageId
      ).executeUpdate()
    }
  }
  
}