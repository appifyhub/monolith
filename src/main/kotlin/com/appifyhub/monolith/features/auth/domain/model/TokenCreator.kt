package com.appifyhub.monolith.features.auth.domain.model

import com.appifyhub.monolith.features.user.domain.model.User
import com.appifyhub.monolith.features.user.domain.model.UserId

data class TokenCreator(
  val id: UserId,
  val authority: User.Authority,
  val isStatic: Boolean,
  val origin: String?,
  val ipAddress: String?,
  val geo: String?,
)
