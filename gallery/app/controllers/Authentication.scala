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
import org.slf4j.LoggerFactory
import utils.EncoderUtils

object Authentication extends Controller {
  
  val Logger = LoggerFactory.getLogger("Authentication")
  private val _TITLE_HTML: String = Configuration.getHTMLTitle()
  
  val form = Form (
    tuple (
      "login" -> text,
      "password" -> text,
      "code-access" -> optional(text),
      "email" -> optional(text),
      "token" -> optional(text),
      "redirect-url" -> optional(text)
    ) verifying (Messages("authentication.login.verifying.text")(Lang("fr")), result => result match {
      case (login, password, codeAccess, email, token, redirectUrl) => User.authenticate(login, password, codeAccess)
    })
  )
  
  def login = Action { implicit request =>
  	val u = User.findUser(Option.apply(Configuration.getAdminLogin()))
    if (u.isDefined) {
    	if (request.flash.get("redirect-url").isDefined) {
    	  val fill = form.fill("nothing", "nothing", Option.empty, Option.empty, Option.empty, Option.apply(request.flash.get("redirect-url").get))
  	    val feedback = new Feedback(Messages("app.global.message.url.redirection.html")(Lang("fr")), FeedbackClass.ok)
  	    Ok(views.html.login(fill, _TITLE_HTML, feedback))
    	} else {
    	  Ok(views.html.login(form, _TITLE_HTML, null))
    	}
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
        
      	val formUsername = value._1
      	val formEmail = value._4
      	val formToken = value._5
      	val formRedirectUrl = value._6
      	
      	val sessionId = generateSessionId(formUsername)
      	MDCUtils.getOrOpenSession(formUsername, sessionId)
      	Logger.info("You've been logged in")
      	
      	if (formRedirectUrl.isDefined) {
          Logger.info("redirection to '{}' after authentication successfull", formRedirectUrl.get)
          connect(formRedirectUrl.get, formUsername, sessionId, Option.empty)(request)
      	} else {
      	  
      	  // validation account configuration e-mail
      	  if (formEmail.isDefined && formToken.isDefined) {
      	    checkAndValidAddressEmail(formUsername, formEmail.get, formToken.get, sessionId)(request)
      	 
      	  } else {
      	  	connect("/album", formUsername, sessionId, Option.empty)(request)

      	  }
        }
      }
    )
  }
  
  private def checkAndValidAddressEmail(username: String, emailValidation: String, tokenValidation: String, sessionId: String) = Action { implicit request =>
    val userEmail = UserEmail.getFromLogin(username)
    if (userEmail.isDefined) {
      connectToAccountConfigurationPage(username, sessionId,
          false, Messages("page.account.configuration.validation.email.already.exists")(Lang("fr")))(request)
          
    } else {
      val tokenTo = EncoderUtils.generateTokenForEmailValidation(username, emailValidation)
      if (tokenTo.equals(tokenValidation)) {
        val user = User.findUser(Option.apply(username))
        if (user.isDefined && User.setAddressMail(user.get, emailValidation)) {
          connectToAccountConfigurationPage(username, sessionId,
              true, Messages("page.account.configuration.validation.email.success")(Lang("fr")))(request)
              
        } else {
          connectToAccountConfigurationPage(username, sessionId,
              false, Messages("page.account.configuration.validation.email.failed", emailValidation)(Lang("fr")))(request)
              
        }
      } else {
        connectToAccountConfigurationPage(username, sessionId,
              false, Messages("page.account.configuration.validation.email.failed", emailValidation)(Lang("fr")))(request)
              
      }
    }
  }
  
  private def connectToAccountConfigurationPage(username: String, sessionId: String, succeed: Boolean, message: String) = Action {
    Redirect(routes.AccountConfigurationController.index).withSession(
        Security.username -> username,
    		Configuration._SESSION_ID_KEY -> sessionId,
    		Configuration._SESSION_EMAIL_KEY -> formatUserEmailToString(username)
    		).flashing(
    		    "connection" -> "success",
    		    "validation-message" -> message,
    		    "succeed" -> succeed.toString)
  }

  private def connect(url: String, username: String, sessionId: String, message: Option[String]) = Action {
    val email = UserEmail.getFromLogin(username)
    if (email.isDefined) {
    	if (message.isDefined) {
    		Redirect(url).withSession(
    				Security.username -> username,
    				Configuration._SESSION_ID_KEY -> sessionId,
    				Configuration._SESSION_EMAIL_KEY -> formatUserEmailToString(username)).flashing("connection" -> "success", "app-message" -> message.get)
    	} else {
    		Redirect(url).withSession(
    				Security.username -> username,
    				Configuration._SESSION_ID_KEY -> sessionId,
    				Configuration._SESSION_EMAIL_KEY -> formatUserEmailToString(username)).flashing("connection" -> "success")
    	}
    } else {
      Redirect(routes.AccountConfigurationController.index).withSession(
    				Security.username -> username,
    				Configuration._SESSION_ID_KEY -> sessionId,
    				Configuration._SESSION_EMAIL_KEY -> formatUserEmailToString(username)).flashing("connection" -> "success")
    }	
  }
  
  private def formatUserEmailToString(username: String) : String = {
    val email = UserEmail.getFromLogin(username)
  	if (!email.isDefined) {
  	  return "nothing"
  	}
    return email.get
  }
  
  def redirectAuthenticate = Action {
  	Redirect(routes.Application.index)
  }
  
  def logout = Action { request =>
    Logger.info("You've been logged out")
    MDCUtils.closeSession()
    if (request.flash.get("redirect-url").isDefined) {
    	Redirect(routes.Authentication.login).withNewSession.flashing("redirect-url" -> request.flash.get("redirect-url").get)
    } else {
    	Redirect(routes.Authentication.login).withNewSession
    }
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
  
  def buildFeedbackObjFromRequestOrKey(request: RequestHeader, messageKey: Option[String]) : Feedback = {
    if (request.flash.get("app-message").isDefined) {
  	  return new Feedback(request.flash.get("app-message").get, FeedbackClass.ok)
    }
    if (messageKey.isDefined) {
      val message = Messages(messageKey.get)(Lang("fr"))
      if (!message.equals(messageKey.get)) {
      	return new Feedback(message, FeedbackClass.ok)
      }
    }
    return null
  }
}