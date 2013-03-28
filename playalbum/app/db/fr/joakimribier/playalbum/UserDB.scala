package db.fr.joakimribier.playalbum

import java.util.Date

import org.joda.time.DateTime

import anorm._
import anorm.SqlParser._
import models.fr.joakimribier.playalbum.User
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
object UserDB {

  private val _DB_TBL_USER: String = ConfigurationUtils.getStringValue(ConfigurationUtils._TABLE_USER_KEY)
	private val _DB_TBL_USER_EMAIL: String = ConfigurationUtils.getStringValue(ConfigurationUtils._TABLE_EMAIL_KEY)
  
  private val simple = {
    get[Pk[Long]](_DB_TBL_USER + ".id") ~
    get[String](_DB_TBL_USER + ".login") ~
    get[String](_DB_TBL_USER + ".password") ~
    get[Date](_DB_TBL_USER + ".created") map {
      case id~login~password~created => User(id, login, password, new DateTime(created), Option.empty)
    }
  }
  
  def findByLoginAndPassword(login: String, password: String): Option[User] = {
    return DB.withConnection { implicit connection =>
      SQL("select * from " + _DB_TBL_USER + " where login = {login} and password = {password}").on(
          'login -> login,
          'password -> password).as(UserDB.simple.singleOpt)
    }
  }
  
  def findById(id: Long): Option[User] = {
    return DB.withConnection { implicit connection =>
      SQL("select * from " + _DB_TBL_USER + " where id = {id}").on(
          'id-> id).as(UserDB.simple.singleOpt)
    }
  }
  
  def findByLogin(login: String): Option[User] = {
    return DB.withConnection { implicit connection =>
      SQL("select * from " + _DB_TBL_USER + " where login ILIKE {login}").on(
          'login-> login).as(UserDB.simple.singleOpt)
    }
  }
   
  def insert(user: User) : Int = {
    return DB.withConnection { implicit connection =>
      SQL(
        """
          insert into """ + _DB_TBL_USER + """ (login, password, created) values (
            {login}, {password}, {created}
          ) RETURNING id
        """
      ).on(
        'login -> user.login,
        'password -> user.password,
        'created -> user.created.toDate()
      ).as(int("id").single)
    }
  }
  
  def findByEmail(email: String): Option[User] = {
    return DB.withConnection { implicit connection =>
      SQL(
          """
            select * from """ + _DB_TBL_USER + """
            JOIN """ + _DB_TBL_USER_EMAIL + """
            ON (""" + _DB_TBL_USER + """.id = """ + _DB_TBL_USER_EMAIL + """.user_id)
            where email = {email}
          """
          ).on('email-> email).as(UserDB.simple.singleOpt)
    }
  }
}