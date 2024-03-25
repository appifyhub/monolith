package com.appifyhub.monolith.features.user.domain

import com.appifyhub.monolith.features.common.domain.stubProject
import com.appifyhub.monolith.features.creator.domain.model.Project
import com.appifyhub.monolith.features.creator.domain.toData
import com.appifyhub.monolith.features.user.domain.model.Organization
import com.appifyhub.monolith.features.user.domain.model.User
import com.appifyhub.monolith.features.user.domain.model.UserId
import com.appifyhub.monolith.features.user.storage.model.OrganizationDbm
import com.appifyhub.monolith.features.user.storage.model.UserDbm
import com.appifyhub.monolith.features.user.storage.model.UserIdDbm

fun UserDbm.toDomain(): User = User(
  id = id.toDomain(),
  signature = signature,
  name = name,
  type = User.Type.find(type, default = User.Type.PERSONAL),
  authority = User.Authority.find(authority, default = User.Authority.DEFAULT),
  allowsSpam = allowsSpam,
  contact = contact,
  contactType = User.ContactType.find(contactType, default = User.ContactType.CUSTOM),
  verificationToken = verificationToken,
  birthday = birthday,
  company = company?.toDomain(),
  languageTag = languageTag,
  createdAt = createdAt,
  updatedAt = updatedAt,
)

fun User.toData(
  project: Project? = null,
): UserDbm = UserDbm(
  id = id.toData(),
  project = (project ?: stubProject().copy(id = id.projectId)).toData(),
  signature = signature,
  name = name,
  type = type.name,
  authority = authority.name,
  allowsSpam = allowsSpam,
  contact = contact,
  contactType = contactType.name,
  verificationToken = verificationToken,
  birthday = birthday,
  company = company?.toData(),
  languageTag = languageTag,
  createdAt = createdAt,
  updatedAt = updatedAt,
)

fun UserIdDbm.toDomain(): UserId = UserId(
  userId = userId,
  projectId = projectId,
)

fun UserId.toData(): UserIdDbm = UserIdDbm(
  userId = userId,
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
