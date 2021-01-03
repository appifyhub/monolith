package com.appifyhub.monolith.service.auth

import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.Authority
import org.springframework.security.core.Authentication

interface AuthService {

  fun isAuthorized(authentication: Authentication, forAuthority: Authority): Boolean

  fun isProjectOwner(authentication: Authentication, projectSignature: String): Boolean

  @Throws fun fetchUserByAuthenticating(authentication: Authentication, shallow: Boolean = false): User

  @Throws fun fetchUserByCredentials(projectSignature: String, identifier: String, signature: String): User

  @Throws fun fetchAdminUserByCredentials(identifier: String, signature: String): User

  @Throws fun generateTokenFor(user: User, origin: String?): String

}