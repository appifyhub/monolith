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
    const val AUTH_ADMINS = "/v1/admin/auth"
    const val REFRESH_USERS = "/v1/refresh"
    const val REFRESH_ADMINS = "/v1/admin/refresh"
    const val UNAUTH_USERS = "/v1/unauth"
    const val UNAUTH_ADMINS = "/v1/admin/unauth"
  }

  private val log = LoggerFactory.getLogger(this::class.java)

  @PostMapping(Endpoints.AUTH_USERS)
  fun authUser(
    @RequestBody creds: UserCredentialsRequest,
    @RequestHeader(Headers.PROJECT_SIGNATURE) projectSignature: String,
  ): TokenResponse {
    val user = try {
      authService.fetchUserByCredentials(projectSignature, creds.identifier, creds.secret, withTokens = false)
    } catch (t: Throwable) {
      log.warn("Failed to find user identified by ${creds.identifier}", t)
      unauthorized("Invalid credentials")
    }

    val token = authService.generateTokenFor(user, creds.origin)
    return TokenResponse(token)
  }

  @PostMapping(Endpoints.AUTH_ADMINS)
  fun authAdmin(
    @RequestBody creds: AdminCredentialsRequest,
  ): TokenResponse {
    val user = try {
      authService.fetchAdminUserByCredentials(creds.identifier, creds.secret, withTokens = false)
    } catch (t: Throwable) {
      log.warn("Failed to find admin identified by ${creds.identifier}", t)
      unauthorized("Invalid credentials")
    }

    val token = authService.generateTokenFor(user, creds.origin)
    return TokenResponse(token)
  }

  @GetMapping(Endpoints.UNAUTH_USERS)
  fun unauthUser(authentication: Authentication): MessageResponse {
    log.debug("[GET] unauth user with $authentication")

    authService.unauthorizeAuthenticationData(authentication)

    return MessageResponse("Done")
  }

  @GetMapping(Endpoints.UNAUTH_ADMINS)
  fun unauthAdmin(authentication: Authentication): MessageResponse {
    log.debug("[GET] unauth admin with $authentication")

    authService.unauthorizeAuthenticationData(authentication)

    return MessageResponse("Done")
  }

  // TODO MM refreshing tokens missing

}