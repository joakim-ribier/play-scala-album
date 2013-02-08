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
import models.Media
import org.joda.time.DateTime
import models.UserEmail
import java.util.Locale
import models.MediaType
import org.slf4j.LoggerFactory

object SendMail extends Controller with Secured {

  private val Logger = LoggerFactory.getLogger("SendMail")
  
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
	  	
				Ok(Json.obj("status" -> "success", "return" -> addressMail))
	  	} else {
	  	  
	  		Ok(Json.obj("status" -> "failed"))
	  	}  
    } catch {
      case e => {
        Logger.error(e.getMessage(), e)
        Ok(Json.obj("status" -> "failed")) 
      }
    }
  }

  def notifyNewPhoto(key: String) = Action {
    try {
	    if (key != null && key.equals(_PRIVATE_KEY)) {
	      
	      val dateTime = DateTime.now().minusDays(_DAY_DURATION)
	      val photos = Media.list(dateTime)
	      val emails = UserEmail.list()
	      
	      if (emails != null && emails.size > 0
	          && photos != null && photos.size > 0) {
	        
	        for (email <- emails) {
	        	sendNotifyNewPhotoMail(dateTime, email, photos)
	        }
	      }
	
	      Logger.info("send notification new media to user")
	      Results.Ok
	    } else {
	      Logger.error("access at notifyNewPhoto service on SendMail controller with wrong private key [ " + key + " ]")
	      Results.Unauthorized
	    }  
    } catch {
      case e => {
        Logger.error(e.getMessage(), e)
        Results.ExpectationFailed
      }
    }
  }
  
  private def sendNotifyNewPhotoMail(dateTime: DateTime, recipient: String, photos: Seq[Media]) {
  	val photosSize = photos.size
  	
    val mail = use[MailerPlugin].email
  	mail.addRecipient(recipient)
  	mail.addFrom(Configuration.getStringValue(Configuration._MAIL_FROM))
		
		var htmlContent = ""
		val host = Configuration.getHost()
		
		var photoCount = 0
		var videoCount = 0
		for (photo <- photos) {
		  if (photo.mediaType == MediaType.PHOTO) {
		    photoCount += 1
		    var url = host + "/album/get/thumbnail/photo/" + photo.filename
		    htmlContent = htmlContent + "<img src=\"" + url + "\" alt=\"" + photo.title + "\">&nbsp;&nbsp;"
		  } else {
		  	videoCount += 1
		  }
		}
		  
  	var labelCountPhotoHTML = Messages("sendmail.notify.new.photo.single.html", photoCount)(Lang("fr"))
		var labelCountPhotoTEXT = Messages("sendmail.notify.new.photo.single.text", photoCount)(Lang("fr"))
		if (photoCount > 1) {
			labelCountPhotoHTML = Messages("sendmail.notify.new.photo.multiple.html", photoCount)(Lang("fr"))
			labelCountPhotoTEXT = Messages("sendmail.notify.new.photo.multiple.text", photoCount)(Lang("fr"))
		}
  	
  	var labelCountVideoHTML = Messages("sendmail.notify.new.video.single.html", videoCount)(Lang("fr"))
		var labelCountVideoTEXT = Messages("sendmail.notify.new.video.single.text", videoCount)(Lang("fr"))
		if (videoCount > 1) {
			labelCountVideoHTML = Messages("sendmail.notify.new.video.multiple.html", videoCount)(Lang("fr"))
			labelCountVideoTEXT = Messages("sendmail.notify.new.video.multiple.text", videoCount)(Lang("fr"))
		}
  	
  	val andText = Messages("sendmail.notify.new.media.and")(Lang("fr"))
  	mail.setSubject(_TITLE_HTML + Messages("sendmail.notify.new.photo.subject", labelCountPhotoTEXT + " " + andText + " " + labelCountVideoTEXT)(Lang("fr")))
  	
		mail.send(
		    Messages("sendmail.notify.new.photo.text", Configuration.getHost(), dateToString(dateTime, "fr"), labelCountPhotoTEXT + " " + andText + " " + labelCountVideoTEXT, "\n\r")(Lang("fr")),
		    Messages("sendmail.notify.new.photo.html", Configuration.getHost(), dateToString(dateTime, "fr"), labelCountPhotoHTML + " " + andText + " " + labelCountVideoHTML, htmlContent)(Lang("fr")))
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