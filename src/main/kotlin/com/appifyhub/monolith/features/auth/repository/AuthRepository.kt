package com.appifyhub.monolith.features.auth.repository

import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.features.auth.domain.model.TokenCreator
import com.appifyhub.monolith.features.auth.domain.model.TokenDetails
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

interface AuthRepository {

  @Throws fun isTokenValid(jwt: JwtAuthenticationToken, shallow: Boolean): Boolean

  @Throws fun isTokenStatic(jwt: JwtAuthenticationToken): Boolean

  @Throws fun createToken(creator: TokenCreator): TokenDetails

  @Throws fun resolveShallowUser(jwt: JwtAuthenticationToken): User

  @Throws fun fetchTokenDetails(jwt: JwtAuthenticationToken): TokenDetails

  @Throws fun fetchAllTokenDetails(jwt: JwtAuthenticationToken, valid: Boolean?): List<TokenDetails>

  @Throws fun fetchAllTokenDetailsFor(id: UserId, valid: Boolean?): List<TokenDetails>

  @Throws fun unauthorizeToken(jwt: JwtAuthenticationToken)

  @Throws fun unauthorizeAllTokens(jwt: JwtAuthenticationToken)

  @Throws fun unauthorizeAllTokensFor(id: UserId)

  @Throws fun unauthorizeAllTokens(tokenValues: List<String>)

}
