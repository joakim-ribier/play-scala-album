package models.fr.joakimribier.playalbum

import anorm._
import db.fr.joakimribier.playalbum.UserEmailDB

case class UserEmail(id: Pk[Long] = NotAssigned, email: String)

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
object UserEmail {

  def getFromLogin(login: String) : Option[String] = {
    return UserEmailDB.findByLogin(login)
  }
  
  def createEmail(user: User, email: String) : Long = {
    if (user == null || email == null) {
      throw new IllegalArgumentException("userId and email argument cannot be null")
    }
    UserEmailDB.insert(user.id.get, email)
  }
  
  def list() = UserEmailDB.findAll()
}