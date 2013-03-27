package models.fr.joakimribier.playalbum

import anorm._
import db.fr.joakimribier.playalbum.UserEmailDB

case class UserEmail(id: Pk[Long] = NotAssigned, email: String)

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