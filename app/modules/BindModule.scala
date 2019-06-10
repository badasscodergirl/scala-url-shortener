package modules

import com.google.inject.{AbstractModule, Singleton}
import service.models.DbUtils

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class BindModule extends AbstractModule {

  override def configure(): Unit = {
    DbUtils.checkAndCreateTables
  }
}
