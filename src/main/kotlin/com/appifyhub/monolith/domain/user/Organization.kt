package com.appifyhub.monolith.domain.user

data class Organization(
  val name: String?,
  val street: String?,
  val postcode: String?,
  val city: String?,
  val countryCode: String?,
) {
  companion object {
    val EMPTY = Organization(
      name = null,
      street = null,
      postcode = null,
      city = null,
      countryCode = null,
    )
  }
}
