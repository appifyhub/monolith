package com.appifyhub.monolith.network.mapper

import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.Authority.DEFAULT
import com.appifyhub.monolith.domain.user.User.ContactType.CUSTOM
import com.appifyhub.monolith.domain.user.User.Type.PERSONAL
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.domain.user.ops.OrganizationUpdater
import com.appifyhub.monolith.domain.user.ops.UserCreator
import com.appifyhub.monolith.domain.user.ops.UserUpdater
import com.appifyhub.monolith.network.common.SettableRequest
import com.appifyhub.monolith.network.user.DateTimeMapper
import com.appifyhub.monolith.network.user.ops.OrganizationUpdaterDto
import com.appifyhub.monolith.network.user.ops.UserCreatorRequest
import com.appifyhub.monolith.network.user.ops.UserUpdaterRequest

fun UserUpdaterRequest.toDomain(
  id: UserId,
): UserUpdater = UserUpdater(
  id = id,
  rawSignature = rawSignature.toDomainNonNull(),
  type = type.mapToDomainNonNull { User.Type.find(it, default = PERSONAL) },
  authority = authority.mapToDomainNonNull { User.Authority.find(it, default = DEFAULT) },
  contactType = contactType.mapToDomainNonNull { User.ContactType.find(it, default = CUSTOM) },
  allowsSpam = allowsSpam.toDomainNonNull(),
  name = name.toDomainNullable(),
  contact = contact.toDomainNullable(),
  verificationToken = null,
  birthday = birthday.mapToDomainNullable { DateTimeMapper.parseAsDate(it) },
  company = company.mapToDomainNullable { it.toDomain() },
  languageTag = languageTag.toDomainNullable(),
)

fun OrganizationUpdaterDto.toDomain(): OrganizationUpdater = OrganizationUpdater(
  name = name.toDomainNullable(),
  street = street.toDomainNullable(),
  postcode = postcode.toDomainNullable(),
  city = city.toDomainNullable(),
  countryCode = countryCode.toDomainNullable(),
)

fun UserCreatorRequest.toDomain(
  projectId: Long,
): UserCreator = UserCreator(
  userId = userId,
  projectId = projectId,
  rawSecret = rawSignature,
  name = name,
  type = User.Type.find(type.orEmpty(), default = PERSONAL),
  authority = User.Authority.find(authority.orEmpty(), default = DEFAULT),
  allowsSpam = allowsSpam ?: false,
  contact = contact,
  contactType = User.ContactType.find(contactType.orEmpty(), default = CUSTOM),
  birthday = birthday?.let { DateTimeMapper.parseAsDate(it) },
  company = company?.toDomain(),
  languageTag = languageTag,
)

fun <T : Any?> SettableRequest<T?>?.toDomainNullable(): Settable<T?>? = this?.let { Settable(it.value) }

fun <T : Any> SettableRequest<T>?.toDomainNonNull(): Settable<T>? = this?.let { Settable(it.value) }

inline fun <T : Any?, R : Any?> SettableRequest<T?>?.mapToDomainNullable(mapper: (T) -> R): Settable<R?>? =
  this?.let { Settable(it.value?.let { v -> mapper(v) }) }

inline fun <T : Any, R : Any> SettableRequest<T>?.mapToDomainNonNull(mapper: (T) -> R): Settable<R>? =
  this?.let { Settable(mapper(value)) }
