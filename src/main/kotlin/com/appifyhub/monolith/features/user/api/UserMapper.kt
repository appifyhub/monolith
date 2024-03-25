package com.appifyhub.monolith.features.user.api

import com.appifyhub.monolith.features.user.api.model.OrganizationDto
import com.appifyhub.monolith.features.user.api.model.UserResponse
import com.appifyhub.monolith.features.user.domain.model.Organization
import com.appifyhub.monolith.features.user.domain.model.User

fun User.toNetwork(): UserResponse = UserResponse(
  userId = id.userId,
  projectId = id.projectId,
  universalId = id.toUniversalFormat(),
  name = name,
  type = type.name,
  authority = authority.name,
  allowsSpam = allowsSpam,
  contact = contact,
  contactType = contactType.name,
  birthday = birthday?.let { DateTimeMapper.formatAsDate(it) },
  company = company?.toNetwork(),
  languageTag = languageTag,
  createdAt = DateTimeMapper.formatAsDateTime(createdAt),
  updatedAt = DateTimeMapper.formatAsDateTime(updatedAt),
)

fun OrganizationDto.toDomain(): Organization = Organization(
  name = name,
  street = street,
  postcode = postcode,
  city = city,
  countryCode = countryCode,
)

fun Organization.toNetwork(): OrganizationDto = OrganizationDto(
  name = name,
  street = street,
  postcode = postcode,
  city = city,
  countryCode = countryCode,
)
