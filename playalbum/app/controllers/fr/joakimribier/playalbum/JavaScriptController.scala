package controllers.fr.joakimribier.playalbum

import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

import org.slf4j.LoggerFactory

import play.api.mvc.Action
import play.api.mvc.Controller

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