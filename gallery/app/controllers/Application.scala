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
import utils.Configuration
import utils.FileUtils

object Application extends Controller {

  private val _TITLE_HTML: String = Configuration.getHTMLTitle()
  private val _LIMIT = Configuration.getDisplayPhotoLimit()
  
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
  
  def isAdmin(login: String) : Boolean = {
    return login.equals(Configuration.getAdminLogin())
  }

  
  // --> HTTP METHODS
  
  def index = Action { request =>
    request.session.get("user").map { user =>
      if (isAdmin(user)) {
         Redirect(routes.Administrator.index)
      } else {
        Ok(views.html.index(_TITLE_HTML, null, user, Tag.list(), Photo.list(0, _LIMIT), 1, "all", countPage(Photo.total())))
      }
    }.getOrElse {
      Redirect(routes.Application.configuration)
    }
  }
  
  def page(page: String, tags: String) = Action { request =>
    request.session.get("user").map { user =>
      if (isAdmin(user)) {
         Redirect(routes.Administrator.index)
      } else {
        
        try {
          
          val pageToInt = page.asInstanceOf[String].toInt
          if (pageToInt >= 1) {
            
            val tagsSeq = tags.split("\\.").toList
            if (tagsSeq.size == 1 && tagsSeq(0) == "all") {
              Ok(views.html.index(_TITLE_HTML, null, user, Tag.list(), Photo.list((pageToInt-1)*_LIMIT, _LIMIT), pageToInt, "all", countPage(Photo.total())))
            } else {
              val photosId: Seq[Long] = Tag.list(tagsSeq)
        	  Ok(views.html.index(_TITLE_HTML, null, user, Tag.list(), Photo.list(photosId, ((pageToInt-1)*_LIMIT), _LIMIT), pageToInt, tags, countPage(tagsSeq.size)))
            }
            
          } else {
            
            Redirect(routes.Application.index)
          }
          
        } catch {
          case _ => Redirect(routes.Application.index)
        }
      }
    }.getOrElse {
      Redirect(routes.Application.configuration)
    }
  }
  
  def configuration = Action {
    val u = User.findUser(Configuration.getAdminLogin())
    if (u.isDefined) {
       Redirect("/login").withNewSession.flashing(
           "success" -> "You've been logged out")
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
  
  def getPhotoInUploadThumbailDirectory(photo: String) = Action {
    getFile(Configuration.getPhotoUploadThumbnailDirectory(), photo)
  }
   
  def getPhotoInStandardDirectory(photo: String) = Action {
    getFile(Configuration.getPhotoStandardDirectory(), photo)
  }
  
  def getPhotoInThumbailDirectory(photo: String) = Action {
    getFile(Configuration.getPhotoThumbnailDirectory(), photo)
  }
  
  def getPhotoIn800x600Directory(photo: String) = Action {
    getFile(Configuration.getPhoto800x600Directory(), photo)
  }
}