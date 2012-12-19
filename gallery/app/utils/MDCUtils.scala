package utils

import org.slf4j.MDC

object MDCUtils {
  
  def getOrOpenSession(username: String, sessionId: String) = {
    MDC.put("username", username)
		MDC.put("sessionId", sessionId);
  }
  
  def closeSession() = {
  	MDC.clear()
  }
  
}