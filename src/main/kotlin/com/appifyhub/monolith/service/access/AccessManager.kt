package com.appifyhub.monolith.service.access

import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.creator.setup.ProjectState
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.Authority
import com.appifyhub.monolith.domain.user.User.Authority.ADMIN
import com.appifyhub.monolith.domain.user.User.Authority.MODERATOR
import com.appifyhub.monolith.domain.user.User.Authority.OWNER
import com.appifyhub.monolith.domain.user.UserId
import org.springframework.security.core.Authentication

interface AccessManager {

  enum class Privilege(val level: Authority) {
    PROJECT_READ(OWNER),
    PROJECT_WRITE(OWNER),
    USER_SEARCH(ADMIN),
    USER_READ_TOKEN(ADMIN),
    USER_READ_DATA(MODERATOR),
    USER_WRITE_TOKEN(ADMIN),
    USER_WRITE_AUTHORITY(OWNER),
    USER_WRITE_DATA(ADMIN),
    USER_WRITE_SIGNATURE(ADMIN),
    USER_WRITE_VERIFICATION(ADMIN),
    USER_DELETE(OWNER),
  }

  enum class Feature(val isRequired: Boolean) {
    BASIC(isRequired = true), // core functionality: service health, status check, feature check, security...
    USERS(isRequired = true), // user-related functionality: signup, login, user data updates...
    DEMO(isRequired = false), // demoing service functionality
  }

  @Throws fun requestUserAccess(authData: Authentication, targetId: UserId, privilege: Privilege): User

  @Throws fun requestProjectAccess(authData: Authentication, targetId: Long, privilege: Privilege): Project

  @Throws fun requestCreator(authData: Authentication, matchesId: UserId?, requireVerified: Boolean): User

  @Throws fun requestSuperCreator(authData: Authentication): User

  @Throws fun fetchProjectState(targetId: Long): ProjectState

  @Throws fun requireProjectFunctional(targetId: Long)

  @Throws fun requireProjectFeaturesFunctional(targetId: Long, vararg features: Feature)

}
