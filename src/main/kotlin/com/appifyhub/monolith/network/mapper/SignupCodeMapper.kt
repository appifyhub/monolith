package com.appifyhub.monolith.network.mapper

import com.appifyhub.monolith.domain.user.SignupCode
import com.appifyhub.monolith.network.user.DateTimeMapper
import com.appifyhub.monolith.network.user.SignupCodeResponse
import com.appifyhub.monolith.network.user.SignupCodesResponse

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
