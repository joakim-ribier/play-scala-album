package db.fr.joakimribier.playalbum.notification

import java.util.Date

import org.joda.time.DateTime

import anorm._
import anorm.SqlParser._
import models.fr.joakimribier.playalbum.Message
import play.api.Play.current
import play.api.db.DB
import utils.fr.joakimribier.playalbum.ConfigurationUtils

/**
 * 
 * Copyright 2013 Joakim Ribier
 * 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
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