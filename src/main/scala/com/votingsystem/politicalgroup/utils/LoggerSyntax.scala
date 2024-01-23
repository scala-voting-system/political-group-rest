package com.votingsystem.politicalgroup.utils

import cats._
import cats.implicits._
import org.typelevel.log4cats.Logger

object LoggerSyntax {
  implicit class LogEffect[F[_], E, A](fa: F[A])(implicit me: MonadError[F, E], logger: Logger[F]) {
    def log(success: A => String, error: E => String): F[A] = fa.attemptTap {
      case Left(e)  => logger.error(error(e))
      case Right(a) => logger.info(success(a))
    }

    def logError(error: E => String): F[A] = fa.attemptTap {
      case Left(e)  => logger.error(error(e))
      case Right(_) => ().pure[F]
    }
  }
}
