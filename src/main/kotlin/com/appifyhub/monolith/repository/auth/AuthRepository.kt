package com.appifyhub.monolith.repository.auth

import com.appifyhub.monolith.domain.user.User
import org.springframework.security.core.Authentication

interface AuthRepository {

  @Throws fun generateToken(user: User, origin: String?): String

  @Throws fun fetchUserByAuthenticationData(authData: Authentication, shallow: Boolean): User

  @Throws fun unauthorizeAuthenticationData(authData: Authentication)

}