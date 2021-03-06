package com.appifyhub.monolith.network.mapper

import com.appifyhub.monolith.domain.user.Organization
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.network.user.DateTimeMapper
import com.appifyhub.monolith.network.user.OrganizationDto
import com.appifyhub.monolith.network.user.UserResponse

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
