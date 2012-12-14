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
import utils.Configuration
import views.html
import scala.collection.immutable.Seq
import scala.collection.immutable.Nil
import utils.FileUtils

object Administrator extends Controller with Secured {

  private val _TITLE_HTML: String = Configuration.getHTMLTitle()
  private val addNewPhotoForm = Form (
    tuple (
      "filename" -> text,
      "title" -> text.verifying(Constraints.maxLength(25)),
      "description" -> optional(text),
      "public" -> boolean,
      "tags" -> list(text)
    ) verifying ("Impossible de sauvegarder la photo, veuillez vérifier le formulaire et recommencer.", result => result match {
      case (filename, title, description, public, tags) => Photo.create(filename, title, description, public, tags)
    })
  )
  
  def index = withAuth { username => implicit request =>
    if (Application.isAdmin(username)) {
      Redirect(routes.Administrator.listPhotoUploaded)
    } else {
      Redirect(routes.Application.index)
    }
  }
  
  def savePhoto = withAuth { username => implicit request =>
    if (Application.isAdmin(username)) {
      
      addNewPhotoForm.bindFromRequest.fold(
        // Form has errors, redisplay it
        formWithErrors => BadRequest(html.adminAddPhoto(_TITLE_HTML, null, username, formWithErrors, Tag.list())),
        // We got a valid User value
        value =>  {
          val files: List[String] = FileUtils.listFilename(Configuration.getPhotoUploadThumbnailDirectory())
          Ok(views.html.adminListPhoto(_TITLE_HTML, null, username, Tag.list(), files))
        }
     )
     
    } else {
      Redirect(routes.Authentication.logout)
    }
  }
  
  def upload = withAuth { username => implicit request =>
    if (Application.isAdmin(username)) {
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
    } else {
      Redirect(routes.Authentication.logout)
    }
  }
  
  def listPhotoUploaded = withAuth { username => implicit request =>
    if (Application.isAdmin(username)) {
      val files: List[String] = FileUtils.listFilename(Configuration.getPhotoUploadThumbnailDirectory())
      Ok(views.html.adminListPhoto(_TITLE_HTML, null, username, Tag.list(), files))
    } else {
      Redirect(routes.Application.index)
    }
  }
  
  def addNewPhoto(name: String) = withAuth { username => implicit request =>
    if (Application.isAdmin(username)) {
      val formFilled = addNewPhotoForm.fill(name, "", Option.empty, false, List("Kazakhstan"))
      Ok(views.html.adminAddPhoto(_TITLE_HTML, null, username, formFilled, Tag.list()))  
    } else {
      Redirect(routes.Application.index)
    }
  }
}