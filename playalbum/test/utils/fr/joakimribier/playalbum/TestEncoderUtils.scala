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
class TestEncoderUtils extends Specification {

  "Generate token for email validation" should {

    "generated token should not contains {+} and {/} special characters" in {
      val token = "@ǜ$$*ù'{[^ç#\\~Ê"
      val id = "dGVzdEDHnCQkKsO5J3tbXsOnI1xw4p0ZXN0QHBsYXkuY29t"
      val generatedToken = EncoderUtils.generateTokenForEmailValidation("test", "test@play.com", token)
      generatedToken must beEqualTo(id) and not be contain("+") and not be contain("/")
    }
    
    "login must be not null and not empty" in {
      val token = "@ǜ$$*ù'{[^ç#\\~Ê"
      EncoderUtils.generateTokenForEmailValidation(null, "test@play.com", token) must throwA[IllegalArgumentException]
      EncoderUtils.generateTokenForEmailValidation("", "test@play.com", token) must throwA[IllegalArgumentException]
    }
    
    "email must be not null and not empty" in {
      val token = "@ǜ$$*ù'{[^ç#\\~Ê"
      EncoderUtils.generateTokenForEmailValidation("test", null, token) must throwA[IllegalArgumentException]
      EncoderUtils.generateTokenForEmailValidation("test", "", token) must throwA[IllegalArgumentException]
    }
    
    "token must be not null and not empty" in {
      EncoderUtils.generateTokenForEmailValidation("test", "test@play.com", null) must throwA[IllegalArgumentException]
      EncoderUtils.generateTokenForEmailValidation("test", "test@play.com", "") must throwA[IllegalArgumentException]
    }
  }

}