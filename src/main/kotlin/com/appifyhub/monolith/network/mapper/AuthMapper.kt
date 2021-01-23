package com.appifyhub.monolith.network.mapper

import com.appifyhub.monolith.domain.auth.OwnedToken
import com.appifyhub.monolith.network.auth.TokenDetailsResponse
import com.appifyhub.monolith.network.user.DateTimeMapper

fun OwnedToken.toNetwork() = TokenDetailsResponse(
  ownerId = owner.userId.id,
  ownerProjectID = owner.userId.projectId,
  tokenId = token.tokenLocator,
  isBlocked = isBlocked,
  origin = origin,
  createdAt = DateTimeMapper.formatAsDateTime(createdAt),
  expiresAt = DateTimeMapper.formatAsDateTime(expiresAt),
)