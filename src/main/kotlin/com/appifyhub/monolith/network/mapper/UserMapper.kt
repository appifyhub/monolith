package com.appifyhub.monolith.network.mapper

import com.appifyhub.monolith.domain.user.Organization
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.network.user.DateTimeMapper
import com.appifyhub.monolith.network.user.OrganizationDto
import com.appifyhub.monolith.network.user.UserResponse

fun User.toNetwork(): UserResponse = UserResponse(
  userId = userId.id,
  projectId = userId.projectId,
  unifiedId = userId.toUnifiedFormat(),
  name = name,
  type = type.name.toLowerCase(),
  authority = authority.name.toLowerCase(),
  allowsSpam = allowsSpam,
  contact = contact,
  contactType = contactType.name.toLowerCase(),
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