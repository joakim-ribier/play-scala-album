package db

import java.util.Date
import org.joda.time.DateTime
import play.api.db._
import play.api.Play.current
import anorm.SqlParser._
import anorm._
import models.User
import utils.Configuration

object UserDB {

  private val _DB_TBL_USER: String = Configuration.getStringValue(Configuration._TABLE_USER_KEY)
	private val _DB_TBL_USER_EMAIL: String = Configuration.getStringValue(Configuration._TABLE_EMAIL_KEY)
  
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