package controllers

import play.api.mvc.Controller
import com.typesafe.plugin._
import play.api.Play.current
import play.api.libs.json.Json
import java.net.URLDecoder
import utils.URLEncoderDecoderUtils
import utils.Configuration
import utils.TokenUtils
import play.api.Logger

object SendMail extends Controller with Secured {

  private val _TITLE_HTML: String = Configuration.getHTMLTitle()
  
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
  
  private def buildUrl(username: String, addressMail: String) : String = {
    val validationURL = "/album/user/new/address/mail/validation/"
    val token = TokenUtils.validationAddressMail(username, addressMail)
		val encodedMail = URLEncoderDecoderUtils.encode(addressMail)
    val encodedToken = URLEncoderDecoderUtils.encode(token)
    return Configuration.getHost() + validationURL + encodedMail + "/token/" + encodedToken
  }
  
  private def sendMail(recipient: String, content: String) {
  	val mail = use[MailerPlugin].email
		mail.setSubject(_TITLE_HTML + " : Validation de l'adresse mail")
		mail.addRecipient(recipient)
		mail.addFrom(Configuration.getStringValue(Configuration._MAIL_FROM))
		mail.send(content)
  }
}