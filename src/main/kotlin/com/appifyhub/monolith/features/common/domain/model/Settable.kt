package com.appifyhub.monolith.features.common.domain.model

data class Settable<out T>(val value: T)

inline fun <T : Any, S : Any?> T.applySettable(
  settable: Settable<S>?,
  action: T.(S) -> T,
): T = settable?.let { action(it.value) } ?: this

inline fun <T : Any?, R : Any?> Settable<T?>.mapValueNullable(
  mapper: (T) -> R?,
): Settable<R?> = if (value == null) Settable<R?>(null) else Settable(mapper(value))

inline fun <T : Any, R : Any> Settable<T>.mapValueNonNull(
  mapper: (T) -> R,
): Settable<R> = Settable(mapper(value))
