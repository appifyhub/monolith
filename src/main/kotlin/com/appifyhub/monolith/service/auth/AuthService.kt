package com.appifyhub.monolith.service.auth

import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.Authority
import org.springframework.security.core.Authentication

interface AuthService {

  fun isAuthorized(authData: Authentication, forAuthority: Authority, shallow: Boolean): Boolean

  fun isProjectOwner(authData: Authentication, projectSignature: String, shallow: Boolean): Boolean

  @Throws fun resolveShallowUser(authData: Authentication): User

  @Throws fun authenticateUser(identifier: String, signature: String, projectSignature: String): User

  @Throws fun authenticateAdmin(identifier: String, signature: String): User

  @Throws fun createTokenFor(user: User, origin: String?): String

  @Throws fun refreshAuthentication(authData: Authentication): String

  @Throws fun unauthorizeAuthentication(authData: Authentication)

  @Throws fun unauthorizeAllAuthentication(authData: Authentication)

}