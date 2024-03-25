package com.appifyhub.monolith.features.auth.domain.model

import com.appifyhub.monolith.features.user.domain.model.User
import com.appifyhub.monolith.features.user.domain.model.UserId
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
  val isStatic: Boolean,
)
