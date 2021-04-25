package com.appifyhub.monolith.validation

interface Cleaner<in T, out R> {

  val name: String

  fun clean(argument: T?): R

}

inline fun <T> cleansToNonNull(
  name: String,
  crossinline cleaner: (T?) -> T,
) = object : Cleaner<T, T> {

  override val name: String = name

  override fun clean(argument: T?) = cleaner(argument)

}

inline fun <T> cleansToNullable(
  name: String,
  crossinline cleaner: (T?) -> T?,
) = object : Cleaner<T, T?> {

  override val name: String = name

  override fun clean(argument: T?) = cleaner(argument)

}
