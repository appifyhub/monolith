package com.appifyhub.monolith.controller.auth

import com.appifyhub.monolith.domain.auth.OwnedToken
import com.appifyhub.monolith.network.auth.TokenDetailsResponse
import com.appifyhub.monolith.network.auth.TokenResponse
import com.appifyhub.monolith.network.auth.UserCredentialsRequest
import com.appifyhub.monolith.network.common.MessageResponse
import com.appifyhub.monolith.network.mapper.toNetwork
import com.appifyhub.monolith.service.auth.AuthService
import com.appifyhub.monolith.util.ext.throwUnauthorized
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
) {

  object Endpoints {
    const val AUTH = "/v1/universal/auth"
    const val TOKENS = "/v1/universal/auth/tokens"

    const val ADMIN_AUTH = "/v1/admin/auth"

    const val ANY_USER_AUTH = "/v1/projects/{projectId}/users/{id}/auth"
  }

  private val log = LoggerFactory.getLogger(this::class.java)

  @PostMapping(Endpoints.AUTH)
  fun authUser(
    @RequestBody creds: UserCredentialsRequest,
  ): TokenResponse {
    log.debug("[POST] auth user with $creds")

    val user = try {
      authService.resolveUser(creds.universalId, creds.secret)
    } catch (t: Throwable) {
      log.warn("Failed to find user identified by ${creds.universalId}", t)
      throwUnauthorized { "Invalid credentials" }
    }

    val token = authService.createTokenFor(user, creds.origin)
    return TokenResponse(token)
  }

  @GetMapping(Endpoints.AUTH)
  fun getCurrentToken(
    authentication: Authentication,
  ): TokenDetailsResponse {
    log.debug("[GET] get current token")
    val currentToken = authService.fetchTokenDetails(authentication)
    return currentToken.toNetwork()
  }

  @GetMapping(Endpoints.TOKENS)
  fun getAllTokens(
    authentication: Authentication,
    @RequestParam(required = false) valid: Boolean?,
  ): List<TokenDetailsResponse> {
    log.debug("[GET] get all tokens, [valid $valid]")
    val tokens = authService.fetchAllTokenDetails(authentication, valid)
    return tokens.map(OwnedToken::toNetwork)
  }

  @PutMapping(Endpoints.AUTH)
  fun refreshUser(authentication: Authentication): TokenResponse {
    log.debug("[PUT] refresh user with $authentication")
    val token = authService.refreshAuth(authentication)
    return TokenResponse(token)
  }

  @DeleteMapping(Endpoints.AUTH)
  fun unauthUser(
    authentication: Authentication,
    @RequestParam(required = false) all: Boolean? = false,
  ): MessageResponse {
    log.debug("[DELETE] unauth user with $authentication, [all $all]")
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
    authService.unauthorizeTokens(authentication, tokenIds)
    return MessageResponse.DONE
  }

}
