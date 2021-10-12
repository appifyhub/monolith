package com.appifyhub.monolith.network.mapper

import com.appifyhub.monolith.domain.creator.property.Property
import com.appifyhub.monolith.domain.creator.property.PropertyCategory
import com.appifyhub.monolith.domain.creator.property.ProjectProperty
import com.appifyhub.monolith.domain.creator.property.PropertyTag
import com.appifyhub.monolith.domain.creator.property.PropertyType
import com.appifyhub.monolith.domain.creator.property.ops.PropertyFilter
import com.appifyhub.monolith.network.creator.property.ops.PropertyFilterQueryParams
import com.appifyhub.monolith.network.creator.property.PropertyConfigurationResponse
import com.appifyhub.monolith.network.creator.property.PropertyResponse
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
