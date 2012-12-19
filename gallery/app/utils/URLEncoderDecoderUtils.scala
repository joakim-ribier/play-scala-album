package utils

object URLEncoderDecoderUtils {

  def encode(param: String) = java.net.URLEncoder.encode(param, "UTF-8")
  
  def decode(param: String) = java.net.URLDecoder.decode(param, "UTF-8")
}