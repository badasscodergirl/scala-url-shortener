package service.url_shortener

import service.url_shortener.UrlDefinitions.UrlProtocol.HTTP
import service.url_shortener.UrlDefinitions.{UrlInfo, UrlProtocol, ValidUrlSummary}


//TODO need a lot of improvement
object UrlValidationUtils {

  def withShortUrlValidation: String => UrlInfo = { url =>
    val pattern = """([http|https]+):\/\/(\w+\.{1}\w+)+\/{1}(\w+)""".r //todo change
    val noProtocolPattern = """(\w+\.{1}\w+)+\/{1}(\w+)""".r //todo change

    val urlTrimmed = url.trim
    if (urlTrimmed.isEmpty) throw new Exception("empty")

    val (protocol, domain, shortCode) = urlTrimmed match {
      case pattern(protocol, domain, shortCode) => (UrlProtocol.fromString(protocol), domain, shortCode)
      case noProtocolPattern(domain, shortCode) => (HTTP, domain, shortCode)
      case _ => throw new Exception("No match")
    }
    val urlString = s"$protocol://$domain/$shortCode"
    val validSummary = ValidUrlSummary(protocol, domain, Some(shortCode), Map.empty, None, urlString)
    UrlInfo(urlTrimmed, Some(validSummary))
  }

  def withLongUrlValidation: String => UrlInfo = { url =>
    //val pattern = """([http|https]+):\/\/(\w+\.{1}\w+)+(\/{1}\w*)*(\?{1}\w*=?{1}\w*[&\w=]*)?""".r
    val pattern = """([http|https]+):\/\/(\w+\.{1}\w+)+(\/{1}\w*)*([\?\w*=\&]*)?""".r
    val noProtocolPattern = """(\w+\.{1}\w+)+(\/{1}\w*)*([\?\w*=\&]*)?""".r

    val urlTrimmed = url.trim
    if (urlTrimmed.isEmpty) throw new Exception("empty")

    val (protocol, domain, pathOpt, qsOpt) = urlTrimmed match {
      case pattern(protocol, domain, path, queryStr, _*) =>
        (UrlProtocol.fromString(protocol), domain, Some(path), Some(queryStr))

      case pattern(protocol, domain, path, _*) =>
        (UrlProtocol.fromString(protocol), domain, Some(path), None)

      case pattern(protocol, domain, _*) => (UrlProtocol.fromString(protocol), domain, None, None)
      case noProtocolPattern(domain, _*) => (HTTP, domain, None, None)
      case _ => throw new Exception("No match")
    }

    val validSummary = ValidUrlSummary(protocol, domain, pathOpt, Map.empty, qsOpt, urlTrimmed)
    UrlInfo(urlTrimmed, Some(validSummary))
  }

}
