package com.appifyhub.monolith.repository.user

import java.util.UUID

object SignupCodeGenerator {

  private const val UUID_DELIMITER = "-"
  private const val SIGNUP_CODE_LENGTH = 12

  const val CODE_DELIMITER = UUID_DELIMITER

  val nextCode: String
    get() = interceptor() ?: UUID.randomUUID()
      .toString()
      .replace(UUID_DELIMITER, "")
      .uppercase()
      .take(SIGNUP_CODE_LENGTH)
      .chunked(4)
      .joinToString(CODE_DELIMITER)

  var interceptor: () -> String? = { null }

}
