package com.appifyhub.monolith.domain.user

import java.util.Date

data class SignupCode(
  var code: String,
  var isUsed: Boolean,
  val owner: User,
  var createdAt: Date,
  var usedAt: Date?,
)
