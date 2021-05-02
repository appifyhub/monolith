package com.appifyhub.monolith.network.mapper

import com.appifyhub.monolith.domain.auth.TokenDetails
import com.appifyhub.monolith.network.auth.TokenDetailsResponse
import com.appifyhub.monolith.network.auth.TokenResponse
import com.appifyhub.monolith.network.user.DateTimeMapper

fun tokenResponseOf(tokenValue: String): TokenResponse = TokenResponse(tokenValue)

fun TokenDetails.toNetwork() = TokenDetailsResponse(
  tokenValue = tokenValue,
  ownerId = ownerId.id,
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
