package db

import models.Photo
import play.api.db._
import play.api.Play.current
import anorm.SqlParser._
import anorm._
import java.util.Date
import org.joda.time.DateTime
import utils._

object PhotoDB {

  private val _DB_TBL_PHOTO: String = play.Configuration.root().getString("app.db.tbl.photo")
  private val simple = {
    get[Pk[Long]](_DB_TBL_PHOTO + ".id") ~
    get[String](_DB_TBL_PHOTO + ".filename") ~
    get[String](_DB_TBL_PHOTO + ".title") ~
    get[Option[String]](_DB_TBL_PHOTO + ".description") ~
    get[Boolean](_DB_TBL_PHOTO + ".public") ~
    get[Date](_DB_TBL_PHOTO + ".created") map {
      case id~filename~title~description~public~created =>
        Photo(id, filename, title, description, Photo.toVisibility(public), new DateTime(created))
    }
  }
  
  def insert(photo: Photo) : Int = {
    return DB.withConnection { implicit connection =>
      SQL(
        """
          insert into """ + _DB_TBL_PHOTO + """ (filename, title, description, public, created) values (
            {filename}, {title}, {description}, {public}, {created}
          ) RETURNING id
        """
      ).on(
        'filename -> photo.filename,
        'title -> photo.title,
        'description -> photo.description,
        'public -> Photo.toBoolean(photo.visibility),
        'created -> photo.created.toDate()
      ).as(int("id").single)
    }
  }
  
  def findAll() : Seq[Photo] = {
    return findAll(OrderENUM.DESC)
  }
  
  def findAll(order: OrderENUM.ORDER) : Seq[Photo] = {
    return DB.withConnection { implicit connection =>
      val sql = SQL(
        """
          SELECT * FROM """ + _DB_TBL_PHOTO + """ ORDER BY created """ + order + """
        """
      )
      sql.as(PhotoDB.simple *)
    }
  }
  
  def findAll(offset: Long, limit: Int) : Seq[Photo] = {
    return DB.withConnection { implicit connection =>
      val sql = SQL(
        """
          SELECT * FROM """ + _DB_TBL_PHOTO + """ ORDER BY created DESC LIMIT {limit} OFFSET {offset}
        """
      ).on(
        'offset -> offset,
        'limit -> limit)
      sql.as(PhotoDB.simple *)
    }
  }
  
  def count() : Long = {
    return DB.withConnection { implicit connection =>
      SQL(
        """
          SELECT count(*) as c FROM """ + _DB_TBL_PHOTO + """
        """
      ).apply().head[Long]("c")
    }
  }
  
  def findAll(photos: Seq[Long], offset: Long, limit: Int) : Seq[Photo] = {
    return DB.withConnection { implicit connection =>
      val sql = SQL(
        """
          SELECT * FROM """ + _DB_TBL_PHOTO + """ 
          WHERE id IN ( """ + DBUtils.formatSEQLongToString(photos) + """) 
          ORDER BY created DESC LIMIT {limit} OFFSET {offset}
        """
      ).on(
        'offset -> offset,
        'limit -> limit)
      sql.as(PhotoDB.simple *)
    }
  }
  
  def findAll(photos: Seq[Long]) : Seq[Photo] = {
    return findAll(photos, OrderENUM.DESC) 
  }
  
  def findAll(photos: Seq[Long], order: OrderENUM.ORDER) : Seq[Photo] = {
    return DB.withConnection { implicit connection =>
      val sql = SQL(
        """
          SELECT * FROM """ + _DB_TBL_PHOTO + """ 
          WHERE id IN ( """ + DBUtils.formatSEQLongToString(photos) + """) 
          ORDER BY created """ + order + """
        """
      )
      sql.as(PhotoDB.simple *)
    }
  }
}