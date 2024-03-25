package com.appifyhub.monolith.features.common.validation

interface Normalizer<T : Any?> {

  data class Result<out T>(
    val value: T,
    val isValid: Boolean,
  ) {
    companion object {
      fun <T> valid(arg: T) = Result(arg, true)
    }
  }

  val name: String

  fun run(argument: T?): Result<T>

}

fun <T> normalizesNullable(
  validator: Validator<T>,
  cleaner: Cleaner<T, T?>,
): Normalizer<T?> = object : Normalizer<T?> {

  override val name: String = "${validator.name}.${cleaner.name}"

  override fun run(argument: T?): Normalizer.Result<T?> {
    val cleaned = cleaner.clean(argument)
    return Normalizer.Result(
      value = cleaned,
      isValid = validator.isValid(cleaned),
    )
  }

}

fun <T> normalizesNonNull(
  validator: Validator<T>,
  cleaner: Cleaner<T, T>,
): Normalizer<T> = object : Normalizer<T> {

  override val name: String = "${validator.name}.${cleaner.name}"

  override fun run(argument: T?): Normalizer.Result<T> {
    val cleaned = cleaner.clean(argument)
    return Normalizer.Result(
      value = cleaned,
      isValid = validator.isValid(cleaned),
    )
  }

}
