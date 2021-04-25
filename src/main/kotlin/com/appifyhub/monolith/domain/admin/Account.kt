package com.appifyhub.monolith.domain.admin

import com.appifyhub.monolith.domain.user.User
import java.util.Date

data class Account(
  val id: Long,
  val owners: List<User> = emptyList(),
  val createdAt: Date,
  val updatedAt: Date = createdAt,
)
