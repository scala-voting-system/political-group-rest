package com.votingsystem.politicalgroup

import cats.effect.{IO, IOApp}
import com.votingsystem.politicalgroup.configs.AppConfig
import com.votingsystem.politicalgroup.modules.{Database, HttpApi, LiveService}
import org.http4s.ember.server.EmberServerBuilder
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderException

object Main extends IOApp.Simple {

  private val configSource = ConfigSource.default.load[AppConfig]
  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run: IO[Unit] = {
    configSource match {
      case Left(errors)  => IO.raiseError[Nothing](ConfigReaderException(errors))
      case Right(config) => initiateHttpServer(config)
    }
  }

  private def initiateHttpServer(config: AppConfig): IO[Nothing] = {
    val serverResource =
      for {
        databaseXa <- Database.makeDbResource[IO](config.dbConfig)
        liveService <- LiveService[IO](databaseXa)
        httpApi <- HttpApi[IO](liveService)
        server <- EmberServerBuilder
          .default[IO]
          .withHost(config.emberConfig.host)
          .withPort(config.emberConfig.port)
          .withHttpApp(httpApi.endpoints.orNotFound)
          .build
      } yield server

    serverResource.use(_ => logger.info("Initiated REST service") *> IO.never)
  }
}
