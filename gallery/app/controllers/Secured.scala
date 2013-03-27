package controllers

import models.User
import play.api.mvc.Request
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.RequestHeader
import play.api.mvc.Result
import play.api.mvc.Results
import play.api.mvc.Security
import org.slf4j.LoggerFactory
import play.api.Logger
import models.UserEmail
import utils.ConfigurationUtils
import org.joda.time.DateTime
import play.api.i18n.Messages
import play.api.i18n.Lang
import utils.DateTimeUtils
import play.cache.Cache

trait Secured {
  
  def username(request: RequestHeader) = request.session.get(Security.username)

  def onUnauthorized(request: RequestHeader) = {
    if (!request.path.equals("/album")) {
      Results.Redirect(routes.AuthenticationController.logout).flashing("redirect-url" -> request.path)
    } else {
      Results.Redirect(routes.AuthenticationController.logout)
    }
  }
  
  def userUnauthorized(request: RequestHeader) = Results.Redirect(routes.AuthenticationController.logout)
  
  def adminUnauthorized(request: RequestHeader) = Results.Redirect(routes.AuthenticationController.logout)

  def withAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
       Action { implicit request =>
         
        val sessionId = request.session.get(ConfigurationUtils._SESSION_ID_KEY)
         if (isExpired(sessionId)) {
           Results.Redirect(routes.AuthenticationController.logout
               ).flashing("app-message" -> Messages("app.global.message.secured.session.expired")(Lang("fr")))
         } else {
           Cache.set(sessionId.get + "-" + ConfigurationUtils._SESSION_TIMEOUT_KEY, DateTimeUtils.now)
           val email = request.session.get(ConfigurationUtils._SESSION_EMAIL_KEY) 
    			 if (email.isDefined && !email.get.equals("nothing")) {
    				 f(user)(request)
    			 } else {
    				 Results.Redirect(routes.AccountConfigurationController.index)
    			 }  
         }
      }
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
  
  private def isExpired(sessionId: Option[String]) : Boolean = {
    if (sessionId.isDefined) {
      val value = Cache.get(sessionId.get + "-" + ConfigurationUtils._SESSION_TIMEOUT_KEY)
      if (value != null) {
      	val dateTime : DateTime = DateTimeUtils.convertToDateTime(Option.apply(value.toString()))
      	return !DateTimeUtils.isAfterNowMinusMinutes(Option.apply(dateTime), Option.apply(ConfigurationUtils.sessionExpiredMinutes))
      }
    }
    return true
  }
}