package com.appifyhub.monolith.controller.admin

import com.appifyhub.monolith.controller.auth.AuthController
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.network.auth.AdminCredentialsRequest
import com.appifyhub.monolith.network.auth.TokenResponse
import com.appifyhub.monolith.network.common.MessageResponse
import com.appifyhub.monolith.service.admin.AdminService
import com.appifyhub.monolith.service.auth.AuthService
import com.appifyhub.monolith.service.user.UserService
import com.appifyhub.monolith.util.requireForAuth
import com.appifyhub.monolith.util.throwUnauthorized
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminAuthController(
  private val authService: AuthService,
  private val adminService: AdminService,
  private val userService: UserService,
) {

  object Endpoints {
    const val ADMIN_AUTH = AuthController.Endpoints.ADMIN_AUTH
    const val ADMIN_REFRESH = AuthController.Endpoints.ADMIN_REFRESH
    const val ADMIN_UNAUTH = AuthController.Endpoints.ADMIN_UNAUTH

    const val ANY_USER_UNAUTH = AuthController.Endpoints.ANY_USER_UNAUTH
  }

  object Privilege {
    val USERS_WRITE = AdminUserController.Privilege.USERS_WRITE
  }

  private val log = LoggerFactory.getLogger(this::class.java)

  @PostMapping(Endpoints.ADMIN_AUTH)
  fun authAdmin(
    @RequestBody creds: AdminCredentialsRequest,
  ): TokenResponse {
    log.debug("[POST] auth admin with $creds")

    val user = try {
      authService.authAdmin(creds.identifier, creds.secret)
    } catch (t: Throwable) {
      log.warn("Failed to find admin identified by ${creds.identifier}", t)
      throwUnauthorized { "Invalid credentials" }
    }

    val token = authService.createTokenFor(user, creds.origin)
    return TokenResponse(token)
  }

  @PostMapping(Endpoints.ADMIN_UNAUTH)
  fun unauthAdmin(authentication: Authentication, @RequestParam all: Boolean): MessageResponse {
    log.debug("[POST] unauth admin with $authentication, [all $all]")
    if (all) {
      authService.unauthorizeAll(authentication)
    } else {
      authService.unauthorize(authentication)
    }
    return MessageResponse.DONE
  }

  @PostMapping(Endpoints.ADMIN_REFRESH)
  fun refreshAdmin(authentication: Authentication): TokenResponse {
    log.debug("[POST] refresh admin with $authentication")
    val token = authService.refreshAuth(authentication)
    return TokenResponse(token)
  }

  @PostMapping(Endpoints.ANY_USER_UNAUTH)
  fun unauthAnyUser(
    authentication: Authentication,
    @PathVariable projectId: Long,
    @PathVariable id: String,
  ): MessageResponse {
    log.debug("[POST] unauth user $id from project $projectId")

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
    val canWriteUsersShallow = shallowRequester.isAuthorizedFor(Privilege.USERS_WRITE)
    requireForAuth(isSelf || canWriteUsersShallow) { "Only ${Privilege.USERS_WRITE.groupName} are authorized" }

    if (isSelf) authService.unauthorizeAll(authentication)

    // check current authorization level
    val requester = userService.fetchUserByUserId(shallowRequester.userId, withTokens = true)
    val canWriteUsers = requester.isAuthorizedFor(Privilege.USERS_WRITE) // creds might have changed
    requireForAuth(canWriteUsers) { "Only ${Privilege.USERS_WRITE.groupName} are authorized" }

    // check if authorization level is enough
    val target = userService.fetchUserByUserId(targetUserId, withTokens = false)
    val hasHighAuthority = requester.authority.ordinal > target.authority.ordinal
    requireForAuth(hasHighAuthority) { "Only ${target.authority.nextGroupName} are authorized" }

    authService.unauthorizeAllFor(authentication, target.userId)

    return MessageResponse.DONE
  }

}