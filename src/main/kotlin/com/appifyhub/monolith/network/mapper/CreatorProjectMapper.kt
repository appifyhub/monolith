package com.appifyhub.monolith.network.mapper

import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.creator.property.ProjectProperty
import com.appifyhub.monolith.domain.creator.property.Property
import com.appifyhub.monolith.domain.creator.property.PropertyCategory
import com.appifyhub.monolith.domain.creator.property.PropertyTag
import com.appifyhub.monolith.domain.creator.property.PropertyType
import com.appifyhub.monolith.domain.creator.property.ops.PropertyFilter
import com.appifyhub.monolith.domain.creator.setup.ProjectStatus
import com.appifyhub.monolith.network.creator.project.ProjectFeatureDto
import com.appifyhub.monolith.network.creator.project.ProjectResponse
import com.appifyhub.monolith.network.creator.project.ProjectStatusDto
import com.appifyhub.monolith.network.creator.property.PropertyConfigurationResponse
import com.appifyhub.monolith.network.creator.property.PropertyResponse
import com.appifyhub.monolith.network.creator.property.ops.PropertyFilterQueryParams
import com.appifyhub.monolith.network.user.DateTimeMapper
import com.appifyhub.monolith.service.access.AccessManager.Feature

// @formatter:off
fun PropertyFilterQueryParams.toDomain(): PropertyFilter = PropertyFilter(
  type = type?.let { PropertyType.findOrNull(it)!! },
  category = category?.let { PropertyCategory.findOrNull(it)!! },
  nameContains = name_contains,
  isMandatory = mandatory,
  isSecret = secret,
  isDeprecated = deprecated,
  mustHaveTags = must_have_tags?.map { PropertyTag.findOrNull(it)!! }?.takeIf { it.isNotEmpty() }?.toSet(),
  hasAtLeastOneOfTags = has_at_least_one_of_tags?.map { PropertyTag.findOrNull(it)!! }?.takeIf { it.isNotEmpty() }?.toSet(), // ktlint-disable max-line-length
)
// @formatter:on

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

fun Feature.toNetwork(): ProjectFeatureDto = ProjectFeatureDto(
  name = name,
  isRequired = isRequired,
  properties = properties.map(ProjectProperty::name),
)

fun ProjectStatus.toNetwork(): ProjectStatusDto = ProjectStatusDto(
  status = status.name,
  usableFeatures = usableFeatures.map(Feature::toNetwork),
  unusableFeatures = unusableFeatures.map(Feature::toNetwork),
  properties = properties.map(Property<*>::toNetwork),
)

fun Project.toNetwork(
  projectStatus: ProjectStatus,
): ProjectResponse = ProjectResponse(
  projectId = id,
  type = type.name,
  status = projectStatus.toNetwork(),
  userIdType = userIdType.name,
  languageTag = languageTag,
  createdAt = DateTimeMapper.formatAsDateTime(createdAt),
  updatedAt = DateTimeMapper.formatAsDateTime(updatedAt),
)
