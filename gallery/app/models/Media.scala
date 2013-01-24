package models

import java.util.Date
import org.joda.time.DateTime
import java.security.MessageDigest
import play.api.Play
import play.api.db._
import play.api.Play.current
import anorm.SqlParser._
import anorm._
import java.io.File
import db.MediaDB
import play.api.Logger
import db.OrderENUM
import utils._

object Visibility extends Enumeration {
  type Visibility = Value
  val PUBLIC, PRIVATE = Value
}
import Visibility._


case class Media(id: Pk[Long] = NotAssigned, filename: String, title: String, description: Option[String], visibility: Visibility, created: DateTime)

object Media {

  def toVisibility(public: Boolean) : Visibility = {
    if (public) {
      return Visibility.PUBLIC
    } else {
      return Visibility.PRIVATE
    }
  }
  
  def toBoolean(visibility: Visibility) : Boolean = {
    visibility match {
      case PUBLIC => true
      case _ => false
    }
  }
  
  def create(filename: String, title: String, description: Option[String], isPublic : Boolean, tags: Seq[String]) : Boolean = {
    if (filename != null && title != null && !title.isEmpty()) {
      val file: File = new File(Configuration.getPhotoUploadStandardDirectory() + filename)
      if (file.isFile()) {
        
        val thumbnail: File = new File(Configuration.getPhotoUploadThumbnailDirectory() + filename)
        if (thumbnail.isFile()) {
          FileUtils.move(thumbnail, Configuration.getPhotoThumbnailDirectory(), filename)
        } else {
          FileUtils.createThumbnails(
            Configuration.getPhotoUploadStandardDirectory(),
            Configuration.getPhotoThumbnailDirectory(), filename, 200, 150) 
        }
            
       FileUtils.createThumbnails(
            Configuration.getPhotoUploadStandardDirectory(),
            Configuration.getPhoto800x600Directory(), filename, 800, 600)
            
        FileUtils.move(file, Configuration.getPhotoStandardDirectory(), filename)

        val id: Int = MediaDB.insert(Media(null, filename, title, description, toVisibility(isPublic), DateTime.now()))
     	if (id.isInstanceOf[Int]) {
     	  Tag.addTagsToPhoto(id, tags)
     	  return true
     	}
      } 
    }
    return false
  }
  
  def list(offset: Long, limit: Int) : Seq[Media] = {
    return MediaDB.findAll(offset, limit)
  }
  
  def list(photos: Seq[Long], offset: Long, limit: Int) : Seq[Media] = {
	 return MediaDB.findAll(photos, offset, limit)
  }
  
  def list(photos: Seq[Long]) : Seq[Media] = {
    return MediaDB.findAll(photos)
  }
   
  def list(dateTime: DateTime) : Seq[Media] = MediaDB.findAllFrom(dateTime)
    
  def total() = MediaDB.count()
  
  def getPreviousPhoto(photoId: Long) : Media = {
    val photos = MediaDB.findAll()
    return getNextElement(photoId, photos)
  }
  
  def getNextPhoto(photoId: Long) : Media = {
    val photos = MediaDB.findAll(OrderENUM.ASC)
    return getNextElement(photoId, photos)
  }
  
  def getPreviousPhoto(photoId: Long, photoIds: Seq[Long]) : Media = {
    val photos = MediaDB.findAll(photoIds)
    return getNextElement(photoId, photos)
  }
  
  def getNextPhoto(photoId: Long, photoIds: Seq[Long]) : Media = {
    val photos = MediaDB.findAll(photoIds, OrderENUM.ASC)
    return getNextElement(photoId, photos)
  }
  
  private def getNextElement(photoId: Long, photos: Seq[Media]) : Media = {
    var find: Media = null
  	val ite = photos.iterator
  	while(ite.hasNext) {
  		val photo = ite.next()
  		if (photo.id.get == photoId && ite.hasNext) {
  			return ite.next()
  		}
  	}
  	return null
  }
}