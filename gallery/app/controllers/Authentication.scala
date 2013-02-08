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
import utils.TokenUtils
import org.slf4j.LoggerFactory

object Authentication extends Controller {
  
  private val Logger = LoggerFactory.getLogger("Authentication")
  private val _TITLE_HTML: String = Configuration.getHTMLTitle()
  
  val form = Form (
    tuple (
      "login" -> text,
      "password" -> text,
      "code-access" -> optional(text),
      "email" -> optional(text),
      "token" -> optional(text)
    ) verifying (Messages("authentication.login.verifying.text")(Lang("fr")), result => result match {
      case (login, password, codeAccess, email, token) => User.authenticate(login, password, codeAccess)
    })
  )
  
  def login = Action { implicit request =>
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
  			var email = UserEmail.getFromLogin(username)
  			if (!email.isDefined) {
  			  email = Option.apply("nothing")
  			}
  			
      	var message = "nothing"
			  if (value._4.isDefined && value._5.isDefined) {
			    if (email.get.equals("nothing")) {
			    	val tokenTo = TokenUtils.validationAddressMail(username, value._4.get)
	    			if (tokenTo.equals(value._5.get)) {
	    				val user = User.findUser(username)
  						if (user.isDefined && User.setAddressMail(user.get, value._4.get)) {
  							email = value._4
  							message = Messages("application.create.new.user.email.success.html", email.get)(Lang("fr"))
  						} else {
  						  message = Messages("application.create.new.user.email.failed.html", value._4.get)(Lang("fr"))
  						}
	    			} else {
	    			  message = Messages("application.create.new.user.email.failed.html", value._4.get)(Lang("fr"))
	    			}  
			    } else {
			      message = Messages("application.create.new.user.email.exists.html", email.get)(Lang("fr"))
			    }
  			}
      	
      	MDCUtils.getOrOpenSession(username, sessionId)
      	Logger.info("You've been logged in")
      	if (message.equals("nothing")) {
      	  redirectToIndex(username, sessionId, email.get)(request)
      	} else {
      	  redirectToIndex(username, sessionId, email.get, message)(request)
      	}
      }
    )
  }
  
  private def redirectToIndex(username: String, sessionId: String, email: String) = Action {
  	Redirect(routes.Application.index).withSession(
            Security.username -> username,
            Configuration._SESSION_ID_KEY -> sessionId,
            Configuration._SESSION_EMAIL_KEY -> email).flashing("connection" -> "success")
  }
  
  private def redirectToIndex(username: String, sessionId: String, email: String, message: String) = Action {
  	Redirect(routes.Application.index).withSession(
            Security.username -> username,
            Configuration._SESSION_ID_KEY -> sessionId,
            Configuration._SESSION_EMAIL_KEY -> email).flashing("connection" -> "success", "app-message" -> message)
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
  
  def userTemplate(username: String, session: Session) : UserTemplate = {
    return new UserTemplate(username, session.get(Configuration._SESSION_EMAIL_KEY))
  }
}