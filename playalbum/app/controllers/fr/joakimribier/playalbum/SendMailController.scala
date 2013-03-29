package controllers.fr.joakimribier.playalbum

import java.util.Locale
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import com.typesafe.plugin.MailerAPI
import com.typesafe.plugin.MailerPlugin
import com.typesafe.plugin.use
import models.fr.joakimribier.playalbum.Media
import models.fr.joakimribier.playalbum.MediaType
import models.fr.joakimribier.playalbum.UserEmail
import play.api.Play.current
import play.api.i18n.Lang
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Results
import play.api.mvc.Security
import utils.fr.joakimribier.playalbum.ConfigurationUtils
import utils.fr.joakimribier.playalbum.EncoderUtils
import models.fr.joakimribier.playalbum.Post
import models.fr.joakimribier.playalbum.Comment

/**
 * 
 * Copyright 2013 Joakim Ribier
 * 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
object SendMailController extends Controller with Secured {

  private val Logger = LoggerFactory.getLogger("SendMail")
  
  private val _TITLE_HTML = ConfigurationUtils.getHTMLTitle
  private val _DAY_DURATION = ConfigurationUtils.getSendingMailEveryDayDuration
  private val _FROM = ConfigurationUtils.getSendingMailFromValue
  private val _HOST = ConfigurationUtils.getHost
  
  def newEmail = Action { implicit request =>
    val username = request.session.get(Security.username)
    if (username.isDefined) {
    	try {
    		val value = request.body.asFormUrlEncoded.get("address-post")
    				val addressMail: String = value(0)
    				if (addressMail != null && addressMail != "") {
    					
    					val generateURL = buildUrl(username.get, addressMail)
    							val textContent = Messages("sendmail.validation.email.text", generateURL, "\n\r")(Lang("fr"))
    							val htmlContent = Messages("sendmail.validation.email.html", generateURL)(Lang("fr"))
    							send(
    									Option.apply(addressMail),
    									Option.empty,
    									_TITLE_HTML + Messages("sendmail.validation.email.subject")(Lang("fr")),
    									Option.apply(textContent), Option.apply(htmlContent))("validation new user email")
    									
    									Ok(Json.obj("status" -> "success", "return" -> addressMail))
    				} else {
    					
    					Ok(Json.obj("status" -> "failed"))
    				}  
    	} catch {
    	case e: Throwable => {
    		Logger.error(e.getMessage(), e)
    		Ok(Json.obj("status" -> "failed")) 
    	}
    	}
    } else {
      Redirect(routes.AuthenticationController.logout)
    }
  }

  def notifyNewPhoto {
    try {
      val dateTime = DateTime.now().minusDays(_DAY_DURATION)
      val photos = Media.list(dateTime)
      val emails = UserEmail.list()
      if (emails != null && emails.size > 0 && photos != null && photos.size > 0) {
        for (email <- emails) {
          sendNotifyNewPhotoMail(dateTime, email, photos)
        }
      }
      Logger.info("send notification new media to user")
    } catch {
      case e: Throwable => {
        Logger.error(e.getMessage(), e)
      }
    }
  }
  
  def comments = {
    try {
      val dateTime = DateTime.now().minusMinutes(ConfigurationUtils.getSendingCommentsEveryMinutesDuration)
      val mediaIds = Post.listMediaIds(Option.apply(dateTime))
      if (!mediaIds.isEmpty) {
        val to = UserEmail.getFromLogin(ConfigurationUtils.getAdminLogin)
        for (mediaId <- mediaIds) {
          val media = Media.get(mediaId)
          val bcc = Post.listEmails(Option.apply(mediaId))
          val comments = Post.descList(Option.apply(mediaId))
          sendComments(comments, media, bcc, to.get)("send " + media.title + " comments from " + dateTime.toDate)
        }
      } else {
        Logger.info("there are no new comments")
      }
    } catch {
      case e: Throwable => {
        Logger.error(e.getMessage(), e)
      }
    }
  }
  
  private def sendComments(comments: Seq[Comment], media: Media, bccSeq: Seq[String], to: String)(log: String) {
    val link = _HOST + "/album/get/media/" + media.id + "/post/page/1/tags/all"
    val linkText = Messages("sendmail.access.post.comments.link.text", link)(Lang("fr"))
    val linkHtml = Messages("sendmail.access.post.comments.link.html", link)(Lang("fr"))

    var text = linkText + "\n\r\n\r"
    var html = linkHtml + "<br /><br />"
    
    for (comment <- comments) {
      val startText = "---------------------\n\rDe " + comment.user
      val commentText = "\n\r\n\r" + comment.message + "\n\r\n\r"
      val endText = comment.created.toString("yyyy-MM-dd HH:mm:SS") + "\n\r---------------------\n\r\n\r"
      
      val user = "De&nbsp;<span style=\"color:rgb(0, 175, 202);font-weight:bold;\">" + comment.user + "</span>"
      val p = "<p style=\"background-color:black;color:rgb(0, 175, 202);border:1px dashed black;text-align:justify;padding:10px;white-space: pre-line;\">" + comment.message + "</p>"
      val date = "<span style=\"color:rgb(0, 175, 202);\">" + comment.created.toString("yyyy-MM-dd HH:mm:SS") + "</span>"
      
      text = text + startText + commentText + endText
      html = html + "<div style=\"border-bottom:1px dashed black;padding:10px;width:400px;\">" + user + p + date + "</div>"  
    }
  	
  	var countComment = Messages("sendmail.comment.single")(Lang("fr"))
  	if (comments.size > 1) {
  	  countComment = Messages("sendmail.comment.multi", comments.size)(Lang("fr"))
  	}
  	
  	val titleText = Messages("sendmail.comment.text", countComment, media.title, "\n\r")(Lang("fr"))
  	val titleHtml = Messages("sendmail.comment.html", countComment, media.title)(Lang("fr"))
  	val textContent = titleText + text
  	val htmlContent = titleHtml + html
  	
    send(
	    Option.empty,
	    Option.apply(bccSeq :+ to),
	    _TITLE_HTML + ": " + Messages("sendmail.comment.subject", media.title)(Lang("fr")),
	    Option.apply(textContent), Option.apply(htmlContent))(log)
  }
  
  private def sendNotifyNewPhotoMail(dateTime: DateTime, recipient: String, photos: Seq[Media]) {
  	val photosSize = photos.size
		var content = ""
		var photoCount = 0
		var videoCount = 0
		for (photo <- photos) {
		  if (photo.mediaType == MediaType.PHOTO) {
		    photoCount += 1
		    var url = _HOST + "/album/get/thumbnail/photo/" + photo.filename
		    content = content + "<img src=\"" + url + "\" alt=\"" + photo.title + "\">&nbsp;&nbsp;"
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
  	val subject = _TITLE_HTML + Messages("sendmail.notify.new.photo.subject", labelCountPhotoTEXT + " " + andText + " " + labelCountVideoTEXT)(Lang("fr"))
  	
  	val textContent = Messages("sendmail.notify.new.photo.text", dateToString(dateTime, "fr"), labelCountPhotoTEXT + " " + andText + " " + labelCountVideoTEXT)(Lang("fr"))
  	val htmlContent = Messages("sendmail.notify.new.photo.html", dateToString(dateTime, "fr"), labelCountPhotoHTML + " " + andText + " " + labelCountVideoHTML, content)(Lang("fr"))
  	send(
  	    Option.apply(recipient),
  	    Option.empty,
  	    subject,
  	    Option.apply(textContent), Option.apply(htmlContent))("notification media from " + dateTime.toDate)
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
    val token = EncoderUtils.generateTokenForEmailValidation(username, addressMail)
    return _HOST + validationURL + addressMail + "/token/" + token
  }
  
  private def send(recipient: Option[String], bcc: Option[Seq[String]], subject: String, textContent: Option[String], htmlContent: Option[String])(log: String) {
  	val mail = use[MailerPlugin].email
		mail.setSubject(subject)
		mail.addFrom(_FROM)
		addRecipient(mail, recipient)
		addBcc(mail, bcc)
		send(mail, textContent, htmlContent)(log)
  }
  
  private def addRecipient(mail: MailerAPI, recipient: Option[String]) {
    if (recipient.isDefined) {
      Logger.info("to {}", recipient.get)
			mail.addRecipient(recipient.get)
		}
  }
  
  private def addBcc(mail: MailerAPI, bcc: Option[Seq[String]]) {
    if (bcc.isDefined) {
      Logger.info("bcc {}", bcc.get)
			mail.addBcc(bcc.get:_*)
		}
  }
  
  private def send(mail: MailerAPI, textContent: Option[String], htmlContent: Option[String])(log: String) {
    try {
    	if (textContent.isDefined && htmlContent.isDefined) {
    		mail.send(
    		    Messages("sendmail.info.auto.text", _HOST, "\n\r")(Lang("fr")) + textContent.get,
    		    Messages("sendmail.info.auto.html", _HOST)(Lang("fr")) + htmlContent.get)
    	} else {
    		if (textContent.isDefined) {
    			mail.send(Messages("sendmail.info.auto.text", _HOST, "\n\r")(Lang("fr")) +  textContent.get)
    		}
    		if (htmlContent.isDefined) {
    			mail.sendHtml(Messages("sendmail.info.auto.html", _HOST)(Lang("fr")) + htmlContent.get)
    		}
    	}
    	Logger.info("send mail '{}' success", log)
    } catch {
      case e: Throwable => {
        Logger.error("send mail '{}' failure", log)
        throw e
      }
    }
  }
}