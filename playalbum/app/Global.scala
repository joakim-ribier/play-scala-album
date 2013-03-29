import java.io.File
import org.slf4j.LoggerFactory
import play.api.Application
import play.api.GlobalSettings
import play.api.mvc.Handler
import play.api.mvc.RequestHeader
import utils.fr.joakimribier.playalbum.ConfigurationUtils
import utils.fr.joakimribier.playalbum.MDCUtils

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
object Global extends GlobalSettings {

  private val Logger = LoggerFactory.getLogger("GlobalSettings")
  
  private val appTablesDB = List(
      ConfigurationUtils._TABLE_USER_KEY,
      ConfigurationUtils._TABLE_EMAIL_KEY,
      ConfigurationUtils._TABLE_MEDIA_KEY,
      ConfigurationUtils._TABLE_MEDIA_TYPE_KEY,
      ConfigurationUtils._TABLE_TAG_KEY,
      ConfigurationUtils._TABLE_MESSAGE_KEY,
      ConfigurationUtils._TABLE_MESSAGE_NOTIFICATION_KEY,
      ConfigurationUtils._TABLE_NOTIFICATION_KEY,
      ConfigurationUtils._TABLE_NOTIFICATION_USER_KEY,
      ConfigurationUtils._TABLE_MEDIA_POST_KEY,
      ConfigurationUtils._TABLE_MEDIA_POST_MESSAGE_KEY)
  
  override def onStart(app: Application) {
    Logger.info("{} application has started",
        play.Configuration.root().getString(ConfigurationUtils._APP_TITLE))
        
    Logger.info("Version {}",
        play.Configuration.root().getString("app.version"))
     
    Logger.info("### check file configuration")
    logAndReturnValue(ConfigurationUtils._APP_ADMIN_LOGIN)
    logAndReturnValue(ConfigurationUtils._APP_CONNECTION_DATE)
    logAndReturnValue(ConfigurationUtils._APP_HTML_TITLE)
    logAndReturnValue("app.google.analytics")
    
    checkDBConfiguration()
    checkFolderStorePhotos();
    
    ConfigurationUtils.schedulers
  }
  
  override def onStop(app: Application) {
    Logger.info("{} application shutdown...", getApplicationName())
  }
  
  override def onRouteRequest(request: RequestHeader): Option[Handler] = {
    val sessionId = request.session.get(ConfigurationUtils._SESSION_ID_KEY)
    if (sessionId.isDefined) {
      MDCUtils.put(sessionId.get)
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
  
  private def getApplicationName() = logAndReturnValue(ConfigurationUtils._APP_TITLE)
  
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
    createDirectoryIfNotExists(ConfigurationUtils._APP_UPLOAD_PHOTO)
    createDirectoryIfNotExists(ConfigurationUtils._APP_UPLOAD_STANDARD_PHOTO)
    createDirectoryIfNotExists(ConfigurationUtils._APP_UPLOAD_THUMBNAIL_PHOTO)
    
	  createDirectoryIfNotExists(ConfigurationUtils._APP_STANDARD_PHOTO)
    createDirectoryIfNotExists(ConfigurationUtils._APP_THUMBNAIL_PHOTO)
    createDirectoryIfNotExists(ConfigurationUtils._APP_800x600_PHOTO)
    
    createDirectoryIfNotExists(ConfigurationUtils._APP_MEDIA_VIDEO_FOLDER)
    createDirectoryIfNotExists(ConfigurationUtils._APP_MEDIA_VIDEO_STANDARD_FOLDER)
    createDirectoryIfNotExists(ConfigurationUtils._APP_MEDIA_VIDEO_UPLOAD_FOLDER)
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