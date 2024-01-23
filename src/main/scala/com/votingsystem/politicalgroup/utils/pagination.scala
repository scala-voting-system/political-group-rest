package com.votingsystem.politicalgroup.utils

import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher

object pagination {
  object OffsetQueryParam extends OptionalQueryParamDecoderMatcher[Int]("offset")
  object LimitQueryParam extends OptionalQueryParamDecoderMatcher[Int]("limit")

  final case class Pagination(offset: Int, limit: Int)

  object Pagination {
    private val DefaultPageSize = 20

    def apply(maybeOffset: Option[Int], maybeLimit: Option[Int]) =
      new Pagination(maybeOffset.getOrElse(0), maybeLimit.getOrElse(DefaultPageSize))
  }
}
