package com.appifyhub.monolith.util

import com.appifyhub.monolith.domain.user.UserId
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

val String.Companion.empty: String get() = ""
val String.Companion.space: String get() = " "

fun String.takeIfNotBlank(): String? = takeIf { it.isNotBlank() }

inline fun String?.requireNotBlank(
  propName: () -> String = { "Property" },
) = if (isNullOrBlank()) throw IllegalArgumentException("${propName()} is null or blank") else Unit

inline fun String?.requireNullOrNotBlank(
  propName: () -> Any = { "Property" },
) = if (!isNullOrNotBlank()) throw IllegalArgumentException("${propName()} is blank") else Unit

fun String?.isNullOrNotBlank(): Boolean = this?.isBlank() != true

fun throwUnauthorized(
  message: () -> Any = { "Not authorized to perform this action" },
): Nothing = throw ResponseStatusException(HttpStatus.UNAUTHORIZED, message().toString())

fun requireForAuth(
  value: Boolean,
  message: () -> Any = { "Not authorized for this operation" },
) = if (!value) throwUnauthorized(message) else Unit

fun UserId.requireValidFormat(
  message: () -> Any = { "Invalid UserId format: $this" },
) = require(id.isNotBlank() && projectId > 0, message)

fun String.hasSpaces() = contains(String.space)

fun String.hasNoSpaces() = !hasSpaces()