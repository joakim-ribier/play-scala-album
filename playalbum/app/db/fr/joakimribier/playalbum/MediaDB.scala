package db.fr.joakimribier.playalbum

import java.util.Date

import org.joda.time.DateTime

import anorm._
import anorm.SqlParser._
import models.fr.joakimribier.playalbum.Media
import play.api.Play.current
import play.api.db.DB
import utils.fr.joakimribier.playalbum.ConfigurationUtils
import utils.fr.joakimribier.playalbum.DBUtils
import utils.fr.joakimribier.playalbum.OrderEnum

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
object MediaDB {

  private val _DB_TBL_MEDIA: String = play.Configuration.root().getString(ConfigurationUtils._TABLE_MEDIA_KEY)
  private val _DB_TBL_MEDIA_TYPE: String = play.Configuration.root().getString(ConfigurationUtils._TABLE_MEDIA_TYPE_KEY)
  private val _DB_TBL_TAG: String = play.Configuration.root().getString(ConfigurationUtils._TABLE_TAG_KEY)
  
  private val simple = {
    get[Pk[Long]](_DB_TBL_MEDIA + ".id") ~
    get[String](_DB_TBL_MEDIA + ".filename") ~
    get[String](_DB_TBL_MEDIA_TYPE + ".media_type") ~
    get[String](_DB_TBL_MEDIA + ".title") ~
    get[Option[String]](_DB_TBL_MEDIA + ".description") ~
    get[Boolean](_DB_TBL_MEDIA + ".public") ~
    get[Date](_DB_TBL_MEDIA + ".created") map {
      case id~filename~mediaType~title~description~public~created =>
        Media(id, filename, Media.toMediaType(mediaType), title, description, Media.toVisibility(public), new DateTime(created))
    }
  }
  
  def insert(media: Media) : Long = {
    return DB.withConnection { implicit connection =>
      SQL(
        """
          INSERT INTO """ + _DB_TBL_MEDIA + """ (filename, title, description, public, created, media_type) VALUES (
            {filename}, {title}, {description}, {public}, {created}, {type}
          ) RETURNING id
        """
      ).on(
        'filename -> media.filename,
        'title -> media.title,
        'description -> media.description,
        'public -> Media.toBoolean(media.visibility),
        'created -> media.created.toDate(),
        'type -> Media.toLong(media.mediaType)
      ).as(long("id").single)
    }
  }
  
  def findById(id: Long) : Option[Media] = {
    return DB.withConnection { implicit connection =>
      val sql = SQL(
        """
          SELECT * FROM """ + _DB_TBL_MEDIA + """
          JOIN """ + _DB_TBL_MEDIA_TYPE + """
          ON (""" + _DB_TBL_MEDIA + """.media_type = """ + _DB_TBL_MEDIA_TYPE + """.id)
          WHERE """ + _DB_TBL_MEDIA + """.id = {mediaId}
        """
      ).on('mediaId -> id)
      sql.as(MediaDB.simple.singleOpt)
    }
  }
  
  def findAll() : Seq[Media] = {
    return findAll(OrderEnum.DESC)
  }
  
  def findAll(order: OrderEnum.Value) : Seq[Media] = {
    return DB.withConnection { implicit connection =>
      val sql = SQL(
        """
          SELECT * FROM """ + _DB_TBL_MEDIA + """
          JOIN """ + _DB_TBL_MEDIA_TYPE + """
          ON (""" + _DB_TBL_MEDIA + """.media_type = """ + _DB_TBL_MEDIA_TYPE + """.id)
          ORDER BY created """ + order + """
        """
      )
      sql.as(MediaDB.simple *)
    }
  }
  
  def findAll(offset: Long, limit: Int) : Seq[Media] = {
    return DB.withConnection { implicit connection =>
      val sql = SQL(
        """
          SELECT * FROM """ + _DB_TBL_MEDIA + """
          JOIN """ + _DB_TBL_MEDIA_TYPE + """
          ON (""" + _DB_TBL_MEDIA + """.media_type = """ + _DB_TBL_MEDIA_TYPE + """.id)
          ORDER BY created DESC LIMIT {limit} OFFSET {offset}
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
          JOIN """ + _DB_TBL_MEDIA_TYPE + """
          ON (""" + _DB_TBL_MEDIA + """.media_type = """ + _DB_TBL_MEDIA_TYPE + """.id)
          WHERE """ + _DB_TBL_MEDIA + """.id IN ( """ + DBUtils.formatSEQLongToString(photos) + """) 
          ORDER BY created DESC LIMIT {limit} OFFSET {offset}
        """
      ).on(
        'offset -> offset,
        'limit -> limit)
      sql.as(MediaDB.simple *)
    }
  }
  
  def findAll(photos: Seq[Long]) : Seq[Media] = {
    return findAll(photos, OrderEnum.DESC) 
  }
  
  def findAll(photos: Seq[Long], order: OrderEnum.Value) : Seq[Media] = {
    return DB.withConnection { implicit connection =>
      val sql = SQL(
        """
          SELECT * FROM """ + _DB_TBL_MEDIA + """
          JOIN """ + _DB_TBL_MEDIA_TYPE + """
          ON (""" + _DB_TBL_MEDIA + """.media_type = """ + _DB_TBL_MEDIA_TYPE + """.id)
          WHERE """ + _DB_TBL_MEDIA + """.id IN ( """ + DBUtils.formatSEQLongToString(photos) + """) 
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
          JOIN """ + _DB_TBL_MEDIA_TYPE + """
          ON (""" + _DB_TBL_MEDIA + """.media_type = """ + _DB_TBL_MEDIA_TYPE + """.id)
          WHERE created > {datetime}
        """
      ).on(
        'datetime -> datetime.toDate())
      sql.as(MediaDB.simple *)
    }
  }
  
  def delete(mediaId: Long) : Int = {
    return DB.withTransaction { implicit connection =>
      SQL("DELETE FROM " + _DB_TBL_TAG + " WHERE media = {mediaId}").on('mediaId -> mediaId).executeUpdate()
      SQL("DELETE FROM " + _DB_TBL_MEDIA + " WHERE id = {mediaId}").on('mediaId -> mediaId).executeUpdate()
    }
  }
  
  def update(mediaId: Long, title: String, description: Option[String], isPublic: Boolean) : Long = {
    return DB.withConnection { implicit connection =>
     SQL(
        """
          UPDATE """ + _DB_TBL_MEDIA + """
          SET title = {title}, description = {description}, public = {public}
          WHERE id = {mediaId}
        """
      ).on(
        'mediaId -> mediaId,
        'title -> title,
        'description -> description,
        'public -> isPublic
      ).executeUpdate()
    }
  }
}