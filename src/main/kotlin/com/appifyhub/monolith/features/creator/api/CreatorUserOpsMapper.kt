package com.appifyhub.monolith.features.creator.api

import com.appifyhub.monolith.features.user.domain.model.User
import com.appifyhub.monolith.features.user.domain.model.UserCreator
import com.appifyhub.monolith.features.creator.api.model.user.CreatorSignupRequest
import com.appifyhub.monolith.features.user.api.toDomain
import com.appifyhub.monolith.features.user.api.DateTimeMapper
import java.util.Locale

fun CreatorSignupRequest.toDomain(
  projectId: Long,
): UserCreator = UserCreator(
  userId = userId,
  projectId = projectId,
  rawSignature = rawSignature,
  name = name,
  type = User.Type.find(type, default = User.Type.ORGANIZATION),
  authority = User.Authority.DEFAULT,
  allowsSpam = true,
  contact = userId,
  contactType = User.ContactType.EMAIL,
  birthday = birthday?.let { DateTimeMapper.parseAsDate(it) },
  company = company?.toDomain(),
  languageTag = Locale.US.toLanguageTag(),
  signupCode = signupCode,
)
