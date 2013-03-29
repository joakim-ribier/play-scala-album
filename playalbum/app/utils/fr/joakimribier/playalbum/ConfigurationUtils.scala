package utils.fr.joakimribier.playalbum

import play.Play
import play.libs.Akka
import akka.actor._
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import actor.fr.joakimribier.playalbum.AppActor
import play.api.Logger
import org.slf4j.LoggerFactory

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
object ConfigurationUtils {
  
  private val Logger = LoggerFactory.getLogger("ConfigurationUtils")
  
  private val _MEDIA_FORMAT_SEPARATOR = ","
  private val _MEDIA_FORMAT_VIDEO = "webm"
  
  private val _SESSION_EMAIL_KEY = "user-email"
  private val _SESSION_ID_KEY = "session-id"
  private val _SESSION_TIMEOUT_KEY = "session-timeout"
  
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
    val value = Play.application().configuration().getString(key)
    if (value == null) {
      throw new IllegalArgumentException("Value for Key {" + key + "} not found in application.conf file")
    }
    return value
  }

  def getIntValue(key: String) : Int = {
    val value = getStringValue(key)
    try {
      return value.toInt
    } catch {
      case e: Throwable => throw e
    }
  }
  
  def getSessionEmailID = _SESSION_EMAIL_KEY
  def getSessionID = _SESSION_ID_KEY
  def getSessionTimeoutID = _SESSION_TIMEOUT_KEY
  
  def getHost = ConfigurationUtils.getStringValue("app.host")
  def getAppName = getStringValue("app.name")
  def getAppVersion = getStringValue("app.version")
  def getHTMLTitle = getStringValue("app.html.title")
  def getGoogleAnalyticsCode = getStringValue("app.google.analytics")
  
  def getCreateNewUserCode = getStringValue("app.connection.code")
  def getAdminLogin = getStringValue("app.connection.admin.login")
  def getToken = getStringValue("app.token")
  
  def getDefaultTag = getStringValue("app.tag.default")
  
  def getPhotoRootFolderPath = getStringValue("app.folder.store.photo.root")
  def getVideoRootFolderPath = getStringValue("app.folder.store.video.root")
  
  def getPhotoUploadFolderPath = getStringValue("app.folder.store.photo.upload.root")
  def getVideoUploadFolderPath = getStringValue("app.folder.store.video.upload.root")
  
  def getPhotoUploadStandardFolderPath = getStringValue("app.folder.store.photo.upload.standard.root")
  def getPhotoUploadThumbnailFolderPath = getStringValue("app.folder.store.photo.upload.thumbnail.root")
  
  def getPhotoStandardFolderPath = getStringValue("app.folder.store.photo.standard.root")
  def getPhotoThumbnailFolderPath = getStringValue("app.folder.store.photo.thumbnail.root")
  def getPhoto800x600FolderPath = getStringValue("app.folder.store.photo.800x600.root")

  def getVideoStandardFolderPath = getStringValue("app.folder.store.video.standard.root")
 
  def getDisplayPhotoLimit = getIntValue("app.display.photo.limit")
  def getVideoFormatAllowed = _MEDIA_FORMAT_VIDEO
  def getMediaFormatsAllowed : Seq[String] = {
    val formats = getStringValue("app.media.format.allowed")
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
  
  def getSessionExpiredMinutesDuration = getIntValue("app.session.expired.time.minute")

  def getSendingMailEveryDayDuration = getIntValue("app.send.mail.auto.day.duration")
  def getSendingMailFromValue = getStringValue("app.send.mail.from")
  def getSendingCommentsEveryMinutesDuration = getIntValue("app.send.mail.auto.comment.minutes.duration")
  
  def schedulers {
    val appActor = Akka.system.actorOf(Props[AppActor], name = "applicationactor")
    
    Logger.info("#### Akka.system.scheduler.schedule.sendcomments")
    Akka.system.scheduler.schedule(
      1 minutes,
      getSendingCommentsEveryMinutesDuration minutes,
      appActor,
      "sendcomments"
    )
    
    Logger.info("##### Akka.system.scheduler.schedule.sendnewmedia")
    Akka.system.scheduler.schedule(
      1 minutes,
      1 hours,
      appActor,
      "sendnewmedia"
    )
  }
}