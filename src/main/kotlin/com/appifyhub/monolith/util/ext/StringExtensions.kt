package com.appifyhub.monolith.util.ext

val String.Companion.empty: String get() = ""
val String.Companion.space: String get() = " "

fun String.takeIfNotBlank(): String? = takeIf { it.isNotBlank() }

fun String?.isNullOrNotBlank(): Boolean = this?.isBlank() != true

fun String.hasSpaces() = contains(String.space)

fun String.hasNoSpaces() = !hasSpaces()