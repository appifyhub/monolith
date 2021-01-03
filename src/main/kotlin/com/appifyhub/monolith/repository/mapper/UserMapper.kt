package com.appifyhub.monolith.repository.mapper

import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.common.applySettable
import com.appifyhub.monolith.domain.common.stubProject
import com.appifyhub.monolith.domain.user.Organization
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.domain.user.ops.OrganizationUpdater
import com.appifyhub.monolith.domain.user.ops.UserCreator
import com.appifyhub.monolith.domain.user.ops.UserUpdater
import com.appifyhub.monolith.storage.model.user.OrganizationDbm
import com.appifyhub.monolith.storage.model.user.UserDbm
import com.appifyhub.monolith.storage.model.user.UserIdDbm
import com.appifyhub.monolith.util.TimeProvider
import com.appifyhub.monolith.util.empty
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.core.userdetails.User as SpringUser

fun UserUpdater.applyTo(
  user: User,
  timeProvider: TimeProvider,
  passwordEncoder: PasswordEncoder,
): User = user
  // non-null values
  .applySettable(rawSignature) { copy(signature = passwordEncoder.encode(it)) }
  .applySettable(type) { copy(type = it) }
  .applySettable(authority) { copy(authority = it) }
  .applySettable(contactType) { copy(contactType = it) }
  .applySettable(allowsSpam) { copy(allowsSpam = it) }
  // possible null values
  .applySettable(name) { copy(name = it) }
  .applySettable(contact) { copy(contact = it) }
  .applySettable(verificationToken) { copy(verificationToken = it) }
  .applySettable(birthday) { copy(birthday = it) }
  .applySettable(company) { copy(company = it?.applyTo(company)) }
  .applySettable(account) { copy(account = it) }
  .copy(updatedAt = timeProvider.currentDate)

fun OrganizationUpdater.applyTo(organization: Organization?): Organization? =
  organization
    ?.applySettable(name) { copy(name = it) }
    ?.applySettable(street) { copy(street = it) }
    ?.applySettable(postcode) { copy(postcode = it) }
    ?.applySettable(city) { copy(city = it) }
    ?.applySettable(countryCode) { copy(countryCode = it) }

fun UserCreator.toUser(
  userId: String,
  timeProvider: TimeProvider,
  passwordEncoder: PasswordEncoder,
): User = User(
  userId = UserId(id = userId, projectId = projectId),
  idType = idType,
  signature = passwordEncoder.encode(rawSignature),
  name = name,
  type = type,
  authority = authority,
  allowsSpam = allowsSpam,
  contact = contact,
  contactType = contactType,
  verificationToken = null,
  birthday = birthday,
  createdAt = timeProvider.currentDate,
  updatedAt = timeProvider.currentDate,
  company = company,
  blockedTokens = emptyList(),
)

fun User.toSecurityUser(): UserDetails =
  SpringUser.builder()
    .username(userId.toUnifiedFormat())
    .password(signature)
    .authorities(*allAuthorities)
    .accountLocked(verificationToken != null)
    .disabled(verificationToken != null)
    .credentialsExpired(false)
    .build()

fun UserDetails.toDomain(timeProvider: TimeProvider): User = User(
  userId = UserId.fromUnifiedFormat(username ?: String.empty),
  idType = User.IdType.CUSTOM,
  signature = password ?: String.empty,
  name = null,
  type = User.Type.PERSONAL,
  authority = User.Authority.find(authorities),
  allowsSpam = false,
  contact = null,
  contactType = User.ContactType.CUSTOM,
  verificationToken = if (isAccountNonLocked) null else String.empty,
  birthday = null,
  createdAt = timeProvider.currentDate,
  updatedAt = timeProvider.currentDate,
  company = null,
  blockedTokens = emptyList(),
  account = null,
)

fun UserDbm.toDomain(): User = User(
  userId = userId.toDomain(),
  idType = User.IdType.find(idType, default = User.IdType.CUSTOM),
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
  blockedTokens = emptyList(),
  account = account?.toDomain(),
)

fun User.toData(
  project: Project? = null,
): UserDbm = UserDbm(
  userId = userId.toData(),
  project = (project ?: stubProject().copy(id = userId.projectId)).toData(),
  idType = idType.name,
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
