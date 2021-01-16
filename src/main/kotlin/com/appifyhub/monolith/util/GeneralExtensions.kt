package com.appifyhub.monolith.util

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

val String.Companion.empty: String get() = ""

fun String.takeIfNotBlank(): String? = takeIf { it.isNotBlank() }

inline fun String?.requireNotBlank(propName: () -> String = { "Property" }) = if (isNullOrBlank())
  throw IllegalArgumentException("${propName()} is null or blank") else Unit

inline fun String?.requireNullOrNotBlank(propName: () -> String = { "Property" }) = if (this?.isBlank() == true)
  throw IllegalArgumentException("${propName()} is blank") else Unit

fun unauthorized(message: String? = null): Nothing =
  throw ResponseStatusException(HttpStatus.UNAUTHORIZED, message ?: "Not authorized to perform this action")