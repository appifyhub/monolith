package com.appifyhub.monolith.domain.mapper

import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.user.SignupCode
import com.appifyhub.monolith.storage.model.user.SignupCodeDbm

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
