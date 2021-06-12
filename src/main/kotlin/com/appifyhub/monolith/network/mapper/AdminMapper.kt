package com.appifyhub.monolith.network.mapper

import com.appifyhub.monolith.domain.admin.ops.PropertyFilter
import com.appifyhub.monolith.domain.admin.property.PropertyCategory
import com.appifyhub.monolith.domain.admin.property.PropertyTag
import com.appifyhub.monolith.domain.admin.property.PropertyType
import com.appifyhub.monolith.network.admin.PropertyFilterQueryParams

fun PropertyFilterQueryParams.toDomain(): PropertyFilter = PropertyFilter(
  type = type?.let { PropertyType.find(it)!! },
  category = category?.let { PropertyCategory.find(it)!! },
  nameContains = name_contains,
  isMandatory = mandatory,
  isSecret = secret,
  isDeprecated = deprecated,
  mustHaveTags = must_have_tags?.map { PropertyTag.find(it)!! }?.takeIf { it.isNotEmpty() }?.toSet(),
  hasAtLeastOneOfTags = has_at_least_one_of_tags?.map { PropertyTag.find(it)!! }?.takeIf { it.isNotEmpty() }?.toSet(),
)
