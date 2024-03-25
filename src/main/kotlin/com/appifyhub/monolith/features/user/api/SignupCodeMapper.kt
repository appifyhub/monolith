package com.appifyhub.monolith.features.user.api

import com.appifyhub.monolith.features.user.api.model.SignupCodeResponse
import com.appifyhub.monolith.features.user.api.model.SignupCodesResponse
import com.appifyhub.monolith.features.user.domain.model.SignupCode

fun SignupCode.toNetwork() = SignupCodeResponse(
  code = code,
  isUsed = isUsed,
  createdAt = DateTimeMapper.formatAsDateTime(createdAt),
  usedAt = usedAt?.let { DateTimeMapper.formatAsDateTime(it) },
)

fun Collection<SignupCode>.toNetwork(
  maxSignupCodes: Int,
) = SignupCodesResponse(
  signupCodes = map(SignupCode::toNetwork),
  maxSignupCodes = maxSignupCodes,
)
