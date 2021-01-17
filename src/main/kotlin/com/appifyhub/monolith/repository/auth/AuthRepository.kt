package com.appifyhub.monolith.repository.auth

import com.appifyhub.monolith.domain.auth.OwnedToken
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

interface AuthRepository {

  @Throws fun createToken(userId: UserId, authorities: List<GrantedAuthority>, origin: String?): String

  @Throws fun resolveShallowUser(token: JwtAuthenticationToken): User

  @Throws fun fetchTokenDetails(token: JwtAuthenticationToken): OwnedToken

  @Throws fun checkIsValid(token: JwtAuthenticationToken, shallow: Boolean): Boolean

  @Throws fun requireValid(token: JwtAuthenticationToken, shallow: Boolean)

  @Throws fun unauthorizeToken(token: JwtAuthenticationToken)

  @Throws fun unauthorizeAllTokens(token: JwtAuthenticationToken)

  @Throws fun unauthorizeAllTokensFor(userId: UserId)

}