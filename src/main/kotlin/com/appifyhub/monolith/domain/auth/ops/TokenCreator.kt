package com.appifyhub.monolith.domain.auth.ops

import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId

data class TokenCreator(
  val userId: UserId,
  val authority: User.Authority,
  val isStatic: Boolean,
  val origin: String?,
  val ipAddress: String?,
  val geo: String?,
)
