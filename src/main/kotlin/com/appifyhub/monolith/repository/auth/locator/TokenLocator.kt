package com.appifyhub.monolith.repository.auth.locator

import com.appifyhub.monolith.domain.user.UserId

data class TokenLocator(
  val userId: UserId,
  val origin: String?,
  val timestamp: Long,
)