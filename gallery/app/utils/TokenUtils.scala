package utils
import sun.misc.BASE64Encoder
import play.api.Logger

object TokenUtils {

  def validationAddressMail(username: String, addressMail: String) : String = {
    val str = trim(username) + Configuration.getToken() + trim(addressMail)
    val encoder: BASE64Encoder = new BASE64Encoder()
    return trim(new String(encoder.encodeBuffer(str.getBytes())))
  }
  
  private def trim(str: String) : String = {
    if (str == null) {
      return ""
    }
    return str.trim()
  }
}