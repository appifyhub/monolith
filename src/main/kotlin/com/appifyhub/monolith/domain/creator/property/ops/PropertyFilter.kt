package com.appifyhub.monolith.domain.creator.property.ops

import com.appifyhub.monolith.domain.creator.property.PropertyCategory
import com.appifyhub.monolith.domain.creator.property.PropertyTag
import com.appifyhub.monolith.domain.creator.property.PropertyType

data class PropertyFilter(
  val type: PropertyType? = null,
  val category: PropertyCategory? = null,
  val nameContains: String? = null,
  val isMandatory: Boolean? = null,
  val isSecret: Boolean? = null,
  val isDeprecated: Boolean? = null,
  val mustHaveTags: Set<PropertyTag>? = null,
  val hasAtLeastOneOfTags: Set<PropertyTag>? = null,
)
