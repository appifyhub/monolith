package com.appifyhub.monolith.util

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

val String.Companion.empty: String get() = ""

fun String.takeIfNotBlank(): String? = takeIf { it.isNotBlank() }

fun String?.assertNotBlank(errorName: String = "Property") = if (isNullOrBlank())
  throw IllegalArgumentException("$errorName is null or blank") else Unit

fun unauthorized(message: String? = null): Nothing =
  throw ResponseStatusException(HttpStatus.UNAUTHORIZED, message ?: "Not authorized to perform this action")