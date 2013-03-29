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
    Logger.info("{} application has started", ConfigurationUtils.getAppName)
    Logger.info("Version {}", ConfigurationUtils.getAppVersion)
     
    Logger.info("# check file configuration")
    ConfigurationUtils.getAdminLogin
    ConfigurationUtils.getHTMLTitle
    ConfigurationUtils.getGoogleAnalyticsCode
    
    checkDBConfiguration()
    checkFolderStorePhotos();
    
    ConfigurationUtils.schedulers
  }
  
  override def onStop(app: Application) {
    Logger.info("{} application shutdown...", ConfigurationUtils.getAppName)
  }
  
  override def onRouteRequest(request: RequestHeader): Option[Handler] = {
    val sessionId = request.session.get(ConfigurationUtils.getSessionID)
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
      ConfigurationUtils.getStringValue(table)
    }
  }
  
  private def checkFolderStorePhotos() {
    Logger.info("### check folders")
    createDirectoryIfNotExists(ConfigurationUtils.getPhotoRootFolderPath)
    createDirectoryIfNotExists(ConfigurationUtils.getVideoRootFolderPath)
    
    createDirectoryIfNotExists(ConfigurationUtils.getPhotoUploadFolderPath)
    createDirectoryIfNotExists(ConfigurationUtils.getVideoUploadFolderPath)
    
    createDirectoryIfNotExists(ConfigurationUtils.getPhotoUploadStandardFolderPath)
    createDirectoryIfNotExists(ConfigurationUtils.getPhotoUploadThumbnailFolderPath)
    
    createDirectoryIfNotExists(ConfigurationUtils.getPhotoStandardFolderPath)
    createDirectoryIfNotExists(ConfigurationUtils.getPhotoThumbnailFolderPath)
    createDirectoryIfNotExists(ConfigurationUtils.getPhoto800x600FolderPath)
    
    createDirectoryIfNotExists(ConfigurationUtils.getVideoStandardFolderPath)
  }
  
  private def createDirectoryIfNotExists(directory: String) {
    if (!new File(directory).isDirectory()) {
      if (new File(directory).mkdir()) {
        Logger.info("Directory {} created", directory)
      } else {
        Logger.error("Directory {} created", directory)
      }
    }
  }
}	