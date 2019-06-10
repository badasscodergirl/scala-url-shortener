package service.url_shortener

import java.security.MessageDigest
import java.util.Base64

object UrlHashingUtils {

  private val MD5 = "MD5"

  def generateHash(str: String, start: Int, end: Int): String =
    Base64.getUrlEncoder.encodeToString(MessageDigest.getInstance(MD5).digest(str.getBytes)).substring(start, end)
}
