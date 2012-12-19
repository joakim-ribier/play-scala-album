package controllers

import play.api.mvc.Controller
import com.typesafe.plugin._
import play.api.Play.current
import utils.Configuration
import play.api.libs.json.Json

object SendMail extends Controller with Secured {

  private val _TITLE_HTML: String = Configuration.getHTMLTitle()
  
  def newEmail = withUser { username => implicit request =>
  	val value = request.body.asFormUrlEncoded.get("address-post")
  	val addressMail: String = value(0)
  	if (addressMail != null && addressMail != "") {

  	  val mail = use[MailerPlugin].email
			mail.setSubject(_TITLE_HTML + " : Validation de l'adresse mail")
			mail.addRecipient(addressMail)
			mail.addFrom(Configuration.getStringValue(Configuration._MAIL_FROM))

			val str = username + Configuration.getToken() + addressMail
      val md = java.security.MessageDigest.getInstance("SHA-1")
      val token = new sun.misc.BASE64Encoder().encode(md.digest(str.getBytes))
    
      val validationURL = "/album/configuration/user/new/address/mail/validation/"
			val generateURL = Configuration.getStringValue(Configuration._APP_HOST) + validationURL + addressMail + "/token/" + token
			mail.send(generateURL)
  	
			Ok(Json.toJson(Map("status" -> "success", "return" -> addressMail)))
  	} else {
  	  
  		Ok(Json.toJson(Map("status" -> "failed")))
  	}
  }
  
}