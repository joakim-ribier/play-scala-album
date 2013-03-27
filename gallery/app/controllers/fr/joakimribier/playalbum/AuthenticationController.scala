package controllers.fr.joakimribier.playalbum

import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import models.fr.joakimribier.playalbum.Feedback
import play.api.data.Form
import play.api.data.Forms.optional
import play.api.data.Forms.text
import play.api.data.Forms.tuple
import play.api.i18n.Lang
import play.api.i18n.Messages
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.RequestHeader
import play.api.mvc.Security
import play.api.mvc.Session
import play.cache.Cache
import utils.fr.joakimribier.playalbum.ConfigurationUtils
import utils.fr.joakimribier.playalbum.DateTimeUtils
import utils.fr.joakimribier.playalbum.EncoderUtils
import utils.fr.joakimribier.playalbum.MDCUtils
import models.fr.joakimribier.playalbum.UserTemplate
import models.fr.joakimribier.playalbum.UserEmail
import models.fr.joakimribier.playalbum.FeedbackClass
import models.fr.joakimribier.playalbum.User

object AuthenticationController extends Controller {
  
  val Logger = LoggerFactory.getLogger("AuthenticationController")
  private val _TITLE_HTML: String = ConfigurationUtils.getHTMLTitle()
  
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
  	val u = User.findUser(Option.apply(ConfigurationUtils.getAdminLogin()))
    if (u.isDefined) {
    	if (request.flash.get("redirect-url").isDefined) {
    	  val fill = form.fill("nothing", "nothing", Option.empty, Option.empty, Option.empty, Option.apply(request.flash.get("redirect-url").get))
  	    val feedback = new Feedback(Messages("app.global.message.url.redirection.html")(Lang("fr")), FeedbackClass.ok)
  	    Ok(views.html.fr.joakimribier.playalbum.login(fill, _TITLE_HTML, feedback))
    	} else {
    	  if (request.flash.get("app-message").isDefined) {
    	    val feedback = new Feedback(request.flash.get("app-message").get, FeedbackClass.ok)
          Ok(views.html.fr.joakimribier.playalbum.login(form, _TITLE_HTML, feedback))
        } else {
          Ok(views.html.fr.joakimribier.playalbum.login(form, _TITLE_HTML, null))
        }
      }
    } else {
    	Redirect(routes.ApplicationController.configuration)
    }
  }
  
  def redirect = Action { request =>
  	Redirect(routes.ApplicationController.configuration)
  }
  
  def authenticate = Action { implicit request =>
    form.bindFromRequest.fold(
      // Form has errors, redisplay it
      formWithErrors => BadRequest(views.html.fr.joakimribier.playalbum.login(formWithErrors, _TITLE_HTML, null)),
      // We got a valid User value
      value => {
        
      	val formUsername = value._1
      	val formEmail = value._4
      	val formToken = value._5
      	val formRedirectUrl = value._6
      	
      	val dateTime = DateTime.now().toString("yyyy-MM-dd'T'HH:mm:ss")
      	val sessionId = "uuid-" + formUsername + "-" + dateTime 
      	Cache.set(sessionId + "-" + ConfigurationUtils._SESSION_TIMEOUT_KEY, DateTimeUtils.now)
      	MDCUtils.put(sessionId)
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
    val user = User.findByEmail(Option.apply(emailValidation))
    val userEmail = UserEmail.getFromLogin(username)
    if (userEmail.isDefined || user.isDefined) {
      if (user.isDefined) {
        connectToAccountConfigurationPage(username, sessionId,
      			false, Messages("page.account.configuration.validation.email.already.exists.for.other.user", emailValidation)(Lang("fr")))(request)
      } else {
      	connectToAccountConfigurationPage(username, sessionId,
      			false, Messages("page.account.configuration.validation.email.already.exists")(Lang("fr")))(request)
      }
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
    		ConfigurationUtils._SESSION_ID_KEY -> sessionId,
    		ConfigurationUtils._SESSION_EMAIL_KEY -> formatUserEmailToString(username)
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
    				ConfigurationUtils._SESSION_ID_KEY -> sessionId,
    				ConfigurationUtils._SESSION_EMAIL_KEY -> formatUserEmailToString(username)
    				).flashing("connection" -> "success", "app-message" -> message.get)
    	} else {
    		Redirect(url).withSession(
    				Security.username -> username,
    				ConfigurationUtils._SESSION_ID_KEY -> sessionId,
    				ConfigurationUtils._SESSION_EMAIL_KEY -> formatUserEmailToString(username)).flashing("connection" -> "success")
    	}
    } else {
      Redirect(routes.AccountConfigurationController.index).withSession(
    				Security.username -> username,
    				ConfigurationUtils._SESSION_ID_KEY -> sessionId,
    				ConfigurationUtils._SESSION_EMAIL_KEY -> formatUserEmailToString(username)
    				).flashing("connection" -> "success")
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
  	Redirect(routes.ApplicationController.index)
  }
  
  def logout = Action { request =>
    Logger.info("You've been logged out")
    MDCUtils.closeSession()
    if (request.flash.get("redirect-url").isDefined) {
    	Redirect(routes.AuthenticationController.login).withNewSession.flashing("redirect-url" -> request.flash.get("redirect-url").get)
    } else {
      if (request.flash.get("app-message").isDefined) {
      	Redirect(routes.AuthenticationController.login).withNewSession.flashing("app-message" -> request.flash.get("app-message").get)
      } else {
        Redirect(routes.AuthenticationController.login).withNewSession
      }
    }
  }
  
  def userTemplate(username: String, session: Session) : UserTemplate = {
    return new UserTemplate(username, session.get(ConfigurationUtils._SESSION_EMAIL_KEY))
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