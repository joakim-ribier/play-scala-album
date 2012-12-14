package models

import java.util.Date
import org.joda.time.DateTime
import anorm._
import utils.Configuration
import utils.DBUtils
import play.api.Logger

case class User(id: Pk[Long] = NotAssigned, login: String, password: String, created: DateTime)

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
  
  def createUser(login: String, password: String) : Boolean = {
	if (!checkNotNullOrNotEmpty(login, password) || UserDB.findByLogin(login).isDefined) {
	  return false;
	}
    val newUser = User(null, login, DBUtils.encodeUserPassword(login, password), DateTime.now())
    val id: Int = UserDB.insert(newUser)
    return id.isInstanceOf[Int]
  }
  
  def checkNotNullOrNotEmpty(login: String, password: String) : Boolean = {
    if (login == null || login.isEmpty() || password == null || password.isEmpty()) {
	  return false
	}
    return true
  }
  
  def findUser(login: String) = UserDB.findByLogin(login)
}