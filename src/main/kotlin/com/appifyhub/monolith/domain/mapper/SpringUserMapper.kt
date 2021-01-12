package com.appifyhub.monolith.domain.mapper

import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.util.TimeProvider
import com.appifyhub.monolith.util.empty
import org.springframework.security.core.userdetails.UserDetails

fun User.toSecurityUser(): UserDetails =
  org.springframework.security.core.userdetails.User.builder()
    .username(userId.toUnifiedFormat())
    .password(signature)
    .authorities(*allAuthorities)
    .accountLocked(verificationToken != null)
    .disabled(verificationToken != null)
    .credentialsExpired(false)
    .build()

fun UserDetails.toDomain(timeProvider: TimeProvider): User = User(
  userId = UserId.fromUnifiedFormat(username ?: String.empty),
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