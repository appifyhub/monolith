package com.appifyhub.monolith.domain.auth

import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import java.util.Date

data class TokenDetails(
  val tokenValue: String,
  val isBlocked: Boolean,
  val createdAt: Date,
  val expiresAt: Date,
  val ownerId: UserId,
  val authority: User.Authority,
  val origin: String?,
  val ipAddress: String?,
  val geo: String?,
  val accountId: Long?,
  val isStatic: Boolean,
)
