package com.appifyhub.monolith.repository.auth

import com.appifyhub.monolith.domain.user.User
import org.springframework.security.core.Authentication

interface AuthRepository {

  @Throws fun generateToken(user: User, origin: String? = null): String

  @Throws fun fetchUserByIdentification(
    authentication: Authentication,
    shallow: Boolean = false,
  ): User

}