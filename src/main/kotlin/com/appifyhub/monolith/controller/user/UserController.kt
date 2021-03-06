package com.appifyhub.monolith.controller.user

import com.appifyhub.monolith.controller.user.UserController.Endpoints.ONE_USER
import com.appifyhub.monolith.network.mapper.toNetwork
import com.appifyhub.monolith.network.user.UserResponse
import com.appifyhub.monolith.service.auth.AuthService
import com.appifyhub.monolith.service.user.UserService
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
  private val userService: UserService,
  private val authService: AuthService,
) {

  object Endpoints {
    const val ONE_USER = "/v1/universal/users/{universalId}"

    const val ANY_USER = "/v1/projects/{projectId}/users/{userId}"
  }

  private val log = LoggerFactory.getLogger(this::class.java)

  @GetMapping(ONE_USER)
  fun getUser(
    authentication: Authentication,
    @PathVariable universalId: String,
  ): UserResponse {
    log.debug("[GET] universal user $universalId")

    // get auth data
    val shallowUser = authService.resolveShallowUser(
      authData = authentication,
      universalId = universalId,
    )

    // fetch non-shallow data
    val user = userService.fetchUserByUserId(shallowUser.id, withTokens = true)
    return user.toNetwork()
  }

}
