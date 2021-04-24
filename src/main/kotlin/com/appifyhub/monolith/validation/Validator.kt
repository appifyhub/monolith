package com.appifyhub.monolith.validation

interface Validator<in T> {

  val name: String

  fun isValid(argument: T?): Boolean

}

inline fun <T> validatesAs(
  name: String,
  crossinline validator: (T?) -> Boolean,
) = object : Validator<T> {

  override val name: String = name

  override fun isValid(argument: T?) = validator(argument)

}
