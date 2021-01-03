package com.appifyhub.monolith.domain.auth

import com.appifyhub.monolith.domain.user.User

data class BlockedToken(
  val token: Token,
  val owner: User,
)
