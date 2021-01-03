package com.appifyhub.monolith.domain.common

data class Settable<out T>(val value: T)

inline fun <T : Any, S : Any?> T.applySettable(
  settable: Settable<S>?,
  action: T.(S) -> T,
) = settable?.let { action(it.value) } ?: this
