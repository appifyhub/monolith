package com.appifyhub.monolith.domain.mapper

import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.auth.TokenDetails
import com.appifyhub.monolith.domain.common.stubUser
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.security.JwtClaims
import com.appifyhub.monolith.security.JwtHelper
import com.appifyhub.monolith.security.JwtHelper.Claims.ACCOUNT_ID
import com.appifyhub.monolith.security.JwtHelper.Claims.AUTHORITIES
import com.appifyhub.monolith.security.JwtHelper.Claims.AUTHORITY_DELIMITER
import com.appifyhub.monolith.security.JwtHelper.Claims.CREATED_AT
import com.appifyhub.monolith.security.JwtHelper.Claims.EXPIRES_AT
import com.appifyhub.monolith.security.JwtHelper.Claims.GEO
import com.appifyhub.monolith.security.JwtHelper.Claims.IP_ADDRESS
import com.appifyhub.monolith.security.JwtHelper.Claims.IS_STATIC
import com.appifyhub.monolith.security.JwtHelper.Claims.ORIGIN
import com.appifyhub.monolith.security.JwtHelper.Claims.UNIVERSAL_ID
import com.appifyhub.monolith.security.JwtHelper.Claims.VALUE
import com.appifyhub.monolith.storage.model.auth.TokenDetailsDbm
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
  accountId = get(ACCOUNT_ID)?.toString()?.toLong(),
  isStatic = get(IS_STATIC).toString().toBoolean(),
)

private fun JwtClaims.getJwtDate(key: String): Date = Date(
  TimeUnit.SECONDS.toMillis((get(key) as Int).toLong())
)
