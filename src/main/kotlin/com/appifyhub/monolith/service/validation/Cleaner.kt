package com.appifyhub.monolith.service.validation

interface Cleaner<in T, out R> {

  fun clean(argument: T?): R

}

inline fun <T> cleansToNonNull(
  crossinline cleaner: (T?) -> T,
) = object : Cleaner<T, T> {
  override fun clean(argument: T?) = cleaner(argument)
}

inline fun <T> cleansToNullable(
  crossinline cleaner: (T?) -> T?,
) = object : Cleaner<T, T?> {
  override fun clean(argument: T?) = cleaner(argument)
}