package com.appifyhub.monolith.service.auth

import com.appifyhub.monolith.domain.auth.TokenDetails
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

interface AuthService {

  @Throws fun requireValidJwt(authData: Authentication, shallow: Boolean): JwtAuthenticationToken

  @Throws fun resolveShallowSelf(authData: Authentication): User

  @Throws fun resolveShallowUser(authData: Authentication, universalId: String): User

  @Throws fun resolveUser(universalId: String, signature: String): User

  @Throws fun resolveAdmin(universalId: String, signature: String): User

  @Throws fun createTokenFor(user: User, origin: String?, ipAddress: String?): String

  @Throws fun createStaticTokenFor(user: User, origin: String?, ipAddress: String?): String

  @Throws fun refreshAuth(authData: Authentication, ipAddress: String?): String

  @Throws fun fetchTokenDetails(authData: Authentication): TokenDetails

  @Throws fun fetchAllTokenDetails(authData: Authentication, valid: Boolean?): List<TokenDetails>

  @Throws fun fetchAllTokenDetailsFor(authData: Authentication, targetId: UserId, valid: Boolean?): List<TokenDetails>

  @Throws fun unauthorize(authData: Authentication)

  @Throws fun unauthorizeAll(authData: Authentication)

  @Throws fun unauthorizeAllFor(authData: Authentication, targetId: UserId)

  @Throws fun unauthorizeTokens(authData: Authentication, tokenValues: List<String>)

}
