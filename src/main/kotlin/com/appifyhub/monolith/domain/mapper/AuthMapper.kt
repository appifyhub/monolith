package com.appifyhub.monolith.domain.mapper

import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.auth.OwnedToken
import com.appifyhub.monolith.domain.auth.Token
import com.appifyhub.monolith.storage.model.auth.OwnedTokenDbm
import com.appifyhub.monolith.storage.model.auth.TokenDbm

fun OwnedTokenDbm.toDomain(): OwnedToken = OwnedToken(
  token = Token(tokenLocator),
  isBlocked = blocked,
  origin = origin,
  createdAt = createdAt,
  expiresAt = expiresAt,
  owner = owner.toDomain(),
)

fun OwnedToken.toData(
  project: Project? = null,
): OwnedTokenDbm = OwnedTokenDbm(
  tokenLocator = token.tokenLocator,
  blocked = isBlocked,
  origin = origin,
  createdAt = createdAt,
  expiresAt = expiresAt,
  owner = owner.toData(project),
)

fun TokenDbm.toDomain(): Token = Token(
  tokenLocator = tokenLocator,
)