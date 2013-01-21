package controllers

import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import play.api._
import views._
import models._
import java.io.File
import java.io.IOException
import play.Play
import play.api.libs.json.Json
import utils.Configuration
import utils.FileUtils
import utils.TokenUtils
import play.api.i18n.Messages
import play.api.i18n.Lang

object Application extends Controller with Secured {

  private val _TITLE_HTML: String = Configuration.getHTMLTitle()
  private val _LIMIT = Configuration.getDisplayPhotoLimit()
  
  private val _TAG_ALL = "all"
  private val _TAG_SEPARATOR = "\\."
    
  private val formNewAdmin = Form (
    tuple (
      "login" -> text,
      "password" -> text
    ) verifying (Messages("application.add.new.admin.verifying.text")(Lang("fr")), result => result match {
      case (login, password) => User.createUser(login, password)
    })
  )
  
  private def getFile(dir: String, name: String) = Action {
	  try {
	  	val file = FileUtils.getFile(dir, name)
	  	Ok.sendFile(file)
	  } catch {
	    case _ => NotFound
	  }
  }
  
  private def countPage(count: Long) : Long = {
    var countPage = count / _LIMIT
    var modulo = count % _LIMIT
    if (modulo > 0) {
      countPage = countPage + 1
    }
    countPage
  }
  
  def index = withAuth { username => implicit request =>
    val userTemplate = new UserTemplate(username, request.session.get(Configuration._SESSION_EMAIL_KEY))
  	if (User.isAdmin(username)) {
  		Redirect(routes.Administrator.index)
  	} else {
  	  if (request.flash.get("app-message").isDefined) {
  	    val feedback = new Feedback(request.flash.get("app-message").get, FeedbackClass.ok)
  	    Ok(views.html.index(_TITLE_HTML, feedback, userTemplate, Tag.list(), Photo.list(0, _LIMIT), 1, _TAG_ALL, countPage(Photo.total()), Notification.listNotClosedByUser(username)))
  	  } else {
  	  	Ok(views.html.index(_TITLE_HTML, null, userTemplate, Tag.list(), Photo.list(0, _LIMIT), 1, _TAG_ALL, countPage(Photo.total()), Notification.listNotClosedByUser(username)))
  	  }
  	}
  }
  
  def page(page: String, tags: String) = withUser { username => implicit request =>
    try {
      val userTemplate = new UserTemplate(username, request.session.get(Configuration._SESSION_EMAIL_KEY))
       
      val pageToInt = page.asInstanceOf[String].toInt
      if (pageToInt >= 1) {
        
        val tagsSeq = tags.split(_TAG_SEPARATOR).toList
        if (tagsSeq.size == 1 && tagsSeq(0) == _TAG_ALL) {
          Ok(views.html.index(_TITLE_HTML, null, userTemplate, Tag.list(), Photo.list((pageToInt-1)*_LIMIT, _LIMIT), pageToInt, _TAG_ALL, countPage(Photo.total()), Notification.listNotClosedByUser(username)))
        } else {
          val photosId: Seq[Long] = Tag.list(tagsSeq)
          Ok(views.html.index(_TITLE_HTML, null, userTemplate, Tag.list(), Photo.list(photosId, ((pageToInt-1)*_LIMIT), _LIMIT), pageToInt, tags, countPage(photosId.size), Notification.listNotClosedByUser(username)))
        }
        
      } else {
        
        Redirect(routes.Application.index)
      }
      
    } catch {
      case _ => Redirect(routes.Application.index)
    }
  }
  
  def getPreviousPhoto(id: String, tags: String) = withUser { username => implicit request =>
    try {
      val photoId = id.asInstanceOf[String].toInt
      val tagsSeq = tags.split(_TAG_SEPARATOR).toList
      if (tagsSeq.size == 1 && tagsSeq(0) == _TAG_ALL) {
        toJSON(Photo.getPreviousPhoto(photoId))
      } else {
        val photosId: Seq[Long] = Tag.list(tagsSeq)
        toJSON(Photo.getPreviousPhoto(photoId, photosId))
      }
    } catch {
      case _ =>  Ok(Json.toJson(Map("status" -> "failed")))
    }
  }
  
  def getNextPhoto(id: String, tags: String) = withUser { username => implicit request =>
    try {
      val photoId = id.asInstanceOf[String].toInt
      val tagsSeq = tags.split(_TAG_SEPARATOR).toList
      if (tagsSeq.size == 1 && tagsSeq(0) == _TAG_ALL) {
        toJSON(Photo.getNextPhoto(photoId))
      } else {
        val photosId: Seq[Long] = Tag.list(tagsSeq)
        toJSON(Photo.getNextPhoto(photoId, photosId))
      }
    } catch {
      case _ =>  Ok(Json.toJson(Map("status" -> "failed")))
    }
  }
  
  private def toJSON(photo: Photo) : Result = {
    if (photo != null) {
      var desc = ""
      if (photo.description.isDefined) {
        desc = photo.description.get
      }
      Ok(Json.toJson(
          Map("status" -> "success",
              "id" -> String.valueOf(photo.id),
              "filename" -> photo.filename,
              "title" -> photo.title,
              "desc" -> desc)))
    } else {
      Ok(Json.toJson(Map("status" -> "nothing")))
    }
  }
  
  def configuration = Action { implicit request =>
    val u = User.findUser(Configuration.getAdminLogin())
    if (u.isDefined) {
       Redirect(routes.Application.index)
    } else {
      val filledForm = formNewAdmin.fill(Configuration.getAdminLogin(), null)
      Ok(views.html.configuration(filledForm, _TITLE_HTML))   
    }
  }
  
  def createAdministrator = Action { implicit request =>
    formNewAdmin.bindFromRequest.fold(
      // Form has errors, redisplay it
      formWithErrors => BadRequest(html.configuration(formWithErrors, _TITLE_HTML)),
      // We got a valid User value
      value => {
        val successText = Messages("application.add.new.admin.success.html", value._1)(Lang("fr"))
        Ok(views.html.login(Authentication.form, _TITLE_HTML, new Feedback(successText, FeedbackClass.ok))) 
      }
    )
  }

  def saveNewUserEmail(email: String, token: String) = Action { implicit request =>
    val form = Authentication.form.fill("nothing", "nothing", Option.empty, Option.apply(email), Option.apply(token))
  	Ok(views.html.login(
  	    form, _TITLE_HTML,
  	    new Feedback(Messages("application.create.new.user.email.redirection.to.login", email)(Lang("fr")), FeedbackClass.ok))).withNewSession
  }
  
  def getPhotoInUploadThumbailDirectory(photo: String) = withAuth { username => implicit request =>
    getFile(Configuration.getPhotoUploadThumbnailDirectory(), photo)(request)
  }
   
  def getPhotoInStandardDirectory(photo: String) = withAuth { username => implicit request =>
    getFile(Configuration.getPhotoStandardDirectory(), photo)(request)
  }
  
  def getPhotoInThumbailDirectory(photo: String) = Action { implicit request =>
    getFile(Configuration.getPhotoThumbnailDirectory(), photo)(request)
  }
  
  def getPhotoIn800x600Directory(photo: String) = withAuth { username => implicit request =>
    getFile(Configuration.getPhoto800x600Directory(), photo)(request)
  }
  
  def userPopupNotifyClose = withUser { username => implicit request =>
  	val notificationId = request.body.asFormUrlEncoded.get("notificationid-post")
  	Logger.info("user closed notification popup { " + notificationId + " }")
  	if (!notificationId.isEmpty) {
  	  Notification.setClosed(username, notificationId(0).toLong)
  	}
  	Ok("return")
  }
}