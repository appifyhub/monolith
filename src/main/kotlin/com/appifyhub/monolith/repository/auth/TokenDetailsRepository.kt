package com.appifyhub.monolith.repository.auth

import com.appifyhub.monolith.domain.auth.TokenDetails
import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.user.User

interface TokenDetailsRepository {

  @Throws fun addToken(token: TokenDetails): TokenDetails

  @Throws fun fetchTokenDetails(tokenValue: String): TokenDetails

  @Throws fun fetchAllTokenDetails(tokenValues: List<String>): List<TokenDetails>

  @Throws fun fetchAllBlockedTokens(owner: User, project: Project?): List<TokenDetails>

  @Throws fun fetchAllValidTokens(owner: User, project: Project?): List<TokenDetails>

  @Throws fun fetchAllTokens(owner: User, project: Project?): List<TokenDetails>

  @Throws fun checkIsExpired(tokenValue: String): Boolean

  @Throws fun checkIsBlocked(tokenValue: String): Boolean

  @Throws fun checkIsStatic(tokenValue: String): Boolean

  @Throws fun blockToken(tokenValue: String): TokenDetails

  @Throws fun blockAllTokens(tokenValues: List<String>): List<TokenDetails>

  @Throws fun removeTokensFor(owner: User, project: Project?)

}
