package com.appifyhub.monolith.features.auth.domain

import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.features.auth.domain.model.TokenDetails
import com.appifyhub.monolith.domain.common.stubUser
import com.appifyhub.monolith.domain.mapper.toData
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.features.auth.domain.security.JwtClaims
import com.appifyhub.monolith.features.auth.domain.security.JwtHelper
import com.appifyhub.monolith.features.auth.domain.security.JwtHelper.Claims.AUTHORITIES
import com.appifyhub.monolith.features.auth.domain.security.JwtHelper.Claims.AUTHORITY_DELIMITER
import com.appifyhub.monolith.features.auth.domain.security.JwtHelper.Claims.CREATED_AT
import com.appifyhub.monolith.features.auth.domain.security.JwtHelper.Claims.EXPIRES_AT
import com.appifyhub.monolith.features.auth.domain.security.JwtHelper.Claims.GEO
import com.appifyhub.monolith.features.auth.domain.security.JwtHelper.Claims.IP_ADDRESS
import com.appifyhub.monolith.features.auth.domain.security.JwtHelper.Claims.IS_STATIC
import com.appifyhub.monolith.features.auth.domain.security.JwtHelper.Claims.ORIGIN
import com.appifyhub.monolith.features.auth.domain.security.JwtHelper.Claims.UNIVERSAL_ID
import com.appifyhub.monolith.features.auth.domain.security.JwtHelper.Claims.VALUE
import com.appifyhub.monolith.features.auth.storage.model.TokenDetailsDbm
import java.util.Date
import java.util.concurrent.TimeUnit
import org.springframework.security.core.GrantedAuthority

fun TokenDetailsDbm.toDomain(
  jwtHelper: JwtHelper,
): TokenDetails = jwtHelper
  .extractPropertiesFromJwt(tokenValue)
  .toTokenDetails(isBlocked = blocked)

fun TokenDetails.toData(
  owner: User? = null,
  project: Project? = null,
): TokenDetailsDbm = TokenDetailsDbm(
  tokenValue = tokenValue,
  blocked = isBlocked,
  owner = owner?.toData(project) ?: stubUser().copy(id = ownerId).toData(project),
)

fun JwtClaims.toTokenDetails(
  isBlocked: Boolean = false,
): TokenDetails = TokenDetails(
  tokenValue = get(VALUE).toString(),
  isBlocked = isBlocked,
  createdAt = getJwtDate(CREATED_AT),
  expiresAt = getJwtDate(EXPIRES_AT),
  ownerId = UserId.fromUniversalFormat(get(UNIVERSAL_ID).toString()),
  authority = get(AUTHORITIES).toString()
    .split(AUTHORITY_DELIMITER)
    .map { authorityValue -> GrantedAuthority { authorityValue } }
    .let { User.Authority.find(it, User.Authority.DEFAULT) },
  origin = get(ORIGIN)?.toString(),
  ipAddress = get(IP_ADDRESS)?.toString(),
  geo = get(GEO)?.toString(),
  isStatic = get(IS_STATIC).toString().toBoolean(),
)

private fun JwtClaims.getJwtDate(key: String): Date = Date(
  TimeUnit.SECONDS.toMillis((get(key) as Int).toLong())
)
