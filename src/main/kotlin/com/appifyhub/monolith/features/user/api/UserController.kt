package com.appifyhub.monolith.features.user.api

import com.appifyhub.monolith.features.common.api.Endpoints
import com.appifyhub.monolith.features.common.domain.model.Settable
import com.appifyhub.monolith.features.auth.domain.access.AccessManager
import com.appifyhub.monolith.features.auth.domain.access.AccessManager.Privilege
import com.appifyhub.monolith.features.auth.domain.service.AuthService
import com.appifyhub.monolith.features.user.api.model.UserResponse
import com.appifyhub.monolith.features.user.api.model.UserSignupRequest
import com.appifyhub.monolith.features.user.api.model.UserUpdateAuthorityRequest
import com.appifyhub.monolith.features.user.api.model.UserUpdateDataRequest
import com.appifyhub.monolith.features.user.api.model.UserUpdateSignatureRequest
import com.appifyhub.monolith.features.user.domain.model.User
import com.appifyhub.monolith.features.user.domain.model.UserId
import com.appifyhub.monolith.features.user.domain.model.UserUpdater
import com.appifyhub.monolith.features.user.domain.service.UserService
import com.appifyhub.monolith.features.common.api.model.SimpleResponse
import com.appifyhub.monolith.util.extension.throwNotFound
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
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

  @PostMapping(Endpoints.PROJECT_SIGNUP)
  fun addUser(
    @PathVariable projectId: Long,
    @RequestBody request: UserSignupRequest,
  ): UserResponse {
    log.debug("[POST] add user $request")

    accessManager.requireProjectFunctional(projectId)
    val creator = request.toDomain(projectId)

    return userService.addUser(creator).toNetwork()
  }

  @GetMapping(Endpoints.UNIVERSAL_USER)
  fun getUser(
    authentication: Authentication,
    @PathVariable universalId: String,
  ): UserResponse {
    log.debug("[GET] universal user $universalId")

    val userId = UserId.fromUniversalFormat(universalId)
    accessManager.requireProjectFunctional(userId.projectId)

    return accessManager.requestUserAccess(authentication, userId, Privilege.USER_READ_DATA).toNetwork()
  }

  @GetMapping(Endpoints.PROJECT_USER_SEARCH)
  fun searchUsers(
    authentication: Authentication,
    @PathVariable projectId: Long,
    @RequestParam("user_name", required = false) userName: String? = null,
    @RequestParam("user_contact", required = false) userContact: String? = null,
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

  @PutMapping(Endpoints.UNIVERSAL_USER_AUTHORITY)
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

  @PutMapping(Endpoints.UNIVERSAL_USER_DATA)
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

  @PutMapping(Endpoints.UNIVERSAL_USER_SIGNATURE)
  fun updateSignature(
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

  @PutMapping(Endpoints.UNIVERSAL_USER_VERIFY)
  fun verifyToken(
    @PathVariable universalId: String,
    @PathVariable verificationToken: String,
  ): SimpleResponse {
    log.debug("[PUT] token verification for $universalId with token $verificationToken")

    val userId = UserId.fromUniversalFormat(universalId)
    accessManager.requireProjectFunctional(userId.projectId)

    val user = userService.fetchUserByUserIdAndVerificationToken(userId, verificationToken)

    val updater = UserUpdater(id = user.id, verificationToken = Settable(null))
    userService.updateUser(updater)

    return SimpleResponse.DONE
  }

  @PutMapping(Endpoints.UNIVERSAL_USER_SIGNATURE_RESET)
  fun resetSignature(
    @PathVariable universalId: String,
  ): SimpleResponse {
    log.debug("[PUT] resetting signature for $universalId")

    val userId = UserId.fromUniversalFormat(universalId)
    accessManager.requireProjectFunctional(userId.projectId)

    val user = userService.resetSignatureById(userId)
    authService.unauthorizeAllFor(user.id)

    return SimpleResponse.DONE
  }

  @DeleteMapping(Endpoints.UNIVERSAL_USER)
  fun deleteUser(
    authentication: Authentication,
    @PathVariable universalId: String,
  ): SimpleResponse {
    log.debug("[DELETE] remove user $universalId")

    val userId = UserId.fromUniversalFormat(universalId)
    accessManager.requireProjectFunctional(userId.projectId)

    accessManager.requestUserAccess(authentication, userId, Privilege.USER_DELETE)
    userService.removeUserById(userId)

    return SimpleResponse.DONE
  }

}
