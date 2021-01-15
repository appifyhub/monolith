package com.appifyhub.monolith.service.auth

import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.Authority
import org.springframework.security.core.Authentication

interface AuthService {

  fun isAuthorized(authData: Authentication, forAuthority: Authority): Boolean

  fun isProjectOwner(authData: Authentication, projectSignature: String): Boolean

  @Throws fun fetchUserByAuthenticating(authData: Authentication, shallow: Boolean): User

  @Throws fun fetchUserByCredentials(
    projectSignature: String,
    identifier: String,
    signature: String,
    withTokens: Boolean,
  ): User

  @Throws fun fetchAdminUserByCredentials(identifier: String, signature: String, withTokens: Boolean): User

  @Throws fun generateTokenFor(user: User, origin: String?): String

  @Throws fun unauthorizeAuthenticationData(authData: Authentication)

}