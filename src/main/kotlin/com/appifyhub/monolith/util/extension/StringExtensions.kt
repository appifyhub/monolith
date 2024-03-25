package com.appifyhub.monolith.util.extension

fun String.takeIfNotBlank(): String? = takeIf { it.isNotBlank() }

fun String?.isNullOrNotBlank(): Boolean = this?.isBlank() != true

fun String.hasSpaces() = any { it.isWhitespace() }

fun String.hasNoSpaces() = !hasSpaces()
