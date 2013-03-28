package models.fr.joakimribier.playalbum

import anorm._
import db.fr.joakimribier.playalbum.TagDB

case class Tag(id: Pk[Long] = NotAssigned, tag: String)

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
object Tag {

  def addTagsToPhoto(photo: Long, tags: Seq[String]) = {
    for (tag <- tags) {
      TagDB.insert(photo, tag)
    }
  }
  
  def list() = TagDB.findAll()
  
  def list(tags: Seq[String]) = TagDB.findAll(tags)

  def list(mediaId: Option[Long]) : Seq[String] = {
    if (!mediaId.isDefined) {
      throw new IllegalArgumentException("params {mediaId} is required")
    }
    return TagDB.findBy(mediaId.get)
  }
  
  def remove(mediaId: Option[Long]) {
    if (!mediaId.isDefined) {
      throw new IllegalArgumentException("params {mediaId} is required")
    }
    TagDB.delete(mediaId.get)
  }
}