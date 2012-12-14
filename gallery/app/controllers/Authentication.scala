package controllers

import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import play.api._
import views._
import models._
import utils.Configuration

object Authentication extends Controller {

  private val _TITLE_HTML: String = Configuration.getHTMLTitle()
  
  val form = Form (
    tuple (
      "login" -> text,
      "password" -> text,
      "code-access" -> optional(text)
    ) verifying ("Connexion impossible, vérifiez votre identifiant et / ou mot de passe.", result => result match {
      case (login, password, codeAccess) => User.authenticate(login, password, codeAccess)
    })
  )
  
  def login = Action { request =>
    request.session.get("user").map { user =>
      Redirect("/album")
    }.getOrElse {
      Ok(views.html.login(form, _TITLE_HTML, null))
    }
  }
  
  def redirect = Action { request =>
    Redirect("/album")
  }
  
  def authenticate = Action { implicit request =>
    form.bindFromRequest.fold(
      // Form has errors, redisplay it
      formWithErrors => BadRequest(html.login(formWithErrors, _TITLE_HTML, null)),
      // We got a valid User value
      value => Redirect(routes.Application.index).withSession("user" -> value._1)
    )
  }
  
  def logout = Action {
    Redirect(routes.Application.index).withNewSession.flashing(
      "success" -> "You've been logged out"
    )
  }
}