package com.appifyhub.monolith.service.auth

import com.appifyhub.monolith.domain.auth.OwnedToken
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.Authority
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.service.user.UserService.UserPrivilege
import org.springframework.security.core.Authentication

interface AuthService {

  fun isAuthorized(authData: Authentication, forAuthority: Authority, shallow: Boolean): Boolean

  fun isProjectOwner(authData: Authentication, shallow: Boolean): Boolean

  @Throws fun resolveShallowUser(authData: Authentication, universalId: String): User

  @Throws fun requestAccessFor(authData: Authentication, targetUserId: UserId, privilege: UserPrivilege): User

  @Throws fun resolveUser(universalId: String, signature: String): User

  @Throws fun resolveAdmin(universalId: String, signature: String): User

  @Throws fun createTokenFor(user: User, origin: String?): String

  @Throws fun refreshAuth(authData: Authentication): String

  @Throws fun fetchTokenDetails(authData: Authentication): OwnedToken

  @Throws fun fetchAllTokenDetails(authData: Authentication, valid: Boolean?): List<OwnedToken>

  @Throws fun fetchAllTokenDetailsFor(authData: Authentication, userId: UserId, valid: Boolean?): List<OwnedToken>

  @Throws fun unauthorize(authData: Authentication)

  @Throws fun unauthorizeAll(authData: Authentication)

  @Throws fun unauthorizeAllFor(authData: Authentication, userId: UserId)

  @Throws fun unauthorizeTokens(authData: Authentication, tokenLocators: List<String>)

}
