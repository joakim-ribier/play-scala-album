package utils

object TokenUtils {

  def validationAddressMail(username: String, addressMail: String) : String = {
    val str = username + Configuration.getToken() + addressMail
    val md = java.security.MessageDigest.getInstance("SHA-1")
    return new sun.misc.BASE64Encoder().encode(md.digest(str.getBytes))
  }
}