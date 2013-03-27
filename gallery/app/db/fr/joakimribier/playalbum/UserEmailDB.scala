package db.fr.joakimribier.playalbum
import play.api.db._
import play.api.Play.current
import anorm.SqlParser._
import anorm._
import utils._
import utils.fr.joakimribier.playalbum.ConfigurationUtils

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