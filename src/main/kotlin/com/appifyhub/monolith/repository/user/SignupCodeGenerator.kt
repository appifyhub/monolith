package com.appifyhub.monolith.repository.user

import java.util.UUID

object SignupCodeGenerator {

  private const val UUID_DELIMITER = "-"
  private const val SIGNUP_CODE_LENGTH = 12

  val nextCode: String
    get() = interceptor() ?: UUID.randomUUID()
      .toString()
      .replace(UUID_DELIMITER, "")
      .uppercase()
      .take(SIGNUP_CODE_LENGTH)
      .chunked(4)
      .joinToString(UUID_DELIMITER)

  var interceptor: () -> String? = { null }

}
