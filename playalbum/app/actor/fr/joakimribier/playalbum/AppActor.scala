package actor.fr.joakimribier.playalbum

import org.slf4j.LoggerFactory
import akka.actor.Actor
import controllers.fr.joakimribier.playalbum.SendMailController
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import utils.fr.joakimribier.playalbum.DateTimeUtils

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
class AppActor extends Actor {
  
  private val Logger = LoggerFactory.getLogger("ApplicationActor")
  
  def receive = {
    case "sendcomments" => SendMailController.comments
    case "sendnewmedia" => {
      val is = DateTimeUtils.isDayAndHour(
          Option.apply(DateTime.now()),
          Option.apply(DateTimeConstants.SATURDAY),
          Option.apply(0))
      if (is) {
        SendMailController.notifyNewPhoto
      }
    }
    case _ => Logger.info("received unknown message")
  }
}
