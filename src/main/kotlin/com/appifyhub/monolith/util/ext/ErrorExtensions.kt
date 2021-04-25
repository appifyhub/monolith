package com.appifyhub.monolith.util.ext

import com.appifyhub.monolith.validation.Normalizer
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

fun throwUnauthorized(
  message: () -> Any = { "Not authorized to perform this action" },
): Nothing = throw ResponseStatusException(HttpStatus.UNAUTHORIZED, message().toString())

fun throwNormalization(
  message: () -> Any = { "Data is invalid" },
): Nothing = throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, message().toString())

fun <T> Normalizer.Result<T>.requireValid(
  propName: () -> Any = { "Property" },
) = if (!isValid) throwNormalization { "${propName()} '$value' is invalid" } else value

inline fun <R> silent(log: Boolean = true, block: () -> R?): R? = try {
  block()
} catch (t: Throwable) {
  if (log) t.printStackTrace()
  null
}
