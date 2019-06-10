package service.url_shortener

import service.url_shortener.UrlValidationUtils._

object UrlDefinitions {

  sealed trait UrlProtocol {
    override def toString: String = asString

    def asString: String
  }
  object UrlProtocol {

    case object HTTP extends UrlProtocol {
      override def asString: String = "http"
    }

    case object HTTPS extends UrlProtocol {
      override def asString: String = "https"
    }

    private def toProtocol(s: String): UrlProtocol = all.collectFirst {
      case u if u.asString == s => u
    }.getOrElse(throw new Exception)

    def fromString(s: String): UrlProtocol = if (s.isEmpty) HTTP else toProtocol(s.toLowerCase)

    def all = Set(HTTP, HTTPS)
  }

  sealed trait Url {
    override def toString: String = url

    def url: String
  }
  object Url {
    //private def fromString[T <: Url](urlInfo: UrlInfo): T
  }
  case class ShortUrl(url: String, domain: String, protocol: UrlProtocol, shortCode: String) extends Url

  //TODO Can use UrlLike
  object ShortUrl {

    def apply(protocol: UrlProtocol, domain: String, shortCode: String): ShortUrl =
      ShortUrl(s"$protocol://$domain/$shortCode", domain, protocol, shortCode)

    def apply(urlStr: String) =
      applyWithValidation[ShortUrl](urlStr)(withShortUrlValidation)(urlInfo => getShortUrl(urlInfo))

    private def getShortUrl(urlInfo: UrlInfo): ShortUrl =
      urlInfo.urlSummary.fold(throw InvalidUrl(urlInfo.rawUrl)) { urlSummary =>
        urlSummary.path.fold(throw InvalidUrl(urlInfo.rawUrl)) { path =>
          ShortUrl(urlSummary.protocol, urlSummary.domain, path)
      }
    }
  }

  case class LongUrl(url: String, domain: String, protocol: UrlProtocol) extends Url

  //TODO Can use UrlLike
  object LongUrl {
    def apply(urlStr: String) =
      applyWithValidation[LongUrl](urlStr)(withLongUrlValidation)(urlInfo => getLongUrl(urlInfo))

    private def getLongUrl(urlInfo: UrlInfo): LongUrl =
      urlInfo.urlSummary.fold(throw InvalidUrl(urlInfo.rawUrl)) { urlSummary =>
        LongUrl(urlSummary.validUrl, urlSummary.domain, urlSummary.protocol)
      }
  }
  private[url_shortener] case class ValidUrlSummary(protocol: UrlProtocol, domain: String, path: Option[String],
                                     queryParams: Map[String, String], queryStr: Option[String], validUrl: String)

  private[url_shortener] case class UrlInfo(rawUrl: String, urlSummary: Option[ValidUrlSummary])


  private def applyWithValidation[T](url: String)
                                    (validUrl: String => UrlInfo)
                                    (fromStr: UrlInfo => T) = {
    val uInfo = validUrl(url)
    fromStr(uInfo)
  }

  case class InvalidUrl(url: String) extends Exception(url)
}
