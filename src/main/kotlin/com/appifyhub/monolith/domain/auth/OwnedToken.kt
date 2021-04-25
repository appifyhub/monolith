package com.appifyhub.monolith.domain.auth

import com.appifyhub.monolith.domain.user.User
import java.util.Date

data class OwnedToken(
  val token: Token,
  val isBlocked: Boolean,
  val origin: String?,
  val createdAt: Date,
  val expiresAt: Date,
  val owner: User,
)
