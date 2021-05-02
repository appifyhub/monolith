package com.appifyhub.monolith.service.auth

import com.appifyhub.monolith.domain.auth.TokenDetails
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.Authority
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.service.user.UserService.UserPrivilege
import org.springframework.security.core.Authentication

interface AuthService {

  fun isAuthorized(authData: Authentication, forAuthority: Authority, shallow: Boolean): Boolean

  fun isProjectOwner(authData: Authentication, shallow: Boolean): Boolean

  @Throws fun resolveShallowSelf(authData: Authentication): User

  @Throws fun resolveShallowUser(authData: Authentication, universalId: String): User

  @Throws fun requestAccessFor(authData: Authentication, targetId: UserId, privilege: UserPrivilege): User

  @Throws fun resolveUser(universalId: String, signature: String): User

  @Throws fun resolveAdmin(universalId: String, signature: String): User

  @Throws fun createTokenFor(user: User, origin: String?, ipAddress: String?): String

  @Throws fun refreshAuth(authData: Authentication, ipAddress: String?): String

  @Throws fun fetchTokenDetails(authData: Authentication): TokenDetails

  @Throws fun fetchAllTokenDetails(authData: Authentication, valid: Boolean?): List<TokenDetails>

  @Throws fun fetchAllTokenDetailsFor(authData: Authentication, id: UserId, valid: Boolean?): List<TokenDetails>

  @Throws fun unauthorize(authData: Authentication)

  @Throws fun unauthorizeAll(authData: Authentication)

  @Throws fun unauthorizeAllFor(authData: Authentication, id: UserId)

  @Throws fun unauthorizeTokens(authData: Authentication, tokenValues: List<String>)

}
