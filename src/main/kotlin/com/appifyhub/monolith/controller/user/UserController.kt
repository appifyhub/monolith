package com.appifyhub.monolith.controller.user

import com.appifyhub.monolith.controller.common.Headers.PROJECT_SIGNATURE
import com.appifyhub.monolith.controller.user.UserController.Endpoints.ONE_USER
import com.appifyhub.monolith.controller.user.UserController.Endpoints.PROJECT_ONE_USER
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.repository.mapper.toSecurityUser
import com.appifyhub.monolith.service.admin.AdminService
import com.appifyhub.monolith.service.auth.AuthService
import com.appifyhub.monolith.service.user.UserService
import com.appifyhub.monolith.util.unauthorized
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
  private val userService: UserService,
  private val authService: AuthService,
  private val adminService: AdminService,
) {

  object Endpoints {
    const val ONE_USER = "/v1/global-project/users/{unifiedId}"
    const val PROJECT_ONE_USER = "/v1/projects/{projectId}/users/{id}"
    const val PROJECT_ALL_USERS = "/v1/projects/{projectId}/users"
  }

  private data class ShallowAuthData(val user: User, val project: Project)

  private val log = LoggerFactory.getLogger(this::class.java)

  @GetMapping(ONE_USER)
  fun getUser(
    authentication: Authentication,
    @PathVariable unifiedId: String,
    @RequestHeader(PROJECT_SIGNATURE) projectSignature: String,
  ): User {
    log.debug("[GET] one user ($unifiedId, $projectSignature)")

    val shallowAuthData = fetchShallowAuthData(authentication, projectSignature)
    with(shallowAuthData) {
      if (user.userId.toUnifiedFormat() != unifiedId) unauthorized("Wrong unified ID")
      if (!user.belongsTo(project)) unauthorized("Wrong project signature")
    }

    // fetch non-shallow data
    return userService.fetchUserByUserId(shallowAuthData.user.userId)
    // TODO MM don't use UserDetails
  }

  @GetMapping(PROJECT_ONE_USER)
  fun getUser(
    authentication: Authentication,
    @PathVariable projectId: Long,
    @PathVariable id: String,
    @RequestHeader(PROJECT_SIGNATURE) projectSignature: String,
  ): User {
    log.debug("[GET] one user ($projectId, $id, $projectSignature)")

    val shallowAuthData = fetchShallowAuthData(authentication, projectSignature)
    with(shallowAuthData) {
      if (user.userId.id != id || user.userId.projectId != projectId) unauthorized("Wrong project/user ID")
      if (!user.belongsTo(project)) unauthorized("Wrong project signature")
    }

    // fetch non-shallow data
    return userService.fetchUserByUserId(shallowAuthData.user.userId)
    // TODO MM don't use UserDetails
  }

  // TODO MM use service.fetchAll by project

  // Helpers

  @Throws
  private fun fetchShallowAuthData(authentication: Authentication, projectSignature: String): ShallowAuthData {
    try {
      val user = authService.fetchUserByAuthenticating(authentication, shallow = true)
      val project = adminService.fetchProjectBySignature(projectSignature)
      return ShallowAuthData(user, project)
    } catch (t: Throwable) {
      log.warn("User and project could not be resolved (shallow auth)", t)
      throw t
    }
  }

}