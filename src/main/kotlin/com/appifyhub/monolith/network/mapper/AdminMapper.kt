package com.appifyhub.monolith.network.mapper

import com.appifyhub.monolith.domain.admin.property.Property
import com.appifyhub.monolith.domain.admin.property.PropertyCategory
import com.appifyhub.monolith.domain.admin.property.ProjectProperty
import com.appifyhub.monolith.domain.admin.property.PropertyTag
import com.appifyhub.monolith.domain.admin.property.PropertyType
import com.appifyhub.monolith.domain.admin.property.ops.PropertyFilter
import com.appifyhub.monolith.network.admin.property.ops.PropertyFilterQueryParams
import com.appifyhub.monolith.network.admin.property.PropertyConfigurationResponse
import com.appifyhub.monolith.network.admin.property.PropertyResponse
import com.appifyhub.monolith.network.user.DateTimeMapper

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

fun ProjectProperty.toNetwork(): PropertyConfigurationResponse = PropertyConfigurationResponse(
  name = name,
  type = type.name,
  category = category.name,
  tags = tags.map(PropertyTag::name).toSet(),
  defaultValue = defaultValue,
  isMandatory = isMandatory,
  isSecret = isSecret,
  isDeprecated = isDeprecated,
)

fun Property<Any>.toNetwork(): PropertyResponse = PropertyResponse(
  config = config.toNetwork(),
  rawValue = rawValue,
  updatedAt = DateTimeMapper.formatAsDateTime(updatedAt),
)
