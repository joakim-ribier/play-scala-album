package utils.fr.joakimribier.playalbum

object OrderEnum extends Enumeration {
  val ASC = new Value(1, "asc")
  val DESC = new Value(2, "desc")
  class Value(id:Int, value: String) extends Val(id, value) {
  	val dbId = id
    val label = value
  }
}

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