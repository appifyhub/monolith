package com.appifyhub.monolith.features.auth.api

import com.appifyhub.monolith.features.auth.domain.model.TokenDetails
import com.appifyhub.monolith.features.auth.api.model.TokenDetailsResponse
import com.appifyhub.monolith.features.auth.api.model.TokenResponse
import com.appifyhub.monolith.network.user.DateTimeMapper

fun tokenResponseOf(tokenValue: String): TokenResponse = TokenResponse(tokenValue)

fun TokenDetails.toNetwork() = TokenDetailsResponse(
  tokenValue = tokenValue,
  ownerId = ownerId.userId,
  ownerProjectId = ownerId.projectId,
  ownerUniversalId = ownerId.toUniversalFormat(),
  createdAt = DateTimeMapper.formatAsDateTime(createdAt),
  expiresAt = DateTimeMapper.formatAsDateTime(expiresAt),
  authority = authority.name,
  isBlocked = isBlocked,
  origin = origin,
  ipAddress = ipAddress,
  geo = geo,
  isStatic = isStatic,
)
