package com.appifyhub.monolith.controller.admin

import com.appifyhub.monolith.controller.user.UserController
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.network.mapper.toNetwork
import com.appifyhub.monolith.network.user.UserResponse
import com.appifyhub.monolith.service.auth.AuthService
import com.appifyhub.monolith.service.user.UserService.UserPrivilege
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminUserController(
  private val authService: AuthService,
) {

  object Endpoints {
    const val ANY_USER = UserController.Endpoints.ANY_USER
  }

  private val log = LoggerFactory.getLogger(this::class.java)

  @GetMapping(Endpoints.ANY_USER)
  fun getAnyUser(
    authentication: Authentication,
    @PathVariable projectId: Long,
    @PathVariable userId: String,
  ): UserResponse {
    log.debug("[GET] user $userId from project $projectId")

    return authService.requestAccessFor(
      authData = authentication,
      targetId = UserId(userId, projectId),
      privilege = UserPrivilege.READ,
    ).toNetwork()
  }

}
