package com.appifyhub.monolith.controller.auth

import com.appifyhub.monolith.controller.common.Headers
import com.appifyhub.monolith.network.auth.AdminCredentialsRequest
import com.appifyhub.monolith.network.auth.TokenResponse
import com.appifyhub.monolith.network.auth.UserCredentialsRequest
import com.appifyhub.monolith.network.common.MessageResponse
import com.appifyhub.monolith.service.auth.AuthService
import com.appifyhub.monolith.util.unauthorized
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
  private val authService: AuthService,
) {

  object Endpoints {
    const val AUTH_USERS = "/v1/auth"
    const val REFRESH_USERS = "/v1/refresh"
    const val UNAUTH_USERS = "/v1/unauth"

    // TODO MM to move to admin controller
    const val AUTH_ADMINS = "/v1/admin/auth"
    const val REFRESH_ADMINS = "/v1/admin/refresh"
    const val UNAUTH_ADMINS = "/v1/admin/unauth"
  }

  private val log = LoggerFactory.getLogger(this::class.java)

  @PostMapping(Endpoints.AUTH_USERS)
  fun authUser(
    @RequestBody creds: UserCredentialsRequest,
    @RequestHeader(Headers.PROJECT_SIGNATURE) projectSignature: String,
  ): TokenResponse {
    log.debug("[POST] auth user with $creds for project $projectSignature")

    val user = try {
      authService.authenticateUser(projectSignature, creds.identifier, creds.secret)
    } catch (t: Throwable) {
      log.warn("Failed to find user identified by ${creds.identifier}", t)
      unauthorized("Invalid credentials")
    }

    val token = authService.createTokenFor(user, creds.origin)
    return TokenResponse(token)
  }

  @PostMapping(Endpoints.AUTH_ADMINS)
  fun authAdmin(
    @RequestBody creds: AdminCredentialsRequest,
  ): TokenResponse {
    log.debug("[POST] auth admin with $creds")

    val user = try {
      authService.authenticateAdmin(creds.identifier, creds.secret)
    } catch (t: Throwable) {
      log.warn("Failed to find admin identified by ${creds.identifier}", t)
      unauthorized("Invalid credentials")
    }

    val token = authService.createTokenFor(user, creds.origin)
    return TokenResponse(token)
  }

  @GetMapping(Endpoints.UNAUTH_USERS)
  // TODO MM add 'all=true/false' query param to unauth all tokens
  fun unauthUser(authentication: Authentication): MessageResponse {
    log.debug("[GET] unauth user with $authentication")
    authService.unauthorizeAuthentication(authentication)
    return MessageResponse("Done")
  }

  @GetMapping(Endpoints.UNAUTH_ADMINS)
  // TODO MM add 'all=true/false' query param to unauth all tokens
  fun unauthAdmin(authentication: Authentication): MessageResponse {
    log.debug("[GET] unauth admin with $authentication")
    authService.unauthorizeAuthentication(authentication)
    return MessageResponse("Done")
  }

  @GetMapping(Endpoints.REFRESH_USERS)
  fun refreshUser(authentication: Authentication): TokenResponse {
    log.debug("[POST] refresh user with $authentication")
    val token = authService.refreshAuthentication(authentication)
    return TokenResponse(token)
  }

  @GetMapping(Endpoints.REFRESH_ADMINS)
  fun refreshAdmin(authentication: Authentication): TokenResponse {
    log.debug("[POST] refresh admin with $authentication")
    val token = authService.refreshAuthentication(authentication)
    return TokenResponse(token)
  }

}