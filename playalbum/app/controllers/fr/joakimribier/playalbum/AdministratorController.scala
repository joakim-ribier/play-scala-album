package controllers.fr.joakimribier.playalbum

import java.io.File
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import models.fr.joakimribier.playalbum.Notification
import play.api.data.Form
import play.api.data.Forms.boolean
import play.api.data.Forms.list
import play.api.data.Forms.longNumber
import play.api.data.Forms.nonEmptyText
import play.api.data.Forms.optional
import play.api.data.Forms.text
import play.api.data.Forms.tuple
import play.api.data.validation.Constraints
import play.api.i18n.Lang
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.mvc.Action
import play.api.mvc.Controller
import utils.fr.joakimribier.playalbum.ConfigurationUtils
import utils.fr.joakimribier.playalbum.FileUtils
import views.html.fr.joakimribier.playalbum
import models.fr.joakimribier.playalbum.Media
import models.fr.joakimribier.playalbum.Post
import utils.fr.joakimribier.playalbum.OrderEnum
import models.fr.joakimribier.playalbum.Tag
import models.fr.joakimribier.playalbum.MediaType
import models.fr.joakimribier.playalbum.Feedback
import models.fr.joakimribier.playalbum.FeedbackClass

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
object AdministratorController extends Controller with Secured {

  private val Logger = LoggerFactory.getLogger("AdministratorController")
  
  private val _TITLE_HTML: String = ConfigurationUtils.getHTMLTitle()
  private val addOrUpdateMediaForm = Form (
    tuple (
      "filename" -> text,
      "type" -> text,
      "title" -> text.verifying(Constraints.maxLength(30)),
      "description" -> optional(text),
      "public" -> boolean,
      "tags" -> list(text),
      "mediaId" -> optional(longNumber)
    ) verifying (Messages("administrator.add.new.photo.verifying.text")(Lang("fr")), result => result match {
      case (filename, mediaType, title, description, public, tags, mediaId) => Media.createOrUpdate(filename, Media.toMediaType(mediaType), title, description, public, tags, mediaId)
    })
  )

  private val createNewNotificationForm = Form.apply("notification" -> text)
  private val createNewNotificationAlarmForm = Form (
    tuple (
      "startDate" -> nonEmptyText,
      "endDate" -> nonEmptyText,
      "messageIds" -> list(text)
    )
  )
  
  def index = withAdmin { username => implicit request =>
     Redirect(routes.AdministratorController.listPhotoUploaded)
  }
  
  def saveMedia = withAdmin { username => implicit request =>
    val userTemplate = AuthenticationController.userTemplate(username, request.session)
    addOrUpdateMediaForm.bindFromRequest.fold(
      // Form has errors, redisplay it
      formWithErrors => BadRequest(views.html.fr.joakimribier.playalbum.adminAddOrUpdateMedia(_TITLE_HTML, null, userTemplate, formWithErrors, Tag.list())),
      // We got a valid User value
      value =>  {
        val photos: List[String] = FileUtils.listFilename(ConfigurationUtils.getPhotoUploadThumbnailDirectory())
        val videos: List[String] = FileUtils.listFilename(ConfigurationUtils.getMediaVideoFolderUploadDirectory())
        
        val mediaTitle = value._3
        val mediaId = value._7
        if (mediaId.isDefined) {
          val feedback = new Feedback(Messages("administrator.update.media.success.html", mediaTitle)(Lang("fr")), FeedbackClass.ok)
        	Ok(views.html.fr.joakimribier.playalbum.adminListMedia(_TITLE_HTML, feedback, userTemplate, Tag.list(), photos, videos))
        } else {
        	val feedback = new Feedback(Messages("administrator.add.media.success.html", mediaTitle)(Lang("fr")), FeedbackClass.ok)
        	Ok(views.html.fr.joakimribier.playalbum.adminListMedia(_TITLE_HTML, feedback, userTemplate, Tag.list(), photos, videos))
        }
      }
   )
  }
  
  def upload = withAdmin { username => implicit request =>
    val files = request.body.asMultipartFormData.get.files.seq
    val formats = ConfigurationUtils.getMediaFormatsAllowed()
    for (media <- files) {
      val filename = media.filename
      val contentType = media.contentType
      val fileType: String = FileUtils.getFileType(filename)
      
      if (ConfigurationUtils.isMediaFormatAllowed(formats, fileType)) {
      	val newFileName = "_" + DateTime.now().getMillis() + "." + fileType
        if (ConfigurationUtils._MEDIA_FORMAT_VIDEO.equalsIgnoreCase(fileType)) {
          media.ref.moveTo(new File(ConfigurationUtils.getMediaVideoFolderUploadDirectory() + newFileName))
        } else {
    			media.ref.moveTo(new File(ConfigurationUtils.getPhotoUploadStandardDirectory() + newFileName))
    			FileUtils.createThumbnails(
    					ConfigurationUtils.getPhotoUploadStandardDirectory(),
    					ConfigurationUtils.getPhotoUploadThumbnailDirectory(), newFileName, 200, 150)
        }
      }
      
    }
    Ok.as("success")
  }
  
  def listPhotoUploaded = withAdmin { username => implicit request =>
    val userTemplate = AuthenticationController.userTemplate(username, request.session)
    val photos: List[String] = FileUtils.listFilename(ConfigurationUtils.getPhotoUploadThumbnailDirectory())
    val videos: List[String] = FileUtils.listFilename(ConfigurationUtils.getMediaVideoFolderUploadDirectory())
    Ok(views.html.fr.joakimribier.playalbum.adminListMedia(_TITLE_HTML, null, userTemplate, Tag.list(), photos, videos))
  }
  
  def addNewPhoto(name: String) = withAdmin { username => implicit request =>
    val userTemplate = AuthenticationController.userTemplate(username, request.session)
    val formFilled = addOrUpdateMediaForm.fill(
        name,
        MediaType.PHOTO.label,
        "", Option.empty, false,
        List(ConfigurationUtils.getStringValue(ConfigurationUtils._APP_TAG_DEFAULT)), Option.empty)
    Ok(views.html.fr.joakimribier.playalbum.adminAddOrUpdateMedia(_TITLE_HTML, null, userTemplate, formFilled, Tag.list()))  
  }
  
  def redirectToAddVideo(file: String) = withAdmin { username => implicit request =>
    val userTemplate = AuthenticationController.userTemplate(username, request.session)
    val formFilled = addOrUpdateMediaForm.fill(
        file,
        MediaType.VIDEO.label,
        "", Option.empty, false,
        List(ConfigurationUtils.getStringValue(ConfigurationUtils._APP_TAG_DEFAULT)), Option.empty)
    Ok(views.html.fr.joakimribier.playalbum.adminAddOrUpdateMedia(_TITLE_HTML, null, userTemplate, formFilled, Tag.list()))  
  }
  
  def notification = withAdmin { username => implicit request =>
    val userTemplate = AuthenticationController.userTemplate(username, request.session)
    Ok(views.html.fr.joakimribier.playalbum.adminNotification(_TITLE_HTML, null, userTemplate, Tag.list(), Notification.listMessages(), Notification.list()))
  }
  
  def saveNewNotification = withAdmin { username => implicit request =>
    Logger.info("save new notification")
    val userTemplate = AuthenticationController.userTemplate(username, request.session)
    createNewNotificationForm.bindFromRequest.fold(
      formWithErrors => Redirect(routes.AdministratorController.notification).flashing("notifcation-create-error" -> Messages("app.global.error")(Lang("fr"))),
      value => {
       Logger.info("create new notification : " + value)
       val id = Notification.createMessage(value)
       if (id.isInstanceOf[Long]) {
      	 Redirect(routes.AdministratorController.notification)
       } else {
      	 Redirect(routes.AdministratorController.notification).flashing("notifcation-create-error" -> Messages("app.global.error")(Lang("fr")))
       }
      }
    )
  }
  
  def saveNewNotificationAlarm = withAdmin { username => implicit request =>
    Logger.info("save new notification alarm")
    val userTemplate = AuthenticationController.userTemplate(username, request.session)
    createNewNotificationAlarmForm.bindFromRequest.fold(
      formWithErrors => Redirect(routes.AdministratorController.notification).flashing("notification-alarm-create-error" -> Messages("app.global.error.missing.field.form")(Lang("fr"))),
      value => {
        try {
        	if (Notification.add(value._1, value._2, value._3)) {
        		Redirect(routes.AdministratorController.notification)
        	} else {
        		Redirect(routes.AdministratorController.notification).flashing("notification-alarm-create-error" -> Messages("app.global.error")(Lang("fr")))
        	}
        } catch {
        	case e => {
        		Logger.error(e.getMessage(), e)
        		Redirect(routes.AdministratorController.notification).flashing("notification-alarm-create-error" -> e.getMessage())
        	}
        }
      }
    )
  }
  
  def deleteNotificationMessage = withAdmin { username => implicit request =>
    val value = request.body.asFormUrlEncoded.get("messageid-post")
    Logger.info("delete notification message {}", value)
    val result = Notification.removeMessage(value(0).toLong)
    if (result == 1) {
    	Ok(Json.obj("status" -> "success"))
    } else {
      Ok(Json.obj("status" -> "failed", "error-message" -> Messages("page.adminNotification.delete.message.failed")(Lang("fr"))))
    }
  }
  
  def deleteNotification = withAdmin { username => implicit request =>
    val value = request.body.asFormUrlEncoded.get("notificationid-post")
    Logger.info("delete notification {}", value)
    val result = Notification.remove(value(0).toLong)
    if (result > 0) {
    	Ok(Json.obj("status" -> "success"))
    } else {
      Ok(Json.obj("status" -> "failed", "error-message" -> Messages("page.adminNotification.delete.failed")(Lang("fr"))))
    }
  }
  
  def deletePhotoToUploadDirectory = withAdmin { username => implicit request =>
    val value = request.body.asFormUrlEncoded.get("filename-post")
    Logger.info("delete photo {} to upload server directory", value)
    try {
      if (FileUtils.delete(value(0), ConfigurationUtils.getPhotoUploadThumbnailDirectory)) {
      	if (FileUtils.delete(value(0), ConfigurationUtils.getPhotoUploadStandardDirectory)) {
      		Ok(Json.obj("status" -> "success"))
      	} else {
      		Ok(Json.obj("status" -> "failed", "error-message" -> fadOutLabel(Messages("administrator.delete.to.upload.directory.photo.failed", value(0))(Lang("fr")))))
      	}  
      } else {
        Ok(Json.obj("status" -> "failed", "error-message" -> fadOutLabel(Messages("administrator.delete.to.upload.directory.photo.failed", value(0))(Lang("fr")))))
      }
    } catch {
      case e: Throwable => {
        Logger.error(e.getMessage(), e) 
        Ok(Json.obj("status" -> "failed", "error-message" -> fadOutLabel(Messages("administrator.delete.to.upload.directory.photo.failed", value(0))(Lang("fr")))))
      }      
    }
  }
  
   def deleteVideoToUploadDirectory = withAdmin { username => implicit request =>
    val value = request.body.asFormUrlEncoded.get("filename-post")
    Logger.info("delete video {} to upload server directory", value)
    try {
      if (FileUtils.delete(value(0), ConfigurationUtils.getMediaVideoFolderUploadDirectory)) {
        Ok(Json.obj("status" -> "success"))
      } else {
      	Ok(Json.obj("status" -> "failed", "error-message" -> fadOutLabel(Messages("administrator.delete.to.upload.directory.video.failed", value(0))(Lang("fr")))))
      }  
    } catch {
      case e: Throwable => {
        Logger.error(e.getMessage(), e) 
        Ok(Json.obj("status" -> "failed", "error-message" -> fadOutLabel(Messages("administrator.delete.to.upload.directory.video.failed", value(0))(Lang("fr")))))
      }      
    }
  }
  
  def displayAllMedia = withAdmin { username => implicit request =>
    redirectToDisplayAllMedia(Option.empty, username)(request)
  }
  
  def displayAllMediaWithMessage(key: String) = withAdmin { username => implicit request =>
  	redirectToDisplayAllMedia(Option.apply(key), username)(request)
  }
  
  private def redirectToDisplayAllMedia(messageKey: Option[String], username: String) = Action { implicit request =>
    val userTemplate = AuthenticationController.userTemplate(username, request.session)
    val medias = Media.list(OrderEnum.DESC)
    Ok(views.html.fr.joakimribier.playalbum.adminAlbumMedias(_TITLE_HTML,
        AuthenticationController.buildFeedbackObjFromRequestOrKey(request, messageKey), userTemplate, Tag.list(), medias))
  }
  
  def deleteMediaToAlbum = withAdmin { username => implicit request =>
    val value = request.body.asFormUrlEncoded.get("mediaid-post")
    Logger.info("delete media {} to album", value)
    try {
      val media = Media.get(value(0).toLong)
      val posts = Post.list(Option.apply(media.id.get))
      if (posts.size > 0) {
        Ok(Json.obj("status" -> "failed",
            "error-message" -> fadOutLabel(Messages("administrator.delete.media.with.comment.html", media.title)(Lang("fr")))))
      } else {
      	Media.remove(Option.apply(media.id.get))
        if (media.mediaType == MediaType.PHOTO) {
        	FileUtils.delete(media.filename, ConfigurationUtils.getPhotoThumbnailDirectory)
        	FileUtils.delete(media.filename, ConfigurationUtils.getPhoto800x600Directory)
      		FileUtils.delete(media.filename, ConfigurationUtils.getPhotoStandardDirectory)
      	} else {
      		FileUtils.delete(media.filename, ConfigurationUtils.getMediaVideoFolderStandardDirectory)
      	}
      	Ok(Json.obj("status" -> "success", "message-key" -> "administrator.delete.media.success.html"))
      }
    } catch {
      case e: Throwable => {
        Logger.error(e.getMessage(), e) 
        Ok(Json.obj("status" -> "failed",
            "error-message" -> fadOutLabel(Messages("administrator.delete.media.failed.html", value(0))(Lang("fr")))))
      }      
    }
  }
  
  def updateMedia(mediaId: String) = withAdmin { username => implicit request =>
    val userTemplate = AuthenticationController.userTemplate(username, request.session)
    try {
      val media = Media.get(mediaId.toLong)
      val formFilled = addOrUpdateMediaForm.fill(
          media.filename,
          media.mediaType.label,
          media.title,
          media.description,
          Media.toBoolean(media.visibility),
          Tag.list(Option.apply(mediaId.toLong)).toList, Option.apply(media.id.get))
      Ok(views.html.fr.joakimribier.playalbum.adminAddOrUpdateMedia(_TITLE_HTML, null, userTemplate, formFilled, Tag.list()))  
    } catch {
      case e: Throwable => {
        Logger.error(e.getMessage(), e) 
        Redirect(routes.AdministratorController.displayAllMedia)
      } 
    }
  }
  
  private def fadOutLabel(message: String) = message + Messages("page.main.message.popup.fadeOut")(Lang("fr"))
}