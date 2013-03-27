package controllers

import play.api.mvc.Controller
import utils.ConfigurationUtils
import models.UserTemplate
import models.Media
import org.slf4j.LoggerFactory
import play.api.i18n.Messages
import play.api.i18n.Lang
import play.api.mvc.Action
import play.api.mvc.RequestHeader
import play.api.libs.json.Json
import models.User
import play.api.mvc.Security

object AccountConfigurationController extends Controller {

  private val Logger = LoggerFactory.getLogger("AccountConfigurationController")
  private val _TITLE_HTML: String = ConfigurationUtils.getHTMLTitle()
  
  def index = Action { implicit request =>
    val username = request.session.get(Security.username)
    if (username.isDefined) {
    	val userTemplate = new UserTemplate(username.get, request.session.get(ConfigurationUtils._SESSION_EMAIL_KEY))
    	if (request.flash.get("validation-message").isDefined) {
    		Ok(views.html.accountConfiguration(
    		    _TITLE_HTML, null, userTemplate,
    		    request.flash.get("succeed"), request.flash.get("validation-message")))
    	} else {
    		Ok(views.html.accountConfiguration(
    		    _TITLE_HTML, null, userTemplate, Option.empty, Option.empty))
    	}
    } else {
    	Redirect(routes.AuthenticationController.logout)
    }
  }
}