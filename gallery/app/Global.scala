import play.api.Application
import play.api.GlobalSettings
import play.api.Logger
import java.io._
import play.api.mvc.RequestHeader
import play.api.mvc.Handler
import play.api.mvc.Security
import utils._
import org.slf4j.LoggerFactory

object Global extends GlobalSettings {

  private val Logger = LoggerFactory.getLogger("GlobalSettings")
  
  private val appTablesDB = List(
      Configuration._TABLE_USER_KEY,
      Configuration._TABLE_EMAIL_KEY,
      Configuration._TABLE_MEDIA_KEY,
      Configuration._TABLE_MEDIA_TYPE_KEY,
      Configuration._TABLE_TAG_KEY,
      Configuration._TABLE_MESSAGE_KEY,
      Configuration._TABLE_MESSAGE_NOTIFICATION_KEY,
      Configuration._TABLE_NOTIFICATION_KEY,
      Configuration._TABLE_NOTIFICATION_USER_KEY,
      Configuration._TABLE_MEDIA_POST_KEY,
      Configuration._TABLE_MEDIA_POST_MESSAGE_KEY)
  
  override def onStart(app: Application) {
    Logger.info("{} application has started",
        play.Configuration.root().getString(Configuration._APP_TITLE))
        
    Logger.info("Version {}",
        play.Configuration.root().getString("app.version"))
     
    Logger.info("### check file configuration")
    logAndReturnValue(Configuration._APP_ADMIN_LOGIN)
    logAndReturnValue(Configuration._APP_CONNECTION_DATE)
    logAndReturnValue(Configuration._APP_HTML_TITLE)
    logAndReturnValue("app.google.analytics")
    
    checkDBConfiguration()
    checkFolderStorePhotos();
  }
  
  override def onStop(app: Application) {
    Logger.info("{} application shutdown...", getApplicationName())
  }
  
  override def onRouteRequest(request: RequestHeader): Option[Handler] = {
    val username = request.session.get(Security.username)
    val sessionId = request.session.get("sessionId")
    if (username.isDefined && sessionId.isDefined) {
      MDCUtils.getOrOpenSession(username.get, sessionId.get)
    } else {
    	MDCUtils.closeSession()
    }
    super.onRouteRequest(request)
  }
  
  private def checkDBConfiguration() {
    Logger.info("## check database tables")
    for (table <- appTablesDB) {
      logAndReturnValue(table)
    }
  }
  
  private def getApplicationName() = logAndReturnValue(Configuration._APP_TITLE)
  
  private def logAndReturnValue(key: String) : String = {
    val value: String = play.Configuration.root().getString(key)
    if (value == null) {
    	throw new IllegalArgumentException("Value for Key {" + key + "} not found in configuration file"); 
    }
    Logger.info(key + ":" + value)
    return value
  }
  
  private def checkFolderStorePhotos() {
    Logger.info("# check folders")
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
  
  private def createDirectoryIfNotExists(keyDirectory: String) {
    val directory: String = logAndReturnValue(keyDirectory)
    if (!new File(directory).isDirectory()) {
      if(new File(directory).mkdir()) {
        Logger.info("Directory {} created", directory)
      } else {
        Logger.error("Directory {} created", directory)
      }
    }
  }
}	