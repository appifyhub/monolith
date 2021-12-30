package com.appifyhub.monolith.controller.user

import com.appifyhub.monolith.controller.common.Endpoints
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.network.mapper.toDomain
import com.appifyhub.monolith.network.mapper.toNetwork
import com.appifyhub.monolith.network.user.UserResponse
import com.appifyhub.monolith.network.user.ops.UserSignupRequest
import com.appifyhub.monolith.network.user.ops.UserUpdateAuthorityRequest
import com.appifyhub.monolith.network.user.ops.UserUpdateDataRequest
import com.appifyhub.monolith.network.user.ops.UserUpdateSignatureRequest
import com.appifyhub.monolith.service.access.AccessManager
import com.appifyhub.monolith.service.access.AccessManager.Privilege
import com.appifyhub.monolith.service.auth.AuthService
import com.appifyhub.monolith.service.user.UserService
import com.appifyhub.monolith.util.ext.throwNotFound
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
  private val userService: UserService,
  private val authService: AuthService,
  private val accessManager: AccessManager,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  @PostMapping(Endpoints.ANY_PROJECT_SIGNUP)
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

  @GetMapping(Endpoints.ANY_PROJECT_SEARCH)
  fun searchUsers(
    authentication: Authentication,
    @PathVariable projectId: Long,
    @RequestParam("user_name") userName: String? = null,
    @RequestParam("user_contact") userContact: String? = null,
  ): List<UserResponse> {
    log.debug("[GET] user search in project $projectId with name $userName or $userContact")

    accessManager.requireProjectFunctional(projectId)
    val project = accessManager.requestProjectAccess(authentication, projectId, Privilege.USER_SEARCH)

    return when {
      userName != null -> userService.searchByName(project.id, userName).map(User::toNetwork)
      userContact != null -> userService.searchByContact(project.id, userContact).map(User::toNetwork)
      else -> throwNotFound { "At least one user property is required for querying" }
    }
  }

  @PutMapping(Endpoints.ANY_USER_UNIVERSAL_AUTHORITY)
  fun updateAuthority(
    authentication: Authentication,
    @PathVariable universalId: String,
    @RequestBody request: UserUpdateAuthorityRequest,
  ): UserResponse {
    log.debug("[PUT] authorization update for $universalId with data $request")

    val userId = UserId.fromUniversalFormat(universalId)
    accessManager.requireProjectFunctional(userId.projectId)

    accessManager.requestUserAccess(authentication, userId, Privilege.USER_WRITE_AUTHORITY)
    val updater = request.toDomain(userId)

    return userService.updateUser(updater).toNetwork()
  }

  @PutMapping(Endpoints.ANY_USER_UNIVERSAL_DATA)
  fun updateData(
    authentication: Authentication,
    @PathVariable universalId: String,
    @RequestBody request: UserUpdateDataRequest,
  ): UserResponse {
    log.debug("[PUT] data update for $universalId with data $request")

    val userId = UserId.fromUniversalFormat(universalId)
    accessManager.requireProjectFunctional(userId.projectId)

    accessManager.requestUserAccess(authentication, userId, Privilege.USER_WRITE_DATA)
    val updater = request.toDomain(userId)

    return userService.updateUser(updater).toNetwork()
  }

  @PutMapping(Endpoints.ANY_USER_UNIVERSAL_SIGNATURE)
  fun updateData(
    authentication: Authentication,
    @PathVariable universalId: String,
    @RequestBody request: UserUpdateSignatureRequest,
    @RequestParam logout: Boolean,
  ): UserResponse {
    log.debug("[PUT] signature update for $universalId with data $request, logout=$logout")

    val userId = UserId.fromUniversalFormat(universalId)
    accessManager.requireProjectFunctional(userId.projectId)

    accessManager.requestUserAccess(authentication, userId, Privilege.USER_WRITE_SIGNATURE)
    authService.resolveUser(universalId, request.rawSignatureOld)
    if (logout) accessManager.requestUserAccess(authentication, userId, Privilege.USER_WRITE_TOKEN)

    val updater = request.toDomain(userId)
    val updated = userService.updateUser(updater).toNetwork()

    if (logout) authService.unauthorizeAllFor(authentication, userId)

    return updated
  }

}
