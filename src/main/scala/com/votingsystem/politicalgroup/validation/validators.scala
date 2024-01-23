package com.votingsystem.politicalgroup.validation

import cats.data.ValidatedNel
import cats.implicits._
import com.votingsystem.politicalgroup.models.PoliticalGroupRequest

object validators {

  sealed class ValidationFailure(val errorMessage: String)
  case class EmptyField(fieldName: String) extends ValidationFailure(s"'$fieldName' is empty.")
  type ValidationResult[A] = ValidatedNel[ValidationFailure, A]

  trait Validator[A] {
    def validate(value: A): ValidationResult[A]
  }

  private def validateRequired[A](field: A, fieldName: String)(required: A => Boolean): ValidationResult[A] =
    if (required(field)) field.validNel
    else EmptyField(fieldName).invalidNel

  implicit val politicalGroupRequestValidator: Validator[PoliticalGroupRequest] =
    (politicalGroupRequest: PoliticalGroupRequest) => {
      val PoliticalGroupRequest(name, members) = politicalGroupRequest

      val validName = validateRequired(name, "name")(_.exists(_.nonEmpty))

      (validName, members.validNel).mapN(PoliticalGroupRequest.apply)
    }
}
