package com.appifyhub.monolith.features.creator.domain.model.messaging

enum class Variable(
  val code: String,
  val example: String,
) {

  USER_NAME(
    code = "user.name",
    example = "Mark Watson",
  ),

  PROJECT_NAME(
    code = "project.name",
    example = "Mortgage Calculator",
  ),

  VERIFICATION_CODE(
    code = "user.code",
    example = "123456",
  ),

  SIGNATURE(
    code = "user.signature",
    example = "123456",
  ),

}
