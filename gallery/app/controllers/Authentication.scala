package controllers

import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import play.api._
import views._
import models._
import utils.Configuration
import org.slf4j.MDC
import java.util.Calendar
import java.text.SimpleDateFormat
import utils.MDCUtils

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
        val sessionId = generateSessionId(value._1)
        MDCUtils.getOrOpenSession(value._1, sessionId)
        
        Logger.info("You've been logged in")
        
        Redirect(routes.Application.index).withSession(
            Security.username -> value._1,
            "sessionId" -> sessionId)
      }
    )
  }
  
  def redirectAuthenticate = Action {
  	Redirect(routes.Application.index)
  }
  
  def logout = Action { request =>
    Logger.info("You've been logged out")
    MDCUtils.closeSession()
    Redirect(routes.Authentication.login).withNewSession
  }

  private def generateSessionId(username: String) : String = {
  	val date: Calendar = Calendar.getInstance();
		val dateformatter: SimpleDateFormat  = new SimpleDateFormat("yyyy.MM.dd_hh:mm:ss")
		val now: String = dateformatter.format(date.getTime())
		return username + "-" + now 
  }
}