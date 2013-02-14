package utils
import org.specs2.mutable.Specification

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