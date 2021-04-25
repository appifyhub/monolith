package com.appifyhub.monolith.network.mapper

import com.appifyhub.monolith.domain.auth.OwnedToken
import com.appifyhub.monolith.domain.auth.Token
import com.appifyhub.monolith.network.auth.TokenDetailsResponse
import com.appifyhub.monolith.network.auth.TokenResponse
import com.appifyhub.monolith.network.user.DateTimeMapper

fun Token.toNetwork() = TokenResponse(
  token = tokenLocator,
)

fun OwnedToken.toNetwork() = TokenDetailsResponse(
  ownerId = owner.userId.id,
  ownerProjectId = owner.userId.projectId,
  ownerUniversalId = owner.userId.toUniversalFormat(),
  tokenId = token.toNetwork().token,
  isBlocked = isBlocked,
  origin = origin,
  createdAt = DateTimeMapper.formatAsDateTime(createdAt),
  expiresAt = DateTimeMapper.formatAsDateTime(expiresAt),
)
