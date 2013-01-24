package db

import models.Media
import play.api.db._
import play.api.Play.current
import anorm.SqlParser._
import anorm._
import java.util.Date
import org.joda.time.DateTime
import utils._

object MediaDB {

  private val _DB_TBL_MEDIA: String = play.Configuration.root().getString(Configuration._TABLE_MEDIA_KEY)
  private val simple = {
    get[Pk[Long]](_DB_TBL_MEDIA + ".id") ~
    get[String](_DB_TBL_MEDIA + ".filename") ~
    get[String](_DB_TBL_MEDIA + ".title") ~
    get[Option[String]](_DB_TBL_MEDIA + ".description") ~
    get[Boolean](_DB_TBL_MEDIA + ".public") ~
    get[Date](_DB_TBL_MEDIA + ".created") map {
      case id~filename~title~description~public~created =>
        Media(id, filename, title, description, Media.toVisibility(public), new DateTime(created))
    }
  }
  
  def insert(photo: Media) : Int = {
    return DB.withConnection { implicit connection =>
      SQL(
        """
          insert into """ + _DB_TBL_MEDIA + """ (filename, title, description, public, created) values (
            {filename}, {title}, {description}, {public}, {created}
          ) RETURNING id
        """
      ).on(
        'filename -> photo.filename,
        'title -> photo.title,
        'description -> photo.description,
        'public -> Media.toBoolean(photo.visibility),
        'created -> photo.created.toDate()
      ).as(int("id").single)
    }
  }
  
  def findAll() : Seq[Media] = {
    return findAll(OrderENUM.DESC)
  }
  
  def findAll(order: OrderENUM.ORDER) : Seq[Media] = {
    return DB.withConnection { implicit connection =>
      val sql = SQL(
        """
          SELECT * FROM """ + _DB_TBL_MEDIA + """ ORDER BY created """ + order + """
        """
      )
      sql.as(MediaDB.simple *)
    }
  }
  
  def findAll(offset: Long, limit: Int) : Seq[Media] = {
    return DB.withConnection { implicit connection =>
      val sql = SQL(
        """
          SELECT * FROM """ + _DB_TBL_MEDIA + """ ORDER BY created DESC LIMIT {limit} OFFSET {offset}
        """
      ).on(
        'offset -> offset,
        'limit -> limit)
      sql.as(MediaDB.simple *)
    }
  }
  
  def count() : Long = {
    return DB.withConnection { implicit connection =>
      SQL(
        """
          SELECT count(*) as c FROM """ + _DB_TBL_MEDIA + """
        """
      ).apply().head[Long]("c")
    }
  }
  
  def findAll(photos: Seq[Long], offset: Long, limit: Int) : Seq[Media] = {
    return DB.withConnection { implicit connection =>
      val sql = SQL(
        """
          SELECT * FROM """ + _DB_TBL_MEDIA + """ 
          WHERE id IN ( """ + DBUtils.formatSEQLongToString(photos) + """) 
          ORDER BY created DESC LIMIT {limit} OFFSET {offset}
        """
      ).on(
        'offset -> offset,
        'limit -> limit)
      sql.as(MediaDB.simple *)
    }
  }
  
  def findAll(photos: Seq[Long]) : Seq[Media] = {
    return findAll(photos, OrderENUM.DESC) 
  }
  
  def findAll(photos: Seq[Long], order: OrderENUM.ORDER) : Seq[Media] = {
    return DB.withConnection { implicit connection =>
      val sql = SQL(
        """
          SELECT * FROM """ + _DB_TBL_MEDIA + """ 
          WHERE id IN ( """ + DBUtils.formatSEQLongToString(photos) + """) 
          ORDER BY created """ + order + """
        """
      )
      sql.as(MediaDB.simple *)
    }
  }
  
  def findAllFrom(datetime: DateTime) : Seq[Media] = {
    return DB.withConnection { implicit connection =>
      val sql = SQL(
        """
          SELECT * FROM """ + _DB_TBL_MEDIA + """
          WHERE created > {datetime}
        """
      ).on(
        'datetime -> datetime.toDate())
      sql.as(MediaDB.simple *)
    }
  }
  
}