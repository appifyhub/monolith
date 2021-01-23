package com.appifyhub.monolith.service.validation

interface Normalizer<T : Any?> {

  data class Result<out T>(
    val value: T,
    val isValid: Boolean,
  ) {
    companion object {
      fun <T> valid(arg: T) = Result(arg, true)
    }
  }

  fun run(argument: T?): Result<T>

}

fun <T> normalizesNullable(
  validator: Validator<T>,
  cleaner: Cleaner<T, T?>,
): Normalizer<T?> = object : Normalizer<T?> {
  override fun run(argument: T?): Normalizer.Result<T?> {
    val cleaned = cleaner.clean(argument)
    return Normalizer.Result(
      value = cleaned,
      isValid = validator.isValid(cleaned)
    )
  }
}

fun <T> normalizesNonNull(
  validator: Validator<T>,
  cleaner: Cleaner<T, T>,
): Normalizer<T> = object : Normalizer<T> {
  override fun run(argument: T?): Normalizer.Result<T> {
    val cleaned = cleaner.clean(argument)
    return Normalizer.Result(
      value = cleaned,
      isValid = validator.isValid(cleaned)
    )
  }
}
