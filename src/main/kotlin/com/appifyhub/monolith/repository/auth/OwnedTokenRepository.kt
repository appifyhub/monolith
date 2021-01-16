package com.appifyhub.monolith.repository.auth

import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.auth.OwnedToken
import com.appifyhub.monolith.domain.auth.Token
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import java.util.Date

interface OwnedTokenRepository {

  @Throws fun addToken(
    userId: UserId,
    token: Token,
    createdAt: Date,
    expiresAt: Date,
    origin: String?,
  ): OwnedToken

  @Throws fun fetchTokenDetails(token: Token): OwnedToken

  @Throws fun fetchAllBlockedTokens(owner: User, project: Project): List<OwnedToken>

  @Throws fun fetchAllValidTokens(owner: User, project: Project): List<OwnedToken>

  @Throws fun fetchAllTokens(owner: User, project: Project): List<OwnedToken>

  @Throws fun checkIsExpired(token: Token): Boolean

  @Throws fun checkIsBlocked(token: Token): Boolean

  @Throws fun blockToken(token: Token): OwnedToken

  @Throws fun blockAllTokens(owner: User): List<OwnedToken>

}