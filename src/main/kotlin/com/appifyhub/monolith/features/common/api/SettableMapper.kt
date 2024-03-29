package com.appifyhub.monolith.features.common.api

import com.appifyhub.monolith.features.common.api.model.SettableRequest
import com.appifyhub.monolith.features.common.domain.model.Settable

fun <T : Any?> SettableRequest<T?>?.toDomainNullable(): Settable<T?>? = this?.let { Settable(it.value) }

fun <T : Any> SettableRequest<T>?.toDomainNonNull(): Settable<T>? = this?.let { Settable(it.value) }

inline fun <T : Any?, R : Any?> SettableRequest<T?>?.mapToDomainNullable(mapper: (T) -> R): Settable<R?>? =
  this?.let { Settable(it.value?.let { v -> mapper(v) }) }

inline fun <T : Any, R : Any> SettableRequest<T>?.mapToDomainNonNull(mapper: (T) -> R): Settable<R>? =
  this?.let { Settable(mapper(value)) }
