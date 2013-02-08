import play.api.Application
import play.api.GlobalSettings
import play.api.Logger
import java.io._
import play.api.mvc.RequestHeader
import play.api.mvc.Handler
import play.api.mvc.Security
import utils._

object Global extends GlobalSettings {

  val appTablesDB = List(
      Configuration._TABLE_USER_KEY,
      Configuration._TABLE_EMAIL_KEY,
      Configuration._TABLE_MEDIA_KEY,
      Configuration._TABLE_MEDIA_TYPE_KEY,
      Configuration._TABLE_TAG_KEY,
      Configuration._TABLE_MESSAGE_KEY,
      Configuration._TABLE_MESSAGE_NOTIFICATION_KEY,
      Configuration._TABLE_NOTIFICATION_KEY,
      Configuration._TABLE_NOTIFICATION_USER_KEY)
  
  override def onStart(app: Application) {
    Logger.info(getApplicationName() + " application has started")
    
    getValue("app.version")
    getValue(Configuration._APP_TOKEN)
     
    checkApplicationConnection()
    checkDBConfiguration()
    checkFolderStorePhotos();
    
    getValue("app.google.analytics")
  }
  
  override def onStop(app: Application) {
    Logger.info(getApplicationName() + " application shutdown...")
  }
  
  override def onRouteRequest(request: RequestHeader): Option[Handler] = {
    val username = request.session.get(Security.username)
    val sessionId = request.session.get("sessionId")
    if (username.isDefined && sessionId.isDefined) {
      MDCUtils.getOrOpenSession(username.get, sessionId.get)
    } else {
    	MDCUtils.closeSession()
    }
    Logger.info("onRouteRequest " + request.path)
    super.onRouteRequest(request)
  }
  
  def checkApplicationConnection() {
    getValue(Configuration._APP_CREATE_NEW_USER_CODE)
    getValue(Configuration._APP_ADMIN_LOGIN)
    getValue(Configuration._APP_CONNECTION_DATE)
    getValue(Configuration._APP_HTML_TITLE)
  }

  def checkDBConfiguration() {
    Logger.info("Check database configuration")
    for (table <- appTablesDB) {
      getValue(table)
    }
  }
  
  def getApplicationName() = getValue(Configuration._APP_TITLE)
  
  def getValue(key: String) : String = {
    val value: String = play.Configuration.root().getString(key)
    if (value != null) {
      Logger.info("Value {" + value + "} for Key {" + key + "} found in configuration file")
      return value
    }
    throw new IllegalArgumentException("Value for Key {" + key + "} not found in configuration file"); 
  }
  
  def checkFolderStorePhotos() {
    createDirectoryIfNotExists(Configuration._APP_UPLOAD_PHOTO)
    createDirectoryIfNotExists(Configuration._APP_UPLOAD_STANDARD_PHOTO)
    createDirectoryIfNotExists(Configuration._APP_UPLOAD_THUMBNAIL_PHOTO)
    
	  createDirectoryIfNotExists(Configuration._APP_STANDARD_PHOTO)
    createDirectoryIfNotExists(Configuration._APP_THUMBNAIL_PHOTO)
    createDirectoryIfNotExists(Configuration._APP_800x600_PHOTO)
    
    createDirectoryIfNotExists(Configuration._APP_MEDIA_VIDEO_FOLDER)
    createDirectoryIfNotExists(Configuration._APP_MEDIA_VIDEO_STANDARD_FOLDER)
    createDirectoryIfNotExists(Configuration._APP_MEDIA_VIDEO_UPLOAD_FOLDER)
  }
  
  def createDirectoryIfNotExists(keyDirectory: String) {
    val directory: String = getValue(keyDirectory)
    if (!new File(directory).isDirectory()) {
      val mkdir = new File(directory).mkdir();
      if (mkdir) {
        Logger.info("Directory {" + directory + "} created")
      }
    }
  }
}	