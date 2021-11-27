package com.appifyhub.monolith.controller.auth

import com.appifyhub.monolith.controller.common.RequestIpAddressHolder
import com.appifyhub.monolith.domain.auth.TokenDetails
import com.appifyhub.monolith.network.auth.TokenDetailsResponse
import com.appifyhub.monolith.network.auth.TokenResponse
import com.appifyhub.monolith.network.auth.UserCredentialsRequest
import com.appifyhub.monolith.network.common.MessageResponse
import com.appifyhub.monolith.network.mapper.toNetwork
import com.appifyhub.monolith.network.mapper.tokenResponseOf
import com.appifyhub.monolith.service.access.AccessManager
import com.appifyhub.monolith.service.auth.AuthService
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

  object Endpoints {
    const val AUTH = "/v1/universal/auth"
    const val CREATOR_AUTH = "/v1/creator/auth"
    const val ANY_USER_AUTH = "/v1/projects/{projectId}/users/{userId}/auth"
    const val CREATOR_API_KEY = "/v1/creator/apikey"

    const val TOKENS = "/v1/universal/auth/tokens"
    const val ANY_USER_TOKENS = "/v1/projects/{projectId}/users/{userId}/auth/tokens"
  }

  private val log = LoggerFactory.getLogger(this::class.java)

  @PostMapping(Endpoints.AUTH)
  fun authUser(
    @RequestBody creds: UserCredentialsRequest,
  ): TokenResponse {
    log.debug("[POST] auth user with $creds")

    val user = authService.resolveUser(creds.universalId, creds.secret)
      .also { accessManager.requireProjectFunctional(it.id.projectId) }

    val tokenValue = authService.createTokenFor(user, creds.origin, getRequestIpAddress())

    return tokenResponseOf(tokenValue)
  }

  @GetMapping(Endpoints.AUTH)
  fun getCurrentToken(
    authentication: Authentication,
  ): TokenDetailsResponse {
    log.debug("[GET] get current token")
    val currentToken = authService.fetchTokenDetails(authentication)
      .also { accessManager.requireProjectFunctional(it.ownerId.projectId) }
    return currentToken.toNetwork()
  }

  @GetMapping(Endpoints.TOKENS)
  fun getAllTokens(
    authentication: Authentication,
    @RequestParam(required = false) valid: Boolean?,
  ): List<TokenDetailsResponse> {
    log.debug("[GET] get all tokens, [valid $valid]")
    val tokens = authService.fetchAllTokenDetails(authentication, valid)
      .also { accessManager.requireProjectFunctional(it.first().ownerId.projectId) }
    return tokens.map(TokenDetails::toNetwork)
  }

  @PutMapping(Endpoints.AUTH)
  fun refreshUser(authentication: Authentication): TokenResponse {
    log.debug("[PUT] refresh user with $authentication")

    accessManager.requireProjectFunctional(
      targetId = authService.fetchTokenDetails(authentication).ownerId.projectId
    )

    val tokenValue = authService.refreshAuth(authentication, getRequestIpAddress())
    return tokenResponseOf(tokenValue)
  }

  @DeleteMapping(Endpoints.AUTH)
  fun unauthUser(
    authentication: Authentication,
    @RequestParam(required = false) all: Boolean? = false,
  ): MessageResponse {
    log.debug("[DELETE] unauth user with $authentication, [all $all]")

    accessManager.requireProjectFunctional(
      targetId = authService.fetchTokenDetails(authentication).ownerId.projectId
    )

    if (all == true) {
      authService.unauthorizeAll(authentication)
    } else {
      authService.unauthorize(authentication)
    }

    return MessageResponse.DONE
  }

  @DeleteMapping(Endpoints.TOKENS)
  fun unauthTokens(
    authentication: Authentication,
    @RequestParam tokenIds: List<String>,
  ): MessageResponse {
    log.debug("[DELETE] unauth tokens $tokenIds")

    accessManager.requireProjectFunctional(
      targetId = authService.fetchTokenDetails(authentication).ownerId.projectId
    )

    authService.unauthorizeTokens(authentication, tokenIds)
    return MessageResponse.DONE
  }

}
