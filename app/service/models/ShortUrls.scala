package service.models

import java.sql.Timestamp

import org.joda.time.DateTime
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import ShortUrls._
import service.url_shortener.UrlDefinitions.UrlProtocol


case class ShortUrlEntry(id: Long, longUrl: String, shortUrlDomain: String, shortCode: String, createdAt: DateTime)

class ShortUrls(tag: Tag) extends Table[ShortUrlEntry](tag, ShortUrls.tableName) {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def longUrl = column[String]("long_url")
  def shortUrlDomain = column[String]("short_url_domain")
  //def protocol = column[UrlProtocol]("protocol")
  def shortCode = column[String]("short_code")
  def createdAt = column[DateTime]("created_at")

  override def * = (id, longUrl, shortUrlDomain, shortCode, createdAt) <>
    (ShortUrlEntry.tupled, ShortUrlEntry.unapply)
}

object ShortUrls {

  val tableName = "short_urls"
  lazy val query = TableQuery[ShortUrls]

  implicit val dateTimeDBMapping: BaseColumnType[DateTime] =
    MappedColumnType.base[DateTime, Timestamp](d => new Timestamp(d.getMillis), t => new DateTime(t.getTime))
}


