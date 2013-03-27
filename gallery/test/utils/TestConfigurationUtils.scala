package utils
import org.specs2.mutable.Specification

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