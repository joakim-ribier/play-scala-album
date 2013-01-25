package utils
import org.specs2.mutable.Specification

class TestConfiguration extends Specification {

  "Media formats allowed" should {
    
    "split by ',' separator ('jpg,webm')" in {
      Configuration.getMediaFormatsAllowed("jpg,webm") must containAllOf(Seq("jpg", "webm")).inOrder
      Configuration.getMediaFormatsAllowed("jpg#webm") must containAllOf(Seq("jpg#webm")).only
      Configuration.getMediaFormatsAllowed(null) must empty
    }
    
    "is media format allowed" in {
      val formats = Seq("jpg", "webm")
      Configuration.isMediaFormatAllowed(null, "jpg") must beFalse
      Configuration.isMediaFormatAllowed(formats, null) must beFalse
      Configuration.isMediaFormatAllowed(null, null) must beFalse
      
      Configuration.isMediaFormatAllowed(formats, "jpg") must beTrue
      Configuration.isMediaFormatAllowed(formats, "JPG") must beTrue
      Configuration.isMediaFormatAllowed(formats, " webm ") must beTrue
      
      Configuration.isMediaFormatAllowed(formats, "jpeg") must beFalse
    }
  }
}