package com.appifyhub.monolith.features.user.domain

import com.appifyhub.monolith.features.creator.domain.model.Project
import com.appifyhub.monolith.features.user.domain.model.SignupCode
import com.appifyhub.monolith.features.user.storage.model.SignupCodeDbm

fun SignupCodeDbm.toDomain(): SignupCode = SignupCode(
  code = code,
  isUsed = isUsed,
  owner = owner.toDomain(),
  createdAt = createdAt,
  usedAt = usedAt,
)

fun SignupCode.toData(project: Project? = null): SignupCodeDbm = SignupCodeDbm(
  code = code,
  isUsed = isUsed,
  owner = owner.toData(project),
  createdAt = createdAt,
  usedAt = usedAt,
)
