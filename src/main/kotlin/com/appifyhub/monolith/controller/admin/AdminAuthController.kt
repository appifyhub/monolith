package com.appifyhub.monolith.controller.admin

import com.appifyhub.monolith.controller.auth.UserAuthController
import com.appifyhub.monolith.controller.common.RequestIpAddressHolder
import com.appifyhub.monolith.domain.auth.TokenDetails
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.network.auth.AdminCredentialsRequest
import com.appifyhub.monolith.network.auth.ApiKeyRequest
import com.appifyhub.monolith.network.auth.TokenDetailsResponse
import com.appifyhub.monolith.network.auth.TokenResponse
import com.appifyhub.monolith.network.common.MessageResponse
import com.appifyhub.monolith.network.mapper.toNetwork
import com.appifyhub.monolith.network.mapper.tokenResponseOf
import com.appifyhub.monolith.service.auth.AuthService
import com.appifyhub.monolith.service.user.UserService.Privilege
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
) : RequestIpAddressHolder {

  object Endpoints {
    const val ADMIN_AUTH = UserAuthController.Endpoints.ADMIN_AUTH
    const val ADMIN_API_KEY = UserAuthController.Endpoints.ADMIN_API_KEY

    const val ANY_USER_AUTH = UserAuthController.Endpoints.ANY_USER_AUTH
    const val ANY_USER_TOKENS = UserAuthController.Endpoints.ANY_USER_TOKENS
  }

  private val log = LoggerFactory.getLogger(this::class.java)

  // For self

  @PostMapping(Endpoints.ADMIN_AUTH)
  fun authAdmin(
    @RequestBody creds: AdminCredentialsRequest,
  ): TokenResponse {
    log.debug("[POST] auth admin with $creds")

    val user = authService.resolveAdmin(creds.universalId, creds.secret)
    val token = authService.createTokenFor(user, creds.origin, getRequestIpAddress())
    return tokenResponseOf(token)
  }

  @PostMapping(Endpoints.ADMIN_API_KEY)
  fun createApiKey(
    authentication: Authentication,
    @RequestBody keyData: ApiKeyRequest,
  ): TokenResponse {
    log.debug("[POST] create API key")

    val authUser = authService.resolveShallowSelf(authentication)
    val apiKey = authService.createStaticTokenFor(authUser, keyData.origin, getRequestIpAddress())
    return tokenResponseOf(apiKey)
  }

  // For others

  @GetMapping(Endpoints.ANY_USER_TOKENS)
  fun getAnyUserTokens(
    authentication: Authentication,
    @PathVariable projectId: Long,
    @PathVariable userId: String,
    @RequestParam(required = false) valid: Boolean?,
  ): List<TokenDetailsResponse> {
    log.debug("[GET] get all tokens for user $userId from project $projectId, [valid $valid]")

    val authUser = authService.resolveShallowSelf(authentication)
    val targetUser = authService.requestUserAccess(
      authData = authentication,
      targetId = UserId(userId, projectId),
      privilege = Privilege.USER_READ
    )

    val tokens = if (targetUser.id == authUser.id) {
      authService.fetchAllTokenDetails(authentication, valid) // for self only
    } else {
      authService.fetchAllTokenDetailsFor(authentication, targetUser.id, valid)
    }

    return tokens.map(TokenDetails::toNetwork)
  }

  @DeleteMapping(Endpoints.ANY_USER_AUTH)
  fun unauthAnyUser(
    authentication: Authentication,
    @PathVariable projectId: Long,
    @PathVariable userId: String,
  ): MessageResponse {
    log.debug("[DELETE] unauth user $userId from project $projectId")

    val authUser = authService.resolveShallowSelf(authentication)
    val targetUser = authService.requestUserAccess(authentication, UserId(userId, projectId), Privilege.USER_WRITE)

    if (targetUser.id == authUser.id) {
      authService.unauthorizeAll(authentication) // for self only
    } else {
      authService.unauthorizeAllFor(authentication, targetUser.id)
    }

    return MessageResponse.DONE
  }

}
