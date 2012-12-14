package utils

object DBUtils {

  def formatSEQToString(list: Seq[Object]) : String = {
     var value = ""
     for (el <- list) {
      value = value + ",'" + el + "'"
     }
     return value.substring(1, value.length())
  }
  
  def formatSEQLongToString(list: Seq[Long]) : String = {
     var value = ""
     for (el <- list) {
      value = value + ",'" + el + "'"
     }
     return value.substring(1, value.length())
  }
  
  def encodeUserPassword(login: String, password: String) : String = {
    val s = login + Configuration.getPasswordToken() + password
    val md = java.security.MessageDigest.getInstance("SHA-1")
    return new sun.misc.BASE64Encoder().encode(md.digest(s.getBytes))
  }
}