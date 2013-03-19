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
import utils._
import scalax.file.NotFileException
import models.post.Comment

object Visibility extends Enumeration {
  type Visibility = Value
  val PUBLIC, PRIVATE = Value
}
import Visibility._


object MediaType extends Enumeration {
  val PHOTO = new Value(1, "photo")
  val VIDEO = new Value(2, "video")
  class Value(id:Int, value: String) extends Val(id, value) {
  	val dbId = id
    val label = value
  }
}
import MediaType._


case class Media(id: Pk[Long] = NotAssigned, filename: String, mediaType: MediaType.Value, title: String, description: Option[String], visibility: Visibility, created: DateTime)

object Media {

	def create(filename: String, mediaType: MediaType.Value, title: String, description: Option[String], isPublic : Boolean, tags: Seq[String]) : Boolean = {
    if (filename != null && title != null && !title.isEmpty() && mediaType != null) {
      try {
        if (mediaType == MediaType.PHOTO) {
          createPhotoFile(filename)
        } else {
          createVideoFile(filename)
        }
        val id: Long = MediaDB.insert(Media(null, filename, mediaType, title, description, toVisibility(isPublic), DateTime.now()))
     	  if (id.isInstanceOf[Long]) {
     	    Tag.addTagsToPhoto(id, tags)
     	    return true
     	  }  
      } catch {
        case e: Throwable => false
      }
    }
    return false
  }
  
  private def createPhotoFile(filename: String) {
    try {
      val file: File = new File(Configuration.getPhotoUploadStandardDirectory() + filename)
		  if (!file.isFile()) {
		  	throw new NoSuchElementException("photo file " + filename)
		  }  
      
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
    } catch {
      case e: Throwable => {
        Logger.error(e.getMessage(), e)
        throw e
      }
    }
  }
  
  private def createVideoFile(filename: String) {
    try {
      val file: File = new File(Configuration.getMediaVideoFolderUploadDirectory() + filename)
      if (!file.isFile()) {
		  	throw new NoSuchElementException("video file " + filename)
		  }  
      FileUtils.move(file, Configuration.getMediaVideoFolderStandardDirectory(), filename)
    } catch {
      case e: Throwable => {
        Logger.error(e.getMessage(), e)
        throw e
      }
    }
  }
  
  def get(mediaId: Long) : Media = {
    val media = MediaDB.findById(mediaId)
    if (!media.isDefined) {
      throw new NoSuchElementException("media " + mediaId)  
    }
    return media.get
  }
  
  def list(order: OrderEnum.Value) : Seq[Media] = {
    return MediaDB.findAll(order)
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
    val photos = MediaDB.findAll(OrderEnum.ASC)
    return getNextElement(photoId, photos)
  }
  
  def getPreviousPhoto(photoId: Long, photoIds: Seq[Long]) : Media = {
    val photos = MediaDB.findAll(photoIds)
    return getNextElement(photoId, photos)
  }
  
  def getNextPhoto(photoId: Long, photoIds: Seq[Long]) : Media = {
    val photos = MediaDB.findAll(photoIds, OrderEnum.ASC)
    return getNextElement(photoId, photos)
  }
  
  def computePos(mediaId: Option[Long]) : Long = {
    if (!mediaId.isDefined) {
      throw new IllegalArgumentException("params {mediaId} is required")
    }
    
    val medias = MediaDB.findAll(OrderEnum.DESC)
    var pos = 0
    for(media <- medias) {
      pos = pos + 1
      if (media.id.get == mediaId.get) {
        return pos
      }
    }
    return pos
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
  
  def toVisibility(public: Boolean) : Visibility = {
    public match {
      case true => Visibility.PUBLIC
      case _ => Visibility.PRIVATE
    }
  }
  
  def toBoolean(visibility: Visibility) : Boolean = {
    visibility match {
      case Visibility.PUBLIC => true
      case Visibility.PRIVATE => false
      case _ => throw new IllegalArgumentException(
          "visibility {" + visibility +  "} is not supported in the application")
    }
  }
  
  def toMediaType(mediaType: String) : MediaType.Value = {
    mediaType match {
      case "photo" => MediaType.PHOTO
      case "video" => MediaType.VIDEO
      case _ => throw new IllegalArgumentException(
          "media type {" + mediaType +  "} is not supported in the application")
    }
  }
  
  def toLong(mediaType: MediaType.Value) : Int = {
    mediaType match {
      case MediaType.PHOTO => MediaType.PHOTO.dbId
      case MediaType.VIDEO => MediaType.VIDEO.dbId
      case _ => throw new IllegalArgumentException(
          "media type {" + mediaType +  "} is not supported in the application")
    }
  }
  
  def remove(mediaId: Option[Long]) {
    if (!mediaId.isDefined) {
      throw new IllegalArgumentException("params {mediaId} is required")
    }
    val result = MediaDB.delete(mediaId.get)
    if (result != 1) {
    	throw new UnsupportedOperationException("delete media with id " + mediaId.get + " failed")
    }
  }
}