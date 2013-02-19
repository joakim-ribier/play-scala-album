package utils

import play.Play

object Configuration {

  private val _MEDIA_FORMAT_SEPARATOR = ","
  val _MEDIA_FORMAT_VIDEO = "webm"

  val _APP_HOST = "app.host"
	val _APP_TOKEN = "token"
	val _APP_TAG_DEFAULT = "tag.default"
	val _APP_CREATE_NEW_USER_CODE = "app.connection.code"
  val _APP_ADMIN_LOGIN = "app.connection.admin.login"
  val _APP_CONNECTION_DATE = "app.connection.date"
  val _APP_HTML_TITLE = "app.html.title"
  val _APP_TITLE = "app.name"
  val _APP_DISPLAY_PHOTO_LIMIT= "app.display.photo.limit"
  val _APP_MEDIA_FORMAT_ALLOWED = "app.media.format.allowed"
  
  val _APP_UPLOAD_PHOTO = "app.folder.store.upload.photo"
  val _APP_UPLOAD_STANDARD_PHOTO = "app.folder.store.upload.standard.photo"
  val _APP_UPLOAD_THUMBNAIL_PHOTO = "app.folder.store.upload.thumbnail.photo"
  val _APP_STANDARD_PHOTO = "app.folder.store.standard.photo"
  val _APP_THUMBNAIL_PHOTO = "app.folder.store.thumbnail.photo"
  val _APP_800x600_PHOTO = "app.folder.store.800x600.photo"
	
  val _APP_MEDIA_VIDEO_FOLDER = "app.folder.store.video"
  val _APP_MEDIA_VIDEO_UPLOAD_FOLDER= "app.folder.store.upload.video"
  val _APP_MEDIA_VIDEO_STANDARD_FOLDER = "app.folder.store.standard.video"
    
  val _MAIL_FROM = "app.send.mail.from"
	val _MAIL_AUTO_SEND_PRIVATE_KEY = "app.send.mail.auto.private.key"
	val _MAIL_AUTO_SEND_DAY_DURATION = "app.send.mail.auto.day.duration"
	val _MAIL_AUTO_SEND_COMMENTS_MINUTES_DURATION = "app.send.mail.auto.comment.minutes.duration"
  
  val _SESSION_EMAIL_KEY = "user_email"
  val _SESSION_ID_KEY = "sessionId"
  
  val _TABLE_USER_KEY = "app.db.tbl.user"
  val _TABLE_EMAIL_KEY = "app.db.tbl.email"
  val _TABLE_MEDIA_KEY = "app.db.tbl.media"
  val _TABLE_MEDIA_TYPE_KEY = "app.db.tbl.type.media"
  val _TABLE_TAG_KEY = "app.db.tbl.tag"
  val _TABLE_MESSAGE_KEY = "app.db.tbl.message"
  val _TABLE_MESSAGE_NOTIFICATION_KEY = "app.db.tbl.messagenotification"
  val _TABLE_NOTIFICATION_KEY = "app.db.tbl.notification"
  val _TABLE_NOTIFICATION_USER_KEY = "app.db.tbl.notificationuser"
    
  val _TABLE_MEDIA_POST_KEY = "app.db.tbl.mediapost"
  val _TABLE_MEDIA_POST_MESSAGE_KEY = "app.db.tbl.mediapostmessage"
    
  def getStringValue(key: String) : String = {
    return Play.application().configuration().getString(key)
  }

  def getIntValue(key: String) : Int = {
    return Play.application().configuration().getInt(key)
  }
  
  def getHTMLTitle() = getStringValue(_APP_HTML_TITLE)
  
  def getPhotoUploadStandardDirectory() = getStringValue(_APP_UPLOAD_STANDARD_PHOTO)
  def getPhotoUploadThumbnailDirectory() = getStringValue(_APP_UPLOAD_THUMBNAIL_PHOTO)
  
  def getPhotoStandardDirectory() = getStringValue(_APP_STANDARD_PHOTO)
  def getPhotoThumbnailDirectory() = getStringValue(_APP_THUMBNAIL_PHOTO)
  def getPhoto800x600Directory() = getStringValue(_APP_800x600_PHOTO)
  
  def getMediaVideoFolderStandardDirectory() = getStringValue(_APP_MEDIA_VIDEO_STANDARD_FOLDER)
  def getMediaVideoFolderUploadDirectory() = getStringValue(_APP_MEDIA_VIDEO_UPLOAD_FOLDER)
  
  def getAdminLogin() = getStringValue(_APP_ADMIN_LOGIN)
  
  def getToken() = getStringValue(_APP_TOKEN)
  def getCreateNewUserCode() = getStringValue(_APP_CREATE_NEW_USER_CODE)
  
  def getDisplayPhotoLimit() = getIntValue(_APP_DISPLAY_PHOTO_LIMIT)
  
  def getHost() = Configuration.getStringValue(Configuration._APP_HOST)
  
  
  def getMediaFormatsAllowed() : Seq[String] = {
    val formats = getStringValue(_APP_MEDIA_FORMAT_ALLOWED)
    return getMediaFormatsAllowed(formats)
  }
  
  def getMediaFormatsAllowed(formats: String) : Seq[String] = {
    if (formats == null) {
    	return Seq.empty
    }
    
    val formatsArray = formats.toLowerCase().trim().split(_MEDIA_FORMAT_SEPARATOR)
    if (!formatsArray.isEmpty) {
      return formatsArray.toSeq
    }
    return Seq.empty
  }
  
  def isMediaFormatAllowed(formats: Seq[String], format: String) : Boolean = {
    if (formats != null && format != null) {
      return formats.contains(format.toLowerCase().trim())
    } else {
      return false
    }
  }
}