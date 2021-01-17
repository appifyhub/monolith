package com.appifyhub.monolith.controller.auth

import com.appifyhub.monolith.controller.common.Headers
import com.appifyhub.monolith.network.auth.TokenResponse
import com.appifyhub.monolith.network.auth.UserCredentialsRequest
import com.appifyhub.monolith.network.common.MessageResponse
import com.appifyhub.monolith.service.auth.AuthService
import com.appifyhub.monolith.util.throwUnauthorized
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
  private val authService: AuthService,
) {

  object Endpoints {
    const val AUTH = "/v1/universal/auth"
    const val REFRESH = "/v1/universal/refresh"
    const val UNAUTH = "/v1/universal/unauth"

    const val ADMIN_AUTH = "/v1/admin/auth"
    const val ADMIN_REFRESH = "/v1/admin/refresh"
    const val ADMIN_UNAUTH = "/v1/admin/unauth"

    const val ANY_USER_UNAUTH = "/v1/projects/{projectId}/users/{id}/unauth"
  }

  private val log = LoggerFactory.getLogger(this::class.java)

  @PostMapping(Endpoints.AUTH)
  fun authUser(
    @RequestBody creds: UserCredentialsRequest,
    @RequestHeader(Headers.PROJECT_SIGNATURE) projectSignature: String,
  ): TokenResponse {
    log.debug("[POST] auth user with $creds for project $projectSignature")

    val user = try {
      authService.authUser(projectSignature, creds.identifier, creds.secret)
    } catch (t: Throwable) {
      log.warn("Failed to find user identified by ${creds.identifier}", t)
      throwUnauthorized { "Invalid credentials" }
    }

    val token = authService.createTokenFor(user, creds.origin)
    return TokenResponse(token)
  }

  @PostMapping(Endpoints.UNAUTH)
  fun unauthUser(authentication: Authentication, @RequestParam all: Boolean): MessageResponse {
    log.debug("[POST] unauth user with $authentication, [all $all]")
    if (all) {
      authService.unauthorizeAll(authentication)
    } else {
      authService.unauthorize(authentication)
    }
    return MessageResponse.DONE
  }

  @PostMapping(Endpoints.REFRESH)
  fun refreshUser(authentication: Authentication): TokenResponse {
    log.debug("[POST] refresh user with $authentication")
    val token = authService.refreshAuth(authentication)
    return TokenResponse(token)
  }

}