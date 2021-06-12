package com.appifyhub.monolith.network.admin

data class PropertyFilterQueryParams(
  var type: String? = null,
  var category: String? = null,
  var name_contains: String? = null,
  var mandatory: Boolean? = null,
  var secret: Boolean? = null,
  var deprecated: Boolean? = null,
  var must_have_tags: List<String>? = null,
  var has_at_least_one_of_tags: List<String>? = null,
)
