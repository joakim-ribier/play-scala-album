package utils.fr.joakimribier.playalbum

import org.specs2.mutable.Specification

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
class TestConfigurationUtils extends Specification {

  "Media formats allowed" should {
    
    "split by ',' separator ('jpg,webm')" in {
      ConfigurationUtils.getMediaFormatsAllowed("jpg,webm") must containAllOf(Seq("jpg", "webm")).inOrder
      ConfigurationUtils.getMediaFormatsAllowed("jpg#webm") must containAllOf(Seq("jpg#webm")).only
      ConfigurationUtils.getMediaFormatsAllowed(null) must empty
    }
    
    "is media format allowed" in {
      val formats = Seq("jpg", "webm")
      ConfigurationUtils.isMediaFormatAllowed(null, "jpg") must beFalse
      ConfigurationUtils.isMediaFormatAllowed(formats, null) must beFalse
      ConfigurationUtils.isMediaFormatAllowed(null, null) must beFalse
      
      ConfigurationUtils.isMediaFormatAllowed(formats, "jpg") must beTrue
      ConfigurationUtils.isMediaFormatAllowed(formats, "JPG") must beTrue
      ConfigurationUtils.isMediaFormatAllowed(formats, " webm ") must beTrue
      
      ConfigurationUtils.isMediaFormatAllowed(formats, "jpeg") must beFalse
    }
  }
}