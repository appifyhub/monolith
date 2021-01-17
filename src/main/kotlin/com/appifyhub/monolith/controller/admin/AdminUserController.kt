package com.appifyhub.monolith.controller.admin

import com.appifyhub.monolith.controller.user.UserController
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.network.mapper.toNetwork
import com.appifyhub.monolith.network.user.UserResponse
import com.appifyhub.monolith.service.admin.AdminService
import com.appifyhub.monolith.service.auth.AuthService
import com.appifyhub.monolith.service.user.UserService
import com.appifyhub.monolith.util.requireForAuth
import com.appifyhub.monolith.util.throwUnauthorized
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminUserController(
  private val userService: UserService,
  private val authService: AuthService,
  private val adminService: AdminService,
) {

  object Endpoints {
    const val ANY_USER = UserController.Endpoints.ANY_USER
  }

  object Privilege {
    // MM make configurable?
    val USERS_READ = User.Authority.MODERATOR
    val USERS_WRITE = User.Authority.ADMIN
  }

  private val log = LoggerFactory.getLogger(this::class.java)

  @GetMapping(Endpoints.ANY_USER)
  fun getAnyUser(
    authentication: Authentication,
    @PathVariable projectId: Long,
    @PathVariable id: String,
  ): UserResponse {
    log.debug("[GET] user $id from project $projectId")

    val shallowRequester: User
    val project: Project
    try {
      shallowRequester = authService.resolveShallowUser(authentication)
      project = adminService.fetchProjectById(projectId)
    } catch (t: Throwable) {
      log.warn("User could not be resolved from auth data", t)
      throwUnauthorized { t.message.orEmpty() }
    }

    // quick check to prevent unnecessary queries
    val targetUserId = UserId(id, project.id)
    val isSelf = shallowRequester.userId == targetUserId
    val canReadUsersShallow = shallowRequester.isAuthorizedFor(Privilege.USERS_READ)
    requireForAuth(isSelf || canReadUsersShallow) { "Only ${Privilege.USERS_READ.groupName} are authorized" }

    // fetch non-shallow data for requester
    val requester = userService.fetchUserByUserId(shallowRequester.userId, withTokens = false)
    if (isSelf) return requester.toNetwork()

    // check minimum authorization level
    val canReadUsers = requester.isAuthorizedFor(Privilege.USERS_READ) // creds might have changed
    requireForAuth(canReadUsers) { "Only ${Privilege.USERS_READ.groupName} are authorized" }

    // check if authorization level is enough
    val target = userService.fetchUserByUserId(targetUserId, withTokens = false)
    val hasHighAuthority = requester.authority.ordinal > target.authority.ordinal
    requireForAuth(hasHighAuthority) { "Only ${target.authority.nextGroupName} are authorized" }

    return target.toNetwork()
  }

}