package com.votingsystem.politicalgroup.modules

import cats.effect.{Async, Resource}
import com.votingsystem.politicalgroup.services.{LivePoliticalGroupsService, PoliticalGroupsService}
import doobie.util.transactor.Transactor
import org.typelevel.log4cats.Logger

final class LiveService[F[_]] private (val politicalGroupsService: PoliticalGroupsService[F])

object LiveService {
  def apply[F[_]: Async: Logger](xa: Transactor[F]): Resource[F, LiveService[F]] = {
    for {
      livePoliticalGroupsService <- Resource.eval(LivePoliticalGroupsService[F](xa))
      // example of instantiating multiple services and pass it to the LiveService class constructor
      // service2 <- Resource.eval(LivePoliticalGroupsService[F](xa))
      dbService = new LiveService(livePoliticalGroupsService)
    } yield dbService
  }
}
