package com.appifyhub.monolith.controller.auth

import com.appifyhub.monolith.controller.common.Endpoints
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

  private val log = LoggerFactory.getLogger(this::class.java)

  @PostMapping(Endpoints.AUTH)
  fun authenticate(
    @RequestBody creds: UserCredentialsRequest,
  ): TokenResponse {
    log.debug("[POST] auth user with $creds")

    val user = authService.resolveUser(creds.universalId, creds.signature)
    accessManager.requireProjectFunctional(user.id.projectId)

    val tokenValue = authService.createTokenFor(user, creds.origin, getRequestIpAddress())

    return tokenResponseOf(tokenValue)
  }

  @GetMapping(Endpoints.AUTH)
  fun getCurrentToken(
    authentication: Authentication,
  ): TokenDetailsResponse {
    log.debug("[GET] get current token")

    val currentToken = authService.fetchTokenDetails(authentication)
    accessManager.requireProjectFunctional(currentToken.ownerId.projectId)

    return currentToken.toNetwork()
  }

  @GetMapping(Endpoints.TOKENS)
  fun getAllTokens(
    authentication: Authentication,
    @RequestParam(required = false) valid: Boolean?,
  ): List<TokenDetailsResponse> {
    log.debug("[GET] get all tokens, [valid $valid]")

    val tokens = authService.fetchAllTokenDetails(authentication, valid)
    accessManager.requireProjectFunctional(tokens.first().ownerId.projectId)

    return tokens.map(TokenDetails::toNetwork)
  }

  @PutMapping(Endpoints.AUTH)
  fun refresh(authentication: Authentication): TokenResponse {
    log.debug("[PUT] refresh user with $authentication")

    val token = authService.fetchTokenDetails(authentication)
    accessManager.requireProjectFunctional(token.ownerId.projectId)

    val tokenValue = authService.refreshAuth(authentication, getRequestIpAddress())
    return tokenResponseOf(tokenValue)
  }

  @DeleteMapping(Endpoints.AUTH)
  fun unauthenticate(
    authentication: Authentication,
    @RequestParam(required = false) all: Boolean? = false,
  ): MessageResponse {
    log.debug("[DELETE] unauth user with $authentication, [all $all]")

    val token = authService.fetchTokenDetails(authentication)
    accessManager.requireProjectFunctional(token.ownerId.projectId)

    if (all == true) {
      authService.unauthorizeAll(authentication)
    } else {
      authService.unauthorize(authentication)
    }

    return MessageResponse.DONE
  }

  @DeleteMapping(Endpoints.TOKENS)
  fun unauthenticateTokens(
    authentication: Authentication,
    @RequestParam tokenIds: List<String>,
  ): MessageResponse {
    log.debug("[DELETE] unauth tokens $tokenIds")

    val token = authService.fetchTokenDetails(authentication)
    accessManager.requireProjectFunctional(token.ownerId.projectId)

    authService.unauthorizeTokens(authentication, tokenIds)
    return MessageResponse.DONE
  }

}
