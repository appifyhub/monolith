package com.appifyhub.monolith.features.auth.repository

import com.appifyhub.monolith.features.user.domain.model.User
import com.appifyhub.monolith.features.auth.domain.model.TokenDetails
import com.appifyhub.monolith.features.creator.domain.model.Project

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
