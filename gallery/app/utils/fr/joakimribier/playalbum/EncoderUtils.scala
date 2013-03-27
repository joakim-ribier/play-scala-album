package utils.fr.joakimribier.playalbum

import org.apache.commons.codec.binary.Base64

object EncoderUtils {

  private val _BASE64_VALUE_62 = "+"
  private val _BASE64_VALUE_63 = "/"
  private val _EMPTY = ""
    
  def generateTokenForEmailValidation(username: String, addressMail: String) : String = {
    return generateTokenForEmailValidation(username, addressMail, ConfigurationUtils.getToken())
  }
  
  def generateTokenForEmailValidation(username: String, addressMail: String, token: String) : String = {
    checkParameters(username, addressMail, token)
    val str = trim(username) + token + trim(addressMail)
    val md = java.security.MessageDigest.getInstance("SHA-1")
    val generatedToken = new String(new Base64().encode(str.getBytes()))
    return replaceBase64Values(generatedToken)
  }
  
  private def trim(str: String) = if(str == null) "" else str.trim()
  
  private def replaceBase64Values(token: String) : String = {
    if (token == null) {
      throw new IllegalArgumentException("token is required")
    }
    return token.replace(_BASE64_VALUE_62, _EMPTY).replace(_BASE64_VALUE_63, _EMPTY)
  }
  
  private def checkParameters(params: String*) {
    for (param <- params) {
      if (param == null || param.isEmpty()) {
        throw new IllegalArgumentException("paramerter is required")
      }
    }
  }
} 