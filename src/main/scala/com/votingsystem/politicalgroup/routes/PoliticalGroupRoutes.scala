package com.votingsystem.politicalgroup.routes

import cats.effect.Concurrent
import cats.implicits._
import com.votingsystem.politicalgroup.models.{ PoliticalGroupFilter, PoliticalGroupRequest }
import com.votingsystem.politicalgroup.responses.FailureResponse
import com.votingsystem.politicalgroup.services.PoliticalGroupsService
import org.http4s.HttpRoutes
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._
import org.typelevel.log4cats.Logger
import com.votingsystem.politicalgroup.utils.LoggerSyntax._
import com.votingsystem.politicalgroup.utils.pagination.{ LimitQueryParam, OffsetQueryParam, Pagination }
import com.votingsystem.politicalgroup.validation.Validator.ValidateEntity

class PoliticalGroupRoutes[F[_]: Concurrent: Logger] private (politicalGroupsService: PoliticalGroupsService[F])
    extends Http4sDsl[F] {

  private val allPoliticalGroupsRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? OffsetQueryParam(offset) +& LimitQueryParam(limit) =>
      for {
        politicalGroups <- politicalGroupsService.all(Pagination(offset, limit)).logError(error => s"Failure: $error")
        result <- Ok(politicalGroups)
      } yield result
  }

  private val filterPoliticalGroupsRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "filter" :? OffsetQueryParam(offset) +& LimitQueryParam(limit) =>
      for {
        filter <- req.as[PoliticalGroupFilter].logError(error => s"Filter failure: $error")
        politicalGroups <- politicalGroupsService
          .filterPoliticalGroups(filter, Pagination(offset, limit))
          .logError(error => s"Failure: $error")
        result <- Ok(politicalGroups)
      } yield result
  }

  private val findPoliticalGroupRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / UUIDVar(id) =>
      politicalGroupsService.find(id).logError(error => s"Failure: $error").flatMap {
        case Some(politicalGroup) => Ok(politicalGroup)
        case None                 => NotFound(FailureResponse(s"Political Group with id=$id not found."))
      }
  }

  private val createPoliticalGroupRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root =>
      req.validateAs[PoliticalGroupRequest] { politicalGroupRequest =>
        for {
          _ <- Logger[F].info(s"Inserting Political Group into DB")
          createdPoliticalGroupId <- politicalGroupsService
            .create(politicalGroupRequest)
            .log(_ => "Record inserted into DB successfully", error => s"Saving record into DB failed: $error")
          result <- Created(createdPoliticalGroupId)
        } yield result
      }
  }

  private val updatePoliticalGroupRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ PATCH -> Root / UUIDVar(id) =>
      req.validateAs[PoliticalGroupRequest] { politicalGroupRequest =>
        for {
          maybePoliticalGroup <- politicalGroupsService
            .update(id, politicalGroupRequest)
            .log(_ => "Record updated into DB successfully", error => s"Updating record into DB failed: $error")
          result <- maybePoliticalGroup match {
            case Some(_) => Ok()
            case None    => NotFound(FailureResponse(s"Political Group with id=$id not found."))
          }
        } yield result
      }
  }

  private val deletePoliticalGroupRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case DELETE -> Root / UUIDVar(id) =>
      politicalGroupsService.find(id).flatMap {
        case Some(_) =>
          for {
            _ <- politicalGroupsService
              .delete(id)
              .log(_ => "Record deleted from DB successfully", error => s"Deleting record from DB failed: $error")
            result <- Ok()
          } yield result

        case None => NotFound(FailureResponse(s"Political Group with id=$id not found."))
      }
  }

  val routes: HttpRoutes[F] = Router(
    "/political-groups" -> (
      allPoliticalGroupsRoute <+>
      filterPoliticalGroupsRoute <+>
      findPoliticalGroupRoute <+>
      createPoliticalGroupRoute <+>
      updatePoliticalGroupRoute <+>
      deletePoliticalGroupRoute
    )
  )
}

object PoliticalGroupRoutes {
  def apply[F[_]: Concurrent: Logger](politicalGroupsService: PoliticalGroupsService[F]) =
    new PoliticalGroupRoutes[F](politicalGroupsService)
}
