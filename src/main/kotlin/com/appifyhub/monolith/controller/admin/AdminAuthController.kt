package com.appifyhub.monolith.controller.admin

import com.appifyhub.monolith.controller.auth.UserAuthController
import com.appifyhub.monolith.domain.auth.OwnedToken
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.network.auth.AdminCredentialsRequest
import com.appifyhub.monolith.network.auth.TokenDetailsResponse
import com.appifyhub.monolith.network.auth.TokenResponse
import com.appifyhub.monolith.network.common.MessageResponse
import com.appifyhub.monolith.network.mapper.toNetwork
import com.appifyhub.monolith.service.auth.AuthService
import com.appifyhub.monolith.service.user.UserServiceImpl.UserPrivilege
import com.appifyhub.monolith.util.ext.throwUnauthorized
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminAuthController(
  private val authService: AuthService,
) {

  object Endpoints {
    const val ADMIN_AUTH = UserAuthController.Endpoints.ADMIN_AUTH

    const val ANY_USER_AUTH = UserAuthController.Endpoints.ANY_USER_AUTH
  }

  private val log = LoggerFactory.getLogger(this::class.java)

  // For self

  @PostMapping(Endpoints.ADMIN_AUTH)
  fun authAdmin(
    @RequestBody creds: AdminCredentialsRequest,
  ): TokenResponse {
    log.debug("[POST] auth admin with $creds")

    val user = try {
      authService.authAdmin(creds.universalId, creds.secret)
    } catch (t: Throwable) {
      log.warn("Failed to find admin identified by ${creds.universalId}", t)
      throwUnauthorized { "Invalid credentials" }
    }

    val token = authService.createTokenFor(user, creds.origin)
    return TokenResponse(token)
  }

  // For others

  @GetMapping(Endpoints.ANY_USER_AUTH)
  fun getAnyUserTokens(
    authentication: Authentication,
    @PathVariable projectId: Long,
    @PathVariable id: String,
    @RequestParam(required = false) valid: Boolean?,
  ): List<TokenDetailsResponse> {
    log.debug("[GET] get all tokens for user $id from project $projectId, [valid $valid]")

    val targetUserId = UserId(id, projectId)
    val targetUser = try {
      authService.requestAccessFor(authentication, targetUserId, UserPrivilege.WRITE)
    } catch (t: Throwable) {
      log.warn("Failed to get access", t)
      throwUnauthorized { t.message.orEmpty() }
    }

    val isSelf = targetUser.userId == targetUserId
    val tokens = if (isSelf) {
      authService.fetchAllTokenDetails(authentication, valid)
    } else {
      authService.fetchAllTokenDetailsFor(authentication, targetUser.userId, valid)
    }
    return tokens.map(OwnedToken::toNetwork)
  }

  @DeleteMapping(Endpoints.ANY_USER_AUTH)
  fun unauthAnyUser(
    authentication: Authentication,
    @PathVariable projectId: Long,
    @PathVariable id: String,
  ): MessageResponse {
    log.debug("[DELETE] unauth user $id from project $projectId")

    val targetUserId = UserId(id, projectId)
    val targetUser = try {
      authService.requestAccessFor(authentication, targetUserId, UserPrivilege.WRITE)
    } catch (t: Throwable) {
      log.warn("Failed to get access", t)
      throwUnauthorized { t.message.orEmpty() }
    }

    val isSelf = targetUser.userId == targetUserId
    if (isSelf) {
      authService.unauthorizeAll(authentication)
    } else {
      authService.unauthorizeAllFor(authentication, targetUser.userId)
    }

    return MessageResponse.DONE
  }

}
