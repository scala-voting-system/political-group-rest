package com.votingsystem.politicalgroup.modules

import cats.effect.{ Async, Resource }
import com.votingsystem.politicalgroup.configs.DbConfig
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

object Database {
  def makeDbResource[F[_]: Async](config: DbConfig): Resource[F, HikariTransactor[F]] =
    for {
      ec <- ExecutionContexts.fixedThreadPool(config.nThreads)
      xa <- HikariTransactor
        .newHikariTransactor[F](config.driver, s"${config.host}:${config.dbName}", config.user, config.password, ec)
    } yield xa
}
