package com.votingsystem.politicalgroup.models

import java.time.LocalDateTime
import java.util.UUID

case class PoliticalGroup(
  id: UUID,
  name: String,
  createdAt: Option[LocalDateTime],
  updatedAt: Option[LocalDateTime],
  isActive: Option[Boolean]
)

case class PoliticalGroupRequest(
  name: Option[String],
  // TODO refactor to use proper case class
  members: Option[List[UUID]]
)

final case class PoliticalGroupFilter(name: Option[String])
