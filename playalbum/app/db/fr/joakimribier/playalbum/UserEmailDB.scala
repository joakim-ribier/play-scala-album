package db.fr.joakimribier.playalbum
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
object UserEmailDB {

  private val _DB_TBL_USER_EMAIL: String = play.Configuration.root().getString(ConfigurationUtils._TABLE_EMAIL_KEY)
  private val _DB_TBL_USER: String = play.Configuration.root().getString(ConfigurationUtils._TABLE_USER_KEY)
  
  def findByLogin(login: String): Option[String] = {
    return DB.withConnection { implicit connection =>
      SQL("""
          select * from """ + _DB_TBL_USER_EMAIL + """
          join """ + _DB_TBL_USER + """
          on (user_id=""" + _DB_TBL_USER + """.id)
          where login = {login}
          """)
      .on(
        'login-> login
        ).as(str(_DB_TBL_USER_EMAIL + ".email").singleOpt)
    }
  }
  
  def insert(userId: Long, email: String) : Long = {
    return DB.withConnection { implicit connection =>
      SQL(
        """
          insert into """ + _DB_TBL_USER_EMAIL + """ (user_id, email) values (
            {userId}, {email}
          ) RETURNING id
        """
      ).on(
        'userId -> userId,
        'email -> email
      ).as(long("id").single)
    }
  }
  
  def findAll() : Seq[String] = {
    return DB.withConnection { implicit connection =>
      val sql = SQL(
        """
          SELECT email FROM """ + _DB_TBL_USER_EMAIL + """
        """
      )
      sql().map(row => row[String]("email")).toList
    }
  }
}