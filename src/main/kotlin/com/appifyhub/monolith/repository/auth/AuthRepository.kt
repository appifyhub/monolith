package com.appifyhub.monolith.repository.auth

import com.appifyhub.monolith.domain.auth.TokenDetails
import com.appifyhub.monolith.domain.auth.ops.TokenCreator
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

interface AuthRepository {

  @Throws fun createToken(creator: TokenCreator): TokenDetails

  @Throws fun resolveShallowUser(jwt: JwtAuthenticationToken): User

  @Throws fun fetchTokenDetails(jwt: JwtAuthenticationToken): TokenDetails

  @Throws fun fetchAllTokenDetails(jwt: JwtAuthenticationToken, valid: Boolean?): List<TokenDetails>

  @Throws fun fetchAllTokenDetailsFor(id: UserId, valid: Boolean?): List<TokenDetails>

  @Throws fun checkIsValid(jwt: JwtAuthenticationToken, shallow: Boolean): Boolean

  @Throws fun requireValid(jwt: JwtAuthenticationToken, shallow: Boolean)

  @Throws fun unauthorizeToken(jwt: JwtAuthenticationToken)

  @Throws fun unauthorizeAllTokens(jwt: JwtAuthenticationToken)

  @Throws fun unauthorizeAllTokensFor(id: UserId)

  @Throws fun unauthorizeAllTokens(tokenValues: List<String>)

}
