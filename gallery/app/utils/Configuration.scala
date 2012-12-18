package utils
import play.Play

object Configuration {

	val _APP_HOST = "app.host"
	val _APP_TOKEN = "token"

	//:MAIL CONFIGURATION KEY
	val _MAIL_FROM = "app.send.mail.from"
  
	//:SESSION KEY
  val _SESSION_EMAIL_KEY = "user_email"
  val _SESSION_ID_KEY = "sessionId"
  
  //:DATABASE KEY
  val _TABLE_USER_KEY = "app.db.tbl.user"
  val _TABLE_EMAIL_KEY = "app.db.tbl.email"
  val _TABLE_PHOTO_KEY = "app.db.tbl.photo"
  val _TABLE_TAG_KEY = "app.db.tbl.tag"
  
  //:APPLICATION KEY
  val _APP_CREATE_NEW_USER_CODE = "app.connection.code"
  val _APP_ADMIN_LOGIN = "app.connection.admin.login"
  val _APP_CONNECTION_DATE = "app.connection.date"
  val _APP_HTML_TITLE = "app.html.title"
  val _APP_TITLE = "app.name"
  val _APP_DISPLAY_PHOTO_LIMIT= "app.display.photo.limit"

  //:DIRECTORY KEY
  val _APP_UPLOAD_PHOTO = "app.folder.store.upload.photo"
  val _APP_UPLOAD_STANDARD_PHOTO = "app.folder.store.upload.standard.photo"
  val _APP_UPLOAD_THUMBNAIL_PHOTO = "app.folder.store.upload.thumbnail.photo"
      
  val _APP_STANDARD_PHOTO ="app.folder.store.standard.photo"
  val _APP_THUMBNAIL_PHOTO = "app.folder.store.thumbnail.photo"
  val _APP_800x600_PHOTO = "app.folder.store.800x600.photo"
    
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
  
  def getAdminLogin() = getStringValue(_APP_ADMIN_LOGIN)
  
  def getToken() = getStringValue(_APP_TOKEN)
  def getCreateNewUserCode() = getStringValue(_APP_CREATE_NEW_USER_CODE)
  
  def getDisplayPhotoLimit() = getIntValue(_APP_DISPLAY_PHOTO_LIMIT)
}