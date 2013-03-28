package utils.fr.joakimribier.playalbum

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.DateTimeFormat

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
object DateTimeUtils {
  
  private val PATTERN = "yyyy-MM-dd HH:mm:ss"
  
  def now = DateTime.now().toString(PATTERN)
  
  def convertToDateTime(dateTime: Option[String]) : DateTime = {
    if (!dateTime.isDefined) {
      throw new IllegalArgumentException("datetime field is required")
    }
    
    val formatter: DateTimeFormatter = DateTimeFormat.forPattern(PATTERN);
    return formatter.parseDateTime(dateTime.get);
  }
  
  def isAfterNowMinusMinutes(dateTime: Option[DateTime], minus: Option[Int]) : Boolean = {
    if (!dateTime.isDefined || !minus.isDefined) {
      throw new IllegalArgumentException("datetime and minus fields are required")
    }
    return dateTime.get.isAfter(DateTime.now().minusMinutes(minus.get))
  }
}