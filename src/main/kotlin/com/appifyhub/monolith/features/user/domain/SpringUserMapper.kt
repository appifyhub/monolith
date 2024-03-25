package com.appifyhub.monolith.features.user.domain

import com.appifyhub.monolith.features.user.domain.model.User
import com.appifyhub.monolith.features.user.domain.model.UserId
import com.appifyhub.monolith.util.TimeProvider
import org.springframework.security.core.userdetails.UserDetails

fun User.toSecurityUser(): UserDetails =
  org.springframework.security.core.userdetails.User.builder()
    .username(id.toUniversalFormat())
    .password(signature)
    .authorities(*allAuthorities.toTypedArray())
    .accountLocked(verificationToken != null)
    .disabled(verificationToken != null)
    .credentialsExpired(false)
    .build()

fun UserDetails.toDomain(timeProvider: TimeProvider): User = User(
  id = UserId.fromUniversalFormat(username ?: ""),
  signature = password ?: "",
  name = null,
  type = User.Type.PERSONAL,
  authority = User.Authority.find(authorities, User.Authority.DEFAULT),
  allowsSpam = false,
  contact = null,
  contactType = User.ContactType.CUSTOM,
  verificationToken = if (isAccountNonLocked) null else "",
  birthday = null,
  company = null,
  languageTag = null,
  createdAt = timeProvider.currentDate,
  updatedAt = timeProvider.currentDate,
)
