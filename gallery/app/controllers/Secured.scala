package controllers

import models.User
import play.api.mvc.Request
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.RequestHeader
import play.api.mvc.Result
import play.api.mvc.Results
import play.api.mvc.Security

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