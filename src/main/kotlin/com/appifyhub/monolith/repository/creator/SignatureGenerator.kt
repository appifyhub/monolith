package com.appifyhub.monolith.repository.creator

import java.util.UUID

object SignatureGenerator {

  private const val UUID_DELIMITER = "-"

  val nextSignature: String
    get() = interceptor() ?: UUID.randomUUID()
      .toString()
      .replace(UUID_DELIMITER, "")
      .uppercase()

  var interceptor: () -> String? = { null }

}
