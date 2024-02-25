package com.appifyhub.monolith.domain.user

import java.util.Date

data class SignupCode(
  val code: String,
  val isUsed: Boolean,
  val owner: User,
  val createdAt: Date,
  val usedAt: Date?,
)
