package com.appifyhub.monolith.domain.mapper

import com.appifyhub.monolith.domain.auth.OwnedToken
import com.appifyhub.monolith.domain.auth.Token
import com.appifyhub.monolith.storage.model.auth.OwnedTokenDbm
import com.appifyhub.monolith.storage.model.auth.TokenDbm

fun OwnedTokenDbm.toDomain(): OwnedToken = OwnedToken(
  token = Token(tokenLocator),
  isBlocked = isBlocked,
  origin = origin,
  createdAt = createdAt,
  expiresAt = expiresAt,
  owner = owner.toDomain(),
)

fun OwnedToken.toData(): OwnedTokenDbm = OwnedTokenDbm(
  tokenLocator = token.tokenLocator,
  isBlocked = isBlocked,
  origin = origin,
  createdAt = createdAt,
  expiresAt = expiresAt,
  owner = owner.toData(),
)

fun TokenDbm.toDomain(): Token = Token(
  tokenLocator = tokenLocator,
)

fun Token.toData(): TokenDbm = TokenDbm(
  tokenLocator = tokenLocator,
)