package utils.fr.joakimribier.playalbum

import org.apache.commons.codec.binary.Base64

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
object EncoderUtils {

  private val _BASE64_VALUE_62 = "+"
  private val _BASE64_VALUE_63 = "/"
  private val _EMPTY = ""
    
  def generateTokenForEmailValidation(username: String, addressMail: String) : String = {
    return generateTokenForEmailValidation(username, addressMail, ConfigurationUtils.getToken)
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