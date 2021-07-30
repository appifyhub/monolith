package com.appifyhub.monolith.service.auth

import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.Authority
import com.appifyhub.monolith.domain.user.User.Authority.ADMIN
import com.appifyhub.monolith.domain.user.User.Authority.MODERATOR
import com.appifyhub.monolith.domain.user.User.Authority.OWNER
import com.appifyhub.monolith.domain.user.UserId
import org.springframework.security.core.Authentication

interface AccessManager {

  enum class Privilege(val level: Authority) {
    USER_READ(MODERATOR),
    USER_WRITE(ADMIN),
    PROJECT_READ(OWNER),
    PROJECT_WRITE(OWNER),
  }

  @Throws fun requestUserAccess(authData: Authentication, targetId: UserId, privilege: Privilege): User

  @Throws fun requestProjectAccess(authData: Authentication, targetId: Long, privilege: Privilege): Project

}
