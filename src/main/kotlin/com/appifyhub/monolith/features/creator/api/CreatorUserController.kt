package com.appifyhub.monolith.features.creator.api

import com.appifyhub.monolith.controller.common.Endpoints
import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.domain.user.ops.UserUpdater
import com.appifyhub.monolith.features.auth.domain.access.AccessManager
import com.appifyhub.monolith.features.auth.domain.access.AccessManager.Privilege
import com.appifyhub.monolith.features.creator.api.model.user.CreatorSignupRequest
import com.appifyhub.monolith.features.creator.domain.service.CreatorService
import com.appifyhub.monolith.network.common.SimpleResponse
import com.appifyhub.monolith.network.mapper.toNetwork
import com.appifyhub.monolith.network.user.UserResponse
import com.appifyhub.monolith.service.user.UserService
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class CreatorUserController(
  private val userService: UserService,
  private val creatorService: CreatorService,
  private val accessManager: AccessManager,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  @PostMapping(Endpoints.CREATOR_SIGNUP)
  fun addUser(
    @RequestBody request: CreatorSignupRequest,
  ): UserResponse {
    log.debug("[POST] add creator $request")

    val projectId = creatorService.getCreatorProject().id
    val creator = request.toDomain(projectId)

    return userService.addUser(creator).toNetwork()
  }

  @PostMapping(Endpoints.UNIVERSAL_USER_FORCE_VERIFY)
  fun forceVerifyUser(
    authentication: Authentication,
    @PathVariable universalId: String,
  ): SimpleResponse {
    log.debug("[PUT] force-verifying universal user $universalId")

    val userId = UserId.fromUniversalFormat(universalId)
    accessManager.requireProjectFunctional(userId.projectId)

    val user = accessManager.requestUserAccess(authentication, userId, Privilege.USER_WRITE_VERIFICATION)

    val updater = UserUpdater(id = user.id, verificationToken = Settable(null))
    userService.updateUser(updater)

    return SimpleResponse.DONE
  }

}
