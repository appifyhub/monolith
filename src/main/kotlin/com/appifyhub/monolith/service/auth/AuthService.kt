package com.appifyhub.monolith.service.auth

import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.Authority
import com.appifyhub.monolith.domain.user.UserId
import org.springframework.security.core.Authentication

interface AuthService {

  fun isAuthorized(authData: Authentication, forAuthority: Authority, shallow: Boolean): Boolean

  fun isProjectOwner(authData: Authentication, projectSignature: String, shallow: Boolean): Boolean

  @Throws fun resolveShallowUser(authData: Authentication): User

  @Throws fun authUser(identifier: String, signature: String, projectSignature: String): User

  @Throws fun authAdmin(identifier: String, signature: String): User

  @Throws fun createTokenFor(user: User, origin: String?): String

  @Throws fun refreshAuth(authData: Authentication): String

  @Throws fun unauthorize(authData: Authentication)

  @Throws fun unauthorizeAll(authData: Authentication)

  @Throws fun unauthorizeAllFor(authData: Authentication, userId: UserId)

}