package com.appifyhub.monolith.domain.mapper

import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.common.stubProject
import com.appifyhub.monolith.domain.user.Organization
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.storage.model.user.OrganizationDbm
import com.appifyhub.monolith.storage.model.user.UserDbm
import com.appifyhub.monolith.storage.model.user.UserIdDbm

fun UserDbm.toDomain(): User = User(
  userId = userId.toDomain(),
  signature = signature,
  name = name,
  type = User.Type.find(type, default = User.Type.PERSONAL),
  authority = User.Authority.find(authority, default = User.Authority.DEFAULT),
  allowsSpam = allowsSpam,
  contact = contact,
  contactType = User.ContactType.find(contactType, default = User.ContactType.CUSTOM),
  verificationToken = verificationToken,
  birthday = birthday,
  createdAt = createdAt,
  updatedAt = updatedAt,
  company = company?.toDomain(),
  ownedTokens = emptyList(),
  account = account?.toDomain(),
)

fun User.toData(
  project: Project? = null,
): UserDbm = UserDbm(
  userId = userId.toData(),
  project = (project ?: stubProject().copy(id = userId.projectId)).toData(),
  signature = signature,
  name = name,
  type = type.name,
  authority = authority.name,
  allowsSpam = allowsSpam,
  contact = contact,
  contactType = contactType.name,
  verificationToken = verificationToken,
  birthday = birthday,
  createdAt = createdAt,
  updatedAt = updatedAt,
  company = company?.toData(),
  account = account?.toData(),
)

fun UserIdDbm.toDomain(): UserId = UserId(
  id = identifier,
  projectId = projectId,
)

fun UserId.toData(): UserIdDbm = UserIdDbm(
  identifier = id,
  projectId = projectId,
)

fun OrganizationDbm.toDomain(): Organization = Organization(
  name = name,
  street = street,
  postcode = postcode,
  city = city,
  countryCode = countryCode,
)

fun Organization.toData(): OrganizationDbm = OrganizationDbm(
  name = name,
  street = street,
  postcode = postcode,
  city = city,
  countryCode = countryCode,
)
