package com.appifyhub.monolith.domain.user.ops

import com.appifyhub.monolith.domain.common.Settable

data class OrganizationUpdater(
  val name: Settable<String?>? = null,
  val street: Settable<String?>? = null,
  val postcode: Settable<String?>? = null,
  val city: Settable<String?>? = null,
  val countryCode: Settable<String?>? = null,
)
