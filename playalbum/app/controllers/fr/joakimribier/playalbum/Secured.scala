package controllers.fr.joakimribier.playalbum

import org.joda.time.DateTime
import models.fr.joakimribier.playalbum.User
import play.api.i18n.Lang
import play.api.i18n.Messages
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.Request
import play.api.mvc.RequestHeader
import play.api.mvc.Result
import play.api.mvc.Results
import play.api.mvc.Security
import play.cache.Cache
import utils.fr.joakimribier.playalbum.DateTimeUtils
import utils.fr.joakimribier.playalbum.ConfigurationUtils

/**
 * 
 * Copyright 2013 Joakim Ribier
 * 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
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
         
        val sessionId = request.session.get(ConfigurationUtils.getSessionID)
         if (isExpired(sessionId)) {
           Results.Redirect(routes.AuthenticationController.logout
               ).flashing("app-message" -> Messages("app.global.message.secured.session.expired")(Lang("fr")))
         } else {
           Cache.set(sessionId.get + "-" + ConfigurationUtils.getSessionTimeoutID, DateTimeUtils.now)
           val email = request.session.get(ConfigurationUtils.getSessionEmailID)
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
      val value = Cache.get(sessionId.get + "-" + ConfigurationUtils.getSessionTimeoutID)
      if (value != null) {
      	val dateTime : DateTime = DateTimeUtils.convertToDateTime(Option.apply(value.toString()))
      	return !DateTimeUtils.isAfterNowMinusMinutes(Option.apply(dateTime), Option.apply(ConfigurationUtils.getSessionExpiredMinutesDuration))
      }
    }
    return true
  }
}