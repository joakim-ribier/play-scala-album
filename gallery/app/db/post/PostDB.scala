package db.post

import utils.Configuration
import org.joda.time.DateTime
import play.api.db.DB
import play.api.Play.current
import anorm.SqlParser._
import anorm._
import models.post.PostId
import models.post.CommentId
import models.post.Comments
import models.User
import java.util.Date
import models.post.CommentId
import utils.OrderEnum

object PostDB {

  private val _DB_TBL_MEDIA_POST: String = play.Configuration.root().getString(Configuration._TABLE_MEDIA_POST_KEY)
  private val _DB_TBL_MEDIA_MESSAGE : String = play.Configuration.root().getString(Configuration._TABLE_MEDIA_POST_MESSAGE_KEY)
  private val _DB_TBL_USER : String = play.Configuration.root().getString(Configuration._TABLE_USER_KEY)
  private val _DB_TBL_USER_EMAIL : String = play.Configuration.root().getString(Configuration._TABLE_EMAIL_KEY)
  
  private val id = {
    get[Pk[Long]](_DB_TBL_MEDIA_POST + ".id") map {
      case id => PostId(id.get)
    }
  }
  
  private val messageId = {
    get[Pk[Long]](_DB_TBL_MEDIA_MESSAGE + ".id") map {
      case id => CommentId(id.get)
    }
  }
  
  private val messages = {
    get[Pk[Long]](_DB_TBL_MEDIA_MESSAGE + ".id") ~
    get[String](_DB_TBL_MEDIA_MESSAGE + ".message") ~
    get[Date](_DB_TBL_MEDIA_MESSAGE + ".created") ~
    get[String](_DB_TBL_USER + ".login") ~
    get[Long](_DB_TBL_MEDIA_MESSAGE + ".album_media_post") map {
      case id~message~created~login~postId => Comments(CommentId(id.get), message, new DateTime(created), login, PostId(postId))
    }
  }
  
  def insert(mediaId: Long, created: DateTime) : Option[PostId] = {
    return DB.withConnection { implicit connection =>
      SQL(
        """
          INSERT INTO """ + _DB_TBL_MEDIA_POST + """ (album_media, created) VALUES (
            {mediaId}, {created}
          ) RETURNING id
        """
      ).on(
        'mediaId -> mediaId,
        'created -> created.toDate()
      ).as(PostDB.id.singleOpt)
    }
  }
  
  def findByMediaId(mediaId: Long) : Option[PostId] = {
    return DB.withConnection { implicit connection =>
      val sql = SQL(
        """
          SELECT * FROM """ + _DB_TBL_MEDIA_POST + """
          WHERE album_media = {mediaId}
        """
      ).on('mediaId -> mediaId)
      sql.as(PostDB.id.singleOpt)
    }
  }
   
  def insertMessage(postId: PostId, user: User, message: String, created: DateTime) : Option[CommentId] = {
    return DB.withConnection { implicit connection =>
      SQL(
        """
          INSERT INTO """ + _DB_TBL_MEDIA_MESSAGE + """ (album_media_post, album_user, message, created) VALUES (
            {postId}, {userId}, {message}, {created}
          ) RETURNING id
        """
      ).on(
        'postId -> postId.id,
        'userId -> user.id,
        'message -> message,
        'created -> created.toDate()
      ).as(PostDB.messageId.singleOpt)
    }
  }
  
  def findAllBy(mediaId: Long, order: OrderEnum.Value) : Seq[Comments] = {
    return DB.withConnection { implicit connection =>
      val sql = SQL(
        """
          SELECT * FROM """ + _DB_TBL_MEDIA_MESSAGE + """
          JOIN """ + _DB_TBL_MEDIA_POST + """
          ON (""" + _DB_TBL_MEDIA_MESSAGE + """.album_media_post = """ + _DB_TBL_MEDIA_POST + """.id)
          JOIN """ + _DB_TBL_USER + """
          ON (""" + _DB_TBL_MEDIA_MESSAGE + """.album_user = """ + _DB_TBL_USER + """.id)
          WHERE """ + _DB_TBL_MEDIA_POST + """.album_media = {mediaId}
          ORDER BY """ + _DB_TBL_MEDIA_MESSAGE + """.created """ + order + """
        """
      ).on('mediaId -> mediaId)
      sql.as(PostDB.messages *)
    }
  }
  
   def delete(commentId: CommentId, user: User) : Int = {
    return DB.withTransaction { implicit connection =>
      SQL("DELETE FROM " + _DB_TBL_MEDIA_MESSAGE + " WHERE id = {commentId} AND album_user = {userid}"
    	    ).on('commentId -> commentId.id, 'userid -> user.id).executeUpdate()
    }
  }
   
  def update(commentId: CommentId, message: String, user: User) : Int = {
    return DB.withTransaction { implicit connection =>
      SQL("UPDATE " + _DB_TBL_MEDIA_MESSAGE + " SET message = {message} WHERE id = {commentId} AND album_user = {userid}"
    	    ).on(
    	        'commentId -> commentId.id,
    	        'message -> message,
    	        'userid -> user.id).executeUpdate()
    }
  }

  def findMediaIdsBy(dateTime: DateTime) : Seq[Long] = {
    return DB.withConnection { implicit connection =>
      val sql = SQL(
        """
          SELECT distinct album_media FROM """ + _DB_TBL_MEDIA_POST + """
          JOIN """ + _DB_TBL_MEDIA_MESSAGE + """
          ON (""" + _DB_TBL_MEDIA_MESSAGE + """.album_media_post = """ + _DB_TBL_MEDIA_POST + """.id)
          WHERE """ + _DB_TBL_MEDIA_MESSAGE + """.created > {datetime}
        """
      ).on('datetime -> dateTime.toDate())
      sql().map(row => row[Long]("album_media")).toList
    }
  }
  
  def findEmailsBy(mediaId: Long) : Seq[String] = {
    return DB.withConnection { implicit connection =>
      val sql = SQL(
        """
          SELECT distinct email FROM """ + _DB_TBL_MEDIA_MESSAGE + """
          JOIN """ + _DB_TBL_MEDIA_POST + """
          ON (""" + _DB_TBL_MEDIA_MESSAGE + """.album_media_post = """ + _DB_TBL_MEDIA_POST + """.id)
          JOIN """ + _DB_TBL_USER_EMAIL + """
          ON (""" + _DB_TBL_MEDIA_MESSAGE + """.album_user = """ + _DB_TBL_USER_EMAIL + """.user_id)
          WHERE """ + _DB_TBL_MEDIA_POST + """.album_media = {mediaid}
        """
      ).on('mediaid -> mediaId)
      sql().map(row => row[String]("email")).toList
    }
  }
}
