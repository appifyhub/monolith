package com.appifyhub.monolith.domain.messaging

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

  ;

}
