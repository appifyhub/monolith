package com.appifyhub.monolith.util

import com.appifyhub.monolith.service.validation.Normalizer
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

val String.Companion.empty: String get() = ""
val String.Companion.space: String get() = " "

fun String.takeIfNotBlank(): String? = takeIf { it.isNotBlank() }

fun String?.isNullOrNotBlank(): Boolean = this?.isBlank() != true

fun String.hasSpaces() = contains(String.space)

fun String.hasNoSpaces() = !hasSpaces()

fun throwUnauthorized(
  message: () -> Any = { "Not authorized to perform this action" },
): Nothing = throw ResponseStatusException(HttpStatus.UNAUTHORIZED, message().toString())

fun throwNormalization(
  message: () -> Any = { "Data is invalid" },
): Nothing = throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, message().toString())

fun requireForAuth(
  value: Boolean,
  message: () -> Any = { "Not authorized for this operation" },
) = if (!value) throwUnauthorized(message) else Unit

fun <T> Normalizer.Result<T>.requireValid(
  propName: () -> Any = { "Property" },
) = if (!isValid) throwNormalization { "${propName()} '$value' is invalid" } else value