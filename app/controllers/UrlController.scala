package controllers

import com.google.inject.Singleton
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import service.url_shortener.UrlDefinitions.{LongUrl, ShortUrl}
import service.url_shortener.UrlShortenerService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

@Singleton
class UrlController @Inject()(cc: ControllerComponents,
                              urlShortenerService: UrlShortenerService) extends AbstractController(cc) {

  private val logger = Logger.logger

  def shorten = Action.async(parse.json) { request =>
    val longUrlStr =  (request.body \ "url").as[String]
    try {
      val longUrl = LongUrl.apply(longUrlStr)
      urlShortenerService.shortenUrl(longUrl).map { r =>
        Ok(Json.toJson(r.url))
      }
    } catch {
      case t: Throwable =>
        logger.error(s"Error while finding long url for $longUrlStr", t)
        Future.successful(BadRequest)
    }
    /*Try {
      val longUrl = LongUrl.apply(longUrlStr)
      urlShortenerService.shortenUrl(longUrl)
    }.toEither match {
      case Left(t: Throwable) =>
        logger.error(s"Error while finding long url for $longUrlStr", t)
        Future.successful(BadRequest)
      case Right(resultFuture) => resultFuture.map(r => Ok(Json.toJson(r.url)))
    }*/
  }

  def findLongUrl(suOpt: Option[String]) = Action.async {
    try {
      suOpt match {
        case Some(su) =>
          val shortUrl = ShortUrl.apply(su)
          urlShortenerService.getLongUrl(shortUrl).map { r =>
            Ok(Json.toJson(r.url))
          }
        case None =>
          Future.successful(Ok)
      }
    } catch {
      case t: Throwable =>
        logger.error(s"Error while finding long url for $suOpt", t)
        Future.successful(BadRequest)
    }
  }


}
