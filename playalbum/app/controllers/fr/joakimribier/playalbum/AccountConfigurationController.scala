package controllers.fr.joakimribier.playalbum

import org.slf4j.LoggerFactory
import models.fr.joakimribier.playalbum.UserTemplate
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Security
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
object AccountConfigurationController extends Controller {

  private val Logger = LoggerFactory.getLogger("AccountConfigurationController")
  private val _TITLE_HTML: String = ConfigurationUtils.getHTMLTitle()
  
  def index = Action { implicit request =>
    val username = request.session.get(Security.username)
    if (username.isDefined) {
    	val userTemplate = new UserTemplate(username.get, request.session.get(ConfigurationUtils._SESSION_EMAIL_KEY))
    	if (request.flash.get("validation-message").isDefined) {
    		Ok(views.html.fr.joakimribier.playalbum.accountConfiguration(
    		    _TITLE_HTML, null, userTemplate,
    		    request.flash.get("succeed"), request.flash.get("validation-message")))
    	} else {
    		Ok(views.html.fr.joakimribier.playalbum.accountConfiguration(
    		    _TITLE_HTML, null, userTemplate, Option.empty, Option.empty))
    	}
    } else {
    	Redirect(routes.AuthenticationController.logout)
    }
  }
}