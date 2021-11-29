package com.appifyhub.monolith.controller.user

import com.appifyhub.monolith.controller.common.Endpoints
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

  private val log = LoggerFactory.getLogger(this::class.java)

  @GetMapping(Endpoints.ONE_USER)
  fun getUser(
    authentication: Authentication,
    @PathVariable universalId: String,
  ): UserResponse {
    log.debug("[GET] universal user $universalId")

    // FIXME missing checks
    //   - project is active
    //   - user is verified (maybe here for 'self' access is allowed?)
    //   - READ permissions allowed

    // get auth data
    val shallowUser = authService.resolveShallowUser(
      authData = authentication,
      universalId = universalId,
    )

    // fetch non-shallow data
    val user = userService.fetchUserByUserId(shallowUser.id)
    return user.toNetwork()
  }

}
