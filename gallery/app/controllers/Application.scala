package controllers

import models.Feedback
import models.FeedbackClass
import models.Media
import models.Tag
import models.User
import models.UserTemplate
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.text
import play.api.data.Forms.tuple
import play.api.i18n.Lang
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Result
import utils.Configuration
import utils.FileUtils
import views.html
import models.notification.Notification
import org.slf4j.LoggerFactory
import models.post.Post

object Application extends Controller with Secured {

  private val Logger = LoggerFactory.getLogger("Application")
  
  private val _TITLE_HTML: String = Configuration.getHTMLTitle()
  private val _LIMIT = Configuration.getDisplayPhotoLimit()
  
  private val _TAG_ALL = "all"
  private val _TAG_SEPARATOR = "\\."
    
  private val formNewAdmin = Form (
    tuple (
      "login" -> text,
      "password" -> text,
      "email" -> text
    ) verifying (Messages("application.add.new.admin.verifying.text")(Lang("fr")), result => result match {
      case (login, password, email) => User.createUser(login, password, email)
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
    
    if (countPage == 0) {
      countPage = 1
    }
    return countPage
  }
  
  def index = withAuth { username => implicit request =>
    val userTemplate = new UserTemplate(username, request.session.get(Configuration._SESSION_EMAIL_KEY))
  	if (User.isAdmin(username)) {
  		Redirect(routes.Administrator.index)
  	} else {
  	  val medias = Media.list(0, _LIMIT)
  	  val numberCommentsByMedia = Post.count(medias)
  	  if (request.flash.get("app-message").isDefined) {
  	    val feedback = new Feedback(request.flash.get("app-message").get, FeedbackClass.ok)
  	    Ok(views.html.index(_TITLE_HTML, feedback, userTemplate, Tag.list(), medias, numberCommentsByMedia, 1, _TAG_ALL, countPage(Media.total()), Notification.listNotClosedByUser(username)))
  	  } else {
  	  	Ok(views.html.index(_TITLE_HTML, null, userTemplate, Tag.list(), medias, numberCommentsByMedia, 1, _TAG_ALL, countPage(Media.total()), Notification.listNotClosedByUser(username)))
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
          val medias = Media.list((pageToInt-1)*_LIMIT, _LIMIT)
          val numberCommentsByMedia = Post.count(medias)
          Ok(views.html.index(_TITLE_HTML, null, userTemplate, Tag.list(), medias, numberCommentsByMedia, pageToInt, _TAG_ALL, countPage(Media.total()), Notification.listNotClosedByUser(username)))
        } else {
          val mediaIds: Seq[Long] = Tag.list(tagsSeq)
          val medias = Media.list(mediaIds, ((pageToInt-1)*_LIMIT), _LIMIT)
          val numberCommentsByMedia = Post.count(medias)
          Ok(views.html.index(_TITLE_HTML, null, userTemplate, Tag.list(), medias, numberCommentsByMedia, pageToInt, tags, countPage(mediaIds.size), Notification.listNotClosedByUser(username)))
        }
        
      } else {
        
        Redirect(routes.Application.index)
      }
      
    } catch {
      case e: Throwable => {
       Logger.error(e.getMessage(), e) 
       Redirect(routes.Application.index)
      }
    }
  }
  
  def getPreviousPhoto(id: String, tags: String) = withUser { username => implicit request =>
    try {
      val mediaId = id.asInstanceOf[String].toInt
      val tagsSeq = tags.split(_TAG_SEPARATOR).toList
      if (tagsSeq.size == 1 && tagsSeq(0) == _TAG_ALL) {
        val media = Media.getPreviousPhoto(mediaId)
        toJSON(media, isPreviousPhoto(media, List()))
      } else {
        val mediaIds: Seq[Long] = Tag.list(tagsSeq)
        val media = Media.getPreviousPhoto(mediaId, mediaIds)
        toJSON(media, isPreviousPhoto(media, mediaIds))
      }
    } catch {
      case e: Throwable => {
        Logger.error(e.getMessage(), e) 
        Ok(Json.obj("status" -> "failed"))
      }
    }
  }
  
  def getNextPhoto(id: String, tags: String) = withUser { username => implicit request =>
    try {
      val mediaId = id.asInstanceOf[String].toInt
      val tagsSeq = tags.split(_TAG_SEPARATOR).toList
      if (tagsSeq.size == 1 && tagsSeq(0) == _TAG_ALL) {
        val media = Media.getNextPhoto(mediaId)
        toJSON(media, isNextPhoto(media, List()))
      } else {
        val mediaIds: Seq[Long] = Tag.list(tagsSeq)
        val media = Media.getNextPhoto(mediaId, mediaIds)
        toJSON(media, isNextPhoto(media, mediaIds))
      }
    } catch {
      case e: Throwable => {
        Logger.error(e.getMessage(), e) 
        Ok(Json.obj("status" -> "failed"))
      }
    }
  }
  
  private def isNextPhoto(media: Media, mediaIds: Seq[Long]) : Boolean = {
    if (media != null) {
      if (mediaIds.isEmpty) {
        return Media.getNextPhoto(media.id.get) != null
      } else {
        return Media.getNextPhoto(media.id.get, mediaIds) != null
      }
    }
    return false
  }
  
  private def isPreviousPhoto(media: Media, mediaIds: Seq[Long]) : Boolean = {
    if (media != null) {
      if (mediaIds.isEmpty) {
        return Media.getPreviousPhoto(media.id.get) != null
      } else {
        return Media.getPreviousPhoto(media.id.get, mediaIds) != null
      }
    }
    return false
  }
  
  private def toJSON(media: Media, is: Boolean) : Result = {
    if (media != null) {
      var desc = ""
      if (media.description.isDefined) {
        desc = media.description.get
      }
      Ok(Json.obj(
          		"status" -> "success",
              "id" -> String.valueOf(media.id),
              "filename" -> media.filename,
              "mediaType" -> media.mediaType.label,
              "title" -> media.title,
              "desc" -> desc,
              "is" -> String.valueOf(is)))
    } else {
      Ok(Json.obj("status" -> "nothing"))
    }
  }
  
  def configuration = Action { implicit request =>
    val u = User.findUser(Option.apply(Configuration.getAdminLogin()))
    if (u.isDefined) {
       Redirect(routes.Application.index)
    } else {
      val filledForm = formNewAdmin.fill(Configuration.getAdminLogin(), null, null)
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
    val form = Authentication.form.fill("nothing", "nothing", Option.empty, Option.apply(email), Option.apply(token), Option.empty)
  	Ok(views.html.login(
  	    form, _TITLE_HTML,
  	    new Feedback(Messages("application.create.new.user.email.redirection.to.login", email)(Lang("fr")), FeedbackClass.ok))).withNewSession
  }
  
  def getPhotoInUploadThumbailDirectory(filename: String) = withAuth { username => implicit request =>
    getFile(Configuration.getPhotoUploadThumbnailDirectory(), filename)(request)
  }
   
  def getPhotoInStandardDirectory(filename: String) = withAuth { username => implicit request =>
    getFile(Configuration.getPhotoStandardDirectory(), filename)(request)
  }
  
  def getPhotoInThumbailDirectory(filename: String) = Action { implicit request =>
    getFile(Configuration.getPhotoThumbnailDirectory(), filename)(request)
  }
  
  def getPhotoIn800x600Directory(filename: String) = withAuth { username => implicit request =>
    getFile(Configuration.getPhoto800x600Directory(), filename)(request)
  }
  
  def userPopupNotifyClose = withUser { username => implicit request =>
  	val notificationId = request.body.asFormUrlEncoded.get("notificationid-post")
  	Logger.info("user closed notification popup { " + notificationId + " }")
  	if (!notificationId.isEmpty) {
  	  Notification.setClosed(username, notificationId(0).toLong)
  	}
  	Ok("return")
  }
  
  def getVideoInStandardDirectory(file: String) = withAuth { username => implicit request =>
    getFile(Configuration.getMediaVideoFolderStandardDirectory(), file)(request)
  }
  
  def getVideoInUploadDirectory(file: String) = withAuth { username => implicit request =>
    getFile(Configuration.getMediaVideoFolderUploadDirectory(), file)(request)
  }
  
  def getStringPostValueFromKey = Action { implicit request =>
  	val keyTab = request.body.asFormUrlEncoded.get("configuration-key")
  	if (!keyTab.isEmpty) {
  	  val value = Configuration.getStringValue(keyTab(0).toString())
  	  if (!value.isEmpty()) {
  	  	Ok(Json.obj("status" -> "success", "value" -> value))
  	  } else {
  	    Ok(Json.obj("status" -> "nothing"))
  	  }
  	} else {
  	  Ok(Json.obj("status" -> "nothing"))
  	}
  }
}