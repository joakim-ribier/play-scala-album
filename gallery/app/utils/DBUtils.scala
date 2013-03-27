package utils

object OrderEnum extends Enumeration {
  val ASC = new Value(1, "asc")
  val DESC = new Value(2, "desc")
  class Value(id:Int, value: String) extends Val(id, value) {
  	val dbId = id
    val label = value
  }
}
import OrderEnum._

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
    val s = login + ConfigurationUtils.getToken + password
    val md = java.security.MessageDigest.getInstance("SHA-1")
    return new sun.misc.BASE64Encoder().encode(md.digest(s.getBytes))
  }
}