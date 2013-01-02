package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import play.api.Logger
import play.api.i18n.Lang
import java.io.InputStream

object JavaScriptController extends Controller {

  private val _DEFAULT_RETURN = ""
    
  def i18n(lang: String) = Action { request =>
    val file = formatMessagesLangFile(lang)
    if (file != null) {
	    Logger.info("Loaded i18n " + file)
	    var properties = _DEFAULT_RETURN
	    try {
	      val in: InputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(file)
	      properties = new java.util.Scanner(in).useDelimiter("\\A").next();
	    } catch {
	      case e => {
	        Logger.error("Failed to read messages file")
	      }
	    }
	    
	    Ok(properties).as("text/plain")  
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