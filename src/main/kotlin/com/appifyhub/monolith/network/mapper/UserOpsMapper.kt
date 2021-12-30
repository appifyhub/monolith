package com.appifyhub.monolith.network.mapper

import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.ContactType.CUSTOM
import com.appifyhub.monolith.domain.user.User.Type.PERSONAL
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.domain.user.ops.OrganizationUpdater
import com.appifyhub.monolith.domain.user.ops.UserCreator
import com.appifyhub.monolith.domain.user.ops.UserUpdater
import com.appifyhub.monolith.network.user.DateTimeMapper
import com.appifyhub.monolith.network.user.ops.OrganizationUpdaterDto
import com.appifyhub.monolith.network.user.ops.UserSignupRequest
import com.appifyhub.monolith.network.user.ops.UserUpdateAuthorityRequest
import com.appifyhub.monolith.network.user.ops.UserUpdateDataRequest
import com.appifyhub.monolith.network.user.ops.UserUpdateSignatureRequest
import com.appifyhub.monolith.network.user.ops.UserUpdateVerificationRequest

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

@Suppress("unused") // needed for consistency
fun UserUpdateVerificationRequest.toDomain(
  id: UserId,
): UserUpdater = UserUpdater(
  id = id,
  verificationToken = Settable(null),
)

fun OrganizationUpdaterDto.toDomain(): OrganizationUpdater = OrganizationUpdater(
  name = name.toDomainNullable(),
  street = street.toDomainNullable(),
  postcode = postcode.toDomainNullable(),
  city = city.toDomainNullable(),
  countryCode = countryCode.toDomainNullable(),
)
