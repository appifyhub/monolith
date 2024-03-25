package com.appifyhub.monolith.features.user.repository.util

import java.util.UUID
import kotlin.math.pow

object TokenGenerator {

  private const val UUID_DELIMITER = "-"
  private const val EMAIL_TOKEN_LENGTH = 16
  private const val PHONE_TOKEN_LENGTH = 6

  private val maxPhoneToken = 10.0.pow(PHONE_TOKEN_LENGTH.toDouble()).toInt()

  val nextEmailToken: String
    get() = emailInterceptor() ?: UUID.randomUUID()
      .toString()
      .replace(UUID_DELIMITER, "")
      .uppercase()
      .take(EMAIL_TOKEN_LENGTH)

  val nextPhoneToken: String
    get() = phoneInterceptor() ?: UUID.randomUUID()
      .toString()
      .split(UUID_DELIMITER)
      .last()
      .toLong(16)
      .rem(maxPhoneToken)
      .let { "%0${PHONE_TOKEN_LENGTH}d".format(it) }

  var emailInterceptor: () -> String? = { null }
  var phoneInterceptor: () -> String? = { null }

}
