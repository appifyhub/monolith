package com.appifyhub.monolith.features.user.domain

import com.appifyhub.monolith.features.common.domain.model.applySettable
import com.appifyhub.monolith.features.user.domain.model.Organization
import com.appifyhub.monolith.features.user.domain.model.OrganizationUpdater
import com.appifyhub.monolith.features.user.domain.model.User
import com.appifyhub.monolith.features.user.domain.model.UserCreator
import com.appifyhub.monolith.features.user.domain.model.UserId
import com.appifyhub.monolith.features.user.domain.model.UserUpdater
import com.appifyhub.monolith.util.TimeProvider
import org.springframework.security.crypto.password.PasswordEncoder

fun UserUpdater.applyTo(
  user: User,
  passwordEncoder: PasswordEncoder,
  timeProvider: TimeProvider,
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
  .applySettable(languageTag) { copy(languageTag = it) }
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
  passwordEncoder: PasswordEncoder,
  timeProvider: TimeProvider,
): User = User(
  id = UserId(userId = userId, projectId = projectId),
  signature = passwordEncoder.encode(rawSignature),
  name = name,
  type = type,
  authority = authority,
  allowsSpam = allowsSpam,
  contact = contact,
  contactType = contactType,
  verificationToken = null,
  birthday = birthday,
  company = company,
  languageTag = languageTag,
  createdAt = timeProvider.currentDate,
  updatedAt = timeProvider.currentDate,
)
