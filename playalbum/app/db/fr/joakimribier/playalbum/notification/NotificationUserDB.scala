package db.fr.joakimribier.playalbum.notification

import play.api.db._
import play.api.Play.current
import anorm.SqlParser._
import anorm._
import utils._
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