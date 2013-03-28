package db.fr.joakimribier.playalbum

import anorm._
import anorm.SqlParser._
import models.fr.joakimribier.playalbum.Tag
import play.api.Play.current
import play.api.db.DB
import utils.fr.joakimribier.playalbum.ConfigurationUtils
import utils.fr.joakimribier.playalbum.DBUtils

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
object TagDB {

  private val _DB_TBL_TAG: String = play.Configuration.root().getString(ConfigurationUtils._TABLE_TAG_KEY)
  private val simple = {
    get[Pk[Long]](_DB_TBL_TAG + ".id") ~
    get[String](_DB_TBL_TAG + ".tag") map {
      case id~tag => Tag(id, tag)
    }
  }

  def insert(media: Long, tag: String) : Int = {
    return DB.withConnection { implicit connection =>
      SQL(
        """
          INSERT INTO """ + _DB_TBL_TAG + """ (media, tag) VALUES (
            {media}, {tag}
          ) RETURNING id
        """
      ).on(
        'media -> media,
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
  
  def findBy(mediaId: Long) : Seq[String] = {
    return DB.withConnection { implicit connection =>
      val sql = SQL(
        """
          SELECT tag FROM """ + _DB_TBL_TAG + """
          where media = {mediaId} ORDER BY tag 
        """
      ).on('mediaId -> mediaId)
      sql().map(row => row[String]("tag")).toList
    }
  }
  
  def findAll(tags: Seq[String]) : Seq[Long] = {
    return DB.withConnection { implicit connection =>
      val sql = SQL(
        """
          SELECT distinct media FROM """ + _DB_TBL_TAG + """ 
          WHERE tag IN ( """ + DBUtils.formatSEQToString(tags) + """) 
        """
      )
      sql().map(row => row[Long]("media")).toList
    }
  }
  
  def delete(mediaId: Long) : Int = {
    return DB.withTransaction { implicit connection =>
      SQL("DELETE FROM " + _DB_TBL_TAG + " WHERE media = {mediaId}"
          ).on('mediaId -> mediaId).executeUpdate()
    }
  }
}