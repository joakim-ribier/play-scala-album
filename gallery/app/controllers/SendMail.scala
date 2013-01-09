package controllers

import play.api.mvc.Controller
import com.typesafe.plugin._
import play.api.Play.current
import play.api.libs.json.Json
import java.net.URLDecoder
import utils.Configuration
import utils.TokenUtils
import play.api.Logger
import play.api.i18n.Messages
import play.api.i18n.Lang
import play.api.mvc.Action
import play.api.mvc.Result
import play.api.mvc.Results
import models.Photo
import org.joda.time.DateTime
import models.UserEmail
import java.util.Locale

object SendMail extends Controller with Secured {

	private val _TITLE_HTML = Configuration.getHTMLTitle()
  private val _PRIVATE_KEY = Configuration.getStringValue(Configuration._MAIL_AUTO_SEND_PRIVATE_KEY)
  private val _DAY_DURATION = Configuration.getIntValue(Configuration._MAIL_AUTO_SEND_DAY_DURATION)
  
  def newEmail = withUser { username => implicit request =>
    try {
	    val value = request.body.asFormUrlEncoded.get("address-post")
	  	val addressMail: String = value(0)
	  	if (addressMail != null && addressMail != "") {
	  	  
	      val generateURL = buildUrl(username, addressMail)
	      sendMail(addressMail, generateURL)
	  	
				Ok(Json.toJson(Map("status" -> "success", "return" -> addressMail)))
	  	} else {
	  	  
	  		Ok(Json.toJson(Map("status" -> "failed")))
	  	}  
    } catch {
      case e => {
        Logger.error(e.getMessage(), e)
        Ok(Json.toJson(Map("status" -> "failed"))) 
      }
    }
  }

  def notifyNewPhoto(key: String) = Action {
    try {
	    if (key != null && key.equals(_PRIVATE_KEY)) {
	      
	      val dateTime = DateTime.now().minusDays(_DAY_DURATION)
	      val photos = Photo.list(dateTime)
	      val emails = UserEmail.list()
	      
	      if (emails != null && emails.size > 0
	          && photos != null && photos.size > 0) {
	        
	        for (email <- emails) {
	        	sendNotifyNewPhotoMail(dateTime, email, photos)
	        }
	      }
	
	      Results.Ok
	    } else {
	      Logger.error("Try to access at notifyNewPhoto service on SendMail controller with wrong private key [ " + key + " ]")
	      Results.Unauthorized
	    }  
    } catch {
      case e => {
        Logger.error(e.getMessage(), e)
        Results.ExpectationFailed
      }
    }
  }
  
  private def sendNotifyNewPhotoMail(dateTime: DateTime, recipient: String, photos: Seq[Photo]) {
  	val photosSize = photos.size
  	
    val mail = use[MailerPlugin].email
    
    var labelCountPhotoHTML = Messages("sendmail.notify.new.photo.single.html")(Lang("fr"))
    var labelCountPhotoTEXT = Messages("sendmail.notify.new.photo.single.text")(Lang("fr"))
		if (photosSize > 1) {
			labelCountPhotoHTML = Messages("sendmail.notify.new.photo.multiple.html", photosSize)(Lang("fr"))
		  labelCountPhotoTEXT = Messages("sendmail.notify.new.photo.multiple.text", photosSize)(Lang("fr"))
		}
  	mail.setSubject(_TITLE_HTML + Messages("sendmail.notify.new.photo.subject", labelCountPhotoTEXT)(Lang("fr")))

  	mail.addRecipient(recipient)
  	mail.addFrom(Configuration.getStringValue(Configuration._MAIL_FROM))
		
		var htmlContent = ""
		val host = Configuration.getHost()
		
		for (photo <- photos) {
			var url = host + "/album/get/thumbnail/photo/" + photo.filename
		  htmlContent = htmlContent + "<img src=\"" + url + "\" alt=\"" + photo.title + "\">&nbsp;&nbsp;"
		}
		  
		mail.send(
		    Messages("sendmail.notify.new.photo.text", Configuration.getHost(), dateToString(dateTime, "fr"), labelCountPhotoTEXT, "\n\r")(Lang("fr")),
		    Messages("sendmail.notify.new.photo.html", Configuration.getHost(), dateToString(dateTime, "fr"), labelCountPhotoHTML, htmlContent)(Lang("fr")))
  }

  private def dateToString(dateTime: DateTime, lang: String) : String = {
  	val pDoW = dateTime.dayOfWeek()
    val pDoM = dateTime.dayOfMonth()
    val pMoY = dateTime.monthOfYear()
    val year = dateTime.getYear()
    
  	if (lang.equalsIgnoreCase("fr")) {
      return pDoW.getAsText(Locale.FRENCH) + ", " + pDoM.getAsText(Locale.FRENCH) + " " + pMoY.getAsText(Locale.FRENCH) + " " + dateTime.getYear()
    } else {
      return pDoW.getAsText(Locale.ENGLISH) + ", " + pMoY.getAsText(Locale.ENGLISH) + " " + pDoW.getAsText(Locale.ENGLISH) + " " + dateTime.getYear()
    }
  }
  
  private def buildUrl(username: String, addressMail: String) : String = {
    val validationURL = "/album/user/new/address/mail/validation/"
    val token = TokenUtils.validationAddressMail(username, addressMail)
    return Configuration.getHost() + validationURL + addressMail + "/token/" + token
  }
  
  private def sendMail(recipient: String, content: String) {
  	val mail = use[MailerPlugin].email
		mail.setSubject(_TITLE_HTML + Messages("sendmail.validation.email.subject")(Lang("fr")))
		mail.addRecipient(recipient)
		mail.addFrom(Configuration.getStringValue(Configuration._MAIL_FROM))
		mail.send(
		    Messages("sendmail.validation.email.text", Configuration.getHost(), content, "\n\r")(Lang("fr")),
		    Messages("sendmail.validation.email.html", Configuration.getHost(), content)(Lang("fr")))
  }
}