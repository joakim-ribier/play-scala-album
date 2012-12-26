package controllers

import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import play.api._
import views._
import models._
import org.slf4j.MDC
import java.util.Calendar
import java.text.SimpleDateFormat
import utils.MDCUtils
import utils.Configuration
import play.api.i18n.Messages
import play.api.i18n.Lang

object Authentication extends Controller {
  	
	private val _TITLE_HTML: String = Configuration.getHTMLTitle()
  
  val form = Form (
    tuple (
      "login" -> text,
      "password" -> text,
      "code-access" -> optional(text)
    ) verifying (Messages("authentication.login.verifying.text")(Lang("fr")), result => result match {
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
        val username = value._1
        val sessionId = generateSessionId(username)
        MDCUtils.getOrOpenSession(username, sessionId)
        
        val email = UserEmail.getFromLogin(username)
        
        Logger.info("You've been logged in")
        Redirect(routes.Application.index).withSession(
            Security.username -> username,
            Configuration._SESSION_ID_KEY -> sessionId,
            Configuration._SESSION_EMAIL_KEY -> formatSessionEmail(email))
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
  
  private def formatSessionEmail(email: Option[String]) : String = {
    var sessionEmail = "nothing"
    if (email.isDefined) {
      sessionEmail = email.get
    }
    return sessionEmail
  }
}