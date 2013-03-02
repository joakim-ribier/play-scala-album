package models.post

import models.User
import anorm.Pk
import anorm.NotAssigned
import db.post.PostDB
import org.joda.time.DateTime
import play.api.Logger
import org.slf4j.LoggerFactory
import utils.OrderEnum
import models.Media

case class PostId(id: Long)
case class CommentId(id: Long)
case class Comment(id: CommentId, message: String, created: DateTime, user: String, postId: PostId)

object Post {

  private val Logger = LoggerFactory.getLogger("Post")
  
  def addComment(login: Option[String], mediaId: Option[Long], comment: Option[String]) : Boolean = {
    try {
      val user = User.findUser(login)
      if (user.isDefined) {
        val postId = PostDB.findByMediaId(mediaId.get)
        if (postId.isDefined) {
          return createComment(postId, user, comment).isDefined
        } else {
          val newPostId = create(mediaId)
          return createComment(newPostId, user, comment).isDefined
        }
      }
      return false      
    } catch {
      case e: Throwable => {
        Logger.error(e.getMessage(), e)
        false
      }
    }
  }
  
  def create(mediaId: Option[Long]) : Option[PostId] = {
    if (!mediaId.isDefined) {
      return Option.empty
    }
    return PostDB.insert(mediaId.get, DateTime.now())
  }
  
  def createComment(postId: Option[PostId], user: Option[User], comment: Option[String]) : Option[CommentId] = {
    if (!postId.isDefined || !user.isDefined || !comment.isDefined || comment.get.isEmpty) {
      return Option.empty
    }
    return PostDB.insertMessage(postId.get, user.get, comment.get, DateTime.now())
  }
  
  def list(mediaId: Option[Long]) : Seq[Comment] = {
    if (!mediaId.isDefined) {
      return Seq.empty
    }
    return PostDB.findAllBy(mediaId.get, OrderEnum.ASC)
  }
  
  def descList(mediaId: Option[Long]) : Seq[Comment] = {
    if (!mediaId.isDefined) {
      return Seq.empty
    }
    return PostDB.findAllBy(mediaId.get, OrderEnum.DESC)
  }
  
  def remove(commentId: Option[Long], user: Option[User]) {
    if (!commentId.isDefined || !user.isDefined) {
      throw new IllegalArgumentException("params {commentId} and {user} are required")
    }
    val result = PostDB.delete(CommentId(commentId.get), user.get)
    if (result != 1) {
    	throw new UnsupportedOperationException("delete comment with id " + commentId.get + " failed")
    }
  }
  
  def update(commentId: Option[Long], comment: Option[String], user: Option[User]) {
    if (!commentId.isDefined || !comment.isDefined || !user.isDefined) {
      throw new IllegalArgumentException("params {commentId} and {comment} and {user} are required")
    }
    val result = PostDB.update(CommentId(commentId.get), comment.get, user.get)
    if (result != 1) {
    	throw new UnsupportedOperationException("update comment with id " + commentId.get + " failed")
    }
  }
  
  def listMediaIds(dateTime: Option[DateTime]) : Seq[Long] = {
    if (!dateTime.isDefined) {
      return Seq.empty
    }
    return PostDB.findMediaIdsBy(dateTime.get)
  }
  
  def listEmails(mediaId: Option[Long]) : Seq[String] = {
    if (!mediaId.isDefined) {
      return Seq.empty
    }
    return PostDB.findEmailsBy(mediaId.get)
  }
  
  def count(medias: Seq[Media]) : Map[Long, Long] = {
    var countByMedia: Map[Long, Long] = Map()
    for (media <- medias) {
      val mediaId: Long = media.id.get
      countByMedia += (mediaId -> PostDB.count(mediaId))
    }
    return countByMedia
  }
}