package com.appifyhub.monolith.controller.user

import com.appifyhub.monolith.controller.common.Endpoints
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.network.mapper.toDomain
import com.appifyhub.monolith.network.mapper.toNetwork
import com.appifyhub.monolith.network.user.UserResponse
import com.appifyhub.monolith.network.user.ops.UserSignupRequest
import com.appifyhub.monolith.service.access.AccessManager
import com.appifyhub.monolith.service.access.AccessManager.Privilege
import com.appifyhub.monolith.service.user.UserService
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
  private val userService: UserService,
  private val accessManager: AccessManager,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  @PostMapping(Endpoints.ANY_USER_SIGNUP)
  fun addUser(
    @PathVariable projectId: Long,
    @RequestBody request: UserSignupRequest,
  ): UserResponse {
    log.debug("[POST] add user $request")

    accessManager.requireProjectFunctional(projectId)
    val creator = request.toDomain(projectId)

    return userService.addUser(creator).toNetwork()
  }

  @GetMapping(Endpoints.ANY_USER_UNIVERSAL)
  fun getUser(
    authentication: Authentication,
    @PathVariable universalId: String,
  ): UserResponse {
    log.debug("[GET] universal user $universalId")

    val userId = UserId.fromUniversalFormat(universalId)
    accessManager.requireProjectFunctional(userId.projectId)

    return accessManager.requestUserAccess(authentication, userId, Privilege.USER_READ).toNetwork()
  }

}
