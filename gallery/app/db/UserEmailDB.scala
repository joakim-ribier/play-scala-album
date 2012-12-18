package models

import java.util.Date
import org.joda.time.DateTime
import play.api.db._
import play.api.Play.current
import anorm.SqlParser._
import anorm._
import utils.Configuration

object UserEmailDB {

  private val _DB_TBL_USER_EMAIL: String = play.Configuration.root().getString(Configuration._TABLE_EMAIL_KEY)
  private val _DB_TBL_USER: String = play.Configuration.root().getString(Configuration._TABLE_USER_KEY)
  
  def findByLogin(login: String): String = {
    return DB.withConnection { implicit connection =>
      SQL("""
          select * from """ + _DB_TBL_USER_EMAIL + """
          join """ + _DB_TBL_USER + """
          on (user_id=""" + _DB_TBL_USER + """.id)
          where login = {login}
          """)
      .on(
        'login-> login
        ).as(str("email").single)
    }
  }
}