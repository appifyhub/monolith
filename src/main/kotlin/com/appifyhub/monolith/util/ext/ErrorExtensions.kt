package com.appifyhub.monolith.util.ext

import com.appifyhub.monolith.validation.Normalizer
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

@Throws(ResponseStatusException::class)
fun throwUnauthorized(
  message: () -> Any = { "Not authorized to perform this action" },
): Nothing = throw ResponseStatusException(HttpStatus.UNAUTHORIZED, message().toString())

@Throws(ResponseStatusException::class)
fun throwNormalization(
  message: () -> Any = { "Data is invalid" },
): Nothing = throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, message().toString())

@Throws(ResponseStatusException::class)
fun throwPropertyNotFound(propName: String): Nothing =
  throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Property '$propName' not found")

@Throws(ResponseStatusException::class)
fun throwLocked(
  message: () -> Any = { "Operation is not allowed" },
): Nothing = throw ResponseStatusException(HttpStatus.LOCKED, message().toString())

@Throws(ResponseStatusException::class)
fun throwNotFound(
  message: () -> Any = { "Resource not found" },
): Nothing = throw ResponseStatusException(HttpStatus.NOT_FOUND, message().toString())

@Throws(ResponseStatusException::class)
fun <T> Normalizer.Result<T>.requireValid(
  propName: () -> Any = { "Property" },
) = if (!isValid) throwNormalization { "${propName()} '$value' is invalid" } else value

inline fun <R> silent(log: Boolean = true, block: () -> R?): R? = try {
  block()
} catch (t: Throwable) {
  if (log) t.printStackTrace()
  null
}
