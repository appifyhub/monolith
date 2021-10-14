package com.appifyhub.monolith.network.mapper

import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.creator.ops.ProjectCreator
import com.appifyhub.monolith.domain.creator.property.ProjectProperty
import com.appifyhub.monolith.domain.creator.property.Property
import com.appifyhub.monolith.domain.creator.property.PropertyCategory
import com.appifyhub.monolith.domain.creator.property.PropertyTag
import com.appifyhub.monolith.domain.creator.property.PropertyType
import com.appifyhub.monolith.domain.creator.property.ops.PropertyFilter
import com.appifyhub.monolith.domain.creator.setup.ProjectStatus
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.network.creator.ProjectFeatureDto
import com.appifyhub.monolith.network.creator.ProjectResponse
import com.appifyhub.monolith.network.creator.ProjectStatusDto
import com.appifyhub.monolith.network.creator.ops.ProjectCreateRequest
import com.appifyhub.monolith.network.creator.property.PropertyConfigurationResponse
import com.appifyhub.monolith.network.creator.property.PropertyResponse
import com.appifyhub.monolith.network.creator.property.ops.PropertyFilterQueryParams
import com.appifyhub.monolith.network.user.DateTimeMapper
import com.appifyhub.monolith.service.access.AccessManager.Feature

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
  createdAt = DateTimeMapper.formatAsDateTime(createdAt),
  updatedAt = DateTimeMapper.formatAsDateTime(updatedAt),
)

fun ProjectCreateRequest.toDomain(
  owner: User? = null,
): ProjectCreator = ProjectCreator(
  owner = owner,
  type = Project.Type.find(type, Project.Type.COMMERCIAL),
  status = Project.Status.REVIEW,
  userIdType = Project.UserIdType.find(userIdType, Project.UserIdType.RANDOM),
)
