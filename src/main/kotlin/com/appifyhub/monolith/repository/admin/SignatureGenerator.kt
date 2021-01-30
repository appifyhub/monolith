package com.appifyhub.monolith.repository.admin

import com.appifyhub.monolith.util.ext.empty
import java.util.UUID

object SignatureGenerator {

  private const val UUID_DELIMITER = "-"

  val nextSignature: String
    get() = interceptor() ?: UUID.randomUUID()
      .toString()
      .replace(UUID_DELIMITER, String.empty)
      .toUpperCase()

  var interceptor: () -> String? = { null }

}