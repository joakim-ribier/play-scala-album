package models.fr.joakimribier.playalbum

import anorm._
import db.fr.joakimribier.playalbum.TagDB

case class Tag(id: Pk[Long] = NotAssigned, tag: String)

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