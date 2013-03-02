package controllers

import play.api.mvc.Controller
import utils.Configuration
import models.UserTemplate
import models.Media
import org.slf4j.LoggerFactory
import play.api.data.Form
import play.api.data.Forms.text
import play.api.data.Forms.tuple
import play.api.data.Forms.longNumber
import play.api.i18n.Messages
import play.api.i18n.Lang
import models.post.Post
import play.api.mvc.Action
import models.Feedback
import models.Feedback
import models.FeedbackClass
import play.api.mvc.RequestHeader
import play.api.libs.json.Json
import models.User

object PostController extends Controller with Secured {

  private val Logger = LoggerFactory.getLogger("PostController")
  private val _TITLE_HTML: String = Configuration.getHTMLTitle()
  
  private val formNewComment = Form (
    tuple (
    	"login" -> text,
    	"mediaId" -> longNumber,
      "comment" -> text,
      "page" -> text,
      "byTags" -> text
    ) verifying (Messages("page.post.new.comment.failed.html")(Lang("fr")), result => result match {
      case (login, mediaId, comment, page, byTags) => Post.addComment(Option.apply(login), Option.apply(mediaId), Option.apply(comment))
    })
  )
  
  def index(mediaId: String, page: String, byTags: String) = withUser { username => implicit request =>
    redirectToIndex(mediaId, page, byTags, Option.empty, username)(request)
  }
  
  def indexWithMessageKey(mediaId: String, page: String, byTags: String, messageKey: String) = withUser { username => implicit request =>
    redirectToIndex(mediaId, page, byTags, Option.apply(messageKey), username)(request)
  }
  
  private def redirectToIndex(id: String, page: String, byTags: String, messageKey: Option[String], username: String) = Action { implicit request =>
    try {
      val userTemplate = new UserTemplate(username, request.session.get(Configuration._SESSION_EMAIL_KEY))

      val mediaId = id.asInstanceOf[String].toInt
      val media = Media.get(mediaId)
      Ok(views.html.post(_TITLE_HTML, Authentication.buildFeedbackObjFromRequestOrKey(request, messageKey),
          userTemplate, media, page, byTags, formNewComment, Post.list(Option.apply(media.id.get)), Media.computePos(Option.apply(media.id.get)), Media.total))
    } catch {
      case e: Throwable => {
       Logger.error(e.getMessage(), e) 
       Redirect(routes.Application.index)
      }
    }
  }
  
  def addNewComment = withUser { username => implicit request =>
    Logger.info("add new comment")
    val userTemplate = Authentication.userTemplate(username, request.session)
    formNewComment.bindFromRequest.fold(
      formWithErrors => {
        val mediaId = formWithErrors.data.get("mediaId").get
        val page = formWithErrors.data.get("page").get
        val byTags = formWithErrors.data.get("byTags").get
        Redirect(routes.PostController.index(mediaId, page, byTags)).flashing("app-message" -> formWithErrors.errors(0).message)
      },
      value => {
        val mediaId = value._2.toString
        val page = value._4
        val byTags = value._5
        Redirect(routes.PostController.index(mediaId, page, byTags))
      }
    )
  }
  
  def removeComment = withUser { username => implicit request =>
    try {
    	Logger.info("remove comment")
    	val value = request.body.asFormUrlEncoded.get("commentid-post")
    	Post.remove(Option.apply(value(0).toLong), User.findUser(Option.apply(username)))
    	Ok(Json.obj("status" -> "success", "key" -> "page.post.delete.comment.success.html"))
    } catch {
      case e: Throwable => {
       Logger.error(e.getMessage(), e) 
       Ok(Json.obj("status" -> "failed", "key" -> "page.post.delete.comment.failed.html"))
      }
    }
  }
  
  def updateComment = withUser { username => implicit request =>
    try {
    	Logger.info("update comment")
    	val commentId = request.body.asFormUrlEncoded.get("commentid-post")
    	val commentText = request.body.asFormUrlEncoded.get("commenttext-post")
    	Post.update(Option.apply(commentId(0).toLong), Option.apply(commentText(0)), User.findUser(Option.apply(username)))
    	Ok(Json.obj("status" -> "success", "key" -> "page.post.update.comment.success.html"))
    } catch {
      case e: Throwable => {
       Logger.error(e.getMessage(), e) 
       Ok(Json.obj("status" -> "failed", "key" -> "page.post.update.comment.failed.html"))
      }
    }
  }
  
  def previous(mediaId: String, page: String, byTags: String) = withUser { username => implicit request =>
    try {
    	val media = Media.getNextPhoto(mediaId.toLong)
    	redirectToIndex(media.id.get.toString, page, byTags, Option.empty, username)(request)
    } catch {
      case e: Throwable => {
        Logger.error(e.getMessage(), e) 
        Redirect(routes.Application.index)
      }
    }
  }
  
  def next(mediaId: String, page: String, byTags: String) = withUser { username => implicit request =>
    try {
    	val media = Media.getPreviousPhoto(mediaId.toLong)
    	redirectToIndex(media.id.get.toString, page, byTags, Option.empty, username)(request)
    } catch {
      case e: Throwable => {
        Logger.error(e.getMessage(), e) 
        Redirect(routes.Application.index)
      }
    }
  }
}