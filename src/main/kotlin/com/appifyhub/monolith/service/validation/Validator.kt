package com.appifyhub.monolith.service.validation

interface Validator<in T> {

  fun isValid(argument: T?): Boolean

}

inline fun <T> validatesAs(
  crossinline validator: (T?) -> Boolean,
) = object : Validator<T> {
  override fun isValid(argument: T?) = validator(argument)
}