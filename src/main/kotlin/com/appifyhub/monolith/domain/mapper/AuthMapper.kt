package com.appifyhub.monolith.domain.mapper

import com.appifyhub.monolith.domain.auth.BlockedToken
import com.appifyhub.monolith.domain.auth.Token
import com.appifyhub.monolith.storage.model.auth.BlockedTokenDbm
import com.appifyhub.monolith.storage.model.auth.TokenDbm

fun BlockedTokenDbm.toDomain(): BlockedToken = BlockedToken(
  token = Token(token),
  owner = owner.toDomain(),
)

fun BlockedToken.toData(): BlockedTokenDbm = BlockedTokenDbm(
  token = token.token,
  owner = owner.toData(),
)

fun TokenDbm.toDomain(): Token = Token(
  token = token,
)

fun Token.toData(): TokenDbm = TokenDbm(
  token = token,
)