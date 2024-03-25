package com.appifyhub.monolith.features.user.api

import com.appifyhub.monolith.features.common.domain.model.Settable
import com.appifyhub.monolith.features.user.api.model.OrganizationUpdaterDto
import com.appifyhub.monolith.features.user.api.model.UserSignupRequest
import com.appifyhub.monolith.features.user.api.model.UserUpdateAuthorityRequest
import com.appifyhub.monolith.features.user.api.model.UserUpdateDataRequest
import com.appifyhub.monolith.features.user.api.model.UserUpdateSignatureRequest
import com.appifyhub.monolith.features.user.domain.model.OrganizationUpdater
import com.appifyhub.monolith.features.user.domain.model.User
import com.appifyhub.monolith.features.user.domain.model.User.ContactType.CUSTOM
import com.appifyhub.monolith.features.user.domain.model.User.Type.PERSONAL
import com.appifyhub.monolith.features.user.domain.model.UserCreator
import com.appifyhub.monolith.features.user.domain.model.UserId
import com.appifyhub.monolith.features.user.domain.model.UserUpdater
import com.appifyhub.monolith.features.common.api.mapToDomainNonNull
import com.appifyhub.monolith.features.common.api.mapToDomainNullable
import com.appifyhub.monolith.features.common.api.toDomainNonNull
import com.appifyhub.monolith.features.common.api.toDomainNullable

fun UserSignupRequest.toDomain(
  projectId: Long,
): UserCreator = UserCreator(
  userId = userId,
  projectId = projectId,
  rawSignature = rawSignature,
  name = name,
  type = User.Type.find(type.orEmpty(), default = PERSONAL),
  authority = User.Authority.DEFAULT,
  allowsSpam = allowsSpam ?: false,
  contact = contact,
  contactType = User.ContactType.find(contactType.orEmpty(), default = CUSTOM),
  birthday = birthday?.let { DateTimeMapper.parseAsDate(it) },
  company = company?.toDomain(),
  languageTag = languageTag,
  signupCode = signupCode,
)

fun UserUpdateAuthorityRequest.toDomain(
  id: UserId,
): UserUpdater = UserUpdater(
  id = id,
  authority = Settable(User.Authority.find(authority)),
)

fun UserUpdateDataRequest.toDomain(
  id: UserId,
): UserUpdater = UserUpdater(
  id = id,
  name = name.toDomainNullable(),
  type = type.mapToDomainNonNull { User.Type.find(it, default = PERSONAL) },
  allowsSpam = allowsSpam.toDomainNonNull(),
  contact = contact.toDomainNullable(),
  contactType = contactType.mapToDomainNonNull { User.ContactType.find(it, default = CUSTOM) },
  birthday = birthday.mapToDomainNullable { DateTimeMapper.parseAsDate(it) },
  company = company.mapToDomainNullable { it.toDomain() },
  languageTag = languageTag.toDomainNullable(),
)

fun UserUpdateSignatureRequest.toDomain(
  id: UserId,
): UserUpdater = UserUpdater(
  id = id,
  rawSignature = Settable(rawSignatureNew),
)

fun OrganizationUpdaterDto.toDomain(): OrganizationUpdater = OrganizationUpdater(
  name = name.toDomainNullable(),
  street = street.toDomainNullable(),
  postcode = postcode.toDomainNullable(),
  city = city.toDomainNullable(),
  countryCode = countryCode.toDomainNullable(),
)
