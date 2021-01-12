package com.appifyhub.monolith.domain.mapper

import com.appifyhub.monolith.domain.common.applySettable
import com.appifyhub.monolith.domain.user.Organization
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.domain.user.ops.OrganizationUpdater
import com.appifyhub.monolith.domain.user.ops.UserCreator
import com.appifyhub.monolith.domain.user.ops.UserUpdater
import com.appifyhub.monolith.util.TimeProvider
import org.springframework.security.crypto.password.PasswordEncoder

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