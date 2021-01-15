package com.appifyhub.monolith.repository.auth

import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.auth.OwnedToken
import com.appifyhub.monolith.domain.auth.Token
import com.appifyhub.monolith.domain.user.User

interface OwnedTokenRepository {

  @Throws fun addToken(user: User, token: Token, origin: String?): OwnedToken

  @Throws fun blockToken(token: Token): OwnedToken

  @Throws fun fetchTokenDetails(token: Token): OwnedToken

  @Throws fun fetchAllBlockedTokens(owner: User, project: Project): List<OwnedToken>

  @Throws fun fetchAllValidTokens(owner: User, project: Project): List<OwnedToken>

  @Throws fun fetchAllTokens(owner: User, project: Project): List<OwnedToken>

  @Throws fun checkIsValid(token: Token): Boolean

  @Throws fun checkIsBlocked(token: Token): Boolean

}