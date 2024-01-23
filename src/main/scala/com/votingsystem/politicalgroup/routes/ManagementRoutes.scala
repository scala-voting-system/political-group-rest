package com.votingsystem.politicalgroup.routes

import cats.Monad
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

class ManagementRoutes[F[_]: Monad] private extends Http4sDsl[F] {
  private val healthRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root =>
      Ok("Service is up.")
  }

  val routes: HttpRoutes[F] = Router("health" -> healthRoutes)
}

object ManagementRoutes {
  def apply[F[_]: Monad] = new ManagementRoutes[F]
}
