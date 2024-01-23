package com.votingsystem.politicalgroup.validation

import cats.MonadThrow
import cats.data.Validated._
import cats.implicits._
import com.votingsystem.politicalgroup.responses.FailureResponse
import com.votingsystem.politicalgroup.validation.validators.{ ValidationResult, Validator }
import com.votingsystem.politicalgroup.utils.LoggerSyntax._
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.{ EntityDecoder, Request, Response }
import org.typelevel.log4cats.Logger

object Validator {
  private def validateEntity[A](entity: A)(implicit validator: Validator[A]): ValidationResult[A] =
    validator.validate(entity)

  implicit class ValidateEntity[F[_]: MonadThrow: Logger](req: Request[F]) extends Http4sDsl[F] {
    def validateAs[A: Validator](
      serverLogicIfValid: A => F[Response[F]]
    )(implicit entityDecoder: EntityDecoder[F, A]): F[Response[F]] = {
      req.as[A].logError(e => s"Parsing payload failed: $e").map(entity => validateEntity(entity)).flatMap {
        case Valid(entity) =>
          serverLogicIfValid(entity)
        case Invalid(errors) =>
          BadRequest(FailureResponse(errors.toList.map(_.errorMessage).mkString(",")))
      }
    }
  }
}
