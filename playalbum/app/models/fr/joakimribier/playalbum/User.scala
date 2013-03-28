package models.fr.joakimribier.playalbum

import org.joda.time.DateTime
import anorm._
import utils._
import utils.fr.joakimribier.playalbum.ConfigurationUtils
import db.fr.joakimribier.playalbum.UserDB
import utils.fr.joakimribier.playalbum.DBUtils

case class UserTemplate(login: String, email: Option[String])
case class User(id: Pk[Long] = NotAssigned, login: String, password: String, created: DateTime, userEmail: Option[UserEmail])

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
object User {

  def authenticate(login: String, password: String, code : Option[String]) : Boolean = {
    val find = UserDB.findByLoginAndPassword(login, DBUtils.encodeUserPassword(login, password))
    if (find.isDefined) {
      return true
    }
    
    if (code.isDefined && code.get == ConfigurationUtils.getCreateNewUserCode()) {
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
    return login.equals(ConfigurationUtils.getAdminLogin())
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