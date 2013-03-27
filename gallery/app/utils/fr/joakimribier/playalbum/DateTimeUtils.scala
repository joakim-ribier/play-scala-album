package utils.fr.joakimribier.playalbum

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.DateTimeFormat

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