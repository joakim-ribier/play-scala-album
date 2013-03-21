package utils

import org.slf4j.MDC

object MDCUtils {

  def put(sessionId: String) = MDC.put(Configuration._SESSION_ID_KEY, sessionId)
  
  def closeSession() = MDC.clear()
}