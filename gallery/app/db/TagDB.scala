package models

import play.api.db._
import play.api.Play.current
import anorm.SqlParser._
import anorm._
import anorm.Row
import utils._

object TagDB {

  private val _DB_TBL_TAG: String = play.Configuration.root().getString("app.db.tbl.tag")
  private val simple = {
    get[Pk[Long]](_DB_TBL_TAG + ".id") ~
    get[String](_DB_TBL_TAG + ".tag") map {
      case id~tag => Tag(id, tag)
    }
  }

  def insert(photo: Long, tag: String) : Int = {
    return DB.withConnection { implicit connection =>
      SQL(
        """
          INSERT INTO """ + _DB_TBL_TAG + """ (photo, tag) VALUES (
            {photo}, {tag}
          ) RETURNING id
        """
      ).on(
        'photo -> photo,
        'tag -> tag.toLowerCase()
      ).as(int("id").single)
    }
  }
  
  def findAll() : Seq[String] = {
    return DB.withConnection { implicit connection =>
      val sql = SQL(
        """
          SELECT distinct tag FROM """ + _DB_TBL_TAG + """ ORDER BY tag 
        """
      )
      sql().map(row => row[String]("tag")).toList
    }
  }
  
  def findAll(tags: Seq[String]) : Seq[Long] = {
    return DB.withConnection { implicit connection =>
      val sql = SQL(
        """
          SELECT distinct photo FROM """ + _DB_TBL_TAG + """ 
          WHERE tag IN ( """ + DBUtils.formatSEQToString(tags) + """) 
        """
      )
      sql().map(row => row[Long]("photo")).toList
    }
  }
}