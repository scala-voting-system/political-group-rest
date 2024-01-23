package com.votingsystem.politicalgroup.modules

import cats.effect.{ Concurrent, Resource }
import cats.implicits._
import com.votingsystem.politicalgroup.routes.{ ManagementRoutes, PoliticalGroupRoutes }
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.typelevel.log4cats.Logger

class HttpApi[F[_]: Concurrent: Logger] private (dbService: LiveService[F]) {
  private val managementRoutes = ManagementRoutes[F].routes
  private val politicalGroupRoutes = PoliticalGroupRoutes[F](dbService.politicalGroupsService).routes

  val endpoints: HttpRoutes[F] = Router("/api" -> (managementRoutes <+> politicalGroupRoutes))
}

object HttpApi {
  def apply[F[_]: Concurrent: Logger](dbService: LiveService[F]): Resource[F, HttpApi[F]] =
    Resource.pure(new HttpApi[F](dbService))
}
