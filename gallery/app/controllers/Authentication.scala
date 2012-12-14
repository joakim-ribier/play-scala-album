package controllers

import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import play.api._
import views._
import models._
import utils.Configuration

trait Secured {
  def username(request: RequestHeader) = request.session.get(Security.username)

  def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Authentication.logout)
  
  def userUnauthorized(request: RequestHeader) = Results.Redirect(routes.Authentication.logout)
  
  def adminUnauthorized(request: RequestHeader) = Results.Redirect(routes.Authentication.logout)

  def withAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }
  
  def withUser(f: String => Request[AnyContent] => Result) = withAuth { username => implicit request =>
    if (User.isAdmin(username)) {
    	userUnauthorized(request)
    } else {
    	f(username)(request)
    }
  }
  
  def withAdmin(f: String => Request[AnyContent] => Result) = withAuth { username => implicit request =>
    if (User.isAdmin(username)) {
    	f(username)(request)
    } else {
    	adminUnauthorized(request)
    }
  }
}

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
  	val u = User.findUser(Configuration.getAdminLogin())
    if (u.isDefined) {
    	Ok(views.html.login(form, _TITLE_HTML, null))
    } else {
    	Redirect(routes.Application.configuration)
    }
  }
  
  def redirect = Action { request =>
  	Redirect(routes.Application.configuration)
  }
  
  def authenticate = Action { implicit request =>
    form.bindFromRequest.fold(
      // Form has errors, redisplay it
      formWithErrors => BadRequest(html.login(formWithErrors, _TITLE_HTML, null)),
      // We got a valid User value
      value => {
        Redirect(routes.Application.index).withSession(Security.username -> value._1) 
      }
    )
  }
  
  def redirectAuthenticate = Action {
  	Redirect(routes.Application.index)
  }
  
  def logout = Action {
    Redirect(routes.Authentication.login).withNewSession.flashing(
      "success" -> "You've been logged out"
    )
  }
}