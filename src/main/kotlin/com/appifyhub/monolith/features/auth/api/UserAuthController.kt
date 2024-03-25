package com.appifyhub.monolith.features.auth.api

import com.appifyhub.monolith.features.common.api.Endpoints
import com.appifyhub.monolith.features.common.api.RequestIpAddressHolder
import com.appifyhub.monolith.features.user.domain.model.UserId
import com.appifyhub.monolith.features.auth.api.model.TokenDetailsResponse
import com.appifyhub.monolith.features.auth.api.model.TokenResponse
import com.appifyhub.monolith.features.auth.api.model.UserCredentialsRequest
import com.appifyhub.monolith.features.auth.domain.access.AccessManager
import com.appifyhub.monolith.features.auth.domain.access.AccessManager.Privilege
import com.appifyhub.monolith.features.auth.domain.model.TokenDetails
import com.appifyhub.monolith.features.auth.domain.service.AuthService
import com.appifyhub.monolith.features.common.api.model.SimpleResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class UserAuthController(
  private val authService: AuthService,
  private val accessManager: AccessManager,
) : RequestIpAddressHolder {

  private val log = LoggerFactory.getLogger(this::class.java)

  @PostMapping(Endpoints.USER_AUTH)
  fun authenticate(
    @RequestBody creds: UserCredentialsRequest,
  ): TokenResponse {
    log.debug("[POST] auth user with $creds")

    val user = authService.resolveUser(creds.universalId, creds.signature)
    accessManager.requireProjectFunctional(user.id.projectId)

    val tokenValue = authService.createTokenFor(user, creds.origin, getRequestIpAddress())

    return tokenResponseOf(tokenValue)
  }

  @GetMapping(Endpoints.USER_AUTH)
  fun getCurrentToken(
    authentication: Authentication,
  ): TokenDetailsResponse {
    log.debug("[GET] get current token")

    val currentToken = authService.fetchTokenDetails(authentication)
    accessManager.requireProjectFunctional(currentToken.ownerId.projectId)

    return currentToken.toNetwork()
  }

  @GetMapping(Endpoints.USER_TOKENS)
  fun getAllTokens(
    authentication: Authentication,
    @RequestParam("user_id", required = false) universalUserId: String? = null,
    @RequestParam(required = false) valid: Boolean?,
  ): List<TokenDetailsResponse> {
    log.debug("[GET] get all tokens, universalUserId=$universalUserId, [valid $valid]")

    val authUser = authService.resolveShallowSelf(authentication)
    accessManager.requireProjectFunctional(authUser.id.projectId)

    val targetUserId = universalUserId?.let { UserId.fromUniversalFormat(it) } ?: authUser.id
    val targetUser = accessManager.requestUserAccess(authentication, targetUserId, Privilege.USER_READ_TOKEN)

    val tokens = if (authUser.id == targetUser.id) {
      // for self only
      authService.fetchAllTokenDetails(authentication, valid)
    } else {
      // for others
      authService.fetchAllTokenDetailsFor(authentication, targetUser.id, valid)
    }

    return tokens.map(TokenDetails::toNetwork)
  }

  @PutMapping(Endpoints.USER_AUTH)
  fun refresh(authentication: Authentication): TokenResponse {
    log.debug("[PUT] refresh user with $authentication")

    val token = authService.fetchTokenDetails(authentication)
    accessManager.requireProjectFunctional(token.ownerId.projectId)

    val tokenValue = authService.refreshAuth(authentication, getRequestIpAddress())
    return tokenResponseOf(tokenValue)
  }

  @DeleteMapping(Endpoints.USER_AUTH)
  fun unauthenticate(
    authentication: Authentication,
    @RequestParam("user_id", required = false) universalUserId: String? = null,
    @RequestParam(required = false) all: Boolean? = false,
  ): SimpleResponse {
    log.debug("[DELETE] unauth, universalUserId=$universalUserId, [all $all]")

    val authUser = authService.resolveShallowSelf(authentication)
    accessManager.requireProjectFunctional(authUser.id.projectId)

    val targetUserId = universalUserId?.let { UserId.fromUniversalFormat(it) } ?: authUser.id
    val targetUser = accessManager.requestUserAccess(authentication, targetUserId, Privilege.USER_WRITE_TOKEN)

    if (authUser.id == targetUser.id) {
      // for self only
      if (all == true) {
        authService.unauthorizeAll(authentication)
      } else {
        authService.unauthorize(authentication)
      }
    } else {
      // for others (treated as all=true)
      authService.unauthorizeAllFor(authentication, targetUser.id)
    }

    return SimpleResponse.DONE
  }

  @DeleteMapping(Endpoints.USER_TOKENS)
  fun unauthenticateTokens(
    authentication: Authentication,
    @RequestParam("token_ids") tokenIds: List<String>,
  ): SimpleResponse {
    log.debug("[DELETE] unauth tokens $tokenIds")

    val token = authService.fetchTokenDetails(authentication)
    accessManager.requireProjectFunctional(token.ownerId.projectId)

    authService.unauthorizeTokens(authentication, tokenIds)
    return SimpleResponse.DONE
  }

}
