package com.appifyhub.monolith.features.auth.api

import com.appifyhub.monolith.controller.common.Endpoints
import com.appifyhub.monolith.controller.common.RequestIpAddressHolder
import com.appifyhub.monolith.features.auth.api.model.TokenResponse
import com.appifyhub.monolith.features.auth.domain.service.AuthService
import com.appifyhub.monolith.network.creator.user.ops.ApiKeyRequest
import com.appifyhub.monolith.network.creator.user.ops.CreatorCredentialsRequest
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class CreatorAuthController(
  private val authService: AuthService,
) : RequestIpAddressHolder {

  private val log = LoggerFactory.getLogger(this::class.java)

  @PostMapping(Endpoints.CREATOR_AUTH)
  fun authenticate(
    @RequestBody creds: CreatorCredentialsRequest,
  ): TokenResponse {
    log.debug("[POST] auth creator with $creds")

    val user = authService.resolveCreator(creds.universalId, creds.signature)
    val token = authService.createTokenFor(user, creds.origin, getRequestIpAddress())
    return tokenResponseOf(token)
  }

  @PostMapping(Endpoints.CREATOR_API_KEY)
  fun createApiKey(
    authentication: Authentication,
    @RequestBody keyData: ApiKeyRequest,
  ): TokenResponse {
    log.debug("[POST] create API key")

    val authUser = authService.resolveShallowSelf(authentication)
    val apiKey = authService.createStaticTokenFor(authUser, keyData.origin, getRequestIpAddress())
    return tokenResponseOf(apiKey)
  }

}
