package service.url_shortener

import java.security.MessageDigest
import java.util.Base64

import com.google.inject.{ImplementedBy, Singleton}
import org.joda.time.DateTime
import service.models.{DbUtils, ShortUrlEntry, ShortUrls}
import service.url_shortener.UrlDefinitions.UrlProtocol.HTTP
import service.url_shortener.UrlDefinitions.{LongUrl, ShortUrl}
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[UrlShortenerServiceImpl])
trait UrlShortenerService {

  def shortenUrl(longUrl: LongUrl): Future[ShortUrl]

  def getLongUrl(shortUrl: ShortUrl): Future[LongUrl]

}

@Singleton
class UrlShortenerServiceImpl extends UrlShortenerService {

  private val db = DbUtils.db


 import UrlShortenerService._

  private val shortUrlsQuery = ShortUrls.query

  override def shortenUrl(longUrl: LongUrl): Future[ShortUrl] = {
    DbUtils.db.run(getOrCreateShortUrlDBIO(longUrl).map(_.url)).map(ShortUrl.apply)
  }

  override def getLongUrl(shortUrl: ShortUrl): Future[LongUrl] = {
    DbUtils.db.run(shortUrlsQuery.filter(_.shortCode === shortUrl.shortCode).map(_.longUrl)
      .result.headOption.map(_.getOrElse {
      throw new Exception(s"No data found for $shortUrl")
    })).map(LongUrl.apply)
  }

  private def recursiveCodeGenerator(str: String, start: Int, end: Int): DBIO[String] = {
    val newShortCode = UrlHashingUtils.generateHash(str, start, end)
    for {
      exists <- shortUrlsQuery.filter(_.shortCode === newShortCode).exists.result
      result <- if(exists) recursiveCodeGenerator(str, start + 1, end + 1)
      else DBIO.successful(newShortCode)
    } yield result
  }

  private def getOrCreateShortUrlDBIO(longUrl: LongUrl): DBIO[ShortUrl] = for {
    shortUrlOpt <- byLongUrlDBIO(longUrl.url)
    shortUrl <- shortUrlOpt match {
      case Some(shortUrl) => DBIO.successful(ShortUrl(HTTP, shortUrl.shortUrlDomain, shortUrl.shortCode))
      case None =>
        for {
          newShortCode <- recursiveCodeGenerator(longUrl.url, 0, shortCodeLength - 1)
          _ <- shortUrlsQuery  += ShortUrlEntry(-1l, longUrl.url, domain, newShortCode, DateTime.now)
        } yield ShortUrl(HTTP, domain, newShortCode)
    }
  } yield shortUrl

  private def byLongUrlDBIO(longUrl: String) =
    shortUrlsQuery.filter(_.longUrl === longUrl).result.headOption
}

object UrlShortenerService {
  final val domain = "bcg.co"
  final val shortCodeLength = 7
}