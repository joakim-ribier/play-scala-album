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
import models.Notification
import play.api.libs.json.Json

object Administrator extends Controller with Secured {

  private val _TITLE_HTML: String = Configuration.getHTMLTitle()
  private val addNewPhotoForm = Form (
    tuple (
      "filename" -> text,
      "title" -> text.verifying(Constraints.maxLength(25)),
      "description" -> optional(text),
      "public" -> boolean,
      "tags" -> list(text)
    ) verifying (Messages("administrator.add.new.photo.verifying.text")(Lang("fr")), result => result match {
      case (filename, title, description, public, tags) => Media.create(filename, title, description, public, tags)
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
  
  def savePhoto = withAdmin { username => implicit request =>
    val userTemplate = Authentication.userTemplate(username, request.session)
    addNewPhotoForm.bindFromRequest.fold(
      // Form has errors, redisplay it
      formWithErrors => BadRequest(html.adminAddPhoto(_TITLE_HTML, null, userTemplate, formWithErrors, Tag.list())),
      // We got a valid User value
      value =>  {
        val files: List[String] = FileUtils.listFilename(Configuration.getPhotoUploadThumbnailDirectory())
        Ok(views.html.adminListPhoto(_TITLE_HTML, null, userTemplate, Tag.list(), files))
      }
   )
  }
  
  def upload = withAdmin { username => implicit request =>
    val files = request.body.asMultipartFormData.get.files.seq
    for (image <- files) {
      val filename = image.filename
      val contentType = image.contentType
      val fileType: String = "." + FileUtils.getFileType(filename)
      val newFileName = "_" + DateTime.now().getMillis() + fileType
    
      image.ref.moveTo(new File(Configuration.getPhotoUploadStandardDirectory() + newFileName))

      FileUtils.createThumbnails(
          Configuration.getPhotoUploadStandardDirectory(),
          Configuration.getPhotoUploadThumbnailDirectory(), newFileName, 200, 150)
    }
    Ok.as("success")
  }
  
  def listPhotoUploaded = withAdmin { username => implicit request =>
    val userTemplate = Authentication.userTemplate(username, request.session)
    val files: List[String] = FileUtils.listFilename(Configuration.getPhotoUploadThumbnailDirectory())
    Ok(views.html.adminListPhoto(_TITLE_HTML, null, userTemplate, Tag.list(), files))
  }
  
  def addNewPhoto(name: String) = withAdmin { username => implicit request =>
    val userTemplate = Authentication.userTemplate(username, request.session)
    val formFilled = addNewPhotoForm.fill(name, "", Option.empty, false, List(Configuration.getStringValue(Configuration._APP_TAG_DEFAULT)))
    Ok(views.html.adminAddPhoto(_TITLE_HTML, null, userTemplate, formFilled, Tag.list()))  
  }
  
  def notification = withAdmin { username => implicit request =>
    val userTemplate = Authentication.userTemplate(username, request.session)
    Ok(views.html.adminNotification(_TITLE_HTML, null, userTemplate, Tag.list(), Notification.listMessages(), Notification.list()))
  }
  
  def saveNewNotification = withAdmin { username => implicit request =>
    Logger.info("try to save new notification")
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
    Logger.info("try to save new notification alarm")
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
  	Logger.info("try to delete notification message")
    val value = request.body.asFormUrlEncoded.get("messageid-post")
    val result = Notification.removeMessage(value(0).toLong)
    if (result == 1) {
    	Ok(Json.toJson(Map("status" -> "success")))
    } else {
      Ok(Json.toJson(Map("status" -> "failed", "error-message" -> Messages("page.adminNotification.delete.message.failed")(Lang("fr")))))
    }
  }
  
  def deleteNotification = withAdmin { username => implicit request =>
  	Logger.info("try to delete notification")
    val value = request.body.asFormUrlEncoded.get("notificationid-post")
    val result = Notification.remove(value(0).toLong)
    if (result > 0) {
    	Ok(Json.toJson(Map("status" -> "success")))
    } else {
      Ok(Json.toJson(Map("status" -> "failed", "error-message" -> Messages("page.adminNotification.delete.failed")(Lang("fr")))))
    }
  }
}