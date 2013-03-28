package controllers.fr.joakimribier.playalbum

import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

import org.slf4j.LoggerFactory

import play.api.mvc.Action
import play.api.mvc.Controller

object JavaScriptController extends Controller {

  private val Logger = LoggerFactory.getLogger("JavaScriptController")
  
  private val _DEFAULT_RETURN = ""
    
  def i18n(lang: String) = Action { request =>
    val file = formatMessagesLangFile(lang)
    if (file != null) {
	    var properties = _DEFAULT_RETURN
	    try {
	      val in: InputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(file)
	      val inr: InputStreamReader = new InputStreamReader(in, Charset.forName("UTF-8"))
	      properties = new java.util.Scanner(inr).useDelimiter("\\A").next();
	    } catch {
	      case e => {
	        Logger.error("Failed to read messages file")
	      }
	    }
	    
	    Ok(properties).as("text/plain; charset=utf-8")
    } else {
      Ok(_DEFAULT_RETURN).as("text/plain")
    }
  }
  
  private def formatMessagesLangFile(lang: String) : String = {
    if (lang == null || lang.isEmpty()) {
      return null
    }

    val tab = lang.split("_")
    if (tab.size < 2) {
    	return null
    }
    
    return "messages." + tab(1).substring(0, 2)
  }

}