package controllers

import java.io.File
import org.joda.time.DateTime
import models._
import play.api.data.Forms._
import play.api.data.Forms.boolean
import play.api.data.Forms.list
import play.api.data.Forms.text
import play.api.data.Forms.tuple
import play.api.data.validation.Constraints
import play.api.data._
import play.api.data.Form
import play.api.mvc._
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api._
import views.html
import scala.collection.immutable.Seq
import scala.collection.immutable.Nil
import models.UserTemplate
import utils.FileUtils
import utils.Configuration
import play.api.i18n.Messages
import play.api.i18n.Lang
import play.api.data.Mapping
import play.api.libs.json.Json
import models.notification.Notification
import org.slf4j.LoggerFactory

object Administrator extends Controller with Secured {

  private val Logger = LoggerFactory.getLogger("Administrator")
  
  private val _TITLE_HTML: String = Configuration.getHTMLTitle()
  private val addMediaForm = Form (
    tuple (
      "filename" -> text,
      "type" -> text,
      "title" -> text.verifying(Constraints.maxLength(30)),
      "description" -> optional(text),
      "public" -> boolean,
      "tags" -> list(text)
    ) verifying (Messages("administrator.add.new.photo.verifying.text")(Lang("fr")), result => result match {
      case (filename, mediaType, title, description, public, tags) => Media.create(filename, Media.toMediaType(mediaType), title, description, public, tags)
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
     Redirect(routes.Administrator.listPhotoUploaded)
  }
  
  def saveMedia = withAdmin { username => implicit request =>
    val userTemplate = Authentication.userTemplate(username, request.session)
    addMediaForm.bindFromRequest.fold(
      // Form has errors, redisplay it
      formWithErrors => BadRequest(html.adminAddMedia(_TITLE_HTML, null, userTemplate, formWithErrors, Tag.list())),
      // We got a valid User value
      value =>  {
        val photos: List[String] = FileUtils.listFilename(Configuration.getPhotoUploadThumbnailDirectory())
        val videos: List[String] = FileUtils.listFilename(Configuration.getMediaVideoFolderUploadDirectory())
        Ok(views.html.adminListMedia(_TITLE_HTML, null, userTemplate, Tag.list(), photos, videos))
      }
   )
  }
  
  def upload = withAdmin { username => implicit request =>
    val files = request.body.asMultipartFormData.get.files.seq
    val formats = Configuration.getMediaFormatsAllowed()
    for (media <- files) {
      val filename = media.filename
      val contentType = media.contentType
      val fileType: String = FileUtils.getFileType(filename)
      
      if (Configuration.isMediaFormatAllowed(formats, fileType)) {
      	val newFileName = "_" + DateTime.now().getMillis() + "." + fileType
        if (Configuration._MEDIA_FORMAT_VIDEO.equalsIgnoreCase(fileType)) {
          media.ref.moveTo(new File(Configuration.getMediaVideoFolderUploadDirectory() + newFileName))
        } else {
    			media.ref.moveTo(new File(Configuration.getPhotoUploadStandardDirectory() + newFileName))
    			FileUtils.createThumbnails(
    					Configuration.getPhotoUploadStandardDirectory(),
    					Configuration.getPhotoUploadThumbnailDirectory(), newFileName, 200, 150)
        }
      }
      
    }
    Ok.as("success")
  }
  
  def listPhotoUploaded = withAdmin { username => implicit request =>
    val userTemplate = Authentication.userTemplate(username, request.session)
    val photos: List[String] = FileUtils.listFilename(Configuration.getPhotoUploadThumbnailDirectory())
    val videos: List[String] = FileUtils.listFilename(Configuration.getMediaVideoFolderUploadDirectory())
    Ok(views.html.adminListMedia(_TITLE_HTML, null, userTemplate, Tag.list(), photos, videos))
  }
  
  def addNewPhoto(name: String) = withAdmin { username => implicit request =>
    val userTemplate = Authentication.userTemplate(username, request.session)
    val formFilled = addMediaForm.fill(name, MediaType.PHOTO.label, "", Option.empty, false, List(Configuration.getStringValue(Configuration._APP_TAG_DEFAULT)))
    Ok(views.html.adminAddMedia(_TITLE_HTML, null, userTemplate, formFilled, Tag.list()))  
  }
  
  def redirectToAddVideo(file: String) = withAdmin { username => implicit request =>
    val userTemplate = Authentication.userTemplate(username, request.session)
    val formFilled = addMediaForm.fill(file, MediaType.VIDEO.label, "", Option.empty, false, List(Configuration.getStringValue(Configuration._APP_TAG_DEFAULT)))
    Ok(views.html.adminAddMedia(_TITLE_HTML, null, userTemplate, formFilled, Tag.list()))  
  }
  
  def notification = withAdmin { username => implicit request =>
    val userTemplate = Authentication.userTemplate(username, request.session)
    Ok(views.html.adminNotification(_TITLE_HTML, null, userTemplate, Tag.list(), Notification.listMessages(), Notification.list()))
  }
  
  def saveNewNotification = withAdmin { username => implicit request =>
    Logger.info("save new notification")
    val userTemplate = Authentication.userTemplate(username, request.session)
    createNewNotificationForm.bindFromRequest.fold(
      formWithErrors => Redirect(routes.Administrator.notification).flashing("notifcation-create-error" -> Messages("app.global.error")(Lang("fr"))),
      value => {
       Logger.info("create new notification : " + value)
       val id = Notification.createMessage(value)
       if (id.isInstanceOf[Long]) {
      	 Redirect(routes.Administrator.notification)
       } else {
      	 Redirect(routes.Administrator.notification).flashing("notifcation-create-error" -> Messages("app.global.error")(Lang("fr")))
       }
      }
    )
  }
  
  def saveNewNotificationAlarm = withAdmin { username => implicit request =>
    Logger.info("save new notification alarm")
    val userTemplate = Authentication.userTemplate(username, request.session)
    createNewNotificationAlarmForm.bindFromRequest.fold(
      formWithErrors => Redirect(routes.Administrator.notification).flashing("notification-alarm-create-error" -> Messages("app.global.error.missing.field.form")(Lang("fr"))),
      value => {
        try {
        	if (Notification.add(value._1, value._2, value._3)) {
        		Redirect(routes.Administrator.notification)
        	} else {
        		Redirect(routes.Administrator.notification).flashing("notification-alarm-create-error" -> Messages("app.global.error")(Lang("fr")))
        	}
        } catch {
        	case e => {
        		Logger.error(e.getMessage(), e)
        		Redirect(routes.Administrator.notification).flashing("notification-alarm-create-error" -> e.getMessage())
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
      if (FileUtils.delete(value(0), Configuration.getPhotoUploadThumbnailDirectory)) {
      	if (FileUtils.delete(value(0), Configuration.getPhotoUploadStandardDirectory)) {
      		Ok(Json.obj("status" -> "success"))
      	} else {
      		Ok(Json.obj("status" -> "failed", "error-message" -> addFadOutMessageToEnd(Messages("administrator.delete.to.upload.directory.photo.failed", value(0))(Lang("fr")))))
      	}  
      } else {
        Ok(Json.obj("status" -> "failed", "error-message" -> addFadOutMessageToEnd(Messages("administrator.delete.to.upload.directory.photo.failed", value(0))(Lang("fr")))))
      }
    } catch {
      case e: Throwable => {
        Logger.error(e.getMessage(), e) 
        Ok(Json.obj("status" -> "failed", "error-message" -> addFadOutMessageToEnd(Messages("administrator.delete.to.upload.directory.photo.failed", value(0))(Lang("fr")))))
      }      
    }
  }
  
   def deleteVideoToUploadDirectory = withAdmin { username => implicit request =>
    val value = request.body.asFormUrlEncoded.get("filename-post")
    Logger.info("delete video {} to upload server directory", value)
    try {
      if (FileUtils.delete(value(0), Configuration.getMediaVideoFolderUploadDirectory)) {
        Ok(Json.obj("status" -> "success"))
      } else {
      	Ok(Json.obj("status" -> "failed", "error-message" -> addFadOutMessageToEnd(Messages("administrator.delete.to.upload.directory.video.failed", value(0))(Lang("fr")))))
      }  
    } catch {
      case e: Throwable => {
        Logger.error(e.getMessage(), e) 
        Ok(Json.obj("status" -> "failed", "error-message" -> addFadOutMessageToEnd(Messages("administrator.delete.to.upload.directory.video.failed", value(0))(Lang("fr")))))
      }      
    }
  }
   
  private def addFadOutMessageToEnd(message: String) = message + Messages("page.main.message.popup.fadeOut")(Lang("fr"))
}