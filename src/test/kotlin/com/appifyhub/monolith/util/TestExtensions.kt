package com.appifyhub.monolith.util

import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.util.ext.truncateTo
import java.time.temporal.ChronoUnit

fun User.cleanDates() = copy(
  birthday = birthday?.truncateTo(ChronoUnit.DAYS),
  createdAt = createdAt.truncateTo(ChronoUnit.SECONDS),
  updatedAt = updatedAt.truncateTo(ChronoUnit.SECONDS),
)
