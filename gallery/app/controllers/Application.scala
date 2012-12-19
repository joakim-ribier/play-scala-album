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

object Application extends Controller with Secured {

  private val _TITLE_HTML: String = Configuration.getHTMLTitle()
  private val _LIMIT = Configuration.getDisplayPhotoLimit()
  
  private val _TAG_ALL = "all"
  private val _TAG_SEPARATOR = "\\."
    
  private val formNewAdmin = Form (
    tuple (
      "login" -> text,
      "password" -> text
    ) verifying ("Configuration impossible, vérifiez votre identifiant et / ou mot de passe administrateur.", result => result match {
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
  	  Ok(views.html.index(_TITLE_HTML, null, userTemplate, Tag.list(), Photo.list(0, _LIMIT), 1, _TAG_ALL, countPage(Photo.total())))
  	}
  }
  
  def page(page: String, tags: String) = withUser { username => implicit request =>
    try {
      val userTemplate = new UserTemplate(username, request.session.get(Configuration._SESSION_EMAIL_KEY))
       
      val pageToInt = page.asInstanceOf[String].toInt
      if (pageToInt >= 1) {
        
        val tagsSeq = tags.split(_TAG_SEPARATOR).toList
        if (tagsSeq.size == 1 && tagsSeq(0) == _TAG_ALL) {
          Ok(views.html.index(_TITLE_HTML, null, userTemplate, Tag.list(), Photo.list((pageToInt-1)*_LIMIT, _LIMIT), pageToInt, _TAG_ALL, countPage(Photo.total())))
        } else {
          val photosId: Seq[Long] = Tag.list(tagsSeq)
          Ok(views.html.index(_TITLE_HTML, null, userTemplate, Tag.list(), Photo.list(photosId, ((pageToInt-1)*_LIMIT), _LIMIT), pageToInt, tags, countPage(tagsSeq.size)))
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
  
  def configuration = Action {
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
      value => Ok(views.html.login(Authentication.form, _TITLE_HTML, new Feedback("Administrateur créé", FeedbackClass.ok)))
    )
  }
  
  def saveNewUserEmail(email: String, token: String) = withUser { username => implicit request =>
  	var feedBack = new Feedback("Erreur de validation de l'adresse mail, veuillez recommencer ou contacter l'administrateur de l'application.", FeedbackClass.ko)
    try {
      val userEmailExist = UserEmail.getFromLogin(username)
    	if (!userEmailExist.isDefined) {

    	  val tokenTo = TokenUtils.validationAddressMail(username, email)
	    	if (tokenTo.equals(token)) {
	    		val user = User.findUser(username)
	    		if (user.isDefined && User.setAddressMail(user.get, email)) {
	    		  feedBack = new Feedback("Validation de l'adresse mail [ " + email + " ] pour l'utilisateur [ " + username + " ].", FeedbackClass.ok)
	    		}
	    	}  
    	} else {
    		feedBack = new Feedback("L'utilisateur [ " +  username + " ] a déjà une adresse mail associée.", FeedbackClass.ok)
    	}
      
    	Ok(views.html.login(Authentication.form, _TITLE_HTML, feedBack)).withNewSession
    } catch {
      case e => {
        Logger.error(e.getMessage(), e)
        Ok(views.html.login(Authentication.form, _TITLE_HTML, feedBack)).withNewSession 
      }
    }
  }
  
  def getPhotoInUploadThumbailDirectory(photo: String) = withAuth { username => implicit request =>
    getFile(Configuration.getPhotoUploadThumbnailDirectory(), photo)(request)
  }
   
  def getPhotoInStandardDirectory(photo: String) = withAuth { username => implicit request =>
    getFile(Configuration.getPhotoStandardDirectory(), photo)(request)
  }
  
  def getPhotoInThumbailDirectory(photo: String) = withAuth { username => implicit request =>
    getFile(Configuration.getPhotoThumbnailDirectory(), photo)(request)
  }
  
  def getPhotoIn800x600Directory(photo: String) = withAuth { username => implicit request =>
    getFile(Configuration.getPhoto800x600Directory(), photo)(request)
  }
}