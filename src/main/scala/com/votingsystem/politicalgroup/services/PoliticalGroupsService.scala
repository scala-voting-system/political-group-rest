package com.votingsystem.politicalgroup.services

import cats.effect.MonadCancelThrow
import cats.implicits._
import com.votingsystem.politicalgroup.models.{ PoliticalGroup, PoliticalGroupFilter, PoliticalGroupRequest }
import com.votingsystem.politicalgroup.utils.pagination.Pagination
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import com.votingsystem.politicalgroup.utils.LoggerSyntax._
import org.typelevel.log4cats.Logger

import java.time.LocalDateTime
import java.util.UUID

trait PoliticalGroupsService[F[_]] {
  def create(politicalGroupRequest: PoliticalGroupRequest): F[UUID]
  def all(pagination: Pagination): F[List[PoliticalGroup]]
  def filterPoliticalGroups(filter: PoliticalGroupFilter, pagination: Pagination): F[List[PoliticalGroup]]
  def find(id: UUID): F[Option[PoliticalGroup]]
  def update(id: UUID, politicalGroupRequest: PoliticalGroupRequest): F[Option[PoliticalGroup]]
  def delete(id: UUID): F[Int]
}

class LivePoliticalGroupsService[F[_]: MonadCancelThrow: Logger] private (xa: Transactor[F])
    extends PoliticalGroupsService[F] {
  override def create(politicalGroupRequest: PoliticalGroupRequest): F[UUID] = {
    val createdAt: LocalDateTime = LocalDateTime.now()
    sql"""
         INSERT INTO political_groups(
          name,
          created_at,
          updated_at,
          is_active
         ) VALUES (
          ${politicalGroupRequest.name},
          ${createdAt},
          ${createdAt},
          ${true}
         )
       """.update.withUniqueGeneratedKeys[UUID]("id").transact(xa)
  }

  override def all(pagination: Pagination): F[List[PoliticalGroup]] =
    sql"""
         SELECT
          id,
          name,
          created_at,
          updated_at,
          is_active
        FROM political_groups
        WHERE is_active = true
        ORDER BY id LIMIT ${pagination.limit} OFFSET ${pagination.offset}
       """.query[PoliticalGroup].to[List].transact(xa)

  override def filterPoliticalGroups(filter: PoliticalGroupFilter, pagination: Pagination): F[List[PoliticalGroup]] = {
    val selectFragment: Fragment =
      fr"""
         SELECT
          id,
          name,
          created_at,
          updated_at,
          is_active
        """

    val fromFragment: Fragment =
      fr"FROM political_groups"

    val whereFragment: Fragment = {
      fr"WHERE is_active = true" ++
      Fragments.whereAndOpt(filter.name.map(name => fr"name LIKE '%$name%'"))
    }

    val paginationFragment: Fragment =
      fr"ORDER BY id LIMIT ${pagination.limit} OFFSET ${pagination.offset}"

    val statement = selectFragment |+| fromFragment |+| whereFragment |+| paginationFragment

    Logger[F].info(statement.toString) *>
    statement.query[PoliticalGroup].to[List].transact(xa).logError(error => s"Failed query: $error")
  }

  override def find(id: UUID): F[Option[PoliticalGroup]] =
    sql"""
         SELECT
          id,
          name,
          created_at,
          updated_at,
          is_active
        FROM political_groups
        WHERE id = $id AND is_active = true
       """.query[PoliticalGroup].option.transact(xa)

  override def update(id: UUID, politicalGroupRequest: PoliticalGroupRequest): F[Option[PoliticalGroup]] = {
    val updatedAt: LocalDateTime = LocalDateTime.now()
    sql"""
         UPDATE political_groups
         SET
            name = ${politicalGroupRequest.name},
            updated_at = ${updatedAt}
         WHERE id = ${id}
       """.update.run.transact(xa).flatMap(_ => find(id))
  }

  override def delete(id: UUID): F[Int] = {
    val updatedAt: LocalDateTime = LocalDateTime.now()
    sql"""
         UPDATE political_groups
         SET
            updated_at = ${updatedAt},
            is_active = ${false}
         WHERE id = ${id}
       """.update.run.transact(xa)
  }
}

object LivePoliticalGroupsService {
  implicit val politicalGroupRead: Read[PoliticalGroup] =
    Read[(UUID, String, Option[LocalDateTime], Option[LocalDateTime], Option[Boolean])].map {
      case (
          id: UUID,
          name: String,
          createdAt: Option[LocalDateTime],
          updatedAt: Option[LocalDateTime],
          isActive: Option[Boolean]
          ) =>
        PoliticalGroup(id, name, createdAt, updatedAt, isActive)
    }

  def apply[F[_]: MonadCancelThrow: Logger](xa: Transactor[F]): F[LivePoliticalGroupsService[F]] =
    new LivePoliticalGroupsService[F](xa).pure[F]
}
