package com.appifyhub.monolith.controller.creator

import com.appifyhub.monolith.controller.common.Endpoints
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.network.mapper.toNetwork
import com.appifyhub.monolith.network.user.UserResponse
import com.appifyhub.monolith.service.access.AccessManager
import com.appifyhub.monolith.service.access.AccessManager.Privilege
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class CreatorUserController(
  private val accessManager: AccessManager,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  @GetMapping(Endpoints.ANY_USER)
  fun getAnyUser(
    authentication: Authentication,
    @PathVariable projectId: Long,
    @PathVariable userId: String,
  ): UserResponse {
    log.debug("[GET] user $userId from project $projectId")

    return accessManager.requestUserAccess(
      authData = authentication,
      targetId = UserId(userId, projectId),
      privilege = Privilege.USER_READ,
    ).toNetwork()
  }

}
