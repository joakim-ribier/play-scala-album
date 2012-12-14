package models

import anorm._

case class Tag(id: Pk[Long] = NotAssigned, tag: String)

object Tag {

  def addTagsToPhoto(photo: Long, tags: Seq[String]) = {
    for (tag <- tags) {
      TagDB.insert(photo, tag)
    }
  }
  
  def list() = TagDB.findAll()
  
  def list(tags: Seq[String]) = TagDB.findAll(tags)
}