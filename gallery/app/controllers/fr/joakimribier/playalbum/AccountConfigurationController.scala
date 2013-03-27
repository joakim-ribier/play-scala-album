package controllers.fr.joakimribier.playalbum

import org.slf4j.LoggerFactory
import models.fr.joakimribier.playalbum.UserTemplate
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Security
import utils.fr.joakimribier.playalbum.ConfigurationUtils

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