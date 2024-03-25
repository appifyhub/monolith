package com.appifyhub.monolith.features.user.domain.model

import com.appifyhub.monolith.domain.common.Settable

data class OrganizationUpdater(
  val name: Settable<String?>? = null,
  val street: Settable<String?>? = null,
  val postcode: Settable<String?>? = null,
  val city: Settable<String?>? = null,
  val countryCode: Settable<String?>? = null,
)
