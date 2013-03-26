package models

import java.util.Date
import org.joda.time.DateTime
import anorm._
import play.api.Logger
import utils._
import db.UserDB

case class UserTemplate(login: String, email: Option[String])
case class User(id: Pk[Long] = NotAssigned, login: String, password: String, created: DateTime, userEmail: Option[UserEmail])

object User {

  def authenticate(login: String, password: String, code : Option[String]) : Boolean = {
    val find = UserDB.findByLoginAndPassword(login, DBUtils.encodeUserPassword(login, password))
    if (find.isDefined) {
      return true
    }
    
    if (code.isDefined && code.get == Configuration.getCreateNewUserCode()) {
      // check if user exists and/or create it
      if (createUser(login, password)) {
        return true
      }
    }
    return false
  }
  
  def createUser(login: String, password: String, email: String) : Boolean = {
    if ((!email.isEmpty() && email != null) && createUser(login, password)) {
      val user = findUser(Option.apply(login))
      if (user.isDefined) {
      	return setAddressMail(user.get, email)
      }
    }
    return false
  }
  
  def createUser(login: String, password: String) : Boolean = {
    if (!checkNotNullOrNotEmpty(login, password) || UserDB.findByLogin(login).isDefined) {
    	return false;
    }
    val newUser = User(null, login, DBUtils.encodeUserPassword(login, password), DateTime.now(), Option.empty)
    val id: Int = UserDB.insert(newUser)
    return id.isInstanceOf[Int]
  }
  
  def checkNotNullOrNotEmpty(login: String, password: String) : Boolean = {
    if (login == null || login.isEmpty() || password == null || password.isEmpty()) {
    	return false
    }
    return true
  }
  
  def findUser(login: Option[String]) : Option[User] = {
    if (!login.isDefined) {
      return Option.empty
    }
    return UserDB.findByLogin(login.get) 
  }
  
  def isAdmin(login: String) : Boolean = {
    return login.equals(Configuration.getAdminLogin())
  }
  
  def setAddressMail(user: User, email: String) : Boolean = {
    val id = UserEmail.createEmail(user, email)
    return id.isInstanceOf[Long]
  }
  
  def findByEmail(email: Option[String]) : Option[User] = {
    if (!email.isDefined) {
      return Option.empty
    }
    return UserDB.findByEmail(email.get) 
  }
}