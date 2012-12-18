package models

import anorm._

case class UserEmail(id: Pk[Long] = NotAssigned, email: String)

object UserEmail {

  def getFromLogin(login: String) : String = {
    return UserEmailDB.findByLogin(login)
  }
}